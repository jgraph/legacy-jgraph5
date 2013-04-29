/*
 * $Id: JGraphSQLBackend.java,v 1.1.1.1 2005-08-06 05:26:45 gaudenz Exp $
 * 
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.adapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;

/**
 * Maps changes to the graph model to a custom backend, eg. an SQL database.
 * (This should use prepared statements.)
 */
public class JGraphSQLBackend implements JGraphAdapterBackend {

	protected static final String NO_ID = "0";

	protected int lastId = 0;

	protected boolean trxInProgress = false;

	protected Object vertexPrototype, edgePrototype;

	protected Connection conn;

	public JGraphSQLBackend(Connection conn, Object vertexPrototype,
			Object edgePrototype) throws ClassNotFoundException, SQLException {
		this.conn = conn;
		this.vertexPrototype = vertexPrototype;
		this.edgePrototype = edgePrototype;
		try {
			update("SET AUTOCOMMIT FALSE");
			update("CREATE TABLE entity ( id INTEGER, parent_id INTEGER)");
			update("CREATE TABLE relation ( entity_id INTEGER, source_id INTEGER, target_id INTEGER)");
			update("CREATE TABLE property ( entity_id INTEGER, key VARCHAR(256), value VARCHAR(256))");
			update("CREATE INDEX prp_entity_idx ON property (entity_id)");
			update("CREATE INDEX rel_entity_idx ON relation (entity_id)");
			update("CREATE INDEX rel_source_idx ON relation (source_id)");
			update("CREATE INDEX rel_target_idx ON relation (target_id)");
			// Triggers to update the model if the db changes...
			// update("CREATE TRIGGER test_trigger AFTER UPDATE ON property CALL
			// org.jgraph.studio.business.JGraphStudioSQLBackend.TestTrigger");
			update("COMMIT");
		} catch (SQLException ex2) {
			// this will have no effect on the db
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void commit() throws Exception {
		if (trxInProgress) {
			update("COMMIT");
			trxInProgress = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void rollback() throws Exception {
		if (trxInProgress) {
			update("ROLLBACK");
			trxInProgress = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#vertexAdded(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object)
	 */
	public void vertexAdded(JGraphAdapterModel sender, Object vertex,
			boolean validate) throws Exception {
		if (!validate)
			objectAdded(sender.getValue(vertex));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void edgeAdded(JGraphAdapterModel sender, Object object,
			Object source, Object target, boolean validate) throws Exception {
		if (!validate) {
			Object id = objectAdded(sender.getValue(object));
			if (id != null)
				update("INSERT INTO relation(entity_id, source_id, target_id) VALUES ("
						+ id + ", 0, 0)");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	protected Object objectAdded(Object object) throws SQLException {
		if (object instanceof JGraphSQLEntity) {
			JGraphSQLEntity entity = (JGraphSQLEntity) object;
			if (entity.getID() == null) {
				entity.setID(getNextEntityId());
				update("INSERT INTO entity(id) VALUES (" + entity.getID() + ")");
				Iterator it = entity.getProperties().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					update("INSERT INTO property(entity_id, key, value) VALUES ("
							+ entity.getID()
							+ ", '"
							+ entry.getKey()
							+ "', '"
							+ entry.getValue() + "')");
				}
			}
			return entity.getID();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void cellRemoved(JGraphAdapterModel sender, Object cell,
			boolean validate) throws Exception {
		Object userObj = sender.getValue(cell);
		if (!validate && userObj instanceof JGraphSQLEntity) {
			JGraphSQLEntity entity = (JGraphSQLEntity) userObj;
			Object entityId = entity.getID();
			update("DELETE FROM entity WHERE id = " + entityId);
			update("DELETE FROM relation WHERE entity_id = " + entityId);
			update("DELETE FROM property WHERE entity_id = " + entityId);
			update("UPDATE entity SET parent_id = " + NO_ID
					+ " WHERE parent_id = " + entityId);
			entity.setID(null);
		}
	}

	public void parentChanged(JGraphAdapterModel sender, Object child,
			Object parent, boolean validate) throws Exception {
		Object childObj = sender.getValue(child);
		Object parentObj = sender.getValue(parent);
		if (!validate && childObj instanceof JGraphSQLEntity
				&& parentObj instanceof JGraphSQLEntity) {
			JGraphSQLEntity childEntity = (JGraphSQLEntity) childObj;
			JGraphSQLEntity parentEntity = (JGraphSQLEntity) parentObj;
			update("UPDATE entity SET parent_id = " + parentEntity.getID()
					+ " where id = " + childEntity.getID());
			childEntity.setParent(parentEntity);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void sourceChanged(JGraphAdapterModel sender, Object edge,
			Object source, boolean validate) throws Exception {
		Object object = sender.getValue(edge);
		source = sender.getParentUserObject(source);
		if (!validate && object instanceof JGraphSQLRelation) {
			JGraphSQLRelation relation = (JGraphSQLRelation) object;
			Object id = NO_ID;
			if (source instanceof JGraphSQLEntity) {
				relation.setSource((JGraphSQLEntity) source);
				id = relation.getSource().getID();
			} else
				relation.setSource(null);
			update("UPDATE relation SET source_id = " + id
					+ " where entity_id = " + relation.getID());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void targetChanged(JGraphAdapterModel sender, Object edge,
			Object target, boolean validate) throws Exception {
		Object object = sender.getValue(edge);
		target = sender.getParentUserObject(target);
		if (!validate && object instanceof JGraphSQLRelation) {
			JGraphSQLRelation relation = (JGraphSQLRelation) object;
			Object id = NO_ID;
			if (target instanceof JGraphSQLEntity) {
				relation.setTarget((JGraphSQLEntity) target);
				id = relation.getTarget().getID();
			} else
				relation.setTarget(null);
			update("UPDATE relation SET target_id = " + id
					+ " where entity_id = " + relation.getID());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void attributesChanged(JGraphAdapterModel sender, Object cell,
			Map attributes, boolean validate) throws Exception {
		if (!validate) {
			if (cell instanceof JGraphSQLEntity) {
				propertiesChanged(cell, attributes);
			} else {
				Object value = GraphConstants.getValue(attributes);
				if (value != null) {
					Map properties = new Hashtable();
					properties.put("value", value);
					propertiesChanged(sender.getValue(cell),
							properties);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void propertiesChanged(Object object, Map properties)
			throws Exception {
		if (object instanceof JGraphSQLEntity) {
			JGraphSQLEntity entity = (JGraphSQLEntity) object;
			Iterator it = properties.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				// TODO: Handle new attributes here?
				propertyChanged(entity, entry.getKey(), entry.getValue());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void propertyChanged(JGraphSQLEntity entity, Object key, Object value)
			throws Exception {
		update("UPDATE property SET value = '" + value + "' where key='" + key
				+ "' AND entity_id = " + entity.getID());
	}

	/*
	 * This method is called from the Studio UI. (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void propertyAdded(JGraphSQLEntity entity, Object key, Object value)
			throws Exception {
		update("INSERT INTO property(entity_id, key, value) VALUES ("
				+ entity.getID() + ", '" + key + "', '" + value + "')");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public Collection findVertices(JGraphAdapterModel sender, Object query,
			Object parent) throws SQLException {
		Object object = sender.getValue(parent);
		String parentClause = (object instanceof JGraphSQLEntity) ? " and parent_id = "
				+ ((JGraphSQLEntity) object).getID()
				: "";
		String whereClause = "where "
				+ createFilter(query)
				+ parentClause
				+ " and not exists (select 1 from relation where relation.entity_id = property.entity_id)";
		return findCells(sender, whereClause);
	}

	protected Collection findCells(JGraphAdapterModel context,
			String whereClause) throws SQLException {
		Collection objs = find(whereClause);
		List result = new ArrayList(objs.size());
		Iterator it = objs.iterator();
		while (it.hasNext()) {
			JGraphSQLEntity entity = (JGraphSQLEntity) it.next();
			result.add(createCell(context, entity));
		}
		return result;
	}

	public Object getParent(Object object) {
		if (object instanceof JGraphSQLEntity)
			return ((JGraphSQLEntity) object).getParent();
		return null;
	}

	public Object getSource(Object object) {
		if (object instanceof JGraphSQLRelation)
			return ((JGraphSQLRelation) object).getSource();
		return null;
	}

	public Object getTarget(Object object) {
		if (object instanceof JGraphSQLRelation)
			return ((JGraphSQLRelation) object).getTarget();
		return null;
	}

	// All relations with cells (parent, source, target) must be
	// set when inserting the cells. the backend only sets the relations
	// on the business-object level.
	public Object createCell(JGraphAdapterModel context, JGraphSQLEntity entity)
			throws SQLException {
		Object prototype = (entity instanceof JGraphSQLRelation) ? edgePrototype
				: vertexPrototype;

		// TODO: Do not create if mapped?
		Object vertex = DefaultGraphModel.cloneCell(context, prototype);
		if (vertex instanceof DefaultGraphCell) {
			DefaultGraphCell cell = (DefaultGraphCell) vertex;
			cell.setUserObject(entity);
		}
		return vertex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public Collection findEdges(JGraphAdapterModel sender, Object query,
			Object parent, Object source, Object target, boolean directed)
			throws SQLException {
		Object object = sender.getValue(parent);
		source = sender.getValue(source);
		target = sender.getValue(target);
		String parentClause = (object instanceof JGraphSQLEntity) ? " and parent_id = "
				+ ((JGraphSQLEntity) object).getID()
				: "";
		String edgeWhereClause = "1=1 ";
		if (source instanceof JGraphSQLEntity) {
			JGraphSQLEntity entity = (JGraphSQLEntity) source;
			edgeWhereClause += "and source_id = " + entity.getID() + " ";
		}
		if (target instanceof JGraphSQLEntity) {
			JGraphSQLEntity entity = (JGraphSQLEntity) target;
			edgeWhereClause += "and target_id = " + entity.getID() + " ";
		} else if (!directed && source != null) { // TODO: Shortcut for now...
			JGraphSQLEntity entity = (JGraphSQLEntity) source;
			edgeWhereClause += "or target_id = " + entity.getID() + " ";
		}
		String whereClause = "where "
				+ createFilter(query)
				+ parentClause
				+ " and exists (select 1 from relation where relation.entity_id = property.entity_id and ("
				+ edgeWhereClause + "))";
		return findCells(sender, whereClause);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	protected String createFilter(Object query) {
		if (query != null && query.toString().length() > 0) {
			String[] tokens = query.toString().split(" ");
			if (tokens.length > 0) {
				String filter = "soundex(value) IN (soundex('" + tokens[0]
						+ "')";
				for (int i = 1; i < tokens.length; i++)
					filter += ", soundex('" + tokens[i] + "')";
				return filter + ")";
			}
		}
		return "1=1";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	protected Collection find(String whereClause) throws SQLException {
		Statement st = (conn != null) ? conn.createStatement() : null;
		String sql = "select entity_id, count(entity_id) as ranking from property "
				+ whereClause + " group by entity_id order by ranking desc";
		println("SQL: " + sql);
		ResultSet rs = (st != null) ? st.executeQuery(sql) : null;
		List objects = new LinkedList();
		while (rs != null && rs.next()) {
			Object object = get(rs.getObject(1));
			if (object != null)
				objects.add(object);
		}
		if (st != null)
			st.close();
		return objects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	protected synchronized Object getNextEntityId() {
		int id = lastId;
		try {
			Statement st = (conn != null) ? conn.createStatement() : null;
			ResultSet rs = (st != null) ? st
					.executeQuery("select max(id) from entity") : null;
			if (st != null)
				st.close();
			if (rs != null && rs.next())
				id = rs.getInt(1) + 1;
			else
				// use local var for ids
				lastId++;
		} catch (SQLException e) {
			// ignore and return lastId
		}
		return new Integer(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public JGraphSQLEntity get(Object id) throws SQLException {
		Statement st = (conn != null) ? conn.createStatement() : null;
		JGraphSQLEntity entity = null;
		ResultSet rs = st
				.executeQuery("select id, parent_id from entity where id = "
						+ id);

		Object entityId = null;
		JGraphSQLEntity parent = null;
		if (rs.next()) {
			entityId = rs.getObject(1);
			parent = get(rs.getObject(2));
		}

		// Assumes that the entity exists
		rs = st
				.executeQuery("select source_id, target_id from relation where entity_id = "
						+ id);

		// Fetch the source and target, or create a vertex
		if (rs.next()) {
			JGraphSQLEntity source = get(rs.getObject(1));
			JGraphSQLEntity target = get(rs.getObject(2));
			entity = new JGraphSQLRelation(entityId, parent, source, target); // TODO.
			// Add
			// Parent
		} else {
			entity = new JGraphSQLEntity(entityId, parent);
		}

		// Fill-in the properties
		rs = st
				.executeQuery("select key, value from property where entity_id = "
						+ id);
		Hashtable properties = new Hashtable();
		while (rs.next()) {
			String key = rs.getString(1);
			String value = rs.getString(2);
			if (key != null) {
				properties.put(key, value);
			}
		}
		st.close();
		entity.setProperties(properties);

		return entity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public synchronized void query(String expression) throws SQLException {
		Statement st = (conn != null) ? conn.createStatement() : null;
		ResultSet rs = (st != null) ? st.executeQuery(expression) : null;
		dump(rs);
		st.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void dump(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int colmax = meta.getColumnCount();
		while (rs.next()) {
			for (int i = 0; i < colmax; ++i) {
				Object o = rs.getObject(i + 1);
				if (o != null)
					System.out.print(o.toString() + " ");
			}
			println(" ");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.editor.business.JGraphBusinessBackend#valueForCellChanged(org.jgraph.editor.business.JGraphBusinessModel,
	 *      java.lang.Object, java.lang.Object)
	 */
	public synchronized void update(String expression) throws SQLException {
		trxInProgress = true;
		Statement st = (conn != null) ? conn.createStatement() : null;
		int i = (st != null) ? st.executeUpdate(expression) : 0;
		if (i == -1) {
			println("db error : " + expression);
		} else {
			println("SQL: " + expression);
		}
		if (st != null)
			st.close();
	}

	protected static void println(String msg) {
		JGraphAdapterExample.println(msg);
	}

}