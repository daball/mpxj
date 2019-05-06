/*
 * file:       RecurringExceptionsTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       2017-11-07
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

package net.sf.mpxj.junit.calendar

import net.sf.mpxj.junit.MpxjAssert.*
import org.junit.Assert.*

import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat

import org.junit.Test

import net.sf.mpxj.Day
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.RecurrenceType
import net.sf.mpxj.RecurringData
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.ProjectReaderUtility

/**
 * Tests to ensure basic calendar details are read correctly.
 */
class RecurringExceptionsTest {
    /**
     * Test to validate calendars in files saved by different versions of MS Project.
     */
    @Test
    @Throws(MPXJException::class)
    fun testRecurringExceptions() {
        for (file in MpxjTestData.listFiles("generated/calendar-recurring-exceptions", "calendar-recurring-exceptions")) {
            testRecurringExceptions(file)
        }
    }

    /**
     * Test an individual project.
     *
     * @param file project file
     */
    @Throws(MPXJException::class)
    private fun testRecurringExceptions(file: File) {
        //System.out.println(file);
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        if (reader is MPDDatabaseReader) {
            assumeJvm()
        }

        val df = SimpleDateFormat("dd/MM/yyyy")
        val project = reader.read(file)
        val calendar = project.getCalendarByName("Standard")
        val exceptions = calendar!!.calendarExceptions

        var exception = exceptions.get(0)
        assertEquals("Daily 1", exception.name)
        assertFalse(exception.working)
        var data = exception.recurring
        assertEquals(RecurrenceType.DAILY, data!!.recurrenceType)
        assertEquals(Integer.valueOf(1), data!!.frequency)
        assertEquals("01/01/2000", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(3), data!!.occurrences)

        exception = exceptions.get(1)
        assertEquals("Daily 2", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.DAILY, data!!.recurrenceType)
        assertEquals(Integer.valueOf(3), data!!.frequency)
        assertEquals("01/02/2000", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(4), data!!.occurrences)

        exception = exceptions.get(2)
        assertEquals("Daily 3", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.DAILY, data!!.recurrenceType)
        assertEquals(Integer.valueOf(5), data!!.frequency)
        assertEquals("01/03/2000", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(5), data!!.occurrences)

        exception = exceptions.get(3)
        assertEquals("Daily 4", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.DAILY, data!!.recurrenceType)
        assertEquals(Integer.valueOf(7), data!!.frequency)
        assertEquals("01/04/2000", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(6), data!!.occurrences)

        exception = exceptions.get(4)
        assertEquals("Weekly 1 Monday", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.WEEKLY, data!!.recurrenceType)
        assertEquals(Integer.valueOf(1), data!!.frequency)
        assertFalse(data!!.getWeeklyDay(Day.SUNDAY))
        assertTrue(data!!.getWeeklyDay(Day.MONDAY))
        assertFalse(data!!.getWeeklyDay(Day.TUESDAY))
        assertFalse(data!!.getWeeklyDay(Day.WEDNESDAY))
        assertFalse(data!!.getWeeklyDay(Day.THURSDAY))
        assertFalse(data!!.getWeeklyDay(Day.FRIDAY))
        assertFalse(data!!.getWeeklyDay(Day.SATURDAY))
        assertEquals("01/01/2001", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(3), data!!.occurrences)

        exception = exceptions.get(5)
        assertEquals("Weekly 2 Tuesday", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.WEEKLY, data!!.recurrenceType)
        assertEquals(Integer.valueOf(2), data!!.frequency)
        assertFalse(data!!.getWeeklyDay(Day.SUNDAY))
        assertFalse(data!!.getWeeklyDay(Day.MONDAY))
        assertTrue(data!!.getWeeklyDay(Day.TUESDAY))
        assertFalse(data!!.getWeeklyDay(Day.WEDNESDAY))
        assertFalse(data!!.getWeeklyDay(Day.THURSDAY))
        assertFalse(data!!.getWeeklyDay(Day.FRIDAY))
        assertFalse(data!!.getWeeklyDay(Day.SATURDAY))
        assertEquals("01/01/2001", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(4), data!!.occurrences)

        exception = exceptions.get(6)
        assertEquals("Weekly 3 Wednesday", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.WEEKLY, data!!.recurrenceType)
        assertEquals(Integer.valueOf(3), data!!.frequency)
        assertFalse(data!!.getWeeklyDay(Day.SUNDAY))
        assertFalse(data!!.getWeeklyDay(Day.MONDAY))
        assertFalse(data!!.getWeeklyDay(Day.TUESDAY))
        assertTrue(data!!.getWeeklyDay(Day.WEDNESDAY))
        assertFalse(data!!.getWeeklyDay(Day.THURSDAY))
        assertFalse(data!!.getWeeklyDay(Day.FRIDAY))
        assertFalse(data!!.getWeeklyDay(Day.SATURDAY))
        assertEquals("01/01/2001", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(5), data!!.occurrences)

        exception = exceptions.get(7)
        assertEquals("Weekly 4 Thursday", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.WEEKLY, data!!.recurrenceType)
        assertEquals(Integer.valueOf(4), data!!.frequency)
        assertFalse(data!!.getWeeklyDay(Day.SUNDAY))
        assertFalse(data!!.getWeeklyDay(Day.MONDAY))
        assertFalse(data!!.getWeeklyDay(Day.TUESDAY))
        assertFalse(data!!.getWeeklyDay(Day.WEDNESDAY))
        assertTrue(data!!.getWeeklyDay(Day.THURSDAY))
        assertFalse(data!!.getWeeklyDay(Day.FRIDAY))
        assertFalse(data!!.getWeeklyDay(Day.SATURDAY))
        assertEquals("01/01/2001", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(6), data!!.occurrences)

        exception = exceptions.get(8)
        assertEquals("Weekly 5 Friday", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.WEEKLY, data!!.recurrenceType)
        assertEquals(Integer.valueOf(5), data!!.frequency)
        assertFalse(data!!.getWeeklyDay(Day.SUNDAY))
        assertFalse(data!!.getWeeklyDay(Day.MONDAY))
        assertFalse(data!!.getWeeklyDay(Day.TUESDAY))
        assertFalse(data!!.getWeeklyDay(Day.WEDNESDAY))
        assertFalse(data!!.getWeeklyDay(Day.THURSDAY))
        assertTrue(data!!.getWeeklyDay(Day.FRIDAY))
        assertFalse(data!!.getWeeklyDay(Day.SATURDAY))
        assertEquals("01/01/2001", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(7), data!!.occurrences)

        exception = exceptions.get(9)
        assertEquals("Weekly 6 Saturday", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.WEEKLY, data!!.recurrenceType)
        assertEquals(Integer.valueOf(6), data!!.frequency)
        assertFalse(data!!.getWeeklyDay(Day.SUNDAY))
        assertFalse(data!!.getWeeklyDay(Day.MONDAY))
        assertFalse(data!!.getWeeklyDay(Day.TUESDAY))
        assertFalse(data!!.getWeeklyDay(Day.WEDNESDAY))
        assertFalse(data!!.getWeeklyDay(Day.THURSDAY))
        assertFalse(data!!.getWeeklyDay(Day.FRIDAY))
        assertTrue(data!!.getWeeklyDay(Day.SATURDAY))
        assertEquals("01/01/2001", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(8), data!!.occurrences)

        exception = exceptions.get(10)
        assertEquals("Weekly 7 Sunday", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.WEEKLY, data!!.recurrenceType)
        assertEquals(Integer.valueOf(7), data!!.frequency)
        assertTrue(data!!.getWeeklyDay(Day.SUNDAY))
        assertFalse(data!!.getWeeklyDay(Day.MONDAY))
        assertFalse(data!!.getWeeklyDay(Day.TUESDAY))
        assertFalse(data!!.getWeeklyDay(Day.WEDNESDAY))
        assertFalse(data!!.getWeeklyDay(Day.THURSDAY))
        assertFalse(data!!.getWeeklyDay(Day.FRIDAY))
        assertFalse(data!!.getWeeklyDay(Day.SATURDAY))
        assertEquals("01/01/2001", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(9), data!!.occurrences)

        exception = exceptions.get(11)
        assertEquals("Monthly Relative 1", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.MONTHLY, data!!.recurrenceType)
        assertTrue(data!!.relative)
        assertEquals(Integer.valueOf(1), data!!.dayNumber)
        assertEquals(Day.MONDAY, data!!.dayOfWeek)
        assertEquals(Integer.valueOf(2), data!!.frequency)
        assertEquals("01/01/2002", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(3), data!!.occurrences)

        exception = exceptions.get(12)
        assertEquals("Monthly Relative 2", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.MONTHLY, data!!.recurrenceType)
        assertTrue(data!!.relative)
        assertEquals(Integer.valueOf(2), data!!.dayNumber)
        assertEquals(Day.TUESDAY, data!!.dayOfWeek)
        assertEquals(Integer.valueOf(3), data!!.frequency)
        assertEquals("01/01/2002", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(4), data!!.occurrences)

        exception = exceptions.get(13)
        assertEquals("Monthly Relative 3", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.MONTHLY, data!!.recurrenceType)
        assertTrue(data!!.relative)
        assertEquals(Integer.valueOf(3), data!!.dayNumber)
        assertEquals(Day.WEDNESDAY, data!!.dayOfWeek)
        assertEquals(Integer.valueOf(4), data!!.frequency)
        assertEquals("01/01/2002", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(5), data!!.occurrences)

        exception = exceptions.get(14)
        assertEquals("Monthly Relative 4", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.MONTHLY, data!!.recurrenceType)
        assertTrue(data!!.relative)
        assertEquals(Integer.valueOf(4), data!!.dayNumber)
        assertEquals(Day.THURSDAY, data!!.dayOfWeek)
        assertEquals(Integer.valueOf(5), data!!.frequency)
        assertEquals("01/01/2002", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(6), data!!.occurrences)

        exception = exceptions.get(15)
        assertEquals("Monthly Relative 5", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.MONTHLY, data!!.recurrenceType)
        assertTrue(data!!.relative)
        assertEquals(Integer.valueOf(5), data!!.dayNumber)
        assertEquals(Day.FRIDAY, data!!.dayOfWeek)
        assertEquals(Integer.valueOf(6), data!!.frequency)
        assertEquals("01/01/2002", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(7), data!!.occurrences)

        exception = exceptions.get(16)
        assertEquals("Monthly Relative 6", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.MONTHLY, data!!.recurrenceType)
        assertTrue(data!!.relative)
        assertEquals(Integer.valueOf(1), data!!.dayNumber)
        assertEquals(Day.SATURDAY, data!!.dayOfWeek)
        assertEquals(Integer.valueOf(7), data!!.frequency)
        assertEquals("01/01/2002", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(8), data!!.occurrences)

        exception = exceptions.get(17)
        assertEquals("Monthly Relative 7", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.MONTHLY, data!!.recurrenceType)
        assertTrue(data!!.relative)
        assertEquals(Integer.valueOf(2), data!!.dayNumber)
        assertEquals(Day.SUNDAY, data!!.dayOfWeek)
        assertEquals(Integer.valueOf(8), data!!.frequency)
        assertEquals("01/01/2002", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(9), data!!.occurrences)

        exception = exceptions.get(18)
        assertEquals("Monthly Absolute 1", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.MONTHLY, data!!.recurrenceType)
        assertFalse(data!!.relative)
        assertEquals(Integer.valueOf(1), data!!.dayNumber)
        assertEquals(Integer.valueOf(2), data!!.frequency)
        assertEquals("01/01/2003", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(3), data!!.occurrences)

        exception = exceptions.get(19)
        assertEquals("Monthly Absolute 2", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.MONTHLY, data!!.recurrenceType)
        assertFalse(data!!.relative)
        assertEquals(Integer.valueOf(4), data!!.dayNumber)
        assertEquals(Integer.valueOf(5), data!!.frequency)
        assertEquals("01/01/2003", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(6), data!!.occurrences)

        exception = exceptions.get(20)
        assertEquals("Yearly Relative 1", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.YEARLY, data!!.recurrenceType)
        assertTrue(data!!.relative)
        assertEquals(Integer.valueOf(1), data!!.dayNumber)
        assertEquals(Day.TUESDAY, data!!.dayOfWeek)
        assertEquals(Integer.valueOf(3), data!!.monthNumber)
        assertEquals("01/01/2004", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(4), data!!.occurrences)

        exception = exceptions.get(21)
        assertEquals("Yearly Relative 2", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.YEARLY, data!!.recurrenceType)
        assertTrue(data!!.relative)
        assertEquals(Integer.valueOf(2), data!!.dayNumber)
        assertEquals(Day.WEDNESDAY, data!!.dayOfWeek)
        assertEquals(Integer.valueOf(4), data!!.monthNumber)
        assertEquals("01/01/2004", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(5), data!!.occurrences)

        exception = exceptions.get(22)
        assertEquals("Yearly Relative 3", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.YEARLY, data!!.recurrenceType)
        assertTrue(data!!.relative)
        assertEquals(Integer.valueOf(3), data!!.dayNumber)
        assertEquals(Day.THURSDAY, data!!.dayOfWeek)
        assertEquals(Integer.valueOf(5), data!!.monthNumber)
        assertEquals("01/01/2004", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(6), data!!.occurrences)

        exception = exceptions.get(23)
        assertEquals("Yearly Absolute 1", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.YEARLY, data!!.recurrenceType)
        assertFalse(data!!.relative)
        assertEquals(Integer.valueOf(1), data!!.dayNumber)
        assertEquals(Integer.valueOf(2), data!!.monthNumber)
        assertEquals("01/01/2005", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(3), data!!.occurrences)

        exception = exceptions.get(24)
        assertEquals("Yearly Absolute 2", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.YEARLY, data!!.recurrenceType)
        assertFalse(data!!.relative)
        assertEquals(Integer.valueOf(2), data!!.dayNumber)
        assertEquals(Integer.valueOf(3), data!!.monthNumber)
        assertEquals("01/01/2005", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(4), data!!.occurrences)

        exception = exceptions.get(25)
        assertEquals("Yearly Absolute 3", exception.name)
        assertFalse(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.YEARLY, data!!.recurrenceType)
        assertFalse(data!!.relative)
        assertEquals(Integer.valueOf(3), data!!.dayNumber)
        assertEquals(Integer.valueOf(4), data!!.monthNumber)
        assertEquals("01/01/2005", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(5), data!!.occurrences)

        exception = exceptions.get(26)
        assertEquals("Recurring Working", exception.name)
        assertTrue(exception.working)
        data = exception.recurring
        assertEquals(RecurrenceType.MONTHLY, data!!.recurrenceType)
        assertTrue(data!!.relative)
        assertEquals(Integer.valueOf(1), data!!.dayNumber)
        assertEquals(Day.SATURDAY, data!!.dayOfWeek)
        assertEquals(Integer.valueOf(1), data!!.frequency)
        assertEquals("01/01/2010", df.format(data!!.startDate))
        assertEquals(Integer.valueOf(3), data!!.occurrences)
    }
}
