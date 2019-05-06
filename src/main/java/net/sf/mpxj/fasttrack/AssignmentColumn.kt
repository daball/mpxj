/*
 * file:       AssignmentColumn.java
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
 * Represents resource assignments.
 */
internal class AssignmentColumn : AbstractColumn() {

    private var m_options: Array<String>? = null
    /**
     * {@inheritDoc}
     */
    @Override
    override fun postHeaderSkipBytes(): Int {
        return 14
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun readData(buffer: ByteArray, offset: Int): Int {
        var offset = offset
        if (FastTrackUtility.getByte(buffer, offset) == 0x01) {
            offset += 2
        } else {
            offset += 20
            val options = StringsWithLengthBlock().read(buffer, offset, false)
            m_options = options.data
            offset = options.offset

            // Skip bytes
            offset += 8
        }

        val data = StringsWithLengthBlock().read(buffer, offset, true)
        this.data = data.data
        offset = data.offset

        return offset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun dumpData(pw: PrintWriter) {
        if (m_options != null) {
            pw.println("  [Options")
            for (item in m_options!!) {
                pw.println("    $item")
            }
            pw.println("  ]")
        }
        pw.println("  [Data")
        for (item in data!!) {
            pw.println("    $item")
        }
        pw.println("  ]")
    }
}
