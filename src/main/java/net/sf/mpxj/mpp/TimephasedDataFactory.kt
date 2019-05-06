/*
 * file:       TimephasedDataFactory
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2008
 * date:       25/10/2008
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

package net.sf.mpxj.mpp

import java.util.Date
import java.util.LinkedList

import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.TimephasedCost
import net.sf.mpxj.TimephasedCostContainer
import net.sf.mpxj.TimephasedWork
import net.sf.mpxj.TimephasedWorkContainer
import net.sf.mpxj.common.DefaultTimephasedCostContainer
import net.sf.mpxj.common.DefaultTimephasedWorkContainer
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.TimephasedCostNormaliser
import net.sf.mpxj.common.TimephasedWorkNormaliser

/**
 * This class contains methods to create lists of TimephasedWork
 * and TimephasedCost instances.
 */
internal class TimephasedDataFactory {
    /**
     * Given a block of data representing completed work, this method will
     * retrieve a set of TimephasedWork instances which represent
     * the day by day work carried out for a specific resource assignment.
     *
     * @param calendar calendar on which date calculations are based
     * @param resourceAssignment resource assignment
     * @param data completed work data block
     * @return list of TimephasedWork instances
     */
    fun getCompleteWork(calendar: ProjectCalendar?, resourceAssignment: ResourceAssignment, data: ByteArray?): List<TimephasedWork> {
        val list = LinkedList<TimephasedWork>()

        if (calendar != null && data != null && data.size > 2 && MPPUtility.getShort(data, 0) > 0) {
            val startDate = resourceAssignment.start
            val finishTime = MPPUtility.getInt(data, 24).toDouble()

            val blockCount = MPPUtility.getShort(data, 0)
            var previousCumulativeWork = 0.0
            var previousAssignment: TimephasedWork? = null

            var index = 32
            var currentBlock = 0
            while (currentBlock < blockCount && index + 20 <= data.size) {
                var time = MPPUtility.getInt(data, index + 0).toDouble()

                // If the start of this block is before the start of the assignment, or after the end of the assignment
                // the values don't make sense, so we'll just set the start of this block to be the start of the assignment.
                // This deals with an issue where odd timephased data like this was causing an MPP file to be read
                // extremely slowly.
                if (time < 0 || time > finishTime) {
                    time = 0.0
                } else {
                    time /= 80.0
                }
                val startWork = Duration.getInstance(time, TimeUnit.MINUTES)

                val currentCumulativeWork = MPPUtility.getDouble(data, index + 4).toLong().toDouble()
                var assignmentDuration = currentCumulativeWork - previousCumulativeWork
                previousCumulativeWork = currentCumulativeWork
                assignmentDuration /= 1000.0
                val totalWork = Duration.getInstance(assignmentDuration, TimeUnit.MINUTES)
                time = MPPUtility.getDouble(data, index + 12).toLong().toDouble()
                time /= 125.0
                time *= 6.0
                val workPerDay = Duration.getInstance(time, TimeUnit.MINUTES)

                val start: Date
                if (startWork.getDuration() === 0) {
                    start = startDate
                } else {
                    start = calendar.getDate(startDate, startWork, true)
                }

                val assignment = TimephasedWork()
                assignment.setStart(start)
                assignment.setAmountPerDay(workPerDay)
                assignment.setTotalAmount(totalWork)

                if (previousAssignment != null) {
                    val finish = calendar.getDate(startDate, startWork, false)
                    previousAssignment!!.setFinish(finish)
                    if (previousAssignment!!.getStart().getTime() === previousAssignment!!.getFinish().getTime()) {
                        list.removeLast()
                    }
                }

                list.add(assignment)
                previousAssignment = assignment

                index += 20
                ++currentBlock
            }

            if (previousAssignment != null) {
                val finishWork = Duration.getInstance(finishTime / 80, TimeUnit.MINUTES)
                val finish = calendar.getDate(startDate, finishWork, false)
                previousAssignment!!.setFinish(finish)
                if (previousAssignment!!.getStart().getTime() === previousAssignment!!.getFinish().getTime()) {
                    list.removeLast()
                }
            }
        }

        return list
    }

    /**
     * Given a block of data representing planned work, this method will
     * retrieve a set of TimephasedWork instances which represent
     * the day by day work planned for a specific resource assignment.
     *
     * @param calendar calendar on which date calculations are based
     * @param startDate assignment start date
     * @param units assignment units
     * @param data planned work data block
     * @param timephasedComplete list of complete work
     * @param resourceType resource type
     * @return list of TimephasedWork instances
     */
    fun getPlannedWork(calendar: ProjectCalendar?, startDate: Date, units: Double, data: ByteArray?, timephasedComplete: List<TimephasedWork>, resourceType: ResourceType): List<TimephasedWork> {
        val list = LinkedList<TimephasedWork>()

        if (calendar != null && data != null && data.size > 0) {
            val blockCount = MPPUtility.getShort(data, 0)
            if (blockCount == 0) {
                if (!timephasedComplete.isEmpty() && units != 0.0) {
                    val lastComplete = timephasedComplete[timephasedComplete.size() - 1]

                    val startWork = calendar.getNextWorkStart(lastComplete.getFinish())
                    var time = MPPUtility.getDouble(data, 16)
                    time /= 1000.0
                    val totalWork = Duration.getInstance(time, TimeUnit.MINUTES)
                    val adjustedTotalWork: Duration
                    if (resourceType == ResourceType.WORK) {
                        adjustedTotalWork = Duration.getInstance(time * 100 / units, TimeUnit.MINUTES)
                    } else {
                        adjustedTotalWork = Duration.getInstance(time, TimeUnit.MINUTES)
                    }
                    val finish = calendar.getDate(startWork, adjustedTotalWork, false)

                    time = MPPUtility.getDouble(data, 8)
                    time /= 2000.0
                    time *= 6.0
                    val workPerDay = Duration.getInstance(time, TimeUnit.MINUTES)

                    val assignment = TimephasedWork()
                    assignment.setStart(startWork)
                    assignment.setAmountPerDay(workPerDay)
                    assignment.setModified(false)
                    assignment.setFinish(finish)
                    assignment.setTotalAmount(totalWork)

                    if (assignment.getStart().getTime() !== assignment.getFinish().getTime()) {
                        list.add(assignment)
                    }
                }
            } else {
                var offset = startDate

                if (!timephasedComplete.isEmpty()) {
                    val lastComplete = timephasedComplete[timephasedComplete.size() - 1]
                    offset = lastComplete.getFinish()
                }

                var index = 40
                var previousCumulativeWork = 0.0
                var previousAssignment: TimephasedWork? = null
                var currentBlock = 0
                var previousModifiedFlag = 0

                while (currentBlock < blockCount && index + 28 <= data.size) {
                    var time = MPPUtility.getInt(data, index).toDouble()
                    time /= 80.0
                    val blockDuration = Duration.getInstance(time, TimeUnit.MINUTES)
                    val start: Date
                    if (blockDuration.getDuration() === 0) {
                        start = offset
                    } else {
                        start = calendar.getDate(offset, blockDuration, true)
                    }

                    val currentCumulativeWork = MPPUtility.getDouble(data, index + 4)
                    var assignmentDuration = currentCumulativeWork - previousCumulativeWork
                    assignmentDuration /= 1000.0
                    val totalWork = Duration.getInstance(assignmentDuration, TimeUnit.MINUTES)
                    previousCumulativeWork = currentCumulativeWork

                    time = MPPUtility.getDouble(data, index + 12)
                    time /= 2000.0
                    time *= 6.0
                    val workPerDay = Duration.getInstance(time, TimeUnit.MINUTES)

                    val currentModifiedFlag = MPPUtility.getShort(data, index + 22)
                    val modified = currentBlock > 0 && previousModifiedFlag != 0 && currentModifiedFlag == 0 || currentModifiedFlag and 0x3000 != 0
                    previousModifiedFlag = currentModifiedFlag

                    val assignment = TimephasedWork()
                    assignment.setStart(start)
                    assignment.setAmountPerDay(workPerDay)
                    assignment.setModified(modified)
                    assignment.setTotalAmount(totalWork)

                    if (previousAssignment != null) {
                        val finish = calendar.getDate(offset, blockDuration, false)
                        previousAssignment!!.setFinish(finish)
                        if (previousAssignment!!.getStart().getTime() === previousAssignment!!.getFinish().getTime()) {
                            list.removeLast()
                        }
                    }

                    list.add(assignment)
                    previousAssignment = assignment

                    index += 28
                    ++currentBlock
                }

                if (previousAssignment != null) {
                    var time = MPPUtility.getInt(data, 24).toDouble()
                    time /= 80.0
                    val blockDuration = Duration.getInstance(time, TimeUnit.MINUTES)
                    val finish = calendar.getDate(offset, blockDuration, false)
                    previousAssignment!!.setFinish(finish)
                    if (previousAssignment!!.getStart().getTime() === previousAssignment!!.getFinish().getTime()) {
                        list.removeLast()
                    }
                }
            }
        }

        return list
    }

    /**
     * Test the list of TimephasedWork instances to see
     * if any of them have been modified.
     *
     * @param list list of TimephasedWork instances
     * @return boolean flag
     */
    fun getWorkModified(list: List<TimephasedWork>): Boolean {
        var result = false
        for (assignment in list) {
            result = assignment.getModified()
            if (result) {
                break
            }
        }
        return result
    }

    /**
     * Extracts baseline work from the MPP file for a specific baseline.
     * Returns null if no baseline work is present, otherwise returns
     * a list of timephased work items.
     *
     * @param assignment parent assignment
     * @param calendar baseline calendar
     * @param normaliser normaliser associated with this data
     * @param data timephased baseline work data block
     * @param raw flag indicating if this data is to be treated as raw
     * @return timephased work
     */
    fun getBaselineWork(assignment: ResourceAssignment, calendar: ProjectCalendar, normaliser: TimephasedWorkNormaliser, data: ByteArray?, raw: Boolean): TimephasedWorkContainer? {
        var result: TimephasedWorkContainer? = null

        if (data != null && data.size > 0) {
            var list: LinkedList<TimephasedWork>? = null

            //System.out.println(ByteArrayHelper.hexdump(data, false));
            var index = 8 // 8 byte header
            val blockSize = 40
            var previousCumulativeWorkPerformedInMinutes = 0.0

            var blockStartDate = MPPUtility.getTimestampFromTenths(data, index + 36)
            index += blockSize
            var work: TimephasedWork? = null

            while (index + blockSize <= data.size) {
                val cumulativeWorkInMinutes = MPPUtility.getDouble(data, index + 20).toLong().toDouble() / 1000
                if (!Duration.durationValueEquals(cumulativeWorkInMinutes, previousCumulativeWorkPerformedInMinutes)) {
                    //double unknownWorkThisPeriodInMinutes = ((long) MPPUtility.getDouble(data, index + 0)) / 1000;
                    val normalActualWorkThisPeriodInMinutes = MPPUtility.getInt(data, index + 8).toDouble() / 10
                    val normalRemainingWorkThisPeriodInMinutes = MPPUtility.getInt(data, index + 28).toDouble() / 10
                    val workThisPeriodInMinutes = cumulativeWorkInMinutes - previousCumulativeWorkPerformedInMinutes
                    val overtimeWorkThisPeriodInMinutes = workThisPeriodInMinutes - (normalActualWorkThisPeriodInMinutes + normalRemainingWorkThisPeriodInMinutes)
                    val overtimeFactor = overtimeWorkThisPeriodInMinutes / (normalActualWorkThisPeriodInMinutes + normalRemainingWorkThisPeriodInMinutes)

                    val normalWorkPerDayInMinutes = 480.0
                    val overtimeWorkPerDayInMinutes = normalWorkPerDayInMinutes * overtimeFactor

                    work = TimephasedWork()
                    work!!.setFinish(MPPUtility.getTimestampFromTenths(data, index + 16))
                    work!!.setStart(blockStartDate)
                    work!!.setTotalAmount(Duration.getInstance(workThisPeriodInMinutes, TimeUnit.MINUTES))
                    work!!.setAmountPerDay(Duration.getInstance(normalWorkPerDayInMinutes + overtimeWorkPerDayInMinutes, TimeUnit.MINUTES))

                    previousCumulativeWorkPerformedInMinutes = cumulativeWorkInMinutes

                    if (list == null) {
                        list = LinkedList<TimephasedWork>()
                    }
                    list!!.add(work)
                    //System.out.println(work);
                }
                blockStartDate = MPPUtility.getTimestampFromTenths(data, index + 36)
                index += blockSize
            }

            if (list != null) {
                if (work != null) {
                    work!!.setFinish(assignment.finish)
                }
                result = DefaultTimephasedWorkContainer(calendar, normaliser, list, raw)
            }
        }

        return result
    }

    /**
     * Extracts baseline cost from the MPP file for a specific baseline.
     * Returns null if no baseline cost is present, otherwise returns
     * a list of timephased work items.
     *
     * @param calendar baseline calendar
     * @param normaliser normaliser associated with this data
     * @param data timephased baseline work data block
     * @param raw flag indicating if this data is to be treated as raw
     * @return timephased work
     */
    fun getBaselineCost(calendar: ProjectCalendar, normaliser: TimephasedCostNormaliser, data: ByteArray?, raw: Boolean): TimephasedCostContainer? {
        var result: TimephasedCostContainer? = null

        if (data != null && data.size > 0) {
            var list: LinkedList<TimephasedCost>? = null

            //System.out.println(ByteArrayHelper.hexdump(data, false));
            var index = 16 // 16 byte header
            val blockSize = 20
            var previousTotalCost = 0.0

            var blockStartDate = MPPUtility.getTimestampFromTenths(data, index + 16)
            index += blockSize

            while (index + blockSize <= data.size) {
                val blockEndDate = MPPUtility.getTimestampFromTenths(data, index + 16)
                val currentTotalCost = MPPUtility.getDouble(data, index + 8).toLong().toDouble() / 100
                if (!costEquals(previousTotalCost, currentTotalCost)) {
                    val cost = TimephasedCost()
                    cost.setStart(blockStartDate)
                    cost.setFinish(blockEndDate)
                    cost.setTotalAmount(Double.valueOf(currentTotalCost - previousTotalCost))

                    if (list == null) {
                        list = LinkedList<TimephasedCost>()
                    }
                    list!!.add(cost)
                    //System.out.println(cost);

                    previousTotalCost = currentTotalCost
                }

                blockStartDate = blockEndDate
                index += blockSize
            }

            if (list != null) {
                result = DefaultTimephasedCostContainer(calendar, normaliser, list, raw)
            }
        }

        return result
    }

    /**
     * Equality test cost values.
     *
     * @param lhs cost value
     * @param rhs cost value
     * @return true if costs are equal, within the allowable delta
     */
    private fun costEquals(lhs: Double, rhs: Double): Boolean {
        return NumberHelper.equals(lhs, rhs, 0.00001)
    }

}
