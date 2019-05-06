/*
 * file:       Column.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2003
 * date:       02/11/2003
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

package net.sf.mpxj

import java.io.PrintWriter
import java.io.StringWriter
import java.util.Locale

/**
 * This class represents a column in an MS Project table. The attributes held
 * here describe the layout of the column, along with the title text that has
 * been associated with the column. The title text will either be the default
 * value supplied by MS Project, or it will be a user defined value.
 */
class Column
/**
 * Constructor.
 *
 * @param project reference to the parent project
 */
(private val m_project: ProjectFile) {

    /**
     * Retrieves a value representing the alignment of data displayed in
     * the column.
     *
     * @return alignment type
     */
    /**
     * Sets the alignment of the data in the column.
     *
     * @param alignment data alignment
     */
    var alignData: Int
        get() = m_alignData
        set(alignment) {
            m_alignData = alignment
        }

    /**
     * Retrieves a value representing the alignment of the column title text.
     *
     * @return alignment type
     */
    /**
     * Sets the alignment of the column title.
     *
     * @param alignment column title alignment
     */
    var alignTitle: Int
        get() = m_alignTitle
        set(alignment) {
            m_alignTitle = alignment
        }

    /**
     * Retrieves the type data displayed in the column. This identifier indicates
     * what data will appear in the column, and the default column title
     * that will appear if the user has not provided a user defined column title.
     *
     * @return field type
     */
    /**
     * Sets the type data displayed in the column. This identifier indicates
     * what data will appear in the column, and the default column title
     * that will appear if the user has not provided a user defined column title.
     *
     * @param type field type
     */
    var fieldType: FieldType?
        get() = m_fieldType
        set(type) {
            m_fieldType = type
        }

    /**
     * Retrieves the column title.
     *
     * @return column title
     */
    /**
     * Sets the user defined column title.
     *
     * @param title user defined column title
     */
    var title: String?
        get() = getTitle(Locale.getDefault())
        set(title) {
            m_title = title
        }

    private var m_fieldType: FieldType? = null
    /**
     * Retrieves the width of the column represented as a number of
     * characters.
     *
     * @return column width
     */
    /**
     * Sets the width of the column in characters.
     *
     * @param width column width
     */
    var width: Int = 0
    private var m_alignTitle: Int = 0
    private var m_alignData: Int = 0
    private var m_title: String? = null

    /**
     * Retrieves the column title for the given locale.
     *
     * @param locale required locale for the default column title
     * @return column title
     */
    fun getTitle(locale: Locale): String? {
        var result: String? = null

        if (m_title != null) {
            result = m_title
        } else {
            if (m_fieldType != null) {
                result = m_project.customFields.getCustomField(m_fieldType).getAlias()
                if (result == null) {
                    result = m_fieldType!!.getName(locale)
                }
            }
        }

        return result
    }

    /**
     * This method dumps the contents of this column as a String.
     * Note that this facility is provided as a debugging aid.
     *
     * @return formatted contents of this column
     */
    @Override
    fun toString(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        pw.print("[Column type=")
        pw.print(m_fieldType)

        pw.print(" width=")
        pw.print(width)

        pw.print(" titleAlignment=")
        if (m_alignTitle == ALIGN_LEFT) {
            pw.print("LEFT")
        } else {
            if (m_alignTitle == ALIGN_CENTER) {
                pw.print("CENTER")
            } else {
                pw.print("RIGHT")
            }
        }

        pw.print(" dataAlignment=")
        if (m_alignData == ALIGN_LEFT) {
            pw.print("LEFT")
        } else {
            if (m_alignData == ALIGN_CENTER) {
                pw.print("CENTER")
            } else {
                pw.print("RIGHT")
            }
        }

        pw.print(" title=")
        pw.print(title)
        pw.println("]")
        pw.close()

        return sw.toString()
    }

    companion object {

        /**
         * Column alignment constants.
         */
        val ALIGN_LEFT = 1
        val ALIGN_CENTER = 2
        val ALIGN_RIGHT = 3
    }
}
