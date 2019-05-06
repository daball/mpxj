/*
 * file:       MSPDIWriter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       2005-12-30
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

package net.sf.mpxj.mspdi

import java.io.IOException
import java.io.OutputStream
import java.math.BigInteger
import java.util.ArrayList
import java.util.Arrays
import java.util.Calendar
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.HashSet
import java.util.LinkedList

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller

import net.sf.mpxj.AccrueType
import net.sf.mpxj.AssignmentField
import net.sf.mpxj.Availability
import net.sf.mpxj.CostRateTable
import net.sf.mpxj.CostRateTableEntry
import net.sf.mpxj.CustomField
import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.DayType
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.FieldType
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectCalendarWeek
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.RecurringData
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceField
import net.sf.mpxj.ResourceType
import net.sf.mpxj.ScheduleFrom
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.TaskMode
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.TimephasedWork
import net.sf.mpxj.common.AssignmentFieldLists
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.FieldTypeHelper
import net.sf.mpxj.common.MPPAssignmentField
import net.sf.mpxj.common.MPPResourceField
import net.sf.mpxj.common.MPPTaskField
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.ResourceFieldLists
import net.sf.mpxj.common.TaskFieldLists
import net.sf.mpxj.mspdi.schema.ObjectFactory
import net.sf.mpxj.mspdi.schema.Project
import net.sf.mpxj.mspdi.schema.Project.Calendars.Calendar.Exceptions
import net.sf.mpxj.mspdi.schema.Project.Calendars.Calendar.WorkWeeks
import net.sf.mpxj.mspdi.schema.Project.Calendars.Calendar.WorkWeeks.WorkWeek
import net.sf.mpxj.mspdi.schema.Project.Calendars.Calendar.WorkWeeks.WorkWeek.TimePeriod
import net.sf.mpxj.mspdi.schema.Project.Calendars.Calendar.WorkWeeks.WorkWeek.WeekDays
import net.sf.mpxj.mspdi.schema.Project.Resources.Resource.AvailabilityPeriods
import net.sf.mpxj.mspdi.schema.Project.Resources.Resource.AvailabilityPeriods.AvailabilityPeriod
import net.sf.mpxj.mspdi.schema.Project.Resources.Resource.Rates
import net.sf.mpxj.mspdi.schema.TimephasedDataType
import net.sf.mpxj.writer.AbstractProjectWriter

/**
 * This class creates a new MSPDI file from the contents of an ProjectFile instance.
 */
class MSPDIWriter : AbstractProjectWriter() {

    /**
     * Retrieve list of assignment extended attributes.
     *
     * @return list of extended attributes
     */
    private val allAssignmentExtendedAttributes: List<AssignmentField>
        get() {
            val result = ArrayList<AssignmentField>()
            result.addAll(Arrays.asList(AssignmentFieldLists.CUSTOM_COST))
            result.addAll(Arrays.asList(AssignmentFieldLists.CUSTOM_DATE))
            result.addAll(Arrays.asList(AssignmentFieldLists.CUSTOM_DURATION))
            result.addAll(Arrays.asList(AssignmentFieldLists.ENTERPRISE_COST))
            result.addAll(Arrays.asList(AssignmentFieldLists.ENTERPRISE_DATE))
            result.addAll(Arrays.asList(AssignmentFieldLists.ENTERPRISE_DURATION))
            result.addAll(Arrays.asList(AssignmentFieldLists.ENTERPRISE_FLAG))
            result.addAll(Arrays.asList(AssignmentFieldLists.ENTERPRISE_NUMBER))
            result.addAll(Arrays.asList(AssignmentFieldLists.ENTERPRISE_RESOURCE_MULTI_VALUE))
            result.addAll(Arrays.asList(AssignmentFieldLists.ENTERPRISE_RESOURCE_OUTLINE_CODE))
            result.addAll(Arrays.asList(AssignmentFieldLists.ENTERPRISE_TEXT))
            result.addAll(Arrays.asList(AssignmentFieldLists.CUSTOM_FINISH))
            result.addAll(Arrays.asList(AssignmentFieldLists.CUSTOM_FLAG))
            result.addAll(Arrays.asList(AssignmentFieldLists.CUSTOM_NUMBER))
            result.addAll(Arrays.asList(AssignmentFieldLists.CUSTOM_START))
            result.addAll(Arrays.asList(AssignmentFieldLists.CUSTOM_TEXT))
            return result
        }

    /**
     * Retrieve list of task extended attributes.
     *
     * @return list of extended attributes
     */
    private val allTaskExtendedAttributes: List<TaskField>
        get() {
            val result = ArrayList<TaskField>()
            result.addAll(Arrays.asList(TaskFieldLists.CUSTOM_TEXT))
            result.addAll(Arrays.asList(TaskFieldLists.CUSTOM_START))
            result.addAll(Arrays.asList(TaskFieldLists.CUSTOM_FINISH))
            result.addAll(Arrays.asList(TaskFieldLists.CUSTOM_COST))
            result.addAll(Arrays.asList(TaskFieldLists.CUSTOM_DATE))
            result.addAll(Arrays.asList(TaskFieldLists.CUSTOM_FLAG))
            result.addAll(Arrays.asList(TaskFieldLists.CUSTOM_NUMBER))
            result.addAll(Arrays.asList(TaskFieldLists.CUSTOM_DURATION))
            result.addAll(Arrays.asList(TaskFieldLists.CUSTOM_OUTLINE_CODE))
            result.addAll(Arrays.asList(TaskFieldLists.ENTERPRISE_COST))
            result.addAll(Arrays.asList(TaskFieldLists.ENTERPRISE_DATE))
            result.addAll(Arrays.asList(TaskFieldLists.ENTERPRISE_DURATION))
            result.addAll(Arrays.asList(TaskFieldLists.ENTERPRISE_FLAG))
            result.addAll(Arrays.asList(TaskFieldLists.ENTERPRISE_NUMBER))
            result.addAll(Arrays.asList(TaskFieldLists.ENTERPRISE_TEXT))
            return result
        }

    /**
     * Retrieve list of resource extended attributes.
     *
     * @return list of extended attributes
     */
    private val allResourceExtendedAttributes: List<ResourceField>
        get() {
            val result = ArrayList<ResourceField>()
            result.addAll(Arrays.asList(ResourceFieldLists.CUSTOM_TEXT))
            result.addAll(Arrays.asList(ResourceFieldLists.CUSTOM_START))
            result.addAll(Arrays.asList(ResourceFieldLists.CUSTOM_FINISH))
            result.addAll(Arrays.asList(ResourceFieldLists.CUSTOM_COST))
            result.addAll(Arrays.asList(ResourceFieldLists.CUSTOM_DATE))
            result.addAll(Arrays.asList(ResourceFieldLists.CUSTOM_FLAG))
            result.addAll(Arrays.asList(ResourceFieldLists.CUSTOM_NUMBER))
            result.addAll(Arrays.asList(ResourceFieldLists.CUSTOM_DURATION))
            result.addAll(Arrays.asList(ResourceFieldLists.CUSTOM_OUTLINE_CODE))
            result.addAll(Arrays.asList(ResourceFieldLists.ENTERPRISE_COST))
            result.addAll(Arrays.asList(ResourceFieldLists.ENTERPRISE_DATE))
            result.addAll(Arrays.asList(ResourceFieldLists.ENTERPRISE_DURATION))
            result.addAll(Arrays.asList(ResourceFieldLists.ENTERPRISE_FLAG))
            result.addAll(Arrays.asList(ResourceFieldLists.ENTERPRISE_NUMBER))
            result.addAll(Arrays.asList(ResourceFieldLists.ENTERPRISE_TEXT))
            return result
        }

    /**
     * Package-private accessor method used to retrieve the project file
     * currently being processed by this writer.
     *
     * @return project file instance
     */
    internal val projectFile: ProjectFile?
        get() = m_projectFile

    private var m_factory: ObjectFactory? = null

    private var m_projectFile: ProjectFile? = null

    private var m_eventManager: EventManager? = null

    private var m_extendedAttributesInUse: Set<FieldType>? = null

    /**
     * Retrieves a flag to control whether timephased assignment data is split
     * into days. The default is true.
     *
     * @return boolean true
     */
    /**
     * Sets a flag to control whether timephased assignment data is split
     * into days. The default is true.
     *
     * @param flag boolean flag
     */
    var splitTimephasedAsDays = true

    /**
     * Retrieves the state of the flag which controls whether timephased
     * resource assignment data is written to the file. The default is false.
     *
     * @return boolean flag
     */
    /**
     * Sets a flag to control whether timephased resource assignment data
     * is written to the file. The default is false.
     *
     * @param value boolean flag
     */
    var writeTimephasedData: Boolean = false

    /**
     * Retrieve the save version current set.
     *
     * @return current save version
     */
    /**
     * Set the save version to use when generating an MSPDI file.
     *
     * @param version save version
     */
    var saveVersion = SaveVersion.Project2016

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(IOException::class)
    override fun write(projectFile: ProjectFile, stream: OutputStream) {
        try {
            if (CONTEXT == null) {
                throw CONTEXT_EXCEPTION
            }

            m_projectFile = projectFile
            m_projectFile!!.validateUniqueIDsForMicrosoftProject()
            m_eventManager = m_projectFile!!.eventManager
            DatatypeConverter.setParentFile(m_projectFile)

            val marshaller = CONTEXT!!.createMarshaller()
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)

            m_extendedAttributesInUse = HashSet<FieldType>()

            m_factory = ObjectFactory()
            val project = m_factory!!.createProject()

            writeProjectProperties(project)
            writeCalendars(project)
            writeResources(project)
            writeTasks(project)
            writeAssignments(project)
            writeProjectExtendedAttributes(project)

            marshaller.marshal(project, stream)
        } catch (ex: JAXBException) {
            throw IOException(ex.toString())
        } finally {
            m_projectFile = null
            m_factory = null
            m_extendedAttributesInUse = null
        }
    }

    /**
     * This method writes project properties to an MSPDI file.
     *
     * @param project Root node of the MSPDI file
     */
    private fun writeProjectProperties(project: Project) {
        val properties = m_projectFile!!.projectProperties

        project.isActualsInSync = Boolean.valueOf(properties.actualsInSync)
        project.isAdminProject = Boolean.valueOf(properties.adminProject)
        project.author = properties.author
        project.isAutoAddNewResourcesAndTasks = Boolean.valueOf(properties.autoAddNewResourcesAndTasks)
        project.isAutolink = Boolean.valueOf(properties.autolink)
        project.baselineForEarnedValue = NumberHelper.getBigInteger(properties.baselineForEarnedValue)
        project.calendarUID = if (m_projectFile!!.defaultCalendar == null) BigInteger.ONE else NumberHelper.getBigInteger(m_projectFile!!.defaultCalendar!!.uniqueID)
        project.category = properties.category
        project.company = properties.company
        project.creationDate = properties.creationDate
        project.criticalSlackLimit = NumberHelper.getBigInteger(properties.criticalSlackLimit)
        project.currencyCode = properties.currencyCode
        project.currencyDigits = BigInteger.valueOf(properties.currencyDigits.intValue())
        project.currencySymbol = properties.currencySymbol
        project.currencySymbolPosition = properties.symbolPosition
        project.currentDate = properties.currentDate
        project.daysPerMonth = NumberHelper.getBigInteger(properties.daysPerMonth)
        project.defaultFinishTime = properties.defaultEndTime
        project.defaultFixedCostAccrual = properties.defaultFixedCostAccrual
        project.defaultOvertimeRate = DatatypeConverter.printRate(properties.defaultOvertimeRate)
        project.defaultStandardRate = DatatypeConverter.printRate(properties.defaultStandardRate)
        project.defaultStartTime = properties.defaultStartTime
        project.defaultTaskEVMethod = DatatypeConverter.printEarnedValueMethod(properties.defaultTaskEarnedValueMethod)
        project.defaultTaskType = properties.defaultTaskType
        project.durationFormat = DatatypeConverter.printDurationTimeUnits(properties.defaultDurationUnits, false)
        project.earnedValueMethod = DatatypeConverter.printEarnedValueMethod(properties.earnedValueMethod)
        project.isEditableActualCosts = Boolean.valueOf(properties.editableActualCosts)
        project.extendedCreationDate = properties.extendedCreationDate
        project.finishDate = properties.finishDate
        project.isFiscalYearStart = Boolean.valueOf(properties.fiscalYearStart)
        project.fyStartDate = NumberHelper.getBigInteger(properties.fiscalYearStartMonth)
        project.isHonorConstraints = Boolean.valueOf(properties.honorConstraints)
        project.isInsertedProjectsLikeSummary = Boolean.valueOf(properties.insertedProjectsLikeSummary)
        project.lastSaved = properties.lastSaved
        project.manager = properties.manager
        project.isMicrosoftProjectServerURL = Boolean.valueOf(properties.microsoftProjectServerURL)
        project.minutesPerDay = NumberHelper.getBigInteger(properties.minutesPerDay)
        project.minutesPerWeek = NumberHelper.getBigInteger(properties.minutesPerWeek)
        project.isMoveCompletedEndsBack = Boolean.valueOf(properties.moveCompletedEndsBack)
        project.isMoveCompletedEndsForward = Boolean.valueOf(properties.moveCompletedEndsForward)
        project.isMoveRemainingStartsBack = Boolean.valueOf(properties.moveRemainingStartsBack)
        project.isMoveRemainingStartsForward = Boolean.valueOf(properties.moveRemainingStartsForward)
        project.isMultipleCriticalPaths = Boolean.valueOf(properties.multipleCriticalPaths)
        project.name = properties.name
        project.isNewTasksEffortDriven = Boolean.valueOf(properties.newTasksEffortDriven)
        project.isNewTasksEstimated = Boolean.valueOf(properties.newTasksEstimated)
        project.newTaskStartDate = if (properties.newTaskStartIsProjectStart == true) BigInteger.ZERO else BigInteger.ONE
        project.isProjectExternallyEdited = Boolean.valueOf(properties.projectExternallyEdited)
        project.isRemoveFileProperties = Boolean.valueOf(properties.removeFileProperties)
        project.revision = NumberHelper.getBigInteger(properties.revision)
        project.saveVersion = BigInteger.valueOf(saveVersion.value)
        project.isScheduleFromStart = Boolean.valueOf(properties.scheduleFrom == ScheduleFrom.START)
        project.isSplitsInProgressTasks = Boolean.valueOf(properties.splitInProgressTasks)
        project.isSpreadActualCost = Boolean.valueOf(properties.spreadActualCost)
        project.isSpreadPercentComplete = Boolean.valueOf(properties.spreadPercentComplete)
        project.startDate = properties.startDate
        project.statusDate = properties.statusDate
        project.subject = properties.subject
        project.isTaskUpdatesResource = Boolean.valueOf(properties.updatingTaskStatusUpdatesResourceStatus)
        project.title = properties.projectTitle
        project.uid = properties.uniqueID
        project.weekStartDay = DatatypeConverter.printDay(properties.weekStartDay)
        project.workFormat = DatatypeConverter.printWorkUnits(properties.defaultWorkUnits)
    }

    /**
     * This method writes project extended attribute data into an MSPDI file.
     *
     * @param project Root node of the MSPDI file
     */
    private fun writeProjectExtendedAttributes(project: Project) {
        val attributes = m_factory!!.createProjectExtendedAttributes()
        project.extendedAttributes = attributes
        val list = attributes.extendedAttribute

        val customFields = HashSet<FieldType>()
        for (customField in m_projectFile!!.customFields) {
            val fieldType = customField.getFieldType()
            if (fieldType != null) {
                customFields.add(fieldType)
            }
        }

        customFields.addAll(m_extendedAttributesInUse)

        val customFieldsList = ArrayList<FieldType>()
        customFieldsList.addAll(customFields)


        // Sort to ensure consistent order in file
        val customFieldContainer = m_projectFile!!.customFields
        Collections.sort(customFieldsList, object : Comparator<FieldType>() {
            @Override
            fun compare(o1: FieldType, o2: FieldType): Int {
                val customField1 = customFieldContainer.getCustomField(o1)
                val customField2 = customFieldContainer.getCustomField(o2)
                val name1 = o1.getClass().getSimpleName() + "." + o1.getName() + " " + customField1.getAlias()
                val name2 = o2.getClass().getSimpleName() + "." + o2.getName() + " " + customField2.getAlias()
                return name1.compareTo(name2)
            }
        })

        for (fieldType in customFieldsList) {
            val attribute = m_factory!!.createProjectExtendedAttributesExtendedAttribute()
            list.add(attribute)
            attribute.fieldID = String.valueOf(FieldTypeHelper.getFieldID(fieldType))
            attribute.fieldName = fieldType.getName()

            val customField = customFieldContainer.getCustomField(fieldType)
            attribute.alias = customField.getAlias()
        }
    }

    /**
     * This method writes calendar data to an MSPDI file.
     *
     * @param project Root node of the MSPDI file
     */
    private fun writeCalendars(project: Project) {
        //
        // Create the new MSPDI calendar list
        //
        val calendars = m_factory!!.createProjectCalendars()
        project.calendars = calendars
        val calendar = calendars.calendar

        //
        // Process each calendar in turn
        //
        for (cal in m_projectFile!!.calendars) {
            calendar.add(writeCalendar(cal))
        }
    }

    /**
     * This method writes data for a single calendar to an MSPDI file.
     *
     * @param bc Base calendar data
     * @return New MSPDI calendar instance
     */
    private fun writeCalendar(bc: ProjectCalendar): Project.Calendars.Calendar {
        //
        // Create a calendar
        //
        val calendar = m_factory!!.createProjectCalendarsCalendar()
        calendar.uid = NumberHelper.getBigInteger(bc.uniqueID)
        calendar.isIsBaseCalendar = Boolean.valueOf(!bc.isDerived)

        val base = bc.parent
        // SF-329: null default required to keep Powerproject happy when importing MSPDI files
        calendar.baseCalendarUID = if (base == null) NULL_CALENDAR_ID else NumberHelper.getBigInteger(base.uniqueID)
        calendar.name = bc.name

        //
        // Create a list of normal days
        //
        val days = m_factory!!.createProjectCalendarsCalendarWeekDays()
        var time: Project.Calendars.Calendar.WeekDays.WeekDay.WorkingTimes.WorkingTime
        var bch: ProjectCalendarHours?

        val dayList = days.weekDay

        for (loop in 1..7) {
            val workingFlag = bc.getWorkingDay(Day.getInstance(loop))

            if (workingFlag !== DayType.DEFAULT) {
                val day = m_factory!!.createProjectCalendarsCalendarWeekDaysWeekDay()
                dayList.add(day)
                day.dayType = BigInteger.valueOf(loop)
                day.isDayWorking = Boolean.valueOf(workingFlag === DayType.WORKING)

                if (workingFlag === DayType.WORKING) {
                    val times = m_factory!!.createProjectCalendarsCalendarWeekDaysWeekDayWorkingTimes()
                    day.workingTimes = times
                    val timesList = times.workingTime

                    bch = bc.getCalendarHours(Day.getInstance(loop))
                    if (bch != null) {
                        for (range in bch) {
                            if (range != null) {
                                time = m_factory!!.createProjectCalendarsCalendarWeekDaysWeekDayWorkingTimesWorkingTime()
                                timesList.add(time)

                                time.fromTime = range!!.getStart()
                                time.toTime = range!!.getEnd()
                            }
                        }
                    }
                }
            }
        }

        //
        // Create a list of exceptions
        //
        // A quirk of MS Project is that these exceptions must be
        // in date order in the file, otherwise they are ignored
        //
        val exceptions = ArrayList<ProjectCalendarException>(bc.calendarExceptions)
        if (!exceptions.isEmpty()) {
            Collections.sort(exceptions)
            writeExceptions(calendar, dayList, exceptions)
        }

        //
        // Do not add a weekdays tag to the calendar unless it
        // has valid entries.
        // Fixes SourceForge bug 1854747: MPXJ and MSP 2007 XML formats
        //
        if (!dayList.isEmpty()) {
            calendar.weekDays = days
        }

        writeWorkWeeks(calendar, bc)

        m_eventManager!!.fireCalendarWrittenEvent(bc)

        return calendar
    }

    /**
     * Main entry point used to determine the format used to write
     * calendar exceptions.
     *
     * @param calendar parent calendar
     * @param dayList list of calendar days
     * @param exceptions list of exceptions
     */
    private fun writeExceptions(calendar: Project.Calendars.Calendar, dayList: List<Project.Calendars.Calendar.WeekDays.WeekDay>, exceptions: List<ProjectCalendarException>) {
        // Always write legacy exception data:
        // Powerproject appears not to recognise new format data at all,
        // and legacy data is ignored in preference to new data post MSP 2003
        writeExceptions9(dayList, exceptions)

        if (saveVersion.value > SaveVersion.Project2003.value) {
            writeExceptions12(calendar, exceptions)
        }
    }

    /**
     * Write exceptions in the format used by MSPDI files prior to Project 2007.
     *
     * @param dayList list of calendar days
     * @param exceptions list of exceptions
     */
    private fun writeExceptions9(dayList: List<Project.Calendars.Calendar.WeekDays.WeekDay>, exceptions: List<ProjectCalendarException>) {
        for (exception in exceptions) {
            val working = exception.working

            val day = m_factory!!.createProjectCalendarsCalendarWeekDaysWeekDay()
            dayList.add(day)
            day.dayType = BIGINTEGER_ZERO
            day.isDayWorking = Boolean.valueOf(working)

            val period = m_factory!!.createProjectCalendarsCalendarWeekDaysWeekDayTimePeriod()
            day.timePeriod = period
            period.fromDate = exception.fromDate
            period.toDate = exception.toDate

            if (working) {
                val times = m_factory!!.createProjectCalendarsCalendarWeekDaysWeekDayWorkingTimes()
                day.workingTimes = times
                val timesList = times.workingTime

                for (range in exception) {
                    val time = m_factory!!.createProjectCalendarsCalendarWeekDaysWeekDayWorkingTimesWorkingTime()
                    timesList.add(time)

                    time.fromTime = range.getStart()
                    time.toTime = range.getEnd()
                }
            }
        }
    }

    /**
     * Write exceptions into the format used by MSPDI files from
     * Project 2007 onwards.
     *
     * @param calendar parent calendar
     * @param exceptions list of exceptions
     */
    private fun writeExceptions12(calendar: Project.Calendars.Calendar, exceptions: List<ProjectCalendarException>) {
        val ce = m_factory!!.createProjectCalendarsCalendarExceptions()
        calendar.exceptions = ce
        val el = ce.exception

        for (exception in exceptions) {
            val ex = m_factory!!.createProjectCalendarsCalendarExceptionsException()
            el.add(ex)

            ex.name = exception.name
            val working = exception.working
            ex.isDayWorking = Boolean.valueOf(working)

            if (exception.recurring == null) {
                ex.isEnteredByOccurrences = Boolean.FALSE
                ex.occurrences = BigInteger.ONE
                ex.type = BigInteger.ONE
            } else {
                populateRecurringException(exception, ex)
            }

            val period = m_factory!!.createProjectCalendarsCalendarExceptionsExceptionTimePeriod()
            ex.timePeriod = period
            period.fromDate = exception.fromDate
            period.toDate = exception.toDate

            if (working) {
                val times = m_factory!!.createProjectCalendarsCalendarExceptionsExceptionWorkingTimes()
                ex.workingTimes = times
                val timesList = times.workingTime

                for (range in exception) {
                    val time = m_factory!!.createProjectCalendarsCalendarExceptionsExceptionWorkingTimesWorkingTime()
                    timesList.add(time)

                    time.fromTime = range.getStart()
                    time.toTime = range.getEnd()
                }
            }
        }
    }

    /**
     * Writes the details of a recurring exception.
     *
     * @param mpxjException source MPXJ calendar exception
     * @param xmlException target MSPDI exception
     */
    private fun populateRecurringException(mpxjException: ProjectCalendarException, xmlException: Exceptions.Exception) {
        val data = mpxjException.recurring
        xmlException.isEnteredByOccurrences = Boolean.TRUE
        xmlException.occurrences = NumberHelper.getBigInteger(data!!.occurrences)

        when (data.recurrenceType) {
            RecurrenceType.DAILY -> {
                xmlException.type = BigInteger.valueOf(7)
                xmlException.period = NumberHelper.getBigInteger(data.frequency)
            }

            RecurrenceType.WEEKLY -> {
                xmlException.type = BigInteger.valueOf(6)
                xmlException.period = NumberHelper.getBigInteger(data.frequency)
                xmlException.daysOfWeek = getDaysOfTheWeek(data)
            }

            RecurrenceType.MONTHLY -> {
                xmlException.period = NumberHelper.getBigInteger(data.frequency)
                if (data.relative) {
                    xmlException.type = BigInteger.valueOf(5)
                    xmlException.monthItem = BigInteger.valueOf(data.dayOfWeek!!.getValue() + 2)
                    xmlException.monthPosition = BigInteger.valueOf(NumberHelper.getInt(data.dayNumber) - 1)
                } else {
                    xmlException.type = BigInteger.valueOf(4)
                    xmlException.monthDay = NumberHelper.getBigInteger(data.dayNumber)
                }
            }

            RecurrenceType.YEARLY -> {
                xmlException.month = BigInteger.valueOf(NumberHelper.getInt(data.monthNumber) - 1)
                if (data.relative) {
                    xmlException.type = BigInteger.valueOf(3)
                    xmlException.monthItem = BigInteger.valueOf(data.dayOfWeek!!.getValue() + 2)
                    xmlException.monthPosition = BigInteger.valueOf(NumberHelper.getInt(data.dayNumber) - 1)
                } else {
                    xmlException.type = BigInteger.valueOf(2)
                    xmlException.monthDay = NumberHelper.getBigInteger(data.dayNumber)
                }
            }
        }
    }

    /**
     * Converts days of the week into a bit field.
     *
     * @param data recurring data
     * @return bit field
     */
    private fun getDaysOfTheWeek(data: RecurringData): BigInteger {
        var value = 0
        for (day in Day.values()) {
            if (data.getWeeklyDay(day)) {
                value = value or DAY_MASKS[day.getValue()]
            }
        }
        return BigInteger.valueOf(value)
    }

    /**
     * Write the work weeks associated with this calendar.
     *
     * @param xmlCalendar XML calendar instance
     * @param mpxjCalendar MPXJ calendar instance
     */
    private fun writeWorkWeeks(xmlCalendar: Project.Calendars.Calendar, mpxjCalendar: ProjectCalendar) {
        val weeks = mpxjCalendar.workWeeks
        if (!weeks.isEmpty()) {
            val xmlWorkWeeks = m_factory!!.createProjectCalendarsCalendarWorkWeeks()
            xmlCalendar.workWeeks = xmlWorkWeeks
            val xmlWorkWeekList = xmlWorkWeeks.workWeek

            for (week in weeks) {
                val xmlWeek = m_factory!!.createProjectCalendarsCalendarWorkWeeksWorkWeek()
                xmlWorkWeekList.add(xmlWeek)

                xmlWeek.name = week.name
                val xmlTimePeriod = m_factory!!.createProjectCalendarsCalendarWorkWeeksWorkWeekTimePeriod()
                xmlWeek.timePeriod = xmlTimePeriod
                xmlTimePeriod.fromDate = week.dateRange!!.getStart()
                xmlTimePeriod.toDate = week.dateRange!!.getEnd()

                val xmlWeekDays = m_factory!!.createProjectCalendarsCalendarWorkWeeksWorkWeekWeekDays()
                xmlWeek.weekDays = xmlWeekDays

                val dayList = xmlWeekDays.weekDay

                for (loop in 1..7) {
                    val workingFlag = week.getWorkingDay(Day.getInstance(loop))

                    if (workingFlag !== DayType.DEFAULT) {
                        val day = m_factory!!.createProjectCalendarsCalendarWorkWeeksWorkWeekWeekDaysWeekDay()
                        dayList.add(day)
                        day.dayType = BigInteger.valueOf(loop)
                        day.isDayWorking = Boolean.valueOf(workingFlag === DayType.WORKING)

                        if (workingFlag === DayType.WORKING) {
                            val times = m_factory!!.createProjectCalendarsCalendarWorkWeeksWorkWeekWeekDaysWeekDayWorkingTimes()
                            day.workingTimes = times
                            val timesList = times.workingTime

                            val bch = week.getCalendarHours(Day.getInstance(loop))
                            if (bch != null) {
                                for (range in bch!!) {
                                    if (range != null) {
                                        val time = m_factory!!.createProjectCalendarsCalendarWorkWeeksWorkWeekWeekDaysWeekDayWorkingTimesWorkingTime()
                                        timesList.add(time)

                                        time.fromTime = range!!.getStart()
                                        time.toTime = range!!.getEnd()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This method writes resource data to an MSPDI file.
     *
     * @param project Root node of the MSPDI file
     */
    private fun writeResources(project: Project) {
        val resources = m_factory!!.createProjectResources()
        project.resources = resources
        val list = resources.resource

        for (resource in m_projectFile!!.resources) {
            list.add(writeResource(resource))
        }
    }

    /**
     * This method writes data for a single resource to an MSPDI file.
     *
     * @param mpx Resource data
     * @return New MSPDI resource instance
     */
    private fun writeResource(mpx: Resource): Project.Resources.Resource {
        val xml = m_factory!!.createProjectResourcesResource()
        val cal = mpx.resourceCalendar
        if (cal != null) {
            xml.calendarUID = NumberHelper.getBigInteger(cal.uniqueID)
        }

        xml.accrueAt = mpx.accrueAt
        xml.activeDirectoryGUID = mpx.activeDirectoryGUID
        xml.actualCost = DatatypeConverter.printCurrency(mpx.actualCost)
        xml.actualOvertimeCost = DatatypeConverter.printCurrency(mpx.actualOvertimeCost)
        xml.actualOvertimeWork = DatatypeConverter.printDuration(this, mpx.actualOvertimeWork)
        xml.actualOvertimeWorkProtected = DatatypeConverter.printDuration(this, mpx.actualOvertimeWorkProtected)
        xml.actualWork = DatatypeConverter.printDuration(this, mpx.actualWork)
        xml.actualWorkProtected = DatatypeConverter.printDuration(this, mpx.actualWorkProtected)
        xml.acwp = DatatypeConverter.printCurrency(mpx.acwp)
        xml.availableFrom = mpx.availableFrom
        xml.availableTo = mpx.availableTo
        xml.bcws = DatatypeConverter.printCurrency(mpx.bcws)
        xml.bcwp = DatatypeConverter.printCurrency(mpx.bcwp)
        xml.bookingType = mpx.bookingType
        xml.isIsBudget = Boolean.valueOf(mpx.budget)
        xml.isCanLevel = Boolean.valueOf(mpx.canLevel)
        xml.code = mpx.code
        xml.cost = DatatypeConverter.printCurrency(mpx.cost)
        xml.costPerUse = DatatypeConverter.printCurrency(mpx.costPerUse)
        xml.costVariance = DatatypeConverter.printCurrency(mpx.costVariance)
        xml.creationDate = mpx.creationDate
        xml.cv = DatatypeConverter.printCurrency(mpx.cv)
        xml.emailAddress = mpx.emailAddress
        xml.finish = mpx.finish
        xml.group = mpx.group
        xml.guid = mpx.guid
        xml.hyperlink = mpx.hyperlink
        xml.hyperlinkAddress = mpx.hyperlinkAddress
        xml.hyperlinkSubAddress = mpx.hyperlinkSubAddress
        xml.id = NumberHelper.getBigInteger(mpx.id)
        xml.initials = mpx.initials
        xml.isIsEnterprise = Boolean.valueOf(mpx.enterprise)
        xml.isIsGeneric = Boolean.valueOf(mpx.generic)
        xml.isIsInactive = Boolean.valueOf(!mpx.active)
        xml.isIsNull = Boolean.valueOf(mpx.`null`)
        xml.materialLabel = mpx.materialLabel
        xml.maxUnits = DatatypeConverter.printUnits(mpx.maxUnits)
        xml.name = mpx.name

        if (!mpx.notes.isEmpty()) {
            xml.notes = mpx.notes
        }

        xml.ntAccount = mpx.ntAccount
        xml.isOverAllocated = Boolean.valueOf(mpx.overAllocated)
        xml.overtimeCost = DatatypeConverter.printCurrency(mpx.overtimeCost)
        xml.overtimeRate = DatatypeConverter.printRate(mpx.overtimeRate)
        xml.overtimeRateFormat = DatatypeConverter.printTimeUnit(mpx.overtimeRateUnits)
        xml.overtimeWork = DatatypeConverter.printDuration(this, mpx.overtimeWork)
        xml.peakUnits = DatatypeConverter.printUnits(mpx.peakUnits)
        xml.percentWorkComplete = NumberHelper.getBigInteger(mpx.percentWorkComplete)
        xml.phonetics = mpx.phonetics
        xml.regularWork = DatatypeConverter.printDuration(this, mpx.regularWork)
        xml.remainingCost = DatatypeConverter.printCurrency(mpx.remainingCost)
        xml.remainingOvertimeCost = DatatypeConverter.printCurrency(mpx.remainingOvertimeCost)
        xml.remainingOvertimeWork = DatatypeConverter.printDuration(this, mpx.remainingOvertimeWork)
        xml.remainingWork = DatatypeConverter.printDuration(this, mpx.remainingWork)
        xml.standardRate = DatatypeConverter.printRate(mpx.standardRate)
        xml.standardRateFormat = DatatypeConverter.printTimeUnit(mpx.standardRateUnits)
        xml.start = mpx.start
        xml.sv = DatatypeConverter.printCurrency(mpx.sv)
        xml.uid = mpx.uniqueID
        xml.work = DatatypeConverter.printDuration(this, mpx.work)
        xml.workGroup = mpx.workGroup
        xml.workVariance = DatatypeConverter.printDurationInDecimalThousandthsOfMinutes(mpx.workVariance)

        if (mpx.type == ResourceType.COST) {
            xml.type = ResourceType.MATERIAL
            xml.isIsCostResource = Boolean.TRUE
        } else {
            xml.type = mpx.type
        }

        writeResourceExtendedAttributes(xml, mpx)

        writeResourceBaselines(xml, mpx)

        writeCostRateTables(xml, mpx)

        writeAvailability(xml, mpx)

        return xml
    }

    /**
     * Writes resource baseline data.
     *
     * @param xmlResource MSPDI resource
     * @param mpxjResource MPXJ resource
     */
    private fun writeResourceBaselines(xmlResource: Project.Resources.Resource, mpxjResource: Resource) {
        var baseline = m_factory!!.createProjectResourcesResourceBaseline()
        var populated = false

        var cost = mpxjResource.baselineCost
        if (cost != null && cost.intValue() !== 0) {
            populated = true
            baseline.cost = DatatypeConverter.printCurrency(cost)
        }

        var work = mpxjResource.baselineWork
        if (work != null && work.getDuration() !== 0) {
            populated = true
            baseline.work = DatatypeConverter.printDuration(this, work)
        }

        if (populated) {
            xmlResource.baseline.add(baseline)
            baseline.number = BigInteger.ZERO
        }

        for (loop in 1..10) {
            baseline = m_factory!!.createProjectResourcesResourceBaseline()
            populated = false

            cost = mpxjResource.getBaselineCost(loop)
            if (cost != null && cost.intValue() !== 0) {
                populated = true
                baseline.cost = DatatypeConverter.printCurrency(cost)
            }

            work = mpxjResource.getBaselineWork(loop)
            if (work != null && work.getDuration() !== 0) {
                populated = true
                baseline.work = DatatypeConverter.printDuration(this, work)
            }

            if (populated) {
                xmlResource.baseline.add(baseline)
                baseline.number = BigInteger.valueOf(loop)
            }
        }
    }

    /**
     * This method writes extended attribute data for a resource.
     *
     * @param xml MSPDI resource
     * @param mpx MPXJ resource
     */
    private fun writeResourceExtendedAttributes(xml: Project.Resources.Resource, mpx: Resource) {
        var attrib: Project.Resources.Resource.ExtendedAttribute
        val extendedAttributes = xml.extendedAttribute

        for (mpxFieldID in allResourceExtendedAttributes) {
            val value = mpx.getCachedValue(mpxFieldID)

            if (FieldTypeHelper.valueIsNotDefault(mpxFieldID, value)) {
                m_extendedAttributesInUse!!.add(mpxFieldID)

                val xmlFieldID = Integer.valueOf(MPPResourceField.getID(mpxFieldID) or MPPResourceField.RESOURCE_FIELD_BASE)

                attrib = m_factory!!.createProjectResourcesResourceExtendedAttribute()
                extendedAttributes.add(attrib)
                attrib.fieldID = xmlFieldID.toString()
                attrib.value = DatatypeConverter.printExtendedAttribute(this, value, mpxFieldID.dataType)
                attrib.durationFormat = printExtendedAttributeDurationFormat(value)
            }
        }
    }

    /**
     * Writes a resource's cost rate tables.
     *
     * @param xml MSPDI resource
     * @param mpx MPXJ resource
     */
    private fun writeCostRateTables(xml: Project.Resources.Resource, mpx: Resource) {
        //Rates rates = m_factory.createProjectResourcesResourceRates();
        //xml.setRates(rates);
        //List<Project.Resources.Resource.Rates.Rate> ratesList = rates.getRate();

        var ratesList: List<Project.Resources.Resource.Rates.Rate>? = null

        for (tableIndex in 0..4) {
            val table = mpx.getCostRateTable(tableIndex)
            if (table != null) {
                var from = DateHelper.FIRST_DATE
                for (entry in table) {
                    if (costRateTableWriteRequired(entry, from)) {
                        if (ratesList == null) {
                            val rates = m_factory!!.createProjectResourcesResourceRates()
                            xml.rates = rates
                            ratesList = rates.rate
                        }

                        val rate = m_factory!!.createProjectResourcesResourceRatesRate()
                        ratesList!!.add(rate)

                        rate.costPerUse = DatatypeConverter.printCurrency(entry.getCostPerUse())
                        rate.overtimeRate = DatatypeConverter.printRate(entry.getOvertimeRate())
                        rate.overtimeRateFormat = DatatypeConverter.printTimeUnit(entry.getOvertimeRateFormat())
                        rate.ratesFrom = from
                        from = entry.getEndDate()
                        rate.ratesTo = from
                        rate.rateTable = BigInteger.valueOf(tableIndex)
                        rate.standardRate = DatatypeConverter.printRate(entry.getStandardRate())
                        rate.standardRateFormat = DatatypeConverter.printTimeUnit(entry.getStandardRateFormat())
                    }
                }
            }
        }
    }

    /**
     * This method determines whether the cost rate table should be written.
     * A default cost rate table should not be written to the file.
     *
     * @param entry cost rate table entry
     * @param from from date
     * @return boolean flag
     */
    private fun costRateTableWriteRequired(entry: CostRateTableEntry, from: Date): Boolean {
        val fromDate = DateHelper.compare(from, DateHelper.FIRST_DATE) > 0
        val toDate = DateHelper.compare(entry.getEndDate(), DateHelper.LAST_DATE) > 0
        val costPerUse = NumberHelper.getDouble(entry.getCostPerUse()) !== 0
        val overtimeRate = entry.getOvertimeRate() != null && entry.getOvertimeRate().getAmount() !== 0
        val standardRate = entry.getStandardRate() != null && entry.getStandardRate().getAmount() !== 0
        return fromDate || toDate || costPerUse || overtimeRate || standardRate
    }

    /**
     * This method writes a resource's availability table.
     *
     * @param xml MSPDI resource
     * @param mpx MPXJ resource
     */
    private fun writeAvailability(xml: Project.Resources.Resource, mpx: Resource) {
        val periods = m_factory!!.createProjectResourcesResourceAvailabilityPeriods()
        xml.availabilityPeriods = periods
        val list = periods.availabilityPeriod
        for (availability in mpx.availability) {
            val period = m_factory!!.createProjectResourcesResourceAvailabilityPeriodsAvailabilityPeriod()
            list.add(period)
            val range = availability.getRange()

            period.availableFrom = range.getStart()
            period.availableTo = range.getEnd()
            period.availableUnits = DatatypeConverter.printUnits(availability.getUnits())
        }
    }

    /**
     * This method writes task data to an MSPDI file.
     *
     * @param project Root node of the MSPDI file
     */
    private fun writeTasks(project: Project) {
        val tasks = m_factory!!.createProjectTasks()
        project.tasks = tasks
        val list = tasks.task

        for (task in m_projectFile!!.tasks) {
            list.add(writeTask(task))
        }
    }

    /**
     * This method writes data for a single task to an MSPDI file.
     *
     * @param mpx Task data
     * @return new task instance
     */
    private fun writeTask(mpx: Task): Project.Tasks.Task {
        val xml = m_factory!!.createProjectTasksTask()

        xml.isActive = Boolean.valueOf(mpx.active)
        xml.actualCost = DatatypeConverter.printCurrency(mpx.actualCost)
        xml.actualDuration = DatatypeConverter.printDuration(this, mpx.actualDuration)
        xml.actualFinish = mpx.actualFinish
        xml.actualOvertimeCost = DatatypeConverter.printCurrency(mpx.actualOvertimeCost)
        xml.actualOvertimeWork = DatatypeConverter.printDuration(this, mpx.actualOvertimeWork)
        xml.actualOvertimeWorkProtected = DatatypeConverter.printDuration(this, mpx.actualOvertimeWorkProtected)
        xml.actualStart = mpx.actualStart
        xml.actualWork = DatatypeConverter.printDuration(this, mpx.actualWork)
        xml.actualWorkProtected = DatatypeConverter.printDuration(this, mpx.actualWorkProtected)
        xml.acwp = DatatypeConverter.printCurrency(mpx.acwp)
        xml.bcwp = DatatypeConverter.printCurrency(mpx.bcwp)
        xml.bcws = DatatypeConverter.printCurrency(mpx.bcws)
        xml.calendarUID = getTaskCalendarID(mpx)
        xml.constraintDate = mpx.constraintDate
        xml.constraintType = DatatypeConverter.printConstraintType(mpx.constraintType)
        xml.contact = mpx.contact
        xml.cost = DatatypeConverter.printCurrency(mpx.cost)
        xml.createDate = mpx.createDate
        xml.isCritical = Boolean.valueOf(mpx.critical)
        xml.cv = DatatypeConverter.printCurrency(mpx.cv)
        xml.deadline = mpx.deadline
        xml.duration = DatatypeConverter.printDurationMandatory(this, mpx.duration)
        xml.durationText = mpx.durationText
        xml.durationFormat = DatatypeConverter.printDurationTimeUnits(mpx.duration, mpx.estimated)
        xml.earlyFinish = mpx.earlyFinish
        xml.earlyStart = mpx.earlyStart
        xml.earnedValueMethod = DatatypeConverter.printEarnedValueMethod(mpx.earnedValueMethod)
        xml.isEffortDriven = Boolean.valueOf(mpx.effortDriven)
        xml.isEstimated = Boolean.valueOf(mpx.estimated)
        xml.isExternalTask = Boolean.valueOf(mpx.externalTask)
        xml.externalTaskProject = mpx.project
        xml.finish = mpx.finish
        xml.finishSlack = DatatypeConverter.printDurationInIntegerTenthsOfMinutes(mpx.finishSlack)
        xml.finishText = mpx.finishText
        xml.finishVariance = DatatypeConverter.printDurationInIntegerThousandthsOfMinutes(mpx.finishVariance)
        xml.fixedCost = DatatypeConverter.printCurrency(mpx.fixedCost)

        var fixedCostAccrual: AccrueType? = mpx.fixedCostAccrual
        if (fixedCostAccrual == null) {
            fixedCostAccrual = AccrueType.PRORATED
        }
        xml.fixedCostAccrual = fixedCostAccrual
        xml.freeSlack = DatatypeConverter.printDurationInIntegerTenthsOfMinutes(mpx.freeSlack)
        xml.guid = mpx.guid
        xml.isHideBar = Boolean.valueOf(mpx.hideBar)
        xml.isIsNull = Boolean.valueOf(mpx.`null`)
        xml.isIsSubproject = Boolean.valueOf(mpx.subProject != null)
        xml.isIsSubprojectReadOnly = Boolean.valueOf(mpx.subprojectReadOnly)
        xml.hyperlink = mpx.hyperlink
        xml.hyperlinkAddress = mpx.hyperlinkAddress
        xml.hyperlinkSubAddress = mpx.hyperlinkSubAddress
        xml.id = NumberHelper.getBigInteger(mpx.id)
        xml.isIgnoreResourceCalendar = Boolean.valueOf(mpx.ignoreResourceCalendar)
        xml.lateFinish = mpx.lateFinish
        xml.lateStart = mpx.lateStart
        xml.isLevelAssignments = Boolean.valueOf(mpx.levelAssignments)
        xml.isLevelingCanSplit = Boolean.valueOf(mpx.levelingCanSplit)

        if (mpx.levelingDelay != null) {
            val levelingDelay = mpx.levelingDelay
            val tenthMinutes = 10.0 * Duration.convertUnits(levelingDelay.getDuration(), levelingDelay.getUnits(), TimeUnit.MINUTES, m_projectFile!!.projectProperties).getDuration()
            xml.levelingDelay = BigInteger.valueOf(tenthMinutes.toLong())
            xml.levelingDelayFormat = DatatypeConverter.printDurationTimeUnits(levelingDelay, false)
        }

        xml.isManual = Boolean.valueOf(mpx.taskMode === TaskMode.MANUALLY_SCHEDULED)

        if (mpx.taskMode === TaskMode.MANUALLY_SCHEDULED) {
            xml.manualDuration = DatatypeConverter.printDuration(this, mpx.duration)
            xml.manualFinish = mpx.finish
            xml.manualStart = mpx.start
        }

        xml.isMilestone = Boolean.valueOf(mpx.milestone)
        xml.name = mpx.name

        if (!mpx.notes.isEmpty()) {
            xml.notes = mpx.notes
        }

        xml.outlineLevel = NumberHelper.getBigInteger(mpx.outlineLevel)
        xml.outlineNumber = mpx.outlineNumber
        xml.isOverAllocated = Boolean.valueOf(mpx.overAllocated)
        xml.overtimeCost = DatatypeConverter.printCurrency(mpx.overtimeCost)
        xml.overtimeWork = DatatypeConverter.printDuration(this, mpx.overtimeWork)
        xml.percentComplete = NumberHelper.getBigInteger(mpx.percentageComplete)
        xml.percentWorkComplete = NumberHelper.getBigInteger(mpx.percentageWorkComplete)
        xml.physicalPercentComplete = NumberHelper.getBigInteger(mpx.physicalPercentComplete)
        xml.priority = DatatypeConverter.printPriority(mpx.priority)
        xml.isRecurring = Boolean.valueOf(mpx.recurring)
        xml.regularWork = DatatypeConverter.printDuration(this, mpx.regularWork)
        xml.remainingCost = DatatypeConverter.printCurrency(mpx.remainingCost)

        if (mpx.remainingDuration == null) {
            val duration = mpx.duration

            if (duration != null) {
                var amount = duration.getDuration()
                amount -= amount * NumberHelper.getDouble(mpx.percentageComplete) / 100
                xml.remainingDuration = DatatypeConverter.printDuration(this, Duration.getInstance(amount, duration.getUnits()))
            }
        } else {
            xml.remainingDuration = DatatypeConverter.printDuration(this, mpx.remainingDuration)
        }

        xml.remainingOvertimeCost = DatatypeConverter.printCurrency(mpx.remainingOvertimeCost)
        xml.remainingOvertimeWork = DatatypeConverter.printDuration(this, mpx.remainingOvertimeWork)
        xml.remainingWork = DatatypeConverter.printDuration(this, mpx.remainingWork)
        xml.resume = mpx.resume
        xml.isResumeValid = Boolean.valueOf(mpx.resumeValid)
        xml.isRollup = Boolean.valueOf(mpx.rollup)
        xml.start = mpx.start
        xml.startSlack = DatatypeConverter.printDurationInIntegerTenthsOfMinutes(mpx.startSlack)
        xml.startText = mpx.startText
        xml.startVariance = DatatypeConverter.printDurationInIntegerThousandthsOfMinutes(mpx.startVariance)
        xml.stop = mpx.stop
        xml.subprojectName = mpx.subprojectName
        xml.isSummary = Boolean.valueOf(mpx.hasChildTasks())
        xml.totalSlack = DatatypeConverter.printDurationInIntegerTenthsOfMinutes(mpx.totalSlack)
        xml.type = mpx.type
        xml.uid = mpx.uniqueID
        xml.wbs = mpx.wbs
        xml.wbsLevel = mpx.wbsLevel
        xml.work = DatatypeConverter.printDuration(this, mpx.work)
        xml.workVariance = DatatypeConverter.printDurationInDecimalThousandthsOfMinutes(mpx.workVariance)

        if (mpx.taskMode === TaskMode.MANUALLY_SCHEDULED) {
            xml.manualDuration = DatatypeConverter.printDuration(this, mpx.manualDuration)
        }

        writePredecessors(xml, mpx)

        writeTaskExtendedAttributes(xml, mpx)

        writeTaskBaselines(xml, mpx)

        return xml
    }

    /**
     * Writes task baseline data.
     *
     * @param xmlTask MSPDI task
     * @param mpxjTask MPXJ task
     */
    private fun writeTaskBaselines(xmlTask: Project.Tasks.Task, mpxjTask: Task) {
        var baseline = m_factory!!.createProjectTasksTaskBaseline()
        var populated = false

        var cost = mpxjTask.baselineCost
        if (cost != null && cost.intValue() !== 0) {
            populated = true
            baseline.cost = DatatypeConverter.printCurrency(cost)
        }

        var duration = mpxjTask.baselineDuration
        if (duration != null && duration.getDuration() !== 0) {
            populated = true
            baseline.duration = DatatypeConverter.printDuration(this, duration)
            baseline.durationFormat = DatatypeConverter.printDurationTimeUnits(duration, false)
        }

        var date: Date? = mpxjTask.baselineFinish
        if (date != null) {
            populated = true
            baseline.finish = date
        }

        date = mpxjTask.baselineStart
        if (date != null) {
            populated = true
            baseline.start = date
        }

        duration = mpxjTask.baselineWork
        if (duration != null && duration.getDuration() !== 0) {
            populated = true
            baseline.work = DatatypeConverter.printDuration(this, duration)
        }

        if (populated) {
            baseline.number = BigInteger.ZERO
            xmlTask.baseline.add(baseline)
        }

        for (loop in 1..10) {
            baseline = m_factory!!.createProjectTasksTaskBaseline()
            populated = false

            cost = mpxjTask.getBaselineCost(loop)
            if (cost != null && cost.intValue() !== 0) {
                populated = true
                baseline.cost = DatatypeConverter.printCurrency(cost)
            }

            duration = mpxjTask.getBaselineDuration(loop)
            if (duration != null && duration.getDuration() !== 0) {
                populated = true
                baseline.duration = DatatypeConverter.printDuration(this, duration)
                baseline.durationFormat = DatatypeConverter.printDurationTimeUnits(duration, false)
            }

            date = mpxjTask.getBaselineFinish(loop)
            if (date != null) {
                populated = true
                baseline.finish = date
            }

            date = mpxjTask.getBaselineStart(loop)
            if (date != null) {
                populated = true
                baseline.start = date
            }

            duration = mpxjTask.getBaselineWork(loop)
            if (duration != null && duration.getDuration() !== 0) {
                populated = true
                baseline.work = DatatypeConverter.printDuration(this, duration)
            }

            if (populated) {
                baseline.number = BigInteger.valueOf(loop)
                xmlTask.baseline.add(baseline)
            }
        }
    }

    /**
     * This method writes extended attribute data for a task.
     *
     * @param xml MSPDI task
     * @param mpx MPXJ task
     */
    private fun writeTaskExtendedAttributes(xml: Project.Tasks.Task, mpx: Task) {
        var attrib: Project.Tasks.Task.ExtendedAttribute
        val extendedAttributes = xml.extendedAttribute

        for (mpxFieldID in allTaskExtendedAttributes) {
            val value = mpx.getCachedValue(mpxFieldID)

            if (FieldTypeHelper.valueIsNotDefault(mpxFieldID, value)) {
                m_extendedAttributesInUse!!.add(mpxFieldID)

                val xmlFieldID = Integer.valueOf(MPPTaskField.getID(mpxFieldID) or MPPTaskField.TASK_FIELD_BASE)

                attrib = m_factory!!.createProjectTasksTaskExtendedAttribute()
                extendedAttributes.add(attrib)
                attrib.fieldID = xmlFieldID.toString()
                attrib.value = DatatypeConverter.printExtendedAttribute(this, value, mpxFieldID.getDataType())
                attrib.durationFormat = printExtendedAttributeDurationFormat(value)
            }
        }
    }

    /**
     * Converts a duration to duration time units.
     *
     * @param value duration value
     * @return duration time units
     */
    private fun printExtendedAttributeDurationFormat(value: Object?): BigInteger? {
        var result: BigInteger? = null
        if (value is Duration) {
            result = DatatypeConverter.printDurationTimeUnits((value as Duration).getUnits(), false)
        }
        return result
    }

    /**
     * This method retrieves the UID for a calendar associated with a task.
     *
     * @param mpx MPX Task instance
     * @return calendar UID
     */
    private fun getTaskCalendarID(mpx: Task): BigInteger? {
        var result: BigInteger? = null
        val cal = mpx.calendar
        if (cal != null) {
            result = NumberHelper.getBigInteger(cal.uniqueID)
        } else {
            result = NULL_CALENDAR_ID
        }
        return result
    }

    /**
     * This method writes predecessor data to an MSPDI file.
     * We have to deal with a slight anomaly in this method that is introduced
     * by the MPX file format. It would be possible for someone to create an
     * MPX file with both the predecessor list and the unique ID predecessor
     * list populated... which means that we must process both and avoid adding
     * duplicate predecessors. Also interesting to note is that MSP98 populates
     * the predecessor list, not the unique ID predecessor list, as you might
     * expect.
     *
     * @param xml MSPDI task data
     * @param mpx MPX task data
     */
    private fun writePredecessors(xml: Project.Tasks.Task, mpx: Task) {
        val list = xml.predecessorLink

        val predecessors = mpx.predecessors
        for (rel in predecessors) {
            val taskUniqueID = rel.targetTask.uniqueID
            list.add(writePredecessor(taskUniqueID, rel.type!!, rel.lag))
            m_eventManager!!.fireRelationWrittenEvent(rel)
        }
    }

    /**
     * This method writes a single predecessor link to the MSPDI file.
     *
     * @param taskID The task UID
     * @param type The predecessor type
     * @param lag The lag duration
     * @return A new link to be added to the MSPDI file
     */
    private fun writePredecessor(taskID: Integer, type: RelationType, lag: Duration?): Project.Tasks.Task.PredecessorLink {
        val link = m_factory!!.createProjectTasksTaskPredecessorLink()

        link.predecessorUID = NumberHelper.getBigInteger(taskID)
        link.type = BigInteger.valueOf(type.value)
        link.isCrossProject = Boolean.FALSE // SF-300: required to keep P6 happy when importing MSPDI files

        if (lag != null && lag!!.getDuration() !== 0) {
            var linkLag = lag!!.getDuration()
            if (lag!!.getUnits() !== TimeUnit.PERCENT && lag!!.getUnits() !== TimeUnit.ELAPSED_PERCENT) {
                linkLag = 10.0 * Duration.convertUnits(linkLag, lag!!.getUnits(), TimeUnit.MINUTES, m_projectFile!!.projectProperties).getDuration()
            }
            link.linkLag = BigInteger.valueOf(linkLag.toLong())
            link.lagFormat = DatatypeConverter.printDurationTimeUnits(lag!!.getUnits(), false)
        } else {
            // SF-329: default required to keep Powerproject happy when importing MSPDI files
            link.linkLag = BIGINTEGER_ZERO
            link.lagFormat = DatatypeConverter.printDurationTimeUnits(m_projectFile!!.projectProperties.defaultDurationUnits, false)
        }

        return link
    }

    /**
     * This method writes assignment data to an MSPDI file.
     *
     * @param project Root node of the MSPDI file
     */
    private fun writeAssignments(project: Project) {
        val assignments = m_factory!!.createProjectAssignments()
        project.assignments = assignments
        val list = assignments.assignment

        for (assignment in m_projectFile!!.resourceAssignments) {
            list.add(writeAssignment(assignment))
        }

        //
        // Check to see if we have any tasks that have a percent complete value
        // but do not have resource assignments. If any exist, then we must
        // write a dummy resource assignment record to ensure that the MSPDI
        // file shows the correct percent complete amount for the task.
        //
        val config = m_projectFile!!.projectConfig
        val autoUniqueID = config.autoAssignmentUniqueID
        if (!autoUniqueID) {
            config.autoAssignmentUniqueID = true
        }

        for (task in m_projectFile!!.tasks) {
            val percentComplete = NumberHelper.getDouble(task.percentageComplete)
            if (percentComplete != 0.0 && task.resourceAssignments.isEmpty() === true) {
                val dummy = ResourceAssignment(m_projectFile, task)
                var duration = task.duration
                if (duration == null) {
                    duration = Duration.getInstance(0, TimeUnit.HOURS)
                }
                val durationValue = duration!!.getDuration()
                val durationUnits = duration!!.getUnits()
                val actualWork = durationValue * percentComplete / 100
                val remainingWork = durationValue - actualWork

                dummy.resourceUniqueID = NULL_RESOURCE_ID
                dummy.work = duration
                dummy.actualWork = Duration.getInstance(actualWork, durationUnits)
                dummy.remainingWork = Duration.getInstance(remainingWork, durationUnits)

                // Without this, MS Project will mark a 100% complete milestone as 99% complete
                if (percentComplete == 100.0 && duration!!.getDuration() === 0) {
                    dummy.actualFinish = task.actualStart
                }

                list.add(writeAssignment(dummy))
            }
        }

        config.autoAssignmentUniqueID = autoUniqueID
    }

    /**
     * This method writes data for a single assignment to an MSPDI file.
     *
     * @param mpx Resource assignment data
     * @return New MSPDI assignment instance
     */
    private fun writeAssignment(mpx: ResourceAssignment): Project.Assignments.Assignment {
        val xml = m_factory!!.createProjectAssignmentsAssignment()

        xml.actualCost = DatatypeConverter.printCurrency(mpx.actualCost)
        xml.actualFinish = mpx.actualFinish
        xml.actualOvertimeCost = DatatypeConverter.printCurrency(mpx.actualOvertimeCost)
        xml.actualOvertimeWork = DatatypeConverter.printDuration(this, mpx.actualOvertimeWork)
        xml.actualStart = mpx.actualStart
        xml.actualWork = DatatypeConverter.printDuration(this, mpx.actualWork)
        xml.acwp = DatatypeConverter.printCurrency(mpx.acwp)
        xml.bcwp = DatatypeConverter.printCurrency(mpx.bcwp)
        xml.bcws = DatatypeConverter.printCurrency(mpx.bcws)
        xml.budgetCost = DatatypeConverter.printCurrency(mpx.budgetCost)
        xml.budgetWork = DatatypeConverter.printDuration(this, mpx.budgetWork)
        xml.cost = DatatypeConverter.printCurrency(mpx.cost)

        if (mpx.costRateTableIndex != 0) {
            xml.costRateTable = BigInteger.valueOf(mpx.costRateTableIndex)
        }

        xml.creationDate = mpx.createDate
        xml.cv = DatatypeConverter.printCurrency(mpx.cv)
        xml.delay = DatatypeConverter.printDurationInIntegerTenthsOfMinutes(mpx.delay)
        xml.finish = mpx.finish
        xml.guid = mpx.guid
        xml.isHasFixedRateUnits = Boolean.valueOf(mpx.variableRateUnits == null)
        xml.isFixedMaterial = Boolean.valueOf(mpx.resource != null && mpx.resource!!.type == ResourceType.MATERIAL)
        xml.hyperlink = mpx.hyperlink
        xml.hyperlinkAddress = mpx.hyperlinkAddress
        xml.hyperlinkSubAddress = mpx.hyperlinkSubAddress
        xml.levelingDelay = DatatypeConverter.printDurationInIntegerTenthsOfMinutes(mpx.levelingDelay)
        xml.levelingDelayFormat = DatatypeConverter.printDurationTimeUnits(mpx.levelingDelay, false)

        if (!mpx.notes.isEmpty()) {
            xml.notes = mpx.notes
        }

        xml.overtimeCost = DatatypeConverter.printCurrency(mpx.overtimeCost)
        xml.overtimeWork = DatatypeConverter.printDuration(this, mpx.overtimeWork)
        xml.percentWorkComplete = NumberHelper.getBigInteger(mpx.percentageWorkComplete)
        xml.rateScale = if (mpx.variableRateUnits == null) null else DatatypeConverter.printTimeUnit(mpx.variableRateUnits)
        xml.regularWork = DatatypeConverter.printDuration(this, mpx.regularWork)
        xml.remainingCost = DatatypeConverter.printCurrency(mpx.remainingCost)
        xml.remainingOvertimeCost = DatatypeConverter.printCurrency(mpx.remainingOvertimeCost)
        xml.remainingOvertimeWork = DatatypeConverter.printDuration(this, mpx.remainingOvertimeWork)
        xml.remainingWork = DatatypeConverter.printDuration(this, mpx.remainingWork)
        xml.resourceUID = if (mpx.resource == null) BigInteger.valueOf(NULL_RESOURCE_ID.intValue()) else BigInteger.valueOf(NumberHelper.getInt(mpx.resourceUniqueID))
        xml.resume = mpx.resume
        xml.start = mpx.start
        xml.stop = mpx.stop
        xml.sv = DatatypeConverter.printCurrency(mpx.sv)
        xml.taskUID = NumberHelper.getBigInteger(mpx.task!!.uniqueID)
        xml.uid = NumberHelper.getBigInteger(mpx.uniqueID)
        xml.units = DatatypeConverter.printUnits(mpx.units)
        xml.vac = DatatypeConverter.printCurrency(mpx.vac)
        xml.work = DatatypeConverter.printDuration(this, mpx.work)
        xml.workContour = mpx.workContour

        xml.costVariance = DatatypeConverter.printCurrency(mpx.costVariance)
        xml.workVariance = DatatypeConverter.printDurationInDecimalThousandthsOfMinutes(mpx.workVariance)
        xml.startVariance = DatatypeConverter.printDurationInIntegerThousandthsOfMinutes(mpx.startVariance)
        xml.finishVariance = DatatypeConverter.printDurationInIntegerThousandthsOfMinutes(mpx.finishVariance)

        writeAssignmentBaselines(xml, mpx)

        writeAssignmentExtendedAttributes(xml, mpx)

        writeAssignmentTimephasedData(mpx, xml)

        m_eventManager!!.fireAssignmentWrittenEvent(mpx)

        return xml
    }

    /**
     * Writes assignment baseline data.
     *
     * @param xml MSPDI assignment
     * @param mpxj MPXJ assignment
     */
    private fun writeAssignmentBaselines(xml: Project.Assignments.Assignment, mpxj: ResourceAssignment) {
        var baseline = m_factory!!.createProjectAssignmentsAssignmentBaseline()
        var populated = false

        var cost = mpxj.baselineCost
        if (cost != null && cost.intValue() !== 0) {
            populated = true
            baseline.cost = DatatypeConverter.printExtendedAttributeCurrency(cost)
        }

        var date: Date? = mpxj.baselineFinish
        if (date != null) {
            populated = true
            baseline.finish = DatatypeConverter.printExtendedAttributeDate(date)
        }

        date = mpxj.baselineStart
        if (date != null) {
            populated = true
            baseline.start = DatatypeConverter.printExtendedAttributeDate(date)
        }

        var duration = mpxj.baselineWork
        if (duration != null && duration.getDuration() !== 0) {
            populated = true
            baseline.work = DatatypeConverter.printDuration(this, duration)
        }

        if (populated) {
            baseline.number = "0"
            xml.baseline.add(baseline)
        }

        for (loop in 1..10) {
            baseline = m_factory!!.createProjectAssignmentsAssignmentBaseline()
            populated = false

            cost = mpxj.getBaselineCost(loop)
            if (cost != null && cost.intValue() !== 0) {
                populated = true
                baseline.cost = DatatypeConverter.printExtendedAttributeCurrency(cost)
            }

            date = mpxj.getBaselineFinish(loop)
            if (date != null) {
                populated = true
                baseline.finish = DatatypeConverter.printExtendedAttributeDate(date)
            }

            date = mpxj.getBaselineStart(loop)
            if (date != null) {
                populated = true
                baseline.start = DatatypeConverter.printExtendedAttributeDate(date)
            }

            duration = mpxj.getBaselineWork(loop)
            if (duration != null && duration.getDuration() !== 0) {
                populated = true
                baseline.work = DatatypeConverter.printDuration(this, duration)
            }

            if (populated) {
                baseline.number = Integer.toString(loop)
                xml.baseline.add(baseline)
            }
        }
    }

    /**
     * This method writes extended attribute data for an assignment.
     *
     * @param xml MSPDI assignment
     * @param mpx MPXJ assignment
     */
    private fun writeAssignmentExtendedAttributes(xml: Project.Assignments.Assignment, mpx: ResourceAssignment) {
        var attrib: Project.Assignments.Assignment.ExtendedAttribute
        val extendedAttributes = xml.extendedAttribute

        for (mpxFieldID in allAssignmentExtendedAttributes) {
            val value = mpx.getCachedValue(mpxFieldID)

            if (FieldTypeHelper.valueIsNotDefault(mpxFieldID, value)) {
                m_extendedAttributesInUse!!.add(mpxFieldID)

                val xmlFieldID = Integer.valueOf(MPPAssignmentField.getID(mpxFieldID) or MPPAssignmentField.ASSIGNMENT_FIELD_BASE)

                attrib = m_factory!!.createProjectAssignmentsAssignmentExtendedAttribute()
                extendedAttributes.add(attrib)
                attrib.fieldID = xmlFieldID.toString()
                attrib.value = DatatypeConverter.printExtendedAttribute(this, value, mpxFieldID.getDataType())
                attrib.durationFormat = printExtendedAttributeDurationFormat(value)
            }
        }
    }

    /**
     * Writes the timephased data for a resource assignment.
     *
     * @param mpx MPXJ assignment
     * @param xml MSDPI assignment
     */
    private fun writeAssignmentTimephasedData(mpx: ResourceAssignment, xml: Project.Assignments.Assignment) {
        if (writeTimephasedData && mpx.hasTimephasedData) {
            val list = xml.timephasedData
            val calendar = mpx.calendar
            val assignmentID = xml.uid

            var complete = mpx.timephasedActualWork
            var planned = mpx.timephasedWork

            if (splitTimephasedAsDays) {
                var lastComplete: TimephasedWork? = null
                if (complete != null && !complete.isEmpty()) {
                    lastComplete = complete.get(complete.size() - 1)
                }

                var firstPlanned: TimephasedWork? = null
                if (planned != null && !planned.isEmpty()) {
                    firstPlanned = planned.get(0)
                }

                if (planned != null) {
                    planned = splitDays(calendar, mpx.timephasedWork!!, null, lastComplete)
                }

                if (complete != null) {
                    complete = splitDays(calendar, complete, firstPlanned, null)
                }
            }

            if (planned != null) {
                writeAssignmentTimephasedData(assignmentID, list, planned, 1)
            }

            if (complete != null) {
                writeAssignmentTimephasedData(assignmentID, list, complete, 2)
            }
        }
    }

    /**
     * Splits timephased data into individual days.
     *
     * @param calendar current calendar
     * @param list list of timephased assignment data
     * @param first first planned assignment
     * @param last last completed assignment
     * @return list of timephased data ready for output
     */
    private fun splitDays(calendar: ProjectCalendar?, list: List<TimephasedWork>, first: TimephasedWork?, last: TimephasedWork?): List<TimephasedWork> {
        val result = LinkedList<TimephasedWork>()

        for (assignment in list) {
            val startDate = assignment.getStart()
            val finishDate = assignment.getFinish()
            val startDay = DateHelper.getDayStartDate(startDate)
            val finishDay = DateHelper.getDayStartDate(finishDate)
            if (startDay!!.getTime() === finishDay!!.getTime()) {
                val startTime = calendar!!.getStartTime(startDay)
                var currentStart = DateHelper.setTime(startDay, startTime)
                if (startDate.getTime() > currentStart.getTime()) {
                    var paddingRequired = true

                    if (last != null) {
                        val lastFinish = last!!.getFinish()
                        if (lastFinish.getTime() === startDate.getTime()) {
                            paddingRequired = false
                        } else {
                            val lastFinishDay = DateHelper.getDayStartDate(lastFinish)
                            if (startDay!!.getTime() === lastFinishDay!!.getTime()) {
                                currentStart = lastFinish
                            }
                        }
                    }

                    if (paddingRequired) {
                        val zeroHours = Duration.getInstance(0, TimeUnit.HOURS)
                        val padding = TimephasedWork()
                        padding.setStart(currentStart)
                        padding.setFinish(startDate)
                        padding.setTotalAmount(zeroHours)
                        padding.setAmountPerDay(zeroHours)
                        result.add(padding)
                    }
                }

                result.add(assignment)

                val endTime = calendar.getFinishTime(startDay)
                var currentFinish = DateHelper.setTime(startDay, endTime)
                if (finishDate.getTime() < currentFinish.getTime()) {
                    var paddingRequired = true

                    if (first != null) {
                        val firstStart = first!!.getStart()
                        if (firstStart.getTime() === finishDate.getTime()) {
                            paddingRequired = false
                        } else {
                            val firstStartDay = DateHelper.getDayStartDate(firstStart)
                            if (finishDay!!.getTime() === firstStartDay!!.getTime()) {
                                currentFinish = firstStart
                            }
                        }
                    }

                    if (paddingRequired) {
                        val zeroHours = Duration.getInstance(0, TimeUnit.HOURS)
                        val padding = TimephasedWork()
                        padding.setStart(finishDate)
                        padding.setFinish(currentFinish)
                        padding.setTotalAmount(zeroHours)
                        padding.setAmountPerDay(zeroHours)
                        result.add(padding)
                    }
                }
            } else {
                var currentStart = startDate
                var isWorking = calendar!!.isWorkingDate(currentStart)
                while (currentStart.getTime() < finishDate.getTime()) {
                    if (isWorking) {
                        val endTime = calendar.getFinishTime(currentStart)
                        var currentFinish = DateHelper.setTime(currentStart, endTime)
                        if (currentFinish.getTime() > finishDate.getTime()) {
                            currentFinish = finishDate
                        }

                        val split = TimephasedWork()
                        split.setStart(currentStart)
                        split.setFinish(currentFinish)
                        split.setTotalAmount(assignment.getAmountPerDay())
                        split.setAmountPerDay(assignment.getAmountPerDay())
                        result.add(split)
                    }

                    val cal = DateHelper.popCalendar(currentStart)
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    currentStart = cal.getTime()
                    isWorking = calendar.isWorkingDate(currentStart)
                    if (isWorking) {
                        val startTime = calendar.getStartTime(currentStart)
                        DateHelper.setTime(cal, startTime)
                        currentStart = cal.getTime()
                    }
                    DateHelper.pushCalendar(cal)
                }
            }
        }

        return result
    }

    /**
     * Writes a list of timephased data to the MSPDI file.
     *
     * @param assignmentID current assignment ID
     * @param list output list of timephased data items
     * @param data input list of timephased data
     * @param type list type (planned or completed)
     */
    private fun writeAssignmentTimephasedData(assignmentID: BigInteger, list: List<TimephasedDataType>, data: List<TimephasedWork>, type: Int) {
        for (mpx in data) {
            val xml = m_factory!!.createTimephasedDataType()
            list.add(xml)

            xml.start = mpx.getStart()
            xml.finish = mpx.getFinish()
            xml.type = BigInteger.valueOf(type)
            xml.uid = assignmentID
            xml.unit = DatatypeConverter.printDurationTimeUnits(mpx.getTotalAmount(), false)
            xml.value = DatatypeConverter.printDuration(this, mpx.getTotalAmount())
        }
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
                CONTEXT = JAXBContext.newInstance("net.sf.mpxj.mspdi.schema", MSPDIWriter::class.java!!.getClassLoader())
            } catch (ex: JAXBException) {
                CONTEXT_EXCEPTION = ex
                CONTEXT = null
            }

        }

        // TODO share this
        private val DAY_MASKS = intArrayOf(0x00, 0x01, // Sunday
                0x02, // Monday
                0x04, // Tuesday
                0x08, // Wednesday
                0x10, // Thursday
                0x20, // Friday
                0x40)// Saturday

        private val BIGINTEGER_ZERO = BigInteger.valueOf(0)

        private val NULL_RESOURCE_ID = Integer.valueOf(-65535)

        private val NULL_CALENDAR_ID = BigInteger.valueOf(-1)
    }
}
