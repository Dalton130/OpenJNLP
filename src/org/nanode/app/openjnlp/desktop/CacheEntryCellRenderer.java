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

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;

import org.nanode.launcher.Descriptor;
import org.nanode.launcher.cache.CacheEntry;


/**
 * Renders a CacheEntry in a cell for a JList
 */
public class CacheEntryCellRenderer extends DefaultListCellRenderer {
    CacheEntryCellRenderer() {
        super();
        //setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
        if (index == -1) {
            int selected = list.getSelectedIndex();

            if (selected == -1) {
                return this;
            }

            index = selected;
        }

        try {
            Descriptor des = ((CacheEntry) value).getDescriptor();
            String text = (des != null) ? des.getInformation().getTitle() : ((CacheEntry) value).getTitle();
    
            setText(text);
        } catch (Exception e) {
            setText(value.toString());
        }

        //setComponentOrientation(list.getComponentOrientation());
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        setBorder((hasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

        return this;
    }
}
