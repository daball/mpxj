/*
 * file:       SynchroTable.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       2018-10-11
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

package net.sf.mpxj.synchro

/**
 * Represents the definition of a Synchro table.
 */
internal class SynchroTable
/**
 * Constructor.
 *
 * @param name table name
 * @param offset offset to start of data
 */
(
        /**
         * Retrieve the table name.
         *
         * @return table name
         */
        val name: String,
        /**
         * Retrieve the table offset.
         *
         * @return table offset
         */
        val offset: Int) {
    /**
     * Retrieve the table length.
     *
     * @return table length
     */
    /**
     * Set the table length.
     *
     * @param length table length
     */
    var length = -1

    @Override
    fun toString(): String {
        return "[SynchroTable\t name=$name\toffset=$offset\tlength=$length]"
    }
}
