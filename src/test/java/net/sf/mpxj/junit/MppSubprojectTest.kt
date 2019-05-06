/*
 * file:       MppSubprojectTest.java
 * author:     Wade Golden
 * copyright:  (c) Packwood Software 2006
 * date:       19-September-2006
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
import net.sf.mpxj.SubProject
import net.sf.mpxj.Task
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * Tests to exercise MPP file read functionality for various versions of
 * MPP file.
 */
class MppSubprojectTest {
    /**
     * Test subproject data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9Subproject() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9subproject.mpp"))
        testSubprojects(mpp, true)
    }

    /**
     * Test subproject data read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9SubprojectFrom12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9subproject-from12.mpp"))
        testSubprojects(mpp, true)
    }

    /**
     * Test subproject data read from an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9SubprojectFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9subproject-from14.mpp"))
        testSubprojects(mpp, true)
    }

    /**
     * Test subproject data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12Subproject() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12subproject.mpp"))
        testSubprojects(mpp, true)
    }

    /**
     * Test subproject data read from an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12SubprojectFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12subproject-from14.mpp"))
        testSubprojects(mpp, true)
    }

    /**
     * Test subproject data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14Subproject() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14subproject.mpp"))
        testSubprojects(mpp, true)
    }

    /**
     * Test subproject data read from an MPD9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpd9Subproject() {
        assumeJvm()
        val mpp = MPDDatabaseReader().read(MpxjTestData.filePath("mpp9subproject.mpd"))
        testSubprojects(mpp, false)
    }

    /**
     * Tests the various fields needed to read in subprojects.
     *
     * @param mpp The ProjectFile being tested.
     * @param isMPP is the source an MPP file
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun testSubprojects(mpp: ProjectFile, isMPP: Boolean) {
        val taskNormal = mpp.getTaskByUniqueID(Integer.valueOf(1))
        val taskSubprojectA = mpp.getTaskByUniqueID(Integer.valueOf(2))
        val taskSubprojectB = mpp.getTaskByUniqueID(Integer.valueOf(3))

        assertEquals("Normal Task", taskNormal.name)
        assertEquals("SubprojectA-9", taskSubprojectA.name)
        assertEquals("SubprojectB-9", taskSubprojectB.name)

        // Subproject A
        val subprojectA = taskSubprojectA.subProject
        assertNotNull(subprojectA)
        val expectedFilenameA = "\\SubprojectA-9.mpp"
        //assertEquals(expectedFilenameA, subprojectA.getDosFileName());
        assertTrue(expectedFilenameA.indexOf(subprojectA!!.fileName) !== -1)
        //subprojectA.getDosFullPath(); don't need to test
        assertTrue(subprojectA!!.fullPath!!.indexOf(expectedFilenameA) !== -1)
        assertEquals(Integer.valueOf(2), subprojectA!!.taskUniqueID)

        //assertEquals(null, taskSubprojectA.getSubprojectName());  // TODO: why is this null?
        assertFalse(taskSubprojectA.subprojectReadOnly)

        if (isMPP) {
            assertEquals(Integer.valueOf(8388608), subprojectA!!.uniqueIDOffset) // MPD needs to be fixed
            assertEquals(Integer.valueOf(8388608), taskSubprojectA.subprojectTasksUniqueIDOffset)
            assertEquals(Integer.valueOf(0), taskSubprojectA.subprojectTaskUniqueID)
        }
    }
}
