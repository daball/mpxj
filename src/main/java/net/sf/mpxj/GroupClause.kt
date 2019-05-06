/*
 * file:       GroupClause.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       17/01/2007
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

import java.awt.Color

import net.sf.mpxj.mpp.BackgroundPattern
import net.sf.mpxj.mpp.FontStyle

/**
 * This class represents a clause from a definition of a group.
 */
class GroupClause {

    /**
     * Retrieve the grouping field.
     *
     * @return grouping field
     */
    /**
     * Set the grouping field.
     *
     * @param field grouping field
     */
    var field: FieldType? = null
    /**
     * Retrieve a flag indicating that values are grouped
     * in ascending order.
     *
     * @return boolean flag
     */
    /**
     * Sets a flag indicating that values are grouped
     * in ascending order.
     *
     * @param ascending boolean flag
     */
    var ascending: Boolean = false
    /**
     * Retrieve the font.
     *
     * @return font
     */
    /**
     * Retrieve the font.
     *
     * @param font font
     */
    var font: FontStyle? = null
    /**
     * Retrieves the background color.
     *
     * @return background color
     */
    /**
     * Sets the background color.
     *
     * @param color background color.
     */
    var cellBackgroundColor: Color? = null
    /**
     * Retrieves the pattern.
     *
     * @return pattern
     */
    /**
     * Sets the pattern.
     *
     * @param pattern pattern
     */
    var pattern: BackgroundPattern? = null
    /**
     * Retrieves the group on value.
     *
     * @return group on value
     */
    /**
     * Sets the group on value.
     *
     * @param groupOn group on value
     */
    var groupOn: Int = 0 // TODO can we do this as an enumeration?
    /**
     * Retrieves the "start at" value.
     *
     * @return "start at" value
     */
    /**
     * Sets the "start at" value.
     *
     * @param startAt "start at" value
     */
    var startAt: Object? = null
    /**
     * Retrieve the group interval.
     *
     * @return group interval
     */
    /**
     * Sets the group interval.
     *
     * @param groupInterval group interval
     */
    var groupInterval: Object? = null

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        val sb = StringBuilder()
        sb.append("[GroupClause field=")
        sb.append(field)
        sb.append(" ascending=")
        sb.append(ascending)
        sb.append(" font=")
        sb.append(font)
        sb.append(" color=")
        sb.append(cellBackgroundColor)
        sb.append(" pattern=")
        sb.append(pattern)
        sb.append(" groupOn=")
        sb.append(groupOn)
        sb.append(" startAt=")
        sb.append(startAt)
        sb.append(" groupInterval=")
        sb.append(groupInterval)
        return sb.toString()
    }
}
