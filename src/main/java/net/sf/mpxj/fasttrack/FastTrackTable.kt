/*
 * file:       FastTrackTable.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       14/03/2017
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

package net.sf.mpxj.fasttrack

import java.util.ArrayList
import java.util.HashMap

import net.sf.mpxj.TimeUnit

/**
 * Represents a table of data from an FTS file, made up of a set of MapRow instances.
 */
internal class FastTrackTable
/**
 * Constructor.
 * @param type table type
 * @param data raw data read from the FTS file
 */
(
        /**
         * Retrieve the type of this table.
         *
         * @return table type
         */
        val type: FastTrackTableType, private val m_data: FastTrackData) : Iterable<MapRow> {

    /**
     * Retrieve the duration time units used in this table.
     *
     * @return duration time units
     */
    val durationTimeUnit: TimeUnit
        get() = m_data.durationTimeUnit

    /**
     * Retrieve the work time units used in this table.
     *
     * @return work time units
     */
    val workTimeUnit: TimeUnit
        get() = m_data.workTimeUnit
    private val m_rows = ArrayList<MapRow>()

    /**
     * Add data for a column to this table.
     *
     * @param column column data
     */
    fun addColumn(column: FastTrackColumn) {
        val type = column.type
        val data = column.data
        for (index in data.indices) {
            val row = getRow(index)
            row.map.put(type, data[index])
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun iterator(): Iterator<MapRow> {
        return m_rows.iterator()
    }

    /**
     * Retrieve a specific row by index number, creating a blank row if this row does not exist.
     *
     * @param index index number
     * @return MapRow instance
     */
    private fun getRow(index: Int): MapRow {
        val result: MapRow

        if (index == m_rows.size()) {
            result = MapRow(this, HashMap<FastTrackField, Object>())
            m_rows.add(result)
        } else {
            result = m_rows.get(index)
        }

        return result
    }
}
