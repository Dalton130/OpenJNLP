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
package org.nanode.launcher.cache;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;

import org.nanode.launcher.Gestalt;
import org.nanode.launcher.NativelibReference;
import org.nanode.launcher.Reference;


/**
 * This is an implementation of a secure ClassLoader that will load
 * classes and resources from jars within the file-based cache.
 *
 * @author Kevin Herrboldt (kevin@puppethead.com)
 * @author Christopher Heiny (heiny@eznet.net)
 */
class FileCacheClassLoader extends URLClassLoader {
    FileCacheEntry cacheEntry;

    Thread lazyThread;
    LazyLoader lazyLoader;


    FileCacheClassLoader(FileCacheEntry entry, URL[] classpath) {
        super(classpath);

        cacheEntry = entry;

        // get the lazy jars, if any
        Reference[] lazyJars = null;
        NativelibReference[] lazyLibs = null;
        ArrayList lazy = new ArrayList();

        // gather lazy jars if they exist
        for (Enumeration enum = cacheEntry.getDescriptor().getResources().lazyJars(); enum.hasMoreElements();) {
            lazy.add(enum.nextElement());
        }

        if (lazy.size() > 0) {
            lazyJars = (Reference[]) lazy.toArray(new Reference[0]);
            lazy.clear();
        }

        // gather lazy nativelibs if they exist
        for (Enumeration enum = cacheEntry.getDescriptor().getResources().lazyNativelibs(); enum.hasMoreElements();) {
            lazy.add(enum.nextElement());
        }

        if (lazy.size() > 0) {
            lazyLibs = (NativelibReference[]) lazy.toArray(new NativelibReference[0]);
        }

        // if there's lazy stuff, start the lazy loader in a new thread
        if (lazyJars != null || lazyLibs != null) {
            lazyLoader = new LazyLoader(lazyJars, lazyLibs);
            lazyThread = new Thread(lazyLoader);
            lazyThread.start();
        }
    }

    /**
     * Finds and loads the class with the specified name from the class path. Any lazy jars
     * in the class path will be added as needed until the class is found.
     *
     * @param   name  the name of the class
     * @return  the resulting class
     * @throws  ClassNotFoundException if the class could not be found
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        Class wanted = null;
        String errMsg = "";

        // keep trying to load class until all lazy jars are checked
        do {
            try {
                wanted = super.findClass(name);
            } catch (ClassNotFoundException e) {
                errMsg = e.getMessage();
            }

            if (wanted == null && (lazyLoader != null && lazyLoader.hasMoreJars())) {
                synchronized (lazyLoader) {
                    if (lazyLoader.hasMoreJars()) {
                        System.out.println("waiting on lazy jar");

                        try {
                            lazyLoader.wait();
                        } catch (InterruptedException e) { }
                    }
                }
            }
        } while (wanted == null &&  (lazyLoader != null && lazyLoader.hasMoreJars()));

        if (wanted == null) {
            throw new ClassNotFoundException(errMsg);
        }

        return wanted;
    }

    /**
     * Returns path to native library within cache entry. This transforms the library name into
     * the appropriate form for the current platform.
     *
     * @param   libname  native library name
     * @return  path to the native library appropriate for this platform
     */
    protected String findLibrary(String libname) {
        File f = new File(cacheEntry.getLibraryDir(), System.mapLibraryName(libname));

        // wait while can't read lib and lazy libs left to download
        if ( lazyLoader != null ) while (!f.canRead() && lazyLoader.hasMoreLibs()) {
            synchronized (lazyLoader) {
                if (lazyLoader.hasMoreLibs()) {
                    System.out.println("waiting on lazy libs");

                    try {
                        lazyLoader.wait();
                    } catch (InterruptedException e) { }
                }
            }
        }

        return (f.canRead() ? f.getAbsolutePath() : null);
    }

    /**
     * Finds the resource with the specified name from the class path. Any lazy jars
     * in the class path will be added as needed until the resource is found.
     *
     * @param   name  the name of the resource
     * @return  a URL to the resource or <code>null</code> if the resource could not found
     */
    public URL findResource(String name) {
        URL wanted = null;

        // keep trying to locate resource until all lazy jars are checked
        do {
            wanted = super.findResource(name);

            if (wanted == null && ( lazyLoader != null && lazyLoader.hasMoreJars())) {
                synchronized (lazyLoader) {
                    if (lazyLoader.hasMoreJars()) {
                        System.out.println("waiting on lazy jar");

                        try {
                            lazyLoader.wait();
                        } catch (InterruptedException e) { }
                    }
                }
            }
        } while (wanted == null && ( lazyLoader != null && lazyLoader.hasMoreJars() ));

        return wanted;
    }

    /**
     * Returns an enumerations of URLs to all resources in the class path with the given name.
     * All lazy jars are updated before returning the enumeration.
     *
     * @param   name  the name of the resource
     * @return  an enumeration of URLs for the resource(s)
     * @throws  IOException if an I/O error occurs
     */
    public Enumeration findResources(String name) throws IOException {
        // wait until all lazy jars are updated
        if ( lazyLoader != null ) while (lazyLoader.hasMoreJars()) {
            synchronized (lazyLoader) {
                if (lazyLoader.hasMoreJars()) {
                    try {
                        lazyLoader.wait();
                    } catch (InterruptedException e) { }
                }
            }
        }

        return super.findResources(name);
    }
    

    static FileCacheClassLoader createClassLoader(FileCacheEntry entry) {
        if (entry == null || entry.getDescriptor() == null) {
            return null;
        }

        /* a URLClassLoader can't be instantiated with an empty classpath, so the
           classpath needs to be primed with the eager jars. */
        ArrayList eager = new ArrayList();
        File jarFile;
        URL jarURL;

        // create classpath of URLs to eager jars in cache
        for (Enumeration enum = entry.getDescriptor().getResources().eagerJars(); enum.hasMoreElements();) {
            jarURL = ((Reference) enum.nextElement()).getURL();
            jarFile = new File(entry.getResourceDir(), FileCache.cacheName(jarURL));

            try {
                eager.add(jarFile.toURL());
                //eager.add(FileCache.toSafeURL(jarFile));
            } catch (MalformedURLException e) {
                System.err.println(e);
            }
        }

        URL[] classpath = new URL[eager.size()];
        classpath = (URL[]) eager.toArray((Object[]) classpath);

        return new FileCacheClassLoader(entry, classpath);
    }


    private class LazyLoader implements Runnable {
        Reference[] lazyJars;
        Reference[] lazyLibs;
        int jarsLeft;
        int libsLeft;


        private LazyLoader(Reference[] jars, NativelibReference[] libs) {
            lazyJars = jars;
            jarsLeft = (lazyJars != null) ? lazyJars.length : 0;

            lazyLibs = libs;
            libsLeft = (lazyLibs != null) ? lazyLibs.length : 0;
        }

        public void run() {
            File resFile;

            // if there are lazy jars, update them
            if (lazyJars != null) {
                for (int i = 0; i < lazyJars.length; i++) {
                    resFile = updateResource(lazyJars[i]);
                    --jarsLeft;

                    System.out.println("lazy resource " + resFile.getName() + " updated");

                    try {
                        addURL(resFile.toURL());
                    } catch (MalformedURLException e) {
                        System.err.println(e);
                    }

                    synchronized (this) {
                        this.notifyAll();
                    }
                }
            }

            // if there are lazy libs, update them
            if (lazyLibs != null) {
                for (int i = 0; i < lazyLibs.length; i++) {
                    resFile = updateResource(lazyLibs[i]);
                    --libsLeft;

                    System.out.println("lazy resource " + resFile.getName() + " updated");

                    synchronized (this) {
                        this.notifyAll();
                    }
                }
            }
        }

        private boolean hasMoreJars() {
            return (jarsLeft > 0);
        }

        private boolean hasMoreLibs() {
            return (libsLeft > 0);
        }

        /**
         * Updates resource in cache.
         *
         * @param   ref  reference to the resource
         * @return  the file in the cache of the updated resource
         */
        private File updateResource(Reference ref) {
            cacheEntry.addResource(ref);
            CachedResource cr = cacheEntry.getResource(ref, true);

            return new File(cacheEntry.getResourceDir(), FileCache.cacheName(cr.getReference().getURL()));
        }
    }
}
