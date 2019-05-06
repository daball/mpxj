/*
 * file:       StringColumn.java
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

import java.io.PrintWriter

import net.sf.mpxj.common.CharsetHelper

/**
 * Column containing text values.
 */
internal class StringColumn : AbstractColumn() {
    /**
     * {@inheritDoc}
     */
    @Override
    override fun postHeaderSkipBytes(): Int {
        return 0
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun readData(buffer: ByteArray, offset: Int): Int {
        var offset = offset
        // Unknown
        offset += 6

        // The presence of a non-zero value here determines what structure we expect next
        val structureFlags = FastTrackUtility.getInt(buffer, offset)
        offset += 4

        if (structureFlags == 0) {
            offset += 10
        } else {
            offset = FastTrackUtility.skipToNextMatchingShort(buffer, offset, 0x000F)
        }

        val numberOfItems = FastTrackUtility.getInt(buffer, offset)
        FastTrackUtility.validateSize(numberOfItems)
        data = arrayOfNulls<String>(numberOfItems)
        offset += 4

        // Offset to data
        offset += 4

        val blockOffsets = IntArray(data!!.size + 1)
        for (index in blockOffsets.indices) {
            val offsetInBlock = FastTrackUtility.getInt(buffer, offset)
            blockOffsets[index] = offsetInBlock
            offset += 4
        }

        // Data size
        offset += 4

        for (index in data!!.indices) {
            val itemNameLength = blockOffsets[index + 1] - blockOffsets[index]
            FastTrackUtility.validateSize(itemNameLength)
            data[index] = String(buffer, offset, itemNameLength, CharsetHelper.UTF16LE)
            offset += itemNameLength
        }
        return offset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun dumpData(pw: PrintWriter) {
        pw.println("  [Data")
        for (item in data!!) {
            pw.println("    $item")
        }
        pw.println("  ]")
    }
}
