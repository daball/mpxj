/*
 * file:       SlackTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       1-April-2006
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
import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * The tests contained in this class exercise the slack duration functionality.
 */
class SlackTest {
    /**
     * Exercise slack duration functionality.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testSlack() {
        val mpp = MPPReader().read(MpxjTestData.filePath("slack9.mpp"))
        var task = mpp.getTaskByID(Integer.valueOf(1))
        assertEquals("Task 1", task.name)
        assertEquals(Duration.getInstance(8, TimeUnit.HOURS), task.duration)
        assertEquals(Duration.getInstance(40, TimeUnit.HOURS), task.startSlack)
        assertEquals(Duration.getInstance(40, TimeUnit.HOURS), task.finishSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.HOURS), task.freeSlack)
        assertEquals(Duration.getInstance(40, TimeUnit.HOURS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(2))
        assertEquals("Task 2", task.name)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(3))
        assertEquals("Task 3", task.name)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(10, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(10, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(10, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(4))
        assertEquals("Task 4", task.name)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(5))
        assertEquals("Milestone 1", task.name)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(6))
        assertEquals("Task 5", task.name)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(-1, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(-1, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(-1, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(7))
        assertEquals("Task 6", task.name)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(-1, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(-1, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(-1, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(8))
        assertEquals("Task 7", task.name)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(-1, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(-1, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(-1, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(9))
        assertEquals("Task 8", task.name)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(4, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(4, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(4, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(4, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(10))
        assertEquals("Milestone 2", task.name)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(-1, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(-1, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(11))
        assertEquals("Task 9", task.name)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(12))
        assertEquals("Task 10", task.name)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(13))
        assertEquals("Task 11", task.name)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(14))
        assertEquals("Task 12", task.name)
        assertEquals(Duration.getInstance(5, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(6, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(6, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(6, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(6, TimeUnit.DAYS), task.totalSlack)

        task = mpp.getTaskByID(Integer.valueOf(15))
        assertEquals("Milestone 3", task.name)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.duration)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.startSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.finishSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.freeSlack)
        assertEquals(Duration.getInstance(0, TimeUnit.DAYS), task.totalSlack)

    }
}
