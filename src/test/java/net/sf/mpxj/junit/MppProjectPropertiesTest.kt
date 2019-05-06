/*
 * file:       MppProjectPropertiesTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       23-August-2006
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

import net.sf.mpxj.CurrencySymbolPosition
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.ScheduleFrom
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * Test reading project properties from MPP files.
 */
class MppProjectPropertiesTest {
    /**
     * Test project properties read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9header.mpp"))
        testProperties(mpp, true)
    }

    /**
     * Test project properties read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9From12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9header-from12.mpp"))
        testProperties(mpp, true)
    }

    /**
     * Test project properties read from an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9From14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9header-from14.mpp"))
        testProperties(mpp, true)
    }

    /**
     * Test project properties read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12header.mpp"))
        testProperties(mpp, true)
    }

    /**
     * Test project properties read from an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12From14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12header-from14.mpp"))
        testProperties(mpp, true)
    }

    /**
     * Test project properties read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14header.mpp"))
        testProperties(mpp, true)
    }

    /**
     * Test project properties read from an MPD9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpd9() {
        assumeJvm()
        val mpp = MPDDatabaseReader().read(MpxjTestData.filePath("mpp9header.mpd"))
        testProperties(mpp, false)
    }

    /**
     * Test the project properties as read from an MPP file.
     *
     * @param mpp project file
     * @param isMPP is the source an MPP file
     */
    private fun testProperties(mpp: ProjectFile, isMPP: Boolean) {
        //
        // Create time and date formatters
        //
        val tf = SimpleDateFormat("HH:mm")
        val df = SimpleDateFormat("dd/MM/yyyy")

        //
        // Check the values of project properties.
        // The order of these tests should be the same as the order
        // in which the attributes are read from the MPP file
        // for ease of reference.
        //
        val ph = mpp.projectProperties
        assertEquals(ScheduleFrom.FINISH, ph.scheduleFrom)
        assertEquals("24 Hours", ph.defaultCalendarName)
        assertEquals("08:35", tf.format(ph.defaultStartTime))
        assertEquals("17:35", tf.format(ph.defaultEndTime))
        assertEquals("01/08/2006", df.format(ph.statusDate))

        assertEquals(TimeUnit.HOURS, ph.defaultDurationUnits)
        assertEquals(7 * 60, ph.minutesPerDay.intValue())
        assertEquals(41 * 60, ph.minutesPerWeek.intValue())
        assertEquals(2.0, ph.defaultOvertimeRate.amount, 0.0)
        assertEquals(TimeUnit.HOURS, ph.defaultOvertimeRate.units)
        assertEquals(1.0, ph.defaultStandardRate.amount, 0.0)
        assertEquals(TimeUnit.HOURS, ph.defaultStandardRate.units)
        assertEquals(TimeUnit.WEEKS, ph.defaultWorkUnits)
        assertFalse(ph.splitInProgressTasks)
        assertFalse(ph.updatingTaskStatusUpdatesResourceStatus)

        assertEquals(1, ph.currencyDigits.intValue())
        assertEquals("X", ph.currencySymbol)
        assertEquals(CurrencySymbolPosition.AFTER, ph.symbolPosition)

        assertEquals("title", ph.projectTitle)
        assertEquals("subject", ph.subject)
        assertEquals("author", ph.author)
        assertEquals("keywords", ph.keywords)
        assertEquals("company", ph.company)
        assertEquals("manager", ph.manager)
        assertEquals("category", ph.category)

        // MPP only
        if (isMPP) {
            assertEquals("comments", ph.comments)
            assertTrue(ph.calculateMultipleCriticalPaths)
        }
    }
}
