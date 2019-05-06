/*
 * file:       RowComparator.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2012
 * date:       29/04/2012
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

package net.sf.mpxj.asta

import java.util.Comparator

import net.sf.mpxj.common.NumberHelper

/**
 * Simple comparator to allow two rows to be compared
 * by integer column values.
 */
internal class RowComparator
/**
 * Constructor.
 *
 * @param sortColumns columns used in the comparison.
 */
(vararg sortColumns: String) : Comparator<Row> {

    private val m_sortColumns: Array<String>

    init {
        m_sortColumns = sortColumns
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun compare(leftRow: Row, rightRow: Row): Int {
        var result = 0
        var index = 0
        while (index < m_sortColumns.size) {
            val leftValue = leftRow.getInteger(m_sortColumns[index])
            val rightValue = rightRow.getInteger(m_sortColumns[index])
            result = NumberHelper.compare(leftValue, rightValue)
            if (result != 0) {
                break
            }
            ++index
        }

        return result
    }
}
