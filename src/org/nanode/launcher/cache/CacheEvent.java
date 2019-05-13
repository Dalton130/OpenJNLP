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
 */
package org.nanode.launcher.cache;

import java.util.EventObject;


/**
 * This class represents an event fired by a change in the Cache.
 */
public class CacheEvent extends EventObject {
    public static final int ENTRY_ADDED = 0;
    public static final int ENTRY_REMOVED = 1;
    public static final int ENTRY_UPDATED = 2;


    protected int type;
    protected CacheEntry entry;


    public CacheEvent(Cache source, int type, CacheEntry entry) {
        super(source);

        this.type = type;
        this.entry = entry;
    }

    public Cache getCache() {
        return (Cache) source;
    }

    public CacheEntry getEntry() {
        return entry;
    }

    public int getType() {
        return type;
    }
}
