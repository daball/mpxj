/*
 * file:       MPXJFormats.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       Jan 20, 2006
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

import java.text.DateFormat
import java.text.NumberFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Locale

import net.sf.mpxj.DateOrder
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.ProjectTimeFormat

/**
 * This class manages the various objects required to parse and format
 * data items in MPX files.
 */
class MPXJFormats
/**
 * Constructor.
 *
 * @param locale target locale
 * @param nullText locale specific text to represent a value which has not been set, normally "NA"
 * @param file parent file
 */
(private val m_locale: Locale, private val m_nullText: String, private val m_projectFile: ProjectFile) {

    /**
     * Retrieve the units decimal format.
     *
     * @return units decimal format
     */
    val unitsDecimalFormat: NumberFormat
        get() = m_unitsDecimalFormat

    /**
     * Retrieve the decimal format.
     *
     * @return decimal format
     */
    val decimalFormat: NumberFormat
        get() = m_decimalFormat

    /**
     * Retrieve the currency format.
     *
     * @return currency format
     */
    val currencyFormat: NumberFormat
        get() = m_currencyFormat

    /**
     * Retrieve the duration decimal format.
     *
     * @return duration decimal format
     */
    val durationDecimalFormat: NumberFormat
        get() = m_durationDecimalFormat

    /**
     * Retrieve the percentage decimal format.
     *
     * @return percentage decimal format
     */
    val percentageDecimalFormat: NumberFormat
        get() = m_percentageDecimalFormat

    /**
     * Retrieve the date time format.
     *
     * @return date time format
     */
    val dateTimeFormat: DateFormat
        get() = m_dateTimeFormat

    /**
     * Retrieve the date format.
     *
     * @return date format
     */
    val dateFormat: DateFormat
        get() = m_dateFormat

    /**
     * Retrieve the time format.
     *
     * @return time format
     */
    val timeFormat: DateFormat
        get() = m_timeFormat

    /**
     * Retrieve the text representing a null value.
     *
     * @return null text
     */
    val nullText: String
        get() = m_nullText
    private val m_unitsDecimalFormat = MPXJNumberFormat()
    private val m_decimalFormat = MPXJNumberFormat()
    private val m_currencyFormat = MPXJNumberFormat()
    private val m_durationDecimalFormat = MPXJNumberFormat()
    private val m_percentageDecimalFormat = MPXJNumberFormat()
    private val m_dateTimeFormat = MPXJDateFormat()
    private val m_dateFormat = MPXJDateFormat()
    private val m_timeFormat = MPXJTimeFormat()

    init {
        update()
    }

    /**
     * Called to update the cached formats when something changes.
     */
    fun update() {
        val properties = m_projectFile.projectProperties
        val decimalSeparator = properties.decimalSeparator
        val thousandsSeparator = properties.thousandsSeparator
        m_unitsDecimalFormat.applyPattern("#.##", null, decimalSeparator, thousandsSeparator)
        m_decimalFormat.applyPattern("0.00#", null, decimalSeparator, thousandsSeparator)
        m_durationDecimalFormat.applyPattern("#.##", null, decimalSeparator, thousandsSeparator)
        m_percentageDecimalFormat.applyPattern("##0.##", null, decimalSeparator, thousandsSeparator)
        updateCurrencyFormats(properties, decimalSeparator, thousandsSeparator)
        updateDateTimeFormats(properties)
    }

    /**
     * Update the currency format.
     *
     * @param properties project properties
     * @param decimalSeparator decimal separator
     * @param thousandsSeparator thousands separator
     */
    private fun updateCurrencyFormats(properties: ProjectProperties, decimalSeparator: Char, thousandsSeparator: Char) {
        var prefix = ""
        var suffix = ""
        val currencySymbol = quoteFormatCharacters(properties.currencySymbol)

        when (properties.symbolPosition) {
            AFTER -> {
                suffix = currencySymbol
            }

            BEFORE -> {
                prefix = currencySymbol
            }

            AFTER_WITH_SPACE -> {
                suffix = " $currencySymbol"
            }

            BEFORE_WITH_SPACE -> {
                prefix = "$currencySymbol "
            }
        }

        val pattern = StringBuilder(prefix)
        pattern.append("#0")

        val digits = properties.currencyDigits.intValue()
        if (digits > 0) {
            pattern.append('.')
            for (i in 0 until digits) {
                pattern.append("0")
            }
        }

        pattern.append(suffix)

        val primaryPattern = pattern.toString()

        val alternativePatterns = arrayOfNulls<String>(7)
        alternativePatterns[0] = primaryPattern + ";(" + primaryPattern + ")"
        pattern.insert(prefix.length(), "#,#")
        val secondaryPattern = pattern.toString()
        alternativePatterns[1] = secondaryPattern
        alternativePatterns[2] = secondaryPattern + ";(" + secondaryPattern + ")"

        pattern.setLength(0)
        pattern.append("#0")

        if (digits > 0) {
            pattern.append('.')
            for (i in 0 until digits) {
                pattern.append("0")
            }
        }

        val noSymbolPrimaryPattern = pattern.toString()
        alternativePatterns[3] = noSymbolPrimaryPattern
        alternativePatterns[4] = noSymbolPrimaryPattern + ";(" + noSymbolPrimaryPattern + ")"
        pattern.insert(0, "#,#")
        val noSymbolSecondaryPattern = pattern.toString()
        alternativePatterns[5] = noSymbolSecondaryPattern
        alternativePatterns[6] = noSymbolSecondaryPattern + ";(" + noSymbolSecondaryPattern + ")"

        m_currencyFormat.applyPattern(primaryPattern, alternativePatterns, decimalSeparator, thousandsSeparator)
    }

    /**
     * This method is used to quote any special characters that appear in
     * literal text that is required as part of the currency format.
     *
     * @param literal Literal text
     * @return literal text with special characters in quotes
     */
    private fun quoteFormatCharacters(literal: String): String {
        val sb = StringBuilder()
        val length = literal.length()
        var c: Char

        for (loop in 0 until length) {
            c = literal.charAt(loop)
            when (c) {
                '0', '#', '.', '-', ',', 'E', ';', '%' -> {
                    sb.append("'")
                    sb.append(c)
                    sb.append("'")
                }

                else -> {
                    sb.append(c)
                }
            }
        }

        return sb.toString()
    }

    /**
     * Updates the date and time formats.
     *
     * @param properties project properties
     */
    private fun updateDateTimeFormats(properties: ProjectProperties) {
        val timePatterns = getTimePatterns(properties)
        val datePatterns = getDatePatterns(properties)
        val dateTimePatterns = getDateTimePatterns(properties, timePatterns)

        m_dateTimeFormat.applyPatterns(dateTimePatterns)
        m_dateFormat.applyPatterns(datePatterns)
        m_timeFormat.applyPatterns(timePatterns)

        m_dateTimeFormat.setLocale(m_locale)
        m_dateFormat.setLocale(m_locale)

        m_dateTimeFormat.setNullText(m_nullText)
        m_dateFormat.setNullText(m_nullText)
        m_timeFormat.setNullText(m_nullText)

        m_dateTimeFormat.setAmPmText(properties.amText, properties.pmText)
        m_timeFormat.setAmPmText(properties.amText, properties.pmText)
    }

    /**
     * Generate date patterns based on the project configuration.
     *
     * @param properties project properties
     * @return date patterns
     */
    private fun getDatePatterns(properties: ProjectProperties): Array<String> {
        var pattern = ""

        val datesep = properties.dateSeparator
        val dateOrder = properties.dateOrder

        when (dateOrder) {
            DMY -> {
                pattern = "dd" + datesep + "MM" + datesep + "yy"
            }

            MDY -> {
                pattern = "MM" + datesep + "dd" + datesep + "yy"
            }

            YMD -> {
                pattern = "yy" + datesep + "MM" + datesep + "dd"
            }
        }

        return arrayOf(pattern)
    }

    /**
     * Generate datetime patterns based on the project configuration.
     *
     * @param properties project configuration
     * @param timePatterns time patterns
     * @return datetime patterns
     */
    private fun getDateTimePatterns(properties: ProjectProperties, timePatterns: Array<String>): Array<String> {
        val patterns = ArrayList<String>()
        val datesep = properties.dateSeparator
        val dateOrder = properties.dateOrder

        when (properties.dateFormat) {
            ProjectDateFormat.DD_MM_YY_HH_MM -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.addAll(generateDateTimePatterns("dd" + datesep + "MM" + datesep + "yy", timePatterns))
                    }

                    MDY -> {
                        patterns.addAll(generateDateTimePatterns("MM" + datesep + "dd" + datesep + "yy", timePatterns))
                    }

                    YMD -> {
                        patterns.addAll(generateDateTimePatterns("yy" + datesep + "MM" + datesep + "dd", timePatterns))
                    }
                }
            }

            ProjectDateFormat.DD_MM_YY -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.add("dd" + datesep + "MM" + datesep + "yy")
                    }

                    MDY -> {
                        patterns.add("MM" + datesep + "dd" + datesep + "yy")
                    }

                    YMD -> {
                        patterns.add("yy" + datesep + "MM" + datesep + "dd")

                    }
                }
            }

            ProjectDateFormat.DD_MMMMM_YYYY_HH_MM -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.addAll(generateDateTimePatterns("dd MMMMM yyyy", timePatterns))
                    }

                    MDY -> {
                        patterns.addAll(generateDateTimePatterns("MMMMM dd yyyy", timePatterns))
                    }

                    YMD -> {
                        patterns.addAll(generateDateTimePatterns("yyyy MMMMM dd", timePatterns))
                    }
                }
            }

            ProjectDateFormat.DD_MMMMM_YYYY -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.add("dd MMMMM yyyy")
                    }

                    MDY -> {
                        patterns.add("MMMMM dd yyyy")
                    }

                    YMD -> {
                        patterns.add("yyyy MMMMM dd")
                    }
                }
            }

            ProjectDateFormat.DD_MMM_HH_MM -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.addAll(generateDateTimePatterns("dd MMM", timePatterns))
                    }

                    YMD, MDY -> {
                        patterns.addAll(generateDateTimePatterns("MMM dd", timePatterns))
                    }
                }
            }

            ProjectDateFormat.DD_MMM_YY -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.add("dd MMM ''yy")
                    }

                    MDY -> {
                        patterns.add("MMM dd ''yy")
                    }

                    YMD -> {
                        patterns.add("''yy MMM dd")
                    }
                }
            }

            ProjectDateFormat.DD_MMMMM -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.add("dd MMMMM")
                    }

                    YMD, MDY -> {
                        patterns.add("MMMMM dd")
                    }
                }
            }

            ProjectDateFormat.DD_MMM -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.add("dd MMM")
                    }

                    YMD, MDY -> {
                        patterns.add("MMM dd")
                    }
                }
            }

            ProjectDateFormat.EEE_DD_MM_YY_HH_MM -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.addAll(generateDateTimePatterns("EEE " + "dd" + datesep + "MM" + datesep + "yy", timePatterns))
                    }

                    MDY -> {
                        patterns.addAll(generateDateTimePatterns("EEE " + "MM" + datesep + "dd" + datesep + "yy", timePatterns))
                    }

                    YMD -> {
                        patterns.addAll(generateDateTimePatterns("EEE " + "yy" + datesep + "MM" + datesep + "dd", timePatterns))
                    }
                }
            }

            ProjectDateFormat.EEE_DD_MM_YY -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.add("EEE dd" + datesep + "MM" + datesep + "yy")
                    }

                    MDY -> {
                        patterns.add("EEE MM" + datesep + "dd" + datesep + "yy")
                    }

                    YMD -> {
                        patterns.add("EEE yy" + datesep + "MM" + datesep + "dd")
                    }
                }
            }

            ProjectDateFormat.EEE_DD_MMM_YY -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.add("EEE dd MMM ''yy")
                    }

                    MDY -> {
                        patterns.add("EEE MMM dd ''yy")
                    }

                    YMD -> {
                        patterns.add("EEE ''yy MMM dd")
                    }
                }
            }

            ProjectDateFormat.EEE_HH_MM -> {
                patterns.addAll(generateDateTimePatterns("EEE ", timePatterns))
            }

            ProjectDateFormat.DD_MM -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.add("dd" + datesep + "MM")
                    }

                    YMD, MDY -> {
                        patterns.add("MM" + datesep + "dd")
                    }
                }
            }

            ProjectDateFormat.DD -> {
                patterns.add("dd")
            }

            ProjectDateFormat.HH_MM -> {
                patterns.addAll(Arrays.asList(timePatterns))
            }

            ProjectDateFormat.EEE_DD_MMM -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.add("EEE dd MMM")
                    }

                    YMD, MDY -> {
                        patterns.add("EEE MMM dd")
                    }
                }
            }

            ProjectDateFormat.EEE_DD_MM -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.add("EEE dd" + datesep + "MM")
                    }

                    YMD, MDY -> {
                        patterns.add("EEE MM" + datesep + "dd")
                    }
                }
            }

            ProjectDateFormat.EEE_DD -> {
                patterns.add("EEE dd")
            }

            ProjectDateFormat.DD_WWW -> {
                patterns.add("F$datesep'W'ww")
            }

            ProjectDateFormat.DD_WWW_YY_HH_MM -> {
                patterns.addAll(generateDateTimePatterns("F" + datesep + "'W'ww" + datesep + "yy", timePatterns))
            }

            ProjectDateFormat.DD_MM_YYYY -> {
                when (dateOrder) {
                    DMY -> {
                        patterns.add("dd" + datesep + "MM" + datesep + "yyyy")
                    }

                    MDY -> {
                        patterns.add("MM" + datesep + "dd" + datesep + "yyyy")
                    }

                    YMD -> {
                        patterns.add("yyyy" + datesep + "MM" + datesep + "dd")
                    }
                }
            }
        }

        return patterns.toArray(arrayOfNulls<String>(patterns.size()))
    }

    /**
     * Generate a set of datetime patterns to accommodate variations in MPX files.
     *
     * @param datePattern date pattern element
     * @param timePatterns time patterns
     * @return datetime patterns
     */
    private fun generateDateTimePatterns(datePattern: String, timePatterns: Array<String>): List<String> {
        val patterns = ArrayList<String>()
        for (timePattern in timePatterns) {
            patterns.add("$datePattern $timePattern")
        }

        // Always fall back on the date-only pattern
        patterns.add(datePattern)

        return patterns
    }

    /**
     * Returns time elements considering 12/24 hour formatting.
     *
     * @param properties project properties
     * @return time formatting String
     */
    private fun getTimePatterns(properties: ProjectProperties): Array<String> {
        val result: Array<String>
        val timesep = properties.timeSeparator
        val format = properties.timeFormat

        if (format == null || format == ProjectTimeFormat.TWELVE_HOUR) {
            result = arrayOf("hh" + timesep + "mm a", "hh" + timesep + "mma")
        } else {
            result = arrayOf("HH" + timesep + "mm", "HH")
        }

        return result
    }
}
