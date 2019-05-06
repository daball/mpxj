/*
 * file:       UnknownTableReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       2018-10-11
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

package net.sf.mpxj.synchro

import java.io.IOException

/**
 * Read raw data from a table with unknown structure.
 */
internal class UnknownTableReader
/**
 * Constructor used where we know the row size and magic number.
 *
 * @param stream input stream
 * @param rowSize row size
 * @param rowMagicNumber row magic number
 */
@JvmOverloads constructor(stream: StreamReader, private val m_rowSize: Int = 0, private val m_rowMagicNumber: Int = 0) : TableReader(stream) {

    @Override
    @Throws(IOException::class)
    protected override fun readRow(stream: StreamReader, map: Map<String, Object>) {
        if (m_rowSize == 0) {
            //         System.out.println("REMAINDER");
            //         byte[] remainder = new byte[m_stream.available()];
            //         m_stream.read(remainder);
            //         System.out.println(ByteArrayHelper.hexdump(remainder, true, 16, ""));
            throw IllegalArgumentException("Unexpected records!")
        }

        map.put("UNKNOWN1", stream.readBytes(m_rowSize))
    }

    @Override
    override fun hasUUID(): Boolean {
        return false
    }

    @Override
    override fun rowMagicNumber(): Int {
        return m_rowMagicNumber
    }
}
/**
 * Constructor.
 *
 * @param stream input stream
 */
