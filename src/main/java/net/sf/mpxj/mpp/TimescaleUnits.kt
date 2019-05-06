/*
 * file:       TimescaleUnits.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Apr 7, 2005
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
 * Class representing the units which may be shown on a Gantt chart timescale.
 */
enum class TimescaleUnits
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
    NONE(-1, "None"),
    MINUTES(0, "Minutes"),
    HOURS(1, "Hours"),
    DAYS(2, "Days"),
    WEEKS(3, "Weeks"),
    THIRDS_OF_MONTHS(4, "Thirds of Months"),
    MONTHS(5, "Months"),
    QUARTERS(6, "Quarters"),
    HALF_YEARS(7, "Half Years"),
    YEARS(8, "Years");

    /**
     * Accessor method used to retrieve the numeric representation of the enum.
     *
     * @return int representation of the enum
     */
    override val value: Int
        @Override get() = m_value

    /**
     * Retrieve the name of this time unit. Note that this is not
     * localised.
     *
     * @return name of this timescale unit
     */
    val name: String
        get() = m_name

    /**
     * Generate a string representation of this instance.
     *
     * @return string representation of this instance
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
        fun getInstance(type: Int): TimescaleUnits {
            val result: TimescaleUnits
            if (type < 0 || type >= TYPE_VALUES.size) {
                result = NONE
            } else {
                result = TYPE_VALUES[type]
            }
            return result
        }

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Number?): TimescaleUnits {
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
        private val TYPE_VALUES = EnumHelper.createTypeArray(TimescaleUnits::class.java, -1)
    }
}
