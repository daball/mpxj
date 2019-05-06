/*
 * file:       MpxjConvert.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2005
 * date:       07/12/2005
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

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.UniversalProjectReader
import net.sf.mpxj.writer.ProjectWriter
import net.sf.mpxj.writer.ProjectWriterUtility

/**
 * This is a general utility designed to convert from one project file format
 * to another. It will typically be used as a command line utility. The
 * user passes in two arguments, the input file name and the output file name.
 * The type of each file is determined by its extension.
 */
class MpxjConvert {

    /**
     * Convert one project file format to another.
     *
     * @param inputFile input file
     * @param outputFile output file
     * @throws Exception
     */
    @Throws(Exception::class)
    fun process(inputFile: String, outputFile: String) {
        System.out.println("Reading input file started.")
        var start = System.currentTimeMillis()
        val projectFile = readFile(inputFile)
        var elapsed = System.currentTimeMillis() - start
        System.out.println("Reading input file completed in " + elapsed + "ms.")

        System.out.println("Writing output file started.")
        start = System.currentTimeMillis()
        val writer = ProjectWriterUtility.getProjectWriter(outputFile)
        writer.write(projectFile, outputFile)
        elapsed = System.currentTimeMillis() - start
        System.out.println("Writing output completed in " + elapsed + "ms.")
    }

    /**
     * Use the universal project reader to open the file.
     * Throw an exception if we can't determine the file type.
     *
     * @param inputFile file name
     * @return ProjectFile instance
     */
    @Throws(MPXJException::class)
    private fun readFile(inputFile: String): ProjectFile {
        val reader = UniversalProjectReader()
        return reader.read(inputFile) ?: throw IllegalArgumentException("Unsupported file type")
    }

    companion object {
        /**
         * Main method.
         *
         * @param args array of command line arguments
         */
        fun main(args: Array<String>) {
            try {
                if (args.size != 2) {
                    System.out.println("Usage: MpxjConvert <input file name> <output file name>")
                } else {
                    val convert = MpxjConvert()
                    convert.process(args[0], args[1])
                }

                System.exit(0)
            } catch (ex: Exception) {
                System.out.println()
                System.out.print("Conversion Error: ")
                ex.printStackTrace(System.out)
                System.out.println()
                System.exit(1)
            }

        }
    }
}
