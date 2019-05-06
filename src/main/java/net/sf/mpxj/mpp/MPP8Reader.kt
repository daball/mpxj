/*
 * file:       MPP8Reader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       08/05/2003
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

import java.io.FileNotFoundException
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.LinkedList

import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream

import net.sf.mpxj.AccrueType
import net.sf.mpxj.Column
import net.sf.mpxj.ConstraintType
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.MPXJException
import net.sf.mpxj.Priority
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Rate
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.Table
import net.sf.mpxj.TableContainer
import net.sf.mpxj.Task
import net.sf.mpxj.TaskType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.View
import net.sf.mpxj.common.MPPResourceField
import net.sf.mpxj.common.MPPTaskField
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.Pair
import net.sf.mpxj.common.RtfHelper

/**
 * This class is used to represent a Microsoft Project MPP8 file. This
 * implementation allows the file to be read, and the data it contains
 * exported as a set of MPX objects. These objects can be interrogated
 * to retrieve any required data, or stored as an MPX file.
 */
internal class MPP8Reader : MPPVariantReader {

    //   private static void dumpUnknownData (String name, int[][] spec, byte[] data)
    //   {
    //      System.out.println (name);
    //      for (int loop=0; loop < spec.length; loop++)
    //      {
    //         System.out.println (spec[loop][0] + ": "+ ByteArrayHelper.hexdump(data, spec[loop][0], spec[loop][1], false));
    //      }
    //      System.out.println ();
    //   }

    //
    //   private static final int[][] UNKNOWN_TASK_DATA = new int[][]
    //   {
    //      {8, 12}, // includes known flags
    //      {36, 12},
    //      {50, 18},
    //      {86, 2},
    //      {142, 2},
    //      {144, 4},
    //      {148, 4},
    //      {152, 4},
    //      {164, 4},
    //      {268, 4}, // includes known flags
    //      {274, 32}, // includes known flags
    //      {306, 6}
    //   };
    //
    //   private static final int[][] UNKNOWN_CALENDAR_DATA = new int[][]
    //   {
    //      {8, 12},
    //      {24, 8}
    //   };
    //
    //   private static final int[][] UNKNOWN_ASSIGNMENT_DATA = new int[][]
    //   {
    //     {4, 12},
    //     {32, 79},
    //     {82, 2},
    //     {102, 6},
    //     {108, 6},
    //     {120, 12},
    //     {102, 6},
    //     {144, 12},
    //     {162, 42}
    //   };

    private var m_reader: MPPReader? = null
    private var m_file: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_calendarMap: HashMap<Integer, ProjectCalendar>? = null
    private var m_root: DirectoryEntry? = null
    private var m_projectDir: DirectoryEntry? = null
    private var m_viewDir: DirectoryEntry? = null
    /**
     * This method is used to process an MPP8 file. This is the file format
     * used by Project 98.
     *
     * @param reader parent file reader
     * @param file Parent MPX file
     * @param root Root of the POI file system.
     * @throws MPXJException
     * @throws IOException
     */
    @Override
    @Throws(MPXJException::class, IOException::class)
    override fun process(reader: MPPReader, file: ProjectFile, root: DirectoryEntry) {
        try {
            populateMemberData(reader, file, root)
            processProjectProperties()

            if (!reader.readPropertiesOnly) {
                processCalendarData()
                processResourceData()
                processTaskData()
                processConstraintData()
                processAssignmentData()

                if (reader.readPresentationData) {
                    processViewPropertyData()
                    processViewData()
                    processTableData()
                }
            }
        } finally {
            clearMemberData()
        }
    }

    /**
     * Populate member data used by the rest of the reader.
     *
     * @param reader parent file reader
     * @param file parent MPP file
     * @param root Root of the POI file system.
     */
    @Throws(IOException::class)
    private fun populateMemberData(reader: MPPReader, file: ProjectFile, root: DirectoryEntry) {
        m_reader = reader
        m_root = root
        m_file = file
        m_eventManager = file.eventManager

        m_calendarMap = HashMap<Integer, ProjectCalendar>()
        m_projectDir = root.getEntry("   1") as DirectoryEntry
        m_viewDir = root.getEntry("   2") as DirectoryEntry

        m_file!!.projectProperties.mppFileType = Integer.valueOf(8)
    }

    /**
     * Clear transient member data.
     */
    private fun clearMemberData() {
        m_reader = null
        m_root = null
        m_eventManager = null
        m_file = null
        m_calendarMap = null
        m_projectDir = null
        m_viewDir = null
    }

    /**
     * Process the project properties data.
     */
    @Throws(MPXJException::class, IOException::class)
    private fun processProjectProperties() {
        val props = Props8(DocumentInputStream(m_projectDir!!.getEntry("Props") as DocumentEntry))
        val reader = ProjectPropertiesReader()
        reader.process(m_file, props, m_root)
    }

    /**
     * This method process the data held in the props file specific to the
     * visual appearance of the project data.
     */
    @Throws(IOException::class)
    private fun processViewPropertyData() {
        val props = Props8(DocumentInputStream(m_viewDir!!.getEntry("Props") as DocumentEntry))

        val properties = m_file!!.projectProperties
        properties.showProjectSummaryTask = props.getBoolean(Props.SHOW_PROJECT_SUMMARY_TASK)
    }

    /**
     * This method extracts and collates calendar data.
     *
     * @throws MPXJException
     * @throws IOException
     */
    @Throws(MPXJException::class, IOException::class)
    private fun processCalendarData() {
        val calDir = m_projectDir!!.getEntry("TBkndCal") as DirectoryEntry
        val calendarFixedData = FixFix(36, DocumentInputStream(calDir.getEntry("FixFix   0") as DocumentEntry))
        val calendarVarData = FixDeferFix(DocumentInputStream(calDir.getEntry("FixDeferFix   0") as DocumentEntry))

        var cal: ProjectCalendar
        var hours: ProjectCalendarHours
        var exception: ProjectCalendarException
        var name: String?
        var baseData: ByteArray?
        var extData: ByteArray?

        var periodCount: Int
        var index: Int
        var offset: Int
        var defaultFlag: Int
        var start: Date
        var duration: Long
        var exceptionCount: Int

        //
        // Configure default time ranges
        //
        val df = SimpleDateFormat("HH:mm")
        val defaultStart1: Date
        val defaultEnd1: Date
        val defaultStart2: Date
        val defaultEnd2: Date

        try {
            defaultStart1 = df.parse("08:00")
            defaultEnd1 = df.parse("12:00")
            defaultStart2 = df.parse("13:00")
            defaultEnd2 = df.parse("17:00")
        } catch (ex: ParseException) {
            throw MPXJException(MPXJException.INVALID_FORMAT, ex)
        }

        val calendars = calendarFixedData.itemCount
        var calendarID: Int
        var baseCalendarID: Int
        var periodIndex: Int
        var day: Day
        val baseCalendars = LinkedList<Pair<ProjectCalendar, Integer>>()

        for (loop in 0 until calendars) {
            baseData = calendarFixedData.getByteArrayValue(loop)
            calendarID = MPPUtility.getInt(baseData, 0)
            baseCalendarID = MPPUtility.getInt(baseData, 4)
            name = calendarVarData.getUnicodeString(getOffset(baseData, 20))

            //
            // Uncommenting the call to this method is useful when trying
            // to determine the function of unknown task data.
            //
            //dumpUnknownData (name + " " + MPPUtility.getInt(baseData), UNKNOWN_CALENDAR_DATA, baseData);

            //
            // Skip calendars with negative ID values
            //
            if (calendarID < 0) {
                continue
            }

            //
            // Populate the basic calendar
            //
            val ed = ExtendedData(calendarVarData, getOffset(baseData, 32))
            offset = -1 - ed.getInt(Integer.valueOf(8))

            if (offset == -1) {
                if (baseCalendarID > 0) {
                    cal = m_file!!.addDefaultDerivedCalendar()
                    baseCalendars.add(Pair<ProjectCalendar, Integer>(cal, Integer.valueOf(baseCalendarID)))
                } else {
                    cal = m_file!!.addDefaultBaseCalendar()
                    cal.name = name
                }

                cal.uniqueID = Integer.valueOf(calendarID)
            } else {
                if (baseCalendarID > 0) {
                    cal = m_file!!.addCalendar()
                    baseCalendars.add(Pair<ProjectCalendar, Integer>(cal, Integer.valueOf(baseCalendarID)))
                } else {
                    cal = m_file!!.addCalendar()
                    cal.name = name
                }

                cal.uniqueID = Integer.valueOf(calendarID)

                extData = calendarVarData.getByteArray(offset)

                index = 0
                while (index < 7) {
                    offset = 4 + 40 * index

                    defaultFlag = MPPUtility.getShort(extData, offset)
                    day = Day.getInstance(index + 1)

                    if (defaultFlag == 1) {
                        cal.setWorkingDay(day, DEFAULT_WORKING_WEEK[index])
                        if (cal.isWorkingDay(day) == true) {
                            hours = cal.addCalendarHours(net.sf.mpxj.Day.getInstance(index + 1))
                            hours.addRange(DateRange(defaultStart1, defaultEnd1))
                            hours.addRange(DateRange(defaultStart2, defaultEnd2))
                        }
                    } else {
                        periodCount = MPPUtility.getShort(extData, offset + 2)
                        if (periodCount == 0) {
                            cal.setWorkingDay(day, false)
                        } else {
                            cal.setWorkingDay(day, true)
                            hours = cal.addCalendarHours(Day.getInstance(index + 1))

                            periodIndex = 0
                            while (periodIndex < periodCount) {
                                start = MPPUtility.getTime(extData, offset + 8 + periodIndex * 2)
                                duration = MPPUtility.getDuration(extData, offset + 16 + periodIndex * 4)
                                hours.addRange(DateRange(start, Date(start.getTime() + duration)))
                                periodIndex++
                            }
                        }
                    }
                    index++
                }

                //
                // Handle any exceptions
                //
                exceptionCount = MPPUtility.getShort(extData, 0)
                if (exceptionCount != 0) {
                    index = 0
                    while (index < exceptionCount) {
                        offset = 4 + 40 * 7 + index * 44

                        val fromDate = MPPUtility.getDate(extData, offset)
                        val toDate = MPPUtility.getDate(extData, offset + 2)
                        exception = cal.addCalendarException(fromDate, toDate)

                        periodCount = MPPUtility.getShort(extData, offset + 6)
                        if (periodCount != 0) {
                            for (exceptionPeriodIndex in 0 until periodCount) {
                                start = MPPUtility.getTime(extData, offset + 12 + exceptionPeriodIndex * 2)
                                duration = MPPUtility.getDuration(extData, offset + 20 + exceptionPeriodIndex * 4)
                                exception.addRange(DateRange(start, Date(start.getTime() + duration)))
                            }
                        }
                        index++
                    }
                }
            }

            m_calendarMap!!.put(Integer.valueOf(calendarID), cal)
            m_eventManager!!.fireCalendarReadEvent(cal)
        }

        updateBaseCalendarNames(baseCalendars)
    }

    /**
     * The way calendars are stored in an MPP8 file means that there
     * can be forward references between the base calendar unique ID for a
     * derived calendar, and the base calendar itself. To get around this,
     * we initially populate the base calendar name attribute with the
     * base calendar unique ID, and now in this method we can convert those
     * ID values into the correct names.
     *
     * @param baseCalendars list of calendars and base calendar IDs
     */
    private fun updateBaseCalendarNames(baseCalendars: List<Pair<ProjectCalendar, Integer>>) {
        for (pair in baseCalendars) {
            val cal = pair.first
            val baseCalendarID = pair.second
            val baseCal = m_calendarMap!!.get(baseCalendarID)
            if (baseCal != null) {
                cal!!.parent = baseCal
            }
        }
    }

    /**
     * This method extracts and collates task data.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processTaskData() {
        val taskDir = m_projectDir!!.getEntry("TBkndTask") as DirectoryEntry
        var taskFixedData = FixFix(316, DocumentInputStream(taskDir.getEntry("FixFix   0") as DocumentEntry))
        if (taskFixedData.diff != 0) {
            taskFixedData = FixFix(366, DocumentInputStream(taskDir.getEntry("FixFix   0") as DocumentEntry))
        }

        var taskVarData: FixDeferFix? = null
        var taskExtData: ExtendedData? = null

        val tasks = taskFixedData.itemCount
        var data: ByteArray?
        var uniqueID: Int
        var id: Int
        var deleted: Int
        var task: Task
        var autoWBS = true
        val flags = ByteArray(3)
        var recurringTaskReader: RecurringTaskReader? = null
        val properties = m_file!!.projectProperties
        val defaultProjectTimeUnits = properties.defaultDurationUnits

        for (loop in 0 until tasks) {
            data = taskFixedData.getByteArrayValue(loop)

            //
            // Test for a valid unique id
            //
            uniqueID = MPPUtility.getInt(data, 0)
            if (uniqueID < 1) {
                continue
            }

            //
            // Test to ensure this task has not been deleted.
            // This appears to be a set of flags rather than a single value.
            // The data I have seen to date shows deleted tasks having values of
            // 0x0001 and 0x0002. Valid tasks have had values of 0x0000, 0x0914,
            // 0x0040, 0x004A, 0x203D and 0x0031
            //
            deleted = MPPUtility.getShort(data, 272)
            if (deleted and 0xC0 == 0 && deleted and 0x03 != 0 && deleted != 0x0031 && deleted != 0x203D) {
                continue
            }

            //
            // Load the var data if we have not already done so
            //
            if (taskVarData == null) {
                taskVarData = FixDeferFix(DocumentInputStream(taskDir.getEntry("FixDeferFix   0") as DocumentEntry))
            }

            //
            // Blank rows can be present in MPP files. The following flag
            // appears to indicate that a row is blank, and should be
            // ignored.
            //
            if (data!![8] and 0x01 != 0) {
                continue
            }

            taskExtData = ExtendedData(taskVarData, getOffset(data, 312))
            val recurringData = taskExtData.getByteArray(TASK_RECURRING_DATA)

            id = MPPUtility.getInt(data, 4)
            flags[0] = (data[268] and data[303]).toByte()
            flags[1] = (data[269] and data[304]).toByte()
            flags[2] = (data[270] and data[305]).toByte()

            task = m_file!!.addTask()

            task.actualCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 234).toDouble() / 100)
            task.actualDuration = MPPUtility.getAdjustedDuration(properties, MPPUtility.getInt(data, 74), MPPUtility.getDurationTimeUnits(MPPUtility.getShort(data, 72), defaultProjectTimeUnits))
            task.actualFinish = MPPUtility.getTimestamp(data, 108)
            task.actualOvertimeCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 210).toDouble() / 100)
            task.actualOvertimeWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 192).toDouble() / 100, TimeUnit.HOURS)
            task.actualStart = MPPUtility.getTimestamp(data, 104)
            task.actualWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 180).toDouble() / 100, TimeUnit.HOURS)
            //task.setACWP(); // Calculated value
            //task.setAssignment(); // Calculated value
            //task.setAssignmentDelay(); // Calculated value
            //task.setAssignmentUnits(); // Calculated value
            task.baselineCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 246).toDouble() / 100)
            task.baselineDuration = MPPUtility.getAdjustedDuration(properties, MPPUtility.getInt(data, 82), MPPUtility.getDurationTimeUnits(MPPUtility.getShort(data, 72), defaultProjectTimeUnits))
            task.baselineFinish = MPPUtility.getTimestamp(data, 116)
            task.baselineStart = MPPUtility.getTimestamp(data, 112)
            task.baselineWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 174).toDouble() / 100, TimeUnit.HOURS)
            //task.setBCWP(); // Calculated value
            //task.setBCWS(); // Calculated value
            //task.setConfirmed(); // Calculated value
            task.constraintDate = MPPUtility.getTimestamp(data, 120)
            task.constraintType = ConstraintType.getInstance(MPPUtility.getShort(data, 88))
            task.contact = taskExtData.getUnicodeString(TASK_CONTACT)
            task.cost = NumberHelper.getDouble(MPPUtility.getLong6(data, 222).toDouble() / 100)
            task.setCost(1, NumberHelper.getDouble(taskExtData.getLong(TASK_COST1).toDouble() / 100))
            task.setCost(2, NumberHelper.getDouble(taskExtData.getLong(TASK_COST2).toDouble() / 100))
            task.setCost(3, NumberHelper.getDouble(taskExtData.getLong(TASK_COST3).toDouble() / 100))
            task.setCost(4, NumberHelper.getDouble(taskExtData.getLong(TASK_COST4).toDouble() / 100))
            task.setCost(5, NumberHelper.getDouble(taskExtData.getLong(TASK_COST5).toDouble() / 100))
            task.setCost(6, NumberHelper.getDouble(taskExtData.getLong(TASK_COST6).toDouble() / 100))
            task.setCost(7, NumberHelper.getDouble(taskExtData.getLong(TASK_COST7).toDouble() / 100))
            task.setCost(8, NumberHelper.getDouble(taskExtData.getLong(TASK_COST8).toDouble() / 100))
            task.setCost(9, NumberHelper.getDouble(taskExtData.getLong(TASK_COST9).toDouble() / 100))
            task.setCost(10, NumberHelper.getDouble(taskExtData.getLong(TASK_COST10).toDouble() / 100))
            //task.setCostRateTable(); // Calculated value
            //task.setCostVariance(); // Populated below
            task.createDate = MPPUtility.getTimestamp(data, 138)
            //task.setCritical(); // Calculated value
            //task.setCV(); // Calculated value
            task.setDate(1, taskExtData.getTimestamp(TASK_DATE1))
            task.setDate(2, taskExtData.getTimestamp(TASK_DATE2))
            task.setDate(3, taskExtData.getTimestamp(TASK_DATE3))
            task.setDate(4, taskExtData.getTimestamp(TASK_DATE4))
            task.setDate(5, taskExtData.getTimestamp(TASK_DATE5))
            task.setDate(6, taskExtData.getTimestamp(TASK_DATE6))
            task.setDate(7, taskExtData.getTimestamp(TASK_DATE7))
            task.setDate(8, taskExtData.getTimestamp(TASK_DATE8))
            task.setDate(9, taskExtData.getTimestamp(TASK_DATE9))
            task.setDate(10, taskExtData.getTimestamp(TASK_DATE10))
            //task.setDelay(); // No longer supported by MS Project?
            task.duration = MPPUtility.getAdjustedDuration(properties, MPPUtility.getInt(data, 68), MPPUtility.getDurationTimeUnits(MPPUtility.getShort(data, 72), defaultProjectTimeUnits))
            task.setDuration(1, MPPUtility.getAdjustedDuration(properties, taskExtData.getInt(TASK_DURATION1), MPPUtility.getDurationTimeUnits(taskExtData.getShort(TASK_DURATION1_UNITS), defaultProjectTimeUnits)))
            task.setDuration(2, MPPUtility.getAdjustedDuration(properties, taskExtData.getInt(TASK_DURATION2), MPPUtility.getDurationTimeUnits(taskExtData.getShort(TASK_DURATION2_UNITS), defaultProjectTimeUnits)))
            task.setDuration(3, MPPUtility.getAdjustedDuration(properties, taskExtData.getInt(TASK_DURATION3), MPPUtility.getDurationTimeUnits(taskExtData.getShort(TASK_DURATION3_UNITS), defaultProjectTimeUnits)))
            task.setDuration(4, MPPUtility.getAdjustedDuration(properties, taskExtData.getInt(TASK_DURATION4), MPPUtility.getDurationTimeUnits(taskExtData.getShort(TASK_DURATION4_UNITS), defaultProjectTimeUnits)))
            task.setDuration(5, MPPUtility.getAdjustedDuration(properties, taskExtData.getInt(TASK_DURATION5), MPPUtility.getDurationTimeUnits(taskExtData.getShort(TASK_DURATION5_UNITS), defaultProjectTimeUnits)))
            task.setDuration(6, MPPUtility.getAdjustedDuration(properties, taskExtData.getInt(TASK_DURATION6), MPPUtility.getDurationTimeUnits(taskExtData.getShort(TASK_DURATION6_UNITS), defaultProjectTimeUnits)))
            task.setDuration(7, MPPUtility.getAdjustedDuration(properties, taskExtData.getInt(TASK_DURATION7), MPPUtility.getDurationTimeUnits(taskExtData.getShort(TASK_DURATION7_UNITS), defaultProjectTimeUnits)))
            task.setDuration(8, MPPUtility.getAdjustedDuration(properties, taskExtData.getInt(TASK_DURATION8), MPPUtility.getDurationTimeUnits(taskExtData.getShort(TASK_DURATION8_UNITS), defaultProjectTimeUnits)))
            task.setDuration(9, MPPUtility.getAdjustedDuration(properties, taskExtData.getInt(TASK_DURATION9), MPPUtility.getDurationTimeUnits(taskExtData.getShort(TASK_DURATION9_UNITS), defaultProjectTimeUnits)))
            task.setDuration(10, MPPUtility.getAdjustedDuration(properties, taskExtData.getInt(TASK_DURATION10), MPPUtility.getDurationTimeUnits(taskExtData.getShort(TASK_DURATION10_UNITS), defaultProjectTimeUnits)))
            //task.setDurationVariance(); // Calculated value
            task.earlyFinish = MPPUtility.getTimestamp(data, 20)
            task.earlyStart = MPPUtility.getTimestamp(data, 96)
            task.effortDriven = data[17] and 0x08 != 0
            //task.setExternalTask(); // Calculated value
            task.finish = MPPUtility.getTimestamp(data, 20)
            task.setFinish(1, taskExtData.getTimestamp(TASK_FINISH1))
            task.setFinish(2, taskExtData.getTimestamp(TASK_FINISH2))
            task.setFinish(3, taskExtData.getTimestamp(TASK_FINISH3))
            task.setFinish(4, taskExtData.getTimestamp(TASK_FINISH4))
            task.setFinish(5, taskExtData.getTimestamp(TASK_FINISH5))
            task.setFinish(6, taskExtData.getTimestamp(TASK_FINISH6))
            task.setFinish(7, taskExtData.getTimestamp(TASK_FINISH7))
            task.setFinish(8, taskExtData.getTimestamp(TASK_FINISH8))
            task.setFinish(9, taskExtData.getTimestamp(TASK_FINISH9))
            task.setFinish(10, taskExtData.getTimestamp(TASK_FINISH10))
            //task.setFinishVariance(); // Calculated value
            task.fixedCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 228).toDouble() / 100)
            task.fixedCostAccrual = AccrueType.getInstance(MPPUtility.getShort(data, 136))
            task.setFlag(1, flags[0] and 0x02 != 0)
            task.setFlag(2, flags[0] and 0x04 != 0)
            task.setFlag(3, flags[0] and 0x08 != 0)
            task.setFlag(4, flags[0] and 0x10 != 0)
            task.setFlag(5, flags[0] and 0x20 != 0)
            task.setFlag(6, flags[0] and 0x40 != 0)
            task.setFlag(7, flags[0] and 0x80 != 0)
            task.setFlag(8, flags[1] and 0x01 != 0)
            task.setFlag(9, flags[1] and 0x02 != 0)
            task.setFlag(10, flags[1] and 0x04 != 0)
            task.setFlag(11, flags[1] and 0x08 != 0)
            task.setFlag(12, flags[1] and 0x10 != 0)
            task.setFlag(13, flags[1] and 0x20 != 0)
            task.setFlag(14, flags[1] and 0x40 != 0)
            task.setFlag(15, flags[1] and 0x80 != 0)
            task.setFlag(16, flags[2] and 0x01 != 0)
            task.setFlag(17, flags[2] and 0x02 != 0)
            task.setFlag(18, flags[2] and 0x04 != 0)
            task.setFlag(19, flags[2] and 0x08 != 0)
            task.setFlag(20, flags[2] and 0x10 != 0) // note that this is not correct
            //task.setFreeSlack();  // Calculated value
            task.hideBar = data[16] and 0x01 != 0
            processHyperlinkData(task, taskVarData.getByteArray(-1 - taskExtData.getInt(TASK_HYPERLINK)))
            task.id = Integer.valueOf(id)
            //task.setIndicators(); // Calculated value
            task.lateFinish = MPPUtility.getTimestamp(data, 160)
            task.lateStart = MPPUtility.getTimestamp(data, 24)
            task.levelAssignments = data[19] and 0x10 != 0
            task.levelingCanSplit = data[19] and 0x08 != 0
            task.levelingDelay = MPPUtility.getDuration(MPPUtility.getInt(data, 90).toDouble() / 3, MPPUtility.getDurationTimeUnits(MPPUtility.getShort(data, 94), defaultProjectTimeUnits))
            //task.setLinkedFields();  // Calculated value
            task.marked = data[13] and 0x02 != 0
            task.milestone = data[12] and 0x01 != 0
            task.name = taskVarData.getUnicodeString(getOffset(data, 264))
            task.setNumber(1, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER1)))
            task.setNumber(2, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER2)))
            task.setNumber(3, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER3)))
            task.setNumber(4, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER4)))
            task.setNumber(5, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER5)))
            task.setNumber(6, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER6)))
            task.setNumber(7, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER7)))
            task.setNumber(8, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER8)))
            task.setNumber(9, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER9)))
            task.setNumber(10, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER10)))
            task.setNumber(11, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER11)))
            task.setNumber(12, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER12)))
            task.setNumber(13, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER13)))
            task.setNumber(14, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER14)))
            task.setNumber(15, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER15)))
            task.setNumber(16, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER16)))
            task.setNumber(17, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER17)))
            task.setNumber(18, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER18)))
            task.setNumber(19, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER19)))
            task.setNumber(20, NumberHelper.getDouble(taskExtData.getDouble(TASK_NUMBER20)))
            //task.setObjects(); // Calculated value
            task.outlineLevel = Integer.valueOf(MPPUtility.getShort(data, 48))
            //task.setOutlineNumber(); // Calculated value
            //task.setOverallocated(); // Calculated value
            task.overtimeCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 204).toDouble() / 100)
            //task.setOvertimeWork(); // Calculated value
            //task.getPredecessors(); // Calculated value
            task.percentageComplete = MPPUtility.getPercentage(data, 130)
            task.percentageWorkComplete = MPPUtility.getPercentage(data, 132)
            task.preleveledFinish = MPPUtility.getTimestamp(data, 148)
            task.preleveledStart = MPPUtility.getTimestamp(data, 144)
            task.priority = Priority.getInstance((MPPUtility.getShort(data, 128) + 1) * 100)
            //task.setProject(); // Calculated value
            task.recurring = MPPUtility.getShort(data, 142) != 0
            //task.setRegularWork(); // Calculated value
            task.remainingCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 240).toDouble() / 100)
            task.remainingDuration = MPPUtility.getAdjustedDuration(properties, MPPUtility.getInt(data, 78), MPPUtility.getDurationTimeUnits(MPPUtility.getShort(data, 72), defaultProjectTimeUnits))
            task.remainingOvertimeCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 216).toDouble() / 100)
            task.remainingOvertimeWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 198).toDouble() / 100, TimeUnit.HOURS)
            task.remainingWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 186).toDouble() / 100, TimeUnit.HOURS)
            //task.setResourceGroup(); // Calculated value from resource
            //task.setResourceInitials(); // Calculated value from resource
            //task.setResourceNames(); // Calculated value from resource
            //task.setResourcePhonetics(); // Calculated value from resource
            //task.setResponsePending(); // Calculated value
            task.resume = MPPUtility.getTimestamp(data, 32)
            //task.setResumeNoEarlierThan(); // Not in MSP98?
            task.rollup = data[15] and 0x04 != 0
            task.start = MPPUtility.getTimestamp(data, 96)
            task.setStart(1, taskExtData.getTimestamp(TASK_START1))
            task.setStart(2, taskExtData.getTimestamp(TASK_START2))
            task.setStart(3, taskExtData.getTimestamp(TASK_START3))
            task.setStart(4, taskExtData.getTimestamp(TASK_START4))
            task.setStart(5, taskExtData.getTimestamp(TASK_START5))
            task.setStart(6, taskExtData.getTimestamp(TASK_START6))
            task.setStart(7, taskExtData.getTimestamp(TASK_START7))
            task.setStart(8, taskExtData.getTimestamp(TASK_START8))
            task.setStart(9, taskExtData.getTimestamp(TASK_START9))
            task.setStart(10, taskExtData.getTimestamp(TASK_START10))
            //task.setStartVariance(); // Calculated value
            task.stop = MPPUtility.getTimestamp(data, 124)
            //task.setSubprojectFile();
            //task.setSubprojectReadOnly();
            //task.setSuccessors(); // Calculated value
            //task.setSummary(); // Automatically generated by MPXJ
            //task.setSV(); // Calculated value
            //task.teamStatusPending(); // Calculated value
            task.setText(1, taskExtData.getUnicodeString(TASK_TEXT1))
            task.setText(2, taskExtData.getUnicodeString(TASK_TEXT2))
            task.setText(3, taskExtData.getUnicodeString(TASK_TEXT3))
            task.setText(4, taskExtData.getUnicodeString(TASK_TEXT4))
            task.setText(5, taskExtData.getUnicodeString(TASK_TEXT5))
            task.setText(6, taskExtData.getUnicodeString(TASK_TEXT6))
            task.setText(7, taskExtData.getUnicodeString(TASK_TEXT7))
            task.setText(8, taskExtData.getUnicodeString(TASK_TEXT8))
            task.setText(9, taskExtData.getUnicodeString(TASK_TEXT9))
            task.setText(10, taskExtData.getUnicodeString(TASK_TEXT10))
            task.setText(11, taskExtData.getUnicodeString(TASK_TEXT11))
            task.setText(12, taskExtData.getUnicodeString(TASK_TEXT12))
            task.setText(13, taskExtData.getUnicodeString(TASK_TEXT13))
            task.setText(14, taskExtData.getUnicodeString(TASK_TEXT14))
            task.setText(15, taskExtData.getUnicodeString(TASK_TEXT15))
            task.setText(16, taskExtData.getUnicodeString(TASK_TEXT16))
            task.setText(17, taskExtData.getUnicodeString(TASK_TEXT17))
            task.setText(18, taskExtData.getUnicodeString(TASK_TEXT18))
            task.setText(19, taskExtData.getUnicodeString(TASK_TEXT19))
            task.setText(20, taskExtData.getUnicodeString(TASK_TEXT20))
            task.setText(21, taskExtData.getUnicodeString(TASK_TEXT21))
            task.setText(22, taskExtData.getUnicodeString(TASK_TEXT22))
            task.setText(23, taskExtData.getUnicodeString(TASK_TEXT23))
            task.setText(24, taskExtData.getUnicodeString(TASK_TEXT24))
            task.setText(25, taskExtData.getUnicodeString(TASK_TEXT25))
            task.setText(26, taskExtData.getUnicodeString(TASK_TEXT26))
            task.setText(27, taskExtData.getUnicodeString(TASK_TEXT27))
            task.setText(28, taskExtData.getUnicodeString(TASK_TEXT28))
            task.setText(29, taskExtData.getUnicodeString(TASK_TEXT29))
            task.setText(30, taskExtData.getUnicodeString(TASK_TEXT30))
            //task.setTotalSlack(); // Calculated value
            task.type = TaskType.getInstance(MPPUtility.getShort(data, 134))
            task.uniqueID = Integer.valueOf(uniqueID)
            //task.setUniqueIDPredecessors(); // Calculated value
            //task.setUniqueIDSuccessors(); // Calculated value
            //task.setUpdateNeeded(); // Calculated value
            task.wbs = taskExtData.getUnicodeString(TASK_WBS)
            task.work = MPPUtility.getDuration(MPPUtility.getLong6(data, 168).toDouble() / 100, TimeUnit.HOURS)
            //task.setWorkContour(); // Calculated from resource
            //task.setWorkVariance(); // Calculated value

            //
            // Retrieve task recurring data
            //
            if (recurringData != null) {
                if (recurringTaskReader == null) {
                    recurringTaskReader = RecurringTaskReader(properties)
                }
                recurringTaskReader.processRecurringTask(task, recurringData)
            }


            //
            // Retrieve the task notes.
            //
            setTaskNotes(task, data, taskExtData, taskVarData)

            //
            // If we have a WBS value from the MPP file, don't autogenerate
            //
            if (task.wbs != null) {
                autoWBS = false
            }

            m_eventManager!!.fireTaskReadEvent(task)

            //
            // Uncommenting the call to this method is useful when trying
            // to determine the function of unknown task data.
            //
            //dumpUnknownData (task.getName(), UNKNOWN_TASK_DATA, data);
        }

        //
        // Enable auto WBS if necessary
        //
        m_file!!.projectConfig.autoWBS = autoWBS
    }

    /**
     * There appear to be two ways of representing task notes in an MPP8 file.
     * This method tries to determine which has been used.
     *
     * @param task task
     * @param data task data
     * @param taskExtData extended task data
     * @param taskVarData task var data
     */
    private fun setTaskNotes(task: Task, data: ByteArray, taskExtData: ExtendedData, taskVarData: FixDeferFix) {
        var notes = taskExtData.getString(TASK_NOTES)
        if (notes == null && data.size == 366) {
            val offsetData = taskVarData.getByteArray(getOffset(data, 362))
            if (offsetData != null && offsetData.size >= 12) {
                notes = taskVarData.getString(getOffset(offsetData, 8))

                // We do pick up some random stuff with this approach, and
                // we don't know enough about the file format to know when to ignore it
                // so we'll use a heuristic here to ignore anything that
                // doesn't look like RTF.
                if (notes != null && notes.indexOf('{') === -1) {
                    notes = null
                }
            }
        }

        if (notes != null) {
            if (m_reader!!.preserveNoteFormatting == false) {
                notes = RtfHelper.strip(notes)
            }

            task.notes = notes
        }
    }

    /**
     * This method is used to extract the task hyperlink attributes
     * from a block of data and call the appropriate modifier methods
     * to configure the specified task object.
     *
     * @param task task instance
     * @param data hyperlink data block
     */
    private fun processHyperlinkData(task: Task, data: ByteArray?) {
        if (data != null) {
            var offset = 12
            val hyperlink: String
            val address: String
            val subaddress: String

            offset += 12
            hyperlink = MPPUtility.getUnicodeString(data, offset)
            offset += (hyperlink.length() + 1) * 2

            offset += 12
            address = MPPUtility.getUnicodeString(data, offset)
            offset += (address.length() + 1) * 2

            offset += 12
            subaddress = MPPUtility.getUnicodeString(data, offset)

            task.hyperlink = hyperlink
            task.hyperlinkAddress = address
            task.hyperlinkSubAddress = subaddress
        }
    }

    /**
     * This method extracts and collates constraint data.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processConstraintData() {
        //
        // Locate the directory containing the constraints
        //
        var consDir: DirectoryEntry?

        try {
            consDir = m_projectDir!!.getEntry("TBkndCons") as DirectoryEntry
        } catch (ex: FileNotFoundException) {
            consDir = null
        }

        //
        // It appears possible that valid MPP8 files can be generated without
        // this directory, so only process constraints if the directory
        // exists.
        //
        if (consDir != null) {
            val consFixedData = FixFix(36, DocumentInputStream(consDir.getEntry("FixFix   0") as DocumentEntry))
            val count = consFixedData.itemCount

            for (loop in 0 until count) {
                val data = consFixedData.getByteArrayValue(loop)

                if (MPPUtility.getInt(data, 28) == 0) {
                    val taskID1 = MPPUtility.getInt(data, 12)
                    val taskID2 = MPPUtility.getInt(data, 16)

                    if (taskID1 != taskID2) {
                        val task1 = m_file!!.getTaskByUniqueID(Integer.valueOf(taskID1))
                        val task2 = m_file!!.getTaskByUniqueID(Integer.valueOf(taskID2))
                        if (task1 != null && task2 != null) {
                            val type = RelationType.getInstance(MPPUtility.getShort(data, 20))
                            val durationUnits = MPPUtility.getDurationTimeUnits(MPPUtility.getShort(data, 22))
                            val lag = MPPUtility.getDuration(MPPUtility.getInt(data, 24), durationUnits)
                            val relation = task2.addPredecessor(task1, type, lag)
                            m_eventManager!!.fireRelationReadEvent(relation)
                        }
                    }
                }
            }
        }
    }

    /**
     * This method extracts and collates resource data.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processResourceData() {
        val rscDir = m_projectDir!!.getEntry("TBkndRsc") as DirectoryEntry
        val rscFixedData = FixFix(196, DocumentInputStream(rscDir.getEntry("FixFix   0") as DocumentEntry))
        var rscVarData: FixDeferFix? = null
        var rscExtData: ExtendedData? = null

        val resources = rscFixedData.itemCount
        var data: ByteArray?
        var id: Int
        var resource: Resource
        var notes: String?
        var calendar: ProjectCalendar

        for (loop in 0 until resources) {
            data = rscFixedData.getByteArrayValue(loop)

            //
            // Test for a valid unique id
            //
            id = MPPUtility.getInt(data, 0)
            if (id < 1) {
                continue
            }

            //
            // Blank rows can be present in MPP files. The following flag
            // appears to indicate that a row is blank, and should be
            // ignored.
            //
            if (data!![8] and 0x01 != 0) {
                continue
            }

            //
            // Test to ensure this resource has not been deleted
            // This may be an array of bit flags, as per the task
            // record. I have yet to see data to support this, so
            // the simple non-zero test remains.
            //
            if (MPPUtility.getShort(data, 164) != 0) {
                continue
            }

            //
            // Load the var data if we have not already done so
            //
            if (rscVarData == null) {
                rscVarData = FixDeferFix(DocumentInputStream(rscDir.getEntry("FixDeferFix   0") as DocumentEntry))
            }

            rscExtData = ExtendedData(rscVarData, getOffset(data, 192))

            resource = m_file!!.addResource()

            resource.accrueAt = AccrueType.getInstance(MPPUtility.getShort(data, 20))
            resource.actualCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 114).toDouble() / 100)
            resource.actualOvertimeCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 144).toDouble() / 100)
            resource.actualWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 62).toDouble() / 100, TimeUnit.HOURS)
            resource.availableFrom = MPPUtility.getTimestamp(data, 28)
            resource.availableTo = MPPUtility.getTimestamp(data, 32)
            //resource.setBaseCalendar();
            resource.baselineCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 126).toDouble() / 100)
            resource.baselineWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 68).toDouble() / 100, TimeUnit.HOURS)
            resource.code = rscExtData.getUnicodeString(RESOURCE_CODE)
            resource.cost = NumberHelper.getDouble(MPPUtility.getLong6(data, 120).toDouble() / 100)
            resource.setCost(1, NumberHelper.getDouble(rscExtData.getLong(RESOURCE_COST1).toDouble() / 100))
            resource.setCost(2, NumberHelper.getDouble(rscExtData.getLong(RESOURCE_COST2).toDouble() / 100))
            resource.setCost(3, NumberHelper.getDouble(rscExtData.getLong(RESOURCE_COST3).toDouble() / 100))
            resource.setCost(4, NumberHelper.getDouble(rscExtData.getLong(RESOURCE_COST4).toDouble() / 100))
            resource.setCost(5, NumberHelper.getDouble(rscExtData.getLong(RESOURCE_COST5).toDouble() / 100))
            resource.setCost(6, NumberHelper.getDouble(rscExtData.getLong(RESOURCE_COST6).toDouble() / 100))
            resource.setCost(7, NumberHelper.getDouble(rscExtData.getLong(RESOURCE_COST7).toDouble() / 100))
            resource.setCost(8, NumberHelper.getDouble(rscExtData.getLong(RESOURCE_COST8).toDouble() / 100))
            resource.setCost(9, NumberHelper.getDouble(rscExtData.getLong(RESOURCE_COST9).toDouble() / 100))
            resource.setCost(10, NumberHelper.getDouble(rscExtData.getLong(RESOURCE_COST10).toDouble() / 100))
            resource.costPerUse = NumberHelper.getDouble(MPPUtility.getLong6(data, 80).toDouble() / 100)
            resource.setDate(1, rscExtData.getTimestamp(RESOURCE_DATE1))
            resource.setDate(2, rscExtData.getTimestamp(RESOURCE_DATE2))
            resource.setDate(3, rscExtData.getTimestamp(RESOURCE_DATE3))
            resource.setDate(4, rscExtData.getTimestamp(RESOURCE_DATE4))
            resource.setDate(5, rscExtData.getTimestamp(RESOURCE_DATE5))
            resource.setDate(6, rscExtData.getTimestamp(RESOURCE_DATE6))
            resource.setDate(7, rscExtData.getTimestamp(RESOURCE_DATE7))
            resource.setDate(8, rscExtData.getTimestamp(RESOURCE_DATE8))
            resource.setDate(9, rscExtData.getTimestamp(RESOURCE_DATE9))
            resource.setDate(10, rscExtData.getTimestamp(RESOURCE_DATE10))
            resource.setDuration(1, MPPUtility.getDuration(rscExtData.getInt(RESOURCE_DURATION1), MPPUtility.getDurationTimeUnits(rscExtData.getShort(RESOURCE_DURATION1_UNITS))))
            resource.setDuration(2, MPPUtility.getDuration(rscExtData.getInt(RESOURCE_DURATION2), MPPUtility.getDurationTimeUnits(rscExtData.getShort(RESOURCE_DURATION2_UNITS))))
            resource.setDuration(3, MPPUtility.getDuration(rscExtData.getInt(RESOURCE_DURATION3), MPPUtility.getDurationTimeUnits(rscExtData.getShort(RESOURCE_DURATION3_UNITS))))
            resource.setDuration(4, MPPUtility.getDuration(rscExtData.getInt(RESOURCE_DURATION4), MPPUtility.getDurationTimeUnits(rscExtData.getShort(RESOURCE_DURATION4_UNITS))))
            resource.setDuration(5, MPPUtility.getDuration(rscExtData.getInt(RESOURCE_DURATION5), MPPUtility.getDurationTimeUnits(rscExtData.getShort(RESOURCE_DURATION5_UNITS))))
            resource.setDuration(6, MPPUtility.getDuration(rscExtData.getInt(RESOURCE_DURATION6), MPPUtility.getDurationTimeUnits(rscExtData.getShort(RESOURCE_DURATION6_UNITS))))
            resource.setDuration(7, MPPUtility.getDuration(rscExtData.getInt(RESOURCE_DURATION7), MPPUtility.getDurationTimeUnits(rscExtData.getShort(RESOURCE_DURATION7_UNITS))))
            resource.setDuration(8, MPPUtility.getDuration(rscExtData.getInt(RESOURCE_DURATION8), MPPUtility.getDurationTimeUnits(rscExtData.getShort(RESOURCE_DURATION8_UNITS))))
            resource.setDuration(9, MPPUtility.getDuration(rscExtData.getInt(RESOURCE_DURATION9), MPPUtility.getDurationTimeUnits(rscExtData.getShort(RESOURCE_DURATION9_UNITS))))
            resource.setDuration(10, MPPUtility.getDuration(rscExtData.getInt(RESOURCE_DURATION10), MPPUtility.getDurationTimeUnits(rscExtData.getShort(RESOURCE_DURATION10_UNITS))))
            resource.emailAddress = rscExtData.getUnicodeString(RESOURCE_EMAIL)
            resource.setFinish(1, rscExtData.getTimestamp(RESOURCE_FINISH1))
            resource.setFinish(2, rscExtData.getTimestamp(RESOURCE_FINISH2))
            resource.setFinish(3, rscExtData.getTimestamp(RESOURCE_FINISH3))
            resource.setFinish(4, rscExtData.getTimestamp(RESOURCE_FINISH4))
            resource.setFinish(5, rscExtData.getTimestamp(RESOURCE_FINISH5))
            resource.setFinish(6, rscExtData.getTimestamp(RESOURCE_FINISH6))
            resource.setFinish(7, rscExtData.getTimestamp(RESOURCE_FINISH7))
            resource.setFinish(8, rscExtData.getTimestamp(RESOURCE_FINISH8))
            resource.setFinish(9, rscExtData.getTimestamp(RESOURCE_FINISH9))
            resource.setFinish(10, rscExtData.getTimestamp(RESOURCE_FINISH10))
            resource.group = rscExtData.getUnicodeString(RESOURCE_GROUP)
            resource.id = Integer.valueOf(MPPUtility.getInt(data, 4))
            resource.initials = rscVarData.getUnicodeString(getOffset(data, 160))
            //resource.setLinkedFields(); // Calculated value
            resource.maxUnits = NumberHelper.getDouble(MPPUtility.getInt(data, 52).toDouble() / 100)
            resource.name = rscVarData.getUnicodeString(getOffset(data, 156))
            resource.setNumber(1, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER1)))
            resource.setNumber(2, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER2)))
            resource.setNumber(3, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER3)))
            resource.setNumber(4, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER4)))
            resource.setNumber(5, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER5)))
            resource.setNumber(6, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER6)))
            resource.setNumber(7, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER7)))
            resource.setNumber(8, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER8)))
            resource.setNumber(9, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER9)))
            resource.setNumber(10, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER10)))
            resource.setNumber(11, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER11)))
            resource.setNumber(12, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER12)))
            resource.setNumber(13, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER13)))
            resource.setNumber(14, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER14)))
            resource.setNumber(15, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER15)))
            resource.setNumber(16, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER16)))
            resource.setNumber(17, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER17)))
            resource.setNumber(18, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER18)))
            resource.setNumber(19, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER19)))
            resource.setNumber(20, NumberHelper.getDouble(rscExtData.getDouble(RESOURCE_NUMBER20)))
            //resource.setObjects(); // Calculated value
            //resource.setOverallocated(); // Calculated value
            resource.overtimeCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 138).toDouble() / 100)
            resource.overtimeRate = Rate(MPPUtility.getDouble(data, 44), TimeUnit.HOURS)
            resource.overtimeWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 74).toDouble() / 100, TimeUnit.HOURS)
            resource.peakUnits = NumberHelper.getDouble(MPPUtility.getInt(data, 110).toDouble() / 100)
            //resource.setPercentageWorkComplete(); // Calculated value
            resource.regularWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 92).toDouble() / 100, TimeUnit.HOURS)
            resource.remainingCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 132).toDouble() / 100)
            resource.remainingOvertimeCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 150).toDouble() / 100)
            resource.remainingWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 86).toDouble() / 100, TimeUnit.HOURS)
            resource.standardRate = Rate(MPPUtility.getDouble(data, 36), TimeUnit.HOURS)
            resource.setStart(1, rscExtData.getTimestamp(RESOURCE_START1))
            resource.setStart(2, rscExtData.getTimestamp(RESOURCE_START2))
            resource.setStart(3, rscExtData.getTimestamp(RESOURCE_START3))
            resource.setStart(4, rscExtData.getTimestamp(RESOURCE_START4))
            resource.setStart(5, rscExtData.getTimestamp(RESOURCE_START5))
            resource.setStart(6, rscExtData.getTimestamp(RESOURCE_START6))
            resource.setStart(7, rscExtData.getTimestamp(RESOURCE_START7))
            resource.setStart(8, rscExtData.getTimestamp(RESOURCE_START8))
            resource.setStart(9, rscExtData.getTimestamp(RESOURCE_START9))
            resource.setStart(10, rscExtData.getTimestamp(RESOURCE_START10))
            resource.setText(1, rscExtData.getUnicodeString(RESOURCE_TEXT1))
            resource.setText(2, rscExtData.getUnicodeString(RESOURCE_TEXT2))
            resource.setText(3, rscExtData.getUnicodeString(RESOURCE_TEXT3))
            resource.setText(4, rscExtData.getUnicodeString(RESOURCE_TEXT4))
            resource.setText(5, rscExtData.getUnicodeString(RESOURCE_TEXT5))
            resource.setText(6, rscExtData.getUnicodeString(RESOURCE_TEXT6))
            resource.setText(7, rscExtData.getUnicodeString(RESOURCE_TEXT7))
            resource.setText(8, rscExtData.getUnicodeString(RESOURCE_TEXT8))
            resource.setText(9, rscExtData.getUnicodeString(RESOURCE_TEXT9))
            resource.setText(10, rscExtData.getUnicodeString(RESOURCE_TEXT10))
            resource.setText(11, rscExtData.getUnicodeString(RESOURCE_TEXT11))
            resource.setText(12, rscExtData.getUnicodeString(RESOURCE_TEXT12))
            resource.setText(13, rscExtData.getUnicodeString(RESOURCE_TEXT13))
            resource.setText(14, rscExtData.getUnicodeString(RESOURCE_TEXT14))
            resource.setText(15, rscExtData.getUnicodeString(RESOURCE_TEXT15))
            resource.setText(16, rscExtData.getUnicodeString(RESOURCE_TEXT16))
            resource.setText(17, rscExtData.getUnicodeString(RESOURCE_TEXT17))
            resource.setText(18, rscExtData.getUnicodeString(RESOURCE_TEXT18))
            resource.setText(19, rscExtData.getUnicodeString(RESOURCE_TEXT19))
            resource.setText(20, rscExtData.getUnicodeString(RESOURCE_TEXT20))
            resource.setText(21, rscExtData.getUnicodeString(RESOURCE_TEXT21))
            resource.setText(22, rscExtData.getUnicodeString(RESOURCE_TEXT22))
            resource.setText(23, rscExtData.getUnicodeString(RESOURCE_TEXT23))
            resource.setText(24, rscExtData.getUnicodeString(RESOURCE_TEXT24))
            resource.setText(25, rscExtData.getUnicodeString(RESOURCE_TEXT25))
            resource.setText(26, rscExtData.getUnicodeString(RESOURCE_TEXT26))
            resource.setText(27, rscExtData.getUnicodeString(RESOURCE_TEXT27))
            resource.setText(28, rscExtData.getUnicodeString(RESOURCE_TEXT28))
            resource.setText(29, rscExtData.getUnicodeString(RESOURCE_TEXT29))
            resource.setText(30, rscExtData.getUnicodeString(RESOURCE_TEXT30))
            resource.uniqueID = Integer.valueOf(id)
            resource.work = MPPUtility.getDuration(MPPUtility.getLong6(data, 56).toDouble() / 100, TimeUnit.HOURS)

            //
            // Attach the resource calendar
            //
            calendar = m_calendarMap!!.get(Integer.valueOf(MPPUtility.getInt(data, 24)))
            resource.resourceCalendar = calendar

            //
            // Retrieve the resource notes.
            //
            notes = rscExtData.getString(RESOURCE_NOTES)
            if (notes != null) {
                if (m_reader!!.preserveNoteFormatting == false) {
                    notes = RtfHelper.strip(notes)
                }

                resource.notes = notes
            }

            m_eventManager!!.fireResourceReadEvent(resource)
        }
    }

    /**
     * This method extracts and collates resource assignment data.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processAssignmentData() {
        val assnDir = m_projectDir!!.getEntry("TBkndAssn") as DirectoryEntry
        var assnFixedData = FixFix(204, DocumentInputStream(assnDir.getEntry("FixFix   0") as DocumentEntry))
        if (assnFixedData.diff != 0 || assnFixedData.size % 238 == 0 && testAssignmentTasks(assnFixedData) == false) {
            assnFixedData = FixFix(238, DocumentInputStream(assnDir.getEntry("FixFix   0") as DocumentEntry))
        }

        val count = assnFixedData.itemCount
        var assnVarData: FixDeferFix? = null

        for (loop in 0 until count) {
            if (assnVarData == null) {
                assnVarData = FixDeferFix(DocumentInputStream(assnDir.getEntry("FixDeferFix   0") as DocumentEntry))
            }

            val data = assnFixedData.getByteArrayValue(loop)

            //
            // Check that the deleted flag isn't set
            //
            if (MPPUtility.getByte(data!!, 168) != 0x02) {
                val task = m_file!!.getTaskByUniqueID(Integer.valueOf(MPPUtility.getInt(data, 16)))
                val resource = m_file!!.getResourceByUniqueID(Integer.valueOf(MPPUtility.getInt(data, 20)))

                if (task != null && resource != null) {
                    val assignment = task.addResourceAssignment(resource)
                    assignment.actualCost = NumberHelper.getDouble(MPPUtility.getLong6(data, 138).toDouble() / 100)
                    assignment.actualWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 96).toDouble() / 100, TimeUnit.HOURS)
                    assignment.cost = NumberHelper.getDouble(MPPUtility.getLong6(data, 132).toDouble() / 100)
                    //assignment.setDelay(); // Not sure what this field maps on to in MSP
                    assignment.finish = MPPUtility.getTimestamp(data, 28)
                    assignment.overtimeWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 90).toDouble() / 100, TimeUnit.HOURS)
                    //assignment.setPlannedCost(); // Not sure what this field maps on to in MSP
                    //assignment.setPlannedWork(); // Not sure what this field maps on to in MSP
                    assignment.remainingWork = MPPUtility.getDuration(MPPUtility.getLong6(data, 114).toDouble() / 100, TimeUnit.HOURS)
                    assignment.start = MPPUtility.getTimestamp(data, 24)
                    assignment.uniqueID = Integer.valueOf(MPPUtility.getInt(data, 0))
                    assignment.units = Double.valueOf(MPPUtility.getShort(data, 80).toDouble() / 100)
                    assignment.work = MPPUtility.getDuration(MPPUtility.getLong6(data, 84).toDouble() / 100, TimeUnit.HOURS)

                    m_eventManager!!.fireAssignmentReadEvent(assignment)
                }
            }
        }
    }

    /**
     * It appears that its is possible for task assignment data blocks to be
     * one of two sizes, 204 or 238 bytes. In most cases, simply dividing the
     * overall block size by these values will determine which of these is
     * the one to use, i.e. the one that returns a remainder of zero.
     *
     * Unfortunately it is possible that an overall block size will appear which
     * can be divided exactly by both of these values. In this case we call this
     * method to perform a "rule of thumb" test to determine if the selected
     * block size is correct. From observation it appears that assignment data
     * will always have a valid resource or task associated with it. If both
     * values are invalid, then we assume that we are not using the correct
     * block size.
     *
     * As stated above, this is a "rule of thumb" test, and it is quite likely
     * that we will encounter cases which incorrectly fail this test. We'll
     * just have to keep looking for a better way to determine the correct
     * block size!
     *
     * @param assnFixedData Task assignment fixed data
     * @return boolean flag
     */
    private fun testAssignmentTasks(assnFixedData: FixFix): Boolean {
        var result = true
        val count = assnFixedData.itemCount
        var data: ByteArray?
        var task: Task?
        var resource: Resource?

        for (loop in 0 until count) {
            data = assnFixedData.getByteArrayValue(loop)
            task = m_file!!.getTaskByUniqueID(Integer.valueOf(MPPUtility.getInt(data, 16)))
            resource = m_file!!.getResourceByUniqueID(Integer.valueOf(MPPUtility.getInt(data, 20)))

            if (task == null && resource == null) {
                result = false
                break
            }
        }

        return result
    }

    /**
     * This method extracts view data from the MPP file.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processViewData() {
        val dir = m_viewDir!!.getEntry("CV_iew") as DirectoryEntry
        val ff = FixFix(138, DocumentInputStream(dir.getEntry("FixFix   0") as DocumentEntry))
        val items = ff.itemCount
        var data: ByteArray?
        var view: View

        for (loop in 0 until items) {
            data = ff.getByteArrayValue(loop)
            view = View8(m_file, data)
            m_file!!.views.add(view)
        }
    }

    /**
     * This method extracts table data from the MPP file.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processTableData() {
        val dir = m_viewDir!!.getEntry("CTable") as DirectoryEntry
        val ff = FixFix(126, DocumentInputStream(dir.getEntry("FixFix   0") as DocumentEntry))
        val fdf = FixDeferFix(DocumentInputStream(dir.getEntry("FixDeferFix   0") as DocumentEntry))
        val items = ff.itemCount
        val sb = StringBuilder()
        val container = m_file!!.tables

        for (loop in 0 until items) {
            val data = ff.getByteArrayValue(loop)
            val table = Table()

            table.id = MPPUtility.getInt(data, 0)

            var name = MPPUtility.getUnicodeString(data, 4)
            if (name.indexOf('&') !== -1) {
                sb.setLength(0)
                var index = 0
                var c: Char

                while (index < name.length()) {
                    c = name.charAt(index)
                    if (c != '&') {
                        sb.append(c)
                    }
                    ++index
                }

                name = sb.toString()
            }

            table.name = MPPUtility.removeAmpersands(name)
            container.add(table)

            val extendedData = fdf.getByteArray(getOffset(data, 122))
            if (extendedData != null) {
                val columnData = fdf.getByteArray(getOffset(extendedData, 8))
                processColumnData(table, columnData)
            }

            //System.out.println(table);
        }
    }

    /**
     * This method processes the column data associated with the
     * current table.
     *
     * @param table current table
     * @param data raw column data
     */
    private fun processColumnData(table: Table, data: ByteArray?) {
        val columnCount = MPPUtility.getShort(data, 4) + 1
        var index = 8
        var columnTitleOffset: Int
        var column: Column
        var alignment: Int

        for (loop in 0 until columnCount) {
            column = Column(m_file)

            if (loop == 0) {
                if (MPPUtility.getShort(data, index) == 0) {
                    table.resourceFlag = true
                } else {
                    table.resourceFlag = false
                }
            }

            if (table.resourceFlag == false) {
                column.setFieldType(MPPTaskField.getInstance(MPPUtility.getShort(data, index)))
            } else {
                column.setFieldType(MPPResourceField.getInstance(MPPUtility.getShort(data, index)))
            }

            column.setWidth(MPPUtility.getByte(data!!, index + 4))

            columnTitleOffset = MPPUtility.getShort(data, index + 6)
            if (columnTitleOffset != 0) {
                column.setTitle(MPPUtility.getUnicodeString(data, columnTitleOffset))
            }

            alignment = MPPUtility.getByte(data, index + 8)
            if (alignment == 32) {
                column.setAlignTitle(Column.ALIGN_LEFT)
            } else {
                if (alignment == 33) {
                    column.setAlignTitle(Column.ALIGN_CENTER)
                } else {
                    column.setAlignTitle(Column.ALIGN_RIGHT)
                }
            }

            alignment = MPPUtility.getByte(data, index + 10)
            if (alignment == 32) {
                column.setAlignData(Column.ALIGN_LEFT)
            } else {
                if (alignment == 33) {
                    column.setAlignData(Column.ALIGN_CENTER)
                } else {
                    column.setAlignData(Column.ALIGN_RIGHT)
                }
            }

            table.addColumn(column)
            index += 12
        }
    }

    /**
     * This method is used to extract a value from a fixed data block,
     * which represents an offset into a variable data block.
     *
     * @param data Fixed data block
     * @param offset Offset in fixed data block
     * @return Offset in var data block
     */
    private fun getOffset(data: ByteArray?, offset: Int): Int {
        return -1 - MPPUtility.getInt(data, offset)
    }

    companion object {

        /**
         * Task data types.
         */
        private val TASK_WBS = Integer.valueOf(104)
        private val TASK_CONTACT = Integer.valueOf(105)

        private val TASK_TEXT1 = Integer.valueOf(106)
        private val TASK_TEXT2 = Integer.valueOf(107)
        private val TASK_TEXT3 = Integer.valueOf(108)
        private val TASK_TEXT4 = Integer.valueOf(109)
        private val TASK_TEXT5 = Integer.valueOf(110)
        private val TASK_TEXT6 = Integer.valueOf(111)
        private val TASK_TEXT7 = Integer.valueOf(112)
        private val TASK_TEXT8 = Integer.valueOf(113)
        private val TASK_TEXT9 = Integer.valueOf(114)
        private val TASK_TEXT10 = Integer.valueOf(115)

        private val TASK_START1 = Integer.valueOf(116)
        private val TASK_FINISH1 = Integer.valueOf(117)
        private val TASK_START2 = Integer.valueOf(118)
        private val TASK_FINISH2 = Integer.valueOf(119)
        private val TASK_START3 = Integer.valueOf(120)
        private val TASK_FINISH3 = Integer.valueOf(121)
        private val TASK_START4 = Integer.valueOf(122)
        private val TASK_FINISH4 = Integer.valueOf(123)
        private val TASK_START5 = Integer.valueOf(124)
        private val TASK_FINISH5 = Integer.valueOf(125)
        private val TASK_START6 = Integer.valueOf(126)
        private val TASK_FINISH6 = Integer.valueOf(127)
        private val TASK_START7 = Integer.valueOf(128)
        private val TASK_FINISH7 = Integer.valueOf(129)
        private val TASK_START8 = Integer.valueOf(130)
        private val TASK_FINISH8 = Integer.valueOf(131)
        private val TASK_START9 = Integer.valueOf(132)
        private val TASK_FINISH9 = Integer.valueOf(133)
        private val TASK_START10 = Integer.valueOf(134)
        private val TASK_FINISH10 = Integer.valueOf(135)

        private val TASK_NUMBER1 = Integer.valueOf(137)
        private val TASK_NUMBER2 = Integer.valueOf(138)
        private val TASK_NUMBER3 = Integer.valueOf(139)
        private val TASK_NUMBER4 = Integer.valueOf(140)
        private val TASK_NUMBER5 = Integer.valueOf(141)
        private val TASK_NUMBER6 = Integer.valueOf(142)
        private val TASK_NUMBER7 = Integer.valueOf(143)
        private val TASK_NUMBER8 = Integer.valueOf(144)
        private val TASK_NUMBER9 = Integer.valueOf(145)
        private val TASK_NUMBER10 = Integer.valueOf(146)

        private val TASK_DURATION1 = Integer.valueOf(147)
        private val TASK_DURATION1_UNITS = Integer.valueOf(148)
        private val TASK_DURATION2 = Integer.valueOf(149)
        private val TASK_DURATION2_UNITS = Integer.valueOf(150)
        private val TASK_DURATION3 = Integer.valueOf(151)
        private val TASK_DURATION3_UNITS = Integer.valueOf(152)
        private val TASK_DURATION4 = Integer.valueOf(153)
        private val TASK_DURATION4_UNITS = Integer.valueOf(154)
        private val TASK_DURATION5 = Integer.valueOf(155)
        private val TASK_DURATION5_UNITS = Integer.valueOf(156)
        private val TASK_DURATION6 = Integer.valueOf(157)
        private val TASK_DURATION6_UNITS = Integer.valueOf(158)
        private val TASK_DURATION7 = Integer.valueOf(159)
        private val TASK_DURATION7_UNITS = Integer.valueOf(160)
        private val TASK_DURATION8 = Integer.valueOf(161)
        private val TASK_DURATION8_UNITS = Integer.valueOf(162)
        private val TASK_DURATION9 = Integer.valueOf(163)
        private val TASK_DURATION9_UNITS = Integer.valueOf(164)
        private val TASK_DURATION10 = Integer.valueOf(165)
        private val TASK_DURATION10_UNITS = Integer.valueOf(166)

        private val TASK_RECURRING_DATA = Integer.valueOf(168)

        private val TASK_DATE1 = Integer.valueOf(174)
        private val TASK_DATE2 = Integer.valueOf(175)
        private val TASK_DATE3 = Integer.valueOf(176)
        private val TASK_DATE4 = Integer.valueOf(177)
        private val TASK_DATE5 = Integer.valueOf(178)
        private val TASK_DATE6 = Integer.valueOf(179)
        private val TASK_DATE7 = Integer.valueOf(180)
        private val TASK_DATE8 = Integer.valueOf(181)
        private val TASK_DATE9 = Integer.valueOf(182)
        private val TASK_DATE10 = Integer.valueOf(183)

        private val TASK_TEXT11 = Integer.valueOf(184)
        private val TASK_TEXT12 = Integer.valueOf(185)
        private val TASK_TEXT13 = Integer.valueOf(186)
        private val TASK_TEXT14 = Integer.valueOf(187)
        private val TASK_TEXT15 = Integer.valueOf(188)
        private val TASK_TEXT16 = Integer.valueOf(189)
        private val TASK_TEXT17 = Integer.valueOf(190)
        private val TASK_TEXT18 = Integer.valueOf(191)
        private val TASK_TEXT19 = Integer.valueOf(192)
        private val TASK_TEXT20 = Integer.valueOf(193)
        private val TASK_TEXT21 = Integer.valueOf(194)
        private val TASK_TEXT22 = Integer.valueOf(195)
        private val TASK_TEXT23 = Integer.valueOf(196)
        private val TASK_TEXT24 = Integer.valueOf(197)
        private val TASK_TEXT25 = Integer.valueOf(198)
        private val TASK_TEXT26 = Integer.valueOf(199)
        private val TASK_TEXT27 = Integer.valueOf(200)
        private val TASK_TEXT28 = Integer.valueOf(201)
        private val TASK_TEXT29 = Integer.valueOf(202)
        private val TASK_TEXT30 = Integer.valueOf(203)

        private val TASK_NUMBER11 = Integer.valueOf(204)
        private val TASK_NUMBER12 = Integer.valueOf(205)
        private val TASK_NUMBER13 = Integer.valueOf(206)
        private val TASK_NUMBER14 = Integer.valueOf(207)
        private val TASK_NUMBER15 = Integer.valueOf(208)
        private val TASK_NUMBER16 = Integer.valueOf(209)
        private val TASK_NUMBER17 = Integer.valueOf(210)
        private val TASK_NUMBER18 = Integer.valueOf(211)
        private val TASK_NUMBER19 = Integer.valueOf(212)
        private val TASK_NUMBER20 = Integer.valueOf(213)

        private val TASK_HYPERLINK = Integer.valueOf(236)

        private val TASK_COST1 = Integer.valueOf(237)
        private val TASK_COST2 = Integer.valueOf(238)
        private val TASK_COST3 = Integer.valueOf(239)
        private val TASK_COST4 = Integer.valueOf(240)
        private val TASK_COST5 = Integer.valueOf(241)
        private val TASK_COST6 = Integer.valueOf(242)
        private val TASK_COST7 = Integer.valueOf(243)
        private val TASK_COST8 = Integer.valueOf(244)
        private val TASK_COST9 = Integer.valueOf(245)
        private val TASK_COST10 = Integer.valueOf(246)

        private val TASK_NOTES = Integer.valueOf(247)

        /**
         * Resource data types.
         */
        private val RESOURCE_GROUP = Integer.valueOf(61)
        private val RESOURCE_CODE = Integer.valueOf(62)
        private val RESOURCE_EMAIL = Integer.valueOf(63)

        private val RESOURCE_TEXT1 = Integer.valueOf(64)
        private val RESOURCE_TEXT2 = Integer.valueOf(65)
        private val RESOURCE_TEXT3 = Integer.valueOf(66)
        private val RESOURCE_TEXT4 = Integer.valueOf(67)
        private val RESOURCE_TEXT5 = Integer.valueOf(68)
        private val RESOURCE_TEXT6 = Integer.valueOf(69)
        private val RESOURCE_TEXT7 = Integer.valueOf(70)
        private val RESOURCE_TEXT8 = Integer.valueOf(71)
        private val RESOURCE_TEXT9 = Integer.valueOf(72)
        private val RESOURCE_TEXT10 = Integer.valueOf(73)
        private val RESOURCE_TEXT11 = Integer.valueOf(74)
        private val RESOURCE_TEXT12 = Integer.valueOf(75)
        private val RESOURCE_TEXT13 = Integer.valueOf(76)
        private val RESOURCE_TEXT14 = Integer.valueOf(77)
        private val RESOURCE_TEXT15 = Integer.valueOf(78)
        private val RESOURCE_TEXT16 = Integer.valueOf(79)
        private val RESOURCE_TEXT17 = Integer.valueOf(80)
        private val RESOURCE_TEXT18 = Integer.valueOf(81)
        private val RESOURCE_TEXT19 = Integer.valueOf(82)
        private val RESOURCE_TEXT20 = Integer.valueOf(83)
        private val RESOURCE_TEXT21 = Integer.valueOf(84)
        private val RESOURCE_TEXT22 = Integer.valueOf(85)
        private val RESOURCE_TEXT23 = Integer.valueOf(86)
        private val RESOURCE_TEXT24 = Integer.valueOf(87)
        private val RESOURCE_TEXT25 = Integer.valueOf(88)
        private val RESOURCE_TEXT26 = Integer.valueOf(89)
        private val RESOURCE_TEXT27 = Integer.valueOf(90)
        private val RESOURCE_TEXT28 = Integer.valueOf(91)
        private val RESOURCE_TEXT29 = Integer.valueOf(92)
        private val RESOURCE_TEXT30 = Integer.valueOf(93)

        private val RESOURCE_START1 = Integer.valueOf(94)
        private val RESOURCE_START2 = Integer.valueOf(95)
        private val RESOURCE_START3 = Integer.valueOf(96)
        private val RESOURCE_START4 = Integer.valueOf(97)
        private val RESOURCE_START5 = Integer.valueOf(98)
        private val RESOURCE_START6 = Integer.valueOf(99)
        private val RESOURCE_START7 = Integer.valueOf(100)
        private val RESOURCE_START8 = Integer.valueOf(101)
        private val RESOURCE_START9 = Integer.valueOf(102)
        private val RESOURCE_START10 = Integer.valueOf(103)

        private val RESOURCE_FINISH1 = Integer.valueOf(104)
        private val RESOURCE_FINISH2 = Integer.valueOf(105)
        private val RESOURCE_FINISH3 = Integer.valueOf(106)
        private val RESOURCE_FINISH4 = Integer.valueOf(107)
        private val RESOURCE_FINISH5 = Integer.valueOf(108)
        private val RESOURCE_FINISH6 = Integer.valueOf(109)
        private val RESOURCE_FINISH7 = Integer.valueOf(110)
        private val RESOURCE_FINISH8 = Integer.valueOf(111)
        private val RESOURCE_FINISH9 = Integer.valueOf(112)
        private val RESOURCE_FINISH10 = Integer.valueOf(113)

        private val RESOURCE_NUMBER1 = Integer.valueOf(114)
        private val RESOURCE_NUMBER2 = Integer.valueOf(115)
        private val RESOURCE_NUMBER3 = Integer.valueOf(116)
        private val RESOURCE_NUMBER4 = Integer.valueOf(117)
        private val RESOURCE_NUMBER5 = Integer.valueOf(118)
        private val RESOURCE_NUMBER6 = Integer.valueOf(119)
        private val RESOURCE_NUMBER7 = Integer.valueOf(120)
        private val RESOURCE_NUMBER8 = Integer.valueOf(121)
        private val RESOURCE_NUMBER9 = Integer.valueOf(122)
        private val RESOURCE_NUMBER10 = Integer.valueOf(123)
        private val RESOURCE_NUMBER11 = Integer.valueOf(124)
        private val RESOURCE_NUMBER12 = Integer.valueOf(125)
        private val RESOURCE_NUMBER13 = Integer.valueOf(126)
        private val RESOURCE_NUMBER14 = Integer.valueOf(127)
        private val RESOURCE_NUMBER15 = Integer.valueOf(128)
        private val RESOURCE_NUMBER16 = Integer.valueOf(129)
        private val RESOURCE_NUMBER17 = Integer.valueOf(130)
        private val RESOURCE_NUMBER18 = Integer.valueOf(131)
        private val RESOURCE_NUMBER19 = Integer.valueOf(132)
        private val RESOURCE_NUMBER20 = Integer.valueOf(133)

        private val RESOURCE_DURATION1 = Integer.valueOf(134)
        private val RESOURCE_DURATION2 = Integer.valueOf(135)
        private val RESOURCE_DURATION3 = Integer.valueOf(136)
        private val RESOURCE_DURATION4 = Integer.valueOf(137)
        private val RESOURCE_DURATION5 = Integer.valueOf(138)
        private val RESOURCE_DURATION6 = Integer.valueOf(139)
        private val RESOURCE_DURATION7 = Integer.valueOf(140)
        private val RESOURCE_DURATION8 = Integer.valueOf(141)
        private val RESOURCE_DURATION9 = Integer.valueOf(142)
        private val RESOURCE_DURATION10 = Integer.valueOf(143)

        private val RESOURCE_DURATION1_UNITS = Integer.valueOf(144)
        private val RESOURCE_DURATION2_UNITS = Integer.valueOf(145)
        private val RESOURCE_DURATION3_UNITS = Integer.valueOf(146)
        private val RESOURCE_DURATION4_UNITS = Integer.valueOf(147)
        private val RESOURCE_DURATION5_UNITS = Integer.valueOf(148)
        private val RESOURCE_DURATION6_UNITS = Integer.valueOf(149)
        private val RESOURCE_DURATION7_UNITS = Integer.valueOf(150)
        private val RESOURCE_DURATION8_UNITS = Integer.valueOf(151)
        private val RESOURCE_DURATION9_UNITS = Integer.valueOf(152)
        private val RESOURCE_DURATION10_UNITS = Integer.valueOf(153)

        private val RESOURCE_DATE1 = Integer.valueOf(157)
        private val RESOURCE_DATE2 = Integer.valueOf(158)
        private val RESOURCE_DATE3 = Integer.valueOf(159)
        private val RESOURCE_DATE4 = Integer.valueOf(160)
        private val RESOURCE_DATE5 = Integer.valueOf(161)
        private val RESOURCE_DATE6 = Integer.valueOf(162)
        private val RESOURCE_DATE7 = Integer.valueOf(163)
        private val RESOURCE_DATE8 = Integer.valueOf(164)
        private val RESOURCE_DATE9 = Integer.valueOf(165)
        private val RESOURCE_DATE10 = Integer.valueOf(166)

        private val RESOURCE_NOTES = Integer.valueOf(169)

        private val RESOURCE_COST1 = Integer.valueOf(170)
        private val RESOURCE_COST2 = Integer.valueOf(171)
        private val RESOURCE_COST3 = Integer.valueOf(172)
        private val RESOURCE_COST4 = Integer.valueOf(173)
        private val RESOURCE_COST5 = Integer.valueOf(174)
        private val RESOURCE_COST6 = Integer.valueOf(175)
        private val RESOURCE_COST7 = Integer.valueOf(176)
        private val RESOURCE_COST8 = Integer.valueOf(177)
        private val RESOURCE_COST9 = Integer.valueOf(178)
        private val RESOURCE_COST10 = Integer.valueOf(179)

        /**
         * Default working week.
         */
        private val DEFAULT_WORKING_WEEK = booleanArrayOf(false, true, true, true, true, true, false)
    }
}
