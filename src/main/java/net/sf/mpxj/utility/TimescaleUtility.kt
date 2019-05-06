/*
 * file:       TimescaleUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       2011-02-12
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

package net.sf.mpxj.utility

import java.util.ArrayList
import java.util.Calendar
import java.util.Date

import net.sf.mpxj.DateRange
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.mpp.TimescaleUnits

/**
 * This class contains methods related to creation of timescale data.
 */
class TimescaleUtility {

    /**
     * Retrieves the day on which the week starts.
     *
     * @return week start day
     */
    /**
     * Set the day on which the week starts. Defaults to Calendar.MONDAY.
     *
     * @param weekStartDay week start day
     */
    var weekStartDay = Calendar.MONDAY

    /**
     * Given a start date, a timescale unit, and a number of segments, this
     * method creates an array of date ranges. For example, if "Months" is
     * selected as the timescale units, this method will create an array of
     * ranges, each one representing a month. The number of entries in the
     * array is determined by the segment count.
     *
     * Each of these date ranges is equivalent one of the columns displayed by
     * MS Project when viewing data with a "timescale" at the top of the page.
     *
     * @param startDate start date
     * @param segmentUnit units to be represented by each segment (column)
     * @param segmentCount number of segments (columns) required
     * @return array of date ranges
     */
    fun createTimescale(startDate: Date, segmentUnit: TimescaleUnits, segmentCount: Int): ArrayList<DateRange> {
        val result = ArrayList<DateRange>(segmentCount)

        val cal = DateHelper.popCalendar(startDate)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val calendarIncrementUnit: Int
        val calendarIncrementAmount: Int

        when (segmentUnit) {
            TimescaleUnits.MINUTES -> {
                calendarIncrementUnit = Calendar.MINUTE
                calendarIncrementAmount = 1
            }

            TimescaleUnits.HOURS -> {
                calendarIncrementUnit = Calendar.HOUR_OF_DAY
                calendarIncrementAmount = 1
            }

            TimescaleUnits.WEEKS -> {
                cal.set(Calendar.DAY_OF_WEEK, weekStartDay)
                calendarIncrementUnit = Calendar.DAY_OF_YEAR
                calendarIncrementAmount = 7
            }

            TimescaleUnits.THIRDS_OF_MONTHS -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                calendarIncrementUnit = Calendar.DAY_OF_YEAR
                calendarIncrementAmount = 10
            }

            TimescaleUnits.MONTHS -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                calendarIncrementUnit = Calendar.MONTH
                calendarIncrementAmount = 1
            }

            TimescaleUnits.QUARTERS -> {
                val currentMonth = cal.get(Calendar.MONTH)
                val currentQuarter = currentMonth / 3
                val startMonth = currentQuarter * 3
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.MONTH, startMonth)
                calendarIncrementUnit = Calendar.MONTH
                calendarIncrementAmount = 3
            }

            TimescaleUnits.HALF_YEARS // align to jan, jun
            -> {
                val currentMonth = cal.get(Calendar.MONTH)
                val currentHalf = currentMonth / 6
                val startMonth = currentHalf * 6
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.MONTH, startMonth)
                calendarIncrementUnit = Calendar.MONTH
                calendarIncrementAmount = 6
            }

            TimescaleUnits.YEARS // align to 1 jan
            -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                calendarIncrementUnit = Calendar.YEAR
                calendarIncrementAmount = 1
            }
            TimescaleUnits.DAYS -> {
                calendarIncrementUnit = Calendar.DAY_OF_YEAR
                calendarIncrementAmount = 1
            }

            else -> {
                calendarIncrementUnit = Calendar.DAY_OF_YEAR
                calendarIncrementAmount = 1
            }
        }

        for (loop in 0 until segmentCount) {
            val rangeStart = cal.getTime()

            if (segmentUnit == TimescaleUnits.THIRDS_OF_MONTHS && (loop + 1) % 3 == 0) {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.add(Calendar.MONTH, 1)
            } else {
                cal.add(calendarIncrementUnit, calendarIncrementAmount)
            }

            cal.add(Calendar.MILLISECOND, -1)
            result.add(DateRange(rangeStart, cal.getTime()))
            cal.add(Calendar.MILLISECOND, 1)
        }

        DateHelper.pushCalendar(cal)

        return result
    }
}
