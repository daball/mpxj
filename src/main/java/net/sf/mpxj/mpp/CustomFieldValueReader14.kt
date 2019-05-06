/*
 * file:       CustomFieldValueReader14.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2015
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

package net.sf.mpxj.mpp

import java.util.HashMap
import java.util.UUID

import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.FieldType
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.common.FieldTypeHelper
import net.sf.mpxj.common.NumberHelper

/**
 * MPP14 custom field value reader.
 */
class CustomFieldValueReader14
/**
 * Constructor.
 *
 * @param properties project properties
 * @param container custom field config
 * @param outlineCodeVarMeta raw mpp data
 * @param outlineCodeVarData raw mpp data
 * @param outlineCodeFixedData raw mpp data
 * @param outlineCodeFixedData2 raw mpp data
 * @param taskProps raw mpp data
 */
(properties: ProjectProperties, container: CustomFieldContainer, outlineCodeVarMeta: VarMeta, outlineCodeVarData: Var2Data, outlineCodeFixedData: FixedData, outlineCodeFixedData2: FixedData, taskProps: Props) : CustomFieldValueReader(properties, container, outlineCodeVarMeta, outlineCodeVarData, outlineCodeFixedData, outlineCodeFixedData2, taskProps) {

    @Override
    override fun process() {
        val uniqueid = m_outlineCodeVarMeta.uniqueIdentifierArray
        val parentOffset: Int
        val typeOffset: Int
        val fieldOffset: Int

        if (NumberHelper.getInt(m_properties.applicationVersion) > ApplicationVersion.PROJECT_2010) {
            typeOffset = 16
            fieldOffset = 18
            parentOffset = 10
        } else {
            fieldOffset = 16
            typeOffset = 32
            parentOffset = 8
        }

        val map = populateCustomFieldMap()
        for (loop in uniqueid.indices) {
            val id = uniqueid[loop]

            val item = CustomFieldValueItem(id)
            val value = m_outlineCodeVarData.getByteArray(id, CustomFieldValueReader.VALUE_LIST_VALUE)
            item.description = m_outlineCodeVarData.getUnicodeString(id, CustomFieldValueReader.VALUE_LIST_DESCRIPTION)
            item.unknown = m_outlineCodeVarData.getByteArray(id, CustomFieldValueReader.VALUE_LIST_UNKNOWN)

            val b = m_outlineCodeFixedData.getByteArrayValue(loop + 3)
            if (b != null) {
                item.parent = Integer.valueOf(MPPUtility.getShort(b, parentOffset))
            }

            val b2 = m_outlineCodeFixedData2.getByteArrayValue(loop + 3)
            if (b2 != null) {
                item.guid = MPPUtility.getGUID(b2, 0)
                val parentField = MPPUtility.getGUID(b2, fieldOffset)
                val type = MPPUtility.getShort(b2, typeOffset)
                item.value = getTypedValue(type, value)
                val field = map[parentField]
                if (field != null) {
                    m_container.getCustomField(field).getLookupTable().add(item)
                }
            }
        }
    }

    /**
     * Generate a map of UUID values to field types.
     *
     * @return uUID field value map
     */
    private fun populateCustomFieldMap(): Map<UUID, FieldType> {
        val map = HashMap<UUID, FieldType>()
        val data = m_taskProps.getByteArray(Props.CUSTOM_FIELDS)
        if (data != null) {
            val length = MPPUtility.getInt(data, 0)
            var index = length + 36

            while (index + 52 <= data.size) {
                val fieldID = MPPUtility.getInt(data, index + 0)
                val field = FieldTypeHelper.getInstance(fieldID)
                val guid = MPPUtility.getGUID(data, index + 36)
                map.put(guid, field)

                index += 88
            }
        }
        return map
    }
}
