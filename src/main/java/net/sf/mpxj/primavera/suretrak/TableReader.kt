/*
 * file:       TableReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       01/03/2018
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

package net.sf.mpxj.primavera.suretrak

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.HashMap

import net.sf.mpxj.common.StreamHelper
import net.sf.mpxj.primavera.common.ColumnDefinition
import net.sf.mpxj.primavera.common.Table
import net.sf.mpxj.primavera.common.TableDefinition

/**
 * Handles reading a table from a SureTrak file.
 */
internal class TableReader
/**
 * Constructor.
 *
 * @param definition table structure definition
 */
(private val m_definition: TableDefinition) {

    /**
     * Read the table from the file and populate the supplied Table instance.
     *
     * @param file database file
     * @param table Table instance
     */
    @Throws(IOException::class)
    fun read(file: File, table: Table) {
        //System.out.println("Reading " + file.getName());
        var `is`: InputStream? = null
        try {
            `is` = FileInputStream(file)
            read(`is`!!, table)
        } finally {
            StreamHelper.closeQuietly(`is`)
        }
    }

    /**
     * Read the table from an input stream and populate the supplied Table instance.
     *
     * @param is input stream from table file
     * @param table Table instance
     */
    @Throws(IOException::class)
    private fun read(`is`: InputStream, table: Table) {
        val headerBytes = ByteArray(6)
        `is`.read(headerBytes)

        val recordCountBytes = ByteArray(2)
        `is`.read(recordCountBytes)
        //int recordCount = getShort(recordCountBytes, 0);
        //System.out.println("Header: " + new String(headerBytes) + " Record count:" + recordCount);

        val buffer = ByteArray(m_definition.recordSize)
        while (true) {
            val bytesRead = `is`.read(buffer)
            if (bytesRead == -1) {
                break
            }

            if (bytesRead != buffer.size) {
                throw IOException("Unexpected end of file")
            }

            if (buffer[0].toInt() == 0) {
                readRecord(buffer, table)
            }
        }
    }

    /**
     * Reads a single record from the table.
     *
     * @param buffer record data
     * @param table parent table
     */
    private fun readRecord(buffer: ByteArray, table: Table) {
        //System.out.println(ByteArrayHelper.hexdump(buffer, true, 16, ""));
        val deletedFlag = getShort(buffer, 0)
        if (deletedFlag != 0) {
            val row = HashMap<String, Object>()
            for (column in m_definition.columns) {
                val value = column.read(0, buffer)
                //System.out.println(column.getName() + ": " + value);
                row.put(column.name, value)
            }

            table.addRow(m_definition.primaryKeyColumnName, row)
        }
    }

    /**
     * Read a two byte integer from a byte array.
     *
     * @param data byte array
     * @param offset offset into byte array
     * @return int value
     */
    private fun getShort(data: ByteArray, offset: Int): Int {
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
}
