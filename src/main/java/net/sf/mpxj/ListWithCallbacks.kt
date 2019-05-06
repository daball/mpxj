/*
 * file:       ListWithCallbacks.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2015
 * date:       20/04/2015
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

import java.util.AbstractList
import java.util.ArrayList

/**
 * Class implementing a list interface, backed by an ArrayList instance with callbacks
 * which can be overridden by subclasses for notification of added and removed items.
 *
 * @param <T> list content type
</T> */
abstract class ListWithCallbacks<T> : AbstractList<T>() {

    private val m_list = ArrayList<T>()
    /**
     * Called to notify subclasses of item addition.
     *
     * @param element added item
     */
    protected open fun added(element: T) {
        // Optional implementation supplied by subclass
    }

    /**
     * Called to notify subclasses of item removal.
     *
     * @param element removed item
     */
    protected open fun removed(element: T) {
        // Optional implementation supplied by subclass
    }

    /**
     * Called to notify subclasses of item replacement.
     *
     * @param oldElement old element
     * @param newElement new element
     */
    protected fun replaced(oldElement: T, newElement: T) {
        // Optional implementation supplied by subclass
    }

    /**
     * Clear the list, but don't explicitly "remove" the contents.
     */
    @Override
    fun clear() {
        m_list.clear()
    }

    @Override
    operator fun get(index: Int): T {
        return m_list.get(index)
    }

    @Override
    fun size(): Int {
        return m_list.size()
    }

    @Override
    operator fun set(index: Int, element: T): T {
        val removed = m_list.set(index, element)
        replaced(removed, element)
        return removed
    }

    @Override
    fun add(e: T): Boolean {
        m_list.add(e)
        added(e)
        return true
    }

    @Override
    fun add(index: Int, element: T) {
        m_list.add(index, element)
        added(element)
    }

    @Override
    fun remove(index: Int): T {
        val removed = m_list.remove(index)
        removed(removed)
        return removed
    }
}
