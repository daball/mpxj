/*
 * file:       MppAssignmentTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       09/06/2011
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

import net.sf.mpxj.junit.MpxjAssert.*
import org.junit.Assert.*

import java.text.DateFormat
import java.text.SimpleDateFormat

import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpp.MPPReader
import net.sf.mpxj.mspdi.MSPDIReader

import org.junit.Test

/**
 * Tests to exercise file read functionality for various MS project file types.
 */
class MppAssignmentTest {

    /**
     * Test assignment data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9CustomFields() {
        val reader = MPPReader()
        val mpp = reader.read(MpxjTestData.filePath("mpp9assignmentcustom.mpp"))
        testCustomFields(mpp)
    }

    /**
     * Test assignment data read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9CustomFieldsFrom12() {
        val reader = MPPReader()
        val mpp = reader.read(MpxjTestData.filePath("mpp9assignmentcustom-from12.mpp"))
        testCustomFields(mpp)
    }

    /**
     * Test assignment data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12CustomFields() {
        val reader = MPPReader()
        val mpp = reader.read(MpxjTestData.filePath("mpp12assignmentcustom.mpp"))
        testCustomFields(mpp)
    }

    /**
     * Test assignment data read from an MPP12 file saved  by Project 2010.
     *
     * @throws Exception
     */
    // Sadly this doesn't work, as we just don't understand how a couple of
    // the large var data index values actually work. See FieldMap14 for details
    //   @Test public void testMpp12CustomFieldsFrom14() throws Exception
    //   {
    //      MPPReader reader = new MPPReader();
    //      ProjectFile mpp = reader.read(MpxjTestData.filePath("mpp12assignmentcustom-from14.mpp"));
    //      testCustomFields(mpp);
    //   }

    /**
     * Test assignment data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14CustomFields() {
        val reader = MPPReader()
        val mpp = reader.read(MpxjTestData.filePath("mpp14assignmentcustom.mpp"))
        testCustomFields(mpp)
    }

    /**
     * Test assignment data read from an MSPDI file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMspdiCustomFields() {
        val reader = MSPDIReader()
        val mpp = reader.read(MpxjTestData.filePath("mspdiassignmentcustom.xml"))
        testCustomFields(mpp)
    }

    /**
     * Test assignment data read from an MPD file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpdCustomFields() {
        assumeJvm()
        val reader = MPDDatabaseReader()
        val mpp = reader.read(MpxjTestData.filePath("mpdassignmentcustom.mpd"))
        testCustomFields(mpp)
    }

    /**
     * Validate custom field values.
     *
     * @param mpp project file
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun testCustomFields(mpp: ProjectFile) {
        val df = SimpleDateFormat("dd/MM/yy HH:mm")
        val task = mpp.getTaskByID(Integer.valueOf(1))
        assertEquals("Task One", task.name)
        val assignments = task.resourceAssignments
        val assignment1 = assignments.get(0)
        assertEquals("Resource One", assignment1.resource!!.name)
        val assignment2 = assignments.get(1)
        assertEquals("Resource Two", assignment2.resource!!.name)

        for (loop in 0..9) {
            assertEquals("Assignment 1 baseline cost " + (loop + 1), BASELINE_COSTS[0][loop], assignment1.getBaselineCost(loop + 1).intValue())
            assertEquals("Assignment 2 baseline cost " + (loop + 1), BASELINE_COSTS[1][loop], assignment2.getBaselineCost(loop + 1).intValue())

            assertEquals("Assignment 1 baseline work " + (loop + 1), BASELINE_WORKS[0][loop].toLong(), (assignment1.getBaselineWork(loop + 1).getDuration() as Int).toLong())
            assertEquals("Assignment 2 baseline work " + (loop + 1), BASELINE_WORKS[1][loop].toLong(), (assignment2.getBaselineWork(loop + 1).getDuration() as Int).toLong())
            assertEquals("Assignment 1 baseline work " + (loop + 1), TimeUnit.HOURS, assignment1.getBaselineWork(loop + 1).getUnits())
            assertEquals("Assignment 2 baseline work " + (loop + 1), TimeUnit.HOURS, assignment2.getBaselineWork(loop + 1).getUnits())

            assertEquals("Assignment 1 baseline start " + (loop + 1), BASELINE_STARTS[0][loop], df.format(assignment1.getBaselineStart(loop + 1)))
            assertEquals("Assignment 2 baseline start " + (loop + 1), BASELINE_STARTS[1][loop], df.format(assignment2.getBaselineStart(loop + 1)))

            assertEquals("Assignment 1 baseline finish " + (loop + 1), BASELINE_FINISHES[0][loop], df.format(assignment1.getBaselineFinish(loop + 1)))
            assertEquals("Assignment 2 baseline finish " + (loop + 1), BASELINE_FINISHES[1][loop], df.format(assignment2.getBaselineFinish(loop + 1)))

            assertEquals("Assignment 1 start " + (loop + 1), CUSTOM_START[0][loop], df.format(assignment1.getStart(loop + 1)))
            assertEquals("Assignment 2 start " + (loop + 1), CUSTOM_START[1][loop], df.format(assignment2.getStart(loop + 1)))

            assertEquals("Assignment 1 finish " + (loop + 1), CUSTOM_FINISH[0][loop], df.format(assignment1.getFinish(loop + 1)))
            assertEquals("Assignment 2 finish " + (loop + 1), CUSTOM_FINISH[1][loop], df.format(assignment2.getFinish(loop + 1)))

            assertEquals("Assignment 1 date " + (loop + 1), CUSTOM_DATE[0][loop], df.format(assignment1.getDate(loop + 1)))
            assertEquals("Assignment 2 date " + (loop + 1), CUSTOM_DATE[1][loop], df.format(assignment2.getDate(loop + 1)))

            assertEquals("Assignment 1 duration " + (loop + 1), CUSTOM_DURATION[0][loop], assignment1.getDuration(loop + 1).getDuration(), 0.01)
            assertEquals("Assignment 2 duration " + (loop + 1), CUSTOM_DURATION[1][loop], assignment2.getDuration(loop + 1).getDuration(), 0.01)
            assertEquals("Assignment 1 duration " + (loop + 1), TimeUnit.DAYS, assignment1.getDuration(loop + 1).getUnits())
            assertEquals("Assignment 2 duration " + (loop + 1), TimeUnit.DAYS, assignment2.getDuration(loop + 1).getUnits())

            assertEquals("Assignment 1 cost " + (loop + 1), CUSTOM_COST[0][loop], assignment1.getCost(loop + 1).doubleValue(), 0.01)
            assertEquals("Assignment 2 cost " + (loop + 1), CUSTOM_COST[1][loop], assignment2.getCost(loop + 1).doubleValue(), 0.01)
        }

        for (loop in CUSTOM_TEXT.indices) {
            assertEquals("Assignment 1 text " + (loop + 1), CUSTOM_TEXT[0][loop], assignment1.getText(loop + 1))
            assertEquals("Assignment 2 text " + (loop + 1), CUSTOM_TEXT[1][loop], assignment2.getText(loop + 1))
        }

        for (loop in CUSTOM_NUMBER.indices) {
            assertEquals("Assignment 1 number " + (loop + 1), CUSTOM_NUMBER[0][loop], assignment1.getNumber(loop + 1).intValue())
            assertEquals("Assignment 2 number " + (loop + 1), CUSTOM_NUMBER[1][loop], assignment2.getNumber(loop + 1).intValue())
        }

        for (loop in CUSTOM_FLAG.indices) {
            assertBooleanEquals("Assignment 1 flag " + (loop + 1), CUSTOM_FLAG[0][loop], assignment1.getFlag(loop + 1))
            assertBooleanEquals("Assignment 2 flag " + (loop + 1), CUSTOM_FLAG[1][loop], assignment2.getFlag(loop + 1))
        }
    }

    /**
     * Test assignment fields read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9Fields() {
        val reader = MPPReader()
        val mpp = reader.read(MpxjTestData.filePath("mpp9assignmentfields.mpp"))
        testFields(mpp, null, null)
    }

    /**
     * Test assignment fields read from an MPP9 file, saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9FieldsFrom14() {
        val reader = MPPReader()
        val mpp = reader.read(MpxjTestData.filePath("mpp9assignmentfields-from14.mpp"))
        testFields(mpp, null, null)
    }

    /**
     * Test assignment fields read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12Fields() {
        val reader = MPPReader()
        val mpp = reader.read(MpxjTestData.filePath("mpp12assignmentfields.mpp"))
        testFields(mpp, "230CA12B-3792-4F3B-B69E-89ABAF1C9042", "C3FDB823-3C82-422B-A854-391F7E235EA2")
    }

    /**
     * Test assignment fields read from an MPP12 file, saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12FieldsFrom14() {
        val reader = MPPReader()
        val mpp = reader.read(MpxjTestData.filePath("mpp12assignmentfields-from14.mpp"))
        testFields(mpp, "230CA12B-3792-4F3B-B69E-89ABAF1C9042", "C3FDB823-3C82-422B-A854-391F7E235EA2")
    }

    /**
     * Test assignment fields read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14Fields() {
        val reader = MPPReader()
        val mpp = reader.read(MpxjTestData.filePath("mpp14assignmentfields.mpp"))
        testFields(mpp, "81DC0978-D218-4D29-A139-EF691CDBF851", "0040EAF6-D0A2-41DF-9F67-A3CAEBCC8C5B")
    }

    /**
     * Test assignment fields read from an MSPDI file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMspdiFields() {
        val reader = MSPDIReader()
        val mpp = reader.read(MpxjTestData.filePath("mspdiassignmentfields.xml"))
        testFields(mpp, null, null)
    }

    /**
     * Test assignment fields read from an MPD file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpdFields() {
        assumeJvm()
        val reader = MPDDatabaseReader()
        val mpp = reader.read(MpxjTestData.filePath("mpdassignmentfields.mpd"))
        testFields(mpp, null, null)
    }

    /**
     * Common field value tests for project files.
     *
     * @param mpp project file
     * @param guid1 expected GUID - varies between file types
     * @param guid2 expected GUID - varies between file types
     */
    private fun testFields(mpp: ProjectFile, guid1: String?, guid2: String?) {
        val df = SimpleDateFormat("dd/MM/yy HH:mm")

        //
        // Retrieve the summary task
        //
        var task: Task
        var assignments: List<ResourceAssignment>
        var assignment: ResourceAssignment
        val mppFileType = NumberHelper.getInt(mpp.projectProperties.mppFileType)

        if (mppFileType > 9) {
            task = mpp.getTaskByID(Integer.valueOf(0))

            assignments = task.resourceAssignments
            assignment = assignments[0]
            assertEquals("Budget Work Resource", assignment.resource!!.name)
            assertEquals(97, (assignment.budgetWork.getDuration() as Int).toLong())
            assertEquals(98, (assignment.baselineBudgetWork.getDuration() as Int).toLong())
            // test budget flag?

            for (loop in 1..10) {
                val cost = assignment.getBaselineBudgetCost(loop)
                assertEquals(0, cost.intValue())
                val work = assignment.getBaselineBudgetWork(loop)
                assertEquals(loop.toLong(), (work.getDuration() as Int).toLong())
                assertEquals(TimeUnit.HOURS, work.getUnits())
            }

            assignment = assignments[1]
            assertEquals("Budget Cost Resource", assignment.resource!!.name)
            assertEquals(96, assignment.budgetCost.intValue())
            assertEquals(95, assignment.baselineBudgetCost.intValue())
            for (loop in 1..10) {
                val cost = assignment.getBaselineBudgetCost(loop)
                assertEquals(loop, cost.intValue())
                val work = assignment.getBaselineBudgetWork(loop)
                assertEquals(0, (work.getDuration() as Int).toLong())
            }
        }

        task = mpp.getTaskByID(Integer.valueOf(1))
        assignments = task.resourceAssignments
        assignment = assignments[0]
        assertEquals("Resource One", assignment.resource!!.name)

        assertDurationEquals(2.0, TimeUnit.HOURS, assignment.actualWork!!)
        assertDurationEquals(71.0, TimeUnit.HOURS, assignment.regularWork)
        assertDurationEquals(1.1, TimeUnit.HOURS, assignment.actualOvertimeWork)
        assertDurationEquals(7.9, TimeUnit.HOURS, assignment.remainingOvertimeWork)
        assertEquals(540, assignment.overtimeCost!!.intValue())

        //
        // Bizarre MPP12 bug? - shows as zero in MS Project
        //
        if (mppFileType != 12) {
            assertEquals(3978.92, assignment.remainingCost.doubleValue(), 0.005)
        }

        assertEquals(66.08, assignment.actualOvertimeCost!!.doubleValue(), 0.005)
        assertEquals(473.92, assignment.remainingOvertimeCost!!.doubleValue(), 0.005)
        //assertEquals(111.08, assignment.getACWP().doubleValue(), 0.001);
        //assertEquals(-111.08, assignment.getCV().doubleValue(), 0.001);
        assertEquals(4090.00, assignment.costVariance!!.doubleValue(), 0.001)
        assertEquals(3.0, assignment.percentageWorkComplete!!.doubleValue(), 0.5)
        assertEquals("Assignment Notes", assignment.notes.trim())

        if (mppFileType != 0) {
            assertTrue(assignment.confirmed)
            assertTrue(assignment.responsePending)
            assertFalse(assignment.teamStatusPending)
        }

        MpxjAssert.assertDurationEquals(80.0, TimeUnit.HOURS, assignment.workVariance!!)
        MpxjAssert.assertDurationEquals(2.0, TimeUnit.DAYS, assignment.startVariance)
        MpxjAssert.assertDurationEquals(-2.12, TimeUnit.DAYS, assignment.finishVariance)
        assertEquals(0, assignment.costRateTableIndex.toLong())

        //
        // Can't reliably find the create date in MPP9
        //
        if (mppFileType > 9) {
            assertEquals("06/07/11 12:09", df.format(assignment.createDate))
        }

        if (guid1 != null) {
            assertEquals(guid1, assignment.guid.toString().toUpperCase())
        }

        assignment = assignments[1]
        assertEquals("Resource Two", assignment.resource!!.name)

        MpxjAssert.assertDurationEquals(5.0, TimeUnit.HOURS, assignment.actualWork!!)
        MpxjAssert.assertDurationEquals(3.0, TimeUnit.HOURS, assignment.regularWork)
        MpxjAssert.assertDurationEquals(2.0, TimeUnit.HOURS, assignment.actualOvertimeWork)
        MpxjAssert.assertDurationEquals(18.0, TimeUnit.HOURS, assignment.remainingOvertimeWork)
        assertEquals(860, assignment.overtimeCost!!.intValue())
        assertEquals(774, assignment.remainingCost.doubleValue(), 0.005)
        assertEquals(86, assignment.actualOvertimeCost!!.doubleValue(), 0.005)
        assertEquals(774, assignment.remainingOvertimeCost!!.doubleValue(), 0.005)
        //assertEquals(188, assignment.getACWP().doubleValue(), 0.001);
        //assertEquals(-188, assignment.getCV().doubleValue(), 0.001);
        assertEquals(962, assignment.costVariance!!.doubleValue(), 0.001)
        assertEquals(22, assignment.percentageWorkComplete!!.doubleValue(), 0.5)
        assertEquals("", assignment.notes)
        MpxjAssert.assertDurationEquals(23.0, TimeUnit.HOURS, assignment.workVariance!!)
        MpxjAssert.assertDurationEquals(1.11, TimeUnit.DAYS, assignment.startVariance)
        MpxjAssert.assertDurationEquals(-10.39, TimeUnit.DAYS, assignment.finishVariance)
        assertEquals(1, assignment.costRateTableIndex.toLong())

        if (mppFileType != 0) {
            assertFalse(assignment.confirmed)
            assertFalse(assignment.responsePending)
            assertTrue(assignment.teamStatusPending)
            assertEquals("Test Hyperlink Screen Tip", assignment.hyperlinkScreenTip)
        }

        if (mppFileType > 9) {
            assertEquals("06/07/11 15:31", df.format(assignment.createDate))
        }

        if (guid2 != null) {
            assertEquals(guid2, assignment.guid.toString().toUpperCase())
        }

        assertEquals("Test Hyperlink Display Text", assignment.hyperlink)
        assertEquals("http://news.bbc.co.uk", assignment.hyperlinkAddress)
        assertEquals("x", assignment.hyperlinkSubAddress)
    }

    companion object {

        private val BASELINE_COSTS = arrayOf(intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), intArrayOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20))

        private val BASELINE_WORKS = arrayOf(intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), intArrayOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20))

        private val BASELINE_STARTS = arrayOf(arrayOf("01/01/10 08:00", "02/01/10 08:00", "03/01/10 08:00", "04/01/10 08:00", "05/01/10 08:00", "06/01/10 08:00", "07/01/10 08:00", "08/01/10 08:00", "09/01/10 08:00", "10/01/10 08:00"), arrayOf("01/02/10 08:00", "02/02/10 08:00", "03/02/10 08:00", "04/02/10 08:00", "05/02/10 08:00", "06/02/10 08:00", "07/02/10 08:00", "08/02/10 08:00", "09/02/10 08:00", "10/02/10 08:00"))

        private val BASELINE_FINISHES = arrayOf(arrayOf("01/01/09 17:00", "02/01/09 17:00", "03/01/09 17:00", "04/01/09 17:00", "05/01/09 17:00", "06/01/09 17:00", "07/01/09 17:00", "08/01/09 17:00", "09/01/09 17:00", "10/01/09 17:00"), arrayOf("01/02/09 17:00", "02/02/09 17:00", "03/02/09 17:00", "04/02/09 17:00", "05/02/09 17:00", "06/02/09 17:00", "07/02/09 17:00", "08/02/09 17:00", "09/02/09 17:00", "10/02/09 17:00"))

        private val CUSTOM_TEXT = arrayOf(arrayOf("t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9", "t10", "t11", "t12", "t13", "t14", "t15", "t16", "t17", "t18", "t19", "t20", "t21", "t22", "t23", "t24", "t25", "t26", "t27", "t28", "t29", "t30"), arrayOf("a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "a10", "a11", "a12", "a13", "a14", "a15", "a16", "a17", "a18", "a19", "a20", "a21", "a22", "a23", "a24", "a25", "a26", "a27", "a28", "a29", "a30"))

        private val CUSTOM_START = arrayOf(arrayOf("01/01/11 08:00", "02/01/11 08:00", "03/01/11 08:00", "04/01/11 08:00", "05/06/11 08:00", "06/01/11 08:00", "07/01/11 08:00", "08/01/11 08:00", "09/01/11 08:00", "10/01/11 08:00"), arrayOf("01/02/11 08:00", "02/02/11 08:00", "03/02/11 08:00", "04/02/11 08:00", "05/02/11 08:00", "06/02/11 08:00", "07/02/11 08:00", "08/02/11 08:00", "09/02/11 08:00", "10/02/11 08:00"))

        private val CUSTOM_FINISH = arrayOf(arrayOf("01/03/11 17:00", "02/03/11 17:00", "03/03/11 17:00", "04/03/11 17:00", "05/03/11 17:00", "06/03/11 17:00", "07/03/11 17:00", "08/03/11 17:00", "09/03/11 17:00", "10/03/11 17:00"), arrayOf("01/04/11 17:00", "02/04/11 17:00", "03/04/11 17:00", "04/04/11 17:00", "05/04/11 17:00", "06/04/11 17:00", "07/04/11 17:00", "08/04/11 17:00", "09/04/11 17:00", "10/04/11 17:00"))

        private val CUSTOM_DATE = arrayOf(arrayOf("01/05/11 08:00", "02/05/11 08:00", "03/05/11 08:00", "04/05/11 08:00", "05/05/11 08:00", "06/05/11 08:00", "07/05/11 08:00", "08/05/11 08:00", "09/05/11 08:00", "10/05/11 08:00"), arrayOf("01/06/11 08:00", "02/06/11 08:00", "03/06/11 08:00", "04/06/11 08:00", "05/06/11 08:00", "06/06/11 08:00", "07/06/11 08:00", "08/06/11 08:00", "09/06/11 08:00", "10/06/11 08:00"))

        private val CUSTOM_NUMBER = arrayOf(intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20), intArrayOf(21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40))

        private val CUSTOM_DURATION = arrayOf(doubleArrayOf(0.13, 0.25, 0.38, 0.5, 0.63, 0.75, 0.88, 1.0, 1.13, 1.25), doubleArrayOf(1.38, 1.5, 1.63, 1.75, 1.88, 2.0, 2.13, 2.25, 2.38, 2.5))

        private val CUSTOM_COST = arrayOf(doubleArrayOf(0.01, 0.20, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.10), doubleArrayOf(0.11, 0.12, 0.13, 0.14, 0.15, 0.16, 0.17, 0.18, 0.19, 0.20))

        private val CUSTOM_FLAG = arrayOf(booleanArrayOf(true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false), booleanArrayOf(false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true))
    }
}
