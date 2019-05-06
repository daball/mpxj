/*
 * file:       MPDDatabaseReader.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2007
 * date:       02/02/2006
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

package net.sf.mpxj.mpd

import java.io.File
import java.io.InputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.LinkedList

import javax.sql.DataSource

import net.sf.mpxj.MPXJException
import net.sf.mpxj.ProjectFile
import net.sf.mpxj.listener.ProjectListener
import net.sf.mpxj.reader.ProjectReader

/**
 * This class provides a generic front end to read project data from
 * a database.
 */
class MPDDatabaseReader : ProjectReader {

    private var m_projectID: Integer? = null
    private var m_dataSource: DataSource? = null
    private var m_connection: Connection? = null
    private var m_preserveNoteFormatting: Boolean = false
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
     * Populates a Map instance representing the IDs and names of
     * projects available in the current database.
     *
     * @return Map instance containing ID and name pairs
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    fun listProjects(): Map<Integer, String> {
        val reader = MPD9DatabaseReader()
        return reader.listProjects()
    }

    /**
     * Read project data from a database.
     *
     * @return ProjectFile instance
     * @throws MPXJException
     */
    @Throws(MPXJException::class)
    fun read(): ProjectFile {
        val reader = MPD9DatabaseReader()
        reader.setProjectID(m_projectID)
        reader.setPreserveNoteFormatting(m_preserveNoteFormatting)
        reader.setDataSource(m_dataSource)
        reader.setConnection(m_connection)
        return reader.read()
    }

    /**
     * Set the ID of the project to be read.
     *
     * @param projectID project ID
     */
    fun setProjectID(projectID: Int) {
        m_projectID = Integer.valueOf(projectID)
    }

    /**
     * This method sets a flag to indicate whether the RTF formatting associated
     * with notes should be preserved or removed. By default the formatting
     * is removed.
     *
     * @param preserveNoteFormatting boolean flag
     */
    fun setPreserveNoteFormatting(preserveNoteFormatting: Boolean) {
        m_preserveNoteFormatting = preserveNoteFormatting
    }

    /**
     * Set the data source. A DataSource or a Connection can be supplied
     * to this class to allow connection to the database.
     *
     * @param dataSource data source
     */
    fun setDataSource(dataSource: DataSource) {
        m_dataSource = dataSource
    }

    /**
     * Sets the connection. A DataSource or a Connection can be supplied
     * to this class to allow connection to the database.
     *
     * @param connection database connection
     */
    fun setConnection(connection: Connection) {
        m_connection = connection
    }

    /**
     * This is a convenience method which reads the first project
     * from the named MPD file using the JDBC-ODBC bridge driver.
     *
     * @param accessDatabaseFileName access database file name
     * @return ProjectFile instance
     * @throws MPXJException
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(accessDatabaseFileName: String): ProjectFile {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver")
            val url = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb);DBQ=$accessDatabaseFileName"
            m_connection = DriverManager.getConnection(url)
            m_projectID = Integer.valueOf(1)
            return read()
        } catch (ex: ClassNotFoundException) {
            throw MPXJException("Failed to load JDBC driver", ex)
        } catch (ex: SQLException) {
            throw MPXJException("Failed to create connection", ex)
        } finally {
            if (m_connection != null) {
                try {
                    m_connection!!.close()
                } catch (ex: SQLException) {
                    // silently ignore exceptions when closing connection
                }

            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Throws(MPXJException::class)
    override fun read(file: File): ProjectFile {
        return read(file.getAbsolutePath())
    }

    /**
     * {@inheritDoc}
     */
    @Override
    override fun read(inputStream: InputStream): ProjectFile {
        throw UnsupportedOperationException()
    }
}