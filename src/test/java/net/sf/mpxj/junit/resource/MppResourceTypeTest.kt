/*
 * file:       MppResourceTypeTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       21/09/2014
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

import org.junit.Assert.*

import java.io.File

import org.junit.Test

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceType
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpp.MPPReader

/**
 * Tests to ensure resource type is correctly handled.
 */
class MppResourceTypeTest {
    /**
     * Test to exercise the test case provided for SourceForge bug #235.
     * https://sourceforge.net/p/mpxj/bugs/235/
     */
    @Test
    @Throws(MPXJException::class)
    fun testSourceForge235() {
        val file = File(MpxjTestData.filePath("resource/resource-type/sf235.mpp"))
        val project = MPPReader().read(file)
        testResource(file, project, 1, "Programmer 1", ResourceType.WORK)
        testResource(file, project, 2, "Programmer 2", ResourceType.WORK)
    }

    /**
     * Test to exercise the test case provided for SourceForge bug #235.
     * https://sourceforge.net/p/mpxj/bugs/256/
     */
    @Test
    @Throws(MPXJException::class)
    fun testSourceForge256() {
        val file = File(MpxjTestData.filePath("resource/resource-type/sf256.mpp"))
        val project = MPPReader().read(file)
        testResource(file, project, 1, "Cost", ResourceType.COST)
        testResource(file, project, 2, "Work", ResourceType.WORK)
        testResource(file, project, 3, "Material", ResourceType.MATERIAL)
    }

    /**
     * Test to validate the resource types in an MPP file saved by different versions of MS Project.
     */
    @Test
    @Throws(MPXJException::class)
    fun testResourceType() {
        for (file in MpxjTestData.listFiles("resource/resource-type", "resource-type")) {
            testResourceType(file)
        }
    }

    /**
     * Test the resource types present in an individual MPP file.
     *
     * @param file MPP file to test
     */
    @Throws(MPXJException::class)
    private fun testResourceType(file: File) {
        val project = MPPReader().read(file)
        val resources = project.getResources()
        assertEquals(10, resources.size())

        testResource(file, project, 1, "Work 1", ResourceType.WORK)
        testResource(file, project, 2, "Work 2", ResourceType.WORK)
        testResource(file, project, 3, "Work 3", ResourceType.WORK)
        testResource(file, project, 4, "Material 1", ResourceType.MATERIAL)
        testResource(file, project, 5, "Material 2", ResourceType.MATERIAL)
        testResource(file, project, 6, "Material 3", ResourceType.MATERIAL)

        //
        // The cost resource type was introduced in MPP12
        //
        val expectedType = if (NumberHelper.getInt(project.projectProperties.mppFileType) > 9) ResourceType.COST else ResourceType.MATERIAL
        testResource(file, project, 7, "Cost 1", expectedType)
        testResource(file, project, 8, "Cost 2", expectedType)
        testResource(file, project, 9, "Cost 3", expectedType)
    }

    /**
     * Validate the name and type of an individual resource.
     *
     * @param file MPP file
     * @param project project read from MPP file
     * @param id resource ID
     * @param expectedName expected name
     * @param expectedType expected type
     */
    private fun testResource(file: File, project: ProjectFile, id: Int, expectedName: String, expectedType: ResourceType) {
        val resource = project.getResourceByID(Integer.valueOf(id))
        assertEquals(file.getName(), expectedName, resource.name)
        assertEquals(file.getName(), expectedType, resource.type)
    }
}
