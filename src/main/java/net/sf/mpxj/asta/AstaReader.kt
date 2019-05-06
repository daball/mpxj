/*
 * file:       AstaReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       07/04/2011
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

package net.sf.mpxj.asta

import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Date
import java.util.HashMap
import java.util.LinkedList
import kotlin.collections.Map.Entry

import net.sf.mpxj.ChildTaskContainer
import net.sf.mpxj.ConstraintType
import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.DayType
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectCalendarWeek
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceType
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper

/**
 * This class provides a generic front end to read project data from
 * a database.
 */
internal class AstaReader {

    /**
     * Retrieves the project data read from this file.
     *
     * @return project data
     */
    val project: ProjectFile
    private val m_eventManager: EventManager

    /**
     * Constructor.
     */
    init {
        project = ProjectFile()
        m_eventManager = project.eventManager

        val config = project.projectConfig

        config.autoTaskUniqueID = false
        config.autoResourceUniqueID = false

        config.autoCalendarUniqueID = false

        project.projectProperties.fileApplication = "Asta"
        project.projectProperties.fileType = "PP"

        val fields = project.customFields
        fields.getCustomField(TaskField.TEXT1).setAlias("Code")
    }

    /**
     * Process project properties.
     *
     * @param row project properties data.
     */
    fun processProjectProperties(row: Row) {
        val ph = project.projectProperties
        ph.duration = row.getDuration("DURATIONHOURS")
        ph.startDate = row.getDate("STARU")
        ph.finishDate = row.getDate("ENE")
        ph.name = row.getString("SHORT_NAME")
        ph.author = row.getString("PROJECT_BY")
        //DURATION_TIME_UNIT
        ph.lastSaved = row.getDate("LAST_EDITED_DATE")
    }

    /**
     * Process resources.
     *
     * @param permanentRows permanent resource data
     * @param consumableRows consumable resource data
     */
    fun processResources(permanentRows: List<Row>, consumableRows: List<Row>) {
        //
        // Process permanent resources
        //
        for (row in permanentRows) {
            val resource = project.addResource()
            resource.type = ResourceType.WORK
            resource.uniqueID = row.getInteger("PERMANENT_RESOURCEID")
            resource.emailAddress = row.getString("EMAIL_ADDRESS")
            // EFFORT_TIME_UNIT
            resource.name = row.getString("NASE")
            resource.resourceCalendar = deriveResourceCalendar(row.getInteger("CALENDAV"))
            resource.maxUnits = Double.valueOf(row.getDouble("AVAILABILITY").doubleValue() * 100)
            resource.generic = row.getBoolean("CREATED_AS_FOLDER")
            resource.initials = getInitials(resource.name)
        }

        //
        // Process groups
        //
        /*
            for (Row row : permanentRows)
            {
               Resource resource = m_project.getResourceByUniqueID(row.getInteger("PERMANENT_RESOURCEID"));
               Resource group = m_project.getResourceByUniqueID(row.getInteger("ROLE"));
               if (resource != null && group != null)
               {
                  resource.setGroup(group.getName());
               }
            }
      */
        //
        // Process consumable resources
        //
        for (row in consumableRows) {
            val resource = project.addResource()
            resource.type = ResourceType.MATERIAL
            resource.uniqueID = row.getInteger("CONSUMABLE_RESOURCEID")
            resource.costPerUse = row.getDouble("COST_PER_USEDEFAULTSAMOUNT")
            resource.peakUnits = Double.valueOf(row.getDouble("AVAILABILITY").doubleValue() * 100)
            resource.name = row.getString("NASE")
            resource.resourceCalendar = deriveResourceCalendar(row.getInteger("CALENDAV"))
            resource.availableFrom = row.getDate("AVAILABLE_FROM")
            resource.availableTo = row.getDate("AVAILABLE_TO")
            resource.generic = row.getBoolean("CREATED_AS_FOLDER")
            resource.materialLabel = row.getString("MEASUREMENT")
            resource.initials = getInitials(resource.name)
        }
    }

    /**
     * Derive a calendar for a resource.
     *
     * @param parentCalendarID calendar from which resource calendar is derived
     * @return new calendar for a resource
     */
    private fun deriveResourceCalendar(parentCalendarID: Integer): ProjectCalendar {
        val calendar = project.addDefaultDerivedCalendar()
        calendar.uniqueID = Integer.valueOf(project.projectConfig.nextCalendarUniqueID)
        calendar.parent = project.getCalendarByUniqueID(parentCalendarID)
        return calendar
    }

    /**
     * Organises the data from Asta into a hierarchy and converts this into tasks.
     *
     * @param bars bar data
     * @param expandedTasks expanded task data
     * @param tasks task data
     * @param milestones milestone data
     */
    fun processTasks(bars: List<Row>, expandedTasks: List<Row>, tasks: List<Row>, milestones: List<Row>) {
        val parentBars = buildRowHierarchy(bars, expandedTasks, tasks, milestones)
        createTasks(project, "", parentBars)
        deriveProjectCalendar()
        updateStructure()
    }

    /**
     * Builds the task hierarchy.
     *
     * Note that there are two distinct levels of organisation going on here. The first is the
     * Asta "summary" organisation, where the user organises bars into summary groups. We are using this
     * to create our hierarchy of tasks.
     *
     * The second level displayed within a summary group (or at the project level if the user has not
     * created summary groups) is the WBS. At the moment we are not including the WBS in the hierarchy.
     *
     * @param bars bar data
     * @param expandedTasks expanded task data
     * @param tasks task data
     * @param milestones milestone data
     * @return list containing the top level tasks
     */
    private fun buildRowHierarchy(bars: List<Row>, expandedTasks: List<Row>, tasks: List<Row>, milestones: List<Row>): List<Row> {
        //
        // Create a list of leaf nodes by merging the task and milestone lists
        //
        val leaves = ArrayList<Row>()
        leaves.addAll(tasks)
        leaves.addAll(milestones)

        //
        // Sort the bars and the leaves
        //
        Collections.sort(bars, BAR_COMPARATOR)
        Collections.sort(leaves, LEAF_COMPARATOR)

        //
        // Map bar IDs to bars
        //
        val barIdToBarMap = HashMap<Integer, Row>()
        for (bar in bars) {
            barIdToBarMap.put(bar.getInteger("BARID"), bar)
        }

        //
        // Merge expanded task attributes with parent bars
        // and create an expanded task ID to bar map.
        //
        val expandedTaskIdToBarMap = HashMap<Integer, Row>()
        for (expandedTask in expandedTasks) {
            val bar = barIdToBarMap.get(expandedTask.getInteger("BAR"))
            bar.merge(expandedTask, "_")
            val expandedTaskID = bar.getInteger("_EXPANDED_TASKID")
            expandedTaskIdToBarMap.put(expandedTaskID, bar)
        }

        //
        // Build the hierarchy
        //
        var parentBars: List<Row> = ArrayList<Row>()
        for (bar in bars) {
            val expandedTaskID = bar.getInteger("EXPANDED_TASK")
            val parentBar = expandedTaskIdToBarMap.get(expandedTaskID)
            if (parentBar == null) {
                parentBars.add(bar)
            } else {
                parentBar!!.addChild(bar)
            }
        }

        //
        // Attach the leaves
        //
        for (leaf in leaves) {
            val barID = leaf.getInteger("BAR")
            val bar = barIdToBarMap.get(barID)
            bar.addChild(leaf)
        }

        //
        // Prune any "displaced items" from the top level.
        // We're using a heuristic here as this is the only thing I
        // can see which differs between bars that we want to include
        // and bars that we want to exclude.
        //
        val iter = parentBars.iterator()
        while (iter.hasNext()) {
            val bar = iter.next()
            val barName = bar.getString("NAMH")
            if (barName == null || barName.isEmpty() || barName.equals("Displaced Items")) {
                iter.remove()
            }
        }

        //
        // If we only have a single top level node (effectively a summary task) prune that too.
        //
        if (parentBars.size() === 1) {
            parentBars = parentBars[0].childRows
        }

        return parentBars
    }

    /**
     * Recursively descend through  the hierarchy creating tasks.
     *
     * @param parent parent task
     * @param parentName parent name
     * @param rows rows to add as tasks to this parent
     */
    private fun createTasks(parent: ChildTaskContainer, parentName: String, rows: List<Row>) {
        for (row in rows) {
            val rowIsBar = row.getInteger("BARID") != null

            //
            // Don't export hammock tasks.
            //
            if (rowIsBar && row.childRows.isEmpty()) {
                continue
            }

            val task = parent.addTask()

            //
            // Do we have a bar, task, or milestone?
            //
            if (rowIsBar) {
                //
                // If the bar only has one child task, we skip it and add the task directly
                //
                if (skipBar(row)) {
                    populateLeaf(row.getString("NAMH"), row.childRows.get(0), task)
                } else {
                    populateBar(row, task)
                    createTasks(task, task.name, row.childRows)
                }
            } else {
                populateLeaf(parentName, row, task)
            }

            m_eventManager.fireTaskReadEvent(task)
        }
    }

    /**
     * Returns true if we should skip this bar, i.e. the bar only has a single child task.
     *
     * @param row bar row to test
     * @return true if this bar should be skipped
     */
    private fun skipBar(row: Row): Boolean {
        val childRows = row.childRows
        return childRows.size() === 1 && childRows.get(0).getChildRows().isEmpty()
    }

    /**
     * Adds a leaf node, which could be a task or a milestone.
     *
     * @param parentName parent bar name
     * @param row row to add
     * @param task task to populate with data from the row
     */
    private fun populateLeaf(parentName: String, row: Row, task: Task) {
        if (row.getInteger("TASKID") != null) {
            populateTask(row, task)
        } else {
            populateMilestone(row, task)
        }

        val name = task.name
        if (name == null || name.isEmpty()) {
            task.name = parentName
        }
    }

    /**
     * Populate a task from a Row instance.
     *
     * @param row Row instance
     * @param task Task instance
     */
    private fun populateTask(row: Row, task: Task) {
        //"PROJID"
        task.uniqueID = row.getInteger("TASKID")
        //GIVEN_DURATIONTYPF
        //GIVEN_DURATIONELA_MONTHS
        task.duration = row.getDuration("GIVEN_DURATIONHOURS")
        task.resume = row.getDate("RESUME")
        //task.setStart(row.getDate("GIVEN_START"));
        //LATEST_PROGRESS_PERIOD
        //TASK_WORK_RATE_TIME_UNIT
        //TASK_WORK_RATE
        //PLACEMENT
        //BEEN_SPLIT
        //INTERRUPTIBLE
        //HOLDING_PIN
        ///ACTUAL_DURATIONTYPF
        //ACTUAL_DURATIONELA_MONTHS
        task.actualDuration = row.getDuration("ACTUAL_DURATIONHOURS")
        task.earlyStart = row.getDate("EARLY_START_DATE")
        task.lateStart = row.getDate("LATE_START_DATE")
        //FREE_START_DATE
        //START_CONSTRAINT_DATE
        //END_CONSTRAINT_DATE
        //task.setBaselineWork(row.getDuration("EFFORT_BUDGET"));
        //NATURAO_ORDER
        //LOGICAL_PRECEDENCE
        //SPAVE_INTEGER
        //SWIM_LANE
        //USER_PERCENT_COMPLETE
        task.percentageComplete = row.getDouble("OVERALL_PERCENV_COMPLETE")
        //OVERALL_PERCENT_COMPL_WEIGHT
        task.name = row.getString("NARE")
        task.notes = getNotes(row)
        task.setText(1, row.getString("UNIQUE_TASK_ID"))
        task.calendar = project.getCalendarByUniqueID(row.getInteger("CALENDAU"))
        //EFFORT_TIMI_UNIT
        //WORL_UNIT
        //LATEST_ALLOC_PROGRESS_PERIOD
        //WORN
        //BAR
        //CONSTRAINU
        //PRIORITB
        //CRITICAM
        //USE_PARENU_CALENDAR
        //BUFFER_TASK
        //MARK_FOS_HIDING
        //OWNED_BY_TIMESHEEV_X
        //START_ON_NEX_DAY
        //LONGEST_PATH
        //DURATIOTTYPF
        //DURATIOTELA_MONTHS
        //DURATIOTHOURS
        task.start = row.getDate("STARZ")
        task.finish = row.getDate("ENJ")
        //DURATION_TIMJ_UNIT
        //UNSCHEDULABLG
        //SUBPROJECT_ID
        //ALT_ID
        //LAST_EDITED_DATE
        //LAST_EDITED_BY

        processConstraints(row, task)

        if (NumberHelper.getInt(task.percentageComplete) != 0) {
            task.actualStart = task.start
            if (task.percentageComplete.intValue() === 100) {
                task.actualFinish = task.finish
                task.duration = task.actualDuration
            }
        }
    }

    /**
     * Uses data from a bar to populate a task.
     *
     * @param row bar data
     * @param task task to populate
     */
    private fun populateBar(row: Row, task: Task) {
        val calendarID = row.getInteger("CALENDAU")
        val calendar = project.getCalendarByUniqueID(calendarID)

        //PROJID
        task.uniqueID = row.getInteger("BARID")
        task.start = row.getDate("STARV")
        task.finish = row.getDate("ENF")
        //NATURAL_ORDER
        //SPARI_INTEGER
        task.name = row.getString("NAMH")
        //EXPANDED_TASK
        //PRIORITY
        //UNSCHEDULABLE
        //MARK_FOR_HIDING
        //TASKS_MAY_OVERLAP
        //SUBPROJECT_ID
        //ALT_ID
        //LAST_EDITED_DATE
        //LAST_EDITED_BY
        //Proc_Approve
        //Proc_Design_info
        //Proc_Proc_Dur
        //Proc_Procurement
        //Proc_SC_design
        //Proc_Select_SC
        //Proc_Tender
        //QA Checked
        //Related_Documents
        task.calendar = calendar
    }

    /**
     * Populate a milestone from a Row instance.
     *
     * @param row Row instance
     * @param task Task instance
     */
    private fun populateMilestone(row: Row, task: Task) {
        task.milestone = true
        //PROJID
        task.uniqueID = row.getInteger("MILESTONEID")
        task.start = row.getDate("GIVEN_DATE_TIME")
        task.finish = row.getDate("GIVEN_DATE_TIME")
        //PROGREST_PERIOD
        //SYMBOL_APPEARANCE
        //MILESTONE_TYPE
        //PLACEMENU
        task.percentageComplete = if (row.getBoolean("COMPLETED")) COMPLETE else INCOMPLETE
        //INTERRUPTIBLE_X
        //ACTUAL_DURATIONTYPF
        //ACTUAL_DURATIONELA_MONTHS
        //ACTUAL_DURATIONHOURS
        task.earlyStart = row.getDate("EARLY_START_DATE")
        task.lateStart = row.getDate("LATE_START_DATE")
        //FREE_START_DATE
        //START_CONSTRAINT_DATE
        //END_CONSTRAINT_DATE
        //EFFORT_BUDGET
        //NATURAO_ORDER
        //LOGICAL_PRECEDENCE
        //SPAVE_INTEGER
        //SWIM_LANE
        //USER_PERCENT_COMPLETE
        //OVERALL_PERCENV_COMPLETE
        //OVERALL_PERCENT_COMPL_WEIGHT
        task.name = row.getString("NARE")
        //NOTET
        task.setText(1, row.getString("UNIQUE_TASK_ID"))
        task.calendar = project.getCalendarByUniqueID(row.getInteger("CALENDAU"))
        //EFFORT_TIMI_UNIT
        //WORL_UNIT
        //LATEST_ALLOC_PROGRESS_PERIOD
        //WORN
        //CONSTRAINU
        //PRIORITB
        //CRITICAM
        //USE_PARENU_CALENDAR
        //BUFFER_TASK
        //MARK_FOS_HIDING
        //OWNED_BY_TIMESHEEV_X
        //START_ON_NEX_DAY
        //LONGEST_PATH
        //DURATIOTTYPF
        //DURATIOTELA_MONTHS
        //DURATIOTHOURS
        //STARZ
        //ENJ
        //DURATION_TIMJ_UNIT
        //UNSCHEDULABLG
        //SUBPROJECT_ID
        //ALT_ID
        //LAST_EDITED_DATE
        //LAST_EDITED_BY
        task.duration = Duration.getInstance(0, TimeUnit.HOURS)
    }

    /**
     * Iterates through the tasks setting the correct
     * outline level and ID values.
     */
    private fun updateStructure() {
        var id = 1
        val outlineLevel = Integer.valueOf(1)
        for (task in project.childTasks) {
            id = updateStructure(id, task, outlineLevel)
        }
    }

    /**
     * Iterates through the tasks setting the correct
     * outline level and ID values.
     *
     * @param id current ID value
     * @param task current task
     * @param outlineLevel current outline level
     * @return next ID value
     */
    private fun updateStructure(id: Int, task: Task, outlineLevel: Integer): Int {
        var id = id
        var outlineLevel = outlineLevel
        task.id = Integer.valueOf(id++)
        task.outlineLevel = outlineLevel
        outlineLevel = Integer.valueOf(outlineLevel.intValue() + 1)
        for (childTask in task.childTasks) {
            id = updateStructure(id, childTask, outlineLevel)
        }
        return id
    }

    /**
     * Processes predecessor data.
     *
     * @param rows predecessor data
     * @param completedSections completed section data
     */
    fun processPredecessors(rows: List<Row>, completedSections: List<Row>) {
        val completedSectionMap = HashMap<Integer, Integer>()
        for (section in completedSections) {
            completedSectionMap.put(section.getInteger("TASK_COMPLETED_SECTIONID"), section.getInteger("TASK"))
        }

        for (row in rows) {
            var startTaskID: Integer? = row.getInteger("START_TASK")
            var startTask: Task? = project.getTaskByUniqueID(startTaskID)
            if (startTask == null) {
                startTaskID = completedSectionMap.get(startTaskID)
                if (startTaskID != null) {
                    startTask = project.getTaskByUniqueID(startTaskID)
                }
            }

            var endTaskID: Integer? = row.getInteger("END_TASK")
            var endTask: Task? = project.getTaskByUniqueID(endTaskID)
            if (endTask == null) {
                endTaskID = completedSectionMap.get(endTaskID)
                if (endTaskID != null) {
                    endTask = project.getTaskByUniqueID(endTaskID)
                }
            }

            if (startTask != null && endTask != null) {
                val type = getRelationType(row.getInt("TYPI"))

                val startLag = row.getDuration("START_LAG_TIMEHOURS").getDuration()
                val endLag = row.getDuration("END_LAG_TIMEHOURS").getDuration()
                val totalLag = startLag - endLag

                val relation = endTask.addPredecessor(startTask, type, Duration.getInstance(totalLag, TimeUnit.HOURS))
                relation.uniqueID = row.getInteger("LINKID")
            }

            //PROJID
            //LINKID
            //START_LAG_TIMETYPF
            //START_LAG_TIMEELA_MONTHS
            //START_LAG_TIMEHOURS
            //END_LAG_TIMETYPF
            //END_LAG_TIMEELA_MONTHS
            //END_LAG_TIMEHOURS
            //MAXIMUM_LAGTYPF
            //MAXIMUM_LAGELA_MONTHS
            //MAXIMUM_LAGHOURS
            //STARV_DATE
            //ENF_DATE
            //CURVATURE_PERCENTAGE
            //START_LAG_PERCENT_FLOAT
            //END_LAG_PERCENT_FLOAT
            //COMMENTS
            //LINK_CATEGORY
            //START_LAG_TIME_UNIT
            //END_LAG_TIME_UNIT
            //MAXIMUM_LAG_TIME_UNIT
            //START_TASK
            //END_TASK
            //TYPI
            //START_LAG_TYPE
            //END_LAG_TYPE
            //MAINTAIN_TASK_OFFSETS
            //UNSCHEDULABLF
            //CRITICAL
            //ON_LOOP
            //MAXIMUM_LAG_MODE
            //ANNOTATE_LEAD_LAG
            //START_REPOSITION_ON_TAS_MOVE
            //END_REPOSITION_ON_TASK_MOVE
            //DRAW_CURVED_IF_VERTICAL
            //AUTOMATIC_CURVED_LI_SETTINGS
            //DRAW_CURVED_LINK_TO_LEFT
            //LOCAL_LINK
            //DRIVING
            //ALT_ID
            //LAST_EDITED_DATE
            //LAST_EDITED_BY
        }
    }

    /**
     * Process assignment data.
     *
     * @param permanentAssignments assignment data
     */
    fun processAssignments(permanentAssignments: List<Row>) {
        for (row in permanentAssignments) {
            val task = project.getTaskByUniqueID(row.getInteger("ALLOCATEE_TO"))
            val resource = project.getResourceByUniqueID(row.getInteger("PLAYER"))
            if (task != null && resource != null) {
                val percentComplete = row.getDouble("PERCENT_COMPLETE").doubleValue()
                val work = row.getWork("EFFORW")
                val actualWork = work.getDuration() * percentComplete
                val remainingWork = work.getDuration() - actualWork

                val assignment = task.addResourceAssignment(resource)
                assignment.uniqueID = row.getInteger("PERMANENT_SCHEDUL_ALLOCATIONID")
                assignment.start = row.getDate("STARZ")
                assignment.finish = row.getDate("ENJ")
                assignment.units = Double.valueOf(row.getDouble("GIVEN_ALLOCATION").doubleValue() * 100)
                assignment.delay = row.getDuration("DELAAHOURS")
                assignment.percentageWorkComplete = Double.valueOf(percentComplete * 100)
                assignment.work = work
                assignment.actualWork = Duration.getInstance(actualWork, work.getUnits())
                assignment.remainingWork = Duration.getInstance(remainingWork, work.getUnits())

            }

            //PROJID
            //REQUIREE_BY
            //OWNED_BY_TIMESHEET_X
            //EFFORW
            //GIVEN_EFFORT
            //WORK_FROM_TASK_FACTOR
            //ALLOCATIOO
            //GIVEN_ALLOCATION
            //ALLOCATIOP_OF
            //WORM_UNIT
            //WORK_RATE_TIMF_UNIT
            //EFFORT_TIMJ_UNIT
            //WORO
            //GIVEN_WORK
            //WORL_RATE
            //GIVEN_WORK_RATE
            //TYPV
            //CALCULATEG_PARAMETER
            //BALANCINJ_PARAMETER
            //SHAREE_EFFORT
            //CONTRIBUTES_TO_ACTIVI_EFFORT
            //DELAATYPF
            //DELAAELA_MONTHS
            //DELAAHOURS
            //GIVEO_DURATIONTYPF
            //GIVEO_DURATIONELA_MONTHS
            //GIVEO_DURATIONHOURS
            //DELAY_TIMI_UNIT
            //RATE_TYPE
            //USE_TASM_CALENDAR
            //IGNORF
            //ELAPSEE
            //MAY_BE_SHORTER_THAN_TASK
            //RESUMF
            //SPAXE_INTEGER
            //USER_PERCENU_COMPLETE
            //ALLOCATIOR_GROUP
            //PRIORITC
            //ACCOUNTED_FOR_ELSEWHERE
            //DURATIOTTYPF
            //DURATIOTELA_MONTHS
            //DURATIOTHOURS
            //DURATION_TIMJ_UNIT
            //UNSCHEDULABLG
            //SUBPROJECT_ID
            //permanent_schedul_allocation_ALT_ID
            //permanent_schedul_allocation_LAST_EDITED_DATE
            //permanent_schedul_allocation_LAST_EDITED_BY
            //perm_resource_skill_PROJID
            //PERM_RESOURCE_SKILLID
            //ARR_STOUT_STSKI_APARROW_TYPE
            //ARR_STOUT_STSKI_APLENGTH
            //ARR_STOUT_STSKI_APEDGE
            //ARR_STOUT_STSKI_APBORDET_COL
            //ARR_STOUT_STSKI_APINSIDG_COL
            //ARR_STOUT_STSKI_APPLACEMENW
            //BLI_STOUT_STSKI_APBLIP_TYPE
            //BLI_STOUT_STSKI_APSCALEY
            //BLI_STOUT_STSKI_APSCALEZ
            //BLI_STOUT_STSKI_APGAP
            //BLI_STOUT_STSKI_APBORDES_COL
            //BLI_STOUT_STSKI_APINSIDF_COL
            //BLI_STOUT_STSKI_APPLACEMENV
            //LIN_STOUT_STSKI_APSCALEX
            //LIN_STOUT_STSKI_APWIDTH
            //LIN_STOUT_STSKI_APBORDER_COL
            //LIN_STOUT_STSKI_APINSIDE_COL
            //LIN_STOUT_STSKI_APLINE_TYPE
            //SKI_APFOREGROUND_FILL_COLOUR
            //SKI_APBACKGROUND_FILL_COLOUR
            //SKI_APPATTERN
            //DURATIOODEFAULTTTYPF
            //DURATIOODEFAULTTELA_MONTHS
            //DURATIOODEFAULTTHOURS
            //DELAYDEFAULTTTYPF
            //DELAYDEFAULTTELA_MONTHS
            //DELAYDEFAULTTHOURS
            //DEFAULTTALLOCATION
            //DEFAULTTWORK_FROM_ACT_FACTOR
            //DEFAULTTEFFORT
            //DEFAULTTWORL
            //DEFAULTTWORK_RATE
            //DEFAULTTWORK_UNIT
            //DEFAULTTWORK_RATE_TIME_UNIT
            //DEFAULTTEFFORT_TIMG_UNIT
            //DEFAULTTDURATION_TIMF_UNIT
            //DEFAULTTDELAY_TIME_UNIT
            //DEFAULTTTYPL
            //DEFAULTTCALCULATED_PARAMETER
            //DEFAULTTBALANCING_PARAMETER
            //DEFAULTTWORK_RATE_TYPE
            //DEFAULTTUSE_TASK_CALENDAR
            //DEFAULTTALLOC_PROPORTIONALLY
            //DEFAULTTCAN_BE_SPLIT
            //DEFAULTTCAN_BE_DELAYED
            //DEFAULTTCAN_BE_STRETCHED
            //DEFAULTTACCOUNTED__ELSEWHERE
            //DEFAULTTCONTRIBUTES_T_EFFORT
            //DEFAULTTMAY_BE_SHORTER__TASK
            //DEFAULTTSHARED_EFFORT
            //ABILITY
            //EFFECTIVENESS
            //AVAILABLF_FROM
            //AVAILABLF_TO
            //SPARO_INTEGER
            //EFFORT_TIMF_UNIT
            //ROLE
            //CREATED_AS_FOLDER
            //perm_resource_skill_ALT_ID
            //perm_resource_skill_LAST_EDITED_DATE
            //perm_resource_skill_LAST_EDITED_BY

        }
    }

    /**
     * Convert an integer into a RelationType instance.
     *
     * @param index integer value
     * @return RelationType instance
     */
    private fun getRelationType(index: Int): RelationType {
        var index = index
        if (index < 0 || index > RELATION_TYPES.size) {
            index = 0
        }

        return RELATION_TYPES[index]
    }

    /**
     * Convert a name into initials.
     *
     * @param name source name
     * @return initials
     */
    private fun getInitials(name: String?): String? {
        var result: String? = null

        if (name != null && name.length() !== 0) {
            val sb = StringBuilder()
            sb.append(name.charAt(0))
            var index = 1
            while (true) {
                index = name.indexOf(' ', index)
                if (index == -1) {
                    break
                }

                ++index
                if (index < name.length() && name.charAt(index) !== ' ') {
                    sb.append(name.charAt(index))
                }

                ++index
            }

            result = sb.toString()
        }

        return result
    }

    /**
     * Asta Powerproject assigns an explicit calendar for each task. This method
     * is used to find the most common calendar and use this as the default project
     * calendar. This allows the explicitly assigned task calendars to be removed.
     */
    private fun deriveProjectCalendar() {
        //
        // Count the number of times each calendar is used
        //
        val map = HashMap<ProjectCalendar, Integer>()
        for (task in project.tasks) {
            val calendar = task.calendar
            var count = map.get(calendar)
            if (count == null) {
                count = Integer.valueOf(1)
            } else {
                count = Integer.valueOf(count!!.intValue() + 1)
            }
            map.put(calendar, count)
        }

        //
        // Find the most frequently used calendar
        //
        var maxCount = 0
        var defaultCalendar: ProjectCalendar? = null

        for (entry in map.entrySet()) {
            if (entry.getValue().intValue() > maxCount) {
                maxCount = entry.getValue().intValue()
                defaultCalendar = entry.getKey()
            }
        }

        //
        // Set the default calendar for the project
        // and remove it's use as a task-specific calendar.
        //
        if (defaultCalendar != null) {
            project.defaultCalendar = defaultCalendar
            for (task in project.tasks) {
                if (task.calendar == defaultCalendar) {
                    task.calendar = null
                }
            }
        }
    }

    /**
     * Determines the constraints relating to a task.
     *
     * @param row row data
     * @param task Task instance
     */
    private fun processConstraints(row: Row, task: Task) {
        var constraintType = ConstraintType.AS_SOON_AS_POSSIBLE
        var constraintDate: Date? = null

        when (row.getInt("CONSTRAINU")) {
            0 -> {
                if (row.getInt("PLACEMENT") == 0) {
                    constraintType = ConstraintType.AS_SOON_AS_POSSIBLE
                } else {
                    constraintType = ConstraintType.AS_LATE_AS_POSSIBLE
                }
            }

            1 -> {
                constraintType = ConstraintType.MUST_START_ON
                constraintDate = row.getDate("START_CONSTRAINT_DATE")
            }

            2 -> {
                constraintType = ConstraintType.START_NO_LATER_THAN
                constraintDate = row.getDate("START_CONSTRAINT_DATE")
            }

            3 -> {
                constraintType = ConstraintType.START_NO_EARLIER_THAN
                constraintDate = row.getDate("START_CONSTRAINT_DATE")
            }

            4 -> {
                constraintType = ConstraintType.MUST_FINISH_ON
                constraintDate = row.getDate("END_CONSTRAINT_DATE")
            }

            5 -> {
                constraintType = ConstraintType.FINISH_NO_LATER_THAN
                constraintDate = row.getDate("END_CONSTRAINT_DATE")
            }

            6 -> {
                constraintType = ConstraintType.FINISH_NO_EARLIER_THAN
                constraintDate = row.getDate("END_CONSTRAINT_DATE")
            }

            8 -> {
                task.deadline = row.getDate("END_CONSTRAINT_DATE")
            }
        }

        task.constraintType = constraintType
        task.constraintDate = constraintDate
    }

    /**
     * Creates a mapping between exception ID values and working/non-working days.
     *
     * @param rows rows from the exceptions table
     * @return exception map
     */
    fun createExceptionTypeMap(rows: List<Row>): Map<Integer, DayType> {
        val map = HashMap<Integer, DayType>()
        for (row in rows) {
            val id = row.getInteger("EXCEPTIONNID")
            val result: DayType

            when (row.getInt("UNIQUE_BIT_FIELD")) {
                8 // Working
                    , 32 // Overtime
                    , 128 //Weekend Working
                -> {
                    result = DayType.WORKING
                }

                4 // Non Working
                    , 16 // Holiday
                    , 64 // Weather
                    , --2147483648 // Weekend
                -> {
                    result = DayType.NON_WORKING
                }
                else -> {
                    result = DayType.NON_WORKING
                }
            }

            map.put(id, result)
        }
        return map
    }

    /**
     * Creates a map of work pattern rows indexed by the primary key.
     *
     * @param rows work pattern rows
     * @return work pattern map
     */
    fun createWorkPatternMap(rows: List<Row>): Map<Integer, Row> {
        val map = HashMap<Integer, Row>()
        for (row in rows) {
            map.put(row.getInteger("WORK_PATTERNID"), row)
        }
        return map
    }

    /**
     * Creates a map between a calendar ID and a list of
     * work pattern assignment rows.
     *
     * @param rows work pattern assignment rows
     * @return work pattern assignment map
     */
    fun createWorkPatternAssignmentMap(rows: List<Row>): Map<Integer, List<Row>> {
        val map = HashMap<Integer, List<Row>>()
        for (row in rows) {
            val calendarID = row.getInteger("WORK_PATTERN_ASSIGNMENTID")
            var list = map.get(calendarID)
            if (list == null) {
                list = LinkedList<Row>()
                map.put(calendarID, list)
            }
            list!!.add(row)
        }
        return map
    }

    /**
     * Creates a map between a calendar ID and a list of exception assignment rows.
     *
     * @param rows exception assignment rows
     * @return exception assignment map
     */
    fun createExceptionAssignmentMap(rows: List<Row>): Map<Integer, List<Row>> {
        val map = HashMap<Integer, List<Row>>()
        for (row in rows) {
            val calendarID = row.getInteger("EXCEPTION_ASSIGNMENTID")
            var list = map.get(calendarID)
            if (list == null) {
                list = LinkedList<Row>()
                map.put(calendarID, list)
            }
            list!!.add(row)
        }
        return map
    }

    /**
     * Creates a map between a work pattern ID and a list of time entry rows.
     *
     * @param rows time entry rows
     * @return time entry map
     */
    fun createTimeEntryMap(rows: List<Row>): Map<Integer, List<Row>> {
        val map = HashMap<Integer, List<Row>>()
        for (row in rows) {
            val workPatternID = row.getInteger("TIME_ENTRYID")
            var list = map.get(workPatternID)
            if (list == null) {
                list = LinkedList<Row>()
                map.put(workPatternID, list)
            }
            list!!.add(row)
        }
        return map
    }

    /**
     * Creates a ProjectCalendar instance from the Asta data.
     *
     * @param calendarRow basic calendar data
     * @param workPatternMap work pattern map
     * @param workPatternAssignmentMap work pattern assignment map
     * @param exceptionAssignmentMap exception assignment map
     * @param timeEntryMap time entry map
     * @param exceptionTypeMap exception type map
     */
    fun processCalendar(calendarRow: Row, workPatternMap: Map<Integer, Row>, workPatternAssignmentMap: Map<Integer, List<Row>>, exceptionAssignmentMap: Map<Integer, List<Row>>, timeEntryMap: Map<Integer, List<Row>>, exceptionTypeMap: Map<Integer, DayType>) {
        //
        // Create the calendar and add the default working hours
        //
        val calendar = project.addCalendar()
        val dominantWorkPatternID = calendarRow.getInteger("DOMINANT_WORK_PATTERN")
        calendar.uniqueID = calendarRow.getInteger("CALENDARID")
        processWorkPattern(calendar, dominantWorkPatternID, workPatternMap, timeEntryMap, exceptionTypeMap)
        calendar.name = calendarRow.getString("NAMK")

        //
        // Add any additional working weeks
        //
        var rows = workPatternAssignmentMap[calendar.uniqueID]
        if (rows != null) {
            for (row in rows) {
                val workPatternID = row.getInteger("WORK_PATTERN")
                if (!workPatternID.equals(dominantWorkPatternID)) {
                    val week = calendar.addWorkWeek()
                    week.dateRange = DateRange(row.getDate("START_DATE"), row.getDate("END_DATE"))
                    processWorkPattern(week, workPatternID, workPatternMap, timeEntryMap, exceptionTypeMap)
                }
            }
        }

        //
        // Add exceptions - not sure how exceptions which turn non-working days into working days are handled by Asta - if at all?
        //
        rows = exceptionAssignmentMap[calendar.uniqueID]
        if (rows != null) {
            for (row in rows) {
                val startDate = row.getDate("STARU_DATE")
                val endDate = row.getDate("ENE_DATE")
                calendar.addCalendarException(startDate, endDate)
            }
        }

        m_eventManager.fireCalendarReadEvent(calendar)
    }

    /**
     * Populates a ProjectCalendarWeek instance from Asta work pattern data.
     *
     * @param week target ProjectCalendarWeek instance
     * @param workPatternID target work pattern ID
     * @param workPatternMap work pattern data
     * @param timeEntryMap time entry map
     * @param exceptionTypeMap exception type map
     */
    private fun processWorkPattern(week: ProjectCalendarWeek, workPatternID: Integer, workPatternMap: Map<Integer, Row>, timeEntryMap: Map<Integer, List<Row>>, exceptionTypeMap: Map<Integer, DayType>) {
        val workPatternRow = workPatternMap[workPatternID]
        if (workPatternRow != null) {
            week.name = workPatternRow.getString("NAMN")

            val timeEntryRows = timeEntryMap[workPatternID]
            if (timeEntryRows != null) {
                var lastEndTime = Long.MIN_VALUE
                var currentDay = Day.SUNDAY
                var hours = week.addCalendarHours(currentDay)
                Arrays.fill(week.days, DayType.NON_WORKING)

                for (row in timeEntryRows) {
                    var startTime: Date? = row.getDate("START_TIME")
                    var endTime: Date? = row.getDate("END_TIME")
                    if (startTime == null) {
                        startTime = DateHelper.getDayStartDate(Date(0))
                    }

                    if (endTime == null) {
                        endTime = DateHelper.getDayEndDate(Date(0))
                    }

                    if (startTime!!.getTime() > endTime!!.getTime()) {
                        endTime = DateHelper.addDays(endTime, 1)
                    }

                    if (startTime!!.getTime() < lastEndTime) {
                        currentDay = currentDay.getNextDay()
                        hours = week.addCalendarHours(currentDay)
                    }

                    val type = exceptionTypeMap[row.getInteger("EXCEPTIOP")]
                    if (type === DayType.WORKING) {
                        hours.addRange(DateRange(startTime, endTime))
                        week.setWorkingDay(currentDay, DayType.WORKING)
                    }

                    lastEndTime = endTime!!.getTime()
                }
            }
        }
    }

    /**
     * Extract note text.
     *
     * @param row task data
     * @return note text
     */
    private fun getNotes(row: Row): String? {
        var notes: String? = row.getString("NOTET")
        if (notes != null) {
            if (notes.isEmpty()) {
                notes = null
            } else {
                if (notes.indexOf(LINE_BREAK) !== -1) {
                    notes = notes.replace(LINE_BREAK, "\n")
                }
            }
        }
        return notes
    }

    companion object {

        private val COMPLETE = Double.valueOf(100)
        private val INCOMPLETE = Double.valueOf(0)
        private val LINE_BREAK = "|@|||"
        private val LEAF_COMPARATOR = RowComparator("NATURAL_ORDER", "NATURAO_ORDER")
        private val BAR_COMPARATOR = RowComparator("EXPANDED_TASK", "NATURAL_ORDER")

        private val RELATION_TYPES = arrayOf(RelationType.FINISH_START, RelationType.START_START, RelationType.FINISH_FINISH, RelationType.START_FINISH)
    }
}