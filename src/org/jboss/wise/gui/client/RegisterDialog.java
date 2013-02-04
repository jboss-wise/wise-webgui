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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author <a href="mailto:fabri.wise@javamac.com">Fabrizio Di Giuseppe</a>
 */
public class RegisterDialog {

    private static RegisterDialogUiBinder uiBinder = GWT.create(RegisterDialogUiBinder.class);

    interface RegisterDialogUiBinder extends UiBinder<DialogBox, RegisterDialog> {
    }

    private DialogBox dialog;

    @UiField
    TextBox mail;

    @UiField
    TextBox confirmMail;

    @UiField
    PasswordTextBox password;

    @UiField
    PasswordTextBox confirmPassword;

    @UiField
    SpanElement msg;

    @UiField
    Button okBtn;

    @UiField
    Button cancelBtn;

    public RegisterDialog() {
	dialog = uiBinder.createAndBindUi(this);
	okBtn.addStyleName("gwt-Button");
    }

    public void show() {
	if (!dialog.isShowing()) {
	    mail.setText("");
	    confirmMail.setText("");
	    password.setText("");
	    confirmPassword.setText("");
	    enableButtons();
	    dialog.center();
	    dialog.show();
	    mail.setFocus(true);
	    mail.selectAll();
	}
    }

    public void hide() {
	if (dialog.isShowing())
	    dialog.hide();
    }

    private void enableButtons() {
	boolean fieldsNotEmpty = mail.getText().length() > 0 && confirmMail.getText().length() > 0 && password.getText().length() > 0 && confirmPassword.getText().length() > 0;
	boolean mailConfirmed = mail.getText().equals(confirmMail.getText());
	boolean passwordConfirmed = password.getText().equals(confirmPassword.getText());
	if (mailConfirmed) {
	    if (passwordConfirmed) {
		msg.setInnerText("");
	    } else if (confirmPassword.getText().length() > 0) {
		msg.setInnerText("Password not confirmed");
	    }
	} else if (confirmMail.getText().length() > 0) {
	    msg.setInnerText("Mail not confirmed");
	}
	okBtn.setEnabled(fieldsNotEmpty && mailConfirmed && passwordConfirmed);
    }

    @UiHandler( { "okBtn", "cancelBtn" })
    void handleClick(ClickEvent e) {
	if (e.getSource() == okBtn) {
	    Wise_gui.getInstance().confirmRegistration(mail.getText(), password.getText());
	} else if (e.getSource() == cancelBtn) {
	    Wise_gui.getInstance().cancelRegistration();
	}
    }

    @UiHandler( { "mail", "confirmMail", "password", "confirmPassword" })
    void handleBlur(BlurEvent e) {
	enableButtons();
    }

    @UiHandler( { "mail", "confirmMail", "password", "confirmPassword" })
    void handleUpEvent(KeyUpEvent e) {
	enableButtons();
    }

}
