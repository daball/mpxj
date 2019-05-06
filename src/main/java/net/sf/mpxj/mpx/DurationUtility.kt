/*
 * file:       DurationUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Jan 23, 2006
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

package net.sf.mpxj.mpx

import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

import net.sf.mpxj.Duration
import net.sf.mpxj.MPXJException
import net.sf.mpxj.TimeUnit

/**
 * This class contains method relating to managing Duration instances
 * for MPX files.
 */
internal object DurationUtility {

    /**
     * Retrieve an Duration instance. Use shared objects to
     * represent common values for memory efficiency.
     *
     * @param dur duration formatted as a string
     * @param format number format
     * @param locale target locale
     * @return Duration instance
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    fun getInstance(dur: String, format: NumberFormat, locale: Locale): Duration {
        try {
            val lastIndex = dur.length() - 1
            var index = lastIndex
            val duration: Double
            val units: TimeUnit

            while (index > 0 && Character.isDigit(dur.charAt(index)) === false) {
                --index
            }

            //
            // If we have no units suffix, assume days to allow for MPX3
            //
            if (index == lastIndex) {
                duration = format.parse(dur).doubleValue()
                units = TimeUnit.DAYS
            } else {
                ++index
                duration = format.parse(dur.substring(0, index)).doubleValue()
                while (index < lastIndex && Character.isWhitespace(dur.charAt(index))) {
                    ++index
                }
                units = TimeUnitUtility.getInstance(dur.substring(index), locale)
            }

            return Duration.getInstance(duration, units)
        } catch (ex: ParseException) {
            throw MPXJException("Failed to parse duration", ex)
        }

    }
}
/**
 * Constructor.
 */// private constructor to prevent instantiation
