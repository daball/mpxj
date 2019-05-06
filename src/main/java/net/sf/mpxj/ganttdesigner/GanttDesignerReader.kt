/*
 * file:       GanttDesignerReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2019
 * date:       10 February 2019
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

package net.sf.mpxj.ganttdesigner

import java.io.InputStream
import java.util.HashMap
import java.util.LinkedList

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
import net.sf.mpxj.EventManager
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectCalendarWeek
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.RelationType
import net.sf.mpxj.Task
import net.sf.mpxj.ganttdesigner.schema.Gantt
import net.sf.mpxj.ganttdesigner.schema.GanttDesignerRemark
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * This class creates a new ProjectFile instance by reading a GanttDesigner file.
 */
class GanttDesignerReader : AbstractProjectReader() {

    private var m_projectFile: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_projectListeners: List<ProjectListener>? = null
    internal var m_taskMap: Map<String, Task>? = null
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
            m_taskMap = HashMap<String, Task>()

            val config = m_projectFile!!.projectConfig
            config.autoWBS = false

            m_projectFile!!.projectProperties.fileApplication = "GanttDesigner"
            m_projectFile!!.projectProperties.fileType = "GNT"

            m_eventManager!!.addProjectListeners(m_projectListeners)

            val factory = SAXParserFactory.newInstance()
            val saxParser = factory.newSAXParser()
            val xmlReader = saxParser.getXMLReader()
            val doc = SAXSource(xmlReader, InputSource(stream))

            if (CONTEXT == null) {
                throw CONTEXT_EXCEPTION
            }

            val unmarshaller = CONTEXT!!.createUnmarshaller()

            val gantt = unmarshaller.unmarshal(doc) as Gantt

            readProjectProperties(gantt)
            readCalendar(gantt)
            readTasks(gantt)

            return m_projectFile
        } catch (ex: ParserConfigurationException) {
            throw MPXJException("Failed to parse file", ex)
        } catch (ex: JAXBException) {
            throw MPXJException("Failed to parse file", ex)
        } catch (ex: SAXException) {
            throw MPXJException("Failed to parse file", ex)
        } finally {
            m_projectFile = null
            m_eventManager = null
            m_projectListeners = null
            m_taskMap = null
        }
    }

    /**
     * Read project properties.
     *
     * @param gantt Gantt Designer file
     */
    private fun readProjectProperties(gantt: Gantt) {
        val file = gantt.file
        val props = m_projectFile!!.projectProperties
        props.lastSaved = file.saved
        props.creationDate = file.created
        props.name = file.name
    }

    /**
     * Read the calendar data from a Gantt Designer file.
     *
     * @param gantt Gantt Designer file.
     */
    private fun readCalendar(gantt: Gantt) {
        val ganttCalendar = gantt.calendar
        m_projectFile!!.projectProperties.weekStartDay = ganttCalendar.weekStart

        val calendar = m_projectFile!!.addCalendar()
        calendar.name = "Standard"
        m_projectFile!!.defaultCalendar = calendar

        val workingDays = ganttCalendar.workDays
        calendar.setWorkingDay(Day.SUNDAY, workingDays.charAt(0) === '1')
        calendar.setWorkingDay(Day.MONDAY, workingDays.charAt(1) === '1')
        calendar.setWorkingDay(Day.TUESDAY, workingDays.charAt(2) === '1')
        calendar.setWorkingDay(Day.WEDNESDAY, workingDays.charAt(3) === '1')
        calendar.setWorkingDay(Day.THURSDAY, workingDays.charAt(4) === '1')
        calendar.setWorkingDay(Day.FRIDAY, workingDays.charAt(5) === '1')
        calendar.setWorkingDay(Day.SATURDAY, workingDays.charAt(6) === '1')

        for (i in 1..7) {
            val day = Day.getInstance(i)
            val hours = calendar.addCalendarHours(day)
            if (calendar.isWorkingDay(day)) {
                hours.addRange(ProjectCalendarWeek.DEFAULT_WORKING_MORNING)
                hours.addRange(ProjectCalendarWeek.DEFAULT_WORKING_AFTERNOON)
            }
        }

        for (holiday in gantt.holidays.holiday) {
            val exception = calendar.addCalendarException(holiday.date, holiday.date)
            exception.name = holiday.content
        }
    }

    /**
     * Read task data from a Gantt Designer file.
     *
     * @param gantt Gantt Designer file
     */
    private fun readTasks(gantt: Gantt) {
        processTasks(gantt)
        processPredecessors(gantt)
        processRemarks(gantt)
    }

    /**
     * Read task data from a Gantt Designer file.
     *
     * @param gantt Gantt Designer file
     */
    private fun processTasks(gantt: Gantt) {
        val calendar = m_projectFile!!.defaultCalendar

        for (ganttTask in gantt.tasks.task) {
            val wbs = ganttTask.id
            val parentTask = getParentTask(wbs)

            val task = parentTask!!.addTask()
            //ganttTask.getB() // bar type
            //ganttTask.getBC() // bar color
            task.cost = ganttTask.c
            task.name = ganttTask.content
            task.duration = ganttTask.d
            task.deadline = ganttTask.dl
            //ganttTask.getH() // height
            //ganttTask.getIn(); // indent
            task.wbs = wbs
            task.percentageComplete = ganttTask.pc
            task.start = ganttTask.s
            //ganttTask.getU(); // Unknown
            //ganttTask.getVA(); // Valign

            task.finish = calendar!!.getDate(task.start, task.duration!!, false)
            m_taskMap!!.put(wbs, task)
        }
    }

    /**
     * Read predecessors from a Gantt Designer file.
     *
     * @param gantt Gantt Designer file
     */
    private fun processPredecessors(gantt: Gantt) {
        for (ganttTask in gantt.tasks.task) {
            val predecessors = ganttTask.p
            if (predecessors != null && !predecessors!!.isEmpty()) {
                val wbs = ganttTask.id
                val task = m_taskMap!![wbs]
                for (predecessor in predecessors!!.split(";")) {
                    val predecessorTask = m_projectFile!!.getTaskByID(Integer.valueOf(predecessor))
                    task.addPredecessor(predecessorTask, RelationType.FINISH_START, ganttTask.l)
                }
            }
        }
    }

    /**
     * Read remarks from a Gantt Designer file.
     *
     * @param gantt Gantt Designer file
     */
    private fun processRemarks(gantt: Gantt) {
        processRemarks(gantt.remarks)
        processRemarks(gantt.remarks1)
        processRemarks(gantt.remarks2)
        processRemarks(gantt.remarks3)
        processRemarks(gantt.remarks4)
    }

    /**
     * Read an individual remark type from a Gantt Designer file.
     *
     * @param remark remark type
     */
    private fun processRemarks(remark: GanttDesignerRemark) {
        for (remarkTask in remark.task) {
            val id = remarkTask.row
            val task = m_projectFile!!.getTaskByID(id)
            var notes = task.notes
            if (notes.isEmpty()) {
                notes = remarkTask.content
            } else {
                notes = notes.toInt() + '\n'.toInt() + remarkTask.content.toInt()
            }
            task.notes = notes
        }
    }

    /**
     * Extract the parent WBS from a WBS.
     *
     * @param wbs current WBS
     * @return parent WBS
     */
    private fun getParentWBS(wbs: String): String? {
        val result: String?
        val index = wbs.lastIndexOf('.')
        if (index == -1) {
            result = null
        } else {
            result = wbs.substring(0, index)
        }
        return result
    }

    /**
     * Retrieve the parent task based on its WBS.
     *
     * @param wbs parent WBS
     * @return parent task
     */
    private fun getParentTask(wbs: String): ChildTaskContainer? {
        val result: ChildTaskContainer?
        val parentWbs = getParentWBS(wbs)
        if (parentWbs == null) {
            result = m_projectFile
        } else {
            result = m_taskMap!![parentWbs]
        }
        return result
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
                CONTEXT = JAXBContext.newInstance("net.sf.mpxj.ganttdesigner.schema", GanttDesignerReader::class.java!!.getClassLoader())
            } catch (ex: JAXBException) {
                CONTEXT_EXCEPTION = ex
                CONTEXT = null
            }

        }
    }
}
