/*
 * file:       NamespaceFilter.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2018
 * date:       07/06/2018
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

package net.sf.mpxj.primavera

import java.util.HashMap

import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.XMLFilterImpl

/**
 * This class has two purposes. The first is to ensure that JAXB sees the namespace
 * it is expecting when it reads the file, so we force our hard-coded namespace.
 * The second issue we deal with is that older versions of the PMXML file format
 * appear to be identical to the modern versions, except that the root element
 * is `BusinessObjects` rather than `APIBusinessObjects`. We use this class to
 * rename the element. Note that we try to be efficient about this, so we only
 * check the first start element, and ignore all other elements if it doesn't
 * need to be renamed.
 */
internal class NamespaceFilter : XMLFilterImpl() {

    private var m_firstElement = true
    private var m_replacing: Boolean = false
    @Override
    @Throws(SAXException::class)
    fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {
        var localName = localName
        if (m_firstElement) {
            m_replacing = ELEMENT_MAP.containsKey(localName)
            if (m_replacing) {
                localName = ELEMENT_MAP.get(localName)
            }
            m_firstElement = false
        }
        super.startElement(NAMESPACE, localName, qName, atts)
    }

    @Override
    @Throws(SAXException::class)
    fun endElement(uri: String, localName: String, qName: String) {
        var localName = localName
        if (m_replacing && ELEMENT_MAP.containsKey(localName)) {
            localName = ELEMENT_MAP.get(localName)
        }
        super.endElement(NAMESPACE, localName, qName)
    }

    companion object {

        private val ELEMENT_MAP = HashMap<String, String>()

        init {
            ELEMENT_MAP.put("BusinessObjects", "APIBusinessObjects")
        }

        private val NAMESPACE = "http://xmlns.oracle.com/Primavera/P6/V17.7/API/BusinessObjects"
    }
}