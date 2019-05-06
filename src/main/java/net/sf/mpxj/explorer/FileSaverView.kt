/*
 * file:       FileSaverView.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       2017-11-23
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

import java.awt.Component
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

import com.jgoodies.binding.beans.PropertyAdapter

/**
 * Implements the view component of the FileSaver MVC.
 */
class FileSaverView
/**
 * Constructor.
 *
 * @param parent parent component for the dialog
 * @param model file save model
 */
(private val m_parent: Component, private val m_model: FileSaverModel) {
    protected val m_fileChooser: JFileChooser

    init {
        m_fileChooser = JFileChooser()

        val adapter = PropertyAdapter<FileSaverModel>(m_model, "showDialog", true)
        adapter.addValueChangeListener(object : PropertyChangeListener() {
            @Override
            fun propertyChange(evt: PropertyChangeEvent) {
                openFileChooser()
            }
        })

        val extensionsAdaptor = PropertyAdapter<FileSaverModel>(m_model, "extensions", true)
        extensionsAdaptor.addValueChangeListener(object : PropertyChangeListener() {
            @Override
            fun propertyChange(evt: PropertyChangeEvent) {
                setFileFilter()
            }
        })
    }

    /**
     * Command to open the file chooser.
     */
    protected fun openFileChooser() {
        if (m_model.showDialog) {
            if (m_fileChooser.showSaveDialog(m_parent) === JFileChooser.APPROVE_OPTION) {
                val filter = m_fileChooser.getFileFilter() as FileNameExtensionFilter
                val description = filter.getDescription()
                m_model.type = description.substring(0, description.indexOf(' '))
                m_model.file = null
                m_model.file = m_fileChooser.getSelectedFile()
            }
            m_model.showDialog = false
        }
    }

    /**
     * Update the file chooser's filter settings.
     */
    protected fun setFileFilter() {
        m_fileChooser.setAcceptAllFileFilterUsed(false)
        val extensions = m_model.extensions
        var extensionIndex = 0
        while (extensionIndex < extensions!!.size) {
            m_fileChooser.setFileFilter(FileNameExtensionFilter(extensions[extensionIndex].toUpperCase() + " File", extensions[extensionIndex + 1]))
            extensionIndex += 2
        }
    }
}
