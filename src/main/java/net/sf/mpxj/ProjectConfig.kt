/*
 * file:       ProjectConfig.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2015
 * date:       20/04/2015
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

import net.sf.mpxj.common.NumberHelper

/**
 * Container for configuration details used to control the behaviour of the ProjectFile class.
 */
class ProjectConfig
/**
 * Constructor.
 *
 * @param projectFile parent project
 */
(private val m_parent: ProjectFile) {

    /**
     * This method is used to retrieve the next unique ID for a task.
     *
     * @return next unique ID
     */
    val nextTaskUniqueID: Int
        get() = ++m_taskUniqueID

    /**
     * This method is used to retrieve the next unique ID for a calendar.
     *
     * @return next unique ID
     */
    val nextCalendarUniqueID: Int
        get() = ++m_calendarUniqueID

    /**
     * This method is used to retrieve the next unique ID for an assignment.
     *
     * @return next unique ID
     */
    internal val nextAssignmentUniqueID: Int
        get() = ++m_assignmentUniqueID

    /**
     * This method is used to retrieve the next ID for a task.
     *
     * @return next ID
     */
    val nextTaskID: Int
        get() = ++m_taskID

    /**
     * This method is used to retrieve the next unique ID for a resource.
     *
     * @return next unique ID
     */
    val nextResourceUniqueID: Int
        get() = ++m_resourceUniqueID

    /**
     * This method is used to retrieve the next ID for a resource.
     *
     * @return next ID
     */
    val nextResourceID: Int
        get() = ++m_resourceID

    /**
     * Indicating whether WBS value should be calculated on creation, or will
     * be manually set.
     */
    /**
     * Retrieve the flag that determines whether WBS is generated
     * automatically.
     *
     * @return boolean, default is false.
     */
    /**
     * Used to set whether WBS numbers are automatically created.
     *
     * @param flag true if automatic WBS required.
     */
    var autoWBS = true

    /**
     * Indicating whether the Outline Level value should be calculated on
     * creation, or will be manually set.
     */
    /**
     * Retrieve the flag that determines whether outline level is generated
     * automatically.
     *
     * @return boolean, default is false.
     */
    /**
     * Used to set whether outline level numbers are automatically created.
     *
     * @param flag true if automatic outline level required.
     */
    var autoOutlineLevel = true

    /**
     * Indicating whether the Outline Number value should be calculated on
     * creation, or will be manually set.
     */
    /**
     * Retrieve the flag that determines whether outline numbers are generated
     * automatically.
     *
     * @return boolean, default is false.
     */
    /**
     * Used to set whether outline numbers are automatically created.
     *
     * @param flag true if automatic outline number required.
     */
    var autoOutlineNumber = true

    /**
     * Indicating whether the unique ID of a task should be
     * calculated on creation, or will be manually set.
     */
    /**
     * Retrieve the flag that determines whether the task unique ID
     * is generated automatically.
     *
     * @return boolean, default is false.
     */
    /**
     * Used to set whether the task unique ID field is automatically populated.
     *
     * @param flag true if automatic unique ID required.
     */
    var autoTaskUniqueID = true

    /**
     * Indicating whether the unique ID of a calendar should be
     * calculated on creation, or will be manually set.
     */
    /**
     * Retrieve the flag that determines whether the calendar unique ID
     * is generated automatically.
     *
     * @return boolean, default is false.
     */
    /**
     * Used to set whether the calendar unique ID field is automatically populated.
     *
     * @param flag true if automatic unique ID required.
     */
    var autoCalendarUniqueID = true

    /**
     * Indicating whether the unique ID of an assignment should be
     * calculated on creation, or will be manually set.
     */
    /**
     * Retrieve the flag that determines whether the assignment unique ID
     * is generated automatically.
     *
     * @return boolean, default is true.
     */
    /**
     * Used to set whether the assignment unique ID field is automatically populated.
     *
     * @param flag true if automatic unique ID required.
     */
    var autoAssignmentUniqueID = true

    /**
     * Indicating whether the ID of a task should be
     * calculated on creation, or will be manually set.
     */
    /**
     * Retrieve the flag that determines whether the task ID
     * is generated automatically.
     *
     * @return boolean, default is false.
     */
    /**
     * Used to set whether the task ID field is automatically populated.
     *
     * @param flag true if automatic ID required.
     */
    var autoTaskID = true

    /**
     * Indicating whether the unique ID of a resource should be
     * calculated on creation, or will be manually set.
     */
    /**
     * Retrieve the flag that determines whether the resource unique ID
     * is generated automatically.
     *
     * @return boolean, default is false.
     */
    /**
     * Used to set whether the resource unique ID field is automatically populated.
     *
     * @param flag true if automatic unique ID required.
     */
    var autoResourceUniqueID = true

    /**
     * Indicating whether the ID of a resource should be
     * calculated on creation, or will be manually set.
     */
    /**
     * Retrieve the flag that determines whether the resource ID
     * is generated automatically.
     *
     * @return boolean, default is false.
     */
    /**
     * Used to set whether the resource ID field is automatically populated.
     *
     * @param flag true if automatic ID required.
     */
    var autoResourceID = true

    /**
     * Counter used to populate the unique ID field of a task.
     */
    private var m_taskUniqueID: Int = 0

    /**
     * Counter used to populate the unique ID field of a calendar.
     */
    private var m_calendarUniqueID: Int = 0

    /**
     * Counter used to populate the unique ID field of an assignment.
     */
    private var m_assignmentUniqueID: Int = 0

    /**
     * Counter used to populate the ID field of a task.
     */
    private var m_taskID: Int = 0

    /**
     * Counter used to populate the unique ID field of a resource.
     */
    private var m_resourceUniqueID: Int = 0

    /**
     * Counter used to populate the ID field of a resource.
     */
    private var m_resourceID: Int = 0

    /**
     * This method is called to ensure that after a project file has been
     * read, the cached unique ID values used to generate new unique IDs
     * start after the end of the existing set of unique IDs.
     */
    fun updateUniqueCounters() {
        //
        // Update task unique IDs
        //
        for (task in m_parent.tasks) {
            val uniqueID = NumberHelper.getInt(task.uniqueID)
            if (uniqueID > m_taskUniqueID) {
                m_taskUniqueID = uniqueID
            }
        }

        //
        // Update resource unique IDs
        //
        for (resource in m_parent.resources) {
            val uniqueID = NumberHelper.getInt(resource.uniqueID)
            if (uniqueID > m_resourceUniqueID) {
                m_resourceUniqueID = uniqueID
            }
        }

        //
        // Update calendar unique IDs
        //
        for (calendar in m_parent.calendars) {
            val uniqueID = NumberHelper.getInt(calendar.uniqueID)
            if (uniqueID > m_calendarUniqueID) {
                m_calendarUniqueID = uniqueID
            }
        }

        //
        // Update assignment unique IDs
        //
        for (assignment in m_parent.resourceAssignments) {
            val uniqueID = NumberHelper.getInt(assignment.uniqueID)
            if (uniqueID > m_assignmentUniqueID) {
                m_assignmentUniqueID = uniqueID
            }
        }
    }

}
