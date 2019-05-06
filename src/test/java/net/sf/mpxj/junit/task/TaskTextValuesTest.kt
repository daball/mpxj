/*
 * file:       TaskTextValuesTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       14/11/2014
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
 * Tests to ensure the text versions of Start, Finish and Duration are read correctly.
 */
class TaskTextValuesTest {
    /**
     * Tests to ensure the text versions of Start, Finish and Duration are read correctly.
     */
    @Test
    @Throws(MPXJException::class)
    fun testTaskTextValues() {
        for (file in MpxjTestData.listFiles("generated/task-textvalues", "task-textvalues")) {
            testTaskTextValues(file)
        }
    }

    /**
     * Test an individual project.
     *
     * @param file project file
     */
    @Throws(MPXJException::class)
    private fun testTaskTextValues(file: File) {
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        val project = reader.read(file)
        assertEquals(file.getName() + " number of tasks", (EXPECTED_VALUES.size + 1).toLong(), project.getTasks().size().toLong())
        for (loop in EXPECTED_VALUES.indices) {
            val task = project.getTaskByID(Integer.valueOf(loop + 1))
            assertEquals(file.getName() + " task name", EXPECTED_VALUES[loop][0], task.name)
            assertEquals(file.getName() + " start text", EXPECTED_VALUES[loop][1], task.startText)
            assertEquals(file.getName() + " finish text", EXPECTED_VALUES[loop][2], task.finishText)
            assertEquals(file.getName() + " duration text", EXPECTED_VALUES[loop][3], task.durationText)
        }
    }

    companion object {

        private val EXPECTED_VALUES = arrayOf(arrayOf("Start is text", "AAA", "", ""), arrayOf("Finish is text", "", "BBB", ""), arrayOf("Duration is text", "", "", "CCC"))
    }
}
