/*
 * file:       RecurringData.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       20/10/2017
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

package net.sf.mpxj

import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.text.DateFormatSymbols
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.EnumSet

import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper

/**
 * This class provides a description of a recurring event.
 */
open class RecurringData {

    /**
     * Retrieves the monthly or yearly relative day of the week.
     *
     * @return day of the week
     */
    /**
     * Sets the monthly or yearly relative day of the week.
     *
     * @param day day of the week
     */
    var dayOfWeek: Day?
        get() {
            var result: Day? = null
            if (!m_days.isEmpty()) {
                result = m_days.iterator().next()
            }
            return result
        }
        set(day) {
            m_days.clear()
            m_days.add(day)
        }

    /**
     * Retrieve the set of start dates represented by this recurrence data.
     *
     * @return array of start dates
     */
    val dates: Array<Date>
        get() {
            var frequency = NumberHelper.getInt(this.frequency)
            if (frequency < 1) {
                frequency = 1
            }

            val calendar = DateHelper.popCalendar(startDate)
            val dates = ArrayList<Date>()

            when (recurrenceType) {
                RecurrenceType.DAILY -> {
                    getDailyDates(calendar, frequency, dates)
                }

                RecurrenceType.WEEKLY -> {
                    getWeeklyDates(calendar, frequency, dates)
                }

                RecurrenceType.MONTHLY -> {
                    getMonthlyDates(calendar, frequency, dates)
                }

                RecurrenceType.YEARLY -> {
                    getYearlyDates(calendar, dates)
                }
            }

            DateHelper.pushCalendar(calendar)

            return dates.toArray(arrayOfNulls<Date>(dates.size()))
        }

    //
    // Common attributes
    //
    /**
     * Gets the start date of this recurrence.
     *
     * @return recurrence start date
     */
    /**
     * Sets the start date of this recurrence.
     *
     * @param val recurrence start date
     */
    var startDate: Date? = null
    /**
     * Gets the finish date of this recurrence.
     *
     * @return recurrence finish date
     */
    /**
     * Sets the finish date of this recurrence.
     *
     * @param val recurrence finish date
     */
    var finishDate: Date? = null
    /**
     * Sets the number of occurrences.
     *
     * @return number of occurrences
     */
    /**
     * Retrieves the number of occurrences.
     *
     * @param occurrences number of occurrences
     */
    var occurrences: Integer? = null
    /**
     * Retrieves the recurrence type.
     *
     * @return RecurrenceType instance
     */
    /**
     * Sets the recurrence type.
     *
     * @param type recurrence type
     */
    var recurrenceType: RecurrenceType? = null
    /**
     * Retrieves the relative flag. This is only relevant for monthly and yearly recurrence.
     *
     * @return boolean flag
     */
    /**
     * Sets the relative flag. This is only relevant for monthly and yearly recurrence.
     *
     * @param relative boolean flag
     */
    var relative: Boolean = false
    /**
     * Returns true if daily recurrence applies to working days only.
     *
     * @return true if daily recurrence applies to working days only
     */
    /**
     * Set to true if daily recurrence applies to working days only.
     *
     * @param workingDaysOnly true if daily recurrence applies to working days only
     */
    var isWorkingDaysOnly: Boolean = false
    /**
     * Retrieves the use end date flag.
     *
     * @return use end date flag
     */
    /**
     * Sets the use end date flag.
     *
     * @param useEndDate use end date flag
     */
    var useEndDate: Boolean = false
    /**
     * Retrieves the recurrence frequency.
     *
     * @return recurrence frequency
     */
    /**
     * Set the recurrence frequency.
     *
     * @param frequency recurrence frequency
     */
    var frequency: Integer? = null
    /**
     * Retrieves the monthly or yearly absolute day number.
     *
     * @return absolute day number.
     */
    /**
     * Sets the monthly or yearly absolute day number.
     *
     * @param day absolute day number
     */
    var dayNumber: Integer? = null
    /**
     * Retrieves the yearly month number.
     *
     * @return month number
     */
    /**
     * Sets the yearly month number.
     *
     * @param month month number
     */
    var monthNumber: Integer? = null
    private val m_days = EnumSet.noneOf(Day::class.java)

    /**
     * Returns true if this day is part of a weekly recurrence.
     *
     * @param day Day instance
     * @return true if this day is included
     */
    fun getWeeklyDay(day: Day): Boolean {
        return m_days.contains(day)
    }

    /**
     * Set the state of an individual day in a weekly recurrence.
     *
     * @param day Day instance
     * @param value true if this day is included in the recurrence
     */
    fun setWeeklyDay(day: Day, value: Boolean) {
        if (value) {
            m_days.add(day)
        } else {
            m_days.remove(day)
        }
    }

    /**
     * Converts from a bitmap to individual day flags for a weekly recurrence,
     * using the array of masks.
     *
     * @param days bitmap
     * @param masks array of mask values
     */
    fun setWeeklyDaysFromBitmap(days: Integer?, masks: IntArray) {
        if (days != null) {
            val value = days!!.intValue()
            for (day in Day.values()) {
                setWeeklyDay(day, value and masks[day.getValue()] != 0)
            }
        }
    }

    /**
     * Determines if we need to calculate more dates.
     * If we do not have a finish date, this method falls back on using the
     * occurrences attribute. If we have a finish date, we'll use that instead.
     * We're assuming that the recurring data has one or other of those values.
     *
     * @param calendar current date
     * @param dates dates generated so far
     * @return true if we should calculate another date
     */
    private fun moreDates(calendar: Calendar, dates: List<Date>): Boolean {
        val result: Boolean
        if (finishDate == null) {
            var occurrences = NumberHelper.getInt(this.occurrences)
            if (occurrences < 1) {
                occurrences = 1
            }
            result = dates.size() < occurrences
        } else {
            result = calendar.getTimeInMillis() <= finishDate!!.getTime()
        }
        return result
    }

    /**
     * Calculate start dates for a daily recurrence.
     *
     * @param calendar current date
     * @param frequency frequency
     * @param dates array of start dates
     */
    private fun getDailyDates(calendar: Calendar, frequency: Int, dates: List<Date>) {
        while (moreDates(calendar, dates)) {
            dates.add(calendar.getTime())
            calendar.add(Calendar.DAY_OF_YEAR, frequency)
        }
    }

    /**
     * Calculate start dates for a weekly recurrence.
     *
     * @param calendar current date
     * @param frequency frequency
     * @param dates array of start dates
     */
    private fun getWeeklyDates(calendar: Calendar, frequency: Int, dates: List<Date>) {
        var currentDay = calendar.get(Calendar.DAY_OF_WEEK)

        while (moreDates(calendar, dates)) {
            var offset = 0
            for (dayIndex in 0..6) {
                if (getWeeklyDay(Day.getInstance(currentDay))) {
                    if (offset != 0) {
                        calendar.add(Calendar.DAY_OF_YEAR, offset)
                        offset = 0
                    }
                    if (!moreDates(calendar, dates)) {
                        break
                    }
                    dates.add(calendar.getTime())
                }

                ++offset
                ++currentDay

                if (currentDay > 7) {
                    currentDay = 1
                }
            }

            if (frequency > 1) {
                offset += 7 * (frequency - 1)
            }
            calendar.add(Calendar.DAY_OF_YEAR, offset)
        }
    }

    /**
     * Calculate start dates for a monthly recurrence.
     *
     * @param calendar current date
     * @param frequency frequency
     * @param dates array of start dates
     */
    private fun getMonthlyDates(calendar: Calendar, frequency: Int, dates: List<Date>) {
        if (relative) {
            getMonthlyRelativeDates(calendar, frequency, dates)
        } else {
            getMonthlyAbsoluteDates(calendar, frequency, dates)
        }
    }

    /**
     * Calculate start dates for a monthly relative recurrence.
     *
     * @param calendar current date
     * @param frequency frequency
     * @param dates array of start dates
     */
    private fun getMonthlyRelativeDates(calendar: Calendar, frequency: Int, dates: List<Date>) {
        val startDate = calendar.getTimeInMillis()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val dayNumber = NumberHelper.getInt(this.dayNumber)

        while (moreDates(calendar, dates)) {
            if (dayNumber > 4) {
                setCalendarToLastRelativeDay(calendar)
            } else {
                setCalendarToOrdinalRelativeDay(calendar, dayNumber)
            }

            if (calendar.getTimeInMillis() > startDate) {
                dates.add(calendar.getTime())
                if (!moreDates(calendar, dates)) {
                    break
                }
            }
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, frequency)
        }
    }

    /**
     * Calculate start dates for a monthly absolute recurrence.
     *
     * @param calendar current date
     * @param frequency frequency
     * @param dates array of start dates
     */
    private fun getMonthlyAbsoluteDates(calendar: Calendar, frequency: Int, dates: List<Date>) {
        val currentDayNumber = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val requiredDayNumber = NumberHelper.getInt(dayNumber)
        if (requiredDayNumber < currentDayNumber) {
            calendar.add(Calendar.MONTH, 1)
        }

        while (moreDates(calendar, dates)) {
            var useDayNumber = requiredDayNumber
            val maxDayNumber = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            if (useDayNumber > maxDayNumber) {
                useDayNumber = maxDayNumber
            }
            calendar.set(Calendar.DAY_OF_MONTH, useDayNumber)
            dates.add(calendar.getTime())
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, frequency)
        }
    }

    /**
     * Calculate start dates for a yearly recurrence.
     *
     * @param calendar current date
     * @param dates array of start dates
     */
    private fun getYearlyDates(calendar: Calendar, dates: List<Date>) {
        if (relative) {
            getYearlyRelativeDates(calendar, dates)
        } else {
            getYearlyAbsoluteDates(calendar, dates)
        }
    }

    /**
     * Calculate start dates for a yearly relative recurrence.
     *
     * @param calendar current date
     * @param dates array of start dates
     */
    private fun getYearlyRelativeDates(calendar: Calendar, dates: List<Date>) {
        val startDate = calendar.getTimeInMillis()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.MONTH, NumberHelper.getInt(monthNumber) - 1)

        val dayNumber = NumberHelper.getInt(this.dayNumber)
        while (moreDates(calendar, dates)) {
            if (dayNumber > 4) {
                setCalendarToLastRelativeDay(calendar)
            } else {
                setCalendarToOrdinalRelativeDay(calendar, dayNumber)
            }

            if (calendar.getTimeInMillis() > startDate) {
                dates.add(calendar.getTime())
                if (!moreDates(calendar, dates)) {
                    break
                }
            }
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.YEAR, 1)
        }
    }

    /**
     * Calculate start dates for a yearly absolute recurrence.
     *
     * @param calendar current date
     * @param dates array of start dates
     */
    private fun getYearlyAbsoluteDates(calendar: Calendar, dates: List<Date>) {
        val startDate = calendar.getTimeInMillis()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.MONTH, NumberHelper.getInt(monthNumber) - 1)
        val requiredDayNumber = NumberHelper.getInt(dayNumber)

        while (moreDates(calendar, dates)) {
            var useDayNumber = requiredDayNumber
            val maxDayNumber = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            if (useDayNumber > maxDayNumber) {
                useDayNumber = maxDayNumber
            }

            calendar.set(Calendar.DAY_OF_MONTH, useDayNumber)
            if (calendar.getTimeInMillis() < startDate) {
                calendar.add(Calendar.YEAR, 1)
            }

            dates.add(calendar.getTime())
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.YEAR, 1)
        }
    }

    /**
     * Moves a calendar to the nth named day of the month.
     *
     * @param calendar current date
     * @param dayNumber nth day
     */
    private fun setCalendarToOrdinalRelativeDay(calendar: Calendar, dayNumber: Int) {
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val requiredDayOfWeek = dayOfWeek!!.getValue()
        var dayOfWeekOffset = 0
        if (requiredDayOfWeek > currentDayOfWeek) {
            dayOfWeekOffset = requiredDayOfWeek - currentDayOfWeek
        } else {
            if (requiredDayOfWeek < currentDayOfWeek) {
                dayOfWeekOffset = 7 - (currentDayOfWeek - requiredDayOfWeek)
            }
        }

        if (dayOfWeekOffset != 0) {
            calendar.add(Calendar.DAY_OF_YEAR, dayOfWeekOffset)
        }

        if (dayNumber > 1) {
            calendar.add(Calendar.DAY_OF_YEAR, 7 * (dayNumber - 1))
        }
    }

    /**
     * Moves a calendar to the last named day of the month.
     *
     * @param calendar current date
     */
    private fun setCalendarToLastRelativeDay(calendar: Calendar) {
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val requiredDayOfWeek = dayOfWeek!!.getValue()
        var dayOfWeekOffset = 0

        if (currentDayOfWeek > requiredDayOfWeek) {
            dayOfWeekOffset = requiredDayOfWeek - currentDayOfWeek
        } else {
            if (currentDayOfWeek < requiredDayOfWeek) {
                dayOfWeekOffset = -7 + (requiredDayOfWeek - currentDayOfWeek)
            }
        }

        if (dayOfWeekOffset != 0) {
            calendar.add(Calendar.DAY_OF_YEAR, dayOfWeekOffset)
        }
    }

    /**
     * Sets the yearly absolute date.
     *
     * @param date yearly absolute date
     */
    fun setYearlyAbsoluteFromDate(date: Date?) {
        if (date != null) {
            val cal = DateHelper.popCalendar(date)
            dayNumber = Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH))
            monthNumber = Integer.valueOf(cal.get(Calendar.MONTH) + 1)
            DateHelper.pushCalendar(cal)
        }
    }

    /**
     * Retrieve the ordinal text for a given integer.
     *
     * @param value integer value
     * @return ordinal text
     */
    private fun getOrdinal(value: Integer): String {
        val result: String
        val index = value.intValue()
        if (index >= ORDINAL.size) {
            result = "every " + index + "th"
        } else {
            result = ORDINAL[index]
        }
        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    open fun toString(): String {
        val dfs = DateFormatSymbols()
        val os = ByteArrayOutputStream()
        val pw = PrintWriter(os)
        pw.print("[RecurringData")
        pw.print(recurrenceType)

        when (recurrenceType) {
            RecurrenceType.DAILY -> {
                pw.print(" " + getOrdinal(frequency!!))
                pw.print(if (isWorkingDaysOnly) " Working day" else " Day")
            }

            RecurrenceType.WEEKLY -> {
                pw.print(" " + getOrdinal(frequency!!))
                pw.print(" week on ")

                val sb = StringBuilder()
                for (day in Day.values()) {
                    if (getWeeklyDay(day)) {
                        if (sb.length() !== 0) {
                            sb.append(", ")
                        }
                        sb.append(dfs.getWeekdays()[day.getValue()])
                    }
                }
                pw.print(sb.toString())
            }

            RecurrenceType.MONTHLY -> {
                if (relative) {
                    pw.print(" on The ")
                    pw.print(DAY_ORDINAL[dayNumber!!.intValue()])
                    pw.print(" ")
                    pw.print(dfs.getWeekdays()[dayOfWeek!!.getValue()])
                    pw.print(" of ")
                    pw.print(getOrdinal(frequency!!))
                } else {
                    pw.print(" on Day ")
                    pw.print(dayNumber)
                    pw.print(" of ")
                    pw.print(getOrdinal(frequency!!))
                }
                pw.print(" month")
            }

            RecurrenceType.YEARLY -> {
                pw.print(" on the ")
                if (relative) {
                    pw.print(DAY_ORDINAL[dayNumber!!.intValue()])
                    pw.print(" ")
                    pw.print(dfs.getWeekdays()[dayOfWeek!!.getValue()])
                    pw.print(" of ")
                    pw.print(dfs.getMonths()[monthNumber!!.intValue() - 1])
                } else {
                    pw.print(dayNumber + " " + dfs.getMonths()[monthNumber!!.intValue() - 1])
                }
            }
        }

        pw.print(" From " + startDate!!)
        pw.print(" For $occurrences occurrences")
        pw.print(" To " + finishDate!!)

        pw.println("]")
        pw.flush()
        return os.toString()
    }

    companion object {

        /**
         * List of ordinal names used to generate debugging output.
         */
        private val ORDINAL = arrayOf<String>(null, "every", "every other", "every 3rd")

        /**
         * List of ordinal names used to generate debugging output.
         */
        private val DAY_ORDINAL = arrayOf<String>(null, "First", "Second", "Third", "Fourth", "Last")
    }
}
