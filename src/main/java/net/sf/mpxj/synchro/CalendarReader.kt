/*
 * file:       CalendarReader.java
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
import java.util.UUID

/**
 * Reads a calendar table.
 */
internal class CalendarReader
/**
 * Constructor.
 *
 * @param stream input stream
 */
(stream: StreamReader) : TableReader(stream) {

    /**
     * Retrieve the default calendar UUID.
     *
     * @return Default calendar UUID
     */
    var defaultCalendarUUID: UUID? = null
        private set

    @Override
    @Throws(IOException::class)
    protected override fun readRow(stream: StreamReader, map: Map<String, Object>) {
        map.put("NAME", stream.readString())
        map.put("UNKNOWN1", stream.readTable(UnknownTableReader::class.java))
        map.put("UNKNOWN2", stream.readBytes(4))
        map.put("SUNDAY_DAY_TYPE", stream.readUUID())
        map.put("MONDAY_DAY_TYPE", stream.readUUID())
        map.put("TUESDAY_DAY_TYPE", stream.readUUID())
        map.put("WEDNESDAY_DAY_TYPE", stream.readUUID())
        map.put("THURSDAY_DAY_TYPE", stream.readUUID())
        map.put("FRIDAY_DAY_TYPE", stream.readUUID())
        map.put("SATURDAY_DAY_TYPE", stream.readUUID())
        map.put("UNKNOWN3", stream.readBytes(4))
        map.put("DAY_TYPE_ASSIGNMENTS", stream.readTable(DayTypeAssignmentReader::class.java))
        map.put("DAY_TYPES", stream.readTable(DayTypeReader::class.java))
        map.put("UNKNOWN4", stream.readBytes(8))
    }

    @Override
    @Throws(IOException::class)
    override fun postTrailer(stream: StreamReader) {
        defaultCalendarUUID = stream.readUUID()
    }

    @Override
    override fun rowMagicNumber(): Int {
        return 0x7FEC261D
    }
}
