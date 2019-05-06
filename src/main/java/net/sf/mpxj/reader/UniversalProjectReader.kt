/*
 * file:       UniversalProjectReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2016
 * date:       2016-10-13
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

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.ArrayList
import java.util.Arrays
import java.util.HashSet
import java.util.LinkedList
import java.util.Properties
import java.util.regex.Pattern

import org.apache.poi.poifs.filesystem.POIFSFileSystem

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.asta.AstaDatabaseFileReader
import net.sf.mpxj.asta.AstaDatabaseReader
import net.sf.mpxj.asta.AstaFileReader
import net.sf.mpxj.common.CharsetHelper
import net.sf.mpxj.common.FileHelper
import net.sf.mpxj.common.InputStreamHelper
import net.sf.mpxj.common.StreamHelper
import net.sf.mpxj.conceptdraw.ConceptDrawProjectReader
import net.sf.mpxj.fasttrack.FastTrackReader
import net.sf.mpxj.ganttdesigner.GanttDesignerReader
import net.sf.mpxj.ganttproject.GanttProjectReader
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.merlin.MerlinReader
import net.sf.mpxj.mpd.MPDDatabaseReader
import net.sf.mpxj.mpp.MPPReader
import net.sf.mpxj.mpx.MPXReader
import net.sf.mpxj.mspdi.MSPDIReader
import net.sf.mpxj.phoenix.PhoenixInputStream
import net.sf.mpxj.phoenix.PhoenixReader
import net.sf.mpxj.planner.PlannerReader
import net.sf.mpxj.primavera.PrimaveraDatabaseReader
import net.sf.mpxj.primavera.PrimaveraPMFileReader
import net.sf.mpxj.primavera.PrimaveraXERFileReader
import net.sf.mpxj.primavera.p3.P3DatabaseReader
import net.sf.mpxj.primavera.p3.P3PRXFileReader
import net.sf.mpxj.primavera.suretrak.SureTrakDatabaseReader
import net.sf.mpxj.primavera.suretrak.SureTrakSTXFileReader
import net.sf.mpxj.projectlibre.ProjectLibreReader
import net.sf.mpxj.synchro.SynchroReader
import net.sf.mpxj.turboproject.TurboProjectReader

/**
 * This class implements a universal project reader: given a file or a stream this reader
 * will sample the content and determine the type of file it has been given. It will then
 * instantiate the correct reader for that file type and proceed to read the file.
 */
class UniversalProjectReader : ProjectReader {

    private var m_skipBytes: Int = 0
    private var m_charset: Charset? = null
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
     * Package private method used when handling byte order mark.
     * Tells the reader to skip a number of bytes before starting to read from the stream.
     *
     * @param skipBytes number of bytes to skip
     */
    internal fun setSkipBytes(skipBytes: Int) {
        m_skipBytes = skipBytes
    }

    /**
     * Package private method used when handling byte order mark.
     * Notes the charset indicated by the byte order mark.
     *
     * @param charset character set indicated by byte order mark
     */
    internal fun setCharset(charset: Charset) {
        m_charset = charset
    }

    @Override
    @Throws(MPXJException::class)
    override fun read(fileName: String): ProjectFile? {
        return read(File(fileName))
    }

    @Override
    @Throws(MPXJException::class)
    override fun read(file: File): ProjectFile? {
        try {
            val result: ProjectFile?
            if (file.isDirectory()) {
                result = handleDirectory(file)
            } else {
                var fis: FileInputStream? = null

                try {
                    fis = FileInputStream(file)
                    val projectFile = read(fis)
                    fis!!.close()
                    return projectFile
                } finally {
                    StreamHelper.closeQuietly(fis)
                }
            }
            return result
        } catch (ex: Exception) {
            throw MPXJException(MPXJException.INVALID_FILE, ex)
        }

    }

    /**
     * Note that this method returns null if we can't determine the file type.
     *
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(inputStream: InputStream): ProjectFile? {
        try {
            val bis = BufferedInputStream(inputStream)
            bis.skip(m_skipBytes)
            bis.mark(BUFFER_SIZE)
            val buffer = ByteArray(BUFFER_SIZE)
            val bytesRead = bis.read(buffer)
            bis.reset()

            //
            // If the file is smaller than the buffer we are peeking into,
            // it's probably not a valid schedule file.
            //
            if (bytesRead != BUFFER_SIZE) {
                return null
            }

            //
            // Always check for BOM first. Regex-based fingerprints may ignore these otherwise.
            //
            if (matchesFingerprint(buffer, UTF8_BOM_FINGERPRINT)) {
                return handleByteOrderMark(bis, UTF8_BOM_FINGERPRINT.size, CharsetHelper.UTF8)
            }

            if (matchesFingerprint(buffer, UTF16_BOM_FINGERPRINT)) {
                return handleByteOrderMark(bis, UTF16_BOM_FINGERPRINT.size, CharsetHelper.UTF16)
            }

            if (matchesFingerprint(buffer, UTF16LE_BOM_FINGERPRINT)) {
                return handleByteOrderMark(bis, UTF16LE_BOM_FINGERPRINT.size, CharsetHelper.UTF16LE)
            }

            //
            // Now check for file fingerprints
            //
            if (matchesFingerprint(buffer, BINARY_PLIST)) {
                return handleBinaryPropertyList(bis)
            }

            if (matchesFingerprint(buffer, OLE_COMPOUND_DOC_FINGERPRINT)) {
                return handleOleCompoundDocument(bis)
            }

            if (matchesFingerprint(buffer, MSPDI_FINGERPRINT_1) || matchesFingerprint(buffer, MSPDI_FINGERPRINT_2)) {
                val reader = MSPDIReader()
                reader.charset = m_charset
                return reader.read(bis)
            }

            if (matchesFingerprint(buffer, PP_FINGERPRINT)) {
                return readProjectFile(AstaFileReader(), bis)
            }

            if (matchesFingerprint(buffer, MPX_FINGERPRINT)) {
                return readProjectFile(MPXReader(), bis)
            }

            if (matchesFingerprint(buffer, XER_FINGERPRINT)) {
                return handleXerFile(bis)
            }

            if (matchesFingerprint(buffer, PLANNER_FINGERPRINT)) {
                return readProjectFile(PlannerReader(), bis)
            }

            if (matchesFingerprint(buffer, PMXML_FINGERPRINT)) {
                return readProjectFile(PrimaveraPMFileReader(), bis)
            }

            if (matchesFingerprint(buffer, MDB_FINGERPRINT)) {
                return handleMDBFile(bis)
            }

            if (matchesFingerprint(buffer, SQLITE_FINGERPRINT)) {
                return handleSQLiteFile(bis)
            }

            if (matchesFingerprint(buffer, ZIP_FINGERPRINT)) {
                return handleZipFile(bis)
            }

            if (matchesFingerprint(buffer, PHOENIX_FINGERPRINT)) {
                return readProjectFile(PhoenixReader(), PhoenixInputStream(bis))
            }

            if (matchesFingerprint(buffer, PHOENIX_XML_FINGERPRINT)) {
                return readProjectFile(PhoenixReader(), bis)
            }

            if (matchesFingerprint(buffer, FASTTRACK_FINGERPRINT)) {
                return readProjectFile(FastTrackReader(), bis)
            }

            if (matchesFingerprint(buffer, PROJECTLIBRE_FINGERPRINT)) {
                return readProjectFile(ProjectLibreReader(), bis)
            }

            if (matchesFingerprint(buffer, GANTTPROJECT_FINGERPRINT)) {
                return readProjectFile(GanttProjectReader(), bis)
            }

            if (matchesFingerprint(buffer, TURBOPROJECT_FINGERPRINT)) {
                return readProjectFile(TurboProjectReader(), bis)
            }

            if (matchesFingerprint(buffer, DOS_EXE_FINGERPRINT)) {
                return handleDosExeFile(bis)
            }

            if (matchesFingerprint(buffer, CONCEPT_DRAW_FINGERPRINT)) {
                return readProjectFile(ConceptDrawProjectReader(), bis)
            }

            if (matchesFingerprint(buffer, SYNCHRO_FINGERPRINT)) {
                return readProjectFile(SynchroReader(), bis)
            }

            return if (matchesFingerprint(buffer, GANTT_DESIGNER_FINGERPRINT)) {
                readProjectFile(GanttDesignerReader(), bis)
            } else null

        } catch (ex: Exception) {
            throw MPXJException(MPXJException.INVALID_FILE, ex)
        }

    }

    /**
     * Determine if the start of the buffer matches a fingerprint byte array.
     *
     * @param buffer bytes from file
     * @param fingerprint fingerprint bytes
     * @return true if the file matches the fingerprint
     */
    private fun matchesFingerprint(buffer: ByteArray, fingerprint: ByteArray): Boolean {
        return Arrays.equals(fingerprint, Arrays.copyOf(buffer, fingerprint.size))
    }

    /**
     * Determine if the buffer, when expressed as text, matches a fingerprint regular expression.
     *
     * @param buffer bytes from file
     * @param fingerprint fingerprint regular expression
     * @return true if the file matches the fingerprint
     */
    private fun matchesFingerprint(buffer: ByteArray, fingerprint: Pattern): Boolean {
        return fingerprint.matcher(if (m_charset == null) String(buffer) else String(buffer, m_charset)).matches()
    }

    /**
     * Adds listeners and reads from a stream.
     *
     * @param reader reader for file type
     * @param stream schedule data
     * @return ProjectFile instance
     */
    @Throws(MPXJException::class)
    private fun readProjectFile(reader: ProjectReader, stream: InputStream): ProjectFile {
        addListeners(reader)
        return reader.read(stream)
    }

    /**
     * Adds listeners and reads from a file.
     *
     * @param reader reader for file type
     * @param file schedule data
     * @return ProjectFile instance
     */
    @Throws(MPXJException::class)
    private fun readProjectFile(reader: ProjectReader, file: File): ProjectFile {
        addListeners(reader)
        return reader.read(file)
    }

    /**
     * We have an OLE compound document... but is it an MPP file?
     *
     * @param stream file input stream
     * @return ProjectFile instance
     */
    @Throws(Exception::class)
    private fun handleOleCompoundDocument(stream: InputStream): ProjectFile? {
        val fs = POIFSFileSystem(POIFSFileSystem.createNonClosingInputStream(stream))
        val fileFormat = MPPReader.getFileFormat(fs)
        if (fileFormat != null && fileFormat.startsWith("MSProject")) {
            val reader = MPPReader()
            addListeners(reader)
            return reader.read(fs)
        }
        return null
    }

    /**
     * We have a binary property list.
     *
     * @param stream file input stream
     * @return ProjectFile instance
     */
    @Throws(Exception::class)
    private fun handleBinaryPropertyList(stream: InputStream): ProjectFile? {
        // This is an unusual case. I have seen an instance where an MSPDI file was downloaded
        // as a web archive, which is a binary property list containing the file data.
        // This confused the UniversalProjectReader as it found a valid MSPDI fingerprint
        // but the binary plist header caused the XML parser to fail.
        // I'm not inclined to add support for extracting files from binary plists at the moment,
        // so adding this fingerprint allows us to cleanly reject the file as unsupported
        // rather than getting a confusing error from one of the other file type readers.
        return null
    }

    /**
     * We have identified that we have an MDB file. This could be a Microsoft Project database
     * or an Asta database. Open the database and use the table names present to determine
     * which type this is.
     *
     * @param stream schedule data
     * @return ProjectFile instance
     */
    @Throws(Exception::class)
    private fun handleMDBFile(stream: InputStream): ProjectFile? {
        val file = InputStreamHelper.writeStreamToTempFile(stream, ".mdb")

        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver")
            val url = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb);DBQ=" + file.getCanonicalPath()
            val tableNames = populateTableNames(url)

            if (tableNames.contains("MSP_PROJECTS")) {
                return readProjectFile(MPDDatabaseReader(), file)
            }

            return if (tableNames.contains("EXCEPTIONN")) {
                readProjectFile(AstaDatabaseReader(), file)
            } else null

        } finally {
            FileHelper.deleteQuietly(file)
        }
    }

    /**
     * We have identified that we have a SQLite file. This could be a Primavera Project database
     * or an Asta database. Open the database and use the table names present to determine
     * which type this is.
     *
     * @param stream schedule data
     * @return ProjectFile instance
     */
    @Throws(Exception::class)
    private fun handleSQLiteFile(stream: InputStream): ProjectFile? {
        val file = InputStreamHelper.writeStreamToTempFile(stream, ".sqlite")

        try {
            Class.forName("org.sqlite.JDBC")
            val url = "jdbc:sqlite:" + file.getCanonicalPath()
            val tableNames = populateTableNames(url)

            if (tableNames.contains("EXCEPTIONN")) {
                return readProjectFile(AstaDatabaseFileReader(), file)
            }

            if (tableNames.contains("PROJWBS")) {
                var connection: Connection? = null
                try {
                    val props = Properties()
                    props.setProperty("date_string_format", "yyyy-MM-dd HH:mm:ss")
                    connection = DriverManager.getConnection(url, props)
                    val reader = PrimaveraDatabaseReader()
                    reader.setConnection(connection)
                    addListeners(reader)
                    return reader.read()
                } finally {
                    if (connection != null) {
                        connection!!.close()
                    }
                }
            }

            return if (tableNames.contains("ZSCHEDULEITEM")) {
                readProjectFile(MerlinReader(), file)
            } else null

        } finally {
            FileHelper.deleteQuietly(file)
        }
    }

    /**
     * We have identified that we have a zip file. Extract the contents into
     * a temporary directory and process.
     *
     * @param stream schedule data
     * @return ProjectFile instance
     */
    @Throws(Exception::class)
    private fun handleZipFile(stream: InputStream): ProjectFile? {
        var dir: File? = null

        try {
            dir = InputStreamHelper.writeZipStreamToTempDir(stream)
            val result = handleDirectory(dir)
            if (result != null) {
                return result
            }
        } finally {
            FileHelper.deleteQuietly(dir)
        }

        return null
    }

    /**
     * We have a directory. Determine if this contains a multi-file database we understand, if so
     * process it. If it does not contain a database, test each file within the directory
     * structure to determine if it contains a file whose format we understand.
     *
     * @param directory directory to process
     * @return ProjectFile instance if we can process anything, or null
     */
    @Throws(Exception::class)
    private fun handleDirectory(directory: File?): ProjectFile? {
        var result = handleDatabaseInDirectory(directory!!)
        if (result == null) {
            result = handleFileInDirectory(directory!!)
        }
        return result
    }

    /**
     * Given a directory, determine if it contains a multi-file database whose format
     * we can process.
     *
     * @param directory directory to process
     * @return ProjectFile instance if we can process anything, or null
     */
    @Throws(Exception::class)
    private fun handleDatabaseInDirectory(directory: File): ProjectFile? {
        val buffer = ByteArray(BUFFER_SIZE)
        val files = directory.listFiles()
        if (files != null) {
            for (file in files!!) {
                if (file.isDirectory()) {
                    continue
                }

                val fis = FileInputStream(file)
                val bytesRead = fis.read(buffer)
                fis.close()

                //
                // If the file is smaller than the buffer we are peeking into,
                // it's probably not a valid schedule file.
                //
                if (bytesRead != BUFFER_SIZE) {
                    continue
                }

                if (matchesFingerprint(buffer, BTRIEVE_FINGERPRINT)) {
                    return handleP3BtrieveDatabase(directory)
                }

                if (matchesFingerprint(buffer, STW_FINGERPRINT)) {
                    return handleSureTrakDatabase(directory)
                }
            }
        }
        return null
    }

    /**
     * Given a directory, determine if it  (or any subdirectory) contains a file
     * whose format we understand.
     *
     * @param directory directory to process
     * @return ProjectFile instance if we can process anything, or null
     */
    @Throws(Exception::class)
    private fun handleFileInDirectory(directory: File): ProjectFile? {
        val directories = ArrayList<File>()
        val files = directory.listFiles()

        if (files != null) {
            // Try files first
            for (file in files!!) {
                if (file.isDirectory()) {
                    directories.add(file)
                } else {
                    val reader = UniversalProjectReader()
                    val result = reader.read(file)
                    if (result != null) {
                        return result
                    }
                }
            }

            // Haven't found a file we can read? Try the directories.
            for (file in directories) {
                val result = handleDirectory(file)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    /**
     * Determine if we have a P3 Btrieve multi-file database.
     *
     * @param directory directory to process
     * @return ProjectFile instance if we can process anything, or null
     */
    @Throws(Exception::class)
    private fun handleP3BtrieveDatabase(directory: File): ProjectFile? {
        return P3DatabaseReader.setProjectNameAndRead(directory)
    }

    /**
     * Determine if we have a SureTrak multi-file database.
     *
     * @param directory directory to process
     * @return ProjectFile instance if we can process anything, or null
     */
    @Throws(Exception::class)
    private fun handleSureTrakDatabase(directory: File): ProjectFile? {
        return SureTrakDatabaseReader.setProjectNameAndRead(directory)
    }

    /**
     * The file we are working with has a byte order mark. Skip this and try again to read the file.
     *
     * @param stream schedule data
     * @param length length of the byte order mark
     * @param charset charset indicated by byte order mark
     * @return ProjectFile instance
     */
    @Throws(Exception::class)
    private fun handleByteOrderMark(stream: InputStream, length: Int, charset: Charset): ProjectFile? {
        val reader = UniversalProjectReader()
        reader.setSkipBytes(length)
        reader.setCharset(charset)
        return reader.read(stream)
    }

    /**
     * This could be a self-extracting archive. If we understand the format, expand
     * it and check the content for files we can read.
     *
     * @param stream schedule data
     * @return ProjectFile instance
     */
    @Throws(Exception::class)
    private fun handleDosExeFile(stream: InputStream): ProjectFile? {
        val file = InputStreamHelper.writeStreamToTempFile(stream, ".tmp")
        var `is`: InputStream? = null

        try {
            `is` = FileInputStream(file)
            if (`is`!!.available() > 1350) {
                StreamHelper.skip(`is`, 1024)

                // Bytes at offset 1024
                var data = ByteArray(2)
                `is`!!.read(data)

                if (matchesFingerprint(data, WINDOWS_NE_EXE_FINGERPRINT)) {
                    StreamHelper.skip(`is`, 286)

                    // Bytes at offset 1312
                    data = ByteArray(34)
                    `is`!!.read(data)
                    if (matchesFingerprint(data, PRX_FINGERPRINT)) {
                        `is`!!.close()
                        `is` = null
                        return readProjectFile(P3PRXFileReader(), file)
                    }
                }

                if (matchesFingerprint(data, STX_FINGERPRINT)) {
                    StreamHelper.skip(`is`, 31742)
                    // Bytes at offset 32768
                    data = ByteArray(4)
                    `is`!!.read(data)
                    if (matchesFingerprint(data, PRX3_FINGERPRINT)) {
                        `is`!!.close()
                        `is` = null
                        return readProjectFile(SureTrakSTXFileReader(), file)
                    }
                }
            }
            return null
        } finally {
            StreamHelper.closeQuietly(`is`)
            FileHelper.deleteQuietly(file)
        }
    }

    /**
     * XER files can contain multiple projects when there are cross-project dependencies.
     * As the UniversalProjectReader is designed just to read a single project, we need
     * to select one project from those available in the XER file.
     * The original project selected for export by the user will have its "export flag"
     * set to true. We'll return the first project we find where the export flag is
     * set to true, otherwise we'll just return the first project we find in the file.
     *
     * @param stream schedule data
     * @return ProjectFile instance
     */
    @Throws(Exception::class)
    private fun handleXerFile(stream: InputStream): ProjectFile? {
        val reader = PrimaveraXERFileReader()
        reader.charset = m_charset
        val projects = reader.readAll(stream)
        var project: ProjectFile? = null
        for (file in projects) {
            if (file.projectProperties.exportFlag) {
                project = file
                break
            }
        }
        if (project == null && !projects.isEmpty()) {
            project = projects.get(0)
        }
        return project
    }

    /**
     * Open a database and build a set of table names.
     *
     * @param url database URL
     * @return set containing table names
     */
    @Throws(SQLException::class)
    private fun populateTableNames(url: String): Set<String> {
        val tableNames = HashSet<String>()
        var connection: Connection? = null
        var rs: ResultSet? = null

        try {
            connection = DriverManager.getConnection(url)
            val dmd = connection!!.getMetaData()
            rs = dmd.getTables(null, null, null, null)
            while (rs!!.next()) {
                tableNames.add(rs!!.getString("TABLE_NAME").toUpperCase())
            }
        } finally {
            if (rs != null) {
                rs!!.close()
            }

            if (connection != null) {
                connection!!.close()
            }
        }

        return tableNames
    }

    /**
     * Adds any listeners attached to this reader to the reader created internally.
     *
     * @param reader internal project reader
     */
    private fun addListeners(reader: ProjectReader) {
        if (m_projectListeners != null) {
            for (listener in m_projectListeners!!) {
                reader.addProjectListener(listener)
            }
        }
    }

    companion object {

        private val BUFFER_SIZE = 512

        private val OLE_COMPOUND_DOC_FINGERPRINT = byteArrayOf(0xD0.toByte(), 0xCF.toByte(), 0x11.toByte(), 0xE0.toByte(), 0xA1.toByte(), 0xB1.toByte(), 0x1A.toByte(), 0xE1.toByte())

        private val PP_FINGERPRINT = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte())

        private val MPX_FINGERPRINT = byteArrayOf('M'.toByte(), 'P'.toByte(), 'X'.toByte())

        private val MDB_FINGERPRINT = byteArrayOf(0x00.toByte(), 0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 'S'.toByte(), 't'.toByte(), 'a'.toByte(), 'n'.toByte(), 'd'.toByte(), 'a'.toByte(), 'r'.toByte(), 'd'.toByte(), ' '.toByte(), 'J'.toByte(), 'e'.toByte(), 't'.toByte(), ' '.toByte(), 'D'.toByte(), 'B'.toByte())

        private val SQLITE_FINGERPRINT = byteArrayOf('S'.toByte(), 'Q'.toByte(), 'L'.toByte(), 'i'.toByte(), 't'.toByte(), 'e'.toByte(), ' '.toByte(), 'f'.toByte(), 'o'.toByte(), 'r'.toByte(), 'm'.toByte(), 'a'.toByte(), 't'.toByte())

        private val XER_FINGERPRINT = byteArrayOf('E'.toByte(), 'R'.toByte(), 'M'.toByte(), 'H'.toByte(), 'D'.toByte(), 'R'.toByte())

        private val ZIP_FINGERPRINT = byteArrayOf('P'.toByte(), 'K'.toByte())

        private val PHOENIX_FINGERPRINT = byteArrayOf('P'.toByte(), 'P'.toByte(), 'X'.toByte(), '!'.toByte(), '!'.toByte(), '!'.toByte(), '!'.toByte())

        private val BINARY_PLIST = byteArrayOf('b'.toByte(), 'p'.toByte(), 'l'.toByte(), 'i'.toByte(), 's'.toByte(), 't'.toByte())

        private val FASTTRACK_FINGERPRINT = byteArrayOf(0x1C.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x8B.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())

        private val PROJECTLIBRE_FINGERPRINT = byteArrayOf(0xAC.toByte(), 0xED.toByte(), 0x00.toByte(), 0x05.toByte())

        private val BTRIEVE_FINGERPRINT = byteArrayOf(0x46.toByte(), 0x43.toByte(), 0x00.toByte(), 0x00.toByte())

        private val STW_FINGERPRINT = byteArrayOf(0x53.toByte(), 0x54.toByte(), 0x57.toByte())

        private val DOS_EXE_FINGERPRINT = byteArrayOf(0x4D.toByte(), 0x5A.toByte())

        private val WINDOWS_NE_EXE_FINGERPRINT = byteArrayOf(0x4E.toByte(), 0x45.toByte())

        private val STX_FINGERPRINT = byteArrayOf(0x55.toByte(), 0x8B.toByte())

        private val SYNCHRO_FINGERPRINT = byteArrayOf(0xB6.toByte(), 0x17.toByte())

        private val UTF8_BOM_FINGERPRINT = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

        private val UTF16_BOM_FINGERPRINT = byteArrayOf(0xFE.toByte(), 0xFF.toByte())

        private val UTF16LE_BOM_FINGERPRINT = byteArrayOf(0xFF.toByte(), 0xFE.toByte())

        private val PLANNER_FINGERPRINT = Pattern.compile(".*<project.*mrproject-version.*", Pattern.DOTALL)

        private val PMXML_FINGERPRINT = Pattern.compile(".*(<BusinessObjects|APIBusinessObjects).*", Pattern.DOTALL)

        private val MSPDI_FINGERPRINT_1 = Pattern.compile(".*xmlns=\"http://schemas\\.microsoft\\.com/project.*", Pattern.DOTALL)

        private val MSPDI_FINGERPRINT_2 = Pattern.compile(".*<Project.*<SaveVersion>.*", Pattern.DOTALL)

        private val PHOENIX_XML_FINGERPRINT = Pattern.compile(".*<project.*version=\"(\\d+|\\d+\\.\\d+)\".*update_mode=\"(true|false)\".*>.*", Pattern.DOTALL)

        private val GANTTPROJECT_FINGERPRINT = Pattern.compile(".*<project.*webLink.*", Pattern.DOTALL)

        private val TURBOPROJECT_FINGERPRINT = Pattern.compile(".*dWBSTAB.*", Pattern.DOTALL)

        private val PRX_FINGERPRINT = Pattern.compile("!Self-Extracting Primavera Project", Pattern.DOTALL)

        private val PRX3_FINGERPRINT = Pattern.compile("PRX3", Pattern.DOTALL)

        private val CONCEPT_DRAW_FINGERPRINT = Pattern.compile(".*Application=\\\"CDProject\\\".*", Pattern.DOTALL)

        private val GANTT_DESIGNER_FINGERPRINT = Pattern.compile(".*<Gantt Version=.*", Pattern.DOTALL)
    }

}
