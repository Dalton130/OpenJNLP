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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.nanode.launcher.Descriptor;
import org.nanode.launcher.NativelibReference;
import org.nanode.launcher.Reference;
import org.nanode.launcher.Resources;


/**
 * This is a file system implementation of a cache entry. It tracks cached resources and meta
 * info persistently by using subdirectories and files within the cache entry base directory
 * as determined by the file cache object that creates a file cache entry.
 * <p>
 * All meta info and information about which resources are cached is stored in the file
 * <em>entry.xml</em>. Any resource that is cached by this cache entry is stored in the
 * subdirectory <em>Resources</em>. Finally, any native library jars that are added to this
 * cache entry are expanded into the <em>Library</em> subdirectory.
 * <p>
 * Here is an illustration of a cache entry for an app <em>SomeApp</em> by <em>SomeCo</em>:
 * <blockquote><pre>
 * SomeCo/
 *     SomeApp/
 *         entry.xml
 *         Resources/
 *             someapp.jnlp
 *             someappicon.gif
 *             appjar.jar
 *             nativethings.jar
 *         Library/
 *             libthing1.so
 *             libthing2.so
 * </pre></blockquote>
 *
 * @author Kevin Herrboldt (kevin@nanode.org)
 * @see FileCache
 */
public class FileCacheEntry extends CacheEntry {
    private static final String persistFileName = "entry.xml";
    private static final String rsrcDirName = "Resources";
    private static final String libDirName = "Libraries";

    private static final String[] tags = { "entry", "meta", "nativelib", "resource" };

    private static final int TAG_UNKNOWN = -1;
    private static final int TAG_ENTRY = 0;
    private static final int TAG_META = 1;
    private static final int TAG_NATIVELIB = 2;
    private static final int TAG_RESOURCE = 3;


    /** the cache that contains this cache entry */
    protected Cache cache;

    /** where persistent info about the cache entry is stored */
    protected File persistFile;

    /** the last modified date of when the persistent file was last parsed */
    protected long persistLastMod;

    /** the directory for this cache entry */
    protected File entryDir;

    /** subdirectory where cached resources are stored */
    protected File rsrcDir;

    /** subdirectory where nativelibs are extracted */
    protected File libDir;

    /** the descriptor object for this cache entry */
    protected Descriptor entryDescriptor;

    private Map resources;			// map of (URL, CachedResource)
    private Properties entryMeta;	// all of the meta info keys and values


    /**
     * Creates a cache entry in the file system cache for the specified vendor and title.
     *
     * @param   cache      the cache that this cache entry is in
     * @param   entryBase  the directory in the cache that is the base dir for cache entries
     * @param   vendor     the vendor for this cache entry
     * @param   title      the title for this this cache entry
     *
     */
    protected FileCacheEntry(Cache cache, File entryBase, String vendor, String title) {
        super(vendor, title);

        this.cache = cache;

        // entryDir is Vendor+Title relative to the cache
        entryDir = new File(entryBase, getVendor().replace(' ','_'));
        entryDir = new File(entryDir, getTitle());
        rsrcDir = new File(entryDir, rsrcDirName);
        libDir = new File(entryDir, libDirName);
        persistFile = new File(entryDir, persistFileName);

        entryDir.mkdirs();
        rsrcDir.mkdir();

        checkPersistence();

        if (resources == null) {
            resources = new HashMap();
        }
    }

    /**
     * Returns a new class loader object that uses cached resources in this cache entry for the
     * classpath. The class loader object is only valid if there is a descriptor object in this
     * cache entry, otherwise <code>null</code> is returned.
     *
     * @return class loader object for this cache entry or <code>null</code> if no descriptor set
     */
    public ClassLoader createClassLoader() {
        return FileCacheClassLoader.createClassLoader(this);
    }

    /**
     * Returns the cache that contains this cache entry.
     *
     * @return  the cache for this cache entry
     */
    public Cache getCache() {
        return cache;
    }

    /**
     * Returns the jar manifest for the specified referenced resource.
     *
     * @param   ref  reference to a resource
     * @return  jar manifest or <code>null</code> if the resource is not a jar or has no manifest
     */
    public Manifest getJarManifest(Reference ref) {
        File f = new File(getResourceDir(), FileCache.cacheName(ref.getURL()));
        Manifest man = null;

        try {
            JarFile jf = new JarFile(f, false);
            man = jf.getManifest();
        } catch (IOException e) { /* ignore */ }

        return man;
    }

    /**
     * Returns the meta info specified by the key. If the meta info for the supplied key
     * is not defined <code>null</code> is returned.
     *
     * @param   the key used to identify the meta info
     * @return  the defined meta info if defined; <code>null</code> otherwise
     */
    public String getMetaInfo(String key) {
        checkPersistence();

        return ((entryMeta != null) ? entryMeta.getProperty(key) : null);
    }

    /**
     * Sets the meta-info for the specified key to the specified value. The key can not be
     * <code>null</code>. If the value is <code>null</code> this will remove the meta info
     * with the specified key.
     *
     * @param   key    the meta info key
     * @param   value  the meta info value
     * @return  the previous value of the specified meta info key or <code>null</code> if it did
     *          not have one
     * @throws  NullPointerException if the key is <code>null</code>
     */
    public String setMetaInfo(String key, String value) {
        checkPersistence();

        if (entryMeta == null) {
            entryMeta = new Properties();
        }

        String oldval;

        if (value != null) {
            oldval = (String) entryMeta.setProperty(key, value);
        } else {
            oldval = (String) entryMeta.remove(key);
        }

        writePersistence();

        return oldval;
    }

    /**
     * Returns the descriptor associated with this cache entry.
     * <p>
     * If the descriptor object is defined in this cache entry it will be returned; otherwise
     * <code>null</code> will be returned. This is the actual descriptor object for this cache entry,
     * in contrast to the descriptor meta info, which simply identifies the URL to the descriptor.
     *
     * @return  the descriptor object for this cache entry or <code>null</code> if not defined
     */
    public Descriptor getDescriptor() {
        return entryDescriptor;
    }

    /**
     * Sets the descriptor object for this cache entry.
     * <p>
     * The argument becomes the descriptor object for this cache entry. Any previous descriptor
     * object is replaced.
     *
     * @param   des  the descriptor object
     */
    public void setDescriptor(Descriptor des) {
        entryDescriptor = des;
    }

    /**
     * Indicates the launchability of this cache entry. The cache entry is considered launchable if
     * the descriptor meta info is set.
     *
     * @return  <code>true</code> if descriptor meta info set; <code>false</code> otherwise
     */
    public boolean isLaunchable() {
        checkPersistence();

        return (entryMeta != null && entryMeta.containsKey(CacheEntry.METAKEY_DESCRIPTOR));
    }

    /**
     * Indicates whether the resource identified by the referenced resource is in this cache entry.
     * <p>
     * This provides a means of determining whether or not the referenced resource is part of this
     * cache entry with no regard to the up-to-date status of the resource.
     *
     * @param   ref  the reference to a resource
     * @return  <code>true</code> if the resource is in this cache entry; <code>false</code> otherwise
     */
    public boolean isResourceCached(Reference ref) {
        checkPersistence();

        return resources.containsKey(ref.getURL());
    }

    /**
     * Adds a referenced resource to this cache entry. If the cached resource is added any observers
     * will be notified.
     * <p>
     * The referenced resource will be added to this cache entry if it is not already a part of the
     * cache entry (optional operation). If the referenced resource is already part of this cache entry
     * the call leaves the cache entry unchanged and returns <code>false</code>.
     * <p>
     * If the referenced resource is considered eager, it will be updated, even if it was already
     * part of the cache entry. A lazy referenced resource will be added but will not be updated.
     *
     * @param   ref  the referenced resource
     * @return  <code>true</code> if this cache entry did not already contain the referenced resource;
     *          <code>false</code> if it was already in the cache entry
     * @see     Reference#isLazy
     */
    public boolean addResource(Reference ref) {
        if (ref == null || ref.getURL() == null) {
            return false;
        }

        checkPersistence();

        CachedResource cr = null;
        boolean added = false;
        boolean updated = false;

        if (resources.containsKey(ref.getURL())) {
            cr = (CachedResource) resources.get(ref.getURL());
        } else {
            synchronized (resources) {
                if ((cr = (CachedResource) resources.get(ref.getURL())) == null) {
                    cr = new FileCachedResource(ref, getResourceDir(), getLibraryDir());
                    resources.put(ref.getURL(), cr);
                    added = true;
                    setChanged();
                }
            }
        }

        // notify observers if updating cached resource, even if it's already there
        if (cr != null && !cr.getReference().isLazy()) {
            notifyObservers(cr);
            updated = cr.update();
        }

        if (added) {
            clearChanged();
        }

        // if resource is new or it has been updated, write out persistent info
        if (added || updated) {
            writePersistence();
        }

        return added;
    }

    /**
     * Removes a referenced resource from this cache entry.
     * <p>
     * If the referenced resource is part of this cache entry it will be removed (optional operation);
     * otherwise <code>false</code> will be returned. The cached resource is purged when the referenced
     * resource is removed.
     *
     * @param   ref  the referenced resource
     * @return  <code>true</code> if the referenced resource is removed; <code>false</code> otherwise
     * @see     FileCachedResource#purge
     */
    public boolean removeResource(Reference ref) {
        checkPersistence();

        CachedResource cr = null;
        boolean removed = false;

        if (resources.containsKey(ref.getURL())) {
            synchronized (resources) {
                if ((cr = (CachedResource) resources.remove(ref.getURL())) != null) {
                    removed = true;
                    setChanged();
                }
            }
        }

        // only notify observers if actually removed
        if (cr != null) {
            notifyObservers(cr);
            cr.purge();
            writePersistence();
        }

        if (removed) {
            clearChanged();
        }

        return removed;
    }

    /**
     * Returns an enumeration of all of the cached resources in this cache entry.
     *
     * @return  an enumeration of all cached resources as <code>CachedResource</code> objects
     */
    public Enumeration cachedResources() {
        checkPersistence();

        return Collections.enumeration(resources.values());
    }
    
    /**
     * Get the cached resource from this cache entry, updating if desired and necessary.
     * <p>
     * Will return the cached resource within this cache entry. If updating is requested
     * the resource will be checked and updated if necessary. If the referenced resource is not part
     * of this cache entry then <code>null</code> will be returned.
     *
     * @param   ref     the referenced resource
     * @param   update  whether the cached resource should be updated
     * @return  the cached resource or <code>null</code> if resource not in this cache entry
     * @see     FileCachedResource#update
     */
    public CachedResource getResource(Reference ref, boolean update) {
        checkPersistence();

        CachedResource cr = (CachedResource) resources.get(ref.getURL());

        if (cr != null && update) {
            notifyObservers(cr);

            // if resource is updated, write out persistent info
            if (cr.update()) {
                writePersistence();
            }
        }

        return cr;
    }
    
    /**
     * Returns a string representation of this cache entry.
     *
     * @return  a string representation of this cache entry
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("FileCacheEntry[");

        sb.append(getVendor());
        sb.append(',');
        sb.append(getTitle());
        sb.append(',');
        sb.append((entryDescriptor != null) ? entryDescriptor.toString() : "null");
        sb.append(']');

        return sb.toString();
    }

    /**
     * Returns the library directory for this cache entry. The library directory is where
     * native libraries are stored after being extracted from native lib references.
     *
     * @return  library directory for cache entry
     */
    public File getLibraryDir() {
        return libDir;
    }

    /**
     * Returns the resource directory for this cache entry. The resource directory is where
     * referenced resources in the cache entry are stored.
     *
     * @return  resource directory for cache entry
     */
    protected File getResourceDir() {
        return rsrcDir;
    }

    /**
     * Checks the entry persistent file to see if it's out of date with the contents in this object.
     * If the file has been modified since the last time it was read, this method will re-read the
     * file, reloading all information.
     * <p>
     * <strong>Note:</strong> this will minimize synchronization problems if multiple virtual machines
     * try to update the same entry file, although it is not a perfect solution.
     */
    protected void checkPersistence() {
        if (persistLastMod >= persistFile.lastModified()) {
            return;
        }

        synchronized (persistFile) {
            readPersistence();
        }
    }

    /**
     * Reads the persistent info from a file in the cache entry. When called the persistent
     * info for this cache entry is loaded from the persistent info file, which is <em>entry.xml</em>
     * within the cache entry directory. It is parsed using a SAX2 content handler.
     */
    protected void readPersistence() {
        if (!persistFile.exists()) {
            convertOldEntry();		// if no persistent file, assume old entry and convert
        }

        // parse persistent info file
        try {
            XMLReader xmlReader = FileCache.createXMLReader(null);
            xmlReader.setContentHandler(new EntryContentHandler());

            synchronized (this) {
                FileReader fr = new FileReader(persistFile);

                xmlReader.parse(new InputSource(fr));

                fr.close();

                persistLastMod = persistFile.lastModified();
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Writes the persistent info to a file in the cache entry. When called the persistent
     * info for this cache entry is written to the persistent info file as XML. The XML data are
     * written to the <em>entry.xml</em> file within the cache entry directory.
     */
    protected void writePersistence() {
        try {
            CachedResource[] rsrc = new CachedResource[0];

            if (resources != null) {
            	rsrc = (CachedResource[]) resources.values().toArray(rsrc);
            }

            synchronized (this) {
                FileWriter fw = new FileWriter(persistFile);

                fw.write("<?xml version=\"1.0\"?>\n\n");
                fw.write("<entry vendor=\"");
                fw.write(xmlEncodeAttribute(getVendor()));
                fw.write("\" title=\"");
                fw.write(xmlEncodeAttribute(getTitle()));
                fw.write("\">\n");

                if (entryMeta != null) {
                    for (Enumeration enum = entryMeta.propertyNames(); enum.hasMoreElements();) {
                        String key = (String) enum.nextElement();

                        fw.write("  <meta name=\"");
                        fw.write(xmlEncodeAttribute(key));
                        fw.write("\">");
                        fw.write(xmlEncodeContent(entryMeta.getProperty(key)));
                        fw.write("</meta>\n");
                    }

                    fw.write("\n");
                }

                for (int i = 0; i < rsrc.length; i++) {
                    if (rsrc[i].getReference() instanceof NativelibReference) {
                        fw.write("  <nativelib href=\"");
                    } else {
                        fw.write("  <resource href=\"");
                    }

                    fw.write(xmlEncodeAttribute(rsrc[i].getReference().getURL().toString()));
                    fw.write("\" modtime=\"");
                    fw.write(xmlEncodeAttribute(Long.toString(rsrc[i].getLastModified())));
                    fw.write("\" />\n");
                }

                fw.write("</entry>\n");

                fw.close();

                persistLastMod = persistFile.lastModified();
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Converts old-style cache into current format. Makes sure there's a meta file in the entry
     * when done. If the old-style meta file <em>entry.properties</em> exists, its contents are
     * parsed and moved into current meta information. When done, the old-style meta file is removed.
     */
    private final synchronized void convertOldEntry() {
        File f = new File(entryDir, "entry.properties");

        if (f.exists()) {
            Properties oldProps = new Properties();

            try {
                FileInputStream fis = new FileInputStream(f);

                oldProps.load(fis);
                fis.close();
            } catch (Exception e) {
            }

            f.delete();

            if (oldProps.containsKey("entry.url")) {
                setMetaInfo(CacheEntry.METAKEY_DESCRIPTOR, oldProps.getProperty("entry.url"));
            }

            if (oldProps.containsKey("icon.url")) {
                setMetaInfo(CacheEntry.METAKEY_ICON, oldProps.getProperty("icon.url"));
            }
        } else {
            writePersistence();		// make sure there's a persistent info file
        }
    }

    /**
     * Returns an XML attribute parsing-safe encoded string. If the supplied string contains
     * any character that will confuse parsing of an attribute it will be encoded using XML
     * entities where appropriate.
     *
     * @param   s  the string to encode
     * @return  a parsing-safe string that will be encoded if necessary
     */
    protected static String xmlEncodeAttribute(String s) {
        if (s.indexOf('"') != -1 || s.indexOf('\'') != -1 || s.indexOf('&') != -1) {
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < s.length(); i++) {
                switch (s.charAt(i)) {
                    case '"':	sb.append("&quot;");	break;
                    case '\'':	sb.append("&apos;");	break;
                    case '&':	sb.append("&amp;");		break;
                    default:	sb.append(s.charAt(i));	break;
                }
            }

            s = sb.toString();
        }

        return s;
    }

    /**
     * Returns an XML content parsing-safe encoded string. If the supplied string contains
     * any character that will confuse parsing of content it will be encoded using XML
     * entities where appropriate.
     *
     * @param   s  the string to encode
     * @return  a parsing-safe string that will be encoded if necessary
     */
    protected static String xmlEncodeContent(String s) {
        if (s.indexOf('<') != -1 || s.indexOf('>') != -1 || s.indexOf('&') != -1) {
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < s.length(); i++) {
                switch (s.charAt(i)) {
                    case '<':	sb.append("&lt;");		break;
                    case '>':	sb.append("&gt;");		break;
                    case '&':	sb.append("&amp;");		break;
                    default:	sb.append(s.charAt(i));	break;
                }
            }

            s = sb.toString();
        }

        return s;
    }


    /**
     * Handles the XML content for the XML cache entry meta file. This parses the content into
     * meta info and cached resources in the file cache entry.
     * <p>
     * The XML content is exemplified as:
     * <blockquote><pre>
     * &lt;entry vendor="vendorname" title="entrytitle"&gt;
     *   &lt;meta name="metakey"&gt;metadata&lt;/meta&gt;
     *   .
     *   .
     *   &lt;resource href="http://someurl.." modtime="0"&gt;
     *   .
     *   .
     * &lt;/entry&gt;
     * </pre></blockquote>
     *
     * @author Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
     */
    class EntryContentHandler extends DefaultHandler {
        private int parseState;				// current state (where in the XML structure)
        private StringBuffer textBuffer;	// holds any tag content

        private Map resMap;					// (URL, FileCachedResource) map of resources
        private Properties meta;			// meta info key/value pairs

        private String metaKey;				// temporary value that holds meta info key


        /**
         * Creates a content handler for a file cache entry meta file.
         */
        EntryContentHandler() {
            textBuffer = new StringBuffer();

            meta = new Properties(); 
            resMap = new HashMap();
        }

        /**
         * Initializes parsing at the start of a document.
         *
         * @throws  SAXException  if a parsing error occurs
         */
        public void startDocument() throws SAXException {
            parseState = TAG_UNKNOWN;
        }

        /**
         * Finishes parsing of the document. This will set the meta info and resources of the file
         * cache entry to the parsed values.
         *
         * @throws  SAXException  if a parsing error occurs
         */
        public void endDocument() throws SAXException {
            entryMeta = meta;
            resources = resMap;
        }

        /**
         * Invokes the appropriate tag method for start of tag.
         *
         * @param   namespaceURI   the name space uri, if applicable
         * @param   localName      the tag name within the name space
         * @param   qualifiedName  the full name of the tag
         * @param   attrs          tag attributes
         * @throws  SAXException if parse error occurs (such as unknown tag)
         */
        public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attrs) throws SAXException {
            switch (Arrays.binarySearch(tags, localName)) {
                case TAG_ENTRY:		tagEntry(attrs);		break;
                case TAG_META:		tagMeta(attrs);			break;
                case TAG_NATIVELIB:	tagNativelib(attrs);	break;
                case TAG_RESOURCE:	tagResource(attrs);		break;
                default:
                    throw new SAXException("unknown start tag");
            }
        }

        /**
         * Invokes the appropriate tag method for end of tag.
         *
         * @param   namespaceURI   the name space uri, if applicable
         * @param   localName      the tag name within the name space
         * @param   qualifiedName  the full name of the tag
         * @throws  SAXException if parse error occurs (such as unknown tag)
         */
        public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
            switch (Arrays.binarySearch(tags, localName)) {
                case TAG_ENTRY:		tagEntry(null);		break;
                case TAG_META:		tagMeta(null);		break;
                case TAG_NATIVELIB:	tagNativelib(null);	break;
                case TAG_RESOURCE:	tagResource(null);	break;
                default:
                    throw new SAXException("unknown end tag");
            }
        }

        /**
         * Gathers tag content into a string buffer.
         *
         * @param   text    the text buffer with the text
         * @param   start   where in the text buffer to start
         * @param   length  the number of characters in the buffer to gather
         * @throws  SAXException  if a parsing error occurs
         */
        public void characters(char[] text, int start, int length) throws SAXException {
            textBuffer.append(text, start, length);
        }

        /**
         * Gathers ignorable whitespace, collapsing contiguous whitespace into a single space.
         *
         * @param   text    the text buffer with the text
         * @param   start   where in the text buffer to start
         * @param   length  the number of characters in the buffer to gather
         * @throws  SAXException  if a parsing error occurs
         */
        public void ignorableWhitespace(char[] text, int start, int length) throws SAXException {
            // don't add whitespace to beginning of line
            if (textBuffer.length() > 0) {
                textBuffer.append(' ');     // collapse ignorable whitespace to a single space
            }
        }

        /**
         * Handles the <em>entry</em> tag. The start tag clears the meta information and cached
         * resources; the end tag does nothing.
         *
         * @param   attrs  any associated attributes for the tag; only meaningful for start tag
         * @throws  SAXException  if parsing error occurs
         */
        protected void tagEntry(Attributes attrs) throws SAXException {
            changeState(TAG_ENTRY, (attrs != null), TAG_UNKNOWN);

            if (attrs == null) {
                return;
            }

            meta.clear();
            resMap.clear();
        }

        /**
         * Handles the <em>meta</em> tag. The start tag identifies the <em>name</em> as an attribute
         * and the end tag uses the <em>name</em> as the key and any tag text as the value for
         * meta info.
         *
         * @param   attrs  any associated attributes for the tag; only meaningful for start tag
         * @throws  SAXException  if parsing error occurs
         */
        protected void tagMeta(Attributes attrs) throws SAXException {
            changeState(TAG_META, (attrs != null), TAG_ENTRY);

            if (attrs != null) {
                metaKey = attrs.getValue("name");
                textBuffer.setLength(0);
            } else {
                meta.setProperty(metaKey, textBuffer.toString());
            }
        }

        /**
         * Handles the <em>nativelib</em> tag. The start tag creates a file cached nativelib resource
         * and the end tag does nothing.
         *
         * @param   attrs  any associated attributes for the tag; only meaningful for start tag
         * @throws  SAXException  if parsing error occurs
         */
        protected void tagNativelib(Attributes attrs) throws SAXException {
            changeState(TAG_NATIVELIB, (attrs != null), TAG_ENTRY);

            if (attrs == null) {
                return;
            }

            try {
                Reference ref = new NativelibReference(new URL(attrs.getValue("href")));

                resMap.put(ref.getURL(), new FileCachedResource(ref, Long.parseLong(attrs.getValue("modtime")), getResourceDir(), getLibraryDir()));
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        
        /**
         * Handles the <em>resource</em> tag. The start tag creates a file cached resource and
         * the end tag does nothing.
         *
         * @param   attrs  any associated attributes for the tag; only meaningful for start tag
         * @throws  SAXException  if parsing error occurs
         */
        protected void tagResource(Attributes attrs) throws SAXException {
            changeState(TAG_RESOURCE, (attrs != null), TAG_ENTRY);

            if (attrs == null) {
                return;
            }

            try {
                Reference ref = new Reference(new URL(attrs.getValue("href")));

                resMap.put(ref.getURL(), new FileCachedResource(ref, Long.parseLong(attrs.getValue("modtime")), getResourceDir(), getLibraryDir()));
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        /**
         * Changes the state based on the current tag and the desired tag. If entering the state changes
         * from parent to the new state; leaving the state changes from new state to parent. An exception
         * is thrown if the state change isn't correct.
         *
         * @param   enterState   the new state
         * @param   entering     <code>true</code> if entering state; otherwise exiting state
         * @param   parentState  the parent state
         * @throws  SAXException  if the state transistion is wrong
         */
        protected void changeState(int enterState, boolean entering, int parentState) throws SAXException {
            if (entering) {
                if (parseState != parentState) {
                    throw new SAXException("misplaced tag");
                } else {
                    parseState = enterState;
                }
            } else {
                if (parseState != enterState) {
                    throw new SAXException("end tag with no start");
                } else {
                    parseState = parentState;
                }
            }
        }
    }
}
