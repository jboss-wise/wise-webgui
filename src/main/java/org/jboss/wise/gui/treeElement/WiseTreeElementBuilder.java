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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    
    private WSDynamicClient client;
    
    public WiseTreeElementBuilder(WSDynamicClient client) {
	this.client = client;
    }
    
    public WiseTreeElement buildTreeFromType(Type type, String name, boolean nillable) {
	return buildTreeFromType(type, name, null, nillable, null, null, new HashMap<Type, WiseTreeElement>(), new HashSet<Type>());
    }
    
    public WiseTreeElement buildTreeFromType(Type type, String name, Object obj, boolean nillable) {
	return buildTreeFromType(type, name, obj, nillable, null, null, new HashMap<Type, WiseTreeElement>(), new HashSet<Type>());
    }

    private WiseTreeElement buildTreeFromType(Type type,
	    				      String name,
	    				      Object obj,
	    				      boolean nillable,
	    				      Class<?> scope,
	    				      String namespace,
	    				      Map<Type, WiseTreeElement> typeMap,
	    				      Set<Type> stack) {
	Logger.getLogger(this.getClass()).debug("=> Converting parameter '" + name + "', type '" + type + "'");
	if (type instanceof ParameterizedType) {
	    Logger.getLogger(this.getClass()).debug("Parameterized type...");
	    ParameterizedType pt = (ParameterizedType) type;
	    return this.buildParameterizedType(pt, name, obj, scope, namespace, typeMap, stack);
	} else {
	    Logger.getLogger(this.getClass()).debug("Not a parameterized type... casting to Class");
	    
	    return this.buildFromClass((Class<?>) type, name, obj, nillable, typeMap, stack);

	}
    }

    @SuppressWarnings("rawtypes")
    private WiseTreeElement buildParameterizedType(ParameterizedType pt,
	                                           String name,
	                                           Object obj,
	                                           Class<?> scope,
	                                           String namespace,
	                                           Map<Type, WiseTreeElement> typeMap,
		    				   Set<Type> stack) {
	Type firstTypeArg = pt.getActualTypeArguments()[0];
	if (Collection.class.isAssignableFrom((Class<?>) pt.getRawType())) {
	    WiseTreeElement prototype = this.buildTreeFromType(firstTypeArg, name, null, true, null, null, typeMap, stack);
	    GroupWiseTreeElement group = new GroupWiseTreeElement(pt, name, prototype);
	    if (obj != null) {
		for (Object o : (Collection) obj) {
		    group.addChild(IDGenerator.nextVal(), this.buildTreeFromType(firstTypeArg, name, o, true, null, null, typeMap, stack));
		}
	    }
	    return group;
	} else {
	    ParameterizedWiseTreeElement parameterized = new ParameterizedWiseTreeElement(pt, name, client, scope, namespace);
	    if (obj != null && obj instanceof JAXBElement) {
		obj = ((JAXBElement)obj).getValue();
	    }
	    WiseTreeElement element = this.buildTreeFromType(firstTypeArg, name, obj, true, null, null, typeMap, stack);
	    parameterized.addChild(element.getId(), element);
	    return parameterized;
	}
    }

    private WiseTreeElement buildFromClass(Class<?> cl,
	                                   String name,
	                                   Object obj,
	                                   boolean nillable,
	                                   Map<Type, WiseTreeElement> typeMap,
	    				   Set<Type> stack) {

	if (cl.isArray()) {
	    if (byte.class.equals(cl.getComponentType())) {
		ByteArrayWiseTreeElement element = new ByteArrayWiseTreeElement(cl, name, null);
		if (obj != null) {
		    element.parseObject(obj);
		}
		return element;
	    }
	    Logger.getLogger(this.getClass()).debug("* array");
	    Logger.getLogger(this.getClass()).debug("Component type: " + cl.getComponentType());
	    throw new WiseRuntimeException("Converter doesn't support this Object[] yet.");
	}

	if (isSimpleType(cl, client)) {
	    Logger.getLogger(this.getClass()).debug("* simple");
	    SimpleWiseTreeElement element = SimpleWiseTreeElementFactory.create(cl, name, obj);
	    if (!nillable) {
		element.enforceNotNillable();
	    }
	    return element;
	} else { // complex
	    if (stack.contains(cl)) {
		Logger.getLogger(this.getClass()).debug("* lazy");
		return new LazyLoadWiseTreeElement(cl, name, typeMap);
	    }
	    
	    Logger.getLogger(this.getClass()).debug("* complex");
	    
	    ComplexWiseTreeElement complex = new ComplexWiseTreeElement(cl, name);
	    stack.add(cl);
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
		WiseTreeElement element = this.buildTreeFromType(field.getGenericType(), fieldName, fieldValue, true, cl, namespace, typeMap, stack);
		complex.addChild(element.getId(), element);
	    }
	    stack.remove(cl);
	    typeMap.put(cl, complex.clone());
	    if (!nillable) {
		complex.setNillable(false);
	    }
	    return complex;
	}
    }
    
    private static boolean isSimpleType(Class<?> cl, WSDynamicClient client) {
	return cl.isEnum() || cl.isPrimitive() || client.getClassLoader() != cl.getClassLoader();
    }
}
