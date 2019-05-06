/*
 * file:       BlockHeader.java
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

import java.io.ByteArrayOutputStream
import java.io.PrintWriter

import net.sf.mpxj.common.CharsetHelper

/**
 * Common header structure which appears at the start of each block containing column data.
 */
internal class BlockHeader {

    private val m_header = ByteArray(8)
    private var m_skip: ByteArray? = null
    /**
     * Retrieve the offset after reading the header.
     *
     * @return offset
     */
    var offset: Int = 0
        private set
    /**
     * Retrieve the name of the column represented by this block.
     *
     * @return column name
     */
    var name: String? = null
        private set
    /**
     * Retrieve the column type.
     *
     * @return column type
     */
    var columnType: Int = 0
        private set
    /**
     * Retreve additional flags present in the header.
     *
     * @return flags
     */
    var flags: Int = 0
        private set

    /**
     * Reads the header data from a block.
     *
     * @param buffer block data
     * @param offset current offset into block data
     * @param postHeaderSkipBytes bytes to skip after reading the header
     * @return current BlockHeader instance
     */
    fun read(buffer: ByteArray, offset: Int, postHeaderSkipBytes: Int): BlockHeader {
        this.offset = offset

        System.arraycopy(buffer, this.offset, m_header, 0, 8)
        this.offset += 8

        val nameLength = FastTrackUtility.getInt(buffer, this.offset)
        this.offset += 4

        if (nameLength < 1 || nameLength > 255) {
            throw UnexpectedStructureException()
        }

        name = String(buffer, this.offset, nameLength, CharsetHelper.UTF16LE)
        this.offset += nameLength

        columnType = FastTrackUtility.getShort(buffer, this.offset)
        this.offset += 2

        flags = FastTrackUtility.getShort(buffer, this.offset)
        this.offset += 2

        m_skip = ByteArray(postHeaderSkipBytes)
        System.arraycopy(buffer, this.offset, m_skip, 0, postHeaderSkipBytes)
        this.offset += postHeaderSkipBytes

        return this
    }

    @Override
    fun toString(): String {
        val os = ByteArrayOutputStream()
        val pw = PrintWriter(os)
        pw.println("  [BlockHeader")
        pw.print("    Header: " + FastTrackUtility.hexdump(m_header, 0, m_header.size, false, 16, ""))
        pw.println("    Name: " + name!!)
        pw.println("    Type: $columnType")
        pw.println("    Flags: $flags")
        pw.print("    Skip:\n" + FastTrackUtility.hexdump(m_skip, 0, m_skip!!.size, false, 16, "      "))
        pw.println("  ]")
        pw.flush()
        return os.toString()

    }
}
