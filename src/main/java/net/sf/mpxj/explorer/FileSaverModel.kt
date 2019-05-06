/*
 * file:       FileSaverModel.java
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

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.io.File

/**
 * Implements the model component of the FileChooser MVC.
 */
class FileSaverModel {
    private val m_changeSupport = PropertyChangeSupport(this)
    /**
     * Retrieves the show dialog flag.
     *
     * @return show dialog flag
     */
    /**
     * Sets the show dialog flag.
     *
     * @param showDialog show dialog flag
     */
    var showDialog: Boolean = false
        set(showDialog) {
            m_changeSupport.firePropertyChange("showDialog", this.showDialog, field = showDialog)
        }
    /**
     * Retrieves the file selected by the user.
     *
     * @return file selected by the user
     */
    /**
     * Sets the file selected by the user.
     *
     * @param file file selected by the user.
     */
    var file: File? = null
        set(file) {
            m_changeSupport.firePropertyChange("file", this.file, field = file)
        }
    /**
     * Retrieves the file type selected by the user.
     *
     * @return file type selected by the user
     */
    /**
     * Sets the file type selected by the user.
     *
     * @param type file type selected by the user.
     */
    var type: String? = null
        set(type) {
            m_changeSupport.firePropertyChange("type", this.type, field = type)
        }
    private var m_extensions: Array<String>? = null

    /**
     * Retrieves the file extensions used by the file chooser.
     *
     * @return file extensions
     */
    fun getExtensions(): Array<String>? {
        return m_extensions
    }

    /**
     * Sets the file extensions used by the file chooser.
     *
     * @param extensions file extensions
     */
    fun setExtensions(vararg extensions: String) {
        m_changeSupport.firePropertyChange("extensions", m_extensions, m_extensions = extensions)
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
