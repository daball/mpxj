/*
 * file:       SqliteResultSetRow.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2016
 * date:       06/06/2016
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

import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Types
import java.text.ParseException
import java.util.Date
import java.util.HashMap
import kotlin.collections.Map.Entry

import net.sf.mpxj.Duration
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.NumberHelper

/**
 * Implementation of the Row interface, wrapping a Map.
 */
internal class SqliteResultSetRow
/**
 * Constructor.
 *
 * @param rs result set from which data is drawn
 * @param meta result set meta data
 */
@Throws(SQLException::class)
constructor(rs: ResultSet, meta: Map<String, Integer>) : MapRow(HashMap<String, Object>()) {
    init {

        for (entry in meta.entrySet()) {
            val name = entry.getKey().toUpperCase()
            val type = entry.getValue().intValue()
            var value: Object?

            when (type) {
                Types.BIT, Types.BOOLEAN -> {
                    value = Boolean.valueOf(rs.getBoolean(name))
                }

                Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.CLOB -> {
                    value = rs.getString(name)
                }

                Types.DATE -> {
                    val stringValue = rs.getString(name)
                    if (stringValue == null || stringValue!!.isEmpty()) {
                        value = null
                    } else {
                        value = rs.getDate(name)
                    }
                }

                Types.TIMESTAMP -> {
                    val ts = rs.getTimestamp(name)
                    if (ts != null) {
                        value = Date(ts!!.getTime())
                    } else {
                        value = null
                    }
                }

                Types.DOUBLE, Types.NUMERIC, Types.FLOAT -> {
                    value = NumberHelper.getDouble(rs.getDouble(name))
                }

                Types.INTEGER, Types.SMALLINT -> {
                    value = Integer.valueOf(rs.getInt(name))
                }

                Types.BIGINT -> {
                    value = Long.valueOf(rs.getLong(name))
                }

                Types.VARBINARY, Types.LONGVARBINARY -> {
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

            map.put(name, value)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getDuration(name: String): Duration {
        val value = getString(name)
        if (value == null || value.isEmpty()) {
            throw IllegalArgumentException("Unexpected duration value")
        }

        val items = value.split(",")
        if (items.size != 3) {
            throw IllegalArgumentException("Unexpected duration value: $value")
        }

        val item = DatatypeConverter.parseString(items[2])
        val durationValue: Number?

        try {
            durationValue = DatatypeConverter.parseDouble(item)
        } catch (ex: ParseException) {
            throw IllegalArgumentException("Unexpected duration value", ex)
        }

        return Duration.getInstance(NumberHelper.getDouble(durationValue), TimeUnit.HOURS)
    }

}
