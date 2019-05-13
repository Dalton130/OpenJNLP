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

import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

import org.nanode.launcher.Descriptor;
import org.nanode.launcher.Information;
import org.nanode.launcher.Reference;
import org.nanode.launcher.cache.CacheEntry;


/**
 * Factory class for vending icons of various sizes. When an icon is created at a specific size it
 * is cached and the shared icon will be returned for any future requests that match the icon and
 * size. This limits the number of images created due to repeated calls.
 *
 * @author Kevin Herrboldt (kevin@nanode.org)
 */
public class IconFactory {
    /** Corresponds to the icon's natural (unscaled) size. */    
    public static final int NATURAL_SIZE = -1;
    
    /** 128x128 pixels: traditional "thumbnail" or "preview" icon. */    
    public static final int THUMBNAIL_SIZE = 128;
    
    /** 64x64 pixels: really big icon for use in oversized tool trays, etc. */
    public static final int GIANT_SIZE = 64;
    
    /** 48x48 pixels: bigger version of the standard desktop icon. */    
    public static final int HUGE_SIZE = 48;
    
    /**
     * 32x32 pixels: commonly used size when "large" icons are selected in tool bars, menus,
     * and so forth.  Also useful for desktop icons.
     */    
    public static final int LARGE_SIZE = 32;
    
    /**
     * 16x16 pixels: smaller useful icon size; correct for window decorations, toolbars,
     * menus, and some tooltrays.
     */    
    public static final int SMALL_SIZE = 16;

    /**
     * Cache of shared icons at various images. The icon map is mapped with the (key, value)
     * pair of (</em>iconkey</em>, <em>Icon</em>).
     */
    protected static Map iconMap = new HashMap();

    /** Source url of default icon. */
    private static final URL defaultURL = IconFactory.class.getResource("images/app.jpg");

    /** Default icon at natural size. */
    private static final ImageIcon defaultIcon = new ImageIcon(defaultURL);


    /**
     * Prevents use of this as an object; this is a singleton factory.
     */    
    private IconFactory() {
    }


    /**
     * Returns the default icon scaled to the specified size. This method uses the icon cache
     * so icons returned will be shared with other requests for the same icon and scaling.
     *
     * @param   size  Indicates what scaling should be applied to the icon
     * @return  the desired scaled icon
     * @see     javax.swing.Icon
     */    
    public static ImageIcon getDefaultIcon(int size) {
        String key = Integer.toString(size);
        ImageIcon icon = (ImageIcon) iconMap.get(key);

        if (icon == null) {
            // icon not found, so synchronize on the cache and check again
            synchronized (iconMap) {
                if ((icon = (ImageIcon) iconMap.get(key)) == null) {
                    icon = createScaledIcon(defaultIcon, size);
                    iconMap.put(key, icon);
                }
            }
        }

        return icon;
    }

    /**
     * Returns the icon corresponding to the supplied url in the specified size. If the icon does
     * not exist at the specified size it will be created and cached for future use. If the icon
     * url is inacessible a default icon at the specified size will be substituted.
     *
     * @param   url   url to the icon
     * @param   size  indicates the desired size of the icon
     * @return  an icon guaranteed to fit within a bounding square of the specified size
     */
    public static ImageIcon getIcon(URL url, int size) {
        String key = iconKey(url, size);
        ImageIcon icon = (ImageIcon) iconMap.get(key);

        if (icon == null) {
            ImageIcon naturalIcon = null;

            // if not asking for natural icon, get the natural version for this url
            if (size != NATURAL_SIZE) {
                naturalIcon = getIcon(url, NATURAL_SIZE);
            }

            // now synchronize and check once more for correct icon
            synchronized (iconMap) {
                if ((icon = (ImageIcon) iconMap.get(key)) == null && naturalIcon != null) {
                    // icon still not found but have natural icon, scale new icon and cache it
                    icon = createScaledIcon(naturalIcon, size);
                    iconMap.put(key, icon);
                }
            }
        }

        return (icon != null) ? icon : getDefaultIcon(size);
    }

    /**
     * Returns default icon from the specified descriptor at the specified size. This is equivalent
     * to calling <code>getIcon(des, Information.ICON_DEFAULT, size)</code>.
     *
     * @param   des   the descriptor
     * @param   size  desired size of the icon
     * @return  an icon guaranteed to fit within a bounding square of the specified size
     * @see     #getIcon(Descriptor, String, int)
     */
    public static ImageIcon getIcon(Descriptor des, int size) {
        return getIcon(des, Information.ICON_DEFAULT, size);
    }

    /**
     * Returns the icon from the specified descriptor of the specified type at the specified
     * size. If no icon of the type is defined than the default icon defined in the descriptor
     * will be used, scaled appropriately. If the descriptor's default icon is undefined or
     * not retrievable, a default icon will be substituted.
     * <p>
     * Any icon data retrieved from the descriptor is retrieved via the descriptor's cache entry.
     *
     * @param   des   the descriptor
     * @param   kind  kind of icon to get; <code>null</code> indicates default icon
     * @param   size  desired size of icon
     * @return  icon of the specified kind guaranteed to fit within a bounding square of the
     *          specified size
     */
    public static ImageIcon getIcon(Descriptor des, String kind, int size) {
        URL url = getIconURL(des, kind);

        // if no url defined for the desired icon, use a default icon
        if (url == null) {
            return getDefaultIcon(size);
        }

        // make sure natural icon of specified kind is in icon map via the cache entry
        String key = iconKey(url, NATURAL_SIZE);
        ImageIcon icon = (ImageIcon) iconMap.get(key);

        if (icon == null) {
            synchronized (iconMap) {
                if ((icon = (ImageIcon) iconMap.get(key)) == null) {
                    try {
                        // still no natural icon, create from cache
                        CacheEntry cacheEntry = des.getCacheEntry();
                        Reference ref = cacheEntry.referenceFromURL(url);

                        // load icon through cache
                        if (!cacheEntry.isResourceCached(ref)) {
                            cacheEntry.addResource(ref);
                        }

                        icon = new ImageIcon(cacheEntry.getResource(ref).getBytes());
                    } catch (Exception e) {
                        icon = null;
                    }

                    if (icon != null) {
                        iconMap.put(key, icon);
                    }
                }
            }
        }

        return getIcon(url, size);		// return icon scaled properly
    }

    /**
     * Get icon URL of the specified type from the specified descriptor. If there is no icon defined
     * for the specified type, will return the URL of the default icon. A <code>null</code> URL may
     * be returned if no default icon is defined.
     *
     * @param   des   the descriptor
     * @param   kind  the kind of icon to get
     * @return  URL to desired icon or <code>null</code> if no icon defined
     * @see     Information#getIconInfo(String)
     */    
    public static final URL getIconURL(Descriptor des, String kind) {
        Information.IconInfo ii = des.getInformation().getIconInfo(kind);

        // if no icon defined for type and type is not default, try to get default
        if (ii == null && !Information.ICON_DEFAULT.equals(kind)) {
            ii = des.getInformation().getIconInfo(Information.ICON_DEFAULT);
        }

        return ((ii != null) ? ii.getIconReference().getURL() : null);
    }

    /**
     * Returns a key string suitable for use in maps based on the specified URL and size. This key
     * is used to uniquely identify an icon at the given size.
     *
     * @param   url   url to icon; <code>null</code> is allowed
     * @param   size  desired size of icon
     * @return  a key string based on the specified parameters
     */    
    protected static final String iconKey(URL url, int size) {
        return Integer.toString(size) + ((url != null) ? url.toString() : "");
    }

    /**
     * Creates a scaled icon of the specified maximum size from the specified icon. The aspect ratio
     * is preserved as the icon is scaled. The scaled icon is guaranteed to fit within a bounding
     * square of the specified maximum size.
     * <p>
     * If the specified icon is smaller than the desired size it will be scaled larger.
     *
     * @param   icon     the original icon to scale
     * @param   maxSize  the maximum size to scale the icon to
     * @return  a scaled icon that fits within a bounding square of the specified size
     */
    protected static ImageIcon createScaledIcon(ImageIcon icon, int maxSize) {
        if (maxSize == NATURAL_SIZE) {
            return icon;
        }

        double scalar = 1.0;		// default is no change in size

        // compute the scalar based on the bigger of the width or height
        if (icon.getIconHeight() >= icon.getIconWidth()) {
            scalar = (double) maxSize / icon.getIconHeight();
        } else {
            scalar = (double) maxSize / icon.getIconWidth();
        }

        Image image = icon.getImage().getScaledInstance((int) (scalar * icon.getIconWidth()),
                                                        (int) (scalar * icon.getIconHeight()),
                                                        Image.SCALE_SMOOTH);

        return new ImageIcon(image);
    }
}
