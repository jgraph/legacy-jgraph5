package com.jgraph.example.panelexample;
/*
 * Copyright (c) 2005, Gaudenz Alder
 * Copyright (c) 2005, David Benson
 * 
 */

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jgraph.JGraph;
import org.jgraph.example.GraphEd;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;


public class PortLabelGraph extends GraphEd {


	/**
	 * Reference to the layout dialog which is used to configure the current
	 * layout.
	 */
	protected JDialog sampleDialog = null;

	/**
	 * Constructs a new example
	 */
	public PortLabelGraph() {
		graph.getGraphLayoutCache().setFactory(new DefaultCellViewFactory() {
			/**
			 * Constructs an EdgeView view for the specified object.
			 */
			protected EdgeView createEdgeView(Object cell) {
				return new EdgeView(cell);
			}

			/**
			 * Constructs a PortView view for the specified object.
			 */
			protected PortView createPortView(Object cell) {
				return new PortView(cell);
			}

			/**
			 * Constructs a VertexView view for the specified object.
			 */
			protected VertexView createVertexView(Object cell) {
				if (cell instanceof PortLabelCell) {
					return new PortLabelVertexView(cell);
				} else {
					return new VertexView(cell);
				}
			}
		});
	}

	/**
	 * Hook from GraphEd to set attributes of a new cell
	 */
	public Map createCellAttributes(Point2D point) {
		Map map = new Hashtable();
		// Add a Border Color Attribute to the Map
		GraphConstants.setBorderColor(map, Color.black);
		// Add a White Background
		GraphConstants.setBackground(map, Color.white);
		// Set autosize
		//GraphConstants.setAutoSize(map, true);
		// Move the label of the vertex to the bottom
		GraphConstants.setVerticalAlignment(map, JLabel.BOTTOM);
		return map;
	}

	/**
	 * Create vertex with random numbers of ports that have labels
	 */
	protected DefaultGraphCell createDefaultGraphCell() {
		DefaultGraphCell cell = new PortLabelCell("Cell "
				+ new Integer(cellCount++));
		// Add a random number of ports on either side of the vertex
		Random random = new Random();
		int numLeftSidePorts = random.nextInt(4);//0 to 3 ports are supported in our example
		int numRightSidePorts = random.nextInt(4);
		
		int height = Math.max(numLeftSidePorts, numRightSidePorts);
		for (int i=1; i <= height ;i++) {
			if (i <= numLeftSidePorts) {
				DefaultPort port;
				if (i % 2 == 0) {
					port = new DefaultPort("Test Label");
				} else {
					port = new DefaultPort("A Somewhat Longer Test Label");					
				}
				double pos = roundPortPos(random.nextInt(3));		
				Point2D point = new Point2D.Double(0,
						(GraphConstants.PERMILLE * pos));
				GraphConstants.setOffset(port.getAttributes(), point);
				cell.add(port);
			}
			if (i <= numRightSidePorts) {
				DefaultPort port;
				if (i % 2 == 0) {
					port = new DefaultPort("A Somewhat Longer Test Label");
				} else {
					port = new DefaultPort("Test Label");
				}
				double pos = roundPortPos(random.nextInt(3));		
				Point2D point = new Point2D.Double(GraphConstants.PERMILLE,
						(GraphConstants.PERMILLE * pos));
				GraphConstants.setOffset(port.getAttributes(), point);
				cell.add(port);
			}
		}
		
		height *= 40 + 100;
		Map map = cell.getAttributes();
		// Add a Bounds Attribute to the Map
		GraphConstants.setBounds(map, new Rectangle2D.Double(20,
				20, 200, height));
		return cell;
	}
	
	public static double roundPortPos(double pos) {
		if (pos == 0)
			pos = 0.1;
		else if (pos == 1)
			pos = 0.5;
		else
			pos = 0.9;
		return pos;
	}

	/**
	 * Main method
	 */
	public static void main(String[] args) {
		try {
			// Switch off D3D because of Sun XOR painting bug
			// See http://www.jgraph.com/forum/viewtopic.php?t=4066
			System.setProperty("sun.java2d.d3d", "false");
			// Construct Frame
			JFrame frame = new JFrame(JGraph.VERSION);
			// Set Close Operation to Exit
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			// Add an Editor Panel
			PortLabelGraph portLabelGraph = new PortLabelGraph();
			frame.getContentPane().add(portLabelGraph);
			// Fetch URL to Icon Resource
			URL jgraphUrl = PortLabelGraph.class.getClassLoader().getResource(
					"org/jgraph/example/resources/jgraph.gif");
			// If Valid URL
			if (jgraphUrl != null) {
				// Load Icon
				ImageIcon jgraphIcon = new ImageIcon(jgraphUrl);
				// Use in Window
				frame.setIconImage(jgraphIcon.getImage());
			}
			// Set Default Size
			frame.setSize(640, 480);
			// Show Frame
			frame.setVisible(true);
			portLabelGraph.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}