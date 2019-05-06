/*
 * file:       Var2Data.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       03/01/2003
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
import java.util.Date
import java.util.TreeMap

import net.sf.mpxj.common.ByteArrayHelper
import net.sf.mpxj.common.StreamHelper

/**
 * This class represents a block of variable data. Each block of
 * data is represented by a 4 byte size, followed by the data itself.
 * Each Var2Data block should be associated with a MetaData block
 * which describes the layout of the data in the Var2Data block.
 */
internal class Var2Data
/**
 * Constructor. Extracts the content of the data block, with reference
 * to the meta data held in the VarMeta block.
 *
 * @param meta meta data for this block
 * @param is InputStream from which data is read
 * @throws IOException on file read error
 */
@Throws(IOException::class)
constructor(
        /**
         * Reference to the meta data associated with this block.
         */
        private val m_meta: VarMeta, `is`: InputStream) : MPPComponent() {

    /**
     * Retrieve the underlying meta data. This method is provided
     * mainly as a convenience for debugging.
     *
     * @return VarMeta instance
     */
    val varMeta: VarMeta
        get() = m_meta

    /**
     * Map containing data items indexed by offset.
     */
    private val m_map = TreeMap<Integer, ByteArray>()

    init {
        var data: ByteArray

        var currentOffset = 0
        val available = `is`.available()

        for (itemOffset in m_meta.offsets) {
            if (itemOffset >= available) {
                continue
            }

            if (currentOffset > itemOffset) {
                `is`.reset()
                StreamHelper.skip(`is`, itemOffset.toLong())
            } else {
                if (currentOffset < itemOffset) {
                    StreamHelper.skip(`is`, (itemOffset - currentOffset).toLong())
                }
            }

            val size = readInt(`is`)

            //
            // Try our best to handle corrupt files gracefully
            //
            if (size < 0 || size > `is`.available()) {
                continue
            }

            try {
                data = readByteArray(`is`, size)
            } catch (ex: IndexOutOfBoundsException) {
                // POI fails to read certain MPP files with this exception:
                // https://bz.apache.org/bugzilla/show_bug.cgi?id=61677
                // There is no fix presently, we just have to bail out at
                // this point - we're unable to read any more data.
                break
            }

            m_map.put(Integer.valueOf(itemOffset), data)
            currentOffset = itemOffset + 4 + size
        }
    }

    /**
     * This method retrieves a byte array containing the data at the
     * given offset in the block. If no data is found at the given offset
     * this method returns null.
     *
     * @param offset offset of required data
     * @return byte array containing required data
     */
    fun getByteArray(offset: Integer?): ByteArray? {
        var result: ByteArray? = null

        if (offset != null) {
            result = m_map.get(offset)
        }

        return result
    }

    /**
     * This method retrieves a byte array of the specified type,
     * belonging to the item with the specified unique ID.
     *
     * @param id unique ID of entity to which this data belongs
     * @param type data type identifier
     * @return byte array containing required data
     */
    fun getByteArray(id: Integer, type: Integer): ByteArray? {
        return getByteArray(m_meta.getOffset(id, type))
    }

    /**
     * This method retrieves the data at the given offset and returns
     * it as a String, assuming the underlying data is composed of
     * two byte characters.
     *
     * @param offset offset of required data
     * @return string containing required data
     */
    fun getUnicodeString(offset: Integer?): String? {
        var result: String? = null

        if (offset != null) {
            val value = m_map.get(offset)
            if (value != null) {
                result = MPPUtility.getUnicodeString(value, 0)
            }
        }

        return result
    }

    /**
     * This method retrieves a String of the specified type,
     * belonging to the item with the specified unique ID.
     *
     * @param id unique ID of entity to which this data belongs
     * @param type data type identifier
     * @return string containing required data
     */
    fun getUnicodeString(id: Integer, type: Integer): String? {
        return getUnicodeString(m_meta.getOffset(id, type))
    }

    /**
     * This method retrieves a timestamp of the specified type,
     * belonging to the item with the specified unique ID.
     *
     * @param id unique ID of entity to which this data belongs
     * @param type data type identifier
     * @return required timestamp data
     */
    fun getTimestamp(id: Integer, type: Integer): Date? {
        var result: Date? = null

        val offset = m_meta.getOffset(id, type)

        if (offset != null) {
            val value = m_map.get(offset)
            if (value != null && value!!.size >= 4) {
                result = MPPUtility.getTimestamp(value, 0)
            }
        }

        return result
    }

    /**
     * This method retrieves the data at the given offset and returns
     * it as a String, assuming the underlying data is composed of
     * single byte characters.
     *
     * @param offset offset of required data
     * @return string containing required data
     */
    fun getString(offset: Integer?): String? {
        var result: String? = null

        if (offset != null) {
            val value = m_map.get(offset)
            if (value != null) {
                result = MPPUtility.getString(value!!, 0)
            }
        }

        return result
    }

    /**
     * This method retrieves a string of the specified type,
     * belonging to the item with the specified unique ID.
     *
     * @param id unique ID of entity to which this data belongs
     * @param type data type identifier
     * @return required string data
     */
    fun getString(id: Integer, type: Integer): String? {
        return getString(m_meta.getOffset(id, type))
    }

    /**
     * This method retrieves an integer of the specified type,
     * belonging to the item with the specified unique ID.
     *
     * @param id unique ID of entity to which this data belongs
     * @param type data type identifier
     * @return required integer data
     */
    fun getShort(id: Integer, type: Integer): Int {
        var result = 0

        val offset = m_meta.getOffset(id, type)

        if (offset != null) {
            val value = m_map.get(offset)

            if (value != null && value!!.size >= 2) {
                result = MPPUtility.getShort(value, 0)
            }
        }

        return result
    }

    /**
     * This method retrieves an integer of the specified type,
     * belonging to the item with the specified unique ID.
     *
     * @param id unique ID of entity to which this data belongs
     * @param type data type identifier
     * @return required integer data
     */
    fun getByte(id: Integer, type: Integer): Int {
        var result = 0

        val offset = m_meta.getOffset(id, type)

        if (offset != null) {
            val value = m_map.get(offset)

            if (value != null) {
                result = MPPUtility.getByte(value!!, 0)
            }
        }

        return result
    }

    /**
     * This method retrieves an integer of the specified type,
     * belonging to the item with the specified unique ID.
     *
     * @param id unique ID of entity to which this data belongs
     * @param type data type identifier
     * @return required integer data
     */
    fun getInt(id: Integer, type: Integer): Int {
        var result = 0

        val offset = m_meta.getOffset(id, type)

        if (offset != null) {
            val value = m_map.get(offset)

            if (value != null && value!!.size >= 4) {
                result = MPPUtility.getInt(value, 0)
            }
        }

        return result
    }

    /**
     * This method retrieves an integer of the specified type,
     * belonging to the item with the specified unique ID. Note that
     * the integer value is read from an arbitrary offset within the
     * byte array of data.
     *
     * @param id unique ID of entity to which this data belongs
     * @param offset offset into the byte array fom which to read the integer
     * @param type data type identifier
     * @return required integer data
     */
    fun getInt(id: Integer, offset: Int, type: Integer): Int {
        var result = 0

        val metaOffset = m_meta.getOffset(id, type)

        if (metaOffset != null) {
            val value = m_map.get(metaOffset)

            if (value != null && value!!.size >= offset + 4) {
                result = MPPUtility.getInt(value, offset)
            }
        }

        return result
    }

    /**
     * This method retrieves an integer of the specified type,
     * belonging to the item with the specified unique ID.
     *
     * @param id unique ID of entity to which this data belongs
     * @param type data type identifier
     * @return required integer data
     */
    fun getLong(id: Integer, type: Integer): Long {
        var result: Long = 0

        val offset = m_meta.getOffset(id, type)

        if (offset != null) {
            val value = m_map.get(offset)

            if (value != null && value!!.size >= 8) {
                result = MPPUtility.getLong(value, 0)
            }
        }

        return result
    }

    /**
     * This method retrieves a double of the specified type,
     * belonging to the item with the specified unique ID.
     *
     * @param id unique ID of entity to which this data belongs
     * @param type data type identifier
     * @return required double data
     */
    fun getDouble(id: Integer, type: Integer): Double {
        var result = Double.longBitsToDouble(getLong(id, type))
        if (Double.isNaN(result)) {
            result = 0.0
        }
        return result
    }

    /**
     * This method dumps the contents of this Var2Data block as a String.
     * Note that this facility is provided as a debugging aid.
     *
     * @return formatted contents of this block
     */
    @Override
    fun toString(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        pw.println("BEGIN Var2Data")
        for (entry in m_map.entrySet()) {
            pw.println("   Data at offset: " + entry.getKey() + " size: " + entry.getValue().length)
            pw.println(ByteArrayHelper.hexdump(entry.getValue(), true, 16, "   "))
        }

        pw.println("END Var2Data")
        pw.println()
        pw.close()
        return sw.toString()
    }

    /**
     * This is a specialised version of the toString method which
     * outputs just the data in this structure for the given unique ID.
     *
     * @param id unique ID
     * @return string representation
     */
    fun toString(id: Integer): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        pw.println("BEGIN Var2Data for $id")
        for (type in m_meta.getTypes(id)) {
            val offset = m_meta.getOffset(id, type)
            val data = m_map.get(offset)
            pw.println("   Data at offset: " + offset + " size: " + data.size)
            pw.println(ByteArrayHelper.hexdump(data, true, 16, "   "))
        }
        pw.println("END Var2Data for $id")
        pw.println()
        pw.close()
        return sw.toString()
    }
}
