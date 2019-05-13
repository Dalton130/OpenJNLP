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

import java.awt.Frame;


/**
 * Tests the runtime environment and creates an appropriate graphical user interface. This is
 * a singleton for controlling the GUI once it is created.
 *
 * @author Kevin Herrboldt (kevin@nanode.org)
 */
public class Gooey {
    /** Single instance object of this class. */
    private static Gooey thiz;


    /** The main GUI frame. */
    protected Frame mainFrame;

    /** The controlling runner to transfer control to. */
    protected Runnable runner;


    /**
     * Creates an object of this class.
     */
    private Gooey() {
    }

    /**
     * Creates and transfers control to the main user interface. Once control returns, the user
     * interface is disposed of. Subsequent calls will create a fresh user interface.
     */
    public void go() {
        toFront();

        runner.run();
        mainFrame.dispose();

        mainFrame = null;
        runner = null;
    }

    /**
     * Brings the user interface to the front.
     */
    public void toFront() {
        createGUI();
        mainFrame.show();
    }

    /**
     * Creates the main graphical user interface. This will only create the GUI if it hasn't
     * already been created. The type of GUI created is determined by examining the runtime
     * environment.
     */
    protected void createGUI() {
        if (mainFrame != null) {
            return;
        }

        synchronized (this) {
            if (mainFrame == null) {
                if (isSwingPresent()) {
                    mainFrame = new MasterFrame();
                } else {
                    mainFrame = new NoSwingFrame();
                }
            }

            runner = (Runnable) mainFrame;
        }
    }


    /**
     * Returns the singleton instance of this class, creating it if necessary.
     *
     * @return  the single instance of this class
     */
    public static Gooey getInstance() {
        if (thiz == null) {
            synchronized (Gooey.class) {
                if (thiz == null) {
                    thiz = new Gooey();
                }
            }
        }

        return thiz;
    }

    /**
     * Determines whether or not this runtime environment supports the Swing graphical environment.
     *
     * @return  <code>true</code> if swing is found in the classpath; <code>false</code> otherwise
     */
    public static boolean isSwingPresent() {
        boolean hasSwing = false;
        
        try {
            Class.forName("javax.swing.JFrame");
            hasSwing = true;
        } catch (Throwable t) {
            // ignore this
        }
        
        return hasSwing;
    }
}
