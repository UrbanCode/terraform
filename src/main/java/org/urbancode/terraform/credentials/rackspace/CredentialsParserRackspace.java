package org.urbancode.terraform.credentials.rackspace;

import java.util.Properties;

import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.CredentialsParser;

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
