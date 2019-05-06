/*
 * file:       DatatypeConverter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Mar 30, 2005
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

import java.math.BigDecimal
import java.math.BigInteger
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

import net.sf.mpxj.AccrueType
import net.sf.mpxj.BookingType
import net.sf.mpxj.ConstraintType
import net.sf.mpxj.CurrencySymbolPosition
import net.sf.mpxj.DataType
import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.EarnedValueMethod
import net.sf.mpxj.FieldContainer
import net.sf.mpxj.FieldType
import net.sf.mpxj.Priority
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Rate
import net.sf.mpxj.ResourceType
import net.sf.mpxj.TaskType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.WorkContour
import net.sf.mpxj.WorkGroup
import net.sf.mpxj.common.NumberHelper

/**
 * This class contains methods used to perform the datatype conversions
 * required to read and write MSPDI files.
 */
object DatatypeConverter {

    private val DATE_FORMAT = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            df.setLenient(false)
            return df
        }
    }

    private val TIME_FORMAT = object : ThreadLocal<DateFormat>() {
        @Override
        protected fun initialValue(): DateFormat {
            val df = SimpleDateFormat("HH:mm:ss")
            df.setLenient(false)
            return df
        }
    }

    private val NUMBER_FORMAT = object : ThreadLocal<NumberFormat>() {
        @Override
        protected fun initialValue(): NumberFormat {
            // XML numbers should use . as decimal separator and no grouping.
            val format = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))
            format.setGroupingUsed(false)
            return format
        }
    }

    private val PARENT_FILE = ThreadLocal<ProjectFile>()

    private val BIGDECIMAL_ONE = BigDecimal.valueOf(1)
    /**
     * Print an extended attribute currency value.
     *
     * @param value currency value
     * @return string representation
     */
    fun printExtendedAttributeCurrency(value: Number?): String? {
        return if (value == null) null else NUMBER_FORMAT.get().format(value.doubleValue() * 100)
    }

    /**
     * Parse an extended attribute currency value.
     *
     * @param value string representation
     * @return currency value
     */
    fun parseExtendedAttributeCurrency(value: String?): Number? {
        val result: Number?

        if (value == null) {
            result = null
        } else {
            result = NumberHelper.getDouble(Double.parseDouble(correctNumberFormat(value)) / 100)
        }
        return result
    }

    /**
     * Print an extended attribute numeric value.
     *
     * @param value numeric value
     * @return string representation
     */
    fun printExtendedAttributeNumber(value: Number): String {
        return NUMBER_FORMAT.get().format(value.doubleValue())
    }

    /**
     * Parse and extended attribute numeric value.
     *
     * @param value string representation
     * @return numeric value
     */
    fun parseExtendedAttributeNumber(value: String): Number {
        return Double.valueOf(correctNumberFormat(value))
    }

    /**
     * Print an extended attribute boolean value.
     *
     * @param value boolean value
     * @return string representation
     */
    fun printExtendedAttributeBoolean(value: Boolean): String {
        return if (value.booleanValue()) "1" else "0"
    }

    /**
     * Parse an extended attribute boolean value.
     *
     * @param value string representation
     * @return boolean value
     */
    fun parseExtendedAttributeBoolean(value: String): Boolean {
        return if (value.equals("1")) Boolean.TRUE else Boolean.FALSE
    }

    /**
     * Print an extended attribute date value.
     *
     * @param value date value
     * @return string representation
     */
    fun printExtendedAttributeDate(value: Date?): String? {
        return if (value == null) null else DATE_FORMAT.get().format(value)
    }

    /**
     * Parse an extended attribute date value.
     *
     * @param value string representation
     * @return date value
     */
    fun parseExtendedAttributeDate(value: String?): Date? {
        var result: Date? = null

        if (value != null) {
            try {
                result = DATE_FORMAT.get().parse(value)
            } catch (ex: ParseException) {
                // ignore exceptions
            }

        }

        return result
    }

    /**
     * Print an extended attribute value.
     *
     * @param writer parent MSPDIWriter instance
     * @param value attribute value
     * @param type type of the value being passed
     * @return string representation
     */
    fun printExtendedAttribute(writer: MSPDIWriter, value: Object, type: DataType): String? {
        val result: String?

        if (type === DataType.DATE) {
            result = printExtendedAttributeDate(value as Date)
        } else {
            if (value is Boolean) {
                result = printExtendedAttributeBoolean(value as Boolean)
            } else {
                if (value is Duration) {
                    result = printDuration(writer, value as Duration)
                } else {
                    if (type === DataType.CURRENCY) {
                        result = printExtendedAttributeCurrency(value as Number)
                    } else {
                        if (value is Number) {
                            result = printExtendedAttributeNumber(value as Number)
                        } else {
                            result = value.toString()
                        }
                    }
                }
            }
        }

        return result
    }

    /**
     * Parse an extended attribute value.
     *
     * @param file parent file
     * @param mpx parent entity
     * @param value string value
     * @param mpxFieldID field ID
     * @param durationFormat duration format associated with the extended attribute
     */
    fun parseExtendedAttribute(file: ProjectFile, mpx: FieldContainer, value: String, mpxFieldID: FieldType?, durationFormat: TimeUnit) {
        if (mpxFieldID != null) {
            when (mpxFieldID!!.getDataType()) {
                STRING -> {
                    mpx.set(mpxFieldID, value)
                }

                DATE -> {
                    mpx.set(mpxFieldID, parseExtendedAttributeDate(value))
                }

                CURRENCY -> {
                    mpx.set(mpxFieldID, parseExtendedAttributeCurrency(value))
                }

                BOOLEAN -> {
                    mpx.set(mpxFieldID, parseExtendedAttributeBoolean(value))
                }

                NUMERIC -> {
                    mpx.set(mpxFieldID, parseExtendedAttributeNumber(value))
                }

                DURATION -> {
                    mpx.set(mpxFieldID, parseDuration(file, durationFormat, value))
                }

                else -> {
                }
            }
        }
    }

    /**
     * Prints a currency symbol position value.
     *
     * @param value CurrencySymbolPosition instance
     * @return currency symbol position
     */
    fun printCurrencySymbolPosition(value: CurrencySymbolPosition): String {
        val result: String

        when (value) {
            BEFORE -> {
                result = "0"
            }

            AFTER -> {
                result = "1"
            }

            BEFORE_WITH_SPACE -> {
                result = "2"
            }

            AFTER_WITH_SPACE -> {
                result = "3"
            }
            else -> {
                result = "0"
            }
        }

        return result
    }

    /**
     * Parse a currency symbol position value.
     *
     * @param value currency symbol position
     * @return CurrencySymbolPosition instance
     */
    fun parseCurrencySymbolPosition(value: String): CurrencySymbolPosition {
        var result = CurrencySymbolPosition.BEFORE

        when (NumberHelper.getInt(value)) {
            0 -> {
                result = CurrencySymbolPosition.BEFORE
            }

            1 -> {
                result = CurrencySymbolPosition.AFTER
            }

            2 -> {
                result = CurrencySymbolPosition.BEFORE_WITH_SPACE
            }

            3 -> {
                result = CurrencySymbolPosition.AFTER_WITH_SPACE
            }
        }

        return result
    }

    /**
     * Print an accrue type.
     *
     * @param value AccrueType instance
     * @return accrue type value
     */
    fun printAccrueType(value: AccrueType?): String {
        return Integer.toString(if (value == null) AccrueType.PRORATED.getValue() else value!!.getValue())
    }

    /**
     * Parse an accrue type.
     *
     * @param value accrue type value
     * @return AccrueType instance
     */
    fun parseAccrueType(value: String): AccrueType {
        return AccrueType.getInstance(NumberHelper.getInt(value))
    }

    /**
     * Print a resource type.
     *
     * @param value ResourceType instance
     * @return resource type value
     */
    fun printResourceType(value: ResourceType?): String {
        return Integer.toString(value?.value ?: ResourceType.WORK.value)
    }

    /**
     * Parse a resource type.
     *
     * @param value resource type value
     * @return ResourceType instance
     */
    fun parseResourceType(value: String): ResourceType {
        return ResourceType.getInstance(NumberHelper.getInt(value))
    }

    /**
     * Print a work group.
     *
     * @param value WorkGroup instance
     * @return work group value
     */
    fun printWorkGroup(value: WorkGroup?): String {
        return Integer.toString(if (value == null) WorkGroup.DEFAULT.getValue() else value!!.getValue())
    }

    /**
     * Parse a work group.
     *
     * @param value work group value
     * @return WorkGroup instance
     */
    fun parseWorkGroup(value: String): WorkGroup {
        return WorkGroup.getInstance(NumberHelper.getInt(value))
    }

    /**
     * Print a work contour.
     *
     * @param value WorkContour instance
     * @return work contour value
     */
    fun printWorkContour(value: WorkContour?): String {
        return Integer.toString(if (value == null) WorkContour.FLAT.getValue() else value!!.getValue())
    }

    /**
     * Parse a work contour.
     *
     * @param value work contour value
     * @return WorkContour instance
     */
    fun parseWorkContour(value: String): WorkContour {
        return WorkContour.getInstance(NumberHelper.getInt(value))
    }

    /**
     * Print a booking type.
     *
     * @param value BookingType instance
     * @return booking type value
     */
    fun printBookingType(value: BookingType?): String {
        return Integer.toString(if (value == null) BookingType.COMMITTED.getValue() else value!!.getValue())
    }

    /**
     * Parse a booking type.
     *
     * @param value booking type value
     * @return BookingType instance
     */
    fun parseBookingType(value: String): BookingType {
        return BookingType.getInstance(NumberHelper.getInt(value))
    }

    /**
     * Print a task type.
     *
     * @param value TaskType instance
     * @return task type value
     */
    fun printTaskType(value: TaskType?): String {
        return Integer.toString(if (value == null) TaskType.FIXED_UNITS.getValue() else value!!.getValue())
    }

    /**
     * Parse a task type.
     *
     * @param value task type value
     * @return TaskType instance
     */
    fun parseTaskType(value: String): TaskType {
        return TaskType.getInstance(NumberHelper.getInt(value))
    }

    /**
     * Print an earned value method.
     *
     * @param value EarnedValueMethod instance
     * @return earned value method value
     */
    fun printEarnedValueMethod(value: EarnedValueMethod?): BigInteger {
        return if (value == null) BigInteger.valueOf(EarnedValueMethod.PERCENT_COMPLETE.getValue()) else BigInteger.valueOf(value!!.getValue())
    }

    /**
     * Parse an earned value method.
     *
     * @param value earned value method
     * @return EarnedValueMethod instance
     */
    fun parseEarnedValueMethod(value: Number): EarnedValueMethod {
        return EarnedValueMethod.getInstance(NumberHelper.getInt(value))
    }

    /**
     * Print units.
     *
     * @param value units value
     * @return units value
     */
    fun printUnits(value: Number?): BigDecimal {
        return if (value == null) BIGDECIMAL_ONE else BigDecimal(value.doubleValue() / 100)
    }

    /**
     * Parse units.
     *
     * @param value units value
     * @return units value
     */
    fun parseUnits(value: Number?): Number? {
        return if (value == null) null else NumberHelper.getDouble(value.doubleValue() * 100)
    }

    /**
     * Print time unit.
     *
     * @param value TimeUnit instance
     * @return time unit value
     */
    fun printTimeUnit(value: TimeUnit?): BigInteger {
        return BigInteger.valueOf(if (value == null) TimeUnit.DAYS.getValue() + 1 else value!!.getValue() + 1)
    }

    /**
     * Parse time unit.
     *
     * @param value time unit value
     * @return TimeUnit instance
     */
    fun parseTimeUnit(value: Number): TimeUnit {
        return TimeUnit.getInstance(NumberHelper.getInt(value) - 1)
    }

    /**
     * Print time.
     *
     * @param value time value
     * @return calendar value
     */
    fun printTime(value: Date?): String? {
        var result: String? = null

        if (value != null) {
            result = TIME_FORMAT.get().format(value)
        }

        return result
    }

    /**
     * Parse work units.
     *
     * @param value work units value
     * @return TimeUnit instance
     */
    fun parseWorkUnits(value: BigInteger?): TimeUnit {
        var result = TimeUnit.HOURS

        if (value != null) {
            when (value!!.intValue()) {
                1 -> {
                    result = TimeUnit.MINUTES
                }

                3 -> {
                    result = TimeUnit.DAYS
                }

                4 -> {
                    result = TimeUnit.WEEKS
                }

                5 -> {
                    result = TimeUnit.MONTHS
                }

                7 -> {
                    result = TimeUnit.YEARS
                }
                2 -> {
                    result = TimeUnit.HOURS
                }

                else -> {
                    result = TimeUnit.HOURS
                }
            }
        }

        return result
    }

    /**
     * Print work units.
     *
     * @param value TimeUnit instance
     * @return work units value
     */
    fun printWorkUnits(value: TimeUnit?): BigInteger {
        var value = value
        val result: Int

        if (value == null) {
            value = TimeUnit.HOURS
        }

        when (value) {
            MINUTES -> {
                result = 1
            }

            DAYS -> {
                result = 3
            }

            WEEKS -> {
                result = 4
            }

            MONTHS -> {
                result = 5
            }

            YEARS -> {
                result = 7
            }
            HOURS -> {
                result = 2
            }

            else -> {
                result = 2
            }
        }

        return BigInteger.valueOf(result)
    }

    /**
     * Parse a duration.
     *
     * @param file parent file
     * @param defaultUnits default time units for the resulting duration
     * @param value duration value
     * @return Duration instance
     */
    fun parseDuration(file: ProjectFile, defaultUnits: TimeUnit?, value: String?): Duration? {
        var defaultUnits = defaultUnits
        var result: Duration? = null
        var xsd: XsdDuration? = null

        if (value != null && value.length() !== 0) {
            try {
                xsd = XsdDuration(value)
            } catch (ex: IllegalArgumentException) {
                // The duration is malformed.
                // MS Project simply ignores values like this.
            }

        }

        if (xsd != null) {
            var units = TimeUnit.DAYS

            if (xsd.seconds != 0.0 || xsd.minutes != 0) {
                units = TimeUnit.MINUTES
            }

            if (xsd.hours != 0) {
                units = TimeUnit.HOURS
            }

            if (xsd.days != 0) {
                units = TimeUnit.DAYS
            }

            if (xsd.months != 0) {
                units = TimeUnit.MONTHS
            }

            if (xsd.years != 0) {
                units = TimeUnit.YEARS
            }

            var duration = 0.0

            when (units) {
                YEARS -> {
                    //
                    // Calculate the number of years
                    //
                    duration += xsd.years.toDouble()
                    duration += xsd.months.toDouble() / 12
                    duration += xsd.days.toDouble() / 365
                    duration += xsd.hours.toDouble() / (365 * 24)
                    duration += xsd.minutes.toDouble() / (365 * 24 * 60)
                    duration += xsd.seconds / (365 * 24 * 60 * 60)
                }

                ELAPSED_YEARS -> {
                    //
                    // Calculate the number of years
                    //
                    duration += xsd.years.toDouble()
                    duration += xsd.months.toDouble() / 12
                    duration += xsd.days.toDouble() / 365
                    duration += xsd.hours.toDouble() / (365 * 24)
                    duration += xsd.minutes.toDouble() / (365 * 24 * 60)
                    duration += xsd.seconds / (365 * 24 * 60 * 60)
                }

                MONTHS -> {
                    //
                    // Calculate the number of months
                    //
                    duration += (xsd.years * 12).toDouble()
                    duration += xsd.months.toDouble()
                    duration += xsd.days.toDouble() / 30
                    duration += xsd.hours.toDouble() / (30 * 24)
                    duration += xsd.minutes.toDouble() / (30 * 24 * 60)
                    duration += xsd.seconds / (30 * 24 * 60 * 60)
                }

                ELAPSED_MONTHS -> {
                    //
                    // Calculate the number of months
                    //
                    duration += (xsd.years * 12).toDouble()
                    duration += xsd.months.toDouble()
                    duration += xsd.days.toDouble() / 30
                    duration += xsd.hours.toDouble() / (30 * 24)
                    duration += xsd.minutes.toDouble() / (30 * 24 * 60)
                    duration += xsd.seconds / (30 * 24 * 60 * 60)
                }

                WEEKS -> {
                    //
                    // Calculate the number of weeks
                    //
                    duration += (xsd.years * 52).toDouble()
                    duration += (xsd.months * 4).toDouble()
                    duration += xsd.days.toDouble() / 7
                    duration += xsd.hours.toDouble() / (7 * 24)
                    duration += xsd.minutes.toDouble() / (7 * 24 * 60)
                    duration += xsd.seconds / (7 * 24 * 60 * 60)
                }

                ELAPSED_WEEKS -> {
                    //
                    // Calculate the number of weeks
                    //
                    duration += (xsd.years * 52).toDouble()
                    duration += (xsd.months * 4).toDouble()
                    duration += xsd.days.toDouble() / 7
                    duration += xsd.hours.toDouble() / (7 * 24)
                    duration += xsd.minutes.toDouble() / (7 * 24 * 60)
                    duration += xsd.seconds / (7 * 24 * 60 * 60)
                }

                DAYS -> {
                    //
                    // Calculate the number of days
                    //
                    duration += (xsd.years * 365).toDouble()
                    duration += (xsd.months * 30).toDouble()
                    duration += xsd.days.toDouble()
                    duration += xsd.hours.toDouble() / 24
                    duration += xsd.minutes.toDouble() / (24 * 60)
                    duration += xsd.seconds / (24 * 60 * 60)
                }

                ELAPSED_DAYS -> {
                    //
                    // Calculate the number of days
                    //
                    duration += (xsd.years * 365).toDouble()
                    duration += (xsd.months * 30).toDouble()
                    duration += xsd.days.toDouble()
                    duration += xsd.hours.toDouble() / 24
                    duration += xsd.minutes.toDouble() / (24 * 60)
                    duration += xsd.seconds / (24 * 60 * 60)
                }

                HOURS, ELAPSED_HOURS -> {
                    //
                    // Calculate the number of hours
                    //
                    duration += (xsd.years * (365 * 24)).toDouble()
                    duration += (xsd.months * (30 * 24)).toDouble()
                    duration += (xsd.days * 24).toDouble()
                    duration += xsd.hours.toDouble()
                    duration += xsd.minutes.toDouble() / 60
                    duration += xsd.seconds / (60 * 60)
                }

                MINUTES, ELAPSED_MINUTES -> {
                    //
                    // Calculate the number of minutes
                    //
                    duration += (xsd.years * (365 * 24 * 60)).toDouble()
                    duration += (xsd.months * (30 * 24 * 60)).toDouble()
                    duration += (xsd.days * (24 * 60)).toDouble()
                    duration += (xsd.hours * 60).toDouble()
                    duration += xsd.minutes.toDouble()
                    duration += xsd.seconds / 60
                }

                else -> {
                }
            }

            //
            // Convert from a duration in hours to a duration
            // expressed in the default duration units
            //
            val properties = file.projectProperties
            if (defaultUnits == null) {
                defaultUnits = properties.defaultDurationUnits
            }

            result = Duration.convertUnits(duration, units, defaultUnits, properties)
        }

        return result
    }

    /**
     * Print duration.
     *
     * Note that Microsoft's xsd:duration parser implementation does not
     * appear to recognise durations other than those expressed in hours.
     * We use the compatibility flag to determine whether the output
     * is adjusted for the benefit of Microsoft Project.
     *
     * @param writer parent MSPDIWriter instance
     * @param duration Duration value
     * @return xsd:duration value
     */
    fun printDuration(writer: MSPDIWriter, duration: Duration?): String? {
        var result: String? = null

        if (duration != null && duration!!.getDuration() !== 0) {
            result = printDurationMandatory(writer, duration)
        }

        return result
    }

    /**
     * Print duration.
     *
     * Note that Microsoft's xsd:duration parser implementation does not
     * appear to recognise durations other than those expressed in hours.
     * We use the compatibility flag to determine whether the output
     * is adjusted for the benefit of Microsoft Project.
     *
     * @param writer parent MSPDIWriter instance
     * @param duration Duration value
     * @return xsd:duration value
     */
    fun printDurationMandatory(writer: MSPDIWriter, duration: Duration?): String {
        var duration = duration
        val result: String

        if (duration == null) {
            // SF-329: null default required to keep Powerproject happy when importing MSPDI files
            result = "PT0H0M0S"
        } else {
            val durationType = duration!!.getUnits()

            if (durationType === TimeUnit.HOURS || durationType === TimeUnit.ELAPSED_HOURS) {
                result = XsdDuration(duration).toString()
            } else {
                duration = duration!!.convertUnits(TimeUnit.HOURS, writer.projectFile!!.projectProperties)
                result = XsdDuration(duration).toString()
            }
        }

        return result
    }

    /**
     * Print duration time units.
     *
     * @param duration Duration value
     * @param estimated is this an estimated duration
     * @return time units value
     */
    fun printDurationTimeUnits(duration: Duration?, estimated: Boolean): BigInteger {
        // SF-329: null default required to keep Powerproject happy when importing MSPDI files
        val units = if (duration == null) PARENT_FILE.get().getProjectProperties().getDefaultDurationUnits() else duration!!.getUnits()
        return printDurationTimeUnits(units, estimated)
    }

    /**
     * Parse currency.
     *
     * @param value currency value
     * @return currency value
     */
    fun parseCurrency(value: Number?): Double? {
        return if (value == null) null else NumberHelper.getDouble(value.doubleValue() / 100)
    }

    /**
     * Print currency.
     *
     * @param value currency value
     * @return currency value
     */
    fun printCurrency(value: Number?): BigDecimal? {
        return if (value == null || value.doubleValue() === 0) null else BigDecimal(value.doubleValue() * 100)
    }

    /**
     * Parse duration time units.
     *
     * Note that we don't differentiate between confirmed and unconfirmed
     * durations. Unrecognised duration types are default the supplied default value.
     *
     * @param value BigInteger value
     * @param defaultValue if value is null, use this value as the result
     * @return Duration units
     */
    @JvmOverloads
    fun parseDurationTimeUnits(value: BigInteger?, defaultValue: TimeUnit = TimeUnit.HOURS): TimeUnit {
        var result = defaultValue

        if (value != null) {
            when (value!!.intValue()) {
                3, 35 -> {
                    result = TimeUnit.MINUTES
                }

                4, 36 -> {
                    result = TimeUnit.ELAPSED_MINUTES
                }

                5, 37 -> {
                    result = TimeUnit.HOURS
                }

                6, 38 -> {
                    result = TimeUnit.ELAPSED_HOURS
                }

                7, 39, 53 -> {
                    result = TimeUnit.DAYS
                }

                8, 40 -> {
                    result = TimeUnit.ELAPSED_DAYS
                }

                9, 41 -> {
                    result = TimeUnit.WEEKS
                }

                10, 42 -> {
                    result = TimeUnit.ELAPSED_WEEKS
                }

                11, 43 -> {
                    result = TimeUnit.MONTHS
                }

                12, 44 -> {
                    result = TimeUnit.ELAPSED_MONTHS
                }

                19, 51 -> {
                    result = TimeUnit.PERCENT
                }

                20, 52 -> {
                    result = TimeUnit.ELAPSED_PERCENT
                }

                else -> {
                    result = PARENT_FILE.get().getProjectProperties().getDefaultDurationUnits()
                }
            }
        }

        return result
    }

    /**
     * Print duration time units.
     *
     * Note that we don't differentiate between confirmed and unconfirmed
     * durations. Unrecognised duration types are default to hours.
     *
     * @param value Duration units
     * @param estimated is this an estimated duration
     * @return BigInteger value
     */
    fun printDurationTimeUnits(value: TimeUnit?, estimated: Boolean): BigInteger {
        var value = value
        val result: Int

        if (value == null) {
            value = TimeUnit.HOURS
        }

        when (value) {
            MINUTES -> {
                result = if (estimated) 35 else 3
            }

            ELAPSED_MINUTES -> {
                result = if (estimated) 36 else 4
            }

            ELAPSED_HOURS -> {
                result = if (estimated) 38 else 6
            }

            DAYS -> {
                result = if (estimated) 39 else 7
            }

            ELAPSED_DAYS -> {
                result = if (estimated) 40 else 8
            }

            WEEKS -> {
                result = if (estimated) 41 else 9
            }

            ELAPSED_WEEKS -> {
                result = if (estimated) 42 else 10
            }

            MONTHS -> {
                result = if (estimated) 43 else 11
            }

            ELAPSED_MONTHS -> {
                result = if (estimated) 44 else 12
            }

            PERCENT -> {
                result = if (estimated) 51 else 19
            }

            ELAPSED_PERCENT -> {
                result = if (estimated) 52 else 20
            }
            HOURS -> {
                result = if (estimated) 37 else 5
            }

            else -> {
                result = if (estimated) 37 else 5
            }
        }

        return BigInteger.valueOf(result)
    }

    /**
     * Parse priority.
     *
     *
     * @param priority priority value
     * @return Priority instance
     */
    fun parsePriority(priority: BigInteger?): Priority? {
        return if (priority == null) null else Priority.getInstance(priority!!.intValue())
    }

    /**
     * Print priority.
     *
     * @param priority Priority instance
     * @return priority value
     */
    fun printPriority(priority: Priority?): BigInteger {
        var result = Priority.MEDIUM

        if (priority != null) {
            result = priority.value
        }

        return BigInteger.valueOf(result)
    }

    /**
     * Parse duration represented in thousandths of minutes.
     *
     * @param value duration value
     * @return Duration instance
     */
    fun parseDurationInThousanthsOfMinutes(value: Number): Duration? {
        return parseDurationInFractionsOfMinutes(null, value, TimeUnit.MINUTES, 1000)
    }

    /**
     * Parse duration represented in tenths of minutes.
     *
     * @param value duration value
     * @return Duration instance
     */
    fun parseDurationInTenthsOfMinutes(value: Number): Duration? {
        return parseDurationInFractionsOfMinutes(null, value, TimeUnit.MINUTES, 10)
    }

    /**
     * Parse duration represented in thousandths of minutes.
     *
     * @param properties project properties
     * @param value duration value
     * @param targetTimeUnit required output time units
     * @return Duration instance
     */
    fun parseDurationInThousanthsOfMinutes(properties: ProjectProperties, value: Number, targetTimeUnit: TimeUnit): Duration? {
        return parseDurationInFractionsOfMinutes(properties, value, targetTimeUnit, 1000)
    }

    /**
     * Parse duration represented as tenths of minutes.
     *
     * @param properties project properties
     * @param value duration value
     * @param targetTimeUnit required output time units
     * @return Duration instance
     */
    fun parseDurationInTenthsOfMinutes(properties: ProjectProperties, value: Number, targetTimeUnit: TimeUnit): Duration? {
        return parseDurationInFractionsOfMinutes(properties, value, targetTimeUnit, 10)
    }

    /**
     * Print duration in thousandths of minutes.
     *
     * @param duration Duration instance
     * @return duration in thousandths of minutes
     */
    fun printDurationInIntegerThousandthsOfMinutes(duration: Duration?): BigInteger? {
        var result: BigInteger? = null
        if (duration != null && duration!!.getDuration() !== 0) {
            result = BigInteger.valueOf(printDurationFractionsOfMinutes(duration, 1000).toLong())
        }
        return result
    }

    /**
     * Print duration in thousandths of minutes.
     *
     * @param duration Duration instance
     * @return duration in thousandths of minutes
     */
    fun printDurationInDecimalThousandthsOfMinutes(duration: Duration?): BigDecimal? {
        var result: BigDecimal? = null
        if (duration != null && duration!!.getDuration() !== 0) {
            result = BigDecimal.valueOf(printDurationFractionsOfMinutes(duration, 1000))
        }
        return result
    }

    /**
     * Print duration in tenths of minutes.
     *
     * @param duration Duration instance
     * @return duration in tenths of minutes
     */
    fun printDurationInIntegerTenthsOfMinutes(duration: Duration?): BigInteger? {
        var result: BigInteger? = null

        if (duration != null && duration!!.getDuration() !== 0) {
            result = BigInteger.valueOf(printDurationFractionsOfMinutes(duration, 10).toLong())
        }

        return result
    }

    /**
     * Convert the MSPDI representation of a UUID into a Java UUID instance.
     *
     * @param value MSPDI UUID
     * @return Java UUID instance
     */
    fun parseUUID(value: String?): UUID? {
        return if (value == null || value.isEmpty()) null else UUID.fromString(value)
    }

    /**
     * Retrieve a UUID in the form required by MSPDI.
     *
     * @param guid UUID instance
     * @return formatted UUID
     */
    fun printUUID(guid: UUID?): String? {
        return if (guid == null) null else guid!!.toString()
    }

    /**
     * Parse duration represented as an arbitrary fraction of minutes.
     *
     * @param properties project properties
     * @param value duration value
     * @param targetTimeUnit required output time units
     * @param factor required fraction of a minute
     * @return Duration instance
     */
    private fun parseDurationInFractionsOfMinutes(properties: ProjectProperties?, value: Number?, targetTimeUnit: TimeUnit, factor: Int): Duration? {
        var result: Duration? = null

        if (value != null) {
            result = Duration.getInstance(value.intValue() / factor, TimeUnit.MINUTES)
            if (targetTimeUnit !== result!!.getUnits()) {
                result = result!!.convertUnits(targetTimeUnit, properties)
            }
        }

        return result
    }

    /**
     * Print a duration represented by an arbitrary fraction of minutes.
     *
     * @param duration Duration instance
     * @param factor required factor
     * @return duration represented as an arbitrary fraction of minutes
     */
    private fun printDurationFractionsOfMinutes(duration: Duration?, factor: Int): Double {
        var result = 0.0

        if (duration != null) {
            result = duration!!.getDuration()

            when (duration!!.getUnits()) {
                HOURS, ELAPSED_HOURS -> {
                    result *= 60.0
                }

                DAYS -> {
                    result *= (60 * 8).toDouble()
                }

                ELAPSED_DAYS -> {
                    result *= (60 * 24).toDouble()
                }

                WEEKS -> {
                    result *= (60 * 8 * 5).toDouble()
                }

                ELAPSED_WEEKS -> {
                    result *= (60 * 24 * 7).toDouble()
                }

                MONTHS -> {
                    result *= (60 * 8 * 5 * 4).toDouble()
                }

                ELAPSED_MONTHS -> {
                    result *= (60 * 24 * 30).toDouble()
                }

                YEARS -> {
                    result *= (60 * 8 * 5 * 52).toDouble()
                }

                ELAPSED_YEARS -> {
                    result *= (60 * 24 * 365).toDouble()
                }

                else -> {
                }
            }
        }

        result *= factor.toDouble()

        return result
    }

    /**
     * Print rate.
     *
     * @param rate Rate instance
     * @return rate value
     */
    fun printRate(rate: Rate?): BigDecimal? {
        var result: BigDecimal? = null
        if (rate != null && rate.amount != 0.0) {
            result = BigDecimal(rate.amount)
        }
        return result
    }

    /**
     * Parse rate.
     *
     * @param value rate value
     * @return Rate instance
     */
    fun parseRate(value: BigDecimal?): Rate? {
        var result: Rate? = null

        if (value != null) {
            result = Rate(value, TimeUnit.HOURS)
        }

        return result
    }

    /**
     * Print a day.
     *
     * @param day Day instance
     * @return day value
     */
    fun printDay(day: Day?): BigInteger? {
        return if (day == null) null else BigInteger.valueOf(day!!.getValue() - 1)
    }

    /**
     * Parse a day.
     *
     * @param value day value
     * @return Day instance
     */
    fun parseDay(value: Number): Day {
        return Day.getInstance(NumberHelper.getInt(value) + 1)
    }

    /**
     * Parse a constraint type.
     *
     * @param value constraint type value
     * @return ConstraintType instance
     */
    fun parseConstraintType(value: Number): ConstraintType {
        return ConstraintType.getInstance(value)
    }

    /**
     * Print a constraint type.
     *
     * @param value ConstraintType instance
     * @return constraint type value
     */
    fun printConstraintType(value: ConstraintType?): BigInteger? {
        return if (value == null) null else BigInteger.valueOf(value!!.getValue())
    }

    /**
     * Print a task UID.
     *
     * @param value task UID
     * @return task UID string
     */
    fun printTaskUID(value: Integer): String {
        val file = PARENT_FILE.get()
        if (file != null) {
            file!!.eventManager.fireTaskWrittenEvent(file!!.getTaskByUniqueID(value))
        }
        return value.toString()
    }

    /**
     * Parse a task UID.
     *
     * @param value task UID string
     * @return task UID
     */
    fun parseTaskUID(value: String): Integer {
        return Integer.valueOf(value)
    }

    /**
     * Print a resource UID.
     *
     * @param value resource UID value
     * @return resource UID string
     */
    fun printResourceUID(value: Integer): String {
        val file = PARENT_FILE.get()
        if (file != null) {
            file!!.eventManager.fireResourceWrittenEvent(file!!.getResourceByUniqueID(value))
        }
        return value.toString()
    }

    /**
     * Parse a resource UID.
     *
     * @param value resource UID string
     * @return resource UID value
     */
    fun parseResourceUID(value: String): Integer {
        return Integer.valueOf(value)
    }

    /**
     * Print a boolean.
     *
     * @param value boolean
     * @return boolean value
     */
    fun printBoolean(value: Boolean?): String {
        return if (value == null || !value.booleanValue()) "0" else "1"
    }

    /**
     * Parse a boolean.
     *
     * @param value boolean
     * @return Boolean value
     */
    fun parseBoolean(value: String?): Boolean {
        return if (value == null || value.charAt(0) !== '1') Boolean.FALSE else Boolean.TRUE
    }

    /**
     * Parse a time value.
     *
     * @param value time value
     * @return time value
     */
    fun parseTime(value: String?): Date? {
        var result: Date? = null
        if (value != null && value.length() !== 0) {
            try {
                result = TIME_FORMAT.get().parse(value)
            } catch (ex: ParseException) {
                // Ignore parse errors
            }

        }
        return result
    }

    /**
     * Print a date time value.
     *
     * @param value date time value
     * @return string representation
     */
    fun printDateTime(value: Date?): String? {
        return if (value == null) null else DATE_FORMAT.get().format(value)
    }

    /**
     * Parse a date time value.
     *
     * @param value string representation
     * @return date time value
     */
    fun parseDateTime(value: String?): Date? {
        var result: Date? = null

        if (value != null && value.length() !== 0) {
            try {
                result = DATE_FORMAT.get().parse(value)
            } catch (ex: ParseException) {
                // Ignore parse errors
            }

        }

        return result
    }

    /**
     * Print method for a string: returns the string unchanged.
     * This is used to enable to string representation of an
     * xsd:datetime to be generated by MPXJ.
     *
     * @param value string value
     * @return string value
     */
    fun printString(value: String): String {
        return value
    }

    /**
     * Parse method for a string: returns the string unchanged.
     * This is used to enable to string representation of an
     * xsd:datetime to be processed by MPXJ.
     *
     * @param value string value
     * @return string value
     */
    fun parseString(value: String): String {
        return value
    }

    /**
     * This method is called to set the parent file for the current
     * write operation. This allows task and resource write events
     * to be captured and passed to any file listeners.
     *
     * @param file parent file instance
     */
    fun setParentFile(file: ProjectFile) {
        PARENT_FILE.set(file)
    }

    /**
     * Detect numbers using comma as a decimal separator and replace with period.
     *
     * @param value original numeric value
     * @return corrected numeric value
     */
    private fun correctNumberFormat(value: String): String {
        val result: String
        val index = value.indexOf(',')
        if (index == -1) {
            result = value
        } else {
            val chars = value.toCharArray()
            chars[index] = '.'
            result = String(chars)
        }
        return result
    }
}
/**
 * Parse duration time units.
 *
 * Note that we don't differentiate between confirmed and unconfirmed
 * durations. Unrecognised duration types are default to hours.
 *
 * @param value BigInteger value
 * @return Duration units
 */
