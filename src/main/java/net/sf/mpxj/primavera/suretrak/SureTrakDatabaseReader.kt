/*
 * file:       SureTrakDatabaseReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       01/03/2018
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

package net.sf.mpxj.primavera.suretrak

import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.HashMap
import java.util.LinkedList

import net.sf.mpxj.ChildTaskContainer
import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.FieldContainer
import net.sf.mpxj.FieldType
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.RecurrenceType
import net.sf.mpxj.RecurringData
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceField
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.AlphanumComparator
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.primavera.common.MapRow
import net.sf.mpxj.primavera.common.Table
import net.sf.mpxj.reader.ProjectReader

/**
 * Reads schedule data from a SureTrak multi-file database in a directory.
 */
class SureTrakDatabaseReader : ProjectReader {

    private var m_projectName: String? = null
    private var m_projectFile: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_projectListeners: List<ProjectListener>? = null
    private var m_tables: Map<String, Table>? = null
    private var m_wbsFormat: SureTrakWbsFormat? = null
    private var m_definitions: Map<Integer, List<MapRow>>? = null
    private var m_calendarMap: Map<Integer, ProjectCalendar>? = null
    private var m_resourceMap: Map<String, Resource>? = null
    private var m_wbsMap: Map<String, Task>? = null
    private var m_activityMap: Map<String, Task>? = null

    @Override
    override fun addProjectListener(listener: ProjectListener) {
        if (m_projectListeners == null) {
            m_projectListeners = LinkedList<ProjectListener>()
        }
        m_projectListeners!!.add(listener)
    }

    @Override
    @Throws(MPXJException::class)
    override fun read(fileName: String): ProjectFile {
        return read(File(fileName))
    }

    @Override
    override fun read(inputStream: InputStream): ProjectFile {
        throw UnsupportedOperationException()
    }

    /**
     * Set the prefix used to identify which database is read from the directory.
     * There may potentially be more than one database in a directory.
     *
     * @param prefix file name prefix
     *
     */
    @Deprecated("Use setProjectName")
    fun setPrefix(prefix: String) {
        m_projectName = prefix
    }

    /**
     * Set the project name (file name prefix) used to identify which database is read from the directory.
     * There may potentially be more than one database in a directory.
     *
     * @param projectName project name
     */
    fun setProjectName(projectName: String) {
        m_projectName = projectName
    }

    @Override
    @Throws(MPXJException::class)
    override fun read(directory: File): ProjectFile {
        if (!directory.isDirectory()) {
            throw MPXJException("Directory expected")
        }

        try {
            m_projectFile = ProjectFile()
            m_eventManager = m_projectFile!!.eventManager

            val config = m_projectFile!!.projectConfig
            config.autoResourceID = true
            config.autoResourceUniqueID = true
            config.autoTaskID = true
            config.autoTaskUniqueID = true
            config.autoOutlineLevel = true
            config.autoOutlineNumber = true
            config.autoWBS = false

            // Activity ID
            val customFields = m_projectFile!!.customFields
            customFields.getCustomField(TaskField.TEXT1).setAlias("Code")
            customFields.getCustomField(TaskField.TEXT2).setAlias("Department")
            customFields.getCustomField(TaskField.TEXT3).setAlias("Manager")
            customFields.getCustomField(TaskField.TEXT4).setAlias("Section")
            customFields.getCustomField(TaskField.TEXT5).setAlias("Mail")

            m_projectFile!!.projectProperties.fileApplication = "SureTrak"
            m_projectFile!!.projectProperties.fileType = "STW"

            m_eventManager!!.addProjectListeners(m_projectListeners)

            m_tables = DatabaseReader().process(directory, m_projectName!!)
            m_definitions = HashMap<Integer, List<MapRow>>()
            m_calendarMap = HashMap<Integer, ProjectCalendar>()
            m_resourceMap = HashMap<String, Resource>()
            m_wbsMap = HashMap<String, Task>()
            m_activityMap = HashMap<String, Task>()

            readProjectHeader()
            readDefinitions()
            readCalendars()
            readHolidays()
            readResources()
            readTasks()
            readRelationships()
            readResourceAssignments()

            return m_projectFile
        } catch (ex: IOException) {
            throw MPXJException("Failed to parse file", ex)
        } finally {
            m_projectFile = null
            m_eventManager = null
            m_projectListeners = null
            m_tables = null
            m_definitions = null
            m_wbsFormat = null
            m_calendarMap = null
            m_resourceMap = null
            m_wbsMap = null
            m_activityMap = null
        }
    }

    /**
     * Read general project properties.
     */
    private fun readProjectHeader() {
        // No header data read
    }

    /**
     * Extract definition records from the table and divide into groups.
     */
    private fun readDefinitions() {
        for (row in m_tables!!["TTL"]) {
            val id = row.getInteger("DEFINITION_ID")
            var list = m_definitions!![id]
            if (list == null) {
                list = ArrayList<MapRow>()
                m_definitions!!.put(id, list)
            }
            list!!.add(row)
        }

        val rows = m_definitions!![WBS_FORMAT_ID]
        if (rows != null) {
            m_wbsFormat = SureTrakWbsFormat(rows[0])
        }
    }

    /**
     * Read project calendars.
     */
    private fun readCalendars() {
        val cal = m_tables!!["CAL"]
        for (row in cal) {
            val calendar = m_projectFile!!.addCalendar()
            m_calendarMap!!.put(row.getInteger("CALENDAR_ID"), calendar)
            val days = arrayOf<Integer>(row.getInteger("SUNDAY_HOURS"), row.getInteger("MONDAY_HOURS"), row.getInteger("TUESDAY_HOURS"), row.getInteger("WEDNESDAY_HOURS"), row.getInteger("THURSDAY_HOURS"), row.getInteger("FRIDAY_HOURS"), row.getInteger("SATURDAY_HOURS"))

            calendar.name = row.getString("NAME")
            readHours(calendar, Day.SUNDAY, days[0])
            readHours(calendar, Day.MONDAY, days[1])
            readHours(calendar, Day.TUESDAY, days[2])
            readHours(calendar, Day.WEDNESDAY, days[3])
            readHours(calendar, Day.THURSDAY, days[4])
            readHours(calendar, Day.FRIDAY, days[5])
            readHours(calendar, Day.SATURDAY, days[6])

            var workingDaysPerWeek = 0
            for (day in Day.values()) {
                if (calendar.isWorkingDay(day)) {
                    ++workingDaysPerWeek
                }
            }

            var workingHours: Integer? = null
            for (index in 0..6) {
                if (days[index].intValue() !== 0) {
                    workingHours = days[index]
                    break
                }
            }

            if (workingHours != null) {
                val workingHoursPerDay = countHours(workingHours!!)
                val minutesPerDay = workingHoursPerDay * 60
                val minutesPerWeek = minutesPerDay * workingDaysPerWeek
                val minutesPerMonth = 4 * minutesPerWeek
                val minutesPerYear = 52 * minutesPerWeek

                calendar.setMinutesPerDay(Integer.valueOf(minutesPerDay))
                calendar.setMinutesPerWeek(Integer.valueOf(minutesPerWeek))
                calendar.setMinutesPerMonth(Integer.valueOf(minutesPerMonth))
                calendar.setMinutesPerYear(Integer.valueOf(minutesPerYear))
            }
        }
    }

    /**
     * Reads the integer representation of calendar hours for a given
     * day and populates the calendar.
     *
     * @param calendar parent calendar
     * @param day target day
     * @param hours working hours
     */
    private fun readHours(calendar: ProjectCalendar, day: Day, hours: Integer) {
        var value = hours.intValue()
        var startHour = 0
        var calendarHours: ProjectCalendarHours? = null

        val cal = DateHelper.popCalendar()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        calendar.setWorkingDay(day, false)

        while (value != 0) {
            // Move forward until we find a working hour
            while (startHour < 24 && value and 0x1 == 0) {
                value = value shr 1
                ++startHour
            }

            // No more working hours, bail out
            if (startHour >= 24) {
                break
            }

            // Move forward until we find the end of the working hours
            var endHour = startHour
            while (endHour < 24 && value and 0x1 != 0) {
                value = value shr 1
                ++endHour
            }

            cal.set(Calendar.HOUR_OF_DAY, startHour)
            val startDate = cal.getTime()
            cal.set(Calendar.HOUR_OF_DAY, endHour)
            val endDate = cal.getTime()

            if (calendarHours == null) {
                calendarHours = calendar.addCalendarHours(day)
                calendar.setWorkingDay(day, true)
            }
            calendarHours!!.addRange(DateRange(startDate, endDate))
            startHour = endHour
        }

        DateHelper.pushCalendar(cal)
    }

    /**
     * Count the number of working hours in a day, based in the
     * integer representation of the working hours.
     *
     * @param hours working hours
     * @return number of hours
     */
    private fun countHours(hours: Integer): Int {
        var value = hours.intValue()
        var hoursPerDay = 0
        var hour = 0
        while (value > 0) {
            // Move forward until we find a working hour
            while (hour < 24) {
                if (value and 0x1 != 0) {
                    ++hoursPerDay
                }
                value = value shr 1
                ++hour
            }
        }
        return hoursPerDay
    }

    /**
     * Read holidays from the database and create calendar exceptions.
     */
    private fun readHolidays() {
        for (row in m_tables!!["HOL"]) {
            val calendar = m_calendarMap!![row.getInteger("CALENDAR_ID")]
            if (calendar != null) {
                val date = row.getDate("DATE")
                val exception = calendar.addCalendarException(date, date)
                if (row.getBoolean("ANNUAL")) {
                    val recurring = RecurringData()
                    recurring.recurrenceType = RecurrenceType.YEARLY
                    recurring.setYearlyAbsoluteFromDate(date)
                    recurring.startDate = date
                    exception.recurring = recurring
                    // TODO set end date based on project end date
                }
            }
        }
    }

    /**
     * Read resources.
     */
    private fun readResources() {
        m_resourceMap = HashMap<String, Resource>()
        for (row in m_tables!!["RLB"]) {
            val resource = m_projectFile!!.addResource()
            setFields(RESOURCE_FIELDS, row, resource)
            val calendar = m_calendarMap!![row.getInteger("CALENDAR_ID")]
            if (calendar != null) {
                val baseCalendar = m_calendarMap!![row.getInteger("BASE_CALENDAR_ID")]
                calendar.parent = baseCalendar
                resource.resourceCalendar = calendar
            }
            m_resourceMap!!.put(resource.code, resource)
        }
    }

    /**
     * Read tasks.
     */
    private fun readTasks() {
        readWbs()
        readActivities()
        updateDates()
    }

    /**
     * Read the WBS.
     */
    private fun readWbs() {
        val levelMap = HashMap<Integer, List<MapRow>>()
        val table = m_definitions!![WBS_ENTRIES_ID]
        if (table != null) {
            for (row in table) {
                m_wbsFormat!!.parseRawValue(row.getString("TEXT1"))
                val level = m_wbsFormat!!.level
                var items = levelMap.get(level)
                if (items == null) {
                    items = ArrayList<MapRow>()
                    levelMap.put(level, items)
                }
                items!!.add(row)
            }

            var level = 1
            while (true) {
                val items = levelMap.get(Integer.valueOf(level++)) ?: break

                for (row in items) {
                    m_wbsFormat!!.parseRawValue(row.getString("TEXT1"))
                    val parentWbsValue = m_wbsFormat!!.formattedParentValue
                    val wbsValue = m_wbsFormat!!.formattedValue
                    row.setObject("WBS", wbsValue)
                    row.setObject("PARENT_WBS", parentWbsValue)
                }

                val comparator = AlphanumComparator()
                Collections.sort(items, object : Comparator<MapRow>() {
                    @Override
                    fun compare(o1: MapRow, o2: MapRow): Int {
                        return comparator.compare(o1.getString("WBS"), o2.getString("WBS"))
                    }
                })

                for (row in items) {
                    val wbs = row.getString("WBS")
                    var parent = m_wbsMap!![row.getString("PARENT_WBS")]
                    if (parent == null) {
                        parent = m_projectFile
                    }

                    val task = parent!!.addTask()
                    var name: String? = row.getString("TEXT2")
                    if (name == null || name.isEmpty()) {
                        name = wbs
                    }
                    task.name = name
                    task.wbs = wbs
                    task.summary = true
                    m_wbsMap!!.put(wbs, task)
                }
            }
        }
    }

    /**
     * Read activities.
     */
    private fun readActivities() {
        val items = ArrayList<MapRow>()
        for (row in m_tables!!["ACT"]) {
            items.add(row)
        }
        val comparator = AlphanumComparator()
        Collections.sort(items, object : Comparator<MapRow>() {
            @Override
            fun compare(o1: MapRow, o2: MapRow): Int {
                return comparator.compare(o1.getString("ACTIVITY_ID"), o2.getString("ACTIVITY_ID"))
            }
        })

        for (row in items) {
            val activityID = row.getString("ACTIVITY_ID")

            val wbs: String?
            if (m_wbsFormat == null) {
                wbs = null
            } else {
                m_wbsFormat!!.parseRawValue(row.getString("WBS"))
                wbs = m_wbsFormat!!.formattedValue
            }

            var parent = m_wbsMap!!.get(wbs)
            if (parent == null) {
                parent = m_projectFile
            }

            val task = parent!!.addTask()
            setFields(TASK_FIELDS, row, task)
            task.start = task.earlyStart
            task.finish = task.earlyFinish
            task.milestone = task.duration!!.getDuration() === 0
            task.wbs = wbs
            val duration = task.duration
            val remainingDuration = task.remainingDuration
            task.actualDuration = Duration.getInstance(duration!!.getDuration() - remainingDuration.getDuration(), TimeUnit.HOURS)
            m_activityMap!!.put(activityID, task)
        }
    }

    /**
     * Read task relationships.
     */
    private fun readRelationships() {
        for (row in m_tables!!["REL"]) {
            val predecessor = m_activityMap!![row.getString("PREDECESSOR_ACTIVITY_ID")]
            val successor = m_activityMap!![row.getString("SUCCESSOR_ACTIVITY_ID")]
            if (predecessor != null && successor != null) {
                val lag = row.getDuration("LAG")
                val type = row.getRelationType("TYPE")

                successor.addPredecessor(predecessor, type, lag)
            }
        }
    }

    /**
     * Read resource assignments.
     */
    private fun readResourceAssignments() {
        for (row in m_tables!!["RES"]) {
            val task = m_activityMap!![row.getString("ACTIVITY_ID")]
            val resource = m_resourceMap!![row.getString("RESOURCE_ID")]
            if (task != null && resource != null) {
                task.addResourceAssignment(resource)
            }
        }
    }

    /**
     * Ensure summary tasks have dates.
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
            var startDate: Date? = parentTask.start
            var finishDate: Date? = parentTask.finish
            var actualStartDate = parentTask.actualStart
            var actualFinishDate: Date? = parentTask.actualFinish
            var earlyStartDate: Date? = parentTask.earlyStart
            var earlyFinishDate: Date? = parentTask.earlyFinish
            var lateStartDate: Date? = parentTask.lateStart
            var lateFinishDate: Date? = parentTask.lateFinish

            for (task in parentTask.childTasks) {
                updateDates(task)

                startDate = DateHelper.min(startDate, task.start)
                finishDate = DateHelper.max(finishDate, task.finish)
                actualStartDate = DateHelper.min(actualStartDate, task.actualStart)
                actualFinishDate = DateHelper.max(actualFinishDate, task.actualFinish)
                earlyStartDate = DateHelper.min(earlyStartDate, task.earlyStart)
                earlyFinishDate = DateHelper.max(earlyFinishDate, task.earlyFinish)
                lateStartDate = DateHelper.min(lateStartDate, task.lateStart)
                lateFinishDate = DateHelper.max(lateFinishDate, task.lateFinish)

                if (task.actualFinish != null) {
                    ++finished
                }
            }

            parentTask.start = startDate
            parentTask.finish = finishDate
            parentTask.actualStart = actualStartDate
            parentTask.earlyStart = earlyStartDate
            parentTask.earlyFinish = earlyFinishDate
            parentTask.lateStart = lateStartDate
            parentTask.lateFinish = lateFinishDate

            //
            // Only if all child tasks have actual finish dates do we
            // set the actual finish date on the parent task.
            //
            if (finished == parentTask.childTasks.size()) {
                parentTask.actualFinish = actualFinishDate
            }
        }
    }

    /**
     * Set the value of one or more fields based on the contents of a database row.
     *
     * @param map column to field map
     * @param row database row
     * @param container field container
     */
    private fun setFields(map: Map<String, FieldType>, row: MapRow?, container: FieldContainer) {
        if (row != null) {
            for (entry in map.entrySet()) {
                container.set(entry.getValue(), row.getObject(entry.getKey()))
            }
        }
    }

    companion object {
        /**
         * Convenience method which locates the first SureTrak database in a directory
         * and opens it.
         *
         * @param directory directory containing a SureTrak database
         * @return ProjectFile instance
         *
         */
        @Deprecated("Use setProjectNameAndRead")
        @Throws(MPXJException::class)
        fun setPrefixAndRead(directory: File): ProjectFile? {
            return setProjectNameAndRead(directory)
        }

        /**
         * Convenience method which locates the first SureTrak database in a directory
         * and opens it.
         *
         * @param directory directory containing a SureTrak database
         * @return ProjectFile instance
         */
        @Throws(MPXJException::class)
        fun setProjectNameAndRead(directory: File): ProjectFile? {
            val projects = listProjectNames(directory)

            if (!projects.isEmpty()) {
                val reader = SureTrakDatabaseReader()
                reader.setProjectName(projects.get(0))
                return reader.read(directory)
            }

            return null
        }

        /**
         * Retrieve a list of the available SureTrak project names from a directory.
         *
         * @param directory name of the directory containing SureTrak files
         * @return list of project names
         */
        fun listProjectNames(directory: String): List<String> {
            return listProjectNames(File(directory))
        }

        /**
         * Retrieve a list of the available SureTrak project names from a directory.
         *
         * @param directory directory containing SureTrak files
         * @return list of project names
         */
        fun listProjectNames(directory: File): List<String> {
            val result = ArrayList<String>()

            val files = directory.listFiles(object : FilenameFilter() {
                @Override
                fun accept(dir: File, name: String): Boolean {
                    return name.toUpperCase().endsWith(".DIR")
                }
            })

            if (files != null) {
                for (file in files!!) {
                    val fileName = file.getName()
                    val prefix = fileName.substring(0, fileName.length() - 4)
                    result.add(prefix)
                }
            }

            Collections.sort(result)

            return result
        }

        /**
         * Configure the mapping between a database column and a field, including definition of
         * an alias.
         *
         * @param container column to field map
         * @param name column name
         * @param type field type
         * @param alias field alias
         */
        private fun defineField(container: Map<String, FieldType>, name: String, type: FieldType, alias: String? = null) {
            container.put(name, type)
            //      if (alias != null)
            //      {
            //         ALIASES.put(type, alias);
            //      }
        }

        private val WBS_FORMAT_ID = Integer.valueOf(0x79)
        private val WBS_ENTRIES_ID = Integer.valueOf(0x7A)

        private val RESOURCE_FIELDS = HashMap<String, FieldType>()
        private val TASK_FIELDS = HashMap<String, FieldType>()

        init {
            defineField(RESOURCE_FIELDS, "NAME", ResourceField.NAME)
            defineField(RESOURCE_FIELDS, "CODE", ResourceField.CODE)

            defineField(TASK_FIELDS, "NAME", TaskField.NAME)
            defineField(TASK_FIELDS, "ACTIVITY_ID", TaskField.TEXT1)
            defineField(TASK_FIELDS, "DEPARTMENT", TaskField.TEXT2)
            defineField(TASK_FIELDS, "MANAGER", TaskField.TEXT3)
            defineField(TASK_FIELDS, "SECTION", TaskField.TEXT4)
            defineField(TASK_FIELDS, "MAIL", TaskField.TEXT5)

            defineField(TASK_FIELDS, "PERCENT_COMPLETE", TaskField.PERCENT_COMPLETE)
            defineField(TASK_FIELDS, "EARLY_START", TaskField.EARLY_START)
            defineField(TASK_FIELDS, "LATE_START", TaskField.LATE_START)
            defineField(TASK_FIELDS, "EARLY_FINISH", TaskField.EARLY_FINISH)
            defineField(TASK_FIELDS, "LATE_FINISH", TaskField.LATE_FINISH)
            defineField(TASK_FIELDS, "ACTUAL_START", TaskField.ACTUAL_START)
            defineField(TASK_FIELDS, "ACTUAL_FINISH", TaskField.ACTUAL_FINISH)
            defineField(TASK_FIELDS, "ORIGINAL_DURATION", TaskField.DURATION)
            defineField(TASK_FIELDS, "REMAINING_DURATION", TaskField.REMAINING_DURATION)
        }
    }
}
/**
 * Configure the mapping between a database column and a field.
 *
 * @param container column to field map
 * @param name column name
 * @param type field type
 */
