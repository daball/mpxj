/*
 * file:       PrimaveraReader.java
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

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap

import net.sf.mpxj.ActivityCode
import net.sf.mpxj.ActivityCodeContainer
import net.sf.mpxj.ActivityCodeValue
import net.sf.mpxj.AssignmentField
import net.sf.mpxj.Availability
import net.sf.mpxj.AvailabilityTable
import net.sf.mpxj.ConstraintType
import net.sf.mpxj.CostRateTable
import net.sf.mpxj.CostRateTableEntry
import net.sf.mpxj.CurrencySymbolPosition
import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.DataType
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.DayType
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.FieldContainer
import net.sf.mpxj.FieldType
import net.sf.mpxj.FieldTypeClass
import net.sf.mpxj.Priority
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarDateRanges
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Rate
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceField
import net.sf.mpxj.ResourceType
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.TaskType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.BooleanHelper
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper

/**
 * This class provides a generic front end to read project data from
 * a database.
 */
internal class PrimaveraReader
/**
 * Constructor.
 *
 * @param taskUdfCounters UDF counters for tasks
 * @param resourceUdfCounters UDF counters for resources
 * @param assignmentUdfCounters UDF counters for assignments
 * @param resourceFields resource field mapping
 * @param wbsFields wbs field mapping
 * @param taskFields task field mapping
 * @param assignmentFields assignment field mapping
 * @param aliases alias mapping
 * @param matchPrimaveraWBS determine WBS behaviour
 */
(private val m_taskUdfCounters: UserFieldCounters, private val m_resourceUdfCounters: UserFieldCounters, private val m_assignmentUdfCounters: UserFieldCounters, private val m_resourceFields: Map<FieldType, String>, private val m_wbsFields: Map<FieldType, String>, private val m_taskFields: Map<FieldType, String>, private val m_assignmentFields: Map<FieldType, String>, aliases: Map<FieldType, String>, private val m_matchPrimaveraWBS: Boolean) {

    /**
     * Retrieves the project data read from this file.
     *
     * @return project data
     */
    val project: ProjectFile
    private val m_eventManager: EventManager
    private val m_clashMap = HashMap<Integer, Integer>()
    private val m_calMap = HashMap<Integer, ProjectCalendar>()
    private val m_calendarTimeFormat = SimpleDateFormat("HH:mm")
    private var m_defaultCalendarID: Integer? = null
    /**
     * Retrieves a list of external predecessors relationships.
     *
     * @return list of external predecessors
     */
    val externalPredecessors: List<ExternalPredecessorRelation> = ArrayList<ExternalPredecessorRelation>()

    private val m_udfFields = HashMap<Integer, String>()
    private val m_udfValues = HashMap<String, Map<Integer, List<Row>>>()

    private val m_activityCodeMap = HashMap<Integer, ActivityCodeValue>()
    private val m_activityCodeAssignments = HashMap<Integer, List<Integer>>()

    init {
        project = ProjectFile()
        m_eventManager = project.eventManager

        val config = project.projectConfig
        config.autoTaskUniqueID = false
        config.autoResourceUniqueID = false
        config.autoCalendarUniqueID = true
        config.autoAssignmentUniqueID = false
        config.autoWBS = false

        applyAliases(aliases)
        m_taskUdfCounters.reset()
        m_resourceUdfCounters.reset()
        m_assignmentUdfCounters.reset()
    }

    /**
     * Process project properties.
     *
     * @param rows project properties data.
     * @param projectID project ID
     */
    fun processProjectProperties(rows: List<Row>, projectID: Integer?) {
        if (rows.isEmpty() === false) {
            val row = rows[0]
            val properties = project.projectProperties
            properties.creationDate = row.getDate("create_date")
            properties.finishDate = row.getDate("plan_end_date")
            properties.name = row.getString("proj_short_name")
            properties.startDate = row.getDate("plan_start_date") // data_date?
            properties.defaultTaskType = TASK_TYPE_MAP.get(row.getString("def_duration_type"))
            properties.statusDate = row.getDate("last_recalc_date")
            properties.fiscalYearStartMonth = row.getInteger("fy_start_month_num")
            properties.uniqueID = if (projectID == null) null else projectID!!.toString()
            properties.exportFlag = row.getBoolean("export_flag")
            // cannot assign actual calendar yet as it has not been read yet
            m_defaultCalendarID = row.getInteger("clndr_id")
        }
    }

    /**
     * Read activity code types and values.
     *
     * @param types activity code type data
     * @param typeValues activity code value data
     * @param assignments activity code task assignments
     */
    fun processActivityCodes(types: List<Row>, typeValues: List<Row>, assignments: List<Row>) {
        val container = project.activityCodes
        val map = HashMap<Integer, ActivityCode>()

        for (row in types) {
            val code = ActivityCode(row.getInteger("actv_code_type_id"), row.getString("actv_code_type"))
            container.add(code)
            map.put(code.getUniqueID(), code)
        }

        for (row in typeValues) {
            val code = map.get(row.getInteger("actv_code_type_id"))
            if (code != null) {
                val value = code!!.addValue(row.getInteger("actv_code_id"), row.getString("short_name"), row.getString("actv_code_name"))
                m_activityCodeMap.put(value.getUniqueID(), value)
            }
        }

        for (row in assignments) {
            val taskID = row.getInteger("task_id")
            var list = m_activityCodeAssignments.get(taskID)
            if (list == null) {
                list = ArrayList<Integer>()
                m_activityCodeAssignments.put(taskID, list)
            }
            list!!.add(row.getInteger("actv_code_id"))
        }
    }

    /**
     * Process User Defined Fields (UDF).
     *
     * @param fields field definitions
     * @param values field values
     */
    fun processUserDefinedFields(fields: List<Row>, values: List<Row>) {
        // Process fields
        val tableNameMap = HashMap<Integer, String>()
        for (row in fields) {
            val fieldId = row.getInteger("udf_type_id")
            val tableName = row.getString("table_name")
            tableNameMap.put(fieldId, tableName)

            val fieldType = FIELD_TYPE_MAP.get(tableName)
            if (fieldType != null) {
                val fieldDataType = row.getString("logical_data_type")
                val fieldName = row.getString("udf_type_label")

                m_udfFields.put(fieldId, fieldName)
                addUserDefinedField(fieldType, UserFieldDataType.valueOf(fieldDataType), fieldName)
            }
        }

        // Process values
        for (row in values) {
            val typeID = row.getInteger("udf_type_id")
            val tableName = tableNameMap.get(typeID)
            var tableData = m_udfValues.get(tableName)
            if (tableData == null) {
                tableData = HashMap<Integer, List<Row>>()
                m_udfValues.put(tableName, tableData)
            }

            val id = row.getInteger("fk_id")
            var list = tableData!!.get(id)
            if (list == null) {
                list = ArrayList<Row>()
                tableData!!.put(id, list)
            }
            list!!.add(row)
        }
    }

    /**
     * Process project calendars.
     *
     * @param rows project calendar data
     */
    fun processCalendars(rows: List<Row>) {
        for (row in rows) {
            processCalendar(row)
        }

        if (m_defaultCalendarID != null) {
            val defaultCalendar = m_calMap.get(m_defaultCalendarID)
            // Primavera XER files can sometimes not contain a definition of the default
            // project calendar so only try to set if we find a definition.
            if (defaultCalendar != null) {
                project.defaultCalendar = defaultCalendar
            }
        }
    }

    /**
     * Process data for an individual calendar.
     *
     * @param row calendar data
     */
    fun processCalendar(row: Row) {
        val calendar = project.addCalendar()

        val id = row.getInteger("clndr_id")
        m_calMap.put(id, calendar)
        calendar.name = row.getString("clndr_name")

        try {
            calendar.setMinutesPerDay(Integer.valueOf(NumberHelper.getDouble(row.getDouble("day_hr_cnt")) as Int * 60))
            calendar.setMinutesPerWeek(Integer.valueOf((NumberHelper.getDouble(row.getDouble("week_hr_cnt")) * 60) as Int))
            calendar.setMinutesPerMonth(Integer.valueOf((NumberHelper.getDouble(row.getDouble("month_hr_cnt")) * 60) as Int))
            calendar.setMinutesPerYear(Integer.valueOf((NumberHelper.getDouble(row.getDouble("year_hr_cnt")) * 60) as Int))
        } catch (ex: ClassCastException) {
            // We have seen examples of malformed calendar data where fields have been missing
            // from the record. We'll typically get a class cast exception here as we're trying
            // to process something which isn't a double.
            // We'll just return at this point as it's not clear that we can salvage anything
            // sensible from this record.
            return
        }

        // Process data
        val calendarData = row.getString("clndr_data")
        if (calendarData != null && !calendarData.isEmpty()) {
            val root = Record.getRecord(calendarData)
            if (root != null) {
                processCalendarDays(calendar, root)
                processCalendarExceptions(calendar, root)
            }
        } else {
            // if there is not DaysOfWeek data, Primavera seems to default to Mon-Fri, 8:00-16:00
            val defaultHourRange = DateRange(DateHelper.getTime(8, 0), DateHelper.getTime(16, 0))
            for (day in Day.values()) {
                if (day !== Day.SATURDAY && day !== Day.SUNDAY) {
                    calendar.setWorkingDay(day, true)
                    val hours = calendar.addCalendarHours(day)
                    hours.addRange(defaultHourRange)
                } else {
                    calendar.setWorkingDay(day, false)
                }
            }
        }

        m_eventManager.fireCalendarReadEvent(calendar)
    }

    /**
     * Process calendar days of the week.
     *
     * @param calendar project calendar
     * @param root calendar data
     */
    private fun processCalendarDays(calendar: ProjectCalendar, root: Record) {
        // Retrieve working hours ...
        val daysOfWeek = root.getChild("DaysOfWeek")
        if (daysOfWeek != null) {
            for (dayRecord in daysOfWeek.children) {
                processCalendarHours(calendar, dayRecord)
            }
        }
    }

    /**
     * Process hours in a working day.
     *
     * @param calendar project calendar
     * @param dayRecord working day data
     */
    private fun processCalendarHours(calendar: ProjectCalendar, dayRecord: Record) {
        // ... for each day of the week
        val day = Day.getInstance(Integer.parseInt(dayRecord.field))
        // Get hours
        val recHours = dayRecord.children
        if (recHours.size() === 0) {
            // No data -> not working
            calendar.setWorkingDay(day, false)
        } else {
            calendar.setWorkingDay(day, true)
            // Read hours
            val hours = calendar.addCalendarHours(day)
            for (recWorkingHours in recHours) {
                addHours(hours, recWorkingHours)
            }
        }
    }

    /**
     * Parses a record containing hours and add them to a container.
     *
     * @param ranges hours container
     * @param hoursRecord hours record
     */
    private fun addHours(ranges: ProjectCalendarDateRanges, hoursRecord: Record) {
        if (hoursRecord.value != null) {
            val wh = hoursRecord.value!!.split("\\|")
            try {
                val startText: String
                var endText: String

                if (wh[0].equals("s")) {
                    startText = wh[1]
                    endText = wh[3]
                } else {
                    startText = wh[3]
                    endText = wh[1]
                }

                // for end time treat midnight as midnight next day
                if (endText.equals("00:00")) {
                    endText = "24:00"
                }
                val start = m_calendarTimeFormat.parse(startText)
                val end = m_calendarTimeFormat.parse(endText)

                ranges.addRange(DateRange(start, end))
            } catch (e: ParseException) {
                // silently ignore date parse exceptions
            }

        }
    }

    /**
     * Process calendar exceptions.
     *
     * @param calendar project calendar
     * @param root calendar data
     */
    private fun processCalendarExceptions(calendar: ProjectCalendar, root: Record) {
        // Retrieve exceptions
        val exceptions = root.getChild("Exceptions")
        if (exceptions != null) {
            for (exception in exceptions.children) {
                val daysFromEpoch = Integer.parseInt(exception.value!!.split("\\|")[1])
                val startEx = DateHelper.getDateFromLong(EXCEPTION_EPOCH + daysFromEpoch * DateHelper.MS_PER_DAY)

                val pce = calendar.addCalendarException(startEx, startEx)
                for (exceptionHours in exception.children) {
                    addHours(pce, exceptionHours)
                }
            }
        }
    }

    /**
     * Process resources.
     *
     * @param rows resource data
     */
    fun processResources(rows: List<Row>) {
        for (row in rows) {
            val resource = project.addResource()
            processFields(m_resourceFields, row, resource)
            resource.resourceCalendar = getResourceCalendar(row.getInteger("clndr_id"))

            // Even though we're not filling in a rate, filling in a time unit can still be useful
            // so that we know what rate time unit was originally used in Primavera.
            val timeUnit = TIME_UNIT_MAP.get(row.getString("cost_qty_type"))
            resource.standardRateUnits = timeUnit
            resource.overtimeRateUnits = timeUnit

            // Add User Defined Fields
            populateUserDefinedFieldValues("RSRC", FieldTypeClass.RESOURCE, resource, resource.uniqueID)

            m_eventManager.fireResourceReadEvent(resource)
        }
    }

    /**
     * Retrieve the correct calendar for a resource.
     *
     * @param calendarID calendar ID
     * @return calendar for resource
     */
    private fun getResourceCalendar(calendarID: Integer?): ProjectCalendar? {
        var result: ProjectCalendar? = null
        if (calendarID != null) {
            val calendar = m_calMap.get(calendarID)
            if (calendar != null) {
                //
                // If the resource is linked to a base calendar, derive
                // a default calendar from the base calendar.
                //
                if (!calendar!!.isDerived) {
                    val resourceCalendar = project.addCalendar()
                    resourceCalendar.parent = calendar
                    resourceCalendar.setWorkingDay(Day.MONDAY, DayType.DEFAULT)
                    resourceCalendar.setWorkingDay(Day.TUESDAY, DayType.DEFAULT)
                    resourceCalendar.setWorkingDay(Day.WEDNESDAY, DayType.DEFAULT)
                    resourceCalendar.setWorkingDay(Day.THURSDAY, DayType.DEFAULT)
                    resourceCalendar.setWorkingDay(Day.FRIDAY, DayType.DEFAULT)
                    resourceCalendar.setWorkingDay(Day.SATURDAY, DayType.DEFAULT)
                    resourceCalendar.setWorkingDay(Day.SUNDAY, DayType.DEFAULT)
                    result = resourceCalendar
                } else {
                    //
                    // Primavera seems to allow a calendar to be shared between resources
                    // whereas in the MS Project model there is a one-to-one
                    // relationship. If we find a shared calendar, take a copy of it
                    //
                    if (calendar!!.resource == null) {
                        result = calendar
                    } else {
                        val copy = project.addCalendar()
                        copy.copy(calendar!!)
                        result = copy
                    }
                }
            }
        }

        return result
    }

    /**
     * Process resource rates.
     *
     * @param rows resource rate data
     */
    fun processResourceRates(rows: List<Row>) {
        // Primavera defines resource cost tables by start dates so sort and define end by next
        Collections.sort(rows, object : Comparator<Row>() {
            @Override
            fun compare(r1: Row, r2: Row): Int {
                val id1 = r1.getInteger("rsrc_id")
                val id2 = r2.getInteger("rsrc_id")
                val cmp = NumberHelper.compare(id1, id2)
                if (cmp != 0) {
                    return cmp
                }
                val d1 = r1.getDate("start_date")
                val d2 = r2.getDate("start_date")
                return DateHelper.compare(d1, d2)
            }
        })

        for (i in 0 until rows.size()) {
            val row = rows[i]

            val resourceID = row.getInteger("rsrc_id")
            val standardRate = Rate(row.getDouble("cost_per_qty"), TimeUnit.HOURS)
            val standardRateFormat = TimeUnit.HOURS
            val overtimeRate = Rate(0, TimeUnit.HOURS) // does this exist in Primavera?
            val overtimeRateFormat = TimeUnit.HOURS
            val costPerUse = NumberHelper.getDouble(0.0)
            val maxUnits = NumberHelper.getDouble(NumberHelper.getDouble(row.getDouble("max_qty_per_hr")) * 100) // adjust to be % as in MS Project
            val startDate = row.getDate("start_date")
            var endDate = DateHelper.LAST_DATE

            if (i + 1 < rows.size()) {
                val nextRow = rows[i + 1]
                val nextResourceID = nextRow.getInt("rsrc_id")
                if (resourceID.intValue() === nextResourceID) {
                    endDate = nextRow.getDate("start_date")
                }
            }

            val resource = project.getResourceByUniqueID(resourceID)
            if (resource != null) {
                var costRateTable: CostRateTable? = resource.getCostRateTable(0)
                if (costRateTable == null) {
                    costRateTable = CostRateTable()
                    resource.setCostRateTable(0, costRateTable)
                }
                val entry = CostRateTableEntry(standardRate, standardRateFormat, overtimeRate, overtimeRateFormat, costPerUse, endDate)
                costRateTable!!.add(entry)

                val availabilityTable = resource.availability
                val newAvailability = Availability(startDate, endDate, maxUnits)
                availabilityTable.add(newAvailability)
            }
        }
    }

    /**
     * Process tasks.
     *
     * @param wbs WBS task data
     * @param tasks task data
     */
    fun processTasks(wbs: List<Row>, tasks: List<Row>) {
        val projectProperties = project.projectProperties
        val projectName = projectProperties.name
        val uniqueIDs = HashSet<Integer>()
        val wbsTasks = HashSet<Task>()

        //
        // We set the project name when we read the project properties, but that's just
        // the short name. The full project name lives on the first WBS item. Rather than
        // querying twice, we'll just set it here where we have access to the WBS items.
        // I haven't changed what's in the project name attribute as that's the value
        // MPXJ users are used to receiving in that attribute, so we'll use the title
        // attribute instead.
        //
        if (!wbs.isEmpty()) {
            projectProperties.projectTitle = wbs[0].getString("wbs_name")
        }

        //
        // Read WBS entries and create tasks.
        // Note that the wbs list is supplied to us in the correct order.
        //
        for (row in wbs) {
            val task = project.addTask()
            task.project = projectName // P6 task always belongs to project
            task.summary = true
            processFields(m_wbsFields, row, task)
            populateUserDefinedFieldValues("PROJWBS", FieldTypeClass.TASK, task, task.uniqueID)
            uniqueIDs.add(task.uniqueID)
            wbsTasks.add(task)
            m_eventManager.fireTaskReadEvent(task)
        }

        //
        // Create hierarchical structure
        //
        val activityIDField = getActivityIDField(m_wbsFields)
        project.childTasks.clear()
        for (row in wbs) {
            val task = project.getTaskByUniqueID(row.getInteger("wbs_id"))
            val parentTask = project.getTaskByUniqueID(row.getInteger("parent_wbs_id"))
            if (parentTask == null) {
                project.childTasks.add(task)
            } else {
                project.childTasks.remove(task)
                parentTask.childTasks.add(task)
                task.wbs = parentTask.wbs.toString() + "." + task.wbs
                if (activityIDField != null) {
                    task.set(activityIDField, task.wbs)
                }
            }
        }

        //
        // Read Task entries and create tasks
        //
        var nextID = 1
        m_clashMap.clear()
        for (row in tasks) {
            val task: Task
            val parentTaskID = row.getInteger("wbs_id")
            val parentTask = project.getTaskByUniqueID(parentTaskID)
            if (parentTask == null) {
                task = project.addTask()
            } else {
                task = parentTask.addTask()
            }
            task.project = projectName // P6 task always belongs to project

            processFields(m_taskFields, row, task)

            task.milestone = BooleanHelper.getBoolean(MILESTONE_MAP.get(row.getString("task_type")))

            // Only "Resource Dependent" activities consider resource calendars during scheduling in P6.
            task.ignoreResourceCalendar = !"TT_Rsrc".equals(row.getString("task_type"))

            task.percentageComplete = calculatePercentComplete(row)

            if (m_matchPrimaveraWBS && parentTask != null) {
                task.wbs = parentTask.wbs
            }

            var uniqueID = task.uniqueID

            // Add User Defined Fields - before we handle ID clashes
            populateUserDefinedFieldValues("TASK", FieldTypeClass.TASK, task, uniqueID)

            populateActivityCodes(task)

            if (uniqueIDs.contains(uniqueID)) {
                while (uniqueIDs.contains(Integer.valueOf(nextID))) {
                    ++nextID
                }
                val newUniqueID = Integer.valueOf(nextID)
                m_clashMap.put(uniqueID, newUniqueID)
                uniqueID = newUniqueID
                task.uniqueID = uniqueID
            }
            uniqueIDs.add(uniqueID)

            val calId = row.getInteger("clndr_id")
            val cal = m_calMap.get(calId)
            task.calendar = cal

            val startDate = if (row.getDate("act_start_date") == null) row.getDate("restart_date") else row.getDate("act_start_date")
            task.start = startDate
            val endDate = if (row.getDate("act_end_date") == null) row.getDate("reend_date") else row.getDate("act_end_date")
            task.finish = endDate

            val work = Duration.add(task.actualWork, task.remainingWork, projectProperties)
            task.work = work

            m_eventManager.fireTaskReadEvent(task)
        }

        ActivitySorter(TaskField.TEXT1, wbsTasks).sort(project)

        updateStructure()
        updateDates()
        updateWork()
    }

    /**
     * Read details of any activity codes assigned to this task.
     *
     * @param task parent task
     */
    private fun populateActivityCodes(task: Task) {
        val list = m_activityCodeAssignments.get(task.uniqueID)
        if (list != null) {
            for (id in list!!) {
                val value = m_activityCodeMap.get(id)
                if (value != null) {
                    task.addActivityCode(value)
                }
            }
        }
    }

    /**
     * Determine which field the Activity ID has been mapped to.
     *
     * @param map field map
     * @return field
     */
    private fun getActivityIDField(map: Map<FieldType, String>): FieldType? {
        var result: FieldType? = null
        for (entry in map.entrySet()) {
            if (entry.getValue().equals("task_code")) {
                result = entry.getKey()
                break
            }
        }
        return result
    }

    /**
     * Configure a new user defined field.
     *
     * @param fieldType field type
     * @param dataType field data type
     * @param name field name
     */
    private fun addUserDefinedField(fieldType: FieldTypeClass, dataType: UserFieldDataType, name: String) {
        try {
            when (fieldType) {
                TASK -> {
                    var taskField: TaskField

                    do {
                        taskField = m_taskUdfCounters.nextField(TaskField::class.java, dataType)
                    } while (m_taskFields.containsKey(taskField) || m_wbsFields.containsKey(taskField))

                    project.customFields.getCustomField(taskField).setAlias(name)
                }
                RESOURCE -> {
                    var resourceField: ResourceField

                    do {
                        resourceField = m_resourceUdfCounters.nextField(ResourceField::class.java, dataType)
                    } while (m_resourceFields.containsKey(resourceField))

                    project.customFields.getCustomField(resourceField).setAlias(name)
                }
                ASSIGNMENT -> {
                    var assignmentField: AssignmentField

                    do {
                        assignmentField = m_assignmentUdfCounters.nextField(AssignmentField::class.java, dataType)
                    } while (m_assignmentFields.containsKey(assignmentField))

                    project.customFields.getCustomField(assignmentField).setAlias(name)
                }
                else -> {
                }
            }
        } catch (ex: Exception) {
            //
            // SF#227: If we get an exception thrown here... it's likely that
            // we've run out of user defined fields, for example
            // there are only 30 TEXT fields. We'll ignore this: the user
            // defined field won't be mapped to an alias, so we'll
            // ignore it when we read in the values.
            //
        }

    }

    /**
     * Adds a user defined field value to a task.
     *
     * @param fieldType field type
     * @param container FieldContainer instance
     * @param row UDF data
     */
    private fun addUDFValue(fieldType: FieldTypeClass, container: FieldContainer, row: Row) {
        val fieldId = row.getInteger("udf_type_id")
        val fieldName = m_udfFields.get(fieldId)

        var value: Object? = null
        val field = project.customFields.getFieldByAlias(fieldType, fieldName)
        if (field != null) {
            val fieldDataType = field!!.getDataType()

            when (fieldDataType) {
                DATE -> {
                    value = row.getDate("udf_date")
                }

                CURRENCY, NUMERIC -> {
                    value = row.getDouble("udf_number")
                }

                GUID, INTEGER -> {
                    value = row.getInteger("udf_code_id")
                }

                BOOLEAN -> {
                    val text = row.getString("udf_text")
                    if (text != null) {
                        // before a normal boolean parse, we try to lookup the text as a P6 static type indicator UDF
                        value = STATICTYPE_UDF_MAP.get(text)
                        if (value == null) {
                            value = Boolean.valueOf(row.getBoolean("udf_text"))
                        }
                    } else {
                        value = Boolean.valueOf(row.getBoolean("udf_number"))
                    }
                }

                else -> {
                    value = row.getString("udf_text")
                }
            }

            container.set(field, value)
        }
    }

    /**
     * Populate the UDF values for this entity.
     *
     * @param tableName parent table name
     * @param type entity type
     * @param container entity
     * @param uniqueID entity Unique ID
     */
    private fun populateUserDefinedFieldValues(tableName: String, type: FieldTypeClass, container: FieldContainer, uniqueID: Integer) {
        val tableData = m_udfValues.get(tableName)
        if (tableData != null) {
            val udf = tableData!!.get(uniqueID)
            if (udf != null) {
                for (r in udf!!) {
                    addUDFValue(type, container, r)
                }
            }
        }
    }

    /*
      private String getNotes(List<Row> notes, String keyField, int keyValue, String notesField)
      {
         String result = null;
         for (Row row : notes)
         {
            if (row.getInt(keyField) == keyValue)
            {
               result = row.getString(notesField);
               break;
            }
         }
         return result;
      }
   */

    /**
     * Populates a field based on baseline and actual values.
     *
     * @param container field container
     * @param target target field
     * @param baseline baseline field
     * @param actual actual field
     */
    private fun populateField(container: FieldContainer, target: FieldType, baseline: FieldType, actual: FieldType) {
        var value = container.getCachedValue(actual)
        if (value == null) {
            value = container.getCachedValue(baseline)
        }
        container.set(target, value)
    }

    /**
     * Iterates through the tasks setting the correct
     * outline level and ID values.
     */
    private fun updateStructure() {
        var id = 1
        val outlineLevel = Integer.valueOf(1)
        for (task in project.childTasks) {
            id = updateStructure(id, task, outlineLevel)
        }
    }

    /**
     * Iterates through the tasks setting the correct
     * outline level and ID values.
     *
     * @param id current ID value
     * @param task current task
     * @param outlineLevel current outline level
     * @return next ID value
     */
    private fun updateStructure(id: Int, task: Task, outlineLevel: Integer): Int {
        var id = id
        var outlineLevel = outlineLevel
        task.id = Integer.valueOf(id++)
        task.outlineLevel = outlineLevel
        outlineLevel = Integer.valueOf(outlineLevel.intValue() + 1)
        for (childTask in task.childTasks) {
            id = updateStructure(id, childTask, outlineLevel)
        }
        return id
    }

    /**
     * The Primavera WBS entries we read in as tasks have user-entered start and end dates
     * which aren't calculated or adjusted based on the child task dates. We try
     * to compensate for this by using these user-entered dates as baseline dates, and
     * deriving the planned start, actual start, planned finish and actual finish from
     * the child tasks. This method recursively descends through the tasks to do this.
     */
    private fun updateDates() {
        for (task in project.childTasks) {
            updateDates(task)
        }
    }

    /**
     * See the notes above.
     *
     * @param parentTask parent task.
     */
    private fun updateDates(parentTask: Task) {
        if (parentTask.hasChildTasks()) {
            var finished = 0
            var plannedStartDate: Date? = parentTask.start
            var plannedFinishDate: Date? = parentTask.finish
            var actualStartDate = parentTask.actualStart
            var actualFinishDate: Date? = parentTask.actualFinish
            var earlyStartDate: Date? = parentTask.earlyStart
            var earlyFinishDate: Date? = parentTask.earlyFinish
            var remainingEarlyStartDate: Date? = parentTask.remainingEarlyStart
            var remainingEarlyFinishDate: Date? = parentTask.remainingEarlyFinish
            var lateStartDate: Date? = parentTask.lateStart
            var lateFinishDate: Date? = parentTask.lateFinish
            var baselineStartDate: Date? = parentTask.baselineStart
            var baselineFinishDate: Date? = parentTask.baselineFinish

            for (task in parentTask.childTasks) {
                updateDates(task)

                // the child tasks can have null dates (e.g. for nested wbs elements with no task children) so we
                // still must protect against some children having null dates

                plannedStartDate = DateHelper.min(plannedStartDate, task.start)
                plannedFinishDate = DateHelper.max(plannedFinishDate, task.finish)
                actualStartDate = DateHelper.min(actualStartDate, task.actualStart)
                actualFinishDate = DateHelper.max(actualFinishDate, task.actualFinish)
                earlyStartDate = DateHelper.min(earlyStartDate, task.earlyStart)
                earlyFinishDate = DateHelper.max(earlyFinishDate, task.earlyFinish)
                remainingEarlyStartDate = DateHelper.min(remainingEarlyStartDate, task.remainingEarlyStart)
                remainingEarlyFinishDate = DateHelper.max(remainingEarlyFinishDate, task.remainingEarlyFinish)
                lateStartDate = DateHelper.min(lateStartDate, task.lateStart)
                lateFinishDate = DateHelper.max(lateFinishDate, task.lateFinish)
                baselineStartDate = DateHelper.min(baselineStartDate, task.baselineStart)
                baselineFinishDate = DateHelper.max(baselineFinishDate, task.baselineFinish)

                if (task.actualFinish != null) {
                    ++finished
                }
            }

            parentTask.start = plannedStartDate
            parentTask.finish = plannedFinishDate
            parentTask.actualStart = actualStartDate
            parentTask.earlyStart = earlyStartDate
            parentTask.earlyFinish = earlyFinishDate
            parentTask.remainingEarlyStart = remainingEarlyStartDate
            parentTask.remainingEarlyFinish = remainingEarlyFinishDate
            parentTask.lateStart = lateStartDate
            parentTask.lateFinish = lateFinishDate
            parentTask.baselineStart = baselineStartDate
            parentTask.baselineFinish = baselineFinishDate

            //
            // Only if all child tasks have actual finish dates do we
            // set the actual finish date on the parent task.
            //
            if (finished == parentTask.childTasks.size()) {
                parentTask.actualFinish = actualFinishDate
            }

            var baselineDuration: Duration? = null
            if (baselineStartDate != null && baselineFinishDate != null) {
                baselineDuration = project.defaultCalendar!!.getWork(baselineStartDate, baselineFinishDate, TimeUnit.HOURS)
                parentTask.baselineDuration = baselineDuration
            }

            var remainingDuration: Duration? = null
            if (parentTask.actualFinish == null) {
                var startDate: Date? = parentTask.earlyStart
                if (startDate == null) {
                    startDate = baselineStartDate
                }

                var finishDate: Date? = parentTask.earlyFinish
                if (finishDate == null) {
                    finishDate = baselineFinishDate
                }

                if (startDate != null && finishDate != null) {
                    remainingDuration = project.defaultCalendar!!.getWork(startDate, finishDate, TimeUnit.HOURS)
                }
            } else {
                remainingDuration = Duration.getInstance(0, TimeUnit.HOURS)
            }
            parentTask.remainingDuration = remainingDuration

            if (baselineDuration != null && baselineDuration!!.getDuration() !== 0 && remainingDuration != null) {
                val durationPercentComplete = (baselineDuration!!.getDuration() - remainingDuration!!.getDuration()) / baselineDuration!!.getDuration() * 100.0
                parentTask.percentageComplete = Double.valueOf(durationPercentComplete)
            }
        }
    }

    /**
     * The Primavera WBS entries we read in as tasks don't have work entered. We try
     * to compensate for this by summing the child tasks' work. This method recursively
     * descends through the tasks to do this.
     */
    private fun updateWork() {
        for (task in project.childTasks) {
            updateWork(task)
        }
    }

    /**
     * See the notes above.
     *
     * @param parentTask parent task.
     */
    private fun updateWork(parentTask: Task) {
        if (parentTask.hasChildTasks()) {
            val properties = project.projectProperties

            var actualWork: Duration? = null
            var baselineWork: Duration? = null
            var remainingWork: Duration? = null
            var work: Duration? = null

            for (task in parentTask.childTasks) {
                updateWork(task)

                actualWork = Duration.add(actualWork, task.actualWork, properties)
                baselineWork = Duration.add(baselineWork, task.baselineWork, properties)
                remainingWork = Duration.add(remainingWork, task.remainingWork, properties)
                work = Duration.add(work, task.work, properties)
            }

            parentTask.actualWork = actualWork
            parentTask.baselineWork = baselineWork
            parentTask.remainingWork = remainingWork
            parentTask.work = work
        }
    }

    /**
     * Processes predecessor data.
     *
     * @param rows predecessor data
     */
    fun processPredecessors(rows: List<Row>) {
        for (row in rows) {
            val currentID = mapTaskID(row.getInteger("task_id"))
            val predecessorID = mapTaskID(row.getInteger("pred_task_id"))
            val currentTask = project.getTaskByUniqueID(currentID)
            val predecessorTask = project.getTaskByUniqueID(predecessorID)
            val type = RELATION_TYPE_MAP.get(row.getString("pred_type"))
            val lag = row.getDuration("lag_hr_cnt")
            if (currentTask != null) {
                val uniqueID = row.getInteger("task_pred_id")
                if (predecessorTask != null) {
                    val relation = currentTask.addPredecessor(predecessorTask, type, lag)
                    relation.uniqueID = uniqueID
                    m_eventManager.fireRelationReadEvent(relation)
                } else {
                    // if we can't find the predecessor, it must lie outside the project
                    val relation = ExternalPredecessorRelation(predecessorID, currentTask, type, lag)
                    externalPredecessors.add(relation)
                    relation.uniqueID = uniqueID
                }
            }
        }
    }

    /**
     * Process assignment data.
     *
     * @param rows assignment data
     */
    fun processAssignments(rows: List<Row>) {
        for (row in rows) {
            val task = project.getTaskByUniqueID(mapTaskID(row.getInteger("task_id")))
            val resource = project.getResourceByUniqueID(row.getInteger("rsrc_id"))
            if (task != null && resource != null) {
                val assignment = task.addResourceAssignment(resource)
                processFields(m_assignmentFields, row, assignment)

                populateField(assignment, AssignmentField.START, AssignmentField.BASELINE_START, AssignmentField.ACTUAL_START)
                populateField(assignment, AssignmentField.FINISH, AssignmentField.BASELINE_FINISH, AssignmentField.ACTUAL_FINISH)

                // include actual overtime work in work calculations
                val remainingWork = row.getDuration("remain_qty")
                val actualOvertimeWork = row.getDuration("act_ot_qty")
                val actualRegularWork = row.getDuration("act_reg_qty")
                val actualWork = Duration.add(actualOvertimeWork, actualRegularWork, project.projectProperties)
                val totalWork = Duration.add(actualWork, remainingWork, project.projectProperties)
                assignment.actualWork = actualWork
                assignment.work = totalWork

                // include actual overtime cost in cost calculations
                val remainingCost = row.getDouble("remain_cost")
                val actualOvertimeCost = row.getDouble("act_ot_cost")
                val actualRegularCost = row.getDouble("act_reg_cost")
                val actualCost = NumberHelper.getDouble(actualOvertimeCost) + NumberHelper.getDouble(actualRegularCost)
                val totalCost = actualCost + NumberHelper.getDouble(remainingCost)
                assignment.actualCost = NumberHelper.getDouble(actualCost)
                assignment.cost = NumberHelper.getDouble(totalCost)

                val units: Double
                if (resource.type == ResourceType.MATERIAL) {
                    units = (if (totalWork == null) 0 else totalWork!!.getDuration() * 100).toDouble()
                } else
                // RT_Labor & RT_Equip
                {
                    units = NumberHelper.getDouble(row.getDouble("target_qty_per_hr")) * 100
                }
                assignment.units = NumberHelper.getDouble(units)

                // Add User Defined Fields
                populateUserDefinedFieldValues("TASKRSRC", FieldTypeClass.ASSIGNMENT, assignment, assignment.uniqueID)

                m_eventManager.fireAssignmentReadEvent(assignment)
            }
        }

        updateTaskCosts()
    }

    /**
     * Sets task cost fields by summing the resource assignment costs. The "projcost" table isn't
     * necessarily available in XER files so we do this instead to back into task costs. Costs for
     * the summary tasks constructed from Primavera WBS entries are calculated by recursively
     * summing child costs.
     */
    private fun updateTaskCosts() {
        for (task in project.childTasks) {
            updateTaskCosts(task)
        }
    }

    /**
     * See the notes above.
     *
     * @param parentTask parent task
     */
    private fun updateTaskCosts(parentTask: Task) {
        var baselineCost = 0.0
        var actualCost = 0.0
        var remainingCost = 0.0
        var cost = 0.0

        //process children first before adding their costs
        for (child in parentTask.childTasks) {
            updateTaskCosts(child)
            baselineCost += NumberHelper.getDouble(child.baselineCost)
            actualCost += NumberHelper.getDouble(child.actualCost)
            remainingCost += NumberHelper.getDouble(child.remainingCost)
            cost += NumberHelper.getDouble(child.cost)
        }

        val resourceAssignments = parentTask.resourceAssignments
        for (assignment in resourceAssignments) {
            baselineCost += NumberHelper.getDouble(assignment.baselineCost)
            actualCost += NumberHelper.getDouble(assignment.actualCost)
            remainingCost += NumberHelper.getDouble(assignment.remainingCost)
            cost += NumberHelper.getDouble(assignment.cost)
        }

        parentTask.baselineCost = NumberHelper.getDouble(baselineCost)
        parentTask.actualCost = NumberHelper.getDouble(actualCost)
        parentTask.remainingCost = NumberHelper.getDouble(remainingCost)
        parentTask.cost = NumberHelper.getDouble(cost)
    }

    /**
     * Code common to both XER and database readers to extract
     * currency format data.
     *
     * @param row row containing currency data
     */
    fun processDefaultCurrency(row: Row) {
        val properties = project.projectProperties
        properties.currencySymbol = row.getString("curr_symbol")
        properties.symbolPosition = CURRENCY_SYMBOL_POSITION_MAP.get(row.getString("pos_curr_fmt_type"))
        properties.currencyDigits = row.getInteger("decimal_digit_cnt")
        properties.thousandsSeparator = row.getString("digit_group_symbol").charAt(0)
        properties.decimalSeparator = row.getString("decimal_symbol").charAt(0)
    }

    /**
     * Generic method to extract Primavera fields and assign to MPXJ fields.
     *
     * @param map map of MPXJ field types and Primavera field names
     * @param row Primavera data container
     * @param container MPXJ data contain
     */
    private fun processFields(map: Map<FieldType, String>, row: Row, container: FieldContainer) {
        for (entry in map.entrySet()) {
            val field = entry.getKey()
            val name = entry.getValue()

            val value: Object
            when (field.getDataType()) {
                INTEGER -> {
                    value = row.getInteger(name)
                }

                BOOLEAN -> {
                    value = Boolean.valueOf(row.getBoolean(name))
                }

                DATE -> {
                    value = row.getDate(name)
                }

                CURRENCY, NUMERIC, PERCENTAGE -> {
                    value = row.getDouble(name)
                }

                DELAY, WORK, DURATION -> {
                    value = row.getDuration(name)
                }

                RESOURCE_TYPE -> {
                    value = RESOURCE_TYPE_MAP.get(row.getString(name))
                }

                TASK_TYPE -> {
                    value = TASK_TYPE_MAP.get(row.getString(name))
                }

                CONSTRAINT -> {
                    value = CONSTRAINT_TYPE_MAP.get(row.getString(name))
                }

                PRIORITY -> {
                    value = PRIORITY_MAP.get(row.getString(name))
                }

                GUID -> {
                    value = row.getUUID(name)
                }

                else -> {
                    value = row.getString(name)
                }
            }

            container.set(field, value)
        }
    }

    /**
     * Deals with the case where we have had to map a task ID to a new value.
     *
     * @param id task ID from database
     * @return mapped task ID
     */
    private fun mapTaskID(id: Integer): Integer? {
        var mappedID = m_clashMap.get(id)
        if (mappedID == null) {
            mappedID = id
        }
        return mappedID
    }

    /**
     * Apply aliases to task and resource fields.
     *
     * @param aliases map of aliases
     */
    private fun applyAliases(aliases: Map<FieldType, String>) {
        val fields = project.customFields
        for (entry in aliases.entrySet()) {
            fields.getCustomField(entry.getKey()).setAlias(entry.getValue())
        }
    }

    /**
     * Determine which type of percent complete is used on on this task,
     * and calculate the required value.
     *
     * @param row task data
     * @return percent complete value
     */
    private fun calculatePercentComplete(row: Row): Number {
        val result: Number
        when (PercentCompleteType.getInstance(row.getString("complete_pct_type"))) {
            PercentCompleteType.UNITS -> {
                result = calculateUnitsPercentComplete(row)
            }

            PercentCompleteType.DURATION -> {
                result = calculateDurationPercentComplete(row)
            }

            else -> {
                result = calculatePhysicalPercentComplete(row)
            }
        }

        return result
    }

    /**
     * Calculate the physical percent complete.
     *
     * @param row task data
     * @return percent complete
     */
    private fun calculatePhysicalPercentComplete(row: Row): Number {
        return row.getDouble("phys_complete_pct")
    }

    /**
     * Calculate the units percent complete.
     *
     * @param row task data
     * @return percent complete
     */
    private fun calculateUnitsPercentComplete(row: Row): Number {
        var result = 0.0

        val actualWorkQuantity = NumberHelper.getDouble(row.getDouble("act_work_qty"))
        val actualEquipmentQuantity = NumberHelper.getDouble(row.getDouble("act_equip_qty"))
        val numerator = actualWorkQuantity + actualEquipmentQuantity

        if (numerator != 0.0) {
            val remainingWorkQuantity = NumberHelper.getDouble(row.getDouble("remain_work_qty"))
            val remainingEquipmentQuantity = NumberHelper.getDouble(row.getDouble("remain_equip_qty"))
            val denominator = remainingWorkQuantity + actualWorkQuantity + remainingEquipmentQuantity + actualEquipmentQuantity
            result = if (denominator == 0.0) 0 else numerator * 100 / denominator
        }

        return NumberHelper.getDouble(result)
    }

    /**
     * Calculate the duration percent complete.
     *
     * @param row task data
     * @return percent complete
     */
    private fun calculateDurationPercentComplete(row: Row): Number {
        var result = 0.0
        val targetDuration = row.getDuration("target_drtn_hr_cnt").getDuration()
        val remainingDuration = row.getDuration("remain_drtn_hr_cnt").getDuration()

        if (targetDuration == 0.0) {
            if (remainingDuration == 0.0) {
                if ("TK_Complete".equals(row.getString("status_code"))) {
                    result = 100.0
                }
            }
        } else {
            if (remainingDuration < targetDuration) {
                result = (targetDuration - remainingDuration) * 100 / targetDuration
            }
        }

        return NumberHelper.getDouble(result)
    }

    companion object {

        /**
         * Retrieve the default mapping between MPXJ resource fields and Primavera resource field names.
         *
         * @return mapping
         */
        val defaultResourceFieldMap: Map<FieldType, String>
            get() {
                val map = LinkedHashMap<FieldType, String>()

                map.put(ResourceField.UNIQUE_ID, "rsrc_id")
                map.put(ResourceField.GUID, "guid")
                map.put(ResourceField.NAME, "rsrc_name")
                map.put(ResourceField.CODE, "employee_code")
                map.put(ResourceField.EMAIL_ADDRESS, "email_addr")
                map.put(ResourceField.NOTES, "rsrc_notes")
                map.put(ResourceField.CREATED, "create_date")
                map.put(ResourceField.TYPE, "rsrc_type")
                map.put(ResourceField.INITIALS, "rsrc_short_name")
                map.put(ResourceField.PARENT_ID, "parent_rsrc_id")

                return map
            }

        /**
         * Retrieve the default mapping between MPXJ task fields and Primavera wbs field names.
         *
         * @return mapping
         */
        val defaultWbsFieldMap: Map<FieldType, String>
            get() {
                val map = LinkedHashMap<FieldType, String>()

                map.put(TaskField.UNIQUE_ID, "wbs_id")
                map.put(TaskField.GUID, "guid")
                map.put(TaskField.NAME, "wbs_name")
                map.put(TaskField.BASELINE_COST, "orig_cost")
                map.put(TaskField.REMAINING_COST, "indep_remain_total_cost")
                map.put(TaskField.REMAINING_WORK, "indep_remain_work_qty")
                map.put(TaskField.DEADLINE, "anticip_end_date")
                map.put(TaskField.DATE1, "suspend_date")
                map.put(TaskField.DATE2, "resume_date")
                map.put(TaskField.TEXT1, "task_code")
                map.put(TaskField.WBS, "wbs_short_name")

                return map
            }

        /**
         * Retrieve the default mapping between MPXJ task fields and Primavera task field names.
         *
         * @return mapping
         */
        val defaultTaskFieldMap: Map<FieldType, String>
            get() {
                val map = LinkedHashMap<FieldType, String>()

                map.put(TaskField.UNIQUE_ID, "task_id")
                map.put(TaskField.GUID, "guid")
                map.put(TaskField.NAME, "task_name")
                map.put(TaskField.ACTUAL_DURATION, "act_drtn_hr_cnt")
                map.put(TaskField.REMAINING_DURATION, "remain_drtn_hr_cnt")
                map.put(TaskField.ACTUAL_WORK, "act_work_qty")
                map.put(TaskField.REMAINING_WORK, "remain_work_qty")
                map.put(TaskField.BASELINE_WORK, "target_work_qty")
                map.put(TaskField.BASELINE_DURATION, "target_drtn_hr_cnt")
                map.put(TaskField.DURATION, "target_drtn_hr_cnt")
                map.put(TaskField.CONSTRAINT_DATE, "cstr_date")
                map.put(TaskField.ACTUAL_START, "act_start_date")
                map.put(TaskField.ACTUAL_FINISH, "act_end_date")
                map.put(TaskField.LATE_START, "late_start_date")
                map.put(TaskField.LATE_FINISH, "late_end_date")
                map.put(TaskField.EARLY_START, "early_start_date")
                map.put(TaskField.EARLY_FINISH, "early_end_date")
                map.put(TaskField.REMAINING_EARLY_START, "restart_date")
                map.put(TaskField.REMAINING_EARLY_FINISH, "reend_date")
                map.put(TaskField.BASELINE_START, "target_start_date")
                map.put(TaskField.BASELINE_FINISH, "target_end_date")
                map.put(TaskField.CONSTRAINT_TYPE, "cstr_type")
                map.put(TaskField.PRIORITY, "priority_type")
                map.put(TaskField.CREATED, "create_date")
                map.put(TaskField.TYPE, "duration_type")
                map.put(TaskField.FREE_SLACK, "free_float_hr_cnt")
                map.put(TaskField.TOTAL_SLACK, "total_float_hr_cnt")
                map.put(TaskField.TEXT1, "task_code")
                map.put(TaskField.TEXT2, "task_type")
                map.put(TaskField.TEXT3, "status_code")
                map.put(TaskField.NUMBER1, "rsrc_id")

                return map
            }

        /**
         * Retrieve the default mapping between MPXJ assignment fields and Primavera assignment field names.
         *
         * @return mapping
         */
        val defaultAssignmentFieldMap: Map<FieldType, String>
            get() {
                val map = LinkedHashMap<FieldType, String>()

                map.put(AssignmentField.UNIQUE_ID, "taskrsrc_id")
                map.put(AssignmentField.GUID, "guid")
                map.put(AssignmentField.REMAINING_WORK, "remain_qty")
                map.put(AssignmentField.BASELINE_WORK, "target_qty")
                map.put(AssignmentField.ACTUAL_OVERTIME_WORK, "act_ot_qty")
                map.put(AssignmentField.BASELINE_COST, "target_cost")
                map.put(AssignmentField.ACTUAL_OVERTIME_COST, "act_ot_cost")
                map.put(AssignmentField.REMAINING_COST, "remain_cost")
                map.put(AssignmentField.ACTUAL_START, "act_start_date")
                map.put(AssignmentField.ACTUAL_FINISH, "act_end_date")
                map.put(AssignmentField.BASELINE_START, "target_start_date")
                map.put(AssignmentField.BASELINE_FINISH, "target_end_date")
                map.put(AssignmentField.ASSIGNMENT_DELAY, "target_lag_drtn_hr_cnt")

                return map
            }

        /**
         * Retrieve the default aliases to be applied to MPXJ task and resource fields.
         *
         * @return map of aliases
         */
        val defaultAliases: Map<FieldType, String>
            get() {
                val map = HashMap<FieldType, String>()

                map.put(TaskField.DATE1, "Suspend Date")
                map.put(TaskField.DATE2, "Resume Date")
                map.put(TaskField.TEXT1, "Code")
                map.put(TaskField.TEXT2, "Activity Type")
                map.put(TaskField.TEXT3, "Status")
                map.put(TaskField.NUMBER1, "Primary Resource Unique ID")

                return map
            }

        private val RESOURCE_TYPE_MAP = HashMap<String, ResourceType>()

        init {
            RESOURCE_TYPE_MAP.put(null, ResourceType.WORK)
            RESOURCE_TYPE_MAP.put("RT_Labor", ResourceType.WORK)
            RESOURCE_TYPE_MAP.put("RT_Mat", ResourceType.MATERIAL)
            RESOURCE_TYPE_MAP.put("RT_Equip", ResourceType.WORK)
        }

        private val CONSTRAINT_TYPE_MAP = HashMap<String, ConstraintType>()

        init {
            CONSTRAINT_TYPE_MAP.put("CS_MSO", ConstraintType.MUST_START_ON)
            CONSTRAINT_TYPE_MAP.put("CS_MSOB", ConstraintType.START_NO_LATER_THAN)
            CONSTRAINT_TYPE_MAP.put("CS_MSOA", ConstraintType.START_NO_EARLIER_THAN)
            CONSTRAINT_TYPE_MAP.put("CS_MEO", ConstraintType.MUST_FINISH_ON)
            CONSTRAINT_TYPE_MAP.put("CS_MEOB", ConstraintType.FINISH_NO_LATER_THAN)
            CONSTRAINT_TYPE_MAP.put("CS_MEOA", ConstraintType.FINISH_NO_EARLIER_THAN)
            CONSTRAINT_TYPE_MAP.put("CS_ALAP", ConstraintType.AS_LATE_AS_POSSIBLE)
            CONSTRAINT_TYPE_MAP.put("CS_MANDSTART", ConstraintType.MUST_START_ON)
            CONSTRAINT_TYPE_MAP.put("CS_MANDFIN", ConstraintType.MUST_FINISH_ON)
        }

        private val PRIORITY_MAP = HashMap<String, Priority>()

        init {
            PRIORITY_MAP.put("PT_Top", Priority.getInstance(Priority.HIGHEST))
            PRIORITY_MAP.put("PT_High", Priority.getInstance(Priority.HIGH))
            PRIORITY_MAP.put("PT_Normal", Priority.getInstance(Priority.MEDIUM))
            PRIORITY_MAP.put("PT_Low", Priority.getInstance(Priority.LOW))
            PRIORITY_MAP.put("PT_Lowest", Priority.getInstance(Priority.LOWEST))
        }

        private val RELATION_TYPE_MAP = HashMap<String, RelationType>()

        init {
            RELATION_TYPE_MAP.put("PR_FS", RelationType.FINISH_START)
            RELATION_TYPE_MAP.put("PR_FF", RelationType.FINISH_FINISH)
            RELATION_TYPE_MAP.put("PR_SS", RelationType.START_START)
            RELATION_TYPE_MAP.put("PR_SF", RelationType.START_FINISH)
        }

        private val TASK_TYPE_MAP = HashMap<String, TaskType>()

        init {
            TASK_TYPE_MAP.put("DT_FixedDrtn", TaskType.FIXED_DURATION)
            TASK_TYPE_MAP.put("DT_FixedQty", TaskType.FIXED_UNITS)
            TASK_TYPE_MAP.put("DT_FixedDUR2", TaskType.FIXED_WORK)
            TASK_TYPE_MAP.put("DT_FixedRate", TaskType.FIXED_WORK)
        }

        private val MILESTONE_MAP = HashMap<String, Boolean>()

        init {
            MILESTONE_MAP.put("TT_Task", Boolean.FALSE)
            MILESTONE_MAP.put("TT_Rsrc", Boolean.FALSE)
            MILESTONE_MAP.put("TT_LOE", Boolean.FALSE)
            MILESTONE_MAP.put("TT_Mile", Boolean.TRUE)
            MILESTONE_MAP.put("TT_FinMile", Boolean.TRUE)
            MILESTONE_MAP.put("TT_WBS", Boolean.FALSE)
        }

        private val TIME_UNIT_MAP = HashMap<String, TimeUnit>()

        init {
            TIME_UNIT_MAP.put("QT_Minute", TimeUnit.MINUTES)
            TIME_UNIT_MAP.put("QT_Hour", TimeUnit.HOURS)
            TIME_UNIT_MAP.put("QT_Day", TimeUnit.DAYS)
            TIME_UNIT_MAP.put("QT_Week", TimeUnit.WEEKS)
            TIME_UNIT_MAP.put("QT_Month", TimeUnit.MONTHS)
            TIME_UNIT_MAP.put("QT_Year", TimeUnit.YEARS)
        }

        private val CURRENCY_SYMBOL_POSITION_MAP = HashMap<String, CurrencySymbolPosition>()

        init {
            CURRENCY_SYMBOL_POSITION_MAP.put("#1.1", CurrencySymbolPosition.BEFORE)
            CURRENCY_SYMBOL_POSITION_MAP.put("1.1#", CurrencySymbolPosition.AFTER)
            CURRENCY_SYMBOL_POSITION_MAP.put("# 1.1", CurrencySymbolPosition.BEFORE_WITH_SPACE)
            CURRENCY_SYMBOL_POSITION_MAP.put("1.1 #", CurrencySymbolPosition.AFTER_WITH_SPACE)
        }

        private val STATICTYPE_UDF_MAP = HashMap<String, Boolean>()

        init {
            // this is a judgement call on how the static type indicator values would be best translated to a flag
            STATICTYPE_UDF_MAP.put("UDF_G0", Boolean.FALSE) // no indicator
            STATICTYPE_UDF_MAP.put("UDF_G1", Boolean.FALSE) // red x
            STATICTYPE_UDF_MAP.put("UDF_G2", Boolean.FALSE) // yellow !
            STATICTYPE_UDF_MAP.put("UDF_G3", Boolean.TRUE) // green check
            STATICTYPE_UDF_MAP.put("UDF_G4", Boolean.TRUE) // blue star
        }

        private val FIELD_TYPE_MAP = HashMap<String, FieldTypeClass>()

        init {
            FIELD_TYPE_MAP.put("PROJWBS", FieldTypeClass.TASK)
            FIELD_TYPE_MAP.put("TASK", FieldTypeClass.TASK)
            FIELD_TYPE_MAP.put("RSRC", FieldTypeClass.RESOURCE)
            FIELD_TYPE_MAP.put("TASKRSRC", FieldTypeClass.ASSIGNMENT)
        }

        private val EXCEPTION_EPOCH = -2209161599935L
    }
}