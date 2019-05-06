/*
 * file:       GenericCriteria.java
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

import java.util.Date
import java.util.LinkedList

import net.sf.mpxj.common.DateHelper

/**
 * This class represents the criteria used as part of an evaluation.
 */
open class GenericCriteria
/**
 * Constructor.
 *
 * @param properties project properties
 */
(private val m_properties: ProjectProperties) {

    /**
     * Retrieves the LHS of the expression.
     *
     * @return LHS value
     */
    /**
     * Sets the LHS of the expression.
     *
     * @param value LHS value
     */
    var leftValue: FieldType?
        get() = m_leftValue
        set(value) {
            m_leftValue = value
        }
    private var m_leftValue: FieldType? = null
    /**
     * Retrieve the operator used in the test.
     *
     * @return test operator
     */
    /**
     * Set the operator used in the test.
     *
     * @param operator test operator
     */
    var operator: TestOperator? = null
    private val m_definedRightValues = arrayOfNulls<Object>(2)
    private val m_workingRightValues = arrayOfNulls<Object>(2)
    private var m_symbolicValues: Boolean = false
    /**
     * Retrieves the list of child criteria associated with the current criteria.
     *
     * @return list of criteria
     */
    val criteriaList: List<GenericCriteria> = LinkedList<GenericCriteria>()

    /**
     * Add the value to list of values to be used as part of the
     * evaluation of this indicator.
     *
     * @param index position in the list
     * @param value evaluation value
     */
    fun setRightValue(index: Int, value: Object) {
        var value = value
        m_definedRightValues[index] = value

        if (value is FieldType) {
            m_symbolicValues = true
        } else {
            if (value is Duration) {
                if ((value as Duration).getUnits() !== TimeUnit.HOURS) {
                    value = (value as Duration).convertUnits(TimeUnit.HOURS, m_properties)
                }
            }
        }

        m_workingRightValues[index] = value
    }

    /**
     * Retrieve the first value.
     *
     * @param index position in the list
     * @return first value
     */
    fun getValue(index: Int): Object {
        return m_definedRightValues[index]
    }

    /**
     * Evaluate the criteria and return a boolean result.
     *
     * @param container field container
     * @param promptValues responses to prompts
     * @return boolean flag
     */
    fun evaluate(container: FieldContainer, promptValues: Map<GenericCriteriaPrompt, Object>): Boolean {
        //
        // Retrieve the LHS value
        //
        val field = m_leftValue
        var lhs: Object?

        if (field == null) {
            lhs = null
        } else {
            lhs = container.getCurrentValue(field)
            when (field!!.getDataType()) {
                DATE -> {
                    if (lhs != null) {
                        lhs = DateHelper.getDayStartDate(lhs as Date?)
                    }
                }

                DURATION -> {
                    if (lhs != null) {
                        val dur = lhs as Duration?
                        lhs = dur!!.convertUnits(TimeUnit.HOURS, m_properties)
                    } else {
                        lhs = Duration.getInstance(0, TimeUnit.HOURS)
                    }
                }

                STRING -> {
                    lhs = if (lhs == null) "" else lhs
                }

                else -> {
                }
            }
        }

        //
        // Retrieve the RHS values
        //
        val rhs: Array<Object>
        if (m_symbolicValues == true) {
            rhs = processSymbolicValues(m_workingRightValues, container, promptValues)
        } else {
            rhs = m_workingRightValues
        }

        //
        // Evaluate
        //
        val result: Boolean
        when (operator) {
            AND, OR -> {
                result = evaluateLogicalOperator(container, promptValues)
            }

            else -> {
                result = operator!!.evaluate(lhs, rhs)
            }
        }

        return result
    }

    /**
     * Evalutes AND and OR operators.
     *
     * @param container data context
     * @param promptValues responses to prompts
     * @return operator result
     */
    private fun evaluateLogicalOperator(container: FieldContainer, promptValues: Map<GenericCriteriaPrompt, Object>): Boolean {
        var result = false

        if (criteriaList.size() === 0) {
            result = true
        } else {
            for (criteria in criteriaList) {
                result = criteria.evaluate(container, promptValues)
                if (operator === TestOperator.AND && !result || operator === TestOperator.OR && result) {
                    break
                }
            }
        }

        return result
    }

    /**
     * This method is called to create a new list of values, converting from
     * any symbolic values (represented by FieldType instances) to actual
     * values retrieved from a Task or Resource instance.
     *
     * @param oldValues list of old values containing symbolic items
     * @param container Task or Resource instance
     * @param promptValues response to prompts
     * @return new list of actual values
     */
    private fun processSymbolicValues(oldValues: Array<Object>, container: FieldContainer, promptValues: Map<GenericCriteriaPrompt, Object>?): Array<Object> {
        val newValues = arrayOfNulls<Object>(2)

        for (loop in oldValues.indices) {
            var value: Object? = oldValues[loop] ?: continue

            if (value is FieldType) {
                val type = value as FieldType?
                value = container.getCachedValue(type)

                when (type!!.getDataType()) {
                    DATE -> {
                        if (value != null) {
                            value = DateHelper.getDayStartDate(value as Date?)
                        }
                    }

                    DURATION -> {
                        if (value != null && (value as Duration).getUnits() !== TimeUnit.HOURS) {
                            value = (value as Duration).convertUnits(TimeUnit.HOURS, m_properties)
                        } else {
                            value = Duration.getInstance(0, TimeUnit.HOURS)
                        }
                    }

                    STRING -> {
                        value = if (value == null) "" else value
                    }

                    else -> {
                    }
                }
            } else {
                if (value is GenericCriteriaPrompt && promptValues != null) {
                    val prompt = value as GenericCriteriaPrompt?
                    value = promptValues[prompt]
                }
            }
            newValues[loop] = value
        }
        return newValues
    }

    /**
     * Adds a an item to the list of child criteria.
     *
     * @param criteria criteria item to add
     */
    fun addCriteria(criteria: GenericCriteria) {
        criteriaList.add(criteria)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    open fun toString(): String {
        val sb = StringBuilder()
        sb.append("(")

        when (operator) {
            AND, OR -> {
                var index = 0
                for (c in criteriaList) {
                    sb.append(c)
                    ++index
                    if (index < criteriaList.size()) {
                        sb.append(" ")
                        sb.append(operator)
                        sb.append(" ")
                    }
                }
            }

            else -> {
                sb.append(m_leftValue)
                sb.append(" ")
                sb.append(operator)
                sb.append(" ")
                sb.append(m_definedRightValues[0])
                if (m_definedRightValues[1] != null) {
                    sb.append(",")
                    sb.append(m_definedRightValues[1])
                }
            }
        }

        sb.append(")")
        return sb.toString()
    }
}
