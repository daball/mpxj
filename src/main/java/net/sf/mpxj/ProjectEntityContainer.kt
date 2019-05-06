/*
 * file:       ProjectEntityContainer.java
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

import java.util.HashMap

import net.sf.mpxj.common.NumberHelper

/**
 * Common implementation shared by project entities, providing storage, iteration and lookup.
 *
 * @param <T> concrete entity type
</T> */
abstract class ProjectEntityContainer<T : ProjectEntityWithUniqueID>
/**
 * Constructor.
 *
 * @param projectFile parent project
 */
(protected val m_projectFile: ProjectFile) : ListWithCallbacks<T>() {
    protected var m_uniqueIDMap: Map<Integer, T> = HashMap<Integer, T>()

    /**
     * Returns the value of the first Unique ID to use when renumbering Unique IDs.
     *
     * @return first Unique ID value
     */
    protected open fun firstUniqueID(): Int {
        return 1
    }

    /**
     * Renumbers all entity unique IDs.
     */
    fun renumberUniqueIDs() {
        var uid = firstUniqueID()
        for (entity in this) {
            entity.uniqueID = Integer.valueOf(uid++)
        }
    }

    /**
     * Validate that the Unique IDs for the entities in this container are valid for MS Project.
     * If they are not valid, i.e one or more of them are too large, renumber them.
     */
    fun validateUniqueIDsForMicrosoftProject() {
        if (!isEmpty()) {
            for (entity in this) {
                if (NumberHelper.getInt(entity.uniqueID) > MS_PROJECT_MAX_UNIQUE_ID) {
                    renumberUniqueIDs()
                    break
                }
            }
        }
    }

    /**
     * Retrieve an entity by its Unique ID.
     *
     * @param id entity Unique ID
     * @return entity instance or null
     */
    fun getByUniqueID(id: Integer): T {
        return m_uniqueIDMap[id]
    }

    /**
     * Remove the Unique ID to instance mapping.
     *
     * @param id Unique ID to remove
     */
    fun unmapUniqueID(id: Integer) {
        m_uniqueIDMap.remove(id)
    }

    /**
     * Add a Unique ID to instance mapping.
     *
     * @param id Unique ID
     * @param entity instance
     */
    fun mapUniqueID(id: Integer, entity: T) {
        m_uniqueIDMap.put(id, entity)
    }

    companion object {

        /**
         * Maximum unique ID value MS Project will accept.
         */
        private val MS_PROJECT_MAX_UNIQUE_ID = 0x1FFFFF
    }
}
