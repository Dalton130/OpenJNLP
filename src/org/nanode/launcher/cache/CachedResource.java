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
package org.nanode.launcher.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLConnection;

import org.nanode.launcher.Reference;


public abstract class CachedResource {
    protected Reference reference;

    protected long lastModified;
    protected long actualLength;

    protected Statistics statistics;


    protected CachedResource(Reference ref) {
        reference = ref;

        statistics = new Statistics();
        lastModified = 0L;
        actualLength = 0L;
    }

    public long getLastModified() {
        return lastModified;
    }

    public Reference getReference() {
        return reference;
    }

    public long length() {
        return actualLength;
    }

    public long expectedLength() {
        return statistics.contentLength;
    }

    public long transferAmount() {
        return statistics.transferAmount;
    }

    public int transferRate() {
        return statistics.transferRate;
    }

    public boolean equals(Object obj) {
        return reference.equals(obj);
    }

    public int hashCode() {
        return reference.hashCode();
    }

    public byte[] getBytes() {
        byte[] bytes = null;

        // copy bytes from cache to ByteArrayInputStream
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            InputStream is = openCacheInputStream();

            copy(is, os);

            os.close();
            is.close();

            bytes = os.toByteArray();
        } catch (Exception e) {
            throw new CacheException("Error getting bytes from cache");
        }

        return bytes;
    }

    public void abortUpdate() {
        statistics.aborted = true;
    }

    protected long getRemoteLastModified() {
        URLConnection uc = null;
        long remoteLastModified = 0L;

        // this is a kludge for file URLs because Sun's URLConnection for "file:" doesn't work right
        if ("file".equals(reference.getURL().getProtocol())) {
            return new File(reference.getURL().getPath()).lastModified();
        }

        try {
            uc = reference.getURL().openConnection();
        } catch (Exception e) {
            System.err.println(e);
        }

        if (uc != null) {
            if (uc instanceof HttpURLConnection) {
                try {
                    ((HttpURLConnection) uc).setRequestMethod("HEAD");
                } catch (ProtocolException e) {
                    // ignore this exception, not important
                }
            }

            remoteLastModified = uc.getLastModified();

            if (uc instanceof HttpURLConnection) {
                ((HttpURLConnection) uc).disconnect();
            }
        }

        return remoteLastModified;
    }

    public boolean update() {
        long remoteLastModified = getRemoteLastModified();

        if (remoteLastModified == 0L || remoteLastModified <= lastModified) {
            return false;
        }

        // do a cache update
        synchronized (reference) {
            statistics.reset();

            InputStream in = null;
            OutputStream out = null;

            try {
                URLConnection uc = reference.getURL().openConnection();

                lastModified = uc.getLastModified();
                statistics.contentLength = uc.getContentLength();
                in = uc.getInputStream();
                out = openCacheOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;

                long startMillis = System.currentTimeMillis();
                int secs;
                statistics.updating = true;
                statistics.aborted = false;

                for (boolean done = false; !done && !statistics.aborted;) {
                    if ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);

                        if ((secs = (int) ((System.currentTimeMillis() - startMillis) / 1000L)) == 0) {
                            secs = 1;
                        }

                        statistics.transferAmount += bytesRead;
                        statistics.transferRate = (int) (statistics.transferAmount / (long) secs);
                    } else {
                        done = true;
                    }
                }

                lastModified = remoteLastModified;
            } catch (Exception e) {
                System.err.println(e);
                statistics.aborted = true;
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e0) { }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) { }
            }

            actualLength = statistics.transferAmount;
            statistics.updating = false;

            if (statistics.aborted) {
                purge();
            }
        }

        return true;
    }

    public abstract String resourceCacheName();

    public abstract InputStream openCacheInputStream() throws IOException;
    protected abstract OutputStream openCacheOutputStream() throws IOException;

    protected void purge() {
        statistics.reset();

        lastModified = 0L;
        actualLength = 0L;
    }


    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];

        for (int read = 0; (read = in.read(buffer)) != -1;) {
            out.write(buffer, 0, read);
        }
    }


    class Statistics {
        protected long contentLength;
        protected long transferAmount;
        protected int transferRate;

        protected boolean updating;
        protected boolean aborted;


        Statistics() {
            reset();

            updating = false;
            aborted = false;
        }

        protected void reset() {
            contentLength = 0L;
            transferAmount = 0L;
            transferRate = 0;
        }
    }
}
