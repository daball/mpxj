/*
 * file:       SplitTaskFactory
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2008
 * date:       25/11/2008
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

package net.sf.mpxj.common

import java.util.Date
import java.util.LinkedList

import net.sf.mpxj.DateRange
import net.sf.mpxj.Task
import net.sf.mpxj.TimephasedWork

/**
 * This class contains methods to create lists of Dates representing
 * task splits.
 */
class SplitTaskFactory {
    /**
     * Process the timephased resource assignment data to work out the
     * split structure of the task.
     *
     * @param task parent task
     * @param timephasedComplete completed resource assignment work
     * @param timephasedPlanned planned resource assignment work
     */
    fun processSplitData(task: Task, timephasedComplete: List<TimephasedWork>, timephasedPlanned: List<TimephasedWork>) {
        var splitsComplete: Date? = null
        var lastComplete: TimephasedWork? = null
        var firstPlanned: TimephasedWork? = null
        if (!timephasedComplete.isEmpty()) {
            lastComplete = timephasedComplete[timephasedComplete.size() - 1]
            splitsComplete = lastComplete!!.getFinish()
        }

        if (!timephasedPlanned.isEmpty()) {
            firstPlanned = timephasedPlanned[0]
        }

        val splits = LinkedList<DateRange>()
        var lastAssignment: TimephasedWork? = null
        var lastRange: DateRange? = null
        for (assignment in timephasedComplete) {
            if (lastAssignment != null && lastRange != null && lastAssignment!!.getTotalAmount().getDuration() !== 0 && assignment.getTotalAmount().getDuration() !== 0) {
                splits.removeLast()
                lastRange = DateRange(lastRange!!.getStart(), assignment.getFinish())
            } else {
                lastRange = DateRange(assignment.getStart(), assignment.getFinish())
            }
            splits.add(lastRange)
            lastAssignment = assignment
        }

        //
        // We may not have a split, we may just have a partially
        // complete split.
        //
        var splitStart: Date? = null
        if (lastComplete != null && firstPlanned != null && lastComplete!!.getTotalAmount().getDuration() !== 0 && firstPlanned!!.getTotalAmount().getDuration() !== 0) {
            lastRange = splits.removeLast()
            splitStart = lastRange!!.getStart()
        }

        lastAssignment = null
        lastRange = null
        for (assignment in timephasedPlanned) {
            if (splitStart == null) {
                if (lastAssignment != null && lastRange != null && lastAssignment!!.getTotalAmount().getDuration() !== 0 && assignment.getTotalAmount().getDuration() !== 0) {
                    splits.removeLast()
                    lastRange = DateRange(lastRange!!.getStart(), assignment.getFinish())
                } else {
                    lastRange = DateRange(assignment.getStart(), assignment.getFinish())
                }
            } else {
                lastRange = DateRange(splitStart, assignment.getFinish())
            }
            splits.add(lastRange)
            splitStart = null
            lastAssignment = assignment
        }

        //
        // We must have a minimum of 3 entries for this to be a valid split task
        //
        if (splits.size() > 2) {
            task.splits!!.addAll(splits)
            task.splitCompleteDuration = splitsComplete
        } else {
            task.splits = null
            task.splitCompleteDuration = null
        }
    }

}
