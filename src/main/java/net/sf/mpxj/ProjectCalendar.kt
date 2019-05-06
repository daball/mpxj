/*
 * file:       ProjectCalendar.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       28/11/2003
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

import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.WeakHashMap

import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper

/**
 * This class represents the a Calendar Definition record. Both base calendars
 * and calendars derived from base calendars are represented by instances
 * of this class. The class is used to define the working and non-working days
 * of the week. The default calendar defines Monday to Friday as working days.
 */
class ProjectCalendar : ProjectCalendarWeek, ProjectEntityWithUniqueID {

    /**
     * Retrieve the number of minutes per day for this calendar.
     *
     * @return minutes per day
     */
    val minutesPerDay: Int
        get() = if (m_minutesPerDay == null) NumberHelper.getInt(parentFile!!.projectProperties.minutesPerDay) else m_minutesPerDay!!.intValue()

    /**
     * Retrieve the number of minutes per week for this calendar.
     *
     * @return minutes per week
     */
    val minutesPerWeek: Int
        get() = if (m_minutesPerWeek == null) NumberHelper.getInt(parentFile!!.projectProperties.minutesPerWeek) else m_minutesPerWeek!!.intValue()

    /**
     * Retrieve the number of minutes per month for this calendar.
     *
     * @return minutes per month
     */
    val minutesPerMonth: Int
        get() = if (m_minutesPerMonth == null) NumberHelper.getInt(parentFile!!.projectProperties.minutesPerMonth) else m_minutesPerMonth!!.intValue()

    /**
     * Retrieve the number of minutes per year for this calendar.
     *
     * @return minutes per year
     */
    val minutesPerYear: Int
        get() = if (m_minutesPerYear == null) NumberHelper.getInt(parentFile!!.projectProperties.minutesPerYear) else m_minutesPerYear!!.intValue()

    /**
     * Retrieve the work weeks associated with this calendar.
     *
     * @return list of work weeks
     */
    val workWeeks: List<ProjectCalendarWeek>
        get() = m_workWeeks

    /**
     * This method retrieves a list of exceptions to the current calendar.
     *
     * @return List of calendar exceptions
     */
    val calendarExceptions: List<ProjectCalendarException>
        get() {
            sortExceptions()
            return m_exceptions
        }

    /**
     * Sets the ProjectCalendar instance from which this calendar is derived.
     *
     * @param calendar base calendar instance
     */
    override// I've seen a malformed MSPDI file which sets the parent calendar to itself.
    // Silently ignore this here.
    var parent: ProjectCalendar?
        @Override get() = super.parent as ProjectCalendar?
        set(calendar) {
            if (calendar != this) {
                if (parent != null) {
                    parent!!.removeDerivedCalendar(this)
                }

                super.parent = calendar

                calendar?.addDerivedCalendar(this)
                clearWorkingDateCache()
            }
        }

    /**
     * Accessor method to retrieve the unique ID of this calendar.
     *
     * @return calendar unique identifier
     */
    /**
     * Modifier method to set the unique ID of this calendar.
     *
     * @param uniqueID unique identifier
     */
    override var uniqueID: Integer?
        @Override get() = m_uniqueID
        @Override set(uniqueID) {
            val parent = parentFile

            if (m_uniqueID != null) {
                parent!!.calendars.unmapUniqueID(m_uniqueID)
            }

            parent!!.calendars.mapUniqueID(uniqueID, this)

            m_uniqueID = uniqueID
        }

    /**
     * Retrieve the resource to which this calendar is linked.
     *
     * @return resource instance
     */
    /**
     * Sets the resource to which this calendar is linked. Note that this
     * method updates the calendar's name to be the same as the resource name.
     * If the resource does not yet have a name, then the calendar is given
     * a default name.
     *
     * @param resource resource instance
     */
    var resource: Resource?
        get() = m_resource
        set(resource) {
            m_resource = resource
            var name: String? = m_resource!!.name
            if (name == null || name.length() === 0) {
                name = "Unnamed Resource"
            }
            name = name
        }

    /**
     * Retrieve a list of derived calendars.
     *
     * @return list of derived calendars
     */
    val derivedCalendars: List<ProjectCalendar>
        get() = m_derivedCalendars

    /**
     * Accessor method allowing retrieval of ProjectFile reference.
     *
     * @return reference to this the parent ProjectFile instance
     */
    val parentFile: ProjectFile?
        get() = m_projectFile

    /**
     * Reference to parent ProjectFile.
     */
    private var m_projectFile: ProjectFile? = null

    /**
     * Unique identifier of this calendar.
     */
    private var m_uniqueID = Integer.valueOf(0)

    /**
     * List of exceptions to the base calendar.
     */
    private val m_exceptions = ArrayList<ProjectCalendarException>()

    /**
     * List of exceptions, including expansion of recurring exceptions.
     */
    private val m_expandedExceptions = ArrayList<ProjectCalendarException>()

    /**
     * Flag indicating if the list of exceptions is sorted.
     */
    private var m_exceptionsSorted: Boolean = false

    /**
     * Flag indicating if the list of weeks is sorted.
     */
    private var m_weeksSorted: Boolean = false

    /**
     * This resource to which this calendar is attached.
     */
    private var m_resource: Resource? = null

    /**
     * List of calendars derived from this calendar instance.
     */
    private val m_derivedCalendars = ArrayList<ProjectCalendar>()

    /**
     * Caches used to speed up date calculations.
     */
    private val m_workingDateCache = WeakHashMap<DateRange, Long>()
    private val m_startTimeCache = WeakHashMap<Date, Date>()
    private var m_getDateLastStartDate: Date? = null
    private var m_getDateLastRemainingMinutes: Double = 0.toDouble()
    private var m_getDateLastResult: Date? = null

    /**
     * Work week definitions.
     */
    private val m_workWeeks = ArrayList<ProjectCalendarWeek>()

    private var m_minutesPerDay: Integer? = null
    private var m_minutesPerWeek: Integer? = null
    private var m_minutesPerMonth: Integer? = null
    private var m_minutesPerYear: Integer? = null

    /**
     * Default constructor.
     *
     * @param file the parent file to which this record belongs.
     */
    constructor(file: ProjectFile) {
        m_projectFile = file

        if (file.projectConfig.autoCalendarUniqueID == true) {
            uniqueID = Integer.valueOf(file.projectConfig.nextCalendarUniqueID)
        }
    }

    /**
     * Set the number of minutes per day for this calendar.
     *
     * @param minutes number of minutes
     */
    fun setMinutesPerDay(minutes: Integer) {
        m_minutesPerDay = minutes
    }

    /**
     * Set the number of minutes per week for this calendar.
     *
     * @param minutes number of minutes
     */
    fun setMinutesPerWeek(minutes: Integer) {
        m_minutesPerWeek = minutes
    }

    /**
     * Set the number of minutes per month for this calendar.
     *
     * @param minutes number of minutes
     */
    fun setMinutesPerMonth(minutes: Integer) {
        m_minutesPerMonth = minutes
    }

    /**
     * Set the number of minutes per year for this calendar.
     *
     * @param minutes number of minutes
     */
    fun setMinutesPerYear(minutes: Integer) {
        m_minutesPerYear = minutes
    }

    /**
     * Add an empty work week.
     *
     * @return new work week
     */
    fun addWorkWeek(): ProjectCalendarWeek {
        val week = ProjectCalendarWeek()
        week.parent = this
        m_workWeeks.add(week)
        m_weeksSorted = false
        clearWorkingDateCache()
        return week
    }

    /**
     * Clears the list of calendar exceptions.
     */
    fun clearWorkWeeks() {
        m_workWeeks.clear()
        m_weeksSorted = false
        clearWorkingDateCache()
    }

    /**
     * Used to add exceptions to the calendar. The MPX standard defines
     * a limit of 250 exceptions per calendar.
     *
     * @param fromDate exception start date
     * @param toDate exception end date
     * @return ProjectCalendarException instance
     */
    fun addCalendarException(fromDate: Date?, toDate: Date?): ProjectCalendarException {
        val bce = ProjectCalendarException(fromDate, toDate)
        m_exceptions.add(bce)
        m_expandedExceptions.clear()
        m_exceptionsSorted = false
        clearWorkingDateCache()
        return bce
    }

    /**
     * Clears the list of calendar exceptions.
     */
    fun clearCalendarExceptions() {
        m_exceptions.clear()
        m_expandedExceptions.clear()
        m_exceptionsSorted = false
        clearWorkingDateCache()
    }

    /**
     * Used to add working hours to the calendar. Note that the MPX file
     * definition allows a maximum of 7 calendar hours records to be added to
     * a single calendar.
     *
     * @param day day number
     * @return new ProjectCalendarHours instance
     */
    @Override
    override fun addCalendarHours(day: Day?): ProjectCalendarHours {
        clearWorkingDateCache()
        return super.addCalendarHours(day)
    }

    /**
     * Attaches a pre-existing set of hours to the correct
     * day within the calendar.
     *
     * @param hours calendar hours instance
     */
    @Override
    override fun attachHoursToDay(hours: ProjectCalendarHours) {
        clearWorkingDateCache()
        super.attachHoursToDay(hours)
    }

    /**
     * Removes a set of calendar hours from the day to which they
     * are currently attached.
     *
     * @param hours calendar hours instance
     */
    @Override
    override fun removeHoursFromDay(hours: ProjectCalendarHours) {
        clearWorkingDateCache()
        super.removeHoursFromDay(hours)
    }

    /**
     * Method indicating whether a day is a working or non-working day.
     *
     * @param day required day
     * @return true if this is a working day
     */
    fun isWorkingDay(day: Day): Boolean {
        val value = getWorkingDay(day)
        val result: Boolean

        if (value === DayType.DEFAULT) {
            val cal = parent
            if (cal != null) {
                result = cal.isWorkingDay(day)
            } else {
                result = day !== Day.SATURDAY && day !== Day.SUNDAY
            }
        } else {
            result = value === DayType.WORKING
        }

        return result
    }

    /**
     * This method is provided to allow an absolute period of time
     * represented by start and end dates into a duration in working
     * days based on this calendar instance. This method takes account
     * of any exceptions defined for this calendar.
     *
     * @param startDate start of the period
     * @param endDate end of the period
     * @return new Duration object
     */
    fun getDuration(startDate: Date, endDate: Date): Duration {
        val cal = DateHelper.popCalendar(startDate)
        var days = getDaysInRange(startDate, endDate)
        var duration = 0
        var day = Day.getInstance(cal.get(Calendar.DAY_OF_WEEK))

        while (days > 0) {
            if (isWorkingDate(cal.getTime(), day) == true) {
                ++duration
            }

            --days
            day = day.getNextDay()
            cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1)
        }
        DateHelper.pushCalendar(cal)

        return Duration.getInstance(duration, TimeUnit.DAYS)
    }

    /**
     * Retrieves the time at which work starts on the given date, or returns
     * null if this is a non-working day.
     *
     * @param date Date instance
     * @return start time, or null for non-working day
     */
    fun getStartTime(date: Date): Date? {
        var result = m_startTimeCache.get(date)
        if (result == null) {
            val ranges = getRanges(date, null, null)
            if (ranges == null) {
                result = parentFile!!.projectProperties.defaultStartTime
            } else {
                result = ranges.getRange(0).getStart()
            }
            result = DateHelper.getCanonicalTime(result)
            m_startTimeCache.put(Date(date.getTime()), result)
        }
        return result
    }

    /**
     * Retrieves the time at which work finishes on the given date, or returns
     * null if this is a non-working day.
     *
     * @param date Date instance
     * @return finish time, or null for non-working day
     */
    fun getFinishTime(date: Date?): Date? {
        var result: Date? = null

        if (date != null) {
            val ranges = getRanges(date, null, null)
            if (ranges == null) {
                result = parentFile!!.projectProperties.defaultEndTime
                result = DateHelper.getCanonicalTime(result)
            } else {
                result = ranges.getRange(0).getStart()
                val rangeStart = result
                val rangeFinish = ranges.getRange(ranges.rangeCount - 1).getEnd()
                val startDay = DateHelper.getDayStartDate(rangeStart)
                val finishDay = DateHelper.getDayStartDate(rangeFinish)

                result = DateHelper.getCanonicalTime(rangeFinish)

                //
                // Handle the case where the end of the range is at midnight -
                // this will show up as the start and end days not matching
                //
                if (startDay != null && finishDay != null && startDay.getTime() !== finishDay!!.getTime()) {
                    result = DateHelper.addDays(result, 1)
                }
            }
        }
        return result
    }

    /**
     * Given a start date and a duration, this method calculates the
     * end date. It takes account of working hours in each day, non working
     * days and calendar exceptions. If the returnNextWorkStart parameter is
     * set to true, the method will return the start date and time of the next
     * working period if the end date is at the end of a working period.
     *
     * @param startDate start date
     * @param duration duration
     * @param returnNextWorkStart if set to true will return start of next working period
     * @return end date
     */
    fun getDate(startDate: Date, duration: Duration, returnNextWorkStart: Boolean): Date {
        var startDate = startDate
        var returnNextWorkStart = returnNextWorkStart
        val properties = parentFile!!.projectProperties
        // Note: Using a double allows us to handle date values that are accurate up to seconds.
        //       However, it also means we need to truncate the value to 2 decimals to make the
        //       comparisons work as sometimes the double ends up with some extra e.g. .0000000000003
        //       that wreak havoc on the comparisons.
        var remainingMinutes = NumberHelper.round(duration.convertUnits(TimeUnit.MINUTES, properties).getDuration(), 2.0)

        //
        // Can we skip come computation by working forward from the
        // last call to this method?
        //
        val getDateLastStartDate = m_getDateLastStartDate
        val getDateLastRemainingMinutes = m_getDateLastRemainingMinutes

        m_getDateLastStartDate = startDate
        m_getDateLastRemainingMinutes = remainingMinutes

        if (m_getDateLastResult != null && DateHelper.compare(startDate, getDateLastStartDate) == 0 && remainingMinutes >= getDateLastRemainingMinutes) {
            startDate = m_getDateLastResult
            remainingMinutes = remainingMinutes - getDateLastRemainingMinutes
        }

        val cal = Calendar.getInstance()
        cal.setTime(startDate)
        val endCal = Calendar.getInstance()

        while (remainingMinutes > 0) {
            //
            // Get the current date and time and determine how many
            // working hours remain
            //
            val currentDate = cal.getTime()
            endCal.setTime(currentDate)
            endCal.add(Calendar.DAY_OF_YEAR, 1)
            val currentDateEnd = DateHelper.getDayStartDate(endCal.getTime())
            val currentDateWorkingMinutes = getWork(currentDate, currentDateEnd, TimeUnit.MINUTES).getDuration()

            //
            // We have more than enough hours left
            //
            if (remainingMinutes > currentDateWorkingMinutes) {
                //
                // Deduct this day's hours from our total
                //
                remainingMinutes = NumberHelper.round(remainingMinutes - currentDateWorkingMinutes, 2.0)

                //
                // Move the calendar forward to the next working day
                //
                var day: Day
                var nonWorkingDayCount = 0
                do {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    day = Day.getInstance(cal.get(Calendar.DAY_OF_WEEK))
                    ++nonWorkingDayCount
                    if (nonWorkingDayCount > MAX_NONWORKING_DAYS) {
                        cal.setTime(startDate)
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        remainingMinutes = 0.0
                        break
                    }
                } while (!isWorkingDate(cal.getTime(), day))

                //
                // Retrieve the start time for this day
                //
                val startTime = getStartTime(cal.getTime())
                DateHelper.setTime(cal, startTime)
            } else {
                //
                // We have less hours to allocate than there are working hours
                // in this day. We need to calculate the time of day at which
                // our work ends.
                //
                val ranges = getRanges(cal.getTime(), cal, null)

                //
                // Now we have the range of working hours for this day,
                // step through it to work out the end point
                //
                var endTime: Date? = null
                val currentDateStartTime = DateHelper.getCanonicalTime(currentDate)
                var firstRange = true
                for (range in ranges!!) {
                    //
                    // Skip this range if its end is before our start time
                    //
                    val rangeStart = range.getStart()
                    val rangeEnd = range.getEnd()

                    if (rangeStart == null || rangeEnd == null) {
                        continue
                    }

                    var canonicalRangeEnd = DateHelper.getCanonicalTime(rangeEnd)
                    var canonicalRangeStart = DateHelper.getCanonicalTime(rangeStart)

                    val rangeStartDay = DateHelper.getDayStartDate(rangeStart)
                    val rangeEndDay = DateHelper.getDayStartDate(rangeEnd)

                    if (rangeStartDay!!.getTime() !== rangeEndDay!!.getTime()) {
                        canonicalRangeEnd = DateHelper.addDays(canonicalRangeEnd, 1)
                    }

                    if (firstRange && canonicalRangeEnd!!.getTime() < currentDateStartTime!!.getTime()) {
                        continue
                    }

                    //
                    // Move the start of the range if our current start is
                    // past the range start
                    //
                    if (firstRange && canonicalRangeStart!!.getTime() < currentDateStartTime!!.getTime()) {
                        canonicalRangeStart = currentDateStartTime
                    }
                    firstRange = false

                    var rangeMinutes: Double

                    rangeMinutes = canonicalRangeEnd!!.getTime() - canonicalRangeStart!!.getTime()
                    rangeMinutes /= (1000 * 60).toDouble()

                    if (remainingMinutes > rangeMinutes) {
                        remainingMinutes = NumberHelper.round(remainingMinutes - rangeMinutes, 2.0)
                    } else {
                        if (Duration.durationValueEquals(remainingMinutes, rangeMinutes)) {
                            endTime = canonicalRangeEnd
                            if (rangeStartDay!!.getTime() !== rangeEndDay!!.getTime()) {
                                // The range ends the next day, so let's adjust our date accordingly.
                                cal.add(Calendar.DAY_OF_YEAR, 1)
                            }
                        } else {
                            endTime = Date((canonicalRangeStart.getTime() + remainingMinutes * (60 * 1000)) as Long)
                            returnNextWorkStart = false
                        }
                        remainingMinutes = 0.0
                        break
                    }
                }

                DateHelper.setTime(cal, endTime)
            }
        }

        m_getDateLastResult = cal.getTime()
        if (returnNextWorkStart) {
            updateToNextWorkStart(cal)
        }

        return cal.getTime()
    }

    /**
     * Given a finish date and a duration, this method calculates backwards to the
     * start date. It takes account of working hours in each day, non working
     * days and calendar exceptions.
     *
     * @param finishDate finish date
     * @param duration duration
     * @return start date
     */
    fun getStartDate(finishDate: Date, duration: Duration): Date? {
        val properties = parentFile!!.projectProperties
        // Note: Using a double allows us to handle date values that are accurate up to seconds.
        //       However, it also means we need to truncate the value to 2 decimals to make the
        //       comparisons work as sometimes the double ends up with some extra e.g. .0000000000003
        //       that wreak havoc on the comparisons.
        var remainingMinutes = NumberHelper.round(duration.convertUnits(TimeUnit.MINUTES, properties).getDuration(), 2.0)
        val cal = Calendar.getInstance()
        cal.setTime(finishDate)
        val startCal = Calendar.getInstance()

        while (remainingMinutes > 0) {
            //
            // Get the current date and time and determine how many
            // working hours remain
            //
            val currentDate = cal.getTime()
            startCal.setTime(currentDate)
            startCal.add(Calendar.DAY_OF_YEAR, -1)
            val currentDateEnd = DateHelper.getDayEndDate(startCal.getTime())
            val currentDateWorkingMinutes = getWork(currentDateEnd, currentDate, TimeUnit.MINUTES).getDuration()

            //
            // We have more than enough hours left
            //
            if (remainingMinutes > currentDateWorkingMinutes) {
                //
                // Deduct this day's hours from our total
                //
                remainingMinutes = NumberHelper.round(remainingMinutes - currentDateWorkingMinutes, 2.0)

                //
                // Move the calendar backward to the previous working day
                //
                var count = 0
                var day: Day
                do {
                    if (count > 7) {
                        break // Protect against a calendar with all days non-working
                    }
                    count++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    day = Day.getInstance(cal.get(Calendar.DAY_OF_WEEK))
                } while (!isWorkingDate(cal.getTime(), day))

                if (count > 7) {
                    // We have a calendar with no working days.
                    return null
                }

                //
                // Retrieve the finish time for this day
                //
                val finishTime = getFinishTime(cal.getTime())
                DateHelper.setTime(cal, finishTime)
            } else {
                //
                // We have less hours to allocate than there are working hours
                // in this day. We need to calculate the time of day at which
                // our work starts.
                //
                val ranges = getRanges(cal.getTime(), cal, null)

                //
                // Now we have the range of working hours for this day,
                // step through it to work out the start point
                //
                var startTime: Date? = null
                val currentDateFinishTime = DateHelper.getCanonicalTime(currentDate)
                var firstRange = true
                // Traverse from end to start
                for (i in ranges!!.rangeCount - 1 downTo 0) {
                    val range = ranges.getRange(i)
                    //
                    // Skip this range if its start is after our end time
                    //
                    val rangeStart = range.getStart()
                    val rangeEnd = range.getEnd()

                    if (rangeStart == null || rangeEnd == null) {
                        continue
                    }

                    var canonicalRangeEnd = DateHelper.getCanonicalTime(rangeEnd)
                    val canonicalRangeStart = DateHelper.getCanonicalTime(rangeStart)

                    val rangeStartDay = DateHelper.getDayStartDate(rangeStart)
                    val rangeEndDay = DateHelper.getDayStartDate(rangeEnd)

                    if (rangeStartDay!!.getTime() !== rangeEndDay!!.getTime()) {
                        canonicalRangeEnd = DateHelper.addDays(canonicalRangeEnd, 1)
                    }

                    if (firstRange && canonicalRangeStart!!.getTime() > currentDateFinishTime!!.getTime()) {
                        continue
                    }

                    //
                    // Move the end of the range if our current end is
                    // before the range end
                    //
                    if (firstRange && canonicalRangeEnd!!.getTime() > currentDateFinishTime!!.getTime()) {
                        canonicalRangeEnd = currentDateFinishTime
                    }
                    firstRange = false

                    var rangeMinutes: Double

                    rangeMinutes = canonicalRangeEnd!!.getTime() - canonicalRangeStart!!.getTime()
                    rangeMinutes /= (1000 * 60).toDouble()

                    if (remainingMinutes > rangeMinutes) {
                        remainingMinutes = NumberHelper.round(remainingMinutes - rangeMinutes, 2.0)
                    } else {
                        if (Duration.durationValueEquals(remainingMinutes, rangeMinutes)) {
                            startTime = canonicalRangeStart
                        } else {
                            startTime = Date((canonicalRangeEnd.getTime() - remainingMinutes * (60 * 1000)) as Long)
                        }
                        remainingMinutes = 0.0
                        break
                    }
                }

                DateHelper.setTime(cal, startTime)
            }
        }

        return cal.getTime()
    }

    /**
     * This method finds the start of the next working period.
     *
     * @param cal current Calendar instance
     */
    private fun updateToNextWorkStart(cal: Calendar) {
        val originalDate = cal.getTime()

        //
        // Find the date ranges for the current day
        //
        val ranges = getRanges(originalDate, cal, null)

        if (ranges != null) {
            //
            // Do we have a start time today?
            //
            val calTime = DateHelper.getCanonicalTime(cal.getTime())
            var startTime: Date? = null
            for (range in ranges) {
                val rangeStart = DateHelper.getCanonicalTime(range.getStart())
                var rangeEnd = DateHelper.getCanonicalTime(range.getEnd())
                val rangeStartDay = DateHelper.getDayStartDate(range.getStart())
                val rangeEndDay = DateHelper.getDayStartDate(range.getEnd())

                if (rangeStartDay!!.getTime() !== rangeEndDay!!.getTime()) {
                    rangeEnd = DateHelper.addDays(rangeEnd, 1)
                }

                if (calTime!!.getTime() < rangeEnd!!.getTime()) {
                    if (calTime.getTime() > rangeStart!!.getTime()) {
                        startTime = calTime
                    } else {
                        startTime = rangeStart
                    }
                    break
                }
            }

            //
            // If we don't have a start time today - find the next working day
            // then retrieve the start time.
            //
            if (startTime == null) {
                var day: Day
                var nonWorkingDayCount = 0
                do {
                    cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1)
                    day = Day.getInstance(cal.get(Calendar.DAY_OF_WEEK))
                    ++nonWorkingDayCount
                    if (nonWorkingDayCount > MAX_NONWORKING_DAYS) {
                        cal.setTime(originalDate)
                        break
                    }
                } while (!isWorkingDate(cal.getTime(), day))

                startTime = getStartTime(cal.getTime())
            }

            DateHelper.setTime(cal, startTime)
        }
    }

    /**
     * This method finds the finish of the previous working period.
     *
     * @param cal current Calendar instance
     */
    private fun updateToPreviousWorkFinish(cal: Calendar) {
        val originalDate = cal.getTime()

        //
        // Find the date ranges for the current day
        //
        val ranges = getRanges(originalDate, cal, null)
        if (ranges != null) {
            //
            // Do we have a start time today?
            //
            val calTime = DateHelper.getCanonicalTime(cal.getTime())
            var finishTime: Date? = null
            for (range in ranges) {
                var rangeEnd = DateHelper.getCanonicalTime(range.getEnd())
                val rangeStartDay = DateHelper.getDayStartDate(range.getStart())
                val rangeEndDay = DateHelper.getDayStartDate(range.getEnd())

                if (rangeStartDay!!.getTime() !== rangeEndDay!!.getTime()) {
                    rangeEnd = DateHelper.addDays(rangeEnd, 1)
                }

                if (calTime!!.getTime() >= rangeEnd!!.getTime()) {
                    finishTime = rangeEnd
                    break
                }
            }

            //
            // If we don't have a finish time today - find the previous working day
            // then retrieve the finish time.
            //
            if (finishTime == null) {
                var day: Day
                var nonWorkingDayCount = 0
                do {
                    cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) - 1)
                    day = Day.getInstance(cal.get(Calendar.DAY_OF_WEEK))
                    ++nonWorkingDayCount
                    if (nonWorkingDayCount > MAX_NONWORKING_DAYS) {
                        cal.setTime(originalDate)
                        break
                    }
                } while (!isWorkingDate(cal.getTime(), day))

                finishTime = getFinishTime(cal.getTime())
            }

            DateHelper.setTime(cal, finishTime)
        }
    }

    /**
     * Utility method to retrieve the next working date start time, given
     * a date and time as a starting point.
     *
     * @param date date and time start point
     * @return date and time of next work start
     */
    fun getNextWorkStart(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.setTime(date)
        updateToNextWorkStart(cal)
        return cal.getTime()
    }

    /**
     * Utility method to retrieve the previous working date finish time, given
     * a date and time as a starting point.
     *
     * @param date date and time start point
     * @return date and time of previous work finish
     */
    fun getPreviousWorkFinish(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.setTime(date)
        updateToPreviousWorkFinish(cal)
        return cal.getTime()
    }

    /**
     * This method allows the caller to determine if a given date is a
     * working day. This method takes account of calendar exceptions.
     *
     * @param date Date to be tested
     * @return boolean value
     */
    fun isWorkingDate(date: Date): Boolean {
        val cal = DateHelper.popCalendar(date)
        val day = Day.getInstance(cal.get(Calendar.DAY_OF_WEEK))
        DateHelper.pushCalendar(cal)
        return isWorkingDate(date, day)
    }

    /**
     * This private method allows the caller to determine if a given date is a
     * working day. This method takes account of calendar exceptions. It assumes
     * that the caller has already calculated the day of the week on which
     * the given day falls.
     *
     * @param date Date to be tested
     * @param day Day of the week for the date under test
     * @return boolean flag
     */
    private fun isWorkingDate(date: Date, day: Day): Boolean {
        val ranges = getRanges(date, null, day)
        return ranges!!.rangeCount != 0
    }

    /**
     * This method calculates the absolute number of days between two dates.
     * Note that where two date objects are provided that fall on the same
     * day, this method will return one not zero. Note also that this method
     * assumes that the dates are passed in the correct order, i.e.
     * startDate < endDate.
     *
     * @param startDate Start date
     * @param endDate End date
     * @return number of days in the date range
     */
    private fun getDaysInRange(startDate: Date, endDate: Date): Int {
        val result: Int
        val cal = DateHelper.popCalendar(endDate)
        val endDateYear = cal.get(Calendar.YEAR)
        val endDateDayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        cal.setTime(startDate)

        if (endDateYear == cal.get(Calendar.YEAR)) {
            result = endDateDayOfYear - cal.get(Calendar.DAY_OF_YEAR) + 1
        } else {
            result = 0
            do {
                result += cal.getActualMaximum(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR) + 1
                cal.roll(Calendar.YEAR, 1)
                cal.set(Calendar.DAY_OF_YEAR, 1)
            } while (cal.get(Calendar.YEAR) < endDateYear)
            result += endDateDayOfYear
        }
        DateHelper.pushCalendar(cal)

        return result
    }

    /**
     * Removes this calendar from the project.
     */
    fun remove() {
        parentFile!!.removeCalendar(this)
    }

    /**
     * Retrieve a calendar exception which applies to this date.
     *
     * @param date target date
     * @return calendar exception, or null if none match this date
     */
    fun getException(date: Date): ProjectCalendarException? {
        var exception: ProjectCalendarException? = null

        // We're working with expanded exceptions, which includes any recurring exceptions
        // expanded into individual entries.
        populateExpandedExceptions()
        if (!m_expandedExceptions.isEmpty()) {
            sortExceptions()

            var low = 0
            var high = m_expandedExceptions.size() - 1
            val targetDate = date.getTime()

            while (low <= high) {
                val mid = (low + high).ushr(1)
                val midVal = m_expandedExceptions.get(mid)
                val cmp = 0 - DateHelper.compare(midVal.fromDate!!, midVal.toDate, targetDate)

                if (cmp < 0) {
                    low = mid + 1
                } else {
                    if (cmp > 0) {
                        high = mid - 1
                    } else {
                        exception = midVal
                        break
                    }
                }
            }
        }

        if (exception == null && parent != null) {
            // Check base calendar as well for an exception.
            exception = parent!!.getException(date)
        }
        return exception
    }

    /**
     * Retrieve a work week which applies to this date.
     *
     * @param date target date
     * @return work week, or null if none match this date
     */
    fun getWorkWeek(date: Date): ProjectCalendarWeek? {
        var week: ProjectCalendarWeek? = null
        if (!m_workWeeks.isEmpty()) {
            sortWorkWeeks()

            var low = 0
            var high = m_workWeeks.size() - 1
            val targetDate = date.getTime()

            while (low <= high) {
                val mid = (low + high).ushr(1)
                val midVal = m_workWeeks.get(mid)
                val cmp = 0 - DateHelper.compare(midVal.dateRange!!.getStart(), midVal.dateRange!!.getEnd(), targetDate)

                if (cmp < 0) {
                    low = mid + 1
                } else {
                    if (cmp > 0) {
                        high = mid - 1
                    } else {
                        week = midVal
                        break
                    }
                }
            }
        }

        if (week == null && parent != null) {
            // Check base calendar as well for a work week.
            week = parent!!.getWorkWeek(date)
        }
        return week
    }

    /**
     * Retrieves the amount of work on a given day, and
     * returns it in the specified format.
     *
     * @param date target date
     * @param format required format
     * @return work duration
     */
    fun getWork(date: Date, format: TimeUnit): Duration {
        val ranges = getRanges(date, null, null)
        val time = getTotalTime(ranges!!)
        return convertFormat(time, format)
    }

    /**
     * This method retrieves a Duration instance representing the amount of
     * work between two dates based on this calendar.
     *
     * @param startDate start date
     * @param endDate end date
     * @param format required duration format
     * @return amount of work
     */
    fun getWork(startDate: Date?, endDate: Date?, format: TimeUnit): Duration {
        var startDate = startDate
        var endDate = endDate
        val range = DateRange(startDate, endDate)
        val cachedResult = m_workingDateCache.get(range)
        var totalTime: Long = 0

        if (cachedResult == null) {
            //
            // We want the start date to be the earliest date, and the end date
            // to be the latest date. Set a flag here to indicate if we have swapped
            // the order of the supplied date.
            //
            var invert = false
            if (startDate!!.getTime() > endDate!!.getTime()) {
                invert = true
                val temp = startDate
                startDate = endDate
                endDate = temp
            }

            val canonicalStartDate = DateHelper.getDayStartDate(startDate)
            val canonicalEndDate = DateHelper.getDayStartDate(endDate)

            if (canonicalStartDate!!.getTime() === canonicalEndDate!!.getTime()) {
                val ranges = getRanges(startDate, null, null)
                if (ranges!!.rangeCount != 0) {
                    totalTime = getTotalTime(ranges, startDate!!, endDate!!)
                }
            } else {
                //
                // Find the first working day in the range
                //
                var currentDate: Date = startDate
                val cal = Calendar.getInstance()
                cal.setTime(startDate)
                var day = Day.getInstance(cal.get(Calendar.DAY_OF_WEEK))
                while (isWorkingDate(currentDate, day) == false && currentDate.getTime() < canonicalEndDate!!.getTime()) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    currentDate = cal.getTime()
                    day = day.getNextDay()
                }

                if (currentDate.getTime() < canonicalEndDate!!.getTime()) {
                    //
                    // Calculate the amount of working time for this day
                    //
                    totalTime += getTotalTime(getRanges(currentDate, null, day)!!, currentDate, true)

                    //
                    // Process each working day until we reach the last day
                    //
                    while (true) {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        currentDate = cal.getTime()
                        day = day.getNextDay()

                        //
                        // We have reached the last day
                        //
                        if (currentDate.getTime() >= canonicalEndDate.getTime()) {
                            break
                        }

                        //
                        // Skip this day if it has no working time
                        //
                        val ranges = getRanges(currentDate, null, day)
                        if (ranges!!.rangeCount == 0) {
                            continue
                        }

                        //
                        // Add the working time for the whole day
                        //
                        totalTime += getTotalTime(ranges)
                    }
                }

                //
                // We are now at the last day
                //
                val ranges = getRanges(endDate, null, day)
                if (ranges!!.rangeCount != 0) {
                    totalTime += getTotalTime(ranges, DateHelper.getDayStartDate(endDate)!!, endDate!!)
                }
            }

            if (invert) {
                totalTime = -totalTime
            }

            m_workingDateCache.put(range, Long.valueOf(totalTime))
        } else {
            totalTime = cachedResult!!.longValue()
        }

        return convertFormat(totalTime, format)
    }

    /**
     * Utility method used to convert an integer time representation into a
     * Duration instance.
     *
     * @param totalTime integer time representation
     * @param format required time format
     * @return new Duration instance
     */
    private fun convertFormat(totalTime: Long, format: TimeUnit): Duration {
        var duration = totalTime.toDouble()

        when (format) {
            MINUTES, ELAPSED_MINUTES -> {
                duration /= (60 * 1000).toDouble()
            }

            HOURS, ELAPSED_HOURS -> {
                duration /= (60 * 60 * 1000).toDouble()
            }

            DAYS -> {
                val minutesPerDay = minutesPerDay.toDouble()
                if (minutesPerDay != 0.0) {
                    duration /= minutesPerDay * 60.0 * 1000.0
                } else {
                    duration = 0.0
                }
            }

            WEEKS -> {
                val minutesPerWeek = minutesPerWeek.toDouble()
                if (minutesPerWeek != 0.0) {
                    duration /= minutesPerWeek * 60.0 * 1000.0
                } else {
                    duration = 0.0
                }
            }

            MONTHS -> {
                val daysPerMonth = parentFile!!.projectProperties.daysPerMonth.doubleValue()
                val minutesPerDay = minutesPerDay.toDouble()
                if (daysPerMonth != 0.0 && minutesPerDay != 0.0) {
                    duration /= daysPerMonth * minutesPerDay * 60.0 * 1000.0
                } else {
                    duration = 0.0
                }
            }

            ELAPSED_DAYS -> {
                duration /= (24 * 60 * 60 * 1000).toDouble()
            }

            ELAPSED_WEEKS -> {
                duration /= (7 * 24 * 60 * 60 * 1000).toDouble()
            }

            ELAPSED_MONTHS -> {
                duration /= (30 * 24 * 60 * 60 * 1000).toDouble()
            }

            else -> {
                throw IllegalArgumentException("TimeUnit $format not supported")
            }
        }

        return Duration.getInstance(duration, format)
    }

    /**
     * Retrieves the amount of time represented by a calendar exception
     * before or after an intersection point.
     *
     * @param exception calendar exception
     * @param date intersection time
     * @param after true to report time after intersection, false to report time before
     * @return length of time in milliseconds
     */
    private fun getTotalTime(exception: ProjectCalendarDateRanges, date: Date, after: Boolean): Long {
        val currentTime = DateHelper.getCanonicalTime(date)!!.getTime()
        var total: Long = 0
        for (range in exception) {
            total += getTime(range.getStart(), range.getEnd(), currentTime, after)
        }
        return total
    }

    /**
     * Retrieves the amount of working time represented by
     * a calendar exception.
     *
     * @param exception calendar exception
     * @return length of time in milliseconds
     */
    private fun getTotalTime(exception: ProjectCalendarDateRanges): Long {
        var total: Long = 0
        for (range in exception) {
            total += getTime(range.getStart(), range.getEnd())
        }
        return total
    }

    /**
     * This method calculates the total amount of working time in a single
     * day, which intersects with the supplied time range.
     *
     * @param hours collection of working hours in a day
     * @param startDate time range start
     * @param endDate time range end
     * @return length of time in milliseconds
     */
    private fun getTotalTime(hours: ProjectCalendarDateRanges, startDate: Date, endDate: Date): Long {
        var total: Long = 0
        if (startDate.getTime() !== endDate.getTime()) {
            val start = DateHelper.getCanonicalTime(startDate)
            val end = DateHelper.getCanonicalTime(endDate)

            for (range in hours) {
                val rangeStart = range.getStart()
                val rangeEnd = range.getEnd()
                if (rangeStart != null && rangeEnd != null) {
                    val canoncialRangeStart = DateHelper.getCanonicalTime(rangeStart)
                    var canonicalRangeEnd = DateHelper.getCanonicalTime(rangeEnd)

                    val startDay = DateHelper.getDayStartDate(rangeStart)
                    val finishDay = DateHelper.getDayStartDate(rangeEnd)

                    //
                    // Handle the case where the end of the range is at midnight -
                    // this will show up as the start and end days not matching
                    //
                    if (startDay!!.getTime() !== finishDay!!.getTime()) {
                        canonicalRangeEnd = DateHelper.addDays(canonicalRangeEnd, 1)
                    }

                    if (canoncialRangeStart!!.getTime() === canonicalRangeEnd!!.getTime() && rangeEnd!!.getTime() > rangeStart!!.getTime()) {
                        total += (24 * 60 * 60 * 1000).toLong()
                    } else {
                        total += getTime(start, end, canoncialRangeStart, canonicalRangeEnd)
                    }
                }
            }
        }

        return total
    }

    /**
     * Calculates how much of a time range is before or after a
     * target intersection point.
     *
     * @param start time range start
     * @param end time range end
     * @param target target intersection point
     * @param after true if time after target required, false for time before
     * @return length of time in milliseconds
     */
    private fun getTime(start: Date?, end: Date?, target: Long, after: Boolean): Long {
        var total: Long = 0
        if (start != null && end != null) {
            val startTime = DateHelper.getCanonicalTime(start)
            var endTime = DateHelper.getCanonicalTime(end)

            val startDay = DateHelper.getDayStartDate(start)
            val finishDay = DateHelper.getDayStartDate(end)

            //
            // Handle the case where the end of the range is at midnight -
            // this will show up as the start and end days not matching
            //
            if (startDay!!.getTime() !== finishDay!!.getTime()) {
                endTime = DateHelper.addDays(endTime, 1)
            }

            val diff = DateHelper.compare(startTime!!, endTime, target)
            if (diff == 0) {
                if (after == true) {
                    total = endTime!!.getTime() - target
                } else {
                    total = target - startTime.getTime()
                }
            } else {
                if (after == true && diff < 0 || after == false && diff > 0) {
                    total = endTime!!.getTime() - startTime.getTime()
                }
            }
        }
        return total
    }

    /**
     * Retrieves the amount of time between two date time values. Note that
     * these values are converted into canonical values to remove the
     * date component.
     *
     * @param start start time
     * @param end end time
     * @return length of time
     */
    private fun getTime(start: Date?, end: Date?): Long {
        var total: Long = 0
        if (start != null && end != null) {
            val startTime = DateHelper.getCanonicalTime(start)
            var endTime = DateHelper.getCanonicalTime(end)

            val startDay = DateHelper.getDayStartDate(start)
            val finishDay = DateHelper.getDayStartDate(end)

            //
            // Handle the case where the end of the range is at midnight -
            // this will show up as the start and end days not matching
            //
            if (startDay!!.getTime() !== finishDay!!.getTime()) {
                endTime = DateHelper.addDays(endTime, 1)
            }

            total = endTime!!.getTime() - startTime!!.getTime()
        }
        return total
    }

    /**
     * This method returns the length of overlapping time between two time
     * ranges.
     *
     * @param start1 start of first range
     * @param end1 end of first range
     * @param start2 start start of second range
     * @param end2 end of second range
     * @return overlapping time in milliseconds
     */
    private fun getTime(start1: Date?, end1: Date?, start2: Date?, end2: Date?): Long {
        var total: Long = 0

        if (start1 != null && end1 != null && start2 != null && end2 != null) {
            val start: Long
            val end: Long

            if (start1!!.getTime() < start2!!.getTime()) {
                start = start2!!.getTime()
            } else {
                start = start1!!.getTime()
            }

            if (end1!!.getTime() < end2!!.getTime()) {
                end = end1!!.getTime()
            } else {
                end = end2!!.getTime()
            }

            if (start < end) {
                total = end - start
            }
        }

        return total
    }

    /**
     * Add a reference to a calendar derived from this one.
     *
     * @param calendar derived calendar instance
     */
    protected fun addDerivedCalendar(calendar: ProjectCalendar) {
        m_derivedCalendars.add(calendar)
    }

    /**
     * Remove a reference to a derived calendar.
     *
     * @param calendar derived calendar instance
     */
    protected fun removeDerivedCalendar(calendar: ProjectCalendar) {
        m_derivedCalendars.remove(calendar)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun toString(): String {
        val os = ByteArrayOutputStream()
        val pw = PrintWriter(os)
        pw.println("[ProjectCalendar")
        pw.println("   ID=" + m_uniqueID!!)
        pw.println("   name=" + name!!)
        pw.println("   baseCalendarName=" + if (parent == null) "" else parent!!.name)
        pw.println("   resource=" + if (m_resource == null) "" else m_resource!!.name)

        val dayName = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

        for (loop in 0..6) {
            pw.println("   [Day " + dayName[loop])
            pw.println("      type=" + days[loop])
            pw.println("      hours=" + hours[loop])
            pw.println("   ]")
        }

        if (!m_exceptions.isEmpty()) {
            pw.println("   [Exceptions=")
            for (ex in m_exceptions) {
                pw.println("      " + ex.toString())
            }
            pw.println("   ]")
        }

        if (!m_workWeeks.isEmpty()) {
            pw.println("   [WorkWeeks=")
            for (week in m_workWeeks) {
                pw.println("      " + week.toString())
            }
            pw.println("   ]")
        }

        pw.println("]")
        pw.flush()
        return os.toString()
    }

    /**
     * Create a calendar based on the intersection of a task calendar and a resource calendar.
     *
     * @param file the parent file to which this record belongs.
     * @param taskCalendar task calendar to merge
     * @param resourceCalendar resource calendar to merge
     */
    constructor(file: ProjectFile, taskCalendar: ProjectCalendar, resourceCalendar: ProjectCalendar) {
        m_projectFile = file

        // Set the resource
        resource = resourceCalendar.resource

        // Merge the exceptions

        // Merge the hours
        for (i in 1..7) {
            val day = Day.getInstance(i)

            // Set working/non-working days
            setWorkingDay(day, taskCalendar.isWorkingDay(day) && resourceCalendar.isWorkingDay(day))

            val hours = addCalendarHours(day)

            var taskIndex = 0
            var resourceIndex = 0

            val taskHours = taskCalendar.getHours(day)
            val resourceHours = resourceCalendar.getHours(day)

            var range1: DateRange? = null
            var range2: DateRange? = null

            var start: Date? = null
            var end: Date? = null

            var start1: Date? = null
            var start2: Date? = null
            var end1: Date? = null
            var end2: Date? = null
            while (true) {
                // Find next range start
                if (taskHours!!.rangeCount > taskIndex) {
                    range1 = taskHours.getRange(taskIndex)
                } else {
                    break
                }

                if (resourceHours!!.rangeCount > resourceIndex) {
                    range2 = resourceHours.getRange(resourceIndex)
                } else {
                    break
                }

                start1 = range1!!.getStart()
                start2 = range2!!.getStart()
                end1 = range1!!.getEnd()
                end2 = range2!!.getEnd()

                // Get the later start
                if (start1!!.compareTo(start2) > 0) {
                    start = start1
                } else {
                    start = start2
                }

                // Get the earlier end
                if (end1!!.compareTo(end2) < 0) {
                    end = end1
                    taskIndex++
                } else {
                    end = end2
                    resourceIndex++
                }

                if (end != null && end!!.compareTo(start) > 0) {
                    // Found a block
                    hours.addRange(DateRange(start, end))
                }
            }
        }
        // For now just combine the exceptions. Probably overkill (although would be more accurate) to also merge the exceptions.
        m_exceptions.addAll(taskCalendar.calendarExceptions)
        m_exceptions.addAll(resourceCalendar.calendarExceptions)
        m_expandedExceptions.clear()
        m_exceptionsSorted = false

        m_workWeeks.addAll(taskCalendar.workWeeks)
        m_workWeeks.addAll(resourceCalendar.workWeeks)
        m_weeksSorted = false
    }

    /**
     * Copy the settings from another calendar to this calendar.
     *
     * @param cal calendar data source
     */
    fun copy(cal: ProjectCalendar) {
        name = cal.name
        parent = cal.parent
        System.arraycopy(cal.days, 0, days, 0, days.size)
        for (ex in cal.m_exceptions) {
            addCalendarException(ex.fromDate, ex.toDate)
            for (range in ex) {
                ex.addRange(DateRange(range.getStart(), range.getEnd()))
            }
        }

        for (hours in hours) {
            if (hours != null) {
                val copyHours = cal.addCalendarHours(hours.day)
                for (range in hours) {
                    copyHours.addRange(DateRange(range.getStart(), range.getEnd()))
                }
            }
        }
    }

    /**
     * Utility method to clear cached calendar data.
     */
    private fun clearWorkingDateCache() {
        m_workingDateCache.clear()
        m_startTimeCache.clear()
        m_getDateLastResult = null
        for (calendar in m_derivedCalendars) {
            calendar.clearWorkingDateCache()
        }
    }

    /**
     * Retrieves the working hours on the given date.
     *
     * @param date required date
     * @param cal optional calendar instance
     * @param day optional day instance
     * @return working hours
     */
    private fun getRanges(date: Date, cal: Calendar?, day: Day?): ProjectCalendarDateRanges? {
        var cal = cal
        var day = day
        var ranges: ProjectCalendarDateRanges? = getException(date)
        if (ranges == null) {
            var week = getWorkWeek(date)
            if (week == null) {
                week = this
            }

            if (day == null) {
                if (cal == null) {
                    cal = Calendar.getInstance()
                    cal!!.setTime(date)
                }
                day = Day.getInstance(cal!!.get(Calendar.DAY_OF_WEEK))
            }

            ranges = week.getHours(day)
        }
        return ranges
    }

    /**
     * Ensure exceptions are sorted.
     */
    private fun sortExceptions() {
        if (!m_exceptionsSorted) {
            Collections.sort(m_exceptions)
            m_exceptionsSorted = true
        }
    }

    /**
     * Populate the expanded exceptions list based on the main exceptions list.
     * Where we find recurring exception definitions, we generate individual
     * exceptions for each recurrence to ensure that we account for them correctly.
     */
    private fun populateExpandedExceptions() {
        if (!m_exceptions.isEmpty() && m_expandedExceptions.isEmpty()) {
            for (exception in m_exceptions) {
                val recurring = exception.recurring
                if (recurring == null) {
                    m_expandedExceptions.add(exception)
                } else {
                    for (date in recurring!!.dates) {
                        val startDate = DateHelper.getDayStartDate(date)
                        val endDate = DateHelper.getDayEndDate(date)
                        val newException = ProjectCalendarException(startDate, endDate)
                        val rangeCount = exception.rangeCount
                        for (rangeIndex in 0 until rangeCount) {
                            newException.addRange(exception.getRange(rangeIndex))
                        }
                        m_expandedExceptions.add(newException)
                    }
                }
            }
            Collections.sort(m_expandedExceptions)
        }
    }

    /**
     * Ensure work weeks are sorted.
     */
    private fun sortWorkWeeks() {
        if (!m_weeksSorted) {
            Collections.sort(m_workWeeks)
            m_weeksSorted = true
        }
    }

    companion object {

        /**
         * Default base calendar name to use when none is supplied.
         */
        val DEFAULT_BASE_CALENDAR_NAME = "Standard"

        /**
         * It is possible for a project calendar to be configured with no working
         * days. This will result in an infinite loop when looking for the next
         * working day from a date, so we use this constant to set a limit on the
         * maximum number of non-working days we'll skip before we bail out
         * and take an alternative approach.
         */
        private val MAX_NONWORKING_DAYS = 1000
    }
}
