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
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import edu.stanford.ejalbert.BrowserLauncher;

import org.nanode.jnlp.JNLPSpecification;
import org.nanode.launcher.*;
import org.nanode.launcher.cache.CacheEntry;
import org.nanode.launcher.cache.CachedResource;


/**
 * This class creates an intelligent pane that offers different
 * views of the resource information.
 * <p>
 * A DetailPane consists of two panels arranged vertically. The top
 * panel that indicates which view is visible as well as controls for
 * switching views.
 * <p>
 * The bottom panel uses <code>CardLayout</code> to provide multiple
 * views in the same space. Each view provides different information
 * about the same resource.
 */
public class DetailPane extends JPanel implements ActionListener {
    protected static final Font srcFont = new Font("Monospaced", Font.PLAIN, 10);
    protected static final Font infoFont = new Font("SansSerif", Font.PLAIN, 11);
    protected static final Font buttonFont = new Font("SansSerif", Font.PLAIN, 9);

    static final String[] optionStrings = { "General Information", "JNLP Source" };


    JComboBox cardList;
    JPanel cardPanel;
    CardLayout cards;
    JButton homeButton;
    Map infoMap;


    public DetailPane() {
        super();
        setLayout(new BorderLayout());

        infoMap = new HashMap();

        add(createSelectionPane(), BorderLayout.NORTH);
        add(cardPanel = new JPanel(), BorderLayout.CENTER);

        cardPanel.setLayout(cards = new CardLayout());

        cardPanel.add(createInformationPanel(), optionStrings[0]);
        cardPanel.add(createSourcePanel(), optionStrings[1]);

        homeButton.addActionListener(this);

        // information is the first panel to show
        cardList.addActionListener(this);
        cardList.setSelectedIndex(0);
        //dispatchEvent(new ActionEvent(buttons[0], ActionEvent.ACTION_PERFORMED, buttons[0].getActionCommand()));
    }

    public DetailPane(CacheEntry entry) {
        this();

        setCacheEntry(entry);
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == cardList) {
            cards.show(cardPanel, (String) cardList.getSelectedItem());
        } else if (src == homeButton) {
            final String url = e.getActionCommand();

            SwingWorker worker = new SwingWorker() {
                public Object construct() {
                    String openedURL = null;

                    try {
                        BrowserLauncher.openURL(url);
                        openedURL = url;
                    } catch (IOException e) {
                        // ignore error
                    }

                    return openedURL;
                }
            };

            worker.start();
        }
    }

    /**
     * Updates information display based on new cache entry
     */
    public void setCacheEntry(CacheEntry entry) {
        Descriptor descriptor = (entry != null) ? entry.getDescriptor() : null;	// it's assumed this has already be defined
        Information info = (descriptor != null) ? descriptor.getInformation() : null;
        CachedResource jnlpRes = null;
        JNLPSpecification jnlpSpec = null;

        String vendor = (info != null) ? info.getVendor() : null;
        String homepage = (info != null && info.getHomepage() != null) ? info.getHomepage().toString() : null;
        String desc = (info != null && info.getDescription(Information.DESC_SHORT) != null) ? info.getDescription(Information.DESC_SHORT) : "";
        //String where = null;

        if (entry != null && entry.getMetaInfo(CacheEntry.METAKEY_DESCRIPTOR) != null) {
            try {
                URL descURL = new URL(entry.getMetaInfo(CacheEntry.METAKEY_DESCRIPTOR));
                jnlpRes = entry.getResource(entry.referenceFromURL(descURL));
            } catch (Exception e) {
                // ignore error
            }
        }

        if (descriptor != null && descriptor.getContext() instanceof JNLPSpecification) {
            jnlpSpec = (JNLPSpecification) descriptor.getContext();
        }

        /*if (jnlpSpec != null && jnlpSpec.getReference().getURL() != null) {
            where = jnlpSpec.getReference().getURL().toString();
        }*/

        if (vendor == null && entry != null) {
            vendor = entry.getVendor();
        }

        String src = (jnlpRes != null) ? new String(jnlpRes.getBytes()) : "";

        // populate all display fields
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

        ((JLabel) infoMap.get("kind")).setText(getDescriptorKind(descriptor));
        ((JLabel) infoMap.get("modified")).setText((jnlpRes != null) ? df.format(new Date(jnlpRes.getLastModified())) : "");
        ((JLabel) infoMap.get("version")).setText((jnlpSpec != null) ? jnlpSpec.getReference().getVersions()[0].toString() : "");
        ((JLabel) infoMap.get("vendor")).setText((vendor != null) ? vendor : "");
        homeButton.setText(((info != null) ? info.getTitle() : "No") + " Homepage");

        if (homepage != null) {
            homeButton.setActionCommand(homepage);
            homeButton.setEnabled(true);
        } else {
            homeButton.setEnabled(false);
            homeButton.setActionCommand("");
        }

        JTextArea jta = (JTextArea) infoMap.get("description");
        jta.setText(desc);
        jta.setCaretPosition(0);

        jta = (JTextArea) infoMap.get("source");
        jta.setText(src);
        jta.setCaretPosition(0);
    }

    protected JPanel createSelectionPane() {
        JPanel jp = new JPanel();
        jp.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        jp.setOpaque(false);

        cardList = new JComboBox(optionStrings);
        jp.add(cardList);

        return jp;
    }
    
    /**
     * This creates the panel that displays general information about the CacheEntry
     */
    protected JPanel createInformationPanel() {
        // create fields that contain changeable information
        infoMap.put("kind", createInfoLabel(getDescriptorKind(null)));
        infoMap.put("modified", createInfoLabel(""));
        infoMap.put("version", createInfoLabel(""));
        infoMap.put("vendor", createInfoLabel(""));

        JTextArea jta = new JTextArea(6, 27);		// 27 chars wide seems to be good width
        jta.setFont(infoFont);
        jta.setEditable(false);
        jta.setLineWrap(true);
        jta.setWrapStyleWord(true);
        jta.setCaretPosition(0);
        infoMap.put("description", jta);

        homeButton = new JButton("Open Homepage");
        homeButton.setEnabled(false);
        homeButton.setActionCommand("");
        homeButton.setFont(buttonFont);

        // create panel that contains all info
        JPanel jp = new JPanel() /* {
            // this will provide debugging lines for the layout
            public void paint(Graphics g) {
                super.paint(g);

                if (getLayout() instanceof TableLayout) {
                    ((TableLayout) getLayout()).paintGrid(this, g);
                }
            }
        }*/ ;

        double fill = TableLayoutConstraints.FILL;
        double pref = TableLayoutConstraints.PREFERRED;

        double[][] size = {
            { 65.0, 5.0, fill },
            { pref, pref, pref, pref, 15.0, pref, 30.0, pref, fill }
        };

        jp.setLayout(new TableLayout(size));
        jp.setBorder(BorderFactory.createEmptyBorder(0, 10, 12, 10));
        jp.setOpaque(false);

        jp.add(createInfoLabel("Kind:"), "0, 0, r, t");
        jp.add((JLabel) infoMap.get("kind"), "2, 0, l, t");
        jp.add(createInfoLabel("Vendor:"), "0, 1, r, t");
        jp.add((JLabel) infoMap.get("vendor"), "2, 1, l, t");
        jp.add(createInfoLabel("Modified:"), "0, 2, r, t");
        jp.add((JLabel) infoMap.get("modified"), "2, 2, l, t");
        jp.add(createInfoLabel("Version:"), "0, 3, r, t");
        jp.add((JLabel) infoMap.get("version"), "2, 3, l, t");

        JPanel jp0 = new JPanel();
        jp0.add(homeButton);
        jp.add(jp0, "0, 5, 2, 5");

        JLabel jl = createInfoLabel("Description:");
        jl.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        jp.add(jl, "0, 7, 2, 7, l, t");
        JScrollPane jsp = new JScrollPane((JTextArea) infoMap.get("description"), JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jp.add(jsp, "0, 8, 2, 7, l, t");

        return jp;
    }

    /**
     * This creates a JScrollPane that displays the JNLP source of the descriptor.
     */
    protected JPanel createSourcePanel() {
        JPanel jp = new JPanel();
        jp.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));
        jp.setLayout(new BorderLayout());
        jp.setOpaque(false);

        JTextArea jta = new JTextArea("");
        jta.setFont(srcFont);
        jta.setEditable(false);
        jta.setCaretPosition(0);
        infoMap.put("source", jta);

        jp.add(new JScrollPane(jta), BorderLayout.CENTER);

        return jp;
    }

    protected JLabel createInfoLabel(String text) {
        JLabel jl = new JLabel(text);
        jl.setFont(infoFont);

        return jl;
    }

    protected String getDescriptorKind(Descriptor descriptor) {
        String kind = "Unknown";

        if (descriptor == null) {
            return kind;
        }

        if (descriptor instanceof ApplicationDescriptor) {
            kind = "Application";
        }

        return kind;
    }
}
