/*
 * file:       ResourceContainer.java
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
 * Manages the collection of resources belonging to a project.
 */
class ResourceContainer
/**
 * Constructor.
 *
 * @param projectFile parent project
 */
(projectFile: ProjectFile) : ProjectEntityWithIDContainer<Resource>(projectFile) {

    @Override
    public override fun removed(resource: Resource) {
        m_uniqueIDMap.remove(resource.uniqueID)
        m_idMap.remove(resource.id)

        val iter = m_projectFile.resourceAssignments.iterator()
        val resourceUniqueID = resource.uniqueID
        while (iter.hasNext() === true) {
            val assignment = iter.next()
            if (NumberHelper.equals(assignment.resourceUniqueID, resourceUniqueID)) {
                assignment.task!!.removeResourceAssignment(assignment)
                iter.remove()
            }
        }

        val calendar = resource.resourceCalendar
        calendar?.remove()
    }

    /**
     * Add a resource to the project.
     *
     * @return new resource instance
     */
    fun add(): Resource {
        val resource = Resource(m_projectFile)
        add(resource)
        return resource
    }
}
