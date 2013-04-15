package com.urbancode.terraform.credentials.microsoft;

import java.util.Properties;

import com.urbancode.terraform.credentials.common.Credentials;
import com.urbancode.terraform.credentials.common.CredentialsParser;

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
        return new CredentialsMicrosoft("", "", "");
    }

}
