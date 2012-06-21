package com.urbancode.uprovision.tasks.util;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


public class PropertyResolver {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(PropertyResolver.class);
    final static private Pattern var = Pattern.compile("\\$\\{([^}]+)\\}");
    final static private String symbols = "abcdefghijklmnopqrstuvwxyz0123456789";

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private SecureRandom random = new SecureRandom();
    private Map<String, String> properties = new HashMap<String, String>();

    //----------------------------------------------------------------------------------------------
    public PropertyResolver(List<Property> properties) {
        for (Property p : properties) {
            setProperty(p.getName(), p.getValue());
        }
        if (log.isDebugEnabled()) {
            log.debug("Initializing properties: " + properties);
        }
    }

    //----------------------------------------------------------------------------------------------
    public String getProperty(String name) {
        String result;
        if (name.equals("random")) {
            char[] randomChars = new char[12];
            for (int i = 0; i < randomChars.length; i++) {
                randomChars[i] = symbols.charAt(random.nextInt(symbols.length()));
            }
            result = new String(randomChars);
        }
        else {
            result = properties.get(name);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    //----------------------------------------------------------------------------------------------
    public void removeProperty(String name) {
        properties.remove(name);
    }

    //----------------------------------------------------------------------------------------------
    public String resolve(String text) {
        String result;
        Matcher matcher = var.matcher(text);
        StringBuilder builder = new StringBuilder();
        int start = 0;
        while (matcher.find()) {
            builder.append(text.substring(start, matcher.start()));
            String variable = matcher.group(1);
            String value = getProperty(variable);
            if (log.isDebugEnabled()) {
                log.debug("Resolved " + variable + " to " + value);
            }
            builder.append(value);
            start = matcher.end();
        }
        builder.append(text.substring(start));
        result = builder.toString();
        if (log.isDebugEnabled()) {
            log.debug("Resolved " + text + " to " + result);
        }
        return result;
    }
}
