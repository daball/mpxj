/*
 * file:       HexDumpModel.java
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
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport

import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel

/**
 * Implements the model component of the HexDump MVC.
 */
class HexDumpModel {
    private val m_changeSupport = PropertyChangeSupport(this)
    /**
     * Retrieve the contents of the file represented as a byte array.
     *
     * @return file contents
     */
    /**
     * Set the contents of the file represented as a byte array.
     *
     * @param data file contents
     */
    var data = ByteArray(0)
        set(data) {
            m_changeSupport.firePropertyChange("data", this.data, field = data)
        }
    /**
     * Retrieve the model used by the hex table.
     *
     * @return table model
     */
    /**
     * Set the model used by the hex table.
     *
     * @param tableModel table model
     */
    var hexTableModel: TableModel = DefaultTableModel()
        set(tableModel) {
            m_changeSupport.firePropertyChange("hexTableModel", hexTableModel, field = tableModel)
        }
    /**
     * Retrieve the table used by the ASCII model.
     *
     * @return table model
     */
    /**
     * Set the model used by the ASCII table.
     *
     * @param tableModel table model
     */
    var asciiTableModel: TableModel = DefaultTableModel()
        set(tableModel) {
            m_changeSupport.firePropertyChange("asciiTableModel", asciiTableModel, field = tableModel)
        }
    /**
     * Retrieve the number of columns in the view tables.
     *
     * @return number of columns
     */
    /**
     * Set the number of columns in the view tables.
     *
     * @param columns number of columns
     */
    var columns = 16
        set(columns) {
            m_changeSupport.firePropertyChange("columns", this.columns, field = columns)
        }
    /**
     * Retrieve the offset into the file at which the display starts.
     *
     * @return offset
     */
    /**
     * Set the offset into the file at which the display starts.
     *
     * @param offset offset
     */
    var offset: Int = 0
        set(offset) {
            m_changeSupport.firePropertyChange("offset", this.offset, field = offset)
        }
    /**
     * Retrieve the file size.
     *
     * @return file size
     */
    /**
     * Set the file size.
     *
     * @param size file size
     */
    var sizeValueLabel: String? = null
        set(size) {
            m_changeSupport.firePropertyChange("sizeValueLabel", sizeValueLabel, field = size)
        }
    /**
     * Retrieve the index of the currently selected byte in the hex or ASCII table.
     *
     * @return currently selected byte index
     */
    /**
     * Set the index of the currently selected byte in the hex or ASCII table.
     *
     * @param currentSelectionIndex currently selected byte index
     */
    var currentSelectionIndex: Int = 0
        set(currentSelectionIndex) {
            m_changeSupport.firePropertyChange("currentSelectionIndex", this.currentSelectionIndex, field = currentSelectionIndex)
        }
    /**
     * Retrieve the index of the previously selected byte in the hex or ASCII table.
     *
     * @return previously selected byte index
     */
    /**
     * Set the index of the previously selected byte in the hex or ASCII table.
     *
     * @param previousSelectionIndex previously selected byte index
     */
    var previousSelectionIndex: Int = 0
        set(previousSelectionIndex) {
            m_changeSupport.firePropertyChange("previousSelectionIndex", this.previousSelectionIndex, field = previousSelectionIndex)
        }
    /**
     * Retrieve a description of the previously selected index in the hex or ASCII table.
     *
     * @return previously selected index description
     */
    /**
     * Set a description of the previously selected index in the hex or ASCII table.
     *
     * @param previousSelectionValueLabel previously selected index description
     */
    var previousSelectionValueLabel: String? = null
        set(previousSelectionValueLabel) {
            m_changeSupport.firePropertyChange("previousSelectionValueLabel", this.previousSelectionValueLabel, field = previousSelectionValueLabel)
        }
    /**
     * Retrieve the description of the current hex and ASCII table selection.
     *
     * @return current selection description
     */
    /**
     * Set the description of the current hex and ASCII table selection.
     *
     * @param currentSelectionValueLabel current selection description
     */
    var currentSelectionValueLabel: String? = null
        set(currentSelectionValueLabel) {
            m_changeSupport.firePropertyChange("currentSelectionValueLabel", this.currentSelectionValueLabel, field = currentSelectionValueLabel)
        }
    /**
     * Retrieve the difference in bytes between the previous and the current selection.
     *
     * @return difference in bytes
     */
    /**
     * Set the difference in bytes between the previous and the current selection.
     *
     * @param selectionDifferenceValueLabel difference in bytes
     */
    var selectionDifferenceValueLabel: String? = null
        set(selectionDifferenceValueLabel) {
            m_changeSupport.firePropertyChange("selectionDifferenceValueLabel", this.selectionDifferenceValueLabel, field = selectionDifferenceValueLabel)
        }
    /**
     * Retrieve the current table selection as a row and column point.
     *
     * @return current table selection
     */
    /**
     * Set the current table selection as a row and column point.
     *
     * @param selectedCell current table selection
     */
    var selectedCell: Point? = null
        set(selectedCell) {
            m_changeSupport.firePropertyChange("selectedCell", this.selectedCell, field = selectedCell)
        }
    /**
     * Retrieve the value from the currently selected byte as a short int.
     *
     * @return short value
     */
    /**
     * Set the value from the currently selected byte as a short int.
     *
     * @param shortValueLabel short value
     */
    var shortValueLabel: String? = null
        set(shortValueLabel) {
            m_changeSupport.firePropertyChange("shortValueLabel", this.shortValueLabel, field = shortValueLabel)
        }
    /**
     * Retrieve the value from the currently selected byte as a six byte long.
     *
     * @return long6 value
     */
    /**
     * Set the value from the currently selected byte as a six byte long.
     *
     * @param longSixValueLabel long6 value
     */
    var longSixValueLabel: String? = null
        set(longSixValueLabel) {
            m_changeSupport.firePropertyChange("longSixValueLabel", this.longSixValueLabel, field = longSixValueLabel)
        }
    /**
     * Retrieve the value from the currently selected byte as a long int.
     *
     * @return long int value
     */
    /**
     * Set the value from the currently selected byte as a long int.
     *
     * @param longValueLabel long int value
     */
    var longValueLabel: String? = null
        set(longValueLabel) {
            m_changeSupport.firePropertyChange("longValueLabel", this.longValueLabel, field = longValueLabel)
        }
    /**
     * Retrieve the value from the currently selected byte as a double.
     *
     * @return double value
     */
    /**
     * Set the value from the currently selected byte as a double.
     *
     * @param doubleValueLabel double value
     */
    var doubleValueLabel: String? = null
        set(doubleValueLabel) {
            m_changeSupport.firePropertyChange("doubleValueLabel", this.doubleValueLabel, field = doubleValueLabel)
        }
    /**
     * Retrieve the value from the currently selected byte as a duration.
     *
     * @return duration value
     */
    /**
     * Set the value from the currently selected byte as a duration.
     *
     * @param durationValueLabel duration value
     */
    var durationValueLabel: String? = null
        set(durationValueLabel) {
            m_changeSupport.firePropertyChange("durationValueLabel", this.durationValueLabel, field = durationValueLabel)
        }
    /**
     * Retrieve the value from the currently selected byte as a time unit.
     *
     * @return time unit value
     */
    /**
     * Set the value from the currently selected byte as a time unit.
     *
     * @param timeUnitsValueLabel time unit value
     */
    var timeUnitsValueLabel: String? = null
        set(timeUnitsValueLabel) {
            m_changeSupport.firePropertyChange("timeUnitsValueLabel", this.timeUnitsValueLabel, field = timeUnitsValueLabel)
        }
    /**
     * Retrieve the value from the currently selected byte as a GUID.
     *
     * @return GUID value
     */
    /**
     * Set the value from the currently selected byte as a GUID.
     *
     * @param guidValueLabel GUID value
     */
    var guidValueLabel: String? = null
        set(guidValueLabel) {
            m_changeSupport.firePropertyChange("guidValueLabel", this.guidValueLabel, field = guidValueLabel)
        }
    /**
     * Retrieve the value from the currently selected byte as a percentage.
     *
     * @return percentage value
     */
    /**
     * Set the value from the currently selected byte as a percentage.
     *
     * @param percentageValueLabel percentage value
     */
    var percentageValueLabel: String? = null
        set(percentageValueLabel) {
            m_changeSupport.firePropertyChange("percentageValueLabel", this.percentageValueLabel, field = percentageValueLabel)
        }
    /**
     * Retrieve the value from the currently selected byte as a date.
     *
     * @return date value
     */
    /**
     * Set the value from the currently selected byte as a date.
     *
     * @param dateValueLabel date value
     */
    var dateValueLabel: String? = null
        set(dateValueLabel) {
            m_changeSupport.firePropertyChange("dateValueLabel", this.dateValueLabel, field = dateValueLabel)
        }
    /**
     * Retrieve the value from the currently selected byte as a time.
     *
     * @return time value
     */
    /**
     * Set the value from the currently selected byte as a time.
     *
     * @param timeValueLabel time value
     */
    var timeValueLabel: String? = null
        set(timeValueLabel) {
            m_changeSupport.firePropertyChange("timeValueLabel", this.timeValueLabel, field = timeValueLabel)
        }
    /**
     * Retrieve the value from the currently selected byte as a timestamp.
     *
     * @return timestamp value
     */
    /**
     * Set the value from the currently selected byte as a timestamp.
     *
     * @param timestampValueLabel timestamp value
     */
    var timestampValueLabel: String? = null
        set(timestampValueLabel) {
            m_changeSupport.firePropertyChange("timestampValueLabel", this.timestampValueLabel, field = timestampValueLabel)
        }
    /**
     * Retrieve the value from the currently selected byte as work time units.
     *
     * @return work time units
     */
    /**
     * set the value from the currently selected byte as work time units.
     *
     * @param workUnitsValueLabel work time units
     */
    var workUnitsValueLabel: String? = null
        set(workUnitsValueLabel) {
            m_changeSupport.firePropertyChange("workUnitsValueLabel", this.workUnitsValueLabel, field = workUnitsValueLabel)
        }

    /**
     * Add a property change listener.
     *
     * @param listener property change listener
     */
    fun addPropertyChangeListener(listener: PropertyChangeListener) {
        m_changeSupport.addPropertyChangeListener(listener)
    }

    /**
     * Add a property change listener for a named property.
     *
     * @param propertyName property name
     * @param listener listener
     */
    fun addPropertyChangeListener(propertyName: String, listener: PropertyChangeListener) {
        m_changeSupport.addPropertyChangeListener(propertyName, listener)
    }

    /**
     * Remove a property change listener.
     *
     * @param listener property change listener
     */
    fun removePropertyChangeListener(listener: PropertyChangeListener) {
        m_changeSupport.removePropertyChangeListener(listener)
    }
}
