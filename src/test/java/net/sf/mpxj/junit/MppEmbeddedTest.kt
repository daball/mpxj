/*
 * file:        MppEmbeddedTest.java
 * author:      Jon Iles
 * copyright:   (c) Packwood Software 2008
 * date:        15/03/2008
 */

/*
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */

package net.sf.mpxj.junit

import org.junit.Assert.*

import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Task
import net.sf.mpxj.mpp.MPPReader
import net.sf.mpxj.mpp.RTFEmbeddedObject

import org.junit.Test

/**
 * Test to handle MPP file content embedded in note fields.
 */
class MppEmbeddedTest {
    /**
     * Test MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9Embedded() {
        val reader = MPPReader()
        reader.preserveNoteFormatting = true
        val mpp = reader.read(MpxjTestData.filePath("mpp9embedded.mpp"))
        testEmbeddedObjects(mpp)
    }

    /**
     * Test MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9From12Embedded() {
        val reader = MPPReader()
        reader.preserveNoteFormatting = true
        val mpp = reader.read(MpxjTestData.filePath("mpp9embedded-from12.mpp"))
        testEmbeddedObjects(mpp)
    }

    /**
     * Test MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9From14Embedded() {
        val reader = MPPReader()
        reader.preserveNoteFormatting = true
        val mpp = reader.read(MpxjTestData.filePath("mpp9embedded-from14.mpp"))
        testEmbeddedObjects(mpp)
    }

    /**
     * Test MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12Embedded() {
        val reader = MPPReader()
        reader.preserveNoteFormatting = true
        val mpp = reader.read(MpxjTestData.filePath("mpp12embedded.mpp"))
        testEmbeddedObjects(mpp)
    }

    /**
     * Test MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12From14Embedded() {
        val reader = MPPReader()
        reader.preserveNoteFormatting = true
        val mpp = reader.read(MpxjTestData.filePath("mpp12embedded-from14.mpp"))
        testEmbeddedObjects(mpp)
    }

    /**
     * Test MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14Embedded() {
        val reader = MPPReader()
        reader.preserveNoteFormatting = true
        val mpp = reader.read(MpxjTestData.filePath("mpp14embedded.mpp"))
        testEmbeddedObjects(mpp)
    }

    /**
     * Tests common to all file types.
     *
     * @param file project file
     */
    private fun testEmbeddedObjects(file: ProjectFile) {
        var task = file.getTaskByID(Integer.valueOf(1))
        assertEquals("Task 1", task.name)
        var notes = task.notes
        assertNotNull(notes)
        var list = RTFEmbeddedObject.getEmbeddedObjects(notes)
        assertNull(list)

        task = file.getTaskByID(Integer.valueOf(2))
        assertEquals("Task 2", task.name)
        notes = task.notes
        assertNotNull(notes)
        list = RTFEmbeddedObject.getEmbeddedObjects(notes)
        assertNotNull(list)
        assertEquals(1, list!!.size())
        var objectList = list.get(0)
        assertEquals(4, objectList.size())
        assertEquals("Package", String(objectList.get(0).getData(), 0, 7))
        assertEquals("METAFILEPICT", String(objectList.get(2).getData(), 0, 12))

        task = file.getTaskByID(Integer.valueOf(3))
        assertEquals("Task 3", task.name)
        notes = task.notes
        assertNotNull(notes)
        list = RTFEmbeddedObject.getEmbeddedObjects(notes)
        assertNotNull(list)
        assertEquals(1, list!!.size())
        objectList = list.get(0)
        assertEquals(4, objectList.size())
        assertEquals("Package", String(objectList.get(0).getData(), 0, 7))
        assertEquals("METAFILEPICT", String(objectList.get(2).getData(), 0, 12))

        task = file.getTaskByID(Integer.valueOf(4))
        assertEquals("Task 4", task.name)
        notes = task.notes
        assertNotNull(notes)
        list = RTFEmbeddedObject.getEmbeddedObjects(notes)
        assertNotNull(list)
        assertEquals(1, list!!.size())
        objectList = list.get(0)
        assertEquals(4, objectList.size())
        assertEquals("Package", String(objectList.get(0).getData(), 0, 7))
        assertEquals("METAFILEPICT", String(objectList.get(2).getData(), 0, 12))

        task = file.getTaskByID(Integer.valueOf(5))
        assertEquals("Task 5", task.name)
        notes = task.notes
        assertNotNull(notes)
        list = RTFEmbeddedObject.getEmbeddedObjects(notes)
        assertNotNull(list)
        assertEquals(1, list!!.size())
        objectList = list.get(0)
        assertEquals(4, objectList.size())
        assertEquals("Package", String(objectList.get(0).getData(), 0, 7))
        assertEquals("METAFILEPICT", String(objectList.get(2).getData(), 0, 12))

        task = file.getTaskByID(Integer.valueOf(6))
        assertEquals("Task 6", task.name)
        notes = task.notes
        assertNotNull(notes)
        list = RTFEmbeddedObject.getEmbeddedObjects(notes)
        assertNotNull(list)
        assertEquals(2, list!!.size())
        objectList = list.get(0)
        assertEquals(4, objectList.size())
        assertEquals("Package", String(objectList.get(0).getData(), 0, 7))
        assertEquals("METAFILEPICT", String(objectList.get(2).getData(), 0, 12))
        objectList = list.get(1)
        assertEquals(4, objectList.size())
        assertEquals("Package", String(objectList.get(0).getData(), 0, 7))
        assertEquals("METAFILEPICT", String(objectList.get(2).getData(), 0, 12))

        task = file.getTaskByID(Integer.valueOf(7))
        assertEquals("Task 7", task.name)
        notes = task.notes
        assertNotNull(notes)
        list = RTFEmbeddedObject.getEmbeddedObjects(notes)
        assertNotNull(list)
        assertEquals(2, list!!.size())
        objectList = list.get(0)
        assertEquals(4, objectList.size())
        assertEquals("Package", String(objectList.get(0).getData(), 0, 7))
        assertEquals("METAFILEPICT", String(objectList.get(2).getData(), 0, 12))
        objectList = list.get(1)
        assertEquals(4, objectList.size())
        assertEquals("Package", String(objectList.get(0).getData(), 0, 7))
        assertEquals("METAFILEPICT", String(objectList.get(2).getData(), 0, 12))
    }
}
