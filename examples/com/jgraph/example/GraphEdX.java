/*
 * Copyright (c) 2001-2006, Gaudenz Alder
 * Copyright (c) 2005-2006, David Benson
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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.BeanInfo;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PersistenceDelegate;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.filechooser.FileFilter;

import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.example.GraphEd;
import org.jgraph.graph.AbstractCellView;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.ParentMap;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;

/**
 * An extension to GraphEd demonstrating more advanced JGraph features
 */
public class GraphEdX extends GraphEd {

	/**
	 * JGraph Factory instance for random new graphs
	 */
	protected JGraphGraphFactory graphFactory = new JGraphGraphFactory();

	/**
	 * References the folding manager.
	 */
	protected JGraphFoldingManager foldingManager;

	// Actions which Change State
	protected Action hide, collapse, expand, expandAll, configure;

	/**
	 * File chooser for loading and saving graphs. Note that it is lazily
	 * instaniated, always call initFileChooser before use.
	 */
	protected JFileChooser fileChooser = null;

	static {
		makeCellViewFieldsTransient(PortView.class);
		makeCellViewFieldsTransient(VertexView.class);
		makeCellViewFieldsTransient(EdgeView.class);

		// For XML Encoding of the graph instance, we need to exclude
		// the marquee handler explicitely. Being an inner class of
		// GraphEd, it should not be part of the written file.
		try {
			BeanInfo info = Introspector.getBeanInfo(MyGraph.class);
			PropertyDescriptor[] propertyDescriptors = info
					.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; ++i) {
				PropertyDescriptor pd = propertyDescriptors[i];
				if (pd.getName().equals("marqueeHandler")) {
					pd.setValue("transient", Boolean.TRUE);
				}
			}
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructs a new application
	 */
	public GraphEdX() {
		// Overrides the global vertex renderer
		VertexView.renderer = new JGraphGroupRenderer();

		// Prepares layout actions
		setJMenuBar(new GraphEdXMenuBar(this, graphFactory));
		// Initializes actions states
		valueChanged(null);
	}

	/**
	 * Show something interesting on start.
	 */
	public void init() {
		graphFactory.insertTreeSampleData(getGraph(),
				createCellAttributes(new Point2D.Double(0, 0)),
				createEdgeAttributes());
	}

	// Override parent method
	protected JGraph createGraph() {
		// Creates a model that does not allow disconnections
		GraphModel model = new MyGraphModel();
		GraphLayoutCache layoutCache = new GraphLayoutCache(model,
				new DefaultCellViewFactory(), true);
		return new MyGraph(model, layoutCache);
	}

	// Override parent method
	protected void installListeners(JGraph graph) {
		super.installListeners(graph);
		// Adds redirector for group collapse/expand
		foldingManager = new JGraphFoldingManager();
		graph.addMouseListener(foldingManager);
	}

	protected void uninstallListeners(JGraph graph) {
		super.uninstallListeners(graph);
		graph.removeMouseListener(foldingManager);
	}

	/**
	 * Updates buttons based on application state
	 */
	public void valueChanged(GraphSelectionEvent e) {
		super.valueChanged(e);
		// Group Button only Enabled if a cell is selected
		boolean enabled = !graph.isSelectionEmpty();
		// hide.setEnabled(enabled);
		expand.setEnabled(enabled);
		expandAll.setEnabled(enabled);
		collapse.setEnabled(enabled);
	}

	/**
	 * Overrides the parent example group method to set the bounds of the
	 * collapsed group cell appropriately
	 */
	public void group(Object[] children) {
		// Order Cells by Model Layering
		children = graph.order(children);
		// If Any Cells in View
		if (children != null && children.length > 0) {
			double gs2 = 2 * graph.getGridSize();
			Rectangle2D collapsedBounds = graph.getCellBounds(children);
			collapsedBounds.setFrame(collapsedBounds.getX(), collapsedBounds
					.getY(), Math.max(collapsedBounds.getWidth() / 4, gs2),
					Math.max(collapsedBounds.getHeight() / 2, gs2));
			graph.snap(collapsedBounds);
			DefaultGraphCell group = createGroupCell(collapsedBounds);
			if (group != null && children != null && children.length > 0) {
				// Create the group structure
				ParentMap pm = new ParentMap();
				for (int i = 0; i < children.length; i++) {
					pm.addEntry(children[i], group);
				}
				graph.getGraphLayoutCache().insert(new Object[] { group },
						null, null, pm);
			}
		}
	}

	/**
	 * Hook from GraphEd to create a new group cell
	 */
	protected DefaultGraphCell createGroupCell(Rectangle2D collapsedBounds) {
		DefaultGraphCell group = super.createGroupCell();
		group.addPort();
		GraphConstants.setInset(group.getAttributes(), 10);
		GraphConstants.setBackground(group.getAttributes(), new Color(240, 240,
				255));
		GraphConstants.setBorderColor(group.getAttributes(), Color.black);
		GraphConstants.setOpaque(group.getAttributes(), true);
		GraphConstants.setBorder(group.getAttributes(), JGraphShadowBorder
				.getSharedInstance());
		GraphConstants.setBounds(group.getAttributes(), collapsedBounds);
		return group;
	}

	/**
	 * Hook from GraphEd to set attributes of a new cell
	 */
	public Map createCellAttributes(Point2D point) {
		Map map = super.createCellAttributes(point);
		GraphConstants.setInset(map, 5);
		GraphConstants.setGradientColor(map, new Color(200, 200, 255));
		return map;
	}

	/**
	 * Hook from GraphEd to set attributes of a new edge
	 */
	public Map createEdgeAttributes() {
		Map map = super.createEdgeAttributes();
		// Adds a parallel edge router
		// GraphConstants.setLineStyle(map, GraphConstants.STYLE_SPLINE);
		if (GraphConstants.DEFAULTFONT != null) {
			GraphConstants.setFont(map, GraphConstants.DEFAULTFONT
					.deriveFont(10f));
		}
		return map;
	}

	/**
	 * Hook from GraphEd to add action button to the tool bar
	 */
	public JToolBar createToolBar() {
		JToolBar toolbar = super.createToolBar();

		// Collapse
		collapse = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				graph.getGraphLayoutCache().setVisible(graph.getSelectionCells(), false);
			}
		};
		URL url = getClass().getClassLoader().getResource(
				"org/jgraph/example/resources/collapse.gif");
		collapse.putValue(Action.SMALL_ICON, new ImageIcon(url));
		collapse.setEnabled(false);
		toolbar.add(collapse);

		// Expand
		expand = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				graph.getGraphLayoutCache().expand(graph.getSelectionCells());
			}
		};
		url = getClass().getClassLoader().getResource(
				"org/jgraph/example/resources/expand.gif");
		expand.putValue(Action.SMALL_ICON, new ImageIcon(url));
		expand.setEnabled(false);
		toolbar.add(expand);

		// ExpandAll
		expandAll = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Object[] allCells = graph.getDescendants(graph.getRoots());
				Map nested = new Hashtable();
				for (int i=0; i < allCells.length; i++) {
					Map attributeMap = new Hashtable();
					GraphConstants.setForeground(attributeMap, Color.BLACK);
					nested.put(allCells[i], attributeMap);
				}
				graph.getModel().edit(nested, null, null, null);
			}
		};
		url = getClass().getClassLoader().getResource(
				"org/jgraph/example/resources/expandAll.gif");
		expandAll.putValue(Action.SMALL_ICON, new ImageIcon(url));
		expandAll.setEnabled(false);
		toolbar.add(expandAll);
		return toolbar;
	}

	public void serializeGraph() {
		int returnValue = JFileChooser.CANCEL_OPTION;
		initFileChooser();
		returnValue = fileChooser.showSaveDialog(graph);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			Container parent = graph.getParent();
			BasicMarqueeHandler marquee = graph.getMarqueeHandler();
			graph.setMarqueeHandler(null);
			try {
				// Serializes the graph by removing it from the component
				// hierarchy and removing all listeners from it. The marquee
				// handler, begin an inner class of GraphEd, is not marked
				// serializable and will therefore not be stored. This must
				// be taken into account when deserializing a graph.
				uninstallListeners(graph);
				parent.remove(graph);
				ObjectOutputStream out = new ObjectOutputStream(
						new BufferedOutputStream(new FileOutputStream(
								fileChooser.getSelectedFile())));
				out.writeObject(graph);
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(graph, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			} finally {
				// Adds the component back into the component hierarchy
				graph.setMarqueeHandler(marquee);
				if (parent instanceof JViewport) {
					JViewport viewPort = (JViewport) parent;
					viewPort.setView(graph);
				} else {
					// Best effort...
					parent.add(graph);
				}
				// And reinstalls the listener
				installListeners(graph);
			}
		}
	}

	public void deserializeGraph() {
		int returnValue = JFileChooser.CANCEL_OPTION;
		initFileChooser();
		returnValue = fileChooser.showOpenDialog(graph);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			Container parent = graph.getParent();
			BasicMarqueeHandler marqueeHandler = graph.getMarqueeHandler();
			try {
				uninstallListeners(graph);
				parent.remove(graph);
				ObjectInputStream in = new ObjectInputStream(
						new BufferedInputStream(new FileInputStream(fileChooser
								.getSelectedFile())));
				graph = (JGraph) in.readObject();
				// Take the marquee handler from the original graph and
				// use it in the new graph as well.
				graph.setMarqueeHandler(marqueeHandler);
				// Adds the component back into the component hierarchy
				if (parent instanceof JViewport) {
					JViewport viewPort = (JViewport) parent;
					viewPort.setView(graph);
				} else {
					// Best effort...
					parent.add(graph);
				}
				// graph.setMarqueeHandler(previousHandler);
				// And reinstalls the listener
				installListeners(graph);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(graph, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void saveFile() {
		int returnValue = JFileChooser.CANCEL_OPTION;
		initFileChooser();
		returnValue = fileChooser.showSaveDialog(graph);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			XMLEncoder encoder;
			Container parent = graph.getParent();
			try {
				uninstallListeners(graph);
				parent.remove(graph);
				encoder = new XMLEncoder(new BufferedOutputStream(
						new FileOutputStream(fileChooser.getSelectedFile())));
				configureEncoder(encoder);
				encoder.writeObject(graph);
				encoder.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(graph, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			} finally {
				// Adds the component back into the component hierarchy
				if (parent instanceof JViewport) {
					JViewport viewPort = (JViewport) parent;
					viewPort.setView(graph);
				} else {
					// Best effort...
					parent.add(graph);
				}
				// And reinstalls the listener
				installListeners(graph);
			}
		}
	}

	public void openFile() {
		int returnValue = JFileChooser.CANCEL_OPTION;
		initFileChooser();
		returnValue = fileChooser.showOpenDialog(graph);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			Container parent = graph.getParent();
			BasicMarqueeHandler marqueeHandler = graph.getMarqueeHandler();
			try {
				uninstallListeners(graph);
				parent.remove(graph);
				XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(
						new FileInputStream(fileChooser.getSelectedFile())));

				// In case you need to debug XML decoding here is how
				// to print a stack trace
				/*
				decoder.setExceptionListener(new ExceptionListener()
				{
					public void exceptionThrown(Exception exception)
					{
						exception.printStackTrace();
					}
				});
				*/
				
				graph = (JGraph) decoder.readObject();
				// Take the marquee handler from the original graph and
				// use it in the new graph as well.
				graph.setMarqueeHandler(marqueeHandler);
				// Adds the component back into the component hierarchy
				if (parent instanceof JViewport) {
					JViewport viewPort = (JViewport) parent;
					viewPort.setView(graph);
				} else {
					// Best effort...
					parent.add(graph);
				}
				// graph.setMarqueeHandler(previousHandler);
				// And reinstalls the listener
				installListeners(graph);
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(graph, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	/**
	 * Utility method that ensures the file chooser is created. Start-up time
	 * is improved by lazily instaniating choosers.
	 *
	 */
	protected void initFileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			FileFilter fileFilter = new FileFilter() {
				/**
				 * @see javax.swing.filechooser.FileFilter#accept(File)
				 */
				public boolean accept(File f) {
					if (f == null)
						return false;
					if (f.getName() == null)
						return false;
					if (f.getName().endsWith(".xml"))
						return true;
					if (f.getName().endsWith(".ser"))
						return true;
					if (f.isDirectory())
						return true;

					return false;
				}

				/**
				 * @see javax.swing.filechooser.FileFilter#getDescription()
				 */
				public String getDescription() {
					return "GraphEd file (.xml, .ser)";
				}
			};
			fileChooser.setFileFilter(fileFilter);
		}
	}

	protected void configureEncoder(XMLEncoder encoder) {
		// Better debugging output, in case you need it
		encoder.setExceptionListener(new ExceptionListener() {
			public void exceptionThrown(Exception e) {
				e.printStackTrace();
			}
		});

		encoder.setPersistenceDelegate(DefaultGraphModel.class,
				new DefaultPersistenceDelegate(new String[] { "roots",
						"attributes" }));
		encoder.setPersistenceDelegate(MyGraphModel.class,
				new DefaultPersistenceDelegate(new String[] { "roots",
						"attributes" }));

		// Note: In the static initializer the marquee handler of the
		// MyGraph class is made transient to avoid being written out.
		encoder.setPersistenceDelegate(MyGraph.class,
				new DefaultPersistenceDelegate(new String[] { "model",
						"graphLayoutCache" }));
		encoder
				.setPersistenceDelegate(GraphLayoutCache.class,
						new DefaultPersistenceDelegate(new String[] { "model",
								"factory", "cellViews", "hiddenCellViews",
								"partial" }));
		encoder.setPersistenceDelegate(DefaultGraphCell.class,
				new DefaultPersistenceDelegate(new String[] { "userObject" }));
		encoder.setPersistenceDelegate(DefaultEdge.class,
				new DefaultPersistenceDelegate(new String[] { "userObject" }));
		encoder.setPersistenceDelegate(DefaultPort.class,
				new DefaultPersistenceDelegate(new String[] { "userObject" }));
		encoder.setPersistenceDelegate(AbstractCellView.class,
				new DefaultPersistenceDelegate(new String[] { "cell",
						"attributes" }));
		encoder.setPersistenceDelegate(DefaultEdge.DefaultRouting.class,
				new PersistenceDelegate() {
					protected Expression instantiate(Object oldInstance,
							Encoder out) {
						return new Expression(oldInstance,
								GraphConstants.class, "getROUTING_SIMPLE", null);
					}
				});
		encoder.setPersistenceDelegate(DefaultEdge.LoopRouting.class,
				new PersistenceDelegate() {
					protected Expression instantiate(Object oldInstance,
							Encoder out) {
						return new Expression(oldInstance,
								GraphConstants.class, "getROUTING_DEFAULT",
								null);
					}
				});
		encoder.setPersistenceDelegate(JGraphShadowBorder.class,
				new PersistenceDelegate() {
					protected Expression instantiate(Object oldInstance,
							Encoder out) {
						return new Expression(oldInstance,
								JGraphShadowBorder.class, "getSharedInstance",
								null);
					}
				});
		encoder.setPersistenceDelegate(ArrayList.class, encoder
				.getPersistenceDelegate(List.class));
	}

	/**
	 * Main method
	 */
	public static void main(String[] args) {
		try {
			// Switch off D3D because of Sun XOR painting bug
			// See http://www.jgraph.com/forum/viewtopic.php?t=4066
			System.setProperty("sun.java2d.d3d", "false");
			// Construct Frame
			JFrame frame = new JFrame("GraphEdX");
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
			GraphEdX app = new GraphEdX();
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

	/**
	 * Makes all fields but <code>cell</code> and <code>attributes</code>
	 * transient in the bean info of <code>clazz</code>.
	 * 
	 * @param clazz
	 *            The cell view class who fields should be made transient.
	 */
	public static void makeCellViewFieldsTransient(Class clazz) {
		try {
			BeanInfo info = Introspector.getBeanInfo(clazz);
			PropertyDescriptor[] propertyDescriptors = info
					.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; ++i) {
				PropertyDescriptor pd = propertyDescriptors[i];
				if (!pd.getName().equals("cell")
						&& !pd.getName().equals("attributes")) {
					pd.setValue("transient", Boolean.TRUE);
				}
			}
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Encodable graph model with related constructor. Note: This class must be
	 * static for the XML encoding to work.
	 */
	public static class MyGraphModel extends DefaultGraphModel {

		public MyGraphModel() {
			super();
		}

		public MyGraphModel(List roots, AttributeMap attributes) {
			super(roots, attributes);
		}

		public boolean acceptsSource(Object edge, Object port) {
			return (port != null);
		}

		public boolean acceptsTarget(Object edge, Object port) {
			return (port != null);
		}
	}

}
