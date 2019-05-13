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
package org.nanode.app.openjnlp.desktop;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.nanode.jnlp.JNLPParser;
import org.nanode.launcher.*;
import org.nanode.launcher.cache.CacheEntry;


/**
 * A DetailFrame will display detail about a launchable descriptor.
 */
public class DetailFrame extends JFrame implements ActionListener {
    protected static final Font titleFont = new Font("SansSerif", Font.PLAIN, 14);


    JLabel titleLabel;
    IconLabel iconLabel;
    DetailPane detail;

    CacheEntry entry;
    
    
    public DetailFrame() {
        super();
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        JPanel content = (JPanel) getContentPane();
        content.setLayout(new BorderLayout());

        content.add(createTitlePanel(), BorderLayout.NORTH);
        content.add(detail = new DetailPane(), BorderLayout.CENTER);

        setTitle("Info");

        // put a pretty picture in the corner of the window and/or in tool trays (depending on OS)
        ImageIcon i = IconFactory.getIcon(IconFactory.class.getResource("images/icon32x32.jpg"), IconFactory.NATURAL_SIZE);
        setIconImage(i.getImage());

        //setSize(275, 350);
        setResizable(false);
        pack();
    }

    public DetailFrame(CacheEntry entry) {
        this();

        setCacheEntry(entry);
    }

    public void actionPerformed(ActionEvent e) {
    }

    public void setCacheEntry(final CacheEntry entry) {
        Runnable runner =  new Runnable() {
            public void run() {
                if ((DetailFrame.this.entry = entry) == null) {
                    setTitle("Info");
                    titleLabel.setText("");
                    iconLabel.setDescriptor(null);
                } else {
                    Descriptor des = null;

                    try {
                        des = JNLPParser.getEntryDescriptor(entry);
                    } catch (Exception e) {
                    }

                    String title = (des != null && des.getInformation() != null) ? des.getInformation().getTitle() : entry.getTitle();

                    iconLabel.setDescriptor(des);
                    setTitle(title + " Info");
                    titleLabel.setText(title);

                }

                detail.setCacheEntry(entry);
            }
        };

        try {
            if (SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(runner);
            } else {
                SwingUtilities.invokeAndWait(runner);
            }
        } catch (Exception e) {
            // ignore exception
        }
    }

    /**
     * Creates title panel for information on cache entry
     */
    protected JPanel createTitlePanel() {
        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        jp.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.insets.left = 10;
        gbc.insets.top = 5;
        gbc.insets.bottom = 3;
        jp.add(iconLabel = new IconLabel(IconFactory.HUGE_SIZE), gbc);

        // set up app title
        titleLabel = new JLabel("");
        titleLabel.setFont(titleFont);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = 7;
        gbc.insets.right = 10;
        gbc.weightx = 1.0;
        jp.add(titleLabel, gbc);

        return jp;
    }
}
