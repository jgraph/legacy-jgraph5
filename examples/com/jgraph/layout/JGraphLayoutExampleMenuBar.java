/*
 * $Id: JGraphLayoutExampleMenuBar.java,v 1.1 2009-09-25 15:17:49 david Exp $
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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import com.jgraph.example.GraphEdXMenuBar;
import com.jgraph.example.JGraphGraphFactory;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class JGraphLayoutExampleMenuBar extends GraphEdXMenuBar {

	/**
	 * JGraph Factory instance for random new graphs
	 */
	protected JGraphGraphFactory graphFactory = null;

	public JGraphLayoutExampleMenuBar(final JGraphLayoutExample app,
			JGraphGraphFactory factory) {
		super(app, factory);
		graphFactory = factory;

		// Layout menu
		JMenu layoutMenu = new JMenu("Layout");
		ButtonGroup layoutGroup = new ButtonGroup();
		Action[] layoutActions = app.layoutActions;
		for (int i = 0; i < layoutActions.length; i++) {
			layoutMenu.add(createRadioMenuItem(layoutGroup, layoutActions[i]));
		}
		layoutMenu.getItem(0).setSelected(true);
		add(layoutMenu);
		layoutMenu.setEnabled(true);

		// Move selected cells switch
		JMenu optionsMenu = new JMenu("Options");
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(new AbstractAction(
				"Allow to move selected cells") {
			public void actionPerformed(ActionEvent e) {
				app.layoutCache.layoutMoveSelection = ((JCheckBoxMenuItem) e
						.getSource()).isSelected();
			}
		});
		item.setSelected(app.layoutCache.layoutMoveSelection);
		optionsMenu.add(item);
		item = new JCheckBoxMenuItem(new AbstractAction(
				"Ignore cells inside groups") {
			public void actionPerformed(ActionEvent e) {
				app.layoutCache.ignoreChildren = ((JCheckBoxMenuItem) e
						.getSource()).isSelected();
			}
		});
		item.setSelected(app.layoutCache.ignoreChildren);
		optionsMenu.add(item);
		item = new JCheckBoxMenuItem(new AbstractAction("Ignore hidden cells") {
			public void actionPerformed(ActionEvent e) {
				app.layoutCache.ignoreHidden = ((JCheckBoxMenuItem) e
						.getSource()).isSelected();
			}
		});
		item.setSelected(app.layoutCache.ignoreHidden);
		optionsMenu.add(item);
		item = new JCheckBoxMenuItem(new AbstractAction(
				"Ignore unconnected cells") {
			public void actionPerformed(ActionEvent e) {
				app.layoutCache.ignoreUnconnected = ((JCheckBoxMenuItem) e
						.getSource()).isSelected();
			}
		});
		item.setSelected(app.layoutCache.ignoreUnconnected);
		optionsMenu.add(item);
		item = new JCheckBoxMenuItem(new AbstractAction("Directed layout") {
			public void actionPerformed(ActionEvent e) {
				app.layoutCache.layoutDirectedGraph = ((JCheckBoxMenuItem) e
						.getSource()).isSelected();
			}
		});
		item.setSelected(app.layoutCache.layoutDirectedGraph);
		optionsMenu.add(item);
		optionsMenu.addSeparator();
		item = new JCheckBoxMenuItem(new AbstractAction("Flush to origin") {
			public void actionPerformed(ActionEvent e) {
				app.layoutCache.layoutFlushOrigin = ((JCheckBoxMenuItem) e
						.getSource()).isSelected();
			}
		});
		item.setSelected(app.layoutCache.layoutFlushOrigin);
		optionsMenu.add(item);
		item = new JCheckBoxMenuItem(new AbstractAction("Ignore grid") {
			public void actionPerformed(ActionEvent e) {
				app.layoutCache.layoutIgnoreGrid = ((JCheckBoxMenuItem) e
						.getSource()).isSelected();
			}
		});
		item.setSelected(app.layoutCache.layoutIgnoreGrid);
		optionsMenu.add(item);
		optionsMenu.addSeparator();
		item = new JCheckBoxMenuItem(new AbstractAction("Morphing") {
			public void actionPerformed(ActionEvent e) {
				app.layoutCache.morphing = ((JCheckBoxMenuItem) e.getSource())
						.isSelected();
			}
		});
		item.setSelected(app.layoutCache.morphing);
		optionsMenu.add(item);

		add(optionsMenu);
	}
}
