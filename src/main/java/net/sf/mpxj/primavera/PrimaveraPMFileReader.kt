/*
 * file:       PrimaveraPMFileReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       08/08/2011
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

import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import javax.xml.bind.UnmarshallerHandler
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLFilter
import org.xml.sax.XMLReader

import net.sf.mpxj.ActivityCode
import net.sf.mpxj.ActivityCodeContainer
import net.sf.mpxj.ActivityCodeValue
import net.sf.mpxj.AssignmentField
import net.sf.mpxj.ConstraintType
import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.DayType
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.FieldContainer
import net.sf.mpxj.FieldType
import net.sf.mpxj.FieldTypeClass
import net.sf.mpxj.MPXJException
import net.sf.mpxj.Priority
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceField
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.BooleanHelper
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.primavera.schema.APIBusinessObjects
import net.sf.mpxj.primavera.schema.ActivityCodeType
import net.sf.mpxj.primavera.schema.ActivityCodeTypeType
import net.sf.mpxj.primavera.schema.ActivityType
import net.sf.mpxj.primavera.schema.CalendarType
import net.sf.mpxj.primavera.schema.CalendarType.HolidayOrExceptions
import net.sf.mpxj.primavera.schema.CalendarType.HolidayOrExceptions.HolidayOrException
import net.sf.mpxj.primavera.schema.CalendarType.StandardWorkWeek
import net.sf.mpxj.primavera.schema.CalendarType.StandardWorkWeek.StandardWorkHours
import net.sf.mpxj.primavera.schema.CodeAssignmentType
import net.sf.mpxj.primavera.schema.CurrencyType
import net.sf.mpxj.primavera.schema.GlobalPreferencesType
import net.sf.mpxj.primavera.schema.ProjectType
import net.sf.mpxj.primavera.schema.RelationshipType
import net.sf.mpxj.primavera.schema.ResourceAssignmentType
import net.sf.mpxj.primavera.schema.ResourceType
import net.sf.mpxj.primavera.schema.UDFAssignmentType
import net.sf.mpxj.primavera.schema.UDFTypeType
import net.sf.mpxj.primavera.schema.WBSType
import net.sf.mpxj.primavera.schema.WorkTimeType
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * This class creates a new ProjectFile instance by reading a Primavera PM file.
 */
class PrimaveraPMFileReader : AbstractProjectReader() {

    private var m_projectFile: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_projectListeners: List<ProjectListener>? = null
    private val m_clashMap = HashMap<Integer, Integer>()
    private val m_calMap = HashMap<Integer, ProjectCalendar>()
    private val m_activityCodeMap = HashMap<Integer, ActivityCodeValue>()
    private val m_taskUdfCounters = UserFieldCounters()
    private val m_resourceUdfCounters = UserFieldCounters()
    private val m_assignmentUdfCounters = UserFieldCounters()
    private val m_fieldTypeMap = HashMap<Integer, FieldType>()
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
    override fun read(stream: InputStream): ProjectFile {
        try {
            m_projectFile = ProjectFile()
            m_eventManager = m_projectFile!!.eventManager

            val config = m_projectFile!!.projectConfig
            config.autoTaskUniqueID = false
            config.autoResourceUniqueID = false
            config.autoCalendarUniqueID = false
            config.autoAssignmentUniqueID = false
            config.autoWBS = false

            m_projectFile!!.projectProperties.fileApplication = "Primavera"
            m_projectFile!!.projectProperties.fileType = "PMXML"

            val fields = m_projectFile!!.customFields
            fields.getCustomField(TaskField.TEXT1).setAlias("Code")
            fields.getCustomField(TaskField.TEXT2).setAlias("Activity Type")
            fields.getCustomField(TaskField.TEXT3).setAlias("Status")
            fields.getCustomField(TaskField.NUMBER1).setAlias("Primary Resource Unique ID")

            m_eventManager!!.addProjectListeners(m_projectListeners)

            val factory = SAXParserFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            factory.setNamespaceAware(true)
            val saxParser = factory.newSAXParser()
            val xmlReader = saxParser.getXMLReader()

            if (CONTEXT == null) {
                throw CONTEXT_EXCEPTION
            }

            val unmarshaller = CONTEXT!!.createUnmarshaller()
            val filter = NamespaceFilter()
            filter.setParent(xmlReader)
            val unmarshallerHandler = unmarshaller.getUnmarshallerHandler()
            filter.setContentHandler(unmarshallerHandler)
            filter.parse(InputSource(stream))
            val apibo = unmarshallerHandler.getResult() as APIBusinessObjects

            val projects = apibo.project
            var project: ProjectType? = null
            for (currentProject in projects) {
                if (!BooleanHelper.getBoolean(currentProject.isExternal)) {
                    project = currentProject
                    break
                }
            }

            if (project == null) {
                throw MPXJException("Unable to locate any non-external projects in a list of " + projects.size() + " projects")
            }

            processProjectUDFs(apibo)
            processProjectProperties(apibo, project)
            processActivityCodes(apibo, project)
            processCalendars(apibo)
            processResources(apibo)
            processTasks(project)
            processPredecessors(project)
            processAssignments(project)

            //
            // Ensure that the unique ID counters are correct
            //
            config.updateUniqueCounters()

            return m_projectFile
        } catch (ex: ParserConfigurationException) {
            throw MPXJException("Failed to parse file", ex)
        } catch (ex: JAXBException) {
            throw MPXJException("Failed to parse file", ex)
        } catch (ex: SAXException) {
            throw MPXJException("Failed to parse file", ex)
        } catch (ex: IOException) {
            throw MPXJException("Failed to parse file", ex)
        } finally {
            m_projectFile = null
            m_clashMap.clear()
            m_calMap.clear()
            m_activityCodeMap.clear()
        }
    }

    /**
     * Process UDF definitions.
     *
     * @param apibo top level object
     */
    private fun processProjectUDFs(apibo: APIBusinessObjects) {
        for (udf in apibo.udfType) {
            processUDF(udf)
        }
    }

    /**
     * Process an individual UDF.
     *
     * @param udf UDF definition
     */
    private fun processUDF(udf: UDFTypeType) {
        val fieldType = FIELD_TYPE_MAP.get(udf.subjectArea)
        if (fieldType != null) {
            val dataType = UserFieldDataType.getInstanceFromXmlName(udf.dataType)
            val name = udf.title
            val field = addUserDefinedField(fieldType, dataType, name)
            if (field != null) {
                m_fieldTypeMap.put(udf.objectId, field)
            }
        }
    }

    /**
     * Map the Primavera UDF to a custom field.
     *
     * @param fieldType parent object type
     * @param dataType UDF data type
     * @param name UDF name
     * @return FieldType instance
     */
    private fun addUserDefinedField(fieldType: FieldTypeClass, dataType: UserFieldDataType, name: String): FieldType? {
        var field: FieldType? = null

        try {
            when (fieldType) {
                TASK -> {
                    do {
                        field = m_taskUdfCounters.nextField(TaskField::class.java, dataType)
                    } while (RESERVED_TASK_FIELDS.contains(field))

                    m_projectFile!!.customFields.getCustomField(field).setAlias(name)
                }

                RESOURCE -> {
                    field = m_resourceUdfCounters.nextField(ResourceField::class.java, dataType)
                    m_projectFile!!.customFields.getCustomField(field).setAlias(name)
                }

                ASSIGNMENT -> {
                    field = m_assignmentUdfCounters.nextField(AssignmentField::class.java, dataType)
                    m_projectFile!!.customFields.getCustomField(field).setAlias(name)
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

        return field
    }


    /**
     * Process project properties.
     *
     * @param apibo top level object
     * @param project xml container
     */
    private fun processProjectProperties(apibo: APIBusinessObjects, project: ProjectType) {
        val properties = m_projectFile!!.projectProperties

        properties.creationDate = project.createDate
        properties.finishDate = project.finishDate
        properties.name = project.name
        properties.startDate = project.plannedStartDate
        properties.statusDate = project.dataDate
        properties.projectTitle = project.id
        properties.uniqueID = if (project.objectId == null) null else project.objectId.toString()

        val list = apibo.globalPreferences
        if (!list.isEmpty()) {
            val prefs = list.get(0)

            properties.creationDate = prefs.createDate
            properties.lastSaved = prefs.lastUpdateDate
            properties.minutesPerDay = Integer.valueOf((NumberHelper.getDouble(prefs.hoursPerDay) * 60) as Int)
            properties.minutesPerWeek = Integer.valueOf((NumberHelper.getDouble(prefs.hoursPerWeek) * 60) as Int)
            properties.weekStartDay = Day.getInstance(NumberHelper.getInt(prefs.startDayOfWeek))

            val currencyList = apibo.currency
            for (currency in currencyList) {
                if (currency.objectId.equals(prefs.baseCurrencyObjectId)) {
                    properties.currencySymbol = currency.symbol
                    break
                }
            }
        }
    }

    /**
     * Process activity code data.
     *
     * @param apibo global activity code data
     * @param project project-specific activity code data
     */
    private fun processActivityCodes(apibo: APIBusinessObjects, project: ProjectType) {
        val container = m_projectFile!!.activityCodes
        val map = HashMap<Integer, ActivityCode>()

        val types = ArrayList<ActivityCodeTypeType>()
        types.addAll(apibo.activityCodeType)
        types.addAll(project.activityCodeType)

        for (type in types) {
            val code = ActivityCode(type.objectId, type.name)
            container.add(code)
            map.put(code.getUniqueID(), code)
        }

        val typeValues = ArrayList<ActivityCodeType>()
        typeValues.addAll(apibo.activityCode)
        typeValues.addAll(project.activityCode)

        for (typeValue in typeValues) {
            val code = map.get(typeValue.codeTypeObjectId)
            if (code != null) {
                val value = code!!.addValue(typeValue.objectId, typeValue.codeValue, typeValue.description)
                m_activityCodeMap.put(value.getUniqueID(), value)
            }
        }
    }

    /**
     * Process project calendars.
     *
     * @param apibo xml container
     */
    private fun processCalendars(apibo: APIBusinessObjects) {
        for (row in apibo.calendar) {
            val calendar = m_projectFile!!.addCalendar()
            val id = row.objectId
            m_calMap.put(id, calendar)
            calendar.name = row.name
            calendar.uniqueID = id

            val stdWorkWeek = row.standardWorkWeek
            if (stdWorkWeek != null) {
                for (hours in stdWorkWeek!!.getStandardWorkHours()) {
                    val day = DAY_MAP.get(hours.dayOfWeek)
                    val workTime = hours.getWorkTime()
                    if (workTime.isEmpty() || workTime.get(0) == null) {
                        calendar.setWorkingDay(day, false)
                    } else {
                        calendar.setWorkingDay(day, true)

                        val calendarHours = calendar.addCalendarHours(day)
                        for (work in workTime) {
                            if (work != null) {
                                calendarHours.addRange(DateRange(work!!.start, getEndTime(work!!.finish)))
                            }
                        }
                    }
                }
            }

            val hoe = row.holidayOrExceptions
            if (hoe != null) {
                for (ex in hoe!!.getHolidayOrException()) {
                    val startDate = DateHelper.getDayStartDate(ex.date)
                    val endDate = DateHelper.getDayEndDate(ex.date)
                    val pce = calendar.addCalendarException(startDate, endDate)

                    val workTime = ex.getWorkTime()
                    for (work in workTime) {
                        if (work != null) {
                            pce.addRange(DateRange(work!!.start, getEndTime(work!!.finish)))
                        }
                    }
                }
            }
        }
    }

    /**
     * Process resources.
     *
     * @param apibo xml container
     */
    private fun processResources(apibo: APIBusinessObjects) {
        val resources = apibo.resource
        for (xml in resources) {
            val resource = m_projectFile!!.addResource()
            resource.uniqueID = xml.objectId
            resource.name = xml.name
            resource.code = xml.employeeId
            resource.emailAddress = xml.emailAddress
            resource.guid = DatatypeConverter.parseUUID(xml.guid)
            resource.notes = xml.resourceNotes
            resource.creationDate = xml.createDate
            resource.type = RESOURCE_TYPE_MAP.get(xml.resourceType)
            resource.maxUnits = reversePercentage(xml.maxUnitsPerTime)
            resource.parentID = xml.parentObjectId

            val calendarID = xml.calendarObjectId
            if (calendarID != null) {
                val calendar = m_calMap.get(calendarID)
                if (calendar != null) {
                    //
                    // If the resource is linked to a base calendar, derive
                    // a default calendar from the base calendar.
                    //
                    if (!calendar!!.isDerived) {
                        val resourceCalendar = m_projectFile!!.addCalendar()
                        resourceCalendar.parent = calendar
                        resourceCalendar.setWorkingDay(Day.MONDAY, DayType.DEFAULT)
                        resourceCalendar.setWorkingDay(Day.TUESDAY, DayType.DEFAULT)
                        resourceCalendar.setWorkingDay(Day.WEDNESDAY, DayType.DEFAULT)
                        resourceCalendar.setWorkingDay(Day.THURSDAY, DayType.DEFAULT)
                        resourceCalendar.setWorkingDay(Day.FRIDAY, DayType.DEFAULT)
                        resourceCalendar.setWorkingDay(Day.SATURDAY, DayType.DEFAULT)
                        resourceCalendar.setWorkingDay(Day.SUNDAY, DayType.DEFAULT)
                        resource.resourceCalendar = resourceCalendar
                    } else {
                        //
                        // Primavera seems to allow a calendar to be shared between resources
                        // whereas in the MS Project model there is a one-to-one
                        // relationship. If we find a shared calendar, take a copy of it
                        //
                        if (calendar!!.resource == null) {
                            resource.resourceCalendar = calendar
                        } else {
                            val copy = m_projectFile!!.addCalendar()
                            copy.copy(calendar!!)
                            resource.resourceCalendar = copy
                        }
                    }
                }
            }

            readUDFTypes(resource, xml.getUDF())

            m_eventManager!!.fireResourceReadEvent(resource)
        }
    }

    /**
     * Process tasks.
     *
     * @param project xml container
     */
    private fun processTasks(project: ProjectType) {
        val wbs = project.wbs
        val tasks = project.activity
        val uniqueIDs = HashSet<Integer>()
        val wbsTasks = HashSet<Task>()

        //
        // Read WBS entries and create tasks
        //
        Collections.sort(wbs, WBS_ROW_COMPARATOR)

        for (row in wbs) {
            val task = m_projectFile!!.addTask()
            val uniqueID = row.objectId
            uniqueIDs.add(uniqueID)
            wbsTasks.add(task)

            task.uniqueID = uniqueID
            task.guid = DatatypeConverter.parseUUID(row.guid)
            task.name = row.name
            task.baselineCost = row.summaryBaselineTotalCost
            task.remainingCost = row.summaryRemainingTotalCost
            task.remainingDuration = getDuration(row.summaryRemainingDuration)
            task.summary = true
            task.start = row.anticipatedStartDate
            task.finish = row.anticipatedFinishDate
            task.wbs = row.code
        }

        //
        // Create hierarchical structure
        //
        m_projectFile!!.childTasks.clear()
        for (row in wbs) {
            val task = m_projectFile!!.getTaskByUniqueID(row.objectId)
            val parentTask = m_projectFile!!.getTaskByUniqueID(row.parentObjectId)
            if (parentTask == null) {
                m_projectFile!!.childTasks.add(task)
            } else {
                m_projectFile!!.childTasks.remove(task)
                parentTask.childTasks.add(task)
                task.wbs = parentTask.wbs.toString() + "." + task.wbs
                task.setText(1, task.wbs)
            }
        }

        //
        // Read Task entries and create tasks
        //
        var nextID = 1
        m_clashMap.clear()
        for (row in tasks) {
            var uniqueID = row.objectId
            if (uniqueIDs.contains(uniqueID)) {
                while (uniqueIDs.contains(Integer.valueOf(nextID))) {
                    ++nextID
                }
                val newUniqueID = Integer.valueOf(nextID)
                m_clashMap.put(uniqueID, newUniqueID)
                uniqueID = newUniqueID
            }
            uniqueIDs.add(uniqueID)

            val task: Task
            val parentTaskID = row.wbsObjectId
            val parentTask = m_projectFile!!.getTaskByUniqueID(parentTaskID)
            if (parentTask == null) {
                task = m_projectFile!!.addTask()
            } else {
                task = parentTask.addTask()
            }

            task.uniqueID = uniqueID
            task.guid = DatatypeConverter.parseUUID(row.guid)
            task.name = row.name
            task.percentageComplete = reversePercentage(row.percentComplete)
            task.remainingDuration = getDuration(row.remainingDuration)
            task.actualWork = getDuration(zeroIsNull(row.actualDuration))
            task.remainingWork = getDuration(row.remainingTotalUnits)
            task.baselineDuration = getDuration(row.plannedDuration)
            task.actualDuration = getDuration(row.actualDuration)
            task.duration = getDuration(row.atCompletionDuration)

            // ActualCost and RemainingCost will be set when we resolve the resource assignments
            task.actualCost = NumberHelper.DOUBLE_ZERO
            task.remainingCost = NumberHelper.DOUBLE_ZERO
            task.baselineCost = NumberHelper.DOUBLE_ZERO

            task.constraintDate = row.primaryConstraintDate
            task.constraintType = CONSTRAINT_TYPE_MAP.get(row.primaryConstraintType)
            task.actualStart = row.actualStartDate
            task.actualFinish = row.actualFinishDate
            task.lateStart = row.remainingLateStartDate
            task.lateFinish = row.remainingLateFinishDate
            task.earlyStart = row.remainingEarlyStartDate
            task.earlyFinish = row.remainingEarlyFinishDate
            task.baselineStart = row.plannedStartDate
            task.baselineFinish = row.plannedFinishDate

            task.priority = PRIORITY_MAP.get(row.levelingPriority)
            task.createDate = row.createDate
            task.setText(1, row.id)
            task.setText(2, row.type)
            task.setText(3, row.status)
            task.setNumber(1, row.primaryResourceObjectId)

            task.milestone = BooleanHelper.getBoolean(MILESTONE_MAP.get(row.type))
            task.critical = task.earlyStart != null && task.lateStart != null && task.lateStart.compareTo(task.earlyStart) <= 0

            if (parentTask != null) {
                task.wbs = parentTask.wbs
            }

            val calId = row.calendarObjectId
            val cal = m_calMap.get(calId)
            task.calendar = cal

            task.start = row.startDate
            task.finish = row.finishDate

            populateField(task, TaskField.START, TaskField.START, TaskField.ACTUAL_START, TaskField.BASELINE_START)
            populateField(task, TaskField.FINISH, TaskField.FINISH, TaskField.ACTUAL_FINISH)
            populateField(task, TaskField.WORK, TaskField.ACTUAL_WORK, TaskField.BASELINE_WORK)

            //
            // We've tried the finish and actual finish fields... but we still have null.
            // P6 itself doesn't export PMXML like this.
            // The sample I have that requires this code appears to have been been generated by Synchro.
            //
            if (task.finish == null) {
                //
                // Find the remaining duration, set it to null if it is zero
                //
                var duration: Duration? = task.remainingDuration
                if (duration != null && duration!!.getDuration() === 0) {
                    duration = null
                }

                //
                // If the task hasn't started, or we don't have a usable duration
                // let's just use the baseline finish.
                //
                if (task.actualStart == null || duration == null) {
                    task.finish = task.baselineFinish
                } else {
                    //
                    // The task has started, let's calculate the finish date using the remaining duration
                    // and the "restart" date, which we've put in the baseline start date.
                    //
                    val calendar = task.effectiveCalendar
                    var finish = calendar.getDate(task.baselineStart, duration!!, false)

                    //
                    // Deal with an oddity where the finish date shows up as the
                    // start of work date for the next working day. If we can identify this,
                    // wind the date back to the end of the previous working day.
                    //
                    val nextWorkStart = calendar.getNextWorkStart(finish)
                    if (DateHelper.compare(finish, nextWorkStart) == 0) {
                        finish = calendar.getPreviousWorkFinish(finish)
                    }
                    task.finish = finish
                }
            }

            readUDFTypes(task, row.getUDF())
            readActivityCodes(task, row.getCode())

            m_eventManager!!.fireTaskReadEvent(task)
        }

        ActivitySorter(TaskField.TEXT1, wbsTasks).sort(m_projectFile!!)

        updateStructure()
        updateDates()
    }

    /**
     * The Primavera WBS entries we read in as tasks have user-entered start and end dates
     * which aren't calculated or adjusted based on the child task dates. We try
     * to compensate for this by using these user-entered dates as baseline dates, and
     * deriving the planned start, actual start, planned finish and actual finish from
     * the child tasks. This method recursively descends through the tasks to do this.
     */
    private fun updateDates() {
        for (task in m_projectFile!!.childTasks) {
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
            var actualStartDate = parentTask.actualStart
            var actualFinishDate: Date? = parentTask.actualFinish
            var earlyStartDate: Date? = parentTask.earlyStart
            var earlyFinishDate: Date? = parentTask.earlyFinish
            var lateStartDate: Date? = parentTask.lateStart
            var lateFinishDate: Date? = parentTask.lateFinish
            var baselineStartDate: Date? = parentTask.baselineStart
            var baselineFinishDate: Date? = parentTask.baselineFinish
            var remainingEarlyStartDate: Date? = parentTask.remainingEarlyStart
            var remainingEarlyFinishDate: Date? = parentTask.remainingEarlyFinish

            for (task in parentTask.childTasks) {
                updateDates(task)

                // the child tasks can have null dates (e.g. for nested wbs elements with no task children) so we
                // still must protect against some children having null dates

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
                baselineDuration = m_projectFile!!.defaultCalendar!!.getWork(baselineStartDate, baselineFinishDate, TimeUnit.HOURS)
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
                    remainingDuration = m_projectFile!!.defaultCalendar!!.getWork(startDate, finishDate, TimeUnit.HOURS)
                }
            } else {
                remainingDuration = Duration.getInstance(0, TimeUnit.HOURS)
            }
            parentTask.remainingDuration = remainingDuration

            if (baselineDuration != null && remainingDuration != null && baselineDuration!!.getDuration() !== 0) {
                val durationPercentComplete = (baselineDuration!!.getDuration() - remainingDuration!!.getDuration()) / baselineDuration!!.getDuration() * 100.0
                parentTask.percentageComplete = Double.valueOf(durationPercentComplete)
            }
        }
    }

    /**
     * Populates a field based on baseline and actual values.
     *
     * @param container field container
     * @param target target field
     * @param types fields to test for not-null values
     */
    private fun populateField(container: FieldContainer, target: FieldType, vararg types: FieldType) {
        for (type in types) {
            val value = container.getCachedValue(type)
            if (value != null) {
                container.set(target, value)
                break
            }
        }
    }

    /**
     * Iterates through the tasks setting the correct
     * outline level and ID values.
     */
    private fun updateStructure() {
        var id = 1
        val outlineLevel = Integer.valueOf(1)
        for (task in m_projectFile!!.childTasks) {
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
     * Process predecessors.
     *
     * @param project xml container
     */
    private fun processPredecessors(project: ProjectType) {
        for (row in project.relationship) {

            val currentTask = m_projectFile!!.getTaskByUniqueID(mapTaskID(row.successorActivityObjectId))
            val predecessorTask = m_projectFile!!.getTaskByUniqueID(mapTaskID(row.predecessorActivityObjectId))
            if (currentTask != null && predecessorTask != null) {
                val type = RELATION_TYPE_MAP.get(row.type)
                val lag = getDuration(row.lag)
                val relation = currentTask.addPredecessor(predecessorTask, type, lag)
                relation.uniqueID = row.objectId
                m_eventManager!!.fireRelationReadEvent(relation)
            }
        }
    }

    /**
     * Process resource assignments.
     *
     * @param project xml container
     */
    private fun processAssignments(project: ProjectType) {
        val assignments = project.resourceAssignment
        for (row in assignments) {
            val task = m_projectFile!!.getTaskByUniqueID(mapTaskID(row.activityObjectId))
            val resource = m_projectFile!!.getResourceByUniqueID(row.resourceObjectId)
            if (task != null && resource != null) {
                val assignment = task.addResourceAssignment(resource)

                assignment.uniqueID = row.objectId
                assignment.remainingWork = getDuration(row.remainingUnits)
                assignment.baselineWork = getDuration(row.plannedUnits)
                assignment.actualWork = getDuration(row.actualUnits)
                assignment.remainingCost = row.remainingCost
                assignment.baselineCost = row.plannedCost
                assignment.actualCost = row.actualCost
                assignment.actualStart = row.actualStartDate
                assignment.actualFinish = row.actualFinishDate
                assignment.baselineStart = row.plannedStartDate
                assignment.baselineFinish = row.plannedFinishDate
                assignment.guid = DatatypeConverter.parseUUID(row.guid)

                task.actualCost = Double.valueOf(NumberHelper.getDouble(task.actualCost) + NumberHelper.getDouble(assignment.actualCost))
                task.remainingCost = Double.valueOf(NumberHelper.getDouble(task.remainingCost) + NumberHelper.getDouble(assignment.remainingCost))
                task.baselineCost = Double.valueOf(NumberHelper.getDouble(task.baselineCost) + NumberHelper.getDouble(assignment.baselineCost))

                populateField(assignment, AssignmentField.WORK, AssignmentField.ACTUAL_WORK, AssignmentField.BASELINE_WORK)
                populateField(assignment, AssignmentField.COST, AssignmentField.ACTUAL_COST, AssignmentField.BASELINE_COST)
                populateField(assignment, AssignmentField.START, AssignmentField.ACTUAL_START, AssignmentField.BASELINE_START)
                populateField(assignment, AssignmentField.FINISH, AssignmentField.ACTUAL_FINISH, AssignmentField.BASELINE_FINISH)

                readUDFTypes(assignment, row.getUDF())

                m_eventManager!!.fireAssignmentReadEvent(assignment)
            }
        }
    }

    /**
     * Render a zero Double as null.
     *
     * @param value double value
     * @return null if the double value is zero
     */
    private fun zeroIsNull(value: Double?): Double? {
        var value = value
        if (value != null && value.doubleValue() === 0) {
            value = null
        }
        return value
    }

    /**
     * Extracts a duration from a JAXBElement instance.
     *
     * @param duration duration expressed in hours
     * @return duration instance
     */
    private fun getDuration(duration: Double?): Duration? {
        var result: Duration? = null

        if (duration != null) {
            result = Duration.getInstance(NumberHelper.getDouble(duration), TimeUnit.HOURS)
        }

        return result
    }

    /**
     * The end of a Primavera time range finishes on the last minute
     * of the period, so a range of 12:00 -> 13:00 is represented by
     * Primavera as 12:00 -> 12:59.
     *
     * @param date Primavera end time
     * @return date MPXJ end time
     */
    private fun getEndTime(date: Date): Date {
        return Date(date.getTime() + 60000)
    }

    /**
     * Reverse the effects of PrimaveraPMFileWriter.getPercentage().
     *
     * @param n percentage value to convert
     * @return percentage value usable by MPXJ
     */
    private fun reversePercentage(n: Double?): Number? {
        return if (n == null) null else NumberHelper.getDouble(n.doubleValue() * 100.0)
    }

    /**
     * Process UDFs for a specific object.
     *
     * @param mpxj field container
     * @param udfs UDF values
     */
    private fun readUDFTypes(mpxj: FieldContainer, udfs: List<UDFAssignmentType>) {
        for (udf in udfs) {
            val fieldType = m_fieldTypeMap.get(Integer.valueOf(udf.typeObjectId))
            if (fieldType != null) {
                mpxj.set(fieldType, getUdfValue(udf))
            }
        }
    }

    /**
     * Retrieve the value of a UDF.
     *
     * @param udf UDF value holder
     * @return UDF value
     */
    private fun getUdfValue(udf: UDFAssignmentType): Object? {
        if (udf.costValue != null) {
            return udf.costValue
        }

        if (udf.doubleValue != null) {
            return udf.doubleValue
        }

        if (udf.finishDateValue != null) {
            return udf.finishDateValue
        }

        if (udf.indicatorValue != null) {
            return udf.indicatorValue
        }

        if (udf.integerValue != null) {
            return udf.integerValue
        }

        if (udf.startDateValue != null) {
            return udf.startDateValue
        }

        return if (udf.textValue != null) {
            udf.textValue
        } else null

    }

    /**
     * Read details of any activity codes assigned to this task.
     * @param task parent task
     * @param codes activity code assignments
     */
    private fun readActivityCodes(task: Task, codes: List<CodeAssignmentType>) {
        for (assignment in codes) {
            val code = m_activityCodeMap.get(Integer.valueOf(assignment.valueObjectId))
            if (code != null) {
                task.addActivityCode(code)
            }
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

    companion object {

        /**
         * Cached context to minimise construction cost.
         */
        private var CONTEXT: JAXBContext? = null

        /**
         * Note any error occurring during context construction.
         */
        private var CONTEXT_EXCEPTION: JAXBException? = null

        init {
            try {
                //
                // JAXB RI property to speed up construction
                //
                System.setProperty("com.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot", "true")

                //
                // Construct the context
                //
                CONTEXT = JAXBContext.newInstance("net.sf.mpxj.primavera.schema", PrimaveraPMFileReader::class.java!!.getClassLoader())
            } catch (ex: JAXBException) {
                CONTEXT_EXCEPTION = ex
                CONTEXT = null
            }

        }

        private val RESOURCE_TYPE_MAP = HashMap<String, net.sf.mpxj.ResourceType>()

        init {
            RESOURCE_TYPE_MAP.put(null, net.sf.mpxj.ResourceType.WORK)
            RESOURCE_TYPE_MAP.put("Labor", net.sf.mpxj.ResourceType.WORK)
            RESOURCE_TYPE_MAP.put("Material", net.sf.mpxj.ResourceType.MATERIAL)
            RESOURCE_TYPE_MAP.put("Nonlabor", net.sf.mpxj.ResourceType.MATERIAL)
        }

        private val CONSTRAINT_TYPE_MAP = HashMap<String, ConstraintType>()

        init {
            CONSTRAINT_TYPE_MAP.put("Start On", ConstraintType.MUST_START_ON)
            CONSTRAINT_TYPE_MAP.put("Start On or Before", ConstraintType.START_NO_LATER_THAN)
            CONSTRAINT_TYPE_MAP.put("Start On or After", ConstraintType.START_NO_EARLIER_THAN)
            CONSTRAINT_TYPE_MAP.put("Finish On", ConstraintType.MUST_FINISH_ON)
            CONSTRAINT_TYPE_MAP.put("Finish On or Before", ConstraintType.FINISH_NO_LATER_THAN)
            CONSTRAINT_TYPE_MAP.put("Finish On or After", ConstraintType.FINISH_NO_EARLIER_THAN)
            CONSTRAINT_TYPE_MAP.put("As Late As Possible", ConstraintType.AS_LATE_AS_POSSIBLE)
            CONSTRAINT_TYPE_MAP.put("Mandatory Start", ConstraintType.MUST_START_ON)
            CONSTRAINT_TYPE_MAP.put("Mandatory Finish", ConstraintType.MUST_FINISH_ON)
        }

        private val PRIORITY_MAP = HashMap<String, Priority>()

        init {
            PRIORITY_MAP.put("Top", Priority.getInstance(Priority.HIGHEST))
            PRIORITY_MAP.put("High", Priority.getInstance(Priority.HIGH))
            PRIORITY_MAP.put("Normal", Priority.getInstance(Priority.MEDIUM))
            PRIORITY_MAP.put("Low", Priority.getInstance(Priority.LOW))
            PRIORITY_MAP.put("Lowest", Priority.getInstance(Priority.LOWEST))
        }

        private val RELATION_TYPE_MAP = HashMap<String, RelationType>()

        init {
            RELATION_TYPE_MAP.put("Finish to Start", RelationType.FINISH_START)
            RELATION_TYPE_MAP.put("Finish to Finish", RelationType.FINISH_FINISH)
            RELATION_TYPE_MAP.put("Start to Start", RelationType.START_START)
            RELATION_TYPE_MAP.put("Start to Finish", RelationType.START_FINISH)
        }

        private val DAY_MAP = HashMap<String, Day>()

        init {
            // Current PMXML schema
            DAY_MAP.put("Monday", Day.MONDAY)
            DAY_MAP.put("Tuesday", Day.TUESDAY)
            DAY_MAP.put("Wednesday", Day.WEDNESDAY)
            DAY_MAP.put("Thursday", Day.THURSDAY)
            DAY_MAP.put("Friday", Day.FRIDAY)
            DAY_MAP.put("Saturday", Day.SATURDAY)
            DAY_MAP.put("Sunday", Day.SUNDAY)

            // Older (6.2?) schema
            DAY_MAP.put("1", Day.SUNDAY)
            DAY_MAP.put("2", Day.MONDAY)
            DAY_MAP.put("3", Day.TUESDAY)
            DAY_MAP.put("4", Day.WEDNESDAY)
            DAY_MAP.put("5", Day.THURSDAY)
            DAY_MAP.put("6", Day.FRIDAY)
            DAY_MAP.put("7", Day.SATURDAY)
        }

        private val MILESTONE_MAP = HashMap<String, Boolean>()

        init {
            MILESTONE_MAP.put("Task Dependent", Boolean.FALSE)
            MILESTONE_MAP.put("Resource Dependent", Boolean.FALSE)
            MILESTONE_MAP.put("Level of Effort", Boolean.FALSE)
            MILESTONE_MAP.put("Start Milestone", Boolean.TRUE)
            MILESTONE_MAP.put("Finish Milestone", Boolean.TRUE)
            MILESTONE_MAP.put("WBS Summary", Boolean.FALSE)
        }

        private val FIELD_TYPE_MAP = HashMap<String, FieldTypeClass>()

        init {
            FIELD_TYPE_MAP.put("Activity", FieldTypeClass.TASK)
            FIELD_TYPE_MAP.put("WBS", FieldTypeClass.TASK)
            FIELD_TYPE_MAP.put("Resource", FieldTypeClass.RESOURCE)
            FIELD_TYPE_MAP.put("Resource Assignment", FieldTypeClass.ASSIGNMENT)
        }

        private val RESERVED_TASK_FIELDS = HashSet<TaskField>()

        init {
            RESERVED_TASK_FIELDS.add(TaskField.TEXT1)
            RESERVED_TASK_FIELDS.add(TaskField.TEXT2)

        }

        private val WBS_ROW_COMPARATOR = WbsRowComparatorPMXML()
    }
}
