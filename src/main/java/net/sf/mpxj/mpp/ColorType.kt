/*
 * file:       ColorType.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Apr 7, 2005
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

import java.awt.Color

import net.sf.mpxj.MpxjEnum
import net.sf.mpxj.common.EnumHelper
import net.sf.mpxj.common.NumberHelper

/**
 * This enum represents the colors used by Microsoft Project.
 */
enum class ColorType
/**
 * Private constructor.
 *
 * @param type int version of the enum
 * @param name color name
 * @param color Java color instance
 */
private constructor(
        /**
         * Internal representation of the enum int type.
         */
        private val m_value: Int, private val m_name: String, private val m_color: Color) : MpxjEnum {
    BLACK(0, "Black", Color.BLACK),
    RED(1, "Red", Color.RED),
    YELLOW(2, "Yellow", Color.YELLOW),
    LIME(3, "Lime", Color(148, 255, 148)),
    AQUA(4, "Aqua", Color(194, 220, 255)),
    BLUE(5, "Blue", Color.BLUE),
    FUSCHIA(6, "Fuschia", Color.MAGENTA),
    WHITE(7, "White", Color.WHITE),
    MAROON(8, "Maroon", Color(128, 0, 0)),
    GREEN(9, "Green", Color(0, 128, 0)),
    OLIVE(10, "Olive", Color(128, 128, 0)),
    NAVY(11, "Navy", Color(0, 0, 128)),
    PURPLE(12, "Purple", Color(128, 0, 128)),
    TEAL(13, "Teal", Color(0, 128, 128)),
    GRAY(14, "Gray", Color(128, 128, 128)),
    SILVER(15, "Silver", Color(192, 192, 192)),
    AUTOMATIC(16, "Automatic", null);

    /**
     * Retrieve the color name. Currently this is not localised.
     *
     * @return color name
     */
    val name: String
        get() = m_name

    /**
     * Retrieve a Java Color instance matching the color used in MS Project.
     * Note that this will return null if the color type is automatic.
     *
     * @return Color instance
     */
    val color: Color
        get() = m_color

    /**
     * Accessor method used to retrieve the numeric representation of the enum.
     *
     * @return int representation of the enum
     */
    override val value: Int
        @Override get() = m_value

    companion object {

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Int): ColorType {
            var type = type
            if (type < 0 || type >= TYPE_VALUES.size) {
                type = AUTOMATIC.value
            }
            return TYPE_VALUES[type]
        }

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Number?): ColorType {
            val value: Int
            if (type == null) {
                value = -1
            } else {
                value = NumberHelper.getInt(type)
            }
            return getInstance(value)
        }

        /**
         * Array mapping int types to enums.
         */
        private val TYPE_VALUES = EnumHelper.createTypeArray(ColorType::class.java)
    }
}
