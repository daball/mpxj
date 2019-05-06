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
 * Java class for ActivityStepType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ActivityStepType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="ActivityId" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="40"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ActivityName" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="120"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ActivityObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="CreateDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="CreateUser" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="255"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 * &lt;element name="IsBaseline" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 * &lt;element name="IsCompleted" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
 * &lt;maxLength value="120"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="PercentComplete" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 * &lt;minInclusive value="0.0"/>
 * &lt;maxInclusive value="1.0"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ProjectId" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="40"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ProjectObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="SequenceNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="WBSObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="Weight" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 * &lt;minInclusive value="0.0"/>
 * &lt;maxInclusive value="999999.0"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="WeightPercent" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 * &lt;element name="UDF" type="{http://xmlns.oracle.com/Primavera/P6/V17.7/API/BusinessObjects}UDFAssignmentType" maxOccurs="unbounded" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityStepType", propOrder = {
    "activityId",
    "activityName",
    "activityObjectId",
    "createDate",
    "createUser",
    "description",
    "isBaseline",
    "isCompleted",
    "isTemplate",
    "lastUpdateDate",
    "lastUpdateUser",
    "name",
    "objectId",
    "percentComplete",
    "projectId",
    "projectObjectId",
    "sequenceNumber",
    "wbsObjectId",
    "weight",
    "weightPercent",
    "udf"
})
class ActivityStepType {

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
    @XmlElement(name = "ActivityObjectId")
    var activityObjectId: Integer
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
     * Gets the value of the description property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the description property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "Description")
    var description: String
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
     * Gets the value of the isCompleted property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the isCompleted property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlElement(name = "IsCompleted")
    var isIsCompleted: Boolean
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
     * Gets the value of the percentComplete property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the percentComplete property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "PercentComplete", nillable = true)
    var percentComplete: Double
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
    /**
     * Gets the value of the wbsObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the wbsObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "WBSObjectId", nillable = true)
    var wbsObjectId: Integer
    /**
     * Gets the value of the weight property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the weight property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "Weight", nillable = true)
    var weight: Double
    /**
     * Gets the value of the weightPercent property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the weightPercent property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "WeightPercent", nillable = true)
    var weightPercent: Double
    @XmlElement(name = "UDF")
    protected var udf: List<UDFAssignmentType>? = null

    /**
     * Gets the value of the udf property.
     *
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the udf property.
     *
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     * getUDF().add(newItem);
    </pre> *
     *
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * [UDFAssignmentType]
     *
     *
     */
    fun getUDF(): List<UDFAssignmentType> {
        if (udf == null) {
            udf = ArrayList<UDFAssignmentType>()
        }
        return this.udf
    }

}
