/*
 * file:       PoiTreeModel.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2014
 * date:       06/07/2014
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

package net.sf.mpxj.explorer

import java.util.ArrayList

import javax.swing.event.EventListenerList
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.Entry
import org.apache.poi.poifs.filesystem.POIFSFileSystem

/**
 * Implements the model component of the PoiTree MVC.
 */
class PoiTreeModel : TreeModel {
    private val m_listenerList = EventListenerList()
    private var m_file: POIFSFileSystem? = null

    val root: Object?
        @Override get() {
            var result: Object? = null
            if (m_file != null) {
                result = m_file!!.root
            }
            return result
        }

    /**
     * Point the model to a file.
     *
     * @param file POIFS file
     */
    fun setFile(file: POIFSFileSystem) {
        m_file = file
        fireTreeStructureChanged()
    }

    @Override
    fun getChild(parent: Object, index: Int): Object? {
        var result: Object? = null
        if (parent is DirectoryEntry) {
            val entries = getChildNodes(parent as DirectoryEntry)
            if (entries.size() > index) {
                result = entries[index]
            }
        }
        return result
    }

    @Override
    fun getChildCount(parent: Object): Int {
        val result: Int
        if (parent is DirectoryEntry) {
            val node = parent as DirectoryEntry
            result = node.entryCount
        } else {
            result = 0
        }
        return result
    }

    @Override
    fun isLeaf(node: Object): Boolean {
        return node !is DirectoryEntry
    }

    @Override
    fun valueForPathChanged(path: TreePath, newValue: Object) {
        throw UnsupportedOperationException()
    }

    @Override
    fun getIndexOfChild(parent: Object, child: Object): Int {
        var result = -1
        if (parent is DirectoryEntry) {
            val entries = getChildNodes(parent as DirectoryEntry)
            result = entries.indexOf(child)
        }

        return result
    }

    @Override
    fun addTreeModelListener(l: TreeModelListener) {
        m_listenerList.add(TreeModelListener::class.java, l)
    }

    @Override
    fun removeTreeModelListener(l: TreeModelListener) {
        m_listenerList.remove(TreeModelListener::class.java, l)
    }

    /**
     * Retrieves child nodes from a directory entry.
     *
     * @param parent parent directory entry
     * @return list of child nodes
     */
    private fun getChildNodes(parent: DirectoryEntry): List<Entry> {
        val result = ArrayList<Entry>()
        val entries = parent.entries
        while (entries.hasNext()) {
            result.add(entries.next())
        }
        return result
    }

    /**
     * Notify listeners that the tree structure has changed.
     */
    private fun fireTreeStructureChanged() {
        // Guaranteed to return a non-null array
        val listeners = m_listenerList.getListenerList()
        var e: TreeModelEvent? = null
        // Process the listeners last to first, notifying
        // those that are interested in this event
        var i = listeners.size - 2
        while (i >= 0) {
            if (listeners[i] === TreeModelListener::class.java) {
                // Lazily create the event:
                if (e == null) {
                    e = TreeModelEvent(root, arrayOf<Object>(root), null, null)
                }
                (listeners[i + 1] as TreeModelListener).treeStructureChanged(e)
            }
            i -= 2
        }
    }

}
