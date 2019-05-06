/*
 * file:       CustomFieldValueReader12.java
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

import java.io.IOException
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import kotlin.collections.Map.Entry

import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream

import net.sf.mpxj.CustomField
import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.CustomFieldLookupTable
import net.sf.mpxj.DataType
import net.sf.mpxj.Duration
import net.sf.mpxj.FieldType
import net.sf.mpxj.FieldTypeClass
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.FieldTypeHelper
import net.sf.mpxj.common.Pair

/**
 * MPP9 custom field value reader.
 */
class CustomFieldValueReader9
/**
 * Constructor.
 *
 * @param projectDir project directory
 * @param properties MPXJ project properties
 * @param projectProps MPP project properties
 * @param container custom field container
 */
(private val m_projectDir: DirectoryEntry, private val m_properties: ProjectProperties, private val m_projectProps: Props, private val m_container: CustomFieldContainer) {

    /**
     * Reads custom field values and populates container.
     */
    @Throws(IOException::class)
    fun process() {
        processCustomFieldValues()
        processOutlineCodeValues()
    }

    /**
     * Reads non outline code custom field values and populates container.
     */
    private fun processCustomFieldValues() {
        val data = m_projectProps.getByteArray(Props.TASK_FIELD_ATTRIBUTES)
        if (data != null) {
            var index = 0
            var offset = 0
            // First the length
            val length = MPPUtility.getInt(data, offset)
            offset += 4
            // Then the number of custom value lists
            val numberOfValueLists = MPPUtility.getInt(data, offset)
            offset += 4

            // Then the value lists themselves
            var field: FieldType
            var valueListOffset = 0
            while (index < numberOfValueLists && offset < length) {
                // Each item consists of the Field ID (4 bytes) and the offset to the value list (4 bytes)

                // Get the Field
                field = FieldTypeHelper.getInstance(MPPUtility.getInt(data, offset))
                offset += 4

                // Get the value list offset
                valueListOffset = MPPUtility.getInt(data, offset)
                offset += 4
                // Read the value list itself
                if (valueListOffset < data.size) {
                    var tempOffset = valueListOffset
                    tempOffset += 8
                    // Get the data offset
                    val dataOffset = MPPUtility.getInt(data, tempOffset) + valueListOffset
                    tempOffset += 4
                    // Get the end of the data offset
                    val endDataOffset = MPPUtility.getInt(data, tempOffset) + valueListOffset
                    tempOffset += 4
                    // Get the end of the description
                    val endDescriptionOffset = MPPUtility.getInt(data, tempOffset) + valueListOffset

                    // Get the values themselves
                    val valuesLength = endDataOffset - dataOffset
                    val values = ByteArray(valuesLength)
                    MPPUtility.getByteArray(data, dataOffset, valuesLength, values, 0)

                    // Get the descriptions
                    val descriptionsLength = endDescriptionOffset - endDataOffset
                    val descriptions = ByteArray(descriptionsLength)
                    MPPUtility.getByteArray(data, endDataOffset, descriptionsLength, descriptions, 0)

                    populateContainer(field, values, descriptions)
                }
                index++
            }
        }
    }

    /**
     * Reads outline code custom field values and populates container.
     */
    @Throws(IOException::class)
    private fun processOutlineCodeValues() {
        val outlineCodeDir = m_projectDir.getEntry("TBkndOutlCode") as DirectoryEntry
        val fm = FixedMeta(DocumentInputStream(outlineCodeDir.getEntry("FixedMeta") as DocumentEntry), 10)
        val fd = FixedData(fm, DocumentInputStream(outlineCodeDir.getEntry("FixedData") as DocumentEntry))

        val map = HashMap<Integer, FieldType>()

        val items = fm.itemCount
        for (loop in 0 until items) {
            val data = fd.getByteArrayValue(loop)
            if (data!!.size < 18) {
                continue
            }

            val index = MPPUtility.getShort(data, 0)
            val fieldID = MPPUtility.getInt(data, 12)
            val fieldType = FieldTypeHelper.getInstance(fieldID)
            if (fieldType.getFieldTypeClass() !== FieldTypeClass.UNKNOWN) {
                map.put(Integer.valueOf(index), fieldType)
            }
        }

        val outlineCodeVarMeta = VarMeta9(DocumentInputStream(outlineCodeDir.getEntry("VarMeta") as DocumentEntry))
        val outlineCodeVarData = Var2Data(outlineCodeVarMeta, DocumentInputStream(outlineCodeDir.getEntry("Var2Data") as DocumentEntry))

        val valueMap = HashMap<FieldType, List<Pair<String, String>>>()

        for (id in outlineCodeVarMeta.uniqueIdentifierArray) {
            val fieldType = map.get(id)
            val value = outlineCodeVarData.getUnicodeString(id, VALUE)
            val description = outlineCodeVarData.getUnicodeString(id, DESCRIPTION)

            var list = valueMap.get(fieldType)
            if (list == null) {
                list = ArrayList<Pair<String, String>>()
                valueMap.put(fieldType, list)
            }
            list!!.add(Pair<String, String>(value, description))
        }

        for (entry in valueMap.entrySet()) {
            populateContainer(entry.getKey(), entry.getValue())
        }
    }

    /**
     * Populate the container, converting raw data into Java types.
     *
     * @param field custom field to which these values belong
     * @param values raw value data
     * @param descriptions raw description data
     */
    private fun populateContainer(field: FieldType, values: ByteArray, descriptions: ByteArray) {
        val config = m_container.getCustomField(field)
        val table = config.getLookupTable()

        val descriptionList = convertType(DataType.STRING, descriptions)
        val valueList = convertType(field.getDataType(), values)
        for (index in 0 until descriptionList.size()) {
            val item = CustomFieldValueItem(Integer.valueOf(0))
            item.description = descriptionList[index]
            if (index < valueList.size()) {
                item.value = valueList[index]
            }
            table.add(item)
        }
    }

    /**
     * Populate the container from outline code data.
     *
     * @param field field type
     * @param items pairs of values and descriptions
     */
    private fun populateContainer(field: FieldType, items: List<Pair<String, String>>) {
        val config = m_container.getCustomField(field)
        val table = config.getLookupTable()

        for (pair in items) {
            val item = CustomFieldValueItem(Integer.valueOf(0))
            item.value = pair.first
            item.description = pair.second
            table.add(item)
        }
    }

    /**
     * Convert raw data into Java types.
     *
     * @param type data type
     * @param data raw data
     * @return list of Java object
     */
    private fun convertType(type: DataType, data: ByteArray): List<Object> {
        val result = ArrayList<Object>()
        var index = 0

        while (index < data.size) {
            when (type) {
                STRING -> {
                    val value = MPPUtility.getUnicodeString(data, index)
                    result.add(value)
                    index += (value.length() + 1) * 2
                }

                CURRENCY -> {
                    val value = Double.valueOf(MPPUtility.getDouble(data, index) / 100)
                    result.add(value)
                    index += 8
                }

                NUMERIC -> {
                    val value = Double.valueOf(MPPUtility.getDouble(data, index))
                    result.add(value)
                    index += 8
                }

                DATE -> {
                    val value = MPPUtility.getTimestamp(data, index)
                    result.add(value)
                    index += 4

                }

                DURATION -> {
                    val units = MPPUtility.getDurationTimeUnits(MPPUtility.getShort(data, index + 4), m_properties.defaultDurationUnits)
                    val value = MPPUtility.getAdjustedDuration(m_properties, MPPUtility.getInt(data, index), units)
                    result.add(value)
                    index += 6
                }

                BOOLEAN -> {
                    val value = Boolean.valueOf(MPPUtility.getShort(data, index) == 1)
                    result.add(value)
                    index += 2
                }

                else -> {
                    index = data.size
                }
            }
        }

        return result
    }

    companion object {

        private val VALUE = Integer.valueOf(1)
        private val DESCRIPTION = Integer.valueOf(2)
    }

}
