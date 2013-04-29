package com.jgraph.example.panelexample;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCellEditor;
import org.jgraph.graph.GraphCellEditor;

import com.jgraph.example.panelexample.FlyweightUIComponent;


/**
 * basic in-place editor for custom business objects
 */
public class BusinessObjectEditor2 extends DefaultGraphCellEditor {
	
	public static boolean editorInsideCell = true;
	
	/**
	 * Returns a new RealCellEditor.
	 */
	protected GraphCellEditor createGraphCellEditor() {
		return new RealCellEditor();
	}

	/**
	 * Utlitiy editor for rich text values.
	 */
	class RealCellEditor extends AbstractCellEditor implements
			GraphCellEditor {

		/**
		 * Holds the component used for editing.
		 */
		FlyweightUIComponent editorComponent = new FlyweightUIComponent(this);

		public Component getGraphCellEditorComponent(JGraph graph,
				Object value, boolean isSelected) {
			Object cell = value;
			value = graph.getModel().getValue(value);
			if (value instanceof BusinessObjectWrapper2)
				editorComponent.installAttributes((BusinessObjectWrapper2) value, graph.getGraphLayoutCache().getMapping(cell, false), true, graph);
			else {
				BusinessObjectWrapper2 wrapper = new BusinessObjectWrapper2();
				/*GraphModel model = graph.getModel();
				int childCount = model.getChildCount(cell);
				List result = new ArrayList(childCount);
				for (int i = 0; i < childCount; i++) {
					Object child = model.getChild(cell, i);
					if (model.isPort(child)) {
						CellView portView = graph.getGraphLayoutCache().getMapping(
								child, false);
						if (portView != null) {
							result.add(portView);
						}
					}
				}
				
				CellView[] ports = new CellView[result.size()];
				result.toArray(ports);
				wrapper.setPortviews(ports);*/
				if (value instanceof DefaultMutableTreeNode)
					wrapper.setValue((DefaultMutableTreeNode) value);
				wrapper.setLabel(value.toString());
				editorComponent.installAttributes(wrapper, graph.getGraphLayoutCache().getMapping(cell, false), true, graph);
			}
			Border aBorder = UIManager.getBorder("Tree.editorBorder");
			editorComponent.setBorder(aBorder);
			return editorComponent;
		}

		/**
		 * Returns the rich text value to be stored in the user object.
		 */
		public Object getCellEditorValue() {
			return editorComponent.getValue();
		}
	}
}

