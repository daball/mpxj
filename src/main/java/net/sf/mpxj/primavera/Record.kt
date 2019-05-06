/*
 * file:       Record.java
 * author:     Bruno Gasnier
 * copyright:  (c) Packwood Software 2002-2011
 * date:       2011-02-16
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

package net.sf.mpxj.primavera

import java.util.ArrayList

/**
 * Represents and parses Primavera compound record data.
 */
internal class Record
/**
 * Constructor. Parse a segment of a string.
 *
 * @param text string to parse
 * @param start start position
 * @param end end position
 */
@JvmOverloads protected constructor(text: String, start: Int = 0, end: Int = text.length()) {

    /**
     * Retrieve the field represented by this record.
     *
     * @return field
     */
    var field: String? = null
        private set
    /**
     * Retrieve the value represented by this record.
     *
     * @return value
     */
    var value: String? = null
        private set
    /**
     * Retrieve all child records.
     *
     * @return list of child records
     */
    val children: List<Record> = ArrayList<Record>()

    init {
        parse(text, start, end)
    }

    /**
     * Retrieve a child record by name.
     *
     * @param key child record name
     * @return child record
     */
    fun getChild(key: String?): Record? {
        var result: Record? = null
        if (key != null) {
            for (record in children) {
                if (key.equals(record.field)) {
                    result = record
                    break
                }
            }
        }
        return result
    }

    /**
     * Parse a block of text into records.
     *
     * @param text text to parse
     * @param start start index
     * @param end end index
     */
    private fun parse(text: String, start: Int, end: Int) {
        val closing = getClosingParenthesisPosition(text, start)
        if (closing == -1 || closing > end) {
            throw IllegalStateException("Error in parenthesis hierarchy")
        }
        if (!text.startsWith("(0||")) {
            throw IllegalStateException("Not a valid record")
        }

        val valueStart = getNextOpeningParenthesisPosition(text, start)
        val valueStop = getClosingParenthesisPosition(text, valueStart)
        val dictStart = getNextOpeningParenthesisPosition(text, valueStop)
        val dictStop = getClosingParenthesisPosition(text, dictStart)
        parse(text, start + 4, valueStart, valueStop, dictStart, dictStop)
    }

    /**
     * Parse a block of text into records.
     *
     * @param text text to be parsed
     * @param start start index
     * @param valueStart value start index
     * @param valueStop value end index
     * @param dictStart dictionary start index
     * @param dictStop dictionary end index
     */
    private fun parse(text: String, start: Int, valueStart: Int, valueStop: Int, dictStart: Int, dictStop: Int) {
        field = text.substring(start, valueStart)
        if (valueStop - valueStart <= 1) {
            value = null
        } else {
            value = text.substring(valueStart + 1, valueStop)
        }
        if (dictStop - dictStart > 1) {
            var s = getNextOpeningParenthesisPosition(text, dictStart)
            while (s >= 0 && s < dictStop) {
                val e = getClosingParenthesisPosition(text, s)
                children.add(Record(text, s, e))
                s = getNextOpeningParenthesisPosition(text, e)
            }
        }
    }

    /**
     * Look for the closing parenthesis corresponding to the one at position
     * represented by the opening index.
     *
     * @param text input expression
     * @param opening opening parenthesis index
     * @return closing parenthesis index
     */
    private fun getClosingParenthesisPosition(text: String, opening: Int): Int {
        if (text.charAt(opening) !== '(') {
            return -1
        }

        var count = 0
        for (i in opening until text.length()) {
            val c = text.charAt(i)
            when (c) {
                '(' -> {
                    ++count
                }

                ')' -> {
                    --count
                    if (count == 0) {
                        return i
                    }
                }
            }
        }

        return -1
    }

    /**
     * Retrieve the position of the next opening parenthesis.
     *
     * @param text text being parsed
     * @param position start position
     * @return index of parenthesis
     */
    private fun getNextOpeningParenthesisPosition(text: String, position: Int): Int {
        return text.indexOf('(', position + 1)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return this.toString(1)
    }

    /**
     * Method to build hierarchical string representation - called recursively.
     *
     * @param spaces number of spaces to use to format the string
     * @return formatted string
     */
    protected fun toString(spaces: Int): String {
        val result = StringBuilder("(0||" + (if (field == null) "" else field) + "(" + (if (value == null) "" else value) + ")(")
        for (record in children) {
            if (spaces != 0) {
                result.append(SEPARATOR)
                for (i in 0 until spaces * 2) {
                    result.append(" ")
                }
            }
            result.append(record.toString(spaces + 1))
        }
        result.append("))")
        return result.toString()
    }

    companion object {

        /**
         * Create a structured Record instance from the flat text data.
         * Null is returned if errors are encountered during parse.
         *
         * @param text flat text data
         * @return Record instance
         */
        fun getRecord(text: String): Record {
            var root: Record?

            try {
                root = Record(text)
            } catch (ex: Exception) {
                root = null
            }
            //
            // I've come across invalid calendar data in an otherwise fine Primavera
            // database belonging to a customer. We deal with this gracefully here
            // rather than propagating an exception.
            //

            return root
        }

        private val SEPARATOR = String(byteArrayOf(0x7f, 0x7f))
    }
}
/**
 * Constructor. Parse an entire string.
 *
 * @param text string to parse
 */