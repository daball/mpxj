/*
 * file:       SynchroReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       2018-10-11
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

package net.sf.mpxj.synchro

import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.HashMap
import java.util.LinkedList
import java.util.UUID

import net.sf.mpxj.ChildTaskContainer
import net.sf.mpxj.ConstraintType
import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.EventManager
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarDateRanges
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Resource
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * Reads Synchro SP files.
 */
class SynchroReader : AbstractProjectReader() {

    private var m_data: SynchroData? = null
    private var m_project: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_projectListeners: List<ProjectListener>? = null
    private var m_calendarMap: Map<UUID, ProjectCalendar>? = null
    private var m_taskMap: Map<UUID, Task>? = null
    private var m_predecessorMap: Map<Task, List<MapRow>>? = null
    private var m_resourceMap: Map<UUID, Resource>? = null
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
    override fun read(inputStream: InputStream): ProjectFile {
        try {
            //SynchroLogger.setLogFile("c:/temp/project1.txt");
            SynchroLogger.openLogFile()

            m_calendarMap = HashMap<UUID, ProjectCalendar>()
            m_taskMap = HashMap<UUID, Task>()
            m_predecessorMap = HashMap<Task, List<MapRow>>()
            m_resourceMap = HashMap<UUID, Resource>()

            m_data = SynchroData()
            m_data!!.process(inputStream)
            return read()
        } catch (ex: Exception) {
            throw MPXJException(MPXJException.INVALID_FILE, ex)
        } finally {
            SynchroLogger.closeLogFile()

            m_data = null
            m_calendarMap = null
            m_taskMap = null
            m_predecessorMap = null
            m_resourceMap = null
        }
    }

    /**
     * Reads data from the SP file.
     *
     * @return Project File instance
     */
    @Throws(Exception::class)
    private fun read(): ProjectFile {
        m_project = ProjectFile()
        m_eventManager = m_project!!.eventManager

        m_project!!.projectProperties.fileApplication = "Synchro"
        m_project!!.projectProperties.fileType = "SP"

        val fields = m_project!!.customFields
        fields.getCustomField(TaskField.TEXT1).setAlias("Code")

        m_eventManager!!.addProjectListeners(m_projectListeners)

        processCalendars()
        processResources()
        processTasks()
        processPredecessors()

        return m_project
    }

    /**
     * Extract calendar data.
     */
    @Throws(IOException::class)
    private fun processCalendars() {
        val reader = CalendarReader(m_data!!.getTableData("Calendars"))
        reader.read()

        for (row in reader.rows) {
            processCalendar(row)
        }

        m_project!!.defaultCalendar = m_calendarMap!![reader.defaultCalendarUUID]
    }

    /**
     * Extract data for a single calendar.
     *
     * @param row calendar data
     */
    private fun processCalendar(row: MapRow) {
        val calendar = m_project!!.addCalendar()

        val dayTypeMap = processDayTypes(row.getRows("DAY_TYPES"))

        calendar.name = row.getString("NAME")

        processRanges(dayTypeMap[row.getUUID("SUNDAY_DAY_TYPE")], calendar.addCalendarHours(Day.SUNDAY))
        processRanges(dayTypeMap[row.getUUID("MONDAY_DAY_TYPE")], calendar.addCalendarHours(Day.MONDAY))
        processRanges(dayTypeMap[row.getUUID("TUESDAY_DAY_TYPE")], calendar.addCalendarHours(Day.TUESDAY))
        processRanges(dayTypeMap[row.getUUID("WEDNESDAY_DAY_TYPE")], calendar.addCalendarHours(Day.WEDNESDAY))
        processRanges(dayTypeMap[row.getUUID("THURSDAY_DAY_TYPE")], calendar.addCalendarHours(Day.THURSDAY))
        processRanges(dayTypeMap[row.getUUID("FRIDAY_DAY_TYPE")], calendar.addCalendarHours(Day.FRIDAY))
        processRanges(dayTypeMap[row.getUUID("SATURDAY_DAY_TYPE")], calendar.addCalendarHours(Day.SATURDAY))

        for (assignment in row.getRows("DAY_TYPE_ASSIGNMENTS")) {
            val date = assignment.getDate("DATE")
            processRanges(dayTypeMap[assignment.getUUID("DAY_TYPE_UUID")], calendar.addCalendarException(date, date))
        }

        m_calendarMap!!.put(row.getUUID("UUID"), calendar)
    }

    /**
     * Populate time ranges.
     *
     * @param ranges time ranges from a Synchro table
     * @param container time range container
     */
    private fun processRanges(ranges: List<DateRange>?, container: ProjectCalendarDateRanges) {
        if (ranges != null) {
            for (range in ranges) {
                container.addRange(range)
            }
        }
    }

    /**
     * Extract day type definitions.
     *
     * @param types Synchro day type rows
     * @return Map of day types by UUID
     */
    private fun processDayTypes(types: List<MapRow>): Map<UUID, List<DateRange>> {
        val map = HashMap<UUID, List<DateRange>>()
        for (row in types) {
            val ranges = ArrayList<DateRange>()
            for (range in row.getRows("TIME_RANGES")) {
                ranges.add(DateRange(range.getDate("START"), range.getDate("END")))
            }
            map.put(row.getUUID("UUID"), ranges)
        }

        return map
    }

    /**
     * Extract resource data.
     */
    @Throws(IOException::class)
    private fun processResources() {
        val reader = CompanyReader(m_data!!.getTableData("Companies"))
        reader.read()
        for (companyRow in reader.rows) {
            // TODO: need to sort by type as well as by name!
            for (resourceRow in sort(companyRow.getRows("RESOURCES"), "NAME")) {
                processResource(resourceRow)
            }
        }
    }

    /**
     * Extract data for a single resource.
     *
     * @param row Synchro resource data
     */
    @Throws(IOException::class)
    private fun processResource(row: MapRow) {
        val resource = m_project!!.addResource()
        resource.name = row.getString("NAME")
        resource.guid = row.getUUID("UUID")
        resource.emailAddress = row.getString("EMAIL")
        resource.hyperlink = row.getString("URL")
        resource.notes = getNotes(row.getRows("COMMENTARY"))
        resource.setText(1, row.getString("DESCRIPTION"))
        resource.setText(2, row.getString("SUPPLY_REFERENCE"))
        resource.active = true

        val resources = row.getRows("RESOURCES")
        if (resources != null) {
            for (childResource in sort(resources, "NAME")) {
                processResource(childResource)
            }
        }

        m_resourceMap!!.put(resource.guid, resource)
    }

    /**
     * Extract task data.
     */
    @Throws(IOException::class)
    private fun processTasks() {
        val reader = TaskReader(m_data!!.getTableData("Tasks"))
        reader.read()
        for (row in reader.rows) {
            processTask(m_project!!, row)
        }
        updateDates()
    }

    /**
     * Extract data for a single task.
     *
     * @param parent task parent
     * @param row Synchro task data
     */
    @Throws(IOException::class)
    private fun processTask(parent: ChildTaskContainer, row: MapRow) {
        val task = parent.addTask()
        task.name = row.getString("NAME")
        task.guid = row.getUUID("UUID")
        task.setText(1, row.getString("ID"))
        task.duration = row.getDuration("PLANNED_DURATION")
        task.remainingDuration = row.getDuration("REMAINING_DURATION")
        task.hyperlink = row.getString("URL")
        task.percentageComplete = row.getDouble("PERCENT_COMPLETE")
        task.notes = getNotes(row.getRows("COMMENTARY"))
        task.milestone = task.duration!!.getDuration() === 0

        val calendar = m_calendarMap!![row.getUUID("CALENDAR_UUID")]
        if (calendar != m_project!!.defaultCalendar) {
            task.calendar = calendar
        }

        when (row.getInteger("STATUS").intValue()) {
            1 // Planned
            -> {
                task.start = row.getDate("PLANNED_START")
                task.finish = task.effectiveCalendar.getDate(task.start, task.duration!!, false)
            }

            2 // Started
            -> {
                task.actualStart = row.getDate("ACTUAL_START")
                task.start = task.actualStart
                task.finish = row.getDate("ESTIMATED_FINISH")
                if (task.finish == null) {
                    task.finish = row.getDate("PLANNED_FINISH")
                }
            }

            3 // Finished
            -> {
                task.actualStart = row.getDate("ACTUAL_START")
                task.actualFinish = row.getDate("ACTUAL_FINISH")
                task.percentageComplete = Double.valueOf(100.0)
                task.start = task.actualStart
                task.finish = task.actualFinish
            }
        }

        setConstraints(task, row)

        processChildTasks(task, row)

        m_taskMap!!.put(task.guid, task)

        val predecessors = row.getRows("PREDECESSORS")
        if (predecessors != null && !predecessors.isEmpty()) {
            m_predecessorMap!!.put(task, predecessors)
        }

        val resourceAssignmnets = row.getRows("RESOURCE_ASSIGNMENTS")
        if (resourceAssignmnets != null && !resourceAssignmnets.isEmpty()) {
            processResourceAssignments(task, resourceAssignmnets)
        }
    }

    /**
     * Extract child task data.
     *
     * @param task MPXJ task
     * @param row Synchro task data
     */
    @Throws(IOException::class)
    private fun processChildTasks(task: Task, row: MapRow) {
        val tasks = row.getRows("TASKS")
        if (tasks != null) {
            for (childTask in tasks) {
                processTask(task, childTask)
            }
        }
    }

    /**
     * Extract predecessor data.
     */
    private fun processPredecessors() {
        for (entry in m_predecessorMap!!.entrySet()) {
            val task = entry.getKey()
            val predecessors = entry.getValue()
            for (predecessor in predecessors) {
                processPredecessor(task, predecessor)
            }
        }
    }

    /**
     * Extract data for a single predecessor.
     *
     * @param task parent task
     * @param row Synchro predecessor data
     */
    private fun processPredecessor(task: Task, row: MapRow) {
        val predecessor = m_taskMap!![row.getUUID("PREDECESSOR_UUID")]
        if (predecessor != null) {
            task.addPredecessor(predecessor, row.getRelationType("RELATION_TYPE"), row.getDuration("LAG"))
        }
    }

    /**
     * Extract resource assignments for a task.
     *
     * @param task parent task
     * @param assignments list of Synchro resource assignment data
     */
    private fun processResourceAssignments(task: Task, assignments: List<MapRow>) {
        for (row in assignments) {
            processResourceAssignment(task, row)
        }
    }

    /**
     * Extract data for a single resource assignment.
     *
     * @param task parent task
     * @param row Synchro resource assignment
     */
    private fun processResourceAssignment(task: Task, row: MapRow) {
        val resource = m_resourceMap!![row.getUUID("RESOURCE_UUID")]
        task.addResourceAssignment(resource)
    }

    /**
     * Map Synchro constraints to MPXJ constraints.
     *
     * @param task task
     * @param row Synchro constraint data
     */
    private fun setConstraints(task: Task, row: MapRow) {
        var constraintType: ConstraintType? = null
        var constraintDate: Date? = null
        val lateDate = row.getDate("CONSTRAINT_LATE_DATE")
        val earlyDate = row.getDate("CONSTRAINT_EARLY_DATE")

        when (row.getInteger("CONSTRAINT_TYPE").intValue()) {
            2 // Cannot Reschedule
            -> {
                constraintType = ConstraintType.MUST_START_ON
                constraintDate = task.start
            }

            12 //Finish Between
            -> {
                constraintType = ConstraintType.MUST_FINISH_ON
                constraintDate = lateDate
            }

            10 // Finish On or After
            -> {
                constraintType = ConstraintType.FINISH_NO_EARLIER_THAN
                constraintDate = earlyDate
            }

            11 // Finish On or Before
            -> {
                constraintType = ConstraintType.FINISH_NO_LATER_THAN
                constraintDate = lateDate
            }

            13 // Mandatory Start
                , 5 // Start On
                , 9 // Finish On
            -> {
                constraintType = ConstraintType.MUST_START_ON
                constraintDate = earlyDate
            }

            14 // Mandatory Finish
            -> {
                constraintType = ConstraintType.MUST_FINISH_ON
                constraintDate = earlyDate
            }

            4 // Start As Late As Possible
            -> {
                constraintType = ConstraintType.AS_LATE_AS_POSSIBLE
            }

            3 // Start As Soon As Possible
            -> {
                constraintType = ConstraintType.AS_SOON_AS_POSSIBLE
            }

            8 // Start Between
            -> {
                constraintType = ConstraintType.AS_SOON_AS_POSSIBLE
                constraintDate = earlyDate
            }

            6 // Start On or Before
            -> {
                constraintType = ConstraintType.START_NO_LATER_THAN
                constraintDate = earlyDate
            }

            15 // Work Between
            -> {
                constraintType = ConstraintType.START_NO_EARLIER_THAN
                constraintDate = earlyDate
            }
        }
        task.constraintType = constraintType
        task.constraintDate = constraintDate
    }

    /**
     * Common mechanism to convert Synchro commentary recorss into notes.
     *
     * @param rows commentary table rows
     * @return note text
     */
    private fun getNotes(rows: List<MapRow>?): String? {
        var result: String? = null
        if (rows != null && !rows.isEmpty()) {
            val sb = StringBuilder()
            for (row in rows) {
                sb.append(row.getString("TITLE"))
                sb.append('\n')
                sb.append(row.getString("TEXT"))
                sb.append("\n\n")
            }
            result = sb.toString()
        }
        return result
    }

    /**
     * Sort MapRows based on a named attribute.
     *
     * @param rows map rows to sort
     * @param attribute attribute to sort on
     * @return list argument (allows method chaining)
     */
    private fun sort(rows: List<MapRow>, attribute: String): List<MapRow> {
        Collections.sort(rows, object : Comparator<MapRow>() {
            @Override
            fun compare(o1: MapRow, o2: MapRow): Int {
                val value1 = o1.getString(attribute)
                val value2 = o2.getString(attribute)
                return value1.compareTo(value2)
            }
        })
        return rows
    }

    /**
     * Recursively update parent task dates.
     */
    private fun updateDates() {
        for (task in m_project!!.childTasks) {
            updateDates(task)
        }
    }

    /**
     * Recursively update parent task dates.
     *
     * @param parentTask parent task
     */
    private fun updateDates(parentTask: Task) {
        if (parentTask.hasChildTasks()) {
            var plannedStartDate: Date? = null
            var plannedFinishDate: Date? = null

            for (task in parentTask.childTasks) {
                updateDates(task)
                plannedStartDate = DateHelper.min(plannedStartDate, task.start)
                plannedFinishDate = DateHelper.max(plannedFinishDate, task.finish)
            }

            parentTask.start = plannedStartDate
            parentTask.finish = plannedFinishDate
        }
    }
}
