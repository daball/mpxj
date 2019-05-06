/*
 * file:       MppAbstractTimephasedWorkNormaliser.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       02/12/2011
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
import net.sf.mpxj.TimephasedWork
import net.sf.mpxj.common.AbstractTimephasedWorkNormaliser
import net.sf.mpxj.common.DateHelper

/**
 * Normalise timephased resource assignment data from an MPP file.
 */
abstract class MPPAbstractTimephasedWorkNormaliser : AbstractTimephasedWorkNormaliser() {

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
        if (!list.isEmpty()) {
            //dumpList(list);
            splitDays(calendar, list)
            //dumpList(list);
            mergeSameDay(calendar, list)
            //dumpList(list);
            mergeSameWork(list)
            //dumpList(list);
            convertToHours(list)
            //dumpList(list);
        }
    }

    /**
     * This method breaks down spans of time into individual days.
     *
     * @param calendar current project calendar
     * @param list list of assignment data
     */
    private fun splitDays(calendar: ProjectCalendar, list: LinkedList<TimephasedWork>) {
        val result = LinkedList<TimephasedWork>()
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
                    val totalWork = assignment!!.getTotalAmount()
                    val assignmentWork = getAssignmentWork(calendar, assignment!!)
                    if (totalWork.getDuration() - assignmentWork.getDuration() > EQUALITY_DELTA) {
                        assignment!!.setTotalAmount(assignmentWork)
                        result.add(assignment)
                        val remainingWork = Duration.getInstance(totalWork.getDuration() - assignmentWork.getDuration(), TimeUnit.MINUTES)

                        val remainderStart = DateHelper.addDays(finishDay, 1)
                        val remainderFinish = DateHelper.addDays(remainderStart, 1)

                        val remainder = TimephasedWork()
                        remainder.setStart(remainderStart)
                        remainder.setFinish(remainderFinish)
                        remainder.setTotalAmount(remainingWork)
                        result.add(remainder)

                        remainderInserted = true
                    } else {
                        result.add(assignment)
                    }
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

                val calendarSplitWork = calendar.getWork(assignmentStart, splitFinish, TimeUnit.MINUTES)
                val calendarWorkPerDay = calendar.getWork(assignmentStart, TimeUnit.MINUTES)
                val assignmentWorkPerDay = assignment.getAmountPerDay()
                var splitWork: Duration

                if (calendarSplitWork.durationComponentEquals(calendarWorkPerDay)) {
                    run {
                        if (calendarSplitWork.durationComponentEquals(assignmentWorkPerDay)) {
                            splitWork = assignmentWorkPerDay
                            splitMinutes = splitWork.getDuration()
                        } else {
                            splitMinutes = assignmentWorkPerDay.getDuration()
                            splitMinutes *= calendarSplitWork.getDuration()
                            splitMinutes /= (8 * 60).toDouble() // this appears to be a fixed value
                            splitWork = Duration.getInstance(splitMinutes, TimeUnit.MINUTES)
                        }
                    }
                } else {
                    splitMinutes = assignmentWorkPerDay.getDuration()
                    splitMinutes *= calendarSplitWork.getDuration()
                    splitMinutes /= (8 * 60).toDouble() // this appears to be a fixed value
                    splitWork = Duration.getInstance(splitMinutes, TimeUnit.MINUTES)
                }

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
                split!!.setAmountPerDay(assignment.getAmountPerDay())
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
    protected abstract fun mergeSameDay(calendar: ProjectCalendar, list: LinkedList<TimephasedWork>)

    /**
     * Retrieves the pro-rata work carried out on a given day.
     *
     * @param calendar current calendar
     * @param assignment current assignment.
     * @return assignment work duration
     */
    private fun getAssignmentWork(calendar: ProjectCalendar, assignment: TimephasedWork): Duration {
        val assignmentStart = assignment.getStart()

        val splitFinishTime = calendar.getFinishTime(assignmentStart)
        val splitFinish = DateHelper.setTime(assignmentStart, splitFinishTime)

        val calendarSplitWork = calendar.getWork(assignmentStart, splitFinish, TimeUnit.MINUTES)
        val assignmentWorkPerDay = assignment.getAmountPerDay()
        val splitWork: Duration

        var splitMinutes = assignmentWorkPerDay.getDuration()
        splitMinutes *= calendarSplitWork.getDuration()
        splitMinutes /= (8 * 60).toDouble() // this appears to be a fixed value
        splitWork = Duration.getInstance(splitMinutes, TimeUnit.MINUTES)
        return splitWork
    }

    companion object {

        /*
      private void dumpList(LinkedList<TimephasedWork> list)
      {
         System.out.println();
         for (TimephasedWork assignment : list)
         {
            System.out.println(assignment);
         }
      }
   */

        private val EQUALITY_DELTA = 0.1
    }
}
