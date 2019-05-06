/*
 * file:       CriteriaReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       2010-05-06
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

import java.util.LinkedList
import java.util.TreeMap

import net.sf.mpxj.DataType
import net.sf.mpxj.FieldType
import net.sf.mpxj.FieldTypeClass
import net.sf.mpxj.GenericCriteria
import net.sf.mpxj.GenericCriteriaPrompt
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.TestOperator

/**
 * This class allows criteria definitions to be read from an MPP file.
 */
abstract class CriteriaReader {
    /**
     * Retrieves the offset of the start of the criteria data.
     *
     * @return criteria start offset
     */
    protected abstract val criteriaStartOffset: Int

    /**
     * Retrieves the criteria block size.
     *
     * @return criteria block size
     */
    protected abstract val criteriaBlockSize: Int

    /**
     * Retrieves the offset of the field value.
     *
     * @return field value offset
     */
    protected abstract val valueOffset: Int

    /**
     * Retrieves the offset of the time unit field.
     *
     * @return time unit field offset
     */
    protected abstract val timeUnitsOffset: Int

    /**
     * Retrieves offset of value which determines the start of the text block.
     *
     * @return criteria text start offset
     */
    protected abstract val criteriaTextStartOffset: Int

    private var m_properties: ProjectProperties? = null
    private var m_criteriaData: ByteArray? = null
    private var m_criteriaType: BooleanArray? = null
    private var m_criteriaTextStart: Int = 0
    private var m_dataOffset: Int = 0
    private var m_prompts: List<GenericCriteriaPrompt>? = null
    private var m_fields: List<FieldType>? = null
    protected var m_criteriaBlockMap: Map<Integer, ByteArray> = TreeMap<Integer, ByteArray>()

    /**
     * Retrieves the child of the current block.
     *
     * @param block parent block
     * @return child block
     */
    protected abstract fun getChildBlock(block: ByteArray): ByteArray

    /**
     * Retrieves the next list sibling of this block.
     *
     * @param block current block
     * @return next sibling list block
     */
    protected abstract fun getListNextBlock(block: ByteArray): ByteArray?

    /**
     * Retrieves the offset of the start of the text block.
     *
     * @param block current block
     * @return text block start offset
     */
    protected abstract fun getTextOffset(block: ByteArray): Int

    /**
     * Retrieves the offset of the prompt text.
     *
     * @param block current block
     * @return prompt text offset
     */
    protected abstract fun getPromptOffset(block: ByteArray): Int

    /**
     * Retrieves a field type value.
     *
     * @param block criteria block
     * @return field type value
     */
    protected abstract fun getFieldType(block: ByteArray): FieldType

    /**
     * Main entry point to read criteria data.
     *
     * @param properties project properties
     * @param data criteria data block
     * @param dataOffset offset of the data start within the larger data block
     * @param entryOffset offset of start node for walking the tree
     * @param prompts optional list to hold prompts
     * @param fields optional list of hold fields
     * @param criteriaType optional array representing criteria types
     * @return first node of the criteria
     */
    fun process(properties: ProjectProperties, data: ByteArray, dataOffset: Int, entryOffset: Int, prompts: List<GenericCriteriaPrompt>, fields: List<FieldType>, criteriaType: BooleanArray): GenericCriteria? {
        var entryOffset = entryOffset
        m_properties = properties
        m_prompts = prompts
        m_fields = fields
        m_criteriaType = criteriaType
        m_dataOffset = dataOffset
        if (m_criteriaType != null) {
            m_criteriaType[0] = true
            m_criteriaType[1] = true
        }

        m_criteriaBlockMap.clear()

        m_criteriaData = data
        m_criteriaTextStart = MPPUtility.getShort(m_criteriaData, m_dataOffset + criteriaTextStartOffset)

        //
        // Populate the map
        //
        var criteriaStartOffset = criteriaStartOffset
        val criteriaBlockSize = criteriaBlockSize

        //System.out.println();
        //System.out.println(ByteArrayHelper.hexdump(data, dataOffset, criteriaStartOffset, false));

        if (m_criteriaData!!.size <= m_criteriaTextStart) {
            return null // bad data
        }

        while (criteriaStartOffset + criteriaBlockSize <= m_criteriaTextStart) {
            val block = ByteArray(criteriaBlockSize)
            System.arraycopy(m_criteriaData, m_dataOffset + criteriaStartOffset, block, 0, criteriaBlockSize)
            m_criteriaBlockMap.put(Integer.valueOf(criteriaStartOffset), block)
            //System.out.println(Integer.toHexString(criteriaStartOffset) + ": " + ByteArrayHelper.hexdump(block, false));
            criteriaStartOffset += criteriaBlockSize
        }

        if (entryOffset == -1) {
            entryOffset = criteriaStartOffset
        }

        val list = LinkedList<GenericCriteria>()
        processBlock(list, m_criteriaBlockMap[Integer.valueOf(entryOffset)])
        val criteria: GenericCriteria?
        if (list.isEmpty()) {
            criteria = null
        } else {
            criteria = list.get(0)
        }
        return criteria
    }

    /**
     * Process a single criteria block.
     *
     * @param list parent criteria list
     * @param block current block
     */
    private fun processBlock(list: List<GenericCriteria>, block: ByteArray?) {
        if (block != null) {
            if (MPPUtility.getShort(block, 0) > 0x3E6) {
                addCriteria(list, block)
            } else {
                when (block[0]) {
                    0x0B.toByte() -> {
                        processBlock(list, getChildBlock(block))
                    }

                    0x06.toByte() -> {
                        processBlock(list, getListNextBlock(block))
                    }

                    0xED.toByte() // EQUALS
                    -> {
                        addCriteria(list, block)
                    }

                    0x19.toByte() // AND
                        , 0x1B.toByte() -> {
                        addBlock(list, block, TestOperator.AND)
                    }

                    0x1A.toByte() // OR
                        , 0x1C.toByte() -> {
                        addBlock(list, block, TestOperator.OR)
                    }
                }
            }
        }
    }

    /**
     * Adds a basic LHS OPERATOR RHS block.
     *
     * @param list parent criteria list
     * @param block current block
     */
    private fun addCriteria(list: List<GenericCriteria>, block: ByteArray) {
        val leftBlock = getChildBlock(block)
        val rightBlock1 = getListNextBlock(leftBlock)
        val rightBlock2 = getListNextBlock(rightBlock1)
        val operator = TestOperator.getInstance(MPPUtility.getShort(block, 0) - 0x3E7)
        val leftValue = getFieldType(leftBlock)
        val rightValue1 = getValue(leftValue, rightBlock1!!)
        val rightValue2 = if (rightBlock2 == null) null else getValue(leftValue, rightBlock2)

        val criteria = GenericCriteria(m_properties)
        criteria.setLeftValue(leftValue)
        criteria.setOperator(operator)
        criteria.setRightValue(0, rightValue1)
        criteria.setRightValue(1, rightValue2)
        list.add(criteria)

        if (m_criteriaType != null) {
            m_criteriaType[0] = leftValue.getFieldTypeClass() === FieldTypeClass.TASK
            m_criteriaType[1] = !m_criteriaType!![0]
        }

        if (m_fields != null) {
            m_fields!!.add(leftValue)
        }

        processBlock(list, getListNextBlock(block))
    }

    /**
     * Adds a logical operator block.
     *
     * @param list parent criteria list
     * @param block current block
     * @param operator logical operator represented by this block
     */
    private fun addBlock(list: List<GenericCriteria>, block: ByteArray, operator: TestOperator) {
        val result = GenericCriteria(m_properties)
        result.setOperator(operator)
        list.add(result)
        processBlock(result.getCriteriaList(), getChildBlock(block))
        processBlock(list, getListNextBlock(block))
    }

    /**
     * Retrieves the value component of a criteria expression.
     *
     * @param field field type
     * @param block block data
     * @return field value
     */
    private fun getValue(field: FieldType, block: ByteArray): Object? {
        var result: Object? = null

        when (block[0]) {
            0x07 // Field
            -> {
                result = getFieldType(block)
            }

            0x01 // Constant value
            -> {
                result = getConstantValue(field, block)
            }

            0x00 // Prompt
            -> {
                result = getPromptValue(field, block)
            }
        }

        return result
    }

    /**
     * Retrieves a constant value.
     *
     * @param type field type
     * @param block criteria data block
     * @return constant value
     */
    private fun getConstantValue(type: FieldType, block: ByteArray): Object? {
        val value: Object?
        val dataType = type.getDataType()

        if (dataType == null) {
            value = null
        } else {
            when (dataType) {
                DURATION -> {
                    value = MPPUtility.getAdjustedDuration(m_properties, MPPUtility.getInt(block, valueOffset), MPPUtility.getDurationTimeUnits(MPPUtility.getShort(block, timeUnitsOffset)))
                }

                NUMERIC -> {
                    value = Double.valueOf(MPPUtility.getDouble(block, valueOffset))
                }

                PERCENTAGE -> {
                    value = Double.valueOf(MPPUtility.getShort(block, valueOffset))
                }

                CURRENCY -> {
                    value = Double.valueOf(MPPUtility.getDouble(block, valueOffset) / 100)
                }

                STRING -> {
                    val textOffset = getTextOffset(block)
                    value = MPPUtility.getUnicodeString(m_criteriaData, m_dataOffset + m_criteriaTextStart + textOffset)
                }

                BOOLEAN -> {
                    val intValue = MPPUtility.getShort(block, valueOffset)
                    value = if (intValue == 1) Boolean.TRUE else Boolean.FALSE
                }

                DATE -> {
                    value = MPPUtility.getTimestamp(block, valueOffset)
                }

                else -> {
                    value = null
                }
            }
        }

        return value
    }

    /**
     * Retrieves a prompt value.
     *
     * @param field field type
     * @param block criteria data block
     * @return prompt value
     */
    private fun getPromptValue(field: FieldType, block: ByteArray): GenericCriteriaPrompt {
        val textOffset = getPromptOffset(block)
        val value = MPPUtility.getUnicodeString(m_criteriaData, m_criteriaTextStart + textOffset)
        val prompt = GenericCriteriaPrompt(field.getDataType(), value)
        if (m_prompts != null) {
            m_prompts!!.add(prompt)
        }
        return prompt
    }
}
