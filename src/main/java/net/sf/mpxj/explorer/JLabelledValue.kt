/*
 * file:       JLabelledValue.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       06/07/2014
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

package net.sf.mpxj.explorer

import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font

import javax.swing.JLabel
import javax.swing.JPanel

/**
 * Implements a simple compound control to display a fixed label and a variable value.
 */
class JLabelledValue
/**
 * Constructor.
 *
 * @param label fixed label text
 */
(label: String) : JPanel() {
    private val m_valueLabel: JLabel

    /**
     * Retrieve the text displayed in the variable label.
     *
     * @return value
     */
    /**
     * Set the text displayed in the variable label.
     *
     * @param value value to be displayed
     */
    var value: String
        get() = m_valueLabel.getText()
        set(value) {
            m_valueLabel.setText(value)
        }

    init {
        val flowLayout = getLayout() as FlowLayout
        flowLayout.setAlignment(FlowLayout.LEFT)
        flowLayout.setVgap(0)
        flowLayout.setHgap(0)
        val textLabel = JLabel(label)
        textLabel.setFont(Font("Tahoma", Font.BOLD, 11))
        textLabel.setPreferredSize(Dimension(70, 14))
        add(textLabel)

        m_valueLabel = JLabel("")
        m_valueLabel.setPreferredSize(Dimension(80, 14))
        add(m_valueLabel)
    }
}
