/*
 * file:       JTableExtra.java
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

import java.awt.Point
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

import javax.swing.JTable
import javax.swing.SwingUtilities
import javax.swing.table.TableColumnModel
import javax.swing.table.TableModel

/**
 * An extension of JTable which presents the selected cell as an observable property
 * and sets all columns to a fixed width.
 */
class JTableExtra : JTable() {
    /**
     * Retrieve the currently selected cell.
     *
     * @return selected cell
     */
    /**
     * Set the current selected cell.
     *
     * @param selectedCell selected cell
     */
    var selectedCell = Point(-1, -1)
        set(selectedCell) {
            firePropertyChange("selectedCell", this.selectedCell, field = selectedCell)
        }
    /**
     * Retrieves the fixed column width used by all columns in the table.
     *
     * @return column width
     */
    /**
     * Sets the column width used by all columns in the table.
     *
     * @param columnWidth column width
     */
    var columnWidth = 20
        set(columnWidth) {
            firePropertyChange("columnWidth", this.columnWidth, field = columnWidth)
        }

    /**
     * Constructor.
     */
    init {

        //
        // Fire selection event in response to mouse clicks
        //
        addMouseListener(object : MouseAdapter() {
            @Override
            fun mouseClicked(e: MouseEvent) {
                selectedCell = Point(getSelectedColumn(), getSelectedRow())
            }
        })

        //
        // Fire selection event in response to arrow key moving selection
        //
        addKeyListener(object : KeyAdapter() {
            @Override
            fun keyPressed(e: KeyEvent) {
                when (e.getKeyCode()) {
                    KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN -> {
                        SwingUtilities.invokeLater(object : Runnable() {

                            @Override
                            fun run() {
                                selectedCell = Point(getSelectedColumn(), getSelectedRow())
                            }
                        })
                    }
                }
            }
        })
    }

    /**
     * Updates the model. Ensures that we reset the columns widths.
     *
     * @param model table model
     */
    @Override
    fun setModel(model: TableModel) {
        super.setModel(model)
        val columns = model.getColumnCount()
        val tableColumnModel = getColumnModel()
        for (index in 0 until columns) {
            tableColumnModel.getColumn(index).setPreferredWidth(columnWidth)
        }
    }
}
