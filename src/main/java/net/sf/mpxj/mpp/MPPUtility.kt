/*
 * file:       MPPUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       05/01/2003
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

package net.sf.mpxj.mpp

import java.awt.Color
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Calendar
import java.util.Date
import java.util.UUID

import net.sf.mpxj.CurrencySymbolPosition
import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.ByteArrayHelper
import net.sf.mpxj.common.CharsetHelper
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper

/**
 * This class provides common functionality used by each of the classes
 * that read the different sections of the MPP file.
 */
object MPPUtility {

    /**
     * The mask used by Project to hide the password. The data must first
     * be decoded using the XOR key and then the password can be read by reading
     * the characters in given order starting with 1 and going up to 16.
     *
     * 00000: 00 00 04 00 00 00 05 00 07 00 12 00 10 00 06 00
     * 00016: 14 00 00 00 00 00 08 00 16 00 00 00 00 00 02 00
     * 00032: 00 00 15 00 00 00 11 00 00 00 00 00 00 00 09 00
     * 00048: 03 00 00 00 00 00 00 00 00 00 00 00 01 00 13 00
     */
    private val PASSWORD_MASK = intArrayOf(60, 30, 48, 2, 6, 14, 8, 22, 44, 12, 38, 10, 62, 16, 34, 24)

    private val MINIMUM_PASSWORD_DATA_LENGTH = 64

    /**
     * Epoch date for MPP date calculation is 31/12/1983. This constant
     * is that date expressed in milliseconds using the Java date epoch.
     */
    private val EPOCH = 441676800000L

    /**
     * Epoch Date as a Date instance.
     */
    /**
     * Get the epoch date.
     *
     * @return epoch date.
     */
    val epochDate = DateHelper.getTimestampFromLong(EPOCH)

    /**
     * Mask used to remove flags from the duration units field.
     */
    private val DURATION_UNITS_MASK = 0x1F

    /**
     * This method decodes a byte array with the given encryption code
     * using XOR encryption.
     *
     * @param data Source data
     * @param encryptionCode Encryption code
     */
    fun decodeBuffer(data: ByteArray, encryptionCode: Byte) {
        for (i in data.indices) {
            data[i] = (data[i] xor encryptionCode).toByte()
        }
    }

    /**
     * Decode the password from the given data. Will decode the data block as well.
     *
     * @param data encrypted data block
     * @param encryptionCode encryption code
     *
     * @return password
     */
    fun decodePassword(data: ByteArray, encryptionCode: Byte): String? {
        val result: String?

        if (data.size < MINIMUM_PASSWORD_DATA_LENGTH) {
            result = null
        } else {
            MPPUtility.decodeBuffer(data, encryptionCode)

            val buffer = StringBuilder()
            var c: Char

            for (i in PASSWORD_MASK.indices) {
                val index = PASSWORD_MASK[i]
                c = data[index].toChar()

                if (c.toInt() == 0) {
                    break
                }
                buffer.append(c)
            }

            result = buffer.toString()
        }

        return result
    }

    /**
     * This method extracts a portion of a byte array and writes it into
     * another byte array.
     *
     * @param data Source data
     * @param offset Offset into source data
     * @param size Required size to be extracted from the source data
     * @param buffer Destination buffer
     * @param bufferOffset Offset into destination buffer
     */
    fun getByteArray(data: ByteArray, offset: Int, size: Int, buffer: ByteArray, bufferOffset: Int) {
        System.arraycopy(data, offset, buffer, bufferOffset, size)
    }

    /**
     * This method reads a single byte from the input array.
     *
     * @param data byte array of data
     * @param offset offset of byte data in the array
     * @return byte value
     */
    fun getByte(data: ByteArray, offset: Int): Int {
        return data[offset] and 0xFF
    }

    /**
     * This method reads a two byte integer from the input array.
     *
     * @param data the input array
     * @param offset offset of integer data in the array
     * @return integer value
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
     * This method reads a four byte integer from the input array.
     *
     * @param data the input array
     * @param offset offset of integer data in the array
     * @return integer value
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
     * This method reads an eight byte integer from the input array.
     *
     * @param data the input array
     * @param offset offset of integer data in the array
     * @return integer value
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
     * This method reads a six byte long from the input array.
     *
     * @param data the input array
     * @param offset offset of integer data in the array
     * @return integer value
     */
    fun getLong6(data: ByteArray, offset: Int): Long {
        var result: Long = 0
        var i = offset
        var shiftBy = 0
        while (shiftBy < 48) {
            result = result or ((data[i] and 0xff).toLong() shl shiftBy)
            ++i
            shiftBy += 8
        }
        return result
    }

    /**
     * This method reads an eight byte double from the input array.
     *
     * @param data the input array
     * @param offset offset of double data in the array
     * @return double value
     */
    fun getDouble(data: ByteArray, offset: Int): Double {
        var result = Double.longBitsToDouble(getLong(data, offset))
        if (Double.isNaN(result)) {
            result = 0.0
        }
        return result
    }

    /**
     * Reads a UUID/GUID from a data block.
     *
     * @param data data block
     * @param offset offset into the data block
     * @return UUID instance
     */
    fun getGUID(data: ByteArray?, offset: Int): UUID? {
        var result: UUID? = null
        if (data != null && data.size > 15) {
            var long1: Long = 0
            long1 = long1 or ((data[offset + 3] and 0xFF).toLong() shl 56)
            long1 = long1 or ((data[offset + 2] and 0xFF).toLong() shl 48)
            long1 = long1 or ((data[offset + 1] and 0xFF).toLong() shl 40)
            long1 = long1 or ((data[offset + 0] and 0xFF).toLong() shl 32)
            long1 = long1 or ((data[offset + 5] and 0xFF).toLong() shl 24)
            long1 = long1 or ((data[offset + 4] and 0xFF).toLong() shl 16)
            long1 = long1 or ((data[offset + 7] and 0xFF).toLong() shl 8)
            long1 = long1 or ((data[offset + 6] and 0xFF).toLong() shl 0)

            var long2: Long = 0
            long2 = long2 or ((data[offset + 8] and 0xFF).toLong() shl 56)
            long2 = long2 or ((data[offset + 9] and 0xFF).toLong() shl 48)
            long2 = long2 or ((data[offset + 10] and 0xFF).toLong() shl 40)
            long2 = long2 or ((data[offset + 11] and 0xFF).toLong() shl 32)
            long2 = long2 or ((data[offset + 12] and 0xFF).toLong() shl 24)
            long2 = long2 or ((data[offset + 13] and 0xFF).toLong() shl 16)
            long2 = long2 or ((data[offset + 14] and 0xFF).toLong() shl 8)
            long2 = long2 or ((data[offset + 15] and 0xFF).toLong() shl 0)

            result = UUID(long1, long2)
        }
        return result
    }

    /**
     * Reads a date value. Note that an NA is represented as 65535 in the
     * MPP file. We represent this in Java using a null value. The actual
     * value in the MPP file is number of days since 31/12/1983.
     *
     * @param data byte array of data
     * @param offset location of data as offset into the array
     * @return date value
     */
    fun getDate(data: ByteArray, offset: Int): Date? {
        val result: Date?
        val days = getShort(data, offset).toLong()

        if (days == 65535L) {
            result = null
        } else {
            result = DateHelper.getDateFromLong(EPOCH + days * DateHelper.MS_PER_DAY)
        }

        return result
    }

    /**
     * Reads a time value. The time is represented as tenths of a
     * minute since midnight.
     *
     * @param data byte array of data
     * @param offset location of data as offset into the array
     * @return time value
     */
    fun getTime(data: ByteArray, offset: Int): Date {
        val time = getShort(data, offset) / 10
        val cal = DateHelper.popCalendar(epochDate)
        cal.set(Calendar.HOUR_OF_DAY, time / 60)
        cal.set(Calendar.MINUTE, time % 60)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        DateHelper.pushCalendar(cal)
        return cal.getTime()
    }

    /**
     * Reads a duration value in milliseconds. The time is represented as
     * tenths of a minute since midnight.
     *
     * @param data byte array of data
     * @param offset location of data as offset into the array
     * @return duration value
     */
    fun getDuration(data: ByteArray, offset: Int): Long {
        return getShort(data, offset) * DateHelper.MS_PER_MINUTE / 10
    }

    /**
     * Reads a combined date and time value.
     *
     * @param data byte array of data
     * @param offset location of data as offset into the array
     * @return time value
     */
    fun getTimestamp(data: ByteArray, offset: Int): Date? {
        val result: Date?

        var days = getShort(data, offset + 2).toLong()
        if (days < 100) {
            // We are seeing some files which have very small values for the number of days.
            // When the relevant field is shown in MS Project it appears as NA.
            // We try to mimic this behaviour here.
            days = 0
        }

        if (days == 0L || days == 65535L) {
            result = null
        } else {
            var time = getShort(data, offset).toLong()
            if (time == 65535L) {
                time = 0
            }
            result = DateHelper.getTimestampFromLong(EPOCH + days * DateHelper.MS_PER_DAY + time * DateHelper.MS_PER_MINUTE / 10)
        }

        return result
    }

    /**
     * Reads a combined date and time value expressed in tenths of a minute.
     *
     * @param data byte array of data
     * @param offset location of data as offset into the array
     * @return time value
     */
    fun getTimestampFromTenths(data: ByteArray, offset: Int): Date {
        val ms = getInt(data, offset).toLong() * 6000
        return DateHelper.getTimestampFromLong(EPOCH + ms)
    }

    /**
     * Reads a string of two byte characters from the input array.
     * This method assumes that the string finishes either at the
     * end of the array, or when char zero is encountered.
     * The value starts at the position specified by the offset
     * parameter.
     *
     * @param data byte array of data
     * @param offset start point of unicode string
     * @return string value
     */
    fun getUnicodeString(data: ByteArray, offset: Int): String {
        val length = getUnicodeStringLengthInBytes(data, offset)
        return if (length == 0) "" else String(data, offset, length, CharsetHelper.UTF16LE)
    }

    /**
     * Reads a string of two byte characters from the input array.
     * This method assumes that the string finishes either at the
     * end of the array, or when char zero is encountered, or
     * when a string of a certain length in bytes has been read.
     * The value starts at the position specified by the offset
     * parameter.
     *
     * @param data byte array of data
     * @param offset start point of unicode string
     * @param maxLength length in bytes of the string
     * @return string value
     */
    fun getUnicodeString(data: ByteArray, offset: Int, maxLength: Int): String {
        var length = getUnicodeStringLengthInBytes(data, offset)
        if (maxLength > 0 && length > maxLength) {
            length = maxLength
        }
        return if (length == 0) "" else String(data, offset, length, CharsetHelper.UTF16LE)
    }

    /**
     * Determine the length of a nul terminated UTF16LE string in bytes.
     *
     * @param data string data
     * @param offset offset into string data
     * @return length in bytes
     */
    private fun getUnicodeStringLengthInBytes(data: ByteArray?, offset: Int): Int {
        var result: Int
        if (data == null || offset >= data.size) {
            result = 0
        } else {
            result = data.size - offset

            var loop = offset
            while (loop < data.size - 1) {
                if (data[loop].toInt() == 0 && data[loop + 1].toInt() == 0) {
                    result = loop - offset
                    break
                }
                loop += 2
            }
        }
        return result
    }

    /**
     * Reads a string of single byte characters from the input array.
     * This method assumes that the string finishes either at the
     * end of the array, or when char zero is encountered.
     * Reading begins at the supplied offset into the array.
     *
     * @param data byte array of data
     * @param offset offset into the array
     * @return string value
     */
    fun getString(data: ByteArray, offset: Int): String {
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
     * Reads a duration value. This method relies on the fact that
     * the units of the duration have been specified elsewhere.
     *
     * @param value Duration value
     * @param type type of units of the duration
     * @return Duration instance
     */
    fun getDuration(value: Int, type: TimeUnit): Duration {
        return getDuration(value.toDouble(), type)
    }

    /**
     * Reads a color value represented by three bytes, for R, G, and B
     * components, plus a flag byte indicating if this is an automatic color.
     * Returns null if the color type is "Automatic".
     *
     * @param data byte array of data
     * @param offset offset into array
     * @return new Color instance
     */
    fun getColor(data: ByteArray, offset: Int): Color? {
        var result: Color? = null

        if (getByte(data, offset + 3) == 0) {
            val r = getByte(data, offset)
            val g = getByte(data, offset + 1)
            val b = getByte(data, offset + 2)
            result = Color(r, g, b)
        }

        return result
    }

    /**
     * Reads a duration value. This method relies on the fact that
     * the units of the duration have been specified elsewhere.
     *
     * @param value Duration value
     * @param type type of units of the duration
     * @return Duration instance
     */
    fun getDuration(value: Double, type: TimeUnit): Duration {
        val duration: Double
        // Value is given in 1/10 of minute
        when (type) {
            MINUTES, ELAPSED_MINUTES -> {
                duration = value / 10
            }

            HOURS, ELAPSED_HOURS -> {
                duration = value / 600 // 60 * 10
            }

            DAYS -> {
                duration = value / 4800 // 8 * 60 * 10
            }

            ELAPSED_DAYS -> {
                duration = value / 14400 // 24 * 60 * 10
            }

            WEEKS -> {
                duration = value / 24000 // 5 * 8 * 60 * 10
            }

            ELAPSED_WEEKS -> {
                duration = value / 100800 // 7 * 24 * 60 * 10
            }

            MONTHS -> {
                duration = value / 96000 //
            }

            ELAPSED_MONTHS -> {
                duration = value / 432000 // 30 * 24 * 60 * 10
            }

            else -> {
                duration = value
            }
        }

        return Duration.getInstance(duration, type)
    }

    /**
     * This method converts between the duration units representation
     * used in the MPP file, and the standard MPX duration units.
     * If the supplied units are unrecognised, the units default to days.
     *
     * @param type MPP units
     * @param projectDefaultDurationUnits default duration units for this project
     * @return MPX units
     */
    @JvmOverloads
    fun getDurationTimeUnits(type: Int, projectDefaultDurationUnits: TimeUnit? = null): TimeUnit {
        val units: TimeUnit

        when (type and DURATION_UNITS_MASK) {
            3 -> {
                units = TimeUnit.MINUTES
            }

            4 -> {
                units = TimeUnit.ELAPSED_MINUTES
            }

            5 -> {
                units = TimeUnit.HOURS
            }

            6 -> {
                units = TimeUnit.ELAPSED_HOURS
            }

            8 -> {
                units = TimeUnit.ELAPSED_DAYS
            }

            9 -> {
                units = TimeUnit.WEEKS
            }

            10 -> {
                units = TimeUnit.ELAPSED_WEEKS
            }

            11 -> {
                units = TimeUnit.MONTHS
            }

            12 -> {
                units = TimeUnit.ELAPSED_MONTHS
            }

            19 -> {
                units = TimeUnit.PERCENT
            }

            20 -> {
                units = TimeUnit.ELAPSED_PERCENT
            }

            7 -> {
                units = TimeUnit.DAYS
            }

            21 -> {
                units = if (projectDefaultDurationUnits == null) TimeUnit.DAYS else projectDefaultDurationUnits
            }

            else -> {
                units = TimeUnit.DAYS
            }
        }

        return units
    }

    /**
     * Given a duration and the time units for the duration extracted from an MPP
     * file, this method creates a new Duration to represent the given
     * duration. This instance has been adjusted to take into account the
     * number of "hours per day" specified for the current project.
     *
     * @param properties project properties
     * @param duration duration length
     * @param timeUnit duration units
     * @return Duration instance
     */
    fun getAdjustedDuration(properties: ProjectProperties, duration: Int, timeUnit: TimeUnit): Duration? {
        var result: Duration? = null

        if (duration != -1) {
            when (timeUnit) {
                DAYS -> {
                    val unitsPerDay = properties.minutesPerDay.doubleValue() * 10.0
                    var totalDays = 0.0
                    if (unitsPerDay != 0.0) {
                        totalDays = duration / unitsPerDay
                    }
                    result = Duration.getInstance(totalDays, timeUnit)
                }

                ELAPSED_DAYS -> {
                    val unitsPerDay = 24.0 * 600.0
                    val totalDays = duration / unitsPerDay
                    result = Duration.getInstance(totalDays, timeUnit)
                }

                WEEKS -> {
                    val unitsPerWeek = properties.minutesPerWeek.doubleValue() * 10.0
                    var totalWeeks = 0.0
                    if (unitsPerWeek != 0.0) {
                        totalWeeks = duration / unitsPerWeek
                    }
                    result = Duration.getInstance(totalWeeks, timeUnit)
                }

                ELAPSED_WEEKS -> {
                    val unitsPerWeek = (60 * 24 * 7 * 10).toDouble()
                    val totalWeeks = duration / unitsPerWeek
                    result = Duration.getInstance(totalWeeks, timeUnit)
                }

                MONTHS -> {
                    val unitsPerMonth = properties.minutesPerDay.doubleValue() * properties.daysPerMonth.doubleValue() * 10.0
                    var totalMonths = 0.0
                    if (unitsPerMonth != 0.0) {
                        totalMonths = duration / unitsPerMonth
                    }
                    result = Duration.getInstance(totalMonths, timeUnit)
                }

                ELAPSED_MONTHS -> {
                    val unitsPerMonth = (60 * 24 * 30 * 10).toDouble()
                    val totalMonths = duration / unitsPerMonth
                    result = Duration.getInstance(totalMonths, timeUnit)
                }

                else -> {
                    result = getDuration(duration, timeUnit)
                }
            }
        }
        return result
    }

    /**
     * This method maps from the value used to specify default work units in the
     * MPP file to a standard TimeUnit.
     *
     * @param value Default work units
     * @return TimeUnit value
     */
    fun getWorkTimeUnits(value: Int): TimeUnit {
        return TimeUnit.getInstance(value - 1)
    }

    /**
     * This method maps the currency symbol position from the
     * representation used in the MPP file to the representation
     * used by MPX.
     *
     * @param value MPP symbol position
     * @return MPX symbol position
     */
    fun getSymbolPosition(value: Int): CurrencySymbolPosition {
        val result: CurrencySymbolPosition

        when (value) {
            1 -> {
                result = CurrencySymbolPosition.AFTER
            }

            2 -> {
                result = CurrencySymbolPosition.BEFORE_WITH_SPACE
            }

            3 -> {
                result = CurrencySymbolPosition.AFTER_WITH_SPACE
            }

            0 -> {
                result = CurrencySymbolPosition.BEFORE
            }
            else -> {
                result = CurrencySymbolPosition.BEFORE
            }
        }

        return result
    }

    /**
     * Utility method to remove ampersands embedded in names.
     *
     * @param name name text
     * @return name text without embedded ampersands
     */
    fun removeAmpersands(name: String?): String? {
        var name = name
        if (name != null) {
            if (name.indexOf('&') !== -1) {
                val sb = StringBuilder()
                var index = 0
                var c: Char

                while (index < name.length()) {
                    c = name.charAt(index)
                    if (c != '&') {
                        sb.append(c)
                    }
                    ++index
                }

                name = sb.toString()
            }
        }

        return name
    }

    /**
     * Utility method to read a percentage value.
     *
     * @param data data block
     * @param offset offset into data block
     * @return percentage value
     */
    fun getPercentage(data: ByteArray, offset: Int): Double? {
        val value = MPPUtility.getShort(data, offset)
        var result: Double? = null
        if (value >= 0 && value <= 100) {
            result = NumberHelper.getDouble(value.toDouble())
        }
        return result
    }

    /**
     * This method allows a subsection of a byte array to be copied.
     *
     * @param data source data
     * @param offset offset into the source data
     * @param size length of the source data to copy
     * @return new byte array containing copied data
     */
    fun cloneSubArray(data: ByteArray, offset: Int, size: Int): ByteArray {
        val newData = ByteArray(size)
        System.arraycopy(data, offset, newData, 0, size)
        return newData
    }

    /**
     * Writes a hex dump to a file for a large byte array.
     *
     * @param fileName output file name
     * @param data target data
     */
    fun fileHexDump(fileName: String, data: ByteArray) {
        System.out.println("FILE HEX DUMP")
        try {
            val os = FileOutputStream(fileName)
            os.write(ByteArrayHelper.hexdump(data, true, 16, "").getBytes())
            os.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }

    /**
     * Writes a hex dump to a file from a POI input stream.
     * Note that this assumes that the complete size of the data in
     * the stream is returned by the available() method.
     *
     * @param fileName output file name
     * @param is input stream
     */
    fun fileHexDump(fileName: String, `is`: InputStream) {
        try {
            val data = ByteArray(`is`.available())
            `is`.read(data)
            fileHexDump(fileName, data)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }

    /**
     * Writes a large byte array to a file.
     *
     * @param fileName output file name
     * @param data target data
     */
    fun fileDump(fileName: String, data: ByteArray) {
        System.out.println("FILE DUMP")
        try {
            val os = FileOutputStream(fileName)
            os.write(data)
            os.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }

    /**
     * Dump out all the possible variables within the given data block.
     *
     * @param properties project properties
     * @param data data to dump from
     * @param dumpShort true to dump all the data as shorts
     * @param dumpInt true to dump all the data as ints
     * @param dumpDouble true to dump all the data as Doubles
     * @param dumpTimeStamp true to dump all the data as TimeStamps
     * @param dumpDuration true to dump all the data as Durations (long)
     * @param dumpDate true to dump all the data as Dates
     * @param dumpTime true to dump all the data as Dates (time)
     * @param dumpAdjustedDuration true to dump all data as adjusted durations
     */
    fun dataDump(properties: ProjectProperties, data: ByteArray?, dumpShort: Boolean, dumpInt: Boolean, dumpDouble: Boolean, dumpTimeStamp: Boolean, dumpDuration: Boolean, dumpDate: Boolean, dumpTime: Boolean, dumpAdjustedDuration: Boolean) {
        System.out.println("DATA")

        if (data != null) {
            System.out.println(ByteArrayHelper.hexdump(data, false, 16, ""))

            for (i in data.indices) {
                if (dumpShort) {
                    try {
                        val sh = MPPUtility.getShort(data, i)
                        System.out.println(i + ":" + sh)
                    } catch (ex: Exception) {
                        // Silently ignore exceptions
                    }

                }
                if (dumpInt) {
                    try {
                        val sh = MPPUtility.getInt(data, i)
                        System.out.println(i + ":" + sh)
                    } catch (ex: Exception) {
                        // Silently ignore exceptions
                    }

                }
                if (dumpDouble) {
                    try {
                        val d = MPPUtility.getDouble(data, i)
                        System.out.println(i + ":" + d)
                    } catch (ex: Exception) {
                        // Silently ignore exceptions
                    }

                }
                if (dumpTimeStamp) {
                    try {
                        val d = MPPUtility.getTimestamp(data, i)
                        if (d != null) {
                            System.out.println(i + ":" + d!!.toString())
                        }
                    } catch (ex: Exception) {
                        // Silently ignore exceptions
                    }

                }
                if (dumpDuration) {
                    try {
                        val d = MPPUtility.getDuration(data, i)
                        System.out.println(i + ":" + d)
                    } catch (ex: Exception) {
                        // Silently ignore exceptions
                    }

                }
                if (dumpDate) {
                    try {
                        val d = MPPUtility.getDate(data, i)
                        if (d != null) {
                            System.out.println(i + ":" + d!!.toString())
                        }
                    } catch (ex: Exception) {
                        // Silently ignore exceptions
                    }

                }
                if (dumpTime) {
                    try {
                        val d = MPPUtility.getTime(data, i)
                        System.out.println(i + ":" + d.toString())
                    } catch (ex: Exception) {
                        // Silently ignore exceptions
                    }

                }
                if (dumpAdjustedDuration) {
                    try {
                        System.out.println(i + ":" + MPPUtility.getAdjustedDuration(properties, MPPUtility.getInt(data, i), TimeUnit.DAYS))
                    } catch (ex: Exception) {
                        // Silently ignore exceptions
                    }

                }

            }
        }
    }

    /**
     * Dump out all the possible variables within the given data block.
     *
     * @param data data to dump from
     * @param id unique ID
     * @param dumpShort true to dump all the data as shorts
     * @param dumpInt true to dump all the data as ints
     * @param dumpDouble true to dump all the data as Doubles
     * @param dumpTimeStamp true to dump all the data as TimeStamps
     * @param dumpUnicodeString true to dump all the data as Unicode strings
     * @param dumpString true to dump all the data as strings
     */
    fun varDataDump(data: Var2Data, id: Integer, dumpShort: Boolean, dumpInt: Boolean, dumpDouble: Boolean, dumpTimeStamp: Boolean, dumpUnicodeString: Boolean, dumpString: Boolean) {
        System.out.println("VARDATA")
        for (i in 0..499) {
            if (dumpShort) {
                try {
                    val sh = data.getShort(id, Integer.valueOf(i))
                    System.out.println("$i:$sh")
                } catch (ex: Exception) {
                    // Silently ignore exceptions
                }

            }
            if (dumpInt) {
                try {
                    val sh = data.getInt(id, Integer.valueOf(i))
                    System.out.println("$i:$sh")
                } catch (ex: Exception) {
                    // Silently ignore exceptions
                }

            }
            if (dumpDouble) {
                try {
                    val d = data.getDouble(id, Integer.valueOf(i))
                    System.out.println("$i:$d")
                    System.out.println(i.toString() + ":" + d / 60000)
                } catch (ex: Exception) {
                    // Silently ignore exceptions
                }

            }
            if (dumpTimeStamp) {
                try {
                    val d = data.getTimestamp(id, Integer.valueOf(i))
                    if (d != null) {
                        System.out.println(i.toString() + ":" + d.toString())
                    }
                } catch (ex: Exception) {
                    // Silently ignore exceptions
                }

            }
            if (dumpUnicodeString) {
                try {
                    val s = data.getUnicodeString(id, Integer.valueOf(i))
                    if (s != null) {
                        System.out.println("$i:$s")
                    }
                } catch (ex: Exception) {
                    // Silently ignore exceptions
                }

            }
            if (dumpString) {
                try {
                    val s = data.getString(id, Integer.valueOf(i))
                    if (s != null) {
                        System.out.println("$i:$s")
                    }
                } catch (ex: Exception) {
                    // Silently ignore exceptions
                }

            }
        }
    }

    /**
     * Dumps the contents of a structured block made up from a header
     * and fixed sized records.
     *
     * @param headerSize header zie
     * @param blockSize block size
     * @param data data block
     */
    fun dumpBlockData(headerSize: Int, blockSize: Int, data: ByteArray?) {
        if (data != null) {
            System.out.println(ByteArrayHelper.hexdump(data, 0, headerSize, false))
            var index = headerSize
            while (index < data.size) {
                System.out.println(ByteArrayHelper.hexdump(data, index, blockSize, false))
                index += blockSize
            }
        }
    }
}
/**
 * Private constructor to prevent instantiation.
 */// private constructor to prevent instantiation
/**
 * This method converts between the duration units representation
 * used in the MPP file, and the standard MPX duration units.
 * If the supplied units are unrecognised, the units default to days.
 *
 * @param type MPP units
 * @return MPX units
 */
