//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.3-b18-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2004.09.16 at 08:42:56 BST 
//


package com.tapsterrock.mspdi.schema.impl;

public class TimephasedDataTypeImpl implements com.tapsterrock.mspdi.schema.TimephasedDataType, com.sun.xml.bind.JAXBObject, com.tapsterrock.mspdi.schema.impl.runtime.UnmarshallableObject, com.tapsterrock.mspdi.schema.impl.runtime.XMLSerializable, com.tapsterrock.mspdi.schema.impl.runtime.ValidatableObject
{

    protected java.math.BigInteger _Type;
    protected java.lang.String _Value;
    protected java.util.Calendar _Start;
    protected java.math.BigInteger _Unit;
    protected java.util.Calendar _Finish;
    protected java.math.BigInteger _UID;
    public final static java.lang.Class version = (com.tapsterrock.mspdi.schema.impl.JAXBVersion.class);
    private static com.sun.msv.grammar.Grammar schemaFragment;

    private final static java.lang.Class PRIMARY_INTERFACE_CLASS() {
        return (com.tapsterrock.mspdi.schema.TimephasedDataType.class);
    }

    public java.math.BigInteger getType() {
        return _Type;
    }

    public void setType(java.math.BigInteger value) {
        _Type = value;
    }

    public java.lang.String getValue() {
        return _Value;
    }

    public void setValue(java.lang.String value) {
        _Value = value;
    }

    public java.util.Calendar getStart() {
        return _Start;
    }

    public void setStart(java.util.Calendar value) {
        _Start = value;
    }

    public java.math.BigInteger getUnit() {
        return _Unit;
    }

    public void setUnit(java.math.BigInteger value) {
        _Unit = value;
    }

    public java.util.Calendar getFinish() {
        return _Finish;
    }

    public void setFinish(java.util.Calendar value) {
        _Finish = value;
    }

    public java.math.BigInteger getUID() {
        return _UID;
    }

    public void setUID(java.math.BigInteger value) {
        _UID = value;
    }

    public com.tapsterrock.mspdi.schema.impl.runtime.UnmarshallingEventHandler createUnmarshaller(com.tapsterrock.mspdi.schema.impl.runtime.UnmarshallingContext context) {
        return new com.tapsterrock.mspdi.schema.impl.TimephasedDataTypeImpl.Unmarshaller(context);
    }

    public void serializeBody(com.tapsterrock.mspdi.schema.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        if (_Type!= null) {
            context.startElement("http://schemas.microsoft.com/project", "Type");
            context.endNamespaceDecls();
            context.endAttributes();
            try {
                context.text(javax.xml.bind.DatatypeConverter.printInteger(((java.math.BigInteger) _Type)), "Type");
            } catch (java.lang.Exception e) {
                com.tapsterrock.mspdi.schema.impl.runtime.Util.handlePrintConversionException(this, e, context);
            }
            context.endElement();
        }
        context.startElement("http://schemas.microsoft.com/project", "UID");
        context.endNamespaceDecls();
        context.endAttributes();
        try {
            context.text(javax.xml.bind.DatatypeConverter.printInteger(((java.math.BigInteger) _UID)), "UID");
        } catch (java.lang.Exception e) {
            com.tapsterrock.mspdi.schema.impl.runtime.Util.handlePrintConversionException(this, e, context);
        }
        context.endElement();
        if (_Start!= null) {
            context.startElement("http://schemas.microsoft.com/project", "Start");
            context.endNamespaceDecls();
            context.endAttributes();
            try {
                context.text(com.sun.msv.datatype.xsd.DateTimeType.theInstance.serializeJavaObject(((java.util.Calendar) _Start), null), "Start");
            } catch (java.lang.Exception e) {
                com.tapsterrock.mspdi.schema.impl.runtime.Util.handlePrintConversionException(this, e, context);
            }
            context.endElement();
        }
        if (_Finish!= null) {
            context.startElement("http://schemas.microsoft.com/project", "Finish");
            context.endNamespaceDecls();
            context.endAttributes();
            try {
                context.text(com.sun.msv.datatype.xsd.DateTimeType.theInstance.serializeJavaObject(((java.util.Calendar) _Finish), null), "Finish");
            } catch (java.lang.Exception e) {
                com.tapsterrock.mspdi.schema.impl.runtime.Util.handlePrintConversionException(this, e, context);
            }
            context.endElement();
        }
        if (_Unit!= null) {
            context.startElement("http://schemas.microsoft.com/project", "Unit");
            context.endNamespaceDecls();
            context.endAttributes();
            try {
                context.text(javax.xml.bind.DatatypeConverter.printInteger(((java.math.BigInteger) _Unit)), "Unit");
            } catch (java.lang.Exception e) {
                com.tapsterrock.mspdi.schema.impl.runtime.Util.handlePrintConversionException(this, e, context);
            }
            context.endElement();
        }
        if (_Value!= null) {
            context.startElement("http://schemas.microsoft.com/project", "Value");
            context.endNamespaceDecls();
            context.endAttributes();
            try {
                context.text(((java.lang.String) _Value), "Value");
            } catch (java.lang.Exception e) {
                com.tapsterrock.mspdi.schema.impl.runtime.Util.handlePrintConversionException(this, e, context);
            }
            context.endElement();
        }
    }

    public void serializeAttributes(com.tapsterrock.mspdi.schema.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
    }

    public void serializeURIs(com.tapsterrock.mspdi.schema.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
    }

    public java.lang.Class getPrimaryInterface() {
        return (com.tapsterrock.mspdi.schema.TimephasedDataType.class);
    }

    public com.sun.msv.verifier.DocumentDeclaration createRawValidator() {
        if (schemaFragment == null) {
            schemaFragment = com.sun.xml.bind.validator.SchemaDeserializer.deserialize((
 "\u00ac\u00ed\u0000\u0005sr\u0000\u001fcom.sun.msv.grammar.SequenceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom.su"
+"n.msv.grammar.BinaryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0004exp1t\u0000 Lcom/sun/msv/gra"
+"mmar/Expression;L\u0000\u0004exp2q\u0000~\u0000\u0002xr\u0000\u001ecom.sun.msv.grammar.Expressi"
+"on\u00f8\u0018\u0082\u00e8N5~O\u0002\u0000\u0002L\u0000\u0013epsilonReducibilityt\u0000\u0013Ljava/lang/Boolean;L\u0000\u000b"
+"expandedExpq\u0000~\u0000\u0002xpppsq\u0000~\u0000\u0000ppsq\u0000~\u0000\u0000ppsq\u0000~\u0000\u0000ppsq\u0000~\u0000\u0000ppsr\u0000\u001dcom."
+"sun.msv.grammar.ChoiceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0001ppsr\u0000\'com.sun.msv."
+"grammar.trex.ElementPattern\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\tnameClasst\u0000\u001fLcom/su"
+"n/msv/grammar/NameClass;xr\u0000\u001ecom.sun.msv.grammar.ElementExp\u0000\u0000"
+"\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002Z\u0000\u001aignoreUndeclaredAttributesL\u0000\fcontentModelq\u0000~\u0000\u0002xq"
+"\u0000~\u0000\u0003sr\u0000\u0011java.lang.Boolean\u00cd r\u0080\u00d5\u009c\u00fa\u00ee\u0002\u0000\u0001Z\u0000\u0005valuexp\u0000p\u0000sq\u0000~\u0000\u0000ppsr\u0000"
+"\u001bcom.sun.msv.grammar.DataExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\u0002dtt\u0000\u001fLorg/relaxng/"
+"datatype/Datatype;L\u0000\u0006exceptq\u0000~\u0000\u0002L\u0000\u0004namet\u0000\u001dLcom/sun/msv/util/"
+"StringPair;xq\u0000~\u0000\u0003ppsr\u0000)com.sun.msv.datatype.xsd.EnumerationF"
+"acet\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\u0006valuest\u0000\u000fLjava/util/Set;xr\u00009com.sun.msv.da"
+"tatype.xsd.DataTypeWithValueConstraintFacet\"\u00a7Ro\u00ca\u00c7\u008aT\u0002\u0000\u0000xr\u0000*co"
+"m.sun.msv.datatype.xsd.DataTypeWithFacet\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0005Z\u0000\fisFace"
+"tFixedZ\u0000\u0012needValueCheckFlagL\u0000\bbaseTypet\u0000)Lcom/sun/msv/dataty"
+"pe/xsd/XSDatatypeImpl;L\u0000\fconcreteTypet\u0000\'Lcom/sun/msv/datatyp"
+"e/xsd/ConcreteType;L\u0000\tfacetNamet\u0000\u0012Ljava/lang/String;xr\u0000\'com."
+"sun.msv.datatype.xsd.XSDatatypeImpl\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\fnamespaceUr"
+"iq\u0000~\u0000\u001dL\u0000\btypeNameq\u0000~\u0000\u001dL\u0000\nwhiteSpacet\u0000.Lcom/sun/msv/datatype/"
+"xsd/WhiteSpaceProcessor;xpt\u0000$http://schemas.microsoft.com/pr"
+"ojectpsr\u00005com.sun.msv.datatype.xsd.WhiteSpaceProcessor$Colla"
+"pse\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000,com.sun.msv.datatype.xsd.WhiteSpaceProcess"
+"or\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xp\u0000\u0000sr\u0000$com.sun.msv.datatype.xsd.IntegerType\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000+com.sun.msv.datatype.xsd.IntegerDerivedType\u0099\u00f1]\u0090&"
+"6k\u00be\u0002\u0000\u0001L\u0000\nbaseFacetsq\u0000~\u0000\u001bxr\u0000*com.sun.msv.datatype.xsd.Builtin"
+"AtomicType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000%com.sun.msv.datatype.xsd.ConcreteTy"
+"pe\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u001et\u0000 http://www.w3.org/2001/XMLSchemat\u0000\u0007int"
+"egerq\u0000~\u0000$sr\u0000,com.sun.msv.datatype.xsd.FractionDigitsFacet\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001I\u0000\u0005scalexr\u0000;com.sun.msv.datatype.xsd.DataTypeWithLex"
+"icalConstraintFacetT\u0090\u001c>\u001azb\u00ea\u0002\u0000\u0000xq\u0000~\u0000\u001appq\u0000~\u0000$\u0001\u0000sr\u0000#com.sun.msv"
+".datatype.xsd.NumberType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\'q\u0000~\u0000*t\u0000\u0007decimalq\u0000~\u0000"
+"$q\u0000~\u00000t\u0000\u000efractionDigits\u0000\u0000\u0000\u0000q\u0000~\u0000)t\u0000\u000benumerationsr\u0000\u0011java.util."
+"HashSet\u00baD\u0085\u0095\u0096\u00b8\u00b74\u0003\u0000\u0000xpw\f\u0000\u0000\u0000\u0080?@\u0000\u0000\u0000\u0000\u0000Hsr\u0000)com.sun.msv.datatype.x"
+"sd.IntegerValueType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\u0005valueq\u0000~\u0000\u001dxr\u0000\u0010java.lang.Num"
+"ber\u0086\u00ac\u0095\u001d\u000b\u0094\u00e0\u008b\u0002\u0000\u0000xpt\u0000\u000263sq\u0000~\u00006t\u0000\u000229sq\u0000~\u00006t\u0000\u000249sq\u0000~\u00006t\u0000\u000239sq\u0000~\u00006"
+"t\u0000\u000272sq\u0000~\u00006t\u0000\u000224sq\u0000~\u00006t\u0000\u000261sq\u0000~\u00006t\u0000\u000268sq\u0000~\u00006t\u0000\u000250sq\u0000~\u00006t\u0000\u000223"
+"sq\u0000~\u00006t\u0000\u000267sq\u0000~\u00006t\u0000\u000234sq\u0000~\u00006t\u0000\u000252sq\u0000~\u00006t\u0000\u000274sq\u0000~\u00006t\u0000\u000230sq\u0000~\u0000"
+"6t\u0000\u000218sq\u0000~\u00006t\u0000\u000220sq\u0000~\u00006t\u0000\u000242sq\u0000~\u00006t\u0000\u000264sq\u0000~\u00006t\u0000\u000269sq\u0000~\u00006t\u0000\u00022"
+"5sq\u0000~\u00006t\u0000\u000257sq\u0000~\u00006t\u0000\u000232sq\u0000~\u00006t\u0000\u000243sq\u0000~\u00006t\u0000\u000276sq\u0000~\u00006t\u0000\u000235sq\u0000~"
+"\u00006t\u0000\u000226sq\u0000~\u00006t\u0000\u000244sq\u0000~\u00006t\u0000\u00019sq\u0000~\u00006t\u0000\u000219sq\u0000~\u00006t\u0000\u000251sq\u0000~\u00006t\u0000\u00022"
+"8sq\u0000~\u00006t\u0000\u00013sq\u0000~\u00006t\u0000\u000241sq\u0000~\u00006t\u0000\u000270sq\u0000~\u00006t\u0000\u000227sq\u0000~\u00006t\u0000\u000258sq\u0000~\u0000"
+"6t\u0000\u000260sq\u0000~\u00006t\u0000\u000217sq\u0000~\u00006t\u0000\u000236sq\u0000~\u00006t\u0000\u00016sq\u0000~\u00006t\u0000\u00011sq\u0000~\u00006t\u0000\u000259s"
+"q\u0000~\u00006t\u0000\u00015sq\u0000~\u00006t\u0000\u000216sq\u0000~\u00006t\u0000\u000246sq\u0000~\u00006t\u0000\u000237sq\u0000~\u00006t\u0000\u000256sq\u0000~\u00006t"
+"\u0000\u000255sq\u0000~\u00006t\u0000\u000233sq\u0000~\u00006t\u0000\u000211sq\u0000~\u00006t\u0000\u000240sq\u0000~\u00006t\u0000\u00014sq\u0000~\u00006t\u0000\u000262sq"
+"\u0000~\u00006t\u0000\u000245sq\u0000~\u00006t\u0000\u00017sq\u0000~\u00006t\u0000\u000238sq\u0000~\u00006t\u0000\u00012sq\u0000~\u00006t\u0000\u000275sq\u0000~\u00006t\u0000\u0002"
+"53sq\u0000~\u00006t\u0000\u000247sq\u0000~\u00006t\u0000\u000231sq\u0000~\u00006t\u0000\u000271sq\u0000~\u00006t\u0000\u000265sq\u0000~\u00006t\u0000\u000254sq\u0000"
+"~\u00006t\u0000\u000248sq\u0000~\u00006t\u0000\u000221sq\u0000~\u00006t\u0000\u000210sq\u0000~\u00006t\u0000\u000222sq\u0000~\u00006t\u0000\u000273sq\u0000~\u00006t\u0000"
+"\u00018sq\u0000~\u00006t\u0000\u000266xsr\u00000com.sun.msv.grammar.Expression$NullSetExpr"
+"ession\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003ppsr\u0000\u001bcom.sun.msv.util.StringPair\u00d0t\u001ej"
+"B\u008f\u008d\u00a0\u0002\u0000\u0002L\u0000\tlocalNameq\u0000~\u0000\u001dL\u0000\fnamespaceURIq\u0000~\u0000\u001dxpt\u0000\u000finteger-der"
+"ivedq\u0000~\u0000!sq\u0000~\u0000\nppsr\u0000 com.sun.msv.grammar.AttributeExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000"
+"\u0001\u0002\u0000\u0002L\u0000\u0003expq\u0000~\u0000\u0002L\u0000\tnameClassq\u0000~\u0000\rxq\u0000~\u0000\u0003q\u0000~\u0000\u0011psq\u0000~\u0000\u0013ppsr\u0000\"com."
+"sun.msv.datatype.xsd.QnameType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\'q\u0000~\u0000*t\u0000\u0005QName"
+"q\u0000~\u0000$q\u0000~\u0000\u00c9sq\u0000~\u0000\u00caq\u0000~\u0000\u00d3q\u0000~\u0000*sr\u0000#com.sun.msv.grammar.SimpleName"
+"Class\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\tlocalNameq\u0000~\u0000\u001dL\u0000\fnamespaceURIq\u0000~\u0000\u001dxr\u0000\u001dcom"
+".sun.msv.grammar.NameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpt\u0000\u0004typet\u0000)http://www."
+"w3.org/2001/XMLSchema-instancesr\u00000com.sun.msv.grammar.Expres"
+"sion$EpsilonExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003sq\u0000~\u0000\u0010\u0001q\u0000~\u0000\u00dbsq\u0000~\u0000\u00d5t\u0000\u0004"
+"Typeq\u0000~\u0000!q\u0000~\u0000\u00dbsq\u0000~\u0000\fpp\u0000sq\u0000~\u0000\u0000ppsq\u0000~\u0000\u0013ppq\u0000~\u0000)q\u0000~\u0000\u00c9sq\u0000~\u0000\u00caq\u0000~\u0000+"
+"q\u0000~\u0000*sq\u0000~\u0000\nppsq\u0000~\u0000\u00ceq\u0000~\u0000\u0011pq\u0000~\u0000\u00d0q\u0000~\u0000\u00d7q\u0000~\u0000\u00dbsq\u0000~\u0000\u00d5t\u0000\u0003UIDq\u0000~\u0000!sq\u0000"
+"~\u0000\nppsq\u0000~\u0000\fq\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\u0000ppsq\u0000~\u0000\u0013ppsr\u0000%com.sun.msv.datatype.x"
+"sd.DateTimeType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000)com.sun.msv.datatype.xsd.DateT"
+"imeBaseType\u0014W\u001a@3\u00a5\u00b4\u00e5\u0002\u0000\u0000xq\u0000~\u0000\'q\u0000~\u0000*t\u0000\bdateTimeq\u0000~\u0000$q\u0000~\u0000\u00c9sq\u0000~\u0000\u00ca"
+"q\u0000~\u0000\u00eeq\u0000~\u0000*sq\u0000~\u0000\nppsq\u0000~\u0000\u00ceq\u0000~\u0000\u0011pq\u0000~\u0000\u00d0q\u0000~\u0000\u00d7q\u0000~\u0000\u00dbsq\u0000~\u0000\u00d5t\u0000\u0005Startq"
+"\u0000~\u0000!q\u0000~\u0000\u00dbsq\u0000~\u0000\nppsq\u0000~\u0000\fq\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\u0000ppq\u0000~\u0000\u00easq\u0000~\u0000\nppsq\u0000~\u0000\u00ceq\u0000~"
+"\u0000\u0011pq\u0000~\u0000\u00d0q\u0000~\u0000\u00d7q\u0000~\u0000\u00dbsq\u0000~\u0000\u00d5t\u0000\u0006Finishq\u0000~\u0000!q\u0000~\u0000\u00dbsq\u0000~\u0000\nppsq\u0000~\u0000\fq\u0000~"
+"\u0000\u0011p\u0000sq\u0000~\u0000\u0000ppsq\u0000~\u0000\u0013ppsq\u0000~\u0000\u0017q\u0000~\u0000!pq\u0000~\u0000$\u0000\u0000q\u0000~\u0000)q\u0000~\u0000)q\u0000~\u00003sq\u0000~\u00004"
+"w\f\u0000\u0000\u0000\u0010?@\u0000\u0000\u0000\u0000\u0000\u0006sq\u0000~\u00006t\u0000\u00013sq\u0000~\u00006t\u0000\u00015sq\u0000~\u00006t\u0000\u00012sq\u0000~\u00006t\u0000\u00010sq\u0000~\u00006"
+"t\u0000\u00018sq\u0000~\u00006t\u0000\u00011xq\u0000~\u0000\u00c9sq\u0000~\u0000\u00cat\u0000\u000finteger-derivedq\u0000~\u0000!sq\u0000~\u0000\nppsq\u0000"
+"~\u0000\u00ceq\u0000~\u0000\u0011pq\u0000~\u0000\u00d0q\u0000~\u0000\u00d7q\u0000~\u0000\u00dbsq\u0000~\u0000\u00d5t\u0000\u0004Unitq\u0000~\u0000!q\u0000~\u0000\u00dbsq\u0000~\u0000\nppsq\u0000~\u0000"
+"\fq\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\u0000ppsq\u0000~\u0000\u0013ppsr\u0000#com.sun.msv.datatype.xsd.StringT"
+"ype\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001Z\u0000\risAlwaysValidxq\u0000~\u0000\'q\u0000~\u0000*t\u0000\u0006stringsr\u00005com.su"
+"n.msv.datatype.xsd.WhiteSpaceProcessor$Preserve\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq"
+"\u0000~\u0000#\u0001q\u0000~\u0000\u00c9sq\u0000~\u0000\u00caq\u0000~\u0001\u0019q\u0000~\u0000*sq\u0000~\u0000\nppsq\u0000~\u0000\u00ceq\u0000~\u0000\u0011pq\u0000~\u0000\u00d0q\u0000~\u0000\u00d7q\u0000~\u0000"
+"\u00dbsq\u0000~\u0000\u00d5t\u0000\u0005Valueq\u0000~\u0000!q\u0000~\u0000\u00dbsr\u0000\"com.sun.msv.grammar.ExpressionP"
+"ool\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\bexpTablet\u0000/Lcom/sun/msv/grammar/ExpressionP"
+"ool$ClosedHash;xpsr\u0000-com.sun.msv.grammar.ExpressionPool$Clos"
+"edHash\u00d7j\u00d0N\u00ef\u00e8\u00ed\u001c\u0003\u0000\u0003I\u0000\u0005countB\u0000\rstreamVersionL\u0000\u0006parentt\u0000$Lcom/su"
+"n/msv/grammar/ExpressionPool;xp\u0000\u0000\u0000\u0016\u0001pq\u0000~\u0000\u0005q\u0000~\u0000\bq\u0000~\u0000\u00e0q\u0000~\u0000\u00fdq\u0000~"
+"\u0000\u00fbq\u0000~\u0000\u0007q\u0000~\u0000\u0006q\u0000~\u0000\u0012q\u0000~\u0000\u00e7q\u0000~\u0000\u00f4q\u0000~\u0000\tq\u0000~\u0001\u0015q\u0000~\u0001\u0013q\u0000~\u0000\u00cdq\u0000~\u0000\u00e3q\u0000~\u0000\u00f0q\u0000~"
+"\u0000\u00f7q\u0000~\u0001\u000fq\u0000~\u0000\u00e9q\u0000~\u0000\u00f6q\u0000~\u0001\u001dq\u0000~\u0000\u000bx"));
        }
        return new com.sun.msv.verifier.regexp.REDocumentDeclaration(schemaFragment);
    }

    public class Unmarshaller
        extends com.tapsterrock.mspdi.schema.impl.runtime.AbstractUnmarshallingEventHandlerImpl
    {


        public Unmarshaller(com.tapsterrock.mspdi.schema.impl.runtime.UnmarshallingContext context) {
            super(context, "-------------------");
        }

        protected Unmarshaller(com.tapsterrock.mspdi.schema.impl.runtime.UnmarshallingContext context, int startState) {
            this(context);
            state = startState;
        }

        public java.lang.Object owner() {
            return com.tapsterrock.mspdi.schema.impl.TimephasedDataTypeImpl.this;
        }

        public void enterElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname, org.xml.sax.Attributes __atts)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  15 :
                        if (("Value" == ___local)&&("http://schemas.microsoft.com/project" == ___uri)) {
                            context.pushAttributes(__atts, true);
                            state = 16;
                            return ;
                        }
                        state = 18;
                        continue outer;
                    case  3 :
                        if (("UID" == ___local)&&("http://schemas.microsoft.com/project" == ___uri)) {
                            context.pushAttributes(__atts, true);
                            state = 4;
                            return ;
                        }
                        break;
                    case  0 :
                        if (("Type" == ___local)&&("http://schemas.microsoft.com/project" == ___uri)) {
                            context.pushAttributes(__atts, true);
                            state = 1;
                            return ;
                        }
                        state = 3;
                        continue outer;
                    case  6 :
                        if (("Start" == ___local)&&("http://schemas.microsoft.com/project" == ___uri)) {
                            context.pushAttributes(__atts, true);
                            state = 7;
                            return ;
                        }
                        state = 9;
                        continue outer;
                    case  9 :
                        if (("Finish" == ___local)&&("http://schemas.microsoft.com/project" == ___uri)) {
                            context.pushAttributes(__atts, true);
                            state = 10;
                            return ;
                        }
                        state = 12;
                        continue outer;
                    case  12 :
                        if (("Unit" == ___local)&&("http://schemas.microsoft.com/project" == ___uri)) {
                            context.pushAttributes(__atts, true);
                            state = 13;
                            return ;
                        }
                        state = 15;
                        continue outer;
                    case  18 :
                        revertToParentFromEnterElement(___uri, ___local, ___qname, __atts);
                        return ;
                }
                super.enterElement(___uri, ___local, ___qname, __atts);
                break;
            }
        }

        public void leaveElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  15 :
                        state = 18;
                        continue outer;
                    case  11 :
                        if (("Finish" == ___local)&&("http://schemas.microsoft.com/project" == ___uri)) {
                            context.popAttributes();
                            state = 12;
                            return ;
                        }
                        break;
                    case  0 :
                        state = 3;
                        continue outer;
                    case  2 :
                        if (("Type" == ___local)&&("http://schemas.microsoft.com/project" == ___uri)) {
                            context.popAttributes();
                            state = 3;
                            return ;
                        }
                        break;
                    case  5 :
                        if (("UID" == ___local)&&("http://schemas.microsoft.com/project" == ___uri)) {
                            context.popAttributes();
                            state = 6;
                            return ;
                        }
                        break;
                    case  8 :
                        if (("Start" == ___local)&&("http://schemas.microsoft.com/project" == ___uri)) {
                            context.popAttributes();
                            state = 9;
                            return ;
                        }
                        break;
                    case  6 :
                        state = 9;
                        continue outer;
                    case  9 :
                        state = 12;
                        continue outer;
                    case  12 :
                        state = 15;
                        continue outer;
                    case  17 :
                        if (("Value" == ___local)&&("http://schemas.microsoft.com/project" == ___uri)) {
                            context.popAttributes();
                            state = 18;
                            return ;
                        }
                        break;
                    case  18 :
                        revertToParentFromLeaveElement(___uri, ___local, ___qname);
                        return ;
                    case  14 :
                        if (("Unit" == ___local)&&("http://schemas.microsoft.com/project" == ___uri)) {
                            context.popAttributes();
                            state = 15;
                            return ;
                        }
                        break;
                }
                super.leaveElement(___uri, ___local, ___qname);
                break;
            }
        }

        public void enterAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  15 :
                        state = 18;
                        continue outer;
                    case  0 :
                        state = 3;
                        continue outer;
                    case  6 :
                        state = 9;
                        continue outer;
                    case  9 :
                        state = 12;
                        continue outer;
                    case  12 :
                        state = 15;
                        continue outer;
                    case  18 :
                        revertToParentFromEnterAttribute(___uri, ___local, ___qname);
                        return ;
                }
                super.enterAttribute(___uri, ___local, ___qname);
                break;
            }
        }

        public void leaveAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  15 :
                        state = 18;
                        continue outer;
                    case  0 :
                        state = 3;
                        continue outer;
                    case  6 :
                        state = 9;
                        continue outer;
                    case  9 :
                        state = 12;
                        continue outer;
                    case  12 :
                        state = 15;
                        continue outer;
                    case  18 :
                        revertToParentFromLeaveAttribute(___uri, ___local, ___qname);
                        return ;
                }
                super.leaveAttribute(___uri, ___local, ___qname);
                break;
            }
        }

        public void handleText(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                try {
                    switch (state) {
                        case  15 :
                            state = 18;
                            continue outer;
                        case  0 :
                            state = 3;
                            continue outer;
                        case  10 :
                            eatText1(value);
                            state = 11;
                            return ;
                        case  6 :
                            state = 9;
                            continue outer;
                        case  9 :
                            state = 12;
                            continue outer;
                        case  16 :
                            eatText2(value);
                            state = 17;
                            return ;
                        case  12 :
                            state = 15;
                            continue outer;
                        case  18 :
                            revertToParentFromText(value);
                            return ;
                        case  7 :
                            eatText3(value);
                            state = 8;
                            return ;
                        case  1 :
                            eatText4(value);
                            state = 2;
                            return ;
                        case  13 :
                            eatText5(value);
                            state = 14;
                            return ;
                        case  4 :
                            eatText6(value);
                            state = 5;
                            return ;
                    }
                } catch (java.lang.RuntimeException e) {
                    handleUnexpectedTextException(value, e);
                }
                break;
            }
        }

        private void eatText1(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _Finish = ((java.util.Calendar) com.sun.msv.datatype.xsd.DateTimeType.theInstance.createJavaObject(com.sun.xml.bind.WhiteSpaceProcessor.collapse(value), null));
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

        private void eatText2(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _Value = value;
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

        private void eatText3(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _Start = ((java.util.Calendar) com.sun.msv.datatype.xsd.DateTimeType.theInstance.createJavaObject(com.sun.xml.bind.WhiteSpaceProcessor.collapse(value), null));
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

        private void eatText4(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _Type = javax.xml.bind.DatatypeConverter.parseInteger(com.sun.xml.bind.WhiteSpaceProcessor.collapse(value));
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

        private void eatText5(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _Unit = javax.xml.bind.DatatypeConverter.parseInteger(com.sun.xml.bind.WhiteSpaceProcessor.collapse(value));
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

        private void eatText6(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _UID = javax.xml.bind.DatatypeConverter.parseInteger(com.sun.xml.bind.WhiteSpaceProcessor.collapse(value));
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

    }

}
