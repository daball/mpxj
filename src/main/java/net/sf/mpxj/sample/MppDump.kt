/*
 * file:       MppDump.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       07/02/2003
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
import java.io.InputStream
import java.io.PrintWriter

import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream
import org.apache.poi.poifs.filesystem.Entry
import org.apache.poi.poifs.filesystem.POIFSFileSystem

/**
 * This is a trivial class used to dump the contents of an MPP file
 * broken down into its internal file and directory structure, with the
 * content of each of the files written out as hex digits, and their
 * ASCII equivalents.
 */
object MppDump {

    /**
     * Buffer size for data output.
     */
    private val BUFFER_SIZE = 16

    /**
     * Data used for conversion to hex.
     */
    private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    /**
     * Main method.
     *
     * @param args array of command line arguments
     */
    fun main(args: Array<String>) {
        try {
            if (args.size != 2) {
                System.out.println("Usage: MppDump <input mpp file name> <output text file name>")
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

        val fs = POIFSFileSystem(`is`)
        dumpTree(pw, fs.getRoot(), "", true, true, null)

        `is`.close()
        pw.flush()
        pw.close()
        fs.close()
    }

    /**
     * This method recursively descends the directory structure, dumping
     * details of any files it finds to the output file.
     *
     * @param pw Output PrintWriter
     * @param dir DirectoryEntry to dump
     * @param prefix prefix used to identify path to this object
     * @param showData flag indicating if data is dumped, or just structure
     * @param hex set to true if hex output is required
     * @param indent indent used if displaying structure only
     * @throws Exception Thrown on file read errors
     */
    @Throws(Exception::class)
    private fun dumpTree(pw: PrintWriter, dir: DirectoryEntry, prefix: String, showData: Boolean, hex: Boolean, indent: String?) {
        var byteCount: Long

        val iter = dir.entries
        while (iter.hasNext()) {
            val entry = iter.next()
            if (entry is DirectoryEntry) {
                var childIndent = indent
                if (childIndent != null) {
                    childIndent += " "
                }

                val childPrefix = prefix + "[" + entry.getName() + "]."
                pw.println("start dir: " + prefix + entry.getName())
                dumpTree(pw, entry as DirectoryEntry, childPrefix, showData, hex, childIndent)
                pw.println("end dir: " + prefix + entry.getName())
            } else if (entry is DocumentEntry) {
                if (showData) {
                    pw.println("start doc: " + prefix + entry.getName())
                    if (hex == true) {
                        byteCount = hexdump(DocumentInputStream(entry as DocumentEntry), pw)
                    } else {
                        byteCount = asciidump(DocumentInputStream(entry as DocumentEntry), pw)
                    }
                    pw.println("end doc: " + prefix + entry.getName() + " (" + byteCount + " bytes read)")
                } else {
                    if (indent != null) {
                        pw.print(indent)
                    }
                    pw.println("doc: " + prefix + entry.getName())
                }
            } else {
                pw.println("found unknown: " + prefix + entry.getName())
            }
        }
    }

    /**
     * This method dumps the entire contents of a file to an output
     * print writer as hex and ASCII data.
     *
     * @param is Input Stream
     * @param pw Output PrintWriter
     * @return number of bytes read
     * @throws Exception Thrown on file read errors
     */
    @Throws(Exception::class)
    private fun hexdump(`is`: InputStream, pw: PrintWriter): Long {
        val buffer = ByteArray(BUFFER_SIZE)
        var byteCount: Long = 0

        var c: Char
        var loop: Int
        var count: Int
        val sb = StringBuilder()

        while (true) {
            count = `is`.read(buffer)
            if (count == -1) {
                break
            }

            byteCount += count.toLong()

            sb.setLength(0)

            loop = 0
            while (loop < count) {
                sb.append(" ")
                sb.append(HEX_DIGITS[buffer[loop] and 0xF0 shr 4])
                sb.append(HEX_DIGITS[buffer[loop] and 0x0F])
                loop++
            }

            while (loop < BUFFER_SIZE) {
                sb.append("   ")
                ++loop
            }

            sb.append("   ")

            loop = 0
            while (loop < count) {
                c = buffer[loop].toChar()
                if (c.toInt() > 200 || c.toInt() < 27) {
                    c = ' '
                }

                sb.append(c)
                loop++
            }

            pw.println(sb.toString())
        }

        return byteCount
    }

    /**
     * This method dumps the entire contents of a file to an output
     * print writer as ascii data.
     *
     * @param is Input Stream
     * @param pw Output PrintWriter
     * @return number of bytes read
     * @throws Exception Thrown on file read errors
     */
    @Throws(Exception::class)
    private fun asciidump(`is`: InputStream, pw: PrintWriter): Long {
        val buffer = ByteArray(BUFFER_SIZE)
        var byteCount: Long = 0

        var c: Char
        var loop: Int
        var count: Int
        val sb = StringBuilder()

        while (true) {
            count = `is`.read(buffer)
            if (count == -1) {
                break
            }

            byteCount += count.toLong()

            sb.setLength(0)
            loop = 0
            while (loop < count) {
                c = buffer[loop].toChar()
                if (c.toInt() > 200 || c.toInt() < 27) {
                    c = ' '
                }

                sb.append(c)
                loop++
            }

            pw.print(sb.toString())
        }

        return byteCount
    }
}
