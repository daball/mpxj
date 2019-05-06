/*
 * file:       AstaDatabaseFileReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2016
 * date:       06/06/2016
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

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.text.ParseException
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.LinkedList
import java.util.Properties

import net.sf.mpxj.DayType
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.common.FileHelper
import net.sf.mpxj.common.InputStreamHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.ProjectReader

/**
 * This class provides a generic front end to read project data from
 * a SQLite-based Asta PP file.
 */
class AstaDatabaseFileReader : ProjectReader {
    private var m_reader: AstaReader? = null
    private var m_projectID = Integer.valueOf(1)
    /**
     * Retrieve the name of the schema containing the Primavera tables.
     *
     * @return schema name
     */
    /**
     * Set the name of the schema containing the Primavera tables.
     *
     * @param schema schema name.
     */
    var schema = ""
        set(schema) {
            var schema = schema
            if (schema.charAt(schema.length() - 1) !== '.') {
                schema = "$schema."
            }
            field = schema
        }
    private var m_connection: Connection? = null
    private var m_ps: PreparedStatement? = null
    private var m_rs: ResultSet? = null
    private val m_meta = HashMap<String, Integer>()
    private var m_projectListeners: List<ProjectListener>? = null
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
    override fun read(fileName: String?): ProjectFile {
        return read(File(fileName))
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(file: File): ProjectFile {
        try {
            val url = "jdbc:sqlite:" + file.getAbsolutePath()
            val props = Properties()
            props.setProperty("date_string_format", "yyyy-MM-dd HH:mm:ss")
            // Note that we use the JDBC driver class directly here.
            // This ensures that it is an explicit dependency of MPXJ
            // and will work as expected in .Net.
            m_connection = org.sqlite.JDBC.createConnection(url, props)
            m_projectID = Integer.valueOf(0)
            return read()
        } catch (ex: SQLException) {
            throw MPXJException("Failed to create connection", ex)
        } finally {
            if (m_connection != null) {
                try {
                    m_connection!!.close()
                } catch (ex: SQLException) {
                    // silently ignore exceptions when closing connection
                }

            }
        }
    }

    @Override
    @Throws(MPXJException::class)
    override fun read(inputStream: InputStream?): ProjectFile {
        var tempFile: File? = null

        try {
            tempFile = InputStreamHelper.writeStreamToTempFile(inputStream!!, "pp")
            return read(tempFile!!)
        } catch (ex: IOException) {
            throw MPXJException("Failed to read file", ex)
        } finally {
            FileHelper.deleteQuietly(tempFile)
        }
    }

    /**
     * Read a project from the current data source.
     *
     * @return ProjectFile instance
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    fun read(): ProjectFile {
        try {
            m_reader = AstaReader()
            val project = m_reader!!.project
            project.eventManager.addProjectListeners(m_projectListeners)

            processProjectProperties()
            processCalendars()
            processResources()
            processTasks()
            processPredecessors()
            processAssignments()

            m_reader = null

            return project
        } catch (ex: SQLException) {
            throw MPXJException(MPXJException.READ_ERROR, ex)
        } catch (ex: ParseException) {
            throw MPXJException(MPXJException.READ_ERROR, ex)
        }

    }

    /**
     * Select the project properties row from the database.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processProjectProperties() {
        val rows = getRows("select duration as durationhours, project_start as staru, project_end as ene, * from project_summary where projid=?", m_projectID)
        if (rows.isEmpty() === false) {
            m_reader!!.processProjectProperties(rows[0])
        }
    }

    /**
     * Process calendars.
     *
     * @throws SQLException
     * @throws ParseException
     */
    @Throws(SQLException::class, ParseException::class)
    private fun processCalendars() {
        var rows = getRows("select id as exceptionnid, * from exceptionn")
        val exceptionTypeMap = m_reader!!.createExceptionTypeMap(rows)

        rows = getRows("select id as work_patternid, name as namn, * from work_pattern")
        val workPatternMap = m_reader!!.createWorkPatternMap(rows)

        rows = getRows("select id, work_patterns from calendar")
        val workPatternAssignmentMap = createWorkPatternAssignmentMap(rows)

        rows = getRows("select id, exceptions from calendar")
        val exceptionAssignmentMap = createExceptionAssignmentMap(rows)

        rows = getRows("select id, shifts from work_pattern")
        val timeEntryMap = createTimeEntryMap(rows)

        rows = getRows("select id as calendarid, name as namk, * from calendar where projid=? order by id", m_projectID)
        for (row in rows) {
            m_reader!!.processCalendar(row, workPatternMap, workPatternAssignmentMap, exceptionAssignmentMap, timeEntryMap, exceptionTypeMap)
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
        val permanentRows = getRows("select id as permanent_resourceid, name as nase, calendar as calendav, * from permanent_resource where projid=? order by id", m_projectID)
        val consumableRows = getRows("select id as consumable_resourceid, name as nase, calendar as calendav, * from consumable_resource where projid=? order by id", m_projectID)
        m_reader!!.processResources(permanentRows, consumableRows)
    }

    /**
     * Process tasks.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processTasks() {
        val bars = getRows("select id as barid, bar_start as starv, bar_finish as enf, name as namh, * from bar where projid=?", m_projectID)

        val expandedTasks = getRows("select id as expanded_taskid, constraint_flag as constrainu, * from expanded_task where projid=?", m_projectID)
        val tasks = getRows("select id as taskid, given_duration as given_durationhours, actual_duration as actual_durationhours, overall_percent_complete as overall_percenv_complete, name as nare, calendar as calendau, linkable_start as starz, linkable_finish as enj, notes as notet, wbs as wbt, natural_order as naturao_order, constraint_flag as constrainu, * from task where projid=?", m_projectID)
        val milestones = getRows("select id as milestoneid, name as nare, calendar as calendau, wbs as wbt, natural_order as naturao_order, constraint_flag as constrainu, * from milestone where projid=?", m_projectID)

        m_reader!!.processTasks(bars, expandedTasks, tasks, milestones)
    }

    /**
     * Process predecessors.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processPredecessors() {
        val rows = getRows("select start_lag_time as start_lag_timehours, end_lag_time as end_lag_timehours, link_kind as typi, * from link where projid=? order by id", m_projectID)
        val completedSections = getRows("select id as task_completed_sectionid, * from task_completed_section where projid=? order by id", m_projectID)
        m_reader!!.processPredecessors(rows, completedSections)
    }

    /**
     * Process resource assignments.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processAssignments() {
        val permanentAssignments = getRows("select allocated_to as allocatee_to, player, percent_complete, effort as efforw, permanent_schedul_allocation.id as permanent_schedul_allocationid, linkable_start as starz, linkable_finish as enj, given_allocation, delay as delaahours from permanent_schedul_allocation inner join perm_resource_skill on permanent_schedul_allocation.allocation_of = perm_resource_skill.id where permanent_schedul_allocation.projid=? order by permanent_schedul_allocation.id", m_projectID)
        m_reader!!.processAssignments(permanentAssignments)
    }

    /**
     * Set the ID of the project to be read.
     *
     * @param projectID project ID
     */
    fun setProjectID(projectID: Int) {
        m_projectID = Integer.valueOf(projectID)
    }

    /**
     * Retrieve a number of rows matching the supplied query.
     *
     * @param sql query statement
     * @return result set
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun getRows(sql: String): List<Row> {
        val result = LinkedList<Row>()

        m_ps = m_connection!!.prepareStatement(sql)
        m_rs = m_ps!!.executeQuery()
        populateMetaData()
        while (m_rs!!.next()) {
            result.add(SqliteResultSetRow(m_rs, m_meta))
        }

        return result
    }

    /**
     * Retrieve a number of rows matching the supplied query
     * which takes a single parameter.
     *
     * @param sql query statement
     * @param var bind variable value
     * @return result set
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun getRows(sql: String, `var`: Integer): List<Row> {
        val result = LinkedList<Row>()

        m_ps = m_connection!!.prepareStatement(sql)
        m_ps!!.setInt(1, NumberHelper.getInt(`var`))
        m_rs = m_ps!!.executeQuery()
        populateMetaData()
        while (m_rs!!.next()) {
            result.add(SqliteResultSetRow(m_rs, m_meta))
        }

        return result
    }

    /**
     * Retrieves basic meta data from the result set.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun populateMetaData() {
        m_meta.clear()

        val meta = m_rs!!.getMetaData()
        val columnCount = meta.getColumnCount() + 1
        for (loop in 1 until columnCount) {
            val name = meta.getColumnName(loop)
            val type = Integer.valueOf(meta.getColumnType(loop))
            m_meta.put(name, type)
        }
    }

    /**
     * Create the work pattern assignment map.
     *
     * @param rows calendar rows
     * @return work pattern assignment map
     */
    @Throws(ParseException::class)
    private fun createWorkPatternAssignmentMap(rows: List<Row>): Map<Integer, List<Row>> {
        val map = HashMap<Integer, List<Row>>()
        for (row in rows) {
            val calendarID = row.getInteger("ID")
            val workPatterns = row.getString("WORK_PATTERNS")
            map.put(calendarID, createWorkPatternAssignmentRowList(workPatterns))
        }
        return map
    }

    /**
     * Extract a list of work pattern assignments.
     *
     * @param workPatterns string representation of work pattern assignments
     * @return list of work pattern assignment rows
     */
    @Throws(ParseException::class)
    private fun createWorkPatternAssignmentRowList(workPatterns: String): List<Row> {
        val list = ArrayList<Row>()
        val patterns = workPatterns.split(",|:")
        var index = 1
        while (index < patterns.size) {
            val workPattern = Integer.valueOf(patterns[index + 1])
            val startDate = DatatypeConverter.parseBasicTimestamp(patterns[index + 3])
            val endDate = DatatypeConverter.parseBasicTimestamp(patterns[index + 4])

            val map = HashMap<String, Object>()
            map.put("WORK_PATTERN", workPattern)
            map.put("START_DATE", startDate)
            map.put("END_DATE", endDate)

            list.add(MapRow(map))

            index += 5
        }

        return list
    }

    /**
     * Create the exception assignment map.
     *
     * @param rows calendar rows
     * @return exception assignment map
     */
    private fun createExceptionAssignmentMap(rows: List<Row>): Map<Integer, List<Row>> {
        val map = HashMap<Integer, List<Row>>()
        for (row in rows) {
            val calendarID = row.getInteger("ID")
            val exceptions = row.getString("EXCEPTIONS")
            map.put(calendarID, createExceptionAssignmentRowList(exceptions))
        }
        return map
    }

    /**
     * Extract a list of exception assignments.
     *
     * @param exceptionData string representation of exception assignments
     * @return list of exception assignment rows
     */
    private fun createExceptionAssignmentRowList(exceptionData: String): List<Row> {
        val list = ArrayList<Row>()
        val exceptions = exceptionData.split(",|:")
        var index = 1
        while (index < exceptions.size) {
            val startDate = DatatypeConverter.parseEpochTimestamp(exceptions[index + 0])
            val endDate = DatatypeConverter.parseEpochTimestamp(exceptions[index + 1])
            //Integer exceptionTypeID = Integer.valueOf(exceptions[index + 2]);

            val map = HashMap<String, Object>()
            map.put("STARU_DATE", startDate)
            map.put("ENE_DATE", endDate)

            list.add(MapRow(map))

            index += 3
        }

        return list
    }

    /**
     * Create the time entry map.
     *
     * @param rows work pattern rows
     * @return time entry map
     */
    @Throws(ParseException::class)
    private fun createTimeEntryMap(rows: List<Row>): Map<Integer, List<Row>> {
        val map = HashMap<Integer, List<Row>>()
        for (row in rows) {
            val workPatternID = row.getInteger("ID")
            val shifts = row.getString("SHIFTS")
            map.put(workPatternID, createTimeEntryRowList(shifts))
        }
        return map
    }

    /**
     * Extract a list of time entries.
     *
     * @param shiftData string representation of time entries
     * @return list of time entry rows
     */
    @Throws(ParseException::class)
    private fun createTimeEntryRowList(shiftData: String): List<Row> {
        val list = ArrayList<Row>()
        val shifts = shiftData.split(",|:")
        var index = 1
        while (index < shifts.size) {
            index += 2
            val entryCount = Integer.parseInt(shifts[index])
            index++

            for (entryIndex in 0 until entryCount) {
                val exceptionTypeID = Integer.valueOf(shifts[index + 0])
                val startTime = DatatypeConverter.parseBasicTime(shifts[index + 1])
                val endTime = DatatypeConverter.parseBasicTime(shifts[index + 2])

                val map = HashMap<String, Object>()
                map.put("START_TIME", startTime)
                map.put("END_TIME", endTime)
                map.put("EXCEPTIOP", exceptionTypeID)

                list.add(MapRow(map))

                index += 3
            }
        }

        return list
    }
}