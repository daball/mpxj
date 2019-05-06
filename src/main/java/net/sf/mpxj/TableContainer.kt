/*
 * file:       TableContainer.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2015
 * date:       23/04/2015
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

/**
 * Manages the table definitions belonging to a project.
 */
class TableContainer : ListWithCallbacks<Table>() {

    private val m_taskTablesByName = HashMap<String, Table>()
    private val m_resourceTablesByName = HashMap<String, Table>()
    @Override
    override fun added(table: Table) {
        getIndex(table).put(table.name, table)
    }

    @Override
    override fun removed(table: Table) {
        getIndex(table).remove(table.name)
    }

    /**
     * Utility method to retrieve the definition of a task table by name.
     * This method will return null if the table name is not recognised.
     *
     * @param name table name
     * @return table instance
     */
    fun getTaskTableByName(name: String): Table {
        return m_taskTablesByName.get(name)
    }

    /**
     * Utility method to retrieve the definition of a resource table by name.
     * This method will return null if the table name is not recognised.
     *
     * @param name table name
     * @return table instance
     */
    fun getResourceTableByName(name: String): Table {
        return m_resourceTablesByName.get(name)
    }

    /**
     * Retrieve the correct index for the supplied Table instance.
     *
     * @param table Table instance
     * @return index
     */
    private fun getIndex(table: Table): Map<String, Table> {
        val result: Map<String, Table>

        if (!table.resourceFlag) {
            result = m_taskTablesByName
        } else {
            result = m_resourceTablesByName
        }
        return result
    }
}
