/*
 * file:       TableFactory14.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       08/03/2010
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

import net.sf.mpxj.Column
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Table
import net.sf.mpxj.common.MPPResourceField14
import net.sf.mpxj.common.MPPTaskField14

/**
 * This interface is implemented by classes which can create Table classes
 * from the data extracted from an MS Project file.
 */
internal class TableFactory14
/**
 * Constructor.
 *
 * @param tableColumnDataStandard standard columns data key
 * @param tableColumnDataEnterprise enterprise columns data key
 * @param tableColumnDataBaseline baseline columns data key
 */
(private val m_tableColumnDataStandard: Integer, private val m_tableColumnDataEnterprise: Integer, private val m_tableColumnDataBaseline: Integer?) {

    /**
     * Creates a new Table instance from data extracted from an MPP file.
     *
     * @param file parent project file
     * @param data fixed data
     * @param varMeta var meta
     * @param varData var data
     * @return Table instance
     */
    fun createTable(file: ProjectFile, data: ByteArray, varMeta: VarMeta, varData: Var2Data): Table {
        val table = Table()

        table.id = MPPUtility.getInt(data, 0)
        table.resourceFlag = MPPUtility.getShort(data, 108) == 1
        table.name = MPPUtility.removeAmpersands(MPPUtility.getUnicodeString(data, 4))

        var columnData: ByteArray? = null
        val tableID = Integer.valueOf(table.id)
        if (m_tableColumnDataBaseline != null) {
            columnData = varData.getByteArray(varMeta.getOffset(tableID, m_tableColumnDataBaseline))
        }

        if (columnData == null) {
            columnData = varData.getByteArray(varMeta.getOffset(tableID, m_tableColumnDataEnterprise))
            if (columnData == null) {
                columnData = varData.getByteArray(varMeta.getOffset(tableID, m_tableColumnDataStandard))
            }
        }

        processColumnData(file, table, columnData)

        //System.out.println(table);

        return table
    }

    /**
     * Adds columns to a Table instance.
     *
     * @param file parent project file
     * @param table parent table instance
     * @param data column data
     */
    private fun processColumnData(file: ProjectFile, table: Table, data: ByteArray?) {
        //System.out.println("Table=" + table);
        //System.out.println(ByteArrayHelper.hexdump(data, 12, data.length-12, true, 115, ""));

        if (data != null) {
            val columnCount = MPPUtility.getShort(data, 4) + 1
            var index = 12
            var column: Column
            var alignment: Int

            for (loop in 0 until columnCount) {
                column = Column(file)
                val fieldType = MPPUtility.getShort(data, index)
                if (table.resourceFlag == false) {
                    column.setFieldType(MPPTaskField14.getInstance(fieldType))
                } else {
                    column.setFieldType(MPPResourceField14.getInstance(fieldType))
                }

                //System.out.println(fieldType);

                //            if (column.getFieldType() == null)
                //            {
                //               System.out.println(loop + ": Unknown column type " + fieldType);
                //            }
                //            else
                //            {
                //               System.out.println(loop + ": " + column.getFieldType());
                //            }

                column.setWidth(MPPUtility.getByte(data, index + 4))

                val columnTitle = MPPUtility.getUnicodeString(data, index + 13)
                if (columnTitle.length() !== 0) {
                    column.setTitle(columnTitle)
                }

                alignment = MPPUtility.getByte(data, index + 5)
                if (alignment and 0x0F == 0x00) {
                    column.setAlignTitle(Column.ALIGN_LEFT)
                } else {
                    if (alignment and 0x0F == 0x01) {
                        column.setAlignTitle(Column.ALIGN_CENTER)
                    } else {
                        column.setAlignTitle(Column.ALIGN_RIGHT)
                    }
                }

                alignment = MPPUtility.getByte(data, index + 7)
                if (alignment and 0x0F == 0x00) {
                    column.setAlignData(Column.ALIGN_LEFT)
                } else {
                    if (alignment and 0x0F == 0x01) {
                        column.setAlignData(Column.ALIGN_CENTER)
                    } else {
                        column.setAlignData(Column.ALIGN_RIGHT)
                    }
                }

                table.addColumn(column)
                index += 115
            }
        }
    }
}
