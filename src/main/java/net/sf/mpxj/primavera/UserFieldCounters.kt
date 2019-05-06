/*
 * file:       UserFieldCounters.java
 * author:     Mario Fuentes
 * copyright:  (c) Packwood Software 2013
 * date:       22/03/2010
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

package net.sf.mpxj.primavera

import java.util.HashMap

import net.sf.mpxj.FieldType
import net.sf.mpxj.common.NumberHelper

/**
 * Simple container holding counters used to generate field names
 * for user defined field types.
 */

internal class UserFieldCounters {

    private val m_counters = HashMap<String, Integer>()
    private val m_names = arrayOfNulls<Array<String>>(UserFieldDataType.values().size)

    /**
     * Constructor.
     */
    init {
        for (type in UserFieldDataType.values()) {
            m_names[type.ordinal()] = type.defaultFieldNames
        }
    }

    /**
     * Allow the caller to override the default field name assigned
     * to a user defined data type.
     *
     * @param type target type
     * @param fieldNames field names overriding the default
     */
    fun setFieldNamesForType(type: UserFieldDataType, fieldNames: Array<String>) {
        m_names[type.ordinal()] = fieldNames
    }

    /**
     * Generate the next available field for a user defined field.
     *
     * @param <E> field type class
     * @param clazz class of the desired field enum
     * @param type user defined field type.
     * @return field of specified type
    </E> */
    fun <E : Enum<E>> nextField(clazz: Class<E>, type: UserFieldDataType): E where E : FieldType {
        for (name in m_names[type.ordinal()]) {
            val i = NumberHelper.getInt(m_counters.get(name)) + 1
            try {
                val e = Enum.valueOf(clazz, name + i)
                m_counters.put(name, Integer.valueOf(i))
                return e
            } catch (ex: IllegalArgumentException) {
                // try the next name
            }

        }

        // no more fields available
        throw IllegalArgumentException("No fields for type $type available")
    }

    /**
     * Reset the counters ready to process a new project.
     */
    fun reset() {
        m_counters.clear()
    }
}
