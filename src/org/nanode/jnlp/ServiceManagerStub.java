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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.jnlp.UnavailableServiceException;


/**
 * This is a concrete implementation of the JNLP ServiceManagerStub.
 *
 * @author Kevin Herrboldt
 */
public class ServiceManagerStub implements javax.jnlp.ServiceManagerStub {
    private static ServiceManagerStub thiz;

    Hashtable services;


    private ServiceManagerStub() {
        services = new Hashtable();

        services.put("javax.jnlp.BasicService", new BasicService());
        services.put("javax.jnlp.DownloadService", new DownloadService());
        services.put("javax.jnlp.ExtensionInstallerService", new ExtensionInstallerService());
    }

    public String[] getServiceNames() {
        Vector v = new Vector();

        for (Enumeration enum = services.keys(); enum.hasMoreElements();) {
            v.addElement(enum.nextElement());
        }

        String[] names = new String[v.size()];
        v.copyInto(names);

        return names;
    }

    public Object lookup(String name) throws UnavailableServiceException {
        Object o = services.get(name);

        if (o == null) {
            throw new UnavailableServiceException("Service " + name + " not found");
        }

        return o;
    }


    public static void register() {
        if (thiz == null) {
            synchronized (ServiceManagerStub.class) {
                if (thiz == null) {
                    thiz = new ServiceManagerStub();
                }

                javax.jnlp.ServiceManager.setServiceManagerStub(thiz);
            }
        }
    }
}
