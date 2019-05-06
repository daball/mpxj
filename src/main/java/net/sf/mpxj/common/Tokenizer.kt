/*
 * file:       Tokenizer.java
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

package net.sf.mpxj.common

import java.io.IOException

/**
 * This class implements a tokenizer based loosely on
 * java.io.StreamTokenizer. This tokenizer is designed to parse records from
 * an MPX file correctly. In particular it will handle empty fields,
 * represented by adjacent field delimiters.
 */
abstract class Tokenizer {

    /**
     * This method retrieves the text of the last token found.
     *
     * @return last token text
     */
    val token: String
        get() = m_buffer.toString()

    /**
     * This method retrieves the type of the last token found.
     *
     * @return last token type
     */
    val type: Int
        get() = m_type

    private val m_quote = '"'
    private var m_delimiter = ','
    private var m_next: Int = 0
    private var m_type: Int = 0
    private val m_buffer = StringBuilder()
    /**
     * This method must be implemented to read the next character from the
     * data source.
     *
     * @return next character
     * @throws IOException
     */
    @Throws(IOException::class)
    protected abstract fun read(): Int

    /**
     * This method retrieves the next token and returns a constant representing
     * the type of token found.
     *
     * @return token type value
     */
    @Throws(IOException::class)
    fun nextToken(): Int {
        var c: Int
        var nextc = -1
        var quoted = false
        var result = m_next
        if (m_next != 0) {
            m_next = 0
        }

        m_buffer.setLength(0)

        while (result == 0) {
            if (nextc != -1) {
                c = nextc
                nextc = -1
            } else {
                c = read()
            }

            when (c) {
                TT_EOF -> {
                    if (m_buffer.length() !== 0) {
                        result = TT_WORD
                        m_next = TT_EOF
                    } else {
                        result = TT_EOF
                    }
                }

                TT_EOL -> {
                    var length = m_buffer.length()

                    if (length != 0 && m_buffer.charAt(length - 1) === '\r') {
                        --length
                        m_buffer.setLength(length)
                    }

                    if (length == 0) {
                        result = TT_EOL
                    } else {
                        result = TT_WORD
                        m_next = TT_EOL
                    }
                }

                else -> {
                    if (c == m_quote.toInt()) {
                        if (quoted == false && startQuotedIsValid(m_buffer)) {
                            quoted = true
                        } else {
                            if (quoted == false) {
                                m_buffer.append(c.toChar())
                            } else {
                                nextc = read()
                                if (nextc == m_quote.toInt()) {
                                    m_buffer.append(c.toChar())
                                    nextc = -1
                                } else {
                                    quoted = false
                                }
                            }
                        }
                    } else {
                        if (c == m_delimiter.toInt() && quoted == false) {
                            result = TT_WORD
                        } else {
                            m_buffer.append(c.toChar())
                        }
                    }
                }
            }
        }

        m_type = result

        return result
    }

    /**
     * This method allows us to control the behaviour of the tokenizer for
     * quoted text. Normally quoted text begins with a quote character
     * at the first position within a field. As this method is protected,
     * sub classes can alter this behaviour if required.
     *
     * @param buffer the field contents read so far
     * @return true if it is valid to treat the subsequent text as quoted
     */
    protected open fun startQuotedIsValid(buffer: StringBuilder): Boolean {
        return buffer.length() === 0
    }

    /**
     * This method is used to set the delimiter character recognised
     * by the tokenizer.
     *
     * @param delimiter delimiter character
     */
    fun setDelimiter(delimiter: Char) {
        m_delimiter = delimiter
    }

    companion object {

        val TT_EOL: Int = '\n'.toInt()
        val TT_EOF = -1
        val TT_WORD = -3
    }
}
