/*
 * file:       ConceptDrawProjectReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       9 July 2018
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

package net.sf.mpxj.conceptdraw

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashMap
import java.util.LinkedList
import java.util.UUID

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import javax.xml.bind.UnmarshallerHandler
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLFilter
import org.xml.sax.XMLReader

import net.sf.mpxj.DateRange
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Rate
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.AlphanumComparator
import net.sf.mpxj.conceptdraw.schema.Document
import net.sf.mpxj.conceptdraw.schema.Document.Calendars.Calendar
import net.sf.mpxj.conceptdraw.schema.Document.Calendars.Calendar.ExceptedDays.ExceptedDay
import net.sf.mpxj.conceptdraw.schema.Document.Calendars.Calendar.WeekDays.WeekDay
import net.sf.mpxj.conceptdraw.schema.Document.Links.Link
import net.sf.mpxj.conceptdraw.schema.Document.Projects.Project
import net.sf.mpxj.conceptdraw.schema.Document.WorkspaceProperties
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * This class creates a new ProjectFile instance by reading a ConceptDraw Project file.
 */
class ConceptDrawProjectReader : AbstractProjectReader() {

    private var m_projectFile: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_projectListeners: List<ProjectListener>? = null
    private var m_calendarMap: Map<Integer, ProjectCalendar>? = null
    private var m_taskIdMap: Map<Integer, Task>? = null
    private var m_workHoursPerDay: Double = 0.toDouble()
    /**
     * {@inheritDoc}
     */
    @Override
    override fun addProjectListener(listener: ProjectListener) {
        if (m_projectListeners == null) {
            m_projectListeners = LinkedList<ProjectListener>()
        }
        m_projectListeners!!.add(listener)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(stream: InputStream): ProjectFile {
        try {
            m_projectFile = ProjectFile()
            m_eventManager = m_projectFile!!.eventManager
            m_calendarMap = HashMap<Integer, ProjectCalendar>()
            m_taskIdMap = HashMap<Integer, Task>()

            val config = m_projectFile!!.projectConfig
            config.autoResourceUniqueID = false
            config.autoResourceID = false

            m_projectFile!!.projectProperties.fileApplication = "ConceptDraw PROJECT"
            m_projectFile!!.projectProperties.fileType = "CDP"

            m_eventManager!!.addProjectListeners(m_projectListeners)

            val factory = SAXParserFactory.newInstance()
            val saxParser = factory.newSAXParser()
            val xmlReader = saxParser.getXMLReader()

            if (CONTEXT == null) {
                throw CONTEXT_EXCEPTION
            }

            val unmarshaller = CONTEXT!!.createUnmarshaller()

            val filter = NamespaceFilter()
            filter.setParent(xmlReader)
            val unmarshallerHandler = unmarshaller.getUnmarshallerHandler()
            filter.setContentHandler(unmarshallerHandler)
            filter.parse(InputSource(InputStreamReader(stream)))
            val cdp = unmarshallerHandler.getResult() as Document

            readProjectProperties(cdp)
            readCalendars(cdp)
            readResources(cdp)
            readTasks(cdp)
            readRelationships(cdp)

            //
            // Ensure that the unique ID counters are correct
            //
            config.updateUniqueCounters()

            return m_projectFile
        } catch (ex: ParserConfigurationException) {
            throw MPXJException("Failed to parse file", ex)
        } catch (ex: JAXBException) {
            throw MPXJException("Failed to parse file", ex)
        } catch (ex: SAXException) {
            throw MPXJException("Failed to parse file", ex)
        } catch (ex: IOException) {
            throw MPXJException("Failed to parse file", ex)
        } finally {
            m_projectFile = null
            m_eventManager = null
            m_projectListeners = null
            m_calendarMap = null
            m_taskIdMap = null
        }
    }

    /**
     * Extracts project properties from a ConceptDraw PROJECT file.
     *
     * @param cdp ConceptDraw PROJECT file
     */
    private fun readProjectProperties(cdp: Document) {
        val props = cdp.workspaceProperties
        val mpxjProps = m_projectFile!!.projectProperties
        mpxjProps.symbolPosition = props.currencyPosition
        mpxjProps.currencyDigits = props.currencyDigits
        mpxjProps.currencySymbol = props.currencySymbol
        mpxjProps.daysPerMonth = props.daysPerMonth
        mpxjProps.minutesPerDay = props.hoursPerDay
        mpxjProps.minutesPerWeek = props.hoursPerWeek

        m_workHoursPerDay = mpxjProps.minutesPerDay.doubleValue() / 60.0
    }

    /**
     * Extracts calendar data from a ConceptDraw PROJECT file.
     *
     * @param cdp ConceptDraw PROJECT file
     */
    private fun readCalendars(cdp: Document) {
        for (calendar in cdp.calendars.calendar) {
            readCalendar(calendar)
        }

        for (calendar in cdp.calendars.calendar) {
            val child = m_calendarMap!![calendar.id]
            val parent = m_calendarMap!![calendar.baseCalendarID]
            if (parent == null) {
                m_projectFile!!.defaultCalendar = child
            } else {
                child.parent = parent
            }
        }
    }

    /**
     * Read a calendar.
     *
     * @param calendar ConceptDraw PROJECT calendar
     */
    private fun readCalendar(calendar: Calendar) {
        val mpxjCalendar = m_projectFile!!.addCalendar()
        mpxjCalendar.name = calendar.name
        m_calendarMap!!.put(calendar.id, mpxjCalendar)

        for (day in calendar.weekDays.weekDay) {
            readWeekDay(mpxjCalendar, day)
        }

        for (day in calendar.exceptedDays.exceptedDay) {
            readExceptionDay(mpxjCalendar, day)
        }
    }

    /**
     * Reads a single day for a calendar.
     *
     * @param mpxjCalendar ProjectCalendar instance
     * @param day ConceptDraw PROJECT week day
     */
    private fun readWeekDay(mpxjCalendar: ProjectCalendar, day: WeekDay) {
        if (day.isIsDayWorking) {
            val hours = mpxjCalendar.addCalendarHours(day.day)
            for (period in day.timePeriods.timePeriod) {
                hours.addRange(DateRange(period.from, period.to))
            }
        }
    }

    /**
     * Read an exception day for a calendar.
     *
     * @param mpxjCalendar ProjectCalendar instance
     * @param day ConceptDraw PROJECT exception day
     */
    private fun readExceptionDay(mpxjCalendar: ProjectCalendar, day: ExceptedDay) {
        val mpxjException = mpxjCalendar.addCalendarException(day.date, day.date)
        if (day.isIsDayWorking) {
            for (period in day.timePeriods.timePeriod) {
                mpxjException.addRange(DateRange(period.from, period.to))
            }
        }
    }

    /**
     * Reads resource data from a ConceptDraw PROJECT file.
     *
     * @param cdp ConceptDraw PROJECT file
     */
    private fun readResources(cdp: Document) {
        for (resource in cdp.resources.resource) {
            readResource(resource)
        }
    }

    /**
     * Reads a single resource from a ConceptDraw PROJECT file.
     *
     * @param resource ConceptDraw PROJECT resource
     */
    private fun readResource(resource: Document.Resources.Resource) {
        val mpxjResource = m_projectFile!!.addResource()
        mpxjResource.name = resource.name
        mpxjResource.resourceCalendar = m_calendarMap!![resource.calendarID]
        mpxjResource.standardRate = Rate(resource.cost, resource.costTimeUnit)
        mpxjResource.emailAddress = resource.eMail
        mpxjResource.group = resource.group
        //resource.getHyperlinks()
        mpxjResource.uniqueID = resource.id
        //resource.getMarkerID()
        mpxjResource.notes = resource.note
        mpxjResource.id = Integer.valueOf(resource.outlineNumber)
        //resource.getStyleProject()
        mpxjResource.type = if (resource.subType == null) resource.type else resource.subType
    }

    /**
     * Read the projects from a ConceptDraw PROJECT file as top level tasks.
     *
     * @param cdp ConceptDraw PROJECT file
     */
    private fun readTasks(cdp: Document) {
        //
        // Sort the projects into the correct order
        //
        val projects = ArrayList<Project>(cdp.projects.project)
        val comparator = AlphanumComparator()

        Collections.sort(projects, object : Comparator<Project>() {
            @Override
            fun compare(o1: Project, o2: Project): Int {
                return comparator.compare(o1.outlineNumber, o2.outlineNumber)
            }
        })

        for (project in cdp.projects.project) {
            readProject(project)
        }
    }

    /**
     * Read a project from a ConceptDraw PROJECT file.
     *
     * @param project ConceptDraw PROJECT project
     */
    private fun readProject(project: Project) {
        val mpxjTask = m_projectFile!!.addTask()
        //project.getAuthor()
        mpxjTask.baselineCost = project.baselineCost
        mpxjTask.baselineFinish = project.baselineFinishDate
        mpxjTask.baselineStart = project.baselineStartDate
        //project.getBudget();
        //project.getCompany()
        mpxjTask.finish = project.finishDate
        //project.getGoal()
        //project.getHyperlinks()
        //project.getMarkerID()
        mpxjTask.name = project.name
        mpxjTask.notes = project.note
        mpxjTask.priority = project.priority
        //      project.getSite()
        mpxjTask.start = project.startDate
        //      project.getStyleProject()
        //      project.getTask()
        //      project.getTimeScale()
        //      project.getViewProperties()

        val projectIdentifier = project.id.toString()
        mpxjTask.guid = UUID.nameUUIDFromBytes(projectIdentifier.getBytes())

        //
        // Sort the tasks into the correct order
        //
        val tasks = ArrayList<Document.Projects.Project.Task>(project.task)
        val comparator = AlphanumComparator()

        Collections.sort(tasks, object : Comparator<Document.Projects.Project.Task>() {
            @Override
            fun compare(o1: Document.Projects.Project.Task, o2: Document.Projects.Project.Task): Int {
                return comparator.compare(o1.outlineNumber, o2.outlineNumber)
            }
        })

        val map = HashMap<String, Task>()
        map.put("", mpxjTask)

        for (task in tasks) {
            readTask(projectIdentifier, map, task)
        }
    }

    /**
     * Read a task from a ConceptDraw PROJECT file.
     *
     * @param projectIdentifier parent project identifier
     * @param map outline number to task map
     * @param task ConceptDraw PROJECT task
     */
    private fun readTask(projectIdentifier: String, map: Map<String, Task>, task: Document.Projects.Project.Task) {
        val parentTask = map[getParentOutlineNumber(task.outlineNumber)]
        val mpxjTask = parentTask.addTask()

        val units = task.baseDurationTimeUnit

        mpxjTask.cost = task.actualCost
        mpxjTask.duration = getDuration(units, task.actualDuration)
        mpxjTask.finish = task.actualFinishDate
        mpxjTask.start = task.actualStartDate
        mpxjTask.baselineDuration = getDuration(units, task.baseDuration)
        mpxjTask.baselineFinish = task.baseFinishDate
        mpxjTask.baselineCost = task.baselineCost
        //      task.getBaselineFinishDate()
        //      task.getBaselineFinishTemplateOffset()
        //      task.getBaselineStartDate()
        //      task.getBaselineStartTemplateOffset()
        mpxjTask.baselineStart = task.baseStartDate
        //      task.getCallouts()
        mpxjTask.percentageComplete = task.complete
        mpxjTask.deadline = task.deadlineDate
        //      task.getDeadlineTemplateOffset()
        //      task.getHyperlinks()
        //      task.getMarkerID()
        mpxjTask.name = task.name
        mpxjTask.notes = task.note
        mpxjTask.priority = task.priority
        //      task.getRecalcBase1()
        //      task.getRecalcBase2()
        mpxjTask.type = task.schedulingType
        //      task.getStyleProject()
        //      task.getTemplateOffset()
        //      task.getValidatedByProject()

        if (task.isIsMilestone) {
            mpxjTask.milestone = true
            mpxjTask.duration = Duration.getInstance(0, TimeUnit.HOURS)
            mpxjTask.baselineDuration = Duration.getInstance(0, TimeUnit.HOURS)
        }

        val taskIdentifier = projectIdentifier + "." + task.id
        m_taskIdMap!!.put(task.id, mpxjTask)
        mpxjTask.guid = UUID.nameUUIDFromBytes(taskIdentifier.getBytes())

        map.put(task.outlineNumber, mpxjTask)

        for (assignment in task.resourceAssignments.resourceAssignment) {
            readResourceAssignment(mpxjTask, assignment)
        }
    }

    /**
     * Read resource assignments.
     *
     * @param task Parent task
     * @param assignment ConceptDraw PROJECT resource assignment
     */
    private fun readResourceAssignment(task: Task, assignment: Document.Projects.Project.Task.ResourceAssignments.ResourceAssignment) {
        val resource = m_projectFile!!.getResourceByUniqueID(assignment.resourceID)
        if (resource != null) {
            val mpxjAssignment = task.addResourceAssignment(resource)
            mpxjAssignment.uniqueID = assignment.id
            mpxjAssignment.work = Duration.getInstance(assignment.manHour.doubleValue() * m_workHoursPerDay, TimeUnit.HOURS)
            mpxjAssignment.units = assignment.use
        }
    }

    /**
     * Read all task relationships from a ConceptDraw PROJECT file.
     *
     * @param cdp ConceptDraw PROJECT file
     */
    private fun readRelationships(cdp: Document) {
        for (link in cdp.links.link) {
            readRelationship(link)
        }
    }

    /**
     * Read a task relationship.
     *
     * @param link ConceptDraw PROJECT task link
     */
    private fun readRelationship(link: Link) {
        val sourceTask = m_taskIdMap!![link.sourceTaskID]
        val destinationTask = m_taskIdMap!![link.destinationTaskID]
        if (sourceTask != null && destinationTask != null) {
            val lag = getDuration(link.lagUnit, link.lag)
            val type = link.type
            val relation = destinationTask.addPredecessor(sourceTask, type, lag)
            relation.uniqueID = link.id
        }
    }

    /**
     * Read a duration.
     *
     * @param units duration units
     * @param duration duration value
     * @return Duration instance
     */
    private fun getDuration(units: TimeUnit, duration: Double?): Duration? {
        var result: Duration? = null
        if (duration != null) {
            var durationValue = duration.doubleValue() * 100.0

            when (units) {
                MINUTES -> {
                    durationValue *= MINUTES_PER_DAY.toDouble()
                }

                HOURS -> {
                    durationValue *= HOURS_PER_DAY.toDouble()
                }

                DAYS -> {
                    durationValue *= 3.0
                }

                WEEKS -> {
                    durationValue *= 0.6
                }

                MONTHS -> {
                    durationValue *= 0.15
                }

                else -> {
                    throw IllegalArgumentException("Unsupported time units $units")
                }
            }

            durationValue = Math.round(durationValue) / 100.0

            result = Duration.getInstance(durationValue, units)
        }

        return result
    }

    /**
     * Return the parent outline number, or an empty string if
     * we have a root task.
     *
     * @param outlineNumber child outline number
     * @return parent outline number
     */
    private fun getParentOutlineNumber(outlineNumber: String): String {
        val result: String
        val index = outlineNumber.lastIndexOf('.')
        if (index == -1) {
            result = ""
        } else {
            result = outlineNumber.substring(0, index)
        }
        return result
    }

    companion object {

        private val HOURS_PER_DAY = 24
        private val MINUTES_PER_DAY = HOURS_PER_DAY * 60

        /**
         * Cached context to minimise construction cost.
         */
        private var CONTEXT: JAXBContext? = null

        /**
         * Note any error occurring during context construction.
         */
        private var CONTEXT_EXCEPTION: JAXBException? = null

        init {
            try {
                //
                // JAXB RI property to speed up construction
                //
                System.setProperty("com.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot", "true")

                //
                // Construct the context
                //
                CONTEXT = JAXBContext.newInstance("net.sf.mpxj.conceptdraw.schema", ConceptDrawProjectReader::class.java!!.getClassLoader())
            } catch (ex: JAXBException) {
                CONTEXT_EXCEPTION = ex
                CONTEXT = null
            }

        }
    }
}
