/*
 * file:       DefaultProjectListener.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Dec 13, 2005
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

package net.sf.mpxj.listener

import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.Relation
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceAssignment
import net.sf.mpxj.Task

/**
 * This class is a default implementation of the ProjectListener interface.
 * This is designed to be subclassed by developers to allow them to implement
 * the event methods they require, without having to provide implementations for
 * other methods which they are not interested in.
 */
class DefaultProjectListener : ProjectListener {
    /**
     * {@inheritDoc}
     */
    @Override
    override fun taskRead(task: Task) {
        // default implementation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun taskWritten(task: Task) {
        // default implementation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun resourceRead(resource: Resource) {
        // default implementation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun resourceWritten(resource: Resource) {
        // default implementation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun calendarRead(calendar: ProjectCalendar) {
        // default implementation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun calendarWritten(calendar: ProjectCalendar) {
        // default implementation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun assignmentRead(assignment: ResourceAssignment) {
        // default implementation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun assignmentWritten(assignment: ResourceAssignment) {
        // default implementation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun relationRead(relation: Relation) {
        // default implementation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun relationWritten(relation: Relation) {
        // default implementation
    }
}
