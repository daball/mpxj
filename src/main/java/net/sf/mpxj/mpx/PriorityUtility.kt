/*
 * file:       PriorityUtility.java
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

import net.sf.mpxj.Priority

/**
 * This class contains method relating to managing Priority instances
 * for MPX files.
 */
internal object PriorityUtility {

    /**
     * Index into the VALUE array of the default priority.
     */
    private val DEFAULT_PRIORITY_INDEX = 4

    /**
     * This method takes the textual version of a priority
     * and returns an appropriate instance of this class. Note that unrecognised
     * values are treated as medium priority.
     *
     * @param locale target locale
     * @param priority text version of the priority
     * @return Priority class instance
     */
    fun getInstance(locale: Locale, priority: String?): Priority {
        var index = DEFAULT_PRIORITY_INDEX

        if (priority != null) {
            val priorityTypes = LocaleData.getStringArray(locale, LocaleData.PRIORITY_TYPES)
            for (loop in priorityTypes.indices) {
                if (priorityTypes[loop].equalsIgnoreCase(priority) === true) {
                    index = loop
                    break
                }
            }
        }

        return Priority.getInstance((index + 1) * 100)
    }

}
/**
 * Constructor.
 */// private constructor to prevent instantiation
