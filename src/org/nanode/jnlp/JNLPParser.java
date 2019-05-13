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
package org.nanode.jnlp;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.nanode.launcher.Descriptor;
import org.nanode.launcher.Launcher;
import org.nanode.launcher.LauncherParser;
import org.nanode.launcher.Reference;
import org.nanode.launcher.cache.Cache;
import org.nanode.launcher.cache.CacheEntry;
import org.nanode.launcher.cache.CachedResource;
import org.nanode.launcher.cache.FileCache;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


/**
 * The abstract class <code>JNLPParser</code> is the common superclass of all jnlp file parsers.
 * A jnlp file parser parses an JNLP file into a descriptor object.
 * <p>
 * Subclasses of <code>JNLPParser</code> only need to provide the URL and input stream to the jnlp
 * file itself; this class provides the parsing logic.
 *
 * @author Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 * @see    Descriptor
 */
public abstract class JNLPParser implements LauncherParser {
    /** the MIME type for jnlp files, defined as <code>application/x-java-jnlp-file</code> */
    public static final String JNLP_MIME_TYPE = "application/x-java-jnlp-file";

    /** the file extension for jnlp files, defined as <code>.jnlp</code> */
    public static final String JNLP_FILE_EXTENSION = ".jnlp";

    private static final String[] options = { "-extclasspath", "-extcmd", "-extmain",
                                              "-help", "-internal",  "-setlibpath","-version",
                                             };

    private static final int OPT_EXT_CLASSPATH = 0;
    private static final int OPT_EXT_CMD = 1;
    private static final int OPT_EXT_MAIN = 2;
    private static final int OPT_HELP = 3;
    private static final int OPT_INTERNAL = 4;
    private static final int OPT_VERSION = 6;
    private static final int OPT_LIBPATH = 5;    


    /** the cache that the parsed descriptor will be in */
    protected Cache cache;

    /** the parsed descriptor */
    protected Descriptor descriptor;

    /**
     * Returns the parsed descriptor object. The descriptor object is not set until parsing has
     * been done.
     *
     * @return  the parsed descriptor object
     */
    public Descriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Indicates the parsed state of this parser.
     *
     * @return  <code>true</code> if parsing has occured; <code>false</code> otherwise
     */
    public boolean isParsed() {
        return (descriptor != null);
    }

    /**
     * Parses the jnlp file into a descriptor object. Parsing is only done the first time; once the
     * descriptor object is created subsequent calls will return immediately.
     *
     * @throws  ParseException if a parsing error occurs
     */
    public synchronized void parse() throws ParseException {
        // if already parsed, nothing to do
        if (isParsed()) {
            return;
        }

        descriptor = null;        // unparsed, make sure we start over

        JNLPContentHandler handler = null;

        // attempt to parse XML contents from source
        try {
            handler = new JNLPContentHandler(cache, getSourceURL());

            XMLReader xmlReader = FileCache.createXMLReader(null);		// get default parser
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(openInputStream()));
            descriptor = handler.getDescriptor();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParseException(e.getMessage(), -1);
        }

        descriptor = (handler != null) ? handler.getDescriptor() : null;   // parsing done
    }

    /**
     * Returns the URL to the data source (the jnlp file). This method should be overridden by
     * a subclass.
     *
     * @return  URL to the data source
     */
    public abstract URL getSourceURL();

    /**
     * Opens an input stream to the jnlp file data. This method should be overridden by a subclass.
     *
     * @return  input stream to jnlp file data
     * @throws  IOException if an I/O error occurs
     */
    protected abstract InputStream openInputStream() throws IOException; 


    /**
     * Returns the descriptor object for the supplied cache entry. If the descriptor object for
     * the cache entry is non-null it will be returned. If the descriptor object has not yet
     * been set within the cache entry, the jnlp file for the cache entry will be parsed to
     * create the descriptor object. In this case the cache entry's descriptor will be set to the
     * parsed descriptor object as well.
     *
     * @param   entry  the cache entry from which to get the descriptor
     * @return  the descriptor object for the cache entry
     * @throws  ParseException if there is a parsing error or the cache entry is <code>null</code>
     */
    public static Descriptor getEntryDescriptor(CacheEntry entry) throws ParseException {
        if (entry == null) {
            throw new ParseException("No cache entry is defined", -1);
        }

        Descriptor des = entry.getDescriptor();

        if (des == null) {
            synchronized (entry) {
                if ((des = entry.getDescriptor()) == null) {
                    JNLPParser parser = new CachedJNLPParser(entry);
                    parser.parse();

                    des = entry.getDescriptor();
                }
            }
        }

        return des;
    }

    /**
     * Launches each URL from array in current JVM using specified cache.
     *
     * @param   urls  array of URLs to JNLP descriptors
     * @param setLibPath [in] if true, the system library path is set to the jnlp library directory 
     * @throws  ParseException if error occurs during parsing of JNLP descriptor
     */
    public static void launchJNLP(Cache cache, URL[] urls, boolean setLibPath) throws ParseException {
        launchJNLP(cache, urls, false, setLibPath);
    }

    public static void launchJNLP(Cache cache, URL[] urls, boolean internal, boolean setLibPath) throws ParseException {
        if (urls == null) {
            return;
        }

        for (int i = 0; i < urls.length; i++) {
            if (urls[i] == null) {
                continue;
            }

            launchJNLP(cache, urls[i], internal, setLibPath);
        }
    }

    /**
     * Launches JNLP app within current JVM. The URL is treated as a URL to
     * a JNLP descriptor which is launched from the specified cache within this JVM.
     * The JNLP descriptor and the resources it specifies are updated in the cache
     * or added if necessary.
     *
     * @param   url  URL to JNLP descriptor
     * @param setLibPath [in] if true, the system library path is set to the jnlp library directory
     * @throws  ParseException if error occurs during parsing of JNLP descriptor
     */
    public static void launchJNLP(Cache cache, URL url, boolean setLibPath) throws ParseException {
        launchJNLP(cache, url, false, setLibPath);
    }

    public static void launchJNLP(Cache cache, URL url, boolean internal, boolean setLibPath) throws ParseException {
        if (url == null) {
            return;
        }

        CacheEntry entry = parseDescriptor(cache, url);
        Descriptor des = getEntryDescriptor(entry);

        // make sure eager jars are up-to-date
        for (Enumeration enum = des.getResources().eagerJars(); enum.hasMoreElements();) {
            Reference jarRef = (Reference) enum.nextElement();

            entry.addResource(jarRef);
        }

        // make sure eager nativelibs are up-to-date
        for (Enumeration enum = des.getResources().eagerNativelibs(); enum.hasMoreElements();) {
            Reference libRef = (Reference) enum.nextElement();

            entry.addResource(libRef);
        }

        if (internal) {
            Launcher.launchInternal(des, setLibPath);
        } else {
            Launcher.launchExternal(des, setLibPath);
        }
    }

    /**
     * Returns the media portion from content type. An example of the format of <code>Content-type</code> is:
     * <blockquote>
     * <code>text/plain; charset=US-ASCII</code>
     * </blockquote>
     * The <i>type/subtype</i> portion is the media specification, and the rest (everything afer
     * the semicolon) is optional.
     */
    public static String mediaFromContentType(String contentType) {
        if (contentType == null) {
            return "";
        }

        int end = contentType.indexOf(";");

        return contentType.substring(0, (end != -1) ? end : contentType.length()).trim();
    }
    
    /**
     * Parses the jnlp descriptor file pointed to by the source url into a cache entry in the
     * cache specified. If the source URL refers to a descriptor that already exists in the
     * supplied cache then that cache entry will be used and the jnlp file is reparsed. The
     * source URL will be parsed and a cache entry will be created if the source URL does not
     * refer to an existing cache entry.
     *
     * @param   cache   the cache that will contain the resulting cache entry
     * @param   srcURL  URL to the jnlp descriptor
     * @return  the cache entry for the parsed jnlp descriptor
     * @throws  ParseException if an error occurs during parsing or the cache or source URL are
     *          <code>null</code>
     */
    public static CacheEntry parseDescriptor(Cache cache, URL srcURL) throws ParseException {
        if (cache == null) {
            throw new ParseException("no cache specified", -1);
        }

        if (srcURL == null) {
            throw new ParseException("no source URL", -1);
        }

        JNLPParser parser = null;
        CacheEntry entry = cache.entryFromDescriptorURL(srcURL);	// check first if in cache

        // if no entry in cache, parse the URL directly and add to cache
        if (entry == null) {
            parser = new URLJNLPParser(cache, srcURL);
            parser.parse();

            //Descriptor des = parser.getDescriptor();

            entry = cache.establishEntry(parser.getDescriptor());
            updateMetaInfo(entry);

            // add jnlp descriptor to cache
            JNLPSpecification jnlpSpec = (JNLPSpecification) entry.getDescriptor().getContext();

            if (jnlpSpec.getReference() != null) {
                entry.addResource(jnlpSpec.getReference());
            }
        }

        // if the jnlp descriptor is cached, parse via the cache to use latest/correct jnlp descriptor
        if (entry != null && entry.getMetaInfo(CacheEntry.METAKEY_DESCRIPTOR) != null) {
            // parse descriptor from cache entry
            parser = new CachedJNLPParser(entry);
            parser.parse();

            updateMetaInfo(entry);
        }

        return entry;
    }

    /**
     * Updates meta information in the specified cache entry. The meta information updated is
     * icon and descriptor urls, if defined. Updating only occurs on each meta information value
     * if the meta info set is different from the new value.
     *
     * @param   entry  the cache entry to update
     */
    public static void updateMetaInfo(CacheEntry entry) {
        Descriptor des = (entry != null) ? entry.getDescriptor() : null;
        JNLPSpecification spec = null;
        URL url = null;

        if (entry == null || des == null) {
            return;
        }

        if (des.getContext() instanceof JNLPSpecification) {
            spec = (JNLPSpecification) des.getContext();
        }

        // get descriptor url from either jnlp href or source url
        if (spec != null && spec.getReference() != null && spec.getReference().getURL() != null) {
            url = spec.getReference().getURL();
        } else {
            url = des.getSource().getURL();
        }

        // if url defined and different from what's in cache, update it
        if (url != null) {
            if (!url.toString().equals(entry.getMetaInfo(CacheEntry.METAKEY_DESCRIPTOR))) {
                entry.setMetaInfo(CacheEntry.METAKEY_DESCRIPTOR, url.toString());
            }
        }

        try {
            // if icon is defined and it's different from what's in cache entry set icon meta info
            url = des.getInformation().getIconInfo().getIconReference().getURL();

            if (url != null && !url.toString().equals(entry.getMetaInfo(CacheEntry.METAKEY_ICON))) {
                entry.setMetaInfo(CacheEntry.METAKEY_ICON, url.toString());
            }
        } catch (NullPointerException e) {
            // ignore this
        }
    }

    /**
     * Launches specified JNLP apps in current JVM. Each argument is parsed as a URL and passed
     * to {@link #launchJNLP(Cache, URL[]) launchJNLP}.
     *
     * @param args  array of URLs to JNLP files
     */
    public static void main(String[] args) {
        if (args == null) {
            return;
        }

        // set up any arguments
        int arg = 0;
        boolean showHelp = false;
        boolean showVersion = false;
        boolean internal = false;
        boolean setLibPath = false;
 

        for (arg = 0; arg < args.length && args[arg].charAt(0) == '-'; arg++) {
            switch (Arrays.binarySearch(options, args[arg])) {
                case OPT_EXT_CLASSPATH:
                    if (++arg > args.length) {
                        showHelp = true;
                    } else {
                        //Launcher.setLaunchProperty(Launcher.LAUNCH_CLASSPATH, args[arg]);
                    }

                    break;
                case OPT_EXT_CMD:
                    if (++arg > args.length) {
                        showHelp = true;
                    } else {
                        Launcher.setLaunchProperty(Launcher.LAUNCH_COMMAND, args[arg]);
                    }

                    break;
                case OPT_EXT_MAIN:
                    if (++arg > args.length) {
                        showHelp = true;
                    } else {
                        Launcher.setLaunchProperty(Launcher.LAUNCH_MAIN, args[arg]);
                    }

                    break;
                case OPT_INTERNAL:
                    internal = true;
                    break;
                case OPT_VERSION:
                    showVersion = true;
                    break;                
                case OPT_LIBPATH:
                   setLibPath = true;
                   break;
                    
                case OPT_HELP:                   
                default:
                    showHelp = true;
                    arg = args.length;		// stop processing options
                    break;
            }
        }

        if (showVersion) {
            System.out.println("OpenJNLP version \"0.7.3_cenit\"");
        }

        if (showHelp) {
            System.out.println("Usage: org.nanode.jnlp.JNLPParser [-options] url [url...]");
            System.out.println();
            System.out.println("where options include:");
            System.out.println("    -help      print this help message");
            System.out.println("    -version   print product version");
            System.out.println("    -internal  launch url(s) in the current JVM");
            System.out.println();
            System.out.println("    -extcmd <cmd>");
            System.out.println("               command to use to invoke external launching");
            //System.out.println("    -extclasspath <jar files separated by " + File.pathSeparator + ">");
            //System.out.println("               jars to add to classpath for external launching");
            System.out.println("    -extmain <classname>");
            System.out.println("               classname to call for external launching");
            System.out.println("    -setlibpath");
            System.out.println("               set the libpath to the shared libpath download directory");                        
        }

        List urlList = new ArrayList();

        for (int i = arg; i < args.length; i++) {
            try {
                urlList.add(new URL(args[i]));
            } catch (MalformedURLException e) {
                System.err.println("ignoring bad url: " + args[i]);
            }
        }

        if (urlList.size() > 0) {
            try {
                Cache cache = Cache.getDefaultCache();		// use default cache for launching

                launchJNLP(cache, (URL[]) urlList.toArray(new URL[0]), internal,
                         setLibPath);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }


    static class URLJNLPParser extends JNLPParser {
        URL srcURL;
        boolean strictParsing;


        URLJNLPParser(Cache cache, URL src) {
            this.cache = cache;
            srcURL = src;
        }

        public URL getSourceURL() {
            return srcURL;
        }

        protected InputStream openInputStream() throws IOException {
            URLConnection uc = srcURL.openConnection();

            // if using http and not an OK, throw error
            if (uc instanceof HttpURLConnection) {
                HttpURLConnection huc = (HttpURLConnection) uc;

                if (huc.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException(huc.getResponseMessage());
                }
            }

            // if strict parsing and mime type or extension don't match, throw error
            if (strictParsing && !JNLP_MIME_TYPE.equals(JNLPParser.mediaFromContentType(uc.getContentType()))) {
                throw new IOException("Bad MIME type: " + uc.getContentType());
            }

            return srcURL.openStream();
        }
    }


    static class CachedJNLPParser extends JNLPParser {
        CacheEntry cacheEntry;


        CachedJNLPParser(CacheEntry entry) {
            this.cache = entry.getCache();
            cacheEntry = entry;
        }

        public URL getSourceURL() {
            URL srcURL = null;

            try {
                srcURL = new URL(cacheEntry.getMetaInfo(CacheEntry.METAKEY_DESCRIPTOR));
            } catch (MalformedURLException e) {
            }

            return srcURL;
        }

        public synchronized void parse() throws ParseException {
            if (this.isParsed()) {
                return;
            }

            super.parse();

            cacheEntry.setDescriptor(this.getDescriptor());
        }

        protected InputStream openInputStream() throws IOException {
            URL url = getSourceURL();

            if (url == null) {
                throw new IOException("no descriptor defined for " + cacheEntry.getTitle());
            }

            Reference ref = cacheEntry.referenceFromURL(url);
            CachedResource res = null;

            try {
                // if descriptor not cached yet, add it to cache
                if (!cacheEntry.isResourceCached(ref) && !cacheEntry.addResource(ref)) {
                    throw new IOException("failed to add descriptor to cache for " + cacheEntry.getTitle());
                }

                if ((res = cacheEntry.getResource(ref, true)) == null) {
                    throw new IOException("descriptor not in cache");
                }
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

            return res.openCacheInputStream();
        }
    }
}
