/*
 * file:       AstaFileReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2012
 * date:       23/04/2012
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

package net.sf.mpxj.asta

import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.util.LinkedList

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.AbstractProjectReader
import net.sf.mpxj.reader.ProjectReader

/**
 * This class provides a generic front end to read project data from
 * an Asta PP file. Determines if the file is a text file or a SQLite database
 * and takes the appropriate action.
 */
class AstaFileReader : AbstractProjectReader() {

    private var m_projectListeners: List<ProjectListener>? = null
    /**
     * {@inheritDoc}
     */
    @Override
    override fun addProjectListener(listener: ProjectListener) {
        if (m_projectListeners == null) {
            m_projectListeners = LinkedList<ProjectListener>()
        }
        m_projectListeners!!.add(listener)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(inputStream: InputStream): ProjectFile {
        try {
            val `is` = BufferedInputStream(inputStream)
            `is`.mark(100)
            val buffer = ByteArray(SQLITE_TEXT.length())
            `is`.read(buffer)
            `is`.reset()
            val actualText = String(buffer)
            val result: ProjectFile
            if (SQLITE_TEXT.equals(actualText)) {
                result = readDatabaseFile(`is`)
            } else {
                result = readTextFile(`is`)
            }
            return result
        } catch (ex: IOException) {
            throw MPXJException("Failed to read file", ex)
        }

    }

    /**
     * Adds any listeners attached to this reader to the reader created internally.
     *
     * @param reader internal project reader
     */
    private fun addListeners(reader: ProjectReader) {
        if (m_projectListeners != null) {
            for (listener in m_projectListeners!!) {
                reader.addProjectListener(listener)
            }
        }
    }

    /**
     * Process a text-based PP file.
     *
     * @param inputStream file input stream
     * @return ProjectFile instance
     */
    @Throws(MPXJException::class)
    private fun readTextFile(inputStream: InputStream): ProjectFile {
        val reader = AstaTextFileReader()
        addListeners(reader)
        return reader.read(inputStream)
    }

    /**
     * Process a SQLite database PP file.
     *
     * @param inputStream file input stream
     * @return ProjectFile instance
     */
    @Throws(MPXJException::class)
    private fun readDatabaseFile(inputStream: InputStream): ProjectFile {
        val reader = AstaDatabaseFileReader()
        addListeners(reader)
        return reader.read(inputStream)
    }

    companion object {

        private val SQLITE_TEXT = "SQLite format"
    }
}