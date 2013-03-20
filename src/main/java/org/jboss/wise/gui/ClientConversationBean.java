/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.wise.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;
import org.jboss.wise.core.client.InvocationResult;
import org.jboss.wise.core.client.WSDynamicClient;
import org.jboss.wise.core.client.WSEndpoint;
import org.jboss.wise.core.client.WSMethod;
import org.jboss.wise.core.client.builder.WSDynamicClientBuilder;
import org.jboss.wise.core.client.impl.reflection.builder.ReflectionBasedWSDynamicClientBuilder;
import org.jboss.wise.core.exception.InvocationException;
import org.jboss.wise.core.utils.JBossLoggingOutputStream;
import org.jboss.wise.gui.treeElement.GroupWiseTreeElement;
import org.jboss.wise.gui.treeElement.LazyLoadWiseTreeElement;
import org.jboss.wise.gui.treeElement.WiseTreeElement;
import org.richfaces.component.UITree;
import org.richfaces.event.ItemChangeEvent;
import org.richfaces.model.TreeNodeImpl;

@Named
@ConversationScoped
public class ClientConversationBean implements Serializable {

    private static final long serialVersionUID = -3778997821476776895L;
    
    private static final int CONVERSATION_TIMEOUT = 15 * 60 * 1000; //15 mins instead of default 30 mins
    private static CleanupTask<WSDynamicClient> cleanupTask = new CleanupTask<WSDynamicClient>(true);
    private static Logger logger = Logger.getLogger(ClientConversationBean.class);
    private static PrintStream ps = new PrintStream(new JBossLoggingOutputStream(logger, Logger.Level.DEBUG), true);
    
    @Inject Conversation conversation;
    private WSDynamicClient client;
    private String wsdlUrl;
    private String wsdlUser;
    private String wsdlPwd;
    private String invocationUrl;
    private String invocationUser;
    private String invocationPwd;
    private List<Service> services;
    private String currentOperation;
    private TreeNodeImpl inputTree;
    private TreeNodeImpl outputTree;
    private String error;
    private UITree inTree;
    private String requestPreview;
    private String responseMessage;
    private String requestActiveTab;

    @PostConstruct
    public void init() {
	//this is called each time a new browser tab is used and whenever the conversation expires (hence a new bean is created)
	conversation.begin();
	conversation.setTimeout(CONVERSATION_TIMEOUT);
    }
    
    public void readWsdl() {
	cleanup();
	//restart conversation
	conversation.end();
	conversation.begin();
	try {
	    WSDynamicClientBuilder builder = new ReflectionBasedWSDynamicClientBuilder().verbose(true).messageStream(ps).keepSource(true).maxThreadPoolSize(1);
	    builder.userName(wsdlUser);
	    invocationUser = wsdlUser;
	    builder.password(wsdlPwd);
	    invocationPwd = wsdlPwd;
	    client = builder.wsdlURL(getWsdlUrl()).build();
	    cleanupTask.addRef(client, System.currentTimeMillis() + CONVERSATION_TIMEOUT, new CleanupTask.CleanupCallback<WSDynamicClient>() {
		@Override
		public void cleanup(WSDynamicClient data) {
		    data.close();
		}
	    });
	} catch (Exception e) {
	    error = "Could not read WSDL from specified URL. Please check logs for further information.";
	    logException(e);
	}
	if (client != null) {
	    try {
		services = ClientHelper.convertServicesToGui(client.processServices());
	    } catch (Exception e) {
		error = "Could not parse WSDL from specified URL. Please check logs for further information.";
		logException(e);
	    }
	}
    }
    
    public void parseOperationParameters() {
	if (currentOperation == null) return;
	outputTree = null;
	responseMessage = null;
	error = null;
	try {
	    inputTree = ClientHelper.convertOperationParametersToGui(ClientHelper.getWSMethod(currentOperation, client), client);
	} catch (Exception e) {
	    error = ClientHelper.toErrorMessage(e);
	    logException(e);
	}
    }
    
    public void performInvocation() {
	outputTree = null;
	error = null;
	responseMessage = null;
	try {
	    WSMethod wsMethod = ClientHelper.getWSMethod(currentOperation, client);
	    InvocationResult result = null;
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    try {
		Map<String, Object> params = ClientHelper.processGUIParameters(inputTree);
		ClientHelper.addOUTParameters(params, wsMethod, client);
		final WSEndpoint endpoint = wsMethod.getEndpoint();
		endpoint.setTargetUrl(invocationUrl);
		endpoint.setPassword(invocationPwd);
		endpoint.setUsername(invocationUser);
		endpoint.addHandler(new ResponseLogHandler(os));
		result = wsMethod.invoke(params);
	    } catch (InvocationException e) {
		logException(e);
		error = "Unexpected fault / error received from target endpoint";
	    } finally {
		responseMessage = os.toString("UTF-8");
		if (responseMessage.trim().length() == 0) {
		    responseMessage = null;
		}
	    }
	    if (result != null) {
		outputTree = ClientHelper.convertOperationResultToGui(result, client);
		error = null;
	    }
	} catch (Exception e) {
	    error = ClientHelper.toErrorMessage(e);
	    logException(e);
	}
    }
    
    public void generateRequestPreview() {
	requestPreview = null;
	try {
	    WSMethod wsMethod = ClientHelper.getWSMethod(currentOperation, client);
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    wsMethod.getEndpoint().setTargetUrl(null);
	    wsMethod.writeRequestPreview(ClientHelper.processGUIParameters(inputTree), os);
	    requestPreview = os.toString("UTF-8");
	} catch (Exception e) {
	    requestPreview = ClientHelper.toErrorMessage(e);
	    logException(e);
	}
    }
    
    public void addChild(GroupWiseTreeElement el) {
	el.incrementChildren();
    }
    
    public void removeChild(WiseTreeElement el) {
	((GroupWiseTreeElement)el.getParent()).removeChild(el.getId());
    }
    
    public void lazyLoadChild(LazyLoadWiseTreeElement el) {
	try {
	    el.resolveReference();
	} catch (Exception e) {
	    error = ClientHelper.toErrorMessage(e);
	    logException(e);
	}
    }
    
    public void onInputFocus(WiseTreeElement el) {
	el.setNotNil(true);
    }
    
    public void updateCurrentOperation(ItemChangeEvent event) {
	String ev = event.getNewItemName();
	//skip empty/null operation values as those comes from expansion/collapse of the menu panel
	if (ev != null && ev.length() > 0) {
	    setCurrentOperation(ev);
	}
    }
    
    public boolean isResponseAvailable() {
	return outputTree != null || responseMessage != null;
    }
    
    private void cleanup() {
	if (client != null) {
	    cleanupTask.removeRef(client);
    	    client.close();
    	    client = null;
	}
	services = null;
	currentOperation = null;
	inputTree = null;
	outputTree = null;
	if (inTree != null) {
	    inTree.clearInitialState();
	}
	inputTree = null;
	error = null;
	responseMessage = null;
	invocationUrl = null;
    }
    
    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getWsdlUser() {
        return wsdlUser;
    }

    public void setWsdlUser(String wsdlUser) {
	if (wsdlUser != null && wsdlUser.length() == 0) {
	    this.wsdlUser = null;
	} else {
	    this.wsdlUser = wsdlUser;
	}
    }

    public String getWsdlPwd() {
        return wsdlPwd;
    }

    public void setWsdlPwd(String wsdlPwd) {
	if (wsdlPwd != null && wsdlPwd.length() == 0) {
	    this.wsdlPwd = null;
	} else {
	    this.wsdlPwd = wsdlPwd;
	}
    }

    public String getInvocationUrl() {
        return invocationUrl;
    }

    public void setInvocationUrl(String invocationUrl) {
	if (invocationUrl != null && invocationUrl.length() == 0) {
	    this.invocationUrl = null;
	} else {
	    this.invocationUrl = invocationUrl;
	}
    }

    public String getInvocationUser() {
        return invocationUser;
    }

    public void setInvocationUser(String invocationUser) {
	if (invocationUser != null && invocationUser.length() == 0) {
	    this.invocationUser = null;
	} else {
	    this.invocationUser = invocationUser;
	}
    }

    public String getInvocationPwd() {
        return invocationPwd;
    }

    public void setInvocationPwd(String invocationPwd) {
	if (invocationPwd != null && invocationPwd.length() == 0) {
	    this.invocationPwd = null;
	} else {
	    this.invocationPwd = invocationPwd;
	}
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public String getCurrentOperation() {
        return currentOperation;
    }

    public void setCurrentOperation(String currentOperation) {
        this.currentOperation = currentOperation;
    }

    public UITree getInTree() {
        return inTree;
    }

    public void setInTree(UITree inTree) {
        this.inTree = inTree;
    }

    public TreeNodeImpl getInputTree() {
        return inputTree;
    }

    public void setInputTree(TreeNodeImpl inputTree) {
        this.inputTree = inputTree;
    }

    public TreeNodeImpl getOutputTree() {
        return outputTree;
    }

    public void setOutputTree(TreeNodeImpl outputTree) {
        this.outputTree = outputTree;
    }
    
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    
    public String getRequestPreview() {
        return requestPreview;
    }

    public void setRequestPreview(String requestPreview) {
        this.requestPreview = requestPreview;
    }

    public String getRequestActiveTab() {
        return requestActiveTab;
    }

    public void setRequestActiveTab(String requestActiveTab) {
        this.requestActiveTab = requestActiveTab;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    private static void logException(Exception e) {
	logger.error("", e);
    }
}
