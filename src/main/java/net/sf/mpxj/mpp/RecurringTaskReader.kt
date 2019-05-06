/*
 * file:       RecurringTaskReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2008
 * date:       23/06/2008
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

import net.sf.mpxj.Day
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.RecurrenceType
import net.sf.mpxj.RecurringTask
import net.sf.mpxj.Task

/**
 * This class allows recurring task definitions to be read from an MPP file.
 */
internal class RecurringTaskReader
/**
 * Constructor.
 *
 * @param properties project properties
 */
(private val m_properties: ProjectProperties) {

    /**
     * Reads recurring task data.
     *
     * @param task Task instance
     * @param data recurring task data
     */
    fun processRecurringTask(task: Task, data: ByteArray) {
        val rt = task.addRecurringTask()
        rt.startDate = MPPUtility.getDate(data, 6)
        rt.finishDate = MPPUtility.getDate(data, 10)
        rt.duration = MPPUtility.getAdjustedDuration(m_properties, MPPUtility.getInt(data, 12), MPPUtility.getDurationTimeUnits(MPPUtility.getShort(data, 16)))
        rt.occurrences = Integer.valueOf(MPPUtility.getShort(data, 18))
        rt.recurrenceType = RecurrenceType.getInstance(MPPUtility.getShort(data, 20))
        rt.useEndDate = MPPUtility.getShort(data, 24) == 1
        rt.isWorkingDaysOnly = MPPUtility.getShort(data, 26) == 1
        rt.setWeeklyDay(Day.SUNDAY, MPPUtility.getShort(data, 28) == 1)
        rt.setWeeklyDay(Day.MONDAY, MPPUtility.getShort(data, 30) == 1)
        rt.setWeeklyDay(Day.TUESDAY, MPPUtility.getShort(data, 32) == 1)
        rt.setWeeklyDay(Day.WEDNESDAY, MPPUtility.getShort(data, 34) == 1)
        rt.setWeeklyDay(Day.THURSDAY, MPPUtility.getShort(data, 36) == 1)
        rt.setWeeklyDay(Day.FRIDAY, MPPUtility.getShort(data, 38) == 1)
        rt.setWeeklyDay(Day.SATURDAY, MPPUtility.getShort(data, 40) == 1)

        var frequencyOffset = 0
        var dayOfWeekOffset = 0
        var dayNumberOffset = 0
        var monthNumberOffset = 0
        var dateOffset = 0

        when (rt.recurrenceType) {
            RecurrenceType.DAILY -> {
                frequencyOffset = 46
            }

            RecurrenceType.WEEKLY -> {
                frequencyOffset = 48
            }

            RecurrenceType.MONTHLY -> {
                rt.relative = MPPUtility.getShort(data, 42) == 1
                if (rt.relative) {
                    frequencyOffset = 58
                    dayNumberOffset = 50
                    dayOfWeekOffset = 52
                } else {
                    frequencyOffset = 54
                    dayNumberOffset = 56
                }
            }

            RecurrenceType.YEARLY -> {
                rt.relative = MPPUtility.getShort(data, 44) != 1
                if (rt.relative) {
                    dayNumberOffset = 60
                    dayOfWeekOffset = 62
                    monthNumberOffset = 64
                } else {
                    dateOffset = 70
                }
            }
        }

        if (frequencyOffset != 0) {
            rt.frequency = Integer.valueOf(MPPUtility.getShort(data, frequencyOffset))
        }

        if (dayOfWeekOffset != 0) {
            rt.dayOfWeek = Day.getInstance(MPPUtility.getShort(data, dayOfWeekOffset) + 1)
        }

        if (dayNumberOffset != 0) {
            rt.dayNumber = Integer.valueOf(MPPUtility.getShort(data, dayNumberOffset))
        }

        if (monthNumberOffset != 0) {
            rt.monthNumber = Integer.valueOf(MPPUtility.getShort(data, monthNumberOffset))
        }

        if (dateOffset != 0) {
            rt.setYearlyAbsoluteFromDate(MPPUtility.getDate(data, dateOffset))
        }
    }
}
