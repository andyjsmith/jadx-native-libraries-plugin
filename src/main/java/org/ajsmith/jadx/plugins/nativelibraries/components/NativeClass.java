package org.ajsmith.jadx.plugins.nativelibraries.components;

import jadx.gui.utils.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

public class NativeClass extends NativeObject {
	private final NativePackage parent;
	private final String name;

	private final List<NativeMethod> methods = new ArrayList<>();

	public NativeClass(@NotNull String name, @NotNull NativePackage parent) {
		this.name = name;
		this.parent = parent;
	}

	public @NotNull NativePackage getPkg() {
		return parent;
	}

	@Override
	public @NotNull String getName() {
		return name;
	}

	public @NotNull String getFullName() {
		return parent.getFullName() + "." + name;
	}

	public void addMethod(@NotNull NativeMethod method) {
		methods.add(method);
		methods.sort((Comparator.comparing(NativeMethod::getName)));
	}

	public @NotNull List<NativeMethod> getMethods() {
		return methods;
	}

	public @Nullable NativeMethod getMethodByName(@NotNull String name) {
		for (NativeMethod method : methods) {
			if (name.equals(method.getName())) {
				return method;
			}
		}
		return null;
	}

	public String toString() {
		return getName();
	}


	@Override
	public @NotNull ImageIcon getIcon() {
		return Icons.CLASS;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return getMethods().get(childIndex);
	}

	@Override
	public int getChildCount() {
		return getMethods().size();
	}

	@Override
	public @NotNull NativePackage getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		if (!(node instanceof NativeMethod)) return -1;
		return getMethods().indexOf((NativeMethod) node);
	}

	@Override
	public Enumeration<? extends TreeNode> children() {
		return Collections.enumeration(getMethods());
	}
}
