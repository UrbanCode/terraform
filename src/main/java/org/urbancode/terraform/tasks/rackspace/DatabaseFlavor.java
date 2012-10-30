package org.urbancode.terraform.tasks.rackspace;

public enum DatabaseFlavor {
    SIZE_512MB("512MB"), SIZE_1GB("1GB"), SIZE_2GB("2GB"), SIZE_4GB("4GB");

    private String size;

    //----------------------------------------------------------------------------------------------
    private DatabaseFlavor(String size) {
        this.size = size;
    }

    //----------------------------------------------------------------------------------------------
    static public boolean contains(String testString) {
        for (DatabaseFlavor c : values()) {
            if (c.size.equals(testString)) {
                return true;
            }
        }
        return false;
    }

    //----------------------------------------------------------------------------------------------
    static public String lookupFlavorID(String testSize) {
        if (testSize.equalsIgnoreCase(SIZE_512MB.size)) {
            return "1";
        }
        else if (testSize.equalsIgnoreCase(SIZE_1GB.size)) {
            return "2";
        }
        else if (testSize.equalsIgnoreCase(SIZE_2GB.size)) {
            return "3";
        }
        else if (testSize.equalsIgnoreCase(SIZE_4GB.size)) {
            return "4";
        }
        else {
            return null;
        }
    }
}
