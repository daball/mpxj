/*
 * file:      Resource.java
 * author:    Jon Iles
 *            Scott Melville
 * copyright: (c) Packwood Software 2002-2003
 * date:      15/08/2002
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
import java.util.UUID

import net.sf.mpxj.common.BooleanHelper
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.ResourceFieldLists
import net.sf.mpxj.listener.FieldListener

/**
 * This class represents a resource used in a project.
 */
class Resource
/**
 * Default constructor.
 *
 * @param file the parent file to which this record belongs.
 */
internal constructor(file: ProjectFile) : ProjectEntity(file), Comparable<Resource>, ProjectEntityWithID, FieldContainer {

    /**
     * Gets Resource Name field value.
     *
     * @return value
     */
    /**
     * Sets Name field value.
     *
     * @param val value
     */
    var name: String
        get() = getCachedValue(ResourceField.NAME)
        set(`val`) = set(ResourceField.NAME, `val`)

    /**
     * Retrieves the resource type. Can return TYPE_MATERIAL, or TYPE_WORK.
     *
     * @return resource type
     */
    /**
     * Set the resource type. Can be TYPE_MATERIAL, or TYPE_WORK.
     *
     * @param type resource type
     */
    var type: ResourceType
        get() = getCachedValue(ResourceField.TYPE)
        set(type) = set(ResourceField.TYPE, type)

    /**
     * Retrieve a flag indicating if this is a null resource.
     *
     * @return boolean flag
     */
    val `null`: Boolean
        get() = m_null

    /**
     * Gets Initials of name field value.
     *
     * @return value
     */
    /**
     * Sets Initials field value.
     *
     * @param val value
     */
    var initials: String
        get() = getCachedValue(ResourceField.INITIALS)
        set(`val`) = set(ResourceField.INITIALS, `val`)

    /**
     * Retrieves phonetic information for the Japanese version of MS Project.
     *
     * @return Japanese phonetic information
     */
    /**
     * Sets phonetic information for the Japanese version of MS Project.
     *
     * @param phonetics Japanese phonetic information
     */
    var phonetics: String
        get() = getCachedValue(ResourceField.PHONETICS)
        set(phonetics) = set(ResourceField.PHONETICS, phonetics)

    /**
     * Retrieves the Windows account name for a resource.
     *
     * @return windows account name
     */
    /**
     * Sets the Windows account name for a resource.
     *
     * @param ntAccount windows account name
     */
    var ntAccount: String
        get() = getCachedValue(ResourceField.WINDOWS_USER_ACCOUNT)
        set(ntAccount) = set(ResourceField.WINDOWS_USER_ACCOUNT, ntAccount)

    /**
     * Retrieves the units label for a material resource.
     *
     * @return material resource units label
     */
    /**
     * Set the units label for a material resource.
     *
     * @param materialLabel material resource units label
     */
    var materialLabel: String
        get() = getCachedValue(ResourceField.MATERIAL_LABEL)
        set(materialLabel) = set(ResourceField.MATERIAL_LABEL, materialLabel)

    /**
     * Gets code field value.
     *
     * @return value
     */
    /**
     * Sets code field value.
     *
     * @param val value
     */
    var code: String
        get() = getCachedValue(ResourceField.CODE)
        set(`val`) = set(ResourceField.CODE, `val`)

    /**
     * Gets Group field value.
     *
     * @return value
     */
    /**
     * Sets Group field value.
     *
     * @param val value
     */
    var group: String
        get() = getCachedValue(ResourceField.GROUP)
        set(`val`) = set(ResourceField.GROUP, `val`)

    /**
     * Retrieve the messaging method used to communicate with a project team.
     *
     * @return messaging method
     */
    /**
     * Set the messaging method used to communicate with a project team.
     *
     * @param workGroup messaging method
     */
    var workGroup: WorkGroup
        get() = getCachedValue(ResourceField.WORKGROUP) as WorkGroup?
        set(workGroup) {
            set(ResourceField.WORKGROUP, workGroup)
        }

    /**
     * Retrieves the resource's email address.
     *
     * @return email address
     */
    /**
     * Set the resource's email address.
     *
     * @param emailAddress email address
     */
    var emailAddress: String
        get() = getCachedValue(ResourceField.EMAIL_ADDRESS)
        set(emailAddress) = set(ResourceField.EMAIL_ADDRESS, emailAddress)

    /**
     * Retrieves the hyperlink text.
     *
     * @return hyperlink text
     */
    /**
     * Sets the hyperlink text.
     *
     * @param hyperlink hyperlink text
     */
    var hyperlink: String
        get() = getCachedValue(ResourceField.HYPERLINK)
        set(hyperlink) = set(ResourceField.HYPERLINK, hyperlink)

    /**
     * Retrieves the hyperlink address.
     *
     * @return hyperlink address
     */
    /**
     * Sets the hyperlink address.
     *
     * @param hyperlinkAddress hyperlink address
     */
    var hyperlinkAddress: String
        get() = getCachedValue(ResourceField.HYPERLINK_ADDRESS)
        set(hyperlinkAddress) = set(ResourceField.HYPERLINK_ADDRESS, hyperlinkAddress)

    /**
     * Retrieves the hyperlink sub-address.
     *
     * @return hyperlink sub-address
     */
    /**
     * Sets the hyperlink sub-address.
     *
     * @param hyperlinkSubAddress hyperlink sub-address
     */
    var hyperlinkSubAddress: String
        get() = getCachedValue(ResourceField.HYPERLINK_SUBADDRESS)
        set(hyperlinkSubAddress) = set(ResourceField.HYPERLINK_SUBADDRESS, hyperlinkSubAddress)

    /**
     * Retrieves the maximum availability of a resource.
     *
     * @return maximum availability
     */
    /**
     * Sets the maximum availability of a resource.
     *
     * @param maxUnits maximum availability
     */
    var maxUnits: Number
        get() = getCachedValue(ResourceField.MAX_UNITS)
        set(maxUnits) = set(ResourceField.MAX_UNITS, maxUnits)

    /**
     * Retrieves the peak resource utilisation.
     *
     * @return peak resource utilisation
     */
    /**
     * Sets peak resource utilisation.
     *
     * @param peakUnits peak resource utilisation
     */
    var peakUnits: Number
        get() = getCachedValue(ResourceField.PEAK)
        set(peakUnits) = set(ResourceField.PEAK, peakUnits)

    /**
     * Retrieves the overallocated flag.
     *
     * @return overallocated flag
     */
    /**
     * Set the overallocated flag.
     *
     * @param overallocated overallocated flag
     */
    var overAllocated: Boolean
        get() {
            var overallocated = getCachedValue(ResourceField.OVERALLOCATED) as Boolean?
            if (overallocated == null) {
                val peakUnits = peakUnits
                val maxUnits = maxUnits
                overallocated = Boolean.valueOf(NumberHelper.getDouble(peakUnits) > NumberHelper.getDouble(maxUnits))
                set(ResourceField.OVERALLOCATED, overallocated)
            }
            return overallocated!!.booleanValue()
        }
        set(overallocated) {
            set(ResourceField.OVERALLOCATED, overallocated)
        }

    /**
     * Retrieves the "available from" date.
     *
     * @return available from date
     */
    /**
     * Set the "available from" date.
     *
     * @param date available from date
     */
    var availableFrom: Date
        get() = getCachedValue(ResourceField.AVAILABLE_FROM) as Date?
        set(date) {
            set(ResourceField.AVAILABLE_FROM, date)
        }

    /**
     * Retrieves the "available to" date.
     *
     * @return available from date
     */
    /**
     * Set the "available to" date.
     *
     * @param date available to date
     */
    var availableTo: Date
        get() = getCachedValue(ResourceField.AVAILABLE_TO) as Date?
        set(date) {
            set(ResourceField.AVAILABLE_TO, date)
        }

    /**
     * Retrieves the earliest start date for all assigned tasks.
     *
     * @return start date
     */
    val start: Date?
        get() {
            var result: Date? = null
            for (assignment in m_assignments) {
                if (result == null || DateHelper.compare(result, assignment.start) > 0) {
                    result = assignment.start
                }
            }
            return result
        }

    /**
     * Retrieves the latest finish date for all assigned tasks.
     *
     * @return finish date
     */
    val finish: Date?
        get() {
            var result: Date? = null
            for (assignment in m_assignments) {
                if (result == null || DateHelper.compare(result, assignment.finish) < 0) {
                    result = assignment.finish
                }
            }
            return result
        }

    /**
     * Retrieves the flag indicating if the resource levelling can be applied to
     * this resource.
     *
     * @return boolean flag
     */
    /**
     * Sets the flag indicating if the resource levelling can be applied to this
     * resource.
     *
     * @param canLevel boolean flag
     */
    var canLevel: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ResourceField.CAN_LEVEL) as Boolean?)
        set(canLevel) {
            set(ResourceField.CAN_LEVEL, canLevel)
        }

    /**
     * Gets the Accrue at type.The Accrue At field provides choices for how and
     * when resource standard and overtime costs are to be charged, or accrued,
     * to the cost of a task. The options are: Start, End and Proraetd (Default)
     *
     * @return accrue type
     */
    /**
     * Sets the Accrue at type.The Accrue At field provides choices for how and
     * when resource standard and overtime costs are to be charged, or accrued,
     * to the cost of a task. The options are: Start, End and Prorated (Default)
     *
     * @param type accrue type
     */
    var accrueAt: AccrueType
        get() = getCachedValue(ResourceField.ACCRUE_AT) as AccrueType?
        set(type) {
            set(ResourceField.ACCRUE_AT, type)
        }

    /**
     * Gets Work field value.
     *
     * @return value
     */
    /**
     * This field is ignored on import into MS Project.
     *
     * @param val - value to be set
     */
    var work: Duration?
        get() = getCachedValue(ResourceField.WORK) as Duration?
        set(`val`) {
            set(ResourceField.WORK, `val`)
        }

    /**
     * Retrieve the value of the regular work field. Note that this value is an
     * extension to the MPX specification.
     *
     * @return Regular work value
     */
    /**
     * Set the value of the regular work field. Note that this value is an
     * extension to the MPX specification.
     *
     * @param duration Regular work value
     */
    var regularWork: Duration
        get() = getCachedValue(ResourceField.REGULAR_WORK) as Duration?
        set(duration) {
            set(ResourceField.REGULAR_WORK, duration)
        }

    /**
     * Retrieves the Actual Work field contains the amount of work that has
     * already been done for all assignments assigned to a resource.
     *
     * @return Actual work value
     */
    /**
     * Sets the Actual Work field contains the amount of work that has already
     * been done for all assignments assigned to a resource.
     *
     * @param val duration value
     */
    var actualWork: Duration
        get() = getCachedValue(ResourceField.ACTUAL_WORK) as Duration?
        set(`val`) {
            set(ResourceField.ACTUAL_WORK, `val`)
        }

    /**
     * Retrieves the amount of overtime work.
     *
     * @return overtime work
     */
    /**
     * Sets the amount of overtime work.
     *
     * @param overtimeWork overtime work
     */
    var overtimeWork: Duration
        get() = getCachedValue(ResourceField.OVERTIME_WORK) as Duration?
        set(overtimeWork) {
            set(ResourceField.OVERTIME_WORK, overtimeWork)
        }

    /**
     * Gets Remaining Work field value.
     *
     * @return value
     */
    /**
     * This field is ignored on import into MS Project.
     *
     * @param val - value to be set
     */
    var remainingWork: Duration
        get() = getCachedValue(ResourceField.REMAINING_WORK) as Duration?
        set(`val`) {
            set(ResourceField.REMAINING_WORK, `val`)
        }

    /**
     * Retrieve the value of the actual overtime work field.
     *
     * @return actual overtime work value
     */
    /**
     * Sets the value of the actual overtime work field.
     *
     * @param duration actual overtime work value
     */
    var actualOvertimeWork: Duration
        get() = getCachedValue(ResourceField.ACTUAL_OVERTIME_WORK) as Duration?
        set(duration) {
            set(ResourceField.ACTUAL_OVERTIME_WORK, duration)
        }

    /**
     * Retrieve the value of the remaining overtime work field.
     *
     * @return remaining overtime work value
     */
    /**
     * Sets the value of the remaining overtime work field.
     *
     * @param duration remaining overtime work value
     */
    var remainingOvertimeWork: Duration
        get() = getCachedValue(ResourceField.REMAINING_OVERTIME_WORK) as Duration?
        set(duration) {
            set(ResourceField.REMAINING_OVERTIME_WORK, duration)
        }

    /**
     * Retrieves the value of the percent work complete field.
     *
     * @return percent work complete
     */
    /**
     * Sets the value of the percent work complete field.
     *
     * @param percentWorkComplete percent work complete
     */
    var percentWorkComplete: Number
        get() = getCachedValue(ResourceField.PERCENT_WORK_COMPLETE)
        set(percentWorkComplete) = set(ResourceField.PERCENT_WORK_COMPLETE, percentWorkComplete)

    /**
     * Gets Standard Rate field value.
     *
     * @return Rate
     */
    /**
     * Sets standard rate for this resource.
     *
     * @param val value
     */
    var standardRate: Rate
        get() = getCachedValue(ResourceField.STANDARD_RATE)
        set(`val`) = set(ResourceField.STANDARD_RATE, `val`)

    /**
     * Retrieves the format of the standard rate.
     *
     * @return standard rate format
     */
    /**
     * Sets the format of the standard rate.
     *
     * @param units standard rate format
     */
    var standardRateUnits: TimeUnit
        get() = getCachedValue(ResourceField.STANDARD_RATE_UNITS) as TimeUnit?
        set(units) {
            set(ResourceField.STANDARD_RATE_UNITS, units)
        }

    /**
     * Retrieves the cost field value.
     *
     * @return cost field value
     */
    /**
     * Sets the cost field value.
     *
     * @param cost cost field value
     */
    var cost: Number?
        get() = getCachedValue(ResourceField.COST)
        set(cost) = set(ResourceField.COST, cost)

    /**
     * Retrieves the overtime rate for this resource.
     *
     * @return overtime rate
     */
    /**
     * Sets the overtime rate for this resource.
     *
     * @param overtimeRate overtime rate value
     */
    var overtimeRate: Rate
        get() = getCachedValue(ResourceField.OVERTIME_RATE)
        set(overtimeRate) = set(ResourceField.OVERTIME_RATE, overtimeRate)

    /**
     * Retrieves the format of the overtime rate.
     *
     * @return overtime rate format
     */
    /**
     * Sets the format of the overtime rate.
     *
     * @param units overtime rate format
     */
    var overtimeRateUnits: TimeUnit
        get() = getCachedValue(ResourceField.OVERTIME_RATE_UNITS) as TimeUnit?
        set(units) {
            set(ResourceField.OVERTIME_RATE_UNITS, units)
        }

    /**
     * Retrieve the value of the overtime cost field.
     *
     * @return Overtime cost value
     */
    /**
     * Set the value of the overtime cost field.
     *
     * @param currency Overtime cost
     */
    var overtimeCost: Number
        get() = getCachedValue(ResourceField.OVERTIME_COST)
        set(currency) = set(ResourceField.OVERTIME_COST, currency)

    /**
     * Retrieve the cost per use.
     *
     * @return cost per use
     */
    /**
     * Set the cost per use.
     *
     * @param costPerUse cost per use
     */
    var costPerUse: Number
        get() = getCachedValue(ResourceField.COST_PER_USE)
        set(costPerUse) = set(ResourceField.COST_PER_USE, costPerUse)

    /**
     * Retrieves the actual cost for the work already performed by this resource.
     *
     * @return actual cost
     */
    /**
     * Set the actual cost for the work already performed by this resource.
     *
     * @param actualCost actual cost
     */
    var actualCost: Number
        get() = getCachedValue(ResourceField.ACTUAL_COST)
        set(actualCost) = set(ResourceField.ACTUAL_COST, actualCost)

    /**
     * Retrieve actual overtime cost.
     *
     * @return actual overtime cost
     */
    /**
     * Sets the actual overtime cost.
     *
     * @param actualOvertimeCost actual overtime cost
     */
    var actualOvertimeCost: Number
        get() = getCachedValue(ResourceField.ACTUAL_OVERTIME_COST)
        set(actualOvertimeCost) = set(ResourceField.ACTUAL_OVERTIME_COST, actualOvertimeCost)

    /**
     * Retrieves the remaining cost for this resource.
     *
     * @return remaining cost
     */
    /**
     * Sets the remaining cost for this resource.
     *
     * @param remainingCost remaining cost
     */
    var remainingCost: Number
        get() = getCachedValue(ResourceField.REMAINING_COST)
        set(remainingCost) = set(ResourceField.REMAINING_COST, remainingCost)

    /**
     * Retrieve the remaining overtime cost.
     *
     * @return remaining overtime cost
     */
    /**
     * Set the remaining overtime cost.
     *
     * @param remainingOvertimeCost remaining overtime cost
     */
    var remainingOvertimeCost: Number
        get() = getCachedValue(ResourceField.REMAINING_OVERTIME_COST)
        set(remainingOvertimeCost) = set(ResourceField.REMAINING_OVERTIME_COST, remainingOvertimeCost)

    /**
     * Retrieves the work variance.
     *
     * @return work variance
     */
    /**
     * Sets the work variance.
     *
     * @param workVariance work variance
     */
    var workVariance: Duration?
        get() {
            var variance = getCachedValue(ResourceField.WORK_VARIANCE) as Duration?
            if (variance == null) {
                val work = work
                val baselineWork = baselineWork
                if (work != null && baselineWork != null) {
                    variance = Duration.getInstance(work!!.getDuration() - baselineWork!!.convertUnits(work!!.getUnits(), parentFile.projectProperties).getDuration(), work!!.getUnits())
                    set(ResourceField.WORK_VARIANCE, variance)
                }
            }
            return variance
        }
        set(workVariance) {
            set(ResourceField.WORK_VARIANCE, workVariance)
        }

    /**
     * Retrieves the cost variance.
     *
     * @return cost variance
     */
    /**
     * Sets the cost variance.
     *
     * @param costVariance cost variance
     */
    var costVariance: Number?
        get() {
            var variance = getCachedValue(ResourceField.COST_VARIANCE) as Number?
            if (variance == null) {
                val cost = cost
                val baselineCost = baselineCost
                if (cost != null && baselineCost != null) {
                    variance = NumberHelper.getDouble(cost.doubleValue() - baselineCost.doubleValue())
                    set(ResourceField.COST_VARIANCE, variance)
                }
            }
            return variance
        }
        set(costVariance) = set(ResourceField.COST_VARIANCE, costVariance)

    /**
     * Retrieve the schedule variance.
     *
     * @return schedule variance
     */
    /**
     * Set the schedule variance.
     *
     * @param sv schedule variance
     */
    var sv: Number?
        get() {
            var variance = getCachedValue(ResourceField.SV) as Number?
            if (variance == null) {
                val bcwp = bcwp
                val bcws = bcws
                if (bcwp != null && bcws != null) {
                    variance = NumberHelper.getDouble(bcwp.doubleValue() - bcws.doubleValue())
                    set(ResourceField.SV, variance)
                }
            }
            return variance
        }
        set(sv) = set(ResourceField.SV, sv)

    /**
     * Retrieve the cost variance.
     *
     * @return cost variance
     */
    /**
     * Set the cost variance.
     *
     * @param cv cost variance
     */
    var cv: Number
        get() {
            var variance = getCachedValue(ResourceField.CV) as Number?
            if (variance == null) {
                variance = Double.valueOf(NumberHelper.getDouble(bcwp) - NumberHelper.getDouble(acwp))
                set(ResourceField.CV, variance)
            }
            return variance
        }
        set(cv) = set(ResourceField.CV, cv)

    /**
     * Set the actual cost of work performed.
     *
     * @return actual cost of work performed
     */
    /**
     * Set the actual cost of work performed.
     *
     * @param acwp actual cost of work performed
     */
    var acwp: Number
        get() = getCachedValue(ResourceField.ACWP)
        set(acwp) = set(ResourceField.ACWP, acwp)

    /**
     * Retrieves the notes text for this resource.
     *
     * @return notes text
     */
    /**
     * Sets the notes text for this resource.
     *
     * @param notes notes to be added
     */
    var notes: String
        get() {
            val notes = getCachedValue(ResourceField.NOTES) as String?
            return notes ?: ""
        }
        set(notes) = set(ResourceField.NOTES, notes)

    /**
     * Retrieves the budgeted cost of work scheduled.
     *
     * @return budgeted cost of work scheduled
     */
    /**
     * Sets the budgeted cost of work scheduled.
     *
     * @param bcws budgeted cost of work scheduled
     */
    var bcws: Number?
        get() = getCachedValue(ResourceField.BCWS)
        set(bcws) = set(ResourceField.BCWS, bcws)

    /**
     * Retrieves the budgeted cost of work performed.
     *
     * @return budgeted cost of work performed
     */
    /**
     * Sets the budgeted cost of work performed.
     *
     * @param bcwp budgeted cost of work performed
     */
    var bcwp: Number?
        get() = getCachedValue(ResourceField.BCWP)
        set(bcwp) = set(ResourceField.BCWP, bcwp)

    /**
     * Retrieves the generic flag.
     *
     * @return generic flag
     */
    /**
     * Sets the generic flag.
     *
     * @param value generic flag
     */
    var generic: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ResourceField.GENERIC) as Boolean?)
        set(value) {
            set(ResourceField.GENERIC, value)
        }

    /**
     * Retrieves the inactive flag.
     *
     * @return inactive flag
     */
    val inactive: Boolean
        @Deprecated("use setActive")
        get() = !active

    /**
     * Retrieves the active flag.
     *
     * @return generic flag
     */
    /**
     * Sets the active flag.
     *
     * @param value generic flag
     */
    var active: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ResourceField.ACTIVE) as Boolean?)
        set(value) {
            set(ResourceField.ACTIVE, value)
        }

    /**
     * Retrieves the active directory GUID for this resource.
     *
     * @return active directory GUID
     */
    val activeDirectoryGUID: String?
        get() = m_activeDirectoryGUID

    /**
     * Retrieves the booking type.
     *
     * @return booking type
     */
    /**
     * Sets the booking type.
     *
     * @param bookingType booking type
     */
    var bookingType: BookingType
        get() = getCachedValue(ResourceField.BOOKING_TYPE) as BookingType?
        set(bookingType) {
            set(ResourceField.BOOKING_TYPE, bookingType)
        }

    /**
     * Retrieves the creation date.
     *
     * @return creation date
     */
    /**
     * Sets the creation date.
     *
     * @param creationDate creation date
     */
    var creationDate: Date
        get() = getCachedValue(ResourceField.CREATED) as Date?
        set(creationDate) {
            set(ResourceField.CREATED, creationDate)
        }

    /**
     * Retrieves a flag indicating that a resource is an enterprise resource.
     *
     * @return boolean flag
     */
    /**
     * Sets a flag indicating that a resource is an enterprise resource.
     *
     * @param enterprise boolean flag
     */
    var enterprise: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ResourceField.ENTERPRISE) as Boolean?)
        set(enterprise) {
            set(ResourceField.ENTERPRISE, enterprise)
        }

    /**
     * This method retrieves the calendar associated with this resource.
     *
     * @return ProjectCalendar instance
     */
    /**
     * This method allows a pre-existing resource calendar to be attached to a
     * resource.
     *
     * @param calendar resource calendar
     */
    var resourceCalendar: ProjectCalendar?
        get() = getCachedValue(ResourceField.CALENDAR)
        set(calendar) {
            set(ResourceField.CALENDAR, calendar)
            if (calendar == null) {
                resourceCalendarUniqueID = null
            } else {
                calendar.resource = this
                resourceCalendarUniqueID = calendar.uniqueID
            }
        }

    /**
     * Retrieve the calendar unique ID.
     *
     * @return calendar unique ID
     */
    /**
     * Set the calendar unique ID.
     *
     * @param id calendar unique ID
     */
    var resourceCalendarUniqueID: Integer?
        get() = getCachedValue(ResourceField.CALENDAR_UNIQUE_ID) as Integer?
        set(id) {
            set(ResourceField.CALENDAR_UNIQUE_ID, id)
        }

    /**
     * Retrieves Base Calendar name associated with this resource. This field
     * indicates which calendar is the base calendar for a resource calendar.
     *
     * @return Base calendar name
     */
    /**
     * Sets the Base Calendar field indicates which calendar is the base calendar
     * for a resource calendar. The list includes the three built-in calendars,
     * as well as any new base calendars you have created in the Change Working
     * Time dialog box.
     *
     * @param val calendar name
     */
    var baseCalendar: String?
        get() = getCachedValue(ResourceField.BASE_CALENDAR)
        set(`val`) = set(ResourceField.BASE_CALENDAR, if (`val` == null || `val`.length() === 0) "Standard" else `val`)

    /**
     * Retrieves the Baseline Cost value. This value is the total planned cost
     * for a resource for all assigned tasks. Baseline cost is also referred to
     * as budget at completion (BAC).
     *
     * @return Baseline cost value
     */
    /**
     * Sets the baseline cost. This field is ignored on import into MS Project
     *
     * @param val - value to be set
     */
    var baselineCost: Number?
        get() = getCachedValue(ResourceField.BASELINE_COST)
        set(`val`) = set(ResourceField.BASELINE_COST, `val`)

    /**
     * Retrieves the Baseline Work value.
     *
     * @return Baseline work value
     */
    /**
     * Sets the baseline work duration. This field is ignored on import into MS
     * Project.
     *
     * @param val - value to be set
     */
    var baselineWork: Duration?
        get() = getCachedValue(ResourceField.BASELINE_WORK) as Duration?
        set(`val`) {
            set(ResourceField.BASELINE_WORK, `val`)
        }

    /**
     * Gets ID field value.
     *
     * @return value
     */
    /**
     * Sets ID field value.
     *
     * @param val value
     */
    override var id: Integer?
        @Override get() = getCachedValue(ResourceField.ID) as Integer?
        @Override set(`val`) {
            val parent = parentFile
            val previous = id
            if (previous != null) {
                parent.resources.unmapID(previous)
            }
            parent.resources.mapID(`val`, this)

            set(ResourceField.ID, `val`)
        }

    /**
     * Gets Linked Fields field value.
     *
     * @return value
     */
    /**
     * This field is ignored on import into MS Project.
     *
     * @param val - value to be set
     */
    var linkedFields: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ResourceField.LINKED_FIELDS) as Boolean?)
        set(`val`) {
            set(ResourceField.LINKED_FIELDS, `val`)
        }

    /**
     * Gets objects field value.
     *
     * @return value
     */
    /**
     * Set objects.
     *
     * @param val - value to be set
     */
    var objects: Integer
        get() = getCachedValue(ResourceField.OBJECTS) as Integer?
        set(`val`) {
            set(ResourceField.OBJECTS, `val`)
        }

    /**
     * Gets Unique ID field value.
     *
     * @return value
     */
    /**
     * Sets Unique ID of this resource.
     *
     * @param val Unique ID
     */
    override var uniqueID: Integer
        @Override get() = getCachedValue(ResourceField.UNIQUE_ID) as Integer?
        @Override set(`val`) {
            set(ResourceField.UNIQUE_ID, `val`)
        }

    /**
     * Gets Parent ID field value.
     *
     * @return value
     */
    /**
     * Sets Parent ID of this resource.
     *
     * @param val Parent ID
     */
    var parentID: Integer
        get() = getCachedValue(ResourceField.PARENT_ID) as Integer?
        set(`val`) {
            set(ResourceField.PARENT_ID, `val`)
        }

    /**
     * Retrieves the value of an outline code field.
     *
     * @return outline code value
     */
    /**
     * Sets the value of an outline code field.
     *
     * @param value outline code value
     */
    var outlineCode1: String
        get() = getCachedValue(ResourceField.OUTLINE_CODE1)
        set(value) = set(ResourceField.OUTLINE_CODE1, value)

    /**
     * Retrieves the value of an outline code field.
     *
     * @return outline code value
     */
    /**
     * Sets the value of an outline code field.
     *
     * @param value outline code value
     */
    var outlineCode2: String
        get() = getCachedValue(ResourceField.OUTLINE_CODE2)
        set(value) = set(ResourceField.OUTLINE_CODE2, value)

    /**
     * Retrieves the value of an outline code field.
     *
     * @return outline code value
     */
    /**
     * Sets the value of an outline code field.
     *
     * @param value outline code value
     */
    var outlineCode3: String
        get() = getCachedValue(ResourceField.OUTLINE_CODE3)
        set(value) = set(ResourceField.OUTLINE_CODE3, value)

    /**
     * Retrieves the value of an outline code field.
     *
     * @return outline code value
     */
    /**
     * Sets the value of an outline code field.
     *
     * @param value outline code value
     */
    var outlineCode4: String
        get() = getCachedValue(ResourceField.OUTLINE_CODE4)
        set(value) = set(ResourceField.OUTLINE_CODE4, value)

    /**
     * Retrieves the value of an outline code field.
     *
     * @return outline code value
     */
    /**
     * Sets the value of an outline code field.
     *
     * @param value outline code value
     */
    var outlineCode5: String
        get() = getCachedValue(ResourceField.OUTLINE_CODE5)
        set(value) = set(ResourceField.OUTLINE_CODE5, value)

    /**
     * Retrieves the value of an outline code field.
     *
     * @return outline code value
     */
    /**
     * Sets the value of an outline code field.
     *
     * @param value outline code value
     */
    var outlineCode6: String
        get() = getCachedValue(ResourceField.OUTLINE_CODE6)
        set(value) = set(ResourceField.OUTLINE_CODE6, value)

    /**
     * Retrieves the value of an outline code field.
     *
     * @return outline code value
     */
    /**
     * Sets the value of an outline code field.
     *
     * @param value outline code value
     */
    var outlineCode7: String
        get() = getCachedValue(ResourceField.OUTLINE_CODE7)
        set(value) = set(ResourceField.OUTLINE_CODE7, value)

    /**
     * Retrieves the value of an outline code field.
     *
     * @return outline code value
     */
    /**
     * Sets the value of an outline code field.
     *
     * @param value outline code value
     */
    var outlineCode8: String
        get() = getCachedValue(ResourceField.OUTLINE_CODE8)
        set(value) = set(ResourceField.OUTLINE_CODE8, value)

    /**
     * Retrieves the value of an outline code field.
     *
     * @return outline code value
     */
    /**
     * Sets the value of an outline code field.
     *
     * @param value outline code value
     */
    var outlineCode9: String
        get() = getCachedValue(ResourceField.OUTLINE_CODE9)
        set(value) = set(ResourceField.OUTLINE_CODE9, value)

    /**
     * Retrieves the value of an outline code field.
     *
     * @return outline code value
     */
    /**
     * Sets the value of an outline code field.
     *
     * @param value outline code value
     */
    var outlineCode10: String
        get() = getCachedValue(ResourceField.OUTLINE_CODE10)
        set(value) = set(ResourceField.OUTLINE_CODE10, value)

    /**
     * Retrieve a list of tasks assigned to this resource. Note that if this
     * project data has been read from an MPX file which declared some or all of
     * the resources assignments before the tasks and resources to which the
     * assignments relate, then these assignments may not appear in this list.
     * Caveat emptor!
     *
     * @return list of tasks assigned to this resource
     */
    val taskAssignments: List<ResourceAssignment>
        get() = m_assignments

    /**
     * Where a resource in an MPP file represents a resource from a subproject,
     * this value will be non-zero. The value itself is the unique ID value shown
     * in the parent project. To retrieve the value of the resource unique ID in
     * the child project, remove the top two bytes:
     *
     * resourceID = (subprojectUniqueID & 0xFFFF)
     *
     * @return sub project unique resource ID
     */
    /**
     * Sets the sub project unique resource ID.
     *
     * @param subprojectUniqueResourceID subproject unique resource ID
     */
    var subprojectResourceUniqueID: Integer
        get() = getCachedValue(ResourceField.SUBPROJECT_RESOURCE_UNIQUE_ID) as Integer?
        set(subprojectUniqueResourceID) {
            set(ResourceField.SUBPROJECT_RESOURCE_UNIQUE_ID, subprojectUniqueResourceID)
        }

    /**
     * Retrieve the budget flag.
     *
     * @return budget flag
     */
    /**
     * Set the budget flag.
     *
     * @param budget budget flag
     */
    var budget: Boolean
        get() = BooleanHelper.getBoolean(getCachedValue(ResourceField.BUDGET) as Boolean?)
        set(budget) {
            set(ResourceField.BUDGET, budget)
        }

    /**
     * Retrieves the resource GUID.
     *
     * @return resource GUID.
     */
    /**
     * Sets the resource GUID.
     *
     * @param value resource GUID
     */
    var guid: UUID
        get() = getCachedValue(ResourceField.GUID) as UUID?
        set(value) {
            set(ResourceField.GUID, value)
        }

    /**
     * Array of field values.
     */
    private val m_array = arrayOfNulls<Object>(ResourceField.MAX_VALUE)

    /**
     * List of all assignments for this resource.
     */
    private val m_assignments = LinkedList<ResourceAssignment>()

    private var m_eventsEnabled = true
    private var m_null: Boolean = false
    private var m_activeDirectoryGUID: String? = null
    /**
     * Retrieves the actual overtime work protected duration.
     *
     * @return actual overtime work protected
     */
    /**
     * Sets the actual overtime work protected duration.
     *
     * @param duration actual overtime work protected
     */
    var actualOvertimeWorkProtected: Duration? = null
        get() = field
    /**
     * Retrieves the actual work protected duration.
     *
     * @return actual work protected
     */
    /**
     * Sets the actual work protected duration.
     *
     * @param duration actual work protected
     */
    var actualWorkProtected: Duration? = null
        get() = field

    private val m_costRateTables = arrayOfNulls<CostRateTable>(5)
    /**
     * Retrieve the availability table for this resource.
     *
     * @return availability table
     */
    val availability = AvailabilityTable()
    private var m_listeners: List<FieldListener>? = null

    init {

        type = ResourceType.WORK
        val config = file.projectConfig

        if (config.autoResourceUniqueID == true) {
            uniqueID = Integer.valueOf(config.nextResourceUniqueID)
        }

        if (config.autoResourceID == true) {
            id = Integer.valueOf(config.nextResourceID)
        }
    }

    /**
     * Set the flag indicating that this is a null resource.
     *
     * @param isNull null resource flag
     */
    fun setIsNull(isNull: Boolean) {
        m_null = isNull
    }

    /**
     * Sets the generic flag.
     *
     * @param isGeneric generic flag
     */
    @Deprecated("use setGeneric")
    fun setIsGeneric(isGeneric: Boolean) {
        set(ResourceField.GENERIC, isGeneric)
    }

    /**
     * Sets the inactive flag.
     *
     * @param isInactive inactive flag
     */
    @Deprecated("use getActive")
    fun setIsInactive(isInactive: Boolean) {
        active = !isInactive
    }

    /**
     * Sets the active directory GUID for this resource.
     *
     * @param guid active directory GUID
     */
    fun setActveDirectoryGUID(guid: String) {
        m_activeDirectoryGUID = guid
    }

    /**
     * Sets a flag indicating that a resource is an enterprise resource.
     *
     * @param enterprise boolean flag
     */
    @Deprecated("use setEnterprise")
    fun setIsEnterprise(enterprise: Boolean) {
        set(ResourceField.ENTERPRISE, enterprise)
    }

    /**
     * This method allows a resource calendar to be added to a resource.
     *
     * @return ResourceCalendar
     * @throws MPXJException if more than one calendar is added
     */
    @Throws(MPXJException::class)
    fun addResourceCalendar(): ProjectCalendar {
        if (resourceCalendar != null) {
            throw MPXJException(MPXJException.MAXIMUM_RECORDS)
        }

        val calendar = ProjectCalendar(parentFile)
        resourceCalendar = calendar
        return calendar
    }

    /**
     * Set a text value.
     *
     * @param index text index (1-30)
     * @param value text value
     */
    fun setText(index: Int, value: String) {
        set(selectField(ResourceFieldLists.CUSTOM_TEXT, index), value)
    }

    /**
     * Retrieve a text value.
     *
     * @param index text index (1-30)
     * @return text value
     */
    fun getText(index: Int): String {
        return getCachedValue(selectField(ResourceFieldLists.CUSTOM_TEXT, index))
    }

    /**
     * Set a start value.
     *
     * @param index start index (1-10)
     * @param value start value
     */
    fun setStart(index: Int, value: Date) {
        set(selectField(ResourceFieldLists.CUSTOM_START, index), value)
    }

    /**
     * Retrieve a start value.
     *
     * @param index start index (1-10)
     * @return start value
     */
    fun getStart(index: Int): Date {
        return getCachedValue(selectField(ResourceFieldLists.CUSTOM_START, index)) as Date?
    }

    /**
     * Set a finish value.
     *
     * @param index finish index (1-10)
     * @param value finish value
     */
    fun setFinish(index: Int, value: Date) {
        set(selectField(ResourceFieldLists.CUSTOM_FINISH, index), value)
    }

    /**
     * Retrieve a finish value.
     *
     * @param index finish index (1-10)
     * @return finish value
     */
    fun getFinish(index: Int): Date {
        return getCachedValue(selectField(ResourceFieldLists.CUSTOM_FINISH, index)) as Date?
    }

    /**
     * Set a number value.
     *
     * @param index number index (1-20)
     * @param value number value
     */
    fun setNumber(index: Int, value: Number) {
        set(selectField(ResourceFieldLists.CUSTOM_NUMBER, index), value)
    }

    /**
     * Retrieve a number value.
     *
     * @param index number index (1-20)
     * @return number value
     */
    fun getNumber(index: Int): Number {
        return getCachedValue(selectField(ResourceFieldLists.CUSTOM_NUMBER, index))
    }

    /**
     * Set a duration value.
     *
     * @param index duration index (1-10)
     * @param value duration value
     */
    fun setDuration(index: Int, value: Duration) {
        set(selectField(ResourceFieldLists.CUSTOM_DURATION, index), value)
    }

    /**
     * Retrieve a duration value.
     *
     * @param index duration index (1-10)
     * @return duration value
     */
    fun getDuration(index: Int): Duration {
        return getCachedValue(selectField(ResourceFieldLists.CUSTOM_DURATION, index)) as Duration?
    }

    /**
     * Set a date value.
     *
     * @param index date index (1-10)
     * @param value date value
     */
    fun setDate(index: Int, value: Date) {
        set(selectField(ResourceFieldLists.CUSTOM_DATE, index), value)
    }

    /**
     * Retrieve a date value.
     *
     * @param index date index (1-10)
     * @return date value
     */
    fun getDate(index: Int): Date {
        return getCachedValue(selectField(ResourceFieldLists.CUSTOM_DATE, index)) as Date?
    }

    /**
     * Set a cost value.
     *
     * @param index cost index (1-10)
     * @param value cost value
     */
    fun setCost(index: Int, value: Number) {
        set(selectField(ResourceFieldLists.CUSTOM_COST, index), value)
    }

    /**
     * Retrieve a cost value.
     *
     * @param index cost index (1-10)
     * @return cost value
     */
    fun getCost(index: Int): Number {
        return getCachedValue(selectField(ResourceFieldLists.CUSTOM_COST, index))
    }

    /**
     * Set a flag value.
     *
     * @param index flag index (1-20)
     * @param value flag value
     */
    fun setFlag(index: Int, value: Boolean) {
        set(selectField(ResourceFieldLists.CUSTOM_FLAG, index), value)
    }

    /**
     * Retrieve a flag value.
     *
     * @param index flag index (1-20)
     * @return flag value
     */
    fun getFlag(index: Int): Boolean {
        return BooleanHelper.getBoolean(getCachedValue(selectField(ResourceFieldLists.CUSTOM_FLAG, index)) as Boolean?)
    }

    /**
     * Removes this resource from the project.
     */
    fun remove() {
        parentFile.removeResource(this)
    }

    /**
     * Retrieve the value of a field using its alias.
     *
     * @param alias field alias
     * @return field value
     */
    fun getFieldByAlias(alias: String): Object? {
        return getCachedValue(parentFile.customFields.getFieldByAlias(FieldTypeClass.RESOURCE, alias))
    }

    /**
     * Set the value of a field using its alias.
     *
     * @param alias field alias
     * @param value field value
     */
    fun setFieldByAlias(alias: String, value: Object) {
        set(parentFile.customFields.getFieldByAlias(FieldTypeClass.RESOURCE, alias), value)
    }

    /**
     * This method is used internally within MPXJ to track tasks which are
     * assigned to a particular resource.
     *
     * @param assignment resource assignment instance
     */
    fun addResourceAssignment(assignment: ResourceAssignment) {
        m_assignments.add(assignment)
    }

    /**
     * Internal method used as part of the process of removing a resource
     * assignment.
     *
     * @param assignment resource assignment to be removed
     */
    internal fun removeResourceAssignment(assignment: ResourceAssignment) {
        m_assignments.remove(assignment)
    }

    /**
     * Retrieve an enterprise field value.
     *
     * @param index field index
     * @return field value
     */
    fun getEnterpriseCost(index: Int): Number {
        return getCachedValue(selectField(ResourceFieldLists.ENTERPRISE_COST, index))
    }

    /**
     * Set an enterprise field value.
     *
     * @param index field index
     * @param value field value
     */
    fun setEnterpriseCost(index: Int, value: Number) {
        set(selectField(ResourceFieldLists.ENTERPRISE_COST, index), value)
    }

    /**
     * Retrieve an enterprise field value.
     *
     * @param index field index
     * @return field value
     */
    fun getEnterpriseDate(index: Int): Date {
        return getCachedValue(selectField(ResourceFieldLists.ENTERPRISE_DATE, index)) as Date?
    }

    /**
     * Set an enterprise field value.
     *
     * @param index field index
     * @param value field value
     */
    fun setEnterpriseDate(index: Int, value: Date) {
        set(selectField(ResourceFieldLists.ENTERPRISE_DATE, index), value)
    }

    /**
     * Retrieve an enterprise field value.
     *
     * @param index field index
     * @return field value
     */
    fun getEnterpriseDuration(index: Int): Duration {
        return getCachedValue(selectField(ResourceFieldLists.ENTERPRISE_DURATION, index)) as Duration?
    }

    /**
     * Set an enterprise field value.
     *
     * @param index field index
     * @param value field value
     */
    fun setEnterpriseDuration(index: Int, value: Duration) {
        set(selectField(ResourceFieldLists.ENTERPRISE_DURATION, index), value)
    }

    /**
     * Retrieve an enterprise field value.
     *
     * @param index field index
     * @return field value
     */
    fun getEnterpriseFlag(index: Int): Boolean {
        return BooleanHelper.getBoolean(getCachedValue(selectField(ResourceFieldLists.ENTERPRISE_FLAG, index)) as Boolean?)
    }

    /**
     * Set an enterprise field value.
     *
     * @param index field index
     * @param value field value
     */
    fun setEnterpriseFlag(index: Int, value: Boolean) {
        set(selectField(ResourceFieldLists.ENTERPRISE_FLAG, index), value)
    }

    /**
     * Retrieve an enterprise field value.
     *
     * @param index field index
     * @return field value
     */
    fun getEnterpriseNumber(index: Int): Number {
        return getCachedValue(selectField(ResourceFieldLists.ENTERPRISE_NUMBER, index))
    }

    /**
     * Set an enterprise field value.
     *
     * @param index field index
     * @param value field value
     */
    fun setEnterpriseNumber(index: Int, value: Number) {
        set(selectField(ResourceFieldLists.ENTERPRISE_NUMBER, index), value)
    }

    /**
     * Retrieve an enterprise field value.
     *
     * @param index field index
     * @return field value
     */
    fun getEnterpriseText(index: Int): String {
        return getCachedValue(selectField(ResourceFieldLists.ENTERPRISE_TEXT, index))
    }

    /**
     * Set an enterprise field value.
     *
     * @param index field index
     * @param value field value
     */
    fun setEnterpriseText(index: Int, value: String) {
        set(selectField(ResourceFieldLists.ENTERPRISE_TEXT, index), value)
    }

    /**
     * Retrieve an enterprise custom field value.
     *
     * @param index field index
     * @return field value
     */
    fun getEnterpriseCustomField(index: Int): String {
        return getCachedValue(selectField(ResourceFieldLists.ENTERPRISE_CUSTOM_FIELD, index))
    }

    /**
     * Set an enterprise custom field value.
     *
     * @param index field index
     * @param value field value
     */
    fun setEnterpriseCustomField(index: Int, value: String) {
        set(selectField(ResourceFieldLists.ENTERPRISE_CUSTOM_FIELD, index), value)
    }

    /**
     * Set a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @param value baseline value
     */
    fun setBaselineCost(baselineNumber: Int, value: Number) {
        set(selectField(ResourceFieldLists.BASELINE_COSTS, baselineNumber), value)
    }

    /**
     * Set a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @param value baseline value
     */
    fun setBaselineWork(baselineNumber: Int, value: Duration) {
        set(selectField(ResourceFieldLists.BASELINE_WORKS, baselineNumber), value)
    }

    /**
     * Retrieve a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @return baseline value
     */
    fun getBaselineCost(baselineNumber: Int): Number {
        return getCachedValue(selectField(ResourceFieldLists.BASELINE_COSTS, baselineNumber))
    }

    /**
     * Retrieve a baseline value.
     *
     * @param baselineNumber baseline index (1-10)
     * @return baseline value
     */
    fun getBaselineWork(baselineNumber: Int): Duration {
        return getCachedValue(selectField(ResourceFieldLists.BASELINE_WORKS, baselineNumber)) as Duration?
    }

    /**
     * Associates a complete cost rate table with the
     * current resource. Note that the index corresponds with the
     * letter label used by MS Project to identify each table.
     * For example 0=Table A, 1=Table B, 2=Table C, and so on.
     *
     * @param index table index
     * @param crt table instance
     */
    fun setCostRateTable(index: Int, crt: CostRateTable) {
        m_costRateTables[index] = crt
    }

    /**
     * Retrieves a cost rate table associated with a resource.
     * Note that the index corresponds with the
     * letter label used by MS Project to identify each table.
     * For example 0=Table A, 1=Table B, 2=Table C, and so on.
     *
     * @param index table index
     * @return table instance
     */
    fun getCostRateTable(index: Int): CostRateTable {
        return m_costRateTables[index]
    }

    /**
     * Maps a field index to a ResourceField instance.
     *
     * @param fields array of fields used as the basis for the mapping.
     * @param index required field index
     * @return ResourceField instance
     */
    private fun selectField(fields: Array<ResourceField>, index: Int): ResourceField {
        if (index < 1 || index > fields.size) {
            throw IllegalArgumentException("$index is not a valid field index")
        }
        return fields[index - 1]
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
    fun getCurrentValue(field: FieldType?): Object? {
        var result: Object? = null

        if (field != null) {
            val resourceField = field as ResourceField?

            when (resourceField) {
                ResourceField.COST_VARIANCE -> {
                    result = costVariance
                }

                ResourceField.WORK_VARIANCE -> {
                    result = workVariance
                }

                ResourceField.CV -> {
                    result = cv
                }

                ResourceField.SV -> {
                    result = sv
                }

                ResourceField.OVERALLOCATED -> {
                    result = Boolean.valueOf(overAllocated)
                }

                else -> {
                    result = m_array[field!!.getValue()]
                }
            }
        }

        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    operator fun set(field: FieldType?, value: Object) {
        if (field != null) {
            val index = field!!.getValue()
            if (m_eventsEnabled) {
                fireFieldChangeEvent((field as ResourceField?)!!, m_array[index], value)
            }
            m_array[index] = value
        }
    }

    /**
     * Handle the change in a field value. Reset any cached calculated
     * values affected by this change, pass on the event to any external
     * listeners.
     *
     * @param field field changed
     * @param oldValue old field value
     * @param newValue new field value
     */
    private fun fireFieldChangeEvent(field: ResourceField, oldValue: Object?, newValue: Object) {
        //
        // Internal event handling
        //
        when (field) {
            ResourceField.UNIQUE_ID -> {
                val parent = parentFile
                if (oldValue != null) {
                    parent.resources.unmapUniqueID(oldValue as Integer?)
                }
                parent.resources.mapUniqueID(newValue as Integer, this)

                if (m_assignments.isEmpty() === false) {
                    for (assignment in m_assignments) {
                        assignment.resourceUniqueID = newValue as Integer
                    }
                }
            }

            ResourceField.COST, ResourceField.BASELINE_COST -> {
                m_array[ResourceField.COST_VARIANCE.value] = null
            }

            ResourceField.WORK, ResourceField.BASELINE_WORK -> {
                m_array[ResourceField.WORK_VARIANCE.value] = null
            }

            ResourceField.BCWP, ResourceField.ACWP -> {
                m_array[ResourceField.CV.value] = null
                m_array[ResourceField.SV.value] = null
            }

            ResourceField.BCWS -> {
                m_array[ResourceField.SV.value] = null
            }

            ResourceField.PEAK, ResourceField.MAX_UNITS -> {
                m_array[ResourceField.OVERALLOCATED.value] = null
            }

            else -> {
            }
        }

        //
        // External event handling
        //
        if (m_listeners != null) {
            for (listener in m_listeners!!) {
                listener.fieldChange(this, field, oldValue, newValue)
            }
        }
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
     * This method is used to set the value of a field in the resource.
     *
     * @param field field to be set
     * @param value new value for field.
     */
    private operator fun set(field: FieldType, value: Boolean) {
        set(field, if (value) Boolean.TRUE else Boolean.FALSE)
    }

    /**
     * Disable events firing when fields are updated.
     */
    fun disableEvents() {
        m_eventsEnabled = false
    }

    /**
     * Enable events firing when fields are updated. This is the default state.
     */
    fun enableEvents() {
        m_eventsEnabled = true
    }

    /**
     * This method implements the only method in the Comparable interface. This
     * allows Resources to be compared and sorted based on their ID value. Note
     * that if the MPX/MPP file has been generated by MSP, the ID value will
     * always be in the correct sequence. The Unique ID value will not
     * necessarily be in the correct sequence as task insertions and deletions
     * will change the order.
     *
     * @param o object to compare this instance with
     * @return result of comparison
     */
    @Override
    fun compareTo(o: Resource): Int {
        val id1 = NumberHelper.getInt(id)
        val id2 = NumberHelper.getInt(o.id)
        return if (id1 < id2) -1 else if (id1 == id2) 0 else 1
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun equals(o: Object): Boolean {
        var result = false
        if (o is Resource) {
            result = compareTo(o as Resource) == 0
        }
        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun hashCode(): Int {
        return NumberHelper.getInt(id)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return "[Resource id=$id uniqueID=$uniqueID name=$name]"
    }
}

/*
NEW FIELDS - to be implemented in 5.0
{ResourceField.Baseline Budget Cost, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(757)},
{ResourceField.Baseline Budget Work, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(756)},
{ResourceField.Baseline1 Budget Cost, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(761)},
{ResourceField.Baseline1 Budget Work, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(760)},
{ResourceField.Baseline10 Budget Cost, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(797)},
{ResourceField.Baseline10 Budget Work, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(796)},
{ResourceField.Baseline2 Budget Cost, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(765)},
{ResourceField.Baseline2 Budget Work, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(764)},
{ResourceField.Baseline3 Budget Cost, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(769)},
{ResourceField.Baseline3 Budget Work, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(768)},
{ResourceField.Baseline4 Budget Cost, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(773)},
{ResourceField.Baseline4 Budget Work, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(772)},
{ResourceField.Baseline5 Budget Cost, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(777)},
{ResourceField.Baseline5 Budget Work, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(776)},
{ResourceField.Baseline6 Budget Cost, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(781)},
{ResourceField.Baseline6 Budget Work, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(780)},
{ResourceField.Baseline7 Budget Cost, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(785)},
{ResourceField.Baseline7 Budget Work, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(784)},
{ResourceField.Baseline8 Budget Cost, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(789)},
{ResourceField.Baseline8 Budget Work, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(788)},
{ResourceField.Baseline9 Budget Cost, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(793)},
{ResourceField.Baseline9 Budget Work, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(792)},
{ResourceField.Booking Type, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(699)},
{ResourceField.Budget Cost, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(754)},
{ResourceField.Budget Work, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(753)},
{ResourceField.Calendar GUID, FieldLocation.FIXED_DATA, Integer.valueOf(24), Integer.valueOf(729)},
{ResourceField.Cost Center, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(801)},
{ResourceField.Enterprise Unique ID, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(443)},
{ResourceField.Phonetics, FieldLocation.VAR_DATA, Integer.valueOf(65535), Integer.valueOf(252)},
{ResourceField.Workgroup, FieldLocation.FIXED_DATA, Integer.valueOf(14), Integer.valueOf(272)},

   INDEX(DataType.INTEGER),
   HYPERLINK_SCREEN_TIP(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE1(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE2(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE3(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE4(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE5(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE6(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE7(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE8(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE9(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE10(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE11(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE12(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE13(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE14(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE15(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE16(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE17(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE18(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE19(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE20(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE21(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE22(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE23(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE24(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE25(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE26(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE27(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE28(DataType.STRING),
   ENTERPRISE_OUTLINE_CODE29(DataType.STRING),
   ENTERPRISE_RBS(DataType.STRING),
   ENTERPRISE_NAME_USED(DataType.STRING),
   ENTERPRISE_IS_CHECKED_OUT(DataType.BOOLEAN),
   ENTERPRISE_CHECKED_OUT_BY(DataType.STRING),
   ENTERPRISE_LAST_MODIFIED_DATE(DataType.DATE),
   ENTERPRISE_MULTI_VALUE20(DataType.STRING),
   ENTERPRISE_MULTI_VALUE21(DataType.STRING),
   ENTERPRISE_MULTI_VALUE22(DataType.STRING),
   ENTERPRISE_MULTI_VALUE23(DataType.STRING),
   ENTERPRISE_MULTI_VALUE24(DataType.STRING),
   ENTERPRISE_MULTI_VALUE25(DataType.STRING),
   ENTERPRISE_MULTI_VALUE26(DataType.STRING),
   ENTERPRISE_MULTI_VALUE27(DataType.STRING),
   ENTERPRISE_MULTI_VALUE28(DataType.STRING),
   ENTERPRISE_MULTI_VALUE29(DataType.STRING),
   ACTUAL_WORK_PROTECTED(DataType.WORK),
   ACTUAL_OVERTIME_WORK_PROTECTED(DataType.WORK),

Actual Overtime Work Protected 65535 721
Actual Work Protected 65535 720
Availability Data 65535 276
Baseline Budget Cost 65535 757
Baseline Budget Work 65535 756
Baseline1 Budget Cost 65535 761
Baseline1 Budget Work 65535 760
Baseline10 Budget Cost 65535 797
Baseline10 Budget Work 65535 796
Baseline2 Budget Cost 65535 765
Baseline2 Budget Work 65535 764
Baseline3 Budget Cost 65535 769
Baseline3 Budget Work 65535 768
Baseline4 Budget Cost 65535 773
Baseline4 Budget Work 65535 772
Baseline5 Budget Cost 65535 777
Baseline5 Budget Work 65535 776
Baseline6 Budget Cost 65535 781
Baseline6 Budget Work 65535 780
Baseline7 Budget Cost 65535 785
Baseline7 Budget Work 65535 784
Baseline8 Budget Cost 65535 789
Baseline8 Budget Work 65535 788
Baseline9 Budget Cost 65535 793
Baseline9 Budget Work 65535 792
Booking Type 65535 699
Budget Cost 65535 754
Budget Work 65535 753
Calendar GUID 24 729
Cost Center 65535 801
Created 65535 726
Enterprise Unique ID 65535 443
Phonetics 65535 252
Remaining Overtime Work 116 40
Work 52 13
Workgroup 14 272
*/