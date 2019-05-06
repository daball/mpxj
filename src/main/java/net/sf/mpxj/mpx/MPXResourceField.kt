/*
 * file:       MPXResourceField.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       20-Feb-2006
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

import net.sf.mpxj.ResourceField

/**
 * Utility class used to map between the integer values held in an MPX file
 * to represent a task field, and the enumerated type used to represent
 * task fields in MPXJ.
 */
internal object MPXResourceField {

    val MAX_FIELDS = 52

    private val MPX_MPXJ_ARRAY = arrayOfNulls<ResourceField>(MAX_FIELDS)

    private val MPXJ_MPX_ARRAY = IntArray(ResourceField.MAX_VALUE)
    /**
     * Retrieve an instance of the ResourceField class based on the data read from an
     * MPX file.
     *
     * @param value value from an MS Project file
     * @return instance of this class
     */
    fun getMpxjField(value: Int): ResourceField? {
        var result: ResourceField? = null

        if (value >= 0 && value < MPX_MPXJ_ARRAY.size) {
            result = MPX_MPXJ_ARRAY[value]
        }

        return result
    }

    /**
     * Retrieve the integer value used to represent a resource field in an
     * MPX file.
     *
     * @param value MPXJ resource field value
     * @return MPX field value
     */
    fun getMpxField(value: Int): Int {
        var result = 0

        if (value >= 0 && value < MPXJ_MPX_ARRAY.size) {
            result = MPXJ_MPX_ARRAY[value]
        }

        return result

    }

    init {
        MPX_MPXJ_ARRAY[45] = ResourceField.ACCRUE_AT
        MPX_MPXJ_ARRAY[32] = ResourceField.ACTUAL_COST
        MPX_MPXJ_ARRAY[22] = ResourceField.ACTUAL_WORK
        MPX_MPXJ_ARRAY[48] = ResourceField.BASE_CALENDAR
        MPX_MPXJ_ARRAY[31] = ResourceField.BASELINE_COST
        MPX_MPXJ_ARRAY[21] = ResourceField.BASELINE_WORK
        MPX_MPXJ_ARRAY[4] = ResourceField.CODE
        MPX_MPXJ_ARRAY[30] = ResourceField.COST
        MPX_MPXJ_ARRAY[44] = ResourceField.COST_PER_USE
        MPX_MPXJ_ARRAY[34] = ResourceField.COST_VARIANCE
        MPX_MPXJ_ARRAY[11] = ResourceField.EMAIL_ADDRESS
        MPX_MPXJ_ARRAY[3] = ResourceField.GROUP
        MPX_MPXJ_ARRAY[40] = ResourceField.ID
        MPX_MPXJ_ARRAY[2] = ResourceField.INITIALS
        MPX_MPXJ_ARRAY[51] = ResourceField.LINKED_FIELDS
        MPX_MPXJ_ARRAY[41] = ResourceField.MAX_UNITS
        MPX_MPXJ_ARRAY[1] = ResourceField.NAME
        MPX_MPXJ_ARRAY[50] = ResourceField.OBJECTS
        MPX_MPXJ_ARRAY[46] = ResourceField.OVERALLOCATED
        MPX_MPXJ_ARRAY[43] = ResourceField.OVERTIME_RATE
        MPX_MPXJ_ARRAY[24] = ResourceField.OVERTIME_WORK
        MPX_MPXJ_ARRAY[47] = ResourceField.PEAK
        MPX_MPXJ_ARRAY[26] = ResourceField.PERCENT_WORK_COMPLETE
        MPX_MPXJ_ARRAY[33] = ResourceField.REMAINING_COST
        MPX_MPXJ_ARRAY[23] = ResourceField.REMAINING_WORK
        MPX_MPXJ_ARRAY[42] = ResourceField.STANDARD_RATE
        MPX_MPXJ_ARRAY[5] = ResourceField.TEXT1
        MPX_MPXJ_ARRAY[6] = ResourceField.TEXT2
        MPX_MPXJ_ARRAY[7] = ResourceField.TEXT3
        MPX_MPXJ_ARRAY[8] = ResourceField.TEXT4
        MPX_MPXJ_ARRAY[9] = ResourceField.TEXT5
        MPX_MPXJ_ARRAY[49] = ResourceField.UNIQUE_ID
        MPX_MPXJ_ARRAY[20] = ResourceField.WORK
        MPX_MPXJ_ARRAY[25] = ResourceField.WORK_VARIANCE
    }

    init {
        for (loop in MPX_MPXJ_ARRAY.indices) {
            val field = MPX_MPXJ_ARRAY[loop]
            if (field != null) {
                MPXJ_MPX_ARRAY[field.value] = loop
            }
        }
    }
}
