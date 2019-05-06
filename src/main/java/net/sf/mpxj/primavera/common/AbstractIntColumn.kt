/*
 * file:       AbstractIntColumn.java
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

package net.sf.mpxj.primavera.common

/**
 * Extract column data from a table.
 */
abstract class AbstractIntColumn
/**
 * Constructor.
 *
 * @param name column name
 * @param offset offset within data
 */
(name: String, offset: Int) : AbstractColumn(name, offset) {

    /**
     * Read a four byte integer from the data.
     *
     * @param offset current offset into data block
     * @param data data block
     * @return int value
     */
    fun readInt(offset: Int, data: ByteArray): Int {
        var result = 0
        var i = offset + m_offset
        var shiftBy = 0
        while (shiftBy < 32) {
            result = result or (data[i] and 0xff shl shiftBy)
            ++i
            shiftBy += 8
        }
        return result
    }
}
