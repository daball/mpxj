/*
 * file:       MapRow.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       2018-10-11
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

package net.sf.mpxj.synchro

import java.util.Date
import java.util.UUID

import net.sf.mpxj.Duration
import net.sf.mpxj.RelationType
import net.sf.mpxj.common.BooleanHelper

/**
 * Wraps a simple map which contains name value
 * pairs representing the column values
 * from an individual row. Provides type-specific
 * methods to retrieve the column values.
 */
internal class MapRow
/**
 * Constructor.
 *
 * @param map map to be wrapped by this instance
 */
(protected var m_map: Map<String, Object>) {

    /**
     * Retrieve a string value.
     *
     * @param name column name
     * @return string value
     */
    fun getString(name: String): String {
        return getObject(name)
    }

    /**
     * Retrieve an integer value.
     *
     * @param name column name
     * @return integer value
     */
    fun getInteger(name: String): Integer {
        return getObject(name) as Integer
    }

    /**
     * Retrieve a double value.
     *
     * @param name column name
     * @return double value
     */
    fun getDouble(name: String): Double {
        return getObject(name)
    }

    /**
     * Retrieve a relation type value.
     *
     * @param name column name
     * @return relation type value
     */
    fun getRelationType(name: String): RelationType {
        return getObject(name)
    }

    /**
     * Retrieve a boolean value.
     *
     * @param name column name
     * @return boolean value
     */
    fun getBoolean(name: String): Boolean {
        var result = false
        val value = getObject(name) as Boolean
        if (value != null) {
            result = BooleanHelper.getBoolean(value)
        }
        return result
    }

    /**
     * Retrieve a duration value.
     *
     * @param name column name
     * @return duration value
     */
    fun getDuration(name: String): Duration {
        return getObject(name) as Duration
    }

    /**
     * Retrieve a date value.
     *
     * @param name column name
     * @return date value
     */
    fun getDate(name: String): Date {
        return getObject(name) as Date
    }

    /**
     * Retrieve a UUID value.
     *
     * @param name column name
     * @return UUID value
     */
    fun getUUID(name: String): UUID {
        return getObject(name) as UUID
    }

    /**
     * Retrieve row from a nested table.
     *
     * @param name column name
     * @return nested table rows
     */
    @SuppressWarnings("unchecked")
    fun getRows(name: String): List<MapRow> {
        return getObject(name)
    }

    @Override
    fun toString(): String {
        val sb = StringBuilder()

        for (entry in m_map.entrySet()) {

            if (sb.length() !== 0) {
                sb.append(", ")
            }
            sb.append(entry.getKey())
            sb.append('=')
            sb.append(entry.getValue())

        }

        return "[MapRow " + sb.toString() + "]"
    }

    /**
     * Retrieve a value without being specific about its type.
     *
     * @param name column name
     * @return value
     */
    private fun getObject(name: String): Object {
        return m_map[name]
    }
}
