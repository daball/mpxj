/*
 * file:       Props9.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2003
 * date:       07/11/2003
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

//import java.io.FileOutputStream;
import java.io.IOException
import java.io.InputStream

import net.sf.mpxj.common.StreamHelper

/**
 * This class represents the Props files found in Microsoft Project MPP9 files.
 */
internal class Props9
/**
 * Constructor, reads the property data from an input stream.
 *
 * @param is input stream for reading props data
 */
@Throws(IOException::class)
constructor(`is`: InputStream) : Props() {
    init {
        //FileOutputStream fos = new FileOutputStream ("c:\\temp\\props9." + System.currentTimeMillis() + ".txt");
        //PrintWriter pw = new PrintWriter (fos);

        val header = ByteArray(16)
        var data: ByteArray
        `is`.read(header)

        val headerCount = MPPUtility.getShort(header, 12)
        var foundCount = 0
        var availableBytes = `is`.available()

        while (foundCount < headerCount) {
            val itemSize = readInt(`is`)
            val itemKey = readInt(`is`)
            /*int attrib3 = */readInt(`is`)
            availableBytes -= 12

            if (availableBytes < itemSize || itemSize < 1) {
                break
            }

            data = ByteArray(itemSize)
            `is`.read(data)
            availableBytes -= itemSize

            m_map.put(Integer.valueOf(itemKey), data)
            //pw.println(foundCount + " "+ attrib2 + ": " + ByteArrayHelper.hexdump(data, true));
            ++foundCount

            //
            // Align to two byte boundary
            //
            if (data.size % 2 != 0) {
                StreamHelper.skip(`is`, 1)
            }
        }

        //pw.flush();
        //pw.close();
    }
}
