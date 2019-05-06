/*
 * file:       TimescaleTier.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Apr 7, 2005
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

/**
 * This class collects together the properties which represent a
 * single tier of the timescale on a Gantt chart.
 */
class TimescaleTier {
    /**
     * Retrieves the tier count.
     *
     * @return tier count
     */
    /**
     * Sets the tier count.
     *
     * @param count tier count
     */
    var count: Int
        get() = m_count
        set(count) {
            m_count = count
        }

    /**
     * Retrieves the tier label format.
     *
     * @return tier label format
     */
    /**
     * Sets the tier label format.
     *
     * @param format tier label format
     */
    var format: TimescaleFormat?
        get() = m_format
        set(format) {
            m_format = format
        }

    /**
     * Retrieves the tick lines flag.
     *
     * @return tick lines flag
     */
    /**
     * Sets the tick lines flag.
     *
     * @param tickLines tick lines flag
     */
    var tickLines: Boolean
        get() = m_tickLines
        set(tickLines) {
            m_tickLines = tickLines
        }

    /**
     * Retrieves the timescale units.
     *
     * @return timescale units
     */
    /**
     * Sets the timescale units.
     *
     * @param units timescale units
     */
    var units: TimescaleUnits?
        get() = m_units
        set(units) {
            m_units = units
        }

    /**
     * Retrieves the uses fiscal year flag.
     *
     * @return uses fiscal year flag
     */
    /**
     * Sets the uses fiscal year flag.
     *
     * @param usesFiscalYear uses fiscal year flag
     */
    var usesFiscalYear: Boolean
        get() = m_usesFiscalYear
        set(usesFiscalYear) {
            m_usesFiscalYear = usesFiscalYear
        }

    /**
     * Retrieve the timescale lable alignment.
     *
     * @return label alignment
     */
    /**
     * Set the timescale label alignment.
     *
     * @param alignment label alignment
     */
    var alignment: TimescaleAlignment?
        get() = m_alignment
        set(alignment) {
            m_alignment = alignment
        }

    private var m_usesFiscalYear: Boolean = false
    private var m_tickLines: Boolean = false
    private var m_units: TimescaleUnits? = null
    private var m_count: Int = 0
    private var m_format: TimescaleFormat? = null
    private var m_alignment: TimescaleAlignment? = null

    /**
     * Generate a string representation of this instance.
     *
     * @return string representation of this instance
     */
    @Override
    fun toString(): String {
        return "[TimescaleTier UsesFiscalYear=$m_usesFiscalYear TickLines=$m_tickLines Units=$m_units Count=$m_count Format=[$m_format] Alignment=$m_alignment]"
    }
}
