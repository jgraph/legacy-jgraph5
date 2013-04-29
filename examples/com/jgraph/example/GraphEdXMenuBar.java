/*
 * $Id: GraphEdXMenuBar.java,v 1.5 2005-12-07 19:44:09 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved. 
 * 
 * This file is licensed under the JGraph software license, a copy of which
 * will have been provided to you in the file LICENSE at the root of your
 * installation directory. If you are unable to locate this file please
 * contact JGraph sales for another copy.
 */
package com.jgraph.example;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

/**
 * A simple menu bar
 */
public class GraphEdXMenuBar extends JMenuBar {

	/**
	 * JGraph Factory instance for random new graphs
	 */
	protected JGraphGraphFactory graphFactory = null;

	public GraphEdXMenuBar(final GraphEdX app, JGraphGraphFactory factory) {
		graphFactory = factory;

		// Sample data menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new JMenuItem(new AbstractAction("Open") {
			public void actionPerformed(ActionEvent e) {
				app.openFile();
			}
		}));

		fileMenu.add(new JMenuItem(new AbstractAction("Save") {
			public void actionPerformed(ActionEvent e) {
				app.saveFile();
			}
		}));

		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(new AbstractAction("Deserialize") {
			public void actionPerformed(ActionEvent e) {
				app.deserializeGraph();
			}
		}));

		fileMenu.add(new JMenuItem(new AbstractAction("Serialize") {
			public void actionPerformed(ActionEvent e) {
				app.serializeGraph();
			}
		}));

		add(fileMenu);

		// Sample data menu
		JMenu sampleMenu = new JMenu("Sample Data");
		sampleMenu.add(new JMenuItem(new AbstractAction("Insert Random Tree") {
			public void actionPerformed(ActionEvent e) {
				graphFactory.insertGraph(app.getGraph(),
						JGraphGraphFactory.TREE,
						app.createCellAttributes(new Point2D.Double(0, 0)), app
								.createEdgeAttributes());
			}
		}));

		sampleMenu.add(new JMenuItem(new AbstractAction("Insert Random Graph") {
			public void actionPerformed(ActionEvent e) {
				graphFactory.insertGraph(app.getGraph(),
						JGraphGraphFactory.RANDOM_CONNECTED,
						app.createCellAttributes(new Point2D.Double(0, 0)), app
								.createEdgeAttributes());
			}
		}));

		add(sampleMenu);
	}

	/**
	 * helper for creating radio button menu items
	 * 
	 * @param group
	 *            the <code>ButtonGroup</code> of the item
	 * @param action
	 *            the <code>Action</code> associated with the item
	 * @return the menu item
	 */
	public JRadioButtonMenuItem createRadioMenuItem(ButtonGroup group,
			Action action) {
		JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(action);
		menuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift "
				+ String.valueOf(action.getValue("shortcut")).substring(0, 1)
						.toUpperCase()));
		group.add(menuItem);
		return menuItem;
	}

}
