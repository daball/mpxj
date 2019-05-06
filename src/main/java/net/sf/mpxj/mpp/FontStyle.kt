/*
 * file:       FontStyle.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2005
 * date:       May 24, 2005
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

/**
 * This class builds on the font described by a FontBase instance
 * and add attributes for color, bold, italic and underline.
 */
open class FontStyle
/**
 * Constructor.
 *
 * @param fontBase font base instance
 * @param italic italic flag
 * @param bold bold flag
 * @param underline underline flag
 * @param strikethrough strikethrough flag
 * @param color color type
 * @param backgroundColor background color
 * @param backgroundPattern background pattern
 */
(private val m_fontBase: FontBase, private val m_italic: Boolean, private val m_bold: Boolean, private val m_underline: Boolean, private val m_strikethrough: Boolean, private val m_color: Color,
 /**
  * Retrieve the background color.
  *
  * @return background color
  */
 val backgroundColor: Color,
 /**
  * Retrieve the background pattern.
  *
  * @return background pattern
  */
 val backgroundPattern: BackgroundPattern) {

    /**
     * Retrieve the font base instance.
     *
     * @return font base instance
     */
    val fontBase: FontBase
        get() = m_fontBase

    /**
     * Retrieve the bold flag.
     *
     * @return bold flag
     */
    val bold: Boolean
        get() = m_bold

    /**
     * Retrieve the font color.
     *
     * @return font color
     */
    val color: Color
        get() = m_color

    /**
     * Retrieve the italic flag.
     *
     * @return italic flag
     */
    val italic: Boolean
        get() = m_italic

    /**
     * Retrieve the underline flag.
     *
     * @return underline flag
     */
    val underline: Boolean
        get() = m_underline

    /**
     * Retrieve the strikethrough flag.
     *
     * @return strikethrough flag
     */
    val strikethrough: Boolean
        get() = m_strikethrough

    /**
     * {@inheritDoc}
     */
    @Override
    open fun toString(): String {
        return "[FontStyle fontBase=$m_fontBase italic=$m_italic bold=$m_bold underline=$m_underline strikethrough=$m_strikethrough color=$m_color backgroundColor=$backgroundColor backgroundPattern=$backgroundPattern]"
    }
}
