/*
 * file:       MppFilterLogicTest.java
 * author:     James Styles
 * copyright:  (c) Packwood Software 2009
 * date:       19/05/2009
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

import net.sf.mpxj.Filter
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * Tests to exercise filter logic.
 */
class MppFilterLogicTest {
    /**
     * Exercise an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9FilterLogic() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9filterlogic.mpp"))
        testFilterLogic(mpp)
    }

    /**
     * Exercise an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9FilterLogicFrom12() {
        //ProjectFile mpp = new MPPReader().read(MpxjTestData.filePath("mpp9filterlogic-from12.mpp"));
        //testFilterLogic(mpp);
    }

    /**
     * Exercise an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9FilterLogicFrom14() {
        //ProjectFile mpp = new MPPReader().read(MpxjTestData.filePath("mpp9filterlogic-from14.mpp"));
        //testFilterLogic(mpp);
    }

    /**
     * Exercise an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12FilterLogic() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12filterlogic.mpp"))
        testFilterLogic(mpp)
    }

    /**
     * Exercise an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12FilterLogicFrom14() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12filterlogic-from14.mpp"))
        testFilterLogic(mpp)
    }

    /**
     * Exercise an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14FilterLogic() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14filterlogic.mpp"))
        testFilterLogic(mpp)
    }

    /**
     * Common filter logic tests.
     *
     * @param mpp project file
     * @throws Exception
     */
    private fun testFilterLogic(mpp: ProjectFile) {
        val listAllTasks = mpp.tasks
        val ac1 = listAllTasks.get(1)
        assertEquals("ac1", ac1.name)

        val ac2 = listAllTasks.get(2)
        assertEquals("ac2", ac2.name)

        /*
       * InBlockAnd use:
       *    name equals ac1
       *    AND name equals ac1
       *    OR name equals ac2
       *
       * MSP evaluates this to includes both ac1 and ac2
       */
        val inBlockAndFilter = mpp.filters.getFilterByName("InBlockAnd")

        assertTrue(inBlockAndFilter.evaluate(ac1, null))
        assertTrue(inBlockAndFilter.evaluate(ac2, null))

        /*
       * BetweenBlockAnd use:
       *    name equals ac1
       *
       *    AND
       *
       *    name equals ac1
       *    OR name equals ac2
       *
       * MSP evaluates this to only include ac1
       */
        val betweenBlockAndFilter = mpp.filters.getFilterByName("BetweenBlockAnd")
        assertTrue(betweenBlockAndFilter.evaluate(ac1, null))
        assertFalse(betweenBlockAndFilter.evaluate(ac2, null))
    }
}
