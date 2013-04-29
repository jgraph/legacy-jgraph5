package com.jgraph.example.groupeditor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jgraph.graph.VertexRenderer;

/**
 * A renderer that display an active rectangle that trigger the cell or group edition when clicked
 * @author rvalyi
 * @see com.jgraph.example.JGraphGroupRenderer
 */
public class EditableGroupRenderer extends VertexRenderer {

	public static Rectangle editorButton = new Rectangle(0, 0, 7, 7);

	/**
	 * renderer paint method
	 */
	public void paint(Graphics g) {
		super.paint(g);
		if (selected) {
			g.setColor(Color.GRAY);
			g.fillRect(getWidth() - editorButton.width -1 , 0, editorButton.width, editorButton.height);
			g.setColor(Color.RED);
			g.drawString("e", getWidth() - editorButton.width - 2, 7);
			//g.drawRect(getWidth() - editorButton.width -1, 0, editorButton.width, editorButton.height);
		}
	}
	
	public boolean isEditAsked(Point2D pt, Rectangle2D rectBounds) {
		//return editorButton.contains(pt.getX(), pt.getY());
		return editorButton.contains(editorButton.width + pt.getX() - rectBounds.getWidth(), pt.getY());
	}

}
