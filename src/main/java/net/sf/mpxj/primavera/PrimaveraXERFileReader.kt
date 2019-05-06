/*
 * file:       PrimaveraXERFileReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       25/03/2010
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

import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList

import net.sf.mpxj.FieldType
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Relation
import net.sf.mpxj.Task
import net.sf.mpxj.common.CharsetHelper
import net.sf.mpxj.common.MultiDateFormat
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.ReaderTokenizer
import net.sf.mpxj.common.Tokenizer
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * This class creates a new ProjectFile instance by reading a Primavera XER file.
 */
class PrimaveraXERFileReader : AbstractProjectReader() {

    private var m_encoding: String? = null
    /**
     * Retrieve the Charset used to read the file.
     *
     * @return Charset instance
     */
    /**
     * Alternative way to set the file encoding. If both an encoding name and a Charset instance
     * are supplied, the Charset instance is used.
     *
     * @param charset Charset used when reading the file
     */
    private// We default to CP1252 as this seems to be the most common encoding
    var charset: Charset? = null
        get() {
            var result = field
            if (result == null) {
                result = if (m_encoding == null) CharsetHelper.CP1252 else Charset.forName(m_encoding)
            }
            return result
        }
        set
    private var m_reader: PrimaveraReader? = null
    private var m_projectID: Integer? = null
    internal var m_skipTable: Boolean = false
    private var m_tables: Map<String, List<Row>>? = null
    private var m_currentTableName: String? = null
    private var m_currentTable: List<Row>? = null
    private var m_currentFieldNames: Array<String>? = null
    private var m_defaultCurrencyName: String? = null
    private val m_currencyMap = HashMap<String, DecimalFormat>()
    private var m_numberFormat: DecimalFormat? = null
    private var m_defaultCurrencyData: Row? = null
    private val m_df = MultiDateFormat("yyyy-MM-dd HH:mm", "yyyy-MM-dd")
    private var m_projectListeners: List<ProjectListener>? = null
    private val m_taskUdfCounters = UserFieldCounters()
    private val m_resourceUdfCounters = UserFieldCounters()
    private val m_assignmentUdfCounters = UserFieldCounters()
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
     * Set the ID of the project to be read.
     *
     * @param projectID project ID
     */
    fun setProjectID(projectID: Int) {
        m_projectID = Integer.valueOf(projectID)
    }

    /**
     * Sets the character encoding used when reading an XER file.
     *
     * @param encoding encoding name
     */
    fun setEncoding(encoding: String) {
        m_encoding = encoding
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(`is`: InputStream): ProjectFile {
        try {
            m_tables = HashMap<String, List<Row>>()
            m_numberFormat = DecimalFormat()

            processFile(`is`)

            m_reader = PrimaveraReader(m_taskUdfCounters, m_resourceUdfCounters, m_assignmentUdfCounters, resourceFieldMap, wbsFieldMap, taskFieldMap, assignmentFields, aliases, matchPrimaveraWBS)
            val project = m_reader!!.project
            project.projectProperties.fileApplication = "Primavera"
            project.projectProperties.fileType = "XER"
            project.eventManager.addProjectListeners(m_projectListeners)

            processProjectID()
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
        } finally {
            m_reader = null
            m_tables = null
            m_currentTableName = null
            m_currentTable = null
            m_currentFieldNames = null
            m_defaultCurrencyName = null
            m_currencyMap.clear()
            m_numberFormat = null
            m_defaultCurrencyData = null
        }
    }

    /**
     * This is a convenience method which allows all projects in an
     * XER file to be read in a single pass.
     *
     * @param is input stream
     * @param linkCrossProjectRelations add Relation links that cross ProjectFile boundaries
     * @return list of ProjectFile instances
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    @JvmOverloads
    fun readAll(`is`: InputStream, linkCrossProjectRelations: Boolean = false): List<ProjectFile> {
        try {
            m_tables = HashMap<String, List<Row>>()
            m_numberFormat = DecimalFormat()

            processFile(`is`)

            val rows = getRows("project", null, null)
            val result = ArrayList<ProjectFile>(rows.size())
            val externalPredecessors = ArrayList<ExternalPredecessorRelation>()
            for (row in rows) {
                setProjectID(row.getInt("proj_id"))

                m_reader = PrimaveraReader(m_taskUdfCounters, m_resourceUdfCounters, m_assignmentUdfCounters, resourceFieldMap, wbsFieldMap, taskFieldMap, assignmentFields, aliases, matchPrimaveraWBS)
                val project = m_reader!!.project
                project.eventManager.addProjectListeners(m_projectListeners)

                processProjectProperties()
                processUserDefinedFields()
                processCalendars()
                processResources()
                processResourceRates()
                processTasks()
                processPredecessors()
                processAssignments()

                externalPredecessors.addAll(m_reader!!.externalPredecessors)

                m_reader = null
                project.updateStructure()

                result.add(project)
            }

            if (linkCrossProjectRelations) {
                for (externalRelation in externalPredecessors) {
                    var predecessorTask: Task?
                    // we could aggregate the project task id maps but that's likely more work
                    // than just looping through the projects
                    for (proj in result) {
                        predecessorTask = proj.getTaskByUniqueID(externalRelation.sourceUniqueID)
                        if (predecessorTask != null) {
                            val relation = externalRelation.targetTask.addPredecessor(predecessorTask, externalRelation.type, externalRelation.lag)
                            relation.uniqueID = externalRelation.uniqueID
                            break
                        }
                    }
                    // if predecessorTask not found the external task is outside of the file so ignore
                }
            }

            return result
        } finally {
            m_reader = null
            m_tables = null
            m_currentTableName = null
            m_currentTable = null
            m_currentFieldNames = null
            m_defaultCurrencyName = null
            m_currencyMap.clear()
            m_numberFormat = null
            m_defaultCurrencyData = null
        }
    }

    /**
     * Reads the XER file table and row structure ready for processing.
     *
     * @param is input stream
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun processFile(`is`: InputStream) {
        var line = 1

        try {
            //
            // Test the header and extract the separator. If this is successful,
            // we reset the stream back as far as we can. The design of the
            // BufferedInputStream class means that we can't get back to character
            // zero, so the first record we will read will get "RMHDR" rather than
            // "ERMHDR" in the first field position.
            //
            val bis = BufferedInputStream(`is`)
            val data = ByteArray(6)
            data[0] = bis.read() as Byte
            bis.mark(1024)
            bis.read(data, 1, 5)

            if (!String(data).equals("ERMHDR")) {
                throw MPXJException(MPXJException.INVALID_FILE)
            }

            bis.reset()

            val reader = InputStreamReader(bis, charset)
            val tk = ReaderTokenizer(reader)
            tk.setDelimiter('\t')
            val record = ArrayList<String>()

            while (tk.type != Tokenizer.TT_EOF) {
                readRecord(tk, record)
                if (!record.isEmpty()) {
                    if (processRecord(record)) {
                        break
                    }
                }
                ++line
            }
        } catch (ex: Exception) {
            throw MPXJException(MPXJException.READ_ERROR.toString() + " (failed at line " + line + ")", ex)
        }

    }

    /**
     * If the user has not specified a project ID, this method
     * retrieves the ID of the first project in the file.
     */
    private fun processProjectID() {
        if (m_projectID == null) {
            val rows = getRows("project", null, null)
            if (!rows.isEmpty()) {
                val row = rows[0]
                m_projectID = row.getInteger("proj_id")
            }
        }
    }

    /**
     * Process a currency definition.
     *
     * @param row record from XER file
     */
    private fun processCurrency(row: Row) {
        val currencyName = row.getString("curr_short_name")
        val symbols = DecimalFormatSymbols()
        symbols.setDecimalSeparator(row.getString("decimal_symbol").charAt(0))
        symbols.setGroupingSeparator(row.getString("digit_group_symbol").charAt(0))
        val nf = DecimalFormat()
        nf.setDecimalFormatSymbols(symbols)
        nf.applyPattern("#.#")
        m_currencyMap.put(currencyName, nf)

        if (currencyName.equalsIgnoreCase(m_defaultCurrencyName)) {
            m_numberFormat = nf
            m_defaultCurrencyData = row
        }
    }

    /**
     * Populates a Map instance representing the IDs and names of
     * projects available in the current file.
     *
     * @param is input stream used to read XER file
     * @return Map instance containing ID and name pairs
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    fun listProjects(`is`: InputStream): Map<Integer, String> {
        try {
            m_tables = HashMap<String, List<Row>>()
            processFile(`is`)

            val result = HashMap<Integer, String>()

            val rows = getRows("project", null, null)
            for (row in rows) {
                val id = row.getInteger("proj_id")
                val name = row.getString("proj_short_name")
                result.put(id, name)
            }

            return result
        } finally {
            m_tables = null
            m_currentTable = null
            m_currentFieldNames = null
        }
    }

    /**
     * Process project properties.
     */
    private fun processProjectProperties() {
        //
        // Process common attributes
        //
        val rows = getRows("project", "proj_id", m_projectID)
        m_reader!!.processProjectProperties(rows, m_projectID)

        //
        // Process XER-specific attributes
        //
        if (m_defaultCurrencyData != null) {
            m_reader!!.processDefaultCurrency(m_defaultCurrencyData!!)
        }

        processScheduleOptions()
    }

    /**
     * Process activity code data.
     */
    private fun processActivityCodes() {
        val types = getRows("actvtype", null, null)
        val typeValues = getRows("actvcode", null, null)
        val assignments = getRows("taskactv", null, null)
        m_reader!!.processActivityCodes(types, typeValues, assignments)
    }

    /**
     * Process schedule options from SCHEDOPTIONS. This table only seems to exist
     * in XER files, not P6 databases.
     */
    private fun processScheduleOptions() {
        val rows = getRows("schedoptions", "proj_id", m_projectID)
        if (rows.isEmpty() === false) {
            val row = rows[0]
            val customProperties = HashMap<String, Object>()
            customProperties.put("LagCalendar", row.getString("sched_calendar_on_relationship_lag"))
            customProperties.put("RetainedLogic", Boolean.valueOf(row.getBoolean("sched_retained_logic")))
            customProperties.put("ProgressOverride", Boolean.valueOf(row.getBoolean("sched_progress_override")))
            customProperties.put("IgnoreOtherProjectRelationships", row.getString("sched_outer_depend_type"))
            customProperties.put("StartToStartLagCalculationType", Boolean.valueOf(row.getBoolean("sched_lag_early_start_flag")))
            m_reader!!.project.projectProperties.customProperties = customProperties
        }
    }

    /**
     * Process user defined fields.
     */
    private fun processUserDefinedFields() {
        val fields = getRows("udftype", null, null)
        val values = getRows("udfvalue", null, null)
        m_reader!!.processUserDefinedFields(fields, values)
    }

    /**
     * Process project calendars.
     */
    private fun processCalendars() {
        val rows = getRows("calendar", null, null)
        m_reader!!.processCalendars(rows)
    }

    /**
     * Process resources.
     */
    private fun processResources() {
        val rows = getRows("rsrc", null, null)
        m_reader!!.processResources(rows)
    }

    /**
     * Process resource rates.
     */
    private fun processResourceRates() {
        val rows = getRows("rsrcrate", null, null)
        m_reader!!.processResourceRates(rows)
    }

    /**
     * Process tasks.
     */
    private fun processTasks() {
        val wbs = getRows("projwbs", "proj_id", m_projectID)
        val tasks = getRows("task", "proj_id", m_projectID)
        //List<Row> wbsmemos = getRows("wbsmemo", "proj_id", m_projectID);
        //List<Row> taskmemos = getRows("taskmemo", "proj_id", m_projectID);
        Collections.sort(wbs, WBS_ROW_COMPARATOR)
        m_reader!!.processTasks(wbs, tasks/*, wbsmemos, taskmemos*/)
    }

    /**
     * Process predecessors.
     */
    private fun processPredecessors() {
        val rows = getRows("taskpred", "proj_id", m_projectID)
        m_reader!!.processPredecessors(rows)
    }

    /**
     * Process resource assignments.
     */
    private fun processAssignments() {
        val rows = getRows("taskrsrc", "proj_id", m_projectID)
        m_reader!!.processAssignments(rows)
    }

    /**
     * Reads each token from a single record and adds it to a list.
     *
     * @param tk tokenizer
     * @param record list of tokens
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun readRecord(tk: Tokenizer, record: List<String>) {
        record.clear()
        while (tk.nextToken() == Tokenizer.TT_WORD) {
            record.add(tk.token)
        }
    }

    /**
     * Handles a complete record at a time, stores it in a form ready for
     * further processing.
     *
     * @param record record to be processed
     * @return flag indicating if this is the last record in the file to be processed
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun processRecord(record: List<String>): Boolean {
        var done = false

        val type = RECORD_TYPE_MAP.get(record[0]) ?: throw MPXJException(MPXJException.INVALID_FORMAT)

        when (type) {
            PrimaveraXERFileReader.XerRecordType.HEADER -> {
                processHeader(record)
            }

            PrimaveraXERFileReader.XerRecordType.TABLE -> {
                m_currentTableName = record[1].toLowerCase()
                m_skipTable = !REQUIRED_TABLES.contains(m_currentTableName)
                if (m_skipTable) {
                    m_currentTable = null
                } else {
                    m_currentTable = LinkedList<Row>()
                    m_tables!!.put(m_currentTableName, m_currentTable)
                }
            }

            PrimaveraXERFileReader.XerRecordType.FIELDS -> {
                if (m_skipTable) {
                    m_currentFieldNames = null
                } else {
                    m_currentFieldNames = record.toArray(arrayOfNulls<String>(record.size()))
                    for (loop in m_currentFieldNames!!.indices) {
                        m_currentFieldNames[loop] = m_currentFieldNames!![loop].toLowerCase()
                    }
                }
            }

            PrimaveraXERFileReader.XerRecordType.DATA -> {
                if (!m_skipTable) {
                    val map = HashMap<String, Object>()
                    for (loop in 1 until record.size()) {
                        val fieldName = m_currentFieldNames!![loop]
                        val fieldValue = record[loop]
                        var fieldType = FIELD_TYPE_MAP.get(fieldName)
                        if (fieldType == null) {
                            fieldType = XerFieldType.STRING
                        }

                        var objectValue: Object?
                        if (fieldValue.length() === 0) {
                            objectValue = null
                        } else {
                            when (fieldType) {
                                PrimaveraXERFileReader.XerFieldType.DATE -> {
                                    try {
                                        objectValue = m_df.parseObject(fieldValue)
                                    } catch (ex: ParseException) {
                                        objectValue = fieldValue
                                    }

                                }

                                PrimaveraXERFileReader.XerFieldType.CURRENCY, PrimaveraXERFileReader.XerFieldType.DOUBLE, PrimaveraXERFileReader.XerFieldType.DURATION -> {
                                    try {
                                        objectValue = Double.valueOf(m_numberFormat!!.parse(fieldValue.trim()).doubleValue())
                                    } catch (ex: ParseException) {
                                        objectValue = fieldValue
                                    }

                                }

                                PrimaveraXERFileReader.XerFieldType.INTEGER -> {
                                    objectValue = Integer.valueOf(fieldValue.trim())
                                }

                                else -> {
                                    objectValue = fieldValue
                                }
                            }
                        }

                        map.put(fieldName, objectValue)
                    }

                    val currentRow = MapRow(map)
                    m_currentTable!!.add(currentRow)

                    //
                    // Special case - we need to know the default currency format
                    // ahead of time, so process each row as we get it so that
                    // we can correctly parse currency values in later tables.
                    //
                    if (m_currentTableName!!.equals("currtype")) {
                        processCurrency(currentRow)
                    }
                }
            }

            PrimaveraXERFileReader.XerRecordType.END -> {
                done = true
            }

            else -> {
            }
        }

        return done
    }

    /**
     * Extract any useful attributes from the header record.
     *
     * @param record header record
     */
    private fun processHeader(record: List<String>) {
        m_defaultCurrencyName = if (record.size() > 8) record[8] else "USD"
    }

    /**
     * Override the default field name mapping for Task user defined types.
     *
     * @param type target user defined data type
     * @param fieldNames field names
     */
    fun setFieldNamesForTaskUdfType(type: UserFieldDataType, vararg fieldNames: String) {
        m_taskUdfCounters.setFieldNamesForType(type, fieldNames)
    }

    /**
     * Override the default field name mapping for Resource user defined types.
     *
     * @param type target user defined data type
     * @param fieldNames field names
     */
    fun setFieldNamesForResourceUdfType(type: UserFieldDataType, vararg fieldNames: String) {
        m_resourceUdfCounters.setFieldNamesForType(type, fieldNames)
    }

    /**
     * Override the default field name mapping for Resource user defined types.
     *
     * @param type target user defined data type
     * @param fieldNames field names
     */
    fun setFieldNamesForAssignmentUdfType(type: UserFieldDataType, vararg fieldNames: String) {
        m_assignmentUdfCounters.setFieldNamesForType(type, fieldNames)
    }

    /**
     * Filters a list of rows from the named table. If a column name and a value
     * are supplied, then use this to filter the rows. If no column name is
     * supplied, then return all rows.
     *
     * @param tableName table name
     * @param columnName filter column name
     * @param id filter column value
     * @return filtered list of rows
     */
    private fun getRows(tableName: String, columnName: String?, id: Integer?): List<Row> {
        val result: List<Row>
        val table = m_tables!![tableName]
        if (table == null) {
            result = Collections.emptyList()
        } else {
            if (columnName == null) {
                result = table
            } else {
                result = LinkedList<Row>()
                for (row in table) {
                    if (NumberHelper.equals(id, row.getInteger(columnName))) {
                        result.add(row)
                    }
                }
            }
        }
        return result
    }

    /**
     * Represents expected record types.
     */
    private enum class XerRecordType {
        HEADER,
        TABLE,
        FIELDS,
        DATA,
        END
    }

    /**
     * Represents column data types.
     */
    private enum class XerFieldType {
        STRING,
        INTEGER,
        DOUBLE,
        DATE,
        DURATION,
        CURRENCY
    }

    companion object {

        /**
         * Maps record type text to record types.
         */
        private val RECORD_TYPE_MAP = HashMap<String, XerRecordType>()

        init {
            RECORD_TYPE_MAP.put("RMHDR", XerRecordType.HEADER)
            RECORD_TYPE_MAP.put("%T", XerRecordType.TABLE)
            RECORD_TYPE_MAP.put("%F", XerRecordType.FIELDS)
            RECORD_TYPE_MAP.put("%R", XerRecordType.DATA)
            RECORD_TYPE_MAP.put("", XerRecordType.DATA) // Multiline data
            RECORD_TYPE_MAP.put("%E", XerRecordType.END)
        }

        /**
         * Maps field names to data types.
         */
        private val FIELD_TYPE_MAP = HashMap<String, XerFieldType>()

        init {
            FIELD_TYPE_MAP.put("proj_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("fy_start_month_num", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("create_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("plan_end_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("plan_start_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("rsrc_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("create_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("wbs_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("orig_cost", XerFieldType.CURRENCY)
            FIELD_TYPE_MAP.put("indep_remain_total_cost", XerFieldType.CURRENCY)
            FIELD_TYPE_MAP.put("indep_remain_work_qty", XerFieldType.DURATION)
            FIELD_TYPE_MAP.put("anticip_start_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("anticip_end_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("parent_wbs_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("task_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("phys_complete_pct", XerFieldType.DOUBLE)
            FIELD_TYPE_MAP.put("remain_drtn_hr_cnt", XerFieldType.DURATION)
            FIELD_TYPE_MAP.put("act_work_qty", XerFieldType.DURATION)
            FIELD_TYPE_MAP.put("remain_work_qty", XerFieldType.DURATION)
            FIELD_TYPE_MAP.put("target_work_qty", XerFieldType.DURATION)
            FIELD_TYPE_MAP.put("target_drtn_hr_cnt", XerFieldType.DURATION)
            FIELD_TYPE_MAP.put("cstr_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("act_start_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("act_end_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("late_start_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("late_end_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("expect_end_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("early_start_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("early_end_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("target_start_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("target_end_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("restart_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("reend_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("create_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("pred_task_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("lag_hr_cnt", XerFieldType.DURATION)
            FIELD_TYPE_MAP.put("remain_qty", XerFieldType.DURATION)
            FIELD_TYPE_MAP.put("target_qty", XerFieldType.DURATION)
            FIELD_TYPE_MAP.put("act_reg_qty", XerFieldType.DURATION)
            FIELD_TYPE_MAP.put("act_ot_qty", XerFieldType.DURATION)
            FIELD_TYPE_MAP.put("target_cost", XerFieldType.CURRENCY)
            FIELD_TYPE_MAP.put("act_reg_cost", XerFieldType.CURRENCY)
            FIELD_TYPE_MAP.put("act_ot_cost", XerFieldType.CURRENCY)
            FIELD_TYPE_MAP.put("target_start_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("target_end_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("act_equip_qty", XerFieldType.DOUBLE)
            FIELD_TYPE_MAP.put("remain_equip_qty", XerFieldType.DOUBLE)

            FIELD_TYPE_MAP.put("clndr_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("default_flag", XerFieldType.STRING)
            FIELD_TYPE_MAP.put("clndr_name", XerFieldType.STRING)
            FIELD_TYPE_MAP.put("proj_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("base_clndr_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("last_chng_date", XerFieldType.STRING)
            FIELD_TYPE_MAP.put("clndr_type", XerFieldType.STRING)
            FIELD_TYPE_MAP.put("day_hr_cnt", XerFieldType.DOUBLE)
            FIELD_TYPE_MAP.put("week_hr_cnt", XerFieldType.DOUBLE)
            FIELD_TYPE_MAP.put("month_hr_cnt", XerFieldType.DOUBLE)
            FIELD_TYPE_MAP.put("year_hr_cnt", XerFieldType.DOUBLE)
            FIELD_TYPE_MAP.put("clndr_data", XerFieldType.STRING)

            FIELD_TYPE_MAP.put("seq_num", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("taskrsrc_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("parent_rsrc_id", XerFieldType.INTEGER)

            FIELD_TYPE_MAP.put("free_float_hr_cnt", XerFieldType.DURATION)
            FIELD_TYPE_MAP.put("total_float_hr_cnt", XerFieldType.DURATION)

            FIELD_TYPE_MAP.put("decimal_digit_cnt", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("target_qty_per_hr", XerFieldType.DOUBLE)
            FIELD_TYPE_MAP.put("target_lag_drtn_hr_cnt", XerFieldType.DURATION)

            FIELD_TYPE_MAP.put("act_cost", XerFieldType.DOUBLE)
            FIELD_TYPE_MAP.put("target_cost", XerFieldType.DOUBLE)
            FIELD_TYPE_MAP.put("remain_cost", XerFieldType.DOUBLE)

            FIELD_TYPE_MAP.put("last_recalc_date", XerFieldType.DATE)

            FIELD_TYPE_MAP.put("sched_calendar_on_relationship_lag", XerFieldType.STRING)

            // User Defined Fields types (UDF)
            FIELD_TYPE_MAP.put("udf_type", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("table_name", XerFieldType.STRING)
            FIELD_TYPE_MAP.put("udf_type_name", XerFieldType.STRING)
            FIELD_TYPE_MAP.put("udf_type_label", XerFieldType.STRING)
            FIELD_TYPE_MAP.put("loginal_data_type", XerFieldType.STRING)
            FIELD_TYPE_MAP.put("super_flag", XerFieldType.STRING)
            // User Defined Fields values
            FIELD_TYPE_MAP.put("fk_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("udf_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("udf_number", XerFieldType.DOUBLE)
            FIELD_TYPE_MAP.put("udf_text", XerFieldType.STRING)
            FIELD_TYPE_MAP.put("udf_code_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("udf_type_id", XerFieldType.INTEGER)

            FIELD_TYPE_MAP.put("cost_per_qty", XerFieldType.DOUBLE)
            FIELD_TYPE_MAP.put("start_date", XerFieldType.DATE)
            FIELD_TYPE_MAP.put("max_qty_per_hr", XerFieldType.DOUBLE)

            FIELD_TYPE_MAP.put("task_pred_id", XerFieldType.INTEGER)

            FIELD_TYPE_MAP.put("actv_code_type_id", XerFieldType.INTEGER)
            FIELD_TYPE_MAP.put("actv_code_id", XerFieldType.INTEGER)
        }

        private val REQUIRED_TABLES = HashSet<String>()

        init {
            REQUIRED_TABLES.add("project")
            REQUIRED_TABLES.add("calendar")
            REQUIRED_TABLES.add("rsrc")
            REQUIRED_TABLES.add("rsrcrate")
            REQUIRED_TABLES.add("projwbs")
            REQUIRED_TABLES.add("task")
            REQUIRED_TABLES.add("taskpred")
            REQUIRED_TABLES.add("taskrsrc")
            REQUIRED_TABLES.add("currtype")
            REQUIRED_TABLES.add("udftype")
            REQUIRED_TABLES.add("udfvalue")
            REQUIRED_TABLES.add("schedoptions")
            REQUIRED_TABLES.add("actvtype")
            REQUIRED_TABLES.add("actvcode")
            REQUIRED_TABLES.add("taskactv")
        }

        private val WBS_ROW_COMPARATOR = WbsRowComparatorXER()
    }
}
/**
 * This is a convenience method which allows all projects in an
 * XER file to be read in a single pass. External relationships
 * are not linked.
 *
 * @param is input stream
 * @return list of ProjectFile instances
 * @throws MPXJException
 */
