//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.07.12 at 04:42:45 PM BST 
//

package net.sf.mpxj.conceptdraw.schema

import java.util.ArrayList
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlSchemaType
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

/**
 *
 * Java class for anonymous complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;all>
 * &lt;element name="GridColumns">
 * &lt;complexType>
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="Column" maxOccurs="unbounded">
 * &lt;complexType>
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 * &lt;element name="Index" type="{http://www.w3.org/2001/XMLSchema}int"/>
 * &lt;element name="Width" type="{http://www.w3.org/2001/XMLSchema}int"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
 * &lt;/element>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
 * &lt;/element>
 * &lt;element name="TextBoxVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 * &lt;element name="ShowCriticalPath" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 * &lt;element name="ShowPlannedComplete" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 * &lt;element name="ShowCompleteChangeValue" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 * &lt;element name="ShowCompleteChangeRate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 * &lt;element name="GridTextWrap" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 * &lt;element ref="{http://www.schemas.conceptdraw.com/cdprj/document.xsd}ActiveFilter" minOccurs="0"/>
 * &lt;/all>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "ViewProperties")
class ViewProperties {

    /**
     * Gets the value of the gridColumns property.
     *
     * @return
     * possible object is
     * [ViewProperties.GridColumns]
     */
    /**
     * Sets the value of the gridColumns property.
     *
     * @param value
     * allowed object is
     * [ViewProperties.GridColumns]
     */
    @XmlElement(name = "GridColumns", required = true)
    var gridColumns: ViewProperties.GridColumns
    /**
     * Gets the value of the textBoxVisible property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the textBoxVisible property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlElement(name = "TextBoxVisible")
    var isTextBoxVisible: Boolean
    /**
     * Gets the value of the showCriticalPath property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the showCriticalPath property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlElement(name = "ShowCriticalPath")
    var isShowCriticalPath: Boolean
    /**
     * Gets the value of the showPlannedComplete property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the showPlannedComplete property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlElement(name = "ShowPlannedComplete")
    var isShowPlannedComplete: Boolean
    /**
     * Gets the value of the showCompleteChangeValue property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the showCompleteChangeValue property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlElement(name = "ShowCompleteChangeValue")
    var isShowCompleteChangeValue: Boolean
    /**
     * Gets the value of the showCompleteChangeRate property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the showCompleteChangeRate property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlElement(name = "ShowCompleteChangeRate")
    var isShowCompleteChangeRate: Boolean
    /**
     * Gets the value of the gridTextWrap property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the gridTextWrap property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlElement(name = "GridTextWrap")
    var isGridTextWrap: Boolean
    /**
     * Gets the value of the activeFilter property.
     *
     * @return
     * possible object is
     * [ActiveFilter]
     */
    /**
     * Sets the value of the activeFilter property.
     *
     * @param value
     * allowed object is
     * [ActiveFilter]
     */
    @XmlElement(name = "ActiveFilter")
    var activeFilter: ActiveFilter

    /**
     *
     * Java class for anonymous complex type.
     *
     *
     * The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     * &lt;complexContent>
     * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     * &lt;sequence>
     * &lt;element name="Column" maxOccurs="unbounded">
     * &lt;complexType>
     * &lt;complexContent>
     * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     * &lt;sequence>
     * &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}int"/>
     * &lt;element name="Index" type="{http://www.w3.org/2001/XMLSchema}int"/>
     * &lt;element name="Width" type="{http://www.w3.org/2001/XMLSchema}int"/>
     * &lt;/sequence>
     * &lt;/restriction>
     * &lt;/complexContent>
     * &lt;/complexType>
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
    @XmlType(name = "", propOrder = {
        "column"
    })
    class GridColumns {

        @XmlElement(name = "Column", required = true)
        protected var column: List<ViewProperties.GridColumns.Column>? = null

        /**
         * Gets the value of the column property.
         *
         *
         *
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the column property.
         *
         *
         *
         * For example, to add a new item, do as follows:
         * <pre>
         * getColumn().add(newItem);
        </pre> *
         *
         *
         *
         *
         * Objects of the following type(s) are allowed in the list
         * [ViewProperties.GridColumns.Column]
         *
         *
         */
        fun getColumn(): List<ViewProperties.GridColumns.Column> {
            if (column == null) {
                column = ArrayList<ViewProperties.GridColumns.Column>()
            }
            return this.column
        }

        /**
         *
         * Java class for anonymous complex type.
         *
         *
         * The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType>
         * &lt;complexContent>
         * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         * &lt;sequence>
         * &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}int"/>
         * &lt;element name="Index" type="{http://www.w3.org/2001/XMLSchema}int"/>
         * &lt;element name="Width" type="{http://www.w3.org/2001/XMLSchema}int"/>
         * &lt;/sequence>
         * &lt;/restriction>
         * &lt;/complexContent>
         * &lt;/complexType>
        </pre> *
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "id",
            "index",
            "width"
        })
        class Column {

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
            @XmlElement(name = "ID", required = true, type = String::class)
            @XmlJavaTypeAdapter(Adapter5::class)
            @XmlSchemaType(name = "int")
            var id: Integer
            /**
             * Gets the value of the index property.
             *
             * @return
             * possible object is
             * [String]
             */
            /**
             * Sets the value of the index property.
             *
             * @param value
             * allowed object is
             * [String]
             */
            @XmlElement(name = "Index", required = true, type = String::class)
            @XmlJavaTypeAdapter(Adapter5::class)
            @XmlSchemaType(name = "int")
            var index: Integer
            /**
             * Gets the value of the width property.
             *
             * @return
             * possible object is
             * [String]
             */
            /**
             * Sets the value of the width property.
             *
             * @param value
             * allowed object is
             * [String]
             */
            @XmlElement(name = "Width", required = true, type = String::class)
            @XmlJavaTypeAdapter(Adapter5::class)
            @XmlSchemaType(name = "int")
            var width: Integer

        }

    }

}
