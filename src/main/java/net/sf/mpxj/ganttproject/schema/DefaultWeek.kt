//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2017.12.28 at 05:49:44 PM GMT
//

package net.sf.mpxj.ganttproject.schema

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.XmlValue

/**
 *
 * Java class for default-week complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="default-week">
 * &lt;simpleContent>
 * &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 * &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}int" />
 * &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 * &lt;attribute name="sun" type="{http://www.w3.org/2001/XMLSchema}int" />
 * &lt;attribute name="mon" type="{http://www.w3.org/2001/XMLSchema}int" />
 * &lt;attribute name="tue" type="{http://www.w3.org/2001/XMLSchema}int" />
 * &lt;attribute name="wed" type="{http://www.w3.org/2001/XMLSchema}int" />
 * &lt;attribute name="thu" type="{http://www.w3.org/2001/XMLSchema}int" />
 * &lt;attribute name="fri" type="{http://www.w3.org/2001/XMLSchema}int" />
 * &lt;attribute name="sat" type="{http://www.w3.org/2001/XMLSchema}int" />
 * &lt;/extension>
 * &lt;/simpleContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@SuppressWarnings("all")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "default-week", propOrder = {
    "value"
})
class DefaultWeek {

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
    @XmlValue
    var value: String
    /**
     * Gets the value of the id property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the id property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlAttribute(name = "id")
    var id: Integer
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
    @XmlAttribute(name = "name")
    var name: String
    /**
     * Gets the value of the sun property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the sun property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlAttribute(name = "sun")
    var sun: Integer
    /**
     * Gets the value of the mon property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the mon property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlAttribute(name = "mon")
    var mon: Integer
    /**
     * Gets the value of the tue property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the tue property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlAttribute(name = "tue")
    var tue: Integer
    /**
     * Gets the value of the wed property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the wed property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlAttribute(name = "wed")
    var wed: Integer
    /**
     * Gets the value of the thu property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the thu property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlAttribute(name = "thu")
    var thu: Integer
    /**
     * Gets the value of the fri property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the fri property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlAttribute(name = "fri")
    var fri: Integer
    /**
     * Gets the value of the sat property.
     *
     * @return
     * possible object is
     * [Integer]
     */
    /**
     * Sets the value of the sat property.
     *
     * @param value
     * allowed object is
     * [Integer]
     */
    @XmlAttribute(name = "sat")
    var sat: Integer

}
