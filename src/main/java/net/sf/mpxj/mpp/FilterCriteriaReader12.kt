/*
 * file:       FilterCriteriaReader12.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2006
 * date:       2010-05-06
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

import net.sf.mpxj.FieldType
import net.sf.mpxj.common.FieldTypeHelper

/**
 * This class allows filter criteria definitions to be read from an MPP12 file.
 */
class FilterCriteriaReader12 : CriteriaReader() {
    /**
     * {@inheritDoc}
     */
    protected override val criteriaBlockSize: Int
        @Override get() = 80

    /**
     * {@inheritDoc}
     */
    protected override val criteriaStartOffset: Int
        @Override get() = 20

    /**
     * {@inheritDoc}
     */
    protected override val valueOffset: Int
        @Override get() = 32

    /**
     * {@inheritDoc}
     */
    protected override val timeUnitsOffset: Int
        @Override get() = 42

    /**
     * {@inheritDoc}
     */
    protected override val criteriaTextStartOffset: Int
        @Override get() = 16

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getChildBlock(block: ByteArray): ByteArray {
        val offset = MPPUtility.getShort(block, 74)
        return m_criteriaBlockMap.get(Integer.valueOf(offset))
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getListNextBlock(block: ByteArray): ByteArray? {
        val offset = MPPUtility.getShort(block, 76)
        return m_criteriaBlockMap.get(Integer.valueOf(offset))
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getFieldType(block: ByteArray): FieldType {
        val fieldIndex = MPPUtility.getInt(block, 40)
        return FieldTypeHelper.getInstance(fieldIndex)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getTextOffset(block: ByteArray): Int {
        return MPPUtility.getShort(block, 68)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getPromptOffset(block: ByteArray): Int {
        return MPPUtility.getShort(block, 72)
    }
}
