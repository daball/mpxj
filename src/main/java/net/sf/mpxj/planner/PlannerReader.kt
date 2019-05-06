/*
 * file:       PlannerReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       22 February 2007
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

package net.sf.mpxj.planner

import java.io.InputStream
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList

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

import net.sf.mpxj.ConstraintType
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.DayType
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
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
import net.sf.mpxj.ResourceType
import net.sf.mpxj.Task
import net.sf.mpxj.TaskType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.planner.schema.Allocation
import net.sf.mpxj.planner.schema.Allocations
import net.sf.mpxj.planner.schema.Calendars
import net.sf.mpxj.planner.schema.Constraint
import net.sf.mpxj.planner.schema.Days
import net.sf.mpxj.planner.schema.DefaultWeek
import net.sf.mpxj.planner.schema.Interval
import net.sf.mpxj.planner.schema.OverriddenDayType
import net.sf.mpxj.planner.schema.OverriddenDayTypes
import net.sf.mpxj.planner.schema.Predecessor
import net.sf.mpxj.planner.schema.Predecessors
import net.sf.mpxj.planner.schema.Project
import net.sf.mpxj.planner.schema.Resources
import net.sf.mpxj.planner.schema.Tasks
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * This class creates a new ProjectFile instance by reading a Planner file.
 */
class PlannerReader : AbstractProjectReader() {

    private var m_projectFile: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_defaultCalendar: ProjectCalendar? = null
    private val m_twoDigitFormat = DecimalFormat("00")
    private val m_fourDigitFormat = DecimalFormat("0000")
    private val m_defaultWorkingHours = LinkedList<DateRange>()
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
    override fun read(stream: InputStream): ProjectFile {
        try {
            m_projectFile = ProjectFile()
            m_eventManager = m_projectFile!!.eventManager

            val config = m_projectFile!!.projectConfig
            config.autoTaskUniqueID = false
            config.autoResourceUniqueID = false
            config.autoOutlineLevel = false
            config.autoOutlineNumber = false
            config.autoWBS = false

            m_projectFile!!.projectProperties.fileApplication = "Planner"
            m_projectFile!!.projectProperties.fileType = "XML"

            m_eventManager!!.addProjectListeners(m_projectListeners)

            val factory = SAXParserFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            factory.setNamespaceAware(true)
            val saxParser = factory.newSAXParser()
            val xmlReader = saxParser.getXMLReader()
            val doc = SAXSource(xmlReader, InputSource(stream))

            if (CONTEXT == null) {
                throw CONTEXT_EXCEPTION
            }

            val unmarshaller = CONTEXT!!.createUnmarshaller()

            val plannerProject = unmarshaller.unmarshal(doc) as Project

            readProjectProperties(plannerProject)
            readCalendars(plannerProject)
            readResources(plannerProject)
            readTasks(plannerProject)
            readAssignments(plannerProject)

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
            m_defaultCalendar = null
        }
    }

    /**
     * This method extracts project properties from a Planner file.
     *
     * @param project Root node of the Planner file
     */
    @Throws(MPXJException::class)
    private fun readProjectProperties(project: Project) {
        val properties = m_projectFile!!.projectProperties

        properties.company = project.company
        properties.manager = project.manager
        properties.name = project.name
        properties.startDate = getDateTime(project.projectStart)
    }

    /**
     * This method extracts calendar data from a Planner file.
     *
     * @param project Root node of the Planner file
     */
    @Throws(MPXJException::class)
    private fun readCalendars(project: Project) {
        val calendars = project.calendars
        if (calendars != null) {
            for (cal in calendars.calendar) {
                readCalendar(cal, null)
            }

            val defaultCalendarID = getInteger(project.calendar)
            m_defaultCalendar = m_projectFile!!.getCalendarByUniqueID(defaultCalendarID)
            if (m_defaultCalendar != null) {
                m_projectFile!!.projectProperties.defaultCalendarName = m_defaultCalendar!!.name
            }
        }
    }

    /**
     * This method extracts data for a single calendar from a Planner file.
     *
     * @param plannerCalendar Calendar data
     * @param parentMpxjCalendar parent of derived calendar
     */
    @Throws(MPXJException::class)
    private fun readCalendar(plannerCalendar: net.sf.mpxj.planner.schema.Calendar, parentMpxjCalendar: ProjectCalendar?) {
        //
        // Create a calendar instance
        //
        val mpxjCalendar = m_projectFile!!.addCalendar()

        //
        // Populate basic details
        //
        mpxjCalendar.uniqueID = getInteger(plannerCalendar.id)
        mpxjCalendar.name = plannerCalendar.name
        mpxjCalendar.parent = parentMpxjCalendar

        //
        // Set working and non working days
        //
        val dw = plannerCalendar.defaultWeek
        setWorkingDay(mpxjCalendar, Day.MONDAY, dw.mon)
        setWorkingDay(mpxjCalendar, Day.TUESDAY, dw.tue)
        setWorkingDay(mpxjCalendar, Day.WEDNESDAY, dw.wed)
        setWorkingDay(mpxjCalendar, Day.THURSDAY, dw.thu)
        setWorkingDay(mpxjCalendar, Day.FRIDAY, dw.fri)
        setWorkingDay(mpxjCalendar, Day.SATURDAY, dw.sat)
        setWorkingDay(mpxjCalendar, Day.SUNDAY, dw.sun)

        //
        // Set working hours
        //
        processWorkingHours(mpxjCalendar, plannerCalendar)

        //
        // Process exception days
        //
        processExceptionDays(mpxjCalendar, plannerCalendar)

        m_eventManager!!.fireCalendarReadEvent(mpxjCalendar)

        //
        // Process any derived calendars
        //
        val calendarList = plannerCalendar.calendar
        for (cal in calendarList) {
            readCalendar(cal, mpxjCalendar)
        }
    }

    /**
     * Set the working/non-working status of a weekday.
     *
     * @param mpxjCalendar MPXJ calendar
     * @param mpxjDay day of the week
     * @param plannerDay planner day type
     */
    private fun setWorkingDay(mpxjCalendar: ProjectCalendar, mpxjDay: Day, plannerDay: String?) {
        var dayType = DayType.DEFAULT

        if (plannerDay != null) {
            when (getInt(plannerDay)) {
                0 -> {
                    dayType = DayType.WORKING
                }

                1 -> {
                    dayType = DayType.NON_WORKING
                }
            }
        }

        mpxjCalendar.setWorkingDay(mpxjDay, dayType)
    }

    /**
     * Add the appropriate working hours to each working day.
     *
     * @param mpxjCalendar MPXJ calendar
     * @param plannerCalendar Planner calendar
     */
    @Throws(MPXJException::class)
    private fun processWorkingHours(mpxjCalendar: ProjectCalendar, plannerCalendar: net.sf.mpxj.planner.schema.Calendar) {
        val types = plannerCalendar.overriddenDayTypes
        if (types != null) {
            val typeList = types.overriddenDayType
            val iter = typeList.iterator()
            var odt: OverriddenDayType? = null
            while (iter.hasNext()) {
                odt = iter.next()
                if (getInt(odt!!.id) != 0) {
                    odt = null
                    continue
                }

                break
            }

            if (odt != null) {
                val intervalList = odt.interval
                var mondayHours: ProjectCalendarHours? = null
                var tuesdayHours: ProjectCalendarHours? = null
                var wednesdayHours: ProjectCalendarHours? = null
                var thursdayHours: ProjectCalendarHours? = null
                var fridayHours: ProjectCalendarHours? = null
                var saturdayHours: ProjectCalendarHours? = null
                var sundayHours: ProjectCalendarHours? = null

                if (mpxjCalendar.isWorkingDay(Day.MONDAY)) {
                    mondayHours = mpxjCalendar.addCalendarHours(Day.MONDAY)
                }

                if (mpxjCalendar.isWorkingDay(Day.TUESDAY)) {
                    tuesdayHours = mpxjCalendar.addCalendarHours(Day.TUESDAY)
                }

                if (mpxjCalendar.isWorkingDay(Day.WEDNESDAY)) {
                    wednesdayHours = mpxjCalendar.addCalendarHours(Day.WEDNESDAY)
                }

                if (mpxjCalendar.isWorkingDay(Day.THURSDAY)) {
                    thursdayHours = mpxjCalendar.addCalendarHours(Day.THURSDAY)
                }

                if (mpxjCalendar.isWorkingDay(Day.FRIDAY)) {
                    fridayHours = mpxjCalendar.addCalendarHours(Day.FRIDAY)
                }

                if (mpxjCalendar.isWorkingDay(Day.SATURDAY)) {
                    saturdayHours = mpxjCalendar.addCalendarHours(Day.SATURDAY)
                }

                if (mpxjCalendar.isWorkingDay(Day.SUNDAY)) {
                    sundayHours = mpxjCalendar.addCalendarHours(Day.SUNDAY)
                }

                for (interval in intervalList) {
                    val startTime = getTime(interval.start)
                    val endTime = getTime(interval.end)

                    m_defaultWorkingHours.add(DateRange(startTime, endTime))

                    mondayHours?.addRange(DateRange(startTime, endTime))

                    tuesdayHours?.addRange(DateRange(startTime, endTime))

                    wednesdayHours?.addRange(DateRange(startTime, endTime))

                    thursdayHours?.addRange(DateRange(startTime, endTime))

                    fridayHours?.addRange(DateRange(startTime, endTime))

                    saturdayHours?.addRange(DateRange(startTime, endTime))

                    sundayHours?.addRange(DateRange(startTime, endTime))
                }
            }
        }
    }

    /**
     * Process exception days.
     *
     * @param mpxjCalendar MPXJ calendar
     * @param plannerCalendar Planner calendar
     */
    @Throws(MPXJException::class)
    private fun processExceptionDays(mpxjCalendar: ProjectCalendar, plannerCalendar: net.sf.mpxj.planner.schema.Calendar) {
        val days = plannerCalendar.days
        if (days != null) {
            val dayList = days.day
            for (day in dayList) {
                if (day.type.equals("day-type")) {
                    val exceptionDate = getDate(day.date)
                    val exception = mpxjCalendar.addCalendarException(exceptionDate, exceptionDate)
                    if (getInt(day.id) == 0) {
                        for (hoursIndex in 0 until m_defaultWorkingHours.size()) {
                            val range = m_defaultWorkingHours.get(hoursIndex)
                            exception.addRange(range)
                        }
                    }
                }
            }
        }
    }

    /**
     * This method extracts resource data from a Planner file.
     *
     * @param plannerProject Root node of the Planner file
     */
    @Throws(MPXJException::class)
    private fun readResources(plannerProject: Project) {
        val resources = plannerProject.resources
        if (resources != null) {
            for (res in resources.resource) {
                readResource(res)
            }
        }
    }

    /**
     * This method extracts data for a single resource from a Planner file.
     *
     * @param plannerResource Resource data
     */
    @Throws(MPXJException::class)
    private fun readResource(plannerResource: net.sf.mpxj.planner.schema.Resource) {
        val mpxjResource = m_projectFile!!.addResource()

        //mpxjResource.setResourceCalendar(m_projectFile.getBaseCalendarByUniqueID(getInteger(plannerResource.getCalendar())));
        mpxjResource.emailAddress = plannerResource.email
        mpxjResource.uniqueID = getInteger(plannerResource.id)
        mpxjResource.name = plannerResource.name
        mpxjResource.notes = plannerResource.note
        mpxjResource.initials = plannerResource.shortName
        mpxjResource.type = if (getInt(plannerResource.type) == 2) ResourceType.MATERIAL else ResourceType.WORK
        //plannerResource.getStdRate();
        //plannerResource.getOvtRate();
        //plannerResource.getUnits();
        //plannerResource.getProperties();

        val calendar = mpxjResource.addResourceCalendar()

        calendar.setWorkingDay(Day.SUNDAY, DayType.DEFAULT)
        calendar.setWorkingDay(Day.MONDAY, DayType.DEFAULT)
        calendar.setWorkingDay(Day.TUESDAY, DayType.DEFAULT)
        calendar.setWorkingDay(Day.WEDNESDAY, DayType.DEFAULT)
        calendar.setWorkingDay(Day.THURSDAY, DayType.DEFAULT)
        calendar.setWorkingDay(Day.FRIDAY, DayType.DEFAULT)
        calendar.setWorkingDay(Day.SATURDAY, DayType.DEFAULT)

        var baseCalendar: ProjectCalendar? = m_projectFile!!.getCalendarByUniqueID(getInteger(plannerResource.calendar))
        if (baseCalendar == null) {
            baseCalendar = m_defaultCalendar
        }
        calendar.parent = baseCalendar

        m_eventManager!!.fireResourceReadEvent(mpxjResource)
    }

    /**
     * This method extracts task data from a Planner file.
     *
     * @param plannerProject Root node of the Planner file
     */
    @Throws(MPXJException::class)
    private fun readTasks(plannerProject: Project) {
        val tasks = plannerProject.tasks
        if (tasks != null) {
            for (task in tasks.task) {
                readTask(null, task)
            }

            for (task in tasks.task) {
                readPredecessors(task)
            }
        }

        m_projectFile!!.updateStructure()
    }

    /**
     * This method extracts data for a single task from a Planner file.
     *
     * @param parentTask parent task
     * @param plannerTask Task data
     */
    @Throws(MPXJException::class)
    private fun readTask(parentTask: Task?, plannerTask: net.sf.mpxj.planner.schema.Task) {
        val mpxjTask: Task

        if (parentTask == null) {
            mpxjTask = m_projectFile!!.addTask()
            mpxjTask.outlineLevel = Integer.valueOf(1)
        } else {
            mpxjTask = parentTask.addTask()
            mpxjTask.outlineLevel = Integer.valueOf(parentTask.outlineLevel.intValue() + 1)
        }

        //
        // Read task attributes from Planner
        //
        val percentComplete = getInteger(plannerTask.percentComplete)
        //plannerTask.getDuration(); calculate from end - start, not in file?
        //plannerTask.getEffort(); not set?
        mpxjTask.finish = getDateTime(plannerTask.end)
        mpxjTask.uniqueID = getInteger(plannerTask.id)
        mpxjTask.name = plannerTask.name
        mpxjTask.notes = plannerTask.note
        mpxjTask.percentageComplete = percentComplete
        mpxjTask.percentageWorkComplete = percentComplete
        mpxjTask.priority = Priority.getInstance(getInt(plannerTask.priority) / 10)
        mpxjTask.type = getTaskType(plannerTask.scheduling)
        //plannerTask.getStart(); // Start day, time is always 00:00?
        mpxjTask.milestone = plannerTask.type.equals("milestone")

        mpxjTask.work = getDuration(plannerTask.work)

        mpxjTask.start = getDateTime(plannerTask.workStart)

        //
        // Read constraint
        //
        var mpxjConstraintType = ConstraintType.AS_SOON_AS_POSSIBLE
        val constraint = plannerTask.constraint
        if (constraint != null) {
            if (constraint.type.equals("start-no-earlier-than")) {
                mpxjConstraintType = ConstraintType.START_NO_EARLIER_THAN
            } else {
                if (constraint.type.equals("must-start-on")) {
                    mpxjConstraintType = ConstraintType.MUST_START_ON
                }
            }

            mpxjTask.constraintDate = getDateTime(constraint.time)
        }
        mpxjTask.constraintType = mpxjConstraintType

        //
        // Calculate missing attributes
        //
        val calendar = m_projectFile!!.defaultCalendar
        if (calendar != null) {
            var duration = calendar.getWork(mpxjTask.start, mpxjTask.finish, TimeUnit.HOURS)
            val durationDays = duration.getDuration() / 8
            if (durationDays > 0) {
                duration = Duration.getInstance(durationDays, TimeUnit.DAYS)
            }
            mpxjTask.duration = duration

            if (percentComplete!!.intValue() !== 0) {
                mpxjTask.actualStart = mpxjTask.start

                if (percentComplete!!.intValue() === 100) {
                    mpxjTask.actualFinish = mpxjTask.finish
                    mpxjTask.actualDuration = duration
                    mpxjTask.actualWork = mpxjTask.work
                    mpxjTask.remainingWork = Duration.getInstance(0, TimeUnit.HOURS)
                } else {
                    val work = mpxjTask.work
                    val actualWork = Duration.getInstance(work!!.getDuration() * percentComplete!!.doubleValue() / 100.0, work.getUnits())

                    mpxjTask.actualDuration = Duration.getInstance(duration.getDuration() * percentComplete!!.doubleValue() / 100.0, duration.getUnits())
                    mpxjTask.actualWork = actualWork
                    mpxjTask.remainingWork = Duration.getInstance(work.getDuration() - actualWork.getDuration(), work.getUnits())
                }
            }
        }
        mpxjTask.effortDriven = true

        m_eventManager!!.fireTaskReadEvent(mpxjTask)

        //
        // Process child tasks
        //
        val childTasks = plannerTask.task
        for (childTask in childTasks) {
            readTask(mpxjTask, childTask)
        }
    }

    /**
     * This method extracts predecessor data from a Planner file.
     *
     * @param plannerTask Task data
     */
    private fun readPredecessors(plannerTask: net.sf.mpxj.planner.schema.Task) {
        val mpxjTask = m_projectFile!!.getTaskByUniqueID(getInteger(plannerTask.id))

        val predecessors = plannerTask.predecessors
        if (predecessors != null) {
            val predecessorList = predecessors.predecessor
            for (predecessor in predecessorList) {
                val predecessorID = getInteger(predecessor.predecessorId)
                val predecessorTask = m_projectFile!!.getTaskByUniqueID(predecessorID)
                if (predecessorTask != null) {
                    var lag = getDuration(predecessor.lag)
                    if (lag == null) {
                        lag = Duration.getInstance(0, TimeUnit.HOURS)
                    }
                    val relation = mpxjTask.addPredecessor(predecessorTask, RELATIONSHIP_TYPES.get(predecessor.getType()), lag)
                    m_eventManager!!.fireRelationReadEvent(relation)
                }
            }
        }

        //
        // Process child tasks
        //
        val childTasks = plannerTask.task
        for (childTask in childTasks) {
            readPredecessors(childTask)
        }
    }

    /**
     * This method extracts assignment data from a Planner file.
     *
     * @param plannerProject Root node of the Planner file
     */
    private fun readAssignments(plannerProject: Project) {
        val allocations = plannerProject.allocations
        val allocationList = allocations.allocation
        val tasksWithAssignments = HashSet<Task>()

        for (allocation in allocationList) {
            val taskID = getInteger(allocation.taskId)
            val resourceID = getInteger(allocation.resourceId)
            val units = getInteger(allocation.units)

            val task = m_projectFile!!.getTaskByUniqueID(taskID)
            val resource = m_projectFile!!.getResourceByUniqueID(resourceID)

            if (task != null && resource != null) {
                val work = task.work
                val percentComplete = NumberHelper.getInt(task.percentageComplete)

                val assignment = task.addResourceAssignment(resource)
                assignment.units = units
                assignment.work = work

                if (percentComplete != 0) {
                    val actualWork = Duration.getInstance(work!!.getDuration() * percentComplete / 100, work.getUnits())
                    assignment.actualWork = actualWork
                    assignment.remainingWork = Duration.getInstance(work.getDuration() - actualWork.getDuration(), work.getUnits())
                } else {
                    assignment.remainingWork = work
                }

                assignment.start = task.start
                assignment.finish = task.finish

                tasksWithAssignments.add(task)

                m_eventManager!!.fireAssignmentReadEvent(assignment)
            }
        }

        //
        // Adjust work per assignment for tasks with multiple assignments
        //
        for (task in tasksWithAssignments) {
            val assignments = task.resourceAssignments
            if (assignments.size() > 1) {
                var maxUnits = 0.0
                for (assignment in assignments) {
                    maxUnits += assignment.units.doubleValue()
                }

                for (assignment in assignments) {
                    var work = assignment.work
                    val factor = assignment.units.doubleValue() / maxUnits

                    work = Duration.getInstance(work!!.getDuration() * factor, work!!.getUnits())
                    assignment.work = work
                    var actualWork = assignment.actualWork
                    if (actualWork != null) {
                        actualWork = Duration.getInstance(actualWork!!.getDuration() * factor, actualWork!!.getUnits())
                        assignment.actualWork = actualWork
                    }

                    var remainingWork: Duration? = assignment.remainingWork
                    if (remainingWork != null) {
                        remainingWork = Duration.getInstance(remainingWork!!.getDuration() * factor, remainingWork!!.getUnits())
                        assignment.remainingWork = remainingWork
                    }
                }
            }
        }
    }

    /**
     * Convert a Planner date-time value into a Java date.
     *
     * 20070222T080000Z
     *
     * @param value Planner date-time
     * @return Java Date instance
     */
    @Throws(MPXJException::class)
    private fun getDateTime(value: String): Date {
        try {
            val year = m_fourDigitFormat.parse(value.substring(0, 4))
            val month = m_twoDigitFormat.parse(value.substring(4, 6))
            val day = m_twoDigitFormat.parse(value.substring(6, 8))

            val hours = m_twoDigitFormat.parse(value.substring(9, 11))
            val minutes = m_twoDigitFormat.parse(value.substring(11, 13))

            val cal = DateHelper.popCalendar()
            cal.set(Calendar.YEAR, year.intValue())
            cal.set(Calendar.MONTH, month.intValue() - 1)
            cal.set(Calendar.DAY_OF_MONTH, day.intValue())

            cal.set(Calendar.HOUR_OF_DAY, hours.intValue())
            cal.set(Calendar.MINUTE, minutes.intValue())

            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val result = cal.getTime()
            DateHelper.pushCalendar(cal)

            return result
        } catch (ex: ParseException) {
            throw MPXJException("Failed to parse date-time $value", ex)
        }

    }

    /**
     * Convert a Planner date into a Java date.
     *
     * 20070222
     *
     * @param value Planner date
     * @return Java Date instance
     */
    @Throws(MPXJException::class)
    private fun getDate(value: String): Date {
        try {
            val year = m_fourDigitFormat.parse(value.substring(0, 4))
            val month = m_twoDigitFormat.parse(value.substring(4, 6))
            val day = m_twoDigitFormat.parse(value.substring(6, 8))

            val cal = DateHelper.popCalendar()
            cal.set(Calendar.YEAR, year.intValue())
            cal.set(Calendar.MONTH, month.intValue() - 1)
            cal.set(Calendar.DAY_OF_MONTH, day.intValue())

            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val result = cal.getTime()
            DateHelper.pushCalendar(cal)

            return result
        } catch (ex: ParseException) {
            throw MPXJException("Failed to parse date $value", ex)
        }

    }

    /**
     * Convert a Planner time into a Java date.
     *
     * 0800
     *
     * @param value Planner time
     * @return Java Date instance
     */
    @Throws(MPXJException::class)
    private fun getTime(value: String): Date {
        try {
            val hours = m_twoDigitFormat.parse(value.substring(0, 2))
            val minutes = m_twoDigitFormat.parse(value.substring(2, 4))

            val cal = DateHelper.popCalendar()
            cal.set(Calendar.HOUR_OF_DAY, hours.intValue())
            cal.set(Calendar.MINUTE, minutes.intValue())
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val result = cal.getTime()
            DateHelper.pushCalendar(cal)

            return result
        } catch (ex: ParseException) {
            throw MPXJException("Failed to parse time $value", ex)
        }

    }

    /**
     * Convert a string into an Integer.
     *
     * @param value integer represented as a string
     * @return Integer instance
     */
    private fun getInteger(value: String): Integer? {
        return NumberHelper.getInteger(value)
    }

    /**
     * Convert a string into an int.
     *
     * @param value integer represented as a string
     * @return int value
     */
    private fun getInt(value: String): Int {
        return Integer.parseInt(value)
    }

    /**
     * Convert a string into a long.
     *
     * @param value long represented as a string
     * @return long value
     */
    private fun getLong(value: String): Long {
        return Long.parseLong(value)
    }

    /**
     * Convert a string representation of the task type
     * into a TaskType instance.
     *
     * @param value string value
     * @return TaskType value
     */
    private fun getTaskType(value: String?): TaskType {
        var result = TaskType.FIXED_UNITS
        if (value != null && value.equals("fixed-duration")) {
            result = TaskType.FIXED_DURATION
        }
        return result
    }

    /**
     * Converts the string representation of a Planner duration into
     * an MPXJ Duration instance.
     *
     * Planner represents durations as a number of seconds in its
     * file format, however it displays durations as days and hours,
     * and seems to assume that a working day is 8 hours.
     *
     * @param value string representation of a duration
     * @return Duration instance
     */
    private fun getDuration(value: String?): Duration? {
        var result: Duration? = null

        if (value != null && value.length() !== 0) {
            val seconds = getLong(value).toDouble()
            val hours = seconds / (60 * 60)
            val days = hours / 8

            if (days < 1) {
                result = Duration.getInstance(hours, TimeUnit.HOURS)
            } else {
                val durationDays = hours / 8
                result = Duration.getInstance(durationDays, TimeUnit.DAYS)
            }
        }

        return result
    }

    companion object {

        private val RELATIONSHIP_TYPES = HashMap<String, RelationType>()

        init {
            RELATIONSHIP_TYPES.put("FF", RelationType.FINISH_FINISH)
            RELATIONSHIP_TYPES.put("FS", RelationType.FINISH_START)
            RELATIONSHIP_TYPES.put("SF", RelationType.START_FINISH)
            RELATIONSHIP_TYPES.put("SS", RelationType.START_START)
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
                CONTEXT = JAXBContext.newInstance("net.sf.mpxj.planner.schema", PlannerReader::class.java!!.getClassLoader())
            } catch (ex: JAXBException) {
                CONTEXT_EXCEPTION = ex
                CONTEXT = null
            }

        }
    }
}
