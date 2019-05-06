/*
 * file:       ResourceReader.java
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

/**
 * Reads a resource table.
 */
internal class ResourceReader
/**
 * Constructor.
 *
 * @param stream input stream
 */
(stream: StreamReader) : TableReader(stream) {

    @Override
    @Throws(IOException::class)
    protected override fun readRow(stream: StreamReader, map: Map<String, Object>) {
        val unknown3BlockSize = if (m_stream.majorVersion > 5) 56 else 64

        map.put("NAME", stream.readString())
        map.put("DESCRIPTION", stream.readString())
        val supplyReferenceFlag = stream.readInteger()
        map.put("SUPPLY_REFERENCE_FLAG", supplyReferenceFlag)
        if (supplyReferenceFlag.intValue() !== 0) {
            map.put("SUPPLY_REFERENCE", stream.readString())
        }
        map.put("UNKNOWN1", stream.readBytes(48))
        map.put("RESOURCES", stream.readTable(ResourceReader::class.java))
        map.put("UNKNOWN2", stream.readBytes(20))
        map.put("URL", stream.readString())
        map.put("USER_FIELDS", stream.readTableConditional(UserFieldReader::class.java))
        map.put("ID", stream.readString())
        map.put("EMAIL", stream.readString())
        // NOTE: this contains nested tables
        map.put("UNKNOWN3", stream.readUnknownTable(unknown3BlockSize, 0x701BAFBD))
        map.put("UNKNOWN4", stream.readBytes(30))
        map.put("COMMENTARY", stream.readTableConditional(CommentaryReader::class.java))
        map.put("UNKNOWN5", stream.readBytes(48))
        val unknown6Flag = stream.readInteger()
        map.put("UNKNOWN6_FLAG", unknown6Flag)
        if (unknown6Flag.intValue() !== 0) {
            map.put("UNKNOWN6", stream.readBytes(70))
            map.put("UNKNOWN7", stream.readString())
            map.put("UNKNOWN8", stream.readBytes(4))
        }
        map.put("UNKNOWN9", stream.readBytes(12))

        if (m_stream.majorVersion > 5) {
            map.put("UNKNOWN10", stream.readBytes(12))
        }
        map.put("UNIQUE_ID", stream.readInteger())
    }

    @Override
    override fun rowMagicNumber(): Int {
        return 0x57A85C31
    }
}
