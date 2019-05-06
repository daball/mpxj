/*
 * file:       MPXJNumberFormat.java
 * author:     Jon Iles
 *             Scott Melville
 * copyright:  (c) Packwood Software 2002-2006
 * date:       15/08/2002
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

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParsePosition
import java.util.Arrays

/**
 * This class extends the functionality of the DecimalFormat class
 * for use within MPXJ.
 */
class MPXJNumberFormat : DecimalFormat() {

    /**
     * Number formatter.
     */
    private val m_symbols = DecimalFormatSymbols()
    private var m_alternativeFormats: Array<DecimalFormat>? = null
    /**
     * This method is used to configure the primary and alternative
     * format patterns.
     *
     * @param primaryPattern new format pattern
     * @param alternativePatterns alternative format patterns
     * @param decimalSeparator Locale specific decimal separator to replace placeholder
     * @param groupingSeparator Locale specific grouping separator to replace placeholder
     */
    fun applyPattern(primaryPattern: String, alternativePatterns: Array<String>?, decimalSeparator: Char, groupingSeparator: Char) {
        m_symbols.setDecimalSeparator(decimalSeparator)
        m_symbols.setGroupingSeparator(groupingSeparator)

        setDecimalFormatSymbols(m_symbols)
        applyPattern(primaryPattern)

        if (alternativePatterns != null && alternativePatterns.size != 0) {
            var loop: Int
            if (m_alternativeFormats == null || m_alternativeFormats!!.size != alternativePatterns.size) {
                m_alternativeFormats = arrayOfNulls<DecimalFormat>(alternativePatterns.size)
                loop = 0
                while (loop < alternativePatterns.size) {
                    m_alternativeFormats[loop] = DecimalFormat()
                    loop++
                }
            }

            loop = 0
            while (loop < alternativePatterns.size) {
                m_alternativeFormats!![loop].setDecimalFormatSymbols(m_symbols)
                m_alternativeFormats!![loop].applyPattern(alternativePatterns[loop])
                loop++
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun parse(str: String?, parsePosition: ParsePosition): Number? {
        var str = str
        var result: Number? = null

        if (str == null) {
            parsePosition.setIndex(-1)
        } else {
            str = str.trim()

            if (str.length() === 0) {
                parsePosition.setIndex(-1)
            } else {
                result = super.parse(str, parsePosition)
                if (parsePosition.getIndex() === 0) {
                    result = null

                    if (m_alternativeFormats != null) {
                        for (loop in m_alternativeFormats!!.indices) {
                            result = m_alternativeFormats!![loop].parse(str, parsePosition)
                            if (parsePosition.getIndex() !== 0) {
                                break
                            }
                        }

                        if (parsePosition.getIndex() === 0) {
                            result = null
                        }
                    }
                }
            }
        }

        return result
    }

    @Override
    fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + Arrays.hashCode(m_alternativeFormats)
        result = prime * result + if (m_symbols == null) 0 else m_symbols.hashCode()
        return result
    }

    @Override
    fun equals(obj: Object): Boolean {
        if (this === obj) {
            return true
        }

        if (!super.equals(obj)) {
            return false
        }

        if (getClass() !== obj.getClass()) {
            return false
        }

        val other = obj as MPXJNumberFormat
        if (!Arrays.equals(m_alternativeFormats, other.m_alternativeFormats)) {
            return false
        }

        if (m_symbols == null) {
            if (other.m_symbols != null) {
                return false
            }
        } else {
            if (!m_symbols.equals(other.m_symbols)) {
                return false
            }
        }

        return true
    }
}
