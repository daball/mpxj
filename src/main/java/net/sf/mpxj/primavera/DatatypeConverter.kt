/*
 * file:       DatatypeConverter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       08/08/2011
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

package net.sf.mpxj.primavera

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

/**
 * This class contains methods used to perform the datatype conversions
 * required to read and write PM files.
 */
object DatatypeConverter {

    private val DATE_FORMAT = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            df.setLenient(false)
            return df
        }
    }

    private val TIME_FORMAT = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            val df = SimpleDateFormat("HH:mm:ss")
            df.setLenient(false)
            return df
        }
    }

    /**
     * Convert the Primavera string representation of a UUID into a Java UUID instance.
     *
     * @param value Primavera UUID
     * @return Java UUID instance
     */
    fun parseUUID(value: String?): UUID? {
        var result: UUID? = null
        if (value != null && !value.isEmpty()) {
            if (value.charAt(0) === '{') {
                // PMXML representation: <GUID>{0AB9133E-A09A-9648-B98A-B2384894AC44}</GUID>
                result = UUID.fromString(value.substring(1, value.length() - 1))
            } else {
                // XER representation: CrkTPqCalki5irI4SJSsRA
                val data = javax.xml.bind.DatatypeConverter.parseBase64Binary("$value==")
                var msb: Long = 0
                var lsb: Long = 0

                for (i in 0..7) {
                    msb = msb shl 8 or (data[i] and 0xff)
                }

                for (i in 8..15) {
                    lsb = lsb shl 8 or (data[i] and 0xff)
                }

                result = UUID(msb, lsb)
            }
        }
        return result
    }

    /**
     * Retrieve a UUID in the form required by Primavera PMXML.
     *
     * @param guid UUID instance
     * @return formatted UUID
     */
    fun printUUID(guid: UUID?): String? {
        return if (guid == null) null else "{" + guid!!.toString().toUpperCase() + "}"
    }

    /**
     * Print a date time value.
     *
     * @param value date time value
     * @return string representation
     */
    fun printDateTime(value: Date?): String? {
        return if (value == null) null else DATE_FORMAT.get().format(value)
    }

    /**
     * Parse a date time value.
     *
     * @param value string representation
     * @return date time value
     */
    fun parseDateTime(value: String?): Date? {
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
     * Print a time value.
     *
     * @param value time value
     * @return time value
     */
    fun printTime(value: Date?): String? {
        return if (value == null) null else TIME_FORMAT.get().format(value)
    }

    /**
     * Parse a time value.
     *
     * @param value time value
     * @return time value
     */
    fun parseTime(value: String?): Date? {
        var result: Date? = null
        if (value != null && value.length() !== 0) {
            try {
                result = TIME_FORMAT.get().parse(value)
            } catch (ex: ParseException) {
                // Ignore this and return null
            }

        }
        return result
    }
}
