/*
 * file:       Row.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       07/04/2011
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

import java.util.Date

import net.sf.mpxj.Duration

/**
 * This interface represents a row in a database table. It is envisaged that
 * rows could be retrieved from a database either via Jackcess, in which case
 * the row data will be in the form of a Map, or from a JDBC data source,
 * in which case the row will be in the form of a Result set. Classes that
 * implement this interface will wrap one of these types to provide a consistent
 * interface to MPXJ.
 */
internal interface Row {

    /**
     * Retrieve a list of child rows.
     *
     * @return list of child rows
     */
    val childRows: List<Row>

    /**
     * Retrieve a string attribute.
     *
     * @param name attribute name
     * @return attribute value
     */
    fun getString(name: String): String

    /**
     * Retrieve an Integer attribute.
     *
     * @param name attribute name
     * @return attribute value
     */
    fun getInteger(name: String): Integer

    /**
     * Retrieve a Double attribute.
     *
     * @param name attribute name
     * @return attribute value
     */
    fun getDouble(name: String): Double

    /**
     * Retrieve a currency attribute.
     *
     * @param name attribute name
     * @return attribute value
     */
    fun getCurrency(name: String): Double

    /**
     * Retrieve a boolean attribute.
     *
     * @param name attribute name
     * @return attribute value
     */
    fun getBoolean(name: String): Boolean

    /**
     * Retrieve an in attribute.
     *
     * @param name attribute name
     * @return attribute value
     */
    fun getInt(name: String): Int

    /**
     * Retrieve a date attribute.
     *
     * @param name attribute name
     * @return attribute value
     */
    fun getDate(name: String): Date

    /**
     * Retrieve a duration attribute.
     *
     * @param name attribute name
     * @return attribute value
     */
    fun getDuration(name: String): Duration

    /**
     * Retrieve a duration attribute.
     *
     * @param name attribute name
     * @return attribute value
     */
    fun getWork(name: String): Duration

    /**
     * Add a child Row to this row.
     *
     * @param row child row
     */
    fun addChild(row: Row)

    /**
     * Merge the columns from another row with this row.
     *
     * @param row row to merge
     * @param prefix prefix used to avoid name collisions
     */
    fun merge(row: Row, prefix: String)
}
