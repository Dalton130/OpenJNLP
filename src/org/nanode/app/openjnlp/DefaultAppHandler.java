/*
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/ 
 *
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights and
 * limitations under the License.
 *
 * The Original Code is nanode.org code.
 *
 * The Initial Developer of the Original Code is Nanode LLC. Portions created by Nanode are
 * Copyright (C) 2001-2002 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 *    Christopher Heiny <heiny@eznet.net>
 */
package org.nanode.app.openjnlp;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import org.nanode.app.openjnlp.desktop.Gooey;
import org.nanode.jnlp.JNLPParser;
import org.nanode.launcher.Launcher;
import org.nanode.launcher.Reference;
import org.nanode.launcher.Resources;
import org.nanode.launcher.cache.Cache;
import org.nanode.launcher.cache.CacheEntry;


/**
 * Provides default behavior for application integration with an operating system. The default
 * handler makes no assumptions about the underlying operating system and does a best-attempt
 * at implementing the various application functionality in a platform-neutral manner.
 *
 * @author Kevin Herrboldt (kevin@puppethead.com)
 * @author Christopher Heiny (heiny@eznet.net)
 */
public class DefaultAppHandler implements AppHandler {
    /** Singleton instance of the default app handler to be used unless overridden by a platform-specific handler. */
    private static AppHandler appHandler = new DefaultAppHandler();
    
    /** Controls verbosity of output (both normal and error output) for app handlers. */
    protected static boolean verbose = false;


    /** The primary (default) cache used for caching things. */
    private Cache primaryCache;


    /**
     * Creates a default handler using the default cache.
     *
     * @see     Cache#getDefaultCache
     */
    public DefaultAppHandler() {
        primaryCache = Cache.getDefaultCache();
    }

    /**
     * Returns the primary cache to use for caching things via the launcher.
     *
     * @return  the primary cache
     */
    public Cache getPrimaryCache() {
        return primaryCache;
    }

    /**
     * Called when an "About&#8230;" action is called. This app handler does nothing for this action.
     */
    public void handleAbout() {
    }

    /**
     * Called when the application is opened in interactive mode. This app handler brings up the
     * OpenJNLP user interface.
     *
     * @see     Gooey
     */
    public void handleOpenApplication() {
        Gooey.getInstance().toFront();
    }

    /**
     * Processes the opening of a file via the launcher. This app handler converts the file to a
     * url and then calls <code>handleOpenURL</code>.
     *
     * @param   f  the file to open with the launcher
     * @see     #handleOpenURL
     */
    public void handleOpenFile ( File f ) {
        try {
            handleOpenURL ( f.toURL() );
        } catch ( java.net.MalformedURLException e ) {
            System.err.println("Unable to launch " + f + " - " + e.getMessage() );

            if ( verbose ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes the opening of a url via the launcher. This app handler will treat the url as a
     * url to a JNLP descriptor that is parsed and executed.
     * <p>
     * The url is assumed to point to a JNLP descriptor which is parsed into the primary cache.
     * If parsing is successful than all eager resources are brought up to date and finally the
     * launcher is called to launch the app in separate JVM.
     *
     * @param   url  the url to a JNLP descriptor to be launched
     * @see     #getPrimaryCache
     * @see     Launcher#launchExternal
     */
    public void handleOpenURL(URL url) {
        if (url == null) {
            return;
        }

        try {
            CacheEntry entry = JNLPParser.parseDescriptor(getPrimaryCache(), url);

            // make sure eager jars are up-to-date
            for (Enumeration enum = entry.getDescriptor().getResources().eagerJars(); enum.hasMoreElements();) {
                Reference jarRef = (Reference) enum.nextElement();

                entry.addResource(jarRef);
            }

            // make sure eager nativelibs are up-to-date
            for (Enumeration enum = entry.getDescriptor().getResources().eagerNativelibs(); enum.hasMoreElements();) {
                Reference libRef = (Reference) enum.nextElement();

                entry.addResource(libRef);
            }

            Launcher.launchExternal(entry.getDescriptor());
        } catch (Exception e) {
            System.err.println("Launching failed: " + url);

            if (verbose) {
                System.err.println(e);
            }
        }
    }

    /**
     * Called when the application should terminate. This app handler calls <code>System.exit</code>
     * to terminate the JVM.
     */
    public void handleQuit() {
        System.exit(0);
    }


    /**
     * Returns the installed app handler. This is a factory method that returns a single app handler
     * object regardless of the number of times called. Only after a call to <code>setAppHandler</code>
     * will the app handler object change.
     *
     * @return  the currently-installed app handler
     */
    public static synchronized final AppHandler getAppHandler() {
        return appHandler;
    }

    /**
     * Sets the installed app handler to the specified app handler. This is used to override the
     * default app handler with a platform-specific app handler.
     *
     * @param   handler  the new app handler to install
     */
    public static synchronized final void setAppHandler(AppHandler handler) {
        appHandler = handler;
    }
}
