/*
 * Copyright (c) 2005-2006, David Benson
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.portlabels;

import java.util.Map;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.ParentMap;
import org.jgraph.graph.Port;

/**
 * Graph model cell that has a number of fixed offset ports on the left
 * and right hand sides. It provides a number of utility methods to add
 * and remove those ports.
 */
public class PortLabelCell extends DefaultGraphCell {

	/**
	 * The number of ports on the left hand side of this cell
	 */
	protected int numLeftPorts = 0;
	
	/**
	 * The number of ports on the right hand side of this cell
	 */
	protected int numRightPorts = 0;
	
	/**
	 * Default constructor
	 *
	 */
	public PortLabelCell() {
		this(null);
	}

	/**
	 * Constructor with user object to set for cell. Whatever the user object
	 * returns with toString will appear in the vertex label.
	 * @param userObject the userObject to set
	 */
	public PortLabelCell(Object userObject) {
		super(userObject);
	}

	/**
	 * Adds a port with the specified label to the left hand side of the vertex
	 * at the specified position order
	 * @param position this ports position in the order of port from top to bottom
	 * @param portLabel the label to set for this port
	 * @return the new Port created
	 */
	public Port addLeftPort(int position, String portLabel, Map nestedMap, ParentMap parentMap) {
		return addLeftPort(position, new DefaultPort(portLabel), nestedMap, parentMap);
	}
	
	/**
	 * Adds a port to the left hand side of the vertex at the specified
	 * position order
	 * @param position this ports position in the order of port from top to bottom
	 * @param port the instance of the new port
	 * @return the the port
	 */
	public Port addLeftPort(int position, Port port, Map nestedMap, ParentMap parentMap) {
		if (position > numLeftPorts) {
			throw new RuntimeException("Incorrect port position of left-hand side of cell");
		}
		return null;
	}
	
	/**
	 * Adds a port with the specified label to the right hand side of the vertex
	 * at the specified position order
	 * @param position this ports position in the order of port from top to bottom
	 * @param portLabel the label to set for this port
	 * @return the new Port created
	 */
	public Port addRightPort(int position, String portLabel, Map nestedMap, ParentMap parentMap) {
		return addRightPort(position, new DefaultPort(portLabel), nestedMap, parentMap);
	}
	
	/**
	 * Adds a port to the right hand side of the vertex at the specified
	 * position order
	 * @param position this ports position in the order of port from top to bottom
	 * @param port the instance of the new port
	 * @return the the port
	 */
	public Port addRightPort(int position, Port port, Map nestedMap, ParentMap parentMap) {
		if (position > numRightPorts) {
			throw new RuntimeException("Incorrect port position of right-hand side of cell");
		}
		return null;
	}
	
	/**
	 * Removes the left-hand side port at the specified position order
	 * @param position the position order of the port to be removed
	 */
	public void removeLeftPort(int position, Map nestedMap, ParentMap parentMap) {
		
	}
	
	/**
	 * Removes the left-hand side port at the specified position order
	 * @param position the position order of the port to be removed
	 */
	public void removeRightPort(int position, Map nestedMap, ParentMap parentMap) {
		
	}

	/**
	 * @return Returns the numLeftPorts.
	 */
	public int getNumLeftPorts() {
		return numLeftPorts;
	}

	/**
	 * @param numLeftPorts The numLeftPorts to set.
	 */
	public void setNumLeftPorts(int numLeftPorts) {
		this.numLeftPorts = numLeftPorts;
	}

	/**
	 * @return Returns the numRightPorts.
	 */
	public int getNumRightPorts() {
		return numRightPorts;
	}

	/**
	 * @param numRightPorts The numRightPorts to set.
	 */
	public void setNumRightPorts(int numRightPorts) {
		this.numRightPorts = numRightPorts;
	}

}