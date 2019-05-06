/*
 * file:       AbstractCalendarAndExceptionFactory.java
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

import java.util.Calendar
import java.util.Date

import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.DayType
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectCalendarWeek
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.RecurrenceType
import net.sf.mpxj.RecurringData
import net.sf.mpxj.common.DateHelper

/**
 * Shared code used to read calendar data from MPP files.
 */
internal abstract class AbstractCalendarAndExceptionFactory
/**
 * Constructor.
 *
 * @param file parent ProjectFile instance
 */
(file: ProjectFile) : AbstractCalendarFactory(file) {

    /**
     * This method extracts any exceptions associated with a calendar.
     *
     * @param data calendar data block
     * @param cal calendar instance
     */
    @Override
    override fun processCalendarExceptions(data: ByteArray, cal: ProjectCalendar) {
        //
        // Handle any exceptions
        //
        if (data.size > 420) {
            var offset = 420 // The first 420 is for the working hours data

            val exceptionCount = MPPUtility.getShort(data, offset)

            if (exceptionCount == 0) {
                // align with 8 byte boundary ready to read work weeks
                offset += 4
            } else {
                var exception: ProjectCalendarException
                var duration: Long
                var periodCount: Int
                var start: Date

                //
                // Move to the start of the first exception
                //
                offset += 4

                //
                // Each exception is a 92 byte block, followed by a
                // variable length text block
                //
                for (index in 0 until exceptionCount) {
                    if (offset + 92 > data.size) {
                        // Bail out if we don't have at least 92 bytes available
                        break
                    }

                    val fromDate = MPPUtility.getDate(data, offset)
                    val toDate = MPPUtility.getDate(data, offset + 2)
                    exception = cal.addCalendarException(fromDate, toDate)

                    periodCount = MPPUtility.getShort(data, offset + 14)
                    if (periodCount != 0) {
                        for (exceptionPeriodIndex in 0 until periodCount) {
                            start = MPPUtility.getTime(data, offset + 20 + exceptionPeriodIndex * 2)
                            duration = MPPUtility.getDuration(data, offset + 32 + exceptionPeriodIndex * 4)
                            exception.addRange(DateRange(start, Date(start.getTime() + duration)))
                        }
                    }

                    //
                    // Extract the name length - ensure that it is aligned to a 4 byte boundary
                    //
                    var exceptionNameLength = MPPUtility.getInt(data, offset + 88)
                    if (exceptionNameLength % 4 != 0) {
                        exceptionNameLength = (exceptionNameLength / 4 + 1) * 4
                    }

                    if (exceptionNameLength != 0) {
                        exception.name = MPPUtility.getUnicodeString(data, offset + 92)
                    }

                    //System.out.println(ByteArrayHelper.hexdump(data, offset, 92, false));

                    val rd = RecurringData()
                    val recurrenceTypeValue = MPPUtility.getShort(data, offset + 72)
                    rd.startDate = exception.fromDate
                    rd.finishDate = exception.toDate
                    rd.recurrenceType = getRecurrenceType(recurrenceTypeValue)
                    rd.relative = getRelative(recurrenceTypeValue)
                    rd.occurrences = Integer.valueOf(MPPUtility.getShort(data, offset + 4))

                    when (rd.recurrenceType) {
                        RecurrenceType.DAILY -> {
                            val frequency: Int
                            if (recurrenceTypeValue == 1) {
                                frequency = 1
                            } else {
                                frequency = MPPUtility.getShort(data, offset + 76)
                            }
                            rd.frequency = Integer.valueOf(frequency)
                        }

                        RecurrenceType.WEEKLY -> {
                            rd.setWeeklyDaysFromBitmap(Integer.valueOf(MPPUtility.getByte(data, offset + 76)), DAY_MASKS)
                            rd.frequency = Integer.valueOf(MPPUtility.getShort(data, offset + 78))
                        }

                        RecurrenceType.MONTHLY -> {
                            if (rd.relative) {
                                rd.dayOfWeek = Day.getInstance(MPPUtility.getByte(data, offset + 77) - 2)
                                rd.dayNumber = Integer.valueOf(MPPUtility.getByte(data, offset + 76) + 1)
                                rd.frequency = Integer.valueOf(MPPUtility.getShort(data, offset + 78))
                            } else {
                                rd.dayNumber = Integer.valueOf(MPPUtility.getByte(data, offset + 76))
                                rd.frequency = Integer.valueOf(MPPUtility.getByte(data, offset + 78))
                            }
                        }

                        RecurrenceType.YEARLY -> {
                            if (rd.relative) {
                                rd.dayOfWeek = Day.getInstance(MPPUtility.getByte(data, offset + 78) - 2)
                                rd.dayNumber = Integer.valueOf(MPPUtility.getByte(data, offset + 77) + 1)
                            } else {
                                rd.dayNumber = Integer.valueOf(MPPUtility.getByte(data, offset + 77))
                            }
                            rd.monthNumber = Integer.valueOf(MPPUtility.getByte(data, offset + 76) + 1)
                        }
                    }

                    //
                    // The default values for a non-recurring exception are daily, with 1 occurrence.
                    // Only add recurrence data if it is non-default.
                    //
                    if (rd.recurrenceType != RecurrenceType.DAILY || rd.occurrences!!.intValue() !== 1) {
                        exception.recurring = rd
                    }

                    offset += 92 + exceptionNameLength
                }
            }

            processWorkWeeks(data, offset, cal)
        }
    }

    /**
     * Read the work weeks.
     *
     * @param data calendar data
     * @param offset current offset into data
     * @param cal parent calendar
     */
    private fun processWorkWeeks(data: ByteArray, offset: Int, cal: ProjectCalendar) {
        var offset = offset
        //      System.out.println("Calendar=" + cal.getName());
        //      System.out.println("Work week block start offset=" + offset);
        //      System.out.println(ByteArrayHelper.hexdump(data, true, 16, ""));

        // skip 4 byte header
        offset += 4

        while (data.size >= offset + (7 * 60 + 2 + 2 + 8 + 4)) {
            //System.out.println("Week start offset=" + offset);
            val week = cal.addWorkWeek()
            for (day in Day.values()) {
                // 60 byte block per day
                processWorkWeekDay(data, offset, week, day)
                offset += 60
            }

            val startDate = DateHelper.getDayStartDate(MPPUtility.getDate(data, offset))
            offset += 2

            val finishDate = DateHelper.getDayEndDate(MPPUtility.getDate(data, offset))
            offset += 2

            // skip unknown 8 bytes
            //System.out.println(ByteArrayHelper.hexdump(data, offset, 8, false));
            offset += 8

            //
            // Extract the name length - ensure that it is aligned to a 4 byte boundary
            //
            var nameLength = MPPUtility.getInt(data, offset)
            if (nameLength % 4 != 0) {
                nameLength = (nameLength / 4 + 1) * 4
            }
            offset += 4

            if (nameLength != 0) {
                val name = MPPUtility.getUnicodeString(data, offset, nameLength)
                offset += nameLength
                week.name = name
            }

            week.dateRange = DateRange(startDate, finishDate)
            // System.out.println(week);
        }
    }

    /**
     * Process an individual work week day.
     *
     * @param data calendar data
     * @param offset current offset into data
     * @param week parent week
     * @param day current day
     */
    private fun processWorkWeekDay(data: ByteArray, offset: Int, week: ProjectCalendarWeek, day: Day) {
        //System.out.println(ByteArrayHelper.hexdump(data, offset, 60, false));

        val dayType = MPPUtility.getShort(data, offset + 0)
        if (dayType == 1) {
            week.setWorkingDay(day, DayType.DEFAULT)
        } else {
            val hours = week.addCalendarHours(day)
            val rangeCount = MPPUtility.getShort(data, offset + 2)
            if (rangeCount == 0) {
                week.setWorkingDay(day, DayType.NON_WORKING)
            } else {
                week.setWorkingDay(day, DayType.WORKING)
                val cal = DateHelper.popCalendar()
                for (index in 0 until rangeCount) {
                    val startTime = DateHelper.getCanonicalTime(MPPUtility.getTime(data, offset + 8 + index * 2))
                    val durationInSeconds = MPPUtility.getInt(data, offset + 20 + index * 4) * 6
                    cal.setTime(startTime)
                    cal.add(Calendar.SECOND, durationInSeconds)
                    val finishTime = DateHelper.getCanonicalTime(cal.getTime())
                    hours.addRange(DateRange(startTime, finishTime))
                }
                DateHelper.pushCalendar(cal)
            }
        }
    }

    /**
     * Retrieve the recurrence type.
     *
     * @param value integer value
     * @return RecurrenceType instance
     */
    private fun getRecurrenceType(value: Int): RecurrenceType? {
        val result: RecurrenceType?
        if (value < 0 || value >= RECURRENCE_TYPES.size) {
            result = null
        } else {
            result = RECURRENCE_TYPES[value]
        }

        return result
    }

    /**
     * Determine if the exception is relative based on the recurrence type integer value.
     *
     * @param value integer value
     * @return true if the recurrence is relative
     */
    private fun getRelative(value: Int): Boolean {
        val result: Boolean
        if (value < 0 || value >= RELATIVE_MAP.size) {
            result = false
        } else {
            result = RELATIVE_MAP[value]
        }

        return result
    }

    companion object {

        private val RECURRENCE_TYPES = arrayOf<RecurrenceType>(null, RecurrenceType.DAILY, RecurrenceType.YEARLY, // Absolute
                RecurrenceType.YEARLY, // Relative
                RecurrenceType.MONTHLY, // Absolute
                RecurrenceType.MONTHLY, // Relative
                RecurrenceType.WEEKLY, RecurrenceType.DAILY)

        private val RELATIVE_MAP = booleanArrayOf(false, false, false, true, false, true)

        private val DAY_MASKS = intArrayOf(0x00, 0x01, // Sunday
                0x02, // Monday
                0x04, // Tuesday
                0x08, // Wednesday
                0x10, // Thursday
                0x20, // Friday
                0x40)// Saturday
    }

}
