/*
 * file:       BytesReadInputStream.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2019
 * date:       2019-01-28
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

package net.sf.mpxj.synchro

import java.io.IOException
import java.io.InputStream

/**
 * Input stream wrapper which counts the number of bytes read.
 */
internal class BytesReadInputStream
/**
 * Constructor.
 *
 * @param stream wrapped input stream
 */
(private val m_stream: InputStream) : InputStream() {
    /**
     * Retrieve the number of bytes read.
     *
     * @return number of bytes read.
     */
    var bytesRead: Int = 0
        private set

    @Override
    @Throws(IOException::class)
    fun read(): Int {
        ++bytesRead
        return m_stream.read()
    }
}
