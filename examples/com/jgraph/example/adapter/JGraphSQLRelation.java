/*
 * $Id: JGraphSQLRelation.java,v 1.1.1.1 2005-08-06 05:26:45 gaudenz Exp $
 * 
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.adapter;

/**
 * A business object that represents a relation between two entities.
 */
public class JGraphSQLRelation extends JGraphSQLEntity {

	protected JGraphSQLEntity source, target;

	public JGraphSQLRelation() {
		this(null);
	}

	public JGraphSQLRelation(Object userObject) {
		this(userObject, null, null, null, null);
	}

	public JGraphSQLRelation(Object id, JGraphSQLEntity parent,
			JGraphSQLEntity source, JGraphSQLEntity target) {
		this(null, id, parent, source, target);
	}

	public JGraphSQLRelation(Object userObject, Object id,
			JGraphSQLEntity parent, JGraphSQLEntity source,
			JGraphSQLEntity target) {
		super(userObject, id, parent);
		setSource(source);
		setTarget(target);
	}

	/**
	 * @return Returns the source.
	 */
	public JGraphSQLEntity getSource() {
		return source;
	}

	/**
	 * @param source
	 *            The source to set.
	 */
	public void setSource(JGraphSQLEntity source) {
		this.source = source;
	}

	/**
	 * @return Returns the target.
	 */
	public JGraphSQLEntity getTarget() {
		return target;
	}

	/**
	 * @param target
	 *            The target to set.
	 */
	public void setTarget(JGraphSQLEntity target) {
		this.target = target;
	}

}