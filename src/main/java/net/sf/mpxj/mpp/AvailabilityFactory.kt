/*
 * file:       AvailabilityFactory.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2009
 * date:       09/06/2009
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
import java.util.Collections
import java.util.Date

import net.sf.mpxj.Availability
import net.sf.mpxj.AvailabilityTable
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper

/**
 * Common code to read resource availability tables from MPP files.
 */
internal class AvailabilityFactory {
    /**
     * Populates a resource availability table.
     *
     * @param table resource availability table
     * @param data file data
     */
    fun process(table: AvailabilityTable, data: ByteArray?) {
        if (data != null) {
            val cal = DateHelper.popCalendar()
            val items = MPPUtility.getShort(data, 0)
            var offset = 12

            for (loop in 0 until items) {
                val unitsValue = MPPUtility.getDouble(data, offset + 4)
                if (unitsValue != 0.0) {
                    val startDate = MPPUtility.getTimestampFromTenths(data, offset)
                    var endDate = MPPUtility.getTimestampFromTenths(data, offset + 20)
                    cal.setTime(endDate)
                    cal.add(Calendar.MINUTE, -1)
                    endDate = cal.getTime()
                    val units = NumberHelper.getDouble(unitsValue / 100)
                    val item = Availability(startDate, endDate, units)
                    table.add(item)
                }
                offset += 20
            }
            DateHelper.pushCalendar(cal)
            Collections.sort(table)
        }
    }

}
