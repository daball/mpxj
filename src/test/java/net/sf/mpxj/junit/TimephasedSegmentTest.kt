/*
 * file:       TimephasedSegmentTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       2011-02-12
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

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

import net.sf.mpxj.DateRange
import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.Task
import net.sf.mpxj.TimephasedWork
import net.sf.mpxj.mpp.MPPReader
import net.sf.mpxj.mpp.TimescaleUnits
import net.sf.mpxj.utility.TimephasedUtility
import net.sf.mpxj.utility.TimescaleUtility

import org.junit.Test

/**
 * This example shows an MPP, MPX or MSPDI file being read, and basic
 * task and resource data being extracted.
 */
class TimephasedSegmentTest {

    /*
    * Method used to print segment durations as an array - useful for
    * creating new test cases.
    *
    * @param assignment parent assignment
    * @param list list of durations
    */
    /*
      private void dumpExpectedData(ResourceAssignment assignment, ArrayList<Duration> list)
      {
         //System.out.println(assignment);
         System.out.print("new double[]{");
         boolean first = true;
         for(Duration d : list)
         {
            if (!first)
            {
               System.out.print(", ");
            }
            else
            {
               first = false;
            }
            System.out.print(d.getDuration());
         }
         System.out.println("}");
      }
   */

    private val m_timescale = TimescaleUtility()
    private val m_timephased = TimephasedUtility()
    /**
     * Timephased segment test for MPP9 files.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp9timephasedsegments.mpp"))
        testSegments(file)
    }

    /**
     * Timephased segment test for MPP9 files saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9From12() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp9timephasedsegments-from12.mpp"))
        testSegments(file)
    }

    /**
     * Timephased segment test for MPP9 files saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9From14() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp9timephasedsegments-from14.mpp"))
        testSegments(file)
    }

    /**
     * Timephased segment test for MPP12 files.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp12timephasedsegments.mpp"))
        testSegments(file)
    }

    /**
     * Timephased segment test for MPP12 files saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12From14() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp12timephasedsegments-from14.mpp"))
        testSegments(file)
    }

    /**
     * Timephased segment test for MPP14 files.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp14timephasedsegments.mpp"))
        testSegments(file)
    }

    /**
     * Timephased segment test for MSPDI files.
     *
     * @throws Exception
     */
    //   @Test public void testMspdi () throws Exception
    //   {
    //      ProjectFile file = new MSPDIReader().read(MpxjTestData.filePath("mspditimephasedsegments.xml");
    //      testSegments(file);
    //   }

    /**
     * Suite of tests common to all file types.
     *
     * @param file ProjectFile instance
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun testSegments(file: ProjectFile) {
        //
        // Set the start date
        //
        val df = SimpleDateFormat("dd/MM/yyyy")
        val startDate = df.parse("07/02/2011")

        //
        // Task One - 5 day assignment at 100% utilisation
        //
        var task = file.getTaskByID(Integer.valueOf(1))
        assertEquals("Task One", task.name)
        var assignments = task.resourceAssignments
        var assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(8.0, 8.0, 8.0, 8.0, 8.0, 0.0, 0.0, 0.0, 0.0, 0.0), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), true)

        //
        // Task Two - 5 day assignment at 50% utilisation
        //
        task = file.getTaskByID(Integer.valueOf(2))
        assertEquals("Task Two", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(4.0, 4.0, 4.0, 4.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), true)

        //
        // Task Three - 5 day assignment at 100% utilisation, 50% complete
        //
        task = file.getTaskByID(Integer.valueOf(3))
        assertEquals("Task Three", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.0, 0.0, 4.0, 8.0, 8.0, 0.0, 0.0, 0.0, 0.0, 0.0), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(8.0, 8.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), true)

        //
        // Task Four - 5 day assignment at 50% utilisation, 50% complete
        //
        task = file.getTaskByID(Integer.valueOf(4))
        assertEquals("Task Four", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.0, 0.0, 2.0, 4.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(4.0, 4.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), true)

        //
        // Task Five - 10 day assignment at 100% utilisation
        //
        task = file.getTaskByID(Integer.valueOf(5))
        assertEquals("Task Five", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(8.0, 8.0, 8.0, 8.0, 8.0, 0.0, 0.0, 8.0, 8.0, 8.0, 8.0, 8.0, 0.0, 0.0, 0.0), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), true)

        //
        // Task Six - 10 day assignment at 50% utilisation
        //
        task = file.getTaskByID(Integer.valueOf(6))
        assertEquals("Task Six", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(4.0, 4.0, 4.0, 4.0, 4.0, 0.0, 0.0, 4.0, 4.0, 4.0, 4.0, 4.0, 0.0, 0.0, 0.0), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), true)

        //
        // Task Seven - 10 day assignment at 100% utilisation with a resource calendar non-working day and a non-default working day
        //
        task = file.getTaskByID(Integer.valueOf(7))
        assertEquals("Task Seven", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(8.0, 0.0, 8.0, 8.0, 8.0, 8.0, 0.0, 8.0, 8.0, 8.0, 8.0, 8.0, 0.0, 0.0, 0.0), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), true)

        //
        // Task Eight - 10 day assignment at 100% utilisation with a task calendar, ignoring resource calendar
        //
        task = file.getTaskByID(Integer.valueOf(8))
        assertEquals("Task Eight", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(8.0, 0.0, 8.0, 0.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 0.0, 0.0, 0.0), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), true)

        //
        // Task Nine - 10 day assignment at 100% utilisation front loaded
        //
        task = file.getTaskByID(Integer.valueOf(9))
        assertEquals("Task Nine", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(8.0, 8.0, 8.0, 8.0, 8.0, 0.0, 0.0, 6.0, 6.0, 6.0, 4.67, 4.0, 0.0, 0.0, 4.0, 3.33, 2.0, 1.47, 1.2, 0.0, 0.0, 0.8, 0.53, 0.0, 0.0), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.00, 0.0, 0.0, 0.0, 0.0, 0.00, 0.0, 0.00, 0.0, 0.0, 0.0, 0.0, 0.00, 0.0, 0.0), true)

        //
        // Task Ten - 10 day assignment at 100% utilisation back loaded
        //
        task = file.getTaskByID(Integer.valueOf(10))
        assertEquals("Task Ten", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.80, 0.93, 1.20, 1.73, 2.00, 0.00, 0.00, 4.00, 4.00, 4.00, 5.33, 6.00, 0.00, 0.00, 6.00, 6.67, 8.00, 8.00, 8.00, 0.00, 0.00, 8.00, 5.33, 0.00), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00), true)

        //
        // Task Eleven - 10 day assignment at 100% utilisation double peak
        //
        task = file.getTaskByID(Integer.valueOf(11))
        assertEquals("Task Eleven", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(2.00, 2.00, 4.00, 4.00, 8.00, 0.00, 0.00, 8.00, 4.00, 4.00, 2.00, 2.00, 0.00, 0.00, 2.00, 2.00, 4.00, 4.00, 8.00, 0.00, 0.00, 8.00, 4.00, 4.00, 2.00, 2.00, 0.00, 0.00, 0.00), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00), true)

        //
        // Task Twelve - 10 day assignment at 100% utilisation early peak
        //
        task = file.getTaskByID(Integer.valueOf(12))
        assertEquals("Task Twelve", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(2.00, 2.00, 4.00, 4.00, 8.00, 0.00, 0.00, 8.00, 8.00, 8.00, 6.00, 6.00, 0.00, 0.00, 4.00, 4.00, 4.00, 4.00, 2.00, 0.00, 0.00, 2.00, 1.20, 1.20, 0.80, 0.80, 0.00, 0.00, 0.00), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00), true)

        //
        // Task Thirteen - 10 day assignment at 100% utilisation late peak
        //
        task = file.getTaskByID(Integer.valueOf(13))
        assertEquals("Task Thirteen", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.80, 0.80, 1.20, 1.20, 2.00, 0.00, 0.00, 2.00, 4.00, 4.00, 4.00, 4.00, 0.00, 0.00, 6.00, 6.00, 8.00, 8.00, 8.00, 0.00, 0.00, 8.00, 4.00, 4.00, 2.00, 2.00, 0.00, 0.00, 0.00), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00), true)

        //
        // Task Fourteen - 10 day assignment at 100% utilisation bell
        //
        task = file.getTaskByID(Integer.valueOf(14))
        assertEquals("Task Fourteen", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.80, 0.80, 1.60, 1.60, 3.20, 0.00, 0.00, 3.20, 6.40, 6.40, 8.00, 8.00, 0.00, 0.00, 8.00, 8.00, 6.40, 6.40, 3.20, 0.00, 0.00, 3.20, 1.60, 1.60, 0.80, 0.80, 0.00, 0.00, 0.00), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00), true)

        //
        // Task Fifteen - 10 day assignment at 100% utilisation turtle
        //
        task = file.getTaskByID(Integer.valueOf(15))
        assertEquals("Task Fifteen", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(2.00, 3.15, 4.28, 6.00, 7.43, 0.00, 0.00, 8.00, 8.00, 8.00, 8.00, 8.00, 0.00, 0.00, 6.00, 4.85, 3.72, 2.00, 0.57, 0.00, 0.00, 0.00), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00), true)

        //
        // Task Sixteen - 10 day assignment at 100% utilisation hand edited
        //
        task = file.getTaskByID(Integer.valueOf(16))
        assertEquals("Task Sixteen", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(1.00, 2.00, 3.00, 4.00, 5.00, 0.00, 0.00, 6.00, 7.00, 8.00, 9.00, 10.00, 0.00, 0.00, 0.00), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 00.00, 0.00, 0.00, 0.00), true)

        //
        // Task Seventeen - 10 day assignment at 100% utilisation contoured with a resource calendar non-working day
        //
        task = file.getTaskByID(Integer.valueOf(17))
        assertEquals("Task Seventeen", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(8.00, 8.00, 0.00, 8.00, 8.00, 0.00, 0.00, 8.00, 6.00, 6.00, 6.00, 4.67, 0.00, 0.00, 4.00, 4.00, 3.33, 2.00, 1.47, 0.00, 0.00, 1.20, 0.80, 0.53, 0.00, 0.00, 0.00), false)
        testSegments(assignment, startDate, TimescaleUnits.DAYS, doubleArrayOf(0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00), true)

        //
        // Tests of timescale units
        //
        task = file.getTaskByID(Integer.valueOf(18))
        assertEquals("Task Eighteen", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.WEEKS, doubleArrayOf(40.0, 40.0, 40.0, 40.0, 40.0, 40.0, 0.0), false)
        testSegments(assignment, startDate, TimescaleUnits.THIRDS_OF_MONTHS, doubleArrayOf(32.0, 48.0, 48.0, 64.0, 48.0, 0.0), false)
        testSegments(assignment, startDate, TimescaleUnits.MONTHS, doubleArrayOf(128.0, 112.0, 0.0), false)

        task = file.getTaskByID(Integer.valueOf(19))
        assertEquals("Task Nineteen", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.QUARTERS, doubleArrayOf(312.0, 520.0, 528.0, 160.0, 0.0), false)
        testSegments(assignment, startDate, TimescaleUnits.HALF_YEARS, doubleArrayOf(832.0, 688.0, 0.0), false)

        task = file.getTaskByID(Integer.valueOf(20))
        assertEquals("Task Twenty", task.name)
        assignments = task.resourceAssignments
        assignment = assignments.get(0)
        testSegments(assignment, startDate, TimescaleUnits.YEARS, doubleArrayOf(1880.0, 1160.0, 0.0), false)
    }

    /**
     * Common method used to test timephased assignment segments against expected data.
     *
     * @param assignment parent resource assignment
     * @param startDate start date for segments
     * @param units units of duration for each segment
     * @param expected array of expected durations for each segment
     * @param complete flag indicating if planned or complete work is required
     */
    private fun testSegments(assignment: ResourceAssignment, startDate: Date, units: TimescaleUnits, expected: DoubleArray, complete: Boolean) {
        val dateList = m_timescale.createTimescale(startDate, units, expected.size)
        //System.out.println(dateList);
        val calendar = assignment.calendar
        val assignments = if (complete) assignment.timephasedActualWork else assignment.timephasedWork
        val durationList = m_timephased.segmentWork(calendar, assignments, units, dateList)
        //dumpExpectedData(assignment, durationList);
        assertEquals(expected.size, durationList.size())
        for (loop in expected.indices) {
            assertEquals("Failed at index $loop", expected[loop], durationList.get(loop).getDuration(), 0.009)
        }
    }
}
