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
import java.util.HashMap;
import java.util.Map;

import org.jboss.wise.gui.client.Alert.Reply;
import org.jboss.wise.gui.shared.ServiceWsdl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author <a href="mailto:fabri.wise@javamac.com">Fabrizio Di Giuseppe</a>
 */
public class WsdlList extends Composite {

    private static final String ODD = "odd";

    private static final String EVEN = "even";

    private static final String SELECTED = "selected";

    private static WsdlListUiBinder uiBinder = GWT.create(WsdlListUiBinder.class);

    interface WsdlListUiBinder extends UiBinder<HTMLPanel, WsdlList> {
    }

    @UiField
    TextBox searchBox;

    @UiField
    TableSectionElement content;

    @UiField
    Button deleteBtn;

    @UiField
    Button editBtn;

    @UiField
    Button duplicateBtn;

    @UiField
    Button newBtn;

    @UiField
    Button openBtn;

    private HTMLPanel panel;

    private String contentId;

    private RowWidget selectedRow = null;

    private Map<Long, RowWidget> rowWidgets = null;

    public WsdlList() {
	panel = uiBinder.createAndBindUi(this);
	initWidget(panel);
	contentId = HTMLPanel.createUniqueId();
	content.setId(contentId);
	// setList(Wise_gui.getInstance().getSavedWsdlList());
	enableButtons();
    }

    public void refresh() {
	Map<Long, ServiceWsdl> list = Wise_gui.getInstance().getWsdlList();
	NodeList<TableRowElement> rows = content.getRows();
	if (rows != null) {
	    while (rows.getLength() > 0) {
		content.deleteRow(-1);
	    }
	}
	rowWidgets = new HashMap<Long, RowWidget>();
	for (Map.Entry<Long, ServiceWsdl> wsdlEntry : list.entrySet()) {
	    addRow(wsdlEntry.getKey(), wsdlEntry.getValue());
	}
	select(Wise_gui.getInstance().getSelectedWsdlId());
    }

    private RowWidget addRow(Long id, ServiceWsdl wsdl) {
	RowWidget rw = new RowWidget(id, wsdl);
	com.google.gwt.user.client.Element newRow = rw.getElement();
	assert (newRow != null);
	if (rowWidgets.size() % 2 == 0) {
	    newRow.addClassName(EVEN);
	} else {
	    newRow.addClassName(ODD);
	}
	rowWidgets.put(id, rw);
	panel.add(rw, contentId);
	return rw;
    }

    public void refreshRow(Long id) {
	assert id != null;
	assert rowWidgets != null;
	ServiceWsdl wsdl = Wise_gui.getInstance().getWsdlList().get(id);
	assert wsdl != null;
	RowWidget rw = rowWidgets.get(id);
	if (rw != null) {
	    rw.update(wsdl);
	} else {
	    addRow(id, wsdl);
	}
    }

    public void select(RowWidget newSelectedRow) {
	if (newSelectedRow != selectedRow) {
	    if (selectedRow != null) {
		selectedRow.removeStyleName(SELECTED);
	    }
	    if (newSelectedRow != null) {
		newSelectedRow.addStyleName(SELECTED);
	    }
	    selectedRow = newSelectedRow;
	    enableButtons();
	}
    }

    public void select(Long wsdlId) {
	if (wsdlId == null) {
	    select((RowWidget) null);
	} else {
	    select(rowWidgets.get(wsdlId));
	}
    }

    public void enableButtons() {
	if (Wise_gui.getInstance().getSelectedWsdlId() != null) {
	    deleteBtn.setEnabled(true);
	    editBtn.setEnabled(true);
	    openBtn.setEnabled(true);
	    duplicateBtn.setEnabled(true);
	} else {
	    deleteBtn.setEnabled(false);
	    editBtn.setEnabled(false);
	    openBtn.setEnabled(false);
	    duplicateBtn.setEnabled(false);
	}
	newBtn.setEnabled(true);
    }

    @UiHandler( { "deleteBtn", "editBtn", "newBtn", "openBtn", "duplicateBtn" })
    void onClick(ClickEvent e) {
	if (e.getSource() == deleteBtn) {
	    Alert.caution(Constants.INSTANCE.deleteWsdlMessage(), new Alert.Listener() {
		public void onReply(Alert origin, Reply r) {
		    if (r == Alert.Reply.OK) {
			Wise_gui.getInstance().deleteWsdl();
		    }
		}
	    });
	} else if (e.getSource() == editBtn) {
	    Wise_gui.getInstance().editWsdl();
	} else if (e.getSource() == newBtn) {
	    Wise_gui.getInstance().newWsdl();
	} else if (e.getSource() == openBtn) {
	    Wise_gui.getInstance().retrieveWsdl();
	} else if (e.getSource() == duplicateBtn) {
	    Alert.caution(Constants.INSTANCE.duplicateWsdlMessage(), new Alert.Listener() {
		public void onReply(Alert origin, Reply r) {
		    if (r == Alert.Reply.OK) {
			Wise_gui.getInstance().duplicateWsdl();
		    }
		}
	    });
	}
    }

    private static DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("dd/MM/yyyy HH:mm");

    private class RowWidget extends ComplexPanel implements ClickHandler {

	private CellWidget nameWidget;

	private CellWidget descriptionWidget;

	private CellWidget savingDateWidget;

	Long key;

	ServiceWsdl wsdl;

	public RowWidget(Long key, ServiceWsdl wsdl) {
	    setElement(Document.get().createTRElement());
	    this.key = key;
	    this.wsdl = wsdl;
	    this.nameWidget = new CellWidget("20%", wsdl.getName());
	    this.descriptionWidget = new CellWidget("60%", wsdl.getNotes());
	    this.savingDateWidget = new CellWidget("20%", format(wsdl.getSavingDate()));
	    this.add(nameWidget);
	    this.add(descriptionWidget);
	    this.add(savingDateWidget);
	}

	/**
	 * @param wsdl
	 */
	public void update(ServiceWsdl wsdl) {
	    nameWidget.setText(wsdl.getName());
	    descriptionWidget.setText(wsdl.getNotes());
	    savingDateWidget.setText(format(wsdl.getSavingDate()));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
	 */
	public void onClick(ClickEvent event) {
	    Wise_gui.getInstance().setSelectedWsdlId(this.key);
	}

	@Override
	public void add(Widget cell) {
	    add(cell, getElement());
	}

	private class CellWidget extends Widget implements HasClickHandlers, ClickHandler {

	    public CellWidget(String width, String text) {
		setElement(DOM.createTD());
		getElement().setAttribute("width", width);
		getElement().setInnerText(text);
		addClickHandler(this);
	    }

	    /**
	     * @param text
	     */
	    public void setText(String text) {
		getElement().setInnerText(text);
	    }

	    /**
	     * {@inheritDoc}
	     * 
	     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
	     */
	    public void onClick(ClickEvent event) {
		Wise_gui.getInstance().setSelectedWsdlId(RowWidget.this.key);
		select(RowWidget.this);
	    }

	    /**
	     * {@inheritDoc}
	     * 
	     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
	     */
	    public HandlerRegistration addClickHandler(ClickHandler handler) {
		return addDomHandler(handler, ClickEvent.getType());
	    }
	}

	@SuppressWarnings("synthetic-access")
	private String format(Date v) {
	    if (v == null)
		return "";
	    return dateTimeFormat.format(v);
	}

    }
}
