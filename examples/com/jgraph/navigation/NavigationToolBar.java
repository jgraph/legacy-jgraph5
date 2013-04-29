/*
 * Copyright (c) 2005, Informavores
 */
package com.jgraph.navigation;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;

import org.jgraph.JGraph;
import org.jgraph.graph.BasicMarqueeHandler;

public class NavigationToolBar {

	protected BasicMarqueeHandler connectorHandler;

	//
	// ToolBar
	//
	public JToolBar createToolBar(final NavigationExample app, final JGraph graph) {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		connectorHandler = graph.getMarqueeHandler();

		// Creates a panning handle for alternative use
		final BasicMarqueeHandler panningHandler = new PanningTool();
		URL panningUrl = getClass().getClassLoader().getResource(
				"com/jgraph/navigation/hand.gif");
		final ImageIcon panningIcon = new ImageIcon(panningUrl);
		URL selectingUrl = getClass().getClassLoader().getResource(
				"com/jgraph/navigation/select.gif");
		final ImageIcon selectingIcon = new ImageIcon(selectingUrl);
		toolbar.add(new AbstractAction("", selectingIcon) {
			public void actionPerformed(ActionEvent e) {
				if (graph.getMarqueeHandler() == panningHandler) {
					graph.setMarqueeHandler(connectorHandler);
					putValue(SMALL_ICON, selectingIcon);
				} else {
					graph.setMarqueeHandler(panningHandler);
					putValue(SMALL_ICON, panningIcon);
				}
			}
		});

		// Undo
		toolbar.addSeparator();
		URL undoUrl = getClass().getClassLoader().getResource(
				"org/jgraph/example/resources/undo.gif");
		ImageIcon undoIcon = new ImageIcon(undoUrl);
		Action undo = new AbstractAction("", undoIcon) {
			public void actionPerformed(ActionEvent e) {
				app.undo();
			}
		};
		undo.setEnabled(false);
		toolbar.add(undo);
		app.setUndo(undo);

		// Redo
		URL redoUrl = getClass().getClassLoader().getResource(
				"org/jgraph/example/resources/redo.gif");
		ImageIcon redoIcon = new ImageIcon(redoUrl);
		Action redo = new AbstractAction("", redoIcon) {
			public void actionPerformed(ActionEvent e) {
				app.redo();
			}
		};
		redo.setEnabled(false);
		toolbar.add(redo);
		app.setRedo(redo);

		//
		// Edit Block
		//
		toolbar.addSeparator();
		Action action;
		URL url;

		// Copy
		action = javax.swing.TransferHandler // JAVA13:
				// org.jgraph.plaf.basic.TransferHandler
				.getCopyAction();
		url = getClass().getClassLoader().getResource(
				"org/jgraph/example/resources/copy.gif");
		Action copy = app.new EventRedirector(action, new ImageIcon(url));
		toolbar.add(copy);
		app.setCopy(copy);

		// Paste
		action = javax.swing.TransferHandler // JAVA13:
				// org.jgraph.plaf.basic.TransferHandler
				.getPasteAction();
		url = getClass().getClassLoader().getResource(
				"org/jgraph/example/resources/paste.gif");
		Action paste = app.new EventRedirector(action, new ImageIcon(url));
		toolbar.add(paste);
		app.setPaste(paste);

		// Cut
		action = javax.swing.TransferHandler // JAVA13:
				// org.jgraph.plaf.basic.TransferHandler
				.getCutAction();
		url = getClass().getClassLoader().getResource(
				"org/jgraph/example/resources/cut.gif");
		Action cut = app.new EventRedirector(action, new ImageIcon(url));
		toolbar.add(cut);
		app.setCut(cut);

		// Remove
		URL removeUrl = getClass().getClassLoader().getResource(
				"org/jgraph/example/resources/delete.gif");
		ImageIcon removeIcon = new ImageIcon(removeUrl);
		Action remove = new AbstractAction("", removeIcon) {
			public void actionPerformed(ActionEvent e) {
				if (!graph.isSelectionEmpty()) {
					Object[] cells = graph.getSelectionCells();
					cells = graph.getDescendants(cells);
					graph.getModel().remove(cells);
				}
			}
		};
		remove.setEnabled(false);
		toolbar.add(remove);
		app.setRemove(remove);

		// Zoom Std
		toolbar.addSeparator();
		URL zoomUrl = getClass().getClassLoader().getResource(
				"org/jgraph/example/resources/zoom.gif");
		ImageIcon zoomIcon = new ImageIcon(zoomUrl);
		toolbar.add(new AbstractAction("", zoomIcon) {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(1.0);
			}
		});
		// Zoom In
		URL zoomInUrl = getClass().getClassLoader().getResource(
				"org/jgraph/example/resources/zoomin.gif");
		ImageIcon zoomInIcon = new ImageIcon(zoomInUrl);
		toolbar.add(new AbstractAction("", zoomInIcon) {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(2 * graph.getScale());
			}
		});
		// Zoom Out
		URL zoomOutUrl = getClass().getClassLoader().getResource(
				"org/jgraph/example/resources/zoomout.gif");
		ImageIcon zoomOutIcon = new ImageIcon(zoomOutUrl);
		toolbar.add(new AbstractAction("", zoomOutIcon) {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(graph.getScale() / 2);
			}
		});

		return toolbar;
	}
}
