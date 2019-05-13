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
import java.util.Iterator;

import org.nanode.launcher.Descriptor;


/**
 * This class is the abstract superclass of all actual implementations of the
 * Launcher Cache. Subclasses of Cache are used to bind the cache to particular
 * implementations.
 * <p>
 * This will bind to the default cache implementation unless otherwise set
 *
 * @author Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 */
public abstract class Cache {
    public static final String CACHE_PROPERTY = "org.nanode.launcher.cache";

    protected static final String DEFAULT_CACHE = "org.nanode.launcher.cache.FileCache";
    protected static final CacheListener[] emptyList = new CacheListener[0];


    private static Cache defaultCache;         // the instance of the cache implementation

    protected transient CacheListener[] listenerList = emptyList;


    public synchronized void addCacheListener(CacheListener l) {
        if (l == null) {
            return;
        }

        if (listenerList == emptyList) {
            listenerList = new CacheListener[] { l };
        } else {
            int i = listenerList.length;
            CacheListener[] tmp = new CacheListener[i + 1];

            System.arraycopy(listenerList, 0, tmp, 0, i);
            tmp[i] = l;

            listenerList = tmp;
        }
    }

    public synchronized void removeCacheListener(CacheListener l) {
        if (l == null) {
            return;
        }

        int index = listenerList.length;

        for (; index >= 0; --index) {
            if(listenerList[index].equals(l)) {
                break;
            }
        }

        if (index != -1) {
            CacheListener[] tmp = new CacheListener[listenerList.length - 1];
            System.arraycopy(listenerList, 0, tmp, 0, index);

            if (index < tmp.length) {
                System.arraycopy(listenerList, index + 1, tmp, index, tmp.length - index);
            }

            listenerList = (tmp.length > 0) ? tmp : emptyList;
        }
    }

    protected void fireCacheEntryAdded(CacheEntry entry) {
        CacheEvent evt = null;

        for (int i = 0; i < listenerList.length; i++) {
            if (evt == null) {
                evt = new CacheEvent(this, CacheEvent.ENTRY_ADDED, entry);
            }

            listenerList[i].cacheEntryAdded(evt);
        }
    }

    protected void fireCacheEntryRemoved(CacheEntry entry) {
        CacheEvent evt = null;

        for (int i = 0; i < listenerList.length; i++) {
            if (evt == null) {
                evt = new CacheEvent(this, CacheEvent.ENTRY_REMOVED, entry);
            }

            listenerList[i].cacheEntryRemoved(evt);
        }
    }

    protected void fireCacheEntryUpdated(CacheEntry entry) {
        CacheEvent evt = null;

        for (int i = 0; i < listenerList.length; i++) {
            if (evt == null) {
                evt = new CacheEvent(this, CacheEvent.ENTRY_UPDATED, entry);
            }

            listenerList[i].cacheEntryUpdated(evt);
        }
    }

    public CacheEntry entryFromDescriptorURL(URL descURL) {
        String desc = descURL.toString();

        for (Iterator iter = entries(); iter.hasNext();) {
            CacheEntry entry = (CacheEntry) iter.next();

            if (desc.equals(entry.getMetaInfo(CacheEntry.METAKEY_DESCRIPTOR))) {
                return entry;
            }
        }

        return null;
    }

    public abstract CacheEntry establishEntry(Descriptor des);

    public abstract Iterator entries();


    /**
     * Gets the default cache.
     * <p>
     * If there is a system property named <code>org.nanode.launcher.cache</code>, that
     * property is treated as the name of a class that is a subclass of <code>Cache</code>.
     * <p>
     * If the system property does not exist, then the default cache used is the class
     * named <code>org.nanode.launcher.cache.FileCache</code>, which implements default
     * caching behavior.
     *
     * @see org.nanode.launcher.cache.FileCache
     * @returns the default cache
     * @throws CacheError if a cache could not be found, or if one can not be accessed
     * or instantiated.
     */
    public static synchronized Cache getDefaultCache() {
        if (defaultCache == null) {
            // attempt to instantiate new Cache
            try {
                String cacheClassName = System.getProperty(CACHE_PROPERTY);

                if (cacheClassName != null) {
                    defaultCache = (Cache) Class.forName(cacheClassName).newInstance();
                } else {
                    defaultCache = FileCache.defaultCache();
                }
            } catch (Throwable t) {
                // something went wrong creating the cache, throw an error
                throw new CacheError(t.getMessage());
            }
        }

        return defaultCache;    // this will only be reached if successfully defined
    }
}
