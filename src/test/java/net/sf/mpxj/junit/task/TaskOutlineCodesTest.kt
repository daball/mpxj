/*
 * file:       TaskOutlineCodeTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       10/11/2014
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

package net.sf.mpxj.junit.task

import net.sf.mpxj.junit.MpxjAssert.*
import org.junit.Assert.*

import java.io.File

import org.junit.Test

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.ProjectReaderUtility

/**
 * Tests to ensure task custom outline codes are correctly handled.
 */
class TaskOutlineCodesTest {
    /**
     * Test to validate the custom outline codes in files saved by different versions of MS Project.
     */
    @Test
    @Throws(MPXJException::class)
    fun testTaskOutlineCodes() {
        for (file in MpxjTestData.listFiles("generated/task-outlinecodes", "task-outlinecodes")) {
            testTaskOutlineCodes(file)
        }
    }

    /**
     * Test an individual project.
     *
     * @param file project file
     */
    @Throws(MPXJException::class)
    private fun testTaskOutlineCodes(file: File) {
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        if (reader is MPDDatabaseReader) {
            assumeJvm()
        }

        val maxIndex = 10
        val project = reader.read(file)
        for (index in 1..maxIndex) {
            val task = project.getTaskByID(Integer.valueOf(index))
            assertEquals("Outline Code$index", task.name)
            testFlatTaskOutlineCodes(file, task, index, maxIndex)
        }

        val taskOffset = 10
        for (index in 1..maxIndex) {
            val task = project.getTaskByID(Integer.valueOf(index + taskOffset))
            assertEquals("Outline Code$index", task.name)
            testHierarchicalTaskOutlineCodes(file, task, index, maxIndex)
        }
    }

    /**
     * Test flat outline code values for a task.
     *
     * @param file parent file
     * @param task task
     * @param testIndex index of number being tested
     * @param maxIndex maximum number of custom fields to expect in this file
     */
    private fun testFlatTaskOutlineCodes(file: File, task: Task, testIndex: Int, maxIndex: Int) {
        for (index in 1..maxIndex) {
            val expectedValue = if (testIndex == index) "OC" + Integer.toString(index) + "A" else null
            val actualValue = task.getOutlineCode(index)

            assertEquals(file.getName() + " Outline Code" + index, expectedValue, actualValue)
        }
    }

    /**
     * Test hierarchical outline code values for a task.
     *
     * @param file parent file
     * @param task task
     * @param testIndex index of number being tested
     * @param maxIndex maximum number of custom fields to expect in this file
     */
    private fun testHierarchicalTaskOutlineCodes(file: File, task: Task, testIndex: Int, maxIndex: Int) {
        for (index in 1..maxIndex) {
            val expectedValue = if (testIndex == index) "OC" + Integer.toString(index) + "A.OC" + Integer.toString(index) + "B" else null
            val actualValue = task.getOutlineCode(index)

            assertEquals(file.getName() + " Outline Code" + index, expectedValue, actualValue)
        }
    }

}
