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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.helpers.ParserFactory;
import org.xml.sax.helpers.XMLReaderFactory;

import org.nanode.launcher.Descriptor;
import org.nanode.launcher.Gestalt;


/**
 * This class implements the <code>Cache</code> interface and serves as the default
 * cache implementation.
 * <p>
 * The default cache implementation is a file system-based cache located relative
 * to <code>user.home</code> in the base directory of ".jnlp/cache/". Cached apps
 * are stored in ".jnlp/cache/app/<em>vendor</em>/<em>title</em> where <em>vendor</em>
 * and <em>title</title> are the values from the descriptor information used to
 * establish a cache entry.
 *
 * @author Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 */
public class FileCache extends Cache {
    protected static final File homeDir = new File(System.getProperty("user.home"));
    protected static final File defaultPrefsDir = new File(homeDir, ".jnlp");
    protected static final File defaultCacheDir = new File(defaultPrefsDir, "cache");

    protected static final File macosxLibraryDir = new File(homeDir, "Library");
    protected static final File macosxPrefsDir = new File(new File(macosxLibraryDir, "Preferences"), "OpenJNLP");
    protected static final File macosxCacheDir = new File(new File(macosxLibraryDir, "Caches"), "OpenJNLP");

    protected static final String CACHE_APP = "app";


    protected File cacheBase;
    protected File cacheApp;

    protected Map entryMap;			// this is a set of (String, FileCacheEntry)


    public FileCache(File cacheDir) {
        cacheBase = cacheDir;

        cacheApp = new File(cacheBase, CACHE_APP);
        cacheApp.mkdirs();

        // see if cache is directory and is read/write
        if (!cacheBase.isDirectory() || !cacheBase.canRead()) {
            throw new CacheError("Invalid cache at " + cacheBase.toString());
        }

        if (!cacheApp.isDirectory() || !cacheApp.canRead()) {
            throw new CacheError("Invalid cache at " + cacheApp.toString());
        }

        convertOldCache();		// convert old cache if necessary

        entryMap = new HashMap();

        // read in any existing entries into the entryHash
        String[] vendors = cacheApp.list();
        int cnt = (vendors != null) ? vendors.length : 0;

        for (int i = 0; i < cnt; i++) {
            File cacheVendor = new File(cacheApp, vendors[i]);
            String[] titles = cacheVendor.list();
            int cnt1 = (titles != null) ? titles.length : 0;

            for (int j = 0; j < cnt1; j++) {
                if (new File(cacheVendor, titles[j]).isDirectory()) {
                    establishEntry(vendors[i], titles[j]);
                }
            }
        }
    }

    public Iterator entries() {
        return Collections.unmodifiableCollection(entryMap.values()).iterator();
    }

    /**
     * This returns a CacheEntry instance for the supplied descriptor.
     * <p>
     * Each descriptor's cache entry is kept in a hashtable of (Descriptor, CacheEntry),
     * only creating one cache entry per descriptor.
     *
     * @param  des   the descriptor
     * @return the cache entry for the descriptor
     */
    public CacheEntry establishEntry(Descriptor des) {
        if (des.getInformation() == null) {
            throw new CacheException("unable to define descriptor in cache");
        }

        FileCacheEntry entry = establishEntry(des.getInformation().getDefaultVendor(), des.getInformation().getDefaultTitle());

        if (entry != null) {
            entry.setDescriptor(des);
        }

        return entry;
    }

    protected FileCacheEntry establishEntry(String vendor, String title) {
        String key = CacheEntry.createEntryKey(vendor, title);

        FileCacheEntry entry = (FileCacheEntry) entryMap.get(key);

        if (entry == null) {
            boolean added = false;

            // no entry found, now synchronize on the map and check again
            synchronized (entryMap) {
                if ((entry = (FileCacheEntry) entryMap.get(key)) == null) {
                    // no other thread created one, so create and store in set
                    entry = new FileCacheEntry(this, cacheApp, vendor, title);
                    entryMap.put(key, entry);
                    added = true;
                }
            }

            if (added) {
                fireCacheEntryAdded(entry);
            }
        }

        return entry;
    }

    private final void convertOldCache() {
        deltree(new File(cacheBase, "ref"));
    }


    static final void deltree(File f) {
        if (!f.exists()) {
            return;
        }

        File[] contents = f.listFiles();

        for (int i = 0; i < contents.length; i++) {
            if (contents[i].isDirectory()) {
                deltree(contents[i]);
            }

            contents[i].delete();
        }

        f.delete();
    }


    /**
     * Creates SAX2 XMLReader based on environment and/or argument.
     * <p>
     * Three successive means are tried in order to successfully return an XMLReader. The means tried
     * are, in order:
     * <ul>
     *  <li>SAX2-compatible parser using XMLReaderFactory.createXMLReader</li>
     *  <li>SAX1-adapted parser using ParserFactory.makeParser</li>
     *  <li>NanoXML2 SAX1-adapted parser using ParserFactory.makeParser("net.n3.nanoxml.sax.SAXParser")</li>
     * </ul>
     * <p>
     *
     * @returns desired SAX2 XMLReader
     * @throws ParserException if can't create appropriate parser
     */
    public static final XMLReader createXMLReader(String className) throws ParseException {
        ParseException except = null;
        XMLReader reader = null;

        // first try SAX2-style method
        try {
            reader = (className != null) ? XMLReaderFactory.createXMLReader(className) : XMLReaderFactory.createXMLReader();
        } catch (Throwable t) {
            except = new ParseException(t.getMessage(), -1);
        }

        // if failed to get SAX2 parser, try for SAX1 parser
        if (reader == null) {
            try {
                if (className != null) {
                    reader = new ParserAdapter(ParserFactory.makeParser(className));
                } else {
                    reader = new ParserAdapter(ParserFactory.makeParser());
                }
            } catch (Throwable t) {
                except = new ParseException(t.getMessage(), -1);
            }
        }

        // if still failed, try to get default parser (NanoXML2 SAX1 parser)
        if (reader == null) {
            try {
                reader = new ParserAdapter(ParserFactory.makeParser("net.n3.nanoxml.sax.SAXParser"));
            } catch (Throwable t) {
                if (except == null) {
                    except = new ParseException(t.getMessage(), -1);
                }
            }
        }

        // if failed to get reader and there's a defined exception throw it
        if (reader == null && except != null) {
            throw except;
        }

        return reader;
    }

    public static String cacheName(URL url) {
        return url.getFile().substring(url.getFile().lastIndexOf("/"));
    }

    public static FileCache defaultCache() {
        File base = defaultCacheDirectory();

        // if not default directory, move cache to new directory if necessary (works for all new platforms)
        if (base != defaultCacheDir && defaultCacheDir.exists()) {
            base.mkdirs();
            String[] contents = defaultCacheDir.list();

            for (int i = 0; i < contents.length; i++) {
                boolean result = new File(defaultCacheDir, contents[i]).renameTo(new File(base, contents[i]));

                if (!result) {
                    System.err.println("failed to move cache file: " + contents[i]);
                }
            }

            deltree(defaultPrefsDir);	// delete the whole thing, there were never any prefs stored here
        }
        
        return new FileCache(base);
    }

    public static File defaultCacheDirectory() {
        return (Gestalt.osPlatform() == Gestalt.OSPLATFORM_MACOSX) ? macosxCacheDir : defaultCacheDir;
    }

    public static File defaultPrefsDirectory() {
        return (Gestalt.osPlatform() == Gestalt.OSPLATFORM_MACOSX) ? macosxPrefsDir : defaultPrefsDir;
    }

    public static URL toSafeURL(File f) throws MalformedURLException {
        String path = (File.separatorChar == '/') ? f.getAbsolutePath() : f.getAbsolutePath().replace(File.separatorChar, '/');
        StringBuffer sb = new StringBuffer();

        if (path.charAt(0) != '/') {
            sb.append('/');
        }

        for (int i = 0; i < path.length(); i++) {
            switch  (Character.toLowerCase(path.charAt(i))) {
                // number
                case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
                // alpha
                case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j':
                case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't':
                case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
                // mark
                case '-': case '_': case '.': case '!': case '~': case '*': case '\'': case '(': case ')':
                // other
                case ':': case '@': case '&': case '=': case '+': case '$': case ',': case '/': case ';':
                    sb.append(path.charAt(i));
                    break;
                default:
                    sb.append('%');
                    sb.append(Integer.toString(path.charAt(i), 16));
                    break;
            }
        }

        if (!path.endsWith("/") && f.isDirectory()) {
            sb.append('/');
        }

        return new URL("file", "", sb.toString());
    }
}
