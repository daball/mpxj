/*
 * file:       FileVersion.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2005
 * date:       17/03/2005
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

/**
 * Instances of this class represent enumerated file version values.
 */
enum class FileVersion
/**
 * Private constructor.
 *
 * @param value file version value
 */
private constructor(private val m_value: Int) : MpxjEnum {
    VERSION_1_0(1),
    VERSION_3_0(3),
    VERSION_4_0(4);

    /**
     * Retrieves the int representation of the file version.
     *
     * @return file version value
     */
    override val value: Int
        @Override get() = m_value

    /**
     * Retrieve the string representation of this file type.
     *
     * @return string representation of the file type
     */
    @Override
    fun toString(): String {
        val result: String

        when (m_value) {
            1 -> {
                result = "1.0"
            }

            3 -> {
                result = "3.0"
            }
            4 -> {
                result = "4.0"
            }

            else -> {
                result = "4.0"
            }
        }

        return result
    }

    companion object {

        /**
         * Retrieve a FileVersion instance representing the supplied value.
         *
         * @param value file version value
         * @return FileVersion instance
         */
        fun getInstance(value: String?): FileVersion {
            var result = VERSION_4_0

            if (value != null) {
                if (value.startsWith("4") === false) {
                    if (value.startsWith("3") === true) {
                        result = VERSION_3_0
                    } else {
                        if (value.startsWith("1") === true) {
                            result = VERSION_1_0
                        }
                    }
                }
            }

            return result
        }
    }
}
