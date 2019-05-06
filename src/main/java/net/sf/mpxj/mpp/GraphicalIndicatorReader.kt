/*
 * file:       GraphicalIndicatorReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       16-Feb-2006
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

import java.util.Date

import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.Duration
import net.sf.mpxj.FieldType
import net.sf.mpxj.GraphicalIndicator
import net.sf.mpxj.GraphicalIndicatorCriteria
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.TestOperator
import net.sf.mpxj.common.FieldTypeHelper

/**
 * This class allows graphical indicator definitions to be read from an MPP
 * file.
 */
class GraphicalIndicatorReader {

    private var m_data: ByteArray? = null
    private var m_headerOffset: Int = 0
    private var m_dataOffset: Int = 0
    private var m_container: CustomFieldContainer? = null
    private var m_properties: ProjectProperties? = null
    /**
     * The main entry point for processing graphical indicator definitions.
     *
     * @param indicators graphical indicators container
     * @param properties project properties
     * @param props properties data
     */
    fun process(indicators: CustomFieldContainer, properties: ProjectProperties, props: Props) {
        m_container = indicators
        m_properties = properties
        m_data = props.getByteArray(Props.TASK_FIELD_ATTRIBUTES)

        if (m_data != null) {
            val columnsCount = MPPUtility.getInt(m_data, 4)
            m_headerOffset = 8
            for (loop in 0 until columnsCount) {
                processColumns()
            }
        }
    }

    /**
     * Processes graphical indicator definitions for each column.
     */
    private fun processColumns() {
        val fieldID = MPPUtility.getInt(m_data, m_headerOffset)
        m_headerOffset += 4

        m_dataOffset = MPPUtility.getInt(m_data, m_headerOffset)
        m_headerOffset += 4

        val type = FieldTypeHelper.getInstance(fieldID)
        if (type.getDataType() != null) {
            processKnownType(type)
        }
    }

    /**
     * Process a graphical indicator definition for a known type.
     *
     * @param type field type
     */
    private fun processKnownType(type: FieldType) {
        //System.out.println("Header: " + type);
        //System.out.println(ByteArrayHelper.hexdump(m_data, m_dataOffset, 36, false, 16, ""));

        val indicator = m_container!!.getCustomField(type).getGraphicalIndicator()
        indicator.fieldType = type
        val flags = m_data!![m_dataOffset].toInt()
        indicator.projectSummaryInheritsFromSummaryRows = flags and 0x08 != 0
        indicator.summaryRowsInheritFromNonSummaryRows = flags and 0x04 != 0
        indicator.displayGraphicalIndicators = flags and 0x02 != 0
        indicator.showDataValuesInToolTips = flags and 0x01 != 0
        m_dataOffset += 20

        val nonSummaryRowOffset = MPPUtility.getInt(m_data, m_dataOffset) - 36
        m_dataOffset += 4

        val summaryRowOffset = MPPUtility.getInt(m_data, m_dataOffset) - 36
        m_dataOffset += 4

        val projectSummaryOffset = MPPUtility.getInt(m_data, m_dataOffset) - 36
        m_dataOffset += 4

        val dataSize = MPPUtility.getInt(m_data, m_dataOffset) - 36
        m_dataOffset += 4

        //System.out.println("Data");
        //System.out.println(ByteArrayHelper.hexdump(m_data, m_dataOffset, dataSize, false, 16, ""));

        val maxNonSummaryRowOffset = m_dataOffset + summaryRowOffset
        val maxSummaryRowOffset = m_dataOffset + projectSummaryOffset
        val maxProjectSummaryOffset = m_dataOffset + dataSize

        m_dataOffset += nonSummaryRowOffset

        while (m_dataOffset + 2 < maxNonSummaryRowOffset) {
            indicator.addNonSummaryRowCriteria(processCriteria(type))
        }

        while (m_dataOffset + 2 < maxSummaryRowOffset) {
            indicator.addSummaryRowCriteria(processCriteria(type))
        }

        while (m_dataOffset + 2 < maxProjectSummaryOffset) {
            indicator.addProjectSummaryCriteria(processCriteria(type))
        }
    }

    /**
     * Process the graphical indicator criteria for a single column.
     *
     * @param type field type
     * @return indicator criteria data
     */
    private fun processCriteria(type: FieldType): GraphicalIndicatorCriteria {
        val criteria = GraphicalIndicatorCriteria(m_properties)
        criteria.setLeftValue(type)

        val indicatorType = MPPUtility.getInt(m_data, m_dataOffset)
        m_dataOffset += 4
        criteria.indicator = indicatorType

        if (m_dataOffset + 4 < m_data!!.size) {
            val operatorValue = MPPUtility.getInt(m_data, m_dataOffset)
            m_dataOffset += 4
            val operator = if (operatorValue == 0) TestOperator.IS_ANY_VALUE else TestOperator.getInstance(operatorValue - 0x3E7)
            criteria.setOperator(operator)

            if (operator !== TestOperator.IS_ANY_VALUE) {
                processOperandValue(0, type, criteria)

                if (operator === TestOperator.IS_WITHIN || operator === TestOperator.IS_NOT_WITHIN) {
                    processOperandValue(1, type, criteria)
                }
            }
        }

        return criteria
    }

    /**
     * Process an operand value used in the definition of the graphical
     * indicator criteria.
     *
     * @param index position in operand list
     * @param type field type
     * @param criteria indicator criteria
     */
    private fun processOperandValue(index: Int, type: FieldType, criteria: GraphicalIndicatorCriteria) {
        val valueFlag = MPPUtility.getInt(m_data, m_dataOffset) == 1
        m_dataOffset += 4

        if (valueFlag == false) {
            val fieldID = MPPUtility.getInt(m_data, m_dataOffset)
            criteria.setRightValue(index, FieldTypeHelper.getInstance(fieldID))
            m_dataOffset += 4
        } else {
            //int dataTypeValue = MPPUtility.getShort(m_data, m_dataOffset);
            m_dataOffset += 2

            when (type.getDataType()) {
                DURATION // 0x03
                -> {
                    val value = MPPUtility.getAdjustedDuration(m_properties, MPPUtility.getInt(m_data, m_dataOffset), MPPUtility.getDurationTimeUnits(MPPUtility.getShort(m_data, m_dataOffset + 4)))
                    m_dataOffset += 6
                    criteria.setRightValue(index, value)
                }

                NUMERIC // 0x05
                -> {
                    val value = Double.valueOf(MPPUtility.getDouble(m_data, m_dataOffset))
                    m_dataOffset += 8
                    criteria.setRightValue(index, value)
                }

                CURRENCY // 0x06
                -> {
                    val value = Double.valueOf(MPPUtility.getDouble(m_data, m_dataOffset) / 100)
                    m_dataOffset += 8
                    criteria.setRightValue(index, value)
                }

                STRING // 0x08
                -> {
                    val value = MPPUtility.getUnicodeString(m_data, m_dataOffset)
                    m_dataOffset += (value.length() + 1) * 2
                    criteria.setRightValue(index, value)
                }

                BOOLEAN // 0x0B
                -> {
                    val value = MPPUtility.getShort(m_data, m_dataOffset)
                    m_dataOffset += 2
                    criteria.setRightValue(index, if (value == 1) Boolean.TRUE else Boolean.FALSE)
                }

                DATE // 0x13
                -> {
                    val value = MPPUtility.getTimestamp(m_data, m_dataOffset)
                    m_dataOffset += 4
                    criteria.setRightValue(index, value)
                }

                else -> {
                }
            }
        }
    }
}
