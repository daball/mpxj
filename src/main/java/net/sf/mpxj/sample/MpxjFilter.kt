/*
 * file:       MpxjFilter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2009
 * date:       03/05/2009
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

package net.sf.mpxj.sample

import net.sf.mpxj.Filter
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Resource
import net.sf.mpxj.Task
import net.sf.mpxj.reader.UniversalProjectReader

/**
 * This example shows tasks or resources being read from a project file,
 * a filter applied to the list, and the results displayed.
 * Executing this utility without a valid filter name will result in
 * the list of available filters being displayed.
 */
object MpxjFilter {
    /**
     * Main method.
     *
     * @param args array of command line arguments
     */
    fun main(args: Array<String>) {
        try {
            if (args.size != 2) {
                System.out.println("Usage: MpxFilter <input file name> <filter name>")
            } else {
                filter(args[0], args[1])
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.out)
        }

    }

    /**
     * This method opens the named project, applies the named filter
     * and displays the filtered list of tasks or resources. If an
     * invalid filter name is supplied, a list of valid filter names
     * is shown.
     *
     * @param filename input file name
     * @param filtername input filter name
     */
    @Throws(Exception::class)
    private fun filter(filename: String, filtername: String) {
        val project = UniversalProjectReader().read(filename)
        val filter = project!!.filters.getFilterByName(filtername)

        if (filter == null) {
            displayAvailableFilters(project!!)
        } else {
            System.out.println(filter)
            System.out.println()

            if (filter!!.isTaskFilter()) {
                processTaskFilter(project!!, filter)
            } else {
                processResourceFilter(project!!, filter)
            }
        }
    }

    /**
     * This utility displays a list of available task filters, and a
     * list of available resource filters.
     *
     * @param project project file
     */
    private fun displayAvailableFilters(project: ProjectFile) {
        System.out.println("Unknown filter name supplied.")
        System.out.println("Available task filters:")
        for (filter in project.filters.getTaskFilters()) {
            System.out.println("   " + filter.getName())
        }

        System.out.println("Available resource filters:")
        for (filter in project.filters.getResourceFilters()) {
            System.out.println("   " + filter.getName())
        }

    }

    /**
     * Apply a filter to the list of all tasks, and show the results.
     *
     * @param project project file
     * @param filter filter
     */
    private fun processTaskFilter(project: ProjectFile, filter: Filter) {
        for (task in project.tasks) {
            if (filter.evaluate(task, null)) {
                System.out.println(task.id + "," + task.uniqueID + "," + task.name)
            }
        }
    }

    /**
     * Apply a filter to the list of all resources, and show the results.
     *
     * @param project project file
     * @param filter filter
     */
    private fun processResourceFilter(project: ProjectFile, filter: Filter) {
        for (resource in project.resources) {
            if (filter.evaluate(resource, null)) {
                System.out.println(resource.id + "," + resource.uniqueID + "," + resource.name)
            }
        }
    }

}
