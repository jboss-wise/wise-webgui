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

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

/**
 * @author <a href="mailto:fabri.wise@javamac.com">Fabrizio Di Giuseppe</a>
 */
public interface Constants extends Messages {

    Constants INSTANCE = GWT.create(Constants.class);

    String okBtnText();

    String cancelBtnText();

    String noteDialogTitle();

    String errorDialogTitle();

    String warningDialogTitle();

    String deleteWsdlMessage();

    String duplicateWsdlMessage();

    String duplicatedWsdlName(String originalName);

    String loginError();

    String logoutMessage();

    String wrongReminderMail();

    String registerError();

    String saveWsdlError();

    String deleteWsdlError();

    String editWsdlDialogCaption();

    String newWsdlDialogCaption();

    String applicationException(Throwable caught);

}
