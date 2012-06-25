package org.urbancode.terraform.credentials.vmware;

import java.util.Properties;

import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.CredentialsParser;


public class CredentialsParserVmware extends CredentialsParser {


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
        String url = props.getProperty("url");
        result = new CredentialsVmware(name, username, password, url);

        return result;
    }

}
