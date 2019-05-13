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
 * Copyright (C) 2001 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 */
package org.nanode.app.openjnlp.desktop;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

import org.nanode.jnlp.JNLPParser;
import org.nanode.launcher.Gestalt;


/**
 * GetDialog presents the user with an input dialog for opening JNLP URLs and files
 */
public class GetDialog extends JDialog implements PropertyChangeListener, ActionListener {
    static final Font dialogLabelFont = new Font("SansSerif", Font.PLAIN, 12);
    
    Object semaphore = new Object();		// used for concurrency

    Frame parentFrame;
    JOptionPane optionPane;
    JFileChooser chooser;

    JTextField urlField;
    JButton fileButton;
    
    URL inputURL;
    
    
    public GetDialog(Frame owner) {
        super(owner, true);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        parentFrame = owner;

        optionPane = new JOptionPane(createInputPanel(), JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        optionPane.addPropertyChangeListener(this);

        JPanel content = (JPanel) getContentPane();
        content.add(optionPane, BorderLayout.CENTER);
        
        setResizable(false);
        pack();

        // set up the file chooser
        chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
                                public boolean accept(File f) {
                                    if (f.isDirectory()) {
                                        return true;
                                    }
                                    
                                    return f.getName().endsWith(JNLPParser.JNLP_FILE_EXTENSION);
                                }
                                
                                public String getDescription() {
                                    return ("JNLP Descriptor (*" + JNLPParser.JNLP_FILE_EXTENSION + ")");
                                }
                                });

        // set up listeners
        fileButton.addActionListener(this);

        // kludge around bug on Mac OS X where cut and paste are getting eaten
        if (Gestalt.osPlatform() == Gestalt.OSPLATFORM_MACOSX) {
            urlField.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    if (e.getModifiers() != Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
                        return;
                    }

                    JTextField jf = (JTextField) e.getSource();

                    if (e.getKeyCode() == KeyEvent.VK_X) {
                        jf.cut();
                    } else if (e.getKeyCode() == KeyEvent.VK_C) {
                        jf.copy();
                    } else if (e.getKeyCode() == KeyEvent.VK_V) {
                        jf.paste();
                    }
                }
            });
        }
    }
    
    /**
     * This centers the dialog with the owner and then calls the real show method.
     */
    public void show() {
        setLocationRelativeTo(parentFrame);
        super.show();
    }
    
    /**
     * This returns the url entered by the user. If the dialog is cancelled this will
     * be null.
     */
    public URL getURL() {
        URL theURL = null;

        // ensure inputURL isn't grabbed during update
        synchronized (semaphore) {
            theURL = inputURL;
        }

        return theURL;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == fileButton) {
            selectFile();
        }
    }
    
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        
        // if value hasn't changed or not visible or not this dialog, do nothing
        if (!prop.equals(JOptionPane.VALUE_PROPERTY) || !isVisible() || e.getSource() != optionPane) {
            return;
        }
        
        Object value = optionPane.getValue();
        int option = JOptionPane.NO_OPTION;
        
        if (value == JOptionPane.UNINITIALIZED_VALUE) {
            return;
        }
        
        optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);		// reset
        
        try {
            option = ((Integer) value).intValue();
        } catch (Exception ex) { }
        
        // if not okay, assume cancelled
        if (option != JOptionPane.OK_OPTION) {
            synchronized (semaphore) {
                inputURL = null;
            }

            setVisible(false);
            
            return;
        }
        
        // at this point okay was pressed, parse input value while preventing others from getting it
        synchronized (semaphore) {
            String urlText = urlField.getText().trim();
            inputURL = null;
    
            // try to create URL from input field, adding "http://" if necessary
            try {
                inputURL = new URL(urlText);
            } catch (MalformedURLException ue) { }
            
            if (inputURL == null) {
                try {
                    inputURL = new URL("http://" + urlText);
                } catch (MalformedURLException ue) {  /* XXX - some sort of error here */ }
            }
        }
        
        setVisible(false);
    }
    
    /**
     * Let user select a file from the file system.
     * <p>
     * Presents a file chooser dialog for selecting a JNLP file. If the user
     * selects a file, populates the urlField with the selected file's path
     * converted to a file URL.
     */
    protected void selectFile() {
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                String fileSpec = null;
                
                // throw up file chooser, if file chosen turn into file url string
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    fileSpec = "file://" + chooser.getSelectedFile();
                    urlField.setText(fileSpec);
                }

                return fileSpec;
            }
        };
        
        worker.start();
    }
    
    /**
     * This creates the panel that is used to enter a URL. It also sets up the
     * input components.
     */
    protected JPanel createInputPanel() {
        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel jl = new JLabel("Enter Uniform Resource Locator to open:");
        jl.setFont(dialogLabelFont);
        jp.add(jl, gbc);
        
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;
        jp.add(urlField = new JTextField(30), gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        fileButton = new JButton(new ImageIcon(getClass().getResource("images/OpenDoc.gif")));
        fileButton.setMargin(new Insets(0, 0, 0, 0));
        fileButton.setFocusPainted(false);
        jp.add(fileButton);

        return jp;
    }
}
