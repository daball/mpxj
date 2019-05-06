/*
 * file:       DeletedAssignmentTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       20/09/2014
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

package net.sf.mpxj.junit.assignment

import org.junit.Assert.*

import java.io.File

import org.junit.Test

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpp.MPPReader

/**
 * Tests to ensure delete resource assignments are correctly handled.
 */
class DeletedAssignmentTest {
    /**
     * Test to exercise the test case provided for SourceForge bug #248.
     * https://sourceforge.net/p/mpxj/bugs/248/
     */
    @Test
    @Throws(Exception::class)
    fun testSourceForge248() {
        val file = MPPReader().read(MpxjTestData.filePath("assignment/assignment-deletion/sf248.mpp"))

        val assignments = file.resourceAssignments
        assertEquals(2, assignments.size())
        testAssignment(assignments, 0, "Task2", "Vijay")
        testAssignment(assignments, 1, "Task1", "Anil")
    }

    /**
     * This test relates to SourceForge bug #248, where it appears that MPXJ was reading deleted
     * resource assignments.
     *
     * 1. Create a file in the appropriate format with 10 resource assignments
     * 2. Save to a new name
     * 3. Delete every other assignment (2,4,6,...)
     * 4. Save again
     *
     * These steps should ensure that MS Project doesn't rewrite the whole file
     * (which it probably would when doing a "save as..."), and hence preserves the deleted assignments.
     */
    @Test
    @Throws(Exception::class)
    fun testDeletedResourceAssignments() {
        for (file in MpxjTestData.listFiles("assignment/assignment-deletion", "deleted-resource-assignments")) {
            testDeletedResourceAssignments(file)
        }
    }

    /**
     * Test a project file to ensure that deleted resource assignments are not included.
     *
     * @param file project file to test
     */
    @Throws(MPXJException::class)
    private fun testDeletedResourceAssignments(file: File) {
        val reader = MPPReader()
        val mpp = reader.read(file)
        val assignments = mpp.getResourceAssignments()
        assertEquals(file.getName() + " does not contain 5 resource assignments", 5, assignments.size())

        testAssignment(assignments, 0, "Task 1", "Resource 1")
        testAssignment(assignments, 1, "Task 1", "Resource 3")
        testAssignment(assignments, 2, "Task 1", "Resource 5")
        testAssignment(assignments, 3, "Task 1", "Resource 7")
        testAssignment(assignments, 4, "Task 1", "Resource 9")
    }

    /**
     * Validate that a resource assignment task and resource names match a given value.
     *
     * @param assignments list of assignments
     * @param index index number of the resource to test
     * @param expectedTaskName expected task name
     * @param expectedResourceName expected resource name
     */
    private fun testAssignment(assignments: List<ResourceAssignment>, index: Int, expectedTaskName: String, expectedResourceName: String) {
        val assignment = assignments[index]
        assertEquals(expectedTaskName, assignment.task!!.name)
        assertEquals(expectedResourceName, assignment.resource!!.name)
    }
}
