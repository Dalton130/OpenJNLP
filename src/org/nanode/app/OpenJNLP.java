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
 *    Christopher Heiny <heiny@eznet.net>
 */
package org.nanode.app;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.nanode.app.openjnlp.*;
import org.nanode.app.openjnlp.desktop.Gooey;
import org.nanode.jnlp.JNLPParser;
import org.nanode.launcher.Gestalt;
import org.nanode.launcher.Version;

/**
 * This is the OpenJNLP application. It is the main entry point for the OpenJNLP application which
 * sets up integration with the current operating system and handles any initial arguments. Will
 * invoke the user interface if run in interactive mode.
 *
 * @author Kevin Herrboldt (kevin@puppethead.com)
 * @author Christopher Heiny (heiny@eznet.net)
 */
public class OpenJNLP {
    /** current version of OpenJNLP **/
    private static final Version version;

    static {
        Properties props = new Properties();
        InputStream is = OpenJNLP.class.getResourceAsStream("/lib/openjnlp.properties");

        try {
            props.load(is);
            is.close();
        } catch (Exception e) {
        }

        version = new Version(props.getProperty("openjnlp.version", "exp"));
    }

    /**
     * Returns the version of the OpenJNLP application.
     *
     * @return  current version of the OpenJNLP application
     * @see Version
     */
    public static Version getVersion() {
        return version;
    }

    /**
     * Entry point (main method) of the OpenJNLP application. This method will process any
     * command-line arguments and then invoke the user interface if appropriate.
     *
     * @param   args  array of command-line arguments
     */
    public static void main(String[] args) {
        if (Gestalt.osType() == Gestalt.OSTYPE_MACOS) {
            MacOSAppHandler.setAppHandler();
        }

        try {
            // this sets the user agent for http to OpenJNLP/version
            System.getProperties().put("http.agent", "OpenJNLP/" + getVersion());
        } catch (Throwable t) {
            // ignore, just in case there's a security violation
        }

        // skip all arguments that start with "-" (kludge around a Mac OS X 10.1 issue)
        int optind = -1;

        if (args != null) {
            for (optind = 0; optind < args.length; optind++) {
                if (args[optind].charAt(0) != '-') {
                    break;
                }
            }

            if (optind == args.length) {
                optind = -1;
            }
        }

        if (optind == -1) {
            Gooey.getInstance().go();
        } else {
            AppHandler appHandler = DefaultAppHandler.getAppHandler();

            for (int i = optind; i < args.length; i++) {
                URL url = null;

                try {
                    url = new URL(args[i]);
                } catch (MalformedURLException e) {
                }

                if (url != null) {
                    appHandler.handleOpenURL(url);
                } else {
                    appHandler.handleOpenFile(new File(args[i]));
                }
            }
        }

        System.exit(0);
    }
}
