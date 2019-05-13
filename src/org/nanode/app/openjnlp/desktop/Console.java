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
 *    Christopher Heiny <heiny@eznet.net>
 */
package org.nanode.app.openjnlp.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.nanode.app.OpenJNLP;
import org.nanode.launcher.Descriptor;
import org.nanode.launcher.Launcher;


/**
 * Provides a graphical console frame that captures and displays diagnostic messages.
 *
 * @author Kevin Herrboldt (kevin@nanode.org)
 * @author Christopher Heiny (heiny@eznet.net)
 */
public class Console extends JFrame implements ActionListener {
    protected static final Font consoleFont = new Font("SansSerif", Font.PLAIN, 10);


    JTextArea consoleText;
    
    private Window parent = null;  // the window for which this console provides output


    public Console() {
        this(null);
    }
    
    public Console(Window parent) {
        super("OpenJNLP Console");
        this.parent = parent;

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        JPanel content = (JPanel) getContentPane();
        content.setLayout(new BorderLayout());
        //content.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        content.add(createContentPane(), BorderLayout.CENTER);

        setSize(515, 270);

        if ( parent != null ) {
            // try to position ourselves nicely on the screen so we don't squat right on
            // the main window of interest
            Rectangle parentBounds = parent.getBounds();
            Rectangle myBounds = new java.awt.Rectangle ();
            Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            myBounds.height = this.getSize().height;
            myBounds.width = this.getSize().width;

            if ( parentBounds.x + parentBounds.width + myBounds.width < screen.width ) {
                // put console to right of window
                myBounds.x = parentBounds.x + parentBounds.width;
                myBounds.y = parentBounds.y;
            } else if ( parentBounds.y + parentBounds.height + myBounds.height < screen.height ) {
                // put console below the window
                myBounds.x = parentBounds.x;
                myBounds.y = parentBounds.y + parentBounds.height;
            } else {
                // give up and just offset a bit
                myBounds.x = parentBounds.x + 25;
                myBounds.y = parentBounds.y + 25;
            }

            this.setBounds ( myBounds );
        }

        Launcher.setLogger(new ConsoleLogger());

        // set up redirected output for System.out
        try {
            PipedInputStream pin = new PipedInputStream();
            PipedOutputStream pout = new PipedOutputStream(pin);
            System.setOut(new PrintStream(pout, true));

            Thread t = new Thread(new Streamer(pin, false));
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // set up redirected output for System.err
        try {
            PipedInputStream pin = new PipedInputStream();
            PipedOutputStream pout = new PipedOutputStream(pin);
            System.setErr(new PrintStream(pout, true));

            Thread t = new Thread(new Streamer(pin, true));
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // put a pretty picture in the corner of the window and/or in tool trays (depending on OS)
        ImageIcon i = IconFactory.getIcon(IconFactory.class.getResource("images/icon32x32.jpg"), IconFactory.NATURAL_SIZE);
        setIconImage(i.getImage());
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println(getSize());
    }

    protected JPanel createContentPane() {
        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));

        consoleText = new JTextArea("OpenJNLP v" + OpenJNLP.getVersion() + "\n");
        consoleText.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        consoleText.setFont(consoleFont);
        consoleText.setEditable(false);

        JScrollPane jsp = new JScrollPane(consoleText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jp.add(jsp);

        return jp;
    }


    class ConsoleLogger extends Launcher.Logger {
        ConsoleLogger() {
        }

        public void logErr(Descriptor des, String msg) {
            logOut(des, msg);
        }

        public synchronized void logOut(Descriptor des, String msg) {
            StringBuffer sb = new StringBuffer();

            if (des != null) {
                sb.append('[');
                sb.append(des.getInformation().getTitle());
                sb.append("] ");
            }

            sb.append(msg);

            if ( (msg.length() == 0) || (msg.charAt(msg.length() - 1) != '\n')  ){
                sb.append('\n');
            }

            final String text = sb.toString();

            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        consoleText.append(text);
                    }
                });
            } catch (Exception e) {
            }
        }
    }

    class Streamer implements Runnable {
        PipedInputStream pin;
        boolean isErr;

        StringBuffer bufferText;


        Streamer(PipedInputStream input, boolean err) {
            pin = input;
            isErr = err;

            bufferText = new StringBuffer();
        }

        public void run() {
            String line;

            for (boolean done = false; !done;) {
                try {
                    line = readline();

                    if (isErr) {
                        Launcher.logErr(null, line);
                    } else {
                        Launcher.logOut(null, line);
                    }
                } catch (IOException e) {
                    done = true;
                }
            }
        }

        synchronized String readline() throws IOException {
            bufferText.setLength(0);

            for (boolean eol = false; !eol;) {
                int b = pin.read();

                // skip stupid carriage returns, Windows is so brain-dead about EOL
                if (b == '\r') {
                    continue;
                }

                if (b != -1) {
                    bufferText.append((char) b);
                }

                if (b == -1 || b == '\n') {
                    eol = true;
                }
            }

            return bufferText.toString();
        }
    }
}
