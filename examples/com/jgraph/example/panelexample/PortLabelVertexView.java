package com.jgraph.example.panelexample;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.GraphCellEditor;
import org.jgraph.graph.VertexView;

/**
 * 
 */
public class PortLabelVertexView extends VertexView {

	/**
	 * the renderer for this view
	 */
	protected static WrapperPortLabelRenderer renderer = new WrapperPortLabelRenderer();

	protected CellView[] ports;

	public static transient GraphCellEditor cellEditor = new BusinessObjectEditor2();

	public GraphCellEditor getEditor() {
		return cellEditor;
	}

	/**
	 * Creates new instance of <code>InstanceView</code>.
	 */
	public PortLabelVertexView() {
		super();
	}

	/**
	 * Creates new instance of <code>InstanceView</code> for the specified
	 * graph cell.
	 * 
	 * @param arg0
	 *            a graph cell to create view for
	 */
	public PortLabelVertexView(Object arg0) {
		super(arg0);
	}

	public CellViewRenderer getRenderer() {
		return renderer;
	}

	public static class WrapperPortLabelRenderer extends JPanel implements
			CellViewRenderer {

		FlyweightUIComponent uiPanel = new FlyweightUIComponent(null);

		public Component getRendererComponent(JGraph graph, CellView view,
				boolean sel, boolean focus, boolean preview) {

			Object value = graph.getModel().getValue(view.getCell());
			if (value instanceof BusinessObjectWrapper2)
				uiPanel.installAttributes(value, view,
						false, graph);
			else {
				BusinessObjectWrapper2 wrapper = new BusinessObjectWrapper2();
				if (value instanceof DefaultMutableTreeNode)
					wrapper.setValue((DefaultMutableTreeNode) value);
				wrapper.setLabel(value.toString());
				uiPanel.installAttributes(wrapper, view, true, graph);
			}
			return uiPanel;
		}
	}

}