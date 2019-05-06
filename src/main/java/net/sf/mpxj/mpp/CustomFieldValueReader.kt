/*
 * file:       CustomFieldValueReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2015
 * date:       28/04/2015
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

package net.sf.mpxj.mpp

import java.io.IOException

import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.TimeUnit

/**
 * Common implementation detail shared by custom field value readers.
 */
abstract class CustomFieldValueReader
/**
 * Constructor.
 *
 * @param properties project properties
 * @param container custom field config
 * @param outlineCodeVarMeta raw mpp data
 * @param outlineCodeVarData raw mpp data
 * @param outlineCodeFixedData raw mpp data
 * @param outlineCodeFixedData2 raw mpp data
 * @param taskProps raw mpp data
 */
(protected var m_properties: ProjectProperties, protected var m_container: CustomFieldContainer, protected var m_outlineCodeVarMeta: VarMeta, protected var m_outlineCodeVarData: Var2Data, protected var m_outlineCodeFixedData: FixedData, protected var m_outlineCodeFixedData2: FixedData, protected var m_taskProps: Props) {

    /**
     * Method implement by subclasses to read custom field values.
     */
    @Throws(IOException::class)
    abstract fun process()

    /**
     * Convert raw value as read from the MPP file into a Java type.
     *
     * @param type MPP value type
     * @param value raw value data
     * @return Java object
     */
    protected fun getTypedValue(type: Int, value: ByteArray): Object {
        val result: Object?

        when (type) {
            4 // Date
            -> {
                result = MPPUtility.getTimestamp(value, 0)
            }

            6 // Duration
            -> {
                val units = MPPUtility.getDurationTimeUnits(MPPUtility.getShort(value, 4), m_properties.defaultDurationUnits)
                result = MPPUtility.getAdjustedDuration(m_properties, MPPUtility.getInt(value, 0), units)
            }

            9 // Cost
            -> {
                result = Double.valueOf(MPPUtility.getDouble(value, 0) / 100)
            }

            15 // Number
            -> {
                result = Double.valueOf(MPPUtility.getDouble(value, 0))
            }

            36058, 21 // Text
            -> {
                result = MPPUtility.getUnicodeString(value, 0)
            }

            else -> {
                result = value
            }
        }

        return result
    }

    companion object {

        val VALUE_LIST_VALUE = Integer.valueOf(22)
        val VALUE_LIST_DESCRIPTION = Integer.valueOf(8)
        val VALUE_LIST_UNKNOWN = Integer.valueOf(23)
    }
}
