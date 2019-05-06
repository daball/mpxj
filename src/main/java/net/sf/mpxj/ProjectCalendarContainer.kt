/*
 * file:       ProjectCalendarContainer.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2015
 * date:       20/04/2015
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

/**
 * Manages the collection of calendars belonging to a project.
 */
class ProjectCalendarContainer
/**
 * Constructor.
 *
 * @param projectFile parent project
 */
(projectFile: ProjectFile) : ProjectEntityContainer<ProjectCalendar>(projectFile) {

    @Override
    public override fun removed(calendar: ProjectCalendar) {
        val resource = calendar.resource
        if (resource != null) {
            resource.resourceCalendar = null
        }

        calendar.parent = null
    }

    /**
     * Add a calendar to the project.
     *
     * @return new task instance
     */
    fun add(): ProjectCalendar {
        val calendar = ProjectCalendar(m_projectFile)
        add(calendar)
        return calendar
    }

    /**
     * This is a convenience method used to add a calendar called
     * "Standard" to the project, and populate it with a default working week
     * and default working hours.
     *
     * @return a new default calendar
     */
    fun addDefaultBaseCalendar(): ProjectCalendar {
        val calendar = add()

        calendar.name = ProjectCalendar.DEFAULT_BASE_CALENDAR_NAME

        calendar.setWorkingDay(Day.SUNDAY, false)
        calendar.setWorkingDay(Day.MONDAY, true)
        calendar.setWorkingDay(Day.TUESDAY, true)
        calendar.setWorkingDay(Day.WEDNESDAY, true)
        calendar.setWorkingDay(Day.THURSDAY, true)
        calendar.setWorkingDay(Day.FRIDAY, true)
        calendar.setWorkingDay(Day.SATURDAY, false)

        calendar.addDefaultCalendarHours()

        return calendar
    }

    /**
     * This is a convenience method to add a default derived
     * calendar to the project.
     *
     * @return new ProjectCalendar instance
     */
    fun addDefaultDerivedCalendar(): ProjectCalendar {
        val calendar = add()

        calendar.setWorkingDay(Day.SUNDAY, DayType.DEFAULT)
        calendar.setWorkingDay(Day.MONDAY, DayType.DEFAULT)
        calendar.setWorkingDay(Day.TUESDAY, DayType.DEFAULT)
        calendar.setWorkingDay(Day.WEDNESDAY, DayType.DEFAULT)
        calendar.setWorkingDay(Day.THURSDAY, DayType.DEFAULT)
        calendar.setWorkingDay(Day.FRIDAY, DayType.DEFAULT)
        calendar.setWorkingDay(Day.SATURDAY, DayType.DEFAULT)

        return calendar
    }

    /**
     * Retrieves the named calendar. This method will return
     * null if the named calendar is not located.
     *
     * @param calendarName name of the required calendar
     * @return ProjectCalendar instance
     */
    fun getByName(calendarName: String?): ProjectCalendar? {
        var calendar: ProjectCalendar? = null

        if (calendarName != null && calendarName.length() !== 0) {
            val iter = iterator()
            while (iter.hasNext() === true) {
                calendar = iter.next()
                val name = calendar!!.name

                if (name != null && name.equalsIgnoreCase(calendarName) === true) {
                    break
                }

                calendar = null
            }
        }

        return calendar
    }
}
