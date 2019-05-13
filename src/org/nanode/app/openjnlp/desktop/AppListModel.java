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
 *    Kevin Herrboldt <kevin@nanode.org>
 */
package org.nanode.app.openjnlp.desktop;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractListModel;

import org.nanode.app.openjnlp.DefaultAppHandler;
import org.nanode.jnlp.JNLPParser;
import org.nanode.launcher.cache.Cache;
import org.nanode.launcher.cache.CacheEntry;
import org.nanode.launcher.cache.CacheEvent;
import org.nanode.launcher.cache.CacheListener;


public class AppListModel extends AbstractListModel implements CacheListener {
    final Cache cache;
    final Set entrySet;


    public AppListModel() {
        this(DefaultAppHandler.getAppHandler().getPrimaryCache());
    }

    public AppListModel(Cache c) {
        cache = c;

        entrySet = Collections.synchronizedSet(new TreeSet(new CacheEntryComparator()));

        cache.addCacheListener(this);

        new Thread(new Runnable() {
            public void run() {
                for (Iterator iter = cache.entries(); iter.hasNext();) {
                    CacheEntry ce = (CacheEntry) iter.next();

                    if (ce.isLaunchable()) {
                        entrySet.add(ce);
                        fireContentsChanged(AppListModel.this, 0, getSize());

                        /* try {
                            JNLPParser.getEntryDescriptor(ce);		// make sure reference is set
                        } catch (Exception e) {
                            e.printStackTrace();
                        } */
                    }
                }
            }
        }).start();
    }

    public void cacheEntryAdded(CacheEvent e) {
        /* if (!e.getEntry().isLaunchable()) {
            return;
        } */

        if (entrySet.add(e.getEntry())) {
            fireContentsChanged(this, 0, getSize());

            /* try {
                JNLPParser.getEntryDescriptor(e.getEntry());
            } catch (Exception x) {
            } */
        }
    }

    public void cacheEntryRemoved(CacheEvent e) {
        if (entrySet.remove(e.getEntry())) {
            fireContentsChanged(this, 0, getSize());
        }
    }

    public void cacheEntryUpdated(CacheEvent e) {
    }

    public Object getElementAt(int index) {
        return entrySet.toArray()[index];
    }

    public int getSize() {
        return entrySet.size();
    }


    class CacheEntryComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            CacheEntry c1 = ((CacheEntry) o1);
            CacheEntry c2 = ((CacheEntry) o2);

            if (c1 == c2) {
                return 0;
            }

            if (c1 == null && c2 != null) {
                return -1;
            }

            if (c1 != null && c2 == null) {
                return 1;
            }

            return (c1.getTitle().compareTo(c2.getTitle()));
        }

        public boolean equals(Object obj) {
            return (obj instanceof CacheEntryComparator);
        }
    }
}
