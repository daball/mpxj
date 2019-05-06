/*
 * file:       Filter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       30/10/2006
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

/**
 * This class represents a filter which may be applied to a
 * task or resource view.
 */
class Filter {

    /**
     * Retrieves the filter's unique ID.
     *
     * @return unique ID
     */
    /**
     * Sets the filter's unique ID.
     *
     * @param id unique ID
     */
    var id: Integer?
        get() = m_id
        set(id) {
            m_id = id
        }

    /**
     * Retrieves the filter's name.
     *
     * @return filter name
     */
    /**
     * Sets the filter's name.
     *
     * @param name filter name
     */
    var name: String?
        get() = m_name
        set(name) {
            m_name = name
        }

    /**
     * Retrieves the "show related summary rows" flag.
     *
     * @return boolean flag
     */
    /**
     * Sets the "show related summary rows" flag.
     *
     * @param showRelatedSummaryRows boolean flag
     */
    var showRelatedSummaryRows: Boolean
        get() = m_showRelatedSummaryRows
        set(showRelatedSummaryRows) {
            m_showRelatedSummaryRows = showRelatedSummaryRows
        }

    private var m_id: Integer? = null
    private var m_name: String? = null
    /**
     * Retrieves a flag indicating if this is a task filter.
     *
     * @return boolean flag
     */
    /**
     * Sets the flag indicating if this is a task filter.
     *
     * @param flag task filter flag
     */
    var isTaskFilter: Boolean = false
    /**
     * Retrieves a flag indicating if this is a resource filter.
     *
     * @return boolean flag
     */
    /**
     * Sets the flag indicating if this is a resource filter.
     *
     * @param flag resource filter flag
     */
    var isResourceFilter: Boolean = false
    private var m_showRelatedSummaryRows: Boolean = false
    /**
     * Retrieve the criteria used to define this filter.
     *
     * @return list of filter criteria
     */
    /**
     * Sets the criteria associted with this filter.
     *
     * @param criteria filter criteria
     */
    var criteria: GenericCriteria? = null
    /**
     * Retrieves the prompts required to supply parameters to this filter.
     *
     * @return filter prompts
     */
    /**
     * Sets the prompts to supply the parameters required by this filter.
     *
     * @param prompts filter prompts
     */
    var prompts: List<GenericCriteriaPrompt>? = null

    /**
     * Evaluates the filter, returns true if the supplied Task or Resource
     * instance matches the filter criteria.
     *
     * @param container Task or Resource instance
     * @param promptValues respose to prompts
     * @return boolean flag
     */
    fun evaluate(container: FieldContainer, promptValues: Map<GenericCriteriaPrompt, Object>): Boolean {
        var result = true
        if (criteria != null) {
            result = criteria!!.evaluate(container, promptValues)

            //
            // If this row has failed, but it is a summary row, and we are
            // including related summary rows, then we need to recursively test
            // its children
            //
            if (!result && m_showRelatedSummaryRows && container is Task) {
                for (task in (container as Task).childTasks) {
                    if (evaluate(task, promptValues)) {
                        result = true
                        break
                    }
                }
            }
        }

        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        val sb = StringBuilder()
        sb.append("[Filter id=")
        sb.append(m_id)
        sb.append(" name=")
        sb.append(m_name)
        sb.append(" showRelatedSummaryRows=")
        sb.append(m_showRelatedSummaryRows)
        if (criteria != null) {
            sb.append(" criteria=")
            sb.append(criteria)
        }
        sb.append("]")

        return sb.toString()
    }
}
