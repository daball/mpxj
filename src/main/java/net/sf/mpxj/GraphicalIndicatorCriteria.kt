/*
 * file:       GraphicalIndicatorCriteria.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       15/02/2006
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
 * This class represents the criteria used to determine if a graphical
 * indicator is displayed in place of an attribute value.
 */
class GraphicalIndicatorCriteria
/**
 * Constructor.
 *
 * @param properties project properties
 */
(properties: ProjectProperties) : GenericCriteria(properties) {

    /**
     * Retrieve the number of the indicator to be displayed.
     *
     * @return indicator number
     */
    /**
     * Set the number of the indicator to be displayed.
     *
     * @param indicator indicator number
     */
    var indicator: Int = 0

    /**
     * Evaluate this criteria to determine if a graphical indicator should
     * be displayed. This method will return -1 if no indicator should
     * be displayed, or it will return a positive integer identifying the
     * required indicator.
     *
     * @param container field container
     * @return boolean flag
     */
    fun evaluate(container: FieldContainer): Int {
        return if (evaluate(container, null)) indicator else -1
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        val sb = StringBuilder()
        sb.append("[GraphicalIndicatorCriteria indicator=")
        sb.append(indicator)
        sb.append(" criteria=")
        sb.append(super.toString())
        sb.append("]")
        return sb.toString()
    }
}
