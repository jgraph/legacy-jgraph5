package com.jgraph.example.mycellview;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;


/**
 * This example is an evolution of the hello world example. This time,
 * we demonstrate the use of a custom graph cell view factory able to
 * provide different view indirections for various cells. Then all is about
 * using Java2D features in the view renderer to get draw the shape you want.
 * 
 * @author rvalyi
 */
public class MyCellView {

	public static void main(String[] args) {
		// Switch off D3D because of Sun XOR painting bug
		// See http://www.jgraph.com/forum/viewtopic.php?t=4066
		System.setProperty("sun.java2d.d3d", "false");

		// Construct Model and Graph
		GraphModel model = new DefaultGraphModel();
		JGraph graph = new JGraph(model);
		
		graph.getGraphLayoutCache().setFactory(new GPCellViewFactory());

		// Control-drag should clone selection
		graph.setCloneable(true);

		// Enable edit without final RETURN keystroke
		graph.setInvokesStopCellEditing(true);

		// When over a cell, jump to its default port (we only have one, anyway)
		graph.setJumpToDefaultPort(true);

		// Insert all three cells in one call, so we need an array to store them
		DefaultGraphCell[] cells = new DefaultGraphCell[7];

		cells[0] = createVertex(20, 20, 60, 30, Color.BLUE, false, new DefaultGraphCell("hello"), "com.jgraph.example.mycellview.JGraphEllipseView");
		
		cells[1] = createVertex(140, 25, 40, 20, null, false, new DefaultGraphCell("brave"), null);
		
		cells[2] = createVertex(20, 145, 40, 20, null, true, new DefaultGraphCell("new"), null);

		cells[3] = createVertex(140, 140, 60, 30, Color.ORANGE, false, new DefaultGraphCell("world"), "com.jgraph.example.mycellview.RoundRectView");

		// Create Edges
		DefaultEdge edge0 = new DefaultEdge();
		edge0.setSource(cells[0].getChildAt(0));
		edge0.setTarget(cells[1].getChildAt(0));
		cells[4] = edge0;
		GraphConstants.setLineEnd(edge0.getAttributes(), GraphConstants.ARROW_CIRCLE);
		GraphConstants.setEndFill(edge0.getAttributes(), true);
		
		DefaultEdge edge1 = new DefaultEdge();
		edge1.setSource(cells[1].getChildAt(0));
		edge1.setTarget(cells[2].getChildAt(0));
		cells[5] = edge1;
		GraphConstants.setLineEnd(edge1.getAttributes(), GraphConstants.ARROW_CLASSIC);
		GraphConstants.setEndFill(edge1.getAttributes(), true);
		
		DefaultEdge edge2 = new DefaultEdge();
		edge2.setSource(cells[2].getChildAt(0));
		edge2.setTarget(cells[3].getChildAt(0));
		cells[6] = edge2;
		GraphConstants.setLineEnd(edge2.getAttributes(), GraphConstants.ARROW_DIAMOND);
		GraphConstants.setEndFill(edge2.getAttributes(), true);

		// Insert the cells via the cache, so they get selected
		graph.getGraphLayoutCache().insert(cells);

		// Show in Frame
		JFrame frame = new JFrame();
		frame.getContentPane().add(new JScrollPane(graph));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		graph.setSelectionCell(edge0);//deselecting the cells emphasis makes it easier to see their shape
	}

	public static DefaultGraphCell createVertex(double x,
			double y, double w, double h, Color bg, boolean raised, DefaultGraphCell cell, String viewClass) {
		
		// set the view class (indirection for the renderer and the editor)
		if (viewClass != null)
			GPCellViewFactory.setViewClass(cell.getAttributes(), viewClass);

		// Set bounds
		GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(
				x, y, w, h));

		// Set fill color
		if (bg != null) {
			GraphConstants.setGradientColor(cell.getAttributes(), bg);
			GraphConstants.setOpaque(cell.getAttributes(), true);
		}

		// Set raised border
		if (raised)
			GraphConstants.setBorder(cell.getAttributes(), BorderFactory
					.createRaisedBevelBorder());
		else
			// Set black border
			GraphConstants.setBorderColor(cell.getAttributes(), Color.black);

		// Add a Floating Port
		cell.addPort();

		return cell;
	}

}