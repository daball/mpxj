/*
 * file:       StringsWithLengthBlock.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       14/03/2016
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

import net.sf.mpxj.common.CharsetHelper

/**
 * Reads data from a block containing text strings.
 */
internal class StringsWithLengthBlock {

    /**
     * Retrieve the data read from this block.
     *
     * @return data
     */
    var data: Array<String>? = null
        private set
    /**
     * Retrieve the offset into the block after the data has been read.
     *
     * @return offset
     */
    var offset: Int = 0
        private set

    /**
     * Read data, return the current instance.
     *
     * @param buffer buffer containing data
     * @param offset offset into buffer
     * @param inclusive true if n+1 item read
     * @return current StringsWithLengthBlock instance
     */
    fun read(buffer: ByteArray, offset: Int, inclusive: Boolean): StringsWithLengthBlock {
        var offset = offset
        var numberOfItems = FastTrackUtility.getInt(buffer, offset)
        offset += 4

        FastTrackUtility.validateSize(numberOfItems)

        if (inclusive) {
            ++numberOfItems
        }

        data = arrayOfNulls(numberOfItems)
        for (index in data!!.indices) {
            // Two bytes
            offset += 2
            val itemNameLength = FastTrackUtility.getInt(buffer, offset)
            offset += 4
            data[index] = String(buffer, offset, itemNameLength, CharsetHelper.UTF16LE)
            offset += itemNameLength
        }

        this.offset = offset

        return this
    }
}
