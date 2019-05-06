/*
 * file:       ViewFactory12.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       27 September 2006
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

import net.sf.mpxj.ProjectFile
import net.sf.mpxj.View
import net.sf.mpxj.ViewType

/**
 * Default implementation of a view factory for MPP12 files.
 */
internal class ViewFactory12 : ViewFactory {
    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(IOException::class)
    override fun createView(file: ProjectFile, fixedMeta: ByteArray, fixedData: ByteArray, varData: Var2Data, fontBases: Map<Integer, FontBase>): View {
        val view: View
        val splitViewFlag = MPPUtility.getShort(fixedData, 110)
        if (splitViewFlag == 1) {
            view = SplitView9(file, fixedData, varData)
        } else {
            val type = ViewType.getInstance(MPPUtility.getShort(fixedData, 112))
            when (type) {
                GANTT_CHART -> {
                    view = GanttChartView12(file, fixedMeta, fixedData, varData, fontBases)
                }

                else -> {
                    view = GenericView12(file, fixedData, varData)
                }
            }
        }

        return view
    }
}
