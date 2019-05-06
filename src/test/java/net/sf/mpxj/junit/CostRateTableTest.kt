/*
 * file:       CostRateTableTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2009
 * date:       08/06/2009
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

import java.text.DateFormat
import java.text.SimpleDateFormat

import net.sf.mpxj.CostRateTable
import net.sf.mpxj.CostRateTableEntry
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Resource
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.mpp.MPPReader
import net.sf.mpxj.mspdi.MSPDIReader

import org.junit.Test

/**
 * The tests contained in this class exercise cost rate table functionality.
 */
class CostRateTableTest {

    private val m_df = SimpleDateFormat("dd/MM/yyyy HH:mm")
    /**
     * Test MPP9 file cost rate tables.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp9costratetable.mpp"))
        testCostRateTable(file)
    }

    /**
     * Test MPP9 file cost rate tables saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9From12() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp9costratetable-from12.mpp"))
        testCostRateTable(file)
    }

    /**
     * Test MPP9 file cost rate tables saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9From14() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp9costratetable-from14.mpp"))
        testCostRateTable(file)
    }

    /**
     * Test MPP12 file cost rate tables.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp12costratetable.mpp"))
        testCostRateTable(file)
    }

    /**
     * Test MPP12 file cost rate tables saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12From14() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp12costratetable-from14.mpp"))
        testCostRateTable(file)
    }

    /**
     * Test MPP14 file cost rate tables.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp14costratetable.mpp"))
        testCostRateTable(file)
    }

    /**
     * Test MSPDI file cost rate tables.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMspdi() {
        val file = MSPDIReader().read(MpxjTestData.filePath("mspdicostratetable.xml"))
        testCostRateTable(file)
    }

    /**
     * Common cost rate table tests.
     *
     * @param file project file
     */
    @Throws(Exception::class)
    private fun testCostRateTable(file: ProjectFile) {
        //
        // Resource with default tables
        //
        var resource = file.getResourceByID(Integer.valueOf(1))
        assertEquals("Resource One", resource.name)

        // Table A
        var table = resource.getCostRateTable(0)
        assertEquals(1, table.size())
        assertRateEquals(0.0, TimeUnit.HOURS, 0.0, TimeUnit.HOURS, 0.0, "31/12/2049 23:59", table, 0)

        // Table B
        table = resource.getCostRateTable(1)
        assertEquals(1, table.size())
        assertRateEquals(0.0, TimeUnit.HOURS, 0.0, TimeUnit.HOURS, 0.0, "31/12/2049 23:59", table, 0)

        // Table C
        table = resource.getCostRateTable(2)
        assertEquals(1, table.size())
        assertRateEquals(0.0, TimeUnit.HOURS, 0.0, TimeUnit.HOURS, 0.0, "31/12/2049 23:59", table, 0)

        // Table D
        table = resource.getCostRateTable(3)
        assertEquals(1, table.size())
        assertRateEquals(0.0, TimeUnit.HOURS, 0.0, TimeUnit.HOURS, 0.0, "31/12/2049 23:59", table, 0)

        // Table E
        table = resource.getCostRateTable(4)
        assertEquals(1, table.size())
        assertRateEquals(0.0, TimeUnit.HOURS, 0.0, TimeUnit.HOURS, 0.0, "31/12/2049 23:59", table, 0)

        //
        // Resource with default tables, but non-default values
        //
        resource = file.getResourceByID(Integer.valueOf(2))
        assertEquals("Resource Two", resource.name)

        // Table A
        table = resource.getCostRateTable(0)
        assertEquals(1, table.size())
        assertRateEquals(5.0, TimeUnit.HOURS, 10.0, TimeUnit.HOURS, 15.0, "31/12/2049 23:59", table, 0)

        // Table B
        table = resource.getCostRateTable(1)
        assertEquals(1, table.size())
        assertRateEquals(20.0, TimeUnit.HOURS, 25.0, TimeUnit.HOURS, 30.0, "31/12/2049 23:59", table, 0)

        // Table C
        table = resource.getCostRateTable(2)
        assertEquals(1, table.size())
        assertRateEquals(35.0, TimeUnit.HOURS, 40.0, TimeUnit.HOURS, 45.0, "31/12/2049 23:59", table, 0)

        // Table D
        table = resource.getCostRateTable(3)
        assertEquals(1, table.size())
        assertRateEquals(50.0, TimeUnit.HOURS, 55.0, TimeUnit.HOURS, 60.0, "31/12/2049 23:59", table, 0)

        // Table E
        table = resource.getCostRateTable(4)
        assertEquals(1, table.size())
        assertRateEquals(65.0, TimeUnit.HOURS, 70.0, TimeUnit.HOURS, 75.0, "31/12/2049 23:59", table, 0)

        //
        // Resource with multiple values
        //
        resource = file.getResourceByID(Integer.valueOf(3))
        assertEquals("Resource Three", resource.name)

        // Table A
        table = resource.getCostRateTable(0)
        assertEquals(2, table.size())
        assertRateEquals(5.0, TimeUnit.HOURS, 10.0, TimeUnit.HOURS, 15.0, "15/06/2009 08:00", table, 0)
        assertRateEquals(1200.0, TimeUnit.MINUTES, 25.0, TimeUnit.HOURS, 30.0, "31/12/2049 23:59", table, 1)

        // Table B
        table = resource.getCostRateTable(1)
        assertEquals(2, table.size())
        assertRateEquals(35.0, TimeUnit.HOURS, 40.0, TimeUnit.HOURS, 45.0, "16/06/2009 08:00", table, 0)
        assertRateEquals(6.25, TimeUnit.DAYS, 1.375, TimeUnit.WEEKS, 60.0, "31/12/2049 23:59", table, 1)

        // Table C
        table = resource.getCostRateTable(2)
        assertEquals(2, table.size())
        assertRateEquals(65.0, TimeUnit.HOURS, 70.0, TimeUnit.HOURS, 75.0, "17/06/2009 08:00", table, 0)
        assertRateEquals(0.5, TimeUnit.MONTHS, 0.040, TimeUnit.YEARS, 90.0, "31/12/2049 23:59", table, 1)

        // Table D
        table = resource.getCostRateTable(3)
        assertEquals(2, table.size())
        assertRateEquals(95.0, TimeUnit.HOURS, 100.0, TimeUnit.HOURS, 105.0, "18/06/2009 08:00", table, 0)
        assertRateEquals(110.0, TimeUnit.HOURS, 115.0, TimeUnit.HOURS, 120.0, "31/12/2049 23:59", table, 1)

        // Table E
        table = resource.getCostRateTable(4)
        assertEquals(2, table.size())
        assertRateEquals(125.0, TimeUnit.HOURS, 130.0, TimeUnit.HOURS, 135.0, "19/06/2009 08:00", table, 0)
        assertRateEquals(140.0, TimeUnit.HOURS, 145.0, TimeUnit.HOURS, 150.0, "31/12/2049 23:59", table, 1)

        //
        // Validate date-based row selection
        //
        var entry = table.getEntryByDate(m_df.parse("18/06/2009 07:00"))
        assertRateEquals(125.0, TimeUnit.HOURS, 130.0, TimeUnit.HOURS, 135.0, "19/06/2009 08:00", entry)
        entry = table.getEntryByDate(m_df.parse("19/06/2009 10:00"))
        assertRateEquals(140.0, TimeUnit.HOURS, 145.0, TimeUnit.HOURS, 150.0, "31/12/2049 23:59", entry)
    }

    /**
     * Test a single row from a cost rate table.
     *
     * @param standardRate expected standard rate
     * @param standardRateFormat expected standard rate format
     * @param overtimeRate expected overtime rate
     * @param overtimeRateFormat expected overtime rate format
     * @param perUseRate expected per use rate
     * @param endDate expected end date
     * @param table table instance under test
     * @param index index of table row under test
     */
    private fun assertRateEquals(standardRate: Double, standardRateFormat: TimeUnit, overtimeRate: Double, overtimeRateFormat: TimeUnit, perUseRate: Double, endDate: String, table: CostRateTable, index: Int) {
        val entry = table.get(index)
        assertRateEquals(standardRate, standardRateFormat, overtimeRate, overtimeRateFormat, perUseRate, endDate, entry)
    }

    /**
     * Test a single row from a cost rate table.
     *
     * @param standardRate expected standard rate
     * @param standardRateFormat expected standard rate format
     * @param overtimeRate expected overtime rate
     * @param overtimeRateFormat expected overtime rate format
     * @param costPerUse expected cost per use
     * @param endDate expected end date
     * @param entry table entry instance under test
     */
    private fun assertRateEquals(standardRate: Double, standardRateFormat: TimeUnit, overtimeRate: Double, overtimeRateFormat: TimeUnit, costPerUse: Double, endDate: String, entry: CostRateTableEntry) {
        assertEquals(standardRate, entry.getStandardRate().getAmount(), 0.009)
        assertEquals(overtimeRate, entry.getOvertimeRate().getAmount(), 0.009)
        assertEquals(costPerUse, entry.getCostPerUse().doubleValue(), 0)
        assertEquals(endDate, m_df.format(entry.getEndDate()))
        assertEquals(standardRateFormat, entry.getStandardRateFormat())
        assertEquals(overtimeRateFormat, entry.getOvertimeRateFormat())
    }
}
