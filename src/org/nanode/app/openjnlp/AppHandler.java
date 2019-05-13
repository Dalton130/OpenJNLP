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
 * Copyright (C) 2001 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 *    Christopher Heiny <heiny@eznet.net>
 */
package org.nanode.app.openjnlp;

import java.io.File;
import java.net.URL;

import org.nanode.launcher.cache.Cache;

/**
 * Describes a handler that is used to provide typical operations often used by operating systems
 * running applications. The purpose of an app handler is to integrate with the current operating
 * system in as complete a manner as is possible. This interface allows the creation of specific
 * app handlers that can be installed at runtime to provide an enhanced user experience.
 *
 * @author Kevin Herrboldt (kevin@puppethead.com)
 * @author Christopher Heiny (heiny@eznet.net)
 */
public interface AppHandler {
    /**
     * Returns the primary cache to use for caching things via the launcher.
     *
     * @return  the primary cache
     */
    public abstract Cache getPrimaryCache();

    /**
     * Called when an "About&#8230;" action is called. Typically used to provide some sort of
     * information about the app that is presented to the user.
     */
    public abstract void handleAbout();

    /**
     * Called when the application is opened in interactive mode. Can be used to invoke the main
     * graphical user interface, for example.
     */
    public abstract void handleOpenApplication();

    /**
     * Processes the opening of a file.
     *
     * @param   f  the file to open
     */
    public abstract void handleOpenFile(File f);

    /**
     * Processes the opening of a url.
     *
     * @param   url  the url to open
     */
    public abstract void handleOpenURL(URL url);

    /**
     * Called when the application should terminate.
     */
    public abstract void handleQuit();
}
