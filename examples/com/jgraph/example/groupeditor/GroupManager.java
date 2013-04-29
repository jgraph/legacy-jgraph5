package com.jgraph.example.groupeditor;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.event.MouseInputAdapter;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;

/**
 * Mananges the edition of the groups
 * @see com.jgraph.example.JGraphFoldingManager 
 */
public class GroupManager extends MouseInputAdapter {
	
	/**
	 * Called when the mouse button is released to see if a collapse or expand
	 * request has been made
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.getSource() instanceof JGraph) {
			final JGraph graph = (JGraph) e.getSource();
			getGroupByFoldingHandle(graph, e.getPoint());
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
			if (views[i].getBounds().contains(pt.getX(), pt.getY())) {
				Rectangle2D rectBounds = views[i].getBounds();
				Point2D containerPoint = graph.fromScreen((Point2D) pt.clone());
				containerPoint.setLocation(containerPoint.getX()
						- rectBounds.getX(), containerPoint.getY()
						- rectBounds.getY());
				Component renderer = views[i].getRendererComponent(graph,
						false, false, false);
				if (renderer instanceof EditableGroupRenderer) {
					Object[] list = {views[i].getCell()};
					graph.getGraphLayoutCache().toFront(list);
					graph.getGraphLayoutCache().refresh(views[i], false); 
					EditableGroupRenderer group = (EditableGroupRenderer) renderer;
					if (group.isEditAsked(containerPoint, rectBounds)) {
						graph.startEditingAtCell(views[i].getCell());
					}
				}
			}
		}
		return null;
	}

}

