/*
 * $Id: JGraphExampleGraph.java,v 1.1 2009-09-25 15:17:49 david Exp $
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

import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Timer;

import org.jgraph.example.GraphEd.MyGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

/**
 * A graph that can animate changes (morph).
 */
public class JGraphExampleGraph extends MyGraph {

	/**
	 * Specifies the delay between morphing steps
	 */
	protected int delay = 30;

	/**
	 * Specified the number of steps in the morphing process
	 */
	protected int steps = 10;

	/**
	 * Specified the current morhing step
	 */
	protected int step = 0;

	/**
	 * Stores the previous bounds of morphed cells
	 */
	protected Map oldBounds = new Hashtable();

	/**
	 * Stores the future bounds of morphed cells
	 */
	protected Map newBounds = new Hashtable();

	/**
	 * Stores the previous collective bounds of the morphed cells
	 */
	protected Rectangle2D oldClipBounds;
	
	/**
	 * Stores the new collective bounds of the morphed cells
	 */
	protected Rectangle2D newClipBounds;

	/**
	 * Constructs an example graph for the specified graph model.
	 * 
	 * @param model
	 */
	public JGraphExampleGraph(GraphModel model) {
		super(model);
		setGraphLayoutCache(new JGraphExampleLayoutCache(this));
	}

	public JGraphExampleGraph(GraphModel model, GraphLayoutCache cache) {
		super(model, cache);
	}

	public void morph(final Map nestedMap, Set nomorph) {
		Set parents = initMorphing(nestedMap, nomorph);
		if (!newBounds.isEmpty()) {
			final Object[] cells = parents.toArray();
			Object[] edges = DefaultGraphModel.getEdges(getModel(), cells)
					.toArray();
			final CellView[] edgeViews = getGraphLayoutCache()
					.getMapping(edges);

			// Execute the morphing. This spawns a timer
			// to not block the dispatcher thread (repaint)
			Timer timer = new Timer(delay, new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					if (step >= steps) {
						Timer timer = (Timer) e.getSource();
						timer.stop();
						restore();
						getGraphLayoutCache().edit(nestedMap, null, null, null);
					} else {
						step++;
						Iterator it = newBounds.keySet().iterator();
						while (it.hasNext()) {
							morphCell(it.next(), step);
						}
						getGraphLayoutCache().refresh(edgeViews, false);
						addOffscreenDirty(oldClipBounds);
						addOffscreenDirty(newClipBounds);
						oldClipBounds = newClipBounds = null;
						repaint();
					}
				}
			});
			timer.start();
		} else {
			getGraphLayoutCache().edit(nestedMap, null, null, null);
		}
	}

	/**
	 * Initial step of the morphing process. Analyses the arguments and prepares
	 * internal datastructures for the morphing.
	 * 
	 * @return Returns the set of all cells and ancestors to determine the dirty
	 *         region and connected edges.
	 */
	protected Set initMorphing(Map nestedMap, Set nomorph) {
		oldBounds.clear();
		newBounds.clear();
		step = 0;
		Iterator it = nestedMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object cell = entry.getKey();
			Map attrs = (Map) entry.getValue();
			Rectangle2D rect = GraphConstants.getBounds(attrs);
			if (rect != null) {
				Rectangle2D old = getCellBounds(cell);
				if (old != null && !old.equals(rect)) {
					newBounds.put(cell, rect);
					oldBounds.put(cell, old.clone());
				}
			}
		}

		// Make sure the cells in nomorph are at their future
		// locations and fetches the set of all parents.
		HashSet parents = new HashSet();
		it = oldBounds.keySet().iterator();
		while (it.hasNext()) {
			Object cell = it.next();
			Object parent = getModel().getParent(cell);
			if (nomorph != null && nomorph.contains(cell)) {
				Rectangle2D rect = (Rectangle2D) newBounds.remove(cell);
				setCellBounds(cell, rect);
			}
			while (parent != null) {
				parents.add(parent);
				parent = getModel().getParent(parent);
			}
		}
		parents.addAll(oldBounds.keySet());
		return parents;
	}

	/**
	 * Restore the old bounds values for all cells. (This is required at the end
	 * of the morphing animation and before calling the edit method for the
	 * command history to work correctly.)
	 * 
	 */
	protected void restore() {
		Iterator it = oldBounds.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			setCellBounds(entry.getKey(), (Rectangle2D) entry.getValue());
		}
	}

	/**
	 * Performs the morph positionon a cell for a particular step
	 * 
	 * @param cell
	 *            the cell being morphed
	 * @param step
	 *            the number step into morph process
	 */
	protected void morphCell(Object cell, int step) {
		Rectangle2D old = (Rectangle2D) oldBounds.get(cell);
		Rectangle2D rect = (Rectangle2D) newBounds.get(cell);
		// Add to total clip bounds
		if (old != null) {
			if (oldClipBounds == null) {
				oldClipBounds = (Rectangle2D)old.clone();
			} else {
				oldClipBounds.add(old);
			}
		}
		if (rect != null) {
			if (newClipBounds == null) {
				newClipBounds = (Rectangle2D)rect.clone();
			} else {
				newClipBounds.add(rect);
			}
		}		double dx = (rect.getX() - old.getX()) * step / steps;
		double dy = (rect.getY() - old.getY()) * step / steps;
		Rectangle2D pos = new Rectangle2D.Double(old.getX() + dx, old.getY()
				+ dy, old.getWidth(), old.getHeight());
		setCellBounds(cell, pos);
	}

	/**
	 * Set the new cell bounds
	 * 
	 * @param cell
	 *            the cell whose bounds to set
	 * @param bounds
	 *            the new bounds of the cell
	 */
	protected void setCellBounds(Object cell, Rectangle2D bounds) {
		Rectangle2D rect = getCellBounds(cell);
		if (rect != null && bounds != null) {
			rect.setFrame(bounds.getX(), bounds.getY(), bounds.getWidth(),
					bounds.getHeight());
			CellView view = getGraphLayoutCache().getMapping(cell, false);
			if (view != null)
				view.update(getGraphLayoutCache());
		}
	}

}
