/*
 * file:       MpxjQuery.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       13/02/2003
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

package net.sf.mpxj.sample

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

import net.sf.mpxj.DateRange
import net.sf.mpxj.Duration
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.Task
import net.sf.mpxj.mpp.TimescaleUnits
import net.sf.mpxj.reader.UniversalProjectReader
import net.sf.mpxj.utility.TimephasedUtility
import net.sf.mpxj.utility.TimescaleUtility

/**
 * This example shows an MPP, MPX or MSPDI file being read, and basic
 * task and resource data being extracted.
 */
object MpxjQuery {
    /**
     * Main method.
     *
     * @param args array of command line arguments
     */
    fun main(args: Array<String>) {
        try {
            if (args.size != 1) {
                System.out.println("Usage: MpxQuery <input file name>")
            } else {
                query(args[0])
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.out)
        }

    }

    /**
     * This method performs a set of queries to retrieve information
     * from the an MPP or an MPX file.
     *
     * @param filename name of the MPX file
     * @throws Exception on file read error
     */
    @Throws(Exception::class)
    private fun query(filename: String) {
        val mpx = UniversalProjectReader().read(filename)

        listProjectProperties(mpx!!)

        listResources(mpx!!)

        listTasks(mpx!!)

        listAssignments(mpx!!)

        listAssignmentsByTask(mpx!!)

        listAssignmentsByResource(mpx!!)

        listHierarchy(mpx!!)

        listTaskNotes(mpx!!)

        listResourceNotes(mpx!!)

        listRelationships(mpx!!)

        listSlack(mpx!!)

        listCalendars(mpx!!)

    }

    /**
     * Reads basic summary details from the project properties.
     *
     * @param file MPX file
     */
    private fun listProjectProperties(file: ProjectFile) {
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm z")
        val properties = file.projectProperties
        val startDate = properties.startDate
        val finishDate = properties.finishDate
        val formattedStartDate = if (startDate == null) "(none)" else df.format(startDate)
        val formattedFinishDate = if (finishDate == null) "(none)" else df.format(finishDate)

        System.out.println("MPP file type: " + properties.mppFileType)
        System.out.println("Project Properties: StartDate=$formattedStartDate FinishDate=$formattedFinishDate")
        System.out.println()
    }

    /**
     * This method lists all resources defined in the file.
     *
     * @param file MPX file
     */
    private fun listResources(file: ProjectFile) {
        for (resource in file.resources) {
            System.out.println("Resource: " + resource.name + " (Unique ID=" + resource.uniqueID + ") Start=" + resource.start + " Finish=" + resource.finish)
        }
        System.out.println()
    }

    /**
     * This method lists all tasks defined in the file.
     *
     * @param file MPX file
     */
    private fun listTasks(file: ProjectFile) {
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm z")

        for (task in file.tasks) {
            var date: Date? = task.start
            var text = task.startText
            val startDate = if (text != null) text else if (date != null) df.format(date) else "(no start date supplied)"

            date = task.finish
            text = task.finishText
            val finishDate = if (text != null) text else if (date != null) df.format(date) else "(no finish date supplied)"

            var dur = task.duration
            text = task.durationText
            val duration = if (text != null) text else if (dur != null) dur!!.toString() else "(no duration supplied)"

            dur = task.actualDuration
            val actualDuration = if (dur != null) dur!!.toString() else "(no actual duration supplied)"

            var baselineDuration: String? = task.baselineDurationText
            if (baselineDuration == null) {
                dur = task.baselineDuration
                if (dur != null) {
                    baselineDuration = dur!!.toString()
                } else {
                    baselineDuration = "(no duration supplied)"
                }
            }

            System.out.println("Task: " + task.name + " ID=" + task.id + " Unique ID=" + task.uniqueID + " (Start Date=" + startDate + " Finish Date=" + finishDate + " Duration=" + duration + " Actual Duration" + actualDuration + " Baseline Duration=" + baselineDuration + " Outline Level=" + task.outlineLevel + " Outline Number=" + task.outlineNumber + " Recurring=" + task.recurring + ")")
        }
        System.out.println()
    }

    /**
     * This method lists all tasks defined in the file in a hierarchical
     * format, reflecting the parent-child relationships between them.
     *
     * @param file MPX file
     */
    private fun listHierarchy(file: ProjectFile) {
        for (task in file.childTasks) {
            System.out.println("Task: " + task.name + "\t" + task.start + "\t" + task.finish)
            listHierarchy(task, " ")
        }

        System.out.println()
    }

    /**
     * Helper method called recursively to list child tasks.
     *
     * @param task task whose children are to be displayed
     * @param indent whitespace used to indent hierarchy levels
     */
    private fun listHierarchy(task: Task, indent: String) {
        for (child in task.childTasks) {
            System.out.println(indent + "Task: " + child.name + "\t" + child.start + "\t" + child.finish)
            listHierarchy(child, "$indent ")
        }
    }

    /**
     * This method lists all resource assignments defined in the file.
     *
     * @param file MPX file
     */
    private fun listAssignments(file: ProjectFile) {
        var task: Task?
        var resource: Resource?
        var taskName: String
        var resourceName: String

        for (assignment in file.resourceAssignments) {
            task = assignment.task
            if (task == null) {
                taskName = "(null task)"
            } else {
                taskName = task!!.name
            }

            resource = assignment.resource
            if (resource == null) {
                resourceName = "(null resource)"
            } else {
                resourceName = resource!!.name
            }

            System.out.println("Assignment: Task=$taskName Resource=$resourceName")
            if (task != null) {
                listTimephasedWork(assignment)
            }
        }

        System.out.println()
    }

    /**
     * Dump timephased work for an assignment.
     *
     * @param assignment resource assignment
     */
    private fun listTimephasedWork(assignment: ResourceAssignment) {
        val task = assignment.task
        val days = ((task!!.finish.getTime() - task.start.getTime()) / (1000 * 60 * 60 * 24)) as Int + 1
        if (days > 1) {
            val df = SimpleDateFormat("dd/MM/yy")

            val timescale = TimescaleUtility()
            val dates = timescale.createTimescale(task.start, TimescaleUnits.DAYS, days)
            val timephased = TimephasedUtility()

            val durations = timephased.segmentWork(assignment.calendar, assignment.timephasedWork, TimescaleUnits.DAYS, dates)
            for (range in dates) {
                System.out.print(df.format(range.getStart()) + "\t")
            }
            System.out.println()
            for (duration in durations) {
                System.out.print(duration.toString() + "        ".substring(0, 7) + "\t")
            }
            System.out.println()
        }
    }

    /**
     * This method displays the resource assignments for each task. This time
     * rather than just iterating through the list of all assignments in
     * the file, we extract the assignments on a task-by-task basis.
     *
     * @param file MPX file
     */
    private fun listAssignmentsByTask(file: ProjectFile) {
        for (task in file.tasks) {
            System.out.println("Assignments for task " + task.name + ":")

            for (assignment in task.resourceAssignments) {
                val resource = assignment.resource
                val resourceName: String

                if (resource == null) {
                    resourceName = "(null resource)"
                } else {
                    resourceName = resource!!.name
                }

                System.out.println("   $resourceName")
            }
        }

        System.out.println()
    }

    /**
     * This method displays the resource assignments for each resource. This time
     * rather than just iterating through the list of all assignments in
     * the file, we extract the assignments on a resource-by-resource basis.
     *
     * @param file MPX file
     */
    private fun listAssignmentsByResource(file: ProjectFile) {
        for (resource in file.resources) {
            System.out.println("Assignments for resource " + resource.name + ":")

            for (assignment in resource.taskAssignments) {
                val task = assignment.task
                System.out.println("   " + task!!.name)
            }
        }

        System.out.println()
    }

    /**
     * This method lists any notes attached to tasks.
     *
     * @param file MPX file
     */
    private fun listTaskNotes(file: ProjectFile) {
        for (task in file.tasks) {
            val notes = task.notes

            if (notes.length() !== 0) {
                System.out.println("Notes for " + task.name + ": " + notes)
            }
        }

        System.out.println()
    }

    /**
     * This method lists any notes attached to resources.
     *
     * @param file MPX file
     */
    private fun listResourceNotes(file: ProjectFile) {
        for (resource in file.resources) {
            val notes = resource.notes

            if (notes.length() !== 0) {
                System.out.println("Notes for " + resource.name + ": " + notes)
            }
        }

        System.out.println()
    }

    /**
     * This method lists task predecessor and successor relationships.
     *
     * @param file project file
     */
    private fun listRelationships(file: ProjectFile) {
        for (task in file.tasks) {
            System.out.print(task.id)
            System.out.print('\t')
            System.out.print(task.name)
            System.out.print('\t')

            dumpRelationList(task.predecessors)
            System.out.print('\t')
            dumpRelationList(task.successors)
            System.out.println()
        }
    }

    /**
     * Internal utility to dump relationship lists in a structured format
     * that can easily be compared with the tabular data in MS Project.
     *
     * @param relations relation list
     */
    private fun dumpRelationList(relations: List<Relation>?) {
        if (relations != null && relations.isEmpty() === false) {
            if (relations.size() > 1) {
                System.out.print('"')
            }
            var first = true
            for (relation in relations) {
                if (!first) {
                    System.out.print(',')
                }
                first = false
                System.out.print(relation.targetTask.id)
                val lag = relation.lag
                if (relation.type != RelationType.FINISH_START || lag!!.getDuration() !== 0) {
                    System.out.print(relation.type)
                }

                if (lag!!.getDuration() !== 0) {
                    if (lag!!.getDuration() > 0) {
                        System.out.print("+")
                    }
                    System.out.print(lag)
                }
            }
            if (relations.size() > 1) {
                System.out.print('"')
            }
        }
    }

    /**
     * List the slack values for each task.
     *
     * @param file ProjectFile instance
     */
    private fun listSlack(file: ProjectFile) {
        for (task in file.tasks) {
            System.out.println(task.name + " Total Slack=" + task.totalSlack + " Start Slack=" + task.startSlack + " Finish Slack=" + task.finishSlack)
        }
    }

    /**
     * List details of all calendars in the file.
     *
     * @param file ProjectFile instance
     */
    private fun listCalendars(file: ProjectFile) {
        for (cal in file.calendars) {
            System.out.println(cal.toString())
        }
    }
}
