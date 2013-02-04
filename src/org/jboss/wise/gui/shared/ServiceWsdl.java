/*
 * JBoss, Home of Professional Open Source Copyright 2009, JBoss Inc., and
 * individual contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.jboss.wise.gui.shared;

import java.io.Serializable;
import java.util.Date;

/**
 * @author <a href="mailto:fabri.wise@javamac.com">Fabrizio Di Giuseppe</a>
 */
public class ServiceWsdl implements Serializable {

    private static final long serialVersionUID = -6561249553718237420L;

    private String name;

    private String url;

    private String notes;

    private Date savingDate;

    public ServiceWsdl(String name, String url, String notes, Date savingDate) {
	super();
	this.name = name;
	this.url = url;
	this.notes = notes;
	this.savingDate = savingDate;
    }

    public String getName() {
	return name;
    }

    public String getUrl() {
	return url;
    }

    public String getNotes() {
	return notes;
    }

    public Date getSavingDate() {
	return savingDate;
    }

}
