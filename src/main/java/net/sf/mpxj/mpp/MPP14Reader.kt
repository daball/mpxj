/*
 * file:       MPP14Reader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2010
 * date:       20/01/2010
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

import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.Collections
import java.util.Date
import java.util.HashMap
import java.util.LinkedList
import java.util.TreeMap

import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream

import net.sf.mpxj.AssignmentField
import net.sf.mpxj.DateRange
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.FieldContainer
import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectCalendar
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Resource
import net.sf.mpxj.ResourceField
import net.sf.mpxj.ResourceType
import net.sf.mpxj.SubProject
import net.sf.mpxj.Table
import net.sf.mpxj.TableContainer
import net.sf.mpxj.Task
import net.sf.mpxj.TaskField
import net.sf.mpxj.TaskMode
import net.sf.mpxj.View
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.RtfHelper

/**
 * This class is used to represent a Microsoft Project MPP14 file. This
 * implementation allows the file to be read, and the data it contains
 * exported as a set of MPX objects. These objects can be interrogated
 * to retrieve any required data, or stored as an MPX file.
 */
internal class MPP14Reader : MPPVariantReader {

    //   private static void dumpUnknownData (String label, int[][] spec, byte[] data)
    //   {
    //      System.out.print (label);
    //      for (int loop=0; loop < spec.length; loop++)
    //      {
    //         int startByte = spec[loop][0];
    //         int length = spec[loop][1];
    //         if (length == -1)
    //         {
    //            length = data.length - startByte;
    //         }
    //         System.out.print ("["+spec[loop][0] + "]["+ ByteArrayHelper.hexdump(data, startByte, length, false) + " ]");
    //      }
    //      System.out.println ();
    //   }

    //   private static final int[][] UNKNOWN_TASK_DATA = new int[][]
    //   {
    //      {42, 18},
    //      {116, 4},
    //      {134, 2},
    //      {144, 4},
    //      {144, 16},
    //      {248, 8},
    //      {256, -1}
    //   };

    //   private static final int[][] UNKNOWN_RESOURCE_DATA = new int[][]
    //   {
    //      {14, 6},
    //      {108, 16},
    //   };

    private var m_reader: MPPReader? = null
    private var m_file: ProjectFile? = null
    private var m_eventManager: EventManager? = null
    private var m_root: DirectoryEntry? = null
    private var m_resourceMap: HashMap<Integer, ProjectCalendar>? = null
    private var m_outlineCodeVarData: Var2Data? = null
    private var m_outlineCodeVarMeta: VarMeta? = null
    private var m_outlineCodeFixedData: FixedData? = null
    private var m_outlineCodeFixedMeta: FixedMeta? = null
    private var m_outlineCodeFixedData2: FixedData? = null
    private var m_outlineCodeFixedMeta2: FixedMeta? = null
    private var m_projectProps: Props14? = null
    private var m_fontBases: Map<Integer, FontBase>? = null
    private var m_taskSubProjects: Map<Integer, SubProject>? = null
    private var m_projectDir: DirectoryEntry? = null
    private var m_viewDir: DirectoryEntry? = null
    private var m_parentTasks: Map<Integer, Integer>? = null
    private var m_taskOrder: Map<Long, Integer>? = null
    private var m_nullTaskOrder: Map<Integer, Integer>? = null
    private var m_inputStreamFactory: DocumentInputStreamFactory? = null
    /**
     * This method is used to process an MPP14 file. This is the file format
     * used by Project 14.
     *
     * @param reader parent file reader
     * @param file parent MPP file
     * @param root Root of the POI file system.
     */
    @Override
    @Throws(MPXJException::class, IOException::class)
    override fun process(reader: MPPReader, file: ProjectFile, root: DirectoryEntry) {
        try {
            populateMemberData(reader, file, root)
            processProjectProperties()

            if (!reader.readPropertiesOnly) {
                processSubProjectData()
                processGraphicalIndicators()
                processCustomValueLists()
                processCalendarData()
                processResourceData()
                processTaskData()
                processConstraintData()
                processAssignmentData()
                postProcessTasks()

                if (reader.readPresentationData) {
                    processViewPropertyData()
                    processTableData()
                    processViewData()
                    processFilterData()
                    processGroupData()
                    processSavedViewState()
                }
            }
        } finally {
            clearMemberData()
        }
    }

    /**
     * Populate member data used by the rest of the reader.
     *
     * @param reader parent file reader
     * @param file parent MPP file
     * @param root Root of the POI file system.
     */
    @Throws(FileNotFoundException::class, IOException::class, MPXJException::class)
    private fun populateMemberData(reader: MPPReader, file: ProjectFile, root: DirectoryEntry) {
        m_reader = reader
        m_file = file
        m_eventManager = file.eventManager
        m_root = root

        //
        // Retrieve the high level document properties
        //
        val props14 = Props14(DocumentInputStream(root.getEntry("Props14") as DocumentEntry))
        //System.out.println(props14);

        file.projectProperties.projectFilePath = props14.getUnicodeString(Props.PROJECT_FILE_PATH)
        m_inputStreamFactory = DocumentInputStreamFactory(props14)

        //
        // Test for password protection. In the single byte retrieved here:
        //
        // 0x00 = no password
        // 0x01 = protection password has been supplied
        // 0x02 = write reservation password has been supplied
        // 0x03 = both passwords have been supplied
        //
        if (props14.getByte(Props.PASSWORD_FLAG) and 0x01 != 0) {
            // Couldn't figure out how to get the password for MPP14 files so for now we just need to block the reading
            throw MPXJException(MPXJException.PASSWORD_PROTECTED)
        }

        m_resourceMap = HashMap<Integer, ProjectCalendar>()
        m_projectDir = root.getEntry("   114") as DirectoryEntry
        m_viewDir = root.getEntry("   214") as DirectoryEntry
        val outlineCodeDir = m_projectDir!!.getEntry("TBkndOutlCode") as DirectoryEntry
        m_outlineCodeVarMeta = VarMeta12(DocumentInputStream(outlineCodeDir.getEntry("VarMeta") as DocumentEntry))
        m_outlineCodeVarData = Var2Data(m_outlineCodeVarMeta!!, DocumentInputStream(outlineCodeDir.getEntry("Var2Data") as DocumentEntry))
        m_outlineCodeFixedMeta = FixedMeta(DocumentInputStream(outlineCodeDir.getEntry("FixedMeta") as DocumentEntry), 10)
        m_outlineCodeFixedData = FixedData(m_outlineCodeFixedMeta, DocumentInputStream(outlineCodeDir.getEntry("FixedData") as DocumentEntry))
        m_outlineCodeFixedMeta2 = FixedMeta(DocumentInputStream(outlineCodeDir.getEntry("Fixed2Meta") as DocumentEntry), 10)
        m_outlineCodeFixedData2 = FixedData(m_outlineCodeFixedMeta2, DocumentInputStream(outlineCodeDir.getEntry("Fixed2Data") as DocumentEntry))
        m_projectProps = Props14(m_inputStreamFactory!!.getInstance(m_projectDir!!, "Props"))
        //MPPUtility.fileDump("c:\\temp\\props.txt", m_projectProps.toString().getBytes());
        //FieldMap fm = new FieldMap14(m_file);
        //fm.dumpKnownFieldMaps(m_projectProps);

        m_fontBases = HashMap<Integer, FontBase>()
        m_taskSubProjects = HashMap<Integer, SubProject>()
        m_parentTasks = HashMap<Integer, Integer>()
        m_taskOrder = TreeMap<Long, Integer>()
        m_nullTaskOrder = TreeMap<Integer, Integer>()

        m_file!!.projectProperties.mppFileType = Integer.valueOf(14)
        m_file!!.projectProperties.autoFilter = props14.getBoolean(Props.AUTO_FILTER)
    }

    /**
     * Clear transient member data.
     */
    private fun clearMemberData() {
        m_reader = null
        m_eventManager = null
        m_file = null
        m_root = null
        m_resourceMap = null
        m_projectDir = null
        m_viewDir = null
        m_outlineCodeVarData = null
        m_outlineCodeVarMeta = null
        m_projectProps = null
        m_fontBases = null
        m_taskSubProjects = null
        m_parentTasks = null
        m_taskOrder = null
        m_nullTaskOrder = null
    }

    /**
     * This method extracts and collates the value list information
     * for custom column value lists.
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processCustomValueLists() {
        val taskDir = m_projectDir!!.getEntry("TBkndTask") as DirectoryEntry
        if (taskDir.hasEntry("Props")) {
            val taskProps = Props14(m_inputStreamFactory!!.getInstance(taskDir, "Props"))

            val reader = CustomFieldValueReader14(m_file!!.projectProperties, m_file!!.customFields, m_outlineCodeVarMeta, m_outlineCodeVarData, m_outlineCodeFixedData, m_outlineCodeFixedData2, taskProps)
            reader.process()
        }
    }

    /**
     * Process the project properties data.
     */
    @Throws(MPXJException::class)
    private fun processProjectProperties() {
        val reader = ProjectPropertiesReader()
        reader.process(m_file, m_projectProps, m_root)
    }

    /**
     * Process the graphical indicator data.
     */
    private fun processGraphicalIndicators() {
        val graphicalIndicatorReader = GraphicalIndicatorReader()
        graphicalIndicatorReader.process(m_file!!.customFields, m_file!!.projectProperties, m_projectProps!!)
    }

    /**
     * Read sub project data from the file, and add it to a hash map
     * indexed by task ID.
     *
     * Project stores all subprojects that have ever been inserted into this project
     * in sequence and that is what used to count unique id offsets for each of the
     * subprojects.
     */
    private fun processSubProjectData() {
        val subProjData = m_projectProps!!.getByteArray(Props.SUBPROJECT_DATA)

        //System.out.println (ByteArrayHelper.hexdump(subProjData, true, 16, ""));
        //MPPUtility.fileHexDump("c:\\temp\\dump.txt", subProjData);

        if (subProjData != null) {
            var index = 0
            var offset = 0
            var itemHeaderOffset: Int
            var uniqueIDOffset: Int
            var filePathOffset: Int
            var fileNameOffset: Int

            val itemHeader = ByteArray(20)

            /*int blockSize = MPPUtility.getInt(subProjData, offset);*/
            offset += 4

            /*int unknown = MPPUtility.getInt(subProjData, offset);*/
            offset += 4

            val itemCountOffset = MPPUtility.getInt(subProjData, offset)
            offset += 4

            while (offset < itemCountOffset) {
                index++
                itemHeaderOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                offset += 4

                MPPUtility.getByteArray(subProjData, itemHeaderOffset, itemHeader.size, itemHeader, 0)
                val subProjectType = itemHeader[16]

                //            System.out.println();
                //            System.out.println (ByteArrayHelper.hexdump(itemHeader, false, 16, ""));
                //            System.out.println(ByteArrayHelper.hexdump(subProjData, offset, 16, false));
                //            System.out.println("Offset1: " + (MPPUtility.getInt(subProjData, offset) & 0x1FFFF));
                //            System.out.println("Offset2: " + (MPPUtility.getInt(subProjData, offset+4) & 0x1FFFF));
                //            System.out.println("Offset3: " + (MPPUtility.getInt(subProjData, offset+8) & 0x1FFFF));
                //            System.out.println("Offset4: " + (MPPUtility.getInt(subProjData, offset+12) & 0x1FFFF));
                //            System.out.println ("Offset: " + offset);
                //            System.out.println ("Item Header Offset: " + itemHeaderOffset);
                //            System.out.println("SubProjectType: " + Integer.toHexString(subProjectType));
                when (subProjectType) {
                    //
                    // Subproject that is no longer inserted. This is a placeholder in order to be
                    // able to always guarantee unique unique ids.
                    //
                    0x00,
                        //
                        // deleted entry?
                        //
                    0x10 -> {
                        offset += 8
                    }

                    //
                    // task unique ID, 8 bytes, path, file name
                    //
                    0x99.toByte(), 0x09, 0x0D -> {
                        uniqueIDOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        // sometimes offset of a task ID?
                        offset += 4

                        filePathOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        fileNameOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        readSubProjects(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // task unique ID, 8 bytes, path, file name
                    //
                    0x03, 0x11, 0x91.toByte() -> {
                        uniqueIDOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        filePathOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        fileNameOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        // Unknown offset
                        offset += 4

                        readSubProjects(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // task unique ID, path, unknown, file name
                    //
                    0x81.toByte(), 0x83.toByte(), 0x41 -> {
                        uniqueIDOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        filePathOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        // unknown offset to 2 bytes of data?
                        offset += 4

                        fileNameOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        readSubProjects(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // task unique ID, path, file name
                    //
                    0x01, 0x08 -> {
                        uniqueIDOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        filePathOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        fileNameOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        readSubProjects(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // task unique ID, path, file name
                    //
                    0xC0.toByte() -> {
                        uniqueIDOffset = itemHeaderOffset

                        filePathOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        fileNameOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        // unknown offset
                        offset += 4

                        readSubProjects(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // resource, task unique ID, path, file name
                    //
                    0x05 -> {
                        uniqueIDOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        filePathOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        fileNameOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        m_file!!.subProjects.resourceSubProject = readSubProject(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    0x45 -> {
                        uniqueIDOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        filePathOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        fileNameOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        offset += 4

                        m_file!!.subProjects.resourceSubProject = readSubProject(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // path, file name
                    //
                    0x02 -> {
                        //filePathOffset = MPPUtility.getInt(subProjData, offset) & 0x1FFFF;
                        offset += 4

                        //fileNameOffset = MPPUtility.getInt(subProjData, offset) & 0x1FFFF;
                        offset += 4
                    }//sp = readSubProject(subProjData, -1, filePathOffset, fileNameOffset, index);
                    // 0x02 looks to be the link FROM the resource pool to a project that is using it.

                    0x04 -> {
                        filePathOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        fileNameOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        m_file!!.subProjects.resourceSubProject = readSubProject(subProjData, -1, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // task unique ID, 4 bytes, path, 4 bytes, file name
                    //
                    0x8D.toByte() -> {
                        uniqueIDOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 8

                        filePathOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 8

                        fileNameOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        readSubProjects(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // task unique ID, path, file name
                    //
                    0x0A -> {
                        uniqueIDOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        filePathOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        fileNameOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        readSubProjects(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    // new resource pool entry
                    0x44.toByte() -> {
                        filePathOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        offset += 4

                        fileNameOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        readSubProjects(subProjData, -1, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // Appears when a subproject is collapsed
                    //
                    0x80.toByte() -> {
                        offset += 12
                    }

                    //
                    // Any other value, assume 12 bytes to handle old/deleted data?
                    //
                    else -> {
                        offset += 12
                    }
                }
            }
        }
    }

    /**
     * Read a list of sub projects.
     *
     * @param data byte array
     * @param uniqueIDOffset offset of unique ID
     * @param filePathOffset offset of file path
     * @param fileNameOffset offset of file name
     * @param subprojectIndex index of the subproject, used to calculate unique id offset
     */
    private fun readSubProjects(data: ByteArray, uniqueIDOffset: Int, filePathOffset: Int, fileNameOffset: Int, subprojectIndex: Int) {
        var uniqueIDOffset = uniqueIDOffset
        var subprojectIndex = subprojectIndex
        while (uniqueIDOffset < filePathOffset) {
            readSubProject(data, uniqueIDOffset, filePathOffset, fileNameOffset, subprojectIndex++)
            uniqueIDOffset += 4
        }
    }

    /**
     * Method used to read the sub project details from a byte array.
     *
     * @param data byte array
     * @param uniqueIDOffset offset of unique ID
     * @param filePathOffset offset of file path
     * @param fileNameOffset offset of file name
     * @param subprojectIndex index of the subproject, used to calculate unique id offset
     * @return new SubProject instance
     */
    private fun readSubProject(data: ByteArray, uniqueIDOffset: Int, filePathOffset: Int, fileNameOffset: Int, subprojectIndex: Int): SubProject? {
        var filePathOffset = filePathOffset
        var fileNameOffset = fileNameOffset
        try {
            val sp = SubProject()
            var type = SUBPROJECT_TASKUNIQUEID0

            if (uniqueIDOffset != -1) {
                var value = MPPUtility.getInt(data, uniqueIDOffset)
                type = MPPUtility.getInt(data, uniqueIDOffset + 4)
                when (type) {
                    SUBPROJECT_TASKUNIQUEID0, SUBPROJECT_TASKUNIQUEID1, SUBPROJECT_TASKUNIQUEID2, SUBPROJECT_TASKUNIQUEID3, SUBPROJECT_TASKUNIQUEID4, SUBPROJECT_TASKUNIQUEID5, SUBPROJECT_TASKUNIQUEID6, SUBPROJECT_TASKUNIQUEID7, SUBPROJECT_TASKUNIQUEID8 -> {
                        sp.taskUniqueID = Integer.valueOf(value)
                        m_taskSubProjects!!.put(sp.taskUniqueID, sp)
                    }

                    else -> {
                        if (value != 0) {
                            sp.addExternalTaskUniqueID(Integer.valueOf(value))
                            m_taskSubProjects!!.put(Integer.valueOf(value), sp)
                        }
                    }
                }

                // Now get the unique id offset for this subproject
                value = 0x00800000 + (subprojectIndex - 1) * 0x00400000
                sp.uniqueIDOffset = Integer.valueOf(value)
            }

            if (type == SUBPROJECT_TASKUNIQUEID4) {
                sp.fullPath = MPPUtility.getUnicodeString(data, filePathOffset)
            } else {
                //
                // First block header
                //
                filePathOffset += 18

                //
                // String size as a 4 byte int
                //
                filePathOffset += 4

                //
                // Full DOS path
                //
                sp.dosFullPath = MPPUtility.getString(data, filePathOffset)
                filePathOffset += sp.dosFullPath!!.length() + 1

                //
                // 24 byte block
                //
                filePathOffset += 24

                //
                // 4 byte block size
                //
                var size = MPPUtility.getInt(data, filePathOffset)
                filePathOffset += 4
                if (size == 0) {
                    sp.fullPath = sp.dosFullPath
                } else {
                    //
                    // 4 byte unicode string size in bytes
                    //
                    size = MPPUtility.getInt(data, filePathOffset)
                    filePathOffset += 4

                    //
                    // 2 byte data
                    //
                    filePathOffset += 2

                    //
                    // Unicode string
                    //
                    sp.fullPath = MPPUtility.getUnicodeString(data, filePathOffset, size)
                    //filePathOffset += size;
                }

                //
                // Second block header
                //
                fileNameOffset += 18

                //
                // String size as a 4 byte int
                //
                fileNameOffset += 4

                //
                // DOS file name
                //
                sp.dosFileName = MPPUtility.getString(data, fileNameOffset)
                fileNameOffset += sp.dosFileName!!.length() + 1

                //
                // 24 byte block
                //
                fileNameOffset += 24

                //
                // 4 byte block size
                //
                size = MPPUtility.getInt(data, fileNameOffset)
                fileNameOffset += 4

                if (size == 0) {
                    sp.fileName = sp.dosFileName
                } else {
                    //
                    // 4 byte unicode string size in bytes
                    //
                    size = MPPUtility.getInt(data, fileNameOffset)
                    fileNameOffset += 4

                    //
                    // 2 byte data
                    //
                    fileNameOffset += 2

                    //
                    // Unicode string
                    //
                    sp.fileName = MPPUtility.getUnicodeString(data, fileNameOffset, size)
                    //fileNameOffset += size;
                }
            }

            //System.out.println(sp.toString());

            // Add to the list of subprojects
            m_file!!.subProjects.add(sp)

            return sp
        } catch (ex: ArrayIndexOutOfBoundsException) {
            return null
        }
        //
        // Admit defeat at this point - we have probably stumbled
        // upon a data format we don't understand, so we'll fail
        // gracefully here. This will now be reported as a missing
        // sub project error by end users of the library, rather
        // than as an exception being thrown.
        //
    }

    /**
     * This method process the data held in the props file specific to the
     * visual appearance of the project data.
     */
    @Throws(IOException::class)
    private fun processViewPropertyData() {
        val props = Props14(m_inputStreamFactory!!.getInstance(m_viewDir!!, "Props"))
        val data = props.getByteArray(Props.FONT_BASES)
        if (data != null) {
            processBaseFonts(data)
        }

        val properties = m_file!!.projectProperties
        properties.showProjectSummaryTask = props.getBoolean(Props.SHOW_PROJECT_SUMMARY_TASK)
    }

    /**
     * Create an index of base font numbers and their associated base
     * font instances.
     * @param data property data
     */
    private fun processBaseFonts(data: ByteArray) {
        var offset = 0

        val blockCount = MPPUtility.getShort(data, 0)
        offset += 2

        var size: Int
        var name: String

        for (loop in 0 until blockCount) {
            /*unknownAttribute = MPPUtility.getShort(data, offset);*/
            offset += 2

            size = MPPUtility.getShort(data, offset)
            offset += 2

            name = MPPUtility.getUnicodeString(data, offset)
            offset += 64

            if (name.length() !== 0) {
                val fontBase = FontBase(Integer.valueOf(loop), name, size)
                m_fontBases!!.put(fontBase.index, fontBase)
            }
        }
    }

    /**
     * This method maps the task unique identifiers to their index number
     * within the FixedData block.
     *
     * @param fieldMap field map
     * @param taskFixedMeta Fixed meta data for this task
     * @param taskFixedData Fixed data for this task
     * @param taskVarData Variable task data
     * @return Mapping between task identifiers and block position
     */
    private fun createTaskMap(fieldMap: FieldMap, taskFixedMeta: FixedMeta, taskFixedData: FixedData, taskVarData: Var2Data): TreeMap<Integer, Integer> {
        val taskMap = TreeMap<Integer, Integer>()
        val uniqueIdOffset = fieldMap.getFixedDataOffset(TaskField.UNIQUE_ID)
        val taskNameKey = fieldMap.getVarDataKey(TaskField.NAME)
        val itemCount = taskFixedMeta.adjustedItemCount
        var uniqueID: Int
        var key: Integer

        //
        // First three items are not tasks, so let's skip them
        //
        for (loop in 3 until itemCount) {
            val data = taskFixedData.getByteArrayValue(loop)
            if (data != null) {
                val metaData = taskFixedMeta.getByteArrayValue(loop)

                //
                // Check for the deleted task flag
                //
                val flags = MPPUtility.getInt(metaData, 0)
                if (flags and 0x02 != 0) {
                    // Project stores the deleted tasks unique id's into the fixed data as well
                    // and at least in one case the deleted task was listed twice in the list
                    // the second time with data with it causing a phantom task to be shown.
                    // See CalendarErrorPhantomTasks.mpp
                    //
                    // So let's add the unique id for the deleted task into the map so we don't
                    // accidentally include the task later.
                    //
                    uniqueID = MPPUtility.getShort(data, TASK_UNIQUE_ID_FIXED_OFFSET) // Only a short stored for deleted tasks?
                    key = Integer.valueOf(uniqueID)
                    if (taskMap.containsKey(key) === false) {
                        taskMap.put(key, null) // use null so we can easily ignore this later
                    }
                } else {
                    //
                    // Do we have a null task?
                    //
                    if (data.size == NULL_TASK_BLOCK_SIZE) {
                        uniqueID = MPPUtility.getInt(data, TASK_UNIQUE_ID_FIXED_OFFSET)
                        key = Integer.valueOf(uniqueID)
                        if (taskMap.containsKey(key) === false) {
                            taskMap.put(key, Integer.valueOf(loop))
                        }
                    } else {
                        //
                        // We apply a heuristic here - if we have more than 75% of the data, we assume
                        // the task is valid.
                        //
                        val maxSize = fieldMap.getMaxFixedDataSize(0)
                        if (maxSize == 0 || data.size * 100 / maxSize > 75) {
                            uniqueID = MPPUtility.getInt(data, uniqueIdOffset)
                            key = Integer.valueOf(uniqueID)

                            // Accept this task if it does not have a deleted unique ID or it has a deleted unique ID but the name is not null
                            if (!taskMap.containsKey(key) || taskVarData.getUnicodeString(key, taskNameKey) != null) {
                                taskMap.put(key, Integer.valueOf(loop))
                            }
                        }
                    }
                }
            }
        }

        return taskMap
    }

    /**
     * This method maps the resource unique identifiers to their index number
     * within the FixedData block.
     *
     * @param fieldMap field map
     * @param rscFixedMeta resource fixed meta data
     * @param rscFixedData resource fixed data
     * @return map of resource IDs to resource data
     */
    private fun createResourceMap(fieldMap: FieldMap, rscFixedMeta: FixedMeta, rscFixedData: FixedData): TreeMap<Integer, Integer> {
        val resourceMap = TreeMap<Integer, Integer>()
        val itemCount = rscFixedMeta.adjustedItemCount
        val maxFixedDataSize = fieldMap.getMaxFixedDataSize(0)
        val uniqueIdOffset = fieldMap.getFixedDataOffset(ResourceField.UNIQUE_ID)

        for (loop in 0 until itemCount) {
            val data = rscFixedData.getByteArrayValue(loop)
            if (data == null || data.size < maxFixedDataSize) {
                continue
            }

            val uniqueID = Integer.valueOf(MPPUtility.getShort(data, uniqueIdOffset))
            if (resourceMap.containsKey(uniqueID) === false) {
                resourceMap.put(uniqueID, Integer.valueOf(loop))
            }
        }

        return resourceMap
    }

    /**
     * The format of the calendar data is a 4 byte header followed
     * by 7x 60 byte blocks, one for each day of the week. Optionally
     * following this is a set of 64 byte blocks representing exceptions
     * to the calendar.
     */
    @Throws(IOException::class)
    private fun processCalendarData() {
        val factory = MPP14CalendarFactory(m_file)
        factory.processCalendarData(m_projectDir, m_projectProps, m_inputStreamFactory, m_resourceMap)
    }

    /**
     * This method extracts and collates task data. The code below
     * goes through the modifier methods of the Task class in alphabetical
     * order extracting the data from the MPP file. Where there is no
     * mapping (e.g. the field is calculated on the fly, or we can't
     * find it in the data) the line is commented out.
     *
     * The missing boolean attributes are probably represented in the Props
     * section of the task data, which we have yet to decode.
     *
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    private fun processTaskData() {
        val fieldMap = FieldMap14(m_file!!.projectProperties, m_file!!.customFields)
        fieldMap.createTaskFieldMap(m_projectProps)

        val enterpriseCustomFieldMap = FieldMap14(m_file!!.projectProperties, m_file!!.customFields)
        enterpriseCustomFieldMap.createEnterpriseCustomFieldMap(m_projectProps, TaskField::class.java)

        val taskDir = m_projectDir!!.getEntry("TBkndTask") as DirectoryEntry
        val taskVarMeta = VarMeta12(DocumentInputStream(taskDir.getEntry("VarMeta") as DocumentEntry))
        val taskVarData = Var2Data(taskVarMeta, DocumentInputStream(taskDir.getEntry("Var2Data") as DocumentEntry))
        val taskFixedMeta = FixedMeta(DocumentInputStream(taskDir.getEntry("FixedMeta") as DocumentEntry), 47)
        val taskFixedData = FixedData(taskFixedMeta, DocumentInputStream(taskDir.getEntry("FixedData") as DocumentEntry), fieldMap.getMaxFixedDataSize(0))
        val taskFixed2Meta = FixedMeta(DocumentInputStream(taskDir.getEntry("Fixed2Meta") as DocumentEntry), taskFixedData, 92, 93, 94)
        val taskFixed2Data = FixedData(taskFixed2Meta, DocumentInputStream(taskDir.getEntry("Fixed2Data") as DocumentEntry))

        //      System.out.println(taskFixedMeta);
        //      System.out.println(taskFixedData);
        //      System.out.println(taskVarMeta);
        //      System.out.println(taskVarData);
        //      System.out.println(taskFixed2Meta);
        //      System.out.println(taskFixed2Data);
        //      System.out.println(m_outlineCodeVarData.getVarMeta());
        //      System.out.println(m_outlineCodeVarData);

        // Process aliases
        if (taskDir.hasEntry("Props")) {
            val props = Props14(m_inputStreamFactory!!.getInstance(taskDir, "Props"))
            CustomFieldAliasReader(m_file!!.customFields, props.getByteArray(TASK_FIELD_NAME_ALIASES)).process()
        }

        val taskMap = createTaskMap(fieldMap, taskFixedMeta, taskFixedData, taskVarData)
        // The var data may not contain all the tasks as tasks with no var data assigned will
        // not be saved in there. Most notably these are tasks with no name. So use the task map
        // which contains all the tasks.
        val uniqueIdArray = taskMap.keySet().toArray() //taskVarMeta.getUniqueIdentifierArray();
        var offset: Integer
        var data: ByteArray?
        var metaData: ByteArray?
        var metaData2: ByteArray?
        var task: Task
        var autoWBS = true
        val externalTasks = LinkedList<Task>()
        var recurringTaskReader: RecurringTaskReader? = null
        var notes: String?

        //
        // Select the correct meta data locations depending on
        // which version of Microsoft project generated this file
        //
        val metaDataBitFlags: Array<MppBitFlag>
        val metaData2BitFlags: Array<MppBitFlag>
        if (NumberHelper.getInt(m_file!!.projectProperties.applicationVersion) > ApplicationVersion.PROJECT_2010) {
            metaDataBitFlags = PROJECT2013_TASK_META_DATA_BIT_FLAGS
            metaData2BitFlags = PROJECT2013_TASK_META_DATA2_BIT_FLAGS
        } else {
            metaDataBitFlags = PROJECT2010_TASK_META_DATA_BIT_FLAGS
            metaData2BitFlags = PROJECT2010_TASK_META_DATA2_BIT_FLAGS
        }

        for (loop in uniqueIdArray.indices) {
            val uniqueID = uniqueIdArray[loop] as Integer

            offset = taskMap.get(uniqueID)
            if (taskFixedData.isValidOffset(offset) == false) {
                continue
            }

            data = taskFixedData.getByteArrayValue(offset.intValue())
            metaData = taskFixedMeta.getByteArrayValue(offset.intValue())
            val id = Integer.valueOf(MPPUtility.getInt(data, fieldMap.getFixedDataOffset(TaskField.ID)))

            if (data!!.size == NULL_TASK_BLOCK_SIZE) {
                task = m_file!!.addTask()
                task.`null` = true
                task.uniqueID = Integer.valueOf(MPPUtility.getInt(data, TASK_UNIQUE_ID_FIXED_OFFSET))
                task.id = Integer.valueOf(MPPUtility.getInt(data, TASK_ID_FIXED_OFFSET))
                m_nullTaskOrder!!.put(task.id, task.uniqueID)
                continue
            }

            if (data!!.size < fieldMap.getMaxFixedDataSize(0)) {
                val newData = ByteArray(fieldMap.getMaxFixedDataSize(0) + 8)
                System.arraycopy(data, 0, newData, 0, data!!.size)
                data = newData
            }

            //System.out.println (ByteArrayHelper.hexdump(data, false, 16, ""));
            //System.out.println (ByteArrayHelper.hexdump(data,false));
            //System.out.println (ByteArrayHelper.hexdump(metaData, false, 16, ""));
            //MPPUtility.dataDump(m_file, data, false, false, false, true, false, false, false, false);
            //MPPUtility.dataDump(metaData, true, true, true, true, true, true, true);
            //MPPUtility.varDataDump(taskVarData, id, true, true, true, true, true, true);

            metaData2 = taskFixed2Meta.getByteArrayValue(offset.intValue())
            val data2 = taskFixed2Data.getByteArrayValue(offset.intValue())
            //System.out.println (ByteArrayHelper.hexdump(metaData2, false, 16, ""));
            //System.out.println(ByteArrayHelper.hexdump(data2, false, 16, ""));
            //System.out.println (ByteArrayHelper.hexdump(metaData2,false));

            val recurringData = taskVarData.getByteArray(uniqueID, fieldMap.getVarDataKey(TaskField.RECURRING_DATA))

            val temp = m_file!!.getTaskByID(id)
            if (temp != null) {
                // Task with this id already exists... determine if this is the 'real' task by seeing
                // if this task has some var data. This is sort of hokey, but it's the best method i have
                // been able to see.
                if (!taskVarMeta.uniqueIdentifierSet.contains(uniqueID)) {
                    // Sometimes Project contains phantom tasks that coexist on the same id as a valid
                    // task. In this case don't want to include the phantom task. Seems to be a very rare case.
                    continue
                } else if (temp.name == null) {
                    // Ok, this looks valid. Remove the previous instance since it is most likely not a valid task.
                    // At worst case this removes a task with an empty name.
                    m_file!!.removeTask(temp)
                }
            }
            task = m_file!!.addTask()

            task.disableEvents()

            fieldMap.populateContainer(TaskField::class.java, task, uniqueID, arrayOf(data, data2), taskVarData)

            enterpriseCustomFieldMap.populateContainer(TaskField::class.java, task, uniqueID, null, taskVarData)

            task.enableEvents()

            task.estimated = getDurationEstimated(MPPUtility.getShort(data, fieldMap.getFixedDataOffset(TaskField.ACTUAL_DURATION_UNITS)))

            val externalTaskID = task.subprojectTaskID
            if (externalTaskID != null && externalTaskID.intValue() !== 0) {
                task.externalTask = true
                externalTasks.add(task)
            }

            processHyperlinkData(task, taskVarData.getByteArray(uniqueID, fieldMap.getVarDataKey(TaskField.HYPERLINK_DATA)))

            task.id = id

            task.setOutlineCode(1, getCustomFieldOutlineCodeValue(taskVarData, m_outlineCodeVarData, uniqueID, fieldMap.getVarDataKey(TaskField.OUTLINE_CODE1_INDEX)))
            task.setOutlineCode(2, getCustomFieldOutlineCodeValue(taskVarData, m_outlineCodeVarData, uniqueID, fieldMap.getVarDataKey(TaskField.OUTLINE_CODE2_INDEX)))
            task.setOutlineCode(3, getCustomFieldOutlineCodeValue(taskVarData, m_outlineCodeVarData, uniqueID, fieldMap.getVarDataKey(TaskField.OUTLINE_CODE3_INDEX)))
            task.setOutlineCode(4, getCustomFieldOutlineCodeValue(taskVarData, m_outlineCodeVarData, uniqueID, fieldMap.getVarDataKey(TaskField.OUTLINE_CODE4_INDEX)))
            task.setOutlineCode(5, getCustomFieldOutlineCodeValue(taskVarData, m_outlineCodeVarData, uniqueID, fieldMap.getVarDataKey(TaskField.OUTLINE_CODE5_INDEX)))
            task.setOutlineCode(6, getCustomFieldOutlineCodeValue(taskVarData, m_outlineCodeVarData, uniqueID, fieldMap.getVarDataKey(TaskField.OUTLINE_CODE6_INDEX)))
            task.setOutlineCode(7, getCustomFieldOutlineCodeValue(taskVarData, m_outlineCodeVarData, uniqueID, fieldMap.getVarDataKey(TaskField.OUTLINE_CODE7_INDEX)))
            task.setOutlineCode(8, getCustomFieldOutlineCodeValue(taskVarData, m_outlineCodeVarData, uniqueID, fieldMap.getVarDataKey(TaskField.OUTLINE_CODE8_INDEX)))
            task.setOutlineCode(9, getCustomFieldOutlineCodeValue(taskVarData, m_outlineCodeVarData, uniqueID, fieldMap.getVarDataKey(TaskField.OUTLINE_CODE9_INDEX)))
            task.setOutlineCode(10, getCustomFieldOutlineCodeValue(taskVarData, m_outlineCodeVarData, uniqueID, fieldMap.getVarDataKey(TaskField.OUTLINE_CODE10_INDEX)))

            task.recurring = MPPUtility.getShort(data, 40) == 2

            task.uniqueID = uniqueID

            task.expanded = metaData!![12] and 0x02 == 0
            readBitFields(metaDataBitFlags, task, metaData)
            readBitFields(metaData2BitFlags, task, metaData2)

            m_parentTasks!!.put(task.uniqueID, task.getCachedValue(TaskField.PARENT_TASK_UNIQUE_ID) as Integer)

            if (task.start == null || task.getCachedValue(TaskField.SCHEDULED_START) != null && task.taskMode === TaskMode.AUTO_SCHEDULED) {
                task.start = task.getCachedValue(TaskField.SCHEDULED_START) as Date
            }

            if (task.finish == null || task.getCachedValue(TaskField.SCHEDULED_FINISH) != null && task.taskMode === TaskMode.AUTO_SCHEDULED) {
                task.finish = task.getCachedValue(TaskField.SCHEDULED_FINISH) as Date
            }

            if (task.duration == null || task.getCachedValue(TaskField.SCHEDULED_DURATION) != null && task.taskMode === TaskMode.AUTO_SCHEDULED) {
                task.duration = task.getCachedValue(TaskField.SCHEDULED_DURATION) as Duration
            }

            when (task.constraintType) {
                //
                // Adjust the start and finish dates if the task
                // is constrained to start as late as possible.
                //
                AS_LATE_AS_POSSIBLE -> {
                    if (DateHelper.compare(task.start, task.lateStart) < 0) {
                        task.start = task.lateStart
                    }
                    if (DateHelper.compare(task.finish, task.lateFinish) < 0) {
                        task.finish = task.lateFinish
                    }
                }

                START_NO_LATER_THAN, FINISH_NO_LATER_THAN -> {
                    if (DateHelper.compare(task.finish, task.start) < 0) {
                        task.finish = task.lateFinish
                    }
                }

                else -> {
                }
            }

            //
            // Retrieve task recurring data
            //
            if (recurringData != null) {
                if (recurringTaskReader == null) {
                    recurringTaskReader = RecurringTaskReader(m_file!!.projectProperties)
                }
                recurringTaskReader.processRecurringTask(task, recurringData)
                task.recurring = true
            }

            //
            // Retrieve the task notes.
            //
            notes = task.notes
            if (m_reader!!.preserveNoteFormatting == false) {
                notes = RtfHelper.strip(notes)
            }

            task.notes = notes

            //
            // Set the calendar name
            //
            val calendarID = task.getCachedValue(TaskField.CALENDAR_UNIQUE_ID) as Integer
            if (calendarID != null && calendarID!!.intValue() !== -1) {
                val calendar = m_file!!.getCalendarByUniqueID(calendarID)
                if (calendar != null) {
                    task.calendar = calendar
                }
            }

            //
            // Set the sub project flag
            //
            val sp = m_taskSubProjects!![task.uniqueID]
            task.subProject = sp

            //
            // Set the external flag
            //
            if (sp != null) {
                task.externalTask = sp.isExternalTask(task.uniqueID)
                if (task.externalTask) {
                    task.externalTaskProject = sp.fullPath
                }
            }

            //
            // If we have a WBS value from the MPP file, don't autogenerate
            //
            if (task.wbs != null) {
                autoWBS = false
            }

            //
            // If this is a split task, allocate space for the split durations
            //
            if (metaData[9] and 0x80 == 0) {
                task.splits = LinkedList<DateRange>()
            }

            //
            // If this is a manually scheduled task, read the manual duration
            //
            if (task.taskMode !== TaskMode.MANUALLY_SCHEDULED) {
                task.manualDuration = null
            }

            //
            // Process any enterprise columns
            //
            processTaskEnterpriseColumns(uniqueID, task, taskVarData, metaData2)

            // Unfortunately it looks like 'null' tasks sometimes make it through. So let's check for to see if we
            // need to mark this task as a null task after all.
            if (task.name == null && (task.start == null || task.start.getTime() === MPPUtility.epochDate.getTime() || task.finish == null || task.finish.getTime() === MPPUtility.epochDate.getTime() || task.createDate == null || task.createDate.getTime() === MPPUtility.epochDate.getTime())) {
                // Remove this to avoid passing bad data to the client
                m_file!!.removeTask(task)

                task = m_file!!.addTask()
                task.`null` = true
                task.uniqueID = uniqueID
                task.id = id
                m_nullTaskOrder!!.put(task.id, task.uniqueID)
                //System.out.println(task);
                continue
            }

            if (data2 == null || data2!!.size < 24) {
                m_nullTaskOrder!!.put(task.id, task.uniqueID)
            } else {
                val key = Long.valueOf(MPPUtility.getLong(data2, 16))
                m_taskOrder!!.put(key, task.uniqueID)
            }

            //System.out.println(task + " " + MPPUtility.getShort(data2, 22)); // JPI - looks like this value determines the ID order! Validate and check other versions!
            m_eventManager!!.fireTaskReadEvent(task)
            //dumpUnknownData(task.getUniqueID().toString(), UNKNOWN_TASK_DATA, data);
            //System.out.println(task);
        }

        //
        // Enable auto WBS if necessary
        //
        m_file!!.projectConfig.autoWBS = autoWBS

        //
        // We have now read all of the task, so we are in a position
        // to perform post-processing to set up the relevant details
        // for each external task.
        //
        if (!externalTasks.isEmpty()) {
            processExternalTasks(externalTasks)
        }
    }

    /**
     * MPP14 files seem to exhibit some occasional weirdness
     * with duplicate ID values which leads to the task structure
     * being reported incorrectly. The following method attempts to correct this.
     * The method uses ordering data embedded in the file to reconstruct
     * the correct ID order of the tasks.
     */
    @Throws(MPXJException::class)
    private fun postProcessTasks() {
        //
        // Renumber ID values using a large increment to allow
        // space for later inserts.
        //
        val taskMap = TreeMap<Integer, Integer>()

        // I've found a pathological case of an MPP file with around 102k blank tasks...
        val nextIDIncrement = 102000
        var nextID = if (m_file!!.getTaskByUniqueID(Integer.valueOf(0)) == null) nextIDIncrement else 0
        for (entry in m_taskOrder!!.entrySet()) {
            taskMap.put(Integer.valueOf(nextID), entry.getValue())
            nextID += nextIDIncrement
        }

        //
        // Insert any null tasks into the correct location
        //
        var insertionCount = 0
        val offsetMap = HashMap<Integer, Integer>()
        for (entry in m_nullTaskOrder!!.entrySet()) {
            val idValue = entry.getKey().intValue()
            val baseTargetIdValue = (idValue - insertionCount) * nextIDIncrement
            var targetIDValue = baseTargetIdValue
            val previousOffsetKey = Integer.valueOf(baseTargetIdValue)
            val previousOffset = offsetMap.get(previousOffsetKey)
            var offset = if (previousOffset == null) 0 else previousOffset!!.intValue() + 1
            ++insertionCount

            while (taskMap.containsKey(Integer.valueOf(targetIDValue))) {
                ++offset
                if (offset == nextIDIncrement) {
                    throw MPXJException("Unable to fix task order")
                }
                targetIDValue = baseTargetIdValue - (nextIDIncrement - offset)
            }

            offsetMap.put(previousOffsetKey, Integer.valueOf(offset))
            taskMap.put(Integer.valueOf(targetIDValue), entry.getValue())
        }

        //
        // Finally, we can renumber the tasks
        //
        nextID = if (m_file!!.getTaskByUniqueID(Integer.valueOf(0)) == null) 1 else 0
        for (entry in taskMap.entrySet()) {
            val task = m_file!!.getTaskByUniqueID(entry.getValue())
            if (task != null) {
                task.id = Integer.valueOf(nextID)
            }
            nextID++
        }
    }

    /**
     * Extracts task enterprise column values.
     *
     * @param id task unique ID
     * @param task task instance
     * @param taskVarData task var data
     * @param metaData2 task meta data
     */
    private fun processTaskEnterpriseColumns(id: Integer, task: Task, taskVarData: Var2Data, metaData2: ByteArray?) {
        if (metaData2 != null) {
            val bits = MPPUtility.getInt(metaData2, 29)
            task.set(TaskField.ENTERPRISE_FLAG1, Boolean.valueOf(bits and 0x0000800 != 0))
            task.set(TaskField.ENTERPRISE_FLAG2, Boolean.valueOf(bits and 0x0001000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG3, Boolean.valueOf(bits and 0x0002000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG4, Boolean.valueOf(bits and 0x0004000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG5, Boolean.valueOf(bits and 0x0008000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG6, Boolean.valueOf(bits and 0x0001000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG7, Boolean.valueOf(bits and 0x0002000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG8, Boolean.valueOf(bits and 0x0004000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG9, Boolean.valueOf(bits and 0x0008000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG10, Boolean.valueOf(bits and 0x0010000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG11, Boolean.valueOf(bits and 0x0020000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG12, Boolean.valueOf(bits and 0x0040000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG13, Boolean.valueOf(bits and 0x0080000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG14, Boolean.valueOf(bits and 0x0100000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG15, Boolean.valueOf(bits and 0x0200000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG16, Boolean.valueOf(bits and 0x0400000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG17, Boolean.valueOf(bits and 0x0800000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG18, Boolean.valueOf(bits and 0x1000000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG19, Boolean.valueOf(bits and 0x2000000 != 0))
            task.set(TaskField.ENTERPRISE_FLAG20, Boolean.valueOf(bits and 0x4000000 != 0))
        }
    }

    /**
     * Extracts resource enterprise column data.
     *
     * @param id resource unique ID
     * @param resource resource instance
     * @param resourceVarData resource var data
     * @param metaData2 resource meta data
     */
    private fun processResourceEnterpriseColumns(id: Integer, resource: Resource, resourceVarData: Var2Data, metaData2: ByteArray?) {
        if (metaData2 != null) {
            var bits = MPPUtility.getInt(metaData2, 16)
            resource.set(ResourceField.ENTERPRISE_FLAG1, Boolean.valueOf(bits and 0x00010 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG2, Boolean.valueOf(bits and 0x00020 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG3, Boolean.valueOf(bits and 0x00040 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG4, Boolean.valueOf(bits and 0x00080 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG5, Boolean.valueOf(bits and 0x00100 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG6, Boolean.valueOf(bits and 0x00200 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG7, Boolean.valueOf(bits and 0x00400 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG8, Boolean.valueOf(bits and 0x00800 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG9, Boolean.valueOf(bits and 0x01000 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG10, Boolean.valueOf(bits and 0x02000 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG11, Boolean.valueOf(bits and 0x04000 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG12, Boolean.valueOf(bits and 0x08000 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG13, Boolean.valueOf(bits and 0x10000 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG14, Boolean.valueOf(bits and 0x20000 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG15, Boolean.valueOf(bits and 0x40000 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG16, Boolean.valueOf(bits and 0x80000 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG17, Boolean.valueOf(bits and 0x100000 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG18, Boolean.valueOf(bits and 0x200000 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG19, Boolean.valueOf(bits and 0x400000 != 0))
            resource.set(ResourceField.ENTERPRISE_FLAG20, Boolean.valueOf(bits and 0x800000 != 0))

            bits = MPPUtility.getShort(metaData2, 48)
            resource.set(ResourceField.ENTERPRISE, Boolean.valueOf(bits and 0x20 != 0))
        }
    }

    /**
     * The project files to which external tasks relate appear not to be
     * held against each task, instead there appears to be the concept
     * of the "current" external task file, i.e. the last one used.
     * This method iterates through the list of tasks marked as external
     * and attempts to ensure that the correct external project data (in the
     * form of a SubProject object) is linked to the task.
     *
     * @param externalTasks list of tasks marked as external
     */
    private fun processExternalTasks(externalTasks: List<Task>) {
        //
        // Sort the list of tasks into ID order
        //
        Collections.sort(externalTasks)

        //
        // Find any external tasks which don't have a sub project
        // object, and set this attribute using the most recent
        // value.
        //
        var currentSubProject: SubProject? = null

        for (currentTask in externalTasks) {
            val sp = currentTask.subProject
            if (sp == null) {
                currentTask.subProject = currentSubProject

                //we need to set the external task project path now that we have
                //the subproject for this task (was skipped while processing the task earlier)
                if (currentSubProject != null) {
                    currentTask.externalTaskProject = currentSubProject.fullPath
                }

            } else {
                currentSubProject = sp
            }

            if (currentSubProject != null) {
                //System.out.println ("Task: " +currentTask.getUniqueID() + " " + currentTask.getName() + " File=" + currentSubProject.getFullPath() + " ID=" + currentTask.getExternalTaskID());
                currentTask.project = currentSubProject.fullPath
            }
        }
    }

    /**
     * This method is used to extract the task hyperlink attributes
     * from a block of data and call the appropriate modifier methods
     * to configure the specified task object.
     *
     * @param task task instance
     * @param data hyperlink data block
     */
    private fun processHyperlinkData(task: Task, data: ByteArray?) {
        if (data != null) {
            var offset = 12
            val hyperlink: String
            val address: String
            val subaddress: String

            offset += 12
            hyperlink = MPPUtility.getUnicodeString(data, offset)
            offset += (hyperlink.length() + 1) * 2

            offset += 12
            address = MPPUtility.getUnicodeString(data, offset)
            offset += (address.length() + 1) * 2

            offset += 12
            subaddress = MPPUtility.getUnicodeString(data, offset)

            task.hyperlink = hyperlink
            task.hyperlinkAddress = address
            task.hyperlinkSubAddress = subaddress
        }
    }

    /**
     * This method is used to extract the resource hyperlink attributes
     * from a block of data and call the appropriate modifier methods
     * to configure the specified task object.
     *
     * @param resource resource instance
     * @param data hyperlink data block
     */
    private fun processHyperlinkData(resource: Resource, data: ByteArray?) {
        if (data != null) {
            var offset = 12
            val hyperlink: String
            val address: String
            val subaddress: String

            offset += 12
            hyperlink = MPPUtility.getUnicodeString(data, offset)
            offset += (hyperlink.length() + 1) * 2

            offset += 12
            address = MPPUtility.getUnicodeString(data, offset)
            offset += (address.length() + 1) * 2

            offset += 12
            subaddress = MPPUtility.getUnicodeString(data, offset)

            resource.hyperlink = hyperlink
            resource.hyperlinkAddress = address
            resource.hyperlinkSubAddress = subaddress
        }
    }

    /**
     * This method extracts and collates constraint data.
     *
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    private fun processConstraintData() {
        val factory = ConstraintFactory()
        factory.process(m_projectDir!!, m_file, m_inputStreamFactory)
    }

    /**
     * This method extracts and collates resource data.
     *
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    private fun processResourceData() {
        val fieldMap = FieldMap14(m_file!!.projectProperties, m_file!!.customFields)
        fieldMap.createResourceFieldMap(m_projectProps)

        val enterpriseCustomFieldMap = FieldMap14(m_file!!.projectProperties, m_file!!.customFields)
        enterpriseCustomFieldMap.createEnterpriseCustomFieldMap(m_projectProps, ResourceField::class.java)

        val rscDir = m_projectDir!!.getEntry("TBkndRsc") as DirectoryEntry
        val rscVarMeta = VarMeta12(DocumentInputStream(rscDir.getEntry("VarMeta") as DocumentEntry))
        val rscVarData = Var2Data(rscVarMeta, DocumentInputStream(rscDir.getEntry("Var2Data") as DocumentEntry))
        val rscFixedMeta = FixedMeta(DocumentInputStream(rscDir.getEntry("FixedMeta") as DocumentEntry), 37)
        val rscFixedData = FixedData(rscFixedMeta, m_inputStreamFactory!!.getInstance(rscDir, "FixedData"))
        val rscFixed2Meta = FixedMeta(DocumentInputStream(rscDir.getEntry("Fixed2Meta") as DocumentEntry), 50)
        val rscFixed2Data = FixedData(rscFixed2Meta, m_inputStreamFactory!!.getInstance(rscDir, "Fixed2Data"))

        //System.out.println(rscVarMeta);
        //System.out.println(rscVarData);
        //System.out.println(rscFixedMeta);
        //System.out.println(rscFixedData);
        //System.out.println(rscFixed2Meta);
        //System.out.println(rscFixed2Data);

        // Process aliases
        if (rscDir.hasEntry("Props")) {
            val props = Props14(m_inputStreamFactory!!.getInstance(rscDir, "Props"))
            CustomFieldAliasReader(m_file!!.customFields, props.getByteArray(RESOURCE_FIELD_NAME_ALIASES)).process()
        }

        val resourceMap = createResourceMap(fieldMap, rscFixedMeta, rscFixedData)
        val uniqueid = rscVarMeta.uniqueIdentifierArray
        var id: Integer
        var offset: Integer?
        var data: ByteArray?
        var metaData: ByteArray?
        var resource: Resource
        var notes: String?

        //
        // Select the correct meta data locations depending on
        // which version of Microsoft project generated this file
        //
        val metaDataBitFlags: Array<MppBitFlag>
        val metaData2BitFlags: Array<MppBitFlag>
        val resourceTypeOffset: Int
        val resourceTypeMask: Int

        if (NumberHelper.getInt(m_file!!.projectProperties.applicationVersion) > ApplicationVersion.PROJECT_2010) {
            resourceTypeOffset = 12
            resourceTypeMask = 0x10
            metaDataBitFlags = PROJECT2013_RESOURCE_META_DATA_BIT_FLAGS
            metaData2BitFlags = PROJECT2013_RESOURCE_META_DATA2_BIT_FLAGS
        } else {
            resourceTypeOffset = 9
            resourceTypeMask = 0x02
            metaDataBitFlags = PROJECT2010_RESOURCE_META_DATA_BIT_FLAGS
            metaData2BitFlags = PROJECT2010_RESOURCE_META_DATA2_BIT_FLAGS
        }

        for (loop in uniqueid.indices) {
            id = uniqueid[loop]
            offset = resourceMap.get(id)
            if (offset == null) {
                continue
            }

            data = rscFixedData.getByteArrayValue(offset!!.intValue())
            val metaData2 = rscFixed2Meta.getByteArrayValue(offset!!.intValue())
            val data2 = rscFixed2Data.getByteArrayValue(offset!!.intValue())
            //metaData = rscFixedMeta.getByteArrayValue(offset.intValue());
            //MPPUtility.dataDump(data, true, true, true, true, true, true, true);
            //MPPUtility.dataDump(metaData, true, true, true, true, true, true, true);
            //MPPUtility.varDataDump(rscVarData, id, true, true, true, true, true, true);

            resource = m_file!!.addResource()

            resource.disableEvents()

            fieldMap.populateContainer(ResourceField::class.java, resource, id, arrayOf(data, data2), rscVarData)

            enterpriseCustomFieldMap.populateContainer(ResourceField::class.java, resource, id, null, rscVarData)

            resource.enableEvents()

            processHyperlinkData(resource, rscVarData.getByteArray(id, fieldMap.getVarDataKey(ResourceField.HYPERLINK_DATA)))

            resource.id = Integer.valueOf(MPPUtility.getInt(data, fieldMap.getFixedDataOffset(ResourceField.ID)))

            resource.outlineCode1 = m_outlineCodeVarData!!.getUnicodeString(Integer.valueOf(rscVarData.getInt(id, 2, fieldMap.getVarDataKey(ResourceField.OUTLINE_CODE1_INDEX))), OUTLINECODE_DATA)
            resource.outlineCode2 = m_outlineCodeVarData!!.getUnicodeString(Integer.valueOf(rscVarData.getInt(id, 2, fieldMap.getVarDataKey(ResourceField.OUTLINE_CODE2_INDEX))), OUTLINECODE_DATA)
            resource.outlineCode3 = m_outlineCodeVarData!!.getUnicodeString(Integer.valueOf(rscVarData.getInt(id, 2, fieldMap.getVarDataKey(ResourceField.OUTLINE_CODE3_INDEX))), OUTLINECODE_DATA)
            resource.outlineCode4 = m_outlineCodeVarData!!.getUnicodeString(Integer.valueOf(rscVarData.getInt(id, 2, fieldMap.getVarDataKey(ResourceField.OUTLINE_CODE4_INDEX))), OUTLINECODE_DATA)
            resource.outlineCode5 = m_outlineCodeVarData!!.getUnicodeString(Integer.valueOf(rscVarData.getInt(id, 2, fieldMap.getVarDataKey(ResourceField.OUTLINE_CODE5_INDEX))), OUTLINECODE_DATA)
            resource.outlineCode6 = m_outlineCodeVarData!!.getUnicodeString(Integer.valueOf(rscVarData.getInt(id, 2, fieldMap.getVarDataKey(ResourceField.OUTLINE_CODE6_INDEX))), OUTLINECODE_DATA)
            resource.outlineCode7 = m_outlineCodeVarData!!.getUnicodeString(Integer.valueOf(rscVarData.getInt(id, 2, fieldMap.getVarDataKey(ResourceField.OUTLINE_CODE7_INDEX))), OUTLINECODE_DATA)
            resource.outlineCode8 = m_outlineCodeVarData!!.getUnicodeString(Integer.valueOf(rscVarData.getInt(id, 2, fieldMap.getVarDataKey(ResourceField.OUTLINE_CODE8_INDEX))), OUTLINECODE_DATA)
            resource.outlineCode9 = m_outlineCodeVarData!!.getUnicodeString(Integer.valueOf(rscVarData.getInt(id, 2, fieldMap.getVarDataKey(ResourceField.OUTLINE_CODE9_INDEX))), OUTLINECODE_DATA)
            resource.outlineCode10 = m_outlineCodeVarData!!.getUnicodeString(Integer.valueOf(rscVarData.getInt(id, 2, fieldMap.getVarDataKey(ResourceField.OUTLINE_CODE10_INDEX))), OUTLINECODE_DATA)

            metaData = rscFixedMeta.getByteArrayValue(offset!!.intValue())
            readBitFields(metaDataBitFlags, resource, metaData)
            readBitFields(metaData2BitFlags, resource, metaData2)

            resource.uniqueID = id

            notes = resource.notes
            if (m_reader!!.preserveNoteFormatting == false) {
                notes = RtfHelper.strip(notes)
            }

            resource.notes = notes

            //
            // Configure the resource calendar
            //
            resource.resourceCalendar = m_resourceMap!!.get(id)

            //
            // Process any enterprise columns
            //
            processResourceEnterpriseColumns(id, resource, rscVarData, metaData2)

            //
            // Process cost rate tables
            //
            val crt = CostRateTableFactory()
            crt.process(resource, 0, rscVarData.getByteArray(id, fieldMap.getVarDataKey(ResourceField.COST_RATE_A)))
            crt.process(resource, 1, rscVarData.getByteArray(id, fieldMap.getVarDataKey(ResourceField.COST_RATE_B)))
            crt.process(resource, 2, rscVarData.getByteArray(id, fieldMap.getVarDataKey(ResourceField.COST_RATE_C)))
            crt.process(resource, 3, rscVarData.getByteArray(id, fieldMap.getVarDataKey(ResourceField.COST_RATE_D)))
            crt.process(resource, 4, rscVarData.getByteArray(id, fieldMap.getVarDataKey(ResourceField.COST_RATE_E)))

            //
            // Process availability table
            //
            val af = AvailabilityFactory()
            af.process(resource.availability, rscVarData.getByteArray(id, fieldMap.getVarDataKey(ResourceField.AVAILABILITY_DATA)))

            //
            // Process resource type
            //
            if (metaData!![resourceTypeOffset] and resourceTypeMask != 0) {
                resource.type = ResourceType.WORK
            } else {
                if (metaData2!![8] and 0x10 != 0) {
                    resource.type = ResourceType.COST
                } else {
                    resource.type = ResourceType.MATERIAL
                }
            }

            m_eventManager!!.fireResourceReadEvent(resource)
        }
    }

    /**
     * This method extracts and collates resource assignment data.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processAssignmentData() {
        val fieldMap = FieldMap14(m_file!!.projectProperties, m_file!!.customFields)
        fieldMap.createAssignmentFieldMap(m_projectProps)

        val enterpriseCustomFieldMap = FieldMap14(m_file!!.projectProperties, m_file!!.customFields)
        enterpriseCustomFieldMap.createEnterpriseCustomFieldMap(m_projectProps, AssignmentField::class.java)

        val assnDir = m_projectDir!!.getEntry("TBkndAssn") as DirectoryEntry
        val assnVarMeta = VarMeta12(DocumentInputStream(assnDir.getEntry("VarMeta") as DocumentEntry))
        val assnVarData = Var2Data(assnVarMeta, DocumentInputStream(assnDir.getEntry("Var2Data") as DocumentEntry))
        val assnFixedMeta = FixedMeta(DocumentInputStream(assnDir.getEntry("FixedMeta") as DocumentEntry), 34)
        val assnFixedData = FixedData(110, m_inputStreamFactory!!.getInstance(assnDir, "FixedData"))
        val assnFixedData2 = FixedData(48, m_inputStreamFactory!!.getInstance(assnDir, "Fixed2Data"))
        //FixedMeta assnFixedMeta2 = new FixedMeta(new DocumentInputStream(((DocumentEntry) assnDir.getEntry("Fixed2Meta"))), 53);
        //Props props = new Props14(new DocumentInputStream(((DocumentEntry) assnDir.getEntry("Props"))));

        val factory = ResourceAssignmentFactory()
        factory.process(m_file!!, fieldMap, enterpriseCustomFieldMap, m_reader!!.useRawTimephasedData, m_reader!!.preserveNoteFormatting, assnVarMeta, assnVarData, assnFixedMeta, assnFixedData, assnFixedData2, assnFixedMeta.itemCount)
    }

    /**
     * This method is used to determine if a duration is estimated.
     *
     * @param type Duration units value
     * @return boolean Estimated flag
     */
    private fun getDurationEstimated(type: Int): Boolean {
        return type and DURATION_CONFIRMED_MASK != 0
    }

    /**
     * This method extracts view data from the MPP file.
     *
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    private fun processViewData() {
        val dir = m_viewDir!!.getEntry("CV_iew") as DirectoryEntry
        val viewVarMeta = VarMeta12(DocumentInputStream(dir.getEntry("VarMeta") as DocumentEntry))
        val viewVarData = Var2Data(viewVarMeta, DocumentInputStream(dir.getEntry("Var2Data") as DocumentEntry))
        val fixedMeta = FixedMeta(DocumentInputStream(dir.getEntry("FixedMeta") as DocumentEntry), 10)
        val fixedData = FixedData(138, m_inputStreamFactory!!.getInstance(dir, "FixedData"))

        val items = fixedMeta.adjustedItemCount
        var view: View
        val factory = ViewFactory14()

        var lastOffset = -1
        for (loop in 0 until items) {
            val fm = fixedMeta.getByteArrayValue(loop)
            val offset = MPPUtility.getShort(fm, 4)
            if (offset > lastOffset) {
                val fd = fixedData.getByteArrayValue(fixedData.getIndexFromOffset(offset))
                if (fd != null) {
                    view = factory.createView(m_file, fm, fd, viewVarData, m_fontBases)
                    m_file!!.views.add(view)
                }
                lastOffset = offset
            }
        }
    }

    /**
     * This method extracts table data from the MPP file.
     *
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    private fun processTableData() {
        val dir = m_viewDir!!.getEntry("CTable") as DirectoryEntry

        val varMeta = VarMeta12(DocumentInputStream(dir.getEntry("VarMeta") as DocumentEntry))
        val varData = Var2Data(varMeta, DocumentInputStream(dir.getEntry("Var2Data") as DocumentEntry))
        val fixedData = FixedData(230, DocumentInputStream(dir.getEntry("FixedData") as DocumentEntry))
        //System.out.println(varMeta);
        //System.out.println(varData);
        //System.out.println(fixedData);

        val container = m_file!!.tables
        val factory = TableFactory14(TABLE_COLUMN_DATA_STANDARD, TABLE_COLUMN_DATA_ENTERPRISE, TABLE_COLUMN_DATA_BASELINE)
        val items = fixedData.itemCount
        for (loop in 0 until items) {
            val data = fixedData.getByteArrayValue(loop)
            val table = factory.createTable(m_file, data, varMeta, varData)
            container.add(table)
            //System.out.println(table);
        }
    }

    /**
     * Read filter definitions.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processFilterData() {
        val dir = m_viewDir!!.getEntry("CFilter") as DirectoryEntry

        val fixedMeta: FixedMeta
        val fixedData: FixedData
        val varMeta: VarMeta
        val varData: Var2Data

        try {
            fixedMeta = FixedMeta(DocumentInputStream(dir.getEntry("FixedMeta") as DocumentEntry), 10)
            fixedData = FixedData(fixedMeta, m_inputStreamFactory!!.getInstance(dir, "FixedData"))
            varMeta = VarMeta12(DocumentInputStream(dir.getEntry("VarMeta") as DocumentEntry))
            varData = Var2Data(varMeta, DocumentInputStream(dir.getEntry("Var2Data") as DocumentEntry))
        } catch (ex: IndexOutOfBoundsException) {
            // From a sample file where the stream reports an available number of bytes
            // but attempting to read that number of bytes raises an exception.
            return
        } catch (ex: IOException) {
            // I've come across an unusual sample where the VarMeta magic number is zero, which throws this exception.
            // MS Project opens the file fine. If we get into this state, we'll just ignore the filter definitions.
            return
        }

        //System.out.println(fixedMeta);
        //System.out.println(fixedData);
        //System.out.println(varMeta);
        //System.out.println(varData);

        val reader = FilterReader14()
        reader.process(m_file!!.projectProperties, m_file!!.filters, fixedData, varData)
    }

    /**
     * Read saved view state from an MPP file.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processSavedViewState() {
        val dir = m_viewDir!!.getEntry("CEdl") as DirectoryEntry
        val varMeta = VarMeta12(DocumentInputStream(dir.getEntry("VarMeta") as DocumentEntry))
        val varData = Var2Data(varMeta, DocumentInputStream(dir.getEntry("Var2Data") as DocumentEntry))
        //System.out.println(varMeta);
        //System.out.println(varData);

        val `is` = DocumentInputStream(dir.getEntry("FixedData") as DocumentEntry)
        val fixedData = ByteArray(`is`.available())
        `is`.read(fixedData)
        `is`.close()
        //System.out.println(ByteArrayHelper.hexdump(fixedData, false, 16, ""));

        val reader = ViewStateReader12()
        reader.process(m_file, varData, fixedData)
    }

    /**
     * Read group definitions.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processGroupData() {
        val dir = m_viewDir!!.getEntry("CGrouping") as DirectoryEntry
        val fixedMeta = FixedMeta(DocumentInputStream(dir.getEntry("FixedMeta") as DocumentEntry), 10)
        val fixedData = FixedData(fixedMeta, m_inputStreamFactory!!.getInstance(dir, "FixedData"))
        val varMeta = VarMeta12(DocumentInputStream(dir.getEntry("VarMeta") as DocumentEntry))
        val varData = Var2Data(varMeta, DocumentInputStream(dir.getEntry("Var2Data") as DocumentEntry))

        //System.out.println(fixedMeta);
        //System.out.println(fixedData);
        //System.out.println(varMeta);
        //System.out.println(varData);

        val reader = GroupReader14()
        reader.process(m_file, fixedData, varData, m_fontBases)
    }

    /**
     * Retrieve custom field value.
     *
     * @param varData var data block
     * @param outlineCodeVarData var data block
     * @param id item ID
     * @param type item type
     * @return item value
     */
    private fun getCustomFieldOutlineCodeValue(varData: Var2Data, outlineCodeVarData: Var2Data?, id: Integer, type: Integer?): String? {
        var result: String? = null

        val mask = varData.getShort(id, type)
        if (mask and 0xFF00 != VALUE_LIST_MASK) {
            result = outlineCodeVarData!!.getUnicodeString(Integer.valueOf(varData.getInt(id, 2, type)), OUTLINECODE_DATA)
        } else {
            val uniqueId = varData.getInt(id, 2, type)
            val item = m_file!!.customFields.getCustomFieldValueItemByUniqueID(uniqueId)
            if (item != null) {
                val value = item!!.value
                if (value is String) {
                    result = value
                }

                val result2 = getCustomFieldOutlineCodeValue(varData, outlineCodeVarData, item!!.parent!!)
                if (result != null && result2 != null && !result2.isEmpty()) {
                    result = "$result2.$result"
                }
            }
        }
        return result
    }

    /**
     * Retrieve custom field value.
     *
     * @param varData var data block
     * @param outlineCodeVarData var data block
     * @param id parent item ID
     * @return item value
     */
    private fun getCustomFieldOutlineCodeValue(varData: Var2Data, outlineCodeVarData: Var2Data?, id: Integer): String? {
        var result: String? = null

        val uniqueId = id.intValue()
        if (uniqueId == 0) {
            return ""
        }

        val item = m_file!!.customFields.getCustomFieldValueItemByUniqueID(uniqueId)
        if (item != null) {
            val value = item!!.value
            if (value is String) {
                result = value
            }

            if (result != null && !NumberHelper.equals(id, item!!.parent)) {
                val result2 = getCustomFieldOutlineCodeValue(varData, outlineCodeVarData, item!!.parent!!)
                if (result2 != null && !result2.isEmpty()) {
                    result = "$result2.$result"
                }
            }
        }

        return result
    }

    /**
     * Iterate through a set of bit field flags and set the value for each one
     * in the supplied container.
     *
     * @param flags bit field flags
     * @param container field container
     * @param data source data
     */
    private fun readBitFields(flags: Array<MppBitFlag>, container: FieldContainer, data: ByteArray?) {
        for (flag in flags) {
            flag.setValue(container, data)
        }
    }

    companion object {

        //   private static final Comparator<Task> START_COMPARATOR = new Comparator<Task>()
        //   {
        //      public int compare(Task o1, Task o2)
        //      {
        //         int result = DateUtility.compare(o1.getStart(), o2.getStart());
        //         if (result == 0)
        //         {
        //            result = o1.getUniqueID().intValue() - o2.getUniqueID().intValue();
        //            //result = o1.getID().intValue() - o2.getID().intValue();
        //         }
        //         return (result);
        //      }
        //   };

        //   private static final Comparator<Task> FINISH_COMPARATOR = new Comparator<Task>()
        //   {
        //      public int compare(Task o1, Task o2)
        //      {
        //         int result = DateUtility.compare(o1.getFinish(), o2.getFinish());
        //         if (result == 0)
        //         {
        //            result = o1.getUniqueID().intValue() - o2.getUniqueID().intValue();
        //         }
        //         return (result);
        //      }
        //   };

        // Signals the end of the list of subproject task unique ids
        //private static final int SUBPROJECT_LISTEND = 0x00000303;

        // Signals that the previous value was for the subproject task unique id
        private val SUBPROJECT_TASKUNIQUEID0 = 0x00000000
        private val SUBPROJECT_TASKUNIQUEID1 = 0x0B340000
        private val SUBPROJECT_TASKUNIQUEID2 = 0x0ABB0000
        private val SUBPROJECT_TASKUNIQUEID3 = 0x05A10000
        private val SUBPROJECT_TASKUNIQUEID4 = 0x0BD50000
        private val SUBPROJECT_TASKUNIQUEID5 = 0x03D60000
        private val SUBPROJECT_TASKUNIQUEID6 = 0x067F0000
        private val SUBPROJECT_TASKUNIQUEID7 = 0x067D0000
        private val SUBPROJECT_TASKUNIQUEID8 = 0x00540000

        /**
         * Resource data types.
         */
        private val TABLE_COLUMN_DATA_STANDARD = Integer.valueOf(6)
        private val TABLE_COLUMN_DATA_ENTERPRISE = Integer.valueOf(7)
        private val TABLE_COLUMN_DATA_BASELINE = Integer.valueOf(8)
        private val OUTLINECODE_DATA = Integer.valueOf(22)

        /**
         * Custom value list types.
         */
        private val VALUE_LIST_MASK = 0x0700

        /**
         * Mask used to isolate confirmed flag from the duration units field.
         */
        private val DURATION_CONFIRMED_MASK = 0x20

        private val RESOURCE_FIELD_NAME_ALIASES = Integer.valueOf(71303169)
        private val TASK_FIELD_NAME_ALIASES = Integer.valueOf(71303169)

        /**
         * Deleted and null tasks have their ID and UniqueID attributes at fixed offsets.
         */
        private val TASK_UNIQUE_ID_FIXED_OFFSET = 0
        private val TASK_ID_FIXED_OFFSET = 4
        private val NULL_TASK_BLOCK_SIZE = 16

        private val PROJECT2010_TASK_META_DATA_BIT_FLAGS = arrayOf(MppBitFlag(TaskField.FLAG1, 35, 0x0000040, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG2, 35, 0x0000080, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG3, 35, 0x0000100, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG4, 35, 0x0000200, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG5, 35, 0x0000400, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG6, 35, 0x0000800, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG7, 35, 0x0001000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG8, 35, 0x0002000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG9, 35, 0x0004000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG10, 35, 0x0008000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG11, 35, 0x0010000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG12, 35, 0x0020000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG13, 35, 0x0040000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG14, 35, 0x0080000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG15, 35, 0x0100000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG16, 35, 0x0200000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG17, 35, 0x0400000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG18, 35, 0x0800000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG19, 35, 0x1000000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG20, 35, 0x2000000, Boolean.FALSE, Boolean.TRUE),

                MppBitFlag(TaskField.MILESTONE, 8, 0x20, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.MARKED, 9, 0x40, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.IGNORE_RESOURCE_CALENDAR, 10, 0x02, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.ROLLUP, 10, 0x08, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.HIDE_BAR, 10, 0x80, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.EFFORT_DRIVEN, 11, 0x10, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.LEVEL_ASSIGNMENTS, 13, 0x04, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.LEVELING_CAN_SPLIT, 13, 0x02, Boolean.FALSE, Boolean.TRUE))

        private val PROJECT2013_TASK_META_DATA_BIT_FLAGS = arrayOf(MppBitFlag(TaskField.FLAG1, 24, 0x0002, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG2, 24, 0x0004, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG3, 24, 0x0008, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG4, 24, 0x0010, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG5, 24, 0x0020, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG6, 24, 0x0040, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG7, 24, 0x0080, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG8, 24, 0x0100, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG9, 24, 0x0200, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG10, 24, 0x0400, Boolean.FALSE, Boolean.TRUE),

                MppBitFlag(TaskField.FLAG11, 33, 0x002, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG12, 33, 0x004, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG13, 33, 0x008, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG14, 33, 0x010, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG15, 33, 0x020, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG16, 33, 0x040, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG17, 33, 0x080, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG18, 33, 0x100, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG19, 33, 0x200, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.FLAG20, 33, 0x400, Boolean.FALSE, Boolean.TRUE),

                MppBitFlag(TaskField.MILESTONE, 10, 0x02, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.MARKED, 12, 0x02, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.ROLLUP, 12, 0x04, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.HIDE_BAR, 12, 0x80, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.EFFORT_DRIVEN, 13, 0x08, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.LEVEL_ASSIGNMENTS, 16, 0x04, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.LEVELING_CAN_SPLIT, 16, 0x02, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.IGNORE_RESOURCE_CALENDAR, 17, 0x20, Boolean.FALSE, Boolean.TRUE))

        private val PROJECT2010_TASK_META_DATA2_BIT_FLAGS = arrayOf(MppBitFlag(TaskField.ACTIVE, 8, 0x04, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.TASK_MODE, 8, 0x08, Boolean.FALSE, Boolean.TRUE))

        private val PROJECT2013_TASK_META_DATA2_BIT_FLAGS = arrayOf(MppBitFlag(TaskField.ACTIVE, 8, 0x40, Boolean.FALSE, Boolean.TRUE), MppBitFlag(TaskField.TASK_MODE, 8, 0x80, Boolean.FALSE, Boolean.TRUE))

        private val PROJECT2010_RESOURCE_META_DATA2_BIT_FLAGS = arrayOf(MppBitFlag(ResourceField.BUDGET, 8, 0x20, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.TYPE, 8, 0x10, ResourceType.MATERIAL, ResourceType.COST), MppBitFlag(ResourceField.GENERIC, 32, 0x04000000, Boolean.FALSE, Boolean.TRUE))

        private val PROJECT2013_RESOURCE_META_DATA2_BIT_FLAGS = arrayOf(MppBitFlag(ResourceField.BUDGET, 8, 0x20, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.TYPE, 8, 0x10, ResourceType.MATERIAL, ResourceType.COST), MppBitFlag(ResourceField.GENERIC, 32, 0x10000000, Boolean.FALSE, Boolean.TRUE))

        private val PROJECT2010_RESOURCE_META_DATA_BIT_FLAGS = arrayOf(MppBitFlag(ResourceField.FLAG10, 28, 0x0000020, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG1, 28, 0x0000040, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG2, 28, 0x0000080, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG3, 28, 0x0000100, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG4, 28, 0x0000200, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG5, 28, 0x0000400, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG6, 28, 0x0000800, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG7, 28, 0x0001000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG8, 28, 0x0002000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG9, 28, 0x0004000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG11, 28, 0x0008000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG12, 28, 0x0010000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG13, 28, 0x0020000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG14, 28, 0x0040000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG15, 28, 0x0080000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG16, 28, 0x0100000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG17, 28, 0x0200000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG18, 28, 0x0400000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG19, 28, 0x0800000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG20, 28, 0x1000000, Boolean.FALSE, Boolean.TRUE))

        private val PROJECT2013_RESOURCE_META_DATA_BIT_FLAGS = arrayOf(MppBitFlag(ResourceField.FLAG10, 19, 0x0040, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG1, 19, 0x0080, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG2, 19, 0x0100, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG3, 19, 0x0200, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG4, 19, 0x0400, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG5, 19, 0x0800, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG6, 19, 0x1000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG7, 19, 0x2000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG8, 19, 0x4000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG9, 19, 0x8000, Boolean.FALSE, Boolean.TRUE),

                MppBitFlag(ResourceField.FLAG11, 24, 0x00080, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG12, 24, 0x00100, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG13, 24, 0x00200, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG14, 24, 0x00400, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG15, 24, 0x00800, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG16, 24, 0x01000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG17, 24, 0x02000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG18, 24, 0x04000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG19, 24, 0x08000, Boolean.FALSE, Boolean.TRUE), MppBitFlag(ResourceField.FLAG20, 24, 0x10000, Boolean.FALSE, Boolean.TRUE))
    }
}
