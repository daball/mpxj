/*
 * file:       MSPDIReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       30/12/2005
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
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigInteger
import java.nio.charset.Charset
import java.util.Collections
import java.util.Date
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import javax.xml.bind.UnmarshallerHandler
import javax.xml.bind.ValidationEvent
import javax.xml.bind.ValidationEventHandler
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLFilter
import org.xml.sax.XMLReader

import net.sf.mpxj.AssignmentField
import net.sf.mpxj.Availability
import net.sf.mpxj.AvailabilityTable
import net.sf.mpxj.CostRateTable
import net.sf.mpxj.CostRateTableEntry
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.DayType
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.FieldType
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectCalendarWeek
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Rate
import net.sf.mpxj.RecurrenceType
import net.sf.mpxj.RecurringData
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceField
import net.sf.mpxj.ResourceType
import net.sf.mpxj.ScheduleFrom
import net.sf.mpxj.SubProject
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.TaskMode
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.TimephasedWork
import net.sf.mpxj.common.BooleanHelper
import net.sf.mpxj.common.CharsetHelper
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.DefaultTimephasedWorkContainer
import net.sf.mpxj.common.FieldTypeHelper
import net.sf.mpxj.common.MPPAssignmentField
import net.sf.mpxj.common.MPPResourceField
import net.sf.mpxj.common.MPPTaskField
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.Pair
import net.sf.mpxj.common.SplitTaskFactory
import net.sf.mpxj.common.TimephasedWorkNormaliser
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.mspdi.schema.Project
import net.sf.mpxj.mspdi.schema.Project.Calendars.Calendar.WorkWeeks
import net.sf.mpxj.mspdi.schema.Project.Calendars.Calendar.WorkWeeks.WorkWeek
import net.sf.mpxj.mspdi.schema.Project.Calendars.Calendar.WorkWeeks.WorkWeek.WeekDays
import net.sf.mpxj.mspdi.schema.Project.Calendars.Calendar.WorkWeeks.WorkWeek.WeekDays.WeekDay
import net.sf.mpxj.mspdi.schema.Project.Resources.Resource.AvailabilityPeriods
import net.sf.mpxj.mspdi.schema.Project.Resources.Resource.AvailabilityPeriods.AvailabilityPeriod
import net.sf.mpxj.mspdi.schema.Project.Resources.Resource.Rates
import net.sf.mpxj.mspdi.schema.TimephasedDataType
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * This class creates a new ProjectFile instance by reading an MSPDI file.
 */
class MSPDIReader : AbstractProjectReader() {

    /**
     * Retrieves a flag indicating that this class will attempt to correct
     * and read XML which is not compliant with the XML Schema. This
     * behaviour matches that of Microsoft Project when reading the
     * same data.
     *
     * @return Boolean flag
     */
    /**
     * Sets a flag indicating that this class will attempt to correct
     * and read XML which is not compliant with the XML Schema. This
     * behaviour matches that of Microsoft Project when reading the
     * same data.
     *
     * @param flag input compatibility flag
     */
    var microsoftProjectCompatibleInput: Boolean
        get() = m_compatibleInput
        set(flag) {
            m_compatibleInput = flag
        }

    private var m_compatibleInput = true
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
    private var charset: Charset? = null
        get() {
            var result = field
            if (result == null) {
                result = if (m_encoding == null) CharsetHelper.UTF8 else Charset.forName(m_encoding)
            }
            return result
        }
        set
    private var m_projectFile: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_projectListeners: List<ProjectListener>? = null
    /**
     * Sets the character encoding used when reading an MSPDI file.
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
            config.autoTaskID = false
            config.autoTaskUniqueID = false
            config.autoResourceID = false
            config.autoResourceUniqueID = false
            config.autoOutlineLevel = false
            config.autoOutlineNumber = false
            config.autoWBS = false
            config.autoCalendarUniqueID = false
            config.autoAssignmentUniqueID = false

            m_eventManager!!.addProjectListeners(m_projectListeners)

            val factory = SAXParserFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            factory.setNamespaceAware(true)
            val saxParser = factory.newSAXParser()
            val xmlReader = saxParser.getXMLReader()

            if (CONTEXT == null) {
                throw CONTEXT_EXCEPTION
            }

            DatatypeConverter.setParentFile(m_projectFile)
            val unmarshaller = CONTEXT!!.createUnmarshaller()

            //
            // If we are matching the behaviour of MS project, then we need to
            // ignore validation warnings.
            //
            if (m_compatibleInput == true) {
                unmarshaller.setEventHandler(object : ValidationEventHandler() {
                    @Override
                    fun handleEvent(event: ValidationEvent): Boolean {
                        return true
                    }
                })
            }

            val filter = NamespaceFilter()
            filter.setParent(xmlReader)
            val unmarshallerHandler = unmarshaller.getUnmarshallerHandler()
            filter.setContentHandler(unmarshallerHandler)
            filter.parse(InputSource(InputStreamReader(stream, charset)))
            val project = unmarshallerHandler.getResult() as Project

            val calendarMap = HashMap<BigInteger, ProjectCalendar>()

            readProjectProperties(project)
            readProjectExtendedAttributes(project)
            readCalendars(project, calendarMap)
            readResources(project, calendarMap)
            readTasks(project)
            readAssignments(project)

            //
            // Ensure that the unique ID counters are correct
            //
            config.updateUniqueCounters()

            //
            // Ensure that the default calendar name is set in the project properties
            //
            val defaultCalendar = calendarMap.get(project.calendarUID)
            if (defaultCalendar != null) {
                m_projectFile!!.projectProperties.defaultCalendarName = defaultCalendar!!.name
            }

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
        }
    }

    /**
     * This method extracts project properties from an MSPDI file.
     *
     * @param project Root node of the MSPDI file
     */
    private fun readProjectProperties(project: Project) {
        val properties = m_projectFile!!.projectProperties

        properties.actualsInSync = BooleanHelper.getBoolean(project.isActualsInSync)
        properties.adminProject = BooleanHelper.getBoolean(project.isAdminProject)
        properties.applicationVersion = NumberHelper.getInteger(project.saveVersion)
        properties.author = project.author
        properties.autoAddNewResourcesAndTasks = BooleanHelper.getBoolean(project.isAutoAddNewResourcesAndTasks)
        properties.autolink = BooleanHelper.getBoolean(project.isAutolink)
        properties.baselineForEarnedValue = NumberHelper.getInteger(project.baselineForEarnedValue)
        properties.defaultCalendarName = if (project.calendarUID == null) null else project.calendarUID.toString()
        properties.category = project.category
        properties.company = project.company
        properties.creationDate = project.creationDate
        properties.criticalSlackLimit = NumberHelper.getInteger(project.criticalSlackLimit)
        properties.currencyDigits = NumberHelper.getInteger(project.currencyDigits)
        properties.currencyCode = project.currencyCode
        properties.currencySymbol = project.currencySymbol
        properties.currentDate = project.currentDate
        properties.daysPerMonth = NumberHelper.getInteger(project.daysPerMonth)
        properties.defaultDurationUnits = DatatypeConverter.parseDurationTimeUnits(project.durationFormat)
        properties.defaultEndTime = project.defaultFinishTime
        properties.defaultFixedCostAccrual = project.defaultFixedCostAccrual
        properties.defaultOvertimeRate = DatatypeConverter.parseRate(project.defaultOvertimeRate)
        properties.defaultStandardRate = DatatypeConverter.parseRate(project.defaultStandardRate)
        properties.defaultStartTime = project.defaultStartTime
        properties.defaultTaskEarnedValueMethod = DatatypeConverter.parseEarnedValueMethod(project.defaultTaskEVMethod)
        properties.defaultTaskType = project.defaultTaskType
        properties.defaultWorkUnits = DatatypeConverter.parseWorkUnits(project.workFormat)
        properties.earnedValueMethod = DatatypeConverter.parseEarnedValueMethod(project.earnedValueMethod)
        properties.editableActualCosts = BooleanHelper.getBoolean(project.isEditableActualCosts)
        properties.extendedCreationDate = project.extendedCreationDate
        properties.finishDate = project.finishDate
        properties.fiscalYearStart = BooleanHelper.getBoolean(project.isFiscalYearStart)
        properties.fiscalYearStartMonth = NumberHelper.getInteger(project.fyStartDate)
        properties.honorConstraints = BooleanHelper.getBoolean(project.isHonorConstraints)
        properties.insertedProjectsLikeSummary = BooleanHelper.getBoolean(project.isInsertedProjectsLikeSummary)
        properties.lastSaved = project.lastSaved
        properties.manager = project.manager
        properties.microsoftProjectServerURL = BooleanHelper.getBoolean(project.isMicrosoftProjectServerURL)
        properties.minutesPerDay = NumberHelper.getInteger(project.minutesPerDay)
        properties.minutesPerWeek = NumberHelper.getInteger(project.minutesPerWeek)
        properties.moveCompletedEndsBack = BooleanHelper.getBoolean(project.isMoveCompletedEndsBack)
        properties.moveCompletedEndsForward = BooleanHelper.getBoolean(project.isMoveCompletedEndsForward)
        properties.moveRemainingStartsBack = BooleanHelper.getBoolean(project.isMoveRemainingStartsBack)
        properties.moveRemainingStartsForward = BooleanHelper.getBoolean(project.isMoveRemainingStartsForward)
        properties.multipleCriticalPaths = BooleanHelper.getBoolean(project.isMultipleCriticalPaths)
        properties.name = project.name
        properties.newTasksEffortDriven = BooleanHelper.getBoolean(project.isNewTasksEffortDriven)
        properties.newTasksEstimated = BooleanHelper.getBoolean(project.isNewTasksEstimated)
        properties.newTaskStartIsProjectStart = NumberHelper.getInt(project.newTaskStartDate) == 0
        properties.projectExternallyEdited = BooleanHelper.getBoolean(project.isProjectExternallyEdited)
        properties.projectTitle = project.title
        properties.removeFileProperties = BooleanHelper.getBoolean(project.isRemoveFileProperties)
        properties.revision = NumberHelper.getInteger(project.revision)
        properties.scheduleFrom = if (BooleanHelper.getBoolean(project.isScheduleFromStart)) ScheduleFrom.START else ScheduleFrom.FINISH
        properties.subject = project.subject
        properties.splitInProgressTasks = BooleanHelper.getBoolean(project.isSplitsInProgressTasks)
        properties.spreadActualCost = BooleanHelper.getBoolean(project.isSpreadActualCost)
        properties.spreadPercentComplete = BooleanHelper.getBoolean(project.isSpreadPercentComplete)
        properties.startDate = project.startDate
        properties.statusDate = project.statusDate
        properties.symbolPosition = project.currencySymbolPosition
        properties.uniqueID = project.uid
        properties.updatingTaskStatusUpdatesResourceStatus = BooleanHelper.getBoolean(project.isTaskUpdatesResource)
        properties.weekStartDay = DatatypeConverter.parseDay(project.weekStartDay)
        updateScheduleSource(properties)
    }

    /**
     * Populate the properties indicating the source of this schedule.
     *
     * @param properties project properties
     */
    private fun updateScheduleSource(properties: ProjectProperties) {
        // Rudimentary identification of schedule source
        if (properties.company != null && properties.company.equals("Synchro Software Ltd")) {
            properties.fileApplication = "Synchro"
        } else {
            if (properties.author != null && properties.author.equals("SG Project")) {
                properties.fileApplication = "Simple Genius"
            } else {
                properties.fileApplication = "Microsoft"
            }
        }
        properties.fileType = "MSPDI"
    }

    /**
     * This method extracts calendar data from an MSPDI file.
     *
     * @param project Root node of the MSPDI file
     * @param map Map of calendar UIDs to names
     */
    private fun readCalendars(project: Project, map: HashMap<BigInteger, ProjectCalendar>) {
        val calendars = project.calendars
        if (calendars != null) {
            val baseCalendars = LinkedList<Pair<ProjectCalendar, BigInteger>>()
            for (cal in calendars.calendar) {
                readCalendar(cal, map, baseCalendars)
            }
            updateBaseCalendarNames(baseCalendars, map)
        }

        try {
            val properties = m_projectFile!!.projectProperties
            val calendarID = BigInteger(properties.defaultCalendarName)
            val calendar = map.get(calendarID)
            m_projectFile!!.defaultCalendar = calendar
        } catch (ex: Exception) {
            // Ignore exceptions
        }

    }

    /**
     * This method extracts data for a single calendar from an MSPDI file.
     *
     * @param calendar Calendar data
     * @param map Map of calendar UIDs to names
     * @param baseCalendars list of base calendars
     */
    private fun readCalendar(calendar: Project.Calendars.Calendar, map: HashMap<BigInteger, ProjectCalendar>, baseCalendars: List<Pair<ProjectCalendar, BigInteger>>) {
        val bc = m_projectFile!!.addCalendar()
        bc.uniqueID = NumberHelper.getInteger(calendar.uid)
        bc.name = calendar.name
        val baseCalendarID = calendar.baseCalendarUID
        if (baseCalendarID != null) {
            baseCalendars.add(Pair<ProjectCalendar, BigInteger>(bc, baseCalendarID))
        }

        readExceptions(calendar, bc)
        val readExceptionsFromDays = bc.calendarExceptions.isEmpty()

        val days = calendar.weekDays
        if (days != null) {
            for (weekDay in days.weekDay) {
                readDay(bc, weekDay, readExceptionsFromDays)
            }
        } else {
            bc.setWorkingDay(Day.SUNDAY, DayType.DEFAULT)
            bc.setWorkingDay(Day.MONDAY, DayType.DEFAULT)
            bc.setWorkingDay(Day.TUESDAY, DayType.DEFAULT)
            bc.setWorkingDay(Day.WEDNESDAY, DayType.DEFAULT)
            bc.setWorkingDay(Day.THURSDAY, DayType.DEFAULT)
            bc.setWorkingDay(Day.FRIDAY, DayType.DEFAULT)
            bc.setWorkingDay(Day.SATURDAY, DayType.DEFAULT)
        }

        readWorkWeeks(calendar, bc)

        map.put(calendar.uid, bc)

        m_eventManager!!.fireCalendarReadEvent(bc)
    }

    /**
     * This method extracts data for a single day from an MSPDI file.
     *
     * @param calendar Calendar data
     * @param day Day data
     * @param readExceptionsFromDays read exceptions form day definitions
     */
    private fun readDay(calendar: ProjectCalendar, day: Project.Calendars.Calendar.WeekDays.WeekDay, readExceptionsFromDays: Boolean) {
        val dayType = day.dayType
        if (dayType != null) {
            if (dayType.intValue() === 0) {
                if (readExceptionsFromDays) {
                    readExceptionDay(calendar, day)
                }
            } else {
                readNormalDay(calendar, day)
            }
        }
    }

    /**
     * This method extracts data for a normal working day from an MSPDI file.
     *
     * @param calendar Calendar data
     * @param weekDay Day data
     */
    private fun readNormalDay(calendar: ProjectCalendar, weekDay: Project.Calendars.Calendar.WeekDays.WeekDay) {
        val dayNumber = weekDay.dayType.intValue()
        val day = Day.getInstance(dayNumber)
        calendar.setWorkingDay(day, BooleanHelper.getBoolean(weekDay.isDayWorking))
        val hours = calendar.addCalendarHours(day)

        val times = weekDay.workingTimes
        if (times != null) {
            for (period in times.workingTime) {
                val startTime = period.fromTime
                var endTime: Date? = period.toTime

                if (startTime != null && endTime != null) {
                    if (startTime!!.getTime() >= endTime!!.getTime()) {
                        endTime = DateHelper.addDays(endTime, 1)
                    }

                    hours.addRange(DateRange(startTime, endTime))
                }
            }
        }
    }

    /**
     * This method extracts data for an exception day from an MSPDI file.
     *
     * @param calendar Calendar data
     * @param day Day data
     */
    private fun readExceptionDay(calendar: ProjectCalendar, day: Project.Calendars.Calendar.WeekDays.WeekDay) {
        val timePeriod = day.timePeriod
        val fromDate = timePeriod.fromDate
        val toDate = timePeriod.toDate
        val times = day.workingTimes
        val exception = calendar.addCalendarException(fromDate, toDate)

        if (times != null) {
            val time = times.workingTime
            for (period in time) {
                val startTime = period.fromTime
                var endTime: Date? = period.toTime

                if (startTime != null && endTime != null) {
                    if (startTime!!.getTime() >= endTime!!.getTime()) {
                        endTime = DateHelper.addDays(endTime, 1)
                    }

                    exception.addRange(DateRange(startTime, endTime))
                }
            }
        }
    }

    /**
     * Reads any exceptions present in the file. This is only used in MSPDI
     * file versions saved by Project 2007 and later.
     *
     * @param calendar XML calendar
     * @param bc MPXJ calendar
     */
    private fun readExceptions(calendar: Project.Calendars.Calendar, bc: ProjectCalendar) {
        val exceptions = calendar.exceptions
        if (exceptions != null) {
            for (exception in exceptions.exception) {
                readException(bc, exception)
            }
        }
    }

    /**
     * Read a single calendar exception.
     *
     * @param bc parent calendar
     * @param exception exception data
     */
    private fun readException(bc: ProjectCalendar, exception: Project.Calendars.Calendar.Exceptions.Exception) {
        val fromDate = exception.timePeriod.fromDate
        val toDate = exception.timePeriod.toDate

        // Vico Schedule Planner seems to write start and end dates to FromTime and ToTime
        // rather than FromDate and ToDate. This is plain wrong, and appears to be ignored by MS Project
        // so we will ignore it too!
        if (fromDate != null && toDate != null) {
            val bce = bc.addCalendarException(fromDate, toDate)
            bce.name = exception.name
            readRecurringData(bce, exception)
            val times = exception.workingTimes
            if (times != null) {
                val time = times.workingTime
                for (period in time) {
                    val startTime = period.fromTime
                    var endTime: Date? = period.toTime

                    if (startTime != null && endTime != null) {
                        if (startTime!!.getTime() >= endTime!!.getTime()) {
                            endTime = DateHelper.addDays(endTime, 1)
                        }

                        bce.addRange(DateRange(startTime, endTime))
                    }
                }
            }
        }
    }

    /**
     * Read recurring data for a calendar exception.
     *
     * @param bce MPXJ calendar exception
     * @param exception XML calendar exception
     */
    private fun readRecurringData(bce: ProjectCalendarException, exception: Project.Calendars.Calendar.Exceptions.Exception) {
        val rt = getRecurrenceType(NumberHelper.getInt(exception.type))
        if (rt != null) {
            val rd = RecurringData()
            rd.startDate = bce.fromDate
            rd.finishDate = bce.toDate
            rd.recurrenceType = rt
            rd.relative = getRelative(NumberHelper.getInt(exception.type))
            rd.occurrences = NumberHelper.getInteger(exception.occurrences)

            when (rd.recurrenceType) {
                RecurrenceType.DAILY -> {
                    rd.frequency = getFrequency(exception)
                }

                RecurrenceType.WEEKLY -> {
                    rd.setWeeklyDaysFromBitmap(NumberHelper.getInteger(exception.daysOfWeek), DAY_MASKS)
                    rd.frequency = getFrequency(exception)
                }

                RecurrenceType.MONTHLY -> {
                    if (rd.relative) {
                        rd.dayOfWeek = Day.getInstance(NumberHelper.getInt(exception.monthItem) - 2)
                        rd.dayNumber = Integer.valueOf(NumberHelper.getInt(exception.monthPosition) + 1)
                    } else {
                        rd.dayNumber = NumberHelper.getInteger(exception.monthDay)
                    }
                    rd.frequency = getFrequency(exception)
                }

                RecurrenceType.YEARLY -> {
                    if (rd.relative) {
                        rd.dayOfWeek = Day.getInstance(NumberHelper.getInt(exception.monthItem) - 2)
                        rd.dayNumber = Integer.valueOf(NumberHelper.getInt(exception.monthPosition) + 1)
                    } else {
                        rd.dayNumber = NumberHelper.getInteger(exception.monthDay)
                    }
                    rd.monthNumber = Integer.valueOf(NumberHelper.getInt(exception.month) + 1)
                }
            }

            if (rd.recurrenceType != RecurrenceType.DAILY || rd.dates.size > 1) {
                bce.recurring = rd
            }
        }
    }

    /**
     * Retrieve the recurrence type.
     *
     * @param value integer value
     * @return RecurrenceType instance
     */
    private fun getRecurrenceType(value: Int): RecurrenceType? {
        val result: RecurrenceType?
        if (value < 0 || value >= RECURRENCE_TYPES.size) {
            result = null
        } else {
            result = RECURRENCE_TYPES[value]
        }

        return result
    }

    /**
     * Determine if the exception is relative based on the recurrence type integer value.
     *
     * @param value integer value
     * @return true if the recurrence is relative
     */
    private fun getRelative(value: Int): Boolean {
        val result: Boolean
        if (value < 0 || value >= RELATIVE_MAP.size) {
            result = false
        } else {
            result = RELATIVE_MAP[value]
        }

        return result
    }

    /**
     * Retrieve the frequency of an exception.
     *
     * @param exception XML calendar exception
     * @return frequency
     */
    private fun getFrequency(exception: Project.Calendars.Calendar.Exceptions.Exception): Integer? {
        var period: Integer? = NumberHelper.getInteger(exception.period)
        if (period == null) {
            period = Integer.valueOf(1)
        }
        return period
    }

    /**
     * Read the work weeks associated with this calendar.
     *
     * @param xmlCalendar XML calendar object
     * @param mpxjCalendar MPXJ calendar object
     */
    private fun readWorkWeeks(xmlCalendar: Project.Calendars.Calendar, mpxjCalendar: ProjectCalendar) {
        val ww = xmlCalendar.workWeeks
        if (ww != null) {
            for (xmlWeek in ww.workWeek) {
                val week = mpxjCalendar.addWorkWeek()
                week.name = xmlWeek.name
                var startTime: Date? = xmlWeek.timePeriod.fromDate
                var endTime: Date? = xmlWeek.timePeriod.toDate
                week.dateRange = DateRange(startTime, endTime)

                val xmlWeekDays = xmlWeek.weekDays
                if (xmlWeekDays != null) {
                    for (xmlWeekDay in xmlWeekDays!!.getWeekDay()) {
                        val dayNumber = xmlWeekDay.dayType.intValue()
                        val day = Day.getInstance(dayNumber)
                        week.setWorkingDay(day, BooleanHelper.getBoolean(xmlWeekDay.isDayWorking))
                        val hours = week.addCalendarHours(day)

                        val times = xmlWeekDay.workingTimes
                        if (times != null) {
                            for (period in times!!.getWorkingTime()) {
                                startTime = period.fromTime
                                endTime = period.toTime

                                if (startTime != null && endTime != null) {
                                    if (startTime!!.getTime() >= endTime!!.getTime()) {
                                        endTime = DateHelper.addDays(endTime, 1)
                                    }

                                    hours.addRange(DateRange(startTime, endTime))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This method extracts project extended attribute data from an MSPDI file.
     *
     * @param project Root node of the MSPDI file
     */
    private fun readProjectExtendedAttributes(project: Project) {
        val attributes = project.extendedAttributes
        if (attributes != null) {
            for (ea in attributes.extendedAttribute) {
                readFieldAlias(ea)
            }
        }
    }

    /**
     * Read a single field alias from an extended attribute.
     *
     * @param attribute extended attribute
     */
    private fun readFieldAlias(attribute: Project.ExtendedAttributes.ExtendedAttribute) {
        val alias = attribute.alias
        if (alias != null && alias.length() !== 0) {
            val field = FieldTypeHelper.getInstance(Integer.parseInt(attribute.fieldID))
            m_projectFile!!.customFields.getCustomField(field).setAlias(attribute.alias)
        }
    }

    /**
     * This method extracts resource data from an MSPDI file.
     *
     * @param project Root node of the MSPDI file
     * @param calendarMap Map of calendar UIDs to names
     */
    private fun readResources(project: Project, calendarMap: HashMap<BigInteger, ProjectCalendar>) {
        val resources = project.resources
        if (resources != null) {
            for (resource in resources.resource) {
                readResource(resource, calendarMap)
            }
        }
    }

    /**
     * This method extracts data for a single resource from an MSPDI file.
     *
     * @param xml Resource data
     * @param calendarMap Map of calendar UIDs to names
     */
    private fun readResource(xml: Project.Resources.Resource, calendarMap: HashMap<BigInteger, ProjectCalendar>) {
        val mpx = m_projectFile!!.addResource()

        mpx.accrueAt = xml.accrueAt
        mpx.setActveDirectoryGUID(xml.activeDirectoryGUID)
        mpx.actualCost = DatatypeConverter.parseCurrency(xml.actualCost)
        mpx.actualOvertimeCost = DatatypeConverter.parseCurrency(xml.actualOvertimeCost)
        mpx.actualOvertimeWork = DatatypeConverter.parseDuration(m_projectFile, null, xml.actualOvertimeWork)
        mpx.actualOvertimeWorkProtected = DatatypeConverter.parseDuration(m_projectFile, null, xml.actualOvertimeWorkProtected)
        mpx.actualWork = DatatypeConverter.parseDuration(m_projectFile, null, xml.actualWork)
        mpx.actualWorkProtected = DatatypeConverter.parseDuration(m_projectFile, null, xml.actualWorkProtected)
        mpx.acwp = DatatypeConverter.parseCurrency(xml.acwp)
        mpx.availableFrom = xml.availableFrom
        mpx.availableTo = xml.availableTo
        mpx.bcws = DatatypeConverter.parseCurrency(xml.bcws)
        mpx.bcwp = DatatypeConverter.parseCurrency(xml.bcwp)
        mpx.bookingType = xml.bookingType
        //mpx.setBaseCalendar ();
        //mpx.setBaselineCost();
        //mpx.setBaselineWork();
        mpx.budget = BooleanHelper.getBoolean(xml.isIsBudget)
        mpx.canLevel = BooleanHelper.getBoolean(xml.isCanLevel)
        mpx.code = xml.code
        mpx.cost = DatatypeConverter.parseCurrency(xml.cost)
        mpx.costPerUse = DatatypeConverter.parseCurrency(xml.costPerUse)
        mpx.costVariance = DatatypeConverter.parseCurrency(xml.costVariance)
        mpx.creationDate = xml.creationDate
        mpx.cv = DatatypeConverter.parseCurrency(xml.cv)
        mpx.emailAddress = xml.emailAddress
        mpx.group = xml.group
        mpx.guid = xml.guid
        mpx.hyperlink = xml.hyperlink
        mpx.hyperlinkAddress = xml.hyperlinkAddress
        mpx.hyperlinkSubAddress = xml.hyperlinkSubAddress
        mpx.id = NumberHelper.getInteger(xml.id)
        mpx.initials = xml.initials
        mpx.enterprise = BooleanHelper.getBoolean(xml.isIsEnterprise)
        mpx.generic = BooleanHelper.getBoolean(xml.isIsGeneric)
        mpx.active = !BooleanHelper.getBoolean(xml.isIsInactive)
        mpx.setIsNull(BooleanHelper.getBoolean(xml.isIsNull))
        //mpx.setLinkedFields();
        mpx.materialLabel = xml.materialLabel
        mpx.maxUnits = DatatypeConverter.parseUnits(xml.maxUnits)
        mpx.name = xml.name
        if (xml.notes != null && xml.notes.length() !== 0) {
            mpx.notes = xml.notes
        }
        mpx.ntAccount = xml.ntAccount
        //mpx.setObjects();
        mpx.overtimeCost = DatatypeConverter.parseCurrency(xml.overtimeCost)
        mpx.overtimeRate = DatatypeConverter.parseRate(xml.overtimeRate)
        mpx.overtimeRateUnits = DatatypeConverter.parseTimeUnit(xml.overtimeRateFormat)
        mpx.overtimeWork = DatatypeConverter.parseDuration(m_projectFile, null, xml.overtimeWork)
        mpx.peakUnits = DatatypeConverter.parseUnits(xml.peakUnits)
        mpx.percentWorkComplete = xml.percentWorkComplete
        mpx.phonetics = xml.phonetics
        mpx.regularWork = DatatypeConverter.parseDuration(m_projectFile, null, xml.regularWork)
        mpx.remainingCost = DatatypeConverter.parseCurrency(xml.remainingCost)
        mpx.remainingOvertimeCost = DatatypeConverter.parseCurrency(xml.remainingOvertimeCost)
        mpx.remainingWork = DatatypeConverter.parseDuration(m_projectFile, null, xml.remainingWork)
        mpx.remainingOvertimeWork = DatatypeConverter.parseDuration(m_projectFile, null, xml.remainingOvertimeWork)
        mpx.standardRate = DatatypeConverter.parseRate(xml.standardRate)
        mpx.standardRateUnits = DatatypeConverter.parseTimeUnit(xml.standardRateFormat)
        mpx.sv = DatatypeConverter.parseCurrency(xml.sv)
        mpx.type = xml.type
        mpx.uniqueID = NumberHelper.getInteger(xml.uid)
        mpx.work = DatatypeConverter.parseDuration(m_projectFile, null, xml.work)
        mpx.workGroup = xml.workGroup
        mpx.workVariance = DatatypeConverter.parseDurationInThousanthsOfMinutes(xml.workVariance)

        if (mpx.type == ResourceType.MATERIAL && BooleanHelper.getBoolean(xml.isIsCostResource)) {
            mpx.type = ResourceType.COST
        }

        readResourceExtendedAttributes(xml, mpx)

        readResourceBaselines(xml, mpx)

        mpx.resourceCalendar = calendarMap.get(xml.calendarUID)

        // ensure that we cache this value
        mpx.overAllocated = BooleanHelper.getBoolean(xml.isOverAllocated)

        readCostRateTables(mpx, xml.rates)

        readAvailabilityTable(mpx, xml.availabilityPeriods)

        m_eventManager!!.fireResourceReadEvent(mpx)
    }

    /**
     * Reads baseline values for the current resource.
     *
     * @param xmlResource MSPDI resource instance
     * @param mpxjResource MPXJ resource instance
     */
    private fun readResourceBaselines(xmlResource: Project.Resources.Resource, mpxjResource: Resource) {
        for (baseline in xmlResource.baseline) {
            val number = NumberHelper.getInt(baseline.number)

            val cost = DatatypeConverter.parseCurrency(baseline.cost)
            val work = DatatypeConverter.parseDuration(m_projectFile, TimeUnit.HOURS, baseline.work)

            if (number == 0) {
                mpxjResource.baselineCost = cost
                mpxjResource.baselineWork = work
            } else {
                mpxjResource.setBaselineCost(number, cost)
                mpxjResource.setBaselineWork(number, work)
            }
        }
    }

    /**
     * This method processes any extended attributes associated with a resource.
     *
     * @param xml MSPDI resource instance
     * @param mpx MPX resource instance
     */
    private fun readResourceExtendedAttributes(xml: Project.Resources.Resource, mpx: Resource) {
        for (attrib in xml.extendedAttribute) {
            val xmlFieldID = Integer.parseInt(attrib.fieldID) and 0x0000FFFF
            val mpxFieldID = MPPResourceField.getInstance(xmlFieldID)
            val durationFormat = DatatypeConverter.parseDurationTimeUnits(attrib.durationFormat, null)
            DatatypeConverter.parseExtendedAttribute(m_projectFile, mpx, attrib.value, mpxFieldID, durationFormat)
        }
    }

    /**
     * Reads the cost rate tables from the file.
     *
     * @param resource parent resource
     * @param rates XML cot rate tables
     */
    private fun readCostRateTables(resource: Resource, rates: Rates?) {
        if (rates == null) {
            var table = CostRateTable()
            table.add(CostRateTableEntry.DEFAULT_ENTRY)
            resource.setCostRateTable(0, table)

            table = CostRateTable()
            table.add(CostRateTableEntry.DEFAULT_ENTRY)
            resource.setCostRateTable(1, table)

            table = CostRateTable()
            table.add(CostRateTableEntry.DEFAULT_ENTRY)
            resource.setCostRateTable(2, table)

            table = CostRateTable()
            table.add(CostRateTableEntry.DEFAULT_ENTRY)
            resource.setCostRateTable(3, table)

            table = CostRateTable()
            table.add(CostRateTableEntry.DEFAULT_ENTRY)
            resource.setCostRateTable(4, table)
        } else {
            val tables = HashSet<CostRateTable>()

            for (rate in rates.rate) {
                val standardRate = DatatypeConverter.parseRate(rate.standardRate)
                val standardRateFormat = DatatypeConverter.parseTimeUnit(rate.standardRateFormat)
                val overtimeRate = DatatypeConverter.parseRate(rate.overtimeRate)
                val overtimeRateFormat = DatatypeConverter.parseTimeUnit(rate.overtimeRateFormat)
                val costPerUse = DatatypeConverter.parseCurrency(rate.costPerUse)
                val endDate = rate.ratesTo

                val entry = CostRateTableEntry(standardRate, standardRateFormat, overtimeRate, overtimeRateFormat, costPerUse, endDate)

                val tableIndex = rate.rateTable.intValue()
                var table: CostRateTable? = resource.getCostRateTable(tableIndex)
                if (table == null) {
                    table = CostRateTable()
                    resource.setCostRateTable(tableIndex, table)
                }
                table!!.add(entry)
                tables.add(table)
            }

            for (table in tables) {
                Collections.sort(table)
            }
        }
    }

    /**
     * Reads the availability table from the file.
     *
     * @param resource MPXJ resource instance
     * @param periods MSPDI availability periods
     */
    private fun readAvailabilityTable(resource: Resource, periods: AvailabilityPeriods?) {
        if (periods != null) {
            val table = resource.availability
            val list = periods.availabilityPeriod
            for (period in list) {
                val start = period.availableFrom
                val end = period.availableTo
                val units = DatatypeConverter.parseUnits(period.availableUnits)
                val availability = Availability(start, end, units)
                table.add(availability)
            }
            Collections.sort(table)
        }
    }

    /**
     * This method extracts task data from an MSPDI file.
     *
     * @param project Root node of the MSPDI file
     */
    private fun readTasks(project: Project) {
        val tasks = project.tasks
        if (tasks != null) {
            var tasksWithoutIDCount = 0

            for (task in tasks.task) {
                val mpxjTask = readTask(task)
                if (mpxjTask.id == null) {
                    ++tasksWithoutIDCount
                }
            }

            for (task in tasks.task) {
                readPredecessors(task)
            }

            //
            // MS Project will happily read tasks from an MSPDI file without IDs,
            // it will just generate ID values based on the task order in the file.
            // If we find that there are no ID values present, we'll do the same.
            //
            if (tasksWithoutIDCount == tasks.task.size()) {
                m_projectFile!!.tasks.renumberIDs()
            }
        }

        m_projectFile!!.updateStructure()
    }

    /**
     * This method extracts data for a single task from an MSPDI file.
     *
     * @param xml Task data
     * @return Task instance
     */
    private fun readTask(xml: Project.Tasks.Task): Task {
        val mpx = m_projectFile!!.addTask()
        mpx.`null` = BooleanHelper.getBoolean(xml.isIsNull)
        mpx.id = NumberHelper.getInteger(xml.id)
        mpx.uniqueID = NumberHelper.getInteger(xml.uid)

        if (!mpx.`null`) {
            //
            // Set the duration format up front as this is required later
            //
            val durationFormat = DatatypeConverter.parseDurationTimeUnits(xml.durationFormat)

            mpx.active = if (xml.isActive == null) true else BooleanHelper.getBoolean(xml.isActive)
            mpx.actualCost = DatatypeConverter.parseCurrency(xml.actualCost)
            mpx.actualDuration = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.actualDuration)
            mpx.actualFinish = xml.actualFinish
            mpx.actualOvertimeCost = DatatypeConverter.parseCurrency(xml.actualOvertimeCost)
            mpx.actualOvertimeWork = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.actualOvertimeWork)
            mpx.actualOvertimeWorkProtected = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.actualOvertimeWorkProtected)
            mpx.actualStart = xml.actualStart
            mpx.actualWork = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.actualWork)
            mpx.actualWorkProtected = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.actualWorkProtected)
            mpx.acwp = DatatypeConverter.parseCurrency(xml.acwp)
            //mpx.setBaselineCost();
            //mpx.setBaselineDuration();
            //mpx.setBaselineFinish();
            //mpx.setBaselineStart();
            //mpx.setBaselineWork();
            //mpx.setBCWP();
            //mpx.setBCWS();
            mpx.calendar = getTaskCalendar(xml)
            //mpx.setConfirmed();
            mpx.constraintDate = xml.constraintDate
            mpx.constraintType = DatatypeConverter.parseConstraintType(xml.constraintType)
            mpx.contact = xml.contact
            mpx.cost = DatatypeConverter.parseCurrency(xml.cost)
            //mpx.setCost1();
            //mpx.setCost2();
            //mpx.setCost3();
            //mpx.setCostVariance();
            mpx.createDate = xml.createDate
            mpx.cv = DatatypeConverter.parseCurrency(xml.cv)
            mpx.deadline = xml.deadline
            //mpx.setDelay();
            mpx.duration = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.duration)
            mpx.durationText = xml.durationText
            //mpx.setDuration1();
            //mpx.setDuration2();
            //mpx.setDuration3();
            //mpx.setDurationVariance();
            mpx.earlyFinish = xml.earlyFinish
            mpx.earlyStart = xml.earlyStart
            mpx.earnedValueMethod = DatatypeConverter.parseEarnedValueMethod(xml.earnedValueMethod)
            mpx.effortDriven = BooleanHelper.getBoolean(xml.isEffortDriven)
            mpx.estimated = BooleanHelper.getBoolean(xml.isEstimated)
            mpx.externalTask = BooleanHelper.getBoolean(xml.isExternalTask)
            mpx.project = xml.externalTaskProject
            mpx.finish = xml.finish
            mpx.finishText = xml.finishText
            //mpx.setFinish1();
            //mpx.setFinish2();
            //mpx.setFinish3();
            //mpx.setFinish4();
            //mpx.setFinish5();
            mpx.finishVariance = DatatypeConverter.parseDurationInThousanthsOfMinutes(xml.finishVariance)
            //mpx.setFixed();
            mpx.fixedCost = DatatypeConverter.parseCurrency(xml.fixedCost)
            mpx.fixedCostAccrual = xml.fixedCostAccrual
            //mpx.setFlag1();
            //mpx.setFlag2();
            //mpx.setFlag3();
            //mpx.setFlag4();
            //mpx.setFlag5();
            //mpx.setFlag6();
            //mpx.setFlag7();
            //mpx.setFlag8();
            //mpx.setFlag9();
            //mpx.setFlag10();
            // This is not correct?
            mpx.guid = xml.guid
            mpx.hideBar = BooleanHelper.getBoolean(xml.isHideBar)
            mpx.hyperlink = xml.hyperlink
            mpx.hyperlinkAddress = xml.hyperlinkAddress
            mpx.hyperlinkSubAddress = xml.hyperlinkSubAddress

            mpx.ignoreResourceCalendar = BooleanHelper.getBoolean(xml.isIgnoreResourceCalendar)
            mpx.lateFinish = xml.lateFinish
            mpx.lateStart = xml.lateStart
            mpx.levelAssignments = BooleanHelper.getBoolean(xml.isLevelAssignments)
            mpx.levelingCanSplit = BooleanHelper.getBoolean(xml.isLevelingCanSplit)
            mpx.levelingDelayFormat = DatatypeConverter.parseDurationTimeUnits(xml.levelingDelayFormat)
            if (xml.levelingDelay != null && mpx.levelingDelayFormat != null) {
                val duration = xml.levelingDelay.doubleValue()
                if (duration != 0.0) {
                    mpx.levelingDelay = Duration.convertUnits(duration / 10, TimeUnit.MINUTES, mpx.levelingDelayFormat, m_projectFile!!.projectProperties)
                }
            }

            //mpx.setLinkedFields();
            //mpx.setMarked();
            mpx.milestone = BooleanHelper.getBoolean(xml.isMilestone)
            mpx.name = xml.name
            if (xml.notes != null && xml.notes.length() !== 0) {
                mpx.notes = xml.notes
            }
            //mpx.setNumber1();
            //mpx.setNumber2();
            //mpx.setNumber3();
            //mpx.setNumber4();
            //mpx.setNumber5();
            //mpx.setObjects();
            mpx.outlineLevel = NumberHelper.getInteger(xml.outlineLevel)
            mpx.outlineNumber = xml.outlineNumber
            mpx.overAllocated = BooleanHelper.getBoolean(xml.isOverAllocated)
            mpx.overtimeCost = DatatypeConverter.parseCurrency(xml.overtimeCost)
            mpx.overtimeWork = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.overtimeWork)
            mpx.percentageComplete = xml.percentComplete
            mpx.percentageWorkComplete = xml.percentWorkComplete
            mpx.physicalPercentComplete = NumberHelper.getInteger(xml.physicalPercentComplete)
            mpx.preleveledFinish = xml.preLeveledFinish
            mpx.preleveledStart = xml.preLeveledStart
            mpx.priority = DatatypeConverter.parsePriority(xml.priority)
            //mpx.setProject();
            mpx.recurring = BooleanHelper.getBoolean(xml.isRecurring)
            mpx.regularWork = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.regularWork)
            mpx.remainingCost = DatatypeConverter.parseCurrency(xml.remainingCost)
            mpx.remainingDuration = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.remainingDuration)
            mpx.remainingOvertimeCost = DatatypeConverter.parseCurrency(xml.remainingOvertimeCost)
            mpx.remainingOvertimeWork = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.remainingOvertimeWork)
            mpx.remainingWork = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.remainingWork)
            //mpx.setResourceGroup();
            //mpx.setResourceInitials();
            //mpx.setResourceNames();
            mpx.resume = xml.resume
            mpx.resumeValid = BooleanHelper.getBoolean(xml.isResumeValid)
            //mpx.setResumeNoEarlierThan();
            mpx.rollup = BooleanHelper.getBoolean(xml.isRollup)
            mpx.start = xml.start
            mpx.startText = xml.startText
            //mpx.setStart1();
            //mpx.setStart2();
            //mpx.setStart3();
            //mpx.setStart4();
            //mpx.setStart5();
            mpx.startVariance = DatatypeConverter.parseDurationInThousanthsOfMinutes(xml.startVariance)
            mpx.stop = xml.stop
            mpx.subProject = if (BooleanHelper.getBoolean(xml.isIsSubproject)) SubProject() else null
            mpx.subprojectName = xml.subprojectName
            mpx.subprojectReadOnly = BooleanHelper.getBoolean(xml.isIsSubprojectReadOnly)
            //mpx.setSuccessors();
            mpx.summary = BooleanHelper.getBoolean(xml.isSummary)
            //mpx.setSV();
            //mpx.setText1();
            //mpx.setText2();
            //mpx.setText3();
            //mpx.setText4();
            //mpx.setText5();
            //mpx.setText6();
            //mpx.setText7();
            //mpx.setText8();
            //mpx.setText9();
            //mpx.setText10();
            mpx.taskMode = if (BooleanHelper.getBoolean(xml.isManual)) TaskMode.MANUALLY_SCHEDULED else TaskMode.AUTO_SCHEDULED
            mpx.type = xml.type
            //mpx.setUpdateNeeded();
            mpx.wbs = xml.wbs
            mpx.wbsLevel = xml.wbsLevel
            mpx.work = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.work)
            mpx.workVariance = Duration.getInstance(NumberHelper.getDouble(xml.workVariance) / 1000, TimeUnit.MINUTES)

            validateFinishDate(mpx)

            // read last to ensure correct caching
            mpx.startSlack = DatatypeConverter.parseDurationInTenthsOfMinutes(xml.startSlack)
            mpx.finishSlack = DatatypeConverter.parseDurationInTenthsOfMinutes(xml.finishSlack)
            mpx.freeSlack = DatatypeConverter.parseDurationInTenthsOfMinutes(xml.freeSlack)
            mpx.totalSlack = DatatypeConverter.parseDurationInTenthsOfMinutes(xml.totalSlack)
            mpx.critical = BooleanHelper.getBoolean(xml.isCritical)

            readTaskExtendedAttributes(xml, mpx)

            readTaskBaselines(xml, mpx, durationFormat)

            if (mpx.taskMode === TaskMode.MANUALLY_SCHEDULED) {
                mpx.manualDuration = DatatypeConverter.parseDuration(m_projectFile, durationFormat, xml.manualDuration)
            }

            //
            // When reading an MSPDI file, the project summary task contains
            // some of the values used to populate the project properties.
            //
            if (NumberHelper.getInt(mpx.uniqueID) == 0) {
                updateProjectProperties(mpx)
            }
        }

        m_eventManager!!.fireTaskReadEvent(mpx)

        return mpx
    }

    /**
     * Update the project properties from the project summary task.
     *
     * @param task project summary task
     */
    private fun updateProjectProperties(task: Task) {
        val props = m_projectFile!!.projectProperties
        props.comments = task.notes
    }

    /**
     * When projectmanager.com exports schedules as MSPDI (via Aspose tasks)
     * they do not have finish dates, just a start date and a duration.
     * This method populates finish dates.
     *
     * @param task task to validate
     */
    private fun validateFinishDate(task: Task) {
        if (task.finish == null) {
            val startDate = task.start
            if (startDate != null) {
                if (task.milestone) {
                    task.finish = startDate
                } else {
                    val duration = task.duration
                    if (duration != null) {
                        val calendar = task.effectiveCalendar
                        task.finish = calendar.getDate(startDate, duration, false)
                    }
                }
            }
        }
    }

    /**
     * Reads baseline values for the current task.
     *
     * @param xmlTask MSPDI task instance
     * @param mpxjTask MPXJ task instance
     * @param durationFormat duration format to use
     */
    private fun readTaskBaselines(xmlTask: Project.Tasks.Task, mpxjTask: Task, durationFormat: TimeUnit) {
        for (baseline in xmlTask.baseline) {
            val number = NumberHelper.getInt(baseline.number)

            val cost = DatatypeConverter.parseCurrency(baseline.cost)
            val duration = DatatypeConverter.parseDuration(m_projectFile, durationFormat, baseline.duration)
            val finish = baseline.finish
            val start = baseline.start
            val work = DatatypeConverter.parseDuration(m_projectFile, TimeUnit.HOURS, baseline.work)

            if (number == 0) {
                mpxjTask.baselineCost = cost
                mpxjTask.baselineDuration = duration
                mpxjTask.baselineFinish = finish
                mpxjTask.baselineStart = start
                mpxjTask.baselineWork = work
            } else {
                mpxjTask.setBaselineCost(number, cost)
                mpxjTask.setBaselineDuration(number, duration)
                mpxjTask.setBaselineFinish(number, finish)
                mpxjTask.setBaselineStart(number, start)
                mpxjTask.setBaselineWork(number, work)
            }
        }
    }

    /**
     * This method processes any extended attributes associated with a task.
     *
     * @param xml MSPDI task instance
     * @param mpx MPX task instance
     */
    private fun readTaskExtendedAttributes(xml: Project.Tasks.Task, mpx: Task) {
        for (attrib in xml.extendedAttribute) {
            val xmlFieldID = Integer.parseInt(attrib.fieldID) and 0x0000FFFF
            val mpxFieldID = MPPTaskField.getInstance(xmlFieldID)
            val durationFormat = DatatypeConverter.parseDurationTimeUnits(attrib.durationFormat, null)
            DatatypeConverter.parseExtendedAttribute(m_projectFile, mpx, attrib.value, mpxFieldID, durationFormat)
        }
    }

    /**
     * This method is used to retrieve the calendar associated
     * with a task. If no calendar is associated with a task, this method
     * returns null.
     *
     * @param task MSPDI task
     * @return calendar instance
     */
    private fun getTaskCalendar(task: Project.Tasks.Task): ProjectCalendar? {
        var calendar: ProjectCalendar? = null

        val calendarID = task.calendarUID
        if (calendarID != null) {
            calendar = m_projectFile!!.getCalendarByUniqueID(Integer.valueOf(calendarID.intValue()))
        }

        return calendar
    }

    /**
     * This method extracts predecessor data from an MSPDI file.
     *
     * @param task Task data
     */
    private fun readPredecessors(task: Project.Tasks.Task) {
        val uid = task.uid
        if (uid != null) {
            val currTask = m_projectFile!!.getTaskByUniqueID(uid)
            if (currTask != null) {
                for (link in task.predecessorLink) {
                    readPredecessor(currTask, link)
                }
            }
        }
    }

    /**
     * This method extracts data for a single predecessor from an MSPDI file.
     *
     * @param currTask Current task object
     * @param link Predecessor data
     */
    private fun readPredecessor(currTask: Task, link: Project.Tasks.Task.PredecessorLink) {
        val uid = link.predecessorUID
        if (uid != null) {
            val prevTask = m_projectFile!!.getTaskByUniqueID(Integer.valueOf(uid.intValue()))
            if (prevTask != null) {
                val type: RelationType
                if (link.type != null) {
                    type = RelationType.getInstance(link.type.intValue())
                } else {
                    type = RelationType.FINISH_START
                }

                val lagUnits = DatatypeConverter.parseDurationTimeUnits(link.lagFormat)

                val lagDuration: Duration
                val lag = NumberHelper.getInt(link.linkLag)
                if (lag == 0) {
                    lagDuration = Duration.getInstance(0, lagUnits)
                } else {
                    if (lagUnits === TimeUnit.PERCENT || lagUnits === TimeUnit.ELAPSED_PERCENT) {
                        lagDuration = Duration.getInstance(lag, lagUnits)
                    } else {
                        lagDuration = Duration.convertUnits(lag / 10.0, TimeUnit.MINUTES, lagUnits, m_projectFile!!.projectProperties)
                    }
                }

                val relation = currTask.addPredecessor(prevTask, type, lagDuration)
                m_eventManager!!.fireRelationReadEvent(relation)
            }
        }
    }

    /**
     * This method extracts assignment data from an MSPDI file.
     *
     * @param project Root node of the MSPDI file
     */
    private fun readAssignments(project: Project) {
        val assignments = project.assignments
        if (assignments != null) {
            val splitFactory = SplitTaskFactory()
            val normaliser = MSPDITimephasedWorkNormaliser()
            for (assignment in assignments.assignment) {
                readAssignment(assignment, splitFactory, normaliser)
            }
        }
    }

    /**
     * This method extracts data for a single assignment from an MSPDI file.
     *
     * @param assignment Assignment data
     * @param splitFactory split task handling
     * @param normaliser timephased resource assignment normaliser
     */
    private fun readAssignment(assignment: Project.Assignments.Assignment, splitFactory: SplitTaskFactory, normaliser: TimephasedWorkNormaliser) {
        val taskUID = assignment.taskUID
        val resourceUID = assignment.resourceUID
        if (taskUID != null && resourceUID != null) {
            val task = m_projectFile!!.getTaskByUniqueID(Integer.valueOf(taskUID.intValue()))
            if (task != null) {
                val resource = m_projectFile!!.getResourceByUniqueID(Integer.valueOf(resourceUID.intValue()))
                var calendar: ProjectCalendar? = null
                if (resource != null) {
                    calendar = resource.resourceCalendar
                }

                if (calendar == null || task.ignoreResourceCalendar) {
                    calendar = task.effectiveCalendar
                }

                val timephasedComplete = readTimephasedAssignment(calendar, assignment, 2)
                val timephasedPlanned = readTimephasedAssignment(calendar, assignment, 1)
                var raw = true

                if (isSplit(calendar, timephasedComplete) || isSplit(calendar, timephasedPlanned)) {
                    task.splits = LinkedList<DateRange>()
                    normaliser.normalise(calendar, timephasedComplete)
                    normaliser.normalise(calendar, timephasedPlanned)
                    splitFactory.processSplitData(task, timephasedComplete, timephasedPlanned)
                    raw = false
                }

                val timephasedCompleteData = DefaultTimephasedWorkContainer(calendar, normaliser, timephasedComplete, raw)
                val timephasedPlannedData = DefaultTimephasedWorkContainer(calendar, normaliser, timephasedPlanned, raw)

                val mpx = task.addResourceAssignment(resource)

                mpx.actualCost = DatatypeConverter.parseCurrency(assignment.actualCost)
                mpx.actualFinish = assignment.actualFinish
                mpx.actualOvertimeCost = DatatypeConverter.parseCurrency(assignment.actualOvertimeCost)
                mpx.actualOvertimeWork = DatatypeConverter.parseDuration(m_projectFile, TimeUnit.HOURS, assignment.actualOvertimeWork)
                //assignment.getActualOvertimeWorkProtected()
                mpx.actualStart = assignment.actualStart
                mpx.actualWork = DatatypeConverter.parseDuration(m_projectFile, TimeUnit.HOURS, assignment.actualWork)
                //assignment.getActualWorkProtected()
                mpx.acwp = DatatypeConverter.parseCurrency(assignment.acwp)
                mpx.bcwp = DatatypeConverter.parseCurrency(assignment.bcwp)
                mpx.bcws = DatatypeConverter.parseCurrency(assignment.bcws)
                //assignment.getBookingType()
                mpx.budgetCost = DatatypeConverter.parseCurrency(assignment.budgetCost)
                mpx.budgetWork = DatatypeConverter.parseDuration(m_projectFile, TimeUnit.HOURS, assignment.budgetWork)
                mpx.cost = DatatypeConverter.parseCurrency(assignment.cost)
                mpx.costRateTableIndex = NumberHelper.getInt(assignment.costRateTable)
                mpx.createDate = assignment.creationDate
                mpx.cv = DatatypeConverter.parseCurrency(assignment.cv)
                mpx.delay = DatatypeConverter.parseDurationInTenthsOfMinutes(assignment.delay)
                mpx.finish = assignment.finish
                mpx.variableRateUnits = if (BooleanHelper.getBoolean(assignment.isHasFixedRateUnits)) null else DatatypeConverter.parseTimeUnit(assignment.rateScale)
                mpx.guid = assignment.guid
                mpx.hyperlink = assignment.hyperlink
                mpx.hyperlinkAddress = assignment.hyperlinkAddress
                mpx.hyperlinkSubAddress = assignment.hyperlinkSubAddress
                mpx.levelingDelay = DatatypeConverter.parseDurationInTenthsOfMinutes(m_projectFile!!.projectProperties, assignment.levelingDelay, DatatypeConverter.parseDurationTimeUnits(assignment.levelingDelayFormat))
                mpx.notes = assignment.notes
                mpx.overtimeCost = DatatypeConverter.parseCurrency(assignment.overtimeCost)
                mpx.overtimeWork = DatatypeConverter.parseDuration(m_projectFile, TimeUnit.HOURS, assignment.overtimeWork)
                mpx.percentageWorkComplete = assignment.percentWorkComplete
                //mpx.setPlannedCost();
                //mpx.setPlannedWork();
                mpx.regularWork = DatatypeConverter.parseDuration(m_projectFile, TimeUnit.HOURS, assignment.regularWork)
                mpx.remainingCost = DatatypeConverter.parseCurrency(assignment.remainingCost)
                mpx.remainingOvertimeCost = DatatypeConverter.parseCurrency(assignment.remainingOvertimeCost)
                mpx.remainingOvertimeWork = DatatypeConverter.parseDuration(m_projectFile, TimeUnit.HOURS, assignment.remainingOvertimeWork)
                mpx.remainingWork = DatatypeConverter.parseDuration(m_projectFile, TimeUnit.HOURS, assignment.remainingWork)
                mpx.resume = assignment.resume
                mpx.start = assignment.start
                mpx.stop = assignment.stop
                mpx.sv = DatatypeConverter.parseCurrency(assignment.sv)
                mpx.uniqueID = NumberHelper.getInteger(assignment.uid)
                mpx.units = DatatypeConverter.parseUnits(assignment.units)
                mpx.vac = DatatypeConverter.parseCurrency(assignment.vac)
                mpx.work = DatatypeConverter.parseDuration(m_projectFile, TimeUnit.HOURS, assignment.work)
                mpx.workContour = assignment.workContour

                mpx.setTimephasedActualWork(timephasedCompleteData)
                mpx.setTimephasedWork(timephasedPlannedData)

                readAssignmentExtendedAttributes(assignment, mpx)

                readAssignmentBaselines(assignment, mpx)

                // Read last to ensure caching works as expected
                mpx.costVariance = DatatypeConverter.parseCurrency(assignment.costVariance)
                mpx.workVariance = DatatypeConverter.parseDurationInThousanthsOfMinutes(m_projectFile!!.projectProperties, assignment.workVariance, TimeUnit.HOURS)
                mpx.startVariance = DatatypeConverter.parseDurationInTenthsOfMinutes(m_projectFile!!.projectProperties, assignment.startVariance, TimeUnit.DAYS)
                mpx.finishVariance = DatatypeConverter.parseDurationInTenthsOfMinutes(m_projectFile!!.projectProperties, assignment.finishVariance, TimeUnit.DAYS)

                m_eventManager!!.fireAssignmentReadEvent(mpx)
            }
        }
    }

    /**
     * Extracts assignment baseline data.
     *
     * @param assignment xml assignment
     * @param mpx mpxj assignment
     */
    private fun readAssignmentBaselines(assignment: Project.Assignments.Assignment, mpx: ResourceAssignment) {
        for (baseline in assignment.baseline) {
            val number = NumberHelper.getInt(baseline.number)

            //baseline.getBCWP()
            //baseline.getBCWS()
            val cost = DatatypeConverter.parseExtendedAttributeCurrency(baseline.cost)
            val finish = DatatypeConverter.parseExtendedAttributeDate(baseline.finish)
            //baseline.getNumber()
            val start = DatatypeConverter.parseExtendedAttributeDate(baseline.start)
            val work = DatatypeConverter.parseDuration(m_projectFile, TimeUnit.HOURS, baseline.work)

            if (number == 0) {
                mpx.baselineCost = cost
                mpx.baselineFinish = finish
                mpx.baselineStart = start
                mpx.baselineWork = work
            } else {
                mpx.setBaselineCost(number, cost)
                mpx.setBaselineWork(number, work)
                mpx.setBaselineStart(number, start)
                mpx.setBaselineFinish(number, finish)
            }
        }
    }

    /**
     * This method processes any extended attributes associated with a
     * resource assignment.
     *
     * @param xml MSPDI resource assignment instance
     * @param mpx MPX task instance
     */
    private fun readAssignmentExtendedAttributes(xml: Project.Assignments.Assignment, mpx: ResourceAssignment) {
        for (attrib in xml.extendedAttribute) {
            val xmlFieldID = Integer.parseInt(attrib.fieldID) and 0x0000FFFF
            val mpxFieldID = MPPAssignmentField.getInstance(xmlFieldID)
            val durationFormat = DatatypeConverter.parseDurationTimeUnits(attrib.durationFormat, null)
            DatatypeConverter.parseExtendedAttribute(m_projectFile, mpx, attrib.value, mpxFieldID, durationFormat)
        }
    }

    /**
     * Test to determine if this is a split task.
     *
     * @param calendar current calendar
     * @param list timephased resource assignment list
     * @return boolean flag
     */
    private fun isSplit(calendar: ProjectCalendar?, list: List<TimephasedWork>): Boolean {
        var result = false
        for (assignment in list) {
            if (calendar != null && assignment.getTotalAmount().getDuration() === 0) {
                val calendarWork = calendar.getWork(assignment.getStart(), assignment.getFinish(), TimeUnit.MINUTES)
                if (calendarWork.getDuration() !== 0) {
                    result = true
                    break
                }
            }
        }
        return result
    }

    /**
     * Reads timephased assignment data.
     *
     * @param calendar current calendar
     * @param assignment assignment data
     * @param type flag indicating if this is planned or complete work
     * @return list of timephased resource assignment instances
     */
    private fun readTimephasedAssignment(calendar: ProjectCalendar, assignment: Project.Assignments.Assignment, type: Int): LinkedList<TimephasedWork> {
        val result = LinkedList<TimephasedWork>()

        for (item in assignment.timephasedData) {
            if (NumberHelper.getInt(item.type) != type) {
                continue
            }

            val startDate = item.start
            val finishDate = item.finish

            // Exclude ranges which don't have a start and end date.
            // These seem to be generated by Synchro and have a zero duration.
            if (startDate == null && finishDate == null) {
                continue
            }

            var work = DatatypeConverter.parseDuration(m_projectFile, TimeUnit.MINUTES, item.value)
            if (work == null) {
                work = Duration.getInstance(0, TimeUnit.MINUTES)
            } else {
                work = Duration.getInstance(NumberHelper.round(work.getDuration(), 2.0), TimeUnit.MINUTES)
            }

            val tra = TimephasedWork()
            tra.setStart(startDate)
            tra.setFinish(finishDate)
            tra.setTotalAmount(work)

            result.add(tra)
        }

        return result
    }

    companion object {

        /**
         * The way calendars are stored in an MSPDI file means that there
         * can be forward references between the base calendar unique ID for a
         * derived calendar, and the base calendar itself. To get around this,
         * we initially populate the base calendar name attribute with the
         * base calendar unique ID, and now in this method we can convert those
         * ID values into the correct names.
         *
         * @param baseCalendars list of calendars and base calendar IDs
         * @param map map of calendar ID values and calendar objects
         */
        private fun updateBaseCalendarNames(baseCalendars: List<Pair<ProjectCalendar, BigInteger>>, map: HashMap<BigInteger, ProjectCalendar>) {
            for (pair in baseCalendars) {
                val cal = pair.first
                val baseCalendarID = pair.second
                val baseCal = map.get(baseCalendarID)
                if (baseCal != null) {
                    cal!!.parent = baseCal
                }
            }

        }

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
                CONTEXT = JAXBContext.newInstance("net.sf.mpxj.mspdi.schema", MSPDIReader::class.java!!.getClassLoader())
            } catch (ex: JAXBException) {
                CONTEXT_EXCEPTION = ex
                CONTEXT = null
            }

        }

        private val RECURRENCE_TYPES = arrayOf<RecurrenceType>(null, RecurrenceType.DAILY, RecurrenceType.YEARLY, // Absolute
                RecurrenceType.YEARLY, // Relative
                RecurrenceType.MONTHLY, // Absolute
                RecurrenceType.MONTHLY, // Relative
                RecurrenceType.WEEKLY, RecurrenceType.DAILY)

        private val RELATIVE_MAP = booleanArrayOf(false, false, false, true, false, true)

        private val DAY_MASKS = intArrayOf(0x00, 0x01, // Sunday
                0x02, // Monday
                0x04, // Tuesday
                0x08, // Wednesday
                0x10, // Thursday
                0x20, // Friday
                0x40)// Saturday
    }
}
