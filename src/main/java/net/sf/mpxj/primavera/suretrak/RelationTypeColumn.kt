/*
 * file:       RelationTypeColumn.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       01/03/2018
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

package net.sf.mpxj.primavera.suretrak

import net.sf.mpxj.RelationType
import net.sf.mpxj.primavera.common.AbstractShortColumn

/**
 * Extract column data from a table.
 */
internal class RelationTypeColumn
/**
 * Constructor.
 *
 * @param name column name
 * @param offset offset within data
 */
(name: String, offset: Int) : AbstractShortColumn(name, offset) {

    @Override
    override fun read(offset: Int, data: ByteArray): RelationType {
        val result = data[m_offset + offset].toInt()
        var type: RelationType? = null
        if (result >= 0 || result < TYPES.size) {
            type = TYPES[result]
        }
        if (type == null) {
            type = RelationType.FINISH_START
        }

        return type
    }

    companion object {

        private val TYPES = arrayOf(RelationType.FINISH_START, RelationType.START_START, RelationType.FINISH_FINISH, RelationType.START_FINISH)
    }
}
