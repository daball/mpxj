/*
 * file:       RecurrenceUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2008
 * date:       13/06/2008
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

package net.sf.mpxj.mpx

import java.util.Calendar
import java.util.Date
import java.util.HashMap

import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.RecurrenceType
import net.sf.mpxj.RecurringData
import net.sf.mpxj.RecurringTask
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.DateHelper

/**
 * This class contains method relating to managing Recurrence instances for MPX
 * files.
 */
internal object RecurrenceUtility {

    /**
     * Array to map from the integer representation of a
     * duration's units in the recurring task record to
     * a TimeUnit instance.
     */
    private val DURATION_UNITS = arrayOf<TimeUnit>(TimeUnit.DAYS, TimeUnit.WEEKS, TimeUnit.HOURS, TimeUnit.MINUTES)

    /**
     * Map to allow conversion of a TimeUnit instance back to an integer.
     */
    private val UNITS_MAP = HashMap<TimeUnit, Integer>()

    /**
     * Map of integer values to RecurrenceType instances.
     */
    private val RECURRENCE_TYPE_MAP = HashMap<Integer, RecurrenceType>()

    /**
     * Map of  RecurrenceType instances to integer values.
     */
    private val RECURRENCE_VALUE_MAP = HashMap<RecurrenceType, Integer>()

    /**
     * Array mapping from MPX day index to Day instances.
     */
    private val DAY_ARRAY = arrayOf<Day>(null, Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY, Day.SATURDAY, Day.SUNDAY)

    /**
     * Map from Day instance to MPX day index.
     */
    private val DAY_MAP = HashMap<Day, Integer>()

    val RECURRING_TASK_DAY_MASKS = intArrayOf(0x00, 0x40, // Sunday
            0x20, // Monday
            0x10, // Tuesday
            0x08, // Wednesday
            0x04, // Thursday
            0x02, // Friday
            0x01)// Saturday

    /**
     * Convert the integer representation of a duration value and duration units
     * into an MPXJ Duration instance.
     *
     * @param properties project properties, used for duration units conversion
     * @param durationValue integer duration value
     * @param unitsValue integer units value
     * @return Duration instance
     */
    fun getDuration(properties: ProjectProperties, durationValue: Integer?, unitsValue: Integer): Duration? {
        var result: Duration?
        if (durationValue == null) {
            result = null
        } else {
            result = Duration.getInstance(durationValue!!.intValue(), TimeUnit.MINUTES)
            val units = getDurationUnits(unitsValue)
            if (result!!.getUnits() !== units) {
                result = result!!.convertUnits(units, properties)
            }
        }
        return result
    }

    /**
     * Convert an MPXJ Duration instance into an integer duration in minutes
     * ready to be written to an MPX file.
     *
     * @param properties project properties, used for duration units conversion
     * @param duration Duration instance
     * @return integer duration in minutes
     */
    fun getDurationValue(properties: ProjectProperties, duration: Duration?): Integer? {
        var duration = duration
        val result: Integer?
        if (duration == null) {
            result = null
        } else {
            if (duration!!.getUnits() !== TimeUnit.MINUTES) {
                duration = duration!!.convertUnits(TimeUnit.MINUTES, properties)
            }
            result = Integer.valueOf(duration!!.getDuration() as Int)
        }
        return result
    }

    /**
     * Converts a TimeUnit instance to an integer value suitable for
     * writing to an MPX file.
     *
     * @param recurrence RecurringTask instance
     * @return integer value
     */
    fun getDurationUnits(recurrence: RecurringTask): Integer? {
        val duration = recurrence.duration
        var result: Integer? = null

        if (duration != null) {
            result = UNITS_MAP.get(duration.getUnits())
        }

        return result
    }

    /**
     * Maps a duration unit value from a recurring task record in an MPX file
     * to a TimeUnit instance. Defaults to days if any problems are encountered.
     *
     * @param value integer duration units value
     * @return TimeUnit instance
     */
    private fun getDurationUnits(value: Integer?): TimeUnit? {
        var result: TimeUnit? = null

        if (value != null) {
            val index = value!!.intValue()
            if (index >= 0 && index < DURATION_UNITS.size) {
                result = DURATION_UNITS[index]
            }
        }

        if (result == null) {
            result = TimeUnit.DAYS
        }

        return result
    }

    /**
     * Converts the MPX file integer representation of a recurrence type
     * into a RecurrenceType instance.
     *
     * @param value MPX file integer recurrence type
     * @return RecurrenceType instance
     */
    fun getRecurrenceType(value: Integer): RecurrenceType {
        return RECURRENCE_TYPE_MAP.get(value)
    }

    /**
     * Converts a RecurrenceType instance into the integer representation
     * used in an MPX file.
     *
     * @param value RecurrenceType instance
     * @return integer representation
     */
    fun getRecurrenceValue(value: RecurrenceType): Integer {
        return RECURRENCE_VALUE_MAP.get(value)
    }

    /**
     * Converts the string representation of the days bit field into an integer.
     *
     * @param days string bit field
     * @return integer bit field
     */
    fun getDays(days: String?): Integer? {
        var result: Integer? = null
        if (days != null) {
            result = Integer.valueOf(Integer.parseInt(days, 2))
        }
        return result
    }

    /**
     * Convert weekly recurrence days into a bit field.
     *
     * @param task recurring task
     * @return bit field as a string
     */
    fun getDays(task: RecurringTask): String {
        val sb = StringBuilder()
        for (day in Day.values()) {
            sb.append(if (task.getWeeklyDay(day)) "1" else "0")
        }
        return sb.toString()
    }

    /**
     * Convert MPX day index to Day instance.
     *
     * @param day day index
     * @return Day instance
     */
    fun getDay(day: Integer?): Day? {
        var result: Day? = null
        if (day != null) {
            result = DAY_ARRAY[day!!.intValue()]
        }
        return result
    }

    /**
     * Convert Day instance to MPX day index.
     *
     * @param day Day instance
     * @return day index
     */
    fun getDay(day: Day?): Integer? {
        var result: Integer? = null
        if (day != null) {
            result = DAY_MAP.get(day)
        }
        return result
    }

    /**
     * Retrieves the yearly absolute date.
     *
     * @param data recurrence data
     * @return yearly absolute date
     */
    fun getYearlyAbsoluteAsDate(data: RecurringData): Date? {
        val result: Date?
        val yearlyAbsoluteDay = data.dayNumber
        val yearlyAbsoluteMonth = data.monthNumber
        val startDate = data.startDate

        if (yearlyAbsoluteDay == null || yearlyAbsoluteMonth == null || startDate == null) {
            result = null
        } else {
            val cal = DateHelper.popCalendar(startDate)
            cal.set(Calendar.MONTH, yearlyAbsoluteMonth.intValue() - 1)
            cal.set(Calendar.DAY_OF_MONTH, yearlyAbsoluteDay.intValue())
            result = cal.getTime()
            DateHelper.pushCalendar(cal)
        }
        return result
    }

    init {
        for (loop in DURATION_UNITS.indices) {
            UNITS_MAP.put(DURATION_UNITS[loop], Integer.valueOf(loop))
        }
    }

    init {
        RECURRENCE_TYPE_MAP.put(Integer.valueOf(1), RecurrenceType.DAILY)
        RECURRENCE_TYPE_MAP.put(Integer.valueOf(4), RecurrenceType.WEEKLY)
        RECURRENCE_TYPE_MAP.put(Integer.valueOf(8), RecurrenceType.MONTHLY)
        RECURRENCE_TYPE_MAP.put(Integer.valueOf(16), RecurrenceType.YEARLY)
    }

    init {
        RECURRENCE_VALUE_MAP.put(RecurrenceType.DAILY, Integer.valueOf(1))
        RECURRENCE_VALUE_MAP.put(RecurrenceType.WEEKLY, Integer.valueOf(4))
        RECURRENCE_VALUE_MAP.put(RecurrenceType.MONTHLY, Integer.valueOf(8))
        RECURRENCE_VALUE_MAP.put(RecurrenceType.YEARLY, Integer.valueOf(16))
    }

    init {
        DAY_MAP.put(Day.MONDAY, Integer.valueOf(1))
        DAY_MAP.put(Day.TUESDAY, Integer.valueOf(2))
        DAY_MAP.put(Day.WEDNESDAY, Integer.valueOf(3))
        DAY_MAP.put(Day.THURSDAY, Integer.valueOf(4))
        DAY_MAP.put(Day.FRIDAY, Integer.valueOf(5))
        DAY_MAP.put(Day.SATURDAY, Integer.valueOf(6))
        DAY_MAP.put(Day.SUNDAY, Integer.valueOf(7))
    }
}
/**
 * Constructor.
 */// private constructor to prevent instantiation
