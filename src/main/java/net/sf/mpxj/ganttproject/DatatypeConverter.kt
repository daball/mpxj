/*
 * file:       DatatypeConverter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       28 December 2017
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

package net.sf.mpxj.ganttproject

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * This class contains methods used to perform the datatype conversions
 * required to read GanttProject files.
 */
object DatatypeConverter {

    private val DATE_FORMAT = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            val df = SimpleDateFormat("yyyy-MM-dd")
            df.setLenient(false)
            return df
        }
    }

    /**
     * Parse a date value.
     *
     * @param value string representation
     * @return date value
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
     * Print a date value.
     *
     * @param value time value
     * @return time value
     */
    fun printDate(value: Date?): String? {
        return if (value == null) null else DATE_FORMAT.get().format(value)
    }
}
