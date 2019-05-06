/*
 * file:       TimephasedUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       2011-02-12
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

package net.sf.mpxj.utility

import java.util.ArrayList
import java.util.Calendar
import java.util.Date

import net.sf.mpxj.DateRange
import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.TimephasedCost
import net.sf.mpxj.TimephasedItem
import net.sf.mpxj.TimephasedWork
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.mpp.TimescaleUnits

/**
 * This class contains methods relating to manipulating timephased data.
 */
class TimephasedUtility {
    /**
     * This is the main entry point used to convert the internal representation
     * of timephased work into an external form which can
     * be displayed to the user.
     *
     * @param projectCalendar calendar used by the resource assignment
     * @param work timephased resource assignment data
     * @param rangeUnits timescale units
     * @param dateList timescale date ranges
     * @return list of durations, one per timescale date range
     */
    fun segmentWork(projectCalendar: ProjectCalendar, work: List<TimephasedWork>, rangeUnits: TimescaleUnits, dateList: List<DateRange>): ArrayList<Duration> {
        val result = ArrayList<Duration>(dateList.size())
        var lastStartIndex = 0

        //
        // Iterate through the list of dates range we are interested in.
        // Each date range in this list corresponds to a column
        // shown on the "timescale" view by MS Project
        //
        for (range in dateList) {
            //
            // If the current date range does not intersect with any of the
            // assignment date ranges in the list, then we show a zero
            // duration for this date range.
            //
            val startIndex = if (lastStartIndex == -1) -1 else getStartIndex<TimephasedItem<*>>(range, work, lastStartIndex)
            if (startIndex == -1) {
                result.add(Duration.getInstance(0, TimeUnit.HOURS))
            } else {
                //
                // We have found an assignment which intersects with the current
                // date range, call the method below to determine how
                // much time from this resource assignment can be allocated
                // to the current date range.
                //
                result.add(getRangeDuration(projectCalendar, rangeUnits, range, work, startIndex))
                lastStartIndex = startIndex
            }
        }

        return result
    }

    /**
     * This is the main entry point used to convert the internal representation
     * of timephased baseline work into an external form which can
     * be displayed to the user.
     *
     * @param file parent project file
     * @param work timephased resource assignment data
     * @param rangeUnits timescale units
     * @param dateList timescale date ranges
     * @return list of durations, one per timescale date range
     */
    fun segmentBaselineWork(file: ProjectFile, work: List<TimephasedWork>, rangeUnits: TimescaleUnits, dateList: ArrayList<DateRange>): ArrayList<Duration> {
        return segmentWork(file.baselineCalendar, work, rangeUnits, dateList)
    }

    /**
     * This is the main entry point used to convert the internal representation
     * of timephased cost into an external form which can
     * be displayed to the user.
     *
     * @param projectCalendar calendar used by the resource assignment
     * @param cost timephased resource assignment data
     * @param rangeUnits timescale units
     * @param dateList timescale date ranges
     * @return list of durations, one per timescale date range
     */
    fun segmentCost(projectCalendar: ProjectCalendar, cost: List<TimephasedCost>, rangeUnits: TimescaleUnits, dateList: ArrayList<DateRange>): ArrayList<Double> {
        val result = ArrayList<Double>(dateList.size())
        var lastStartIndex = 0

        //
        // Iterate through the list of dates range we are interested in.
        // Each date range in this list corresponds to a column
        // shown on the "timescale" view by MS Project
        //
        for (range in dateList) {
            //
            // If the current date range does not intersect with any of the
            // assignment date ranges in the list, then we show a zero
            // duration for this date range.
            //
            val startIndex = if (lastStartIndex == -1) -1 else getStartIndex<TimephasedItem<*>>(range, cost, lastStartIndex)
            if (startIndex == -1) {
                result.add(NumberHelper.DOUBLE_ZERO)
            } else {
                //
                // We have found an assignment which intersects with the current
                // date range, call the method below to determine how
                // much time from this resource assignment can be allocated
                // to the current date range.
                //
                result.add(getRangeCost(projectCalendar, rangeUnits, range, cost, startIndex))
                lastStartIndex = startIndex
            }
        }

        return result
    }

    /**
     * This is the main entry point used to convert the internal representation
     * of timephased baseline cost into an external form which can
     * be displayed to the user.
     *
     * @param file parent project file
     * @param cost timephased resource assignment data
     * @param rangeUnits timescale units
     * @param dateList timescale date ranges
     * @return list of durations, one per timescale date range
     */
    fun segmentBaselineCost(file: ProjectFile, cost: List<TimephasedCost>, rangeUnits: TimescaleUnits, dateList: ArrayList<DateRange>): ArrayList<Double> {
        return segmentCost(file.baselineCalendar, cost, rangeUnits, dateList)
    }

    /**
     * Used to locate the first timephased resource assignment block which
     * intersects with the target date range.
     *
     * @param <T> payload type
     * @param range target date range
     * @param assignments timephased resource assignments
     * @param startIndex index at which to start the search
     * @return index of timephased resource assignment which intersects with the target date range
    </T> */
    private fun <T : TimephasedItem<*>> getStartIndex(range: DateRange, assignments: List<T>?, startIndex: Int): Int {
        var result = -1
        if (assignments != null) {
            val rangeStart = range.getStart().getTime()
            val rangeEnd = range.getEnd().getTime()

            for (loop in startIndex until assignments.size()) {
                val assignment = assignments[loop]
                var compareResult = DateHelper.compare(assignment.getStart(), assignment.getFinish(), rangeStart)

                //
                // The start of the target range falls after the assignment end -
                // move on to test the next assignment.
                //
                if (compareResult > 0) {
                    continue
                }

                //
                // The start of the target range  falls within the assignment -
                // return the index of this assignment to the caller.
                //
                if (compareResult == 0) {
                    result = loop
                    break
                }

                //
                // At this point, we know that the start of the target range is before
                // the assignment start. We need to determine if the end of the
                // target range overlaps the assignment.
                //
                compareResult = DateHelper.compare(assignment.getStart(), assignment.getFinish(), rangeEnd)
                if (compareResult >= 0) {
                    result = loop
                    break
                }
            }
        }
        return result
    }

    /**
     * For a given date range, determine the duration of work, based on the
     * timephased resource assignment data.
     *
     * @param projectCalendar calendar used for the resource assignment calendar
     * @param rangeUnits timescale units
     * @param range target date range
     * @param assignments timephased resource assignments
     * @param startIndex index at which to start searching through the timephased resource assignments
     * @return work duration
     */
    private fun getRangeDuration(projectCalendar: ProjectCalendar, rangeUnits: TimescaleUnits, range: DateRange, assignments: List<TimephasedWork>, startIndex: Int): Duration {
        val result: Duration

        when (rangeUnits) {
            TimescaleUnits.MINUTES, TimescaleUnits.HOURS -> {
                result = getRangeDurationSubDay(projectCalendar, rangeUnits, range, assignments, startIndex)
            }

            else -> {
                result = getRangeDurationWholeDay(projectCalendar, rangeUnits, range, assignments, startIndex)
            }
        }

        return result
    }

    /**
     * For a given date range, determine the duration of work, based on the
     * timephased resource assignment data.
     *
     * This method deals with timescale units of less than a day.
     *
     * @param projectCalendar calendar used for the resource assignment calendar
     * @param rangeUnits timescale units
     * @param range target date range
     * @param assignments timephased resource assignments
     * @param startIndex index at which to start searching through the timephased resource assignments
     * @return work duration
     */
    private fun getRangeDurationSubDay(projectCalendar: ProjectCalendar, rangeUnits: TimescaleUnits, range: DateRange, assignments: List<TimephasedWork>, startIndex: Int): Duration {
        throw UnsupportedOperationException("Please request this functionality from the MPXJ maintainer")
    }

    /**
     * For a given date range, determine the duration of work, based on the
     * timephased resource assignment data.
     *
     * This method deals with timescale units of one day or more.
     *
     * @param projectCalendar calendar used for the resource assignment calendar
     * @param rangeUnits timescale units
     * @param range target date range
     * @param assignments timephased resource assignments
     * @param startIndex index at which to start searching through the timephased resource assignments
     * @return work duration
     */
    private fun getRangeDurationWholeDay(projectCalendar: ProjectCalendar?, rangeUnits: TimescaleUnits, range: DateRange, assignments: List<TimephasedWork>, startIndex: Int): Duration {
        var startIndex = startIndex
        // option 1:
        // Our date range starts before the start of the TRA at the start index.
        // We can guarantee that we don't need to look at any earlier TRA blocks so just start here

        // option 2:
        // Our date range starts at the same point as the first TRA: do nothing...

        // option 3:
        // Our date range starts somewhere inside the first TRA...

        // if it's option 1 just set the start date to the start of the TRA block
        // for everything else we just use the start date of our date range.
        // start counting forwards one day at a time until we reach the end of
        // the date range, or until we reach the end of the block.

        // if we have not reached the end of the range, move to the next block and
        // see if the date range overlaps it. if it does not overlap, then we're
        // done.

        // if it does overlap, then move to the next block and repeat

        var totalDays = 0
        var totalWork = 0.0
        var assignment = assignments[startIndex]
        var done = false

        do {
            //
            // Select the correct start date
            //
            var startDate = range.getStart().getTime()
            val assignmentStart = assignment.getStart().getTime()
            if (startDate < assignmentStart) {
                startDate = assignmentStart
            }

            val rangeEndDate = range.getEnd().getTime()
            val traEndDate = assignment.getFinish().getTime()

            val cal = DateHelper.popCalendar(startDate)
            var calendarDate = cal.getTime()

            //
            // Start counting forwards
            //
            while (startDate < rangeEndDate && startDate < traEndDate) {
                if (projectCalendar == null || projectCalendar.isWorkingDate(calendarDate)) {
                    ++totalDays
                }
                cal.add(Calendar.DAY_OF_YEAR, 1)
                startDate = cal.getTimeInMillis()
                calendarDate = cal.getTime()
            }

            DateHelper.pushCalendar(cal)

            //
            // If we still haven't reached the end of our range
            // check to see if the next TRA can be used.
            //
            done = true
            totalWork += assignment.getAmountPerDay().getDuration() * totalDays
            if (startDate < rangeEndDate) {
                ++startIndex
                if (startIndex < assignments.size()) {
                    assignment = assignments[startIndex]
                    totalDays = 0
                    done = false
                }
            }
        } while (!done)

        return Duration.getInstance(totalWork, assignment.getAmountPerDay().getUnits())
    }

    /**
     * For a given date range, determine the cost, based on the
     * timephased resource assignment data.
     *
     * @param projectCalendar calendar used for the resource assignment calendar
     * @param rangeUnits timescale units
     * @param range target date range
     * @param assignments timephased resource assignments
     * @param startIndex index at which to start searching through the timephased resource assignments
     * @return work duration
     */
    private fun getRangeCost(projectCalendar: ProjectCalendar, rangeUnits: TimescaleUnits, range: DateRange, assignments: List<TimephasedCost>, startIndex: Int): Double {
        val result: Double

        when (rangeUnits) {
            TimescaleUnits.MINUTES, TimescaleUnits.HOURS -> {
                result = getRangeCostSubDay(projectCalendar, rangeUnits, range, assignments, startIndex)
            }

            else -> {
                result = getRangeCostWholeDay(projectCalendar, rangeUnits, range, assignments, startIndex)
            }
        }

        return result
    }

    /**
     * For a given date range, determine the cost, based on the
     * timephased resource assignment data.
     *
     * This method deals with timescale units of one day or more.
     *
     * @param projectCalendar calendar used for the resource assignment calendar
     * @param rangeUnits timescale units
     * @param range target date range
     * @param assignments timephased resource assignments
     * @param startIndex index at which to start searching through the timephased resource assignments
     * @return work duration
     */
    private fun getRangeCostWholeDay(projectCalendar: ProjectCalendar?, rangeUnits: TimescaleUnits, range: DateRange, assignments: List<TimephasedCost>, startIndex: Int): Double {
        var startIndex = startIndex
        var totalDays = 0
        var totalCost = 0.0
        var assignment = assignments[startIndex]
        var done = false

        do {
            //
            // Select the correct start date
            //
            var startDate = range.getStart().getTime()
            val assignmentStart = assignment.getStart().getTime()
            if (startDate < assignmentStart) {
                startDate = assignmentStart
            }

            val rangeEndDate = range.getEnd().getTime()
            val traEndDate = assignment.getFinish().getTime()

            val cal = DateHelper.popCalendar(startDate)
            var calendarDate = cal.getTime()

            //
            // Start counting forwards
            //
            while (startDate < rangeEndDate && startDate < traEndDate) {
                if (projectCalendar == null || projectCalendar.isWorkingDate(calendarDate)) {
                    ++totalDays
                }
                cal.add(Calendar.DAY_OF_YEAR, 1)
                startDate = cal.getTimeInMillis()
                calendarDate = cal.getTime()
            }
            DateHelper.pushCalendar(cal)

            //
            // If we still haven't reached the end of our range
            // check to see if the next TRA can be used.
            //
            done = true
            totalCost += assignment.getAmountPerDay().doubleValue() * totalDays
            if (startDate < rangeEndDate) {
                ++startIndex
                if (startIndex < assignments.size()) {
                    assignment = assignments[startIndex]
                    totalDays = 0
                    done = false
                }
            }
        } while (!done)

        return Double.valueOf(totalCost)
    }

    /**
     * For a given date range, determine the cost, based on the
     * timephased resource assignment data.
     *
     * This method deals with timescale units of less than a day.
     *
     * @param projectCalendar calendar used for the resource assignment calendar
     * @param rangeUnits timescale units
     * @param range target date range
     * @param assignments timephased resource assignments
     * @param startIndex index at which to start searching through the timephased resource assignments
     * @return work duration
     */
    private fun getRangeCostSubDay(projectCalendar: ProjectCalendar, rangeUnits: TimescaleUnits, range: DateRange, assignments: List<TimephasedCost>, startIndex: Int): Double {
        throw UnsupportedOperationException("Please request this functionality from the MPXJ maintainer")
    }
}
