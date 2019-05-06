/*
 * file:       DateColumn.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       01/03/2018
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

package net.sf.mpxj.primavera.p3

import java.util.Calendar
import java.util.Date

import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.primavera.common.AbstractColumn

/**
 * Extract column data from a table.
 */
internal class DateColumn
/**
 * Constructor.
 *
 * @param name column name
 * @param offset offset within data
 */
(name: String, offset: Int) : AbstractColumn(name, offset) {

    @Override
    override fun read(offset: Int, data: ByteArray): Date? {
        var result: Date? = null

        var intValue = 0
        var i = offset + m_offset
        var shiftBy = 0
        while (shiftBy < 32) {
            intValue = intValue or (data[i] and 0xff shl shiftBy)
            ++i
            shiftBy += 8
        }

        if (intValue != 0) {
            val stringValue = Integer.toString(intValue)
            if (stringValue.length() === 10) {
                val year = Integer.parseInt(stringValue.substring(0, 4))
                val month = Integer.parseInt(stringValue.substring(4, 6))
                val day = Integer.parseInt(stringValue.substring(6, 8))

                val cal = DateHelper.popCalendar()
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month - 1)
                cal.set(Calendar.DAY_OF_MONTH, day)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                if (cal.getTimeInMillis() > DatabaseReader.EPOCH) {
                    result = cal.getTime()
                }
                DateHelper.pushCalendar(cal)
            }
        }

        return result
    }
}
