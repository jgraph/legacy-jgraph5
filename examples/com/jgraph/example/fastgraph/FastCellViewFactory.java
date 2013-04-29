/*
 * Copyright (c) 2005, David Benson
 *
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.fastgraph;

import java.io.Serializable;

import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewFactory;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;

/**
 * The implementation of a cell view factory that allows for ports also being
 * vertices
 */
public class FastCellViewFactory implements CellViewFactory, Serializable {
	
	/**
	 * Constructs a view for the specified cell and associates it with the
	 * specified object using the specified CellMapper. This calls refresh on
	 * the created CellView to create all dependent views.
	 * <p>
	 * Note: The mapping needs to be available before the views of child cells
	 * and ports are created.
	 * <b>Note: This method must return new instances!</b>
	 * 
	 * @param cell
	 *            reference to the object in the model
	 */
	public CellView createView(GraphModel model, Object cell) {
		CellView view = null;
		if (model.isPort(cell))
			view = createPortView(cell);
		else if (model.isEdge(cell))
			view = createEdgeView(cell);
		else
			view = createVertexView(cell);
		return view;
	}

	/**
	 * Constructs an EdgeView view for the specified object.
	 */
	protected EdgeView createEdgeView(Object cell) {
		return new FastEdgeView(cell);
	}

	/**
	 * Constructs a PortView view for the specified object.
	 */
	protected PortView createPortView(Object cell) {
		return new FastPortView(cell);
	}

	/**
	 * Constructs a VertexView view for the specified object.
	 */
	protected VertexView createVertexView(Object cell) {
		if (cell instanceof FastCircleCell) {
			return new FastCircleView(cell);
		} else {
			return new FastVertexView(cell);
		}
	}
}