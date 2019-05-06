/*
 * file:       DateColumn.java
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
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

import net.sf.mpxj.common.DateHelper

/**
 * Column containing dates.
 */
internal class DateColumn : AbstractColumn() {
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

        // Structure flags? See StringColumn...
        offset += 4

        // Originally I though that there was a fixed 48 byte offset from the end of
        // the header to the start of the data. In fact there appears to be an optional
        // block of string data after the header, but before the binary version of the dates.
        // The string dates in this optional block don't appear to match the actual dates, so
        // we skip past them. We're looking for a byte pattern which we expect at the start
        // of the block of binary dates... it's fragile, but the best we can do at the moment.
        offset = FastTrackUtility.skipToNextMatchingShort(buffer, offset, 0x000A) - 2

        val data = FixedSizeItemsBlock().read(buffer, offset)
        offset = data.offset

        val cal = DateHelper.popCalendar()
        val rawData = data.data
        this.data = arrayOfNulls<Date>(rawData!!.size)
        for (index in rawData.indices) {
            val rawValue = rawData[index]
            if (rawValue != null && rawValue.size >= 4) {
                val value = FastTrackUtility.getInt(rawValue, 0)
                if (value > 0) {
                    cal.setTimeInMillis(DATE_EPOCH)
                    cal.add(Calendar.DAY_OF_YEAR, value)
                    this.data[index] = cal.getTime()
                }
            }
        }
        DateHelper.pushCalendar(cal)

        return offset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun dumpData(pw: PrintWriter) {
        val df = SimpleDateFormat("dd/MM/yyyy")
        pw.println("  [Data")
        for (item in data!!) {
            val value = if (item == null) "" else df.format(item as Date)
            pw.println("    $value")
        }
        pw.println("  ]")
    }

    companion object {

        /**
         * 31/12/1979 00:00.
         */
        private val DATE_EPOCH = 315446400000L
    }
}
