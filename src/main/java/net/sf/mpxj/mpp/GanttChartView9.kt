/*
 * file:       GanttChartView9.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Apr 7, 2005
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
import java.util.LinkedList

import net.sf.mpxj.Day
import net.sf.mpxj.FieldType
import net.sf.mpxj.Filter
import net.sf.mpxj.GenericCriteria
import net.sf.mpxj.ProjectFile

/**
 * This class represents the set of properties used to define the appearance
 * of a Gantt chart view in MS Project.
 */
class GanttChartView9
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
        val f = GanttBarStyleFactoryCommon()
        barStyles = f.processDefaultStyles(props)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun processExceptionBarStyles(props: Props) {
        val f = GanttBarStyleFactoryCommon()
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
        val criteria = FilterCriteriaReader9()
        val fields = LinkedList<FieldType>()

        //
        // Filter data: 24 byte header, plus 80 byte criteria blocks,
        // plus var data. Total block size is specified at the start of the
        // block.
        //
        for (loop in 0 until filterCount) {
            val blockSize = MPPUtility.getShort(data, offset)

            //
            // Steelray 12335: the block size may be zero
            //
            if (blockSize == 0) {
                break
            }

            //System.out.println(ByteArrayHelper.hexdump(data, offset, blockSize, true, 16, ""));

            val entryOffset = MPPUtility.getShort(data, offset + 12)
            fields.clear()
            val c = criteria.process(m_properties, data, offset + 4, entryOffset, null, fields, null)
            //System.out.println(c);

            if (!fields.isEmpty()) {
                val filter = Filter()
                filter.setCriteria(c)
                m_autoFilters.add(filter)
                m_autoFiltersByType.put(fields.get(0), filter)
            }

            //
            // Move to the next filter
            //
            offset += blockSize
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected override fun processViewProperties(fontBases: Map<Integer, FontBase>, props: Props) {
        //MPPUtility.fileDump("c:\\temp\\props.txt", props.toString().getBytes());

        val viewPropertyData = props.getByteArray(GanttChartView.VIEW_PROPERTIES)
        if (viewPropertyData != null) {
            //MPPUtility.fileDump("c:\\temp\\props.txt", ByteArrayHelper.hexdump(viewPropertyData, false, 16, "").getBytes());

            m_highlightedTasksFontStyle = getFontStyle(viewPropertyData, 26, fontBases)
            m_rowAndColumnFontStyle = getFontStyle(viewPropertyData, 30, fontBases)
            m_nonCriticalTasksFontStyle = getFontStyle(viewPropertyData, 34, fontBases)
            m_criticalTasksFontStyle = getFontStyle(viewPropertyData, 38, fontBases)
            m_summaryTasksFontStyle = getFontStyle(viewPropertyData, 42, fontBases)
            m_milestoneTasksFontStyle = getFontStyle(viewPropertyData, 46, fontBases)
            m_middleTimescaleFontStyle = getFontStyle(viewPropertyData, 50, fontBases)
            m_bottomTimescaleFontStyle = getFontStyle(viewPropertyData, 54, fontBases)
            m_barTextLeftFontStyle = getFontStyle(viewPropertyData, 58, fontBases)
            m_barTextRightFontStyle = getFontStyle(viewPropertyData, 62, fontBases)
            m_barTextTopFontStyle = getFontStyle(viewPropertyData, 66, fontBases)
            m_barTextBottomFontStyle = getFontStyle(viewPropertyData, 70, fontBases)
            m_barTextInsideFontStyle = getFontStyle(viewPropertyData, 74, fontBases)
            m_markedTasksFontStyle = getFontStyle(viewPropertyData, 78, fontBases)
            m_projectSummaryTasksFontStyle = getFontStyle(viewPropertyData, 82, fontBases)
            m_externalTasksFontStyle = getFontStyle(viewPropertyData, 86, fontBases)
            m_topTimescaleFontStyle = getFontStyle(viewPropertyData, 90, fontBases)

            m_sheetRowsGridLines = getGridLines(viewPropertyData, 99)
            m_sheetColumnsGridLines = getGridLines(viewPropertyData, 109)
            m_titleVerticalGridLines = getGridLines(viewPropertyData, 119)
            m_titleHorizontalGridLines = getGridLines(viewPropertyData, 129)
            m_middleTierColumnGridLines = getGridLines(viewPropertyData, 139)
            m_bottomTierColumnGridLines = getGridLines(viewPropertyData, 149)
            m_ganttRowsGridLines = getGridLines(viewPropertyData, 159)
            m_barRowsGridLines = getGridLines(viewPropertyData, 169)
            m_currentDateGridLines = getGridLines(viewPropertyData, 179)
            m_pageBreakGridLines = getGridLines(viewPropertyData, 189)
            m_projectStartGridLines = getGridLines(viewPropertyData, 199)
            m_projectFinishGridLines = getGridLines(viewPropertyData, 209)
            m_statusDateGridLines = getGridLines(viewPropertyData, 219)

            m_nonWorkingDaysCalendarName = MPPUtility.getUnicodeString(viewPropertyData, 352)
            m_nonWorkingColor = ColorType.getInstance(viewPropertyData[1153].toInt()).color
            m_nonWorkingPattern = ChartPattern.getInstance(viewPropertyData[1154].toInt())
            m_nonWorkingStyle = NonWorkingTimeStyle.getInstance(viewPropertyData[1152].toInt())

            m_ganttBarHeight = mapGanttBarHeight(MPPUtility.getByte(viewPropertyData, 1163))

            val flags = viewPropertyData[228]

            m_timescaleMiddleTier = TimescaleTier()
            m_timescaleMiddleTier!!.tickLines = flags and 0x01 != 0
            m_timescaleMiddleTier!!.usesFiscalYear = flags and 0x08 != 0
            m_timescaleMiddleTier!!.units = TimescaleUnits.getInstance(viewPropertyData[242].toInt())
            m_timescaleMiddleTier!!.count = viewPropertyData[246]
            m_timescaleMiddleTier!!.format = TimescaleFormat.getInstance(MPPUtility.getShort(viewPropertyData, 250))
            m_timescaleMiddleTier!!.alignment = TimescaleAlignment.getInstance(viewPropertyData[256] - 32)

            m_timescaleBottomTier = TimescaleTier()
            m_timescaleBottomTier!!.tickLines = flags and 0x02 != 0
            m_timescaleBottomTier!!.usesFiscalYear = flags and 0x10 != 0
            m_timescaleBottomTier!!.units = TimescaleUnits.getInstance(viewPropertyData[244].toInt())
            m_timescaleBottomTier!!.count = viewPropertyData[248]
            m_timescaleBottomTier!!.format = TimescaleFormat.getInstance(MPPUtility.getShort(viewPropertyData, 252))
            m_timescaleBottomTier!!.alignment = TimescaleAlignment.getInstance(viewPropertyData[254] - 32)

            m_timescaleScaleSeparator = flags and 0x04 != 0
            m_timescaleSize = viewPropertyData[268].toInt()

            m_showDrawings = viewPropertyData[1156].toInt() != 0
            m_roundBarsToWholeDays = viewPropertyData[1158].toInt() != 0
            m_showBarSplits = viewPropertyData[1160].toInt() != 0
            m_alwaysRollupGanttBars = viewPropertyData[1186].toInt() != 0
            m_hideRollupBarsWhenSummaryExpanded = viewPropertyData[1188].toInt() != 0
            m_barDateFormat = GanttBarDateFormat.getInstance(viewPropertyData[1182] + 1)
            m_linkStyle = LinkStyle.getInstance(viewPropertyData[1155].toInt())

        }

        val timescaleData = props.getByteArray(GanttChartView.TIMESCALE_PROPERTIES)
        if (timescaleData != null) {
            m_timescaleTopTier = TimescaleTier()

            m_timescaleTopTier!!.tickLines = timescaleData[48].toInt() != 0
            m_timescaleTopTier!!.usesFiscalYear = timescaleData[60].toInt() != 0
            m_timescaleTopTier!!.units = TimescaleUnits.getInstance(timescaleData[30].toInt())
            m_timescaleTopTier!!.count = timescaleData[32]
            m_timescaleTopTier!!.format = TimescaleFormat.getInstance(MPPUtility.getShort(timescaleData, 34))
            m_timescaleTopTier!!.alignment = TimescaleAlignment.getInstance(timescaleData[36] - 20)

            m_topTierColumnGridLines = getGridLines(timescaleData, 39)

            timescaleShowTiers = timescaleData[0].toInt()
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
        val normalLineColor = ColorType.getInstance(data[offset].toInt()).color
        val normalLineStyle = LineStyle.getInstance(data[offset + 3].toInt())
        val intervalNumber = data[offset + 4].toInt()
        val intervalLineStyle = LineStyle.getInstance(data[offset + 5].toInt())
        val intervalLineColor = ColorType.getInstance(data[offset + 6].toInt()).color
        return GridLines(normalLineColor, normalLineStyle, intervalNumber, intervalLineStyle, intervalLineColor)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected override fun processTableFontStyles(fontBases: Map<Integer, FontBase>, columnData: ByteArray) {
        m_tableFontStyles = arrayOfNulls(columnData.size / 16)
        var offset = 0
        for (loop in m_tableFontStyles!!.indices) {
            m_tableFontStyles[loop] = getColumnFontStyle(columnData, offset, fontBases)
            offset += 16
        }
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
        m_progressLinesIntervalWeeklyDay[Day.SUNDAY.getValue()] = progressLineData[14].toInt() != 0
        m_progressLinesIntervalWeeklyDay[Day.MONDAY.getValue()] = progressLineData[16].toInt() != 0
        m_progressLinesIntervalWeeklyDay[Day.TUESDAY.getValue()] = progressLineData[18].toInt() != 0
        m_progressLinesIntervalWeeklyDay[Day.WEDNESDAY.getValue()] = progressLineData[20].toInt() != 0
        m_progressLinesIntervalWeeklyDay[Day.THURSDAY.getValue()] = progressLineData[22].toInt() != 0
        m_progressLinesIntervalWeeklyDay[Day.FRIDAY.getValue()] = progressLineData[24].toInt() != 0
        m_progressLinesIntervalWeeklyDay[Day.SATURDAY.getValue()] = progressLineData[26].toInt() != 0
        m_progressLinesIntervalWeekleyWeekNumber = progressLineData[30].toInt()
        m_progressLinesIntervalMonthlyDay = progressLineData[32].toInt() != 0
        m_progressLinesIntervalMonthlyDayDayNumber = progressLineData[34].toInt()
        progressLinesIntervalMonthlyDayMonthNumber = progressLineData[28].toInt()
        m_progressLinesIntervalMonthlyFirstLastDay = ProgressLineDay.getInstance(progressLineData[36].toInt())
        m_progressLinesIntervalMonthlyFirstLast = progressLineData[40].toInt() == 1
        progressLinesIntervalMonthlyFirstLastMonthNumber = progressLineData[30].toInt()
        m_progressLinesBeginAtProjectStart = progressLineData[44].toInt() != 0
        m_progressLinesBeginAtDate = MPPUtility.getDate(progressLineData, 46)
        m_progressLinesDisplaySelected = progressLineData[48].toInt() != 0
        m_progressLinesActualPlan = progressLineData[52].toInt() != 0
        m_progressLinesDisplayType = MPPUtility.getShort(progressLineData, 54)
        m_progressLinesShowDate = progressLineData[56].toInt() != 0
        m_progressLinesDateFormat = MPPUtility.getShort(progressLineData, 58)
        m_progressLinesFontStyle = getFontStyle(progressLineData, 60, fontBases)
        m_progressLinesCurrentLineColor = ColorType.getInstance(progressLineData[64].toInt()).color
        m_progressLinesCurrentLineStyle = LineStyle.getInstance(progressLineData[65].toInt())
        m_progressLinesCurrentProgressPointColor = ColorType.getInstance(progressLineData[66].toInt()).color
        m_progressLinesCurrentProgressPointShape = progressLineData[67].toInt()
        m_progressLinesOtherLineColor = ColorType.getInstance(progressLineData[68].toInt()).color
        m_progressLinesOtherLineStyle = LineStyle.getInstance(progressLineData[69].toInt())
        m_progressLinesOtherProgressPointColor = ColorType.getInstance(progressLineData[70].toInt()).color
        m_progressLinesOtherProgressPointShape = progressLineData[71].toInt()

        val dateCount = MPPUtility.getShort(progressLineData, 50)
        if (dateCount != 0) {
            m_progressLinesDisplaySelectedDates = arrayOfNulls<Date>(dateCount)
            var offset = 72
            var count = 0
            while (count < dateCount && offset < progressLineData.size) {
                m_progressLinesDisplaySelectedDates[count] = MPPUtility.getDate(progressLineData, offset)
                offset += 2
                ++count
            }
        }
    }

    companion object {

        private val PROPERTIES = Integer.valueOf(1)
    }
}
