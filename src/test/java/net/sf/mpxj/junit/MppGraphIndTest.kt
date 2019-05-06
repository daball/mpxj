/*
 * file:       MppGraphIndTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       24-Feb-2006
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

import net.sf.mpxj.FieldContainer
import net.sf.mpxj.FieldType
import net.sf.mpxj.GraphicalIndicator
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * The tests contained in this class exercise the graphical indicator
 * evaluation code.
 */
class MppGraphIndTest {
    /**
     * Test the graphical indicator evaluation code for an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9GraphInd() {
        val project = MPPReader().read(MpxjTestData.filePath("mpp9graphind.mpp"))
        testGraphicalIndicators(project)
    }

    /**
     * Test the graphical indicator evaluation code for an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9GraphIndFrom12() {
        val project = MPPReader().read(MpxjTestData.filePath("mpp9graphind-from12.mpp"))
        testGraphicalIndicators(project)
    }

    /**
     * Test the graphical indicator evaluation code for an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9GraphIndFrom14() {
        val project = MPPReader().read(MpxjTestData.filePath("mpp9graphind-from14.mpp"))
        testGraphicalIndicators(project)
    }

    /**
     * Test the graphical indicator evaluation code for an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12GraphInd() {
        val project = MPPReader().read(MpxjTestData.filePath("mpp12graphind.mpp"))
        testGraphicalIndicators(project)
    }

    /**
     * Test the graphical indicator evaluation code for an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12GraphIndFrom14() {
        val project = MPPReader().read(MpxjTestData.filePath("mpp12graphind-from14.mpp"))
        testGraphicalIndicators(project)
    }

    /**
     * Test the graphical indicator evaluation code for an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14GraphInd() {
        val project = MPPReader().read(MpxjTestData.filePath("mpp14graphind.mpp"))
        testGraphicalIndicators(project)
    }

    /**
     * Common graphical indicator tests.
     *
     * @param project project to test
     */
    private fun testGraphicalIndicators(project: ProjectFile) {
        val taskList = project.tasks
        val tasks = taskList.toArray(arrayOfNulls<Task>(taskList.size()))

        testIndicator(project, TaskField.COST1, tasks, COST1_RESULTS)
        testIndicator(project, TaskField.COST2, tasks, COST2_RESULTS)
        testIndicator(project, TaskField.COST3, tasks, COST3_RESULTS)
        testIndicator(project, TaskField.COST4, tasks, COST4_RESULTS)

        testIndicator(project, TaskField.DATE1, tasks, DATE1_RESULTS)
        testIndicator(project, TaskField.DATE2, tasks, DATE2_RESULTS)
        testIndicator(project, TaskField.DATE3, tasks, DATE3_RESULTS)
        testIndicator(project, TaskField.DATE4, tasks, DATE4_RESULTS)
        testIndicator(project, TaskField.DATE5, tasks, DATE5_RESULTS)

        testIndicator(project, TaskField.DURATION1, tasks, DURATION1_RESULTS)
        testIndicator(project, TaskField.DURATION2, tasks, DURATION2_RESULTS)
        testIndicator(project, TaskField.DURATION3, tasks, DURATION3_RESULTS)
        testIndicator(project, TaskField.DURATION4, tasks, DURATION4_RESULTS)

        testIndicator(project, TaskField.FLAG1, tasks, FLAG_RESULTS)
        testIndicator(project, TaskField.FLAG2, tasks, FLAG_RESULTS)
        testIndicator(project, TaskField.FLAG3, tasks, FLAG_RESULTS)

        testIndicator(project, TaskField.NUMBER1, tasks, NUMBER1_RESULTS)
        testIndicator(project, TaskField.NUMBER2, tasks, NUMBER2_RESULTS)
        testIndicator(project, TaskField.NUMBER3, tasks, NUMBER3_RESULTS)
        testIndicator(project, TaskField.NUMBER4, tasks, NUMBER4_RESULTS)

        testIndicator(project, TaskField.TEXT1, tasks, TEXT1_RESULTS)
        testIndicator(project, TaskField.TEXT2, tasks, TEXT2_RESULTS)
        testIndicator(project, TaskField.TEXT3, tasks, TEXT3_RESULTS)
        testIndicator(project, TaskField.TEXT4, tasks, TEXT4_RESULTS)
        testIndicator(project, TaskField.TEXT5, tasks, TEXT5_RESULTS)
        testIndicator(project, TaskField.TEXT6, tasks, TEXT6_RESULTS)
        testIndicator(project, TaskField.TEXT7, tasks, TEXT7_RESULTS)
    }

    /**
     * For a particular field type, ensure that the correct set of graphical
     * indicators are being generated.
     *
     * @param project parent project
     * @param fieldType target field type
     * @param rows array of rows containing field data
     * @param expectedResults array of expected results
     */
    private fun testIndicator(project: ProjectFile, fieldType: FieldType, rows: Array<FieldContainer>, expectedResults: IntArray) {
        val indicator = project.customFields.getCustomField(fieldType).getGraphicalIndicator()
        for (loop in expectedResults.indices) {
            val value = indicator.evaluate(rows[loop])
            assertEquals("Testing $fieldType row $loop", expectedResults[loop].toLong(), value.toLong())
        }
    }

    companion object {

        private val NONE = 0
        private val GREEN_BALL = 1
        private val AMBER_BALL = 2
        private val RED_BALL = 3
        private val BLACK_BALL = 4
        private val WHITE_BALL = 5

        private val COST1_RESULTS = intArrayOf(BLACK_BALL, GREEN_BALL, AMBER_BALL, RED_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL)

        private val COST2_RESULTS = intArrayOf(WHITE_BALL, AMBER_BALL, RED_BALL, RED_BALL, RED_BALL, NONE, NONE, BLACK_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL)

        private val COST3_RESULTS = intArrayOf(RED_BALL, RED_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, RED_BALL, RED_BALL, NONE, NONE, NONE, NONE, RED_BALL, RED_BALL)

        private val COST4_RESULTS = intArrayOf(GREEN_BALL, AMBER_BALL, GREEN_BALL, RED_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, GREEN_BALL)

        private val DATE1_RESULTS = intArrayOf(RED_BALL, RED_BALL, GREEN_BALL, NONE, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL)

        private val DATE2_RESULTS = intArrayOf(GREEN_BALL, GREEN_BALL, GREEN_BALL, AMBER_BALL, NONE, NONE, RED_BALL, RED_BALL, RED_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL)

        private val DATE3_RESULTS = intArrayOf(AMBER_BALL, AMBER_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, AMBER_BALL, AMBER_BALL, NONE, NONE, NONE, NONE, AMBER_BALL, AMBER_BALL)

        private val DATE4_RESULTS = intArrayOf(GREEN_BALL, RED_BALL, GREEN_BALL, RED_BALL, AMBER_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, GREEN_BALL)

        private val DATE5_RESULTS = intArrayOf(GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL)

        private val DURATION1_RESULTS = intArrayOf(AMBER_BALL, GREEN_BALL, AMBER_BALL, RED_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL)

        private val DURATION2_RESULTS = intArrayOf(WHITE_BALL, AMBER_BALL, RED_BALL, RED_BALL, RED_BALL, NONE, NONE, BLACK_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL)

        private val DURATION3_RESULTS = intArrayOf(RED_BALL, RED_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, RED_BALL, RED_BALL, NONE, NONE, NONE, NONE, RED_BALL, RED_BALL)

        private val DURATION4_RESULTS = intArrayOf(GREEN_BALL, AMBER_BALL, GREEN_BALL, RED_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, GREEN_BALL)

        private val FLAG_RESULTS = intArrayOf(RED_BALL, GREEN_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL)

        private val NUMBER1_RESULTS = intArrayOf(AMBER_BALL, GREEN_BALL, AMBER_BALL, RED_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL)

        private val NUMBER2_RESULTS = intArrayOf(WHITE_BALL, AMBER_BALL, RED_BALL, RED_BALL, RED_BALL, NONE, NONE, BLACK_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL)

        private val NUMBER3_RESULTS = intArrayOf(RED_BALL, RED_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, RED_BALL, RED_BALL, NONE, NONE, NONE, NONE, RED_BALL, RED_BALL)

        private val NUMBER4_RESULTS = intArrayOf(GREEN_BALL, AMBER_BALL, GREEN_BALL, RED_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, GREEN_BALL)

        private val TEXT1_RESULTS = intArrayOf(AMBER_BALL, GREEN_BALL, AMBER_BALL, RED_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL)

        private val TEXT2_RESULTS = intArrayOf(WHITE_BALL, AMBER_BALL, RED_BALL, RED_BALL, RED_BALL, NONE, NONE, BLACK_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL, WHITE_BALL)

        private val TEXT3_RESULTS = intArrayOf(RED_BALL, RED_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, RED_BALL, RED_BALL, NONE, NONE, NONE, NONE, RED_BALL, RED_BALL)

        private val TEXT4_RESULTS = intArrayOf(GREEN_BALL, AMBER_BALL, GREEN_BALL, RED_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, AMBER_BALL, GREEN_BALL)

        private val TEXT5_RESULTS = intArrayOf(RED_BALL, GREEN_BALL, GREEN_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL, RED_BALL)

        private val TEXT6_RESULTS = intArrayOf(NONE, GREEN_BALL, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE)

        private val TEXT7_RESULTS = intArrayOf(GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL, GREEN_BALL)
    }

}
