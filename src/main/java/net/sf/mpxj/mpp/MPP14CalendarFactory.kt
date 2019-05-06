/*
 * file:       MPP14CalendarFactory.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       2017-10-04
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

import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream

import net.sf.mpxj.ProjectFile
import net.sf.mpxj.common.NumberHelper

/**
 * MPP12-specific calendar factory.
 */
internal class MPP14CalendarFactory
/**
 * Constructor.
 *
 * @param file parent ProjectFile instance
 */
(file: ProjectFile) : AbstractCalendarAndExceptionFactory(file) {

    /**
     * {@inheritDoc}
     */
    protected override val calendarHoursOffset: Int
        @Override get() = 0

    /**
     * {@inheritDoc}
     */
    protected override val calendarNameVarDataType: Integer
        @Override get() = CALENDAR_NAME

    /**
     * {@inheritDoc}
     */
    protected override val calendarDataVarDataType: Integer
        @Override get() = CALENDAR_DATA

    /**
     * {@inheritDoc}
     */
    @get:Override
    protected override val calendarIDOffset: Int
    /**
     * {@inheritDoc}
     */
    @get:Override
    protected override val baseIDOffset: Int
    /**
     * {@inheritDoc}
     */
    @get:Override
    protected override val resourceIDOffset: Int

    init {

        if (NumberHelper.getInt(file.projectProperties.applicationVersion) > ApplicationVersion.PROJECT_2010) {
            calendarIDOffset = 8
            baseIDOffset = 0
            resourceIDOffset = 4
        } else {
            calendarIDOffset = 0
            baseIDOffset = 4
            resourceIDOffset = 8
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(IOException::class)
    override fun getCalendarVarMeta(directory: DirectoryEntry): VarMeta {
        return VarMeta12(DocumentInputStream(directory.getEntry("VarMeta") as DocumentEntry))
    }

    companion object {

        private val CALENDAR_NAME = Integer.valueOf(1)
        private val CALENDAR_DATA = Integer.valueOf(8)
    }
}
