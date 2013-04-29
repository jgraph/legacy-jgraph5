/*
 * $Id: JGraphWordBrowser.java,v 1.1 2009-09-25 15:17:49 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved. 
 * 
 * This file is licensed under the JGraph software license, a copy of which
 * will have been provided to you in the file LICENSE at the root of your
 * installation directory. If you are unable to locate this file please
 * contact JGraph sales for another copy.
 */
package com.jgraph.layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;
import org.jgraph.plaf.basic.BasicGraphUI;

import com.jgraph.layout.graph.JGraphSimpleLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;
import com.jgraph.layout.organic.JGraphFastOrganicLayout;
import com.jgraph.layout.organic.JGraphOrganicLayout;

/**
 * A simple example to browse a word database.
 */
public class JGraphWordBrowser extends DefaultGraphModel {

	/**
	 * Internal mapping of user objects to cells.
	 */
	protected Map mapping = new Hashtable();

	/**
	 * Shortcut to {@link #addVertex(Object, String)} with an empty style.
	 */
	public Object addVertex(Object data) {
		return addVertex(data, null);
	}

	/**
	 * Adds a vertex to the model.
	 */
	public Object addVertex(Object data, String stylename) {
		if (data != null) {
			Object vertex = mapping.get(data);
			if (vertex == null) {
				vertex = createCell(data, stylename, false);
				insert(new Object[] { vertex }, null, null, null, null);
			}
		}
		return data;
	}

	/**
	 * Shortcut to {@link #addEdge(Object, Object, Object)} with an empty user
	 * object.
	 */
	public Object addEdge(Object source, Object target) {
		return addEdge(source, target, null);
	}

	/**
	 * Shortcut to {@link #addEdge(Object, Object, Object, String)} with an
	 * empty style.
	 */
	public Object addEdge(Object source, Object target, Object data) {
		return addEdge(source, target, data, null);
	}

	/**
	 * Shortcut to {@link #addEdge(Object, Object, Object, String, String)} with
	 * an empty vertex style.
	 */
	public Object addEdge(Object source, Object target, Object data,
			String edgeStylename) {
		return addEdge(source, target, data, edgeStylename, null);
	}

	/**
	 * Adds an edge between source and target to the model.
	 */
	public Object addEdge(Object source, Object target, Object data,
			String edgeStylename, String vertexStylename) {
		if (source != null && target != null) {
			Collection newCells = new ArrayList(3);

			// Creates the source
			Object sourceVertex = mapping.get(source);
			if (sourceVertex == null) {
				sourceVertex = createCell(source, vertexStylename, false);
				newCells.add(sourceVertex);
			}

			// Creates the neighbour
			Object targetVertex = mapping.get(target);
			if (targetVertex == null) {
				targetVertex = createCell(target, vertexStylename, false);
				newCells.add(targetVertex);
			}

			// Creates the edge to the neighbour
			Object edge = (data != null) ? mapping.get(data) : null;
			ConnectionSet cs = null;
			if (edge == null) {
				edge = createCell(data, edgeStylename, true);
				newCells.add(edge);
				if (getChildCount(sourceVertex) > 0
						&& getChildCount(targetVertex) > 0)
					cs = new ConnectionSet(edge, getDefaultPort(sourceVertex),
							getDefaultPort(targetVertex));
			}

			// Inserts the new cells into the graph model
			if (!newCells.isEmpty()) {
				insert(newCells.toArray(), null, cs, null, null);
			}
		}
		return data;
	}

	/**
	 * Uses the factory to create a vertex or edge for a specified stylename and
	 * registers the mapping.
	 */
	protected Object createCell(Object data, String stylename, boolean isEdge) {
		Object cell = (isEdge) ? createEdge(data) : createVertex(data);
		if (data != null) {
			mapping.put(data, cell);
		}

		// Stores the stylename in the attribute map
		if (stylename != null) {
			//Map attributes = getAttributes(cell);
			// JSPGraphConstants.setStyle(attributes, stylename);
		}

		return cell;
	}

	/**
	 * Hook for subclassers to create the vertex.
	 */
	public Object createVertex(Object data) {
		DefaultGraphCell vertex = new DefaultGraphCell(data);
		GraphConstants.setAutoSize(vertex.getAttributes(), true);
		GraphConstants.setInset(vertex.getAttributes(), 4);
		vertex.addPort();
		return vertex;
	}

	/**
	 * Hook for subclassers to create the vertex.
	 */
	public Object createEdge(Object data) {
		DefaultEdge edge = new DefaultEdge(data);
		int len = String.valueOf(data).length();
		int size = (len > 20) ? ((len > 40) ? 8 : 10) : 12;
		GraphConstants
				.setFont(edge.getAttributes(), new Font("Serif", 0, size));
		GraphConstants.setLabelAlongEdge(edge.getAttributes(), true);
		GraphConstants.setLineColor(edge.getAttributes(), Color.GRAY);
		// GraphConstants.setRouting(edge.getAttributes(),
		// JSPGraphParallelEdgeRouter.sharedInstance);
		return edge;
	}

	/**
	 * Removes a cell from the model.
	 */
	public Object remove(Object data) {
		Object vertex = mapping.remove(data);
		if (vertex != null) {

			// Includes all connections to/from the cell
			Set cells = DefaultGraphModel.getEdges(this,
					new Object[] { vertex });
			cells.add(vertex);

			// Removes the cells from the model
			remove(cells.toArray());
		}
		return vertex;
	}

	/**
	 * Returns the default port for the specified vertex.
	 */
	public Object getDefaultPort(Object vertex) {
		return getChild(vertex, 0);
	}

	/**
	 *
	 * 
	 */
	public void add(String word, String wordType) {
		if (word.endsWith(")")) {
			int index = word.indexOf(" (");
			wordType = word.substring(index + 1);
			word = word.substring(0, index);
		}
		try {
			List page = getContent(word);
			Iterator it = page.iterator();
			String type = "";
			Object vertex = null;
			while (it.hasNext()) {
				String line = (String) it.next();
				if (line.indexOf("<h3>Noun</h3>") >= 0) {
					type = "(N)";
					if (wordType.equals(type)) {
						vertex = addVertex(word + " " + type);
					} else {
						vertex = null;
					}
				} else if (line.indexOf("<h3>Adjective</h3>") >= 0) {
					type = "(A)";
					if (wordType.equals(type)) {
						vertex = addVertex(word + " " + type);
					} else {
						vertex = null;
					}
				} else if (line.indexOf("<h3>Verb</h3>") >= 0) {
					type = "(V)";
					if (wordType.equals(type)) {
						vertex = addVertex(word + " " + type);
					} else {
						vertex = null;
					}
				}
				if (vertex != null) {
					if (line.indexOf("<li>") >= 0) {
						List words = getWords(line);
						String edge = "";
						int index = line.lastIndexOf("(") + 1;
						if (index >= 0) {
							edge = line.substring(index, line.indexOf(")",
									index));
						}
						int counter = 1;
						Iterator it2 = words.iterator();
						while (it2.hasNext()) {
							String target = it2.next() + " " + type;
							if (!target.startsWith("S:")
									&& !target.equals(vertex)) {
								addEdge(vertex, target, edge + " (" + counter++
										+ ")");
								System.out.println(vertex + " -> " + edge
										+ " -> " + target);
							}
						}
					}
				}
			}
			sendEdgesToBack();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List getWords(String line) {
		List words = new ArrayList();
		int index = line.indexOf("<a href=\"");
		while (index > 0) {
			index = line.indexOf("\">", index) + 2;
			int end = line.indexOf("</a>", index);
			String word = line.substring(index, end);
			words.add(word);
			index = line.indexOf("<a href=\"", end + 4);
		}
		return words;
	}

	public List getContent(String word) throws IOException {
		URL url = new URL("http://wordnet.princeton.edu/perl/webwn");
		URLConnection urlConn = url.openConnection();
		urlConn.setDoInput(true);
		urlConn.setDoOutput(true);
		urlConn.setUseCaches(false);
		urlConn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		DataOutputStream printout = new DataOutputStream(urlConn
				.getOutputStream());
		String content = "s=" + URLEncoder.encode(word, "UTF-8")
				+ "&o2=&o0=1&o6=&o1=1&o5=&o4=&o3=&h=";
		printout.writeBytes(content);
		printout.flush();
		printout.close();
		BufferedReader ireader = new BufferedReader(new InputStreamReader(
				urlConn.getInputStream()));
		ArrayList webpage = new ArrayList(100);
		String line;
		while ((line = ireader.readLine()) != null) {
			webpage.add(line);
		}
		ireader.close();
		return webpage;
	}

	public static void layout(JGraph graph, boolean organic, boolean horizontal) {
		JGraphFacade facade = createFacade(graph);
		if (!organic) {
			JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
			if (horizontal) {
				layout.setOrientation(SwingConstants.WEST);
			}
			layout.setInterRankCellSpacing(50);
			layout.setIntraCellSpacing(60);
			try {
				layout.run(facade);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				organic = true;
			}
		}
		if (organic) {
			JGraphLayout layout = new JGraphSimpleLayout(
					JGraphSimpleLayout.TYPE_CIRCLE);
			layout.run(facade);
			JGraphFastOrganicLayout fastOrganicLayout = new JGraphFastOrganicLayout();
			fastOrganicLayout.setForceConstant(80);
			fastOrganicLayout.run(facade);
			JGraphOrganicLayout organicLayout = new JGraphOrganicLayout();
			organicLayout.setRadiusScaleFactor(0.9);
			organicLayout.setNodeDistributionCostFactor(8000000.0);
			organicLayout.setOptimizeBorderLine(false);
			organicLayout.setDeterministic(true);
			organicLayout.run(facade);
		}
		Map map = facade.createNestedMap(true, true);
		graph.getGraphLayoutCache().edit(map, null, null, null);
	}

	/**
	 * Sends all edges to the background. TODO: Create special graph model with
	 * keepEdgesInBack option.
	 */
	public void sendEdgesToBack() {
		Object[] cells = DefaultGraphModel.getAll(this);
		List edges = new ArrayList(cells.length);
		for (int i = 0; i < cells.length; i++) {
			if (isEdge(cells[i])) {
				edges.add(cells[i]);
			}
		}
		toBack(edges.toArray());
	}

	/**
	 * Creates a {@link JGraphFacade}.
	 * 
	 * @param graph
	 *            The graph to use for the facade.
	 * @return Returns a new facade for the specified graph.
	 */
	protected static JGraphFacade createFacade(JGraph graph) {
		JGraphFacade facade = new JGraphFacade(graph);
		facade.setIgnoresUnconnectedCells(true);
		facade.setIgnoresCellsInGroups(true);
		facade.setIgnoresHiddenCells(true);
		facade.setDirected(true);
		return facade;
	}

	public static void main(String[] args) {
		final boolean organic = true;
		VertexView.renderer = new SynonymRenderer();
		JGraphWordBrowser synModel = new JGraphWordBrowser();
		final JGraph graph = new JGraph(synModel) {
			public void updateUI() {
				setUI(new BasicGraphUI() {
					protected boolean startEditing(Object cell, MouseEvent e) {
						if (!getModel().isEdge(cell)) {
							Cursor previous = graph.getCursor();
							graph.setCursor(new Cursor(Cursor.WAIT_CURSOR));
							((JGraphWordBrowser) getModel()).add(String
									.valueOf(cell), null);
							graph.setSelectionCell(cell);
							JGraphWordBrowser.layout(graph, organic, true);
							scrollCellToVisible(cell);
							Rectangle2D bounds = getCellBounds(cell);
							if (getParent() instanceof JViewport) {
								Rectangle dim = ((JViewport) getParent())
										.getBounds();
								int w = (int) dim.getWidth();
								int h = (int) dim.getHeight();
								Rectangle rect = new Rectangle((int) (bounds
										.getX() - w / 2),
										(int) (bounds.getY() - h / 2), w, h);
								scrollRectToVisible(rect);
							} else {
								scrollCellToVisible(cell);
							}
							graph.setCursor(previous);
							return true;
						}
						return false;
					}
				});
				invalidate();
			}
		};
		graph.getGraphLayoutCache().setSelectsAllInsertedCells(false);
		graph.setEditClickCount(1);
		graph.setAntiAliased(true);
		JFrame frame = new JFrame("Graph Word Browser Example");
		frame.getContentPane().setLayout(new BorderLayout());
		final JTextField text = new JTextField(
				"Type a word and type and press [ENTER] Supported types are (V), (N) and (A)");
		frame.getContentPane().add(text, BorderLayout.NORTH);
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					JGraphWordBrowser synModel = new JGraphWordBrowser();
					graph.setModel(synModel);
					String type = (text.getText().indexOf("(") >= 0) ? null
							: "(V)";
					if (type != null) {
						text.setText(text.getText() + " (V)");
					}
					synModel.add(text.getText(), type); // graph
					layout(graph, organic, true);
				}
			}
		});
		frame.getContentPane().add(new JScrollPane(graph), BorderLayout.CENTER);
		frame.setSize(640, 480);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static class SynonymRenderer extends VertexRenderer {

		public void installAttributes(CellView cellView) {
			super.installAttributes(cellView);
			if (selected) {
				setForeground(Color.RED);
			}
		}

	}

}
