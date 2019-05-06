/*
 * file:       Table.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       12/01/2018
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

package net.sf.mpxj.turboproject

import java.io.IOException
import java.io.InputStream
import java.util.TreeMap

import net.sf.mpxj.common.StreamHelper

/**
 * This class represents a table read from a PEP file.
 * The class is responsible for breaking down the raw
 * data into individual byte arrays representing each
 * row. Subclasses can then process these rows
 * and create MapRow instances to represent the data.
 * Users of this table can either iterate through the
 * rows, or select individual rows by their primary key.
 */
internal open class Table : Iterable<MapRow> {

    private val m_rows = TreeMap<Integer, MapRow>()
    /**
     * {@inheritDoc}
     */
    @Override
    fun iterator(): Iterator<MapRow> {
        return m_rows.values().iterator()
    }

    /**
     * Reads the table data from an input stream and breaks
     * it down into rows.
     *
     * @param is input stream
     */
    @Throws(IOException::class)
    fun read(`is`: InputStream) {
        val headerBlock = ByteArray(20)
        `is`.read(headerBlock)

        val headerLength = PEPUtility.getShort(headerBlock, 8)
        val recordCount = PEPUtility.getInt(headerBlock, 10)
        val recordLength = PEPUtility.getInt(headerBlock, 16)
        StreamHelper.skip(`is`, (headerLength - headerBlock.size).toLong())

        val record = ByteArray(recordLength)
        for (recordIndex in 1..recordCount) {
            `is`.read(record)
            readRow(recordIndex, record)
        }
    }

    /**
     * Retrieve a row based on its primary key.
     *
     * @param uniqueID unique ID of the required row
     * @return MapRow instance, or null if the row is not found
     */
    fun find(uniqueID: Integer): MapRow {
        return m_rows.get(uniqueID)
    }

    /**
     * Implemented by subclasses to extract data from the
     * byte array representing a row.
     *
     * @param uniqueID the unique ID for this row
     * @param data row data as a byte array
     */
    protected open fun readRow(uniqueID: Int, data: ByteArray) {
        // Implemented by subclasses
    }

    /**
     * Adds a row to the internal storage, indexed by primary key.
     *
     * @param uniqueID unique ID of the row
     * @param map row data as a simpe map
     */
    protected fun addRow(uniqueID: Int, map: Map<String, Object>) {
        m_rows.put(Integer.valueOf(uniqueID), MapRow(map))
    }
}
