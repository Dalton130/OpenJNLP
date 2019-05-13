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
 * Copyright (C) 2001-2002 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 */
package org.nanode.launcher;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;


/**
 * Encapsulates all resources used by a descriptor. It is assumed that all resources are
 * needed on the current platform.
 *
 * @author Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 */
public class Resources {
    Set eagerSet;
    Set lazySet;

    Reference mainJar;
    Properties properties;


    public Resources() {
        eagerSet = Collections.synchronizedSet(new HashSet());
        lazySet = Collections.synchronizedSet(new HashSet());

        mainJar = null;
    }

    public void addReference(Reference ref) {
        if (ref == null) {
            return;
        }

        if (ref.isLazy()) {
            lazySet.add(ref);
        } else {
            eagerSet.add(ref);
        }
    }

    public Reference getMainJar() {
        return mainJar;
    }

    public void setMainJar(Reference rsrc) {
        mainJar = rsrc;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties props) {
        properties = props;
    }

    public Enumeration eagerJars() {
        return new RsrcEnumeration(Reference.class, true);
    }

    public Enumeration eagerNativelibs() {
        return new RsrcEnumeration(NativelibReference.class, true);
    }

    public Enumeration lazyJars() {
        return new RsrcEnumeration(Reference.class, false);
    }

    public Enumeration lazyNativelibs() {
        return new RsrcEnumeration(NativelibReference.class, false);
    }

    public Enumeration jars() {
        return new RsrcEnumeration(Reference.class);
    }

    public Enumeration nativelibs() {
        return new RsrcEnumeration(NativelibReference.class);
    }


    public static String[] parseKeys(String keys) {
        if (keys == null || keys.length() == 0) {
            return new String[0];
        }

        ArrayList list = new ArrayList();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < keys.length(); i++) {
            switch (keys.charAt(i)) {
                case ' ':
                    list.add(sb.toString());
                    sb.setLength(0);
                    break;
                case '\\':
                    if (i++ + 1 < keys.length()) {
                        sb.append(keys.charAt(i));		// skip backslash, add next char
                    }

                    break;
                default:
                    sb.append(keys.charAt(i));
                    break;
            }
        }

        list.add(sb.toString());	// add last key in string

        return (String[]) list.toArray(new String[0]);
    }


    class RsrcEnumeration implements Enumeration {
        Class wanted;
        boolean wantEager;
        boolean wantLazy;

        Enumeration rsrcEnum;


        RsrcEnumeration(Class cls) {
            wanted = cls;
            wantEager = true;
            wantLazy = true;

            createEnum();
        }

        RsrcEnumeration(Class cls, boolean eagerOnly) {
            wanted = cls;
            wantEager = eagerOnly;
            wantLazy = !eagerOnly;

            createEnum();
        }

        public boolean hasMoreElements() {
            return rsrcEnum.hasMoreElements();
        }

        public Object nextElement() {
            return rsrcEnum.nextElement();
        }

        void createEnum() {
            List list = new ArrayList();

            if (wantEager) {
                for (Iterator iter = eagerSet.iterator(); iter.hasNext();) {
                    Object obj = iter.next();
                    boolean isNativelib = (obj instanceof NativelibReference);

                    if (wanted == NativelibReference.class && isNativelib
                     || wanted == Reference.class && !isNativelib) {
                        list.add(obj);
                    }
                }
            }

            if (wantLazy) {
                for (Iterator iter = lazySet.iterator(); iter.hasNext();) {
                    Object obj = iter.next();
                    boolean isNativelib = (obj instanceof NativelibReference);

                    if (wanted == NativelibReference.class && isNativelib
                     || wanted == Reference.class && ! isNativelib) {
                        list.add(obj);
                    }
                }
            }

            rsrcEnum = Collections.enumeration(list);
        }
    }
}
