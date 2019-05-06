/*
 * file:       TimeUnit.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       03/01/2003
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
 * This class contains utility functions allowing time unit specifications
 * to be parsed and formatted.
 */
enum class TimeUnit
/**
 * Private constructor.
 *
 * @param type int version of the enum
 * @param name enum name
 */
private constructor(
        /**
         * Internal representation of the enum int type.
         */
        private val m_value: Int, private val m_name: String) : MpxjEnum {
    /**
     * Constant representing Minutes.
     */
    MINUTES(0, "m"),

    /**
     * Constant representing Hours.
     */
    HOURS(1, "h"),

    /**
     * Constant representing Days.
     */
    DAYS(2, "d"),

    /**
     * Constant representing Weeks.
     */
    WEEKS(3, "w"),

    /**
     * Constant representing Months.
     */
    MONTHS(4, "mo"),

    /**
     * Constant representing Percent.
     */
    PERCENT(5, "%"),

    /**
     * Constant representing Years.
     */
    YEARS(6, "y"),

    /**
     * Constant representing Elapsed Minutes.
     */
    ELAPSED_MINUTES(7, "em"),

    /**
     * Constant representing Elapsed Hours.
     */
    ELAPSED_HOURS(8, "eh"),

    /**
     * Constant representing Elapsed Days.
     */
    ELAPSED_DAYS(9, "ed"),

    /**
     * Constant representing Elapsed Weeks.
     */
    ELAPSED_WEEKS(10, "ew"),

    /**
     * Constant representing Elapsed Months.
     */
    ELAPSED_MONTHS(11, "emo"),

    /**
     * Constant representing Elapsed Years.
     */
    ELAPSED_YEARS(12, "ey"),

    /**
     * Constant representing Elapsed Percent.
     */
    ELAPSED_PERCENT(13, "e%");

    /**
     * Accessor method used to retrieve the numeric representation of the enum.
     *
     * @return int representation of the enum
     */
    override val value: Int
        @Override get() = m_value

    /**
     * Retrieve the name associated with this enum.
     *
     * @return name
     */
    val name: String
        get() = m_name

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return name
    }

    companion object {

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Int): TimeUnit {
            var type = type
            if (type < 0 || type >= TYPE_VALUES.size) {
                type = DAYS.value
            }
            return TYPE_VALUES[type]
        }

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Number?): TimeUnit {
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
        private val TYPE_VALUES = EnumHelper.createTypeArray(TimeUnit::class.java)
    }
}
