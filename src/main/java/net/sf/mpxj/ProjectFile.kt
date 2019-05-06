/*
 * file:       ProjectFile.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2006
 * date:       15/08/2002
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

package net.sf.mpxj

import java.util.Date
import java.util.LinkedList

import net.sf.mpxj.common.NumberHelper

/**
 * This class represents a project plan.
 */
class ProjectFile : ChildTaskContainer {

    /**
     * Find the earliest task start date. We treat this as the
     * start date for the project.
     *
     * @return start date
     */
    //
    // If a hidden "summary" task is present we ignore it
    //
    //
    // Select the actual or forecast start date. Note that the
    // behaviour is different for milestones. The milestone end date
    // is always correct, the milestone start date may be different
    // to reflect a missed deadline.
    //
    val startDate: Date?
        get() {
            var startDate: Date? = null

            for (task in allTasks) {
                if (NumberHelper.getInt(task.uniqueID) == 0) {
                    continue
                }
                var taskStartDate: Date?
                if (task.milestone == true) {
                    taskStartDate = task.actualFinish
                    if (taskStartDate == null) {
                        taskStartDate = task.finish
                    }
                } else {
                    taskStartDate = task.actualStart
                    if (taskStartDate == null) {
                        taskStartDate = task.start
                    }
                }

                if (taskStartDate != null) {
                    if (startDate == null) {
                        startDate = taskStartDate
                    } else {
                        if (taskStartDate!!.getTime() < startDate!!.getTime()) {
                            startDate = taskStartDate
                        }
                    }
                }
            }

            return startDate
        }

    /**
     * Find the latest task finish date. We treat this as the
     * finish date for the project.
     *
     * @return finish date
     */
    //
    // If a hidden "summary" task is present we ignore it
    //
    //
    // Select the actual or forecast start date
    //
    val finishDate: Date?
        get() {
            var finishDate: Date? = null

            for (task in allTasks) {
                if (NumberHelper.getInt(task.uniqueID) == 0) {
                    continue
                }
                var taskFinishDate: Date?
                taskFinishDate = task.actualFinish
                if (taskFinishDate == null) {
                    taskFinishDate = task.finish
                }

                if (taskFinishDate != null) {
                    if (finishDate == null) {
                        finishDate = taskFinishDate
                    } else {
                        if (taskFinishDate!!.getTime() > finishDate!!.getTime()) {
                            finishDate = taskFinishDate
                        }
                    }
                }
            }

            return finishDate
        }

    /**
     * Retrieves the default calendar for this project based on the calendar name
     * given in the project properties. If a calendar of this name cannot be found, then
     * the first calendar listed for the project will be returned. If the
     * project contains no calendars, then a default calendar is added.
     *
     * @return default projectCalendar instance
     */
    /**
     * Sets the default calendar for this project.
     *
     * @param calendar default calendar instance
     */
    var defaultCalendar: ProjectCalendar?
        get() {
            val calendarName = projectProperties.defaultCalendarName
            var calendar = getCalendarByName(calendarName)
            if (calendar == null) {
                if (calendars.isEmpty()) {
                    calendar = addDefaultBaseCalendar()
                } else {
                    calendar = calendars.get(0)
                }
            }
            return calendar
        }
        set(calendar) {
            projectProperties.defaultCalendarName = calendar.name
        }

    /**
     * Retrieve the calendar used internally for timephased baseline calculation.
     *
     * @return baseline calendar
     */
    //
    // Attempt to locate the calendar normally used by baselines
    // If this isn't present, fall back to using the default
    // project calendar.
    //
    val baselineCalendar: ProjectCalendar
        get() {
            var result = getCalendarByName("Used for Microsoft Project 98 Baseline Calendar")
            if (result == null) {
                result = defaultCalendar
            }
            return result
        }

    /**
     * Retrieve project configuration data.
     *
     * @return ProjectConfig instance.
     */
    val projectConfig = ProjectConfig(this)
    /**
     * This method is used to retrieve the project properties.
     *
     * @return project properties
     */
    val projectProperties = ProjectProperties(this)
    /**
     * Retrieves a list of all resources in this project.
     *
     * @return list of all resources
     */
    @get:Deprecated("Use getResources()")
    val allResources = ResourceContainer(this)
    /**
     * This method is used to retrieve a list of all of the tasks
     * that are defined in this project file.
     *
     * @return list of all tasks
     */
    @get:Deprecated("Use getTasks()")
    val allTasks = TaskContainer(this)
    /**
     * This method is used to retrieve a list of all of the top level tasks
     * that are defined in this project file.
     *
     * @return list of tasks
     */
    @get:Override
    val childTasks: List<Task> = LinkedList<Task>()
    /**
     * Retrieves a list of all resource assignments in this project.
     *
     * @return list of all resources
     */
    @get:Deprecated("Use getResourceAssignments")
    val allResourceAssignments = ResourceAssignmentContainer(this)
    /**
     * This method retrieves the list of calendars defined in
     * this file.
     *
     * @return list of calendars
     */
    val calendars = ProjectCalendarContainer(this)
    /**
     * This method returns the tables defined in an MPP file.
     *
     * @return list of tables
     */
    val tables = TableContainer()
    /**
     * This method returns the filters defined in an MPP file.
     *
     * @return filters
     */
    val filters = FilterContainer()
    /**
     * Retrieves a list of all groups.
     *
     * @return list of all groups
     */
    val groups = GroupContainer()
    /**
     * Retrieves all the subprojects for this project.
     *
     * @return all sub project details
     */
    val subProjects = SubProjectContainer()
    /**
     * This method returns a list of the views defined in this MPP file.
     *
     * @return list of views
     */
    val views = ViewContainer()
    /**
     * Retrieve the event manager for this project.
     *
     * @return event manager
     */
    val eventManager = EventManager()
    /**
     * Retrieves the custom field configuration for this project.
     *
     * @return custom field configuration
     */
    val customFields = CustomFieldContainer()
    /**
     * Retrieves the activity code configuration for this project.
     *
     * @return custom field configuration
     */
    val activityCodes = ActivityCodeContainer()

    /**
     * This method allows a task to be added to the file programmatically.
     *
     * @return new task object
     */
    @Override
    fun addTask(): Task {
        return allTasks.add()
    }

    /**
     * This method is used to remove a task from the project.
     *
     * @param task task to be removed
     */
    fun removeTask(task: Task) {
        allTasks.remove(task.toInt())
    }

    /**
     * This method can be called to ensure that the IDs of all
     * tasks in this project are sequential, and start from an
     * appropriate point. If tasks are added to and removed from
     * the list of tasks, then the project is loaded into Microsoft
     * project, if the ID values have gaps in the sequence, there will
     * be blank task rows shown.
     *
     */
    @Deprecated("Use getTasks().renumberIDs()")
    fun renumberTaskIDs() {
        allTasks.renumberIDs()
    }

    /**
     * This method can be called to ensure that the IDs of all
     * resources in this project are sequential, and start from an
     * appropriate point. If resources are added to and removed from
     * the list of resources, then the project is loaded into Microsoft
     * project, if the ID values have gaps in the sequence, there will
     * be blank resource rows shown.
     *
     */
    @Deprecated("Use getResources().renumberIDs()")
    fun renumberResourceIDs() {
        allResources.renumberIDs()
    }

    /**
     * This method is called to ensure that all unique ID values
     * held by MPXJ are within the range supported by MS Project.
     * If any of these values fall outside of this range, the unique IDs
     * of the relevant entities are renumbered.
     */
    fun validateUniqueIDsForMicrosoftProject() {
        allTasks.validateUniqueIDsForMicrosoftProject()
        allResources.validateUniqueIDsForMicrosoftProject()
        allResourceAssignments.validateUniqueIDsForMicrosoftProject()
        calendars.validateUniqueIDsForMicrosoftProject()
    }

    /**
     * Microsoft Project bases the order of tasks displayed on their ID
     * value. This method takes the hierarchical structure of tasks
     * represented in MPXJ and renumbers the ID values to ensure that
     * this structure is displayed as expected in Microsoft Project. This
     * is typically used to deal with the case where a hierarchical task
     * structure has been created programmatically in MPXJ.
     *
     */
    @Deprecated("Use getTasks().synchronizeTaskIDToHierarchy()")
    fun synchronizeTaskIDToHierarchy() {
        allTasks.synchronizeTaskIDToHierarchy()
    }

    /**
     * This method is used to retrieve a list of all of the tasks
     * that are defined in this project file.
     *
     * @return list of all tasks
     */
    fun getTasks(): TaskContainer {
        return allTasks
    }

    /**
     * This method is used to add a new calendar to the file.
     *
     * @return new calendar object
     */
    fun addCalendar(): ProjectCalendar {
        return calendars.add()
    }

    /**
     * Removes a calendar.
     *
     * @param calendar calendar to be removed
     */
    fun removeCalendar(calendar: ProjectCalendar) {
        calendars.remove(calendar.toInt())
    }

    /**
     * This is a convenience method used to add a calendar called
     * "Standard" to the file, and populate it with a default working week
     * and default working hours.
     *
     * @return a new default calendar
     */
    fun addDefaultBaseCalendar(): ProjectCalendar {
        return calendars.addDefaultBaseCalendar()
    }

    /**
     * This is a convenience method to add a default derived
     * calendar.
     *
     * @return new ProjectCalendar instance
     */
    fun addDefaultDerivedCalendar(): ProjectCalendar {
        return calendars.addDefaultDerivedCalendar()
    }

    /**
     * This method is used to add a new resource to the file.
     *
     * @return new resource object
     */
    fun addResource(): Resource {
        return allResources.add()
    }

    /**
     * This method is used to remove a resource from the project.
     *
     * @param resource resource to be removed
     */
    fun removeResource(resource: Resource) {
        allResources.remove(resource.toInt())
    }

    /**
     * Retrieves a list of all resources in this project.
     *
     * @return list of all resources
     */
    fun getResources(): ResourceContainer {
        return allResources
    }

    /**
     * Retrieves a list of all resource assignments in this project.
     *
     * @return list of all resources
     */
    fun getResourceAssignments(): ResourceAssignmentContainer {
        return allResourceAssignments
    }

    /**
     * This method has been provided to allow the subclasses to
     * instantiate ResourecAssignment instances.
     *
     * @param task parent task
     * @return new resource assignment instance
     */
    @Deprecated("Use Task.addResourceAssignment(resource) instead")
    fun newResourceAssignment(task: Task): ResourceAssignment {
        return ResourceAssignment(this, task)
    }

    /**
     * Retrieves the named calendar. This method will return
     * null if the named calendar is not located.
     *
     * @param calendarName name of the required calendar
     * @return ProjectCalendar instance
     */
    fun getCalendarByName(calendarName: String): ProjectCalendar? {
        return calendars.getByName(calendarName)
    }

    /**
     * Retrieves the calendar referred to by the supplied unique ID
     * value. This method will return null if the required calendar is not
     * located.
     *
     * @param calendarID calendar unique ID
     * @return ProjectCalendar instance
     */
    fun getCalendarByUniqueID(calendarID: Integer): ProjectCalendar {
        return calendars.getByUniqueID(calendarID)
    }

    /**
     * This method is used to calculate the duration of work between two fixed
     * dates according to the work schedule defined in the named calendar. The
     * calendar used is the "Standard" calendar. If this calendar does not exist,
     * and exception will be thrown.
     *
     * @param startDate start of the period
     * @param endDate end of the period
     * @return new Duration object
     * @throws MPXJException normally when no Standard calendar is available
     */
    @Deprecated("use calendar.getDuration(startDate, endDate)")
    @Throws(MPXJException::class)
    fun getDuration(startDate: Date, endDate: Date): Duration {
        return getDuration("Standard", startDate, endDate)
    }

    /**
     * This method is used to calculate the duration of work between two fixed
     * dates according to the work schedule defined in the named calendar.
     * The name of the calendar to be used is passed as an argument.
     *
     * @param calendarName name of the calendar to use
     * @param startDate start of the period
     * @param endDate end of the period
     * @return new Duration object
     * @throws MPXJException normally when no Standard calendar is available
     */
    @Deprecated("use calendar.getDuration(startDate, endDate)")
    @Throws(MPXJException::class)
    fun getDuration(calendarName: String, startDate: Date, endDate: Date): Duration {
        val calendar = getCalendarByName(calendarName)
                ?: throw MPXJException(MPXJException.CALENDAR_ERROR.toString() + ": " + calendarName)

        return calendar.getDuration(startDate, endDate)
    }

    /**
     * This method allows an arbitrary task to be retrieved based
     * on its ID field.
     *
     * @param id task identified
     * @return the requested task, or null if not found
     */
    fun getTaskByID(id: Integer): Task {
        return allTasks.getByID(id)
    }

    /**
     * This method allows an arbitrary task to be retrieved based
     * on its UniqueID field.
     *
     * @param id task identified
     * @return the requested task, or null if not found
     */
    fun getTaskByUniqueID(id: Integer): Task {
        return allTasks.getByUniqueID(id)
    }

    /**
     * This method allows an arbitrary resource to be retrieved based
     * on its ID field.
     *
     * @param id resource identified
     * @return the requested resource, or null if not found
     */
    fun getResourceByID(id: Integer): Resource {
        return allResources.getByID(id)
    }

    /**
     * This method allows an arbitrary resource to be retrieved based
     * on its UniqueID field.
     *
     * @param id resource identified
     * @return the requested resource, or null if not found
     */
    fun getResourceByUniqueID(id: Integer): Resource {
        return allResources.getByUniqueID(id)
    }

    /**
     * This method is used to recreate the hierarchical structure of the
     * project file from scratch. The method sorts the list of all tasks,
     * then iterates through it creating the parent-child structure defined
     * by the outline level field.
     */
    fun updateStructure() {
        allTasks.updateStructure()
    }
}
