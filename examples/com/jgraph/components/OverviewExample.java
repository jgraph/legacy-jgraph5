/*
 * Copyright (c) 2005-2006, David Benson
 * 
 * All rights reserved. 
 * 
 * This file is licensed under the JGraph software license, a copy of which
 * will have been provided to you in the file LICENSE at the root of your
 * installation directory. If you are unable to locate this file please
 * contact JGraph sales for another copy.
 */
package com.jgraph.components;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.jgraph.example.GraphEdX;
import com.jgraph.navigation.GraphNavigator;

/**
 * Simple example showing the creation and use of a graph overview (also known
 * as birds-eye view and navigator).
 */
public class OverviewExample extends GraphEdX {

	public OverviewExample() {
	}

	// Hook for subclassers
	protected void populateContentPane() {
		// Use Border Layout
		getContentPane().setLayout(new BorderLayout());
		// Add a ToolBar
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
		// Add a Navigator
		GraphNavigator navigator = GraphNavigator
				.createInstance(createGraph());
		navigator.setCurrentGraph(graph);
		// Add the Graph as Center Component
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				navigator, new JScrollPane(graph));
		getContentPane().add(splitPane, BorderLayout.CENTER);
		statusBar = createStatusBar();
		getContentPane().add(statusBar, BorderLayout.SOUTH);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Switch off D3D because of Sun XOR painting bug
			// See http://www.jgraph.com/forum/viewtopic.php?t=4066
			System.setProperty("sun.java2d.d3d", "false");
			// Construct Frame
			JFrame frame = new JFrame("Overview Example");
			// Set Close Operation to Exit
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			// Fetch URL to Icon Resource
			URL jgraphUrl = GraphEdX.class.getClassLoader().getResource(
					"org/jgraph/example/resources/jgraph.gif");
			// If Valid URL
			if (jgraphUrl != null) {
				// Load Icon
				ImageIcon jgraphIcon = new ImageIcon(jgraphUrl);
				// Use in Window
				frame.setIconImage(jgraphIcon.getImage());
			}
			// Add an Editor Panel
			OverviewExample app = new OverviewExample();
			frame.getContentPane().add(app);
			app.init();
			// Set Default Size
			frame.setSize(640, 480);
			// Show Frame
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
