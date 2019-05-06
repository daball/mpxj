/*
 * file:       AbstractWbsFormat.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       01/03/2018
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

package net.sf.mpxj.primavera.common

import java.util.ArrayList

/**
 * Common methods to support reading the WBS format definition from P3 and SureTrak.
 */
open class AbstractWbsFormat {

    /**
     * Retrieves the formatted WBS value.
     *
     * @return formatted WBS value
     */
    val formattedValue: String
        get() = joinElements(m_elements.size())

    /**
     * Retrieves the level of this WBS code.
     *
     * @return level value
     */
    val level: Integer
        get() = Integer.valueOf((m_elements.size() + 1) / 2)

    /**
     * Retrieves the formatted parent WBS value.
     *
     * @return formatted parent WBS value
     */
    val formattedParentValue: String?
        get() {
            var result: String? = null
            if (m_elements.size() > 2) {
                result = joinElements(m_elements.size() - 2)
            }
            return result
        }

    private val m_elements = ArrayList<String>()
    protected val m_lengths: List<Integer> = ArrayList<Integer>()
    protected val m_separators: List<String> = ArrayList<String>()
    /**
     * Parses a raw WBS value from the database and breaks it into
     * component parts ready for formatting.
     *
     * @param value raw WBS value
     */
    fun parseRawValue(value: String) {
        var valueIndex = 0
        var elementIndex = 0
        m_elements.clear()
        while (valueIndex < value.length() && elementIndex < m_elements.size()) {
            val elementLength = m_lengths[elementIndex].intValue()
            if (elementIndex > 0) {
                m_elements.add(m_separators[elementIndex - 1])
            }
            var endIndex = valueIndex + elementLength
            if (endIndex > value.length()) {
                endIndex = value.length()
            }
            val element = value.substring(valueIndex, endIndex)
            m_elements.add(element)
            valueIndex += elementLength
            elementIndex++
        }
    }

    /**
     * Joins the individual WBS elements to make the formated value.
     *
     * @param length number of elements to join
     * @return formatted WBS value
     */
    private fun joinElements(length: Int): String {
        val sb = StringBuilder()
        for (index in 0 until length) {
            sb.append(m_elements.get(index))
        }
        return sb.toString()
    }
}
