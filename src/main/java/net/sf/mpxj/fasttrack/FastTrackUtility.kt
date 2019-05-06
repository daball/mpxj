/*
 * file:       FastTrackUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       14/03/2016
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

import java.text.DecimalFormat

import net.sf.mpxj.TimeUnit

/**
 * Common methods used when reading an FTS file.
 */
internal object FastTrackUtility {

    private val NULL_DOUBLE = 0x3949F623D5A8A733L

    private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    /**
     * Ensure that a size value falls within sensible bounds.
     *
     * @param size value
     */
    fun validateSize(size: Int) {
        if (size < 0 || size > 100000) {
            throw UnexpectedStructureException()
        }
    }

    /**
     * Ensure that an array index is in range.
     *
     * @param buffer array of data
     * @param offset index into array
     */
    fun validateOffset(buffer: ByteArray, offset: Int) {
        if (offset >= buffer.size) {
            throw UnexpectedStructureException()
        }
    }

    /**
     * Retrieve a four byte integer.
     *
     * @param data array of data
     * @param offset offset into array
     * @return int value
     */
    fun getInt(data: ByteArray, offset: Int): Int {
        var result = 0
        var i = offset
        var shiftBy = 0
        while (shiftBy < 32) {
            result = result or (data[i] and 0xff shl shiftBy)
            ++i
            shiftBy += 8
        }
        return result
    }

    /**
     * Retrieve a two byte integer.
     *
     * @param data array of data
     * @param offset offset into array
     * @return int value
     */
    fun getShort(data: ByteArray, offset: Int): Int {
        var result = 0
        var i = offset
        var shiftBy = 0
        while (shiftBy < 16) {
            result = result or (data[i] and 0xff shl shiftBy)
            ++i
            shiftBy += 8
        }
        return result
    }

    /**
     * This method reads an eight byte integer from the input array.
     *
     * @param data the input array
     * @param offset offset of integer data in the array
     * @return integer value
     */
    fun getLong(data: ByteArray, offset: Int): Long {
        if (data.size != 8) {
            throw UnexpectedStructureException()
        }

        var result: Long = 0
        var i = offset
        var shiftBy = 0
        while (shiftBy < 64) {
            result = result or ((data[i] and 0xff).toLong() shl shiftBy)
            ++i
            shiftBy += 8
        }
        return result
    }

    /**
     * This method reads an eight byte double from the input array.
     *
     * @param data the input array
     * @param offset offset of double data in the array
     * @return double value
     */
    fun getDouble(data: ByteArray, offset: Int): Double? {
        var result: Double? = null
        val longValue = getLong(data, offset)
        if (longValue != NULL_DOUBLE) {
            val doubleValue = Double.longBitsToDouble(longValue)
            if (!Double.isNaN(doubleValue)) {
                result = Double.valueOf(doubleValue)
            }
        }
        return result
    }

    /**
     * Retrieve a single byte from an input array.
     *
     * @param data input array
     * @param offset offset into inut array
     * @return byte value
     */
    fun getByte(data: ByteArray, offset: Int): Int {
        return data[offset] and 0xFF
    }

    /**
     * Convert an integer value into a TimeUnit instance.
     *
     * @param value time unit value
     * @return TimeUnit instance
     */
    fun getTimeUnit(value: Int): TimeUnit? {
        var result: TimeUnit? = null

        when (value) {
            1 -> {
                // Appears to mean "use the document format"
                result = TimeUnit.ELAPSED_DAYS
            }

            2 -> {
                result = TimeUnit.HOURS
            }

            4 -> {
                result = TimeUnit.DAYS
            }

            6 -> {
                result = TimeUnit.WEEKS
            }

            8, 10 -> {
                result = TimeUnit.MONTHS
            }

            12 -> {
                result = TimeUnit.YEARS
            }

            else -> {
            }
        }

        return result
    }

    /**
     * Skip to the next matching short value.
     *
     * @param buffer input data array
     * @param offset start offset into the input array
     * @param value value to match
     * @return offset of matching pattern
     */
    fun skipToNextMatchingShort(buffer: ByteArray, offset: Int, value: Int): Int {
        var nextOffset = offset
        while (getShort(buffer, nextOffset) != value) {
            ++nextOffset
        }
        nextOffset += 2

        return nextOffset
    }

    /**
     * Dump raw data as hex.
     *
     * @param buffer buffer
     * @param offset offset into buffer
     * @param length length of data to dump
     * @param ascii true if ASCII should also be printed
     * @param columns number of columns
     * @param prefix prefix when printing
     * @return hex dump
     */
    fun hexdump(buffer: ByteArray?, offset: Int, length: Int, ascii: Boolean, columns: Int, prefix: String): String {
        var columns = columns
        val sb = StringBuilder()
        if (buffer != null) {
            var index = offset
            val df = DecimalFormat("00000")

            while (index < offset + length) {
                if (index + columns > offset + length) {
                    columns = offset + length - index
                }

                sb.append(prefix)
                sb.append(df.format(index - offset))
                sb.append(":")
                sb.append(hexdump(buffer, index, columns, ascii))
                sb.append('\n')

                index += columns
            }
        }

        return sb.toString()
    }

    /**
     * Dump raw data as hex.
     *
     * @param buffer buffer
     * @param offset offset into buffer
     * @param length length of data to dump
     * @param ascii true if ASCII should also be printed
     * @return hex dump
     */
    fun hexdump(buffer: ByteArray?, offset: Int, length: Int, ascii: Boolean): String {
        val sb = StringBuilder()

        if (buffer != null) {
            var c: Char
            var loop: Int
            val count = offset + length

            loop = offset
            while (loop < count) {
                sb.append(" ")
                sb.append(HEX_DIGITS[buffer[loop] and 0xF0 shr 4])
                sb.append(HEX_DIGITS[buffer[loop] and 0x0F])
                loop++
            }

            if (ascii == true) {
                sb.append("   ")

                loop = offset
                while (loop < count) {
                    c = buffer[loop].toChar()

                    if (c.toInt() > 200 || c.toInt() < 27) {
                        c = ' '
                    }

                    sb.append(c)
                    loop++
                }
            }
        }

        return sb.toString()
    }
}
