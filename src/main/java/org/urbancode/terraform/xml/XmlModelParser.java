/*******************************************************************************
 * Copyright 2012 Urbancode, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.urbancode.terraform.xml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.Context;
import org.urbancode.terraform.tasks.common.ExtensionTask;
import org.urbancode.terraform.tasks.util.PropertyResolver;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



public class XmlModelParser {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(XmlModelParser.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private Context context;
    private NamespaceConfiguration persistConf;
    private PropertyResolver resolver;

    //----------------------------------------------------------------------------------------------
    public XmlModelParser() {
        persistConf = NamespaceConfiguration.getInstance();
    }

    //----------------------------------------------------------------------------------------------
    public void setContext(Context context) {
        this.context = context;
    }

    //----------------------------------------------------------------------------------------------
    public void setPropertyResolver(PropertyResolver resolver) {
        this.resolver = resolver;
    }

    //----------------------------------------------------------------------------------------------
    public Context parse(Element element)
    throws XmlParsingException {
        Context result = null;

        parse(element, this);
        this.context.setResolver(resolver);
        result = this.context;
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void parse(Element element, Object context)
    throws XmlParsingException {
        Node child;
        String prefix = element.getPrefix();
        if(prefix == null) {
            //dom thinks default namespace (empty prefix) is null
            prefix = "";
        }
        String nodeName = element.getNodeName();
        String uri = element.getNamespaceURI();

        // ensure that we have a NameSpace
        if (uri == null || uri.length() == 0) {
            log.error("uri is null or empty!");
            throw new XmlParsingException("No NameSpace found for element: "+nodeName);
        }

        if(context == null) {
            log.error("Context is null!");
            throw new XmlParsingException("Context of element " + element.getNodeName() + " is null");
        }

        Object nContext = null;
        String noPrefixNodeName = nodeName.substring(nodeName.indexOf(':')+1, nodeName.length());
        String className = persistConf.getClassNameForElement(noPrefixNodeName, uri);
        Class clazz;
        try {

            log.debug("Instantiating class: " + className);
            clazz = Class.forName(className);

            // check whether we should call a create method or
            // create the instance ourselves and call a setter
            if (ExtensionTask.class.isAssignableFrom(clazz)) {
                // create our own instance and pass it to a setter
                Object obj = clazz.newInstance();

                String methodToFind = "add" + convertToCamelCase(element.getLocalName());
                Method method = getMethodForName(methodToFind, context.getClass());
                nContext = method.invoke(context, obj);
            }
            else if (Context.class.isAssignableFrom(clazz)) {
                Object newContext = clazz.newInstance();
                log.debug("Trying to run setContext( " + context + " ) on " + newContext);
                String methodToFind = "setContext";
                Method method = getMethodForName(methodToFind, context.getClass());
                method.invoke(context, newContext);
                nContext = newContext;
            }
            else {
                // call a create method
                String methodToFind = "create" + convertToCamelCase(element.getLocalName());
                log.debug("Lookin for method: " + methodToFind);
                Method method = getMethodForName(methodToFind, context.getClass());
                if (method != null) {
                    log.debug("Found method: " + method.getName());
                    nContext = method.invoke(context);
                }
                else {
                    throw new NullPointerException("Unable to find method " + methodToFind);
                }
            }

        }
        catch (ClassNotFoundException e) {
            throw new XmlParsingException("Error parsing element: "+element.getLocalName()
                    +". error with class: "+className, e);
        }
        catch (InstantiationException e) {
            throw new XmlParsingException("Error parsing element: "+element.getLocalName()
                    +". error with class: "+className, e);
        }
        catch (IllegalAccessException e) {
            throw new XmlParsingException("Error parsing element: "+element.getLocalName()
                    +". error with class: "+className, e);
        }
        catch (IllegalArgumentException e) {
            throw new XmlParsingException("Error parsing element: "+element.getLocalName()
                    +". error with class: "+className, e);
        }
        catch (InvocationTargetException e) {
            throw new XmlParsingException("Error invoking constructor involving element: "
                    +element.getLocalName() +". error with class: "+className, e);
        }

        setAllAttributes(element, nContext);

        // recurse into child elements
        int i = 0;
        while ((child = element.getChildNodes().item(i++)) != null) {
            if (!child.getNodeName().contains("#")) {
                parse((Element)child, nContext);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private String convertToCamelCase(String xmlElement) {
        StringBuilder result = new StringBuilder();
        String[] splitString = xmlElement.split("-");
        StringBuilder builder;

        for (String string : splitString) {
            string.replaceAll("-", "");
            builder = new StringBuilder(string.length());
            builder.append(Character.toUpperCase(string.charAt(0)))
                    .append(string.substring(1));
            result.append(builder.toString());
        }

        return result.toString();
    }

    //----------------------------------------------------------------------------------------------
    private Method getMethodForName(String methodName, Class clazz) {
        Method result = null;

        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                result = method;
                break;
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    private void setAttribute(String aName, String aValue, Object instance)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (aValue.equalsIgnoreCase("null") || aValue.equals("")) {
            aValue = null;
            log.debug("null attribute: " + aName);
            return;
        }
        boolean secure = aName.contains("password") || aName.contains("secure") ||
                aValue.contains("password") || aValue.contains("secure") ? true : false;

        String resolvedValue = resolver.resolve(aValue);
        if(!(resolvedValue == null || "".equals(resolvedValue) || "null".equalsIgnoreCase(resolvedValue))) {
            aValue = resolvedValue;
        }

        if (!secure) {
            log.debug("[" + instance.toString() + "] Setting attribute: " + aName + " to value: " + aValue);
        }
        String methodName = "set" + convertToCamelCase(aName);
        Method method = getMethodForName(methodName, instance.getClass());
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 0) {
            // Assumes setMethods have just 1 parameter
            if (paramTypes[0].getName().equals("boolean")) {
                boolean bValue = Boolean.parseBoolean(aValue);
                method.invoke(instance, bValue);
            }
            else if (paramTypes[0].getName().equals("int")) {
                int iValue = Integer.parseInt(aValue);
                method.invoke(instance, iValue);
            }

            else if (paramTypes[0].getName().equals("long")) {
                long iValue = Long.parseLong(aValue);
                method.invoke(instance, iValue);
            }

            else {
                method.invoke(instance, aValue);
            }
        }
        else {
            method.invoke(instance, aValue);
        }
    }

    //----------------------------------------------------------------------------------------------
    private void setAllAttributes(Element element, Object instance)
    throws XmlParsingException {
        Node attr = null;
        int i = 0;

        while ((attr = element.getAttributes().item(i++)) != null) {
            String name = attr.getNodeName();
            if (!name.startsWith("xmlns")) {
                try {
                    setAttribute(attr.getNodeName(), attr.getNodeValue(), instance);
                }
                catch (Exception e) {
                    String message = "";
                    if(instance == null) {
                        message = "exception while setting attribute " + name + "; instance was null";
                    }
                    else {
                        message = "exception while setting attribute " + name + " ";
                    }
                    throw new XmlParsingException(message, e);
                }
            }
        }
    }
}
