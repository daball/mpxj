/*
 * file:       EnumHelper.java
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

package net.sf.mpxj.common

import java.lang.reflect.Array
import java.util.EnumSet

import net.sf.mpxj.MpxjEnum

/**
 * Utility method for working with enumerations.
 */
object EnumHelper {
    /**
     * Creates a lookup array based on the "value" associated with an MpxjEnum.
     *
     * @param <E> target enumeration
     * @param c enumeration class
     * @return lookup array
    </E> */
    fun <E : Enum<E>> createTypeArray(c: Class<E>): Array<E> {
        return createTypeArray<Enum>(c, 0)
    }

    /**
     * Creates a lookup array based on the "value" associated with an MpxjEnum.
     *
     * @param <E> target enumeration
     * @param c enumeration class
     * @param arraySizeOffset offset to apply to the array size
     * @return lookup array
    </E> */
    @SuppressWarnings("unchecked")
    fun <E : Enum<E>> createTypeArray(c: Class<E>, arraySizeOffset: Int): Array<E> {
        val set = EnumSet.allOf(c)
        val array = Array.newInstance(c, set.size() + arraySizeOffset) as Array<E>

        for (e in set) {
            val index = (e as MpxjEnum).value
            if (index >= 0) {
                array[index] = e
            }
        }
        return array
    }
}
