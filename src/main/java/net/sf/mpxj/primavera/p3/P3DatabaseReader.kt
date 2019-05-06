/*
 * file:       P3DatabaseReader.java
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

package net.sf.mpxj.primavera.p3

import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.HashMap
import java.util.LinkedList

import net.sf.mpxj.ChildTaskContainer
import net.sf.mpxj.ConstraintType
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.FieldContainer
import net.sf.mpxj.FieldType
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectField
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceField
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.common.AlphanumComparator
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.primavera.common.MapRow
import net.sf.mpxj.primavera.common.Table
import net.sf.mpxj.reader.ProjectReader

/**
 * Reads schedule data from a P3 multi-file Btrieve database in a directory.
 */
class P3DatabaseReader : ProjectReader {

    private var m_projectName: String? = null
    private var m_projectFile: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_projectListeners: List<ProjectListener>? = null
    private var m_tables: Map<String, Table>? = null
    private var m_wbsFormat: P3WbsFormat? = null
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
            m_projectFile!!.customFields.getCustomField(TaskField.TEXT1).setAlias("Code")

            m_projectFile!!.projectProperties.fileApplication = "P3"
            m_projectFile!!.projectProperties.fileType = "BTRIEVE"

            m_eventManager!!.addProjectListeners(m_projectListeners)

            m_tables = DatabaseReader().process(directory, m_projectName!!)
            m_resourceMap = HashMap<String, Resource>()
            m_wbsMap = HashMap<String, Task>()
            m_activityMap = HashMap<String, Task>()

            readProjectHeader()
            readCalendars()
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
            m_resourceMap = null
            m_wbsFormat = null
            m_wbsMap = null
            m_activityMap = null
        }
    }

    /**
     * Read general project properties.
     */
    private fun readProjectHeader() {
        val table = m_tables!!["DIR"]
        val row = table.find("")
        if (row != null) {
            setFields(PROJECT_FIELDS, row, m_projectFile!!.projectProperties)
            m_wbsFormat = P3WbsFormat(row)
        }
    }

    /**
     * Read project calendars.
     */
    private fun readCalendars() {
        // TODO: understand the calendar data representation.
    }

    /**
     * Read resources.
     */
    private fun readResources() {
        for (row in m_tables!!["RLB"]) {
            val resource = m_projectFile!!.addResource()
            setFields(RESOURCE_FIELDS, row, resource)
            m_resourceMap!!.put(resource.code, resource)
        }
    }

    /**
     * Read tasks.
     */
    private fun readTasks() {
        readWBS()
        readActivities()
        updateDates()
    }

    /**
     * Read tasks representing the WBS.
     */
    private fun readWBS() {
        val levelMap = HashMap<Integer, List<MapRow>>()
        for (row in m_tables!!["STR"]) {
            val level = row.getInteger("LEVEL_NUMBER")
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
                m_wbsFormat!!.parseRawValue(row.getString("CODE_VALUE"))
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
                if (wbs != null && !wbs!!.isEmpty()) {
                    var parent = m_wbsMap!![row.getString("PARENT_WBS")]
                    if (parent == null) {
                        parent = m_projectFile
                    }

                    val task = parent!!.addTask()
                    var name: String? = row.getString("CODE_TITLE")
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
     * Read tasks representing activities.
     */
    private fun readActivities() {
        val parentMap = HashMap<String, ChildTaskContainer>()
        for (row in m_tables!!["WBS"]) {
            val activityID = row.getString("ACTIVITY_ID")
            m_wbsFormat!!.parseRawValue(row.getString("CODE_VALUE"))
            val parentWBS = m_wbsFormat!!.formattedValue

            val parent = m_wbsMap!!.get(parentWBS)
            if (parent != null) {
                parentMap.put(activityID, parent)
            }
        }

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
            var parent = parentMap.get(activityID)
            if (parent == null) {
                parent = m_projectFile
            }
            val task = parent!!.addTask()
            setFields(TASK_FIELDS, row, task)
            task.start = task.earlyStart
            task.finish = task.earlyFinish
            task.milestone = task.duration!!.getDuration() === 0
            if (parent is Task) {
                task.wbs = (parent as Task).wbs
            }

            var flag = row.getInteger("ACTUAL_START_OR_CONSTRAINT_FLAG").intValue()
            if (flag != 0) {
                val date = row.getDate("AS_OR_ED_CONSTRAINT")
                when (flag) {
                    1 -> {
                        task.constraintType = ConstraintType.START_NO_EARLIER_THAN
                        task.constraintDate = date
                    }

                    3 -> {
                        task.constraintType = ConstraintType.FINISH_NO_EARLIER_THAN
                        task.constraintDate = date
                    }

                    99 -> {
                        task.actualStart = date
                    }
                }
            }

            flag = row.getInteger("ACTUAL_FINISH_OR_CONSTRAINT_FLAG").intValue()
            if (flag != 0) {
                val date = row.getDate("AF_OR_LD_CONSTRAINT")
                when (flag) {
                    2 -> {
                        task.constraintType = ConstraintType.START_NO_LATER_THAN
                        task.constraintDate = date
                    }

                    4 -> {
                        task.constraintType = ConstraintType.FINISH_NO_LATER_THAN
                        task.constraintDate = date
                    }

                    99 -> {
                        task.actualFinish = date
                    }
                }
            }

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
                val lag = row.getDuration("LAG_VALUE")
                val type = row.getRelationType("LAG_TYPE")

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
         * Convenience method which locates the first P3 database in a directory
         * and opens it.
         *
         * @param directory directory containing a P3 database
         * @return ProjectFile instance
         *
         */
        @Deprecated("Use setProjectAndRead")
        @Throws(MPXJException::class)
        fun setPrefixAndRead(directory: File): ProjectFile? {
            return setProjectNameAndRead(directory)
        }

        /**
         * Convenience method which locates the first P3 database in a directory
         * and opens it.
         *
         * @param directory directory containing a P3 database
         * @return ProjectFile instance
         */
        @Throws(MPXJException::class)
        fun setProjectNameAndRead(directory: File): ProjectFile? {
            val projects = listProjectNames(directory)

            if (!projects.isEmpty()) {
                val reader = P3DatabaseReader()
                reader.setProjectName(projects.get(0))
                return reader.read(directory)
            }

            return null
        }

        /**
         * Retrieve a list of the available P3 project names from a directory.
         *
         * @param directory name of the directory containing P3 files
         * @return list of project names
         */
        fun listProjectNames(directory: String): List<String> {
            return listProjectNames(File(directory))
        }

        /**
         * Retrieve a list of the available P3 project names from a directory.
         *
         * @param directory directory containing P3 files
         * @return list of project names
         */
        fun listProjectNames(directory: File): List<String> {
            val result = ArrayList<String>()

            val files = directory.listFiles(object : FilenameFilter() {
                @Override
                fun accept(dir: File, name: String): Boolean {
                    return name.toUpperCase().endsWith("STR.P3")
                }
            })

            if (files != null) {
                for (file in files!!) {
                    val fileName = file.getName()
                    val prefix = fileName.substring(0, fileName.length() - 6)
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

        private val PROJECT_FIELDS = HashMap<String, FieldType>()
        private val RESOURCE_FIELDS = HashMap<String, FieldType>()
        private val TASK_FIELDS = HashMap<String, FieldType>()

        init {
            defineField(PROJECT_FIELDS, "PROJECT_START_DATE", ProjectField.START_DATE)
            defineField(PROJECT_FIELDS, "PROJECT_FINISH_DATE", ProjectField.FINISH_DATE)
            defineField(PROJECT_FIELDS, "CURRENT_DATA_DATE", ProjectField.STATUS_DATE)
            defineField(PROJECT_FIELDS, "COMPANY_TITLE", ProjectField.COMPANY)
            defineField(PROJECT_FIELDS, "PROJECT_TITLE", ProjectField.NAME)

            defineField(RESOURCE_FIELDS, "RES_TITLE", ResourceField.NAME)
            defineField(RESOURCE_FIELDS, "RES_ID", ResourceField.CODE)

            defineField(TASK_FIELDS, "ACTIVITY_TITLE", TaskField.NAME)
            defineField(TASK_FIELDS, "ACTIVITY_ID", TaskField.TEXT1)
            defineField(TASK_FIELDS, "ORIGINAL_DURATION", TaskField.DURATION)
            defineField(TASK_FIELDS, "REMAINING_DURATION", TaskField.REMAINING_DURATION)
            defineField(TASK_FIELDS, "PERCENT_COMPLETE", TaskField.PERCENT_COMPLETE)
            defineField(TASK_FIELDS, "EARLY_START", TaskField.EARLY_START)
            defineField(TASK_FIELDS, "LATE_START", TaskField.LATE_START)
            defineField(TASK_FIELDS, "EARLY_FINISH", TaskField.EARLY_FINISH)
            defineField(TASK_FIELDS, "LATE_FINISH", TaskField.LATE_FINISH)
            defineField(TASK_FIELDS, "FREE_FLOAT", TaskField.FREE_SLACK)
            defineField(TASK_FIELDS, "TOTAL_FLOAT", TaskField.TOTAL_SLACK)
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
