/*
 * $Id: JGraphFoldingManager.java,v 1.2 2006-05-11 17:11:25 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved. 
 * 
 * This file is licensed under the JGraph software license, a copy of which
 * will have been provided to you in the file LICENSE at the root of your
 * installation directory. If you are unable to locate this file please
 * contact JGraph sales for another copy.
 */
package com.jgraph.example;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.event.MouseInputAdapter;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;

/**
 * Mananges the folding and unfolding of groups
 */
public class JGraphFoldingManager extends MouseInputAdapter {

	/**
	 * Called when the mouse button is released to see if a collapse or expand
	 * request has been made
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.getSource() instanceof JGraph) {
			final JGraph graph = (JGraph) e.getSource();
			CellView view = getGroupByFoldingHandle(graph, e.getPoint());
			if (view != null) {
				if (view.isLeaf())
					graph.getGraphLayoutCache().expand(
							new Object[] { view.getCell() });
				else
					graph.getGraphLayoutCache().collapse(
							new Object[] { view.getCell() });
			}
			e.consume();
		}
	}

	/**
	 * Called when the mouse button is released to see if a collapse or expand
	 * request has been made
	 */
	public static CellView getGroupByFoldingHandle(JGraph graph, Point2D pt) {
		CellView[] views = graph.getGraphLayoutCache().getCellViews();
		for (int i = 0; i < views.length; i++) {
			Point2D containerPoint = graph.fromScreen((Point2D) pt.clone());
			if (views[i].getBounds().contains(containerPoint.getX(), containerPoint.getY())) {
				Rectangle2D rectBounds = views[i].getBounds();
				containerPoint.setLocation(containerPoint.getX()
						- rectBounds.getX(), containerPoint.getY()
						- rectBounds.getY());
				Component renderer = views[i].getRendererComponent(graph,
						false, false, false);
				if (renderer instanceof JGraphGroupRenderer
						&& DefaultGraphModel.isGroup(graph.getModel(), views[i]
								.getCell())) {
					JGraphGroupRenderer group = (JGraphGroupRenderer) renderer;
					if (group.inHitRegion(containerPoint)) {
						return views[i];
					}
				}
			}
		}
		return null;
	}

}
