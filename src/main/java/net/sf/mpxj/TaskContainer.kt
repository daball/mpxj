/*
 * file:       TaskContainer.java
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

import java.util.Collections

import net.sf.mpxj.common.NumberHelper

/**
 * Manages the collection of tasks belonging to a project.
 */
class TaskContainer
/**
 * Constructor.
 *
 * @param projectFile parent project
 */
(projectFile: ProjectFile) : ProjectEntityWithIDContainer<Task>(projectFile) {

    /**
     * Add a task to the project.
     *
     * @return new task instance
     */
    fun add(): Task {
        val task = Task(m_projectFile, null as Task?)
        add(task)
        m_projectFile.childTasks.add(task)
        return task
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public override fun removed(task: Task) {
        //
        // Remove the task from the file and its parent task
        //
        m_uniqueIDMap.remove(task.uniqueID)
        m_idMap.remove(task.id)

        val parentTask = task.parentTask
        if (parentTask != null) {
            parentTask.removeChildTask(task)
        } else {
            m_projectFile.childTasks.remove(task)
        }

        //
        // Remove all resource assignments
        //
        val iter = m_projectFile.resourceAssignments.iterator()
        while (iter.hasNext() === true) {
            val assignment = iter.next()
            if (assignment.task == task) {
                val resource = assignment.resource
                if (resource != null) {
                    resource!!.removeResourceAssignment(assignment)
                }
                iter.remove()
            }
        }

        //
        // Recursively remove any child tasks
        //
        while (true) {
            val childTaskList = task.childTasks
            if (childTaskList.isEmpty() === true) {
                break
            }

            remove(childTaskList.get(0))
        }
    }

    /**
     * Microsoft Project bases the order of tasks displayed on their ID
     * value. This method takes the hierarchical structure of tasks
     * represented in MPXJ and renumbers the ID values to ensure that
     * this structure is displayed as expected in Microsoft Project. This
     * is typically used to deal with the case where a hierarchical task
     * structure has been created programmatically in MPXJ.
     */
    fun synchronizeTaskIDToHierarchy() {
        clear()

        var currentID = if (getByID(Integer.valueOf(0)) == null) 1 else 0
        for (task in m_projectFile.childTasks) {
            task.id = Integer.valueOf(currentID++)
            add(task)
            currentID = synchroizeTaskIDToHierarchy(task, currentID)
        }
    }

    /**
     * Called recursively to renumber child task IDs.
     *
     * @param parentTask parent task instance
     * @param currentID current task ID
     * @return updated current task ID
     */
    private fun synchroizeTaskIDToHierarchy(parentTask: Task, currentID: Int): Int {
        var currentID = currentID
        for (task in parentTask.childTasks) {
            task.id = Integer.valueOf(currentID++)
            add(task)
            currentID = synchroizeTaskIDToHierarchy(task, currentID)
        }
        return currentID
    }

    /**
     * This method is used to recreate the hierarchical structure of the
     * project file from scratch. The method sorts the list of all tasks,
     * then iterates through it creating the parent-child structure defined
     * by the outline level field.
     */
    fun updateStructure() {
        if (size() > 1) {
            Collections.sort(this)
            m_projectFile.childTasks.clear()

            var lastTask: Task? = null
            var lastLevel = -1
            val autoWbs = m_projectFile.projectConfig.autoWBS
            val autoOutlineNumber = m_projectFile.projectConfig.autoOutlineNumber

            for (task in this) {
                task.clearChildTasks()
                var parent: Task? = null
                if (!task.`null`) {
                    var level = NumberHelper.getInt(task.outlineLevel)

                    if (lastTask != null) {
                        if (level == lastLevel || task.`null`) {
                            parent = lastTask.parentTask
                            level = lastLevel
                        } else {
                            if (level > lastLevel) {
                                parent = lastTask
                            } else {
                                while (level <= lastLevel) {
                                    parent = lastTask!!.parentTask

                                    if (parent == null) {
                                        break
                                    }

                                    lastLevel = NumberHelper.getInt(parent.outlineLevel)
                                    lastTask = parent
                                }
                            }
                        }
                    }

                    lastTask = task
                    lastLevel = level

                    if (autoWbs || task.wbs == null) {
                        task.generateWBS(parent)
                    }

                    if (autoOutlineNumber) {
                        task.generateOutlineNumber(parent)
                    }
                }

                if (parent == null) {
                    m_projectFile.childTasks.add(task)
                } else {
                    parent.addChildTask(task)
                }
            }
        }
    }

    @Override
    override fun firstUniqueID(): Int {
        val firstEntity = getByID(Integer.valueOf(0))
        return if (firstEntity == null) 1 else 0
    }
}
