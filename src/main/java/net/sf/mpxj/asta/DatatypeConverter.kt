/*
 * file:       DatatypeConverter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2016
 * date:       09/06/2016
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

import java.text.DateFormat
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

import net.sf.mpxj.common.DateHelper

/**
 * Methods for handling Asta data types.
 */
internal object DatatypeConverter {

    private val TIMESTAMP_FORMAT = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            return SimpleDateFormat("yyyyMMdd HHmmss")
        }
    }

    private val DATE_FORMAT1 = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            return SimpleDateFormat("yyyyMMdd 0")
        }
    }

    private val DATE_FORMAT2 = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            return SimpleDateFormat("yyyyMMdd")
        }
    }

    private val TIME_FORMAT = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            return SimpleDateFormat("HHmmss")
        }
    }

    private val DOUBLE_FORMAT = object : ThreadLocal<DecimalFormat>() {
        @Override
        protected fun initialValue(): DecimalFormat {
            return DecimalFormat("#.#E0")
        }
    }

    private val JAVA_EPOCH = -2208988800000L
    private val ASTA_EPOCH = 2415021L
    /**
     * Parse a string.
     *
     * @param value string representation
     * @return String value
     */
    fun parseString(value: String?): String? {
        var value = value
        if (value != null) {
            // Strip angle brackets if present
            if (!value.isEmpty() && value.charAt(0) === '<') {
                value = value.substring(1, value.length() - 1)
            }

            // Strip quotes if present
            if (!value.isEmpty() && value.charAt(0) === '"') {
                value = value.substring(1, value.length() - 1)
            }
        }
        return value
    }

    /**
     * Parse the string representation of a double.
     *
     * @param value string representation
     * @return Java representation
     * @throws ParseException
     */
    @Throws(ParseException::class)
    fun parseDouble(value: String?): Number? {
        var value = value

        var result: Number? = null
        value = parseString(value)

        // If we still have a value
        if (value != null && !value.isEmpty() && !value.equals("-1 -1")) {
            val index = value.indexOf("E+")
            if (index != -1) {
                value = value.substring(0, index) + 'E' + value.substring(index + 2, value.length())
            }

            if (value.indexOf('E') !== -1) {
                result = DOUBLE_FORMAT.get().parse(value)
            } else {
                result = Double.valueOf(value)
            }
        }

        return result
    }

    /**
     * Parse a string representation of a Boolean value.
     *
     * @param value string representation
     * @return Boolean value
     */
    @Throws(ParseException::class)
    fun parseBoolean(value: String): Boolean? {
        var result: Boolean? = null
        val number = parseInteger(value)
        if (number != null) {
            result = if (number!!.intValue() === 0) Boolean.FALSE else Boolean.TRUE
        }

        return result
    }

    /**
     * Parse a string representation of an Integer value.
     *
     * @param value string representation
     * @return Integer value
     */
    @Throws(ParseException::class)
    fun parseInteger(value: String): Integer? {
        var result: Integer? = null

        if (value.length() > 0 && value.indexOf(' ') === -1) {
            if (value.indexOf('.') === -1) {
                result = Integer.valueOf(value)
            } else {
                val n = DatatypeConverter.parseDouble(value)
                result = Integer.valueOf(n!!.intValue())
            }
        }

        return result
    }

    /**
     * Parse the string representation of a timestamp.
     *
     * @param value string representation
     * @return Java representation
     */
    fun parseEpochTimestamp(value: String): Date? {
        var value = value
        var result: Date? = null

        if (value.length() > 0) {
            if (!value.equals("-1 -1")) {
                val cal = DateHelper.popCalendar(JAVA_EPOCH)

                val index = value.indexOf(' ')
                if (index == -1) {
                    if (value.length() < 6) {
                        value = "000000$value"
                        value = value.substring(value.length() - 6)
                    }

                    val hours = Integer.parseInt(value.substring(0, 2))
                    val minutes = Integer.parseInt(value.substring(2, 4))
                    val seconds = Integer.parseInt(value.substring(4))

                    cal.set(Calendar.HOUR, hours)
                    cal.set(Calendar.MINUTE, minutes)
                    cal.set(Calendar.SECOND, seconds)
                } else {
                    val astaDays = Long.parseLong(value.substring(0, index))
                    val astaSeconds = Integer.parseInt(value.substring(index + 1))

                    cal.add(Calendar.DAY_OF_YEAR, (astaDays - ASTA_EPOCH).toInt())
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.HOUR, 0)
                    cal.add(Calendar.SECOND, astaSeconds)
                }

                result = cal.getTime()
                DateHelper.pushCalendar(cal)
            }
        }

        return result
    }

    /**
     * Parse a timestamp value.
     *
     * @param value timestamp as String
     * @return timestamp as Date
     */
    @Throws(ParseException::class)
    fun parseBasicTimestamp(value: String): Date? {
        var value = value
        var result: Date? = null

        if (value.length() > 0) {
            if (!value.equals("-1 -1") && !value.equals("0")) {
                val df: DateFormat
                if (value.endsWith(" 0")) {
                    df = DATE_FORMAT1.get()
                } else {
                    if (value.indexOf(' ') === -1) {
                        df = DATE_FORMAT2.get()
                    } else {
                        df = TIMESTAMP_FORMAT.get()
                        val timeIndex = value.indexOf(' ') + 1
                        if (timeIndex + 6 > value.length()) {
                            val time = value.substring(timeIndex)
                            value = value.substring(0, timeIndex) + "0" + time
                        }
                    }
                }

                result = df.parse(value)
            }
        }

        //System.out.println(value + "=>" + result);
        return result
    }

    /**
     * Parse a time value.
     *
     * @param value time as String
     * @return time as Date
     */
    @Throws(ParseException::class)
    fun parseBasicTime(value: String): Date? {
        var value = value
        var result: Date? = null

        if (value.length() > 0) {
            if (!value.equals("0")) {
                value = "000000$value"
                value = value.substring(value.length() - 6)
                result = TIME_FORMAT.get().parse(value)
            }
        }

        return result
    }
}
