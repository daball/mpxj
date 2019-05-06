/*
 * file:       DocumentInputStreamFactory.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2015
 * date:       21/03/2015
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

import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream

/**
 * Factory used to create document input streams. Can switch between normal and encrypted streams as required.
 */
internal class DocumentInputStreamFactory
/**
 * Constructor.
 *
 * @param props document properties
 */
(props: Props) {

    private val m_encrypted: Boolean
    /**
     * Retrieve the encryption code.
     *
     * @return encryption code.
     */
    val encryptionCode: Byte

    init {
        m_encrypted = props.getByte(Props.PASSWORD_FLAG).toInt() != 0
        val code = props.getByte(Props.ENCRYPTION_CODE)
        encryptionCode = (if (code.toInt() == 0x00) 0x00 else 0xFF - code).toByte()
    }

    /**
     * Method used to instantiate the appropriate input stream reader,
     * a standard one, or one which can deal with "encrypted" data.
     *
     * @param directory directory entry
     * @param name file name
     * @return new input stream
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getInstance(directory: DirectoryEntry, name: String): InputStream {
        val entry = directory.getEntry(name) as DocumentEntry
        val stream: InputStream
        if (m_encrypted) {
            stream = EncryptedDocumentInputStream(entry, encryptionCode.toInt())
        } else {
            stream = DocumentInputStream(entry)
        }

        return stream
    }
}
