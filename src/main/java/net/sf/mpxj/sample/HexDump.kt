/*
 * file:       HexDump.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       10/03/2018
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

import java.io.FileInputStream
import java.io.FileWriter
import java.io.PrintWriter

import net.sf.mpxj.common.ByteArrayHelper

/**
 * This is a trivial class used to dump the contents of a file
 * as hex digits, and their ASCII equivalents.
 */
object HexDump {
    /**
     * Main method.
     *
     * @param args array of command line arguments
     */
    fun main(args: Array<String>) {
        try {
            if (args.size != 2) {
                System.out.println("Usage: HexDump <input file name> <output text file name>")
            } else {
                System.out.println("Dump started.")
                val start = System.currentTimeMillis()
                process(args[0], args[1])
                val elapsed = System.currentTimeMillis() - start
                System.out.println("Dump completed in " + elapsed + "ms")
            }
        } catch (ex: Exception) {
            System.out.println("Caught " + ex.toString())
        }

    }

    /**
     * This method opens the input and output files and kicks
     * off the processing.
     *
     * @param input Name of the input file
     * @param output Name of the output file
     * @throws Exception Thrown on file read errors
     */
    @Throws(Exception::class)
    private fun process(input: String, output: String) {
        val `is` = FileInputStream(input)
        val pw = PrintWriter(FileWriter(output))

        val buffer = ByteArray(`is`.available())
        `is`.read(buffer)
        pw.println(ByteArrayHelper.hexdump(buffer, 0, buffer.size, true, 16, ""))

        `is`.close()
        pw.flush()
        pw.close()
    }
}
