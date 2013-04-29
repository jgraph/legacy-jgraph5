/*
 * Copyright (c) 2005, David Benson
 *
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.fastgraph;

import java.util.Map;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.VertexView;

public class FastVertexView extends VertexView {

	/**
	 * Default constructor
	 */
	public FastVertexView() {
		super();
	}

	/**
	 * @param cell
	 */
	public FastVertexView(Object cell) {
		super(cell);
	}

	/**
	 * Hook from AbstractCellView, avoid creating attribute map until as late
	 * as possible
	 */
	protected AttributeMap createAttributeMap() {
		return AttributeMap.emptyAttributeMap;
	}

	/**
	 * If the model is a FastGraphModel check the undoDisabled flag. If set,
	 * copy the reference from the model attributes. This will cause the
	 * preview of a drag to edit in-place, but since undo is disabled this
	 * will only cause a performance improvement.
	 */
	protected AttributeMap getCellAttributes(GraphModel model) {
		if (model instanceof FastGraphModel) {
			if (((FastGraphModel)model).undoDisabled) {
				return model.getAttributes(cell);
			}
		}
		return (AttributeMap) model.getAttributes(cell).clone();
	}

	protected void mergeAttributes() {
	}

	public void setAttributes(AttributeMap attributes) {
		if (allAttributes == null)
			allAttributes = new AttributeMap(4, 0.75f);
		this.allAttributes = attributes;
	}
	
	public Map changeAttributes(GraphLayoutCache cache, Map change) {
		if (change != null) {
			Map undo = allAttributes.applyMap(change);
			update(cache);
			return undo;
		}
		return null;
	}
}
