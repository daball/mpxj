/*
 * file:       ProjectPropertiesReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       24/08/2006
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

import java.util.HashMap

import org.apache.poi.hpsf.CustomProperties
import org.apache.poi.hpsf.CustomProperty
import org.apache.poi.hpsf.DocumentSummaryInformation
import org.apache.poi.hpsf.PropertySet
import org.apache.poi.hpsf.SummaryInformation
import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream

import net.sf.mpxj.Day
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Rate
import net.sf.mpxj.ScheduleFrom
import net.sf.mpxj.TaskType
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.NumberHelper

/**
 * This class reads project properties data from MPP8, MPP9, and MPP12 files.
 */
class ProjectPropertiesReader {
    /**
     * The main entry point for processing project properties.
     *
     * @param file parent project file
     * @param props properties data
     * @param rootDir Root of the POI file system.
     */
    @Throws(MPXJException::class)
    fun process(file: ProjectFile, props: Props, rootDir: DirectoryEntry) {
        try {
            //MPPUtility.fileDump("c:\\temp\\props.txt", props.toString().getBytes());
            val ph = file.projectProperties
            ph.startDate = props.getTimestamp(Props.PROJECT_START_DATE)
            ph.finishDate = props.getTimestamp(Props.PROJECT_FINISH_DATE)
            ph.scheduleFrom = ScheduleFrom.getInstance(1 - props.getShort(Props.SCHEDULE_FROM))
            ph.defaultCalendarName = props.getUnicodeString(Props.DEFAULT_CALENDAR_NAME)
            ph.defaultStartTime = props.getTime(Props.START_TIME)
            ph.defaultEndTime = props.getTime(Props.END_TIME)
            ph.statusDate = props.getTimestamp(Props.STATUS_DATE)
            ph.hyperlinkBase = props.getUnicodeString(Props.HYPERLINK_BASE)

            //ph.setDefaultDurationIsFixed();
            ph.defaultDurationUnits = MPPUtility.getDurationTimeUnits(props.getShort(Props.DURATION_UNITS))
            ph.minutesPerDay = Integer.valueOf(props.getInt(Props.MINUTES_PER_DAY))
            ph.minutesPerWeek = Integer.valueOf(props.getInt(Props.MINUTES_PER_WEEK))
            ph.defaultOvertimeRate = Rate(props.getDouble(Props.OVERTIME_RATE), TimeUnit.HOURS)
            ph.defaultStandardRate = Rate(props.getDouble(Props.STANDARD_RATE), TimeUnit.HOURS)
            ph.defaultWorkUnits = MPPUtility.getWorkTimeUnits(props.getShort(Props.WORK_UNITS))
            ph.splitInProgressTasks = props.getBoolean(Props.SPLIT_TASKS)
            ph.updatingTaskStatusUpdatesResourceStatus = props.getBoolean(Props.TASK_UPDATES_RESOURCE)
            ph.criticalSlackLimit = Integer.valueOf(props.getInt(Props.CRITICAL_SLACK_LIMIT))

            ph.currencyDigits = Integer.valueOf(props.getShort(Props.CURRENCY_DIGITS))
            ph.currencySymbol = props.getUnicodeString(Props.CURRENCY_SYMBOL)
            ph.currencyCode = props.getUnicodeString(Props.CURRENCY_CODE)
            //ph.setDecimalSeparator();
            ph.defaultTaskType = TaskType.getInstance(props.getShort(Props.DEFAULT_TASK_TYPE))
            ph.symbolPosition = MPPUtility.getSymbolPosition(props.getShort(Props.CURRENCY_PLACEMENT))
            //ph.setThousandsSeparator();
            ph.weekStartDay = Day.getInstance(props.getShort(Props.WEEK_START_DAY) + 1)
            ph.fiscalYearStartMonth = Integer.valueOf(props.getShort(Props.FISCAL_YEAR_START_MONTH))
            ph.fiscalYearStart = props.getShort(Props.FISCAL_YEAR_START) == 1
            ph.daysPerMonth = Integer.valueOf(props.getShort(Props.DAYS_PER_MONTH))
            ph.editableActualCosts = props.getBoolean(Props.EDITABLE_ACTUAL_COSTS)
            ph.honorConstraints = !props.getBoolean(Props.HONOR_CONSTRAINTS)

            var ps: PropertySet? = PropertySet(DocumentInputStream(rootDir.getEntry(SummaryInformation.DEFAULT_STREAM_NAME) as DocumentEntry))
            val summaryInformation = SummaryInformation(ps!!)
            ph.projectTitle = summaryInformation.title
            ph.subject = summaryInformation.subject
            ph.author = summaryInformation.author
            ph.keywords = summaryInformation.keywords
            ph.comments = summaryInformation.comments
            ph.template = summaryInformation.template
            ph.lastAuthor = summaryInformation.lastAuthor
            ph.revision = NumberHelper.parseInteger(summaryInformation.revNumber)
            ph.creationDate = summaryInformation.createDateTime
            ph.lastSaved = summaryInformation.lastSaveDateTime
            ph.shortApplicationName = summaryInformation.applicationName
            ph.editingTime = Integer.valueOf(summaryInformation.editTime.toInt())
            ph.lastPrinted = summaryInformation.lastPrinted

            try {
                ps = PropertySet(DocumentInputStream(rootDir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME) as DocumentEntry))
            } catch (ex: RuntimeException) {
                // I have one example MPP file which has a corrupt document summary property set.
                // Microsoft Project opens the file successfully, apparently by just ignoring
                // the corrupt data. We'll do the same here. I have raised a bug with POI
                // to see if they want to make the library more robust in the face of bad data.
                // https://bz.apache.org/bugzilla/show_bug.cgi?id=61550
                ps = null
            }

            val documentSummaryInformation = ps?.let { DocumentSummaryInformation(it) } ?: DocumentSummaryInformation()
            ph.category = documentSummaryInformation.category
            ph.presentationFormat = documentSummaryInformation.presentationFormat
            ph.manager = documentSummaryInformation.manager
            ph.company = documentSummaryInformation.company
            ph.contentType = documentSummaryInformation.contentType
            ph.contentStatus = documentSummaryInformation.contentStatus
            ph.language = documentSummaryInformation.language
            ph.documentVersion = documentSummaryInformation.documentVersion

            val customPropertiesMap = HashMap<String, Object>()
            val customProperties = documentSummaryInformation.customProperties
            if (customProperties != null) {
                for (property in customProperties.properties()) {
                    customPropertiesMap.put(property.getName(), property.getValue())
                }
            }
            ph.customProperties = customPropertiesMap

            ph.calculateMultipleCriticalPaths = props.getBoolean(Props.CALCULATE_MULTIPLE_CRITICAL_PATHS)

            ph.baselineDate = props.getTimestamp(Props.BASELINE_DATE)
            ph.setBaselineDate(1, props.getTimestamp(Props.BASELINE1_DATE))
            ph.setBaselineDate(2, props.getTimestamp(Props.BASELINE2_DATE))
            ph.setBaselineDate(3, props.getTimestamp(Props.BASELINE3_DATE))
            ph.setBaselineDate(4, props.getTimestamp(Props.BASELINE4_DATE))
            ph.setBaselineDate(5, props.getTimestamp(Props.BASELINE5_DATE))
            ph.setBaselineDate(6, props.getTimestamp(Props.BASELINE6_DATE))
            ph.setBaselineDate(7, props.getTimestamp(Props.BASELINE7_DATE))
            ph.setBaselineDate(8, props.getTimestamp(Props.BASELINE8_DATE))
            ph.setBaselineDate(9, props.getTimestamp(Props.BASELINE9_DATE))
            ph.setBaselineDate(10, props.getTimestamp(Props.BASELINE10_DATE))
        } catch (ex: Exception) {
            throw MPXJException(MPXJException.READ_ERROR, ex)
        }

    }
}
