/*
 * file:       ProjectCalendarException.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       28/11/2003
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

import java.util.Date

import net.sf.mpxj.common.DateHelper

/**
 * This class represents instances of Calendar Exception records from
 * an MPX file. It is used to define exceptions to the working days described
 * in both base and resource calendars.
 */
class ProjectCalendarException
/**
 * Package private constructor.
 *
 * @param fromDate exception start date
 * @param toDate exception end date
 */
internal constructor(fromDate: Date, toDate: Date) : ProjectCalendarDateRanges(), Comparable<ProjectCalendarException> {

    /**
     * Returns the from date.
     *
     * @return Date
     */
    val fromDate: Date?
        get() = m_fromDate

    /**
     * Get to date.
     *
     * @return Date
     */
    val toDate: Date?
        get() = m_toDate

    /**
     * Gets working status.
     *
     * @return boolean value
     */
    val working: Boolean
        get() = rangeCount != 0

    private val m_fromDate: Date?
    private val m_toDate: Date?
    /**
     * Retrieve the name of this exception.
     *
     * @return exception name
     */
    /**
     * Set the name of this exception.
     *
     * @param name exception name
     */
    var name: String? = null
    /**
     * Retrieve any recurrence data associated with this exception.
     * This will return null if this is a default single day exception.
     *
     * @return recurrence data
     */
    /**
     * Set the recurrence data associated with this exception.
     * Set this to null if this is a default single day exception.
     *
     * @param recurring recurrence data
     */
    var recurring: RecurringData? = null

    init {
        m_fromDate = DateHelper.getDayStartDate(fromDate)
        m_toDate = DateHelper.getDayEndDate(toDate)
    }

    /**
     * This method determines whether the given date falls in the range of
     * dates covered by this exception. Note that this method assumes that both
     * the start and end date of this exception have been set.
     *
     * @param date Date to be tested
     * @return Boolean value
     */
    operator fun contains(date: Date?): Boolean {
        var result = false

        if (date != null) {
            result = DateHelper.compare(fromDate, toDate, date!!) == 0
        }

        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun compareTo(o: ProjectCalendarException): Int {
        val fromTime1 = m_fromDate!!.getTime()
        val fromTime2 = o.m_fromDate!!.getTime()
        return if (fromTime1 < fromTime2) -1 else if (fromTime1 == fromTime2) 0 else 1
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        val sb = StringBuilder()
        sb.append("[ProjectCalendarException")
        if (name != null && !name!!.isEmpty()) {
            sb.append(" name=" + name!!)
        }
        sb.append(" working=$working")
        sb.append(" fromDate=" + m_fromDate!!)
        sb.append(" toDate=" + m_toDate!!)

        if (recurring != null) {
            sb.append(" recurring=" + recurring!!)
        }

        for (range in this) {
            sb.append(range.toString())
        }

        sb.append("]")
        return sb.toString()
    }
}
