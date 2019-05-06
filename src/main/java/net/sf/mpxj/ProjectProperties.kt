/*
 * file:       ProjectProperties.java
 * author:     Jon Iles
 *             Scott Melville
 * copyright:  (c) Packwood Software 2002-2015
 * date:       15/08/2002
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

package net.sf.mpxj

import java.util.Date
import java.util.LinkedList

import net.sf.mpxj.common.BooleanHelper
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.ProjectFieldLists
import net.sf.mpxj.listener.FieldListener

/**
 * This class represents a collection of properties relevant to the whole project.
 */
class ProjectProperties
/**
 * Default constructor.
 *
 * @param file the parent file to which this record belongs.
 */
internal constructor(file: ProjectFile) : ProjectEntity(file), FieldContainer {

    /**
     * Gets Default Duration units. The constants used to define the
     * duration units are defined by the `TimeUnit` class.
     *
     * @return default duration units
     * @see TimeUnit
     */
    /**
     * Default duration units. The constants used to define the
     * duration units are defined by the `TimeUnit` class.
     *
     * @param units default duration units
     * @see TimeUnit
     */
    var defaultDurationUnits: TimeUnit
        get() = getCachedValue(ProjectField.DEFAULT_DURATION_UNITS) as TimeUnit?
        set(units) {
            set(ProjectField.DEFAULT_DURATION_UNITS, units)
        }

    /**
     * Retrieves a flag indicating if the default duration type is fixed.
     *
     * @return boolean flag
     */
    /**
     * Sets a flag indicating if the default duration type is fixed.
     *
     * @param fixed boolean flag
     */
    var defaultDurationIsFixed: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.DEFAULT_DURATION_IS_FIXED) as Boolean?)
        set(fixed) {
            set(ProjectField.DEFAULT_DURATION_IS_FIXED, fixed)
        }

    /**
     * Default work units. The constants used to define the
     * work units are defined by the `TimeUnit` class.
     *
     * @return default work units
     * @see TimeUnit
     */
    /**
     * Default work units. The constants used to define the
     * work units are defined by the `TimeUnit` class.
     *
     * @param units  default work units
     * @see TimeUnit
     */
    var defaultWorkUnits: TimeUnit
        get() = getCachedValue(ProjectField.DEFAULT_WORK_UNITS) as TimeUnit?
        set(units) {
            set(ProjectField.DEFAULT_WORK_UNITS, units)
        }

    /**
     * Retrieves the default standard rate.
     *
     * @return default standard rate
     */
    /**
     * Sets the default standard rate.
     *
     * @param rate default standard rate
     */
    var defaultStandardRate: Rate
        get() = getCachedValue(ProjectField.DEFAULT_STANDARD_RATE)
        set(rate) = set(ProjectField.DEFAULT_STANDARD_RATE, rate)

    /**
     * Get overtime rate.
     *
     * @return rate
     */
    /**
     * Set default overtime rate.
     *
     * @param rate default overtime rate
     */
    var defaultOvertimeRate: Rate
        get() = getCachedValue(ProjectField.DEFAULT_OVERTIME_RATE)
        set(rate) = set(ProjectField.DEFAULT_OVERTIME_RATE, rate)

    /**
     * Flags whether updating Task status also updates resource status.
     *
     * @return boolean flag
     */
    /**
     * Flags whether updating Task status also updates resource status.
     *
     * @param flag boolean flag
     */
    var updatingTaskStatusUpdatesResourceStatus: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.UPDATING_TASK_STATUS_UPDATES_RESOURCE_STATUS) as Boolean?)
        set(flag) {
            set(ProjectField.UPDATING_TASK_STATUS_UPDATES_RESOURCE_STATUS, flag)
        }

    /**
     * Flag representing whether or not to split in-progress tasks.
     *
     * @return Boolean value
     */
    /**
     * Flag representing whether or not to split in-progress tasks.
     *
     * @param flag boolean value
     */
    var splitInProgressTasks: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.SPLIT_IN_PROGRESS_TASKS) as Boolean?)
        set(flag) {
            set(ProjectField.SPLIT_IN_PROGRESS_TASKS, flag)
        }

    /**
     * Gets constant representing set Date order eg DMY, MDY.
     *
     * @return constant value for date order
     */
    /**
     * Sets constant representing set Date order eg DMY, MDY.
     *
     * @param dateOrder date order value
     */
    var dateOrder: DateOrder
        get() = getCachedValue(ProjectField.DATE_ORDER) as DateOrder?
        set(dateOrder) {
            set(ProjectField.DATE_ORDER, dateOrder)
        }

    /**
     * Gets constant representing the Time Format.
     *
     * @return time format constant
     */
    /**
     * Sets constant representing the time format.
     *
     * @param timeFormat constant value
     */
    var timeFormat: ProjectTimeFormat
        get() = getCachedValue(ProjectField.TIME_FORMAT)
        set(timeFormat) = set(ProjectField.TIME_FORMAT, timeFormat)

    /**
     * Retrieve the default start time, specified using the Java Date type.
     * Note that this assumes that the value returned from
     * the getTime method starts at zero... i.e. the date part
     * of the date/time value has not been set.
     *
     * @return default start time
     */
    /**
     * Set the default start time, specified using the Java Date type.
     * Note that this assumes that the value returned from
     * the getTime method starts at zero... i.e. the date part
     * of the date/time value has not been set.
     *
     * @param defaultStartTime default time
     */
    var defaultStartTime: Date?
        get() = getCachedValue(ProjectField.DEFAULT_START_TIME) as Date?
        set(defaultStartTime) {
            set(ProjectField.DEFAULT_START_TIME, defaultStartTime)
        }

    /**
     * Gets the date separator.
     *
     * @return date separator as set.
     */
    /**
     * Sets the date separator.
     *
     * @param dateSeparator date separator as set.
     */
    var dateSeparator: Char
        get() = getCachedCharValue(ProjectField.DATE_SEPARATOR, DEFAULT_DATE_SEPARATOR)
        set(dateSeparator) {
            set(ProjectField.DATE_SEPARATOR, Character.valueOf(dateSeparator))
        }

    /**
     * Gets the time separator.
     *
     * @return time separator as set.
     */
    /**
     * Sets the time separator.
     *
     * @param timeSeparator time separator
     */
    var timeSeparator: Char
        get() = getCachedCharValue(ProjectField.TIME_SEPARATOR, DEFAULT_TIME_SEPARATOR)
        set(timeSeparator) {
            set(ProjectField.TIME_SEPARATOR, Character.valueOf(timeSeparator))
        }

    /**
     * Gets the AM text.
     *
     * @return AM Text as set.
     */
    /**
     * Sets the AM text.
     *
     * @param amText AM Text as set.
     */
    var amText: String
        get() = getCachedValue(ProjectField.AM_TEXT)
        set(amText) = set(ProjectField.AM_TEXT, amText)

    /**
     * Gets the PM text.
     *
     * @return PM Text as set.
     */
    /**
     * Sets the PM text.
     *
     * @param pmText PM Text as set.
     */
    var pmText: String
        get() = getCachedValue(ProjectField.PM_TEXT)
        set(pmText) = set(ProjectField.PM_TEXT, pmText)

    /**
     * Gets the set Date Format.
     *
     * @return project date format
     */
    /**
     * Sets the set Date Format.
     *
     * @param dateFormat int representing Date Format
     */
    var dateFormat: ProjectDateFormat
        get() = getCachedValue(ProjectField.DATE_FORMAT)
        set(dateFormat) = set(ProjectField.DATE_FORMAT, dateFormat)

    /**
     * Gets Bar Text Date Format.
     *
     * @return int value
     */
    /**
     * Sets Bar Text Date Format.
     *
     * @param dateFormat value to be set
     */
    var barTextDateFormat: ProjectDateFormat
        get() = getCachedValue(ProjectField.BAR_TEXT_DATE_FORMAT)
        set(dateFormat) = set(ProjectField.BAR_TEXT_DATE_FORMAT, dateFormat)

    /**
     * Retrieves the default end time.
     *
     * @return End time
     */
    /**
     * Sets the default end time.
     *
     * @param date End time
     */
    var defaultEndTime: Date
        get() = getCachedValue(ProjectField.DEFAULT_END_TIME) as Date?
        set(date) {
            set(ProjectField.DEFAULT_END_TIME, date)
        }

    /**
     * Gets the project title.
     *
     * @return project title
     */
    /**
     * Sets the project title.
     *
     * @param projectTitle project title
     */
    var projectTitle: String
        get() = getCachedValue(ProjectField.PROJECT_TITLE)
        set(projectTitle) = set(ProjectField.PROJECT_TITLE, projectTitle)

    /**
     * Retrieves the company name.
     *
     * @return company name
     */
    /**
     * Sets the company name.
     *
     * @param company company name
     */
    var company: String?
        get() = getCachedValue(ProjectField.COMPANY)
        set(company) = set(ProjectField.COMPANY, company)

    /**
     * Retrieves the manager name.
     *
     * @return manager name
     */
    /**
     * Sets the manager name.
     *
     * @param manager manager name
     */
    var manager: String?
        get() = getCachedValue(ProjectField.MANAGER)
        set(manager) = set(ProjectField.MANAGER, manager)

    /**
     * Gets the Calendar used. 'Standard' if no value is set.
     *
     * @return Calendar name
     */
    /**
     * Sets the Calendar used. 'Standard' if no value is set.
     *
     * @param calendarName Calendar name
     */
    var defaultCalendarName: String?
        get() = getCachedValue(ProjectField.DEFAULT_CALENDAR_NAME)
        set(calendarName) {
            var calendarName = calendarName
            if (calendarName == null || calendarName.length() === 0) {
                calendarName = DEFAULT_CALENDAR_NAME
            }

            set(ProjectField.DEFAULT_CALENDAR_NAME, calendarName)
        }

    /**
     * Retrieves the project start date. If an explicit start date has not been
     * set, this method calculates the start date by looking for
     * the earliest task start date.
     *
     * @return project start date
     */
    /**
     * Sets the project start date.
     *
     * @param startDate project start date
     */
    var startDate: Date?
        get() {
            var result = getCachedValue(ProjectField.START_DATE) as Date?
            if (result == null) {
                result = parentFile.startDate
            }
            return result
        }
        set(startDate) {
            set(ProjectField.START_DATE, startDate)
        }

    /**
     * Retrieves the project finish date. If an explicit finish date has not been
     * set, this method calculates the finish date by looking for
     * the latest task finish date.
     *
     * @return Finish Date
     */
    /**
     * Sets the project finish date.
     *
     * @param finishDate project finish date
     */
    var finishDate: Date?
        get() {
            var result = getCachedValue(ProjectField.FINISH_DATE) as Date?
            if (result == null) {
                result = parentFile.finishDate
            }
            return result
        }
        set(finishDate) {
            set(ProjectField.FINISH_DATE, finishDate)
        }

    /**
     * Retrieves an enumerated value indicating if tasks in this project are
     * scheduled from a start or a finish date.
     *
     * @return schedule from flag
     */
    /**
     * Sets an enumerated value indicating if tasks in this project are
     * scheduled from a start or a finish date.
     *
     * @param scheduleFrom schedule from value
     */
    var scheduleFrom: ScheduleFrom
        get() = getCachedValue(ProjectField.SCHEDULE_FROM)
        set(scheduleFrom) = set(ProjectField.SCHEDULE_FROM, scheduleFrom)

    /**
     * Retrieves the current date.
     *
     * @return current date
     */
    /**
     * Sets the current date.
     *
     * @param currentDate current date
     */
    var currentDate: Date
        get() = getCachedValue(ProjectField.CURRENT_DATE) as Date?
        set(currentDate) {
            set(ProjectField.CURRENT_DATE, currentDate)
        }

    /**
     * Returns any comments.
     *
     * @return comments
     */
    /**
     * Set comment text.
     *
     * @param comments comment text
     */
    var comments: String?
        get() = getCachedValue(ProjectField.COMMENTS)
        set(comments) = set(ProjectField.COMMENTS, comments)

    /**
     * Retrieves the project cost.
     *
     * @return project cost
     */
    /**
     * Sets the project cost.
     *
     * @param cost project cost
     */
    var cost: Number
        get() = getCachedValue(ProjectField.COST)
        set(cost) = set(ProjectField.COST, cost)

    /**
     * Retrieves the baseline project cost.
     *
     * @return baseline project cost
     */
    /**
     * Sets the baseline project cost.
     *
     * @param baselineCost baseline project cost
     */
    var baselineCost: Number
        get() = getCachedValue(ProjectField.BASELINE_COST)
        set(baselineCost) = set(ProjectField.BASELINE_COST, baselineCost)

    /**
     * Retrieves the actual project cost.
     *
     * @return actual project cost
     */
    /**
     * Sets the actual project cost.
     *
     * @param actualCost actual project cost
     */
    var actualCost: Number
        get() = getCachedValue(ProjectField.ACTUAL_COST)
        set(actualCost) = set(ProjectField.ACTUAL_COST, actualCost)

    /**
     * Retrieves the project work duration.
     *
     * @return project work duration
     */
    /**
     * Sets the project work duration.
     *
     * @param work project work duration
     */
    var work: Duration
        get() = getCachedValue(ProjectField.WORK) as Duration?
        set(work) {
            set(ProjectField.WORK, work)
        }

    /**
     * Retrieves the baseline project work duration.
     *
     * @return baseline project work duration
     */
    /**
     * Set the baseline project work duration.
     *
     * @param baselineWork baseline project work duration
     */
    var baselineWork: Duration
        get() = getCachedValue(ProjectField.BASELINE_WORK) as Duration?
        set(baselineWork) {
            set(ProjectField.BASELINE_WORK, baselineWork)
        }

    /**
     * Retrieves the actual project work duration.
     *
     * @return actual project work duration
     */
    /**
     * Sets the actual project work duration.
     *
     * @param actualWork actual project work duration
     */
    var actualWork: Duration
        get() = getCachedValue(ProjectField.ACTUAL_WORK) as Duration?
        set(actualWork) {
            set(ProjectField.ACTUAL_WORK, actualWork)
        }

    /**
     * Retrieves the project's "Work 2" attribute.
     *
     * @return Work 2 attribute
     */
    /**
     * Sets the project's "Work 2" attribute.
     *
     * @param work2 work2 percentage value
     */
    var work2: Number
        get() = getCachedValue(ProjectField.WORK2)
        set(work2) = set(ProjectField.WORK2, work2)

    /**
     * Retrieves the project duration.
     *
     * @return project duration
     */
    /**
     * Sets the project duration.
     *
     * @param duration project duration
     */
    var duration: Duration
        get() = getCachedValue(ProjectField.DURATION) as Duration?
        set(duration) {
            set(ProjectField.DURATION, duration)
        }

    /**
     * Retrieves the baseline duration value.
     *
     * @return baseline project duration value
     */
    /**
     * Sets the baseline project duration value.
     *
     * @param baselineDuration baseline project duration
     */
    var baselineDuration: Duration
        get() = getCachedValue(ProjectField.BASELINE_DURATION) as Duration?
        set(baselineDuration) {
            set(ProjectField.BASELINE_DURATION, baselineDuration)
        }

    /**
     * Retrieves the actual project duration.
     *
     * @return actual project duration
     */
    /**
     * Sets the actual project duration.
     *
     * @param actualDuration actual project duration
     */
    var actualDuration: Duration
        get() = getCachedValue(ProjectField.ACTUAL_DURATION) as Duration?
        set(actualDuration) {
            set(ProjectField.ACTUAL_DURATION, actualDuration)
        }

    /**
     * Retrieves the project percentage complete.
     *
     * @return percentage value
     */
    /**
     * Sets project percentage complete.
     *
     * @param percentComplete project percent complete
     */
    var percentageComplete: Number
        get() = getCachedValue(ProjectField.PERCENTAGE_COMPLETE)
        set(percentComplete) = set(ProjectField.PERCENTAGE_COMPLETE, percentComplete)

    /**
     * Retrieves the baseline project start date.
     *
     * @return baseline project start date
     */
    /**
     * Sets the baseline project start date.
     *
     * @param baselineStartDate baseline project start date
     */
    var baselineStart: Date?
        get() = getCachedValue(ProjectField.BASELINE_START) as Date?
        set(baselineStartDate) {
            set(ProjectField.BASELINE_START, baselineStartDate)
        }

    /**
     * Retrieves the baseline project finish date.
     *
     * @return baseline project finish date
     */
    /**
     * Sets the baseline project finish date.
     *
     * @param baselineFinishDate baseline project finish date
     */
    var baselineFinish: Date?
        get() = getCachedValue(ProjectField.BASELINE_FINISH) as Date?
        set(baselineFinishDate) {
            set(ProjectField.BASELINE_FINISH, baselineFinishDate)
        }

    /**
     * Retrieves the actual project start date.
     *
     * @return actual project start date
     */
    /**
     * Sets the actual project start date.
     *
     * @param actualStartDate actual project start date
     */
    var actualStart: Date?
        get() = getCachedValue(ProjectField.ACTUAL_START) as Date?
        set(actualStartDate) {
            set(ProjectField.ACTUAL_START, actualStartDate)
        }

    /**
     * Retrieves the actual project finish date.
     *
     * @return actual project finish date
     */
    /**
     * Sets the actual project finish date.
     *
     * @param actualFinishDate actual project finish date
     */
    var actualFinish: Date?
        get() = getCachedValue(ProjectField.ACTUAL_FINISH) as Date?
        set(actualFinishDate) {
            set(ProjectField.ACTUAL_FINISH, actualFinishDate)
        }

    /**
     * Retrieves the start variance duration.
     *
     * @return start date variance
     */
    /**
     * Sets the start variance duration.
     *
     * @param startVariance the start date variance
     */
    var startVariance: Duration
        get() = getCachedValue(ProjectField.START_VARIANCE) as Duration?
        set(startVariance) {
            set(ProjectField.START_VARIANCE, startVariance)
        }

    /**
     * Retrieves the project finish variance duration.
     *
     * @return project finish variance duration
     */
    /**
     * Sets the project finish variance duration.
     *
     * @param finishVariance project finish variance duration
     */
    var finishVariance: Duration
        get() = getCachedValue(ProjectField.FINISH_VARIANCE) as Duration?
        set(finishVariance) {
            set(ProjectField.FINISH_VARIANCE, finishVariance)
        }

    /**
     * Returns the project subject text.
     *
     * @return subject text
     */
    /**
     * Sets the project subject text.
     *
     * @param subject subject text
     */
    var subject: String?
        get() = getCachedValue(ProjectField.SUBJECT)
        set(subject) = set(ProjectField.SUBJECT, subject)

    /**
     * Retrieves the project author text.
     *
     * @return author text
     */
    /**
     * Sets the project author text.
     *
     * @param author project author text
     */
    var author: String?
        get() = getCachedValue(ProjectField.AUTHOR)
        set(author) = set(ProjectField.AUTHOR, author)

    /**
     * Retrieves the project keyword text.
     *
     * @return project keyword text
     */
    /**
     * Sets the project keyword text.
     *
     * @param keywords project keyword text
     */
    var keywords: String?
        get() = getCachedValue(ProjectField.KEYWORDS)
        set(keywords) = set(ProjectField.KEYWORDS, keywords)

    /**
     * Retrieves the currency symbol.
     *
     * @return currency symbol
     */
    /**
     * Sets currency symbol.
     *
     * @param symbol currency symbol
     */
    var currencySymbol: String?
        get() = getCachedValue(ProjectField.CURRENCY_SYMBOL)
        set(symbol) {
            var symbol = symbol
            if (symbol == null) {
                symbol = DEFAULT_CURRENCY_SYMBOL
            }

            set(ProjectField.CURRENCY_SYMBOL, symbol)
        }

    /**
     * Retrieves a constant representing the position of the currency symbol.
     *
     * @return position
     */
    /**
     * Sets the position of the currency symbol.
     *
     * @param posn currency symbol position.
     */
    var symbolPosition: CurrencySymbolPosition?
        get() = getCachedValue(ProjectField.CURRENCY_SYMBOL_POSITION) as CurrencySymbolPosition?
        set(posn) {
            var posn = posn
            if (posn == null) {
                posn = DEFAULT_CURRENCY_SYMBOL_POSITION
            }
            set(ProjectField.CURRENCY_SYMBOL_POSITION, posn)
        }

    /**
     * Gets no of currency digits.
     *
     * @return Available values, 0,1,2
     */
    /**
     * Sets no of currency digits.
     *
     * @param currDigs Available values, 0,1,2
     */
    var currencyDigits: Integer?
        get() = getCachedValue(ProjectField.CURRENCY_DIGITS) as Integer?
        set(currDigs) {
            var currDigs = currDigs
            if (currDigs == null) {
                currDigs = DEFAULT_CURRENCY_DIGITS
            }
            set(ProjectField.CURRENCY_DIGITS, currDigs)
        }

    /**
     * Gets the thousands separator.
     * Note that this separator defines the thousands separator for all decimal
     * numbers that appear in the MPX file.
     *
     * @return character
     */
    /**
     * Sets the thousands separator.
     * Note that this separator defines the thousands separator for all decimal
     * numbers that appear in the MPX file.
     *
     * @param sep character
     */
    var thousandsSeparator: Char
        get() = getCachedCharValue(ProjectField.THOUSANDS_SEPARATOR, DEFAULT_THOUSANDS_SEPARATOR)
        set(sep) {
            set(ProjectField.THOUSANDS_SEPARATOR, Character.valueOf(sep))
        }

    /**
     * Gets the decimal separator.
     * Note that this separator defines the decimal separator for all decimal
     * numbers that appear in the MPX file.
     *
     * @return character
     */
    /**
     * Sets the decimal separator.
     * Note that this separator defines the decimal separator for all decimal
     * numbers that appear in the MPX file.
     *
     * @param decSep character
     */
    var decimalSeparator: Char
        get() = getCachedCharValue(ProjectField.DECIMAL_SEPARATOR, DEFAULT_DECIMAL_SEPARATOR)
        set(decSep) {
            set(ProjectField.DECIMAL_SEPARATOR, Character.valueOf(decSep))
        }

    /**
     * Retrieve the externally edited flag.
     *
     * @return externally edited flag
     */
    /**
     * Set the externally edited flag.
     *
     * @param projectExternallyEdited externally edited flag
     */
    var projectExternallyEdited: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.PROJECT_EXTERNALLY_EDITED) as Boolean?)
        set(projectExternallyEdited) {
            set(ProjectField.PROJECT_EXTERNALLY_EDITED, projectExternallyEdited)
        }

    /**
     * Retrieves the category text.
     *
     * @return category text
     */
    /**
     * Sets the category text.
     *
     * @param category category text
     */
    var category: String
        get() = getCachedValue(ProjectField.CATEGORY)
        set(category) = set(ProjectField.CATEGORY, category)

    /**
     * Retrieve the number of days per month.
     *
     * @return days per month
     */
    /**
     * Set the number of days per month.
     *
     * @param daysPerMonth days per month
     */
    var daysPerMonth: Number?
        get() = getCachedValue(ProjectField.DAYS_PER_MONTH)
        set(daysPerMonth) {
            if (daysPerMonth != null) {
                set(ProjectField.DAYS_PER_MONTH, daysPerMonth)
            }
        }

    /**
     * Retrieve the number of minutes per day.
     *
     * @return minutes per day
     */
    /**
     * Set the number of minutes per day.
     *
     * @param minutesPerDay minutes per day
     */
    var minutesPerDay: Number?
        get() = getCachedValue(ProjectField.MINUTES_PER_DAY)
        set(minutesPerDay) {
            if (minutesPerDay != null) {
                set(ProjectField.MINUTES_PER_DAY, minutesPerDay)
            }
        }

    /**
     * Retrieve the number of minutes per week.
     *
     * @return minutes per week
     */
    /**
     * Set the number of minutes per week.
     *
     * @param minutesPerWeek minutes per week
     */
    var minutesPerWeek: Number?
        get() = getCachedValue(ProjectField.MINUTES_PER_WEEK)
        set(minutesPerWeek) {
            if (minutesPerWeek != null) {
                set(ProjectField.MINUTES_PER_WEEK, minutesPerWeek)
            }
        }

    /**
     * Retrieve the default number of minutes per month.
     *
     * @return minutes per month
     */
    val minutesPerMonth: Number
        get() = Integer.valueOf(NumberHelper.getInt(minutesPerDay) * NumberHelper.getInt(daysPerMonth))

    /**
     * Retrieve the default number of minutes per year.
     *
     * @return minutes per year
     */
    val minutesPerYear: Number
        get() = Integer.valueOf(NumberHelper.getInt(minutesPerDay) * NumberHelper.getInt(daysPerMonth) * 12)

    /**
     * Retrieve the fiscal year start flag.
     *
     * @return fiscal year start flag
     */
    /**
     * Set the fiscal year start flag.
     *
     * @param fiscalYearStart fiscal year start
     */
    var fiscalYearStart: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.FISCAL_YEAR_START) as Boolean?)
        set(fiscalYearStart) {
            set(ProjectField.FISCAL_YEAR_START, fiscalYearStart)
        }

    /**
     * Retrieves the default task earned value method.
     *
     * @return default task earned value method
     */
    /**
     * Sets the default task earned value method.
     *
     * @param defaultTaskEarnedValueMethod default task earned value method
     */
    var defaultTaskEarnedValueMethod: EarnedValueMethod
        get() = getCachedValue(ProjectField.DEFAULT_TASK_EARNED_VALUE_METHOD) as EarnedValueMethod?
        set(defaultTaskEarnedValueMethod) {
            set(ProjectField.DEFAULT_TASK_EARNED_VALUE_METHOD, defaultTaskEarnedValueMethod)
        }

    /**
     * Retrieve the remove file properties flag.
     *
     * @return remove file properties flag
     */
    /**
     * Set the remove file properties flag.
     *
     * @param removeFileProperties remove file properties flag
     */
    var removeFileProperties: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.REMOVE_FILE_PROPERTIES) as Boolean?)
        set(removeFileProperties) {
            set(ProjectField.REMOVE_FILE_PROPERTIES, removeFileProperties)
        }

    /**
     * Retrieve the move completed ends back flag.
     *
     * @return move completed ends back flag
     */
    /**
     * Set the move completed ends back flag.
     *
     * @param moveCompletedEndsBack move completed ends back flag
     */
    var moveCompletedEndsBack: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.MOVE_COMPLETED_ENDS_BACK) as Boolean?)
        set(moveCompletedEndsBack) {
            set(ProjectField.MOVE_COMPLETED_ENDS_BACK, moveCompletedEndsBack)
        }

    /**
     * Retrieve the new tasks estimated flag.
     *
     * @return new tasks estimated flag
     */
    /**
     * Set the new tasks estimated flag.
     *
     * @param newTasksEstimated new tasks estimated flag
     */
    var newTasksEstimated: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.NEW_TASKS_ESTIMATED) as Boolean?)
        set(newTasksEstimated) {
            set(ProjectField.NEW_TASKS_ESTIMATED, newTasksEstimated)
        }

    /**
     * Retrieve the spread actual cost flag.
     *
     * @return spread actual cost flag
     */
    /**
     * Set the spread actual cost flag.
     *
     * @param spreadActualCost spread actual cost flag
     */
    var spreadActualCost: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.SPREAD_ACTUAL_COST) as Boolean?)
        set(spreadActualCost) {
            set(ProjectField.SPREAD_ACTUAL_COST, spreadActualCost)
        }

    /**
     * Retrieve the multiple critical paths flag.
     *
     * @return multiple critical paths flag
     */
    /**
     * Set the multiple critical paths flag.
     *
     * @param multipleCriticalPaths multiple critical paths flag
     */
    var multipleCriticalPaths: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.MULTIPLE_CRITICAL_PATHS) as Boolean?)
        set(multipleCriticalPaths) {
            set(ProjectField.MULTIPLE_CRITICAL_PATHS, multipleCriticalPaths)
        }

    /**
     * Retrieve the auto add new resources and tasks flag.
     *
     * @return auto add new resources and tasks flag
     */
    /**
     * Set the auto add new resources and tasks flag.
     *
     * @param autoAddNewResourcesAndTasks auto add new resources and tasks flag
     */
    var autoAddNewResourcesAndTasks: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.AUTO_ADD_NEW_RESOURCES_AND_TASKS) as Boolean?)
        set(autoAddNewResourcesAndTasks) {
            set(ProjectField.AUTO_ADD_NEW_RESOURCES_AND_TASKS, autoAddNewResourcesAndTasks)
        }

    /**
     * Retrieve the last saved date.
     *
     * @return last saved date
     */
    /**
     * Set the last saved date.
     *
     * @param lastSaved last saved date
     */
    var lastSaved: Date
        get() = getCachedValue(ProjectField.LAST_SAVED) as Date?
        set(lastSaved) {
            set(ProjectField.LAST_SAVED, lastSaved)
        }

    /**
     * Retrieve the status date.
     *
     * @return status date
     */
    /**
     * Set the status date.
     *
     * @param statusDate status date
     */
    var statusDate: Date
        get() = getCachedValue(ProjectField.STATUS_DATE) as Date?
        set(statusDate) {
            set(ProjectField.STATUS_DATE, statusDate)
        }

    /**
     * Retrieves the move remaining starts back flag.
     *
     * @return move remaining starts back flag
     */
    /**
     * Sets the move remaining starts back flag.
     *
     * @param moveRemainingStartsBack remaining starts back flag
     */
    var moveRemainingStartsBack: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.MOVE_REMAINING_STARTS_BACK) as Boolean?)
        set(moveRemainingStartsBack) {
            set(ProjectField.MOVE_REMAINING_STARTS_BACK, moveRemainingStartsBack)
        }

    /**
     * Retrieves the autolink flag.
     *
     * @return autolink flag
     */
    /**
     * Sets the autolink flag.
     *
     * @param autolink autolink flag
     */
    var autolink: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.AUTO_LINK) as Boolean?)
        set(autolink) {
            set(ProjectField.AUTO_LINK, autolink)
        }

    /**
     * Retrieves the Microsoft Project Server URL flag.
     *
     * @return Microsoft Project Server URL flag
     */
    /**
     * Sets the Microsoft Project Server URL flag.
     *
     * @param microsoftProjectServerURL Microsoft Project Server URL flag
     */
    var microsoftProjectServerURL: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.MICROSOFT_PROJECT_SERVER_URL) as Boolean?)
        set(microsoftProjectServerURL) {
            set(ProjectField.MICROSOFT_PROJECT_SERVER_URL, microsoftProjectServerURL)
        }

    /**
     * Retrieves the honor constraints flag.
     *
     * @return honor constraints flag
     */
    /**
     * Sets the honor constraints flag.
     *
     * @param honorConstraints honor constraints flag
     */
    var honorConstraints: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.HONOR_CONSTRAINTS) as Boolean?)
        set(honorConstraints) {
            set(ProjectField.HONOR_CONSTRAINTS, honorConstraints)
        }

    /**
     * Retrieve the admin project flag.
     *
     * @return admin project flag
     */
    /**
     * Set the admin project flag.
     *
     * @param adminProject admin project flag
     */
    var adminProject: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.ADMIN_PROJECT) as Boolean?)
        set(adminProject) {
            set(ProjectField.ADMIN_PROJECT, adminProject)
        }

    /**
     * Retrieves the inserted projects like summary flag.
     *
     * @return inserted projects like summary flag
     */
    /**
     * Sets the inserted projects like summary flag.
     *
     * @param insertedProjectsLikeSummary inserted projects like summary flag
     */
    var insertedProjectsLikeSummary: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.INSERTED_PROJECTS_LIKE_SUMMARY) as Boolean?)
        set(insertedProjectsLikeSummary) {
            set(ProjectField.INSERTED_PROJECTS_LIKE_SUMMARY, insertedProjectsLikeSummary)
        }

    /**
     * Retrieves the project name.
     *
     * @return project name
     */
    /**
     * Sets the project name.
     *
     * @param name project name
     */
    var name: String
        get() = getCachedValue(ProjectField.NAME)
        set(name) = set(ProjectField.NAME, name)

    /**
     * Retrieves the spread percent complete flag.
     *
     * @return spread percent complete flag
     */
    /**
     * Sets the spread percent complete flag.
     *
     * @param spreadPercentComplete spread percent complete flag
     */
    var spreadPercentComplete: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.SPREAD_PERCENT_COMPLETE) as Boolean?)
        set(spreadPercentComplete) {
            set(ProjectField.SPREAD_PERCENT_COMPLETE, spreadPercentComplete)
        }

    /**
     * Retrieve the move completed ends forward flag.
     *
     * @return move completed ends forward flag
     */
    /**
     * Sets the move completed ends forward flag.
     *
     * @param moveCompletedEndsForward move completed ends forward flag
     */
    var moveCompletedEndsForward: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.MOVE_COMPLETED_ENDS_FORWARD) as Boolean?)
        set(moveCompletedEndsForward) {
            set(ProjectField.MOVE_COMPLETED_ENDS_FORWARD, moveCompletedEndsForward)
        }

    /**
     * Retrieve the editable actual costs flag.
     *
     * @return editable actual costs flag
     */
    /**
     * Set the editable actual costs flag.
     *
     * @param editableActualCosts editable actual costs flag
     */
    var editableActualCosts: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.EDITABLE_ACTUAL_COSTS) as Boolean?)
        set(editableActualCosts) {
            set(ProjectField.EDITABLE_ACTUAL_COSTS, editableActualCosts)
        }

    /**
     * Retrieve the unique ID for this project.
     *
     * @return unique ID
     */
    /**
     * Set the unique ID for this project.
     *
     * @param uniqueID unique ID
     */
    var uniqueID: String
        get() = getCachedValue(ProjectField.UNIQUE_ID)
        set(uniqueID) = set(ProjectField.UNIQUE_ID, uniqueID)

    /**
     * Retrieve the project revision number.
     *
     * @return revision number
     */
    /**
     * Set the project revision number.
     *
     * @param revision revision number
     */
    var revision: Integer
        get() = getCachedValue(ProjectField.REVISION) as Integer?
        set(revision) {
            set(ProjectField.REVISION, revision)
        }

    /**
     * Retrieve the new tasks effort driven flag.
     *
     * @return new tasks effort driven flag
     */
    /**
     * Sets the new tasks effort driven flag.
     *
     * @param newTasksEffortDriven new tasks effort driven flag
     */
    var newTasksEffortDriven: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.NEW_TASKS_EFFORT_DRIVEN) as Boolean?)
        set(newTasksEffortDriven) {
            set(ProjectField.NEW_TASKS_EFFORT_DRIVEN, newTasksEffortDriven)
        }

    /**
     * Retrieve the move remaining starts forward flag.
     *
     * @return move remaining starts forward flag
     */
    /**
     * Set the move remaining starts forward flag.
     *
     * @param moveRemainingStartsForward move remaining starts forward flag
     */
    var moveRemainingStartsForward: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.MOVE_REMAINING_STARTS_FORWARD) as Boolean?)
        set(moveRemainingStartsForward) {
            set(ProjectField.MOVE_REMAINING_STARTS_FORWARD, moveRemainingStartsForward)
        }

    /**
     * Retrieve the actuals in sync flag.
     *
     * @return actuals in sync flag
     */
    /**
     * Set the actuals in sync flag.
     *
     * @param actualsInSync actuals in sync flag
     */
    var actualsInSync: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.ACTUALS_IN_SYNC) as Boolean?)
        set(actualsInSync) {
            set(ProjectField.ACTUALS_IN_SYNC, actualsInSync)
        }

    /**
     * Retrieve the default task type.
     *
     * @return default task type
     */
    /**
     * Set the default task type.
     *
     * @param defaultTaskType default task type
     */
    var defaultTaskType: TaskType
        get() = getCachedValue(ProjectField.DEFAULT_TASK_TYPE) as TaskType?
        set(defaultTaskType) {
            set(ProjectField.DEFAULT_TASK_TYPE, defaultTaskType)
        }

    /**
     * Retrieve the earned value method.
     *
     * @return earned value method
     */
    /**
     * Set the earned value method.
     *
     * @param earnedValueMethod earned value method
     */
    var earnedValueMethod: EarnedValueMethod
        get() = getCachedValue(ProjectField.EARNED_VALUE_METHOD) as EarnedValueMethod?
        set(earnedValueMethod) {
            set(ProjectField.EARNED_VALUE_METHOD, earnedValueMethod)
        }

    /**
     * Retrieve the project creation date.
     *
     * @return project creation date
     */
    /**
     * Set the project creation date.
     *
     * @param creationDate project creation date
     */
    var creationDate: Date
        get() = getCachedValue(ProjectField.CREATION_DATE) as Date?
        set(creationDate) {
            set(ProjectField.CREATION_DATE, creationDate)
        }

    /**
     * Retrieve the extended creation date.
     *
     * @return extended creation date
     */
    /**
     * Set the extended creation date.
     *
     * @param creationDate extended creation date
     */
    var extendedCreationDate: Date
        get() = getCachedValue(ProjectField.EXTENDED_CREATION_DATE) as Date?
        set(creationDate) {
            set(ProjectField.EXTENDED_CREATION_DATE, creationDate)
        }

    /**
     * Retrieve the default fixed cost accrual type.
     *
     * @return default fixed cost accrual type
     */
    /**
     * Sets the default fixed cost accrual type.
     *
     * @param defaultFixedCostAccrual default fixed cost accrual type
     */
    var defaultFixedCostAccrual: AccrueType
        get() = getCachedValue(ProjectField.DEFAULT_FIXED_COST_ACCRUAL) as AccrueType?
        set(defaultFixedCostAccrual) {
            set(ProjectField.DEFAULT_FIXED_COST_ACCRUAL, defaultFixedCostAccrual)
        }

    /**
     * Retrieve the critical slack limit.
     *
     * @return critical slack limit
     */
    /**
     * Set the critical slack limit.
     *
     * @param criticalSlackLimit critical slack limit
     */
    var criticalSlackLimit: Integer
        get() = getCachedValue(ProjectField.CRITICAL_SLACK_LIMIT) as Integer?
        set(criticalSlackLimit) {
            set(ProjectField.CRITICAL_SLACK_LIMIT, criticalSlackLimit)
        }

    /**
     * Retrieve the number of the baseline to use for earned value
     * calculations.
     *
     * @return baseline for earned value
     */
    /**
     * Set the number of the baseline to use for earned value
     * calculations.
     *
     * @param baselineForEarnedValue baseline for earned value
     */
    var baselineForEarnedValue: Integer
        get() = getCachedValue(ProjectField.BASELINE_FOR_EARNED_VALUE) as Integer?
        set(baselineForEarnedValue) {
            set(ProjectField.BASELINE_FOR_EARNED_VALUE, baselineForEarnedValue)
        }

    /**
     * Retrieves the fiscal year start month (January=1, December=12).
     *
     * @return fiscal year start month
     */
    /**
     * Sets the fiscal year start month (January=1, December=12).
     *
     * @param fiscalYearStartMonth fiscal year start month
     */
    var fiscalYearStartMonth: Integer
        get() = getCachedValue(ProjectField.FISCAL_YEAR_START_MONTH) as Integer?
        set(fiscalYearStartMonth) {
            set(ProjectField.FISCAL_YEAR_START_MONTH, fiscalYearStartMonth)
        }

    /**
     * Retrieve the flag indicating if new tasks should default to the
     * project start date (true) or the current date (false).
     *
     * @return new task start is project start
     */
    /**
     * Sets the flag indicating if new tasks should default to the
     * project start date (true) or the current date (false).
     *
     * @param newTaskStartIsProjectStart new task start is project start
     */
    var newTaskStartIsProjectStart: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.NEW_TASK_START_IS_PROJECT_START) as Boolean?)
        set(newTaskStartIsProjectStart) {
            set(ProjectField.NEW_TASK_START_IS_PROJECT_START, newTaskStartIsProjectStart)
        }

    /**
     * Retrieve the week start day.
     *
     * @return week start day
     */
    /**
     * Set the week start day.
     *
     * @param weekStartDay week start day
     */
    var weekStartDay: Day
        get() = getCachedValue(ProjectField.WEEK_START_DAY) as Day?
        set(weekStartDay) {
            set(ProjectField.WEEK_START_DAY, weekStartDay)
        }

    /**
     * Retrieves the calculate multiple critical paths flag.
     *
     * @return boolean flag
     */
    /**
     * Sets the calculate multiple critical paths flag.
     *
     * @param flag boolean flag
     */
    var calculateMultipleCriticalPaths: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.CALCULATE_MULTIPLE_CRITICAL_PATHS) as Boolean?)
        set(flag) {
            set(ProjectField.CALCULATE_MULTIPLE_CRITICAL_PATHS, flag)
        }

    /**
     * Retrieve the currency code for this project.
     *
     * @return currency code
     */
    /**
     * Set the currency code for this project.
     *
     * @param currencyCode currency code
     */
    var currencyCode: String
        get() = getCachedValue(ProjectField.CURRENCY_CODE)
        set(currencyCode) = set(ProjectField.CURRENCY_CODE, currencyCode)

    /**
     * Retrieve a map of custom document properties.
     *
     * @return the Document Summary Information Map
     */
    /**
     * Sets a map of custom document properties.
     *
     * @param customProperties The Document Summary Information Map
     */
    var customProperties: Map<String, Object>
        @SuppressWarnings("unchecked") get() = getCachedValue(ProjectField.CUSTOM_PROPERTIES)
        set(customProperties) = set(ProjectField.CUSTOM_PROPERTIES, customProperties)

    /**
     * Gets the hyperlink base for this Project. If any.
     *
     * @return Hyperlink base
     */
    /**
     * Sets the hyperlink base for this Project.
     *
     * @param hyperlinkBase Hyperlink base
     */
    var hyperlinkBase: String
        get() = getCachedValue(ProjectField.HYPERLINK_BASE)
        set(hyperlinkBase) = set(ProjectField.HYPERLINK_BASE, hyperlinkBase)

    /**
     * Retrieves the "show project summary task" flag.
     *
     * @return boolean flag
     */
    /**
     * Sets the "show project summary task" flag.
     *
     * @param value boolean flag
     */
    var showProjectSummaryTask: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.SHOW_PROJECT_SUMMARY_TASK) as Boolean?)
        set(value) {
            set(ProjectField.SHOW_PROJECT_SUMMARY_TASK, value)
        }

    /**
     * Retrieve a baseline value.
     *
     * @return baseline value
     */
    /**
     * Set a baseline value.
     *
     * @param value baseline value
     */
    var baselineDate: Date
        get() = getCachedValue(ProjectField.BASELINE_DATE) as Date?
        set(value) {
            set(ProjectField.BASELINE_DATE, value)
        }

    /**
     * Retrieve the template property.
     *
     * @return template property
     */
    /**
     * Set the template property.
     *
     * @param template property value
     */
    var template: String
        get() = getCachedValue(ProjectField.TEMPLATE)
        set(template) = set(ProjectField.TEMPLATE, template)

    /**
     * Retrieve the project user property.
     *
     * @return project user property
     */
    /**
     * Set the project user property.
     *
     * @param projectUser project user property
     */
    var lastAuthor: String
        get() = getCachedValue(ProjectField.LAST_AUTHOR)
        set(projectUser) = set(ProjectField.LAST_AUTHOR, projectUser)

    /**
     * Retrieve the last printed property.
     *
     * @return last printed property
     */
    /**
     * Set the last printed property.
     *
     * @param lastPrinted property value
     */
    var lastPrinted: Date
        get() = getCachedValue(ProjectField.LASTPRINTED) as Date?
        set(lastPrinted) {
            set(ProjectField.LASTPRINTED, lastPrinted)
        }

    /**
     * Retrieve the application property.
     *
     * @return property value
     */
    /**
     * Set the application property.
     *
     * @param application property value
     */
    var shortApplicationName: String
        get() = getCachedValue(ProjectField.SHORT_APPLICATION_NAME)
        set(application) = set(ProjectField.SHORT_APPLICATION_NAME, application)

    /**
     * Retrieve the editing time property.
     *
     * @return property value
     */
    /**
     * Set the editing time property.
     *
     * @param editingTime editing time property
     */
    var editingTime: Integer
        get() = getCachedValue(ProjectField.EDITING_TIME) as Integer?
        set(editingTime) {
            set(ProjectField.EDITING_TIME, editingTime)
        }

    /**
     * Retrieve the format property.
     *
     * @return property value
     */
    /**
     * Set the format property.
     *
     * @param format property value
     */
    var presentationFormat: String
        get() = getCachedValue(ProjectField.PRESENTATION_FORMAT)
        set(format) = set(ProjectField.PRESENTATION_FORMAT, format)

    /**
     * Retrieve the content type property.
     *
     * @return content type property
     */
    /**
     * Set the content type property.
     *
     * @param contentType property value
     */
    var contentType: String
        get() = getCachedValue(ProjectField.CONTENT_TYPE)
        set(contentType) = set(ProjectField.CONTENT_TYPE, contentType)

    /**
     * Retrieve the content status property.
     *
     * @return property value
     */
    /**
     * Set the content status property.
     *
     * @param contentStatus property value
     */
    var contentStatus: String
        get() = getCachedValue(ProjectField.CONTENT_STATUS)
        set(contentStatus) = set(ProjectField.CONTENT_STATUS, contentStatus)

    /**
     * Retrieve the language property.
     *
     * @return property value
     */
    /**
     * Set the language property.
     *
     * @param language property value
     */
    var language: String
        get() = getCachedValue(ProjectField.LANGUAGE)
        set(language) = set(ProjectField.LANGUAGE, language)

    /**
     * Retrieve the document version property.
     *
     * @return property value
     */
    /**
     * Set the document version property.
     *
     * @param documentVersion property value
     */
    var documentVersion: String
        get() = getCachedValue(ProjectField.DOCUMENT_VERSION)
        set(documentVersion) = set(ProjectField.DOCUMENT_VERSION, documentVersion)

    /**
     * Retrieves the delimiter character, "," by default.
     *
     * @return delimiter character
     */
    /**
     * Sets the delimiter character, "," by default.
     *
     * @param delimiter delimiter character
     */
    var mpxDelimiter: Char
        get() = getCachedCharValue(ProjectField.MPX_DELIMITER, DEFAULT_MPX_DELIMITER)
        set(delimiter) {
            set(ProjectField.MPX_DELIMITER, Character.valueOf(delimiter))
        }

    /**
     * Program name file created by.
     *
     * @return program name
     */
    /**
     * Program name file created by.
     *
     * @param programName system name
     */
    var mpxProgramName: String
        get() = getCachedValue(ProjectField.MPX_PROGRAM_NAME)
        set(programName) = set(ProjectField.MPX_PROGRAM_NAME, programName)

    /**
     * Version of the MPX file.
     *
     * @return MPX file version
     */
    /**
     * Version of the MPX file.
     *
     * @param version MPX file version
     */
    var mpxFileVersion: FileVersion
        get() = getCachedValue(ProjectField.MPX_FILE_VERSION) as FileVersion?
        set(version) {
            set(ProjectField.MPX_FILE_VERSION, version)
        }

    /**
     * Retrieves the codepage.
     *
     * @return code page type
     */
    /**
     * Sets the codepage.
     *
     * @param codePage code page type
     */
    var mpxCodePage: CodePage
        get() = getCachedValue(ProjectField.MPX_CODE_PAGE) as CodePage?
        set(codePage) {
            set(ProjectField.MPX_CODE_PAGE, codePage)
        }

    /**
     * Gets the project file path.
     *
     * @return project file path
     */
    /**
     * Sets the project file path.
     *
     * @param projectFilePath project file path
     */
    var projectFilePath: String
        get() = getCachedValue(ProjectField.PROJECT_FILE_PATH)
        set(projectFilePath) = set(ProjectField.PROJECT_FILE_PATH, projectFilePath)

    /**
     * Retrieves the name of the application used to create this project data.
     *
     * @return application name
     */
    /**
     * Sets the name of the application used to create this project data.
     *
     * @param name application name
     */
    var fullApplicationName: String
        get() = getCachedValue(ProjectField.FULL_APPLICATION_NAME)
        set(name) = set(ProjectField.FULL_APPLICATION_NAME, name)

    /**
     * Retrieves the version of the application used to create this project.
     *
     * @return application name
     */
    /**
     * Sets the version of the application used to create this project.
     *
     * @param version application version
     */
    var applicationVersion: Integer
        get() = getCachedValue(ProjectField.APPLICATION_VERSION) as Integer?
        set(version) {
            set(ProjectField.APPLICATION_VERSION, version)
        }

    /**
     * This method retrieves a value representing the type of MPP file
     * that has been read. Currently this method will return the value 8 for
     * an MPP8 file (Project 98), 9 for an MPP9 file (Project 2000 and
     * Project 2002), 12 for an MPP12 file (Project 2003, Project 2007) and 14 for an
     * MPP14 file (Project 2010 and Project 2013).
     *
     * @return integer representing the file type
     */
    /**
     * Used internally to set the file type.
     *
     * @param fileType file type
     */
    var mppFileType: Integer
        get() = getCachedValue(ProjectField.MPP_FILE_TYPE) as Integer?
        set(fileType) {
            set(ProjectField.MPP_FILE_TYPE, fileType)
        }

    /**
     * Retrieve a flag indicating if auto filter is enabled.
     *
     * @return auto filter flag
     */
    /**
     * Sets a flag indicating if auto filter is enabled.
     *
     * @param autoFilter boolean flag
     */
    var autoFilter: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.AUTOFILTER) as Boolean?)
        set(autoFilter) {
            set(ProjectField.AUTOFILTER, autoFilter)
        }

    /**
     * Retrieves the vendor of the file used to populate this ProjectFile instance.
     *
     * @return file type
     */
    /**
     * Sets the vendor of file used to populate this ProjectFile instance.
     *
     * @param type file type
     */
    var fileApplication: String
        get() = getCachedValue(ProjectField.FILE_APPLICATION)
        set(type) = set(ProjectField.FILE_APPLICATION, type)

    /**
     * Retrieves the type of file used to populate this ProjectFile instance.
     *
     * @return file type
     */
    /**
     * Sets the type of file used to populate this ProjectFile instance.
     *
     * @param type file type
     */
    var fileType: String
        get() = getCachedValue(ProjectField.FILE_TYPE)
        set(type) = set(ProjectField.FILE_TYPE, type)

    /**
     * Retrieves the export flag used to specify if the project was chosen to export from P6.
     * Projects that have external relationships may be included in an export, even when not
     * specifically flagged in the export. This flag differentiates those projects
     *
     * @return export boolean flag
     */
    /**
     * Sets the export flag to populate this ProjectFile instance.
     *
     * @param value boolean flag
     */
    var exportFlag: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ProjectField.EXPORT_FLAG) as Boolean?)
        set(value) {
            set(ProjectField.EXPORT_FLAG, value)
        }

    /**
     * Array of field values.
     */
    private val m_array = arrayOfNulls<Object>(ProjectField.MAX_VALUE)

    /**
     * Listeners.
     */
    private var m_listeners: List<FieldListener>? = null

    init {

        //
        // Configure MPX File Creation Record Settings
        //
        mpxDelimiter = DEFAULT_MPX_DELIMITER
        mpxProgramName = "Microsoft Project for Windows"
        mpxFileVersion = FileVersion.VERSION_4_0
        mpxCodePage = CodePage.ANSI

        //
        // Configure MPX Date Time Settings and Currency Settings Records
        //
        currencySymbol = DEFAULT_CURRENCY_SYMBOL
        symbolPosition = DEFAULT_CURRENCY_SYMBOL_POSITION
        currencyDigits = DEFAULT_CURRENCY_DIGITS
        thousandsSeparator = DEFAULT_THOUSANDS_SEPARATOR
        decimalSeparator = DEFAULT_DECIMAL_SEPARATOR

        dateOrder = DateOrder.DMY
        timeFormat = ProjectTimeFormat.TWELVE_HOUR
        defaultStartTime = DateHelper.getTimeFromMinutesPastMidnight(Integer.valueOf(480))
        dateSeparator = DEFAULT_DATE_SEPARATOR
        timeSeparator = DEFAULT_TIME_SEPARATOR
        amText = "am"
        pmText = "pm"
        dateFormat = ProjectDateFormat.DD_MM_YYYY
        barTextDateFormat = ProjectDateFormat.DD_MM_YYYY

        //
        // Configure MPX Default Settings Record
        //
        defaultDurationUnits = TimeUnit.DAYS
        defaultDurationIsFixed = false
        defaultWorkUnits = TimeUnit.HOURS
        minutesPerDay = Integer.valueOf(480)
        minutesPerWeek = Integer.valueOf(2400)
        defaultStandardRate = Rate(10, TimeUnit.HOURS)
        defaultOvertimeRate = Rate(15, TimeUnit.HOURS)
        updatingTaskStatusUpdatesResourceStatus = true
        splitInProgressTasks = false

        //
        // Configure MPX Project Header Record
        //
        projectTitle = "Project1"
        company = null
        manager = null
        defaultCalendarName = DEFAULT_CALENDAR_NAME
        startDate = null
        finishDate = null
        scheduleFrom = DEFAULT_SCHEDULE_FROM
        currentDate = Date()
        comments = null
        cost = DEFAULT_COST
        baselineCost = DEFAULT_COST
        actualCost = DEFAULT_COST
        work = DEFAULT_WORK
        baselineWork = DEFAULT_WORK
        actualWork = DEFAULT_WORK
        work2 = DEFAULT_WORK2
        duration = DEFAULT_DURATION
        baselineDuration = DEFAULT_DURATION
        actualDuration = DEFAULT_DURATION
        percentageComplete = DEFAULT_PERCENT_COMPLETE
        baselineStart = null
        baselineFinish = null
        actualStart = null
        actualFinish = null
        startVariance = DEFAULT_DURATION
        finishVariance = DEFAULT_DURATION
        subject = null
        author = null
        keywords = null

        //
        // Configure non-MPX attributes
        //
        projectExternallyEdited = false
        minutesPerDay = DEFAULT_MINUTES_PER_DAY
        daysPerMonth = DEFAULT_DAYS_PER_MONTH
        minutesPerWeek = DEFAULT_MINUTES_PER_WEEK
        fiscalYearStart = false
        defaultTaskEarnedValueMethod = EarnedValueMethod.PERCENT_COMPLETE
        newTasksEstimated = true
        autoAddNewResourcesAndTasks = true
        autolink = true
        microsoftProjectServerURL = true
        defaultTaskType = TaskType.FIXED_UNITS
        defaultFixedCostAccrual = AccrueType.END
        criticalSlackLimit = DEFAULT_CRITICAL_SLACK_LIMIT
        baselineForEarnedValue = DEFAULT_BASELINE_FOR_EARNED_VALUE
        fiscalYearStartMonth = DEFAULT_FISCAL_YEAR_START_MONTH
        newTaskStartIsProjectStart = true
        weekStartDay = DEFAULT_WEEK_START_DAY
    }

    /**
     * Retrieve a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @return baseline value
     */
    fun getBaselineDate(baselineNumber: Int): Date {
        return getCachedValue(selectField(ProjectFieldLists.BASELINE_DATES, baselineNumber)) as Date?
    }

    /**
     * Set a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @param value baseline value
     */
    fun setBaselineDate(baselineNumber: Int, value: Date) {
        set(selectField(ProjectFieldLists.BASELINE_DATES, baselineNumber), value)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun addFieldListener(listener: FieldListener) {
        if (m_listeners == null) {
            m_listeners = LinkedList<FieldListener>()
        }
        m_listeners!!.add(listener)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun removeFieldListener(listener: FieldListener) {
        if (m_listeners != null) {
            m_listeners!!.remove(listener)
        }
    }

    /**
     * Maps a field index to a TaskField instance.
     *
     * @param fields array of fields used as the basis for the mapping.
     * @param index required field index
     * @return TaskField instance
     */
    private fun selectField(fields: Array<ProjectField>, index: Int): ProjectField {
        if (index < 1 || index > fields.size) {
            throw IllegalArgumentException("$index is not a valid field index")
        }
        return fields[index - 1]
    }

    /**
     * Handles retrieval of primitive char type.
     *
     * @param field required field
     * @param defaultValue default value if field is missing
     * @return char value
     */
    private fun getCachedCharValue(field: FieldType, defaultValue: Char): Char {
        val c = getCachedValue(field) as Character?
        return if (c == null) defaultValue else c!!.charValue()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun getCachedValue(field: FieldType?): Object? {
        return if (field == null) null else m_array[field!!.getValue()]
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun getCurrentValue(field: FieldType): Object? {
        return getCachedValue(field)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    operator fun set(field: FieldType?, value: Object) {
        if (field != null) {
            val index = field!!.getValue()
            m_array[index] = value
        }
    }

    /**
     * This method inserts a name value pair into internal storage.
     *
     * @param field task field
     * @param value attribute value
     */
    private operator fun set(field: FieldType, value: Boolean) {
        set(field, if (value) Boolean.TRUE else Boolean.FALSE)
    }

    companion object {

        /**
         * Default time separator character.
         */
        private val DEFAULT_TIME_SEPARATOR = ':'

        /**
         * Default date separator character.
         */
        private val DEFAULT_DATE_SEPARATOR = '/'

        /**
         * Default thousands separator character.
         */
        private val DEFAULT_THOUSANDS_SEPARATOR = ','

        /**
         * Default decimal separator character.
         */
        private val DEFAULT_DECIMAL_SEPARATOR = '.'

        /**
         * Default currency symbol.
         */
        private val DEFAULT_CURRENCY_SYMBOL = "$"

        /**
         * Default currency digits.
         */
        private val DEFAULT_CURRENCY_DIGITS = Integer.valueOf(2)

        /**
         * Default currency symbol position.
         */
        private val DEFAULT_CURRENCY_SYMBOL_POSITION = CurrencySymbolPosition.BEFORE

        /**
         * Default cost value.
         */
        private val DEFAULT_COST = Double.valueOf(0)

        /**
         * Default MPX delimiter.
         */
        private val DEFAULT_MPX_DELIMITER = ','

        /**
         * Default critical slack limit.
         */
        private val DEFAULT_CRITICAL_SLACK_LIMIT = Integer.valueOf(0)

        /**
         * Default baseline for earned value.
         */
        private val DEFAULT_BASELINE_FOR_EARNED_VALUE = Integer.valueOf(0)

        /**
         * Default fiscal year start month.
         */
        private val DEFAULT_FISCAL_YEAR_START_MONTH = Integer.valueOf(1)

        /**
         * Default week start day.
         */
        private val DEFAULT_WEEK_START_DAY = Day.MONDAY

        /**
         * Default work value.
         */
        private val DEFAULT_WORK = Duration.getInstance(0, TimeUnit.HOURS)

        /**
         * Default work 2 value.
         */
        private val DEFAULT_WORK2 = Double.valueOf(0)

        /**
         * Default duration value.
         */
        private val DEFAULT_DURATION = Duration.getInstance(0, TimeUnit.DAYS)

        /**
         * Default schedule from value.
         */
        private val DEFAULT_SCHEDULE_FROM = ScheduleFrom.START

        /**
         * Default percent complete value.
         */
        private val DEFAULT_PERCENT_COMPLETE = Double.valueOf(0)

        /**
         * Default calendar name.
         */
        private val DEFAULT_CALENDAR_NAME = "Standard"

        /**
         * Default minutes per day.
         */
        private val DEFAULT_MINUTES_PER_DAY = Integer.valueOf(480)

        /**
         * Default days per month.
         */
        private val DEFAULT_DAYS_PER_MONTH = Integer.valueOf(20)

        /**
         * Default minutes per week.
         */
        private val DEFAULT_MINUTES_PER_WEEK = Integer.valueOf(2400)
    }
}
