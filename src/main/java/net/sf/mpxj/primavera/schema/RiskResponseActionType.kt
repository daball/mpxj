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
 * Java class for RiskResponseActionType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RiskResponseActionType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="ActivityId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 * &lt;element name="ActivityName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 * &lt;element name="ActivityObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="ActualCost" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 * &lt;element name="CreateDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="CreateUser" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="255"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="FinishDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="Id" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="40"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="IsBaseline" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 * &lt;element name="IsTemplate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
 * &lt;maxLength value="200"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="PlannedCost" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 * &lt;element name="PlannedFinishDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="PlannedStartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="ProjectId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 * &lt;element name="ProjectName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 * &lt;element name="ProjectObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="RemainingCost" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 * &lt;element name="ResourceId" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="20"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ResourceName" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="100"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ResourceObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="RiskId" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="40"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="RiskObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="RiskResponsePlanId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 * &lt;element name="RiskResponsePlanName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 * &lt;element name="RiskResponsePlanObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="Score" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="ScoreColor" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;pattern value="#[A-Fa-f0-9]{6}"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ScoreText" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="40"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="StartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="Status" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;enumeration value="Proposed"/>
 * &lt;enumeration value="Sanctioned"/>
 * &lt;enumeration value="Rejected"/>
 * &lt;enumeration value="In Progress"/>
 * &lt;enumeration value="Complete"/>
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
@XmlType(name = "RiskResponseActionType", propOrder = {
    "activityId",
    "activityName",
    "activityObjectId",
    "actualCost",
    "createDate",
    "createUser",
    "finishDate",
    "id",
    "isBaseline",
    "isTemplate",
    "lastUpdateDate",
    "lastUpdateUser",
    "name",
    "objectId",
    "plannedCost",
    "plannedFinishDate",
    "plannedStartDate",
    "projectId",
    "projectName",
    "projectObjectId",
    "remainingCost",
    "resourceId",
    "resourceName",
    "resourceObjectId",
    "riskId",
    "riskObjectId",
    "riskResponsePlanId",
    "riskResponsePlanName",
    "riskResponsePlanObjectId",
    "score",
    "scoreColor",
    "scoreText",
    "startDate",
    "status"
})
class RiskResponseActionType {

    /**
     * Gets the value of the activityId property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the activityId property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "ActivityId")
    var activityId: String
    /**
     * Gets the value of the activityName property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the activityName property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "ActivityName")
    var activityName: String
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
    @XmlElement(name = "ActivityObjectId", nillable = true)
    var activityObjectId: Integer
    /**
     * Gets the value of the actualCost property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the actualCost property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "ActualCost", nillable = true)
    var actualCost: Double
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
     * Gets the value of the finishDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the finishDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "FinishDate", type = String::class)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var finishDate: Date
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
     * Gets the value of the isBaseline property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the isBaseline property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlElement(name = "IsBaseline")
    var isIsBaseline: Boolean
    /**
     * Gets the value of the isTemplate property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the isTemplate property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlElement(name = "IsTemplate")
    var isIsTemplate: Boolean
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
     * Gets the value of the plannedCost property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the plannedCost property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "PlannedCost", nillable = true)
    var plannedCost: Double
    /**
     * Gets the value of the plannedFinishDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the plannedFinishDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "PlannedFinishDate", type = String::class)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var plannedFinishDate: Date
    /**
     * Gets the value of the plannedStartDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the plannedStartDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "PlannedStartDate", type = String::class)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var plannedStartDate: Date
    /**
     * Gets the value of the projectId property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the projectId property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "ProjectId")
    var projectId: String
    /**
     * Gets the value of the projectName property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the projectName property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "ProjectName")
    var projectName: String
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
    @XmlElement(name = "ProjectObjectId")
    var projectObjectId: Integer
    /**
     * Gets the value of the remainingCost property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the remainingCost property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "RemainingCost", nillable = true)
    var remainingCost: Double
    /**
     * Gets the value of the resourceId property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the resourceId property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "ResourceId")
    var resourceId: String
    /**
     * Gets the value of the resourceName property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the resourceName property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "ResourceName")
    var resourceName: String
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
     * Gets the value of the riskId property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the riskId property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "RiskId")
    var riskId: String
    /**
     * Gets the value of the riskObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the riskObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "RiskObjectId")
    var riskObjectId: Integer
    /**
     * Gets the value of the riskResponsePlanId property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the riskResponsePlanId property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "RiskResponsePlanId")
    var riskResponsePlanId: String
    /**
     * Gets the value of the riskResponsePlanName property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the riskResponsePlanName property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "RiskResponsePlanName")
    var riskResponsePlanName: String
    /**
     * Gets the value of the riskResponsePlanObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the riskResponsePlanObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "RiskResponsePlanObjectId")
    var riskResponsePlanObjectId: Integer
    /**
     * Gets the value of the score property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the score property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "Score", nillable = true)
    var score: Integer
    /**
     * Gets the value of the scoreColor property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the scoreColor property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "ScoreColor", nillable = true)
    var scoreColor: String
    /**
     * Gets the value of the scoreText property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the scoreText property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "ScoreText")
    var scoreText: String
    /**
     * Gets the value of the startDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the startDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "StartDate", type = String::class)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var startDate: Date
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
