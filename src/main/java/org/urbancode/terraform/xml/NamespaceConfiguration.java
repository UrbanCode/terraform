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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.log4j.Logger;

public class NamespaceConfiguration {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    static private final Logger log = Logger.getLogger(NamespaceConfiguration.class);

    private static NamespaceConfiguration instance = new NamespaceConfiguration();

    //----------------------------------------------------------------------------------------------
    public static NamespaceConfiguration getInstance() {
        return instance;
    }

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    final private String NAMESPACE_FILE = "terralib.classes";

    // <NameSpace, <ElementName, ClassName>>
    private Map<String, BidiMap> nameSpaces = new HashMap<String, BidiMap>();

    //----------------------------------------------------------------------------------------------
    private NamespaceConfiguration() {

    }

    //----------------------------------------------------------------------------------------------
    public Map<String, BidiMap> getNameSpaces() {
        return nameSpaces;
    }

    //----------------------------------------------------------------------------------------------
    public String getClassNameForElement(String element, String nameSpace) {
        String result = null;
        log.debug("Looking for element: " + element + " in namespace: " + nameSpace);

        if (!nameSpaces.containsKey(nameSpace)) {
            loadNameSpaceFromClassPath(nameSpace);
        }

        BidiMap tmp = nameSpaces.get(nameSpace);

        if (tmp != null) {
            result = (String) tmp.get(element);
        }

        log.debug("Classname found: " + result);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String getNameSpaceForClassName(String clazzName) {
        String result = null;

        // gets first match only
        for (String ns : nameSpaces.keySet()) {
            if (nameSpaces.get(ns).containsValue(clazzName)) {
                result = ns;
                break;
            }
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String getElementForClassName(String clazzName) {
        String result = null;

        // gets first match only
        for (String ns : nameSpaces.keySet()) {
            if (nameSpaces.get(ns).containsValue(clazzName)) {
                result = (String)nameSpaces.get(ns).getKey(clazzName);
                break;
            }
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    private void loadNameSpaceFromClassPath(String nameSpace) {
        BidiMap biMap = new DualHashBidiMap();
        String filePath = nameSpace.replaceAll("[:.:]", File.separator);
        String resourceName = filePath + File.separator + NAMESPACE_FILE;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();

        log.debug("Looking for resource: " + resourceName);

        try {
            props.load(classLoader.getResourceAsStream(resourceName));
        }
        catch (IOException e) {
            log.error(e.getClass() + " caught in " + this.getClass());
            // swallow
        }

        // build the biDirectionalMap
        for (Object prop : props.keySet()) {
            biMap.put(prop, props.getProperty((String)prop));
        }

        log.debug("added following elements into map: " + biMap.keySet());
        log.debug("Added following value: " + biMap.values());

        nameSpaces.put(nameSpace, biMap);
    }

    //----------------------------------------------------------------------------------------------
    public String getPrefixForNameSpace(String nameSpace) {
        String result;

        int tmp = nameSpace.lastIndexOf('.');
        result = nameSpace.substring(tmp+1, nameSpace.length());

        return result;
    }
}
