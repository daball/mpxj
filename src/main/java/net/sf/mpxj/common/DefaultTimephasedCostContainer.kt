/*
 * file:       DefaultTimephasedCostContainer.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       2011-12-03
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

import java.util.LinkedList

import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.TimephasedCost
import net.sf.mpxj.TimephasedCostContainer

/**
 * Class used to manage timephased data.
 */
class DefaultTimephasedCostContainer
/**
 * Constructor.
 *
 * @param calendar calendar to which the timephased data relates
 * @param normaliser normaliser used to process this data
 * @param data timephased data
 * @param raw flag indicating if this data is raw
 */
(private val m_calendar: ProjectCalendar, private val m_normaliser: TimephasedCostNormaliser, data: List<TimephasedCost>, private var m_raw: Boolean) : TimephasedCostContainer {

    /* (non-Javadoc)
    * @see net.sf.mpxj.TimephasedCostContainer#getData()
    */
    val data: List<TimephasedCost>?
        @Override get() {
            if (m_raw) {
                m_normaliser.normalise(m_calendar, m_data)
                m_raw = false
            }
            return m_data
        }

    private var m_data: LinkedList<TimephasedCost>? = null

    init {
        if (data is LinkedList<*>) {
            m_data = data as LinkedList<TimephasedCost>
        } else {
            m_data = LinkedList<TimephasedCost>(data)
        }
    }

    /**
     * Indicates if any timephased data is present.
     *
     * @return boolean flag
     */
    @Override
    fun hasData(): Boolean {
        return !m_data!!.isEmpty()
    }
}
