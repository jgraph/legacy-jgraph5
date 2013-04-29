/*
 * $Id: JGraphHeadlessLayoutExample.java,v 1.1 2009-09-25 15:17:49 david Exp $
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

import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

import com.jgraph.example.JGraphGraphFactory;
import com.jgraph.layout.tree.JGraphTreeLayout;

public class JGraphHeadlessLayoutExample {

	public static void main(String[] args) {

		// Construct Model and GraphLayoutCache
		GraphModel model = new DefaultGraphModel();
		// When not using a JGraph instance, a GraphLayoutCache does not
		// automatically listen to model changes. Therefore, use a special
		// layout cache with a built-in listener
		GraphLayoutCache cache = new DataGraphLayoutCache(model,
				new DefaultCellViewFactory());

		// Insert all three cells in one call, so we need an array to store them
		DefaultGraphCell[] cells = new DefaultGraphCell[5];

		// Create parent Vertex
		cells[0] = createVertex("Parent", 20, 20, 40, 20);

		// Create child 1 Vertex
		cells[1] = createVertex("Child1", 20, 20, 40, 20);

		// Create child 1 Vertex
		cells[2] = createVertex("Child2", 20, 20, 40, 20);
		// Note the cells are all initially positioned at (20,20)
		
		// Create Edges
		DefaultEdge edge1 = new DefaultEdge();
		DefaultEdge edge2 = new DefaultEdge();
		// Fetch the ports from the new vertices, and connect them with the edge
		edge1.setSource(cells[0].getChildAt(0));
		edge1.setTarget(cells[1].getChildAt(0));
		edge2.setSource(cells[0].getChildAt(0));
		edge2.setTarget(cells[2].getChildAt(0));
		cells[3] = edge1;
		cells[4] = edge2;

		// Insert the cells via the cache
		JGraphGraphFactory.insert(model, cells);

		// Create the layout facade. When creating a facade for the tree
		// layouts, pass in any cells that are intended to be the tree roots
		// in the layout
		JGraphFacade facade = new JGraphModelFacade(model, new Object[]{cells[0]});
		// Create the layout to be applied
		JGraphLayout layout = new JGraphTreeLayout();
		// Run the layout, the facade holds the results
		layout.run(facade);
		// Obtain the output of the layout from the facade. The second
		// parameter defines whether or not to flush the output to the
		// origin of the graph
		Map nested = facade.createNestedMap(true, true);
		// Apply the result to the graph
		cache.edit(nested);
		
		// Display the new positions of the cell
		Rectangle2D cell0Bounds = GraphConstants.getBounds(model.getAttributes(cells[0]));
		Rectangle2D cell1Bounds = GraphConstants.getBounds(model.getAttributes(cells[1]));
		Rectangle2D cell2Bounds = GraphConstants.getBounds(model.getAttributes(cells[2]));
		System.out.println("Parent cell is at " + cell0Bounds.getX() + "," + cell0Bounds.getY());
		System.out.println("Child cell 1 is at " + cell1Bounds.getX() + "," + cell1Bounds.getY());
		System.out.println("Child cell 2 is at " + cell2Bounds.getX() + "," + cell2Bounds.getY());
	}

	public static DefaultGraphCell createVertex(String name, double x,
			double y, double w, double h) {

		// Create vertex with the given name
		DefaultGraphCell cell = new DefaultGraphCell(name);

		// Set bounds
		GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(
				x, y, w, h));

		// Add a Port
		cell.addPort();

		return cell;
	}
}