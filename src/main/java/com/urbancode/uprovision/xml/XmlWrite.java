package com.urbancode.uprovision.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.urbancode.uprovision.tasks.common.Task;

public class XmlWrite {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(XmlWrite.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private PersistenceConfig persistConf = PersistenceConfig.getInstance();

    //----------------------------------------------------------------------------------------------
    public XmlWrite() {
        
    }

    //----------------------------------------------------------------------------------------------
    public String convertFromCamelCase(String toConvert) {
        String result = toConvert;
        StringBuilder builder = new StringBuilder();

        for (int i=0; i<result.length(); i++) {
            if (Character.isUpperCase(result.charAt(i)) && i != 0) {
                builder.append("-");
            }
            builder.append(result.charAt(i));
        }
        result = builder.toString().toLowerCase();

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public List<Method> findMethodsForClass(String methodPrefix, Class clazz)
    throws ClassNotFoundException {
        List<Method> result = new ArrayList<Method>();
        for (Method method : clazz.getMethods()) {
            if (method.getName().startsWith(methodPrefix)) {
                // TODO - need to find another solution for this
                if (!(method.getName().equals("getClass") || Modifier.isProtected(method.getModifiers()))) {
                    result.add(method);
                }
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void makeXml(Object object, Document doc, Element parent)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        
//        String nameSpace = object.getClass().getPackage().getName();
        
        String className = object.getClass().getCanonicalName();
        String nameSpace = persistConf.getNameSpaceForClassName(className);
        
        String prefix = persistConf.getPrefixForNameSpace(nameSpace);
        
        String elemName = persistConf.getElementForClassName(className);
        Element element = doc.createElementNS(nameSpace, prefix + ":" + elemName);
        
        if (parent == null) {
            doc.appendChild(element);
        }
        else {
            parent.appendChild(element);
        }

        List<Method> methods = findMethodsForClass("get", object.getClass());
        for (Method method : methods) {
            Object methodResult = method.invoke(object);

            // if the result is a list of Tasks,
            // then iterate through each one and write it out as a sub-element
            if (methodResult instanceof List<?>) {
                for (Task nextTask : (List<Task>)methodResult) {
                    makeXml(nextTask, doc, element);
                }
            }
            // if the result is a Task,
            // then write it out as a sub-element
            else if (methodResult instanceof Task) {
                makeXml(methodResult, doc, element);
            }
            // if the result is not any of the above,
            // then write it out as an attribute
            else {
                String attrName = method.getName();
                attrName = attrName.replaceFirst("get", "");
                attrName = convertFromCamelCase(attrName);
                if (methodResult == null  || "null".equals(methodResult)) {
                    methodResult = "";
                }
                element.setAttribute(attrName, methodResult.toString());
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public void writeDocToFile(File filePath, Document doc)
    throws TransformerException, FileNotFoundException, IOException {
        Transformer trans = createTransformer();
        String result = docToString(doc, trans);
        log.info(result);
        writeFile(filePath, result);
    }

    //----------------------------------------------------------------------------------------------
    public Transformer createTransformer() throws TransformerConfigurationException {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        return transformer;
    }

    //----------------------------------------------------------------------------------------------
    public String docToString(Document doc, Transformer trans) 
    throws TransformerException {
        String result;

        StringWriter writer = new StringWriter();
        StreamResult stream = new StreamResult(writer);
        DOMSource source = new DOMSource(doc);
        trans.transform(source, stream);
        result = writer.toString();

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void writeFile(File filePath, String toWrite) 
    throws IOException, FileNotFoundException {
        FileUtils.writeStringToFile(filePath, toWrite);
    }
}
