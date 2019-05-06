/*
 * file:       TableFontStyle.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       Jun 23, 2005
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

import java.awt.Color

import net.sf.mpxj.FieldType

/**
 * This class builds on the font style described by a FontStyle instance
 * to apply a style to a column, row, or individual cell of a table.
 */
class TableFontStyle
/**
 * Constructor.
 *
 * @param rowUniqueID unique ID of the entity shown on the row
 * @param fieldType field type of the table column
 * @param fontBase font base
 * @param italic italic flag
 * @param bold bold flag
 * @param underline underline flag
 * @param strikethrough strikethrough flag
 * @param color color
 * @param backgroundColor background color
 * @param backgroundPattern background pattern
 * @param italicChanged italic changed flag
 * @param boldChanged bold changed flag
 * @param underlineChanged underline changed flag
 * @param strikethroughChanged strikethrough changed flag
 * @param colorChanged color changed flag
 * @param fontChanged font changed flag
 * @param backgroundColorChanged background color changed
 * @param backgroundPatternChanged background pattern changed
 */
(private val m_rowUniqueID: Int, private val m_fieldType: FieldType, fontBase: FontBase, italic: Boolean, bold: Boolean, underline: Boolean, strikethrough: Boolean, color: Color, backgroundColor: Color, backgroundPattern: BackgroundPattern, private val m_italicChanged: Boolean, private val m_boldChanged: Boolean, private val m_underlineChanged: Boolean, private val m_strikethroughChanged: Boolean, private val m_colorChanged: Boolean, private val m_fontChanged: Boolean, private val m_backgroundColorChanged: Boolean, private val m_backgroundPatternChanged: Boolean) : FontStyle(fontBase, italic, bold, underline, strikethrough, color, backgroundColor, backgroundPattern) {

    /**
     * Retrieves the unique ID of the entity shown on the row
     * affected by this style. This method will return -1 if the
     * style applies to all rows.
     *
     * @return row unique ID
     */
    val rowUniqueID: Int
        get() = m_rowUniqueID

    /**
     * Retrieve the field type of the column to which this style applies.
     *
     * @return field type
     */
    val fieldType: FieldType
        get() = m_fieldType

    /**
     * Retrieve the bold changed flag.
     *
     * @return boolean flag
     */
    val boldChanged: Boolean
        get() = m_boldChanged

    /**
     * Retrieve the color changed flag.
     *
     * @return boolean flag
     */
    val colorChanged: Boolean
        get() = m_colorChanged

    /**
     * Retrieve the italic change flag.
     *
     * @return boolean flag
     */
    val italicChanged: Boolean
        get() = m_italicChanged

    /**
     * Retrieve the underline changed flag.
     *
     * @return boolean flag
     */
    val underlineChanged: Boolean
        get() = m_underlineChanged

    /**
     * Retrieve the strikethrough changed flag.
     *
     * @return boolean flag
     */
    val strikethroughChanged: Boolean
        get() = m_strikethroughChanged

    /**
     * Retrieve the font changed flag.
     *
     * @return boolean flag
     */
    val fontChanged: Boolean
        get() = m_fontChanged

    /**
     * Retrieve the background color changed flag.
     *
     * @return boolean flag
     */
    val backgroundColorChanged: Boolean
        get() = m_backgroundColorChanged

    /**
     * Retrieve the background pattern changed flag.
     *
     * @return boolean flag
     */
    val backgroundPatternChanged: Boolean
        get() = m_backgroundPatternChanged

    /**
     * {@inheritDoc}
     */
    @Override
    override fun toString(): String {
        return "[ColumnFontStyle rowUniqueID=" + m_rowUniqueID + " fieldType=" + m_fieldType + (if (m_italicChanged) " italic=$italic" else "") + (if (m_boldChanged) " bold=$bold" else "") + (if (m_underlineChanged) " underline=$underline" else "") + (if (m_strikethroughChanged) " strikethrough=$strikethrough" else "") + (if (m_fontChanged) " font=$fontBase" else "") + (if (m_colorChanged) " color=$color" else "") + (if (m_backgroundColorChanged) " backgroundColor=$backgroundColor" else "") + (if (m_backgroundPatternChanged) " backgroundPattern=$backgroundPattern" else "") + "]"
    }
}
