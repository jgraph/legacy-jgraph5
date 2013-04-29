/*
 * $Id: JGraphSQLQueryPane.java,v 1.2 2007-08-18 10:20:11 david Exp $
 * 
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.adapter;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jgraph.JGraph;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.ParentMap;

/**
 * A quick and dirty implementation of a query pane. This allows to enter
 * keywords to search for property values of entities. Results may then be
 * dragged from the result list to the diagram. If the cells are already in the
 * diagram then they should be moved and selected, otherwise a new cell should
 * be created and inserted into the graph model.
 */
public class JGraphSQLQueryPane extends JPanel {

	public JGraphSQLQueryPane(final JGraphAdapterExample adapter,
			final JGraph graph) {
		super(new BorderLayout());
		final JGraphSQLBusinessModel businessModel = (JGraphSQLBusinessModel) graph
		.getModel();
		final JTextField input = new JTextField("[Type Query + Press Enter]");
		add(input, BorderLayout.NORTH);
		final DefaultTreeModel treeModel = new DefaultTreeModel(
				new DefaultMutableTreeNode("root"));
		final JTree tree = new JTree(treeModel);
		tree.setRootVisible(false);
		tree.setAutoscrolls(true);
		tree.setShowsRootHandles(true);
		tree.setEditable(false);
		add(new JScrollPane(tree), BorderLayout.CENTER);

		// Update the result set on ENTER
		input.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String query = input.getText();
					if (businessModel != null) {
						// TODO: Move into a method
						try {
							if (query.startsWith(":")) {
								query = query.substring(1);
								((JGraphSQLBackend) businessModel.getBackend())
								.query(query);
							} else if (query.startsWith(">")) {
								query = query.substring(1);
								((JGraphSQLBackend) businessModel.getBackend())
								.update(query);
							} else {
								DefaultMutableTreeNode root = new DefaultMutableTreeNode(
								"root");
								treeModel.setRoot(root);
								Iterator it = businessModel.findVertices(query,
										null).iterator();
								while (it.hasNext()) {
									Object source = it.next();
									DefaultMutableTreeNode node = new DefaultMutableTreeNode(
											source);
									treeModel.insertNodeInto(node, root,
											treeModel.getChildCount(root));
									Iterator it2 = businessModel.findEdges("",
											null, source, null, false)
											.iterator();
									while (it2.hasNext()) {

										// Create the tree node for the edge
										treeModel.insertNodeInto(
												new DefaultMutableTreeNode(it2
														.next()), node,
														treeModel.getChildCount(node));
									}
								}
								tree.expandPath(new TreePath(root));
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}

					} else {
						println("No backend found.");
					}
				}
			}
		});

		// React to double clicks
		tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (tree.isSelectionEmpty())
					return;

				// Result objects are wrapped in treenodes
				DefaultGraphCell cell = (DefaultGraphCell) businessModel
				.getValue(tree.getSelectionPath()
						.getLastPathComponent());
				if (cell != null) {
					if (e.getClickCount() == 2) {
						if (graph != null
								&& graph.getModel() instanceof JGraphAdapterModel) {
							JGraphAdapterModel bm = (JGraphAdapterModel) graph
							.getModel();

							// Check if already in model
							Object modelCell = bm.getMapping(cell
									.getUserObject());

							if (modelCell == null) {
								// TODO: Move into a method
								Map nested = new Hashtable();
								println("Inserting " + cell.getClass());
								List newCells = new ArrayList(3);
								newCells.add(cell);

								// TODO: Parent is only used if already in
								// model.
								ParentMap pm = new ParentMap();
								Object parent = ((JGraphSQLBackend) bm
										.getBackend()).getParent(cell
												.getUserObject());
								if (parent != null) {
									Object parentCell = bm.getMapping(parent);
									if (parentCell != null)
										pm.addEntry(cell, parentCell);
								}

								// Source and target are used or inserted.
								ConnectionSet cs = new ConnectionSet();
								if (bm.isEdge(cell)) {
									// Create source vertex if required
									Object source = (((JGraphSQLBackend) bm
											.getBackend()).getSource(cell
													.getUserObject()));
									if (source != null) {
										Object sourceCell = bm
										.getMapping(source);
										if (sourceCell == null
												&& source instanceof JGraphSQLEntity) {
											try {
												sourceCell = ((JGraphSQLBackend) bm
														.getBackend())
														.createCell(
																bm,
																(JGraphSQLEntity) source);
												nested
												.put(
														sourceCell,
														adapter
														.createCellAttributes(new Point2D.Double(
																10,
																10)));
											} catch (SQLException e1) {
												// TODO Auto-generated catch
												// block
												e1.printStackTrace();
											}
											newCells.add(sourceCell);
										}
										cs.connect(cell, bm.getChild(
												sourceCell, 0), true);
									}

									// Create target vertex if required
									Object target = (((JGraphSQLBackend) bm
											.getBackend()).getTarget(cell
													.getUserObject()));
									if (target != null) {
										Object targetCell = bm
										.getMapping(target);
										if (targetCell == null
												&& target instanceof JGraphSQLEntity) {
											try {
												targetCell = ((JGraphSQLBackend) bm
														.getBackend())
														.createCell(
																bm,
																(JGraphSQLEntity) target);
												nested
												.put(
														targetCell,
														adapter
														.createCellAttributes(new Point2D.Double(
																100,
																100)));
											} catch (SQLException e1) {
												// TODO Auto-generated catch
												// block
												e1.printStackTrace();
											}
											newCells.add(targetCell);
										}
										cs.connect(cell, bm.getChild(
												targetCell, 0), false);
									}
									nested.put(cell, adapter
											.createEdgeAttributes());
								} else {
									nested
									.put(
											cell,
											adapter
											.createCellAttributes(new Point2D.Double(
													10, 10)));
								}

								// TODO: For all new cells
								GraphConstants.setResize(cell.getAttributes(),
										true);

								println("nested: " + nested);
								// Inserts the double clicked node into the
								// model
								graph.getGraphLayoutCache().insert(
										newCells.toArray(), nested, cs, pm);
							} else {
								// TODO: Move into a method
								println("Updating userobject");
								// TODO: Maybe add tcn to entity?
								JGraphBusinessObject bo = (JGraphBusinessObject) businessModel
								.getValue(modelCell);
								JGraphBusinessObject other = (JGraphBusinessObject) cell
								.getUserObject();
								if (bo != other) {
									bo.getProperties().clear();
									bo.getProperties().putAll(
											other.getProperties());
									bm.cellsChanged(new Object[] { cell });
								}
								if (graph.getGraphLayoutCache().getMapping(
										modelCell, false) == null) {
									graph.getGraphLayoutCache().setVisible(
											new Object[] { modelCell }, null);
								} else {
									ParentMap pm = new ParentMap();
									Object parent = bm
									.getMapping(((JGraphSQLBackend) bm
											.getBackend())
											.getParent(cell
													.getUserObject()));
									if (parent != null) {
										pm.addEntry(modelCell, parent);
										graph.getGraphLayoutCache().edit(null,
												null, pm, null);
									}
									// TODO: Update source and target?
								}
								graph.setSelectionCell(modelCell);
							}
						}
					} else if (cell.getUserObject() instanceof JGraphBusinessObject
							&& SwingUtilities.isRightMouseButton(e)) { // eg. right
						// click
						println("Properties: "
								+ ((JGraphBusinessObject) cell.getUserObject())
								.getProperties());
					}
				}
			}
		});
	}

	protected static void println(String msg) {
		JGraphAdapterExample.println(msg);
	}

}