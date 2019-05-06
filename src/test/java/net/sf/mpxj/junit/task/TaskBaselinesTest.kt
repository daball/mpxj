/*
 * file:       TaskBaselineValuesTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2015
 * date:       07/02/2014
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
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

import org.junit.Test

import net.sf.mpxj.AccrueType
import net.sf.mpxj.Duration
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Task
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpp.ApplicationVersion
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.ProjectReaderUtility

/**
 * Tests to ensure task task baseline values are correctly handled.
 */
class TaskBaselinesTest {

    private val m_dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")

    /**
     * Test to verify SourceForeg issue is fixed.
     *
     * @throws MPXJException
     */
    @Test
    @Throws(MPXJException::class)
    fun testSourceForgeIssue259() {
        val file = File(MpxjTestData.filePath("generated/task-baselines"), "sf259.mpp")
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        val project = reader.read(file)
        for (index in SF259_BASELINE_STARTS.indices) {
            val task = project.getTaskByID(Integer.valueOf(index + 1))
            assertEquals(SF259_BASELINE_STARTS[index], m_dateFormat.format(task.baselineStart))
            assertEquals(SF259_BASELINE_FINISHES[index], m_dateFormat.format(task.baselineFinish))
        }
    }

    /**
     * Test to validate the baseline values saved by different versions of MS Project.
     */
    @Test
    @Throws(MPXJException::class)
    fun testTaskBaselineValues() {
        for (file in MpxjTestData.listFiles("generated/task-baselines", "task-baselines")) {
            testTaskBaselineValues(file)
        }
    }

    /**
     * Test an individual project.
     *
     * @param file project file
     */
    @Throws(MPXJException::class)
    private fun testTaskBaselineValues(file: File) {
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        if (reader is MPDDatabaseReader) {
            assumeJvm()
        }

        val project = reader.read(file)

        var startTaskID = 1
        val maxBaselines = if (file.getName().contains("project98") || file.getName().contains("project2000")) 1 else 11
        startTaskID = testCosts(project, startTaskID, maxBaselines)
        startTaskID = testDurations(project, startTaskID, maxBaselines)
        startTaskID = testFinishes(project, startTaskID, maxBaselines)
        startTaskID = testStarts(project, startTaskID, maxBaselines)
        startTaskID = testWorks(project, startTaskID, maxBaselines)

        //
        // Handle different file content depending on which application and file version have been used
        //
        val properties = project.projectProperties
        if (NumberHelper.getInt(properties.applicationVersion) >= ApplicationVersion.PROJECT_2010 && NumberHelper.getInt(properties.mppFileType) >= 14) {
            startTaskID = testEstimatedDurations(project, startTaskID, maxBaselines)
            startTaskID = testEstimatedFinishes(project, startTaskID, maxBaselines)
            startTaskID = testEstimatedStarts(project, startTaskID, maxBaselines)
            startTaskID = testFixedCosts(project, startTaskID, maxBaselines)
            testFixedCostAccruals(project, startTaskID, maxBaselines)
        }
    }

    /**
     * Test baseline costs.
     *
     * @param project project
     * @param startTaskID initial task ID
     * @param maxBaselines maximum baselines to test
     * @return task ID for next tests
     */
    private fun testCosts(project: ProjectFile, startTaskID: Int, maxBaselines: Int): Int {
        var taskID = startTaskID

        for (index in 0 until maxBaselines) {
            val task = project.getTaskByID(Integer.valueOf(taskID))
            taskID++
            val value: Number?

            if (index == 0) {
                value = task.baselineCost
            } else {
                value = task.getBaselineCost(index)
            }

            assertEquals(COSTS[index], value!!.toString())
        }

        return taskID
    }

    /**
     * Test baseline durations.
     *
     * @param project project
     * @param startTaskID initial task ID
     * @param maxBaselines maximum baselines to test
     * @return task ID for next tests
     */
    private fun testDurations(project: ProjectFile, startTaskID: Int, maxBaselines: Int): Int {
        var taskID = startTaskID

        for (index in 0 until maxBaselines) {
            val task = project.getTaskByID(Integer.valueOf(taskID))
            taskID++
            val value: Duration?

            if (index == 0) {
                value = task.baselineDuration
            } else {
                value = task.getBaselineDuration(index)
            }

            assertEquals("Baseline$index", DURATIONS[index], value!!.toString())
        }

        return taskID
    }

    /**
     * Test baseline estimated durations.
     *
     * @param project project
     * @param startTaskID initial task ID
     * @param maxBaselines maximum baselines to test
     * @return task ID for next tests
     */
    private fun testEstimatedDurations(project: ProjectFile, startTaskID: Int, maxBaselines: Int): Int {
        var taskID = startTaskID

        for (index in 0 until maxBaselines) {
            val task = project.getTaskByID(Integer.valueOf(taskID))
            taskID++
            val value: Duration

            if (index == 0) {
                value = task.baselineEstimatedDuration
            } else {
                value = task.getBaselineEstimatedDuration(index)
            }

            assertEquals(ESTIMATED_DURATIONS[index], value.toString())
        }

        return taskID
    }

    /**
     * Test baseline estimated finishes.
     *
     * @param project project
     * @param startTaskID initial task ID
     * @param maxBaselines maximum baselines to test
     * @return task ID for next tests
     */
    private fun testEstimatedFinishes(project: ProjectFile, startTaskID: Int, maxBaselines: Int): Int {
        var taskID = startTaskID

        for (index in 0 until maxBaselines) {
            val task = project.getTaskByID(Integer.valueOf(taskID))
            taskID++
            val value: Date

            if (index == 0) {
                value = task.baselineEstimatedFinish
            } else {
                value = task.getBaselineEstimatedFinish(index)
            }

            assertEquals(ESTIMATED_FINISHES[index], m_dateFormat.format(value))
        }

        return taskID
    }

    /**
     * Test baseline estimated starts.
     *
     * @param project project
     * @param startTaskID initial task ID
     * @param maxBaselines maximum baselines to test
     * @return task ID for next tests
     */
    private fun testEstimatedStarts(project: ProjectFile, startTaskID: Int, maxBaselines: Int): Int {
        var taskID = startTaskID

        for (index in 0 until maxBaselines) {
            val task = project.getTaskByID(Integer.valueOf(taskID))
            taskID++
            val value: Date

            if (index == 0) {
                value = task.baselineEstimatedStart
            } else {
                value = task.getBaselineEstimatedStart(index)
            }

            assertEquals(ESTIMATED_STARTS[index], m_dateFormat.format(value))
        }

        return taskID
    }

    /**
     * Test baseline finishes.
     *
     * @param project project
     * @param startTaskID initial task ID
     * @param maxBaselines maximum baselines to test
     * @return task ID for next tests
     */
    private fun testFinishes(project: ProjectFile, startTaskID: Int, maxBaselines: Int): Int {
        var taskID = startTaskID

        for (index in 0 until maxBaselines) {
            val task = project.getTaskByID(Integer.valueOf(taskID))
            taskID++
            val value: Date

            if (index == 0) {
                value = task.baselineFinish
            } else {
                value = task.getBaselineFinish(index)
            }

            assertEquals(FINISHES[index], m_dateFormat.format(value))
        }

        return taskID
    }

    /**
     * Test baseline fixed costs.
     *
     * @param project project
     * @param startTaskID initial task ID
     * @param maxBaselines maximum baselines to test
     * @return task ID for next tests
     */
    private fun testFixedCosts(project: ProjectFile, startTaskID: Int, maxBaselines: Int): Int {
        var taskID = startTaskID

        for (index in 0 until maxBaselines) {
            val task = project.getTaskByID(Integer.valueOf(taskID))
            taskID++
            val value: Number

            if (index == 0) {
                value = task.baselineFixedCost
            } else {
                value = task.getBaselineFixedCost(index)
            }

            assertEquals(FIXED_COSTS[index], value.toString())
        }

        return taskID
    }

    /**
     * Test baseline fixed cost accruals.
     *
     * @param project project
     * @param startTaskID initial task ID
     * @param maxBaselines maximum baselines to test
     * @return task ID for next tests
     */
    private fun testFixedCostAccruals(project: ProjectFile, startTaskID: Int, maxBaselines: Int): Int {
        var taskID = startTaskID

        for (index in 0 until maxBaselines) {
            val task = project.getTaskByID(Integer.valueOf(taskID))
            taskID++
            val value: AccrueType

            if (index == 0) {
                value = task.baselineFixedCostAccrual
            } else {
                value = task.getBaselineFixedCostAccrual(index)
            }

            assertEquals(FIXED_COST_ACCRUALS[index], value.toString())
        }

        return taskID
    }

    /**
     * Test baseline starts.
     *
     * @param project project
     * @param startTaskID initial task ID
     * @param maxBaselines maximum baselines to test
     * @return task ID for next tests
     */
    private fun testStarts(project: ProjectFile, startTaskID: Int, maxBaselines: Int): Int {
        var taskID = startTaskID

        for (index in 0 until maxBaselines) {
            val task = project.getTaskByID(Integer.valueOf(taskID))
            taskID++
            val value: Date

            if (index == 0) {
                value = task.baselineStart
            } else {
                value = task.getBaselineStart(index)
            }

            assertEquals(STARTS[index], m_dateFormat.format(value))
        }

        return taskID
    }

    /**
     * Test baseline works.
     *
     * @param project project
     * @param startTaskID initial task ID
     * @param maxBaselines maximum baselines to test
     * @return task ID for next tests
     */
    private fun testWorks(project: ProjectFile, startTaskID: Int, maxBaselines: Int): Int {
        var taskID = startTaskID

        for (index in 0 until maxBaselines) {
            val task = project.getTaskByID(Integer.valueOf(taskID))
            taskID++
            val value: Duration?

            if (index == 0) {
                value = task.baselineWork
            } else {
                value = task.getBaselineWork(index)
            }

            assertEquals(WORKS[index], value!!.toString())
        }

        return taskID
    }

    companion object {

        private val COSTS = arrayOf("1.0", "2.0", "3.0", "4.0", "5.0", "6.0", "7.0", "8.0", "9.0", "10.0", "11.0")

        private val DURATIONS = arrayOf("11.0d", "12.0d", "13.0d", "14.0d", "15.0d", "16.0d", "17.0d", "18.0d", "19.0d", "20.0d", "21.0d")

        private val ESTIMATED_DURATIONS = arrayOf("31.0d", "32.0d", "33.0d", "34.0d", "35.0d", "36.0d", "37.0d", "38.0d", "39.0d", "40.0d", "41.0d")

        private val ESTIMATED_FINISHES = arrayOf("01/01/2014 09:00", "02/01/2014 10:00", "03/01/2014 11:00", "04/01/2014 12:00", "05/01/2014 13:00", "06/01/2014 14:00", "07/01/2014 15:00", "08/01/2014 16:00", "09/01/2014 17:00", "10/01/2014 18:00", "10/01/2014 19:00")

        private val ESTIMATED_STARTS = arrayOf("01/02/2014 09:00", "02/02/2014 10:00", "03/02/2014 11:00", "04/02/2014 12:00", "05/02/2014 13:00", "06/02/2014 14:00", "07/02/2014 15:00", "08/02/2014 16:00", "09/02/2014 17:00", "10/02/2014 18:00", "10/02/2014 19:00")

        private val FINISHES = arrayOf("01/03/2014 09:00", "02/03/2014 10:00", "03/03/2014 11:00", "04/03/2014 12:00", "05/03/2014 13:00", "06/03/2014 14:00", "07/03/2014 15:00", "08/03/2014 16:00", "09/03/2014 17:00", "10/03/2014 18:00", "10/03/2014 19:00")

        private val FIXED_COSTS = arrayOf("11.0", "12.0", "13.0", "14.0", "15.0", "16.0", "17.0", "18.0", "19.0", "20.0", "21.0")

        private val FIXED_COST_ACCRUALS = arrayOf("START", "PRORATED", "END", "START", "PRORATED", "END", "START", "PRORATED", "END", "START", "PRORATED")

        private val STARTS = arrayOf("01/04/2014 09:00", "02/04/2014 10:00", "03/04/2014 11:00", "04/04/2014 12:00", "05/04/2014 13:00", "06/04/2014 14:00", "07/04/2014 15:00", "08/04/2014 16:00", "09/04/2014 17:00", "10/04/2014 18:00", "10/04/2014 19:00")

        private val WORKS = arrayOf("51.0h", "52.0h", "53.0h", "54.0h", "55.0h", "56.0h", "57.0h", "58.0h", "59.0h", "60.0h", "61.0h")

        private val SF259_BASELINE_STARTS = arrayOf("01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 01:00", "01/03/2015 01:00", "01/03/2015 01:00", "01/03/2015 01:00", "01/03/2015 01:00", "01/03/2015 01:00", "01/03/2015 03:00")

        private val SF259_BASELINE_FINISHES = arrayOf("01/03/2015 04:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 00:00", "01/03/2015 04:00", "01/03/2015 04:00", "01/03/2015 00:00", "01/03/2015 01:00", "01/03/2015 03:00", "01/03/2015 03:00", "01/03/2015 02:30", "01/03/2015 02:30", "01/03/2015 03:00", "01/03/2015 03:00", "01/03/2015 04:00")
    }

}
