/*
 * @(#)GraphSelectionDemo.java 3.3 23-APR-04
 *
 * Copyright (c) 2001-2004, Gaudenz Alder All rights reserved.
 *
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jgraph.JGraph;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.GraphModel;

/**
 * With this example you'll learn how to listen to graph selection event,
 * program graph selections and navigate accross the graph groups.
 * 
 * This demo is is a bit like the GraphTreeModel demo. But this time, we
 * synchronize the graph and the tree selection with two listeners.
 * 
 * There are two issues actually. The first is that we should disable one
 * listener when we programatically change its selection after the other
 * listener receives a selection event (else we would fail in infinite loops).
 * The other issue is navigating the graph and especially its group in order to
 * get the equivalent tree selection. Also notice that ports should be handled
 * like a special kind of group children.
 * 
 * @author rvalyi
 */
public class GraphSelectionDemo extends DefaultTreeModel implements
		GraphModelListener {

	private static JTree tree;

	private static JGraph graph;

	private static GraphSelectionDemo gtModel;

	private static GraphModelTreeNode gtModelTreeNode;
	
	private static SyncGraphSelectionListener mySyncTreeSelectionListener = new SyncGraphSelectionListener();

	public static void main(String[] args) {
		// Switch off D3D because of Sun XOR painting bug
		// See http://www.jgraph.com/forum/viewtopic.php?t=4066
		System.setProperty("sun.java2d.d3d", "false");
		JFrame frame = new JFrame("GraphSelectionDemo");
		graph = new JGraph();
		gtModel = new GraphSelectionDemo(graph.getModel());
		gtModelTreeNode = new GraphModelTreeNode(graph.getModel());
		graph.getModel().addGraphModelListener(gtModel);
		tree = new JTree(gtModel);
		tree.setRootVisible(false);
		JScrollPane sGraph = new JScrollPane(graph);
		JScrollPane sTree = new JScrollPane(tree);
		tree.addTreeSelectionListener(new SyncTreeSelectionListener());
		graph.addGraphSelectionListener(mySyncTreeSelectionListener);
		graph.addMouseListener(new MyMouseListener());
		JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sTree,
				sGraph);
		frame.getContentPane().add(pane);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public GraphSelectionDemo(GraphModel model) {
		super(new GraphModelTreeNode(model));
	}

	public void graphChanged(GraphModelEvent e) {
		reload();
	}

	public static class SyncTreeSelectionListener implements
			TreeSelectionListener {

		public void valueChanged(TreeSelectionEvent e) {
			if (tree.isSelectionEmpty())//a selection in the graph can trigger that event while no tree selection exists yet
				return;
			TreePath[] treeSelection = tree.getSelectionModel()
					.getSelectionPaths();
			Object[] graphSelection = new Object[treeSelection.length];

			for (int i = 0; i < treeSelection.length; i++) {
				graphSelection[i] = treeSelection[i].getLastPathComponent();
			}

			// now we set the corresponding graph selection
			graph.setSelectionCells(graphSelection);

			/*
			 * System.out .print(tree.getSelectionModel().getSelectionPath() +
			 * "\n");
			 */
		}
	}
	
	/**
	 * Prevent from loosing the synchronisation after a graph element is dragged
	 */
	public static class MyMouseListener extends MouseInputAdapter {
        public void mouseReleased(MouseEvent e) {
            if (e.getSource() instanceof JGraph) {
                mySyncTreeSelectionListener.valueChanged(null);
            }
        }
    }

	public static class SyncGraphSelectionListener implements
			GraphSelectionListener {
		public void valueChanged(GraphSelectionEvent e) {
			Object selectedPort = null;
			Object[] selection = graph.getSelectionModel().getSelectionCells();
			if (selection == null)
				return;
			TreePath[] paths = new TreePath[selection.length];
			for (int i = 0; i < selection.length; i++) {

				ArrayList list = new ArrayList();
				Object selected = selection[i];
				if (selected == null)
					return;
				list.add(tree.getModel().getRoot());
				if (graph.getModel().isPort(selected)) {// shift if a port
					// is
					// selected
					selectedPort = selected;
					selected = graph.getModel().getParent(selected);
				}

				list = computeTreePathSelection(list, graph.getModel()
						.getParent(selected), selected);
				if (selectedPort != null)
					list.add(selectedPort);
				TreePath treePath = new TreePath(list.toArray());

				if (treePath != null) {
					paths[i] = treePath;
					// System.out.print( treePath.toString() + "\n");
				}

			}

			// to emphasis the change, we collapse the whole tree before
			// assigning a new selection
			int treeSize = tree.getRowCount();
			int row = 0;
			while (row < treeSize) {
				tree.collapseRow(row);
				row++;
			}

			// now we set the corresponding tree selection
			tree.setSelectionPaths(paths);
		}

		/**
		 * Recursively adds the selected graph cells to the array list, starting
		 * from the current child and going until the graph root.
		 * 
		 * @param currentList
		 * @param parent
		 * @param child
		 * @return collection of selected graph cells
		 */
		public ArrayList computeTreePathSelection(ArrayList currentList,
				Object parent, Object child) {
			if (parent == null) {
				currentList.add(child);
				return currentList;
			}

			int parentIndex = graph.getModel().getIndexOfRoot(parent);

			if (parentIndex < 0) {// then parent is in a group or is a port
				computeTreePathSelection(currentList, graph.getModel()
						.getParent(parent), parent);
			}

			// parentIndex refer to a valid selected node in the tree
			currentList.add(gtModelTreeNode.getChildAt(parentIndex));

			// index of the child in the graph
			int childIndex = graph.getModel().getIndexOfChild(parent, child);
			// corresponding node in the tree
			Object node = tree.getModel().getChild(parent, childIndex);

			currentList.add(node);
			return currentList;
		}
	}

	/**
	 * See the GraphModelTree demo
	 */
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