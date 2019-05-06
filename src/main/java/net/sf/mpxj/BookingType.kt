/*
 * file:       BookingType.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2005
 * date:       28/11/2004
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
 * Enumeration representing booking types.
 */
enum class BookingType
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
    COMMITTED(0),
    PROPOSED(1);

    /**
     * Accessor method used to retrieve the numeric representation of the enum.
     *
     * @return int representation of the enum
     */
    override val value: Int
        @Override get() = m_value

    companion object {

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Int): BookingType {
            var type = type
            if (type < 0 || type >= TYPE_VALUES.size) {
                type = PROPOSED.value
            }
            return TYPE_VALUES[type]
        }

        /**
         * Array mapping int types to enums.
         */
        private val TYPE_VALUES = EnumHelper.createTypeArray(BookingType::class.java)
    }
}
