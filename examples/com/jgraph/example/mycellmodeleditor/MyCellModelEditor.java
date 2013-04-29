package com.jgraph.example.mycellmodeleditor;

import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgraph.graph.AbstractCellView;

/**
 * Try to edit cells. Remark that the cell label is not necessarly coupled with
 * the graph cell user object.
 * Tip: use F2 to edit groups!
 * 
 * This example shows how one can set up a custom Swing component to in place
 * edit cells and also how the cell user object can be decorrellated from the
 * cell label.
 * 
 * @author rvalyi
 */
public class MyCellModelEditor {

	public static void main(String[] args) {
		// Switch off D3D because of Sun XOR painting bug
		// See http://www.jgraph.com/forum/viewtopic.php?t=4066
		System.setProperty("sun.java2d.d3d", "false");
		JFrame frame = new JFrame("MyCellModelEditor");
		JGraph graph = new JGraph();
		frame.getContentPane().add(graph);
		BusinessObjectEditor.editorInsideCell = false;
		AbstractCellView.cellEditor = new BusinessObjectEditor();

		frame.setSize(700, 500);
		frame.setVisible(true);
	}
}
