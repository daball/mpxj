/*
 * file:       MppFilePanel.java
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

import java.awt.GridLayout
import java.io.File

import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.TreePath

import org.apache.poi.poifs.filesystem.DocumentEntry

/**
 * Component representing the main view of an MPP file.
 */
class MppFilePanel
/**
 * Constructor.
 *
 * @param file MPP file to be displayed in this view.
 */
(file: File) : JPanel() {
    private val m_treeModel: PoiTreeModel
    private val m_treeController: PoiTreeController
    private val m_treeView: PoiTreeView

    private val m_hexDumpModel: HexDumpModel
    protected var m_hexDumpController: HexDumpController
    private val m_hexDumpView: HexDumpView

    init {
        m_treeModel = PoiTreeModel()
        m_treeController = PoiTreeController(m_treeModel)
        m_treeView = PoiTreeView(m_treeModel)
        m_treeView.setShowsRootHandles(true)

        m_hexDumpModel = HexDumpModel()
        m_hexDumpController = HexDumpController(m_hexDumpModel)
        setLayout(GridLayout(0, 1, 0, 0))
        m_hexDumpView = HexDumpView(m_hexDumpModel)

        val splitPane = JSplitPane()
        splitPane.setDividerLocation(0.3)
        add(splitPane)

        val scrollPane = JScrollPane(m_treeView)
        splitPane.setLeftComponent(scrollPane)

        val tabbedPane = JTabbedPane(SwingConstants.TOP)
        splitPane.setRightComponent(tabbedPane)
        tabbedPane.add("Hex Dump", m_hexDumpView)

        m_treeView.addTreeSelectionListener(object : TreeSelectionListener() {
            @Override
            fun valueChanged(e: TreeSelectionEvent) {
                val path = e.getPath()
                val component = path.getLastPathComponent()
                if (component is DocumentEntry) {
                    m_hexDumpController.viewDocument(component as DocumentEntry)
                }
            }
        })

        m_treeController.loadFile(file)
    }

}
