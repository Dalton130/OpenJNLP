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

import java.io.IOException;
import java.net.URL;

import javax.jnlp.DownloadServiceListener;


/**
 * Provides the JNLP DownloadService to an app for controlling how its own resources are cached,
 * determining which resources are cached, forcing resources to be cached and removing cached
 * resources.
 *
 * @author Kevin Herrboldt (kevin@nanode.org)
 */
public class DownloadService implements javax.jnlp.DownloadService {
    /**
     * Initializes the download service for a given app.
     */
    protected DownloadService() {
    }

    /**
     * Returns a default implementation of a download service listener, which should open and
     * update a progress window when passed a load method.
     *
     * @return  DownloadServiceListener to provide progress window
     */
    public DownloadServiceListener getDefaultProgressWindow() {
        return null;
    }

    /**
     * Returns <code>true</code> if the part referred to by the specified name is cached.
     *
     * @param   part  the name of the part
     * @return  <code>true</code> if the part is cached; <code>false</code> otherwise
     */
    public boolean isPartCached(String part) {
        return false;
    }

    /**
     * Returns <code>true</code> if the parts referred to by the specified names are cached.
     *
     * @param   parts  array of part names
     * @return  <code>true</code> if all of the parts are cached; <code>false</code> otherwise
     */
    public boolean isPartCached(String[] parts) {
        if (parts == null) {
            return false;
        }

        for (int i = 0; i < parts.length; i++) {
            // stop checking as soon as any part isn't cached, return false
            if (!isPartCached(parts[i])) {
                return false;
            }
        }

        return true;		// at this point all parts checked and cached
    }

    /**
     * Returns <code>true</code> if the specified part of the specified extension is cached.
     *
     * @param   ref      the url of the extension resource
     * @param   version  the version id or <code>null</code>
     * @param   part     the name of the part within the extension
     * @return  <code>true</code> if the part of the extension is cached; <code>false</code> otherwise
     */
    public boolean isExtensionPartCached(URL ref, String version, String part) {
        return false;
    }

    /**
     * Returns <code>true</code> if the specified parts of the specified extension are cached.
     *
     * @param   ref      the url of the extension resource
     * @param   version  the version id or <code>null</code>
     * @param   parts    the name of the parts within the extension
     * @return  <code>true</code> if the parts of the extension are cached; <code>false</code> otherwise
     */
    public boolean isExtensionPartCached(URL ref, String version, String[] parts) {
        if (parts == null) {
            return false;
        }

        for (int i = 0; i < parts.length; i++) {
            // stop checking as soon as any part isn't cached, return false
            if (!isExtensionPartCached(ref, version, parts[i])) {
                return false;
            }
        }

        return true;		// at this point all parts checked and cached
    }

    /**
     * Returns <code>true</code> if the resource referred to by the specified URL and version is
     * cached. A version of <code>null</code> refers to a basic (unversioned) resource.
     *
     * @param   ref      the url for the resource
     * @param   version  the version id or <code>null</code>
     * @return  <code>true</code> if the matching resource is cached; <code>false</code> otherwise
     */
    public boolean isResourceCached(URL ref, String version) {
        return false;
    }

    /**
     * Downloads the specified part of the specified extension. This method will block until the
     * download is complete or an exception occurs.
     *
     * @param   ref       the url of the extension resource
     * @param   version   the version id or <code>null</code>
     * @param   part      the name of the part
     * @param   progress  the download progress callback object
     * @throws  IOException if there is an I/O error during download
     */
    public void loadExtensionPart(URL ref, String version, String part, DownloadServiceListener progress) throws IOException {
    }

    /**
     * Downloads the specified parts of the specified extension. This method will block until the
     * download is complete or an exception occurs.
     *
     * @param   ref       the url of the extension resource
     * @param   version   the version id or <code>null</code>
     * @param   parts     array of part names
     * @param   progress  the download progress callback object
     * @throws  IOException if there is an I/O error during download
     */
    public void loadExtensionPart(URL ref, String version, String[] parts, DownloadServiceListener progress) throws IOException {
        if (parts == null) {
            return;
        }

        for (int i = 0; i < parts.length; i++) {
            loadExtensionPart(ref, version, parts[i], progress);
        }
    }

    /**
     * Downloads the specified part. This method will block until the download is complete or an
     * exception occurs.
     *
     * @param   part      the name of the part
     * @param   progress  the download progress callback object
     * @throws  IOException if there is an I/O error during download
     */
    public void loadPart(String part, DownloadServiceListener progress) throws IOException {
    }

    /**
     * Downloads the specified parts. This method will block until the download is complete or an
     * exception occurs.
     *
     * @param   parts     array of part names
     * @param   progress  the download progress callback object
     */
    public void loadPart(String[] parts, DownloadServiceListener progress) throws IOException {
        if (parts == null) {
            return;
        }

        for (int i = 0; i < parts.length; i++) {
            loadPart(parts[i], progress);
        }
    }

    /**
     * Downloads the specified resource. This method will block until the download is complete or an
     * exception occurs.
     *
     * @param   ref       the url for the resource
     * @param   version   the version id or <code>null</code>
     * @param   progress  the download progress callback object
     * @throws  IOException if there is an I/O error during download
     */
    public void loadResource(URL ref, String version, DownloadServiceListener progress) throws IOException {
    }

    /**
     * Removes the specified part of the specified extension.
     *
     * @param   ref       the url for the resource
     * @param   version   the version id or <code>null</code>
     * @param   part      the name of the part
     * @throws  IOException if there is an I/O error
     */
    public void removeExtensionPart(URL ref, String version, String part) throws IOException {
    }

    /**
     * Removes the specified part of the specified extension.
     *
     * @param   ref       the url for the resource
     * @param   version   the version id or <code>null</code>
     * @param   parts     array of part names
     * @throws  IOException if there is an I/O error
     */
    public void removeExtensionPart(URL ref, String version, String[] parts) throws IOException {
        if (parts == null) {
            return;
        }

        for (int i = 0; i < parts.length; i++) {
            removeExtensionPart(ref, version, parts[i]);
        }
    }

    /**
     * Removes the specified part.
     *
     * @param   part  the name of the part
     * @throws  IOException if there is an I/O error
     */
    public void removePart(String part) throws IOException {
    }

    /**
     * Removes the specified parts.
     *
     * @param   parts  array of part names
     * @throws  IOException if there is an I/O error
     */
    public void removePart(String[] parts) throws IOException {
        if (parts == null) {
            return;
        }

        for (int i = 0; i < parts.length; i++) {
            removePart(parts[i]);
        }
    }

    /**
     * Removes the specified resource.
     *
     * @param   ref       the url for the resource
     * @param   version   the version id or <code>null</code>
     * @throws  IOException if there is an I/O error
     */
    public void removeResource(URL ref, String version) throws IOException {
    }
}
