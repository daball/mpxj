/*
 * file:       AbstractCalendarFactory.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       2017-10-04
 */

/*
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */

package net.sf.mpxj.mpp

import java.io.IOException
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.LinkedList

import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream

import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.DayType
import net.sf.mpxj.EventManager
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectCalendarWeek
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.common.Pair

/**
 * Shared code used to read calendar data from MPP files.
 */
internal abstract class AbstractCalendarFactory
/**
 * Constructor.
 *
 * @param file parent ProjectFile instance
 */
(private val m_file: ProjectFile) : CalendarFactory {

    /**
     * Retrieve the Calendar ID offset.
     *
     * @return Calendar ID offset
     */
    protected abstract val calendarIDOffset: Int

    /**
     * Retrieve the Base Calendar ID offset.
     *
     * @return BaseCalendar ID offset
     */
    protected abstract val baseIDOffset: Int

    /**
     * Retrieve the Resource ID offset.
     *
     * @return Resource ID offset
     */
    protected abstract val resourceIDOffset: Int

    /**
     * Retrieve the offset to the start of each calendar hours block.
     *
     * @return calendar hours offset
     */
    protected abstract val calendarHoursOffset: Int

    /**
     * Retrieve the VarData type containing the calendar name.
     *
     * @return VarData type
     */
    protected abstract val calendarNameVarDataType: Integer

    /**
     * Retrieve the VarData type containing the calendar data.
     *
     * @return VarData type
     */
    protected abstract val calendarDataVarDataType: Integer

    /**
     * The format of the calendar data is a 4 byte header followed
     * by 7x 60 byte blocks, one for each day of the week. Optionally
     * following this is a set of 64 byte blocks representing exceptions
     * to the calendar.
     *
     * @param projectDir project data directory in the MPP file
     * @param projectProps project properties
     * @param inputStreamFactory input stream factory
     * @param resourceMap map of resources to calendars
     * @throws IOException
     */
    @Override
    @Throws(IOException::class)
    override fun processCalendarData(projectDir: DirectoryEntry, projectProps: Props, inputStreamFactory: DocumentInputStreamFactory, resourceMap: HashMap<Integer, ProjectCalendar>) {
        val calDir = projectDir.getEntry("TBkndCal") as DirectoryEntry

        //MPPUtility.fileHexDump("c:\\temp\\varmeta.txt", new DocumentInputStream (((DocumentEntry)calDir.getEntry("VarMeta"))));

        val calVarMeta = getCalendarVarMeta(calDir)
        val calVarData = Var2Data(calVarMeta, DocumentInputStream(calDir.getEntry("Var2Data") as DocumentEntry))

        //      System.out.println(calVarMeta);
        //      System.out.println(calVarData);

        val calFixedMeta = FixedMeta(DocumentInputStream(calDir.getEntry("FixedMeta") as DocumentEntry), 10)
        val calFixedData = FixedData(calFixedMeta, inputStreamFactory.getInstance(calDir, "FixedData"), 12)

        //      System.out.println(calFixedMeta);
        //      System.out.println(calFixedData);

        //      FixedMeta calFixed2Meta = new FixedMeta(new DocumentInputStream(((DocumentEntry) calDir.getEntry("Fixed2Meta"))), 9);
        //      FixedData calFixed2Data = new FixedData(calFixed2Meta, inputStreamFactory.getInstance(calDir, "Fixed2Data"), 48);
        //      System.out.println(calFixed2Meta);
        //      System.out.println(calFixed2Data);

        val calendarMap = HashMap<Integer, ProjectCalendar>()
        val items = calFixedData.itemCount
        val baseCalendars = LinkedList<Pair<ProjectCalendar, Integer>>()
        val defaultCalendarData = projectProps.getByteArray(Props.DEFAULT_CALENDAR_HOURS)
        val defaultCalendar = ProjectCalendar(m_file)
        processCalendarHours(defaultCalendarData, null, defaultCalendar, true)

        val eventManager = m_file.eventManager

        for (loop in 0 until items) {
            val fixedData = calFixedData.getByteArrayValue(loop)
            if (fixedData != null && fixedData.size >= 8) {
                var offset = 0

                //
                // Bug 890909, here we ensure that we have a complete 12 byte
                // block before attempting to process the data.
                //
                while (offset + 12 <= fixedData.size) {
                    val calendarID = Integer.valueOf(MPPUtility.getInt(fixedData, offset + calendarIDOffset))
                    val baseCalendarID = MPPUtility.getInt(fixedData, offset + baseIDOffset)

                    if (calendarID.intValue() > 0 && calendarMap.containsKey(calendarID) === false) {
                        var varData = calVarData.getByteArray(calendarID, calendarDataVarDataType)
                        val cal: ProjectCalendar

                        if (baseCalendarID == 0 || baseCalendarID == -1 || baseCalendarID == calendarID.intValue()) {
                            if (varData != null || defaultCalendarData != null) {
                                cal = m_file.addCalendar()
                                if (varData == null) {
                                    varData = defaultCalendarData
                                }
                            } else {
                                cal = m_file.addDefaultBaseCalendar()
                            }

                            cal.name = calVarData.getUnicodeString(calendarID, calendarNameVarDataType)
                        } else {
                            if (varData != null) {
                                cal = m_file.addCalendar()
                            } else {
                                cal = m_file.addDefaultDerivedCalendar()
                            }

                            baseCalendars.add(Pair<ProjectCalendar, Integer>(cal, Integer.valueOf(baseCalendarID)))
                            val resourceID = Integer.valueOf(MPPUtility.getInt(fixedData, offset + resourceIDOffset))
                            resourceMap.put(resourceID, cal)
                        }

                        cal.uniqueID = calendarID

                        if (varData != null) {
                            processCalendarHours(varData, defaultCalendar, cal, baseCalendarID == -1)
                            processCalendarExceptions(varData, cal)
                        }

                        calendarMap.put(calendarID, cal)
                        eventManager.fireCalendarReadEvent(cal)
                    }

                    offset += 12
                }
            }
        }

        updateBaseCalendarNames(baseCalendars, calendarMap)
    }

    /**
     * For a given set of calendar data, this method sets the working
     * day status for each day, and if present, sets the hours for that
     * day.
     *
     * NOTE: MPP14 defines the concept of working weeks. MPXJ does not
     * currently support this, and thus we only read the working hours
     * for the default working week.
     *
     * @param data calendar data block
     * @param defaultCalendar calendar to use for default values
     * @param cal calendar instance
     * @param isBaseCalendar true if this is a base calendar
     */
    private fun processCalendarHours(data: ByteArray?, defaultCalendar: ProjectCalendar?, cal: ProjectCalendar, isBaseCalendar: Boolean) {
        // Dump out the calendar related data and fields.
        //MPPUtility.dataDump(data, true, false, false, false, true, false, true);

        var offset: Int
        var hours: ProjectCalendarHours
        var periodIndex: Int
        var index: Int
        var defaultFlag: Int
        var periodCount: Int
        var start: Date
        var duration: Long
        var day: Day
        val dateRanges = ArrayList<DateRange>(5)

        index = 0
        while (index < 7) {
            offset = calendarHoursOffset + 60 * index
            defaultFlag = if (data == null) 1 else MPPUtility.getShort(data, offset)
            day = Day.getInstance(index + 1)

            if (defaultFlag == 1) {
                if (isBaseCalendar) {
                    if (defaultCalendar == null) {
                        cal.setWorkingDay(day, DEFAULT_WORKING_WEEK[index])
                        if (cal.isWorkingDay(day)) {
                            hours = cal.addCalendarHours(Day.getInstance(index + 1))
                            hours.addRange(ProjectCalendarWeek.DEFAULT_WORKING_MORNING)
                            hours.addRange(ProjectCalendarWeek.DEFAULT_WORKING_AFTERNOON)
                        }
                    } else {
                        val workingDay = defaultCalendar.isWorkingDay(day)
                        cal.setWorkingDay(day, workingDay)
                        if (workingDay) {
                            hours = cal.addCalendarHours(Day.getInstance(index + 1))
                            for (range in defaultCalendar.getHours(day)!!) {
                                hours.addRange(range)
                            }
                        }
                    }
                } else {
                    cal.setWorkingDay(day, DayType.DEFAULT)
                }
            } else {
                dateRanges.clear()

                periodIndex = 0
                periodCount = MPPUtility.getShort(data, offset + 2)
                while (periodIndex < periodCount) {
                    val startOffset = offset + 8 + periodIndex * 2
                    start = MPPUtility.getTime(data, startOffset)
                    val durationOffset = offset + 20 + periodIndex * 4
                    duration = MPPUtility.getDuration(data, durationOffset)
                    val end = Date(start.getTime() + duration)
                    dateRanges.add(DateRange(start, end))
                    ++periodIndex
                }

                if (dateRanges.isEmpty()) {
                    cal.setWorkingDay(day, false)
                } else {
                    cal.setWorkingDay(day, true)
                    hours = cal.addCalendarHours(Day.getInstance(index + 1))

                    for (range in dateRanges) {
                        hours.addRange(range)
                    }
                }
            }
            index++
        }
    }

    /**
     * The way calendars are stored in an MPP14 file means that there
     * can be forward references between the base calendar unique ID for a
     * derived calendar, and the base calendar itself. To get around this,
     * we initially populate the base calendar name attribute with the
     * base calendar unique ID, and now in this method we can convert those
     * ID values into the correct names.
     *
     * @param baseCalendars list of calendars and base calendar IDs
     * @param map map of calendar ID values and calendar objects
     */
    private fun updateBaseCalendarNames(baseCalendars: List<Pair<ProjectCalendar, Integer>>, map: HashMap<Integer, ProjectCalendar>) {
        for (pair in baseCalendars) {
            val cal = pair.first
            val baseCalendarID = pair.second
            val baseCal = map.get(baseCalendarID)
            if (baseCal != null && baseCal!!.name != null) {
                cal!!.parent = baseCal
            } else {
                // Remove invalid calendar to avoid serious problems later.
                m_file.removeCalendar(cal)
            }
        }
    }

    /**
     * Retrieve the calendar VarMeta data.
     *
     * @param directory calendar directory
     * @return VarMeta instance
     */
    @Throws(IOException::class)
    protected abstract fun getCalendarVarMeta(directory: DirectoryEntry): VarMeta

    /**
     * This method extracts any exceptions associated with a calendar.
     *
     * @param data calendar data block
     * @param cal calendar instance
     */
    protected abstract fun processCalendarExceptions(data: ByteArray, cal: ProjectCalendar)

    companion object {

        /**
         * Default working week.
         */
        private val DEFAULT_WORKING_WEEK = booleanArrayOf(false, true, true, true, true, true, false)
    }
}
