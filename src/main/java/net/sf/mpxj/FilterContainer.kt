/*
 * file:       FilterContainer.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2015
 * date:       24/04/2015
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

import java.util.ArrayList
import java.util.HashMap

/**
 * Manages filter definitions belonging to a project.
 */
class FilterContainer {

    /**
     * Retrieves a list of all resource filters.
     *
     * @return list of all resource filters
     */
    val resourceFilters: List<Filter>
        get() = m_resourceFilters

    /**
     * Retrieves a list of all task filters.
     *
     * @return list of all task filters
     */
    val taskFilters: List<Filter>
        get() = m_taskFilters

    /**
     * List of all task filters.
     */
    private val m_taskFilters = ArrayList<Filter>()

    /**
     * List of all resource filters.
     */
    private val m_resourceFilters = ArrayList<Filter>()

    /**
     * Index of filters by name.
     */
    private val m_filtersByName = HashMap<String, Filter>()

    /**
     * Index of filters by ID.
     */
    private val m_filtersByID = HashMap<Integer, Filter>()

    /**
     * Adds a filter definition to this project file.
     *
     * @param filter filter definition
     */
    fun addFilter(filter: Filter) {
        if (filter.isTaskFilter()) {
            m_taskFilters.add(filter)
        }

        if (filter.isResourceFilter()) {
            m_resourceFilters.add(filter)
        }

        m_filtersByName.put(filter.getName(), filter)
        m_filtersByID.put(filter.getID(), filter)
    }

    /**
     * Removes a filter from this project file.
     *
     * @param filterName The name of the filter
     */
    fun removeFilter(filterName: String) {
        val filter = getFilterByName(filterName)
        if (filter != null) {
            if (filter!!.isTaskFilter()) {
                m_taskFilters.remove(filter)
            }

            if (filter!!.isResourceFilter()) {
                m_resourceFilters.remove(filter)
            }
            m_filtersByName.remove(filterName)
            m_filtersByID.remove(filter!!.getID())
        }
    }

    /**
     * Retrieve a given filter by name.
     *
     * @param name filter name
     * @return filter instance
     */
    fun getFilterByName(name: String): Filter? {
        return m_filtersByName.get(name)
    }

    /**
     * Retrieve a given filter by ID.
     *
     * @param id filter ID
     * @return filter instance
     */
    fun getFilterByID(id: Integer): Filter {
        return m_filtersByID.get(id)
    }
}
