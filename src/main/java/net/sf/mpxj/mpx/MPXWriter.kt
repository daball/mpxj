/*
 * file:       MPXWriter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       03/01/2006
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

import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.Calendar
import java.util.Date
import java.util.Locale

import net.sf.mpxj.AccrueType
import net.sf.mpxj.ConstraintType
import net.sf.mpxj.DataType
import net.sf.mpxj.DateRange
import net.sf.mpxj.Day
import net.sf.mpxj.DayType
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.Priority
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectCalendarException
import net.sf.mpxj.ProjectCalendarHours
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Rate
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
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.writer.AbstractProjectWriter

/**
 * This class creates a new MPX file from the contents of
 * a ProjectFile instance.
 */
class MPXWriter : AbstractProjectWriter() {

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
    var locale: Locale?
        get() = m_locale
        set(locale) {
            m_locale = locale
        }

    /**
     * Retrieves an array of locales supported by this class.
     *
     * @return array of supported locales
     */
    val supportedLocales: Array<Locale>
        get() = LocaleUtility.supportedLocales

    private var m_projectFile: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_writer: OutputStreamWriter? = null
    private var m_resourceModel: ResourceModel? = null
    private var m_taskModel: TaskModel? = null
    private var m_delimiter: Char = ' '
    private var m_locale = Locale.ENGLISH
    /**
     * Retrieves a flag indicating if the default settings for the locale should
     * override any project settings.
     *
     * @return boolean flag.
     */
    /**
     * Sets a flag indicating if the default settings for the locale should
     * override any project settings.
     *
     * @param useLocaleDefaults boolean flag
     */
    var useLocaleDefaults = true
    private var m_buffer: StringBuilder? = null
    private var m_formats: MPXJFormats? = null
    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(IOException::class)
    override fun write(projectFile: ProjectFile, out: OutputStream) {
        m_projectFile = projectFile
        m_eventManager = projectFile.eventManager

        if (useLocaleDefaults == true) {
            LocaleUtility.setLocale(m_projectFile!!.projectProperties, m_locale)
        }

        m_delimiter = projectFile.projectProperties.mpxDelimiter
        m_writer = OutputStreamWriter(BufferedOutputStream(out), projectFile.projectProperties.mpxCodePage.getCharset())
        m_buffer = StringBuilder()
        m_formats = MPXJFormats(m_locale, LocaleData.getString(m_locale, LocaleData.NA), m_projectFile)

        try {
            write()
        } finally {
            m_writer = null
            m_projectFile = null
            m_resourceModel = null
            m_taskModel = null
            m_buffer = null
            m_locale = null
            m_formats = null
        }
    }

    /**
     * Writes the contents of the project file as MPX records.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun write() {
        m_projectFile!!.validateUniqueIDsForMicrosoftProject()

        writeFileCreationRecord()
        writeProjectHeader(m_projectFile!!.projectProperties)

        if (m_projectFile!!.resources.isEmpty() === false) {
            m_resourceModel = ResourceModel(m_projectFile, m_locale)
            m_writer!!.write(m_resourceModel!!.toString())
            for (resource in m_projectFile!!.resources) {
                writeResource(resource)
            }
        }

        if (m_projectFile!!.tasks.isEmpty() === false) {
            m_taskModel = TaskModel(m_projectFile, m_locale)
            m_writer!!.write(m_taskModel!!.toString())
            writeTasks(m_projectFile!!.childTasks)
        }

        m_writer!!.flush()
    }

    /**
     * Write file creation record.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeFileCreationRecord() {
        val properties = m_projectFile!!.projectProperties

        m_buffer!!.setLength(0)
        m_buffer!!.append("MPX")
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(properties.mpxProgramName)
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(properties.mpxFileVersion)
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(properties.mpxCodePage)
        m_buffer!!.append(MPXConstants.EOL)
        m_writer!!.write(m_buffer!!.toString())
    }

    /**
     * Write project header.
     *
     * @param properties project properties
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeProjectHeader(properties: ProjectProperties) {
        m_buffer!!.setLength(0)

        //
        // Currency Settings Record
        //
        m_buffer!!.append(MPXConstants.CURRENCY_SETTINGS_RECORD_NUMBER)
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.currencySymbol))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.symbolPosition))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.currencyDigits))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(Character.valueOf(properties.thousandsSeparator)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(Character.valueOf(properties.decimalSeparator)))
        stripTrailingDelimiters(m_buffer!!)
        m_buffer!!.append(MPXConstants.EOL)

        //
        // Default Settings Record
        //
        m_buffer!!.append(MPXConstants.DEFAULT_SETTINGS_RECORD_NUMBER)
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(Integer.valueOf(properties.defaultDurationUnits.getValue())))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(if (properties.defaultDurationIsFixed) "1" else "0")
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(Integer.valueOf(properties.defaultWorkUnits.getValue())))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDecimal(NumberHelper.getDouble(properties.minutesPerDay) / 60)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDecimal(NumberHelper.getDouble(properties.minutesPerWeek) / 60)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatRate(properties.defaultStandardRate)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatRate(properties.defaultOvertimeRate)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(if (properties.updatingTaskStatusUpdatesResourceStatus) "1" else "0")
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(if (properties.splitInProgressTasks) "1" else "0")
        stripTrailingDelimiters(m_buffer!!)
        m_buffer!!.append(MPXConstants.EOL)

        //
        // Date Time Settings Record
        //
        m_buffer!!.append(MPXConstants.DATE_TIME_SETTINGS_RECORD_NUMBER)
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.dateOrder))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.timeFormat))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(getIntegerTimeInMinutes(properties.defaultStartTime)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(Character.valueOf(properties.dateSeparator)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(Character.valueOf(properties.timeSeparator)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.amText))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.pmText))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.dateFormat))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.barTextDateFormat))
        stripTrailingDelimiters(m_buffer!!)
        m_buffer!!.append(MPXConstants.EOL)
        m_writer!!.write(m_buffer!!.toString())

        //
        // Write project calendars
        //
        for (cal in m_projectFile!!.calendars) {
            writeCalendar(cal)
        }

        //
        // Project Header Record
        //
        m_buffer!!.setLength(0)
        m_buffer!!.append(MPXConstants.PROJECT_HEADER_RECORD_NUMBER)
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.projectTitle))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.company))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.manager))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.defaultCalendarName))
        m_buffer!!.append(m_delimiter)

        m_buffer!!.append(format(formatDateTime(properties.startDate)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDateTime(properties.finishDate)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.scheduleFrom))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDateTime(properties.currentDate)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.comments))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatCurrency(properties.cost)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatCurrency(properties.baselineCost)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatCurrency(properties.actualCost)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(properties.work)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(properties.baselineWork)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(properties.actualWork)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatPercentage(properties.work2)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(properties.duration)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(properties.baselineDuration)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(properties.actualDuration)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatPercentage(properties.percentageComplete)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDateTime(properties.baselineStart)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDateTime(properties.baselineFinish)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDateTime(properties.actualStart)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDateTime(properties.actualFinish)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(properties.startVariance)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(properties.finishVariance)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.subject))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.author))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(properties.keywords))
        stripTrailingDelimiters(m_buffer!!)
        m_buffer!!.append(MPXConstants.EOL)

        m_writer!!.write(m_buffer!!.toString())
    }

    /**
     * Write a calendar.
     *
     * @param record calendar instance
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeCalendar(record: ProjectCalendar) {
        //
        // Test used to ensure that we don't write the default calendar used for the "Unassigned" resource
        //
        if (record.parent == null || record.resource != null) {
            m_buffer!!.setLength(0)

            if (record.parent == null) {
                m_buffer!!.append(MPXConstants.BASE_CALENDAR_RECORD_NUMBER)
                m_buffer!!.append(m_delimiter)
                if (record.name != null) {
                    m_buffer!!.append(record.name)
                }
            } else {
                m_buffer!!.append(MPXConstants.RESOURCE_CALENDAR_RECORD_NUMBER)
                m_buffer!!.append(m_delimiter)
                m_buffer!!.append(record.parent!!.name)
            }

            for (day in record.days) {
                if (day == null) {
                    day = DayType.DEFAULT
                }
                m_buffer!!.append(m_delimiter)
                m_buffer!!.append(day.getValue())
            }

            m_buffer!!.append(MPXConstants.EOL)
            m_writer!!.write(m_buffer!!.toString())

            val hours = record.hours
            for (loop in hours.indices) {
                if (hours[loop] != null) {
                    writeCalendarHours(record, hours[loop])
                }
            }

            if (!record.calendarExceptions.isEmpty()) {
                //
                // A quirk of MS Project is that these exceptions must be
                // in date order in the file, otherwise they are ignored.
                // The getCalendarExceptions method now guarantees that
                // the exceptions list is sorted when retrieved.
                //
                for (ex in record.calendarExceptions) {
                    writeCalendarException(record, ex)
                }
            }

            m_eventManager!!.fireCalendarWrittenEvent(record)
        }
    }

    /**
     * Write calendar hours.
     *
     * @param parentCalendar parent calendar instance
     * @param record calendar hours instance
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeCalendarHours(parentCalendar: ProjectCalendar, record: ProjectCalendarHours) {
        m_buffer!!.setLength(0)

        val recordNumber: Int

        if (!parentCalendar.isDerived) {
            recordNumber = MPXConstants.BASE_CALENDAR_HOURS_RECORD_NUMBER
        } else {
            recordNumber = MPXConstants.RESOURCE_CALENDAR_HOURS_RECORD_NUMBER
        }

        var range1: DateRange? = record.getRange(0)
        if (range1 == null) {
            range1 = DateRange.EMPTY_RANGE
        }

        var range2: DateRange? = record.getRange(1)
        if (range2 == null) {
            range2 = DateRange.EMPTY_RANGE
        }

        var range3: DateRange? = record.getRange(2)
        if (range3 == null) {
            range3 = DateRange.EMPTY_RANGE
        }

        m_buffer!!.append(recordNumber)
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(record.day))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatTime(range1!!.getStart())))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatTime(range1!!.getEnd())))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatTime(range2!!.getStart())))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatTime(range2!!.getEnd())))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatTime(range3!!.getStart())))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatTime(range3!!.getEnd())))
        stripTrailingDelimiters(m_buffer!!)
        m_buffer!!.append(MPXConstants.EOL)

        m_writer!!.write(m_buffer!!.toString())
    }

    /**
     * Write a calendar exception.
     *
     * @param parentCalendar parent calendar instance
     * @param record calendar exception instance
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeCalendarException(parentCalendar: ProjectCalendar, record: ProjectCalendarException) {
        m_buffer!!.setLength(0)

        if (!parentCalendar.isDerived) {
            m_buffer!!.append(MPXConstants.BASE_CALENDAR_EXCEPTION_RECORD_NUMBER)
        } else {
            m_buffer!!.append(MPXConstants.RESOURCE_CALENDAR_EXCEPTION_RECORD_NUMBER)
        }
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDate(record.fromDate)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDate(record.toDate)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(if (record.working) "1" else "0")
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatTime(record.getRange(0).getStart())))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatTime(record.getRange(0).getEnd())))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatTime(record.getRange(1).getStart())))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatTime(record.getRange(1).getEnd())))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatTime(record.getRange(2).getStart())))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatTime(record.getRange(2).getEnd())))
        stripTrailingDelimiters(m_buffer!!)
        m_buffer!!.append(MPXConstants.EOL)

        m_writer!!.write(m_buffer!!.toString())
    }

    /**
     * Write a resource.
     *
     * @param record resource instance
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeResource(record: Resource) {
        m_buffer!!.setLength(0)

        //
        // Write the resource record
        //
        val fields = m_resourceModel!!.model

        m_buffer!!.append(MPXConstants.RESOURCE_RECORD_NUMBER)
        for (loop in fields.indices) {
            val mpxFieldType = fields[loop]
            if (mpxFieldType == -1) {
                break
            }

            val resourceField = MPXResourceField.getMpxjField(mpxFieldType)
            var value = record.getCachedValue(resourceField)
            value = formatType(resourceField!!.dataType, value)

            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(value))
        }

        stripTrailingDelimiters(m_buffer!!)
        m_buffer!!.append(MPXConstants.EOL)
        m_writer!!.write(m_buffer!!.toString())

        //
        // Write the resource notes
        //
        val notes = record.notes
        if (notes.length() !== 0) {
            writeNotes(MPXConstants.RESOURCE_NOTES_RECORD_NUMBER, notes)
        }

        //
        // Write the resource calendar
        //
        if (record.resourceCalendar != null) {
            writeCalendar(record.resourceCalendar!!)
        }

        m_eventManager!!.fireResourceWrittenEvent(record)
    }

    /**
     * Write notes.
     *
     * @param recordNumber record number
     * @param text note text
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeNotes(recordNumber: Int, text: String?) {
        m_buffer!!.setLength(0)

        m_buffer!!.append(recordNumber)
        m_buffer!!.append(m_delimiter)

        if (text != null) {
            val note = stripLineBreaks(text, MPXConstants.EOL_PLACEHOLDER_STRING)
            val quote = note.indexOf(m_delimiter) !== -1 || note.indexOf('"') !== -1
            val length = note.length()
            var c: Char

            if (quote == true) {
                m_buffer!!.append('"')
            }

            for (loop in 0 until length) {
                c = note.charAt(loop)

                when (c) {
                    '"' -> {
                        m_buffer!!.append("\"\"")
                    }

                    else -> {
                        m_buffer!!.append(c)
                    }
                }
            }

            if (quote == true) {
                m_buffer!!.append('"')
            }
        }

        m_buffer!!.append(MPXConstants.EOL)

        m_writer!!.write(m_buffer!!.toString())
    }

    /**
     * Write a task.
     *
     * @param record task instance
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeTask(record: Task) {
        m_buffer!!.setLength(0)

        //
        // Write the task
        //
        val fields = m_taskModel!!.model
        var field: Int

        m_buffer!!.append(MPXConstants.TASK_RECORD_NUMBER)
        for (loop in fields.indices) {
            field = fields[loop]
            if (field == -1) {
                break
            }

            val taskField = MPXTaskField.getMpxjField(field)
            var value = record.getCachedValue(taskField)
            value = formatType(taskField!!.getDataType(), value)

            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(value))
        }

        stripTrailingDelimiters(m_buffer!!)
        m_buffer!!.append(MPXConstants.EOL)
        m_writer!!.write(m_buffer!!.toString())

        //
        // Write the task notes
        //
        val notes = record.notes
        if (notes.length() !== 0) {
            writeNotes(MPXConstants.TASK_NOTES_RECORD_NUMBER, notes)
        }

        //
        // Write the recurring task
        //
        if (record.recurringTask != null) {
            writeRecurringTask(record.recurringTask!!)
        }

        //
        // Write any resource assignments
        //
        if (record.resourceAssignments.isEmpty() === false) {
            for (assignment in record.resourceAssignments) {
                writeResourceAssignment(assignment)
            }
        }

        m_eventManager!!.fireTaskWrittenEvent(record)
    }

    /**
     * Write a recurring task.
     *
     * @param record recurring task instance
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeRecurringTask(record: RecurringTask) {
        m_buffer!!.setLength(0)

        m_buffer!!.append(MPXConstants.RECURRING_TASK_RECORD_NUMBER)
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append("1")

        if (record.recurrenceType != null) {
            val monthlyRelative = record.recurrenceType == RecurrenceType.MONTHLY && record.relative
            val monthlyAbsolute = record.recurrenceType == RecurrenceType.MONTHLY && !record.relative
            val yearlyRelative = record.recurrenceType == RecurrenceType.YEARLY && record.relative
            val yearlyAbsolute = record.recurrenceType == RecurrenceType.YEARLY && !record.relative

            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(formatDateTime(record.startDate)))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(formatDateTime(record.finishDate)))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(RecurrenceUtility.getDurationValue(m_projectFile!!.projectProperties, record.duration)))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(RecurrenceUtility.getDurationUnits(record)))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(record.occurrences))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(RecurrenceUtility.getRecurrenceValue(record.recurrenceType)))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append("0")
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(if (record.useEndDate) "1" else "0")
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(if (record.isWorkingDaysOnly) "1" else "0")
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(RecurrenceUtility.getDays(record)))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(if (monthlyRelative) "1" else "0")
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(if (yearlyAbsolute) "1" else "0")
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(if (record.recurrenceType == RecurrenceType.DAILY) record.frequency else "1"))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(if (record.recurrenceType == RecurrenceType.WEEKLY) record.frequency else "1"))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(if (monthlyRelative) record.dayNumber else "1"))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(RecurrenceUtility.getDay(if (monthlyRelative) record.dayOfWeek else Day.MONDAY)))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(if (monthlyRelative) record.frequency else "1"))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(if (monthlyAbsolute) record.dayNumber else "1"))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(if (monthlyAbsolute) record.frequency else "1"))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(if (yearlyRelative) record.dayNumber else "1"))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(RecurrenceUtility.getDay(if (yearlyRelative) record.dayOfWeek else Day.MONDAY)))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(record.monthNumber))
            m_buffer!!.append(m_delimiter)
            m_buffer!!.append(format(formatDateTime(RecurrenceUtility.getYearlyAbsoluteAsDate(record))))

            stripTrailingDelimiters(m_buffer!!)
        }
        m_buffer!!.append(MPXConstants.EOL)

        m_writer!!.write(m_buffer!!.toString())
    }

    /**
     * Write resource assignment.
     *
     * @param record resource assignment instance
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeResourceAssignment(record: ResourceAssignment) {
        m_buffer!!.setLength(0)

        m_buffer!!.append(MPXConstants.RESOURCE_ASSIGNMENT_RECORD_NUMBER)
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(formatResource(record.resource))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatUnits(record.units)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(record.work)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(record.baselineWork)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(record.actualWork)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(record.overtimeWork)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatCurrency(record.cost)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatCurrency(record.baselineCost)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatCurrency(record.actualCost)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDateTime(record.start)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDateTime(record.finish)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDuration(record.delay)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(record.resourceUniqueID))
        stripTrailingDelimiters(m_buffer!!)
        m_buffer!!.append(MPXConstants.EOL)
        m_writer!!.write(m_buffer!!.toString())

        var workgroup = record.workgroupAssignment
        if (workgroup == null) {
            workgroup = ResourceAssignmentWorkgroupFields.EMPTY
        }
        writeResourceAssignmentWorkgroupFields(workgroup!!)

        m_eventManager!!.fireAssignmentWrittenEvent(record)
    }

    /**
     * Write resource assignment workgroup.
     *
     * @param record resource assignment workgroup instance
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeResourceAssignmentWorkgroupFields(record: ResourceAssignmentWorkgroupFields) {
        m_buffer!!.setLength(0)

        m_buffer!!.append(MPXConstants.RESOURCE_ASSIGNMENT_WORKGROUP_FIELDS_RECORD_NUMBER)
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(record.messageUniqueID))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(if (record.confirmed) "1" else "0")
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(if (record.responsePending) "1" else "0")
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDateTimeNull(record.updateStart)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(formatDateTimeNull(record.updateFinish)))
        m_buffer!!.append(m_delimiter)
        m_buffer!!.append(format(record.scheduleID))

        stripTrailingDelimiters(m_buffer!!)
        m_buffer!!.append(MPXConstants.EOL)

        m_writer!!.write(m_buffer!!.toString())
    }

    /**
     * Recursively write tasks.
     *
     * @param tasks list of tasks
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeTasks(tasks: List<Task>) {
        for (task in tasks) {
            writeTask(task)
            writeTasks(task.childTasks)
        }
    }

    /**
     * This internal method is used to convert from a Date instance to an
     * integer representing the number of minutes past midnight.
     *
     * @param date date instance
     * @return minutes past midnight as an integer
     */
    private fun getIntegerTimeInMinutes(date: Date?): Integer? {
        var result: Integer? = null
        if (date != null) {
            val cal = DateHelper.popCalendar(date)
            var time = cal.get(Calendar.HOUR_OF_DAY) * 60
            time += cal.get(Calendar.MINUTE)
            DateHelper.pushCalendar(cal)
            result = Integer.valueOf(time)
        }
        return result
    }

    /**
     * This method is called when double quotes are found as part of
     * a value. The quotes are escaped by adding a second quote character
     * and the entire value is quoted.
     *
     * @param value text containing quote characters
     * @return escaped and quoted text
     */
    private fun escapeQuotes(value: String): String {
        val sb = StringBuilder()
        val length = value.length()
        var c: Char

        sb.append('"')
        for (index in 0 until length) {
            c = value.charAt(index)
            sb.append(c)

            if (c == '"') {
                sb.append('"')
            }
        }
        sb.append('"')

        return sb.toString()
    }

    /**
     * This method removes line breaks from a piece of text, and replaces
     * them with the supplied text.
     *
     * @param text source text
     * @param replacement line break replacement text
     * @return text with line breaks removed.
     */
    private fun stripLineBreaks(text: String, replacement: String): String {
        var text = text
        if (text.indexOf('\r') !== -1 || text.indexOf('\n') !== -1) {
            val sb = StringBuilder(text)

            var index: Int

            while ((index = sb.indexOf("\r\n")) != -1) {
                sb.replace(index, index + 2, replacement)
            }

            while ((index = sb.indexOf("\n\r")) != -1) {
                sb.replace(index, index + 2, replacement)
            }

            while ((index = sb.indexOf("\r")) != -1) {
                sb.replace(index, index + 1, replacement)
            }

            while ((index = sb.indexOf("\n")) != -1) {
                sb.replace(index, index + 1, replacement)
            }

            text = sb.toString()
        }

        return text
    }

    /**
     * This method returns the string representation of an object. In most
     * cases this will simply involve calling the normal toString method
     * on the object, but a couple of exceptions are handled here.
     *
     * @param o the object to formatted
     * @return formatted string representing input Object
     */
    private fun format(o: Object?): String {
        var result: String

        if (o == null) {
            result = ""
        } else {
            if (o is Boolean == true) {
                result = LocaleData.getString(m_locale, if ((o as Boolean).booleanValue() === true) LocaleData.YES else LocaleData.NO)
            } else {
                if (o is Float == true || o is Double == true) {
                    result = m_formats!!.decimalFormat.format((o as Number).doubleValue())
                } else {
                    if (o is Day) {
                        result = Integer.toString((o as Day).getValue())
                    } else {
                        result = o!!.toString()
                    }
                }
            }

            //
            // At this point there should be no line break characters in
            // the file. If we find any, replace them with spaces
            //
            result = stripLineBreaks(result, MPXConstants.EOL_PLACEHOLDER_STRING)

            //
            // Finally we check to ensure that there are no embedded
            // quotes or separator characters in the value. If there are, then
            // we quote the value and escape any existing quote characters.
            //
            if (result.indexOf('"') !== -1) {
                result = escapeQuotes(result)
            } else {
                if (result.indexOf(m_delimiter) !== -1) {
                    result = '"'.toInt() + result.toInt() + '"'.toInt()
                }
            }
        }

        return result
    }

    /**
     * This method removes trailing delimiter characters.
     *
     * @param buffer input sring buffer
     */
    private fun stripTrailingDelimiters(buffer: StringBuilder) {
        var index = buffer.length() - 1

        while (index > 0 && buffer.charAt(index) === m_delimiter) {
            --index
        }

        buffer.setLength(index + 1)
    }

    /**
     * This method is called to format a time value.
     *
     * @param value time value
     * @return formatted time value
     */
    private fun formatTime(value: Date?): String? {
        return if (value == null) null else m_formats!!.timeFormat.format(value)
    }

    /**
     * This method is called to format a currency value.
     *
     * @param value numeric value
     * @return currency value
     */
    private fun formatCurrency(value: Number?): String? {
        return if (value == null) null else m_formats!!.currencyFormat.format(value)
    }

    /**
     * This method is called to format a units value.
     *
     * @param value numeric value
     * @return currency value
     */
    private fun formatUnits(value: Number?): String? {
        return if (value == null) null else m_formats!!.unitsDecimalFormat.format(value.doubleValue() / 100)
    }

    /**
     * This method is called to format a date.
     *
     * @param value date value
     * @return formatted date value
     */
    private fun formatDateTime(value: Object?): String? {
        var result: String? = null
        if (value is Date) {
            result = m_formats!!.dateTimeFormat.format(value)
        }
        return result
    }

    /**
     * This method is called to format a date. It will return the null text
     * if a null value is supplied.
     *
     * @param value date value
     * @return formatted date value
     */
    private fun formatDateTimeNull(value: Date?): String {
        return if (value == null) m_formats!!.nullText else m_formats!!.dateTimeFormat.format(value)
    }

    /**
     * This method is called to format a date.
     *
     * @param value date value
     * @return formatted date value
     */
    private fun formatDate(value: Date?): String? {
        return if (value == null) null else m_formats!!.dateFormat.format(value)
    }

    /**
     * This method is called to format a percentage value.
     *
     * @param value numeric value
     * @return percentage value
     */
    private fun formatPercentage(value: Number?): String? {
        return if (value == null) null else m_formats!!.percentageDecimalFormat.format(value) + "%"
    }

    /**
     * This method is called to format an accrue type value.
     *
     * @param type accrue type
     * @return formatted accrue type
     */
    private fun formatAccrueType(type: AccrueType?): String? {
        return if (type == null) null else LocaleData.getStringArray(m_locale, LocaleData.ACCRUE_TYPES)[type!!.getValue() - 1]
    }

    /**
     * This method is called to format a constraint type.
     *
     * @param type constraint type
     * @return formatted constraint type
     */
    private fun formatConstraintType(type: ConstraintType?): String? {
        return if (type == null) null else LocaleData.getStringArray(m_locale, LocaleData.CONSTRAINT_TYPES)[type!!.getValue()]
    }

    /**
     * This method is called to format a duration.
     *
     * @param value duration value
     * @return formatted duration value
     */
    private fun formatDuration(value: Object?): String? {
        var result: String? = null
        if (value is Duration) {
            val duration = value as Duration?
            result = m_formats!!.durationDecimalFormat.format(duration!!.getDuration()) + formatTimeUnit(duration!!.getUnits())
        }
        return result
    }

    /**
     * This method is called to format a rate.
     *
     * @param value rate value
     * @return formatted rate
     */
    private fun formatRate(value: Rate?): String? {
        var result: String? = null
        if (value != null) {
            val buffer = StringBuilder(m_formats!!.currencyFormat.format(value.amount))
            buffer.append("/")
            buffer.append(formatTimeUnit(value.units!!))
            result = buffer.toString()
        }
        return result
    }

    /**
     * This method is called to format a priority.
     *
     * @param value priority instance
     * @return formatted priority value
     */
    private fun formatPriority(value: Priority?): String? {
        var result: String? = null

        if (value != null) {
            val priorityTypes = LocaleData.getStringArray(m_locale, LocaleData.PRIORITY_TYPES)
            var priority = value.value
            if (priority < Priority.LOWEST) {
                priority = Priority.LOWEST
            } else {
                if (priority > Priority.DO_NOT_LEVEL) {
                    priority = Priority.DO_NOT_LEVEL
                }
            }

            priority /= 100

            result = priorityTypes[priority - 1]
        }

        return result
    }

    /**
     * This method is called to format a task type.
     *
     * @param value task type value
     * @return formatted task type
     */
    private fun formatTaskType(value: TaskType): String {
        return LocaleData.getString(m_locale, if (value === TaskType.FIXED_DURATION) LocaleData.YES else LocaleData.NO)
    }

    /**
     * This method is called to format a relation list.
     *
     * @param value relation list instance
     * @return formatted relation list
     */
    private fun formatRelationList(value: List<Relation>?): String? {
        var result: String? = null

        if (value != null && value.size() !== 0) {
            val sb = StringBuilder()
            for (relation in value) {
                if (sb.length() !== 0) {
                    sb.append(m_delimiter)
                }

                sb.append(formatRelation(relation))
            }

            result = sb.toString()
        }

        return result
    }

    /**
     * This method is called to format a relation.
     *
     * @param relation relation instance
     * @return formatted relation instance
     */
    private fun formatRelation(relation: Relation?): String? {
        var result: String? = null

        if (relation != null) {
            val sb = StringBuilder(relation.targetTask.id!!.toString())

            val duration = relation.lag
            val type = relation.type
            val durationValue = duration!!.getDuration()

            if (durationValue != 0.0 || type != RelationType.FINISH_START) {
                val typeNames = LocaleData.getStringArray(m_locale, LocaleData.RELATION_TYPES)
                sb.append(typeNames[type!!.value])
            }

            if (durationValue != 0.0) {
                if (durationValue > 0) {
                    sb.append('+')
                }

                sb.append(formatDuration(duration))
            }

            result = sb.toString()
        }

        m_eventManager!!.fireRelationWrittenEvent(relation)
        return result
    }

    /**
     * This method formats a time unit.
     *
     * @param timeUnit time unit instance
     * @return formatted time unit instance
     */
    private fun formatTimeUnit(timeUnit: TimeUnit): String {
        val units = timeUnit.getValue()
        val result: String
        val unitNames = LocaleData.getStringArrays(m_locale, LocaleData.TIME_UNITS_ARRAY)

        if (units < 0 || units >= unitNames.size) {
            result = ""
        } else {
            result = unitNames[units][0]
        }

        return result
    }

    /**
     * This method formats a decimal value.
     *
     * @param value value
     * @return formatted value
     */
    private fun formatDecimal(value: Double): String {
        return m_formats!!.decimalFormat.format(value)
    }

    /**
     * Converts a value to the appropriate type.
     *
     * @param type target type
     * @param value input value
     * @return output value
     */
    @SuppressWarnings("unchecked")
    private fun formatType(type: DataType, value: Object?): Object? {
        var value = value
        when (type) {
            DATE -> {
                value = formatDateTime(value)
            }

            CURRENCY -> {
                value = formatCurrency(value as Number?)
            }

            UNITS -> {
                value = formatUnits(value as Number?)
            }

            PERCENTAGE -> {
                value = formatPercentage(value as Number?)
            }

            ACCRUE -> {
                value = formatAccrueType(value as AccrueType?)
            }

            CONSTRAINT -> {
                value = formatConstraintType(value as ConstraintType?)
            }

            WORK, DURATION -> {
                value = formatDuration(value)
            }

            RATE -> {
                value = formatRate(value as Rate?)
            }

            PRIORITY -> {
                value = formatPriority(value as Priority?)
            }

            RELATION_LIST -> {
                value = formatRelationList(value as List<Relation>?)
            }

            TASK_TYPE -> {
                value = formatTaskType(value as TaskType?)
            }

            else -> {
            }
        }

        return value
    }

    /**
     * Formats a resource, taking into account that the resource reference
     * may be null.
     *
     * @param resource Resource instance
     * @return formatted value
     */
    private fun formatResource(resource: Resource?): String {
        return if (resource == null) "-65535" else format(resource.id)
    }
}
