//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2019.01.18 at 11:10:40 AM GMT
//

package net.sf.mpxj.mspdi.schema

import java.util.Date
import javax.xml.bind.annotation.adapters.XmlAdapter

@SuppressWarnings("all")
class Adapter1 : XmlAdapter<String, Date>() {

    fun unmarshal(value: String): Date? {
        return net.sf.mpxj.mspdi.DatatypeConverter.parseDateTime(value)
    }

    fun marshal(value: Date): String? {
        return net.sf.mpxj.mspdi.DatatypeConverter.printDateTime(value)
    }

}
