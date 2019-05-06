/*
 * file:       TaskDurationsTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       17/10/2014
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

import net.sf.mpxj.Duration
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpx.MPXReader
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.ProjectReaderUtility

/**
 * Tests to ensure task custom durations are correctly handled.
 */
class TaskDurationsTest {
    /**
     * Test to validate the custom durations in files saved by different versions of MS Project.
     */
    @Test
    @Throws(MPXJException::class)
    fun testTaskDurations() {
        for (file in MpxjTestData.listFiles("generated/task-durations", "task-durations")) {
            val reader = ProjectReaderUtility.getProjectReader(file.getName())
            if (reader is MPDDatabaseReader) {
                assumeJvm()
            }

            val project = reader.read(file)
            testDurationValues(file, reader, project)
            testDurationUnits(file, reader, project)
        }
    }

    /**
     * Test duration values.
     *
     * @param file project file
     * @param reader reader used to parse the file
     * @param project project file
     */
    private fun testDurationValues(file: File, reader: ProjectReader, project: ProjectFile) {
        val maxIndex = if (reader is MPXReader) 3 else 10
        for (index in 1..maxIndex) {
            val task = project.getTaskByID(Integer.valueOf(index))
            assertEquals("Duration$index", task.name)
            testTaskDurations(file, task, index, maxIndex)
        }
    }

    /**
     * Test the duration values for a task.
     *
     * @param file parent file
     * @param task task
     * @param testIndex index of number being tested
     * @param maxIndex maximum number of custom fields to expect in this file
     */
    private fun testTaskDurations(file: File, task: Task, testIndex: Int, maxIndex: Int) {
        for (index in 1..maxIndex) {
            val expectedValue = if (testIndex == index) "$index.0d" else "0.0d"
            val actualValue = if (task.getDuration(index) == null) "0.0d" else task.getDuration(index).toString()

            assertEquals(file.getName() + " " + task.name + " Duration" + index, expectedValue, actualValue)
        }
    }

    /**
     * Test duration units.
     *
     * @param file project file
     * @param reader reader used to parse the file
     * @param project project file
     */
    private fun testDurationUnits(file: File, reader: ProjectReader, project: ProjectFile) {
        val units = if (NumberHelper.getInt(project.projectProperties.mppFileType) == 8 || reader is MPXReader) UNITS_PROJECT98 else UNITS_PROJECT2000
        val maxIndex = if (reader is MPXReader) 3 else 10

        var taskID = 11
        for (fieldIndex in 1..maxIndex) {
            for (unitsIndex in units.indices) {
                val task = project.getTaskByID(Integer.valueOf(taskID))
                val expectedTaskName = "Duration$fieldIndex - Task $unitsIndex"
                assertEquals(expectedTaskName, task.name)
                val duration = task.getDuration(fieldIndex)
                assertEquals(file.getName() + " " + expectedTaskName, units[unitsIndex], duration.getUnits())
                ++taskID
            }
        }
    }

    companion object {

        private val UNITS_PROJECT98 = arrayOf<TimeUnit>(TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS, TimeUnit.WEEKS, TimeUnit.ELAPSED_MINUTES, TimeUnit.ELAPSED_HOURS, TimeUnit.ELAPSED_DAYS, TimeUnit.ELAPSED_WEEKS)
        private val UNITS_PROJECT2000 = arrayOf<TimeUnit>(TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS, TimeUnit.WEEKS, TimeUnit.MONTHS, TimeUnit.ELAPSED_MINUTES, TimeUnit.ELAPSED_HOURS, TimeUnit.ELAPSED_DAYS, TimeUnit.ELAPSED_WEEKS, TimeUnit.ELAPSED_MONTHS)
    }
}
