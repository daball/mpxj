/*
 * file:       TurboProjectReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       09/01/2018
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

package net.sf.mpxj.turboproject

import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.LinkedList

import net.sf.mpxj.ChildTaskContainer
import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.FieldContainer
import net.sf.mpxj.FieldType
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarWeek
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceField
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.common.StreamHelper
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * This class creates a new ProjectFile instance by reading a TurboProject PEP file.
 */
class TurboProjectReader : AbstractProjectReader() {

    private var m_projectFile: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_projectListeners: List<ProjectListener>? = null
    private var m_tables: HashMap<String, Table>? = null
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
            m_tables = HashMap<String, Table>()

            val config = m_projectFile!!.projectConfig
            config.autoResourceID = false
            config.autoCalendarUniqueID = false
            config.autoResourceUniqueID = false
            config.autoTaskID = false
            config.autoTaskUniqueID = false
            config.autoOutlineLevel = true
            config.autoOutlineNumber = true
            config.autoWBS = true

            m_projectFile!!.projectProperties.fileApplication = "TurboProject"
            m_projectFile!!.projectProperties.fileType = "PEP"

            m_eventManager!!.addProjectListeners(m_projectListeners)

            applyAliases()

            readFile(stream)
            readCalendars()
            readResources()
            readTasks()
            readRelationships()
            readResourceAssignments()

            //
            // Ensure that the unique ID counters are correct
            //
            config.updateUniqueCounters()

            return m_projectFile
        } catch (ex: IOException) {
            throw MPXJException("Failed to parse file", ex)
        } finally {
            m_projectFile = null
            m_eventManager = null
            m_projectListeners = null
            m_tables = null
        }
    }

    /**
     * Reads a PEP file from the input stream.
     *
     * @param is input stream representing a PEP file
     */
    @Throws(IOException::class)
    private fun readFile(`is`: InputStream) {
        StreamHelper.skip(`is`, 64)
        var index = 64

        val offsetList = ArrayList<Integer>()
        val nameList = ArrayList<String>()

        while (true) {
            val table = ByteArray(32)
            `is`.read(table)
            index += 32

            val offset = PEPUtility.getInt(table, 0)
            offsetList.add(Integer.valueOf(offset))
            if (offset == 0) {
                break
            }

            nameList.add(PEPUtility.getString(table, 5).toUpperCase())
        }

        StreamHelper.skip(`is`, offsetList.get(0).intValue() - index)

        for (offsetIndex in 1 until offsetList.size() - 1) {
            val name = nameList.get(offsetIndex - 1)
            var tableClass = TABLE_CLASSES.get(name)
            if (tableClass == null) {
                tableClass = Table::class.java
            }

            val table: Table
            try {
                table = tableClass!!.newInstance()
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }

            m_tables!!.put(name, table)
            table.read(`is`)
        }
    }

    /**
     * Read calendar data from a PEP file.
     */
    private fun readCalendars() {
        //
        // Create the calendars
        //
        for (row in getTable("NCALTAB")!!) {
            val calendar = m_projectFile!!.addCalendar()
            calendar.uniqueID = row.getInteger("UNIQUE_ID")
            calendar.name = row.getString("NAME")
            calendar.setWorkingDay(Day.SUNDAY, row.getBoolean("SUNDAY"))
            calendar.setWorkingDay(Day.MONDAY, row.getBoolean("MONDAY"))
            calendar.setWorkingDay(Day.TUESDAY, row.getBoolean("TUESDAY"))
            calendar.setWorkingDay(Day.WEDNESDAY, row.getBoolean("WEDNESDAY"))
            calendar.setWorkingDay(Day.THURSDAY, row.getBoolean("THURSDAY"))
            calendar.setWorkingDay(Day.FRIDAY, row.getBoolean("FRIDAY"))
            calendar.setWorkingDay(Day.SATURDAY, row.getBoolean("SATURDAY"))

            for (day in Day.values()) {
                if (calendar.isWorkingDay(day)) {
                    // TODO: this is an approximation
                    calendar.addDefaultCalendarHours(day)
                }
            }
        }

        //
        // Set up the hierarchy and add exceptions
        //
        val exceptionsTable = getTable("CALXTAB")
        for (row in getTable("NCALTAB")!!) {
            val child = m_projectFile!!.getCalendarByUniqueID(row.getInteger("UNIQUE_ID"))
            val parent = m_projectFile!!.getCalendarByUniqueID(row.getInteger("BASE_CALENDAR_ID"))
            if (child != null && parent != null) {
                child.parent = parent
            }

            addCalendarExceptions(exceptionsTable!!, child, row.getInteger("FIRST_CALENDAR_EXCEPTION_ID"))

            m_eventManager!!.fireCalendarReadEvent(child)
        }
    }

    /**
     * Read exceptions for a calendar.
     *
     * @param table calendar exception data
     * @param calendar calendar
     * @param exceptionID first exception ID
     */
    private fun addCalendarExceptions(table: Table, calendar: ProjectCalendar?, exceptionID: Integer) {
        var currentExceptionID = exceptionID
        while (true) {
            val row = table.find(currentExceptionID) ?: break

            val date = row.getDate("DATE")
            val exception = calendar!!.addCalendarException(date, date)
            if (row.getBoolean("WORKING")) {
                exception.addRange(ProjectCalendarWeek.DEFAULT_WORKING_MORNING)
                exception.addRange(ProjectCalendarWeek.DEFAULT_WORKING_AFTERNOON)
            }

            currentExceptionID = row.getInteger("NEXT_CALENDAR_EXCEPTION_ID")
        }
    }

    /**
     * Read resource data from a PEP file.
     */
    private fun readResources() {
        for (row in getTable("RTAB")!!) {
            val resource = m_projectFile!!.addResource()
            setFields(RESOURCE_FIELDS, row, resource)
            m_eventManager!!.fireResourceReadEvent(resource)
            // TODO: Correctly handle calendar
        }
    }

    /**
     * Read task data from a PEP file.
     */
    private fun readTasks() {
        val rootID = Integer.valueOf(1)
        readWBS(m_projectFile, rootID)
        readTasks(rootID)
        m_projectFile!!.tasks.synchronizeTaskIDToHierarchy()
    }

    /**
     * Recursively read the WBS structure from a PEP file.
     *
     * @param parent parent container for tasks
     * @param id initial WBS ID
     */
    private fun readWBS(parent: ChildTaskContainer?, id: Integer) {
        var currentID = id
        val table = getTable("WBSTAB")

        while (currentID.intValue() !== 0) {
            val row = table!!.find(currentID)
            val taskID = row.getInteger("TASK_ID")
            val task = readTask(parent!!, taskID)
            val childID = row.getInteger("CHILD_ID")
            if (childID.intValue() !== 0) {
                readWBS(task, childID)
            }
            currentID = row.getInteger("NEXT_ID")
        }
    }

    /**
     * Read leaf tasks attached to the WBS.
     *
     * @param id initial WBS ID
     */
    private fun readTasks(id: Integer) {
        var currentID = id
        val table = getTable("WBSTAB")

        while (currentID.intValue() !== 0) {
            val row = table!!.find(currentID)
            val task = m_projectFile!!.getTaskByUniqueID(row.getInteger("TASK_ID"))
            readLeafTasks(task, row.getInteger("FIRST_CHILD_TASK_ID"))
            val childID = row.getInteger("CHILD_ID")
            if (childID.intValue() !== 0) {
                readTasks(childID)
            }
            currentID = row.getInteger("NEXT_ID")
        }
    }

    /**
     * Read the leaf tasks for an individual WBS node.
     *
     * @param parent parent task
     * @param id first task ID
     */
    private fun readLeafTasks(parent: Task, id: Integer) {
        var currentID = id
        val table = getTable("A1TAB")
        while (currentID.intValue() !== 0) {
            if (m_projectFile!!.getTaskByUniqueID(currentID) == null) {
                readTask(parent, currentID)
            }
            currentID = table!!.find(currentID).getInteger("NEXT_TASK_ID")
        }
    }

    /**
     * Read data for an individual task from the tables in a PEP file.
     *
     * @param parent parent task
     * @param id task ID
     * @return task instance
     */
    private fun readTask(parent: ChildTaskContainer, id: Integer): Task {
        val a0 = getTable("A0TAB")
        val a1 = getTable("A1TAB")
        val a2 = getTable("A2TAB")
        val a3 = getTable("A3TAB")
        val a4 = getTable("A4TAB")

        val task = parent.addTask()
        val a1Row = a1!!.find(id)
        val a2Row = a2!!.find(id)

        setFields(A0TAB_FIELDS, a0!!.find(id), task)
        setFields(A1TAB_FIELDS, a1Row, task)
        setFields(A2TAB_FIELDS, a2Row, task)
        setFields(A3TAB_FIELDS, a3!!.find(id), task)
        setFields(A5TAB_FIELDS, a4!!.find(id), task)

        task.start = task.earlyStart
        task.finish = task.earlyFinish
        if (task.name == null) {
            task.name = task.getText(1)
        }

        m_eventManager!!.fireTaskReadEvent(task)

        return task
    }

    /**
     * Read relationship data from a PEP file.
     */
    private fun readRelationships() {
        for (row in getTable("CONTAB")!!) {
            val task1 = m_projectFile!!.getTaskByUniqueID(row.getInteger("TASK_ID_1"))
            val task2 = m_projectFile!!.getTaskByUniqueID(row.getInteger("TASK_ID_2"))

            if (task1 != null && task2 != null) {
                val type = row.getRelationType("TYPE")
                val lag = row.getDuration("LAG")
                val relation = task2.addPredecessor(task1, type, lag)
                m_eventManager!!.fireRelationReadEvent(relation)
            }
        }
    }

    /**
     * Read resource assignment data from a PEP file.
     */
    private fun readResourceAssignments() {
        for (row in getTable("USGTAB")!!) {
            val task = m_projectFile!!.getTaskByUniqueID(row.getInteger("TASK_ID"))
            val resource = m_projectFile!!.getResourceByUniqueID(row.getInteger("RESOURCE_ID"))
            if (task != null && resource != null) {
                val assignment = task.addResourceAssignment(resource)
                m_eventManager!!.fireAssignmentReadEvent(assignment)
            }
        }
    }

    /**
     * Retrieve a table by name.
     *
     * @param name table name
     * @return Table instance
     */
    private fun getTable(name: String): Table? {
        var table = m_tables!!.get(name)
        if (table == null) {
            table = EMPTY_TABLE
        }
        return table
    }

    /**
     * Configure column aliases.
     */
    private fun applyAliases() {
        val fields = m_projectFile!!.customFields
        for (entry in ALIASES.entrySet()) {
            fields.getCustomField(entry.getKey()).setAlias(entry.getValue())
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
            if (alias != null) {
                ALIASES.put(type, alias)
            }
        }

        private val EMPTY_TABLE = Table()

        private val TABLE_CLASSES = HashMap<String, Class<out Table>>()

        init {
            TABLE_CLASSES.put("RTAB", TableRTAB::class.java)
            TABLE_CLASSES.put("A0TAB", TableA0TAB::class.java)
            TABLE_CLASSES.put("A1TAB", TableA1TAB::class.java)
            TABLE_CLASSES.put("A2TAB", TableA2TAB::class.java)
            TABLE_CLASSES.put("A3TAB", TableA3TAB::class.java)
            TABLE_CLASSES.put("A5TAB", TableA5TAB::class.java)
            TABLE_CLASSES.put("CONTAB", TableCONTAB::class.java)
            TABLE_CLASSES.put("USGTAB", TableUSGTAB::class.java)
            TABLE_CLASSES.put("NCALTAB", TableNCALTAB::class.java)
            TABLE_CLASSES.put("CALXTAB", TableCALXTAB::class.java)
            TABLE_CLASSES.put("WBSTAB", TableWBSTAB::class.java)
        }

        private val ALIASES = HashMap<FieldType, String>()
        private val RESOURCE_FIELDS = HashMap<String, FieldType>()
        private val A0TAB_FIELDS = HashMap<String, FieldType>()
        private val A1TAB_FIELDS = HashMap<String, FieldType>()
        private val A2TAB_FIELDS = HashMap<String, FieldType>()
        private val A3TAB_FIELDS = HashMap<String, FieldType>()
        private val A5TAB_FIELDS = HashMap<String, FieldType>()

        init {
            defineField(RESOURCE_FIELDS, "ID", ResourceField.ID)
            defineField(RESOURCE_FIELDS, "UNIQUE_ID", ResourceField.UNIQUE_ID)
            defineField(RESOURCE_FIELDS, "NAME", ResourceField.NAME)
            defineField(RESOURCE_FIELDS, "GROUP", ResourceField.GROUP)
            defineField(RESOURCE_FIELDS, "DESCRIPTION", ResourceField.NOTES)
            defineField(RESOURCE_FIELDS, "PARENT_ID", ResourceField.PARENT_ID)

            defineField(RESOURCE_FIELDS, "RATE", ResourceField.NUMBER1, "Rate")
            defineField(RESOURCE_FIELDS, "POOL", ResourceField.NUMBER2, "Pool")
            defineField(RESOURCE_FIELDS, "PER_DAY", ResourceField.NUMBER3, "Per Day")
            defineField(RESOURCE_FIELDS, "PRIORITY", ResourceField.NUMBER4, "Priority")
            defineField(RESOURCE_FIELDS, "PERIOD_DUR", ResourceField.NUMBER5, "Period Dur")
            defineField(RESOURCE_FIELDS, "EXPENSES_ONLY", ResourceField.FLAG1, "Expenses Only")
            defineField(RESOURCE_FIELDS, "MODIFY_ON_INTEGRATE", ResourceField.FLAG2, "Modify On Integrate")
            defineField(RESOURCE_FIELDS, "UNIT", ResourceField.TEXT1, "Unit")

            defineField(A0TAB_FIELDS, "UNIQUE_ID", TaskField.UNIQUE_ID)

            defineField(A1TAB_FIELDS, "ORDER", TaskField.ID)
            defineField(A1TAB_FIELDS, "PLANNED_START", TaskField.BASELINE_START)
            defineField(A1TAB_FIELDS, "PLANNED_FINISH", TaskField.BASELINE_FINISH)

            defineField(A2TAB_FIELDS, "DESCRIPTION", TaskField.TEXT1, "Description")

            defineField(A3TAB_FIELDS, "EARLY_START", TaskField.EARLY_START)
            defineField(A3TAB_FIELDS, "LATE_START", TaskField.LATE_START)
            defineField(A3TAB_FIELDS, "EARLY_FINISH", TaskField.EARLY_FINISH)
            defineField(A3TAB_FIELDS, "LATE_FINISH", TaskField.LATE_FINISH)

            defineField(A5TAB_FIELDS, "ORIGINAL_DURATION", TaskField.DURATION)
            defineField(A5TAB_FIELDS, "REMAINING_DURATION", TaskField.REMAINING_DURATION)
            defineField(A5TAB_FIELDS, "PERCENT_COMPLETE", TaskField.PERCENT_COMPLETE)
            defineField(A5TAB_FIELDS, "TARGET_START", TaskField.DATE1, "Target Start")
            defineField(A5TAB_FIELDS, "TARGET_FINISH", TaskField.DATE2, "Target Finish")
            defineField(A5TAB_FIELDS, "ACTUAL_START", TaskField.ACTUAL_START)
            defineField(A5TAB_FIELDS, "ACTUAL_FINISH", TaskField.ACTUAL_FINISH)
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
