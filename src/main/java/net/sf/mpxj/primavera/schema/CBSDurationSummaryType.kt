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
 * Java class for CBSDurationSummaryType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CBSDurationSummaryType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="CBSObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="OriginalProjectObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="ProjectId" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="40"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ProjectName" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="100"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ProjectObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="SummaryActualDuration" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 * &lt;element name="SummaryActualFinishDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="SummaryActualStartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="SummaryPercentComplete" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 * &lt;element name="SummaryPlannedDuration" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 * &lt;element name="SummaryPlannedFinishDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="SummaryPlannedStartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="SummaryRemainingDuration" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 * &lt;element name="SummaryRemainingFinishDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="SummaryRemainingStartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CBSDurationSummaryType", propOrder = {
    "cbsObjectId",
    "originalProjectObjectId",
    "projectId",
    "projectName",
    "projectObjectId",
    "summaryActualDuration",
    "summaryActualFinishDate",
    "summaryActualStartDate",
    "summaryPercentComplete",
    "summaryPlannedDuration",
    "summaryPlannedFinishDate",
    "summaryPlannedStartDate",
    "summaryRemainingDuration",
    "summaryRemainingFinishDate",
    "summaryRemainingStartDate"
})
class CBSDurationSummaryType {

    /**
     * Gets the value of the cbsObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the cbsObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "CBSObjectId", nillable = true)
    var cbsObjectId: Integer
    /**
     * Gets the value of the originalProjectObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the originalProjectObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "OriginalProjectObjectId", nillable = true)
    var originalProjectObjectId: Integer
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
    @XmlElement(name = "ProjectObjectId", nillable = true)
    var projectObjectId: Integer
    /**
     * Gets the value of the summaryActualDuration property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the summaryActualDuration property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "SummaryActualDuration", nillable = true)
    var summaryActualDuration: Double
    /**
     * Gets the value of the summaryActualFinishDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the summaryActualFinishDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "SummaryActualFinishDate", type = String::class, nillable = true)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var summaryActualFinishDate: Date
    /**
     * Gets the value of the summaryActualStartDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the summaryActualStartDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "SummaryActualStartDate", type = String::class, nillable = true)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var summaryActualStartDate: Date
    /**
     * Gets the value of the summaryPercentComplete property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the summaryPercentComplete property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "SummaryPercentComplete", nillable = true)
    var summaryPercentComplete: Double
    /**
     * Gets the value of the summaryPlannedDuration property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the summaryPlannedDuration property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "SummaryPlannedDuration", nillable = true)
    var summaryPlannedDuration: Double
    /**
     * Gets the value of the summaryPlannedFinishDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the summaryPlannedFinishDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "SummaryPlannedFinishDate", type = String::class, nillable = true)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var summaryPlannedFinishDate: Date
    /**
     * Gets the value of the summaryPlannedStartDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the summaryPlannedStartDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "SummaryPlannedStartDate", type = String::class, nillable = true)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var summaryPlannedStartDate: Date
    /**
     * Gets the value of the summaryRemainingDuration property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the summaryRemainingDuration property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "SummaryRemainingDuration", nillable = true)
    var summaryRemainingDuration: Double
    /**
     * Gets the value of the summaryRemainingFinishDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the summaryRemainingFinishDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "SummaryRemainingFinishDate", type = String::class, nillable = true)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var summaryRemainingFinishDate: Date
    /**
     * Gets the value of the summaryRemainingStartDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the summaryRemainingStartDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "SummaryRemainingStartDate", type = String::class, nillable = true)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var summaryRemainingStartDate: Date

}
