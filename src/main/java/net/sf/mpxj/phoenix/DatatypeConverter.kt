/*
 * file:       DatatypeConverter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2015
 * date:       28/11/2015
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

package net.sf.mpxj.phoenix

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.EnumMap
import java.util.HashMap
import java.util.UUID

import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.RelationType
import net.sf.mpxj.ResourceType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.DateHelper

/**
 * This class contains methods used to perform the datatype conversions
 * required to read and write Phoenix files.
 */
object DatatypeConverter {

    private val STRING_TO_RESOURCE_TYPE_MAP = HashMap<String, ResourceType>()

    private val RESOURCE_TYPE_TO_STRING_MAP = EnumMap<ResourceType, String>(ResourceType::class.java)

    private val STRING_TO_TIME_UNITS_MAP = HashMap<String, TimeUnit>()

    private val TIME_UNITS_TO_STRING_MAP = EnumMap<TimeUnit, String>(TimeUnit::class.java)

    private val NAME_TO_RELATION_TYPE = HashMap<String, RelationType>()

    private val RELATION_TYPE_TO_NAME = HashMap<RelationType, String>()

    private val NAME_TO_DAY = HashMap<String, Day>()

    private val DAY_TO_NAME = HashMap<Day, String>()

    private val DATE_FORMAT = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            val df = SimpleDateFormat("yyyyMMdd'T'HHmmss")
            df.setLenient(false)
            return df
        }
    }

    /**
     * Convert the Phoenix representation of a UUID into a Java UUID instance.
     *
     * @param value Phoenix UUID
     * @return Java UUID instance
     */
    fun parseUUID(value: String): UUID {
        return UUID.fromString(value)
    }

    /**
     * Retrieve a UUID in the form required by Phoenix.
     *
     * @param guid UUID instance
     * @return formatted UUID
     */
    fun printUUID(guid: UUID): String {
        return guid.toString()
    }

    /**
     * Retrieve an integer in the form required by Phoenix.
     *
     * @param value integer value
     * @return formatted integer
     */
    fun printInteger(value: Integer?): String? {
        return if (value == null) null else value!!.toString()
    }

    /**
     * Convert the Phoenix representation of an integer into a Java Integer instance.
     *
     * @param value Phoenix integer
     * @return Java Integer instance
     */
    fun parseInteger(value: String): Integer {
        return Integer.valueOf(value)
    }

    /**
     * Convert the Phoenix representation of a resource type into a ResourceType instance.
     *
     * @param value Phoenix resource type
     * @return ResourceType instance
     */
    fun parseResourceType(value: String): ResourceType {
        return STRING_TO_RESOURCE_TYPE_MAP.get(value)
    }

    /**
     * Retrieve a resource type in the form required by Phoenix.
     *
     * @param type ResourceType instance
     * @return formatted resource type
     */
    fun printResourceType(type: ResourceType): String {
        return RESOURCE_TYPE_TO_STRING_MAP.get(type)
    }

    /**
     * Convert the Phoenix representation of a task relationship type into a RelationType instance.
     *
     * @param value Phoenix relationship type
     * @return RelationType instance
     */
    fun parseRelationType(value: String): RelationType {
        return NAME_TO_RELATION_TYPE.get(value)
    }

    /**
     * Retrieve a relation type in the form required by Phoenix.
     *
     * @param type RelationType instance
     * @return formatted relation type
     */
    fun printRelationType(type: RelationType): String {
        return RELATION_TYPE_TO_NAME.get(type)
    }

    /**
     * Convert the Phoenix representation of a time unit into a TimeUnit instance.
     *
     * @param value Phoenix time unit
     * @return TimeUnit instance
     */
    fun parseTimeUnits(value: String): TimeUnit {
        return STRING_TO_TIME_UNITS_MAP.get(value)
    }

    /**
     * Retrieve a time unit in the form required by Phoenix.
     *
     * @param type TimeUnit instance
     * @return formatted time unit
     */
    fun printTimeUnits(type: TimeUnit): String {
        return TIME_UNITS_TO_STRING_MAP.get(type)
    }

    /**
     * Retrieve a date time in the form required by Phoenix.
     *
     * @param value Date instance
     * @return formatted date time
     */
    fun printDateTime(value: Date?): String? {
        return if (value == null) null else DATE_FORMAT.get().format(value)
    }

    /**
     * Convert the Phoenix representation of a date time into a Date instance.
     *
     * @param value Phoenix date time
     * @return Date instance
     */
    fun parseDateTime(value: String?): Date? {
        var result: Date? = null

        if (value != null && value.length() !== 0) {
            try {
                result = DATE_FORMAT.get().parse(value)
            } catch (ex: ParseException) {
                // Ignored
            }

        }

        return result
    }

    /**
     * Convert the Phoenix representation of a duration into a Duration instance.
     *
     * @param value Phoenix duration
     * @return Duration instance
     */
    fun parseDuration(value: String?): Duration? {
        var result: Duration? = null
        if (value != null) {
            val split = value.indexOf(' ')
            if (split != -1) {
                val durationValue = Double.parseDouble(value.substring(0, split))
                val durationUnits = parseTimeUnits(value.substring(split + 1))

                result = Duration.getInstance(durationValue, durationUnits)

            }
        }
        return result
    }

    /**
     * Retrieve a duration in the form required by Phoenix.
     *
     * @param duration Duration instance
     * @return formatted duration
     */
    fun printDuration(duration: Duration?): String? {
        var result: String? = null
        if (duration != null) {
            result = duration!!.getDuration() + " " + printTimeUnits(duration!!.getUnits())
        }
        return result
    }

    /**
     * Convert the Phoenix representation of a day into a Day instance.
     *
     * @param value Phoenix day
     * @return Day instance
     */
    fun parseDay(value: String): Day {
        return NAME_TO_DAY.get(value)
    }

    /**
     * Retrieve a finish date time in the form required by Phoenix.
     *
     * @param value Date instance
     * @return formatted date time
     */
    fun printFinishDateTime(value: Date?): String? {
        var value = value
        if (value != null) {
            value = DateHelper.addDays(value, 1)
        }
        return if (value == null) null else DATE_FORMAT.get().format(value)
    }

    /**
     * Convert the Phoenix representation of a finish date time into a Date instance.
     *
     * @param value Phoenix date time
     * @return Date instance
     */
    fun parseFinishDateTime(value: String): Date? {
        var result = parseDateTime(value)
        if (result != null) {
            result = DateHelper.addDays(result, -1)
        }
        return result
    }

    /**
     * Retrieve a day in the form required by Phoenix.
     *
     * @param value Day instance
     * @return formatted day
     */
    fun printDay(value: Day): String {
        return DAY_TO_NAME.get(value)
    }

    init {
        STRING_TO_RESOURCE_TYPE_MAP.put("Labor", ResourceType.WORK)
        STRING_TO_RESOURCE_TYPE_MAP.put("Non-Labor", ResourceType.MATERIAL)
    }

    init {
        RESOURCE_TYPE_TO_STRING_MAP.put(ResourceType.WORK, "Labor")
        RESOURCE_TYPE_TO_STRING_MAP.put(ResourceType.MATERIAL, "Non-Labor")
        RESOURCE_TYPE_TO_STRING_MAP.put(ResourceType.COST, "Non-Labor")
    }

    init {
        STRING_TO_TIME_UNITS_MAP.put("Days", TimeUnit.DAYS)
        STRING_TO_TIME_UNITS_MAP.put("days", TimeUnit.DAYS)
        STRING_TO_TIME_UNITS_MAP.put("day", TimeUnit.DAYS)
    }

    init {
        TIME_UNITS_TO_STRING_MAP.put(TimeUnit.DAYS, "Days")
    }

    init {
        NAME_TO_RELATION_TYPE.put("FinishToFinish", RelationType.FINISH_FINISH)
        NAME_TO_RELATION_TYPE.put("FinishToStart", RelationType.FINISH_START)
        NAME_TO_RELATION_TYPE.put("StartToFinish", RelationType.START_FINISH)
        NAME_TO_RELATION_TYPE.put("StartToStart", RelationType.START_START)
    }

    init {
        RELATION_TYPE_TO_NAME.put(RelationType.FINISH_FINISH, "FinishToFinish")
        RELATION_TYPE_TO_NAME.put(RelationType.FINISH_START, "FinishToStart")
        RELATION_TYPE_TO_NAME.put(RelationType.START_FINISH, "StartToFinish")
        RELATION_TYPE_TO_NAME.put(RelationType.START_START, "StartToStart")
    }

    init {
        NAME_TO_DAY.put("Mon", Day.MONDAY)
        NAME_TO_DAY.put("Tue", Day.TUESDAY)
        NAME_TO_DAY.put("Wed", Day.WEDNESDAY)
        NAME_TO_DAY.put("Thu", Day.THURSDAY)
        NAME_TO_DAY.put("Fri", Day.FRIDAY)
        NAME_TO_DAY.put("Sat", Day.SATURDAY)
        NAME_TO_DAY.put("Sun", Day.SUNDAY)
    }

    init {
        DAY_TO_NAME.put(Day.MONDAY, "Mon")
        DAY_TO_NAME.put(Day.TUESDAY, "Tue")
        DAY_TO_NAME.put(Day.WEDNESDAY, "Wed")
        DAY_TO_NAME.put(Day.THURSDAY, "Thu")
        DAY_TO_NAME.put(Day.FRIDAY, "Fri")
        DAY_TO_NAME.put(Day.SATURDAY, "Sat")
        DAY_TO_NAME.put(Day.SUNDAY, "Sun")
    }
}
