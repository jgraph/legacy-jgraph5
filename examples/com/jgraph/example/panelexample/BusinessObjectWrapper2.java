package com.jgraph.example.panelexample;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jgraph.graph.CellView;

/**
 * A very basic wrapper storing a reference to a custom user object with a
 * label.
 * 
 * @author rvalyi
 */
public class BusinessObjectWrapper2 {
	private String label = "";
	
	private CellView[] portviews;

	/**
	 * The wrapper where you put your real buisness object.
	 * (An other solution is that you business object
	 * implements a toString method and you deal with it
	 * in the editor)
	 */
	private DefaultMutableTreeNode value;

	public String getLabel() {
		return label;
	}

	public void setLabel(String stringValue) {
		this.label = stringValue;
	}

	public DefaultMutableTreeNode getValue() {
		return value;
	}

	public void setValue(DefaultMutableTreeNode value) {
		this.value = value;
	}

	/**
	 * Used by JGraph to render the cell label
	 */
	public String toString() {
		return label;
	}

	public CellView[] getPortviews() {
		return portviews;
	}

	public void setPortviews(CellView[] portviews) {
		this.portviews = portviews;
	}
}
