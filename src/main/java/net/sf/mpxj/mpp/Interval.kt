/*
 * file:       Interval.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       22 July 2005
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

import net.sf.mpxj.MpxjEnum
import net.sf.mpxj.common.EnumHelper
import net.sf.mpxj.common.NumberHelper

/**
 * This class represents daily, weekly or monthly time intervals.
 */
enum class Interval
/**
 * Private constructor.
 *
 * @param type int version of the enum
 * @param name name of the enum
 */
private constructor(
        /**
         * Internal representation of the enum int type.
         */
        private val m_value: Int, private val m_name: String) : MpxjEnum {
    DAILY(0, "Daily"),
    WEEKLY(1, "Weekly"),
    MONTHLY(2, "Monthly");

    /**
     * Accessor method used to retrieve the numeric representation of the enum.
     *
     * @return int representation of the enum
     */
    override val value: Int
        @Override get() = m_value

    /**
     * Retrieve the interval name. Currently this is not localised.
     *
     * @return interval name
     */
    val name: String
        get() = m_name

    /**
     * Retrieve the String representation of this line style.
     *
     * @return String representation of this line style
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
        fun getInstance(type: Int): Interval {
            var type = type
            if (type < 0 || type >= TYPE_VALUES.size) {
                type = DAILY.value
            }
            return TYPE_VALUES[type]
        }

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Number?): Interval {
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
        private val TYPE_VALUES = EnumHelper.createTypeArray(Interval::class.java)
    }
}
