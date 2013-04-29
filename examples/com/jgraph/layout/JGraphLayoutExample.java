/*
 * $Id: JGraphLayoutExample.java,v 1.2 2009-10-30 14:17:07 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved. 
 * 
 * This file is licensed under the JGraph software license, a copy of which
 * will have been provided to you in the file LICENSE at the root of your
 * installation directory. If you are unable to locate this file please
 * contact JGraph sales for another copy.
 */
package com.jgraph.layout;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;

import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.GraphModel;

import com.jgraph.example.GraphEdX;
import com.jgraph.layout.graph.JGraphSimpleLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;
import com.jgraph.layout.organic.JGraphFastOrganicLayout;
import com.jgraph.layout.organic.JGraphOrganicLayout;
import com.jgraph.layout.organic.JGraphSelfOrganizingOrganicLayout;
import com.jgraph.layout.routing.JGraphParallelRouter;
import com.jgraph.layout.tree.JGraphCompactTreeLayout;
import com.jgraph.layout.tree.JGraphRadialTreeLayout;
import com.jgraph.layout.tree.JGraphTreeLayout;

/**
 * An example applet that provides features for testing the JGraph Layout
 * package. Sample data can be inserted, the layouts applied, cells moved while
 * the layout auto-applied and morphs the cells into position. This example also
 * supports grouping and expand/collapse.
 */
public class JGraphLayoutExample extends GraphEdX {

	/**
	 * Global reference to the example layout cache.
	 */
	protected JGraphExampleLayoutCache layoutCache;

	// Layout actions
	protected Action[] layoutActions;

	/**
	 * Button representing the auto layout feature
	 */
	protected JToggleButton autoLayoutButton;

	/**
	 * Semaphore for autolayout.
	 */
	protected boolean isInsideLayout = false;
	
	protected int edgeCount = 0;

	/**
	 * Constructs a new example
	 */
	public JGraphLayoutExample() {
		layoutCache = (JGraphExampleLayoutCache) graph.getGraphLayoutCache();
//		JGraphParallelRouter.setGraph(graph);
		// Prepares layout actions
		layoutCache.layout = new JGraphCompactTreeLayout();
		createLayoutActions();
		setJMenuBar(new JGraphLayoutExampleMenuBar(this, graphFactory));
		// Initializes actions states
		valueChanged(null);
	}

	/**
	 * Show something interesting on start.
	 */
	public void init() {
		graphFactory.insertTreeSampleData(getGraph(),
				createCellAttributes(new Point2D.Double(0, 0)),
				createEdgeAttributes());
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// ignore
		}
		layoutActions[0].actionPerformed(null);
	}

	// Override parent method
	protected JGraph createGraph() {
		// Creates a model that does not allow disconnections
		GraphModel model = new MyGraphModel();
		return new JGraphExampleGraph(model);
	}

	/**
	 * Hook from GraphEd to set attributes of a new edge
	 */
	public Map createEdgeAttributes() {
		Map map = super.createEdgeAttributes();
		// Adds a parallel edge router
//		GraphConstants.setRouting(map, JGraphParallelRouter.getSharedInstance());
		return map;
	}

	// Hook for subclassers
	protected DefaultEdge createDefaultEdge() {
		return new DefaultEdge("Edge " + new Integer(edgeCount++));
	}

	/**
	 * Creates the actions for the layouts
	 */
	public void createLayoutActions() {
		JGraphLayout circleLayout = new JGraphSimpleLayout(
				JGraphSimpleLayout.TYPE_CIRCLE);
		JGraphLayout isomLayout = new JGraphSelfOrganizingOrganicLayout();
		JGraphLayout frLayout = new JGraphFastOrganicLayout();
		JGraphLayout hierLayout = new JGraphHierarchicalLayout();
		layoutActions = new Action[] {
				createLayoutAction("Compact Tree", "m", layoutCache.layout),
				createLayoutAction("Tree", "t", new JGraphTreeLayout()),
//				createLayoutAction("Organisational Chart", "t", new OrganizationalChart()),
				createLayoutAction("Radial Tree", "r",
						new JGraphRadialTreeLayout()),
				createLayoutAction("ISOM", "i", isomLayout),
				createLayoutAction("Organic", "o", new JGraphOrganicLayout()),
				createLayoutAction("Fast Organic", "f", frLayout),
				createLayoutAction("Hierarchical", "h", hierLayout),
				createLayoutAction("Circle", "c", circleLayout),
				createLayoutAction("Tilt", "l", new JGraphSimpleLayout(
						JGraphSimpleLayout.TYPE_TILT, 100, 100)),
				createLayoutAction("Random", "n", new JGraphSimpleLayout(
						JGraphSimpleLayout.TYPE_RANDOM, 640, 480)) };
	}

	/**
	 * action to apply a layout
	 * 
	 * @param title
	 *            the name of the layout
	 * @param shortcut
	 *            the string representing the shortcut keyboard combination that
	 *            invokes this action
	 * @param layout
	 *            the layout itself
	 * @return the apply layout action
	 */
	public Action createLayoutAction(final String title, String shortcut,
			final JGraphLayout layout) {
		Action action = new AbstractAction(title) {
			public void actionPerformed(ActionEvent e) {
				layoutCache.layout = layout;
				layoutCache.layout(null);
			}
		};
		action.putValue("shortcut", shortcut);
		return action;

	}

	/**
	 * Updates buttons based on application state
	 */
	public void valueChanged(GraphSelectionEvent e) {
		super.valueChanged(e);
		// Group Button only Enabled if a cell is selected
		boolean enabled = !graph.isSelectionEmpty();
		boolean notEmpty = (graph.getModel().getRootCount() > 0);
		// hide.setEnabled(enabled);
		expand.setEnabled(enabled);
		expandAll.setEnabled(enabled);
		collapse.setEnabled(enabled);
		configure.setEnabled(layoutCache != null && layoutCache.layout != null);
		if (layoutActions != null) {
			for (int i = 0; i < layoutActions.length; i++) {
				layoutActions[i].setEnabled(notEmpty);
			}
		}
	}

	protected void configureEncoder(XMLEncoder encoder) {
		super.configureEncoder(encoder);
		encoder
				.setPersistenceDelegate(JGraphExampleLayoutCache.class,
						new DefaultPersistenceDelegate(new String[] { "model",
								"factory", "cellViews", "hiddenCellViews",
								"partial" }));
		encoder.setPersistenceDelegate(JGraphExampleGraph.class,
				new DefaultPersistenceDelegate(new String[] { "model",
						"graphLayoutCache" }));
		encoder.setPersistenceDelegate(JGraphParallelRouter.class,
				new PersistenceDelegate() {
					protected Expression instantiate(Object oldInstance,
							Encoder out) {
						return new Expression(oldInstance,
								JGraphParallelRouter.class,
								"getSharedInstance", null);
					}
				});
	}

	/**
	 * Hook from GraphEd to add action button to the tool bar
	 */
	public JToolBar createToolBar() {
		JToolBar toolbar = super.createToolBar();

		// Configure layout
		configure = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				layoutCache.configureLayout();
			}
		};
		URL url = getClass().getClassLoader().getResource(
				"com/jgraph/layout/image/configure.gif");
		configure.putValue(Action.SMALL_ICON, new ImageIcon(url));
		configure.setEnabled(false);
		toolbar.add(configure);

		// AutoLayout toggle
		autoLayoutButton = new JToggleButton();
		autoLayoutButton.setSelected(false);
		url = getClass().getClassLoader().getResource(
				"com/jgraph/layout/image/layout.gif");
		autoLayoutButton.setIcon(new ImageIcon(url));
		autoLayoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				layoutCache.autolayout = autoLayoutButton.isSelected();
			}
		});
		toolbar.add(autoLayoutButton);

		// JButton button = new JButton("My");
		// toolbar.add(button);
		// button.addActionListener(new ActionListener() {
		//
		// public void actionPerformed(ActionEvent event) {
		// // Add your code here
		// }
		//
		// });
		return toolbar;
	}

	public void openFile() {
		int returnValue = JFileChooser.CANCEL_OPTION;
		initFileChooser();
		returnValue = fileChooser.showOpenDialog(graph);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			Container parent = graph.getParent();
			BasicMarqueeHandler marqueeHandler = graph.getMarqueeHandler();
			try {
				uninstallListeners(graph);
				parent.remove(graph);
				XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(
						new FileInputStream(fileChooser.getSelectedFile())));
				graph = (JGraph) decoder.readObject();
				// Take the marquee handler from the original graph and
				// use it in the new graph as well.
				graph.setMarqueeHandler(marqueeHandler);
				JGraphParallelRouter.setGraph(graph);
				// Adds the component back into the component hierarchy
				if (parent instanceof JViewport) {
					JViewport viewPort = (JViewport) parent;
					viewPort.setView(graph);
				} else {
					// Best effort...
					parent.add(graph);
				}
				// graph.setMarqueeHandler(previousHandler);
				// And reinstalls the listener
				installListeners(graph);
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(graph, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	/**
	 * 
	 * @return a String representing the version of this application
	 */
	protected String getVersion() {
		return JGraph.VERSION;
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
			JGraphLayoutExample layoutExample = new JGraphLayoutExample();
			frame.getContentPane().add(layoutExample);
			// Fetch URL to Icon Resource
			URL jgraphUrl = JGraphLayoutExample.class.getClassLoader()
					.getResource("org/jgraph/example/resources/jgraph.gif");
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
			layoutExample.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
