/*
 * file:       FastTrackColumn.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       14/03/2017
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

package net.sf.mpxj.fasttrack

/**
 * Implemented by classes which represent columns of data from an FTS file.
 */
internal interface FastTrackColumn {

    /**
     * Retrieve the column name.
     *
     * @return column name
     */
    val name: String

    /**
     * Retrieve the column type.
     *
     * @return column type
     */
    val type: FastTrackField

    /**
     * Retrieve the column data.
     *
     * @return column data
     */
    val data: Array<Object>

    /**
     * Read and parse the column data.
     *
     * @param tableType parent table type
     * @param buffer data buffer
     * @param startIndex offset into data buffer
     * @param length length of the block containing the column data
     */
    fun read(tableType: FastTrackTableType, buffer: ByteArray, startIndex: Int, length: Int)
}
