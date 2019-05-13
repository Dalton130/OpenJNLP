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

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;


/**
 * The NoSwingFrame alerts the user that Swing is not present in the classpath and
 * waits for acknowledgement, after which it will exit the JVM.
 */
public class NoSwingFrame extends Frame implements ActionListener, Runnable {
    static final String message = "Swing is not present on your system. "
                                + "Swing can be found at <http://java.sun.com/products/jfc/>.\n"
                                + "Please be sure and get the version of Swing appropriate for your version of Java.\n \n"
                                + "Once you have downloaded Swing, place the \"swingall.jar\" in the \"Java\" folder of OpenJNLP.\n \n"
                                + "You are running Java version " + System.getProperty("java.version") + ".\n";


    Object semaphore = new Object();
    
    Button exitButton;
    
    
    public NoSwingFrame() {
        super("OpenJNLP");
        
        setLayout(new BorderLayout(15, 15));
        add(new MultiLineLabel(message, 20, 20), BorderLayout.CENTER);
        
        Panel p = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        p.add(exitButton = new Button("Exit"));
        add(p, BorderLayout.SOUTH);
        
        setResizable(false);
        pack();
        
        // center on screen
        Dimension d0 = getToolkit().getScreenSize();
        Dimension d1 = getSize();
        
        setLocation((d0.width - d1.width) / 2, (d0.height / 2 - d1.height) / 2);
        
        // set up event handling for this frame
        exitButton.addActionListener(this);
        
        addWindowListener(new WindowAdapter() {
                                public void windowClosing(WindowEvent e) {
                                    synchronized (semaphore) {
                                        semaphore.notify();
                                    }
                                }
                          });
    }
    
    /**
     * This is the entry point for the OpenJNLP error user interface.
     * <p>
     * Basically this should be invoked from the main thread. The GUI will be
     * displayed and then this method will wait for notification from the GUI
     * which will indicate exiting the program.
     */
    public void run() {
        // perform any necessary startup processing
        show();
        
        // wait for notification from somewhere
        synchronized (semaphore) {
            try {
                semaphore.wait();
            } catch (InterruptedException e) { }
         }
         
         // any necessary shutdown processing goes here
         setVisible(false);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exitButton) {
            synchronized (semaphore) {
                semaphore.notify();
            }
        }
    }
    
    
    class MultiLineLabel extends Component {
        protected String label;
        protected int alignment;
        protected int marginHeight;
        protected int marginWidth;
        protected int maxWidth;
        protected int widths[];
        
        protected String[] lines;
        protected int lineAscent;
        protected int lineCount;
        protected int lineHeight;
        
        protected boolean measured;
        
        
        MultiLineLabel(String label, int width, int height) {
            marginWidth = width;
            marginHeight = height;
            alignment = Label.LEFT;
            
            processLabel(label);
        }
        
        public void setLabel(String label) {
            processLabel(label);
            measured = false;
            repaint();
        }
        
        public Dimension getPreferredSize() {
            if (!measured) {
                measure();
            }
            
            return new Dimension(maxWidth + 2 * marginWidth,
                                 lineCount * lineHeight + 2 * marginHeight);
        }
        
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
        
        public void paint(Graphics g) {
            Dimension size = getSize();

            if (!measured) {
                measure();
            }
            
            int y = lineAscent + (size.height - lineCount * lineHeight) / 2;
            int x;
            
            for (int i = 0; i < lineCount; y += lineHeight, i++) {
                switch (alignment) {
                    case Label.RIGHT:
                        x = size.width - widths[i] - marginWidth;
                        break;
                    case Label.CENTER:
                        x = (size.width - widths[i]) / 2;
                        break;
                    case Label.LEFT:
                    default:
                        x = marginWidth;
                        break;
                }
                
                g.drawString(lines[i], x, y);
            }
        }
        
        synchronized void processLabel(String label) {
            this.label = label;

            StringTokenizer t = new StringTokenizer(label, "\n");
            lineCount = t.countTokens();
            lines = new String[lineCount];
            widths = new int[lineCount];
            
            for (int i = 0; i < lineCount; i++) {
                lines[i] = t.nextToken();
            }
        }
        
        synchronized void measure() {
            FontMetrics fm = getToolkit().getFontMetrics(getFont());
            lineHeight = fm.getHeight();
            lineAscent = fm.getAscent();
            maxWidth = 0;
            
            for (int i = 0; i < lineCount; i++) {
                widths[i] = fm.stringWidth(lines[i]);
                
                if (widths[i] > maxWidth) {
                    maxWidth = widths[i];
                }
            }
            
            measured = true;
        }
    }
}
