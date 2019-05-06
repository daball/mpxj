/*
 * file:       ViewType.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2006
 * date:       27/01/2006
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
 * This class represents the enumeration of the valid types of view.
 */
enum class ViewType
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
    UNKNOWN(0, "UNKNOWN"),
    GANTT_CHART(1, "GANTT_CHART"),
    NETWORK_DIAGRAM(2, "NETWORK_DIAGRAM"),
    RELATIONSHIP_DIAGRAM(3, "RELATIONSHIP_DIAGRAM"),
    TASK_FORM(4, "TASK_FORM"),
    TASK_SHEET(5, "TASK_SHEET"),
    RESOURCE_FORM(6, "RESOURCE_FORM"),
    RESOURCE_SHEET(7, "RESOURCE_SHEET"),
    RESOURCE_GRAPH(8, "RESOURCE_GRAPH"),
    TASK_DETAILS_FORM(10, "TASK_DETAILS_FORM"),
    TASK_NAME_FORM(11, "TASK_NAME_FORM"),
    RESOURCE_NAME_FORM(12, "RESOURCE_NAME_FORM"),
    CALENDAR(13, "CALENDAR"),
    TASK_USAGE(14, "TASK_USAGE"),
    RESOURCE_USAGE(15, "RESOURCE_USAGE");

    /**
     * Accessor method used to retrieve the numeric representation of the enum.
     *
     * @return int representation of the enum
     */
    override val value: Int
        @Override get() = m_value

    /**
     * Retrieve the name of this enum.
     *
     * @return enum name
     */
    val name: String
        get() = m_name

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
        fun getInstance(type: Int): ViewType {
            var type = type
            if (type < 0 || type >= TYPE_VALUES.size) {
                type = UNKNOWN.value
            }
            var result: ViewType? = TYPE_VALUES[type]
            if (result == null) {
                result = UNKNOWN
            }
            return result
        }

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Number?): ViewType {
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
        private val TYPE_VALUES = EnumHelper.createTypeArray(ViewType::class.java, 1)
    }
}
