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

package org.jboss.wise.gui.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jboss.wise.gui.shared.Operation;
import org.jboss.wise.gui.shared.ServiceWsdl;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * 
 * @author <a href="mailto:fabri.wise@javamac.com">Fabrizio Di Giuseppe</a>
 */
public class Wise_gui implements EntryPoint {

    private static Wise_gui instance = null;

    private LoginDialog loginDialog = null;

    private PasswordReminderDialog passwordReminderDialog = null;

    private RegisterDialog registerDialog = null;

    private Desk desk = null;

    private WsdlList wsdlListWidget = null;

    private Map<Long, ServiceWsdl> wsdlList = null;

    private WsdlEditDialog wsdlEditDialog = null;

    private WsdlRetrieve wsdlRetrieveDialog = null;

    private EndpointSelection endpointSelectionDialog = null;

    private WsdlBrowser wsdlBrowser = null;

    private String loginMail = null;

    private Long selectedWsdlId = null;

    private List<Operation> operations = null;

    private String navBarRootId = HTMLPanel.createUniqueId();

    private WiseServiceProxy wiseService = new WiseServiceProxy();

    public void onModuleLoad() {
	instance = this;
	login();
    }

    public static Wise_gui getInstance() {
	return instance;
    }

    public void login() {
	if (loginDialog == null) {
	    loginDialog = new LoginDialog();
	}
	loginDialog.show();
    }

    public void passwordReminder() {
	if (passwordReminderDialog == null) {
	    passwordReminderDialog = new PasswordReminderDialog();
	}
	passwordReminderDialog.show();
    }

    public void sendPasswordReminder(final String mail) {
	assert passwordReminderDialog != null;
	wiseService.sendReminder(mail, new AsyncCallback<Boolean>() {
	    public void onFailure(Throwable caught) {
		Alert.error(Constants.INSTANCE.applicationException(caught));
	    }

	    public void onSuccess(Boolean result) {
		if (result) {
		    passwordReminderDialog.hide();
		    login();
		} else {
		    Alert.error(Constants.INSTANCE.wrongReminderMail());
		}
	    }
	});

    }

    public void passwordReminderClosed() {
	assert passwordReminderDialog != null;
	passwordReminderDialog.hide();
	login();
    }

    public void register() {
	if (registerDialog == null) {
	    registerDialog = new RegisterDialog();
	}
	registerDialog.show();
    }

    public void confirmRegistration(final String mail, final String password) {
	assert registerDialog != null;
	wiseService.register(mail, password, new AsyncCallback<Boolean>() {
	    public void onFailure(Throwable caught) {
		Alert.error(Constants.INSTANCE.applicationException(caught));
	    }

	    public void onSuccess(Boolean result) {
		if (result) {
		    registerDialog.hide();
		    login();
		} else {
		    Alert.error(Constants.INSTANCE.registerError());
		}
	    }

	});
    }

    public void cancelRegistration() {
	registerDialog.hide();
	login();
    }

    public void verifyLogin(final String mail, final String password) {
	wiseService.login(mail, password, new AsyncCallback<Boolean>() {
	    public void onFailure(Throwable caught) {
		Alert.error(Constants.INSTANCE.applicationException(caught));
	    }

	    public void onSuccess(Boolean result) {
		assert result != null;
		if (result) {
		    Wise_gui.this.loginMail = mail;
		    loginDialog.hide();
		    startDesk();
		} else {
		    Alert.error(Constants.INSTANCE.loginError());
		}
	    }
	});
    }

    public void editWsdl() {
	if (wsdlEditDialog == null) {
	    wsdlEditDialog = new WsdlEditDialog();
	}
	wsdlEditDialog.editServiceWsdl(selectedWsdlId);
	wsdlEditDialog.show();
    }

    public void duplicateWsdl() {
	assert selectedWsdlId != null;
	assert wsdlList != null;
	ServiceWsdl wsdl = wsdlList.get(selectedWsdlId);
	assert wsdl != null;
	final ServiceWsdl updatedWsdl = new ServiceWsdl(Constants.INSTANCE.duplicatedWsdlName(wsdl.getName()), wsdl.getUrl(), wsdl.getNotes(), new Date());
	wiseService.addWsdl(updatedWsdl, new AsyncCallback<Long>() {
	    public void onFailure(Throwable caught) {
		Alert.error(Constants.INSTANCE.applicationException(caught));
	    }

	    public void onSuccess(Long result) {
		assert result != null;
		selectedWsdlId = result;
		wsdlList.put(result, updatedWsdl);
		wsdlListWidget.refreshRow(result);
		wsdlListWidget.select(result);
	    }
	});
    }

    public void saveEditedWsdl() {
	final ServiceWsdl updatedWsdl = wsdlEditDialog.getServiceWsdl();
	if (wsdlEditDialog.getServiceWsdlId() != null) {
	    wiseService.updateWsdl(wsdlEditDialog.getServiceWsdlId(), updatedWsdl, new AsyncCallback<Boolean>() {
		public void onFailure(Throwable caught) {
		    Alert.error(Constants.INSTANCE.applicationException(caught));
		}

		public void onSuccess(Boolean result) {
		    assert result != null;
		    if (result) {
			wsdlEditDialog.hide();
			wsdlList.put(selectedWsdlId, updatedWsdl);
			wsdlListWidget.refreshRow(selectedWsdlId);
		    } else {
			Alert.error(Constants.INSTANCE.loginError());
		    }
		}
	    });
	} else {
	    wiseService.addWsdl(updatedWsdl, new AsyncCallback<Long>() {
		public void onFailure(Throwable caught) {
		    Alert.error(Constants.INSTANCE.applicationException(caught));
		}

		public void onSuccess(Long result) {
		    assert result != null;
		    selectedWsdlId = result;
		    wsdlEditDialog.hide();
		    wsdlList.put(result, updatedWsdl);
		    wsdlListWidget.refreshRow(result);
		    wsdlListWidget.select(result);
		}
	    });
	}

    }

    public void discardEditedWsdl() {
	wsdlEditDialog.hide();
    }

    public void newWsdl() {
	if (wsdlEditDialog == null) {
	    wsdlEditDialog = new WsdlEditDialog();
	}
	wsdlEditDialog.editServiceWsdl(null);
	wsdlEditDialog.show();
    }

    public void deleteWsdl() {
	assert selectedWsdlId != null;
	wiseService.removeWsdl(selectedWsdlId, new AsyncCallback<Boolean>() {
	    public void onFailure(Throwable caught) {
		Alert.error(Constants.INSTANCE.applicationException(caught));
	    }

	    public void onSuccess(Boolean result) {
		assert result != null;
		if (result) {
		    selectedWsdlId = null;
		    wsdlListWidget.refresh();
		} else {
		    Alert.error(Constants.INSTANCE.deleteWsdlError());
		}
	    }
	});
    }

    public void logout() {
	if (desk != null) {
	    RootPanel.get("main").remove(desk);
	    desk.setContentWidget(null);
	    desk = null;
	    wsdlListWidget = null;
	}
	wiseService.logout(new AsyncCallback<Void>() {
	    public void onFailure(Throwable caught) {
		Alert.error(Constants.INSTANCE.applicationException(caught));
	    }

	    public void onSuccess(Void result) {
		loginMail = null;
		login();
	    }
	});
    }

    public void startDesk() {
	if (desk == null) {
	    desk = new Desk();
	    RootPanel.get("main").add(desk);
	}
	assert loginMail != null;
	desk.setMail(loginMail);
	selectedWsdlId = null;
	showWsdlList();
    }

    public void showWsdlList() {
	if (wsdlListWidget == null) {
	    wsdlListWidget = new WsdlList();
	}
	wiseService.getWsdlList(new AsyncCallback<Map<Long, ServiceWsdl>>() {
	    public void onFailure(Throwable caught) {
		Alert.error(Constants.INSTANCE.applicationException(caught));
	    }

	    public void onSuccess(Map<Long, ServiceWsdl> result) {
		wsdlList = result;
		wsdlListWidget.refresh();
		operations = null;
		desk.setContentWidget(wsdlListWidget);
		desk.setNavBarWidget(new HTMLPanel("span", "WISEGui"));
	    }
	});

    }

    public void retrieveWsdl() {
	if (wsdlRetrieveDialog == null) {
	    wsdlRetrieveDialog = new WsdlRetrieve();
	}
	assert selectedWsdlId != null;
	wsdlRetrieveDialog.show();
    }

    public void selectEndpoint() {
	if (endpointSelectionDialog == null) {
	    endpointSelectionDialog = new EndpointSelection();
	}
	assert selectedWsdlId != null;
	endpointSelectionDialog.show();
    }

    public void operations() {
	operations = new ArrayList<Operation>();
	for (int op = 0; op < 30; op++) {
	    operations.add(new Operation("Operation " + op));
	}
	if (wsdlBrowser == null) {
	    wsdlBrowser = new WsdlBrowser();
	}
	desk.setContentWidget(wsdlBrowser);
	StringBuilder sb = new StringBuilder();
	sb.append("<span id='" + navBarRootId + "'></span> > ");
	// sb.append(serviceEndpoint.getWsdl().getName());
	HTMLPanel navBar = new HTMLPanel("span", sb.toString());
	BackLink backLink = new BackLink("WISEGui");
	backLink.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		showWsdlList();
	    }
	});
	navBar.addAndReplaceElement(backLink, navBarRootId);
	desk.setNavBarWidget(navBar);
    }

    /**
     * @return wsdlList
     */
    public Map<Long, ServiceWsdl> getWsdlList() {
	return wsdlList;
    }

    /**
     * @param selectedWsdlId
     *            Sets selectedWsdlId to the specified value.
     */
    public void setSelectedWsdlId(Long selectedWsdlId) {
	this.selectedWsdlId = selectedWsdlId;
    }

    /**
     * @return selectedWsdlId
     */
    public Long getSelectedWsdlId() {
	return selectedWsdlId;
    }

    /**
     * @return operations
     */
    public List<Operation> getOperations() {
	return operations;
    }

}
