/*
 * file:       ResourceAssignment.java
 * author:     Scott Melville
 *             Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       15/08/2002
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

package net.sf.mpxj

import java.util.Date
import java.util.LinkedList
import java.util.UUID

import net.sf.mpxj.common.AssignmentFieldLists
import net.sf.mpxj.common.BooleanHelper
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.DefaultTimephasedWorkContainer
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.listener.FieldListener

/**
 * This class represents a resource assignment record from an MPX file.
 */
class ResourceAssignment
/**
 * Constructor.
 *
 * @param file The parent file to which this record belongs.
 * @param task The task to which this assignment is being made
 */
(file: ProjectFile,
 /**
  * Reference to the parent task of this assignment.
  */
 private var m_task: Task?) : ProjectEntity(file), ProjectEntityWithUniqueID, FieldContainer {

    /**
     * Retrieve the unique ID of this resource assignment.
     *
     * @return resource assignment unique ID
     */
    /**
     * Set the unique ID of this resource assignment.
     *
     * @param uniqueID resource assignment unique ID
     */
    override var uniqueID: Integer
        @Override get() = getCachedValue(AssignmentField.UNIQUE_ID) as Integer?
        @Override set(uniqueID) {
            set(AssignmentField.UNIQUE_ID, uniqueID)
        }

    /**
     * Returns the units of this resource assignment.
     *
     * @return units
     */
    /**
     * Sets the units for this resource assignment.
     *
     * @param val units
     */
    var units: Number
        get() = getCachedValue(AssignmentField.ASSIGNMENT_UNITS)
        set(`val`) = set(AssignmentField.ASSIGNMENT_UNITS, `val`)

    /**
     * Returns the work of this resource assignment.
     *
     * @return work
     */
    /**
     * Sets the work for this resource assignment.
     *
     * @param dur work
     */
    var work: Duration?
        get() = getCachedValue(AssignmentField.WORK) as Duration?
        set(dur) {
            set(AssignmentField.WORK, dur)
        }

    /**
     * Retrieve the baseline start date.
     *
     * @return baseline start date
     */
    /**
     * Set the baseline start date.
     *
     * @param start baseline start date
     */
    var baselineStart: Date
        get() = getCachedValue(AssignmentField.BASELINE_START) as Date?
        set(start) {
            set(AssignmentField.BASELINE_START, start)
        }

    /**
     * Retrieve the actual start date.
     *
     * @return actual start date
     */
    /**
     * Set the actual start date.
     *
     * @param start actual start date
     */
    var actualStart: Date
        get() = getCachedValue(AssignmentField.ACTUAL_START) as Date?
        set(start) {
            set(AssignmentField.ACTUAL_START, start)
        }

    /**
     * Retrieve the baseline finish date.
     *
     * @return baseline finish date
     */
    /**
     * Set the baseline finish date.
     *
     * @param finish baseline finish
     */
    var baselineFinish: Date
        get() = getCachedValue(AssignmentField.BASELINE_FINISH) as Date?
        set(finish) {
            set(AssignmentField.BASELINE_FINISH, finish)
        }

    /**
     * Retrieve the actual finish date.
     *
     * @return actual finish date
     */
    /**
     * Set the actual finish date.
     *
     * @param finish actual finish
     */
    var actualFinish: Date
        get() = getCachedValue(AssignmentField.ACTUAL_FINISH) as Date?
        set(finish) {
            set(AssignmentField.ACTUAL_FINISH, finish)
        }

    /**
     * Returns the baseline work of this resource assignment.
     *
     * @return planned work
     */
    /**
     * Sets the baseline work for this resource assignment.
     *
     * @param val planned work
     */
    var baselineWork: Duration?
        get() = getCachedValue(AssignmentField.BASELINE_WORK) as Duration?
        set(`val`) {
            set(AssignmentField.BASELINE_WORK, `val`)
        }

    /**
     * Returns the actual completed work of this resource assignment.
     *
     * @return completed work
     */
    /**
     * Sets the actual completed work for this resource assignment.
     *
     * @param val actual completed work
     */
    var actualWork: Duration?
        get() = getCachedValue(AssignmentField.ACTUAL_WORK) as Duration?
        set(`val`) {
            set(AssignmentField.ACTUAL_WORK, `val`)
        }

    /**
     * Returns the overtime work done of this resource assignment.
     *
     * @return overtime work
     */
    /**
     * Sets the overtime work for this resource assignment.
     *
     * @param overtimeWork overtime work
     */
    var overtimeWork: Duration?
        get() = getCachedValue(AssignmentField.OVERTIME_WORK) as Duration?
        set(overtimeWork) {
            set(AssignmentField.OVERTIME_WORK, overtimeWork)
        }

    /**
     * Returns the cost  of this resource assignment.
     *
     * @return cost
     */
    /**
     * Sets the cost for this resource assignment.
     *
     * @param cost cost
     */
    var cost: Number?
        get() = getCachedValue(AssignmentField.COST)
        set(cost) = set(AssignmentField.COST, cost)

    /**
     * Returns the planned cost for this resource assignment.
     *
     * @return planned cost
     */
    /**
     * Sets the planned cost for this resource assignment.
     *
     * @param val planned cost
     */
    var baselineCost: Number?
        get() = getCachedValue(AssignmentField.BASELINE_COST)
        set(`val`) = set(AssignmentField.BASELINE_COST, `val`)

    /**
     * Returns the actual cost for this resource assignment.
     *
     * @return actual cost
     */
    /**
     * Sets the actual cost so far incurred for this resource assignment.
     *
     * @param actualCost actual cost
     */
    var actualCost: Number
        get() = getCachedValue(AssignmentField.ACTUAL_COST)
        set(actualCost) = set(AssignmentField.ACTUAL_COST, actualCost)

    /**
     * Returns the start of this resource assignment.
     *
     * @return start date
     */
    /**
     * Sets the start date for this resource assignment.
     *
     * @param val start date
     */
    var start: Date
        get() {
            var result = getCachedValue(AssignmentField.START) as Date?
            if (result == null) {
                result = task!!.start
            }
            return result
        }
        set(`val`) {
            set(AssignmentField.START, `val`)
        }

    /**
     * Returns the finish date for this resource assignment.
     *
     * @return finish date
     */
    /**
     * Sets the finish date for this resource assignment.
     *
     * @param val finish date
     */
    var finish: Date
        get() {
            var result = getCachedValue(AssignmentField.FINISH) as Date?
            if (result == null) {
                result = task!!.finish
            }
            return result
        }
        set(`val`) {
            set(AssignmentField.FINISH, `val`)
        }

    /**
     * Returns the delay for this resource assignment.
     *
     * @return delay
     */
    /**
     * Sets the delay for this resource assignment.
     *
     * @param dur delay
     */
    var delay: Duration
        get() = getCachedValue(AssignmentField.ASSIGNMENT_DELAY) as Duration?
        set(dur) {
            set(AssignmentField.ASSIGNMENT_DELAY, dur)
        }

    /**
     * Returns the resources unique id for this resource assignment.
     *
     * @return resources unique id
     */
    /**
     * Sets the resources unique id for this resource assignment.
     *
     * @param val resources unique id
     */
    var resourceUniqueID: Integer
        get() = getCachedValue(AssignmentField.RESOURCE_UNIQUE_ID) as Integer?
        set(`val`) {
            set(AssignmentField.RESOURCE_UNIQUE_ID, `val`)
        }

    /**
     * Gets the Resource Assignment Workgroup Fields if one exists.
     *
     * @return workgroup assignment object
     */
    val workgroupAssignment: ResourceAssignmentWorkgroupFields?
        get() = m_workgroup

    /**
     * This method retrieves a reference to the task with which this
     * assignment is associated.
     *
     * @return task
     */
    val task: Task?
        get() {
            if (m_task == null) {
                m_task = parentFile.getTaskByUniqueID(taskUniqueID)
            }
            return m_task
        }

    /**
     * This method retrieves a reference to the resource with which this
     * assignment is associated.
     *
     * @return resource
     */
    val resource: Resource?
        get() = parentFile.getResourceByUniqueID(resourceUniqueID)

    /**
     * This method returns the Work Contour type of this Assignment.
     *
     * @return the Work Contour type
     */
    /**
     * This method sets the Work Contour type of this Assignment.
     *
     * @param workContour the Work Contour type
     */
    var workContour: WorkContour
        get() = getCachedValue(AssignmentField.WORK_CONTOUR) as WorkContour?
        set(workContour) {
            set(AssignmentField.WORK_CONTOUR, workContour)
        }

    /**
     * Returns the remaining work for this resource assignment.
     *
     * @return remaining work
     */
    /**
     * Sets the remaining work for this resource assignment.
     *
     * @param remainingWork remaining work
     */
    var remainingWork: Duration
        get() = getCachedValue(AssignmentField.REMAINING_WORK) as Duration?
        set(remainingWork) {
            set(AssignmentField.REMAINING_WORK, remainingWork)
        }

    /**
     * Retrieves the leveling delay for this resource assignment.
     *
     * @return leveling delay
     */
    /**
     * Sets the leveling delay for this resource assignment.
     *
     * @param levelingDelay leveling delay
     */
    var levelingDelay: Duration
        get() = getCachedValue(AssignmentField.LEVELING_DELAY) as Duration?
        set(levelingDelay) {
            set(AssignmentField.LEVELING_DELAY, levelingDelay)
        }

    /**
     * Retrieves the timephased breakdown of the completed work for this
     * resource assignment.
     *
     * @return timephased completed work
     */
    val timephasedActualWork: List<TimephasedWork>?
        get() = if (m_timephasedActualWork == null) null else m_timephasedActualWork!!.getData()

    /**
     * Retrieves the timephased breakdown of the planned work for this
     * resource assignment.
     *
     * @return timephased planned work
     */
    val timephasedWork: List<TimephasedWork>?
        get() = if (m_timephasedWork == null) null else m_timephasedWork!!.data

    /**
     * Retrieves the timephased breakdown of the planned overtime work for this
     * resource assignment.
     *
     * @return timephased planned work
     */
    val timephasedOvertimeWork: List<TimephasedWork>?
        get() {
            if (m_timephasedOvertimeWork == null && m_timephasedWork != null && overtimeWork != null) {
                var perDayFactor = remainingOvertimeWork.getDuration() / (remainingWork.getDuration() - remainingOvertimeWork.getDuration())
                var totalFactor = remainingOvertimeWork.getDuration() / remainingWork.getDuration()

                perDayFactor = if (Double.isNaN(perDayFactor)) 0 else perDayFactor
                totalFactor = if (Double.isNaN(totalFactor)) 0 else totalFactor

                m_timephasedOvertimeWork = DefaultTimephasedWorkContainer(m_timephasedWork!!, perDayFactor, totalFactor)
            }
            return if (m_timephasedOvertimeWork == null) null else m_timephasedOvertimeWork!!.getData()
        }

    /**
     * Retrieves the timephased breakdown of the actual overtime work for this
     * resource assignment.
     *
     * @return timephased planned work
     */
    val timephasedActualOvertimeWork: List<TimephasedWork>?
        get() = if (m_timephasedActualOvertimeWork == null) null else m_timephasedActualOvertimeWork!!.getData()

    /**
     * Retrieves the timephased breakdown of cost.
     *
     * @return timephased cost
     */
    //for Work and Material resources, we will calculate in the normal way
    val timephasedCost: List<TimephasedCost>?
        get() {
            if (m_timephasedCost == null) {
                val r = resource
                val type = if (r != null) r.type else ResourceType.WORK
                if (type != ResourceType.COST) {
                    if (m_timephasedWork != null && m_timephasedWork!!.hasData()) {
                        if (hasMultipleCostRates()) {
                            m_timephasedCost = getTimephasedCostMultipleRates(timephasedWork!!, timephasedOvertimeWork!!)
                        } else {
                            m_timephasedCost = getTimephasedCostSingleRate(timephasedWork, timephasedOvertimeWork)
                        }
                    }
                } else {
                    m_timephasedCost = timephasedCostFixedAmount
                }

            }
            return m_timephasedCost
        }

    /**
     * Retrieves the timephased breakdown of actual cost.
     *
     * @return timephased actual cost
     */
    //for Work and Material resources, we will calculate in the normal way
    val timephasedActualCost: List<TimephasedCost>?
        get() {
            if (m_timephasedActualCost == null) {
                val r = resource
                val type = if (r != null) r.type else ResourceType.WORK
                if (type != ResourceType.COST) {
                    if (m_timephasedActualWork != null && m_timephasedActualWork!!.hasData()) {
                        if (hasMultipleCostRates()) {
                            m_timephasedActualCost = getTimephasedCostMultipleRates(timephasedActualWork!!, timephasedActualOvertimeWork!!)
                        } else {
                            m_timephasedActualCost = getTimephasedCostSingleRate(timephasedActualWork, timephasedActualOvertimeWork)
                        }
                    }
                } else {
                    m_timephasedActualCost = timephasedActualCostFixedAmount
                }

            }

            return m_timephasedActualCost
        }

    /**
     * Generates timephased costs from the assignment's cost value. Used for Cost type Resources.
     *
     * @return timephased cost
     */
    private//for prorated, we have to deal with it differently depending on whether or not
    //any actual has been entered, since we want to mimic the other timephased data
    //where planned and actual values do not overlap
    //need to get three possible blocks of data: one for the possible partial amount
    //overlap with timephased actual cost; one with all the standard amount days
    //that happen after the actual cost stops; and one with any remaining
    //partial day cost amount
    //see if there's anything left to work with
    //have to split up the amount into standard prorated amount days and whatever is left
    //no actual cost to worry about, so just a standard split from the beginning of the assignment
    val timephasedCostFixedAmount: List<TimephasedCost>
        get() {
            val result = LinkedList<TimephasedCost>()

            val cal = calendar

            var remainingCost = remainingCost.doubleValue()

            if (remainingCost > 0) {
                val accrueAt = resource!!.accrueAt

                if (accrueAt === AccrueType.START) {
                    result.add(splitCostStart(cal!!, remainingCost, start))
                } else if (accrueAt === AccrueType.END) {
                    result.add(splitCostEnd(cal!!, remainingCost, finish))
                } else {
                    val numWorkingDays = cal!!.getWork(start, finish, TimeUnit.DAYS).getDuration()
                    val standardAmountPerDay = cost!!.doubleValue() / numWorkingDays

                    if (actualCost.intValue() > 0) {

                        val numActualDaysUsed = Math.ceil(actualCost.doubleValue() / standardAmountPerDay) as Int
                        val actualWorkFinish = cal.getDate(start, Duration.getInstance(numActualDaysUsed, TimeUnit.DAYS), false)

                        val partialDayActualAmount = actualCost.doubleValue() % standardAmountPerDay

                        if (partialDayActualAmount > 0) {
                            val dayAmount = if (standardAmountPerDay < remainingCost) standardAmountPerDay - partialDayActualAmount else remainingCost

                            result.add(splitCostEnd(cal, dayAmount, actualWorkFinish))

                            remainingCost -= dayAmount
                        }
                        if (remainingCost > 0) {
                            result.addAll(splitCostProrated(cal, remainingCost, standardAmountPerDay, cal.getNextWorkStart(actualWorkFinish)))
                        }

                    } else {
                        result.addAll(splitCostProrated(cal, remainingCost, standardAmountPerDay, start))
                    }
                }
            }

            return result
        }

    /**
     * Generates timephased actual costs from the assignment's cost value. Used for Cost type Resources.
     *
     * @return timephased cost
     */
    private//for prorated, we have to deal with it differently; have to 'fill up' each
    //day with the standard amount before going to the next one
    val timephasedActualCostFixedAmount: List<TimephasedCost>
        get() {
            val result = LinkedList<TimephasedCost>()

            val actualCost = actualCost.doubleValue()

            if (actualCost > 0) {
                val accrueAt = resource!!.accrueAt

                if (accrueAt === AccrueType.START) {
                    result.add(splitCostStart(calendar!!, actualCost, actualStart))
                } else if (accrueAt === AccrueType.END) {
                    result.add(splitCostEnd(calendar!!, actualCost, actualFinish))
                } else {
                    val numWorkingDays = calendar!!.getWork(start, finish, TimeUnit.DAYS).getDuration()
                    val standardAmountPerDay = cost!!.doubleValue() / numWorkingDays

                    result.addAll(splitCostProrated(calendar, actualCost, standardAmountPerDay, actualStart))
                }
            }

            return result
        }

    /**
     * Retrieve a flag indicating if this resource assignment has timephased
     * data associated with it.
     *
     * @return true if this resource assignment has timephased data
     */
    val hasTimephasedData: Boolean
        get() = m_timephasedWork != null && m_timephasedWork!!.hasData() || m_timephasedActualWork != null && m_timephasedActualWork!!.hasData()

    /**
     * Retrieves the calendar used for this resource assignment.
     *
     * @return ProjectCalendar instance
     */
    val calendar: ProjectCalendar?
        get() {
            var calendar: ProjectCalendar? = null
            val resource = resource
            if (resource != null) {
                calendar = resource.resourceCalendar
            }

            val task = task
            if (calendar == null || task!!.ignoreResourceCalendar) {
                calendar = task!!.effectiveCalendar
            }

            return calendar
        }

    /**
     * Retrieve the variable rate time units, null if fixed rate.
     *
     * @return variable rate time units
     */
    /**
     * Set the variable rate time units, null if fixed rate.
     *
     * @param variableRateUnits variable rate units
     */
    var variableRateUnits: TimeUnit
        get() = getCachedValue(AssignmentField.VARIABLE_RATE_UNITS) as TimeUnit?
        set(variableRateUnits) {
            set(AssignmentField.VARIABLE_RATE_UNITS, variableRateUnits)
        }

    /**
     * Retrieve the parent task unique ID.
     *
     * @return task unique ID
     */
    /**
     * Set the parent task unique ID.
     *
     * @param id task unique ID
     */
    var taskUniqueID: Integer
        get() = getCachedValue(AssignmentField.TASK_UNIQUE_ID) as Integer?
        set(id) {
            set(AssignmentField.TASK_UNIQUE_ID, id)
        }

    /**
     * Retrieves the budget cost.
     *
     * @return budget cost
     */
    /**
     * Sets the budget cost.
     *
     * @param cost budget cost
     */
    var budgetCost: Number
        get() = getCachedValue(AssignmentField.BUDGET_COST)
        set(cost) = set(AssignmentField.BUDGET_COST, cost)

    /**
     * Retrieves the budget work value.
     *
     * @return budget work
     */
    /**
     * Sets the budget work value.
     *
     * @param work budget work
     */
    var budgetWork: Duration
        get() = getCachedValue(AssignmentField.BUDGET_WORK) as Duration?
        set(work) {
            set(AssignmentField.BUDGET_WORK, work)
        }

    /**
     * Retrieves the baseline budget cost.
     *
     * @return baseline budget cost
     */
    /**
     * Sets the baseline budget cost.
     *
     * @param cost baseline budget cost
     */
    var baselineBudgetCost: Number
        get() = getCachedValue(AssignmentField.BASELINE_BUDGET_COST)
        set(cost) = set(AssignmentField.BASELINE_BUDGET_COST, cost)

    /**
     * Retrieves the baseline budget work value.
     *
     * @return baseline budget work
     */
    /**
     * Sets the baseline budget work value.
     *
     * @param work baseline budget work
     */
    var baselineBudgetWork: Duration
        get() = getCachedValue(AssignmentField.BASELINE_BUDGET_WORK) as Duration?
        set(work) {
            set(AssignmentField.BASELINE_BUDGET_WORK, work)
        }

    /**
     * Returns the regular work of this resource assignment.
     *
     * @return work
     */
    /**
     * Sets the regular work for this resource assignment.
     *
     * @param dur work
     */
    var regularWork: Duration
        get() = getCachedValue(AssignmentField.REGULAR_WORK) as Duration?
        set(dur) {
            set(AssignmentField.REGULAR_WORK, dur)
        }

    /**
     * Returns the actual overtime work of this resource assignment.
     *
     * @return work
     */
    /**
     * Sets the actual overtime work for this resource assignment.
     *
     * @param dur work
     */
    var actualOvertimeWork: Duration
        get() = getCachedValue(AssignmentField.ACTUAL_OVERTIME_WORK) as Duration?
        set(dur) {
            set(AssignmentField.ACTUAL_OVERTIME_WORK, dur)
        }

    /**
     * Returns the remaining overtime work of this resource assignment.
     *
     * @return work
     */
    /**
     * Sets the remaining overtime work for this resource assignment.
     *
     * @param dur work
     */
    var remainingOvertimeWork: Duration
        get() = getCachedValue(AssignmentField.REMAINING_OVERTIME_WORK) as Duration?
        set(dur) {
            set(AssignmentField.REMAINING_OVERTIME_WORK, dur)
        }

    /**
     * Returns the overtime cost of this resource assignment.
     *
     * @return cost
     */
    /**
     * Sets the overtime cost for this resource assignment.
     *
     * @param cost cost
     */
    var overtimeCost: Number?
        get() {
            var cost = getCachedValue(AssignmentField.OVERTIME_COST) as Number?
            if (cost == null) {
                val actual = actualOvertimeCost
                val remaining = remainingOvertimeCost
                if (actual != null && remaining != null) {
                    cost = NumberHelper.getDouble(actual.doubleValue() + remaining.doubleValue())
                    set(AssignmentField.OVERTIME_COST, cost)
                }
            }
            return cost
        }
        set(cost) = set(AssignmentField.OVERTIME_COST, cost)

    /**
     * Returns the remaining cost of this resource assignment.
     *
     * @return cost
     */
    /**
     * Sets the remaining cost for this resource assignment.
     *
     * @param cost cost
     */
    var remainingCost: Number
        get() = getCachedValue(AssignmentField.REMAINING_COST)
        set(cost) = set(AssignmentField.REMAINING_COST, cost)

    /**
     * Returns the actual overtime cost of this resource assignment.
     *
     * @return cost
     */
    /**
     * Sets the actual overtime cost for this resource assignment.
     *
     * @param cost cost
     */
    var actualOvertimeCost: Number?
        get() = getCachedValue(AssignmentField.ACTUAL_OVERTIME_COST)
        set(cost) = set(AssignmentField.ACTUAL_OVERTIME_COST, cost)

    /**
     * Returns the remaining overtime cost of this resource assignment.
     *
     * @return cost
     */
    /**
     * Sets the remaining overtime cost for this resource assignment.
     *
     * @param cost cost
     */
    var remainingOvertimeCost: Number?
        get() = getCachedValue(AssignmentField.REMAINING_OVERTIME_COST)
        set(cost) = set(AssignmentField.REMAINING_OVERTIME_COST, cost)

    /**
     * The BCWP (budgeted cost of work performed) field contains
     * the cumulative value of the assignment's timephased percent complete
     * multiplied by the assignment's timephased baseline cost.
     * BCWP is calculated up to the status date or today's date.
     * This information is also known as earned value.
     *
     * @return currency amount as float
     */
    /**
     * The BCWP (budgeted cost of work performed) field contains the
     * cumulative value
     * of the assignment's timephased percent complete multiplied by
     * the assignments
     * timephased baseline cost. BCWP is calculated up to the status
     * date or todays
     * date. This information is also known as earned value.
     *
     * @param val the amount to be set
     */
    var bcwp: Number?
        get() = getCachedValue(AssignmentField.BCWP)
        set(`val`) = set(AssignmentField.BCWP, `val`)

    /**
     * The BCWS (budgeted cost of work scheduled) field contains the cumulative
     * timephased baseline costs up to the status date or today's date.
     *
     * @return currency amount as float
     */
    /**
     * The BCWS (budgeted cost of work scheduled) field contains the cumulative
     * timephased baseline costs up to the status date or today's date.
     *
     * @param val the amount to set
     */
    var bcws: Number?
        get() = getCachedValue(AssignmentField.BCWS)
        set(`val`) = set(AssignmentField.BCWS, `val`)

    /**
     * Retrieve the ACWP value.
     *
     * @return ACWP value
     */
    /**
     * Set the ACWP value.
     *
     * @param acwp ACWP value
     */
    var acwp: Number
        get() = getCachedValue(AssignmentField.ACWP)
        set(acwp) = set(AssignmentField.ACWP, acwp)

    /**
     * The SV (earned value schedule variance) field shows the difference in
     * cost terms between the current progress and the baseline plan of the
     * task up to the status date or today's date. You can use SV to
     * check costs to determine whether tasks are on schedule.
     *
     * @return -earned value schedule variance
     */
    /**
     * The SV (earned value schedule variance) field shows the difference
     * in cost terms between the current progress and the baseline plan
     * of the task up to the status date or today's date. You can use SV
     * to check costs to determine whether tasks are on schedule.
     * @param val - currency amount
     */
    var sv: Number?
        get() {
            var variance = getCachedValue(AssignmentField.SV) as Number?
            if (variance == null) {
                val bcwp = bcwp
                val bcws = bcws
                if (bcwp != null && bcws != null) {
                    variance = NumberHelper.getDouble(bcwp.doubleValue() - bcws.doubleValue())
                    set(AssignmentField.SV, variance)
                }
            }
            return variance
        }
        set(`val`) = set(AssignmentField.SV, `val`)

    /**
     * The CV (earned value cost variance) field shows the difference between
     * how much it should have cost to achieve the current level of completion
     * on the task, and how much it has actually cost to achieve the current
     * level of completion up to the status date or today's date.
     * How Calculated   CV is the difference between BCWP
     * (budgeted cost of work performed) and ACWP
     * (actual cost of work performed). Microsoft Project calculates
     * the CV as follows: CV = BCWP - ACWP
     *
     * @return sum of earned value cost variance
     */
    /**
     * The CV (earned value cost variance) field shows the difference
     * between how much it should have cost to achieve the current level of
     * completion on the task, and how much it has actually cost to achieve the
     * current level of completion up to the status date or today's date.
     *
     * @param val value to set
     */
    var cv: Number
        get() {
            var variance = getCachedValue(AssignmentField.CV) as Number?
            if (variance == null) {
                variance = Double.valueOf(NumberHelper.getDouble(bcwp) - NumberHelper.getDouble(acwp))
                set(AssignmentField.CV, variance)
            }
            return variance
        }
        set(`val`) = set(AssignmentField.CV, `val`)

    /**
     * The Cost Variance field shows the difference between the baseline cost
     * and total cost for a task. The total cost is the current estimate of costs
     * based on actual costs and remaining costs. This is also referred to as
     * variance at completion (VAC).
     *
     * @return amount
     */
    /**
     * The Cost Variance field shows the difference between the
     * baseline cost and total cost for a task. The total cost is the
     * current estimate of costs based on actual costs and remaining costs.
     * This is also referred to as variance at completion (VAC).
     *
     * @param val amount
     */
    var costVariance: Number?
        get() {
            var variance = getCachedValue(AssignmentField.COST_VARIANCE) as Number?
            if (variance == null) {
                val cost = cost
                val baselineCost = baselineCost
                if (cost != null && baselineCost != null) {
                    variance = NumberHelper.getDouble(cost.doubleValue() - baselineCost.doubleValue())
                    set(AssignmentField.COST_VARIANCE, variance)
                }
            }
            return variance
        }
        set(`val`) = set(AssignmentField.COST_VARIANCE, `val`)

    /**
     * The % Work Complete field contains the current status of a task,
     * expressed as the percentage of the task's work that has been completed.
     * You can enter percent work complete, or you can have Microsoft Project
     * calculate it for you based on actual work on the task.
     *
     * @return percentage as float
     */
    /**
     * The % Work Complete field contains the current status of a task,
     * expressed as the
     * percentage of the task's work that has been completed. You can enter
     * percent work
     * complete, or you can have Microsoft Project calculate it for you
     * based on actual
     * work on the task.
     *
     * @param val value to be set
     */
    var percentageWorkComplete: Number?
        get() {
            var pct = getCachedValue(AssignmentField.PERCENT_WORK_COMPLETE) as Number?
            if (pct == null) {
                val actualWork = actualWork
                val work = work
                if (actualWork != null && work != null && work!!.getDuration() !== 0) {
                    pct = Double.valueOf(actualWork!!.getDuration() * 100 / work!!.convertUnits(actualWork!!.getUnits(), parentFile.projectProperties).getDuration())
                    set(AssignmentField.PERCENT_WORK_COMPLETE, pct)
                }
            }
            return pct
        }
        set(`val`) = set(AssignmentField.PERCENT_WORK_COMPLETE, `val`)

    /**
     * The Notes field contains notes that you can enter about a task.
     * You can use task notes to help maintain a history for a task.
     *
     * @return notes
     */
    /**
     * This method is used to add notes to the current task.
     *
     * @param notes notes to be added
     */
    var notes: String
        get() {
            val notes = getCachedValue(AssignmentField.NOTES) as String?
            return notes ?: ""
        }
        set(notes) = set(AssignmentField.NOTES, notes)

    /**
     * The Confirmed field indicates whether all resources assigned to a task
     * have accepted or rejected the task assignment in response to a TeamAssign
     * message regarding their assignments.
     *
     * @return boolean
     */
    /**
     * The Confirmed field indicates whether all resources assigned to a task have
     * accepted or rejected the task assignment in response to a TeamAssign message
     * regarding their assignments.
     *
     * @param val boolean value
     */
    var confirmed: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(AssignmentField.CONFIRMED) as Boolean?)
        set(`val`) {
            set(AssignmentField.CONFIRMED, `val`)
        }

    /**
     * The Update Needed field indicates whether a TeamUpdate message
     * should be sent to the assigned resources because of changes to the
     * start date, finish date, or resource reassignments of the task.
     *
     * @return true if needed.
     */
    /**
     * The Update Needed field indicates whether a TeamUpdate message should
     * be sent to the assigned resources because of changes to the start date,
     * finish date, or resource reassignments of the task.
     *
     * @param val - boolean
     */
    var updateNeeded: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(AssignmentField.UPDATE_NEEDED) as Boolean?)
        set(`val`) {
            set(AssignmentField.UPDATE_NEEDED, `val`)
        }

    /**
     * The Linked Fields field indicates whether there are OLE links to the task,
     * either from elsewhere in the active project, another Microsoft Project file,
     * or from another program.
     *
     * @return boolean
     */
    /**
     * The Linked Fields field indicates whether there are OLE links to the task,
     * either from elsewhere in the active project, another Microsoft Project
     * file, or from another program.
     *
     * @param flag boolean value
     */
    var linkedFields: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(AssignmentField.LINKED_FIELDS) as Boolean?)
        set(flag) {
            set(AssignmentField.LINKED_FIELDS, flag)
        }

    /**
     * Retrieves the task hyperlink attribute.
     *
     * @return hyperlink attribute
     */
    /**
     * Sets the task hyperlink attribute.
     *
     * @param text hyperlink attribute
     */
    var hyperlink: String
        get() = getCachedValue(AssignmentField.HYPERLINK)
        set(text) = set(AssignmentField.HYPERLINK, text)

    /**
     * Retrieves the task hyperlink address attribute.
     *
     * @return hyperlink address attribute
     */
    /**
     * Sets the task hyperlink address attribute.
     *
     * @param text hyperlink address attribute
     */
    var hyperlinkAddress: String
        get() = getCachedValue(AssignmentField.HYPERLINK_ADDRESS)
        set(text) = set(AssignmentField.HYPERLINK_ADDRESS, text)

    /**
     * Retrieves the task hyperlink sub-address attribute.
     *
     * @return hyperlink sub address attribute
     */
    /**
     * Sets the task hyperlink sub address attribute.
     *
     * @param text hyperlink sub address attribute
     */
    var hyperlinkSubAddress: String
        get() = getCachedValue(AssignmentField.HYPERLINK_SUBADDRESS)
        set(text) = set(AssignmentField.HYPERLINK_SUBADDRESS, text)

    /**
     * The Work Variance field contains the difference between a task's
     * baseline work and the currently scheduled work.
     *
     * @return Duration representing duration.
     */
    /**
     * The Work Variance field contains the difference between a task's baseline
     * work and the currently scheduled work.
     *
     * @param val - duration
     */
    var workVariance: Duration?
        get() {
            var variance = getCachedValue(AssignmentField.WORK_VARIANCE) as Duration?
            if (variance == null) {
                val work = work
                val baselineWork = baselineWork
                if (work != null && baselineWork != null) {
                    variance = Duration.getInstance(work!!.getDuration() - baselineWork!!.convertUnits(work!!.getUnits(), parentFile.projectProperties).getDuration(), work!!.getUnits())
                    set(AssignmentField.WORK_VARIANCE, variance)
                }
            }
            return variance
        }
        set(`val`) {
            set(AssignmentField.WORK_VARIANCE, `val`)
        }

    /**
     * Calculate the start variance.
     *
     * @return start variance
     */
    /**
     * The Start Variance field contains the amount of time that represents the
     * difference between a task's baseline start date and its currently
     * scheduled start date.
     *
     * @param val - duration
     */
    var startVariance: Duration
        get() {
            var variance = getCachedValue(AssignmentField.START_VARIANCE) as Duration?
            if (variance == null) {
                val format = parentFile.projectProperties.defaultDurationUnits
                variance = DateHelper.getVariance(task, baselineStart, start, format)
                set(AssignmentField.START_VARIANCE, variance)
            }
            return variance
        }
        set(`val`) {
            set(AssignmentField.START_VARIANCE, `val`)
        }

    /**
     * Calculate the finish variance.
     *
     * @return finish variance
     */
    /**
     * The Finish Variance field contains the amount of time that represents the
     * difference between a task's baseline finish date and its forecast
     * or actual finish date.
     *
     * @param duration duration value
     */
    var finishVariance: Duration
        get() {
            var variance = getCachedValue(AssignmentField.FINISH_VARIANCE) as Duration?
            if (variance == null) {
                val format = parentFile.projectProperties.defaultDurationUnits
                variance = DateHelper.getVariance(task, baselineFinish, finish, format)
                set(AssignmentField.FINISH_VARIANCE, variance)
            }
            return variance
        }
        set(duration) {
            set(AssignmentField.FINISH_VARIANCE, duration)
        }

    /**
     * The Created field contains the date and time when a task was added
     * to the project.
     *
     * @return Date
     */
    /**
     * The Created field contains the date and time when a task was
     * added to the project.
     *
     * @param val date
     */
    var createDate: Date
        get() = getCachedValue(AssignmentField.CREATED) as Date?
        set(`val`) {
            set(AssignmentField.CREATED, `val`)
        }

    /**
     * Retrieve the task GUID.
     *
     * @return task GUID
     */
    /**
     * Set the task GUID.
     *
     * @param value task GUID
     */
    var guid: UUID
        get() = getCachedValue(AssignmentField.GUID) as UUID?
        set(value) {
            set(AssignmentField.GUID, value)
        }

    /**
     * Retrieves a flag to indicate if a response has been received from a resource
     * assigned to a task.
     *
     * @return boolean value
     */
    /**
     * Sets a flag to indicate if a response has been received from a resource
     * assigned to a task.
     *
     * @param val boolean value
     */
    var responsePending: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(AssignmentField.RESPONSE_PENDING) as Boolean?)
        set(`val`) {
            set(AssignmentField.RESPONSE_PENDING, `val`)
        }

    /**
     * Retrieves a flag to indicate if a response has been received from a resource
     * assigned to a task.
     *
     * @return boolean value
     */
    /**
     * Sets a flag to indicate if a response has been received from a resource
     * assigned to a task.
     *
     * @param val boolean value
     */
    var teamStatusPending: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(AssignmentField.TEAM_STATUS_PENDING) as Boolean?)
        set(`val`) {
            set(AssignmentField.TEAM_STATUS_PENDING, `val`)
        }

    /**
     * Returns the VAC for this resource assignment.
     *
     * @return VAC value
     */
    /**
     * Sets VAC for this resource assignment.
     *
     * @param value VAC value
     */
    var vac: Number
        get() = getCachedValue(AssignmentField.VAC)
        set(value) = set(AssignmentField.VAC, value)

    /**
     * Returns the cost rate table index for this assignment.
     *
     * @return cost rate table index
     */
    /**
     * Sets the index of the cost rate table for this assignment.
     *
     * @param index cost rate table index
     */
    var costRateTableIndex: Int
        get() {
            val value = getCachedValue(AssignmentField.COST_RATE_TABLE) as Integer?
            return if (value == null) 0 else value!!.intValue()
        }
        set(index) {
            set(AssignmentField.COST_RATE_TABLE, Integer.valueOf(index))
        }

    /**
     * Returns the cost rate table for this assignment.
     *
     * @return cost rate table index
     */
    val costRateTable: CostRateTable?
        get() = if (resource == null) null else resource!!.getCostRateTable(costRateTableIndex)

    /**
     * Retrieves the hyperlink screen tip attribute.
     *
     * @return hyperlink screen tip attribute
     */
    /**
     * Sets the hyperlink screen tip attribute.
     *
     * @param text hyperlink screen tip attribute
     */
    var hyperlinkScreenTip: String
        get() = getCachedValue(AssignmentField.HYPERLINK_SCREEN_TIP)
        set(text) = set(AssignmentField.HYPERLINK_SCREEN_TIP, text)

    /**
     * Retrieves the resource request type attribute.
     *
     * @return resource request type
     */
    /**
     * Sets the resource request type attribute.
     *
     * @param type resource request type
     */
    var resourceRequestType: ResourceRequestType?
        get() = getCachedValue(AssignmentField.RESOURCE_REQUEST_TYPE)
        set(type) = set(AssignmentField.RESOURCE_REQUEST_TYPE, type)

    /**
     * Retrieve the stop date.
     *
     * @return stop date
     */
    /**
     * Set the stop date.
     *
     * @param stop stop date
     */
    var stop: Date
        get() = getCachedValue(AssignmentField.STOP) as Date?
        set(stop) {
            set(AssignmentField.STOP, stop)
        }

    /**
     * Retrieve the resume date.
     *
     * @return resume date
     */
    /**
     * Set the resume date.
     *
     * @param resume resume date
     */
    var resume: Date
        get() = getCachedValue(AssignmentField.RESUME) as Date?
        set(resume) {
            set(AssignmentField.RESUME, resume)
        }

    /**
     * Array of field values.
     */
    private val m_array = arrayOfNulls<Object>(AssignmentField.MAX_VALUE)

    private var m_eventsEnabled = true

    private var m_timephasedWork: DefaultTimephasedWorkContainer? = null
    private var m_timephasedCost: List<TimephasedCost>? = null

    private var m_timephasedActualWork: TimephasedWorkContainer? = null
    private var m_timephasedActualCost: List<TimephasedCost>? = null

    private var m_timephasedOvertimeWork: TimephasedWorkContainer? = null
    private var m_timephasedActualOvertimeWork: TimephasedWorkContainer? = null

    private var m_listeners: List<FieldListener>? = null
    private val m_timephasedBaselineWork = arrayOfNulls<TimephasedWorkContainer>(11)
    private val m_timephasedBaselineCost = arrayOfNulls<TimephasedCostContainer>(11)

    /**
     * Child record for Workgroup fields.
     */
    private var m_workgroup: ResourceAssignmentWorkgroupFields? = null

    init {

        if (file.projectConfig.autoAssignmentUniqueID == true) {
            uniqueID = Integer.valueOf(file.projectConfig.nextAssignmentUniqueID)
        }
    }

    /**
     * This method allows a resource assignment workgroup fields record
     * to be added to the current resource assignment. A maximum of
     * one of these records can be added to a resource assignment record.
     *
     * @return ResourceAssignmentWorkgroupFields object
     * @throws MPXJException if MSP defined limit of 1 is exceeded
     */
    @Throws(MPXJException::class)
    fun addWorkgroupAssignment(): ResourceAssignmentWorkgroupFields {
        if (m_workgroup != null) {
            throw MPXJException(MPXJException.MAXIMUM_RECORDS)
        }

        m_workgroup = ResourceAssignmentWorkgroupFields()

        return m_workgroup
    }

    /**
     * Removes this resource assignment from the project.
     */
    fun remove() {
        parentFile.resourceAssignments.remove(this.toInt())
    }

    /**
     * Sets the timephased breakdown of the completed work for this
     * resource assignment.
     *
     * @param data timephased data
     */
    fun setTimephasedActualWork(data: TimephasedWorkContainer) {
        m_timephasedActualWork = data
    }

    /**
     * Sets the timephased breakdown of the planned work for this
     * resource assignment.
     *
     * @param data timephased data
     */
    fun setTimephasedWork(data: DefaultTimephasedWorkContainer) {
        m_timephasedWork = data
    }

    /**
     * Sets the timephased breakdown of the actual overtime work
     * for this assignment.
     *
     * @param data timephased work
     */
    fun setTimephasedActualOvertimeWork(data: TimephasedWorkContainer) {
        m_timephasedActualOvertimeWork = data
    }

    /**
     * Generates timephased costs from timephased work where a single cost rate
     * applies to the whole assignment.
     *
     * @param standardWorkList timephased work
     * @param overtimeWorkList timephased work
     * @return timephased cost
     */
    private fun getTimephasedCostSingleRate(standardWorkList: List<TimephasedWork>?, overtimeWorkList: List<TimephasedWork>?): List<TimephasedCost> {
        val result = LinkedList<TimephasedCost>()

        //just return an empty list if there is no timephased work passed in
        if (standardWorkList == null) {
            return result
        }

        //takes care of the situation where there is no timephased overtime work
        val overtimeIterator = overtimeWorkList?.iterator() ?: java.util.Collections.emptyList().iterator()

        for (standardWork in standardWorkList) {
            val rate = getCostRateTableEntry(standardWork.getStart())
            val standardRateValue = rate.getStandardRate().getAmount()
            val standardRateUnits = rate.getStandardRate().getUnits()
            var overtimeRateValue = 0.0
            var overtimeRateUnits = standardRateUnits

            if (rate.getOvertimeRate() != null) {
                overtimeRateValue = rate.getOvertimeRate().getAmount()
                overtimeRateUnits = rate.getOvertimeRate().getUnits()
            }

            val overtimeWork = if (overtimeIterator.hasNext()) overtimeIterator.next() else null

            var standardWorkPerDay = standardWork.getAmountPerDay()
            if (standardWorkPerDay.getUnits() !== standardRateUnits) {
                standardWorkPerDay = standardWorkPerDay.convertUnits(standardRateUnits, parentFile.projectProperties)
            }

            var totalStandardWork = standardWork.getTotalAmount()
            if (totalStandardWork.getUnits() !== standardRateUnits) {
                totalStandardWork = totalStandardWork.convertUnits(standardRateUnits, parentFile.projectProperties)
            }

            val overtimeWorkPerDay: Duration
            val totalOvertimeWork: Duration

            if (overtimeWork == null || overtimeWork!!.getTotalAmount().getDuration() === 0) {
                overtimeWorkPerDay = Duration.getInstance(0, standardWorkPerDay.getUnits())
                totalOvertimeWork = Duration.getInstance(0, standardWorkPerDay.getUnits())
            } else {
                overtimeWorkPerDay = overtimeWork!!.getAmountPerDay()
                if (overtimeWorkPerDay.getUnits() !== overtimeRateUnits) {
                    overtimeWorkPerDay = overtimeWorkPerDay.convertUnits(overtimeRateUnits, parentFile.projectProperties)
                }

                totalOvertimeWork = overtimeWork!!.getTotalAmount()
                if (totalOvertimeWork.getUnits() !== overtimeRateUnits) {
                    totalOvertimeWork = totalOvertimeWork.convertUnits(overtimeRateUnits, parentFile.projectProperties)
                }
            }

            val costPerDay = standardWorkPerDay.getDuration() * standardRateValue + overtimeWorkPerDay.getDuration() * overtimeRateValue
            val totalCost = totalStandardWork.getDuration() * standardRateValue + totalOvertimeWork.getDuration() * overtimeRateValue

            //if the overtime work does not span the same number of days as the work,
            //then we have to split this into two TimephasedCost values
            if (overtimeWork == null || overtimeWork!!.getFinish().equals(standardWork.getFinish())) {
                //normal way
                val cost = TimephasedCost()
                cost.setStart(standardWork.getStart())
                cost.setFinish(standardWork.getFinish())
                cost.setModified(standardWork.getModified())
                cost.setAmountPerDay(Double.valueOf(costPerDay))
                cost.setTotalAmount(Double.valueOf(totalCost))
                result.add(cost)

            } else {
                //prorated way
                result.addAll(splitCostProrated(calendar, totalCost, costPerDay, standardWork.getStart()))
            }

        }

        return result
    }

    /**
     * Generates timephased costs from timephased work where multiple cost rates
     * apply to the assignment.
     *
     * @param standardWorkList timephased work
     * @param overtimeWorkList timephased work
     * @return timephased cost
     */
    private fun getTimephasedCostMultipleRates(standardWorkList: List<TimephasedWork>, overtimeWorkList: List<TimephasedWork>): List<TimephasedCost> {
        val standardWorkResult = LinkedList<TimephasedWork>()
        val overtimeWorkResult = LinkedList<TimephasedWork>()
        val table = costRateTable
        val calendar = calendar

        val iter = overtimeWorkList.iterator()
        for (standardWork in standardWorkList) {
            val overtimeWork = if (iter.hasNext()) iter.next() else null

            val startIndex = getCostRateTableEntryIndex(standardWork.getStart())
            val finishIndex = getCostRateTableEntryIndex(standardWork.getFinish())

            if (startIndex == finishIndex) {
                standardWorkResult.add(standardWork)
                if (overtimeWork != null) {
                    overtimeWorkResult.add(overtimeWork)
                }
            } else {
                standardWorkResult.addAll(splitWork(table!!, calendar, standardWork, startIndex))
                if (overtimeWork != null) {
                    overtimeWorkResult.addAll(splitWork(table!!, calendar, overtimeWork!!, startIndex))
                }
            }
        }

        return getTimephasedCostSingleRate(standardWorkResult, overtimeWorkResult)
    }

    /**
     * Used for Cost type Resources.
     *
     * Generates a TimphasedCost block for the total amount on the start date. This is useful
     * for Cost resources that have an AccrueAt value of Start.
     *
     * @param calendar calendar used by this assignment
     * @param totalAmount cost amount for this block
     * @param start start date of the timephased cost block
     * @return timephased cost
     */
    private fun splitCostStart(calendar: ProjectCalendar, totalAmount: Double, start: Date): TimephasedCost {
        val cost = TimephasedCost()
        cost.setStart(start)
        cost.setFinish(calendar.getDate(start, Duration.getInstance(1, TimeUnit.DAYS), false))
        cost.setAmountPerDay(Double.valueOf(totalAmount))
        cost.setTotalAmount(Double.valueOf(totalAmount))

        return cost
    }

    /**
     * Used for Cost type Resources.
     *
     * Generates a TimphasedCost block for the total amount on the finish date. This is useful
     * for Cost resources that have an AccrueAt value of End.
     *
     * @param calendar calendar used by this assignment
     * @param totalAmount cost amount for this block
     * @param finish finish date of the timephased cost block
     * @return timephased cost
     */
    private fun splitCostEnd(calendar: ProjectCalendar, totalAmount: Double, finish: Date): TimephasedCost {
        val cost = TimephasedCost()
        cost.setStart(calendar.getStartDate(finish, Duration.getInstance(1, TimeUnit.DAYS)))
        cost.setFinish(finish)
        cost.setAmountPerDay(Double.valueOf(totalAmount))
        cost.setTotalAmount(Double.valueOf(totalAmount))

        return cost
    }

    /**
     * Used for Cost type Resources.
     *
     * Generates up to two TimephasedCost blocks for a cost amount. The first block will contain
     * all the days using the standardAmountPerDay, and a second block will contain any
     * final amount that is not enough for a complete day. This is useful for Cost resources
     * who have an AccrueAt value of Prorated.
     *
     * @param calendar calendar used by this assignment
     * @param totalAmount cost amount to be prorated
     * @param standardAmountPerDay cost amount for a normal working day
     * @param start date of the first timephased cost block
     * @return timephased cost
     */
    private fun splitCostProrated(calendar: ProjectCalendar?, totalAmount: Double, standardAmountPerDay: Double, start: Date): List<TimephasedCost> {
        var start = start
        val result = LinkedList<TimephasedCost>()

        val numStandardAmountDays = Math.floor(totalAmount / standardAmountPerDay)
        val amountForLastDay = totalAmount % standardAmountPerDay

        //first block contains all the normal work at the beginning of the assignments life, if any

        if (numStandardAmountDays > 0) {
            val finishStandardBlock = calendar!!.getDate(start, Duration.getInstance(numStandardAmountDays, TimeUnit.DAYS), false)

            val standardBlock = TimephasedCost()
            standardBlock.setAmountPerDay(Double.valueOf(standardAmountPerDay))
            standardBlock.setStart(start)
            standardBlock.setFinish(finishStandardBlock)
            standardBlock.setTotalAmount(Double.valueOf(numStandardAmountDays * standardAmountPerDay))

            result.add(standardBlock)

            start = calendar.getNextWorkStart(finishStandardBlock)
        }

        //next block contains the partial day amount, if any
        if (amountForLastDay > 0) {
            val nextBlock = TimephasedCost()
            nextBlock.setAmountPerDay(Double.valueOf(amountForLastDay))
            nextBlock.setTotalAmount(Double.valueOf(amountForLastDay))
            nextBlock.setStart(start)
            nextBlock.setFinish(calendar!!.getDate(start, Duration.getInstance(1, TimeUnit.DAYS), false))

            result.add(nextBlock)
        }

        return result
    }

    /**
     * Splits timephased work segments in line with cost rates. Note that this is
     * an approximation - where a rate changes during a working day, the second
     * rate is used for the whole day.
     *
     * @param table cost rate table
     * @param calendar calendar used by this assignment
     * @param work timephased work segment
     * @param rateIndex rate applicable at the start of the timephased work segment
     * @return list of segments which replace the one supplied by the caller
     */
    private fun splitWork(table: CostRateTable, calendar: ProjectCalendar?, work: TimephasedWork, rateIndex: Int): List<TimephasedWork> {
        var rateIndex = rateIndex
        val result = LinkedList<TimephasedWork>()
        work.setTotalAmount(Duration.getInstance(0, work.getAmountPerDay().getUnits()))

        while (true) {
            val rate = table.get(rateIndex)
            val splitDate = rate.getEndDate()
            if (splitDate.getTime() >= work.getFinish().getTime()) {
                result.add(work)
                break
            }

            val currentPeriodEnd = calendar!!.getPreviousWorkFinish(splitDate)

            val currentPeriod = TimephasedWork(work)
            currentPeriod.setFinish(currentPeriodEnd)
            result.add(currentPeriod)

            val nextPeriodStart = calendar.getNextWorkStart(splitDate)
            work.setStart(nextPeriodStart)

            ++rateIndex
        }

        return result
    }

    /**
     * Used to determine if multiple cost rates apply to this assignment.
     *
     * @return true if multiple cost rates apply to this assignment
     */
    private fun hasMultipleCostRates(): Boolean {
        var result = false
        val table = costRateTable
        if (table != null) {
            //
            // We assume here that if there is just one entry in the cost rate
            // table, this is an open ended rate which covers any work, it won't
            // have specific dates attached to it.
            //
            if (table!!.size() > 1) {
                //
                // If we have multiple rates in the table, see if the same rate
                // is in force at the start and the end of the aaaignment.
                //
                val startEntry = table!!.getEntryByDate(start)
                val finishEntry = table!!.getEntryByDate(finish)
                result = startEntry !== finishEntry
            }
        }
        return result
    }

    /**
     * Retrieves the cost rate table entry active on a given date.
     *
     * @param date target date
     * @return cost rate table entry
     */
    private fun getCostRateTableEntry(date: Date): CostRateTableEntry {
        val result: CostRateTableEntry

        val table = costRateTable
        if (table == null) {
            val resource = resource
            result = CostRateTableEntry(resource!!.standardRate, TimeUnit.HOURS, resource.overtimeRate, TimeUnit.HOURS, resource.costPerUse, null)
        } else {
            if (table!!.size() === 1) {
                result = table!!.get(0)
            } else {
                result = table!!.getEntryByDate(date)
            }
        }

        return result
    }

    /**
     * Retrieves the index of a cost rate table entry active on a given date.
     *
     * @param date target date
     * @return cost rate table entry index
     */
    private fun getCostRateTableEntryIndex(date: Date): Int {
        var result = -1

        val table = costRateTable
        if (table != null) {
            if (table!!.size() === 1) {
                result = 0
            } else {
                result = table!!.getIndexByDate(date)
            }
        }

        return result
    }

    /**
     * Set timephased baseline work. Note that index 0 represents "Baseline",
     * index 1 represents "Baseline1" and so on.
     *
     * @param index baseline index
     * @param data timephased data
     */
    fun setTimephasedBaselineWork(index: Int, data: TimephasedWorkContainer) {
        m_timephasedBaselineWork[index] = data
    }

    /**
     * Set timephased baseline cost. Note that index 0 represents "Baseline",
     * index 1 represents "Baseline1" and so on.
     *
     * @param index baseline index
     * @param data timephased data
     */
    fun setTimephasedBaselineCost(index: Int, data: TimephasedCostContainer) {
        m_timephasedBaselineCost[index] = data
    }

    /**
     * Retrieve timephased baseline work. Note that index 0 represents "Baseline",
     * index 1 represents "Baseline1" and so on.
     *
     * @param index baseline index
     * @return timephased work, or null if no baseline is present
     */
    fun getTimephasedBaselineWork(index: Int): List<TimephasedWork>? {
        return if (m_timephasedBaselineWork[index] == null) null else m_timephasedBaselineWork[index].getData()
    }

    /**
     * Retrieve timephased baseline cost. Note that index 0 represents "Baseline",
     * index 1 represents "Baseline1" and so on.
     *
     * @param index baseline index
     * @return timephased work, or null if no baseline is present
     */
    fun getTimephasedBaselineCost(index: Int): List<TimephasedCost>? {
        return if (m_timephasedBaselineCost[index] == null) null else m_timephasedBaselineCost[index].getData()
    }

    /**
     * Set a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @param value baseline value
     */
    fun setBaselineCost(baselineNumber: Int, value: Number) {
        set(selectField(AssignmentFieldLists.BASELINE_COSTS, baselineNumber), value)
    }

    /**
     * Set a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @param value baseline value
     */
    fun setBaselineWork(baselineNumber: Int, value: Duration) {
        set(selectField(AssignmentFieldLists.BASELINE_WORKS, baselineNumber), value)
    }

    /**
     * Retrieve a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @return baseline value
     */
    fun getBaselineWork(baselineNumber: Int): Duration {
        return getCachedValue(selectField(AssignmentFieldLists.BASELINE_WORKS, baselineNumber)) as Duration?
    }

    /**
     * Retrieve a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @return baseline value
     */
    fun getBaselineCost(baselineNumber: Int): Number {
        return getCachedValue(selectField(AssignmentFieldLists.BASELINE_COSTS, baselineNumber))
    }

    /**
     * Set a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @param value baseline value
     */
    fun setBaselineStart(baselineNumber: Int, value: Date) {
        set(selectField(AssignmentFieldLists.BASELINE_STARTS, baselineNumber), value)
    }

    /**
     * Retrieve a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @return baseline value
     */
    fun getBaselineStart(baselineNumber: Int): Date {
        return getCachedValue(selectField(AssignmentFieldLists.BASELINE_STARTS, baselineNumber)) as Date?
    }

    /**
     * Set a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @param value baseline value
     */
    fun setBaselineFinish(baselineNumber: Int, value: Date) {
        set(selectField(AssignmentFieldLists.BASELINE_FINISHES, baselineNumber), value)
    }

    /**
     * Retrieve a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @return baseline value
     */
    fun getBaselineFinish(baselineNumber: Int): Date {
        return getCachedValue(selectField(AssignmentFieldLists.BASELINE_FINISHES, baselineNumber)) as Date?
    }

    /**
     * Set a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @param value baseline value
     */
    fun setBaselineBudgetCost(baselineNumber: Int, value: Number) {
        set(selectField(AssignmentFieldLists.BASELINE_BUDGET_COSTS, baselineNumber), value)
    }

    /**
     * Set a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @param value baseline value
     */
    fun setBaselineBudgetWork(baselineNumber: Int, value: Duration) {
        set(selectField(AssignmentFieldLists.BASELINE_BUDGET_WORKS, baselineNumber), value)
    }

    /**
     * Retrieve a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @return baseline value
     */
    fun getBaselineBudgetWork(baselineNumber: Int): Duration {
        return getCachedValue(selectField(AssignmentFieldLists.BASELINE_BUDGET_WORKS, baselineNumber)) as Duration?
    }

    /**
     * Retrieve a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @return baseline value
     */
    fun getBaselineBudgetCost(baselineNumber: Int): Number {
        return getCachedValue(selectField(AssignmentFieldLists.BASELINE_BUDGET_COSTS, baselineNumber))
    }

    /**
     * Set a text value.
     *
     * @param index text index (1-30)
     * @param value text value
     */
    fun setText(index: Int, value: String) {
        set(selectField(AssignmentFieldLists.CUSTOM_TEXT, index), value)
    }

    /**
     * Retrieve a text value.
     *
     * @param index text index (1-30)
     * @return text value
     */
    fun getText(index: Int): String {
        return getCachedValue(selectField(AssignmentFieldLists.CUSTOM_TEXT, index))
    }

    /**
     * Set a start value.
     *
     * @param index start index (1-10)
     * @param value start value
     */
    fun setStart(index: Int, value: Date) {
        set(selectField(AssignmentFieldLists.CUSTOM_START, index), value)
    }

    /**
     * Retrieve a start value.
     *
     * @param index start index (1-10)
     * @return start value
     */
    fun getStart(index: Int): Date {
        return getCachedValue(selectField(AssignmentFieldLists.CUSTOM_START, index)) as Date?
    }

    /**
     * Set a finish value.
     *
     * @param index finish index (1-10)
     * @param value finish value
     */
    fun setFinish(index: Int, value: Date) {
        set(selectField(AssignmentFieldLists.CUSTOM_FINISH, index), value)
    }

    /**
     * Retrieve a finish value.
     *
     * @param index finish index (1-10)
     * @return finish value
     */
    fun getFinish(index: Int): Date {
        return getCachedValue(selectField(AssignmentFieldLists.CUSTOM_FINISH, index)) as Date?
    }

    /**
     * Set a date value.
     *
     * @param index date index (1-10)
     * @param value date value
     */
    fun setDate(index: Int, value: Date) {
        set(selectField(AssignmentFieldLists.CUSTOM_DATE, index), value)
    }

    /**
     * Retrieve a date value.
     *
     * @param index date index (1-10)
     * @return date value
     */
    fun getDate(index: Int): Date {
        return getCachedValue(selectField(AssignmentFieldLists.CUSTOM_DATE, index)) as Date?
    }

    /**
     * Set a number value.
     *
     * @param index number index (1-20)
     * @param value number value
     */
    fun setNumber(index: Int, value: Number) {
        set(selectField(AssignmentFieldLists.CUSTOM_NUMBER, index), value)
    }

    /**
     * Retrieve a number value.
     *
     * @param index number index (1-20)
     * @return number value
     */
    fun getNumber(index: Int): Number {
        return getCachedValue(selectField(AssignmentFieldLists.CUSTOM_NUMBER, index))
    }

    /**
     * Set a duration value.
     *
     * @param index duration index (1-10)
     * @param value duration value
     */
    fun setDuration(index: Int, value: Duration) {
        set(selectField(AssignmentFieldLists.CUSTOM_DURATION, index), value)
    }

    /**
     * Retrieve a duration value.
     *
     * @param index duration index (1-10)
     * @return duration value
     */
    fun getDuration(index: Int): Duration {
        return getCachedValue(selectField(AssignmentFieldLists.CUSTOM_DURATION, index)) as Duration?
    }

    /**
     * Set a cost value.
     *
     * @param index cost index (1-10)
     * @param value cost value
     */
    fun setCost(index: Int, value: Number) {
        set(selectField(AssignmentFieldLists.CUSTOM_COST, index), value)
    }

    /**
     * Retrieve a cost value.
     *
     * @param index cost index (1-10)
     * @return cost value
     */
    fun getCost(index: Int): Number {
        return getCachedValue(selectField(AssignmentFieldLists.CUSTOM_COST, index))
    }

    /**
     * Set a flag value.
     *
     * @param index flag index (1-20)
     * @param value flag value
     */
    fun setFlag(index: Int, value: Boolean) {
        set(selectField(AssignmentFieldLists.CUSTOM_FLAG, index), value)
    }

    /**
     * Retrieve a flag value.
     *
     * @param index flag index (1-20)
     * @return flag value
     */
    fun getFlag(index: Int): Boolean {
        return BooleanHelper.getBoolean(getCachedValue(selectField(AssignmentFieldLists.CUSTOM_FLAG, index)) as Boolean?)
    }

    /**
     * Set an enterprise cost value.
     *
     * @param index cost index (1-30)
     * @param value cost value
     */
    fun setEnterpriseCost(index: Int, value: Number) {
        set(selectField(AssignmentFieldLists.ENTERPRISE_COST, index), value)
    }

    /**
     * Retrieve an enterprise cost value.
     *
     * @param index cost index (1-30)
     * @return cost value
     */
    fun getEnterpriseCost(index: Int): Number {
        return getCachedValue(selectField(AssignmentFieldLists.ENTERPRISE_COST, index))
    }

    /**
     * Set an enterprise date value.
     *
     * @param index date index (1-30)
     * @param value date value
     */
    fun setEnterpriseDate(index: Int, value: Date) {
        set(selectField(AssignmentFieldLists.ENTERPRISE_DATE, index), value)
    }

    /**
     * Retrieve an enterprise date value.
     *
     * @param index date index (1-30)
     * @return date value
     */
    fun getEnterpriseDate(index: Int): Date {
        return getCachedValue(selectField(AssignmentFieldLists.ENTERPRISE_DATE, index)) as Date?
    }

    /**
     * Set an enterprise duration value.
     *
     * @param index duration index (1-30)
     * @param value duration value
     */
    fun setEnterpriseDuration(index: Int, value: Duration) {
        set(selectField(AssignmentFieldLists.ENTERPRISE_DURATION, index), value)
    }

    /**
     * Retrieve an enterprise duration value.
     *
     * @param index duration index (1-30)
     * @return duration value
     */
    fun getEnterpriseDuration(index: Int): Duration {
        return getCachedValue(selectField(AssignmentFieldLists.ENTERPRISE_DURATION, index)) as Duration?
    }

    /**
     * Set an enterprise flag value.
     *
     * @param index flag index (1-20)
     * @param value flag value
     */
    fun setEnterpriseFlag(index: Int, value: Boolean) {
        set(selectField(AssignmentFieldLists.ENTERPRISE_FLAG, index), value)
    }

    /**
     * Retrieve an enterprise flag value.
     *
     * @param index flag index (1-20)
     * @return flag value
     */
    fun getEnterpriseFlag(index: Int): Boolean {
        return BooleanHelper.getBoolean(getCachedValue(selectField(AssignmentFieldLists.ENTERPRISE_FLAG, index)) as Boolean?)
    }

    /**
     * Set an enterprise number value.
     *
     * @param index number index (1-40)
     * @param value number value
     */
    fun setEnterpriseNumber(index: Int, value: Number) {
        set(selectField(AssignmentFieldLists.ENTERPRISE_NUMBER, index), value)
    }

    /**
     * Retrieve an enterprise number value.
     *
     * @param index number index (1-40)
     * @return number value
     */
    fun getEnterpriseNumber(index: Int): Number {
        return getCachedValue(selectField(AssignmentFieldLists.ENTERPRISE_NUMBER, index))
    }

    /**
     * Set an enterprise text value.
     *
     * @param index text index (1-40)
     * @param value text value
     */
    fun setEnterpriseText(index: Int, value: String) {
        set(selectField(AssignmentFieldLists.ENTERPRISE_TEXT, index), value)
    }

    /**
     * Retrieve an enterprise text value.
     *
     * @param index text index (1-40)
     * @return text value
     */
    fun getEnterpriseText(index: Int): String {
        return getCachedValue(selectField(AssignmentFieldLists.ENTERPRISE_TEXT, index))
    }

    /**
     * Retrieve an enterprise custom field value.
     *
     * @param index field index
     * @return field value
     */
    fun getEnterpriseCustomField(index: Int): String {
        return getCachedValue(selectField(AssignmentFieldLists.ENTERPRISE_CUSTOM_FIELD, index))
    }

    /**
     * Set an enterprise custom field value.
     *
     * @param index field index
     * @param value field value
     */
    fun setEnterpriseCustomField(index: Int, value: String) {
        set(selectField(AssignmentFieldLists.ENTERPRISE_CUSTOM_FIELD, index), value)
    }

    /**
     * Maps a field index to an AssignmentField instance.
     *
     * @param fields array of fields used as the basis for the mapping.
     * @param index required field index
     * @return AssignmnetField instance
     */
    private fun selectField(fields: Array<AssignmentField>, index: Int): AssignmentField {
        if (index < 1 || index > fields.size) {
            throw IllegalArgumentException("$index is not a valid field index")
        }
        return fields[index - 1]
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return "[Resource Assignment task=" + task!!.name + " resource=" + (if (resource == null) "Unassigned" else resource!!.name) + " start=" + start + " finish=" + finish + " duration=" + work + " workContour=" + workContour + "]"
    }

    /**
     * {@inheritDoc}
     */
    @Override
    operator fun set(field: FieldType?, value: Object) {
        if (field != null) {
            val index = field!!.getValue()
            if (m_eventsEnabled) {
                fireFieldChangeEvent((field as AssignmentField?)!!, m_array[index], value)
            }
            m_array[index] = value
        }
    }

    /**
     * This method inserts a name value pair into internal storage.
     *
     * @param field task field
     * @param value attribute value
     */
    private operator fun set(field: FieldType, value: Boolean) {
        set(field, if (value) Boolean.TRUE else Boolean.FALSE)
    }

    /**
     * Handle the change in a field value. Reset any cached calculated
     * values affected by this change, pass on the event to any external
     * listeners.
     *
     * @param field field changed
     * @param oldValue old field value
     * @param newValue new field value
     */
    private fun fireFieldChangeEvent(field: AssignmentField, oldValue: Object, newValue: Object) {
        //
        // Internal event handling
        //
        when (field) {
            START, BASELINE_START -> {
                m_array[AssignmentField.START_VARIANCE.getValue()] = null
            }

            FINISH, BASELINE_FINISH -> {
                m_array[AssignmentField.FINISH_VARIANCE.getValue()] = null
            }

            BCWP, ACWP -> {
                m_array[AssignmentField.CV.getValue()] = null
                m_array[AssignmentField.SV.getValue()] = null
            }

            COST, BASELINE_COST -> {
                m_array[AssignmentField.COST_VARIANCE.getValue()] = null
            }

            WORK, BASELINE_WORK -> {
                m_array[AssignmentField.WORK_VARIANCE.getValue()] = null
            }

            ACTUAL_OVERTIME_COST, REMAINING_OVERTIME_COST -> {
                m_array[AssignmentField.OVERTIME_COST.getValue()] = null
            }

            else -> {
            }
        }

        //
        // External event handling
        //
        if (m_listeners != null) {
            for (listener in m_listeners!!) {
                listener.fieldChange(this, field, oldValue, newValue)
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun addFieldListener(listener: FieldListener) {
        if (m_listeners == null) {
            m_listeners = LinkedList<FieldListener>()
        }
        m_listeners!!.add(listener)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun removeFieldListener(listener: FieldListener) {
        if (m_listeners != null) {
            m_listeners!!.remove(listener)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun getCachedValue(field: FieldType?): Object? {
        return if (field == null) null else m_array[field!!.getValue()]
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun getCurrentValue(field: FieldType?): Object? {
        var result: Object? = null

        if (field != null) {
            val fieldValue = field!!.getValue()

            result = m_array[fieldValue]
        }

        return result
    }

    /**
     * Disable events firing when fields are updated.
     */
    fun disableEvents() {
        m_eventsEnabled = false
    }

    /**
     * Enable events firing when fields are updated. This is the default state.
     */
    fun enableEvents() {
        m_eventsEnabled = true
    }

    companion object {

        /**
         * Default units value: 100%.
         */
        val DEFAULT_UNITS = Double.valueOf(100)
    }

}
