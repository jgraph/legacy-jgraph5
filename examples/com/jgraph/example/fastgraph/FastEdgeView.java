/*
 * Copyright (c) 2005, David Benson
 *
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.fastgraph;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Map;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.Edge;
import org.jgraph.graph.EdgeRenderer;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

public class FastEdgeView extends EdgeView {

	public CellViewRenderer getRenderer() {
		return renderer;
	}

	/**
	 * @param cell
	 */
	public FastEdgeView(Object cell) {
		super(cell);
	}

	/**
	 * Hook from AbstractCellView, avoid creating attribute map until as late
	 * as possible
	 */
	protected AttributeMap createAttributeMap() {
		return AttributeMap.emptyAttributeMap;
	}


	/**
	 * If the model is a FastGraphModel check the undoDisabled flag. If set,
	 * copy the reference from the model attributes. This will cause the
	 * preview of a drag to edit in-place, but since undo is disabled this
	 * will only cause a performance improvement.
	 */
	protected AttributeMap getCellAttributes(GraphModel model) {
		if (model instanceof FastGraphModel) {
			if (((FastGraphModel)model).undoDisabled) {
				return model.getAttributes(cell);
			}
		}
		return (AttributeMap) model.getAttributes(cell).clone();
	}

	protected void mergeAttributes() {
	}

	public void setAttributes(AttributeMap attributes) {
		if (allAttributes == null)
			allAttributes = new AttributeMap(1, 1.0f);
		this.allAttributes = attributes;
	}
	
	public Map changeAttributes(GraphLayoutCache cache, Map change) {
		if (change != null) {
			Map undo = allAttributes.applyMap(change);
			update(cache);
			return undo;
		}
		return null;
	}

	/**
	 * Returns the local renderer. Do not access the renderer field directly.
	 * Use this method instead. Note: This method is package private.
	 */
	private EdgeRenderer getFastEdgeRenderer() {
		return (EdgeRenderer) getRenderer();
	}

	//
	// View Methods
	//

	/**
	 * Returns true if this view intersects the given rectangle.
	 */
	public boolean intersects(JGraph graph, Rectangle2D rect) {
		boolean intersects = false;
		Rectangle2D bounds = getBounds();
		if (bounds != null) {
			intersects = bounds.intersects(rect);
		}
		if (intersects) {
			Rectangle r = new Rectangle((int) rect.getX(), (int) rect.getY(),
					(int) rect.getWidth(), (int) rect.getHeight());
			return getFastEdgeRenderer().intersects(graph, this, r);
		}
		return false;
	}

	/**
	 * Update attributes and recurse children.
	 */
	public void update(GraphLayoutCache cache) {
		// Save the reference to the points so they can be changed
		// in-place by use of setPoint, setSource, setTarget methods.
		points = GraphConstants.getPoints(allAttributes);
		if (points == null) {
			points = new ArrayList(2);
			points.add(getAllAttributes().createPoint(10, 10));
			points.add(getAllAttributes().createPoint(20, 20));
			GraphConstants.setPoints(allAttributes, points);
		}
		checkDefaultLabelPosition();
		Edge.Routing routing = GraphConstants.getRouting(allAttributes);
		if (routing != null)
			routing.route(cache, this);
		// Clear cached shapes
		beginShape = null;
		endShape = null;
		lineShape = null;
		invalidateFastEdge();
	}

	private void invalidateFastEdge() {
		labelVector = null;
		sharedPath = null;
		cachedBounds = null;
	}
	
	/**
	 * Returns the location for this portview.
	 */
	public Rectangle2D getBounds() {
		Rectangle2D rect = null;
		if (cachedBounds == null) {
			String label = String.valueOf(getCell());
			if (label != null) {
				// If there is a label get the bounds from the renderer
				cachedBounds = getFastEdgeRenderer().getBounds(this);
			} else {
				// Otherwise make simpe calculation for bounds
				int n = points.size();
				double minX, minY, maxX, maxY;
				minX = maxX = getPoint(0).getX();
				minY = maxY = getPoint(0).getY();
				for (int i = 1; i < n; i++) {
					Point2D currentPoint = getPoint(i);
					double currentX = currentPoint.getX();
					double currentY = currentPoint.getY();
					
					if (currentX < minX) {
						minX = currentX;
					} else if (currentX > maxX) {
						maxX = currentX;
					}
					if (currentY < minY) {
						minY = currentY;
					} else if (currentY > maxY) {
						maxY = currentY;
					}
				}
				cachedBounds = new Rectangle2D.Double(minX, minY, maxX-minX+1, maxY-minY+1);
			}
		}
		rect = cachedBounds;
		return rect;
	}


}
