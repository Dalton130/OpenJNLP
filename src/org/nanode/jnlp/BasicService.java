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
package org.nanode.jnlp;

import java.net.URL;


/**
 * Implements the JNLP BasicService which provides a set of methods for querying and interacting
 * with the environment, similar to what the <code>AppletContext</code> provides a Java applet.
 *
 * @author Kevin Herrboldt (kevin@nanode.org)
 */
public class BasicService implements javax.jnlp.BasicService {
    /**
     * Initializes the basic service for a given app.
     */
    protected BasicService() {
    }

    /**
     * Returns the codebase for the app. The codebase is either specified explicitly in the
     * JNLP file or is the location of the jar file containing the main class of the app.
     *
     * @return  a URL to the codebase of the app
     */
    public URL getCodeBase() {
        return null;
    }

    /**
     * Indicates if the app is running offline. Returns the "best guess" of the offline state
     * of app.
     *
     * @return  <code>true</code> if the app is running offline; <code>false</code> otherwise
     */
    public boolean isOffline() {
        return false;
    }

    /**
     * Indicates if a web browser is supported on the current platform and by the JNLP client.
     * If a web browser is not supported than <code>showDocument</code> will always return false.
     *
     * @return  <code>true</code> if a web browser is supported; <code>false</code> otherwise
     * @see     #showDocument
     */
    public boolean isWebBrowserSupported() {
        return false;
    }

    /**
     * Opens the specified URL in a web browser, if a web browser is supported. May cause a
     * web browser application to be invoked based on system configuration. If no web browser
     * is supported this will do nothing.
     *
     * @param   url  a URL to be opened in a web browser
     * @return  <code>true</code> if URL was successfully passed to a web browser;
     *          <code>false</code> otherwise
     * @see     #isWebBrowserSupported
     */
    public boolean showDocument(URL url) {
        return false;
    }
}
