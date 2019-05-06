/*
 * file:       CostRateTableEntry.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2009
 * date:       08/06/2009
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

import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper

/**
 * This class represents a row from a resource's cost rate table.
 * Note that MS Project always represents costs as an hourly rate,
 * it holds an additional field to indicate the format used when
 * displaying the rate.
 */
class CostRateTableEntry : Comparable<CostRateTableEntry> {

    /**
     * Retrieves the end date after which this table entry is not valid.
     *
     * @return end date
     */
    var endDate: Date? = null
        private set
    /**
     * Retrieves the standard rate represented by this entry.
     *
     * @return standard rate
     */
    var standardRate: Rate? = null
        private set
    /**
     * Retrieves the format used when displaying the standard rate.
     *
     * @return standard rate format
     */
    var standardRateFormat: TimeUnit? = null
        private set
    /**
     * Retrieves the overtime rate represented by this entry.
     *
     * @return overtime rate
     */
    var overtimeRate: Rate? = null
        private set
    /**
     * Retrieves the format used when displaying the overtime rate.
     *
     * @return overtime rate format
     */
    var overtimeRateFormat: TimeUnit? = null
        private set
    /**
     * Retrieves the cost per use represented by this entry.
     *
     * @return per use rate
     */
    var costPerUse: Number? = null
        private set

    /**
     * Constructor. Used to construct singleton default table entry.
     */
    private constructor() {
        endDate = DateHelper.LAST_DATE
        standardRate = Rate(0, TimeUnit.HOURS)
        standardRateFormat = TimeUnit.HOURS
        overtimeRate = standardRate
        overtimeRateFormat = TimeUnit.HOURS
        costPerUse = NumberHelper.getDouble(0.0)
    }

    /**
     * Constructor.
     *
     * @param standardRate standard rate
     * @param standardRateFormat standard rate format
     * @param overtimeRate overtime rate
     * @param overtimeRateFormat overtime rate format
     * @param costPerUse cost per use
     * @param endDate end date
     */
    constructor(standardRate: Rate, standardRateFormat: TimeUnit, overtimeRate: Rate, overtimeRateFormat: TimeUnit, costPerUse: Number, endDate: Date) {
        this.endDate = endDate
        this.standardRate = standardRate
        this.standardRateFormat = standardRateFormat
        this.overtimeRate = overtimeRate
        this.overtimeRateFormat = overtimeRateFormat
        this.costPerUse = costPerUse
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun compareTo(o: CostRateTableEntry): Int {
        return DateHelper.compare(endDate, o.endDate)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return "[CostRateTableEntry standardRate=$standardRate overtimeRate=$overtimeRate costPerUse=$costPerUse endDate=$endDate]"
    }

    companion object {

        val DEFAULT_ENTRY = CostRateTableEntry()
    }
}
