/*
 * file:       ResourceModel.java
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
import net.sf.mpxj.Resource

/**
 * This class represents the resource table definition record in an MPX file.
 * This record defines which fields are present in a resource record.
 * This record has two forms, one textual and one numeric. Both
 * variants are handled by this class.
 */
internal class ResourceModel
/**
 * Default constructor.
 *
 * @param file the parent file to which this record belongs.
 * @param locale target locale
 */
(private val m_parentFile: ProjectFile, locale: Locale) {

    /**
     * This method is used to retrieve an array of field identifiers
     * indicating the fields present in a resource record. Note that
     * the values in this array will be terminated by -1.
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
    private val m_flags = BooleanArray(MPXResourceField.MAX_FIELDS)

    /**
     * Array of field numbers in order of their appearance.
     */
    private val m_fields = IntArray(MPXResourceField.MAX_FIELDS + 1)

    /**
     * Count of the number of fields present.
     */
    private var m_count: Int = 0

    /**
     * Array of resource column names, indexed by ID.
     */
    private var m_resourceNames: Array<String>? = null

    /**
     * Map to store Resource field Numbers.
     */
    private val m_resourceNumbers = HashMap<String, Integer>()

    init {
        setLocale(locale)
    }

    /**
     * This method is used to update the locale specific data used by this class.
     *
     * @param locale target locale
     */
    fun setLocale(locale: Locale) {
        m_resourceNames = LocaleData.getStringArray(locale, LocaleData.RESOURCE_NAMES)

        var name: String?
        m_resourceNumbers.clear()

        for (loop in m_resourceNames!!.indices) {
            name = m_resourceNames!![loop]
            if (name != null) {
                m_resourceNumbers.put(name, Integer.valueOf(loop))
            }
        }
    }

    /**
     * This method populates the resource model from data read from an MPX file.
     *
     * @param record data read from an MPX file
     * @param isText flag indicating whether the tetxual or numeric data is being supplied
     */
    @Throws(MPXJException::class)
    fun update(record: Record, isText: Boolean) {
        val length = record.length

        for (i in 0 until length) {
            if (isText == true) {
                add(getResourceCode(record.getString(i)))
            } else {
                add(record.getInteger(i)!!.intValue())
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

        for (resource in m_parentFile.resources) {
            for (loop in 0 until MPXResourceField.MAX_FIELDS) {
                if (resource.getCachedValue(MPXResourceField.getMpxjField(loop)) != null) {
                    if (m_flags[loop] == false) {
                        m_flags[loop] = true
                        m_fields[m_count] = loop
                        ++m_count
                    }
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

        textual.append(MPXConstants.RESOURCE_MODEL_TEXT_RECORD_NUMBER)
        numeric.append(MPXConstants.RESOURCE_MODEL_NUMERIC_RECORD_NUMBER)

        for (loop in 0 until m_count) {
            number = m_fields[loop]

            textual.append(delimiter)
            numeric.append(delimiter)

            textual.append(getResourceField(number))
            numeric.append(number)
        }

        textual.append(MPXConstants.EOL)
        numeric.append(MPXConstants.EOL)

        textual.append(numeric.toString())

        return textual.toString()
    }

    /**
     * This method is called from the Resource class each time an attribute
     * is added, ensuring that all of the attributes present in each resource
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
     * Given a resource field number, this method returns the resource field name.
     *
     * @param key resource field number
     * @return resource field name
     */
    private fun getResourceField(key: Int): String? {
        var result: String? = null

        if (key > 0 && key < m_resourceNames!!.size) {
            result = m_resourceNames!![key]
        }

        return result
    }

    /**
     * Given a resource field name, this method returns the resource field number.
     *
     * @param field resource field name
     * @return resource field number
     */
    @Throws(MPXJException::class)
    private fun getResourceCode(field: String?): Int {
        val result = m_resourceNumbers.get(field)
                ?: throw MPXJException(MPXJException.INVALID_RESOURCE_FIELD_NAME.toString() + " " + field)

        return result.intValue()
    }
}
