/*
 * file:       DatabaseReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       01/03/2018
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

package net.sf.mpxj.primavera.p3

import java.io.File
import java.io.IOException
import java.util.Date
import java.util.HashMap

import net.sf.mpxj.primavera.common.ByteColumn
import net.sf.mpxj.primavera.common.ColumnDefinition
import net.sf.mpxj.primavera.common.IntColumn
import net.sf.mpxj.primavera.common.RowValidator
import net.sf.mpxj.primavera.common.ShortColumn
import net.sf.mpxj.primavera.common.StringColumn
import net.sf.mpxj.primavera.common.Table
import net.sf.mpxj.primavera.common.TableDefinition

/**
 * Reads a directory containing a P3 Btrieve database and returns a map
 * of table names and the data they contain.
 */
internal class DatabaseReader {
    /**
     * Main entry point. Reads a directory containing a P3 Btrieve database files
     * and returns a map of table names and table content.
     *
     * @param directory directory containing the database
     * @param prefix file name prefix used to identify files from the same database
     * @return Map of table names to table data
     */
    @Throws(IOException::class)
    fun process(directory: File, prefix: String): Map<String, Table> {
        val filePrefix = prefix.toUpperCase()
        val tables = HashMap<String, Table>()
        val files = directory.listFiles()
        if (files != null) {
            for (file in files!!) {
                val name = file.getName().toUpperCase()
                if (!name.startsWith(filePrefix)) {
                    continue
                }

                val typeIndex = name.lastIndexOf('.') - 3
                val type = name.substring(typeIndex, typeIndex + 3)
                val definition = TABLE_DEFINITIONS.get(type)
                if (definition != null) {
                    val table = Table()
                    val reader = TableReader(definition)
                    reader.read(file, table)
                    tables.put(type, table)
                    //dumpCSV(type, definition, table);
                }
            }
        }
        return tables
    }

    companion object {

        //   private void dumpCSV(String type, TableDefinition definition, Table table) throws IOException
        //   {
        //      PrintWriter pw = new PrintWriter(new File("c:/temp/" + type + ".csv"));
        //      pw.print("ROW_NUMBER,ROW_VERSION,");
        //
        //      for (ColumnDefinition column : definition.getColumns())
        //      {
        //         pw.print(column.getName());
        //         pw.print(',');
        //      }
        //      pw.println();
        //
        //      for (MapRow row : table)
        //      {
        //         pw.print(row.getObject("ROW_NUMBER"));
        //         pw.print(',');
        //         pw.print(row.getObject("ROW_VERSION"));
        //         pw.print(',');
        //
        //         for (ColumnDefinition column : definition.getColumns())
        //         {
        //            pw.print(row.getObject(column.getName()));
        //            pw.print(',');
        //         }
        //         pw.println();
        //      }
        //
        //      pw.close();
        //   }

        private val AC2_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("UNKNOWN_1", 2, 4), StringColumn("UNKNOWN_2", 6, 8), ShortColumn("UNKNOWN_3", 14), ShortColumn("UNKNOWN_4", 16), StringColumn("UNKNOWN_5", 18, 4), StringColumn("UNKNOWN_6", 26, 8))

        private val ACC_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("COST_ACCOUNT_NUMBER", 2, 12), StringColumn("UNDEFINED_1", 14, 4), StringColumn("ACC_TITLE", 18, 40))

        private val ACT_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("ACTIVITY_ID", 2, 10), StringColumn("UNDEFINED_1", 12, 2), DurationColumn("FREE_FLOAT", 14), ShortColumn("CALENDAR_ID", 16), IntColumn("DURATION_CALC_CODE", 18), DurationColumn("ORIGINAL_DURATION", 22), DurationColumn("REMAINING_DURATION", 24), ShortColumn("ACTUAL_START_OR_CONSTRAINT_FLAG", 26), ShortColumn("ACTUAL_FINISH_OR_CONSTRAINT_FLAG", 28), PercentColumn("PERCENT_COMPLETE", 30), DateColumn("EARLY_START_INTERNAL", 34), DateColumn("LATE_START_INTERNAL", 38), DateColumn("AS_OR_ED_CONSTRAINT", 42), DateColumn("AF_OR_LD_CONSTRAINT", 46), DateColumn("EF_INTERNAL", 50), DateColumn("LF_INTERNAL", 54), DurationColumn("TOTAL_FLOAT", 58), StringColumn("MILESTONE", 60, 1), StringColumn("CRITICAL_FLAG", 61, 1), StringColumn("UNDEFINED_4", 62, 8), ByteColumn("ST_ACTIVITY_TYPE", 70), ByteColumn("LEVELING_TYPE", 71), StringColumn("UNDEFINED_5B", 72, 2), StringColumn("DEPT", 74, 3), StringColumn("RESP", 77, 5), StringColumn("PHAS", 82, 5), StringColumn("STEP", 87, 5), StringColumn("ITEM", 92, 5), StringColumn("UNDEFINED_6", 97, 41), StringColumn("ACTIVITY_TITLE", 138, 48), IntColumn("SUSPEND_DATE", 186), IntColumn("RESUME_DATE", 190), IntColumn("UNDEFINED_8A", 194), IntColumn("UNDEFINED_8B", 198), IntColumn("UNDEFINED_8C", 202), IntColumn("UNDEFINED_8D", 206), IntColumn("UNDEFINED_8E", 210), BtrieveDateColumn("EARLY_START", 214), BtrieveDateColumn("LATE_START", 218), BtrieveDateColumn("EARLY_FINISH", 222), BtrieveDateColumn("LATE_FINISH", 226), ByteColumn("EARLY_START_HOUR", 230), ByteColumn("LATE_START_HOUR", 231), ByteColumn("EARLY_FINISH_HOUR", 232), ByteColumn("LATE_FINISH_HOUR", 233), StringColumn("ACTUAL_START_FLAG", 234, 1), StringColumn("ACTUAL_FINISH_FLAG", 235, 1), StringColumn("UNDEFINED_10", 236, 10))

        private val DIR_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("SUB_PROJECT_NAME", 2, 4), IntColumn("SEQUENCE__NUMBER", 6), IntColumn("PRODUCT_CODE", 10), DateColumn("PROJECT_START_DATE", 14), IntColumn("HOLIDAY_CONVENTION", 18), StringColumn("SUB_PROJECT_ID", 22, 2), StringColumn("UNDEFINED_1", 24, 2), DateColumn("PROJECT_FINISH_DATE", 26), IntColumn("REPORT_COUNTER", 30), StringColumn("ACT_CODE_1_TO_4_SIZE", 34, 4), StringColumn("ACT_CODE_5_TO_8_SIZE", 38, 4), StringColumn("ACT_CODE_9_TO_12_SIZE", 42, 4), StringColumn("ACT_CODE_13_TO_16_SIZE", 46, 4), StringColumn("ACT_CODE_17_TO_20_SIZE", 50, 4), StringColumn("ACT_ID_CODE_1_TO_4_SIZE", 54, 4), IntColumn("PROJECT_TYPE", 58), DateColumn("CURRENT_DATA_DATE", 62), DateColumn("CALENDAR_START_DATE", 66), StringColumn("UNDEFINED_2", 70, 4), StringColumn("COMPANY_TITLE", 74, 36), StringColumn("PROJECT_TITLE", 110, 36), StringColumn("REPORT_TITLE", 146, 48), StringColumn("PROJECT_VERSION", 194, 16), StringColumn("UNDEFINED_3", 210, 32), StringColumn("AUTO_COST_SET", 242, 4), DateColumn("AUTO_COST_DATE", 246), StringColumn("AUTO_COST_RULES", 250, 14), StringColumn("UNDEFINED_4", 264, 14), IntColumn("SCHEDULE_LOGIC", 278), IntColumn("INTERRUPTIBLE_FLAG", 282), DateColumn("LATEST_EARLY_FINISH", 286), StringColumn("TARGET_1_NAME", 290, 4), StringColumn("UNDEFINED_5", 294, 4), StringColumn("TARGET_2_NAME", 298, 4), StringColumn("UNDEFINED_6", 302, 4), ShortColumn("LEVELED_SWITCH", 306), // LOGICAL
                ShortColumn("TOTAL_FLOAT_TYPE", 308), StringColumn("UNDEFINED_7", 310, 4), ShortColumn("START_DAY_OF_WEEK", 314), StringColumn("UNDEFINED_8", 316, 2), ShortColumn("MASTER_CALENDAR_TYPE", 318), ShortColumn("MASTER_CALENDAR_TYPE_AUX", 320), StringColumn("GRAPHIC_SUMMARY_PROJECT", 322, 1), StringColumn("SCHED_MAST_SUB_BOTH", 323, 1), StringColumn("DECIMAL_PLACES", 324, 1), StringColumn("UPDATE_SUB_DATA_DATE", 325, 1), StringColumn("SUMMARY_CAL_ID", 326, 1), StringColumn("END_DATE_FROM_MS", 327, 1), StringColumn("SS_LAG_FROM_ASES", 328, 1), StringColumn("UNDEFINED_9", 329, 1), ByteColumn("WBSW_01", 330), StringColumn("WBSS_01", 331, 1), ByteColumn("WBSW_02", 332), StringColumn("WBSS_02", 333, 1), ByteColumn("WBSW_03", 334), StringColumn("WBSS_03", 335, 1), ByteColumn("WBSW_04", 336), StringColumn("WBSS_04", 337, 1), ByteColumn("WBSW_05", 338), StringColumn("WBSS_05", 339, 1), ByteColumn("WBSW_06", 340), StringColumn("WBSS_06", 341, 1), ByteColumn("WBSW_07", 342), StringColumn("WBSS_07", 343, 1), ByteColumn("WBSW_08", 344), StringColumn("WBSS_08", 345, 1), ByteColumn("WBSW_09", 346), StringColumn("WBSS_09", 347, 1), ByteColumn("WBSW_10", 348), StringColumn("WBSS_10", 349, 1), ByteColumn("WBSW_11", 350), StringColumn("WBSS_11", 351, 1), ByteColumn("WBSW_12", 352), StringColumn("WBSS_12", 353, 1), ByteColumn("WBSW_13", 354), StringColumn("WBSS_13", 355, 1), ByteColumn("WBSW_14", 356), StringColumn("WBSS_14", 357, 1), ByteColumn("WBSW_15", 358), StringColumn("WBSS_15", 359, 1), ByteColumn("WBSW_16", 360), StringColumn("WBSS_16", 361, 1), ByteColumn("WBSW_17", 362), StringColumn("WBSS_17", 363, 1), ByteColumn("WBSW_18", 364), StringColumn("WBSS_18", 365, 1), ByteColumn("WBSW_19", 366), StringColumn("WBSS_19", 367, 1), ByteColumn("WBSW_20", 368), StringColumn("WBSS_20", 369, 1), IntColumn("INTR_PRO_INDEX", 370), IntColumn("INTR_PROJ_LAST_SCED_DATE", 374), ShortColumn("LEVEL_NUM_SPLITS", 378), ShortColumn("LEVEL_SPLIT_NON_WORK", 380), ShortColumn("LEVEL_CONTIG_WORK", 382), ShortColumn("LEVEL_MIN_PCT_UPT", 384), ShortColumn("LEVEL_MAX_PCT_UPT", 386), StringColumn("PROJECT_CODE_01", 388, 10), StringColumn("PROJECT_CODE_02", 398, 10), StringColumn("PROJECT_CODE_03", 408, 10), StringColumn("PROJECT_CODE_04", 418, 10), StringColumn("PROJECT_CODE_05", 428, 10), StringColumn("PROJECT_CODE_06", 438, 10), StringColumn("PROJECT_CODE_07", 448, 10), StringColumn("PROJECT_CODE_08", 458, 10), StringColumn("PROJECT_CODE_09", 468, 10), StringColumn("PROJECT_CODE_10", 478, 10), StringColumn("UNDEFINED_10", 488, 18))

        private val AIT_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("ACT_ID", 2, 10), StringColumn("ACTID_EXT", 12, 2), StringColumn("RES", 14, 8), StringColumn("COST_ACCOUNT_NUMBER", 22, 12), StringColumn("RESOURCE_ID", 34, 1), StringColumn("UNDEFINED_1", 35, 3), DateColumn("PLANNED_START", 38), DateColumn("PLANNED_FINISH", 42), IntColumn("APPROVED_CHANGES", 46))

        private val DTL_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("CODE_NAME", 2, 4), StringColumn("CODE_VALUE", 6, 10), StringColumn("DESCRIPTION", 16, 48))

        private val HOL_COLUMNS = arrayOf<ColumnDefinition>(ShortColumn("CAL_ID", 2), DateColumn("START_OF_HOLIDAY", 4), DateColumn("END_OF_HOLIDAY", 8))

        private val LOG_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("ACT_ID", 2, 10), StringColumn("ACT_ID_EXT", 12, 2), ShortColumn("LOG_SEQ_NUMBER", 14), StringColumn("LOG_MASK", 16, 2), StringColumn("LOG_RECORD_INFO", 18, 48))

        private val REL_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("PREDECESSOR_ACTIVITY_ID", 2, 10), StringColumn("PREDECESSOR_ACTIVITY_EXT", 12, 2), StringColumn("SUCCESSOR_ACTIVITY_ID", 14, 10), StringColumn("SUCCESSOR_ACTIVITY_EXT", 24, 2), RelationTypeColumn("LAG_TYPE", 26), DurationColumn("LAG_VALUE", 28), StringColumn("DRIVING_REL", 30, 1))

        private val RES_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("ACTIVITY_ID", 2, 10), StringColumn("UNDEFINED_1", 12, 2), StringColumn("RESOURCE_ID", 14, 8), StringColumn("COST_ACCOUNT_NUMBER", 22, 12), ShortColumn("PERCENT_COMPLETE", 34), ShortColumn("LAG", 36), DurationColumn("REMAINING_DURATION", 38), StringColumn("RES_DESIGNATOR", 40, 1), StringColumn("DRIVING_RESOURCE", 41, 1), IntColumn("BUDGET_QUANTITY", 42), IntColumn("QUANTITY_THIS_PERIOD", 46), IntColumn("QUANTITY_TO_DATE", 50), IntColumn("QUANTITY_AT_COMPLETE", 54), DateColumn("ST_RES_EARLY_START", 58), DateColumn("ST_RES_EARLY_FINISH", 62), IntColumn("UNDEFINED_2", 66), IntColumn("BUDGET_COST", 70), IntColumn("COST_THIS_PERIOD", 74), IntColumn("COST_TO_DATE", 78), IntColumn("COST_AT_COMPLETION", 82), DateColumn("ST_RES_LATE_START", 86), DateColumn("ST_RES_LATE_FINISH", 90), IntColumn("UNDEFINED_3", 94))

        private val RIT_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("ACTID", 2, 10), StringColumn("ACTID_EXT", 12, 2), StringColumn("RES", 13, 8), StringColumn("COST_ACCOUNT_NUMBER", 21, 12), StringColumn("RESOURCE_ID", 33, 1), StringColumn("UNDEFINED_1", 34, 3), IntColumn("COMMITMENT_AMOUNT", 37), IntColumn("ORIGINAL_BUDGET", 41))

        private val RLB_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("RES_ID", 2, 8), StringColumn("UNIT_OF_MEASURE", 10, 4), StringColumn("RES_TITLE", 14, 40), IntColumn("ESCALATION_VAL_1", 54), DateColumn("ESCALATION_DATE_1", 58), IntColumn("ESCALATION_VAL_2", 62), DateColumn("ESCALATION_DATE_2", 66), IntColumn("ESCALATION_VAL_3", 70), DateColumn("ESCALATION_DATE_3", 7), IntColumn("ESCALATION_VAL_4", 78), DateColumn("ESCALATION_DATE_4", 8), IntColumn("ESCALATION_VAL_5", 86), DateColumn("ESCALATION_DATE_5", 9), IntColumn("ESCALATION_VAL_6", 94), DateColumn("ESCALATION_DATE_6", 9), IntColumn("NORM_LIM_VAL_1", 102), IntColumn("MAX_LIM_VAL_1", 106), DateColumn("LIM_TO_DATE_1", 110), IntColumn("NORM_LIM_VAL_2", 114), IntColumn("MAX_LIM_VAL_2", 118), DateColumn("LIM_TO_DATE_2", 122), IntColumn("NORM_LIM_VAL_3", 126), IntColumn("MAX_LIM_VAL_3", 130), DateColumn("LIM_TO_DATE_3", 134), IntColumn("NORM_LIM_VAL_4", 138), IntColumn("MAX_LIM_VAL_4", 142), DateColumn("LIM_TO_DATE_4", 146), IntColumn("NORM_LIM_VAL_5", 150), IntColumn("MAX_LIM_VAL_5", 154), DateColumn("LIM_TO_DATE_5", 158), IntColumn("NORM_LIM_VAL_6", 162), IntColumn("MAX_LIM_VAL_6", 166), DateColumn("LIM_TO_DATE_6", 170), ShortColumn("SHIFT_NUMB", 174), ShortColumn("SHIFT_LIMIT_TABLE", 176), ShortColumn("DRIVING_RESOURCE", 178), ShortColumn("UNDEFINED_1", 180))

        private val SRT_COLUMNS = arrayOf<ColumnDefinition>(IntColumn("SEQ_NUMBER", 2), StringColumn("ACT_ID", 2, 10), StringColumn("UNDEFINED_1", 2, 16))

        private val STR_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("INDICATOR", 2, 1), StringColumn("INDICATOR_EXT", 3, 1), ShortColumn("LEVEL_NUMBER", 4), StringColumn("UNDEFINED_2", 6, 4), StringColumn("CODE_VALUE", 10, 48), StringColumn("CODE_TITLE", 58, 48))

        private val TTL_COLUMNS = arrayOf<ColumnDefinition>(IntColumn("CODE_NAME", 2), StringColumn("CODE_VALUE", 6, 12), StringColumn("DESCRIPTION", 18, 48), ByteColumn("SORT_ORDER", 66))

        private val WBS_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("ACTIVITY_ID", 2, 10), StringColumn("ACTIVITY_ID_EXT", 12, 2), StringColumn("CODE_VALUE", 14, 48), StringColumn("INDICATOR", 62, 1))

        // Budget Summary
        private val ITM_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("ACTIVITY_ID", 2, 12), StringColumn("RESOURCE", 14, 8), StringColumn("COST_ACCOUNT", 22, 11), StringColumn("CATEGORY", 33, 5), ShortColumn("UNKNOWN_3", 38), ShortColumn("UNKNOWN_4", 40))

        private val PPA_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("UNKNOWN_1", 2, 10), StringColumn("UNKNOWN_2", 12, 19), StringColumn("UNKNOWN_3", 31, 2))// additional unknown fields

        private val SPR_COLUMNS = arrayOf<ColumnDefinition>(ShortColumn("UNKNOWN_1", 4))// additional unknown fields

        private val STW_COLUMNS = arrayOf<ColumnDefinition>()// unknown fields

        private val TIM_COLUMNS = arrayOf<ColumnDefinition>()// unknown fields

        private val AUD_COLUMNS = arrayOf<ColumnDefinition>()// unknown fields

        private val REP_COLUMNS = arrayOf<ColumnDefinition>()// unknown fields

        private val LAY_COLUMNS = arrayOf<ColumnDefinition>()// unknown fields

        private val PLT_COLUMNS = arrayOf<ColumnDefinition>()// unknown fields

        /**
         * Used to determine if a row from the DIR Table is valid, based on whether
         * the project start date falls after the date epoch.
         */
        private val DIR_ROW_VALIDATOR = object : RowValidator {
            @Override
            override fun validRow(row: Map<String, Object>): Boolean {
                val date = row["PROJECT_START_DATE"] as Date
                return date != null && date!!.getTime() > EPOCH
            }
        }

        /**
         * Epoch for date calculations. Represents 31/12/1983.
         */
        val EPOCH = 441676800000L

        private val TABLE_DEFINITIONS = HashMap<String, TableDefinition>()

        init {
            TABLE_DEFINITIONS.put("AC2", TableDefinition(512, 34, *AC2_COLUMNS))
            TABLE_DEFINITIONS.put("ACC", TableDefinition(512, 58, *ACC_COLUMNS))
            TABLE_DEFINITIONS.put("ACT", TableDefinition(1024, 250, "ACTIVITY_ID", null, *ACT_COLUMNS))
            TABLE_DEFINITIONS.put("AIT", TableDefinition(1024, 214, *AIT_COLUMNS))
            TABLE_DEFINITIONS.put("AUD", TableDefinition(1024, 143, *AUD_COLUMNS))
            // CAL - not a Btrieve file
            TABLE_DEFINITIONS.put("DIR", TableDefinition(512, 506, "SUB_PROJECT_NAME", DIR_ROW_VALIDATOR, *DIR_COLUMNS))
            // DST - not a Btrieve file
            TABLE_DEFINITIONS.put("DTL", TableDefinition(1024, 64, *DTL_COLUMNS))
            TABLE_DEFINITIONS.put("HOL", TableDefinition(512, 12, *HOL_COLUMNS))
            TABLE_DEFINITIONS.put("ITM", TableDefinition(1024, 42, *ITM_COLUMNS))
            TABLE_DEFINITIONS.put("LAY", TableDefinition(512, 14, *LAY_COLUMNS))
            TABLE_DEFINITIONS.put("LOG", TableDefinition(1024, 66, *LOG_COLUMNS))
            TABLE_DEFINITIONS.put("PLT", TableDefinition(512, 21, *PLT_COLUMNS))
            TABLE_DEFINITIONS.put("PPA", TableDefinition(1024, 46, *PPA_COLUMNS))
            TABLE_DEFINITIONS.put("REL", TableDefinition(512, 31, *REL_COLUMNS))
            TABLE_DEFINITIONS.put("REP", TableDefinition(512, 21, *REP_COLUMNS))
            TABLE_DEFINITIONS.put("RES", TableDefinition(1024, 114, *RES_COLUMNS))
            TABLE_DEFINITIONS.put("RIT", TableDefinition(1024, 214, *RIT_COLUMNS))
            TABLE_DEFINITIONS.put("RLB", TableDefinition(1024, 182, "RES_ID", null, *RLB_COLUMNS))
            TABLE_DEFINITIONS.put("SPR", TableDefinition(1024, 37, *SPR_COLUMNS))
            TABLE_DEFINITIONS.put("SRT", TableDefinition(4096, 16, *SRT_COLUMNS))
            TABLE_DEFINITIONS.put("STR", TableDefinition(512, 122, "CODE_VALUE", null, *STR_COLUMNS))
            TABLE_DEFINITIONS.put("STW", TableDefinition(1024, 58, *STW_COLUMNS))
            TABLE_DEFINITIONS.put("TIM", TableDefinition(1024, 153, *TIM_COLUMNS))
            TABLE_DEFINITIONS.put("TTL", TableDefinition(1024, 67, *TTL_COLUMNS))
            TABLE_DEFINITIONS.put("WBS", TableDefinition(1024, 63, "ACTIVITY_ID", null, *WBS_COLUMNS))
        }
    }
}
