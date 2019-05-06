/*
 * file:       MppGanttTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       13/05/2010
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

package net.sf.mpxj.junit

import org.junit.Assert.*

import java.text.SimpleDateFormat
import java.util.HashSet

import net.sf.mpxj.Day
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.View
import net.sf.mpxj.mpp.ChartPattern
import net.sf.mpxj.mpp.GanttBarDateFormat
import net.sf.mpxj.mpp.GanttChartView
import net.sf.mpxj.mpp.Interval
import net.sf.mpxj.mpp.LineStyle
import net.sf.mpxj.mpp.LinkStyle
import net.sf.mpxj.mpp.MPPReader
import net.sf.mpxj.mpp.NonWorkingTimeStyle
import net.sf.mpxj.mpp.ProgressLineDay
import net.sf.mpxj.mpp.TableFontStyle

import org.junit.Test

/**
 * Tests to exercise MPP file read functionality for various versions of
 * MPP file.
 */
class MppGanttTest {
    /**
     * Test Gantt chart data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9Gantt() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9gantt.mpp"))
        testAll(mpp)
    }

    /**
     * Test Gantt chart data read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9GanttFrom12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9gantt-from12.mpp"))
        testAll(mpp)
    }

    /**
     * Test Gantt chart data read from an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9GanttFrom14() {
        //ProjectFile mpp = new MPPReader().read(MpxjTestData.filePath("mpp9gantt-from14.mpp"));
        //testAll(mpp);
    }

    /**
     * Test Gantt chart data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12Gantt() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12gantt.mpp"))
        testAll(mpp)
    }

    /**
     * Test Gantt chart data read from an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12GanttFrom14() {
        //ProjectFile mpp = new MPPReader().read(MpxjTestData.filePath("mpp12gantt-from14.mpp"));
        //testAll(mpp);
    }

    /**
     * Test Gantt chart data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14Gantt() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14gantt.mpp"))
        testAll(mpp)
    }

    /**
     * Main entry point for common tests.
     *
     * @param mpp project file to be tested
     */
    private fun testAll(mpp: ProjectFile) {
        testSummaryData(mpp)
        testFontStyles(mpp)
        testGridlines(mpp)
        testTimescales(mpp)
        testLayout(mpp)
        testTableFontStyles(mpp)
        testProgressLines(mpp)
    }

    /**
     * Test Gantt chart view summary data.
     *
     * @param file project file
     */
    private fun testSummaryData(file: ProjectFile) {
        val views = file.views

        //
        // Retrieve the Gantt Chart view
        //
        val view = views.get(0) as GanttChartView
        assertEquals("Gantt Chart", view.name)

        assertTrue(view.showInMenu)
        assertEquals(778, view.tableWidth.toLong())
        assertFalse(view.highlightFilter)
        assertEquals("Entry", view.tableName)
        assertEquals("&All Tasks", view.defaultFilterName)
        assertEquals("No Group", view.groupName)

        assertEquals("Standard", view.nonWorkingDaysCalendarName)
        assertEquals("java.awt.Color[r=194,g=220,b=255]", view.nonWorkingColor!!.toString())
        assertEquals(ChartPattern.LIGHTDOTTED, view.nonWorkingPattern)
        assertEquals(NonWorkingTimeStyle.BEHIND, view.nonWorkingStyle)

    }

    /**
     * Test the font styles associated with a Gantt chart view.
     *
     * @param file project file
     */
    private fun testFontStyles(file: ProjectFile) {
        val views = file.views

        //
        // Retrieve the Gantt Chart view
        //
        val view = views.get(0) as GanttChartView
        assertEquals("Gantt Chart", view.name)

        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=false underline=false strikethrough=false color=java.awt.Color[r=0,g=0,b=255] backgroundColor=null backgroundPattern=Solid]", view.highlightedTasksFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=false underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.rowAndColumnFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Berlin Sans FB size=8] italic=false bold=true underline=true strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.nonCriticalTasksFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=false underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.criticalTasksFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=true underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.summaryTasksFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Baskerville Old Face size=9] italic=true bold=false underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.milestoneTasksFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=false underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.middleTimescaleFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=false underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.bottomTimescaleFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=true underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.barTextLeftFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=true underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.barTextRightFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=true underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.barTextTopFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=true underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.barTextBottomFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=true underline=false strikethrough=false color=java.awt.Color[r=0,g=0,b=0] backgroundColor=null backgroundPattern=Solid]", view.barTextInsideFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=BankGothic Lt BT size=8] italic=false bold=false underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.markedTasksFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=10] italic=false bold=true underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.projectSummaryTasksFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=false underline=false strikethrough=false color=java.awt.Color[r=128,g=128,b=128] backgroundColor=null backgroundPattern=Solid]", view.externalTasksFontStyle!!.toString())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=false underline=false strikethrough=false color=null backgroundColor=null backgroundPattern=Solid]", view.topTimescaleFontStyle!!.toString())
    }

    /**
     * Common gridline tests.
     *
     * @param file project file
     */
    private fun testGridlines(file: ProjectFile) {
        val views = file.views

        //
        // Retrieve the Gantt Chart view
        //
        val view = views.get(0) as GanttChartView
        assertEquals("Gantt Chart", view.name)

        //
        // Test each set of grid line definitions
        //
        assertEquals("[GridLines NormalLineColor=null NormalLineStyle=None IntervalNumber=0 IntervalLineStyle=None IntervalLineColor=null]", view.ganttRowsGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=java.awt.Color[r=255,g=0,b=0] NormalLineStyle=Solid IntervalNumber=0 IntervalLineStyle=None IntervalLineColor=null]", view.barRowsGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=java.awt.Color[r=0,g=0,b=255] NormalLineStyle=Dotted1 IntervalNumber=0 IntervalLineStyle=None IntervalLineColor=java.awt.Color[r=0,g=0,b=0]]", view.middleTierColumnGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=java.awt.Color[r=0,g=128,b=0] NormalLineStyle=None IntervalNumber=0 IntervalLineStyle=None IntervalLineColor=null]", view.bottomTierColumnGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=java.awt.Color[r=128,g=128,b=128] NormalLineStyle=Dotted1 IntervalNumber=0 IntervalLineStyle=None IntervalLineColor=java.awt.Color[r=128,g=128,b=128]]", view.currentDateGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=java.awt.Color[r=192,g=192,b=192] NormalLineStyle=Solid IntervalNumber=5 IntervalLineStyle=None IntervalLineColor=java.awt.Color[r=192,g=192,b=192]]", view.sheetRowsGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=java.awt.Color[r=192,g=192,b=192] NormalLineStyle=Solid IntervalNumber=2 IntervalLineStyle=Dotted1 IntervalLineColor=java.awt.Color[r=192,g=192,b=192]]", view.sheetColumnsGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=java.awt.Color[r=128,g=128,b=128] NormalLineStyle=Solid IntervalNumber=0 IntervalLineStyle=None IntervalLineColor=java.awt.Color[r=128,g=128,b=128]]", view.titleVerticalGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=java.awt.Color[r=128,g=128,b=128] NormalLineStyle=Solid IntervalNumber=0 IntervalLineStyle=None IntervalLineColor=java.awt.Color[r=128,g=128,b=128]]", view.titleHorizontalGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=null NormalLineStyle=Dashed IntervalNumber=0 IntervalLineStyle=None IntervalLineColor=null]", view.pageBreakGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=null NormalLineStyle=None IntervalNumber=0 IntervalLineStyle=None IntervalLineColor=null]", view.projectStartGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=null NormalLineStyle=None IntervalNumber=0 IntervalLineStyle=None IntervalLineColor=null]", view.projectFinishGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=null NormalLineStyle=None IntervalNumber=0 IntervalLineStyle=None IntervalLineColor=null]", view.statusDateGridLines!!.toString())
        assertEquals("[GridLines NormalLineColor=java.awt.Color[r=0,g=0,b=128] NormalLineStyle=None IntervalNumber=0 IntervalLineStyle=None IntervalLineColor=null]", view.topTierColumnGridLines!!.toString())
    }

    /**
     * Test the timescale settings.
     *
     * @param file project file
     */
    private fun testTimescales(file: ProjectFile) {
        val views = file.views

        //
        // Retrieve the Gantt Chart view
        //
        val view = views.get(0) as GanttChartView
        assertEquals("Gantt Chart", view.name)

        assertEquals(2, view.timescaleShowTiers.toLong())
        assertEquals(100, view.timescaleSize.toLong())
        assertTrue(view.timescaleScaleSeparator)

        assertEquals("[TimescaleTier UsesFiscalYear=true TickLines=true Units=None Count=1 Format=[None] Alignment=Center]", view.timescaleTopTier!!.toString())
        assertEquals("[TimescaleTier UsesFiscalYear=true TickLines=true Units=Weeks Count=1 Format=[January 27, '02] Alignment=Left]", view.timescaleMiddleTier!!.toString())
        assertEquals("[TimescaleTier UsesFiscalYear=true TickLines=true Units=Days Count=1 Format=[S, M, T, ...] Alignment=Center]", view.timescaleBottomTier!!.toString())
    }

    /**
     * Test the layout settings.
     *
     * @param file project file
     */
    private fun testLayout(file: ProjectFile) {
        val views = file.views

        //
        // Retrieve the Gantt Chart view
        //
        val view = views.get(0) as GanttChartView
        assertEquals("Gantt Chart", view.name)

        assertTrue(view.showDrawings)
        assertTrue(view.roundBarsToWholeDays)
        assertTrue(view.showBarSplits)
        assertFalse(view.alwaysRollupGanttBars)
        assertFalse(view.hideRollupBarsWhenSummaryExpanded)
        assertEquals(12, view.ganttBarHeight.toLong())
        assertEquals(GanttBarDateFormat.DDMM, view.barDateFormat)
        assertEquals(LinkStyle.END_TOP, view.linkStyle)
    }

    /**
     * Test the table font style settings.
     *
     * @param file project file
     */
    private fun testTableFontStyles(file: ProjectFile) {
        val views = file.views

        //
        // Retrieve the Gantt Chart view
        //
        val view = views.get(0) as GanttChartView
        assertEquals("Gantt Chart", view.name)

        val tfs = view.tableFontStyles
        assertEquals(TABLE_FONT_STYLES.size.toLong(), tfs!!.size.toLong())

        for (loop in tfs.indices) {
            assertTrue(TABLE_FONT_STYLES_SET.contains(tfs[loop].toString()))
        }
    }

    /**
     * Test the progress line settings.
     *
     * @param file project file
     */
    private fun testProgressLines(file: ProjectFile) {
        val df = SimpleDateFormat("dd/MM/yyyy")

        val views = file.views

        //
        // Retrieve the Gantt Chart view
        //
        val view = views.get(0) as GanttChartView
        assertEquals("Gantt Chart", view.name)

        assertTrue(view.progressLinesEnabled)
        assertFalse(view.progressLinesAtCurrentDate)
        assertTrue(view.progressLinesAtRecurringIntervals)
        assertEquals(Interval.WEEKLY, view.progressLinesInterval)
        assertEquals(1, view.progressLinesIntervalDailyDayNumber.toLong())
        assertTrue(view.isProgressLinesIntervalDailyWorkday)
        val weeklyDay = view.progressLinesIntervalWeeklyDay
        assertFalse(weeklyDay[Day.SUNDAY.getValue()])
        assertTrue(weeklyDay[Day.MONDAY.getValue()])
        assertFalse(weeklyDay[Day.TUESDAY.getValue()])
        assertFalse(weeklyDay[Day.WEDNESDAY.getValue()])
        assertFalse(weeklyDay[Day.THURSDAY.getValue()])
        assertFalse(weeklyDay[Day.FRIDAY.getValue()])
        assertFalse(weeklyDay[Day.SATURDAY.getValue()])
        assertEquals(1, view.progressLinesIntervalWeekleyWeekNumber.toLong())
        assertFalse(view.progressLinesIntervalMonthlyDay)
        assertEquals(1, view.progressLinesIntervalMonthlyDayMonthNumber.toLong())
        assertEquals(1, view.progressLinesIntervalMonthlyDayDayNumber.toLong())
        assertEquals(ProgressLineDay.DAY, view.progressLinesIntervalMonthlyFirstLastDay)
        assertTrue(view.progressLinesIntervalMonthlyFirstLast)
        assertEquals(1, view.progressLinesIntervalMonthlyFirstLastMonthNumber.toLong())

        assertFalse(view.progressLinesBeginAtProjectStart)
        assertEquals("13/05/2010", df.format(view.progressLinesBeginAtDate))
        assertTrue(view.progressLinesDisplaySelected)
        assertTrue(view.progressLinesActualPlan)
        assertEquals(0, view.progressLinesDisplayType.toLong())
        assertFalse(view.progressLinesShowDate)
        assertEquals(26, view.progressLinesDateFormat.toLong())
        assertEquals("[FontStyle fontBase=[FontBase name=Arial size=8] italic=false bold=false underline=false strikethrough=false color=java.awt.Color[r=0,g=0,b=0] backgroundColor=null backgroundPattern=Solid]", view.progressLinesFontStyle!!.toString())
        assertEquals("java.awt.Color[r=255,g=0,b=0]", view.progressLinesCurrentLineColor!!.toString())
        assertEquals(LineStyle.SOLID, view.progressLinesCurrentLineStyle)
        assertEquals("java.awt.Color[r=255,g=0,b=0]", view.progressLinesCurrentProgressPointColor!!.toString())
        assertEquals(13, view.progressLinesCurrentProgressPointShape.toLong())
        assertEquals(null, view.progressLinesOtherLineColor)
        assertEquals(LineStyle.SOLID, view.progressLinesOtherLineStyle)
        assertEquals(null, view.progressLinesOtherProgressPointColor)
        assertEquals(0, view.progressLinesOtherProgressPointShape.toLong())
        assertEquals(2, view.progressLinesDisplaySelectedDates!!.size.toLong())
        assertEquals("01/02/2010", df.format(view.progressLinesDisplaySelectedDates!![0]))
        assertEquals("01/01/2010", df.format(view.progressLinesDisplaySelectedDates!![1]))
    }

    companion object {

        private val TABLE_FONT_STYLES = arrayOf("[ColumnFontStyle rowUniqueID=3 fieldType=Text2 color=java.awt.Color[r=0,g=0,b=255]]", "[ColumnFontStyle rowUniqueID=-1 fieldType=Task Name italic=false bold=true underline=false font=[FontBase name=Arial Black size=8] color=null backgroundColor=java.awt.Color[r=0,g=0,b=0] backgroundPattern=Transparent]", "[ColumnFontStyle rowUniqueID=-1 fieldType=Duration italic=false bold=true underline=false font=[FontBase name=Arial size=8] color=null backgroundColor=java.awt.Color[r=0,g=0,b=0] backgroundPattern=Transparent]", "[ColumnFontStyle rowUniqueID=-1 fieldType=Start italic=true bold=false underline=false font=[FontBase name=Arial size=8] color=null backgroundColor=java.awt.Color[r=0,g=0,b=0] backgroundPattern=Transparent]", "[ColumnFontStyle rowUniqueID=-1 fieldType=Finish italic=true bold=true underline=false font=[FontBase name=Arial size=8] color=null backgroundColor=java.awt.Color[r=0,g=0,b=0] backgroundPattern=Transparent]", "[ColumnFontStyle rowUniqueID=-1 fieldType=Predecessors italic=false bold=false underline=false font=[FontBase name=Arial size=10] color=null backgroundColor=java.awt.Color[r=0,g=0,b=0] backgroundPattern=Transparent]", "[ColumnFontStyle rowUniqueID=-1 fieldType=Text1 italic=false bold=false underline=true font=[FontBase name=Arial size=8] color=null backgroundColor=java.awt.Color[r=0,g=0,b=0] backgroundPattern=Transparent]", "[ColumnFontStyle rowUniqueID=-1 fieldType=Text2 italic=false bold=false underline=false font=[FontBase name=Arial size=8] color=java.awt.Color[r=255,g=0,b=0] backgroundColor=java.awt.Color[r=0,g=0,b=0] backgroundPattern=Transparent]")

        private val TABLE_FONT_STYLES_SET = HashSet<String>()

        init {
            for (style in TABLE_FONT_STYLES) {
                TABLE_FONT_STYLES_SET.add(style)
            }
        }
    }
}
