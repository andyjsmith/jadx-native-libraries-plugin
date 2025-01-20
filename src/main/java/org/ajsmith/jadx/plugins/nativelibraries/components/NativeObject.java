package org.ajsmith.jadx.plugins.nativelibraries.components;

import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.gui.JadxGuiContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;
import java.util.Objects;

public abstract class NativeObject implements TreeNode {
	protected static final String JAVA_PREFIX = "Java_";

	@Nullable
	public abstract ImageIcon getIcon();

	@NotNull
	public abstract String getName();

	/// Search up the tree to find an instance of the class
	private <T> @Nullable T findClassInTree(Class<T> clazz) {
		NativeObject cur = this;
		while (cur != null) {
			if (clazz.isInstance(cur)) {
				return clazz.cast(cur);
			}
			cur = cur.getParent();
		}
		return null;
	}

	@Nullable
	public NativeRoot getRoot() {
		return findClassInTree(NativeRoot.class);
	}

	@Nullable
	public NativeLibrary getLibrary() {
		return findClassInTree(NativeLibrary.class);
	}

	@Override
	public abstract NativeObject getParent();

	@Override
	public boolean isLeaf() {
		return getChildCount() == 0;
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	public @Nullable JadxPluginContext getContext() {
		if (getParent() == null) return null;
		return getParent().getContext();
	}

	public @NotNull JadxGuiContext getGuiContext() {
		return getContext().getGuiContext();
	}
}
