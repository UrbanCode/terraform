package com.urbancode.uprovision.tasks.util;

import java.io.Serializable;

public class Property implements Serializable {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    private static final long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    final private String name;
    final private String value;

    //----------------------------------------------------------------------------------------------
    public Property(String name, String value) {
        this.name = name;
        this.value = value;
    }

    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }

    //----------------------------------------------------------------------------------------------
    public String getValue() {
        return value;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return name + "=" + value;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public int hashCode() {
        return name.hashCode() + value.hashCode();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public boolean equals(Object other) {
        return (((Property) other).getName().equals(name) && ((Property) other).getValue().equals(value));
    }
}
