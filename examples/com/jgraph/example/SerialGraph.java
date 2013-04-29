/*
 * @(#)EditorGraph.java 3.3 23-APR-04
 *  
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFrame;

import org.jgraph.JGraph;

/**
 * An example to demonstrate the use of serialization in JGraph. As of version
 * 5.7.5, the JGraph object is fully serializable. In a real-world situation,
 * the graph must be prepared for serialization as shown in GraphEdX. Writing
 * the graph to a file using the XMLEncoder is also demonstrated there.
 * 
 * @version 1.0 06-DEC-05
 * @author Gaudenz Alder
 */
public class SerialGraph {

	public static String FILENAME = "test.tmp";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Switch off D3D because of Sun XOR painting bug
		// See http://www.jgraph.com/forum/viewtopic.php?t=4066
		System.setProperty("sun.java2d.d3d", "false");
		JGraph graph = new JGraph();
		writeObject(graph, FILENAME);
		graph = (JGraph) readObject(FILENAME);
		JFrame frame = new JFrame("SerialGraph");
		frame.getContentPane().add(graph);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public static void writeObject(Object object, String filename) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					new BufferedOutputStream(new FileOutputStream(filename)));
			out.writeObject(object);
			out.flush();
			out.close();
			File file = new File(filename);
			System.out.println("File size is " + file.length() + " byte(s)");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Object readObject(String filename) {
		try {
			ObjectInputStream in = new ObjectInputStream(
					new BufferedInputStream(new FileInputStream(filename)));
			Object object = in.readObject();
			in.close();
			File file = new File(filename);
			file.delete();
			return object;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
