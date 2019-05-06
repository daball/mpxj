/*
 * file:       ViewState.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       2007-01-08
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

/**
 * This class represents the state of a view which has been saved
 * as part of a project file.
 */
class ViewState
/**
 * Constructor.
 *
 * @param file parent project file
 * @param viewName view name
 * @param uniqueIdList unique ID list
 * @param filterID filter ID
 */
(private val m_file: ProjectFile,
 /**
  * Retrieve the name of the view associated with this state.
  *
  * @return view name
  */
 val viewName: String,
 /**
  * Retrieve a list of unique IDs representing the contents of this view.
  *
  * @return unique ID list
  */
 val uniqueIdList: List<Integer>, filterID: Int) {

    /**
     * Retrieve the currently applied filter.
     *
     * @return filter instance
     */
    val filter: Filter
        get() = m_file.filters.getFilterByID(m_filterID)
    private val m_filterID: Integer

    init {
        m_filterID = Integer.valueOf(filterID)
    }
}
