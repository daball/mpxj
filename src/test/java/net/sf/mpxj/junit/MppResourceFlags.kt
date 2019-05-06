package net.sf.mpxj.junit

import net.sf.mpxj.junit.MpxjAssert.*
import org.junit.Assert.*
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceType
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * Tests reading resource field bit flags from MPP files.
 */
class MppResourceFlags {
    /**
     * Test MPP14 saved by Project 2010.
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14FromProject2010() {
        val mpp = MPPReader().read(MpxjTestData.filePath("resourceFlags-mpp14Project2010.mpp"))
        testFlags(mpp)
    }

    /**
     * Test MPP14 saved by Project 2013.
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14FromProject2013() {
        // TODO work in progress - fix reading from Project 2013 MPP14
        //ProjectFile mpp = new MPPReader().read(MpxjTestData.filePath("resourceFlags-mpp14Project2013.mpp");
        //testFlags(mpp);
    }

    /**
     * Common code to test flag values.
     *
     * @param mpp project file to test
     */
    private fun testFlags(mpp: ProjectFile) {
        var resource: Resource

        //
        // Type
        //
        resource = mpp.getResourceByUniqueID(Integer.valueOf(1))
        assertEquals("Work 1", resource.name)
        assertEquals(ResourceType.WORK, resource.type)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(2))
        assertEquals("Material 1", resource.name)
        assertEquals(ResourceType.MATERIAL, resource.type)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(30))
        assertEquals("Cost 1", resource.name)
        assertEquals(ResourceType.COST, resource.type)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(4))
        assertEquals("Material 2", resource.name)
        assertEquals(ResourceType.MATERIAL, resource.type)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(31))
        assertEquals("Cost 2", resource.name)
        assertEquals(ResourceType.COST, resource.type)

        //
        // Budget
        //
        resource = mpp.getResourceByUniqueID(Integer.valueOf(5))
        assertEquals("Budget: No 1", resource.name)
        assertFalse(resource.budget)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(6))
        assertEquals("Budget: Yes 1", resource.name)
        assertTrue(resource.budget)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(7))
        assertEquals("Budget: No 2", resource.name)
        assertFalse(resource.budget)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(8))
        assertEquals("Budget: Yes 2", resource.name)
        assertTrue(resource.budget)

        //
        // Flags
        //
        resource = mpp.getResourceByUniqueID(Integer.valueOf(9))
        assertEquals("Flag1", resource.name)
        testFlag(resource, 1)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(10))
        assertEquals("Flag2", resource.name)
        testFlag(resource, 2)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(11))
        assertEquals("Flag3", resource.name)
        testFlag(resource, 3)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(12))
        assertEquals("Flag4", resource.name)
        testFlag(resource, 4)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(13))
        assertEquals("Flag5", resource.name)
        testFlag(resource, 5)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(14))
        assertEquals("Flag6", resource.name)
        testFlag(resource, 6)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(15))
        assertEquals("Flag7", resource.name)
        testFlag(resource, 7)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(16))
        assertEquals("Flag8", resource.name)
        testFlag(resource, 8)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(17))
        assertEquals("Flag9", resource.name)
        testFlag(resource, 9)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(18))
        assertEquals("Flag10", resource.name)
        testFlag(resource, 10)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(19))
        assertEquals("Flag11", resource.name)
        testFlag(resource, 11)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(20))
        assertEquals("Flag12", resource.name)
        testFlag(resource, 12)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(21))
        assertEquals("Flag13", resource.name)
        testFlag(resource, 13)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(22))
        assertEquals("Flag14", resource.name)
        testFlag(resource, 14)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(23))
        assertEquals("Flag15", resource.name)
        testFlag(resource, 15)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(24))
        assertEquals("Flag16", resource.name)
        testFlag(resource, 16)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(25))
        assertEquals("Flag17", resource.name)
        testFlag(resource, 17)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(26))
        assertEquals("Flag18", resource.name)
        testFlag(resource, 18)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(27))
        assertEquals("Flag19", resource.name)
        testFlag(resource, 19)

        resource = mpp.getResourceByUniqueID(Integer.valueOf(28))
        assertEquals("Flag20", resource.name)
        testFlag(resource, 20)
    }

    /**
     * Test all 20 custom field flags.
     *
     * @param resource resource to be tested
     * @param flag flag index to test
     */
    private fun testFlag(resource: Resource, flag: Int) {
        for (loop in 0..19) {
            assertBooleanEquals("Flag" + (loop + 1), flag == loop + 1, resource.getFlag(loop + 1))
        }
    }
}