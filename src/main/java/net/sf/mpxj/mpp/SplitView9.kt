/*
 * file:       SplitView9.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Jan 27, 2006
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

package net.sf.mpxj.mpp

import java.io.ByteArrayInputStream
import java.io.IOException

import net.sf.mpxj.ProjectFile

/**
 * This class represents a user defined view in Microsoft Project
 * which is made up of two existing views, typically shown above
 * and below the division of a split screen.
 */
class SplitView9
/**
 * Constructor.
 *
 * @param parent parent file
 * @param fixedData fixed data block
 * @param varData var data block
 * @throws IOException
 */
@Throws(IOException::class)
internal constructor(parent: ProjectFile, fixedData: ByteArray, varData: Var2Data) : GenericView9(parent, fixedData, varData) {

    /**
     * Retrieves the upper view name.
     *
     * @return upper view name
     */
    var upperViewName: String? = null
        private set
    /**
     * Retrieves the lower view name.
     *
     * @return lower view name
     */
    var lowerViewName: String? = null
        private set

    init {

        val propsData = varData.getByteArray(m_id, PROPERTIES)
        if (propsData != null) {
            val props = Props9(ByteArrayInputStream(propsData))

            val upperViewName = props.getByteArray(UPPER_VIEW_NAME)
            if (upperViewName != null) {
                this.upperViewName = MPPUtility.removeAmpersands(MPPUtility.getUnicodeString(upperViewName, 0))
            }

            val lowerViewName = props.getByteArray(LOWER_VIEW_NAME)
            if (lowerViewName != null) {
                this.lowerViewName = MPPUtility.removeAmpersands(MPPUtility.getUnicodeString(lowerViewName, 0))
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun toString(): String {
        return "[SplitView9 upperViewName=$upperViewName lowerViewName=$lowerViewName]"
    }

    companion object {

        private val PROPERTIES = Integer.valueOf(1)
        private val UPPER_VIEW_NAME = Integer.valueOf(574619658)
        private val LOWER_VIEW_NAME = Integer.valueOf(574619659)
    }
}
