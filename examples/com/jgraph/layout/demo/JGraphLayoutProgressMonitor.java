/*
 * $Id: JGraphLayoutProgressMonitor.java,v 1.1 2009-09-25 15:17:49 david Exp $
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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ProgressMonitor;

import com.jgraph.layout.JGraphLayoutProgress;

/**
 * Utility progress monitor for a layout progress object. Implements a
 * property change listener to update itself based on the running layout.
 * The listener is added to the progress object in the constructor and
 * removed from it when the close method is called.
 * 
 */
public class JGraphLayoutProgressMonitor extends ProgressMonitor implements
		PropertyChangeListener {

	/**
	 * References the progress being monitored.
	 */
	protected JGraphLayoutProgress progress;

	/**
	 * Constructs a new progress monitor for the specified progress object.
	 * 
	 * @param component
	 *            The parent component to use for the dialog.
	 * @param progress
	 *            The progress object to be monitored.
	 * @param message
	 *            The message to display.
	 */
	public JGraphLayoutProgressMonitor(Component component,
			JGraphLayoutProgress progress, String message) {
		super(component, message, "", 0, 100);
		this.progress = progress;
		progress.addPropertyChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(
				JGraphLayoutProgress.PROGRESS_PROPERTY)) {
			int newValue = Integer.parseInt(String.valueOf(evt
					.getNewValue()));
			setProgress(newValue);
		}

		// Updates the maximum property. This is set after the layout
		// run method has been called.
		else if (evt.getPropertyName().equals(
				JGraphLayoutProgress.MAXIMUM_PROPERTY)) {
			int newValue = Integer.parseInt(String.valueOf(evt
					.getNewValue()));
			setMaximum(newValue);
		}

		// Checks isCancelled and stops the layout if it has been pressed
		if (isCanceled())
			progress.setStopped(true);
	}

	/**
	 * Overrides the parent's implementation to remove the property change
	 * listener from the progress.
	 */
	public void close() {
		progress.removePropertyChangeListener(this);
		super.close();
	}

}