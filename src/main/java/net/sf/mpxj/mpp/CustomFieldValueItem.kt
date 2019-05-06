/*
 * file:       CustomFieldValueItem.java
 * author:     Jari Niskala
 * copyright:  (c) Packwood Software 2008
 * date:       17/01/2008
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

package net.sf.mpxj.mpp

import java.util.UUID

/**
 * Instances of this type represent a possible value for a custom field that is
 * using value lists.
 */
class CustomFieldValueItem
/**
 * Constructor.
 *
 * @param uniqueID item unique ID
 */
(
        /**
         * Get the unique id for this item.
         *
         * @return item unique ID
         */
        val uniqueID: Integer) {
    /**
     * Retrieve the GUID for this value.
     *
     * @return value GUID
     */
    /**
     * Set the GUID for this value.
     *
     * @param guid value GUID
     */
    var guid: UUID? = null
    /**
     * Get the value of this item.
     *
     * @return item value
     */
    /**
     * Set the value of this item.
     *
     * @param value item value
     */
    var value: Object? = null
    /**
     * Get the description for this item.
     *
     * @return item description
     */
    /**
     * Set the description for this item.
     *
     * @param description item description
     */
    var description: String? = null
    /**
     * Get an unknown property for this item.
     *
     * @return unknown data block
     */
    /**
     * Set an Unknown property for this item.
     *
     * @param unknown unknown data block
     */
    var unknown: ByteArray? = null
    /**
     * Retrieve the parent ID.
     *
     * @return parent IDs
     */
    /**
     * Set the parent ID.
     *
     * @param id parent ID
     */
    var parent: Integer? = null

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return String.format("[CustomFieldValueItem uniqueID=%d guid=%s parentId=%d value=%s", uniqueID, guid, parent, String.valueOf(value))
    }
}
