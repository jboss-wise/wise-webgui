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

import java.util.Date;

import org.jboss.wise.gui.shared.ServiceWsdl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author <a href="mailto:fabri.wise@javamac.com">Fabrizio Di Giuseppe</a>
 */
public class WsdlEditDialog {

    private static WsdlEditDialogUiBinder uiBinder = GWT.create(WsdlEditDialogUiBinder.class);

    interface WsdlEditDialogUiBinder extends UiBinder<DialogBox, WsdlEditDialog> {
    }

    private DialogBox dialog;

    @UiField
    TextBox name;

    @UiField
    TextArea description;

    @UiField
    TextBox wsdl;

    @UiField
    Button cancelBtn;

    @UiField
    Button okBtn;

    private ServiceWsdl serviceWsdl = null;

    private Long serviceWsdlId = null;

    public WsdlEditDialog() {
	dialog = uiBinder.createAndBindUi(this);
	okBtn.addStyleName("gwt-Button");
    }

    public void show() {
	if (!dialog.isShowing()) {
	    assert serviceWsdl != null;
	    name.setText(this.serviceWsdl.getName());
	    description.setText(this.serviceWsdl.getNotes());
	    wsdl.setText(this.serviceWsdl.getUrl());
	    enableButtons();
	    dialog.center();
	    dialog.show();
	    name.setFocus(true);
	    name.selectAll();
	}
    }

    public void hide() {
	if (dialog.isShowing())
	    dialog.hide();
    }

    private void enableButtons() {
	String nameText = name.getText();
	String descriptionText = description.getText();
	String wsdlText = wsdl.getText();
	okBtn.setEnabled(nameText.length() > 0 && descriptionText.length() > 0 && wsdlText.length() > 0);
    }

    @UiHandler( { "cancelBtn", "okBtn" })
    void onClick(ClickEvent e) {
	if (e.getSource() == cancelBtn) {
	    Wise_gui.getInstance().discardEditedWsdl();
	} else if (e.getSource() == okBtn) {
	    serviceWsdl = new ServiceWsdl(name.getText(), wsdl.getText(), description.getText(), new Date());
	    Wise_gui.getInstance().saveEditedWsdl();
	}
    }

    @UiHandler( { "name", "description", "wsdl" })
    void handleBlur(BlurEvent e) {
	enableButtons();
    }

    @UiHandler( { "name", "description", "wsdl" })
    void handleUpEvent(KeyUpEvent e) {
	enableButtons();
    }

    /**
     * @return serviceWsdl
     */
    public ServiceWsdl getServiceWsdl() {
	return serviceWsdl;
    }

    /**
     * @return serviceWsdlId
     */
    public Long getServiceWsdlId() {
	return serviceWsdlId;
    }

    public void editServiceWsdl(Long serviceWsdlId) {
	this.serviceWsdlId = serviceWsdlId;
	if (this.serviceWsdlId != null) {
	    serviceWsdl = Wise_gui.getInstance().getWsdlList().get(serviceWsdlId);
	} else {
	    serviceWsdl = new ServiceWsdl("", "", "", new Date());
	}
    }
}
