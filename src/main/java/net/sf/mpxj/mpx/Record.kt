/*
 * file:       Record.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       01/01/2003
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

import java.io.IOException
import java.text.ParseException
import java.util.Arrays
import java.util.Date
import java.util.LinkedList
import java.util.Locale

import net.sf.mpxj.AccrueType
import net.sf.mpxj.CodePage
import net.sf.mpxj.CurrencySymbolPosition
import net.sf.mpxj.DateOrder
import net.sf.mpxj.Duration
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectDateFormat
import net.sf.mpxj.ProjectTimeFormat
import net.sf.mpxj.Rate
import net.sf.mpxj.ScheduleFrom
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.Tokenizer

/**
 * This class is used to represent a record in an MPX file.
 */
internal class Record
/**
 * This constructor takes a stream of tokens and extracts the
 * fields of an individual record from those tokens.
 *
 * @param locale target locale
 * @param tk tokenizer providing the input stream of tokens
 * @param formats formats used when parsing data
 * @throws MPXJException normally thrown when parsing fails
 */
@Throws(MPXJException::class)
constructor(locale: Locale, tk: Tokenizer, formats: MPXJFormats) {

    /**
     * Retrieves the record number associated with this record.
     *
     * @return the record number associated with this record
     */
    val recordNumber: Integer?
        get() = m_recordNumber

    /**
     * This method returns the number of fields present in this record.
     *
     * @return number of fields
     */
    val length: Int
        get() = m_fields!!.size

    /**
     * Target locale.
     */
    private var m_locale: Locale? = null

    /**
     * Current record number.
     */
    private var m_recordNumber: Integer? = null

    /**
     * Array of field data.
     */
    private var m_fields: Array<String>? = null

    private var m_formats: MPXJFormats? = null

    init {
        try {
            m_locale = locale

            m_formats = formats

            val list = LinkedList<String>()

            while (tk.nextToken() == Tokenizer.TT_WORD) {
                list.add(tk.token)
            }

            if (list.size() > 0) {
                setRecordNumber(list)
                m_fields = list.toArray(arrayOfNulls<String>(list.size()))
            }
        } catch (ex: IOException) {
            throw MPXJException(MPXJException.INVALID_RECORD, ex)
        }

    }

    /**
     * Pop the record number from the front of the list, and parse it to ensure that
     * it is a valid integer.
     *
     * @param list MPX record
     */
    private fun setRecordNumber(list: LinkedList<String>) {
        try {
            val number = list.remove(0)
            m_recordNumber = Integer.valueOf(number)
        } catch (ex: NumberFormatException) {
            // Malformed MPX file: the record number isn't a valid integer
            // Catch the exception here, leaving m_recordNumber as null
            // so we will skip this record entirely.
        }

    }

    /**
     * Accessor method used to retrieve a String object representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    fun getString(field: Int): String? {
        var result: String?

        if (field < m_fields!!.size) {
            result = m_fields!![field]

            if (result != null) {
                result = result.replace(MPXConstants.EOL_PLACEHOLDER, '\n')
            }
        } else {
            result = null
        }

        return result
    }

    /**
     * Accessor method used to retrieve a char representing the
     * contents of an individual field. If the field does not exist in the
     * record, the default character is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    fun getCharacter(field: Int): Character? {
        val result: Character?

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = Character.valueOf(m_fields!![field].charAt(0))
        } else {
            result = null
        }

        return result
    }

    /**
     * Accessor method used to retrieve a Float object representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    @Throws(MPXJException::class)
    fun getFloat(field: Int): Number? {
        try {
            val result: Number?

            if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
                result = m_formats!!.decimalFormat.parse(m_fields!![field])
            } else {
                result = null
            }

            return result
        } catch (ex: ParseException) {
            throw MPXJException("Failed to parse float", ex)
        }

    }

    /**
     * Accessor method used to retrieve an Integer object representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    fun getInteger(field: Int): Integer? {
        val result: Integer?

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = Integer.valueOf(m_fields!![field])
        } else {
            result = null
        }

        return result
    }

    /**
     * Accessor method used to retrieve an Date instance representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     * @throws MPXJException normally thrown when parsing fails
     */
    @Throws(MPXJException::class)
    fun getDateTime(field: Int): Date? {
        var result: Date? = null

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            try {
                result = m_formats!!.dateTimeFormat.parse(m_fields!![field])
            } catch (ex: ParseException) {
                // Failed to parse a full date time.
            }

            //
            // Fall back to trying just parsing the date component
            //
            if (result == null) {
                try {
                    result = m_formats!!.dateFormat.parse(m_fields!![field])
                } catch (ex: ParseException) {
                    throw MPXJException("Failed to parse date time", ex)
                }

            }
        }

        return result
    }

    /**
     * Accessor method used to retrieve an Date instance representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     * @throws MPXJException normally thrown when parsing fails
     */
    @Throws(MPXJException::class)
    fun getDate(field: Int): Date? {
        try {
            val result: Date?

            if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
                result = m_formats!!.dateFormat.parse(m_fields!![field])
            } else {
                result = null
            }

            return result
        } catch (ex: ParseException) {
            throw MPXJException("Failed to parse date", ex)
        }

    }

    /**
     * Accessor method used to retrieve an Date instance representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     * @throws MPXJException normally thrown when parsing fails
     */
    @Throws(MPXJException::class)
    fun getTime(field: Int): Date? {
        try {
            val result: Date?

            if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
                result = m_formats!!.timeFormat.parse(m_fields!![field])
            } else {
                result = null
            }

            return result
        } catch (ex: ParseException) {
            throw MPXJException("Failed to parse time", ex)
        }

    }

    /**
     * Accessor method used to retrieve a Boolean object representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    fun getNumericBoolean(field: Int): Boolean {
        var result = false

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = Integer.parseInt(m_fields!![field]) === 1
        }

        return result
    }

    /**
     * Accessor method used to retrieve an Rate object representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     * @throws MPXJException normally thrown when parsing fails
     */
    @Throws(MPXJException::class)
    fun getRate(field: Int): Rate? {
        val result: Rate?

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            try {
                val rate = m_fields!![field]
                val index = rate.indexOf('/')
                val amount: Double
                val units: TimeUnit

                if (index == -1) {
                    amount = m_formats!!.currencyFormat.parse(rate).doubleValue()
                    units = TimeUnit.HOURS
                } else {
                    amount = m_formats!!.currencyFormat.parse(rate.substring(0, index)).doubleValue()
                    units = TimeUnitUtility.getInstance(rate.substring(index + 1), m_locale)
                }

                result = Rate(amount, units)
            } catch (ex: ParseException) {
                throw MPXJException("Failed to parse rate", ex)
            }

        } else {
            result = null
        }

        return result
    }

    /**
     * Accessor method used to retrieve an Number instance representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     * @throws MPXJException normally thrown when parsing fails
     */
    @Throws(MPXJException::class)
    fun getCurrency(field: Int): Number? {
        val result: Number?

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            try {
                result = m_formats!!.currencyFormat.parse(m_fields!![field])
            } catch (ex: ParseException) {
                throw MPXJException("Failed to parse currency", ex)
            }

        } else {
            result = null
        }

        return result
    }

    /**
     * Accessor method used to retrieve an Number instance representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     * @throws MPXJException normally thrown when parsing fails
     */
    @Throws(MPXJException::class)
    fun getPercentage(field: Int): Number? {
        val result: Number?

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            try {
                result = m_formats!!.percentageDecimalFormat.parse(m_fields!![field])
            } catch (ex: ParseException) {
                throw MPXJException("Failed to parse percentage", ex)
            }

        } else {
            result = null
        }

        return result
    }

    /**
     * Accessor method used to retrieve an Duration object representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     * @throws MPXJException normally thrown when parsing fails
     */
    @Throws(MPXJException::class)
    fun getDuration(field: Int): Duration? {
        val result: Duration?

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = DurationUtility.getInstance(m_fields!![field], m_formats!!.durationDecimalFormat, m_locale)
        } else {
            result = null
        }

        return result
    }

    /**
     * Accessor method used to retrieve a Number instance representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     * @throws MPXJException normally thrown when parsing fails
     */
    @Throws(MPXJException::class)
    fun getUnits(field: Int): Number? {
        val result: Number?

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            try {
                result = Double.valueOf(m_formats!!.unitsDecimalFormat.parse(m_fields!![field]).doubleValue() * 100)
            } catch (ex: ParseException) {
                throw MPXJException("Failed to parse units", ex)
            }

        } else {
            result = null
        }

        return result
    }

    /**
     * Accessor method used to retrieve an Integer object representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    fun getTimeUnit(field: Int): TimeUnit {
        val result: TimeUnit

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = TimeUnit.getInstance(Integer.parseInt(m_fields!![field]))
        } else {
            result = TimeUnit.DAYS
        }

        return result
    }

    /**
     * Accessor method used to retrieve an Integer object representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    fun getTimeFormat(field: Int): ProjectTimeFormat {
        val result: ProjectTimeFormat

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = ProjectTimeFormat.getInstance(Integer.parseInt(m_fields!![field]))
        } else {
            result = ProjectTimeFormat.TWELVE_HOUR
        }

        return result
    }

    /**
     * Accessor method used to retrieve an Integer object representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    fun getScheduleFrom(field: Int): ScheduleFrom {
        val result: ScheduleFrom

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = ScheduleFrom.getInstance(Integer.parseInt(m_fields!![field]))
        } else {
            result = ScheduleFrom.START
        }

        return result
    }

    /**
     * Accessor method used to retrieve an Integer object representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    fun getDateOrder(field: Int): DateOrder {
        val result: DateOrder

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = DateOrder.getInstance(Integer.parseInt(m_fields!![field]))
        } else {
            result = DateOrder.MDY
        }

        return result
    }

    /**
     * Accessor method used to retrieve an Integer object representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    fun getCurrencySymbolPosition(field: Int): CurrencySymbolPosition {
        val result: CurrencySymbolPosition

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = CurrencySymbolPosition.getInstance(Integer.parseInt(m_fields!![field]))
        } else {
            result = CurrencySymbolPosition.BEFORE
        }

        return result
    }

    /**
     * Accessor method used to retrieve an Integer object representing the
     * contents of an individual field. If the field does not exist in the
     * record, null is returned.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    fun getDateFormat(field: Int): ProjectDateFormat? {
        var result: ProjectDateFormat? = null

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = ProjectDateFormat.getInstance(Integer.parseInt(m_fields!![field]))
        } else {
            result = ProjectDateFormat.DD_MM_YY
        }

        return result
    }

    /**
     * Retrieves a CodePage instance. Defaults to ANSI.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    fun getCodePage(field: Int): CodePage {
        val result: CodePage

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = CodePage.getInstance(m_fields!![field])
        } else {
            result = CodePage.getInstance(null)
        }

        return result
    }

    /**
     * Accessor method to retrieve an accrue type instance.
     *
     * @param field the index number of the field to be retrieved
     * @return the value of the required field
     */
    fun getAccrueType(field: Int): AccrueType? {
        val result: AccrueType?

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = AccrueTypeUtility.getInstance(m_fields!![field], m_locale)
        } else {
            result = null
        }

        return result
    }

    /**
     * Accessor method to retrieve a Boolean instance.
     *
     * @param field the index number of the field to be retrieved
     * @param falseText locale specific text representing false
     * @return the value of the required field
     */
    fun getBoolean(field: Int, falseText: String): Boolean? {
        val result: Boolean?

        if (field < m_fields!!.size && m_fields!![field].length() !== 0) {
            result = if (m_fields!![field].equalsIgnoreCase(falseText) === true) Boolean.FALSE else Boolean.TRUE
        } else {
            result = null
        }

        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return Arrays.toString(m_fields)
    }
}
