package org.urbancode.terraform.credentials.microsoft;

import java.util.Properties;

import org.urbancode.terraform.credentials.Credentials;
import org.urbancode.terraform.credentials.CredentialsParser;

public class CredentialsParserMicrosoft extends CredentialsParser {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    @Override
    public Credentials parse(Properties props) {
        // TODO Auto-generated method stub
        return new CredentialsMicrosoft("", "", "");
    }

}
