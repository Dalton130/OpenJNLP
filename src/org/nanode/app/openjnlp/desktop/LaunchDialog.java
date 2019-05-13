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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.nanode.launcher.Descriptor;
import org.nanode.launcher.Launcher;
import org.nanode.launcher.Reference;
import org.nanode.launcher.Resources;
import org.nanode.launcher.cache.CacheEntry;
import org.nanode.launcher.cache.CachedResource;

import org.nanode.jnlp.JNLPParser;


public class LaunchDialog extends JDialog implements Observer, ActionListener {
    protected static final Font titleFont = new Font("SansSerif", Font.BOLD, 20);
    protected static final Font vendorFont = new Font("SansSerif", Font.BOLD, 16);


    protected CacheEntry cacheEntry;

    protected Updater updater;
    protected IconLabel iconLabel;
    protected JLabel titleLabel;
    protected JLabel vendorLabel;
    protected JLabel messageLabel;
    protected JLabel xferLabel;
    protected JLabel progressLabel;
    protected JButton cancelButton;

    boolean shouldAbort;


    public LaunchDialog(Frame owner) {
        super(owner, true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel jp = (JPanel) getContentPane();
        jp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        jp.add(createTitlePane(), BorderLayout.NORTH);
        jp.add(createActivityPane(), BorderLayout.CENTER);
        jp.add(createButtonPane(), BorderLayout.SOUTH);

        setEntry(null);

        setSize(420, 225);
        setResizable(false);

        Dimension screen = getToolkit().getScreenSize();
        Dimension window = getSize();

        setLocation((screen.width - window.width) / 2, (screen.height - window.height) / 4);

        new Thread(updater = new Updater()).start();

        cancelButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        shouldAbort = true;
        updater.abortUpdate();

        try {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        messageLabel.setText("Cancelling\u2026");
                        xferLabel.setText(" ");
                    }
                });
        } catch (Exception x) { }
    }

    public void update(Observable o, Object arg) {
        CachedResource rsrc = null;

        try {
            if (!cacheEntry.equals(o)) {
                System.err.println("update from an unknown observable: " + o);
            }

            rsrc = (CachedResource) arg;
        } catch (Exception e) {
            System.err.println(e);
        }

        if (rsrc != null) {
            updater.setResource(rsrc);
        }
    }

    public void launchApp(final CacheEntry entry) {
        if (entry == null || !entry.isLaunchable()) {
            return;
        }

        setEntry(entry);

        SwingWorker worker = new SwingWorker() {
                public Object construct() {
                    Descriptor des = null;
                    String errmsg = null;
                    int option = 1;				// cancel by default

                    try {
                        setMessage("Updating JNLP file", " ");

                        if (entry == null || (des = JNLPParser.getEntryDescriptor(entry)) == null) {
                            des = null;
                            errmsg = "Unable to launch:\nno details";
                        }
                    } catch (ParseException e) {
                        des = null;
                        errmsg = "Error parsing app descriptor:\n" + e.getMessage();
                    } catch (Exception e) {
                        des = null;
                        errmsg = "Unknown error:\n" + e.getMessage();
                    }

                    if (des == null) {
                        JOptionPane.showMessageDialog(null, errmsg, "Launch Failure", JOptionPane.ERROR_MESSAGE);
                    } else {
                        ArrayList rsrc = new ArrayList();

                        // add eager jars to list
                        for (Enumeration enum = des.getResources().eagerJars(); enum.hasMoreElements();) {
                            rsrc.add(enum.nextElement());
                        }

                        // add eager nativelibs to list
                        for (Enumeration enum = des.getResources().eagerNativelibs(); enum.hasMoreElements();) {
                            rsrc.add(enum.nextElement());
                        }

                        Reference[] refs = (Reference[]) rsrc.toArray(new Reference[0]);

                        for (int i = 0; !shouldAbort && i < refs.length; i++) {
                            setProgress(i + 1, refs.length);

                            setMessage("Checking " + refs[i].getURL().getFile().substring(refs[i].getURL().getFile().lastIndexOf("/") + 1), " ");
                            entry.addResource(refs[i]);
                        }

                        if (!shouldAbort) {
                            // first get confirmation from user because of security issue
                            Object[] securityOptions = { "Continue", "Cancel" };
                            option = JOptionPane.showOptionDialog(null, "OpenJNLP provides no security checking!\nRun the app at your own risk!",
                                                                  "Security Alert", JOptionPane.DEFAULT_OPTION,
                                                                  JOptionPane.WARNING_MESSAGE, null,
                                                                  securityOptions, securityOptions[1]);
                        }
                    }

                    LaunchDialog.this.setVisible(false);

                    return (option == 0) ? des : null;
                }
            };

        shouldAbort = false;
        worker.start();
        show();

        setEntry(null);

        Descriptor des = (Descriptor) worker.get();

        if (!shouldAbort && des != null) {
        	Launcher.launchExternal(des);
        }
    }

    protected Box createActivityPane() {
        Box b0 = Box.createVerticalBox();

        b0.add(Box.createVerticalStrut(7));

        messageLabel = new JLabel();
        Box b1 = Box.createHorizontalBox();
        b1.add(messageLabel);
        b1.add(Box.createHorizontalGlue());
        b0.add(b1);

        b0.add(Box.createVerticalStrut(7));

        xferLabel = new JLabel();
        b1 = Box.createHorizontalBox();
        b1.add(xferLabel);
        b1.add(Box.createHorizontalGlue());
        b0.add(b1);

        b0.add(Box.createVerticalStrut(7));

        progressLabel = new JLabel();
        progressLabel.setForeground(Color.blue);
        b1 = Box.createHorizontalBox();
        b1.add(progressLabel);
        b1.add(Box.createHorizontalGlue());
        b0.add(b1);

        b0.add(Box.createGlue());

        return b0;
    }

    protected Box createButtonPane() {
        Box b = Box.createHorizontalBox();
        
        b.add(Box.createHorizontalGlue());

        cancelButton = new JButton("Cancel");
        b.add(cancelButton);

        return b;
    }

    protected JPanel createTitlePane() {
        JPanel jp = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        jp.add(iconLabel = new IconLabel(IconFactory.GIANT_SIZE), gbc);

        gbc.gridheight = 1;
        gbc.insets = new Insets(3, 5, 0, 0);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        titleLabel = new JLabel();
        titleLabel.setFont(titleFont);
        jp.add(titleLabel, gbc);

        vendorLabel = new JLabel();
        vendorLabel.setFont(vendorFont);
        jp.add(vendorLabel, gbc);

        return jp;
    }

    protected void setEntry(final CacheEntry entry) {
        synchronized (this) {
            if (cacheEntry != null) {
                cacheEntry.deleteObserver(this);
            }

            cacheEntry = entry;

            if (cacheEntry != null) {
                cacheEntry.addObserver(this);
            }
        }

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    if (entry != null) {
                        setTitle("Launching: " + entry.getTitle());

                        iconLabel.setDescriptor(entry.getDescriptor());
                        titleLabel.setText(entry.getTitle());
                        vendorLabel.setText(entry.getVendor());
                    } else {
                        setTitle("(Waiting to Launch)");

                        iconLabel.setDescriptor(null);
                        titleLabel.setText(" ");
                        vendorLabel.setText(" ");
                    }

                    messageLabel.setText(" ");
                    xferLabel.setText(" ");
                    progressLabel.setText(" ");
                }
            });
        } catch (Exception e) { }
    }

    protected void setMessage(final String msg, final String xferMsg) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        messageLabel.setText(msg);
                        xferLabel.setText(xferMsg);
                    }
                });
        } catch (Exception e) { }
    }

    protected void setProgress(final int amt, final int max) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        StringBuffer sb = new StringBuffer("Updating ");

                        sb.append(amt);
                        sb.append(" out of ");
                        sb.append(max);

                        progressLabel.setText(sb.toString());
                    }
                });
        } catch (Exception e) { }
    }


    class Updater implements Runnable {
        CachedResource rsrc;
        StringBuffer textBuffer;


        Updater() {
            textBuffer = new StringBuffer();
        }

        public void run() {
            for (;;) {
                if (!shouldAbort && rsrc != null) {
                    updateMessage();
                }

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) { }
            }
        }

        synchronized void abortUpdate() {
            if (rsrc != null) {
                rsrc.abortUpdate();
            }
        }

        synchronized void setResource(CachedResource cr) {
            rsrc = cr;
        }

        void updateMessage() {
            String fileName = rsrc.getReference().getURL().getFile();

            textBuffer.setLength(0);
            textBuffer.append("Loading ");
            textBuffer.append(fileName.substring(fileName.lastIndexOf("/") + 1));
            textBuffer.append(" from ");
            textBuffer.append(rsrc.getReference().getURL().getHost());

            String msg = textBuffer.toString();

            textBuffer.setLength(0);
            textBuffer.append("Transferred ");
            textBuffer.append(Long.toString(rsrc.transferAmount() / 1024L));
            textBuffer.append("KB of ");
            textBuffer.append(Long.toString(rsrc.expectedLength() / 1024L));
            textBuffer.append("KB (");
            textBuffer.append(Integer.toString(rsrc.transferRate() / 1024));
            textBuffer.append("KB/s)");

            setMessage(msg, textBuffer.toString());
        }
    }
}
