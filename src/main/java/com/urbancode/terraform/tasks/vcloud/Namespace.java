package com.urbancode.terraform.tasks.vcloud;

public enum Namespace {

    XSI ("xsi", "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""),
    VCLOUD ("", "xmlns=\"http://www.vmware.com/vcloud/v1.5\""),
    OVF ("ovf", "xmlns:ovf=\"http://schemas.dmtf.org/ovf/envelope/1\">"),
    RASD ("rasd", "xmlns:rasd=\"http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/" +
            "CIM_ResourceAllocationSettingData\""),
    VCLOUD_RASD ("vcloud", "vcloud:type=\"application/vnd.vmware.vcloud.rasdItem+xml\""),
    VCLOUD_RASD_ITEM_LIST ("", "type=\"application/vnd.vmware.vcloud.rasdItemsList+xml\"");
    
    final private String prefix;
    final private String declaration;
    
    Namespace(String prefix, String declaration) {
        this.prefix = prefix;
        this.declaration = declaration;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public String getDeclaration() {
        return declaration;
    }
}
