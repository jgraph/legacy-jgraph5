/*
 * Copyright (c) 2005, David Benson
 *
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.fastgraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jgraph.graph.CellView;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.plaf.basic.BasicGraphUI;

public class FastGraphUI extends BasicGraphUI {
	/**
	 * Paints the renderer of <code>view</code> to <code>g</code> at
	 * <code>bounds</code>. Recursive implementation that paints the children
	 * first.
	 * <p>
	 * The reciever should NOT modify <code>clipBounds</code>, or
	 * <code>insets</code>. The <code>preview</code> flag is passed to the
	 * renderer, and is not used here.
	 */
	public void paintCell(Graphics g, CellView view, Rectangle2D bounds,
			boolean preview) {
		if (view != null && view instanceof FastEdgeView) {
			// First Paint View
			if (bounds != null) {
				boolean selected = graph.isCellSelected(view.getCell());
				Graphics2D g2 = (Graphics2D) g;
				Stroke stroke = g2.getStroke();
				Object rendererHint = g2
						.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);

				g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
						RenderingHints.VALUE_STROKE_PURE);
				int c = BasicStroke.CAP_BUTT;
				int j = BasicStroke.JOIN_MITER;
				float lineWidth = 1.0f;
				g2.setStroke(new BasicStroke(lineWidth, c, j));
				// TODO
				g2.setColor(Color.BLACK);//(getForeground());
				int n = ((EdgeView)view).getPointCount();
				Point2D lastPoint = null;
				for (int i = 0; i < n; i++) {
					Point2D currentPoint = ((EdgeView) view).getPoint(i);
					if (i != 0) {
						g2.drawLine((int) lastPoint.getX(), (int) lastPoint.getY(),
								(int) currentPoint.getX(),
								(int) currentPoint.getY());
					}
					lastPoint = currentPoint;
				}
				if (selected) { // Paint Selected
					g2.setStroke(GraphConstants.SELECTION_STROKE);
					g2.setColor(graph.getHighlightColor());
					for (int i = 0; i < n; i++) {
						Point2D currentPoint = ((EdgeView) view).getPoint(i);
						if (i != 0) {
							g2.drawLine((int) lastPoint.getX(), (int) lastPoint.getY(),
									(int) currentPoint.getX(),
									(int) currentPoint.getY());
						}
						lastPoint = currentPoint;
					}
				}
				g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, rendererHint);
				g2.setStroke(stroke);
			}
		} else {
			super.paintCell(g, view, bounds, preview);
		}
	}

	/**
	 * Updates the <code>preferredSize</code> instance variable, which is
	 * returned from <code>getPreferredSize()</code>. Ignores edges for
	 * performance
	 */
	protected void updateCachedPreferredSize() {
		CellView[] views = graphLayoutCache.getRoots();
		Rectangle2D size = null;
		if (views != null && views.length > 0) {
			for (int i = 0; i < views.length; i++) {
				if (views[i] != null && !(views[i] instanceof FastEdgeView)) {
					Rectangle2D r = views[i].getBounds();
					if (r != null) {
						if (size == null)
							size = new Rectangle2D.Double(r.getX(), r.getY(), r
									.getWidth(), r.getHeight());
						else
							Rectangle2D.union(size, r, size);
					}
				}
			}
		}
		if (size == null)
			size = new Rectangle2D.Double();
		Point2D psize = new Point2D.Double(size.getX() + size.getWidth(), size
				.getY()
				+ size.getHeight());
		Dimension d = graph.getMinimumSize();
		Point2D min = (d != null) ? graph
				.toScreen(new Point(d.width, d.height)) : new Point(0, 0);
		Point2D scaled = graph.toScreen(psize);
		preferredSize = new Dimension(
				(int) Math.max(min.getX(), scaled.getX()), (int) Math.max(min
						.getY(), scaled.getY()));
		Insets in = graph.getInsets();
		if (in != null) {
			preferredSize.setSize(
					preferredSize.getWidth() + in.left + in.right,
					preferredSize.getHeight() + in.top + in.bottom);
		}
		validCachedPreferredSize = true;
	}
}