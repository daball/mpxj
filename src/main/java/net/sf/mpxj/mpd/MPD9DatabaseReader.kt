/*
 * file:       MPD9DatabaseReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       2006-02-02
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

package net.sf.mpxj.mpd

import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList

import javax.sql.DataSource

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.SubProject
import net.sf.mpxj.Task
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.listener.ProjectListener

/**
 * This class reads project data from an MPD9 format database.
 */
class MPD9DatabaseReader : MPD9AbstractReader() {

    private var m_dataSource: DataSource? = null
    private var m_allocatedConnection: Boolean = false
    private var m_connection: Connection? = null
    private var m_ps: PreparedStatement? = null
    private var m_rs: ResultSet? = null
    private val m_meta = HashMap<String, Integer>()
    private var m_projectListeners: List<ProjectListener>? = null
    private var m_hasResourceBaselines: Boolean = false
    private var m_hasTaskBaselines: Boolean = false
    private var m_hasAssignmentBaselines: Boolean = false
    /**
     * Add a listener to receive events as a project is being read.
     *
     * @param listener ProjectListener instance
     */
    fun addProjectListener(listener: ProjectListener) {
        if (m_projectListeners == null) {
            m_projectListeners = LinkedList<ProjectListener>()
        }
        m_projectListeners!!.add(listener)
    }

    /**
     * Populates a Map instance representing the IDs and names of
     * projects available in the current database.
     *
     * @return Map instance containing ID and name pairs
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    fun listProjects(): Map<Integer, String> {
        try {
            val result = HashMap<Integer, String>()

            val rows = getRows("SELECT PROJ_ID, PROJ_NAME FROM MSP_PROJECTS")
            for (row in rows) {
                processProjectListItem(result, row)
            }

            return result
        } catch (ex: SQLException) {
            throw MPXJException(MPXJException.READ_ERROR, ex)
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
            m_project = ProjectFile()
            m_eventManager = m_project!!.eventManager

            val config = m_project!!.projectConfig
            config.autoTaskID = false
            config.autoTaskUniqueID = false
            config.autoResourceID = false
            config.autoResourceUniqueID = false
            config.autoOutlineLevel = false
            config.autoOutlineNumber = false
            config.autoWBS = false
            config.autoCalendarUniqueID = false
            config.autoAssignmentUniqueID = false

            m_project!!.projectProperties.fileApplication = "Microsoft"
            m_project!!.projectProperties.fileType = "MPD"

            m_project!!.eventManager.addProjectListeners(m_projectListeners)

            processProjectProperties()
            processCalendars()
            processResources()
            processResourceBaselines()
            processTasks()
            processTaskBaselines()
            processLinks()
            processAssignments()
            processAssignmentBaselines()
            processExtendedAttributes()
            processSubProjects()
            postProcessing()

            return m_project
        } catch (ex: SQLException) {
            throw MPXJException(MPXJException.READ_ERROR, ex)
        } finally {
            reset()

            if (m_allocatedConnection && m_connection != null) {
                try {
                    m_connection!!.close()
                } catch (ex: SQLException) {
                    // silently ignore errors on close
                }

                m_connection = null
            }
        }
    }

    /**
     * Select the project properties from the database.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processProjectProperties() {
        val rows = getRows("SELECT * FROM MSP_PROJECTS WHERE PROJ_ID=?", m_projectID)
        if (rows.isEmpty() === false) {
            processProjectProperties(rows[0])
        }
    }

    /**
     * Select calendar data from the database.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processCalendars() {
        for (row in getRows("SELECT * FROM MSP_CALENDARS WHERE PROJ_ID=?", m_projectID)) {
            processCalendar(row)
        }

        updateBaseCalendarNames()

        processCalendarData(m_project!!.calendars)
    }

    /**
     * Process calendar hours and exception data from the database.
     *
     * @param calendars all calendars for the project
     */
    @Throws(SQLException::class)
    private fun processCalendarData(calendars: List<ProjectCalendar>) {
        for (calendar in calendars) {
            processCalendarData(calendar, getRows("SELECT * FROM MSP_CALENDAR_DATA WHERE PROJ_ID=? AND CAL_UID=?", m_projectID, calendar.uniqueID))
        }
    }

    /**
     * Process the hours and exceptions for an individual calendar.
     *
     * @param calendar project calendar
     * @param calendarData hours and exception rows for this calendar
     */
    private fun processCalendarData(calendar: ProjectCalendar, calendarData: List<ResultSetRow>) {
        for (row in calendarData) {
            processCalendarData(calendar, row)
        }
    }

    /**
     * Process resources.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processResources() {
        for (row in getRows("SELECT * FROM MSP_RESOURCES WHERE PROJ_ID=?", m_projectID)) {
            processResource(row)
        }
    }

    /**
     * Process resource baseline values.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processResourceBaselines() {
        if (m_hasResourceBaselines) {
            for (row in getRows("SELECT * FROM MSP_RESOURCE_BASELINES WHERE PROJ_ID=?", m_projectID)) {
                processResourceBaseline(row)
            }
        }
    }

    /**
     * Process tasks.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processTasks() {
        for (row in getRows("SELECT * FROM MSP_TASKS WHERE PROJ_ID=?", m_projectID)) {
            processTask(row)
        }
    }

    /**
     * Process task baseline values.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processTaskBaselines() {
        if (m_hasTaskBaselines) {
            for (row in getRows("SELECT * FROM MSP_TASK_BASELINES WHERE PROJ_ID=?", m_projectID)) {
                processTaskBaseline(row)
            }
        }
    }

    /**
     * Process links.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processLinks() {
        for (row in getRows("SELECT * FROM MSP_LINKS WHERE PROJ_ID=?", m_projectID)) {
            processLink(row)
        }
    }

    /**
     * Process resource assignments.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processAssignments() {
        for (row in getRows("SELECT * FROM MSP_ASSIGNMENTS WHERE PROJ_ID=?", m_projectID)) {
            processAssignment(row)
        }
    }

    /**
     * Process resource assignment baseline values.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processAssignmentBaselines() {
        if (m_hasAssignmentBaselines) {
            for (row in getRows("SELECT * FROM MSP_ASSIGNMENT_BASELINES WHERE PROJ_ID=?", m_projectID)) {
                processAssignmentBaseline(row)
            }
        }
    }

    /**
     * This method reads the extended task and resource attributes.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processExtendedAttributes() {
        processTextFields()
        processNumberFields()
        processFlagFields()
        processDurationFields()
        processDateFields()
        processOutlineCodeFields()
    }

    /**
     * The only indication that a task is a SubProject is the contents
     * of the subproject file name field. We test these here then add a skeleton
     * subproject structure to match the way we do things with MPP files.
     */
    private fun processSubProjects() {
        var subprojectIndex = 1
        for (task in m_project!!.tasks) {
            val subProjectFileName = task.subprojectName
            if (subProjectFileName != null) {
                var fileName: String = subProjectFileName
                val offset = 0x01000000 + subprojectIndex * 0x00400000
                val index = subProjectFileName!!.lastIndexOf('\\')
                if (index != -1) {
                    fileName = subProjectFileName!!.substring(index + 1)
                }

                val sp = SubProject()
                sp.fileName = fileName
                sp.fullPath = subProjectFileName
                sp.uniqueIDOffset = Integer.valueOf(offset)
                sp.taskUniqueID = task.uniqueID
                task.subProject = sp

                ++subprojectIndex
            }
        }
    }

    /**
     * Reads text field extended attributes.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processTextFields() {
        for (row in getRows("SELECT * FROM MSP_TEXT_FIELDS WHERE PROJ_ID=?", m_projectID)) {
            processTextField(row)
        }
    }

    /**
     * Reads number field extended attributes.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processNumberFields() {
        for (row in getRows("SELECT * FROM MSP_NUMBER_FIELDS WHERE PROJ_ID=?", m_projectID)) {
            processNumberField(row)
        }
    }

    /**
     * Reads flag field extended attributes.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processFlagFields() {
        for (row in getRows("SELECT * FROM MSP_FLAG_FIELDS WHERE PROJ_ID=?", m_projectID)) {
            processFlagField(row)
        }
    }

    /**
     * Reads duration field extended attributes.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processDurationFields() {
        for (row in getRows("SELECT * FROM MSP_DURATION_FIELDS WHERE PROJ_ID=?", m_projectID)) {
            processDurationField(row)
        }
    }

    /**
     * Reads date field extended attributes.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processDateFields() {
        for (row in getRows("SELECT * FROM MSP_DATE_FIELDS WHERE PROJ_ID=?", m_projectID)) {
            processDateField(row)
        }
    }

    /**
     * Process outline code fields.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processOutlineCodeFields() {
        for (row in getRows("SELECT * FROM MSP_CODE_FIELDS WHERE PROJ_ID=?", m_projectID)) {
            processOutlineCodeFields(row)
        }
    }

    /**
     * Process a single outline code.
     *
     * @param parentRow outline code to task mapping table
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processOutlineCodeFields(parentRow: Row) {
        val entityID = parentRow.getInteger("CODE_REF_UID")
        val outlineCodeEntityID = parentRow.getInteger("CODE_UID")

        for (row in getRows("SELECT * FROM MSP_OUTLINE_CODES WHERE CODE_UID=?", outlineCodeEntityID)) {
            processOutlineCodeField(entityID, row)
        }
    }

    /**
     * Retrieve a number of rows matching the supplied query.
     *
     * @param sql query statement
     * @return result set
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun getRows(sql: String): List<ResultSetRow> {
        allocateConnection()

        try {
            val result = LinkedList<ResultSetRow>()

            m_ps = m_connection!!.prepareStatement(sql)
            m_rs = m_ps!!.executeQuery()
            populateMetaData()
            while (m_rs!!.next()) {
                result.add(ResultSetRow(m_rs, m_meta))
            }

            return result
        } finally {
            releaseConnection()
        }
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
    private fun getRows(sql: String, `var`: Integer): List<ResultSetRow> {
        allocateConnection()

        try {
            val result = LinkedList<ResultSetRow>()

            m_ps = m_connection!!.prepareStatement(sql)
            m_ps!!.setInt(1, NumberHelper.getInt(`var`))
            m_rs = m_ps!!.executeQuery()
            populateMetaData()
            while (m_rs!!.next()) {
                result.add(ResultSetRow(m_rs, m_meta))
            }

            return result
        } finally {
            releaseConnection()
        }
    }

    /**
     * Retrieve a number of rows matching the supplied query
     * which takes two parameters.
     *
     * @param sql query statement
     * @param var1 bind variable value
     * @param var2 bind variable value
     * @return result set
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun getRows(sql: String, var1: Integer, var2: Integer?): List<ResultSetRow> {
        allocateConnection()

        try {
            val result = LinkedList<ResultSetRow>()

            m_ps = m_connection!!.prepareStatement(sql)
            m_ps!!.setInt(1, NumberHelper.getInt(var1))
            m_ps!!.setInt(2, NumberHelper.getInt(var2))
            m_rs = m_ps!!.executeQuery()
            populateMetaData()
            while (m_rs!!.next()) {
                result.add(ResultSetRow(m_rs, m_meta))
            }

            return result
        } finally {
            releaseConnection()
        }
    }

    /**
     * Allocates a database connection.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun allocateConnection() {
        if (m_connection == null) {
            m_connection = m_dataSource!!.getConnection()
            m_allocatedConnection = true
            queryDatabaseMetaData()
        }
    }

    /**
     * Releases a database connection, and cleans up any resources
     * associated with that connection.
     */
    private fun releaseConnection() {
        if (m_rs != null) {
            try {
                m_rs!!.close()
            } catch (ex: SQLException) {
                // silently ignore errors on close
            }

            m_rs = null
        }

        if (m_ps != null) {
            try {
                m_ps!!.close()
            } catch (ex: SQLException) {
                // silently ignore errors on close
            }

            m_ps = null
        }
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
     * Sets the data source used to read the project data.
     *
     * @param dataSource data source
     */
    fun setDataSource(dataSource: DataSource) {
        m_dataSource = dataSource
    }

    /**
     * Sets the connection to be used to read the project data.
     *
     * @param connection database connection
     */
    fun setConnection(connection: Connection) {
        m_connection = connection
        queryDatabaseMetaData()
    }

    /**
     * Queries database meta data to check for the existence of
     * specific tables.
     */
    private fun queryDatabaseMetaData() {
        var rs: ResultSet? = null

        try {
            val tables = HashSet<String>()
            val dmd = m_connection!!.getMetaData()
            rs = dmd.getTables(null, null, null, null)
            while (rs!!.next()) {
                tables.add(rs!!.getString("TABLE_NAME"))
            }

            m_hasResourceBaselines = tables.contains("MSP_RESOURCE_BASELINES")
            m_hasTaskBaselines = tables.contains("MSP_TASK_BASELINES")
            m_hasAssignmentBaselines = tables.contains("MSP_ASSIGNMENT_BASELINES")
        } catch (ex: Exception) {
            // Ignore errors when reading meta data
        } finally {
            if (rs != null) {
                try {
                    rs!!.close()
                } catch (ex: SQLException) {
                    // Ignore errors when closing result set
                }

                rs = null
            }
        }
    }
}