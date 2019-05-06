/*
 * file:       ViewStateReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       Jan 07, 2007
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

import java.io.IOException
import java.util.LinkedList

import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ViewState

/**
 * This class allows the saved state of a view to be read from an MPP file.
 */
abstract class ViewStateReader {
    /**
     * Retrieves the props data using a file format specific method.
     *
     * @param varData var data block
     * @return props data
     * @throws IOException
     */
    @Throws(IOException::class)
    protected abstract fun getProps(varData: Var2Data): Props?

    /**
     * Entry point for processing saved view state.
     *
     * @param file project file
     * @param varData view state var data
     * @param fixedData view state fixed data
     * @throws IOException
     */
    @Throws(IOException::class)
    fun process(file: ProjectFile, varData: Var2Data, fixedData: ByteArray) {
        val props = getProps(varData)
        //System.out.println(props);
        if (props != null) {
            val viewName = MPPUtility.removeAmpersands(props.getUnicodeString(VIEW_NAME))
            val listData = props.getByteArray(VIEW_CONTENTS)
            val uniqueIdList = LinkedList<Integer>()
            if (listData != null) {
                var index = 0
                while (index < listData.size) {
                    val uniqueID = Integer.valueOf(MPPUtility.getInt(listData, index))

                    //
                    // Ensure that we have a valid task, and that if we have and
                    // ID of zero, this is the first task shown.
                    //
                    if (file.getTaskByUniqueID(uniqueID) != null && (uniqueID.intValue() !== 0 || index == 0)) {
                        uniqueIdList.add(uniqueID)
                    }
                    index += 4
                }
            }

            val filterID = MPPUtility.getShort(fixedData, 128)

            val state = ViewState(file, viewName, uniqueIdList, filterID)
            file.views.setViewState(state)
        }
    }

    companion object {

        private val VIEW_NAME = Integer.valueOf(641728536)
        private val VIEW_CONTENTS = Integer.valueOf(641728565)
    }
}
