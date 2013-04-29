/*
 * $Id: JGraphLayoutPanel.java,v 1.2 2009-10-30 14:16:52 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved. 
 * 
 * This file is licensed under the JGraph software license, a copy of which
 * will have been provided to you in the file LICENSE at the root of your
 * installation directory. If you are unable to locate this file please
 * contact JGraph sales for another copy.
 */
package com.jgraph.layout.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;

import com.jgraph.example.JGraphGraphFactory;
import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.graph.JGraphSimpleLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;
import com.jgraph.layout.organic.JGraphFastOrganicLayout;
import com.jgraph.layout.organic.JGraphOrganicLayout;
import com.jgraph.layout.organic.JGraphSelfOrganizingOrganicLayout;
import com.jgraph.layout.tree.JGraphCompactTreeLayout;
import com.jgraph.layout.tree.JGraphRadialTreeLayout;
import com.jgraph.layout.tree.JGraphTreeLayout;
import com.l2fprod.common.swing.JTaskPane;
import com.l2fprod.common.swing.JTaskPaneGroup;

public class JGraphLayoutPanel extends JPanel {

	protected static JGraphGraphFactory graphFactory = new JGraphGraphFactory();

	protected JGraph graph = new JGraph(new DefaultGraphModel());

	protected JTaskPane taskPane = new JTaskPane();

	protected JCheckBox flushOriginCheckBox = new JCheckBox("Flush", true),
			directedCheckBox = new JCheckBox("Directed", true);

	/**
	 * Holds the morphing manager.
	 */
	protected JGraphLayoutMorphingManager morpher = new JGraphLayoutMorphingManager();

	public JGraphLayoutPanel() {
		super(new BorderLayout());
		setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

		// Configures the graph
		graph.setGridSize(4);
		graph.setGridEnabled(true);
		graph.setAntiAliased(true);
		graph.setCloneable(true);

		// Configures the taskpane
		JTaskPaneGroup taskGroup = new JTaskPaneGroup();
		taskGroup.setText("Layout");
		taskGroup.add(new AbstractAction("Hierarchical") {
			public void actionPerformed(ActionEvent e) {
				execute(new JGraphHierarchicalLayout());
			}
		});
		taskGroup.add(new AbstractAction("Fast Organic") {
			public void actionPerformed(ActionEvent e) {
				JGraphFastOrganicLayout layout = new JGraphFastOrganicLayout();
				layout.setForceConstant(60);
				execute(layout);
			}
		});
		taskGroup.add(new AbstractAction("Organic") {
			public void actionPerformed(ActionEvent e) {
				JGraphOrganicLayout layout = new JGraphOrganicLayout();
				layout.setOptimizeBorderLine(false);
				execute(layout);
			}
		});

		taskGroup.add(new AbstractAction("Self-Organizing") {
			public void actionPerformed(ActionEvent e) {
				JGraphSelfOrganizingOrganicLayout layout = new JGraphSelfOrganizingOrganicLayout();
				layout.setStartRadius(4);
				layout.setMaxIterationsMultiple(40);
				layout.setDensityFactor(20000);
				execute(new JGraphSelfOrganizingOrganicLayout());
			}
		});
		taskGroup.add(new AbstractAction("Compact Tree") {
			public void actionPerformed(ActionEvent e) {
				execute(new JGraphCompactTreeLayout());
			}
		});
		taskGroup.add(new AbstractAction("Radialtree") {
			public void actionPerformed(ActionEvent e) {
				execute(new JGraphRadialTreeLayout());
			}
		});
		taskGroup.add(new AbstractAction("Tree") {
			public void actionPerformed(ActionEvent e) {
				execute(new JGraphTreeLayout());
			}
		});
		taskGroup.add(new AbstractAction("Reset") {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		// taskGroup.add(new AbstractAction("Tilt") {
		// public void actionPerformed(ActionEvent e) {
		// execute(new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_TILT,
		// 100, 100));
		// }
		// });
		// taskGroup.add(new AbstractAction("Random") {
		// public void actionPerformed(ActionEvent e) {
		// execute(new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_RANDOM,
		// 640, 480));
		// }
		// });

		taskPane.add(taskGroup);

		taskGroup = new JTaskPaneGroup();
		taskGroup.setText("Graph");
		taskGroup.add(new AbstractAction("Create Graph") {
			public void actionPerformed(ActionEvent e) {
				graphFactory.insertGraph(graph,
						JGraphGraphFactory.RANDOM_CONNECTED,
						createCellAttributes(new Point2D.Double(0, 0)),
						createEdgeAttributes());
				reset();
			}
		});
		taskGroup.add(new AbstractAction("Create Tree") {
			public void actionPerformed(ActionEvent e) {
				graphFactory.insertGraph(graph, JGraphGraphFactory.TREE,
						createCellAttributes(new Point2D.Double(0, 0)),
						createEdgeAttributes());
				reset();
			}
		});
		taskGroup.add(new AbstractAction("Actual Size") {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(1);
			}
		});
		taskGroup.add(new AbstractAction("Fit Window") {
			public void actionPerformed(ActionEvent e) {
				JGraphLayoutMorphingManager.fitViewport(graph);
			}
		});
		taskPane.add(taskGroup);

		taskGroup = new JTaskPaneGroup();
		taskGroup.setText("Options");
		flushOriginCheckBox.setOpaque(false);
		taskGroup.add(flushOriginCheckBox);
		directedCheckBox.setOpaque(false);
		taskGroup.add(directedCheckBox);
		taskPane.add(taskGroup);

		// Adds the split pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(taskPane), new JScrollPane(graph));
		add(splitPane, BorderLayout.CENTER);

		// Adds the status bar
		JLabel version = new JLabel(JGraph.VERSION);
		version.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		version.setFont(version.getFont().deriveFont(Font.PLAIN));
		add(version, BorderLayout.SOUTH);
	}

	/**
	 * Shows something useful. Should be called after the enclosing frame has
	 * been made visible.
	 */
	public void init() {
		graphFactory.insertConnectedGraphSampleData(graph,
				createCellAttributes(new Point2D.Double(0, 0)),
				createEdgeAttributes());
		reset();
	}

	/**
	 * Resets the graph to a circular layout.
	 */
	public void reset() {
		execute(new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_CIRCLE));
		graph.clearSelection();
		JGraphLayoutMorphingManager.fitViewport(graph);
	}

	/**
	 * Executes the current layout on the current graph by creating a facade and
	 * progress monitor for the layout and invoking it's run method in a
	 * separate thread so this method call returns immediately. To display the
	 * result of the layout algorithm a {@link JGraphLayoutMorphingManager} is
	 * used.
	 */
	public void execute(final JGraphLayout layout) {
		if (graph != null && graph.isEnabled() && graph.isMoveable()
				&& layout != null) {
			final JGraphFacade facade = createFacade(graph);

			final ProgressMonitor progressMonitor = (layout instanceof JGraphLayout.Stoppable) ? createProgressMonitor(
					graph, (JGraphLayout.Stoppable) layout)
					: null;
			new Thread() {
				public void run() {
					synchronized (this) {
						try {
							// Executes the layout and checks if the user has
							// clicked
							// on cancel during the layout run. If no progress
							// monitor
							// has been displayed or cancel has not been pressed
							// then
							// the result of the layout algorithm is processed.
							layout.run(facade);

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									boolean ignoreResult = false;
									if (progressMonitor != null) {
										ignoreResult = progressMonitor
												.isCanceled();
										progressMonitor.close();
									}
									if (!ignoreResult) {

										// Processes the result of the layout
										// algorithm
										// by creating a nested map based on the
										// global
										// settings and passing the map to a
										// morpher
										// for the graph that should be changed.
										// The morpher will animate the change
										// and then
										// invoke the edit method on the graph
										// layout
										// cache.
										Map map = facade.createNestedMap(true,
												(flushOriginCheckBox
														.isSelected()) ? true
														: false);
										morpher.morph(graph, map);
										graph.requestFocus();
									}
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane
									.showMessageDialog(graph, e.getMessage());
						}
					}
				}
			}.start(); // fork
		}
	}

	/**
	 * Creates a {@link JGraphFacade} and makes sure it contains a valid set of
	 * root cells if the specified layout is a tree layout. A root cell in this
	 * context is one that has no incoming edges.
	 * 
	 * @param graph
	 *            The graph to use for the facade.
	 * @return Returns a new facade for the specified layout and graph.
	 */
	protected JGraphFacade createFacade(JGraph graph) {
		// Creates and configures the facade using the global switches
		JGraphFacade facade = new JGraphFacade(graph, graph.getSelectionCells());
		facade.setIgnoresUnconnectedCells(true);
		facade.setIgnoresCellsInGroups(true);
		facade.setIgnoresHiddenCells(true);
		facade.setDirected(directedCheckBox.isSelected());

		// Removes all existing control points from edges
		facade.resetControlPoints();
		return facade;
	}

	/**
	 * Creates a {@link JGraphLayoutProgressMonitor} for the specified layout.
	 * 
	 * @param graph
	 *            The graph to use as the parent component.
	 * @param layout
	 *            The layout to create the progress monitor for.
	 * @return Returns a new progress monitor.
	 */
	protected ProgressMonitor createProgressMonitor(JGraph graph,
			JGraphLayout.Stoppable layout) {
		ProgressMonitor monitor = new JGraphLayoutProgressMonitor(graph,
				((JGraphLayout.Stoppable) layout).getProgress(),
				"PerformingLayout");
		monitor.setMillisToDecideToPopup(100);
		monitor.setMillisToPopup(500);
		return monitor;
	}

	/**
	 * Hook from GraphEd to set attributes of a new cell
	 */
	public Map createCellAttributes(Point2D point) {
		Map map = new Hashtable();
		// Snap the Point to the Grid
		point = graph.snap((Point2D) point.clone());
		// Add a Bounds Attribute to the Map
		GraphConstants.setBounds(map, new Rectangle2D.Double(point.getX(),
				point.getY(), 0, 0));
		// Make sure the cell is resized on insert
		GraphConstants.setResize(map, true);
		// Add a nice looking gradient background
		GraphConstants.setGradientColor(map, Color.blue);
		// Add a Border Color Attribute to the Map
		GraphConstants.setBorderColor(map, Color.black);
		// Add a White Background
		GraphConstants.setBackground(map, Color.white);
		// Make Vertex Opaque
		GraphConstants.setOpaque(map, true);
		GraphConstants.setInset(map, 2);
		GraphConstants.setGradientColor(map, new Color(200, 200, 255));
		return map;
	}

	/**
	 * Hook from GraphEd to set attributes of a new edge
	 */
	public Map createEdgeAttributes() {
		Map map = new Hashtable();
		// Add a Line End Attribute
		GraphConstants.setLineEnd(map, GraphConstants.ARROW_SIMPLE);
		// Add a label along edge attribute
		GraphConstants.setLabelAlongEdge(map, true);
		// Adds a parallel edge router
		GraphConstants.setLineStyle(map, GraphConstants.STYLE_SPLINE);
		GraphConstants.setFont(map, GraphConstants.DEFAULTFONT.deriveFont(10f));
		return map;
	}

}
