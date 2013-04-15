/*******************************************************************************
 * Copyright 2012 Urbancode, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.urbancode.terraform.tasks.vmware.util;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ip4 implements Comparable<Ip4>, Serializable {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Pattern ipPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
    private static final long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    final private int address;

    //----------------------------------------------------------------------------------------------
    public Ip4(String address) {
        this.address = stringToInt(address);
    }

    //----------------------------------------------------------------------------------------------
    public Ip4(int address) {
        this.address = address;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof Ip4) {
            Ip4 rhs = (Ip4)obj;
            result = address == rhs.address;
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public int getAddress() {
        return address;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public int hashCode() {
        return address;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return intToString(address);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public int compareTo(Ip4 rhs) {
        // use long to avoid sign issues
        long longAddress = 0xFFFFFFFFL & address;
        long longRhsAddress = 0xFFFFFFFFL & rhs.address;
        return (longAddress < longRhsAddress) ? -1 : (longAddress == longRhsAddress) ? 0 : 1;
    }

    //----------------------------------------------------------------------------------------------
    public Ip4 bitAnd(Ip4 rhs) {
        return new Ip4(address & rhs.address);
    }

    //----------------------------------------------------------------------------------------------
    public Ip4 bitOr(Ip4 rhs) {
        return new Ip4(address | rhs.address);
    }

    //----------------------------------------------------------------------------------------------
    public Ip4 bitNot() {
        return new Ip4(~address);
    }

    //----------------------------------------------------------------------------------------------
    public Ip4 plus(int increment) {
        return new Ip4(address + increment);
    }

    //----------------------------------------------------------------------------------------------
    public Ip4 minus(int decrement) {
        return new Ip4(address - decrement);
    }

    //----------------------------------------------------------------------------------------------
    public int stringToInt(String ip) {
        Matcher matcher = ipPattern.matcher(ip);
        if (!matcher.matches()) {
            if (ip != null) {
                throw new IllegalArgumentException("Invalid address: " + ip);
            }
            else {
                throw new IllegalArgumentException("Invalid (null) address");
            }
        }
        int byte1 = Integer.parseInt(matcher.group(1));
        int byte2 = Integer.parseInt(matcher.group(2));
        int byte3 = Integer.parseInt(matcher.group(3));
        int byte4 = Integer.parseInt(matcher.group(4));
        if (byte1 < 0 || byte1 > 255) {
            throw new IllegalArgumentException("Invalid address at first byte: " + byte1);
        }
        if (byte2 < 0 || byte2 > 255) {
            throw new IllegalArgumentException("Invalid address at second byte: " + byte2);
        }
        if (byte3 < 0 || byte3 > 255) {
            throw new IllegalArgumentException("Invalid address at third byte: " + byte3);
        }
        if (byte4 < 0 || byte4 > 255) {
            throw new IllegalArgumentException("Invalid address at fourth byte: " + byte4);
        }
        return byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
    }

    //----------------------------------------------------------------------------------------------
    public String intToString(int value) {
        int byte1 = (value >>> 24) & 0xFF;
        int byte2 = (value >>> 16) & 0xFF;
        int byte3 = (value >>> 8) & 0xFF;
        int byte4 = (value >>> 0) & 0xFF;
        return String.format("%d.%d.%d.%d", byte1, byte2, byte3, byte4);
    }
}
