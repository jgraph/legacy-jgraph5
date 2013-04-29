/*
 * $Id: JGraphExampleLayoutCache.java,v 1.3 2010-12-01 17:03:26 david Exp $
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.undo.UndoableEdit;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewFactory;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.ParentMap;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * A class that encapsulates autolayout functionality for manual changes. This
 * layout cache overrides the edit method to add layout results for the given
 * change which are then passed to the superclass' edit method. This way a
 * manual change and layout result is merged into one call on the model, thus
 * resulting in correct undo and redo in the command history. <br>
 * To morph into the new geometry this cache needs a reference to the hosting
 * graph, which it calls to perform the animation. After morphing, which is
 * asynchronous in order to not block the UI dispatcher thread, the graph
 * invokes edit on the cache again. The {@link #autolayout}-switch is used
 * avoid infinite recursion for this second call.
 */
public class JGraphExampleLayoutCache extends GraphLayoutCache {

	/**
	 * Reference to the layout dialog which is used to configure the current
	 * layout.
	 */
	protected transient JDialog configDialog = null;

	/**
	 * Properties panel used inside the layout configuration dialog.
	 */
	protected transient PropertySheetPanel propertySheet = new PropertySheetPanel();

	/**
	 * Reference to the hosting graph which is in charge of animating changes to
	 * the geometry (morphing).
	 */
	protected transient JGraphExampleGraph graph;

	/**
	 * Switches to create the facade and to fetch the result from the layout.
	 */
	protected boolean ignoreChildren = true, ignoreHidden = true,
			ignoreUnconnected = true, layoutDirectedGraph = true,
			layoutIgnoreGrid = true, layoutFlushOrigin = true,
			layoutMoveSelection = true, morphing = true;

	/**
	 * Specifies if all manual changes should trigger an autolayout.
	 */
	protected transient boolean autolayout = false;

	/**
	 * Specifies the current layout algorithm.
	 */
	protected transient JGraphLayout layout;

	public JGraphExampleLayoutCache() {
		// empty c'tor
	}
	
	/**
	 * Constructs a new example layout cache for the specified example graph. In
	 * contrast to the default implementation, where these two are not related,
	 * here, they are closely coupled as one is used to be notified of changes
	 * BEFORE they are applied to the model (override edit method), whereas the
	 * other is used to animate these changes (requires JComponent).
	 * 
	 * @param graph the graph this layout cache is to associated with
	 */
	public JGraphExampleLayoutCache(JGraphExampleGraph graph) {
		super(graph.getModel(), new DefaultCellViewFactory(), true);
		setHidesExistingConnections(false);
		this.graph = graph;
	}
	
	/**
	 * Setting the graph after loading from file.
	 */
	public void setGraph(JGraphExampleGraph graph) {
		this.graph = graph;
	}

	/**
	 * Required for XML persistence
	 * 
	 * @param model
	 *            the model that constitues the data source
	 */
	public JGraphExampleLayoutCache(GraphModel model, CellViewFactory factory,
			CellView[] cellViews, CellView[] hiddenCellViews, boolean partial) {
		 super(model, factory, cellViews, hiddenCellViews, partial);
	}
	
	/**
	 * Overrides the superclass' method to invoke autolayout.
	 */
	public void edit(Map attributes, ConnectionSet cs, ParentMap pm,
			UndoableEdit[] e) {
		if (!autolayout || (graph != null && !graph.isEnabled()) || cs != null || pm != null) {
			if (graph == null) {
				// Loading a persisted graph will be missed the graph
				// instance
				graph = new JGraphExampleGraph(this.getModel(), this);
			}
			graph.setEnabled(true);
			super.edit(attributes, cs, pm, e);
		} else {
			layout(attributes);
		}
	}

	/**
	 * Layouts the graph using the current {@link #layout}. The specified map
	 * constitutes a previous (manual) change that should be reflected by the
	 * facade. The result of the layout is subsequently merged with the map and
	 * passed to the morpher only if the layout provided additional cell
	 * locations.
	 * <p>
	 * This may run asynchronously, eg. the method may return when the layout is
	 * still running. The graph will remain disabled until the layout terminates
	 * or it stopped by the user, but the user interface will not be blocked
	 * while the layout is running. Instead, a progress dialog will inform the
	 * user of the current progress.
	 * 
	 * @param nestedMap
	 *            the nested map constituting a manual change
	 */
	public void layout(final Map nestedMap) {
		if (graph.isEnabled()) {
			graph.setEnabled(false);

			final JGraphFacade facade;
			Object[] roots = graph.getSelectionCells();
			facade = new JGraphMoveSelectionFacade(graph, roots);
			facade.setBounds(nestedMap);
			facade.resetControlPoints();

			if (layout instanceof JGraphLayout.Stoppable) {
				final ProgressMonitor progressMonitor = new ProgressMonitor(
						graph, "Performing layout...", "", 0, 100);
				progressMonitor.setMillisToDecideToPopup(100);
				progressMonitor.setMillisToPopup(500);

				// Update the max progress and install the update listener
				final JGraphLayoutProgress progress = ((JGraphLayout.Stoppable) layout)
						.getProgress();

				// Listen to progress updates
				final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals(
								JGraphLayoutProgress.PROGRESS_PROPERTY)) {
							int newValue = Integer.parseInt(String.valueOf(evt
									.getNewValue()));
							progressMonitor.setProgress(newValue);
						} else if (evt.getPropertyName().equals(
								JGraphLayoutProgress.MAXIMUM_PROPERTY)) {
							int newValue = Integer.parseInt(String.valueOf(evt
									.getNewValue()));
							progressMonitor.setMaximum(newValue);
						}

						// Checks isCancelled as the progress
						// monitor has no listener
						if (progressMonitor.isCanceled())
							progress.setStopped(true);
					}
				};

				Thread layoutThread = new Thread() {
					public void run() {
						progress
								.addPropertyChangeListener(propertyChangeListener);
						// long t1 = System.currentTimeMillis();
						try {
							facade.run(layout, false);
						} catch(Exception e) {
						    e.printStackTrace();
						}
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								progress
										.removePropertyChangeListener(propertyChangeListener);

								// After the layout terminates...
								boolean ignoreResult = progressMonitor
										.isCanceled();
								progressMonitor.close();

								// If not canceled process result
								if (!ignoreResult)
									processResult(facade, nestedMap);

								// Else just repeat the original call
								// which will also release the lock
								else {
									edit(nestedMap);
								}
							}
						});
					}
				};
				// While the layout runs...
				layoutThread.start();
			} else {

				// Immediately runs and processes the result
				facade.run(layout, false);
				processResult(facade, nestedMap);
			}
		}
	}

	/**
	 * Processes the result of the layout.
	 */
	protected void processResult(JGraphFacade facade, Map nestedMap) {
		boolean flushToOrigin = false;
		if (layoutFlushOrigin) {
			flushToOrigin = true;
		}
		Map layoutResult = facade.createNestedMap(layoutIgnoreGrid, flushToOrigin);

		// Remove changed bounds and points from the attributes
		if ((layoutFlushOrigin || layoutMoveSelection) && nestedMap != null) {
			layoutResult = GraphConstants.merge(layoutResult, new Hashtable(
					nestedMap));
		} else {
			layoutResult = GraphConstants.merge(nestedMap, layoutResult);
		}

		// Morph into new positions only if additional cells found in layout
		int manualCount = 0;
		if (nestedMap != null)
			manualCount = facade.getVertices(nestedMap.keySet(), false).size();

		if (morphing && layoutResult != null
				&& (nestedMap == null || manualCount <= layoutResult.size())) {
			graph.morph(layoutResult, (nestedMap != null) ? nestedMap.keySet()
					: null);
		} else {
			edit(layoutResult, null, null, null);
		}
	}

	public Rectangle2D getBounds(Object cell, Map nested) {
		Map attrs = (nested != null) ? (Map) nested.get(cell) : null;
		Rectangle2D tmp = null;
		if (attrs != null)
			tmp = GraphConstants.getBounds(attrs);
		if (tmp == null)
			tmp = graph.getCellBounds(cell);
		return tmp;
	}
	
	/**
	 * Required for XML persistence
	 * @return whether or not the cache is partial
	 */
	public boolean getPartial() {
		return isPartial();
	}

	/**
	 * Displays the properties of the current {@link #layout}in dialog.
	 */
	public void configureLayout() {
		if (layout != null) {
			try {
				if (configDialog == null) {
					configDialog = new JDialog(Frame.getFrames()[0],
							"Layout Properties");
					propertySheet.setSortingProperties(true);
					propertySheet.setBorder(null);
					configDialog.getContentPane().setLayout(new BorderLayout());
					configDialog.getContentPane().add(propertySheet,
							BorderLayout.CENTER);
					JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					panel.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(1, 0, 0, 0,
									Color.GRAY), BorderFactory
									.createEmptyBorder(16, 8, 8, 8)));
					JButton applyButton = new JButton("Apply");
					JButton closeButton = new JButton("Close");
					panel.add(closeButton);
					panel.add(applyButton);
					applyButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							propertySheet.writeToObject(layout);
							layout(null);
						}
					});
					closeButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							configDialog.setVisible(false);
						}
					});
					configDialog.getContentPane()
							.add(panel, BorderLayout.SOUTH);
					configDialog.pack();
				}
				BeanInfo info = Introspector.getBeanInfo(layout.getClass());
				propertySheet.setBeanInfo(info);
				propertySheet.readFromObject(layout);
				configDialog.setVisible(true);
			} catch (IntrospectionException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * An example of how to override the graph facade to control the cells that
	 * are moveable by graph layouts. This example takes into account the move
	 * selection option and makes sure selected cells may (not) be moved.
	 */
	public class JGraphMoveSelectionFacade extends JGraphFacade {

		/**
		 * @param graph
		 */
		public JGraphMoveSelectionFacade(JGraph graph, Object[] roots) {
			super(graph, roots);
			setIgnoresUnconnectedCells(ignoreUnconnected);
			setIgnoresCellsInGroups(ignoreChildren);
			setIgnoresHiddenCells(ignoreHidden);
			setDirected(layoutDirectedGraph);
		}

		/**
		 * Returns true if the move Selection cells is on and the cell is not
		 * selected.
		 */
		public boolean isMoveable(Object cell) {
			return (layoutMoveSelection || !graph.isCellSelected(cell))
					&& super.isMoveable(cell);
		}

	}

	/**
	 * Serialization support.
	 */
	private void readObject(ObjectInputStream s) throws IOException,
			ClassNotFoundException {
		s.defaultReadObject();
		propertySheet = new PropertySheetPanel();
	}

}
