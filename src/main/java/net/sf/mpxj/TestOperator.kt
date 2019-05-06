/*
 * file:       TestOperator.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2006
 * date:       15/02/2006
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

import net.sf.mpxj.common.EnumHelper
import net.sf.mpxj.common.NumberHelper

/**
 * This class represents the set of operators used to perform a test
 * between two or more operands.
 */
enum class TestOperator
/**
 * Private constructor.
 *
 * @param type int version of the enum
 */
private constructor(
        /**
         * Internal representation of the enum int type.
         */
        private val m_value: Int) : MpxjEnum {
    IS_ANY_VALUE(0) {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object, rhs: Object): Boolean {
            return true
        }
    },

    IS_WITHIN(1) {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object, rhs: Object): Boolean {
            return evaluateWithin(lhs, rhs)
        }
    },

    IS_GREATER_THAN(2) {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object, rhs: Object): Boolean {
            return evaluateCompareTo(lhs, rhs) > 0
        }
    },

    IS_LESS_THAN(3) {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object, rhs: Object): Boolean {
            return evaluateCompareTo(lhs, rhs) < 0
        }
    },

    IS_GREATER_THAN_OR_EQUAL_TO(4) {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object, rhs: Object): Boolean {
            return evaluateCompareTo(lhs, rhs) >= 0
        }
    },

    IS_LESS_THAN_OR_EQUAL_TO(5) {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object, rhs: Object): Boolean {
            return evaluateCompareTo(lhs, rhs) <= 0
        }
    },

    EQUALS(6) {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object?, rhs: Object): Boolean {
            val result: Boolean

            if (lhs == null) {
                result = getSingleOperand(rhs) == null
            } else {
                result = lhs!!.equals(getSingleOperand(rhs))
            }
            return result
        }
    },

    DOES_NOT_EQUAL(7) {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object?, rhs: Object): Boolean {
            val result: Boolean
            if (lhs == null) {
                result = getSingleOperand(rhs) != null
            } else {
                result = !lhs!!.equals(getSingleOperand(rhs))
            }
            return result
        }
    },

    CONTAINS(8) {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object, rhs: Object): Boolean {
            return evaluateContains(lhs, rhs)
        }
    },

    IS_NOT_WITHIN(9) {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object, rhs: Object): Boolean {
            return !evaluateWithin(lhs, rhs)
        }
    },

    DOES_NOT_CONTAIN(10) {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object, rhs: Object): Boolean {
            return !evaluateContains(lhs, rhs)
        }
    },

    CONTAINS_EXACTLY(11) {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object, rhs: Object): Boolean {
            return evaluateContainsExactly(lhs, rhs)
        }
    },

    AND(12) // Extension used by MPXJ, Not MS Project
    {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object, rhs: Object): Boolean {
            throw UnsupportedOperationException()
        }
    },

    OR(13) // Extension used by MPXJ, Not MS Project
    {
        /**
         * {@inheritDoc}
         */
        @Override
        override fun evaluate(lhs: Object, rhs: Object): Boolean {
            throw UnsupportedOperationException()
        }
    };

    /**
     * Accessor method used to retrieve the numeric representation of the enum.
     *
     * @return int representation of the enum
     */
    override val value: Int
        @Override get() = m_value

    /**
     * This method applies the operator represented by this class to the
     * supplied operands. Note that the RHS operand can be a list, allowing
     * range operators like "within" to operate.
     *
     * @param lhs operand
     * @param rhs operand
     * @return boolean result
     */
    abstract fun evaluate(lhs: Object, rhs: Object): Boolean

    /**
     * This method is used to ensure that if a list of operand values has been
     * supplied, that a single operand is extracted.
     *
     * @param operand operand value
     * @return single operand value
     */
    protected fun getSingleOperand(operand: Object?): Object? {
        var operand = operand
        if (operand is Array<Object>) {
            val list = operand as Array<Object>?
            operand = list!![0]
        }

        return operand
    }

    /**
     * Determine if the supplied value falls within the specified range.
     *
     * @param lhs single value operand
     * @param rhs range operand
     * @return boolean result
     */
    @SuppressWarnings("unchecked", "rawtypes")
    protected fun evaluateWithin(lhs: Object?, rhs: Object): Boolean {
        var result = false

        if (rhs is Array<Object>) {
            val rhsList = rhs as Array<Object>
            if (lhs != null) {
                val lhsComparable = lhs as Comparable?
                if (rhsList[0] != null && rhsList[1] != null) {
                    // Project also tries with the values flipped
                    result = lhsComparable!!.compareTo(rhsList[0]) >= 0 && lhsComparable.compareTo(rhsList[1]) <= 0 || lhsComparable.compareTo(rhsList[0]) <= 0 && lhsComparable.compareTo(rhsList[1]) >= 0
                }
            } else {
                // Project also respects null equality (e.g. NA dates)
                result = rhsList[0] == null || rhsList[1] == null
            }
        }
        return result
    }

    /**
     * Implements a simple compare-to operation. Assumes that the LHS
     * operand implements the Comparable interface.
     *
     * @param lhs operand
     * @param rhs operand
     * @return boolean result
     */
    @SuppressWarnings("unchecked", "rawtypes")
    protected fun evaluateCompareTo(lhs: Object?, rhs: Object?): Int {
        var rhs = rhs
        val result: Int

        rhs = getSingleOperand(rhs)

        if (lhs == null || rhs == null) {
            if (lhs === rhs) {
                result = 0
            } else {
                if (lhs == null) {
                    result = 1
                } else {
                    result = -1
                }
            }
        } else {
            result = (lhs as Comparable).compareTo(rhs)
        }

        return result
    }

    /**
     * Assuming the supplied arguments are both Strings, this method
     * determines if rhs is contained within lhs. This test is case insensitive.
     *
     * @param lhs operand
     * @param rhs operand
     * @return boolean result
     */
    protected fun evaluateContains(lhs: Object, rhs: Object?): Boolean {
        var rhs = rhs
        var result = false

        rhs = getSingleOperand(rhs)

        if (lhs is String && rhs is String) {
            result = (lhs as String).toUpperCase().indexOf((rhs as String).toUpperCase()) !== -1
        }

        return result
    }

    /**
     * Assuming the supplied arguments are both Strings, this method
     * determines if rhs is contained within lhs. This test is case sensitive.
     *
     * @param lhs operand
     * @param rhs operand
     * @return boolean result
     */
    protected fun evaluateContainsExactly(lhs: Object, rhs: Object?): Boolean {
        var rhs = rhs
        var result = false

        rhs = getSingleOperand(rhs)

        if (lhs is String && rhs is String) {
            result = (lhs as String).indexOf(rhs as String?) !== -1
        }

        return result
    }

    companion object {

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Int): TestOperator {
            var type = type
            if (type < 0 || type >= TYPE_VALUES.size) {
                type = IS_ANY_VALUE.value
            }
            return TYPE_VALUES[type]
        }

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Number?): TestOperator {
            val value: Int
            if (type == null) {
                value = -1
            } else {
                value = NumberHelper.getInt(type)
            }
            return getInstance(value)
        }

        /**
         * Array mapping int types to enums.
         */
        private val TYPE_VALUES = EnumHelper.createTypeArray(TestOperator::class.java)
    }
}
