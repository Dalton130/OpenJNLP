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

import java.net.URL;


/**
 * A reference to a native library jar. This is identical in functionality to a common reference;
 * this is used to create a unique object signature for native library jars.
 */
public class NativelibReference extends Reference {
    /**
     * Creates a reference to a native library jar with no version-id that is eagerly downloaded.
     *
     * @param   url  the url to the native library jar
     */
    public NativelibReference(URL url) {
        super(url);
    }

    /**
     * Creates a reference to a native library jar with the specified version-id that is eagerly downloaded.
     * If <code>ver</code> is <code>null</code> then an empty version-id will be assumed.
     *
     * @param   url  the url to the native library jar
     * @param   ver  the version-id associated with the url
     */
    public NativelibReference(URL url, Version ver) {
        super(url, ver);
    }

    /**
     * Creates a reference to a native library jar with the specified version-ids and how to download.
     * If <code>vers</code> is <code>null</code> then an empty version-id will be assumed.
     *
     * @param   url  the url to the native library jar
     * @param   vers an array of version-ids associated with the url
     * @param   lazy if <code>true</code> indicates this is a lazy reference, otherwise eager
     */
    public NativelibReference(URL url, Version[] vers, boolean lazy) {
        super(url, vers, lazy);
    }
}
