/*
 * file:       MPP9Reader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2002-2006
 * date:       22/05/2003
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
import java.util.Collections
import java.util.Date
import java.util.HashMap
import java.util.LinkedList
import java.util.TreeMap

import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream

import net.sf.mpxj.CustomFieldContainer
import net.sf.mpxj.DateRange
import net.sf.mpxj.Duration
import net.sf.mpxj.EventManager
import net.sf.mpxj.FieldType
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
import net.sf.mpxj.TimeUnit
import net.sf.mpxj.View
import net.sf.mpxj.common.DateHelper
import net.sf.mpxj.common.MPPResourceField
import net.sf.mpxj.common.MPPTaskField
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.common.RtfHelper

/**
 * This class is used to represent a Microsoft Project MPP9 file. This
 * implementation allows the file to be read, and the data it contains
 * exported as a set of MPX objects. These objects can be interrogated
 * to retrieve any required data, or stored as an MPX file.
 */
internal class MPP9Reader : MPPVariantReader {

    //   private static void dumpUnknownData (String name, int[][] spec, byte[] data)
    //   {
    //      System.out.println (name);
    //      for (int loop=0; loop < spec.length; loop++)
    //      {
    //         System.out.println (spec[loop][0] + ": "+ ByteArrayHelper.hexdump(data, spec[loop][0], spec[loop][1], false));
    //      }
    //      System.out.println ();
    //   }

    //   private static final int[][] UNKNOWN_TASK_DATA = new int[][]
    //   {
    //      {36, 4},
    //      {42, 18},
    //      {116, 4},
    //      {134, 14},
    //      {144, 4},
    //      {148, 4},
    //      {152, 4},
    //      {156, 4},
    //      {248, 8},
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
    private var m_projectProps: Props9? = null
    private var m_fontBases: Map<Integer, FontBase>? = null
    private var m_taskSubProjects: Map<Integer, SubProject>? = null
    private var m_projectDir: DirectoryEntry? = null
    private var m_viewDir: DirectoryEntry? = null
    private var m_inputStreamFactory: DocumentInputStreamFactory? = null
    /**
     * This method is used to process an MPP9 file. This is the file format
     * used by Project 2000, 2002, and 2003.
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
    @Throws(MPXJException::class, IOException::class)
    private fun populateMemberData(reader: MPPReader, file: ProjectFile, root: DirectoryEntry) {
        m_reader = reader
        m_file = file
        m_eventManager = file.eventManager
        m_root = root

        //
        // Retrieve the high level document properties (never encoded)
        //
        val props9 = Props9(DocumentInputStream(root.getEntry("Props9") as DocumentEntry))
        //System.out.println(props9);

        file.projectProperties.projectFilePath = props9.getUnicodeString(Props.PROJECT_FILE_PATH)
        m_inputStreamFactory = DocumentInputStreamFactory(props9)

        //
        // Test for password protection. In the single byte retrieved here:
        //
        // 0x00 = no password
        // 0x01 = protection password has been supplied
        // 0x02 = write reservation password has been supplied
        // 0x03 = both passwords have been supplied
        //
        if (props9.getByte(Props.PASSWORD_FLAG) and 0x01 != 0) {
            // File is password protected for reading, let's read the password
            // and see if the correct read password was given to us.
            val readPassword = MPPUtility.decodePassword(props9.getByteArray(Props.PROTECTION_PASSWORD_HASH), m_inputStreamFactory!!.encryptionCode)
            // It looks like it is possible for a project file to have the password protection flag on without a password. In
            // this case MS Project treats the file as NOT protected. We need to do the same. It is worth noting that MS Project does
            // correct the problem if the file is re-saved (at least it did for me).
            if (readPassword != null && readPassword.length() > 0) {
                // See if the correct read password was given
                if (reader.readPassword == null || reader.readPassword!!.matches(readPassword) === false) {
                    // Passwords don't match
                    throw MPXJException(MPXJException.PASSWORD_PROTECTED_ENTER_PASSWORD)
                }
            }
            // Passwords matched so let's allow the reading to continue.
        }

        m_resourceMap = HashMap<Integer, ProjectCalendar>()
        m_projectDir = root.getEntry("   19") as DirectoryEntry
        m_viewDir = root.getEntry("   29") as DirectoryEntry
        val outlineCodeDir = m_projectDir!!.getEntry("TBkndOutlCode") as DirectoryEntry
        val outlineCodeVarMeta = VarMeta9(DocumentInputStream(outlineCodeDir.getEntry("VarMeta") as DocumentEntry))
        m_outlineCodeVarData = Var2Data(outlineCodeVarMeta, DocumentInputStream(outlineCodeDir.getEntry("Var2Data") as DocumentEntry))
        m_projectProps = Props9(m_inputStreamFactory!!.getInstance(m_projectDir!!, "Props"))
        //MPPUtility.fileDump("c:\\temp\\props.txt", m_projectProps.toString().getBytes());

        m_fontBases = HashMap<Integer, FontBase>()
        m_taskSubProjects = HashMap<Integer, SubProject>()

        m_file!!.projectProperties.mppFileType = Integer.valueOf(9)
        m_file!!.projectProperties.autoFilter = props9.getBoolean(Props.AUTO_FILTER)
    }

    /**
     * Clear transient member data.
     */
    private fun clearMemberData() {
        m_reader = null
        m_file = null
        m_eventManager = null
        m_root = null
        m_resourceMap = null
        m_projectDir = null
        m_viewDir = null
        m_outlineCodeVarData = null
        m_fontBases = null
        m_taskSubProjects = null
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
            var sp: SubProject?

            val itemHeader = ByteArray(20)

            /*int blockSize = MPPUtility.getInt(subProjData, offset);*/
            offset += 4

            /*int unknown = MPPUtility.getInt(subProjData, offset);*/
            offset += 4

            val itemCountOffset = MPPUtility.getInt(subProjData, offset)
            offset += 4

            while (offset < itemCountOffset) {
                index++
                itemHeaderOffset = MPPUtility.getShort(subProjData, offset)
                offset += 4

                MPPUtility.getByteArray(subProjData, itemHeaderOffset, itemHeader.size, itemHeader, 0)

                //            System.out.println ();
                //            System.out.println ();
                //            System.out.println ("offset=" + offset);
                //            System.out.println ("ItemHeaderOffset=" + itemHeaderOffset);
                //            System.out.println ("type=" + ByteArrayHelper.hexdump(itemHeader, 16, 1, false));
                //            System.out.println (ByteArrayHelper.hexdump(itemHeader, false, 16, ""));

                val subProjectType = itemHeader[16]
                when (subProjectType) {
                    //
                    // Subproject that is no longer inserted. This is a placeholder in order to be
                    // able to always guarantee unique unique ids.
                    //
                    0x00 -> {
                        offset += 8
                    }

                    //
                    // task unique ID, 8 bytes, path, file name
                    //
                    0x99.toByte(), 0x09, 0x0D -> {
                        uniqueIDOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        // sometimes offset of a task ID?
                        offset += 4

                        filePathOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        fileNameOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        sp = readSubProject(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // task unique ID, 8 bytes, path, file name
                    //
                    0x91.toByte() -> {
                        uniqueIDOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        filePathOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        fileNameOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        // Unknown offset
                        offset += 4

                        sp = readSubProject(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // task unique ID, path, file name
                    //
                    0x01, 0x03, 0x08, 0x0A, 0x11 -> {
                        uniqueIDOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        filePathOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        fileNameOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        sp = readSubProject(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // task unique ID, path, unknown, file name
                    //
                    0x81.toByte(), 0x41 -> {
                        uniqueIDOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        filePathOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        // unknown offset to 2 bytes of data?
                        offset += 4

                        fileNameOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        sp = readSubProject(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // task unique ID, path, file name
                    //
                    0xC0.toByte() -> {
                        uniqueIDOffset = itemHeaderOffset

                        filePathOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        fileNameOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        // unknown offset
                        offset += 4

                        sp = readSubProject(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // resource, task unique ID, path, file name
                    //
                    0x05 -> {
                        uniqueIDOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        filePathOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        fileNameOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        sp = readSubProject(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                        m_file!!.subProjects.resourceSubProject = sp
                    }

                    0x45 -> {
                        uniqueIDOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        filePathOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        fileNameOffset = MPPUtility.getInt(subProjData, offset) and 0x1FFFF
                        offset += 4

                        offset += 4
                        sp = readSubProject(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                        m_file!!.subProjects.resourceSubProject = sp
                    }

                    //
                    // path, file name
                    //
                    0x02, 0x04 -> {
                        filePathOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        fileNameOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        sp = readSubProject(subProjData, -1, filePathOffset, fileNameOffset, index)
                        // 0x02 looks to be the link FROM the resource pool to a project that uses it
                        if (subProjectType.toInt() == 0x04) {
                            m_file!!.subProjects.resourceSubProject = sp
                        }
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

                        sp = readSubProject(subProjData, uniqueIDOffset, filePathOffset, fileNameOffset, index)
                    }

                    //
                    // Appears when a subproject is collapsed
                    //
                    0x80.toByte() -> {
                        offset += 12
                    }

                    // deleted entry?
                    0x10 -> {
                        offset += 8
                    }

                    // new resource pool entry
                    0x44.toByte() -> {
                        filePathOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        offset += 4

                        fileNameOffset = MPPUtility.getShort(subProjData, offset)
                        offset += 4

                        sp = readSubProject(subProjData, -1, filePathOffset, fileNameOffset, index)
                        m_file!!.subProjects.resourceSubProject = sp
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
        var uniqueIDOffset = uniqueIDOffset
        var filePathOffset = filePathOffset
        var fileNameOffset = fileNameOffset
        try {
            val sp = SubProject()

            if (uniqueIDOffset != -1) {
                var prev = 0
                var value = MPPUtility.getInt(data, uniqueIDOffset)
                while (value != SUBPROJECT_LISTEND) {
                    when (value) {
                        SUBPROJECT_TASKUNIQUEID0, SUBPROJECT_TASKUNIQUEID1, SUBPROJECT_TASKUNIQUEID2, SUBPROJECT_TASKUNIQUEID3, SUBPROJECT_TASKUNIQUEID4, SUBPROJECT_TASKUNIQUEID5 -> {
                            // The previous value was for the subproject unique task id
                            sp.taskUniqueID = Integer.valueOf(prev)
                            m_taskSubProjects!!.put(sp.taskUniqueID, sp)
                            prev = 0
                        }

                        else -> {
                            if (prev != 0) {
                                // The previous value was for an external task unique task id
                                sp.addExternalTaskUniqueID(Integer.valueOf(prev))
                                m_taskSubProjects!!.put(Integer.valueOf(prev), sp)
                            }
                            prev = value
                        }
                    }
                    // Read the next value
                    uniqueIDOffset += 4
                    value = MPPUtility.getInt(data, uniqueIDOffset)
                }
                if (prev != 0) {
                    // The previous value was for an external task unique task id
                    sp.addExternalTaskUniqueID(Integer.valueOf(prev))
                    m_taskSubProjects!!.put(Integer.valueOf(prev), sp)
                }

                // Now get the unique id offset for this subproject
                value = 0x00800000 + (subprojectIndex - 1) * 0x00400000
                sp.uniqueIDOffset = Integer.valueOf(value)
            }

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
                // filePathOffset += size;
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
        val props = Props9(m_inputStreamFactory!!.getInstance(m_viewDir!!, "Props"))
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
     * Retrieve any task field value lists defined in the MPP file.
     */
    @Throws(IOException::class)
    private fun processCustomValueLists() {
        val reader = CustomFieldValueReader9(m_projectDir, m_file!!.projectProperties, m_projectProps, m_file!!.customFields)
        reader.process()
    }

    /**
     * Retrieves the description value list associated with a custom task field.
     * This method will return null if no descriptions for the value list has
     * been defined for this field.
     *
     * @param data data block
     * @return list of descriptions
     */
    fun getTaskFieldDescriptions(data: ByteArray?): List<String>? {
        if (data == null || data.size == 0) {
            return null
        }
        val descriptions = LinkedList<String>()
        var offset = 0
        while (offset < data.size) {
            val description = MPPUtility.getUnicodeString(data, offset)
            descriptions.add(description)
            offset += description.length() * 2 + 2
        }
        return descriptions
    }

    /**
     * Retrieves the description value list associated with a custom task field.
     * This method will return null if no descriptions for the value list has
     * been defined for this field.
     *
     * @param properties project properties
     * @param field task field
     * @param data data block
     * @return list of task field values
     */
    fun getTaskFieldValues(properties: ProjectProperties, field: FieldType?, data: ByteArray?): List<Object>? {
        if (field == null || data == null || data.size == 0) {
            return null
        }

        val list = LinkedList<Object>()
        var offset = 0

        when (field!!.getDataType()) {
            DATE -> while (offset + 4 <= data.size) {
                val date = MPPUtility.getTimestamp(data, offset)
                list.add(date)
                offset += 4
            }
            CURRENCY -> while (offset + 8 <= data.size) {
                val number = NumberHelper.getDouble(MPPUtility.getDouble(data, offset) / 100.0)
                list.add(number)
                offset += 8
            }
            NUMERIC -> while (offset + 8 <= data.size) {
                val number = NumberHelper.getDouble(MPPUtility.getDouble(data, offset))
                list.add(number)
                offset += 8
            }
            DURATION -> while (offset + 6 <= data.size) {
                val duration = MPPUtility.getAdjustedDuration(properties, MPPUtility.getInt(data, offset), MPPUtility.getDurationTimeUnits(MPPUtility.getShort(data, offset + 4)))
                list.add(duration)
                offset += 6
            }
            STRING -> while (offset < data.size) {
                val s = MPPUtility.getUnicodeString(data, offset)
                list.add(s)
                offset += s.length() * 2 + 2
            }
            BOOLEAN -> while (offset + 2 <= data.size) {
                val b = MPPUtility.getShort(data, offset) == 0x01
                list.add(Boolean.valueOf(b))
                offset += 2
            }
            else -> return null
        }

        return list
    }

    /**
     * Retrieve any resource field aliases defined in the MPP file.
     *
     * @param map index to field map
     * @param data resource field name alias data
     */
    private fun processFieldNameAliases(map: Map<Integer, FieldType>, data: ByteArray?) {
        if (data != null) {
            var offset = 0
            var index = 0
            val fields = m_file!!.customFields
            while (offset < data.size) {
                val alias = MPPUtility.getUnicodeString(data, offset)
                if (!alias.isEmpty()) {
                    val field = map[Integer.valueOf(index)]
                    if (field != null) {
                        fields.getCustomField(field).setAlias(alias)
                    }
                }
                offset += (alias.length() + 1) * 2
                index++
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

        for (loop in 0 until itemCount) {
            val data = rscFixedData.getByteArrayValue(loop)
            if (data == null || data.size < fieldMap.getMaxFixedDataSize(0)) {
                continue
            }

            val uniqueID = Integer.valueOf(MPPUtility.getShort(data, 0))
            resourceMap.put(uniqueID, Integer.valueOf(loop))
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
        val factory = MPP9CalendarFactory(m_file)
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
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processTaskData() {
        val fieldMap = FieldMap9(m_file!!.projectProperties, m_file!!.customFields)
        fieldMap.createTaskFieldMap(m_projectProps)

        val taskDir = m_projectDir!!.getEntry("TBkndTask") as DirectoryEntry
        val taskVarMeta = VarMeta9(DocumentInputStream(taskDir.getEntry("VarMeta") as DocumentEntry))
        val taskVarData = Var2Data(taskVarMeta, DocumentInputStream(taskDir.getEntry("Var2Data") as DocumentEntry))
        val taskFixedMeta = FixedMeta(DocumentInputStream(taskDir.getEntry("FixedMeta") as DocumentEntry), 47)
        val taskFixedData = FixedData(taskFixedMeta, m_inputStreamFactory!!.getInstance(taskDir, "FixedData"), 768, fieldMap.getMaxFixedDataSize(0))
        //System.out.println(taskFixedData);
        //System.out.println(taskFixedMeta);
        //System.out.println(taskVarMeta);
        //System.out.println(taskVarData);

        processFieldNameAliases(TASK_FIELD_ALIASES, m_projectProps!!.getByteArray(Props.TASK_FIELD_NAME_ALIASES))

        val taskMap = createTaskMap(fieldMap, taskFixedMeta, taskFixedData, taskVarData)
        // The var data may not contain all the tasks as tasks with no var data assigned will
        // not be saved in there. Most notably these are tasks with no name. So use the task map
        // which contains all the tasks.
        val uniqueIdArray = taskMap.keySet().toArray() //taskVarMeta.getUniqueIdentifierArray();
        var offset: Integer
        var data: ByteArray?
        var metaData: ByteArray?
        var task: Task
        var autoWBS = true
        val externalTasks = LinkedList<Task>()
        var recurringTaskReader: RecurringTaskReader? = null
        var notes: String?

        for (loop in uniqueIdArray.indices) {
            val uniqueID = uniqueIdArray[loop] as Integer

            offset = taskMap.get(uniqueID)
            if (taskFixedData.isValidOffset(offset) == false) {
                continue
            }

            data = taskFixedData.getByteArrayValue(offset.intValue())
            val id = Integer.valueOf(MPPUtility.getInt(data, fieldMap.getFixedDataOffset(TaskField.ID)))
            if (data!!.size == NULL_TASK_BLOCK_SIZE) {
                task = m_file!!.addTask()
                task.`null` = true
                task.uniqueID = Integer.valueOf(MPPUtility.getShort(data, TASK_UNIQUE_ID_FIXED_OFFSET))
                task.id = Integer.valueOf(MPPUtility.getShort(data, TASK_ID_FIXED_OFFSET))
                continue
            }

            if (data.size < fieldMap.getMaxFixedDataSize(0)) {
                continue
            }

            if (uniqueID.intValue() !== 0 && !taskVarMeta.containsKey(uniqueID)) {
                continue
            }

            metaData = taskFixedMeta.getByteArrayValue(offset.intValue())
            //System.out.println (ByteArrayHelper.hexdump(data, false, 16, ""));
            //System.out.println (ByteArrayHelper.hexdump(metaData, 8, 4, false));
            //MPPUtility.dataDump(data, true, true, true, true, true, true, true);
            //MPPUtility.dataDump(metaData, true, true, true, true, true, true, true);
            //MPPUtility.varDataDump(taskVarData, id, true, true, true, true, true, true);
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
            fieldMap.populateContainer(TaskField::class.java, task, uniqueID, arrayOf(data), taskVarData)
            task.enableEvents()

            task.effortDriven = metaData!![11] and 0x10 != 0

            task.estimated = getDurationEstimated(MPPUtility.getShort(data, fieldMap.getFixedDataOffset(TaskField.ACTUAL_DURATION_UNITS)))

            task.expanded = metaData[12] and 0x02 == 0
            val externalTaskID = task.subprojectTaskID
            if (externalTaskID != null && externalTaskID.intValue() !== 0) {
                task.subprojectTaskID = externalTaskID
                task.externalTask = true
                externalTasks.add(task)
            }

            task.setFlag(1, metaData[37] and 0x20 != 0)
            task.setFlag(2, metaData[37] and 0x40 != 0)
            task.setFlag(3, metaData[37] and 0x80 != 0)
            task.setFlag(4, metaData[38] and 0x01 != 0)
            task.setFlag(5, metaData[38] and 0x02 != 0)
            task.setFlag(6, metaData[38] and 0x04 != 0)
            task.setFlag(7, metaData[38] and 0x08 != 0)
            task.setFlag(8, metaData[38] and 0x10 != 0)
            task.setFlag(9, metaData[38] and 0x20 != 0)
            task.setFlag(10, metaData[38] and 0x40 != 0)
            task.setFlag(11, metaData[38] and 0x80 != 0)
            task.setFlag(12, metaData[39] and 0x01 != 0)
            task.setFlag(13, metaData[39] and 0x02 != 0)
            task.setFlag(14, metaData[39] and 0x04 != 0)
            task.setFlag(15, metaData[39] and 0x08 != 0)
            task.setFlag(16, metaData[39] and 0x10 != 0)
            task.setFlag(17, metaData[39] and 0x20 != 0)
            task.setFlag(18, metaData[39] and 0x40 != 0)
            task.setFlag(19, metaData[39] and 0x80 != 0)
            task.setFlag(20, metaData[40] and 0x01 != 0)
            task.hideBar = metaData[10] and 0x80 != 0
            processHyperlinkData(task, taskVarData.getByteArray(uniqueID, fieldMap.getVarDataKey(TaskField.HYPERLINK_DATA)))

            task.id = id
            task.ignoreResourceCalendar = metaData[10] and 0x02 != 0
            task.levelAssignments = metaData[13] and 0x04 != 0
            task.levelingCanSplit = metaData[13] and 0x02 != 0
            task.marked = metaData[9] and 0x40 != 0
            task.milestone = metaData[8] and 0x20 != 0

            task.setOutlineCode(1, m_outlineCodeVarData!!.getUnicodeString(task.getCachedValue(TaskField.OUTLINE_CODE1_INDEX) as Integer, OUTLINECODE_DATA))
            task.setOutlineCode(2, m_outlineCodeVarData!!.getUnicodeString(task.getCachedValue(TaskField.OUTLINE_CODE2_INDEX) as Integer, OUTLINECODE_DATA))
            task.setOutlineCode(3, m_outlineCodeVarData!!.getUnicodeString(task.getCachedValue(TaskField.OUTLINE_CODE3_INDEX) as Integer, OUTLINECODE_DATA))
            task.setOutlineCode(4, m_outlineCodeVarData!!.getUnicodeString(task.getCachedValue(TaskField.OUTLINE_CODE4_INDEX) as Integer, OUTLINECODE_DATA))
            task.setOutlineCode(5, m_outlineCodeVarData!!.getUnicodeString(task.getCachedValue(TaskField.OUTLINE_CODE5_INDEX) as Integer, OUTLINECODE_DATA))
            task.setOutlineCode(6, m_outlineCodeVarData!!.getUnicodeString(task.getCachedValue(TaskField.OUTLINE_CODE6_INDEX) as Integer, OUTLINECODE_DATA))
            task.setOutlineCode(7, m_outlineCodeVarData!!.getUnicodeString(task.getCachedValue(TaskField.OUTLINE_CODE7_INDEX) as Integer, OUTLINECODE_DATA))
            task.setOutlineCode(8, m_outlineCodeVarData!!.getUnicodeString(task.getCachedValue(TaskField.OUTLINE_CODE8_INDEX) as Integer, OUTLINECODE_DATA))
            task.setOutlineCode(9, m_outlineCodeVarData!!.getUnicodeString(task.getCachedValue(TaskField.OUTLINE_CODE9_INDEX) as Integer, OUTLINECODE_DATA))
            task.setOutlineCode(10, m_outlineCodeVarData!!.getUnicodeString(task.getCachedValue(TaskField.OUTLINE_CODE10_INDEX) as Integer, OUTLINECODE_DATA))

            task.rollup = metaData[10] and 0x08 != 0
            task.uniqueID = uniqueID

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
            //notes = taskVarData.getString(id, TASK_NOTES);
            notes = task.notes
            if (!m_reader!!.preserveNoteFormatting) {
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
            // Unfortunately it looks like 'null' tasks sometimes make it through,
            // so let's check for to see if we need to mark this task as a null
            // task after all.
            //
            if (task.name == null && (task.start == null || task.start.getTime() === MPPUtility.epochDate.getTime() || task.finish == null || task.finish.getTime() === MPPUtility.epochDate.getTime() || task.createDate == null || task.createDate.getTime() === MPPUtility.epochDate.getTime())) {
                m_file!!.removeTask(task)
                task = m_file!!.addTask()
                task.`null` = true
                task.uniqueID = uniqueID
                task.id = id
                continue
            }

            //
            // Process any enterprise columns
            //
            processTaskEnterpriseColumns(fieldMap, task, taskVarData)

            //
            // Fire the task read event
            //
            m_eventManager!!.fireTaskReadEvent(task)
            //System.out.println(task);
            //dumpUnknownData (task.getName(), UNKNOWN_TASK_DATA, data);
        }

        //
        // Enable auto WBS if necessary
        //
        m_file!!.projectConfig.autoWBS = autoWBS

        //
        // We have now read all of the tasks, so we are in a position
        // to perform post-processing to set up the relevant details
        // for each external task.
        //
        if (!externalTasks.isEmpty()) {
            processExternalTasks(externalTasks)
        }
    }

    /**
     * Extracts task enterprise column values.
     *
     * @param fieldMap fieldMap
     * @param task task instance
     * @param taskVarData task var data
     */
    private fun processTaskEnterpriseColumns(fieldMap: FieldMap, task: Task, taskVarData: Var2Data) {
        var data: ByteArray? = null
        val varDataKey = fieldMap.getVarDataKey(TaskField.ENTERPRISE_DATA)

        if (varDataKey != null) {
            data = taskVarData.getByteArray(task.uniqueID, varDataKey)
        }

        if (data != null) {
            val props = PropsBlock(data)
            //System.out.println(props);

            for (key in props.keySet()) {
                val keyValue = key.intValue() - MPPTaskField.TASK_FIELD_BASE
                var field = MPPTaskField.getInstance(keyValue)

                if (field != null) {
                    var value: Object? = null

                    when (field.getDataType()) {
                        CURRENCY -> {
                            value = Double.valueOf(props.getDouble(key) / 100)
                        }

                        DATE -> {
                            value = props.getTimestamp(key)
                        }

                        WORK -> {
                            val durationValueInHours = MPPUtility.getDouble(props.getByteArray(key), 0) / 60000
                            value = Duration.getInstance(durationValueInHours, TimeUnit.HOURS)
                        }

                        DURATION -> {
                            val durationData = props.getByteArray(key)
                            val durationValueInHours = MPPUtility.getInt(durationData, 0).toDouble() / 600
                            val durationUnits: TimeUnit
                            if (durationData.size < 6) {
                                durationUnits = TimeUnit.DAYS
                            } else {
                                durationUnits = MPPUtility.getDurationTimeUnits(MPPUtility.getShort(durationData, 4))
                            }
                            val duration = Duration.getInstance(durationValueInHours, TimeUnit.HOURS)
                            value = duration.convertUnits(durationUnits, m_file!!.projectProperties)
                        }

                        BOOLEAN -> {
                            field = null
                            val bits = props.getInt(key)
                            task.set(TaskField.ENTERPRISE_FLAG1, Boolean.valueOf(bits and 0x00002 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG2, Boolean.valueOf(bits and 0x00004 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG3, Boolean.valueOf(bits and 0x00008 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG4, Boolean.valueOf(bits and 0x00010 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG5, Boolean.valueOf(bits and 0x00020 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG6, Boolean.valueOf(bits and 0x00040 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG7, Boolean.valueOf(bits and 0x00080 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG8, Boolean.valueOf(bits and 0x00100 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG9, Boolean.valueOf(bits and 0x00200 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG10, Boolean.valueOf(bits and 0x00400 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG11, Boolean.valueOf(bits and 0x00800 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG12, Boolean.valueOf(bits and 0x01000 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG13, Boolean.valueOf(bits and 0x02000 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG14, Boolean.valueOf(bits and 0x04000 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG15, Boolean.valueOf(bits and 0x08000 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG16, Boolean.valueOf(bits and 0x10000 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG17, Boolean.valueOf(bits and 0x20000 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG18, Boolean.valueOf(bits and 0x40000 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG19, Boolean.valueOf(bits and 0x80000 != 0))
                            task.set(TaskField.ENTERPRISE_FLAG20, Boolean.valueOf(bits and 0x100000 != 0))
                        }

                        NUMERIC -> {
                            value = Double.valueOf(props.getDouble(key))
                        }

                        STRING -> {
                            value = props.getUnicodeString(key)
                        }

                        PERCENTAGE -> {
                            value = Integer.valueOf(props.getShort(key))
                        }

                        else -> {
                        }
                    }

                    task.set(field, value)
                }
            }
        }
    }

    /**
     * Extracts resource enterprise column data.
     *
     * @param fieldMap field map
     * @param resource resource instance
     * @param resourceVarData resource var data
     */
    private fun processResourceEnterpriseColumns(fieldMap: FieldMap, resource: Resource, resourceVarData: Var2Data) {
        var data: ByteArray? = null
        val varDataKey = fieldMap.getVarDataKey(ResourceField.ENTERPRISE_DATA)
        if (varDataKey != null) {
            data = resourceVarData.getByteArray(resource.uniqueID, varDataKey)
        }

        if (data != null) {
            val props = PropsBlock(data)
            //System.out.println(props);
            resource.creationDate = props.getTimestamp(Props.RESOURCE_CREATION_DATE)

            for (key in props.keySet()) {
                val keyValue = key.intValue() - MPPResourceField.RESOURCE_FIELD_BASE
                //System.out.println("Key=" + keyValue);

                var field = MPPResourceField.getInstance(keyValue)

                if (field != null) {
                    var value: Object? = null

                    when (field.dataType) {
                        CURRENCY -> {
                            value = Double.valueOf(props.getDouble(key) / 100)
                        }

                        DATE -> {
                            value = props.getTimestamp(key)
                        }

                        DURATION -> {
                            val durationData = props.getByteArray(key)
                            val durationValueInHours = MPPUtility.getInt(durationData, 0).toDouble() / 600
                            val durationUnits: TimeUnit
                            if (durationData.size < 6) {
                                durationUnits = TimeUnit.DAYS
                            } else {
                                durationUnits = MPPUtility.getDurationTimeUnits(MPPUtility.getShort(durationData, 4))
                            }
                            val duration = Duration.getInstance(durationValueInHours, TimeUnit.HOURS)
                            value = duration.convertUnits(durationUnits, m_file!!.projectProperties)

                        }

                        BOOLEAN -> {
                            when (field) {
                                ResourceField.FLAG1 -> {
                                    field = null
                                    val bits = props.getInt(key)
                                    resource.set(ResourceField.ENTERPRISE_FLAG1, Boolean.valueOf(bits and 0x00002 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG2, Boolean.valueOf(bits and 0x00004 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG3, Boolean.valueOf(bits and 0x00008 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG4, Boolean.valueOf(bits and 0x00010 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG5, Boolean.valueOf(bits and 0x00020 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG6, Boolean.valueOf(bits and 0x00040 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG7, Boolean.valueOf(bits and 0x00080 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG8, Boolean.valueOf(bits and 0x00100 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG9, Boolean.valueOf(bits and 0x00200 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG10, Boolean.valueOf(bits and 0x00400 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG11, Boolean.valueOf(bits and 0x00800 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG12, Boolean.valueOf(bits and 0x01000 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG13, Boolean.valueOf(bits and 0x02000 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG14, Boolean.valueOf(bits and 0x04000 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG15, Boolean.valueOf(bits and 0x08000 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG16, Boolean.valueOf(bits and 0x10000 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG17, Boolean.valueOf(bits and 0x20000 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG18, Boolean.valueOf(bits and 0x40000 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG19, Boolean.valueOf(bits and 0x80000 != 0))
                                    resource.set(ResourceField.ENTERPRISE_FLAG20, Boolean.valueOf(bits and 0x100000 != 0))
                                }

                                ResourceField.GENERIC -> {
                                    field = null
                                    resource.generic = props.getShort(key) != 0
                                }

                                else -> {
                                }
                            }
                        }

                        NUMERIC -> {
                            value = Double.valueOf(props.getDouble(key))
                        }

                        STRING -> {
                            value = props.getUnicodeString(key)
                        }

                        else -> {
                        }
                    }

                    resource.set(field, value)
                }
            }
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
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processConstraintData() {
        val factory = ConstraintFactory()
        factory.process(m_projectDir!!, m_file, m_inputStreamFactory)
    }

    /**
     * This method extracts and collates resource data.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processResourceData() {
        val fieldMap = FieldMap9(m_file!!.projectProperties, m_file!!.customFields)
        fieldMap.createResourceFieldMap(m_projectProps)

        val rscDir = m_projectDir!!.getEntry("TBkndRsc") as DirectoryEntry
        val rscVarMeta = VarMeta9(DocumentInputStream(rscDir.getEntry("VarMeta") as DocumentEntry))
        val rscVarData = Var2Data(rscVarMeta, DocumentInputStream(rscDir.getEntry("Var2Data") as DocumentEntry))
        val rscFixedMeta = FixedMeta(DocumentInputStream(rscDir.getEntry("FixedMeta") as DocumentEntry), 37)
        val rscFixedData = FixedData(rscFixedMeta, m_inputStreamFactory!!.getInstance(rscDir, "FixedData"))
        //System.out.println(rscVarMeta);
        //System.out.println(rscVarData);
        //System.out.println(rscFixedMeta);
        //System.out.println(rscFixedData);

        processFieldNameAliases(RESOURCE_FIELD_ALIASES, m_projectProps!!.getByteArray(Props.RESOURCE_FIELD_NAME_ALIASES))

        val resourceMap = createResourceMap(fieldMap, rscFixedMeta, rscFixedData)
        val uniqueid = rscVarMeta.uniqueIdentifierArray
        var id: Integer
        var offset: Integer?
        var data: ByteArray?
        var metaData: ByteArray?
        var resource: Resource
        var notes: String?

        for (loop in uniqueid.indices) {
            id = uniqueid[loop]
            offset = resourceMap.get(id)
            if (offset == null) {
                continue
            }
            data = rscFixedData.getByteArrayValue(offset!!.intValue())

            //MPPUtility.dataDump(data, true, true, true, true, true, true, true);
            //MPPUtility.varDataDump(rscVarData, id, true, true, true, true, true, true);

            resource = m_file!!.addResource()

            resource.disableEvents()
            fieldMap.populateContainer(ResourceField::class.java, resource, id, arrayOf(data), rscVarData)
            resource.enableEvents()

            processHyperlinkData(resource, rscVarData.getByteArray(id, fieldMap.getVarDataKey(ResourceField.HYPERLINK_DATA)))
            resource.id = Integer.valueOf(MPPUtility.getInt(data, 4))

            resource.outlineCode1 = m_outlineCodeVarData!!.getUnicodeString(resource.getCachedValue(ResourceField.OUTLINE_CODE1_INDEX) as Integer, OUTLINECODE_DATA)
            resource.outlineCode2 = m_outlineCodeVarData!!.getUnicodeString(resource.getCachedValue(ResourceField.OUTLINE_CODE2_INDEX) as Integer, OUTLINECODE_DATA)
            resource.outlineCode3 = m_outlineCodeVarData!!.getUnicodeString(resource.getCachedValue(ResourceField.OUTLINE_CODE3_INDEX) as Integer, OUTLINECODE_DATA)
            resource.outlineCode4 = m_outlineCodeVarData!!.getUnicodeString(resource.getCachedValue(ResourceField.OUTLINE_CODE4_INDEX) as Integer, OUTLINECODE_DATA)
            resource.outlineCode5 = m_outlineCodeVarData!!.getUnicodeString(resource.getCachedValue(ResourceField.OUTLINE_CODE5_INDEX) as Integer, OUTLINECODE_DATA)
            resource.outlineCode6 = m_outlineCodeVarData!!.getUnicodeString(resource.getCachedValue(ResourceField.OUTLINE_CODE6_INDEX) as Integer, OUTLINECODE_DATA)
            resource.outlineCode7 = m_outlineCodeVarData!!.getUnicodeString(resource.getCachedValue(ResourceField.OUTLINE_CODE7_INDEX) as Integer, OUTLINECODE_DATA)
            resource.outlineCode8 = m_outlineCodeVarData!!.getUnicodeString(resource.getCachedValue(ResourceField.OUTLINE_CODE8_INDEX) as Integer, OUTLINECODE_DATA)
            resource.outlineCode9 = m_outlineCodeVarData!!.getUnicodeString(resource.getCachedValue(ResourceField.OUTLINE_CODE9_INDEX) as Integer, OUTLINECODE_DATA)
            resource.outlineCode10 = m_outlineCodeVarData!!.getUnicodeString(resource.getCachedValue(ResourceField.OUTLINE_CODE10_INDEX) as Integer, OUTLINECODE_DATA)

            resource.uniqueID = id

            metaData = rscFixedMeta.getByteArrayValue(offset!!.intValue())
            resource.setFlag(1, metaData!![28] and 0x40 != 0)
            resource.setFlag(2, metaData!![28] and 0x80 != 0)
            resource.setFlag(3, metaData!![29] and 0x01 != 0)
            resource.setFlag(4, metaData!![29] and 0x02 != 0)
            resource.setFlag(5, metaData!![29] and 0x04 != 0)
            resource.setFlag(6, metaData!![29] and 0x08 != 0)
            resource.setFlag(7, metaData!![29] and 0x10 != 0)
            resource.setFlag(8, metaData!![29] and 0x20 != 0)
            resource.setFlag(9, metaData!![29] and 0x40 != 0)
            resource.setFlag(10, metaData!![28] and 0x20 != 0)
            resource.setFlag(11, metaData!![29] and 0x80 != 0)
            resource.setFlag(12, metaData!![30] and 0x01 != 0)
            resource.setFlag(13, metaData!![30] and 0x02 != 0)
            resource.setFlag(14, metaData!![30] and 0x04 != 0)
            resource.setFlag(15, metaData!![30] and 0x08 != 0)
            resource.setFlag(16, metaData!![30] and 0x10 != 0)
            resource.setFlag(17, metaData!![30] and 0x20 != 0)
            resource.setFlag(18, metaData!![30] and 0x40 != 0)
            resource.setFlag(19, metaData!![30] and 0x80 != 0)
            resource.setFlag(20, metaData!![31] and 0x01 != 0)

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
            processResourceEnterpriseColumns(fieldMap, resource, rscVarData)

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
            if (metaData!![9] and 0x02 != 0) {
                resource.type = ResourceType.WORK
            } else {
                resource.type = ResourceType.MATERIAL
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
        val fieldMap = FieldMap9(m_file!!.projectProperties, m_file!!.customFields)
        fieldMap.createAssignmentFieldMap(m_projectProps)

        val assnDir = m_projectDir!!.getEntry("TBkndAssn") as DirectoryEntry
        val assnVarMeta = VarMeta9(DocumentInputStream(assnDir.getEntry("VarMeta") as DocumentEntry))
        val assnVarData = Var2Data(assnVarMeta, DocumentInputStream(assnDir.getEntry("Var2Data") as DocumentEntry))

        val assnFixedMeta = FixedMeta(DocumentInputStream(assnDir.getEntry("FixedMeta") as DocumentEntry), 34)
        var assnFixedData = FixedData(142, m_inputStreamFactory!!.getInstance(assnDir, "FixedData"))
        if (assnFixedData.itemCount != assnFixedMeta.adjustedItemCount) {
            assnFixedData = FixedData(assnFixedMeta, m_inputStreamFactory!!.getInstance(assnDir, "FixedData"))
        }

        val factory = ResourceAssignmentFactory()
        factory.process(m_file!!, fieldMap, null, m_reader!!.useRawTimephasedData, m_reader!!.preserveNoteFormatting, assnVarMeta, assnVarData, assnFixedMeta, assnFixedData, null, assnFixedMeta.adjustedItemCount)
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
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processViewData() {
        val dir = m_viewDir!!.getEntry("CV_iew") as DirectoryEntry
        val viewVarMeta = VarMeta9(DocumentInputStream(dir.getEntry("VarMeta") as DocumentEntry))
        val viewVarData = Var2Data(viewVarMeta, DocumentInputStream(dir.getEntry("Var2Data") as DocumentEntry))
        val fixedMeta = FixedMeta(DocumentInputStream(dir.getEntry("FixedMeta") as DocumentEntry), 10)
        val fixedData = FixedData(122, m_inputStreamFactory!!.getInstance(dir, "FixedData"))

        val items = fixedMeta.adjustedItemCount
        var view: View
        val factory = ViewFactory9()

        var lastOffset = -1
        for (loop in 0 until items) {
            val fm = fixedMeta.getByteArrayValue(loop)
            val offset = MPPUtility.getShort(fm, 4)
            if (offset > lastOffset) {
                val fd = fixedData.getByteArrayValue(fixedData.getIndexFromOffset(offset))
                if (fd != null) {
                    view = factory.createView(m_file, fm, fd, viewVarData, m_fontBases)
                    m_file!!.views.add(view)
                    //System.out.print(view);
                }
                lastOffset = offset
            }
        }
    }

    /**
     * This method extracts table data from the MPP file.
     *
     * @todo This implementation does not deal with MPP9 files saved by later
     * versions of MS Project
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processTableData() {
        val dir = m_viewDir!!.getEntry("CTable") as DirectoryEntry
        //FixedMeta fixedMeta = new FixedMeta(getEncryptableInputStream(dir, "FixedMeta"), 9);
        val stream = m_inputStreamFactory!!.getInstance(dir, "FixedData")
        val blockSize = if (stream.available() % 115 === 0) 115 else 110
        val fixedData = FixedData(blockSize, stream)
        val varMeta = VarMeta9(DocumentInputStream(dir.getEntry("VarMeta") as DocumentEntry))
        val varData = Var2Data(varMeta, DocumentInputStream(dir.getEntry("Var2Data") as DocumentEntry))

        val container = m_file!!.tables
        val factory = TableFactory(TABLE_COLUMN_DATA_STANDARD, TABLE_COLUMN_DATA_ENTERPRISE, TABLE_COLUMN_DATA_BASELINE)
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
     * @todo Doesn't work correctly with MPP9 files saved by Propject 2007 and 2010
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processFilterData() {
        val dir = m_viewDir!!.getEntry("CFilter") as DirectoryEntry
        //FixedMeta fixedMeta = new FixedMeta(new DocumentInputStream(((DocumentEntry) dir.getEntry("FixedMeta"))), 9);
        //FixedData fixedData = new FixedData(fixedMeta, getEncryptableInputStream(dir, "FixedData"));
        val stream = m_inputStreamFactory!!.getInstance(dir, "FixedData")
        val blockSize = if (stream.available() % 115 === 0) 115 else 110
        val fixedData = FixedData(blockSize, stream, true)
        val varMeta = VarMeta9(DocumentInputStream(dir.getEntry("VarMeta") as DocumentEntry))
        val varData = Var2Data(varMeta, DocumentInputStream(dir.getEntry("Var2Data") as DocumentEntry))

        //System.out.println(fixedMeta);
        //System.out.println(fixedData);
        //System.out.println(varMeta);
        //System.out.println(varData);

        val reader = FilterReader9()
        reader.process(m_file!!.projectProperties, m_file!!.filters, fixedData, varData)
    }

    /**
     * Read group definitions.
     *
     * @todo Doesn't work correctly with MPP9 files saved by Propject 2007 and 2010
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processGroupData() {
        val dir = m_viewDir!!.getEntry("CGrouping") as DirectoryEntry
        //FixedMeta fixedMeta = new FixedMeta(new DocumentInputStream(((DocumentEntry) dir.getEntry("FixedMeta"))), 9);
        val fixedData = FixedData(110, m_inputStreamFactory!!.getInstance(dir, "FixedData"))
        val varMeta = VarMeta9(DocumentInputStream(dir.getEntry("VarMeta") as DocumentEntry))
        val varData = Var2Data(varMeta, DocumentInputStream(dir.getEntry("Var2Data") as DocumentEntry))

        //      System.out.println(fixedMeta);
        //      System.out.println(fixedData);
        //      System.out.println(varMeta);
        //      System.out.println(varData);

        val reader = GroupReader9()
        reader.process(m_file, fixedData, varData, m_fontBases)
    }

    /**
     * Read saved view state from an MPP file.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processSavedViewState() {
        val dir = m_viewDir!!.getEntry("CEdl") as DirectoryEntry
        val varMeta = VarMeta9(DocumentInputStream(dir.getEntry("VarMeta") as DocumentEntry))
        val varData = Var2Data(varMeta, DocumentInputStream(dir.getEntry("Var2Data") as DocumentEntry))
        //System.out.println(varMeta);
        //System.out.println(varData);

        val `is` = m_inputStreamFactory!!.getInstance(dir, "FixedData")
        val fixedData = ByteArray(`is`.available())
        `is`.read(fixedData)
        //System.out.println(ByteArrayHelper.hexdump(fixedData, false, 16, ""));

        val reader = ViewStateReader9()
        reader.process(m_file, varData, fixedData)
    }

    /**
     * This method is called to try to catch any invalid tasks that may have sneaked past all our other checks.
     * This is done by validating the tasks by task ID.
     */
    private fun postProcessTasks() {
        val allTasks = m_file!!.tasks
        if (allTasks.size() > 1) {
            Collections.sort(allTasks)

            var taskID = -1
            var lastTaskID = -1

            for (i in 0 until allTasks.size()) {
                val task = allTasks.get(i)
                taskID = NumberHelper.getInt(task.id)
                // In Project the tasks IDs are always contiguous so we can spot invalid tasks by making sure all
                // IDs are represented.
                if (!task.`null` && lastTaskID != -1 && taskID > lastTaskID + 1) {
                    // This task looks to be invalid.
                    task.`null` = true
                } else {
                    lastTaskID = taskID
                }
            }
        }
    }

    companion object {

        // Signals the end of the list of subproject task unique ids
        private val SUBPROJECT_LISTEND = 0x00000303

        // Signals that the previous value was for the subproject task unique id
        private val SUBPROJECT_TASKUNIQUEID0 = 0x00000000
        private val SUBPROJECT_TASKUNIQUEID1 = 0x0B340000
        private val SUBPROJECT_TASKUNIQUEID2 = 0x0ABB0000
        private val SUBPROJECT_TASKUNIQUEID3 = 0x05A10000
        private val SUBPROJECT_TASKUNIQUEID4 = 0x02F70000
        private val SUBPROJECT_TASKUNIQUEID5 = 0x07010000

        private val TABLE_COLUMN_DATA_STANDARD = Integer.valueOf(1)
        private val TABLE_COLUMN_DATA_ENTERPRISE = Integer.valueOf(2)
        private val TABLE_COLUMN_DATA_BASELINE: Integer? = null
        private val OUTLINECODE_DATA = Integer.valueOf(1)

        /**
         * Mask used to isolate confirmed flag from the duration units field.
         */
        private val DURATION_CONFIRMED_MASK = 0x20

        /**
         * Deleted and null tasks have their ID and UniqueID attributes at fixed offsets.
         */
        private val TASK_UNIQUE_ID_FIXED_OFFSET = 0
        private val TASK_ID_FIXED_OFFSET = 4
        private val NULL_TASK_BLOCK_SIZE = 8

        private val RESOURCE_FIELD_ALIASES = HashMap<Integer, FieldType>()

        init {
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(52), ResourceField.TEXT1)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(53), ResourceField.TEXT2)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(54), ResourceField.TEXT3)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(55), ResourceField.TEXT4)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(56), ResourceField.TEXT5)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(57), ResourceField.TEXT6)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(58), ResourceField.TEXT7)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(59), ResourceField.TEXT8)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(60), ResourceField.TEXT9)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(61), ResourceField.TEXT10)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(62), ResourceField.TEXT11)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(63), ResourceField.TEXT12)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(64), ResourceField.TEXT13)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(65), ResourceField.TEXT14)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(66), ResourceField.TEXT15)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(67), ResourceField.TEXT16)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(68), ResourceField.TEXT17)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(69), ResourceField.TEXT18)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(70), ResourceField.TEXT19)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(71), ResourceField.TEXT20)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(72), ResourceField.TEXT21)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(73), ResourceField.TEXT22)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(74), ResourceField.TEXT23)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(75), ResourceField.TEXT24)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(76), ResourceField.TEXT25)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(77), ResourceField.TEXT26)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(78), ResourceField.TEXT27)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(79), ResourceField.TEXT28)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(80), ResourceField.TEXT29)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(81), ResourceField.TEXT30)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(82), ResourceField.START1)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(83), ResourceField.START2)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(84), ResourceField.START3)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(85), ResourceField.START4)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(86), ResourceField.START5)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(87), ResourceField.START6)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(88), ResourceField.START7)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(89), ResourceField.START8)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(90), ResourceField.START9)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(91), ResourceField.START10)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(92), ResourceField.FINISH1)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(93), ResourceField.FINISH2)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(94), ResourceField.FINISH3)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(95), ResourceField.FINISH4)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(96), ResourceField.FINISH5)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(97), ResourceField.FINISH6)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(98), ResourceField.FINISH7)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(99), ResourceField.FINISH8)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(100), ResourceField.FINISH9)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(101), ResourceField.FINISH10)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(102), ResourceField.NUMBER1)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(103), ResourceField.NUMBER2)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(104), ResourceField.NUMBER3)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(105), ResourceField.NUMBER4)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(106), ResourceField.NUMBER5)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(107), ResourceField.NUMBER6)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(108), ResourceField.NUMBER7)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(109), ResourceField.NUMBER8)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(110), ResourceField.NUMBER9)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(111), ResourceField.NUMBER10)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(112), ResourceField.NUMBER11)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(113), ResourceField.NUMBER12)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(114), ResourceField.NUMBER13)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(115), ResourceField.NUMBER14)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(116), ResourceField.NUMBER15)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(117), ResourceField.NUMBER16)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(118), ResourceField.NUMBER17)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(119), ResourceField.NUMBER18)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(120), ResourceField.NUMBER19)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(121), ResourceField.NUMBER20)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(122), ResourceField.DURATION1)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(123), ResourceField.DURATION2)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(124), ResourceField.DURATION3)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(125), ResourceField.DURATION4)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(126), ResourceField.DURATION5)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(127), ResourceField.DURATION6)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(128), ResourceField.DURATION7)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(129), ResourceField.DURATION8)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(130), ResourceField.DURATION9)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(131), ResourceField.DURATION10)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(145), ResourceField.DATE1)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(146), ResourceField.DATE2)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(147), ResourceField.DATE3)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(148), ResourceField.DATE4)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(149), ResourceField.DATE5)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(150), ResourceField.DATE6)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(151), ResourceField.DATE7)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(152), ResourceField.DATE8)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(153), ResourceField.DATE9)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(154), ResourceField.DATE10)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(155), ResourceField.OUTLINE_CODE1)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(156), ResourceField.OUTLINE_CODE2)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(157), ResourceField.OUTLINE_CODE3)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(158), ResourceField.OUTLINE_CODE4)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(159), ResourceField.OUTLINE_CODE5)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(160), ResourceField.OUTLINE_CODE6)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(161), ResourceField.OUTLINE_CODE7)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(162), ResourceField.OUTLINE_CODE8)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(163), ResourceField.OUTLINE_CODE9)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(164), ResourceField.OUTLINE_CODE10)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(165), ResourceField.FLAG10)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(166), ResourceField.FLAG1)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(167), ResourceField.FLAG2)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(168), ResourceField.FLAG3)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(169), ResourceField.FLAG4)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(170), ResourceField.FLAG5)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(171), ResourceField.FLAG6)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(172), ResourceField.FLAG7)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(173), ResourceField.FLAG8)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(174), ResourceField.FLAG9)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(175), ResourceField.FLAG11)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(176), ResourceField.FLAG12)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(177), ResourceField.FLAG13)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(178), ResourceField.FLAG14)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(179), ResourceField.FLAG15)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(180), ResourceField.FLAG16)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(181), ResourceField.FLAG17)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(182), ResourceField.FLAG18)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(183), ResourceField.FLAG19)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(184), ResourceField.FLAG20)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(207), ResourceField.COST1)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(208), ResourceField.COST2)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(209), ResourceField.COST3)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(210), ResourceField.COST4)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(211), ResourceField.COST5)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(212), ResourceField.COST6)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(213), ResourceField.COST7)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(214), ResourceField.COST8)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(215), ResourceField.COST9)
            RESOURCE_FIELD_ALIASES.put(Integer.valueOf(216), ResourceField.COST10)
        }

        private val TASK_FIELD_ALIASES = HashMap<Integer, FieldType>()

        init {
            TASK_FIELD_ALIASES.put(Integer.valueOf(118), TaskField.TEXT1)
            TASK_FIELD_ALIASES.put(Integer.valueOf(119), TaskField.TEXT2)
            TASK_FIELD_ALIASES.put(Integer.valueOf(120), TaskField.TEXT3)
            TASK_FIELD_ALIASES.put(Integer.valueOf(121), TaskField.TEXT4)
            TASK_FIELD_ALIASES.put(Integer.valueOf(122), TaskField.TEXT5)
            TASK_FIELD_ALIASES.put(Integer.valueOf(123), TaskField.TEXT6)
            TASK_FIELD_ALIASES.put(Integer.valueOf(124), TaskField.TEXT7)
            TASK_FIELD_ALIASES.put(Integer.valueOf(125), TaskField.TEXT8)
            TASK_FIELD_ALIASES.put(Integer.valueOf(126), TaskField.TEXT9)
            TASK_FIELD_ALIASES.put(Integer.valueOf(127), TaskField.TEXT10)
            TASK_FIELD_ALIASES.put(Integer.valueOf(128), TaskField.START1)
            TASK_FIELD_ALIASES.put(Integer.valueOf(129), TaskField.FINISH1)
            TASK_FIELD_ALIASES.put(Integer.valueOf(130), TaskField.START2)
            TASK_FIELD_ALIASES.put(Integer.valueOf(131), TaskField.FINISH2)
            TASK_FIELD_ALIASES.put(Integer.valueOf(132), TaskField.START3)
            TASK_FIELD_ALIASES.put(Integer.valueOf(133), TaskField.FINISH3)
            TASK_FIELD_ALIASES.put(Integer.valueOf(134), TaskField.START4)
            TASK_FIELD_ALIASES.put(Integer.valueOf(135), TaskField.FINISH4)
            TASK_FIELD_ALIASES.put(Integer.valueOf(136), TaskField.START5)
            TASK_FIELD_ALIASES.put(Integer.valueOf(137), TaskField.FINISH5)
            TASK_FIELD_ALIASES.put(Integer.valueOf(138), TaskField.START6)
            TASK_FIELD_ALIASES.put(Integer.valueOf(139), TaskField.FINISH6)
            TASK_FIELD_ALIASES.put(Integer.valueOf(140), TaskField.START7)
            TASK_FIELD_ALIASES.put(Integer.valueOf(141), TaskField.FINISH7)
            TASK_FIELD_ALIASES.put(Integer.valueOf(142), TaskField.START8)
            TASK_FIELD_ALIASES.put(Integer.valueOf(143), TaskField.FINISH8)
            TASK_FIELD_ALIASES.put(Integer.valueOf(144), TaskField.START9)
            TASK_FIELD_ALIASES.put(Integer.valueOf(145), TaskField.FINISH9)
            TASK_FIELD_ALIASES.put(Integer.valueOf(146), TaskField.START10)
            TASK_FIELD_ALIASES.put(Integer.valueOf(147), TaskField.FINISH10)
            TASK_FIELD_ALIASES.put(Integer.valueOf(149), TaskField.NUMBER1)
            TASK_FIELD_ALIASES.put(Integer.valueOf(150), TaskField.NUMBER2)
            TASK_FIELD_ALIASES.put(Integer.valueOf(151), TaskField.NUMBER3)
            TASK_FIELD_ALIASES.put(Integer.valueOf(152), TaskField.NUMBER4)
            TASK_FIELD_ALIASES.put(Integer.valueOf(153), TaskField.NUMBER5)
            TASK_FIELD_ALIASES.put(Integer.valueOf(154), TaskField.NUMBER6)
            TASK_FIELD_ALIASES.put(Integer.valueOf(155), TaskField.NUMBER7)
            TASK_FIELD_ALIASES.put(Integer.valueOf(156), TaskField.NUMBER8)
            TASK_FIELD_ALIASES.put(Integer.valueOf(157), TaskField.NUMBER9)
            TASK_FIELD_ALIASES.put(Integer.valueOf(158), TaskField.NUMBER10)
            TASK_FIELD_ALIASES.put(Integer.valueOf(159), TaskField.DURATION1)
            TASK_FIELD_ALIASES.put(Integer.valueOf(161), TaskField.DURATION2)
            TASK_FIELD_ALIASES.put(Integer.valueOf(163), TaskField.DURATION3)
            TASK_FIELD_ALIASES.put(Integer.valueOf(165), TaskField.DURATION4)
            TASK_FIELD_ALIASES.put(Integer.valueOf(167), TaskField.DURATION5)
            TASK_FIELD_ALIASES.put(Integer.valueOf(169), TaskField.DURATION6)
            TASK_FIELD_ALIASES.put(Integer.valueOf(171), TaskField.DURATION7)
            TASK_FIELD_ALIASES.put(Integer.valueOf(173), TaskField.DURATION8)
            TASK_FIELD_ALIASES.put(Integer.valueOf(175), TaskField.DURATION9)
            TASK_FIELD_ALIASES.put(Integer.valueOf(177), TaskField.DURATION10)
            TASK_FIELD_ALIASES.put(Integer.valueOf(184), TaskField.DATE1)
            TASK_FIELD_ALIASES.put(Integer.valueOf(185), TaskField.DATE2)
            TASK_FIELD_ALIASES.put(Integer.valueOf(186), TaskField.DATE3)
            TASK_FIELD_ALIASES.put(Integer.valueOf(187), TaskField.DATE4)
            TASK_FIELD_ALIASES.put(Integer.valueOf(188), TaskField.DATE5)
            TASK_FIELD_ALIASES.put(Integer.valueOf(189), TaskField.DATE6)
            TASK_FIELD_ALIASES.put(Integer.valueOf(190), TaskField.DATE7)
            TASK_FIELD_ALIASES.put(Integer.valueOf(191), TaskField.DATE8)
            TASK_FIELD_ALIASES.put(Integer.valueOf(192), TaskField.DATE9)
            TASK_FIELD_ALIASES.put(Integer.valueOf(193), TaskField.DATE10)
            TASK_FIELD_ALIASES.put(Integer.valueOf(194), TaskField.TEXT11)
            TASK_FIELD_ALIASES.put(Integer.valueOf(195), TaskField.TEXT12)
            TASK_FIELD_ALIASES.put(Integer.valueOf(196), TaskField.TEXT13)
            TASK_FIELD_ALIASES.put(Integer.valueOf(197), TaskField.TEXT14)
            TASK_FIELD_ALIASES.put(Integer.valueOf(198), TaskField.TEXT15)
            TASK_FIELD_ALIASES.put(Integer.valueOf(199), TaskField.TEXT16)
            TASK_FIELD_ALIASES.put(Integer.valueOf(200), TaskField.TEXT17)
            TASK_FIELD_ALIASES.put(Integer.valueOf(201), TaskField.TEXT18)
            TASK_FIELD_ALIASES.put(Integer.valueOf(202), TaskField.TEXT19)
            TASK_FIELD_ALIASES.put(Integer.valueOf(203), TaskField.TEXT20)
            TASK_FIELD_ALIASES.put(Integer.valueOf(204), TaskField.TEXT21)
            TASK_FIELD_ALIASES.put(Integer.valueOf(205), TaskField.TEXT22)
            TASK_FIELD_ALIASES.put(Integer.valueOf(206), TaskField.TEXT23)
            TASK_FIELD_ALIASES.put(Integer.valueOf(207), TaskField.TEXT24)
            TASK_FIELD_ALIASES.put(Integer.valueOf(208), TaskField.TEXT25)
            TASK_FIELD_ALIASES.put(Integer.valueOf(209), TaskField.TEXT26)
            TASK_FIELD_ALIASES.put(Integer.valueOf(210), TaskField.TEXT27)
            TASK_FIELD_ALIASES.put(Integer.valueOf(211), TaskField.TEXT28)
            TASK_FIELD_ALIASES.put(Integer.valueOf(212), TaskField.TEXT29)
            TASK_FIELD_ALIASES.put(Integer.valueOf(213), TaskField.TEXT30)
            TASK_FIELD_ALIASES.put(Integer.valueOf(214), TaskField.NUMBER11)
            TASK_FIELD_ALIASES.put(Integer.valueOf(215), TaskField.NUMBER12)
            TASK_FIELD_ALIASES.put(Integer.valueOf(216), TaskField.NUMBER13)
            TASK_FIELD_ALIASES.put(Integer.valueOf(217), TaskField.NUMBER14)
            TASK_FIELD_ALIASES.put(Integer.valueOf(218), TaskField.NUMBER15)
            TASK_FIELD_ALIASES.put(Integer.valueOf(219), TaskField.NUMBER16)
            TASK_FIELD_ALIASES.put(Integer.valueOf(220), TaskField.NUMBER17)
            TASK_FIELD_ALIASES.put(Integer.valueOf(221), TaskField.NUMBER18)
            TASK_FIELD_ALIASES.put(Integer.valueOf(222), TaskField.NUMBER19)
            TASK_FIELD_ALIASES.put(Integer.valueOf(223), TaskField.NUMBER20)
            TASK_FIELD_ALIASES.put(Integer.valueOf(227), TaskField.OUTLINE_CODE1)
            TASK_FIELD_ALIASES.put(Integer.valueOf(228), TaskField.OUTLINE_CODE2)
            TASK_FIELD_ALIASES.put(Integer.valueOf(229), TaskField.OUTLINE_CODE3)
            TASK_FIELD_ALIASES.put(Integer.valueOf(230), TaskField.OUTLINE_CODE4)
            TASK_FIELD_ALIASES.put(Integer.valueOf(231), TaskField.OUTLINE_CODE5)
            TASK_FIELD_ALIASES.put(Integer.valueOf(232), TaskField.OUTLINE_CODE6)
            TASK_FIELD_ALIASES.put(Integer.valueOf(233), TaskField.OUTLINE_CODE7)
            TASK_FIELD_ALIASES.put(Integer.valueOf(234), TaskField.OUTLINE_CODE8)
            TASK_FIELD_ALIASES.put(Integer.valueOf(235), TaskField.OUTLINE_CODE9)
            TASK_FIELD_ALIASES.put(Integer.valueOf(236), TaskField.OUTLINE_CODE10)
            TASK_FIELD_ALIASES.put(Integer.valueOf(237), TaskField.FLAG1)
            TASK_FIELD_ALIASES.put(Integer.valueOf(238), TaskField.FLAG2)
            TASK_FIELD_ALIASES.put(Integer.valueOf(239), TaskField.FLAG3)
            TASK_FIELD_ALIASES.put(Integer.valueOf(240), TaskField.FLAG4)
            TASK_FIELD_ALIASES.put(Integer.valueOf(241), TaskField.FLAG5)
            TASK_FIELD_ALIASES.put(Integer.valueOf(242), TaskField.FLAG6)
            TASK_FIELD_ALIASES.put(Integer.valueOf(243), TaskField.FLAG7)
            TASK_FIELD_ALIASES.put(Integer.valueOf(244), TaskField.FLAG8)
            TASK_FIELD_ALIASES.put(Integer.valueOf(245), TaskField.FLAG9)
            TASK_FIELD_ALIASES.put(Integer.valueOf(246), TaskField.FLAG10)
            TASK_FIELD_ALIASES.put(Integer.valueOf(247), TaskField.FLAG11)
            TASK_FIELD_ALIASES.put(Integer.valueOf(248), TaskField.FLAG12)
            TASK_FIELD_ALIASES.put(Integer.valueOf(249), TaskField.FLAG13)
            TASK_FIELD_ALIASES.put(Integer.valueOf(250), TaskField.FLAG14)
            TASK_FIELD_ALIASES.put(Integer.valueOf(251), TaskField.FLAG15)
            TASK_FIELD_ALIASES.put(Integer.valueOf(252), TaskField.FLAG16)
            TASK_FIELD_ALIASES.put(Integer.valueOf(253), TaskField.FLAG17)
            TASK_FIELD_ALIASES.put(Integer.valueOf(254), TaskField.FLAG18)
            TASK_FIELD_ALIASES.put(Integer.valueOf(255), TaskField.FLAG19)
            TASK_FIELD_ALIASES.put(Integer.valueOf(256), TaskField.FLAG20)
            TASK_FIELD_ALIASES.put(Integer.valueOf(278), TaskField.COST1)
            TASK_FIELD_ALIASES.put(Integer.valueOf(279), TaskField.COST2)
            TASK_FIELD_ALIASES.put(Integer.valueOf(280), TaskField.COST3)
            TASK_FIELD_ALIASES.put(Integer.valueOf(281), TaskField.COST4)
            TASK_FIELD_ALIASES.put(Integer.valueOf(282), TaskField.COST5)
            TASK_FIELD_ALIASES.put(Integer.valueOf(283), TaskField.COST6)
            TASK_FIELD_ALIASES.put(Integer.valueOf(284), TaskField.COST7)
            TASK_FIELD_ALIASES.put(Integer.valueOf(285), TaskField.COST8)
            TASK_FIELD_ALIASES.put(Integer.valueOf(286), TaskField.COST9)
            TASK_FIELD_ALIASES.put(Integer.valueOf(287), TaskField.COST10)
        }
    }

}
