/*
 * file:       EncryptedDocumentInputStream.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2007
 * date:       20/10/2007
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

package net.sf.mpxj.mpp

import java.io.IOException
import java.io.InputStream

import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream

/**
 * This class wraps the POI [DocumentInputStream] class
 * to allow data to be decrypted before passing
 * it back to the caller.
 */
internal class EncryptedDocumentInputStream
/**
 * Constructor.
 *
 * @param entry file entry
 * @param mask the mask used to decrypt the stream.
 * @throws IOException
 */
@Throws(IOException::class)
constructor(entry: DocumentEntry, private val m_mask: Int) : InputStream() {

    private val m_dis: DocumentInputStream

    init {
        m_dis = DocumentInputStream(entry)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(IOException::class)
    fun read(): Int {
        var value = m_dis.read()
        value = value xor m_mask
        return value
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(IOException::class)
    fun read(b: ByteArray, off: Int, len: Int): Int {
        val result = m_dis.read(b, off, len)
        for (loop in 0 until len) {
            b[loop + off] = b[loop + off] xor m_mask.toByte()
        }
        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun available(): Int {
        return m_dis.available()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun close() {
        m_dis.close()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Synchronized
    fun mark(readlimit: Int) {
        m_dis.mark(readlimit)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun markSupported(): Boolean {
        return m_dis.markSupported()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Synchronized
    fun reset() {
        m_dis.reset()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(IOException::class)
    fun skip(n: Long): Long {
        return m_dis.skip(n)
    }
}
