/*
 * $Id: FastGraph.java,v 1.4 2009-02-12 18:40:04 david Exp $
 * 
 * Copyright (c) 2001-2005, Gaudenz Alder
 * Copyright (c) 2005, David Benson
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.fastgraph;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.jgraph.JGraph;
import org.jgraph.example.GraphEd;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

import com.jgraph.example.JGraphGraphFactory;

/**
 * An example applet that demonstrates how to improve the performance of
 * JGraph and reduce its memory footprint
 */
public class FastGraph extends GraphEd {

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
	public FastGraph() {
		setJMenuBar(new FastGraphMenuBar(this));
		// Initializes actions states
		valueChanged(null);

		graphFactory = new FastGraphFactory();

		// Use a Custom Marquee Handler
		graph.setMarqueeHandler(new FastMarqueeHandler());

		graph.getGraphLayoutCache().setFactory(new FastCellViewFactory());
	}

	// Override parent method
	protected JGraph createGraph() {
		// Creates a model that does not allow disconnections
		return new MyFastGraph(new FastGraphModel());
	}

	//
	// Custom Graph
	//

	// Defines a Graph that uses the Shift-Button (Instead of the Right
	// Mouse Button, which is Default) to add/remove point to/from an edge.
	public static class MyFastGraph extends JGraph {

		// Construct the Graph using the Model as its Data Source
		public MyFastGraph(GraphModel model) {
			super(model);
			// Make Ports Invisible by Default
			setPortsVisible(false);
			// No Grid
			setGridEnabled(false);
			// Set the Tolerance to 2 Pixel
			setTolerance(2);
			// Accept edits if click on background
			setInvokesStopCellEditing(true);
			// Allows control-drag
			setCloneable(true);
			// Jump to default port on connect
			setJumpToDefaultPort(true);
			// Turn off double buffering for speed
			setDoubleBuffered(false);
			// Pretty background
			setBackground(Color.YELLOW);
		}

		/** 
		 * Override parent method with custom GraphUI. 
		 */
		public void updateUI() {
			setUI(new FastGraphUI());
			invalidate();
		}
	}

	public class FastMarqueeHandler extends MyMarqueeHandler {
		// Add timings
		public void mousePressed(final MouseEvent e) {
			System.out.println("Started mousePressed, timestamp="
					+ System.currentTimeMillis() + "ms");
			super.mousePressed(e);
		}

		// Add timings
		public void mouseDragged(MouseEvent e) {
			System.out.println("Started mouseDragged, timestamp="
					+ System.currentTimeMillis() + "ms");
			super.mouseDragged(e);
		}

		// Add timings
		public void mouseReleased(MouseEvent e) {
			System.out.println("Started mouseReleased, timestamp="
					+ System.currentTimeMillis() + "ms");
			super.mouseReleased(e);
		}

	}

	public class FastGraphMenuBar extends JMenuBar {

		public FastGraphMenuBar(final FastGraph app) {
			// Sample data menu
			JMenu sampleMenu = new JMenu("Sample Data");

			sampleMenu.add(new JMenuItem(new AbstractAction(
					"Insert Randomly Connected Graph") {
				public void actionPerformed(ActionEvent e) {
					graphFactory.setInsertIntoModel(true);
					System.out
							.println("Starting to insert randomly connected graph, timestamp="
									+ System.currentTimeMillis() + "ms");
					graphFactory.insertGraph(app.getGraph(),
							JGraphGraphFactory.RANDOM_CONNECTED, app
									.createCellAttributes(new Point2D.Double(0,
											0)), app.createEdgeAttributes());
					System.out
							.println("Finished inserting randomly connected graph, timestamp="
									+ System.currentTimeMillis() + "ms");
				}
			}));

			sampleMenu.add(new JMenuItem(new AbstractAction(
					"Insert Fully Connected Graph") {
				public void actionPerformed(ActionEvent e) {
					graphFactory.setInsertIntoModel(true);
					System.out
							.println("Starting to insert fully connected graph, timestamp="
									+ System.currentTimeMillis() + "ms");
					graphFactory.insertGraph(app.getGraph(),
							JGraphGraphFactory.FULLY_CONNECTED, app
									.createCellAttributes(new Point2D.Double(0,
											0)), app.createEdgeAttributes());
					System.out
							.println("Finished inserting fully connected graph, timestamp="
									+ System.currentTimeMillis() + "ms");
				}
			}));

			add(sampleMenu);

			// Performance/Memory data menu
			JMenu profileMenu = new JMenu("Profiling");
			profileMenu.add(new JMenuItem(new AbstractAction(
					"Perform Garbage Collection") {
				public void actionPerformed(ActionEvent e) {
					System.gc();
				}
			}));

			add(profileMenu);
		}
	}

	/**
	 * Hook from GraphEd to set attributes of a new cell
	 */
	public Map createCellAttributes(Point2D point) {
		Map map = new Hashtable();
		// Add a Bounds Attribute to the Map
		GraphConstants.setBounds(map, new Rectangle2D.Double(point.getX(),
				point.getY(), 40, 40));
		// Add a nice looking gradient background
		GraphConstants.setGradientColor(map, Color.blue);
		// Add a Border Color Attribute to the Map
		GraphConstants.setBorderColor(map, Color.black);
		// Add a White Background
		GraphConstants.setBackground(map, Color.white);
		// Make Vertex Opaque
		GraphConstants.setOpaque(map, true);
		return map;
	}

	/**
	 * Create circles with fixed ports
	 */
	protected DefaultGraphCell createDefaultGraphCell() {
		DefaultGraphCell cell = new FastCircleCell("Cell "
				+ new Integer(cellCount++));
		// Restrict to a square shape
		GraphConstants.setConstrained(cell.getAttributes(), true);
		// Add one central fixed port
		Point2D point = new Point2D.Double(GraphConstants.PERMILLE / 2,
				GraphConstants.PERMILLE / 2);
		cell.addPort(point);
		return cell;
	}

	/**
	 * Hook from GraphEd to set attributes of a new edge
	 */
	public Map createEdgeAttributes() {
		return null;
	}

	/**
	 * Main method
	 */
	public static void main(String[] args) {
		try {
			// Switch off D3D because of Sun XOR painting bug
			// See http://www.jgraph.com/forum/viewtopic.php?t=4066
			System.setProperty("sun.java2d.d3d", "false");
			System.out.println("FastGraph starting, timestamp="
					+ System.currentTimeMillis() + "ms");
			// Construct Frame
			JFrame frame = new JFrame(JGraph.VERSION);
			// Set Close Operation to Exit
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			// Add an Editor Panel
			FastGraph fastGraph = new FastGraph();
			frame.getContentPane().add(fastGraph);
			// Fetch URL to Icon Resource
			URL jgraphUrl = FastGraph.class.getClassLoader().getResource(
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
			fastGraph.init();
			System.out.println("FastGraph initialised, timestamp="
					+ System.currentTimeMillis() + "ms");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
