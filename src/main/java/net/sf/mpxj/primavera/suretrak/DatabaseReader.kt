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

package net.sf.mpxj.primavera.suretrak

import java.io.File
import java.io.IOException
import java.util.HashMap

import net.sf.mpxj.primavera.common.ByteColumn
import net.sf.mpxj.primavera.common.ColumnDefinition
import net.sf.mpxj.primavera.common.IntColumn
import net.sf.mpxj.primavera.common.ShortColumn
import net.sf.mpxj.primavera.common.StringColumn
import net.sf.mpxj.primavera.common.Table
import net.sf.mpxj.primavera.common.TableDefinition

/**
 * Reads a directory containing a SureTrak database and returns a map
 * of table names and the data they contain.
 */
internal class DatabaseReader {
    /**
     * Main entry point. Reads a directory containing a SureTrak database files
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

                val typeIndex = name.lastIndexOf('.')
                val type = name.substring(typeIndex + 1, name.length())
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

        private val ACT_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("ACTIVITY_ID", 1, 10), StringColumn("NAME", 11, 48), StringColumn("DEPARTMENT", 59, 5), StringColumn("MANAGER", 64, 8), StringColumn("SECTION", 72, 4), StringColumn("MAIL", 76, 8), StringColumn("WBS", 123, 48), PercentColumn("PERCENT_COMPLETE", 192), DurationColumn("ORIGINAL_DURATION", 198), DurationColumn("REMAINING_DURATION", 200), DateInHoursColumn("EARLY_START", 202), DateInHoursColumn("EARLY_FINISH", 206), DateInHoursColumn("LATE_START", 210), DateInHoursColumn("LATE_FINISH", 214),
                //new DateInHoursColumn("UNKNOWN_DATE1", 218),
                //new DateInHoursColumn("UNKNOWN_DATE2", 222),
                DateInHoursColumn("ACTUAL_START", 234), DateInHoursColumn("ACTUAL_FINISH", 238), DateInHoursColumn("TARGET_START", 242), DateInHoursColumn("TARGET_FINISH,", 246))

        private val CAL_COLUMNS = arrayOf<ColumnDefinition>(ShortColumn("CALENDAR_ID", 1), StringColumn("NAME", 3, 16), IntColumn("SUNDAY_HOURS", 19), IntColumn("MONDAY_HOURS", 23), IntColumn("TUESDAY_HOURS", 27), IntColumn("WEDNESDAY_HOURS", 31), IntColumn("THURSDAY_HOURS", 35), IntColumn("FRIDAY_HOURS", 39), IntColumn("SATURDAY_HOURS", 43))

        private val DIR_COLUMNS = arrayOf<ColumnDefinition>()

        private val FLT_COLUMNS = arrayOf<ColumnDefinition>()

        private val HOL_COLUMNS = arrayOf<ColumnDefinition>(ShortColumn("CALENDAR_ID", 1), DateInDaysColumn("DATE", 3), AnnualColumn("ANNUAL", 3))

        private val REL_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("PREDECESSOR_ACTIVITY_ID", 1, 10), StringColumn("SUCCESSOR_ACTIVITY_ID", 11, 10), RelationTypeColumn("TYPE", 21), DurationColumn("LAG", 22))

        private val RES_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("ACTIVITY_ID", 1, 10), StringColumn("RESOURCE_ID", 11, 8))

        private val RLB_COLUMNS = arrayOf<ColumnDefinition>(StringColumn("CODE", 1, 8), StringColumn("NAME", 9, 40), ShortColumn("BASE_CALENDAR_ID", 99), ShortColumn("CALENDAR_ID", 101))

        private val TTL_COLUMNS = arrayOf<ColumnDefinition>(RawColumn("DATA", 0, 100), StringColumn("TEXT1", 1, 48), StringColumn("TEXT2", 49, 48), ByteColumn("DEFINITION_ID", 97), ShortColumn("ORDER", 98))

        private val TABLE_DEFINITIONS = HashMap<String, TableDefinition>()

        init {
            TABLE_DEFINITIONS.put("ACT", TableDefinition(0, 298, *ACT_COLUMNS))
            TABLE_DEFINITIONS.put("CAL", TableDefinition(0, 47, "CALENDAR_ID", null, *CAL_COLUMNS))
            TABLE_DEFINITIONS.put("DIR", TableDefinition(0, 565, *DIR_COLUMNS))
            TABLE_DEFINITIONS.put("FLT", TableDefinition(0, 137, *FLT_COLUMNS))
            TABLE_DEFINITIONS.put("HOL", TableDefinition(0, 11, *HOL_COLUMNS))
            //TABLE_DEFINITIONS.put("LAY", new TableDefinition(0, 0, LAY_COLUMNS));
            //TABLE_DEFINITIONS.put("LOG", new TableDefinition(0, 0, LOG_COLUMNS));
            TABLE_DEFINITIONS.put("REL", TableDefinition(0, 26, *REL_COLUMNS))
            //TABLE_DEFINITIONS.put("REP", new TableDefinition(0, 0, REP_COLUMNS));
            TABLE_DEFINITIONS.put("RES", TableDefinition(0, 118, *RES_COLUMNS))
            TABLE_DEFINITIONS.put("RLB", TableDefinition(0, 111, *RLB_COLUMNS))
            TABLE_DEFINITIONS.put("TTL", TableDefinition(0, 100, *TTL_COLUMNS))
        }
    }
}
