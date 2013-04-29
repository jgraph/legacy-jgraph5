/*
 * $Id: JGraphModelLayoutExample.java,v 1.1 2009-09-25 15:17:49 david Exp $
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

import java.awt.ScrollPane;
import java.util.Map;

import javax.swing.JFrame;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphModel;

import com.jgraph.example.JGraphGraphFactory;
import com.jgraph.example.fastgraph.FastGraphModel;
import com.jgraph.layout.tree.JGraphTreeLayout;

public class JGraphModelLayoutExample {

	public static GraphModel persistModel;
	
	public static void main(String[] args) {
		// Switch off D3D because of Sun XOR painting bug
		// See http://www.jgraph.com/forum/viewtopic.php?t=4066
		System.setProperty("sun.java2d.d3d", "false");
		// Construct Model and GraphLayoutCache
		GraphModel model = new FastGraphModel();
		persistModel = model;
		
		// Create a new tree and insert it directly into the model
		JGraphGraphFactory graphFactory = new JGraphGraphFactory();
		graphFactory.setNumNodes(7500);
		graphFactory.setNumEdges(7499);
		long startTime = System.currentTimeMillis();
		Object treeRoot = graphFactory.insertTreeSampleData(model,
				new AttributeMap(), new AttributeMap());
		System.out.println("After insert cells, elapsed msec = " + (System.currentTimeMillis()-startTime));

		// Create the layout facade. When creating a facade for the tree
		// layouts, pass in any cells that are intended to be the tree roots
		// in the layout
		JGraphFacade facade = new JGraphModelFacade(model, new Object[]{treeRoot}, true, false, false, true);
		// Create the layout to be applied
		JGraphLayout layout = new JGraphTreeLayout();
		// Run the layout, the facade holds the results
		layout.run(facade);
		System.out.println("After layout, elapsed msec = " + (System.currentTimeMillis()-startTime));
		// Obtain the output of the layout from the facade. The second
		// parameter defines whether or not to flush the output to the
		// origin of the graph
		Map nested = facade.createNestedMap(true, true);
		// Apply the result to the graph
		model.edit(nested, null, null, null);
		System.out.println("After layout applied, elapsed msec = " + (System.currentTimeMillis()-startTime));
		// Construct Frame
		JFrame frame = new JFrame("Model only");
		// Set Close Operation to Exit
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Add an Editor Panel
		ScrollPane scrollPane = new ScrollPane();
//		scrollPane.add(new JGraph(model));
		frame.getContentPane().add(scrollPane);
		// Set Default Size
		frame.setSize(520, 390);
		// Show Frame
		frame.setVisible(true);
	}
}