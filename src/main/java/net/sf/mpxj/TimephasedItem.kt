/*
 * file:       TimephasedItem.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2008
 * date:       25/10/2008
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

package net.sf.mpxj

import java.util.Date

/**
 * This class represents an amount, spread over a period of time.
 *
 * @param <T> payload type
</T> */
abstract class TimephasedItem<T> {

    /**
     * Retrieve the start date.
     *
     * @return start date
     */
    /**
     * Set the start date.
     *
     * @param start start date
     */
    var start: Date? = null
    /**
     * Retrieve the total amount.
     *
     * @return total amount
     */
    /**
     * Set the total amount.
     *
     * @param totalAmount total amount
     */
    var totalAmount: T? = null
    /**
     * Retrieve the finish date.
     *
     * @return finish date
     */
    /**
     * Set the finish date.
     *
     * @param finish finish date
     */
    var finish: Date? = null
    /**
     * Retrieve the amount per day.
     *
     * @return amount per day
     */
    /**
     * Set the amount per day.
     *
     * @param amountPerDay amount per day
     */
    var amountPerDay: T? = null
    /**
     * Retrieve the modified flag.
     *
     * @return modified flag
     */
    /**
     * Set the modified flag.
     *
     * @param modified modified flag
     */
    var modified: Boolean = false

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return "[TimephasedItem start=$start totalAmount=$totalAmount finish=$finish amountPerDay=$amountPerDay modified=$modified]"
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    fun equals(o: Object): Boolean {
        var result = false

        if (o is TimephasedItem<*>) {
            val t = o as TimephasedItem<T>
            result = start!!.equals(t.start) && finish!!.equals(t.finish) && totalAmount!!.equals(t.totalAmount) && amountPerDay!!.equals(t.amountPerDay)
        }

        return result
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun hashCode(): Int {
        return start!!.hashCode() + finish!!.hashCode() + totalAmount!!.hashCode() + amountPerDay!!.hashCode()
    }
}
