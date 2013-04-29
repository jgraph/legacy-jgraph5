/*
 * Copyright (c) 2005-2006, David Benson
 *
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.fastgraph;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.Port;

import com.jgraph.example.JGraphGraphFactory;

public class FastGraphFactory extends JGraphGraphFactory {

	/**
	 * Default Constructor
	 */
	public FastGraphFactory() {
		dialog = new FastFactoryConfigDialog();
	}

	/**
	 * Method hook to create custom vertices
	 * 
	 * @param userObject
	 *            the user object to pass to the cell
	 * @return the new vertex instance
	 */
	protected DefaultGraphCell createVertex(Object userObject, Point2D position, Map defaultVertexAttributes) {
		AttributeMap attributes = new AttributeMap(defaultVertexAttributes);
		GraphConstants.setBounds(attributes, new Rectangle2D.Double(
				position.getX()*2, position.getY()*2, 60, 60 ));
		// Restrict to a square shape
		GraphConstants.setConstrained(attributes, true);
		Point2D point = new Point2D.Double(GraphConstants.PERMILLE / 2,
				GraphConstants.PERMILLE / 2);
		DefaultGraphCell cell = new FastCircleCell(userObject, attributes);
		// Add one central fixed port
		cell.addPort(point);

		return cell;
	}

	/**
	 * Method hook to create custom edges
	 * 
	 * @return the new vertex instance
	 */
	protected Edge createEdge(Map defaultEdgeAttributes,
	Port sourcePort, Port targetPort) {
		AttributeMap edgeAttrib = null;
		if (defaultEdgeAttributes != null) {
			edgeAttrib = new AttributeMap(defaultEdgeAttributes);
		} else {
			edgeAttrib = new AttributeMap(4);
		}
		Edge edge = new FastEdge(null, edgeAttrib);
		
		edge.setSource(sourcePort);
		edge.setTarget(targetPort);

		return edge;

	}
	
	public class FastFactoryConfigDialog extends FactoryConfigDialog {
		public FastFactoryConfigDialog() {
			super();
		}
		
		protected void applyValues() {
			super.applyValues();
			System.out.println("Insert pressed on factory dialog, timestamp="
					+ System.currentTimeMillis() + "ms");
		}

	}
}