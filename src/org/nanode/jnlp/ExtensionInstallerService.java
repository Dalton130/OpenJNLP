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
package org.nanode.jnlp;

import java.net.URL;


/**
 * Implementation of the JNLP ExtensionInstallerService.
 *
 * @author Kevin Herrboldt
 */
public class ExtensionInstallerService implements javax.jnlp.ExtensionInstallerService {
    protected ExtensionInstallerService() {
    }

    public String getInstalledJRE(URL ref, String version) {
        return null;
    }

    public URL getExtensionLocation() {
        return null;
    }

    public String getExtensionVersion() {
        return null;
    }

    public String getInstallPath() {
        return null;
    }

    public void hideProgressBar() {
    }

    public void hideStatusWindow() {
    }

    public void installFailed() {
    }

    public void installSucceeded(boolean needsReboot) {
    }

    public void setHeading(String heading) {
    }

    public void setJREInfo(String platformVersion, String jrePath) {
    }

    public void setNativeLibraryInfo(String path) {
    }

    public void setStatus(String status) {
    }

    public void updateProgress(int value) {
    }
}
