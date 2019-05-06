/*
 * file:       BlockReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       2018-10-11
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
import java.util.ArrayList
import java.util.LinkedHashMap

/**
 * Read a collection of fixed size blocks.
 */
internal abstract class BlockReader
/**
 * Constructor.
 *
 * @param stream input stream
 */
(protected val m_stream: StreamReader) {

    /**
     * Read a list of fixed sized blocks from the input stream.
     *
     * @return List of MapRow instances representing the fixed size blocks
     */
    @Throws(IOException::class)
    fun read(): List<MapRow> {
        val result = ArrayList<MapRow>()
        val fileCount = m_stream.readInt()
        if (fileCount != 0) {
            for (index in 0 until fileCount) {
                // We use a LinkedHashMap to preserve insertion order in iteration
                // Useful when debugging the file format.
                val map = LinkedHashMap<String, Object>()
                readBlock(map)
                result.add(MapRow(map))
            }
        }
        return result
    }

    /**
     * Implemented by child classes to determine how the fixed size blocks are
     * read and interpreted.
     *
     * @param map Map to receive attributes read from the block
     */
    @Throws(IOException::class)
    protected abstract fun readBlock(map: Map<String, Object>)
}
