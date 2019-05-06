/*
 * file:       FixFix.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       31/03/2003
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

import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter

import net.sf.mpxj.common.ByteArrayHelper

/**
 * This class represents the a block of fixed length data items that appears
 * in the Microsoft Project 98 file format.
 */
internal class FixFix
/**
 * Constructor. Extract fixed size data items from the input stream.
 * Note that for MSP98 we normally expect the data blocks to be a standard
 * size, as supplied in the itemSize parameter. However we have found
 * example files where the block size is larger, hence the requirement for
 * the code to check for a remainder when the overall size is divided by
 * the block size. If the remainder is non-zero, we iteratively increase the
 * itemSize until we find one that fits the overall available data size.
 *
 * @param itemSize Size of the items held in this block
 * @param is Input stream
 * @throws IOException Thrown when reading from the stream fails
 */
@Throws(IOException::class)
constructor(itemSize: Int, `is`: InputStream) : MPPComponent() {

    /**
     * This method is used to retrieve the remainder obtained when the
     * available data size is divided by the expected item size. If this
     * value is non-zero, it suggests that the available data contains
     * items of a different size.
     *
     * @return remainder
     */
    val diff: Int
        get() = m_diff

    /**
     * This method retrieves the overall data block size.
     *
     * @return data block size
     */
    val size: Int
        get() = m_size

    /**
     * Accessor method used to retrieve the number of items held in
     * this fixed data block.
     *
     * @return number of items in the block
     */
    val itemCount: Int
        get() = m_array.size

    /**
     * An array containing all of the items of data held in this block.
     */
    private val m_array: Array<Object>

    /**
     * Overall data block size.
     */
    private val m_size: Int

    /**
     * Variable containing the remainder after the available size has
     * been divided by the item size.
     */
    private val m_diff: Int

    init {
        m_size = `is`.available()
        m_diff = m_size % itemSize
        val itemCount = m_size / itemSize
        m_array = arrayOfNulls<Object>(itemCount)

        for (loop in 0 until itemCount) {
            m_array[loop] = readByteArray(`is`, itemSize)
        }
    }

    /**
     * This method retrieves a byte array containing the data at the
     * given index in the block. If no data is found at the given index
     * this method returns null.
     *
     * @param index index of the data item to be retrieved
     * @return byte array containing the requested data
     */
    fun getByteArrayValue(index: Int): ByteArray? {
        var result: ByteArray? = null

        if (m_array[index] != null) {
            result = m_array[index]
        }

        return result
    }

    /**
     * This method dumps the contents of this FixFix block as a String.
     * Note that this facility is provided as a debugging aid.
     *
     * @return formatted contents of this block
     */
    @Override
    fun toString(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        pw.println("BEGIN FixFix")
        for (loop in m_array.indices) {
            pw.println("   Data at index: $loop")
            pw.println("  " + ByteArrayHelper.hexdump(m_array[loop] as ByteArray, true))
        }
        pw.println("END FixFix")

        pw.println()
        pw.close()
        return sw.toString()
    }
}
