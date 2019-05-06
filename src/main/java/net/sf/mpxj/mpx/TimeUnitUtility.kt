/*
 * file:       TimeUnitUtility.java
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

import java.util.Locale

import net.sf.mpxj.MPXJException
import net.sf.mpxj.TimeUnit

/**
 * This class contains method relating to managing TimeUnit instances
 * for MPX files.
 */
internal object TimeUnitUtility {

    /**
     * This method is used to parse a string representation of a time
     * unit, and return the appropriate constant value.
     *
     * @param units string representation of a time unit
     * @param locale target locale
     * @return numeric constant
     * @throws MPXJException normally thrown when parsing fails
     */
    @SuppressWarnings("unchecked")
    @Throws(MPXJException::class)
    fun getInstance(units: String, locale: Locale): TimeUnit {
        val map = LocaleData.getMap(locale, LocaleData.TIME_UNITS_MAP)
        val result = map.get(units.toLowerCase())
                ?: throw MPXJException(MPXJException.INVALID_TIME_UNIT.toString() + " " + units)
        return TimeUnit.getInstance(result!!.intValue())
    }
}
/**
 * Constructor.
 */// private constructor to prevent instantiation
