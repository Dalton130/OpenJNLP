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
 *    Christopher Heiny <heiny@eznet.net>
 */
package org.nanode.app.openjnlp.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.text.ParseException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.nanode.app.OpenJNLP;
import org.nanode.app.openjnlp.AppHandler;
import org.nanode.app.openjnlp.DefaultAppHandler;
import org.nanode.app.openjnlp.MacOSAppHandler;
import org.nanode.jnlp.JNLPParser;
import org.nanode.launcher.Gestalt;
import org.nanode.launcher.Launcher;
import org.nanode.launcher.Reference;
import org.nanode.launcher.cache.Cache;
import org.nanode.launcher.cache.CacheEntry;


/**
 * The MasterFrame is the primary Frame for the OpenJNLP application on a desktop
 * operating system.
 *
 * @author Kevin Herrboldt (kevin@nanode.org)
 * @author Christopher Heiny (heiny@eznet.net)
 */
public class MasterFrame extends JFrame implements ActionCommands, Runnable, ActionListener, ListSelectionListener, MouseListener {
    static Dimension minimumSize;		// minimum size of JFrame

    // disable this for now because Windows laf sucks (bad fonts, missing elipses)
    /*static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore error
        }
    }*/


    Object semaphore = new Object();
    int actionCommand = CMD_NONE;

    AppHandler appHandler;
    AppHandler guiHandler;

    DetailFrame infoFrame;
    AboutDialog aboutDialog;
    GetDialog getDialog;			// used to enter URLs
    LaunchDialog launchDialog;		// used for launching apps
    Console console;

    JLabel statusLabel;

    JButton getButton;
    JButton infoButton;
    JButton runButton;

    final JList appList;


    public MasterFrame() {
        super("OpenJNLP");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        appHandler = DefaultAppHandler.getAppHandler();
        guiHandler = new GUIAppHandler();

        JPanel content = (JPanel) getContentPane();
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 5, 7, 5));

        appList = new JList(new AppListModel());

        content.add(createTitlePane(), BorderLayout.NORTH);
        content.add(createContentPane(), BorderLayout.CENTER);

        setupMenus();

        setLocation(2, 3);
        setSize(getMinimumSize());
        setResizable(true);

        // set up event handling for this frame
        appList.addMouseListener(this);
        appList.addListSelectionListener(this);
        getButton.addActionListener(this);
        infoButton.addActionListener(this);
        runButton.addActionListener(this);

        getRootPane().setDefaultButton(runButton);

        // put a pretty picture in the corner of the window and/or in tool trays (depending on OS)
        ImageIcon i = IconFactory.getIcon(IconFactory.class.getResource("images/icon32x32.jpg"), IconFactory.NATURAL_SIZE);
        setIconImage(i.getImage());

        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    appHandler.handleQuit();
                }
            });
    }

    /**
     * This is the entry point for the OpenJNLP application user interface.
     * <p>
     * Basically this should be invoked from the main thread. The GUI will be
     * displayed and then this method will wait for notification from the GUI
     * which will indicate exiting the program.
     */
    public void run() {
        // perform any necessary startup processing
        show();

        getDialog = new GetDialog(this);
        MenuFactory.findMenuItem(getJMenuBar(), CMD_OPEN).setEnabled(true);
        getButton.setEnabled(true);

        launchDialog = new LaunchDialog(this);

        if (appList != null && !appList.isSelectionEmpty()) {
            MenuFactory.findMenuItem(getJMenuBar(), CMD_RUN).setEnabled(true);
            runButton.setEnabled(true);
        }

        infoFrame = new DetailFrame();
        Point p = getLocation();
        p.x += getWidth() + 15;

        if (p.x > getToolkit().getScreenSize().width) {
            p.x -= getWidth() + infoFrame.getWidth();
        }

        infoFrame.setLocation(p);

        if (appList != null && !appList.isSelectionEmpty()) {
            CacheEntry entry = (CacheEntry) appList.getSelectedValue();
            infoFrame.setCacheEntry(entry);

            MenuFactory.findMenuItem(getJMenuBar(), CMD_INFO).setEnabled(true);
            infoButton.setEnabled(true);
        }

        console = new Console(this);
        MenuFactory.findMenuItem(getJMenuBar(), CMD_CONSOLE).setEnabled(true);

        // wait for notification from somewhere
        for (int curCmd = CMD_NONE; curCmd != CMD_QUIT;) {
            synchronized (semaphore) {
                try {
                    semaphore.wait();
                } catch (InterruptedException e) { }

                curCmd = actionCommand;
            }

            switch (curCmd) {
                case CMD_ABOUT:
                    if (aboutDialog == null) {
                        aboutDialog = new AboutDialog();
                    }

                    aboutDialog.show();
                    break;
                case CMD_OPEN:
                    getDialog.show();
                    URL targetURL = getDialog.getURL();

                    if (targetURL != null) {
                        getApp(targetURL);
                    }

                    break;
                case CMD_INFO:
                    try {
                        CacheEntry entry = (CacheEntry) appList.getSelectedValue();

                        if (entry != null) {
                            infoFrame.show();
                            //showDetail(entry);
                        }
                    } catch (ClassCastException e) { /* ignore exception */ }

                    break;
                case CMD_RUN:
                    try {
                        CacheEntry entry = (CacheEntry) appList.getSelectedValue();

                        if (entry != null) {
                            launchDialog.launchApp(entry);
                        }
                    } catch (ClassCastException e) { /* ignore exception */ }

                    break;
            }
         }

         // any necessary shutdown processing goes here
         setVisible(false);
    }
    
    public void actionPerformed(ActionEvent e) {
        int actCmd = CMD_NONE;
        
        // try and get meaningful action command
        try {
            actCmd = Integer.parseInt(e.getActionCommand());
        } catch (Exception ex) {
            // ignore error
        }

        // do appropriate thing to execute command
        switch (actCmd) {
            case CMD_NONE:
                break;
            case CMD_ABOUT:
                guiHandler.handleAbout();
                break;
            case CMD_QUIT:
                guiHandler.handleQuit();
                break;
            case CMD_CONSOLE:
                console.setVisible(true);
                break;
            default:
                // specify which command and notify non-event thread(s)
                synchronized (semaphore) {
                    actionCommand = actCmd;
                    semaphore.notify();
                }
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || e.getFirstIndex() == -1 || e.getSource() != appList) {
            return;
        }

        // enable/disable controls as appropriate
        boolean state = !appList.isSelectionEmpty();

        JMenuItem infoItem = MenuFactory.findMenuItem(getJMenuBar(), CMD_INFO);
        JMenuItem runItem = MenuFactory.findMenuItem(getJMenuBar(), CMD_RUN);

        runButton.setEnabled(state);
        runItem.setEnabled(true);

        if (launchDialog != null) {
            infoButton.setEnabled(state);
            infoItem.setEnabled(state);
        }

        if (infoFrame != null) {
            if (!state) {
                //statusLabel.setText("OpenJNLP v" + OpenJNLP.getVersion());
                infoFrame.setCacheEntry(null);
            } else {
                CacheEntry entry = (CacheEntry) appList.getSelectedValue();
                infoFrame.setCacheEntry(entry);
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int index = appList.locationToIndex(e.getPoint());
            //System.out.println("Double-click on item #" + index + " in index");
        }
    }

    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }

    public Dimension getMinimumSize() {
        if (minimumSize == null) {
            minimumSize = new Dimension(410, 250);
        }

        return minimumSize;
    }

    /**
     * This will take the supplied URL and generate a SwingWorker to attempt
     * to process the URL as a JNLP reference.
     * <p>
     * Since processing the JNLP reference involves lots of time-consuming things
     * like accessing the network and doing parsing, this needs to be done in a
     * SwingWorker thread to avoid locking up the Swing GUI.
     */
    void getApp(URL targetURL) {
        // if url null, do nothing
        if (targetURL == null) {
            return;
        }
        
        final URL srcURL = targetURL;

        SwingWorker worker = new SwingWorker() {
            public Object construct() {
                String errMsg = null;

                try {
                    Cache cache = DefaultAppHandler.getAppHandler().getPrimaryCache();
                    CacheEntry entry = JNLPParser.parseDescriptor(cache, srcURL);
                    appList.setSelectedValue(entry, true);
                } catch (Exception e) {
                    errMsg = e.getMessage();   // could be I/O or Parse exception
                }
                
                // this is where any error gets displayed to the user                
                if (errMsg != null) {
                    JOptionPane.showMessageDialog(null, errMsg, "Problem Retrieving", JOptionPane.ERROR_MESSAGE);
                }
                
                return srcURL;			// this is really unnecessary, but something has to be returned
            }
        };

        worker.start();
    }

    protected JPanel createTitlePane() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(new EdgeBorder(SwingConstants.BOTTOM, Color.gray));

        p.add(Box.createHorizontalGlue());
        p.add(statusLabel = new JLabel("OpenJNLP v" + OpenJNLP.getVersion()));

        return p;
    }

    protected JPanel createContentPane() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        appList.setCellRenderer(new CacheEntryCellRenderer());
        JScrollPane jsp = new JScrollPane(appList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                          JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JLabel jl = new JLabel("Downloaded Applications");
        jl.setFont(new Font("SansSerif", Font.BOLD, 12));

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 2, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(jl, gbc);

        gbc.gridheight = 4;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        p.add(jsp, gbc);

        gbc.gridheight = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 10, 7, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(getButton = new JButton("Get\u2026"), gbc);
        p.add(infoButton = new JButton("Details"), gbc);
        p.add(runButton = new JButton("Run"), gbc);

        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        p.add(Box.createGlue(), gbc);

        getButton.setActionCommand(Integer.toString(CMD_OPEN));
        getButton.setEnabled(false);

        infoButton.setActionCommand(Integer.toString(CMD_INFO));
        infoButton.setEnabled(false);

        runButton.setActionCommand(Integer.toString(CMD_RUN));
        runButton.setEnabled(false);

        return p;
    }

    protected void setupMenus() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(MenuFactory.getMenu(MenuFactory.MENU_FILE, this));
        menuBar.add(MenuFactory.getMenu(MenuFactory.MENU_EDIT, this));
        menuBar.add(MenuFactory.getMenu(MenuFactory.MENU_TOOLS, this));

        if (Gestalt.osType() != Gestalt.OSTYPE_MACOS) {
            menuBar.add(MenuFactory.getMenu(MenuFactory.MENU_HELP, this));
            MenuFactory.findMenuItem(menuBar, CMD_ABOUT).setEnabled(true);
        }

        setJMenuBar(menuBar);

        if (Gestalt.osType() == Gestalt.OSTYPE_MACOS) {
            if (appHandler instanceof MacOSAppHandler) {
                ((MacOSAppHandler) appHandler).setDelegateAppHandler(guiHandler);
            }
        }

        if (Gestalt.osPlatform() != Gestalt.OSPLATFORM_MACOSX) {
            JMenuItem quit = MenuFactory.findMenuItem(getJMenuBar(), CMD_QUIT);

            if (quit != null) {
                quit.setEnabled(true);
            }
        }
    }


    private class GUIAppHandler extends DefaultAppHandler implements AppHandler {
        public void handleAbout() {
            synchronized (semaphore) {
                actionCommand = CMD_ABOUT;
                semaphore.notify();
            }
        }

        public void handleQuit() {
            synchronized (semaphore) {
                actionCommand = CMD_QUIT;
                semaphore.notify();
            }
        }
    }
}
