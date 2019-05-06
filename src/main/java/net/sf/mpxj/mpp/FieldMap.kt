/*
 * file:       FieldMap.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       13/04/2011
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

import java.io.PrintWriter
import java.io.StringWriter
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Date
import java.util.HashMap
import kotlin.collections.Map.Entry

import net.sf.mpxj.AccrueType
import net.sf.mpxj.BookingType
import net.sf.mpxj.ConstraintType
import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.DataType
import net.sf.mpxj.Duration
import net.sf.mpxj.EarnedValueMethod
import net.sf.mpxj.FieldContainer
import net.sf.mpxj.FieldType
import net.sf.mpxj.Priority
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Rate
import net.sf.mpxj.ResourceRequestType
import net.sf.mpxj.TaskType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.WorkGroup
import net.sf.mpxj.common.ByteArrayHelper
import net.sf.mpxj.common.NumberHelper

/**
 * This class is used to represent the mapping present in the MPP file
 * between fields and their locations in various data blocks.
 */
internal abstract class FieldMap
/**
 * Constructor.
 *
 * @param properties project properties
 * @param customFields custom field values
 */
(
        /**
         * Retrieve the project properties.
         *
         * @return project file
         */
        protected val projectProperties: ProjectProperties, protected var m_customFields: CustomFieldContainer) {

    /**
     * Abstract method used by child classes to supply default data.
     *
     * @return default data
     */
    protected abstract val defaultTaskData: Array<FieldItem>

    /**
     * Abstract method used by child classes to supply default data.
     *
     * @return default data
     */
    protected abstract val defaultResourceData: Array<FieldItem>

    /**
     * Abstract method used by child classes to supply default data.
     *
     * @return default data
     */
    protected abstract val defaultAssignmentData: Array<FieldItem>

    /**
     * Abstract method used by child classes to supply default data.
     *
     * @return default data
     */
    protected abstract val defaultRelationData: Array<FieldItem>
    private val m_map = HashMap<FieldType, FieldItem>()
    private val m_maxFixedDataSize = IntArray(MAX_FIXED_DATA_BLOCKS)
    private var m_debug: Boolean = false

    /**
     * When set to true, the selected field map will print details of the field structure.
     *
     * @param value pass try to enable output
     */
    fun setDebug(value: Boolean) {
        m_debug = value
    }

    /**
     * Generic method used to create a field map from a block of data.
     *
     * @param data field map data
     */
    private fun createFieldMap(data: ByteArray) {
        var index = 0
        var lastDataBlockOffset = 0
        var dataBlockIndex = 0

        while (index < data.size) {
            val mask = MPPUtility.getInt(data, index + 0).toLong()
            //mask = mask << 4;

            val dataBlockOffset = MPPUtility.getShort(data, index + 4)
            //int metaFlags = MPPUtility.getByte(data, index + 8);
            val type = getFieldType(MPPUtility.getInt(data, index + 12))
            val category = MPPUtility.getShort(data, index + 20)
            //int sizeInBytes = MPPUtility.getShort(data, index + 22);
            //int metaIndex = MPPUtility.getInt(data, index + 24);

            //
            // Categories
            //
            // 02 - Short values [RATE_UNITS, WORKGROUP, ACCRUE, TIME_UNITS, PRIORITY, TASK_TYPE, CONSTRAINT, ACCRUE, PERCENTAGE, SHORT, WORK_UNITS]  - BOOKING_TYPE, EARNED_VALUE_METHOD, DELIVERABLE_TYPE, RESOURCE_REQUEST_TYPE - we have as string in MPXJ????
            // 03 - Int values [DURATION, INTEGER] - Recalc outline codes as Boolean?
            // 05 - Rate, Number [RATE, NUMERIC]
            // 08 - String (and some durations!!!) [STRING, DURATION]
            // 0B - Boolean (meta block 0?) - [BOOLEAN]
            // 13 - Date - [DATE]
            // 48 - GUID - [GUID]
            // 64 - Boolean (meta block 1?)- [BOOLEAN]
            // 65 - Work, Currency [WORK, CURRENCY]
            // 66 - Units [UNITS]
            // 1D - Raw bytes [BINARY, ASCII_STRING] - Exception: outline code indexes, they are integers, but stored as part of a binary block

            val varDataKey: Int
            if (useTypeAsVarDataKey()) {
                val substitute = substituteVarDataKey(type)
                if (substitute == null) {
                    varDataKey = MPPUtility.getInt(data, index + 12) and 0x0000FFFF
                } else {
                    varDataKey = substitute!!.intValue()
                }
            } else {
                varDataKey = MPPUtility.getByte(data, index + 6)
            }

            val location: FieldLocation
            val metaBlock: Int

            when (category) {
                0x0B -> {
                    location = FieldLocation.META_DATA
                    metaBlock = 0
                }

                0x64 -> {
                    location = FieldLocation.META_DATA
                    metaBlock = 1
                }

                else -> {
                    metaBlock = 0
                    if (dataBlockOffset != 65535) {
                        location = FieldLocation.FIXED_DATA
                        if (dataBlockOffset < lastDataBlockOffset) {
                            ++dataBlockIndex
                        }
                        lastDataBlockOffset = dataBlockOffset
                        val typeSize = getFixedDataFieldSize(type!!)

                        if (dataBlockOffset + typeSize > m_maxFixedDataSize[dataBlockIndex]) {
                            m_maxFixedDataSize[dataBlockIndex] = dataBlockOffset + typeSize
                        }
                    } else {
                        if (varDataKey != 0) {
                            location = FieldLocation.VAR_DATA
                        } else {
                            location = FieldLocation.UNKNOWN
                        }
                    }
                }
            }

            val item = FieldItem(type, location, dataBlockIndex, dataBlockOffset, varDataKey, mask, metaBlock)
            if (m_debug) {
                System.out.println(ByteArrayHelper.hexdump(data, index, 28, false).toString() + " " + item + " mpxjDataType=" + item.type.getDataType() + " index=" + index)
            }
            m_map.put(type, item)

            index += 28
        }
    }

    /**
     * Used to determine what value is used as the var data key.
     *
     * @return true if the field type value is used as the var data key
     */
    protected abstract fun useTypeAsVarDataKey(): Boolean

    /**
     * Given a field ID, derive the field type.
     *
     * @param fieldID field ID
     * @return field type
     */
    protected abstract fun getFieldType(fieldID: Int): FieldType?

    /**
     * In some circumstances the var data key used in the file
     * does not match the var data key derived from the type.
     * This method is used to perform a substitution so that
     * the correct value is used.
     *
     * @param type field type to be tested
     * @return substituted value, or null
     */
    protected abstract fun substituteVarDataKey(type: FieldType): Integer?

    /**
     * Creates a field map for tasks.
     *
     * @param props props data
     */
    fun createTaskFieldMap(props: Props) {
        var fieldMapData: ByteArray? = null
        for (key in TASK_KEYS) {
            fieldMapData = props.getByteArray(key)
            if (fieldMapData != null) {
                break
            }
        }

        if (fieldMapData == null) {
            populateDefaultData(defaultTaskData)
        } else {
            createFieldMap(fieldMapData)
        }
    }

    /**
     * Creates a field map for relations.
     *
     * @param props props data
     */
    fun createRelationFieldMap(props: Props) {
        var fieldMapData: ByteArray? = null
        for (key in RELATION_KEYS) {
            fieldMapData = props.getByteArray(key)
            if (fieldMapData != null) {
                break
            }
        }

        if (fieldMapData == null) {
            populateDefaultData(defaultRelationData)
        } else {
            createFieldMap(fieldMapData)
        }
    }

    /**
     * Create a field map for enterprise custom fields.
     *
     * @param props props data
     * @param c target class
     */
    fun createEnterpriseCustomFieldMap(props: Props, c: Class<*>) {
        var fieldMapData: ByteArray? = null
        for (key in ENTERPRISE_CUSTOM_KEYS) {
            fieldMapData = props.getByteArray(key)
            if (fieldMapData != null) {
                break
            }
        }

        if (fieldMapData != null) {
            var index = 4
            while (index < fieldMapData.size) {
                //Looks like the custom fields have varying types, it may be that the last byte of the four represents the type?
                //System.out.println(ByteArrayHelper.hexdump(fieldMapData, index, 4, false));
                val typeValue = MPPUtility.getInt(fieldMapData, index)
                val type = getFieldType(typeValue)
                if (type != null && type!!.getClass() === c && type!!.toString().startsWith("Enterprise Custom Field")) {
                    val varDataKey = typeValue and 0xFFFF
                    val item = FieldItem(type, FieldLocation.VAR_DATA, 0, 0, varDataKey, 0, 0)
                    m_map.put(type, item)
                    //System.out.println(item);
                }
                //System.out.println((type == null ? "?" : type.getClass().getSimpleName() + "." + type) + " " + Integer.toHexString(typeValue));

                index += 4
            }
        }
    }

    /**
     * Creates a field map for resources.
     *
     * @param props props data
     */
    fun createResourceFieldMap(props: Props) {
        var fieldMapData: ByteArray? = null
        for (key in RESOURCE_KEYS) {
            fieldMapData = props.getByteArray(key)
            if (fieldMapData != null) {
                break
            }
        }

        if (fieldMapData == null) {
            populateDefaultData(defaultResourceData)
        } else {
            createFieldMap(fieldMapData)
        }
    }

    /**
     * Creates a field map for assignments.
     *
     * @param props props data
     */
    fun createAssignmentFieldMap(props: Props) {
        //System.out.println("ASSIGN");
        var fieldMapData: ByteArray? = null
        for (key in ASSIGNMENT_KEYS) {
            fieldMapData = props.getByteArray(key)
            if (fieldMapData != null) {
                break
            }
        }

        if (fieldMapData == null) {
            populateDefaultData(defaultAssignmentData)
        } else {
            createFieldMap(fieldMapData)
        }
    }

    /**
     * This method takes an array of data and uses this to populate the
     * field map.
     *
     * @param defaultData field map default data
     */
    private fun populateDefaultData(defaultData: Array<FieldItem>) {
        for (item in defaultData) {
            m_map.put(item.type, item)
        }
    }

    /**
     * Given a container, and a set of raw data blocks, this method extracts
     * the field data and writes it into the container.
     *
     * @param type expected type
     * @param container field container
     * @param id entity ID
     * @param fixedData fixed data block
     * @param varData var data block
     */
    fun populateContainer(type: Class<out FieldType>, container: FieldContainer, id: Integer, fixedData: Array<ByteArray>, varData: Var2Data) {
        //System.out.println(container.getClass().getSimpleName()+": " + id);
        for (item in m_map.values()) {
            if (item.type.getClass().equals(type)) {
                //System.out.println(item.m_type);
                val value = item.read(id, fixedData, varData)
                //System.out.println(item.m_type.getClass().getSimpleName() + "." + item.m_type +  ": " + value);
                container.set(item.type, value)
            }
        }
    }

    /**
     * Retrieve the maximum offset in the fixed data block.
     *
     * @param blockIndex required block index
     * @return maximum offset
     */
    fun getMaxFixedDataSize(blockIndex: Int): Int {
        return m_maxFixedDataSize[blockIndex]
    }

    /**
     * Retrieve the fixed data offset for a specific field.
     *
     * @param type field type
     * @return offset
     */
    fun getFixedDataOffset(type: FieldType): Int {
        val result: Int
        val item = m_map.get(type)
        if (item != null) {
            result = item!!.fixedDataOffset
        } else {
            result = -1
        }
        return result
    }

    /**
     * Retrieve the var data key for a specific field.
     *
     * @param type field type
     * @return var data key
     */
    fun getVarDataKey(type: FieldType): Integer? {
        var result: Integer? = null
        val item = m_map.get(type)
        if (item != null) {
            result = item!!.varDataKey
        }
        return result
    }

    /**
     * Used to map from a var data key to a field type. Note this
     * is designed for diagnostic use only, and uses an inefficient search.
     *
     * @param key var data key
     * @return field type
     */
    fun getFieldTypeFromVarDataKey(key: Integer): FieldType? {
        var result: FieldType? = null
        for (entry in m_map.entrySet()) {
            if (entry.getValue().getFieldLocation() === FieldLocation.VAR_DATA && entry.getValue().getVarDataKey().equals(key)) {
                result = entry.getKey()
                break
            }
        }
        return result
    }

    /**
     * Retrieve the field location for a specific field.
     *
     * @param type field type
     * @return field location
     */
    fun getFieldLocation(type: FieldType): FieldLocation? {
        var result: FieldLocation? = null

        val item = m_map.get(type)
        if (item != null) {
            result = item!!.fieldLocation
        }
        return result
    }

    /**
     * Retrieve a single field value.
     *
     * @param id parent entity ID
     * @param type field type
     * @param fixedData fixed data block
     * @param varData var data block
     * @return field value
     */
    protected fun getFieldData(id: Integer, type: FieldType, fixedData: Array<ByteArray>, varData: Var2Data): Object? {
        var result: Object? = null

        val item = m_map.get(type)
        if (item != null) {
            result = item!!.read(id, fixedData, varData)
        }

        return result
    }

    /**
     * Clear the field map.
     */
    fun clear() {
        m_map.clear()
        Arrays.fill(m_maxFixedDataSize, 0)
    }

    /**
     * Diagnostic method used to dump known field map data.
     *
     * @param props props block containing field map data
     */
    fun dumpKnownFieldMaps(props: Props) {
        //for (int key=131092; key < 131098; key++)
        for (key in 50331668..50331673) {
            val fieldMapData = props.getByteArray(Integer.valueOf(key))
            if (fieldMapData != null) {
                System.out.println("KEY: $key")
                createFieldMap(fieldMapData)
                System.out.println(toString())
                clear()
            }
        }
    }

    /**
     * Determine the size of a field in a fixed data block.
     *
     * @param type field data type
     * @return field size in bytes
     */
    private fun getFixedDataFieldSize(type: FieldType): Int {
        var result = 0
        val dataType = type.getDataType()
        if (dataType != null) {
            when (dataType) {
                DATE, INTEGER, DURATION -> {
                    result = 4
                }

                TIME_UNITS, CONSTRAINT, PRIORITY, PERCENTAGE, TASK_TYPE, ACCRUE, SHORT, BOOLEAN, DELAY, WORKGROUP, RATE_UNITS, EARNED_VALUE_METHOD, RESOURCE_REQUEST_TYPE -> {
                    result = 2
                }

                CURRENCY, UNITS, RATE, WORK -> {
                    result = 8
                }

                WORK_UNITS -> {
                    result = 1
                }

                GUID -> {
                    result = 16
                }

                else -> {
                    result = 0
                }
            }
        }

        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        val items = ArrayList<FieldItem>(m_map.values())
        Collections.sort(items)

        pw.println("[FieldMap")

        for (loop in m_maxFixedDataSize.indices) {
            pw.print(" MaxFixedOffset (block ")
            pw.print(loop)
            pw.print(")=")
            pw.println(m_maxFixedDataSize[loop])
        }

        for (item in items) {
            pw.print(" ")
            pw.println(item)
        }
        pw.println("]")

        pw.close()
        return sw.toString()
    }

    /**
     * Enumeration representing the location of field data.
     */
    internal enum class FieldLocation {
        FIXED_DATA,
        VAR_DATA,
        META_DATA,
        UNKNOWN
    }

    /**
     * This class is used to collect together the attributes necessary to
     * describe the location of each field within the MPP file. It also provides
     * the methods used to extract an individual field value.
     */
    inner class FieldItem
    /**
     * Constructor.
     *
     * @param type field type
     * @param location identifies which block the field is present in
     * @param fixedDataBlockIndex identifies which block the data comes from
     * @param fixedDataOffset fixed data block offset
     * @param varDataKey var data block key
     * @param mask TODO
     * @param metaBlock TODO
     */
    internal constructor(
            /**
             * Retrieve the field type.
             *
             * @return field type
             */
            val type: FieldType,
            /**
             * Retrieve the field location for this field.
             *
             * @return field location
             */
            val fieldLocation: FieldLocation,
            /**
             * Retrieve the index of the fixed data block containing this item.
             *
             * @return fixed data block index
             */
            val fixedDataBlockIndex: Int,
            /**
             * Retrieve the fixed data offset for this field.
             *
             * @return fixed data offset
             */
            val fixedDataOffset: Int, varDataKey: Int, private val m_mask: Long, private val m_metaBlock: Int) : Comparable<FieldItem> {
        /**
         * Retrieve the var data key for this field.
         *
         * @return var data key
         */
        val varDataKey: Integer

        init {
            this.varDataKey = Integer.valueOf(varDataKey)
        }

        /**
         * Reads a single field value.
         *
         * @param id parent entity ID
         * @param fixedData fixed data block
         * @param varData var data block
         * @return field value
         */
        fun read(id: Integer, fixedData: Array<ByteArray>, varData: Var2Data): Object? {
            var result: Object? = null

            when (fieldLocation) {
                FieldMap.FieldLocation.FIXED_DATA -> {
                    result = readFixedData(id, fixedData, varData)
                }

                FieldMap.FieldLocation.VAR_DATA -> {
                    result = readVarData(id, fixedData, varData)
                }

                FieldMap.FieldLocation.META_DATA -> {
                }// We know that the Boolean flags are stored in the
                // "meta data" block, and can see that the first
                // four bytes of each row read from the field map
                // data in the MPP file represents a bit mask... but
                // we just haven't worked out how to convert this into
                // the actual location in the data. For now we rely on
                // the location in the file being fixed. This is why
                // we ignore the META_DATA case.

                else -> {
                }// Unknown location - ignore this.
            }

            return result
        }

        /**
         * Read a field from the fixed data block.
         *
         * @param id parent entity ID
         * @param fixedData fixed data block
         * @param varData var data block
         * @return field value
         */
        private fun readFixedData(id: Integer, fixedData: Array<ByteArray>, varData: Var2Data): Object? {
            var result: Object? = null
            if (fixedDataBlockIndex < fixedData.size) {
                val data = fixedData[fixedDataBlockIndex]
                if (data != null && fixedDataOffset < data.size) {
                    when (type.getDataType()) {
                        DATE -> {
                            result = MPPUtility.getTimestamp(data, fixedDataOffset)
                        }

                        INTEGER -> {
                            result = Integer.valueOf(MPPUtility.getInt(data, fixedDataOffset))
                        }

                        DURATION -> {
                            val unitsType = type.getUnitsType()
                            var units = getFieldData(id, unitsType, fixedData, varData) as TimeUnit?
                            if (units == null) {
                                units = projectProperties.defaultDurationUnits
                            }

                            result = MPPUtility.getAdjustedDuration(projectProperties, MPPUtility.getInt(data, fixedDataOffset), units)
                        }

                        TIME_UNITS -> {
                            result = MPPUtility.getDurationTimeUnits(MPPUtility.getShort(data, fixedDataOffset), projectProperties.defaultDurationUnits)
                        }

                        CONSTRAINT -> {
                            result = ConstraintType.getInstance(MPPUtility.getShort(data, fixedDataOffset))
                        }

                        PRIORITY -> {
                            result = Priority.getInstance(MPPUtility.getShort(data, fixedDataOffset))
                        }

                        PERCENTAGE -> {
                            result = MPPUtility.getPercentage(data, fixedDataOffset)
                        }

                        TASK_TYPE -> {
                            result = TaskType.getInstance(MPPUtility.getShort(data, fixedDataOffset))
                        }

                        ACCRUE -> {
                            result = AccrueType.getInstance(MPPUtility.getShort(data, fixedDataOffset))
                        }

                        CURRENCY, UNITS -> {
                            result = NumberHelper.getDouble(MPPUtility.getDouble(data, fixedDataOffset) / 100)
                        }

                        RATE -> {
                            result = Rate(MPPUtility.getDouble(data, fixedDataOffset), TimeUnit.HOURS)
                        }

                        WORK -> {
                            result = Duration.getInstance(MPPUtility.getDouble(data, fixedDataOffset) / 60000, TimeUnit.HOURS)
                        }

                        SHORT -> {
                            result = Integer.valueOf(MPPUtility.getShort(data, fixedDataOffset))
                        }

                        BOOLEAN -> {
                            result = Boolean.valueOf(MPPUtility.getShort(data, fixedDataOffset) != 0)
                        }

                        DELAY -> {
                            result = MPPUtility.getDuration(MPPUtility.getShort(data, fixedDataOffset), TimeUnit.HOURS)
                        }

                        WORK_UNITS -> {
                            val variableRateUnitsValue = MPPUtility.getByte(data, fixedDataOffset)
                            result = if (variableRateUnitsValue == 0) null else MPPUtility.getWorkTimeUnits(variableRateUnitsValue)
                        }

                        WORKGROUP -> {
                            result = WorkGroup.getInstance(MPPUtility.getShort(data, fixedDataOffset))
                        }

                        RATE_UNITS -> {
                            result = TimeUnit.getInstance(MPPUtility.getShort(data, fixedDataOffset) - 1)
                        }

                        EARNED_VALUE_METHOD -> {
                            result = EarnedValueMethod.getInstance(MPPUtility.getShort(data, fixedDataOffset))
                        }

                        RESOURCE_REQUEST_TYPE -> {
                            result = ResourceRequestType.getInstance(MPPUtility.getShort(data, fixedDataOffset))
                        }

                        GUID -> {
                            result = MPPUtility.getGUID(data, fixedDataOffset)
                        }

                        BINARY -> {
                        }// Do nothing for binary data

                        else -> {
                        }//System.out.println("**** UNSUPPORTED FIXED DATA TYPE");
                    }
                }
            }
            return result
        }

        /**
         * Read a field value from a var data block.
         *
         * @param id parent entity ID
         * @param fixedData fixed data block
         * @param varData var data block
         * @return field value
         */
        private fun readVarData(id: Integer, fixedData: Array<ByteArray>, varData: Var2Data): Object? {
            var result: Object? = null

            when (type.getDataType()) {
                DURATION -> {
                    val unitsType = type.getUnitsType()
                    var units = getFieldData(id, unitsType, fixedData, varData) as TimeUnit?
                    if (units == null) {
                        units = TimeUnit.HOURS
                    }
                    result = getCustomFieldDurationValue(varData, id, varDataKey, units)
                }

                TIME_UNITS -> {
                    result = MPPUtility.getDurationTimeUnits(varData.getShort(id, varDataKey), projectProperties.defaultDurationUnits)
                }

                CURRENCY -> {
                    result = NumberHelper.getDouble(varData.getDouble(id, varDataKey) / 100)
                }

                STRING -> {
                    result = getCustomFieldUnicodeStringValue(varData, id, varDataKey)
                }

                DATE -> {
                    result = getCustomFieldTimestampValue(varData, id, varDataKey)
                }

                NUMERIC -> {
                    result = getCustomFieldDoubleValue(varData, id, varDataKey)
                }

                INTEGER -> {
                    result = Integer.valueOf(varData.getInt(id, varDataKey))
                }

                WORK -> {
                    result = Duration.getInstance(varData.getDouble(id, varDataKey) / 60000, TimeUnit.HOURS)
                }

                ASCII_STRING -> {
                    result = varData.getString(id, varDataKey)
                }

                DELAY -> {
                    result = MPPUtility.getDuration(varData.getShort(id, varDataKey), TimeUnit.HOURS)
                }

                WORK_UNITS -> {
                    val variableRateUnitsValue = varData.getByte(id, varDataKey)
                    result = if (variableRateUnitsValue == 0) null else MPPUtility.getWorkTimeUnits(variableRateUnitsValue)
                }

                RATE_UNITS -> {
                    result = TimeUnit.getInstance(varData.getShort(id, varDataKey) - 1)
                }

                EARNED_VALUE_METHOD -> {
                    result = EarnedValueMethod.getInstance(varData.getShort(id, varDataKey))
                }

                RESOURCE_REQUEST_TYPE -> {
                    result = ResourceRequestType.getInstance(varData.getShort(id, varDataKey))
                }

                ACCRUE -> {
                    result = AccrueType.getInstance(varData.getShort(id, varDataKey))
                }

                SHORT -> {
                    result = Integer.valueOf(varData.getShort(id, varDataKey))
                }

                BOOLEAN -> {
                    result = Boolean.valueOf(varData.getShort(id, varDataKey) != 0)
                }

                WORKGROUP -> {
                    result = WorkGroup.getInstance(varData.getShort(id, varDataKey))
                }

                GUID -> {
                    result = MPPUtility.getGUID(varData.getByteArray(id, varDataKey), 0)
                }

                BOOKING_TYPE -> {
                    result = BookingType.getInstance(varData.getShort(id, varDataKey))
                }

                BINARY -> {
                }// Do nothing for binary data

                else -> {
                }//System.out.println("**** UNSUPPORTED VAR DATA TYPE");
            }

            return result
        }

        /**
         * Retrieve custom field value.
         *
         * @param varData var data block
         * @param id item ID
         * @param type item type
         * @return item value
         */
        private fun getCustomFieldTimestampValue(varData: Var2Data, id: Integer, type: Integer): Object? {
            var result: Object? = null

            //
            // Note that this simplistic approach could produce false positives
            //
            val mask = varData.getShort(id, type)
            if (mask and 0xFF00 != VALUE_LIST_MASK) {
                result = getRawTimestampValue(varData, id, type)
            } else {
                val uniqueId = varData.getInt(id, 2, type)
                val item = m_customFields.getCustomFieldValueItemByUniqueID(uniqueId)
                if (item != null) {
                    val value = item!!.value
                    if (value is Date) {
                        result = value
                    }
                }

                //
                // If we can't find a custom field value with this ID, fall back to treating this as a normal value
                //
                if (result == null) {
                    result = getRawTimestampValue(varData, id, type)
                }
            }
            return result
        }

        /**
         * Retrieve a timestamp value.
         *
         * @param varData var data block
         * @param id item ID
         * @param type item type
         * @return item value
         */
        private fun getRawTimestampValue(varData: Var2Data, id: Integer, type: Integer): Object? {
            var result: Object? = null
            val data = varData.getByteArray(id, type)
            if (data != null) {
                if (data.size == 512) {
                    result = MPPUtility.getUnicodeString(data, 0)
                } else {
                    if (data.size >= 4) {
                        result = MPPUtility.getTimestamp(data, 0)
                    }
                }
            }
            return result
        }

        /**
         * Retrieve custom field value.
         *
         * @param varData var data block
         * @param id item ID
         * @param type item type
         * @param units duration units
         * @return item value
         */
        private fun getCustomFieldDurationValue(varData: Var2Data, id: Integer, type: Integer, units: TimeUnit): Object? {
            var result: Object? = null

            val data = varData.getByteArray(id, type)

            if (data != null) {
                if (data.size == 512) {
                    result = MPPUtility.getUnicodeString(data, 0)
                } else {
                    if (data.size >= 4) {
                        val duration = MPPUtility.getInt(data, 0)
                        result = MPPUtility.getAdjustedDuration(projectProperties, duration, units)
                    }
                }
            }

            return result
        }

        /**
         * Retrieve custom field value.
         *
         * @param varData var data block
         * @param id item ID
         * @param type item type
         * @return item value
         */
        private fun getCustomFieldDoubleValue(varData: Var2Data, id: Integer, type: Integer): Double {
            var result = 0.0

            //
            // Note that this simplistic approach could produce false positives
            //
            val mask = varData.getShort(id, type)
            if (mask and 0xFF00 != VALUE_LIST_MASK) {
                result = varData.getDouble(id, type)
            } else {
                val uniqueId = varData.getInt(id, 2, type)
                val item = m_customFields.getCustomFieldValueItemByUniqueID(uniqueId)
                if (item != null) {
                    val value = item!!.value
                    if (value is Number) {
                        result = (value as Number).doubleValue()
                    }
                }
            }
            return NumberHelper.getDouble(result)
        }

        /**
         * Retrieve custom field value.
         *
         * @param varData var data block
         * @param id item ID
         * @param type item type
         * @return item value
         */
        private fun getCustomFieldUnicodeStringValue(varData: Var2Data, id: Integer, type: Integer): String? {
            var result: String? = null

            //
            // Note that this simplistic approach could produce false positives
            //
            val mask = varData.getShort(id, type)
            if (mask and 0xFF00 != VALUE_LIST_MASK) {
                result = varData.getUnicodeString(id, type)
            } else {
                val uniqueId = varData.getInt(id, 2, type)
                val item = m_customFields.getCustomFieldValueItemByUniqueID(uniqueId)
                if (item != null) {
                    val value = item!!.value
                    if (value is String) {
                        result = value
                    }
                }
            }
            return result
        }

        /**
         * Implements the only method in the Comparable interface to allow
         * FieldItem instances to be sorted.
         *
         * @param item item to compare with
         * @return comparison result
         */
        @Override
        fun compareTo(item: FieldItem): Int {
            var result = fieldLocation.compareTo(item.fieldLocation)
            if (result == 0) {
                when (fieldLocation) {
                    FieldMap.FieldLocation.FIXED_DATA -> {
                        result = fixedDataBlockIndex - item.fixedDataBlockIndex
                        if (result == 0) {
                            result = fixedDataOffset - item.fixedDataOffset
                        }
                    }

                    FieldMap.FieldLocation.VAR_DATA -> {
                        result = varDataKey.intValue() - item.varDataKey.intValue()
                    }

                    else -> {
                    }
                }
            }
            return result
        }

        /**
         * {@inheritDoc}
         */
        @Override
        fun toString(): String {
            val buffer = StringBuilder()
            buffer.append("[FieldItem type=")
            buffer.append(type.getFieldTypeClass())
            buffer.append('.')
            buffer.append(type)
            buffer.append(" location=")
            buffer.append(fieldLocation)

            when (fieldLocation) {
                FieldMap.FieldLocation.FIXED_DATA -> {
                    buffer.append(" fixedDataBlockIndex=")
                    buffer.append(fixedDataBlockIndex)
                    buffer.append(" fixedDataBlockOffset=")
                    buffer.append(fixedDataOffset)
                }

                FieldMap.FieldLocation.VAR_DATA -> {
                    buffer.append(" varDataKey=")
                    buffer.append(varDataKey)
                }

                FieldMap.FieldLocation.META_DATA -> {
                    buffer.append(" mask=")
                    buffer.append(Long.toHexString(m_mask))
                    buffer.append(" block=")
                    buffer.append(m_metaBlock)
                }

                else -> {
                }
            }

            buffer.append("]")

            return buffer.toString()
        }
    }

    companion object {

        private val TASK_KEYS = arrayOf<Integer>(Props.TASK_FIELD_MAP, Props.TASK_FIELD_MAP2)

        private val ENTERPRISE_CUSTOM_KEYS = arrayOf<Integer>(Props.ENTERPRISE_CUSTOM_FIELD_MAP)

        private val RESOURCE_KEYS = arrayOf<Integer>(Props.RESOURCE_FIELD_MAP, Props.RESOURCE_FIELD_MAP2)

        private val ASSIGNMENT_KEYS = arrayOf<Integer>(Props.ASSIGNMENT_FIELD_MAP, Props.ASSIGNMENT_FIELD_MAP2)

        private val RELATION_KEYS = arrayOf<Integer>(Props.RELATION_FIELD_MAP)

        private val VALUE_LIST_MASK = 0x0700

        private val MAX_FIXED_DATA_BLOCKS = 2
    }
}
