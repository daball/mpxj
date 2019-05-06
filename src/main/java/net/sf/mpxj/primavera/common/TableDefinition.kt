/*
 * file:       TableDefinition.java
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

package net.sf.mpxj.primavera.common

/**
 * Represents the structure of a P3 or SureTrak table.
 */
class TableDefinition
/**
 * Constructor.
 *
 * @param pageSize page size in bytes
 * @param recordSize record size in bytes
 * @param primaryKeyColumnName optional primary key column name
 * @param rowValidator optional row validation
 * @param columns list of column definitions
 */
(
        /**
         * Retrieve the page size.
         *
         * @return page size in bytes
         */
        val pageSize: Int,
        /**
         * Retrieve the record size.
         *
         * @return record size in bytes
         */
        val recordSize: Int,
        /**
         * Retrieve the optional primary key column name.
         *
         * @return primary key column name or null
         */
        val primaryKeyColumnName: String?,
        /**
         * Retrieve the optional row validator.
         *
         * @return RowValidator instance or null
         */
        val rowValidator: RowValidator?, vararg columns: ColumnDefinition) {
    /**
     * Retrieve the column definitions.
     *
     * @return array of column definitions
     */
    val columns: Array<ColumnDefinition>

    /**
     * Constructor.
     *
     * @param pageSize page size in bytes
     * @param recordSize record size in bytes
     * @param columns list of column definitions
     */
    constructor(pageSize: Int, recordSize: Int, vararg columns: ColumnDefinition) : this(pageSize, recordSize, null, null, *columns) {}

    init {
        this.columns = columns
    }
}
