/*
 * file:       MapRow.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2016
 * date:       17/11/2016
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

package net.sf.mpxj.merlin

import java.util.Calendar
import java.util.Date
import java.util.UUID

import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.RelationType
import net.sf.mpxj.ResourceType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.BooleanHelper
import net.sf.mpxj.common.DateHelper
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

    init {
        this.map = map
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getString(name: String): String? {
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
    override fun getInteger(name: String): Integer? {
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
            if (result !is Double) {
                if (result is ByteArray) {
                    result = Double.valueOf(String(result as ByteArray?))
                } else {
                    result = Double.valueOf((result as Number).doubleValue())
                }
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
    override fun getTimestamp(name: String): Date? {
        val result: Date?
        // They are stored as seconds since Jan 1st, 2001 00:00
        val value = getInteger(name)
        if (value == null) {
            result = null
        } else {
            result = Date(TIMESTAMP_EPOCH + value!!.longValue() * 1000)
        }
        return result
    }

    @Override
    override fun getDate(name: String): Date? {
        val result: Date?
        // They are stored as days since Jan 7th, 2001 00:00
        val value = getInteger(name)
        if (value == null) {
            result = null
        } else {
            val cal = DateHelper.popCalendar(DATE_EPOCH)
            cal.add(Calendar.DAY_OF_YEAR, value!!.intValue())
            result = cal.getTime()
            DateHelper.pushCalendar(cal)
        }
        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getDuration(name: String): Duration? {
        val result: Duration?
        val value = getString(name)
        if (value == null) {
            result = null
        } else {
            result = parseDuration(value)
        }
        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getWork(name: String): Duration? {
        val result: Duration?
        val value = getString(name)
        if (value == null) {
            result = null
        } else {
            result = parseDuration(value)
        }
        return result
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
    override fun getUUID(name: String): UUID {
        var value = getString(name)
        value = value!!.replace("-", "+").replace("_", "/")

        val data = javax.xml.bind.DatatypeConverter.parseBase64Binary(value!! + "==")
        var msb: Long = 0
        var lsb: Long = 0

        for (i in 0..7) {
            msb = msb shl 8 or (data[i] and 0xff)
        }

        for (i in 8..15) {
            lsb = lsb shl 8 or (data[i] and 0xff)
        }

        return UUID(msb, lsb)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getRelationType(name: String): RelationType {
        val result: RelationType
        val type = getInt(name)

        when (type) {
            1 -> {
                result = RelationType.START_START
            }

            2 -> {
                result = RelationType.FINISH_FINISH
            }

            3 -> {
                result = RelationType.START_FINISH
            }

            0 -> {
                result = RelationType.FINISH_START
            }
            else -> {
                result = RelationType.FINISH_START
            }
        }

        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getResourceType(name: String): ResourceType {
        val result: ResourceType
        val value = getInteger(name)
        if (value == null) {
            result = ResourceType.WORK
        } else {
            if (value!!.intValue() === 1) {
                result = ResourceType.MATERIAL
            } else {
                result = ResourceType.WORK
            }
        }

        return result
    }

    /**
     * Convert the string representation of a duration to a Duration instance.
     *
     * @param value string representation of a duration
     * @return Duration instance
     */
    private fun parseDuration(value: String): Duration {
        //
        // Let's assume that we always receive well-formed values.
        //
        var unitsLength = 1
        var unitsChar = value.charAt(value.length() - unitsLength)

        //
        // Handle an estimated duration
        //
        if (unitsChar == '?') {
            unitsLength = 2
            unitsChar = value.charAt(value.length() - unitsLength)
        }

        var durationValue = Double.parseDouble(value.substring(0, value.length() - unitsLength))

        //
        // Note that we don't handle 'u' the material type here
        //
        val durationUnits: TimeUnit
        when (unitsChar) {
            's' -> {
                durationUnits = TimeUnit.MINUTES
                durationValue /= 60.0
            }

            'm' -> {
                durationUnits = TimeUnit.MINUTES
            }

            'h' -> {
                durationUnits = TimeUnit.HOURS
            }

            'w' -> {
                durationUnits = TimeUnit.WEEKS
            }

            'M' -> {
                durationUnits = TimeUnit.MONTHS
            }

            'q' -> {
                durationUnits = TimeUnit.MONTHS
                durationValue *= 3.0
            }

            'y' -> {
                durationUnits = TimeUnit.YEARS
            }

            'f' -> {
                durationUnits = TimeUnit.PERCENT
            }

            'd' -> {
                durationUnits = TimeUnit.DAYS
            }
            else -> {
                durationUnits = TimeUnit.DAYS
            }
        }

        return Duration.getInstance(durationValue, durationUnits)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getDay(name: String): Day? {
        var result: Day? = null
        val value = getInteger(name)
        if (value != null) {
            result = Day.getInstance(value!!.intValue() + 1)
        }
        return result
    }

    companion object {

        /**
         * 01/01/2001 00:00.
         */
        private val TIMESTAMP_EPOCH = 978307200000L

        /**
         * 07/01/2001 00:00.
         */
        private val DATE_EPOCH = 978825600000L
    }
}
