/*
 * file:       TextFileRow.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2012
 * date:       29/04/2012
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

package net.sf.mpxj.asta

import java.sql.Types
import java.util.HashMap

import net.sf.mpxj.MPXJException

/**
 * Extends the MapRow class to allow it to manage data read from an Asta file.
 */
internal class TextFileRow
/**
 * Constructor.
 *
 * @param table table definition
 * @param data table data
 * @param epochDateFormat true if date is represented as an offset from an epoch
 * @throws MPXJException
 */
@Throws(MPXJException::class)
constructor(table: TableDefinition, data: List<String>, epochDateFormat: Boolean) : MapRow(HashMap<String, Object>()) {
    init {

        val columns = table.columns
        for (index in columns.indices) {
            val column = columns[index]
            if (index < data.size()) {
                if (column != null) {
                    map.put(column.name, getColumnValue(table.name, column.name, data[index], column.type, epochDateFormat))
                }
            }
        }
    }

    /**
     * Maps the text representation of column data to Java types.
     *
     * @param table table name
     * @param column column name
     * @param data text representation of column data
     * @param type column data type
     * @param epochDateFormat true if date is represented as an offset from an epoch
     * @return Java representation of column data
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun getColumnValue(table: String, column: String, data: String, type: Int, epochDateFormat: Boolean): Object? {
        try {
            var value: Object? = null

            when (type) {
                Types.BIT -> {
                    value = DatatypeConverter.parseBoolean(data)
                }

                Types.VARCHAR, Types.LONGVARCHAR -> {
                    value = DatatypeConverter.parseString(data)
                }

                Types.TIME -> {
                    value = DatatypeConverter.parseBasicTime(data)
                }

                Types.TIMESTAMP -> {
                    if (epochDateFormat) {
                        value = DatatypeConverter.parseEpochTimestamp(data)
                    } else {
                        value = DatatypeConverter.parseBasicTimestamp(data)
                    }
                }

                Types.DOUBLE -> {
                    value = DatatypeConverter.parseDouble(data)
                }

                Types.INTEGER -> {
                    value = DatatypeConverter.parseInteger(data)
                }

                else -> {
                    throw IllegalArgumentException("Unsupported SQL type: $type")
                }
            }

            return value
        } catch (ex: Exception) {
            throw MPXJException("Failed to parse $table.$column (data=$data, type=$type)", ex)
        }

    }
}
