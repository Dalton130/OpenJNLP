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
 * Copyright (C) 2002 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 */
package org.nanode.launcher;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


class AppletRunner implements AppletContext, Runnable {
    protected ApplicationDescriptor descriptor;

    protected Applet applet;
    protected AppletFrame appletFrame;
    private boolean activeState;

    protected Map streamMap;


    protected AppletRunner(ApplicationDescriptor desc, ClassLoader loader) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        descriptor = desc;

        if (!descriptor.isAppletDescriptor()) {
            throw new InstantiationException("descriptor not for applet");
        }

        streamMap = Collections.synchronizedMap(new HashMap());

        activeState = false;
        applet = (Applet) loader.loadClass(descriptor.getMainClass()).newInstance();
        appletFrame = new AppletFrame();
        applet.setStub(appletFrame);
    }

    public void run() {
        activeState = true;
        appletFrame.show();
        
        applet.init();
        showStatus(" ");
        applet.start();

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }

        applet.stop();
        applet.destroy();

        appletFrame.setVisible(false);
        activeState = false;

        System.exit(0);
    }

    public Applet getApplet(String name) {
        if (name != null && name.equals(descriptor.getAppletName())) {
            return applet;
        }

        return null;
    }

    public Enumeration getApplets() {
        return new Enumeration() {
            Applet[] applets;
            int cnt;

            /* extend constructor */ {
                applets = new Applet[1];
                applets[0] = applet;
                cnt = 0;
            }

            public boolean hasMoreElements() {
                return (cnt < applets.length);
            }

            public Object nextElement() {
                Applet a = null;

                if (cnt < applets.length) {
                    synchronized (applets) {
                        if (cnt < applets.length) {
                            a = applets[cnt++];
                        }
                    }
                }

                if (a == null) {
                    throw new NoSuchElementException("no more applets");
                }

                return a;
            }
        };
    }

    public AudioClip getAudioClip(URL url) {
        // need to do security check here
        return Applet.newAudioClip(url);
    }

    public Image getImage(URL url) {
        // need to do security check here
        return applet.getToolkit().getImage(url);
    }

    public void showDocument(URL url) {
    }

    public void showDocument(URL url, String target) {
    }

    public void showStatus(String status) {
        if (appletFrame != null) {
            appletFrame.setStatus(status);
        }
    }
    
    /**
     * Returns the stream to which the specified key is associated in this applet context.
     * Will return <code>null</code> if there is no stream associated with the specified key.
     *
     * @param   key  key to stream to look up
     * @return  the stream associated with the specified key or <code>null</code> if key not
     *          found
     */
    public InputStream getStream(String key) {
        return (InputStream) streamMap.get(key);
    }    

    /**
     * Returns all keys assocated with streams in this applet context.
     *
     * @return  an <code>Iterator</code> of all the keys mapped to streams
     */
    public Iterator getStreamKeys() {
        return streamMap.keySet().iterator();
    }
    
    /**
     * Associates the specified stream with the specified key in this applet context. If the
     * applet context previously contained a mapping for this key the old value is replaced.
     *
     * @param   key     key with which to associate this stream
     * @param   stream  the stream to associate; if <code>null</code> the specified key will be
     *                  removed from the mapping
     * @throws  IOException if the stream size exceeds a certain limit. Currently never possible.
     */
    public void setStream(String key, InputStream stream) throws IOException {
        if (stream == null) {
            streamMap.remove(key);
        } else {
            streamMap.put(key, stream);
        }
    }
    
    private class AppletFrame extends JFrame implements AppletStub {
        JPanel appletPanel;
        JLabel statusLine;
        boolean resizing;


        private AppletFrame() {
            super(descriptor.getInformation().getTitle());

            statusLine = new JLabel(" ");
            appletPanel = new JPanel(new BorderLayout());
            appletPanel.add(applet, BorderLayout.CENTER);
            getContentPane().add(appletPanel, BorderLayout.CENTER);
            getContentPane().add(statusLine, BorderLayout.SOUTH);

            setResizable(false);
            appletResize(descriptor.getWidth(), descriptor.getHeight());

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    synchronized (AppletRunner.this) {
                        AppletRunner.this.notify();
                    }
                }
            });
        }

        public synchronized void appletResize(int width, int height) {
            if (resizing) {
                return;
            }

            Dimension d = new Dimension(width, height);

            resizing = true;
            appletPanel.setMinimumSize(d);
            appletPanel.setMaximumSize(d);
            appletPanel.setPreferredSize(d);
            applet.resize(d);
            resizing = false;

            pack();
        }

        public AppletContext getAppletContext() {
            return AppletRunner.this;
        }

        public URL getCodeBase() {
            return descriptor.getCodebase();
        }

        public URL getDocumentBase() {
            return descriptor.getDocumentBase();
        }

        public String getParameter(String name) {
            return descriptor.getParam(name);
        }

        public boolean isActive() {
            return activeState;
        }

        private synchronized void setStatus(String s) {
            statusLine.setText((s != null) ? s : " ");
        }
    }
}
