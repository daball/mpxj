/*
 * file:       MppCalendarTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       5-October-2006
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

import java.text.DateFormat
import java.text.SimpleDateFormat

import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.DayType
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * Tests to exercise MPP file read functionality for various versions of
 * MPP file.
 */
class MppCalendarTest {
    /**
     * Test calendar data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9Calendar() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9calendar.mpp"))
        testCalendars(mpp)
    }

    /**
     * Test calendar data read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9CalendarFrom12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9calendar-from12.mpp"))
        testCalendars(mpp)
    }

    /**
     * Test calendar data read from an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9CalendarFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9calendar-from14.mpp"))
        testCalendars(mpp)
    }

    /**
     * Test calendar data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12Calendar() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12calendar.mpp"))
        testCalendars(mpp)
    }

    /**
     * Test calendar data read from an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12CalendarFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14calendar.mpp"))
        testCalendars(mpp)
    }

    /**
     * Test calendar data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14Calendar() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14calendar.mpp"))
        testCalendars(mpp)
    }

    /**
     * Test calendar data read from an MPD9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpd9Calendar() {
        assumeJvm()
        val mpp = MPDDatabaseReader().read(MpxjTestData.filePath("mpp9calendar.mpd"))
        testCalendars(mpp)
    }

    /**
     * Test calendar exception data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9CalendarExceptions() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9exceptions.mpp"))
        testExceptions(mpp)
    }

    /**
     * Test calendar exception data read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9CalendarExceptionsFrom12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9exceptions-from12.mpp"))
        testExceptions(mpp)
    }

    /**
     * Test calendar exception data read from an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9CalendarExceptionsFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9exceptions-from14.mpp"))
        testExceptions(mpp)
    }

    /**
     * Test calendar exception data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12CalendarExceptions() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12exceptions.mpp"))
        testExceptions(mpp)
    }

    /**
     * Test calendar exception data read from an MPP12 file saved by Project 2010..
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12CalendarExceptionsFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12exceptions-from14.mpp"))
        testExceptions(mpp)
    }

    /**
     * Test calendar exception data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14CalendarExceptions() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14exceptions.mpp"))
        testExceptions(mpp)
    }

    /**
     * Test calendar data.
     *
     * @param mpp ProjectFile instance
     */
    private fun testCalendars(mpp: ProjectFile) {
        val tf = SimpleDateFormat("HH:mm")

        val baseCalendars = mpp.calendars
        assertEquals(8, baseCalendars.size())

        val cal = mpp.getCalendarByUniqueID(Integer.valueOf(1))
        assertNotNull(cal)
        assertEquals("Standard", cal.name)
        assertNull(cal.parent)
        assertFalse(cal.isDerived)
        assertEquals(DayType.WORKING, cal.getWorkingDay(Day.MONDAY))
        assertEquals(DayType.WORKING, cal.getWorkingDay(Day.TUESDAY))
        assertEquals(DayType.WORKING, cal.getWorkingDay(Day.WEDNESDAY))
        assertEquals(DayType.WORKING, cal.getWorkingDay(Day.THURSDAY))
        assertEquals(DayType.WORKING, cal.getWorkingDay(Day.FRIDAY))

        assertEquals(DayType.NON_WORKING, cal.getWorkingDay(Day.SATURDAY))
        assertEquals(DayType.NON_WORKING, cal.getWorkingDay(Day.SUNDAY))

        assertEquals(0, cal.calendarExceptions.size())

        val hours = cal.getCalendarHours(Day.MONDAY)
        assertEquals(2, hours.rangeCount.toLong())

        var range = hours.getRange(0)
        assertEquals("08:00", tf.format(range.getStart()))
        assertEquals("12:00", tf.format(range.getEnd()))
        range = cal.getCalendarHours(Day.MONDAY).getRange(1)
        assertEquals("13:00", tf.format(range.getStart()))
        assertEquals("17:00", tf.format(range.getEnd()))
    }

    /**
     * Test calendar exceptions.
     *
     * @param mpp ProjectFile instance
     */
    private fun testExceptions(mpp: ProjectFile) {
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val tf = SimpleDateFormat("HH:mm")

        val baseCalendars = mpp.calendars
        assertEquals(2, baseCalendars.size())

        val cal = mpp.getCalendarByUniqueID(Integer.valueOf(1))
        assertNotNull(cal)
        assertEquals("Standard", cal.name)
        assertNull(cal.parent)
        assertFalse(cal.isDerived)
        assertEquals(DayType.WORKING, cal.getWorkingDay(Day.MONDAY))
        assertEquals(DayType.NON_WORKING, cal.getWorkingDay(Day.TUESDAY))
        assertEquals(DayType.WORKING, cal.getWorkingDay(Day.WEDNESDAY))
        assertEquals(DayType.WORKING, cal.getWorkingDay(Day.THURSDAY))
        assertEquals(DayType.WORKING, cal.getWorkingDay(Day.FRIDAY))
        assertEquals(DayType.WORKING, cal.getWorkingDay(Day.SATURDAY))
        assertEquals(DayType.NON_WORKING, cal.getWorkingDay(Day.SUNDAY))

        val exceptions = cal.calendarExceptions
        assertEquals(3, exceptions.size())

        var exception = exceptions.get(0)
        assertFalse(exception.working)
        assertEquals("05/03/2008 00:00", df.format(exception.fromDate))
        assertEquals("05/03/2008 23:59", df.format(exception.toDate))
        assertNull(exception.getRange(0).getStart())
        assertNull(exception.getRange(0).getEnd())
        assertNull(exception.getRange(1).getStart())
        assertNull(exception.getRange(1).getEnd())
        assertNull(exception.getRange(2).getStart())
        assertNull(exception.getRange(2).getEnd())
        assertNull(exception.getRange(3).getStart())
        assertNull(exception.getRange(3).getEnd())
        assertNull(exception.getRange(4).getStart())
        assertNull(exception.getRange(4).getEnd())

        exception = exceptions.get(1)
        assertTrue(exception.working)
        assertEquals("09/03/2008 00:00", df.format(exception.fromDate))
        assertEquals("09/03/2008 23:59", df.format(exception.toDate))
        assertEquals("08:00", tf.format(exception.getRange(0).getStart()))
        assertEquals("12:00", tf.format(exception.getRange(0).getEnd()))
        assertEquals("13:00", tf.format(exception.getRange(1).getStart()))
        assertEquals("17:00", tf.format(exception.getRange(1).getEnd()))
        assertNull(exception.getRange(2).getStart())
        assertNull(exception.getRange(2).getEnd())
        assertNull(exception.getRange(3).getStart())
        assertNull(exception.getRange(3).getEnd())
        assertNull(exception.getRange(4).getStart())
        assertNull(exception.getRange(4).getEnd())

        exception = exceptions.get(2)
        assertTrue(exception.working)
        assertEquals("16/03/2008 00:00", df.format(exception.fromDate))
        assertEquals("16/03/2008 23:59", df.format(exception.toDate))
        assertEquals("08:00", tf.format(exception.getRange(0).getStart()))
        assertEquals("09:00", tf.format(exception.getRange(0).getEnd()))
        assertEquals("11:00", tf.format(exception.getRange(1).getStart()))
        assertEquals("12:00", tf.format(exception.getRange(1).getEnd()))
        assertEquals("14:00", tf.format(exception.getRange(2).getStart()))
        assertEquals("15:00", tf.format(exception.getRange(2).getEnd()))
        assertEquals("16:00", tf.format(exception.getRange(3).getStart()))
        assertEquals("17:00", tf.format(exception.getRange(3).getEnd()))
        assertEquals("18:00", tf.format(exception.getRange(4).getStart()))
        assertEquals("19:00", tf.format(exception.getRange(4).getEnd()))
    }
}
