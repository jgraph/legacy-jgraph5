/*
 * Copyright (c) 2005, Gaudenz Alder
 * Copyright (c) 2005-2006, David Benson
 * 
 */
package com.jgraph.example.portlabels;


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
import com.jgraph.example.JGraphGraphFactory;
import org.jgraph.example.GraphEd;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;

/**
 * An example applet that demonstrates how to improve the performance of
 * JGraph and reduce its memory footprint
 */
public class PortLabelGraph extends GraphEd {

	/**
	 * JGraph Factory instance for random new graphs
	 */
	protected JGraphGraphFactory graphFactory;

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
		GraphConstants.setAutoSize(map, true);
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
		double numLeftSidePorts = random.nextInt(6) + 2;
		double numRightSidePorts = random.nextInt(6) + 2;
		
		double height = Math.max(numLeftSidePorts, numRightSidePorts);
		for (double i=1.0; i <= height ;i++) {
			if (i <= numLeftSidePorts) {
				DefaultPort port = new DefaultPort("(0, GraphConstants.PERMILLE / " + new Double(i / (numLeftSidePorts+1)) + ")");
				Point2D point = new Point2D.Double(0,
						i * (GraphConstants.PERMILLE / (numLeftSidePorts+1)));
				GraphConstants.setOffset(port.getAttributes(), point);
				GraphConstants.setBackground(port.getAttributes(), Color.RED);
				cell.add(port);
			}
			if (i <= numRightSidePorts) {
				DefaultPort port = new DefaultPort("(GraphConstants.PERMILLE, GraphConstants.PERMILLE / " + new Double(i / (numRightSidePorts+1)) + ")");
				Point2D point = new Point2D.Double(GraphConstants.PERMILLE,
						i * (GraphConstants.PERMILLE / (numRightSidePorts+1)));
				GraphConstants.setOffset(port.getAttributes(), point);
				GraphConstants.setBackground(port.getAttributes(), Color.YELLOW);
				cell.add(port);
			}
		}
		
		height *= 50;
		Map map = cell.getAttributes();
		// Add a Bounds Attribute to the Map
		GraphConstants.setBounds(map, new Rectangle2D.Double(20,
				20, 160, height));
		return cell;
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