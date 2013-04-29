/*
 * @(#)GraphTreeModel.java 3.3 23-APR-04
 * 
 * Copyright (c) 2001-2004, Gaudenz Alder All rights reserved.
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.jgraph.JGraph;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.graph.GraphModel;

/**
 * JGraph gives you the opportunity to define whatever graph model you want.
 * Since trees are only special cases of graph, we will here show a fairly simple
 * new graph model: a tree model.
 * 
 * So we build a new type inheriting most of its behavior
 * from DefaultTreeModel and adding the minimal requirements to be a GraphModel also.
 * To achieve this, we hold a GraphModel reference in GraphModelTreeNode which implements
 * TreeModel and we use GraphModelTreeNode as the TreeModel to construct the DefaultTreeModel.
 *
 * Finally, don't believe graph models are limited to trees. Rather the GraphModel
 * interface provides you lots of hooks to detail your model. For instance you
 * could define custome implemetations to decide wether or not some
 * connections are allowed...
 */
public class GraphTreeModel
	extends DefaultTreeModel
	implements GraphModelListener {

	public static void main(String[] args) {
		JFrame frame = new JFrame("GraphTreeModel");
		JGraph graph = new JGraph();
		GraphTreeModel gtModel = new GraphTreeModel(graph.getModel());
		JTree tree = new JTree(gtModel);
		graph.getModel().addGraphModelListener(gtModel);
		tree.setRootVisible(false);
		JScrollPane sGraph = new JScrollPane(graph);
		JScrollPane sTree = new JScrollPane(tree);
		JSplitPane pane =
			new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sTree, sGraph);
		frame.getContentPane().add(pane);
		frame.pack();
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * Look how we wrapp the GraphModel into a GraphModelTreeNode to construct
	 * the GraphTreeModel and satisfy both the GraphModel and the TreeModel interfaces.
	 * @param model
	 */
	public GraphTreeModel(GraphModel model) {
		super(new GraphModelTreeNode(model));
	}

	public void graphChanged(GraphModelEvent e) {
		reload();
	}

	public static class GraphModelTreeNode implements TreeNode {

		protected GraphModel model;

		public GraphModelTreeNode(GraphModel model) {
			this.model = model;
		}

		public Enumeration children() {
			Vector v = new Vector();
			for (int i = 0; i < model.getRootCount(); i++)
				v.add(model.getRootAt(i));
			return v.elements();
		}

		public boolean getAllowsChildren() {
			return true;
		}

		public TreeNode getChildAt(int childIndex) {
			return (TreeNode) model.getRootAt(childIndex);
		}

		public int getChildCount() {
			return model.getRootCount();
		}

		public int getIndex(TreeNode node) {
			return model.getIndexOfRoot(node);
		}

		public TreeNode getParent() {
			return null;
		}

		public boolean isLeaf() {
			return false;
		}

		public String toString() {
			return model.toString();
		}

	}

}
