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
package org.jboss.wise.gui.treeElement;

import java.lang.reflect.ParameterizedType;
import java.util.Iterator;

import org.jboss.wise.core.client.WSDynamicClient;
import org.jboss.wise.core.exception.WiseRuntimeException;
import org.jboss.wise.core.utils.IDGenerator;

/**
 * A tree element to handle JAXBElement<T>.
 * 
 * @author alessio.soldano@jboss.com
 */
public class ParameterizedWiseTreeElement extends WiseTreeElement {

    private static final long serialVersionUID = 5492389675960954725L;

    private WSDynamicClient client;

    private Class<?> scope;

    private String namespace;

    public ParameterizedWiseTreeElement() {
	this.kind = PARAMETERIZED;
	this.id = IDGenerator.nextVal();
    }

    public ParameterizedWiseTreeElement(ParameterizedType classType, String name, WSDynamicClient client, Class<?> scope, String namespace) {
	this.kind = PARAMETERIZED;
	this.id = IDGenerator.nextVal();
	this.classType = classType;
	this.nil = false;
	this.name = name;
	this.client = client;
	this.scope = scope;
	this.namespace = namespace;
    }

    @Override
    public WiseTreeElement clone() {
	ParameterizedWiseTreeElement element = new ParameterizedWiseTreeElement();
	element.setName(this.name);
	element.setNil(this.nil);
	element.setClassType(this.classType);
	element.setRemovable(this.isRemovable());
	element.setNillable(this.isNillable());
	element.setClient(this.client);
	element.setScope(this.scope);
	element.setNamespace(this.namespace);
	Iterator<Object> keyIt = this.getChildrenKeysIterator();
	while (keyIt.hasNext()) { // actually 1 child only
	    WiseTreeElement child = (WiseTreeElement)this.getChild(keyIt.next());
	    element.addChild(child.getId(), (WiseTreeElement) child.clone());
	}
	return element;
    }

    @Override
    public Object toObject() throws WiseRuntimeException {
	return null;
//	if (client == null) {
//	    throw new WiseRuntimeException("null client: impossible conversion of ParameterizedWiseTreeElemnt to object");
//	}
//	return isLeaf() ? null : client.instanceXmlElementDecl(this.name, this.scope, this.namespace, this.getChildrenAsList().get(0).toObject());
    }

    public void setClient(WSDynamicClient client) {
	this.client = client;
    }

    public void setScope(Class<?> scope) {
	this.scope = scope;
    }

    public void setNamespace(String namespace) {
	this.namespace = namespace;
    }

    public String getNamespace() {
	return namespace;
    }
}