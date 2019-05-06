/*
 * file:       MPXJException.java
 * author:     Scott Melville
 *             Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
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

package net.sf.mpxj

/**
 * Standard exception type thrown by the MPXJ library.
 */
class MPXJException : Exception {
    /**
     * Constructor allowing a message to be added to this exception.
     *
     * @param message message
     */
    constructor(message: String) : super(message) {}

    /**
     * Constructor for an exception containing a message and an embedded
     * exception.
     *
     * @param message message
     * @param exception original exception
     */
    constructor(message: String, exception: Exception) : super(message, exception) {}

    companion object {

        /**
         * Maximum records error message.
         */
        val MAXIMUM_RECORDS = "Maximum number of records of this type exist"

        /**
         * Invalid time unit error message.
         */
        val INVALID_TIME_UNIT = "Invalid time unit"

        /**
         * Invalid date error message.
         */
        val INVALID_DATE = "Invalid date"

        /**
         * Invalid number error message.
         */
        val INVALID_NUMBER = "Invalid number or number format"

        /**
         * Invalid file error message.
         */
        val INVALID_FILE = "Invalid file format"

        /**
         * Invalid record error message.
         */
        val INVALID_RECORD = "Invalid record"

        /**
         * Read error message.
         */
        val READ_ERROR = "Error reading file"

        /**
         * Invalid calendar error message.
         */
        val CALENDAR_ERROR = "Invalid calendar"

        /**
         * Invalid outline error message.
         */
        val INVALID_OUTLINE = "Invalid outline level"

        /**
         * Invalid format error message.
         */
        val INVALID_FORMAT = "Invalid format"

        /**
         * Invalid task field name error message.
         */
        val INVALID_TASK_FIELD_NAME = "Invalid task field name"

        /**
         * Invalid resource field name error message.
         */
        val INVALID_RESOURCE_FIELD_NAME = "Invalid resource field name"

        /**
         * Password protected file error message.
         */
        val PASSWORD_PROTECTED = "File is password protected"

        /**
         * Password protected file error message.
         */
        val PASSWORD_PROTECTED_ENTER_PASSWORD = "File is password protected. Please enter password."
    }
}
