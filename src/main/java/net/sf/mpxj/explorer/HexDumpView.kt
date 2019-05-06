/*
 * file:       HexDumpView.java
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

import java.awt.Color
import java.awt.FlowLayout
import java.text.NumberFormat

import javax.swing.BoxLayout
import javax.swing.JFormattedTextField
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.SpringLayout
import javax.swing.SwingUtilities
import javax.swing.border.LineBorder
import javax.swing.text.NumberFormatter

import com.jgoodies.binding.adapter.Bindings
import com.jgoodies.binding.beans.BeanAdapter
import com.jgoodies.binding.beans.PropertyConnector

/**
 * Implements the view component of the HexDump MVC.
 */
class HexDumpView
/**
 * Constructor.
 *
 * @param model HexDump model
 */
(model: HexDumpModel) : JPanel() {
    init {
        val integerFormat = NumberFormatter(NumberFormat.getIntegerInstance())
        integerFormat.setValueClass(Integer::class.java)
        val springLayout = SpringLayout()
        setLayout(springLayout)

        //
        // Controls panel
        //
        val controlsPanel = JPanel()
        springLayout.putConstraint(SpringLayout.NORTH, controlsPanel, 0, SpringLayout.NORTH, this)
        springLayout.putConstraint(SpringLayout.WEST, controlsPanel, 0, SpringLayout.WEST, this)
        springLayout.putConstraint(SpringLayout.EAST, controlsPanel, 0, SpringLayout.EAST, this)
        val flowLayout = controlsPanel.getLayout() as FlowLayout
        flowLayout.setAlignment(FlowLayout.LEFT)
        add(controlsPanel)

        val columnsLabel = JLabel("Columns")
        controlsPanel.add(columnsLabel)

        val columns = JFormattedTextField(integerFormat)
        controlsPanel.add(columns)
        columns.setBorder(LineBorder(Color(171, 173, 179)))
        columns.setColumns(10)

        val offsetLabel = JLabel("Offset")
        controlsPanel.add(offsetLabel)

        val offset = JFormattedTextField(integerFormat)
        controlsPanel.add(offset)
        offset.setBorder(LineBorder(Color(171, 173, 179)))
        offset.setColumns(10)

        //
        // Table panel
        //
        val tablePanel = JTablePanel()
        val infoPanel = JPanel()
        val infoPanelLayout = FlowLayout(FlowLayout.LEFT, 5, 5)
        infoPanel.setLayout(infoPanelLayout)
        infoPanel.setBorder(null)

        //
        // Selection data
        //
        val infoPanelSelection = JPanel()
        infoPanel.add(infoPanelSelection)
        infoPanelSelection.setLayout(BoxLayout(infoPanelSelection, BoxLayout.Y_AXIS))

        val sizeValueLabel = JLabelledValue("Size:")
        infoPanelSelection.add(sizeValueLabel)

        val currentSelectionValueLabel = JLabelledValue("Current Selection:")
        infoPanelSelection.add(currentSelectionValueLabel)

        val previousSelectionValueLabel = JLabelledValue("Previous Selection:")
        infoPanelSelection.add(previousSelectionValueLabel)

        val differenceValueLabel = JLabelledValue("Difference:")
        infoPanelSelection.add(differenceValueLabel)

        //
        // Numeric data
        //
        val infoPanelNumeric = JPanel()
        infoPanel.add(infoPanelNumeric)
        infoPanelNumeric.setLayout(BoxLayout(infoPanelNumeric, BoxLayout.Y_AXIS))

        val shortValueLabel = JLabelledValue("Short:")
        infoPanelNumeric.add(shortValueLabel)

        val longSixValueLabel = JLabelledValue("Long6:")
        infoPanelNumeric.add(longSixValueLabel)

        val longValueLabel = JLabelledValue("Long:")
        infoPanelNumeric.add(longValueLabel)

        val doubleValueLabel = JLabelledValue("Double:")
        infoPanelNumeric.add(doubleValueLabel)

        //
        // Duration data
        //
        val infoPanelDuration = JPanel()
        infoPanel.add(infoPanelDuration)
        infoPanelDuration.setLayout(BoxLayout(infoPanelDuration, BoxLayout.Y_AXIS))

        val durationValueLabel = JLabelledValue("Duration:")
        infoPanelDuration.add(durationValueLabel)

        val timeUnitsValueLabel = JLabelledValue("TmeUnit:")
        infoPanelDuration.add(timeUnitsValueLabel)

        val workUnitsValueLabel = JLabelledValue("Work Units:")
        infoPanelDuration.add(workUnitsValueLabel)

        //
        // Date and time  data
        //
        val infoPanelDate = JPanel()
        infoPanel.add(infoPanelDate)
        infoPanelDate.setLayout(BoxLayout(infoPanelDate, BoxLayout.Y_AXIS))

        val dateValueLabel = JLabelledValue("Date:")
        infoPanelDate.add(dateValueLabel)

        val timeValueLabel = JLabelledValue("Time:")
        infoPanelDate.add(timeValueLabel)

        val timestampValueLabel = JLabelledValue("Timestamp:")
        infoPanelDate.add(timestampValueLabel)

        //
        // Misc
        //
        val infoPanelMisc = JPanel()
        infoPanel.add(infoPanelMisc)
        infoPanelMisc.setLayout(BoxLayout(infoPanelMisc, BoxLayout.Y_AXIS))

        val guidValueLabel = JLabelledValue("GUID:")
        infoPanelMisc.add(guidValueLabel)

        val percentageValueLabel = JLabelledValue("Percentage:")
        infoPanelMisc.add(percentageValueLabel)

        //
        // Split pane
        //
        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)
        springLayout.putConstraint(SpringLayout.NORTH, splitPane, 0, SpringLayout.SOUTH, controlsPanel)
        springLayout.putConstraint(SpringLayout.WEST, splitPane, 0, SpringLayout.WEST, this)
        springLayout.putConstraint(SpringLayout.SOUTH, splitPane, 0, SpringLayout.SOUTH, this)
        springLayout.putConstraint(SpringLayout.EAST, splitPane, 0, SpringLayout.EAST, this)
        add(splitPane)
        splitPane.setLeftComponent(tablePanel)
        splitPane.setRightComponent(infoPanel)
        SwingUtilities.invokeLater(object : Runnable() {
            @Override
            fun run() {
                splitPane.setDividerLocation(0.85)
                splitPane.setResizeWeight(0.85)
            }
        })

        //
        // Bindings
        //
        val modelAdapter = BeanAdapter(model, true)
        Bindings.bind(columns, modelAdapter.getValueModel("columns"))
        Bindings.bind(offset, modelAdapter.getValueModel("offset"))
        Bindings.bind(sizeValueLabel, "value", modelAdapter.getValueModel("sizeValueLabel"))
        Bindings.bind(currentSelectionValueLabel, "value", modelAdapter.getValueModel("currentSelectionValueLabel"))
        Bindings.bind(previousSelectionValueLabel, "value", modelAdapter.getValueModel("previousSelectionValueLabel"))
        Bindings.bind(differenceValueLabel, "value", modelAdapter.getValueModel("selectionDifferenceValueLabel"))
        Bindings.bind(shortValueLabel, "value", modelAdapter.getValueModel("shortValueLabel"))
        Bindings.bind(longSixValueLabel, "value", modelAdapter.getValueModel("longSixValueLabel"))
        Bindings.bind(longValueLabel, "value", modelAdapter.getValueModel("longValueLabel"))
        Bindings.bind(doubleValueLabel, "value", modelAdapter.getValueModel("doubleValueLabel"))
        Bindings.bind(durationValueLabel, "value", modelAdapter.getValueModel("durationValueLabel"))
        Bindings.bind(timeUnitsValueLabel, "value", modelAdapter.getValueModel("timeUnitsValueLabel"))
        Bindings.bind(guidValueLabel, "value", modelAdapter.getValueModel("guidValueLabel"))
        Bindings.bind(percentageValueLabel, "value", modelAdapter.getValueModel("percentageValueLabel"))
        Bindings.bind(dateValueLabel, "value", modelAdapter.getValueModel("dateValueLabel"))
        Bindings.bind(timeValueLabel, "value", modelAdapter.getValueModel("timeValueLabel"))
        Bindings.bind(timestampValueLabel, "value", modelAdapter.getValueModel("timestampValueLabel"))
        Bindings.bind(workUnitsValueLabel, "value", modelAdapter.getValueModel("workUnitsValueLabel"))

        PropertyConnector.connect(tablePanel, "leftTableModel", model, "hexTableModel")
        PropertyConnector.connect(tablePanel, "rightTableModel", model, "asciiTableModel")
        PropertyConnector.connect(tablePanel, "selectedCell", model, "selectedCell")
    }
}
