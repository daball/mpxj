/*
 * file:       MpxjBatchConvert.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2017
 * date:       19/07/2017
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

import java.io.File
import java.io.FileFilter

/**
 * This is a general utility designed to multiple files in one directory
 * into a different file format.
 */
object MpxjBatchConvert {
    /**
     * Main method.
     *
     * @param args array of command line arguments
     */
    fun main(args: Array<String>) {
        try {
            if (args.size != 4) {
                System.out.println("Usage: MpxjBatchConvert <source directory> <source suffix> <target directory> <target suffix>")
            } else {
                val sourceDirectory = File(args[0])
                val sourceSuffix = args[1]
                val targetDirectory = args[2]
                val targetSuffix = args[3]

                val fileList = sourceDirectory.listFiles(object : FileFilter() {
                    @Override
                    fun accept(pathname: File): Boolean {
                        return pathname.getName().endsWith(sourceSuffix)
                    }
                })

                if (fileList != null) {
                    val convert = MpxjConvert()
                    for (file in fileList!!) {
                        val oldName = file.getName()
                        val newName = oldName.substring(0, oldName.length() - sourceSuffix.length()) + targetSuffix
                        val newFile = File(targetDirectory, newName)
                        convert.process(file.getCanonicalPath(), newFile.getCanonicalPath())
                    }
                }
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
