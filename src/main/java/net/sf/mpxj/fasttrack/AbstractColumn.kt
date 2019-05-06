/*
 * file:       AbstractColumn.java
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

/**
 * Implements common elements of the FastTrackColumn interface.
 */
internal abstract class AbstractColumn : FastTrackColumn {

    override val name: String?
        @Override get() = if (m_header == null) "<unknown>" else m_header!!.name

    private var m_header: BlockHeader? = null
    private var m_trailer: ByteArray? = null
    @get:Override
    override var type: FastTrackField? = null
        private set
    @get:Override
    override var data: Array<Object>? = null
        protected set

    @Override
    override fun read(tableType: FastTrackTableType, buffer: ByteArray, startIndex: Int, length: Int) {
        m_header = BlockHeader().read(buffer, startIndex, postHeaderSkipBytes())
        setFieldType(tableType)
        val offset = readData(buffer, m_header!!.offset)

        if (length > offset) {
            m_trailer = ByteArray(length - offset)
            System.arraycopy(buffer, startIndex + offset, m_trailer, 0, m_trailer!!.size)
        } else {
            m_trailer = ByteArray(0)
        }
    }

    /**
     * Number of bytes to skip once the header has been read.
     *
     * @return number of bytes
     */
    protected abstract fun postHeaderSkipBytes(): Int

    /**
     * Reads data in a format specific to the column type.
     *
     * @param buffer buffer containing column data
     * @param offset current offset into the buffer
     * @return offset after reading the column data
     */
    protected abstract fun readData(buffer: ByteArray, offset: Int): Int

    /**
     * Dump the column data for debugging.
     *
     * @param pw debug output
     */
    protected abstract fun dumpData(pw: PrintWriter)

    @Override
    fun toString(): String {
        val os = ByteArrayOutputStream()
        val pw = PrintWriter(os)
        pw.println("[" + getClass().getSimpleName())
        pw.println(m_header!!.toString())
        dumpData(pw)
        pw.print("  Trailer: " + FastTrackUtility.hexdump(m_trailer, 0, m_trailer!!.size, false, 16, ""))
        pw.println("]")
        pw.flush()
        return os.toString()
    }

    /**
     * Set the enum representing the type of this column.
     *
     * @param tableType type of table to which this column belongs
     */
    private fun setFieldType(tableType: FastTrackTableType) {
        when (tableType) {
            FastTrackTableType.ACTBARS -> {
                type = ActBarField.getInstance(m_header!!.columnType)
            }
            FastTrackTableType.ACTIVITIES -> {
                type = ActivityField.getInstance(m_header!!.columnType)
            }
            FastTrackTableType.RESOURCES -> {
                type = ResourceField.getInstance(m_header!!.columnType)
            }
        }
    }
}
