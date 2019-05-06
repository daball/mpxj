/*
 * file:       MppViewStateTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       9-January-2007
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

import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ViewState
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * Tests to exercise MPP file read functionality for various versions of
 * MPP file.
 */
class MppViewStateTest {

    /**
     * Test view state data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9ViewState() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9viewstate.mpp"))
        testViewState(mpp)
    }

    /**
     * Test view state data read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9ViewStateFrom12() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9viewstate-from12.mpp"))
        testViewState(mpp)
    }

    /**
     * Test view state data read from an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9ViewStateFrom14() {
        //ProjectFile mpp = new MPPReader().read(MpxjTestData.filePath("mpp9viewstate-from14.mpp"));
        //testViewState(mpp);
    }

    /**
     * Test view state data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12ViewState() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12viewstate.mpp"))
        testViewState(mpp)
    }

    /**
     * Test view state data read from an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12ViewStateFrom14() {
        //ProjectFile mpp = new MPPReader().read(MpxjTestData.filePath("mpp12viewstate-from14.mpp"));
        //testViewState(mpp);
    }

    /**
     * Test view state data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14ViewState() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14viewstate.mpp"))
        testViewState(mpp)
    }

    /**
     * Test view state.
     *
     * @param mpp ProjectFile instance
     */
    private fun testViewState(mpp: ProjectFile) {
        val state = mpp.views.getViewState()
        assertNotNull(state)

        assertEquals("Gantt Chart", state.getViewName())

        val list = state.getUniqueIdList()
        assertEquals(50, list.size())

        for (loop in UNIQUE_ID_LIST.indices) {
            assertEquals(UNIQUE_ID_LIST[loop], list.get(loop).intValue())
        }
    }

    companion object {

        private val UNIQUE_ID_LIST = intArrayOf(5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54)
    }
}
