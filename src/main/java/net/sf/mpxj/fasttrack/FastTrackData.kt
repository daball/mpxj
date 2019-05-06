/*
 * file:       FastTrackData.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software
 * date:       04/03/2017
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

import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.util.ArrayList
import java.util.EnumMap
import java.util.HashMap
import java.util.TreeSet

import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.CharsetHelper

/**
 * Read tables of data from a FastTrack file.
 */
internal class FastTrackData {

    /**
     * Retrieve the time units used for durations in this FastTrack file.
     *
     * @return TimeUnit instance
     */
    val durationTimeUnit: TimeUnit
        get() = if (m_durationTimeUnit == null) TimeUnit.DAYS else m_durationTimeUnit

    /**
     * Retrieve the time units used for work in this FastTrack file.
     *
     * @return TimeUnit instance
     */
    val workTimeUnit: TimeUnit
        get() = if (m_workTimeUnit == null) TimeUnit.HOURS else m_workTimeUnit

    private var m_buffer: ByteArray? = null
    private var m_logFile: String? = null
    private var m_log: PrintWriter? = null
    private val m_tables = EnumMap<FastTrackTableType, FastTrackTable>(FastTrackTableType::class.java)
    private var m_currentTable: FastTrackTable? = null
    private var m_currentColumn: FastTrackColumn? = null
    private val m_currentFields = TreeSet<FastTrackField>()
    private var m_durationTimeUnit: TimeUnit? = null
    private var m_workTimeUnit: TimeUnit? = null
    /**
     * Read a FastTrack file.
     *
     * @param file FastTrack file
     */
    @Throws(Exception::class)
    fun process(file: File) {
        openLogFile()

        var blockIndex = 0
        val length = file.length() as Int
        m_buffer = ByteArray(length)
        val `is` = FileInputStream(file)
        try {
            val bytesRead = `is`.read(m_buffer)
            if (bytesRead != length) {
                throw RuntimeException("Read count different")
            }
        } finally {
            `is`.close()
        }

        val blocks = ArrayList<Integer>()
        for (index in 64 until m_buffer!!.size - 11) {
            if (matchPattern(PARENT_BLOCK_PATTERNS, index)) {
                blocks.add(Integer.valueOf(index))
            }
        }

        var startIndex = 0
        for (endIndex in blocks) {
            val blockLength = endIndex - startIndex
            readBlock(blockIndex, startIndex, blockLength)
            startIndex = endIndex
            ++blockIndex
        }

        val blockLength = m_buffer!!.size - startIndex
        readBlock(blockIndex, startIndex, blockLength)

        closeLogFile()
    }

    /**
     * Retrieve a table of data.
     *
     * @param type table type
     * @return FastTrackTable instance
     */
    fun getTable(type: FastTrackTableType): FastTrackTable? {
        var result = m_tables.get(type)
        if (result == null) {
            result = EMPTY_TABLE
        }
        return result
    }

    /**
     * Read a block of data from the FastTrack file and determine if
     * it contains a table definition, or columns.
     *
     * @param blockIndex index of the current block
     * @param startIndex start index of the block in the file
     * @param blockLength block length
     */
    @Throws(Exception::class)
    private fun readBlock(blockIndex: Int, startIndex: Int, blockLength: Int) {
        logBlock(blockIndex, startIndex, blockLength)

        if (blockLength < 128) {
            readTableBlock(startIndex, blockLength)
        } else {
            readColumnBlock(startIndex, blockLength)
        }
    }

    /**
     * Read the name of a table and prepare to populate it with column data.
     *
     * @param startIndex start of the block
     * @param blockLength length of the block
     */
    private fun readTableBlock(startIndex: Int, blockLength: Int) {
        for (index in startIndex until startIndex + blockLength - 11) {
            if (matchPattern(TABLE_BLOCK_PATTERNS, index)) {
                var offset = index + 7
                val nameLength = FastTrackUtility.getInt(m_buffer, offset)
                offset += 4
                val name = String(m_buffer, offset, nameLength, CharsetHelper.UTF16LE).toUpperCase()
                val type = REQUIRED_TABLES.get(name)
                if (type != null) {
                    m_currentTable = FastTrackTable(type, this)
                    m_tables.put(type, m_currentTable)
                } else {
                    m_currentTable = null
                }
                m_currentFields.clear()
                break
            }
        }
    }

    /**
     * Read multiple columns from a block.
     *
     * @param startIndex start of the block
     * @param blockLength length of the block
     */
    @Throws(Exception::class)
    private fun readColumnBlock(startIndex: Int, blockLength: Int) {
        val endIndex = startIndex + blockLength
        val blocks = ArrayList<Integer>()
        for (index in startIndex until endIndex - 11) {
            if (matchChildBlock(index)) {
                val childBlockStart = index - 2
                blocks.add(Integer.valueOf(childBlockStart))
            }
        }
        blocks.add(Integer.valueOf(endIndex))

        var childBlockStart = -1
        for (childBlockEnd in blocks) {
            if (childBlockStart != -1) {
                val childblockLength = childBlockEnd - childBlockStart
                try {
                    readColumn(childBlockStart, childblockLength)
                } catch (ex: UnexpectedStructureException) {
                    logUnexpectedStructure()
                }

            }
            childBlockStart = childBlockEnd
        }
    }

    /**
     * Read data for a single column.
     *
     * @param startIndex block start
     * @param length block length
     */
    @Throws(Exception::class)
    private fun readColumn(startIndex: Int, length: Int) {
        if (m_currentTable != null) {
            val value = FastTrackUtility.getByte(m_buffer!!, startIndex)
            var klass: Class<*>? = COLUMN_MAP[value]
            if (klass == null) {
                klass = UnknownColumn::class.java
            }

            val column = klass!!.newInstance() as FastTrackColumn
            m_currentColumn = column

            logColumnData(startIndex, length)

            column.read(m_currentTable!!.type, m_buffer, startIndex, length)
            val type = column.type

            //
            // Don't try to add this data if:
            // 1. We don't know what type it is
            // 2. We have seen the type already
            //
            if (type != null && !m_currentFields.contains(type)) {
                m_currentFields.add(type)
                m_currentTable!!.addColumn(column)
                updateDurationTimeUnit(column)
                updateWorkTimeUnit(column)

                logColumn(column)
            }
        }
    }

    /**
     * Locate a feature in the file by match a byte pattern.
     *
     * @param patterns patterns to match
     * @param bufferIndex start index
     * @return true if the bytes at the position match a pattern
     */
    private fun matchPattern(patterns: Array<ByteArray>, bufferIndex: Int): Boolean {
        var match = false
        for (pattern in patterns) {
            var index = 0
            match = true
            for (b in pattern) {
                if (b != m_buffer!![bufferIndex + index]) {
                    match = false
                    break
                }
                ++index
            }
            if (match) {
                break
            }
        }
        return match
    }

    /**
     * Locate a child block by byte pattern and validate by
     * checking the length of the string we are expecting
     * to follow the pattern.
     *
     * @param bufferIndex start index
     * @return true if a child block starts at this point
     */
    private fun matchChildBlock(bufferIndex: Int): Boolean {
        //
        // Match the pattern we see at the start of the child block
        //
        var index = 0
        for (b in CHILD_BLOCK_PATTERN) {
            if (b != m_buffer!![bufferIndex + index]) {
                return false
            }
            ++index
        }

        //
        // The first step will produce false positives. To handle this, we should find
        // the name of the block next, and check to ensure that the length
        // of the name makes sense.
        //
        val nameLength = FastTrackUtility.getInt(m_buffer, bufferIndex + index)

        //      System.out.println("Name length: " + nameLength);
        //
        //      if (nameLength > 0 && nameLength < 100)
        //      {
        //         String name = new String(m_buffer, bufferIndex+index+4, nameLength, CharsetHelper.UTF16LE);
        //         System.out.println("Name: " + name);
        //      }

        return nameLength > 0 && nameLength < 100
    }

    /**
     * Update the default time unit for durations based on data read from the file.
     *
     * @param column column data
     */
    private fun updateDurationTimeUnit(column: FastTrackColumn) {
        if (m_durationTimeUnit == null && isDurationColumn(column)) {
            val value = (column as DurationColumn).timeUnitValue
            if (value != 1) {
                m_durationTimeUnit = FastTrackUtility.getTimeUnit(value)
            }
        }
    }

    /**
     * Update the default time unit for work based on data read from the file.
     *
     * @param column column data
     */
    private fun updateWorkTimeUnit(column: FastTrackColumn) {
        if (m_workTimeUnit == null && isWorkColumn(column)) {
            val value = (column as DurationColumn).timeUnitValue
            if (value != 1) {
                m_workTimeUnit = FastTrackUtility.getTimeUnit(value)
            }
        }
    }

    /**
     * Determines if this is a duration column.
     *
     * @param column column to test
     * @return true if this is a duration column
     */
    private fun isDurationColumn(column: FastTrackColumn): Boolean {
        return column is DurationColumn && column.name.indexOf("Duration") !== -1
    }

    /**
     * Determines if this is a work column.
     *
     * @param column column to test
     * @return true if this is a work column
     */
    private fun isWorkColumn(column: FastTrackColumn): Boolean {
        return column is DurationColumn && column.name.indexOf("Work") !== -1
    }

    /**
     * Provide the file path for rudimentary logging to support development.
     *
     * @param logFile full path to log file
     */
    fun setLogFile(logFile: String) {
        m_logFile = logFile
    }

    /**
     * Open the log file for writing.
     */
    @Throws(IOException::class)
    private fun openLogFile() {
        if (m_logFile != null) {
            m_log = PrintWriter(FileWriter(m_logFile))
        }
    }

    /**
     * Close the log file.
     */
    private fun closeLogFile() {
        if (m_logFile != null) {
            m_log!!.flush()
            m_log!!.close()
        }
    }

    /**
     * Log block data.
     *
     * @param blockIndex current block index
     * @param startIndex start index
     * @param blockLength length
     */
    private fun logBlock(blockIndex: Int, startIndex: Int, blockLength: Int) {
        if (m_log != null) {
            m_log!!.println("Block Index: $blockIndex")
            m_log!!.println("Length: " + blockLength + " (" + Integer.toHexString(blockLength) + ")")
            m_log!!.println()
            m_log!!.println(FastTrackUtility.hexdump(m_buffer, startIndex, blockLength, true, 16, ""))
            m_log!!.flush()
        }
    }

    /**
     * Log the data for a single column.
     *
     * @param startIndex offset into buffer
     * @param length length
     */
    private fun logColumnData(startIndex: Int, length: Int) {
        if (m_log != null) {
            m_log!!.println()
            m_log!!.println(FastTrackUtility.hexdump(m_buffer, startIndex, length, true, 16, ""))
            m_log!!.println()
            m_log!!.flush()
        }
    }

    /**
     * Log unexpected column structure.
     */
    private fun logUnexpectedStructure() {
        if (m_log != null) {
            m_log!!.println("ABORTED COLUMN - unexpected structure: " + m_currentColumn!!.getClass().getSimpleName() + " " + m_currentColumn!!.name)
        }
    }

    /**
     * Log column data.
     *
     * @param column column data
     */
    private fun logColumn(column: FastTrackColumn) {
        if (m_log != null) {
            m_log!!.println("TABLE: " + m_currentTable!!.type)
            m_log!!.println(column.toString())
            m_log!!.flush()
        }
    }

    companion object {

        private val PARENT_BLOCK_PATTERNS = arrayOf(byteArrayOf(0xFB.toByte(), 0x01, 0x02, 0x00, 0x02, 0x00, 0xFF.toByte(), 0xFF.toByte(), 0x00, 0x00, 0x00), byteArrayOf(0xFC.toByte(), 0x01, 0x02, 0x00, 0x02, 0x00, 0xFF.toByte(), 0xFF.toByte(), 0x00, 0x00, 0x00), byteArrayOf(0xFD.toByte(), 0x01, 0x02, 0x00, 0x02, 0x00, 0xFF.toByte(), 0xFF.toByte(), 0x00, 0x00, 0x00), byteArrayOf(0x00, 0x00, 0x02, 0x00, 0x02, 0x00, 0xFF.toByte(), 0xFF.toByte(), 0x00, 0x00, 0x00))

        private val CHILD_BLOCK_PATTERN = byteArrayOf(0x05, 0x00, 0x00, 0x00, 0x01, 0x00)

        private val TABLE_BLOCK_PATTERNS = arrayOf(byteArrayOf(0x00, 0x00, 0x00, 0x65, 0x00, 0x01, 0x00))

        private val COLUMN_MAP = arrayOfNulls<Class<*>>(256)

        init {
            COLUMN_MAP[0x6E] = DateColumn::class.java
            COLUMN_MAP[0x6F] = TimeColumn::class.java
            COLUMN_MAP[0x71] = DurationColumn::class.java
            COLUMN_MAP[0x46] = PercentColumn::class.java
            COLUMN_MAP[0x6C] = ShortColumn::class.java
            COLUMN_MAP[0x73] = ShortColumn::class.java
            COLUMN_MAP[0x6D] = IdentifierColumn::class.java
            COLUMN_MAP[0x70] = NumberColumn::class.java
            COLUMN_MAP[0x5C] = CalendarColumn::class.java
            COLUMN_MAP[0x4B] = IntegerColumn::class.java
            COLUMN_MAP[0x49] = AssignmentColumn::class.java
            COLUMN_MAP[0x59] = EnumColumn::class.java
            COLUMN_MAP[0x53] = BooleanColumn::class.java
            COLUMN_MAP[0x5b] = DoubleColumn::class.java
            COLUMN_MAP[0x4A] = DoubleColumn::class.java
            COLUMN_MAP[0x54] = DoubleColumn::class.java
            COLUMN_MAP[0x57] = RelationColumn::class.java
            COLUMN_MAP[0x58] = RelationColumn::class.java
            COLUMN_MAP[0x68] = StringColumn::class.java
            COLUMN_MAP[0x69] = StringColumn::class.java
        }

        private val REQUIRED_TABLES = HashMap<String, FastTrackTableType>()

        init {
            REQUIRED_TABLES.put("ACTBARS", FastTrackTableType.ACTBARS)
            REQUIRED_TABLES.put("ACTIVITIES", FastTrackTableType.ACTIVITIES)
            REQUIRED_TABLES.put("RESOURCES", FastTrackTableType.RESOURCES)
        }

        private val EMPTY_TABLE = FastTrackTable(null, null)
    }
}
