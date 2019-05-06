/*
 * file:       MerlinReaders.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2016
 * date:       17/11/2016
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

package net.sf.mpxj.merlin

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.StringReader
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.LinkedList
import java.util.Properties

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import net.sf.mpxj.ConstraintType
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.MPXJException
import net.sf.mpxj.Priority
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceType
import net.sf.mpxj.ScheduleFrom
import net.sf.mpxj.Task
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.FileHelper
import net.sf.mpxj.common.InputStreamHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.ProjectReader

/**
 * This class reads Merlin Project files. As Merlin is a Mac application, the "file"
 * seen by the user is actually a directory. The file in this directory we are interested
 * in is a SQLite database. You can either point the read methods directly to this database
 * file, or the read methods that accept a file name or a File object can be pointed at
 * the top level directory.
 */
class MerlinReader : ProjectReader {

    private var m_project: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private val m_projectID = Integer.valueOf(1)
    private var m_connection: Connection? = null
    private var m_ps: PreparedStatement? = null
    private var m_rs: ResultSet? = null
    private val m_meta = HashMap<String, Integer>()
    private var m_projectListeners: List<ProjectListener>? = null
    private var m_documentBuilder: DocumentBuilder? = null
    private val m_calendarTimeFormat = SimpleDateFormat("HH:mm:ss")
    private var m_dayTimeIntervals: XPathExpression? = null
    private var m_entityMap: Map<String, Integer>? = null
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
    override fun read(stream: InputStream?): ProjectFile {
        var file: File? = null
        try {
            file = InputStreamHelper.writeStreamToTempFile(stream!!, ".sqlite")
            return read(file!!)
        } catch (ex: IOException) {
            throw MPXJException("", ex)
        } finally {
            FileHelper.deleteQuietly(file)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(fileName: String?): ProjectFile {
        return read(File(fileName))
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(file: File): ProjectFile {
        val databaseFile: File
        if (file.isDirectory()) {
            databaseFile = File(file, "state.sql")
        } else {
            databaseFile = file
        }
        return readFile(databaseFile)
    }

    /**
     * By the time we reach this method, we should be looking at the SQLite
     * database file itself.
     *
     * @param file SQLite database file
     * @return ProjectFile instance
     */
    @Throws(MPXJException::class)
    private fun readFile(file: File): ProjectFile {
        try {
            val url = "jdbc:sqlite:" + file.getAbsolutePath()
            val props = Properties()
            m_connection = org.sqlite.JDBC.createConnection(url, props)

            m_documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

            val xPathfactory = XPathFactory.newInstance()
            val xpath = xPathfactory.newXPath()
            m_dayTimeIntervals = xpath.compile("/array/dayTimeInterval")
            m_entityMap = HashMap<String, Integer>()
            return read()
        } catch (ex: Exception) {
            throw MPXJException(MPXJException.INVALID_FORMAT, ex)
        } finally {
            if (m_connection != null) {
                try {
                    m_connection!!.close()
                } catch (ex: SQLException) {
                    // silently ignore exceptions when closing connection
                }

            }

            m_documentBuilder = null
            m_dayTimeIntervals = null
            m_entityMap = null
        }
    }

    /**
     * Read the project data and return a ProjectFile instance.
     *
     * @return ProjectFile instance
     */
    @Throws(Exception::class)
    private fun read(): ProjectFile {
        m_project = ProjectFile()
        m_eventManager = m_project!!.eventManager

        val config = m_project!!.projectConfig
        config.autoCalendarUniqueID = false
        config.autoTaskUniqueID = false
        config.autoResourceUniqueID = false

        m_project!!.projectProperties.fileApplication = "Merlin"
        m_project!!.projectProperties.fileType = "SQLITE"

        m_eventManager!!.addProjectListeners(m_projectListeners)

        populateEntityMap()
        processProject()
        processCalendars()
        processResources()
        processTasks()
        processAssignments()
        processDependencies()

        return m_project
    }

    /**
     * Create a mapping from entity names to entity ID values.
     */
    @Throws(SQLException::class)
    private fun populateEntityMap() {
        for (row in getRows("select * from z_primarykey")) {
            m_entityMap!!.put(row.getString("Z_NAME"), row.getInteger("Z_ENT"))
        }
    }

    /**
     * Read project properties.
     */
    @Throws(SQLException::class)
    private fun processProject() {
        val props = m_project!!.projectProperties
        val row = getRows("select * from zproject where z_pk=?", m_projectID)[0]
        props.weekStartDay = Day.getInstance(row.getInt("ZFIRSTDAYOFWEEK") + 1)
        props.scheduleFrom = if (row.getInt("ZSCHEDULINGDIRECTION") == 1) ScheduleFrom.START else ScheduleFrom.FINISH
        props.minutesPerDay = Integer.valueOf(row.getInt("ZHOURSPERDAY") * 60)
        props.daysPerMonth = row.getInteger("ZDAYSPERMONTH")
        props.minutesPerWeek = Integer.valueOf(row.getInt("ZHOURSPERWEEK") * 60)
        props.statusDate = row.getTimestamp("ZGIVENSTATUSDATE")
        props.currencySymbol = row.getString("ZCURRENCYSYMBOL")
        props.name = row.getString("ZTITLE")
        props.uniqueID = row.getUUID("ZUNIQUEID").toString()
    }

    /**
     * Read calendar data.
     */
    @Throws(Exception::class)
    private fun processCalendars() {
        val rows = getRows("select * from zcalendar where zproject=?", m_projectID)
        for (row in rows) {
            val calendar = m_project!!.addCalendar()
            calendar.uniqueID = row.getInteger("Z_PK")
            calendar.name = row.getString("ZTITLE")
            processDays(calendar)
            processExceptions(calendar)
            m_eventManager!!.fireCalendarReadEvent(calendar)
        }
    }

    /**
     * Process normal calendar working and non-working days.
     *
     * @param calendar parent calendar
     */
    @Throws(Exception::class)
    private fun processDays(calendar: ProjectCalendar) {
        // Default all days to non-working
        for (day in Day.values()) {
            calendar.setWorkingDay(day, false)
        }

        val rows = getRows("select * from zcalendarrule where zcalendar1=? and z_ent=?", calendar.uniqueID, m_entityMap!!["CalendarWeekDayRule"])
        for (row in rows) {
            val day = row.getDay("ZWEEKDAY")
            val timeIntervals = row.getString("ZTIMEINTERVALS")
            if (timeIntervals == null) {
                calendar.setWorkingDay(day, false)
            } else {
                val hours = calendar.addCalendarHours(day)
                val nodes = getNodeList(timeIntervals, m_dayTimeIntervals!!)
                calendar.setWorkingDay(day, nodes.getLength() > 0)

                for (loop in 0 until nodes.getLength()) {
                    val attributes = nodes.item(loop).getAttributes()
                    val startTime = m_calendarTimeFormat.parse(attributes.getNamedItem("startTime").getTextContent())
                    var endTime = m_calendarTimeFormat.parse(attributes.getNamedItem("endTime").getTextContent())

                    if (startTime.getTime() >= endTime.getTime()) {
                        endTime = DateHelper.addDays(endTime, 1)
                    }

                    hours.addRange(DateRange(startTime, endTime))
                }
            }
        }
    }

    /**
     * Process calendar exceptions.
     *
     * @param calendar parent calendar.
     */
    @Throws(Exception::class)
    private fun processExceptions(calendar: ProjectCalendar) {
        val rows = getRows("select * from zcalendarrule where zcalendar=? and z_ent=?", calendar.uniqueID, m_entityMap!!["CalendarExceptionRule"])
        for (row in rows) {
            val startDay = row.getDate("ZSTARTDAY")
            val endDay = row.getDate("ZENDDAY")
            val exception = calendar.addCalendarException(startDay, endDay)

            val timeIntervals = row.getString("ZTIMEINTERVALS")
            if (timeIntervals != null) {
                val nodes = getNodeList(timeIntervals, m_dayTimeIntervals!!)
                for (loop in 0 until nodes.getLength()) {
                    val attributes = nodes.item(loop).getAttributes()
                    val startTime = m_calendarTimeFormat.parse(attributes.getNamedItem("startTime").getTextContent())
                    var endTime = m_calendarTimeFormat.parse(attributes.getNamedItem("endTime").getTextContent())

                    if (startTime.getTime() >= endTime.getTime()) {
                        endTime = DateHelper.addDays(endTime, 1)
                    }

                    exception.addRange(DateRange(startTime, endTime))
                }
            }
        }
    }

    /**
     * Read resource data.
     */
    @Throws(SQLException::class)
    private fun processResources() {
        val rows = getRows("select * from zresource where zproject=? order by zorderinproject", m_projectID)
        for (row in rows) {
            val resource = m_project!!.addResource()
            resource.uniqueID = row.getInteger("Z_PK")
            resource.emailAddress = row.getString("ZEMAIL")
            resource.initials = row.getString("ZINITIALS")
            resource.name = row.getString("ZTITLE_")
            resource.guid = row.getUUID("ZUNIQUEID")
            resource.type = row.getResourceType("ZTYPE")
            resource.materialLabel = row.getString("ZMATERIALUNIT")

            if (resource.type == ResourceType.WORK) {
                resource.maxUnits = Double.valueOf(NumberHelper.getDouble(row.getDouble("ZAVAILABLEUNITS_")) * 100.0)
            }

            val calendarID = row.getInteger("ZRESOURCECALENDAR")
            if (calendarID != null) {
                val calendar = m_project!!.getCalendarByUniqueID(calendarID)
                if (calendar != null) {
                    calendar.name = resource.name
                    resource.resourceCalendar = calendar
                }
            }

            m_eventManager!!.fireResourceReadEvent(resource)
        }
    }

    /**
     * Read all top level tasks.
     */
    @Throws(SQLException::class)
    private fun processTasks() {
        //
        // Yes... we could probably read this in one query in the right order
        // using a CTE... but life's too short.
        //
        val rows = getRows("select * from zscheduleitem where zproject=? and zparentactivity_ is null and z_ent=? order by zorderinparentactivity", m_projectID, m_entityMap!!["Activity"])
        for (row in rows) {
            val task = m_project!!.addTask()
            populateTask(row, task)
            processChildTasks(task)
        }
    }

    /**
     * Read all child tasks for a given parent.
     *
     * @param parentTask parent task
     */
    @Throws(SQLException::class)
    private fun processChildTasks(parentTask: Task) {
        val rows = getRows("select * from zscheduleitem where zparentactivity_=? and z_ent=? order by zorderinparentactivity", parentTask.uniqueID, m_entityMap!!["Activity"])
        for (row in rows) {
            val task = parentTask.addTask()
            populateTask(row, task)
            processChildTasks(task)
        }
    }

    /**
     * Read data for an individual task.
     *
     * @param row task data from database
     * @param task Task instance
     */
    private fun populateTask(row: Row, task: Task) {
        task.uniqueID = row.getInteger("Z_PK")
        task.name = row.getString("ZTITLE")
        task.priority = Priority.getInstance(row.getInt("ZPRIORITY"))
        task.milestone = row.getBoolean("ZISMILESTONE")
        task.actualFinish = row.getTimestamp("ZGIVENACTUALENDDATE_")
        task.actualStart = row.getTimestamp("ZGIVENACTUALSTARTDATE_")
        task.notes = row.getString("ZOBJECTDESCRIPTION")
        task.duration = row.getDuration("ZGIVENDURATION_")
        task.overtimeWork = row.getWork("ZGIVENWORKOVERTIME_")
        task.work = row.getWork("ZGIVENWORK_")
        task.levelingDelay = row.getDuration("ZLEVELINGDELAY_")
        task.actualOvertimeWork = row.getWork("ZGIVENACTUALWORKOVERTIME_")
        task.actualWork = row.getWork("ZGIVENACTUALWORK_")
        task.remainingWork = row.getWork("ZGIVENACTUALWORK_")
        task.guid = row.getUUID("ZUNIQUEID")

        val calendarID = row.getInteger("ZGIVENCALENDAR")
        if (calendarID != null) {
            val calendar = m_project!!.getCalendarByUniqueID(calendarID)
            if (calendar != null) {
                task.calendar = calendar
            }
        }

        populateConstraints(row, task)

        // Percent complete is calculated bottom up from assignments and actual work vs. planned work

        m_eventManager!!.fireTaskReadEvent(task)
    }

    /**
     * Populate the constraint type and constraint date.
     * Note that Merlin allows both start and end constraints simultaneously.
     * As we can't have both, we'll prefer the start constraint.
     *
     * @param row task data from database
     * @param task Task instance
     */
    private fun populateConstraints(row: Row, task: Task) {
        val endDateMax = row.getTimestamp("ZGIVENENDDATEMAX_")
        val endDateMin = row.getTimestamp("ZGIVENENDDATEMIN_")
        val startDateMax = row.getTimestamp("ZGIVENSTARTDATEMAX_")
        val startDateMin = row.getTimestamp("ZGIVENSTARTDATEMIN_")

        var constraintType: ConstraintType? = null
        var constraintDate: Date? = null

        if (endDateMax != null) {
            constraintType = ConstraintType.FINISH_NO_LATER_THAN
            constraintDate = endDateMax
        }

        if (endDateMin != null) {
            constraintType = ConstraintType.FINISH_NO_EARLIER_THAN
            constraintDate = endDateMin
        }

        if (endDateMin != null && endDateMin === endDateMax) {
            constraintType = ConstraintType.MUST_FINISH_ON
            constraintDate = endDateMin
        }

        if (startDateMax != null) {
            constraintType = ConstraintType.START_NO_LATER_THAN
            constraintDate = startDateMax
        }

        if (startDateMin != null) {
            constraintType = ConstraintType.START_NO_EARLIER_THAN
            constraintDate = startDateMin
        }

        if (startDateMin != null && startDateMin === endDateMax) {
            constraintType = ConstraintType.MUST_START_ON
            constraintDate = endDateMin
        }

        task.constraintType = constraintType
        task.constraintDate = constraintDate
    }

    /**
     * Read assignment data.
     */
    @Throws(SQLException::class)
    private fun processAssignments() {
        val rows = getRows("select * from zscheduleitem where zproject=? and z_ent=? order by zorderinactivity", m_projectID, m_entityMap!!["Assignment"])
        for (row in rows) {
            val task = m_project!!.getTaskByUniqueID(row.getInteger("ZACTIVITY_"))
            val resource = m_project!!.getResourceByUniqueID(row.getInteger("ZRESOURCE"))
            if (task != null && resource != null) {
                val assignment = task.addResourceAssignment(resource)
                assignment.guid = row.getUUID("ZUNIQUEID")
                assignment.actualFinish = row.getTimestamp("ZGIVENACTUALENDDATE_")
                assignment.actualStart = row.getTimestamp("ZGIVENACTUALSTARTDATE_")

                assignment.work = assignmentDuration(task, row.getWork("ZGIVENWORK_"))
                assignment.overtimeWork = assignmentDuration(task, row.getWork("ZGIVENWORKOVERTIME_"))
                assignment.actualWork = assignmentDuration(task, row.getWork("ZGIVENACTUALWORK_"))
                assignment.actualOvertimeWork = assignmentDuration(task, row.getWork("ZGIVENACTUALWORKOVERTIME_"))
                assignment.remainingWork = assignmentDuration(task, row.getWork("ZGIVENREMAININGWORK_"))

                assignment.levelingDelay = row.getDuration("ZLEVELINGDELAY_")

                if (assignment.remainingWork == null) {
                    assignment.remainingWork = assignment.work
                }

                if (resource.type == ResourceType.WORK) {
                    assignment.units = Double.valueOf(NumberHelper.getDouble(row.getDouble("ZRESOURCEUNITS_")) * 100.0)
                }
            }
        }
    }

    /**
     * Extract a duration amount from the assignment, converting a percentage
     * into an actual duration.
     *
     * @param task parent task
     * @param work duration from assignment
     * @return Duration instance
     */
    private fun assignmentDuration(task: Task, work: Duration): Duration? {
        var result: Duration? = work

        if (result != null) {
            if (result!!.getUnits() === TimeUnit.PERCENT) {
                val taskWork = task.work
                if (taskWork != null) {
                    result = Duration.getInstance(taskWork.getDuration() * result!!.getDuration(), taskWork.getUnits())
                }
            }
        }
        return result
    }

    /**
     * Read relation data.
     */
    @Throws(SQLException::class)
    private fun processDependencies() {
        val rows = getRows("select * from zdependency where zproject=?", m_projectID)
        for (row in rows) {
            val nextTask = m_project!!.getTaskByUniqueID(row.getInteger("ZNEXTACTIVITY_"))
            val prevTask = m_project!!.getTaskByUniqueID(row.getInteger("ZPREVIOUSACTIVITY_"))
            val lag = row.getDuration("ZLAG_")
            val type = row.getRelationType("ZTYPE")
            val relation = nextTask.addPredecessor(prevTask, type, lag)
            relation.uniqueID = row.getInteger("Z_PK")
        }
    }

    /**
     * Retrieve a number of rows matching the supplied query
     * which takes a single parameter.
     *
     * @param sql query statement
     * @param values bind variable values
     * @return result set
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun getRows(sql: String, vararg values: Integer): List<Row> {
        val result = LinkedList<Row>()

        m_ps = m_connection!!.prepareStatement(sql)
        var bindIndex = 1
        for (value in values) {
            m_ps!!.setInt(bindIndex++, NumberHelper.getInt(value))
        }
        m_rs = m_ps!!.executeQuery()
        populateMetaData()
        while (m_rs!!.next()) {
            result.add(SqliteResultSetRow(m_rs, m_meta))
        }

        return result
    }

    /**
     * Retrieves basic meta data from the result set.
     *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun populateMetaData() {
        m_meta.clear()

        val meta = m_rs!!.getMetaData()
        val columnCount = meta.getColumnCount() + 1
        for (loop in 1 until columnCount) {
            val name = meta.getColumnName(loop)
            val type = Integer.valueOf(meta.getColumnType(loop))
            m_meta.put(name, type)
        }
    }

    /**
     * Retrieve a node list based on an XPath expression.
     *
     * @param document XML document to process
     * @param expression compiled XPath expression
     * @return node list
     */
    @Throws(Exception::class)
    private fun getNodeList(document: String, expression: XPathExpression): NodeList {
        val doc = m_documentBuilder!!.parse(InputSource(StringReader(document)))
        return expression.evaluate(doc, XPathConstants.NODESET) as NodeList
    }
}
