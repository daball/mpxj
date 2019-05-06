/*
 * file:       DateHelper.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       Jan 18, 2006
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

package net.sf.mpxj.common

import java.util.ArrayDeque
import java.util.Calendar
import java.util.Date
import java.util.Deque
import java.util.TimeZone

import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit

/**
 * Utility methods for manipulating dates.
 */
object DateHelper {

    /**
     * First date supported by Microsoft Project: January 01 00:00:00 1984.
     */
    val FIRST_DATE = DateHelper.getTimestampFromLong(441763200000L)

    /**
     * Last date supported by Microsoft Project: Friday December 31 23:59:00 2049.
     */
    val LAST_DATE = DateHelper.getTimestampFromLong(2524607946000L)

    /**
     * Number of milliseconds per minute.
     */
    val MS_PER_MINUTE = (60 * 1000).toLong()

    /**
     * Number of milliseconds per minute.
     */
    val MS_PER_HOUR = 60 * MS_PER_MINUTE

    /**
     * Number of milliseconds per day.
     */
    val MS_PER_DAY = 24 * MS_PER_HOUR

    /**
     * Default value to use for DST savings if we are using a version
     * of Java < 1.4.
     */
    private val DEFAULT_DST_SAVINGS = 3600000

    /**
     * Flag used to indicate the existence of the getDSTSavings
     * method that was introduced in Java 1.4.
     */
    private var HAS_DST_SAVINGS: Boolean = false

    private val CALENDARS = object : ThreadLocal<Deque<Calendar>>() {
        @Override
        protected fun initialValue(): Deque<Calendar> {
            return ArrayDeque<Calendar>()
        }
    }

    /**
     * Returns a new Date instance whose value
     * represents the start of the day (i.e. the time of day is 00:00:00.000)
     *
     * @param date date to convert
     * @return day start date
     */
    fun getDayStartDate(date: Date?): Date? {
        var date = date
        if (date != null) {
            val cal = popCalendar(date)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            date = cal.getTime()
            pushCalendar(cal)
        }
        return date
    }

    /**
     * Returns a new Date instance whose value
     * represents the end of the day (i.e. the time of days is 11:59:59.999)
     *
     * @param date date to convert
     * @return day start date
     */
    fun getDayEndDate(date: Date?): Date? {
        var date = date
        if (date != null) {
            val cal = popCalendar(date)
            cal.set(Calendar.MILLISECOND, 999)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.HOUR_OF_DAY, 23)
            date = cal.getTime()
            pushCalendar(cal)
        }
        return date
    }

    /**
     * This method resets the date part of a date time value to
     * a standard date (1/1/1). This is used to allow times to
     * be compared and manipulated.
     *
     * @param date date time value
     * @return date time with date set to a standard value
     */
    fun getCanonicalTime(date: Date?): Date? {
        var date = date
        if (date != null) {
            val cal = popCalendar(date)
            cal.set(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.YEAR, 1)
            cal.set(Calendar.MILLISECOND, 0)
            date = cal.getTime()
            pushCalendar(cal)
        }
        return date
    }

    /**
     * This method compares a target date with a date range. The method will
     * return 0 if the date is within the range, less than zero if the date
     * is before the range starts, and greater than zero if the date is after
     * the range ends.
     *
     * @param startDate range start date
     * @param endDate range end date
     * @param targetDate target date
     * @return comparison result
     */
    fun compare(startDate: Date, endDate: Date, targetDate: Date): Int {
        return compare(startDate, endDate, targetDate.getTime())
    }

    /**
     * This method compares a target date with a date range. The method will
     * return 0 if the date is within the range, less than zero if the date
     * is before the range starts, and greater than zero if the date is after
     * the range ends.
     *
     * @param startDate range start date
     * @param endDate range end date
     * @param targetDate target date in milliseconds
     * @return comparison result
     */
    fun compare(startDate: Date, endDate: Date, targetDate: Long): Int {
        var result = 0
        if (targetDate < startDate.getTime()) {
            result = -1
        } else {
            if (targetDate > endDate.getTime()) {
                result = 1
            }
        }
        return result
    }

    /**
     * Compare two dates, handling null values.
     * TODO: correct the comparison order to align with Date.compareTo
     *
     * @param d1 Date instance
     * @param d2 Date instance
     * @return int comparison result
     */
    fun compare(d1: Date?, d2: Date?): Int {
        val result: Int
        if (d1 == null || d2 == null) {
            result = if (d1 === d2) 0 else if (d1 == null) 1 else -1
        } else {
            val diff = d1!!.getTime() - d2!!.getTime()
            result = if (diff == 0L) 0 else if (diff > 0) 1 else -1
        }
        return result
    }

    /**
     * Returns the earlier of two dates, handling null values. A non-null Date
     * is always considered to be earlier than a null Date.
     *
     * @param d1 Date instance
     * @param d2 Date instance
     * @return Date earliest date
     */
    fun min(d1: Date?, d2: Date?): Date? {
        val result: Date?
        if (d1 == null) {
            result = d2
        } else if (d2 == null) {
            result = d1
        } else {
            result = if (d1!!.compareTo(d2) < 0) d1 else d2
        }
        return result
    }

    /**
     * Returns the later of two dates, handling null values. A non-null Date
     * is always considered to be later than a null Date.
     *
     * @param d1 Date instance
     * @param d2 Date instance
     * @return Date latest date
     */
    fun max(d1: Date?, d2: Date?): Date? {
        val result: Date?
        if (d1 == null) {
            result = d2
        } else if (d2 == null) {
            result = d1
        } else {
            result = if (d1!!.compareTo(d2) > 0) d1 else d2
        }
        return result
    }

    /**
     * This utility method calculates the difference in working
     * time between two dates, given the context of a task.
     *
     * @param task parent task
     * @param date1 first date
     * @param date2 second date
     * @param format required format for the resulting duration
     * @return difference in working time between the two dates
     */
    fun getVariance(task: Task, date1: Date?, date2: Date?, format: TimeUnit): Duration? {
        var variance: Duration? = null

        if (date1 != null && date2 != null) {
            val calendar = task.effectiveCalendar
            if (calendar != null) {
                variance = calendar.getWork(date1, date2, format)
            }
        }

        if (variance == null) {
            variance = Duration.getInstance(0, format)
        }

        return variance
    }

    /**
     * Creates a date from the equivalent long value. This conversion
     * takes account of the time zone.
     *
     * @param date date expressed as a long integer
     * @return new Date instance
     */
    fun getDateFromLong(date: Long): Date {
        val tz = TimeZone.getDefault()
        return Date(date - tz.getRawOffset())
    }

    /**
     * Creates a timestamp from the equivalent long value. This conversion
     * takes account of the time zone and any daylight savings time.
     *
     * @param timestamp timestamp expressed as a long integer
     * @return new Date instance
     */
    fun getTimestampFromLong(timestamp: Long): Date {
        val tz = TimeZone.getDefault()
        var result = Date(timestamp - tz.getRawOffset())

        if (tz.inDaylightTime(result) === true) {
            val savings: Int

            if (HAS_DST_SAVINGS == true) {
                savings = tz.getDSTSavings()
            } else {
                savings = DEFAULT_DST_SAVINGS
            }

            result = Date(result.getTime() - savings)
        }
        return result
    }

    /**
     * Create a Date instance representing a specific time.
     *
     * @param hour hour 0-23
     * @param minutes minutes 0-59
     * @return new Date instance
     */
    fun getTime(hour: Int, minutes: Int): Date {
        val cal = popCalendar()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minutes)
        cal.set(Calendar.SECOND, 0)
        val result = cal.getTime()
        pushCalendar(cal)
        return result
    }

    /**
     * Given a date represented by a Calendar instance, set the time
     * component of the date based on the hours and minutes of the
     * time supplied by the Date instance.
     *
     * @param cal Calendar instance representing the date
     * @param time Date instance representing the time of day
     */
    fun setTime(cal: Calendar, time: Date?) {
        if (time != null) {
            val startCalendar = popCalendar(time)
            cal.set(Calendar.HOUR_OF_DAY, startCalendar.get(Calendar.HOUR_OF_DAY))
            cal.set(Calendar.MINUTE, startCalendar.get(Calendar.MINUTE))
            cal.set(Calendar.SECOND, startCalendar.get(Calendar.SECOND))
            pushCalendar(startCalendar)
        }
    }

    /**
     * Given a date represented by a Date instance, set the time
     * component of the date based on the hours and minutes of the
     * time supplied by the Date instance.
     *
     * @param date Date instance representing the date
     * @param canonicalTime Date instance representing the time of day
     * @return new Date instance with the required time set
     */
    fun setTime(date: Date, canonicalTime: Date?): Date {
        val result: Date
        if (canonicalTime == null) {
            result = date
        } else {
            //
            // The original naive implementation of this method generated
            // the "start of day" date (midnight) for the required day
            // then added the milliseconds from the canonical time
            // to move the time forward to the required point. Unfortunately
            // if the date we'e trying to do this for is the entry or
            // exit from DST, the result is wrong, hence I've switched to
            // the approach below.
            //
            val cal = popCalendar(canonicalTime)
            val dayOffset = cal.get(Calendar.DAY_OF_YEAR) - 1
            val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            val second = cal.get(Calendar.SECOND)
            val millisecond = cal.get(Calendar.MILLISECOND)

            cal.setTime(date)

            if (dayOffset != 0) {
                // The canonical time can be +1 day.
                // It's to do with the way we've historically
                // managed time ranges and midnight.
                cal.add(Calendar.DAY_OF_YEAR, dayOffset)
            }

            cal.set(Calendar.MILLISECOND, millisecond)
            cal.set(Calendar.SECOND, second)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)

            result = cal.getTime()
            pushCalendar(cal)
        }
        return result
    }

    /**
     * This internal method is used to convert from an integer representing
     * minutes past midnight into a Date instance whose time component
     * represents the start time.
     *
     * @param time integer representing the start time in minutes past midnight
     * @return Date instance
     */
    fun getTimeFromMinutesPastMidnight(time: Integer?): Date? {
        var result: Date? = null

        if (time != null) {
            var minutes = time!!.intValue()
            val hours = minutes / 60
            minutes -= hours * 60

            val cal = popCalendar()
            cal.set(Calendar.MILLISECOND, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MINUTE, minutes)
            cal.set(Calendar.HOUR_OF_DAY, hours)
            result = cal.getTime()
            pushCalendar(cal)
        }

        return result
    }

    /**
     * Add a number of days to the supplied date.
     *
     * @param date start date
     * @param days number of days to add
     * @return  new date
     */
    fun addDays(date: Date, days: Int): Date {
        val cal = popCalendar(date)
        cal.add(Calendar.DAY_OF_YEAR, days)
        val result = cal.getTime()
        pushCalendar(cal)
        return result
    }

    /**
     * Acquire a calendar instance.
     *
     * @return Calendar instance
     */
    fun popCalendar(): Calendar {
        val result: Calendar
        val calendars = CALENDARS.get()
        if (calendars.isEmpty()) {
            result = Calendar.getInstance()
        } else {
            result = calendars.pop()
        }
        return result
    }

    /**
     * Acquire a Calendar instance and set the initial date.
     *
     * @param date initial date
     * @return Calendar instance
     */
    fun popCalendar(date: Date): Calendar {
        val calendar = popCalendar()
        calendar.setTime(date)
        return calendar
    }

    /**
     * Acquire a Calendar instance and set the initial date.
     *
     * @param timeInMillis initial date
     * @return Calendar instance
     */
    fun popCalendar(timeInMillis: Long): Calendar {
        val calendar = popCalendar()
        calendar.setTimeInMillis(timeInMillis)
        return calendar
    }

    /**
     * Return a Calendar instance.
     *
     * @param cal Calendar instance to return
     */
    fun pushCalendar(cal: Calendar) {
        CALENDARS.get().push(cal)
    }

    init {
        val tz = TimeZone::class.java

        try {
            tz!!.getMethod("getDSTSavings", null as Array<Class>?)
            HAS_DST_SAVINGS = true
        } catch (ex: NoSuchMethodException) {
            HAS_DST_SAVINGS = false
        }

    }
}
/**
 * Constructor.
 */// private constructor to prevent instantiation
