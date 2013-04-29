/*
 * $Id: JGraphAdapterBackend.java,v 1.1.1.1 2005-08-06 05:26:45 gaudenz Exp $
 * 
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.adapter;

import java.util.Map;

/**
 * TODO Support parent child relations and ports, but make it easy to ignore. We
 * do not use an event-model for the backend since the calling of these methods
 * is already based on an event information, and we use the commit / rollback to
 * mimic transactional behaviour.
 */
public interface JGraphAdapterBackend {
	
	/**
	 * Invoked after no exception has been thrown during a non-validating
	 * calling sequence on the backend.
	 */
	public void commit() throws Exception;

	/**
	 * Invoked when an exception has been thrown during a non-validating
	 * calling sequence on the backend.
	 */
	public void rollback() throws Exception;

	/**
	 * Invoked when a vertex has been added to the sender model. If
	 * validate is true then the change should not be performed yet.
	 * If the validation or the actual change fail on the backend,
	 * you should throw an exception.
	 */
	public void vertexAdded(JGraphAdapterModel sender, Object vertex,
			boolean validate) throws Exception;

	public void edgeAdded(JGraphAdapterModel sender, Object edge,
			Object source, Object target, boolean validate) throws Exception;

	public void cellRemoved(JGraphAdapterModel sender, Object cell,
			boolean validate) throws Exception;

	public void parentChanged(JGraphAdapterModel sender, Object child,
			Object parent, boolean validate) throws Exception;

	public void sourceChanged(JGraphAdapterModel sender, Object edge,
			Object source, boolean validate) throws Exception;

	public void targetChanged(JGraphAdapterModel sender, Object edge,
			Object target, boolean validate) throws Exception;

	public void attributesChanged(JGraphAdapterModel sender, Object cell,
			Map attributes, boolean validate) throws Exception;

}