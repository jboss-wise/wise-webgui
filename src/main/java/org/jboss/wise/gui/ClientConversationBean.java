/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
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

import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jws.soap.SOAPBinding;

import org.jboss.wise.core.client.InvocationResult;
import org.jboss.wise.core.client.WSDynamicClient;
import org.jboss.wise.core.client.WSEndpoint;
import org.jboss.wise.core.client.WSMethod;
import org.jboss.wise.core.client.WSService;
import org.jboss.wise.core.client.WebParameter;
import org.jboss.wise.core.client.builder.WSDynamicClientBuilder;
import org.jboss.wise.core.client.factories.WSDynamicClientFactory;
import org.jboss.wise.core.exception.InvocationException;
import org.jboss.wise.core.exception.WiseRuntimeException;
import org.jboss.wise.gui.treeElement.GroupWiseTreeElement;
import org.jboss.wise.gui.treeElement.LazyLoadWiseTreeElement;
import org.jboss.wise.gui.treeElement.WiseTreeElement;
import org.jboss.wise.gui.treeElement.WiseTreeElementBuilder;
import org.richfaces.component.UITree;
import org.richfaces.event.ItemChangeEvent;
import org.richfaces.model.TreeNodeImpl;

@Named
@ConversationScoped
public class ClientConversationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int CONVERSATION_TIMEOUT = 15 * 60 * 1000; //15 mins instead of default 30 mins
    private static WSDynamicClientBuilder clientBuilder;
    private static CleanupTask<WSDynamicClient> cleanupTask = new CleanupTask<WSDynamicClient>(true);
    
    @Inject Conversation conversation;
    private WSDynamicClient client;
    private String wsdlUrl;
    private List<Service> services;
    private String currentOperation;
    private TreeNodeImpl inputTree;
    private TreeNodeImpl outputTree;
    private String error;
    private UITree inTree;
    
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
	    client = getClientBuilder().verbose(true).keepSource(true).wsdlURL(getWsdlUrl()).maxThreadPoolSize(1).build();
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
		services = convertServicesToGui(client.processServices());
	    } catch (Exception e) {
		error = "Could not parse WSDL from specified URL. Please check logs for further information.";
		logException(e);
	    }
	}
    }
    
    public void parseOperationParameters() {
	if (currentOperation == null) return;
	outputTree = null;
	error = null;
	StringTokenizer st = new StringTokenizer(currentOperation, ";");
	String serviceName = st.nextToken();
	String portName = st.nextToken();
	String operationName = st.nextToken();
	try {
	    inputTree = convertOperationParametersToGui(client.getWSMethod(serviceName, portName, operationName), client);
	} catch (Exception e) {
	    error = toErrorMessage(e);
	    logException(e);
	}
    }
    
    public void performInvocation() {
	outputTree = null;
	error = null;
	StringTokenizer st = new StringTokenizer(currentOperation, ";");
	String serviceName = st.nextToken();
	String portName = st.nextToken();
	String operationName = st.nextToken();
	try {
	    WSMethod wsMethod = client.getWSMethod(serviceName, portName, operationName);
	    Map<String, Object> params = new HashMap<String, Object>();
	    for (Iterator<Object> it = inputTree.getChildrenKeysIterator(); it.hasNext(); ) {
		WiseTreeElement wte = (WiseTreeElement)inputTree.getChild(it.next());
		params.put(wte.getName(), wte.isNil() ? null : wte.toObject());
	    }
	    InvocationResult result = null;
	    try {
		result = wsMethod.invoke(params);
	    } catch (InvocationException e) {
		logException(e);
		error = "Unexpected fault / error received from target endpoint";
	    }
	    if (result != null) {
		outputTree = convertOperationResultToGui(result, client);
		error = null;
	    }
	} catch (Exception e) {
	    error = toErrorMessage(e);
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
	    error = toErrorMessage(e);
	    logException(e);
	}
    }
    
    public void onInputTextFocus(WiseTreeElement el) {
	el.setNotNil(true);
    }
    
    private static TreeNodeImpl convertOperationParametersToGui(WSMethod wsMethod, WSDynamicClient client) {
	WiseTreeElementBuilder builder = new WiseTreeElementBuilder(client);
	TreeNodeImpl rootElement = new TreeNodeImpl();
	Collection<? extends WebParameter> parameters = wsMethod.getWebParams().values();
	SOAPBinding soapBindingAnn = wsMethod.getEndpoint().getUnderlyingObjectClass().getAnnotation(SOAPBinding.class);
	boolean rpcLit = false;
	if (soapBindingAnn != null) {
	    SOAPBinding.Style style = soapBindingAnn.style();
	    rpcLit = style != null && SOAPBinding.Style.RPC.equals(style);
	}
	for (WebParameter parameter : parameters) {
	    WiseTreeElement wte = builder.buildTreeFromType(parameter.getType(), parameter.getName(), !rpcLit);
	    rootElement.addChild(wte.getId(), wte);
	}
	return rootElement;
    }
    
    private static TreeNodeImpl convertOperationResultToGui(InvocationResult result,  WSDynamicClient client) {
	WiseTreeElementBuilder builder = new WiseTreeElementBuilder(client);
	TreeNodeImpl rootElement = new TreeNodeImpl();
	for (Entry<String, Object> res : result.getResult().entrySet()) {
	    Object resObj = res.getValue();
	    WiseTreeElement wte = builder.buildTreeFromType(resObj.getClass(), res.getKey(), resObj, true);
	    rootElement.addChild(wte.getId(), wte);
	}
	return rootElement;
    }
    
    private static List<Service> convertServicesToGui(Map<String, WSService> servicesMap) {
	List<Service> services = new LinkedList<Service>();
	for (Entry<String, WSService> serviceEntry : servicesMap.entrySet()) {
	    Service service = new Service();
	    services.add(service);
	    service.setName(serviceEntry.getKey());
	    List<Port> ports = new LinkedList<Port>();
	    service.setPorts(ports);
	    for (Entry<String, WSEndpoint> endpointEntry : serviceEntry.getValue().processEndpoints().entrySet()) {
		Port port = new Port();
		port.setName(endpointEntry.getKey());
		ports.add(port);
		List<Operation> operations = new LinkedList<Operation>();
		port.setOperations(operations);
		for (Entry<String, WSMethod> methodEntry : endpointEntry.getValue().getWSMethods().entrySet()) {
		    Operation operation = new Operation();
		    operation.setName(methodEntry.getKey());
		    StringBuilder sb = new StringBuilder();
		    sb.append(methodEntry.getKey());
		    sb.append("(");
		    Iterator<? extends WebParameter> paramIt = methodEntry.getValue().getWebParams().values().iterator();
		    while (paramIt.hasNext()) {
			WebParameter param = paramIt.next();
			Type type = param.getType();
			sb.append(type instanceof Class<?> ? ((Class<?>)type).getSimpleName() : type.toString());
			sb.append(" ");
			sb.append(param.getName());
			if (paramIt.hasNext()) {
			    sb.append(", ");
			}
		    }
		    sb.append(")");
		    operation.setFullName(sb.toString());
		    operations.add(operation);
		}
	    }
	}
	return services;
    }
    
    public void updateCurrentOperation(ItemChangeEvent event){
	  setCurrentOperation(event.getNewItemName());
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
    }
    
    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
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

    private static synchronized WSDynamicClientBuilder getClientBuilder() {
	if (clientBuilder == null) {
	    clientBuilder = WSDynamicClientFactory.getJAXWSClientBuilder();
	}
	return clientBuilder;
    }
    
    private static String toErrorMessage(Exception e) {
	StringBuilder sb = new StringBuilder();
	if (e instanceof WiseRuntimeException) {
	    sb.append(e.getMessage());
	} else {
	    sb.append(e.toString());
	}
	if (e.getCause() != null) {
	    sb.append(", caused by ");
	    sb.append(e.getCause());
	}
	sb.append(". Please check logs for further information.");
	return sb.toString();
    }
    
    private static void logException(Exception e) {
	e.printStackTrace(); //TODO!!
    }
}
