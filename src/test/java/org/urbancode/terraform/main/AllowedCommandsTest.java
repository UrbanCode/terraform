package org.urbancode.terraform.main;

import junit.framework.Assert;

import org.junit.Test;

import com.urbancode.terraform.main.AllowedCommands;

public class AllowedCommandsTest {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    @Test
    public void testEnum() {
        String create = "create";
        Assert.assertEquals(true, AllowedCommands.contains(create));
    }
}
