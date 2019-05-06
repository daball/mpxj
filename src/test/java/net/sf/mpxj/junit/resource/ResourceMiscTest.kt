/*
 * file:       ResourceMiscTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       08/-03/2017
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

package net.sf.mpxj.junit.resource

import net.sf.mpxj.junit.MpxjAssert.*
import org.junit.Assert.*

import java.io.File

import org.junit.Test

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Resource
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.ProjectReaderUtility

/**
 * Tests to ensure task custom costs are correctly handled.
 */
class ResourceMiscTest {
    /**
     * Test to validate the custom costs in files saved by different versions of MS Project.
     */
    @Test
    @Throws(MPXJException::class)
    fun testResourceMisc() {
        for (file in MpxjTestData.listFiles("generated/resource-misc", "resource-misc")) {
            testResourceMisc(file)
        }
    }

    /**
     * Test an individual project.
     *
     * @param file project file
     */
    @Throws(MPXJException::class)
    private fun testResourceMisc(file: File) {
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        if (reader is MPDDatabaseReader) {
            assumeJvm()
        }

        val project = reader.read(file)

        val resource1 = project.getResourceByID(Integer.valueOf(1))
        assertEquals("Resource 1", resource1.name)
        assertEquals("Code1", resource1.code)
        assertEquals(Double.valueOf(1.23), resource1.costPerUse)
        assertEquals("resource1@example.com", resource1.emailAddress)
        assertEquals("Group1", resource1.group)
        assertEquals("R1", resource1.initials)
        assertEquals("Notes1", resource1.notes)

        val resource2 = project.getResourceByID(Integer.valueOf(2))
        assertEquals("Resource 2", resource2.name)
        assertEquals("Code2", resource2.code)
        assertEquals(Double.valueOf(4.56), resource2.costPerUse)
        assertEquals("resource2@example.com", resource2.emailAddress)
        assertEquals("Group2", resource2.group)
        assertEquals("R2", resource2.initials)
        assertEquals("Notes2", resource2.notes)
    }
}
