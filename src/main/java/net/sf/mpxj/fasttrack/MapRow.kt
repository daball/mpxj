/*
 * file:       MapRow.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       14/03/2016
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

package net.sf.mpxj.fasttrack

import java.util.Calendar
import java.util.Date
import java.util.UUID

import net.sf.mpxj.Duration
import net.sf.mpxj.common.BooleanHelper
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper

/**
 * Implementation of the Row interface, wrapping a Map.
 */
internal class MapRow
/**
 * Constructor.
 *
 * @param table parent table
 * @param map map to be wrapped by this instance
 */
(private val m_table: FastTrackTable,
 /**
  * Retrieve the internal Map instance used to hold row data.
  *
  * @return Map instance
  */
 val map: Map<FastTrackField, Object>) {

    /**
     * Retrieve a string field.
     *
     * @param type field type
     * @return field data
     */
    fun getString(type: FastTrackField): String {
        return getObject(type)
    }

    /**
     * Retrieve an integer field.
     *
     * @param type field type
     * @return field data
     */
    fun getInteger(type: FastTrackField): Integer {
        return getObject(type) as Integer?
    }

    /**
     * Retrieve a double field.
     *
     * @param type field type
     * @return field data
     */
    fun getDouble(type: FastTrackField): Double {
        return getObject(type)
    }

    /**
     * Retrieve a currency field.
     *
     * @param type field type
     * @return field data
     */
    fun getCurrency(type: FastTrackField): Double {
        return getDouble(type)
    }

    /**
     * Retrieve a boolean field.
     *
     * @param type field type
     * @return field data
     */
    fun getBoolean(type: FastTrackField): Boolean {
        var result = false
        val value = getObject(type)
        if (value != null) {
            result = BooleanHelper.getBoolean(value as Boolean?)
        }
        return result
    }

    /**
     * Retrieve an integer field as an int.
     *
     * @param type field type
     * @return field data
     */
    fun getInt(type: FastTrackField): Int {
        return NumberHelper.getInt(getObject(type) as Number?)
    }

    /**
     * Retrieve a timestamp field.
     *
     * @param dateName field containing the date component
     * @param timeName field containing the time component
     * @return Date instance
     */
    fun getTimestamp(dateName: FastTrackField, timeName: FastTrackField): Date? {
        var result: Date? = null
        val date = getDate(dateName)
        if (date != null) {
            val dateCal = DateHelper.popCalendar(date)
            val time = getDate(timeName)
            if (time != null) {
                val timeCal = DateHelper.popCalendar(time)
                dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                dateCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND))
                dateCal.set(Calendar.MILLISECOND, timeCal.get(Calendar.MILLISECOND))
                DateHelper.pushCalendar(timeCal)
            }

            result = dateCal.getTime()
            DateHelper.pushCalendar(dateCal)
        }

        return result
    }

    /**
     * Retrieve a date field.
     *
     * @param type field type
     * @return Date instance
     */
    fun getDate(type: FastTrackField): Date? {
        return getObject(type) as Date?
    }

    /**
     * Retrieve a duration field.
     *
     * @param type field type
     * @return Duration instance
     */
    fun getDuration(type: FastTrackField): Duration? {
        val value = getObject(type) as Double?
        return if (value == null) null else Duration.getInstance(value.doubleValue(), m_table.durationTimeUnit)
    }

    /**
     * Retrieve a work field.
     *
     * @param type field type
     * @return Duration instance
     */
    fun getWork(type: FastTrackField): Duration? {
        val value = getObject(type) as Double?
        return if (value == null) null else Duration.getInstance(value.doubleValue(), m_table.workTimeUnit)
    }

    /**
     * Retrieve a value from the map.
     *
     * @param type column name
     * @return column value
     */
    fun getObject(type: FastTrackField): Object? {
        return map[type]
    }

    /**
     * Retrieve a UUID field.
     *
     * @param type field type
     * @return UUID instance
     */
    fun getUUID(type: FastTrackField): UUID? {
        var value: String? = getString(type)
        var result: UUID? = null
        if (value != null && !value.isEmpty() && value.length() >= 36) {
            if (value.startsWith("{")) {
                value = value.substring(1, value.length() - 1)
            }
            if (value.length() > 16) {
                value = value.substring(0, 36)
            }
            result = UUID.fromString(value)
        }
        return result
    }
}
