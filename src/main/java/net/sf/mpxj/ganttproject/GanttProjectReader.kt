/*
 * file:       GanttProjectReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       22 March 2017
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

package net.sf.mpxj.ganttproject

import java.io.InputStream
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.LinkedList
import java.util.Locale

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.sax.SAXSource

import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader

import net.sf.mpxj.ChildTaskContainer
import net.sf.mpxj.ConstraintType
import net.sf.mpxj.CustomField
import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.FieldType
import net.sf.mpxj.MPXJException
import net.sf.mpxj.Priority
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectCalendarWeek
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Rate
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceField
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.Pair
import net.sf.mpxj.common.ResourceFieldLists
import net.sf.mpxj.common.TaskFieldLists
import net.sf.mpxj.ganttproject.schema.Allocation
import net.sf.mpxj.ganttproject.schema.Allocations
import net.sf.mpxj.ganttproject.schema.Calendars
import net.sf.mpxj.ganttproject.schema.CustomPropertyDefinition
import net.sf.mpxj.ganttproject.schema.CustomResourceProperty
import net.sf.mpxj.ganttproject.schema.CustomTaskProperty
import net.sf.mpxj.ganttproject.schema.DayTypes
import net.sf.mpxj.ganttproject.schema.DefaultWeek
import net.sf.mpxj.ganttproject.schema.Depend
import net.sf.mpxj.ganttproject.schema.Project
import net.sf.mpxj.ganttproject.schema.Resources
import net.sf.mpxj.ganttproject.schema.Role
import net.sf.mpxj.ganttproject.schema.Roles
import net.sf.mpxj.ganttproject.schema.Taskproperty
import net.sf.mpxj.ganttproject.schema.Tasks
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * This class creates a new ProjectFile instance by reading a GanttProject file.
 */
class GanttProjectReader : AbstractProjectReader() {

    private var m_projectFile: ProjectFile? = null
    private var m_mpxjCalendar: ProjectCalendar? = null
    private var m_eventManager: EventManager? = null
    private var m_projectListeners: List<ProjectListener>? = null
    private var m_localeDateFormat: DateFormat? = null
    private var m_dateFormat: DateFormat? = null
    private var m_resourcePropertyDefinitions: Map<String, Pair<FieldType, String>>? = null
    private var m_taskPropertyDefinitions: Map<String, Pair<FieldType, String>>? = null
    private var m_roleDefinitions: Map<String, String>? = null
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
            m_resourcePropertyDefinitions = HashMap<String, Pair<FieldType, String>>()
            m_taskPropertyDefinitions = HashMap<String, Pair<FieldType, String>>()
            m_roleDefinitions = HashMap<String, String>()
            m_dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")

            val config = m_projectFile!!.projectConfig
            config.autoResourceUniqueID = false
            config.autoTaskUniqueID = false
            config.autoOutlineLevel = true
            config.autoOutlineNumber = true
            config.autoWBS = true

            m_projectFile!!.projectProperties.fileApplication = "GanttProject"
            m_projectFile!!.projectProperties.fileType = "GAN"

            m_eventManager!!.addProjectListeners(m_projectListeners)

            val factory = SAXParserFactory.newInstance()
            val saxParser = factory.newSAXParser()
            val xmlReader = saxParser.getXMLReader()
            val doc = SAXSource(xmlReader, InputSource(stream))

            if (CONTEXT == null) {
                throw CONTEXT_EXCEPTION
            }

            val unmarshaller = CONTEXT!!.createUnmarshaller()

            val ganttProject = unmarshaller.unmarshal(doc) as Project

            readProjectProperties(ganttProject)
            readCalendars(ganttProject)
            readResources(ganttProject)
            readTasks(ganttProject)
            readRelationships(ganttProject)
            readResourceAssignments(ganttProject)

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
        } finally {
            m_projectFile = null
            m_mpxjCalendar = null
            m_eventManager = null
            m_projectListeners = null
            m_localeDateFormat = null
            m_resourcePropertyDefinitions = null
            m_taskPropertyDefinitions = null
            m_roleDefinitions = null
        }
    }

    /**
     * This method extracts project properties from a GanttProject file.
     *
     * @param ganttProject GanttProject file
     */
    private fun readProjectProperties(ganttProject: Project) {
        val mpxjProperties = m_projectFile!!.projectProperties
        mpxjProperties.name = ganttProject.name
        mpxjProperties.company = ganttProject.company
        mpxjProperties.defaultDurationUnits = TimeUnit.DAYS

        var locale: String? = ganttProject.locale
        if (locale == null) {
            locale = "en_US"
        }
        m_localeDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale(locale))
    }

    /**
     * This method extracts calendar data from a GanttProject file.
     *
     * @param ganttProject Root node of the GanttProject file
     */
    private fun readCalendars(ganttProject: Project) {
        m_mpxjCalendar = m_projectFile!!.addCalendar()
        m_mpxjCalendar!!.name = ProjectCalendar.DEFAULT_BASE_CALENDAR_NAME

        val gpCalendar = ganttProject.calendars
        setWorkingDays(m_mpxjCalendar, gpCalendar)
        setExceptions(m_mpxjCalendar, gpCalendar)
        m_eventManager!!.fireCalendarReadEvent(m_mpxjCalendar)
    }

    /**
     * Add working days and working time to a calendar.
     *
     * @param mpxjCalendar MPXJ calendar
     * @param gpCalendar GanttProject calendar
     */
    private fun setWorkingDays(mpxjCalendar: ProjectCalendar, gpCalendar: Calendars) {
        val dayTypes = gpCalendar.dayTypes
        val defaultWeek = dayTypes.defaultWeek
        if (defaultWeek == null) {
            mpxjCalendar.setWorkingDay(Day.SUNDAY, false)
            mpxjCalendar.setWorkingDay(Day.MONDAY, true)
            mpxjCalendar.setWorkingDay(Day.TUESDAY, true)
            mpxjCalendar.setWorkingDay(Day.WEDNESDAY, true)
            mpxjCalendar.setWorkingDay(Day.THURSDAY, true)
            mpxjCalendar.setWorkingDay(Day.FRIDAY, true)
            mpxjCalendar.setWorkingDay(Day.SATURDAY, false)
        } else {
            mpxjCalendar.setWorkingDay(Day.MONDAY, isWorkingDay(defaultWeek.mon))
            mpxjCalendar.setWorkingDay(Day.TUESDAY, isWorkingDay(defaultWeek.tue))
            mpxjCalendar.setWorkingDay(Day.WEDNESDAY, isWorkingDay(defaultWeek.wed))
            mpxjCalendar.setWorkingDay(Day.THURSDAY, isWorkingDay(defaultWeek.thu))
            mpxjCalendar.setWorkingDay(Day.FRIDAY, isWorkingDay(defaultWeek.fri))
            mpxjCalendar.setWorkingDay(Day.SATURDAY, isWorkingDay(defaultWeek.sat))
            mpxjCalendar.setWorkingDay(Day.SUNDAY, isWorkingDay(defaultWeek.sun))
        }

        for (day in Day.values()) {
            if (mpxjCalendar.isWorkingDay(day)) {
                val hours = mpxjCalendar.addCalendarHours(day)
                hours.addRange(ProjectCalendarWeek.DEFAULT_WORKING_MORNING)
                hours.addRange(ProjectCalendarWeek.DEFAULT_WORKING_AFTERNOON)
            }
        }
    }

    /**
     * Returns true if the flag indicates a working day.
     *
     * @param value flag value
     * @return true if this is a working day
     */
    private fun isWorkingDay(value: Integer): Boolean {
        return NumberHelper.getInt(value) == 0
    }

    /**
     * Add exceptions to the calendar.
     *
     * @param mpxjCalendar MPXJ calendar
     * @param gpCalendar GanttProject calendar
     */
    private fun setExceptions(mpxjCalendar: ProjectCalendar, gpCalendar: Calendars) {
        val dates = gpCalendar.date
        for (date in dates) {
            addException(mpxjCalendar, date)
        }
    }

    /**
     * Add a single exception to a calendar.
     *
     * @param mpxjCalendar MPXJ calendar
     * @param date calendar exception
     */
    private fun addException(mpxjCalendar: ProjectCalendar, date: net.sf.mpxj.ganttproject.schema.Date) {
        val year = date.year
        if (year == null || year.isEmpty()) {
            // In order to process recurring exceptions using MPXJ, we need a start and end date
            // to constrain the number of dates we generate.
            // May need to pre-process the tasks in order to calculate a start and finish date.
            // TODO: handle recurring exceptions
        } else {
            val calendar = DateHelper.popCalendar()
            calendar.set(Calendar.YEAR, Integer.parseInt(year))
            calendar.set(Calendar.MONTH, NumberHelper.getInt(date.month))
            calendar.set(Calendar.DAY_OF_MONTH, NumberHelper.getInt(date.date))
            val exceptionDate = calendar.getTime()
            DateHelper.pushCalendar(calendar)
            val exception = mpxjCalendar.addCalendarException(exceptionDate, exceptionDate)

            // TODO: not sure how NEUTRAL should be handled
            if ("WORKING_DAY".equals(date.type)) {
                exception.addRange(ProjectCalendarWeek.DEFAULT_WORKING_MORNING)
                exception.addRange(ProjectCalendarWeek.DEFAULT_WORKING_AFTERNOON)
            }
        }
    }

    /**
     * This method extracts resource data from a GanttProject file.
     *
     * @param ganttProject parent node for resources
     */
    private fun readResources(ganttProject: Project) {
        val resources = ganttProject.resources
        readResourceCustomPropertyDefinitions(resources)
        readRoleDefinitions(ganttProject)

        for (gpResource in resources.resource) {
            readResource(gpResource)
        }
    }

    /**
     * Read custom property definitions for resources.
     *
     * @param gpResources GanttProject resources
     */
    private fun readResourceCustomPropertyDefinitions(gpResources: Resources) {
        var field = m_projectFile!!.customFields.getCustomField(ResourceField.TEXT1)
        field.setAlias("Phone")

        for (definition in gpResources.customPropertyDefinition) {
            //
            // Find the next available field of the correct type.
            //
            val type = definition.type
            var fieldType = RESOURCE_PROPERTY_TYPES.get(type).getField()

            //
            // If we have run out of fields of the right type, try using a text field.
            //
            if (fieldType == null) {
                fieldType = RESOURCE_PROPERTY_TYPES.get("text").getField()
            }

            //
            // If we actually have a field available, set the alias to match
            // the name used in GanttProject.
            //
            if (fieldType != null) {
                field = m_projectFile!!.customFields.getCustomField(fieldType)
                field.setAlias(definition.name)
                var defaultValue: String? = definition.defaultValue
                if (defaultValue != null && defaultValue.isEmpty()) {
                    defaultValue = null
                }
                m_resourcePropertyDefinitions!!.put(definition.id, Pair<FieldType, String>(fieldType, defaultValue))
            }
        }
    }

    /**
     * Read custom property definitions for tasks.
     *
     * @param gpTasks GanttProject tasks
     */
    private fun readTaskCustomPropertyDefinitions(gpTasks: Tasks) {
        for (definition in gpTasks.taskproperties.taskproperty) {
            //
            // Ignore everything but custom values
            //
            if (!"custom".equals(definition.type)) {
                continue
            }

            //
            // Find the next available field of the correct type.
            //
            val type = definition.valuetype
            var fieldType = TASK_PROPERTY_TYPES.get(type).getField()

            //
            // If we have run out of fields of the right type, try using a text field.
            //
            if (fieldType == null) {
                fieldType = TASK_PROPERTY_TYPES.get("text").getField()
            }

            //
            // If we actually have a field available, set the alias to match
            // the name used in GanttProject.
            //
            if (fieldType != null) {
                val field = m_projectFile!!.customFields.getCustomField(fieldType)
                field.setAlias(definition.name)
                var defaultValue: String? = definition.defaultvalue
                if (defaultValue != null && defaultValue.isEmpty()) {
                    defaultValue = null
                }
                m_taskPropertyDefinitions!!.put(definition.id, Pair<FieldType, String>(fieldType, defaultValue))
            }
        }
    }

    /**
     * Read the role definitions from a GanttProject project.
     *
     * @param gpProject GanttProject project
     */
    private fun readRoleDefinitions(gpProject: Project) {
        m_roleDefinitions!!.put("Default:1", "project manager")

        for (roles in gpProject.roles) {
            if ("Default".equals(roles.rolesetName)) {
                continue
            }

            for (role in roles.getRole()) {
                m_roleDefinitions!!.put(role.id, role.name)
            }
        }
    }

    /**
     * This method extracts data for a single resource from a GanttProject file.
     *
     * @param gpResource resource data
     */
    private fun readResource(gpResource: net.sf.mpxj.ganttproject.schema.Resource) {
        val mpxjResource = m_projectFile!!.addResource()
        mpxjResource.uniqueID = Integer.valueOf(NumberHelper.getInt(gpResource.id) + 1)
        mpxjResource.name = gpResource.name
        mpxjResource.emailAddress = gpResource.contacts
        mpxjResource.setText(1, gpResource.phone)
        mpxjResource.group = m_roleDefinitions!!.get(gpResource.function)

        val gpRate = gpResource.rate
        if (gpRate != null) {
            mpxjResource.standardRate = Rate(gpRate.valueAttribute, TimeUnit.DAYS)
        }
        readResourceCustomFields(gpResource, mpxjResource)
        m_eventManager!!.fireResourceReadEvent(mpxjResource)
    }

    /**
     * Read custom fields for a GanttProject resource.
     *
     * @param gpResource GanttProject resource
     * @param mpxjResource MPXJ Resource instance
     */
    private fun readResourceCustomFields(gpResource: net.sf.mpxj.ganttproject.schema.Resource, mpxjResource: Resource) {
        //
        // Populate custom field default values
        //
        val customFields = HashMap<FieldType, Object>()
        for (definition in m_resourcePropertyDefinitions!!.values()) {
            customFields.put(definition.first, definition.second)
        }

        //
        // Update with custom field actual values
        //
        for (property in gpResource.customProperty) {
            val definition = m_resourcePropertyDefinitions!![property.definitionId]
            if (definition != null) {
                //
                // Retrieve the value. If it is empty, use the default.
                //
                var value: String? = property.valueAttribute
                if (value!!.isEmpty()) {
                    value = null
                }

                //
                // If we have a value,convert it to the correct type
                //
                if (value != null) {
                    var result: Object?

                    when (definition.first!!.getDataType()) {
                        NUMERIC -> {
                            if (value.indexOf('.') === -1) {
                                result = Integer.valueOf(value)
                            } else {
                                result = Double.valueOf(value)
                            }
                        }

                        DATE -> {
                            try {
                                result = m_localeDateFormat!!.parse(value)
                            } catch (ex: ParseException) {
                                result = null
                            }

                        }

                        BOOLEAN -> {
                            result = Boolean.valueOf(value.equals("true"))
                        }

                        else -> {
                            result = value
                        }
                    }

                    if (result != null) {
                        customFields.put(definition.first, result)
                    }
                }
            }
        }

        for (item in customFields.entrySet()) {
            if (item.getValue() != null) {
                mpxjResource.set(item.getKey(), item.getValue())
            }
        }
    }

    /**
     * Read custom fields for a GanttProject task.
     *
     * @param gpTask GanttProject task
     * @param mpxjTask MPXJ Task instance
     */
    private fun readTaskCustomFields(gpTask: net.sf.mpxj.ganttproject.schema.Task, mpxjTask: Task) {
        //
        // Populate custom field default values
        //
        val customFields = HashMap<FieldType, Object>()
        for (definition in m_taskPropertyDefinitions!!.values()) {
            customFields.put(definition.first, definition.second)
        }

        //
        // Update with custom field actual values
        //
        for (property in gpTask.customproperty) {
            val definition = m_taskPropertyDefinitions!![property.taskpropertyId]
            if (definition != null) {
                //
                // Retrieve the value. If it is empty, use the default.
                //
                var value: String? = property.valueAttribute
                if (value!!.isEmpty()) {
                    value = null
                }

                //
                // If we have a value,convert it to the correct type
                //
                if (value != null) {
                    var result: Object?

                    when (definition.first!!.getDataType()) {
                        NUMERIC -> {
                            if (value.indexOf('.') === -1) {
                                result = Integer.valueOf(value)
                            } else {
                                result = Double.valueOf(value)
                            }
                        }

                        DATE -> {
                            try {
                                result = m_dateFormat!!.parse(value)
                            } catch (ex: ParseException) {
                                result = null
                            }

                        }

                        BOOLEAN -> {
                            result = Boolean.valueOf(value.equals("true"))
                        }

                        else -> {
                            result = value
                        }
                    }

                    if (result != null) {
                        customFields.put(definition.first, result)
                    }
                }
            }
        }

        for (item in customFields.entrySet()) {
            if (item.getValue() != null) {
                mpxjTask.set(item.getKey(), item.getValue())
            }
        }
    }

    /**
     * Read the top level tasks from GanttProject.
     *
     * @param gpProject GanttProject project
     */
    private fun readTasks(gpProject: Project) {
        val tasks = gpProject.tasks
        readTaskCustomPropertyDefinitions(tasks)
        for (task in tasks.task) {
            readTask(m_projectFile!!, task)
        }
    }

    /**
     * Recursively read a task, and any sub tasks.
     *
     * @param mpxjParent Parent for the MPXJ tasks
     * @param gpTask GanttProject task
     */
    private fun readTask(mpxjParent: ChildTaskContainer, gpTask: net.sf.mpxj.ganttproject.schema.Task) {
        val mpxjTask = mpxjParent.addTask()
        mpxjTask.uniqueID = Integer.valueOf(NumberHelper.getInt(gpTask.id) + 1)
        mpxjTask.name = gpTask.name
        mpxjTask.percentageComplete = gpTask.complete
        mpxjTask.priority = getPriority(gpTask.priority)
        mpxjTask.hyperlink = gpTask.webLink

        val duration = Duration.getInstance(NumberHelper.getDouble(gpTask.duration), TimeUnit.DAYS)
        mpxjTask.duration = duration

        if (duration.getDuration() === 0) {
            mpxjTask.milestone = true
        } else {
            mpxjTask.start = gpTask.start
            mpxjTask.finish = m_mpxjCalendar!!.getDate(gpTask.start, mpxjTask.duration!!, false)
        }

        mpxjTask.constraintDate = gpTask.thirdDate
        if (mpxjTask.constraintDate != null) {
            // TODO: you don't appear to be able to change this setting in GanttProject
            // task.getThirdDateConstraint()
            mpxjTask.constraintType = ConstraintType.START_NO_EARLIER_THAN
        }

        readTaskCustomFields(gpTask, mpxjTask)

        m_eventManager!!.fireTaskReadEvent(mpxjTask)

        // TODO: read custom values

        //
        // Process child tasks
        //
        for (childTask in gpTask.task) {
            readTask(mpxjTask, childTask)
        }
    }

    /**
     * Given a GanttProject priority value, turn this into an MPXJ Priority instance.
     *
     * @param gpPriority GanttProject priority
     * @return Priority instance
     */
    private fun getPriority(gpPriority: Integer?): Priority {
        val result: Int
        if (gpPriority == null) {
            result = Priority.MEDIUM
        } else {
            val index = gpPriority!!.intValue()
            if (index < 0 || index >= PRIORITY.size) {
                result = Priority.MEDIUM
            } else {
                result = PRIORITY[index]
            }
        }
        return Priority.getInstance(result)
    }

    /**
     * Read all task relationships from a GanttProject.
     *
     * @param gpProject GanttProject project
     */
    private fun readRelationships(gpProject: Project) {
        for (gpTask in gpProject.tasks.task) {
            readRelationships(gpTask)
        }
    }

    /**
     * Read the relationships for an individual GanttProject task.
     *
     * @param gpTask GanttProject task
     */
    private fun readRelationships(gpTask: net.sf.mpxj.ganttproject.schema.Task) {
        for (depend in gpTask.depend) {
            val task1 = m_projectFile!!.getTaskByUniqueID(Integer.valueOf(NumberHelper.getInt(gpTask.id) + 1))
            val task2 = m_projectFile!!.getTaskByUniqueID(Integer.valueOf(NumberHelper.getInt(depend.id) + 1))
            if (task1 != null && task2 != null) {
                val lag = Duration.getInstance(NumberHelper.getInt(depend.difference), TimeUnit.DAYS)
                val relation = task2.addPredecessor(task1, getRelationType(depend.type), lag)
                m_eventManager!!.fireRelationReadEvent(relation)
            }
        }
    }

    /**
     * Convert a GanttProject task relationship type into an MPXJ RelationType instance.
     *
     * @param gpType GanttProject task relation type
     * @return RelationType instance
     */
    private fun getRelationType(gpType: Integer?): RelationType {
        var result: RelationType? = null
        if (gpType != null) {
            val index = NumberHelper.getInt(gpType)
            if (index > 0 && index < RELATION.size) {
                result = RELATION[index]
            }
        }

        if (result == null) {
            result = RelationType.FINISH_START
        }

        return result
    }

    /**
     * Read all resource assignments from a GanttProject project.
     *
     * @param gpProject GanttProject project
     */
    private fun readResourceAssignments(gpProject: Project) {
        val allocations = gpProject.allocations
        if (allocations != null) {
            for (allocation in allocations.allocation) {
                readResourceAssignment(allocation)
            }
        }
    }

    /**
     * Read an individual GanttProject resource assignment.
     *
     * @param gpAllocation GanttProject resource assignment.
     */
    private fun readResourceAssignment(gpAllocation: Allocation) {
        val taskID = Integer.valueOf(NumberHelper.getInt(gpAllocation.taskId) + 1)
        val resourceID = Integer.valueOf(NumberHelper.getInt(gpAllocation.resourceId) + 1)
        val task = m_projectFile!!.getTaskByUniqueID(taskID)
        val resource = m_projectFile!!.getResourceByUniqueID(resourceID)
        if (task != null && resource != null) {
            val mpxjAssignment = task.addResourceAssignment(resource)
            mpxjAssignment.units = gpAllocation.load
            m_eventManager!!.fireAssignmentReadEvent(mpxjAssignment)
        }
    }

    companion object {

        private val RESOURCE_PROPERTY_TYPES = HashMap<String, CustomProperty>()

        init {
            val numeric = CustomProperty(ResourceFieldLists.CUSTOM_NUMBER)
            RESOURCE_PROPERTY_TYPES.put("int", numeric)
            RESOURCE_PROPERTY_TYPES.put("double", numeric)
            RESOURCE_PROPERTY_TYPES.put("text", CustomProperty(ResourceFieldLists.CUSTOM_TEXT, 1))
            RESOURCE_PROPERTY_TYPES.put("date", CustomProperty(ResourceFieldLists.CUSTOM_DATE))
            RESOURCE_PROPERTY_TYPES.put("boolean", CustomProperty(ResourceFieldLists.CUSTOM_FLAG))
        }

        private val TASK_PROPERTY_TYPES = HashMap<String, CustomProperty>()

        init {
            val numeric = CustomProperty(TaskFieldLists.CUSTOM_NUMBER)
            TASK_PROPERTY_TYPES.put("int", numeric)
            TASK_PROPERTY_TYPES.put("double", numeric)
            TASK_PROPERTY_TYPES.put("text", CustomProperty(TaskFieldLists.CUSTOM_TEXT))
            TASK_PROPERTY_TYPES.put("date", CustomProperty(TaskFieldLists.CUSTOM_DATE))
            TASK_PROPERTY_TYPES.put("boolean", CustomProperty(TaskFieldLists.CUSTOM_FLAG))
        }

        private val PRIORITY = intArrayOf(Priority.LOW, // 0 - Low
                Priority.MEDIUM, // 1 - Normal
                Priority.HIGH, // 2 - High
                Priority.LOWEST, // 3- Lowest
                Priority.HIGHEST)// 4 - Highest

        internal val RELATION = arrayOf<RelationType>(null, //0
                RelationType.START_START, // 1 - Start Start
                RelationType.FINISH_START, // 2 - Finish Start
                RelationType.FINISH_FINISH, // 3 - Finish Finish
                RelationType.START_FINISH // 4 - Start Finish
        )

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
                CONTEXT = JAXBContext.newInstance("net.sf.mpxj.ganttproject.schema", GanttProjectReader::class.java!!.getClassLoader())
            } catch (ex: JAXBException) {
                CONTEXT_EXCEPTION = ex
                CONTEXT = null
            }

        }
    }
}
