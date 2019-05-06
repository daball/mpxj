/*
 * file:       ResourceAssignmentWorkgroupFields.java
 * author:     Scott Melville
 *             Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
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

/**
 * This class represents a resource assignment workgroup fields record
 * from an MPX file.
 */
class ResourceAssignmentWorkgroupFields {

    /**
     * Gets the Message Unique ID.
     *
     * @return ID
     */
    /**
     * Sets the Message Unique ID.
     *
     * @param val ID
     */
    var messageUniqueID: String? = null
        get() = field
    /**
     * Gets confirmed flag.
     *
     * @return boolean value
     */
    /**
     * Sets confirmed flag.
     *
     * @param val boolean flag
     */
    var confirmed: Boolean = false
        get() = field
    /**
     * Retrieves response pending flag.
     *
     * @return boolean flag
     */
    /**
     * Sets response pending flag.
     *
     * @param val boolean flag
     */
    var responsePending: Boolean = false
        get() = field
    /**
     * Gets the Update Start Field value.
     *
     * @return update Start Date
     */
    /**
     * Sets the Update Start Field.
     *
     * @param val date to set
     */
    var updateStart: Date? = null
        get() = field
    /**
     * Gets the Update Finish Field value.
     *
     * @return update Finish Date
     */
    /**
     * Sets the Update Finish Field.
     *
     * @param val date to set
     */
    var updateFinish: Date? = null
        get() = field
    /**
     * Retrieves the schedule ID.
     *
     * @return schedule ID
     */
    /**
     * Sets the schedule ID.
     *
     * @param val schedule ID
     */
    var scheduleID: String? = null
        get() = field

    companion object {

        val EMPTY = ResourceAssignmentWorkgroupFields()
    }
}
