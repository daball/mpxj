/*
 * file:       ProjectFilePanel.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       16/07/2014
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
import java.util.HashMap

import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.TreePath

/**
 * Component representing the main view of a project file.
 */
class ProjectFilePanel
/**
 * Constructor.
 *
 * @param file MPP file to be displayed in this view.
 */
(file: File) : JPanel() {
    private val m_treeModel: ProjectTreeModel
    private val m_treeController: ProjectTreeController
    private val m_treeView: ProjectTreeView
    internal val m_openTabs: Map<MpxjTreeNode, ObjectPropertiesPanel>

    init {
        m_treeModel = ProjectTreeModel()
        m_treeController = ProjectTreeController(m_treeModel)
        setLayout(GridLayout(0, 1, 0, 0))
        m_treeView = ProjectTreeView(m_treeModel)
        m_treeView.setShowsRootHandles(true)

        val splitPane = JSplitPane()
        splitPane.setDividerLocation(0.3)
        add(splitPane)

        val scrollPane = JScrollPane(m_treeView)
        splitPane.setLeftComponent(scrollPane)

        val tabbedPane = JTabbedPane(SwingConstants.TOP)
        splitPane.setRightComponent(tabbedPane)

        m_openTabs = HashMap<MpxjTreeNode, ObjectPropertiesPanel>()

        m_treeView.addTreeSelectionListener(object : TreeSelectionListener() {
            @Override
            fun valueChanged(e: TreeSelectionEvent) {
                val path = e.getPath()
                val component = path.getLastPathComponent() as MpxjTreeNode
                if (component.getUserObject() !is String) {
                    var panel = m_openTabs[component]
                    if (panel == null) {
                        panel = ObjectPropertiesPanel(component.getUserObject(), component.excludedMethods)
                        tabbedPane.add(component.toString(), panel)
                        m_openTabs.put(component, panel)
                    }
                    tabbedPane.setSelectedComponent(panel)
                }
            }
        })

        m_treeController.loadFile(file)
    }

    /**
     * Saves the project file displayed in this panel.
     *
     * @param file target file
     * @param type file type
     */
    fun saveFile(file: File?, type: String) {
        if (file != null) {
            m_treeController.saveFile(file, type)
        }
    }
}
