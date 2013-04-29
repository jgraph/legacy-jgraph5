/*
 * $Id: JGraphBusinessObject.java,v 1.2 2005-08-09 08:40:47 david Exp $
 * 
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.adapter;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Gaudenz Alder
 * 
 * An object that represents an entity with an arbitrary set of properties.
 */
public class JGraphBusinessObject implements Cloneable, Serializable {

	protected Map properties = new Hashtable();

	protected String valueKey = "value";

	public JGraphBusinessObject() {
		this("");
	}

	public JGraphBusinessObject(Object userObject) {
		setValue(userObject);
	}

	/**
	 * @return Returns the properties.
	 */
	public Map getProperties() {
		return properties;
	}

	/**
	 * @param properties
	 *            The properties to set.
	 */
	public void setProperties(Map properties) {
		this.properties = properties;
	}

	public void setValue(Object value) {
		putProperty(valueKey, value);
	}

	public Object getValue() {
		return getProperty(valueKey);
	}

	/**
	 * @return Returns the labelKey.
	 */
	public String getValueKey() {
		return valueKey;
	}

	/**
	 * @param valueKey
	 *            The labelKey to set.
	 */
	public void setValueKey(String valueKey) {
		this.valueKey = valueKey;
	}

	public Object putProperty(Object key, Object value) {
		if (key != null && value != null)
			return properties.put(key, value);
		return null;
	}

	public Object getProperty(Object key) {
		if (key != null)
			return properties.get(key);
		return null;
	}

	public String toString() {
		Object value = getValue();
		if (value != null)
			return String.valueOf(value);
		return "";
	}

	public Object clone() {
		JGraphBusinessObject clone;
		try {
			clone = (JGraphBusinessObject) super.clone();
		} catch (CloneNotSupportedException e) {
			clone = new JGraphBusinessObject();
		}
		clone.setProperties(new Hashtable(getProperties()));
		clone.setValueKey(getValueKey());
		return clone;
	}

}