package com.jgraph.example.panelexample;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;

import javax.swing.CellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

/**
 * A basic example JComponent to in place edit both the cell model (user object)
 * and the cell label. First of all you have the opportunity to edit the cell
 * label. Also you can choose the cell user object inside a JTree. A JTree is
 * fine to allow the user to choose within a hierarchy, but you could have
 * choosen a combo box instead for instance. You can choose whether you want to
 * use the label feature or if you rather prefer to use the toString method of
 * the user object instead. Finally you can confirm or cancel your choice.
 * 
 * @author rvalyi
 */
public class FlyweightUIComponent extends JPanel implements ActionListener,
		ItemListener, TreeSelectionListener {
	private JPanel labelPanel = new JPanel();

	private JPanel modelPanel = new JPanel();

	private JPanel actionPanel = new JPanel();

	private JButton okButton = new JButton("OK");

	private JButton cancelButton = new JButton("Cancel");

	private JCheckBox useModelLabel = new JCheckBox("use model label");

	private JTextField labelField = new JTextField();

	private JTree chooserTree = new JTree();

	public JPanel east = new JPanel(new GridBagLayout());

	public JPanel west = new JPanel();

	public JPanel north = new JPanel();

	public JPanel south = new JPanel();

	public JPanel center = new JPanel();

	public static boolean editorInsideCell = true;

	/**
	 * A temporary clone of the business object to work with before commiting
	 * the change (allows to undo).
	 */
	private BusinessObjectWrapper2 newModel;

	/**
	 * the old user object
	 */
	private BusinessObjectWrapper2 oldModel;

	/**
	 * a reference to the editor: here we only use it to force the end of the
	 * editing after the OK button or Cancel button is pressed
	 */
	private static CellEditor cellEditor;

	public FlyweightUIComponent(CellEditor editor) {
		super(new BorderLayout());
		cellEditor = editor;
		labelField.setColumns(10);
		labelPanel.add(labelField);
		labelPanel.add(useModelLabel);
		modelPanel.add(chooserTree);
		actionPanel.add(okButton);
		actionPanel.add(cancelButton);
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		okButton.setActionCommand("ok");
		cancelButton.setActionCommand("cancel");
		center.setLayout(new BorderLayout());
		center.add(labelPanel, BorderLayout.NORTH);
		center.add(new JScrollPane(modelPanel), BorderLayout.CENTER);
		center.add(actionPanel, BorderLayout.SOUTH);
		this.add(center, BorderLayout.CENTER);
		chooserTree.addTreeSelectionListener(this);
		chooserTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		useModelLabel.addItemListener(this);
	}
	
	 public void paint(Graphics g) {
		 super.paint(g);
	 }

	/**
	 * the editor is lightweight so you need to install the graph cell
	 * properties before using it.
	 * 
	 * @param value
	 */
	private void installValue(BusinessObjectWrapper2 value, boolean isEditing) {
		oldModel = value;
		newModel = value;
		labelField.setText(new String(newModel.getLabel()));
		labelField.setEnabled(true);
		useModelLabel.setSelected(false);
		chooserTree.setSelectionPath(null);

		if (newModel.getValue() != null)
			chooserTree.setSelectionPath(new TreePath(
					((DefaultTreeModel) chooserTree.getModel())
							.getPathToRoot(newModel.getValue())));
	}

	protected void installAttributes(Object value, CellView view,
			boolean isEditing, JGraph graph) {
		if (editorInsideCell && view.getBounds() != null)
			setPreferredSize(new Dimension((int) view.getBounds().getWidth(),
					(int) view.getBounds().getHeight()));
		if (value instanceof BusinessObjectWrapper2)
			installValue((BusinessObjectWrapper2) value, true);
		else {
			BusinessObjectWrapper2 wrapper = new BusinessObjectWrapper2();
			if (value instanceof DefaultMutableTreeNode)
				wrapper.setValue((DefaultMutableTreeNode) value);
			wrapper.setLabel(value.toString());
			installValue(wrapper, true);
		}
		
		this.remove(east);
		this.remove(west);
		this.remove(north);
		this.remove(south);
		
		east = new JPanel(new GridBagLayout());
		west = new JPanel(new GridBagLayout());
		north = new JPanel(new GridBagLayout());
		south = new JPanel(new GridBagLayout());
		

		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 1.0;
		c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 0;
		
		GraphModel model = graph.getModel();
		int childCount = model.getChildCount(view.getCell());
		for (int i = 0; i < childCount; i++) {
			Object child = model.getChild(view.getCell(), i);
			if (model.isPort(child)) {
				CellView portView = graph.getGraphLayoutCache().getMapping(
						child, false);
				if (portView != null) {
					Point2D point = GraphConstants.getOffset(portView.getAllAttributes());
					String label = (String) model.getValue(portView.getCell());
					JComponent portComponent;
					if (isEditing)
						portComponent = new JTextField(label);
					else
						portComponent = new JLabel(label);
					portComponent.setPreferredSize(new Dimension(30, 20));
					if (point.getX() == 0) {//left port
						if (point.getY() == 100) {
							c.anchor = GridBagConstraints.FIRST_LINE_START;
						}
						else if (point.getY() == 500) {
							c.anchor = GridBagConstraints.LINE_START;
						}
						else {
							c.anchor = GridBagConstraints.LAST_LINE_START;
						}
						west.add(portComponent, c);
					}
					else if (point.getX() == GraphConstants.PERMILLE) {//right port
						if (point.getY() == 100) {
							c.anchor = GridBagConstraints.FIRST_LINE_END;
						}
						else if (point.getY() == 500) {
							c.anchor = GridBagConstraints.LINE_END;
						}
						else {
							c.anchor = GridBagConstraints.LAST_LINE_END;
						}
						east.add(portComponent, c);
					}
				}
			}
		}

		this.add(east, BorderLayout.EAST);
		this.add(north, BorderLayout.NORTH);
		this.add(west, BorderLayout.WEST);
		this.add(south, BorderLayout.SOUTH);



		/*JButton button = new JButton("P1");
		c.weighty = 1.0; // request any extra vertical space
		c.anchor = GridBagConstraints.FIRST_LINE_END; // bottom of space
        c.gridx = 0;
        c.gridy = 0;
		east.add(button, c);
		
		JButton button2 = new JButton("P2");
		c.anchor = GridBagConstraints.LINE_END; // bottom of space
		east.add(button2, c);
		
		JButton button3 = new JButton("P3");
		c.anchor = GridBagConstraints.LAST_LINE_END; // bottom of space
		east.add(button3, c);
		

		
		this.remove(north);
		north = new JPanel(new GridBagLayout());
		JButton button4 = new JButton("P1");
		c.weightx = 1.0; // request any extra vertical space
		c.anchor = GridBagConstraints.FIRST_LINE_START; // bottom of space
        c.gridx = 0;
        c.gridy = 0;
        north.add(button4, c);
		
		JButton button5 = new JButton("P2");
		c.anchor = GridBagConstraints.PAGE_START; // bottom of space
		north.add(button5, c);
		
		JButton button6 = new JButton("P3");
		c.anchor = GridBagConstraints.FIRST_LINE_END; // bottom of space
		north.add(button6, c);*/
		

	}

	public BusinessObjectWrapper2 getValue() {
		return oldModel;
	}

	/**
	 * Unique action entry point dispatching several component specific actions
	 */
	public void actionPerformed(ActionEvent e) {
		if ("ok".equals(e.getActionCommand())) {
			oldModel = newModel;
			oldModel.setLabel(labelField.getText());
			cellEditor.stopCellEditing();
		} else {
			cellEditor.stopCellEditing();
		}
	}

	/**
	 * the tree selection listener
	 */
	public void valueChanged(TreeSelectionEvent e) {
		if (chooserTree.isSelectionEmpty())
			return;
		TreePath[] treeSelection = chooserTree.getSelectionModel()
				.getSelectionPaths();
		newModel.setValue((DefaultMutableTreeNode) treeSelection[0]
				.getLastPathComponent());
		if (!useModelLabel.isSelected()) {
			newModel.setLabel(treeSelection[0].getLastPathComponent()
					.toString());
			labelField.setText(newModel.toString());
		}
	}

	/**
	 * the checkbox listener
	 */
	public void itemStateChanged(ItemEvent e) {
		if (!useModelLabel.isSelected()) {
			if (newModel.getValue() != null)
				labelField.setText(newModel.getValue().toString());
			labelField.setEnabled(false);
		} else {
			labelField.setEnabled(true);
			labelPanel.requestFocus();
		}
	}

}
