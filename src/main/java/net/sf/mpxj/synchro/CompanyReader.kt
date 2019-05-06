/*
 * file:       CompanyReader.java
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
 * Read the companies table.
 */
internal class CompanyReader
/**
 * Constructor.
 *
 * @param stream input stream
 */
(stream: StreamReader) : TableReader(stream) {

    @Override
    @Throws(IOException::class)
    protected override fun readRow(stream: StreamReader, map: Map<String, Object>) {
        map.put("UNKNOWN1", stream.readBytes(20))
        map.put("RESOURCES", stream.readTable(ResourceReader::class.java))
        map.put("NAME", stream.readString())
        map.put("ADDRESS", stream.readString())
        map.put("PHONE", stream.readString())
        map.put("FAX", stream.readString())
        map.put("EMAIL", stream.readString())
        map.put("UNKNOWN2", stream.readBytes(12))
        map.put("URL", stream.readString())
        map.put("UNKNOWN3", stream.readBytes(8))
    }

    @Override
    override fun rowMagicNumber(): Int {
        return 0x0598BFDA
    }
}
