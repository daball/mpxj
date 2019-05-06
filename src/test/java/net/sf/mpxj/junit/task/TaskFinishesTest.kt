/*
 * file:       TaskFinishesTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       03/11/2014
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
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

import org.junit.Test

import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpx.MPXReader
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.ProjectReaderUtility

/**
 * Tests to ensure task custom finish dates are correctly handled.
 */
class TaskFinishesTest {

    private val m_dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
    private val m_dateFormat = SimpleDateFormat("dd/MM/yyyy")
    /**
     * Test to validate the custom finish dates in files saved by different versions of MS Project.
     */
    @Test
    @Throws(Exception::class)
    fun testTaskFinishDates() {
        for (file in MpxjTestData.listFiles("generated/task-finishes", "task-finishes")) {
            testTaskFinishDates(file)
        }
    }

    /**
     * Test an individual project.
     *
     * @param file project file
     */
    @Throws(Exception::class)
    private fun testTaskFinishDates(file: File) {
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        if (reader is MPDDatabaseReader) {
            assumeJvm()
        }

        val isMpxFile = reader is MPXReader
        val maxIndex = if (isMpxFile) 5 else 10
        val project = reader.read(file)
        for (index in 1..maxIndex) {
            val task = project.getTaskByID(Integer.valueOf(index))
            assertEquals("Finish$index", task.name)
            testTaskFinishDates(file, task, index, maxIndex, isMpxFile)
        }
    }

    /**
     * Test the finish date values for a task.
     *
     * @param file parent file
     * @param task task
     * @param testIndex index of number being tested
     * @param maxIndex highest index to test
     * @param useDateFormat true=use date-only format false=use date time format
     */
    @Throws(ParseException::class)
    private fun testTaskFinishDates(file: File, task: Task, testIndex: Int, maxIndex: Int, useDateFormat: Boolean) {
        val format = if (useDateFormat) m_dateFormat else m_dateTimeFormat
        for (index in 1..maxIndex) {
            val expectedValue = if (testIndex == index) format.parse(DATES[index - 1]) else null
            val actualValue = task.getFinish(index)

            assertEquals(file.getName() + " Finish" + index, expectedValue, actualValue)
        }
    }

    companion object {

        private val DATES = arrayOf("01/01/2014 09:00", "02/01/2014 10:00", "03/01/2014 11:00", "04/01/2014 12:00", "05/01/2014 13:00", "06/01/2014 14:00", "07/01/2014 15:00", "08/01/2014 16:00", "09/01/2014 17:00", "10/01/2014 18:00")
    }
}
