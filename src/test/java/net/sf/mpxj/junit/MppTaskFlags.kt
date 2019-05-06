package net.sf.mpxj.junit

import net.sf.mpxj.junit.MpxjAssert.*
import org.junit.Assert.*
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.TaskMode
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * Tests reading task field bit flags from MPP files.
 */
class MppTaskFlags {
    /**
     * Test MPP9 saved by Project 2013.
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9FromProject2013() {
        val mpp = MPPReader().read(MpxjTestData.filePath("taskFlags-mpp9Project2013.mpp"))
        testFlags(mpp)
    }

    /**
     * Test MPP12 saved by Project 2013.
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12FromProject2013() {
        val mpp = MPPReader().read(MpxjTestData.filePath("taskFlags-mpp12Project2013.mpp"))
        testFlags(mpp)
    }

    /**
     * Test MPP14 saved by Project 2013.
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14FromProject2013() {
        val mpp = MPPReader().read(MpxjTestData.filePath("taskFlags-mpp14Project2013.mpp"))
        testFlags(mpp)
    }

    /**
     * Test MPP9 saved by Project 2010.
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9FromProject2010() {
        val mpp = MPPReader().read(MpxjTestData.filePath("taskFlags-mpp9Project2010.mpp"))
        testFlags(mpp)
    }

    /**
     * Test MPP12 saved by Project 2010.
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12FromProject2010() {
        val mpp = MPPReader().read(MpxjTestData.filePath("taskFlags-mpp12Project2010.mpp"))
        testFlags(mpp)
    }

    /**
     * Test MPP14 saved by Project 2010.
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14FromProject2010() {
        val mpp = MPPReader().read(MpxjTestData.filePath("taskFlags-mpp14Project2010.mpp"))
        testFlags(mpp)
    }

    /**
     * Common code to test flag values.
     *
     * @param mpp project file to test
     */
    private fun testFlags(mpp: ProjectFile) {
        var task: Task

        //
        // Ignore resource calendars
        //
        task = mpp.getTaskByUniqueID(Integer.valueOf(1))
        assertEquals("Ignore Resource Calendars: No", task.name)
        assertFalse(task.ignoreResourceCalendar)

        task = mpp.getTaskByUniqueID(Integer.valueOf(2))
        assertEquals("Ignore Resource Calendars: Yes", task.name)
        assertTrue(task.ignoreResourceCalendar)

        task = mpp.getTaskByUniqueID(Integer.valueOf(3))
        assertEquals("Ignore Resource Calendars: No", task.name)
        assertFalse(task.ignoreResourceCalendar)

        task = mpp.getTaskByUniqueID(Integer.valueOf(4))
        assertEquals("Ignore Resource Calendars: Yes", task.name)
        assertTrue(task.ignoreResourceCalendar)

        //
        // Effort driven
        //
        task = mpp.getTaskByUniqueID(Integer.valueOf(7))
        assertEquals("Effort Driven: Yes", task.name)
        assertTrue(task.effortDriven)

        task = mpp.getTaskByUniqueID(Integer.valueOf(8))
        assertEquals("Effort Driven: No", task.name)
        assertFalse(task.effortDriven)

        task = mpp.getTaskByUniqueID(Integer.valueOf(9))
        assertEquals("Effort Driven: Yes", task.name)
        assertTrue(task.effortDriven)

        task = mpp.getTaskByUniqueID(Integer.valueOf(10))
        assertEquals("Effort Driven: No", task.name)
        assertFalse(task.effortDriven)

        //
        // Hide bar
        //
        task = mpp.getTaskByUniqueID(Integer.valueOf(12))
        assertEquals("Hide Bar: Yes", task.name)
        assertTrue(task.hideBar)

        task = mpp.getTaskByUniqueID(Integer.valueOf(13))
        assertEquals("Hide Bar: No", task.name)
        assertFalse(task.hideBar)

        task = mpp.getTaskByUniqueID(Integer.valueOf(14))
        assertEquals("Hide Bar: Yes", task.name)
        assertTrue(task.hideBar)

        task = mpp.getTaskByUniqueID(Integer.valueOf(15))
        assertEquals("Hide Bar: No", task.name)
        assertFalse(task.hideBar)

        //
        // Level assignments
        //
        task = mpp.getTaskByUniqueID(Integer.valueOf(17))
        assertEquals("Level Assignments: Yes", task.name)
        assertTrue(task.levelAssignments)

        task = mpp.getTaskByUniqueID(Integer.valueOf(18))
        assertEquals("Level Assignments: No", task.name)
        assertFalse(task.levelAssignments)

        task = mpp.getTaskByUniqueID(Integer.valueOf(19))
        assertEquals("Level Assignments: Yes", task.name)
        assertTrue(task.levelAssignments)

        task = mpp.getTaskByUniqueID(Integer.valueOf(20))
        assertEquals("Level Assignments: No", task.name)
        assertFalse(task.levelAssignments)

        //
        // Levelling can split
        //
        task = mpp.getTaskByUniqueID(Integer.valueOf(22))
        assertEquals("Leveling Can Split: Yes", task.name)
        assertTrue(task.levelingCanSplit)

        task = mpp.getTaskByUniqueID(Integer.valueOf(23))
        assertEquals("Leveling Can Split: No", task.name)
        assertFalse(task.levelingCanSplit)

        task = mpp.getTaskByUniqueID(Integer.valueOf(24))
        assertEquals("Leveling Can Split: Yes", task.name)
        assertTrue(task.levelingCanSplit)

        task = mpp.getTaskByUniqueID(Integer.valueOf(25))
        assertEquals("Leveling Can Split: Yno", task.name)
        assertFalse(task.levelingCanSplit)

        //
        // Marked
        //
        task = mpp.getTaskByUniqueID(Integer.valueOf(27))
        assertEquals("Marked: Yes", task.name)
        assertTrue(task.marked)

        task = mpp.getTaskByUniqueID(Integer.valueOf(28))
        assertEquals("Marked: No", task.name)
        assertFalse(task.marked)

        task = mpp.getTaskByUniqueID(Integer.valueOf(29))
        assertEquals("Marked: Yes", task.name)
        assertTrue(task.marked)

        task = mpp.getTaskByUniqueID(Integer.valueOf(30))
        assertEquals("Marked: No", task.name)
        assertFalse(task.marked)

        //
        // Milestone
        //
        task = mpp.getTaskByUniqueID(Integer.valueOf(32))
        assertEquals("Milestone: Yes", task.name)
        assertTrue(task.milestone)

        task = mpp.getTaskByUniqueID(Integer.valueOf(33))
        assertEquals("Milestone: No", task.name)
        assertFalse(task.milestone)

        task = mpp.getTaskByUniqueID(Integer.valueOf(34))
        assertEquals("Milestone: Yes", task.name)
        assertTrue(task.milestone)

        task = mpp.getTaskByUniqueID(Integer.valueOf(35))
        assertEquals("Milestone: No", task.name)
        assertFalse(task.milestone)

        //
        // Rollup
        //
        task = mpp.getTaskByUniqueID(Integer.valueOf(37))
        assertEquals("Rollup: Yes", task.name)
        assertTrue(task.rollup)

        task = mpp.getTaskByUniqueID(Integer.valueOf(38))
        assertEquals("Rollup: No", task.name)
        assertFalse(task.rollup)

        task = mpp.getTaskByUniqueID(Integer.valueOf(39))
        assertEquals("Rollup: Yes", task.name)
        assertTrue(task.rollup)

        task = mpp.getTaskByUniqueID(Integer.valueOf(40))
        assertEquals("Rollup: No", task.name)
        assertFalse(task.rollup)

        //
        // Flags
        //
        task = mpp.getTaskByUniqueID(Integer.valueOf(42))
        assertEquals("Flag1", task.name)
        testFlag(task, 1)

        task = mpp.getTaskByUniqueID(Integer.valueOf(43))
        assertEquals("Flag2", task.name)
        testFlag(task, 2)

        task = mpp.getTaskByUniqueID(Integer.valueOf(44))
        assertEquals("Flag3", task.name)
        testFlag(task, 3)

        task = mpp.getTaskByUniqueID(Integer.valueOf(45))
        assertEquals("Flag4", task.name)
        testFlag(task, 4)

        task = mpp.getTaskByUniqueID(Integer.valueOf(46))
        assertEquals("Flag5", task.name)
        testFlag(task, 5)

        task = mpp.getTaskByUniqueID(Integer.valueOf(47))
        assertEquals("Flag6", task.name)
        testFlag(task, 6)

        task = mpp.getTaskByUniqueID(Integer.valueOf(48))
        assertEquals("Flag7", task.name)
        testFlag(task, 7)

        task = mpp.getTaskByUniqueID(Integer.valueOf(49))
        assertEquals("Flag8", task.name)
        testFlag(task, 8)

        task = mpp.getTaskByUniqueID(Integer.valueOf(50))
        assertEquals("Flag9", task.name)
        testFlag(task, 9)

        task = mpp.getTaskByUniqueID(Integer.valueOf(51))
        assertEquals("Flag10", task.name)
        testFlag(task, 10)

        task = mpp.getTaskByUniqueID(Integer.valueOf(52))
        assertEquals("Flag11", task.name)
        testFlag(task, 11)

        task = mpp.getTaskByUniqueID(Integer.valueOf(53))
        assertEquals("Flag12", task.name)
        testFlag(task, 12)

        task = mpp.getTaskByUniqueID(Integer.valueOf(54))
        assertEquals("Flag13", task.name)
        testFlag(task, 13)

        task = mpp.getTaskByUniqueID(Integer.valueOf(55))
        assertEquals("Flag14", task.name)
        testFlag(task, 14)

        task = mpp.getTaskByUniqueID(Integer.valueOf(56))
        assertEquals("Flag15", task.name)
        testFlag(task, 15)

        task = mpp.getTaskByUniqueID(Integer.valueOf(57))
        assertEquals("Flag16", task.name)
        testFlag(task, 16)

        task = mpp.getTaskByUniqueID(Integer.valueOf(58))
        assertEquals("Flag17", task.name)
        testFlag(task, 17)

        task = mpp.getTaskByUniqueID(Integer.valueOf(59))
        assertEquals("Flag18", task.name)
        testFlag(task, 18)

        task = mpp.getTaskByUniqueID(Integer.valueOf(60))
        assertEquals("Flag19", task.name)
        testFlag(task, 19)

        task = mpp.getTaskByUniqueID(Integer.valueOf(61))
        assertEquals("Flag20", task.name)
        testFlag(task, 20)

        if (NumberHelper.getInt(mpp.projectProperties.mppFileType) == 14) {
            //
            // Active
            //
            task = mpp.getTaskByUniqueID(Integer.valueOf(63))
            assertEquals("Active: On", task.name)
            assertTrue(task.active)

            task = mpp.getTaskByUniqueID(Integer.valueOf(64))
            assertEquals("Active: Off", task.name)
            assertFalse(task.active)

            task = mpp.getTaskByUniqueID(Integer.valueOf(65))
            assertEquals("Active: On", task.name)
            assertTrue(task.active)

            task = mpp.getTaskByUniqueID(Integer.valueOf(66))
            assertEquals("Active: Off", task.name)
            assertFalse(task.active)

            //
            // Task Mode
            //
            task = mpp.getTaskByUniqueID(Integer.valueOf(68))
            assertEquals("Mode: Auto", task.name)
            assertEquals(TaskMode.AUTO_SCHEDULED, task.taskMode)

            task = mpp.getTaskByUniqueID(Integer.valueOf(69))
            assertEquals("Mode: Manual", task.name)
            assertEquals(TaskMode.MANUALLY_SCHEDULED, task.taskMode)

            task = mpp.getTaskByUniqueID(Integer.valueOf(70))
            assertEquals("Mode: Auto", task.name)
            assertEquals(TaskMode.AUTO_SCHEDULED, task.taskMode)

            task = mpp.getTaskByUniqueID(Integer.valueOf(71))
            assertEquals("Mode: Manual", task.name)
            assertEquals(TaskMode.MANUALLY_SCHEDULED, task.taskMode)
        }
    }

    /**
     * Test all 20 custom field flags.
     *
     * @param task task to be tested
     * @param flag flag index to test
     */
    private fun testFlag(task: Task, flag: Int) {
        for (loop in 0..19) {
            assertBooleanEquals("Flag" + (loop + 1), flag == loop + 1, task.getFlag(loop + 1))
        }
    }
}