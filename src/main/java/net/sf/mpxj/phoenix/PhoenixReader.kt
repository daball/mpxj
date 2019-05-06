/*
 * file:       PhoenixReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2015
 * date:       28 November 2015
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

package net.sf.mpxj.phoenix

import java.io.InputStream
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.HashMap
import java.util.LinkedList
import java.util.UUID

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.sax.SAXSource

import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader

import net.sf.mpxj.ChildTaskContainer
import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectCalendarWeek
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Rate
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.AlphanumComparator
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.phoenix.schema.Project
import net.sf.mpxj.phoenix.schema.Project.Layouts.Layout
import net.sf.mpxj.phoenix.schema.Project.Layouts.Layout.CodeOptions.CodeOption
import net.sf.mpxj.phoenix.schema.Project.Settings
import net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint
import net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint.Activities.Activity
import net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint.Activities.Activity.CodeAssignment
import net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint.ActivityCodes.Code
import net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint.ActivityCodes.Code.Value
import net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint.Calendars
import net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint.Calendars.Calendar
import net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint.Calendars.Calendar.NonWork
import net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint.Relationships.Relationship
import net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint.Resources
import net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint.Resources.Resource.Assignment
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * This class creates a new ProjectFile instance by reading a Phoenix Project Manager file.
 */
class PhoenixReader : AbstractProjectReader() {

    private var m_projectFile: ProjectFile? = null
    private var m_activityMap: Map<String, Task>? = null
    private var m_activityCodeValues: Map<UUID, String>? = null
    internal var m_activityCodeSequence: Map<UUID, Integer>? = null
    private var m_activityCodeCache: Map<Activity, ???>? = null
    private var m_eventManager: EventManager? = null
    private var m_projectListeners: List<ProjectListener>? = null
    internal var m_codeSequence: List<UUID>? = null
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
            m_activityMap = HashMap<String, Task>()
            m_activityCodeValues = HashMap<UUID, String>()
            m_activityCodeSequence = HashMap<UUID, Integer>()
            m_activityCodeCache = HashMap<Activity, Map<UUID, UUID>>()
            m_codeSequence = ArrayList<UUID>()
            m_eventManager = m_projectFile!!.eventManager

            val config = m_projectFile!!.projectConfig
            config.autoResourceUniqueID = true
            config.autoOutlineLevel = false
            config.autoOutlineNumber = false
            config.autoWBS = false

            m_projectFile!!.projectProperties.fileApplication = "Phoenix"
            m_projectFile!!.projectProperties.fileType = "PPX"

            // Equivalent to Primavera's Activity ID
            m_projectFile!!.customFields.getCustomField(TaskField.TEXT1).setAlias("Code")

            m_eventManager!!.addProjectListeners(m_projectListeners)

            val factory = SAXParserFactory.newInstance()
            val saxParser = factory.newSAXParser()
            val xmlReader = saxParser.getXMLReader()
            val doc = SAXSource(xmlReader, InputSource(SkipNulInputStream(stream)))

            if (CONTEXT == null) {
                throw CONTEXT_EXCEPTION
            }

            val unmarshaller = CONTEXT!!.createUnmarshaller()

            val phoenixProject = unmarshaller.unmarshal(doc) as Project
            val storepoint = getCurrentStorepoint(phoenixProject)
            readProjectProperties(phoenixProject.settings, storepoint)
            readCalendars(storepoint)
            readTasks(phoenixProject, storepoint)
            readResources(storepoint)
            readRelationships(storepoint)

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
        } finally {
            m_projectFile = null
            m_activityMap = null
            m_activityCodeValues = null
            m_activityCodeSequence = null
            m_activityCodeCache = null
            m_codeSequence = null
        }
    }

    /**
     * This method extracts project properties from a Phoenix file.
     *
     * @param phoenixSettings Phoenix settings
     * @param storepoint Current storepoint
     */
    private fun readProjectProperties(phoenixSettings: Settings, storepoint: Storepoint) {
        val mpxjProperties = m_projectFile!!.projectProperties
        mpxjProperties.name = phoenixSettings.title
        mpxjProperties.defaultDurationUnits = phoenixSettings.baseunit
        mpxjProperties.statusDate = storepoint.dataDate
    }

    /**
     * This method extracts calendar data from a Phoenix file.
     *
     * @param phoenixProject Root node of the Phoenix file
     */
    private fun readCalendars(phoenixProject: Storepoint) {
        val calendars = phoenixProject.calendars
        if (calendars != null) {
            for (calendar in calendars.calendar) {
                readCalendar(calendar)
            }

            val defaultCalendar = m_projectFile!!.getCalendarByName(phoenixProject.defaultCalendar)
            if (defaultCalendar != null) {
                m_projectFile!!.projectProperties.defaultCalendarName = defaultCalendar.name
            }
        }
    }

    /**
     * This method extracts data for a single calendar from a Phoenix file.
     *
     * @param calendar calendar data
     */
    private fun readCalendar(calendar: Calendar) {
        // Create the calendar
        val mpxjCalendar = m_projectFile!!.addCalendar()
        mpxjCalendar.name = calendar.name

        // Default all days to working
        for (day in Day.values()) {
            mpxjCalendar.setWorkingDay(day, true)
        }

        // Mark non-working days
        val nonWorkingDays = calendar.nonWork
        for (nonWorkingDay in nonWorkingDays) {
            // TODO: handle recurring exceptions
            if (nonWorkingDay.type.equals("internal_weekly")) {
                mpxjCalendar.setWorkingDay(nonWorkingDay.weekday, false)
            }
        }

        // Add default working hours for working days
        for (day in Day.values()) {
            if (mpxjCalendar.isWorkingDay(day)) {
                val hours = mpxjCalendar.addCalendarHours(day)
                hours.addRange(ProjectCalendarWeek.DEFAULT_WORKING_MORNING)
                hours.addRange(ProjectCalendarWeek.DEFAULT_WORKING_AFTERNOON)
            }
        }
    }

    /**
     * This method extracts resource data from a Phoenix file.
     *
     * @param phoenixProject parent node for resources
     */
    private fun readResources(phoenixProject: Storepoint) {
        val resources = phoenixProject.resources
        if (resources != null) {
            for (res in resources.resource) {
                val resource = readResource(res)
                readAssignments(resource, res)
            }
        }
    }

    /**
     * This method extracts data for a single resource from a Phoenix file.
     *
     * @param phoenixResource resource data
     * @return Resource instance
     */
    private fun readResource(phoenixResource: net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint.Resources.Resource): Resource {
        val mpxjResource = m_projectFile!!.addResource()

        var rateUnits: TimeUnit? = phoenixResource.monetarybase
        if (rateUnits == null) {
            rateUnits = TimeUnit.HOURS
        }

        // phoenixResource.getMaximum()
        mpxjResource.costPerUse = phoenixResource.monetarycostperuse
        mpxjResource.standardRate = Rate(phoenixResource.monetaryrate, rateUnits)
        mpxjResource.standardRateUnits = rateUnits
        mpxjResource.name = phoenixResource.name
        mpxjResource.type = phoenixResource.type
        mpxjResource.materialLabel = phoenixResource.unitslabel
        //phoenixResource.getUnitsperbase()
        mpxjResource.guid = phoenixResource.uuid

        m_eventManager!!.fireResourceReadEvent(mpxjResource)

        return mpxjResource
    }

    /**
     * Read phases and activities from the Phoenix file to create the task hierarchy.
     *
     * @param phoenixProject all project data
     * @param storepoint storepoint containing current project data
     */
    private fun readTasks(phoenixProject: Project, storepoint: Storepoint) {
        processLayouts(phoenixProject)
        processActivityCodes(storepoint)
        processActivities(storepoint)
        updateDates()
    }

    /**
     * Map from an activity code value UUID to the actual value itself, and its
     * sequence number.
     *
     * @param storepoint storepoint containing current project data
     */
    private fun processActivityCodes(storepoint: Storepoint) {
        for (code in storepoint.activityCodes.code) {
            var sequence = 0
            for (value in code.getValue()) {
                val uuid = getUUID(value.uuid, value.name)
                m_activityCodeValues!!.put(uuid, value.name)
                m_activityCodeSequence!!.put(uuid, Integer.valueOf(++sequence))
            }
        }
    }

    /**
     * Find the current layout and extract the activity code order and visibility.
     *
     * @param phoenixProject phoenix project data
     */
    private fun processLayouts(phoenixProject: Project) {
        //
        // Find the active layout
        //
        val activeLayout = getActiveLayout(phoenixProject)

        //
        // Create a list of the visible codes in the correct order
        //
        for (option in activeLayout.codeOptions.codeOption) {
            if (option.isShown.booleanValue()) {
                m_codeSequence!!.add(getUUID(option.codeUuid, option.code))
            }
        }
    }

    /**
     * Find the current active layout.
     *
     * @param phoenixProject phoenix project data
     * @return current active layout
     */
    private fun getActiveLayout(phoenixProject: Project): Layout {
        //
        // Start with the first layout we find
        //
        var activeLayout = phoenixProject.layouts.layout.get(0)

        //
        // If this isn't active, find one which is... and if none are,
        // we'll just use the first.
        //
        if (!activeLayout.isActive.booleanValue()) {
            for (layout in phoenixProject.layouts.layout) {
                if (layout.isActive.booleanValue()) {
                    activeLayout = layout
                    break
                }
            }
        }

        return activeLayout
    }

    /**
     * Process the set of activities from the Phoenix file.
     *
     * @param phoenixProject project data
     */
    private fun processActivities(phoenixProject: Storepoint) {
        val comparator = AlphanumComparator()
        val activities = phoenixProject.activities.activity
        Collections.sort(activities, object : Comparator<Activity>() {
            @Override
            fun compare(o1: Activity, o2: Activity): Int {
                val codes1 = getActivityCodes(o1)
                val codes2 = getActivityCodes(o2)
                for (code in m_codeSequence!!) {
                    val codeValue1 = codes1[code]
                    val codeValue2 = codes2[code]

                    if (codeValue1 == null || codeValue2 == null) {
                        if (codeValue1 == null && codeValue2 == null) {
                            continue
                        }

                        if (codeValue1 == null) {
                            return -1
                        }

                        if (codeValue2 == null) {
                            return 1
                        }
                    }

                    if (!codeValue1!!.equals(codeValue2)) {
                        val sequence1 = m_activityCodeSequence!![codeValue1]
                        val sequence2 = m_activityCodeSequence!![codeValue2]

                        return NumberHelper.compare(sequence1, sequence2)
                    }
                }

                return comparator.compare(o1.id, o2.id)
            }
        })

        for (activity in activities) {
            processActivity(activity)
        }
    }

    /**
     * Create a Task instance from a Phoenix activity.
     *
     * @param activity Phoenix activity data
     */
    private fun processActivity(activity: Activity) {
        val task = getParentTask(activity).addTask()
        task.setText(1, activity.id)

        task.actualDuration = activity.actualDuration
        task.actualFinish = activity.actualFinish
        task.actualStart = activity.actualStart
        //activity.getBaseunit()
        //activity.getBilled()
        //activity.getCalendar()
        //activity.getCostAccount()
        task.createDate = activity.creationTime
        task.finish = activity.currentFinish
        task.start = activity.currentStart
        task.name = activity.description
        task.duration = activity.durationAtCompletion
        task.earlyFinish = activity.earlyFinish
        task.earlyStart = activity.earlyStart
        task.freeSlack = activity.freeFloat
        task.lateFinish = activity.lateFinish
        task.lateStart = activity.lateStart
        task.notes = activity.notes
        task.baselineDuration = activity.originalDuration
        //activity.getPathFloat()
        task.physicalPercentComplete = activity.physicalPercentComplete
        task.remainingDuration = activity.remainingDuration
        task.cost = activity.totalCost
        task.totalSlack = activity.totalFloat
        task.milestone = activityIsMilestone(activity)
        //activity.getUserDefined()
        task.guid = activity.uuid

        if (task.milestone) {
            if (activityIsStartMilestone(activity)) {
                task.finish = task.start
            } else {
                task.start = task.finish
            }
        }

        if (task.actualStart == null) {
            task.percentageComplete = Integer.valueOf(0)
        } else {
            if (task.actualFinish != null) {
                task.percentageComplete = Integer.valueOf(100)
            } else {
                val remaining = activity.remainingDuration
                val total = activity.durationAtCompletion
                if (remaining != null && total != null && total.getDuration() !== 0) {
                    val percentComplete = (total.getDuration() - remaining.getDuration()) * 100.0 / total.getDuration()
                    task.percentageComplete = Double.valueOf(percentComplete)
                }
            }
        }

        m_activityMap!!.put(activity.id, task)
    }

    /**
     * Returns true if the activity is a milestone.
     *
     * @param activity Phoenix activity
     * @return true if the activity is a milestone
     */
    private fun activityIsMilestone(activity: Activity): Boolean {
        val type = activity.type
        return type != null && type.indexOf("Milestone") !== -1
    }

    /**
     * Returns true if the activity is a start milestone.
     *
     * @param activity Phoenix activity
     * @return true if the activity is a milestone
     */
    private fun activityIsStartMilestone(activity: Activity): Boolean {
        val type = activity.type
        return type != null && type.indexOf("StartMilestone") !== -1
    }

    /**
     * Retrieves the parent task for a Phoenix activity.
     *
     * @param activity Phoenix activity
     * @return parent task
     */
    private fun getParentTask(activity: Activity): ChildTaskContainer {
        //
        // Make a map of activity codes and their values for this activity
        //
        val map = getActivityCodes(activity)

        //
        // Work through the activity codes in sequence
        //
        var parent: ChildTaskContainer? = m_projectFile
        val uniqueIdentifier = StringBuilder()
        for (activityCode in m_codeSequence!!) {
            val activityCodeValue = map[activityCode]
            val activityCodeText = m_activityCodeValues!![activityCodeValue]
            if (activityCodeText != null) {
                if (uniqueIdentifier.length() !== 0) {
                    uniqueIdentifier.append('>')
                }
                uniqueIdentifier.append(activityCodeValue.toString())
                val uuid = UUID.nameUUIDFromBytes(uniqueIdentifier.toString().getBytes())
                var newParent = findChildTaskByUUID(parent!!, uuid)
                if (newParent == null) {
                    newParent = parent!!.addTask()
                    newParent!!.guid = uuid
                    newParent.name = activityCodeText
                }
                parent = newParent
            }
        }
        return parent
    }

    /**
     * Locates a task within a child task container which matches the supplied UUID.
     *
     * @param parent child task container
     * @param uuid required UUID
     * @return Task instance or null if the task is not found
     */
    private fun findChildTaskByUUID(parent: ChildTaskContainer, uuid: UUID): Task? {
        var result: Task? = null

        for (task in parent.getChildTasks()) {
            if (uuid.equals(task.guid)) {
                result = task
                break
            }
        }

        return result
    }

    /**
     * Reads Phoenix resource assignments.
     *
     * @param mpxjResource MPXJ resource
     * @param res Phoenix resource
     */
    private fun readAssignments(mpxjResource: Resource, res: net.sf.mpxj.phoenix.schema.Project.Storepoints.Storepoint.Resources.Resource) {
        for (assignment in res.assignment) {
            readAssignment(mpxjResource, assignment)
        }
    }

    /**
     * Read a single resource assignment.
     *
     * @param resource MPXJ resource
     * @param assignment Phoenix assignment
     */
    private fun readAssignment(resource: Resource, assignment: Assignment) {
        val task = m_activityMap!!.get(assignment.activity)
        task?.addResourceAssignment(resource)
    }

    /**
     * Read task relationships from a Phoenix file.
     *
     * @param phoenixProject Phoenix project data
     */
    private fun readRelationships(phoenixProject: Storepoint) {
        for (relation in phoenixProject.relationships.relationship) {
            readRelation(relation)
        }
    }

    /**
     * Read an individual Phoenix task relationship.
     *
     * @param relation Phoenix task relationship
     */
    private fun readRelation(relation: Relationship) {
        val predecessor = m_activityMap!!.get(relation.predecessor)
        val successor = m_activityMap!!.get(relation.successor)
        if (predecessor != null && successor != null) {
            val lag = relation.lag
            val type = relation.type
            successor.addPredecessor(predecessor, type, lag)
        }
    }

    /**
     * For a given activity, retrieve a map of the activity code values which have been assigned to it.
     *
     * @param activity target activity
     * @return map of activity code value UUIDs
     */
    internal fun getActivityCodes(activity: Activity): Map<UUID, UUID> {
        var map = m_activityCodeCache!![activity]
        if (map == null) {
            map = HashMap<UUID, UUID>()
            m_activityCodeCache!!.put(activity, map)
            for (ca in activity.codeAssignment) {
                val code = getUUID(ca.codeUuid, ca.code)
                val value = getUUID(ca.valueUuid, ca.value)
                map!!.put(code, value)
            }
        }
        return map
    }

    /**
     * Retrieve the most recent storepoint.
     *
     * @param phoenixProject project data
     * @return Storepoint instance
     */
    private fun getCurrentStorepoint(phoenixProject: Project): Storepoint {
        val storepoints = phoenixProject.storepoints.storepoint
        Collections.sort(storepoints, object : Comparator<Storepoint>() {
            @Override
            fun compare(o1: Storepoint, o2: Storepoint): Int {
                return DateHelper.compare(o2.creationTime, o1.creationTime)
            }
        })
        return storepoints.get(0)
    }

    /**
     * Utility method. In some cases older compressed PPX files only have a name (or other string attribute)
     * but no UUID. This method ensures that we either use the UUID supplied, or if it is missing, we
     * generate a UUID from the name.
     *
     * @param uuid uuid from object
     * @param name name from object
     * @return UUID instance
     */
    private fun getUUID(uuid: UUID?, name: String): UUID {
        return if (uuid == null) UUID.nameUUIDFromBytes(name.getBytes()) else uuid
    }

    /**
     * Ensure summary tasks have dates.
     */
    private fun updateDates() {
        for (task in m_projectFile!!.childTasks) {
            updateDates(task)
        }
    }

    /**
     * See the notes above.
     *
     * @param parentTask parent task.
     */
    private fun updateDates(parentTask: Task) {
        if (parentTask.hasChildTasks()) {
            var finished = 0
            var plannedStartDate: Date? = parentTask.start
            var plannedFinishDate: Date? = parentTask.finish
            var actualStartDate = parentTask.actualStart
            var actualFinishDate: Date? = parentTask.actualFinish
            var earlyStartDate: Date? = parentTask.earlyStart
            var earlyFinishDate: Date? = parentTask.earlyFinish
            var lateStartDate: Date? = parentTask.lateStart
            var lateFinishDate: Date? = parentTask.lateFinish

            for (task in parentTask.childTasks) {
                updateDates(task)

                plannedStartDate = DateHelper.min(plannedStartDate, task.start)
                plannedFinishDate = DateHelper.max(plannedFinishDate, task.finish)
                actualStartDate = DateHelper.min(actualStartDate, task.actualStart)
                actualFinishDate = DateHelper.max(actualFinishDate, task.actualFinish)
                earlyStartDate = DateHelper.min(earlyStartDate, task.earlyStart)
                earlyFinishDate = DateHelper.max(earlyFinishDate, task.earlyFinish)
                lateStartDate = DateHelper.min(lateStartDate, task.lateStart)
                lateFinishDate = DateHelper.max(lateFinishDate, task.lateFinish)

                if (task.actualFinish != null) {
                    ++finished
                }
            }

            parentTask.start = plannedStartDate
            parentTask.finish = plannedFinishDate
            parentTask.actualStart = actualStartDate
            parentTask.earlyStart = earlyStartDate
            parentTask.earlyFinish = earlyFinishDate
            parentTask.lateStart = lateStartDate
            parentTask.lateFinish = lateFinishDate

            //
            // Only if all child tasks have actual finish dates do we
            // set the actual finish date on the parent task.
            //
            if (finished == parentTask.childTasks.size()) {
                parentTask.actualFinish = actualFinishDate
            }

            var duration: Duration? = null
            if (plannedStartDate != null && plannedFinishDate != null) {
                duration = m_projectFile!!.defaultCalendar!!.getWork(plannedStartDate, plannedFinishDate, TimeUnit.DAYS)
                parentTask.duration = duration
            }
        }
    }

    companion object {

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
                CONTEXT = JAXBContext.newInstance("net.sf.mpxj.phoenix.schema", PhoenixReader::class.java!!.getClassLoader())
            } catch (ex: JAXBException) {
                CONTEXT_EXCEPTION = ex
                CONTEXT = null
            }

        }
    }
}
