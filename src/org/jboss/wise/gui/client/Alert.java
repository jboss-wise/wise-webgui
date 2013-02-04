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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Message dialog window
 * 
 * @todo TODO: move message strings into a resource bundle
 * @todo TODO: allow customization of alert properties like size and location
 * 
 * @author <a href="mailto:fabri.wise@javamac.com">Fabrizio Di Giuseppe</a>
 */
public class Alert extends DialogBox {

    private static final String ALERT_W = "320px";

    /**
     * Implement this interface to listen alert events
     */
    public interface Listener {
	void onReply(Alert origin, Reply r);
    }

    /**
     * Reply the alert could send back to the caller that depends on the button
     * the user clicked
     */
    public enum Reply {
	OK(Constants.INSTANCE.okBtnText()), CANCEL(Constants.INSTANCE.cancelBtnText());

	private String text;

	Reply(String t) {
	    text = t;
	}

	String getText() {
	    return text;
	}
    }

    /**
     * Alert types
     */
    public enum Type {
	MESSAGE(Constants.INSTANCE.noteDialogTitle(), Reply.OK), ERROR(Constants.INSTANCE.errorDialogTitle(), Reply.OK), CAUTION(
		Constants.INSTANCE.warningDialogTitle(),
		Reply.CANCEL,
		Reply.OK);

	private String message;

	private Reply replySet[];

	Type(String m, Reply... rs) {
	    message = m;
	    replySet = rs;
	}

	String getMessage() {
	    return message;
	}

	Reply[] getReplySet() {
	    return replySet;
	}
    }

    private static class ReplyButtonClickHandler implements ClickHandler {
	private Reply reply;

	private Alert alert;

	public ReplyButtonClickHandler(Reply r, Alert a) {
	    reply = r;
	    alert = a;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
	 */
	public void onClick(ClickEvent event) {
	    alert.hide();
	    if (alert.listener != null)
		alert.listener.onReply(alert, reply);
	}

    }

    /**
     * Open an alert of the specified type
     * 
     * @param t
     *            alert type
     * @param m
     *            the text message to be displayed
     * @return the button clicked by the user
     */
    public static Alert open(Type t, String m) {
	final Alert a = new Alert(m, t);
	a.show();
	a.center();
	return a;
    }

    /**
     * Open an alert of the specified type with a custom listener
     * 
     * @param t
     *            the alert type
     * @param m
     *            the text message to be displayed
     * @param l
     *            the listener that handle the user choice
     * @return the button clicked by the user
     */
    public static Alert open(Type t, String m, Listener l) {
	final Alert a = new Alert(m, t);
	a.listener = l;
	a.show();
	a.center();
	return a;
    }

    /**
     * Open a message alert
     * 
     * @param m
     *            the text message to be displayed
     * @return the button clicked by the user
     */
    public static Alert message(String m) {
	return open(Type.MESSAGE, m);
    }

    /**
     * Open an error alert
     * 
     * @param message
     *            the text message to be displayed
     * @return the button clicked by the user
     */
    public static Alert error(String message) {
	return open(Type.ERROR, message);
    }

    /**
     * Open a caution alert
     * 
     * @param m
     *            the text message to be displayed
     * @param l
     *            the listener that handle the user choice
     * @return the button clicked by the user
     */
    public static Alert caution(String m, Listener l) {
	return open(Type.CAUTION, m, l);
    }

    Listener listener = null;

    /**
     * creates a new alert window
     * 
     * @param m
     *            the text message to be displayed
     * @param t
     *            the alert type
     */
    public Alert(String m, Type t) {
	super(false, true);
	setAnimationEnabled(true);

	setText(t.getMessage());

	VerticalPanel content = new VerticalPanel();
	content.setWidth(ALERT_W);

	HTML msg = new HTML(m);
	msg.setWidth("100%");
	msg.setWordWrap(true);
	content.add(msg);

	HorizontalPanel buttons = new HorizontalPanel();
	buttons.setWidth("100%");
	buttons.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
	HorizontalPanel bt = new HorizontalPanel();
	for (Reply r : t.getReplySet()) {
	    Button btn = new Button(r.getText());
	    btn.addClickHandler(new ReplyButtonClickHandler(r, this));
	    bt.add(btn);
	}
	buttons.add(bt);
	content.add(buttons);

	setWidget(content);
    }
}
