/*
 * file:       TaskLinksTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       20/10/2014
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

import net.sf.mpxj.junit.MpxjAssert.*
import org.junit.Assert.*

import java.io.File

import org.junit.Test

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.ProjectReaderUtility

/**
 * Tests to ensure task links are correctly handled.
 */
class TaskLinksTest {
    /**
     * Test to validate links between tasks in files saved by different versions of MS Project.
     */
    @Test
    @Throws(MPXJException::class)
    fun testTaskLinks() {
        for (file in MpxjTestData.listFiles("generated/task-links", "task-links")) {
            testTaskLinks(file)
        }
    }

    /**
     * Test an individual project.
     *
     * @param file project file
     */
    @Throws(MPXJException::class)
    private fun testTaskLinks(file: File) {
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        if (reader is MPDDatabaseReader) {
            assumeJvm()
        }

        val project = reader.read(file)

        //
        // Test different durations and time units
        //
        testTaskLinks(project, 1, 2, "Task 1", "Task 2", RelationType.FINISH_START, 0.0, TimeUnit.DAYS)
        testTaskLinks(project, 3, 4, "Task 1", "Task 2", RelationType.FINISH_START, 1.0, TimeUnit.DAYS)
        testTaskLinks(project, 5, 6, "Task 1", "Task 2", RelationType.FINISH_START, 2.0, TimeUnit.DAYS)
        testTaskLinks(project, 7, 8, "Task 1", "Task 2", RelationType.FINISH_START, 1.0, TimeUnit.WEEKS)
        testTaskLinks(project, 9, 10, "Task 1", "Task 2", RelationType.FINISH_START, 2.0, TimeUnit.WEEKS)

        //
        // Test different relation types
        //
        testTaskLinks(project, 11, 12, "Task 1", "Task 2", RelationType.START_FINISH, 2.0, TimeUnit.DAYS)
        testTaskLinks(project, 13, 14, "Task 1", "Task 2", RelationType.START_START, 2.0, TimeUnit.DAYS)
        testTaskLinks(project, 15, 16, "Task 1", "Task 2", RelationType.FINISH_FINISH, 2.0, TimeUnit.DAYS)
    }

    /**
     * Test a relationship between two tasks.
     *
     * @param project parent project
     * @param taskID1 first task
     * @param taskID2 second task
     * @param name1 expected task name 1
     * @param name2 expected task name 1
     * @param type expected relation type
     * @param lagDuration expected lag duration
     * @param lagUnits expected lag units
     */
    private fun testTaskLinks(project: ProjectFile, taskID1: Int, taskID2: Int, name1: String, name2: String, type: RelationType, lagDuration: Double, lagUnits: TimeUnit) {
        val task1 = project.getTaskByID(Integer.valueOf(taskID1))
        val task2 = project.getTaskByID(Integer.valueOf(taskID2))

        assertEquals(name1, task1.name)
        assertEquals(name2, task2.name)

        val relations = task2.predecessors
        assertEquals(1, relations.size())
        val relation = relations.get(0)
        assertEquals(task2, relation.sourceTask)
        assertEquals(task1, relation.targetTask)
        assertEquals(type, relation.type)
        assertEquals(lagUnits, relation.lag!!.getUnits())
        assertTrue(NumberHelper.equals(lagDuration, relation.lag!!.getDuration(), 0.0001))
    }
}
