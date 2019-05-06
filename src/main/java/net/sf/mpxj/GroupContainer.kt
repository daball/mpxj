/*
 * file:       GroupContainer.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2015
 * date:       23/04/2015
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

import java.util.HashMap

/**
 * Manages the group definitions belonging to a project.
 */
class GroupContainer : ListWithCallbacks<Group>() {

    private val m_groupsByName = HashMap<String, Group>()
    @Override
    override fun added(group: Group) {
        m_groupsByName.put(group.name, group)
    }

    @Override
    override fun removed(group: Group) {
        m_groupsByName.remove(group.name)
    }

    /**
     * Retrieve a given group by name.
     *
     * @param name group name
     * @return Group instance
     */
    fun getByName(name: String): Group {
        return m_groupsByName.get(name)
    }
}
