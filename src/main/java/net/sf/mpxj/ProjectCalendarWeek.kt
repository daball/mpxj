/*
 * file:       ProjectCalendarWeek.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       08/11/2011
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

import net.sf.mpxj.common.DateHelper

/**
 * This class represents a basic working week, with no exceptions.
 */
open class ProjectCalendarWeek : Comparable<ProjectCalendarWeek> {

    /**
     * Flag indicating if this week is derived from another week.
     *
     * @return true if this week is derived from another
     */
    val isDerived: Boolean
        get() = parent != null

    /**
     * Retrieve an array representing all of the calendar hours defined
     * by this calendar.
     *
     * @return array of calendar hours
     */
    val hours: Array<ProjectCalendarHours>
        get() = m_hours

    /**
     * Retrieve an array representing the days of the week for this calendar.
     *
     * @return array of days of the week
     */
    val days: Array<DayType>
        get() = m_days

    /**
     * Calendar name.
     */
    /**
     * Calendar name.
     *
     * @return calendar name
     */
    /**
     * Calendar name.
     *
     * @param name calendar name
     */
    var name: String? = null
        get() = field

    /**
     * Date range for which this week is valid, null if this is the default week.
     */
    /**
     * Retrieves the data range for which this week is valid.
     * Returns null if this is the default week.
     *
     * @return date range, or null
     */
    /**
     * Sets the date range for this week. Set this to null to indicate
     * the default week.
     *
     * @param range date range, or null
     */
    var dateRange: DateRange? = null

    /**
     * Parent week from which this is derived, if any.
     */
    /**
     * If this week is derived from a another week, this method
     * will return the parent week.
     *
     * @return parent week
     */
    /**
     * Set the parent from which this week is derived.
     *
     * @param parent parent week
     */
    open var parent: ProjectCalendarWeek? = null
        internal set(parent) {
            field = parent

            for (loop in m_days.indices) {
                if (m_days[loop] == null) {
                    m_days[loop] = DayType.DEFAULT
                }
            }
        }

    /**
     * Working hours for each day.
     */
    private val m_hours = arrayOfNulls<ProjectCalendarHours>(7)

    /**
     * Working/non-working/default flag for each day.
     */
    private val m_days = arrayOfNulls<DayType>(7)

    /**
     * Adds a set of hours to this calendar without assigning them to
     * a particular day.
     *
     * @return calendar hours instance
     */
    fun addCalendarHours(): ProjectCalendarHours {
        return ProjectCalendarHours(this)
    }

    /**
     * This method retrieves the calendar hours for the specified day.
     * Note that this method only returns the hours specification for the
     * current calendar.If this is a derived calendar, it does not refer to
     * the base calendar.
     *
     * @param day Day instance
     * @return calendar hours
     */
    fun getCalendarHours(day: Day): ProjectCalendarHours {
        return m_hours[day.getValue() - 1]
    }

    /**
     * This method retrieves the calendar hours for the specified day.
     * Note that if this is a derived calendar, then this method
     * will refer to the base calendar where no hours are specified
     * in the derived calendar.
     *
     * @param day Day instance
     * @return calendar hours
     */
    fun getHours(day: Day): ProjectCalendarHours? {
        var result: ProjectCalendarHours? = getCalendarHours(day)
        if (result == null) {
            //
            // If this is a base calendar and we have no hours, then we
            // have a problem - so we add the default hours and try again
            //
            if (parent == null) {
                // Only add default hours for the day that is 'missing' to avoid overwriting real calendar hours
                addDefaultCalendarHours(day)
                result = getCalendarHours(day)
            } else {
                result = parent!!.getHours(day)
            }
        }
        return result
    }

    /**
     * This is a convenience method used to add a default set of calendar
     * hours to a calendar.
     */
    fun addDefaultCalendarHours() {
        for (i in 1..7) {
            addDefaultCalendarHours(Day.getInstance(i))
        }
    }

    /**
     * This is a convenience method used to add a default set of calendar
     * hours to a calendar.
     *
     * @param day Day for which to add default hours for
     */
    fun addDefaultCalendarHours(day: Day) {
        val hours = addCalendarHours(day)

        if (day !== Day.SATURDAY && day !== Day.SUNDAY) {
            hours.addRange(DEFAULT_WORKING_MORNING)
            hours.addRange(DEFAULT_WORKING_AFTERNOON)
        }
    }

    /**
     * Used to add working hours to the calendar. Note that the MPX file
     * definition allows a maximum of 7 calendar hours records to be added to
     * a single calendar.
     *
     * @param day day number
     * @return new ProjectCalendarHours instance
     */
    open fun addCalendarHours(day: Day): ProjectCalendarHours {
        val bch = ProjectCalendarHours(this)
        bch.day = day
        m_hours[day.getValue() - 1] = bch
        return bch
    }

    /**
     * Attaches a pre-existing set of hours to the correct
     * day within the calendar.
     *
     * @param hours calendar hours instance
     */
    open fun attachHoursToDay(hours: ProjectCalendarHours) {
        if (hours.parentCalendar !== this) {
            throw IllegalArgumentException()
        }
        m_hours[hours.day!!.getValue() - 1] = hours
    }

    /**
     * Removes a set of calendar hours from the day to which they
     * are currently attached.
     *
     * @param hours calendar hours instance
     */
    open fun removeHoursFromDay(hours: ProjectCalendarHours) {
        if (hours.parentCalendar !== this) {
            throw IllegalArgumentException()
        }
        m_hours[hours.day!!.getValue() - 1] = null
    }

    /**
     * This method allows the retrieval of the actual working day flag,
     * which can take the values DEFAULT, WORKING, or NONWORKING. This differs
     * from the isWorkingDay method as it retrieves the actual flag value.
     * The isWorkingDay method will always refer back to the base calendar
     * to get a boolean value if the underlying flag value is DEFAULT. If
     * isWorkingDay were the only method available to access this flag,
     * it would not be possible to determine that a resource calendar
     * had one or more flags set to DEFAULT.
     *
     * @param day required day
     * @return value of underlying working day flag
     */
    fun getWorkingDay(day: Day): DayType {
        return m_days[day.getValue() - 1]
    }

    /**
     * convenience method for setting working or non-working days.
     *
     * @param day required day
     * @param working flag indicating if the day is a working day
     */
    fun setWorkingDay(day: Day, working: Boolean) {
        setWorkingDay(day, if (working) DayType.WORKING else DayType.NON_WORKING)
    }

    /**
     * This is a convenience method provided to allow a day to be set
     * as working or non-working, by using the day number to
     * identify the required day.
     *
     * @param day required day
     * @param working flag indicating if the day is a working day
     */
    fun setWorkingDay(day: Day, working: DayType?) {
        val value: DayType

        if (working == null) {
            if (isDerived) {
                value = DayType.DEFAULT
            } else {
                value = DayType.WORKING
            }
        } else {
            value = working
        }

        m_days[day.getValue() - 1] = value
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun compareTo(o: ProjectCalendarWeek): Int {
        val fromTime1 = dateRange!!.getStart().getTime()
        val fromTime2 = o.dateRange!!.getStart().getTime()
        return if (fromTime1 < fromTime2) -1 else if (fromTime1 == fromTime2) 0 else 1
    }

    /**
     * {@inheritDoc}
     */
    @Override
    open fun toString(): String {
        val os = ByteArrayOutputStream()
        val pw = PrintWriter(os)
        pw.println("[ProjectCalendarWeek")
        pw.println("   name=" + name!!)
        pw.println("   date_range=" + dateRange!!)

        val dayName = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

        for (loop in 0..6) {
            pw.println("   [Day " + dayName[loop])
            pw.println("      type=" + days[loop])
            pw.println("      hours=" + hours[loop])
            pw.println("   ]")
        }

        pw.println("]")
        pw.flush()
        return os.toString()
    }

    companion object {

        /**
         * Constants representing the default working morning and afternoon hours.
         */
        val DEFAULT_WORKING_MORNING = DateRange(DateHelper.getTime(8, 0), DateHelper.getTime(12, 0))
        val DEFAULT_WORKING_AFTERNOON = DateRange(DateHelper.getTime(13, 0), DateHelper.getTime(17, 0))
    }
}
