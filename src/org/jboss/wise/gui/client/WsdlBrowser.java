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

import java.util.List;

import org.jboss.wise.gui.shared.Operation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author <a href="mailto:fabri.wise@javamac.com">Fabrizio Di Giuseppe</a>
 */
public class WsdlBrowser extends Composite {

    private static WsdlBrowserUiBinder uiBinder = GWT.create(WsdlBrowserUiBinder.class);

    @UiField
    ListBox operations;

    @UiField
    SimplePanel scrapBook;

    private Sheet[] sheets;

    interface WsdlBrowserUiBinder extends UiBinder<Widget, WsdlBrowser> {
    }

    public WsdlBrowser() {
	initWidget(uiBinder.createAndBindUi(this));
	List<Operation> ops = Wise_gui.getInstance().getOperations();
	for (Operation op : ops) {
	    operations.addItem(op.getName());
	}
	sheets = new Sheet[Wise_gui.getInstance().getOperations().size()];
    }

    @UiHandler("operations")
    void handleClick(ClickEvent e) {
    }

    @UiHandler("operations")
    void handleChange(ChangeEvent e) {
	if (e.getSource() == operations) {
	    selectOperation(operations.getSelectedIndex());
	}
    }

    /**
     * @param selectedIndex
     */
    public void selectOperation(int selectedIndex) {
	assert selectedIndex >= 0 && selectedIndex < sheets.length;
	Sheet sheet = sheets[selectedIndex];
	if (sheet == null) {
	    sheets[selectedIndex] = sheet = new Sheet(Wise_gui.getInstance().getOperations().get(selectedIndex));
	}
	scrapBook.setWidget(sheet);
    }
}
