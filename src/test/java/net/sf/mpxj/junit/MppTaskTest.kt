/*
 * file:       MppTaskTest.java
 * author:     Wade Golden
 * copyright:  (c) Packwood Software 2006
 * date:       22-August-2006
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

import net.sf.mpxj.AccrueType
import net.sf.mpxj.ConstraintType
import net.sf.mpxj.DateRange
import net.sf.mpxj.Duration
import net.sf.mpxj.Priority
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.Task
import net.sf.mpxj.TaskType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpp.MPPReader
import net.sf.mpxj.mspdi.MSPDIReader

import org.junit.Test

/**
 * Tests to exercise MPP file read functionality for various versions of
 * MPP file.
 */
class MppTaskTest {

    private val m_df = SimpleDateFormat("dd/MM/yyyy HH:mm")

    /**
     * Test task data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9Task() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9task.mpp"))
        testBasicTask(mpp)
    }

    /**
     * Test task data read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9TaskFrom12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9task-from12.mpp"))
        testBasicTask(mpp)
    }

    /**
     * Test task data read from an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9TaskFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9task-from14.mpp"))
        testBasicTask(mpp)
    }

    /**
     * Test task data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12Task() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12task.mpp"))
        testBasicTask(mpp)
    }

    /**
     * Test task data read from an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12TaskFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12task-from14.mpp"))
        testBasicTask(mpp)
    }

    /**
     * Test task data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14Task() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14task.mpp"))
        testBasicTask(mpp)
    }

    /**
     * Test task data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14TaskFromProject2013() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14task-from2013.mpp"))
        testBasicTask(mpp)
    }

    /**
     * Test task data read from an MPD9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpd9Task() {
        assumeJvm()
        val mpp = MPDDatabaseReader().read(MpxjTestData.filePath("mpp9task.mpd"))
        testBasicTask(mpp)
    }

    /**
     * Test task data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9Baseline() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9baseline.mpp"))
        testBaselineTasks(mpp)
    }

    /**
     * Test task data read from an MPP9 file saved from Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9BaselineFrom12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9baseline-from12.mpp"))
        testBaselineTasks(mpp)
    }

    /**
     * Test task data read from an MPP9 file saved from Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9BaselineFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9baseline-from14.mpp"))
        testBaselineTasks(mpp)
    }

    /**
     * Test task data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12Baseline() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12baseline.mpp"))
        testBaselineTasks(mpp)
    }

    /**
     * Test task data read from an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12BaselineFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12baseline-from14.mpp"))
        testBaselineTasks(mpp)
    }

    /**
     * Test task data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14Baseline() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14baseline.mpp"))
        testBaselineTasks(mpp)
    }

    /**
     * Test task data read from an MPD9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpd9Baseline() {
        assumeJvm()
        val mpp = MPDDatabaseReader().read(MpxjTestData.filePath("mpp9baseline.mpd"))
        testBaselineTasks(mpp)
    }

    /**
     * Test Split Tasks in an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9Splits() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9splittask.mpp"))
        testSplitTasks(mpp)
    }

    /**
     * Test Split Tasks in an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9SplitsFrom12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9splittask-from12.mpp"))
        testSplitTasks(mpp)
    }

    /**
     * Test Split Tasks in an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9SplitsFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9splittask-from14.mpp"))
        testSplitTasks(mpp)
    }

    /**
     * Test Split Tasks in an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12Splits() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12splittask.mpp"))
        testSplitTasks(mpp)
    }

    /**
     * Test Split Tasks in an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12SplitsFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12splittask-from14.mpp"))
        testSplitTasks(mpp)
    }

    /**
     * Test Split Tasks in an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14Splits() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14splittask.mpp"))
        testSplitTasks(mpp)
    }

    /**
     * Test Split Tasks in an MSPDI file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMspdiSplits() {
        val mpp = MSPDIReader().read(MpxjTestData.filePath("mspdisplittask.xml"))
        testSplitTasks(mpp)
    }

    /**
     * Test Split Tasks in an MPD9 file.
     *
     * Currently split tasks are not supported in MPD files.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpd9Splits() {
        //       ProjectFile mpp = new MPDDatabaseReader().read (MpxjTestData.filePath("mpp9splittask.mpd");
        //       testSplitTasks(mpp);
    }

    /**
     * Tests Relations in an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9Relations() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9relations.mpp"))
        testRelations(mpp)
    }

    /**
     * Tests Relations in an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9RelationsFrom12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9relations-from12.mpp"))
        testRelations(mpp)
    }

    /**
     * Tests Relations in an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9RelationsFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9relations-from14.mpp"))
        testRelations(mpp)
    }

    /**
     * Tests Relations in an MPD9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpd9Relations() {
        assumeJvm()
        val mpp = MPDDatabaseReader().read(MpxjTestData.filePath("mpp9relations.mpd"))
        testRelations(mpp)
    }

    /**
     * Tests Relations in an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12Relations() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12relations.mpp"))
        testRelations(mpp)
    }

    /**
     * Tests Relations in an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12RelationsFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12relations-from14.mpp"))
        testRelations(mpp)
    }

    /**
     * Tests Relations in an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14Relations() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14relations.mpp"))
        testRelations(mpp)
    }

    /**
     * Tests Relations in an MSPDI file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMspdiRelations() {
        val mpp = MSPDIReader().read(MpxjTestData.filePath("mspdirelations.xml"))
        testRelations(mpp)
    }

    /**
     * Tests dozens of basic fields of a Task.
     * @param mpp The ProjectFile being tested.
     * @throws Exception
     *
     * <br></br><br></br>
     * Columns not tested:<br></br><br></br>
     *
     * Overtime Cost, Remaining Overtime Cost, Actual Overtime Cost<br></br>
     * Overtime Work, Remaining Overtime Work, Actual Overtime Work<br></br>
     * Actual Start<br></br>
     * Actual Finish<br></br>
     * Baseline Cost<br></br>
     * Baseline Start<br></br>
     * Baseline Finish<br></br>
     * Baseline Duration<br></br>
     * Baseline Work<br></br>
     * Confirmed (??? - don't know how to make this field 'Yes' in Project)<br></br>
     * Cost Rate Table (calculated in Project)<br></br>
     * Critical (Calculated in Steelray)<br></br>
     * Estimated (test in another method)<br></br>
     * External Task<br></br>
     * Group By Summary<br></br>
     * Hyperlink Subaddress<br></br>
     * Linked Fields<br></br>
     * Outline Code1-10<br></br>
     * Objects<br></br>
     * Overallocated<br></br>
     * Preleveled Start<br></br>
     * Preleveled Finish<br></br>
     * Recurring<br></br>
     * Resource Phonetics<br></br>
     * Resource Type<br></br>
     * Response Pending<br></br>
     * Subproject File<br></br>
     * Subproject Read Only<br></br>
     * Predecessors<br></br>
     * Summary<br></br>
     * Task Calendar<br></br>
     * Team Status Pending<br></br>
     * Type<br></br>
     * Unique ID Predecessors, Unique ID Succeessors<br></br>
     * Update Needed<br></br>
     * WBS Predecessors, WBS Succeessors<br></br>
     * Work Contour<br></br><br></br><br></br>
     *
     * Fields that are not supported in the MPP9 format (usually return null or false)<br></br><br></br>
     *
     * AWCP<br></br>
     * BCWP<br></br>
     * BCWS<br></br>
     * CV<br></br>
     * Hyperlink Href<br></br>
     * Project<br></br>
     * Regular Work<br></br>
     * Resource Group<br></br>
     * Resource Initials<br></br>
     * Resource Names<br></br>
     * Successors<br></br>
     * SV<br></br>
     * VAC<br></br>
     */
    @Throws(Exception::class)
    private fun testBasicTask(mpp: ProjectFile) {

        val df = SimpleDateFormat("dd/MM/yyyy")

        val properties = mpp.projectProperties
        assertNotNull(properties)

        // test various global properties
        val listAllTasks = mpp.tasks
        val listAllResources = mpp.resources
        assertNotNull(listAllTasks)
        assertNotNull(listAllResources)
        assertEquals(2, listAllTasks.size())
        // This will fail in MPP12 as we appear to have a summary resource
        //assertEquals(1, listAllResources.size());

        /* Test Specifics */

        // task 0 - the project task
        var task = listAllTasks.get(0)
        assertNotNull(task)
        assertEquals(0, task.id!!.intValue())
        assertEquals("MPP12 Test", task.name)

        // task 1
        task = listAllTasks.get(1)
        assertNotNull(task)
        assertEquals(1, task.id!!.intValue())
        // name
        assertEquals("Task #1", task.name)
        // start and finish
        val expectedStart = "23/08/2006"
        val expectedFinish = "29/08/2006"
        assertEquals(expectedStart, df.format(task.start))
        assertEquals(expectedFinish, df.format(task.finish))
        // no predecessors
        assertTrue(task.predecessors.isEmpty())
        // duration
        var expectedDuration = Duration.getInstance(1, TimeUnit.WEEKS)
        // note that this is a direct comparison - TimeUnit must match
        assertEquals(expectedDuration, task.duration)
        // work
        expectedDuration = Duration.getInstance(40, TimeUnit.HOURS)
        assertEquals(expectedDuration, task.work)
        // percent complete
        var expectedPctComp = Double.valueOf(45)
        assertEquals(expectedPctComp, task.percentageComplete)
        // percent work complete
        expectedPctComp = Double.valueOf(45)
        assertEquals(expectedPctComp, task.percentageWorkComplete)
        // cost
        var expectedCost = Double.valueOf(5000)
        assertEquals(expectedCost, task.cost)
        // actual cost
        expectedCost = Double.valueOf(2800)
        assertEquals(expectedCost, task.actualCost)
        // fixed cost
        expectedCost = Double.valueOf(1000)
        assertEquals(expectedCost, task.fixedCost)
        // remaining cost
        expectedCost = Double.valueOf(2200)
        assertEquals(expectedCost, task.remainingCost)
        // actual work
        expectedDuration = Duration.getInstance(18, TimeUnit.HOURS)
        assertEquals(expectedDuration, expectedDuration)
        // contact
        val expectedContact = "wade"
        assertEquals(expectedContact, task.contact)
        // constraint date
        assertEquals(expectedStart, df.format(task.constraintDate))
        // constraint type
        assertEquals(ConstraintType.MUST_START_ON, task.constraintType)
        // custom cost columns
        expectedCost = Double.valueOf(1)
        assertEquals(expectedCost, task.getCost(1))
        expectedCost = Double.valueOf(2)
        assertEquals(expectedCost, task.getCost(2))
        expectedCost = Double.valueOf(3)
        assertEquals(expectedCost, task.getCost(3))
        expectedCost = Double.valueOf(4)
        assertEquals(expectedCost, task.getCost(4))
        expectedCost = Double.valueOf(5)
        assertEquals(expectedCost, task.getCost(5))
        expectedCost = Double.valueOf(6)
        assertEquals(expectedCost, task.getCost(6))
        expectedCost = Double.valueOf(7)
        assertEquals(expectedCost, task.getCost(7))
        expectedCost = Double.valueOf(8)
        assertEquals(expectedCost, task.getCost(8))
        expectedCost = Double.valueOf(9)
        assertEquals(expectedCost, task.getCost(9))
        expectedCost = Double.valueOf(10)
        assertEquals(expectedCost, task.getCost(10))
        // cost variance
        expectedCost = Double.valueOf(5000)
        assertEquals(expectedCost, task.costVariance)
        // created
        //Date dateExpected = new Date(1156360320000L);
        assertEquals(expectedStart, df.format(task.createDate))
        // custom date columns
        assertEquals("25/08/2006", df.format(task.getDate(1)))
        assertEquals("26/08/2006", df.format(task.getDate(2)))
        assertEquals("27/08/2006", df.format(task.getDate(3)))
        assertEquals("28/08/2006", df.format(task.getDate(4)))
        assertEquals("29/08/2006", df.format(task.getDate(5)))
        assertEquals("30/08/2006", df.format(task.getDate(6)))
        assertEquals("31/08/2006", df.format(task.getDate(7)))
        assertEquals("01/09/2006", df.format(task.getDate(8)))
        assertEquals("02/09/2006", df.format(task.getDate(9)))
        assertEquals("03/09/2006", df.format(task.getDate(10)))
        // deadline
        assertEquals("30/08/2006", df.format(task.deadline))
        // custom duration columns
        expectedDuration = Duration.getInstance(1, TimeUnit.DAYS)
        assertEquals(expectedDuration, task.getDuration(1))
        expectedDuration = Duration.getInstance(2, TimeUnit.DAYS)
        assertEquals(expectedDuration, task.getDuration(2))
        expectedDuration = Duration.getInstance(3, TimeUnit.DAYS)
        assertEquals(expectedDuration, task.getDuration(3))
        expectedDuration = Duration.getInstance(4, TimeUnit.DAYS)
        assertEquals(expectedDuration, task.getDuration(4))
        expectedDuration = Duration.getInstance(5, TimeUnit.DAYS)
        assertEquals(expectedDuration, task.getDuration(5))
        expectedDuration = Duration.getInstance(6, TimeUnit.DAYS)
        assertEquals(expectedDuration, task.getDuration(6))
        expectedDuration = Duration.getInstance(7, TimeUnit.DAYS)
        assertEquals(expectedDuration, task.getDuration(7))
        expectedDuration = Duration.getInstance(8, TimeUnit.DAYS)
        assertEquals(expectedDuration, task.getDuration(8))
        expectedDuration = Duration.getInstance(9, TimeUnit.DAYS)
        assertEquals(expectedDuration, task.getDuration(9))
        expectedDuration = Duration.getInstance(10, TimeUnit.DAYS)
        assertEquals(expectedDuration, task.getDuration(10))
        // duration variance
        expectedDuration = Duration.getInstance(1, TimeUnit.WEEKS)
        assertEquals(expectedDuration, task.durationVariance)
        // early start and finish
        assertEquals(expectedStart, df.format(task.earlyStart))
        assertEquals(expectedFinish, df.format(task.earlyFinish))
        // effort driven
        assertTrue("Effort Driven does not match", task.effortDriven) // should return true
        // custom start columns
        assertEquals("25/08/2006", df.format(task.getStart(1)))
        assertEquals("26/08/2006", df.format(task.getStart(2)))
        assertEquals("27/08/2006", df.format(task.getStart(3)))
        assertEquals("28/08/2006", df.format(task.getStart(4)))
        assertEquals("29/08/2006", df.format(task.getStart(5)))
        assertEquals("30/08/2006", df.format(task.getStart(6)))
        assertEquals("31/08/2006", df.format(task.getStart(7)))
        assertEquals("01/09/2006", df.format(task.getStart(8)))
        assertEquals("02/09/2006", df.format(task.getStart(9)))
        assertEquals("03/09/2006", df.format(task.getStart(10)))
        // custom finish columns
        assertEquals("25/08/2006", df.format(task.getFinish(1)))
        assertEquals("26/08/2006", df.format(task.getFinish(2)))
        assertEquals("27/08/2006", df.format(task.getFinish(3)))
        assertEquals("28/08/2006", df.format(task.getFinish(4)))
        assertEquals("29/08/2006", df.format(task.getFinish(5)))
        assertEquals("30/08/2006", df.format(task.getFinish(6)))
        assertEquals("31/08/2006", df.format(task.getFinish(7)))
        assertEquals("01/09/2006", df.format(task.getFinish(8)))
        assertEquals("02/09/2006", df.format(task.getFinish(9)))
        assertEquals("03/09/2006", df.format(task.getFinish(10)))
        // finish slack
        expectedDuration = Duration.getInstance(0, TimeUnit.WEEKS) // use for Finish Slack, Start Slack, Free Slack
        assertEquals(expectedDuration, task.finishSlack)
        // start slack
        assertEquals(expectedDuration, task.startSlack)
        // free slack
        assertEquals(expectedDuration, task.freeSlack)
        // finish variance
        expectedDuration = Duration.getInstance(0, TimeUnit.DAYS)
        assertEquals(expectedDuration, task.finishVariance)
        // fixed cost accrual
        assertEquals(AccrueType.START, task.fixedCostAccrual)
        // custom flag columns
        val expectedValue = true
        assertBooleanEquals(expectedValue, task.getFlag(1))
        assertBooleanEquals(expectedValue, task.getFlag(2))
        assertBooleanEquals(expectedValue, task.getFlag(3))
        assertBooleanEquals(expectedValue, task.getFlag(4))
        assertBooleanEquals(expectedValue, task.getFlag(5))
        assertBooleanEquals(expectedValue, task.getFlag(6))
        assertBooleanEquals(expectedValue, task.getFlag(7))
        assertBooleanEquals(expectedValue, task.getFlag(8))
        assertBooleanEquals(expectedValue, task.getFlag(9))
        assertBooleanEquals(expectedValue, task.getFlag(10))
        assertBooleanEquals(expectedValue, task.getFlag(11))
        assertBooleanEquals(expectedValue, task.getFlag(12))
        assertBooleanEquals(expectedValue, task.getFlag(13))
        assertBooleanEquals(expectedValue, task.getFlag(14))
        assertBooleanEquals(expectedValue, task.getFlag(15))
        assertBooleanEquals(expectedValue, task.getFlag(16))
        assertBooleanEquals(expectedValue, task.getFlag(17))
        assertBooleanEquals(expectedValue, task.getFlag(18))
        assertBooleanEquals(expectedValue, task.getFlag(19))
        assertBooleanEquals(expectedValue, task.getFlag(20))
        // hide bar
        assertBooleanEquals(expectedValue, task.hideBar)
        // hyperlink
        val expectedHyperlink = "http://www.steelray.com"
        assertEquals(expectedHyperlink, task.hyperlink)
        // hyperlink address
        assertEquals(expectedHyperlink, task.hyperlinkAddress)
        // ignore resource calendar
        // (note that 'false' is the default value, so this isn't really a test of MPXJ -
        // I couldn't change the value in Project, though)
        assertFalse(task.ignoreResourceCalendar)
        // late start and finish
        assertEquals(expectedStart, df.format(task.lateStart))
        assertEquals(expectedFinish, df.format(task.lateFinish))
        // leveling
        assertFalse(task.levelAssignments)
        assertFalse(task.levelingCanSplit)
        // leveling delay
        expectedDuration = Duration.getInstance(0, TimeUnit.ELAPSED_DAYS)
        assertEquals(expectedDuration, task.levelingDelay)
        // marked
        assertTrue(task.marked)
        // milestone
        assertTrue(task.milestone)
        // Notes
        assertEquals("Notes Example", task.notes)
        // custom number columns
        assertEquals(Double.valueOf(1), task.getNumber(1))
        assertEquals(Double.valueOf(2), task.getNumber(2))
        assertEquals(Double.valueOf(3), task.getNumber(3))
        assertEquals(Double.valueOf(4), task.getNumber(4))
        assertEquals(Double.valueOf(5), task.getNumber(5))
        assertEquals(Double.valueOf(6), task.getNumber(6))
        assertEquals(Double.valueOf(7), task.getNumber(7))
        assertEquals(Double.valueOf(8), task.getNumber(8))
        assertEquals(Double.valueOf(9), task.getNumber(9))
        assertEquals(Double.valueOf(10), task.getNumber(10))
        assertEquals(Double.valueOf(11), task.getNumber(11))
        assertEquals(Double.valueOf(12), task.getNumber(12))
        assertEquals(Double.valueOf(13), task.getNumber(13))
        assertEquals(Double.valueOf(14), task.getNumber(14))
        assertEquals(Double.valueOf(15), task.getNumber(15))
        assertEquals(Double.valueOf(16), task.getNumber(16))
        assertEquals(Double.valueOf(17), task.getNumber(17))
        assertEquals(Double.valueOf(18), task.getNumber(18))
        assertEquals(Double.valueOf(19), task.getNumber(19))
        assertEquals(Double.valueOf(20), task.getNumber(20))
        // outline level
        assertEquals(Integer.valueOf(1), task.outlineLevel)
        // outline codes
        assertEquals("1", task.outlineNumber)
        assertEquals("1", task.getOutlineCode(1))
        assertEquals("A", task.getOutlineCode(2))
        assertEquals("a", task.getOutlineCode(3))
        assertEquals("Aa", task.getOutlineCode(4))
        assertEquals("5", task.getOutlineCode(5))
        assertEquals("6", task.getOutlineCode(6))
        assertEquals("7", task.getOutlineCode(7))
        assertEquals("8", task.getOutlineCode(8))
        assertEquals("9", task.getOutlineCode(9))
        assertEquals("10", task.getOutlineCode(10))
        // priority
        assertEquals(Priority.getInstance(600), task.priority)
        // remaining work
        expectedDuration = Duration.getInstance(22, TimeUnit.HOURS)
        assertEquals(expectedDuration, task.remainingWork)
        // remaining duration
        expectedDuration = Duration.getInstance(0.55, TimeUnit.WEEKS)
        assertEquals(expectedDuration, task.remainingDuration)
        // resume
        assertEquals("25/08/2006", df.format(task.resume))
        // rollup
        assertTrue(task.rollup)
        // start slack
        expectedDuration = Duration.getInstance(0, TimeUnit.WEEKS)
        assertEquals(expectedDuration, task.startSlack)
        // start variance
        expectedDuration = Duration.getInstance(0, TimeUnit.DAYS)
        assertEquals(expectedDuration, task.startVariance)
        // stop
        assertEquals("25/08/2006", df.format(task.stop))
        // total slack
        expectedDuration = Duration.getInstance(0, TimeUnit.WEEKS)
        assertEquals(expectedDuration, task.totalSlack)
        // custom text columns
        assertEquals("1", task.getText(1))
        assertEquals("2", task.getText(2))
        assertEquals("3", task.getText(3))
        assertEquals("4", task.getText(4))
        assertEquals("5", task.getText(5))
        assertEquals("6", task.getText(6))
        assertEquals("7", task.getText(7))
        assertEquals("8", task.getText(8))
        assertEquals("9", task.getText(9))
        assertEquals("10", task.getText(10))
        assertEquals("11", task.getText(11))
        assertEquals("12", task.getText(12))
        assertEquals("13", task.getText(13))
        assertEquals("14", task.getText(14))
        assertEquals("15", task.getText(15))
        assertEquals("16", task.getText(16))
        assertEquals("17", task.getText(17))
        assertEquals("18", task.getText(18))
        assertEquals("19", task.getText(19))
        assertEquals("20", task.getText(20))
        assertEquals("21", task.getText(21))
        assertEquals("22", task.getText(22))
        assertEquals("23", task.getText(23))
        assertEquals("24", task.getText(24))
        assertEquals("25", task.getText(25))
        assertEquals("26", task.getText(26))
        assertEquals("27", task.getText(27))
        assertEquals("28", task.getText(28))
        assertEquals("29", task.getText(29))
        assertEquals("30", task.getText(30))
        // unique id
        assertEquals(Integer.valueOf(1), task.uniqueID)
        // wbs
        assertEquals("1", task.wbs)
        // work variance
        expectedDuration = Duration.getInstance(40, TimeUnit.HOURS)
        assertEquals(expectedDuration, task.workVariance)
    }

    /**
     * Tests fields related to Baseline information, as well as actual
     * dates, estimated, and other fields (see below).
     *
     * @param mpp The ProjectFile being tested.
     * @throws Exception
     *
     * <br></br><br></br>
     * Columns tested:<br></br><br></br>
     *
     * Actual Start<br></br>
     * Actual Finish<br></br>
     * Baseline Start<br></br>
     * Baseline Finish<br></br>
     * Baseline Duration<br></br>
     * Baseline Work<br></br>
     * Estimated<br></br>
     * Predecessors<br></br>
     * Summary<br></br>
     * Outline Number<br></br>
     * WBS<br></br>
     */
    @Throws(Exception::class)
    private fun testBaselineTasks(mpp: ProjectFile) {
        /**
         * Columns tested:
         *
         * Actual Start
         * Actual Finish
         * Baseline Start
         * Baseline Finish
         * Baseline Duration
         * Baseline Work
         * Estimated
         * Predecessors
         * Summary
         * Outline Number
         * WBS
         */

        val df = SimpleDateFormat("dd/MM/yyyy")

        val listAllTasks = mpp.tasks
        val listAllResources = mpp.resources
        assertNotNull(listAllTasks)
        assertNotNull(listAllResources)
        assertTrue(listAllTasks.size() > 0)
        assertTrue(listAllResources.size() > 0)

        val baseTask: Task
        val subtask1: Task
        val subtask2: Task
        val subtask3: Task
        val subtask4: Task
        val subtask5: Task
        val completeTask: Task
        val complexOutlineNumberTask: Task
        val subtaskA: Task
        val subtaskA1: Task
        val subtaskA2: Task
        val subtaskB: Task
        val subtaskB1: Task
        val subtaskB1a: Task

        // verify that the get()s match the right tasks
        baseTask = listAllTasks.get(1)
        assertEquals("Base Task", baseTask.name)
        subtask1 = listAllTasks.get(2)
        assertEquals("Subtask 1", subtask1.name)
        subtask2 = listAllTasks.get(3)
        assertEquals("Subtask 2", subtask2.name)
        subtask3 = listAllTasks.get(4)
        assertEquals("Subtask 3", subtask3.name)
        subtask4 = listAllTasks.get(5)
        assertEquals("Subtask 4", subtask4.name)
        subtask5 = listAllTasks.get(6)
        assertEquals("Subtask 5", subtask5.name)
        completeTask = listAllTasks.get(7)
        assertEquals("Complete", completeTask.name)
        complexOutlineNumberTask = listAllTasks.get(8)
        assertEquals("Complex Outline Number", complexOutlineNumberTask.name)
        subtaskA = listAllTasks.get(9)
        assertEquals("Subtask A", subtaskA.name)
        subtaskA1 = listAllTasks.get(10)
        assertEquals("Subtask A1", subtaskA1.name)
        subtaskA2 = listAllTasks.get(11)
        assertEquals("Subtask A2", subtaskA2.name)
        subtaskB = listAllTasks.get(12)
        assertEquals("Subtask B", subtaskB.name)
        subtaskB1 = listAllTasks.get(13)
        assertEquals("Subtask B1", subtaskB1.name)
        subtaskB1a = listAllTasks.get(14)
        assertEquals("Subtask B1a", subtaskB1a.name)

        // Baseline for 'Base Task'
        assertEquals("24/08/2006", df.format(baseTask.baselineStart))
        assertEquals("13/09/2006", df.format(baseTask.baselineFinish))

        // Actual for 'Base Task'
        assertEquals("24/08/2006", df.format(baseTask.actualStart))
        assertEquals(null, baseTask.actualFinish)

        // % Complete
        assertEquals(Double.valueOf(57), baseTask.percentageComplete)
        // Type for 'Base Task'
        assertEquals(TaskType.FIXED_DURATION, baseTask.type)

        // Test 'Subtask 2' baseline opposed to planned and actual
        // Planned for 'Subtask 2'
        assertEquals("30/08/2006", df.format(subtask2.start))
        assertEquals("05/09/2006", df.format(subtask2.finish))

        // Actual for 'Subtask 2'
        assertEquals("30/08/2006", df.format(subtask2.actualStart))
        assertEquals("05/09/2006", df.format(subtask2.actualFinish))

        // Baseline for 'Subtask 2'
        assertEquals("29/08/2006", df.format(subtask2.baselineStart))
        assertEquals("01/09/2006", df.format(subtask2.baselineFinish))

        // Predecessor for Subtask 2
        var predecessors = subtask2.predecessors
        assertEquals(1, predecessors.size())
        var relation = predecessors.get(0)
        // check task unique id that's stored in the Relation
        assertEquals(subtask1.uniqueID, relation.targetTask.uniqueID)
        // check task id stored in the Task that's stored in the relation
        val predTask = relation.targetTask
        assertEquals(predTask.id, subtask1.id)
        // check task unique id stored in the Task that's stored in the relation
        assertEquals(predTask.uniqueID, subtask1.uniqueID)
        // Type for 'Subtask 2'
        assertEquals(TaskType.FIXED_UNITS, subtask2.type)

        // Predecessors for Subtask 5 (multiple predecessors)
        predecessors = subtask5.predecessors
        assertEquals(2, predecessors.size())
        relation = predecessors.get(0)
        val relation2 = predecessors.get(1)
        assertEquals(subtask3.uniqueID, relation.targetTask.uniqueID)
        assertEquals(subtask4.uniqueID, relation2.targetTask.uniqueID)
        // Type for subtask 5
        assertEquals(TaskType.FIXED_WORK, subtask5.type)

        // Summary for 'Subtask A1'
        assertTrue(subtaskA.summary)

        // Estimated for 'Subtask A1'
        assertTrue(subtaskA1.estimated)
        // Outline Number and WBS for 'Subtask A1'
        var outlineNumber = "2.1.1"
        assertEquals(outlineNumber, subtaskA1.outlineNumber)
        assertEquals(outlineNumber, subtaskA1.wbs)

        // Outline Number and WBS for 'Subtask B1a'
        outlineNumber = "2.2.1.1"
        assertEquals(outlineNumber, subtaskB1a.outlineNumber)
        assertEquals(outlineNumber, subtaskB1a.wbs)
    }

    /**
     * Tests Split Tasks.
     *
     * @param mpp MPP file
     */
    private fun testSplitTasks(mpp: ProjectFile) {
        val task1 = mpp.getTaskByID(Integer.valueOf(1))
        val task2 = mpp.getTaskByID(Integer.valueOf(2))

        val listSplits1 = task1.splits
        val listSplits2 = task2.splits

        assertEquals(3, listSplits1!!.size())
        assertEquals(5, listSplits2!!.size())

        testSplit(listSplits1.get(0), "21/09/2006 08:00", "26/09/2006 17:00")
        testSplit(listSplits1.get(1), "27/09/2006 08:00", "29/09/2006 17:00")
        testSplit(listSplits1.get(2), "02/10/2006 08:00", "09/10/2006 17:00")

        testSplit(listSplits2.get(0), "21/09/2006 08:00", "25/09/2006 17:00")
        testSplit(listSplits2.get(1), "26/09/2006 08:00", "27/09/2006 17:00")
        testSplit(listSplits2.get(2), "28/09/2006 08:00", "04/10/2006 17:00")
        testSplit(listSplits2.get(3), "05/10/2006 08:00", "09/10/2006 17:00")
        testSplit(listSplits2.get(4), "10/10/2006 08:00", "18/10/2006 17:00")
    }

    /**
     * Utility method to test a split task date range.
     *
     * @param range DateRange instance
     * @param start expected start date
     * @param end expected end date
     */
    private fun testSplit(range: DateRange, start: String, end: String) {
        assertEquals(start, m_df.format(range.start))
        assertEquals(end, m_df.format(range.end))
    }

    /**
     * Tests Relations.
     *
     * @param mpp mpp file
     */
    private fun testRelations(mpp: ProjectFile) {
        val listAllTasks = mpp.tasks
        assertNotNull(listAllTasks)

        val task1 = mpp.getTaskByID(Integer.valueOf(1))
        val task2 = mpp.getTaskByID(Integer.valueOf(2))
        val task3 = mpp.getTaskByID(Integer.valueOf(3))
        val task4 = mpp.getTaskByID(Integer.valueOf(4))
        val task5 = mpp.getTaskByID(Integer.valueOf(5))

        var listPreds = task2.predecessors
        var relation = listPreds.get(0)
        assertEquals(1, relation.targetTask.uniqueID.intValue())
        assertEquals(RelationType.FINISH_START, relation.type)
        assertEquals(task1, relation.targetTask)

        listPreds = task3.predecessors
        relation = listPreds.get(0)
        assertEquals(2, relation.targetTask.uniqueID.intValue())
        assertEquals(RelationType.START_START, relation.type)
        val duration = relation.lag
        if (duration!!.getUnits() === TimeUnit.DAYS) {
            assertEquals(1, (duration!!.getDuration() as Int).toLong())
        } else {
            if (duration!!.getUnits() === TimeUnit.HOURS) {
                assertEquals(8, (duration!!.getDuration() as Int).toLong())
            }
        }

        listPreds = task4.predecessors
        relation = listPreds.get(0)
        assertEquals(3, relation.targetTask.uniqueID.intValue())
        assertEquals(RelationType.FINISH_FINISH, relation.type)

        var removed = task4.removePredecessor(relation.targetTask, relation.type, relation.lag)
        assertTrue(removed)
        listPreds = task4.predecessors
        assertTrue(listPreds.isEmpty())

        task4.addPredecessor(relation.targetTask, relation.type, relation.lag)

        task4.addPredecessor(task2, RelationType.FINISH_START, Duration.getInstance(0, TimeUnit.DAYS))

        listPreds = task4.predecessors
        removed = task4.removePredecessor(task2, RelationType.FINISH_FINISH, Duration.getInstance(0, TimeUnit.DAYS))
        assertFalse(removed)

        task4.addPredecessor(task2, RelationType.FINISH_START, Duration.getInstance(0, TimeUnit.DAYS))
        listPreds = task4.predecessors
        removed = task4.removePredecessor(task2, RelationType.FINISH_START, Duration.getInstance(0, TimeUnit.DAYS))
        assertTrue(removed)

        listPreds = task4.predecessors
        relation = listPreds.get(0)
        assertEquals(3, relation.targetTask.uniqueID.intValue())
        assertEquals(RelationType.FINISH_FINISH, relation.type)

        listPreds = task5.predecessors
        relation = listPreds.get(0)
        assertEquals(4, relation.targetTask.uniqueID.intValue())
        assertEquals(RelationType.START_FINISH, relation.type)
    }
}
