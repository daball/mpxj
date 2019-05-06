//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2017.09.18 at 02:35:45 PM BST
//

package net.sf.mpxj.primavera.schema

import java.util.Date
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlSchemaType
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

/**
 *
 * Java class for ResourceLocationType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ResourceLocationType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="CreateDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="CreateUser" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="255"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="LastUpdateDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="LastUpdateUser" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="255"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="Latitude" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 * &lt;element name="Longitude" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 * &lt;element name="ObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="ResourceObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResourceLocationType", propOrder = {
    "createDate",
    "createUser",
    "lastUpdateDate",
    "lastUpdateUser",
    "latitude",
    "longitude",
    "objectId",
    "resourceObjectId"
})
class ResourceLocationType {

    /**
     * Gets the value of the createDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the createDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "CreateDate", type = String::class, nillable = true)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var createDate: Date
    /**
     * Gets the value of the createUser property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the createUser property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "CreateUser")
    var createUser: String
    /**
     * Gets the value of the lastUpdateDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the lastUpdateDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "LastUpdateDate", type = String::class, nillable = true)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var lastUpdateDate: Date
    /**
     * Gets the value of the lastUpdateUser property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the lastUpdateUser property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "LastUpdateUser")
    var lastUpdateUser: String
    /**
     * Gets the value of the latitude property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the latitude property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "Latitude", nillable = true)
    var latitude: Double
    /**
     * Gets the value of the longitude property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the longitude property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "Longitude", nillable = true)
    var longitude: Double
    /**
     * Gets the value of the objectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the objectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "ObjectId", nillable = true)
    var objectId: Integer
    /**
     * Gets the value of the resourceObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the resourceObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "ResourceObjectId", nillable = true)
    var resourceObjectId: Integer

}
