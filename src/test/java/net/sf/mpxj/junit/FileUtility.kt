/*
 * file:       FileUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       07-Mar-2006
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

package net.sf.mpxj.junit

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Utility methods for handling files.
 */
object FileUtility {
    /**
     * Utility method to ensure that two files contain identical data.
     *
     * @param file1 File object
     * @param file2 File object
     * @return boolean flag
     */
    @Throws(Exception::class)
    fun equals(file1: File, file2: File): Boolean {
        var result: Boolean

        result = true

        val input1 = BufferedInputStream(FileInputStream(file1))
        val input2 = BufferedInputStream(FileInputStream(file2))
        var c1: Int
        var c2: Int

        while (true) {
            // Ignore line endings: dropping all \r character should ensure that
            // both files just have \n line endings.
            do {
                c1 = input1.read()
            } while (c1 == '\r'.toInt())

            do {
                c2 = input2.read()
            } while (c2 == '\r'.toInt())

            if (c1 != c2) {
                result = false
                break
            }

            if (c1 == -1) {
                break
            }
        }

        input1.close()
        input2.close()

        return result
    }

}
