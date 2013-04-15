package com.urbancode.terraform.credentials.vcloud;

import java.util.Properties;

import com.urbancode.terraform.credentials.common.Credentials;
import com.urbancode.terraform.credentials.common.CredentialsParser;

public class CredentialsParserVCloud extends CredentialsParser {

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
        String password = props.getProperty("password");
        String organization = props.getProperty("organization");
        String location = props.getProperty("location");
        String apiKey = props.getProperty("api-key");
        String secretKey = props.getProperty("secret-key");
        result = new CredentialsVCloud(name, username, password, organization, location, apiKey, secretKey);
        return result;
    }

}
