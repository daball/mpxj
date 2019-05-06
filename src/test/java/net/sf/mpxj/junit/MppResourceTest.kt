/*
 * file:       MppResourceTest.java
 * author:     Wade Golden
 * copyright:  (c) Packwood Software 2006
 * date:       19-September-2006
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

import net.sf.mpxj.junit.MpxjAssert.*
import org.junit.Assert.*

import java.text.DateFormat
import java.text.SimpleDateFormat

import net.sf.mpxj.AccrueType
import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Rate
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceType
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.WorkContour
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpp.MPPReader
import net.sf.mpxj.mspdi.MSPDIReader

import org.junit.Test

/**
 * Tests to exercise MPP file read functionality for various versions of
 * MPP file.
 */
class MppResourceTest {

    /**
     * Test resource data read from an MPP9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9Resource() {
        val reader = MPPReader()
        reader.preserveNoteFormatting = false
        val mpp = reader.read(MpxjTestData.filePath("mpp9resource.mpp"))
        testResources(mpp)
        testNotes(mpp)
        testResourceAssignments(mpp)
        testResourceOutlineCodes(mpp)
    }

    /**
     * Test resource data read from an MPP9 file saved by Project 2007.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9ResourceFrom12() {
        val reader = MPPReader()
        reader.preserveNoteFormatting = false
        val mpp = reader.read(MpxjTestData.filePath("mpp9resource-from12.mpp"))
        testResources(mpp)
        testNotes(mpp)
        testResourceAssignments(mpp)
        testResourceOutlineCodes(mpp)
    }

    /**
     * Test resource data read from an MPP9 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9ResourceFrom14() {
        val reader = MPPReader()
        reader.preserveNoteFormatting = false
        val mpp = reader.read(MpxjTestData.filePath("mpp9resource-from14.mpp"))
        testResources(mpp)
        testNotes(mpp)
        testResourceAssignments(mpp)
        testResourceOutlineCodes(mpp)
    }

    /**
     * Test resource data read from an MPP12 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12Resource() {
        val reader = MPPReader()
        reader.preserveNoteFormatting = false
        val mpp = reader.read(MpxjTestData.filePath("mpp12resource.mpp"))
        testResources(mpp)
        testNotes(mpp)
        testResourceAssignments(mpp)
        testResourceOutlineCodes(mpp)
    }

    /**
     * Test resource data read from an MPP1 file saved by Project 2010.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12ResourceFrom14() {
        val reader = MPPReader()
        reader.preserveNoteFormatting = false
        val mpp = reader.read(MpxjTestData.filePath("mpp12resource-from14.mpp"))
        testResources(mpp)
        testNotes(mpp)
        testResourceAssignments(mpp)
        testResourceOutlineCodes(mpp)
    }

    /**
     * Test resource data read from an MPP14 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14Resource() {
        val reader = MPPReader()
        reader.preserveNoteFormatting = false
        val mpp = reader.read(MpxjTestData.filePath("mpp14resource.mpp"))
        testResources(mpp)
        testNotes(mpp)
        testResourceAssignments(mpp)
        testResourceOutlineCodes(mpp)
    }

    /**
     * Test resource data read from an MSPDI file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMspdiResource() {
        val reader = MSPDIReader()
        val mpp = reader.read(MpxjTestData.filePath("mspdiresource.xml"))
        testResources(mpp)
        testNotes(mpp)
        testResourceAssignments(mpp)
        //testResourceOutlineCodes(mpp); TODO: MSPDI resource outline code support
    }

    /**
     * Test resource data read from an MPD9 file.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpd9Resource() {
        assumeJvm()
        val reader = MPDDatabaseReader()
        reader.setPreserveNoteFormatting(false)
        val mpp = reader.read(MpxjTestData.filePath("mpp9resource.mpd"))
        testResources(mpp)
        testNotes(mpp)
        testResourceAssignments(mpp)
        testResourceOutlineCodes(mpp)
    }

    /**
     * Tests fields related to Resources.
     *
     * @param mpp The ProjectFile being tested.
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun testResources(mpp: ProjectFile) {

        /** MPP9 fields that return null:
         *
         * (would like these fixed in MPP9 as well)
         *
         * Material Label
         * Base Calendar
         */

        val df = SimpleDateFormat("dd/MM/yyyy")

        val listAllResources = mpp.resources
        assertTrue(listAllResources != null)
        // Fails for MPP12 as there is a summary resource
        //assertEquals(4, listAllResources.size());

        val resourceWade = mpp.getResourceByID(Integer.valueOf(1))
        val resourceJon = mpp.getResourceByID(Integer.valueOf(2))
        val resourceBrian = mpp.getResourceByID(Integer.valueOf(3))
        val resourceConcrete = mpp.getResourceByID(Integer.valueOf(4))
        // resource names
        assertEquals("Wade Golden", resourceWade.name)
        assertEquals("Jon Iles", resourceJon.name)
        assertEquals("Brian Leach", resourceBrian.name)
        assertEquals("Concrete", resourceConcrete.name)
        // type
        assertEquals(ResourceType.WORK, resourceWade.type)
        assertEquals(ResourceType.MATERIAL, resourceConcrete.type)
        // material label
        //assertEquals("ton", resourceConcrete.getMaterialLabel());
        // initials
        assertEquals("WG", resourceWade.initials)
        //  group
        assertEquals("Steelray", resourceWade.group)
        assertEquals("Tapsterrock", resourceJon.group)
        assertEquals("Steelray", resourceBrian.group)
        assertEquals("Mat", resourceConcrete.group)
        // max units
        assertEquals(Double.valueOf(100), resourceWade.maxUnits)
        // std rate
        var rate = Rate(50, TimeUnit.HOURS)
        assertEquals(rate, resourceWade.standardRate)
        rate = Rate(75, TimeUnit.HOURS)
        assertEquals(rate, resourceJon.standardRate)
        rate = Rate(100, TimeUnit.HOURS)
        assertEquals(rate, resourceBrian.standardRate)
        // overtime rate
        rate = Rate(100, TimeUnit.HOURS)
        assertEquals(rate, resourceWade.overtimeRate)
        rate = Rate(150, TimeUnit.HOURS)
        assertEquals(rate, resourceJon.overtimeRate)
        rate = Rate(200, TimeUnit.HOURS)
        assertEquals(rate, resourceBrian.overtimeRate)
        // cost per use
        assertEquals(Double.valueOf(500), resourceConcrete.costPerUse)
        // accrue type
        assertEquals(AccrueType.END, resourceWade.accrueAt)
        assertEquals(AccrueType.PRORATED, resourceJon.accrueAt)
        assertEquals(AccrueType.START, resourceConcrete.accrueAt)
        // code
        assertEquals("10", resourceWade.code)
        assertEquals("20", resourceJon.code)
        assertEquals("30", resourceBrian.code)

        assertEquals(1, resourceWade.getCost(1).intValue())
        assertEquals(2, resourceWade.getCost(2).intValue())
        assertEquals(3, resourceWade.getCost(3).intValue())
        assertEquals(4, resourceWade.getCost(4).intValue())
        assertEquals(5, resourceWade.getCost(5).intValue())
        assertEquals(6, resourceWade.getCost(6).intValue())
        assertEquals(7, resourceWade.getCost(7).intValue())
        assertEquals(8, resourceWade.getCost(8).intValue())
        assertEquals(9, resourceWade.getCost(9).intValue())
        assertEquals(10, resourceWade.getCost(10).intValue())

        assertEquals("wade.golden@steelray.com", resourceWade.emailAddress)

        assertEquals("01/01/2006", df.format(resourceWade.getDate(1)))
        assertEquals("02/01/2006", df.format(resourceWade.getDate(2)))
        assertEquals("03/01/2006", df.format(resourceWade.getDate(3)))
        assertEquals("04/01/2006", df.format(resourceWade.getDate(4)))
        assertEquals("05/01/2006", df.format(resourceWade.getDate(5)))
        assertEquals("06/01/2006", df.format(resourceWade.getDate(6)))
        assertEquals("07/01/2006", df.format(resourceWade.getDate(7)))
        assertEquals("08/01/2006", df.format(resourceWade.getDate(8)))
        assertEquals("09/01/2006", df.format(resourceWade.getDate(9)))
        assertEquals("10/01/2006", df.format(resourceWade.getDate(10)))

        assertEquals("01/02/2006", df.format(resourceWade.getStart(1)))
        assertEquals("02/02/2006", df.format(resourceWade.getStart(2)))
        assertEquals("03/02/2006", df.format(resourceWade.getStart(3)))
        assertEquals("04/02/2006", df.format(resourceWade.getStart(4)))
        assertEquals("05/02/2006", df.format(resourceWade.getStart(5)))
        assertEquals("06/02/2006", df.format(resourceWade.getStart(6)))
        assertEquals("07/02/2006", df.format(resourceWade.getStart(7)))
        assertEquals("08/02/2006", df.format(resourceWade.getStart(8)))
        assertEquals("09/02/2006", df.format(resourceWade.getStart(9)))
        assertEquals("10/02/2006", df.format(resourceWade.getStart(10)))

        assertEquals("01/03/2006", df.format(resourceWade.getFinish(1)))
        assertEquals("02/03/2006", df.format(resourceWade.getFinish(2)))
        assertEquals("03/03/2006", df.format(resourceWade.getFinish(3)))
        assertEquals("04/03/2006", df.format(resourceWade.getFinish(4)))
        assertEquals("05/03/2006", df.format(resourceWade.getFinish(5)))
        assertEquals("06/03/2006", df.format(resourceWade.getFinish(6)))
        assertEquals("07/03/2006", df.format(resourceWade.getFinish(7)))
        assertEquals("08/03/2006", df.format(resourceWade.getFinish(8)))
        assertEquals("09/03/2006", df.format(resourceWade.getFinish(9)))
        assertEquals("10/03/2006", df.format(resourceWade.getFinish(10)))

        assertEquals(1, (resourceWade.getDuration(1).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, resourceWade.getDuration(1).getUnits())
        assertEquals(2, (resourceWade.getDuration(2).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, resourceWade.getDuration(2).getUnits())
        assertEquals(3, (resourceWade.getDuration(3).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, resourceWade.getDuration(3).getUnits())
        assertEquals(4, (resourceWade.getDuration(4).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, resourceWade.getDuration(4).getUnits())
        assertEquals(5, (resourceWade.getDuration(5).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, resourceWade.getDuration(5).getUnits())
        assertEquals(6, (resourceWade.getDuration(6).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, resourceWade.getDuration(6).getUnits())
        assertEquals(7, (resourceWade.getDuration(7).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, resourceWade.getDuration(7).getUnits())
        assertEquals(8, (resourceWade.getDuration(8).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, resourceWade.getDuration(8).getUnits())
        assertEquals(9, (resourceWade.getDuration(9).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, resourceWade.getDuration(9).getUnits())
        assertEquals(10, (resourceWade.getDuration(10).getDuration() as Int).toLong())
        assertEquals(TimeUnit.DAYS, resourceWade.getDuration(10).getUnits())

        assertEquals(1, resourceWade.getNumber(1).intValue())
        assertEquals(2, resourceWade.getNumber(2).intValue())
        assertEquals(3, resourceWade.getNumber(3).intValue())
        assertEquals(4, resourceWade.getNumber(4).intValue())
        assertEquals(5, resourceWade.getNumber(5).intValue())
        assertEquals(6, resourceWade.getNumber(6).intValue())
        assertEquals(7, resourceWade.getNumber(7).intValue())
        assertEquals(8, resourceWade.getNumber(8).intValue())
        assertEquals(9, resourceWade.getNumber(9).intValue())
        assertEquals(10, resourceWade.getNumber(10).intValue())
        assertEquals(11, resourceWade.getNumber(11).intValue())
        assertEquals(12, resourceWade.getNumber(12).intValue())
        assertEquals(13, resourceWade.getNumber(13).intValue())
        assertEquals(14, resourceWade.getNumber(14).intValue())
        assertEquals(15, resourceWade.getNumber(15).intValue())
        assertEquals(16, resourceWade.getNumber(16).intValue())
        assertEquals(17, resourceWade.getNumber(17).intValue())
        assertEquals(18, resourceWade.getNumber(18).intValue())
        assertEquals(19, resourceWade.getNumber(19).intValue())
        assertEquals(20, resourceWade.getNumber(20).intValue())

        assertEquals("1", resourceWade.getText(1))
        assertEquals("2", resourceWade.getText(2))
        assertEquals("3", resourceWade.getText(3))
        assertEquals("4", resourceWade.getText(4))
        assertEquals("5", resourceWade.getText(5))
        assertEquals("6", resourceWade.getText(6))
        assertEquals("7", resourceWade.getText(7))
        assertEquals("8", resourceWade.getText(8))
        assertEquals("9", resourceWade.getText(9))
        assertEquals("10", resourceWade.getText(10))
        assertEquals("11", resourceWade.getText(11))
        assertEquals("12", resourceWade.getText(12))
        assertEquals("13", resourceWade.getText(13))
        assertEquals("14", resourceWade.getText(14))
        assertEquals("15", resourceWade.getText(15))
        assertEquals("16", resourceWade.getText(16))
        assertEquals("17", resourceWade.getText(17))
        assertEquals("18", resourceWade.getText(18))
        assertEquals("19", resourceWade.getText(19))
        assertEquals("20", resourceWade.getText(20))
        assertEquals("21", resourceWade.getText(21))
        assertEquals("22", resourceWade.getText(22))
        assertEquals("23", resourceWade.getText(23))
        assertEquals("24", resourceWade.getText(24))
        assertEquals("25", resourceWade.getText(25))
        assertEquals("26", resourceWade.getText(26))
        assertEquals("27", resourceWade.getText(27))
        assertEquals("28", resourceWade.getText(28))
        assertEquals("29", resourceWade.getText(29))
        assertEquals("30", resourceWade.getText(30))

        //assertEquals("Standard", resourceWade.getBaseCalendar()); // both of these currently return null from MPP9
        //assertEquals("Night Shift", resourceBrian.getBaseCalendar());
    }

    /**
     * Test resource outline codes.
     *
     * @param mpp project file
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun testResourceOutlineCodes(mpp: ProjectFile) {
        val resourceWade = mpp.getResourceByID(Integer.valueOf(1))
        assertEquals("AAA", resourceWade.outlineCode1)
        assertEquals("BBB", resourceWade.outlineCode2)
        assertEquals("CCC", resourceWade.outlineCode3)
        assertEquals("DDD", resourceWade.outlineCode4)
        assertEquals("EEE", resourceWade.outlineCode5)
        assertEquals("FFF", resourceWade.outlineCode6)
        assertEquals("GGG", resourceWade.outlineCode7)
        assertEquals("HHH", resourceWade.outlineCode8)
        assertEquals("III", resourceWade.outlineCode9)
        assertEquals("JJJ", resourceWade.outlineCode10)
    }

    /**
     * Tests fields related to Resource Assignments.
     *
     * @param mpp The ProjectFile being tested.
     */
    private fun testResourceAssignments(mpp: ProjectFile) {
        val intOne = Integer.valueOf(1)
        val df = SimpleDateFormat("dd/MM/yyyy")

        val listResourceAssignments = mpp.resourceAssignments

        val ra = listResourceAssignments.get(0)
        // id
        assertEquals(intOne, ra.resource!!.id)
        assertEquals(intOne, ra.resourceUniqueID)

        // start and finish
        assertEquals("25/08/2006", df.format(ra.start))
        assertEquals("29/08/2006", df.format(ra.finish))

        // task
        var task = ra.task
        assertEquals(intOne, task!!.id)
        assertEquals(Integer.valueOf(2), task!!.uniqueID)
        assertEquals("Task A", task!!.name)

        // units
        assertEquals(Double.valueOf(100), ra.units)

        // work and remaining work
        val dur24Hours = Duration.getInstance(24, TimeUnit.HOURS)
        assertEquals(dur24Hours, ra.work)
        assertEquals(dur24Hours, ra.remainingWork)

        //
        // Baseline values
        //
        assertEquals("01/01/2006", df.format(ra.baselineStart))
        assertEquals("02/01/2006", df.format(ra.baselineFinish))
        assertEquals(1, ra.baselineCost!!.intValue())
        assertEquals("2.0h", ra.baselineWork!!.toString())

        // Task 2
        // contour
        val ra2 = listResourceAssignments.get(3)
        assertEquals(WorkContour.TURTLE, ra2.workContour)

        // Task 3
        // completed
        task = mpp.getTaskByUniqueID(Integer.valueOf(4))
        assertEquals("Completed Task", task!!.name)
        val ra3 = task!!.resourceAssignments.get(0)

        //
        // Actual values
        //
        // actual start 26/08/06
        assertEquals("26/08/2006", df.format(ra3.actualStart))
        // actual finish 29/08/06
        assertEquals("29/08/2006", df.format(ra3.actualFinish))
        // actual work 16h
        assertEquals("16.0h", ra3.actualWork!!.toString())
        // actual cost $800
        assertEquals(800, ra3.actualCost.intValue())

    }

    /**
     * Validates that we are retrieving the notes correctly for each resource.
     *
     * @param file project file
     */
    private fun testNotes(file: ProjectFile) {
        for (resource in file.resources) {
            val id = resource.id!!.intValue()
            if (id != 0) {
                assertEquals("Resource Notes $id", resource.notes.trim())
            }
        }
    }

    /**
     * In the original MPP14 reader implementation, the ID and Unique ID
     * resource fields were read the wrong way around. This test validates
     * that the values are read correctly, especially when the ID != Unique ID.
     */
    @Test
    @Throws(Exception::class)
    fun testResourceIdAndUniqueID() {
        val reader = MPPReader()

        var file = reader.read(MpxjTestData.filePath("ResourceIdAndUniqueId-project2013-mpp14.mpp"))
        validateIdValues(file)

        file = reader.read(MpxjTestData.filePath("ResourceIdAndUniqueId-project2010-mpp14.mpp"))
        validateIdValues(file)
    }

    /**
     * Validate the ID, Unique ID and name attributes.
     *
     * @param file project file
     */
    private fun validateIdValues(file: ProjectFile) {
        assertEquals(4, file.resources.size().toLong())

        var resource = file.getResourceByUniqueID(Integer.valueOf(11))
        assertEquals(1, resource.id!!.intValue())
        assertEquals("Resource One", resource.name)

        resource = file.getResourceByUniqueID(Integer.valueOf(12))
        assertEquals(2, resource.id!!.intValue())
        assertEquals("Resource Two", resource.name)

        resource = file.getResourceByUniqueID(Integer.valueOf(13))
        assertEquals(3, resource.id!!.intValue())
        assertEquals("Resource Three", resource.name)

    }
}
