/*
 * file:       GenericView.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2003
 * date:       06/04/2005
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
import net.sf.mpxj.ViewType

/**
 * This class represents a view of a set of project data that has been
 * instantiated within an MS Project file. View data is instantiated when a user
 * first looks at a view in MS Project. Each "screen" in MS Project, for example
 * the Gantt Chart, the Resource Sheet and so on are views. If a user has not
 * looked at a view (for example the Resource Usage view), information about
 * that view will not be present in the MPP file.
 */
abstract class GenericView
/**
 * Extract the view data from the view data block.
 *
 * @param parent parent file
 * @param data view data
 * @param varData var data
 */
@Throws(IOException::class)
constructor(parent: ProjectFile, data: ByteArray, varData: Var2Data) : AbstractMppView(parent) {

    /**
     * Retrieve the ID of the properties data.
     *
     * @return properties data ID
     */
    protected abstract val propertiesID: Integer

    init {

        m_id = Integer.valueOf(MPPUtility.getInt(data, 0))
        m_name = removeAmpersand(MPPUtility.getUnicodeString(data, 4))
        m_type = ViewType.getInstance(MPPUtility.getShort(data, 112))

        val propsData = varData.getByteArray(m_id, propertiesID)
        if (propsData != null) {
            val props = Props9(ByteArrayInputStream(propsData))
            //MPPUtility.fileDump("c:\\temp\\props.txt", props.toString().getBytes());

            val tableName = props.getByteArray(TABLE_NAME)
            if (tableName != null) {
                m_tableName = MPPUtility.removeAmpersands(MPPUtility.getUnicodeString(tableName, 0))
            }
        }
    }

    companion object {

        private val TABLE_NAME = Integer.valueOf(574619658)
    }

}
