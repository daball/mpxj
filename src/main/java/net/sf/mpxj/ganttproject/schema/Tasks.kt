//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2017.12.28 at 05:49:44 PM GMT
//

package net.sf.mpxj.ganttproject.schema

import java.util.ArrayList
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

/**
 *
 * Java class for tasks complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="tasks">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="taskproperties" type="{}taskproperties"/>
 * &lt;element name="task" type="{}task" maxOccurs="unbounded" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;attribute name="empty-milestones" type="{http://www.w3.org/2001/XMLSchema}string" />
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tasks", propOrder = {
    "taskproperties",
    "task"
})
class Tasks {

    /**
     * Gets the value of the taskproperties property.
     *
     * @return
     * possible object is
     * [Taskproperties]
     */
    /**
     * Sets the value of the taskproperties property.
     *
     * @param value
     * allowed object is
     * [Taskproperties]
     */
    @XmlElement(required = true)
    var taskproperties: Taskproperties
    protected var task: List<Task>? = null
    /**
     * Gets the value of the emptyMilestones property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the emptyMilestones property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlAttribute(name = "empty-milestones")
    var emptyMilestones: String

    /**
     * Gets the value of the task property.
     *
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the task property.
     *
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     * getTask().add(newItem);
    </pre> *
     *
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * [Task]
     *
     *
     */
    fun getTask(): List<Task> {
        if (task == null) {
            task = ArrayList<Task>()
        }
        return this.task
    }

}
