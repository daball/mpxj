/*
 * file:       ObjectPropertiesController.java
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

import java.lang.reflect.Method
import java.util.ArrayList
import kotlin.collections.Map.Entry
import java.util.TreeMap

import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel

import net.sf.mpxj.Duration

/**
 * Implements the controller component of the ObjectProperties MVC.
 */
class ObjectPropertiesController
/**
 * Constructor.
 *
 * @param model model component
 */
(private val m_model: ObjectPropertiesModel) {

    /**
     * Populate the model with the object's properties.
     *
     * @param object object whose properties we're displaying
     * @param excludedMethods method names to exclude
     */
    fun loadObject(`object`: Object, excludedMethods: Set<String>) {
        m_model.tableModel = createTableModel(`object`, excludedMethods)
    }

    /**
     * Create a table model from an object's properties.
     *
     * @param object target object
     * @param excludedMethods method names to exclude
     * @return table model
     */
    private fun createTableModel(`object`: Object, excludedMethods: Set<String>): TableModel {
        val methods = ArrayList<Method>()
        for (method in `object`.getClass().getMethods()) {
            if (method.getParameterTypes().length === 0 || method.getParameterTypes().length === 1 && method.getParameterTypes()[0] === Int::class.javaPrimitiveType) {
                val name = method.getName()
                if (!excludedMethods.contains(name) && (name.startsWith("get") || name.startsWith("is"))) {
                    methods.add(method)
                }
            }
        }

        val map = TreeMap<String, String>()
        for (method in methods) {
            if (method.getParameterTypes().length === 0) {
                getSingleValue(method, `object`, map)
            } else {
                getMultipleValues(method, `object`, map)
            }
        }

        val headings = arrayOf("Property", "Value")

        val data = Array<Array<String>>(map.size()) { arrayOfNulls(2) }
        var rowIndex = 0
        for (entry in map.entrySet()) {
            data[rowIndex][0] = entry.getKey()
            data[rowIndex][1] = entry.getValue()
            ++rowIndex
        }

        return object : DefaultTableModel(data, headings) {
            @Override
            fun isCellEditable(r: Int, c: Int): Boolean {
                return false
            }
        }
    }

    /**
     * Replace default values will null, allowing them to be ignored.
     *
     * @param value value to test
     * @return filtered value
     */
    private fun filterValue(value: Object?): Object? {
        var value = value
        if (value is Boolean && !(value as Boolean).booleanValue()) {
            value = null
        }
        if (value is String && (value as String).isEmpty()) {
            value = null
        }
        if (value is Double && (value as Double).doubleValue() === 0.0) {
            value = null
        }
        if (value is Integer && (value as Integer).intValue() === 0) {
            value = null
        }
        if (value is Duration && (value as Duration).getDuration() === 0.0) {
            value = null
        }

        return value
    }

    /**
     * Retrieve a single value property.
     *
     * @param method method definition
     * @param object target object
     * @param map parameter values
     */
    private fun getSingleValue(method: Method, `object`: Object, map: Map<String, String>) {
        var value: Object?
        try {
            value = filterValue(method.invoke(`object`))
        } catch (ex: Exception) {
            value = ex.toString()
        }

        if (value != null) {
            map.put(getPropertyName(method), String.valueOf(value))
        }
    }

    /**
     * Retrieve multiple properties.
     *
     * @param method method definition
     * @param object target object
     * @param map parameter values
     */
    private fun getMultipleValues(method: Method, `object`: Object, map: Map<String, String>) {
        try {
            var index = 1
            while (true) {
                val value = filterValue(method.invoke(`object`, Integer.valueOf(index)))
                if (value != null) {
                    map.put(getPropertyName(method, index), String.valueOf(value))
                }
                ++index
            }
        } catch (ex: Exception) {
            // Reached the end of the valid indexes
        }

    }

    /**
     * Convert a method name into a property name.
     *
     * @param method target method
     * @return property name
     */
    private fun getPropertyName(method: Method): String {
        var result = method.getName()
        if (result.startsWith("get")) {
            result = result.substring(3)
        }
        return result
    }

    /**
     * Convert a method name into a property name.
     *
     * @param method target method
     * @param index property index
     * @return property name
     */
    private fun getPropertyName(method: Method, index: Int): String {
        return method.getName().substring(3) + index
    }

}
