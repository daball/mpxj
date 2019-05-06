/*
 * file:       MPD9AbstractReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       02/02/2006
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

package net.sf.mpxj.mpd

import java.util.Date
import java.util.HashMap
import java.util.LinkedList

import net.sf.mpxj.AccrueType
import net.sf.mpxj.AssignmentField
import net.sf.mpxj.ConstraintType
import net.sf.mpxj.DataType
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.Priority
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Rate
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceField
import net.sf.mpxj.ResourceType
import net.sf.mpxj.ScheduleFrom
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.TaskType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.WorkContour
import net.sf.mpxj.WorkGroup
import net.sf.mpxj.common.MPPAssignmentField
import net.sf.mpxj.common.MPPResourceField
import net.sf.mpxj.common.MPPTaskField
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.Pair
import net.sf.mpxj.common.RtfHelper

/**
 * This class implements retrieval of data from a project database
 * independently of whether the data is read directly from an MDB file,
 * or from a JDBC database connection.
 */
internal abstract class MPD9AbstractReader {

    protected var m_projectID: Integer
    protected var m_project: ProjectFile? = null
    protected var m_eventManager: EventManager? = null

    private var m_preserveNoteFormatting: Boolean = false
    private var m_autoWBS = true

    private val m_calendarMap = HashMap<Integer, ProjectCalendar>()
    private val m_baseCalendarReferences = LinkedList<Pair<ProjectCalendar, Integer>>()
    private val m_resourceMap = HashMap<Integer, ProjectCalendar>()
    private val m_assignmentMap = HashMap<Integer, ResourceAssignment>()
    /**
     * Called to reset internal state prior to reading a new project.
     */
    protected fun reset() {
        m_calendarMap.clear()
        m_baseCalendarReferences.clear()
        m_resourceMap.clear()
        m_assignmentMap.clear()
    }

    /**
     * Retrieve the details of a single project from the database.
     *
     * @param result Map instance containing the results
     * @param row result set row read from the database
     */
    protected fun processProjectListItem(result: Map<Integer, String>, row: Row) {
        val id = row.getInteger("PROJ_ID")
        val name = row.getString("PROJ_NAME")
        result.put(id, name)
    }

    /**
     * Reads the project properties.
     *
     * @param row project properties data
     */
    protected fun processProjectProperties(row: Row) {
        val properties = m_project!!.projectProperties

        properties.currencySymbol = row.getString("PROJ_OPT_CURRENCY_SYMBOL")
        properties.symbolPosition = MPDUtility.getSymbolPosition(row.getInt("PROJ_OPT_CURRENCY_POSITION"))
        properties.currencyDigits = row.getInteger("PROJ_OPT_CURRENCY_DIGITS")
        //properties.setThousandsSeparator();
        //properties.setDecimalSeparator();
        properties.defaultDurationUnits = MPDUtility.getDurationTimeUnits(row.getInt("PROJ_OPT_DUR_ENTRY_FMT"))
        //properties.setDefaultDurationIsFixed();
        properties.defaultWorkUnits = MPDUtility.getDurationTimeUnits(row.getInt("PROJ_OPT_WORK_ENTRY_FMT"))
        properties.minutesPerDay = row.getInteger("PROJ_OPT_MINUTES_PER_DAY")
        properties.minutesPerWeek = row.getInteger("PROJ_OPT_MINUTES_PER_WEEK")
        properties.defaultStandardRate = Rate(row.getDouble("PROJ_OPT_DEF_STD_RATE"), TimeUnit.HOURS)
        properties.defaultOvertimeRate = Rate(row.getDouble("PROJ_OPT_DEF_OVT_RATE"), TimeUnit.HOURS)
        properties.updatingTaskStatusUpdatesResourceStatus = row.getBoolean("PROJ_OPT_TASK_UPDATES_RES")
        properties.splitInProgressTasks = row.getBoolean("PROJ_OPT_SPLIT_IN_PROGRESS")
        //properties.setDateOrder();
        //properties.setTimeFormat();
        properties.defaultStartTime = row.getDate("PROJ_OPT_DEF_START_TIME")
        //properties.setDateSeparator();
        //properties.setTimeSeparator();
        //properties.setAmText();
        //properties.setPmText();
        //properties.setDateFormat();
        //properties.setBarTextDateFormat();
        properties.projectTitle = row.getString("PROJ_PROP_TITLE")
        properties.company = row.getString("PROJ_PROP_COMPANY")
        properties.manager = row.getString("PROJ_PROP_MANAGER")
        properties.defaultCalendarName = row.getString("PROJ_INFO_CAL_NAME")
        properties.startDate = row.getDate("PROJ_INFO_START_DATE")
        properties.finishDate = row.getDate("PROJ_INFO_FINISH_DATE")
        properties.scheduleFrom = ScheduleFrom.getInstance(1 - row.getInt("PROJ_INFO_SCHED_FROM"))
        properties.currentDate = row.getDate("PROJ_INFO_CURRENT_DATE")
        //properties.setComments();
        //properties.setCost();
        //properties.setBaselineCost();
        //properties.setActualCost();
        //properties.setWork();
        //properties.setBaselineWork();
        //properties.setActualWork();
        //properties.setWork2();
        //properties.setDuration();
        //properties.setBaselineDuration();
        //properties.setActualDuration();
        //properties.setPercentageComplete();
        //properties.setBaselineStart();
        //properties.setBaselineFinish();
        //properties.setActualStart();
        //properties.setActualFinish();
        //properties.setStartVariance();
        //properties.setFinishVariance();
        properties.subject = row.getString("PROJ_PROP_SUBJECT")
        properties.author = row.getString("PROJ_PROP_AUTHOR")
        properties.keywords = row.getString("PROJ_PROP_KEYWORDS")
        properties.defaultEndTime = row.getDate("PROJ_OPT_DEF_FINISH_TIME")
        properties.projectExternallyEdited = row.getBoolean("PROJ_EXT_EDITED_FLAG")
        properties.category = row.getString("PROJ_PROP_CATEGORY")
        properties.daysPerMonth = row.getInteger("PROJ_OPT_DAYS_PER_MONTH")
        properties.fiscalYearStart = row.getBoolean("PROJ_OPT_FY_USE_START_YR")
        //properties.setDefaultTaskEarnedValueMethod();
        //properties.setRemoveFileProperties();
        //properties.setMoveCompletedEndsBack();
        properties.newTasksEstimated = row.getBoolean("PROJ_OPT_NEW_TASK_EST")
        properties.spreadActualCost = row.getBoolean("PROJ_OPT_SPREAD_ACT_COSTS")
        properties.multipleCriticalPaths = row.getBoolean("PROJ_OPT_MULT_CRITICAL_PATHS")
        //properties.setAutoAddNewResourcesAndTasks();
        properties.lastSaved = row.getDate("PROJ_LAST_SAVED")
        properties.statusDate = row.getDate("PROJ_INFO_STATUS_DATE")
        //properties.setMoveRemainingStartsBack();
        //properties.setAutolink();
        //properties.setMicrosoftProjectServerURL();
        properties.honorConstraints = row.getBoolean("PROJ_OPT_HONOR_CONSTRAINTS")
        //properties.setAdminProject(row.getInt("PROJ_ADMINPROJECT")!=0); // Not in MPP9 MPD?
        //properties.setInsertedProjectsLikeSummary();
        properties.name = row.getString("PROJ_NAME")
        properties.spreadPercentComplete = row.getBoolean("PROJ_OPT_SPREAD_PCT_COMP")
        //properties.setMoveCompletedEndsForward();
        //properties.setEditableActualCosts();
        //properties.setUniqueID();
        //properties.setRevision();
        properties.newTasksEffortDriven = row.getBoolean("PROJ_OPT_NEW_ARE_EFFORT_DRIVEN")
        //properties.setMoveRemainingStartsForward();
        //properties.setActualsInSync(row.getInt("PROJ_ACTUALS_SYNCH") != 0); // Not in MPP9 MPD?
        properties.defaultTaskType = TaskType.getInstance(row.getInt("PROJ_OPT_DEF_TASK_TYPE"))
        //properties.setEarnedValueMethod();
        properties.creationDate = row.getDate("PROJ_CREATION_DATE")
        //properties.setExtendedCreationDate(row.getDate("PROJ_CREATION_DATE_EX")); // Not in MPP9 MPD?
        properties.defaultFixedCostAccrual = AccrueType.getInstance(row.getInt("PROJ_OPT_DEF_FIX_COST_ACCRUAL"))
        properties.criticalSlackLimit = row.getInteger("PROJ_OPT_CRITICAL_SLACK_LIMIT")
        //properties.setBaselineForEarnedValue;
        properties.fiscalYearStartMonth = row.getInteger("PROJ_OPT_FY_START_MONTH")
        //properties.setNewTaskStartIsProjectStart();
        properties.weekStartDay = Day.getInstance(row.getInt("PROJ_OPT_WEEK_START_DAY") + 1)
        //properties.setCalculateMultipleCriticalPaths();
        properties.multipleCriticalPaths = row.getBoolean("PROJ_OPT_MULT_CRITICAL_PATHS")

        //
        // Unused attributes
        //

        //    PROJ_OPT_CALC_ACT_COSTS
        //    PROJ_POOL_ATTACHED_TO
        //    PROJ_IS_RES_POOL
        //    PROJ_OPT_CALC_SUB_AS_SUMMARY
        //    PROJ_OPT_SHOW_EST_DUR
        //    PROJ_OPT_EXPAND_TIMEPHASED
        //    PROJ_PROJECT
        //    PROJ_VERSION
        //    PROJ_ENT_LIST_SEPARATOR
        //    PROJ_EXT_EDITED_DUR
        //    PROJ_EXT_EDITED_NUM
        //    PROJ_EXT_EDITED_FLAG
        //    PROJ_EXT_EDITED_CODE
        //    PROJ_EXT_EDITED_TEXT
        //    PROJ_IGNORE_FRONT_END
        //    PROJ_EXT_EDITED
        //    PROJ_DATA_SOURCE
        //    PROJ_READ_ONLY
        //    PROJ_READ_WRITE
        //    PROJ_READ_COUNT
        //    PROJ_LOCKED
        //    PROJ_MACHINE_ID
        //    PROJ_TYPE
        //    PROJ_CHECKEDOUT
        //    PROJ_CHECKEDOUTBY
        //    PROJ_CHECKEDOUTDATE
        //    RESERVED_BINARY_DATA
    }

    /**
     * Read a calendar.
     *
     * @param row calendar data
     */
    protected fun processCalendar(row: Row) {
        val uniqueID = row.getInteger("CAL_UID")
        if (NumberHelper.getInt(uniqueID) > 0) {
            val baseCalendar = row.getBoolean("CAL_IS_BASE_CAL")
            val cal: ProjectCalendar
            if (baseCalendar == true) {
                cal = m_project!!.addCalendar()
                cal.name = row.getString("CAL_NAME")
            } else {
                val resourceID = row.getInteger("RES_UID")
                cal = m_project!!.addCalendar()
                m_baseCalendarReferences.add(Pair<ProjectCalendar, Integer>(cal, row.getInteger("CAL_BASE_UID")))
                m_resourceMap.put(resourceID, cal)
            }

            cal.uniqueID = uniqueID
            m_calendarMap.put(uniqueID, cal)
            m_eventManager!!.fireCalendarReadEvent(cal)
        }
    }

    /**
     * Read calendar hours and exception data.
     *
     * @param calendar parent calendar
     * @param row calendar hours and exception data
     */
    protected fun processCalendarData(calendar: ProjectCalendar, row: Row) {
        val dayIndex = row.getInt("CD_DAY_OR_EXCEPTION")
        if (dayIndex == 0) {
            processCalendarException(calendar, row)
        } else {
            processCalendarHours(calendar, row, dayIndex)
        }
    }

    /**
     * Process a calendar exception.
     *
     * @param calendar parent calendar
     * @param row calendar exception data
     */
    private fun processCalendarException(calendar: ProjectCalendar, row: Row) {
        val fromDate = row.getDate("CD_FROM_DATE")
        val toDate = row.getDate("CD_TO_DATE")
        val working = row.getInt("CD_WORKING") != 0
        val exception = calendar.addCalendarException(fromDate, toDate)
        if (working) {
            exception.addRange(DateRange(row.getDate("CD_FROM_TIME1"), row.getDate("CD_TO_TIME1")))
            exception.addRange(DateRange(row.getDate("CD_FROM_TIME2"), row.getDate("CD_TO_TIME2")))
            exception.addRange(DateRange(row.getDate("CD_FROM_TIME3"), row.getDate("CD_TO_TIME3")))
            exception.addRange(DateRange(row.getDate("CD_FROM_TIME4"), row.getDate("CD_TO_TIME4")))
            exception.addRange(DateRange(row.getDate("CD_FROM_TIME5"), row.getDate("CD_TO_TIME5")))
        }
    }

    /**
     * Process calendar hours.
     *
     * @param calendar parent calendar
     * @param row calendar hours data
     * @param dayIndex day index
     */
    private fun processCalendarHours(calendar: ProjectCalendar, row: Row, dayIndex: Int) {
        val day = Day.getInstance(dayIndex)
        val working = row.getInt("CD_WORKING") != 0
        calendar.setWorkingDay(day, working)
        if (working == true) {
            val hours = calendar.addCalendarHours(day)

            var start: Date? = row.getDate("CD_FROM_TIME1")
            var end: Date? = row.getDate("CD_TO_TIME1")
            if (start != null && end != null) {
                hours.addRange(DateRange(start, end))
            }

            start = row.getDate("CD_FROM_TIME2")
            end = row.getDate("CD_TO_TIME2")
            if (start != null && end != null) {
                hours.addRange(DateRange(start, end))
            }

            start = row.getDate("CD_FROM_TIME3")
            end = row.getDate("CD_TO_TIME3")
            if (start != null && end != null) {
                hours.addRange(DateRange(start, end))
            }

            start = row.getDate("CD_FROM_TIME4")
            end = row.getDate("CD_TO_TIME4")
            if (start != null && end != null) {
                hours.addRange(DateRange(start, end))
            }

            start = row.getDate("CD_FROM_TIME5")
            end = row.getDate("CD_TO_TIME5")
            if (start != null && end != null) {
                hours.addRange(DateRange(start, end))
            }
        }
    }

    /**
     * The way calendars are stored in an MPP9 file means that there
     * can be forward references between the base calendar unique ID for a
     * derived calendar, and the base calendar itself. To get around this,
     * we initially populate the base calendar name attribute with the
     * base calendar unique ID, and now in this method we can convert those
     * ID values into the correct names.
     */
    protected fun updateBaseCalendarNames() {
        for (pair in m_baseCalendarReferences) {
            val cal = pair.first
            val baseCalendarID = pair.second
            val baseCal = m_calendarMap.get(baseCalendarID)
            if (baseCal != null) {
                cal!!.parent = baseCal
            }
        }
    }

    /**
     * Process a resource.
     *
     * @param row resource data
     */
    protected fun processResource(row: Row) {
        val uniqueID = row.getInteger("RES_UID")
        if (uniqueID != null && uniqueID.intValue() >= 0) {
            val resource = m_project!!.addResource()
            resource.accrueAt = AccrueType.getInstance(row.getInt("RES_ACCRUE_AT"))
            resource.actualCost = getDefaultOnNull(row.getCurrency("RES_ACT_COST"), NumberHelper.DOUBLE_ZERO)
            resource.actualOvertimeCost = row.getCurrency("RES_ACT_OVT_COST")
            resource.actualOvertimeWork = row.getDuration("RES_ACT_OVT_WORK")
            //resource.setActualOvertimeWorkProtected();
            resource.actualWork = row.getDuration("RES_ACT_WORK")
            //resource.setActualWorkProtected();
            //resource.setActveDirectoryGUID();
            resource.acwp = row.getCurrency("RES_ACWP")
            resource.availableFrom = row.getDate("RES_AVAIL_FROM")
            resource.availableTo = row.getDate("RES_AVAIL_TO")
            //resource.setBaseCalendar();
            resource.baselineCost = getDefaultOnNull(row.getCurrency("RES_BASE_COST"), NumberHelper.DOUBLE_ZERO)
            resource.baselineWork = row.getDuration("RES_BASE_WORK")
            resource.bcwp = row.getCurrency("RES_BCWP")
            resource.bcws = row.getCurrency("RES_BCWS")
            //resource.setBookingType();
            resource.canLevel = row.getBoolean("RES_CAN_LEVEL")
            //resource.setCode();
            resource.cost = getDefaultOnNull(row.getCurrency("RES_COST"), NumberHelper.DOUBLE_ZERO)
            //resource.setCost1();
            //resource.setCost2();
            //resource.setCost3();
            //resource.setCost4();
            //resource.setCost5();
            //resource.setCost6();
            //resource.setCost7();
            //resource.setCost8();
            //resource.setCost9();
            //resource.setCost10();
            resource.costPerUse = row.getCurrency("RES_COST_PER_USE")
            //resource.setCreationDate();
            //resource.setCV();
            //resource.setDate1();
            //resource.setDate2();
            //resource.setDate3();
            //resource.setDate4();
            //resource.setDate5();
            //resource.setDate6();
            //resource.setDate7();
            //resource.setDate8();
            //resource.setDate9();
            //resource.setDate10();
            //resource.setDuration1();
            //resource.setDuration2();
            //resource.setDuration3();
            //resource.setDuration4();
            //resource.setDuration5();
            //resource.setDuration6();
            //resource.setDuration7();
            //resource.setDuration8();
            //resource.setDuration9();
            //resource.setDuration10();
            //resource.setEmailAddress();
            //resource.setFinish();
            //resource.setFinish1();
            //resource.setFinish2();
            //resource.setFinish3();
            //resource.setFinish4();
            //resource.setFinish5();
            //resource.setFinish6();
            //resource.setFinish7();
            //resource.setFinish8();
            //resource.setFinish9();
            //resource.setFinish10();
            //resource.setFlag1();
            //resource.setFlag2();
            //resource.setFlag3();
            //resource.setFlag4();
            //resource.setFlag5();
            //resource.setFlag6();
            //resource.setFlag7();
            //resource.setFlag8();
            //resource.setFlag9();
            //resource.setFlag10();
            //resource.setFlag11();
            //resource.setFlag12();
            //resource.setFlag13();
            //resource.setFlag14();
            //resource.setFlag15();
            //resource.setFlag16();
            //resource.setFlag17();
            //resource.setFlag18();
            //resource.setFlag19();
            //resource.setFlag20();
            //resource.setGroup();
            //resource.setHyperlink();
            //resource.setHyperlinkAddress();
            //resource.setHyperlinkSubAddress();
            resource.id = row.getInteger("RES_ID")
            resource.initials = row.getString("RES_INITIALS")
            //resource.setIsEnterprise();
            //resource.setIsGeneric();
            //resource.setIsInactive();
            //resource.setIsNull();
            //resource.setLinkedFields();RES_HAS_LINKED_FIELDS = false ( java.lang.Boolean)
            resource.materialLabel = row.getString("RES_MATERIAL_LABEL")
            resource.maxUnits = Double.valueOf(NumberHelper.getDouble(row.getDouble("RES_MAX_UNITS")) * 100)
            resource.name = row.getString("RES_NAME")
            //resource.setNtAccount();
            //resource.setNumber1();
            //resource.setNumber2();
            //resource.setNumber3();
            //resource.setNumber4();
            //resource.setNumber5();
            //resource.setNumber6();
            //resource.setNumber7();
            //resource.setNumber8();
            //resource.setNumber9();
            //resource.setNumber10();
            //resource.setNumber11();
            //resource.setNumber12();
            //resource.setNumber13();
            //resource.setNumber14();
            //resource.setNumber15();
            //resource.setNumber16();
            //resource.setNumber17();
            //resource.setNumber18();
            //resource.setNumber19();
            //resource.setNumber20();
            resource.objects = getNullOnValue(row.getInteger("RES_NUM_OBJECTS"), 0)
            //resource.setOutlineCode1();
            //resource.setOutlineCode2();
            //resource.setOutlineCode3();
            //resource.setOutlineCode4();
            //resource.setOutlineCode5();
            //resource.setOutlineCode6();
            //resource.setOutlineCode7();
            //resource.setOutlineCode8();
            //resource.setOutlineCode9();
            //resource.setOutlineCode10();
            resource.overAllocated = row.getBoolean("RES_IS_OVERALLOCATED")
            resource.overtimeCost = row.getCurrency("RES_OVT_COST")
            resource.overtimeRate = Rate(row.getDouble("RES_OVT_RATE"), TimeUnit.HOURS)
            resource.overtimeRateUnits = TimeUnit.getInstance(row.getInt("RES_OVT_RATE_FMT") - 1)
            resource.overtimeWork = row.getDuration("RES_OVT_WORK")
            resource.peakUnits = Double.valueOf(NumberHelper.getDouble(row.getDouble("RES_PEAK")) * 100)
            //resource.setPercentWorkComplete();
            resource.phonetics = row.getString("RES_PHONETICS")
            resource.regularWork = row.getDuration("RES_REG_WORK")
            resource.remainingCost = getDefaultOnNull(row.getCurrency("RES_REM_COST"), NumberHelper.DOUBLE_ZERO)
            resource.remainingOvertimeCost = row.getCurrency("RES_REM_OVT_COST")
            resource.remainingOvertimeWork = row.getDuration("RES_REM_OVT_WORK")
            resource.remainingWork = row.getDuration("RES_REM_WORK")
            //resource.setResourceCalendar();RES_CAL_UID = null ( ) // CHECK THIS
            resource.standardRate = Rate(row.getDouble("RES_STD_RATE"), TimeUnit.HOURS)
            resource.standardRateUnits = TimeUnit.getInstance(row.getInt("RES_STD_RATE_FMT") - 1)
            //resource.setStart();
            //resource.setStart1();
            //resource.setStart2();
            //resource.setStart3();
            //resource.setStart4();
            //resource.setStart5();
            //resource.setStart6();
            //resource.setStart7();
            //resource.setStart8();
            //resource.setStart9();
            //resource.setStart10();
            //resource.setText1();
            //resource.setText2();
            //resource.setText3();
            //resource.setText4();
            //resource.setText5();
            //resource.setText6();
            //resource.setText7();
            //resource.setText8();
            //resource.setText9();
            //resource.setText10();
            //resource.setText11();
            //resource.setText12();
            //resource.setText13();
            //resource.setText14();
            //resource.setText15();
            //resource.setText16();
            //resource.setText17();
            //resource.setText18();
            //resource.setText19();
            //resource.setText20();
            //resource.setText21();
            //resource.setText22();
            //resource.setText23();
            //resource.setText24();
            //resource.setText25();
            //resource.setText26();
            //resource.setText27();
            //resource.setText28();
            //resource.setText29();
            //resource.setText30();
            resource.type = if (row.getBoolean("RES_TYPE")) ResourceType.WORK else ResourceType.MATERIAL
            resource.uniqueID = uniqueID
            resource.work = row.getDuration("RES_WORK")
            resource.workGroup = WorkGroup.getInstance(row.getInt("RES_WORKGROUP_MESSAGING"))

            var notes: String? = row.getString("RES_RTF_NOTES")
            if (notes != null) {
                if (m_preserveNoteFormatting == false) {
                    notes = RtfHelper.strip(notes)
                }
                resource.notes = notes
            }

            resource.resourceCalendar = m_project!!.getCalendarByUniqueID(row.getInteger("RES_CAL_UID"))

            //
            // Calculate the cost variance
            //
            if (resource.cost != null && resource.baselineCost != null) {
                resource.costVariance = NumberHelper.getDouble(resource.cost!!.doubleValue() - resource.baselineCost!!.doubleValue())
            }

            //
            // Calculate the work variance
            //
            if (resource.work != null && resource.baselineWork != null) {
                resource.workVariance = Duration.getInstance(resource.work!!.getDuration() - resource.baselineWork!!.getDuration(), TimeUnit.HOURS)
            }

            //
            // Set the overallocated flag
            //
            resource.overAllocated = NumberHelper.getDouble(resource.peakUnits) > NumberHelper.getDouble(resource.maxUnits)

            m_eventManager!!.fireResourceReadEvent(resource)

            //
            // Unused attributes
            //
            //EXT_EDIT_REF_DATA = null ( )
            //RESERVED_DATA = null ( )
        }
    }

    /**
     * Read resource baseline values.
     *
     * @param row result set row
     */
    protected fun processResourceBaseline(row: Row) {
        val id = row.getInteger("RES_UID")
        val resource = m_project!!.getResourceByUniqueID(id)
        if (resource != null) {
            val index = row.getInt("RB_BASE_NUM")

            resource.setBaselineWork(index, row.getDuration("RB_BASE_WORK"))
            resource.setBaselineCost(index, row.getCurrency("RB_BASE_COST"))
        }
    }

    /**
     * Read a single text field extended attribute.
     *
     * @param row field data
     */
    protected fun processTextField(row: Row) {
        processField(row, "TEXT_FIELD_ID", "TEXT_REF_UID", row.getString("TEXT_VALUE"))
    }

    /**
     * Read a single number field extended attribute.
     *
     * @param row field data
     */
    protected fun processNumberField(row: Row) {
        processField(row, "NUM_FIELD_ID", "NUM_REF_UID", row.getDouble("NUM_VALUE"))
    }

    /**
     * Read a single flag field extended attribute.
     *
     * @param row field data
     */
    protected fun processFlagField(row: Row) {
        processField(row, "FLAG_FIELD_ID", "FLAG_REF_UID", Boolean.valueOf(row.getBoolean("FLAG_VALUE")))
    }

    /**
     * Read a single duration field extended attribute.
     *
     * @param row field data
     */
    protected fun processDurationField(row: Row) {
        processField(row, "DUR_FIELD_ID", "DUR_REF_UID", MPDUtility.getAdjustedDuration(m_project, row.getInt("DUR_VALUE"), MPDUtility.getDurationTimeUnits(row.getInt("DUR_FMT"))))
    }

    /**
     * Read a single date field extended attribute.
     *
     * @param row field data
     */
    protected fun processDateField(row: Row) {
        processField(row, "DATE_FIELD_ID", "DATE_REF_UID", row.getDate("DATE_VALUE"))
    }

    /**
     * Read a single outline code field extended attribute.
     *
     * @param entityID parent entity
     * @param row field data
     */
    protected fun processOutlineCodeField(entityID: Integer, row: Row) {
        processField(row, "OC_FIELD_ID", entityID, row.getString("OC_NAME"))
    }

    /**
     * Generic method to process an extended attribute field.
     *
     * @param row extended attribute data
     * @param fieldIDColumn column containing the field ID
     * @param entityIDColumn column containing the entity ID
     * @param value field value
     */
    protected fun processField(row: Row, fieldIDColumn: String, entityIDColumn: String, value: Object) {
        processField(row, fieldIDColumn, row.getInteger(entityIDColumn), value)
    }

    /**
     * Generic method to process an extended attribute field.
     *
     * @param row extended attribute data
     * @param fieldIDColumn column containing the field ID
     * @param entityID parent entity ID
     * @param value field value
     */
    protected fun processField(row: Row, fieldIDColumn: String, entityID: Integer, value: Object) {
        var value = value
        val fieldID = row.getInt(fieldIDColumn)

        val prefix = fieldID and -0x10000
        val index = fieldID and 0x0000FFFF

        when (prefix) {
            MPPTaskField.TASK_FIELD_BASE -> {
                val field = MPPTaskField.getInstance(index)
                if (field != null && field !== TaskField.NOTES) {
                    val task = m_project!!.getTaskByUniqueID(entityID)
                    if (task != null) {
                        if (field.getDataType() === DataType.CURRENCY) {
                            value = Double.valueOf((value as Double).doubleValue() / 100)
                        }
                        task.set(field, value)
                    }
                }
            }

            MPPResourceField.RESOURCE_FIELD_BASE -> {
                val field = MPPResourceField.getInstance(index)
                if (field != null && field != ResourceField.NOTES) {
                    val resource = m_project!!.getResourceByUniqueID(entityID)
                    if (resource != null) {
                        if (field.dataType === DataType.CURRENCY) {
                            value = Double.valueOf((value as Double).doubleValue() / 100)
                        }
                        resource.set(field, value)
                    }
                }
            }

            MPPAssignmentField.ASSIGNMENT_FIELD_BASE -> {
                val field = MPPAssignmentField.getInstance(index)
                if (field != null && field !== AssignmentField.NOTES) {
                    val assignment = m_assignmentMap.get(entityID)
                    if (assignment != null) {
                        if (field.getDataType() === DataType.CURRENCY) {
                            value = Double.valueOf((value as Double).doubleValue() / 100)
                        }
                        assignment!!.set(field, value)
                    }
                }
            }
        }
    }

    /**
     * Process a task.
     *
     * @param row task data
     */
    protected fun processTask(row: Row) {
        val uniqueID = row.getInteger("TASK_UID")
        if (uniqueID != null && uniqueID.intValue() >= 0) {
            val task = m_project!!.addTask()
            val durationFormat = MPDUtility.getDurationTimeUnits(row.getInt("TASK_DUR_FMT"))

            task.actualCost = row.getCurrency("TASK_ACT_COST")
            task.actualDuration = MPDUtility.getAdjustedDuration(m_project, row.getInt("TASK_ACT_DUR"), durationFormat)
            task.actualFinish = row.getDate("TASK_ACT_FINISH")
            task.actualOvertimeCost = row.getCurrency("TASK_ACT_OVT_COST")
            task.actualOvertimeWork = row.getDuration("TASK_ACT_OVT_WORK")
            //task.setActualOvertimeWorkProtected();
            task.actualStart = row.getDate("TASK_ACT_START")
            task.actualWork = row.getDuration("TASK_ACT_WORK")
            //task.setActualWorkProtected();
            task.acwp = row.getCurrency("TASK_ACWP")
            task.baselineCost = row.getCurrency("TASK_BASE_COST")
            task.baselineDuration = MPDUtility.getAdjustedDuration(m_project, row.getInt("TASK_BASE_DUR"), durationFormat)
            task.baselineFinish = row.getDate("TASK_BASE_FINISH")
            task.baselineStart = row.getDate("TASK_BASE_START")
            task.baselineWork = row.getDuration("TASK_BASE_WORK")
            //task.setBCWP(row.getCurrency("TASK_BCWP")); //@todo FIXME
            //task.setBCWS(row.getCurrency("TASK_BCWS")); //@todo FIXME
            task.calendar = m_project!!.getCalendarByUniqueID(row.getInteger("TASK_CAL_UID"))
            //task.setConfirmed();
            task.constraintDate = row.getDate("TASK_CONSTRAINT_DATE")
            task.constraintType = ConstraintType.getInstance(row.getInt("TASK_CONSTRAINT_TYPE"))
            //task.setContact();
            task.cost = row.getCurrency("TASK_COST")
            //task.setCost1();
            //task.setCost2();
            //task.setCost3();
            //task.setCost4();
            //task.setCost5();
            //task.setCost6();
            //task.setCost7();
            //task.setCost8();
            //task.setCost9();
            //task.setCost10();
            //task.setCostVariance();
            task.createDate = row.getDate("TASK_CREATION_DATE")
            //task.setCritical(row.getBoolean("TASK_IS_CRITICAL")); @todo FIX ME
            //task.setCV();
            //task.setDate1();
            //task.setDate2();
            //task.setDate3();
            //task.setDate4();
            //task.setDate5();
            //task.setDate6();
            //task.setDate7();
            //task.setDate8();
            //task.setDate9();
            //task.setDate10();
            task.deadline = row.getDate("TASK_DEADLINE")
            //task.setDelay();
            task.duration = MPDUtility.getAdjustedDuration(m_project, row.getInt("TASK_DUR"), durationFormat)

            //task.setDuration1();
            //task.setDuration2();
            //task.setDuration3();
            //task.setDuration4();
            //task.setDuration5();
            //task.setDuration6();
            //task.setDuration7();
            //task.setDuration8();
            //task.setDuration9();
            //task.setDuration10();

            task.durationVariance = MPDUtility.getAdjustedDuration(m_project, row.getInt("TASK_DUR_VAR"), durationFormat)
            task.earlyFinish = row.getDate("TASK_EARLY_FINISH")
            task.earlyStart = row.getDate("TASK_EARLY_START")
            //task.setEarnedValueMethod();
            task.effortDriven = row.getBoolean("TASK_IS_EFFORT_DRIVEN")
            task.estimated = row.getBoolean("TASK_DUR_IS_EST")
            task.expanded = !row.getBoolean("TASK_IS_COLLAPSED")
            task.externalTask = row.getBoolean("TASK_IS_EXTERNAL")
            //task.setExternalTaskProject();
            task.finish = row.getDate("TASK_FINISH_DATE")
            //task.setFinish1();
            //task.setFinish2();
            //task.setFinish3();
            //task.setFinish4();
            //task.setFinish5();
            //task.setFinish6();
            //task.setFinish7();
            //task.setFinish8();
            //task.setFinish9();
            //task.setFinish10();
            //task.setFinishVariance(MPDUtility.getAdjustedDuration(m_project, row.getInt("TASK_FINISH_VAR"), durationFormat)); // Calculate for consistent results?
            //task.setFixed();
            task.fixedCost = row.getCurrency("TASK_FIXED_COST")
            task.fixedCostAccrual = AccrueType.getInstance(row.getInt("TASK_FIXED_COST_ACCRUAL"))
            //task.setFlag1();
            //task.setFlag2();
            //task.setFlag3();
            //task.setFlag4();
            //task.setFlag5();
            //task.setFlag6();
            //task.setFlag7();
            //task.setFlag8();
            //task.setFlag9();
            //task.setFlag10();
            //task.setFlag11();
            //task.setFlag12();
            //task.setFlag13();
            //task.setFlag14();
            //task.setFlag15();
            //task.setFlag16();
            //task.setFlag17();
            //task.setFlag18();
            //task.setFlag19();
            //task.setFlag20();
            task.freeSlack = row.getDuration("TASK_FREE_SLACK").convertUnits(durationFormat, m_project!!.projectProperties)
            task.hideBar = row.getBoolean("TASK_BAR_IS_HIDDEN")
            //task.setHyperlink();
            //task.setHyperlinkAddress();
            //task.setHyperlinkSubAddress();
            task.id = row.getInteger("TASK_ID")
            task.ignoreResourceCalendar = row.getBoolean("TASK_IGNORES_RES_CAL")
            task.lateFinish = row.getDate("TASK_LATE_FINISH")
            task.lateStart = row.getDate("TASK_LATE_START")
            task.levelAssignments = row.getBoolean("TASK_LEVELING_ADJUSTS_ASSN")
            task.levelingCanSplit = row.getBoolean("TASK_LEVELING_CAN_SPLIT")
            task.levelingDelayFormat = MPDUtility.getDurationTimeUnits(row.getInt("TASK_LEVELING_DELAY_FMT"))
            task.levelingDelay = MPDUtility.getAdjustedDuration(m_project, row.getInt("TASK_LEVELING_DELAY"), task.levelingDelayFormat!!)
            //task.setLinkedFields(row.getBoolean("TASK_HAS_LINKED_FIELDS")); @todo FIXME
            task.marked = row.getBoolean("TASK_IS_MARKED")
            task.milestone = row.getBoolean("TASK_IS_MILESTONE")
            task.name = row.getString("TASK_NAME")
            //task.setNull();
            //task.setNumber1();
            //task.setNumber2();
            //task.setNumber3();
            //task.setNumber4();
            //task.setNumber5();
            //task.setNumber6();
            //task.setNumber7();
            //task.setNumber8();
            //task.setNumber9();
            //task.setNumber10();
            //task.setNumber11();
            //task.setNumber12();
            //task.setNumber13();
            //task.setNumber14();
            //task.setNumber15();
            //task.setNumber16();
            //task.setNumber17();
            //task.setNumber18();
            //task.setNumber19();
            //task.setNumber20();
            task.objects = getNullOnValue(row.getInteger("TASK_NUM_OBJECTS"), 0)
            //task.setOutlineCode1();
            //task.setOutlineCode2();
            //task.setOutlineCode3();
            //task.setOutlineCode4();
            //task.setOutlineCode5();
            //task.setOutlineCode6();
            //task.setOutlineCode7();
            //task.setOutlineCode8();
            //task.setOutlineCode9();
            //task.setOutlineCode10();
            task.outlineLevel = row.getInteger("TASK_OUTLINE_LEVEL")
            task.outlineNumber = row.getString("TASK_OUTLINE_NUM")
            task.overAllocated = row.getBoolean("TASK_IS_OVERALLOCATED")
            task.overtimeCost = row.getCurrency("TASK_OVT_COST")
            //task.setOvertimeWork();
            task.percentageComplete = row.getDouble("TASK_PCT_COMP")
            task.percentageWorkComplete = row.getDouble("TASK_PCT_WORK_COMP")
            //task.setPhysicalPercentComplete();
            task.preleveledFinish = row.getDate("TASK_PRELEVELED_FINISH")
            task.preleveledStart = row.getDate("TASK_PRELEVELED_START")
            task.priority = Priority.getInstance(row.getInt("TASK_PRIORITY"))
            task.recurring = row.getBoolean("TASK_IS_RECURRING")
            task.regularWork = row.getDuration("TASK_REG_WORK")
            task.remainingCost = row.getCurrency("TASK_REM_COST")
            task.remainingDuration = MPDUtility.getAdjustedDuration(m_project, row.getInt("TASK_REM_DUR"), durationFormat)
            task.remainingOvertimeCost = row.getCurrency("TASK_REM_OVT_COST")
            task.remainingOvertimeWork = row.getDuration("TASK_REM_OVT_WORK")
            task.remainingWork = row.getDuration("TASK_REM_WORK")
            //task.setResourceGroup();
            //task.setResourceInitials();
            //task.setResourceNames();
            task.resume = row.getDate("TASK_RESUME_DATE")
            //task.setResumeNoEarlierThan();
            //task.setResumeValid();
            task.rollup = row.getBoolean("TASK_IS_ROLLED_UP")
            task.start = row.getDate("TASK_START_DATE")
            //task.setStart1();
            //task.setStart2();
            //task.setStart3();
            //task.setStart4();
            //task.setStart5();
            //task.setStart6();
            //task.setStart7();
            //task.setStart8();
            //task.setStart9();
            //task.setStart10();
            //task.setStartVariance(MPDUtility.getAdjustedDuration(m_project, row.getInt("TASK_START_VAR"), durationFormat)); // more accurate by calculation?
            task.stop = row.getDate("TASK_STOP_DATE")
            task.summary = row.getBoolean("TASK_IS_SUMMARY")
            //task.setText1();
            //task.setText2();
            //task.setText3();
            //task.setText4();
            //task.setText5();
            //task.setText6();
            //task.setText7();
            //task.setText8();
            //task.setText9();
            //task.setText10();
            //task.setText11();
            //task.setText12();
            //task.setText13();
            //task.setText14();
            //task.setText15();
            //task.setText16();
            //task.setText17();
            //task.setText18();
            //task.setText19();
            //task.setText20();
            //task.setText21();
            //task.setText22();
            //task.setText23();
            //task.setText24();
            //task.setText25();
            //task.setText26();
            //task.setText27();
            //task.setText28();
            //task.setText29();
            //task.setText30();
            //task.setTotalSlack(row.getDuration("TASK_TOTAL_SLACK")); //@todo FIX ME
            task.type = TaskType.getInstance(row.getInt("TASK_TYPE"))
            task.uniqueID = uniqueID
            //task.setUpdateNeeded();
            task.wbs = row.getString("TASK_WBS")
            //task.setWBSLevel();
            task.work = row.getDuration("TASK_WORK")
            //task.setWorkVariance();

            //TASK_HAS_NOTES = false ( java.lang.Boolean)
            //TASK_RTF_NOTES = null ( )
            var notes: String? = row.getString("TASK_RTF_NOTES")
            if (notes != null) {
                if (m_preserveNoteFormatting == false) {
                    notes = RtfHelper.strip(notes)
                }
                task.notes = notes
            }

            //
            // Calculate the cost variance
            //
            if (task.cost != null && task.baselineCost != null) {
                task.costVariance = NumberHelper.getDouble(task.cost!!.doubleValue() - task.baselineCost!!.doubleValue())
            }

            //
            // Set default flag values
            //
            task.setFlag(1, false)
            task.setFlag(2, false)
            task.setFlag(3, false)
            task.setFlag(4, false)
            task.setFlag(5, false)
            task.setFlag(6, false)
            task.setFlag(7, false)
            task.setFlag(8, false)
            task.setFlag(9, false)
            task.setFlag(10, false)

            //
            // If we have a WBS value from the MPD file, don't autogenerate
            //
            if (task.wbs != null) {
                m_autoWBS = false
            }

            //
            // Attempt to identify null tasks
            //
            if (task.name == null && task.start == null && task.finish == null) {
                task.`null` = true
            }

            m_eventManager!!.fireTaskReadEvent(task)
        }
    }

    /**
     * Read task baseline values.
     *
     * @param row result set row
     */
    protected fun processTaskBaseline(row: Row) {
        val id = row.getInteger("TASK_UID")
        val task = m_project!!.getTaskByUniqueID(id)
        if (task != null) {
            val index = row.getInt("TB_BASE_NUM")

            task.setBaselineDuration(index, MPDUtility.getAdjustedDuration(m_project, row.getInt("TB_BASE_DUR"), MPDUtility.getDurationTimeUnits(row.getInt("TB_BASE_DUR_FMT"))))
            task.setBaselineStart(index, row.getDate("TB_BASE_START"))
            task.setBaselineFinish(index, row.getDate("TB_BASE_FINISH"))
            task.setBaselineWork(index, row.getDuration("TB_BASE_WORK"))
            task.setBaselineCost(index, row.getCurrency("TB_BASE_COST"))
        }
    }

    /**
     * Process a relationship between two tasks.
     *
     * @param row relationship data
     */
    protected fun processLink(row: Row) {
        val predecessorTask = m_project!!.getTaskByUniqueID(row.getInteger("LINK_PRED_UID"))
        val successorTask = m_project!!.getTaskByUniqueID(row.getInteger("LINK_SUCC_UID"))
        if (predecessorTask != null && successorTask != null) {
            val type = RelationType.getInstance(row.getInt("LINK_TYPE"))
            val durationUnits = MPDUtility.getDurationTimeUnits(row.getInt("LINK_LAG_FMT"))
            val duration = MPDUtility.getDuration(row.getDouble("LINK_LAG").doubleValue(), durationUnits)
            val relation = successorTask.addPredecessor(predecessorTask, type, duration)
            relation.uniqueID = row.getInteger("LINK_UID")
            m_eventManager!!.fireRelationReadEvent(relation)
        }
    }

    /**
     * Process a resource assignment.
     *
     * @param row resource assignment data
     */
    protected fun processAssignment(row: Row) {
        val resource = m_project!!.getResourceByUniqueID(row.getInteger("RES_UID"))
        val task = m_project!!.getTaskByUniqueID(row.getInteger("TASK_UID"))

        if (task != null) {
            val assignment = task.addResourceAssignment(resource)
            m_assignmentMap.put(row.getInteger("ASSN_UID"), assignment)

            assignment.actualCost = row.getCurrency("ASSN_ACT_COST")
            assignment.actualFinish = row.getDate("ASSN_ACT_FINISH")
            assignment.actualOvertimeCost = row.getCurrency("ASSN_ACT_OVT_COST")
            assignment.actualOvertimeWork = row.getDuration("ASSN_ACT_OVT_WORK")
            assignment.actualStart = row.getDate("ASSN_ACT_START")
            assignment.actualWork = row.getDuration("ASSN_ACT_WORK")
            assignment.acwp = row.getCurrency("ASSN_ACWP")
            assignment.baselineCost = row.getCurrency("ASSN_BASE_COST")
            assignment.baselineFinish = row.getDate("ASSN_BASE_FINISH")
            assignment.baselineStart = row.getDate("ASSN_BASE_START")
            assignment.baselineWork = row.getDuration("ASSN_BASE_WORK")
            assignment.bcwp = row.getCurrency("ASSN_BCWP")
            assignment.bcws = row.getCurrency("ASSN_BCWS")
            assignment.cost = row.getCurrency("ASSN_COST")
            assignment.costRateTableIndex = row.getInt("ASSN_COST_RATE_TABLE")
            //assignment.setCostVariance();
            //assignment.setCreateDate(row.getDate("ASSN_CREATION_DATE")); - not present in some MPD files?
            //assignment.setCV();
            assignment.delay = row.getDuration("ASSN_DELAY")
            assignment.finish = row.getDate("ASSN_FINISH_DATE")
            assignment.finishVariance = MPDUtility.getAdjustedDuration(m_project, row.getInt("ASSN_FINISH_VAR"), TimeUnit.DAYS)

            //assignment.setGUID();
            assignment.levelingDelay = MPDUtility.getAdjustedDuration(m_project, row.getInt("ASSN_LEVELING_DELAY"), MPDUtility.getDurationTimeUnits(row.getInt("ASSN_DELAY_FMT")))
            assignment.linkedFields = row.getBoolean("ASSN_HAS_LINKED_FIELDS")
            //assignment.setOvertimeCost();
            assignment.overtimeWork = row.getDuration("ASSN_OVT_WORK")
            //assignment.setPercentageWorkComplete();
            assignment.remainingCost = row.getCurrency("ASSN_REM_COST")
            assignment.remainingOvertimeCost = row.getCurrency("ASSN_REM_OVT_COST")
            assignment.remainingOvertimeWork = row.getDuration("ASSN_REM_OVT_WORK")
            assignment.regularWork = row.getDuration("ASSN_REG_WORK")
            assignment.remainingWork = row.getDuration("ASSN_REM_WORK")
            assignment.responsePending = row.getBoolean("ASSN_RESPONSE_PENDING")
            assignment.start = row.getDate("ASSN_START_DATE")
            assignment.startVariance = MPDUtility.getAdjustedDuration(m_project, row.getInt("ASSN_START_VAR"), TimeUnit.DAYS)

            //assignment.setSV();
            assignment.teamStatusPending = row.getBoolean("ASSN_TEAM_STATUS_PENDING")
            assignment.uniqueID = row.getInteger("ASSN_UID")
            assignment.units = Double.valueOf(row.getDouble("ASSN_UNITS").doubleValue() * 100.0)
            assignment.updateNeeded = row.getBoolean("ASSN_UPDATE_NEEDED")
            //assignment.setVAC(v);
            assignment.work = row.getDuration("ASSN_WORK")
            assignment.workContour = WorkContour.getInstance(row.getInt("ASSN_WORK_CONTOUR"))
            //assignment.setWorkVariance();

            var notes: String? = row.getString("ASSN_RTF_NOTES")
            if (notes != null) {
                if (m_preserveNoteFormatting == false) {
                    notes = RtfHelper.strip(notes)
                }
                assignment.notes = notes
            }

            m_eventManager!!.fireAssignmentReadEvent(assignment)
        }
    }

    /**
     * Read resource assignment baseline values.
     *
     * @param row result set row
     */
    protected fun processAssignmentBaseline(row: Row) {
        val id = row.getInteger("ASSN_UID")
        val assignment = m_assignmentMap.get(id)
        if (assignment != null) {
            val index = row.getInt("AB_BASE_NUM")

            assignment!!.setBaselineStart(index, row.getDate("AB_BASE_START"))
            assignment!!.setBaselineFinish(index, row.getDate("AB_BASE_FINISH"))
            assignment!!.setBaselineWork(index, row.getDuration("AB_BASE_WORK"))
            assignment!!.setBaselineCost(index, row.getCurrency("AB_BASE_COST"))
        }
    }

    /**
     * Carry out any post-processing required to tidy up
     * the data read from the database.
     */
    protected fun postProcessing() {
        //
        // Update the internal structure. We'll take this opportunity to
        // generate outline numbers for the tasks as they don't appear to
        // be present in the MPP file.
        //
        val config = m_project!!.projectConfig
        config.autoWBS = m_autoWBS
        config.autoOutlineNumber = true
        m_project!!.updateStructure()
        config.autoOutlineNumber = false

        //
        // Perform post-processing to set the summary flag
        //
        for (task in m_project!!.tasks) {
            task.summary = task.hasChildTasks()
        }

        //
        // Ensure that the unique ID counters are correct
        //
        config.updateUniqueCounters()
    }

    /**
     * This method returns the value it is passed, or null if the value
     * matches the nullValue argument.
     *
     * @param value value under test
     * @param nullValue return null if value under test matches this value
     * @return value or null
     */
    //   private Duration getNullOnValue (Duration value, Duration nullValue)
    //   {
    //      return (value.equals(nullValue)?null:value);
    //   }
    /**
     * This method returns the value it is passed, or null if the value
     * matches the nullValue argument.
     *
     * @param value value under test
     * @param nullValue return null if value under test matches this value
     * @return value or null
     */
    private fun getNullOnValue(value: Integer, nullValue: Int): Integer? {
        return if (NumberHelper.getInt(value) == nullValue) null else value
    }

    /**
     * Returns a default value if a null value is found.
     *
     * @param value value under test
     * @param defaultValue default if value is null
     * @return value
     */
    fun getDefaultOnNull(value: Double?, defaultValue: Double): Double {
        return value ?: defaultValue
    }

    /**
     * Returns a default value if a null value is found.
     *
     * @param value value under test
     * @param defaultValue default if value is null
     * @return value
     */
    fun getDefaultOnNull(value: Integer?, defaultValue: Integer): Integer {
        return if (value == null) defaultValue else value
    }

    /**
     * Sets the ID of the project to be read.
     *
     * @param projectID project ID
     */
    fun setProjectID(projectID: Integer) {
        m_projectID = projectID
    }

    /**
     * This method sets a flag to indicate whether the RTF formatting associated
     * with notes should be preserved or removed. By default the formatting
     * is removed.
     *
     * @param preserveNoteFormatting boolean flag
     */
    fun setPreserveNoteFormatting(preserveNoteFormatting: Boolean) {
        m_preserveNoteFormatting = preserveNoteFormatting
    }
}

/*
TASK_VAC = 0.0 ( java.lang.Double)
EXT_EDIT_REF_DATA = null ( )
TASK_IS_SUBPROJ = false ( java.lang.Boolean)
TASK_IS_FROM_FINISH_SUBPROJ = false ( java.lang.Boolean)
TASK_IS_RECURRING_SUMMARY = false ( java.lang.Boolean)
TASK_IS_READONLY_SUBPROJ = false ( java.lang.Boolean)
TASK_BASE_DUR_FMT = 39 ( java.lang.Short)
TASK_WBS_RIGHTMOST_LEVEL = null ( )
*/
