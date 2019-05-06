/*
 * file:       PropsBlock.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       07/12/2007
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

import java.util.TreeMap

/**
 * This class represents a block of property data.
 */
internal class PropsBlock
/**
 * Constructor.
 *
 * @param data block of property data
 */
(data: ByteArray) : Props() {
    init {
        val dataSize = MPPUtility.getInt(data, 0)
        val itemCount = MPPUtility.getInt(data, 4)

        var offset = 8
        val offsetMap = TreeMap<Integer, Integer>()
        for (loop in 0 until itemCount) {
            val itemKey = MPPUtility.getInt(data, offset)
            offset += 4

            val itemOffset = MPPUtility.getInt(data, offset)
            offset += 4

            offsetMap.put(Integer.valueOf(itemOffset), Integer.valueOf(itemKey))
        }

        var previousItemOffset: Integer? = null
        var previousItemKey: Integer? = null

        for (itemOffset in offsetMap.keySet()) {
            populateMap(data, previousItemOffset, previousItemKey, itemOffset)
            previousItemOffset = itemOffset
            previousItemKey = offsetMap.get(previousItemOffset)
        }

        if (previousItemOffset != null) {
            val itemOffset = Integer.valueOf(dataSize)
            populateMap(data, previousItemOffset, previousItemKey, itemOffset)
        }
    }

    /**
     * Method used to extract data from the block of properties and
     * insert the key value pair into a map.
     *
     * @param data block of property data
     * @param previousItemOffset previous offset
     * @param previousItemKey item key
     * @param itemOffset current item offset
     */
    private fun populateMap(data: ByteArray, previousItemOffset: Integer?, previousItemKey: Integer?, itemOffset: Integer) {
        if (previousItemOffset != null) {
            val itemSize = itemOffset.intValue() - previousItemOffset!!.intValue()
            val itemData = ByteArray(itemSize)
            System.arraycopy(data, previousItemOffset!!.intValue(), itemData, 0, itemSize)
            m_map.put(previousItemKey, itemData)
        }
    }

}
