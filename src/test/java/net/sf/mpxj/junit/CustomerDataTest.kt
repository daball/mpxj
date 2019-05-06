/*
 * file:       CustomerDataTest.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2008
 * date:       27/11/2008
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

package net.sf.mpxj.junit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

import java.io.File
import java.io.FileInputStream
import java.util.ArrayList
import java.util.Date
import java.util.Locale

import org.junit.Test

import net.sf.mpxj.ProjectFile
import net.sf.mpxj.common.FileHelper
import net.sf.mpxj.json.JsonWriter
import net.sf.mpxj.mpx.MPXReader
import net.sf.mpxj.mpx.MPXWriter
import net.sf.mpxj.mspdi.MSPDIReader
import net.sf.mpxj.mspdi.MSPDIWriter
import net.sf.mpxj.planner.PlannerWriter
import net.sf.mpxj.primavera.PrimaveraPMFileWriter
import net.sf.mpxj.primavera.PrimaveraXERFileReader
import net.sf.mpxj.reader.UniversalProjectReader
import net.sf.mpxj.writer.ProjectWriter

/**
 * The tests contained in this class exercise MPXJ
 * using customer supplied data.
 */
class CustomerDataTest {

    private val m_privateDirectory: File?
    private val m_baselineDirectory: File?
    private val m_universalReader: UniversalProjectReader
    private val m_mpxReader: MPXReader
    private val m_xerReader: PrimaveraXERFileReader

    /**
     * Constructor.
     */
    init {
        m_privateDirectory = configureDirectory("mpxj.junit.privatedir")
        m_baselineDirectory = configureDirectory("mpxj.junit.baselinedir")

        m_universalReader = UniversalProjectReader()
        m_mpxReader = MPXReader()
        m_xerReader = PrimaveraXERFileReader()
    }

    /**
     * Test customer data.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCustomerData1() {
        testCustomerData(1, 10)
    }

    /**
     * Test customer data.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCustomerData2() {
        testCustomerData(2, 10)
    }

    /**
     * Test customer data.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCustomerData3() {
        testCustomerData(3, 10)
    }

    /**
     * Test customer data.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCustomerData4() {
        testCustomerData(4, 10)
    }

    /**
     * Test customer data.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCustomerData5() {
        testCustomerData(5, 10)
    }

    /**
     * Test customer data.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCustomerData6() {
        testCustomerData(6, 10)
    }

    /**
     * Test customer data.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCustomerData7() {
        testCustomerData(7, 10)
    }

    /**
     * Test customer data.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCustomerData8() {
        testCustomerData(8, 10)
    }

    /**
     * Test customer data.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCustomerData9() {
        testCustomerData(9, 10)
    }

    /**
     * Test customer data.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCustomerData10() {
        testCustomerData(10, 10)
    }

    /**
     * Create a File instance from a path stored as a property.
     *
     * @param propertyName property name
     * @return File instance
     */
    private fun configureDirectory(propertyName: String): File? {
        var dir: File? = null
        val dirName = System.getProperty(propertyName)
        if (dirName != null && !dirName!!.isEmpty()) {
            dir = File(dirName)
            if (!dir!!.exists() || !dir!!.isDirectory()) {
                dir = null
            }
        }

        return dir
    }

    /**
     * As part of the bug reports that are submitted for MPXJ I am passed a
     * number of confidential project files, which for obvious reasons cannot
     * be redistributed as test cases. These files reside in a directory on
     * my development machine, and assuming that this directory exists, this
     * test will attempt of read each of the files in turn.
     *
     * @param index current chunk
     * @param max maximum number of chunks
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun testCustomerData(index: Int, max: Int) {
        if (m_privateDirectory != null) {
            val files = ArrayList<File>()
            listFiles(files, m_privateDirectory!!)

            val interval = files.size() / max
            val startIndex = (index - 1) * interval
            val endIndex: Int
            if (index == max) {
                endIndex = files.size()
            } else {
                endIndex = startIndex + interval
            }

            executeTests(files.subList(startIndex, endIndex))
        }
    }

    /**
     * Recursively descend through the test data directory adding files to the list.
     *
     * @param list file list
     * @param parent parent directory
     */
    private fun listFiles(list: List<File>, parent: File) {
        val runtime = System.getProperty("java.runtime.name")
        val isIKVM = runtime != null && runtime!!.indexOf("IKVM") !== -1
        val fileList = parent.listFiles()
        assertNotNull(fileList)

        for (file in fileList) {
            if (file.isDirectory()) {
                listFiles(list, file)
            } else {
                val name = file.getName().toLowerCase()
                if (isIKVM && (name.endsWith(".mpd") || name.endsWith(".mdb"))) {
                    continue
                }
                list.add(file)
            }
        }
    }

    /**
     * Validate that all of the files in the list can be read by MPXJ.
     *
     * @param files file list
     */
    private fun executeTests(files: List<File>) {
        var failures = 0
        for (file in files) {
            val name = file.getName().toUpperCase()
            if (name.endsWith(".MPP.XML")) {
                continue
            }

            var mpxj: ProjectFile? = null
            //System.out.println(name);

            try {
                mpxj = testReader(name, file)
                if (mpxj == null) {
                    System.err.println("Failed to read $name")
                    ++failures
                } else {
                    if (!testBaseline(file, mpxj)) {
                        System.err.println("Failed to validate baseline $name")
                        ++failures
                    } else {
                        testWriters(mpxj)
                    }
                }
            } catch (ex: Exception) {
                System.err.println("Failed to read $name")
                ex.printStackTrace()
                ++failures
            }

        }

        assertEquals("Failed to read $failures files", 0, failures.toLong())
    }

    /**
     * Ensure that we can read the file.
     *
     * @param name file name
     * @param file File instance
     * @return ProjectFile instance
     */
    @Throws(Exception::class)
    private fun testReader(name: String, file: File): ProjectFile? {
        var mpxj: ProjectFile? = null

        if (name.endsWith(".MPX") === true) {
            m_mpxReader.locale = Locale.ENGLISH

            if (name.indexOf(".DE.") !== -1) {
                m_mpxReader.locale = Locale.GERMAN
            }

            if (name.indexOf(".SV.") !== -1) {
                m_mpxReader.locale = Locale("sv")
            }

            mpxj = m_mpxReader.read(file)
        } else {
            mpxj = m_universalReader.read(file)
            if (name.endsWith(".MPP")) {
                validateMpp(file.getCanonicalPath(), mpxj)
            }

            // If we have an XER file, exercise the "readAll" functionality too.
            // For now, ignore files with non-standard encodings.
            if (name.endsWith(".XER") && !name.endsWith(".ENCODING.XER")) {
                m_xerReader.readAll(FileInputStream(file), true)
            }
        }

        return mpxj
    }

    /**
     * Generate new files from the file under test and compare them to a baseline
     * we have previously created. This potentially allows us to capture unintended
     * changes in functionality. If we do not have a baseline for this particular
     * file, we'll generate one.
     *
     * @param file file under test
     * @param project ProjectFile instance
     * @return true if the baseline test is successful
     */
    @Throws(Exception::class)
    private fun testBaseline(file: File, project: ProjectFile): Boolean {
        if (m_baselineDirectory == null) {
            return true
        }

        val mspdi = testBaseline(file, project, File(m_baselineDirectory, "mspdi"), MSPDIWriter::class.java!!)
        val pmxml = testBaseline(file, project, File(m_baselineDirectory, "pmxml"), PrimaveraPMFileWriter::class.java!!)

        return mspdi && pmxml
    }

    /**
     * Generate a baseline for a specific file type.
     *
     * @param file file under test
     * @param project ProjectFile instance
     * @param baselineDirectory baseline directory location
     * @param writerClass file writer class
     * @return true if the baseline test is successful
     */
    @SuppressWarnings("unused")
    @Throws(Exception::class)
    private fun testBaseline(file: File, project: ProjectFile, baselineDirectory: File, writerClass: Class<out ProjectWriter>): Boolean {
        var success = true
        val sourceDirNameLength = m_privateDirectory!!.getPath().length()
        val baselineFile = File(baselineDirectory, file.getPath().substring(sourceDirNameLength) + ".xml")

        val writer = writerClass.newInstance()
        project.projectProperties.currentDate = BASELINE_CURRENT_DATE

        if (baselineFile.exists()) {
            val out = File.createTempFile("junit", ".xml")
            writer.write(project, out)
            success = FileUtility.equals(baselineFile, out)

            if (success || !DEBUG_FAILURES) {
                FileHelper.deleteQuietly(out)
            } else {
                System.out.println()
                System.out.println("Baseline: " + baselineFile.getPath())
                System.out.println("Test: " + out.getPath())
                System.out.println("copy /y \"" + out.getPath() + "\" \"" + baselineFile.getPath() + "\"")
            }
        } else {
            FileHelper.mkdirsQuietly(baselineFile.getParentFile())
            writer.write(project, baselineFile)
        }

        return success
    }

    /**
     * Ensure that we can export the file under test through our writers, without error.
     *
     * @param project ProjectFile instance
     */
    @Throws(Exception::class)
    private fun testWriters(project: ProjectFile) {
        for (c in WRITER_CLASSES) {
            val outputFile = File.createTempFile("writer_test", ".dat")
            outputFile.deleteOnExit()
            val p = c.newInstance()
            p.write(project, outputFile)
            FileHelper.deleteQuietly(outputFile)
        }
    }

    /**
     * As part of the regression test process, I save customer's MPP files
     * as MSPDI files using a version of MS Project. This method allows these
     * two versions to be compared in order to ensure that MPXJ is
     * correctly reading the data from both file formats.
     *
     * @param name file name
     * @param mpp MPP file data structure
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun validateMpp(name: String, mpp: ProjectFile?) {
        val xmlFile = File("$name.xml")
        if (xmlFile.exists() === true) {
            val xml = MSPDIReader().read(xmlFile)
            val compare = MppXmlCompare()
            compare.process(xml, mpp!!)
        }
    }

    companion object {

        private val WRITER_CLASSES = ArrayList<Class<out ProjectWriter>>()

        private val BASELINE_CURRENT_DATE = Date(1544100702438L)

        private val DEBUG_FAILURES = false

        init {
            WRITER_CLASSES.add(JsonWriter::class.java)

            // Exercised by baseline test
            //WRITER_CLASSES.add(MSPDIWriter.class);

            WRITER_CLASSES.add(PlannerWriter::class.java)

            // Exercise by baseline test
            //WRITER_CLASSES.add(PrimaveraPMFileWriter.class);

            // Not reliable enough results to include
            // WRITER_CLASSES.add(SDEFWriter.class);

            // Write MPX last as applying locale settings will change some project values
            WRITER_CLASSES.add(MPXWriter::class.java)
        }
    }
}
