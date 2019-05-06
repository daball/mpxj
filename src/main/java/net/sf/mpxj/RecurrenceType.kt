/*
 * file:       RecurrenceType.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2008
 * date:       12/06/2008
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

/**
 * Represents the recurrence type.
 */
enum class RecurrenceType
/**
 * Private constructor.
 *
 * @param type int version of the enum
 * @param name English name used for debugging
 */
private constructor(
        /**
         * Internal representation of the enum int type.
         */
        private val m_value: Int, private val m_name: String) : MpxjEnum {
    DAILY(1, "Daily"),
    WEEKLY(4, "Weekly"),
    MONTHLY(8, "Monthly"),
    YEARLY(16, "Yearly");

    /**
     * Accessor method used to retrieve the numeric representation of the enum.
     *
     * @return int representation of the enum
     */
    override val value: Int
        @Override get() = m_value

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return m_name
    }

    companion object {

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Int): RecurrenceType {
            var type = type
            if (type < 1 || type >= TYPE_VALUES.size) {
                type = DAILY.value
            }
            return TYPE_VALUES[type]
        }

        /**
         * Array mapping int types to enums.
         */
        private val TYPE_VALUES = EnumHelper.createTypeArray(RecurrenceType::class.java, 13)
    }
}
