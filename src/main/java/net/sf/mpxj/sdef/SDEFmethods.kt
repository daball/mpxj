/*
 * file:       SDEFmethods.java
 * author:     William (Bill) Iverson
 * copyright:  (c) GeoComputer 2011
 * date:       06/01/2012
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

package net.sf.mpxj.sdef

import net.sf.mpxj.DayType
import net.sf.mpxj.ProjectCalendar

/**
 * This class contains some general purpose static methods used in my SDEFWriter.
 */
internal object SDEFmethods {
    /**
     * Method to force an input string into a fixed width field
     * and set it on the left with the right side filled with
     * space ' ' characters.
     *
     * @param input input string
     * @param width required width
     * @return formatted string
     */
    fun lset(input: String?, width: Int): String {
        val result: String
        val pad = StringBuilder()
        if (input == null) {
            for (i in 0 until width) {
                pad.append(' ') // put blanks into buffer
            }
            result = pad.toString()
        } else {
            if (input.length() >= width) {
                result = input.substring(0, width) // when input is too long, truncate
            } else {
                val padLength = width - input.length() // number of blanks to add
                for (i in 0 until padLength) {
                    pad.append(' ') // force put blanks into buffer
                }
                result = input + pad // concatenate
            }
        }
        return result
    }

    /**
     * Another method to force an input string into a fixed width field
     * and set it on the right with the left side filled with space ' ' characters.
     *
     * @param input input string
     * @param width required width
     * @return formatted string
     */
    fun rset(input: String?, width: Int): String {
        val result: String // result to return
        val pad = StringBuilder()
        if (input == null) {
            for (i in 0 until width - 1) {
                pad.append(' ') // put blanks into buffer
            }
            result = " $pad" // one short to use + overload
        } else {
            if (input.length() >= width) {
                result = input.substring(0, width) // when input is too long, truncate
            } else {
                val padLength = width - input.length() // number of blanks to add
                for (i in 0 until padLength) {
                    pad.append(' ') // actually put blanks into buffer
                }
                result = pad + input // concatenate
            }
        }
        return result
    }

    /**
     * This method takes a calendar of MPXJ library type, then returns a String of the
     * general working days USACE format.  For example, the regular 5-day work week is
     * NYYYYYN
     *
     * If you get Fridays off work, then the String becomes NYYYYNN
     *
     * @param input ProjectCalendar instance
     * @return work days string
     */
    fun workDays(input: ProjectCalendar): String {
        val result = StringBuilder()
        val test = input.days // get the array from MPXJ ProjectCalendar
        for (i in test) { // go through every day in the given array
            if (i === DayType.NON_WORKING) {
                result.append("N") // only put N for non-working day of the week
            } else {
                result.append("Y") // Assume WORKING day unless NON_WORKING
            }
        }
        return result.toString() // According to USACE specs., exceptions will be specified in HOLI records
    }

}
