/*
 * file:       MppColumnsTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       15/03/2010
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
import net.sf.mpxj.Column
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Table
import net.sf.mpxj.mpp.MPPReader

import org.junit.Test

/**
 * Test columns read from MPP files.
 */
class MppColumnsTest {
    /**
     * Test MPP9 file columns.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp9() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp9columns.mpp"))
        testTaskColumns(file, TASK_COLUMNS_1, MPP12_TASK_COLUMNS_1)
        testTaskColumns(file, TASK_COLUMNS_2, MPP12_TASK_COLUMNS_2)
        testTaskColumns(file, TASK_COLUMNS_3, MPP14_TASK_COLUMNS_3)
        testTaskColumns(file, TASK_COLUMNS_4, MPP14_TASK_COLUMNS_4)

        testResourceColumns(file, RESOURCE_COLUMNS_1, MPP12_RESOURCE_COLUMNS_1)
        testResourceColumns(file, RESOURCE_COLUMNS_2, MPP14_RESOURCE_COLUMNS_2)
        testResourceColumns(file, RESOURCE_COLUMNS_3, MPP14_RESOURCE_COLUMNS_3)
        testResourceColumns(file, RESOURCE_COLUMNS_4, MPP14_RESOURCE_COLUMNS_4)
        testResourceColumns(file, RESOURCE_COLUMNS_5, MPP14_RESOURCE_COLUMNS_5)
        testResourceColumns(file, RESOURCE_COLUMNS_6, MPP14_RESOURCE_COLUMNS_6)
        testResourceColumns(file, RESOURCE_COLUMNS_7, MPP14_RESOURCE_COLUMNS_7)
        testResourceColumns(file, RESOURCE_COLUMNS_8, MPP14_RESOURCE_COLUMNS_8)
        testResourceColumns(file, RESOURCE_COLUMNS_9, MPP14_RESOURCE_COLUMNS_9)
        testResourceColumns(file, RESOURCE_COLUMNS_10, MPP14_RESOURCE_COLUMNS_10)
        testResourceColumns(file, RESOURCE_COLUMNS_11, MPP14_RESOURCE_COLUMNS_11)
        testResourceColumns(file, RESOURCE_COLUMNS_12, MPP14_RESOURCE_COLUMNS_12)
        testResourceColumns(file, RESOURCE_COLUMNS_13, MPP14_RESOURCE_COLUMNS_13)
        testResourceColumns(file, RESOURCE_COLUMNS_14, MPP14_RESOURCE_COLUMNS_14)
        testResourceColumns(file, RESOURCE_COLUMNS_15, MPP14_RESOURCE_COLUMNS_15)
    }

    /**
     * Test MPP12 file columns.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp12() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp12columns.mpp"))
        testTaskColumns(file, TASK_COLUMNS_1, MPP12_TASK_COLUMNS_1)
        testTaskColumns(file, TASK_COLUMNS_2, MPP12_TASK_COLUMNS_2)
        testTaskColumns(file, TASK_COLUMNS_3, MPP14_TASK_COLUMNS_3)
        testTaskColumns(file, TASK_COLUMNS_4, MPP14_TASK_COLUMNS_4)

        testResourceColumns(file, RESOURCE_COLUMNS_1, MPP12_RESOURCE_COLUMNS_1)
        testResourceColumns(file, RESOURCE_COLUMNS_2, MPP14_RESOURCE_COLUMNS_2)
        testResourceColumns(file, RESOURCE_COLUMNS_3, MPP14_RESOURCE_COLUMNS_3)
        testResourceColumns(file, RESOURCE_COLUMNS_4, MPP14_RESOURCE_COLUMNS_4)
        testResourceColumns(file, RESOURCE_COLUMNS_5, MPP14_RESOURCE_COLUMNS_5)
        testResourceColumns(file, RESOURCE_COLUMNS_6, MPP14_RESOURCE_COLUMNS_6)
        testResourceColumns(file, RESOURCE_COLUMNS_7, MPP14_RESOURCE_COLUMNS_7)
        testResourceColumns(file, RESOURCE_COLUMNS_8, MPP14_RESOURCE_COLUMNS_8)
        testResourceColumns(file, RESOURCE_COLUMNS_9, MPP14_RESOURCE_COLUMNS_9)
        testResourceColumns(file, RESOURCE_COLUMNS_10, MPP14_RESOURCE_COLUMNS_10)
        testResourceColumns(file, RESOURCE_COLUMNS_11, MPP14_RESOURCE_COLUMNS_11)
        testResourceColumns(file, RESOURCE_COLUMNS_12, MPP14_RESOURCE_COLUMNS_12)
        testResourceColumns(file, RESOURCE_COLUMNS_13, MPP14_RESOURCE_COLUMNS_13)
        testResourceColumns(file, RESOURCE_COLUMNS_14, MPP14_RESOURCE_COLUMNS_14)
        testResourceColumns(file, RESOURCE_COLUMNS_15, MPP14_RESOURCE_COLUMNS_15)
    }

    /**
     * Test MPP14 file columns.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testMpp14() {
        val file = MPPReader().read(MpxjTestData.filePath("mpp14columns.mpp"))
        testTaskColumns(file, TASK_COLUMNS_1, MPP14_TASK_COLUMNS_1)
        testTaskColumns(file, TASK_COLUMNS_2, MPP14_TASK_COLUMNS_2)
        testTaskColumns(file, TASK_COLUMNS_3, MPP14_TASK_COLUMNS_3)
        testTaskColumns(file, TASK_COLUMNS_4, MPP14_TASK_COLUMNS_4)

        testResourceColumns(file, RESOURCE_COLUMNS_1, MPP14_RESOURCE_COLUMNS_1)
        testResourceColumns(file, RESOURCE_COLUMNS_2, MPP14_RESOURCE_COLUMNS_2)
        testResourceColumns(file, RESOURCE_COLUMNS_3, MPP14_RESOURCE_COLUMNS_3)
        testResourceColumns(file, RESOURCE_COLUMNS_4, MPP14_RESOURCE_COLUMNS_4)
        testResourceColumns(file, RESOURCE_COLUMNS_5, MPP14_RESOURCE_COLUMNS_5)
        testResourceColumns(file, RESOURCE_COLUMNS_6, MPP14_RESOURCE_COLUMNS_6)
        testResourceColumns(file, RESOURCE_COLUMNS_7, MPP14_RESOURCE_COLUMNS_7)
        testResourceColumns(file, RESOURCE_COLUMNS_8, MPP14_RESOURCE_COLUMNS_8)
        testResourceColumns(file, RESOURCE_COLUMNS_9, MPP14_RESOURCE_COLUMNS_9)
        testResourceColumns(file, RESOURCE_COLUMNS_10, MPP14_RESOURCE_COLUMNS_10)
        testResourceColumns(file, RESOURCE_COLUMNS_11, MPP14_RESOURCE_COLUMNS_11)
        testResourceColumns(file, RESOURCE_COLUMNS_12, MPP14_RESOURCE_COLUMNS_12)
        testResourceColumns(file, RESOURCE_COLUMNS_13, MPP14_RESOURCE_COLUMNS_13)
        testResourceColumns(file, RESOURCE_COLUMNS_14, MPP14_RESOURCE_COLUMNS_14)
        testResourceColumns(file, RESOURCE_COLUMNS_15, MPP14_RESOURCE_COLUMNS_15)
    }

    /**
     * Compare the columns from an MPP file to reference data.
     *
     * @param file input MPP file
     * @param name table name
     * @param columns reference columns
     */
    private fun testTaskColumns(file: ProjectFile, name: String, columns: Array<String>) {
        val table = file.tables.getTaskTableByName(name)
        assertEquals(columns.size, table.columns.size())
        var index = 0
        for (column in table.columns) {
            assertEquals(columns[index++], column.getTitle())
        }
    }

    /**
     * Compare the columns from an MPP file to reference data.
     *
     * @param file input MPP file
     * @param name table name
     * @param columns reference columns
     */
    private fun testResourceColumns(file: ProjectFile, name: String, columns: Array<String>) {
        val table = file.tables.getResourceTableByName(name)
        assertEquals(columns.size, table.columns.size())
        var index = 0
        for (column in table.columns) {
            assertEquals(columns[index++], column.getTitle())
        }
    }

    companion object {

        private val TASK_COLUMNS_1 = "Task Columns 1"
        private val TASK_COLUMNS_2 = "Task Columns 2"
        private val TASK_COLUMNS_3 = "Task Columns 3"
        private val TASK_COLUMNS_4 = "Task Columns 4"

        private val RESOURCE_COLUMNS_1 = "Resource Columns 1"
        private val RESOURCE_COLUMNS_2 = "Resource Columns 2"
        private val RESOURCE_COLUMNS_3 = "Resource Columns 3"
        private val RESOURCE_COLUMNS_4 = "Resource Columns 4"
        private val RESOURCE_COLUMNS_5 = "Resource Columns 5"
        private val RESOURCE_COLUMNS_6 = "Resource Columns 6"
        private val RESOURCE_COLUMNS_7 = "Resource Columns 7"
        private val RESOURCE_COLUMNS_8 = "Resource Columns 8"
        private val RESOURCE_COLUMNS_9 = "Resource Columns 9"
        private val RESOURCE_COLUMNS_10 = "Resource Columns 10"
        private val RESOURCE_COLUMNS_11 = "Resource Columns 11"
        private val RESOURCE_COLUMNS_12 = "Resource Columns 12"
        private val RESOURCE_COLUMNS_13 = "Resource Columns 13"
        private val RESOURCE_COLUMNS_14 = "Resource Columns 14"
        private val RESOURCE_COLUMNS_15 = "Resource Columns 15"

        private val MPP14_TASK_COLUMNS_1 = arrayOf("Task Name", "Task Mode", "% Complete", "% Work Complete", "Active", "Actual Cost", "Actual Duration", "Actual Finish", "Actual Overtime Cost", "Actual Overtime Work", "Actual Start", "Actual Work", "ACWP", "Assignment", "Assignment Delay", "Assignment Owner", "Assignment Units", "Baseline Budget Cost", "Baseline Budget Work", "Baseline Cost", "Baseline Deliverable Finish", "Baseline Deliverable Start", "Baseline Duration", "Baseline Estimated Duration", "Baseline Estimated Finish", "Baseline Estimated Start", "Baseline Finish", "Baseline Fixed Cost", "Baseline Fixed Cost Accrual", "Baseline Start", "Baseline Work", "Baseline1 Budget Cost", "Baseline1 Budget Work", "Baseline1 Cost", "Baseline1 Deliverable Finish", "Baseline1 Deliverable Start", "Baseline1 Duration", "Baseline1 Estimated Duration", "Baseline1 Estimated Finish", "Baseline1 Estimated Start", "Baseline1 Finish", "Baseline1 Fixed Cost", "Baseline1 Fixed Cost Accrual", "Baseline1 Start", "Baseline1 Work", "Baseline10 Budget Cost", "Baseline10 Budget Work", "Baseline10 Cost", "Baseline10 Deliverable Finish", "Baseline10 Deliverable Start", "Baseline10 Duration", "Baseline10 Estimated Duration", "Baseline10 Estimated Finish", "Baseline10 Estimated Start", "Baseline10 Finish", "Baseline10 Fixed Cost", "Baseline10 Fixed Cost Accrual", "Baseline10 Start", "Baseline10 Work", "Baseline2 Budget Cost", "Baseline2 Budget Work", "Baseline2 Cost", "Baseline2 Deliverable Finish", "Baseline2 Deliverable Start", "Baseline2 Duration", "Baseline2 Estimated Duration", "Baseline2 Estimated Finish", "Baseline2 Estimated Start", "Baseline2 Finish", "Baseline2 Fixed Cost", "Baseline2 Fixed Cost Accrual", "Baseline2 Start", "Baseline2 Work", "Baseline3 Budget Cost", "Baseline3 Budget Work", "Baseline3 Cost", "Baseline3 Deliverable Finish", "Baseline3 Deliverable Start", "Baseline3 Duration", "Baseline3 Estimated Duration", "Baseline3 Estimated Finish", "Baseline3 Estimated Start", "Baseline3 Finish", "Baseline3 Fixed Cost", "Baseline3 Fixed Cost Accrual", "Baseline3 Start", "Baseline3 Work", "Baseline4 Budget Cost", "Baseline4 Budget Work", "Baseline4 Cost", "Baseline4 Deliverable Finish", "Baseline4 Deliverable Start", "Baseline4 Duration", "Baseline4 Estimated Duration", "Baseline4 Estimated Finish", "Baseline4 Estimated Start", "Baseline4 Finish", "Baseline4 Fixed Cost", "Baseline4 Fixed Cost Accrual", "Baseline4 Start", "Baseline4 Work", "Baseline5 Budget Cost", "Baseline5 Budget Work", "Baseline5 Cost", "Baseline5 Deliverable Finish", "Baseline5 Deliverable Start", "Baseline5 Duration", "Baseline5 Estimated Duration", "Baseline5 Estimated Finish", "Baseline5 Estimated Start", "Baseline5 Finish", "Baseline5 Fixed Cost", "Baseline5 Fixed Cost Accrual", "Baseline5 Start", "Baseline5 Work", "Baseline6 Budget Cost", "Baseline6 Budget Work", "Baseline6 Cost", "Baseline6 Deliverable Finish", "Baseline6 Deliverable Start", "Baseline6 Duration", "Baseline6 Estimated Duration", "Baseline6 Estimated Finish", "Baseline6 Estimated Start", "Baseline6 Finish", "Baseline6 Fixed Cost", "Baseline6 Fixed Cost Accrual", "Baseline6 Start", "Baseline6 Work", "Baseline7 Budget Cost", "Baseline7 Budget Work", "Baseline7 Cost", "Baseline7 Deliverable Finish", "Baseline7 Deliverable Start", "Baseline7 Duration", "Baseline7 Estimated Duration", "Baseline7 Estimated Finish", "Baseline7 Estimated Start", "Baseline7 Finish", "Baseline7 Fixed Cost", "Baseline7 Fixed Cost Accrual", "Baseline7 Start", "Baseline7 Work", "Baseline8 Budget Cost", "Baseline8 Budget Work", "Baseline8 Cost", "Baseline8 Deliverable Finish", "Baseline8 Deliverable Start", "Baseline8 Duration", "Baseline8 Estimated Duration", "Baseline8 Estimated Finish", "Baseline8 Estimated Start", "Baseline8 Finish", "Baseline8 Fixed Cost", "Baseline8 Fixed Cost Accrual", "Baseline8 Start", "Baseline8 Work", "Baseline9 Budget Cost", "Baseline9 Budget Work", "Baseline9 Cost", "Baseline9 Deliverable Finish", "Baseline9 Deliverable Start", "Baseline9 Duration", "Baseline9 Estimated Duration", "Baseline9 Estimated Finish", "Baseline9 Estimated Start", "Baseline9 Finish", "Baseline9 Fixed Cost", "Baseline9 Fixed Cost Accrual", "Baseline9 Start", "Baseline9 Work", "Confirmed", "Constraint Date", "Constraint Type", "Contact", "Cost", "Cost Rate Table", "Cost Variance", "Cost1", "Cost10", "Cost2", "Cost3", "Cost4", "Cost5", "Cost6", "Cost7", "Cost8", "Cost9", "CPI", "Created", "Critical", "CV", "CV%", "Date1", "Date10", "Date2", "Date3", "Date4", "Date5", "Date6", "Date7", "Date8", "Date9", "Deadline", "Deliverable Finish", "Deliverable GUID", "Deliverable Name", "Deliverable Start", "Deliverable Type", "Duration", "Duration Variance", "Duration1", "Duration10", "Duration2", "Duration3", "Duration4", "Duration5", "Duration6", "Duration7", "Duration8", "Duration9", "EAC", "Early Finish", "Early Start", "Earned Value Method", "Effort Driven", "Error Message", "Estimated", "External Task", "Finish", "Finish Slack", "Finish Variance", "Finish1", "Finish10", "Finish2", "Finish3", "Finish4", "Finish5", "Finish6", "Finish7", "Finish8", "Finish9", "Fixed Cost", "Fixed Cost Accrual", "Flag1", "Flag10", "Flag11", "Flag12", "Flag13", "Flag14", "Flag15", "Flag16")

        private val MPP14_TASK_COLUMNS_2 = arrayOf<String>(null, "Task Mode", "Flag17", "Flag18", "Flag19", "Flag2", "Flag20", "Flag3", "Flag4", "Flag5", "Flag6", "Flag7", "Flag8", "Flag9", "Free Slack", "Group By Summary", "GUID", "Hide Bar", "Hyperlink", "Hyperlink Address", "Hyperlink Href", "Hyperlink SubAddress", "ID", "Ignore Resource Calendar", "Ignore Warnings", "Indicators", "Late Finish", "Late Start", "Level Assignments", "Leveling Can Split", "Leveling Delay", "Linked Fields", "Marked", "Milestone", "Task Name", "Notes", "Number1", "Number10", "Number11", "Number12", "Number13", "Number14", "Number15", "Number16", "Number17", "Number18", "Number19", "Number2", "Number20", "Number3", "Number4", "Number5", "Number6", "Number7", "Number8", "Number9", "Objects", "Outline Code1", "Outline Code10", "Outline Code2", "Outline Code3", "Outline Code4", "Outline Code5", "Outline Code6", "Outline Code7", "Outline Code8", "Outline Code9", "Outline Level", "Outline Number", "Overallocated", "Overtime Cost", "Overtime Work", "Peak", "Physical % Complete", "Placeholder", "Predecessors", "Preleveled Finish", "Preleveled Start", "Priority", "Project", "Publish", "Recurring", "Regular Work", "Remaining Cost", "Remaining Duration", "Remaining Overtime Cost", "Remaining Overtime Work", "Remaining Work", "Request/Demand", "Resource Group", "Resource Initials", "Resource Names", "Resource Phonetics", "Resource Type", "Response Pending", "Resume", "Rollup", "Scheduled Duration", "Scheduled Finish", "Scheduled Start", "SPI", "Start", "Start Slack", "Start Variance", "Start1", "Start10", "Start2", "Start3", "Start4", "Start5", "Start6", "Start7", "Start8", "Start9", "Status", "Status Indicator", "Status Manager", "Stop", "Subproject File", "Subproject Read Only", "Successors", "Summary", "SV", "SV%", "Task Calendar", "Task Calendar GUID", "Task Mode", "TCPI", "TeamStatus Pending", "Text1", "Text10", "Text11", "Text12", "Text13", "Text14", "Text15", "Text16", "Text17", "Text18", "Text19", "Text2", "Text20", "Text21", "Text22", "Text23", "Text24", "Text25", "Text26", "Text27", "Text28", "Text29", "Text3", "Text30", "Text4", "Text5", "Text6", "Text7", "Text8", "Text9", "Total Slack", "Type", "Unique ID", "Unique ID Predecessors", "Unique ID Successors", "Update Needed", "VAC", "Warning", "WBS", "WBS Predecessors", "WBS Successors", "Work", "Work Contour", "Work Variance")

        private val MPP14_TASK_COLUMNS_3 = arrayOf("ID", "Task Name", "Enterprise Text1", "Enterprise Text2", "Enterprise Text3", "Enterprise Text4", "Enterprise Text5", "Enterprise Text6", "Enterprise Text7", "Enterprise Text8", "Enterprise Text9", "Enterprise Text10", "Enterprise Text11", "Enterprise Text12", "Enterprise Text13", "Enterprise Text14", "Enterprise Text15", "Enterprise Text16", "Enterprise Text17", "Enterprise Text18", "Enterprise Text19", "Enterprise Text20", "Enterprise Text21", "Enterprise Text22", "Enterprise Text23", "Enterprise Text24", "Enterprise Text25", "Enterprise Text26", "Enterprise Text27", "Enterprise Text28", "Enterprise Text29", "Enterprise Text30", "Enterprise Text31", "Enterprise Text32", "Enterprise Text33", "Enterprise Text34", "Enterprise Text35", "Enterprise Text36", "Enterprise Text37", "Enterprise Text38", "Enterprise Text39", "Enterprise Text40", "Enterprise Cost1", "Enterprise Cost2", "Enterprise Cost3", "Enterprise Cost4", "Enterprise Cost5", "Enterprise Cost6", "Enterprise Cost7", "Enterprise Cost8", "Enterprise Cost9", "Enterprise Cost10", "Enterprise Date1", "Enterprise Date2", "Enterprise Date3", "Enterprise Date4", "Enterprise Date5", "Enterprise Date6", "Enterprise Date7", "Enterprise Date8", "Enterprise Date9", "Enterprise Date10", "Enterprise Date11", "Enterprise Date12", "Enterprise Date13", "Enterprise Date14", "Enterprise Date15", "Enterprise Date16", "Enterprise Date17", "Enterprise Date18", "Enterprise Date19", "Enterprise Date20", "Enterprise Date21", "Enterprise Date22", "Enterprise Date23", "Enterprise Date24", "Enterprise Date25", "Enterprise Date26", "Enterprise Date27", "Enterprise Date28", "Enterprise Date29", "Enterprise Date30")

        private val MPP14_TASK_COLUMNS_4 = arrayOf("ID", "Task Name", "Enterprise Duration1", "Enterprise Duration2", "Enterprise Duration3", "Enterprise Duration4", "Enterprise Duration5", "Enterprise Duration6", "Enterprise Duration7", "Enterprise Duration8", "Enterprise Duration9", "Enterprise Duration10", "Enterprise Flag1", "Enterprise Flag2", "Enterprise Flag3", "Enterprise Flag4", "Enterprise Flag5", "Enterprise Flag6", "Enterprise Flag7", "Enterprise Flag8", "Enterprise Flag9", "Enterprise Flag10", "Enterprise Flag11", "Enterprise Flag12", "Enterprise Flag13", "Enterprise Flag14", "Enterprise Flag15", "Enterprise Flag16", "Enterprise Flag17", "Enterprise Flag18", "Enterprise Flag19", "Enterprise Flag20", "Enterprise Number1", "Enterprise Number2", "Enterprise Number3", "Enterprise Number4", "Enterprise Number5", "Enterprise Number6", "Enterprise Number7", "Enterprise Number8", "Enterprise Number9", "Enterprise Number10", "Enterprise Number11", "Enterprise Number12", "Enterprise Number13", "Enterprise Number14", "Enterprise Number15", "Enterprise Number16", "Enterprise Number17", "Enterprise Number18", "Enterprise Number19", "Enterprise Number20", "Enterprise Number21", "Enterprise Number22", "Enterprise Number23", "Enterprise Number24", "Enterprise Number25", "Enterprise Number26", "Enterprise Number27", "Enterprise Number28", "Enterprise Number29", "Enterprise Number30", "Enterprise Number31", "Enterprise Number32", "Enterprise Number33", "Enterprise Number34", "Enterprise Number35", "Enterprise Number36", "Enterprise Number37", "Enterprise Number38", "Enterprise Number39", "Enterprise Number40")

        private val MPP12_TASK_COLUMNS_1 = arrayOf("Task Name", "% Complete", "% Work Complete", "<Unavailable>", "Actual Cost", "Actual Duration", "Actual Finish", "Actual Overtime Cost", "Actual Overtime Work", "Actual Start", "Actual Work", "ACWP", "Assignment", "Assignment Delay", "Assignment Owner", "Assignment Units", "Baseline Budget Cost", "Baseline Budget Work", "Baseline Cost", "Baseline Deliverable Finish", "Baseline Deliverable Start", "Baseline Duration", "Baseline Duration", "Baseline Finish", "Baseline Start", "Baseline Finish", "Baseline Fixed Cost", "Baseline Fixed Cost Accrual", "Baseline Start", "Baseline Work", "Baseline1 Budget Cost", "Baseline1 Budget Work", "Baseline1 Cost", "Baseline1 Deliverable Finish", "Baseline1 Deliverable Start", "Baseline1 Duration", "Baseline1 Duration", "Baseline1 Finish", "Baseline1 Start", "Baseline1 Finish", "Baseline1 Fixed Cost", "Baseline1 Fixed Cost Accrual", "Baseline1 Start", "Baseline1 Work", "Baseline10 Budget Cost", "Baseline10 Budget Work", "Baseline10 Cost", "Baseline10 Deliverable Finish", "Baseline10 Deliverable Start", "Baseline10 Duration", "Baseline10 Duration", "Baseline10 Finish", "Baseline10 Start", "Baseline10 Finish", "Baseline10 Fixed Cost", "Baseline10 Fixed Cost Accrual", "Baseline10 Start", "Baseline10 Work", "Baseline2 Budget Cost", "Baseline2 Budget Work", "Baseline2 Cost", "Baseline2 Deliverable Finish", "Baseline2 Deliverable Start", "Baseline2 Duration", "Baseline2 Duration", "Baseline2 Finish", "Baseline2 Start", "Baseline2 Finish", "Baseline2 Fixed Cost", "Baseline2 Fixed Cost Accrual", "Baseline2 Start", "Baseline2 Work", "Baseline3 Budget Cost", "Baseline3 Budget Work", "Baseline3 Cost", "Baseline3 Deliverable Finish", "Baseline3 Deliverable Start", "Baseline3 Duration", "Baseline3 Duration", "Baseline3 Finish", "Baseline3 Start", "Baseline3 Finish", "Baseline3 Fixed Cost", "Baseline3 Fixed Cost Accrual", "Baseline3 Start", "Baseline3 Work", "Baseline4 Budget Cost", "Baseline4 Budget Work", "Baseline4 Cost", "Baseline4 Deliverable Finish", "Baseline4 Deliverable Start", "Baseline4 Duration", "Baseline4 Duration", "Baseline4 Finish", "Baseline4 Start", "Baseline4 Finish", "Baseline4 Fixed Cost", "Baseline4 Fixed Cost Accrual", "Baseline4 Start", "Baseline4 Work", "Baseline5 Budget Cost", "Baseline5 Budget Work", "Baseline5 Cost", "Baseline5 Deliverable Finish", "Baseline5 Deliverable Start", "Baseline5 Duration", "Baseline5 Duration", "Baseline5 Finish", "Baseline5 Start", "Baseline5 Finish", "Baseline5 Fixed Cost", "Baseline5 Fixed Cost Accrual", "Baseline5 Start", "Baseline5 Work", "Baseline6 Budget Cost", "Baseline6 Budget Work", "Baseline6 Cost", "Baseline6 Deliverable Finish", "Baseline6 Deliverable Start", "Baseline6 Duration", "Baseline6 Duration", "Baseline6 Finish", "Baseline6 Start", "Baseline6 Finish", "Baseline6 Fixed Cost", "Baseline6 Fixed Cost Accrual", "Baseline6 Start", "Baseline6 Work", "Baseline7 Budget Cost", "Baseline7 Budget Work", "Baseline7 Cost", "Baseline7 Deliverable Finish", "Baseline7 Deliverable Start", "Baseline7 Duration", "Baseline7 Duration", "Baseline7 Finish", "Baseline7 Start", "Baseline7 Finish", "Baseline7 Fixed Cost", "Baseline7 Fixed Cost Accrual", "Baseline7 Start", "Baseline7 Work", "Baseline8 Budget Cost", "Baseline8 Budget Work", "Baseline8 Cost", "Baseline8 Deliverable Finish", "Baseline8 Deliverable Start", "Baseline8 Duration", "Baseline8 Duration", "Baseline8 Finish", "Baseline8 Start", "Baseline8 Finish", "Baseline8 Fixed Cost", "Baseline8 Fixed Cost Accrual", "Baseline8 Start", "Baseline8 Work", "Baseline9 Budget Cost", "Baseline9 Budget Work", "Baseline9 Cost", "Baseline9 Deliverable Finish", "Baseline9 Deliverable Start", "Baseline9 Duration", "Baseline9 Duration", "Baseline9 Finish", "Baseline9 Start", "Baseline9 Finish", "Baseline9 Fixed Cost", "Baseline9 Fixed Cost Accrual", "Baseline9 Start", "Baseline9 Work", "Confirmed", "Constraint Date", "Constraint Type", "Contact", "Cost", "Cost Rate Table", "Cost Variance", "Cost1", "Cost10", "Cost2", "Cost3", "Cost4", "Cost5", "Cost6", "Cost7", "Cost8", "Cost9", "CPI", "Created", "Critical", "CV", "CV%", "Date1", "Date10", "Date2", "Date3", "Date4", "Date5", "Date6", "Date7", "Date8", "Date9", "Deadline", "Deliverable Finish", "Deliverable GUID", "Deliverable Name", "Deliverable Start", "Deliverable Type", "Duration", "Duration Variance", "Duration1", "Duration10", "Duration2", "Duration3", "Duration4", "Duration5", "Duration6", "Duration7", "Duration8", "Duration9", "EAC", "Early Finish", "Early Start", "Earned Value Method", "Effort Driven", "Error Message", "Estimated", "External Task", "Finish", "Finish Slack", "Finish Variance", "Finish1", "Finish10", "Finish2", "Finish3", "Finish4", "Finish5", "Finish6", "Finish7", "Finish8", "Finish9", "Fixed Cost", "Fixed Cost Accrual", "Flag1", "Flag10", "Flag11", "Flag12", "Flag13", "Flag14", "Flag15", "Flag16")

        private val MPP12_TASK_COLUMNS_2 = arrayOf<String>(null, "Flag17", "Flag18", "Flag19", "Flag2", "Flag20", "Flag3", "Flag4", "Flag5", "Flag6", "Flag7", "Flag8", "Flag9", "Free Slack", "Group By Summary", "GUID", "Hide Bar", "Hyperlink", "Hyperlink Address", "Hyperlink Href", "Hyperlink SubAddress", "ID", "Ignore Resource Calendar", "<Unavailable>", "Indicators", "Late Finish", "Late Start", "Level Assignments", "Leveling Can Split", "Leveling Delay", "Linked Fields", "Marked", "Milestone", "Task Name", "Notes", "Number1", "Number10", "Number11", "Number12", "Number13", "Number14", "Number15", "Number16", "Number17", "Number18", "Number19", "Number2", "Number20", "Number3", "Number4", "Number5", "Number6", "Number7", "Number8", "Number9", "Objects", "Outline Code1", "Outline Code10", "Outline Code2", "Outline Code3", "Outline Code4", "Outline Code5", "Outline Code6", "Outline Code7", "Outline Code8", "Outline Code9", "Outline Level", "Outline Number", "Overallocated", "Overtime Cost", "Overtime Work", "<Unavailable>", "Physical % Complete", "<Unavailable>", "Predecessors", "Preleveled Finish", "Preleveled Start", "Priority", "Project", "Publish", "Recurring", "Regular Work", "Remaining Cost", "Remaining Duration", "Remaining Overtime Cost", "Remaining Overtime Work", "Remaining Work", "Request/Demand", "Resource Group", "Resource Initials", "Resource Names", "Resource Phonetics", "Resource Type", "Response Pending", "Resume", "Rollup", "Duration", "Finish", "Start", "SPI", "Start", "Start Slack", "Start Variance", "Start1", "Start10", "Start2", "Start3", "Start4", "Start5", "Start6", "Start7", "Start8", "Start9", "Status", "Status Indicator", "Status Manager", "Stop", "Subproject File", "Subproject Read Only", "Successors", "Summary", "SV", "SV%", "Task Calendar", "Task Calendar GUID", "TCPI", "TeamStatus Pending", "Text1", "Text10", "Text11", "Text12", "Text13", "Text14", "Text15", "Text16", "Text17", "Text18", "Text19", "Text2", "Text20", "Text21", "Text22", "Text23", "Text24", "Text25", "Text26", "Text27", "Text28", "Text29", "Text3", "Text30", "Text4", "Text5", "Text6", "Text7", "Text8", "Text9", "Total Slack", "Type", "Unique ID", "Unique ID Predecessors", "Unique ID Successors", "Update Needed", "VAC", "<Unavailable>", "WBS", "WBS Predecessors", "WBS Successors", "Work", "Work Contour", "Work Variance")

        private val MPP14_RESOURCE_COLUMNS_1 = arrayOf("ID", "Name", "% Work Complete", "Accrue At", "Active", "Actual Cost", "Actual Finish", "Actual Overtime Cost", "Actual Overtime Work", "Actual Start", "Actual Work", "ACWP", "Assignment", "Assignment Delay", "Assignment Owner", "Assignment Units", "Available From", "Available To", "Base Calendar", "Baseline Budget Cost", "Baseline Budget Work", "Baseline Cost", "Baseline Finish", "Baseline Start", "Baseline Work")

        private val MPP12_RESOURCE_COLUMNS_1 = arrayOf("ID", "Name", "% Work Complete", "Accrue At", "<Unavailable>", "Actual Cost", "Actual Finish", "Actual Overtime Cost", "Actual Overtime Work", "Actual Start", "Actual Work", "ACWP", "Assignment", "Assignment Delay", "Assignment Owner", "Assignment Units", "Available From", "Available To", "Base Calendar", "Baseline Budget Cost", "Baseline Budget Work", "Baseline Cost", "Baseline Finish", "Baseline Start", "Baseline Work")

        private val MPP14_RESOURCE_COLUMNS_2 = arrayOf("ID", "Name", "Baseline1 Budget Cost", "Baseline1 Budget Work", "Baseline1 Cost", "Baseline1 Finish", "Baseline1 Start", "Baseline1 Work", "Baseline10 Budget Cost", "Baseline10 Budget Work", "Baseline10 Cost", "Baseline10 Finish", "Baseline10 Start", "Baseline10 Work", "Baseline2 Budget Cost", "Baseline2 Budget Work", "Baseline2 Cost", "Baseline2 Finish", "Baseline2 Start", "Baseline2 Work", "Baseline3 Budget Cost", "Baseline3 Budget Work", "Baseline3 Cost", "Baseline3 Finish", "Baseline3 Start", "Baseline3 Work", "Baseline4 Budget Cost", "Baseline4 Budget Work", "Baseline4 Cost", "Baseline4 Finish", "Baseline4 Start", "Baseline4 Work")

        private val MPP14_RESOURCE_COLUMNS_3 = arrayOf("ID", "Name", "Baseline5 Budget Cost", "Baseline5 Budget Work", "Baseline5 Cost", "Baseline5 Finish", "Baseline5 Start", "Baseline5 Work", "Baseline6 Budget Cost", "Baseline6 Budget Work", "Baseline6 Cost", "Baseline6 Finish", "Baseline6 Start", "Baseline6 Work", "Baseline7 Budget Cost", "Baseline7 Budget Work", "Baseline7 Cost", "Baseline7 Finish", "Baseline7 Start", "Baseline7 Work", "Baseline8 Budget Cost", "Baseline8 Budget Work", "Baseline8 Cost", "Baseline8 Finish", "Baseline8 Start", "Baseline8 Work", "Baseline9 Budget Cost", "Baseline9 Budget Work", "Baseline9 Cost", "Baseline9 Finish", "Baseline9 Start", "Baseline9 Work")

        private val MPP14_RESOURCE_COLUMNS_4 = arrayOf("ID", "Name", "BCWP", "BCWS", "Booking Type", "Budget", "Budget Cost", "Budget Work", "Calendar GUID", "Can Level", "Code", "Confirmed", "Cost", "Cost Center", "Cost Per Use", "Cost Rate Table", "Cost Variance", "Cost1", "Cost10", "Cost2", "Cost3", "Cost4", "Cost5", "Cost6", "Cost7", "Cost8", "Cost9", "Created", "CV")

        private val MPP14_RESOURCE_COLUMNS_5 = arrayOf("ID", "Name", "Date1", "Date10", "Date2", "Date3", "Date4", "Date5", "Date6", "Date7", "Date8", "Default Assignment Owner", "Default Assignment Owner", "Duration1", "Duration10", "Duration2", "Duration3", "Duration4", "Duration5", "Duration6", "Duration7", "Duration8", "Duration9", "Email Address", "Enterprise", "Enterprise Base Calendar", "Enterprise Required Values", "Enterprise Team Member", "Enterprise Unique ID", "Error Message")

        private val MPP14_RESOURCE_COLUMNS_6 = arrayOf("ID", "Name", "Finish", "Finish1", "Finish10", "Finish2", "Finish3", "Finish4", "Finish5", "Finish6", "Finish7", "Finish8", "Finish9", "Flag1", "Flag10", "Flag11", "Flag12", "Flag13", "Flag14", "Flag15", "Flag16", "Flag17", "Flag18", "Flag19", "Flag2", "Flag20", "Flag3", "Flag4", "Flag5", "Flag6", "Flag7", "Flag8", "Flag9")

        private val MPP14_RESOURCE_COLUMNS_7 = arrayOf("ID", "Name", "Generic", "Group", "Group By Summary", "GUID", "Hyperlink", "Hyperlink Address", "Hyperlink Href", "Hyperlink SubAddress", "ID", "Import", "Inactive", "Indicators", "Initials", "Leveling Delay", "Linked Fields", "Material Label", "Max Units", "Name", "Notes", "Number1", "Number10", "Number11", "Number12", "Number13", "Number14", "Number15", "Number16", "Number17", "Number18", "Number19", "Number2", "Number20", "Number3", "Number4", "Number5", "Number6", "Number7", "Number8", "Number9")

        private val MPP14_RESOURCE_COLUMNS_8 = arrayOf("ID", "Name", "Objects", "Outline Code1", "Outline Code10", "Outline Code2", "Outline Code3", "Outline Code4", "Outline Code5", "Outline Code6", "Outline Code7", "Outline Code8", "Outline Code9", "Overallocated", "Overtime Cost", "Overtime Rate", "Overtime Work", "Peak", "Phonetics", "Project", "Regular Work", "Remaining Cost", "Remaining Overtime Cost", "Remaining Overtime Work", "Remaining Work", "Request/Demand", "Response Pending")

        private val MPP14_RESOURCE_COLUMNS_9 = arrayOf("ID", "Name", "Standard Rate", "Start", "Start1", "Start10", "Start2", "Start3", "Start4", "Start5", "Start6", "Start7", "Start8", "Start9", "Summary", "SV", "Task Outline Number", "Task Summary Name", "Team Assignment Pool", "TeamStatus Pending", "Text1", "Text10", "Text11", "Text12", "Text13", "Text14", "Text15", "Text16", "Text17", "Text18", "Text19", "Text2", "Text20", "Text21", "Text22", "Text23", "Text24", "Text25", "Text26", "Text27", "Text28", "Text29", "Text3", "Text30", "Text4", "Text5", "Text6", "Text7", "Text8", "Text9", "Type")

        private val MPP14_RESOURCE_COLUMNS_10 = arrayOf("ID", "Name", "Unique ID", "Update Needed", "VAC", "WBS", "Windows User Account", "Work", "Work Contour", "Work Variance")

        private val MPP14_RESOURCE_COLUMNS_11 = arrayOf("ID", "Name", "Enterprise Date1", "Enterprise Date2", "Enterprise Date3", "Enterprise Date4", "Enterprise Date5", "Enterprise Date6", "Enterprise Date7", "Enterprise Date8", "Enterprise Date9", "Enterprise Date10", "Enterprise Date11", "Enterprise Date12", "Enterprise Date13", "Enterprise Date14", "Enterprise Date15", "Enterprise Date16", "Enterprise Date17", "Enterprise Date18", "Enterprise Date19", "Enterprise Date20", "Enterprise Date21", "Enterprise Date22", "Enterprise Date23", "Enterprise Date24", "Enterprise Date25", "Enterprise Date26", "Enterprise Date27", "Enterprise Date28", "Enterprise Date29", "Enterprise Date30")

        private val MPP14_RESOURCE_COLUMNS_12 = arrayOf("ID", "Name", "Enterprise Cost1", "Enterprise Cost2", "Enterprise Cost3", "Enterprise Cost4", "Enterprise Cost5", "Enterprise Cost6", "Enterprise Cost7", "Enterprise Cost8", "Enterprise Cost9", "Enterprise Cost10", "Enterprise Duration1", "Enterprise Duration2", "Enterprise Duration3", "Enterprise Duration4", "Enterprise Duration5", "Enterprise Duration6", "Enterprise Duration7", "Enterprise Duration8", "Enterprise Duration9", "Enterprise Duration10")

        private val MPP14_RESOURCE_COLUMNS_13 = arrayOf("ID", "Name", "Enterprise Flag1", "Enterprise Flag2", "Enterprise Flag3", "Enterprise Flag4", "Enterprise Flag5", "Enterprise Flag6", "Enterprise Flag7", "Enterprise Flag8", "Enterprise Flag9", "Enterprise Flag10", "Enterprise Flag11", "Enterprise Flag12", "Enterprise Flag13", "Enterprise Flag14", "Enterprise Flag15", "Enterprise Flag16", "Enterprise Flag17", "Enterprise Flag18", "Enterprise Flag19", "Enterprise Flag20")

        private val MPP14_RESOURCE_COLUMNS_14 = arrayOf("ID", "Name", "Enterprise Number1", "Enterprise Number2", "Enterprise Number3", "Enterprise Number4", "Enterprise Number5", "Enterprise Number6", "Enterprise Number7", "Enterprise Number8", "Enterprise Number9", "Enterprise Number10", "Enterprise Number11", "Enterprise Number12", "Enterprise Number13", "Enterprise Number14", "Enterprise Number15", "Enterprise Number16", "Enterprise Number17", "Enterprise Number18", "Enterprise Number19", "Enterprise Number20", "Enterprise Number21", "Enterprise Number22", "Enterprise Number23", "Enterprise Number24", "Enterprise Number25", "Enterprise Number26", "Enterprise Number27", "Enterprise Number28", "Enterprise Number29", "Enterprise Number30", "Enterprise Number31", "Enterprise Number32", "Enterprise Number33", "Enterprise Number34", "Enterprise Number35", "Enterprise Number36", "Enterprise Number37", "Enterprise Number38", "Enterprise Number39", "Enterprise Number40")

        private val MPP14_RESOURCE_COLUMNS_15 = arrayOf("ID", "Name", "Enterprise Text1", "Enterprise Text2", "Enterprise Text3", "Enterprise Text4", "Enterprise Text5", "Enterprise Text6", "Enterprise Text7", "Enterprise Text8", "Enterprise Text9", "Enterprise Text10", "Enterprise Text11", "Enterprise Text12", "Enterprise Text13", "Enterprise Text14", "Enterprise Text15", "Enterprise Text16", "Enterprise Text17", "Enterprise Text18", "Enterprise Text19", "Enterprise Text20", "Enterprise Text21", "Enterprise Text22", "Enterprise Text23", "Enterprise Text24", "Enterprise Text25", "Enterprise Text26", "Enterprise Text27", "Enterprise Text28", "Enterprise Text29", "Enterprise Text30", "Enterprise Text31", "Enterprise Text32", "Enterprise Text33", "Enterprise Text34", "Enterprise Text35", "Enterprise Text36", "Enterprise Text37", "Enterprise Text38", "Enterprise Text39", "Enterprise Text40")
    }
}
