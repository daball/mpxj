/*
 * file:       Props.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2003
 * date:       27/05/2003
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

package net.sf.mpxj.mpp

import java.io.PrintWriter
import java.io.StringWriter
import java.util.Date
import java.util.TreeMap

import net.sf.mpxj.common.ByteArrayHelper

/**
 * This class represents the common structure of Props files found in
 * Microsoft Project MPP files. The MPP8 and MPP9 file formats both
 * implement Props files slightly differently, so this class contains
 * the shared implementation detail, with specific implementations for
 * MPP8 and MPP9 Props files found in the Props8 and Props9 classes.
 */
internal open class Props : MPPComponent() {

    protected var m_map = TreeMap<Integer, ByteArray>()
    /**
     * Retrieve property data as a byte array.
     *
     * @param type Type identifier
     * @return  byte array of data
     */
    fun getByteArray(type: Integer): ByteArray {
        return m_map.get(type)
    }

    /**
     * Retrieves a byte value from the property data.
     *
     * @param type Type identifier
     * @return byte value
     */
    fun getByte(type: Integer): Byte {
        var result: Byte = 0

        val item = m_map.get(type)
        if (item != null) {
            result = item!![0]
        }

        return result
    }

    /**
     * Retrieves a short int value from the property data.
     *
     * @param type Type identifier
     * @return short int value
     */
    fun getShort(type: Integer): Int {
        var result = 0

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getShort(item, 0)
        }

        return result
    }

    /**
     * Retrieves an integer value from the property data.
     *
     * @param type Type identifier
     * @return integer value
     */
    fun getInt(type: Integer): Int {
        var result = 0

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getInt(item, 0)
        }

        return result
    }

    /**
     * Retrieves a double value from the property data.
     *
     * @param type Type identifier
     * @return double value
     */
    fun getDouble(type: Integer): Double {
        var result = 0.0

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getDouble(item, 0)
        }

        return result
    }

    /**
     * Retrieves a timestamp from the property data.
     *
     * @param type Type identifier
     * @return timestamp
     */
    fun getTime(type: Integer): Date? {
        var result: Date? = null

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getTime(item, 0)
        }

        return result
    }

    /**
     * Retrieves a timestamp from the property data.
     *
     * @param type Type identifier
     * @return timestamp
     */
    fun getTimestamp(type: Integer): Date? {
        var result: Date? = null

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getTimestamp(item, 0)
        }

        return result
    }

    /**
     * Retrieves a boolean value from the property data.
     *
     * @param type Type identifier
     * @return boolean value
     */
    fun getBoolean(type: Integer): Boolean {
        var result = false

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getShort(item, 0) != 0
        }

        return result
    }

    /**
     * Retrieves a string value from the property data.
     *
     * @param type Type identifier
     * @return string value
     */
    fun getUnicodeString(type: Integer): String? {
        var result: String? = null

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getUnicodeString(item, 0)
        }

        return result
    }

    /**
     * Retrieves a date value from the property data.
     *
     * @param type Type identifier
     * @return string value
     */
    fun getDate(type: Integer): Date? {
        var result: Date? = null

        val item = m_map.get(type)
        if (item != null) {
            result = MPPUtility.getDate(item, 0)
        }

        return result
    }

    /**
     * Retrieve the set of keys represented by this instance.
     *
     * @return key set
     */
    fun keySet(): Set<Integer> {
        return m_map.keySet()
    }

    /**
     * This method dumps the contents of this properties block as a String.
     * Note that this facility is provided as a debugging aid.
     *
     * @return formatted contents of this block
     */
    @Override
    open fun toString(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        pw.println("BEGIN Props")

        for (entry in m_map.entrySet()) {
            pw.println("   Key: " + entry.getKey() + " Value: ")
            pw.println(ByteArrayHelper.hexdump(entry.getValue(), true, 16, "      "))
        }

        pw.println("END Props")

        pw.println()
        pw.close()
        return sw.toString()
    }

    companion object {

        /**
         * Data types.
         */
        val PROJECT_START_DATE = Integer.valueOf(37748738)
        val PROJECT_FINISH_DATE = Integer.valueOf(37748739)
        val SCHEDULE_FROM = Integer.valueOf(37748740)
        val DEFAULT_CALENDAR_NAME = Integer.valueOf(37748750)
        val CURRENCY_SYMBOL = Integer.valueOf(37748752)
        val CURRENCY_PLACEMENT = Integer.valueOf(37748753)
        val CURRENCY_DIGITS = Integer.valueOf(37748754)

        val CRITICAL_SLACK_LIMIT = Integer.valueOf(37748756)
        val DURATION_UNITS = Integer.valueOf(37748757)
        val WORK_UNITS = Integer.valueOf(37748758)
        val TASK_UPDATES_RESOURCE = Integer.valueOf(37748761)
        val SPLIT_TASKS = Integer.valueOf(37748762)
        val START_TIME = Integer.valueOf(37748764)
        val MINUTES_PER_DAY = Integer.valueOf(37748765)
        val MINUTES_PER_WEEK = Integer.valueOf(37748766)
        val STANDARD_RATE = Integer.valueOf(37748767)
        val OVERTIME_RATE = Integer.valueOf(37748768)
        val END_TIME = Integer.valueOf(37748769)

        val WEEK_START_DAY = Integer.valueOf(37748773)
        val FISCAL_YEAR_START_MONTH = Integer.valueOf(37748780)
        val DEFAULT_TASK_TYPE = Integer.valueOf(37748785)
        val HONOR_CONSTRAINTS = Integer.valueOf(37748794)
        val FISCAL_YEAR_START = Integer.valueOf(37748801)
        val EDITABLE_ACTUAL_COSTS = Integer.valueOf(37748802)

        val DAYS_PER_MONTH = Integer.valueOf(37753743)

        val CURRENCY_CODE = Integer.valueOf(37753787)

        val CALCULATE_MULTIPLE_CRITICAL_PATHS = Integer.valueOf(37748793)

        val TASK_FIELD_NAME_ALIASES = Integer.valueOf(1048577)
        val RESOURCE_FIELD_NAME_ALIASES = Integer.valueOf(1048578)

        val PASSWORD_FLAG = Integer.valueOf(893386752)

        val PROTECTION_PASSWORD_HASH = Integer.valueOf(893386756)

        val WRITE_RESERVATION_PASSWORD_HASH = Integer.valueOf(893386757)

        val ENCRYPTION_CODE = Integer.valueOf(893386759)

        val STATUS_DATE = Integer.valueOf(37748805)

        val SUBPROJECT_COUNT = Integer.valueOf(37748868)
        val SUBPROJECT_DATA = Integer.valueOf(37748898)
        val SUBPROJECT_TASK_COUNT = Integer.valueOf(37748900)

        val DEFAULT_CALENDAR_HOURS = Integer.valueOf(37753736)

        val TASK_FIELD_ATTRIBUTES = Integer.valueOf(37753744)

        val FONT_BASES = Integer.valueOf(54525952)

        val AUTO_FILTER = Integer.valueOf(893386767)

        val PROJECT_FILE_PATH = Integer.valueOf(893386760)

        val HYPERLINK_BASE = Integer.valueOf(37748810)

        val RESOURCE_CREATION_DATE = Integer.valueOf(205521219)

        val SHOW_PROJECT_SUMMARY_TASK = Integer.valueOf(54525961)

        val TASK_FIELD_MAP = Integer.valueOf(131092)
        val TASK_FIELD_MAP2 = Integer.valueOf(50331668)
        val ENTERPRISE_CUSTOM_FIELD_MAP = Integer.valueOf(37753797) // MPP14 37753768?

        val RESOURCE_FIELD_MAP = Integer.valueOf(131093)
        val RESOURCE_FIELD_MAP2 = Integer.valueOf(50331669)

        val RELATION_FIELD_MAP = Integer.valueOf(131094)

        val ASSIGNMENT_FIELD_MAP = Integer.valueOf(131095)
        val ASSIGNMENT_FIELD_MAP2 = Integer.valueOf(50331671)

        val BASELINE_DATE = Integer.valueOf(37753749)
        val BASELINE1_DATE = Integer.valueOf(37753750)
        val BASELINE2_DATE = Integer.valueOf(37753751)
        val BASELINE3_DATE = Integer.valueOf(37753752)
        val BASELINE4_DATE = Integer.valueOf(37753753)
        val BASELINE5_DATE = Integer.valueOf(37753754)
        val BASELINE6_DATE = Integer.valueOf(37753755)
        val BASELINE7_DATE = Integer.valueOf(37753756)
        val BASELINE8_DATE = Integer.valueOf(37753757)
        val BASELINE9_DATE = Integer.valueOf(37753758)
        val BASELINE10_DATE = Integer.valueOf(37753759)

        val CUSTOM_FIELDS = Integer.valueOf(71303169)
    }
}
