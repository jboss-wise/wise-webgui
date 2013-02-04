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
package org.jboss.wise.gui.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.wise.gui.shared.ServiceWsdl;
import org.jboss.wise.gui.shared.WiseServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author <a href="mailto:fabri.wise@javamac.com">Fabrizio Di Giuseppe</a>
 */
public class WiseServiceProxy implements WiseServiceAsync {

    private Map<String, UserData> users;

    private UserData loggedUser;

    public WiseServiceProxy() {
	users = new HashMap<String, UserData>();
	users.put("a", new UserData("a")); // <- test login
	loggedUser = null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.wise.gui.shared.WiseServiceAsync#register(java.lang.String,
     *      java.lang.String, com.google.gwt.user.client.rpc.AsyncCallback)
     */
    public void register(final String mail, final String password, AsyncCallback<Boolean> callback) {
	assert mail != null;
	assert mail.length() > 0;
	assert password != null;
	assert password.length() > 0;
	assert loggedUser == null;
	if (users.get(mail) != null) {
	    callback.onSuccess(false);
	} else {
	    UserData newUser = new UserData(password);
	    users.put(mail, newUser);
	    callback.onSuccess(true);
	}
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.wise.gui.shared.WiseServiceAsync#sendReminder(java.lang.String,
     *      com.google.gwt.user.client.rpc.AsyncCallback)
     */
    public void sendReminder(final String mail, AsyncCallback<Boolean> callback) {
	if (users.get(mail) != null) {
	    callback.onSuccess(true);
	} else {
	    callback.onSuccess(false);
	}
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.wise.gui.shared.WiseServiceAsync#login(java.lang.String,
     *      java.lang.String, com.google.gwt.user.client.rpc.AsyncCallback)
     */
    public void login(final String mail, final String password, AsyncCallback<Boolean> callback) {
	assert loggedUser == null;
	UserData user = users.get(mail);
	boolean logged = (user != null && user.verifyPassword(password));
	if (logged)
	    loggedUser = user;
	callback.onSuccess(logged);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.wise.gui.shared.WiseServiceAsync#logout(com.google.gwt.user.client.rpc.AsyncCallback)
     */
    public void logout(AsyncCallback<Void> callback) {
	assert loggedUser != null;
	loggedUser = null;
	callback.onSuccess(null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.wise.gui.shared.WiseServiceAsync#getWsdlList(com.google.gwt.user.client.rpc.AsyncCallback)
     */
    public void getWsdlList(AsyncCallback<Map<Long, ServiceWsdl>> callback) {
	assert loggedUser != null;
	callback.onSuccess(loggedUser.getWsdlList());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.wise.gui.shared.WiseServiceAsync#addWsdl(org.jboss.wise.gui.shared.ServiceWsdl,
     *      com.google.gwt.user.client.rpc.AsyncCallback)
     */
    public void addWsdl(final ServiceWsdl wsdl, AsyncCallback<Long> callback) {
	assert loggedUser != null;
	callback.onSuccess(loggedUser.add(wsdl));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.wise.gui.shared.WiseServiceAsync#updateWsdl(long,
     *      org.jboss.wise.gui.shared.ServiceWsdl,
     *      com.google.gwt.user.client.rpc.AsyncCallback)
     */
    public void updateWsdl(long id, final ServiceWsdl wsdl, AsyncCallback<Boolean> callback) {
	assert loggedUser != null;
	callback.onSuccess(loggedUser.updateWsdl(id, wsdl));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.jboss.wise.gui.shared.WiseServiceAsync#removeWsdl(long,
     *      com.google.gwt.user.client.rpc.AsyncCallback)
     */
    public void removeWsdl(long id, AsyncCallback<Boolean> callback) {
	assert loggedUser != null;
	callback.onSuccess(loggedUser.removeWsdl(id));
    }

    private class UserData {

	private String password;

	private Map<Long, ServiceWsdl> wsdlList;

	private int nextWsdlId = 0;

	public UserData(String password) {
	    this.password = password;
	    wsdlList = new HashMap<Long, ServiceWsdl>();
	    for (int i = 0; i < 10; i++) {
		wsdlList.put(Long.valueOf(nextWsdlId++), new ServiceWsdl("Service " + i, "http://HOST " + i + ":8080/Service1WS/Service1WSBean?wsdl", "This tool may be...", new Date()));
	    }
	}

	public boolean verifyPassword(final String password) {
	    return this.password.equals(password);
	}

	public Map<Long, ServiceWsdl> getWsdlList() {
	    return wsdlList;
	}

	public long add(final ServiceWsdl wsdl) {
	    long id = nextWsdlId++;
	    wsdlList.put(Long.valueOf(id), wsdl);
	    return id;
	}

	public boolean updateWsdl(long id, final ServiceWsdl wsdl) {
	    if (wsdlList.get(id) != null) {
		wsdlList.put(id, wsdl);
		return true;
	    }
	    return false;
	}

	public boolean removeWsdl(long id) {
	    return wsdlList.remove(id) != null;
	}

    }

}
