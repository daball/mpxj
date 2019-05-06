/*
 * file:       MppPasswordTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2015
 * date:       21/03/2015
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
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * Test password protected files.
 */
class MppPasswordTest {
    /**
     * Test reading a password protected MPP9 file.
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9PasswordProtected() {
        val reader = MPPReader()
        reader.readPassword = "password"
        val project = reader.read(MpxjTestData.filePath("password-protected-mpp9.mpp"))
        assertEquals("Task from a password protected file", project.getTaskByID(Integer.valueOf(1)).name)
    }
}
