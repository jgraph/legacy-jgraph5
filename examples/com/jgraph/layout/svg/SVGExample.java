/*
 * $Id: SVGExample.java,v 1.1 2009-09-25 15:17:49 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * Copyright (c) 2005-2006, David Benson
 *
 * All rights reserved.
 *
 * This file is licensed under the JGraph software license, a copy of which
 * will have been provided to you in the file LICENSE at the root of your
 * installation directory. If you are unable to locate this file please
 * contact JGraph sales for another copy.
 */
package com.jgraph.layout.svg;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;

import com.jgraph.example.GraphEdX;
import com.jgraph.io.svg.SVGGraphWriter;
import com.jgraph.io.svg.SVGVertexRenderer;

public class SVGExample extends GraphEdX {

	/** the default background color, white by default */
	protected Color background = Color.white;

	/**
	 * File chooser for exporting SVG. Note that it is lazily
	 * instaniated, always call initFileChooser before use.
	 */
	protected JFileChooser SVGFileChooser = null;

	public SVGExample() {
		setJMenuBar(new SVGMenuBar(this, graphFactory));
	}

	public void setDefaultBackgroundColor(Color color) {
		background = color;
	}

	/**
	 * @return Returns the background.
	 */
	public Color getDefaultBackgroundColour() {
		return background;
	}

	/**
	 * Utility method that ensures the file chooser is created. Start-up time
	 * is improved by lazily instaniating choosers.
	 *
	 */
	protected void initSVGFileChooser() {
		if (SVGFileChooser == null) {
			SVGFileChooser = new JFileChooser();
			FileFilter fileFilter = new FileFilter() {
				/**
				 * @see javax.swing.filechooser.FileFilter#accept(File)
				 */
				public boolean accept(File f) {
					if (f == null)
						return false;
					if (f.getName() == null)
						return false;
					if (f.getName().endsWith(".svg"))
						return true;
					if (f.isDirectory())
						return true;

					return false;
				}

				/**
				 * @see javax.swing.filechooser.FileFilter#getDescription()
				 */
				public String getDescription() {
					return "SVG file (.SVG)";
				}
			};
			SVGFileChooser.setFileFilter(fileFilter);
		}
	}

	protected void exportSVG() {
		int returnValue = JFileChooser.CANCEL_OPTION;
		initSVGFileChooser();
		returnValue = SVGFileChooser.showSaveDialog(graph);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			VertexRenderer oldRenderer = VertexView.renderer;
			try {
				OutputStream out = new BufferedOutputStream(new FileOutputStream(
						SVGFileChooser.getSelectedFile()));
				// Set all vertices to use the SVG renderer
				VertexView.renderer = new SVGVertexRenderer();
				SVGGraphWriter writer = new SVGGraphWriter();
				// SVG nodes will have no size at this point. They need
				// to be given suitable bounds prior to being passed to the
				// layout algorithm.
//				graph.computeVertexSizes();
				writer.write(new BufferedOutputStream(out), null,
						graph.getGraphLayoutCache(), 35);
				out.flush();
				out.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(graph, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			VertexView.renderer = oldRenderer;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Construct Frame
			JFrame frame = new JFrame("SVG Example");
			// Set Close Operation to Exit
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			// Fetch URL to Icon Resource
			URL jgraphUrl = GraphEdX.class.getClassLoader().getResource(
					"org/jgraph/example/resources/jgraph.gif");
			// If Valid URL
			if (jgraphUrl != null) {
				// Load Icon
				ImageIcon jgraphIcon = new ImageIcon(jgraphUrl);
				// Use in Window
				frame.setIconImage(jgraphIcon.getImage());
			}
			// Add an Editor Panel
			SVGExample app = new SVGExample();
			frame.getContentPane().add(app);
			app.init();
			// Set Default Size
			frame.setSize(640, 480);
			// Show Frame
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}