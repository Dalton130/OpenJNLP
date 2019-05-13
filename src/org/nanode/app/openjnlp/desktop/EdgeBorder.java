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
package org.nanode.app.openjnlp.desktop;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;


/**
 * Border that draws a line along one edge.
 */
public class EdgeBorder extends AbstractBorder {
    int lineEdge;
    Color lineColor;
    int lineWidth;
    int edgeGap;


    EdgeBorder(int edge, Color color) {
        this(edge, color, 1, 3);
    }

    EdgeBorder(int edge, Color color, int thickness, int gap) {
        super();

        lineEdge = edge;
        lineColor = color;
        lineWidth = thickness;
        edgeGap = gap;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Color oldColor = g.getColor();
        Insets insets = getBorderInsets(c);

        g.setColor(lineColor);

        switch (lineEdge) {
            case SwingConstants.LEFT:
                g.fillRect(x, y, insets.left - edgeGap, h);
                break;
            case SwingConstants.RIGHT:
                g.fillRect(x + w - insets.right + edgeGap, y, insets.right - edgeGap, h);
                break;
            case SwingConstants.TOP:
                g.fillRect(x, y, w, insets.top - edgeGap);
                break;
            case SwingConstants.BOTTOM:
            default:
                g.fillRect(x, y + h - insets.bottom + edgeGap, w, insets.bottom - edgeGap);
                break;
        }

        g.setColor(oldColor);
    }

    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        switch (lineEdge) {
            case SwingConstants.LEFT:
                insets.left = lineWidth + edgeGap;
                break;
            case SwingConstants.RIGHT:
                insets.right = lineWidth + edgeGap;
                break;
            case SwingConstants.TOP:
                insets.top = lineWidth + edgeGap;
                break;
            case SwingConstants.BOTTOM:
            default:
                insets.bottom = lineWidth + edgeGap;
                break;
        }

        return insets;
    }

    public boolean isBorderOpaque() {
        return true;
    }
}
