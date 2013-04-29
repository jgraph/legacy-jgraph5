/*
 * $Id: JGraphGraphFactory.java,v 1.12 2006-01-03 11:11:41 david Exp $
 * Copyright (c) 2001-2006, Gaudenz Alder
 * Copyright (c) 2005-2006, David Benson
 *
 * All rights reserved.
 *
 * This file is licensed under the JGraph software license, a copy of which
 * will have been provided to you in the file LICENSE at the root of your
 * installation directory. If you are unable to locate this file please
 * contact JGraph sales for another copy.
 */
package com.jgraph.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.ParentMap;
import org.jgraph.graph.Port;

/**
 * A helper class that creates graphs. Currently supports tree graphs and a
 * random graph where all edges are connected at least once
 */
public class JGraphGraphFactory {

	public static final int FULLY_CONNECTED = 0;

	public static final int RANDOM_CONNECTED = 1;

	public static final int TREE = 2;

	public static final int FLOW = 3;

	/**
	 * Shared <code>Random</code>
	 */
	private Random random = new Random();

	/**
	 * Number of vertices on current tree level being worked on
	 */
	private int numVerticesLevel;

	/**
	 * Stores first unconnected edge available
	 */
	private int edgeIndex;

	/**
	 * Stores first unconnected cell available in smaple tree, root cell never
	 * is available
	 */
	private int vertexIndex;

	/**
	 * Whether or not insert at performed directly on the model
	 */
	private boolean insertIntoModel = false;

	/**
	 * Number of nodes for use as bean variable
	 */
	protected int numNodes = 36;

	/**
	 * Number of edges for use as bean variable
	 */
	protected int numEdges = 36;

	/**
	 * The maximum number of child nodes any parent in the tree graph can have
	 */
	protected int maxNodesPerTreeLevel = 2;

	protected FactoryConfigDialog dialog;

	/**
	 * Default constructor
	 */
	public JGraphGraphFactory() {
	}

	/**
	 * Entry method for inserting a sample graph
	 * 
	 * @param graph
	 *            the JGraph to perform the insert on
	 * @param graphType
	 *            which sample graph type is to be inserted
	 * @param defaultVertexAttributes
	 *            the default attributes to use for vertices
	 * @param defaultEdgeAttributes
	 *            the default attributes to use for edges
	 */
	public void insertGraph(JGraph graph, int graphType,
			Map defaultVertexAttributes, Map defaultEdgeAttributes) {
		if (dialog == null) {
			dialog = new FactoryConfigDialog();
		}
		dialog.configureLayout(graph, graphType, defaultVertexAttributes,
				defaultEdgeAttributes);
		dialog.setModal(true);
		center(dialog);
		dialog.setVisible(true);
	}

	/**
	 * clears the graph and inserts a random tree. The nodes are initially
	 * placed a grid with the root node selected. The algorithm used is not
	 * recursive as the number of nodes per level are not know at the size. A
	 * DFS search would not work, since we don't know where the leaves are.
	 * Cells are inserted over edges for clarity
	 * 
	 * @param graph
	 *            the JGraph to perform the insert on
	 * @param defaultVertexAttributes
	 *            the default attributes to use for vertices
	 * @param defaultEdgeAttributes
	 *            the default attributes to use for edges
	 * @return the root node of the tree
	 */
	public Object insertTreeSampleData(JGraph graph, Map defaultVertexAttributes,
			Map defaultEdgeAttributes) {
		// Create array big enough for all cells
		Object[] cells = new Object[numNodes * 2];
		initialise(graph);
		numVerticesLevel = 1;
		edgeIndex = 0;
		vertexIndex = 1;
		int gridWidth = (int) Math.sqrt(numNodes);

		// Ensure arrows are present in edge attributes
		int arrow = GraphConstants.ARROW_CLASSIC;
		GraphConstants.setLineEnd(defaultEdgeAttributes, arrow);
		GraphConstants.setEndFill(defaultEdgeAttributes, true);

		// the cell occupy the first half of the cells array, i.e. from
		// 0 to numNodes-1, the edge the second half, from numNodes to
		// numNode*2-1
		for (int i = 0; i < numNodes; i++) {
			Point2D cellPosition = calcCellPosition(i, gridWidth);
			DefaultGraphCell cell = createVertex(new Integer(i).toString(),
					cellPosition, defaultVertexAttributes);
			cells[i] = cell;
		}

		connectNextLevel(graph.getModel(), cells, defaultEdgeAttributes);
		Object[] cells2 = new Object[numNodes + numNodes - 1];
		System.arraycopy(cells, numNodes, cells2, 0, numNodes - 1);
		System.arraycopy(cells, 0, cells2, numNodes - 1, numNodes);
		insertIntoGraph(graph, cells2);

		// Select the root cell
		graph.setSelectionCell(cells[0]);
		return cells[0];
	}

	/**
	 * clears the graph and inserts a random tree. The nodes are initially
	 * placed a grid with the root node selected. The algorithm used is not
	 * recursive as the number of nodes per level are not know at the size. A
	 * DFS search would not work, since we don't know where the leaves are.
	 * Cells are inserted over edges for clarity
	 * 
	 * @param model
	 *            the model to perform the insert on
	 * @param defaultVertexAttributes
	 *            the default attributes to use for vertices
	 * @param defaultEdgeAttributes
	 *            the default attributes to use for edges
	 * @return the root node of the tree
	 */
	public Object insertTreeSampleData(GraphModel model, Map defaultVertexAttributes,
			Map defaultEdgeAttributes) {
		// Create array big enough for all cells
		Object[] cells = new Object[numNodes * 2];
		// Clear out the model
		Object[] roots = DefaultGraphModel.getRoots(model);
		Object[] descendants = DefaultGraphModel.getDescendants(model, roots).toArray();
		model.remove(descendants);
		
		numVerticesLevel = 1;
		edgeIndex = 0;
		vertexIndex = 1;
		int gridWidth = (int) Math.sqrt(numNodes);

		// Ensure arrows are present in edge attributes
		int arrow = GraphConstants.ARROW_CLASSIC;
		GraphConstants.setLineEnd(defaultEdgeAttributes, arrow);
		GraphConstants.setEndFill(defaultEdgeAttributes, true);

		// the cell occupy the first half of the cells array, i.e. from
		// 0 to numNodes-1, the edge the second half, from numNodes to
		// numNode*2-1
		for (int i = 0; i < numNodes; i++) {
			Point2D cellPosition = calcCellPosition(i, gridWidth);
			DefaultGraphCell cell = createVertex(new Integer(i).toString(),
					cellPosition, defaultVertexAttributes);
			cells[i] = cell;
		}

		connectNextLevel(model, cells, defaultEdgeAttributes);
		Object[] cells2 = new Object[numNodes + numNodes - 1];
		System.arraycopy(cells, numNodes, cells2, 0, numNodes - 1);
		System.arraycopy(cells, 0, cells2, numNodes - 1, numNodes);
		JGraphGraphFactory.insert(model, cells2);
		return cells[0];
	}

	/**
	 * Takes all cells to be connected between one level and the next creates
	 * 
	 * @param cells
	 * @param defaultEdgeAttributes
	 */
	protected void connectNextLevel(GraphModel model, Object[] cells, Map defaultEdgeAttributes) {
		// If we've connected all vertices stop connecting
		if (vertexIndex < numNodes) {
			// Store the number of vertices on this level locally as the
			// variable is going to be reused
			int localNumVerticesLevel = numVerticesLevel;
			numVerticesLevel = 0;
			int localVertexCount = vertexIndex;
			// For each node in this level connect a random number of vertices
			for (int i = localVertexCount - localNumVerticesLevel; i < localVertexCount; i++) {
				connectChildrenVertices(model, cells, cells[i], defaultEdgeAttributes);
			}
			// Recurse
			connectNextLevel(model, cells, defaultEdgeAttributes);
		}
	}

	/**
	 * Connects the next <code>numChildren</code> free vertices as targets
	 * from the specified <code>parent</code>
	 * 
	 * @param cells
	 * @param parent
	 * @param defaultEdgeAttributes
	 */
	protected void connectChildrenVertices(GraphModel model, Object[] cells, Object parent,
			Map defaultEdgeAttributes) {
		// If we've connected all vertices stop connecting
		if (vertexIndex < numNodes) {
			// Make a list of child cells first. We don't recurse straight down
			// to leaves since we don't how deep the tree is. Instead, each
			// level is connected at a time. Increasing maxNodesPerTreeLevel
			// makes the tree wider and shallower, decreasing makes it deeper
			// and narrower
			int numChildren = random.nextInt(maxNodesPerTreeLevel) + 1;
			Port parentPort;
			if (parent instanceof Port) {
				parentPort = (Port)parent;
			} else {
				parentPort = (Port)model.getChild(parent, 0);
			}
					
			for (int i = 0; i < numChildren; i++) {
				// If we've connected all vertices stop connecting
				if (vertexIndex < numNodes) {
					numVerticesLevel++;
					// Port of child i
					Port childPort;
					if (cells[vertexIndex] instanceof Port) {
						childPort = (Port)cells[vertexIndex++];
					} else {
						childPort = (Port)model.getChild(cells[vertexIndex++], 0);
					}

					Edge edge = createEdge(defaultEdgeAttributes, parentPort,
							childPort);
					cells[(edgeIndex++) + numNodes] = edge;
				}
			}
		}
	}

	/**
	 * clears the graph and inserts a random graph. The nodes are initially
	 * placed a grid with no node selected. If there are at least as many edges
	 * as nodes then all cells have at least one edge connected to them.
	 * 
	 * @param graph
	 *            the JGraph instance to act upon
	 * @param defaultVertexAttributes
	 *            the default attributes to use for vertices
	 * @param defaultEdgeAttributes
	 *            the default attributes to use for edges
	 */
	public void insertConnectedGraphSampleData(JGraph graph,
			Map defaultVertexAttributes, Map defaultEdgeAttributes) {
		// Create array big enough for all cells
		Object[] cells = new DefaultGraphCell[numNodes + numEdges];
		GraphModel model = graph.getModel();
		initialise(graph);

		int gridWidth = (int) Math.sqrt(numNodes);
		for (int i = 0; i < numNodes; i++) {
			Point2D cellPosition = calcCellPosition(i, gridWidth);
			DefaultGraphCell cell = createVertex(new Integer(i).toString(),
					cellPosition, defaultVertexAttributes);
			cells[i] = cell;
		}

		// Connect every cell in turn to a random other
		for (int i = 0; i < Math.min(numNodes, numEdges); i++) {
			// Port of child i
			Port sourcePort;
			if (cells[i] instanceof Port) {
				sourcePort = (Port)cells[i];
			} else {
				sourcePort = (Port)model.getChild(cells[i], 0);
			}
			// Select random other cell
			int node = random.nextInt(numNodes);

			if (numNodes > 1) {
				while (node == i) {
					node = random.nextInt(numNodes);
				}
			}

			Port targetPort;
			if (cells[node] instanceof Port) {
				targetPort = (Port)cells[node];
			} else {
				targetPort = (Port)model.getChild(cells[node], 0);
			}

			Edge edge = createEdge(defaultEdgeAttributes, sourcePort,
					targetPort);
			cells[i + numNodes] = edge;
		}

		// Connect remaining edges randomly
		for (int i = numNodes; i < numEdges; i++) {
			int sourceNode = random.nextInt(numNodes);
			Port sourcePort;
			if (cells[sourceNode] instanceof Port) {
				sourcePort = (Port)cells[sourceNode];
			} else {
				sourcePort = (Port)model.getChild(cells[sourceNode], 0);
			}
			// Select random other cell
			int targetNode = random.nextInt(numNodes);

			if (numNodes > 1) {
				while (targetNode == sourceNode) {
					targetNode = random.nextInt(numNodes);
				}
			}

			Port targetPort;
			if (cells[targetNode] instanceof Port) {
				targetPort = (Port)cells[targetNode];
			} else {
				targetPort = (Port)model.getChild(cells[targetNode], 0);
			}

			Edge edge = createEdge(defaultEdgeAttributes, sourcePort,
					targetPort);
			cells[i + numNodes] = edge;
		}
		Object[] cells2 = new Object[numNodes + numEdges];
		System.arraycopy(cells, numNodes, cells2, 0, numEdges);
		System.arraycopy(cells, 0, cells2, numEdges, numNodes);
		insertIntoGraph(graph, cells2);
	}

	/**
	 * clears the graph and inserts a fully connected graph. The nodes are
	 * initially placed a grid. There are the same number of cells and edges in
	 * the graph, all cells have at least one edge connected to them.
	 * 
	 * @param graph
	 *            the JGraph instance to act upon
	 * @param defaultVertexAttributes
	 *            the default attributes to use for vertices
	 * @param defaultEdgeAttributes
	 *            the default attributes to use for edges
	 */
	public void insertFullyConnectedGraphSampleData(JGraph graph,
			Map defaultVertexAttributes, Map defaultEdgeAttributes) {
		GraphModel model = graph.getModel();
		// Calculate the number of edges
		int numEdges = ((numNodes - 1) * (numNodes)) / 2;
		// Create array big enough for all cells
		Object[] cells = new DefaultGraphCell[numNodes + numEdges];
		initialise(graph);

		int gridWidth = (int) Math.sqrt(numNodes);
		for (int i = 0; i < numNodes; i++) {
			Point2D cellPosition = calcCellPosition(i, gridWidth);
			DefaultGraphCell cell = createVertex(new Integer(i).toString(),
					cellPosition, defaultVertexAttributes);
			cells[numEdges + i] = cell;
		}

		int cellCount = 0;
		// Connect every cell to each other
		for (int i = 0; i < numNodes; i++) {
			// Port of child i
			Port sourcePort;
			if (cells[numEdges + i] instanceof Port) {
				sourcePort = (Port)cells[numEdges + i];
			} else {
				sourcePort = (Port)model.getChild(cells[numEdges + i], 0);
			}

			for (int j = i + 1; j < numNodes; j++) {
				Port targetPort;
				if (cells[numEdges + j] instanceof Port) {
					targetPort = (Port)cells[numEdges + j];
				} else {
					targetPort = (Port)model.getChild(cells[numEdges + j], 0);
				}

				Edge edge = createEdge(defaultEdgeAttributes, sourcePort,
						targetPort);
				cells[cellCount++] = edge;
			}
		}

		insertIntoGraph(graph, cells);
	}

	/**
	 * clears the graph and inserts a fully connected graph. The nodes are
	 * initially placed a grid. There are the same number of cells and edges in
	 * the graph, all cells have at least one edge connected to them.
	 * 
	 * @param graph
	 *            the JGraph instance to act upon
	 * @param defaultVertexAttributes
	 *            the default attributes to use for vertices
	 * @param defaultEdgeAttributes
	 *            the default attributes to use for edges
	 */
	public void insertSampleFlowGraph(JGraph graph,
			Map defaultVertexAttributes, Map defaultEdgeAttributes) {
	}

	/**
	 * Returns a point on a square grid given the index into the total number of
	 * cells and the width of one line of the grid
	 * 
	 * @param i
	 *            index of cell
	 * @param gridWidth
	 *            width of each grid line
	 * @return the position of the cell
	 */
	private Point2D calcCellPosition(int i, int gridWidth) {
		if (i != 0) {
			return new Point2D.Double(20 + (60 * (i % gridWidth)),
					20 + (40 * (i / gridWidth)));
		} else {
			return new Point2D.Double(20, 20);
		}
	}

	/**
	 * Method hook to create custom vertices
	 * 
	 * @param userObject
	 *            the user object to pass to the cell
	 * @return the new vertex instance
	 */
	protected DefaultGraphCell createVertex(Object userObject,
			Point2D position, Map defaultVertexAttributes) {
		AttributeMap attributes = new AttributeMap(defaultVertexAttributes);
		GraphConstants.setBounds(attributes, new Rectangle2D.Double(position
				.getX(), position.getY(), 40, 20));
		DefaultGraphCell cell = new DefaultGraphCell(userObject, attributes);
		// Add a Port
		cell.addPort();
		return cell;
	}

	/**
	 * Method hook to create custom edges
	 * 
	 * @return the new vertex instance
	 */
	protected Edge createEdge(Map defaultEdgeAttributes,
			Port sourcePort, Port targetPort) {
		AttributeMap edgeAttrib = null;
		if (defaultEdgeAttributes != null) {
			edgeAttrib = new AttributeMap(defaultEdgeAttributes);
		} else {
			edgeAttrib = new AttributeMap(6);
		}
		Edge edge = new DefaultEdge(null, edgeAttrib);

		edge.setSource(sourcePort);
		edge.setTarget(targetPort);

		return edge;
	}

	/**
	 * Common initialization functionality
	 * 
	 */
	protected void initialise(JGraph graph) {
		// Remove all previous cells
		graph.getModel().remove(graph.getDescendants(graph.getRoots()));
	}

	/**
	 * Common insert functionality
	 */
	protected void insertIntoGraph(JGraph graph, Object[] cells) {
		// For performance, don't select inserted cells
		boolean selectsAll = graph.getGraphLayoutCache()
				.isSelectsAllInsertedCells();
		boolean selectsLocal = graph.getGraphLayoutCache()
				.isSelectsLocalInsertedCells();
		graph.getGraphLayoutCache().setSelectsAllInsertedCells(false);
		graph.getGraphLayoutCache().setSelectsLocalInsertedCells(false);

		if (insertIntoModel) {
			graph.getModel().insert(cells, null, null, null, null);
		} else {
			graph.getGraphLayoutCache().insert(cells);
		}

		graph.getGraphLayoutCache().setSelectsAllInsertedCells(selectsAll);
		graph.getGraphLayoutCache().setSelectsLocalInsertedCells(selectsLocal);
	}
	
	/**
	 * Inserts the specified cells into the graph model. This method is a
	 * general implementation of cell insertion. If the source and target port
	 * are null, then no connection set is created. The method uses the
	 * attributes from the specified edge and the egdge's children to construct
	 * the insert call. This example shows how to insert an edge with a special
	 * arrow between two known vertices:
	 * 
	 * <pre>
	 * Object source = graph.getDefaultPortForCell(sourceVertex).getCell();
	 * Object target = graph.getDefaultPortForCell(targetVertex).getCell();
	 * DefaultEdge edge = new DefaultEdge(&quot;Hello, world!&quot;);
	 * edge.setSource(source);
	 * edge.setTarget(target);
	 * Map attrs = edge.getAttributes();
	 * GraphConstants.setLineEnd(attrs, GraphConstants.ARROW_TECHNICAL);
	 * graph.getGraphLayoutCache().insert(edge);
	 * </pre>
	 */
	public static void insert(GraphModel model, Object[] cells) {
		insert(model, cells, new Hashtable(), new ConnectionSet(), new ParentMap());
	}

	/**
	 * Variant of the insert method that allows to pass a default connection set
	 * and parent map and nested map.
	 */
	public static void insert(GraphModel model, Object[] cells, Map nested, ConnectionSet cs,
			ParentMap pm) {
		if (cells != null) {
			if (nested == null)
				nested = new Hashtable();
			if (cs == null)
				cs = new ConnectionSet();
			if (pm == null)
				pm = new ParentMap();
			for (int i = 0; i < cells.length; i++) {
				// Using the children of the vertex we construct the parent map.
				int childCount = model.getChildCount(cells[i]);
				for (int j = 0; j < childCount; j++) {
					Object child = model.getChild(cells[i], j);
					pm.addEntry(child, cells[i]);

					// And add their attributes to the nested map
					AttributeMap attrs = model.getAttributes(child);
					if (attrs != null)
						nested.put(child, attrs);
				}

				// A nested map with the vertex as key
				// and its attributes as the value
				// is required for the model.
				Map attrsTmp = (Map) nested.get(cells[i]);
				Map attrs = model.getAttributes(cells[i]);
				if (attrsTmp != null)
					attrs.putAll(attrsTmp);
				nested.put(cells[i], attrs);

				// Check if we have parameters for a connection set.
				Object sourcePort = model.getSource(cells[i]);
				if (sourcePort != null)
					cs.connect(cells[i], sourcePort, true);

				Object targetPort = model.getTarget(cells[i]);
				if (targetPort != null)
					cs.connect(cells[i], targetPort, false);
			}
			// Create an array with the parent and its children.
			cells = DefaultGraphModel.getDescendants(model, cells)
					.toArray();

			// Finally call the insert method on the parent class.
			model.insert(cells, nested, cs, pm, null);
		}
	}


	/**
	 * @return Returns the insertIntoModel.
	 */
	public boolean isInsertIntoModel() {
		return insertIntoModel;
	}

	/**
	 * @param insertIntoModel
	 *            The insertIntoModel to set.
	 */
	public void setInsertIntoModel(boolean insertIntoModel) {
		this.insertIntoModel = insertIntoModel;
	}

	/**
	 * @return Returns the numEdges.
	 */
	public int getNumEdges() {
		return numEdges;
	}

	/**
	 * @param numEdges
	 *            The numEdges to set.
	 */
	public void setNumEdges(int numEdges) {
		if (numEdges < 1) {
			numEdges = 1;
		} else if (numEdges > 2000000) {
			numEdges = 2000000;
		}
		this.numEdges = numEdges;
	}

	/**
	 * @return Returns the numNodes.
	 */
	public int getNumNodes() {
		return numNodes;
	}

	/**
	 * @param numNodes
	 *            The numNodes to set.
	 */
	public void setNumNodes(int numNodes) {
		if (numNodes < 1) {
			numNodes = 1;
		} else if (numNodes > 2000000) {
			numNodes = 2000000;
		}
		this.numNodes = numNodes;
	}

	/**
	 * @return Returns the maxNodesPerTreeLevel.
	 */
	public int getMaxNodesPerTreeLevel() {
		return maxNodesPerTreeLevel;
	}

	/**
	 * @param maxNodesPerTreeLevel
	 *            The maxNodesPerTreeLevel to set.
	 */
	public void setMaxNodesPerTreeLevel(int maxNodesPerTreeLevel) {
		this.maxNodesPerTreeLevel = maxNodesPerTreeLevel;
	}

	public static void center(Window wnd) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = wnd.getSize();
		wnd.setLocation(screenSize.width / 2 - (frameSize.width / 2),
				screenSize.height / 2 - (frameSize.height / 2));
	}

	/**
	 * Simple Dialog that configures how many nodes and edges the graph factory
	 * is to create
	 */
	public class FactoryConfigDialog extends JDialog {
		protected boolean insertGraph = false;

		protected JGraph graph;

		protected int graphType;

		protected Map defaultVertexAttributes;

		protected Map defaultEdgeAttributes;

		protected JTextField maxTreeNodeChildren = new JTextField();

		protected JTextField numNodes = new JTextField();

		protected JTextField numEdges = new JTextField();

		protected JCheckBox insertIntoModel = new JCheckBox();

		public FactoryConfigDialog() {
			super((Frame) null, "Configure Sample Graph", true);

			JPanel panel = new JPanel(new GridLayout(4, 2, 4, 4));
			panel.add(new JLabel("Max Child Nodes in Tree"));
			panel.add(maxTreeNodeChildren);
			panel.add(new JLabel("Number of nodes"));
			panel.add(numNodes);
			panel.add(new JLabel("Number of edges"));
			panel.add(numEdges);
			panel.add(new JLabel("Insert into model"));
			panel.add(insertIntoModel);

			JPanel panelBorder = new JPanel();
			panelBorder.setBorder(new EmptyBorder(10, 10, 10, 10));
			panelBorder.add(panel);

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
					.createMatteBorder(1, 0, 0, 0, Color.GRAY), BorderFactory
					.createEmptyBorder(16, 8, 8, 8)));

			JButton applyButton = new JButton("Insert");
			JButton closeButton = new JButton("Cancel");
			buttonPanel.add(closeButton);
			buttonPanel.add(applyButton);
			getRootPane().setDefaultButton(applyButton);

			applyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					applyValues();
					if (graphType == TREE) {
						insertTreeSampleData(graph, defaultVertexAttributes,
								defaultEdgeAttributes);
					} else if (graphType == RANDOM_CONNECTED) {
						insertConnectedGraphSampleData(graph,
								defaultVertexAttributes, defaultEdgeAttributes);
					} else if (graphType == FULLY_CONNECTED) {
						insertFullyConnectedGraphSampleData(graph,
								defaultVertexAttributes, defaultEdgeAttributes);
					} else if (graphType == FLOW) {
						insertSampleFlowGraph(graph, defaultVertexAttributes,
								defaultEdgeAttributes);
					}
					setVisible(false);
				}
			});
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertGraph = false;
					setVisible(false);
				}
			});

			getContentPane().add(panelBorder, BorderLayout.CENTER);
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			pack();
			setResizable(false);
			// setLocationRelativeTo(parent);
		}

		public void configureLayout(JGraph graph, int graphType,
				Map defaultVertexAttributes, Map defaultEdgeAttributes) {
			this.graph = graph;
			this.graphType = graphType;
			this.defaultVertexAttributes = defaultVertexAttributes;
			this.defaultEdgeAttributes = defaultEdgeAttributes;

			maxTreeNodeChildren.setText(String
					.valueOf(getMaxNodesPerTreeLevel()));
			this.numNodes.setText(String.valueOf(getNumNodes()));
			this.numEdges.setText(String.valueOf(getNumEdges()));
			this.insertIntoModel.setSelected(isInsertIntoModel());
		}

		protected void applyValues() {
			setMaxNodesPerTreeLevel(Integer.parseInt(maxTreeNodeChildren
					.getText()));
			setNumNodes(Integer.parseInt(this.numNodes.getText()));
			setNumEdges(Integer.parseInt(this.numEdges.getText()));
			setInsertIntoModel(this.insertIntoModel.isSelected());
		}
	}
}
