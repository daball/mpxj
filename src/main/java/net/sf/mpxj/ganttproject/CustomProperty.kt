/*
 * file:       CustomProperty.java
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

import net.sf.mpxj.FieldType

/**
 * This class is used to track which custom fields of a certain type
 * have been used when extracting custom properties from a GanttProject schedule.
 */
internal class CustomProperty
/**
 * Constructor.
 *
 * @param fields array of available fields
 * @param index index into this array at which to start
 */
@JvmOverloads constructor(private val m_fields: Array<FieldType>, private var m_index: Int = 0) {

    /**
     * Retrieve the next available field.
     *
     * @return FieldType instance for the next available field
     */
    val field: FieldType?
        get() {
            var result: FieldType? = null
            if (m_index < m_fields.size) {
                result = m_fields[m_index++]
            }

            return result
        }
}
/**
 * Constructor.
 *
 * @param fields array of available fields
 */
