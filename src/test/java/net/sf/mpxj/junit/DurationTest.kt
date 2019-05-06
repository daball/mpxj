/*
 * file:       DurationTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2009
 * date:       25/03/2009
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
import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpp.MPPReader
import net.sf.mpxj.mspdi.MSPDIReader

import org.junit.Test

/**
 * Tests to exercise reading duration values.
 */
class DurationTest {
    /**
     * Test duration data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9duration.mpp"))
        testDurations(mpp)
    }

    /**
     * Test duration data read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9From12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9duration-from12.mpp"))
        testDurations(mpp)
    }

    /**
     * Test duration data read from an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9From14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9duration-from14.mpp"))
        testDurations(mpp)
    }

    /**
     * Test duration data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12duration.mpp"))
        testDurations(mpp)
    }

    /**
     * Test duration data read from an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12From14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12duration-from14.mpp"))
        testDurations(mpp)
    }

    /**
     * Test duration data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14duration.mpp"))
        testDurations(mpp)
    }

    /**
     * Test duration data read from an MSPDI file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMspdi() {
        val mpp = MSPDIReader().read(MpxjTestData.filePath("mspdiduration.xml"))
        testDurations(mpp)
    }

    /**
     * Test duration data read from an MPD file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpd() {
        assumeJvm()
        val mpp = MPDDatabaseReader().read(MpxjTestData.filePath("mpdduration.mpd"))
        testDurations(mpp)
    }

    /**
     * Validates duration values.
     *
     * @param mpp project file
     */
    private fun testDurations(mpp: ProjectFile) {
        var task = mpp.getTaskByID(Integer.valueOf(1))
        assertEquals(Duration.getInstance(1, TimeUnit.MINUTES), task.duration)

        task = mpp.getTaskByID(Integer.valueOf(2))
        assertEquals(Duration.getInstance(1, TimeUnit.HOURS), task.duration)

        task = mpp.getTaskByID(Integer.valueOf(3))
        assertEquals(Duration.getInstance(1, TimeUnit.DAYS), task.duration)

        task = mpp.getTaskByID(Integer.valueOf(4))
        assertEquals(Duration.getInstance(1, TimeUnit.WEEKS), task.duration)

        task = mpp.getTaskByID(Integer.valueOf(5))
        assertEquals(Duration.getInstance(1, TimeUnit.MONTHS), task.duration)

        task = mpp.getTaskByID(Integer.valueOf(6))
        assertEquals(Duration.getInstance(1, TimeUnit.ELAPSED_MINUTES), task.duration)

        task = mpp.getTaskByID(Integer.valueOf(7))
        assertEquals(Duration.getInstance(1, TimeUnit.ELAPSED_HOURS), task.duration)

        task = mpp.getTaskByID(Integer.valueOf(8))
        assertEquals(Duration.getInstance(1, TimeUnit.ELAPSED_DAYS), task.duration)

        task = mpp.getTaskByID(Integer.valueOf(9))
        assertEquals(Duration.getInstance(1, TimeUnit.ELAPSED_WEEKS), task.duration)

        task = mpp.getTaskByID(Integer.valueOf(10))
        assertEquals(Duration.getInstance(1, TimeUnit.ELAPSED_MONTHS), task.duration)
    }

}
