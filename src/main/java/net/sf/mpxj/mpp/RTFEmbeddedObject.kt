/*
 * file:       RTFEmbeddedObject.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Jun 28, 2005
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

import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.util.LinkedList

import net.sf.mpxj.common.ByteArrayHelper

/**
 * This class represents embedded object data contained within an RTF
 * document. According to the RTF specification, this data has been written using
 * the OLESaveToStream, although I have been unable to locate any existing
 * Java implementations of the equivalent OLELoadFromStream in order to
 * read this data, hence the current implementation.
 *
 * To use this class with note fields in MPXJ, call
 * MPPFile.setPreserveNoteFormatting(true) to allow retrieval of the raw RTF
 * document text from the note fields. You can use the RTFUtility.strip()
 * method to extract plain text from the document for display. If you want
 * to extract any embedded objects from the document, call the
 * RTFEmbeddedObject.getEmbeddedObjects() method, passing in the raw RTF
 * document.
 *
 * The structure of data embedded in a notes field is beyond the scope
 * of the MPXJ documentation. However, generally speaking, you will find that
 * each item of embedded data will be made up of two RTFEmbeddedObject instances,
 * the first is a header usually containing string data, the second is the
 * actual payload data, which will typically be binary. You can retrieve the
 * String data using the RTFEmbeddedObject.getStringData() method, and the
 * binary data using the RTFEmbeddedObject.getData() method.
 *
 * For each embedded item in the document you will typically find two
 * groups of these objects. The first group of two RTFEmbeddedObject instances
 * (one header object and one data object) represent either the location of a
 * linked document, or the binary data for the document itself. The second
 * group of two RTFEmbeddedObject instances contain a METAFILEPICT, which
 * either contains the icon image used as a placeholder for the embedded
 * document, or it contains an image of the document contents, again used
 * as a placeholder.
 *
 * Warning: this functionality is experimental, please submit bugs for any
 * example files containing embedded objects which fail to parse when using this
 * class.
 */
class RTFEmbeddedObject
/**
 * Constructor.
 *
 * @param blocks list of data blocks
 * @param type expected type of next block.
 */
private constructor(blocks: List<ByteArray>, type: Int) {

    /**
     * Retrieve type flag 1.
     *
     * @return type flag 1
     */
    val typeFlag1: Int
        get() = m_typeFlag1

    /**
     * Retrieve type flag 2.
     *
     * @return type flag 2
     */
    val typeFlag2: Int
        get() = m_typeFlag2

    /**
     * Retrieve the data associated with this block as a byte array.
     *
     * @return byte array of data
     */
    val data: ByteArray?
        get() = m_data

    /**
     * Retrieve the data associated with this block as a string.
     *
     * @return string data
     */
    val dataString: String
        get() = if (m_data == null) "" else String(m_data)

    private var m_typeFlag1: Int = 0
    private var m_typeFlag2: Int = 0
    private var m_data: ByteArray? = null

    init {
        when (type) {
            2, 5 -> {
                m_typeFlag1 = getInt(blocks)
                m_typeFlag2 = getInt(blocks)
                val length = getInt(blocks)
                m_data = getData(blocks, length)
            }

            1 -> {
                val length = getInt(blocks)
                m_data = getData(blocks, length)
            }
        }
    }

    /**
     * Internal method used to retrieve a integer from an
     * embedded data block.
     *
     * @param blocks list of data blocks
     * @return int value
     */
    private fun getInt(blocks: List<ByteArray>): Int {
        val result: Int
        if (blocks.isEmpty() === false) {
            val data = blocks.remove(0)
            result = MPPUtility.getInt(data, 0)
        } else {
            result = 0
        }
        return result
    }

    /**
     * Internal method used to retrieve a byte array from one
     * or more embedded data blocks. Consecutive data blocks may
     * need to be concatenated by this method in order to retrieve
     * the complete set of data.
     *
     * @param blocks list of data blocks
     * @param length expected length of the data
     * @return byte array
     */
    private fun getData(blocks: List<ByteArray>, length: Int): ByteArray? {
        var length = length
        val result: ByteArray?

        if (blocks.isEmpty() === false) {
            if (length < 4) {
                length = 4
            }

            result = ByteArray(length)
            var offset = 0
            var data: ByteArray

            while (offset < length) {
                data = blocks.remove(0)
                System.arraycopy(data, 0, result, offset, data.size)
                offset += data.size
            }
        } else {
            result = null
        }

        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        val os = ByteArrayOutputStream()
        val pw = PrintWriter(os)

        pw.println("[RTFObject")
        pw.println("   Flag1=$m_typeFlag1")
        pw.println("   Flag2=$m_typeFlag2")
        pw.println("   Data=")
        pw.println(ByteArrayHelper.hexdump(m_data, true, 16, "  "))
        pw.println("]")
        pw.flush()

        return os.toString()
    }

    companion object {

        /**
         * This method generates a list of lists. Each list represents the data
         * for an embedded object, and contains set set of RTFEmbeddedObject instances
         * that make up the embedded object. This method will return null
         * if there are no embedded objects in the RTF document.
         *
         * @param text RTF document
         * @return list of lists of RTFEmbeddedObject instances
         */
        fun getEmbeddedObjects(text: String): List<???>? {
            var objects: List<???>? = null
            var objectData: List<RTFEmbeddedObject>

            var offset = text.indexOf(OBJDATA)
            if (offset != -1) {
                objects = LinkedList<List<RTFEmbeddedObject>>()

                while (offset != -1) {
                    objectData = LinkedList<RTFEmbeddedObject>()
                    objects!!.add(objectData)
                    offset = readObjectData(offset, text, objectData)
                    offset = text.indexOf(OBJDATA, offset)
                }
            }

            return objects
        }

        /**
         * This method extracts byte arrays from the embedded object data
         * and converts them into RTFEmbeddedObject instances, which
         * it then adds to the supplied list.
         *
         * @param offset offset into the RTF document
         * @param text RTF document
         * @param objects destination for RTFEmbeddedObject instances
         * @return new offset into the RTF document
         */
        private fun readObjectData(offset: Int, text: String, objects: List<RTFEmbeddedObject>): Int {
            var offset = offset
            val blocks = LinkedList<ByteArray>()

            offset += OBJDATA.length()
            offset = skipEndOfLine(text, offset)
            var length: Int
            var lastOffset = offset

            while (offset != -1) {
                length = getBlockLength(text, offset)
                lastOffset = readDataBlock(text, offset, length, blocks)
                offset = skipEndOfLine(text, lastOffset)
            }

            var headerObject: RTFEmbeddedObject
            var dataObject: RTFEmbeddedObject

            while (blocks.isEmpty() === false) {
                headerObject = RTFEmbeddedObject(blocks, 2)
                objects.add(headerObject)

                if (blocks.isEmpty() === false) {
                    dataObject = RTFEmbeddedObject(blocks, headerObject.typeFlag2)
                    objects.add(dataObject)
                }
            }

            return lastOffset
        }

        /**
         * This method skips the end-of-line markers in the RTF document.
         * It also indicates if the end of the embedded object has been reached.
         *
         * @param text RTF document test
         * @param offset offset into the RTF document
         * @return new offset
         */
        private fun skipEndOfLine(text: String, offset: Int): Int {
            var offset = offset
            var c: Char
            var finished = false

            while (finished == false) {
                c = text.charAt(offset)
                when (c) {
                    ' ' // found that OBJDATA could be followed by a space the EOL
                        , '\r', '\n' -> {
                        ++offset
                    }

                    '}' -> {
                        offset = -1
                        finished = true
                    }

                    else -> {
                        finished = true
                    }
                }
            }

            return offset
        }

        /**
         * Calculates the length of the next block of RTF data.
         *
         * @param text RTF data
         * @param offset current offset into this data
         * @return block length
         */
        private fun getBlockLength(text: String, offset: Int): Int {
            var offset = offset
            val startIndex = offset
            var finished = false
            var c: Char

            while (finished == false) {
                c = text.charAt(offset)
                when (c) {
                    '\r', '\n', '}' -> {
                        finished = true
                    }

                    else -> {
                        ++offset
                    }
                }
            }

            return offset - startIndex
        }

        /**
         * Reads a data block and adds it to the list of blocks.
         *
         * @param text RTF data
         * @param offset current offset
         * @param length next block length
         * @param blocks list of blocks
         * @return next offset
         */
        private fun readDataBlock(text: String, offset: Int, length: Int, blocks: List<ByteArray>): Int {
            var offset = offset
            val bytes = length / 2
            val data = ByteArray(bytes)

            for (index in 0 until bytes) {
                data[index] = Integer.parseInt(text.substring(offset, offset + 2), 16) as Byte
                offset += 2
            }

            blocks.add(data)
            return offset
        }

        private val OBJDATA = "\\objdata"
    }
}
