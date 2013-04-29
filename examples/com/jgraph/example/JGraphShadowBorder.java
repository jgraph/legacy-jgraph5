/*
 * $Id: JGraphShadowBorder.java,v 1.3 2005-11-25 15:53:11 david Exp $
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.Serializable;

import javax.swing.border.Border;

/**
 * Example of a shadowed border
 */
public class JGraphShadowBorder implements Border, Serializable {
	/**
	 * Inset for groups
	 */
	protected Insets insets;
	
	/**
	 * Shared class instance
	 */
	public static JGraphShadowBorder sharedInstance = new JGraphShadowBorder();

	/**
	 * Creates a new shadow border with default dimensions
	 *
	 */
	private JGraphShadowBorder() {
		insets = new Insets(1, 1, 3, 3);
	}

	/**
	 * @return the <code>insets</code> value
	 */
	public Insets getBorderInsets(Component c) {
		return insets;
	}

	/**
	 * @return whether not the border is opaque
	 */
	public boolean isBorderOpaque() {
		// we'll be filling in our own background.
		return true;
	}

	/**
	 * The paint method for this border
	 */
	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
		// choose which colors we want to use
		Color bg = c.getBackground();
		if (c.getParent() != null)
			bg = c.getParent().getBackground();
		Color mid = bg.darker();
		Color rect = mid.darker();
		Color edge = average(mid, bg);

		// fill in the corners with the parent-background
		// so it looks see-through
		g.setColor(bg);
		g.fillRect(0, h - 3, 3, 3);
		g.fillRect(w - 3, 0, 3, 3);
		g.fillRect(w - 3, h - 3, 3, 3);

		// draw the outline
		g.setColor(rect);
		g.drawRect(0, 0, w - 3, h - 3);

		// draw the drop-shadow
		g.setColor(mid);
		g.drawLine(1, h - 2, w - 2, h - 2);
		g.drawLine(w - 2, 1, w - 2, h - 2);

		g.setColor(edge);
		g.drawLine(2, h - 1, w - 2, h - 1);
		g.drawLine(w - 1, 2, w - 1, h - 2);
	}

	/**
	 * Returns the average color between the two provided
	 * @param c1 the first color
	 * @param c2 the second color
	 * @return the average of the input colors
	 */
	private static Color average(Color c1, Color c2) {
		int red = c1.getRed() + (c2.getRed() - c1.getRed()) / 2;
		int green = c1.getGreen() + (c2.getGreen() - c1.getGreen()) / 2;
		int blue = c1.getBlue() + (c2.getBlue() - c1.getBlue()) / 2;
		return new Color(red, green, blue);
	}
	
	/**
	 * @return the shared instance of this class
	 */
	public static JGraphShadowBorder getSharedInstance() {
		return sharedInstance;
	}
}