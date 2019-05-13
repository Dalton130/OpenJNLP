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
package org.nanode.launcher.cache;

import java.net.URL;
import java.util.Enumeration;
import java.util.Observable;
import java.util.jar.Manifest;

import org.nanode.launcher.Descriptor;
import org.nanode.launcher.Reference;


/**
 * The abstract class <code>CacheEntry</code> is the common superclass for any cache entry that
 * is provided by a cache implementation. A cache entry is defined by a vendor and title and
 * knows how to manage its resources within the cache.
 * <p>
 * An instance of <code>CacheEntry</code> is never created directly by an application but is
 * returned by a specific cache implementation.
 *
 * @author Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 * @see    Cache#establishEntry
 * @see    Descriptor#getCacheEntry
 */
public abstract class CacheEntry extends Observable {
    /** meta info key for retrieving this cache entry's descriptor URL */
    public static final String METAKEY_DESCRIPTOR = "descriptor";

    /** meta info key for retrieving this cache entry's icon URL */
    public static final String METAKEY_ICON = "icon";


    /** the vendor name for this cache entry */
    protected String vendor;

    /** the title of this cache entry */
    protected String title;

    /** the key that uniquely identifies this cache entry within a cache */
    protected String entryKey;


    /**
     * Creates a cache entry with the specified vendor and title. Combined, these two
     * strings define a unique entry within the cache.
     *
     * @param   vendor  the vendor name of this entry; must not be null
     * @param   title   the title of this entry; must not be null
     */
    protected CacheEntry(String vendor, String title) {
        if (vendor == null || title == null) {
            throw new IllegalArgumentException("vendor and title must be non-null");
        }

        this.vendor = vendor;
        this.title = title;

        entryKey = createEntryKey(vendor, title);
    }

    /**
     * Returns the vendor name for this cache entry.
     *
     * @return  vendor name
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Returns the title for this cache entry.
     *
     * @return  title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns a classloader that can be used to execute this cache entry. This method should be
     * overridden by a subclass.
     * <p>
     * For security reasons, each call to this method is expected to return a new classloader instead
     * of returning a shared instance.
     *
     * @return  a classloader
     */
    public abstract ClassLoader createClassLoader();

    /**
     * Returns the cache that contains this cache entry. This method should be overridden by a subclass.
     *
     * @return  the cache for this cache entry
     */
    public abstract Cache getCache();

    /**
     * Returns the jar manifest for the specified referenced resource.
     *
     * @param   ref  reference to a resource
     * @return  jar manifest or <code>null</code> if the resource is not a jar or has no manifest
     */
    public abstract Manifest getJarManifest(Reference ref);

    /**
     * Returns the meta info specified by the key. This method should be overridden by a subclass.
     * <p>
     * If the meta info for the supplied key is not defined <code>null</code> should be returned.
     *
     * @param   the key used to identify the meta info
     * @return  the defined meta info if defined; <code>null</code> otherwise
     */
    public abstract String getMetaInfo(String key);

    /**
     * Sets the meta-info for the specified key to the specified value. This method should be
     * overridden by a subclass.
     * <p>
     * The key can not be <code>null</code>. If the value is <code>null</code> this will remove
     * the meta info with the specified key.
     *
     * @param   key    the meta info key
     * @param   value  the meta info value
     * @return  the previous value of the specified meta info key or <code>null</code> if it did
     *          not have one
     * @throws  NullPointerException if the key is <code>null</code>
     */
    public abstract String setMetaInfo(String key, String value);

    /**
     * Returns the descriptor associated with this cache entry. This method should be overridden by
     * a subclass.
     * <p>
     * If the descriptor object is defined in this cache entry it will be returned; otherwise
     * <code>null</code> will be returned. This is the actual descriptor object for this cache entry,
     * in contrast to the descriptor meta info, which simply identifies the URL to the descriptor.
     *
     * @return  the descriptor object for this cache entry or <code>null</code> if not defined
     */
    public abstract Descriptor getDescriptor();

    /**
     * Sets the descriptor object for this cache entry. This method should be overridden by a
     * subclass.
     * <p>
     * The argument becomes the descriptor object for this cache entry. Any previous descriptor
     * object is replaced.
     *
     * @param   des  the descriptor object
     */
    public abstract void setDescriptor(Descriptor des);

    /**
     * Indicates the launchability of this cache entry. This method should be overridden by a subclass.
     * <p>
     * It is up to the specific cache entry implementation to determine the rules for launchability.
     *
     * @return  <code>true</code> if this cache entry is considered launchable; <code>false</code> otherwise
     */
    public abstract boolean isLaunchable();

    /**
     * Indicates whether the resource identified by the referenced resource is in this cache entry.
     * This method should be overridden by a subclass.
     * <p>
     * This provides a means of determining whether or not the referenced resource is part of this
     * cache entry with no regard to the up-to-date status of the resource.
     *
     * @param   ref  the reference to a resource
     * @return  <code>true</code> if the resource is in this cache entry; <code>false</code> otherwise
     */
    public abstract boolean isResourceCached(Reference ref);

    /**
     * Adds a referenced resource to this cache entry. This method should be overridden by a subclass.
     * <p>
     * The referenced resource will be added to this cache entry if it is not already a part of the
     * cache entry (optional operation). If the referenced resource is already part of this cache entry
     * the call leaves the cache entry unchanged and returns <code>false</code>.
     * <p>
     * It is up to the specific implementation of the cache entry to make decisions about updating the
     * contents of the cache with this call.
     *
     * @param   ref  the referenced resource
     * @return  <code>true</code> if this cache entry did not already contain the referenced resource;
     *          <code>false</code> if it was already in the cache entry
     */
    public abstract boolean addResource(Reference ref);

    /**
     * Removes a referenced resource from this cache entry. This method should be overridden by a subclass.
     * <p>
     * If the referenced resource is part of this cache entry it will be removed (optional operation);
     * otherwise <code>false</code> will be returned. It is expected the specific cache entry implementation
     * will remove the actual entry from storage as appropriate.
     *
     * @param   ref  the referenced resource
     * @return  <code>true</code> if the referenced resource is removed; <code>false</code> otherwise
     */
    public abstract boolean removeResource(Reference ref);

    /**
     * Returns an enumeration of all of the cached resources in this cache entry. This method should
     * be overridden by a subclass.
     * <p>
     * Each element in the enumeration is expected to be a <code>CachedResource</code> object.
     *
     * @return  an enumeration of all cached resources as <code>CachedResource</code> objects
     */
    public abstract Enumeration cachedResources();
    
    /**
     * Get the cached resource from this cache entry, updating if desired and necessary. This method
     * should be overridden by a subclass.
     * <p>
     * Will return the cached resource within this cache entry. If updating is requested it is expected
     * the resource will be checked and updated if necessary. If the referenced resource is not part
     * of this cache entry then <code>null</code> will be returned.
     *
     * @param   ref     the referenced resource
     * @param   update  whether the cached resource should be updated
     * @return  the cached resource or <code>null</code> if resource not in this cache entry
     */
    public abstract CachedResource getResource(Reference ref, boolean update);

    /**
     * Get the cached resource from this cache entry. The cached resource is not brought up to date.
     *
     * @param   ref  the referenced resource
     * @return  the cached resource or <code>null</code> if resource not in this cache entry
     */
    public final CachedResource getResource(Reference ref) {
        return getResource(ref, false);
    }

    /**
     * Returns the reference to a URL from the specified URL object. If the url is not referenced in
     * this cache entry, a new reference to the URL will be created and returned.
     *
     * @param   url  the url for the desired reference
     * @return  reference to the URL in this cache entry or <code>null</code> if not in this cache entry
     * @throws  NullPointerException if URL is <code>null</code>
     */
    public Reference referenceFromURL(URL url) {
        CachedResource cr;

        for (Enumeration enum = cachedResources(); enum.hasMoreElements();) {
            cr = (CachedResource) enum.nextElement();

            if (url.equals(cr.getReference().getURL())) {
                return cr.getReference();
            }
        }

        return new Reference(url);
    }

    /**
     * Compares two cache entries. The result is <code>true</code> if and only if the argument is
     * not null and is a <code>CacheEntry</code> object that represents the same cache entry as
     * this object. Two cache entry objects are equal if they have the same vendor and title and
     * they are in the same cache.
     *
     * @param   obj  the cache entry to compare to
     * @return  <code>true</code> if the objects are the same; <code>false</code> otherwise
     * @see     Cache#equals
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        try {
            CacheEntry ce = (CacheEntry) obj;

            return (getCache().equals(ce.getCache()) && entryKey.equals(createEntryKey(ce.getVendor(), ce.getTitle())));
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Returns the hash code value for this cache entry. The hash code of a cache entry is defined as the
     * hash code of the entry key (combination of vendor and title) exclusive-or the cache hash code. This
     * ensures that two cache entries with the same entry key will not be equal unless they reside within
     * the same cache.
     *
     * @return  the hash code value for this cache entry
     * @see     Cache#hashCode
     */
    public int hashCode() {
        return (entryKey.hashCode() ^ getCache().hashCode());
    }

    /**
     * Returns a string representation of this cache entry.
     *
     * @return  a string representation of this cache entry
     */
    public String toString() {
        return entryKey;
    }


    /**
     * Creates a key string for the specified vendor and title.
     *
     * @param   vendor  the vendor name
     * @param   title   the title
     * @return  a string representation of the supplied vendor and title
     */
    public static final String createEntryKey(String vendor, String title) {
        if (vendor == null) {
            vendor = "";
        }

        if (title == null) {
            title = "";
        }

        StringBuffer sb = new StringBuffer(vendor);
        sb.append('\u2192');			// --> (arrow)
        sb.append(title);

        return sb.toString();
    }
}
