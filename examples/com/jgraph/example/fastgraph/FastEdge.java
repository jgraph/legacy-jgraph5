/*
 * Copyright (c) 2005, David Benson
 *
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.fastgraph;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultEdge;

public class FastEdge extends DefaultEdge {
	
	public FastEdge(Object userObject, AttributeMap storageMap) {
		super(userObject, storageMap);
	}
	
	/**
	 * Sets the attributes.
	 * @param attributes The attributes to set
	 */
	public void setAttributes(AttributeMap attributes) {
		if (attributes == null)
			attributes = new AttributeMap(1, 1.0f);
		this.attributes = attributes;
	}
}