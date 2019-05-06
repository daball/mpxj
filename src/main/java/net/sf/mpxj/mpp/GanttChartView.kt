/*
 * file:       GanttChartView.java
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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.util.Date
import java.util.HashMap
import java.util.LinkedList

import net.sf.mpxj.FieldType
import net.sf.mpxj.Filter
import net.sf.mpxj.FilterContainer
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.common.MPPTaskField

/**
 * This class represents the set of properties used to define the appearance
 * of a Gantt chart view in MS Project.
 */
abstract class GanttChartView
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
internal constructor(parent: ProjectFile, fixedMeta: ByteArray, fixedData: ByteArray, varData: Var2Data, fontBases: Map<Integer, FontBase>) : GenericView(parent, fixedData, varData) {

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val sheetColumnsGridLines: GridLines?
        get() = m_sheetColumnsGridLines

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val sheetRowsGridLines: GridLines?
        get() = m_sheetRowsGridLines

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val statusDateGridLines: GridLines?
        get() = m_statusDateGridLines

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val titleHorizontalGridLines: GridLines?
        get() = m_titleHorizontalGridLines

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val titleVerticalGridLines: GridLines?
        get() = m_titleVerticalGridLines

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val barRowsGridLines: GridLines?
        get() = m_barRowsGridLines

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val currentDateGridLines: GridLines?
        get() = m_currentDateGridLines

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val ganttRowsGridLines: GridLines?
        get() = m_ganttRowsGridLines

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val topTierColumnGridLines: GridLines?
        get() = m_topTierColumnGridLines

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val middleTierColumnGridLines: GridLines?
        get() = m_middleTierColumnGridLines

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val bottomTierColumnGridLines: GridLines?
        get() = m_bottomTierColumnGridLines

    /**
     * Retrieve the name of the calendar used to define non-working days for
     * this view..
     *
     * @return calendar name
     */
    val nonWorkingDaysCalendarName: String?
        get() = m_nonWorkingDaysCalendarName

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val pageBreakGridLines: GridLines?
        get() = m_pageBreakGridLines

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val projectFinishGridLines: GridLines?
        get() = m_projectFinishGridLines

    /**
     * Retrieve a grid lines definition.
     *
     * @return grid lines definition
     */
    val projectStartGridLines: GridLines?
        get() = m_projectStartGridLines

    /**
     * Retrieve the height of the Gantt bars in this view.
     *
     * @return Gantt bar height
     */
    val ganttBarHeight: Int
        get() = m_ganttBarHeight

    /**
     * Retrieve a flag indicating if a separator is shown between the
     * major and minor scales.
     *
     * @return boolean flag
     */
    val timescaleScaleSeparator: Boolean
        get() = m_timescaleScaleSeparator

    /**
     * Retrieves a timescale tier.
     *
     * @return timescale tier
     */
    val timescaleTopTier: TimescaleTier?
        get() = m_timescaleTopTier

    /**
     * Retrieves a timescale tier.
     *
     * @return timescale tier
     */
    val timescaleMiddleTier: TimescaleTier?
        get() = m_timescaleMiddleTier

    /**
     * Retrieves a timescale tier.
     *
     * @return timescale tier
     */
    val timescaleBottomTier: TimescaleTier?
        get() = m_timescaleBottomTier

    /**
     * Retrieve the timescale size value. This is a percentage value.
     *
     * @return timescale size value
     */
    val timescaleSize: Int
        get() = m_timescaleSize

    /**
     * Retrieve the non-working time color.
     *
     * @return non-working time color
     */
    val nonWorkingColor: Color?
        get() = m_nonWorkingColor

    /**
     * Retrieve the non-working time pattern. This is an integer between
     * 0 and 10 inclusive which represents the fixed set of patterns
     * supported by MS Project.
     *
     * @return non-working time pattern
     */
    val nonWorkingPattern: ChartPattern?
        get() = m_nonWorkingPattern

    /**
     * Retrieve the style used to draw non-working time.
     *
     * @return non working time style
     */
    val nonWorkingStyle: NonWorkingTimeStyle?
        get() = m_nonWorkingStyle

    /**
     * Retrieve the always rollup Gantt bars flag.
     *
     * @return always rollup Gantt bars flag
     */
    val alwaysRollupGanttBars: Boolean
        get() = m_alwaysRollupGanttBars

    /**
     * Retrieve the bar date format.
     *
     * @return bar date format
     */
    val barDateFormat: GanttBarDateFormat?
        get() = m_barDateFormat

    /**
     * Retrieve the hide rollup bars when summary expanded.
     *
     * @return hide rollup bars when summary expanded
     */
    val hideRollupBarsWhenSummaryExpanded: Boolean
        get() = m_hideRollupBarsWhenSummaryExpanded

    /**
     * Retrieve the bar link style.
     *
     * @return bar link style
     */
    val linkStyle: LinkStyle?
        get() = m_linkStyle

    /**
     * Retrieve the round bars to whole days flag.
     *
     * @return round bars to whole days flag
     */
    val roundBarsToWholeDays: Boolean
        get() = m_roundBarsToWholeDays

    /**
     * Retrieve the show bar splits flag.
     *
     * @return show bar splits flag
     */
    val showBarSplits: Boolean
        get() = m_showBarSplits

    /**
     * Retrieve the show drawings flag.
     *
     * @return show drawings flag
     */
    val showDrawings: Boolean
        get() = m_showDrawings

    /**
     * Retrieve the width ofthe table part of the view.
     *
     * @return table width
     */
    val tableWidth: Int
        get() = m_tableWidth

    /**
     * Retrieve the name of the filter applied to this view.
     *
     * @return filter name
     */
    val defaultFilterName: String?
        get() = m_defaultFilterName

    /**
     * Convenience method used to retrieve the default filter instance
     * associated with this view.
     *
     * @return filter instance, null if no filter associated with view
     */
    val defaultFilter: Filter
        get() = m_filters.getFilterByName(m_defaultFilterName)

    /**
     * Retrieve the name of the grouping applied to this view.
     *
     * @return group name
     */
    val groupName: String?
        get() = m_groupName

    /**
     * Retrieve the highlight filter flag.
     *
     * @return highlight filter flag
     */
    val highlightFilter: Boolean
        get() = m_highlightFilter

    /**
     * Retrieve the show in menu flag.
     *
     * @return show in menu flag
     */
    val showInMenu: Boolean
        get() = m_showInMenu

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val barTextBottomFontStyle: FontStyle?
        get() = m_barTextBottomFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val barTextInsideFontStyle: FontStyle?
        get() = m_barTextInsideFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val barTextLeftFontStyle: FontStyle?
        get() = m_barTextLeftFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val barTextRightFontStyle: FontStyle?
        get() = m_barTextRightFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val barTextTopFontStyle: FontStyle?
        get() = m_barTextTopFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val criticalTasksFontStyle: FontStyle?
        get() = m_criticalTasksFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val externalTasksFontStyle: FontStyle?
        get() = m_externalTasksFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val highlightedTasksFontStyle: FontStyle?
        get() = m_highlightedTasksFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val topTimescaleFontStyle: FontStyle?
        get() = m_topTimescaleFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val middleTimescaleFontStyle: FontStyle?
        get() = m_middleTimescaleFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val markedTasksFontStyle: FontStyle?
        get() = m_markedTasksFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val milestoneTasksFontStyle: FontStyle?
        get() = m_milestoneTasksFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val bottomTimescaleFontStyle: FontStyle?
        get() = m_bottomTimescaleFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val nonCriticalTasksFontStyle: FontStyle?
        get() = m_nonCriticalTasksFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val projectSummaryTasksFontStyle: FontStyle?
        get() = m_projectSummaryTasksFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val rowAndColumnFontStyle: FontStyle?
        get() = m_rowAndColumnFontStyle

    /**
     * Retrieve a FontStyle instance.
     *
     * @return FontStyle instance
     */
    val summaryTasksFontStyle: FontStyle?
        get() = m_summaryTasksFontStyle

    /**
     * Retrieve any column font syles which the user has defined.
     *
     * @return column font styles array
     */
    val tableFontStyles: Array<TableFontStyle>?
        get() = m_tableFontStyles

    /**
     * Retrieve the progress lines actual plan flag.
     *
     * @return boolean flag
     */
    val progressLinesActualPlan: Boolean
        get() = m_progressLinesActualPlan

    /**
     * Retrieve the progress lines at current date flag.
     *
     * @return boolean flag
     */
    val progressLinesAtCurrentDate: Boolean
        get() = m_progressLinesAtCurrentDate

    /**
     * Retrieve the progress lines at recurring intervals flag.
     *
     * @return boolean flag
     */
    val progressLinesAtRecurringIntervals: Boolean
        get() = m_progressLinesAtRecurringIntervals

    /**
     * Retrieve the progress lines begin at date.
     *
     * @return progress lines begin at date
     */
    val progressLinesBeginAtDate: Date?
        get() = m_progressLinesBeginAtDate

    /**
     * Retrieve the progress lines begin at project start flag.
     *
     * @return boolean flag
     */
    val progressLinesBeginAtProjectStart: Boolean
        get() = m_progressLinesBeginAtProjectStart

    /**
     * Retrieve the progress lines current line color.
     *
     * @return current line color
     */
    val progressLinesCurrentLineColor: Color?
        get() = m_progressLinesCurrentLineColor

    /**
     * Retrieve the progress lines current line style.
     *
     * @return current line style
     */
    val progressLinesCurrentLineStyle: LineStyle?
        get() = m_progressLinesCurrentLineStyle

    /**
     * Retrieve the current progress point color.
     *
     * @return current progress point color
     */
    val progressLinesCurrentProgressPointColor: Color?
        get() = m_progressLinesCurrentProgressPointColor

    /**
     * Retrieve the current progress point shape.
     *
     * @return current progress point shape
     */
    val progressLinesCurrentProgressPointShape: Int
        get() = m_progressLinesCurrentProgressPointShape

    /**
     * Retrieve the progress lines daily day number.
     *
     * @return progress lines daily day number
     */
    val progressLinesIntervalDailyDayNumber: Int
        get() = m_progressLinesIntervalDailyDayNumber

    /**
     * Retrieve the progress lines daily workday flag.
     *
     * @return daily workday flag
     */
    val isProgressLinesIntervalDailyWorkday: Boolean
        get() = m_progressLinesIntervalDailyWorkday

    /**
     * Retrieve the progress line date format.
     *
     * @return progress line date format.
     */
    val progressLinesDateFormat: Int
        get() = m_progressLinesDateFormat

    /**
     * Retrieves the flag indicating if selected dates have been supplied
     * for progress line display.
     *
     * @return boolean flag
     */
    val progressLinesDisplaySelected: Boolean
        get() = m_progressLinesDisplaySelected

    /**
     * Retrieves an array of selected dates for progress line display,
     * or returns null if no dates have been supplied.
     *
     * @return array of selected dates
     */
    val progressLinesDisplaySelectedDates: Array<Date>?
        get() = m_progressLinesDisplaySelectedDates

    /**
     * Retrieves the progress lines display type.
     *
     * @return progress lines display type
     */
    val progressLinesDisplayType: Int
        get() = m_progressLinesDisplayType

    /**
     * Retrieves the progress lines enabled flag.
     *
     * @return boolean flag
     */
    val progressLinesEnabled: Boolean
        get() = m_progressLinesEnabled

    /**
     * Retrieves the progress lines font style.
     *
     * @return progress lines font style
     */
    val progressLinesFontStyle: FontStyle?
        get() = m_progressLinesFontStyle

    /**
     * Retrieves the progress line interval.
     *
     * @return progress line interval
     */
    val progressLinesInterval: Interval?
        get() = m_progressLinesInterval

    /**
     * Retrieves the progress lines monthly day.
     *
     * @return progress lines monthly day
     */
    val progressLinesIntervalMonthlyFirstLastDay: ProgressLineDay?
        get() = m_progressLinesIntervalMonthlyFirstLastDay

    /**
     * Retrieves the progress lines monthly day number.
     *
     * @return progress lines monthly day number
     */
    val progressLinesIntervalMonthlyDayDayNumber: Int
        get() = m_progressLinesIntervalMonthlyDayDayNumber

    /**
     * Retrieves the progress lines monthly day of month.
     *
     * @return progress lines monthly day of month
     */
    val progressLinesIntervalMonthlyDay: Boolean
        get() = m_progressLinesIntervalMonthlyDay

    /**
     * Retrieves the progress lines monthly first flag.
     *
     * @return progress lines monthly first flag
     */
    val progressLinesIntervalMonthlyFirstLast: Boolean
        get() = m_progressLinesIntervalMonthlyFirstLast

    /**
     * Retrieves the progress lines other line color.
     *
     * @return progress lines other line color
     */
    val progressLinesOtherLineColor: Color?
        get() = m_progressLinesOtherLineColor

    /**
     * Retrieves the progress lines other line style.
     *
     * @return progress lines other line style
     */
    val progressLinesOtherLineStyle: LineStyle?
        get() = m_progressLinesOtherLineStyle

    /**
     * Retrieves the progress lines other progress point color.
     *
     * @return progress lines other progress point color
     */
    val progressLinesOtherProgressPointColor: Color?
        get() = m_progressLinesOtherProgressPointColor

    /**
     * Retrieves the progress lines other progress point shape.
     *
     * @return progress lines other progress point shape
     */
    val progressLinesOtherProgressPointShape: Int
        get() = m_progressLinesOtherProgressPointShape

    /**
     * Retrieves the progress lines show date flag.
     *
     * @return progress lines show date flag
     */
    val progressLinesShowDate: Boolean
        get() = m_progressLinesShowDate

    /**
     * Retrieves the progress lines weekly week number.
     *
     * @return progress lines weekly week number
     */
    val progressLinesIntervalWeekleyWeekNumber: Int
        get() = m_progressLinesIntervalWeekleyWeekNumber

    /**
     * Retrieves the progress lines weekly day.
     * Note that this is designed to be used with the constants defined
     * by the Day class, for example use Day.MONDAY.getValue() as the
     * index into this array for the Monday flag.
     *
     * @return progress lines weekly day
     */
    val progressLinesIntervalWeeklyDay: BooleanArray
        get() = m_progressLinesIntervalWeeklyDay

    /**
     * Retrieves a list of all auto filters associated with this view.
     *
     * @return list of filter instances
     */
    val autoFilters: List<Filter>
        get() = m_autoFilters

    protected var m_sheetRowsGridLines: GridLines? = null
    protected var m_sheetColumnsGridLines: GridLines? = null
    protected var m_titleVerticalGridLines: GridLines? = null
    protected var m_titleHorizontalGridLines: GridLines? = null
    protected var m_middleTierColumnGridLines: GridLines? = null
    protected var m_bottomTierColumnGridLines: GridLines? = null
    protected var m_ganttRowsGridLines: GridLines? = null
    protected var m_barRowsGridLines: GridLines? = null
    protected var m_currentDateGridLines: GridLines? = null
    protected var m_pageBreakGridLines: GridLines? = null
    protected var m_projectStartGridLines: GridLines? = null
    protected var m_projectFinishGridLines: GridLines? = null
    protected var m_statusDateGridLines: GridLines? = null
    protected var m_topTierColumnGridLines: GridLines? = null

    protected var m_ganttBarHeight: Int = 0

    protected var m_timescaleTopTier: TimescaleTier? = null
    protected var m_timescaleMiddleTier: TimescaleTier? = null
    protected var m_timescaleBottomTier: TimescaleTier? = null
    protected var m_timescaleScaleSeparator: Boolean = false
    protected var m_timescaleSize: Int = 0
    /**
     * Retrieve the number of timescale tiers to display.
     *
     * @return number of timescale tiers to show
     */
    var timescaleShowTiers: Int = 0
        protected set

    protected var m_nonWorkingDaysCalendarName: String? = null
    protected var m_nonWorkingColor: Color? = null
    protected var m_nonWorkingPattern: ChartPattern? = null
    protected var m_nonWorkingStyle: NonWorkingTimeStyle? = null

    protected var m_showDrawings: Boolean = false
    protected var m_roundBarsToWholeDays: Boolean = false
    protected var m_showBarSplits: Boolean = false
    protected var m_alwaysRollupGanttBars: Boolean = false
    protected var m_hideRollupBarsWhenSummaryExpanded: Boolean = false
    protected var m_barDateFormat: GanttBarDateFormat? = null
    protected var m_linkStyle: LinkStyle? = null

    /**
     * Retrieve an array of bar styles which are applied to all Gantt
     * chart bars, unless an exception has been defined.
     *
     * @return array of bar styles
     */
    var barStyles: Array<GanttBarStyle>? = null
        protected set
    /**
     * Retrieve an array representing bar styles which have been defined
     * by the user for a specific task.
     *
     * @return array of bar style exceptions
     */
    var barStyleExceptions: Array<GanttBarStyleException>? = null
        protected set

    private var m_tableWidth: Int = 0
    private var m_defaultFilterName: String? = null
    private var m_groupName: String? = null
    private var m_highlightFilter: Boolean = false
    private val m_showInMenu: Boolean

    protected var m_highlightedTasksFontStyle: FontStyle? = null
    protected var m_rowAndColumnFontStyle: FontStyle? = null
    protected var m_nonCriticalTasksFontStyle: FontStyle? = null
    protected var m_criticalTasksFontStyle: FontStyle? = null
    protected var m_summaryTasksFontStyle: FontStyle? = null
    protected var m_milestoneTasksFontStyle: FontStyle? = null
    protected var m_topTimescaleFontStyle: FontStyle? = null
    protected var m_middleTimescaleFontStyle: FontStyle? = null
    protected var m_bottomTimescaleFontStyle: FontStyle? = null
    protected var m_barTextLeftFontStyle: FontStyle? = null
    protected var m_barTextRightFontStyle: FontStyle? = null
    protected var m_barTextTopFontStyle: FontStyle? = null
    protected var m_barTextBottomFontStyle: FontStyle? = null
    protected var m_barTextInsideFontStyle: FontStyle? = null
    protected var m_markedTasksFontStyle: FontStyle? = null
    protected var m_projectSummaryTasksFontStyle: FontStyle? = null
    protected var m_externalTasksFontStyle: FontStyle? = null

    protected var m_tableFontStyles: Array<TableFontStyle>? = null

    protected var m_progressLinesEnabled: Boolean = false
    protected var m_progressLinesAtCurrentDate: Boolean = false
    protected var m_progressLinesAtRecurringIntervals: Boolean = false
    protected var m_progressLinesInterval: Interval? = null
    protected var m_progressLinesIntervalDailyDayNumber: Int = 0
    protected var m_progressLinesIntervalDailyWorkday: Boolean = false
    protected var m_progressLinesIntervalWeeklyDay = BooleanArray(8)
    protected var m_progressLinesIntervalWeekleyWeekNumber: Int = 0
    protected var m_progressLinesIntervalMonthlyDay: Boolean = false
    protected var m_progressLinesIntervalMonthlyDayDayNumber: Int = 0
    /**
     * Retrieves the progress line month number for the monthly day type.
     *
     * @return month number
     */
    var progressLinesIntervalMonthlyDayMonthNumber: Int = 0
        protected set
    protected var m_progressLinesIntervalMonthlyFirstLastDay: ProgressLineDay? = null
    protected var m_progressLinesIntervalMonthlyFirstLast: Boolean = false
    /**
     * Retrieves the progress lines month number for the monthly first last type.
     *
     * @return month number
     */
    var progressLinesIntervalMonthlyFirstLastMonthNumber: Int = 0
        protected set
    protected var m_progressLinesBeginAtProjectStart: Boolean = false
    protected var m_progressLinesBeginAtDate: Date? = null
    protected var m_progressLinesDisplaySelected: Boolean = false
    protected var m_progressLinesDisplaySelectedDates: Array<Date>? = null
    protected var m_progressLinesActualPlan: Boolean = false
    protected var m_progressLinesDisplayType: Int = 0
    protected var m_progressLinesShowDate: Boolean = false
    protected var m_progressLinesDateFormat: Int = 0
    protected var m_progressLinesFontStyle: FontStyle? = null
    protected var m_progressLinesCurrentLineColor: Color? = null
    protected var m_progressLinesCurrentLineStyle: LineStyle? = null
    protected var m_progressLinesCurrentProgressPointColor: Color? = null
    protected var m_progressLinesCurrentProgressPointShape: Int = 0
    protected var m_progressLinesOtherLineColor: Color? = null
    protected var m_progressLinesOtherLineStyle: LineStyle? = null
    protected var m_progressLinesOtherProgressPointColor: Color? = null
    protected var m_progressLinesOtherProgressPointShape: Int = 0
    protected var m_autoFilters: List<Filter> = LinkedList<Filter>()
    protected var m_autoFiltersByType: Map<FieldType, Filter> = HashMap<FieldType, Filter>()

    private val m_filters: FilterContainer
    /**
     * Extract the Gantt bar styles.
     *
     * @param props props structure containing the style definitions
     */
    protected abstract fun processDefaultBarStyles(props: Props)

    /**
     * Extract the exception Gantt bar styles.
     *
     * @param props props structure containing the style definitions
     */
    protected abstract fun processExceptionBarStyles(props: Props)

    /**
     * Extract autofilter definitions.
     *
     * @param data autofilters data block
     */
    protected abstract fun processAutoFilters(data: ByteArray)

    /**
     * Extract view properties.
     *
     * @param fontBases font defintions
     * @param props Gantt chart view props
     */
    protected abstract fun processViewProperties(fontBases: Map<Integer, FontBase>, props: Props)

    /**
     * Extract table font styles.
     *
     * @param fontBases font bases
     * @param data column data
     */
    protected abstract fun processTableFontStyles(fontBases: Map<Integer, FontBase>, data: ByteArray)

    /**
     * Extract progress line properties.
     *
     * @param fontBases font bases
     * @param data column data
     */
    protected abstract fun processProgressLines(fontBases: Map<Integer, FontBase>, data: ByteArray)

    init {
        //      System.out.println(varData.getVarMeta());
        //      MPPUtility.fileDump("c:\\temp\\"+getName()+"-vardata.txt", varData.toString().getBytes());

        m_filters = parent.filters
        m_showInMenu = fixedMeta[8] and 0x08 != 0

        val propsData = varData.getByteArray(m_id, propertiesID)
        if (propsData != null) {
            val props = Props9(ByteArrayInputStream(propsData))
            //MPPUtility.fileDump("c:\\temp\\props.txt", props.toString().getBytes());

            val tableData = props.getByteArray(TABLE_PROPERTIES)
            if (tableData != null) {
                m_tableWidth = MPPUtility.getShort(tableData, 35)
                m_highlightFilter = tableData[7].toInt() != 0
            }

            val filterName = props.getByteArray(FILTER_NAME)
            if (filterName != null) {
                m_defaultFilterName = MPPUtility.getUnicodeString(filterName, 0)
            }

            val groupName = props.getByteArray(GROUP_NAME)
            if (groupName != null) {
                m_groupName = MPPUtility.getUnicodeString(groupName, 0)
            }

            processViewProperties(fontBases, props)

            processDefaultBarStyles(props)

            processExceptionBarStyles(props)

            val columnData = props.getByteArray(COLUMN_PROPERTIES)
            if (columnData != null) {
                processTableFontStyles(fontBases, columnData)
            }

            val progressLineData = props.getByteArray(PROGRESS_LINE_PROPERTIES)
            if (progressLineData != null) {
                processProgressLines(fontBases, progressLineData)
            }

            val autoFilterData = props.getByteArray(AUTO_FILTER_PROPERTIES)
            if (autoFilterData != null) {
                processAutoFilters(autoFilterData)
            }
        }

        //MPPUtility.fileDump("c:\\temp\\GanttChartView9.txt", toString().getBytes());
    }

    /**
     * This method maps the encoded height of a Gantt bar to
     * the height in pixels.
     *
     * @param height encoded height
     * @return height in pixels
     */
    protected fun mapGanttBarHeight(height: Int): Int {
        var height = height
        when (height) {
            0 -> {
                height = 6
            }

            1 -> {
                height = 8
            }

            2 -> {
                height = 10
            }

            3 -> {
                height = 12
            }

            4 -> {
                height = 14
            }

            5 -> {
                height = 18
            }

            6 -> {
                height = 24
            }
        }

        return height
    }

    /**
     * Retrieve font details from a block of property data.
     *
     * @param data property data
     * @param offset offset into property data
     * @param fontBases map of font bases
     * @return FontStyle instance
     */
    protected fun getFontStyle(data: ByteArray, offset: Int, fontBases: Map<Integer, FontBase>): FontStyle {
        val index = Integer.valueOf(MPPUtility.getByte(data, offset))
        val fontBase = fontBases[index]
        val style = MPPUtility.getByte(data, offset + 1)
        val color = ColorType.getInstance(MPPUtility.getByte(data, offset + 2))

        val bold = style and 0x01 != 0
        val italic = style and 0x02 != 0
        val underline = style and 0x04 != 0

//System.out.println(fontStyle);
        return FontStyle(fontBase, italic, bold, underline, false, color.color, null, BackgroundPattern.SOLID)
    }

    /**
     * Retrieve column font details from a block of property data.
     *
     * @param data property data
     * @param offset offset into property data
     * @param fontBases map of font bases
     * @return ColumnFontStyle instance
     */
    protected open fun getColumnFontStyle(data: ByteArray, offset: Int, fontBases: Map<Integer, FontBase>): TableFontStyle {
        val uniqueID = MPPUtility.getInt(data, offset)
        val fieldType = MPPTaskField.getInstance(MPPUtility.getShort(data, offset + 4))
        val index = Integer.valueOf(MPPUtility.getByte(data, offset + 8))
        val style = MPPUtility.getByte(data, offset + 9)
        val color = ColorType.getInstance(MPPUtility.getByte(data, offset + 10))
        val change = MPPUtility.getByte(data, offset + 12)

        val fontBase = fontBases[index]

        val bold = style and 0x01 != 0
        val italic = style and 0x02 != 0
        val underline = style and 0x04 != 0

        val boldChanged = change and 0x01 != 0
        val underlineChanged = change and 0x02 != 0
        val italicChanged = change and 0x04 != 0
        val colorChanged = change and 0x08 != 0
        val fontChanged = change and 0x10 != 0
        val backgroundColorChanged = uniqueID == -1
        val backgroundPatternChanged = uniqueID == -1

        return TableFontStyle(uniqueID, fieldType, fontBase, italic, bold, underline, false, color.color, Color.BLACK, BackgroundPattern.TRANSPARENT, italicChanged, boldChanged, underlineChanged, false, colorChanged, fontChanged, backgroundColorChanged, backgroundPatternChanged)
    }

    /**
     * Retrieves the auto filter definition associated with an
     * individual column. Returns null if there is no filter defined for
     * the supplied column type.
     *
     * @param type field type
     * @return filter instance
     */
    fun getAutoFilterByType(type: FieldType): Filter {
        return m_autoFiltersByType[type]
    }

    /**
     * Generate a string representation of this instance.
     *
     * @return string representation of this instance
     */
    @Override
    override fun toString(): String {
        val os = ByteArrayOutputStream()
        val pw = PrintWriter(os)
        pw.println("[GanttChartView")
        pw.println("   " + super.toString())

        pw.println("   highlightedTasksFontStyle=" + m_highlightedTasksFontStyle!!)
        pw.println("   rowAndColumnFontStyle=" + m_rowAndColumnFontStyle!!)
        pw.println("   nonCriticalTasksFontStyle=" + m_nonCriticalTasksFontStyle!!)
        pw.println("   criticalTasksFontStyle=" + m_criticalTasksFontStyle!!)
        pw.println("   summaryTasksFontStyle=" + m_summaryTasksFontStyle!!)
        pw.println("   milestoneTasksFontStyle=" + m_milestoneTasksFontStyle!!)
        pw.println("   topTimescaleFontStyle=" + m_topTimescaleFontStyle!!)
        pw.println("   middleTimescaleFontStyle=" + m_middleTimescaleFontStyle!!)
        pw.println("   bottomTimescaleFontStyle=" + m_bottomTimescaleFontStyle!!)
        pw.println("   barTextLeftFontStyle=" + m_barTextLeftFontStyle!!)
        pw.println("   barTextRightFontStyle=" + m_barTextRightFontStyle!!)
        pw.println("   barTextTopFontStyle=" + m_barTextTopFontStyle!!)
        pw.println("   barTextBottomFontStyle=" + m_barTextBottomFontStyle!!)
        pw.println("   barTextInsideFontStyle=" + m_barTextInsideFontStyle!!)
        pw.println("   markedTasksFontStyle=" + m_markedTasksFontStyle!!)
        pw.println("   projectSummaryTasksFontStyle=" + m_projectSummaryTasksFontStyle!!)
        pw.println("   externalTasksFontStyle=" + m_externalTasksFontStyle!!)

        pw.println("   SheetRowsGridLines=" + m_sheetRowsGridLines!!)
        pw.println("   SheetColumnsGridLines=" + m_sheetColumnsGridLines!!)
        pw.println("   TitleVerticalGridLines=" + m_titleVerticalGridLines!!)
        pw.println("   TitleHorizontalGridLines=" + m_titleHorizontalGridLines!!)
        pw.println("   TopTierColumnGridLines=" + m_topTierColumnGridLines!!)
        pw.println("   MiddleTierColumnGridLines=" + m_middleTierColumnGridLines!!)
        pw.println("   BottomTierColumnGridLines=" + m_bottomTierColumnGridLines!!)
        pw.println("   GanttRowsGridLines=" + m_ganttRowsGridLines!!)
        pw.println("   BarRowsGridLines=" + m_barRowsGridLines!!)
        pw.println("   CurrentDateGridLines=" + m_currentDateGridLines!!)
        pw.println("   PageBreakGridLines=" + m_pageBreakGridLines!!)
        pw.println("   ProjectStartGridLines=" + m_projectStartGridLines!!)
        pw.println("   ProjectFinishGridLines=" + m_projectFinishGridLines!!)
        pw.println("   StatusDateGridLines=" + m_statusDateGridLines!!)
        pw.println("   GanttBarHeight=$m_ganttBarHeight")
        pw.println("   TimescaleTopTier=" + m_timescaleTopTier!!)
        pw.println("   TimescaleMiddleTier=" + m_timescaleMiddleTier!!)
        pw.println("   TimescaleBottomTier=" + m_timescaleBottomTier!!)
        pw.println("   TimescaleSeparator=$m_timescaleScaleSeparator")
        pw.println("   TimescaleSize=$m_timescaleSize%")
        pw.println("   NonWorkingDaysCalendarName=" + m_nonWorkingDaysCalendarName!!)
        pw.println("   NonWorkingColor=" + m_nonWorkingColor!!)
        pw.println("   NonWorkingPattern=" + m_nonWorkingPattern!!)
        pw.println("   NonWorkingStyle=" + m_nonWorkingStyle!!)
        pw.println("   ShowDrawings=$m_showDrawings")
        pw.println("   RoundBarsToWholeDays=$m_roundBarsToWholeDays")
        pw.println("   ShowBarSplits=$m_showBarSplits")
        pw.println("   AlwaysRollupGanttBars=$m_alwaysRollupGanttBars")
        pw.println("   HideRollupBarsWhenSummaryExpanded=$m_hideRollupBarsWhenSummaryExpanded")
        pw.println("   BarDateFormat=" + m_barDateFormat!!)
        pw.println("   LinkStyle=" + m_linkStyle!!)

        pw.println("   ProgressLinesEnabled=$m_progressLinesEnabled")
        pw.println("   ProgressLinesAtCurrentDate=$m_progressLinesAtCurrentDate")
        pw.println("   ProgressLinesAtRecurringIntervals=$m_progressLinesAtRecurringIntervals")
        pw.println("   ProgressLinesInterval=" + m_progressLinesInterval!!)
        pw.println("   ProgressLinesDailyDayNumber=$m_progressLinesIntervalDailyDayNumber")
        pw.println("   ProgressLinesDailyWorkday=$m_progressLinesIntervalDailyWorkday")

        pw.print("   ProgressLinesWeeklyDay=[")
        for (loop in m_progressLinesIntervalWeeklyDay.indices) {
            if (loop != 0) {
                pw.print(",")
            }
            pw.print(m_progressLinesIntervalWeeklyDay[loop])
        }
        pw.println("]")

        pw.println("   ProgressLinesWeeklyWeekNumber=$m_progressLinesIntervalWeekleyWeekNumber")
        pw.println("   ProgressLinesMonthlyDayOfMonth=$m_progressLinesIntervalMonthlyDay")
        pw.println("   ProgressLinesMonthDayNumber=$m_progressLinesIntervalMonthlyDayDayNumber")
        pw.println("   ProgressLinesMonthlyDay=" + m_progressLinesIntervalMonthlyFirstLastDay!!)
        pw.println("   ProgressLinesMonthlyFirst=$m_progressLinesIntervalMonthlyFirstLast")
        pw.println("   ProgressLinesBeginAtProjectStart=$m_progressLinesBeginAtProjectStart")
        pw.println("   ProgressLinesBeginAtDate=" + m_progressLinesBeginAtDate!!)
        pw.println("   ProgressLinesDisplaySelected=$m_progressLinesDisplaySelected")

        pw.print("   ProgressLinesDisplaySelectedDates=[")
        if (m_progressLinesDisplaySelectedDates != null) {
            for (loop in m_progressLinesDisplaySelectedDates!!.indices) {
                if (loop != 0) {
                    pw.print(",")
                }
                pw.print(m_progressLinesDisplaySelectedDates!![loop])
            }
        }
        pw.println("]")

        pw.println("   ProgressLinesActualPlan=$m_progressLinesActualPlan")
        pw.println("   ProgressLinesDisplayType=$m_progressLinesDisplayType")
        pw.println("   ProgressLinesShowDate=$m_progressLinesShowDate")
        pw.println("   ProgressLinesDateFormat=$m_progressLinesDateFormat")
        pw.println("   ProgressLinesFontStyle=" + m_progressLinesFontStyle!!)
        pw.println("   ProgressLinesCurrentLineColor=" + m_progressLinesCurrentLineColor!!)
        pw.println("   ProgressLinesCurrentLineStyle=" + m_progressLinesCurrentLineStyle!!)
        pw.println("   ProgressLinesCurrentProgressPointColor=" + m_progressLinesCurrentProgressPointColor!!)
        pw.println("   ProgressLinesCurrentProgressPointShape=$m_progressLinesCurrentProgressPointShape")
        pw.println("   ProgressLinesOtherLineColor=" + m_progressLinesOtherLineColor!!)
        pw.println("   ProgressLinesOtherLineStyle=" + m_progressLinesOtherLineStyle!!)
        pw.println("   ProgressLinesOtherProgressPointColor=" + m_progressLinesOtherProgressPointColor!!)
        pw.println("   ProgressLinesOtherProgressPointShape=$m_progressLinesOtherProgressPointShape")

        pw.println("   TableWidth=$m_tableWidth")
        pw.println("   DefaultFilterName=" + m_defaultFilterName!!)
        pw.println("   GroupName=" + m_groupName!!)
        pw.println("   HighlightFilter=$m_highlightFilter")
        pw.println("   ShowInMenu=$m_showInMenu")

        if (m_tableFontStyles != null) {
            for (loop in m_tableFontStyles!!.indices) {
                pw.println("   ColumnFontStyle=" + m_tableFontStyles!![loop])
            }
        }

        if (barStyles != null) {
            for (loop in barStyles!!.indices) {
                pw.println("   BarStyle=" + barStyles!![loop])
            }
        }

        if (barStyleExceptions != null) {
            for (loop in barStyleExceptions!!.indices) {
                pw.println("   BarStyleException=" + barStyleExceptions!![loop])
            }
        }

        if (!m_autoFilters.isEmpty()) {
            for (f in m_autoFilters) {
                pw.println("   AutoFilter=$f")
            }
        }

        pw.println("]")
        pw.flush()
        return os.toString()
    }

    companion object {

        protected val VIEW_PROPERTIES = Integer.valueOf(574619656)
        protected val TIMESCALE_PROPERTIES = Integer.valueOf(574619678)
        private val TABLE_PROPERTIES = Integer.valueOf(574619655)
        private val FILTER_NAME = Integer.valueOf(574619659)
        private val GROUP_NAME = Integer.valueOf(574619672)
        private val COLUMN_PROPERTIES = Integer.valueOf(574619660)
        private val PROGRESS_LINE_PROPERTIES = Integer.valueOf(574619671)
        private val AUTO_FILTER_PROPERTIES = Integer.valueOf(574619669)
    }
}
