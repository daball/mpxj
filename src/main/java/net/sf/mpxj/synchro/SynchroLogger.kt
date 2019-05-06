/*
 * file:       SynchroLogger.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       2018-10-11
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

package net.sf.mpxj.synchro

import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter

import net.sf.mpxj.common.ByteArrayHelper

/**
 * Provides optional logging to assist with development.
 * Disabled unless a log file is specified.
 */
internal object SynchroLogger {

    private var LOG_FILE: String? = null
    private var LOG: PrintWriter? = null

    /**
     * Provide the file path for rudimentary logging to support development.
     *
     * @param logFile full path to log file
     */
    fun setLogFile(logFile: String) {
        LOG_FILE = logFile
    }

    /**
     * Open the log file for writing.
     */
    @Throws(IOException::class)
    fun openLogFile() {
        if (LOG_FILE != null) {
            System.out.println("SynchroLogger Configured")
            LOG = PrintWriter(FileWriter(LOG_FILE))
        }
    }

    /**
     * Close the log file.
     */
    fun closeLogFile() {
        if (LOG_FILE != null) {
            LOG!!.flush()
            LOG!!.close()
        }
    }

    /**
     * Log a byte array.
     *
     * @param label label text
     * @param data byte array
     */
    fun log(label: String, data: ByteArray) {
        if (LOG != null) {
            LOG!!.write(label)
            LOG!!.write(": ")
            LOG!!.println(ByteArrayHelper.hexdump(data, true))
            LOG!!.flush()
        }
    }

    /**
     * Log a string.
     *
     * @param label label text
     * @param data string data
     */
    fun log(label: String, data: String) {
        if (LOG != null) {
            LOG!!.write(label)
            LOG!!.write(": ")
            LOG!!.println(data)
            LOG!!.flush()
        }
    }

    /**
     * Log a table header.
     *
     * @param label label text
     * @param data table header
     */
    fun log(label: String, data: SynchroTable) {
        if (LOG != null) {
            LOG!!.write(label)
            LOG!!.write(": ")
            LOG!!.println(data.toString())
            LOG!!.flush()
        }
    }

    /**
     * Log a byte array as a hex dump.
     *
     * @param data byte array
     */
    fun log(data: ByteArray) {
        if (LOG != null) {
            LOG!!.println(ByteArrayHelper.hexdump(data, true, 16, ""))
            LOG!!.flush()
        }
    }

    /**
     * Log table contents.
     *
     * @param label label text
     * @param klass reader class name
     * @param map table data
     */
    fun log(label: String, klass: Class<*>, map: Map<String, Object>) {
        if (LOG != null) {
            LOG!!.write(label)
            LOG!!.write(": ")
            LOG!!.println(klass.getSimpleName())

            for (entry in map.entrySet()) {
                LOG!!.println(entry.getKey() + ": " + entry.getValue())
            }
            LOG!!.println()
            LOG!!.flush()
        }
    }
}
/**
 * Private constructor to avoid instantiation.
 */
