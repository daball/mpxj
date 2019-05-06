/*
 * file:       BackgroundPattern.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       31/03/2010
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

import net.sf.mpxj.MpxjEnum
import net.sf.mpxj.common.EnumHelper

/**
 * Represents the pattern used to fill a group.
 */
enum class BackgroundPattern
/**
 * Private constructor.
 *
 * @param type int version of the enum
 * @param name name of the enum
 */
private constructor(
        /**
         * Internal representation of the enum int type.
         */
        private val m_value: Int, private val m_name: String) : MpxjEnum {
    TRANSPARENT(0, "Transparent"),
    SOLID(1, "Solid"),
    LIGHTDOTTED(2, "Light Dotted"),
    DOTTED(3, "Dotted"),
    HEAVYDOTTED(4, "Heavy Dotted"),
    BACKSLASH(5, "Back Slash"),
    FORWARDSLASH(6, "Forward Slash"),
    INVERSEBACKSLASH(7, "Inverse Back Slash"),
    INVERSEFORWARDSLASH(8, "Inverse Forward Slash"),
    LIGHTVERTICALSTRIPE(9, "Light Vertical Stripe"),
    HEAVYVERTICALSTRIPE(10, "Heavy Vertical Stripe"),
    CHECKERED(11, "Checkered"),
    DENSEFORWARDSLASH(12, "Dense Forward Slash"),
    INVERSECHECKERED(13, "Inverse Checkered");

    /**
     * Accessor method used to retrieve the numeric representation of the enum.
     *
     * @return int representation of the enum
     */
    override val value: Int
        @Override get() = m_value

    /**
     * Retrieve the line style name. Currently this is not localised.
     *
     * @return style name
     */
    val name: String
        get() = m_name

    /**
     * Retrieve the String representation of this line style.
     *
     * @return String representation of this line style
     */
    @Override
    fun toString(): String {
        return name
    }

    companion object {

        /**
         * Retrieve an instance of the enum based on its int value.
         *
         * @param type int type
         * @return enum instance
         */
        fun getInstance(type: Int): BackgroundPattern {
            var type = type
            if (type < 0 || type >= TYPE_VALUES.size) {
                type = TRANSPARENT.value
            }
            return TYPE_VALUES[type]
        }

        /**
         * Array mapping int types to enums.
         */
        private val TYPE_VALUES = EnumHelper.createTypeArray(BackgroundPattern::class.java)
    }
}
