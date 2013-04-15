package com.urbancode.terraform.tasks.rackspace;

public enum ServerFlavor {

    SIZE_512MB("512MB"), SIZE_1GB("1GB"), SIZE_2GB("2GB"), SIZE_4GB("4GB"), SIZE_8GB("8GB"), SIZE_15GB("15GB"), SIZE_30GB("30GB");

    private String size;

    //----------------------------------------------------------------------------------------------
    private ServerFlavor(String size) {
        this.size = size;
    }

    //----------------------------------------------------------------------------------------------
    static public boolean contains(String testString) {
        for (ServerFlavor c : values()) {
            if (c.size.equals(testString)) {
                return true;
            }
        }
        return false;
    }

    //----------------------------------------------------------------------------------------------
    static public String lookupFlavorID(String testSize) {
        if (testSize.equalsIgnoreCase(SIZE_512MB.size)) {
            return "2";
        }
        else if (testSize.equalsIgnoreCase(SIZE_1GB.size)) {
            return "3";
        }
        else if (testSize.equalsIgnoreCase(SIZE_2GB.size)) {
            return "4";
        }
        else if (testSize.equalsIgnoreCase(SIZE_4GB.size)) {
            return "5";
        }
        else if (testSize.equalsIgnoreCase(SIZE_8GB.size)) {
            return "6";
        }
        else if (testSize.equalsIgnoreCase(SIZE_15GB.size)) {
            return "7";
        }
        else if (testSize.equalsIgnoreCase(SIZE_30GB.size)) {
            return "8";
        }
        else {
            return null;
        }
    }



}
