/*
 * $Id: JGraphLayoutMorphingManager.java,v 1.1 2009-09-25 15:17:49 david Exp $
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JViewport;
import javax.swing.Timer;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;

/**
 * Animation for simple graph changes (moves). This takes a nested map and
 * animates the change visually so the vertices appear to float to their new
 * locations. This implementation only takes into account new positions of
 * vertices, sizes, colors etc are changed after the animation in a single step.<br>
 * Note: This class is not thread-safe.
 */
public class JGraphLayoutMorphingManager implements ActionListener {

	/**
	 * Specifies the delay between morphing steps in milliseconds. Default is
	 * 30.
	 */
	protected int delay = 30;

	/**
	 * Specifies the number of animation steps. Default is 10.
	 */
	protected int steps = 10;

	/**
	 * References the graph to be morphed.
	 */
	protected JGraph graph;

	/**
	 * Holds the current morhing step.
	 */
	protected transient int step = 0;

	/**
	 * Holds the current and final bounds of the animation.
	 */
	protected transient Map oldBounds = new Hashtable(),
			newBounds = new Hashtable();

	/**
	 * Holds the context cells, eg the edges connected to the animated cells or
	 * one of their parents.
	 */
	protected transient CellView[] context;

	/**
	 * Holds the clipping shape to be used for repainting the graph.
	 */
	protected transient Shape clip;

	/**
	 * Holds the original nested map for the final execute step.
	 */
	protected transient Map nestedMap;

	/**
	 * Animates the graph so that all vertices move from their current location
	 * to the new location stored in the nested map. This sets the
	 * {@link #nestedMap} and {@link #graph} variable and spawns a timer
	 * process. While the timer is running, further method calls are ignored.
	 * The call will return immediately.
	 * 
	 * @param nestedMap
	 *            The nested map that defines the new locations.
	 */
	public synchronized void morph(JGraph graph, Map nestedMap) {
		if (this.graph == null && this.nestedMap == null && graph != null
				&& nestedMap != null) {
			this.graph = graph;
			//graph.setAntiAliased(false);
			this.nestedMap = nestedMap;
			initialize();
			// Execute the morphing. This spawns a timer
			// to not block the dispatcher thread (repaint).
			if (!newBounds.isEmpty()) {
				Timer timer = new Timer(delay, this);
				timer.start();
			} else {
				execute();
			}
		}
	}

	/**
	 * Hook for subclassers to determine whether the specified cell should be
	 * animated. This implementation returns true for all cells.
	 * 
	 * @param cell
	 *            The cells to be checked.
	 * @return Returns true if the cell may be animated.
	 */
	protected boolean isAnimatable(Object cell) {
		return true;
	}

	/**
	 * Initializes the datastructures required for the animation. This
	 * implementation sets the current and final location for the cells to be
	 * animated using the specified nestedMap to get the new locations. If a
	 * cell is in the nested map but {@link #isAnimatable(Object)} returns false
	 * then the cell is moved to it's final location in the first animation
	 * step.
	 */
	protected void initialize() {
		// Initialize the old (current) and new (final) bounds
		// hashtables if the bounds differ. For the non-animatable
		// cells this will temporily apply the new bounds.
		Iterator it = nestedMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object cell = entry.getKey();
			Rectangle2D rect = GraphConstants.getBounds((Map) entry.getValue());
			if (rect != null) {
				Rectangle2D old = graph.getCellBounds(cell);
				if (old != null && !old.equals(rect)) {
					oldBounds.put(cell, old.clone());
					if (!isAnimatable(cell))
						setCellBounds(cell, rect);
					else
						newBounds.put(cell, rect);
				}
			}
		}

		// Finds the set of parents to determine the clipping region
		// and context of the animation. To make sure the complete
		// graph is painted including the non-animatable cells
		// this uses the cells from the oldbounds map.
		HashSet parents = new HashSet();
		it = oldBounds.keySet().iterator();
		while (it.hasNext()) {
			Object cell = it.next();

			// Fetches all parents of the cell and adds them to the parent set
			Object parent = graph.getModel().getParent(cell);
			while (parent != null) {
				parents.add(parent);
				parent = graph.getModel().getParent(parent);
			}
		}
		parents.addAll(oldBounds.keySet());
		Object[] cells = parents.toArray();

		// Initializes the clipping region
		clip = graph.getCellBounds(cells);

		// Initializes the context of the animation. The context consists
		// of all cells connected to either a cell or one of its parents.
		Object[] edges = DefaultGraphModel.getEdges(graph.getModel(), cells)
				.toArray();
		context = graph.getGraphLayoutCache().getMapping(edges);
	}

	/**
	 * Invoked to perform an animation step and stop the timer if all animation
	 * steps have been performed.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void actionPerformed(ActionEvent event) {
		if (step >= steps) {
			Timer timer = (Timer) event.getSource();
			timer.stop();
			execute();
		} else {
			step++;
			Iterator it = newBounds.keySet().iterator();
			while (it.hasNext())
				updateCell(it.next());
			graph.getGraphLayoutCache().update(context);
			graph.getGraphics().setClip(clip);
			fitViewport(graph);
			graph.repaint();
		}
	}

	/**
	 * Executes the actual change on the graph layout cache. This implementation
	 * restored the bounds on the modified cells to their old values for correct
	 * undo of the change, then calls the graph layout cache's edit method with
	 * the original nested map and cleans up the datastructures. This implements
	 * the final step of the animation.
	 */
	protected void execute() {
		try {
			Iterator it = oldBounds.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				setCellBounds(entry.getKey(), (Rectangle2D) entry.getValue());
			}
			graph.getGraphLayoutCache().edit(nestedMap, null, null, null);
			fitViewport(graph);
			//graph.setAntiAliased(true);
		} finally {
			graph = null;
			nestedMap = null;
			oldBounds.clear();
			newBounds.clear();
			context = null;
			clip = null;
			step = 0;
		}
	}

	/**
	 * Updates the specified cell for {@link #step}. This implementation moves
	 * the cell by a single increment towards it's final location using
	 * {@link #setCellBounds(Object, Rectangle2D)} to update the cell's bounds.
	 * 
	 * @param cell
	 *            The cell to be updated.
	 */
	protected void updateCell(Object cell) {
		if (isAnimatable(cell)) {
			Rectangle2D old = (Rectangle2D) oldBounds.get(cell);
			Rectangle2D rect = (Rectangle2D) newBounds.get(cell);
			double dx = (rect.getX() - old.getX()) * step / steps;
			double dy = (rect.getY() - old.getY()) * step / steps;
			Rectangle2D pos = new Rectangle2D.Double(old.getX() + dx, old
					.getY()
					+ dy, old.getWidth(), old.getHeight());
			setCellBounds(cell, pos);
		}
	}

	/**
	 * Sets the bounds for the specified cell.
	 * 
	 * @param cell
	 *            The cell whose bounds to set.
	 * @param bounds
	 *            The new bounds of the cell.
	 */
	protected void setCellBounds(Object cell, Rectangle2D bounds) {
		Rectangle2D rect = graph.getCellBounds(cell);
		if (rect != null && bounds != null) {
			rect.setFrame(bounds.getX(), bounds.getY(), bounds.getWidth(),
					bounds.getHeight());
			CellView view = graph.getGraphLayoutCache().getMapping(cell, false);
			if (view != null)
				view.update(graph.getGraphLayoutCache());
		}
	}
	
	public static void fitViewport(JGraph graph) {
		int border = 5;
		Component parent = graph.getParent();
		if (parent instanceof JViewport) {
			Dimension size = ((JViewport) parent).getExtentSize();
			Rectangle2D p = graph.getCellBounds(graph.getRoots());
			if (p != null) {
				graph.setScale(Math.min((double) size.getWidth()
						/ (p.getX() + p.getWidth() + border),
						(double) size.getHeight()
								/ (p.getY() + p.getHeight() + border)));
			}
		}
	}

}
