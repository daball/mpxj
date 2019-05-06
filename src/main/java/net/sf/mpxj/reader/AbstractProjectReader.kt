/*
 * file:       AbstractProjectReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Dec 21, 2005
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

package net.sf.mpxj.reader

import java.io.File
import java.io.FileInputStream
import java.io.IOException

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.common.StreamHelper

/**
 * Abstract implementation of the ProjectReader interface
 * which supplies implementations of the trivial read methods.
 */
abstract class AbstractProjectReader : ProjectReader {
    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(fileName: String): ProjectFile {
        var fis: FileInputStream? = null

        try {
            fis = FileInputStream(fileName)
            val projectFile = read(fis)
            fis!!.close()
            fis = null
            return projectFile
        } catch (ex: IOException) {
            throw MPXJException(MPXJException.READ_ERROR, ex)
        } finally {
            StreamHelper.closeQuietly(fis)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(file: File): ProjectFile {
        var fis: FileInputStream? = null

        try {
            fis = FileInputStream(file)
            val projectFile = read(fis)
            fis!!.close()
            return projectFile
        } catch (ex: IOException) {
            throw MPXJException(MPXJException.READ_ERROR, ex)
        } finally {
            StreamHelper.closeQuietly(fis)
        }
    }
}
