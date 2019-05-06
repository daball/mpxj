/*
 * file:       MpxjTestData.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       20/08/2014
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

import org.junit.Assert.*

import java.io.File
import java.io.FileFilter
import java.io.IOException

/**
 * Simple utility class to provide access to named test data files.
 */
object MpxjTestData {
    private val DATA_DIR: String

    init {
        var dataDirValue: String? = null
        val dataDir = File("junit/data")
        if (dataDir.exists() && dataDir.isDirectory()) {
            try {
                dataDirValue = dataDir.getCanonicalPath()
            } catch (ex: IOException) {
                // Ignore this
            }

        } else {
            dataDirValue = System.getProperty("mpxj.junit.datadir")
        }

        if (dataDirValue == null || dataDirValue.length() === 0) {
            fail("missing datadir property")
        }

        DATA_DIR = dataDirValue
    }

    /**
     * Retrieve the path to a test data file.
     *
     * @param fileName test data file name
     * @return file path
     */
    fun filePath(fileName: String): String {
        return "$DATA_DIR/$fileName"
    }

    /**
     * Helper method used to retrieve a list of test files.
     *
     * @param path path to test files
     * @param name file name prefix
     * @return list of files
     */
    fun listFiles(path: String, name: String): Array<File> {
        val testDataDir = File(filePath(path))
        var result = testDataDir.listFiles(object : FileFilter() {
            @Override
            fun accept(pathname: File): Boolean {
                return pathname.getName().startsWith(name)
            }
        })
        if (result == null) {
            result = arrayOfNulls<File>(0)
        }
        return result
    }

}
