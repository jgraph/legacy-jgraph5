/*
 * $Id: JGraphSQLEntity.java,v 1.1.1.1 2005-08-06 05:26:45 gaudenz Exp $
 * 
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.adapter;

/**
 * An object that represents an entity in a tree.
 */
public class JGraphSQLEntity extends JGraphBusinessObject {

	protected Object id;

	protected JGraphSQLEntity parent;

	public JGraphSQLEntity() {
		this(null);
	}

	public JGraphSQLEntity(Object userObject) {
		this(userObject, null, null);
	}

	public JGraphSQLEntity(Object id, JGraphSQLEntity parent) {
		this(null, id, parent);
	}

	public JGraphSQLEntity(Object userObject, Object id, JGraphSQLEntity parent) {
		super(userObject);
		setID(id);
		setParent(parent);
	}

	/**
	 * @return Returns the id.
	 */
	public Object getID() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setID(Object id) {
		this.id = id;
	}

	/**
	 * @return Returns the parent.
	 */
	public JGraphSQLEntity getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            The parent to set.
	 */
	public void setParent(JGraphSQLEntity parent) {
		this.parent = parent;
	}

	public Object clone() {
		JGraphSQLEntity entity = (JGraphSQLEntity) super.clone();
		entity.setID(null);
		return entity;
	}

	public int hashCode() {
		return (id != null) ? id.hashCode() : super.hashCode();
	}

	public boolean equals(Object other) {
		if (id != null && other instanceof JGraphSQLEntity)
			return ((JGraphSQLEntity) other).getID().equals(getID());
		return false;
	}

}
