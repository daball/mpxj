/*
 * file:       FieldTypeHelper.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       2011-05-17
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

package net.sf.mpxj.common

import java.util.Locale

import net.sf.mpxj.AssignmentField
import net.sf.mpxj.ConstraintField
import net.sf.mpxj.DataType
import net.sf.mpxj.Duration
import net.sf.mpxj.FieldType
import net.sf.mpxj.FieldTypeClass
import net.sf.mpxj.ResourceField
import net.sf.mpxj.TaskField

/**
 * Utility class containing methods relating to the FieldType class.
 */
object FieldTypeHelper {
    /**
     * Retrieve an MPP9/MPP12 field ID based on an MPXJ FieldType instance.
     *
     * @param type FieldType instance
     * @return field ID
     */
    fun getFieldID(type: FieldType): Int {
        val result: Int
        when (type.getFieldTypeClass()) {
            TASK -> {
                result = MPPTaskField.TASK_FIELD_BASE or MPPTaskField.getID(type as TaskField)
            }

            RESOURCE -> {
                result = MPPResourceField.RESOURCE_FIELD_BASE or MPPResourceField.getID(type as ResourceField)
            }

            ASSIGNMENT -> {
                result = MPPAssignmentField.ASSIGNMENT_FIELD_BASE or MPPAssignmentField.getID(type as AssignmentField)
            }

            else -> {
                result = -1
            }
        }
        return result
    }

    /**
     * Retrieve a FieldType instance based on an ID value from
     * an MPP9 or MPP12 file.
     *
     * @param fieldID field ID
     * @return FieldType instance
     */
    fun getInstance(fieldID: Int): FieldType {
        var result: FieldType?
        val prefix = fieldID and -0x10000
        val index = fieldID and 0x0000FFFF

        when (prefix) {
            MPPTaskField.TASK_FIELD_BASE -> {
                result = MPPTaskField.getInstance(index)
                if (result == null) {
                    result = getPlaceholder(TaskField::class.java, index)
                }
            }

            MPPResourceField.RESOURCE_FIELD_BASE -> {
                result = MPPResourceField.getInstance(index)
                if (result == null) {
                    result = getPlaceholder(ResourceField::class.java, index)
                }
            }

            MPPAssignmentField.ASSIGNMENT_FIELD_BASE -> {
                result = MPPAssignmentField.getInstance(index)
                if (result == null) {
                    result = getPlaceholder(AssignmentField::class.java, index)
                }
            }

            MPPConstraintField.CONSTRAINT_FIELD_BASE -> {
                result = MPPConstraintField.getInstance(index)
                if (result == null) {
                    result = getPlaceholder(ConstraintField::class.java, index)
                }
            }

            else -> {
                result = getPlaceholder(null, index)
            }
        }

        return result
    }

    /**
     * Retrieve a FieldType instance based on an ID value from
     * an MPP14 field, mapping the START_TEXT, FINISH_TEXT, and DURATION_TEXT
     * values to START, FINISH, and DURATION respectively.
     *
     * @param fieldID field ID
     * @return FieldType instance
     */
    fun getInstance14(fieldID: Int): FieldType {
        var result: FieldType?
        val prefix = fieldID and -0x10000
        val index = fieldID and 0x0000FFFF

        when (prefix) {
            MPPTaskField.TASK_FIELD_BASE -> {
                result = MPPTaskField14.getInstance(index)
                if (result == null) {
                    result = getPlaceholder(TaskField::class.java, index)
                }
            }

            MPPResourceField.RESOURCE_FIELD_BASE -> {
                result = MPPResourceField14.getInstance(index)
                if (result == null) {
                    result = getPlaceholder(ResourceField::class.java, index)
                }
            }

            MPPAssignmentField.ASSIGNMENT_FIELD_BASE -> {
                result = MPPAssignmentField14.getInstance(index)
                if (result == null) {
                    result = getPlaceholder(AssignmentField::class.java, index)
                }
            }

            MPPConstraintField.CONSTRAINT_FIELD_BASE -> {
                result = MPPConstraintField.getInstance(index)
                if (result == null) {
                    result = getPlaceholder(ConstraintField::class.java, index)
                }
            }

            else -> {
                result = getPlaceholder(null, index)
            }
        }

        return result
    }

    /**
     * Generate a placeholder for an unknown type.
     *
     * @param type expected type
     * @param fieldID field ID
     * @return placeholder
     */
    private fun getPlaceholder(type: Class<*>?, fieldID: Int): FieldType {
        return object : FieldType() {
            val fieldTypeClass: FieldTypeClass
                @Override get() = FieldTypeClass.UNKNOWN

            val value: Int
                @Override get() = fieldID

            val name: String
                @Override get() = "Unknown " + if (type == null) "" else type!!.getSimpleName() + "(" + fieldID + ")"

            val dataType: DataType?
                @Override get() = null

            val unitsType: FieldType?
                @Override get() = null

            @Override
            fun name(): String {
                return "UNKNOWN"
            }

            @Override
            fun getName(locale: Locale): String {
                return name
            }

            @Override
            fun toString(): String {
                return name
            }
        }
    }

    /**
     * In some circumstances MS Project refers to the text version of a field (e.g. Start Text rather than Star) when we
     * actually need to process the non-text version of the field. This method performs that mapping.
     *
     * @param field field to mapped
     * @return mapped field
     */
    fun mapTextFields(field: FieldType?): FieldType? {
        var field = field
        if (field != null && field!!.getFieldTypeClass() === FieldTypeClass.TASK) {
            val taskField = field as TaskField?
            when (taskField) {
                START_TEXT -> {
                    field = TaskField.START
                }

                FINISH_TEXT -> {
                    field = TaskField.FINISH
                }

                DURATION_TEXT -> {
                    field = TaskField.DURATION
                }

                else -> {
                }
            }
        }

        return field
    }

    /**
     * Determines if this value is the default value for the given field type.
     *
     * @param type field type
     * @param value value
     * @return true if the value is not default
     */
    fun valueIsNotDefault(type: FieldType, value: Object?): Boolean {
        var result = true

        if (value == null) {
            result = false
        } else {
            val dataType = type.getDataType()
            when (dataType) {
                BOOLEAN -> {
                    result = (value as Boolean).booleanValue()
                }

                CURRENCY, NUMERIC -> {
                    result = !NumberHelper.equals((value as Number).doubleValue(), 0.0, 0.00001)
                }

                DURATION -> {
                    result = (value as Duration).getDuration() !== 0
                }

                else -> {
                }
            }
        }

        return result
    }

}
