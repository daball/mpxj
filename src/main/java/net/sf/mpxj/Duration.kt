/*
 * file:       Duration.java
 * author:     Scott Melville
 *             Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
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

import net.sf.mpxj.common.NumberHelper

/**
 * This represents time durations as specified in an MPX file.
 */
class Duration : Comparable<Duration> {

    /**
     * This method is used to retrieve the size of the duration.
     *
     * @return size of the duration
     */
    val duration: Double
        get() = m_duration

    /**
     * This method is used to retrieve the type of units the duration
     * is expressed in. The valid types of units are found in the TimeUnit
     * class.
     *
     * @return type of units
     */
    val units: TimeUnit?
        get() = m_units

    /**
     * Duration amount.
     */
    private var m_duration: Double = 0.toDouble()

    /**
     * Duration type.
     */
    private var m_units: TimeUnit? = null

    /**
     * Constructs an instance of this class from a duration amount and
     * time unit type.
     *
     * @param duration amount of duration
     * @param type time unit of duration
     */
    private constructor(duration: Double, type: TimeUnit) {
        m_duration = duration
        m_units = type
    }

    /**
     * Constructs an instance of this class from a duration amount and
     * time unit type.
     *
     * @param duration amount of duration
     * @param type time unit of duration
     */
    private constructor(duration: Int, type: TimeUnit) {
        m_duration = duration.toDouble()
        m_units = type
    }

    /**
     * This method provides an *approximate* conversion between duration
     * units. It does take into account the project defaults for number of hours
     * in a day and a week, but it does not take account of calendar details.
     * The results obtained from it should therefore be treated with caution.
     *
     * @param type target duration type
     * @param defaults project properties containing default values
     * @return new Duration instance
     */
    fun convertUnits(type: TimeUnit?, defaults: ProjectProperties): Duration {
        return convertUnits(m_duration, m_units, type, defaults)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun equals(o: Object): Boolean {
        var result = false
        if (o is Duration) {
            val rhs = o as Duration
            result = durationComponentEquals(rhs) && m_units === rhs.m_units
        }
        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun hashCode(): Int {
        return m_units!!.getValue() + m_duration.toInt()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun compareTo(rhs: Duration): Int {
        var rhs = rhs
        if (m_units !== rhs.m_units) {
            rhs = convertUnits(rhs.m_duration, rhs.m_units!!, m_units, (8 * 60).toDouble(), (5 * 8 * 60).toDouble(), 20.0)
        }

        return if (durationComponentEquals(rhs)) 0 else if (m_duration < rhs.m_duration) -1 else 1
    }

    /**
     * Equality test for duration component of a Duration instance.
     * Note that this does not take into account the units - use with care!
     *
     * @param rhs duration to compare
     * @return true if duration components are equal, within the allowable delta
     */
    fun durationComponentEquals(rhs: Duration): Boolean {
        return durationValueEquals(m_duration, rhs.m_duration)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return m_duration + m_units!!.toString()
    }

    companion object {

        /**
         * This method provides an *approximate* conversion between duration
         * units. It does take into account the project defaults for number of hours
         * in a day and a week, but it does not take account of calendar details.
         * The results obtained from it should therefore be treated with caution.
         *
         * @param duration duration value
         * @param fromUnits units to convert from
         * @param toUnits units to convert to
         * @param defaults project properties containing default values
         * @return new Duration instance
         */
        fun convertUnits(duration: Double, fromUnits: TimeUnit?, toUnits: TimeUnit?, defaults: ProjectProperties): Duration {
            return convertUnits(duration, fromUnits!!, toUnits, defaults.minutesPerDay.doubleValue(), defaults.minutesPerWeek.doubleValue(), defaults.daysPerMonth.doubleValue())
        }

        /**
         * This method provides an *approximate* conversion between duration
         * units. It does take into account the project defaults for number of hours
         * in a day and a week, but it does not take account of calendar details.
         * The results obtained from it should therefore be treated with caution.
         *
         * @param duration duration value
         * @param fromUnits units to convert from
         * @param toUnits units to convert to
         * @param minutesPerDay number of minutes per day
         * @param minutesPerWeek number of minutes per week
         * @param daysPerMonth number of days per month
         * @return new Duration instance
         */
        fun convertUnits(duration: Double, fromUnits: TimeUnit, toUnits: TimeUnit?, minutesPerDay: Double, minutesPerWeek: Double, daysPerMonth: Double): Duration {
            var duration = duration
            when (fromUnits) {
                YEARS -> {
                    duration *= minutesPerWeek * 52
                }

                ELAPSED_YEARS -> {
                    duration *= (60 * 24 * 7 * 52).toDouble()
                }

                MONTHS -> {
                    duration *= minutesPerDay * daysPerMonth
                }

                ELAPSED_MONTHS -> {
                    duration *= (60 * 24 * 30).toDouble()
                }

                WEEKS -> {
                    duration *= minutesPerWeek
                }

                ELAPSED_WEEKS -> {
                    duration *= (60 * 24 * 7).toDouble()
                }

                DAYS -> {
                    duration *= minutesPerDay
                }

                ELAPSED_DAYS -> {
                    duration *= (60 * 24).toDouble()
                }

                HOURS, ELAPSED_HOURS -> {
                    duration *= 60.0
                }

                else -> {
                }
            }

            if (toUnits !== TimeUnit.MINUTES && toUnits !== TimeUnit.ELAPSED_MINUTES) {
                when (toUnits) {
                    HOURS, ELAPSED_HOURS -> {
                        duration /= 60.0
                    }

                    DAYS -> {
                        if (minutesPerDay != 0.0) {
                            duration /= minutesPerDay
                        } else {
                            duration = 0.0
                        }
                    }

                    ELAPSED_DAYS -> {
                        duration /= (60 * 24).toDouble()
                    }

                    WEEKS -> {
                        if (minutesPerWeek != 0.0) {
                            duration /= minutesPerWeek
                        } else {
                            duration = 0.0
                        }
                    }

                    ELAPSED_WEEKS -> {
                        duration /= (60 * 24 * 7).toDouble()
                    }

                    MONTHS -> {
                        if (minutesPerDay != 0.0 && daysPerMonth != 0.0) {
                            duration /= minutesPerDay * daysPerMonth
                        } else {
                            duration = 0.0
                        }
                    }

                    ELAPSED_MONTHS -> {
                        duration /= (60 * 24 * 30).toDouble()
                    }

                    YEARS -> {
                        if (minutesPerWeek != 0.0) {
                            duration /= minutesPerWeek * 52
                        } else {
                            duration = 0.0
                        }
                    }

                    ELAPSED_YEARS -> {
                        duration /= (60 * 24 * 7 * 52).toDouble()
                    }

                    else -> {
                    }
                }
            }

            return Duration.getInstance(duration, toUnits)
        }

        /**
         * Retrieve an Duration instance. Use shared objects to
         * represent common values for memory efficiency.
         *
         * @param duration duration value
         * @param type duration type
         * @return Duration instance
         */
        fun getInstance(duration: Double, type: TimeUnit?): Duration {
            val result: Duration
            if (duration == 0.0) {
                result = ZERO_DURATIONS[type!!.getValue()]
            } else {
                result = Duration(duration, type)
            }
            return result
        }

        /**
         * Retrieve an Duration instance. Use shared objects to
         * represent common values for memory efficiency.
         *
         * @param duration duration value
         * @param type duration type
         * @return Duration instance
         */
        fun getInstance(duration: Int, type: TimeUnit): Duration {
            val result: Duration
            if (duration == 0) {
                result = ZERO_DURATIONS[type.getValue()]
            } else {
                result = Duration(duration, type)
            }
            return result
        }

        /**
         * Equality test for two duration values.
         *
         * @param lhs duration value
         * @param rhs duration value
         * @return true if duration values are equal, within the allowable delta
         */
        fun durationValueEquals(lhs: Double, rhs: Double): Boolean {
            return NumberHelper.equals(lhs, rhs, 0.00001)
        }

        /**
         * If a and b are not null, returns a new duration of a + b.
         * If a is null and b is not null, returns b.
         * If a is not null and b is null, returns a.
         * If a and b are null, returns null.
         * If needed, b is converted to a's time unit using the project properties.
         *
         * @param a first duration
         * @param b second duration
         * @param defaults project properties containing default values
         * @return a + b
         */
        fun add(a: Duration?, b: Duration?, defaults: ProjectProperties): Duration? {
            var b = b
            if (a == null && b == null) {
                return null
            }
            if (a == null) {
                return b
            }
            if (b == null) {
                return a
            }
            val unit = a.units
            if (b.units !== unit) {
                b = b.convertUnits(unit, defaults)
            }

            return Duration.getInstance(a.duration + b.duration, unit)
        }

        private val ZERO_DURATIONS = arrayOf(Duration(0, TimeUnit.MINUTES), Duration(0, TimeUnit.HOURS), Duration(0, TimeUnit.DAYS), Duration(0, TimeUnit.WEEKS), Duration(0, TimeUnit.MONTHS), Duration(0, TimeUnit.YEARS), Duration(0, TimeUnit.PERCENT), Duration(0, TimeUnit.ELAPSED_MINUTES), Duration(0, TimeUnit.ELAPSED_HOURS), Duration(0, TimeUnit.ELAPSED_DAYS), Duration(0, TimeUnit.ELAPSED_WEEKS), Duration(0, TimeUnit.ELAPSED_MONTHS), Duration(0, TimeUnit.ELAPSED_YEARS), Duration(0, TimeUnit.ELAPSED_PERCENT))
    }
}
