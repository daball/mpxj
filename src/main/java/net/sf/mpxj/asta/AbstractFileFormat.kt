/*
 * file:       AbstractFileFormat.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2016
 * date:       27/01/2016
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

package net.sf.mpxj.asta

import java.sql.Types
import java.util.HashMap

/**
 * Represents the structure of an Asta PP file.
 */
internal abstract class AbstractFileFormat {
    /**
     * Retrieves the table structure for an Asta PP file. Subclasses determine the exact contents of the structure
     * for a specific version of the Asta PP file.
     *
     * @return PP file table structure
     */
    fun tableDefinitions(): Map<Integer, TableDefinition> {
        val result = HashMap<Integer, TableDefinition>()

        result.put(Integer.valueOf(2), TableDefinition("PROJECT_SUMMARY", columnDefinitions(PROJECT_SUMMARY_COLUMNS, projectSummaryColumnsOrder())))
        result.put(Integer.valueOf(7), TableDefinition("BAR", columnDefinitions(BAR_COLUMNS, barColumnsOrder())))
        result.put(Integer.valueOf(11), TableDefinition("CALENDAR", columnDefinitions(CALENDAR_COLUMNS, calendarColumnsOrder())))
        result.put(Integer.valueOf(12), TableDefinition("EXCEPTIONN", columnDefinitions(EXCEPTIONN_COLUMNS, exceptionColumnsOrder())))
        result.put(Integer.valueOf(14), TableDefinition("EXCEPTION_ASSIGNMENT", columnDefinitions(EXCEPTION_ASSIGNMENT_COLUMNS, exceptionAssignmentColumnsOrder())))
        result.put(Integer.valueOf(15), TableDefinition("TIME_ENTRY", columnDefinitions(TIME_ENTRY_COLUMNS, timeEntryColumnsOrder())))
        result.put(Integer.valueOf(17), TableDefinition("WORK_PATTERN", columnDefinitions(WORK_PATTERN_COLUMNS, workPatternColumnsOrder())))
        result.put(Integer.valueOf(18), TableDefinition("TASK_COMPLETED_SECTION", columnDefinitions(TASK_COMPLETED_SECTION_COLUMNS, taskCompletedSectionColumnsOrder())))
        result.put(Integer.valueOf(21), TableDefinition("TASK", columnDefinitions(TASK_COLUMNS, taskColumnsOrder())))
        result.put(Integer.valueOf(22), TableDefinition("MILESTONE", columnDefinitions(MILESTONE_COLUMNS, milestoneColumnsOrder())))
        result.put(Integer.valueOf(23), TableDefinition("EXPANDED_TASK", columnDefinitions(EXPANDED_TASK_COLUMNS, expandedTaskColumnsOrder())))
        result.put(Integer.valueOf(25), TableDefinition("LINK", columnDefinitions(LINK_COLUMNS, linkColumnsOrder())))
        result.put(Integer.valueOf(61), TableDefinition("CONSUMABLE_RESOURCE", columnDefinitions(CONSUMABLE_RESOURCE_COLUMNS, consumableResourceColumnsOrder())))
        result.put(Integer.valueOf(62), TableDefinition("PERMANENT_RESOURCE", columnDefinitions(PERMANENT_RESOURCE_COLUMNS, permanentResourceColumnsOrder())))
        result.put(Integer.valueOf(63), TableDefinition("PERM_RESOURCE_SKILL", columnDefinitions(PERMANENT_RESOURCE_SKILL_COLUMNS, permanentResourceSkillColumnsOrder())))
        result.put(Integer.valueOf(67), TableDefinition("PERMANENT_SCHEDUL_ALLOCATION", columnDefinitions(PERMANENT_SCHEDULE_ALLOCATION_COLUMNS, permanentScheduleAllocationColumnsOrder())))
        result.put(Integer.valueOf(190), TableDefinition("WBS_ENTRY", columnDefinitions(WBS_ENTRY_COLUMNS, wbsEntryColumnsOrder())))

        return result
    }

    /**
     * Indicates if dates are encoded as integer offsets from an epoch date (true),
     * or as simple numeric date formats (false).
     *
     * @return epoch date format flag
     */
    abstract fun epochDateFormat(): Boolean

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun barColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun calendarColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun consumableResourceColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun exceptionColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun exceptionAssignmentColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun expandedTaskColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun linkColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun milestoneColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun permanentResourceSkillColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun permanentResourceColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun permanentScheduleAllocationColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun projectSummaryColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun taskColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun timeEntryColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun workPatternColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun wbsEntryColumnsOrder(): Array<String>

    /**
     * Ordered column names for a table.
     *
     * @return ordered column names
     */
    protected abstract fun taskCompletedSectionColumnsOrder(): Array<String>

    /**
     * Generate an ordered set of column definitions from an ordered set of column names.
     *
     * @param columns column definitions
     * @param order column names
     * @return ordered set of column definitions
     */
    private fun columnDefinitions(columns: Array<ColumnDefinition>, order: Array<String>): Array<ColumnDefinition> {
        val map = makeColumnMap(columns)
        val result = arrayOfNulls<ColumnDefinition>(order.size)
        for (index in order.indices) {
            result[index] = map[order[index]]
        }
        return result
    }

    /**
     * Convert an array of column definitions into a map keyed by column name.
     *
     * @param columns array of column definitions
     * @return map of column definitions
     */
    private fun makeColumnMap(columns: Array<ColumnDefinition>): Map<String, ColumnDefinition> {
        val map = HashMap<String, ColumnDefinition>()
        for (def in columns) {
            map.put(def.name, def)
        }
        return map
    }

    companion object {

        private val PROJECT_SUMMARY_COLUMNS = arrayOf(ColumnDefinition("PROJECT_SUMMARYID", Types.INTEGER), ColumnDefinition("DURATIONTYPF", Types.INTEGER), ColumnDefinition("DURATIONELA_MONTHS", Types.INTEGER), ColumnDefinition("DURATIONHOURS", Types.DOUBLE), ColumnDefinition("STARU", Types.TIMESTAMP), ColumnDefinition("ENE", Types.TIMESTAMP), ColumnDefinition("FISCAL_YEAR_START", Types.TIMESTAMP), ColumnDefinition("LAST_ID_USED_IN_BASELINE", Types.INTEGER), ColumnDefinition("DS_ID_BOOKED_FROM", Types.INTEGER), ColumnDefinition("WBN_CONSTRAINT", Types.INTEGER), ColumnDefinition("WBN_RANGE_FROM", Types.INTEGER), ColumnDefinition("WBN_RANGE_TO", Types.INTEGER), ColumnDefinition("WBN_INCREMENT", Types.INTEGER), ColumnDefinition("WBN_MINIMUM_WIDTH", Types.INTEGER), ColumnDefinition("SPARF_INTEGER", Types.INTEGER), ColumnDefinition("UTID_CONSTRAINT", Types.INTEGER), ColumnDefinition("UTID_START_VALUE", Types.INTEGER), ColumnDefinition("UTID_INCREMENT", Types.INTEGER), ColumnDefinition("UTID_SUB_INCREMENT", Types.INTEGER), ColumnDefinition("UTID_MINIMUM_WIDTH", Types.INTEGER), ColumnDefinition("INITIAL_VIEW", Types.INTEGER), ColumnDefinition("POINT_RELEASE", Types.INTEGER), ColumnDefinition("TIMESHEET_PROJECT_ID", Types.INTEGER), ColumnDefinition("LAST_ID_USED_IN_ARCHIVES", Types.INTEGER), ColumnDefinition("PROJECT_VERSION", Types.INTEGER), ColumnDefinition("STANDARD_WORK_MIN_FADE", Types.INTEGER), ColumnDefinition("BOOKOUT_SET_UNIQUE_ID", Types.INTEGER), ColumnDefinition("NUMBER_BOOKED_OUT_SETS", Types.INTEGER), ColumnDefinition("SHORT_NAME", Types.VARCHAR), ColumnDefinition("LONG_NAME", Types.VARCHAR), ColumnDefinition("LOCAL_FILE_BOOKED_FROM", Types.VARCHAR), ColumnDefinition("WBN_START_VALUE", Types.VARCHAR), ColumnDefinition("WBN_PATHNAME_SEPARATOR", Types.VARCHAR), ColumnDefinition("WBN_TASK_SEPARATOR", Types.VARCHAR), ColumnDefinition("WBN_PREFIX", Types.VARCHAR), ColumnDefinition("LAST_WBN_USED", Types.VARCHAR), ColumnDefinition("PROJECT_FOR", Types.VARCHAR), ColumnDefinition("PROJECT_BY", Types.VARCHAR), ColumnDefinition("PATH_SEPARATOR", Types.VARCHAR), ColumnDefinition("CHART_PATH_SEPARATOR", Types.VARCHAR), ColumnDefinition("UTID_PREFIX", Types.VARCHAR), ColumnDefinition("TIMESHEET_CONNECTION", Types.VARCHAR), ColumnDefinition("WBS_PATH_SEPARATOR", Types.VARCHAR), ColumnDefinition("PROJECT_GUID", Types.VARCHAR), ColumnDefinition("DURATION_TIME_UNIT", Types.INTEGER), ColumnDefinition("SECURITY_CODELIBRARY", Types.INTEGER), ColumnDefinition("BOOKOUT_COUNTER", Types.INTEGER), ColumnDefinition("PROGRESS_METHOD", Types.INTEGER), ColumnDefinition("FORMULA_DATE_FORMAT", Types.INTEGER), ColumnDefinition("WBN_ENABLED", Types.BIT), ColumnDefinition("OLD_START_VALUE", Types.BIT), ColumnDefinition("IGNORE_SATISFIED_COSTS", Types.BIT), ColumnDefinition("UTID_ENABLE_SUB_INCREMENTS", Types.BIT), ColumnDefinition("EXCLUSIVE_CUSTOM_TIME_UNITS", Types.BIT), ColumnDefinition("IS_AN_ARCHIVE", Types.BIT), ColumnDefinition("SORT_BY_SORT_ORDER", Types.BIT), ColumnDefinition("USE_PROJECT_BASELINES_FOR_JP", Types.BIT), ColumnDefinition("USE_ROLLED_UP_OPC_WEIGHTINGS", Types.BIT), ColumnDefinition("DISPLAY_WBS_BY_CODE", Types.BIT), ColumnDefinition("INHERIT_FROM_NEIGHBOUR", Types.BIT), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER), ColumnDefinition("SCALE_SPR_FONTS_CONSISTENTLY", Types.INTEGER))

        private val BAR_COLUMNS = arrayOf(ColumnDefinition("BARID", Types.INTEGER), ColumnDefinition("STARV", Types.TIMESTAMP), ColumnDefinition("ENF", Types.TIMESTAMP), ColumnDefinition("NATURAL_ORDER", Types.INTEGER), ColumnDefinition("SPARI_INTEGER", Types.INTEGER), ColumnDefinition("NAMH", Types.VARCHAR), ColumnDefinition("EXPANDED_TASK", Types.INTEGER), ColumnDefinition("PRIORITY", Types.INTEGER), ColumnDefinition("UNSCHEDULABLE", Types.BIT), ColumnDefinition("MARK_FOR_HIDING", Types.BIT), ColumnDefinition("TASKS_MAY_OVERLAP", Types.BIT), ColumnDefinition("SUBPROJECT_ID", Types.INTEGER), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER))// Followed by user defined columns which differ by project

        private val CALENDAR_COLUMNS = arrayOf(ColumnDefinition("CALENDARID", Types.INTEGER), ColumnDefinition("SPARL_INTEGER", Types.INTEGER), ColumnDefinition("NAMK", Types.VARCHAR), ColumnDefinition("DOMINANT_WORK_PATTERN", Types.INTEGER), ColumnDefinition("CALENDAR", Types.INTEGER), ColumnDefinition("DISPLAY_THRESHOLD", Types.INTEGER), ColumnDefinition("NO_WORKING_TIME_COLOUR", Types.INTEGER), ColumnDefinition("WORKING_TIME_COLOUR", Types.INTEGER), ColumnDefinition("NUMBERING", Types.INTEGER), ColumnDefinition("SHOW_PAST_DATES", Types.BIT), ColumnDefinition("ISO8601_WEEK_NUMBERING", Types.BIT), ColumnDefinition("CREATED_AS_FOLDER", Types.BIT), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER))

        private val EXCEPTIONN_COLUMNS = arrayOf(ColumnDefinition("EXCEPTIONNID", Types.INTEGER), ColumnDefinition("ARR_STOUT_STAPPANDARROW_TYPE", Types.INTEGER), ColumnDefinition("ARR_STOUT_STAPPANDLENGTH", Types.INTEGER), ColumnDefinition("ARR_STOUT_STAPPANDEDGE", Types.INTEGER), ColumnDefinition("ARR_STOUT_STAPPANDBORDET_COL", Types.INTEGER), ColumnDefinition("ARR_STOUT_STAPPANDINSIDG_COL", Types.INTEGER), ColumnDefinition("ARR_STOUT_STAPPANDPLACEMENW", Types.INTEGER), ColumnDefinition("BLI_STOUT_STAPPANDBLIP_TYPE", Types.INTEGER), ColumnDefinition("BLI_STOUT_STAPPANDSCALEY", Types.INTEGER), ColumnDefinition("BLI_STOUT_STAPPANDSCALEZ", Types.INTEGER), ColumnDefinition("BLI_STOUT_STAPPANDGAP", Types.INTEGER), ColumnDefinition("BLI_STOUT_STAPPANDBORDES_COL", Types.INTEGER), ColumnDefinition("BLI_STOUT_STAPPANDINSIDF_COL", Types.INTEGER), ColumnDefinition("BLI_STOUT_STAPPANDPLACEMENV", Types.INTEGER), ColumnDefinition("LIN_STOUT_STAPPANDSCALEX", Types.DOUBLE), ColumnDefinition("LIN_STOUT_STAPPANDWIDTH", Types.INTEGER), ColumnDefinition("LIN_STOUT_STAPPANDBORDER_COL", Types.INTEGER), ColumnDefinition("LIN_STOUT_STAPPANDINSIDE_COL", Types.INTEGER), ColumnDefinition("LIN_STOUT_STAPPANDLINE_TYPE", Types.INTEGER), ColumnDefinition("APPANDFOREGROUND_FILL_COLOUR", Types.INTEGER), ColumnDefinition("APPANDBACKGROUND_FILL_COLOUR", Types.INTEGER), ColumnDefinition("APPANDPATTERN", Types.INTEGER), ColumnDefinition("UNIQUE_BIT_FIELD", Types.INTEGER), ColumnDefinition("NAML", Types.VARCHAR), ColumnDefinition("TYPG", Types.INTEGER), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER), ColumnDefinition("SORT_ORDER", Types.INTEGER))

        private val EXCEPTION_ASSIGNMENT_COLUMNS = arrayOf(ColumnDefinition("EXCEPTION_ASSIGNMENTID", Types.INTEGER),
                //new ColumnDefinition("ORDF", Types.INTEGER),
                ColumnDefinition("STARU_DATE", Types.TIMESTAMP), ColumnDefinition("ENE_DATE", Types.TIMESTAMP), ColumnDefinition("EXCEPTIOO", Types.INTEGER))

        private val TIME_ENTRY_COLUMNS = arrayOf(ColumnDefinition("TIME_ENTRYID", Types.INTEGER), ColumnDefinition("EXCEPTIOP", Types.INTEGER), ColumnDefinition("START_TIME", Types.TIME), ColumnDefinition("END_TIME", Types.TIME))

        private val WORK_PATTERN_COLUMNS = arrayOf(ColumnDefinition("WORK_PATTERNID", Types.INTEGER), ColumnDefinition("DEFAULT_OFFSET", Types.INTEGER), ColumnDefinition("NAMN", Types.VARCHAR), ColumnDefinition("DEFAULT_ALIGNMENT_DATE", Types.TIMESTAMP), ColumnDefinition("CREATED_AS_FOLDER", Types.BIT), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER))

        private val TASK_COLUMNS = arrayOf(ColumnDefinition("TASKID", Types.INTEGER), ColumnDefinition("GIVEN_DURATIONTYPF", Types.INTEGER), ColumnDefinition("GIVEN_DURATIONELA_MONTHS", Types.INTEGER), ColumnDefinition("GIVEN_DURATIONHOURS", Types.DOUBLE), ColumnDefinition("RESUME", Types.TIMESTAMP), ColumnDefinition("GIVEN_START", Types.TIMESTAMP), ColumnDefinition("LATEST_PROGRESS_PERIOD", Types.INTEGER), ColumnDefinition("TASK_WORK_RATE_TIME_UNIT", Types.INTEGER), ColumnDefinition("TASK_WORK_RATE", Types.DOUBLE), ColumnDefinition("PLACEMENT", Types.INTEGER), ColumnDefinition("BEEN_SPLIT", Types.BIT), ColumnDefinition("INTERRUPTIBLE", Types.BIT), ColumnDefinition("HOLDING_PIN", Types.BIT), ColumnDefinition("ACTUAL_DURATIONTYPF", Types.INTEGER), ColumnDefinition("ACTUAL_DURATIONELA_MONTHS", Types.INTEGER), ColumnDefinition("ACTUAL_DURATIONHOURS", Types.DOUBLE), ColumnDefinition("EARLY_START_DATE", Types.TIMESTAMP), ColumnDefinition("LATE_START_DATE", Types.TIMESTAMP), ColumnDefinition("FREE_START_DATE", Types.TIMESTAMP), ColumnDefinition("START_CONSTRAINT_DATE", Types.TIMESTAMP), ColumnDefinition("END_CONSTRAINT_DATE", Types.TIMESTAMP), ColumnDefinition("EFFORT_BUDGET", Types.DOUBLE), ColumnDefinition("NATURAO_ORDER", Types.INTEGER), ColumnDefinition("LOGICAL_PRECEDENCE", Types.INTEGER), ColumnDefinition("SPAVE_INTEGER", Types.INTEGER), ColumnDefinition("SWIM_LANE", Types.INTEGER), ColumnDefinition("USER_PERCENT_COMPLETE", Types.DOUBLE), ColumnDefinition("OVERALL_PERCENV_COMPLETE", Types.DOUBLE), ColumnDefinition("OVERALL_PERCENT_COMPL_WEIGHT", Types.DOUBLE), ColumnDefinition("NARE", Types.VARCHAR), ColumnDefinition("WBN_CODE", Types.VARCHAR), ColumnDefinition("NOTET", Types.LONGVARCHAR), ColumnDefinition("UNIQUE_TASK_ID", Types.VARCHAR), ColumnDefinition("CALENDAU", Types.INTEGER), ColumnDefinition("WBT", Types.INTEGER), ColumnDefinition("EFFORT_TIMI_UNIT", Types.INTEGER), ColumnDefinition("WORL_UNIT", Types.INTEGER), ColumnDefinition("LATEST_ALLOC_PROGRESS_PERIOD", Types.INTEGER), ColumnDefinition("WORN", Types.DOUBLE), ColumnDefinition("BAR", Types.INTEGER), ColumnDefinition("CONSTRAINU", Types.INTEGER), ColumnDefinition("PRIORITB", Types.INTEGER), ColumnDefinition("CRITICAM", Types.BIT), ColumnDefinition("USE_PARENU_CALENDAR", Types.BIT), ColumnDefinition("BUFFER_TASK", Types.BIT), ColumnDefinition("MARK_FOS_HIDING", Types.BIT), ColumnDefinition("OWNED_BY_TIMESHEEV_X", Types.BIT), ColumnDefinition("START_ON_NEX_DAY", Types.BIT), ColumnDefinition("LONGEST_PATH", Types.BIT), ColumnDefinition("DURATIOTTYPF", Types.INTEGER), ColumnDefinition("DURATIOTELA_MONTHS", Types.INTEGER), ColumnDefinition("DURATIOTHOURS", Types.DOUBLE), ColumnDefinition("STARZ", Types.TIMESTAMP), ColumnDefinition("ENJ", Types.TIMESTAMP), ColumnDefinition("DURATION_TIMJ_UNIT", Types.INTEGER), ColumnDefinition("UNSCHEDULABLG", Types.BIT), ColumnDefinition("SUBPROJECT_ID", Types.INTEGER), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER), ColumnDefinition("IFC_PRODUCT_SET", Types.INTEGER), ColumnDefinition("IFC_TASK_TYPE", Types.INTEGER))

        private val MILESTONE_COLUMNS = arrayOf(ColumnDefinition("MILESTONEID", Types.INTEGER), ColumnDefinition("GIVEN_DATE_TIME", Types.TIMESTAMP), ColumnDefinition("PROGREST_PERIOD", Types.INTEGER), ColumnDefinition("SYMBOL_APPEARANCE", Types.INTEGER), ColumnDefinition("MILESTONE_TYPE", Types.INTEGER), ColumnDefinition("PLACEMENU", Types.INTEGER), ColumnDefinition("COMPLETED", Types.BIT), ColumnDefinition("INTERRUPTIBLE_X", Types.BIT), ColumnDefinition("ACTUAL_DURATIONTYPF", Types.INTEGER), ColumnDefinition("ACTUAL_DURATIONELA_MONTHS", Types.INTEGER), ColumnDefinition("ACTUAL_DURATIONHOURS", Types.DOUBLE), ColumnDefinition("EARLY_START_DATE", Types.TIMESTAMP), ColumnDefinition("LATE_START_DATE", Types.TIMESTAMP), ColumnDefinition("FREE_START_DATE", Types.TIMESTAMP), ColumnDefinition("START_CONSTRAINT_DATE", Types.TIMESTAMP), ColumnDefinition("END_CONSTRAINT_DATE", Types.TIMESTAMP), ColumnDefinition("EFFORT_BUDGET", Types.DOUBLE), ColumnDefinition("NATURAO_ORDER", Types.INTEGER), ColumnDefinition("LOGICAL_PRECEDENCE", Types.INTEGER), ColumnDefinition("SPAVE_INTEGER", Types.INTEGER), ColumnDefinition("SWIM_LANE", Types.INTEGER), ColumnDefinition("USER_PERCENT_COMPLETE", Types.DOUBLE), ColumnDefinition("OVERALL_PERCENV_COMPLETE", Types.DOUBLE), ColumnDefinition("OVERALL_PERCENT_COMPL_WEIGHT", Types.DOUBLE), ColumnDefinition("NARE", Types.VARCHAR), ColumnDefinition("WBN_CODE", Types.VARCHAR), ColumnDefinition("NOTET", Types.LONGVARCHAR), ColumnDefinition("UNIQUE_TASK_ID", Types.VARCHAR), ColumnDefinition("CALENDAU", Types.INTEGER), ColumnDefinition("WBT", Types.INTEGER), ColumnDefinition("EFFORT_TIMI_UNIT", Types.INTEGER), ColumnDefinition("WORL_UNIT", Types.INTEGER), ColumnDefinition("LATEST_ALLOC_PROGRESS_PERIOD", Types.INTEGER), ColumnDefinition("WORN", Types.DOUBLE), ColumnDefinition("BAR", Types.INTEGER), ColumnDefinition("CONSTRAINU", Types.INTEGER), ColumnDefinition("PRIORITB", Types.INTEGER), ColumnDefinition("CRITICAM", Types.BIT), ColumnDefinition("USE_PARENU_CALENDAR", Types.BIT), ColumnDefinition("BUFFER_TASK", Types.BIT), ColumnDefinition("MARK_FOS_HIDING", Types.BIT), ColumnDefinition("OWNED_BY_TIMESHEEV_X", Types.BIT), ColumnDefinition("START_ON_NEX_DAY", Types.BIT), ColumnDefinition("LONGEST_PATH", Types.BIT), ColumnDefinition("DURATIOTTYPF", Types.INTEGER), ColumnDefinition("DURATIOTELA_MONTHS", Types.INTEGER), ColumnDefinition("DURATIOTHOURS", Types.DOUBLE), ColumnDefinition("STARZ", Types.TIMESTAMP), ColumnDefinition("ENJ", Types.TIMESTAMP), ColumnDefinition("DURATION_TIMJ_UNIT", Types.INTEGER), ColumnDefinition("UNSCHEDULABLG", Types.BIT), ColumnDefinition("SUBPROJECT_ID", Types.INTEGER), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER), ColumnDefinition("IFC_PRODUCT_SET", Types.INTEGER), ColumnDefinition("IFC_TASK_TYPE", Types.INTEGER))

        private val EXPANDED_TASK_COLUMNS = arrayOf(ColumnDefinition("EXPANDED_TASKID", Types.INTEGER), ColumnDefinition("VAR_DATE1COMM_ATTSFIXED_DATE", Types.TIMESTAMP), ColumnDefinition("VAR_DATE1COMM_ATTSBASE_DATE", Types.INTEGER), ColumnDefinition("VAR_DATE2COMM_ATTSFIXED_DATE", Types.TIMESTAMP), ColumnDefinition("VAR_DATE2COMM_ATTSBASE_DATE", Types.INTEGER), ColumnDefinition("VAR_DATE3COMM_ATTSFIXED_DATE", Types.TIMESTAMP), ColumnDefinition("VAR_DATE3COMM_ATTSBASE_DATE", Types.INTEGER), ColumnDefinition("COMM_ATTSSCALE1", Types.DOUBLE), ColumnDefinition("COMM_ATTSSCALE2", Types.DOUBLE), ColumnDefinition("COMM_ATTSSCALE3", Types.DOUBLE), ColumnDefinition("COMM_ATTSNSCALES", Types.INTEGER), ColumnDefinition("PERCENTAGE_LIKELIHOOD", Types.DOUBLE), ColumnDefinition("PROJ_RISK", Types.DOUBLE), ColumnDefinition("PROJ_PRIORITY", Types.DOUBLE), ColumnDefinition("SUM_WEIGHTS", Types.DOUBLE), ColumnDefinition("ISSUE_DATE", Types.TIMESTAMP), ColumnDefinition("REVISION_DATE", Types.TIMESTAMP), ColumnDefinition("PROJECT_BASELINE_ID", Types.INTEGER), ColumnDefinition("DRAWN_BY", Types.VARCHAR), ColumnDefinition("REVISION_COMMENT", Types.VARCHAR), ColumnDefinition("CHART_MANAGER", Types.VARCHAR), ColumnDefinition("REVISION_NUMBER", Types.VARCHAR), ColumnDefinition("PROGRAMME_NUMBER", Types.VARCHAR), ColumnDefinition("COMMENU", Types.VARCHAR), ColumnDefinition("PROJ_TYPE", Types.VARCHAR), ColumnDefinition("PROJ_STATUS", Types.VARCHAR), ColumnDefinition("PROGRESU_PERIOD", Types.INTEGER), ColumnDefinition("MANAGER_RESOURCE", Types.INTEGER), ColumnDefinition("TYPH", Types.INTEGER), ColumnDefinition("TAG_FIELD", Types.INTEGER), ColumnDefinition("IS_PROJECT", Types.BIT), ColumnDefinition("CONTAINS_PROJECTS", Types.BIT), ColumnDefinition("CUMULATIVH_COSTCURRENCZ", Types.INTEGER), ColumnDefinition("CUMULATIVH_COSTAMOUNT", Types.DOUBLE), ColumnDefinition("CUMULATIVH_INCOMECURRENCZ", Types.INTEGER), ColumnDefinition("CUMULATIVH_INCOMEAMOUNT", Types.DOUBLE), ColumnDefinition("CUMULATIVE_ACTU_COSTCURRENCZ", Types.INTEGER), ColumnDefinition("CUMULATIVE_ACTU_COSTAMOUNT", Types.DOUBLE), ColumnDefinition("CUMULATIV_DURATIONTYPF", Types.INTEGER), ColumnDefinition("CUMULATIV_DURATIONELA_MONTHS", Types.INTEGER), ColumnDefinition("CUMULATIV_DURATIONHOURS", Types.DOUBLE), ColumnDefinition("ACTUAL_CU_DURATIONTYPF", Types.INTEGER), ColumnDefinition("ACTUAL_CU_DURATIONELA_MONTHS", Types.INTEGER), ColumnDefinition("ACTUAL_CU_DURATIONHOURS", Types.DOUBLE), ColumnDefinition("ACTUAL_CUMULATIVE_QUANTITY", Types.DOUBLE), ColumnDefinition("CUMULATIVE_QUANTIT_REMAINING", Types.DOUBLE), ColumnDefinition("CUMULATIVE_EFFORT_P_COMPLETE", Types.DOUBLE), ColumnDefinition("CUMULATIVE_WORK_PER_COMPLETE", Types.DOUBLE), ColumnDefinition("CUMULATIVE_QUANTITY_COMPLETE", Types.DOUBLE), ColumnDefinition("MILESTONE_PERCENT_COMPLETE", Types.DOUBLE), ColumnDefinition("FIRST_PREFERRED_START", Types.TIMESTAMP), ColumnDefinition("CALCULATED_PROGRESS_DATE", Types.TIMESTAMP), ColumnDefinition("EARLIEST_PROGRESS_DATE", Types.TIMESTAMP), ColumnDefinition("LATEST_PROGRESS_DATE", Types.TIMESTAMP), ColumnDefinition("EARLY_END_DATE_RT", Types.TIMESTAMP), ColumnDefinition("LATE_END_DATE_RT", Types.TIMESTAMP), ColumnDefinition("FREE_END_DATE_RT", Types.TIMESTAMP), ColumnDefinition("CUMULATIVE_DEMANE_EFFORT", Types.DOUBLE), ColumnDefinition("CUMULATIVE_SCHEDULEE_EFFORT", Types.DOUBLE), ColumnDefinition("ACTUAL_CUMULATIVF_EFFORT", Types.DOUBLE), ColumnDefinition("CUMULATIVE_EFFORU_REMAINING", Types.DOUBLE), ColumnDefinition("ACTUAL_CUMULATIVE_WORK", Types.DOUBLE), ColumnDefinition("CUMULATIVE_WORK_REMAINING", Types.DOUBLE), ColumnDefinition("MILESTONES_DONE", Types.INTEGER), ColumnDefinition("MILESTONES_REMAINING", Types.INTEGER), ColumnDefinition("CUMULATIVE_EFFORT_TIME_UNIT", Types.INTEGER), ColumnDefinition("CUMULATIVE_LATEST_PRO_PERIOD", Types.INTEGER), ColumnDefinition("ACTUAL_DURATIONTYPF", Types.INTEGER), ColumnDefinition("ACTUAL_DURATIONELA_MONTHS", Types.INTEGER), ColumnDefinition("ACTUAL_DURATIONHOURS", Types.DOUBLE), ColumnDefinition("EARLY_START_DATE", Types.TIMESTAMP), ColumnDefinition("LATE_START_DATE", Types.TIMESTAMP), ColumnDefinition("FREE_START_DATE", Types.TIMESTAMP), ColumnDefinition("START_CONSTRAINT_DATE", Types.TIMESTAMP), ColumnDefinition("END_CONSTRAINT_DATE", Types.TIMESTAMP), ColumnDefinition("EFFORT_BUDGET", Types.DOUBLE), ColumnDefinition("NATURAO_ORDER", Types.INTEGER), ColumnDefinition("LOGICAL_PRECEDENCE", Types.INTEGER), ColumnDefinition("SPAVE_INTEGER", Types.INTEGER), ColumnDefinition("SWIM_LANE", Types.INTEGER), ColumnDefinition("USER_PERCENT_COMPLETE", Types.DOUBLE), ColumnDefinition("OVERALL_PERCENV_COMPLETE", Types.DOUBLE), ColumnDefinition("OVERALL_PERCENT_COMPL_WEIGHT", Types.DOUBLE), ColumnDefinition("NARE", Types.VARCHAR), ColumnDefinition("WBN_CODE", Types.VARCHAR), ColumnDefinition("NOTET", Types.LONGVARCHAR), ColumnDefinition("UNIQUE_TASK_ID", Types.VARCHAR), ColumnDefinition("CALENDAU", Types.INTEGER), ColumnDefinition("WBT", Types.INTEGER), ColumnDefinition("EFFORT_TIMI_UNIT", Types.INTEGER), ColumnDefinition("WORL_UNIT", Types.INTEGER), ColumnDefinition("LATEST_ALLOC_PROGRESS_PERIOD", Types.INTEGER), ColumnDefinition("WORN", Types.DOUBLE), ColumnDefinition("BAR", Types.INTEGER), ColumnDefinition("CONSTRAINU", Types.INTEGER), ColumnDefinition("PRIORITB", Types.INTEGER), ColumnDefinition("CRITICAM", Types.BIT), ColumnDefinition("USE_PARENU_CALENDAR", Types.BIT), ColumnDefinition("BUFFER_TASK", Types.BIT), ColumnDefinition("MARK_FOS_HIDING", Types.BIT), ColumnDefinition("OWNED_BY_TIMESHEEV_X", Types.BIT), ColumnDefinition("START_ON_NEX_DAY", Types.BIT), ColumnDefinition("LONGEST_PATH", Types.BIT), ColumnDefinition("DURATIOTTYPF", Types.INTEGER), ColumnDefinition("DURATIOTELA_MONTHS", Types.INTEGER), ColumnDefinition("DURATIOTHOURS", Types.DOUBLE), ColumnDefinition("STARZ", Types.TIMESTAMP), ColumnDefinition("ENJ", Types.TIMESTAMP), ColumnDefinition("DURATION_TIMJ_UNIT", Types.INTEGER), ColumnDefinition("UNSCHEDULABLG", Types.BIT), ColumnDefinition("SUBPROJECT_ID", Types.INTEGER), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER), ColumnDefinition("NUMBER_OF_ACTIVITIES", Types.INTEGER), ColumnDefinition("ONLY_PM_MAY_APPROVE", Types.BIT), ColumnDefinition("IFC_PRODUCT_SET", Types.INTEGER), ColumnDefinition("IFC_TASK_TYPE", Types.INTEGER))

        private val LINK_COLUMNS = arrayOf(ColumnDefinition("LINKID", Types.INTEGER), ColumnDefinition("START_LAG_TIMETYPF", Types.INTEGER), ColumnDefinition("START_LAG_TIMEELA_MONTHS", Types.INTEGER), ColumnDefinition("START_LAG_TIMEHOURS", Types.DOUBLE), ColumnDefinition("END_LAG_TIMETYPF", Types.INTEGER), ColumnDefinition("END_LAG_TIMEELA_MONTHS", Types.INTEGER), ColumnDefinition("END_LAG_TIMEHOURS", Types.DOUBLE), ColumnDefinition("MAXIMUM_LAGTYPF", Types.INTEGER), ColumnDefinition("MAXIMUM_LAGELA_MONTHS", Types.INTEGER), ColumnDefinition("MAXIMUM_LAGHOURS", Types.DOUBLE), ColumnDefinition("STARV_DATE", Types.TIMESTAMP), ColumnDefinition("ENF_DATE", Types.TIMESTAMP), ColumnDefinition("CURVATURE_PERCENTAGE", Types.INTEGER), ColumnDefinition("START_LAG_PERCENT_FLOAT", Types.DOUBLE), ColumnDefinition("END_LAG_PERCENT_FLOAT", Types.DOUBLE), ColumnDefinition("COMMENTS", Types.VARCHAR), ColumnDefinition("LINK_CATEGORY", Types.INTEGER), ColumnDefinition("START_LAG_TIME_UNIT", Types.INTEGER), ColumnDefinition("END_LAG_TIME_UNIT", Types.INTEGER), ColumnDefinition("MAXIMUM_LAG_TIME_UNIT", Types.INTEGER), ColumnDefinition("START_TASK", Types.INTEGER), ColumnDefinition("END_TASK", Types.INTEGER), ColumnDefinition("TYPI", Types.INTEGER), ColumnDefinition("START_LAG_TYPE", Types.INTEGER), ColumnDefinition("END_LAG_TYPE", Types.INTEGER), ColumnDefinition("MAINTAIN_TASK_OFFSETS", Types.INTEGER), ColumnDefinition("UNSCHEDULABLF", Types.BIT), ColumnDefinition("CRITICAL", Types.BIT), ColumnDefinition("ON_LOOP", Types.BIT), ColumnDefinition("MAXIMUM_LAG_MODE", Types.BIT), ColumnDefinition("ANNOTATE_LEAD_LAG", Types.BIT), ColumnDefinition("START_REPOSITION_ON_TAS_MOVE", Types.BIT), ColumnDefinition("END_REPOSITION_ON_TASK_MOVE", Types.BIT), ColumnDefinition("DRAW_CURVED_IF_VERTICAL", Types.BIT), ColumnDefinition("AUTOMATIC_CURVED_LI_SETTINGS", Types.BIT), ColumnDefinition("DRAW_CURVED_LINK_TO_LEFT", Types.BIT), ColumnDefinition("LOCAL_LINK", Types.BIT), ColumnDefinition("DRIVING", Types.BIT), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER))

        private val CONSUMABLE_RESOURCE_COLUMNS = arrayOf(ColumnDefinition("CONSUMABLE_RESOURCEID", Types.INTEGER), ColumnDefinition("COST_PER_UNITCURRENCZ", Types.INTEGER), ColumnDefinition("COST_PER_UNITAMOUNT", Types.DOUBLE), ColumnDefinition("INCOME_PER_UNITCURRENCZ", Types.INTEGER), ColumnDefinition("INCOME_PER_UNITAMOUNT", Types.DOUBLE), ColumnDefinition("COST_PER_USEDEFAULTSCURRENCZ", Types.INTEGER), ColumnDefinition("COST_PER_USEDEFAULTSAMOUNT", Types.DOUBLE), ColumnDefinition("INCOME_P_USEDEFAULTSCURRENCZ", Types.INTEGER), ColumnDefinition("INCOME_P_USEDEFAULTSAMOUNT", Types.DOUBLE), ColumnDefinition("DURATIOPDEFAULTSTYPF", Types.INTEGER), ColumnDefinition("DURATIOPDEFAULTSELA_MONTHS", Types.INTEGER), ColumnDefinition("DURATIOPDEFAULTSHOURS", Types.DOUBLE), ColumnDefinition("DELAZDEFAULTSTYPF", Types.INTEGER), ColumnDefinition("DELAZDEFAULTSELA_MONTHS", Types.INTEGER), ColumnDefinition("DELAZDEFAULTSHOURS", Types.DOUBLE), ColumnDefinition("DEFAULTSQUANTITY", Types.DOUBLE), ColumnDefinition("DEFAULTSACTIVITY_CONV_FACTOR", Types.DOUBLE), ColumnDefinition("DEFAULTSCONSUMPTION_RATE", Types.DOUBLE), ColumnDefinition("DEFAULTSCONSUMPTION_RAT_UNIT", Types.INTEGER), ColumnDefinition("DEFAULTSDURATION_TIMG_UNIT", Types.INTEGER), ColumnDefinition("DEFAULTSDELAY_TIMF_UNIT", Types.INTEGER), ColumnDefinition("DEFAULTSEXPENDITURE_C_CENTRE", Types.INTEGER), ColumnDefinition("DEFAULTSINCOME_COST_CENTRE", Types.INTEGER), ColumnDefinition("DEFAULTSTYPM", Types.INTEGER), ColumnDefinition("DEFAULTSCALCULATEE_PARAMETER", Types.INTEGER), ColumnDefinition("DEFAULTSBALANCINH_PARAMETER", Types.INTEGER), ColumnDefinition("DEFAULTSCONSUMPTION_RAT_TYPE", Types.INTEGER), ColumnDefinition("DEFAULTSUSE_TASL_CALENDAR", Types.BIT), ColumnDefinition("DEFAULTSALLOD_PROPORTIONALLY", Types.BIT), ColumnDefinition("DEFAULTSCONSUMED", Types.BIT), ColumnDefinition("DEFAULTSACCOUNTEDA_ELSEWHERE", Types.BIT), ColumnDefinition("DEFAULTSMAY_BE_SHORTERA_TASK", Types.BIT), ColumnDefinition("AVAILABLE_FROM", Types.TIMESTAMP), ColumnDefinition("AVAILABLE_TO", Types.TIMESTAMP), ColumnDefinition("MEASUREMENT", Types.VARCHAR), ColumnDefinition("CONSUMABLE_RESOURCE", Types.INTEGER), ColumnDefinition("ARR_STOUT_STRES_APARROW_TYPE", Types.INTEGER), ColumnDefinition("ARR_STOUT_STRES_APLENGTH", Types.INTEGER), ColumnDefinition("ARR_STOUT_STRES_APEDGE", Types.INTEGER), ColumnDefinition("ARR_STOUT_STRES_APBORDET_COL", Types.INTEGER), ColumnDefinition("ARR_STOUT_STRES_APINSIDG_COL", Types.INTEGER), ColumnDefinition("ARR_STOUT_STRES_APPLACEMENW", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APBLIP_TYPE", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APSCALEY", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APSCALEZ", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APGAP", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APBORDES_COL", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APINSIDF_COL", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APPLACEMENV", Types.INTEGER), ColumnDefinition("LIN_STOUT_STRES_APSCALEX", Types.DOUBLE), ColumnDefinition("LIN_STOUT_STRES_APWIDTH", Types.INTEGER), ColumnDefinition("LIN_STOUT_STRES_APBORDER_COL", Types.INTEGER), ColumnDefinition("LIN_STOUT_STRES_APINSIDE_COL", Types.INTEGER), ColumnDefinition("LIN_STOUT_STRES_APLINE_TYPE", Types.INTEGER), ColumnDefinition("RES_APFOREGROUND_FILL_COLOUR", Types.INTEGER), ColumnDefinition("RES_APBACKGROUND_FILL_COLOUR", Types.INTEGER), ColumnDefinition("RES_APPATTERN", Types.INTEGER), ColumnDefinition("AVAILABILITY", Types.DOUBLE), ColumnDefinition("TOTAL_AVAILABILITY", Types.DOUBLE), ColumnDefinition("SPAWE_INTEGER", Types.INTEGER), ColumnDefinition("NASE", Types.VARCHAR), ColumnDefinition("SHORT_NAME_SINGLE", Types.VARCHAR), ColumnDefinition("SHORT_NAME_PLURAL", Types.VARCHAR), ColumnDefinition("CALENDAV", Types.INTEGER), ColumnDefinition("USE_PARENV_CALENDAR", Types.BIT), ColumnDefinition("USE_LINE_STYLE_P_ALLOCATIONS", Types.BIT), ColumnDefinition("CREATED_AS_FOLDER", Types.BIT), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER), ColumnDefinition("NO_NEW_ASSIGNMENTS", Types.INTEGER))

        private val PERMANENT_RESOURCE_COLUMNS = arrayOf(ColumnDefinition("PERMANENT_RESOURCEID", Types.INTEGER), ColumnDefinition("EMAIL_ADDRESS", Types.VARCHAR), ColumnDefinition("EFFORT_TIME_UNIT", Types.INTEGER), ColumnDefinition("PURE_TREE", Types.BIT), ColumnDefinition("EXCLUDED_FROM_TIMESHEET", Types.BIT), ColumnDefinition("ARR_STOUT_STRES_APARROW_TYPE", Types.INTEGER), ColumnDefinition("ARR_STOUT_STRES_APLENGTH", Types.INTEGER), ColumnDefinition("ARR_STOUT_STRES_APEDGE", Types.INTEGER), ColumnDefinition("ARR_STOUT_STRES_APBORDET_COL", Types.INTEGER), ColumnDefinition("ARR_STOUT_STRES_APINSIDG_COL", Types.INTEGER), ColumnDefinition("ARR_STOUT_STRES_APPLACEMENW", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APBLIP_TYPE", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APSCALEY", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APSCALEZ", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APGAP", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APBORDES_COL", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APINSIDF_COL", Types.INTEGER), ColumnDefinition("BLI_STOUT_STRES_APPLACEMENV", Types.INTEGER), ColumnDefinition("LIN_STOUT_STRES_APSCALEX", Types.DOUBLE), ColumnDefinition("LIN_STOUT_STRES_APWIDTH", Types.INTEGER), ColumnDefinition("LIN_STOUT_STRES_APBORDER_COL", Types.INTEGER), ColumnDefinition("LIN_STOUT_STRES_APINSIDE_COL", Types.INTEGER), ColumnDefinition("LIN_STOUT_STRES_APLINE_TYPE", Types.INTEGER), ColumnDefinition("RES_APFOREGROUND_FILL_COLOUR", Types.INTEGER), ColumnDefinition("RES_APBACKGROUND_FILL_COLOUR", Types.INTEGER), ColumnDefinition("RES_APPATTERN", Types.INTEGER), ColumnDefinition("AVAILABILITY", Types.DOUBLE), ColumnDefinition("TOTAL_AVAILABILITY", Types.DOUBLE), ColumnDefinition("SPAWE_INTEGER", Types.INTEGER), ColumnDefinition("NASE", Types.VARCHAR), ColumnDefinition("SHORT_NAME_SINGLE", Types.VARCHAR), ColumnDefinition("SHORT_NAME_PLURAL", Types.VARCHAR), ColumnDefinition("CALENDAV", Types.INTEGER), ColumnDefinition("USE_PARENV_CALENDAR", Types.BIT), ColumnDefinition("USE_LINE_STYLE_P_ALLOCATIONS", Types.BIT), ColumnDefinition("CREATED_AS_FOLDER", Types.BIT), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER), ColumnDefinition("NO_NEW_ASSIGNMENTS", Types.INTEGER))// Followed by user defined columns which differ by project

        private val PERMANENT_RESOURCE_SKILL_COLUMNS = arrayOf(ColumnDefinition("PERM_RESOURCE_SKILLID", Types.INTEGER), ColumnDefinition("ARR_STOUT_STSKI_APARROW_TYPE", Types.INTEGER), ColumnDefinition("ARR_STOUT_STSKI_APLENGTH", Types.INTEGER), ColumnDefinition("ARR_STOUT_STSKI_APEDGE", Types.INTEGER), ColumnDefinition("ARR_STOUT_STSKI_APBORDET_COL", Types.INTEGER), ColumnDefinition("ARR_STOUT_STSKI_APINSIDG_COL", Types.INTEGER), ColumnDefinition("ARR_STOUT_STSKI_APPLACEMENW", Types.INTEGER), ColumnDefinition("BLI_STOUT_STSKI_APBLIP_TYPE", Types.INTEGER), ColumnDefinition("BLI_STOUT_STSKI_APSCALEY", Types.INTEGER), ColumnDefinition("BLI_STOUT_STSKI_APSCALEZ", Types.INTEGER), ColumnDefinition("BLI_STOUT_STSKI_APGAP", Types.INTEGER), ColumnDefinition("BLI_STOUT_STSKI_APBORDES_COL", Types.INTEGER), ColumnDefinition("BLI_STOUT_STSKI_APINSIDF_COL", Types.INTEGER), ColumnDefinition("BLI_STOUT_STSKI_APPLACEMENV", Types.INTEGER), ColumnDefinition("LIN_STOUT_STSKI_APSCALEX", Types.DOUBLE), ColumnDefinition("LIN_STOUT_STSKI_APWIDTH", Types.INTEGER), ColumnDefinition("LIN_STOUT_STSKI_APBORDER_COL", Types.INTEGER), ColumnDefinition("LIN_STOUT_STSKI_APINSIDE_COL", Types.INTEGER), ColumnDefinition("LIN_STOUT_STSKI_APLINE_TYPE", Types.INTEGER), ColumnDefinition("SKI_APFOREGROUND_FILL_COLOUR", Types.INTEGER), ColumnDefinition("SKI_APBACKGROUND_FILL_COLOUR", Types.INTEGER), ColumnDefinition("SKI_APPATTERN", Types.INTEGER), ColumnDefinition("DURATIOODEFAULTTTYPF", Types.INTEGER), ColumnDefinition("DURATIOODEFAULTTELA_MONTHS", Types.INTEGER), ColumnDefinition("DURATIOODEFAULTTHOURS", Types.DOUBLE), ColumnDefinition("DELAYDEFAULTTTYPF", Types.INTEGER), ColumnDefinition("DELAYDEFAULTTELA_MONTHS", Types.INTEGER), ColumnDefinition("DELAYDEFAULTTHOURS", Types.DOUBLE), ColumnDefinition("DEFAULTTALLOCATION", Types.DOUBLE), ColumnDefinition("DEFAULTTWORK_FROM_ACT_FACTOR", Types.DOUBLE), ColumnDefinition("DEFAULTTEFFORT", Types.DOUBLE), ColumnDefinition("DEFAULTTWORL", Types.DOUBLE), ColumnDefinition("DEFAULTTWORK_RATE", Types.DOUBLE), ColumnDefinition("DEFAULTTWORK_UNIT", Types.INTEGER), ColumnDefinition("DEFAULTTWORK_RATE_TIME_UNIT", Types.INTEGER), ColumnDefinition("DEFAULTTEFFORT_TIMG_UNIT", Types.INTEGER), ColumnDefinition("DEFAULTTDURATION_TIMF_UNIT", Types.INTEGER), ColumnDefinition("DEFAULTTDELAY_TIME_UNIT", Types.INTEGER), ColumnDefinition("DEFAULTTTYPL", Types.INTEGER), ColumnDefinition("DEFAULTTCALCULATED_PARAMETER", Types.INTEGER), ColumnDefinition("DEFAULTTBALANCING_PARAMETER", Types.INTEGER), ColumnDefinition("DEFAULTTWORK_RATE_TYPE", Types.INTEGER), ColumnDefinition("DEFAULTTUSE_TASK_CALENDAR", Types.BIT), ColumnDefinition("DEFAULTTALLOC_PROPORTIONALLY", Types.BIT), ColumnDefinition("DEFAULTTCAN_BE_SPLIT", Types.BIT), ColumnDefinition("DEFAULTTCAN_BE_DELAYED", Types.BIT), ColumnDefinition("DEFAULTTCAN_BE_STRETCHED", Types.BIT), ColumnDefinition("DEFAULTTACCOUNTED__ELSEWHERE", Types.BIT), ColumnDefinition("DEFAULTTCONTRIBUTES_T_EFFORT", Types.BIT), ColumnDefinition("DEFAULTTMAY_BE_SHORTER__TASK", Types.BIT), ColumnDefinition("DEFAULTTSHARED_EFFORT", Types.BIT), ColumnDefinition("ABILITY", Types.DOUBLE), ColumnDefinition("EFFECTIVENESS", Types.DOUBLE), ColumnDefinition("AVAILABILITY", Types.DOUBLE), ColumnDefinition("AVAILABLF_FROM", Types.TIMESTAMP), ColumnDefinition("AVAILABLF_TO", Types.TIMESTAMP), ColumnDefinition("SPARO_INTEGER", Types.INTEGER), ColumnDefinition("EFFORT_TIMF_UNIT", Types.INTEGER), ColumnDefinition("ROLE", Types.INTEGER), ColumnDefinition("PLAYER", Types.INTEGER), ColumnDefinition("CREATED_AS_FOLDER", Types.BIT), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER))

        private val PERMANENT_SCHEDULE_ALLOCATION_COLUMNS = arrayOf(ColumnDefinition("PERMANENT_SCHEDUL_ALLOCATIONID", Types.INTEGER), ColumnDefinition("REQUIREE_BY", Types.INTEGER), ColumnDefinition("OWNED_BY_TIMESHEET_X", Types.BIT), ColumnDefinition("EFFORW", Types.DOUBLE), ColumnDefinition("GIVEN_EFFORT", Types.DOUBLE), ColumnDefinition("WORK_FROM_TASK_FACTOR", Types.DOUBLE), ColumnDefinition("ALLOCATIOO", Types.DOUBLE), ColumnDefinition("GIVEN_ALLOCATION", Types.DOUBLE), ColumnDefinition("ALLOCATIOP_OF", Types.INTEGER), ColumnDefinition("WORM_UNIT", Types.INTEGER), ColumnDefinition("WORK_RATE_TIMF_UNIT", Types.INTEGER), ColumnDefinition("EFFORT_TIMJ_UNIT", Types.INTEGER), ColumnDefinition("WORO", Types.DOUBLE), ColumnDefinition("GIVEN_WORK", Types.DOUBLE), ColumnDefinition("WORL_RATE", Types.DOUBLE), ColumnDefinition("GIVEN_WORK_RATE", Types.DOUBLE), ColumnDefinition("TYPV", Types.INTEGER), ColumnDefinition("CALCULATEG_PARAMETER", Types.INTEGER), ColumnDefinition("BALANCINJ_PARAMETER", Types.INTEGER), ColumnDefinition("SHAREE_EFFORT", Types.BIT), ColumnDefinition("CONTRIBUTES_TO_ACTIVI_EFFORT", Types.BIT), ColumnDefinition("DELAATYPF", Types.INTEGER), ColumnDefinition("DELAAELA_MONTHS", Types.INTEGER), ColumnDefinition("DELAAHOURS", Types.DOUBLE), ColumnDefinition("GIVEO_DURATIONTYPF", Types.INTEGER), ColumnDefinition("GIVEO_DURATIONELA_MONTHS", Types.INTEGER), ColumnDefinition("GIVEO_DURATIONHOURS", Types.DOUBLE), ColumnDefinition("DELAY_TIMI_UNIT", Types.INTEGER), ColumnDefinition("RATE_TYPE", Types.INTEGER), ColumnDefinition("USE_TASM_CALENDAR", Types.BIT), ColumnDefinition("IGNORF", Types.BIT), ColumnDefinition("ELAPSEE", Types.BIT), ColumnDefinition("MAY_BE_SHORTER_THAN_TASK", Types.BIT), ColumnDefinition("RESUMF", Types.TIMESTAMP), ColumnDefinition("SPAXE_INTEGER", Types.INTEGER), ColumnDefinition("PERCENT_COMPLETE", Types.DOUBLE), ColumnDefinition("USER_PERCENU_COMPLETE", Types.DOUBLE), ColumnDefinition("ALLOCATIOR_GROUP", Types.INTEGER), ColumnDefinition("ALLOCATEE_TO", Types.INTEGER), ColumnDefinition("PRIORITC", Types.INTEGER), ColumnDefinition("ACCOUNTED_FOR_ELSEWHERE", Types.BIT), ColumnDefinition("DURATIOTTYPF", Types.INTEGER), ColumnDefinition("DURATIOTELA_MONTHS", Types.INTEGER), ColumnDefinition("DURATIOTHOURS", Types.DOUBLE), ColumnDefinition("STARZ", Types.TIMESTAMP), ColumnDefinition("ENJ", Types.TIMESTAMP), ColumnDefinition("DURATION_TIMJ_UNIT", Types.INTEGER), ColumnDefinition("UNSCHEDULABLG", Types.BIT), ColumnDefinition("SUBPROJECT_ID", Types.INTEGER), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER), ColumnDefinition("TIMESHEET_ROUND_UP_IF_UNDER", Types.INTEGER), ColumnDefinition("TIMESHEET_CAP_IF_OVER", Types.INTEGER), ColumnDefinition("BUDGETED_COST_CURRENCY", Types.INTEGER), ColumnDefinition("BUDGETED_COST_AMOUNT", Types.DOUBLE), ColumnDefinition("FLAGS", Types.INTEGER), ColumnDefinition("ALLOCATION_PROFILE", Types.VARCHAR), ColumnDefinition("RESOURCE_CURVE", Types.INTEGER), ColumnDefinition("NONLINEAR_TYPE", Types.INTEGER))

        private val WBS_ENTRY_COLUMNS = arrayOf(ColumnDefinition("WBS_ENTRYID", Types.INTEGER), ColumnDefinition("NATURAP_ORDER", Types.INTEGER), ColumnDefinition("WBT_CODE", Types.VARCHAR), ColumnDefinition("WBT_NAME", Types.VARCHAR), ColumnDefinition("WBS_ENTRY", Types.INTEGER), ColumnDefinition("CREATED_AS_FOLDER", Types.BIT), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER))

        private val TASK_COMPLETED_SECTION_COLUMNS = arrayOf(ColumnDefinition("TASK_COMPLETED_SECTIONID", Types.INTEGER), ColumnDefinition("NATURAM_ORDER", Types.INTEGER), ColumnDefinition("OVERALL_PERCENT_COMPLETE", Types.DOUBLE), ColumnDefinition("ACTUAL_TASK_WORK", Types.DOUBLE), ColumnDefinition("TASK", Types.INTEGER), ColumnDefinition("ACTUAL_START", Types.TIMESTAMP), ColumnDefinition("ACTUAL_END", Types.TIMESTAMP), ColumnDefinition("SPAUE_INTEGER", Types.INTEGER), ColumnDefinition("DURATIOTTYPF", Types.INTEGER), ColumnDefinition("DURATIOTELA_MONTHS", Types.INTEGER), ColumnDefinition("DURATIOTHOURS", Types.DOUBLE), ColumnDefinition("STARZ", Types.TIMESTAMP), ColumnDefinition("ENJ", Types.TIMESTAMP), ColumnDefinition("DURATION_TIMJ_UNIT", Types.INTEGER), ColumnDefinition("UNSCHEDULABLG", Types.INTEGER), ColumnDefinition("SUBPROJECT_ID", Types.INTEGER), ColumnDefinition("ALT_ID", Types.INTEGER), ColumnDefinition("LAST_EDITED_DATE", Types.TIMESTAMP), ColumnDefinition("LAST_EDITED_BY", Types.INTEGER))
    }
}
