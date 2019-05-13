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
 * Copyright (C) 2002 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 */
package org.nanode.launcher.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.nanode.launcher.NativelibReference;
import org.nanode.launcher.Reference;


/**
 * A file system implementation of a cached resource. This provides the means to read and
 * write cached data to the local file system for persistence. A cached resource is stored
 * in the specified resource dir.
 * <p>
 * This also will unjar native libraries if the reference is to a native library jar and a
 * library dir is specified.
 *
 * @author Kevin Herrboldt (kevin@nanode.org)
 */
public class FileCachedResource extends CachedResource {
    /** where resources are stored */
    private File rsrcDir;

    /** where native libs are extracted to */
    private File libsDir;

    /** the cached resource file */
    private File cacheFile;


    /**
     * Creates a cached resource in the resource dir for the specified reference.
     * <p>
     * The resource directory specified is where the cached bytes will be stored, using
     * the resource cache name. Specifying the resource directory as <code>null</code>
     * effectively prevents the resource from being cached, making it inaccessible.
     * <p>
     * If the referenced resource is a native library reference and the native library
     * directory specified is non-null, native libraries will be extracted into the
     * native library directory every time the resource is updated. A <code>null</code>
     * library directory will inhibit the extraction process but the resource will
     * still be cached.
     *
     * @param   ref  the reference to cache
     * @param   rsrc  the directory in which to cache the reference
     * @param   libs  the directory in which to extract native libraries
     * @see #resourceCacheName
     */
    public FileCachedResource(Reference ref, File rsrc, File libs) {
        super(ref);

        // if the library dir is specified, make sure it exists
        if ((libsDir = libs) != null) {
            libsDir.mkdirs();
        }
        
        // if the resource dir is specified, make sure it exists
        if ((rsrcDir = rsrc) != null) {
            rsrcDir.mkdirs();

            cacheFile = new File(rsrcDir, resourceCacheName());
        }

        if (cacheFile != null) {
            lastModified = cacheFile.lastModified();
            actualLength = cacheFile.length();
        }
    }

    /**
     * Creates a cached resource in the resource dir for the specified reference with the
     * specified last-modified date. This is used primarily when instantiating the cache
     * entry from the file system.
     *
     * @param   ref      the reference to cache
     * @param   lastMod  the last-modified date of this cached resource
     * @param   rsrc     the directory in which to cache the reference
     * @param   libs     the directory in which to extract native libraries
     */
    public FileCachedResource(Reference ref, long lastMod, File rsrc, File libs) {
        this(ref, rsrc, libs);

        lastModified = lastMod;

        if (cacheFile != null) {
            cacheFile.setLastModified(lastModified);
        }
    }

    /**
     * Returns the file reference to this cached resource.
     *
     * @return  file reference to cached resource
     */
    public File getCacheFile() {
        return cacheFile;
    }

    /**
     * Returns the directory that contains cached native libraries, if any.
     *
     * @return  native library directory for cached resource
     */
    public File getLibraryDir() {
        return libsDir;
    }

    /**
     * Returns the directory that contains the cached resource.
     *
     * @return  directory that contains the cached resource
     */
    public File getResourceDir() {
        return rsrcDir;
    }

    /**
     * Updates the cached resource if necessary.
     * <p>
     * If this cached resource is a native library jar, any native libraries at the root level
     * in the jar are extracted into the cache entry library directory.
     *
     * @return  <code>true</code> if resource is updated; <code>false</code> otherwise
     */
    public boolean update() {
        boolean status = super.update();

        // if not updated, nothing more to do
        if (!status) {
            return status;
        }

        getCacheFile().setLastModified(getLastModified());	// update last modified date on file

        // if this is a nativelib, extract the files at the root level of the jar
        if (getCacheFile() !=null && getLibraryDir() != null && reference instanceof NativelibReference) {
            try {
                ZipFile zf = new ZipFile(getCacheFile());

                for (Enumeration entries = zf.entries(); entries.hasMoreElements();) {
                    ZipEntry ze = (ZipEntry) entries.nextElement();

                    if (ze.isDirectory() || ze.getName().indexOf('/') > 0) {
                        continue;
                    }

                    InputStream is = zf.getInputStream(ze);
                    OutputStream os = new FileOutputStream(new File(getLibraryDir(), ze.getName()));
                    CachedResource.copy(is, os);
                    os.close();
                    is.close();
                }

                zf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return status;
    }
    
    /**
     * Converts the referenced resource into a name to be used relative to the resource
     * directory for storing the cached resource.
     *
     * @return  the cache name for this resource
     */
    public String resourceCacheName() {
        String path = reference.getURL().getPath();

        return path.substring(path.lastIndexOf("/"));
    }

    /**
     * Returns a new input stream to the file that this cached resource represents.
     *
     * @return  input stream to this cached resource file
     */
    public InputStream openCacheInputStream() {
        InputStream is = null;

        // create input stream from cached file
        try {
            is = new FileInputStream(getCacheFile());
        } catch (Exception e) {
            throw new CacheException("Error creating input stream from cache");
        }

        return is;
    }

    /**
     * Returns a new output stream to the file that this cached resource represents.
     *
     * @return  output stream to this cached resource file
     */
    protected OutputStream openCacheOutputStream() {
        OutputStream os = null;

        // create output stream to cached file
        try {
            os = new FileOutputStream(getCacheFile());
        } catch (Exception e) {
            throw new CacheException("Error creating output stream to cache");
        }

        return os;
    }

    /**
     * Purges the resource from the cache. This is done by removing the file in the file cache
     * entry resources directory.
     * <p>
     * If this cached resource is a native library jar, any extracted native libraries are
     * removed from the cache entry library directory.
     */
    protected void purge() {
        super.purge();

        // if this is a nativelib, delete any extracted files from the library directory
        if (getCacheFile() != null && getLibraryDir() != null && reference instanceof NativelibReference) {
            try {
                ZipFile zf = new ZipFile(getCacheFile());

                for (Enumeration entries = zf.entries(); entries.hasMoreElements();) {
                    ZipEntry ze = (ZipEntry) entries.nextElement();

                    if (ze.isDirectory() || ze.getName().indexOf('/') > 0) {
                        continue;
                    }

                    new File(getLibraryDir(), ze.getName()).delete();
                }

                zf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (getCacheFile() != null) {
            getCacheFile().delete();
        }
    }
}
