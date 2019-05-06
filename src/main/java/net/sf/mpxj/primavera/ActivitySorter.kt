/*
 * file:       ActivitySorter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       06/06/2018
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

package net.sf.mpxj.primavera

import java.util.Collections
import java.util.Comparator

import net.sf.mpxj.ChildTaskContainer
import net.sf.mpxj.FieldType
import net.sf.mpxj.Task

/**
 * Ensures correct activity order within.
 */
internal class ActivitySorter
/**
 * Constructor.
 *
 * @param activityIDField field containing the Activity ID attribute
 * @param wbsTasks set of WBS tasks
 */
(val m_activityIDField: FieldType, val m_wbsTasks: Set<Task>) {

    /**
     * Recursively sort the supplied child tasks.
     *
     * @param container child tasks
     */
    fun sort(container: ChildTaskContainer) {
        // Do we have any tasks?
        val tasks = container.getChildTasks()
        if (!tasks.isEmpty()) {
            for (task in tasks) {
                //
                // Sort child activities
                //
                sort(task)

                //
                // Sort Order:
                // 1. Activities come first
                // 2. WBS come last
                // 3. Activities ordered by activity ID
                // 4. WBS ordered by ID
                //
                Collections.sort(tasks, object : Comparator<Task>() {
                    @Override
                    fun compare(t1: Task, t2: Task): Int {
                        val t1IsWbs = m_wbsTasks.contains(t1)
                        val t2IsWbs = m_wbsTasks.contains(t2)

                        // Both are WBS
                        if (t1IsWbs && t2IsWbs) {
                            return t1.id!!.compareTo(t2.id)
                        }

                        // Both are activities
                        if (!t1IsWbs && !t2IsWbs) {
                            val activityID1 = t1.getCurrentValue(m_activityIDField) as String
                            val activityID2 = t2.getCurrentValue(m_activityIDField) as String

                            return if (activityID1 == null || activityID2 == null) {
                                if (activityID1 == null && activityID2 == null) 0 else if (activityID1 == null) 1 else -1
                            } else activityID1.compareTo(activityID2)

                        }

                        // One activity one WBS
                        return if (t1IsWbs) 1 else -1
                    }
                })
            }
        }
    }
}
