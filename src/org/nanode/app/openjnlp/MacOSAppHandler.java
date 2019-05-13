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

import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJOpenApplicationHandler;
import com.apple.mrj.MRJOpenDocumentHandler;
import com.apple.mrj.MRJQuitHandler;

import org.nanode.launcher.cache.Cache;


/**
 * Provides an app handler that registers itself with Mac OS and can delegate to another app handler.
 * <p>
 * Apple Computer provides a mechanism for Java applications to register with Mac OS to handle various
 * events such as opening documents and quitting. An app handler of this type registers itself with
 * Mac OS to handle these operating system events. If this app handler is used on a non-Mac OS system
 * it will not attempt to register itself and will behave like a <code>DefaultAppHandler</code>.
 * <p>
 * Another important function this type of app handler provides is a delegating app handler. By being
 * a delegating app handler, this app handler is the only type that needs to register with Mac OS. Any
 * extended functionality (such as displaying an about dialog) that is desired can be implemented as
 * a standard app handler and set as the delegate, freeing the implementation from needing to worry
 * about interfacing with Mac OS.
 *
 * @author Kevin Herrboldt (kevin@puppethead.com)
 * @author Christopher Heiny (heiny@eznet.net)
 */
public final class MacOSAppHandler extends DefaultAppHandler implements AppHandler,
                                                                        MRJAboutHandler,
                                                                        MRJOpenApplicationHandler,
                                                                        MRJOpenDocumentHandler,
                                                                        MRJQuitHandler {

    /** The delegated app handler. */
    private AppHandler delegate;


    /**
     * Creates a delegating app handler and registers it with Mac OS if possible.
     *
     * @see     MRJApplicationUtils
     */
    public MacOSAppHandler() {
        super();

        try {
            // if actually running on Mac OS, register all the handlers
            if (MRJApplicationUtils.isMRJToolkitAvailable()) {
                MRJApplicationUtils.registerAboutHandler(this);
                MRJApplicationUtils.registerOpenApplicationHandler(this);
                MRJApplicationUtils.registerOpenDocumentHandler(this);
                MRJApplicationUtils.registerQuitHandler(this);
            }
        } catch (Exception e) {
            System.err.println("Failed to register app handler with Mac OS");

            if (DefaultAppHandler.verbose) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the primary cache from the delegate if set, otherwise from the default app handler.
     *
     * @return   the primary cache
     */
    public Cache getPrimaryCache() {
        return (delegate != null) ? delegate.getPrimaryCache() : super.getPrimaryCache();
    }

    /**
     * Called by Mac OS about handler when a user selects the "About&#8230;" menu item.
     */
    public void handleAbout() {
        if (delegate != null) {
            delegate.handleAbout();
        } else {
            super.handleAbout();
        }
    }

    /**
     * Called by Mac OS when the app is launched directly instead of by opening a document.
     */
    public void handleOpenApplication() {
        if (delegate != null) {
            delegate.handleOpenApplication();
        } else {
            super.handleOpenApplication();
        }
    }

    /**
     * Called by Mac OS when a document that this app handles is opened. This will cause the app
     * to be launched if it is not already running or will just be called if the app is already
     * running.
     *
     * @param   f  the file to open
     */
    public void handleOpenFile(File f) {
        if (delegate != null) {
            delegate.handleOpenFile(f);
        } else {
            super.handleOpenFile(f);
        }
    }

    /**
     * Opens the url via the delegate app handler or default app handler if delegate not set.
     */
    public void handleOpenURL(URL url) {
        if (delegate != null) {
            delegate.handleOpenURL(url);
        } else {
            super.handleOpenURL(url);
        }
    }

    /**
     * Called by Mac OS when the user selects the "Quit" menu item.
     */
    public void handleQuit() {
        if (delegate != null) {
            delegate.handleQuit();
        } else {
            super.handleQuit();
        }
    }

    /**
     * Sets a delegate app handler. The purpose of this is to allow the installation and invocation of
     * an app handler that provides a graphical user interface that is consistent with the Mac OS
     * platform, although any type of app handler can be set.
     *
     * @param   handler  the app handler to delegate to
     */
    public void setDelegateAppHandler(AppHandler handler) {
        delegate = handler;
    }


    /**
     * Provides a convenient way to set up a Mac OS-specific app handler and install it so it is invoked
     * by the app.
     */
    public static final void setAppHandler() {
        DefaultAppHandler.setAppHandler(new MacOSAppHandler());
    }
}
