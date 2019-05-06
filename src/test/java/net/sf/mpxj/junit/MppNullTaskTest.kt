/*
 * file:       MppNullTaskTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2008
 * date:       05/11/2008
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
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpp.MPPReader
import net.sf.mpxj.mspdi.MSPDIReader

import org.junit.Test

/**
 * Tests to exercise MPP file read functionality for various versions of
 * MPP file.
 */
class MppNullTaskTest {

    /**
     * Test null task data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9NullTasks() {
        val project = MPPReader().read(MpxjTestData.filePath("mpp9nulltasks.mpp"))
        testNullTasks(project)
    }

    /**
     * Test null task data read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9NullTasksFrom12() {
        val project = MPPReader().read(MpxjTestData.filePath("mpp9nulltasks-from12.mpp"))
        testNullTasks(project)
    }

    /**
     * Test null task data read from an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9NullTasksFrom14() {
        val project = MPPReader().read(MpxjTestData.filePath("mpp9nulltasks-from14.mpp"))
        testNullTasks(project)
    }

    /**
     * Test null task data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12NullTasks() {
        val project = MPPReader().read(MpxjTestData.filePath("mpp12nulltasks.mpp"))
        testNullTasks(project)
    }

    /**
     * Test null task data read from an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12NullTasksFrom14() {
        val project = MPPReader().read(MpxjTestData.filePath("mpp12nulltasks-from14.mpp"))
        testNullTasks(project)
    }

    /**
     * Test null task data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14NullTasks() {
        val project = MPPReader().read(MpxjTestData.filePath("mpp14nulltasks.mpp"))
        testNullTasks(project)
    }

    /**
     * Test null task data read from an MPD9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpd9NullTasks() {
        assumeJvm()
        val project = MPDDatabaseReader().read(MpxjTestData.filePath("mpp9nulltasks.mpd"))
        testNullTasks(project)
    }

    /**
     * Test null task data read from an MSPDI file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMspdiNullTasks() {
        val project = MSPDIReader().read(MpxjTestData.filePath("mspdinulltasks.xml"))
        testNullTasks(project)
    }

    /**
     * Tests a project containing null tasks.
     *
     * @param project The ProjectFile instance being tested.
     * @throws Exception
     */
    private fun testNullTasks(project: ProjectFile) {
        var task: Task? = project.getTaskByID(Integer.valueOf(1))
        assertNotNull(task)
        assertEquals("Task 1", task!!.name)
        assertEquals(1, task!!.outlineLevel.intValue())
        assertEquals("1", task.outlineNumber)
        assertEquals("1", task.wbs)
        assertTrue(task.summary)

        task = project.getTaskByID(Integer.valueOf(2))
        if (task != null) {
            assertEquals(null, task.name)
            assertEquals(null, task.outlineLevel)
            assertEquals(null, task.outlineNumber)
            assertEquals(null, task.wbs)
            assertFalse(task.summary)
        }

        task = project.getTaskByID(Integer.valueOf(3))
        assertNotNull(task)
        assertEquals("Task 2", task!!.name)
        assertEquals(2, task!!.outlineLevel.intValue())
        assertEquals("1.1", task.outlineNumber)
        assertEquals("1.1", task.wbs)
        assertFalse(task.summary)

        task = project.getTaskByID(Integer.valueOf(4))
        assertNotNull(task)
        assertEquals("Task 3", task!!.name)
        assertEquals(2, task!!.outlineLevel.intValue())
        assertEquals("1.2", task.outlineNumber)
        assertEquals("1.2", task.wbs)
        assertFalse(task.summary)

        task = project.getTaskByID(Integer.valueOf(5))
        if (task != null) {
            assertEquals(null, task.name)
            assertEquals(null, task.outlineLevel)
            assertEquals(null, task.outlineNumber)
            assertEquals(null, task.wbs)
            assertFalse(task.summary)
        }

        task = project.getTaskByID(Integer.valueOf(6))
        assertNotNull(task)
        assertEquals("Task 4", task!!.name)
        assertEquals(1, task!!.outlineLevel.intValue())
        assertEquals("2", task.outlineNumber)
        assertEquals("2", task.wbs)
        assertTrue(task.summary)

        task = project.getTaskByID(Integer.valueOf(7))
        if (task != null) {
            assertEquals(null, task.name)
            assertEquals(null, task.outlineLevel)
            assertEquals(null, task.outlineNumber)
            assertEquals(null, task.wbs)
            assertFalse(task.summary)
        }

        task = project.getTaskByID(Integer.valueOf(8))
        assertNotNull(task)
        assertEquals("Task 5", task!!.name)
        assertEquals(2, task!!.outlineLevel.intValue())
        assertEquals("2.1", task.outlineNumber)
        assertEquals("2.1", task.wbs)
        assertFalse(task.summary)

    }
}
