/*
 * @(#)Main.java 3.3 23-APR-04
 * 
 * Copyright (c) 2001-2005, Gaudenz Alder All rights reserved.
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class Main {

	// Construct Frame
	static JFrame f = new JFrame("JGraph Examples");

	// Main Method
	public static void main(String[] args) {
		// Switch off D3D because of Sun XOR painting bug
		// See http://www.jgraph.com/forum/viewtopic.php?t=4066
		System.setProperty("sun.java2d.d3d", "false");
		// Set Close Operation to Exit
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Get Content Pane
		Container p = f.getContentPane();
		// Set Layout
		p.setLayout(new GridLayout(3, 2));
		// Add Buttons
		p.add(createButton(com.jgraph.example.EditorGraph.class, args));
		p.add(createButton(org.jgraph.example.GraphEd.class, args));
		p.add(createButton(com.jgraph.example.GraphEdX.class, args));
		p.add(createButton(com.jgraph.example.GraphEdMV.class, args));
		p.add(createButton(com.jgraph.example.GraphTreeModel.class, args));
		p.add(createButton(com.jgraph.example.MyPortView.class, args));
		p.add(createButton(org.jgraph.example.HelloWorld.class, args));
		p.add(createButton(com.jgraph.example.fastgraph.FastGraph.class, args));
		p
				.add(createButton(
						com.jgraph.example.adapter.JGraphAdapterExample.class, args));
		p
		.add(createButton(
				com.jgraph.example.GraphSelectionDemo.class, args));
		p
		.add(createButton(
				com.jgraph.example.mycellview.MyCellView.class, args));
		p
		.add(createButton(
				com.jgraph.example.mycellmodeleditor.MyCellModelEditor.class, args));
		p
		.add(createButton(
				com.jgraph.example.groupeditor.EditableGroupDemo.class, args));
		
		// Set Default Size
		f.pack();
		// Show Frame
		f.setVisible(true);
	}

	public static JButton createButton(final Class aClass, final String[] args) {
		JButton b = new JButton(aClass.getName().substring(
				aClass.getPackage().getName().length() + 1));
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run(aClass, args);
			}
		});
		return b;
	}

	public static void run(Class aClass, String[] args) {
		try {
			aClass.getDeclaredMethod("main", new Class[] { String[].class })
					.invoke(null, new Object[] { args });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
