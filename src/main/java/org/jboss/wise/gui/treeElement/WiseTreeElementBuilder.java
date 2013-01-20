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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;

import org.jboss.logging.Logger;
import org.jboss.wise.core.client.WSDynamicClient;
import org.jboss.wise.core.exception.WiseRuntimeException;
import org.jboss.wise.core.utils.IDGenerator;
import org.jboss.wise.core.utils.ReflectionUtils;

/**
 * Builds WiseTreeElements given a type or class
 * 
 * (Type/Class) + [Object] ===> Tree
 * 
 * @author alessio.soldano@jboss.com
 */
public class WiseTreeElementBuilder {

    public WiseTreeElement buildTreeFromType(Type type, String name, WSDynamicClient client) {
	return buildTreeFromType(type, name, client, null, null, null);
    }
    
    public WiseTreeElement buildTreeFromType(Type type, String name, WSDynamicClient client, Object obj) {
	return buildTreeFromType(type, name, client, obj, null, null);
    }

    private WiseTreeElement buildTreeFromType(Type type, String name, WSDynamicClient client, Object obj, Class<?> scope, String namespace) {
	Logger.getLogger(this.getClass()).debug("=> Converting parameter '" + name + "', type '" + type + "'");
	if (type instanceof ParameterizedType) {
	    Logger.getLogger(this.getClass()).debug("Parameterized type...");
	    ParameterizedType pt = (ParameterizedType) type;
	    return this.buildParameterizedType(pt, name, obj, client, scope, namespace);
	} else {
	    Logger.getLogger(this.getClass()).debug("Not a parameterized type... casting to Class");
	    return this.buildFromClass((Class<?>) type, name, obj, client);

	}
    }

    @SuppressWarnings("rawtypes")
    private WiseTreeElement buildParameterizedType(ParameterizedType pt, String name, Object obj, WSDynamicClient client, Class<?> scope, String namespace) {
	if (Collection.class.isAssignableFrom((Class<?>) pt.getRawType())) {
	    WiseTreeElement prototype = this.buildTreeFromType(pt.getActualTypeArguments()[0], name, client, null);
	    GroupWiseTreeElement group = new GroupWiseTreeElement(pt, name, prototype);
	    if (obj != null) {
		for (Object o : (Collection) obj) {
		    group.addChild(IDGenerator.nextVal(), this.buildTreeFromType(pt.getActualTypeArguments()[0], name, client, o));
		}
	    }
	    return group;
	} else {
	    ParameterizedWiseTreeElement parameterized = new ParameterizedWiseTreeElement(pt, name, client, scope, namespace);
	    if (obj != null && obj instanceof JAXBElement) {
		obj = ((JAXBElement)obj).getValue();
	    }
	    WiseTreeElement element = this.buildTreeFromType(pt.getActualTypeArguments()[0], name, client, obj);
	    parameterized.addChild(element.getId(), element);
	    return parameterized;
	}
    }

    private WiseTreeElement buildFromClass(Class<?> cl, String name, Object obj, WSDynamicClient client) {

	if (cl.isArray()) {
	    Logger.getLogger(this.getClass()).debug("* array");
	    Logger.getLogger(this.getClass()).debug("Component type: " + cl.getComponentType());
	    throw new WiseRuntimeException("Converter doesn't support this Object[] yet.");
	}

	if (cl.isEnum() || cl.isPrimitive() || client.getClassLoader() != cl.getClassLoader()) {
	    Logger.getLogger(this.getClass()).debug("* simple");
	    SimpleWiseTreeElement element = SimpleWiseTreeElementFactory.create(cl, name);
	    element.parseObject(obj);
	    return element;
	} else { // complex
	    Logger.getLogger(this.getClass()).debug("* complex");
	    ComplexWiseTreeElement complex = new ComplexWiseTreeElement(cl, name);

	    for (Field field : ReflectionUtils.getAllFields(cl)) {
		XmlElement elemAnnotation = field.getAnnotation(XmlElement.class);
		XmlElementRef refAnnotation = field.getAnnotation(XmlElementRef.class);
		String fieldName = null;
		String namespace = null;
		if (elemAnnotation != null && !elemAnnotation.name().startsWith("#")) {
		    fieldName =  elemAnnotation.name();
		}
		if (refAnnotation != null) {
		    fieldName = refAnnotation.name();
		    namespace = refAnnotation.namespace();
		}
		if (fieldName == null) {
		    fieldName = field.getName();
		}
		//String fieldName = (annotation != null && !annotation.name().startsWith("#")) ? annotation.name() : field.getName();
		Object fieldValue = null;
		if (obj != null) {
		    try {
			Method getter = cl.getMethod(ReflectionUtils.getGetter(field), (Class[]) null);
			fieldValue = getter.invoke(obj, (Object[]) null);
		    } catch (Exception e) {
			throw new WiseRuntimeException("Error calling getter method for field " + field, e);
		    }
		}
		WiseTreeElement element = this.buildTreeFromType(field.getGenericType(), fieldName, client, fieldValue, cl, namespace);
		complex.addChild(element.getId(), element);
	    }
	    return complex;
	}
    }
}
