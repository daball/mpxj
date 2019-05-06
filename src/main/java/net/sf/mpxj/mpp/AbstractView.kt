/*
 * file:       AbstractView.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2003
 * date:       27/10/2003
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

package net.sf.mpxj.mpp

import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Table
import net.sf.mpxj.TableContainer
import net.sf.mpxj.View
import net.sf.mpxj.ViewType

/**
 * This abstract class implements functionality common to all views.
 */
abstract class AbstractView
/**
 * Constructor.
 *
 * @param parent parent file
 */
(parent: ProjectFile) : View {

    /**
     * {@inheritDoc}
     */
    val id: Integer?
        @Override get() = m_id

    /**
     * {@inheritDoc}
     */
    val name: String?
        @Override get() = m_name

    /**
     * {@inheritDoc}
     */
    val type: ViewType?
        @Override get() = m_type

    /**
     * Retrieve the name of the table part of the view.
     *
     * @return table name
     */
    val tableName: String?
        get() = m_tableName

    /**
     * Retrieve an instance of the Table class representing the
     * table part of this view.
     *
     * @return table instance
     */
    val table: Table
        get() = m_tables.getTaskTableByName(m_tableName)

    protected var m_properties: ProjectProperties
    protected var m_tables: TableContainer
    protected var m_id: Integer? = null
    protected var m_name: String? = null
    protected var m_type: ViewType? = null
    protected var m_tableName: String? = null

    init {
        m_properties = parent.projectProperties
        m_tables = parent.tables
    }

    /**
     * This method dumps the contents of this View as a String.
     * Note that this facility is provided as a debugging aid.
     *
     * @return formatted contents of this view
     */
    @Override
    open fun toString(): String {
        return "[View id=" + m_id + " type=" + m_type + " name=" + m_name + (if (m_tableName == null) "" else " table=" + m_tableName!!) + "]"
    }
}
