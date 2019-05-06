/*
 * file:       MapRow.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       08-Feb-2006
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

package net.sf.mpxj.mpd

import java.util.Date

import net.sf.mpxj.Duration
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.BooleanHelper
import net.sf.mpxj.common.NumberHelper

/**
 * Implementation of the Row interface, wrapping a Map.
 */
internal open class MapRow
/**
 * Constructor.
 *
 * @param map map to be wrapped by this instance
 */
(protected var m_map: Map<String, Object>) : Row {

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getString(name: String): String {
        val value = getObject(name)
        val result: String
        if (value is ByteArray) {
            result = String(value as ByteArray?)
        } else {
            result = value
        }
        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getInteger(name: String): Integer {
        var result = getObject(name)
        if (result != null) {
            if (result is Integer == false) {
                result = Integer.valueOf((result as Number).intValue())
            }
        }
        return result as Integer?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getDouble(name: String): Double {
        var result = getObject(name)
        if (result != null) {
            if (result is Double == false) {
                result = Double.valueOf((result as Number).doubleValue())
            }
        }
        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getCurrency(name: String): Double? {
        var value: Double? = getDouble(name)
        if (value != null) {
            value = Double.valueOf(value.doubleValue() / 100)
        }
        return value
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getBoolean(name: String): Boolean {
        var result = false
        val value = getObject(name)
        if (value != null) {
            if (value is Boolean) {
                result = BooleanHelper.getBoolean(value as Boolean?)
            } else {
                result = (value as Number).intValue() === 1
            }
        }
        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getInt(name: String): Int {
        return NumberHelper.getInt(getObject(name) as Number?)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getDate(name: String): Date {
        return getObject(name) as Date?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getDuration(name: String): Duration {
        return Duration.getInstance(NumberHelper.getDouble(getDouble(name)) / 60000, TimeUnit.HOURS)
    }

    /**
     * Retrieve a value from the map, ensuring that a key exists in the map
     * with the specified name.
     *
     * @param name column name
     * @return column value
     */
    private fun getObject(name: String): Object? {
        if (m_map.containsKey(name) === false) {
            throw IllegalArgumentException("Invalid column name $name")
        }

        return m_map[name]
    }
}
