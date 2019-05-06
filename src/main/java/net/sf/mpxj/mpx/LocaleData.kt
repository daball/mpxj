/*
 * file:       LocaleData.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2004
 * date:       24/03/2004
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

import java.util.HashMap
import java.util.ListResourceBundle
import java.util.Locale
import java.util.ResourceBundle

import net.sf.mpxj.CodePage
import net.sf.mpxj.CurrencySymbolPosition
import net.sf.mpxj.DateOrder
import net.sf.mpxj.ProjectDateFormat
import net.sf.mpxj.ProjectTimeFormat
import net.sf.mpxj.TimeUnit

/**
 * This class defines utility routines for handling resources, and also
 * the default set of English resources used in MPX files.
 */
class LocaleData : ListResourceBundle() {
    /**
     * {@inheritDoc}
     */
    val contents: Array<Array<Object>>
        @Override get() = RESOURCE_DATA

    companion object {

        /**
         * Convenience method for retrieving a String resource.
         *
         * @param locale locale identifier
         * @param key resource key
         * @return resource value
         */
        fun getString(locale: Locale, key: String): String {
            val bundle = ResourceBundle.getBundle(LocaleData::class.java!!.getName(), locale)
            return bundle.getString(key)
        }

        /**
         * Convenience method for retrieving a String[] resource.
         *
         * @param locale locale identifier
         * @param key resource key
         * @return resource value
         */
        fun getStringArray(locale: Locale, key: String): Array<String> {
            val bundle = ResourceBundle.getBundle(LocaleData::class.java!!.getName(), locale)
            return bundle.getStringArray(key)
        }

        /**
         * Convenience method for retrieving a String[][] resource.
         *
         * @param locale locale identifier
         * @param key resource key
         * @return resource value
         */
        fun getStringArrays(locale: Locale, key: String): Array<Array<String>> {
            val bundle = ResourceBundle.getBundle(LocaleData::class.java!!.getName(), locale)
            return bundle.getObject(key)
        }

        /**
         * Convenience method for retrieving an Object resource.
         *
         * @param locale locale identifier
         * @param key resource key
         * @return resource value
         */
        fun getObject(locale: Locale, key: String): Object {
            val bundle = ResourceBundle.getBundle(LocaleData::class.java!!.getName(), locale)
            return bundle.getObject(key)
        }

        /**
         * Convenience method for retrieving a Map resource.
         *
         * @param locale locale identifier
         * @param key resource key
         * @return resource value
         */
        @SuppressWarnings("rawtypes")
        fun getMap(locale: Locale, key: String): Map {
            val bundle = ResourceBundle.getBundle(LocaleData::class.java!!.getName(), locale)
            return bundle.getObject(key)
        }

        /**
         * Convenience method for retrieving an Integer resource.
         *
         * @param locale locale identifier
         * @param key resource key
         * @return resource value
         */
        fun getInteger(locale: Locale, key: String): Integer {
            val bundle = ResourceBundle.getBundle(LocaleData::class.java!!.getName(), locale)
            return bundle.getObject(key) as Integer
        }

        /**
         * Convenience method for retrieving a char resource.
         *
         * @param locale locale identifier
         * @param key resource key
         * @return resource value
         */
        fun getChar(locale: Locale, key: String): Char {
            val bundle = ResourceBundle.getBundle(LocaleData::class.java!!.getName(), locale)
            return bundle.getString(key).charAt(0)
        }

        val FILE_DELIMITER = "FILE_DELIMITER"
        val PROGRAM_NAME = "PROGRAM_NAME"
        val FILE_VERSION = "FILE_VERSION"
        val CODE_PAGE = "CODE_PAGE"

        val YES = "YES"
        val NO = "NO"

        val CURRENCY_SYMBOL = "CURRENCY_SYMBOL"
        val CURRENCY_SYMBOL_POSITION = "CURRENCY_SYMBOL_POSITION"
        val CURRENCY_DIGITS = "CURRENCY_DIGITS"
        val CURRENCY_THOUSANDS_SEPARATOR = "CURRENCY_THOUSANDS_SEPARATOR"
        val CURRENCY_DECIMAL_SEPARATOR = "CURRENCY_DECIMAL_SEPARATOR"

        val DATE_ORDER = "DATE_ORDER"
        val TIME_FORMAT = "TIME_FORMAT"
        val DEFAULT_START_TIME = "DEFAULT_START_TIME"
        val DATE_SEPARATOR = "DATE_SEPARATOR"
        val TIME_SEPARATOR = "TIME_SEPARATOR"
        val AM_TEXT = "AM_TEXT"
        val PM_TEXT = "PM_TEXT"
        val DATE_FORMAT = "DATE_FORMAT"
        val BAR_TEXT_DATE_FORMAT = "BAR_TEXT_DATE_FORMAT"
        val NA = "NA"

        val TIME_UNITS_ARRAY = "TIME_UNITS_ARRAY"
        val TIME_UNITS_MAP = "TIME_UNITS_MAP"

        val ACCRUE_TYPES = "ACCRUE_TYPES"
        val RELATION_TYPES = "RELATION_TYPES"
        val PRIORITY_TYPES = "PRIORITY_TYPES"
        val CONSTRAINT_TYPES = "CONSTRAINT_TYPES"

        val TASK_NAMES = "TASK_NAMES"
        val RESOURCE_NAMES = "RESOURCE_NAMES"

        private val TIME_UNITS_ARRAY_DATA = arrayOfNulls<Array<String>>(TimeUnit.values().length)

        init {
            TIME_UNITS_ARRAY_DATA[TimeUnit.MINUTES.getValue()] = arrayOf("m", "mins")

            TIME_UNITS_ARRAY_DATA[TimeUnit.HOURS.getValue()] = arrayOf("h", "hours")

            TIME_UNITS_ARRAY_DATA[TimeUnit.DAYS.getValue()] = arrayOf("d", "days")

            TIME_UNITS_ARRAY_DATA[TimeUnit.WEEKS.getValue()] = arrayOf("w", "weeks")

            TIME_UNITS_ARRAY_DATA[TimeUnit.MONTHS.getValue()] = arrayOf("mon", "months")

            TIME_UNITS_ARRAY_DATA[TimeUnit.YEARS.getValue()] = arrayOf("y", "years")

            TIME_UNITS_ARRAY_DATA[TimeUnit.PERCENT.getValue()] = arrayOf("%")

            TIME_UNITS_ARRAY_DATA[TimeUnit.ELAPSED_MINUTES.getValue()] = arrayOf("em")

            TIME_UNITS_ARRAY_DATA[TimeUnit.ELAPSED_HOURS.getValue()] = arrayOf("eh")

            TIME_UNITS_ARRAY_DATA[TimeUnit.ELAPSED_DAYS.getValue()] = arrayOf("ed")

            TIME_UNITS_ARRAY_DATA[TimeUnit.ELAPSED_WEEKS.getValue()] = arrayOf("ew")

            TIME_UNITS_ARRAY_DATA[TimeUnit.ELAPSED_MONTHS.getValue()] = arrayOf("emon")

            TIME_UNITS_ARRAY_DATA[TimeUnit.ELAPSED_YEARS.getValue()] = arrayOf("ey")

            TIME_UNITS_ARRAY_DATA[TimeUnit.ELAPSED_PERCENT.getValue()] = arrayOf("e%")
        }

        private val TIME_UNITS_MAP_DATA = HashMap<String, Integer>()

        init {
            for (loop in TIME_UNITS_ARRAY_DATA.indices) {
                val value = Integer.valueOf(loop)
                for (name in TIME_UNITS_ARRAY_DATA[loop]) {
                    TIME_UNITS_MAP_DATA.put(name, value)
                }
            }
        }

        private val ACCRUE_TYPES_DATA = arrayOf("Start", "End", "Prorated")

        private val RELATION_TYPES_DATA = arrayOf("FF", "FS", "SF", "SS")

        private val PRIORITY_TYPES_DATA = arrayOf("Lowest", "Very Low", "Lower", "Low", "Medium", "High", "Higher", "Very High", "Highest", "Do Not Level")

        private val CONSTRAINT_TYPES_DATA = arrayOf("As Soon As Possible", "As Late As Possible", "Must Start On", "Must Finish On", "Start No Earlier Than", "Start No Later Than", "Finish No Earlier Than", "Finish No Later Than")

        private val TASK_NAMES_DATA = arrayOf<String>(null, "Name", "WBS", "Outline Level", "Text1", "Text2", "Text3", "Text4", "Text5", "Text6", "Text7", "Text8", "Text9", "Text10", "Notes", "Contact", "Resource Group", null, null, null, "Work", "Baseline Work", "Actual Work", "Remaining Work", "Work Variance", "% Work Complete", null, null, null, null, "Cost", "Baseline Cost", "Actual Cost", "Remaining Cost", "Cost Variance", "Fixed Cost", "Cost1", "Cost2", "Cost3", null, "Duration", "Baseline Duration", "Actual Duration", "Remaining Duration", "% Complete", "Duration Variance", "Duration1", "Duration2", "Duration3", null, "Start", "Finish", "Early Start", "Early Finish", "Late Start", "Late Finish", "Baseline Start", "Baseline Finish", "Actual Start", "Actual Finish", "Start1", "Finish1", "Start2", "Finish2", "Start3", "Finish3", "Start Variance", "Finish Variance", "Constraint Date", null, "Predecessors", "Successors", "Resource Names", "Resource Initials", "Unique ID Predecessors", "Unique ID Successors", null, null, null, null, "Fixed", "Milestone", "Critical", "Marked", "Rollup", "BCWS", "BCWP", "SV", "CV", null, "ID", "Constraint Type", "Delay", "Free Slack", "Total Slack", "Priority", "Subproject File", "Project", "Unique ID", "Outline Number", null, null, null, null, null, null, null, null, null, null, "Flag1", "Flag2", "Flag3", "Flag4", "Flag5", "Flag6", "Flag7", "Flag8", "Flag9", "Flag10", "Summary", "Objects", "Linked Fields", "Hide Bar", null, "Created", "Start4", "Finish4", "Start5", "Finish5", null, null, null, null, null, "Confirmed", "Update Needed", null, null, null, "Number1", "Number2", "Number3", "Number4", "Number5", null, null, null, null, null, "Stop", "Resume No Earlier Than", "Resume")

        private val RESOURCE_NAMES_DATA = arrayOf<String>(null, "Name", "Initials", "Group", "Code", "Text1", "Text2", "Text3", "Text4", "Text5", "Notes", "Email Address", null, null, null, null, null, null, null, null, "Work", "Baseline Work", "Actual Work", "Remaining Work", "Overtime Work", "Work Variance", "% Work Complete", null, null, null, "Cost", "Baseline Cost", "Actual Cost", "Remaining Cost", "Cost Variance", null, null, null, null, null, "ID", "Max Units", "Standard Rate", "Overtime Rate", "Cost Per Use", "Accrue At", "Overallocated", "Peak", "Base Calendar", "Unique ID", "Objects", "Linked Fields")

        private val RESOURCE_DATA = arrayOf<Array<Object>>(arrayOf<Object>(FILE_DELIMITER, ","), arrayOf<Object>(PROGRAM_NAME, "Microsoft Project for Windows"), arrayOf<Object>(FILE_VERSION, "4.0"), arrayOf<Object>(CODE_PAGE, CodePage.ANSI),

                arrayOf<Object>(CURRENCY_SYMBOL, "$"), arrayOf<Object>(CURRENCY_SYMBOL_POSITION, CurrencySymbolPosition.BEFORE), arrayOf<Object>(CURRENCY_DIGITS, Integer.valueOf(2)), arrayOf<Object>(CURRENCY_THOUSANDS_SEPARATOR, ","), arrayOf<Object>(CURRENCY_DECIMAL_SEPARATOR, "."),

                arrayOf<Object>(DATE_ORDER, DateOrder.DMY), arrayOf<Object>(TIME_FORMAT, ProjectTimeFormat.TWELVE_HOUR), arrayOf<Object>(DEFAULT_START_TIME, Integer.valueOf(480)), arrayOf<Object>(DATE_SEPARATOR, "/"), arrayOf<Object>(TIME_SEPARATOR, ":"), arrayOf<Object>(AM_TEXT, "am"), arrayOf<Object>(PM_TEXT, "pm"), arrayOf<Object>(DATE_FORMAT, ProjectDateFormat.DD_MM_YYYY), arrayOf<Object>(BAR_TEXT_DATE_FORMAT, Integer.valueOf(0)), arrayOf<Object>(NA, "NA"),

                arrayOf<Object>(YES, "Yes"), arrayOf<Object>(NO, "No"),

                arrayOf<Object>(TIME_UNITS_ARRAY, TIME_UNITS_ARRAY_DATA), arrayOf<Object>(TIME_UNITS_MAP, TIME_UNITS_MAP_DATA),

                arrayOf<Object>(ACCRUE_TYPES, ACCRUE_TYPES_DATA), arrayOf<Object>(RELATION_TYPES, RELATION_TYPES_DATA), arrayOf<Object>(PRIORITY_TYPES, PRIORITY_TYPES_DATA), arrayOf<Object>(CONSTRAINT_TYPES, CONSTRAINT_TYPES_DATA),

                arrayOf<Object>(TASK_NAMES, TASK_NAMES_DATA), arrayOf<Object>(RESOURCE_NAMES, RESOURCE_NAMES_DATA))
    }
}
