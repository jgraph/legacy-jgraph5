/* 
 * $Id: NavigationExample.java,v 1.1 2009-09-25 15:17:49 david Exp $
 * Copyright (c) 2005-2006 David Benson
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.navigation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import org.jgraph.example.GraphEd;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;

import com.jgraph.example.GraphEdX;

public class NavigationExample extends GraphEdX {

	public NavigationExample() {
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
	 * Hook from GraphEd to set attributes of a new cell
	 */
	public Map createCellAttributes(Point2D point) {
		Map map = super.createCellAttributes(point);
		GraphConstants.setOpaque(map, false);
		map.remove(GraphConstants.BORDERCOLOR);
		GraphConstants.setInset(map, 5);
		GraphConstants.setGradientColor(map, new Color(200, 200, 255));
		URL iconUrl = getClass().getClassLoader().getResource(
				"com/informavores/example/centre.gif");
		final ImageIcon centreIcon = new ImageIcon(iconUrl);
		GraphConstants.setIcon(map, centreIcon);
		return map;
	}

	// Hook for subclassers
	protected DefaultGraphCell createDefaultGraphCell() {
		DefaultGraphCell cell = new DefaultGraphCell("");
		// Add one Floating Port
		DefaultPort port = new DefaultPort();
		Point2D midPoint = new Point2D.Double(GraphConstants.PERMILLE / 2,
				GraphConstants.PERMILLE / 2);
		GraphConstants.setOffset(port.getAttributes(), midPoint);
		cell.add(port);
		return cell;
	}

	// Hook for subclassers
	protected DefaultGraphCell createBranchCell() {
		DefaultGraphCell cell = new DefaultGraphCell();
		GraphConstants.setSizeable(cell.getAttributes(), false);
		GraphConstants.setEditable(cell.getAttributes(), false);
		// Add one Floating Port
		DefaultPort port = new DefaultPort();
		cell.add(port);
		AttributeMap attrs = graph.getModel().getAttributes(port);
		GraphConstants.setAbsolute(attrs, false);
		double u2 = GraphConstants.PERMILLE / 2;
		GraphConstants.setOffset(attrs, attrs.createPoint(u2, u2));
		return cell;
	}

	/**
	 * Hook from GraphEd to set attributes of a new edge
	 */
	public Map createEdgeAttributes() {
		Map map = super.createEdgeAttributes();
		// Adds a parallel edge router
		GraphConstants.setLineStyle(map, GraphConstants.STYLE_SPLINE);
		GraphConstants.setFont(map, GraphConstants.DEFAULTFONT.deriveFont(10f));
		GraphConstants.setBackground(map, Color.black);
		GraphConstants.setLineColor(map, Color.black);
		return map;
	}

	/**
	 * Hook from GraphEd to add action button to the tool bar
	 */
	public JToolBar createToolBar() {
		NavigationToolBar toolbarFactory = new NavigationToolBar();
		JToolBar toolbar = toolbarFactory.createToolBar(this, graph);
		return toolbar;
	}

	// Main Method
	public static void main(String[] args) {
		// Construct Frame
		JFrame frame = new JFrame("Navigation Example");
		// Set Close Operation to Exit
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Add an Editor Panel
		frame.getContentPane().add(new NavigationExample());
		// Fetch URL to Icon Resource
		URL jgraphUrl = GraphEd.class.getClassLoader().getResource(
				"org/jgraph/example/resources/jgraph.gif");
		// If Valid URL
		if (jgraphUrl != null) {
			// Load Icon
			ImageIcon jgraphIcon = new ImageIcon(jgraphUrl);
			// Use in Window
			frame.setIconImage(jgraphIcon.getImage());
		}
		// Set Default Size
		frame.setSize(520, 390);
		// Show Frame
		frame.setVisible(true);
	}

}