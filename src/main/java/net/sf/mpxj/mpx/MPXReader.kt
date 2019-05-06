/*
 * file:       MPXReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Jan 3, 2006
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

package net.sf.mpxj.mpx

import java.io.BufferedInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.text.ParseException
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.Locale

import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.DayType
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.FileVersion
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.RecurrenceType
import net.sf.mpxj.RecurringTask
import net.sf.mpxj.Relation
import net.sf.mpxj.RelationType
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceAssignmentWorkgroupFields
import net.sf.mpxj.ResourceField
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.TaskType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.InputStreamTokenizer
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.ReaderTokenizer
import net.sf.mpxj.common.Tokenizer
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * This class creates a new ProjectFile instance by reading an MPX file.
 */
class MPXReader : AbstractProjectReader() {

    /**
     * This method returns the locale used by this MPX file.
     *
     * @return current locale
     */
    /**
     * This method sets the locale to be used by this MPX file.
     *
     * @param locale locale to be used
     */
    var locale: Locale
        get() = m_locale
        set(locale) {
            m_locale = locale
        }

    /**
     * Retrieves the flag indicating that the text version of the Task and
     * Resource Table Definition records should be ignored.
     *
     * @return Boolean flag
     */
    /**
     * This method sets the flag indicating that the text version of the
     * Task and Resource Table Definition records should be ignored. Ignoring
     * these records gets around the problem where MPX files have been generated
     * with incorrect task or resource field names, but correct task or resource
     * field numbers in the numeric version of the record.
     *
     * @param flag Boolean flag
     */
    var ignoreTextModels: Boolean
        get() = m_ignoreTextModels
        set(flag) {
            m_ignoreTextModels = flag
        }

    private var m_locale = Locale.ENGLISH
    private var m_ignoreTextModels = true

    private var m_projectFile: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_projectConfig: ProjectConfig? = null
    private var m_lastTask: Task? = null
    private var m_lastResource: Resource? = null
    private var m_lastResourceCalendar: ProjectCalendar? = null
    private var m_lastResourceAssignment: ResourceAssignment? = null
    private var m_lastBaseCalendar: ProjectCalendar? = null
    private var m_resourceTableDefinition: Boolean = false
    private var m_taskTableDefinition: Boolean = false
    private var m_taskModel: TaskModel? = null
    private var m_resourceModel: ResourceModel? = null
    private var m_delimiter: Char = ' '
    private var m_formats: MPXJFormats? = null
    private var m_deferredRelationships: List<DeferredRelationship>? = null
    private var m_projectListeners: List<ProjectListener>? = null

    /**
     * This member data is used to hold the outline level number of the
     * first outline level used in the MPX file. When data from
     * Microsoft Project is saved in MPX format, MSP creates an invisible
     * task with an outline level as zero, which acts as an umbrella
     * task for all of the other tasks defined in the file. This is not
     * a strict requirement, and an MPX file could be generated from another
     * source that only contains "visible" tasks that have outline levels
     * >= 1.
     */
    private var m_baseOutlineLevel: Int = 0

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
    override fun read(`is`: InputStream): ProjectFile {
        var line = 1

        try {
            //
            // Test the header and extract the separator. If this is successful,
            // we reset the stream back as far as we can. The design of the
            // BufferedInputStream class means that we can't get back to character
            // zero, so the first record we will read will get "PX" rather than
            // "MPX" in the first field position.
            //
            val bis = BufferedInputStream(`is`)
            val data = ByteArray(4)
            data[0] = bis.read() as Byte
            bis.mark(1024)
            data[1] = bis.read() as Byte
            data[2] = bis.read() as Byte
            data[3] = bis.read() as Byte

            if (data[0] != 'M'.toByte() || data[1] != 'P'.toByte() || data[2] != 'X'.toByte()) {
                throw MPXJException(MPXJException.INVALID_FILE)
            }

            m_projectFile = ProjectFile()
            m_eventManager = m_projectFile!!.eventManager

            m_projectConfig = m_projectFile!!.projectConfig
            m_projectConfig!!.autoTaskID = false
            m_projectConfig!!.autoTaskUniqueID = false
            m_projectConfig!!.autoResourceID = false
            m_projectConfig!!.autoResourceUniqueID = false
            m_projectConfig!!.autoOutlineLevel = false
            m_projectConfig!!.autoOutlineNumber = false
            m_projectConfig!!.autoWBS = false

            m_eventManager!!.addProjectListeners(m_projectListeners)

            LocaleUtility.setLocale(m_projectFile!!.projectProperties, m_locale)
            m_delimiter = data[3].toChar()
            m_projectFile!!.projectProperties.mpxDelimiter = m_delimiter
            m_projectFile!!.projectProperties.fileApplication = "Microsoft"
            m_projectFile!!.projectProperties.fileType = "MPX"
            m_taskModel = TaskModel(m_projectFile, m_locale)
            m_taskModel!!.setLocale(m_locale)
            m_resourceModel = ResourceModel(m_projectFile, m_locale)
            m_resourceModel!!.setLocale(m_locale)
            m_baseOutlineLevel = -1
            m_formats = MPXJFormats(m_locale, LocaleData.getString(m_locale, LocaleData.NA), m_projectFile)
            m_deferredRelationships = LinkedList<DeferredRelationship>()

            bis.reset()

            //
            // Read the file creation record. At this point we are reading
            // directly from an input stream so no character set decoding is
            // taking place. We assume that any text in this record will not
            // require decoding.
            //
            var tk: Tokenizer = InputStreamTokenizer(bis)
            tk.setDelimiter(m_delimiter)

            //
            // Add the header record
            //
            parseRecord(Integer.valueOf(MPXConstants.FILE_CREATION_RECORD_NUMBER), Record(m_locale, tk, m_formats))
            ++line

            //
            // Now process the remainder of the file in full. As we have read the
            // file creation record we have access to the field which specifies the
            // codepage used to encode the character set in this file. We set up
            // an input stream reader using the appropriate character set, and
            // create a new tokenizer to read from this Reader instance.
            //
            val reader = InputStreamReader(bis, m_projectFile!!.projectProperties.mpxCodePage.getCharset())
            tk = ReaderTokenizer(reader)
            tk.setDelimiter(m_delimiter)

            //
            // Read the remainder of the records
            //
            while (tk.type != Tokenizer.TT_EOF) {
                val record = Record(m_locale, tk, m_formats)
                val number = record.recordNumber

                if (number != null) {
                    parseRecord(number, record)
                }

                ++line
            }

            processDeferredRelationships()

            //
            // Ensure that the structure is consistent
            //
            m_projectFile!!.updateStructure()

            //
            // Ensure that the unique ID counters are correct
            //
            m_projectConfig!!.updateUniqueCounters()

            m_projectConfig!!.autoCalendarUniqueID = false

            return m_projectFile
        } catch (ex: Exception) {
            throw MPXJException(MPXJException.READ_ERROR.toString() + " (failed at line " + line + ")", ex)
        } finally {
            m_projectFile = null
            m_lastTask = null
            m_lastResource = null
            m_lastResourceCalendar = null
            m_lastResourceAssignment = null
            m_lastBaseCalendar = null
            m_resourceTableDefinition = false
            m_taskTableDefinition = false
            m_taskModel = null
            m_resourceModel = null
            m_formats = null
            m_deferredRelationships = null
        }
    }

    /**
     * Parse an MPX record.
     *
     * @param recordNumber record number
     * @param record record data
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun parseRecord(recordNumber: Integer, record: Record) {
        when (recordNumber.intValue()) {
            MPXConstants.PROJECT_NAMES_RECORD_NUMBER, MPXConstants.DDE_OLE_CLIENT_LINKS_RECORD_NUMBER, MPXConstants.COMMENTS_RECORD_NUMBER -> {
            }// silently ignored

            MPXConstants.CURRENCY_SETTINGS_RECORD_NUMBER -> {
                populateCurrencySettings(record, m_projectFile!!.projectProperties)
                m_formats!!.update()
            }

            MPXConstants.DEFAULT_SETTINGS_RECORD_NUMBER -> {
                populateDefaultSettings(record, m_projectFile!!.projectProperties)
                m_formats!!.update()
            }

            MPXConstants.DATE_TIME_SETTINGS_RECORD_NUMBER -> {
                populateDateTimeSettings(record, m_projectFile!!.projectProperties)
                m_formats!!.update()
            }

            MPXConstants.BASE_CALENDAR_RECORD_NUMBER -> {
                m_lastBaseCalendar = m_projectFile!!.addCalendar()
                populateCalendar(record, m_lastBaseCalendar, true)
            }

            MPXConstants.BASE_CALENDAR_HOURS_RECORD_NUMBER -> {
                if (m_lastBaseCalendar != null) {
                    val hours = m_lastBaseCalendar!!.addCalendarHours()
                    populateCalendarHours(record, hours)
                }
            }

            MPXConstants.BASE_CALENDAR_EXCEPTION_RECORD_NUMBER -> {
                if (m_lastBaseCalendar != null) {
                    populateCalendarException(record, m_lastBaseCalendar)
                }
            }

            MPXConstants.PROJECT_HEADER_RECORD_NUMBER -> {
                populateProjectHeader(record, m_projectFile!!.projectProperties)
                m_formats!!.update()
            }

            MPXConstants.RESOURCE_MODEL_TEXT_RECORD_NUMBER -> {
                if (m_resourceTableDefinition == false && m_ignoreTextModels == false) {
                    m_resourceModel!!.update(record, true)
                    m_resourceTableDefinition = true
                }
            }

            MPXConstants.RESOURCE_MODEL_NUMERIC_RECORD_NUMBER -> {
                if (m_resourceTableDefinition == false) {
                    m_resourceModel!!.update(record, false)
                    m_resourceTableDefinition = true
                }
            }

            MPXConstants.RESOURCE_RECORD_NUMBER -> {
                m_lastResource = m_projectFile!!.addResource()
                populateResource(m_lastResource, record)
                m_eventManager!!.fireResourceReadEvent(m_lastResource)
            }

            MPXConstants.RESOURCE_NOTES_RECORD_NUMBER -> {
                if (m_lastResource != null) {
                    m_lastResource!!.notes = record.getString(0)
                }
            }

            MPXConstants.RESOURCE_CALENDAR_RECORD_NUMBER -> {
                if (m_lastResource != null) {
                    m_lastResourceCalendar = m_lastResource!!.addResourceCalendar()
                    populateCalendar(record, m_lastResourceCalendar, false)
                }
            }

            MPXConstants.RESOURCE_CALENDAR_HOURS_RECORD_NUMBER -> {
                if (m_lastResourceCalendar != null) {
                    val hours = m_lastResourceCalendar!!.addCalendarHours()
                    populateCalendarHours(record, hours)
                }
            }

            MPXConstants.RESOURCE_CALENDAR_EXCEPTION_RECORD_NUMBER -> {
                if (m_lastResourceCalendar != null) {
                    populateCalendarException(record, m_lastResourceCalendar)
                }
            }

            MPXConstants.TASK_MODEL_TEXT_RECORD_NUMBER -> {
                if (m_taskTableDefinition == false && m_ignoreTextModels == false) {
                    m_taskModel!!.update(record, true)
                    m_taskTableDefinition = true
                }
            }

            MPXConstants.TASK_MODEL_NUMERIC_RECORD_NUMBER -> {
                if (m_taskTableDefinition == false) {
                    m_taskModel!!.update(record, false)
                    m_taskTableDefinition = true
                }
            }

            MPXConstants.TASK_RECORD_NUMBER -> {
                m_lastTask = m_projectFile!!.addTask()
                populateTask(record, m_lastTask)

                val outlineLevel = NumberHelper.getInt(m_lastTask!!.outlineLevel)

                if (m_baseOutlineLevel == -1) {
                    m_baseOutlineLevel = outlineLevel
                }

                if (outlineLevel != m_baseOutlineLevel) {
                    val childTasks = m_projectFile!!.childTasks
                    if (childTasks.isEmpty() === true) {
                        throw MPXJException(MPXJException.INVALID_OUTLINE)
                    }
                    childTasks.get(childTasks.size() - 1).addChildTask(m_lastTask, outlineLevel)
                }

                m_eventManager!!.fireTaskReadEvent(m_lastTask)
            }

            MPXConstants.TASK_NOTES_RECORD_NUMBER -> {
                if (m_lastTask != null) {
                    m_lastTask!!.notes = record.getString(0)
                }
            }

            MPXConstants.RECURRING_TASK_RECORD_NUMBER -> {
                if (m_lastTask != null) {
                    m_lastTask!!.recurring = true
                    val task = m_lastTask!!.addRecurringTask()
                    populateRecurringTask(record, task)
                }
            }

            MPXConstants.RESOURCE_ASSIGNMENT_RECORD_NUMBER -> {
                if (m_lastTask != null) {
                    m_lastResourceAssignment = m_lastTask!!.addResourceAssignment(null as Resource?)
                    populateResourceAssignment(record, m_lastResourceAssignment)
                }
            }

            MPXConstants.RESOURCE_ASSIGNMENT_WORKGROUP_FIELDS_RECORD_NUMBER -> {
                if (m_lastResourceAssignment != null) {
                    val workgroup = m_lastResourceAssignment!!.addWorkgroupAssignment()
                    populateResourceAssignmentWorkgroupFields(record, workgroup)
                }
            }

            MPXConstants.FILE_CREATION_RECORD_NUMBER -> {
                populateFileCreationRecord(record, m_projectFile!!.projectProperties)
            }

            else -> {
                throw MPXJException(MPXJException.INVALID_RECORD)
            }
        }
    }

    /**
     * Populates currency settings.
     *
     * @param record MPX record
     * @param properties project properties
     */
    private fun populateCurrencySettings(record: Record, properties: ProjectProperties) {
        properties.currencySymbol = record.getString(0)
        properties.symbolPosition = record.getCurrencySymbolPosition(1)
        properties.currencyDigits = record.getInteger(2)

        var c = record.getCharacter(3)
        if (c != null) {
            properties.thousandsSeparator = c.charValue()
        }

        c = record.getCharacter(4)
        if (c != null) {
            properties.decimalSeparator = c.charValue()
        }
    }

    /**
     * Populates default settings.
     *
     * @param record MPX record
     * @param properties project properties
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun populateDefaultSettings(record: Record, properties: ProjectProperties) {
        properties.defaultDurationUnits = record.getTimeUnit(0)
        properties.defaultDurationIsFixed = record.getNumericBoolean(1)
        properties.defaultWorkUnits = record.getTimeUnit(2)
        properties.minutesPerDay = Double.valueOf(NumberHelper.getDouble(record.getFloat(3)) * 60)
        properties.minutesPerWeek = Double.valueOf(NumberHelper.getDouble(record.getFloat(4)) * 60)
        properties.defaultStandardRate = record.getRate(5)
        properties.defaultOvertimeRate = record.getRate(6)
        properties.updatingTaskStatusUpdatesResourceStatus = record.getNumericBoolean(7)
        properties.splitInProgressTasks = record.getNumericBoolean(8)
    }

    /**
     * Populates date time settings.
     *
     * @param record MPX record
     * @param properties project properties
     */
    private fun populateDateTimeSettings(record: Record, properties: ProjectProperties) {
        properties.dateOrder = record.getDateOrder(0)
        properties.timeFormat = record.getTimeFormat(1)

        val time = getTimeFromInteger(record.getInteger(2))
        if (time != null) {
            properties.defaultStartTime = time
        }

        var c = record.getCharacter(3)
        if (c != null) {
            properties.dateSeparator = c.charValue()
        }

        c = record.getCharacter(4)
        if (c != null) {
            properties.timeSeparator = c.charValue()
        }

        properties.amText = record.getString(5)
        properties.pmText = record.getString(6)
        properties.dateFormat = record.getDateFormat(7)
        properties.barTextDateFormat = record.getDateFormat(8)
    }

    /**
     * Converts a time represented as an integer to a Date instance.
     *
     * @param time integer time
     * @return Date instance
     */
    private fun getTimeFromInteger(time: Integer?): Date? {
        var result: Date? = null

        if (time != null) {
            var minutes = time!!.intValue()
            val hours = minutes / 60
            minutes -= hours * 60

            val cal = DateHelper.popCalendar()
            cal.set(Calendar.MILLISECOND, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MINUTE, minutes)
            cal.set(Calendar.HOUR_OF_DAY, hours)
            result = cal.getTime()
            DateHelper.pushCalendar(cal)
        }

        return result
    }

    /**
     * Populates the project header.
     *
     * @param record MPX record
     * @param properties project properties
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun populateProjectHeader(record: Record, properties: ProjectProperties) {
        properties.projectTitle = record.getString(0)
        properties.company = record.getString(1)
        properties.manager = record.getString(2)
        properties.defaultCalendarName = record.getString(3)
        properties.startDate = record.getDateTime(4)
        properties.finishDate = record.getDateTime(5)
        properties.scheduleFrom = record.getScheduleFrom(6)
        properties.currentDate = record.getDateTime(7)
        properties.comments = record.getString(8)
        properties.cost = record.getCurrency(9)
        properties.baselineCost = record.getCurrency(10)
        properties.actualCost = record.getCurrency(11)
        properties.work = record.getDuration(12)
        properties.baselineWork = record.getDuration(13)
        properties.actualWork = record.getDuration(14)
        properties.work2 = record.getPercentage(15)
        properties.duration = record.getDuration(16)
        properties.baselineDuration = record.getDuration(17)
        properties.actualDuration = record.getDuration(18)
        properties.percentageComplete = record.getPercentage(19)
        properties.baselineStart = record.getDateTime(20)
        properties.baselineFinish = record.getDateTime(21)
        properties.actualStart = record.getDateTime(22)
        properties.actualFinish = record.getDateTime(23)
        properties.startVariance = record.getDuration(24)
        properties.finishVariance = record.getDuration(25)
        properties.subject = record.getString(26)
        properties.author = record.getString(27)
        properties.keywords = record.getString(28)
    }

    /**
     * Populates a calendar hours instance.
     *
     * @param record MPX record
     * @param hours calendar hours instance
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun populateCalendarHours(record: Record, hours: ProjectCalendarHours) {
        hours.day = Day.getInstance(NumberHelper.getInt(record.getInteger(0)))
        addDateRange(hours, record.getTime(1), record.getTime(2))
        addDateRange(hours, record.getTime(3), record.getTime(4))
        addDateRange(hours, record.getTime(5), record.getTime(6))
    }

    /**
     * Get a date range that correctly handles the case where the end time
     * is midnight. In this instance the end time should be the start of the
     * next day.
     *
     * @param hours calendar hours
     * @param start start date
     * @param end end date
     */
    private fun addDateRange(hours: ProjectCalendarHours, start: Date?, end: Date?) {
        var end = end
        if (start != null && end != null) {
            val cal = DateHelper.popCalendar(end)
            // If the time ends on midnight, the date should be the next day. Otherwise problems occur.
            if (cal.get(Calendar.HOUR_OF_DAY) === 0 && cal.get(Calendar.MINUTE) === 0 && cal.get(Calendar.SECOND) === 0 && cal.get(Calendar.MILLISECOND) === 0) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            end = cal.getTime()
            DateHelper.pushCalendar(cal)

            hours.addRange(DateRange(start, end))
        }
    }

    /**
     * Populates a calendar exception instance.
     *
     * @param record MPX record
     * @param calendar calendar to which the exception will be added
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun populateCalendarException(record: Record, calendar: ProjectCalendar) {
        val fromDate = record.getDate(0)
        var toDate = record.getDate(1)
        val working = record.getNumericBoolean(2)

        // I have found an example MPX file where a single day exception is expressed with just the start date set.
        // If we find this for we assume that the end date is the same as the start date.
        if (fromDate != null && toDate == null) {
            toDate = fromDate
        }

        val exception = calendar.addCalendarException(fromDate, toDate)
        if (working) {
            addExceptionRange(exception, record.getTime(3), record.getTime(4))
            addExceptionRange(exception, record.getTime(5), record.getTime(6))
            addExceptionRange(exception, record.getTime(7), record.getTime(8))
        }
    }

    /**
     * Add a range to an exception, ensure that we don't try to add null ranges.
     *
     * @param exception target exception
     * @param start exception start
     * @param finish exception finish
     */
    private fun addExceptionRange(exception: ProjectCalendarException, start: Date?, finish: Date?) {
        if (start != null && finish != null) {
            exception.addRange(DateRange(start, finish))
        }
    }

    /**
     * Populates a calendar instance.
     *
     * @param record MPX record
     * @param calendar calendar instance
     * @param isBaseCalendar true if this is a base calendar
     */
    private fun populateCalendar(record: Record, calendar: ProjectCalendar?, isBaseCalendar: Boolean) {
        if (isBaseCalendar == true) {
            calendar!!.name = record.getString(0)
        } else {
            calendar!!.parent = m_projectFile!!.getCalendarByName(record.getString(0))
        }

        calendar.setWorkingDay(Day.SUNDAY, DayType.getInstance(record.getInteger(1)))
        calendar.setWorkingDay(Day.MONDAY, DayType.getInstance(record.getInteger(2)))
        calendar.setWorkingDay(Day.TUESDAY, DayType.getInstance(record.getInteger(3)))
        calendar.setWorkingDay(Day.WEDNESDAY, DayType.getInstance(record.getInteger(4)))
        calendar.setWorkingDay(Day.THURSDAY, DayType.getInstance(record.getInteger(5)))
        calendar.setWorkingDay(Day.FRIDAY, DayType.getInstance(record.getInteger(6)))
        calendar.setWorkingDay(Day.SATURDAY, DayType.getInstance(record.getInteger(7)))

        m_eventManager!!.fireCalendarReadEvent(calendar)
    }

    /**
     * Populates a resource.
     *
     * @param resource resource instance
     * @param record MPX record
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun populateResource(resource: Resource?, record: Record) {
        val falseText = LocaleData.getString(m_locale, LocaleData.NO)

        val length = record.length
        val model = m_resourceModel!!.model

        for (i in 0 until length) {
            val mpxFieldType = model[i]
            if (mpxFieldType == -1) {
                break
            }

            val field = record.getString(i)

            if (field == null || field.length() === 0) {
                continue
            }

            val resourceField = MPXResourceField.getMpxjField(mpxFieldType)
            when (resourceField) {
                ResourceField.OBJECTS -> {
                    resource!!.set(resourceField, record.getInteger(i))
                }

                ResourceField.ID -> {
                    resource!!.id = record.getInteger(i)
                }

                ResourceField.UNIQUE_ID -> {
                    resource!!.uniqueID = record.getInteger(i)
                }

                ResourceField.MAX_UNITS -> {
                    resource!!.set(resourceField, record.getUnits(i))
                }

                ResourceField.PERCENT_WORK_COMPLETE, ResourceField.PEAK -> {
                    resource!!.set(resourceField, record.getPercentage(i))
                }

                ResourceField.COST, ResourceField.COST_PER_USE, ResourceField.COST_VARIANCE, ResourceField.BASELINE_COST, ResourceField.ACTUAL_COST, ResourceField.REMAINING_COST -> {
                    resource!!.set(resourceField, record.getCurrency(i))
                }

                ResourceField.OVERTIME_RATE, ResourceField.STANDARD_RATE -> {
                    resource!!.set(resourceField, record.getRate(i))
                }

                ResourceField.REMAINING_WORK, ResourceField.OVERTIME_WORK, ResourceField.BASELINE_WORK, ResourceField.ACTUAL_WORK, ResourceField.WORK, ResourceField.WORK_VARIANCE -> {
                    resource!!.set(resourceField, record.getDuration(i))
                }

                ResourceField.ACCRUE_AT -> {
                    resource!!.set(resourceField, record.getAccrueType(i))
                }

                ResourceField.LINKED_FIELDS, ResourceField.OVERALLOCATED -> {
                    resource!!.set(resourceField, record.getBoolean(i, falseText))
                }

                else -> {
                    resource!!.set(resourceField, field)
                }
            }
        }

        if (m_projectConfig!!.autoResourceUniqueID == true) {
            resource!!.uniqueID = Integer.valueOf(m_projectConfig!!.nextResourceUniqueID)
        }

        if (m_projectConfig!!.autoResourceID == true) {
            resource!!.id = Integer.valueOf(m_projectConfig!!.nextResourceID)
        }

        //
        // Handle malformed MPX files - ensure we have a unique ID
        //
        if (resource!!.uniqueID == null) {
            resource.uniqueID = resource.id
        }
    }

    /**
     * Populates a relation list.
     *
     * @param task parent task
     * @param field target task field
     * @param data MPX relation list data
     */
    private fun populateRelationList(task: Task?, field: TaskField, data: String) {
        val dr = DeferredRelationship()
        dr.task = task
        dr.field = field
        dr.data = data
        m_deferredRelationships!!.add(dr)
    }

    /**
     * This method iterates through the deferred relationships,
     * parsing the data and setting up relationships between tasks.
     *
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun processDeferredRelationships() {
        for (dr in m_deferredRelationships!!) {
            processDeferredRelationship(dr)
        }
    }

    /**
     * This method processes a single deferred relationship list.
     *
     * @param dr deferred relationship list data
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun processDeferredRelationship(dr: DeferredRelationship) {
        val data = dr.data
        val task = dr.task

        val length = data!!.length()

        if (length != 0) {
            var start = 0
            var end = 0

            while (end != length) {
                end = data.indexOf(m_delimiter, start)

                if (end == -1) {
                    end = length
                }

                populateRelation(dr.field, task, data.substring(start, end).trim())

                start = end + 1
            }
        }
    }

    /**
     * Creates and populates a new task relationship.
     *
     * @param field which task field source of data
     * @param sourceTask relationship source task
     * @param relationship relationship string
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun populateRelation(field: TaskField?, sourceTask: Task?, relationship: String) {
        var index = 0
        val length = relationship.length()

        //
        // Extract the identifier
        //
        while (index < length && Character.isDigit(relationship.charAt(index)) === true) {
            ++index
        }

        val taskID: Integer
        try {
            taskID = Integer.valueOf(relationship.substring(0, index))
        } catch (ex: NumberFormatException) {
            throw MPXJException(MPXJException.INVALID_FORMAT.toString() + " '" + relationship + "'")
        }

        //
        // Now find the task, so we can extract the unique ID
        //
        val targetTask: Task?
        if (field === TaskField.PREDECESSORS) {
            targetTask = m_projectFile!!.getTaskByID(taskID)
        } else {
            targetTask = m_projectFile!!.getTaskByUniqueID(taskID)
        }

        //
        // If we haven't reached the end, we next expect to find
        // SF, SS, FS, FF
        //
        var type: RelationType? = null
        var lag: Duration? = null

        if (index == length) {
            type = RelationType.FINISH_START
            lag = Duration.getInstance(0, TimeUnit.DAYS)
        } else {
            if (index + 1 == length) {
                throw MPXJException(MPXJException.INVALID_FORMAT.toString() + " '" + relationship + "'")
            }

            type = RelationTypeUtility.getInstance(m_locale, relationship.substring(index, index + 2))

            index += 2

            if (index == length) {
                lag = Duration.getInstance(0, TimeUnit.DAYS)
            } else {
                if (relationship.charAt(index) === '+') {
                    ++index
                }

                lag = DurationUtility.getInstance(relationship.substring(index), m_formats!!.durationDecimalFormat, m_locale)
            }
        }

        if (type == null) {
            throw MPXJException(MPXJException.INVALID_FORMAT.toString() + " '" + relationship + "'")
        }

        // We have seen at least one example MPX file where an invalid task ID
        // is present. We'll ignore this as the schedule is otherwise valid.
        if (targetTask != null) {
            val relation = sourceTask!!.addPredecessor(targetTask, type, lag)
            m_eventManager!!.fireRelationReadEvent(relation)
        }
    }

    /**
     * Populates a task instance.
     *
     * @param record MPX record
     * @param task task instance
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun populateTask(record: Record, task: Task?) {
        val falseText = LocaleData.getString(m_locale, LocaleData.NO)

        var mpxFieldID = 0
        var field: String?

        var i = 0
        val length = record.length
        val model = m_taskModel!!.model

        while (i < length) {
            mpxFieldID = model[i]

            if (mpxFieldID == -1) {
                break
            }

            field = record.getString(i++)

            if (field == null || field.length() === 0) {
                continue
            }

            val taskField = MPXTaskField.getMpxjField(mpxFieldID)
            if (taskField == null) {
                System.out.println("Null Task Field $mpxFieldID")
                continue
            }

            when (taskField) {
                PREDECESSORS, UNIQUE_ID_PREDECESSORS -> {
                    populateRelationList(task, taskField, field)
                }

                PERCENT_COMPLETE, PERCENT_WORK_COMPLETE -> {
                    try {
                        task!!.set(taskField, m_formats!!.percentageDecimalFormat.parse(field))
                    } catch (ex: ParseException) {
                        throw MPXJException("Failed to parse percentage", ex)
                    }

                }

                ACTUAL_COST, BASELINE_COST, BCWP, BCWS, COST, COST1, COST2, COST3, COST_VARIANCE, CV, FIXED_COST, REMAINING_COST, SV -> {
                    try {
                        task!!.set(taskField, m_formats!!.currencyFormat.parse(field))
                    } catch (ex: ParseException) {
                        throw MPXJException("Failed to parse currency", ex)
                    }

                }

                ACTUAL_DURATION, ACTUAL_WORK, BASELINE_DURATION, BASELINE_WORK, DURATION, DURATION1, DURATION2, DURATION3, DURATION_VARIANCE, FINISH_VARIANCE, FREE_SLACK, REMAINING_DURATION, REMAINING_WORK, START_VARIANCE, TOTAL_SLACK, WORK, WORK_VARIANCE, LEVELING_DELAY -> {
                    task!!.set(taskField, DurationUtility.getInstance(field, m_formats!!.durationDecimalFormat, m_locale))
                }

                ACTUAL_FINISH, ACTUAL_START, BASELINE_FINISH, BASELINE_START, CONSTRAINT_DATE, CREATED, EARLY_FINISH, EARLY_START, FINISH, FINISH1, FINISH2, FINISH3, FINISH4, FINISH5, LATE_FINISH, LATE_START, RESUME, START, START1, START2, START3, START4, START5, STOP -> {
                    try {
                        task!!.set(taskField, m_formats!!.dateTimeFormat.parse(field))
                    } catch (ex: ParseException) {
                        throw MPXJException("Failed to parse date time", ex)
                    }

                }

                CONFIRMED, CRITICAL, FLAG1, FLAG2, FLAG3, FLAG4, FLAG5, FLAG6, FLAG7, FLAG8, FLAG9, FLAG10, HIDE_BAR, LINKED_FIELDS, MARKED, MILESTONE, ROLLUP, SUMMARY, UPDATE_NEEDED -> {
                    task!!.set(taskField, if (field.equalsIgnoreCase(falseText) === true) Boolean.FALSE else Boolean.TRUE)
                }

                CONSTRAINT_TYPE -> {
                    task!!.set(taskField, ConstraintTypeUtility.getInstance(m_locale, field))
                }

                OBJECTS, OUTLINE_LEVEL -> {
                    task!!.set(taskField, Integer.valueOf(field))
                }

                ID -> {
                    task!!.id = Integer.valueOf(field)
                }

                UNIQUE_ID -> {
                    task!!.uniqueID = Integer.valueOf(field)
                }

                NUMBER1, NUMBER2, NUMBER3, NUMBER4, NUMBER5 -> {
                    try {
                        task!!.set(taskField, m_formats!!.decimalFormat.parse(field))
                    } catch (ex: ParseException) {
                        throw MPXJException("Failed to parse number", ex)
                    }

                }

                PRIORITY -> {
                    task!!.set(taskField, PriorityUtility.getInstance(m_locale, field))
                }

                TYPE -> {
                    val fixed = !field.equalsIgnoreCase(falseText)
                    task!!.type = if (fixed) TaskType.FIXED_DURATION else TaskType.FIXED_UNITS
                }

                else -> {
                    task!!.set(taskField, field)
                }
            }
        }

        if (m_projectConfig!!.autoWBS == true) {
            task!!.generateWBS(null)
        }

        if (m_projectConfig!!.autoOutlineNumber == true) {
            task!!.generateOutlineNumber(null)
        }

        if (m_projectConfig!!.autoOutlineLevel == true) {
            task!!.outlineLevel = Integer.valueOf(1)
        }

        if (m_projectConfig!!.autoTaskUniqueID == true) {
            task!!.uniqueID = Integer.valueOf(m_projectConfig!!.nextTaskUniqueID)
        }

        if (task!!.id == null || m_projectConfig!!.autoTaskID == true) {
            task.id = Integer.valueOf(m_projectConfig!!.nextTaskID)
        }

        //
        // Handle malformed MPX files - ensure we have a unique ID
        //
        if (task.uniqueID == null) {
            task.uniqueID = task.id
        }

        //
        // Some applications (I'm looking at you SureTrak) don't write start and finish
        // attributes. If you open an MPX file like this in MS Project, it will use
        // the early start and early finish values (if present) to populate
        // the start and finish attributes.

        if (task.start == null && task.earlyStart != null) {
            task.start = task.earlyStart
        }

        if (task.finish == null && task.earlyFinish != null) {
            task.finish = task.earlyFinish
        }
    }

    /**
     * Populates a recurring task.
     *
     * @param record MPX record
     * @param task recurring task
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun populateRecurringTask(record: Record, task: RecurringTask) {
        //System.out.println(record);
        task.startDate = record.getDateTime(1)
        task.finishDate = record.getDateTime(2)
        task.duration = RecurrenceUtility.getDuration(m_projectFile!!.projectProperties, record.getInteger(3), record.getInteger(4))
        task.occurrences = record.getInteger(5)
        task.recurrenceType = RecurrenceUtility.getRecurrenceType(record.getInteger(6))
        task.useEndDate = NumberHelper.getInt(record.getInteger(8)) == 1
        task.isWorkingDaysOnly = NumberHelper.getInt(record.getInteger(9)) == 1
        task.setWeeklyDaysFromBitmap(RecurrenceUtility.getDays(record.getString(10)), RecurrenceUtility.RECURRING_TASK_DAY_MASKS)

        val type = task.recurrenceType
        if (type != null) {
            when (task.recurrenceType) {
                RecurrenceType.DAILY -> {
                    task.frequency = record.getInteger(13)
                }

                RecurrenceType.WEEKLY -> {
                    task.frequency = record.getInteger(14)
                }

                RecurrenceType.MONTHLY -> {
                    task.relative = NumberHelper.getInt(record.getInteger(11)) == 1
                    if (task.relative) {
                        task.frequency = record.getInteger(17)
                        task.dayNumber = record.getInteger(15)
                        task.dayOfWeek = RecurrenceUtility.getDay(record.getInteger(16))
                    } else {
                        task.frequency = record.getInteger(19)
                        task.dayNumber = record.getInteger(18)
                    }
                }

                RecurrenceType.YEARLY -> {
                    task.relative = NumberHelper.getInt(record.getInteger(12)) != 1
                    if (task.relative) {
                        task.dayNumber = record.getInteger(20)
                        task.dayOfWeek = RecurrenceUtility.getDay(record.getInteger(21))
                        task.monthNumber = record.getInteger(22)
                    } else {
                        task.setYearlyAbsoluteFromDate(record.getDateTime(23))
                    }
                }
            }
        }

        //System.out.println(task);
    }

    /**
     * Populate a resource assignment.
     *
     * @param record MPX record
     * @param assignment resource assignment
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun populateResourceAssignment(record: Record, assignment: ResourceAssignment?) {
        //
        // Handle malformed MPX files - ensure that we can locate the resource
        // using either the Unique ID attribute or the ID attribute.
        //
        var resource: Resource? = m_projectFile!!.getResourceByUniqueID(record.getInteger(12))
        if (resource == null) {
            resource = m_projectFile!!.getResourceByID(record.getInteger(0))
        }

        assignment!!.units = record.getUnits(1)
        assignment.work = record.getDuration(2)
        assignment.baselineWork = record.getDuration(3)
        assignment.actualWork = record.getDuration(4)
        assignment.overtimeWork = record.getDuration(5)
        assignment.cost = record.getCurrency(6)
        assignment.baselineCost = record.getCurrency(7)
        assignment.actualCost = record.getCurrency(8)
        assignment.start = record.getDateTime(9)
        assignment.finish = record.getDateTime(10)
        assignment.delay = record.getDuration(11)

        //
        // Calculate the remaining work
        //
        val work = assignment.work
        var actualWork = assignment.actualWork
        if (work != null && actualWork != null) {
            if (work.getUnits() !== actualWork!!.getUnits()) {
                actualWork = actualWork.convertUnits(work.getUnits(), m_projectFile!!.projectProperties)
            }

            assignment.remainingWork = Duration.getInstance(work.getDuration() - actualWork.getDuration(), work.getUnits())
        }

        if (resource != null) {
            assignment.resourceUniqueID = resource.uniqueID
            resource.addResourceAssignment(assignment)
        }

        m_eventManager!!.fireAssignmentReadEvent(assignment)
    }

    /**
     * Populate a resource assignment workgroup instance.
     *
     * @param record MPX record
     * @param workgroup workgroup instance
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    private fun populateResourceAssignmentWorkgroupFields(record: Record, workgroup: ResourceAssignmentWorkgroupFields) {
        workgroup.messageUniqueID = record.getString(0)
        workgroup.confirmed = NumberHelper.getInt(record.getInteger(1)) == 1
        workgroup.responsePending = NumberHelper.getInt(record.getInteger(1)) == 1
        workgroup.updateStart = record.getDateTime(3)
        workgroup.updateFinish = record.getDateTime(4)
        workgroup.scheduleID = record.getString(5)
    }

    /**
     * Transient working data.
     */

    /**
     * This class is used to collect relationship data awaiting
     * deferred processing. We do this to allow forward references
     * between tasks.
     */
    protected class DeferredRelationship {

        /**
         * Retrieve the parent task.
         *
         * @return parent Task instance
         */
        /**
         * Set the parent task instance.
         *
         * @param task parent Task instance
         */
        var task: Task? = null
        /**
         * Retrieve the target task field.
         *
         * @return TaskField instance
         */
        /**
         * Set the target task field.
         *
         * @param field TaskField instance
         */
        var field: TaskField? = null
        /**
         * Retrieve the relationship data.
         *
         * @return relationship data
         */
        /**
         * Set the relationship data.
         *
         * @param data relationship data
         */
        var data: String? = null
    }

    companion object {

        /**
         * Populate a file creation record.
         *
         * @param record MPX record
         * @param properties project properties
         */
        internal fun populateFileCreationRecord(record: Record, properties: ProjectProperties) {
            properties.mpxProgramName = record.getString(0)
            properties.mpxFileVersion = FileVersion.getInstance(record.getString(1))
            properties.mpxCodePage = record.getCodePage(2)
        }

        /**
         * Retrieves an array of locales supported by this class.
         *
         * @return array of supported locales
         */
        val supportedLocales: Array<Locale>
            get() = LocaleUtility.supportedLocales
    }
}
