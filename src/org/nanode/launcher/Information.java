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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * Encapsulates all information about a descriptor. The information describes the reference but
 * is not used to invoke the reference.
 * <p>
 * Methods used to get various information elements return locale-specific elements unless otherwise
 * noted. Locale matching is done against the default locale using this algorithm:
 * <ol>
 * <li>look for match by (language, country, variant)</li>
 * <li>if no match, try matching on (language, country)</li>
 * <li>if no match, try matching on (language)</li>
 * <li>use default locale info if no other match</li>
 * </ol>
 *
 * @author Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 */
public class Information {
    /** default description */
    public static final String DESC_DEFAULT = null;

    /** one-line description */
    public static final String DESC_ONELINE = "one-line";

    /** short (single paragraph) description */
    public static final String DESC_SHORT = "short";

    /** tool-tip description */
    public static final String DESC_TOOLTIP = "tooltip";

    /** default icon information */
    public static final String ICON_DEFAULT = "default";

    /** information for selected icon */
    public static final String ICON_SELECTED = "selected";

    /** information for disabled icon */
    public static final String ICON_DISABLED = "disabled";

    /** information for rollover icon */
    public static final String ICON_ROLLOVER = "rollover";


    LocaleInfo defaultInfo;		// default LocaleInfo
    Map localeMap;				// (Locale, LocaleInfo)

    Locale vrntLocale;			// locale of (lang, country, variant) or null
    Locale ctryLocale;			// locale of (lang, country) or null
    Locale langLocale;			// locale of (lang) or null


    /**
     * Constructs new information object with the specified default locale info.
     */
    public Information(LocaleInfo defaultInfo) {
        this.defaultInfo = defaultInfo;

        localeMap = new HashMap();

        // set up the three permutations of locales
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

    /**
     * Identifies if off-line operation is supported. This is taken from from the default locale info.
     *
     * @return <code>true</code> if off-line is supported, otherwise <code>false</code>
     */
    public boolean allowOffline() {
        return defaultInfo.allowOffline;		// this ain't right, check locales
    }

    /**
     * Returns title from the default locale info.
     *
     * @return the title from the default locale info
     */
    public String getDefaultTitle() {
        return defaultInfo.title;
    }

    /**
     * Returns vendor from the default locale info.
     *
     * @return the vendor from the default locale info
     */
    public String getDefaultVendor() {
        return defaultInfo.vendor;
    }

    /**
     * Returns the default description for the current locale. This is equivalent to
     * <code>getDescription(null)</code>.
     *
     * @return default description for current locale
     */
    public String getDescription() {
        return getDescription(null);
    }

    /**
     * Returns description of the specified type for the current locale. If no description
     * is defined for the specified type, the default description will be returned.
     *
     * @param  type the type of description to return
     * @return specified description for current locale, or default description if not defined
     */
    public String getDescription(String type) {
        LocaleInfo li = getInfo(vrntLocale);
        String desc = (li != null) ? li.getDescription(type) : null;

        // if no description, check country locale
        if (desc == null) {
            li = getInfo(ctryLocale);
            desc = (li != null) ? li.getDescription(type) : null;
        }

        // if still no description, check language locale
        if (desc == null) {
            li = getInfo(langLocale);
            desc = (li != null) ? li.getDescription(type) : null;
        }

        return ((desc != null) ? desc : defaultInfo.getDescription(type));
    }

    /**
     * Returns the URL to the homepage for the current locale.
     *
     * @return homepage URL or <code>null</code> if not defined
     */
    public URL getHomepage() {
        LocaleInfo li = getInfo(vrntLocale);
        URL homepage = (li != null) ? li.homepage : null;

        // if no homepage, check country locale
        if (homepage == null) {
            li = getInfo(ctryLocale);
            homepage = (li != null) ? li.homepage : null;
        }

        // if still no homepage, check language locale
        if (homepage == null) {
            li = getInfo(langLocale);
            homepage = (li != null) ? li.homepage : null;
        }

        return ((homepage != null) ? homepage : defaultInfo.homepage);
    }

    /**
     * Returns default icon info for the current locale. This is equivalent to
     * <code>getIconInfo(ICON_DEFAULT)</code>.
     *
     * @return default icon info for current locale
     */
    public IconInfo getIconInfo() {
        return getIconInfo(ICON_DEFAULT);
    }

    /**
     * Returns icon info of the specified kind for the current locale. If no icon info is
     * defined, the default icon info will be returned.
     *
     * @param  kind which kind of icon info to get
     * @return icon info of the specified kind for the current locale
     */
    public IconInfo getIconInfo(String kind) {
        if (kind == null) {
            kind = ICON_DEFAULT;
        }

        LocaleInfo li = getInfo(vrntLocale);
        IconInfo iconi = (li != null) ? li.getIconInfo(kind) : null;

        // if no icon info, check country locale
        if (iconi == null) {
            li = getInfo(ctryLocale);
            iconi = (li != null) ? li.getIconInfo(kind) : null;
        }

        // if still no icon info, check language locale
        if (iconi == null) {
            li = getInfo(langLocale);
            iconi = (li != null) ? li.getIconInfo(kind) : null;
        }

        return ((iconi != null) ? iconi : defaultInfo.getIconInfo(kind));
    }

    /**
     * Returns title for the current locale.
     *
     * @return title for current locale
     */
    public String getTitle() {
        LocaleInfo li = getInfo(vrntLocale);
        String title = (li != null) ? li.title : null;

        // if no title found, check country locale
        if (title == null) {
            li = getInfo(ctryLocale);
            title = (li != null) ? li.title : null;
        }

        // if still no title, check language locale
        if (title == null) {
            li = getInfo(langLocale);
            title = (li != null) ? li.title : null;
        }

        return ((title != null) ? title : defaultInfo.title);
    }

    /**
     * Returns vendor for the current locale.
     *
     * @return vendor for current locale
     */
    public String getVendor() {
        LocaleInfo li = getInfo(vrntLocale);
        String vendor = (li != null) ? li.vendor : null;

        // if no vendor found, check country locale
        if (vendor == null) {
            li = getInfo(ctryLocale);
            vendor = (li != null) ? li.vendor : null;
        }

        // if still no vendor, check language locale
        if (vendor == null) {
            li = getInfo(langLocale);
            vendor = (li != null) ? li.vendor : null;
        }

        return ((vendor != null) ? vendor : defaultInfo.vendor);
    }

    /**
     * Sets locale-specific info.
     * <p>
     * If the <code>info</code> is null, this <code>locale</code> will cleared. If <code>locale</code> has
     * been previously set it will be mapped to this <code>info</code>, losing any previous values.
     *
     * @param  locale  the locale for which the info applies
     * @param  info    the locale-specific info
     */
    public synchronized void setLocaleInfo(Locale locale, LocaleInfo info) {
        if (info == null) {
            localeMap.remove(locale);
        } else {
            localeMap.put(locale, info);
        }
    }

    /**
     * Returns the locales defined in this information.
     *
     * @return a set of locales as Locale objects.
     */
    public Set locales() {
        return localeMap.keySet();
    }

    /**
     * Returns locale info for specified locale or <code>null</code> if not defined.
     *
     * @return matched locale info or <code>null</code> if no match on locale
     */
    LocaleInfo getInfo(Locale loc) {
        return ((loc != null) ? (LocaleInfo) localeMap.get(loc) : null);
    }


    /**
     * Returns an array of locales after parsing as a list of locales each separated by a space.
     * If the locales argument is null or empty string then an empty list is returned, indicating
     * default locale.
     *
     * @param  locales  string of locales separated by spaces
     * @return list of locale objects - an empty list indicated default locale
     */
    public static Locale[] parseLocales(String locales) {
        if (locales == null || locales.length() == 0) {
            return new Locale[0];
        }

        ArrayList list = new ArrayList();
        StringTokenizer st = new StringTokenizer(locales, " ");

        while (st.hasMoreTokens()) {
            StringTokenizer st0 = new StringTokenizer(st.nextToken(), "_");
            String language = st0.hasMoreTokens() ? st0.nextToken() : "";
            String country = st0.hasMoreTokens() ? st0.nextToken() : "";
            String variant = st0.hasMoreTokens() ? st0.nextToken() : null;

            Locale loc = (variant == null) ? new Locale(language, country) : new Locale(language, country, variant);
            list.add(loc);
        }

        return (Locale[]) list.toArray(new Locale[0]);
    }


    /**
     * Locale-specific information about a descriptor. Encapsulates all information for a reference for a
     * particular locale, including any icon resource information.
     */
    public static class LocaleInfo {
        String title;
        String vendor;
        URL homepage;
        boolean allowOffline;

        Map descriptions;
        Map icons;


        /**
         * Constructs locale info with the specified values.
         *
         * @param title     title of the reference
         * @param vendor    vendor of the reference
         * @param desc      a map of descriptions assumed to be of (type, description)
         * @param homepage  URL of homepage for the reference
         * @param icon      a map of icon infos assumed to be of (kind, icon info)
         * @param offline   indicates off-line support
         */
        public LocaleInfo(String title, String vendor, Map desc, URL homepage, Map icon, boolean offline) {
            this.title = title;
            this.vendor = vendor;
            this.homepage = homepage;
            allowOffline = offline;

            descriptions = (desc != null) ? new HashMap(desc) : new HashMap();
            icons = (icon != null) ? new HashMap(icon) : new HashMap();
        }

        /**
         * Returns description of the specified type. If no description is defined for the
         * specified type, the default description will be returned.
         *
         * @param  type the type of description to return
         * @return specified description or default description if not defined
         */
        public String getDescription(String type) {
            String desc = (String) descriptions.get(type);

            return ((desc != null) ? desc : (String) descriptions.get(DESC_DEFAULT));
        }

        /**
         * Returns icon info of the specified kind. If no icon info is defined of the
         * specified kind, the default icon info will be returned.
         *
         * @param  kind the kind of icon info to return
         * @return specified icon info or default icon info if not defined
         */
        public IconInfo getIconInfo(String kind) {
            IconInfo iconi = (IconInfo) icons.get(kind);

            return ((iconi != null) ? iconi : (IconInfo) icons.get(ICON_DEFAULT));
        }

        /**
         * Returns the title of the reference.
         *
         * @return title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Returns the name of the vendor.
         *
         * @return vendor
         */
        public String getVendor() {
            return vendor;
        }
    }


    /**
     * Information about an icon resource. The data for the icon image are not included; a
     * URL to the data is specified instead.
     */
    public static class IconInfo {
        Reference iconRef;
        int iconWidth;
        int iconHeight;
        int iconDepth;
        int size;


        /**
         * Creates icon info with the specified values.
         *
         * @param  ref     reference to the icon
         * @param  ver     version-id of the icon data (used with the URL for version-based retrieval)
         * @param  width   width in pixels of icon or -1 if undefined
         * @param  height  height in pixels of icon or -1 if undefined
         * @param  depth   color depth of icon or -1 if undefined
         * @param  size    size in bytes of icon data or -1 if undefined
         */
        public IconInfo(Reference ref, int width, int height, int depth, int size) {
            iconRef = ref;
            iconWidth = width;
            iconHeight = height;
            iconDepth = depth;
            this.size = size;
        }

        /**
         * Returns the reference to the icon.
         *
         * @return icon reference
         */
        public Reference getIconReference() {
            return iconRef;
        }

        /**
         * Returns the color depth of the icon.
         *
         * @return icon depth or -1 if not defined
         */
        public int getIconDepth() {
            return iconDepth;
        }

        /**
         * Returns the height in pixels of the icon.
         *
         * @return icon height or -1 if not defined
         */
        public int getIconHeight() {
            return iconHeight;
        }

        /**
         * Returns the width in pixels of the icon.
         *
         * @return icon width or -1 if not defined
         */
        public int getIconWidth() {
            return iconWidth;
        }

        /**
         * Returns the size of the icon data in bytes.
         *
         * @return icon size or -1 if not defined
         */
        public int getSize() {
            return size;
        }
    }
}
