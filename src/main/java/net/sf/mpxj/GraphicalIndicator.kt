/*
 * file:       GraphicalIndicator.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       16/02/2006
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

import net.sf.mpxj.common.NumberHelper

/**
 * This class represents the set of information which defines how
 * a Graphical Indicator will be presented for a single column in
 * a table within Microsoft Project.
 */
class GraphicalIndicator {

    /**
     * Retrieves the field type to which this indicator applies.
     *
     * @return field type
     */
    /**
     * Sets the field type to which this indicator applies.
     *
     * @param fieldType field type
     */
    var fieldType: FieldType?
        get() = m_fieldType
        set(fieldType) {
            m_fieldType = fieldType
        }

    /**
     * Retrieves a flag indicating if graphical indicators should be displayed
     * for this column, rather than the actual values.
     *
     * @return boolean flag
     */
    /**
     * Sets a flag indicating if graphical indicators should be displayed
     * for this column, rather than the actual values.
     *
     * @param displayGraphicalIndicators boolean flag
     */
    var displayGraphicalIndicators: Boolean
        get() = m_displayGraphicalIndicators
        set(displayGraphicalIndicators) {
            m_displayGraphicalIndicators = displayGraphicalIndicators
        }

    /**
     * Retrieve the criteria to be applied to non-summary rows.
     *
     * @return list of non-summary row criteria
     */
    val nonSummaryRowCriteria: List<GraphicalIndicatorCriteria>
        get() = m_nonSummaryRowCriteria

    /**
     * Retrieve the criteria to be applied to the project summary.
     *
     * @return list of project summary criteria
     */
    val projectSummaryCriteria: List<GraphicalIndicatorCriteria>
        get() = m_projectSummaryCriteria

    /**
     * Retrieves a flag which indicates if the project summary row inherits
     * criteria from the summary row.
     *
     * @return boolean flag
     */
    /**
     * Sets a flag which indicates if the project summary row inherits
     * criteria from the summary row.
     *
     * @param projectSummaryInheritsFromSummaryRows boolean flag
     */
    var projectSummaryInheritsFromSummaryRows: Boolean
        get() = m_projectSummaryInheritsFromSummaryRows
        set(projectSummaryInheritsFromSummaryRows) {
            m_projectSummaryInheritsFromSummaryRows = projectSummaryInheritsFromSummaryRows
        }

    /**
     * Retrieves a flag which indicates if summary rows inherit
     * criteria from non-summary rows.
     *
     * @return boolean flag
     */
    /**
     * Sets a flag which indicates if summary rows inherit
     * criteria from non-summary rows.
     *
     * @param summaryRowsInheritFromNonSummaryRows boolean flag
     */
    var summaryRowsInheritFromNonSummaryRows: Boolean
        get() = m_summaryRowsInheritFromNonSummaryRows
        set(summaryRowsInheritFromNonSummaryRows) {
            m_summaryRowsInheritFromNonSummaryRows = summaryRowsInheritFromNonSummaryRows
        }

    /**
     * Retrieve the flag which indicates that data values should be shown
     * as tool tips.
     *
     * @return boolean flag
     */
    /**
     * Set the flag which indicates that data values should be shown
     * as tool tips.
     *
     * @param showDataValuesInToolTips boolean flag
     */
    var showDataValuesInToolTips: Boolean
        get() = m_showDataValuesInToolTips
        set(showDataValuesInToolTips) {
            m_showDataValuesInToolTips = showDataValuesInToolTips
        }

    private var m_fieldType: FieldType? = null
    private var m_displayGraphicalIndicators: Boolean = false
    private var m_summaryRowsInheritFromNonSummaryRows: Boolean = false
    private var m_projectSummaryInheritsFromSummaryRows: Boolean = false
    private var m_showDataValuesInToolTips: Boolean = false
    private val m_nonSummaryRowCriteria = LinkedList<GraphicalIndicatorCriteria>()
    /**
     * Retrieve the criteria to be applied to summary rows.
     *
     * @return list of summary row criteria
     */
    val summaryRowCriteria: List<GraphicalIndicatorCriteria> = LinkedList<GraphicalIndicatorCriteria>()
    private val m_projectSummaryCriteria = LinkedList<GraphicalIndicatorCriteria>()
    /**
     * This method evaluates a if a graphical indicator should
     * be displayed, given a set of Task or Resource data. The
     * method will return -1 if no indicator should be displayed.
     *
     * @param container Task or Resource instance
     * @return indicator index
     */
    fun evaluate(container: FieldContainer): Int {
        //
        // First step - determine the list of criteria we are should use
        //
        val criteria: List<GraphicalIndicatorCriteria>
        if (container is Task) {
            val task = container as Task
            if (NumberHelper.getInt(task.uniqueID) == 0) {
                if (m_projectSummaryInheritsFromSummaryRows == false) {
                    criteria = m_projectSummaryCriteria
                } else {
                    if (m_summaryRowsInheritFromNonSummaryRows == false) {
                        criteria = summaryRowCriteria
                    } else {
                        criteria = m_nonSummaryRowCriteria
                    }
                }
            } else {
                if (task.summary == true) {
                    if (m_summaryRowsInheritFromNonSummaryRows == false) {
                        criteria = summaryRowCriteria
                    } else {
                        criteria = m_nonSummaryRowCriteria
                    }
                } else {
                    criteria = m_nonSummaryRowCriteria
                }
            }
        } else {
            // It is possible to have a resource summary row, but at the moment
            // I can't see how you can determine this.
            criteria = m_nonSummaryRowCriteria
        }

        //
        // Now we have the criteria, evaluate each one until we get a result
        //
        var result = -1
        for (gic in criteria) {
            result = gic.evaluate(container)
            if (result != -1) {
                break
            }
        }

        //
        // If we still don't have a result at the end, return the
        // default value, which is 0
        //
        if (result == -1) {
            result = 0
        }

        return result
    }

    /**
     * Add criteria relating to non summary rows.
     *
     * @param criteria indicator criteria
     */
    fun addNonSummaryRowCriteria(criteria: GraphicalIndicatorCriteria) {
        m_nonSummaryRowCriteria.add(criteria)
    }

    /**
     * Add criteria relating to summary rows.
     *
     * @param criteria indicator criteria
     */
    fun addSummaryRowCriteria(criteria: GraphicalIndicatorCriteria) {
        summaryRowCriteria.add(criteria)
    }

    /**
     * Add criteria relating to project summary.
     *
     * @param criteria indicator criteria
     */
    fun addProjectSummaryCriteria(criteria: GraphicalIndicatorCriteria) {
        m_projectSummaryCriteria.add(criteria)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        val os = ByteArrayOutputStream()
        val pw = PrintWriter(os)
        pw.println("[GraphicalIndicator")
        pw.println(" FieldType=" + m_fieldType!!)
        pw.println(" DisplayGraphicalIndicators=$m_displayGraphicalIndicators")
        pw.println(" SummaryRowsInheritFromNonSummaryRows=$m_summaryRowsInheritFromNonSummaryRows")
        pw.println(" ProjectSummaryInheritsFromSummaryRows=$m_projectSummaryInheritsFromSummaryRows")
        pw.println(" ShowDataValuesInToolTips=$m_showDataValuesInToolTips")
        pw.println(" NonSummaryRowCriteria=")
        for (gi in m_nonSummaryRowCriteria) {
            pw.println("  $gi")
        }
        pw.println(" SummaryRowCriteria=")
        for (gi in summaryRowCriteria) {
            pw.println("  $gi")
        }
        pw.println(" ProjectSummaryCriteria=")
        for (gi in m_projectSummaryCriteria) {
            pw.println("  $gi")
        }
        pw.println("]")
        pw.flush()
        return os.toString()
    }
}
