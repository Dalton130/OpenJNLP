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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.nanode.launcher.Descriptor;


/**
 * An image-based label that is a fixed, square size. The image displayed is an icon that is centered
 * within the bounding square that is the icon label. The icon is retrieved from the supplied
 * descriptor.
 * <p>
 * A special property of an icon label is that it will initially use a default icon image and will
 * build the desired icon in a separate thread, updating itself to the desired icon after it is
 * successfully retrieved from the icon factory.
 *
 * @author Kevin Herrboldt (kevin@nanode.org)
 */
public class IconLabel extends JComponent {
    /** The descriptor that is the source of the icon. */
    protected Descriptor descriptor;

    /** Bounding square of the icon; used as the size of the component. */
    protected Dimension iconSize;

    /** The icon being displayed in this component. */
    protected ImageIcon icon;


    /**
     * Creates an icon label component of the specified size with no descriptor. This will display
     * a default icon.
     *
     * @param   size  the size of the icon label, used for both height and width
     */
    public IconLabel(int size) {
        this(null, size);
    }

    /**
     * Creates an icon label component of the specified size that will get the icon from the
     * specified descriptor. A default icon will be initially used until the desired icon is available.
     *
     * @param   des   the descriptor source of the icon
     * @param   size  the size of the icon label, used for both height and width
     */
    public IconLabel(Descriptor des, int size) {
        super();

        descriptor = des;
        iconSize = new Dimension(size, size);

        icon = IconFactory.getDefaultIcon(size);		// initialize to default icon
        updateIcon();
    }

    /**
     * Returns minimum size of this component.
     *
     * @return  minimum size of this component
     */
    public Dimension getMinimumSize() {
        return iconSize;
    }

    /**
     * Returns the preferred size of this component.
     *
     * @return  preferred size of this component
     */
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    /**
     * Returns the size of the icon bounding square. This is used for both the height and the width
     * for the dimension of the icon label component.
     *
     * @return  size of icon bounding square
     */
    public int getIconSize() {
        return iconSize.height;
    }

    /**
     * Returns the descriptor used to define the icon image.
     *
     * @return  the descriptor used to get the icon
     */
    public Descriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Changes the size of the icon label component to a new bounding square of the specified size.
     * This will cause a new icon image to be used that is scaled appropriately.
     *
     * @param   size  the new size for the bounding square
     */
    public void setIconSize(int size) {
        if (size == iconSize.height) {
            return;
        }

        iconSize.height = size;
        iconSize.width = size;

        // redraw with default icon of new size
        icon = IconFactory.getDefaultIcon(size);
        repaint();

        updateIcon();
    }

    /**
     * Changes the descriptor used to define the icon image. This will cause a new icon image to be
     * used that is scaled to the current icon size.
     *
     * @param   des  the new descriptor to use for defining the icon
     */
    public void setDescriptor(Descriptor des) {
        descriptor = des;

        updateIcon();
    }

    /**
     * Paints the contents of this component. Will paint the current icon image centered within the
     * component.
     *
     * @param   g  the graphics object to paint into
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (icon == null) {
            return;
        }

        Image image = icon.getImage();
        int x = getInsets().left + (getIconSize() - icon.getIconWidth()) / 2;
        int y = getInsets().top + (getIconSize() - icon.getIconHeight()) / 2;

        g.drawImage(image, x, y, this);
    }

    /**
     * Updates the icon image in a separate thread. Will repaint this component when the icon has
     * been updated. If the desired icon can not be retrieved a default icon will be substituted
     * from the icon factory.
     */
    protected void updateIcon() {
        final Descriptor des = descriptor;
        final int size = iconSize.height;

        final Runnable updater = new Runnable() {
            public void run() {
                if (des == null) {
                    icon = IconFactory.getDefaultIcon(size);
                } else {
                    icon = IconFactory.getIcon(des, size);
                }

                repaint();
            }
        };

        new Thread() {
            public void run() {
                try {
                    SwingUtilities.invokeAndWait(updater);
                } catch (Exception e) { }
            }
        }.start();
    }
}
