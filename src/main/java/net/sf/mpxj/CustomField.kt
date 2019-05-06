/*
 * file:       CustomField.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2015
 * date:       28/04/2015
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

package net.sf.mpxj

/**
 * Configuration detail for a custom field.
 */
class CustomField
/**
 * Constructor.
 *
 * @param field field
 * @param parent parent container
 */
(
        /**
         * Retrieve the field type represented by this instance.
         *
         * @return field type
         */
        val fieldType: FieldType, private val m_parent: CustomFieldContainer) {
    /**
     * Retrieve the value lookup table associated with this custom field.
     *
     * @return value lookup table
     */
    val lookupTable: CustomFieldLookupTable
    /**
     * Retrieve the graphical indicator configuration for this custom field.
     *
     * @return graphical indicator configuration
     */
    val graphicalIndicator: GraphicalIndicator
    /**
     * Retrieve the alias for this field.
     *
     * @return field alias
     */
    /**
     * Set the alias for this field.
     *
     * @param alias field alias
     */
    var alias: String? = null
        set(alias) {
            field = alias
            m_parent.registerAlias(fieldType, alias)
        }

    init {
        lookupTable = CustomFieldLookupTable(m_parent)
        graphicalIndicator = GraphicalIndicator()
    }
}
