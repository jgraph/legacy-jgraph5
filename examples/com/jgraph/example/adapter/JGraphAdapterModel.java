/*
 * $Id: JGraphAdapterModel.java,v 1.1.1.1 2005-08-06 05:26:45 gaudenz Exp $
 * 
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.adapter;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.undo.UndoableEdit;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.ParentMap;

/**
 * Maps from business objects to cells and manages prototypes to create new cells
 * on the fly.
 * Maps from userobjects to cells and manages cell prototypes.
 */
public class JGraphAdapterModel extends DefaultGraphModel {

	public static final String VERSION = "@NAME@ (v@VERSION@)";
	
	/**
	 * A store of the mapping between model cells and back-end objects
	 */
	protected Map mapping = new Hashtable();

	/**
	 * The current back-end logic object
	 */
	protected JGraphAdapterBackend backend;

	public JGraphAdapterModel() {
		super();
	}

	/**
	 * @param roots
	 * @param attributes
	 */
	public JGraphAdapterModel(List roots, AttributeMap attributes) {
		this(roots, attributes, null);
	}

	/**
	 * @param roots
	 * @param attributes
	 */
	public JGraphAdapterModel(List roots, AttributeMap attributes,
			JGraphAdapterBackend backend) {
		super(roots, attributes);
		this.backend = backend;
	}

	public void addProperty(Object cell, Object key, Object value) {
		if (key != null && value != null) {
			Object userObj = getValue(cell);
			JGraphBusinessObject bo = (JGraphBusinessObject) userObj;
			bo.putProperty(key, value);
			cellsChanged(new Object[] { cell });
		}
	}

	protected Map handleAttributes(Map attributes) {
		Map undo = super.handleAttributes(attributes);
		if (attributes != null) {
			if (undo == null)
				undo = new Hashtable();
			Iterator it = attributes.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				Object cell = entry.getKey();
				Map properties = (Map) entry.getValue();
				if (cell instanceof JGraphBusinessObject) {
					JGraphBusinessObject bo = (JGraphBusinessObject) cell;
					Map deltaOld = new Hashtable();
					Iterator it2 = properties.entrySet().iterator();
					while (it2.hasNext()) {
						Map.Entry property = (Map.Entry) it2.next();
						Object key = property.getKey();
						Object oldValue = bo.putProperty(key, property
								.getValue());
						if (oldValue != null)
							deltaOld.put(key, oldValue);
					}
					undo.put(cell, deltaOld);
				}
			}
		}
		return undo;
	}

	/**
	 * @return Returns the backend.
	 */
	public JGraphAdapterBackend getBackend() {
		return backend;
	}

	/**
	 * @param backend
	 *            The backend to set.
	 */
	public void setBackend(JGraphAdapterBackend backend) {
		this.backend = backend;
	}

	/**
	 * 
	 * @param obj
	 * @return the object that the specified object maps to
	 */
	public Object getMapping(Object obj) {
		return mapping.get(obj);
	}

	/**
	 * 
	 * @param obj
	 * @param cell
	 */
	protected void putMapping(Object obj, Object cell) {
		mapping.put(obj, cell);
	}

	/**
	 * 
	 * @param obj
	 */
	protected void removeMapping(Object obj) {
		mapping.remove(obj);
	}

	/**
	 * 
	 * @param child
	 * @return the user object associated with the parent of the specified child
	 */
	public Object getParentUserObject(Object child) {
		return getValue(getParent(child));
	}

	/**
	 * 
	 * @param edge
	 * @return the user object associated with the vertex connected to the source end of this edge
	 */
	public Object getSourceVertexUserObject(Object edge) {
		Object cell = DefaultGraphModel.getSourceVertex(this, edge);
		return getValue(cell);
	}

	/**
	 * 
	 * @param edge
	 * @return the user object associated with the vertex connected to the target end of this edge
	 */
	public Object getTargetVertexUserObject(Object edge) {
		Object cell = DefaultGraphModel.getTargetVertex(this, edge);
		return getValue(cell);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#edit(java.util.Map,
	 *      org.jgraph.graph.ConnectionSet, org.jgraph.graph.ParentMap,
	 *      javax.swing.undo.UndoableEdit[])
	 */
	public void edit(Map attributes, ConnectionSet cs, ParentMap pm,
			UndoableEdit[] edits) {
		if ((attributes != null && !attributes.isEmpty())
				|| (cs != null && !cs.isEmpty())) {
			try {
				// Call source / targetChanged
				processConnectionSet(cs, true);
				processParentMap(pm, true);
				processNestedAttributes(attributes, true);
				super.edit(attributes, cs, pm, edits);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			super.edit(attributes, cs, pm, edits);
		}
	}

	protected Object cloneUserObject(Object userObject) {
		if (userObject instanceof JGraphBusinessObject)
			return ((JGraphBusinessObject) userObject).clone();
		return super.cloneUserObject(userObject);
	}

	public Object valueForCellChanged(Object cell, Object newValue) {
		Object userObject = getValue(cell);
		if (userObject instanceof JGraphBusinessObject
				&& newValue instanceof String) {
			JGraphBusinessObject user = (JGraphBusinessObject) userObject;
			Object oldLabel = user.getValue();
			user.setValue(newValue);
			return oldLabel;
		} else
			return super.valueForCellChanged(cell, newValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#insert(java.lang.Object[],
	 *      java.util.Map, org.jgraph.graph.ConnectionSet,
	 *      org.jgraph.graph.ParentMap, javax.swing.undo.UndoableEdit[])
	 */
	public void insert(Object[] cells, Map attributes, ConnectionSet cs,
			ParentMap pm, UndoableEdit[] edits) {
		if (cells != null || attributes != null || cs != null) {
			try {
				processInsert(cells, attributes, cs, pm, true);
				super.insert(cells, attributes, cs, pm, edits);
			} catch (Exception e) {
				e.printStackTrace();

			}
		} else {
			super.insert(cells, attributes, cs, pm, edits);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#remove(java.lang.Object[])
	 */
	public void remove(Object[] cells) {
		if (cells != null) {
			try {
				processRemove(cells, true);
				super.remove(cells);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			super.remove(cells);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.GraphModel#insert(java.lang.Object[],
	 *      java.util.Map, org.jgraph.graph.ConnectionSet,
	 *      org.jgraph.graph.ParentMap, javax.swing.undo.UndoableEdit[])
	 */
	public void processInsert(Object[] cells, Map attributes, ConnectionSet cs,
			ParentMap pm, boolean validate) throws Exception {
		if (cells != null || attributes != null || cs != null) {
			if (cells != null) {
				// Vertices in the first run (so they have an ID)
				for (int i = 0; i < cells.length; i++) {
					if (!isPort(cells[i])) {
						if (!isEdge(cells[i])) {
							fireVertexAdded(cells[i], validate);
							if (!validate)
								putMapping(getValue(cells[i]), cells[i]);
						}
					}
				}
				// Edges in the second run
				for (int i = 0; i < cells.length; i++) {
					if (!isPort(cells[i])) {
						if (isEdge(cells[i]) && cs != null) {
							fireEdgeAdded(cells[i],
									cs.getPort(cells[i], false), cs.getPort(
											cells[i], true), validate);
							if (!validate)
								putMapping(getValue(cells[i]), cells[i]);
						}
					}
				}
			}
			processConnectionSet(cs, validate);
			processParentMap(pm, validate);
			processNestedAttributes(attributes, validate);
		}
	}

	protected void processRemove(Object[] cells, boolean validate)
			throws Exception {
		if (cells != null) {
			for (int i = 0; i < cells.length; i++) {
				if (!isPort(cells[i])) {
					fireCellRemoved(cells[i], validate);
					if (!validate)
						removeMapping(getValue(cells[i]));
				}
			}
		}
	}

	/**
	 * 
	 * @param cs
	 * @param validate
	 * @throws Exception
	 */
	protected void processConnectionSet(ConnectionSet cs, boolean validate)
			throws Exception {
		if (cs != null) {
			Iterator it = cs.connections();
			while (it.hasNext()) {
				ConnectionSet.Connection conn = (ConnectionSet.Connection) it
						.next();
				Object edge = conn.getEdge();
				if (contains(edge)) {
					if (conn.isSource()) {
						fireSourceChanged(edge, conn.getPort(), validate);
					} else {
						fireTargetChanged(edge, conn.getPort(), validate);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param pm
	 * @param validate
	 * @throws Exception
	 */
	protected void processParentMap(ParentMap pm, boolean validate)
			throws Exception {
		if (pm != null) {
			Iterator it = pm.entries();
			while (it.hasNext()) {
				ParentMap.Entry entry = (ParentMap.Entry) it.next();
				Object parent = entry.getParent();
				Object child = entry.getChild();
				if (contains(child)) {
					fireParentChanged(child, parent, validate);
				}
			}
		}
	}

	/**
	 * 
	 * @param nested
	 * @param validate
	 * @throws Exception
	 */
	protected void processNestedAttributes(Map nested, boolean validate)
			throws Exception {
		if (nested != null) {
			Iterator it = nested.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				Object cell = entry.getKey();
				if (!isPort(cell)) { // TODO: Remove isPort check
					Map change = (Map) entry.getValue();
					fireAttributesChanged(cell, change, validate);
				}
			}
		}
	}

	// Override parent method to hook the backend into the execute method.
	protected GraphModelEdit createEdit(Object[] inserted, Object[] removed,
			Map attributes, ConnectionSet cs, ParentMap pm) {
		return new BusinessModelEdit(inserted, removed, attributes, cs, pm);
	}

	public class BusinessModelEdit extends GraphModelEdit {

		protected boolean inProgress = false;

		/**
		 * @param inserted
		 * @param removed
		 * @param attributes
		 * @param connectionSet
		 * @param parentMap
		 */
		public BusinessModelEdit(Object[] inserted, Object[] removed,
				Map attributes, ConnectionSet connectionSet, ParentMap parentMap) {
			super(inserted, removed, attributes, connectionSet, parentMap);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jgraph.event.GraphModelEvent.ExecutableGraphChange#execute()
		 */
		public void execute() {
			super.execute();
			try {
				if (!inProgress) {
					inProgress = true;
					processRemove(insert, false);
					processInsert(remove, previousAttributes,
							previousConnectionSet, previousParentMap, false);
					fireCommit();
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					fireRollback();
					undo();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} finally {
				inProgress = false;
			}
		}

	}

	/**
	 * 
	 * @throws Exception
	 */
	public void fireCommit() throws Exception {
		if (backend != null)
			backend.commit();
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void fireRollback() throws Exception {
		if (backend != null)
			backend.rollback();
	}

	/**
	 * 
	 * @param vertex
	 * @param validate
	 * @throws Exception
	 */
	public void fireVertexAdded(Object vertex, boolean validate)
			throws Exception {
		if (backend != null)
			backend.vertexAdded(this, vertex, validate);
	}

	/**
	 * 
	 * @param edge
	 * @param source
	 * @param target
	 * @param validate
	 * @throws Exception
	 */
	public void fireEdgeAdded(Object edge, Object source, Object target,
			boolean validate) throws Exception {
		if (backend != null)
			backend.edgeAdded(this, edge, source, target, validate);
	}

	/**
	 * 
	 * @param object
	 * @param validate
	 * @throws Exception
	 */
	public void fireCellRemoved(Object object, boolean validate)
			throws Exception {
		if (backend != null)
			backend.cellRemoved(this, object, validate);
	}

	/**
	 * 
	 * @param child
	 * @param parent
	 * @param validate
	 * @throws Exception
	 */
	public void fireParentChanged(Object child, Object parent, boolean validate)
			throws Exception {
		if (backend != null)
			backend.parentChanged(this, child, parent, validate);
	}

	/**
	 * 
	 * @param edge
	 * @param source
	 * @param validate
	 * @throws Exception
	 */
	public void fireSourceChanged(Object edge, Object source, boolean validate)
			throws Exception {
		if (backend != null)
			backend.sourceChanged(this, edge, source, validate);
	}

	/**
	 * 
	 * @param edge
	 * @param target
	 * @param validate
	 * @throws Exception
	 */
	public void fireTargetChanged(Object edge, Object target, boolean validate)
			throws Exception {
		if (backend != null)
			backend.targetChanged(this, edge, target, validate);
	}

	/**
	 * 
	 * @param cell
	 * @param change
	 * @param validate
	 * @throws Exception
	 */
	public void fireAttributesChanged(Object cell, Map change, boolean validate)
			throws Exception {
		if (backend != null)
			backend.attributesChanged(this, cell, change, validate);
	}

}