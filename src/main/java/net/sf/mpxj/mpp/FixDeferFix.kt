/*
 * file:       FixDeferFix.java
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
import java.util.TreeSet

import net.sf.mpxj.common.ByteArrayHelper

/**
 * This class represents the a block of variable length data items that appears
 * in the Microsoft Project 98 file format.
 */
internal class FixDeferFix
/**
 * Extract the variable size data items from the input stream.
 *
 * @param is Input stream
 * @throws IOException Thrown on read errors
 */
@Throws(IOException::class)
constructor(`is`: InputStream) : MPPComponent() {

    private val m_data: ByteArray

    init {
        m_data = ByteArray(`is`.available())
        `is`.read(m_data)
    }

    /**
     * Retrieve a byte array of containing the data starting at the supplied
     * offset in the FixDeferFix file. Note that this method will return null
     * if the requested data is not found for some reason.
     *
     * @param offset Offset into the file
     * @return Byte array containing the requested data
     */
    fun getByteArray(offset: Int): ByteArray? {
        var offset = offset
        var result: ByteArray? = null

        if (offset > 0 && offset < m_data.size) {
            var nextBlockOffset = MPPUtility.getInt(m_data, offset)
            offset += 4

            val itemSize = MPPUtility.getInt(m_data, offset)
            offset += 4

            if (itemSize > 0 && itemSize < m_data.size) {
                var blockRemainingSize = 28

                if (nextBlockOffset != -1 || itemSize <= blockRemainingSize) {
                    var itemRemainingSize = itemSize
                    result = ByteArray(itemSize)
                    var resultOffset = 0

                    while (nextBlockOffset != -1) {
                        MPPUtility.getByteArray(m_data, offset, blockRemainingSize, result, resultOffset)
                        resultOffset += blockRemainingSize
                        offset += blockRemainingSize
                        itemRemainingSize -= blockRemainingSize

                        if (offset != nextBlockOffset) {
                            offset = nextBlockOffset
                        }

                        nextBlockOffset = MPPUtility.getInt(m_data, offset)
                        offset += 4
                        blockRemainingSize = 32
                    }

                    MPPUtility.getByteArray(m_data, offset, itemRemainingSize, result, resultOffset)
                }
            }
        }

        return result
    }

    /**
     * This method retrieves the string at the specified offset.
     *
     * @param offset Offset into var data
     * @return String value
     */
    fun getString(offset: Int): String? {
        var result: String? = null
        val data = getByteArray(offset)
        if (data != null) {
            result = String(data)
        }

        return result
    }

    /**
     * This method retrieves the string at the specified offset.
     *
     * @param offset Offset into var data
     * @return String value
     */
    fun getUnicodeString(offset: Int): String? {
        var result: String? = null
        val data = getByteArray(offset)
        if (data != null) {
            result = MPPUtility.getUnicodeString(data, 0)
        }

        return result
    }

    /**
     * This method dumps the contents of this FixDeferFix block as a String.
     * Note that this facility is provided as a debugging aid.
     *
     * @return formatted contents of this block
     */
    @Override
    fun toString(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        pw.println("BEGIN FixDeferFix")

        //
        // Calculate the block size
        //
        val available = m_data.size

        //
        // 4 byte header
        //
        var fileOffset = 0
        MPPUtility.getInt(m_data, fileOffset)
        fileOffset += 4

        //
        // Read data
        //
        var itemSize: Int
        var itemRemainingSize: Int
        var blockRemainingSize: Int
        var skip: Int
        var nextBlockOffset: Int
        var buffer: ByteArray
        var bufferOffset: Int
        val skipped = TreeSet<Integer>()
        val read = TreeSet<Integer>()
        var startOffset: Int

        while (fileOffset < available || skipped.size() !== 0) {
            var temp: Integer

            if (fileOffset >= available) {
                temp = skipped.first()
                skipped.remove(temp)
                fileOffset = temp.intValue()
            }

            temp = Integer.valueOf(fileOffset)
            if (read.add(temp) === false) {
                fileOffset = available
                continue
            }

            startOffset = fileOffset

            nextBlockOffset = MPPUtility.getInt(m_data, fileOffset)
            fileOffset += 4

            itemSize = MPPUtility.getInt(m_data, fileOffset)
            fileOffset += 4

            blockRemainingSize = 28

            if (nextBlockOffset == -1 && itemSize > blockRemainingSize) {
                fileOffset += blockRemainingSize
                continue
            }

            itemRemainingSize = itemSize
            buffer = ByteArray(itemSize)
            bufferOffset = 0

            while (nextBlockOffset != -1) {
                MPPUtility.getByteArray(m_data, fileOffset, blockRemainingSize, buffer, bufferOffset)
                bufferOffset += blockRemainingSize
                fileOffset += blockRemainingSize
                itemRemainingSize -= blockRemainingSize

                if (fileOffset != nextBlockOffset) {
                    skipped.add(Integer.valueOf(fileOffset))
                    fileOffset = nextBlockOffset
                }

                temp = Integer.valueOf(fileOffset)
                if (read.add(temp) === false) {
                    fileOffset = available
                    continue
                }

                nextBlockOffset = MPPUtility.getInt(m_data, fileOffset)
                fileOffset += 4
                blockRemainingSize = 32
            }

            MPPUtility.getByteArray(m_data, fileOffset, itemRemainingSize, buffer, bufferOffset)
            fileOffset += itemRemainingSize

            if (itemRemainingSize < blockRemainingSize) {
                skip = blockRemainingSize - itemRemainingSize
                fileOffset += skip
            }

            pw.println("   Data: offset: " + startOffset + " size: " + buffer.size)
            pw.println("  " + ByteArrayHelper.hexdump(buffer, true))
        }

        pw.println("END FixDeferFix")
        pw.println()
        pw.close()

        return sw.toString()
    }
}
