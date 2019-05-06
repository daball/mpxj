/*
 * file:       MPDUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       02-Feb-2006
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

import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import kotlin.collections.Map.Entry

import net.sf.mpxj.CurrencySymbolPosition
import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.TimeUnit

/**
 * This class implements common utility methods used when processing
 * MPD files.
 */
object MPDUtility {

    /**
     * Constants used to convert bytes to hex digits.
     */
    private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    /**
     * Mask used to remove flags from the duration units field.
     */
    private val DURATION_UNITS_MASK = 0x1F

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
     * This method converts between the duration units representation
     * used in the MPP file, and the standard MPX duration units.
     * If the supplied units are unrecognised, the units default to days.
     *
     * @param type MPP units
     * @return MPX units
     */
    fun getDurationTimeUnits(type: Int): TimeUnit {
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
            7 -> {
                units = TimeUnit.DAYS
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
     * @param file parent file
     * @param duration duration length
     * @param timeUnit duration units
     * @return Duration instance
     */
    fun getAdjustedDuration(file: ProjectFile, duration: Int, timeUnit: TimeUnit): Duration {
        val result: Duration
        when (timeUnit) {
            MINUTES, ELAPSED_MINUTES -> {
                val totalMinutes = duration / 10.0
                result = Duration.getInstance(totalMinutes, timeUnit)
            }

            HOURS, ELAPSED_HOURS -> {
                val totalHours = duration / 600.0
                result = Duration.getInstance(totalHours, timeUnit)
            }

            DAYS -> {
                val unitsPerDay = file.projectProperties.minutesPerDay.doubleValue() * 10.0
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
                val unitsPerWeek = file.projectProperties.minutesPerWeek.doubleValue() * 10.0
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

            ELAPSED_MONTHS -> {
                val unitsPerMonth = (60 * 24 * 30 * 10).toDouble()
                val totalMonths = duration / unitsPerMonth
                result = Duration.getInstance(totalMonths, timeUnit)
            }

            MONTHS -> {
                val totalMonths = duration / 96000.0
                result = Duration.getInstance(totalMonths, timeUnit)
            }

            else -> {
                result = Duration.getInstance(duration, timeUnit)
            }
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
                duration = value / 96000 // 4 * 5 * 8 * 60 * 10
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
     * Dump the contents of a row from an MPD file.
     *
     * @param row row data
     */
    fun dumpRow(row: Map<String, Object>) {
        for (entry in row.entrySet()) {
            val value = entry.getValue()
            System.out.println(entry.getKey() + " = " + value + " ( " + (if (value == null) "" else value!!.getClass().getName()) + ")")
        }
    }

    /**
     * This method generates a formatted version of the data contained
     * in a byte array. The data is written both in hex, and as ASCII
     * characters.
     *
     * @param buffer data to be displayed
     * @param offset offset of start of data to be displayed
     * @param length length of data to be displayed
     * @param ascii flag indicating whether ASCII equivalent chars should also be displayed
     * @return formatted string
     */
    fun hexdump(buffer: ByteArray?, offset: Int, length: Int, ascii: Boolean): String {
        val sb = StringBuilder()

        if (buffer != null) {
            var c: Char
            var loop: Int
            val count = offset + length

            loop = offset
            while (loop < count) {
                sb.append(" ")
                sb.append(HEX_DIGITS[buffer[loop] and 0xF0 shr 4])
                sb.append(HEX_DIGITS[buffer[loop] and 0x0F])
                loop++
            }

            if (ascii == true) {
                sb.append("   ")

                loop = offset
                while (loop < count) {
                    c = buffer[loop].toChar()

                    if (c.toInt() > 200 || c.toInt() < 27) {
                        c = ' '
                    }

                    sb.append(c)
                    loop++
                }
            }
        }

        return sb.toString()
    }

    /**
     * This method generates a formatted version of the data contained
     * in a byte array. The data is written both in hex, and as ASCII
     * characters.
     *
     * @param buffer data to be displayed
     * @param ascii flag indicating whether ASCII equivalent chars should also be displayed
     * @return formatted string
     */
    fun hexdump(buffer: ByteArray?, ascii: Boolean): String {
        var length = 0

        if (buffer != null) {
            length = buffer.size
        }

        return hexdump(buffer, 0, length, ascii)
    }

    /**
     * This method generates a formatted version of the data contained
     * in a byte array. The data is written both in hex, and as ASCII
     * characters. The data is organised into fixed width columns.
     *
     * @param buffer data to be displayed
     * @param ascii flag indicating whether ASCII equivalent chars should also be displayed
     * @param columns number of columns
     * @param prefix prefix to be added before the start of the data
     * @return formatted string
     */
    fun hexdump(buffer: ByteArray?, ascii: Boolean, columns: Int, prefix: String): String {
        var columns = columns
        val sb = StringBuilder()
        if (buffer != null) {
            var index = 0
            val df = DecimalFormat("00000")

            while (index < buffer.size) {
                if (index + columns > buffer.size) {
                    columns = buffer.size - index
                }

                sb.append(prefix)
                sb.append(df.format(index))
                sb.append(":")
                sb.append(hexdump(buffer, index, columns, ascii))
                sb.append('\n')

                index += columns
            }
        }

        return sb.toString()
    }

    /**
     * Writes a hex dump to a file for a large byte array.
     *
     * @param fileName output file name
     * @param data target data
     */
    fun fileHexDump(fileName: String, data: ByteArray) {
        try {
            val os = FileOutputStream(fileName)
            os.write(hexdump(data, true, 16, "").getBytes())
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
        try {
            val os = FileOutputStream(fileName)
            os.write(data)
            os.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }

}
