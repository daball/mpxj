/*
 * file:       AbstractVarMeta.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2005
 * date:       05/12/2005
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

import java.io.PrintWriter
import java.io.StringWriter
import java.util.HashSet
import java.util.TreeMap

import net.sf.mpxj.FieldType

/**
 * This class reads in the data from a VarMeta block. This block contains
 * meta data about variable length data items stored in a Var2Data block.
 * The meta data allows the size of the Var2Data block to be determined,
 * along with the number of data items it contains, identifiers for each item,
 * and finally the offset of each item within the block.
 */
internal abstract class AbstractVarMeta : MPPComponent(), VarMeta {
    /**
     * This method retrieves the number of items in the Var2Data block.
     *
     * @return number of items
     */
    override val itemCount: Int
        @Override get() = m_itemCount

    /**
     * This method retrieves the size of the Var2Data block.
     *
     * @return data size
     */
    override val dataSize: Int
        @Override get() = m_dataSize

    /**
     * This method returns an array containing all of the unique identifiers
     * for which data has been stored in the Var2Data block.
     *
     * @return array of unique identifiers
     */
    override val uniqueIdentifierArray: Array<Integer>
        @Override get() {
            val result = arrayOfNulls<Integer>(m_table.size())
            var index = 0
            for (value in m_table.keySet()) {
                result[index] = value
                ++index
            }
            return result
        }

    /**
     * This method returns an set containing all of the unique identifiers
     * for which data has been stored in the Var2Data block.
     *
     * @return set of unique identifiers
     */
    override val uniqueIdentifierSet: Set<Integer>
        @Override get() = m_table.keySet()

    //protected int m_unknown1;
    protected var m_itemCount: Int = 0
    //protected int m_unknown2;
    //protected int m_unknown3;
    protected var m_dataSize: Int = 0
    /**
     * {@inheritDoc}
     */
    /**
     * Allows subclasses to provide the array of offsets.
     *
     * @param offsets array of offsets
     */
    @get:Override
    override var offsets: IntArray? = null
        protected set
    protected var m_table: Map<Integer, ???> = TreeMap<Integer, Map<Integer, Integer>>()

    /**
     * This method retrieves the offset of a given entry in the Var2Data block.
     * Each entry can be uniquely located by the identifier of the object to
     * which the data belongs, and the type of the data.
     *
     * @param id unique identifier of an entity
     * @param type data type identifier
     * @return offset of requested item
     */
    @Override
    override fun getOffset(id: Integer, type: Integer?): Integer? {
        var result: Integer? = null

        val map = m_table[id]
        if (map != null && type != null) {
            result = map!!.get(type)
        }

        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun getTypes(id: Integer): Set<Integer> {
        val result: Set<Integer>

        val map = m_table[id]
        if (map != null) {
            result = map!!.keySet()
        } else {
            result = HashSet<Integer>()
        }

        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun containsKey(key: Integer): Boolean {
        return m_table.containsKey(key)
    }

    /**
     * This method dumps the contents of this VarMeta block as a String.
     * Note that this facility is provided as a debugging aid.
     *
     * @return formatted contents of this block
     */
    @Override
    fun toString(): String {
        return toString(null)
    }

    /**
     * This method dumps the contents of this VarMeta block as a String.
     * Note that this facility is provided as a debugging aid.
     *
     * @param fieldMap field map used to decode var data keys
     * @return formatted contents of this block
     */
    @Override
    override fun toString(fieldMap: FieldMap?): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        pw.println("BEGIN: VarMeta")
        pw.println("   Item count: $m_itemCount")
        pw.println("   Data size: $m_dataSize")

        for (tableEntry in m_table.entrySet()) {
            val uniqueID = tableEntry.getKey()
            pw.println("   Entries for Unique ID: $uniqueID")
            val map = tableEntry.getValue()
            for (entry in map.entrySet()) {
                val fieldType = fieldMap?.getFieldTypeFromVarDataKey(entry.getKey())
                pw.println("      Type=" + (fieldType ?: entry.getKey()) + " Offset=" + entry.getValue())
            }
        }

        pw.println("END: VarMeta")
        pw.println()

        pw.close()
        return sw.toString()
    }
}
