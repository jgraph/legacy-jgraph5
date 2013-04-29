/*
 * $Id: DataGraphLayoutCache.java,v 1.1 2009-09-25 15:17:49 david Exp $
 * Copyright (c) 2005, David Benson
 * 
 * All rights reserved. 
 * 
 * This file is licensed under the JGraph software license, a copy of which
 * will have been provided to you in the file LICENSE at the root of your
 * installation directory. If you are unable to locate this file please
 * contact JGraph sales for another copy.
 */
package com.jgraph.layout;

import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewFactory;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

/**
 * A Graph Layout Cache that automatically attaches itself as a listener
 * of its associated graph model. The intend usage is for applications
 * that do not create a JGraph instance. Without a JGraph instance there
 * is no UI (in JGraph 5.x) and it is the UI that causes the layout cache
 * to automatically listen to the model.
 */
public class DataGraphLayoutCache extends GraphLayoutCache implements GraphModelListener {

	/**
	 * Constructs a graph layout cache.
	 */
	public DataGraphLayoutCache() {
		this(new DefaultGraphModel(), new DefaultCellViewFactory());
	}

	/**
	 * Constructs a view for the specified model that uses <code>factory</code>
	 * to create its views.
	 * 
	 * @param model
	 *            the model that constitues the data source
	 */
	public DataGraphLayoutCache(GraphModel model, CellViewFactory factory) {
		this(model, factory, false);
	}

	/**
	 * Constructs a view for the specified model that uses <code>factory</code>
	 * to create its views.
	 * 
	 * @param model
	 *            the model that constitues the data source
	 */
	public DataGraphLayoutCache(GraphModel model, CellViewFactory factory,
			boolean partial) {
		this(model, factory, null, null, partial);
	}

	/**
	 * Constructs a view for the specified model that uses <code>factory</code>
	 * to create its views.
	 * 
	 * @param model
	 *            the model that constitues the data source
	 */
	public DataGraphLayoutCache(GraphModel model, CellViewFactory factory,
			CellView[] cellViews, CellView[] hiddenCellViews, boolean partial) {
		super(model, factory, cellViews, hiddenCellViews, partial);
		if (this.graphModel != null) {
			graphModel.addGraphModelListener(this);
		}
	}
	
	public void graphChanged(GraphModelEvent e) {
		graphChanged(e.getChange());
	}
}