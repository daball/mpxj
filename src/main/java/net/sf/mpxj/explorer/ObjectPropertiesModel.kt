/*
 * file:       ObjectPropertiesModel.java
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

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport

import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel

/**
 * Implements the model component of the PropertyTable MVC.
 */
class ObjectPropertiesModel {
    private val m_changeSupport = PropertyChangeSupport(this)
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
    var tableModel: TableModel = DefaultTableModel()
        set(tableModel) {
            m_changeSupport.firePropertyChange("tableModel", this.tableModel, field = tableModel)
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
