/*
 * file:       MapRow.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2012
 * date:       29/04/2012
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

package net.sf.mpxj.asta

import java.util.ArrayList
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
(map: Map<String, Object>) : Row {
    /**
     * Retrieve the internal Map instance used to hold row data.
     *
     * @return Map instance
     */
    var map: Map<String, Object>
        protected set
    /**
     * {@inheritDoc}
     */
    @get:Override
    override val childRows: List<Row> = ArrayList<Row>()

    init {
        this.map = map
    }

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
        return Duration.getInstance(NumberHelper.getDouble(getDouble(name)), TimeUnit.HOURS)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getWork(name: String): Duration {
        return Duration.getInstance(NumberHelper.getDouble(getDouble(name)) / 3600, TimeUnit.HOURS)
    }

    /**
     * Retrieve a value from the map.
     *
     * @param name column name
     * @return column value
     */
    fun getObject(name: String): Object? {
        return map[name]
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun addChild(row: Row) {
        childRows.add(row)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun merge(row: Row, prefix: String) {
        val otherMap = (row as MapRow).map
        for (entry in otherMap.entrySet()) {
            map.put(prefix + entry.getKey(), entry.getValue())
        }
    }
}
