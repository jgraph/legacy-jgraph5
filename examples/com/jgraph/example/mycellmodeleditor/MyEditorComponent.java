package com.jgraph.example.mycellmodeleditor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.CellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
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
public class MyEditorComponent extends JComponent implements ActionListener,
		ItemListener, TreeSelectionListener {
	private static JPanel labelPanel = new JPanel();

	private static JPanel modelPanel = new JPanel();

	private static JPanel actionPanel = new JPanel();

	private static JButton okButton = new JButton("OK");

	private static JButton cancelButton = new JButton("Cancel");

	private static JCheckBox useModelLabel = new JCheckBox("use model label");

	private static JTextField labelField = new JTextField();

	private static JTree chooserTree = new JTree();

	/**
	 * A temporary clone of the business object to work with before commiting
	 * the change (allows to undo).
	 */
	private static BusinessObjectWrapper newModel;

	/**
	 * the old user object
	 */
	private static BusinessObjectWrapper oldModel;

	/**
	 * a reference to the editor: here we only use it to force the end of the
	 * editing after the OK button or Cancel button is pressed
	 */
	public static CellEditor cellEditor;

	public MyEditorComponent(CellEditor cellEditor) {
		MyEditorComponent.cellEditor = cellEditor;
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
		this.setLayout(new BorderLayout());
		this.add(labelPanel, BorderLayout.NORTH);
		this.add(new JScrollPane(modelPanel), BorderLayout.CENTER);
		this.add(actionPanel, BorderLayout.SOUTH);
		chooserTree.addTreeSelectionListener(this);
		chooserTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		useModelLabel.addItemListener(this);
	}

	/**
	 * the editor is lightweight so you need to install the graph cell
	 * properties before using it.
	 * 
	 * @param value
	 */
	public void installValue(BusinessObjectWrapper value) {
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

	public BusinessObjectWrapper getValue() {
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
