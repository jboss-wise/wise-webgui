/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.wise.gui.shared;

import java.io.Serializable;

/**
 * @author <a href="mailto:fabri.wise@javamac.com">Fabrizio Di Giuseppe</a>
 */
public class ServiceEndpoint implements Serializable {

    private static final long serialVersionUID = 7115553037071389063L;

    private ServiceWsdl wsdl;

    private String customUrl;

    /**
     * @param wsdl
     * @param customUrl
     */
    public ServiceEndpoint(ServiceWsdl wsdl, String customUrl) {
	super();
	this.wsdl = wsdl;
	this.customUrl = customUrl;
    }

    /**
     * @param wsdl
     */
    public ServiceEndpoint(ServiceWsdl wsdl) {
	super();
	this.wsdl = wsdl;
	this.customUrl = wsdl.getUrl();
    }

    /**
     * @return wsdl
     */
    public ServiceWsdl getWsdl() {
	return wsdl;
    }

    /**
     * @return customUrl
     */
    public String getCustomUrl() {
	return customUrl;
    }

}
