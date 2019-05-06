/*
 * file:       DatatypeConverter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       9 July 2018
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

package net.sf.mpxj.conceptdraw

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap

import net.sf.mpxj.CurrencySymbolPosition
import net.sf.mpxj.Day
import net.sf.mpxj.Priority
import net.sf.mpxj.RelationType
import net.sf.mpxj.ResourceType
import net.sf.mpxj.TaskType
import net.sf.mpxj.TimeUnit

/**
 * This class contains methods used to perform the datatype conversions
 * required to read and write ConceptDraw PROJECT files.
 */
object DatatypeConverter {

    private val MAP_TO_CURRENCY_SYMBOL_POSITION = HashMap<String, CurrencySymbolPosition>()

    private val MAP_FROM_CURRENCY_SYMBOL_POSITION = HashMap<CurrencySymbolPosition, String>()

    private val MAP_TO_TIME_UNIT = HashMap<String, TimeUnit>()

    private val MAP_TO_RESOURCE_TYPE = HashMap<String, ResourceType>()

    private val MAP_TO_PRIORITY = HashMap<String, Priority>()

    private val MAP_TO_TASK_TYPE = HashMap<String, TaskType>()

    private val MAP_TO_RELATION_TYPE = HashMap<String, RelationType>()

    private val TIME_FORMAT = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            val df = SimpleDateFormat("HH:mm:ss")
            df.setLenient(false)
            return df
        }
    }

    private val DATE_FORMAT = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            val df = SimpleDateFormat("yyyy-MM-dd")
            df.setLenient(false)
            return df
        }
    }

    private val DATE_TIME_FORMAT = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            df.setLenient(false)
            return df
        }
    }

    /**
     * Parse an integer value.
     *
     * @param value string representation
     * @return Integer instance
     */
    fun parseInteger(value: String): Integer {
        return Integer.valueOf(value)
    }

    /**
     * Print an integer value.
     *
     * @param value integer value
     * @return string representation
     */
    fun printInteger(value: Integer): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a double value.
     *
     * @param value String representation.
     * @return Double instance
     */
    fun parseDouble(value: String): Double {
        return Double.valueOf(value)
    }

    /**
     * Print a double value.
     * @param value Double instance
     * @return string representation.
     */
    fun printDouble(value: Double): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a percentage value.
     *
     * @param value String representation
     * @return Double instance
     */
    fun parsePercent(value: String): Double {
        return Double.valueOf(Double.parseDouble(value) * 100.0)
    }

    /**
     * Print a percentage value.
     *
     * @param value Double instance
     * @return String representation
     */
    fun printPercent(value: Double): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a duration in minutes form a number of hours.
     *
     * @param value String representation
     * @return Integer instance
     */
    fun parseMinutesFromHours(value: String?): Integer? {
        var result: Integer? = null
        if (value != null) {
            result = Integer.valueOf(Integer.parseInt(value) * 60)
        }
        return result
    }

    /**
     * Print a duration in hours from a number of minutes.
     *
     * @param value String representation
     * @return String representation
     */
    fun printHoursFromMinutes(value: Integer): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a currency symbol position from a string representation.
     *
     * @param value String representation
     * @return CurrencySymbolPosition instance
     */
    fun parseCurrencySymbolPosition(value: String): CurrencySymbolPosition {
        var result = MAP_TO_CURRENCY_SYMBOL_POSITION.get(value)
        result = if (result == null) CurrencySymbolPosition.BEFORE_WITH_SPACE else result
        return result
    }

    /**
     * Print a currency symbol position.
     *
     * @param value CurrencySymbolPosition instance
     * @return String representation
     */
    fun printCurrencySymbolPosition(value: CurrencySymbolPosition): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a Day.
     *
     * @param value String representation
     * @return Day instance
     */
    fun parseDay(value: String): Day {
        return Day.getInstance(Integer.parseInt(value) + 1)
    }

    /**
     * Print a Day.
     *
     * @param value Day instance
     * @return String representation
     */
    fun printDay(value: Day): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a time value.
     *
     * @param value String representation
     * @return Date instance
     */
    fun parseTime(value: String?): Date? {
        var result: Date? = null

        try {
            if (value != null && !value.isEmpty()) {
                result = TIME_FORMAT.get().parse(value)
            }
        } catch (ex: ParseException) {
            // Ignore
        }

        return result
    }

    /**
     * Print a time value.
     *
     * @param value Date instance
     * @return String representation
     */
    fun printTime(value: Date): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a date value.
     *
     * @param value String representation
     * @return Date instance
     */
    fun parseDate(value: String?): Date? {
        var result: Date? = null

        try {
            if (value != null && !value.isEmpty()) {
                result = DATE_FORMAT.get().parse(value)
            }
        } catch (ex: ParseException) {
            // Ignore
        }

        return result
    }

    /**
     * Print a date value.
     *
     * @param value Date instance
     * @return String representation
     */
    fun printDate(value: Date): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a date time value.
     *
     * @param value String representation
     * @return Date instance
     */
    fun parseDateTime(value: String?): Date? {
        var result: Date? = null

        try {
            if (value != null && !value.isEmpty()) {
                result = DATE_TIME_FORMAT.get().parse(value)
            }
        } catch (ex: ParseException) {
            // Ignore
        }

        return result
    }

    /**
     * Print a date time value.
     *
     * @param value Date instance
     * @return String representation
     */
    fun printDateTime(value: Date): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a time unit value.
     *
     * @param value String representation
     * @return TimeUnit instance
     */
    fun parseTimeUnit(value: String): TimeUnit {
        return MAP_TO_TIME_UNIT.get(value)
    }

    /**
     * Print a time unit value.
     *
     * @param value TimeUnit instance
     * @return String representation
     */
    fun printTimeUnit(value: TimeUnit): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a resource type value.
     *
     * @param value String representation
     * @return ResourceType instance
     */
    fun parseResourceType(value: String): ResourceType {
        return MAP_TO_RESOURCE_TYPE.get(value)
    }

    /**
     * Print a resource type value.
     *
     * @param value ResourceType instance
     * @return String representation
     */
    fun printResourceType(value: ResourceType): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a priority value.
     *
     * @param value String representation
     * @return Priority instance
     */
    fun parsePriority(value: String): Priority {
        return MAP_TO_PRIORITY.get(value)
    }

    /**
     * Print a priority value.
     *
     * @param value Priority instance
     * @return String representation
     */
    fun printPriority(value: Priority): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a task type value.
     *
     * @param value String representation
     * @return TaskType instance
     */
    fun parseTaskType(value: String): TaskType {
        return MAP_TO_TASK_TYPE.get(value)
    }

    /**
     * Print a task type value.
     *
     * @param value TaskType instance
     * @return String representation
     */
    fun printTaskType(value: TaskType): String {
        throw UnsupportedOperationException()
    }

    /**
     * Parse a relation type value.
     *
     * @param value String representation
     * @return RelationType instance
     */
    fun parseRelationType(value: String): RelationType {
        return MAP_TO_RELATION_TYPE.get(value)
    }

    /**
     * Print a relation type value.
     *
     * @param value RelationType instance
     * @return string representation
     */
    fun printRelationType(value: RelationType): String {
        throw UnsupportedOperationException()
    }

    init {
        MAP_TO_CURRENCY_SYMBOL_POSITION.put("0", CurrencySymbolPosition.BEFORE)
        MAP_TO_CURRENCY_SYMBOL_POSITION.put("1", CurrencySymbolPosition.AFTER)
        MAP_TO_CURRENCY_SYMBOL_POSITION.put("2", CurrencySymbolPosition.BEFORE_WITH_SPACE)
        MAP_TO_CURRENCY_SYMBOL_POSITION.put("3", CurrencySymbolPosition.AFTER_WITH_SPACE)
    }

    init {
        MAP_FROM_CURRENCY_SYMBOL_POSITION.put(CurrencySymbolPosition.BEFORE, "0")
        MAP_FROM_CURRENCY_SYMBOL_POSITION.put(CurrencySymbolPosition.AFTER, "1")
        MAP_FROM_CURRENCY_SYMBOL_POSITION.put(CurrencySymbolPosition.BEFORE_WITH_SPACE, "2")
        MAP_FROM_CURRENCY_SYMBOL_POSITION.put(CurrencySymbolPosition.AFTER_WITH_SPACE, "3")
    }

    init {
        MAP_TO_TIME_UNIT.put("0", TimeUnit.MINUTES)
        MAP_TO_TIME_UNIT.put("1", TimeUnit.HOURS)
        MAP_TO_TIME_UNIT.put("2", TimeUnit.DAYS)
        MAP_TO_TIME_UNIT.put("3", TimeUnit.WEEKS)
        MAP_TO_TIME_UNIT.put("4", TimeUnit.MONTHS)
    }

    init {
        MAP_TO_RESOURCE_TYPE.put("0", ResourceType.MATERIAL)
        MAP_TO_RESOURCE_TYPE.put("1", ResourceType.WORK)
        MAP_TO_RESOURCE_TYPE.put("work", ResourceType.WORK)
        MAP_TO_RESOURCE_TYPE.put("material", ResourceType.MATERIAL)
        MAP_TO_RESOURCE_TYPE.put("cost", ResourceType.COST)
    }

    init {
        MAP_TO_PRIORITY.put("veryLow", Priority.getInstance(Priority.LOWEST))
        MAP_TO_PRIORITY.put("low", Priority.getInstance(Priority.LOW))
        MAP_TO_PRIORITY.put("normal", Priority.getInstance(Priority.MEDIUM))
        MAP_TO_PRIORITY.put("high", Priority.getInstance(Priority.HIGH))
        MAP_TO_PRIORITY.put("veryHigh", Priority.getInstance(Priority.HIGHEST))
    }

    init {
        MAP_TO_TASK_TYPE.put("fixedDuration", TaskType.FIXED_DURATION)
        MAP_TO_TASK_TYPE.put("fixedUnits", TaskType.FIXED_UNITS)
        MAP_TO_TASK_TYPE.put("fixedWork", TaskType.FIXED_WORK)
    }

    init {
        MAP_TO_RELATION_TYPE.put("0", RelationType.START_START)
        MAP_TO_RELATION_TYPE.put("1", RelationType.START_FINISH)
        MAP_TO_RELATION_TYPE.put("2", RelationType.FINISH_START)
        MAP_TO_RELATION_TYPE.put("3", RelationType.FINISH_FINISH)

    }
}
