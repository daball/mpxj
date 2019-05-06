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
 * Java class for ResourceAssignmentCreateType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ResourceAssignmentCreateType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="ActivityObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="ActualFinishDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="ActualStartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="ActualUnits" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 * &lt;minInclusive value="0.0"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="AssignmentIsRead" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="10"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ChangeSetObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="Date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="ProjectObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="RemainingDuration" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 * &lt;minInclusive value="0.0"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="RemainingFinishDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="RemainingUnits" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 * &lt;minInclusive value="0.0"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="RequestUserObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="ResourceAssignmentCreateObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="ResourceAssignmentObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="ResourceObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="Status" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;enumeration value="Pending"/>
 * &lt;enumeration value="Held"/>
 * &lt;enumeration value="Approved"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResourceAssignmentCreateType", propOrder = {
    "activityObjectId",
    "actualFinishDate",
    "actualStartDate",
    "actualUnits",
    "assignmentIsRead",
    "changeSetObjectId",
    "date",
    "projectObjectId",
    "remainingDuration",
    "remainingFinishDate",
    "remainingUnits",
    "requestUserObjectId",
    "resourceAssignmentCreateObjectId",
    "resourceAssignmentObjectId",
    "resourceObjectId",
    "status"
})
class ResourceAssignmentCreateType {

    /**
     * Gets the value of the activityObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the activityObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "ActivityObjectId")
    var activityObjectId: Integer
    /**
     * Gets the value of the actualFinishDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the actualFinishDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "ActualFinishDate", type = String::class, nillable = true)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var actualFinishDate: Date
    /**
     * Gets the value of the actualStartDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the actualStartDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "ActualStartDate", type = String::class, nillable = true)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var actualStartDate: Date
    /**
     * Gets the value of the actualUnits property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the actualUnits property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "ActualUnits", nillable = true)
    var actualUnits: Double
    /**
     * Gets the value of the assignmentIsRead property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the assignmentIsRead property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "AssignmentIsRead")
    var assignmentIsRead: String
    /**
     * Gets the value of the changeSetObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the changeSetObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "ChangeSetObjectId")
    var changeSetObjectId: Integer
    /**
     * Gets the value of the date property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the date property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "Date", type = String::class, nillable = true)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var date: Date
    /**
     * Gets the value of the projectObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the projectObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "ProjectObjectId", nillable = true)
    var projectObjectId: Integer
    /**
     * Gets the value of the remainingDuration property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the remainingDuration property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "RemainingDuration", nillable = true)
    var remainingDuration: Double
    /**
     * Gets the value of the remainingFinishDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the remainingFinishDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "RemainingFinishDate", type = String::class, nillable = true)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var remainingFinishDate: Date
    /**
     * Gets the value of the remainingUnits property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the remainingUnits property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "RemainingUnits", nillable = true)
    var remainingUnits: Double
    /**
     * Gets the value of the requestUserObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the requestUserObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "RequestUserObjectId", nillable = true)
    var requestUserObjectId: Integer
    /**
     * Gets the value of the resourceAssignmentCreateObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the resourceAssignmentCreateObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "ResourceAssignmentCreateObjectId")
    var resourceAssignmentCreateObjectId: Integer
    /**
     * Gets the value of the resourceAssignmentObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the resourceAssignmentObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "ResourceAssignmentObjectId", nillable = true)
    var resourceAssignmentObjectId: Integer
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
    /**
     * Gets the value of the status property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the status property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "Status")
    var status: String

}
