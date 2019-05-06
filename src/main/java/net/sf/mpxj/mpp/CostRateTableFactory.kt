/*
 * file:       CostRateTableFactory.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2009
 * date:       09/06/2009
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

import java.util.Collections
import java.util.Date

import net.sf.mpxj.CostRateTable
import net.sf.mpxj.CostRateTableEntry
import net.sf.mpxj.Rate
import net.sf.mpxj.Resource
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.NumberHelper

/**
 * Common code to read resource cost rate tables from MPP files.
 */
internal class CostRateTableFactory {
    /**
     * Creates a CostRateTable instance from a block of data.
     *
     * @param resource parent resource
     * @param index cost rate table index
     * @param data data block
     */
    fun process(resource: Resource, index: Int, data: ByteArray?) {
        val result = CostRateTable()

        if (data != null) {
            var i = 16
            while (i + 44 <= data.size) {
                val standardRate = Rate(MPPUtility.getDouble(data, i), TimeUnit.HOURS)
                val standardRateFormat = getFormat(MPPUtility.getShort(data, i + 8))
                val overtimeRate = Rate(MPPUtility.getDouble(data, i + 16), TimeUnit.HOURS)
                val overtimeRateFormat = getFormat(MPPUtility.getShort(data, i + 24))
                val costPerUse = NumberHelper.getDouble(MPPUtility.getDouble(data, i + 32) / 100.0)
                val endDate = MPPUtility.getTimestampFromTenths(data, i + 40)
                val entry = CostRateTableEntry(standardRate, standardRateFormat, overtimeRate, overtimeRateFormat, costPerUse, endDate)
                result.add(entry)
                i += 44
            }
            Collections.sort(result)
        } else {
            //
            // MS Project economises by not actually storing the first cost rate
            // table if it doesn't need to, so we take this into account here.
            //
            if (index == 0) {
                val standardRate = resource.standardRate
                val overtimeRate = resource.overtimeRate
                val costPerUse = resource.costPerUse
                val entry = CostRateTableEntry(standardRate, standardRate.units, overtimeRate, overtimeRate.units, costPerUse, CostRateTableEntry.DEFAULT_ENTRY.getEndDate())
                result.add(entry)
            } else {
                result.add(CostRateTableEntry.DEFAULT_ENTRY)
            }
        }

        resource.setCostRateTable(index, result)
    }

    /**
     * Converts an integer into a time format.
     *
     * @param format integer format value
     * @return TimeUnit instance
     */
    private fun getFormat(format: Int): TimeUnit {
        val result: TimeUnit
        if (format == 0xFFFF) {
            result = TimeUnit.HOURS
        } else {
            result = MPPUtility.getWorkTimeUnits(format)
        }
        return result
    }
}
