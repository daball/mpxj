/*
 * file:       PrimaveraConvert.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2010
 * date:       23/03/2010
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

package net.sf.mpxj.sample

import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

import net.sf.mpxj.ProjectFile
import net.sf.mpxj.primavera.PrimaveraDatabaseReader
import net.sf.mpxj.writer.ProjectWriter
import net.sf.mpxj.writer.ProjectWriterUtility

/**
 * This utility is design simply to illustrate the use of the
 * PrimaveraReader class. Example commend line:
 *
 * PrimaveraConvert "net.sourceforge.jtds.jdbc.Driver" "jdbc:jtds:sqlserver://localhost/PMDB;user=pmdb;password=pmdb" 1 "c:\temp\project1.xml"
 *
 * This assumes the use of the JTDS JDBC driver to access the PMDB database on
 * a local SQL Server instance. The project with ID=1 is exported to
 * an MSPDI file.
 */
class PrimaveraConvert {

    /**
     * Extract Primavera project data and export in another format.
     *
     * @param driverClass JDBC driver class name
     * @param connectionString JDBC connection string
     * @param projectID project ID
     * @param outputFile output file
     * @throws Exception
     */
    @Throws(Exception::class)
    fun process(driverClass: String, connectionString: String, projectID: String, outputFile: String) {
        System.out.println("Reading Primavera database started.")

        Class.forName(driverClass)
        val props = Properties()

        //
        // This is not a very robust way to detect that we're working with SQLlite...
        // If you are trying to grab data from
        // a standalone P6 using SQLite, the SQLite JDBC driver needs this property
        // in order to correctly parse timestamps.
        //
        if (driverClass.equals("org.sqlite.JDBC")) {
            props.setProperty("date_string_format", "yyyy-MM-dd HH:mm:ss")
        }

        val c = DriverManager.getConnection(connectionString, props)
        val reader = PrimaveraDatabaseReader()
        reader.setConnection(c)

        processProject(reader, Integer.parseInt(projectID), outputFile)
    }

    /**
     * Process a single project.
     *
     * @param reader Primavera reader
     * @param projectID required project ID
     * @param outputFile output file name
     */
    @Throws(Exception::class)
    private fun processProject(reader: PrimaveraDatabaseReader, projectID: Int, outputFile: String) {
        var start = System.currentTimeMillis()
        reader.setProjectID(projectID)
        val projectFile = reader.read()
        var elapsed = System.currentTimeMillis() - start
        System.out.println("Reading database completed in " + elapsed + "ms.")

        System.out.println("Writing output file started.")
        start = System.currentTimeMillis()
        val writer = ProjectWriterUtility.getProjectWriter(outputFile)
        writer.write(projectFile, outputFile)
        elapsed = System.currentTimeMillis() - start
        System.out.println("Writing output completed in " + elapsed + "ms.")
    }

    companion object {
        /**
         * Main method.
         *
         * @param args array of command line arguments
         */
        fun main(args: Array<String>) {
            try {
                if (args.size != 4) {
                    System.out.println("Usage: PrimaveraConvert <JDBC Driver Class> <JDBC connection string> <project ID> <output file name>")
                } else {
                    val convert = PrimaveraConvert()
                    convert.process(args[0], args[1], args[2], args[3])
                }
            } catch (ex: Exception) {
                ex.printStackTrace(System.out)
            }

        }
    }
}
