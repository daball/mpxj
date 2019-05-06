/*
 * file:       DataExportUtility.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2011
 * date:       05/04/2011
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

import java.io.FileWriter
import java.io.PrintWriter
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Types
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Simple utility to export data to an XML file from an arbitrary database
 * schema.
 */
class DataExportUtility {

    /**
     * Export data base contents to a directory using supplied connection.
     *
     * @param connection database connection
     * @param directory target directory
     * @throws Exception
     */
    @Throws(Exception::class)
    fun process(connection: Connection, directory: String) {
        connection.setAutoCommit(true)

        //
        // Retrieve meta data about the connection
        //
        val dmd = connection.getMetaData()

        val types = arrayOf("TABLE")

        val fw = FileWriter(directory)
        val pw = PrintWriter(fw)

        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        pw.println()
        pw.println("<database>")

        val tables = dmd.getTables(null, null, null, types)
        while (tables.next() === true) {
            processTable(pw, connection, tables.getString("TABLE_NAME"))
        }

        pw.println("</database>")

        pw.close()

        tables.close()
    }

    /**
     * Process a single table.
     *
     * @param pw output print writer
     * @param connection database connection
     * @param name table name
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun processTable(pw: PrintWriter, connection: Connection, name: String) {
        System.out.println("Processing $name")

        //
        // Prepare statement to retrieve all data
        //
        val ps = connection.prepareStatement("select * from $name")

        //
        // Execute the query
        //
        val rs = ps.executeQuery()

        //
        // Retrieve column meta data
        //
        val rmd = ps.getMetaData()

        var index: Int
        val columnCount = rmd.getColumnCount()
        val columnNames = arrayOfNulls<String>(columnCount)
        val columnTypes = IntArray(columnCount)
        val columnPrecision = IntArray(columnCount)
        val columnScale = IntArray(columnCount)

        index = 0
        while (index < columnCount) {
            columnNames[index] = rmd.getColumnName(index + 1)
            columnTypes[index] = rmd.getColumnType(index + 1)
            if (columnTypes[index] == Types.NUMERIC) {
                columnPrecision[index] = rmd.getPrecision(index + 1)
                columnScale[index] = rmd.getScale(index + 1)
            }
            index++
        }

        //
        // Generate the output file
        //
        pw.println("<table name=\"$name\">")

        val buffer = StringBuilder(255)
        val df = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.UK)

        while (rs.next() === true) {
            pw.println(" <row>")

            index = 0
            while (index < columnCount) {
                when (columnTypes[index]) {
                    Types.BINARY, Types.BLOB, Types.LONGVARBINARY, Types.VARBINARY -> {
                        pw.print("  <column name=\"" + columnNames[index] + "\" type=\"" + columnTypes[index] + "\">")

                        pw.println("[BINARY DATA]")

                        pw.println("</column>")
                    }

                    Types.DATE, Types.TIME -> {
                        val data = rs.getDate(index + 1)
                        //if (data != null)
                        run {
                            pw.print("  <column name=\"" + columnNames[index] + "\" type=\"" + columnTypes[index] + "\">")
                            if (data != null) {
                                pw.print(df.format(data))
                            }
                            pw.println("</column>")
                        }
                    }

                    Types.TIMESTAMP -> {
                        val data = rs.getTimestamp(index + 1)
                        //if (data != null)
                        run {
                            pw.print("  <column name=\"" + columnNames[index] + "\" type=\"" + columnTypes[index] + "\">")
                            if (data != null) {
                                pw.print(data!!.toString())
                            }
                            pw.println("</column>")
                        }
                    }

                    Types.NUMERIC -> {
                        //
                        // If we have a non-null value, map the value to a
                        // more specific type
                        //
                        val data = rs.getString(index + 1)
                        //if (data != null)
                        run {
                            var type = Types.NUMERIC
                            val precision = columnPrecision[index]
                            val scale = columnScale[index]

                            if (scale == 0) {
                                if (precision == 10) {
                                    type = Types.INTEGER
                                } else {
                                    if (precision == 5) {
                                        type = Types.SMALLINT
                                    } else {
                                        if (precision == 1) {
                                            type = Types.BIT
                                        }
                                    }
                                }
                            } else {
                                if (precision > 125) {
                                    type = Types.DOUBLE
                                }
                            }

                            pw.print("  <column name=\"" + columnNames[index] + "\" type=\"" + type + "\">")
                            if (data != null) {
                                pw.print(data)
                            }
                            pw.println("</column>")
                        }
                    }

                    else -> {
                        val data = rs.getString(index + 1)
                        //if (data != null)
                        run {
                            pw.print("  <column name=\"" + columnNames[index] + "\" type=\"" + columnTypes[index] + "\">")
                            if (data != null) {
                                pw.print(escapeText(buffer, data!!))
                            }
                            pw.println("</column>")
                        }
                    }
                }
                index++
            }

            pw.println(" </row>")
        }

        pw.println("</table>")

        ps.close()

    }

    /**
     * Quick and dirty XML text escape.
     *
     * @param sb working string buffer
     * @param text input text
     * @return escaped text
     */
    private fun escapeText(sb: StringBuilder, text: String): String {
        val length = text.length()
        var c: Char

        sb.setLength(0)

        for (loop in 0 until length) {
            c = text.charAt(loop)

            when (c) {
                '<' -> {
                    sb.append("&lt;")
                }

                '>' -> {
                    sb.append("&gt;")
                }

                '&' -> {
                    sb.append("&amp;")
                }

                else -> {
                    if (validXMLCharacter(c)) {
                        if (c.toInt() > 127) {
                            sb.append("&#" + c.toInt() + ";")
                        } else {
                            sb.append(c)
                        }
                    }
                }
            }
        }

        return sb.toString()
    }

    /**
     * Quick and dirty valid XML character test.
     *
     * @param c input character
     * @return Boolean flag
     */
    private fun validXMLCharacter(c: Char): Boolean {
        return c.toInt() == 0x9 || c.toInt() == 0xA || c.toInt() == 0xD || c.toInt() >= 0x20 && c.toInt() <= 0xD7FF || c.toInt() >= 0xE000 && c.toInt() <= 0xFFFD
    }

    companion object {
        /**
         * Command line entry point.
         *
         * @param argv command line arguments
         */
        fun main(argv: Array<String>) {
            if (argv.size != 2) {
                System.out.println("DataExport <filename> <output directory>")
            } else {
                var connection: Connection? = null

                try {
                    Class.forName("sun.jdbc.odbc.JdbcOdbcDriver")
                    val url = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb);DBQ=" + argv[0]
                    connection = DriverManager.getConnection(url)

                    val dx = DataExportUtility()
                    dx.process(connection!!, argv[1])
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                    if (connection != null) {
                        try {
                            connection!!.close()
                        } catch (ex: SQLException) {
                            // silently ignore exceptions when closing connection
                        }

                    }
                }
            }
        }
    }
}
