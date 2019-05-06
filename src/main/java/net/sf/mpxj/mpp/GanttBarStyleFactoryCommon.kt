/*
 * file:       GanttarStyleFactoryCommon.java
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

import net.sf.mpxj.common.MPPTaskField

/**
 * Reads Gantt bar styles from a MPP9 and MPP12 files.
 */
class GanttBarStyleFactoryCommon : GanttBarStyleFactory {
    /**
     * {@inheritDoc}
     */
    @Override
    override fun processDefaultStyles(props: Props): Array<GanttBarStyle>? {
        var barStyles: Array<GanttBarStyle>? = null
        val barStyleData = props.getByteArray(DEFAULT_PROPERTIES)
        if (barStyleData != null) {
            barStyles = arrayOfNulls(barStyleData[812])
            var styleOffset = 840
            var nameOffset = styleOffset + barStyles.size * 58

            for (loop in barStyles.indices) {
                val styleName = MPPUtility.getUnicodeString(barStyleData, nameOffset)
                nameOffset += (styleName.length() + 1) * 2
                val style = GanttBarStyle()
                barStyles[loop] = style

                style.name = styleName

                style.middleShape = GanttBarMiddleShape.getInstance(barStyleData[styleOffset].toInt())
                style.middlePattern = ChartPattern.getInstance(barStyleData[styleOffset + 1].toInt())
                style.middleColor = ColorType.getInstance(barStyleData[styleOffset + 2].toInt()).color

                style.startShape = GanttBarStartEndShape.getInstance(barStyleData[styleOffset + 4] % 21)
                style.startType = GanttBarStartEndType.getInstance(barStyleData[styleOffset + 4] / 21)
                style.startColor = ColorType.getInstance(barStyleData[styleOffset + 5].toInt()).color

                style.endShape = GanttBarStartEndShape.getInstance(barStyleData[styleOffset + 6] % 21)
                style.endType = GanttBarStartEndType.getInstance(barStyleData[styleOffset + 6] / 21)
                style.endColor = ColorType.getInstance(barStyleData[styleOffset + 7].toInt()).color

                style.fromField = MPPTaskField.getInstance(MPPUtility.getShort(barStyleData, styleOffset + 8))
                style.toField = MPPTaskField.getInstance(MPPUtility.getShort(barStyleData, styleOffset + 12))

                extractFlags(style, GanttBarShowForTasks.NORMAL, MPPUtility.getLong6(barStyleData, styleOffset + 16))
                extractFlags(style, GanttBarShowForTasks.NOT_NORMAL, MPPUtility.getLong6(barStyleData, styleOffset + 24))

                style.row = barStyleData[styleOffset + 32] + 1

                style.leftText = MPPTaskField.getInstance(MPPUtility.getShort(barStyleData, styleOffset + 34))
                style.rightText = MPPTaskField.getInstance(MPPUtility.getShort(barStyleData, styleOffset + 38))
                style.topText = MPPTaskField.getInstance(MPPUtility.getShort(barStyleData, styleOffset + 42))
                style.bottomText = MPPTaskField.getInstance(MPPUtility.getShort(barStyleData, styleOffset + 46))
                style.insideText = MPPTaskField.getInstance(MPPUtility.getShort(barStyleData, styleOffset + 50))

                styleOffset += 58
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
            barStyle = arrayOfNulls(barData.size / 38)
            var offset = 0
            for (loop in barStyle.indices) {
                val style = GanttBarStyleException()
                barStyle[loop] = style

                //System.out.println("GanttBarStyleException");
                //System.out.println(ByteArrayHelper.hexdump(data, offset, 38, false));

                style.taskUniqueID = MPPUtility.getInt(barData, offset)
                style.barStyleIndex = MPPUtility.getShort(barData, offset + 4) - 1

                style.startShape = GanttBarStartEndShape.getInstance(barData[offset + 9] % 21)
                style.startType = GanttBarStartEndType.getInstance(barData[offset + 9] / 21)
                style.startColor = ColorType.getInstance(barData[offset + 10].toInt()).color

                style.middleShape = GanttBarMiddleShape.getInstance(barData[offset + 6].toInt())
                style.middlePattern = ChartPattern.getInstance(barData[offset + 7].toInt())

                style.middleColor = ColorType.getInstance(barData[offset + 8].toInt()).color

                style.endShape = GanttBarStartEndShape.getInstance(barData[offset + 11] % 21)
                style.endType = GanttBarStartEndType.getInstance(barData[offset + 11] / 21)
                style.endColor = ColorType.getInstance(barData[offset + 12].toInt()).color

                style.leftText = MPPTaskField.getInstance(MPPUtility.getShort(barData, offset + 16))
                style.rightText = MPPTaskField.getInstance(MPPUtility.getShort(barData, offset + 20))
                style.topText = MPPTaskField.getInstance(MPPUtility.getShort(barData, offset + 24))
                style.bottomText = MPPTaskField.getInstance(MPPUtility.getShort(barData, offset + 28))
                style.insideText = MPPTaskField.getInstance(MPPUtility.getShort(barData, offset + 32))

                offset += 38
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

        while (index < 48) {
            if (flagValue and flag != 0L) {
                val enumValue = GanttBarShowForTasks.getInstance(baseCriteria.value + index)

                style.addShowForTasks(enumValue)
            }

            flag = flag shl 1

            index++
        }
    }

    companion object {

        private val DEFAULT_PROPERTIES = Integer.valueOf(574619686)
        private val EXCEPTION_PROPERTIES = Integer.valueOf(574619661)
    }
}
