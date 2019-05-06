/*
 * file:       AstaTextFileReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2012
 * date:       23/04/2012
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

package net.sf.mpxj.asta

import java.io.InputStream
import java.io.InputStreamReader
import java.sql.SQLException
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.LinkedList
import kotlin.collections.Map.Entry

import net.sf.mpxj.DayType
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.common.CharsetHelper
import net.sf.mpxj.common.ReaderTokenizer
import net.sf.mpxj.common.Tokenizer
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * This class provides a generic front end to read project data from
 * a text-based Asta PP file.
 */
internal class AstaTextFileReader : AbstractProjectReader() {

    private var m_reader: AstaReader? = null
    private var m_projectListeners: List<ProjectListener>? = null
    private var m_tables: Map<String, List<Row>>? = null
    private var m_tableDefinitions: Map<Integer, TableDefinition>? = null
    private var m_epochDateFormat: Boolean = false
    /**
     * {@inheritDoc}
     */
    @Override
    override fun addProjectListener(listener: ProjectListener) {
        if (m_projectListeners == null) {
            m_projectListeners = LinkedList<ProjectListener>()
        }
        m_projectListeners!!.add(listener)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(inputStream: InputStream): ProjectFile {
        try {
            m_reader = AstaReader()
            val project = m_reader!!.project
            project.eventManager.addProjectListeners(m_projectListeners)

            m_tables = HashMap<String, List<Row>>()

            processFile(inputStream)

            processProjectProperties()
            processCalendars()
            processResources()
            processTasks()
            processPredecessors()
            processAssignments()

            return project
        } catch (ex: SQLException) {
            throw MPXJException(MPXJException.READ_ERROR, ex)
        } finally {
            m_reader = null
        }
    }

    /**
     * Tokenizes the input file and extracts the required data.
     *
     * @param is input stream
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun processFile(`is`: InputStream) {
        try {
            val reader = InputStreamReader(`is`, CharsetHelper.UTF8)
            val tk = object : ReaderTokenizer(reader) {
                @Override
                override fun startQuotedIsValid(buffer: StringBuilder): Boolean {
                    return buffer.length() === 1 && buffer.charAt(0) === '<'
                }
            }

            tk.setDelimiter(DELIMITER)
            val columns = ArrayList<String>()
            var nextTokenPrefix: String? = null

            while (tk.type != Tokenizer.TT_EOF) {
                columns.clear()
                var table: TableDefinition? = null

                while (tk.nextToken() == Tokenizer.TT_WORD) {
                    var token: String? = tk.token
                    if (columns.size() === 0) {
                        if (token!!.charAt(0) === '#') {
                            val index = token!!.lastIndexOf(':')
                            if (index != -1) {
                                val headerToken: String
                                if (token.endsWith("-") || token.endsWith("=")) {
                                    headerToken = token
                                    token = null
                                } else {
                                    headerToken = token.substring(0, index)
                                    token = token.substring(index + 1)
                                }

                                val header = RowHeader(headerToken)
                                table = m_tableDefinitions!![header.type]
                                columns.add(header.id)
                            }
                        } else {
                            if (token!!.charAt(0) === 0) {
                                processFileType(token!!)
                            }
                        }
                    }

                    if (table != null && token != null) {
                        if (token.startsWith("<\"") && !token.endsWith("\">")) {
                            nextTokenPrefix = token
                        } else {
                            if (nextTokenPrefix != null) {
                                token = nextTokenPrefix.toInt() + DELIMITER.toInt() + token.toInt()
                                nextTokenPrefix = null
                            }

                            columns.add(token)
                        }
                    }
                }

                if (table != null && columns.size() > 1) {
                    //               System.out.println(table.getName() + " " + columns.size());
                    //               ColumnDefinition[] columnDefs = table.getColumns();
                    //               int unknownIndex = 1;
                    //               for (int xx = 0; xx < columns.size(); xx++)
                    //               {
                    //                  String x = columns.get(xx);
                    //                  String columnName = xx < columnDefs.length ? (columnDefs[xx] == null ? "UNKNOWN" + (unknownIndex++) : columnDefs[xx].getName()) : "?";
                    //                  System.out.println(columnName + ": " + x + ", ");
                    //               }
                    //               System.out.println();

                    val row = TextFileRow(table, columns, m_epochDateFormat)
                    var rows = m_tables!!.get(table.name)
                    if (rows == null) {
                        rows = LinkedList<Row>()
                        m_tables!!.put(table.name, rows)
                    }
                    rows!!.add(row)
                }
            }
        } catch (ex: Exception) {
            throw MPXJException(MPXJException.READ_ERROR, ex)
        }

    }

    /**
     * Reads the file version and configures the expected file format.
     *
     * @param token token containing the file version
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun processFileType(token: String) {
        val version = token.substring(2).split(" ")[0]
        //System.out.println(version);
        val fileFormatClass = FILE_VERSION_MAP.get(Integer.valueOf(version))
                ?: throw MPXJException("Unsupported PP file format version $version")

        try {
            val format = fileFormatClass.newInstance()
            m_tableDefinitions = format.tableDefinitions()
            m_epochDateFormat = format.epochDateFormat()
        } catch (ex: Exception) {
            throw MPXJException("Failed to configure file format", ex)
        }

    }

    /**
     * Select the project properties row from the database.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processProjectProperties() {
        val rows = getTable("PROJECT_SUMMARY")
        if (rows!!.isEmpty() === false) {
            m_reader!!.processProjectProperties(rows!![0])
        }
    }

    /**
     * Extract calendar data from the file.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processCalendars() {
        var rows = getTable("EXCEPTIONN")
        val exceptionMap = m_reader!!.createExceptionTypeMap(rows!!)

        rows = getTable("WORK_PATTERN")
        val workPatternMap = m_reader!!.createWorkPatternMap(rows!!)

        rows = LinkedList<Row>()// getTable("WORK_PATTERN_ASSIGNMENT"); // Need to generate an example
        val workPatternAssignmentMap = m_reader!!.createWorkPatternAssignmentMap(rows)

        rows = getTable("EXCEPTION_ASSIGNMENT")
        val exceptionAssignmentMap = m_reader!!.createExceptionAssignmentMap(rows!!)

        rows = getTable("TIME_ENTRY")
        val timeEntryMap = m_reader!!.createTimeEntryMap(rows!!)

        rows = getTable("CALENDAR")
        Collections.sort(rows, CALENDAR_COMPARATOR)
        for (row in rows!!) {
            m_reader!!.processCalendar(row, workPatternMap, workPatternAssignmentMap, exceptionAssignmentMap, timeEntryMap, exceptionMap)
        }

        //
        // Update unique counters at this point as we will be generating
        // resource calendars, and will need to auto generate IDs
        //
        m_reader!!.project.projectConfig.updateUniqueCounters()
    }

    /**
     * Process resources.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processResources() {
        val permanentRows = getTable("PERMANENT_RESOURCE")
        val consumableRows = getTable("CONSUMABLE_RESOURCE")

        Collections.sort(permanentRows, PERMANENT_RESOURCE_COMPARATOR)
        Collections.sort(consumableRows, CONSUMABLE_RESOURCE_COMPARATOR)

        m_reader!!.processResources(permanentRows!!, consumableRows)
    }

    /**
     * Process tasks.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processTasks() {
        val bars = getTable("BAR")
        val expandedTasks = getTable("EXPANDED_TASK")
        val tasks = getTable("TASK")
        val milestones = getTable("MILESTONE")

        m_reader!!.processTasks(bars, expandedTasks, tasks, milestones)
    }

    /**
     * Process predecessors.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processPredecessors() {
        val rows = getTable("LINK")
        val completedSections = getTable("TASK_COMPLETED_SECTION")
        Collections.sort(rows, LINK_COMPARATOR)
        m_reader!!.processPredecessors(rows, completedSections!!)
    }

    /**
     * Process resource assignments.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processAssignments() {
        val allocationRows = getTable("PERMANENT_SCHEDUL_ALLOCATION")
        val skillRows = getTable("PERM_RESOURCE_SKILL")
        val permanentAssignments = join(allocationRows, "ALLOCATIOP_OF", "PERM_RESOURCE_SKILL", skillRows, "PERM_RESOURCE_SKILLID")
        Collections.sort(permanentAssignments, ALLOCATION_COMPARATOR)
        m_reader!!.processAssignments(permanentAssignments)
    }

    /**
     * Very basic implementation of an inner join between two result sets.
     *
     * @param leftRows left result set
     * @param leftColumn left foreign key column
     * @param rightTable right table name
     * @param rightRows right result set
     * @param rightColumn right primary key column
     * @return joined result set
     */
    private fun join(leftRows: List<Row>?, leftColumn: String, rightTable: String, rightRows: List<Row>?, rightColumn: String): List<Row> {
        val result = LinkedList<Row>()

        val leftComparator = RowComparator(*arrayOf<String>(leftColumn))
        val rightComparator = RowComparator(*arrayOf<String>(rightColumn))
        Collections.sort(leftRows, leftComparator)
        Collections.sort(rightRows, rightComparator)

        val rightIterator = rightRows!!.listIterator()
        var rightRow = if (rightIterator.hasNext()) rightIterator.next() else null

        for (leftRow in leftRows!!) {
            val leftValue = leftRow.getInteger(leftColumn)
            var match = false

            while (rightRow != null) {
                val rightValue = rightRow.getInteger(rightColumn)
                val comparison = leftValue.compareTo(rightValue)
                if (comparison == 0) {
                    match = true
                    break
                }

                if (comparison < 0) {
                    if (rightIterator.hasPrevious()) {
                        rightRow = rightIterator.previous()
                    }
                    break
                }

                rightRow = rightIterator.next()
            }

            if (match && rightRow != null) {
                val newMap = HashMap<String, Object>((leftRow as MapRow).map)

                for (entry in (rightRow as MapRow).map.entrySet()) {
                    var key = entry.getKey()
                    if (newMap.containsKey(key)) {
                        key = "$rightTable.$key"
                    }
                    newMap.put(key, entry.getValue())
                }

                result.add(MapRow(newMap))
            }
        }

        return result
    }

    /**
     * Retrieve table data, return an empty result set if no table data is present.
     *
     * @param name table name
     * @return table data
     */
    private fun getTable(name: String): List<Row>? {
        var result = m_tables!![name]
        if (result == null) {
            result = Collections.emptyList()
        }
        return result
    }

    companion object {

        private val DELIMITER = ','

        private val CALENDAR_COMPARATOR = RowComparator("CALENDARID")
        private val PERMANENT_RESOURCE_COMPARATOR = RowComparator("PERMANENT_RESOURCEID")
        private val CONSUMABLE_RESOURCE_COMPARATOR = RowComparator("CONSUMABLE_RESOURCEID")
        private val LINK_COMPARATOR = RowComparator("LINKID")
        private val ALLOCATION_COMPARATOR = RowComparator("PERMANENT_SCHEDUL_ALLOCATIONID")

        private val FILE_VERSION_MAP = HashMap<Integer, Class<out AbstractFileFormat>>()

        init {
            FILE_VERSION_MAP.put(Integer.valueOf(8020), FileFormat8020::class.java) // EasyProject 2
            FILE_VERSION_MAP.put(Integer.valueOf(9006), FileFormat9006::class.java) // EasyProject 3
            FILE_VERSION_MAP.put(Integer.valueOf(10008), FileFormat10008::class.java) // EasyProject 4
            FILE_VERSION_MAP.put(Integer.valueOf(11004), FileFormat11004::class.java) // EasyProject 5 and PowerProject 11
            FILE_VERSION_MAP.put(Integer.valueOf(12002), FileFormat12002::class.java) // PowerProject 12.0.0.2
            FILE_VERSION_MAP.put(Integer.valueOf(12005), FileFormat12005::class.java) // PowerProject 12
            FILE_VERSION_MAP.put(Integer.valueOf(13001), FileFormat13001::class.java) // PowerProject 13
            FILE_VERSION_MAP.put(Integer.valueOf(13004), FileFormat13004::class.java) // PowerProject 13
        }
    }
}