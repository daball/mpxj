/*
 * file:       PEPUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       12/01/2018
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

package net.sf.mpxj.turboproject

import java.util.Date

import net.sf.mpxj.common.DateHelper

/**
 * Common utility methods for extracting data from a byte array.
 */
internal object PEPUtility {

    private val EPOCH = 946598400000L
    /**
     * Read a four byte integer.
     *
     * @param data byte array
     * @param offset offset into array
     * @return integer value
     */
    fun getInt(data: ByteArray, offset: Int): Int {
        var result = 0
        var i = offset
        var shiftBy = 0
        while (shiftBy < 32) {
            result = result or (data[i] and 0xff shl shiftBy)
            ++i
            shiftBy += 8
        }
        return result
    }

    /**
     * Read a two byte integer.
     *
     * @param data byte array
     * @param offset offset into array
     * @return integer value
     */
    fun getShort(data: ByteArray, offset: Int): Int {
        var result = 0
        var i = offset
        var shiftBy = 0
        while (shiftBy < 16) {
            result = result or (data[i] and 0xff shl shiftBy)
            ++i
            shiftBy += 8
        }
        return result
    }

    /**
     * Retrieve a string value with a maximum length.
     *
     * @param data byte array
     * @param offset offset into byte array
     * @param maxLength maximum string length
     * @return string
     */
    @JvmOverloads
    fun getString(data: ByteArray, offset: Int, maxLength: Int = data.size - offset): String {
        val buffer = StringBuilder()
        var c: Char

        for (loop in 0 until maxLength) {
            c = data[offset + loop].toChar()

            if (c.toInt() == 0) {
                break
            }

            buffer.append(c)
        }

        return buffer.toString()
    }

    /**
     * Retrieve a start date.
     *
     * @param data byte array
     * @param offset offset into byte array
     * @return start date
     */
    fun getStartDate(data: ByteArray, offset: Int): Date? {
        val result: Date?
        val days = getShort(data, offset).toLong()

        if (days == 0x8000L) {
            result = null
        } else {
            result = DateHelper.getDateFromLong(EPOCH + days * DateHelper.MS_PER_DAY)
        }

        return result
    }

    /**
     * Retrieve a finish date.
     *
     * @param data byte array
     * @param offset offset into byte array
     * @return finish date
     */
    fun getFinishDate(data: ByteArray, offset: Int): Date? {
        val result: Date?
        val days = getShort(data, offset).toLong()

        if (days == 0x8000L) {
            result = null
        } else {
            result = DateHelper.getDateFromLong(EPOCH + (days - 1) * DateHelper.MS_PER_DAY)
        }

        return result
    }
}
/**
 * Retrieve a string value.
 *
 * @param data byte array
 * @param offset offset into byte array
 * @return string value
 */
