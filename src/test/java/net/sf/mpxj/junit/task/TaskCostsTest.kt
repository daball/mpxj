/*
 * file:       TaskCostsTest.java
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
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpx.MPXReader
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.ProjectReaderUtility

/**
 * Tests to ensure task custom costs are correctly handled.
 */
class TaskCostsTest {
    /**
     * Test to validate the custom costs in files saved by different versions of MS Project.
     */
    @Test
    @Throws(MPXJException::class)
    fun testTaskCosts() {
        for (file in MpxjTestData.listFiles("generated/task-costs", "task-costs")) {
            testTaskCosts(file)
        }
    }

    /**
     * Test an individual project.
     *
     * @param file project file
     */
    @Throws(MPXJException::class)
    private fun testTaskCosts(file: File) {
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        if (reader is MPDDatabaseReader) {
            assumeJvm()
        }

        val maxIndex = if (reader is MPXReader) 3 else 10
        val project = reader.read(file)
        for (index in 1..maxIndex) {
            val task = project.getTaskByID(Integer.valueOf(index))
            assertEquals("Cost$index", task.name)
            testTaskCosts(file, task, index, maxIndex)
        }
    }

    /**
     * Test the cost values for a task.
     *
     * @param file parent file
     * @param task task
     * @param testIndex index of number being tested
     * @param maxIndex maximum number of custom fields to expect in this file
     */
    private fun testTaskCosts(file: File, task: Task, testIndex: Int, maxIndex: Int) {
        for (index in 1..maxIndex) {
            val expectedValue = if (testIndex == index) index else 0
            val actualValue = NumberHelper.getInt(task.getCost(index))

            assertEquals(file.getName() + " Cost" + index, expectedValue.toLong(), actualValue.toLong())
        }
    }
}
