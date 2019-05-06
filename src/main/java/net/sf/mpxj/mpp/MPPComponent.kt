/*
 * file:       MPPComponent.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       03/01/2003
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

import java.io.EOFException
import java.io.IOException
import java.io.InputStream

/**
 * This class provides common functionality used by each of the classes
 * that read the different sections of the MPP file.
 */
internal open class MPPComponent
/**
 * Constructor.
 */
protected constructor()// protected constructor to prevent instantiation
{

    /**
     * This method reads a single byte from the input stream.
     *
     * @param is the input stream
     * @return byte value
     * @throws IOException on file read error or EOF
     */
    @Throws(IOException::class)
    protected fun readByte(`is`: InputStream): Int {
        val data = ByteArray(1)
        if (`is`.read(data) !== data.size) {
            throw EOFException()
        }

        return MPPUtility.getByte(data, 0)
    }

    /**
     * This method reads a two byte integer from the input stream.
     *
     * @param is the input stream
     * @return integer value
     * @throws IOException on file read error or EOF
     */
    @Throws(IOException::class)
    protected fun readShort(`is`: InputStream): Int {
        val data = ByteArray(2)
        if (`is`.read(data) !== data.size) {
            throw EOFException()
        }

        return MPPUtility.getShort(data, 0)
    }

    /**
     * This method reads a four byte integer from the input stream.
     *
     * @param is the input stream
     * @return byte value
     * @throws IOException on file read error or EOF
     */
    @Throws(IOException::class)
    protected fun readInt(`is`: InputStream): Int {
        val data = ByteArray(4)
        if (`is`.read(data) !== data.size) {
            throw EOFException()
        }

        return MPPUtility.getInt(data, 0)
    }

    /**
     * This method reads a byte array from the input stream.
     *
     * @param is the input stream
     * @param size number of bytes to read
     * @return byte array
     * @throws IOException on file read error or EOF
     */
    @Throws(IOException::class)
    protected fun readByteArray(`is`: InputStream, size: Int): ByteArray {
        val buffer = ByteArray(size)
        if (`is`.read(buffer) !== buffer.size) {
            throw EOFException()
        }
        return buffer
    }
}
