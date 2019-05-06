/*
 * file:       MppBaselineTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2008
 * date:       31/01/2008
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

import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Resource
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.mpp.MPPReader
import net.sf.mpxj.mspdi.MSPDIReader

import org.junit.Test

/**
 * Tests to exercise MPP file read functionality for various versions of
 * MPP file.
 */
class MppBaselineTest {

    /**
     * Test baseline data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9BaselineFields() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9baselines.mpp"))
        testBaselineFields(mpp)
    }

    /**
     * Test baseline data read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9BaselineFieldsFrom12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9baselines-from12.mpp"))
        testBaselineFields(mpp)
    }

    /**
     * Test baseline data read from an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9BaselineFieldsFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9baselines-from14.mpp"))
        testBaselineFields(mpp)
    }

    /**
     * Test baseline data read from an MSPDI file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMspdiBaselineFields() {
        val mpp = MSPDIReader().read(MpxjTestData.filePath("baselines.xml"))
        testBaselineFields(mpp)
    }

    /**
     * Test baseline data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12BaselineFields() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12baselines.mpp"))
        testBaselineFields(mpp)
    }

    /**
     * Test baseline data read from an MPP1 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12BaselineFieldsFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12baselines-from14.mpp"))
        testBaselineFields(mpp)
    }

    /**
     * Test baseline data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14BaselineFields() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14baselines.mpp"))
        testBaselineFields(mpp)
    }

    /**
     * Tests baseline fields.
     *
     * @param mpp The ProjectFile being tested.
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun testBaselineFields(mpp: ProjectFile) {
        val df = SimpleDateFormat("dd/MM/yyyy")
        val task = mpp.getTaskByID(Integer.valueOf(1))

        assertEquals(1, task.getBaselineCost(1).intValue())
        assertEquals(2, task.getBaselineCost(2).intValue())
        assertEquals(3, task.getBaselineCost(3).intValue())
        assertEquals(4, task.getBaselineCost(4).intValue())
        assertEquals(5, task.getBaselineCost(5).intValue())
        assertEquals(6, task.getBaselineCost(6).intValue())
        assertEquals(7, task.getBaselineCost(7).intValue())
        assertEquals(8, task.getBaselineCost(8).intValue())
        assertEquals(9, task.getBaselineCost(9).intValue())
        assertEquals(10, task.getBaselineCost(10).intValue())

        assertEquals(1, (task.getBaselineDuration(1).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, task.getBaselineDuration(1).getUnits())
        assertEquals(2, (task.getBaselineDuration(2).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, task.getBaselineDuration(2).getUnits())
        assertEquals(3, (task.getBaselineDuration(3).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, task.getBaselineDuration(3).getUnits())
        assertEquals(4, (task.getBaselineDuration(4).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, task.getBaselineDuration(4).getUnits())
        assertEquals(5, (task.getBaselineDuration(5).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, task.getBaselineDuration(5).getUnits())
        assertEquals(6, (task.getBaselineDuration(6).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, task.getBaselineDuration(6).getUnits())
        assertEquals(7, (task.getBaselineDuration(7).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, task.getBaselineDuration(7).getUnits())
        assertEquals(8, (task.getBaselineDuration(8).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, task.getBaselineDuration(8).getUnits())
        assertEquals(9, (task.getBaselineDuration(9).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, task.getBaselineDuration(9).getUnits())
        assertEquals(10, (task.getBaselineDuration(10).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, task.getBaselineDuration(10).getUnits())

        assertEquals("01/01/2000", df.format(task.getBaselineFinish(1)))
        assertEquals("02/01/2000", df.format(task.getBaselineFinish(2)))
        assertEquals("03/01/2000", df.format(task.getBaselineFinish(3)))
        assertEquals("04/01/2000", df.format(task.getBaselineFinish(4)))
        assertEquals("05/01/2000", df.format(task.getBaselineFinish(5)))
        assertEquals("06/01/2000", df.format(task.getBaselineFinish(6)))
        assertEquals("07/01/2000", df.format(task.getBaselineFinish(7)))
        assertEquals("08/01/2000", df.format(task.getBaselineFinish(8)))
        assertEquals("09/01/2000", df.format(task.getBaselineFinish(9)))
        assertEquals("10/01/2000", df.format(task.getBaselineFinish(10)))

        assertEquals("01/01/2001", df.format(task.getBaselineStart(1)))
        assertEquals("02/01/2001", df.format(task.getBaselineStart(2)))
        assertEquals("03/01/2001", df.format(task.getBaselineStart(3)))
        assertEquals("04/01/2001", df.format(task.getBaselineStart(4)))
        assertEquals("05/01/2001", df.format(task.getBaselineStart(5)))
        assertEquals("06/01/2001", df.format(task.getBaselineStart(6)))
        assertEquals("07/01/2001", df.format(task.getBaselineStart(7)))
        assertEquals("08/01/2001", df.format(task.getBaselineStart(8)))
        assertEquals("09/01/2001", df.format(task.getBaselineStart(9)))
        assertEquals("10/01/2001", df.format(task.getBaselineStart(10)))

        assertEquals(1, (task.getBaselineWork(1).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, task.getBaselineWork(1).getUnits())
        assertEquals(2, (task.getBaselineWork(2).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, task.getBaselineWork(2).getUnits())
        assertEquals(3, (task.getBaselineWork(3).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, task.getBaselineWork(3).getUnits())
        assertEquals(4, (task.getBaselineWork(4).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, task.getBaselineWork(4).getUnits())
        assertEquals(5, (task.getBaselineWork(5).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, task.getBaselineWork(5).getUnits())
        assertEquals(6, (task.getBaselineWork(6).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, task.getBaselineWork(6).getUnits())
        assertEquals(7, (task.getBaselineWork(7).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, task.getBaselineWork(7).getUnits())
        assertEquals(8, (task.getBaselineWork(8).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, task.getBaselineWork(8).getUnits())
        assertEquals(9, (task.getBaselineWork(9).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, task.getBaselineWork(9).getUnits())
        assertEquals(10, (task.getBaselineWork(10).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, task.getBaselineWork(10).getUnits())

        val resource = mpp.getResourceByID(Integer.valueOf(1))

        assertEquals(1, (resource.getBaselineWork(1).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, resource.getBaselineWork(1).getUnits())
        assertEquals(2, (resource.getBaselineWork(2).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, resource.getBaselineWork(2).getUnits())
        assertEquals(3, (resource.getBaselineWork(3).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, resource.getBaselineWork(3).getUnits())
        assertEquals(4, (resource.getBaselineWork(4).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, resource.getBaselineWork(4).getUnits())
        assertEquals(5, (resource.getBaselineWork(5).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, resource.getBaselineWork(5).getUnits())
        assertEquals(6, (resource.getBaselineWork(6).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, resource.getBaselineWork(6).getUnits())
        assertEquals(7, (resource.getBaselineWork(7).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, resource.getBaselineWork(7).getUnits())
        assertEquals(8, (resource.getBaselineWork(8).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, resource.getBaselineWork(8).getUnits())
        assertEquals(9, (resource.getBaselineWork(9).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, resource.getBaselineWork(9).getUnits())
        assertEquals(10, (resource.getBaselineWork(10).getDuration() as Int).toLong())
        assertEquals(TimeUnit.HOURS, resource.getBaselineWork(10).getUnits())

        assertEquals(1, resource.getBaselineCost(1).intValue())
        assertEquals(2, resource.getBaselineCost(2).intValue())
        assertEquals(3, resource.getBaselineCost(3).intValue())
        assertEquals(4, resource.getBaselineCost(4).intValue())
        assertEquals(5, resource.getBaselineCost(5).intValue())
        assertEquals(6, resource.getBaselineCost(6).intValue())
        assertEquals(7, resource.getBaselineCost(7).intValue())
        assertEquals(8, resource.getBaselineCost(8).intValue())
        assertEquals(9, resource.getBaselineCost(9).intValue())
        assertEquals(10, resource.getBaselineCost(10).intValue())
    }
}
