/*
 * file:       EnumColumn.java
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
 * Column containing enumerated values.
 */
internal class EnumColumn : AbstractColumn() {

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

        // Skip bytes
        offset += 4

        val data = FixedSizeItemsBlock().read(buffer, offset)
        offset = data.offset

        val rawData = data.data
        this.data = arrayOfNulls<String>(rawData!!.size)
        for (index in rawData.indices) {
            val optionIndex = FastTrackUtility.getShort(rawData[index], 0) - 1
            if (optionIndex >= 0 && optionIndex < m_options!!.size) {
                this.data[index] = m_options!![optionIndex]
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
