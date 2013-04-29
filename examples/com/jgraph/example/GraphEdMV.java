/*
 * @(#)GraphEdMV.java 3.3 23-APR-04
 * 
 * Copyright (c) 2001-2005, Gaudenz Alder All rights reserved.
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.UndoableEditEvent;

import org.jgraph.event.GraphSelectionListener;
import org.jgraph.example.GraphEd;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphUndoManager;

public class GraphEdMV extends GraphEd
		implements
			GraphSelectionListener,
			KeyListener {

	// Shared Model
	protected static GraphModel model;

	// Undo Manager
	protected static GraphUndoManager undoManager = new GraphUndoManager() {

		public void undoableEditHappened(UndoableEditEvent e) {
			super.undoableEditHappened(e);
			// Then Update Undo/Redo Buttons
			updateAllHistoryButtons();
		}
	};

	protected static ArrayList instances = new ArrayList();

	// Actions which Change State
	protected Action undo, redo, remove, group, ungroup, tofront, toback, cut,
			copy, paste;

	// 
	// Main
	// 
	// Main Method
	public static void main(String[] args) {
		// Switch off D3D because of Sun XOR painting bug
		// See http://www.jgraph.com/forum/viewtopic.php?t=4066
		System.setProperty("sun.java2d.d3d", "false");
		model = new MyModel();
		// Register UndoManager with the Model
		model.addUndoableEditListener(undoManager);
		// Construct Frame
		JFrame frame = new JFrame("GraphEdMV");
		// Set Close Operation to Exit
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Add an Editor Panel
		GraphEdMV e1 = new GraphEdMV();
		GraphEdMV e2 = new GraphEdMV();
		instances.add(e1);
		instances.add(e2);
		frame.getContentPane().add(
				new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, e1, e2));
		// Fetch URL to Icon Resource
		URL jgraphUrl = GraphEdMV.class.getClassLoader().getResource("org/jgraph/example/resources/jgraph.gif");
		// If Valid URL
		if (jgraphUrl != null) {
			// Load Icon
			ImageIcon jgraphIcon = new ImageIcon(jgraphUrl);
			// Use in Window
			frame.setIconImage(jgraphIcon.getImage());
		}
		// Set Default Size
		frame.pack(); //setSize(520, 390);
		frame.setSize(new Dimension(frame.getWidth(), 400));
		// Show Frame
		frame.setVisible(true);
	}

	// 
	// Editor Panel
	// 
	// Construct an Editor Panel
	public GraphEdMV() {
		// Use Border Layout
		getContentPane().setLayout(new BorderLayout());
		// Construct the Graph
		graph = new MyGraph(model);
		// Use a Custom Marquee Handler
		graph.setMarqueeHandler(new MyMarqueeHandler());
		
		// Define the set of "view-local" attributes
		Set localAttributes = new HashSet();
		localAttributes.add(GraphConstants.BOUNDS);
		localAttributes.add(GraphConstants.POINTS);
		localAttributes.add(GraphConstants.LABELPOSITION);
		localAttributes.add(GraphConstants.ROUTING);
		graph.getGraphLayoutCache().setLocalAttributes(localAttributes);
		// 
		// Add Listeners to Graph
		// 
		// Update ToolBar based on Selection Changes
		graph.getSelectionModel().addGraphSelectionListener(this);
		// Listen for Delete Keystroke when the Graph has Focus
		graph.addKeyListener(this);
		// Construct Panel
		// 
		// Add a ToolBar
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
		// Add the Graph as Center Component
		getContentPane().add(new JScrollPane(graph), BorderLayout.CENTER);
	}

	// Create a Group that Contains the Cells
	public void group(Object[] cells) {
		// Invert Array for Model ordering
		Object[] tmp = new Object[cells.length];
		for (int i = 0; i < cells.length; i++)
			tmp[cells.length - i - 1] = cells[i];
		cells = tmp;
		super.group(tmp);
	}

	protected static void updateAllHistoryButtons() {
		Iterator it = instances.iterator();
		while (it.hasNext())
			((GraphEdMV) it.next()).updateHistoryButtons();
	}

}