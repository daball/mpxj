/*
 * file:       ProjectExplorer.java
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

import java.awt.EventQueue
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.File

import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import javax.swing.UIManager

import com.jgoodies.binding.beans.PropertyAdapter

/**
 * MppExplorer is a Swing UI used to examine the contents of a project file read by MPXJ.
 */
class ProjectExplorer {
    protected var m_frame: JFrame

    /**
     * Create the application.
     */
    init {
        initialize()
    }

    /**
     * Initialize the contents of the frame.
     */
    private fun initialize() {
        m_frame = JFrame()
        m_frame.setBounds(100, 100, 900, 451)
        m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        m_frame.getContentPane().setLayout(GridLayout(1, 0, 0, 0))

        val fileChooserModel = FileChooserModel()
        val fileChooserController = FileChooserController(fileChooserModel)
        @SuppressWarnings("unused")
        val fileChooserView = FileChooserView(m_frame, fileChooserModel)
        fileChooserModel.setExtensions("cdpx", "cdpz", "fts", "gan", "gnt", "mdb", "mpp", "mpx", "pep", "planner", "pmxml", "pod", "pp", "ppx", "prx", "sp", "stx", "xer", "xml", "zip")

        val fileSaverModel = FileSaverModel()
        val fileSaverController = FileSaverController(fileSaverModel)
        @SuppressWarnings("unused")
        val fileSaverView = FileSaverView(m_frame, fileSaverModel)
        fileSaverModel.setExtensions("sdef", "sdef", "mpx", "mpx", "planner", "xml", "pmxml", "xml", "json", "json", "mspdi", "xml")

        val menuBar = JMenuBar()
        m_frame.setJMenuBar(menuBar)

        val mnFile = JMenu("File")
        menuBar.add(mnFile)

        val mntmOpen = JMenuItem("Open File...")
        mnFile.add(mntmOpen)

        val mntmSave = JMenuItem("Save As...")
        mntmSave.setEnabled(false)
        mnFile.add(mntmSave)

        //
        // Open file
        //
        mntmOpen.addActionListener(object : ActionListener() {
            @Override
            fun actionPerformed(e: ActionEvent) {
                fileChooserController.openFileChooser()
            }
        })

        //
        // Save file
        //
        mntmSave.addActionListener(object : ActionListener() {
            @Override
            fun actionPerformed(e: ActionEvent) {
                fileSaverController.openFileSaver()
            }
        })

        val tabbedPane = JTabbedPane(SwingConstants.TOP)
        m_frame.getContentPane().add(tabbedPane)

        val openAdapter = PropertyAdapter<FileChooserModel>(fileChooserModel, "file", true)
        openAdapter.addValueChangeListener(object : PropertyChangeListener() {
            @Override
            fun propertyChange(evt: PropertyChangeEvent) {
                val file = fileChooserModel.file
                tabbedPane.add(file!!.getName(), ProjectFilePanel(file))
                mntmSave.setEnabled(true)
            }
        })

        val saveAdapter = PropertyAdapter<FileSaverModel>(fileSaverModel, "file", true)
        saveAdapter.addValueChangeListener(object : PropertyChangeListener() {
            @Override
            fun propertyChange(evt: PropertyChangeEvent) {
                val panel = tabbedPane.getSelectedComponent() as ProjectFilePanel
                panel.saveFile(fileSaverModel.file, fileSaverModel.type)
            }
        })

    }

    companion object {

        /**
         * Launch the application.
         *
         * @param args command line arguments.
         */
        fun main(args: Array<String>) {
            EventQueue.invokeLater(object : Runnable() {
                @Override
                fun run() {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                        val window = ProjectExplorer()
                        window.m_frame.setVisible(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            })
        }
    }
}
