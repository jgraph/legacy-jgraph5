/*
 * $Id: JGraphSQLBusinessModel.java,v 1.2 2005-08-09 08:40:47 david Exp $
 * 
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.adapter;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.jgraph.graph.AttributeMap;

/**
 * Allows to set the properties of user objects via the edit method's nested
 * argument. You must add (userObject, Map) pairs to the nested map for each
 * user object's new attributes.
 */
public class JGraphSQLBusinessModel extends JGraphAdapterModel {

	public JGraphSQLBusinessModel() {
		super();
	}

	/**
	 * @param roots
	 * @param attributes
	 */
	public JGraphSQLBusinessModel(List roots, AttributeMap attributes) {
		this(roots, attributes, null);
	}

	/**
	 * @param roots
	 * @param attributes
	 * @param backend
	 */
	public JGraphSQLBusinessModel(List roots, AttributeMap attributes,
			JGraphAdapterBackend backend) {
		super(roots, attributes, backend);
	}

	public void addProperty(Object cell, Object key, Object value) {
		if (key != null && value != null) {
			try {
				Object userObj = getValue(cell);
				if (userObj instanceof JGraphSQLEntity
						&& getBackend() instanceof JGraphSQLBackend) {
					((JGraphSQLBackend) getBackend()).propertyAdded(
							(JGraphSQLEntity) userObj, key, value);
					fireCommit();
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					fireRollback();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		super.addProperty(cell, key, value);
	}

	public Collection findVertices(String query, Object parent)
			throws SQLException {
		return ((JGraphSQLBackend) getBackend()).findVertices(this, query,
				parent);
	}

	public Collection findEdges(String query, Object parent, Object source,
			Object target, boolean directed) throws SQLException {
		return ((JGraphSQLBackend) getBackend()).findEdges(this, query, parent,
				source, target, directed);
	}
}