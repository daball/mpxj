/*
 * file:       MppCleanUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2008
 * date:       07/02/2008
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

package net.sf.mpxj.utility

import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashMap

import org.apache.poi.poifs.filesystem.DirectoryEntry
import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.DocumentInputStream
import org.apache.poi.poifs.filesystem.POIFSFileSystem

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.ProjectProperties
import net.sf.mpxj.Resource
import net.sf.mpxj.Task
import net.sf.mpxj.common.NumberHelper
import net.sf.mpxj.mpp.MPPReader

/**
 * This class allows the caller to replace the content of an MPP file
 * to make it anonymous, in such a way that the structure of the project
 * is maintained unchanged. The point of this exercise is to allow end
 * customers who use MPXJ functionality to submit problematic project files
 * obtain support. The fact that the structure of the file is maintained
 * unchanged means that it is likely that the problem with the file will
 * still be apparent. It also means that end users are more likely to
 * submit these files as, along with the removal of sensitive information, this
 * utility means that no user effort is required to modify the file
 * before it is sent to the organisation providing support.
 *
 * Note the following items are made anonymous:
 * - Task Names
 * - Resource Names
 * - Resource Initials
 * - Project Summary Data
 */
class MppCleanUtility {

    private var m_project: ProjectFile? = null
    private var m_projectDir: DirectoryEntry? = null

    /**
     * Process an MPP file to make it anonymous.
     *
     * @param input input file name
     * @param output output file name
     * @throws Exception
     */
    @Throws(MPXJException::class, IOException::class)
    private fun process(input: String, output: String) {
        //
        // Extract the project data
        //
        val reader = MPPReader()
        m_project = reader.read(input)

        val varDataFileName: String
        val projectDirName: String
        val mppFileType = NumberHelper.getInt(m_project!!.projectProperties.mppFileType)
        when (mppFileType) {
            8 -> {
                projectDirName = "   1"
                varDataFileName = "FixDeferFix   0"
            }

            9 -> {
                projectDirName = "   19"
                varDataFileName = "Var2Data"
            }

            12 -> {
                projectDirName = "   112"
                varDataFileName = "Var2Data"
            }

            14 -> {
                projectDirName = "   114"
                varDataFileName = "Var2Data"
            }

            else -> {
                throw IllegalArgumentException("Unsupported file type $mppFileType")
            }
        }

        //
        // Load the raw file
        //
        val `is` = FileInputStream(input)
        val fs = POIFSFileSystem(`is`)
        `is`.close()

        //
        // Locate the root of the project file system
        //
        val root = fs.getRoot()
        m_projectDir = root.getEntry(projectDirName)

        //
        // Process Tasks
        //
        val replacements = HashMap<String, String>()
        for (task in m_project!!.tasks) {
            mapText(task.name, replacements)
        }
        processReplacements(m_projectDir!!.getEntry("TBkndTask") as DirectoryEntry, varDataFileName, replacements, true)

        //
        // Process Resources
        //
        replacements.clear()
        for (resource in m_project!!.resources) {
            mapText(resource.name, replacements)
            mapText(resource.initials, replacements)
        }
        processReplacements(m_projectDir!!.getEntry("TBkndRsc") as DirectoryEntry, varDataFileName, replacements, true)

        //
        // Process project properties
        //
        replacements.clear()
        val properties = m_project!!.projectProperties
        mapText(properties.projectTitle, replacements)
        processReplacements(m_projectDir!!, "Props", replacements, true)

        replacements.clear()
        mapText(properties.projectTitle, replacements)
        mapText(properties.subject, replacements)
        mapText(properties.author, replacements)
        mapText(properties.keywords, replacements)
        mapText(properties.comments, replacements)
        processReplacements(root, "\u0005SummaryInformation", replacements, false)

        replacements.clear()
        mapText(properties.manager, replacements)
        mapText(properties.company, replacements)
        mapText(properties.category, replacements)
        processReplacements(root, "\u0005DocumentSummaryInformation", replacements, false)

        //
        // Write the replacement raw file
        //
        val os = FileOutputStream(output)
        fs.writeFilesystem(os)
        os.flush()
        os.close()
        fs.close()
    }

    /**
     * Extracts a block of data from the MPP file, and iterates through the map
     * of find/replace pairs to make the data anonymous.
     *
     * @param parentDirectory parent directory object
     * @param fileName target file name
     * @param replacements find/replace data
     * @param unicode true for double byte text
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processReplacements(parentDirectory: DirectoryEntry, fileName: String, replacements: Map<String, String>, unicode: Boolean) {
        //
        // Populate a list of keys and sort into descending order of length
        //
        val keys = ArrayList<String>(replacements.keySet())
        Collections.sort(keys, object : Comparator<String>() {
            @Override
            fun compare(o1: String, o2: String): Int {
                return o2.length() - o1.length()
            }
        })

        //
        // Extract the raw file data
        //
        val targetFile = parentDirectory.getEntry(fileName) as DocumentEntry
        val dis = DocumentInputStream(targetFile)
        val dataSize = dis.available()
        val data = ByteArray(dataSize)
        dis.read(data)
        dis.close()

        //
        // Replace the text
        //
        for (findText in keys) {
            val replaceText = replacements[findText]
            replaceData(data, findText, replaceText, unicode)
        }

        //
        // Remove the document entry
        //
        targetFile.delete()

        //
        // Replace it with a new one
        //
        parentDirectory.createDocument(fileName, ByteArrayInputStream(data))
    }

    /**
     * Converts plan text into anonymous text. Preserves upper case, lower case,
     * punctuation, whitespace and digits while making the text unreadable.
     *
     * @param oldText text to replace
     * @param replacements map of find/replace pairs
     */
    private fun mapText(oldText: String?, replacements: Map<String, String>) {
        var c2: Char = 0.toChar()
        if (oldText != null && oldText.length() !== 0 && !replacements.containsKey(oldText)) {
            val newText = StringBuilder(oldText.length())
            for (loop in 0 until oldText.length()) {
                val c = oldText.charAt(loop)
                if (Character.isUpperCase(c)) {
                    newText.append('X')
                } else {
                    if (Character.isLowerCase(c)) {
                        newText.append('x')
                    } else {
                        if (Character.isDigit(c)) {
                            newText.append('0')
                        } else {
                            if (Character.isLetter(c)) {
                                // Handle other codepages etc. If possible find a way to
                                // maintain the same code page as original.
                                // E.g. replace with a character from the same alphabet.
                                // This 'should' work for most cases
                                if (c2.toInt() == 0) {
                                    c2 = c
                                }
                                newText.append(c2)
                            } else {
                                newText.append(c)
                            }
                        }
                    }
                }
            }

            replacements.put(oldText, newText.toString())
        }
    }

    /**
     * For a given find/replace pair, iterate through the supplied block of data
     * and perform a find and replace.
     *
     * @param data data block
     * @param findText text to find
     * @param replaceText replacement text
     * @param unicode true if text is double byte
     */
    private fun replaceData(data: ByteArray, findText: String, replaceText: String, unicode: Boolean) {
        var replaced = false
        val findBytes = getBytes(findText, unicode)
        val replaceBytes = getBytes(replaceText, unicode)
        val endIndex = data.size - findBytes.size
        var index = 0
        while (index <= endIndex) {
            if (compareBytes(findBytes, data, index)) {
                System.arraycopy(replaceBytes, 0, data, index, replaceBytes.size)
                index += replaceBytes.size
                System.out.println("$findText -> $replaceText")
                replaced = true
            }
            index++
        }
        if (!replaced) {
            System.out.println("Failed to find $findText")
        }
    }

    /**
     * Convert a Java String instance into the equivalent array of single or
     * double bytes.
     *
     * @param value Java String instance representing text
     * @param unicode true if double byte characters are required
     * @return byte array representing the supplied text
     */
    private fun getBytes(value: String, unicode: Boolean): ByteArray {
        val result: ByteArray
        if (unicode) {
            var start = 0
            // Get the bytes in UTF-16
            var bytes: ByteArray

            try {
                bytes = value.getBytes("UTF-16")
            } catch (e: UnsupportedEncodingException) {
                bytes = value.getBytes()
            }

            if (bytes.size > 2 && bytes[0].toInt() == -2 && bytes[1].toInt() == -1) {
                // Skip the unicode identifier
                start = 2
            }
            result = ByteArray(bytes.size - start)
            var loop = start
            while (loop < bytes.size - 1) {
                // Swap the order here
                result[loop - start] = bytes[loop + 1]
                result[loop + 1 - start] = bytes[loop]
                loop += 2
            }
        } else {
            result = ByteArray(value.length() + 1)
            System.arraycopy(value.getBytes(), 0, result, 0, value.length())
        }
        return result
    }

    /**
     * Compare an array of bytes with a subsection of a larger array of bytes.
     *
     * @param lhs small array of bytes
     * @param rhs large array of bytes
     * @param rhsOffset offset into larger array of bytes
     * @return true if a match is found
     */
    private fun compareBytes(lhs: ByteArray, rhs: ByteArray, rhsOffset: Int): Boolean {
        var result = true
        for (loop in lhs.indices) {
            if (lhs[loop] != rhs[rhsOffset + loop]) {
                result = false
                break
            }
        }
        return result
    }

    companion object {
        /**
         * Main method.
         *
         * @param args array of command line arguments
         */
        fun main(args: Array<String>) {
            try {
                if (args.size != 2) {
                    System.out.println("Usage: MppClean <input mpp file name> <output mpp file name>")
                } else {
                    System.out.println("Clean started.")
                    val start = System.currentTimeMillis()
                    val clean = MppCleanUtility()
                    clean.process(args[0], args[1])
                    val elapsed = System.currentTimeMillis() - start
                    System.out.println("Clean completed in " + elapsed + "ms")
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }
    }
}
