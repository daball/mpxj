/*
 * file:       ByteArrayHelper.java
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

package net.sf.mpxj.common

import java.text.DecimalFormat

/**
 * Helper methods for working with byte arrays.
 */
object ByteArrayHelper {

    /**
     * Constants used to convert bytes to hex digits.
     */
    private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    /**
     * This method generates a formatted version of the data contained
     * in a byte array. The data is written both in hex, and as ASCII
     * characters.
     *
     * @param buffer data to be displayed
     * @param offset offset of start of data to be displayed
     * @param length length of data to be displayed
     * @param ascii flag indicating whether ASCII equivalent chars should also be displayed
     * @return formatted string
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

    /**
     * This method generates a formatted version of the data contained
     * in a byte array. The data is written both in hex, and as ASCII
     * characters.
     *
     * @param buffer data to be displayed
     * @param ascii flag indicating whether ASCII equivalent chars should also be displayed
     * @return formatted string
     */
    fun hexdump(buffer: ByteArray?, ascii: Boolean): String {
        var length = 0

        if (buffer != null) {
            length = buffer.size
        }

        return hexdump(buffer, 0, length, ascii)
    }

    /**
     * This method generates a formatted version of the data contained
     * in a byte array. The data is written both in hex, and as ASCII
     * characters. The data is organised into fixed width columns.
     *
     * @param buffer data to be displayed
     * @param ascii flag indicating whether ASCII equivalent chars should also be displayed
     * @param columns number of columns
     * @param prefix prefix to be added before the start of the data
     * @return formatted string
     */
    fun hexdump(buffer: ByteArray?, ascii: Boolean, columns: Int, prefix: String): String {
        var columns = columns
        val sb = StringBuilder()
        if (buffer != null) {
            var index = 0
            val df = DecimalFormat("00000")

            while (index < buffer.size) {
                if (index + columns > buffer.size) {
                    columns = buffer.size - index
                }

                sb.append(prefix)
                sb.append(df.format(index))
                sb.append(":")
                sb.append(hexdump(buffer, index, columns, ascii))
                sb.append('\n')

                index += columns
            }
        }

        return sb.toString()
    }

    /**
     * This method generates a formatted version of the data contained
     * in a byte array. The data is written both in hex, and as ASCII
     * characters. The data is organised into fixed width columns.
     *
     * @param buffer data to be displayed
     * @param offset offset into buffer
     * @param length number of bytes to display
     * @param ascii flag indicating whether ASCII equivalent chars should also be displayed
     * @param columns number of columns
     * @param prefix prefix to be added before the start of the data
     * @return formatted string
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
}
