/*
 * file:       GanttarStyleFactory14.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       19/04/2010
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

import net.sf.mpxj.TaskField
import net.sf.mpxj.common.MPPTaskField14

/**
 * Reads Gantt bar styles from an MPP14 file.
 */
class GanttBarStyleFactory14 : GanttBarStyleFactory {
    /**
     * {@inheritDoc}
     */
    @Override
    override fun processDefaultStyles(props: Props): Array<GanttBarStyle>? {
        var barStyles: Array<GanttBarStyle>? = null
        val barStyleData = props.getByteArray(DEFAULT_PROPERTIES)
        if (barStyleData != null && barStyleData.size > 2240) {
            val barStyleCount = MPPUtility.getByte(barStyleData, 2243)
            if (barStyleCount > 0 && barStyleCount < 65535) {
                barStyles = arrayOfNulls(barStyleCount)
                var styleOffset = 2255

                for (loop in 0 until barStyleCount) {
                    val style = GanttBarStyle()
                    barStyles[loop] = style

                    style.name = MPPUtility.getUnicodeString(barStyleData, styleOffset + 91)

                    style.leftText = getTaskField(MPPUtility.getShort(barStyleData, styleOffset + 67))
                    style.rightText = getTaskField(MPPUtility.getShort(barStyleData, styleOffset + 71))
                    style.topText = getTaskField(MPPUtility.getShort(barStyleData, styleOffset + 75))
                    style.bottomText = getTaskField(MPPUtility.getShort(barStyleData, styleOffset + 79))
                    style.insideText = getTaskField(MPPUtility.getShort(barStyleData, styleOffset + 83))

                    style.startShape = GanttBarStartEndShape.getInstance(barStyleData[styleOffset + 15] % 25)
                    style.startType = GanttBarStartEndType.getInstance(barStyleData[styleOffset + 15] / 25)
                    style.startColor = MPPUtility.getColor(barStyleData, styleOffset + 16)

                    style.middleShape = GanttBarMiddleShape.getInstance(barStyleData[styleOffset].toInt())
                    style.middlePattern = ChartPattern.getInstance(barStyleData[styleOffset + 1].toInt())
                    style.middleColor = MPPUtility.getColor(barStyleData, styleOffset + 2)

                    style.endShape = GanttBarStartEndShape.getInstance(barStyleData[styleOffset + 28] % 25)
                    style.endType = GanttBarStartEndType.getInstance(barStyleData[styleOffset + 28] / 25)
                    style.endColor = MPPUtility.getColor(barStyleData, styleOffset + 29)

                    style.fromField = getTaskField(MPPUtility.getShort(barStyleData, styleOffset + 41))
                    style.toField = getTaskField(MPPUtility.getShort(barStyleData, styleOffset + 45))

                    extractFlags(style, GanttBarShowForTasks.NORMAL, MPPUtility.getLong(barStyleData, styleOffset + 49))
                    extractFlags(style, GanttBarShowForTasks.NOT_NORMAL, MPPUtility.getLong(barStyleData, styleOffset + 57))

                    style.row = MPPUtility.getShort(barStyleData, styleOffset + 65) + 1

                    styleOffset += 195
                }
            }
        }
        return barStyles
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun processExceptionStyles(props: Props): Array<GanttBarStyleException>? {
        var barStyle: Array<GanttBarStyleException>? = null
        val barData = props.getByteArray(EXCEPTION_PROPERTIES)
        if (barData != null) {
            //System.out.println(ByteArrayHelper.hexdump(barData, false, 71, ""));

            barStyle = arrayOfNulls(barData.size / 71)
            var offset = 0
            for (loop in barStyle.indices) {
                val style = GanttBarStyleException()
                barStyle[loop] = style

                style.taskUniqueID = MPPUtility.getInt(barData, offset)
                style.barStyleIndex = MPPUtility.getShort(barData, offset + 4) - 1

                style.startShape = GanttBarStartEndShape.getInstance(barData[offset + 20] % 25)
                style.startType = GanttBarStartEndType.getInstance(barData[offset + 20] / 25)
                style.startColor = MPPUtility.getColor(barData, offset + 21)

                style.middleShape = GanttBarMiddleShape.getInstance(barData[offset + 6].toInt())
                style.middlePattern = ChartPattern.getInstance(barData[offset + 7].toInt())
                style.middleColor = MPPUtility.getColor(barData, offset + 8)

                style.endShape = GanttBarStartEndShape.getInstance(barData[offset + 33] % 25)
                style.endType = GanttBarStartEndType.getInstance(barData[offset + 33] / 25)
                style.endColor = MPPUtility.getColor(barData, offset + 34)

                style.leftText = getTaskField(MPPUtility.getShort(barData, offset + 49))
                style.rightText = getTaskField(MPPUtility.getShort(barData, offset + 53))
                style.topText = getTaskField(MPPUtility.getShort(barData, offset + 57))
                style.bottomText = getTaskField(MPPUtility.getShort(barData, offset + 61))
                style.insideText = getTaskField(MPPUtility.getShort(barData, offset + 65))

                //System.out.println(style);
                offset += 71
            }
        }
        return barStyle
    }

    /**
     * Extract the flags indicating which task types this bar style
     * is relevant for. Note that this work for the "normal" task types
     * and the "negated" task types (e.g. Normal Task, Not Normal task).
     * The set of values used is determined by the baseCriteria argument.
     *
     * @param style parent bar style
     * @param baseCriteria determines if the normal or negated enums are used
     * @param flagValue flag data
     */
    private fun extractFlags(style: GanttBarStyle, baseCriteria: GanttBarShowForTasks, flagValue: Long) {
        var index = 0
        var flag: Long = 0x0001

        while (index < 64) {
            if (flagValue and flag != 0L) {
                val enumValue = GanttBarShowForTasks.getInstance(baseCriteria.value + index)
                if (enumValue != null) {
                    style.addShowForTasks(enumValue)
                }
            }

            flag = flag shl 1

            index++
        }
    }

    /**
     * Maps an integer field ID to a field type.
     *
     * @param field field ID
     * @return field type
     */
    private fun getTaskField(field: Int): TaskField? {
        var result = MPPTaskField14.getInstance(field)

        if (result != null) {
            when (result) {
                START_TEXT -> {
                    result = TaskField.START
                }

                FINISH_TEXT -> {
                    result = TaskField.FINISH
                }

                DURATION_TEXT -> {
                    result = TaskField.DURATION
                }

                else -> {
                }
            }
        }

        return result
    }

    companion object {

        private val DEFAULT_PROPERTIES = Integer.valueOf(574619656)
        private val EXCEPTION_PROPERTIES = Integer.valueOf(574619661)
    }
}
