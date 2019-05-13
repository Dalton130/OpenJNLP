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

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.Event;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.nanode.launcher.Gestalt;


public class MenuFactory implements ActionCommands {
    public static final int MENU_FILE = 0;
    public static final int MENU_EDIT = 1;
    public static final int MENU_APPLICATION = 2;
    public static final int MENU_TOOLS = 3;
    public static final int MENU_WINDOW = 4;
    public static final int MENU_HELP = 5;


    public static JMenuItem findMenuItem(JMenuBar mbar, int cmd) {
        JMenu menu = null;
        JMenuItem item = null;

        for (int i = 0; i < mbar.getMenuCount(); i++) {
            menu = mbar.getMenu(i);

            for (int j = 0; j < menu.getItemCount(); j++) {
                try {
                    if ((item = menu.getItem(j)) != null && cmd == Integer.parseInt(item.getActionCommand())) {
                        return item;
                    }
                } catch (NumberFormatException e) {
                    // ignore this, not important
                }
            }
        }

        return null;
    }

    public static JMenu getMenu(int mid, ActionListener al) {
        JMenu menu = null;
        int accelMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        switch (mid) {
            case MENU_FILE:
                menu = new JMenu("File");
                menu.add(createMenuItem(CMD_NEW, "New", KeyStroke.getKeyStroke(KeyEvent.VK_N, accelMask), al));
                menu.add(createMenuItem(CMD_OPEN, "Get JNLP\u2026", KeyStroke.getKeyStroke(KeyEvent.VK_L, accelMask), al));
                menu.addSeparator();
                menu.add(createMenuItem(CMD_CLOSE, "Close", KeyStroke.getKeyStroke(KeyEvent.VK_W, accelMask), al));
                menu.add(createMenuItem(CMD_SAVE, "Save", KeyStroke.getKeyStroke(KeyEvent.VK_S, accelMask), al));
                menu.add(createMenuItem(CMD_SAVE_AS, "Save As\u2026", KeyStroke.getKeyStroke(KeyEvent.VK_S, accelMask | Event.SHIFT_MASK), al));
                menu.add(createMenuItem(CMD_REVERT, "Revert", null, al));
                menu.addSeparator();
                menu.add(createMenuItem(CMD_INFO, "Show Details", KeyStroke.getKeyStroke(KeyEvent.VK_I, accelMask), al));
                menu.add(createMenuItem(CMD_RUN, "Run App", KeyStroke.getKeyStroke(KeyEvent.VK_R, accelMask), al));
                menu.addSeparator();
                menu.add(createMenuItem(CMD_SETUP, "Page Setup\u2026", KeyStroke.getKeyStroke(KeyEvent.VK_P, accelMask | Event.SHIFT_MASK), al));
                menu.add(createMenuItem(CMD_PRINT, "Print\u2026", KeyStroke.getKeyStroke(KeyEvent.VK_P, accelMask), al));

                if (Gestalt.osPlatform() != Gestalt.OSPLATFORM_MACOSX) {
                    menu.addSeparator();
                    menu.add(createMenuItem(CMD_QUIT, "Quit", KeyStroke.getKeyStroke(KeyEvent.VK_Q, accelMask), al));
                }

                break;
            case MENU_EDIT:
                menu = new JMenu("Edit");
                menu.add(createMenuItem(CMD_UNDO, "Undo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, accelMask), al));
                menu.add(createMenuItem(CMD_UNDO, "Redo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, accelMask | Event.SHIFT_MASK), al));
                menu.addSeparator();
                menu.add(createMenuItem(CMD_CUT, "Cut", KeyStroke.getKeyStroke(KeyEvent.VK_X, accelMask), al));
                menu.add(createMenuItem(CMD_COPY, "Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, accelMask), al));
                menu.add(createMenuItem(CMD_PASTE, "Paste", KeyStroke.getKeyStroke(KeyEvent.VK_V, accelMask), al));
                menu.add(createMenuItem(CMD_CLEAR, "Clear", null, al));
                menu.add(createMenuItem(CMD_ALL, "Select All", KeyStroke.getKeyStroke(KeyEvent.VK_A, accelMask), al));
                break;
            case MENU_TOOLS:
                menu = new JMenu("Tools");
                menu.add(createMenuItem(CMD_CONSOLE, "Show Console", KeyStroke.getKeyStroke(KeyEvent.VK_C, accelMask | Event.SHIFT_MASK), al));
                break;
            case MENU_HELP:
                menu = new JMenu("Help");

                if (Gestalt.osType() != Gestalt.OSTYPE_MACOS) {
                    menu.add(createMenuItem(CMD_ABOUT, "About OpenJNLP\u2026", null, al));
                }

                break;
        }

        return menu;
    }

    protected static JMenuItem createMenuItem(int cmd, String name, KeyStroke accel, ActionListener al) {
        JMenuItem item = new JMenuItem(name);

        item.setActionCommand(Integer.toString(cmd));
        item.setEnabled(false);
        item.addActionListener(al);

        if (accel != null) {
            item.setAccelerator(accel);
        }

        return item;
    }

    /*protected MenuItem createAWTMenuItem(int cmd, String name, KeyStroke accel, ActionListener al) {
        MenuItem awtItem = new MenuItem(name);

        awtItem.setActionCommand(Integer.toString(cmd));
        awtItem.setEnabled(false);
        awtItem.addActionListener(this);

        if (accel != null) {
            boolean withShift = ((accel.getModifiers() & Event.SHIFT_MASK) != 0);

            awtItem.setShortcut(new MenuShortcut(accel.getKeyCode(), withShift));
        }

        return awtItem;
    }*/
}
