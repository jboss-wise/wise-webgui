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

import org.jboss.wise.gui.client.Alert.Reply;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author <a href="mailto:fabri.wise@javamac.com">Fabrizio Di Giuseppe</a>
 */
public class Desk extends Composite {

    private static DeskUiBinder uiBinder = GWT.create(DeskUiBinder.class);

    interface DeskUiBinder extends UiBinder<Widget, Desk> {
    }

    static {
	Resources.INSTANCE.wiseGuiStyle().ensureInjected();
    }

    @UiField
    SimplePanel content;

    @UiField
    SimplePanel navBar;

    // @UiField
    // Image editBtn;

    @UiField
    Image logoutBtn;

    @UiField
    SpanElement mail;

    public Desk() {
	initWidget(uiBinder.createAndBindUi(this));
	// editBtn.addStyleName(Resources.INSTANCE.wiseGuiStyle().btnImage());
	logoutBtn.addStyleName(Resources.INSTANCE.wiseGuiStyle().btnImage());
    }

    public void setMail(String loginMail) {
	mail.setInnerText(loginMail);
    }

    public void setContentWidget(Widget contentWidget) {
	content.setWidget(contentWidget);
    }

    public void setNavBarWidget(Widget navBarWidget) {
	navBar.setWidget(navBarWidget);
    }

    @UiHandler( { "logoutBtn" })
    void onClick(ClickEvent e) {
	// if (e.getSource() == editBtn) {
	// Wise_gui.getInstance().editWsdl();
	// } else
	if (e.getSource() == logoutBtn) {
	    Alert.caution(Constants.INSTANCE.logoutMessage(), new Alert.Listener() {
		public void onReply(Alert origin, Reply r) {
		    if (r == Alert.Reply.OK) {
			Wise_gui.getInstance().logout();
		    }
		}
	    });
	}
    }

}
