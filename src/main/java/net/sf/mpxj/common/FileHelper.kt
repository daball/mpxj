/*
 * file:       FileHelper
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       12/03/2018
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

package net.sf.mpxj.common

import java.io.File
import java.io.IOException

/**
 * Common helper methods for working with files.
 */
object FileHelper {
    /**
     * Delete a file and raise an exception if unsuccessful.
     *
     * @param file file to delete
     */
    @Throws(IOException::class)
    fun delete(file: File?) {
        if (file != null) {
            if (!file!!.delete()) {
                throw IOException("Failed to delete file")
            }
        }
    }

    /**
     * Delete a file ignoring failures.
     *
     * @param file file to delete
     */
    fun deleteQuietly(file: File?) {
        if (file != null) {
            if (file!!.isDirectory()) {
                val children = file!!.listFiles()
                if (children != null) {
                    for (child in children!!) {
                        deleteQuietly(child)
                    }
                }
            }
            file!!.delete()
        }
    }

    /**
     * Create a directory hierarchy, raise an exception in case of failure.
     *
     * @param file child file or directory
     */
    @Throws(IOException::class)
    fun mkdirs(file: File?) {
        if (file != null) {
            if (!file!!.mkdirs()) {
                throw IOException("Failed to create directories")
            }
        }
    }

    /**
     * Create a directory hierarchy, ignore failures.
     *
     * @param file child file or directory
     */
    fun mkdirsQuietly(file: File?) {
        if (file != null) {
            file!!.mkdirs()
        }
    }

    /**
     * Create a temporary directory.
     *
     * @return File instance representing temporary directory
     */
    @Throws(IOException::class)
    fun createTempDir(): File {
        val dir = File.createTempFile("mpxj", "tmp")
        delete(dir)
        mkdirs(dir)
        return dir
    }

    /**
     * Create a new file. Raise an exception if the file exists.
     *
     * @param file file to create
     */
    @Throws(IOException::class)
    fun createNewFile(file: File) {
        if (!file.createNewFile()) {
            throw IOException("Failed to create new file")
        }
    }
}
