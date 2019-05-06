/*
 * file:       JsonWriter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2015
 * date:       18/02/2015
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

package net.sf.mpxj.json

import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.Date
import java.util.HashMap

import net.sf.mpxj.AssignmentField
import net.sf.mpxj.CustomField
import net.sf.mpxj.DataType
import net.sf.mpxj.Duration
import net.sf.mpxj.FieldContainer
import net.sf.mpxj.FieldType
import net.sf.mpxj.Priority
import net.sf.mpxj.ProjectField
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Relation
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.ResourceField
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.common.CharsetHelper
import net.sf.mpxj.writer.AbstractProjectWriter

/**
 * This class creates a new JSON file from the contents of
 * a ProjectFile instance.
 */
class JsonWriter : AbstractProjectWriter() {

    private var m_projectFile: ProjectFile? = null
    private var m_writer: JsonStreamWriter? = null
    /**
     * Retrieve the pretty-print flag.
     *
     * @return true if pretty printing is enabled
     */
    /**
     * Set the pretty-print flag.
     *
     * @param pretty true if pretty printing is enabled
     */
    var pretty: Boolean = false
    /**
     * Retrieve the encoding to used when writing the JSON file.
     *
     * @return encoding
     */
    /**
     * Set the encoding to used when writing the JSON file.
     *
     * @param encoding encoding to use
     */
    var encoding = DEFAULT_ENCODING

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(IOException::class)
    override fun write(projectFile: ProjectFile, stream: OutputStream) {
        try {
            m_projectFile = projectFile
            m_writer = JsonStreamWriter(stream, encoding)
            m_writer!!.pretty = pretty

            m_writer!!.writeStartObject(null)
            writeCustomFields()
            writeProperties()
            writeResources()
            writeTasks()
            writeAssignments()
            m_writer!!.writeEndObject()

            m_writer!!.flush()
        } finally {
            m_projectFile = null
        }
    }

    /**
     * Write a list of custom field attributes.
     */
    @Throws(IOException::class)
    private fun writeCustomFields() {
        m_writer!!.writeStartList("custom_fields")
        for (field in m_projectFile!!.customFields) {
            writeCustomField(field)
        }
        m_writer!!.writeEndList()
    }

    /**
     * Write attributes for an individual custom field.
     * Note that at present we are only writing a subset of the
     * available data... in this instance the field alias.
     * If the field does not have an alias we won't write an
     * entry.
     *
     * @param field custom field to write
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeCustomField(field: CustomField) {
        if (field.getAlias() != null) {
            m_writer!!.writeStartObject(null)
            m_writer!!.writeNameValuePair("field_type_class", field.getFieldType().getFieldTypeClass().name().toLowerCase())
            m_writer!!.writeNameValuePair("field_type", field.getFieldType().name().toLowerCase())
            m_writer!!.writeNameValuePair("field_alias", field.getAlias())
            m_writer!!.writeEndObject()
        }
    }

    /**
     * This method writes project property data to a JSON file.
     */
    @Throws(IOException::class)
    private fun writeProperties() {
        writeAttributeTypes("property_types", ProjectField.values())
        writeFields("property_values", m_projectFile!!.projectProperties, ProjectField.values())
    }

    /**
     * This method writes resource data to a JSON file.
     */
    @Throws(IOException::class)
    private fun writeResources() {
        writeAttributeTypes("resource_types", ResourceField.values())

        m_writer!!.writeStartList("resources")
        for (resource in m_projectFile!!.resources) {
            writeFields(null, resource, ResourceField.values())
        }
        m_writer!!.writeEndList()
    }

    /**
     * This method writes task data to a JSON file.
     * Note that we write the task hierarchy in order to make rebuilding the hierarchy easier.
     */
    @Throws(IOException::class)
    private fun writeTasks() {
        writeAttributeTypes("task_types", TaskField.values())

        m_writer!!.writeStartList("tasks")
        for (task in m_projectFile!!.childTasks) {
            writeTask(task)
        }
        m_writer!!.writeEndList()
    }

    /**
     * This method is called recursively to write a task and its child tasks
     * to the JSON file.
     *
     * @param task task to write
     */
    @Throws(IOException::class)
    private fun writeTask(task: Task) {
        writeFields(null, task, TaskField.values())
        for (child in task.childTasks) {
            writeTask(child)
        }
    }

    /**
     * This method writes assignment data to a JSON file.
     */
    @Throws(IOException::class)
    private fun writeAssignments() {
        writeAttributeTypes("assignment_types", AssignmentField.values())

        m_writer!!.writeStartList("assignments")
        for (assignment in m_projectFile!!.resourceAssignments) {
            writeFields(null, assignment, AssignmentField.values())
        }
        m_writer!!.writeEndList()

    }

    /**
     * Generates a mapping between attribute names and data types.
     *
     * @param name name of the map
     * @param types types to write
     */
    @Throws(IOException::class)
    private fun writeAttributeTypes(name: String, types: Array<FieldType>) {
        m_writer!!.writeStartObject(name)
        for (field in types) {
            m_writer!!.writeNameValuePair(field.name().toLowerCase(), field.getDataType().getValue())
        }
        m_writer!!.writeEndObject()
    }

    /**
     * Write a set of fields from a field container to a JSON file.
     * @param objectName name of the object, or null if no name required
     * @param container field container
     * @param fields fields to write
     */
    @Throws(IOException::class)
    private fun writeFields(objectName: String?, container: FieldContainer, fields: Array<FieldType>) {
        m_writer!!.writeStartObject(objectName)
        for (field in fields) {
            val value = container.getCurrentValue(field)
            if (value != null) {
                writeField(field, value)
            }
        }
        m_writer!!.writeEndObject()
    }

    /**
     * Write the appropriate data for a field to the JSON file based on its type.
     *
     * @param field field type
     * @param value field value
     */
    @Throws(IOException::class)
    private fun writeField(field: FieldType, value: Object) {
        val fieldName = field.name().toLowerCase()
        writeField(fieldName, field.getDataType(), value)
    }

    /**
     * Write the appropriate data for a field to the JSON file based on its type.
     *
     * @param fieldName field name
     * @param fieldType field type
     * @param value field value
     */
    @Throws(IOException::class)
    private fun writeField(fieldName: String, fieldType: DataType, value: Object?) {
        when (fieldType) {
            INTEGER -> {
                writeIntegerField(fieldName, value)
            }

            PERCENTAGE, CURRENCY, NUMERIC, UNITS -> {
                writeDoubleField(fieldName, value)
            }

            BOOLEAN -> {
                writeBooleanField(fieldName, value)
            }

            WORK, DURATION -> {
                writeDurationField(fieldName, value)
            }

            DATE -> {
                writeDateField(fieldName, value)
            }

            TIME_UNITS -> {
                writeTimeUnitsField(fieldName, value)
            }

            PRIORITY -> {
                writePriorityField(fieldName, value)
            }

            RELATION_LIST -> {
                writeRelationList(fieldName, value)
            }

            MAP -> {
                writeMap(fieldName, value)
            }

            BINARY -> {
            }// Don't write binary data

            else -> {
                writeStringField(fieldName, value!!)
            }
        }
    }

    /**
     * Write an integer field to the JSON file.
     *
     * @param fieldName field name
     * @param value field value
     */
    @Throws(IOException::class)
    private fun writeIntegerField(fieldName: String, value: Object?) {
        val `val` = (value as Number).intValue()
        if (`val` != 0) {
            m_writer!!.writeNameValuePair(fieldName, `val`)
        }
    }

    /**
     * Write an double field to the JSON file.
     *
     * @param fieldName field name
     * @param value field value
     */
    @Throws(IOException::class)
    private fun writeDoubleField(fieldName: String, value: Object?) {
        val `val` = (value as Number).doubleValue()
        if (`val` != 0.0) {
            m_writer!!.writeNameValuePair(fieldName, `val`)
        }
    }

    /**
     * Write a boolean field to the JSON file.
     *
     * @param fieldName field name
     * @param value field value
     */
    @Throws(IOException::class)
    private fun writeBooleanField(fieldName: String, value: Object?) {
        val `val` = (value as Boolean).booleanValue()
        if (`val`) {
            m_writer!!.writeNameValuePair(fieldName, `val`)
        }
    }

    /**
     * Write a duration field to the JSON file.
     *
     * @param fieldName field name
     * @param value field value
     */
    @Throws(IOException::class)
    private fun writeDurationField(fieldName: String, value: Object?) {
        if (value is String) {
            m_writer!!.writeNameValuePair(fieldName + "_text", value as String?)
        } else {
            val `val` = value as Duration?
            if (`val`!!.getDuration() !== 0) {
                val minutes = `val`!!.convertUnits(TimeUnit.MINUTES, m_projectFile!!.projectProperties)
                val seconds = (minutes.getDuration() * 60.0) as Long
                m_writer!!.writeNameValuePair(fieldName, seconds)
            }
        }
    }

    /**
     * Write a date field to the JSON file.
     *
     * @param fieldName field name
     * @param value field value
     */
    @Throws(IOException::class)
    private fun writeDateField(fieldName: String, value: Object?) {
        if (value is String) {
            m_writer!!.writeNameValuePair(fieldName + "_text", value as String?)
        } else {
            val `val` = value as Date?
            m_writer!!.writeNameValuePair(fieldName, `val`)
        }
    }

    /**
     * Write a time units field to the JSON file.
     *
     * @param fieldName field name
     * @param value field value
     */
    @Throws(IOException::class)
    private fun writeTimeUnitsField(fieldName: String, value: Object?) {
        val `val` = value as TimeUnit?
        if (`val` !== m_projectFile!!.projectProperties.defaultDurationUnits) {
            m_writer!!.writeNameValuePair(fieldName, `val`!!.toString())
        }
    }

    /**
     * Write a priority field to the JSON file.
     *
     * @param fieldName field name
     * @param value field value
     */
    @Throws(IOException::class)
    private fun writePriorityField(fieldName: String, value: Object?) {
        m_writer!!.writeNameValuePair(fieldName, (value as Priority).value)
    }

    /**
     * Write a map field to the JSON file.
     *
     * @param fieldName field name
     * @param value field value
     */
    @Throws(IOException::class)
    private fun writeMap(fieldName: String, value: Object?) {
        @SuppressWarnings("unchecked")
        val map = value as Map<String, Object>?
        m_writer!!.writeStartObject(fieldName)
        for (entry in map!!.entrySet()) {
            var entryValue = entry.getValue()
            if (entryValue != null) {
                var type = TYPE_MAP.get(entryValue!!.getClass().getName())
                if (type == null) {
                    type = DataType.STRING
                    entryValue = entryValue!!.toString()
                }
                writeField(entry.getKey(), type!!, entryValue)
            }
        }
        m_writer!!.writeEndObject()
    }

    /**
     * Write a string field to the JSON file.
     *
     * @param fieldName field name
     * @param value field value
     */
    @Throws(IOException::class)
    private fun writeStringField(fieldName: String, value: Object) {
        val `val` = value.toString()
        if (!`val`.isEmpty()) {
            m_writer!!.writeNameValuePair(fieldName, `val`)
        }
    }

    /**
     * Write a relation list field to the JSON file.
     *
     * @param fieldName field name
     * @param value field value
     */
    @Throws(IOException::class)
    private fun writeRelationList(fieldName: String, value: Object?) {
        @SuppressWarnings("unchecked")
        val list = value as List<Relation>?
        if (!list!!.isEmpty()) {
            m_writer!!.writeStartList(fieldName)
            for (relation in list) {
                m_writer!!.writeStartObject(null)
                writeIntegerField("task_unique_id", relation.targetTask.uniqueID)
                writeDurationField("lag", relation.lag)
                writeStringField("type", relation.type!!)
                m_writer!!.writeEndObject()
            }
            m_writer!!.writeEndList()
        }
    }

    companion object {

        private val DEFAULT_ENCODING = CharsetHelper.UTF8

        private val TYPE_MAP = HashMap<String, DataType>()

        init {
            TYPE_MAP.put(Boolean::class.java!!.getName(), DataType.BOOLEAN)
            TYPE_MAP.put(Date::class.java!!.getName(), DataType.DATE)
            TYPE_MAP.put(Double::class.java!!.getName(), DataType.NUMERIC)
            TYPE_MAP.put(Duration::class.java!!.getName(), DataType.DURATION)
            TYPE_MAP.put(Integer::class.java!!.getName(), DataType.INTEGER)
        }
    }
}
