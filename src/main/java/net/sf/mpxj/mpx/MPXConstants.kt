/*
 * file:       MPXConstants.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       Jan 17, 2006
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

/**
 * This class contains definitions of constants used when reading and writing
 * MPX files.
 */
internal object MPXConstants {
    /**
     * Constant containing the end of line characters used in MPX files.
     */
    val EOL = "\r\n"

    /**
     * Comment record number.
     */
    val COMMENTS_RECORD_NUMBER = 0

    /**
     * Currency settngs record number.
     */
    val CURRENCY_SETTINGS_RECORD_NUMBER = 10

    /**
     * Default settings record number.
     */
    val DEFAULT_SETTINGS_RECORD_NUMBER = 11

    /**
     * Date time settings record number.
     */
    val DATE_TIME_SETTINGS_RECORD_NUMBER = 12

    /**
     * Base calendar record number.
     */
    val BASE_CALENDAR_RECORD_NUMBER = 20

    /**
     * Base calendar hours record number.
     */
    val BASE_CALENDAR_HOURS_RECORD_NUMBER = 25

    /**
     * Base calendar exception record number.
     */
    val BASE_CALENDAR_EXCEPTION_RECORD_NUMBER = 26

    /**
     * Project header record number.
     */
    val PROJECT_HEADER_RECORD_NUMBER = 30

    /**
     * Resource calendar record number.
     */
    val RESOURCE_CALENDAR_RECORD_NUMBER = 55

    /**
     * Resource calendar hours record number.
     */
    val RESOURCE_CALENDAR_HOURS_RECORD_NUMBER = 56

    /**
     * Resource calendar exception record number.
     */
    val RESOURCE_CALENDAR_EXCEPTION_RECORD_NUMBER = 57

    /**
     * Text resource model record number.
     */
    val RESOURCE_MODEL_TEXT_RECORD_NUMBER = 40

    /**
     * Numeric resource model record number.
     */
    val RESOURCE_MODEL_NUMERIC_RECORD_NUMBER = 41

    /**
     * Resource record number.
     */
    val RESOURCE_RECORD_NUMBER = 50

    /**
     * Resource notes record number.
     */
    val RESOURCE_NOTES_RECORD_NUMBER = 51

    /**
     * Text task model record number.
     */
    val TASK_MODEL_TEXT_RECORD_NUMBER = 60

    /**
     * Numeric task model record number.
     */
    val TASK_MODEL_NUMERIC_RECORD_NUMBER = 61

    /**
     * Task record number.
     */
    val TASK_RECORD_NUMBER = 70

    /**
     * Task notes record number.
     */
    val TASK_NOTES_RECORD_NUMBER = 71

    /**
     * Recurring task record number.
     */
    val RECURRING_TASK_RECORD_NUMBER = 72

    /**
     * Resource assignment record number.
     */
    val RESOURCE_ASSIGNMENT_RECORD_NUMBER = 75

    /**
     * Resource assignment workgroup record number.
     */
    val RESOURCE_ASSIGNMENT_WORKGROUP_FIELDS_RECORD_NUMBER = 76

    /**
     * Project names record number.
     */
    val PROJECT_NAMES_RECORD_NUMBER = 80

    /**
     * DDE OLE client links record number.
     */
    val DDE_OLE_CLIENT_LINKS_RECORD_NUMBER = 81

    /**
     * File creation record number.
     * Note that in this case it is a dummy value, the actual value used
     * in the file is MPX. The dummy value is used to allow all record types
     * to be identified numerically.
     */
    val FILE_CREATION_RECORD_NUMBER = 999

    /**
     * Placeholder character used in MPX files to represent
     * carriage returns embedded in note text.
     */
    val EOL_PLACEHOLDER = 0x7F.toChar()
    val EOL_PLACEHOLDER_STRING = String(byteArrayOf(EOL_PLACEHOLDER.toByte()))
}
/**
 * Constructor.
 */// private constructor to prevent instantiation
