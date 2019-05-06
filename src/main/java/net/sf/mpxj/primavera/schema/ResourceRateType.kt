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
 * Java class for ResourceRateType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ResourceRateType">
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
 * &lt;element name="EffectiveDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="LastUpdateDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 * &lt;element name="LastUpdateUser" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;maxLength value="255"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="MaxUnitsPerTime" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 * &lt;minInclusive value="0.0"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="ObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;element name="PricePerUnit" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 * &lt;minInclusive value="0.0"/>
 * &lt;maxInclusive value="9.99999999999999E12"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="PricePerUnit2" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 * &lt;minInclusive value="0.0"/>
 * &lt;maxInclusive value="9.99999999999999E12"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="PricePerUnit3" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 * &lt;minInclusive value="0.0"/>
 * &lt;maxInclusive value="9.99999999999999E12"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="PricePerUnit4" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 * &lt;minInclusive value="0.0"/>
 * &lt;maxInclusive value="9.99999999999999E12"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
 * &lt;element name="PricePerUnit5" minOccurs="0">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 * &lt;minInclusive value="0.0"/>
 * &lt;maxInclusive value="9.99999999999999E12"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/element>
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
 * &lt;element name="ShiftPeriodObjectId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResourceRateType", propOrder = {
    "createDate",
    "createUser",
    "effectiveDate",
    "lastUpdateDate",
    "lastUpdateUser",
    "maxUnitsPerTime",
    "objectId",
    "pricePerUnit",
    "pricePerUnit2",
    "pricePerUnit3",
    "pricePerUnit4",
    "pricePerUnit5",
    "resourceId",
    "resourceName",
    "resourceObjectId",
    "shiftPeriodObjectId"
})
class ResourceRateType {

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
     * Gets the value of the effectiveDate property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the effectiveDate property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlElement(name = "EffectiveDate", type = String::class)
    @XmlJavaTypeAdapter(Adapter1::class)
    @XmlSchemaType(name = "dateTime")
    var effectiveDate: Date
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
     * Gets the value of the maxUnitsPerTime property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the maxUnitsPerTime property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "MaxUnitsPerTime", nillable = true)
    var maxUnitsPerTime: Double
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
     * Gets the value of the pricePerUnit property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the pricePerUnit property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "PricePerUnit", nillable = true)
    var pricePerUnit: Double
    /**
     * Gets the value of the pricePerUnit2 property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the pricePerUnit2 property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "PricePerUnit2", nillable = true)
    var pricePerUnit2: Double
    /**
     * Gets the value of the pricePerUnit3 property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the pricePerUnit3 property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "PricePerUnit3", nillable = true)
    var pricePerUnit3: Double
    /**
     * Gets the value of the pricePerUnit4 property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the pricePerUnit4 property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "PricePerUnit4", nillable = true)
    var pricePerUnit4: Double
    /**
     * Gets the value of the pricePerUnit5 property.
     *
     * @return
     * possible object is
     * [Double]
     */
    /**
     * Sets the value of the pricePerUnit5 property.
     *
     * @param value
     * allowed object is
     * [Double]
     */
    @XmlElement(name = "PricePerUnit5", nillable = true)
    var pricePerUnit5: Double
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
    @XmlElement(name = "ResourceObjectId")
    var resourceObjectId: Integer
    /**
     * Gets the value of the shiftPeriodObjectId property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the shiftPeriodObjectId property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlElement(name = "ShiftPeriodObjectId", nillable = true)
    var shiftPeriodObjectId: Integer

}
