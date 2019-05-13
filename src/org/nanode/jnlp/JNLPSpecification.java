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
package org.nanode.jnlp;

import java.net.URL;

import org.nanode.launcher.Reference;
import org.nanode.launcher.Version;


/**
 * Encapsulates all the information associated with a particular descriptor (JNLP file).
 *
 * @author Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 */
public class JNLPSpecification {
    public static final Version[] DEFAULT_JNLP_VERSIONS = Version.parseVersions("1.0+");


    Reference jnlpRef;
    URL codebase;
    Version[] specification;


    public JNLPSpecification(Reference src, URL base) {
        this(src, base, null);
    }

    public JNLPSpecification(Reference ref, URL base, Version[] spec) {
        jnlpRef = ref;
        codebase = base;
        specification = spec;
    }

    public URL getCodebase() {
        return (codebase != null) ? codebase : jnlpRef.getURL();
    }

    public Reference getReference() {
        return jnlpRef;
    }
    
    public Version[] getSpecification() {
        return ((specification != null) ? specification : DEFAULT_JNLP_VERSIONS);
    }

    public String getJNLPName() {
        return jnlpRef.getURL().getFile().substring(jnlpRef.getURL().getFile().lastIndexOf("/"));
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("JNLPSpecification[ref=");

        sb.append(jnlpRef);
        sb.append(",codebase=");

        if (codebase != null) {
            sb.append(codebase);
        }

        sb.append(",spec=(");

        if (specification != null) {
            for (int i = 0; i < specification.length; i++) {
                sb.append(specification[i]);

                if (i < specification.length - 1) {
                    sb.append(' ');
                }
            }
        }

        sb.append(")]");

        return sb.toString();
    }
}
