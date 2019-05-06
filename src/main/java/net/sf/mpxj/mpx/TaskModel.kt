/*
 * file:       TaskModel.java
 * author:     Scott Melville
 *             Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       15/08/2002
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

package net.sf.mpxj.mpx

import java.util.Arrays
import java.util.HashMap
import java.util.Locale

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.Relation
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField

/**
 * This class represents the task table definition record in an MPX file.
 * This record defines which fields are present in a task record.
 * This record has two forms, one textual and one numeric. Both
 * variants are handled by this class.
 */
internal class TaskModel
/**
 * Default constructor.
 *
 * @param file the parent file to which this record belongs.
 * @param locale target locale
 */
(private val m_parentFile: ProjectFile, locale: Locale) {

    /**
     * This method is used to retrieve a linked list of field identifiers
     * indicating the fields present in a task record.
     *
     * @return list of field names
     */
    val model: IntArray
        get() {
            m_fields[m_count] = -1
            return m_fields
        }

    /**
     * Array of flags indicating whether each field has already been
     * added to the model.
     */
    private val m_flags = BooleanArray(MPXTaskField.MAX_FIELDS)

    /**
     * Array of field numbers in order of their appearance.
     */
    private val m_fields = IntArray(MPXTaskField.MAX_FIELDS + 1)

    /**
     * Count of the number of fields present.
     */
    private var m_count: Int = 0

    /**
     * Array of task column names, indexed by ID.
     */
    private var m_taskNames: Array<String>? = null

    /**
     * Map used to store task field numbers.
     */
    private val m_taskNumbers = HashMap<String, Integer>()

    init {
        setLocale(locale)
    }

    /**
     * This method is used to update the locale specific data used by this class.
     *
     * @param locale target locale
     */
    fun setLocale(locale: Locale) {
        m_taskNames = LocaleData.getStringArray(locale, LocaleData.TASK_NAMES)

        var name: String?
        m_taskNumbers.clear()

        for (loop in m_taskNames!!.indices) {
            name = m_taskNames!![loop]

            if (name != null) {
                m_taskNumbers.put(name, Integer.valueOf(loop))
            }
        }
    }

    /**
     * This method populates the task model from data read from an MPX file.
     *
     * @param record data read from an MPX file
     * @param isText flag indicating whether the textual or numeric data is being supplied
     */
    @Throws(MPXJException::class)
    fun update(record: Record, isText: Boolean) {
        val length = record.length

        for (i in 0 until length) {
            if (isText == true) {
                add(getTaskCode(record.getString(i)!!))
            } else {
                add(record.getInteger(i)!!.intValue())
            }
        }
    }

    /**
     * This method is called from the task class each time an attribute
     * is added, ensuring that all of the attributes present in each task
     * record are present in the resource model.
     *
     * @param field field identifier
     */
    private fun add(field: Int) {
        if (field < m_flags.size) {
            if (m_flags[field] == false) {
                m_flags[field] = true
                m_fields[m_count] = field
                ++m_count
            }
        }
    }

    /**
     * This method is called to populate the arrays which are then
     * used to generate the text version of the model.
     */
    private fun populateModel() {
        if (m_count != 0) {
            m_count = 0
            Arrays.fill(m_flags, false)
        }

        for (task in m_parentFile.tasks) {
            for (loop in 0 until MPXTaskField.MAX_FIELDS) {
                if (!m_flags[loop] && isFieldPopulated(task, MPXTaskField.getMpxjField(loop))) {
                    m_flags[loop] = true
                    m_fields[m_count] = loop
                    ++m_count
                }
            }
        }

        //
        // Ensure the the model fields always appear in the same order
        //
        Arrays.sort(m_fields)
        System.arraycopy(m_fields, m_fields.size - m_count, m_fields, 0, m_count)
    }

    /**
     * Determine if a task field contains data.
     *
     * @param task task instance
     * @param field target field
     * @return true if the field contains data
     */
    @SuppressWarnings("unchecked")
    private fun isFieldPopulated(task: Task, field: TaskField?): Boolean {
        var result = false
        if (field != null) {
            val value = task.getCachedValue(field)
            when (field) {
                PREDECESSORS, SUCCESSORS -> {
                    result = value != null && !(value as List<Relation>).isEmpty()
                }

                else -> {
                    result = value != null
                }
            }
        }
        return result
    }

    /**
     * This method generates a string in MPX format representing the
     * contents of this record. Both the textual and numeric record
     * types are written by this method.
     *
     * @return string containing the data for this record in MPX format.
     */
    @Override
    fun toString(): String {
        populateModel()

        var number: Int
        val delimiter = m_parentFile.projectProperties.mpxDelimiter

        val textual = StringBuilder()
        val numeric = StringBuilder()

        textual.append(MPXConstants.TASK_MODEL_TEXT_RECORD_NUMBER)
        numeric.append(MPXConstants.TASK_MODEL_NUMERIC_RECORD_NUMBER)

        for (loop in 0 until m_count) {
            number = m_fields[loop]

            textual.append(delimiter)
            numeric.append(delimiter)

            textual.append(getTaskField(number))
            numeric.append(number)
        }

        textual.append(MPXConstants.EOL)
        numeric.append(MPXConstants.EOL)

        textual.append(numeric.toString())

        return textual.toString()
    }

    /**
     * Returns Task field name of supplied code no.
     *
     * @param key - the code no of required Task field
     * @return - field name
     */
    private fun getTaskField(key: Int): String? {
        var result: String? = null

        if (key > 0 && key < m_taskNames!!.size) {
            result = m_taskNames!![key]
        }

        return result
    }

    /**
     * Returns code number of Task field supplied.
     *
     * @param field - name
     * @return - code no
     */
    @Throws(MPXJException::class)
    private fun getTaskCode(field: String): Int {
        val result = m_taskNumbers.get(field.trim())
                ?: throw MPXJException(MPXJException.INVALID_TASK_FIELD_NAME.toString() + " " + field)

        return result.intValue()
    }
}
