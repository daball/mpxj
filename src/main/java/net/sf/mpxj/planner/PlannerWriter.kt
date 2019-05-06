/*
 * file:       PlannerWriter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Mar 16, 2007
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

import java.io.IOException
import java.io.OutputStream
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.HashMap

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller

import net.sf.mpxj.ConstraintType
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceType
import net.sf.mpxj.Task
import net.sf.mpxj.TaskType
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.planner.schema.Allocation
import net.sf.mpxj.planner.schema.Allocations
import net.sf.mpxj.planner.schema.Calendars
import net.sf.mpxj.planner.schema.Constraint
import net.sf.mpxj.planner.schema.DayType
import net.sf.mpxj.planner.schema.DayTypes
import net.sf.mpxj.planner.schema.Days
import net.sf.mpxj.planner.schema.DefaultWeek
import net.sf.mpxj.planner.schema.Interval
import net.sf.mpxj.planner.schema.ObjectFactory
import net.sf.mpxj.planner.schema.OverriddenDayType
import net.sf.mpxj.planner.schema.OverriddenDayTypes
import net.sf.mpxj.planner.schema.Predecessor
import net.sf.mpxj.planner.schema.Predecessors
import net.sf.mpxj.planner.schema.Project
import net.sf.mpxj.planner.schema.Resources
import net.sf.mpxj.planner.schema.Tasks
import net.sf.mpxj.writer.AbstractProjectWriter

/**
 * This class creates a new Planner file from the contents of
 * a ProjectFile instance.
 */
class PlannerWriter : AbstractProjectWriter() {

    /**
     * Retrieve the encoding used to write the file. If this value is null,
     * UTF-8 is used.
     *
     * @return encoding name
     */
    /**
     * Set the encoding used to write the file. By default UTF-8 is used.
     *
     * @param encoding encoding name
     */
    var encoding: String? = null
    private var m_projectFile: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_factory: ObjectFactory? = null
    private var m_plannerProject: Project? = null

    private val m_twoDigitFormat = DecimalFormat("00")
    private val m_fourDigitFormat = DecimalFormat("0000")
    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(IOException::class)
    override fun write(projectFile: ProjectFile, stream: OutputStream) {
        try {
            m_projectFile = projectFile
            m_eventManager = projectFile.eventManager

            if (CONTEXT == null) {
                throw CONTEXT_EXCEPTION
            }

            val marshaller = CONTEXT!!.createMarshaller()
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)
            if (encoding != null) {
                marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding)
            }

            //
            // The Planner implementation used  as the basis for this work, 0.14.1
            // does not appear to have a particularly robust parser, and rejects
            // files with the full XML declaration produced by JAXB. The
            // following property suppresses this declaration.
            //
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE)

            m_factory = ObjectFactory()
            m_plannerProject = m_factory!!.createProject()

            writeProjectProperties()
            writeCalendars()
            writeResources()
            writeTasks()
            writeAssignments()

            marshaller.marshal(m_plannerProject, stream)
        } catch (ex: JAXBException) {
            throw IOException(ex.toString())
        } finally {
            m_projectFile = null
            m_factory = null
            m_plannerProject = null
        }
    }

    /**
     * This method writes project properties to a Planner file.
     */
    private fun writeProjectProperties() {
        val properties = m_projectFile!!.projectProperties

        m_plannerProject!!.company = properties.company
        m_plannerProject!!.manager = properties.manager
        m_plannerProject!!.name = getString(properties.name)
        m_plannerProject!!.projectStart = getDateTime(properties.startDate)
        m_plannerProject!!.calendar = getIntegerString(m_projectFile!!.defaultCalendar!!.uniqueID)
        m_plannerProject!!.mrprojectVersion = "2"
    }

    /**
     * This method writes calendar data to a Planner file.
     *
     * @throws JAXBException on xml creation errors
     */
    @Throws(JAXBException::class)
    private fun writeCalendars() {
        //
        // Create the new Planner calendar list
        //
        val calendars = m_factory!!.createCalendars()
        m_plannerProject!!.calendars = calendars
        writeDayTypes(calendars)
        val calendar = calendars.calendar

        //
        // Process each calendar in turn
        //
        for (mpxjCalendar in m_projectFile!!.calendars) {
            val plannerCalendar = m_factory!!.createCalendar()
            calendar.add(plannerCalendar)
            writeCalendar(mpxjCalendar, plannerCalendar)
        }
    }

    /**
     * Write the standard set of day types.
     *
     * @param calendars parent collection of calendars
     */
    private fun writeDayTypes(calendars: Calendars) {
        val dayTypes = m_factory!!.createDayTypes()
        calendars.dayTypes = dayTypes
        val typeList = dayTypes.dayType

        var dayType = m_factory!!.createDayType()
        typeList.add(dayType)
        dayType.id = "0"
        dayType.name = "Working"
        dayType.description = "A default working day"

        dayType = m_factory!!.createDayType()
        typeList.add(dayType)
        dayType.id = "1"
        dayType.name = "Nonworking"
        dayType.description = "A default non working day"

        dayType = m_factory!!.createDayType()
        typeList.add(dayType)
        dayType.id = "2"
        dayType.name = "Use base"
        dayType.description = "Use day from base calendar"
    }

    /**
     * This method writes data for a single calendar to a Planner file.
     *
     * @param mpxjCalendar MPXJ calendar instance
     * @param plannerCalendar Planner calendar instance
     * @throws JAXBException on xml creation errors
     */
    @Throws(JAXBException::class)
    private fun writeCalendar(mpxjCalendar: ProjectCalendar, plannerCalendar: net.sf.mpxj.planner.schema.Calendar) {
        //
        // Populate basic details
        //
        plannerCalendar.id = getIntegerString(mpxjCalendar.uniqueID)
        plannerCalendar.name = getString(mpxjCalendar.name)

        //
        // Set working and non working days
        //
        val dw = m_factory!!.createDefaultWeek()
        plannerCalendar.defaultWeek = dw
        dw.mon = getWorkingDayString(mpxjCalendar, Day.MONDAY)
        dw.tue = getWorkingDayString(mpxjCalendar, Day.TUESDAY)
        dw.wed = getWorkingDayString(mpxjCalendar, Day.WEDNESDAY)
        dw.thu = getWorkingDayString(mpxjCalendar, Day.THURSDAY)
        dw.fri = getWorkingDayString(mpxjCalendar, Day.FRIDAY)
        dw.sat = getWorkingDayString(mpxjCalendar, Day.SATURDAY)
        dw.sun = getWorkingDayString(mpxjCalendar, Day.SUNDAY)

        //
        // Set working hours
        //
        val odt = m_factory!!.createOverriddenDayTypes()
        plannerCalendar.overriddenDayTypes = odt
        val typeList = odt.overriddenDayType
        val uniqueID = Sequence(0)

        //
        // This is a bit arbitrary, so not ideal, however...
        // The idea here is that MS Project allows us to specify working hours
        // for each day of the week individually. Planner doesn't do this,
        // but instead allows us to specify working hours for each day type.
        // What we are doing here is stepping through the days of the week to
        // find the first working day, then using the hours for that day
        // as the hours for the working day type in Planner.
        //
        for (dayLoop in 1..7) {
            val day = Day.getInstance(dayLoop)
            if (mpxjCalendar.isWorkingDay(day)) {
                processWorkingHours(mpxjCalendar, uniqueID, day, typeList)
                break
            }
        }

        //
        // Process exception days
        //
        val plannerDays = m_factory!!.createDays()
        plannerCalendar.days = plannerDays
        val dayList = plannerDays.day
        processExceptionDays(mpxjCalendar, dayList)

        m_eventManager!!.fireCalendarWrittenEvent(mpxjCalendar)

        //
        // Process any derived calendars
        //
        val calendarList = plannerCalendar.calendar

        for (mpxjDerivedCalendar in mpxjCalendar.derivedCalendars) {
            val plannerDerivedCalendar = m_factory!!.createCalendar()
            calendarList.add(plannerDerivedCalendar)
            writeCalendar(mpxjDerivedCalendar, plannerDerivedCalendar)
        }
    }

    /**
     * Process the standard working hours for a given day.
     *
     * @param mpxjCalendar MPXJ Calendar instance
     * @param uniqueID unique ID sequence generation
     * @param day Day instance
     * @param typeList Planner list of days
     */
    private fun processWorkingHours(mpxjCalendar: ProjectCalendar, uniqueID: Sequence, day: Day, typeList: List<OverriddenDayType>) {
        if (isWorkingDay(mpxjCalendar, day)) {
            val mpxjHours = mpxjCalendar.getCalendarHours(day)
            if (mpxjHours != null) {
                val odt = m_factory!!.createOverriddenDayType()
                typeList.add(odt)
                odt.id = getIntegerString(uniqueID.next())
                val intervalList = odt.interval
                for (mpxjRange in mpxjHours) {
                    val rangeStart = mpxjRange.getStart()
                    val rangeEnd = mpxjRange.getEnd()

                    if (rangeStart != null && rangeEnd != null) {
                        val interval = m_factory!!.createInterval()
                        intervalList.add(interval)
                        interval.start = getTimeString(rangeStart)
                        interval.end = getTimeString(rangeEnd)
                    }
                }
            }
        }
    }

    /**
     * Process exception days.
     *
     * @param mpxjCalendar MPXJ Calendar instance
     * @param dayList Planner list of exception days
     */
    private fun processExceptionDays(mpxjCalendar: ProjectCalendar, dayList: List<net.sf.mpxj.planner.schema.Day>) {
        for (mpxjCalendarException in mpxjCalendar.calendarExceptions) {
            val rangeStartDay = mpxjCalendarException.fromDate
            val rangeEndDay = mpxjCalendarException.toDate
            if (DateHelper.getDayStartDate(rangeStartDay)!!.getTime() === DateHelper.getDayEndDate(rangeEndDay)!!.getTime()) {
                //
                // Exception covers a single day
                //
                val day = m_factory!!.createDay()
                dayList.add(day)
                day.type = "day-type"
                day.date = getDateString(mpxjCalendarException.fromDate)
                day.id = if (mpxjCalendarException.working) "0" else "1"
            } else {
                //
                // Exception covers a range of days
                //
                val cal = DateHelper.popCalendar(rangeStartDay)

                while (cal.getTime().getTime() < rangeEndDay!!.getTime()) {
                    val day = m_factory!!.createDay()
                    dayList.add(day)
                    day.type = "day-type"
                    day.date = getDateString(cal.getTime())
                    day.id = if (mpxjCalendarException.working) "0" else "1"
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }

                DateHelper.pushCalendar(cal)
            }

            /**
             * @TODO we need to deal with date ranges here
             */
        }
    }

    /**
     * This method writes resource data to a Planner file.
     */
    private fun writeResources() {
        val resources = m_factory!!.createResources()
        m_plannerProject!!.resources = resources
        val resourceList = resources.resource
        for (mpxjResource in m_projectFile!!.resources) {
            val plannerResource = m_factory!!.createResource()
            resourceList.add(plannerResource)
            writeResource(mpxjResource, plannerResource)
        }
    }

    /**
     * This method writes data for a single resource to a Planner file.
     *
     * @param mpxjResource MPXJ Resource instance
     * @param plannerResource Planner Resource instance
     */
    private fun writeResource(mpxjResource: Resource, plannerResource: net.sf.mpxj.planner.schema.Resource) {
        val resourceCalendar = mpxjResource.resourceCalendar
        if (resourceCalendar != null) {
            plannerResource.calendar = getIntegerString(resourceCalendar.uniqueID)
        }

        plannerResource.email = mpxjResource.emailAddress
        plannerResource.id = getIntegerString(mpxjResource.uniqueID)
        plannerResource.name = getString(mpxjResource.name)
        plannerResource.note = mpxjResource.notes
        plannerResource.shortName = mpxjResource.initials
        plannerResource.type = if (mpxjResource.type == ResourceType.MATERIAL) "2" else "1"
        //plannerResource.setStdRate();
        //plannerResource.setOvtRate();
        plannerResource.units = "0"
        //plannerResource.setProperties();
        m_eventManager!!.fireResourceWrittenEvent(mpxjResource)
    }

    /**
     * This method writes task data to a Planner file.
     *
     * @throws JAXBException on xml creation errors
     */
    @Throws(JAXBException::class)
    private fun writeTasks() {
        val tasks = m_factory!!.createTasks()
        m_plannerProject!!.tasks = tasks
        val taskList = tasks.task
        for (task in m_projectFile!!.childTasks) {
            writeTask(task, taskList)
        }
    }

    /**
     * This method writes data for a single task to a Planner file.
     *
     * @param mpxjTask MPXJ Task instance
     * @param taskList list of child tasks for current parent
     */
    @Throws(JAXBException::class)
    private fun writeTask(mpxjTask: Task, taskList: List<net.sf.mpxj.planner.schema.Task>) {
        val plannerTask = m_factory!!.createTask()
        taskList.add(plannerTask)
        plannerTask.end = getDateTimeString(mpxjTask.finish)
        plannerTask.id = getIntegerString(mpxjTask.uniqueID)
        plannerTask.name = getString(mpxjTask.name)
        plannerTask.note = mpxjTask.notes
        plannerTask.percentComplete = getIntegerString(mpxjTask.percentageWorkComplete)
        plannerTask.priority = if (mpxjTask.priority == null) null else getIntegerString(mpxjTask.priority!!.value * 10)
        plannerTask.setScheduling(getScheduling(mpxjTask.type))
        plannerTask.start = getDateTimeString(DateHelper.getDayStartDate(mpxjTask.start))
        if (mpxjTask.milestone) {
            plannerTask.setType("milestone")
        } else {
            plannerTask.setType("normal")
        }
        plannerTask.work = getDurationString(mpxjTask.work)
        plannerTask.workStart = getDateTimeString(mpxjTask.start)

        val mpxjConstraintType = mpxjTask.constraintType
        if (mpxjConstraintType !== ConstraintType.AS_SOON_AS_POSSIBLE) {
            val plannerConstraint = m_factory!!.createConstraint()
            plannerTask.constraint = plannerConstraint
            if (mpxjConstraintType === ConstraintType.START_NO_EARLIER_THAN) {
                plannerConstraint.type = "start-no-earlier-than"
            } else {
                if (mpxjConstraintType === ConstraintType.MUST_START_ON) {
                    plannerConstraint.type = "must-start-on"
                }
            }

            plannerConstraint.time = getDateTimeString(mpxjTask.constraintDate)
        }

        //
        // Write predecessors
        //
        writePredecessors(mpxjTask, plannerTask)

        m_eventManager!!.fireTaskWrittenEvent(mpxjTask)

        //
        // Write child tasks
        //
        val childTaskList = plannerTask.task
        for (task in mpxjTask.childTasks) {
            writeTask(task, childTaskList)
        }
    }

    /**
     * This method writes predecessor data to a Planner file.
     * We have to deal with a slight anomaly in this method that is introduced
     * by the MPX file format. It would be possible for someone to create an
     * MPX file with both the predecessor list and the unique ID predecessor
     * list populated... which means that we must process both and avoid adding
     * duplicate predecessors. Also interesting to note is that MSP98 populates
     * the predecessor list, not the unique ID predecessor list, as you might
     * expect.
     *
     * @param mpxjTask MPXJ task instance
     * @param plannerTask planner task instance
     */
    private fun writePredecessors(mpxjTask: Task, plannerTask: net.sf.mpxj.planner.schema.Task) {
        val plannerPredecessors = m_factory!!.createPredecessors()
        plannerTask.predecessors = plannerPredecessors
        val predecessorList = plannerPredecessors.predecessor
        var id = 0

        val predecessors = mpxjTask.predecessors
        for (rel in predecessors) {
            val taskUniqueID = rel.targetTask.uniqueID
            val plannerPredecessor = m_factory!!.createPredecessor()
            plannerPredecessor.id = getIntegerString(++id)
            plannerPredecessor.predecessorId = getIntegerString(taskUniqueID)
            plannerPredecessor.lag = getDurationString(rel.lag)
            plannerPredecessor.type = RELATIONSHIP_TYPES.get(rel.type)
            predecessorList.add(plannerPredecessor)
            m_eventManager!!.fireRelationWrittenEvent(rel)
        }
    }

    /**
     * This method writes assignment data to a Planner file.
     *
     */
    private fun writeAssignments() {
        val allocations = m_factory!!.createAllocations()
        m_plannerProject!!.allocations = allocations

        val allocationList = allocations.allocation
        for (mpxjAssignment in m_projectFile!!.resourceAssignments) {
            val plannerAllocation = m_factory!!.createAllocation()
            allocationList.add(plannerAllocation)

            plannerAllocation.taskId = getIntegerString(mpxjAssignment.task!!.uniqueID)
            plannerAllocation.resourceId = getIntegerString(mpxjAssignment.resourceUniqueID)
            plannerAllocation.units = getIntegerString(mpxjAssignment.units)

            m_eventManager!!.fireAssignmentWrittenEvent(mpxjAssignment)
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
    private fun getDateTime(value: Date?): String {
        val result = StringBuilder(16)

        if (value != null) {
            val cal = DateHelper.popCalendar(value)
            result.append(m_fourDigitFormat.format(cal.get(Calendar.YEAR)))
            result.append(m_twoDigitFormat.format(cal.get(Calendar.MONTH) + 1))
            result.append(m_twoDigitFormat.format(cal.get(Calendar.DAY_OF_MONTH)))
            result.append("T")
            result.append(m_twoDigitFormat.format(cal.get(Calendar.HOUR_OF_DAY)))
            result.append(m_twoDigitFormat.format(cal.get(Calendar.MINUTE)))
            result.append(m_twoDigitFormat.format(cal.get(Calendar.SECOND)))
            result.append("Z")
            DateHelper.pushCalendar(cal)
        }

        return result.toString()
    }

    /**
     * Convert an Integer value into a String.
     *
     * @param value Integer value
     * @return String value
     */
    private fun getIntegerString(value: Number?): String? {
        return if (value == null) null else Integer.toString(value.intValue())
    }

    /**
     * Convert an int value into a String.
     *
     * @param value int value
     * @return String value
     */
    private fun getIntegerString(value: Int): String {
        return Integer.toString(value)
    }

    /**
     * Used to determine if a particular day of the week is normally
     * a working day.
     *
     * @param mpxjCalendar ProjectCalendar instance
     * @param day Day instance
     * @return boolean flag
     */
    private fun isWorkingDay(mpxjCalendar: ProjectCalendar, day: Day): Boolean {
        var result = false
        var type: net.sf.mpxj.DayType? = mpxjCalendar.getWorkingDay(day)
        if (type == null) {
            type = net.sf.mpxj.DayType.DEFAULT
        }

        when (type) {
            WORKING -> {
                result = true
            }

            NON_WORKING -> {
                result = false
            }

            DEFAULT -> {
                if (mpxjCalendar.parent == null) {
                    result = false
                } else {
                    result = isWorkingDay(mpxjCalendar.parent!!, day)
                }
            }
        }

        return result
    }

    /**
     * Returns a flag represented as a String, indicating if
     * the supplied day is a working day.
     *
     * @param mpxjCalendar MPXJ ProjectCalendar instance
     * @param day Day instance
     * @return boolean flag as a string
     */
    private fun getWorkingDayString(mpxjCalendar: ProjectCalendar, day: Day): String? {
        var result: String? = null
        var type: net.sf.mpxj.DayType? = mpxjCalendar.getWorkingDay(day)
        if (type == null) {
            type = net.sf.mpxj.DayType.DEFAULT
        }

        when (type) {
            WORKING -> {
                result = "0"
            }

            NON_WORKING -> {
                result = "1"
            }

            DEFAULT -> {
                result = "2"
            }
        }

        return result
    }

    /**
     * Convert a Java date into a Planner time.
     *
     * 0800
     *
     * @param value Java Date instance
     * @return Planner time value
     */
    private fun getTimeString(value: Date): String {
        val cal = DateHelper.popCalendar(value)
        val hours = cal.get(Calendar.HOUR_OF_DAY)
        val minutes = cal.get(Calendar.MINUTE)
        DateHelper.pushCalendar(cal)

        val sb = StringBuilder(4)
        sb.append(m_twoDigitFormat.format(hours))
        sb.append(m_twoDigitFormat.format(minutes))

        return sb.toString()
    }

    /**
     * Convert a Java date into a Planner date.
     *
     * 20070222
     *
     * @param value Java Date instance
     * @return Planner date
     */
    private fun getDateString(value: Date?): String {
        val cal = DateHelper.popCalendar(value)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        DateHelper.pushCalendar(cal)

        val sb = StringBuilder(8)
        sb.append(m_fourDigitFormat.format(year))
        sb.append(m_twoDigitFormat.format(month))
        sb.append(m_twoDigitFormat.format(day))

        return sb.toString()
    }

    /**
     * Convert a Java date into a Planner date-time string.
     *
     * 20070222T080000Z
     *
     * @param value Java date
     * @return Planner date-time string
     */
    private fun getDateTimeString(value: Date?): String? {
        var result: String? = null
        if (value != null) {
            val cal = DateHelper.popCalendar(value)
            val sb = StringBuilder(16)
            sb.append(m_fourDigitFormat.format(cal.get(Calendar.YEAR)))
            sb.append(m_twoDigitFormat.format(cal.get(Calendar.MONTH) + 1))
            sb.append(m_twoDigitFormat.format(cal.get(Calendar.DAY_OF_MONTH)))
            sb.append('T')
            sb.append(m_twoDigitFormat.format(cal.get(Calendar.HOUR_OF_DAY)))
            sb.append(m_twoDigitFormat.format(cal.get(Calendar.MINUTE)))
            sb.append(m_twoDigitFormat.format(cal.get(Calendar.SECOND)))
            sb.append('Z')
            result = sb.toString()
            DateHelper.pushCalendar(cal)
        }
        return result
    }

    /**
     * Converts an MPXJ Duration instance into the string representation
     * of a Planner duration.
     *
     * Planner represents durations as a number of seconds in its
     * file format, however it displays durations as days and hours,
     * and seems to assume that a working day is 8 hours.
     *
     * @param value string representation of a duration
     * @return Duration instance
     */
    private fun getDurationString(value: Duration?): String? {
        var result: String? = null

        if (value != null) {
            var seconds = 0.0

            when (value!!.getUnits()) {
                MINUTES, ELAPSED_MINUTES -> {
                    seconds = value!!.getDuration() * 60
                }

                HOURS, ELAPSED_HOURS -> {
                    seconds = value!!.getDuration() * (60 * 60)
                }

                DAYS -> {
                    val minutesPerDay = m_projectFile!!.projectProperties.minutesPerDay.doubleValue()
                    seconds = value!!.getDuration() * (minutesPerDay * 60)
                }

                ELAPSED_DAYS -> {
                    seconds = value!!.getDuration() * (24 * 60 * 60)
                }

                WEEKS -> {
                    val minutesPerWeek = m_projectFile!!.projectProperties.minutesPerWeek.doubleValue()
                    seconds = value!!.getDuration() * (minutesPerWeek * 60)
                }

                ELAPSED_WEEKS -> {
                    seconds = value!!.getDuration() * (7 * 24 * 60 * 60)
                }

                MONTHS -> {
                    val minutesPerDay = m_projectFile!!.projectProperties.minutesPerDay.doubleValue()
                    val daysPerMonth = m_projectFile!!.projectProperties.daysPerMonth.doubleValue()
                    seconds = value!!.getDuration() * (daysPerMonth * minutesPerDay * 60.0)
                }

                ELAPSED_MONTHS -> {
                    seconds = value!!.getDuration() * (30 * 24 * 60 * 60)
                }

                YEARS -> {
                    val minutesPerDay = m_projectFile!!.projectProperties.minutesPerDay.doubleValue()
                    val daysPerMonth = m_projectFile!!.projectProperties.daysPerMonth.doubleValue()
                    seconds = value!!.getDuration() * (12.0 * daysPerMonth * minutesPerDay * 60.0)
                }

                ELAPSED_YEARS -> {
                    seconds = value!!.getDuration() * (365 * 24 * 60 * 60)
                }

                else -> {
                }
            }

            result = Long.toString(seconds.toLong())
        }

        return result
    }

    /**
     * Convert a string representation of the task type
     * into a TaskType instance.
     *
     * @param value string value
     * @return TaskType value
     */
    private fun getScheduling(value: TaskType?): String {
        var result = "fixed-work"
        if (value != null && value === TaskType.FIXED_DURATION) {
            result = "fixed-duration"
        }
        return result
    }

    /**
     * Writes a string value, ensuring that null is mapped to an empty string.
     *
     * @param value string value
     * @return string value
     */
    private fun getString(value: String?): String {
        return value ?: ""
    }

    companion object {

        private val RELATIONSHIP_TYPES = HashMap<RelationType, String>()

        init {
            RELATIONSHIP_TYPES.put(RelationType.FINISH_FINISH, "FF")
            RELATIONSHIP_TYPES.put(RelationType.FINISH_START, "FS")
            RELATIONSHIP_TYPES.put(RelationType.START_FINISH, "SF")
            RELATIONSHIP_TYPES.put(RelationType.START_START, "SS")
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
                CONTEXT = JAXBContext.newInstance("net.sf.mpxj.planner.schema", PlannerWriter::class.java!!.getClassLoader())
            } catch (ex: JAXBException) {
                CONTEXT_EXCEPTION = ex
                CONTEXT = null
            }

        }
    }
}
