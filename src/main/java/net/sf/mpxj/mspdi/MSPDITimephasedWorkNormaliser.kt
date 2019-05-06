/*
 * file:       MspdiTimephasedWorkNormaliser.java
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

package net.sf.mpxj.mspdi

import java.util.Date
import java.util.LinkedList

import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.TimephasedWork
import net.sf.mpxj.common.AbstractTimephasedWorkNormaliser
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper

/**
 * Normalise timephased resource assignment data from an MSPDI file.
 */
class MSPDITimephasedWorkNormaliser : AbstractTimephasedWorkNormaliser() {

    /**
     * This method converts the internal representation of timephased
     * resource assignment data used by MS Project into a standardised
     * format to make it easy to work with.
     *
     * @param calendar current calendar
     * @param list list of assignment data
     */
    @Override
    override fun normalise(calendar: ProjectCalendar, list: LinkedList<TimephasedWork>) {
        //dumpList("raw", result);
        splitDays(calendar, list)
        //dumpList("split days", result);
        mergeSameDay(calendar, list)
        //dumpList("mergeSameDay", result);
        mergeSameWork(list)
        //dumpList("mergeSameWork", result);
        validateSameDay(calendar, list)
        convertToHours(list)
    }

    /*
      private void dumpList(String label, LinkedList<TimephasedWork> list)
      {
         System.out.println(label);
         for (TimephasedWork assignment : list)
         {
            System.out.println(assignment);
         }
      }
      */

    /**
     * This method breaks down spans of time into individual days.
     *
     * @param calendar current project calendar
     * @param list list of assignment data
     */
    private fun splitDays(calendar: ProjectCalendar, list: LinkedList<TimephasedWork>) {
        val result = LinkedList<TimephasedWork>()
        for (assignment in list) {
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
    private fun splitFirstDay(calendar: ProjectCalendar, assignment: TimephasedWork): Array<TimephasedWork> {
        val result = arrayOfNulls<TimephasedWork>(2)

        //
        // Retrieve data used to calculate the pro-rata work split
        //
        val assignmentStart = assignment.getStart()
        val assignmentFinish = assignment.getFinish()
        val calendarWork = calendar.getWork(assignmentStart, assignmentFinish, TimeUnit.MINUTES)
        val assignmentWork = assignment.getTotalAmount()

        if (calendarWork.getDuration() !== 0) {
            //
            // Split the first day
            //
            var splitFinish: Date
            var splitMinutes: Double
            if (calendar.isWorkingDate(assignmentStart)) {
                val splitFinishTime = calendar.getFinishTime(assignmentStart)
                splitFinish = DateHelper.setTime(assignmentStart, splitFinishTime)
                splitMinutes = calendar.getWork(assignmentStart, splitFinish, TimeUnit.MINUTES).getDuration()

                splitMinutes *= assignmentWork.getDuration()
                splitMinutes /= calendarWork.getDuration()
                splitMinutes = NumberHelper.round(splitMinutes, 2.0)

                val splitWork = Duration.getInstance(splitMinutes, TimeUnit.MINUTES)

                val split = TimephasedWork()
                split.setStart(assignmentStart)
                split.setFinish(splitFinish)
                split.setTotalAmount(splitWork)

                result[0] = split
            } else {
                splitFinish = assignmentStart
                splitMinutes = 0.0
            }

            //
            // Split the remainder
            //
            val splitStart = calendar.getNextWorkStart(splitFinish)
            splitFinish = assignmentFinish
            val split: TimephasedWork?
            if (splitStart.getTime() > splitFinish.getTime()) {
                split = null
            } else {
                splitMinutes = assignmentWork.getDuration() - splitMinutes
                val splitWork = Duration.getInstance(splitMinutes, TimeUnit.MINUTES)

                split = TimephasedWork()
                split!!.setStart(splitStart)
                split!!.setFinish(splitFinish)
                split!!.setTotalAmount(splitWork)
            }

            result[1] = split
        }
        return result
    }

    /**
     * This method merges together assignment data for the same day.
     *
     * @param calendar current calendar
     * @param list assignment data
     */
    private fun mergeSameDay(calendar: ProjectCalendar, list: LinkedList<TimephasedWork>) {
        val result = LinkedList<TimephasedWork>()

        var previousAssignment: TimephasedWork? = null
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
                    val previousAssignmentWork = previousAssignment!!.getTotalAmount()
                    val assignmentWork = assignment.getTotalAmount()

                    if (previousAssignmentWork.getDuration() !== 0 && assignmentWork.getDuration() === 0) {
                        continue
                    }

                    result.removeLast()

                    if (previousAssignmentWork.getDuration() !== 0 && assignmentWork.getDuration() !== 0) {
                        var work = previousAssignment!!.getTotalAmount().getDuration()
                        work += assignment.getTotalAmount().getDuration()
                        val totalWork = Duration.getInstance(work, TimeUnit.MINUTES)

                        val merged = TimephasedWork()
                        merged.setStart(previousAssignment!!.getStart())
                        merged.setFinish(assignment.getFinish())
                        merged.setTotalAmount(totalWork)
                        assignment = merged
                    } else {
                        if (assignmentWork.getDuration() === 0) {
                            assignment = previousAssignment
                        }
                    }
                }

                assignment.setAmountPerDay(assignment.getTotalAmount())
                result.add(assignment)
            }

            val calendarWork = calendar.getWork(assignment.getStart(), assignment.getFinish(), TimeUnit.MINUTES)
            val assignmentWork = assignment.getTotalAmount()
            if (calendarWork.getDuration() === 0 && assignmentWork.getDuration() === 0) {
                result.removeLast()
            } else {
                previousAssignment = assignment
            }
        }

        list.clear()
        list.addAll(result)
    }

    /**
     * Ensures that the start and end dates for ranges fit within the
     * working times for a given day.
     *
     * @param calendar current calendar
     * @param list assignment data
     */
    private fun validateSameDay(calendar: ProjectCalendar, list: LinkedList<TimephasedWork>) {
        for (assignment in list) {
            var assignmentStart = assignment.getStart()
            val calendarStartTime = calendar.getStartTime(assignmentStart)
            val assignmentStartTime = DateHelper.getCanonicalTime(assignmentStart)
            var assignmentFinish = assignment.getFinish()
            val calendarFinishTime = calendar.getFinishTime(assignmentFinish)
            val assignmentFinishTime = DateHelper.getCanonicalTime(assignmentFinish)
            val totalWork = assignment.getTotalAmount().getDuration()

            if (assignmentStartTime != null && calendarStartTime != null) {
                if (totalWork == 0.0 && assignmentStartTime.getTime() !== calendarStartTime!!.getTime() || assignmentStartTime.getTime() < calendarStartTime.getTime()) {
                    assignmentStart = DateHelper.setTime(assignmentStart, calendarStartTime)
                    assignment.setStart(assignmentStart)
                }
            }

            if (assignmentFinishTime != null && calendarFinishTime != null) {
                if (totalWork == 0.0 && assignmentFinishTime.getTime() !== calendarFinishTime!!.getTime() || assignmentFinishTime.getTime() > calendarFinishTime.getTime()) {
                    assignmentFinish = DateHelper.setTime(assignmentFinish, calendarFinishTime)
                    assignment.setFinish(assignmentFinish)
                }
            }
        }
    }
}
