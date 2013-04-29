package com.jgraph.example.groupeditor;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgraph.graph.VertexView;

import com.jgraph.example.GraphEdX;
import com.jgraph.example.GraphEdXMenuBar;

/**
 * A demo based on GraphEdX that demonstrates a simple way of allowing the user
 * to edit cells inside groups or the groups themselves. NB: in order to have the
 * edit button displayed in the top layer (not shadowed by child cells), we need
 * to display them on the border away from the child cells. To do that, we
 * assume we groups have larger bounds than the union of their child cells, a
 * feauture implemented in GraphEdX in the group method.
 * 
 * Remark: using a click system rather than this button system wouldn't work
 * because the click count is already used to traverse the cell selection.
 * 
 * @author rvalyi
 */
public class EditableGroupDemo extends GraphEdX {

	/**
	 * References the folding manager.
	 */
	protected GroupManager foldingManager;

	// Override parent method
	protected void installListeners(JGraph graph) {
		super.installListeners(graph);
		// Adds redirector for group collapse/expand
		foldingManager = new GroupManager();
		graph.addMouseListener(foldingManager);
	}

	protected void uninstallListeners(JGraph graph) {
		super.uninstallListeners(graph);
		graph.removeMouseListener(foldingManager);
	}

	/**
	 * Constructs a new application
	 */
	public EditableGroupDemo() {
		// Overrides the global vertex renderer
		VertexView.renderer = new EditableGroupRenderer();

		// Prepares layout actions
		setJMenuBar(new GraphEdXMenuBar(this, graphFactory));
		// Initializes actions states
		valueChanged(null);
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
			// Add an Editor Panel
			GraphEdX app = new EditableGroupDemo();
			frame.getContentPane().add(app);
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
			// Set Default Size
			frame.setSize(640, 480);
			// Show Frame
			frame.setVisible(true);
			app.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
