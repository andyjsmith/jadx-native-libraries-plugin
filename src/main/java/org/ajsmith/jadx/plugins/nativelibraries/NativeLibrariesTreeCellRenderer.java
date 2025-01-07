package org.ajsmith.jadx.plugins.nativelibraries;

import org.ajsmith.jadx.plugins.nativelibraries.components.NativeMethod;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeObject;

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import java.awt.Component;

public class NativeLibrariesTreeCellRenderer extends DefaultTreeCellRenderer {

	/// Custom renderer to add icons and text formatting
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		TreeNode node = (TreeNode) value;
		if (!(node instanceof NativeObject)) return this;

		NativeObject nativeObject = (NativeObject) node;
		setIcon(nativeObject.getIcon());

		if (node instanceof NativeMethod && ((NativeMethod) node).getJavaMethod() == null) {
			// Give a visual indication to methods without a Java definition
			setText("<html><i>" + value + "</i></html>");
			setForeground(UIManager.getColor("textInactiveText"));
		}

		return this;
	}
}
