/*
 * $Id: JGraphGroupRenderer.java,v 1.1 2005-10-08 13:30:01 david Exp $
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.VertexRenderer;

/**
 * Renderer for grouped cells, includes '+' '-' to expand/collapse
 */
public class JGraphGroupRenderer extends VertexRenderer {

	/**
	 * Default handle bounds for renderer, '+' or '-'
	 */
	public static Rectangle handle = new Rectangle(0, 0, 7, 7);

	/**
	 * Specifies whether the current view is a rich text value, and if the image
	 * should be stretched.
	 */
	protected boolean isGroup = false;

	/**
	 * Holds the background and foreground of the graph.
	 */
	protected Color handleColor = Color.white, graphForeground = Color.black;

	/**
	 * Overrides the parent implementation to return the value component stored
	 * in the user object instead of this renderer if a value component exists.
	 * This applies some of the values installed to this renderer to the value
	 * component (border, opaque) if the latter is a JComponent.
	 * 
	 * @return Returns a configured renderer for the specified view.
	 */
	public Component getRendererComponent(JGraph graph, CellView view,
			boolean sel, boolean focus, boolean preview) {
		handleColor = graph.getHandleColor();
		graphForeground = graph.getForeground();
		isGroup = DefaultGraphModel.isGroup(graph.getModel(), view.getCell());
		return super.getRendererComponent(graph, view, sel, focus, preview);
	}

	/**
	 * renderer paint method
	 */
	public void paint(Graphics g) {
		super.paint(g);
		if (isGroup) {
			g.setColor(handleColor);
			g.fill3DRect(handle.x, handle.y, handle.width, handle.height, true);
			g.setColor(graphForeground);
			g.drawRect(handle.x, handle.y, handle.width, handle.height);
			g.drawLine(handle.x + 1, handle.y + handle.height / 2, handle.x
					+ handle.width - 2, handle.y + handle.height / 2);
			if (view.isLeaf())
				g.drawLine(handle.x + handle.width / 2, handle.y + 1, handle.x
						+ handle.width / 2, handle.y + handle.height - 2);
		}
	}

	/**
	 * Detect whether or not a point has hit the group/ungroup image
	 * 
	 * @param pt
	 *            the point to check
	 * @return whether or not the point lies within the handle
	 */
	public boolean inHitRegion(Point2D pt) {
		return handle.contains(pt.getX(), pt.getY());
	}

}
