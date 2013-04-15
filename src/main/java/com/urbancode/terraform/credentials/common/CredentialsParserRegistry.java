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
package com.urbancode.terraform.credentials.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class CredentialsParserRegistry {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    static private final Logger log = Logger.getLogger(CredentialsParserRegistry.class);

    static private CredentialsParserRegistry instance = new CredentialsParserRegistry();

    //----------------------------------------------------------------------------------------------
    static public CredentialsParserRegistry getInstance() {
        return instance;
    }

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private Map<String, Class<? extends CredentialsParser>> parserMap;

    //----------------------------------------------------------------------------------------------
    private CredentialsParserRegistry() {
        parserMap = new HashMap<String, Class<? extends CredentialsParser>>();
    }

    //----------------------------------------------------------------------------------------------
    public CredentialsParser getParser(String type) {
        CredentialsParser result = null;

        try {
            Class<? extends CredentialsParser> clazz = parserMap.get(type);
            if (clazz == null) {
                throw new NullPointerException("Parser not found for type " + type);
            }
            result = clazz.newInstance();
        } catch (InstantiationException e) {
            log.error("Could not instantiate class " + type + " : " + parserMap.get(type), e);
        } catch (IllegalAccessException e) {
            log.error("Could not access " + type + " : " + parserMap.get(type), e);
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void register(String type, Class<? extends CredentialsParser> clazz) {
        parserMap.put(type, clazz);
    }
}
