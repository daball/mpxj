/*
 * file:       ProjectPropertiesTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       27/11/2014
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

package net.sf.mpxj.junit.project

import net.sf.mpxj.junit.MpxjAssert.*
import org.junit.Assert.*

import java.io.File

import org.junit.Test

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.junit.MpxjTestData
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.reader.ProjectReader
import net.sf.mpxj.reader.ProjectReaderUtility

/**
 * Tests to ensure project properties are correctly handled.
 */
class ProjectPropertiesTest {
    /**
     * Test to validate the project properties in files saved by different versions of MS Project.
     */
    @Test
    @Throws(MPXJException::class)
    fun testProjectProperties() {
        for (file in MpxjTestData.listFiles("generated/project-properties", "project-properties")) {
            testProjectProperties(file)
        }
    }

    /**
     * Test an individual project.
     *
     * @param file project file
     */
    @Throws(MPXJException::class)
    private fun testProjectProperties(file: File) {
        val reader = ProjectReaderUtility.getProjectReader(file.getName())
        if (reader is MPDDatabaseReader) {
            assumeJvm()
        }

        //DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        val project = reader.read(file)
        val properties = project.projectProperties

        //
        // We are reading title successfully - it's just that when MS Project saves
        // the test data files, sometimes it sets the value we asked for... sometimes
        // it reverts to the file name.
        //
        //assertEquals("Title", properties.getProjectTitle());

        assertEquals(file.getName(), "Subject", properties.subject)
        assertEquals("Author", properties.author)
        assertEquals("Keywords", properties.keywords)
        assertEquals("Comments", properties.comments)
        assertEquals("Template", properties.template)
        assertEquals("Category", properties.category)
        assertEquals("Format", properties.presentationFormat)
        assertEquals("Manager", properties.manager)
        assertEquals("Company", properties.company)

        if (NumberHelper.getInt(project.projectProperties.mppFileType) > 9) {
            assertEquals("Content type", properties.contentType)
            assertEquals("Content status", properties.contentStatus)
            assertEquals("Language", properties.language)
            assertEquals("Document version", properties.documentVersion)
        }

        val custom = properties.customProperties
        assertEquals(Integer.valueOf(1000), custom.get("CustomNumber"))
        assertEquals(Double.valueOf(1.5), custom.get("CustomFloat"))
        assertEquals("This is a custom property.", custom.get("CustomString"))
        // Project 2019 returns a time one hour behind what we expect. DST maybe?
        // TODO: investigate further
        //assertEquals(file.getName(), "01/01/2014", df.format((Date) custom.get("CustomDate")));
    }
}
