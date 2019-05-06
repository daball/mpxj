//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2017.09.18 at 02:35:45 PM BST
//

package net.sf.mpxj.primavera.schema

import java.util.ArrayList
import java.util.Date
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlSchemaType
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

/**
 *
 * Java class for RoleType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RoleType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="CalculateCostFromUnits" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 * &lt;element name="CreateDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="CreateUser" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="255"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="Id" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="40"/>
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
 * &lt;element name="Name" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="100"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="ParentObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="Responsibilities" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 * &lt;element name="SequenceNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="ProjectRoleSpread" type="{http://xmlns.oracle.com/Primavera/P6/V17.7/API/BusinessObjects}ProjectRoleSpreadType" maxOccurs="unbounded" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoleType", propOrder = {
    "calculateCostFromUnits",
    "createDate",
    "createUser",
    "id",
    "lastUpdateDate",
    "lastUpdateUser",
    "name",
    "objectId",
    "parentObjectId",
    "responsibilities",
    "sequenceNumber",
    "projectRoleSpread"
})
class RoleType {

    /**
     * Gets the value of the calculateCostFromUnits property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the calculateCostFromUnits property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlElement(name = "CalculateCostFromUnits")
    var isCalculateCostFromUnits: Boolean
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
    @XmlElement(name = "Id")
    var id: String
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
     * Gets the value of the parentObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the parentObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "ParentObjectId", nillable = true)
    var parentObjectId: Integer
    /**
     * Gets the value of the responsibilities property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the responsibilities property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "Responsibilities")
    var responsibilities: String
    /**
     * Gets the value of the sequenceNumber property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the sequenceNumber property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "SequenceNumber")
    var sequenceNumber: Integer
    @XmlElement(name = "ProjectRoleSpread")
    protected var projectRoleSpread: List<ProjectRoleSpreadType>? = null

    /**
     * Gets the value of the projectRoleSpread property.
     *
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the projectRoleSpread property.
     *
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     * getProjectRoleSpread().add(newItem);
    </pre> *
     *
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * [ProjectRoleSpreadType]
     *
     *
     */
    fun getProjectRoleSpread(): List<ProjectRoleSpreadType> {
        if (projectRoleSpread == null) {
            projectRoleSpread = ArrayList<ProjectRoleSpreadType>()
        }
        return this.projectRoleSpread
    }

}
