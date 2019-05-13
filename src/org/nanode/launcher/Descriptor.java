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

import org.nanode.launcher.cache.Cache;
import org.nanode.launcher.cache.CacheEntry;


/**
 * Description of a launchable object.
 *
 * @author Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 */
public abstract class Descriptor {
    protected Cache cache;
    protected URL codebase;
    protected Reference source;

    protected transient CacheEntry cacheEntry;
    protected transient Object context;
    protected transient Information information;
    protected transient Resources resources;


    public Descriptor(Cache cache, URL base, Reference src) {
        this.cache = cache;
        codebase = base;
        source = src;
    }

    /**
     * This will return the cache entry for this descriptor, establishing it if necessary.
     *
     * @return the cache entry for this descriptor
     */
    public CacheEntry getCacheEntry() {
        if (cacheEntry == null) {
            synchronized (this) {
                if (cacheEntry == null) {
                    cacheEntry = cache.establishEntry(this);
                }
            }
        }

        return cacheEntry;
    }

    public URL getCodebase() {
        return codebase;
    }

    public Reference getSource() {
        return source;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object obj) {
        context = obj;
    }

    public Information getInformation() {
        return information;
    }

    public void setInformation(Information info) {
        information = info;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources res) {
        resources = res;
    }
}
