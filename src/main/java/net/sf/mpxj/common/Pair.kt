/*
 * file:       Pair.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2005
 * date:       14/11/2005
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

/**
 * Represents a pair of values.
 *
 * @param <L> first value type
 * @param <R> second value type
</R></L> */
class Pair<L, R>
/**
 * Constructor.
 *
 * @param first first object
 * @param second second object
 */
(private val m_first: L?, private val m_second: R?) {

    /**
     * Retrieve the the first object.
     *
     * @return first object
     */
    val first: L?
        get() = m_first

    /**
     * Retrieve the second object.
     *
     * @return second object
     */
    val second: R?
        get() = m_second

    @Override
    fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (m_first?.hashCode() ?: 0)
        result = prime * result + (m_second?.hashCode() ?: 0)
        return result
    }

    @Override
    fun equals(obj: Object?): Boolean {
        if (this === obj) {
            return true
        }

        if (obj == null) {
            return false
        }

        if (getClass() !== obj!!.getClass()) {
            return false
        }

        val other = obj as Pair<*, *>?
        if (m_first == null) {
            if (other!!.m_first != null) {
                return false
            }
        } else {
            if (!m_first.equals(other!!.m_first)) {
                return false
            }
        }

        if (m_second == null) {
            if (other.m_second != null) {
                return false
            }
        } else {
            if (!m_second.equals(other.m_second)) {
                return false
            }
        }

        return true
    }
}
