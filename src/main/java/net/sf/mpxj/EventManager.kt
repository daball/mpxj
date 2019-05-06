/*
 * file:       EventManager.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2015
 * date:       27/04/2015
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

import java.util.LinkedList

import net.sf.mpxj.listener.ProjectListener

/**
 * Provides subscriptions to events raised when project files are written and read.
 */
class EventManager {

    /**
     * List of project event listeners.
     */
    private var m_projectListeners: List<ProjectListener>? = null

    /**
     * This method is called to alert project listeners to the fact that
     * a task has been read from a project file.
     *
     * @param task task instance
     */
    fun fireTaskReadEvent(task: Task) {
        if (m_projectListeners != null) {
            for (listener in m_projectListeners!!) {
                listener.taskRead(task)
            }
        }
    }

    /**
     * This method is called to alert project listeners to the fact that
     * a task has been written to a project file.
     *
     * @param task task instance
     */
    fun fireTaskWrittenEvent(task: Task) {
        if (m_projectListeners != null) {
            for (listener in m_projectListeners!!) {
                listener.taskWritten(task)
            }
        }
    }

    /**
     * This method is called to alert project listeners to the fact that
     * a resource has been read from a project file.
     *
     * @param resource resource instance
     */
    fun fireResourceReadEvent(resource: Resource) {
        if (m_projectListeners != null) {
            for (listener in m_projectListeners!!) {
                listener.resourceRead(resource)
            }
        }
    }

    /**
     * This method is called to alert project listeners to the fact that
     * a resource has been written to a project file.
     *
     * @param resource resource instance
     */
    fun fireResourceWrittenEvent(resource: Resource) {
        if (m_projectListeners != null) {
            for (listener in m_projectListeners!!) {
                listener.resourceWritten(resource)
            }
        }
    }

    /**
     * This method is called to alert project listeners to the fact that
     * a calendar has been read from a project file.
     *
     * @param calendar calendar instance
     */
    fun fireCalendarReadEvent(calendar: ProjectCalendar) {
        if (m_projectListeners != null) {
            for (listener in m_projectListeners!!) {
                listener.calendarRead(calendar)
            }
        }
    }

    /**
     * This method is called to alert project listeners to the fact that
     * a resource assignment has been read from a project file.
     *
     * @param resourceAssignment resourceAssignment instance
     */
    fun fireAssignmentReadEvent(resourceAssignment: ResourceAssignment) {
        if (m_projectListeners != null) {
            for (listener in m_projectListeners!!) {
                listener.assignmentRead(resourceAssignment)
            }
        }
    }

    /**
     * This method is called to alert project listeners to the fact that
     * a resource assignment has been written to a project file.
     *
     * @param resourceAssignment resourceAssignment instance
     */
    fun fireAssignmentWrittenEvent(resourceAssignment: ResourceAssignment) {
        if (m_projectListeners != null) {
            for (listener in m_projectListeners!!) {
                listener.assignmentWritten(resourceAssignment)
            }
        }
    }

    /**
     * This method is called to alert project listeners to the fact that
     * a relation has been read from a project file.
     *
     * @param relation relation instance
     */
    fun fireRelationReadEvent(relation: Relation) {
        if (m_projectListeners != null) {
            for (listener in m_projectListeners!!) {
                listener.relationRead(relation)
            }
        }
    }

    /**
     * This method is called to alert project listeners to the fact that
     * a relation has been written to a project file.
     *
     * @param relation relation instance
     */
    fun fireRelationWrittenEvent(relation: Relation) {
        if (m_projectListeners != null) {
            for (listener in m_projectListeners!!) {
                listener.relationWritten(relation)
            }
        }
    }

    /**
     * This method is called to alert project listeners to the fact that
     * a calendar has been written to a project file.
     *
     * @param calendar calendar instance
     */
    fun fireCalendarWrittenEvent(calendar: ProjectCalendar) {
        if (m_projectListeners != null) {
            for (listener in m_projectListeners!!) {
                listener.calendarWritten(calendar)
            }
        }
    }

    /**
     * Adds a listener to this project file.
     *
     * @param listener listener instance
     */
    fun addProjectListener(listener: ProjectListener) {
        if (m_projectListeners == null) {
            m_projectListeners = LinkedList<ProjectListener>()
        }
        m_projectListeners!!.add(listener)
    }

    /**
     * Adds a collection of listeners to the current project.
     *
     * @param listeners collection of listeners
     */
    fun addProjectListeners(listeners: List<ProjectListener>?) {
        if (listeners != null) {
            for (listener in listeners) {
                addProjectListener(listener)
            }
        }
    }

    /**
     * Removes a listener from this project file.
     *
     * @param listener listener instance
     */
    fun removeProjectListener(listener: ProjectListener) {
        if (m_projectListeners != null) {
            m_projectListeners!!.remove(listener)
        }
    }
}
