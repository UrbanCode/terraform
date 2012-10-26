package org.urbancode.terraform.tasks.rackspace;

public enum Subdomains {

    SERVERS("servers");

    private String subdomain;

    //----------------------------------------------------------------------------------------------
    private Subdomains(String subdomain) {
        this.subdomain = subdomain;
    }

    //----------------------------------------------------------------------------------------------
    public String getSubdomain() {
        return this.subdomain;
    }

    //----------------------------------------------------------------------------------------------
    public boolean equalsIgnoreCase(String string) {
        return subdomain.equalsIgnoreCase(string);
    }

    //----------------------------------------------------------------------------------------------
    public static boolean contains(String testSubdomain) {
        for (Subdomains c : values()) {
            if (c.getSubdomain().equals(testSubdomain)) {
                return true;
            }
        }
        return false;
    }

    //----------------------------------------------------------------------------------------------
    public static boolean containsIgnoreCase(String testSubdomain) {
        for (Subdomains c : values()) {
            if (c.getSubdomain().equalsIgnoreCase(testSubdomain)) {
                return true;
            }
        }
        return false;
    }
}
