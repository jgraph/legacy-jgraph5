/*
 * Copyright (c) 2005, David Benson
 *
 * See LICENSE file in distribution for licensing details of this source file
 */

package com.jgraph.example.fastgraph;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;

public class FastCircleCell extends DefaultGraphCell {

	public FastCircleCell() {
		this(null);
	}

	public FastCircleCell(Object userObject) {
		super(userObject);
	}
	
	public FastCircleCell(Object userObject, AttributeMap storageMap) {
		super(userObject, storageMap);
	}

}