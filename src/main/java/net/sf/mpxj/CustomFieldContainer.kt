/*
 * file:       CustomFieldContainer.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-20015
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

import java.util.HashMap

import net.sf.mpxj.common.Pair
import net.sf.mpxj.mpp.CustomFieldValueItem

/**
 * Container holding configuration details for all custom fields.
 */
class CustomFieldContainer : Iterable<CustomField> {

    private val m_configMap = HashMap<FieldType, CustomField>()
    private val m_valueMap = HashMap<Integer, CustomFieldValueItem>()
    private val m_aliasMap = HashMap<Pair<FieldTypeClass, String>, FieldType>()
    /**
     * Retrieve configuration details for a given custom field.
     *
     * @param field required custom field
     * @return configuration detail
     */
    fun getCustomField(field: FieldType): CustomField {
        var result = m_configMap.get(field)
        if (result == null) {
            result = CustomField(field, this)
            m_configMap.put(field, result)
        }
        return result
    }

    /**
     * Return the number of custom fields.
     *
     * @return number of custom fields
     */
    fun size(): Int {
        return m_configMap.values().size()
    }

    @Override
    fun iterator(): Iterator<CustomField> {
        return m_configMap.values().iterator()
    }

    /**
     * Retrieve a custom field value by its unique ID.
     *
     * @param uniqueID custom field value unique ID
     * @return custom field value
     */
    fun getCustomFieldValueItemByUniqueID(uniqueID: Int): CustomFieldValueItem {
        return m_valueMap.get(Integer.valueOf(uniqueID))
    }

    /**
     * Add a value to the custom field value index.
     *
     * @param item custom field value
     */
    fun registerValue(item: CustomFieldValueItem) {
        m_valueMap.put(item.uniqueID, item)
    }

    /**
     * Remove a value from the custom field value index.
     *
     * @param item custom field value
     */
    fun deregisterValue(item: CustomFieldValueItem) {
        m_valueMap.remove(item.uniqueID)
    }

    /**
     * When an alias for a field is added, index it here to allow lookup by alias and type.
     *
     * @param type field type
     * @param alias field alias
     */
    internal fun registerAlias(type: FieldType, alias: String) {
        m_aliasMap.put(Pair<FieldTypeClass, String>(type.getFieldTypeClass(), alias), type)
    }

    /**
     * Retrieve a field from a particular entity using its alias.
     *
     * @param typeClass the type of entity we are interested in
     * @param alias the alias
     * @return the field type referred to be the alias, or null if not found
     */
    fun getFieldByAlias(typeClass: FieldTypeClass, alias: String): FieldType {
        return m_aliasMap.get(Pair<FieldTypeClass, String>(typeClass, alias))
    }
}
