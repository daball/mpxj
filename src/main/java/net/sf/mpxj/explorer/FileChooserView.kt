/*
 * file:       FileChooserView.java
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

import java.awt.Component
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

import com.jgoodies.binding.beans.PropertyAdapter

/**
 * Implements the view component of the FileChooser MVC.
 */
class FileChooserView
/**
 * Constructor.
 *
 * @param parent parent component for the dialog
 * @param model file choose model
 */
(private val m_parent: Component, private val m_model: FileChooserModel) {
    protected val m_fileChooser: JFileChooser

    init {
        m_fileChooser = JFileChooser()
        m_fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES)

        val adapter = PropertyAdapter<FileChooserModel>(m_model, "showDialog", true)
        adapter.addValueChangeListener(object : PropertyChangeListener() {
            @Override
            fun propertyChange(evt: PropertyChangeEvent) {
                openFileChooser()
            }
        })

        val extensionsAdaptor = PropertyAdapter<FileChooserModel>(m_model, "extensions", true)
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
            if (m_fileChooser.showOpenDialog(m_parent) === JFileChooser.APPROVE_OPTION) {
                m_model.file = m_fileChooser.getSelectedFile()
            }
            m_model.showDialog = false
        }
    }

    /**
     * Update the file chooser's filter settings.
     */
    protected fun setFileFilter() {
        val extensions = m_model.extensions
        for (extension in extensions!!) {
            m_fileChooser.setFileFilter(FileNameExtensionFilter(extension.toUpperCase() + " Files", extension))
        }
        m_fileChooser.setFileFilter(FileNameExtensionFilter("Project Files", m_model.extensions))
    }
}
