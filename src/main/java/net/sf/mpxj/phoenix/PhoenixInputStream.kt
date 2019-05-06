/*
 * file:       PhoenixInputStream.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2015
 * date:       28 November 2015
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

package net.sf.mpxj.phoenix

import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.HashMap
import java.util.zip.InflaterInputStream

import net.sf.mpxj.common.CharsetHelper

/**
 * Input stream used to handle compressed Phoenix files.
 */
class PhoenixInputStream
/**
 * Constructor.
 *
 * @param stream input stream we're wrapping
 */
@Throws(IOException::class)
constructor(stream: InputStream) : InputStream() {

    /**
     * Retrieve the file format version from the Phoenix header.
     *
     * @return file format version
     */
    val version: String
        get() = m_properties.get("VERSION")

    /**
     * Read the compression flag from the Phoenix file header.
     *
     * @return true if the file is compressed
     */
    val isCompressed: Boolean
        get() {
            val result = m_properties.get("COMPRESSION")
            return result != null && result!!.equals("yes")
        }

    private val m_stream: InputStream
    private val m_properties = HashMap<String, String>()

    init {
        m_stream = prepareInputStream(stream)
        //Files.copy(m_stream, new File("c:/temp/project1.ppx").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(IOException::class)
    fun read(): Int {
        return m_stream.read()
    }

    /**
     * If the file is compressed, handle this so that the stream is ready to read.
     *
     * @param stream input stream
     * @return uncompressed input stream
     */
    @Throws(IOException::class)
    private fun prepareInputStream(stream: InputStream): InputStream {
        val result: InputStream
        val bis = BufferedInputStream(stream)
        readHeaderProperties(bis)
        if (isCompressed) {
            result = InflaterInputStream(bis)
        } else {
            result = bis
        }
        return result
    }

    /**
     * Read the header from the Phoenix file.
     *
     * @param stream input stream
     * @return raw header data
     */
    @Throws(IOException::class)
    private fun readHeaderString(stream: BufferedInputStream): String {
        val bufferSize = 100
        stream.mark(bufferSize)
        val buffer = ByteArray(bufferSize)
        stream.read(buffer)
        val charset = CharsetHelper.UTF8
        val header = String(buffer, charset)
        val prefixIndex = header.indexOf("PPX!!!!|")
        val suffixIndex = header.indexOf("|!!!!XPP")

        if (prefixIndex != 0 || suffixIndex == -1) {
            throw IOException("File format not recognised")
        }

        val skip = suffixIndex + 9
        stream.reset()
        stream.skip(skip)

        return header.substring(prefixIndex + 8, suffixIndex)
    }

    /**
     * Read properties from the raw header data.
     *
     * @param stream input stream
     */
    @Throws(IOException::class)
    private fun readHeaderProperties(stream: BufferedInputStream) {
        val header = readHeaderString(stream)
        for (property in header.split("\\|")) {
            val expression = property.split("=")
            m_properties.put(expression[0], expression[1])
        }
    }
}
