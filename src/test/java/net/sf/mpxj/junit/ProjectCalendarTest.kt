/*
 * file:       ProjectCalendarTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       17-Mar-2006
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

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

import net.sf.mpxj.DateRange
import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * This class contains tests used to exercise ProjectCalendar functionality.
 */
class ProjectCalendarTest {
    /**
     * Test get getWork method.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testGetWork() {
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm")
        var startDate: Date
        var endDate: Date
        var variance: Duration

        val project = ProjectFile()
        val projectCalendar = project.addDefaultBaseCalendar()

        startDate = df.parse("14/03/2006 08:00")
        endDate = df.parse("15/03/2006 08:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(1.0, variance.duration, 0.01)

        endDate = df.parse("13/03/2006 08:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(-1.0, variance.duration, 0.01)

        startDate = df.parse("14/03/2006 08:00")
        endDate = df.parse("15/03/2006 09:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(1.13, variance.duration, 0.01)

        endDate = df.parse("15/03/2006 09:30")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(1.19, variance.duration, 0.01)

        endDate = df.parse("15/03/2006 12:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(1.5, variance.duration, 0.01)

        endDate = df.parse("15/03/2006 13:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(1.5, variance.duration, 0.01)

        endDate = df.parse("15/03/2006 14:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(1.63, variance.duration, 0.01)

        endDate = df.parse("15/03/2006 16:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(1.88, variance.duration, 0.01)

        endDate = df.parse("15/03/2006 17:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(2.0, variance.duration, 0.01)

        endDate = df.parse("16/03/2006 07:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(2.0, variance.duration, 0.01)

        endDate = df.parse("16/03/2006 08:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(2.0, variance.duration, 0.01)

        endDate = df.parse("18/03/2006 08:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(4.0, variance.duration, 0.01)

        endDate = df.parse("19/03/2006 08:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(4.0, variance.duration, 0.01)

        endDate = df.parse("20/03/2006 08:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(4.0, variance.duration, 0.01)

        startDate = df.parse("18/03/2006 08:00")
        endDate = df.parse("19/03/2006 17:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(0.0, variance.duration, 0.01)

        startDate = df.parse("18/03/2006 08:00")
        endDate = df.parse("20/03/2006 17:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(1.0, variance.duration, 0.01)

        startDate = df.parse("17/03/2006 08:00")
        endDate = df.parse("20/03/2006 17:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(2.0, variance.duration, 0.01)

        startDate = df.parse("17/03/2006 08:00")
        endDate = df.parse("18/03/2006 17:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(1.0, variance.duration, 0.01)

        //
        // Try a date in BST
        //
        startDate = df.parse("10/07/2006 08:00")
        endDate = df.parse("11/07/2006 08:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(1.0, variance.duration, 0.01)

        //
        // Try a date crossing GMT to BST
        //
        startDate = df.parse("13/03/2006 08:00")
        endDate = df.parse("11/07/2006 08:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(86.0, variance.duration, 0.01)

        //
        // Same date tests
        //
        startDate = df.parse("14/03/2006 08:00")
        endDate = df.parse("14/03/2006 08:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(0.0, variance.duration, 0.01)

        endDate = df.parse("14/03/2006 09:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(0.13, variance.duration, 0.01)

        endDate = df.parse("14/03/2006 10:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(0.25, variance.duration, 0.01)

        endDate = df.parse("14/03/2006 11:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(0.38, variance.duration, 0.01)

        endDate = df.parse("14/03/2006 12:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(0.5, variance.duration, 0.01)

        endDate = df.parse("14/03/2006 13:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(0.5, variance.duration, 0.01)

        endDate = df.parse("14/03/2006 16:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(0.88, variance.duration, 0.01)

        endDate = df.parse("14/03/2006 17:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(1.0, variance.duration, 0.01)

        endDate = df.parse("14/03/2006 18:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(1.0, variance.duration, 0.01)

        //
        // Same date non-working day
        //
        startDate = df.parse("12/03/2006 08:00")
        endDate = df.parse("12/03/2006 17:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(0.0, variance.duration, 0.01)

        //
        // Exception tests
        //
        startDate = df.parse("13/03/2006 08:00")
        endDate = df.parse("24/03/2006 16:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(9.88, variance.duration, 0.01)

        projectCalendar.addCalendarException(df.parse("14/03/2006 00:00"), df.parse("14/03/2006 23:59"))

        startDate = df.parse("13/03/2006 08:00")
        endDate = df.parse("24/03/2006 16:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(8.88, variance.duration, 0.01)

        val exception = projectCalendar.addCalendarException(df.parse("18/03/2006 00:00"), df.parse("18/03/2006 23:59"))
        exception.addRange(DateRange(df.parse("18/03/2006 08:00"), df.parse("18/03/2006 12:00")))

        startDate = df.parse("18/03/2006 08:00")
        endDate = df.parse("18/03/2006 16:00")
        variance = projectCalendar.getWork(startDate, endDate, TimeUnit.DAYS)
        assertEquals(0.5, variance.duration, 0.01)
    }

    /**
     * Exercise various duration variance calculations.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testVarianceCalculations9() {
        val reader = MPPReader()
        val file = reader.read(MpxjTestData.filePath("DurationTest9.mpp"))
        var task: Task
        var duration: Duration?

        //
        // Task 1
        //
        task = file.getTaskByID(Integer.valueOf(1))

        duration = task.durationVariance
        assertEquals(-59.0, duration!!.duration, 0.01)
        assertEquals(TimeUnit.MINUTES, duration.units)

        duration = task.startVariance
        assertEquals(-1.09, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration.units)

        duration = task.finishVariance
        assertEquals(-1.97, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration.units)

        //
        // Task 2
        //
        task = file.getTaskByID(Integer.valueOf(2))

        duration = task.durationVariance
        assertEquals(0.98, duration!!.duration, 0.01)
        assertEquals(TimeUnit.HOURS, duration.units)

        duration = task.startVariance
        assertEquals(0.94, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration.units)

        duration = task.finishVariance
        assertEquals(0.13, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration.units)

        //
        // Task 3
        //
        task = file.getTaskByID(Integer.valueOf(3))

        duration = task.durationVariance
        assertEquals(-4, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration!!.units)

        duration = task.startVariance
        assertEquals(0.88, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration.units)

        duration = task.finishVariance
        assertEquals(-1, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration!!.units)

        //
        // Task 4
        //
        task = file.getTaskByID(Integer.valueOf(4))

        duration = task.durationVariance
        assertEquals(0.8, duration!!.duration, 0.01)
        assertEquals(TimeUnit.WEEKS, duration.units)

        duration = task.startVariance
        assertEquals(0, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration!!.units)

        //
        // Task 5
        //
        task = file.getTaskByID(Integer.valueOf(5))

        duration = task.durationVariance
        assertEquals(-1.45, duration!!.duration, 0.01)
        assertEquals(TimeUnit.MONTHS, duration.units)

        duration = task.startVariance
        assertEquals(0, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration!!.units)

        //
        // Task 6
        //
        task = file.getTaskByID(Integer.valueOf(6))

        duration = task.durationVariance
        assertEquals(-59, duration!!.duration, 0.01)
        assertEquals(TimeUnit.MINUTES, duration!!.units)

        duration = task.startVariance
        assertEquals(-5, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration!!.units)

        //
        // Task 7
        //
        task = file.getTaskByID(Integer.valueOf(7))

        duration = task.durationVariance
        assertEquals(0.98, duration!!.duration, 0.01)
        assertEquals(TimeUnit.HOURS, duration.units)

        duration = task.startVariance
        assertEquals(-5, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration!!.units)

        //
        // Task 8
        //
        task = file.getTaskByID(Integer.valueOf(8))

        duration = task.durationVariance
        assertEquals(-4, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration!!.units)

        duration = task.startVariance
        assertEquals(-5, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration!!.units)

        //
        // Task 9
        //
        task = file.getTaskByID(Integer.valueOf(9))

        duration = task.durationVariance
        assertEquals(0.8, duration!!.duration, 0.01)
        assertEquals(TimeUnit.WEEKS, duration.units)

        duration = task.startVariance
        assertEquals(-6, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration!!.units)

        //
        // Task 10
        //
        task = file.getTaskByID(Integer.valueOf(10))

        duration = task.durationVariance
        assertEquals(-1.5, duration!!.duration, 0.01)
        assertEquals(TimeUnit.MONTHS, duration.units)

        //
        // Task 11
        //
        task = file.getTaskByID(Integer.valueOf(11))

        duration = task.durationVariance
        assertEquals(-59, duration!!.duration, 0.01)
        assertEquals(TimeUnit.ELAPSED_MINUTES, duration!!.units)

        //
        // Task 12
        //
        task = file.getTaskByID(Integer.valueOf(12))

        duration = task.durationVariance
        assertEquals(0.98, duration!!.duration, 0.01)
        assertEquals(TimeUnit.ELAPSED_HOURS, duration.units)

        //
        // Task 13
        //
        task = file.getTaskByID(Integer.valueOf(13))

        duration = task.durationVariance
        assertEquals(-0.67, duration!!.duration, 0.01)
        assertEquals(TimeUnit.ELAPSED_DAYS, duration.units)

        //
        // Task 14
        //
        task = file.getTaskByID(Integer.valueOf(14))

        duration = task.durationVariance
        assertEquals(0.95, duration!!.duration, 0.01)
        assertEquals(TimeUnit.ELAPSED_WEEKS, duration.units)

        //
        // Task 15
        //
        task = file.getTaskByID(Integer.valueOf(15))

        duration = task.durationVariance
        assertEquals(0.44, duration!!.duration, 0.01)
        assertEquals(TimeUnit.ELAPSED_MONTHS, duration.units)
    }

    /**
     * Exercise various duration variance calculations.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testVarianceCalculations8() {
        val reader = MPPReader()
        val file = reader.read(MpxjTestData.filePath("DurationTest8.mpp"))
        var task: Task
        var duration: Duration?

        //
        // Task 1
        //
        task = file.getTaskByID(Integer.valueOf(1))

        duration = task.durationVariance
        assertEquals(-59.0, duration!!.duration, 0.01)
        assertEquals(TimeUnit.MINUTES, duration.units)

        duration = task.startVariance
        assertEquals(-1.09, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration.units)

        duration = task.finishVariance
        assertEquals(-1.97, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration.units)

        //
        // Task 2
        //
        task = file.getTaskByID(Integer.valueOf(2))

        duration = task.durationVariance
        assertEquals(0.98, duration!!.duration, 0.01)
        assertEquals(TimeUnit.HOURS, duration.units)

        duration = task.startVariance
        assertEquals(0.94, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration.units)

        duration = task.finishVariance
        assertEquals(0.13, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration.units)

        //
        // Task 3
        //
        task = file.getTaskByID(Integer.valueOf(3))

        duration = task.durationVariance
        assertEquals(-4, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration!!.units)

        duration = task.startVariance
        assertEquals(0.88, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration.units)

        duration = task.finishVariance
        assertEquals(-1, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration!!.units)

        //
        // Task 4
        //
        task = file.getTaskByID(Integer.valueOf(4))

        duration = task.durationVariance
        assertEquals(0.8, duration!!.duration, 0.01)
        assertEquals(TimeUnit.WEEKS, duration.units)

        duration = task.startVariance
        assertEquals(0, duration!!.duration, 0.01)
        assertEquals(TimeUnit.DAYS, duration!!.units)
    }

    /**
     * Simple tests to exercise the ProjectCalendar.getDate method.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testGetDate() {
        val file = ProjectFile()
        var duration: Duration
        val cal = file.addDefaultBaseCalendar()
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm")
        var startDate = df.parse("09/10/2003 08:00")

        //
        // Add one 8 hour day
        //
        duration = Duration.getInstance(8, TimeUnit.HOURS)
        var endDate = cal.getDate(startDate, duration, false)
        assertEquals("09/10/2003 17:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("10/10/2003 08:00", df.format(endDate))

        //
        // Add two 8 hour days
        //
        duration = Duration.getInstance(16, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("10/10/2003 17:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("13/10/2003 08:00", df.format(endDate))

        //
        // Add three 8 hour days which span a weekend
        //
        duration = Duration.getInstance(24, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("13/10/2003 17:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("14/10/2003 08:00", df.format(endDate))

        //
        // Add 9 hours from the start of a day
        //
        duration = Duration.getInstance(9, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("10/10/2003 09:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("10/10/2003 09:00", df.format(endDate))

        //
        // Add 1 hour from the start of a day
        //
        duration = Duration.getInstance(1, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("09/10/2003 09:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("09/10/2003 09:00", df.format(endDate))

        //
        // Add 1 hour offset by 1 hour from the start of a day
        //
        startDate = df.parse("09/10/2003 09:00")
        duration = Duration.getInstance(1, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("09/10/2003 10:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("09/10/2003 10:00", df.format(endDate))

        //
        // Add 1 hour which crosses a date ranges
        //
        startDate = df.parse("09/10/2003 11:30")
        duration = Duration.getInstance(1, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("09/10/2003 13:30", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("09/10/2003 13:30", df.format(endDate))

        //
        // Add 1 hour at the start of the second range
        //
        startDate = df.parse("09/10/2003 13:00")
        duration = Duration.getInstance(1, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("09/10/2003 14:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("09/10/2003 14:00", df.format(endDate))

        //
        // Add 1 hour offset by 1 hour from the start of the second range
        //
        startDate = df.parse("09/10/2003 14:00")
        duration = Duration.getInstance(1, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("09/10/2003 15:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("09/10/2003 15:00", df.format(endDate))

        //
        // Full first range
        //
        startDate = df.parse("09/10/2003 08:00")
        duration = Duration.getInstance(4, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("09/10/2003 12:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("09/10/2003 13:00", df.format(endDate))

        //
        // Full second range
        //
        startDate = df.parse("09/10/2003 13:00")
        duration = Duration.getInstance(4, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("09/10/2003 17:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("10/10/2003 08:00", df.format(endDate))

        //
        // Offset full first range
        //
        startDate = df.parse("09/10/2003 09:00")
        duration = Duration.getInstance(3, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("09/10/2003 12:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("09/10/2003 13:00", df.format(endDate))

        //
        // Offset full second range
        //
        startDate = df.parse("09/10/2003 14:00")
        duration = Duration.getInstance(3, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("09/10/2003 17:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("10/10/2003 08:00", df.format(endDate))

        //
        // Cross weekend
        //
        startDate = df.parse("09/10/2003 8:00")
        duration = Duration.getInstance(24, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("13/10/2003 17:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("14/10/2003 08:00", df.format(endDate))

        //
        // Make Friday 10th a non-working day
        //
        cal.addCalendarException(df.parse("10/10/2003 00:00"), df.parse("10/10/2003 23:59"))

        //
        // Cross weekend with a non-working day exception
        //
        startDate = df.parse("09/10/2003 8:00")
        duration = Duration.getInstance(24, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("14/10/2003 17:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("15/10/2003 08:00", df.format(endDate))

        //
        // Make Saturday 11th a working day
        //
        val ex = cal.addCalendarException(df.parse("11/10/2003 00:00"), df.parse("11/10/2003 23:59"))
        ex.addRange(DateRange(df.parse("11/10/2003 09:00"), df.parse("11/10/2003 13:00")))

        //
        // Cross weekend with a non-working day exception and a working day exception
        //
        startDate = df.parse("09/10/2003 8:00")
        duration = Duration.getInstance(24, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("14/10/2003 12:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("14/10/2003 13:00", df.format(endDate))

        //
        // Make the start date a non-working day
        //
        startDate = df.parse("12/10/2003 8:00")
        duration = Duration.getInstance(8, TimeUnit.HOURS)
        endDate = cal.getDate(startDate, duration, false)
        assertEquals("13/10/2003 17:00", df.format(endDate))
        endDate = cal.getDate(startDate, duration, true)
        assertEquals("14/10/2003 08:00", df.format(endDate))
    }

    /**
     * Simple tests to exercise the ProjectCalendar.getStartTime method.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testStartTime() {
        val file = ProjectFile()
        val cal = file.addDefaultBaseCalendar()
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm")

        //
        // Working day
        //
        assertEquals("01/01/0001 08:00", df.format(cal.getStartTime(df.parse("09/10/2003 00:00"))))

        //
        // Non-working day
        //
        assertNull(cal.getStartTime(df.parse("11/10/2003 00:00")))
    }
}
