/*
 * file:       MPPReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       2005-12-21
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
import java.io.InputStream
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedList

import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream
import org.apache.poi.poifs.filesystem.POIFSFileSystem

import net.sf.mpxj.DateRange
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectConfig
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Relation
import net.sf.mpxj.Task
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.AbstractProjectReader

/**
 * This class creates a new ProjectFile instance by reading an MPP file.
 */
class MPPReader : AbstractProjectReader() {

    /**
     * Flag used to indicate whether RTF formatting in notes should
     * be preserved. The default value for this flag is false.
     */
    /**
     * This method retrieves the state of the preserve note formatting flag.
     *
     * @return boolean flag
     */
    /**
     * This method sets a flag to indicate whether the RTF formatting associated
     * with notes should be preserved or removed. By default the formatting
     * is removed.
     *
     * @param preserveNoteFormatting boolean flag
     */
    var preserveNoteFormatting: Boolean = false
        get() = field

    /**
     * Setting this flag to true allows raw timephased data to be retrieved.
     */
    /**
     * If this flag is true, raw timephased data will be retrieved
     * from MS Project: no normalisation will take place.
     *
     * @return boolean flag
     */
    /**
     * If this flag is true, raw timephased data will be retrieved
     * from MS Project: no normalisation will take place.
     *
     * @param useRawTimephasedData boolean flag
     */
    var useRawTimephasedData: Boolean = false

    /**
     * Flag to allow time and memory to be saved by not reading
     * presentation data from the MPP file.
     */
    /**
     * Retrieves a flag which indicates whether presentation data will
     * be read from the MPP file. Not reading this data saves time and memory.
     *
     * @return presentation data flag
     */
    /**
     * Flag to allow time and memory to be saved by not reading
     * presentation data from the MPP file.
     *
     * @param readPresentationData set to false to prevent presentation data being read
     */
    var readPresentationData = true
    /**
     * Flag to determine if the reader should only read the project properties.
     * This allows for rapid access to the document properties, without the
     * cost of reading the entire contents of the project file.
     *
     * @return true if the reader should only read the project properties
     */
    /**
     * Flag to determine if the reader should only read the project properties.
     * This allows for rapid access to the document properties, without the
     * cost of reading the entire contents of the project file.
     *
     * @param readPropertiesOnly true if the reader should only read the project properties
     */
    var readPropertiesOnly: Boolean = false

    /**
     * Internal only. Get the read password for this Project file. This is
     * needed in order to be allowed to read a read-protected Project file.
     *
     * @return password password text
     */
    /**
     * Set the read password for this Project file. This is needed in order to
     * be allowed to read a read-protected Project file.
     *
     * Note: Set this each time before calling the read method.
     *
     * @param password password text
     */
    var readPassword: String? = null
    private var m_projectListeners: List<ProjectListener>? = null
    /**
     * {@inheritDoc}
     */
    @Override
    override fun addProjectListener(listener: ProjectListener) {
        if (m_projectListeners == null) {
            m_projectListeners = LinkedList<ProjectListener>()
        }
        m_projectListeners!!.add(listener)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(`is`: InputStream): ProjectFile {

        try {

            //
            // Open the file system
            //
            val fs = POIFSFileSystem(`is`)

            return read(fs)

        } catch (ex: IOException) {

            throw MPXJException(MPXJException.READ_ERROR, ex)

        }

    }

    /**
     * Alternative entry point allowing an MPP file to be read from
     * a user-supplied POI file stream.
     *
     * @param fs POI file stream
     * @return ProjectFile instance
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    fun read(fs: POIFSFileSystem): ProjectFile {
        try {
            val projectFile = ProjectFile()
            val config = projectFile.projectConfig

            config.autoTaskID = false
            config.autoTaskUniqueID = false
            config.autoResourceID = false
            config.autoResourceUniqueID = false
            config.autoOutlineLevel = false
            config.autoOutlineNumber = false
            config.autoWBS = false
            config.autoCalendarUniqueID = false
            config.autoAssignmentUniqueID = false

            projectFile.eventManager.addProjectListeners(m_projectListeners)

            //
            // Open the file system and retrieve the root directory
            //
            val root = fs.root

            //
            // Retrieve the CompObj data, validate the file format and process
            //
            val compObj = CompObj(DocumentInputStream(root.getEntry("\u0001CompObj") as DocumentEntry))
            val projectProperties = projectFile.projectProperties
            projectProperties.fullApplicationName = compObj.applicationName
            projectProperties.applicationVersion = compObj.applicationVersion
            val format = compObj.fileFormat
            val readerClass = FILE_CLASS_MAP.get(format)
                    ?: throw MPXJException(MPXJException.INVALID_FILE.toString() + ": " + format)
            val reader = readerClass.newInstance()
            reader.process(this, projectFile, root)

            //
            // Update the internal structure. We'll take this opportunity to
            // generate outline numbers for the tasks as they don't appear to
            // be present in the MPP file.
            //
            config.autoOutlineNumber = true
            projectFile.updateStructure()
            config.autoOutlineNumber = false

            //
            // Perform post-processing to set the summary flag and clean
            // up any instances where a task has an empty splits list.
            //
            for (task in projectFile.tasks) {
                task.summary = task.hasChildTasks()
                val splits = task.splits
                if (splits != null && splits!!.isEmpty()) {
                    task.splits = null
                }
                validationRelations(task)
            }

            //
            // Ensure that the unique ID counters are correct
            //
            config.updateUniqueCounters()

            //
            // Add some analytics
            //
            val projectFilePath = projectFile.projectProperties.projectFilePath
            if (projectFilePath != null && projectFilePath.startsWith("<>\\")) {
                projectProperties.fileApplication = "Microsoft Project Server"
            } else {
                projectProperties.fileApplication = "Microsoft"
            }
            projectProperties.fileType = "MPP"

            return projectFile
        } catch (ex: IOException) {
            throw MPXJException(MPXJException.READ_ERROR, ex)
        } catch (ex: IllegalAccessException) {
            throw MPXJException(MPXJException.READ_ERROR, ex)
        } catch (ex: InstantiationException) {
            throw MPXJException(MPXJException.READ_ERROR, ex)
        }

    }

    /**
     * This method validates all relationships for a task, removing
     * any which have been incorrectly read from the MPP file and
     * point to a parent task.
     *
     * @param task task under test
     */
    private fun validationRelations(task: Task) {
        val predecessors = task.predecessors
        if (!predecessors.isEmpty()) {
            val invalid = ArrayList<Relation>()
            for (relation in predecessors) {
                val sourceTask = relation.sourceTask
                val targetTask = relation.targetTask

                val sourceOutlineNumber = sourceTask.outlineNumber
                val targetOutlineNumber = targetTask.outlineNumber

                if (sourceOutlineNumber != null && targetOutlineNumber != null && sourceOutlineNumber!!.startsWith(targetOutlineNumber!! + '.')) {
                    invalid.add(relation)
                }
            }

            for (relation in invalid) {
                relation.sourceTask.removePredecessor(relation.targetTask, relation.type, relation.lag)
            }
        }
    }

    companion object {

        /**
         * This method allows us to peek into the OLE compound document to extract the file format.
         * This allows the UniversalProjectReader to determine if this is an MPP file, or if
         * it is another type of OLE compound document.
         *
         * @param fs POIFSFileSystem instance
         * @return file format name
         * @throws IOException
         */
        @Throws(IOException::class)
        fun getFileFormat(fs: POIFSFileSystem): String? {
            var fileFormat: String? = ""
            val root = fs.root
            if (root.entryNames.contains("\u0001CompObj")) {
                val compObj = CompObj(DocumentInputStream(root.getEntry("\u0001CompObj") as DocumentEntry))
                fileFormat = compObj.fileFormat
            }
            return fileFormat
        }

        /**
         * Populate a map of file types and file processing classes.
         */
        private val FILE_CLASS_MAP = HashMap<String, Class<out MPPVariantReader>>()

        init {
            FILE_CLASS_MAP.put("MSProject.MPP9", MPP9Reader::class.java)
            FILE_CLASS_MAP.put("MSProject.MPT9", MPP9Reader::class.java)
            FILE_CLASS_MAP.put("MSProject.GLOBAL9", MPP9Reader::class.java)
            FILE_CLASS_MAP.put("MSProject.MPP8", MPP8Reader::class.java)
            FILE_CLASS_MAP.put("MSProject.MPT8", MPP8Reader::class.java)
            FILE_CLASS_MAP.put("MSProject.MPP12", MPP12Reader::class.java)
            FILE_CLASS_MAP.put("MSProject.MPT12", MPP12Reader::class.java)
            FILE_CLASS_MAP.put("MSProject.GLOBAL12", MPP12Reader::class.java)
            FILE_CLASS_MAP.put("MSProject.MPP14", MPP14Reader::class.java)
            FILE_CLASS_MAP.put("MSProject.MPT14", MPP14Reader::class.java)
            FILE_CLASS_MAP.put("MSProject.GLOBAL14", MPP14Reader::class.java)
        }
    }
}
