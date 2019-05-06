/*
 * file:       Group.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       17/01/2007
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

import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.util.LinkedList

/**
 * This class represents the definition of the grouping used
 * to organise data in a view.
 */
class Group
/**
 * Constructor.
 *
 * @param id group identifier
 * @param name group name
 * @param showSummaryTasks show summary tasks
 */
(
        /**
         * Retrieve group ID.
         *
         * @return group ID
         */
        val id: Integer,
        /**
         * Retrieve the group name.
         *
         * @return group name
         */
        val name: String,
        /**
         * Retrieve the show summary tasks flag.
         *
         * @return boolean flag
         */
        val showSummaryTasks: Boolean) {

    /**
     * Retrieve a list of all clauses which define this group.
     *
     * @return list of clauses
     */
    val groupClauses: List<GroupClause>
        get() = m_clauses
    private val m_clauses = LinkedList<GroupClause>()

    /**
     * Adds a clause to the group definition.
     *
     * @param clause group clause
     */
    fun addGroupClause(clause: GroupClause) {
        m_clauses.add(clause)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        val os = ByteArrayOutputStream()
        val pw = PrintWriter(os)
        pw.println("[Group ")
        pw.println(" id=$id")
        pw.println(" name=$name")
        pw.println(" showSummaryTasks=$showSummaryTasks")
        pw.println(" [Clauses=")
        for (gc in m_clauses) {
            pw.println("  $gc")
        }
        pw.println(" ]")
        pw.println("]")
        pw.flush()
        return os.toString()

    }
}
