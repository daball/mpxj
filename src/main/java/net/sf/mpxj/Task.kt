/*
 * file:       Task.java
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

import net.sf.mpxj.common.BooleanHelper
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.TaskFieldLists
import net.sf.mpxj.listener.FieldListener

/**
 * This class represents a task record from an project file.
 */
 class Task/**
 * Default constructor.
 *
 * @param file Parent file to which this record belongs.
 * @param parent Parent task
 */
    internal constructor(file:ProjectFile, /**
 * This is a reference to the parent task, as specified by the
 * outline level.
 */
   private var m_parent:Task?):ProjectEntity(file), Comparable<Task>, ProjectEntityWithID, FieldContainer, ChildTaskContainer {

/**
 * This method retrieves the recurring task record. If the current
 * task is not a recurring task, then this method will return null.
 *
 * @return Recurring task record.
 */
    val recurringTask:RecurringTask?
get() =m_recurringTask

/**
 * This method allows the list of resource assignments for this
 * task to be retrieved.
 *
 * @return list of resource assignments
 */
    val resourceAssignments:List<ResourceAssignment>
get() =m_assignments

/**
 * Read the manual duration attribute.
 *
 * @return manual duration
 */
   /**
 * Set the manual duration attribute.
 *
 * @param dur manual duration
 */
    var manualDuration:Duration
get() =getCachedValue(TaskField.MANUAL_DURATION) as Duration?
set(dur) {
set(TaskField.MANUAL_DURATION, dur)
}

/**
 * The % Complete field contains the current status of a task,
 * expressed as the percentage of the task's duration that has been completed.
 * You can enter percent complete, or you can have Microsoft Project calculate
 * it for you based on actual duration.
 * @return percentage as float
 */
   /**
 * The % Complete field contains the current status of a task, expressed
 * as the percentage of the
 * task's duration that has been completed. You can enter percent complete,
 * or you can have
 * Microsoft Project calculate it for you based on actual duration.
 *
 * @param val value to be set
 */
    var percentageComplete:Number
get() =getCachedValue(TaskField.PERCENT_COMPLETE)
set(`val`) =set(TaskField.PERCENT_COMPLETE, `val`)

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
    var percentageWorkComplete:Number
get() =getCachedValue(TaskField.PERCENT_WORK_COMPLETE)
set(`val`) =set(TaskField.PERCENT_WORK_COMPLETE, `val`)

/**
 * The Actual Cost field shows costs incurred for work already performed
 * by all resources on a task, along with any other recorded costs associated
 * with the task. You can enter all the actual costs or have Microsoft Project
 * calculate them for you.
 *
 * @return currency amount as float
 */
   /**
 * The Actual Cost field shows costs incurred for work already performed
 * by all resources
 * on a task, along with any other recorded costs associated with the task.
 * You can enter
 * all the actual costs or have Microsoft Project calculate them for you.
 *
 * @param val value to be set
 */
    var actualCost:Number
get() =getCachedValue(TaskField.ACTUAL_COST)
set(`val`) =set(TaskField.ACTUAL_COST, `val`)

/**
 * The Actual Duration field shows the span of actual working time for a
 * task so far, based on the scheduled duration and current remaining work
 * or completion percentage.
 *
 * @return duration string
 */
   /**
 * The Actual Duration field shows the span of actual working time for a
 * task so far,
 * based on the scheduled duration and current remaining work or
 * completion percentage.
 *
 * @param val value to be set
 */
    var actualDuration:Duration
get() =getCachedValue(TaskField.ACTUAL_DURATION) as Duration?
set(`val`) {
set(TaskField.ACTUAL_DURATION, `val`)
}

/**
 * The Actual Finish field shows the date and time that a task actually
 * finished. Microsoft Project sets the Actual Finish field to the scheduled
 * finish date if the completion percentage is 100. This field contains "NA"
 * until you enter actual information or set the completion percentage to 100.
 * If "NA" is entered as value, arbitrary year zero Date is used. Date(0);
 *
 * @return Date
 */
   /**
 * The Actual Finish field shows the date and time that a task actually
 * finished.
 * Microsoft Project sets the Actual Finish field to the scheduled finish
 * date if
 * the completion percentage is 100. This field contains "NA" until you
 * enter actual
 * information or set the completion percentage to 100.
 *
 * @param val value to be set
 */
    var actualFinish:Date
get() =getCachedValue(TaskField.ACTUAL_FINISH) as Date?
set(`val`) {
set(TaskField.ACTUAL_FINISH, `val`)
}

/**
 * The Actual Start field shows the date and time that a task actually began.
 * When a task is first created, the Actual Start field contains "NA." Once
 * you enter the first actual work or a completion percentage for a task,
 * Microsoft Project sets the actual start date to the scheduled start date.
 * If "NA" is entered as value, arbitrary year zero Date is used. Date(0);
 *
 * @return Date
 */
   /**
 * The Actual Start field shows the date and time that a task actually began.
 * When a task is first created, the Actual Start field contains "NA." Once you
 * enter the first actual work or a completion percentage for a task, Microsoft
 * Project sets the actual start date to the scheduled start date.
 * @param val value to be set
 */
    var actualStart:Date?
get() =getCachedValue(TaskField.ACTUAL_START) as Date?
set(`val`) {
set(TaskField.ACTUAL_START, `val`)
}

/**
 * The Actual Work field shows the amount of work that has already been done
 * by the resources assigned to a task.
 *
 * @return duration string
 */
   /**
 * The Actual Work field shows the amount of work that has already been
 * done by the
 * resources assigned to a task.
 * @param val value to be set
 */
    var actualWork:Duration
get() =getCachedValue(TaskField.ACTUAL_WORK) as Duration?
set(`val`) {
set(TaskField.ACTUAL_WORK, `val`)
}

/**
 * The Baseline Cost field shows the total planned cost for a task.
 * Baseline cost is also referred to as budget at completion (BAC).
 * @return currency amount as float
 */
   /**
 * The Baseline Cost field shows the total planned cost for a task.
 * Baseline cost is also referred to as budget at completion (BAC).
 *
 * @param val the amount to be set
 */
    var baselineCost:Number?
get() =getCachedValue(TaskField.BASELINE_COST)
set(`val`) =set(TaskField.BASELINE_COST, `val`)

/**
 * The Baseline Duration field shows the original span of time planned
 * to complete a task.
 *
 * @return  - duration string
 */
   /**
 * The Baseline Duration field shows the original span of time planned to
 * complete a task.
 *
 * @param val duration
 */
    var baselineDuration:Duration?
get() {
var result = getCachedValue(TaskField.BASELINE_DURATION)
if (result == null)
{
result = getCachedValue(TaskField.BASELINE_ESTIMATED_DURATION)
}

if (result !is Duration)
{
result = null
}
return result as Duration?
}
set(`val`) {
set(TaskField.BASELINE_DURATION, `val`)
}

/**
 * Retrieves the text value for the baseline duration.
 *
 * @return baseline duration text
 */
   /**
 * Sets the baseline duration text value.
 *
 * @param value baseline duration text
 */
    var baselineDurationText:String
get() {
var result = getCachedValue(TaskField.BASELINE_DURATION)
if (result == null)
{
result = getCachedValue(TaskField.BASELINE_ESTIMATED_DURATION)
}

if (result !is String)
{
result = null
}
return result
}
set(value) =set(TaskField.BASELINE_DURATION, value)

/**
 * The Baseline Finish field shows the planned completion date for a task
 * at the time you saved a baseline. Information in this field becomes
 * available when you set a baseline for a task.
 *
 * @return Date
 */
   /**
 * The Baseline Finish field shows the planned completion date for a
 * task at the time
 * you saved a baseline. Information in this field becomes available
 * when you set a
 * baseline for a task.
 *
 * @param val Date to be set
 */
    var baselineFinish:Date
get() {
var result = getCachedValue(TaskField.BASELINE_FINISH)
if (result == null)
{
result = getCachedValue(TaskField.BASELINE_ESTIMATED_FINISH)
}

if (result !is Date)
{
result = null
}
return result as Date?
}
set(`val`) {
set(TaskField.BASELINE_FINISH, `val`)
}

/**
 * Retrieves the baseline finish text value.
 *
 * @return baseline finish text
 */
   /**
 * Sets the baseline finish text value.
 *
 * @param value baseline finish text
 */
    var baselineFinishText:String
get() {
var result = getCachedValue(TaskField.BASELINE_FINISH)
if (result == null)
{
result = getCachedValue(TaskField.BASELINE_ESTIMATED_FINISH)
}

if (result !is String)
{
result = null
}
return result
}
set(value) =set(TaskField.BASELINE_FINISH, value)

/**
 * The Baseline Start field shows the planned beginning date for a task at
 * the time you saved a baseline. Information in this field becomes available
 * when you set a baseline.
 *
 * @return Date
 */
   /**
 * The Baseline Start field shows the planned beginning date for a task at
 * the time
 * you saved a baseline. Information in this field becomes available when you
 * set a baseline.
 *
 * @param val Date to be set
 */
    var baselineStart:Date
get() {
var result = getCachedValue(TaskField.BASELINE_START)
if (result == null)
{
result = getCachedValue(TaskField.BASELINE_ESTIMATED_START)
}

if (result !is Date)
{
result = null
}
return result as Date?
}
set(`val`) {
set(TaskField.BASELINE_START, `val`)
}

/**
 * Retrieves the baseline start text value.
 *
 * @return baseline start value
 */
   /**
 * Sets the baseline start text value.
 *
 * @param value baseline start text
 */
    var baselineStartText:String
get() {
var result = getCachedValue(TaskField.BASELINE_START)
if (result == null)
{
result = getCachedValue(TaskField.BASELINE_ESTIMATED_START)
}

if (result !is String)
{
result = null
}
return result
}
set(value) =set(TaskField.BASELINE_START, value)

/**
 * The Baseline Work field shows the originally planned amount of work to be
 * performed by all resources assigned to a task. This field shows the planned
 * person-hours scheduled for a task. Information in the Baseline Work field
 * becomes available when you set a baseline for the project.
 *
 * @return Duration
 */
   /**
 * The Baseline Work field shows the originally planned amount of work to
 * be performed
 * by all resources assigned to a task. This field shows the planned
 * person-hours
 * scheduled for a task. Information in the Baseline Work field
 * becomes available
 * when you set a baseline for the project.
 *
 * @param val the duration to be set.
 */
    var baselineWork:Duration?
get() =getCachedValue(TaskField.BASELINE_WORK) as Duration?
set(`val`) {
set(TaskField.BASELINE_WORK, `val`)
}

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
    var bcwp:Number?
get() =getCachedValue(TaskField.BCWP)
set(`val`) =set(TaskField.BCWP, `val`)

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
    var bcws:Number?
get() =getCachedValue(TaskField.BCWS)
set(`val`) =set(TaskField.BCWS, `val`)

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
    var confirmed:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.CONFIRMED) as Boolean?)
set(`val`) {
set(TaskField.CONFIRMED, `val`)
}

/**
 * The Constraint Date field shows the specific date associated with certain
 * constraint types, such as Must Start On, Must Finish On,
 * Start No Earlier Than,
 * Start No Later Than, Finish No Earlier Than, and Finish No Later Than.
 *
 * @return Date
 */
   /**
 * The Constraint Date field shows the specific date associated with certain
 * constraint types,
 * such as Must Start On, Must Finish On, Start No Earlier Than,
 * Start No Later Than,
 * Finish No Earlier Than, and Finish No Later Than.
 * SEE class constants
 *
 * @param val Date to be set
 */
    var constraintDate:Date
get() =getCachedValue(TaskField.CONSTRAINT_DATE) as Date?
set(`val`) {
set(TaskField.CONSTRAINT_DATE, `val`)
}

/**
 * The Constraint Type field provides choices for the type of constraint you
 * can apply for scheduling a task.
 *
 * @return constraint type
 */
   /**
 * Private method for dealing with string parameters from File.
 *
 * @param type string constraint type
 */
    var constraintType:ConstraintType
get() =getCachedValue(TaskField.CONSTRAINT_TYPE) as ConstraintType?
set(type) {
set(TaskField.CONSTRAINT_TYPE, type)
}

/**
 * The Contact field contains the name of an individual
 * responsible for a task.
 *
 * @return String
 */
   /**
 * The Contact field contains the name of an individual
 * responsible for a task.
 *
 * @param val value to be set
 */
    var contact:String
get() =getCachedValue(TaskField.CONTACT)
set(`val`) =set(TaskField.CONTACT, `val`)

/**
 * The Cost field shows the total scheduled, or projected, cost for a task,
 * based on costs already incurred for work performed by all resources assigned
 * to the task, in addition to the costs planned for the remaining work for the
 * assignment. This can also be referred to as estimate at completion (EAC).
 *
 * @return cost amount
 */
   /**
 * The Cost field shows the total scheduled, or projected, cost for a task,
 * based on costs already incurred for work performed by all resources assigned
 * to the task, in addition to the costs planned for the remaining work for the
 * assignment. This can also be referred to as estimate at completion (EAC).
 *
 * @param val amount
 */
    var cost:Number?
get() =getCachedValue(TaskField.COST)
set(`val`) =set(TaskField.COST, `val`)

/**
 * The Cost Variance field shows the difference between the baseline cost
 * and total cost for a task. The total cost is the current estimate of costs
 * based on actual costs and remaining costs.
 *
 * @return amount
 */
   /**
 * The Cost Variance field shows the difference between the
 * baseline cost and total cost for a task. The total cost is the
 * current estimate of costs based on actual costs and remaining costs.
 *
 * @param val amount
 */
    var costVariance:Number?
get() {
var variance = getCachedValue(TaskField.COST_VARIANCE) as Number?
if (variance == null)
{
val cost = cost
val baselineCost = baselineCost
if (cost != null && baselineCost != null)
{
variance = NumberHelper.getDouble(cost.doubleValue() - baselineCost.doubleValue())
set(TaskField.COST_VARIANCE, variance)
}
}
return variance
}
set(`val`) =set(TaskField.COST_VARIANCE, `val`)

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
    var createDate:Date
get() =getCachedValue(TaskField.CREATED) as Date?
set(`val`) {
set(TaskField.CREATED, `val`)
}

/**
 * The Critical field indicates whether a task has any room in the schedule
 * to slip, or if a task is on the critical path. The Critical field contains
 * Yes if the task is critical and No if the task is not critical.
 *
 * @return boolean
 */
   /**
 * The Critical field indicates whether a task has any room in the
 * schedule to slip,
 * or if a task is on the critical path. The Critical field contains
 * Yes if the task
 * is critical and No if the task is not critical.
 *
 * @param val whether task is critical or not
 */
    var critical:Boolean
get() {
var critical = getCachedValue(TaskField.CRITICAL) as Boolean?
if (critical == null)
{
var totalSlack = totalSlack
val props = parentFile.projectProperties
val criticalSlackLimit = NumberHelper.getInt(props.criticalSlackLimit)
if (criticalSlackLimit != 0 && totalSlack.getDuration() !== 0 && totalSlack.getUnits() !== TimeUnit.DAYS)
{
totalSlack = totalSlack.convertUnits(TimeUnit.DAYS, props)
}
critical = Boolean.valueOf(totalSlack.getDuration() <= criticalSlackLimit && NumberHelper.getInt(percentageComplete) != 100 && (taskMode === TaskMode.AUTO_SCHEDULED || durationText == null && startText == null && finishText == null))
set(TaskField.CRITICAL, critical)
}
return BooleanHelper.getBoolean(critical)
}
set(`val`) {
set(TaskField.CRITICAL, `val`)
}

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
    var cv:Number
get() {
var variance = getCachedValue(TaskField.CV) as Number?
if (variance == null)
{
variance = Double.valueOf(NumberHelper.getDouble(bcwp) - NumberHelper.getDouble(acwp))
set(TaskField.CV, variance)
}
return variance
}
set(`val`) =set(TaskField.CV, `val`)

/**
 * Delay , in MPX files as eg '0ed'. Use duration
 *
 * @return Duration
 */
   /**
 * Set amount of delay as elapsed real time.
 *
 * @param val elapsed time
 */
    var levelingDelay:Duration
get() =getCachedValue(TaskField.LEVELING_DELAY) as Duration?
set(`val`) {
set(TaskField.LEVELING_DELAY, `val`)
}

/**
 * The Duration field is the total span of active working time for a task.
 * This is generally the amount of time from the start to the finish of a task.
 * The default for new tasks is 1 day (1d).
 *
 * @return Duration
 */
   /**
 * The Duration field is the total span of active working time for a task.
 * This is generally the amount of time from the start to the finish of a task.
 * The default for new tasks is 1 day (1d).
 *
 * @param val duration
 */
    var duration:Duration?
get() =getCachedValue(TaskField.DURATION) as Duration?
set(`val`) {
set(TaskField.DURATION, `val`)
}

/**
 * Retrieves the duration text of a manually scheduled task.
 *
 * @return duration text
 */
   /**
 * Set the duration text used for a manually scheduled task.
 *
 * @param val text
 */
    var durationText:String?
get() =getCachedValue(TaskField.DURATION_TEXT)
set(`val`) =set(TaskField.DURATION_TEXT, `val`)

/**
 * The Duration Variance field contains the difference between the
 * baseline duration of a task and the total duration (current estimate)
 * of a task.
 *
 * @return Duration
 */
   /**
 * The Duration Variance field contains the difference between the
 * baseline duration of a task and the forecast or actual duration
 * of the task.
 *
 * @param duration duration value
 */
    var durationVariance:Duration?
get() {
var variance = getCachedValue(TaskField.DURATION_VARIANCE) as Duration?
if (variance == null)
{
val duration = duration
val baselineDuration = baselineDuration

if (duration != null && baselineDuration != null)
{
variance = Duration.getInstance(duration!!.getDuration() - baselineDuration!!.convertUnits(duration!!.getUnits(), parentFile.projectProperties).getDuration(), duration!!.getUnits())
set(TaskField.DURATION_VARIANCE, variance)
}
}
return variance
}
set(duration) {
set(TaskField.DURATION_VARIANCE, duration)
}

/**
 * The Early Finish field contains the earliest date that a task could
 * possibly finish, based on early finish dates of predecessor and
 * successor tasks, other constraints, and any leveling delay.
 *
 * @return Date
 */
   /**
 * The Early Finish field contains the earliest date that a task
 * could possibly finish, based on early finish dates of predecessor
 * and successor tasks, other constraints, and any leveling delay.
 *
 * @param date Date value
 */
    var earlyFinish:Date
get() =getCachedValue(TaskField.EARLY_FINISH) as Date?
set(date) {
set(TaskField.EARLY_FINISH, date)
}

/**
 * The date the resource is scheduled to finish the remaining work for the activity.
 *
 * @return Date
 */
   /**
 * The date the resource is scheduled to finish the remaining work for the activity.
 *
 * @param date Date value
 */
    var remainingEarlyFinish:Date
get() =getCachedValue(TaskField.REMAINING_EARLY_FINISH) as Date?
set(date) {
set(TaskField.REMAINING_EARLY_FINISH, date)
}

/**
 * The Early Start field contains the earliest date that a task could
 * possibly begin, based on the early start dates of predecessor and
 * successor tasks, and other constraints.
 *
 * @return Date
 */
   /**
 * The Early Start field contains the earliest date that a task could
 * possibly begin, based on the early start dates of predecessor and
 * successor tasks, and other constraints.
 *
 * @param date Date value
 */
    var earlyStart:Date
get() =getCachedValue(TaskField.EARLY_START) as Date?
set(date) {
set(TaskField.EARLY_START, date)
}

/**
 * The date the resource is scheduled to start the remaining work for the activity.
 *
 * @return Date
 */
   /**
 * The date the resource is scheduled to begin the remaining work for the activity.
 *
 * @param date Date value
 */
    var remainingEarlyStart:Date
get() =getCachedValue(TaskField.REMAINING_EARLY_START) as Date?
set(date) {
set(TaskField.REMAINING_EARLY_START, date)
}

/**
 * The Finish field shows the date and time that a task is scheduled to
 * be completed. You can enter the finish date you want, to indicate the
 * date when the task should be completed. Or, you can have Microsoft
 * Project calculate the finish date.
 *
 * @return Date
 */
   /**
 * The Finish field shows the date and time that a task is scheduled to be
 * completed. MS project allows a finish date to be entered, and will
 * calculate the duration, or a duration can be supplied and MS Project
 * will calculate the finish date.
 *
 * @param date Date value
 */
    var finish:Date
get() =getCachedValue(TaskField.FINISH) as Date?
set(date) {
set(TaskField.FINISH, date)
}

/**
 * Retrieves the finish text of a manually scheduled task.
 *
 * @return finish text
 */
   /**
 * Set the finish text used for a manually scheduled task.
 *
 * @param val text
 */
    var finishText:String?
get() =getCachedValue(TaskField.FINISH_TEXT)
set(`val`) =set(TaskField.FINISH_TEXT, `val`)

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
    var finishVariance:Duration
get() {
var variance = getCachedValue(TaskField.FINISH_VARIANCE) as Duration?
if (variance == null)
{
val format = parentFile.projectProperties.defaultDurationUnits
variance = DateHelper.getVariance(this, baselineFinish, finish, format)
set(TaskField.FINISH_VARIANCE, variance)
}
return variance
}
set(duration) {
set(TaskField.FINISH_VARIANCE, duration)
}

/**
 * The Fixed Cost field shows any task expense that is not associated
 * with a resource cost.
 *
 * @return currency amount
 */
   /**
 * The Fixed Cost field shows any task expense that is not associated
 * with a resource cost.
 *
 * @param val amount
 */
    var fixedCost:Number
get() =getCachedValue(TaskField.FIXED_COST)
set(`val`) =set(TaskField.FIXED_COST, `val`)

/**
 * The Free Slack field contains the amount of time that a task can be
 * delayed without delaying any successor tasks. If the task has no
 * successors, free slack is the amount of time that a task can be
 * delayed without delaying the entire project's finish date.
 *
 * @return Duration
 */
   /**
 * The Free Slack field contains the amount of time that a task can be
 * delayed without delaying any successor tasks. If the task has no
 * successors, free slack is the amount of time that a task can be delayed
 * without delaying the entire project's finish date.
 *
 * @param duration duration value
 */
    var freeSlack:Duration
get() =getCachedValue(TaskField.FREE_SLACK) as Duration?
set(duration) {
set(TaskField.FREE_SLACK, duration)
}

/**
 * The Hide Bar field indicates whether the Gantt bars and Calendar bars
 * for a task are hidden. Click Yes in the Hide Bar field to hide the
 * bar for the task. Click No in the Hide Bar field to show the bar
 * for the task.
 *
 * @return boolean
 */
   /**
 * The Hide Bar flag indicates whether the Gantt bars and Calendar bars
 * for a task are hidden when this project's data is displayed in MS Project.
 *
 * @param flag boolean value
 */
    var hideBar:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.HIDE_BAR) as Boolean?)
set(flag) {
set(TaskField.HIDE_BAR, flag)
}

/**
 * The ID field contains the identifier number that Microsoft Project
 * automatically assigns to each task as you add it to the project.
 * The ID indicates the position of a task with respect to the other tasks.
 *
 * @return the task ID
 */
   /**
 * The ID field contains the identifier number that Microsoft Project
 * automatically assigns to each task as you add it to the project.
 * The ID indicates the position of a task with respect to the other tasks.
 *
 * @param val ID
 */
   override var id:Integer?
@Override get() =getCachedValue(TaskField.ID) as Integer?
@Override set(`val`) {
val parent = parentFile
val previous = id

if (previous != null)
{
parent.tasks.unmapID(previous)
}

parent.tasks.mapID(`val`, this)

set(TaskField.ID, `val`)
}

/**
 * The Late Finish field contains the latest date that a task can finish
 * without delaying the finish of the project. This date is based on the
 * task's late start date, as well as the late start and late finish
 * dates of predecessor and successor
 * tasks, and other constraints.
 *
 * @return Date
 */
   /**
 * The Late Finish field contains the latest date that a task can finish
 * without delaying the finish of the project. This date is based on the
 * task's late start date, as well as the late start and late finish dates
 * of predecessor and successor tasks, and other constraints.
 *
 * @param date date value
 */
    var lateFinish:Date
get() =getCachedValue(TaskField.LATE_FINISH) as Date?
set(date) {
set(TaskField.LATE_FINISH, date)
}

/**
 * The Late Start field contains the latest date that a task can start
 * without delaying the finish of the project. This date is based on
 * the task's start date, as well as the late start and late finish
 * dates of predecessor and successor tasks, and other constraints.
 *
 * @return Date
 */
   /**
 * The Late Start field contains the latest date that a task can start
 * without delaying the finish of the project. This date is based on the
 * task's start date, as well as the late start and late finish dates of
 * predecessor and successor tasks, and other constraints.
 *
 * @param date date value
 */
    var lateStart:Date
get() =getCachedValue(TaskField.LATE_START) as Date?
set(date) {
set(TaskField.LATE_START, date)
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
    var linkedFields:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.LINKED_FIELDS) as Boolean?)
set(flag) {
set(TaskField.LINKED_FIELDS, flag)
}

/**
 * The Marked field indicates whether a task is marked for further action or
 * identification of some kind. To mark a task, click Yes in the Marked field.
 * If you don't want a task marked, click No.
 *
 * @return true for marked
 */
   /**
 * This is a user defined field used to mark a task for some form of
 * additional action.
 *
 * @param flag boolean value
 */
    var marked:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.MARKED) as Boolean?)
set(flag) {
set(TaskField.MARKED, flag)
}

/**
 * The Milestone field indicates whether a task is a milestone.
 *
 * @return boolean
 */
   /**
 * The Milestone field indicates whether a task is a milestone.
 *
 * @param flag boolean value
 */
    var milestone:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.MILESTONE) as Boolean?)
set(flag) {
set(TaskField.MILESTONE, flag)
}

/**
 * Retrieves the task name.
 *
 * @return task name
 */
   /**
 * The Name field contains the name of a task.
 *
 * @param name task name
 */
    var name:String
get() =getCachedValue(TaskField.NAME)
set(name) =set(TaskField.NAME, name)

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
    var notes:String
get() {
val notes = getCachedValue(TaskField.NOTES) as String?
return notes ?: ""
}
set(notes) =set(TaskField.NOTES, notes)

/**
 * The Objects field contains the number of objects attached to a task.
 * Microsoft Project counts the number of objects linked or embedded to a task.
 * However, objects in the Notes box in the Resource Form are not included
 * in this count.
 *
 * @return int
 */
   /**
 * The Objects field contains the number of objects attached to a task.
 *
 * @param val - integer value
 */
    var objects:Integer
get() =getCachedValue(TaskField.OBJECTS) as Integer?
set(`val`) {
set(TaskField.OBJECTS, `val`)
}

/**
 * The Outline Level field contains the number that indicates the level
 * of the task in the project outline hierarchy.
 *
 * @return int
 */
   /**
 * The Outline Level field contains the number that indicates the level of
 * the task in the project outline hierarchy.
 *
 * @param val - int
 */
    var outlineLevel:Integer
get() =getCachedValue(TaskField.OUTLINE_LEVEL) as Integer?
set(`val`) {
set(TaskField.OUTLINE_LEVEL, `val`)
}

/**
 * The Outline Number field contains the number of the task in the structure
 * of an outline. This number indicates the task's position within the
 * hierarchical structure of the project outline. The outline number is
 * similar to a WBS (work breakdown structure) number,
 * except that the outline number is automatically entered by
 * Microsoft Project.
 *
 * @return String
 */
   /**
 * The Outline Number field contains the number of the task in the structure
 * of an outline. This number indicates the task's position within the
 * hierarchical structure of the project outline. The outline number is
 * similar to a WBS (work breakdown structure) number, except that the
 * outline number is automatically entered by Microsoft Project.
 *
 * @param val - text
 */
    var outlineNumber:String
get() =getCachedValue(TaskField.OUTLINE_NUMBER)
set(`val`) =set(TaskField.OUTLINE_NUMBER, `val`)

/**
 * Retrieves the list of predecessors for this task.
 *
 * @return list of predecessor Relation instances
 */
    val predecessors:List<Relation>
@SuppressWarnings("unchecked") get() =getCachedValue(TaskField.PREDECESSORS)

/**
 * Retrieves the list of successors for this task.
 *
 * @return list of successor Relation instances
 */
    val successors:List<Relation>
@SuppressWarnings("unchecked") get() =getCachedValue(TaskField.SUCCESSORS)

/**
 * The Priority field provides choices for the level of importance
 * assigned to a task, which in turn indicates how readily a task can be
 * delayed or split during resource leveling.
 * The default priority is Medium. Those tasks with a priority
 * of Do Not Level are never delayed or split when Microsoft Project levels
 * tasks that have overallocated resources assigned.
 *
 * @return priority class instance
 */
   /**
 * The Priority field provides choices for the level of importance
 * assigned to a task, which in turn indicates how readily a task can be
 * delayed or split during resource leveling.
 * The default priority is Medium. Those tasks with a priority
 * of Do Not Level are never delayed or split when Microsoft Project levels
 * tasks that have overallocated resources assigned.
 *
 * @param priority the priority value
 */
    var priority:Priority?
get() =getCachedValue(TaskField.PRIORITY)
set(priority) =set(TaskField.PRIORITY, priority)

/**
 * The Project field shows the name of the project from which a task
 * originated.
 * This can be the name of the active project file. If there are other
 * projects inserted
 * into the active project file, the name of the inserted project appears
 * in this field
 * for the task.
 *
 * @return name of originating project
 */
   /**
 * The Project field shows the name of the project from which a
 * task originated.
 * This can be the name of the active project file. If there are
 * other projects
 * inserted into the active project file, the name of the
 * inserted project appears
 * in this field for the task.
 *
 * @param val - text
 */
    var project:String
get() =getCachedValue(TaskField.PROJECT)
set(`val`) =set(TaskField.PROJECT, `val`)

/**
 * The Remaining Cost field shows the remaining scheduled expense of a
 * task that will be incurred in completing the remaining scheduled work
 * by all resources assigned to the task.
 *
 * @return remaining cost
 */
   /**
 * The Remaining Cost field shows the remaining scheduled expense of a task that
 * will be incurred in completing the remaining scheduled work by all resources
 * assigned to the task.
 *
 * @param val - currency amount
 */
    var remainingCost:Number
get() =getCachedValue(TaskField.REMAINING_COST)
set(`val`) =set(TaskField.REMAINING_COST, `val`)

/**
 * The Remaining Duration field shows the amount of time required
 * to complete the unfinished portion of a task.
 *
 * @return Duration
 */
   /**
 * The Remaining Duration field shows the amount of time required to complete
 * the unfinished portion of a task.
 *
 * @param val - duration.
 */
    var remainingDuration:Duration
get() =getCachedValue(TaskField.REMAINING_DURATION) as Duration?
set(`val`) {
set(TaskField.REMAINING_DURATION, `val`)
}

/**
 * The Remaining Work field shows the amount of time, or person-hours,
 * still required by all assigned resources to complete a task.
 *
 * @return the amount of time still required to complete a task
 */
   /**
 * The Remaining Work field shows the amount of time, or person-hours,
 * still required by all assigned resources to complete a task.
 * @param val  - duration
 */
    var remainingWork:Duration
get() =getCachedValue(TaskField.REMAINING_WORK) as Duration?
set(`val`) {
set(TaskField.REMAINING_WORK, `val`)
}

/**
 * The Resource Group field contains the list of resource groups to which
 * the resources assigned to a task belong.
 *
 * @return single string list of groups
 */
   /**
 * The Resource Group field contains the list of resource groups to which the
 * resources assigned to a task belong.
 *
 * @param val - String list
 */
    var resourceGroup:String
get() =getCachedValue(TaskField.RESOURCE_GROUP)
set(`val`) =set(TaskField.RESOURCE_GROUP, `val`)

/**
 * The Resource Initials field lists the abbreviations for the names of
 * resources assigned to a task. These initials can serve as substitutes
 * for the names.
 *
 * Note that MS Project 98 does not export values for this field when
 * writing an MPX file, and the field is not currently populated by MPXJ
 * when reading an MPP file.
 *
 * @return String containing a comma separated list of initials
 */
   /**
 * The Resource Initials field lists the abbreviations for the names of
 * resources assigned to a task. These initials can serve as substitutes
 * for the names.
 *
 * Note that MS Project 98 does no normally populate this field when
 * it generates an MPX file, and will therefore not expect to see values
 * in this field when it reads an MPX file. Supplying values for this
 * field will cause MS Project 98, 2000, and 2002 to create new resources
 * and ignore any other resource assignments that have been defined
 * in the MPX file.
 *
 * @param val String containing a comma separated list of initials
 */
    var resourceInitials:String
get() =getCachedValue(TaskField.RESOURCE_INITIALS)
set(`val`) =set(TaskField.RESOURCE_INITIALS, `val`)

/**
 * The Resource Names field lists the names of all resources assigned
 * to a task.
 *
 * Note that MS Project 98 does not export values for this field when
 * writing an MPX file, and the field is not currently populated by MPXJ
 * when reading an MPP file.
 *
 * @return String containing a comma separated list of names
 */
   /**
 * The Resource Names field lists the names of all resources
 * assigned to a task.
 *
 * Note that MS Project 98 does not normally populate this field when
 * it generates an MPX file, and will therefore not expect to see values
 * in this field when it reads an MPX file. Supplying values for this
 * field when writing an MPX file will cause MS Project 98, 2000, and 2002
 * to create new resources and ignore any other resource assignments
 * that have been defined in the MPX file.
 *
 * @param val String containing a comma separated list of names
 */
    var resourceNames:String
get() =getCachedValue(TaskField.RESOURCE_NAMES)
set(`val`) =set(TaskField.RESOURCE_NAMES, `val`)

/**
 * The Resume field shows the date that the remaining portion of a task
 * is scheduled to resume after you enter a new value for the % Complete
 * field. The Resume field is also recalculated when the remaining portion
 * of a task is moved to a new date.
 *
 * @return Date
 */
   /**
 * The Resume field shows the date that the remaining portion of a task is
 * scheduled to resume after you enter a new value for the % Complete field.
 * The Resume field is also recalculated when the remaining portion of a task
 * is moved to a new date.
 *
 * @param val - Date
 */
    var resume:Date
get() =getCachedValue(TaskField.RESUME) as Date?
set(`val`) {
set(TaskField.RESUME, `val`)
}

/**
 * For subtasks, the Rollup field indicates whether information on the
 * subtask Gantt bars
 * will be rolled up to the summary task bar. For summary tasks, the
 * Rollup field indicates
 * whether the summary task bar displays rolled up bars. You must
 * have the Rollup field for
 * summary tasks set to Yes for any subtasks to roll up to them.
 *
 * @return boolean
 */
   /**
 * For subtasks, the Rollup field indicates whether information on the subtask
 * Gantt bars will be rolled up to the summary task bar. For summary tasks, the
 * Rollup field indicates whether the summary task bar displays rolled up bars.
 * You must have the Rollup field for summary tasks set to Yes for any subtasks
 * to roll up to them.
 *
 * @param val - boolean
 */
    var rollup:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.ROLLUP) as Boolean?)
set(`val`) {
set(TaskField.ROLLUP, `val`)
}

/**
 * The Start field shows the date and time that a task is scheduled to begin.
 * You can enter the start date you want, to indicate the date when the task
 * should begin. Or, you can have Microsoft Project calculate the start date.
 *
 * @return Date
 */
   /**
 * The Start field shows the date and time that a task is scheduled to begin.
 * You can enter the start date you want, to indicate the date when the task
 * should begin. Or, you can have Microsoft Project calculate the start date.
 * @param val - Date
 */
    var start:Date
get() =getCachedValue(TaskField.START) as Date?
set(`val`) {
set(TaskField.START, `val`)
}

/**
 * Retrieve the start text for a manually scheduled task.
 *
 * @return start text
 */
   /**
 * Set the start text used for a manually scheduled task.
 *
 * @param val text
 */
    var startText:String?
get() =getCachedValue(TaskField.START_TEXT)
set(`val`) =set(TaskField.START_TEXT, `val`)

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
    var startVariance:Duration
get() {
var variance = getCachedValue(TaskField.START_VARIANCE) as Duration?
if (variance == null)
{
val format = parentFile.projectProperties.defaultDurationUnits
variance = DateHelper.getVariance(this, baselineStart, start, format)
set(TaskField.START_VARIANCE, variance)
}
return variance
}
set(`val`) {
set(TaskField.START_VARIANCE, `val`)
}

/**
 * The Stop field shows the date that represents the end of the actual
 * portion of a task. Typically, Microsoft Project calculates the stop date.
 * However, you can edit this date as well.
 *
 * @return Date
 */
   /**
 * The Stop field shows the date that represents the end of the actual
 * portion of a task. Typically, Microsoft Project calculates the stop date.
 * However, you can edit this date as well.
 *
 * @param val - Date
 */
    var stop:Date
get() =getCachedValue(TaskField.STOP) as Date?
set(`val`) {
set(TaskField.STOP, `val`)
}

/**
 * Contains the file name and path of the sub project represented by
 * the current task.
 *
 * @return sub project file path
 */
   /**
 * The Subproject File field contains the name of a project inserted into
 * the active project file. The Subproject File field contains the inserted
 * project's path and file name.
 *
 * @param val - String
 */
    var subprojectName:String
get() =getCachedValue(TaskField.SUBPROJECT_FILE)
set(`val`) =set(TaskField.SUBPROJECT_FILE, `val`)

/**
 * The Summary field indicates whether a task is a summary task.
 *
 * @return boolean, true-is summary task
 */
   /**
 * The Summary field indicates whether a task is a summary task.
 *
 * @param val - boolean
 */
    var summary:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.SUMMARY) as Boolean?)
set(`val`) {
set(TaskField.SUMMARY, `val`)
}

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
    var sv:Number?
get() {
var variance = getCachedValue(TaskField.SV) as Number?
if (variance == null)
{
val bcwp = bcwp
val bcws = bcws
if (bcwp != null && bcws != null)
{
variance = NumberHelper.getDouble(bcwp.doubleValue() - bcws.doubleValue())
set(TaskField.SV, variance)
}
}
return variance
}
set(`val`) =set(TaskField.SV, `val`)

/**
 * The Total Slack field contains the amount of time a task can be
 * delayed without delaying the project's finish date.
 *
 * @return string representing duration
 */
   /**
 * The Total Slack field contains the amount of time a task can be delayed
 * without delaying the project's finish date.
 *
 * @param val - duration
 */
    var totalSlack:Duration
get() {
var totalSlack = getCachedValue(TaskField.TOTAL_SLACK) as Duration?
if (totalSlack == null)
{
var duration = duration
if (duration == null)
{
duration = Duration.getInstance(0, TimeUnit.DAYS)
}

val units = duration!!.getUnits()

var startSlack = startSlack
if (startSlack == null)
{
startSlack = Duration.getInstance(0, units)
}
else
{
if (startSlack!!.getUnits() !== units)
{
startSlack = startSlack!!.convertUnits(units, parentFile.projectProperties)
}
}

var finishSlack = finishSlack
if (finishSlack == null)
{
finishSlack = Duration.getInstance(0, units)
}
else
{
if (finishSlack!!.getUnits() !== units)
{
finishSlack = finishSlack!!.convertUnits(units, parentFile.projectProperties)
}
}

val startSlackDuration = startSlack!!.getDuration()
val finishSlackDuration = finishSlack!!.getDuration()

if (startSlackDuration == 0.0 || finishSlackDuration == 0.0)
{
if (startSlackDuration != 0.0)
{
totalSlack = startSlack
}
else
{
totalSlack = finishSlack
}
}
else
{
if (startSlackDuration < finishSlackDuration)
{
totalSlack = startSlack
}
else
{
totalSlack = finishSlack
}
}

set(TaskField.TOTAL_SLACK, totalSlack)
}

return totalSlack
}
set(`val`) {
set(TaskField.TOTAL_SLACK, `val`)
}

/**
 * The Unique ID field contains the number that Microsoft Project
 * automatically designates whenever a new task is created. This number
 * indicates the sequence in which the task was
 * created, regardless of placement in the schedule.
 *
 * @return String
 */
   /**
 * The Unique ID field contains the number that Microsoft Project
 * automatically designates whenever a new task is created.
 * This number indicates the sequence in which the task was created,
 * regardless of placement in the schedule.
 *
 * @param val unique ID
 */
   override var uniqueID:Integer
@Override get() =getCachedValue(TaskField.UNIQUE_ID) as Integer?
@Override set(`val`) {
set(TaskField.UNIQUE_ID, `val`)
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
    var updateNeeded:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.UPDATE_NEEDED) as Boolean?)
set(`val`) {
set(TaskField.UPDATE_NEEDED, `val`)
}

/**
 * The work breakdown structure code. The WBS field contains an
 * alphanumeric code you can use to represent the task's position within
 * the hierarchical structure of the project. This field is similar to
 * the outline number, except that you can edit it.
 *
 * @return string
 */
   /**
 * The work breakdown structure code. The WBS field contains an alphanumeric
 * code you can use to represent the task's position within the hierarchical
 * structure of the project. This field is similar to the outline number,
 * except that you can edit it.
 *
 * @param val - String
 */
    var wbs:String
get() =getCachedValue(TaskField.WBS)
set(`val`) =set(TaskField.WBS, `val`)

/**
 * The Work field shows the total amount of work scheduled to be performed
 * on a task by all assigned resources. This field shows the total work,
 * or person-hours, for a task.
 *
 * @return Duration representing duration .
 */
   /**
 * The Work field shows the total amount of work scheduled to be performed
 * on a task by all assigned resources. This field shows the total work,
 * or person-hours, for a task.
 *
 * @param val - duration
 */
    var work:Duration?
get() =getCachedValue(TaskField.WORK) as Duration?
set(`val`) {
set(TaskField.WORK, `val`)
}

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
    var workVariance:Duration?
get() {
var variance = getCachedValue(TaskField.WORK_VARIANCE) as Duration?
if (variance == null)
{
val work = work
val baselineWork = baselineWork
if (work != null && baselineWork != null)
{
variance = Duration.getInstance(work!!.getDuration() - baselineWork!!.convertUnits(work!!.getUnits(), parentFile.projectProperties).getDuration(), work!!.getUnits())
set(TaskField.WORK_VARIANCE, variance)
}
}
return variance
}
set(`val`) {
set(TaskField.WORK_VARIANCE, `val`)
}

/**
 * This method retrieves a reference to the parent of this task, as
 * defined by the outline level. If this task is at the top level,
 * this method will return null.
 *
 * @return parent task
 */
    val parentTask:Task?
get() =m_parent

/**
 * This method retrieves a list of child tasks relative to the
 * current task, as defined by the outine level. If there
 * are no child tasks, this method will return an empty list.
 *
 * @return child tasks
 */
    val childTasks:List<Task>
@Override get() =m_children

/**
 * This method retrieves a flag indicating whether the duration of the
 * task has only been estimated.
 *
 * @return boolean
 */
   /**
 * This method retrieves a flag indicating whether the duration of the
 * task has only been estimated.
 *
 * @param estimated Boolean flag
 */
    var estimated:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.ESTIMATED) as Boolean?)
set(estimated) {
set(TaskField.ESTIMATED, estimated)
}

/**
 * This method retrieves the deadline for this task.
 *
 * @return Task deadline
 */
   /**
 * This method sets the deadline for this task.
 *
 * @param deadline deadline date
 */
    var deadline:Date
get() =getCachedValue(TaskField.DEADLINE) as Date?
set(deadline) {
set(TaskField.DEADLINE, deadline)
}

/**
 * This method retrieves the task type.
 *
 * @return int representing the task type
 */
   /**
 * This method sets the task type.
 *
 * @param type task type
 */
    var type:TaskType
get() =getCachedValue(TaskField.TYPE) as TaskType?
set(type) {
set(TaskField.TYPE, type)
}

/**
 * Retrieve the recurring flag.
 *
 * @return recurring flag
 */
   /**
 * Set the recurring flag.
 *
 * @param recurring recurring flag
 */
    var recurring:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.RECURRING) as Boolean?)
set(recurring) {
set(TaskField.RECURRING, recurring)
}

/**
 * Retrieve the over allocated flag.
 *
 * @return over allocated flag
 */
   /**
 * Set the over allocated flag.
 *
 * @param overAllocated over allocated flag
 */
    var overAllocated:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.OVERALLOCATED) as Boolean?)
set(overAllocated) {
set(TaskField.OVERALLOCATED, overAllocated)
}

/**
 * Where a task in an MPP file represents a task from a subproject,
 * this value will be non-zero. The value itself is the unique ID
 * value shown in the parent project. To retrieve the value of the
 * task unique ID in the child project, remove the top two bytes:
 *
 * taskID = (subprojectUniqueID & 0xFFFF)
 *
 * @return sub project unique task ID
 */
   /**
 * Sets the sub project unique task ID.
 *
 * @param subprojectUniqueTaskID subproject unique task ID
 */
    var subprojectTaskUniqueID:Integer
get() =getCachedValue(TaskField.SUBPROJECT_UNIQUE_TASK_ID) as Integer?
set(subprojectUniqueTaskID) {
set(TaskField.SUBPROJECT_UNIQUE_TASK_ID, subprojectUniqueTaskID)
}

/**
 * Where a task in an MPP file represents a task from a subproject,
 * this value will be non-zero. The value itself is the ID
 * value shown in the parent project.
 *
 * @return sub project task ID
 */
   /**
 * Sets the sub project task ID.
 *
 * @param subprojectTaskID subproject task ID
 */
    var subprojectTaskID:Integer
get() =getCachedValue(TaskField.SUBPROJECT_TASK_ID) as Integer?
set(subprojectTaskID) {
set(TaskField.SUBPROJECT_TASK_ID, subprojectTaskID)
}

/**
 * Retrieves the offset added to unique task IDs from sub projects
 * to generate the task ID shown in the master project.
 *
 * @return unique ID offset
 */
   /**
 * Sets the offset added to unique task IDs from sub projects
 * to generate the task ID shown in the master project.
 *
 * @param offset unique ID offset
 */
    var subprojectTasksUniqueIDOffset:Integer
get() =getCachedValue(TaskField.SUBPROJECT_TASKS_UNIQUEID_OFFSET) as Integer?
set(offset) {
set(TaskField.SUBPROJECT_TASKS_UNIQUEID_OFFSET, offset)
}

/**
 * Retrieve the subproject read only flag.
 *
 * @return subproject read only flag
 */
   /**
 * Set the subproject read only flag.
 *
 * @param subprojectReadOnly subproject read only flag
 */
    var subprojectReadOnly:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.SUBPROJECT_READ_ONLY) as Boolean?)
set(subprojectReadOnly) {
set(TaskField.SUBPROJECT_READ_ONLY, subprojectReadOnly)
}

/**
 * Retrieves the external task flag.
 *
 * @return external task flag
 */
   /**
 * Sets the external task flag.
 *
 * @param externalTask external task flag
 */
    var externalTask:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.EXTERNAL_TASK) as Boolean?)
set(externalTask) {
set(TaskField.EXTERNAL_TASK, externalTask)
}

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
    var acwp:Number
get() =getCachedValue(TaskField.ACWP)
set(acwp) =set(TaskField.ACWP, acwp)

/**
 * Retrieves the ignore resource celandar flag.
 *
 * @return ignore resource celandar flag
 */
   /**
 * Sets the ignore resource celandar flag.
 *
 * @param ignoreResourceCalendar ignore resource celandar flag
 */
    var ignoreResourceCalendar:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.IGNORE_RESOURCE_CALENDAR) as Boolean?)
set(ignoreResourceCalendar) {
set(TaskField.IGNORE_RESOURCE_CALENDAR, ignoreResourceCalendar)
}

/**
 * Retrieves the physical percent complete value.
 *
 * @return physical percent complete value
 */
   /**
 * Sets the physical percent complete value.
 *
 * @param physicalPercentComplete physical percent complete value
 */
    var physicalPercentComplete:Number
get() =getCachedValue(TaskField.PHYSICAL_PERCENT_COMPLETE)
set(physicalPercentComplete) =set(TaskField.PHYSICAL_PERCENT_COMPLETE, physicalPercentComplete)

/**
 * Retrieves the earned value method.
 *
 * @return earned value method
 */
   /**
 * Sets the earned value method.
 *
 * @param earnedValueMethod earned value method
 */
    var earnedValueMethod:EarnedValueMethod
get() =getCachedValue(TaskField.EARNED_VALUE_METHOD) as EarnedValueMethod?
set(earnedValueMethod) {
set(TaskField.EARNED_VALUE_METHOD, earnedValueMethod)
}

/**
 * Retrieve the amount of regular work.
 *
 * @return amount of regular work
 */
   /**
 * Set the amount of regular work.
 *
 * @param regularWork amount of regular work
 */
    var regularWork:Duration
get() =getCachedValue(TaskField.REGULAR_WORK) as Duration?
set(regularWork) {
set(TaskField.REGULAR_WORK, regularWork)
}

/**
 * Retrieves the effort driven flag.
 *
 * @return Flag value
 */
   /**
 * Sets the effort driven flag.
 *
 * @param flag value
 */
    var effortDriven:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.EFFORT_DRIVEN) as Boolean?)
set(flag) {
set(TaskField.EFFORT_DRIVEN, flag)
}

/**
 * Retrieves the overtime cost.
 *
 * @return Cost value
 */
   /**
 * Sets the overtime cost value.
 *
 * @param number Cost value
 */
    var overtimeCost:Number
get() =getCachedValue(TaskField.OVERTIME_COST)
set(number) =set(TaskField.OVERTIME_COST, number)

/**
 * Retrieves the actual overtime cost for this task.
 *
 * @return actual overtime cost
 */
   /**
 * Sets the actual overtime cost for this task.
 *
 * @param cost actual overtime cost
 */
    var actualOvertimeCost:Number
get() =getCachedValue(TaskField.ACTUAL_OVERTIME_COST)
set(cost) =set(TaskField.ACTUAL_OVERTIME_COST, cost)

/**
 * Retrieves the actual overtime work value.
 *
 * @return actual overtime work value
 */
   /**
 * Sets the actual overtime work value.
 *
 * @param work actual overtime work value
 */
    var actualOvertimeWork:Duration
get() =getCachedValue(TaskField.ACTUAL_OVERTIME_WORK) as Duration?
set(work) {
set(TaskField.ACTUAL_OVERTIME_WORK, work)
}

/**
 * Retrieves the fixed cost accrual flag value.
 *
 * @return fixed cost accrual flag
 */
   /**
 * Sets the fixed cost accrual flag value.
 *
 * @param type fixed cost accrual type
 */
    var fixedCostAccrual:AccrueType
get() =getCachedValue(TaskField.FIXED_COST_ACCRUAL) as AccrueType?
set(type) {
set(TaskField.FIXED_COST_ACCRUAL, type)
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
    var hyperlink:String
get() =getCachedValue(TaskField.HYPERLINK)
set(text) =set(TaskField.HYPERLINK, text)

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
    var hyperlinkAddress:String
get() =getCachedValue(TaskField.HYPERLINK_ADDRESS)
set(text) =set(TaskField.HYPERLINK_ADDRESS, text)

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
    var hyperlinkSubAddress:String
get() =getCachedValue(TaskField.HYPERLINK_SUBADDRESS)
set(text) =set(TaskField.HYPERLINK_SUBADDRESS, text)

/**
 * Retrieves the level assignments flag.
 *
 * @return level assignments flag
 */
   /**
 * Sets the level assignments flag.
 *
 * @param flag level assignments flag
 */
    var levelAssignments:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.LEVEL_ASSIGNMENTS) as Boolean?)
set(flag) {
set(TaskField.LEVEL_ASSIGNMENTS, flag)
}

/**
 * Retrieves the leveling can split flag.
 *
 * @return leveling can split flag
 */
   /**
 * Sets the leveling can split flag.
 *
 * @param flag leveling can split flag
 */
    var levelingCanSplit:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.LEVELING_CAN_SPLIT) as Boolean?)
set(flag) {
set(TaskField.LEVELING_CAN_SPLIT, flag)
}

/**
 * Retrieves the overtime work attribute.
 *
 * @return overtime work value
 */
   /**
 * Sets the overtime work attribute.
 *
 * @param work overtime work value
 */
    var overtimeWork:Duration
get() =getCachedValue(TaskField.OVERTIME_WORK) as Duration?
set(work) {
set(TaskField.OVERTIME_WORK, work)
}

/**
 * Retrieves the preleveled start attribute.
 *
 * @return preleveled start
 */
   /**
 * Sets the preleveled start attribute.
 *
 * @param date preleveled start attribute
 */
    var preleveledStart:Date
get() =getCachedValue(TaskField.PRELEVELED_START) as Date?
set(date) {
set(TaskField.PRELEVELED_START, date)
}

/**
 * Retrieves the preleveled finish attribute.
 *
 * @return preleveled finish
 */
   /**
 * Sets the preleveled finish attribute.
 *
 * @param date preleveled finish attribute
 */
    var preleveledFinish:Date
get() =getCachedValue(TaskField.PRELEVELED_FINISH) as Date?
set(date) {
set(TaskField.PRELEVELED_FINISH, date)
}

/**
 * Retrieves the remaining overtime work attribute.
 *
 * @return remaining overtime work
 */
   /**
 * Sets the remaining overtime work attribute.
 *
 * @param work remaining overtime work
 */
    var remainingOvertimeWork:Duration
get() =getCachedValue(TaskField.REMAINING_OVERTIME_WORK) as Duration?
set(work) {
set(TaskField.REMAINING_OVERTIME_WORK, work)
}

/**
 * Retrieves the remaining overtime cost.
 *
 * @return remaining overtime cost value
 */
   /**
 * Sets the remaining overtime cost value.
 *
 * @param cost overtime cost value
 */
    var remainingOvertimeCost:Number
get() =getCachedValue(TaskField.REMAINING_OVERTIME_COST)
set(cost) =set(TaskField.REMAINING_OVERTIME_COST, cost)

/**
 * Retrieves the base calendar instance associated with this task.
 * Note that this attribute appears in MPP9 and MSPDI files.
 *
 * @return ProjectCalendar instance
 */
   /**
 * Sets the name of the base calendar associated with this task.
 * Note that this attribute appears in MPP9 and MSPDI files.
 *
 * @param calendar calendar instance
 */
    var calendar:ProjectCalendar?
get() =getCachedValue(TaskField.CALENDAR)
set(calendar) {
set(TaskField.CALENDAR, calendar)
calendarUniqueID = calendar?.uniqueID
}

/**
 * Retrieve the calendar unique ID.
 *
 * @return calendar unique ID
 */
   /**
 * Set the calendar unique ID.
 *
 * @param id calendar unique ID
 */
    var calendarUniqueID:Integer?
get() =getCachedValue(TaskField.CALENDAR_UNIQUE_ID) as Integer?
set(id) {
set(TaskField.CALENDAR_UNIQUE_ID, id)
}

/**
 * Retrieve the start slack.
 *
 * @return start slack
 */
   /**
 * Set the start slack.
 *
 * @param duration start slack
 */
    var startSlack:Duration?
get() {
var startSlack = getCachedValue(TaskField.START_SLACK) as Duration?
if (startSlack == null)
{
val duration = duration
if (duration != null)
{
startSlack = DateHelper.getVariance(this, earlyStart, lateStart, duration!!.getUnits())
set(TaskField.START_SLACK, startSlack)
}
}
return startSlack
}
set(duration) {
set(TaskField.START_SLACK, duration)
}

/**
 * Retrieve the finish slack.
 *
 * @return finish slack
 */
   /**
 * Set the finish slack.
 *
 * @param duration finish slack
 */
    var finishSlack:Duration?
get() {
var finishSlack = getCachedValue(TaskField.FINISH_SLACK) as Duration?
if (finishSlack == null)
{
val duration = duration
if (duration != null)
{
finishSlack = DateHelper.getVariance(this, earlyFinish, lateFinish, duration!!.getUnits())
set(TaskField.FINISH_SLACK, finishSlack)
}
}
return finishSlack
}
set(duration) {
set(TaskField.FINISH_SLACK, duration)
}

/**
 * Retrieve the "complete through" date.
 *
 * @return complete through date
 */
    val completeThrough:Date?
get() {
var value = getCachedValue(TaskField.COMPLETE_THROUGH) as Date?
if (value == null)
{
val percentComplete = NumberHelper.getInt(percentageComplete)
when (percentComplete) {
0 -> {}

100 -> {
value = actualFinish
}

else -> {
val actualStart = actualStart
var duration = duration
if (actualStart != null && duration != null)
{
val durationValue = duration!!.getDuration() * percentComplete / 100.0
duration = Duration.getInstance(durationValue, duration!!.getUnits())
val calendar = effectiveCalendar
value = calendar.getDate(actualStart, duration!!, true)
}
}
}

set(TaskField.COMPLETE_THROUGH, value)
}
return value
}

/**
 * Retrieve the summary progress date.
 *
 * @return summary progress date
 */
   /**
 * Set the summary progress date.
 *
 * @param value summary progress date
 */
    var summaryProgress:Date?
get() =getCachedValue(TaskField.SUMMARY_PROGRESS) as Date?
set(value) {
set(TaskField.SUMMARY_PROGRESS, value)
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
    var guid:UUID
get() =getCachedValue(TaskField.GUID) as UUID?
set(value) {
set(TaskField.GUID, value)
}

/**
 * Retrieves the task mode.
 *
 * @return task mode
 */
   /**
 * Sets the task mode.
 *
 * @param mode task mode
 */
    var taskMode:TaskMode
get() =if (BooleanHelper.getBoolean(getCachedValue(TaskField.TASK_MODE) as Boolean?)) TaskMode.MANUALLY_SCHEDULED else TaskMode.AUTO_SCHEDULED
set(mode) {
set(TaskField.TASK_MODE, mode === TaskMode.MANUALLY_SCHEDULED)
}

/**
 * Retrieves the active flag.
 *
 * @return active flag value
 */
   /**
 * Sets the active flag.
 *
 * @param active active flag value
 */
    var active:Boolean
get() =BooleanHelper.getBoolean(getCachedValue(TaskField.ACTIVE) as Boolean?)
set(active) {
set(TaskField.ACTIVE, active)
}

/**
 * Retrieve the baseline estimated duration.
 *
 * @return baseline estimated duration
 */
   /**
 * Set the baseline estimated duration.
 *
 * @param duration baseline estimated duration
 */
    var baselineEstimatedDuration:Duration
get() =getCachedValue(TaskField.BASELINE_ESTIMATED_DURATION) as Duration?
set(duration) {
set(TaskField.BASELINE_ESTIMATED_DURATION, duration)
}

/**
 * Retrieve the baseline estimated start.
 *
 * @return baseline estimated start
 */
   /**
 * Set the baseline estimated start.
 *
 * @param date baseline estimated start
 */
    var baselineEstimatedStart:Date
get() =getCachedValue(TaskField.BASELINE_ESTIMATED_START) as Date?
set(date) {
set(TaskField.BASELINE_ESTIMATED_START, date)
}

/**
 * Retrieve the baseline estimated finish.
 *
 * @return baseline estimated finish
 */
   /**
 * Set the baseline estimated finish.
 *
 * @param date baseline estimated finish
 */
    var baselineEstimatedFinish:Date
get() =getCachedValue(TaskField.BASELINE_ESTIMATED_FINISH) as Date?
set(date) {
set(TaskField.BASELINE_ESTIMATED_FINISH, date)
}

/**
 * The Fixed Cost field shows any task expense that is not associated
 * with a resource cost.
 *
 * @return currency amount
 */
   /**
 * The Fixed Cost field shows any task expense that is not associated
 * with a resource cost.
 *
 * @param val amount
 */
    var baselineFixedCost:Number
get() =getCachedValue(TaskField.BASELINE_FIXED_COST)
set(`val`) =set(TaskField.BASELINE_FIXED_COST, `val`)

/**
 * Retrieves the baseline fixed cost accrual.
 *
 * @return fixed cost accrual flag
 */
   /**
 * Sets the baseline fixed cost accrual.
 *
 * @param type fixed cost accrual type
 */
    var baselineFixedCostAccrual:AccrueType
get() =getCachedValue(TaskField.BASELINE_FIXED_COST_ACCRUAL) as AccrueType?
set(type) {
set(TaskField.BASELINE_FIXED_COST_ACCRUAL, type)
}

/**
 * Retrieve the effective calendar for this task. If the task does not have
 * a specific calendar associated with it, fall back to using the default calendar
 * for the project.
 *
 * @return ProjectCalendar instance
 */
    val effectiveCalendar:ProjectCalendar
get() {
var result = calendar
if (result == null)
{
result = parentFile.defaultCalendar
}
return result
}

/**
 * Array of field values.
 */
   private val m_array = arrayOfNulls<Object>(TaskField.MAX_VALUE)

/**
 * This list holds references to all tasks that are children of the
 * current task as specified by the outline level.
 */
   private val m_children = LinkedList<Task>()

/**
 * List of resource assignments for this task.
 */
   private val m_assignments = LinkedList<ResourceAssignment>()

/**
 * List of activity codes for this task.
 */
   /**
 * Retrieve the activity codes associated with this task.
 *
 * @return list of activity codes
 */
    val activityCodes:List<ActivityCodeValue> = LinkedList<ActivityCodeValue>()

/**
 * Recurring task details associated with this task.
 */
   private var m_recurringTask:RecurringTask? = null

private var m_eventsEnabled = true
/**
 * Retrieves the flag indicating if this is a null task.
 *
 * @return boolean flag
 */
   /**
 * Sets the flag indicating if this is a null task.
 *
 * @param isNull boolean flag
 */
    var `null`:Boolean = false
get() =field
/**
 * Retrieve the WBS level.
 *
 * @return WBS level
 */
   /**
 * Set the WBS level.
 *
 * @param wbsLevel WBS level
 */
    var wbsLevel:String? = null
get() =field
/**
 * Retrieve the resume valid flag.
 *
 * @return resume valie flag
 */
   /**
 * Set the resume valid flag.
 *
 * @param resumeValid resume valid flag
 */
    var resumeValid:Boolean = false
get() =field
/**
 * Retrieves the external task project file name.
 *
 * @return external task project file name
 */
   /**
 * Sets the external task project file name.
 *
 * @param externalTaskProject external task project file name
 */
    var externalTaskProject:String? = null
get() =field
/**
 * Retrieve the leveling delay format.
 *
 * @return leveling delay  format
 */
   /**
 * Set the leveling delay format.
 *
 * @param levelingDelayFormat leveling delay format
 */
    var levelingDelayFormat:TimeUnit? = null
get() =field
/**
 * Retrieves the actual work protected value.
 *
 * @return actual work protected value
 */
   /**
 * Sets the actual work protected value.
 *
 * @param actualWorkProtected actual work protected value
 */
    var actualWorkProtected:Duration? = null
get() =field
/**
 * Retrieves the actual overtime work protected value.
 *
 * @return actual overtime work protected value
 */
   /**
 * Sets the actual overtime work protected value.
 *
 * @param actualOvertimeWorkProtected actual overtime work protected value
 */
    var actualOvertimeWorkProtected:Duration? = null
get() =field
/**
 * Retrieve a flag indicating if the task is shown as expanded
 * in MS Project. If this flag is set to true, any sub tasks
 * for this current task will be visible. If this is false,
 * any sub tasks will be hidden.
 *
 * @return boolean flag
 */
   /**
 * Set a flag indicating if the task is shown as expanded
 * in MS Project. If this flag is set to true, any sub tasks
 * for this current task will be visible. If this is false,
 * any sub tasks will be hidden.
 *
 * @param expanded boolean flag
 */
    var expanded = true
get() =field

/**
 * This method retrieves a list of task splits. Each split is represented
 * by a DateRange instance. The list will always follow the pattern
 * task range, split range, task range and so on.
 *
 * Note that this method will return null if the task is not split.
 *
 * @return list of split times
 */
   /**
 * Internal method used to set the list of splits.
 *
 * @param splits list of split times
 */
    var splits:List<DateRange>? = null
get() =field
/**
 * Task splits contain the time up to which the splits are completed.
 *
 * @return Duration of completed time for the splits.
 */
   /**
 * Set the time up to which the splits are completed.
 *
 * @param splitsComplete Duration of completed time for the splits.
 */
    var splitCompleteDuration:Date? = null
/**
 * Retrieve the sub project represented by this task.
 *
 * @return sub project
 */
   /**
 * Set the sub project represented by this task.
 *
 * @param subProject sub project
 */
    var subProject:SubProject? = null
get() =field
private var m_listeners:List<FieldListener>? = null
init{

type = TaskType.FIXED_UNITS
constraintType = ConstraintType.AS_SOON_AS_POSSIBLE
taskMode = TaskMode.AUTO_SCHEDULED
active = true
set(TaskField.PREDECESSORS, LinkedList<Relation>())
set(TaskField.SUCCESSORS, LinkedList<Relation>())
val config = file.projectConfig

if (config.autoTaskUniqueID == true)
{
uniqueID = Integer.valueOf(config.nextTaskUniqueID)
}

if (config.autoTaskID == true)
{
id = Integer.valueOf(config.nextTaskID)
}

if (config.autoWBS == true)
{
generateWBS(m_parent)
}

if (config.autoOutlineNumber == true)
{
generateOutlineNumber(m_parent)
}

if (config.autoOutlineLevel == true)
{
if (m_parent == null)
{
outlineLevel = Integer.valueOf(1)
}
else
{
outlineLevel = Integer.valueOf(NumberHelper.getInt(m_parent!!.outlineLevel) + 1)
}
}
}//      m_array[TaskField.PREDECESSORS.getValue()] = new LinkedList<Relation>();
 //      m_array[TaskField.SUCCESSORS.getValue()] = new LinkedList<Relation>();

/**
 * This method is used to automatically generate a value
 * for the WBS field of this task.
 *
 * @param parent Parent Task
 */
    fun generateWBS(parent:Task?) {
var wbs:String

if (parent == null)
{
if (NumberHelper.getInt(uniqueID) == 0)
{
wbs = "0"
}
else
{
wbs = Integer.toString(parentFile.childTasks.size() + 1)
}
}
else
{
wbs = parent.wbs

 //
         // Apparently I added the next lines to support MPX files generated by Artemis, back in 2005
         // Unfortunately I have no test data which exercises this code, and it now breaks
         // otherwise valid WBS values read (in this case) from XER files. So it's commented out
         // until someone complains about their 2005-era Artemis MPX files not working!
         //
         //         int index = wbs.lastIndexOf(".0");
         //         if (index != -1)
         //         {
         //            wbs = wbs.substring(0, index);
         //         }

         val childTaskCount = parent.childTasks.size() + 1
if (wbs.equals("0"))
{
wbs = Integer.toString(childTaskCount)
}
else
{
wbs += ".$childTaskCount"
}
}

wbs = wbs
}

/**
 * This method is used to automatically generate a value
 * for the Outline Number field of this task.
 *
 * @param parent Parent Task
 */
    fun generateOutlineNumber(parent:Task?) {
var outline:String

if (parent == null)
{
if (NumberHelper.getInt(uniqueID) == 0)
{
outline = "0"
}
else
{
outline = Integer.toString(parentFile.childTasks.size() + 1)
}
}
else
{
outline = parent.outlineNumber

val index = outline.lastIndexOf(".0")

if (index != -1)
{
outline = outline.substring(0, index)
}

val childTaskCount = parent.childTasks.size() + 1
if (outline.equals("0"))
{
outline = Integer.toString(childTaskCount)
}
else
{
outline += ".$childTaskCount"
}
}

outlineNumber = outline
}

/**
 * This method allows nested tasks to be added, with the WBS being
 * completed automatically.
 *
 * @return new task
 */
   @Override  fun addTask():Task {
val parent = parentFile

val task = Task(parent, this)

m_children.add(task)

parent.tasks.add(task)

summary = true

return task
}

/**
 * This method is used to associate a child task with the current
 * task instance. It has package access, and has been designed to
 * allow the hierarchical outline structure of tasks in an MPX
 * file to be constructed as the file is read in.
 *
 * @param child Child task.
 * @param childOutlineLevel Outline level of the child task.
 */
    fun addChildTask(child:Task, childOutlineLevel:Int) {
val outlineLevel = NumberHelper.getInt(outlineLevel)

if (outlineLevel + 1 == childOutlineLevel)
{
m_children.add(child)
summary = true
}
else
{
if (m_children.isEmpty() === false)
{
m_children.get(m_children.size() - 1).addChildTask(child, childOutlineLevel)
}
}
}

/**
 * This method is used to associate a child task with the current
 * task instance. It has been designed to
 * allow the hierarchical outline structure of tasks in an MPX
 * file to be updated once all of the task data has been read.
 *
 * @param child child task
 */
    fun addChildTask(child:Task) {
child.m_parent = this
m_children.add(child)
summary = true

if (parentFile.projectConfig.autoOutlineLevel == true)
{
child.outlineLevel = Integer.valueOf(NumberHelper.getInt(outlineLevel) + 1)
}
}

/**
 * Inserts a child task prior to a given sibling task.
 *
 * @param child new child task
 * @param previousSibling sibling task
 */
    fun addChildTaskBefore(child:Task, previousSibling:Task) {
val index = m_children.indexOf(previousSibling)
if (index == -1)
{
m_children.add(child)
}
else
{
m_children.add(index, child)
}

child.m_parent = this
summary = true

if (parentFile.projectConfig.autoOutlineLevel == true)
{
child.outlineLevel = Integer.valueOf(NumberHelper.getInt(outlineLevel) + 1)
}
}

/**
 * Removes a child task.
 *
 * @param child child task instance
 */
    fun removeChildTask(child:Task) {
if (m_children.remove(child))
{
child.m_parent = null
}
summary = !m_children.isEmpty()
}

/**
 * This method allows the list of child tasks to be cleared in preparation
 * for the hierarchical task structure to be built.
 */
    fun clearChildTasks() {
m_children.clear()
summary = false
}

/**
 * This method allows recurring task details to be added to the
 * current task.
 *
 * @return RecurringTask object
 */
    fun addRecurringTask():RecurringTask {
if (m_recurringTask == null)
{
m_recurringTask = RecurringTask()
}

return m_recurringTask
}

/**
 * Assign an activity code to this task.
 *
 * @param value activity coe value
 */
    fun addActivityCode(value:ActivityCodeValue) {
activityCodes.add(value)
}

/**
 * This method allows a resource assignment to be added to the
 * current task.
 *
 * @param resource the resource to assign
 * @return ResourceAssignment object
 */
    fun addResourceAssignment(resource:Resource?):ResourceAssignment {
var assignment = getExistingResourceAssignment(resource)

if (assignment == null)
{
assignment = ResourceAssignment(parentFile, this)
m_assignments.add(assignment)
parentFile.resourceAssignments.add(assignment)

assignment.taskUniqueID = uniqueID
assignment.work = duration
assignment.units = ResourceAssignment.DEFAULT_UNITS

if (resource != null)
{
assignment.resourceUniqueID = resource.uniqueID
resource.addResourceAssignment(assignment)
}
}

return assignment
}

/**
 * Add a resource assignment which has been populated elsewhere.
 *
 * @param assignment resource assignment
 */
    fun addResourceAssignment(assignment:ResourceAssignment) {
if (getExistingResourceAssignment(assignment.resource) == null)
{
m_assignments.add(assignment)
parentFile.resourceAssignments.add(assignment)

val resource = assignment.resource
resource?.addResourceAssignment(assignment)
}
}

/**
 * Retrieves an existing resource assignment if one is present,
 * to prevent duplicate resource assignments being added.
 *
 * @param resource resource to test for
 * @return existing resource assignment
 */
   private fun getExistingResourceAssignment(resource:Resource?):ResourceAssignment? {
var assignment:ResourceAssignment? = null
var resourceUniqueID:Integer? = null

if (resource != null)
{
val iter = m_assignments.iterator()
resourceUniqueID = resource.uniqueID

while (iter.hasNext() === true)
{
assignment = iter.next()
val uniqueID = assignment!!.resourceUniqueID
if (uniqueID != null && uniqueID.equals(resourceUniqueID) === true)
{
break
}
assignment = null
}
}

return assignment
}

/**
 * Internal method used as part of the process of removing a
 * resource assignment.
 *
 * @param assignment resource assignment to be removed
 */
   internal fun removeResourceAssignment(assignment:ResourceAssignment) {
m_assignments.remove(assignment)
}

/**
 * This method allows a predecessor relationship to be added to this
 * task instance.
 *
 * @param targetTask the predecessor task
 * @param type relation type
 * @param lag relation lag
 * @return relationship
 */
   @SuppressWarnings("unchecked")  fun addPredecessor(targetTask:Task, type:RelationType, lag:Duration?):Relation {
var lag = lag
 //
      // Ensure that we have a valid lag duration
      //
      if (lag == null)
{
lag = Duration.getInstance(0, TimeUnit.DAYS)
}

 //
      // Retrieve the list of predecessors
      //
      val predecessorList = getCachedValue(TaskField.PREDECESSORS) as List<Relation>?

 //
      // Ensure that there is only one predecessor relationship between
      // these two tasks.
      //
      var predecessorRelation:Relation? = null
var iter = predecessorList!!.iterator()
while (iter.hasNext() === true)
{
predecessorRelation = iter.next()
if (predecessorRelation.targetTask == targetTask)
{
if (predecessorRelation.type != type || predecessorRelation.lag!!.compareTo(lag) !== 0)
{
predecessorRelation = null
}
break
}
predecessorRelation = null
}

 //
      // If necessary, create a new predecessor relationship
      //
      if (predecessorRelation == null)
{
predecessorRelation = Relation(this, targetTask, type, lag)
predecessorList.add(predecessorRelation)
}

 //
      // Retrieve the list of successors
      //
      val successorList = targetTask.getCachedValue(TaskField.SUCCESSORS) as List<Relation>?

 //
      // Ensure that there is only one successor relationship between
      // these two tasks.
      //
      var successorRelation:Relation? = null
iter = successorList!!.iterator()
while (iter.hasNext() === true)
{
successorRelation = iter.next()
if (successorRelation.targetTask == this)
{
if (successorRelation.type != type || successorRelation.lag!!.compareTo(lag) !== 0)
{
successorRelation = null
}
break
}
successorRelation = null
}

 //
      // If necessary, create a new successor relationship
      //
      if (successorRelation == null)
{
successorRelation = Relation(targetTask, this, type, lag)
successorList.add(successorRelation)
}

return predecessorRelation
}

/**
 * Set a cost value.
 *
 * @param index cost index (1-10)
 * @param value cost value
 */
    fun setCost(index:Int, value:Number) {
set(selectField(TaskFieldLists.CUSTOM_COST, index), value)
}

/**
 * Retrieve a cost value.
 *
 * @param index cost index (1-10)
 * @return cost value
 */
    fun getCost(index:Int):Number {
return getCachedValue(selectField(TaskFieldLists.CUSTOM_COST, index))
}

/**
 * Set a duration value.
 *
 * @param index duration index (1-10)
 * @param value duration value
 */
    fun setDuration(index:Int, value:Duration) {
set(selectField(TaskFieldLists.CUSTOM_DURATION, index), value)
}

/**
 * Retrieve a duration value.
 *
 * @param index duration index (1-10)
 * @return duration value
 */
    fun getDuration(index:Int):Duration {
return getCachedValue(selectField(TaskFieldLists.CUSTOM_DURATION, index)) as Duration?
}

/**
 * Set a finish value.
 *
 * @param index finish index (1-10)
 * @param value finish value
 */
    fun setFinish(index:Int, value:Date) {
set(selectField(TaskFieldLists.CUSTOM_FINISH, index), value)
}

/**
 * Retrieve a finish value.
 *
 * @param index finish index (1-10)
 * @return finish value
 */
    fun getFinish(index:Int):Date {
return getCachedValue(selectField(TaskFieldLists.CUSTOM_FINISH, index)) as Date?
}

/**
 * Set a flag value.
 *
 * @param index flag index (1-20)
 * @param value flag value
 */
    fun setFlag(index:Int, value:Boolean) {
set(selectField(TaskFieldLists.CUSTOM_FLAG, index), value)
}

/**
 * Retrieve a flag value.
 *
 * @param index flag index (1-20)
 * @return flag value
 */
    fun getFlag(index:Int):Boolean {
return BooleanHelper.getBoolean(getCachedValue(selectField(TaskFieldLists.CUSTOM_FLAG, index)) as Boolean?)
}

/**
 * Set a number value.
 *
 * @param index number index (1-20)
 * @param value number value
 */
    fun setNumber(index:Int, value:Number) {
set(selectField(TaskFieldLists.CUSTOM_NUMBER, index), value)
}

/**
 * Retrieve a number value.
 *
 * @param index number index (1-20)
 * @return number value
 */
    fun getNumber(index:Int):Number {
return getCachedValue(selectField(TaskFieldLists.CUSTOM_NUMBER, index))
}

/**
 * Set a start value.
 *
 * @param index start index (1-10)
 * @param value start value
 */
    fun setStart(index:Int, value:Date) {
set(selectField(TaskFieldLists.CUSTOM_START, index), value)
}

/**
 * Retrieve a start value.
 *
 * @param index start index (1-10)
 * @return start value
 */
    fun getStart(index:Int):Date {
return getCachedValue(selectField(TaskFieldLists.CUSTOM_START, index)) as Date?
}

/**
 * Set a text value.
 *
 * @param index text index (1-30)
 * @param value text value
 */
    fun setText(index:Int, value:String) {
set(selectField(TaskFieldLists.CUSTOM_TEXT, index), value)
}

/**
 * Retrieve a text value.
 *
 * @param index text index (1-30)
 * @return text value
 */
    fun getText(index:Int):String {
return getCachedValue(selectField(TaskFieldLists.CUSTOM_TEXT, index))
}

/**
 * Set an outline code value.
 *
 * @param index outline code index (1-10)
 * @param value outline code value
 */
    fun setOutlineCode(index:Int, value:String) {
set(selectField(TaskFieldLists.CUSTOM_OUTLINE_CODE, index), value)
}

/**
 * Retrieve an outline code value.
 *
 * @param index outline code index (1-10)
 * @return outline code value
 */
    fun getOutlineCode(index:Int):String {
return getCachedValue(selectField(TaskFieldLists.CUSTOM_OUTLINE_CODE, index))
}

/**
 * This method implements the only method in the Comparable interface.
 * This allows Tasks to be compared and sorted based on their ID value.
 * Note that if the MPX/MPP file has been generated by MSP, the ID value
 * will always be in the correct sequence. The Unique ID value will not
 * necessarily be in the correct sequence as task insertions and deletions
 * will change the order.
 *
 * @param o object to compare this instance with
 * @return result of comparison
 */
   @Override  fun compareTo(o:Task):Int {
val id1 = NumberHelper.getInt(id)
val id2 = NumberHelper.getInt(o.id)
return if (id1 < id2) -1 else if (id1 == id2) 0 else 1
}

/**
 * Set a date value.
 *
 * @param index date index (1-10)
 * @param value date value
 */
    fun setDate(index:Int, value:Date) {
set(selectField(TaskFieldLists.CUSTOM_DATE, index), value)
}

/**
 * Retrieve a date value.
 *
 * @param index date index (1-10)
 * @return date value
 */
    fun getDate(index:Int):Date {
return getCachedValue(selectField(TaskFieldLists.CUSTOM_DATE, index)) as Date?
}

/**
 * Retrieve the value of a field using its alias.
 *
 * @param alias field alias
 * @return field value
 */
    fun getFieldByAlias(alias:String):Object? {
return getCachedValue(parentFile.customFields.getFieldByAlias(FieldTypeClass.TASK, alias))
}

/**
 * Set the value of a field using its alias.
 *
 * @param alias field alias
 * @param value field value
 */
    fun setFieldByAlias(alias:String, value:Object) {
set(parentFile.customFields.getFieldByAlias(FieldTypeClass.TASK, alias), value)
}

/**
 * Removes this task from the project.
 */
    fun remove() {
parentFile.removeTask(this)
}

/**
 * Retrieve an enterprise field value.
 *
 * @param index field index
 * @return field value
 */
    fun getEnterpriseCost(index:Int):Number {
return getCachedValue(selectField(TaskFieldLists.ENTERPRISE_COST, index))
}

/**
 * Set an enterprise field value.
 *
 * @param index field index
 * @param value field value
 */
    fun setEnterpriseCost(index:Int, value:Number) {
set(selectField(TaskFieldLists.ENTERPRISE_COST, index), value)
}

/**
 * Retrieve an enterprise field value.
 *
 * @param index field index
 * @return field value
 */
    fun getEnterpriseDate(index:Int):Date {
return getCachedValue(selectField(TaskFieldLists.ENTERPRISE_DATE, index)) as Date?
}

/**
 * Set an enterprise field value.
 *
 * @param index field index
 * @param value field value
 */
    fun setEnterpriseDate(index:Int, value:Date) {
set(selectField(TaskFieldLists.ENTERPRISE_DATE, index), value)
}

/**
 * Retrieve an enterprise field value.
 *
 * @param index field index
 * @return field value
 */
    fun getEnterpriseDuration(index:Int):Duration {
return getCachedValue(selectField(TaskFieldLists.ENTERPRISE_DURATION, index)) as Duration?
}

/**
 * Set an enterprise field value.
 *
 * @param index field index
 * @param value field value
 */
    fun setEnterpriseDuration(index:Int, value:Duration) {
set(selectField(TaskFieldLists.ENTERPRISE_DURATION, index), value)
}

/**
 * Retrieve an enterprise field value.
 *
 * @param index field index
 * @return field value
 */
    fun getEnterpriseFlag(index:Int):Boolean {
return BooleanHelper.getBoolean(getCachedValue(selectField(TaskFieldLists.ENTERPRISE_FLAG, index)) as Boolean?)
}

/**
 * Set an enterprise field value.
 *
 * @param index field index
 * @param value field value
 */
    fun setEnterpriseFlag(index:Int, value:Boolean) {
set(selectField(TaskFieldLists.ENTERPRISE_FLAG, index), value)
}

/**
 * Retrieve an enterprise field value.
 *
 * @param index field index
 * @return field value
 */
    fun getEnterpriseNumber(index:Int):Number {
return getCachedValue(selectField(TaskFieldLists.ENTERPRISE_NUMBER, index))
}

/**
 * Set an enterprise field value.
 *
 * @param index field index
 * @param value field value
 */
    fun setEnterpriseNumber(index:Int, value:Number) {
set(selectField(TaskFieldLists.ENTERPRISE_NUMBER, index), value)
}

/**
 * Retrieve an enterprise field value.
 *
 * @param index field index
 * @return field value
 */
    fun getEnterpriseText(index:Int):String {
return getCachedValue(selectField(TaskFieldLists.ENTERPRISE_TEXT, index))
}

/**
 * Set an enterprise field value.
 *
 * @param index field index
 * @param value field value
 */
    fun setEnterpriseText(index:Int, value:String) {
set(selectField(TaskFieldLists.ENTERPRISE_TEXT, index), value)
}

/**
 * Retrieve an enterprise custom field value.
 *
 * @param index field index
 * @return field value
 */
    fun getEnterpriseCustomField(index:Int):String {
return getCachedValue(selectField(TaskFieldLists.ENTERPRISE_CUSTOM_FIELD, index))
}

/**
 * Set an enterprise custom field value.
 *
 * @param index field index
 * @param value field value
 */
    fun setEnterpriseCustomField(index:Int, value:String) {
set(selectField(TaskFieldLists.ENTERPRISE_CUSTOM_FIELD, index), value)
}

/**
 * Set a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @param value baseline value
 */
    fun setBaselineCost(baselineNumber:Int, value:Number) {
set(selectField(TaskFieldLists.BASELINE_COSTS, baselineNumber), value)
}

/**
 * Set a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @param value baseline value
 */
    fun setBaselineDuration(baselineNumber:Int, value:Duration) {
set(selectField(TaskFieldLists.BASELINE_DURATIONS, baselineNumber), value)
}

/**
 * Set a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @param value baseline value
 */
    fun setBaselineFinish(baselineNumber:Int, value:Date) {
set(selectField(TaskFieldLists.BASELINE_FINISHES, baselineNumber), value)
}

/**
 * Set a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @param value baseline value
 */
    fun setBaselineStart(baselineNumber:Int, value:Date) {
set(selectField(TaskFieldLists.BASELINE_STARTS, baselineNumber), value)
}

/**
 * Set a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @param value baseline value
 */
    fun setBaselineWork(baselineNumber:Int, value:Duration) {
set(selectField(TaskFieldLists.BASELINE_WORKS, baselineNumber), value)
}

/**
 * Retrieve a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @return baseline value
 */
    fun getBaselineCost(baselineNumber:Int):Number {
return getCachedValue(selectField(TaskFieldLists.BASELINE_COSTS, baselineNumber))
}

/**
 * Retrieve a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @return baseline value
 */
    fun getBaselineDuration(baselineNumber:Int):Duration {
var result = getCachedValue(selectField(TaskFieldLists.BASELINE_DURATIONS, baselineNumber))
if (result == null)
{
result = getCachedValue(selectField(TaskFieldLists.BASELINE_ESTIMATED_DURATIONS, baselineNumber))
}

if (result !is Duration)
{
result = null
}
return result as Duration?
}

/**
 * Retrieves the baseline duration text value.
 *
 * @param baselineNumber baseline number
 * @return baseline duration text value
 */
    fun getBaselineDurationText(baselineNumber:Int):String {
var result = getCachedValue(selectField(TaskFieldLists.BASELINE_DURATIONS, baselineNumber))
if (result == null)
{
result = getCachedValue(selectField(TaskFieldLists.BASELINE_ESTIMATED_DURATIONS, baselineNumber))
}

if (result !is String)
{
result = null
}
return result
}

/**
 * Sets the baseline duration text value.
 *
 * @param baselineNumber baseline number
 * @param value baseline duration text value
 */
    fun setBaselineDurationText(baselineNumber:Int, value:String) {
set(selectField(TaskFieldLists.BASELINE_DURATIONS, baselineNumber), value)
}

/**
 * Retrieve a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @return baseline value
 */
    fun getBaselineFinish(baselineNumber:Int):Date {
var result = getCachedValue(selectField(TaskFieldLists.BASELINE_FINISHES, baselineNumber))
if (result == null)
{
result = getCachedValue(selectField(TaskFieldLists.BASELINE_ESTIMATED_FINISHES, baselineNumber))
}

if (result !is Date)
{
result = null
}
return result as Date?
}

/**
 * Retrieves the baseline finish text value.
 *
 * @param baselineNumber baseline number
 * @return baseline finish text value
 */
    fun getBaselineFinishText(baselineNumber:Int):String {
var result = getCachedValue(selectField(TaskFieldLists.BASELINE_FINISHES, baselineNumber))
if (result == null)
{
result = getCachedValue(selectField(TaskFieldLists.BASELINE_ESTIMATED_FINISHES, baselineNumber))
}

if (result !is String)
{
result = null
}
return result
}

/**
 * Sets the baseline finish text value.
 *
 * @param baselineNumber baseline number
 * @param value baseline finish text value
 */
    fun setBaselineFinishText(baselineNumber:Int, value:String) {
set(selectField(TaskFieldLists.BASELINE_FINISHES, baselineNumber), value)
}

/**
 * Retrieve a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @return baseline value
 */
    fun getBaselineStart(baselineNumber:Int):Date {
var result = getCachedValue(selectField(TaskFieldLists.BASELINE_STARTS, baselineNumber))
if (result == null)
{
result = getCachedValue(selectField(TaskFieldLists.BASELINE_ESTIMATED_STARTS, baselineNumber))
}

if (result !is Date)
{
result = null
}
return result as Date?
}

/**
 * Retrieves the baseline start text value.
 *
 * @param baselineNumber baseline number
 * @return baseline start text value
 */
    fun getBaselineStartText(baselineNumber:Int):String {
var result = getCachedValue(selectField(TaskFieldLists.BASELINE_STARTS, baselineNumber))
if (result == null)
{
result = getCachedValue(selectField(TaskFieldLists.BASELINE_ESTIMATED_STARTS, baselineNumber))
}

if (result !is String)
{
result = null
}
return result
}

/**
 * Sets the baseline start text value.
 *
 * @param baselineNumber baseline number
 * @param value baseline start text value
 */
    fun setBaselineStartText(baselineNumber:Int, value:String) {
set(selectField(TaskFieldLists.BASELINE_STARTS, baselineNumber), value)
}

/**
 * Retrieve a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @return baseline value
 */
    fun getBaselineWork(baselineNumber:Int):Duration {
return getCachedValue(selectField(TaskFieldLists.BASELINE_WORKS, baselineNumber)) as Duration?
}

/**
 * Set a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @param value baseline value
 */
    fun setBaselineEstimatedDuration(baselineNumber:Int, value:Duration) {
set(selectField(TaskFieldLists.BASELINE_ESTIMATED_DURATIONS, baselineNumber), value)
}

/**
 * Retrieve a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @return baseline value
 */
    fun getBaselineEstimatedDuration(baselineNumber:Int):Duration {
var result = getCachedValue(selectField(TaskFieldLists.BASELINE_ESTIMATED_DURATIONS, baselineNumber))
if (result !is Duration)
{
result = null
}
return result as Duration?
}

/**
 * Retrieve a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @return baseline value
 */
    fun getBaselineEstimatedStart(baselineNumber:Int):Date {
var result = getCachedValue(selectField(TaskFieldLists.BASELINE_ESTIMATED_STARTS, baselineNumber))
if (result !is Date)
{
result = null
}
return result as Date?
}

/**
 * Set a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @param value baseline value
 */
    fun setBaselineEstimatedStart(baselineNumber:Int, value:Date) {
set(selectField(TaskFieldLists.BASELINE_ESTIMATED_STARTS, baselineNumber), value)
}

/**
 * Retrieve a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @return baseline value
 */
    fun getBaselineEstimatedFinish(baselineNumber:Int):Date {
var result = getCachedValue(selectField(TaskFieldLists.BASELINE_ESTIMATED_FINISHES, baselineNumber))
if (result !is Date)
{
result = null
}
return result as Date?
}

/**
 * Set a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @param value baseline value
 */
    fun setBaselineEstimatedFinish(baselineNumber:Int, value:Date) {
set(selectField(TaskFieldLists.BASELINE_ESTIMATED_FINISHES, baselineNumber), value)
}

/**
 * Set a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @param value baseline value
 */
    fun setBaselineFixedCost(baselineNumber:Int, value:Number) {
set(selectField(TaskFieldLists.BASELINE_FIXED_COSTS, baselineNumber), value)
}

/**
 * Retrieve a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @return baseline value
 */
    fun getBaselineFixedCost(baselineNumber:Int):Number {
return getCachedValue(selectField(TaskFieldLists.BASELINE_FIXED_COSTS, baselineNumber))
}

/**
 * Set a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @param value baseline value
 */
    fun setBaselineFixedCostAccrual(baselineNumber:Int, value:AccrueType) {
set(selectField(TaskFieldLists.BASELINE_FIXED_COST_ACCRUALS, baselineNumber), value)
}

/**
 * Retrieve a baseline value.
 *
 * @param baselineNumber baseline index (1-10)
 * @return baseline value
 */
    fun getBaselineFixedCostAccrual(baselineNumber:Int):AccrueType {
return getCachedValue(selectField(TaskFieldLists.BASELINE_FIXED_COST_ACCRUALS, baselineNumber)) as AccrueType?
}

/**
 * This method allows a predecessor relationship to be removed from this
 * task instance.  It will only delete relationships that exactly match the
 * given targetTask, type and lag time.
 *
 * @param targetTask the predecessor task
 * @param type relation type
 * @param lag relation lag
 * @return returns true if the relation is found and removed
 */
    fun removePredecessor(targetTask:Task, type:RelationType, lag:Duration?):Boolean {
var lag = lag
var matchFound = false

 //
      // Retrieve the list of predecessors
      //
      val predecessorList = predecessors
if (!predecessorList.isEmpty())
{
 //
         // Ensure that we have a valid lag duration
         //
         if (lag == null)
{
lag = Duration.getInstance(0, TimeUnit.DAYS)
}

 //
         // Ensure that there is a predecessor relationship between
         // these two tasks, and remove it.
         //
         matchFound = removeRelation(predecessorList, targetTask, type, lag)

 //
         // If we have removed a predecessor, then we must remove the
         // corresponding successor entry from the target task list
         //
         if (matchFound)
{
 //
            // Retrieve the list of successors
            //
            val successorList = targetTask.successors
if (!successorList.isEmpty())
{
 //
               // Ensure that there is a successor relationship between
               // these two tasks, and remove it.
               //
               removeRelation(successorList, this, type, lag)
}
}
}

return matchFound
}

/**
 * Internal method used to locate an remove an item from a list Relations.
 *
 * @param relationList list of Relation instances
 * @param targetTask target relationship task
 * @param type target relationship type
 * @param lag target relationship lag
 * @return true if a relationship was removed
 */
   private fun removeRelation(relationList:List<Relation>, targetTask:Task, type:RelationType, lag:Duration?):Boolean {
var matchFound = false
for (relation in relationList)
{
if (relation.targetTask == targetTask)
{
if (relation.type == type && relation.lag!!.compareTo(lag) === 0)
{
matchFound = relationList.remove(relation)
break
}
}
}
return matchFound
}

/**
 * Maps a field index to a TaskField instance.
 *
 * @param fields array of fields used as the basis for the mapping.
 * @param index required field index
 * @return TaskField instance
 */
   private fun selectField(fields:Array<TaskField>, index:Int):TaskField {
if (index < 1 || index > fields.size)
{
throw IllegalArgumentException("$index is not a valid field index")
}
return fields[index - 1]
}

/**
 * {@inheritDoc}
 */
   @Override  fun getCachedValue(field:FieldType?):Object? {
return if (field == null) null else m_array[field!!.getValue()]
}

/**
 * {@inheritDoc}
 */
   @Override  fun getCurrentValue(field:FieldType?):Object? {
var result:Object? = null

if (field != null)
{
when (field as TaskField?) {
PARENT_TASK_UNIQUE_ID -> {
result = if (m_parent == null) Integer.valueOf(-1) else m_parent!!.uniqueID
}

START_VARIANCE -> {
result = startVariance
}

FINISH_VARIANCE -> {
result = finishVariance
}

START_SLACK -> {
result = startSlack
}

FINISH_SLACK -> {
result = finishSlack
}

COST_VARIANCE -> {
result = costVariance
}

DURATION_VARIANCE -> {
result = durationVariance
}

WORK_VARIANCE -> {
result = workVariance
}

CV -> {
result = cv
}

SV -> {
result = sv
}

TOTAL_SLACK -> {
result = totalSlack
}

CRITICAL -> {
result = Boolean.valueOf(critical)
}

COMPLETE_THROUGH -> {
result = completeThrough
}

else -> {
result = m_array[field!!.getValue()]
}
}
}

return result
}

/**
 * {@inheritDoc}
 */
   @Overrideoperator  fun set(field:FieldType?, value:Object) {
if (field != null)
{
val index = field!!.getValue()
if (m_eventsEnabled)
{
fireFieldChangeEvent((field as TaskField?)!!, m_array[index], value)
}
m_array[index] = value
}
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
   private fun fireFieldChangeEvent(field:TaskField, oldValue:Object?, newValue:Object) {
 //
      // Internal event handling
      //
      when (field) {
UNIQUE_ID -> {
val parent = parentFile
if (oldValue != null)
{
parent.tasks.unmapUniqueID(oldValue as Integer?)
}
parent.tasks.mapUniqueID(newValue as Integer, this)
}

START, BASELINE_START -> {
m_array[TaskField.START_VARIANCE.getValue()] = null
}

FINISH, BASELINE_FINISH -> {
m_array[TaskField.FINISH_VARIANCE.getValue()] = null
}

COST, BASELINE_COST -> {
m_array[TaskField.COST_VARIANCE.getValue()] = null
}

DURATION -> {
m_array[TaskField.DURATION_VARIANCE.getValue()] = null
m_array[TaskField.COMPLETE_THROUGH.getValue()] = null
}

BASELINE_DURATION -> {
m_array[TaskField.DURATION_VARIANCE.getValue()] = null
}

WORK, BASELINE_WORK -> {
m_array[TaskField.WORK_VARIANCE.getValue()] = null
}

BCWP, ACWP -> {
m_array[TaskField.CV.getValue()] = null
m_array[TaskField.SV.getValue()] = null
}

BCWS -> {
m_array[TaskField.SV.getValue()] = null
}

START_SLACK, FINISH_SLACK -> {
m_array[TaskField.TOTAL_SLACK.getValue()] = null
m_array[TaskField.CRITICAL.getValue()] = null
}

EARLY_FINISH, LATE_FINISH -> {
m_array[TaskField.FINISH_SLACK.getValue()] = null
m_array[TaskField.TOTAL_SLACK.getValue()] = null
m_array[TaskField.CRITICAL.getValue()] = null
}

EARLY_START, LATE_START -> {
m_array[TaskField.START_SLACK.getValue()] = null
m_array[TaskField.TOTAL_SLACK.getValue()] = null
m_array[TaskField.CRITICAL.getValue()] = null
}

ACTUAL_START, PERCENT_COMPLETE -> {
m_array[TaskField.COMPLETE_THROUGH.getValue()] = null
}

else -> {}
}

 //
      // External event handling
      //
      if (m_listeners != null)
{
for (listener in m_listeners!!)
{
listener.fieldChange(this, field, oldValue, newValue)
}
}
}

/**
 * {@inheritDoc}
 */
   @Override  fun addFieldListener(listener:FieldListener) {
if (m_listeners == null)
{
m_listeners = LinkedList<FieldListener>()
}
m_listeners!!.add(listener)
}

/**
 * {@inheritDoc}
 */
   @Override  fun removeFieldListener(listener:FieldListener) {
if (m_listeners != null)
{
m_listeners!!.remove(listener)
}
}

/**
 * This method inserts a name value pair into internal storage.
 *
 * @param field task field
 * @param value attribute value
 */
   private operator fun set(field:FieldType, value:Boolean) {
set(field, if (value) Boolean.TRUE else Boolean.FALSE)
}

/**
 * {@inheritDoc}
 */
   @Override  fun toString():String {
return "[Task id=" + id + " uniqueID=" + uniqueID + " name=" + name + (if (externalTask) " [EXTERNAL uid=$subprojectTaskUniqueID id=$subprojectTaskID]" else "]") + if (subProject == null) "" else " project=" + subProject!!
}

/**
 * Utility method used to determine if the supplied task
 * is a predecessor of the current task.
 *
 * @param task potential predecessor task
 * @return Boolean flag
 */
    fun isPredecessor(task:Task):Boolean {
return isRelated(task, predecessors)
}

/**
 * Utility method used to determine if the supplied task
 * is a successor of the current task.
 *
 * @param task potential successor task
 * @return Boolean flag
 */
    fun isSucessor(task:Task):Boolean {
return isRelated(task, successors)
}

/**
 * Used to determine if a task has child tasks.
 *
 * @return true if the task has child tasks
 */
    fun hasChildTasks():Boolean {
return !m_children.isEmpty()
}

/**
 * Internal method used to test for the existence of a relationship
 * with a task.
 *
 * @param task target task
 * @param list list of relationships
 * @return boolean flag
 */
   private fun isRelated(task:Task, list:List<Relation>):Boolean {
var result = false
for (relation in list)
{
if (relation.targetTask.uniqueID.intValue() === task.uniqueID.intValue())
{
result = true
break
}
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
}
