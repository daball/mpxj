/*
 * file:       MPPTimephasedBaselineCostNormaliser.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2009
 * date:       09/01/2009
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
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.TimephasedCost
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.TimephasedCostNormaliser

/**
 * Common implementation detail for normalisation.
 */
class MPPTimephasedBaselineCostNormaliser : TimephasedCostNormaliser {
    /**
     * This method converts the internal representation of timephased
     * resource assignment data used by MS Project into a standardised
     * format to make it easy to work with.
     *
     * @param calendar current calendar
     * @param list list of assignment data
     */
    @Override
    override fun normalise(calendar: ProjectCalendar, list: LinkedList<TimephasedCost>) {
        if (!list.isEmpty()) {
            //dumpList(list);
            splitDays(calendar, list)
            //dumpList(list);
            mergeSameDay(list)
            //dumpList(list);
            mergeSameCost(list)
            //dumpList(list);
        }
    }

    /**
     * This method breaks down spans of time into individual days.
     *
     * @param calendar current project calendar
     * @param list list of assignment data
     */
    private fun splitDays(calendar: ProjectCalendar, list: LinkedList<TimephasedCost>) {
        val result = LinkedList<TimephasedCost>()
        var remainderInserted = false

        for (assignment in list) {
            if (remainderInserted) {
                assignment!!.setStart(DateHelper.addDays(assignment!!.getStart(), 1))
                remainderInserted = false
            }

            while (assignment != null) {
                val startDay = DateHelper.getDayStartDate(assignment!!.getStart())
                var finishDay = DateHelper.getDayStartDate(assignment!!.getFinish())

                // special case - when the finishday time is midnight, it's really the previous day...
                if (assignment!!.getFinish().getTime() === finishDay!!.getTime()) {
                    finishDay = DateHelper.addDays(finishDay, -1)
                }

                if (startDay!!.getTime() === finishDay!!.getTime()) {
                    result.add(assignment)
                    break
                }

                val split = splitFirstDay(calendar, assignment!!)
                if (split[0] != null) {
                    result.add(split[0])
                }

                if (assignment!!.equals(split[1])) {
                    break
                }

                assignment = split[1]
            }
        }

        list.clear()
        list.addAll(result)
    }

    /**
     * This method splits the first day off of a time span.
     *
     * @param calendar current calendar
     * @param assignment timephased assignment span
     * @return first day and remainder assignments
     */
    private fun splitFirstDay(calendar: ProjectCalendar, assignment: TimephasedCost): Array<TimephasedCost> {
        val result = arrayOfNulls<TimephasedCost>(2)

        //
        // Retrieve data used to calculate the pro-rata work split
        //
        val assignmentStart = assignment.getStart()
        val assignmentFinish = assignment.getFinish()
        val calendarWork = calendar.getWork(assignmentStart, assignmentFinish, TimeUnit.MINUTES)

        if (calendarWork.getDuration() !== 0) {
            //
            // Split the first day
            //
            var splitFinish: Date
            var splitCost: Double
            if (calendar.isWorkingDate(assignmentStart)) {
                val splitFinishTime = calendar.getFinishTime(assignmentStart)
                splitFinish = DateHelper.setTime(assignmentStart, splitFinishTime)
                val calendarSplitWork = calendar.getWork(assignmentStart, splitFinish, TimeUnit.MINUTES)
                splitCost = assignment.getTotalAmount().doubleValue() * calendarSplitWork.getDuration() / calendarWork.getDuration()

                val split = TimephasedCost()
                split.setStart(assignmentStart)
                split.setFinish(splitFinish)
                split.setTotalAmount(Double.valueOf(splitCost))

                result[0] = split
            } else {
                splitFinish = assignmentStart
                splitCost = 0.0
            }

            //
            // Split the remainder
            //
            val splitStart = calendar.getNextWorkStart(splitFinish)
            splitFinish = assignmentFinish
            val split: TimephasedCost?
            if (splitStart.getTime() > splitFinish.getTime()) {
                split = null
            } else {
                splitCost = assignment.getTotalAmount().doubleValue() - splitCost

                split = TimephasedCost()
                split!!.setStart(splitStart)
                split!!.setFinish(splitFinish)
                split!!.setTotalAmount(Double.valueOf(splitCost))
                split!!.setAmountPerDay(assignment.getAmountPerDay())
            }

            result[1] = split
        }
        return result
    }

    /**
     * This method merges together assignment data for the same day.
     *
     * @param list assignment data
     */
    private fun mergeSameDay(list: LinkedList<TimephasedCost>) {
        val result = LinkedList<TimephasedCost>()

        var previousAssignment: TimephasedCost? = null
        for (assignment in list) {
            if (previousAssignment == null) {
                assignment.setAmountPerDay(assignment.getTotalAmount())
                result.add(assignment)
            } else {
                val previousAssignmentStart = previousAssignment!!.getStart()
                val previousAssignmentStartDay = DateHelper.getDayStartDate(previousAssignmentStart)
                val assignmentStart = assignment.getStart()
                val assignmentStartDay = DateHelper.getDayStartDate(assignmentStart)

                if (previousAssignmentStartDay!!.getTime() === assignmentStartDay!!.getTime()) {
                    result.removeLast()

                    var cost = previousAssignment!!.getTotalAmount().doubleValue()
                    cost += assignment.getTotalAmount().doubleValue()

                    val merged = TimephasedCost()
                    merged.setStart(previousAssignment!!.getStart())
                    merged.setFinish(assignment.getFinish())
                    merged.setTotalAmount(Double.valueOf(cost))
                    assignment = merged
                }

                assignment.setAmountPerDay(assignment.getTotalAmount())
                result.add(assignment)
            }

            previousAssignment = assignment
        }

        list.clear()
        list.addAll(result)
    }

    /**
     * This method merges together assignment data for the same cost.
     *
     * @param list assignment data
     */
    protected fun mergeSameCost(list: LinkedList<TimephasedCost>) {
        val result = LinkedList<TimephasedCost>()

        var previousAssignment: TimephasedCost? = null
        for (assignment in list) {
            if (previousAssignment == null) {
                assignment.setAmountPerDay(assignment.getTotalAmount())
                result.add(assignment)
            } else {
                val previousAssignmentCost = previousAssignment!!.getAmountPerDay()
                val assignmentCost = assignment.getTotalAmount()

                if (NumberHelper.equals(previousAssignmentCost.doubleValue(), assignmentCost.doubleValue(), 0.01)) {
                    val assignmentStart = previousAssignment!!.getStart()
                    val assignmentFinish = assignment.getFinish()
                    var total = previousAssignment!!.getTotalAmount().doubleValue()
                    total += assignmentCost.doubleValue()

                    val merged = TimephasedCost()
                    merged.setStart(assignmentStart)
                    merged.setFinish(assignmentFinish)
                    merged.setAmountPerDay(assignmentCost)
                    merged.setTotalAmount(Double.valueOf(total))

                    result.removeLast()
                    assignment = merged
                } else {
                    assignment.setAmountPerDay(assignment.getTotalAmount())
                }
                result.add(assignment)
            }

            previousAssignment = assignment
        }

        list.clear()
        list.addAll(result)
    }

    /*
   private void dumpList(LinkedList<TimephasedCost> list)
   {
      System.out.println();
      for (TimephasedCost assignment : list)
      {
         System.out.println(assignment);
      }
   }
   */

}
