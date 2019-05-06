/*
 * file:       HexDumpController.java
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
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.IOException
import java.io.InputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel

import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream

import net.sf.mpxj.Duration
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.StreamHelper
import net.sf.mpxj.mpp.MPPUtility

/**
 * Implements the controller component of the HexDump MVC.
 */
class HexDumpController
/**
 * Constructor.
 *
 * @param model HexDump model
 */
(private val m_model: HexDumpModel) {

    init {

        m_model.addPropertyChangeListener("columns", object : PropertyChangeListener() {
            @Override
            fun propertyChange(evt: PropertyChangeEvent) {
                updateTables()
            }
        })

        m_model.addPropertyChangeListener("offset", object : PropertyChangeListener() {
            @Override
            fun propertyChange(evt: PropertyChangeEvent) {
                updateTables()
            }
        })

        m_model.addPropertyChangeListener("selectedCell", object : PropertyChangeListener() {
            @Override
            fun propertyChange(evt: PropertyChangeEvent) {
                updateSelection()
            }
        })
    }

    /**
     * Command to select a document from the POIFS for viewing.
     *
     * @param entry document to view
     */
    fun viewDocument(entry: DocumentEntry) {
        var `is`: InputStream? = null

        try {
            `is` = DocumentInputStream(entry)
            val data = ByteArray(`is`!!.available())
            `is`!!.read(data)
            m_model.data = data
            updateTables()
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        } finally {
            StreamHelper.closeQuietly(`is`)
        }

    }

    /**
     * Update the content of the tables.
     */
    protected fun updateTables() {
        val data = m_model.data
        val columns = m_model.columns
        val rows = data.size / columns + 1
        val offset = m_model.offset

        val hexData = Array<Array<String>>(rows) { arrayOfNulls(columns) }
        val asciiData = Array<Array<String>>(rows) { arrayOfNulls(columns) }

        var row = 0
        var column = 0
        val hexValue = StringBuilder()
        for (index in offset until data.size) {
            val value = data[index].toInt()
            hexValue.setLength(0)
            hexValue.append(HEX_DIGITS[value and 0xF0 shr 4])
            hexValue.append(HEX_DIGITS[value and 0x0F])

            var c = value.toChar()
            if (c.toInt() > 200 || c.toInt() < 27) {
                c = ' '
            }

            hexData[row][column] = hexValue.toString()
            asciiData[row][column] = Character.toString(c)

            ++column
            if (column == columns) {
                column = 0
                ++row
            }
        }

        val columnHeadings = arrayOfNulls<String>(columns)
        val hexTableModel = object : DefaultTableModel(hexData, columnHeadings) {
            @Override
            fun isCellEditable(r: Int, c: Int): Boolean {
                return false
            }
        }

        val asciiTableModel = object : DefaultTableModel(asciiData, columnHeadings) {
            @Override
            fun isCellEditable(r: Int, c: Int): Boolean {
                return false
            }
        }

        m_model.sizeValueLabel = Integer.toString(data.size)
        m_model.hexTableModel = hexTableModel
        m_model.asciiTableModel = asciiTableModel
        m_model.currentSelectionIndex = 0
        m_model.previousSelectionIndex = 0
    }

    /**
     * Updates the selection information.
     */
    protected fun updateSelection() {
        val data = m_model.data
        val offset = m_model.offset
        val selectedCell = m_model.selectedCell

        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val timeFormat = SimpleDateFormat("HH:mm")
        val timestampFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")

        val selectionIndex = selectedCell!!.y * m_model.columns + selectedCell.x
        val selectionLabel = selectedCell.y + "," + selectedCell.x
        val differenceLabel = Integer.toString(Math.abs(m_model.currentSelectionIndex - selectionIndex))

        var shortValueLabel = ""
        var longSixValueLabel = ""
        var longValueLabel = ""
        var doubleValueLabel = ""
        var durationValueLabel = ""
        var timeUnitsValueLabel = ""
        var guidValueLabel = ""
        var percentageValueLabel = ""
        var dateValueLabel = ""
        var timeValueLabel = ""
        var timestampValueLabel = ""
        var workUnitsValueLabel = ""

        if (selectionIndex + offset + 2 <= data.size) {
            shortValueLabel = Integer.toString(MPPUtility.getShort(data, selectionIndex + offset))
            timeUnitsValueLabel = MPPUtility.getDurationTimeUnits(MPPUtility.getShort(data, selectionIndex + offset)).toString()

            val value = MPPUtility.getPercentage(data, selectionIndex + offset)
            if (value != null) {
                percentageValueLabel = value.toString()
            }

            val date = MPPUtility.getDate(data, selectionIndex + offset)
            if (date != null) {
                dateValueLabel = dateFormat.format(date)
            }

            timeValueLabel = timeFormat.format(MPPUtility.getTime(data, selectionIndex + offset))
        }

        //
        // 1 byte
        //
        workUnitsValueLabel = MPPUtility.getWorkTimeUnits(MPPUtility.getByte(data, selectionIndex + offset)).toString()

        //
        // 4 bytes
        //
        if (selectionIndex + offset + 4 <= data.size) {
            val timestamp = MPPUtility.getTimestamp(data, selectionIndex + offset)
            if (timestamp != null) {
                timestampValueLabel = timestampFormat.format(timestamp)
            }
        }

        //
        // 6 bytes
        //
        if (selectionIndex + offset + 6 <= data.size) {
            longSixValueLabel = Long.toString(MPPUtility.getLong6(data, selectionIndex + offset))
        }

        //
        // 8 bytes
        //
        if (selectionIndex + offset + 8 <= data.size) {
            longValueLabel = Long.toString(MPPUtility.getLong(data, selectionIndex + offset))
            doubleValueLabel = Double.toString(MPPUtility.getDouble(data, selectionIndex + offset))
            durationValueLabel = Duration.getInstance(MPPUtility.getDouble(data, selectionIndex + offset) / 60000, TimeUnit.HOURS).toString()
        }

        //
        // 16 bytes
        //
        if (selectionIndex + offset + 16 <= data.size) {
            guidValueLabel = MPPUtility.getGUID(data, selectionIndex + offset)!!.toString().toUpperCase()
        }

        m_model.previousSelectionIndex = m_model.currentSelectionIndex
        m_model.currentSelectionIndex = selectionIndex
        m_model.previousSelectionValueLabel = m_model.currentSelectionValueLabel
        m_model.currentSelectionValueLabel = selectionLabel
        m_model.selectionDifferenceValueLabel = differenceLabel
        m_model.shortValueLabel = shortValueLabel
        m_model.longSixValueLabel = longSixValueLabel
        m_model.longValueLabel = longValueLabel
        m_model.doubleValueLabel = doubleValueLabel
        m_model.durationValueLabel = durationValueLabel
        m_model.timeUnitsValueLabel = timeUnitsValueLabel
        m_model.guidValueLabel = guidValueLabel
        m_model.percentageValueLabel = percentageValueLabel
        m_model.dateValueLabel = dateValueLabel
        m_model.timeValueLabel = timeValueLabel
        m_model.timestampValueLabel = timestampValueLabel
        m_model.workUnitsValueLabel = workUnitsValueLabel
    }

    companion object {
        private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    }
}
