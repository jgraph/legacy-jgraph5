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

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.VertexView;

import com.jgraph.components.labels.MultiLineVertexView;
import com.jgraph.components.labels.RichTextBusinessObject;
import com.jgraph.components.labels.RichTextGraphModel;
import com.jgraph.components.labels.RichTextValue;
import com.jgraph.example.GraphEdX;

/**
 * Simple example showing the creation and use of a graph overview (also known
 * as birds-eye view and navigator).
 */
public class RichTextExample extends GraphEdX {

	public RichTextExample() {
	}

	// Override parent method
	protected JGraph createGraph() {
		// Creates a model that does not allow disconnections
		GraphModel model = new RichTextGraphModel();
		GraphLayoutCache layoutCache = new GraphLayoutCache(model,
				new DefaultCellViewFactory() {
			protected VertexView createVertexView(Object cell) {
				return new MultiLineVertexView(cell);
			}
		}, true);
		return new MyGraph(model, layoutCache);
	}

	// Hook for subclassers
	protected DefaultGraphCell createDefaultGraphCell() {
		RichTextBusinessObject userObject = new RichTextBusinessObject();
		RichTextValue textValue = new RichTextValue("Cell "	+ new Integer(cellCount++));
		userObject.setValue(textValue);
		DefaultGraphCell cell = new DefaultGraphCell(userObject);
		// Add one Floating Port
		cell.addPort();
		return cell;
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
			JFrame frame = new JFrame("Rich Text Example");
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
			RichTextExample app = new RichTextExample();
			frame.getContentPane().add(app);
			// Set Default Size
			frame.setSize(640, 480);
			// Show Frame
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
