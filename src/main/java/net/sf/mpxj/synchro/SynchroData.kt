/*
 * file:       SynchroData.java
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

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Comparator
import java.util.HashMap
import java.util.HashSet
import java.util.zip.DataFormatException
import java.util.zip.Inflater

import net.sf.mpxj.common.StreamHelper

/**
 * Reads the raw table data from an S file, ready to be processed.
 * Note that we only extract data for the tables we're going to read.
 */
internal class SynchroData {

    private var m_majorVersion: Int = 0
    private var m_offset: Int = 0
    private val m_tableData = HashMap<String, ByteArray>()
    /**
     * Extract raw table data from the input stream.
     *
     * @param is input stream
     */
    @Throws(Exception::class)
    fun process(`is`: InputStream) {
        readHeader(`is`)
        readVersion(`is`)
        readTableData(readTableHeaders(`is`), `is`)
    }

    /**
     * Return an input stream to read the data from the named table.
     *
     * @param name table name
     * @return InputStream instance
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getTableData(name: String): StreamReader {
        val stream = ByteArrayInputStream(m_tableData.get(name))
        if (m_majorVersion > 5) {
            val header = ByteArray(24)
            stream.read(header)
            SynchroLogger.log("TABLE HEADER", header)
        }
        return StreamReader(m_majorVersion, stream)
    }

    /**
     * Read the table headers. This allows us to break the file into chunks
     * representing the individual tables.
     *
     * @param is input stream
     * @return list of tables in the file
     */
    @Throws(IOException::class)
    private fun readTableHeaders(`is`: InputStream): List<SynchroTable> {
        // Read the headers
        val tables = ArrayList<SynchroTable>()
        val header = ByteArray(48)
        while (true) {
            `is`.read(header)
            m_offset += 48
            val table = readTableHeader(header) ?: break
            tables.add(table)
        }

        // Ensure sorted by offset
        Collections.sort(tables, object : Comparator<SynchroTable>() {
            @Override
            fun compare(o1: SynchroTable, o2: SynchroTable): Int {
                return o1.offset - o2.offset
            }
        })

        // Calculate lengths
        var previousTable: SynchroTable? = null
        for (table in tables) {
            if (previousTable != null) {
                previousTable.length = table.offset - previousTable.offset
            }

            previousTable = table
        }

        for (table in tables) {
            SynchroLogger.log("TABLE", table)
        }

        return tables
    }

    /**
     * Read the header data for a single file.
     *
     * @param header header data
     * @return SynchroTable instance
     */
    private fun readTableHeader(header: ByteArray): SynchroTable? {
        var result: SynchroTable? = null
        val tableName = DatatypeConverter.getSimpleString(header, 0)
        if (!tableName.isEmpty()) {
            val offset = DatatypeConverter.getInt(header, 40)
            result = SynchroTable(tableName, offset)
        }
        return result
    }

    /**
     * Read the data for all of the tables we're interested in.
     *
     * @param tables list of all available tables
     * @param is input stream
     */
    @Throws(IOException::class)
    private fun readTableData(tables: List<SynchroTable>, `is`: InputStream) {
        for (table in tables) {
            if (REQUIRED_TABLES.contains(table.name)) {
                readTable(`is`, table)
            }
        }
    }

    /**
     * Read data for a single table and store it.
     *
     * @param is input stream
     * @param table table header
     */
    @Throws(IOException::class)
    private fun readTable(`is`: InputStream, table: SynchroTable) {
        val skip = table.offset - m_offset
        if (skip != 0) {
            StreamHelper.skip(`is`, skip.toLong())
            m_offset += skip
        }

        val tableName = DatatypeConverter.getString(`is`)
        val tableNameLength = 2 + tableName!!.length()
        m_offset += tableNameLength

        val dataLength: Int
        if (table.length == -1) {
            dataLength = `is`.available()
        } else {
            dataLength = table.length - tableNameLength
        }

        SynchroLogger.log("READ", tableName)

        val compressedTableData = ByteArray(dataLength)
        `is`.read(compressedTableData)
        m_offset += dataLength

        val inflater = Inflater()
        inflater.setInput(compressedTableData)
        val outputStream = ByteArrayOutputStream(compressedTableData.size)
        val buffer = ByteArray(1024)
        while (!inflater.finished()) {
            val count: Int

            try {
                count = inflater.inflate(buffer)
            } catch (ex: DataFormatException) {
                throw IOException(ex)
            }

            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        val uncompressedTableData = outputStream.toByteArray()

        SynchroLogger.log(uncompressedTableData)

        m_tableData.put(table.name, uncompressedTableData)
    }

    /**
     * Read the file header data.
     *
     * @param is input stream
     */
    @Throws(IOException::class)
    private fun readHeader(`is`: InputStream) {
        val header = ByteArray(20)
        `is`.read(header)
        m_offset += 20
        SynchroLogger.log("HEADER", header)
    }

    /**
     * Read the version number.
     *
     * @param is input stream
     */
    @Throws(IOException::class)
    private fun readVersion(`is`: InputStream) {
        val bytesReadStream = BytesReadInputStream(`is`)
        val version = DatatypeConverter.getString(bytesReadStream)
        m_offset += bytesReadStream.bytesRead
        SynchroLogger.log("VERSION", version)

        val versionArray = version!!.split("\\.")
        m_majorVersion = Integer.parseInt(versionArray[0])
    }

    companion object {
        private val REQUIRED_TABLES = HashSet<String>(Arrays.asList("Tasks", "Calendars", "Companies"))
    }
}
