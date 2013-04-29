/*
 * $Id: JGraphLayoutDemo.java,v 1.1 2009-09-25 15:17:49 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved. 
 * 
 * This file is licensed under the JGraph software license, a copy of which
 * will have been provided to you in the file LICENSE at the root of your
 * installation directory. If you are unable to locate this file please
 * contact JGraph sales for another copy.
 */
package com.jgraph.layout.demo;

import java.awt.BorderLayout;

import javax.swing.JApplet;
import javax.swing.JFrame;

public class JGraphLayoutDemo extends JApplet {

	/**
	 * Initializes the applet by showing something interesting.
	 */
	public void start() {
		JGraphLayoutPanel layoutPanel = new JGraphLayoutPanel();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(layoutPanel, BorderLayout.CENTER);
		layoutPanel.init();
	}

	/**
	 * Starts the demo as an application.
	 */
	public static void main(String[] args) {
		// Switch off D3D because of Sun XOR painting bug
		// See http://www.jgraph.com/forum/viewtopic.php?t=4066
		System.setProperty("sun.java2d.d3d", "false");
		JFrame frame = new JFrame();
		JGraphLayoutPanel layoutPanel = new JGraphLayoutPanel();
		frame.getContentPane().add(layoutPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setSize(640, 520);
		frame.setVisible(true);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// continue
		}
		layoutPanel.init();
	}

}
