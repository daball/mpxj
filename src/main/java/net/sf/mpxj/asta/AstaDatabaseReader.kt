/*
 * file:       AstaDatabaseReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       07/04/2011
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
import java.io.InputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.util.HashMap
import java.util.LinkedList

import javax.sql.DataSource

import net.sf.mpxj.DayType
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.ProjectReader

/**
 * This class provides a generic front end to read project data from
 * a database.
 */
class AstaDatabaseReader : ProjectReader {

    private var m_reader: AstaReader? = null
    private var m_projectID: Integer? = null
    /**
     * Retrieve the name of the schema containing the schedule tables.
     *
     * @return schema name
     */
    /**
     * Set the name of the schema containing the schedule tables.
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
    private var m_dataSource: DataSource? = null
    private var m_connection: Connection? = null
    private var m_allocatedConnection: Boolean = false
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

            val rows = getRows("select projid, short_name from project_summary")
            for (row in rows) {
                val id = row.getInteger("projid")
                val name = row.getString("short_name")
                result.put(id, name)
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
        } finally {
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
     * Select the project properties row from the database.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processProjectProperties() {
        val rows = getRows("select * from project_summary where projid=?", m_projectID)
        if (rows.isEmpty() === false) {
            m_reader!!.processProjectProperties(rows[0])
        }
    }

    /**
     * Process calendars.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processCalendars() {
        var rows = getRows("select * from exceptionn")
        val exceptionMap = m_reader!!.createExceptionTypeMap(rows)

        rows = getRows("select * from work_pattern")
        val workPatternMap = m_reader!!.createWorkPatternMap(rows)

        rows = getRows("select * from work_pattern_assignment")
        val workPatternAssignmentMap = m_reader!!.createWorkPatternAssignmentMap(rows)

        rows = getRows("select * from exception_assignment order by exception_assignmentid, ordf")
        val exceptionAssignmentMap = m_reader!!.createExceptionAssignmentMap(rows)

        rows = getRows("select * from time_entry order by time_entryid, ordf")
        val timeEntryMap = m_reader!!.createTimeEntryMap(rows)

        rows = getRows("select * from calendar where projid=? order by calendarid", m_projectID)
        for (row in rows) {
            m_reader!!.processCalendar(row, workPatternMap, workPatternAssignmentMap, exceptionAssignmentMap, timeEntryMap, exceptionMap)
        }

        //
        // In theory the code below can be used to establish parent-child relationships between
        // calendars, however the resulting calendars aren't assigned to tasks and resources correctly, so
        // I've left this out for the moment.
        //
        /*
            for (Row row : rows)
            {
               ProjectCalendar child = m_reader.getProject().getCalendarByUniqueID(row.getInteger("CALENDARID"));
               ProjectCalendar parent = m_reader.getProject().getCalendarByUniqueID(row.getInteger("CALENDAR"));
               if (child != null && parent != null)
               {
                  child.setParent(parent);
               }
            }
      */

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
        val permanentRows = getRows("select * from permanent_resource where projid=? order by permanent_resourceid", m_projectID)
        val consumableRows = getRows("select * from consumable_resource where projid=? order by consumable_resourceid", m_projectID)
        m_reader!!.processResources(permanentRows, consumableRows)
    }

    /**
     * Process tasks.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processTasks() {
        val bars = getRows("select * from bar where projid=?", m_projectID)
        val expandedTasks = getRows("select * from expanded_task where projid=?", m_projectID)
        val tasks = getRows("select * from task where projid=?", m_projectID)
        val milestones = getRows("select * from milestone where projid=?", m_projectID)
        m_reader!!.processTasks(bars, expandedTasks, tasks, milestones)
    }

    /**
     * Process predecessors.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processPredecessors() {
        val rows = getRows("select * from link where projid=? order by linkid", m_projectID)
        val completedSections = getRows("select * from task_completed_section where projid=?", m_projectID)
        m_reader!!.processPredecessors(rows, completedSections)
    }

    /**
     * Process resource assignments.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processAssignments() {
        val permanentAssignments = getRows("select * from permanent_schedul_allocation inner join perm_resource_skill on permanent_schedul_allocation.allocatiop_of = perm_resource_skill.perm_resource_skillid where permanent_schedul_allocation.projid=? order by permanent_schedul_allocation.permanent_schedul_allocationid", m_projectID)
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
     * Set the data source. A DataSource or a Connection can be supplied
     * to this class to allow connection to the database.
     *
     * @param dataSource data source
     */
    fun setDataSource(dataSource: DataSource) {
        m_dataSource = dataSource
    }

    /**
     * Sets the connection. A DataSource or a Connection can be supplied
     * to this class to allow connection to the database.
     *
     * @param connection database connection
     */
    fun setConnection(connection: Connection) {
        m_connection = connection
    }

    /**
     * This is a convenience method which reads the first project
     * from the named Asta MDB file using the JDBC-ODBC bridge driver.
     *
     * @param accessDatabaseFileName access database file name
     * @return ProjectFile instance
     * @throws MPXJException
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(accessDatabaseFileName: String): ProjectFile {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver")
            val url = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb);DBQ=$accessDatabaseFileName"
            m_connection = DriverManager.getConnection(url)
            m_projectID = Integer.valueOf(0)
            return read()
        } catch (ex: ClassNotFoundException) {
            throw MPXJException("Failed to load JDBC driver", ex)
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(file: File): ProjectFile {
        return read(file.getAbsolutePath())
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun read(inputStream: InputStream): ProjectFile {
        throw UnsupportedOperationException()
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
        allocateConnection()

        try {
            val result = LinkedList<Row>()

            m_ps = m_connection!!.prepareStatement(sql)
            m_rs = m_ps!!.executeQuery()
            populateMetaData()
            while (m_rs!!.next()) {
                result.add(MpdResultSetRow(m_rs, m_meta))
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
    private fun getRows(sql: String, `var`: Integer?): List<Row> {
        allocateConnection()

        try {
            val result = LinkedList<Row>()

            m_ps = m_connection!!.prepareStatement(sql)
            m_ps!!.setInt(1, NumberHelper.getInt(`var`))
            m_rs = m_ps!!.executeQuery()
            populateMetaData()
            while (m_rs!!.next()) {
                result.add(MpdResultSetRow(m_rs, m_meta))
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
}