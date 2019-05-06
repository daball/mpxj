/*
 * file:       DateRange.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       25/03/2005
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
 * This class represents a period of time.
 */
class DateRange
/**
 * Constructor.
 *
 * @param startDate start date
 * @param endDate end date
 */
(private val m_start: Date, private val m_end: Date) : Comparable<DateRange> {

    /**
     * Retrieve the date at the start of the range.
     *
     * @return start date
     */
    val start: Date
        get() = m_start

    /**
     * Retrieve the date at the end of the range.
     *
     * @return end date
     */
    val end: Date
        get() = m_end

    /**
     * This method compares a target date with a date range. The method will
     * return 0 if the date is within the range, less than zero if the date
     * is before the range starts, and greater than zero if the date is after
     * the range ends.
     *
     * @param date target date
     * @return comparison result
     */
    operator fun compareTo(date: Date): Int {
        return DateHelper.compare(m_start, m_end, date)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun compareTo(o: DateRange): Int {
        var result = net.sf.mpxj.common.DateHelper.compare(m_start, o.m_start)
        if (result == 0) {
            result = net.sf.mpxj.common.DateHelper.compare(m_end, o.m_end)
        }
        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun equals(o: Object): Boolean {
        var result = false
        if (o is DateRange) {
            val rhs = o as DateRange
            result = compareTo(rhs) == 0
        }
        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun hashCode(): Int {
        val start = m_start.getTime()
        val end = m_end.getTime()
        return start.toInt() xor (start shr 32).toInt() xor (end.toInt() xor (end shr 32).toInt())
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return "[DateRange start=$m_start end=$m_end]"
    }

    companion object {

        val EMPTY_RANGE = DateRange(null, null)
    }
}
