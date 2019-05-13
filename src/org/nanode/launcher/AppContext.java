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
 *    Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 */
package org.nanode.launcher;

import java.lang.reflect.Method;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.nanode.launcher.cache.CacheEntry;


class AppContext implements Runnable {
    private final Descriptor descriptor;
    private final Process process;

    private final CacheEntry appEntry;
    private ClassLoader appLoader;


    AppContext(Descriptor des, Process proc) {
        descriptor = des;
        process = proc;

        appEntry = descriptor.getCacheEntry();
        //appLoader = appEntry.createClassLoader();
    }

    public void run() {
        if (process == null) {
            execApp();
        } else {
            watchProcess();
        }
    }

    CacheEntry getAppEntry() {
        return appEntry;
    }

    ClassLoader getAppLoader() {
        // only create classloader if asked for and not defined
        if (appLoader == null) {
            synchronized (this) {
                if (appLoader == null) {
                    appLoader = appEntry.createClassLoader();
                }
            }
        }

        return appLoader;
    }

    public Descriptor getDescriptor() {
        return descriptor;
    }

    private void execApp() {
        if (descriptor == null) {
            return;
        }

        if (descriptor.getResources().getProperties() != null) {
            Properties props = descriptor.getResources().getProperties();

            for (Enumeration enum = props.propertyNames(); enum.hasMoreElements();) {
                String name = (String) enum.nextElement();

                System.setProperty(name, props.getProperty(name));
            }
        }

        if (descriptor instanceof ApplicationDescriptor) {
            ApplicationDescriptor appDesc = (ApplicationDescriptor) descriptor;

            if (!appDesc.isAppletDescriptor()) {
                String mainClassName = appDesc.getMainClass();

                // if no main-class specified, check the manifest of the main jar
                if (mainClassName == null) {
                    Manifest man = appEntry.getJarManifest(appDesc.getResources().getMainJar());

                    if (man != null) {
                        Attributes att = man.getMainAttributes();
                        mainClassName = (att != null) ? att.getValue(Attributes.Name.MAIN_CLASS) : null;
                    }
                }

                // run application
                try {
                    Class mc = appLoader.loadClass(mainClassName);

                    Class[] types = { Class.forName("[Ljava.lang.String;") };
                    Method mainMethod = mc.getDeclaredMethod("main", types);
                    Object[] parms = new Object[1];
                    parms[0] = appDesc.getArguments();

                    mainMethod.invoke(null, parms);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // run applet
                try {
                    AppletRunner ar = new AppletRunner(appDesc, appLoader);

                    ar.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void watchProcess() {
        StreamEater stdin = new StreamEater(process.getInputStream(), false);
        StreamEater stderr = new StreamEater(process.getErrorStream(), true);

        new Thread(stdin, "stdin").start();
        new Thread(stderr, "stderr").start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
        }

        stdin.finish();
        stderr.finish();
    }


    private class StreamEater implements Runnable {
        BufferedReader input;
        boolean isErrStream;
        boolean finished;


        StreamEater(InputStream is, boolean isErr) {
            if (is != null) {
                input = new BufferedReader(new InputStreamReader(is));
            }

            isErrStream = isErr;
            finished = false;
        }

        public void run() {
            if (input == null) {
                return;
            }

            try {
                String line;

                while (!finished && (line = input.readLine()) != null) {
                    if (isErrStream) {
                        Launcher.logErr(descriptor, line);
                    } else {
                        Launcher.logOut(descriptor, line);
                    }
                }
            } catch (IOException e) {
            }
        }

        void finish() {
            finished = true;
        }
    }
}
