/*
 * file:       PrimaveraDatabaseReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       22/03/2010
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

package net.sf.mpxj.primavera

import java.io.File
import java.io.InputStream
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedList

import javax.sql.DataSource

import net.sf.mpxj.Day
import net.sf.mpxj.FieldType
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.ProjectReader

/**
 * This class provides a generic front end to read project data from
 * a database.
 */
class PrimaveraDatabaseReader : ProjectReader {

    private var m_reader: PrimaveraReader? = null
    private var m_projectID: Integer? = null
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
    var schema: String? = ""
        set(schema) {
            var schema = schema
            if (schema == null) {
                schema = ""
            } else {
                if (!schema.isEmpty() && !schema.endsWith(".")) {
                    schema = "$schema."
                }
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
    private val m_taskUdfCounters = UserFieldCounters()
    private val m_resourceUdfCounters = UserFieldCounters()
    private val m_assignmentUdfCounters = UserFieldCounters()
    /**
     * If set to true, the WBS for each task read from Primavera will exactly match the WBS value shown in Primavera.
     * If set to false, each task will be given a unique WBS based on the WBS present in Primavera.
     * Defaults to true.
     *
     * @return flag value
     */
    /**
     * If set to true, the WBS for each task read from Primavera will exactly match the WBS value shown in Primavera.
     * If set to false, each task will be given a unique WBS based on the WBS present in Primavera.
     * Defaults to true.
     *
     * @param matchPrimaveraWBS flag value
     */
    var matchPrimaveraWBS = true

    /**
     * Customise the data retrieved by this reader by modifying the contents of this map.
     *
     * @return Primavera field name to MPXJ field type map
     */
    val resourceFieldMap = PrimaveraReader.defaultResourceFieldMap
    /**
     * Customise the data retrieved by this reader by modifying the contents of this map.
     *
     * @return Primavera field name to MPXJ field type map
     */
    val wbsFieldMap = PrimaveraReader.defaultWbsFieldMap
    /**
     * Customise the data retrieved by this reader by modifying the contents of this map.
     *
     * @return Primavera field name to MPXJ field type map
     */
    val taskFieldMap = PrimaveraReader.defaultTaskFieldMap
    /**
     * Customise the data retrieved by this reader by modifying the contents of this map.
     *
     * @return Primavera field name to MPXJ field type map
     */
    val assignmentFields = PrimaveraReader.defaultAssignmentFieldMap
    /**
     * Customise the MPXJ field name aliases applied by this reader by modifying the contents of this map.
     *
     * @return Primavera field name to MPXJ field type map
     */
    val aliases = PrimaveraReader.defaultAliases

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

            val rows = getRows("select proj_id, proj_short_name from " + schema + "project where delete_date is null")
            for (row in rows) {
                val id = row.getInteger("proj_id")
                val name = row.getString("proj_short_name")
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
            m_reader = PrimaveraReader(m_taskUdfCounters, m_resourceUdfCounters, m_assignmentUdfCounters, resourceFieldMap, wbsFieldMap, taskFieldMap, assignmentFields, aliases, matchPrimaveraWBS)
            val project = m_reader!!.project
            project.eventManager.addProjectListeners(m_projectListeners)

            processAnalytics()
            processProjectProperties()
            processActivityCodes()
            processUserDefinedFields()
            processCalendars()
            processResources()
            processResourceRates()
            processTasks()
            processPredecessors()
            processAssignments()

            m_reader = null
            project.updateStructure()

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
     * Convenience method which allows all projects in the database to
     * be read in a single operation.
     *
     * @return list of ProjectFile instances
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    fun readAll(): List<ProjectFile> {
        val projects = listProjects()
        val result = ArrayList<ProjectFile>(projects.keySet().size())
        for (id in projects.keySet()) {
            setProjectID(id.intValue())
            result.add(read())
        }
        return result
    }

    /**
     * Populate data for analytics.
     */
    @Throws(SQLException::class)
    private fun processAnalytics() {
        allocateConnection()

        try {
            val meta = m_connection!!.getMetaData()
            var productName = meta.getDatabaseProductName()
            if (productName == null || productName!!.isEmpty()) {
                productName = "DATABASE"
            } else {
                productName = productName!!.toUpperCase()
            }

            val properties = m_reader!!.project.projectProperties
            properties.fileApplication = "Primavera"
            properties.fileType = productName
        } finally {
            releaseConnection()
        }
    }

    /**
     * Select the project properties from the database.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processProjectProperties() {
        //
        // Process common attributes
        //
        var rows = getRows("select * from " + schema + "project where proj_id=?", m_projectID)
        m_reader!!.processProjectProperties(rows, m_projectID)

        //
        // Process PMDB-specific attributes
        //
        rows = getRows("select * from " + schema + "prefer where prefer.delete_date is null")
        if (!rows.isEmpty()) {
            val row = rows[0]
            val ph = m_reader!!.project.projectProperties
            ph.creationDate = row.getDate("create_date")
            ph.lastSaved = row.getDate("update_date")
            ph.minutesPerDay = Double.valueOf(row.getDouble("day_hr_cnt").doubleValue() * 60)
            ph.minutesPerWeek = Double.valueOf(row.getDouble("week_hr_cnt").doubleValue() * 60)
            ph.weekStartDay = Day.getInstance(row.getInt("week_start_day_num"))

            processDefaultCurrency(row.getInteger("curr_id"))
        }

        processSchedulingProjectProperties()
    }

    /**
     * Process activity code data.
     */
    @Throws(SQLException::class)
    private fun processActivityCodes() {
        val types = getRows("select * from " + schema + "actvtype where actv_code_type_id in (select distinct actv_code_type_id from taskactv where proj_id=?)", m_projectID)
        val typeValues = getRows("select * from " + schema + "actvcode where actv_code_id in (select distinct actv_code_id from taskactv where proj_id=?)", m_projectID)
        val assignments = getRows("select * from " + schema + "taskactv where proj_id=?", m_projectID)
        m_reader!!.processActivityCodes(types, typeValues, assignments)
    }

    /**
     * Process user defined fields.
     */
    @Throws(SQLException::class)
    private fun processUserDefinedFields() {
        val fields = getRows("select * from " + schema + "udftype")
        val values = getRows("select * from " + schema + "udfvalue where proj_id=? or proj_id is null", m_projectID)
        m_reader!!.processUserDefinedFields(fields, values)
    }

    /**
     * Process the scheduling project property from PROJPROP. This table only seems to exist
     * in P6 databases, not XER files.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processSchedulingProjectProperties() {
        val rows = getRows("select * from " + schema + "projprop where proj_id=? and prop_name='scheduling'", m_projectID)
        if (!rows.isEmpty()) {
            val row = rows[0]
            val record = Record.getRecord(row.getString("prop_value"))
            if (record != null) {
                val keyValues = record.value!!.split("\\|")
                for (i in 0 until keyValues.size - 1) {
                    if ("sched_calendar_on_relationship_lag".equals(keyValues[i])) {
                        val customProperties = HashMap<String, Object>()
                        customProperties.put("LagCalendar", keyValues[i + 1])
                        m_reader!!.project.projectProperties.customProperties = customProperties
                        break
                    }
                }
            }
        }
    }

    /**
     * Select the default currency properties from the database.
     *
     * @param currencyID default currency ID
     */
    @Throws(SQLException::class)
    private fun processDefaultCurrency(currencyID: Integer) {
        val rows = getRows("select * from " + schema + "currtype where curr_id=?", currencyID)
        if (!rows.isEmpty()) {
            val row = rows[0]
            m_reader!!.processDefaultCurrency(row)
        }
    }

    /**
     * Process resources.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processResources() {
        val rows = getRows("select * from " + schema + "rsrc where delete_date is null and rsrc_id in (select rsrc_id from " + schema + "taskrsrc t where proj_id=? and delete_date is null) order by rsrc_seq_num", m_projectID)
        m_reader!!.processResources(rows)
    }

    /**
     * Process resource rates.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processResourceRates() {
        val rows = getRows("select * from " + schema + "rsrcrate where delete_date is null and rsrc_id in (select rsrc_id from " + schema + "taskrsrc t where proj_id=? and delete_date is null) order by rsrc_rate_id", m_projectID)
        m_reader!!.processResourceRates(rows)
    }

    /**
     * Process tasks.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processTasks() {
        val wbs = getRows("select * from " + schema + "projwbs where proj_id=? and delete_date is null order by parent_wbs_id,seq_num", m_projectID)
        val tasks = getRows("select * from " + schema + "task where proj_id=? and delete_date is null", m_projectID)
        m_reader!!.processTasks(wbs, tasks)
    }

    /**
     * Process predecessors.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processPredecessors() {
        val rows = getRows("select * from " + schema + "taskpred where proj_id=? and delete_date is null", m_projectID)
        m_reader!!.processPredecessors(rows)
    }

    /**
     * Process calendars.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processCalendars() {
        val rows = getRows("select * from " + schema + "calendar where (proj_id is null or proj_id=?) and delete_date is null", m_projectID)
        m_reader!!.processCalendars(rows)
    }

    /**
     * Process resource assignments.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun processAssignments() {
        val rows = getRows("select * from " + schema + "taskrsrc where proj_id=? and delete_date is null", m_projectID)
        m_reader!!.processAssignments(rows)
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
     * {@inheritDoc}
     */
    @Override
    override fun read(fileName: String): ProjectFile {
        throw UnsupportedOperationException()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun read(file: File): ProjectFile {
        throw UnsupportedOperationException()
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
    private fun getRows(sql: String, `var`: Integer?): List<Row> {
        allocateConnection()

        try {
            val result = LinkedList<Row>()

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
            val name = meta.getColumnName(loop).toLowerCase()
            val type = Integer.valueOf(meta.getColumnType(loop))
            m_meta.put(name, type)
        }
    }

    /**
     * Override the default field name mapping for Task user defined types.
     *
     * @param type target user defined data type
     * @param fieldNames field names
     */
    fun setFieldNamesForTaskUdfType(type: UserFieldDataType, fieldNames: Array<String>) {
        m_taskUdfCounters.setFieldNamesForType(type, fieldNames)
    }

    /**
     * Override the default field name mapping for Resource user defined types.
     *
     * @param type target user defined data type
     * @param fieldNames field names
     */
    fun setFieldNamesForResourceUdfType(type: UserFieldDataType, fieldNames: Array<String>) {
        m_resourceUdfCounters.setFieldNamesForType(type, fieldNames)
    }

    /**
     * Override the default field name mapping for Assignment user defined types.
     *
     * @param type target user defined data type
     * @param fieldNames field names
     */
    fun setFieldNamesForAssignmentUdfType(type: UserFieldDataType, fieldNames: Array<String>) {
        m_assignmentUdfCounters.setFieldNamesForType(type, fieldNames)
    }
}