/*
 * file:       BooleanColumn.java
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

/**
 * Column containing Boolean values.
 */
internal class BooleanColumn : AbstractColumn() {

    private var m_options: Array<String>? = null
    /**
     * {@inheritDoc}
     */
    @Override
    override fun postHeaderSkipBytes(): Int {
        return 34
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun readData(buffer: ByteArray, offset: Int): Int {
        var offset = offset
        val options = StringsWithLengthBlock().read(buffer, offset, false)
        m_options = options.data
        offset = options.offset

        offset = FastTrackUtility.skipToNextMatchingShort(buffer, offset, 0x000F)

        val numberOfItems = FastTrackUtility.getInt(buffer, offset) + 1
        FastTrackUtility.validateSize(numberOfItems)
        data = arrayOfNulls<Boolean>(numberOfItems)
        offset += 4

        // Data length
        offset += 4

        // Offsets to data
        offset += data!!.size * 4

        // Data length
        offset += 4

        for (index in data!!.indices) {
            val value = FastTrackUtility.getShort(buffer, offset)
            offset += 2
            if (value != 2) {
                data[index] = Boolean.valueOf(value == 1)
            }
        }

        return offset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun dumpData(pw: PrintWriter) {
        pw.println("  [Options")
        for (item in m_options!!) {
            pw.println("    $item")
        }
        pw.println("  ]")

        pw.println("  [Data")
        for (item in data!!) {
            pw.println("    $item")
        }
        pw.println("  ]")
    }
}
