/*
 * file:       GanttChartView14.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       16/04/2010
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
import java.io.IOException
import java.util.Date

import net.sf.mpxj.Day
import net.sf.mpxj.FieldType
import net.sf.mpxj.Filter
import net.sf.mpxj.GenericCriteria
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.common.FieldTypeHelper
import net.sf.mpxj.common.MPPTaskField14

/**
 * This class represents the set of properties used to define the appearance
 * of a Gantt chart view in MS Project.
 */
class GanttChartView14
/**
 * Create a GanttChartView from the fixed and var data blocks associated
 * with a view.
 *
 * @param parent parent MPP file
 * @param fixedMeta fixed meta data block
 * @param fixedData fixed data block
 * @param varData var data block
 * @param fontBases map of font bases
 * @throws IOException
 */
@Throws(IOException::class)
internal constructor(parent: ProjectFile, fixedMeta: ByteArray, fixedData: ByteArray, varData: Var2Data, fontBases: Map<Integer, FontBase>) : GanttChartView(parent, fixedMeta, fixedData, varData, fontBases) {
    /**
     * {@inheritDoc}
     */
    protected override val propertiesID: Integer
        @Override get() = PROPERTIES

    /**
     * {@inheritDoc}
     */
    @Override
    override fun processDefaultBarStyles(props: Props) {
        val f = GanttBarStyleFactory14()
        barStyles = f.processDefaultStyles(props)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun processExceptionBarStyles(props: Props) {
        val f = GanttBarStyleFactory14()
        barStyleExceptions = f.processExceptionStyles(props)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun processAutoFilters(data: ByteArray) {
        //System.out.println(ByteArrayHelper.hexdump(data, true, 16, ""));

        //
        // 16 byte block header containing the filter count
        //
        val filterCount = MPPUtility.getShort(data, 8)
        var offset = 16
        val criteria = FilterCriteriaReader14()

        //
        // 16 byte header
        // followed by 4 bytes = field type
        // followed by 2 byte block size
        for (loop in 0 until filterCount) {
            val field = getFieldType(data, offset)
            val blockSize = MPPUtility.getShort(data, offset + 4)

            //
            // Steelray 12335: the block size may be zero
            //
            if (blockSize == 0) {
                break
            }

            //System.out.println(ByteArrayHelper.hexdump(data, offset, 32, false));

            // may need to sort this out
            val c = criteria.process(m_properties, data, offset + 12, -1, null, null, null)
            //System.out.println(c);

            val filter = Filter()
            filter.setCriteria(c)
            m_autoFilters.add(filter)
            m_autoFiltersByType.put(field, filter)

            //
            // Move to the next filter
            //
            offset += blockSize
        }
    }

    /**
     * Retrieves a field type from a location in a data block.
     *
     * @param data data block
     * @param offset offset into data block
     * @return field type
     */
    private fun getFieldType(data: ByteArray, offset: Int): FieldType? {
        val fieldIndex = MPPUtility.getInt(data, offset)
        return FieldTypeHelper.mapTextFields(FieldTypeHelper.getInstance14(fieldIndex))
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected override fun processViewProperties(fontBases: Map<Integer, FontBase>, props: Props) {
        val viewPropertyData = props.getByteArray(GanttChartView.VIEW_PROPERTIES)
        if (viewPropertyData != null && viewPropertyData.size > 41360) {
            //MPPUtility.fileDump("c:\\temp\\props.txt", ByteArrayHelper.hexdump(viewPropertyData, false, 16, "").getBytes());

            m_highlightedTasksFontStyle = getFontStyle(viewPropertyData, 26, fontBases, false)
            m_rowAndColumnFontStyle = getFontStyle(viewPropertyData, 58, fontBases, false)
            m_nonCriticalTasksFontStyle = getFontStyle(viewPropertyData, 90, fontBases, false)
            m_criticalTasksFontStyle = getFontStyle(viewPropertyData, 122, fontBases, false)
            m_summaryTasksFontStyle = getFontStyle(viewPropertyData, 154, fontBases, false)
            m_milestoneTasksFontStyle = getFontStyle(viewPropertyData, 186, fontBases, false)
            m_middleTimescaleFontStyle = getFontStyle(viewPropertyData, 218, fontBases, false)
            m_bottomTimescaleFontStyle = getFontStyle(viewPropertyData, 250, fontBases, false)
            m_barTextLeftFontStyle = getFontStyle(viewPropertyData, 282, fontBases, false)
            m_barTextRightFontStyle = getFontStyle(viewPropertyData, 314, fontBases, false)
            m_barTextTopFontStyle = getFontStyle(viewPropertyData, 346, fontBases, false)
            m_barTextBottomFontStyle = getFontStyle(viewPropertyData, 378, fontBases, false)
            m_barTextInsideFontStyle = getFontStyle(viewPropertyData, 410, fontBases, false)
            m_markedTasksFontStyle = getFontStyle(viewPropertyData, 442, fontBases, false)
            m_projectSummaryTasksFontStyle = getFontStyle(viewPropertyData, 474, fontBases, false)
            m_externalTasksFontStyle = getFontStyle(viewPropertyData, 506, fontBases, false)
            m_topTimescaleFontStyle = getFontStyle(viewPropertyData, 538, fontBases, false)

            m_sheetRowsGridLines = getGridLines(viewPropertyData, 667)
            m_sheetColumnsGridLines = getGridLines(viewPropertyData, 697)
            m_titleVerticalGridLines = getGridLines(viewPropertyData, 727)
            m_titleHorizontalGridLines = getGridLines(viewPropertyData, 757)
            m_middleTierColumnGridLines = getGridLines(viewPropertyData, 787)
            m_bottomTierColumnGridLines = getGridLines(viewPropertyData, 817)
            m_ganttRowsGridLines = getGridLines(viewPropertyData, 847)
            m_barRowsGridLines = getGridLines(viewPropertyData, 877)
            m_currentDateGridLines = getGridLines(viewPropertyData, 907)
            m_pageBreakGridLines = getGridLines(viewPropertyData, 937)
            m_projectStartGridLines = getGridLines(viewPropertyData, 967)
            m_projectFinishGridLines = getGridLines(viewPropertyData, 997)
            m_statusDateGridLines = getGridLines(viewPropertyData, 1027)
            m_topTierColumnGridLines = getGridLines(viewPropertyData, 1057)

            m_nonWorkingDaysCalendarName = MPPUtility.getUnicodeString(viewPropertyData, 1422)
            m_nonWorkingColor = MPPUtility.getColor(viewPropertyData, 2223)
            m_nonWorkingPattern = ChartPattern.getInstance(viewPropertyData[2235].toInt())
            m_nonWorkingStyle = NonWorkingTimeStyle.getInstance(viewPropertyData[2222].toInt())

            timescaleShowTiers = viewPropertyData[41255].toInt()
            m_timescaleSize = viewPropertyData[1180].toInt()

            val flags = viewPropertyData[1086].toInt()
            m_timescaleScaleSeparator = flags and 0x04 != 0

            m_timescaleTopTier = TimescaleTier()

            m_timescaleTopTier!!.tickLines = viewPropertyData[41349].toInt() != 0
            m_timescaleTopTier!!.usesFiscalYear = viewPropertyData[41361] and 0x01 != 0
            m_timescaleTopTier!!.units = TimescaleUnits.getInstance(viewPropertyData[41311].toInt())
            m_timescaleTopTier!!.count = viewPropertyData[41313]
            m_timescaleTopTier!!.format = TimescaleFormat.getInstance(MPPUtility.getShort(viewPropertyData, 41315))
            m_timescaleTopTier!!.alignment = TimescaleAlignment.getInstance(viewPropertyData[41317].toInt())

            m_timescaleMiddleTier = TimescaleTier()
            m_timescaleMiddleTier!!.tickLines = flags and 0x01 != 0
            m_timescaleMiddleTier!!.usesFiscalYear = flags and 0x08 != 0
            m_timescaleMiddleTier!!.units = TimescaleUnits.getInstance(viewPropertyData[1152].toInt())
            m_timescaleMiddleTier!!.count = viewPropertyData[1156]
            m_timescaleMiddleTier!!.format = TimescaleFormat.getInstance(MPPUtility.getShort(viewPropertyData, 1160))
            m_timescaleMiddleTier!!.alignment = TimescaleAlignment.getInstance(viewPropertyData[1166].toInt())

            m_timescaleBottomTier = TimescaleTier()
            m_timescaleBottomTier!!.tickLines = flags and 0x02 != 0
            m_timescaleBottomTier!!.usesFiscalYear = flags and 0x10 != 0
            m_timescaleBottomTier!!.units = TimescaleUnits.getInstance(viewPropertyData[1154].toInt())
            m_timescaleBottomTier!!.count = viewPropertyData[1158]
            m_timescaleBottomTier!!.format = TimescaleFormat.getInstance(MPPUtility.getShort(viewPropertyData, 1162))
            m_timescaleBottomTier!!.alignment = TimescaleAlignment.getInstance(viewPropertyData[1164].toInt())

            m_showDrawings = viewPropertyData[2237].toInt() != 0
            m_roundBarsToWholeDays = viewPropertyData[2239].toInt() != 0
            m_showBarSplits = viewPropertyData[2241].toInt() != 0
            m_alwaysRollupGanttBars = viewPropertyData[2251].toInt() != 0
            m_hideRollupBarsWhenSummaryExpanded = viewPropertyData[2253].toInt() != 0
            m_ganttBarHeight = mapGanttBarHeight(MPPUtility.getByte(viewPropertyData, 2244))

            m_barDateFormat = GanttBarDateFormat.getInstance(viewPropertyData[2247] + 1)
            m_linkStyle = LinkStyle.getInstance(viewPropertyData[2236].toInt())
        }
    }

    /**
     * Creates a new GridLines instance.
     *
     * @param data data block
     * @param offset offset into data block
     * @return new GridLines instance
     */
    private fun getGridLines(data: ByteArray, offset: Int): GridLines {
        //System.out.println(offset+ ": " + ByteArrayHelper.hexdump(data, offset, 30, false));
        val normalLineColor = MPPUtility.getColor(data, offset)
        val normalLineStyle = LineStyle.getInstance(data[offset + 13].toInt())
        val intervalNumber = data[offset + 14].toInt()
        val intervalLineStyle = LineStyle.getInstance(data[offset + 15].toInt())
        val intervalLineColor = MPPUtility.getColor(data, offset + 16)
        return GridLines(normalLineColor, normalLineStyle, intervalNumber, intervalLineStyle, intervalLineColor)
    }

    /**
     * Retrieve font details from a block of property data.
     *
     * @param data property data
     * @param offset offset into property data
     * @param fontBases map of font bases
     * @param ignoreBackground set background to default values
     * @return FontStyle instance
     */
    protected fun getFontStyle(data: ByteArray, offset: Int, fontBases: Map<Integer, FontBase>, ignoreBackground: Boolean): FontStyle {
        //System.out.println(ByteArrayHelper.hexdump(data, offset, 32, false));

        val index = Integer.valueOf(MPPUtility.getByte(data, offset))
        val fontBase = fontBases[index]
        val style = MPPUtility.getByte(data, offset + 3)
        val color = MPPUtility.getColor(data, offset + 4)
        val backgroundColor: Color?
        val backgroundPattern: BackgroundPattern

        if (ignoreBackground) {
            backgroundColor = null
            backgroundPattern = BackgroundPattern.SOLID
        } else {
            backgroundColor = MPPUtility.getColor(data, offset + 16)
            backgroundPattern = BackgroundPattern.getInstance(MPPUtility.getShort(data, offset + 28))
        }

        val bold = style and 0x01 != 0
        val italic = style and 0x02 != 0
        val underline = style and 0x04 != 0
        val strikethrough = style and 0x08 != 0

//System.out.println(fontStyle);
        return FontStyle(fontBase, italic, bold, underline, strikethrough, color, backgroundColor, backgroundPattern)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected override fun processTableFontStyles(fontBases: Map<Integer, FontBase>, columnData: ByteArray) {
        //MPPUtility.fileDump("c:\\temp\\props.txt", ByteArrayHelper.hexdump(columnData, false, 44, "").getBytes());

        m_tableFontStyles = arrayOfNulls(columnData.size / 44)
        var offset = 0
        for (loop in m_tableFontStyles!!.indices) {
            m_tableFontStyles[loop] = getColumnFontStyle(columnData, offset, fontBases)
            offset += 44
        }
    }

    @Override
    protected override fun getColumnFontStyle(data: ByteArray, offset: Int, fontBases: Map<Integer, FontBase>): TableFontStyle {
        val uniqueID = MPPUtility.getInt(data, offset)
        val fieldType = MPPTaskField14.getInstance(MPPUtility.getShort(data, offset + 4))
        val index = Integer.valueOf(MPPUtility.getByte(data, offset + 8))
        val style = MPPUtility.getByte(data, offset + 11)
        val color = MPPUtility.getColor(data, offset + 12)
        val change = MPPUtility.getShort(data, offset + 40)
        val backgroundColor = MPPUtility.getColor(data, offset + 24)
        val backgroundPattern = BackgroundPattern.getInstance(MPPUtility.getShort(data, offset + 36))

        val fontBase = fontBases[index]

        val bold = style and 0x01 != 0
        val italic = style and 0x02 != 0
        val underline = style and 0x04 != 0
        val strikethrough = style and 0x08 != 0

        val boldChanged = change and 0x01 != 0
        val underlineChanged = change and 0x02 != 0
        val italicChanged = change and 0x04 != 0
        val colorChanged = change and 0x08 != 0
        val fontChanged = change and 0x10 != 0
        val backgroundColorChanged = change and 0x40 != 0
        val backgroundPatternChanged = change and 0x80 != 0
        val strikethroughChanged = change and 0x100 != 0

//System.out.println(tfs);
        return TableFontStyle(uniqueID, fieldType, fontBase, italic, bold, underline, strikethrough, color, backgroundColor, backgroundPattern, italicChanged, boldChanged, underlineChanged, strikethroughChanged, colorChanged, fontChanged, backgroundColorChanged, backgroundPatternChanged)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected override fun processProgressLines(fontBases: Map<Integer, FontBase>, progressLineData: ByteArray) {
        //MPPUtility.fileDump("c:\\temp\\props.txt", ByteArrayHelper.hexdump(progressLineData, false, 16, "").getBytes());
        m_progressLinesEnabled = progressLineData[0].toInt() != 0
        m_progressLinesAtCurrentDate = progressLineData[2].toInt() != 0
        m_progressLinesAtRecurringIntervals = progressLineData[4].toInt() != 0
        m_progressLinesInterval = Interval.getInstance(progressLineData[6].toInt())
        m_progressLinesIntervalDailyDayNumber = progressLineData[8].toInt()
        m_progressLinesIntervalDailyWorkday = progressLineData[10].toInt() != 0
        m_progressLinesIntervalWeekleyWeekNumber = progressLineData[12].toInt()
        m_progressLinesIntervalWeeklyDay[Day.SUNDAY.getValue()] = progressLineData[14].toInt() != 0
        m_progressLinesIntervalWeeklyDay[Day.MONDAY.getValue()] = progressLineData[16].toInt() != 0
        m_progressLinesIntervalWeeklyDay[Day.TUESDAY.getValue()] = progressLineData[18].toInt() != 0
        m_progressLinesIntervalWeeklyDay[Day.WEDNESDAY.getValue()] = progressLineData[20].toInt() != 0
        m_progressLinesIntervalWeeklyDay[Day.THURSDAY.getValue()] = progressLineData[22].toInt() != 0
        m_progressLinesIntervalWeeklyDay[Day.FRIDAY.getValue()] = progressLineData[24].toInt() != 0
        m_progressLinesIntervalWeeklyDay[Day.SATURDAY.getValue()] = progressLineData[26].toInt() != 0
        m_progressLinesIntervalMonthlyDay = progressLineData[32].toInt() != 0
        m_progressLinesIntervalMonthlyDayDayNumber = progressLineData[34].toInt()
        progressLinesIntervalMonthlyDayMonthNumber = progressLineData[28].toInt()
        m_progressLinesIntervalMonthlyFirstLast = progressLineData[40].toInt() == 1
        m_progressLinesIntervalMonthlyFirstLastDay = ProgressLineDay.getInstance(progressLineData[36].toInt())
        progressLinesIntervalMonthlyFirstLastMonthNumber = progressLineData[30].toInt()
        m_progressLinesBeginAtProjectStart = progressLineData[44].toInt() != 0
        m_progressLinesBeginAtDate = MPPUtility.getDate(progressLineData, 46)
        m_progressLinesDisplaySelected = progressLineData[48].toInt() != 0
        m_progressLinesActualPlan = progressLineData[52].toInt() != 0
        m_progressLinesDisplayType = MPPUtility.getShort(progressLineData, 54)
        m_progressLinesShowDate = progressLineData[56].toInt() != 0
        m_progressLinesDateFormat = MPPUtility.getShort(progressLineData, 58)
        m_progressLinesFontStyle = getFontStyle(progressLineData, 60, fontBases, true)
        m_progressLinesCurrentLineColor = MPPUtility.getColor(progressLineData, 92)
        m_progressLinesCurrentLineStyle = LineStyle.getInstance(progressLineData[104].toInt())
        m_progressLinesCurrentProgressPointColor = MPPUtility.getColor(progressLineData, 105)
        m_progressLinesCurrentProgressPointShape = progressLineData[117].toInt()
        m_progressLinesOtherLineColor = MPPUtility.getColor(progressLineData, 118)
        m_progressLinesOtherLineStyle = LineStyle.getInstance(progressLineData[130].toInt())
        m_progressLinesOtherProgressPointColor = MPPUtility.getColor(progressLineData, 131)
        m_progressLinesOtherProgressPointShape = progressLineData[143].toInt()

        val dateCount = MPPUtility.getShort(progressLineData, 50)
        if (dateCount != 0) {
            m_progressLinesDisplaySelectedDates = arrayOfNulls<Date>(dateCount)
            var offset = 144
            var count = 0
            while (count < dateCount && offset < progressLineData.size) {
                m_progressLinesDisplaySelectedDates[count] = MPPUtility.getDate(progressLineData, offset)
                offset += 2
                ++count
            }
        }
    }

    companion object {

        private val PROPERTIES = Integer.valueOf(6)
    }
}
