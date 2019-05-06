/*
 * file:       ProjectEntityWithIDContainer.java
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
import java.util.HashMap

import net.sf.mpxj.common.NumberHelper

/**
 * Common implementation shared by project entities, providing storage, iteration and lookup.
 *
 * @param <T> concrete entity type
</T> */
abstract class ProjectEntityWithIDContainer<T : ProjectEntityWithID>
/**
 * Constructor.
 *
 * @param projectFile parent project
 */
(projectFile: ProjectFile) : ProjectEntityContainer<T>(projectFile) where T : Comparable<T> {

    protected var m_idMap: Map<Integer, T> = HashMap<Integer, T>()

    /**
     * This method can be called to ensure that the IDs of all
     * entities are sequential, and start from an
     * appropriate point. If entities are added to and removed from
     * this list, then the project is loaded into Microsoft
     * project, if the ID values have gaps in the sequence, there will
     * be blank rows shown.
     */
    fun renumberIDs() {
        if (!isEmpty()) {
            Collections.sort(this)
            val firstEntity = get(0)
            var id = NumberHelper.getInt(firstEntity.id)
            if (id != 0) {
                id = 1
            }

            for (entity in this) {
                entity.id = Integer.valueOf(id++)
            }
        }
    }

    /**
     * Retrieve an entity by its ID.
     *
     * @param id entity ID
     * @return entity instance or null
     */
    fun getByID(id: Integer): T {
        return m_idMap[id]
    }

    /**
     * Remove the ID to instance mapping.
     *
     * @param id ID to remove
     */
    fun unmapID(id: Integer) {
        m_idMap.remove(id)
    }

    /**
     * Add an ID to instance mapping.
     *
     * @param id ID
     * @param entity instance
     */
    fun mapID(id: Integer, entity: T) {
        m_idMap.put(id, entity)
    }
}
