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
package org.nanode.jnlp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.nanode.launcher.*;
import org.nanode.launcher.cache.Cache;


/**
 * Provides SAX2 Handler for parsing JNLP files.
 *
 * @author Kevin Herrboldt (<a href="mailto:kevin@nanode.org>kevin@nanode.org</a>)
 */
public class JNLPContentHandler extends DefaultHandler {
    /* these define all of the tags that can be handled. It is crucial that the list is in sorted order
       and that the numeric values represent the correct array value for each tag. */
    private static final String[] tags = {
        "all-permissions", "applet-desc", "application-desc", "argument",
        "component-desc", "description", "ext-download", "extension",
        "homepage", "icon", "information", "installer-desc",
        "j2ee-application-client-permissions", "j2se", "jar", "jnlp",
        "jre", "nativelib", "offline-allowed", "package",
        "param", "property", "resources", "security",
        "title", "vendor" };

    private static final int TAG_UNKNOWN = -1;
    private static final int TAG_ALL_PERMISSIONS = 0;
    private static final int TAG_APPLET_DESC = 1;
    private static final int TAG_APPLICATION_DESC = 2;
    private static final int TAG_ARGUMENT = 3;
    private static final int TAG_COMPONENT_DESC = 4;
    private static final int TAG_DESCRIPTION = 5;
    private static final int TAG_EXT_DOWNLOAD = 6;
    private static final int TAG_EXTENSION = 7;
    private static final int TAG_HOMEPAGE = 8;
    private static final int TAG_ICON = 9;
    private static final int TAG_INFORMATION = 10;
    private static final int TAG_INSTALLER_DESC = 11;
    private static final int TAG_J2EE_APPLICATION_CLIENT_PERMISSIONS = 12;
    private static final int TAG_J2SE = 13;
    private static final int TAG_JAR = 14;
    private static final int TAG_JNLP = 15;
    private static final int TAG_JRE = 16;
    private static final int TAG_NATIVELIB = 17;
    private static final int TAG_OFFLINE_ALLOWED = 18;
    private static final int TAG_PACKAGE = 19;
    private static final int TAG_PARAM = 20;
    private static final int TAG_PROPERTY = 21;
    private static final int TAG_RESOURCES = 22;
    private static final int TAG_SECURITY = 23;
    private static final int TAG_TITLE = 24;
    private static final int TAG_VENDOR = 25;


    Cache cache;
    Reference srcReference;

    Descriptor descriptor;
    JNLPSpecification jnlpSpec;

    String[] archs;
    String[] oses;
    Locale[] locales;
    boolean resMatch;

    // these can be set within <information> tags
    Map infoMap;			// list of all locale-specific info
    Locale defaultLocale;

    String title;
    String vendor;
    URL homepage;
    boolean offlineAllowed;
    Map descMap;
    Map iconMap;

    String descKind;	// set by <description> tag

    // these are set within <resources> tags
    Reference firstJar;
    Reference mainJar;

    Set jarSet;
    Set nativelibSet;
    Properties props;

    int curState;		// current processing state
    StringBuffer textBuffer;

    String curArch;
    String curOs;
    Locale vrntLocale;
    Locale ctryLocale;
    Locale langLocale;


    JNLPContentHandler(Cache cache, URL url) {
        this.cache = cache;
        srcReference = new Reference(url);

        textBuffer = new StringBuffer();

        infoMap = new HashMap();
        descMap = new HashMap();
        iconMap = new HashMap();

        jarSet = new HashSet();
        nativelibSet = new HashSet();

        curArch = System.getProperty("os.arch", "");
        curOs = System.getProperty("os.name", "");

        Locale loc = Locale.getDefault();

        if (loc.getVariant().length() > 0) {
            vrntLocale = loc;
            loc = new Locale(loc.getLanguage(), loc.getCountry());
        }

        if (loc.getCountry().length() > 0) {
            ctryLocale = loc;
            loc = new Locale(loc.getLanguage(), "");
        }

        langLocale = loc;
    }
    
    Descriptor getDescriptor() {
        return descriptor;
    }

    public void startDocument() throws SAXException {
        curState = TAG_UNKNOWN;
        descriptor = null;
        jnlpSpec = null;
    }

    public void endDocument() throws SAXException {
    }

    public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attrs) throws SAXException {
        switch (Arrays.binarySearch(tags, localName)) {
            case TAG_ALL_PERMISSIONS:	tagAllPermissions(attrs);	break;
            case TAG_APPLET_DESC:		tagAppletDesc(attrs);		break;
            case TAG_APPLICATION_DESC:	tagApplicationDesc(attrs);	break;
            case TAG_ARGUMENT:			tagArgument(attrs);			break;
            case TAG_COMPONENT_DESC:	tagComponentDesc(attrs);	break;
            case TAG_DESCRIPTION:		tagDescription(attrs);		break;
            case TAG_EXT_DOWNLOAD:		tagExtDownload(attrs);		break;
            case TAG_EXTENSION:			tagExtension(attrs);		break;
            case TAG_HOMEPAGE:			tagHomepage(attrs);			break;
            case TAG_ICON:				tagIcon(attrs);				break;
            case TAG_INFORMATION:		tagInformation(attrs);		break;
            case TAG_INSTALLER_DESC:	tagInstallerDesc(attrs);	break;
            case TAG_J2EE_APPLICATION_CLIENT_PERMISSIONS: tagJ2EEApplicationClientPermissions(attrs); break;
            case TAG_J2SE:				tagJ2SE(attrs);				break;
            case TAG_JAR:				tagJar(attrs);				break;
            case TAG_JNLP:				tagJNLP(attrs);				break;
            case TAG_JRE:				tagJRE(attrs);				break;
            case TAG_NATIVELIB:			tagNativelib(attrs);		break;
            case TAG_OFFLINE_ALLOWED:	tagOfflineAllowed(attrs);	break;
            case TAG_PACKAGE:			tagPackage(attrs);			break;
            case TAG_PARAM:				tagParam(attrs);			break;
            case TAG_PROPERTY:			tagProperty(attrs);			break;
            case TAG_RESOURCES:			tagResources(attrs);		break;
            case TAG_SECURITY:			tagSecurity(attrs);			break;
            case TAG_TITLE:				tagTitle(attrs);			break;
            case TAG_VENDOR:			tagVendor(attrs);			break;
            default:
                //throw new SAXException("unknown start tag " + localName);
        }
    }

    public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
        switch (Arrays.binarySearch(tags, localName)) {
            case TAG_ALL_PERMISSIONS:	tagAllPermissions(null);	break;
            case TAG_APPLET_DESC:		tagAppletDesc(null);		break;
            case TAG_APPLICATION_DESC:	tagApplicationDesc(null);	break;
            case TAG_ARGUMENT:			tagArgument(null);			break;
            case TAG_COMPONENT_DESC:	tagComponentDesc(null);		break;
            case TAG_DESCRIPTION:		tagDescription(null);		break;
            case TAG_EXT_DOWNLOAD:		tagExtDownload(null);		break;
            case TAG_EXTENSION:			tagExtension(null);			break;
            case TAG_HOMEPAGE:			tagHomepage(null);			break;
            case TAG_ICON:				tagIcon(null);				break;
            case TAG_INFORMATION:		tagInformation(null);		break;
            case TAG_INSTALLER_DESC:	tagInstallerDesc(null);		break;
            case TAG_J2EE_APPLICATION_CLIENT_PERMISSIONS: tagJ2EEApplicationClientPermissions(null); break;
            case TAG_J2SE:				tagJ2SE(null);				break;
            case TAG_JAR:				tagJar(null);				break;
            case TAG_JNLP:				tagJNLP(null);				break;
            case TAG_JRE:				tagJRE(null);				break;
            case TAG_NATIVELIB:			tagNativelib(null);			break;
            case TAG_OFFLINE_ALLOWED:	tagOfflineAllowed(null);	break;
            case TAG_PACKAGE:			tagPackage(null);			break;
            case TAG_PARAM:				tagParam(null);				break;
            case TAG_PROPERTY:			tagProperty(null);			break;
            case TAG_RESOURCES:			tagResources(null);			break;
            case TAG_SECURITY:			tagSecurity(null);			break;
            case TAG_TITLE:				tagTitle(null);				break;
            case TAG_VENDOR:			tagVendor(null);			break;
            default:
                //throw new SAXException("unknown end tag " + localName);
        }
    }

    public void characters(char[] text, int start, int length) throws SAXException {
        int whiteRun = 0;

        for (int i = start; i < start + length; i++) {
            if (Character.isWhitespace(text[i])) {
                whiteRun++;
            } else {
                if (whiteRun > 0) {
                    textBuffer.append(' ');	// collapse whitespace run into single space
                    whiteRun = 0;			// reset white runlength, this span has been dealt with
                }

                textBuffer.append(text[i]);
            }
        }
    }

    public void ignorableWhitespace(char[] text, int start, int length) throws SAXException {
        characters(text, start, length);
    }

    protected String text() {
        int start = 0;
        int end = textBuffer.length();

        // trim trailing whitespace
        while (end > 0 && Character.isWhitespace(textBuffer.charAt(end - 1))) {
            --end;
        }

        // trim leading whitespace
        while (start < end && Character.isWhitespace(textBuffer.charAt(start))) {
            start++;
        }

        return textBuffer.substring(start, end);
    }

    protected void tagAllPermissions(Attributes attrs) throws SAXException {
        changeState(TAG_ALL_PERMISSIONS, (attrs != null), TAG_SECURITY);
    }

    protected void tagAppletDesc(Attributes attrs) throws SAXException {
        changeState(TAG_APPLET_DESC, (attrs != null), TAG_JNLP);

        if (attrs != null) {
            String name = attrs.getValue("name");

            if (name == null) {
                throw new SAXException("applet-desc requires name to be defined");
            }

            try {
                int width = Integer.parseInt(attrs.getValue("width"));
                int height = Integer.parseInt(attrs.getValue("height"));

                descriptor = new ApplicationDescriptor(cache, jnlpSpec.getCodebase(),
                                                       (jnlpSpec.getReference() != null) ? jnlpSpec.getReference() : srcReference,
                                                       attrs.getValue("main-class"), name, width, height,
                                                       convertToURL(attrs.getValue("documentbase")));
            } catch (Exception e) {
                throw new SAXException(e.toString());
            }
        }
    }

    protected void tagApplicationDesc(Attributes attrs) throws SAXException {
        changeState(TAG_APPLICATION_DESC, (attrs != null), TAG_JNLP);

        if (attrs != null) {
            descriptor = new ApplicationDescriptor(cache, jnlpSpec.getCodebase(),
                                                   (jnlpSpec.getReference() != null) ? jnlpSpec.getReference() : srcReference,
                                                   attrs.getValue("main-class"));
        }
    }

    protected void tagArgument(Attributes attrs) throws SAXException {
        changeState(TAG_ARGUMENT, (attrs != null), TAG_APPLICATION_DESC);

        if (attrs != null) {
            textBuffer.setLength(0);
        } else {
            if (descriptor instanceof ApplicationDescriptor) {
                ((ApplicationDescriptor) descriptor).addArgument(text());
            }
        }
    }

    protected void tagComponentDesc(Attributes attrs) throws SAXException {
        changeState(TAG_COMPONENT_DESC, (attrs != null), TAG_JNLP);
    }

    protected void tagDescription(Attributes attrs) throws SAXException {
        changeState(TAG_DESCRIPTION, (attrs != null), TAG_INFORMATION);

        if (attrs != null) {
            descKind = attrs.getValue("kind");		// default description should have null for kind
            textBuffer.setLength(0);
        } else {
            descMap.put(descKind, text());
        }
    }

    protected void tagExtDownload(Attributes attrs) throws SAXException {
        changeState(TAG_EXT_DOWNLOAD, (attrs != null), TAG_EXTENSION);
    }

    protected void tagExtension(Attributes attrs) throws SAXException {
        changeState(TAG_EXTENSION, (attrs != null), TAG_RESOURCES);
    }

    protected void tagHomepage(Attributes attrs) throws SAXException {
        changeState(TAG_HOMEPAGE, (attrs != null), TAG_INFORMATION);

        if (attrs != null) {
            homepage = convertToURL(attrs.getValue("href"));
        }
    }

    protected void tagIcon(Attributes attrs) throws SAXException {
        changeState(TAG_ICON, (attrs != null), TAG_INFORMATION);

        if (attrs != null) {
            Information.IconInfo iconi = null;
            URL href = convertToURL(attrs.getValue("href"));
            String kind = attrs.getValue("kind");

            if (kind == null) {
                kind = "default";
            }

            try {
                String s;

                iconi = new Information.IconInfo(new Reference(href, Version.parseVersions(attrs.getValue("version")), false),
                                                 ((s = attrs.getValue("width")) != null) ? Integer.parseInt(s) : -1,
                                                 ((s = attrs.getValue("height")) != null) ? Integer.parseInt(s) : -1,
                                                 ((s = attrs.getValue("depth")) != null) ? Integer.parseInt(s) : -1,
                                                 ((s = attrs.getValue("size")) != null) ? Integer.parseInt(s) : -1);
            } catch (NumberFormatException e1) {
                throw new SAXException("bad numeric value for icon");
            }

            iconMap.put(kind, iconi);		// default icon will have a kind of null
        }
    }

    protected void tagInformation(Attributes attrs) throws SAXException {
        changeState(TAG_INFORMATION, (attrs != null), TAG_JNLP);

        if (attrs != null) {
            locales = Information.parseLocales(attrs.getValue("locale"));

            title = null;
            vendor = null;
            homepage = null;
            descKind = null;
            offlineAllowed = false;

            descMap.clear();
            iconMap.clear();
        } else {
            Information.LocaleInfo li = new Information.LocaleInfo(title, vendor, descMap, homepage, iconMap, offlineAllowed);

            if (locales.length == 0) {
                infoMap.put(null, li);
            } else {
                // in case default <information> not defined, remember first locale in file as default
                if (defaultLocale == null) {
                    defaultLocale = locales[0];
                }

                // add information to map for each locale specified
                for (int i = 0; i < locales.length; i++) {
                    infoMap.put(locales[i], li);
                }
            }
        }
    }

    protected void tagInstallerDesc(Attributes attrs) throws SAXException {
        changeState(TAG_INSTALLER_DESC, (attrs != null), TAG_JNLP);
    }

    protected void tagJ2EEApplicationClientPermissions(Attributes attrs) throws SAXException {
        changeState(TAG_J2EE_APPLICATION_CLIENT_PERMISSIONS, (attrs != null), TAG_SECURITY);
    }

    protected void tagJ2SE(Attributes attrs) throws SAXException {
        changeState(TAG_J2SE, (attrs != null), TAG_RESOURCES);
    }

    protected void tagJar(Attributes attrs) throws SAXException {
        changeState(TAG_JAR, (attrs != null), TAG_RESOURCES);

        if (attrs == null || !resMatch) {
            return;
        }

        URL href = convertToURL(attrs.getValue("href"));
        Reference jarRef = new Reference(href, Version.parseVersions(attrs.getValue("version")), "lazy".equals(attrs.getValue("download")));

        if ("true".equals(attrs.getValue("main"))) {
            if (mainJar != null) {
                throw new SAXException("more than one jar defined as main");
            }

            mainJar = jarRef;
        }
        
        if (firstJar == null) {
            firstJar = jarRef;
        }

        jarSet.add(jarRef);
    }

    protected void tagJNLP(Attributes attrs) throws SAXException {
        changeState(TAG_JNLP, (attrs != null), TAG_UNKNOWN);

        if (attrs != null) {
            /* This will parse attributes and create the JNLPSpecification.
             * It will also create the parts for the descriptor. */
            URL codebase = null;
            URL href = null;
            String s;

            // get codebase if defined
            if ((s = fixCodebaseString(attrs.getValue("codebase"))) != null) {
                try {
                    codebase = new URL(s);
                } catch (MalformedURLException e) {
                    throw new SAXException("bad codebase in <jnlp>");
                }
            }

            // get href if defined, could be relative to codebase
            if ((s = attrs.getValue("href")) != null) {
                try {
                    href = (codebase == null) ? new URL(s) : new URL(codebase, s);
                } catch (MalformedURLException e) {
                    throw new SAXException("bad href in <jnlp>");
                }
            }

            String spec = attrs.getValue("spec");
            String vers = attrs.getValue("version");
            Reference srcRef = (href != null) ? new Reference(href, (vers != null) ? Version.parseVersions(vers) : null, false)
                                              : null;

            jnlpSpec = new JNLPSpecification(srcRef, codebase,
                                             (spec != null) ? Version.parseVersions(spec) : null);

            infoMap.clear();

            jarSet.clear();
            nativelibSet.clear();
            props = null;				// no properties

            resMatch = false;
            firstJar = null;
            mainJar = null;
        } else {
            // jnlp tag closed, build the descriptor out of its parts
            Information.LocaleInfo def = (Information.LocaleInfo) infoMap.get(null);

            if (def == null) {
                def = (Information.LocaleInfo) infoMap.get(defaultLocale);

                // all JNLP descriptors must define vendor and title, so throw exception if not true
                if (def == null || def.getVendor() == null || def.getTitle() == null) {
                    throw new SAXException("No default information defined");
                }
            }

            Information information = new Information(def);

            for (Iterator iter = infoMap.keySet().iterator(); iter.hasNext();) {
                Locale locale = (Locale) iter.next();

                information.setLocaleInfo(locale, (Information.LocaleInfo) infoMap.get(locale));
            }

            Resources resources = new Resources();
            resources.setProperties(props);				// set props (or null if none)

            if (mainJar != null || firstJar != null) {
                resources.setMainJar((mainJar != null) ? mainJar : firstJar);
            } else {
                throw new SAXException("no main jar defined");
            }

            for (Iterator iter = jarSet.iterator(); iter.hasNext();) {
                resources.addReference((Reference) iter.next());
            }

            for (Iterator iter = nativelibSet.iterator(); iter.hasNext();) {
                resources.addReference((Reference) iter.next());
            }

            descriptor.setContext(jnlpSpec);
            descriptor.setInformation(information);
            descriptor.setResources(resources);
        }
    }

    protected void tagJRE(Attributes attrs) throws SAXException {
        changeState(TAG_JRE, (attrs != null), TAG_RESOURCES);
    }

    protected void tagNativelib(Attributes attrs) throws SAXException {
        changeState(TAG_NATIVELIB, (attrs != null), TAG_RESOURCES);

        if (attrs == null || !resMatch) {
            return;
        }

        URL href = convertToURL(attrs.getValue("href"));
        Reference libRef = new NativelibReference(href, Version.parseVersions(attrs.getValue("version")), "lazy".equals(attrs.getValue("download")));

        nativelibSet.add(libRef);
    }

    protected void tagOfflineAllowed(Attributes attrs) throws SAXException {
        changeState(TAG_OFFLINE_ALLOWED, (attrs != null), TAG_INFORMATION);

        if (attrs != null) {
            offlineAllowed = true;
        }
    }

    protected void tagPackage(Attributes attrs) throws SAXException {
        changeState(TAG_PACKAGE, (attrs != null), TAG_RESOURCES);
    }

    protected void tagParam(Attributes attrs) throws SAXException {
        changeState(TAG_PARAM, (attrs != null), TAG_APPLET_DESC);

        if (attrs != null) {
            try {
                ApplicationDescriptor desc = (ApplicationDescriptor) descriptor;

                desc.putParam(attrs.getValue("name"), attrs.getValue("value"));
            } catch (Exception e) {
                throw new SAXException(e.toString());
            }
        }
    }

    protected void tagProperty(Attributes attrs) throws SAXException {
        changeState(TAG_PROPERTY, (attrs != null), TAG_RESOURCES);

        if (attrs != null) {
            // if no properties yet, create it
            if (props == null) {
                props = new Properties();
            }

            props.setProperty(attrs.getValue("name"), attrs.getValue("value"));
        }
    }

    protected void tagResources(Attributes attrs) throws SAXException {
        changeState(TAG_RESOURCES, (attrs != null), TAG_JNLP);

        if (attrs == null) {
            return;
        }

        String locAttr = attrs.getValue("locale");
        String archAttr = attrs.getValue("arch");
        String osAttr = attrs.getValue("os");

        locales = Information.parseLocales(attrs.getValue("locale"));
        archs = Resources.parseKeys(attrs.getValue("arch"));
        oses = Resources.parseKeys(attrs.getValue("os"));

        // if no locales, archs or oses are specified, these are default resources and need to be added
        if ((resMatch = (locAttr == null && archAttr == null && osAttr == null))) {
            return;
        }

        // if arch specified, check for match
        if (archAttr != null) {
            int i;

            for (i = 0; i < archs.length; i++) {
                // if a match is found set flag and leave for loop early
                if (curArch.startsWith(archs[i])) {
                    resMatch = true;
                    break;
                }
            }

            // if no archs matched from those specified, don't add these resources
            if (i == archs.length) {
                resMatch = false;
                return;
            }
        }

        if (osAttr != null) {
            int i;

            for (i = 0; i < oses.length; i++) {
                // if a match is found set flag and leave for loop early
                if (curOs.startsWith(oses[i])) {
                    resMatch = true;
                    break;
                }
            }

            // if no archs matched from those specified, don't add these resources
            if (i == oses.length) {
                resMatch = false;
                return;
            }
        }

        if (locAttr != null) {
            int i;

            for (i = 0; i < locales.length; i++) {
                // if a match is found set flag and leave for loop early
                if (vrntLocale != null && vrntLocale.equals(locales[i])
                 || ctryLocale != null && ctryLocale.equals(locales[i])
                 || langLocale != null && langLocale.equals(locales[i])) {
                    resMatch = true;
                    break;
                }
            }

            // if no locales matched from those specified, don't add these resources
            if (i == locales.length) {
                resMatch = false;
                return;
            }
        }
    }

    protected void tagSecurity(Attributes attrs) throws SAXException {
        changeState(TAG_SECURITY, (attrs != null), TAG_JNLP);
    }

    protected void tagTitle(Attributes attrs) throws SAXException {
        changeState(TAG_TITLE, (attrs != null), TAG_INFORMATION);

        if (attrs != null) {
            textBuffer.setLength(0);
        } else {
            title = text();
        }
    }

    protected void tagVendor(Attributes attrs) throws SAXException {
        changeState(TAG_VENDOR, (attrs != null), TAG_INFORMATION);

        if (attrs != null) {
            textBuffer.setLength(0);
        } else {
            vendor = text();
        }
    }
    
    protected void changeState(int enterState, boolean entering, int parentState) throws SAXException {
        if (entering) {
            // entering state
            if (curState != parentState) {
                throw new SAXException("misplaced " + tags[enterState] + " tag");
            } else {
                curState = enterState;
            }
        } else {
            // leaving state
            if (curState != enterState) {
                throw new SAXException(tags[enterState] + " end tag with no start");
            } else {
                curState = parentState;
            }
        }
    }

    protected URL convertToURL(String href) throws SAXException {
        URL url = null;

        if (href != null) {
            try {
                url = (jnlpSpec != null) ? new URL(jnlpSpec.getCodebase(), href) : new URL(href);
            } catch (MalformedURLException e) {
                throw new SAXException(e.getMessage());
            }
        }

        return url;
    }

    /**
     * This is a helper method to make sure a codebase string will work as a codebase URL
     * by forcing a "/" on the end of the string.
     */
    public static final String fixCodebaseString(String s) {
        // a string that ends with / is already fixed (do nothing if null)
        if (s == null || s.endsWith("/")) {
            return s;
        }

        return (s + "/");
    }
}
