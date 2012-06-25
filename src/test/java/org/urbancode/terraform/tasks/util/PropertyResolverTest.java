package org.urbancode.terraform.tasks.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.urbancode.terraform.tasks.util.Property;
import org.urbancode.terraform.tasks.util.PropertyResolver;

import junit.framework.Assert;

public class PropertyResolverTest {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    @Test
    public void testResolver() {
        Property myProp = new Property("myname", "myval");
        Property myProp2 = new Property("myname2", "myval2");
        List<Property> properties = new ArrayList<Property>();
        properties.add(myProp);
        properties.add(myProp2);
        PropertyResolver resolver = new PropertyResolver(properties);
        Assert.assertEquals(true, "myval".equals(resolver.resolve("${myname}")));
        Assert.assertEquals(true, "null".equals(resolver.resolve("${doesnotexist}")));
    }
}
