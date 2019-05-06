/*
 * file:       ExtendedData.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       23/05/2003
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

import java.io.PrintWriter
import java.io.StringWriter
import java.util.Date
import java.util.HashMap

import net.sf.mpxj.common.ByteArrayHelper

/**
 * This class represents the extended data structure which is used to
 * hold additional non-core data items associated with tasks and resources.
 */
internal class ExtendedData
/**
 * Constructor. Given the var data block, and the offset of the extended
 * data block within the var data, the constructor extracts each data item
 * of extended data and inserts it into a Map using it's type as the key.
 *
 * @param varData Var data block
 * @param offset Offset of extended data within the var data block
 */
(private val m_data: FixDeferFix, offset: Int) {
    private val m_map = HashMap<Integer, ByteArray>()

    init {
        val data = m_data.getByteArray(offset)

        if (data != null) {
            var index = 0
            var size: Int
            var type: Int

            while (index < data.size) {
                size = MPPUtility.getInt(data, index)
                index += 4

                type = MPPUtility.getInt(data, index)
                index += 4

                m_map.put(Integer.valueOf(type), MPPUtility.cloneSubArray(data, index, size))
                index += size
            }
        }
    }

    /**
     * Retrieves a string value from the extended data.
     *
     * @param type Type identifier
     * @return string value
     */
    fun getString(type: Integer): String? {
        var result: String? = null

        val item = m_map.get(type)
        if (item != null) {
            result = m_data.getString(getOffset(item))
        }

        return result
    }

    /**
     * Retrieves a string value from the extended data.
     *
     * @param type Type identifier
     * @return string value
     */
    fun getUnicodeString(type: Integer): String? {
        var result: String? = null

        val item = m_map.get(type)
        if (item != null) {
            result = m_data.getUnicodeString(getOffset(item))
        }

        return result
    }

    /**
     * Retrieves a short int value from the extended data.
     *
     * @param type Type identifier
     * @return short int value
     */
    fun getShort(type: Integer): Int {
        var result = 0

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getShort(item, 0)
        }

        return result
    }

    /**
     * Retrieves an integer value from the extended data.
     *
     * @param type Type identifier
     * @return integer value
     */
    fun getInt(type: Integer): Int {
        var result = 0

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getInt(item, 0)
        }

        return result
    }

    /**
     * Retrieves a long value from the extended data.
     *
     * @param type Type identifier
     * @return long value
     */
    fun getLong(type: Integer): Long {
        var result: Long = 0

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getLong6(item, 0)
        }

        return result
    }

    /**
     * Retrieves a double value from the extended data.
     *
     * @param type Type identifier
     * @return double value
     */
    fun getDouble(type: Integer): Double {
        var result = 0.0

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getDouble(item, 0)
        }

        return result
    }

    /**
     * Retrieves a timestamp from the extended data.
     *
     * @param type Type identifier
     * @return timestamp
     */
    fun getTimestamp(type: Integer): Date? {
        var result: Date? = null

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getTimestamp(item, 0)
        }

        return result
    }

    /**
     * Retrieve the raw byte array.
     *
     * @param type data type
     * @return byte array
     */
    fun getByteArray(type: Integer): ByteArray {
        return m_map.get(type)
    }

    /**
     * Convert an integer into an offset.
     *
     * @param data four byte integer value
     * @return offset value
     */
    private fun getOffset(data: ByteArray): Int {
        return -1 - MPPUtility.getInt(data, 0)
    }

    /**
     * Used for debugging. Outputs the contents of the extended data
     * block as a formatted string.
     *
     * @return string representation of extended data block
     */
    @Override
    fun toString(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        pw.println("BEGIN ExtendedData")

        for (entry in m_map.entrySet()) {
            pw.println("Type: " + entry.getKey() + " Data:" + ByteArrayHelper.hexdump(entry.getValue(), false))
        }

        pw.println("END ExtendedData")
        pw.println()
        pw.close()

        return sw.toString()
    }
}
