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
 * Java class for ProjectProfileType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ProjectProfileType">
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
 * &lt;element name="IsDefault" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 * &lt;element name="IsSuperUser" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 * &lt;element name="LastUpdateDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="LastUpdateUser" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="255"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="Name" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="100"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="Privileges" type="{http://xmlns.oracle.com/Primavera/P6/V17.7/API/BusinessObjects}ProjectPrivilegesType"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProjectProfileType", propOrder = {
    "createDate",
    "createUser",
    "isDefault",
    "isSuperUser",
    "lastUpdateDate",
    "lastUpdateUser",
    "name",
    "objectId",
    "privileges"
})
class ProjectProfileType {

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
     * Gets the value of the isDefault property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the isDefault property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlElement(name = "IsDefault")
    var isIsDefault: Boolean
    /**
     * Gets the value of the isSuperUser property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the isSuperUser property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlElement(name = "IsSuperUser", nillable = true)
    var isIsSuperUser: Boolean
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
    @XmlElement(name = "Name")
    var name: String
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
    @XmlElement(name = "ObjectId")
    var objectId: Integer
    /**
     * Gets the value of the privileges property.
     *
     * @return
     * possible object is
     * [ProjectPrivilegesType]
     */
    /**
     * Sets the value of the privileges property.
     *
     * @param value
     * allowed object is
     * [ProjectPrivilegesType]
     */
    @XmlElement(name = "Privileges", required = true)
    var privileges: ProjectPrivilegesType

}
