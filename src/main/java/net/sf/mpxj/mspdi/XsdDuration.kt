/*
 * file:       XsdDuration.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       20/02/2003
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

package net.sf.mpxj.mspdi

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

import net.sf.mpxj.Duration

/**
 * This class parses and represents an xsd:duration value.
 */
internal class XsdDuration {

    /**
     * Retrieves the number of days.
     *
     * @return int
     */
    val days: Int
        get() = m_days

    /**
     * Retrieves the number of hours.
     *
     * @return int
     */
    val hours: Int
        get() = m_hours

    /**
     * Retrieves the number of minutes.
     *
     * @return int
     */
    val minutes: Int
        get() = m_minutes

    /**
     * Retrieves the number of months.
     *
     * @return int
     */
    val months: Int
        get() = m_months

    /**
     * Retrieves the number of seconds.
     *
     * @return double
     */
    val seconds: Double
        get() = m_seconds

    /**
     * Retrieves the number of years.
     *
     * @return int
     */
    val years: Int
        get() = m_years

    private var m_hasTime: Boolean = false
    private var m_years: Int = 0
    private var m_months: Int = 0
    private var m_days: Int = 0
    private var m_hours: Int = 0
    private var m_minutes: Int = 0
    private var m_seconds: Double = 0.toDouble()

    /**
     * Constructor. Parses the xsd:duration value and extracts the
     * duration data from it.
     *
     * @param duration value formatted as an xsd:duration
     */
    constructor(duration: String?) {
        if (duration != null) {
            val length = duration.length()
            if (length > 0) {
                // We have come across schedules exported from Synchro which represent
                // zero duration as a plain `0` rather than a well-formed XSD Duration.
                // MS Project reads this, so we'll treat it as a special case.
                if (length == 1 && duration.charAt(0) === '0') {
                    return
                }

                if (duration.charAt(0) !== 'P') {
                    if (length < 2 || duration.charAt(0) !== '-' && duration.charAt(1) !== 'P') {
                        throw IllegalArgumentException(duration)
                    }
                }

                var index: Int
                val negative: Boolean
                if (duration.charAt(0) === '-') {
                    index = 2
                    negative = true
                } else {
                    index = 1
                    negative = false
                }

                while (index < length) {
                    index = readComponent(duration, index, length)
                }

                if (negative == true) {
                    m_years = -m_years
                    m_months = -m_months
                    m_days = -m_days
                    m_hours = -m_hours
                    m_minutes = -m_minutes
                    m_seconds = -m_seconds
                }
            }
        }
    }

    /**
     * This constructor allows an xsd:duration to be created from
     * an MPX duration.
     *
     * @param duration An MPX duration.
     */
    constructor(duration: Duration?) {
        if (duration != null) {
            var amount = duration!!.getDuration()

            if (amount != 0.0) {
                when (duration!!.getUnits()) {
                    MINUTES, ELAPSED_MINUTES -> {
                        m_minutes = amount.toInt()
                        m_seconds = amount * 60 - m_minutes * 60
                    }

                    HOURS, ELAPSED_HOURS -> {
                        m_hours = amount.toInt()
                        amount = amount * 60 - m_hours * 60
                        m_minutes = amount.toInt()
                        m_seconds = amount * 60 - m_minutes * 60
                    }

                    DAYS, ELAPSED_DAYS -> {
                        m_days = amount.toInt()
                        amount = amount * 24 - m_days * 24
                        m_hours = amount.toInt()
                        amount = amount * 60 - m_hours * 60
                        m_minutes = amount.toInt()
                        m_seconds = amount * 60 - m_minutes * 60
                    }

                    WEEKS, ELAPSED_WEEKS -> {
                        amount *= 7.0
                        m_days = amount.toInt()
                        amount = amount * 24 - m_days * 24
                        m_hours = amount.toInt()
                        amount = amount * 60 - m_hours * 60
                        m_minutes = amount.toInt()
                        m_seconds = amount * 60 - m_minutes * 60
                    }

                    MONTHS, ELAPSED_MONTHS -> {
                        m_months = amount.toInt()
                        amount = amount * 28 - m_months * 28
                        m_days = amount.toInt()
                        amount = amount * 24 - m_days * 24
                        m_hours = amount.toInt()
                        amount = amount * 60 - m_hours * 60
                        m_minutes = amount.toInt()
                        m_seconds = amount * 60 - m_minutes * 60
                    }

                    YEARS, ELAPSED_YEARS -> {
                        m_years = amount.toInt()
                        amount = amount * 12 - m_years * 12
                        m_months = amount.toInt()
                        amount = amount * 28 - m_months * 28
                        m_days = amount.toInt()
                        amount = amount * 24 - m_days * 24
                        m_hours = amount.toInt()
                        amount = amount * 60 - m_hours * 60
                        m_minutes = amount.toInt()
                        m_seconds = amount * 60 - m_minutes * 60
                    }

                    else -> {
                    }
                }
            }
        }
    }

    /**
     * This method is called repeatedly to parse each duration component
     * from sorting data in xsd:duration format. Each component consists
     * of a number, followed by a letter representing the type.
     *
     * @param duration xsd:duration formatted string
     * @param index current position in the string
     * @param length length of string
     * @return current position in the string
     */
    private fun readComponent(duration: String, index: Int, length: Int): Int {
        var index = index
        var c: Char = 0.toChar()
        val number = StringBuilder()

        while (index < length) {
            c = duration.charAt(index)
            if (Character.isDigit(c) === true || c == '.') {
                number.append(c)
            } else {
                break
            }

            ++index
        }

        when (c) {
            'Y' -> {
                m_years = Integer.parseInt(number.toString())
            }

            'M' -> {
                if (m_hasTime == false) {
                    m_months = Integer.parseInt(number.toString())
                } else {
                    m_minutes = Integer.parseInt(number.toString())
                }
            }

            'D' -> {
                m_days = Integer.parseInt(number.toString())
            }

            'T' -> {
                m_hasTime = true
            }

            'H' -> {
                m_hours = Integer.parseInt(number.toString())
            }

            'S' -> {
                m_seconds = Double.parseDouble(number.toString())
            }

            else -> {
                throw IllegalArgumentException(duration)
            }
        }

        ++index

        return index
    }

    /**
     * This method generates the string representation of an xsd:duration value.
     *
     * @return xsd:duration value
     */
    @Override
    fun toString(): String {
        val buffer = StringBuilder("P")
        var negative = false

        if (m_years != 0 || m_months != 0 || m_days != 0) {
            if (m_years < 0) {
                negative = true
                buffer.append(-m_years)
            } else {
                buffer.append(m_years)
            }
            buffer.append("Y")

            if (m_months < 0) {
                negative = true
                buffer.append(-m_months)
            } else {
                buffer.append(m_months)
            }
            buffer.append("M")

            if (m_days < 0) {
                negative = true
                buffer.append(-m_days)
            } else {
                buffer.append(m_days)
            }
            buffer.append("D")
        }

        buffer.append("T")

        if (m_hours < 0) {
            negative = true
            buffer.append(-m_hours)
        } else {
            buffer.append(m_hours)
        }
        buffer.append("H")

        if (m_minutes < 0) {
            negative = true
            buffer.append(-m_minutes)
        } else {
            buffer.append(m_minutes)
        }
        buffer.append("M")

        if (m_seconds < 0) {
            negative = true
            buffer.append(FORMAT.format(-m_seconds))
        } else {
            buffer.append(FORMAT.format(m_seconds))
        }
        buffer.append("S")

        if (negative == true) {
            buffer.insert(0, '-')
        }

        return buffer.toString()
    }

    companion object {

        /**
         * Configure the decimal separator to be independent of the
         * one used by the default locale.
         */
        private val SYMBOLS = DecimalFormatSymbols()

        init {
            SYMBOLS.setDecimalSeparator('.')
        }

        private val FORMAT = DecimalFormat("#", SYMBOLS)
    }
}
