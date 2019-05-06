/*
 * file:       SureTrakSTXFileReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       11/03/2018
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

package net.sf.mpxj.primavera.suretrak

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.LinkedList

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.common.FileHelper
import net.sf.mpxj.common.FixedLengthInputStream
import net.sf.mpxj.common.StreamHelper
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.primavera.common.Blast
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * Reads a schedule data from a SureTrak STX file.
 */
class SureTrakSTXFileReader : AbstractProjectReader() {

    private var m_projectListeners: List<ProjectListener>? = null
    @Override
    override fun addProjectListener(listener: ProjectListener) {
        if (m_projectListeners == null) {
            m_projectListeners = LinkedList<ProjectListener>()
        }
        m_projectListeners!!.add(listener)
    }

    @Override
    @Throws(MPXJException::class)
    override fun read(stream: InputStream): ProjectFile? {
        var tempDir: File? = null

        try {
            StreamHelper.skip(stream, (32768 + 4).toLong())
            tempDir = FileHelper.createTempDir()

            while (stream.available() > 0) {
                extractFile(stream, tempDir)
            }

            return SureTrakDatabaseReader.setProjectNameAndRead(tempDir)
        } catch (ex: IOException) {
            throw MPXJException("Failed to parse file", ex)
        } finally {
            FileHelper.deleteQuietly(tempDir)
        }
    }

    /**
     * Extracts the data for a single file from the input stream and writes
     * it to a target directory.
     *
     * @param stream input stream
     * @param dir target directory
     */
    @Throws(IOException::class)
    private fun extractFile(stream: InputStream, dir: File?) {
        val dataSize = ByteArray(4)
        val header = ByteArray(4)
        val fileName = ByteArray(260)

        stream.read(dataSize)
        stream.read(header)
        stream.read(fileName)

        val dataSizeValue = getInt(dataSize, 0)
        val fileNameValue = getString(fileName, 0)

        val file = File(dir, fileNameValue)
        if (dataSizeValue == 0) {
            FileHelper.createNewFile(file)
        } else {
            val os = FileOutputStream(file)
            val inputStream = FixedLengthInputStream(stream, dataSizeValue.toLong())
            val blast = Blast()
            blast.blast(inputStream, os)
            os.close()
        }
    }

    /**
     * Retrieve a four byte integer.
     *
     * @param data byte array
     * @param offset offset into array
     * @return int value
     */
    fun getInt(data: ByteArray, offset: Int): Int {
        var result = 0
        var i = offset
        var shiftBy = 0
        while (shiftBy < 32) {
            result = result or (data[i] and 0xff shl shiftBy)
            ++i
            shiftBy += 8
        }
        return result
    }

    /**
     * Retrieve a string from the byte array.
     *
     * @param data byte array
     * @param offset offset into byte array
     * @return String instance
     */
    fun getString(data: ByteArray, offset: Int): String {
        val buffer = StringBuilder()
        var c: Char

        var loop = 0
        while (offset + loop < data.size) {
            c = data[offset + loop].toChar()

            if (c.toInt() == 0) {
                break
            }

            buffer.append(c)
            loop++
        }

        return buffer.toString()
    }
}
