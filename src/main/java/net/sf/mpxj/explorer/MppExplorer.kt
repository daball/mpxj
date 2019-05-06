/*
 * file:       MppExplorer.java
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
 * MppExplorer is a Swing UI used to examine the contents of an MPP file.
 */
class MppExplorer {
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
        fileChooserModel.setExtensions("mpp")

        val menuBar = JMenuBar()
        m_frame.setJMenuBar(menuBar)

        val mnFile = JMenu("File")
        menuBar.add(mnFile)

        val mntmOpen = JMenuItem("Open")
        mnFile.add(mntmOpen)

        //
        // Open file
        //
        mntmOpen.addActionListener(object : ActionListener() {
            @Override
            fun actionPerformed(e: ActionEvent) {
                fileChooserController.openFileChooser()
            }
        })

        val tabbedPane = JTabbedPane(SwingConstants.TOP)
        m_frame.getContentPane().add(tabbedPane)

        val adapter = PropertyAdapter<FileChooserModel>(fileChooserModel, "file", true)
        adapter.addValueChangeListener(object : PropertyChangeListener() {
            @Override
            fun propertyChange(evt: PropertyChangeEvent) {
                val file = fileChooserModel.file
                tabbedPane.add(file!!.getName(), MppFilePanel(file))
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
                        val window = MppExplorer()
                        window.m_frame.setVisible(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            })
        }
    }
}
