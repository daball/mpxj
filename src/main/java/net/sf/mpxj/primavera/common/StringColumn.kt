/*
 * file:       StringColumn.java
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
class StringColumn
/**
 * Constructor.
 *
 * @param name column name
 * @param offset offset in data
 * @param length maximum string length
 */
(name: String, offset: Int, private val m_length: Int) : AbstractColumn(name, offset) {

    @Override
    override fun read(offset: Int, data: ByteArray): String {
        val buffer = StringBuilder()
        var c: Char

        for (loop in 0 until m_length) {
            c = data[offset + m_offset + loop].toChar()

            if (c.toInt() == 0) {
                break
            }

            buffer.append(c)
        }

        return buffer.toString().trim()
    }
}
