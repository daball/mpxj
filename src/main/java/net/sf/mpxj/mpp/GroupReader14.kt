/*
 * file:       GroupReader14.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       31/03/2010
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

import java.awt.Color

import net.sf.mpxj.FieldType
import net.sf.mpxj.Group
import net.sf.mpxj.GroupClause
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.common.FieldTypeHelper

/**
 * This class allows filter definitions to be read from an MPP file.
 */
class GroupReader14 {
    /**
     * Entry point for processing group definitions.
     *
     * @param file project file
     * @param fixedData group fixed data
     * @param varData group var data
     * @param fontBases map of font bases
     */
    fun process(file: ProjectFile, fixedData: FixedData, varData: Var2Data, fontBases: Map<Integer, FontBase>) {
        val groupCount = fixedData.itemCount
        for (groupLoop in 0 until groupCount) {
            val groupFixedData = fixedData.getByteArrayValue(groupLoop)
            if (groupFixedData == null || groupFixedData.size < 4) {
                continue
            }

            val groupID = Integer.valueOf(MPPUtility.getInt(groupFixedData, 0))

            val groupVarData = varData.getByteArray(groupID, GROUP_DATA) ?: continue

            val groupName = MPPUtility.getUnicodeString(groupFixedData, 4)

            // 8 byte header, 48 byte blocks for each clause
            //System.out.println(ByteArrayHelper.hexdump(groupVarData, true, 16, ""));

            // header=4 byte int for unique id
            // short 4 = show summary tasks
            // short int at byte 6 for number of clauses
            //Integer groupUniqueID = Integer.valueOf(MPPUtility.getInt(groupVarData, 0));
            val showSummaryTasks = MPPUtility.getShort(groupVarData, 4) != 0

            val group = Group(groupID, groupName, showSummaryTasks)
            file.groups.add(group)

            val clauseCount = MPPUtility.getShort(groupVarData, 10)
            var offset = 12

            for (clauseIndex in 0 until clauseCount) {
                if (offset + 71 > groupVarData.size) {
                    break
                }

                //System.out.println("Clause " + clauseIndex);
                //System.out.println(ByteArrayHelper.hexdump(groupVarData, offset, 71, false, 16, ""));

                val clause = GroupClause()
                group.addGroupClause(clause)

                val fieldID = MPPUtility.getInt(groupVarData, offset)
                val type = FieldTypeHelper.getInstance14(fieldID)
                clause.field = type

                // from byte 0 2 byte short int - field type
                // byte 3 - entity type 0b/0c
                // 4th byte in clause is 1=asc 0=desc
                // offset+8=font index, from font bases
                // offset+12=color, byte
                // offset+13=pattern, byte

                val ascending = MPPUtility.getByte(groupVarData, offset + 4) != 0
                clause.ascending = ascending

                val fontIndex = MPPUtility.getByte(groupVarData, offset + 8)
                val fontBase = fontBases[Integer.valueOf(fontIndex)]

                val style = MPPUtility.getByte(groupVarData, offset + 9)
                val bold = style and 0x01 != 0
                val italic = style and 0x02 != 0
                val underline = style and 0x04 != 0
                val fontColor = MPPUtility.getColor(groupVarData, offset + 10)

                val fontStyle = FontStyle(fontBase, italic, bold, underline, false, fontColor, null, BackgroundPattern.SOLID)
                clause.font = fontStyle
                clause.cellBackgroundColor = MPPUtility.getColor(groupVarData, offset + 22)
                clause.pattern = BackgroundPattern.getInstance(MPPUtility.getByte(groupVarData, offset + 34) and 0x0F)

                // offset+14=group on
                val groupOn = MPPUtility.getByte(groupVarData, offset + 38)
                clause.groupOn = groupOn
                // offset+24=start at
                // offset+40=group interval

                var startAt: Object? = null
                var groupInterval: Object? = null
                if (type.getDataType() != null) {
                    when (type.getDataType()) {
                        DURATION, NUMERIC, CURRENCY -> {
                            startAt = Double.valueOf(MPPUtility.getDouble(groupVarData, offset + 47))
                            groupInterval = Double.valueOf(MPPUtility.getDouble(groupVarData, offset + 63))
                        }

                        PERCENTAGE -> {
                            startAt = Integer.valueOf(MPPUtility.getInt(groupVarData, offset + 47))
                            groupInterval = Integer.valueOf(MPPUtility.getInt(groupVarData, offset + 63))
                        }

                        BOOLEAN -> {
                            startAt = if (MPPUtility.getShort(groupVarData, offset + 47) == 1) Boolean.TRUE else Boolean.FALSE
                        }

                        DATE -> {
                            startAt = MPPUtility.getTimestamp(groupVarData, offset + 47)
                            groupInterval = Integer.valueOf(MPPUtility.getInt(groupVarData, offset + 63))
                        }

                        else -> {
                        }
                    }
                }

                clause.startAt = startAt
                clause.groupInterval = groupInterval

                offset += 71
            }

            //System.out.println(group);
        }
    }

    companion object {

        private val GROUP_DATA = Integer.valueOf(6)
    }
}
