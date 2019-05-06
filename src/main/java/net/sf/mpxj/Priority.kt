/*
 * file:       Priority.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       18/02/2003
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
 * This class is used to represent a priority. It provides a mapping
 * between the textual description of a priority found in an MPX
 * file, and an enumerated representation that can be more easily manipulated
 * programatically.
 */
class Priority
/**
 * This constructor takes the numeric enumerated representation of a
 * priority and populates the class instance appropriately.
 * Note that unrecognised values are treated as medium priorities.
 *
 * @param priority int representation of the priority
 */
private constructor(priority: Int) {

    /**
     * Accessor method used to retrieve the numeric representation of the
     * priority.
     *
     * @return int representation of the priority
     */
    val value: Int
        get() = m_value

    /**
     * Internal representation of the priority.
     */
    private var m_value: Int = 0

    init {
        if (priority < 0 || priority > DO_NOT_LEVEL) {
            m_value = MEDIUM
        } else {
            m_value = priority
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return "[Priority value=$m_value]"
    }

    companion object {

        /**
         * This method takes an integer enumeration of a priority
         * and returns an appropriate instance of this class. Note that unrecognised
         * values are treated as medium priority.
         *
         * @param priority int version of the priority
         * @return Priority class instance
         */
        fun getInstance(priority: Int): Priority {
            val result: Priority

            if (priority >= LOWEST && priority <= DO_NOT_LEVEL && priority % 100 == 0) {
                result = VALUE[priority / 100 - 1]
            } else {
                result = Priority(priority)
            }

            return result
        }

        /**
         * Constant for lowest priority.
         */
        val LOWEST = 100

        /**
         * Constant for low priority.
         */
        val VERY_LOW = 200

        /**
         * Constant for lower priority.
         */
        val LOWER = 300

        /**
         * Constant for low priority.
         */
        val LOW = 400

        /**
         * Constant for medium priority.
         */
        val MEDIUM = 500

        /**
         * Constant for high priority.
         */
        val HIGH = 600

        /**
         * Constant for higher priority.
         */
        val HIGHER = 700

        /**
         * Constant for very high priority.
         */
        val VERY_HIGH = 800

        /**
         * Constant for highest priority.
         */
        val HIGHEST = 900

        /**
         * Constant for do not level.
         */
        val DO_NOT_LEVEL = 1000

        /**
         * Array of type values matching the above constants.
         */
        private val VALUE = arrayOf(Priority(LOWEST), Priority(VERY_LOW), Priority(LOWER), Priority(LOW), Priority(MEDIUM), Priority(HIGH), Priority(HIGHER), Priority(VERY_HIGH), Priority(HIGHEST), Priority(DO_NOT_LEVEL))
    }
}
