//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2010.08.02 at 09:18:52 PM BST
//

package net.sf.mpxj.planner.schema

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

/**
 *
 */
@SuppressWarnings("all")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "list-item")
class ListItem {

    /**
     * Gets the value of the value property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the value property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter::class)
    var value: String

}
