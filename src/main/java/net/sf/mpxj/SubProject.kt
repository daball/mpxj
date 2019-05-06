/*
 * file:       SubProject.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       May 23, 2005
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

/**
 * This class represents a sub project.
 */
class SubProject {

    /**
     * Retrieve the full path.
     *
     * @return full path
     */
    /**
     * Sets the full path.
     *
     * @param fullPath full path
     */
    var fullPath: String?
        get() = m_fullPath
        set(fullPath) {
            m_fullPath = fullPath
        }

    /**
     * Retrieves the offset applied to task unique IDs
     * from the sub project.
     *
     * @return unique ID offset
     */
    /**
     * Set the the offset applied to task unique IDs
     * from the sub project.
     *
     * @param uniqueIDOffset unique ID offset
     */
    var uniqueIDOffset: Integer?
        get() = m_uniqueIDOffset
        set(uniqueIDOffset) {
            m_uniqueIDOffset = uniqueIDOffset
        }

    /**
     * Retrieve the unique ID of the task to which this subproject
     * relates.
     *
     * @return task Unique ID
     */
    /**
     * Set the unique ID of the task to which this subproject relates.
     *
     * @param taskUniqueID task unique ID
     */
    var taskUniqueID: Integer?
        get() = m_taskUniqueID
        set(taskUniqueID) {
            m_taskUniqueID = taskUniqueID
        }

    /**
     * Retrieves all the external task unique ids for this project file.
     *
     * @return all sub project details
     */
    val allExternalTaskUniqueIDs: List<Integer>
        get() = m_externalTaskUniqueIDs

    private var m_taskUniqueID: Integer? = null
    private var m_uniqueIDOffset: Integer? = null
    private val m_externalTaskUniqueIDs = LinkedList<Integer>()
    /**
     * Retrieves the DOS full path.
     *
     * @return DOS full path
     */
    /**
     * Sets the DOS full path.
     *
     * @param dosFullPath DOS full path
     */
    var dosFullPath: String? = null
        get() = field
    private var m_fullPath: String? = null
    /**
     * Retrieves the DOS file name.
     *
     * @return DOS file name
     */
    /**
     * Sets the DOS file name.
     *
     * @param dosFileName DOS file name
     */
    var dosFileName: String? = null
        get() = field
    /**
     * Retrieve the file name.
     *
     * @return file name
     */
    /**
     * Sets the file name.
     *
     * @param fileName file name
     */
    var fileName: String? = null
        get() = field

    /**
     * Check to see if the given task is an external task from this subproject.
     *
     * @param taskUniqueID task unique ID
     * @return true if the task is external
     */
    fun isExternalTask(taskUniqueID: Integer): Boolean {
        return m_externalTaskUniqueIDs.contains(taskUniqueID)
    }

    /**
     * This package-private method is used to add external task unique id.
     *
     * @param externalTaskUniqueID external task unique id
     */
    fun addExternalTaskUniqueID(externalTaskUniqueID: Integer) {
        m_externalTaskUniqueIDs.add(externalTaskUniqueID)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return "[SubProject taskUniqueID=$m_taskUniqueID uniqueIDOffset=$m_uniqueIDOffset path=$m_fullPath]"
    }
}
