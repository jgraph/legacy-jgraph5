/*
 * Copyright (c) 2001-2005, Gaudenz Alder
 * Copyright (c) 2005, David Benson
 * 
 * See LICENSE file in distribution for licensing details of this source file
 */
package com.jgraph.example.fastgraph;

import java.util.Iterator;
import java.util.Map;

import javax.swing.undo.UndoableEdit;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.ParentMap;

// A Custom Model that does not allow Self-References
public class FastGraphModel extends DefaultGraphModel {
	
	/**
	 * A flag indicating whether or not undos are currently disabled
	 */
	protected boolean undoDisabled = true;
	
	// Override Superclass Method
	public boolean acceptsSource(Object edge, Object port) {
		// Source only Valid if not Equal Target
		return (((Edge) edge).getTarget() != port);
	}

	// Override Superclass Method
	public boolean acceptsTarget(Object edge, Object port) {
		// Target only Valid if not Equal Source
		return (((Edge) edge).getSource() != port);
	}

	/**
	 * Extends DefaultGraphModel insert method to check for large changes and
	 * sets the <code>undoDisabled</code> flag to true. Disabling the undo
	 * provides large performance and memory footprint advantages on huge
	 * inserts.
	 */
	public void insert(Object[] roots, Map attributes, ConnectionSet cs,
			ParentMap pm, UndoableEdit[] edits) {
//		System.out.println("Model insert starting, timestamp="
//				+ System.currentTimeMillis() + "ms");
		GraphModelEdit edit = createEdit(roots, null, attributes, cs, pm, edits);
		if (edit != null) {
			edit.execute(); // fires graphChangeEvent
			if (edits != null) {
				for (int i = 0; i < edits.length; i++)
					if (edits[i] instanceof GraphLayoutCache.GraphLayoutCacheEdit)
						((GraphLayoutCache.GraphLayoutCacheEdit) edits[i])
								.execute();
			}
			if (!undoDisabled) {
				postEdit(edit); // fires undoableedithappened
			}
		}
//		System.out.println("Model insert finished, timestamp="
//				+ System.currentTimeMillis() + "ms");
	}

	public void edit(Map attributes, ConnectionSet cs, ParentMap pm,
			UndoableEdit[] edits) {
//		System.out.println("Model edit starting, timestamp="
//				+ System.currentTimeMillis() + "ms");
		super.edit(attributes, cs, pm, edits);
//		System.out.println("Model edit finished, timestamp="
//				+ System.currentTimeMillis() + "ms");
	}

	/**
	 * Applies <code>attributes</code> to the cells specified as keys. Returns
	 * the <code>attributes</code> to undo the change.
	 */
	protected Map handleAttributes(Map attributes) {
		if (!undoDisabled) {
			return super.handleAttributes(attributes);
		}
		if (attributes != null) {
			Iterator it = attributes.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				Object cell = entry.getKey();
				Map deltaNew = (Map) entry.getValue();
				AttributeMap attr = getAttributes(cell);
				
				if (attr != null) {
					attr.applyMap(deltaNew);
				}
				// Handle new values
				Object newValue = deltaNew.get(GraphConstants.VALUE);
				if (newValue != null) {
					valueForCellChanged(cell, newValue);
				}
			}
		}
		return null;
	}
}