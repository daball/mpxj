/*
 * file:       TableReader.java
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
import java.util.ArrayList
import java.util.LinkedHashMap

/**
 * Common functionality to support reading Synchro tables.
 */
internal abstract class TableReader
/**
 * Constructor.
 *
 * @param stream input stream
 */
(protected val m_stream: StreamReader) {
    /**
     * Retrieve the rows read from the table.
     *
     * @return table rows
     */
    val rows: List<MapRow> = ArrayList<MapRow>()

    /**
     * Read data from the table. Return a reference to the current
     * instance to allow method chaining.
     *
     * @return reader instance
     */
    @Throws(IOException::class)
    fun read(): TableReader {
        val tableHeader = m_stream.readInt()
        if (tableHeader != 0x39AF547A) {
            throw IllegalArgumentException("Unexpected file format")
        }

        val recordCount = m_stream.readInt()
        for (loop in 0 until recordCount) {
            val rowMagicNumber = m_stream.readInt()
            if (rowMagicNumber != rowMagicNumber()) {
                throw IllegalArgumentException("Unexpected file format")
            }

            // We use a LinkedHashMap to preserve insertion order in iteration
            // Useful when debugging the file format.
            val map = LinkedHashMap<String, Object>()

            if (hasUUID()) {
                readUUID(m_stream, map)
            }

            readRow(m_stream, map)

            SynchroLogger.log("READER", getClass(), map)

            rows.add(MapRow(map))
        }

        val tableTrailer = m_stream.readInt()
        if (tableTrailer != 0x6F99E416) {
            throw IllegalArgumentException("Unexpected file format")
        }

        postTrailer(m_stream)

        return this
    }

    /**
     * Overridden by child classes to indicate to the reader that the typical UUID structure
     * is not present for some types of table.
     *
     * @return true if the table starts with an expected UUID structure
     */
    protected open fun hasUUID(): Boolean {
        return true
    }

    /**
     * Read the optional row header and UUID.
     *
     * @param stream input stream
     * @param map row map
     */
    @Throws(IOException::class)
    protected open fun readUUID(stream: StreamReader, map: Map<String, Object>) {
        val unknown0Size = if (stream.majorVersion > 5) 8 else 16
        map.put("UNKNOWN0", stream.readBytes(unknown0Size))
        map.put("UUID", stream.readUUID())
    }

    /**
     * Allows additional behaviour once the main table data has been read.
     *
     * @param stream input stream
     */
    @SuppressWarnings("unused")
    @Throws(IOException::class)
    protected open fun postTrailer(stream: StreamReader) {
        // Default implementation
    }

    /**
     * Overridden by child classes to define their row magic number.
     *
     * @return row magic number
     */
    protected abstract fun rowMagicNumber(): Int

    /**
     * Overridden by child classes to extract data from a single row.
     *
     * @param stream input stream
     * @param map map to store data from row
     */
    @Throws(IOException::class)
    protected abstract fun readRow(stream: StreamReader, map: Map<String, Object>)
}
