/*
 * file:       StreamReader.java
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
import java.io.InputStream
import java.util.Collections
import java.util.Date
import java.util.UUID

import net.sf.mpxj.Duration
import net.sf.mpxj.common.ByteArray

/**
 * This class wraps an input stream, providing methods to read specific types.
 */
internal class StreamReader
/**
 * Constructor.
 *
 * @param majorVersion major version
 * @param stream input stream
 */
(
        /**
         * Retrieve the major version number of this file.
         *
         * @return major version number
         */
        val majorVersion: Int, private val m_stream: InputStream) {

    /**
     * Read a single byte.
     *
     * @return Integer instance representing the byte read
     */
    @Throws(IOException::class)
    fun readByte(): Integer {
        return Integer.valueOf(m_stream.read())
    }

    /**
     * Read a Boolean.
     *
     * @return Boolean instance
     */
    @Throws(IOException::class)
    fun readBoolean(): Boolean {
        return Boolean.valueOf(DatatypeConverter.getBoolean(m_stream))
    }

    /**
     * Read a nested table. Instantiates the supplied reader class to
     * extract the data.
     *
     * @param reader table reader class
     * @return table rows
     */
    @Throws(IOException::class)
    fun readTable(reader: TableReader): List<MapRow> {
        reader.read()
        return reader.rows
    }

    /**
     * Read a nested table whose contents we don't understand.
     *
     * @param rowSize fixed row size
     * @param rowMagicNumber row magic number
     * @return table rows
     */
    @Throws(IOException::class)
    fun readUnknownTable(rowSize: Int, rowMagicNumber: Int): List<MapRow> {
        val reader = UnknownTableReader(this, rowSize, rowMagicNumber)
        reader.read()
        return reader.rows
    }

    /**
     * Reads a nested table. Uses the supplied reader class instance.
     *
     * @param readerClass reader class instance
     * @return table rows
     */
    @Throws(IOException::class)
    fun readTable(readerClass: Class<out TableReader>): List<MapRow> {
        val reader: TableReader

        try {
            reader = readerClass.getConstructor(StreamReader::class.java).newInstance(this)
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

        return readTable(reader)
    }

    /**
     * Conditionally read a nested table based in the value of a boolean flag which precedes the table data.
     *
     * @param readerClass reader class
     * @return table rows or empty list if table not present
     */
    @Throws(IOException::class)
    fun readTableConditional(readerClass: Class<out TableReader>): List<MapRow> {
        val result: List<MapRow>
        if (DatatypeConverter.getBoolean(m_stream)) {
            result = readTable(readerClass)
        } else {
            result = Collections.emptyList()
        }
        return result
    }

    /**
     * Read an array of bytes of a specified size.
     *
     * @param size number of bytes to read
     * @return ByteArray instance
     */
    @Throws(IOException::class)
    fun readBytes(size: Int): ByteArray {
        val data = ByteArray(size)
        m_stream.read(data)
        return ByteArray(data)
    }

    /**
     * Read a UUID.
     *
     * @return UUID instance
     */
    @Throws(IOException::class)
    fun readUUID(): UUID {
        return DatatypeConverter.getUUID(m_stream)
    }

    /**
     * Read a string.
     *
     * @return String instance
     */
    @Throws(IOException::class)
    fun readString(): String? {
        return DatatypeConverter.getString(m_stream)
    }

    /**
     * Read a date.
     *
     * @return Date instance.
     */
    @Throws(IOException::class)
    fun readDate(): Date? {
        return DatatypeConverter.getDate(m_stream)
    }

    /**
     * Read a time value.
     *
     * @return Date instance
     */
    @Throws(IOException::class)
    fun readTime(): Date? {
        return DatatypeConverter.getTime(m_stream)
    }

    /**
     * Read a duration.
     *
     * @return Duration instance
     */
    @Throws(IOException::class)
    fun readDuration(): Duration {
        return DatatypeConverter.getDuration(m_stream)
    }

    /**
     * Read an int.
     *
     * @return int value
     */
    @Throws(IOException::class)
    fun readInt(): Int {
        return DatatypeConverter.getInt(m_stream)
    }

    /**
     * Read an integer.
     *
     * @return Integer instance
     */
    @Throws(IOException::class)
    fun readInteger(): Integer {
        return DatatypeConverter.getInteger(m_stream)
    }

    /**
     * Read a double.
     *
     * @return Double instance.
     */
    @Throws(IOException::class)
    fun readDouble(): Double {
        return DatatypeConverter.getDouble(m_stream)
    }

    /**
     * Read a list of fixed size blocks as byte arrays.
     *
     * @param size fixed block size
     * @return list of blocks
     */
    @Throws(IOException::class)
    fun readUnknownBlocks(size: Int): List<MapRow> {
        return UnknownBlockReader(this, size).read()
    }

    /**
     * Read a list of fixed size blocks using an instance of the supplied reader class.
     *
     * @param readerClass reader class
     * @return list of blocks
     */
    @Throws(IOException::class)
    fun readBlocks(readerClass: Class<out BlockReader>): List<MapRow> {
        val reader: BlockReader

        try {
            reader = readerClass.getConstructor(StreamReader::class.java).newInstance(this)
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

        return reader.read()
    }
}
