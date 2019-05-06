/*
 * file:       RowHeader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2012
 * date:       29/04/2012
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

package net.sf.mpxj.asta

import java.util.ArrayList

/**
 * Used to parse and represent the header data present at the
 * start of each line in an Asta PP file.
 */
internal class RowHeader
/**
 * Constructor.
 *
 * @param header header text
 */
(header: String) {

    /**
     * Retrieve the ID value of this row.
     *
     * @return ID value
     */
    var id: String? = null
        private set
    /**
     * Retrieve the sequence value of this row.
     *
     * @return sequence value
     */
    var sequence: Int = 0
        private set
    /**
     * Retrieve the type of table this row belongs to.
     *
     * @return table type
     */
    var type: Integer? = null
        private set
    /**
     * Retrieve the "sub type" of this row.
     *
     * @return row sub type
     */
    var subtype: Int = 0
        private set

    init {
        parse(header)
    }

    /**
     * Parses values out of the header text.
     *
     * @param header header text
     */
    private fun parse(header: String) {
        val list = ArrayList<String>(4)
        val sb = StringBuilder()
        var index = 1
        while (index < header.length()) {
            val c = header.charAt(index++)
            if (Character.isDigit(c)) {
                sb.append(c)
            } else {
                if (sb.length() !== 0) {
                    list.add(sb.toString())
                    sb.setLength(0)
                }
            }
        }

        if (sb.length() !== 0) {
            list.add(sb.toString())
        }

        id = list.get(0)
        sequence = Integer.parseInt(list.get(1))
        type = Integer.valueOf(list.get(2))
        if (list.size() > 3) {
            subtype = Integer.parseInt(list.get(3))
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return "[RowHeader id=$id sequence=$sequence type=$type subtype=$subtype]"
    }
}
