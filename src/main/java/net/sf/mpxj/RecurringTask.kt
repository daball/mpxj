/*
 * file:       RecurringTask.java
 * author:     Jon Iles
 *             Scott Melville
 * copyright:  (c) Packwood Software 2002-2008
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

import java.io.ByteArrayOutputStream
import java.io.PrintWriter

/**
 * This class represents the Recurring Task Record as found in an MPX file.
 */
class RecurringTask : RecurringData() {

    /**
     * Retrieve the duration of the recurring task.
     *
     * @return duration of recurring task
     */
    /**
     * Set the duration of the recurring task.
     *
     * @param duration duration of the recurring task
     */
    var duration: Duration? = null

    /**
     * {@inheritDoc}
     */
    @Override
    override fun toString(): String {
        val os = ByteArrayOutputStream()
        val pw = PrintWriter(os)
        pw.print("[RecurringTask")
        if (duration != null) {
            pw.print(super.toString())
        }
        pw.println("]")
        pw.flush()
        return os.toString()
    }
}
