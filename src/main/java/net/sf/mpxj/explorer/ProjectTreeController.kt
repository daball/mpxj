/*
 * file:       ProjectTreeController.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       06/07/2014
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

package net.sf.mpxj.explorer

import java.io.File
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet

import net.sf.mpxj.ChildTaskContainer
import net.sf.mpxj.Column
import net.sf.mpxj.CustomField
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.FieldType
import net.sf.mpxj.Filter
import net.sf.mpxj.Group
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarDateRanges
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.Table
import net.sf.mpxj.Task
import net.sf.mpxj.View
import net.sf.mpxj.json.JsonWriter
import net.sf.mpxj.mpx.MPXWriter
import net.sf.mpxj.mspdi.MSPDIWriter
import net.sf.mpxj.planner.PlannerWriter
import net.sf.mpxj.primavera.PrimaveraPMFileWriter
import net.sf.mpxj.reader.UniversalProjectReader
import net.sf.mpxj.sdef.SDEFWriter
import net.sf.mpxj.writer.ProjectWriter

/**
 * Implements the controller component of the ProjectTree MVC.
 */
class ProjectTreeController
/**
 * Constructor.
 *
 * @param model PoiTree model
 */
(private val m_model: ProjectTreeModel) {

    internal val m_timeFormat = SimpleDateFormat("HH:mm")
    internal val m_dateFormat = SimpleDateFormat("yyyy-MM-dd")
    private var m_projectFile: ProjectFile? = null

    /**
     * Command to load a file.
     *
     * @param file file to load
     */
    fun loadFile(file: File) {
        try {
            m_projectFile = UniversalProjectReader().read(file)
            if (m_projectFile == null) {
                throw IllegalArgumentException("Unsupported file type")
            }
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

        val projectNode = object : MpxjTreeNode(m_projectFile, FILE_EXCLUDED_METHODS) {
            @Override
            fun toString(): String {
                return "Project"
            }
        }

        val configNode = object : MpxjTreeNode(m_projectFile!!.projectConfig) {
            @Override
            fun toString(): String {
                return "MPXJ Configuration"
            }
        }
        projectNode.add(configNode)

        val propertiesNode = object : MpxjTreeNode(m_projectFile!!.projectProperties) {
            @Override
            fun toString(): String {
                return "Properties"
            }
        }
        projectNode.add(propertiesNode)

        val tasksFolder = MpxjTreeNode("Tasks")
        projectNode.add(tasksFolder)
        addTasks(tasksFolder, m_projectFile!!)

        val resourcesFolder = MpxjTreeNode("Resources")
        projectNode.add(resourcesFolder)
        addResources(resourcesFolder, m_projectFile!!)

        val assignmentsFolder = MpxjTreeNode("Assignments")
        projectNode.add(assignmentsFolder)
        addAssignments(assignmentsFolder, m_projectFile!!)

        val calendarsFolder = MpxjTreeNode("Calendars")
        projectNode.add(calendarsFolder)
        addCalendars(calendarsFolder, m_projectFile!!)

        val groupsFolder = MpxjTreeNode("Groups")
        projectNode.add(groupsFolder)
        addGroups(groupsFolder, m_projectFile!!)

        val customFieldsFolder = MpxjTreeNode("Custom Fields")
        projectNode.add(customFieldsFolder)
        addCustomFields(customFieldsFolder, m_projectFile!!)

        val filtersFolder = MpxjTreeNode("Filters")
        projectNode.add(filtersFolder)

        val taskFiltersFolder = MpxjTreeNode("Task Filters")
        filtersFolder.add(taskFiltersFolder)
        addFilters(taskFiltersFolder, m_projectFile!!.filters.getTaskFilters())

        val resourceFiltersFolder = MpxjTreeNode("Resource Filters")
        filtersFolder.add(resourceFiltersFolder)
        addFilters(resourceFiltersFolder, m_projectFile!!.filters.getResourceFilters())

        val viewsFolder = MpxjTreeNode("Views")
        projectNode.add(viewsFolder)
        addViews(viewsFolder, m_projectFile!!)

        val tablesFolder = MpxjTreeNode("Tables")
        projectNode.add(tablesFolder)
        addTables(tablesFolder, m_projectFile!!)

        m_model.setRoot(projectNode)
    }

    /**
     * Add tasks to the tree.
     *
     * @param parentNode parent tree node
     * @param parent parent task container
     */
    private fun addTasks(parentNode: MpxjTreeNode, parent: ChildTaskContainer) {
        for (task in parent.getChildTasks()) {
            val childNode = object : MpxjTreeNode(task, TASK_EXCLUDED_METHODS) {
                @Override
                fun toString(): String {
                    return task.name
                }
            }
            parentNode.add(childNode)
            addTasks(childNode, task)
        }
    }

    /**
     * Add resources to the tree.
     *
     * @param parentNode parent tree node
     * @param file resource container
     */
    private fun addResources(parentNode: MpxjTreeNode, file: ProjectFile) {
        for (resource in file.resources) {
            val childNode = object : MpxjTreeNode(resource) {
                @Override
                fun toString(): String {
                    return resource.name
                }
            }
            parentNode.add(childNode)
        }
    }

    /**
     * Add calendars to the tree.
     *
     * @param parentNode parent tree node
     * @param file calendar container
     */
    private fun addCalendars(parentNode: MpxjTreeNode, file: ProjectFile) {
        for (calendar in file.calendars) {
            addCalendar(parentNode, calendar)
        }
    }

    /**
     * Add a calendar node.
     *
     * @param parentNode parent node
     * @param calendar calendar
     */
    private fun addCalendar(parentNode: MpxjTreeNode, calendar: ProjectCalendar) {
        val calendarNode = object : MpxjTreeNode(calendar, CALENDAR_EXCLUDED_METHODS) {
            @Override
            fun toString(): String? {
                return calendar.name
            }
        }
        parentNode.add(calendarNode)

        val daysFolder = MpxjTreeNode("Days")
        calendarNode.add(daysFolder)

        for (day in Day.values()) {
            addCalendarDay(daysFolder, calendar, day)
        }

        val exceptionsFolder = MpxjTreeNode("Exceptions")
        calendarNode.add(exceptionsFolder)

        for (exception in calendar.calendarExceptions) {
            addCalendarException(exceptionsFolder, exception)
        }
    }

    /**
     * Add a calendar day node.
     *
     * @param parentNode parent node
     * @param calendar ProjectCalendar instance
     * @param day calendar day
     */
    private fun addCalendarDay(parentNode: MpxjTreeNode, calendar: ProjectCalendar, day: Day) {
        val dayNode = object : MpxjTreeNode(day) {
            @Override
            fun toString(): String {
                return day.name()
            }
        }
        parentNode.add(dayNode)
        addHours(dayNode, calendar.getHours(day)!!)
    }

    /**
     * Add hours to a parent object.
     *
     * @param parentNode parent node
     * @param hours list of ranges
     */
    private fun addHours(parentNode: MpxjTreeNode, hours: ProjectCalendarDateRanges) {
        for (range in hours) {
            val rangeNode = object : MpxjTreeNode(range) {
                @Override
                fun toString(): String {
                    return m_timeFormat.format(range.getStart()) + " - " + m_timeFormat.format(range.getEnd())
                }
            }
            parentNode.add(rangeNode)
        }
    }

    /**
     * Add an exception to a calendar.
     *
     * @param parentNode parent node
     * @param exception calendar exceptions
     */
    private fun addCalendarException(parentNode: MpxjTreeNode, exception: ProjectCalendarException) {
        val exceptionNode = object : MpxjTreeNode(exception, CALENDAR_EXCEPTION_EXCLUDED_METHODS) {
            @Override
            fun toString(): String {
                return m_dateFormat.format(exception.fromDate)
            }
        }
        parentNode.add(exceptionNode)
        addHours(exceptionNode, exception)
    }

    /**
     * Add groups to the tree.
     *
     * @param parentNode parent tree node
     * @param file group container
     */
    private fun addGroups(parentNode: MpxjTreeNode, file: ProjectFile) {
        for (group in file.groups) {
            val childNode = object : MpxjTreeNode(group) {
                @Override
                fun toString(): String {
                    return group.name
                }
            }
            parentNode.add(childNode)
        }
    }

    /**
     * Add custom fields to the tree.
     *
     * @param parentNode parent tree node
     * @param file custom fields container
     */
    private fun addCustomFields(parentNode: MpxjTreeNode, file: ProjectFile) {
        for (field in file.customFields) {
            val childNode = object : MpxjTreeNode(field) {
                @Override
                fun toString(): String {
                    val type = field.getFieldType()

                    return if (type == null) "(unknown)" else type!!.getFieldTypeClass() + "." + type!!.toString()
                }
            }
            parentNode.add(childNode)
        }
    }

    /**
     * Add views to the tree.
     *
     * @param parentNode parent tree node
     * @param file views container
     */
    private fun addViews(parentNode: MpxjTreeNode, file: ProjectFile) {
        for (view in file.views) {
            val childNode = object : MpxjTreeNode(view) {
                @Override
                fun toString(): String {
                    return view.getName()
                }
            }
            parentNode.add(childNode)
        }
    }

    /**
     * Add tables to the tree.
     *
     * @param parentNode parent tree node
     * @param file tables container
     */
    private fun addTables(parentNode: MpxjTreeNode, file: ProjectFile) {
        for (table in file.tables) {
            val childNode = object : MpxjTreeNode(table, TABLE_EXCLUDED_METHODS) {
                @Override
                fun toString(): String? {
                    return table.name
                }
            }
            parentNode.add(childNode)

            addColumns(childNode, table)
        }
    }

    /**
     * Add columns to the tree.
     *
     * @param parentNode parent tree node
     * @param table columns container
     */
    private fun addColumns(parentNode: MpxjTreeNode, table: Table) {
        for (column in table.columns) {
            val childNode = object : MpxjTreeNode(column) {
                @Override
                fun toString(): String {
                    return column.getTitle()
                }
            }
            parentNode.add(childNode)
        }
    }

    /**
     * Add filters to the tree.
     *
     * @param parentNode parent tree node
     * @param filters list of filters
     */
    private fun addFilters(parentNode: MpxjTreeNode, filters: List<Filter>) {
        for (field in filters) {
            val childNode = object : MpxjTreeNode(field) {
                @Override
                fun toString(): String {
                    return field.getName()
                }
            }
            parentNode.add(childNode)
        }
    }

    /**
     * Add assignments to the tree.
     *
     * @param parentNode parent tree node
     * @param file assignments container
     */
    private fun addAssignments(parentNode: MpxjTreeNode, file: ProjectFile) {
        for (assignment in file.resourceAssignments) {
            val childNode = object : MpxjTreeNode(assignment) {
                @Override
                fun toString(): String {
                    val resource = assignment.resource
                    val resourceName = if (resource == null) "(unknown resource)" else resource!!.name
                    val task = assignment.task
                    val taskName = if (task == null) "(unknown task)" else task!!.name
                    return "$resourceName->$taskName"
                }
            }
            parentNode.add(childNode)
        }
    }

    /**
     * Save the current file as the given type.
     *
     * @param file target file
     * @param type file type
     */
    fun saveFile(file: File, type: String) {
        try {
            val fileClass = WRITER_MAP.get(type) ?: throw IllegalArgumentException("Cannot write files of type: $type")

            val writer = fileClass.newInstance()
            writer.write(m_projectFile, file)
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

    }

    companion object {
        private val WRITER_MAP = HashMap<String, Class<out ProjectWriter>>()

        init {
            WRITER_MAP.put("MPX", MPXWriter::class.java)
            WRITER_MAP.put("MSPDI", MSPDIWriter::class.java)
            WRITER_MAP.put("PMXML", PrimaveraPMFileWriter::class.java)
            WRITER_MAP.put("PLANNER", PlannerWriter::class.java)
            WRITER_MAP.put("JSON", JsonWriter::class.java)
            WRITER_MAP.put("SDEF", SDEFWriter::class.java)
        }

        private val FILE_EXCLUDED_METHODS = excludedMethods("getAllResourceAssignments", "getAllResources", "getAllTasks", "getChildTasks", "getCalendars", "getCustomFields", "getEventManager", "getFilters", "getGroups", "getProjectProperties", "getProjectConfig", "getViews", "getTables")
        private val CALENDAR_EXCLUDED_METHODS = excludedMethods("getCalendarExceptions")
        private val TASK_EXCLUDED_METHODS = excludedMethods("getChildTasks", "getEffectiveCalendar", "getParentTask", "getResourceAssignments")
        private val CALENDAR_EXCEPTION_EXCLUDED_METHODS = excludedMethods("getRange")
        private val TABLE_EXCLUDED_METHODS = excludedMethods("getColumns")

        /**
         * Generates a set of excluded method names.
         *
         * @param methodNames method names
         * @return set of method names
         */
        private fun excludedMethods(vararg methodNames: String): Set<String> {
            val set = HashSet<String>(MpxjTreeNode.DEFAULT_EXCLUDED_METHODS)
            set.addAll(Arrays.asList(methodNames))
            return set
        }
    }
}
