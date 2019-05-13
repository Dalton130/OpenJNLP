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
 * Copyright (C) 2001-2002 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 */
package org.nanode.launcher;

import java.net.URL;

import org.nanode.launcher.cache.Cache;


/**
 * This class provides a descriptor of an extension. An extension is defined as
 * either a component or an installer.
 * <p>
 * The concept behind a component is that it describes one or more elements
 * that comprise a single thing. An example of this would be to
 * identify an XML-parsing component, which could be comprised of three jars:
 * <i>jaxp.jar, crimson.jar, xalan.jar</i>. These are then treated as a single
 * component, which can not be directly executed but can be included within
 * applications.
 * <p>
 * An installer is an executable application, but it will only be executed once
 * on any given platform. If an installer is included within an application, a
 * check must be made to see if the installer has already been executed. If
 * not, it gets executed. Otherwise it would be silently ignored. An example of
 * referencing an installer is to include an installer for a dictionary for
 * a spell checker. Once a dictionary is installed, there would be no need to
 * rerun the dictionary installer, although it needs to be executed the first
 * time to ensure that a dictionary is in place for spell checking.
 *
 * @author Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 */
public class ExtensionDescriptor extends Descriptor {
    public ExtensionDescriptor(Cache cache, URL base, Reference src) {
        super(cache, base, src);
    }
}
