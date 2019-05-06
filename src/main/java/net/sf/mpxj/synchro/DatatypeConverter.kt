/*
 * file:       DatatypeConverter.java
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

import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.Date
import java.util.UUID

import net.sf.mpxj.Duration
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.CharsetHelper
import net.sf.mpxj.common.DateHelper

/**
 * Common data extraction/conversion conversion methods.
 */
internal object DatatypeConverter {
    /**
     * Extract a simple nul-terminated string from a byte array.
     *
     * @param data byte array
     * @param offset start offset
     * @return String instance
     */
    fun getSimpleString(data: ByteArray, offset: Int): String {
        val buffer = StringBuilder()
        var c: Char

        var loop = 0
        while (offset + loop < data.size) {
            c = data[offset + loop].toChar()

            if (c.toInt() == 0) {
                break
            }

            buffer.append(c)
            loop++
        }

        return buffer.toString()
    }

    /**
     * Read an int from a byte array.
     *
     * @param data byte array
     * @param offset start offset
     * @return int value
     */
    fun getInt(data: ByteArray, offset: Int): Int {
        var result = 0
        var i = offset
        var shiftBy = 0
        while (shiftBy < 32) {
            result = result or (data[i] and 0xff shl shiftBy)
            ++i
            shiftBy += 8
        }
        return result
    }

    /**
     * Read a short int from a byte array.
     *
     * @param data byte array
     * @param offset start offset
     * @return int value
     */
    fun getShort(data: ByteArray, offset: Int): Int {
        var result = 0
        var i = offset
        var shiftBy = 0
        while (shiftBy < 16) {
            result = result or (data[i] and 0xff shl shiftBy)
            ++i
            shiftBy += 8
        }
        return result
    }

    /**
     * Read a long int from a byte array.
     *
     * @param data byte array
     * @param offset start offset
     * @return long value
     */
    fun getLong(data: ByteArray, offset: Int): Long {
        var result: Long = 0
        var i = offset
        var shiftBy = 0
        while (shiftBy < 64) {
            result = result or ((data[i] and 0xff).toLong() shl shiftBy)
            ++i
            shiftBy += 8
        }
        return result
    }

    /**
     * Read an int from an input stream.
     *
     * @param is input stream
     * @return int value
     */
    @Throws(IOException::class)
    fun getInt(`is`: InputStream): Int {
        val data = ByteArray(4)
        `is`.read(data)
        return getInt(data, 0)
    }

    /**
     * Read an Integer from an input stream.
     *
     * @param is input stream
     * @return Integer instance
     */
    @Throws(IOException::class)
    fun getInteger(`is`: InputStream): Integer {
        return Integer.valueOf(getInt(`is`))
    }

    /**
     * Read a short int from an input stream.
     *
     * @param is input stream
     * @return int value
     */
    @Throws(IOException::class)
    fun getShort(`is`: InputStream): Int {
        val data = ByteArray(2)
        `is`.read(data)
        return getShort(data, 0)
    }

    /**
     * Read a long int from an input stream.
     *
     * @param is input stream
     * @return long value
     */
    @Throws(IOException::class)
    fun getLong(`is`: InputStream): Long {
        val data = ByteArray(8)
        `is`.read(data)
        return getLong(data, 0)
    }

    /**
     * Read a Synchro string from an input stream.
     *
     * @param is input stream
     * @return String instance
     */
    @Throws(IOException::class)
    fun getString(`is`: InputStream): String? {
        val type = `is`.read()
        if (type != 1) {
            throw IllegalArgumentException("Unexpected string format")
        }

        var charset = CharsetHelper.UTF8

        var length = `is`.read()
        if (length == 0xFF) {
            length = getShort(`is`)
            if (length == 0xFFFE) {
                charset = CharsetHelper.UTF16LE
                length = `is`.read() * 2
            }
        }

        val result: String?
        if (length == 0) {
            result = null
        } else {
            val stringData = ByteArray(length)
            `is`.read(stringData)
            result = String(stringData, charset)
        }
        return result
    }

    /**
     * Retrieve a boolean from an input stream.
     *
     * @param is input stream
     * @return boolean value
     */
    @Throws(IOException::class)
    fun getBoolean(`is`: InputStream): Boolean {
        val value = `is`.read()
        return value != 0
    }

    /**
     * Retrieve a UUID from an input stream.
     *
     * @param is input stream
     * @return UUID instance
     */
    @Throws(IOException::class)
    fun getUUID(`is`: InputStream): UUID {
        val data = ByteArray(16)
        `is`.read(data)

        var long1: Long = 0
        long1 = long1 or ((data[3] and 0xFF).toLong() shl 56)
        long1 = long1 or ((data[2] and 0xFF).toLong() shl 48)
        long1 = long1 or ((data[1] and 0xFF).toLong() shl 40)
        long1 = long1 or ((data[0] and 0xFF).toLong() shl 32)
        long1 = long1 or ((data[5] and 0xFF).toLong() shl 24)
        long1 = long1 or ((data[4] and 0xFF).toLong() shl 16)
        long1 = long1 or ((data[7] and 0xFF).toLong() shl 8)
        long1 = long1 or ((data[6] and 0xFF).toLong() shl 0)

        var long2: Long = 0
        long2 = long2 or ((data[8] and 0xFF).toLong() shl 56)
        long2 = long2 or ((data[9] and 0xFF).toLong() shl 48)
        long2 = long2 or ((data[10] and 0xFF).toLong() shl 40)
        long2 = long2 or ((data[11] and 0xFF).toLong() shl 32)
        long2 = long2 or ((data[12] and 0xFF).toLong() shl 24)
        long2 = long2 or ((data[13] and 0xFF).toLong() shl 16)
        long2 = long2 or ((data[14] and 0xFF).toLong() shl 8)
        long2 = long2 or ((data[15] and 0xFF).toLong() shl 0)

        return UUID(long1, long2)
    }

    /**
     * Read a Synchro date from an input stream.
     *
     * @param is input stream
     * @return Date instance
     */
    @Throws(IOException::class)
    fun getDate(`is`: InputStream): Date? {
        var timeInSeconds = getInt(`is`).toLong()
        if (timeInSeconds == -0x6cbf9001L) {
            return null
        }
        timeInSeconds -= 3600
        timeInSeconds *= 1000
        return DateHelper.getDateFromLong(timeInSeconds)
    }

    /**
     * Read a Synchro time from an input stream.
     *
     * @param is input stream
     * @return Date instance
     */
    @Throws(IOException::class)
    fun getTime(`is`: InputStream): Date? {
        var timeValue = getInt(`is`)
        timeValue -= 86400
        timeValue /= 60
        return DateHelper.getTimeFromMinutesPastMidnight(Integer.valueOf(timeValue))
    }

    /**
     * Retrieve a Synchro Duration from an input stream.
     *
     * @param is input stream
     * @return Duration instance
     */
    @Throws(IOException::class)
    fun getDuration(`is`: InputStream): Duration {
        var durationInSeconds = getInt(`is`).toDouble()
        durationInSeconds /= (60 * 60).toDouble()
        return Duration.getInstance(durationInSeconds, TimeUnit.HOURS)
    }

    /**
     * Retrieve a Double from an input stream.
     *
     * @param is input stream
     * @return Double instance
     */
    @Throws(IOException::class)
    fun getDouble(`is`: InputStream): Double {
        var result = Double.longBitsToDouble(getLong(`is`))
        if (Double.isNaN(result)) {
            result = 0.0
        }
        return Double.valueOf(result)
    }
}
