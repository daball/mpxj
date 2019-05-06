/*
 * This is a modified version of the MapFileGenerator from
 * http://www.frijters.net/MapFileGenerator.java.
 *
 * The original copyright notice appears below.
 */

/*
  Copyright (C) 2005 Valdemar Mejstad
  Copyright (C) 2005 Jeroen Frijters

  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the authors be held liable for any damages
  arising from the use of this software.

  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:

  1. The origin of this software must not be misrepresented; you must not
     claim that you wrote the original software. If you use this software
     in a product, an acknowledgment in the product documentation would be
     appreciated but is not required.
  2. Altered source versions must be plainly marked as such, and must not be
     misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.
*/

package net.sf.mpxj.ikvm

import java.beans.BeanInfo
import java.beans.IntrospectionException
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.net.URL
import java.net.URLClassLoader
import java.util.Enumeration
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList
import java.util.jar.JarEntry
import java.util.jar.JarFile

import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamWriter

/**
 * Generate a map file for conversion of MPXJ using IKVM.
 */
class MapFileGenerator {

    private var m_responseList: List<String>? = null

    /**
     * Generate a map file from a jar file.
     *
     * @param jarFile jar file
     * @param mapFileName map file name
     * @param mapClassMethods true if we want to produce .Net style class method names
     * @throws XMLStreamException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IntrospectionException
     */
    @Throws(XMLStreamException::class, IOException::class, ClassNotFoundException::class, IntrospectionException::class)
    fun generateMapFile(jarFile: File, mapFileName: String, mapClassMethods: Boolean) {
        m_responseList = LinkedList<String>()
        writeMapFile(mapFileName, jarFile, mapClassMethods)
    }

    /**
     * Generate an IKVM map file.
     *
     * @param mapFileName map file name
     * @param jarFile jar file containing code to be mapped
     * @param mapClassMethods true if we want to produce .Net style class method names
     * @throws IOException
     * @throws XMLStreamException
     * @throws ClassNotFoundException
     * @throws IntrospectionException
     */
    @Throws(IOException::class, XMLStreamException::class, ClassNotFoundException::class, IntrospectionException::class)
    private fun writeMapFile(mapFileName: String, jarFile: File, mapClassMethods: Boolean) {
        val fw = FileWriter(mapFileName)
        val xof = XMLOutputFactory.newInstance()
        val writer = xof.createXMLStreamWriter(fw)
        //XMLStreamWriter writer = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(fw));

        writer.writeStartDocument()
        writer.writeStartElement("root")
        writer.writeStartElement("assembly")

        addClasses(writer, jarFile, mapClassMethods)

        writer.writeEndElement()
        writer.writeEndElement()
        writer.writeEndDocument()
        writer.flush()
        writer.close()

        fw.flush()
        fw.close()
    }

    /**
     * Add classes to the map file.
     *
     * @param writer XML stream writer
     * @param jarFile jar file
     * @param mapClassMethods true if we want to produce .Net style class method names
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws XMLStreamException
     * @throws IntrospectionException
     */
    @Throws(IOException::class, ClassNotFoundException::class, XMLStreamException::class, IntrospectionException::class)
    private fun addClasses(writer: XMLStreamWriter, jarFile: File, mapClassMethods: Boolean) {
        val currentThreadClassLoader = Thread.currentThread().getContextClassLoader()

        val loader = URLClassLoader(arrayOf<URL>(jarFile.toURI().toURL()), currentThreadClassLoader)

        val jar = JarFile(jarFile)
        val enumeration = jar.entries()
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            if (!jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")) {
                addClass(loader, jarEntry, writer, mapClassMethods)
            }
        }
        jar.close()
    }

    /**
     * Add an individual class to the map file.
     *
     * @param loader jar file class loader
     * @param jarEntry jar file entry
     * @param writer XML stream writer
     * @param mapClassMethods true if we want to produce .Net style class method names
     * @throws ClassNotFoundException
     * @throws XMLStreamException
     * @throws IntrospectionException
     */
    @Throws(ClassNotFoundException::class, XMLStreamException::class, IntrospectionException::class)
    private fun addClass(loader: URLClassLoader, jarEntry: JarEntry, writer: XMLStreamWriter, mapClassMethods: Boolean) {
        val className = jarEntry.getName().replaceAll("\\.class", "").replaceAll("/", ".")
        writer.writeStartElement("class")
        writer.writeAttribute("name", className)

        val methodSet = HashSet<Method>()
        val aClass = loader.loadClass(className)

        processProperties(writer, methodSet, aClass)

        if (mapClassMethods && !Modifier.isInterface(aClass.getModifiers())) {
            processClassMethods(writer, aClass, methodSet)
        }
        writer.writeEndElement()
    }

    /**
     * Process class properties.
     *
     * @param writer output stream
     * @param methodSet set of methods processed
     * @param aClass class being processed
     * @throws IntrospectionException
     * @throws XMLStreamException
     */
    @Throws(IntrospectionException::class, XMLStreamException::class)
    private fun processProperties(writer: XMLStreamWriter, methodSet: Set<Method>, aClass: Class<*>) {
        val beanInfo = Introspector.getBeanInfo(aClass, aClass.getSuperclass())
        val propertyDescriptors = beanInfo.getPropertyDescriptors()

        for (i in propertyDescriptors.indices) {
            val propertyDescriptor = propertyDescriptors[i]
            if (propertyDescriptor.getPropertyType() != null) {
                val name = propertyDescriptor.getName()
                val readMethod = propertyDescriptor.getReadMethod()
                val writeMethod = propertyDescriptor.getWriteMethod()

                val readMethodName = if (readMethod == null) null else readMethod!!.getName()
                val writeMethodName = if (writeMethod == null) null else writeMethod!!.getName()
                addProperty(writer, name, propertyDescriptor.getPropertyType(), readMethodName, writeMethodName)

                if (readMethod != null) {
                    methodSet.add(readMethod)
                }

                if (writeMethod != null) {
                    methodSet.add(writeMethod)
                }
            } else {
                processAmbiguousProperty(writer, methodSet, aClass, propertyDescriptor)
            }
        }
    }

    /**
     * Add a simple property to the map file.
     *
     * @param writer xml stream writer
     * @param name property name
     * @param propertyType property type
     * @param readMethod read method name
     * @param writeMethod write method name
     * @throws XMLStreamException
     */
    @Throws(XMLStreamException::class)
    private fun addProperty(writer: XMLStreamWriter, name: String, propertyType: Class<*>, readMethod: String?, writeMethod: String?) {
        if (name.length() !== 0) {
            writer.writeStartElement("property")

            // convert property name to .NET style (i.e. first letter uppercase)
            val propertyName = name.substring(0, 1).toUpperCase() + name.substring(1)
            writer.writeAttribute("name", propertyName)

            val type = getTypeString(propertyType)

            writer.writeAttribute("sig", "()$type")
            if (readMethod != null) {
                writer.writeStartElement("getter")
                writer.writeAttribute("name", readMethod)
                writer.writeAttribute("sig", "()$type")
                writer.writeEndElement()
            }
            if (writeMethod != null) {
                writer.writeStartElement("setter")
                writer.writeAttribute("name", writeMethod)
                writer.writeAttribute("sig", "($type)V")
                writer.writeEndElement()
            }

            writer.writeEndElement()
        }
    }

    /**
     * Converts a class into a signature token.
     *
     * @param c class
     * @return signature token text
     */
    private fun getTypeString(c: Class<*>): String {
        var result = TYPE_MAP.get(c)
        if (result == null) {
            result = c.getName()
            if (!result!!.endsWith(";") && !result!!.startsWith("[")) {
                result = "L$result;"
            }
        }
        return result
    }

    /**
     * Where bean introspection is confused by getProperty() and getProperty(int index), this method determines the correct
     * properties to add.
     *
     * @param writer XML stream writer
     * @param methodSet set of methods processed
     * @param aClass Java class
     * @param propertyDescriptor Java property
     * @throws SecurityException
     * @throws XMLStreamException
     */
    @Throws(SecurityException::class, XMLStreamException::class)
    private fun processAmbiguousProperty(writer: XMLStreamWriter, methodSet: Set<Method>, aClass: Class<*>, propertyDescriptor: PropertyDescriptor) {
        var name = propertyDescriptor.getName()
        name = name.toUpperCase().charAt(0) + name.substring(1)

        var readMethod: Method? = null
        try {
            readMethod = aClass.getMethod("get$name", null as Array<Class<*>>?)
        } catch (ex: NoSuchMethodException) {
            // Silently ignore
        }

        if (readMethod != null) {
            var writeMethod: Method? = null
            try {
                writeMethod = aClass.getMethod("set$name", readMethod!!.getReturnType())
            } catch (ex: NoSuchMethodException) {
                // Silently ignore
            }

            val readMethodName = readMethod!!.getName()
            val writeMethodName = if (writeMethod == null) null else writeMethod!!.getName()
            addProperty(writer, name, readMethod!!.getReturnType(), readMethodName, writeMethodName)

            methodSet.add(readMethod)
            if (writeMethod != null) {
                methodSet.add(writeMethod)
            }
        }
    }

    /**
     * Hides the original Java-style method name using an attribute
     * which should be respected by Visual Studio, the creates a new
     * wrapper method using a .Net style method name.
     *
     * Note that this does not work for VB as it is case insensitive. Even
     * though Visual Studio won't show you the Java-style method name,
     * the VB compiler sees both and thinks they are the same... which
     * causes it to fail.
     *
     * @param writer output stream
     * @param aClass class being processed
     * @param methodSet set of methods which have been processed.
     * @throws XMLStreamException
     */
    @Throws(XMLStreamException::class)
    private fun processClassMethods(writer: XMLStreamWriter, aClass: Class<*>, methodSet: Set<Method>) {
        val methods = aClass.getDeclaredMethods()
        for (method in methods) {
            if (!methodSet.contains(method) && Modifier.isPublic(method.getModifiers()) && !Modifier.isInterface(method.getModifiers())) {
                if (Modifier.isStatic(method.getModifiers())) {
                    // TODO Handle static methods here
                } else {
                    var name = method.getName()
                    val methodSignature = createMethodSignature(method)
                    val fullJavaName = aClass.getCanonicalName() + "." + name + methodSignature

                    if (!ignoreMethod(fullJavaName)) {
                        //
                        // Hide the original method
                        //
                        writer.writeStartElement("method")
                        writer.writeAttribute("name", name)
                        writer.writeAttribute("sig", methodSignature)

                        writer.writeStartElement("attribute")
                        writer.writeAttribute("type", "System.ComponentModel.EditorBrowsableAttribute")
                        writer.writeAttribute("sig", "(Lcli.System.ComponentModel.EditorBrowsableState;)V")
                        writer.writeStartElement("parameter")
                        writer.writeCharacters("Never")
                        writer.writeEndElement()
                        writer.writeEndElement()
                        writer.writeEndElement()

                        //
                        // Create a wrapper method
                        //
                        name = name.toUpperCase().charAt(0) + name.substring(1)

                        writer.writeStartElement("method")
                        writer.writeAttribute("name", name)
                        writer.writeAttribute("sig", methodSignature)
                        writer.writeAttribute("modifiers", "public")

                        writer.writeStartElement("body")

                        for (index in 0..method.getParameterTypes().length) {
                            if (index < 4) {
                                writer.writeEmptyElement("ldarg_$index")
                            } else {
                                writer.writeStartElement("ldarg_s")
                                writer.writeAttribute("argNum", Integer.toString(index))
                                writer.writeEndElement()
                            }
                        }

                        writer.writeStartElement("callvirt")
                        writer.writeAttribute("class", aClass.getName())
                        writer.writeAttribute("name", method.getName())
                        writer.writeAttribute("sig", methodSignature)
                        writer.writeEndElement()

                        if (!method.getReturnType().getName().equals("void")) {
                            writer.writeEmptyElement("ldnull")
                            writer.writeEmptyElement("pop")
                        }
                        writer.writeEmptyElement("ret")
                        writer.writeEndElement()
                        writer.writeEndElement()

                        /*
                   * The private method approach doesn't work... so
                   * 3. Add EditorBrowsableAttribute (Never) to original methods
                   * 4. Generate C Sharp and VB variants of the DLL to avid case-sensitivity issues
                   * 5. Implement static method support?
                  <attribute type="System.ComponentModel.EditorBrowsableAttribute" sig="(Lcli.System.ComponentModel.EditorBrowsableState;)V">
                  914                       <parameter>Never</parameter>
                  915                   </attribute>
                  */

                        m_responseList!!.add(fullJavaName)
                    }
                }
            }
        }
    }

    /**
     * Used to determine if the current method should be ignored.
     *
     * @param name method name
     * @return true if the method should be ignored
     */
    private fun ignoreMethod(name: String): Boolean {
        var result = false

        for (ignoredName in IGNORED_METHODS) {
            if (name.matches(ignoredName)) {
                result = true
                break
            }
        }

        return result
    }

    /**
     * Creates a method signature.
     *
     * @param method Method instance
     * @return method signature
     */
    private fun createMethodSignature(method: Method): String {
        val sb = StringBuilder()
        sb.append("(")
        for (type in method.getParameterTypes()) {
            sb.append(getTypeString(type))
        }
        sb.append(")")
        val type = method.getReturnType()
        if (type.getName().equals("void")) {
            sb.append("V")
        } else {
            sb.append(getTypeString(type))
        }
        return sb.toString()
    }

    companion object {
        /**
         * Command line entry point.
         *
         * @param args command line arguments
         * @throws ClassNotFoundException
         * @throws XMLStreamException
         * @throws IOException
         * @throws IntrospectionException
         */
        @Throws(ClassNotFoundException::class, XMLStreamException::class, IOException::class, IntrospectionException::class)
        fun main(args: Array<String>) {
            if (args.size != 3) {
                System.out.println("Usage: MapFileGenerator <file.jar> <remapfile.xml> <map class methods flag>")
            } else {
                val generator = MapFileGenerator()
                generator.generateMapFile(File(args[0]), args[1], Boolean.parseBoolean(args[2]))
            }
        }

        private val TYPE_MAP = HashMap<Class<*>, String>()

        init {
            TYPE_MAP.put(Boolean::class.javaPrimitiveType, "Z")
            TYPE_MAP.put(Byte::class.javaPrimitiveType, "B")
            TYPE_MAP.put(Short::class.javaPrimitiveType, "S")
            TYPE_MAP.put(Char::class.javaPrimitiveType, "C")
            TYPE_MAP.put(Int::class.javaPrimitiveType, "I")
            TYPE_MAP.put(Long::class.javaPrimitiveType, "J")
            TYPE_MAP.put(Float::class.javaPrimitiveType, "F")
            TYPE_MAP.put(Double::class.javaPrimitiveType, "D")
        }

        private val IGNORED_METHODS = arrayOf(".*\\.toString\\(\\)Ljava.lang.String;")
    }
}
