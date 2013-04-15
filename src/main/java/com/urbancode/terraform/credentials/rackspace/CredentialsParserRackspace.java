package com.urbancode.terraform.credentials.rackspace;

import java.util.Properties;

import com.urbancode.terraform.credentials.common.Credentials;
import com.urbancode.terraform.credentials.common.CredentialsParser;

public class CredentialsParserRackspace extends CredentialsParser {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    @Override
    public Credentials parse(Properties props) {
        Credentials result = null;

        String name = props.getProperty("name");
        String username = props.getProperty("username");
        String password = "";
        String apiKey = props.getProperty("api-key");

        result = new CredentialsRackspace(name, username, password, apiKey);
        return result;
    }

}
