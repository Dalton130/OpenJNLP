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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nanode.launcher.cache.Cache;


/**
 * A descriptor for an application or an applet.
 * <p>
 * An application is defined as a Java application which is invoked by calling the
 * <code>public void main(String[])</code> method of the main class.
 * <p>
 * A Java applet is invoked by setting up an applet context and then calling the
 * <code>init</code> and <code>start</code> methods of the applet main class.
 *
 * @author Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 */
public class ApplicationDescriptor extends Descriptor {
    /** the name of the main class to call for this application or applet */
    protected String mainClass;

    /** arguments that should be passed to the main class of an application*/
    protected List arguments;

    /** the name of the applet */
    protected String appletName;

    /** the width of the applet */
    protected int appletWidth;

    /** the height of the applet */
    protected int appletHeight;

    /** the document base for the applet */
    protected URL docBase;
    
    /** params for the applet as (name, value) pairs */
    protected Map params;
    

    /**
     * Creates descriptor for an application.
     *
     * @param  cache      the cache this descriptor should be added to
     * @param  base       the codebase for URLs related to this descriptor
     * @param  src        the source of this descriptor
     * @param  mainclass  the name of the main class
     */
    public ApplicationDescriptor(Cache cache, URL base, Reference src, String mainclass) {
        super(cache, base, src);

        mainClass = mainclass;

        arguments = Collections.synchronizedList(new ArrayList());
        params = null;
    }


    /**
     * Creates descriptor for an applet.
     *
     * @param  cache      the cache this descriptor should be added to
     * @param  base       the codebase for URLs related to this descriptor
     * @param  src        the source of this descriptor
     * @param  mainclass  the name of the main class
     * @param  name       the name of the applet
     * @param  width      width of applet in pixels
     * @param  height     height of applet in pixels
     * @param  docbase    the document base of the applet
     */
    public ApplicationDescriptor(Cache cache, URL base, Reference src, String mainclass,
                                 String name, int width, int height, URL docbase) {
        super(cache, base, src);

        mainClass = mainclass;
        appletName = name;
        appletWidth = width;
        appletHeight = height;
        docBase = docbase;

        params = Collections.synchronizedMap(new HashMap());
        arguments = null;
    }

    /**
     * Returns indication of whether this descriptor is for an applet.
     *
     * @return  <code>true</code> if this is a descriptor for an applet; <code>false</code> otherwise
     */
    public boolean isAppletDescriptor() {
        return (params != null);
    }
    
    /**
     * Adds an argument to list of arguments that will be passed to application at invocation.
     * If <code>arg</code> is null it will not be added to the list.
     *
     * @param  arg  argument to be added
     * @throw  IllegalStateException if this is not a descriptor for an application
     */
    public void addArgument(String arg) {
        if (arguments == null) {
            throw new IllegalStateException("applet descriptor does not support arguments");
        }

        if (arg != null) {
            arguments.add(arg);
        }
    }

    /**
     * Returns the list of arguments that should be passed into the <code>static public(String[])</code>
     * method of the main class. The list will never be null, but can be zero in length.
     *
     * @return array of arguments
     * @throw  IllegalStateException if this is not a descriptor for an application
     */
    public String[] getArguments() {
        if (arguments == null) {
            throw new IllegalStateException("applet descriptor does not support arguments");
        }

        return (String[]) arguments.toArray(new String[0]);
    }

    /**
     * Returns the value of the applet param specified.
     *
     * @param   name  name of param
     * @return  value of param; <code>null</code> if not set
     * @throw   IllegalStateException if this is not a descriptor for an applet
     */
    public String getParam(String name) {
        if (params == null) {
            throw new IllegalStateException("application descriptor does not support params");
        }

        return (String) params.get(name);
    }
    
    /**
     * Adds a param to map of params that are available to the applet after invokation.
     * If <code>name</code> is null it will not be added to the map.
     *
     * @param   name   name of param
     * @param   value  value of param
     * @throw   IllegalStateException if this is not a descriptor for an applet
     */
    public void putParam(String name, String value) {
        if (params == null) {
            throw new IllegalStateException("application descriptor does not support params");
        }

        if (name != null) {
            params.put(name, value);
        }
    }
    
    /**
     * Returns the main class name used to invoke this application.
     *
     * @return  name of main class
     */
    public String getMainClass() {
        return mainClass;
    }

    /**
     * Returns the name of this applet
     *
     * @return  applet name
     * @throw   IllegalStateException if this is not a descriptor for an applet
     */
    public String getAppletName() {
        if (!isAppletDescriptor()) {
            throw new IllegalStateException("application descriptor does not support name");
        }

        return appletName;
    }

    /**
     * Returns the codebase for this app. If this is an applet and the descriptor codebase is <code>null</code>,
     * the URL of the main jar is returned.
     *
     * @return  the codebase of the descriptor or the URL of the main jar if and only if this descriptor is
     *          for an applet and the descriptor codebase is <code>null</code>
     */
    public URL getCodebase() {
        URL base = super.getCodebase();

        if (isAppletDescriptor() && base == null) {
            base = getResources().getMainJar().getURL();
        }

        return base;
    }
    
    /**
     * Returns the document base of this applet
     *
     * @return  document base of applet
     * @throw   IllegalStateException if this is not a descriptor for an applet
     */
    public URL getDocumentBase() {
        if (!isAppletDescriptor()) {
            throw new IllegalStateException("application descriptor does not support docbase");
        }

        return docBase;
    }
    
    /**
     * Returns the height in pixels of the applet
     *
     * @return  height of applet in pixels
     * @throw   IllegalStateException if this is not a descriptor for an applet
     */
    public int getHeight() {
        if (!isAppletDescriptor()) {
            throw new IllegalStateException("application descriptor does not support size");
        }

        return appletHeight;
    }
    
    /**
     * Returns the width in pixels of the applet
     *
     * @return  width of applet in pixels
     * @throw   IllegalStateException if this is not a descriptor for an applet
     */
    public int getWidth() {
        if (!isAppletDescriptor()) {
            throw new IllegalStateException("application descriptor does not support size");
        }

        return appletWidth;
    }
}
