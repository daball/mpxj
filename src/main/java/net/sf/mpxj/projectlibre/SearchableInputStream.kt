/*
 * file:       SearchableInputStream.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       24/04/2017
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

package net.sf.mpxj.projectlibre

import java.io.IOException
import java.io.InputStream

/**
 * Search through the input stream until the pattern is found, the acts as a normal input stream from that point.
 */
class SearchableInputStream
/**
 * Constructor.
 *
 * @param stream original input stream
 * @param pattern pattern to locate
 */
(private val m_stream: InputStream, pattern: String) : InputStream() {
    private val m_pattern: ByteArray
    private var m_searching = true
    /**
     * Returns true if the search failed.
     *
     * @return Boolean flag
     */
    var searchFailed: Boolean = false
        private set

    init {
        m_pattern = pattern.getBytes()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(IOException::class)
    fun read(): Int {
        var c: Int

        if (m_searching) {
            var index = 0
            c = -1
            while (m_searching) {
                c = m_stream.read()
                if (c == -1) {
                    searchFailed = true
                    throw IOException("Pattern not found")
                }

                if (c == m_pattern[index].toInt()) {
                    ++index
                    if (index == m_pattern.size) {
                        m_searching = false
                        c = m_stream.read()
                    }
                } else {
                    index = 0
                }
            }
        } else {
            c = m_stream.read()
        }

        return c
    }
}
