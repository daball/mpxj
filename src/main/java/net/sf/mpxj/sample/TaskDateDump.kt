/*
 * file:       TaskDateDump.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       14/10/2014
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

package net.sf.mpxj.sample

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.reader.UniversalProjectReader

/**
 * Simple data dump utility. Originally written to allow simple comparison
 * of data read by MPXJ, with activity data exported from Primavera to an Excel spreadsheet.
 */
class TaskDateDump {
    private val m_df = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")

    /**
     * Dump data for all non-summary tasks to stdout.
     *
     * @param name file name
     */
    @Throws(Exception::class)
    fun process(name: String) {
        val file = UniversalProjectReader().read(name)
        for (task in file!!.getTasks()) {
            if (!task.summary) {
                System.out.print(task.wbs)
                System.out.print("\t")
                System.out.print(task.name)
                System.out.print("\t")
                System.out.print(format(task.start))
                System.out.print("\t")
                System.out.print(format(task.actualStart))
                System.out.print("\t")
                System.out.print(format(task.finish))
                System.out.print("\t")
                System.out.print(format(task.actualFinish))
                System.out.println()
            }
        }
    }

    /**
     * Format a date for ease of comparison.
     *
     * @param date raw date
     * @return formatted date
     */
    private fun format(date: Date?): String {
        return if (date == null) "" else m_df.format(date)
    }

    companion object {

        /**
         * Command line entry point.
         *
         * @param args command line arguments
         */
        fun main(args: Array<String>) {
            try {
                if (args.size != 1) {
                    System.out.println("Usage: TaskDateDump <input file name>")
                } else {
                    val tdd = TaskDateDump()
                    tdd.process(args[0])
                }
            } catch (ex: Exception) {
                ex.printStackTrace(System.out)
            }

        }
    }
}
