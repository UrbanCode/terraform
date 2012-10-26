package org.urbancode.terraform.tasks.rackspace;

public enum Region {

    DFW("DFW"), ORD("ORD"), LON("LON");

    private String region;

    //----------------------------------------------------------------------------------------------
    private Region(String region) {
        this.region = region;
    }

    //----------------------------------------------------------------------------------------------
    public String getRegion() {
        return this.region;
    }

    //----------------------------------------------------------------------------------------------
    public String getRegionLowerCase() {
        return this.region.toLowerCase();
    }

    //----------------------------------------------------------------------------------------------
    public boolean equalsIgnoreCase(String string) {
        return region.equalsIgnoreCase(string);
    }

    //----------------------------------------------------------------------------------------------
    public static boolean contains(String testRegion) {
        for (Region c : values()) {
            if (c.getRegion().equals(testRegion)) {
                return true;
            }
        }
        return false;
    }

    //----------------------------------------------------------------------------------------------
    public static boolean containsIgnoreCase(String testRegion) {
        for (Region c : values()) {
            if (c.getRegion().equalsIgnoreCase(testRegion)) {
                return true;
            }
        }
        return false;
    }
}
