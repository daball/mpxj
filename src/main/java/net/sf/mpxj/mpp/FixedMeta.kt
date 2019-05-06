/*
 * file:       FixedMeta.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       03/01/2003
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

import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter

import net.sf.mpxj.common.ByteArrayHelper

/**
 * This class is used to represent the "FixedMeta" file entries that are
 * found in a Microsoft Project MPP file. These file entries describe the
 * structure of the "FixedData" blocks with which they are associated.
 * The structure of the Fixed Meta block is not currently fully understood.
 *
 * Note that this class has package level access only, and is not intended
 * for use outside of this context.
 */
internal class FixedMeta
/**
 * Constructor. Supply an item size provider to allow different strategies to be
 * used to determine the correct item size.
 *
 * @param is input stream from which the meta data is read
 * @param itemSizeProvider item size provider used to calculate the item size
 * @throws IOException
 */
@Throws(IOException::class)
constructor(`is`: InputStream, itemSizeProvider: FixedMetaItemSizeProvider) : MPPComponent() {

    /**
     * This method retrieves the number of items in the FixedData block, as reported in the block header.
     *
     * @return number of items in the fixed data block
     */
    val itemCount: Int
        get() = m_itemCount

    /**
     * This method retrieves the number of items in the FixedData block.
     * Where we don't trust the number of items reported by the block header
     * this value is adjusted based on what we know about the block size
     * and the size of the individual items.
     *
     * @return number of items in the fixed data block
     */
    val adjustedItemCount: Int
        get() = m_adjustedItemCount

    /**
     * Number of items in the data block, as reported in the block header.
     */
    private val m_itemCount: Int

    /**
     * Number of items in the data block, adjusted based on block size and item size.
     */
    private val m_adjustedItemCount: Int

    /**
     * Unknown data items relating to each entry in the fixed data block.
     */
    private val m_array: Array<Object>

    /**
     * Constructor. Reads the meta data from an input stream. Note that
     * this version of the constructor copes with more MSP inconsistencies.
     * We already know the block size, so we ignore the item count in the
     * block and work it out for ourselves.
     *
     * @param is input stream from which the meta data is read
     * @param itemSize size of each item in the block
     * @throws IOException on file read failure
     */
    @Throws(IOException::class)
    constructor(`is`: InputStream, itemSize: Int) : this(`is`, FixedMetaItemSizeProvider { fileSize, itemCount -> itemSize }) {
    }

    init {

        //
        // The POI file system guarantees that this is accurate
        //
        val fileSize = `is`.available()

        //
        // First 4 bytes
        //
        if (readInt(`is`) != MAGIC) {
            throw IOException("Bad magic number")
        }

        readInt(`is`)
        m_itemCount = readInt(`is`)
        readInt(`is`)

        val itemSize = itemSizeProvider.getItemSize(fileSize, m_itemCount)
        m_adjustedItemCount = (fileSize - HEADER_SIZE) / itemSize

        m_array = arrayOfNulls<Object>(m_adjustedItemCount)

        for (loop in 0 until m_adjustedItemCount) {
            m_array[loop] = readByteArray(`is`, itemSize)
        }
    }

    /**
     * Constructor, allowing a selection of possible block sizes to be supplied.
     *
     * @param is input stream
     * @param otherFixedBlock  other fixed block to use as part of the heuristic
     * @param itemSizes list of potential block sizes
     */
    @Throws(IOException::class)
    constructor(`is`: InputStream, otherFixedBlock: FixedData, vararg itemSizes: Int) : this(`is`, FixedMetaItemSizeProvider { fileSize, itemCount ->
        var itemSize = itemSizes[0]
        val available = fileSize - HEADER_SIZE
        var distance = Integer.MIN_VALUE
        val otherFixedBlockCount = otherFixedBlock.itemCount

        for (index in itemSizes.indices) {
            val testItemSize = itemSizes[index]
            if (available % testItemSize == 0) {
                //
                // If we are testing a size which fits exactly into
                // the block size, and matches the number of items from
                // another block, we can be pretty certain we have the correct
                // size, so bail out at this point
                //
                if (available / testItemSize == otherFixedBlockCount) {
                    itemSize = testItemSize
                    break
                }

                //
                // Otherwise use a rule-of-thumb to decide on the closest match
                //
                val testDistance = itemCount * testItemSize - available
                if (testDistance <= 0 && testDistance > distance) {
                    itemSize = testItemSize
                    distance = testDistance
                }
            }
        }

        itemSize
    }) {
    }

    /**
     * This method retrieves a byte array containing the data at the
     * given index in the block. If no data is found at the given index
     * this method returns null.
     *
     * @param index index of the data item to be retrieved
     * @return byte array containing the requested data
     */
    fun getByteArrayValue(index: Int): ByteArray? {
        var result: ByteArray? = null

        if (index >= 0 && index < m_array.size && m_array[index] != null) {
            result = m_array[index]
        }

        return result
    }

    /**
     * This method dumps the contents of this FixedMeta block as a String.
     * Note that this facility is provided as a debugging aid.
     *
     * @return formatted contents of this block
     */
    @Override
    fun toString(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        pw.println("BEGIN: FixedMeta")
        pw.println("   Adjusted Item count: $m_adjustedItemCount")

        for (loop in 0 until m_adjustedItemCount) {
            pw.println("   Data at index: $loop")
            pw.println("  " + ByteArrayHelper.hexdump(m_array[loop] as ByteArray, true))
        }

        pw.println("END: FixedMeta")
        pw.println()

        pw.close()
        return sw.toString()
    }

    companion object {

        /**
         * Constant representing the magic number appearing
         * at the start of the block.
         */
        private val MAGIC = -0x5205246

        /**
         * Header size.
         */
        private val HEADER_SIZE = 16
    }
}
