/*
 * file:       VarMeta9.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       03/01/2003
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

import java.io.IOException
import java.io.InputStream
import java.util.Arrays
import java.util.TreeMap

/**
 * This class reads in the data from a VarMeta block. This block contains
 * meta data about variable length data items stored in a Var2Data block.
 * The meta data allows the size of the Var2Data block to be determined,
 * along with the number of data items it contains, identifiers for each item,
 * and finally the offset of each item within the block.
 */
internal class VarMeta9
/**
 * Constructor. Extracts that makes up this block from the input stream.
 *
 * @param is Input stream from whic data is read
 * @throws IOException on file read error
 */
@Throws(IOException::class)
constructor(`is`: InputStream) : AbstractVarMeta() {
    init {
        if (readInt(`is`) != MAGIC) {
            throw IOException("Bad magic number")
        }

        /*m_unknown1 =*/readInt(`is`)
        m_itemCount = readInt(`is`)
        /*m_unknown2 =*/readInt(`is`)
        /*m_unknown3 =*/readInt(`is`)
        m_dataSize = readInt(`is`)

        var uniqueID: Integer
        var type: Integer
        var offset: Integer
        var map: Map<Integer, Integer>?
        var offsets = IntArray(m_itemCount)

        //
        // There appears to be some Microsoft weirdness here. The unique ID
        // in this block is stored as a 3 byte integer. We use a 4 byte array
        // with the last byte permanently set to zero to read in the data
        // and convert it using the standard utility method.
        //
        val uniqueIDArray = ByteArray(4)

        for (loop in 0 until m_itemCount) {
            `is`.read(uniqueIDArray, 0, 3)
            uniqueID = Integer.valueOf(MPPUtility.getInt(uniqueIDArray, 0))

            type = Integer.valueOf(readByte(`is`))
            offset = Integer.valueOf(readInt(`is`))

            map = m_table.get(uniqueID)
            if (map == null) {
                map = TreeMap<Integer, Integer>()
                m_table.put(uniqueID, map)
            }
            map!!.put(type, offset)
            offsets[loop] = offset.intValue()
        }

        Arrays.sort(offsets)
        offsets = offsets
    }

    companion object {

        private val MAGIC = -0x5205246
    }
}
