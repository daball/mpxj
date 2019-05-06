/*
 * file:       CalendarColumn.java
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
 * Column containing calendar name.
 */
internal class CalendarColumn : AbstractColumn() {
    /**
     * {@inheritDoc}
     */
    @Override
    override fun postHeaderSkipBytes(): Int {
        return 18
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun readData(buffer: ByteArray, offset: Int): Int {
        var offset = offset
        val data = FixedSizeItemsBlock().read(buffer, offset)
        offset = data.offset

        val rawData = data.data
        this.data = arrayOfNulls<Integer>(rawData!!.size)
        for (index in rawData.indices) {
            this.data[index] = Integer.valueOf(FastTrackUtility.getInt(rawData[index], 0))
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
