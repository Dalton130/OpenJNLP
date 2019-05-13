/*
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/ 
 *
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights and
 * limitations under the License.
 *
 * The Original Code is openjnlp.nanode.org code.
 *
 * The Initial Developer of the Original Code is Nanode LLC. Portions created by Nanode are
 * Copyright (C) 2001 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 */
package org.nanode.launcher;


public final class Gestalt {
    public static final String KEY_MACOS = "macos";
    public static final String KEY_MACOSX = "macosx";
    public static final String KEY_WINDOWS = "windows";
    public static final String KEY_UNIX = "unix";
    public static final String KEY_OTHER = "other";

    public static final int OSTYPE_MASK = 0x1ff;
    public static final int OSTYPE_UNKNOWN = 0;
    public static final int OSTYPE_MACOS = 1;
    public static final int OSTYPE_WINDOWS = 2;
    public static final int OSTYPE_UNIX = 3;
    
    public static final int OSPLATFORM_MASK = 0xfe00;
    public static final int OSPLATFORM_UNKNOWN = 0;
    
    // Mac OS gets 0x0200-0x0e00
    public static final int OSPLATFORM_MACOS = 0x0200;
    public static final int OSPLATFORM_MACOSX = 0x0400;
    
    // Windows gets 0x1000-0x1e00
    public static final int OSPLATFORM_WINDOWS95 = 0x1000;
    public static final int OSPLATFORM_WINDOWS98 = 0x1200;
    public static final int OSPLATFORM_WINDOWSNT = 0x1400;
    public static final int OSPLATFORM_WINDOWS2K = 0x1600;

    // these need to change
    public static final int OSPLATFORM_LINUX = 0x2000;
    public static final int OSPLATFORM_SOLARIS = 0x2200;
    
    private static final String osName;
    
    static {
        String s = "";
        try {
            s = System.getProperty("os.name", "");
        } catch (Throwable t) {
        }
        
        osName = s;
    }
    

    public static final int osType() {
        int type = OSTYPE_UNKNOWN;
        
        if (osName.startsWith("Mac")) {
            type = OSTYPE_MACOS;
        } else if (osName.startsWith("Windows")) {
            type = OSTYPE_WINDOWS;
        } else if (osName.startsWith("Linux")
                || osName.startsWith("Solaris")) {
            type = OSTYPE_UNIX;
        }
        
        return type;
    }
    
    public static final int osPlatformFromValue(int osValue) {
        return (osValue & OSPLATFORM_MASK);
    }
    
    public static final int osType(int osValue) {
        return (osValue & OSTYPE_MASK);
    }
    
    public static final int osValue() {
        int type = osType();

        return (osPlatform(type) | type);
    }
    
    public static final int osPlatform() {
        return osPlatform(osType());
    }

    public static final int osPlatform(int type) {
        int platform = OSPLATFORM_UNKNOWN;

        switch (type) {
            case OSTYPE_MACOS:
                platform = OSPLATFORM_MACOS;

                if (osName.startsWith("Mac OS X")) {
                    platform = OSPLATFORM_MACOSX;
                }
                
                break;
            case OSTYPE_WINDOWS:
                if (osName.startsWith("Windows 95")) {
                    platform = OSPLATFORM_WINDOWS95;
                } else if (osName.startsWith("Windows 98")) {
                    platform = OSPLATFORM_WINDOWS98;
                } else if (osName.startsWith("Windows NT")) {
                    platform = OSPLATFORM_WINDOWSNT;
                } else if (osName.startsWith("Windows 2000")) {
                    platform = OSPLATFORM_WINDOWS2K;
                }

                break;
            case OSTYPE_UNIX:
                if (osName.startsWith("Linux")) {
                    platform = OSPLATFORM_LINUX;
                } else if (osName.startsWith("Solaris")) {
                    platform = OSPLATFORM_SOLARIS;
                }

                break;
        }
        
        return platform;
    }

    public static final String platformKey() {
        return platformKey(osPlatform());
    }

    public static final String platformKey(int platform) {
        String key = KEY_OTHER;

        switch (platform) {
            case OSPLATFORM_MACOS:
                key = KEY_MACOS;
                break;
            case OSPLATFORM_MACOSX:
                key = KEY_MACOSX;
                break;
            case OSPLATFORM_WINDOWS95:
            case OSPLATFORM_WINDOWS98:
            case OSPLATFORM_WINDOWSNT:
            case OSPLATFORM_WINDOWS2K:
                key = KEY_WINDOWS;
                break;
            case OSPLATFORM_LINUX:
            case OSPLATFORM_SOLARIS:
                key = KEY_UNIX;
                break;
        }

        return key;
    }
}