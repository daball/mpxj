/*
 * file:       P3WbsFormat.java
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

import net.sf.mpxj.primavera.common.AbstractWbsFormat
import net.sf.mpxj.primavera.common.MapRow

/**
 * Reads the WBS format definition from a P3 database, and allows
 * that format to be applied to WBS values.
 */
internal class P3WbsFormat
/**
 * Constructor. Reads the format definition.
 *
 * @param row database row containing WBS format
 */
(row: MapRow) : AbstractWbsFormat() {
    init {
        var index = 1
        while (true) {
            val suffix = String.format("%02d", Integer.valueOf(index++))
            val length = row.getInteger("WBSW_$suffix")
            if (length == null || length.intValue() === 0) {
                break
            }
            m_lengths.add(length)
            m_separators.add(row.getString("WBSS_$suffix"))
        }
    }
}
