/*
 * file:       PredecessorReader.java
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

import net.sf.mpxj.Duration
import net.sf.mpxj.RelationType

/**
 * Reads a predecessor table.
 */
internal class PredecessorReader
/**
 * Constructor.
 *
 * @param stream input stream
 */
(stream: StreamReader) : TableReader(stream) {

    @Override
    @Throws(IOException::class)
    protected override fun readRow(stream: StreamReader, map: Map<String, Object>) {
        map.put("PREDECESSOR_UUID", stream.readUUID())
        map.put("RELATION_TYPE", getRelationType(stream.readInt()))
        map.put("UNKNOWN1", stream.readBytes(4))
        var lag = stream.readDuration()
        map.put("UNKNOWN2", stream.readBytes(4))
        val lagIsNegative = stream.readInt() == 2
        map.put("CALENDAR_UUID", stream.readUUID())
        map.put("UNKNOWN3", stream.readBytes(8))

        if (lagIsNegative) {
            lag = Duration.getInstance(-lag.getDuration(), lag.getUnits())
        }

        map.put("LAG", lag)
    }

    @Override
    override fun rowMagicNumber(): Int {
        return 0x04E7E3D1
    }

    /**
     * Convert an integer to a RelationType instance.
     *
     * @param type integer value
     * @return RelationType instance
     */
    private fun getRelationType(type: Int): RelationType {
        val result: RelationType
        if (type > 0 && type < RELATION_TYPES.size) {
            result = RELATION_TYPES[type]
        } else {
            result = RelationType.FINISH_START
        }
        return result
    }

    companion object {

        private val RELATION_TYPES = arrayOf<RelationType>(null, RelationType.FINISH_START, RelationType.START_FINISH, RelationType.START_START, RelationType.FINISH_FINISH)
    }
}
