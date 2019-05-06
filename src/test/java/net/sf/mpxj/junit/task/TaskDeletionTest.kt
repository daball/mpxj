/*
 * file:       TaskDeletionTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       11/11/2014
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

package net.sf.mpxj.junit.task

import org.junit.Assert.*

import java.io.File

import org.junit.Test

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.ProjectReaderUtility

/**
 * Tests to ensure deleted tasks, both blank and normal, are handled correctly.
 *
 * Test Data Generation:
 * 1. Create an MPP file with the following tasks:
 * 1. blank
 * 2. T1
 * 3. blank
 * 4. T2
 * 5. T3
 * 6. T4
 * 7. blank
 * 8. blank
 * 9. T5
 * 2. Save this file in the required format
 * 3. Copy the file
 * 4. Open the copy and delete tasks with IDs 1,3,5,7
 * 5. Save the file
 */
class TaskDeletionTest {
    /**
     * Ensure that we can see the correct pre-deletion tasks.
     */
    @Test
    @Throws(MPXJException::class)
    fun testTasksPreDeletion() {
        for (file in MpxjTestData.listFiles("task/task-deletion", "task-deletion1")) {
            testTaskDeletion(file, TASK_DELETION1)
        }
    }

    /**
     * Ensure that we can see the correct post-deletion tasks.
     */
    @Test
    @Throws(MPXJException::class)
    fun testTasksPostDeletion() {
        for (file in MpxjTestData.listFiles("task/task-deletion", "task-deletion2")) {
            testTaskDeletion(file, TASK_DELETION2)
        }
    }

    /**
     * Test an individual project.
     *
     * @param file project file
     * @param expectedNames expected task names
     */
    @Throws(MPXJException::class)
    private fun testTaskDeletion(file: File, expectedNames: Array<String>) {
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        val project = reader.read(file)
        assertEquals((expectedNames.size + 1).toLong(), project.getTasks().size().toLong())
        for (index in expectedNames.indices) {
            val task = project.getTaskByID(Integer.valueOf(index + 1))
            assertNotNull(file.getName() + " Task " + (index + 1), task)
            assertEquals(file.getName() + " Task " + task.id, expectedNames[index], task.name)
        }
    }

    companion object {
        private val TASK_DELETION1 = arrayOf<String>(null, "T1", null, "T2", "T3", "T4", null, null, "T5")

        private val TASK_DELETION2 = arrayOf<String>("T1", "T2", "T4", null, "T5")
    }
}
