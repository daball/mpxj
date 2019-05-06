/*
 * file:       Relation.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2003
 * date:       14/01/2003
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
 * This class represents the relationship between two tasks.
 */
class Relation
/**
 * Default constructor.
 *
 * @param sourceTask source task instance
 * @param targetTask target task instance
 * @param type relation type
 * @param lag relation lag
 */
(
        /**
         * Parent task file.
         */
        /**
         * Retrieve the source task of this relationship.
         *
         * @return source task
         */
        val sourceTask: Task,
        /**
         * Identifier of task with which this relationship is held.
         */
        /**
         * Retrieve the target task of this relationship.
         *
         * @return target task
         */
        val targetTask: Task, type: RelationType, lag: Duration) {

    /**
     * Method used to retrieve the type of relationship being
     * represented.
     *
     * @return relationship type
     */
    val type: RelationType?
        get() = m_type

    /**
     * This method retrieves the lag duration associated
     * with this relationship.
     *
     * @return lag duration
     */
    val lag: Duration?
        get() = m_lag

    /**
     * Retrieve the Unique ID of this Relation.
     *
     * @return unique ID
     */
    /**
     * Set the Unique ID of this Relation.
     *
     * @param uniqueID unique ID
     */
    var uniqueID: Integer? = null

    /**
     * Type of relationship.
     */
    private var m_type: RelationType? = null

    /**
     * Lag between the two tasks.
     */
    private var m_lag: Duration? = null

    init {
        m_type = type
        m_lag = lag

        if (m_type == null) {
            m_type = RelationType.FINISH_START
        }

        if (m_lag == null) {
            m_lag = Duration.getInstance(0, TimeUnit.DAYS)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    fun toString(): String {
        return "[Relation lag: $m_lag type: $m_type $sourceTask -> $targetTask]"
    }
}
