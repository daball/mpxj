/*
 * file:       MppGroupTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       24 January 2007
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

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

import net.sf.mpxj.Group
import net.sf.mpxj.GroupClause
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.TaskField
import net.sf.mpxj.mpp.BackgroundPattern
import net.sf.mpxj.mpp.ColorType
import net.sf.mpxj.mpp.FontStyle
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * Tests to exercise MPP file read functionality for various versions of
 * MPP file.
 */
class MppGroupTest {
    /**
     * Test group data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9Groups() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp9group.mpp"))
        testGroups(mpp)
    }

    /**
     * Test group data read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9GroupsFrom12() {
        //ProjectFile mpp = new MPPReader().read(MpxjTestData.filePath("mpp9group-from12.mpp"));
        //testGroups(mpp);
    }

    /**
     * Test group data read from an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9GroupsFrom14() {
        //ProjectFile mpp = new MPPReader().read(MpxjTestData.filePath("mpp9group-from14.mpp"));
        //testGroups(mpp);
    }

    /**
     * Test group data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12Groups() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp12group.mpp"))
        testGroups(mpp)
    }

    /**
     * Test group data read from an MPP12 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12GroupsFrom14() {
        //ProjectFile mpp = new MPPReader().read(MpxjTestData.filePath("mpp12group-from14.mpp"));
        //testGroups(mpp);
    }

    /**
     * Test group data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14Groups() {
        val mpp = MPPReader().read(MpxjTestData.filePath("mpp14group.mpp"))
        testGroups(mpp)
    }

    /**
     * Test group data.
     *
     * @param mpp ProjectFile instance
     */
    private fun testGroups(mpp: ProjectFile) {
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm")

        val group = mpp.groups.getByName("Group 1")
        assertNotNull(group)
        assertEquals("Group 1", group.name)
        assertFalse(group.showSummaryTasks)

        val clauses = group.groupClauses
        assertNotNull(clauses)
        assertEquals(6, clauses.size())

        //
        // Test clause 1
        //
        var clause = clauses.get(0)
        assertEquals(TaskField.DURATION1, clause.field)
        assertTrue(clause.ascending)
        var font = clause.font
        assertEquals("Arial", font!!.fontBase.name)
        assertEquals(8, font!!.fontBase.size.toLong())
        assertTrue(font!!.bold)
        assertFalse(font!!.italic)
        assertFalse(font!!.underline)
        assertEquals(ColorType.BLACK.color, font!!.color)
        assertEquals(ColorType.YELLOW.color, clause.cellBackgroundColor)
        assertEquals(1, clause.groupOn.toLong())
        assertEquals(1, (clause.startAt as Double).intValue())
        assertEquals(2, (clause.groupInterval as Double).intValue())
        assertEquals(BackgroundPattern.DOTTED, clause.pattern)

        //
        // Test clause 2
        //
        clause = clauses.get(1)
        assertEquals(TaskField.NUMBER1, clause.field)
        assertFalse(clause.ascending)
        font = clause.font
        assertEquals("Arial", font!!.fontBase.name)
        assertEquals(8, font!!.fontBase.size.toLong())
        assertTrue(font!!.bold)
        assertFalse(font!!.italic)
        assertFalse(font!!.underline)
        assertEquals(ColorType.BLACK.color, font!!.color)
        assertEquals(ColorType.SILVER.color, clause.cellBackgroundColor)
        assertEquals(1, clause.groupOn.toLong())
        assertEquals(3, (clause.startAt as Double).intValue())
        assertEquals(4, (clause.groupInterval as Double).intValue())
        assertEquals(BackgroundPattern.CHECKERED, clause.pattern)

        //
        // Test clause 3
        //
        clause = clauses.get(2)
        assertEquals(TaskField.COST1, clause.field)
        assertTrue(clause.ascending)
        font = clause.font
        assertEquals("Arial", font!!.fontBase.name)
        assertEquals(8, font!!.fontBase.size.toLong())
        assertTrue(font!!.bold)
        assertFalse(font!!.italic)
        assertFalse(font!!.underline)
        assertEquals(ColorType.BLACK.color, font!!.color)
        assertEquals(ColorType.YELLOW.color, clause.cellBackgroundColor)
        assertEquals(1, clause.groupOn.toLong())
        assertEquals(5, (clause.startAt as Double).intValue())
        assertEquals(6, (clause.groupInterval as Double).intValue())
        assertEquals(BackgroundPattern.LIGHTDOTTED, clause.pattern)

        //
        // Test clause 4
        //
        clause = clauses.get(3)
        assertEquals(TaskField.PERCENT_COMPLETE, clause.field)
        assertFalse(clause.ascending)
        font = clause.font
        assertEquals("Arial", font!!.fontBase.name)
        assertEquals(8, font!!.fontBase.size.toLong())
        assertTrue(font!!.bold)
        assertFalse(font!!.italic)
        assertFalse(font!!.underline)
        assertEquals(ColorType.BLACK.color, font!!.color)
        assertEquals(ColorType.SILVER.color, clause.cellBackgroundColor)
        assertEquals(1, clause.groupOn.toLong())
        assertEquals(7, (clause.startAt as Integer).intValue())
        assertEquals(8, (clause.groupInterval as Integer).intValue())
        assertEquals(BackgroundPattern.SOLID, clause.pattern)

        //
        // Test clause 5
        //
        clause = clauses.get(4)
        assertEquals(TaskField.FLAG1, clause.field)
        assertTrue(clause.ascending)
        font = clause.font
        assertEquals("Arial", font!!.fontBase.name)
        assertEquals(8, font!!.fontBase.size.toLong())
        assertTrue(font!!.bold)
        assertFalse(font!!.italic)
        assertFalse(font!!.underline)
        assertEquals(ColorType.BLACK.color, font!!.color)
        assertEquals(ColorType.YELLOW.color, clause.cellBackgroundColor)
        assertEquals(BackgroundPattern.DOTTED, clause.pattern)

        //
        // Test clause 6
        //
        clause = clauses.get(5)
        assertEquals(TaskField.DATE1, clause.field)
        assertFalse(clause.ascending)
        font = clause.font
        assertEquals("Arial", font!!.fontBase.name)
        assertEquals(8, font!!.fontBase.size.toLong())
        assertTrue(font!!.bold)
        assertFalse(font!!.italic)
        assertFalse(font!!.underline)
        assertEquals(ColorType.BLACK.color, font!!.color)
        assertEquals(ColorType.SILVER.color, clause.cellBackgroundColor)
        assertEquals(1, clause.groupOn.toLong())
        assertEquals("07/02/2006 00:00", df.format(clause.startAt as Date))
        assertEquals(10, (clause.groupInterval as Integer).intValue())
        assertEquals(BackgroundPattern.CHECKERED, clause.pattern)
    }
}
