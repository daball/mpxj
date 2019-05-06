/*
 * file:       RelationType.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       10/05/2005
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
 * This class is used to represent a relation type. It provides a mapping
 * between the textual description of a relation type found in an MPX
 * file, and an enumerated representation that can be more easily manipulated
 * programatically.
 */
enum class RelationType
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
    FINISH_FINISH(0, "FF"),
    FINISH_START(1, "FS"),
    START_FINISH(2, "SF"),
    START_START(3, "SS");

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
        fun getInstance(type: Int): RelationType {
            var type = type
            if (type < 0 || type >= TYPE_VALUES.size) {
                type = FINISH_START.value
            }
            return TYPE_VALUES[type]
        }

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Number?): RelationType {
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
        private val TYPE_VALUES = EnumHelper.createTypeArray(RelationType::class.java)
    }
}
