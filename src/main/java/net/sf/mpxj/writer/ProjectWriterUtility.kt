/*
 * file:       ProjectWriterUtility.java
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

package net.sf.mpxj.writer

import java.util.HashMap

import net.sf.mpxj.json.JsonWriter
import net.sf.mpxj.mpx.MPXWriter
import net.sf.mpxj.mspdi.MSPDIWriter
import net.sf.mpxj.planner.PlannerWriter
import net.sf.mpxj.primavera.PrimaveraPMFileWriter
import net.sf.mpxj.sdef.SDEFWriter

/**
 * This class contains utility methods for working with ProjectWriters.
 */
object ProjectWriterUtility {

    /**
     * Retrieves a set containing the file extensions supported by the
     * getProjectWriter method.
     *
     * @return set of file extensions
     */
    val supportedFileExtensions: Set<String>
        get() = WRITER_MAP.keySet()

    private val WRITER_MAP = HashMap<String, Class<out ProjectWriter>>()

    /**
     * Retrieves a ProjectWriter instance which can write a file of the
     * type specified by the supplied file name.
     *
     * @param name file name
     * @return ProjectWriter instance
     */
    @Throws(InstantiationException::class, IllegalAccessException::class)
    fun getProjectWriter(name: String): ProjectWriter {
        val index = name.lastIndexOf('.')
        if (index == -1) {
            throw IllegalArgumentException("Filename has no extension: $name")
        }

        val extension = name.substring(index + 1).toUpperCase()

        val fileClass = WRITER_MAP.get(extension) ?: throw IllegalArgumentException("Cannot write files of type: $name")

        return fileClass.newInstance()
    }

    init {
        WRITER_MAP.put("MPX", MPXWriter::class.java)
        WRITER_MAP.put("XML", MSPDIWriter::class.java)
        WRITER_MAP.put("PMXML", PrimaveraPMFileWriter::class.java)
        WRITER_MAP.put("PLANNER", PlannerWriter::class.java)
        WRITER_MAP.put("JSON", JsonWriter::class.java)
        WRITER_MAP.put("SDEF", SDEFWriter::class.java)
    }
}
/**
 * Constructor.
 */// Private constructor to prevent instantiation.
