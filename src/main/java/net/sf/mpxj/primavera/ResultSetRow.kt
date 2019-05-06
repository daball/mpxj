/*
 * file:       ResultSetRow.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       22/03/2010
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

package net.sf.mpxj.primavera

import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Types
import java.util.Date
import java.util.HashMap
import kotlin.collections.Map.Entry

import net.sf.mpxj.common.NumberHelper

/**
 * Implementation of the Row interface, wrapping a Map.
 */
internal class ResultSetRow
/**
 * Constructor.
 *
 * @param rs
 * result set from which data is drawn
 * @param meta
 * result set meta data
 */
@Throws(SQLException::class)
constructor(rs: ResultSet, meta: Map<String, Integer>) : MapRow(HashMap<String, Object>()) {
    init {

        for (entry in meta.entrySet()) {
            val name = entry.getKey()
            val type = entry.getValue().intValue()
            var value: Object?

            when (type) {
                Types.BIT, Types.BOOLEAN -> {
                    value = Boolean.valueOf(rs.getBoolean(name))
                }

                Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.CLOB, Types.NVARCHAR, Types.LONGNVARCHAR -> {
                    value = rs.getString(name)
                }

                Types.DATE -> {
                    value = rs.getDate(name)
                }

                Types.TIMESTAMP, SQL_SERVER_TIMESTAMP -> {
                    val ts = rs.getTimestamp(name)
                    if (ts != null) {
                        value = Date(ts!!.getTime())
                    } else {
                        value = null
                    }
                }

                Types.REAL, Types.DECIMAL, Types.DOUBLE, Types.NUMERIC -> {
                    value = NumberHelper.getDouble(rs.getDouble(name))
                }

                Types.INTEGER, Types.SMALLINT -> {
                    value = Integer.valueOf(rs.getInt(name))
                }

                Types.BIGINT -> {
                    value = Long.valueOf(rs.getLong(name))
                }

                Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> {
                    value = rs.getBytes(name)
                }

                Types.OTHER -> {
                    value = rs.getObject(name)
                }

                else -> {
                    throw IllegalArgumentException("Unsupported SQL type: $type for column $name")
                }
            }

            if (rs.wasNull()) {
                value = null
            }

            m_map.put(name, value)
        }
    }

    companion object {

        // https://stackoverflow.com/questions/45377247/microsoft-sql-jdbc-driver-v6-2-returning-incorrect-sql-type-code-for-datetime-fi
        private val SQL_SERVER_TIMESTAMP = -151
    }
}
