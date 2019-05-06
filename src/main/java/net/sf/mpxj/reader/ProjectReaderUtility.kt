/*
 * file:       ProjectReaderUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2008
 * date:       28/01/2008
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

package net.sf.mpxj.reader

import java.util.HashMap

import net.sf.mpxj.MPXJException
import net.sf.mpxj.asta.AstaFileReader
import net.sf.mpxj.fasttrack.FastTrackReader
import net.sf.mpxj.ganttproject.GanttProjectReader
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpp.MPPReader
import net.sf.mpxj.mpx.MPXReader
import net.sf.mpxj.mspdi.MSPDIReader
import net.sf.mpxj.phoenix.PhoenixReader
import net.sf.mpxj.planner.PlannerReader
import net.sf.mpxj.primavera.PrimaveraPMFileReader
import net.sf.mpxj.primavera.PrimaveraXERFileReader
import net.sf.mpxj.projectlibre.ProjectLibreReader

/**
 * This class contains utility methods for working with ProjectReaders.
 * Note that you should probably be using the UniversalProjectReader instead
 * as it can distinguish the correct file type based on content.
 */
object ProjectReaderUtility {

    /**
     * Retrieves a set containing the file extensions supported by the
     * getProjectReader method.
     *
     * @return set of file extensions
     */
    val supportedFileExtensions: Set<String>
        get() = READER_MAP.keySet()

    private val READER_MAP = HashMap<String, Class<out ProjectReader>>()

    /**
     * Retrieves a ProjectReader instance which can read a file of the
     * type specified by the supplied file name.
     *
     * @param name file name
     * @return ProjectReader instance
     */
    @Throws(MPXJException::class)
    fun getProjectReader(name: String): ProjectReader {
        val index = name.lastIndexOf('.')
        if (index == -1) {
            throw IllegalArgumentException("Filename has no extension: $name")
        }

        val extension = name.substring(index + 1).toUpperCase()

        val fileClass = READER_MAP.get(extension)
                ?: throw IllegalArgumentException("Cannot read files of type: $extension")

        try {
            return fileClass.newInstance()
        } catch (ex: Exception) {
            throw MPXJException("Failed to load project reader", ex)
        }

    }

    init {
        READER_MAP.put("MPP", MPPReader::class.java)
        READER_MAP.put("MPT", MPPReader::class.java)
        READER_MAP.put("MPX", MPXReader::class.java)
        READER_MAP.put("XML", MSPDIReader::class.java)
        READER_MAP.put("MPD", MPDDatabaseReader::class.java)
        READER_MAP.put("PLANNER", PlannerReader::class.java)
        READER_MAP.put("XER", PrimaveraXERFileReader::class.java)
        READER_MAP.put("PMXML", PrimaveraPMFileReader::class.java)
        READER_MAP.put("PP", AstaFileReader::class.java)
        READER_MAP.put("PPX", PhoenixReader::class.java)
        READER_MAP.put("FTS", FastTrackReader::class.java)
        READER_MAP.put("POD", ProjectLibreReader::class.java)
        READER_MAP.put("GAN", GanttProjectReader::class.java)
    }
}
/**
 * Constructor.
 */// Private constructor to prevent instantiation.
