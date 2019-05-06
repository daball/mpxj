/*
 * file:       GanttBarCommonStyle.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       2005-04-13
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
import java.io.ByteArrayOutputStream
import java.io.PrintWriter

import net.sf.mpxj.TaskField

/**
 * This class represents common elements of the Gantt char bar styles
 * shared between the normal styles, and the individual task bar
 * exception styles.
 */
open class GanttBarCommonStyle {
    /**
     * Retrieve the text appearing at the bottom of the bar.
     *
     * @return bottom text
     */
    /**
     * Sets the text appearing at the bottom of the bar.
     *
     * @param field bottom text
     */
    var bottomText: TaskField?
        get() = m_bottomText
        set(field) {
            m_bottomText = field
        }

    /**
     * Retrieve the color of the end of the bar.
     *
     * @return end color
     */
    /**
     * Sets the color of the end of the bar.
     *
     * @param color end color
     */
    var endColor: Color?
        get() = m_endColor
        set(color) {
            m_endColor = color
        }

    /**
     * Retrieve the text appearing inside the Gantt bar.
     *
     * @return inside text
     */
    /**
     * Sets the text appearing inside the Gantt bar.
     *
     * @param field inside text
     */
    var insideText: TaskField?
        get() = m_insideText
        set(field) {
            m_insideText = field
        }

    /**
     * Retrieve the text appearing to the left of the bar.
     *
     * @return left text
     */
    /**
     * Sets the text appearing to the left of the bar.
     *
     * @param field left text
     */
    var leftText: TaskField?
        get() = m_leftText
        set(field) {
            m_leftText = field
        }

    /**
     * Retrieve the color of the middle section of the bar.
     *
     * @return middle color
     */
    /**
     * Sets the color of the middle section of the bar.
     *
     * @param color middle color
     */
    var middleColor: Color?
        get() = m_middleColor
        set(color) {
            m_middleColor = color
        }

    /**
     * Retrieve the pattern appearing in the middle section of the bar.
     *
     * @return middle pattern
     */
    /**
     * Sets the pattern appearing in the middle section of the bar.
     *
     * @param pattern middle pattern
     */
    var middlePattern: ChartPattern?
        get() = m_middlePattern
        set(pattern) {
            m_middlePattern = pattern
        }

    /**
     * Retrieve the shape of the middle section of the bar.
     *
     * @return middle shape
     */
    /**
     * Sets the shape of the middle section of the bar.
     *
     * @param shape middle shape
     */
    var middleShape: GanttBarMiddleShape?
        get() = m_middleShape
        set(shape) {
            m_middleShape = shape
        }

    /**
     * Retrieve the text appearing to the right of the bar.
     *
     * @return right text
     */
    /**
     * Sets the text appearing to the right of the bar.
     *
     * @param field right text
     */
    var rightText: TaskField?
        get() = m_rightText
        set(field) {
            m_rightText = field
        }

    /**
     * Retrieve the text which appears above the bar.
     *
     * @return top text
     */
    /**
     * Sets the top text.
     *
     * @param field top text
     */
    var topText: TaskField?
        get() = m_topText
        set(field) {
            m_topText = field
        }

    /**
     * Retrieve the bar start shape.
     *
     * @return bar start shape
     */
    /**
     * Sets the bar start shape.
     *
     * @param shape start shape
     */
    var startShape: GanttBarStartEndShape? = null
    /**
     * Retrieve the bar start type.
     *
     * @return bar start type
     */
    /**
     * Sets the bar start type.
     *
     * @param type bar start type
     */
    var startType: GanttBarStartEndType? = null
    /**
     * Retrieve the color of the start of the bar.
     *
     * @return start color
     */
    /**
     * Sets the color of the start of the bar.
     *
     * @param color start color
     */
    var startColor: Color? = null

    private var m_middleShape: GanttBarMiddleShape? = null
    private var m_middlePattern: ChartPattern? = null
    private var m_middleColor: Color? = null

    /**
     * Retrieve the bar end shape.
     *
     * @return bar end shape
     */
    /**
     * Sets the bar end shape.
     *
     * @param shape end shape
     */
    var endShape: GanttBarStartEndShape? = null
    /**
     * Retrieve the bar end type.
     *
     * @return bar end type
     */
    /**
     * Sets the bar end type.
     *
     * @param type bar end type
     */
    var endType: GanttBarStartEndType? = null
    private var m_endColor: Color? = null

    private var m_leftText: TaskField? = null
    private var m_rightText: TaskField? = null
    private var m_topText: TaskField? = null
    private var m_bottomText: TaskField? = null
    private var m_insideText: TaskField? = null

    /**
     * Generate a string representation of this instance.
     *
     * @return string representation of this instance
     */
    @Override
    open fun toString(): String {
        val os = ByteArrayOutputStream()
        val pw = PrintWriter(os)
        pw.println("      StartShape=" + startShape!!)
        pw.println("      StartType=" + startType!!)
        pw.println("      StartColor=" + startColor!!)
        pw.println("      MiddleShape=" + m_middleShape!!)
        pw.println("      MiddlePattern=" + m_middlePattern!!)
        pw.println("      MiddleColor=" + m_middleColor!!)
        pw.println("      EndShape=" + endShape!!)
        pw.println("      EndType=" + endType!!)
        pw.println("      EndColor=" + m_endColor!!)
        pw.println("      LeftText=" + m_leftText!!)
        pw.println("      RightText=" + m_rightText!!)
        pw.println("      TopText=" + m_topText!!)
        pw.println("      BottomText=" + m_bottomText!!)
        pw.println("      InsideText=" + m_insideText!!)
        pw.flush()
        return os.toString()
    }
}
