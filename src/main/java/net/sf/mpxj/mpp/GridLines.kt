/*
 * file:       GridLines.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Apr 7, 2005
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

import java.awt.Color

/**
 * This class represents the set of properties that define the position
 * and appearance of a set of grid lines.
 */
class GridLines
/**
 * Constructor.
 *
 * @param normalLineColor normal line color
 * @param normalLineStyle normal line style
 * @param intervalNumber interval number
 * @param intervalLineStyle interval line style
 * @param intervalLineColor interval line color
 */
(private val m_normalLineColor: Color,
 /**
  * Retrieve the normal line style.
  *
  * @return line style
  */
 val normalLineStyle: LineStyle, private val m_intervalNumber: Int, private val m_intervalLineStyle: LineStyle, private val m_intervalLineColor: Color) {

    /**
     * Retrieve the interval line color.
     *
     * @return interval line color
     */
    val intervalLineColor: Color
        get() = m_intervalLineColor

    /**
     * Retrieve the interval line style.
     *
     * @return interval line style
     */
    val intervalLineStyle: LineStyle
        get() = m_intervalLineStyle

    /**
     * Retrieve the interval number.
     *
     * @return interval number
     */
    val intervalNumber: Int
        get() = m_intervalNumber

    /**
     * Retrieve the normal line color.
     *
     * @return line color
     */
    val normalLineColor: Color
        get() = m_normalLineColor

    /**
     * Generate a string representation of this instance.
     *
     * @return string representation of this instance
     */
    @Override
    fun toString(): String {
        return "[GridLines NormalLineColor=$m_normalLineColor NormalLineStyle=$normalLineStyle IntervalNumber=$m_intervalNumber IntervalLineStyle=$m_intervalLineStyle IntervalLineColor=$m_intervalLineColor]"
    }
}
