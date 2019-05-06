/*
 * file:       ProgressLineDay.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       26/03/2005
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

import net.sf.mpxj.Day
import net.sf.mpxj.MpxjEnum
import net.sf.mpxj.common.EnumHelper

/**
 * Instances of this class represent enumerated day values used as to
 * define when progress lines are drawn.
 */
enum class ProgressLineDay
/**
 * Private constructor.
 *
 * @param value day value
 * @param day equivalent Day instance
 */
private constructor(
        /**
         * Retrieves the int representation of the day.
         *
         * @return task type value
         */
        @get:Override override val value: Int,
        /**
         * Retrieve the Day instance which is equivalent to this ProgressLine.
         *
         * @return Day instance
         */
        val day: Day) : MpxjEnum {
    SUNDAY(1, Day.SUNDAY),
    MONDAY(2, Day.MONDAY),
    TUESDAY(3, Day.TUESDAY),
    WEDNESDAY(4, Day.WEDNESDAY),
    THURSDAY(5, Day.THURSDAY),
    FRIDAY(6, Day.FRIDAY),
    SATURDAY(7, Day.SATURDAY),
    DAY(8, null),
    WORKINGDAY(9, null),
    NONWORKINGDAY(10, null);

    /**
     * This method provides a simple mechanism to retrieve
     * the next day in correct sequence, including the transition
     * from Sunday to Monday.
     *
     * @return ProgressLineDay instance
     */
    val nextDay: ProgressLineDay?
        get() {
            var value = this.value + 1
            if (value > 7) {
                value = 1
            }
            return getInstance(value)
        }

    companion object {

        /**
         * Retrieve a ProgressLineDay instance representing the supplied value.
         *
         * @param type type value
         * @return ProgressLineDay instance
         */
        fun getInstance(type: Int): ProgressLineDay? {
            val result: ProgressLineDay?

            if (type < 0 || type >= TYPE_VALUES.size) {
                result = null
            } else {
                result = TYPE_VALUES[type]
            }
            return result
        }

        /**
         * Array mapping int types to enums.
         */
        private val TYPE_VALUES = EnumHelper.createTypeArray(ProgressLineDay::class.java, 1)
    }

}
