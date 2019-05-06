/*
 * file:       JsonStreamWriter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2015
 * date:       18/02/2015
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

package net.sf.mpxj.json

import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Deque
import java.util.LinkedList

/**
 * Writes JSON data to an output stream.
 */
class JsonStreamWriter
/**
 * Constructor.
 *
 * @param stream target output stream
 * @param encoding target encoding
 */
(stream: OutputStream, encoding: Charset) {

    private val m_buffer = StringBuilder()
    private val m_writer: OutputStreamWriter
    private val m_firstNameValuePair = LinkedList<Boolean>()
    /**
     * Retrieve the pretty-print flag.
     *
     * @return true if pretty printing is enabled
     */
    /**
     * Set the pretty-print flag.
     *
     * @param pretty true if pretty printing is enabled
     */
    var pretty: Boolean = false
    private var m_indent = ""
    private val m_format = SimpleDateFormat("\"yyyy-MM-dd'T'HH:mm:ss.S\"")

    init {
        m_writer = OutputStreamWriter(stream, encoding)
        m_firstNameValuePair.push(Boolean.TRUE)
    }

    /**
     * Flush the output stream.
     */
    @Throws(IOException::class)
    fun flush() {
        m_writer.flush()
    }

    /**
     * Begin writing a named object attribute.
     *
     * @param name attribute name
     */
    @Throws(IOException::class)
    fun writeStartObject(name: String?) {
        writeComma()
        writeNewLineIndent()

        if (name != null) {
            writeName(name)
            writeNewLineIndent()
        }

        m_writer.write("{")
        increaseIndent()
    }

    /**
     * Begin writing a named list attribute.
     *
     * @param name attribute name
     */
    @Throws(IOException::class)
    fun writeStartList(name: String) {
        writeComma()
        writeNewLineIndent()
        writeName(name)
        writeNewLineIndent()
        m_writer.write("[")
        increaseIndent()
    }

    /**
     * End writing an object.
     */
    @Throws(IOException::class)
    fun writeEndObject() {
        decreaseIndent()
        m_writer.write("}")
    }

    /**
     * End writing a list.
     */
    @Throws(IOException::class)
    fun writeEndList() {
        decreaseIndent()
        m_writer.write("]")
    }

    /**
     * Write a string attribute.
     *
     * @param name attribute name
     * @param value attribute value
     */
    @Throws(IOException::class)
    fun writeNameValuePair(name: String, value: String) {
        internalWriteNameValuePair(name, escapeString(value))
    }

    /**
     * Write an int attribute.
     *
     * @param name attribute name
     * @param value attribute value
     */
    @Throws(IOException::class)
    fun writeNameValuePair(name: String, value: Int) {
        internalWriteNameValuePair(name, Integer.toString(value))
    }

    /**
     * Write a long attribute.
     *
     * @param name attribute name
     * @param value attribute value
     */
    @Throws(IOException::class)
    fun writeNameValuePair(name: String, value: Long) {
        internalWriteNameValuePair(name, Long.toString(value))
    }

    /**
     * Write a double attribute.
     *
     * @param name attribute name
     * @param value attribute value
     */
    @Throws(IOException::class)
    fun writeNameValuePair(name: String, value: Double) {
        internalWriteNameValuePair(name, Double.toString(value))
    }

    /**
     * Write a boolean attribute.
     *
     * @param name attribute name
     * @param value attribute value
     */
    @Throws(IOException::class)
    fun writeNameValuePair(name: String, value: Boolean) {
        internalWriteNameValuePair(name, if (value) "true" else "false")
    }

    /**
     * Write a Date attribute.
     *
     * @param name attribute name
     * @param value attribute value
     */
    @Throws(IOException::class)
    fun writeNameValuePair(name: String, value: Date) {
        internalWriteNameValuePair(name, m_format.format(value))
    }

    /**
     * Core write attribute implementation.
     *
     * @param name attribute name
     * @param value attribute value
     */
    @Throws(IOException::class)
    private fun internalWriteNameValuePair(name: String, value: String) {
        writeComma()
        writeNewLineIndent()
        writeName(name)

        if (pretty) {
            m_writer.write(' ')
        }

        m_writer.write(value)
    }

    /**
     * Escape text to ensure valid JSON.
     *
     * @param value value
     * @return escaped value
     */
    private fun escapeString(value: String): String {
        m_buffer.setLength(0)
        m_buffer.append('"')
        for (index in 0 until value.length()) {
            val c = value.charAt(index)
            when (c) {
                '"' -> {
                    m_buffer.append("\\\"")
                }

                '\\' -> {
                    m_buffer.append("\\\\")
                }

                '/' -> {
                    m_buffer.append("\\/")
                }

                '\b' -> {
                    m_buffer.append("\\b")
                }

                '\f' -> {
                    m_buffer.append("\\f")
                }

                '\n' -> {
                    m_buffer.append("\\n")
                }

                '\r' -> {
                    m_buffer.append("\\r")
                }

                '\t' -> {
                    m_buffer.append("\\t")
                }

                else -> {
                    // Append if it's not a control character (0x00 to 0x1f)
                    if (c.toInt() > 0x1f) {
                        m_buffer.append(c)
                    }
                }
            }
        }
        m_buffer.append('"')
        return m_buffer.toString()
    }

    /**
     * Write a comma to the output stream if required.
     */
    @Throws(IOException::class)
    private fun writeComma() {
        if (m_firstNameValuePair.peek().booleanValue()) {
            m_firstNameValuePair.pop()
            m_firstNameValuePair.push(Boolean.FALSE)
        } else {
            m_writer.write(',')
        }
    }

    /**
     * Write a new line and indent.
     */
    @Throws(IOException::class)
    private fun writeNewLineIndent() {
        if (pretty) {
            if (!m_indent.isEmpty()) {
                m_writer.write('\n')
                m_writer.write(m_indent)
            }
        }
    }

    /**
     * Write an attribute name.
     *
     * @param name attribute name
     */
    @Throws(IOException::class)
    private fun writeName(name: String) {
        m_writer.write('"')
        m_writer.write(name)
        m_writer.write('"')
        m_writer.write(":")
    }

    /**
     * Increase the indent level.
     */
    private fun increaseIndent() {
        m_firstNameValuePair.push(Boolean.TRUE)
        if (pretty) {
            m_indent += INDENT
        }
    }

    /**
     * Decrease the indent level.
     */
    @Throws(IOException::class)
    private fun decreaseIndent() {
        if (pretty) {
            m_writer.write('\n')
            m_indent = m_indent.substring(0, m_indent.length() - INDENT.length())
            m_writer.write(m_indent)
        }
        m_firstNameValuePair.pop()
    }

    companion object {

        private val INDENT = "  "
    }
}
