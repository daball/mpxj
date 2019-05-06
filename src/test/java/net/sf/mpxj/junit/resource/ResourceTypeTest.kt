/*
 * file:       ResourceTypeTest.java
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
import net.sf.mpxj.ResourceType
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.ProjectReaderUtility

/**
 * Tests to ensure task custom costs are correctly handled.
 */
class ResourceTypeTest {
    /**
     * Test to validate the custom costs in files saved by different versions of MS Project.
     */
    @Test
    @Throws(MPXJException::class)
    fun testResourceType() {
        for (file in MpxjTestData.listFiles("generated/resource-type", "resource-type")) {
            testResourceType(file)
        }
    }

    /**
     * Test an individual project.
     *
     * @param file project file
     */
    @Throws(MPXJException::class)
    private fun testResourceType(file: File) {
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        if (reader is MPDDatabaseReader) {
            assumeJvm()
        }

        val project = reader.read(file)
        val expectedType: ResourceType
        val mppFileType = project.projectProperties.mppFileType
        val missingCostType = mppFileType != null && mppFileType!!.intValue() < 12 || file.getName().endsWith(".mpd") || file.getName().indexOf("2003-mspdi") !== -1 || file.getName().indexOf("2002-mspdi") !== -1
        if (missingCostType) {
            expectedType = ResourceType.MATERIAL
        } else {
            expectedType = ResourceType.COST
        }

        assertEquals(file.getName(), expectedType, project.getResourceByID(Integer.valueOf(1)).type)
        assertEquals(file.getName(), expectedType, project.getResourceByID(Integer.valueOf(2)).type)
        assertEquals(file.getName(), expectedType, project.getResourceByID(Integer.valueOf(3)).type)
        assertEquals(file.getName(), expectedType, project.getResourceByID(Integer.valueOf(4)).type)
        assertEquals(file.getName(), expectedType, project.getResourceByID(Integer.valueOf(5)).type)

        assertEquals(file.getName(), ResourceType.MATERIAL, project.getResourceByID(Integer.valueOf(6)).type)
        assertEquals(file.getName(), ResourceType.MATERIAL, project.getResourceByID(Integer.valueOf(7)).type)
        assertEquals(file.getName(), ResourceType.MATERIAL, project.getResourceByID(Integer.valueOf(8)).type)
        assertEquals(file.getName(), ResourceType.MATERIAL, project.getResourceByID(Integer.valueOf(9)).type)
        assertEquals(file.getName(), ResourceType.MATERIAL, project.getResourceByID(Integer.valueOf(10)).type)

        assertEquals(file.getName(), ResourceType.WORK, project.getResourceByID(Integer.valueOf(11)).type)
        assertEquals(file.getName(), ResourceType.WORK, project.getResourceByID(Integer.valueOf(12)).type)
        assertEquals(file.getName(), ResourceType.WORK, project.getResourceByID(Integer.valueOf(13)).type)
        assertEquals(file.getName(), ResourceType.WORK, project.getResourceByID(Integer.valueOf(14)).type)
        assertEquals(file.getName(), ResourceType.WORK, project.getResourceByID(Integer.valueOf(15)).type)
    }
}
