package com.jgraph.example.mycellmodeleditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractCellEditor;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCellEditor;
import org.jgraph.graph.GraphCellEditor;


/**
 * basic in-place editor for custom business objects
 */
public class BusinessObjectEditor extends DefaultGraphCellEditor {
	
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
		MyEditorComponent editorComponent = new MyEditorComponent(this);

		public Component getGraphCellEditorComponent(JGraph graph,
				Object value, boolean isSelected) {
			if (editorInsideCell) {
				Rectangle2D tmp = graph.getGraphLayoutCache().getMapping(value, false).getBounds();
				editorComponent.setPreferredSize(new Dimension((int) tmp.getWidth(), (int) tmp.getHeight()));
			}
			value = graph.getModel().getValue(value);
			if (value instanceof BusinessObjectWrapper)
				editorComponent.installValue((BusinessObjectWrapper) value);
			else {
				BusinessObjectWrapper wrapper = new BusinessObjectWrapper();
				if (value instanceof DefaultMutableTreeNode)
					wrapper.setValue((DefaultMutableTreeNode) value);
				wrapper.setLabel(value.toString());
				editorComponent.installValue(wrapper);
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

