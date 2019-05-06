/*
 * file:       TaskContainerTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       12/11/2015
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

package net.sf.mpxj.junit.project

import org.junit.Assert.*

import org.junit.Test

import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task

/**
 * Test to exercise TaskContainer functionality.
 */
class TaskContainerTest {
    /**
     * Test fix for SourceForge issue 277.
     */
    @Test
    @Throws(Exception::class)
    fun testSynchronizeTaskIDToHierarchy() {
        val file = ProjectFile()
        file.projectConfig.autoTaskID = false

        val task1 = file.addTask()
        val task2 = file.addTask()
        val task3 = task2.addTask()
        val task4 = task3.addTask()

        assertEquals(null, task1.id)
        assertEquals(null, task2.id)
        assertEquals(null, task3.id)
        assertEquals(null, task4.id)

        assertEquals(4, file.tasks.size().toLong())

        file.tasks.synchronizeTaskIDToHierarchy()

        assertEquals(4, file.tasks.size().toLong())

        assertEquals(Integer.valueOf(1), task1.id)
        assertEquals(Integer.valueOf(2), task2.id)
        assertEquals(Integer.valueOf(3), task3.id)
        assertEquals(Integer.valueOf(4), task4.id)

        assertEquals(task1, file.childTasks.get(0))
        assertEquals(task2, file.childTasks.get(1))
        assertEquals(task3, task2.childTasks.get(0))
        assertEquals(task4, task3.childTasks.get(0))
    }
}
