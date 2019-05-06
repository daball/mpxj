/*
 * file:       CalendarCalendarsTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2015
 * date:       29/04/2015
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

import org.junit.Test

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectCalendarContainer
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.ProjectReaderUtility

/**
 * Tests to ensure basic calendar details are read correctly.
 */
class CalendarCalendarsTest {
    /**
     * Test to validate calendars in files saved by different versions of MS Project.
     */
    @Test
    @Throws(MPXJException::class)
    fun testCalendars() {
        for (file in MpxjTestData.listFiles("generated/calendar-calendars", "calendar-calendars")) {
            testCalendars(file)
        }
    }

    /**
     * Test an individual project.
     *
     * @param file project file
     */
    @Throws(MPXJException::class)
    private fun testCalendars(file: File) {
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        if (reader is MPDDatabaseReader) {
            assumeJvm()
        }

        val project = reader.read(file)
        val calendars = project.calendars

        var id = 1
        assertEquals("Standard", calendars.getByUniqueID(Integer.valueOf(id++)).name)

        if (!file.getName().endsWith(".mpx")) {
            id++
        }

        assertEquals("Calendar1", calendars.getByUniqueID(Integer.valueOf(id++)).name)
        assertEquals("Calendar2", calendars.getByUniqueID(Integer.valueOf(id++)).name)
        assertEquals("Resource One", calendars.getByUniqueID(Integer.valueOf(id++)).name)
        assertEquals("Resource Two", calendars.getByUniqueID(Integer.valueOf(id++)).name)
    }
}
