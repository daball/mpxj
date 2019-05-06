/*
 * file:       ProjectWriterUtilityTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2008
 * date:       27/11/2008
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

package net.sf.mpxj.junit

import org.junit.Assert.*

import org.junit.Test

import net.sf.mpxj.mpx.MPXWriter
import net.sf.mpxj.mspdi.MSPDIWriter
import net.sf.mpxj.planner.PlannerWriter
import net.sf.mpxj.writer.ProjectWriter
import net.sf.mpxj.writer.ProjectWriterUtility

/**
 * The tests contained in this class exercise the ProjectWriterUtility class.
 */
class ProjectWriterUtilityTest {
    /**
     * Exercise the GetProjectWriter method.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testGetProjectWriter() {
        var writer: ProjectWriter

        try {
            writer = ProjectWriterUtility.getProjectWriter("filename.xxx")
            assertTrue("Failed to throw exception", false)
        } catch (ex: Exception) {
            assertEquals("Cannot write files of type: filename.xxx", ex.getMessage())
        }

        try {
            writer = ProjectWriterUtility.getProjectWriter("filename")
            assertTrue("Failed to throw exception", false)
        } catch (ex: Exception) {
            assertEquals("Filename has no extension: filename", ex.getMessage())
        }

        writer = ProjectWriterUtility.getProjectWriter("filename.mpx")
        assertTrue(writer is MPXWriter)

        writer = ProjectWriterUtility.getProjectWriter("filename.xml")
        assertTrue(writer is MSPDIWriter)

        writer = ProjectWriterUtility.getProjectWriter("filename.planner")
        assertTrue(writer is PlannerWriter)
    }

    /**
     * Test to exercise the getSupportedFileExtensions method.
     */
    @Test
    fun testGetSupportedFileExtensions() {
        val extensions = ProjectWriterUtility.supportedFileExtensions
        assertTrue(extensions.contains("MPX"))
    }
}
