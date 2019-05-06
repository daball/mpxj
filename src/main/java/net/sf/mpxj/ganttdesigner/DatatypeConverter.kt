/*
 * file:       DatatypeConverter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2019
 * date:       10 February 2019
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

package net.sf.mpxj.ganttdesigner

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.TimeUnit

/**
 * Methods to handle data type conversions for Gantt Designer files.
 */
object DatatypeConverter {


    private val TIMESTAMP_FORMAT = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
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

    /**
     * Parse a timestamp value.
     *
     * @param value string representation
     * @return date value
     */
    fun parseTimestamp(value: String?): Date? {
        var result: Date? = null

        if (value != null && value.length() !== 0) {
            try {
                result = TIMESTAMP_FORMAT.get().parse(value)
            } catch (ex: ParseException) {
                // Ignore parse exception
            }

        }

        return result
    }

    /**
     * Print a timestamp value.
     *
     * @param value time value
     * @return time value
     */
    fun printTimestamp(value: Date?): String? {
        return if (value == null) null else TIMESTAMP_FORMAT.get().format(value)
    }

    /**
     * Parse a duration value.
     *
     * @param value duration value
     * @return Duration instance
     */
    fun parseDuration(value: String?): Duration? {
        return if (value == null) null else Duration.getInstance(Double.parseDouble(value), TimeUnit.DAYS)
    }

    /**
     * Print a duration value.
     *
     * @param value Duration instance
     * @return string representation of a duration
     */
    fun printDuration(value: Duration?): String? {
        return if (value == null) null else Double.toString(value!!.getDuration())
    }

    /**
     * Parse a date.
     *
     * @param value string representation of a date
     * @return Date instance
     */
    fun parseDate(value: String?): Date? {
        var result: Date? = null

        if (value != null && value.length() !== 0) {
            try {
                result = DATE_FORMAT.get().parse(value)
            } catch (ex: ParseException) {
                // Ignore parse exception
            }

        }

        return result
    }

    /**
     * Print a date.
     *
     * @param value Date instance
     * @return string representation of a date
     */
    fun printDate(value: Date?): String? {
        return if (value == null) null else DATE_FORMAT.get().format(value)
    }

    /**
     * Parse a percent complete value.
     *
     * @param value sting representation of a percent complete value.
     * @return Double instance
     */
    fun parsePercent(value: String?): Double? {
        return if (value == null) null else Double.valueOf(Double.parseDouble(value) * 100.0)
    }

    /**
     * Print a percent complete value.
     *
     * @param value Double instance
     * @return percent complete value
     */
    fun printPercent(value: Double?): String? {
        return if (value == null) null else Double.toString(value.doubleValue() / 100.0)
    }

    /**
     * Parse a Day value.
     *
     * @param value string representation of a day
     * @return Day instance
     */
    fun parseDay(value: String): Day {
        return Day.getInstance(Integer.parseInt(value) + 1)
    }

    /**
     * Print a day value.
     *
     * @param value Day instance
     * @return string representation of a day
     */
    fun printDay(value: Day): String {
        return Integer.toString(value.getValue() - 1)
    }

}
