//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2017.09.18 at 02:35:45 PM BST
//

package net.sf.mpxj.primavera.schema

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

/**
 *
 * Java class for PortfolioTeamMemberType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PortfolioTeamMemberType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="Id">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="40"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="Name">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="100"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ObjectId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PortfolioTeamMemberType", propOrder = {
    "id",
    "name",
    "objectId"
})
class PortfolioTeamMemberType {

    /**
     * Gets the value of the id property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the id property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "Id", required = true)
    var id: String
    /**
     * Gets the value of the name property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the name property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "Name", required = true)
    var name: String
    /**
     * Gets the value of the objectId property.
     *
     */
    /**
     * Sets the value of the objectId property.
     *
     */
    @XmlElement(name = "ObjectId")
    var objectId: Int = 0

}
