/**
 * Java Web Archive Toolkit - Software to read and validate ARC, WARC
 * and GZip files. (http://jwat.org/)
 * Copyright 2011-2012 Netarkivet.dk (http://netarkivet.dk/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.mpxj.common

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

/**
 * `InputStream` with a fixed amount of bytes available to read.
 * When the stream is closed the remaining bytes that have not been read are
 * read or skipped.
 *
 * @author lbihanic, selghissassi, nicl
 */
class FixedLengthInputStream
/**
 * Create a new input stream with a fixed number of bytes available from
 * the underlying stream.
 * @param in the input stream to wrap
 * @param length fixed number of bytes available through this stream
 */
(@SuppressWarnings("hiding") `in`: InputStream,
 /** Remaining bytes available.  */
 private var m_remaining: Long) : FilterInputStream(`in`) {

    /**
     * Closing will only skip to the end of this fixed length input stream and
     * not call the parent's close method.
     * @throws IOException if an I/O error occurs while closing stream
     */
    @Override
    @Throws(IOException::class)
    fun close() {
        var skippedLast: Long = 0
        if (m_remaining > 0) {
            skippedLast = skip(m_remaining)
            while (m_remaining > 0 && skippedLast > 0) {
                skippedLast = skip(m_remaining)
            }
        }
    }

    @Override
    fun available(): Int {
        return if (m_remaining > Integer.MAX_VALUE) Integer.MAX_VALUE else m_remaining.toInt()
    }

    @Override
    fun markSupported(): Boolean {
        return false
    }

    @Override
    @Synchronized
    fun mark(readlimit: Int) {
        // Not supported
    }

    @Override
    @Synchronized
    fun reset() {
        throw UnsupportedOperationException()
    }

    @Override
    @Throws(IOException::class)
    fun read(): Int {
        var b = -1
        if (m_remaining > 0) {
            b = `in`.read()
            if (b != -1) {
                --m_remaining
            }
        }
        return b
    }

    @Override
    @Throws(IOException::class)
    @JvmOverloads
    fun read(b: ByteArray, off: Int = 0, len: Int = b.size): Int {
        var bytesRead = -1
        if (m_remaining > 0) {
            bytesRead = `in`.read(b, off, Math.min(len, m_remaining) as Int)
            if (bytesRead > 0) {
                m_remaining -= bytesRead.toLong()
            }
        }
        return bytesRead
    }

    @Override
    @Throws(IOException::class)
    fun skip(n: Long): Long {
        var bytesSkipped: Long = 0
        if (m_remaining > 0) {
            bytesSkipped = `in`.skip(Math.min(n, m_remaining))
            m_remaining -= bytesSkipped
        }
        return bytesSkipped
    }
}