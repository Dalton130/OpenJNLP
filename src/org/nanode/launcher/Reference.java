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
import java.util.Arrays;
import java.util.HashSet;


/**
 * Reference to an object accessible via a URL. A reference can indicate version-ids and whether
 * or not to download eagerly or lazily.
 *
 * @author Kevin Herrboldt (kevin@nanode.org)
 */
public class Reference {
    /** url that this reference describes. */
    protected URL url;

    /** all of the version-ids associated with this reference. */
    protected Version[] versions;

    /** indicates the lazy status of this reference. */
    protected boolean lazy;


    /**
     * Creates a reference to a url with no version-id that is eagerly downloaded.
     *
     * @param   url  the url this reference describes
     */
    public Reference(URL url) {
        this(url, (Version[]) null, false);
    }

    /**
     * Creates a reference to a url with the specified version-id that is eagerly downloaded.
     * If <code>ver</code> is <code>null</code> then an empty version-id will be assumed.
     *
     * @param   url  the url this reference describes
     * @param   ver  the version-id associated with the url
     */
    public Reference(URL url, Version ver) {
        this(url, (ver != null) ? new Version[] { ver } : null, false);
    }

    /**
     * Creates a reference to a url with the specified version-ids and how to download.
     * If <code>vers</code> is <code>null</code> then an empty version-id will be assumed.
     *
     * @param   url  the url this reference describes
     * @param   vers an array of version-ids associated with the url
     * @param   lazy if <code>true</code> indicates this is a lazy reference, otherwise eager
     */
    public Reference(URL url, Version[] vers, boolean lazy) {
        this.url = url;
        versions = (vers != null && vers.length > 0) ? vers : new Version[] { Version.EMPTY_VERSION };
        this.lazy = lazy;
    }

    /**
     * Returns the url this reference describes.
     *
     * @return  the url this reference describes
     */
    public URL getURL() {
        return url;
    }

    /**
     * Returns the version-ids associated with the url of this reference. An unversioned reference
     * is referenced with <code>Version.EMPTY_VERSION</code>.
     *
     * @return  an array of version-ids
     * @see     Version
     */
    public Version[] getVersions() {
        return versions;
    }

    /**
     * Returns whether or not this resource should be treated as lazy-downloable.
     *
     * @return  <code>true</code> if this is a lazy resource; <code>false</code> if it is eager
     */
    public boolean isLazy() {
        return lazy;
    }

    /**
     * Returns an integer suitable for hash table indexing.
     *
     * @return  a hash code for this reference
     */
    public int hashCode() {
        return url.hashCode();
    }

    /**
     * Compares two references. The result is <code>true</code> if and only if the argument is
     * not null and is a <code>Reference</code> object that represents the same reference as
     * this object. Two reference objects are equal if they reference URLs that are equal,
     * they have the same lazy or eager state and the references have at least one version in
     * common.
     *
     * @param   obj  the reference to compare to
     * @return  <code>true</code> if the objects are the same; <code>false</code> otherwise
     * @see     java.net.URL#equals
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Reference)) {
            return false;
        }

        Reference ref = (Reference) obj;

        // see if lazy states match
        if (isLazy() != ref.isLazy()) {
            return false;
        }

        // see if urls match
        if (!url.equals(ref.getURL())) {
            return false;
        }

        // if either reference is version-less then they are equal only if both are version-less
        if (versions.length == 0 && ref.getVersions().length == 0) {
            return true;
        }

        // determine if there are any versions in common between the two references
        HashSet verSet = new HashSet(Arrays.asList(versions));
        verSet.retainAll(new HashSet(Arrays.asList(ref.getVersions())));

        return (verSet.size() > 0);
    }

    /**
     * Converts this reference to a string.
     *
     * @return  a string representation of this reference
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("Reference[url=");

        sb.append(url);
        sb.append(",versions=\"");

        for (int i = 0; i < versions.length; i++) {
            sb.append(versions[i]);

            if (i < versions.length - 1) {
                sb.append(' ');
            }
        }

        sb.append(lazy ? "\",lazy]" : "\",eager]");

        return sb.toString();
    }
}
