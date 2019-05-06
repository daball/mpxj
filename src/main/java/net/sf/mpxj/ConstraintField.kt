/*
 * file:       ConstraintField.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       24/10/2014
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

package net.sf.mpxj

import java.util.EnumSet
import java.util.Locale

/**
 * Instances of this type represent constraint fields.
 */
enum class ConstraintField
/**
 * Constructor.
 *
 * @param dataType field data type
 */
private constructor(private val m_dataType: DataType) : FieldType {
    UNIQUE_ID(DataType.INTEGER),
    TASK1(DataType.INTEGER),
    TASK2(DataType.INTEGER);

    /**
     * {@inheritDoc}
     */
    val fieldTypeClass: FieldTypeClass
        @Override get() = FieldTypeClass.CONSTRAINT

    /**
     * {@inheritDoc}
     */
    val name: String?
        @Override get() = getName(Locale.ENGLISH)

    /**
     * {@inheritDoc}
     */
    val value: Int
        @Override get() = m_value

    /**
     * {@inheritDoc}
     */
    val dataType: DataType
        @Override get() = m_dataType

    private var m_value: Int = 0
    /**
     * {@inheritDoc}
     */
    @get:Override
    val unitsType: FieldType? = null

    /**
     * {@inheritDoc}
     */
    @Override
    fun getName(locale: Locale): String? {
        val titles = LocaleData.getStringArray(locale, LocaleData.CONSTRAINT_COLUMNS)
        var result: String? = null

        if (m_value >= 0 && m_value < titles.size) {
            result = titles[m_value]
        }

        return result
    }

    /**
     * Retrieves the string representation of this instance.
     *
     * @return string representation
     */
    @Override
    fun toString(): String? {
        return name
    }

    companion object {

        /**
         * This method takes the integer enumeration of a constraint field
         * and returns an appropriate class instance.
         *
         * @param type integer constraint field enumeration
         * @return ConstraintField instance
         */
        fun getInstance(type: Int): ConstraintField? {
            var result: ConstraintField? = null

            if (type >= 0 && type < MAX_VALUE) {
                result = TYPE_VALUES[type]
            }

            return result
        }

        val MAX_VALUE = EnumSet.allOf(ConstraintField::class.java).size()
        private val TYPE_VALUES = arrayOfNulls<ConstraintField>(MAX_VALUE)

        init {
            var value = 0
            for (e in EnumSet.allOf(ConstraintField::class.java)) {
                e.m_value = value++
                TYPE_VALUES[e.value] = e
            }
        }
    }

}
