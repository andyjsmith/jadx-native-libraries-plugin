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

public class NativePackage extends NativeObject {
	private final String name;
	private final List<NativeClass> classes = new ArrayList<>();
	private final @Nullable NativeObject parent;
	private final List<NativePackage> subPackages = new ArrayList<>();

	public NativePackage(@NotNull String packageName, @Nullable NativeObject parent) {
		this.name = packageName;
		this.parent = parent;
	}

	/// Split a package name into parts
	public static @NotNull String[] splitPackageName(@NotNull String path) {
		return path.split("\\.");
	}

	/// Get name of package, not including parent package(s).
	@Override
	public @NotNull String getName() {
		return name;
	}

	/// Get full name of package and parent(s), each separated by '.'
	public @NotNull String getFullName() {
		if (parent instanceof NativePackage) {
			return ((NativePackage) parent).getFullName() + "." + name;
		}
		return name;
	}

	/// Get classes in package. Not recursive.
	public @NotNull List<NativeClass> getClasses() {
		return classes;
	}

	/// Get class in package by class name. Not recursive.
	public @Nullable NativeClass getClassByName(@NotNull String name) {
		for (NativeClass c : classes) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		return null;
	}

	/// Add class to package
	public void addClass(@NotNull NativeClass c) {
		classes.add(c);
		classes.sort(Comparator.comparing(NativeClass::getName));
	}

	/// Get subpackages of this package. Not recursive.
	public @NotNull List<NativePackage> getSubPackages() {
		return subPackages;
	}

	/// Add a subpackage to this package
	public void addSubPackage(@NotNull NativePackage subPackage) {
		subPackages.add(subPackage);
		subPackages.sort((Comparator.comparing(NativePackage::getName)));
	}

	/// Get a subpackage by name. Not recursive.
	public @Nullable NativePackage getSubPackageByName(String name) {
		for (NativePackage subPackage : subPackages) {
			if (subPackage.getName().equals(name)) {
				return subPackage;
			}
		}
		return null;
	}

	public String toString() {
		return getNameCompacted();
	}

	public List<TreeNode> getChildren() {
		List<TreeNode> children = new ArrayList<>();
		children.addAll(getSubPackages());
		children.addAll(getClasses());
		return children;
	}

	protected boolean canBeCompacted() {
		return getSubPackages().size() == 1 && getClasses().isEmpty();
	}

	public List<TreeNode> getChildrenCompacted() {
		if (canBeCompacted()) {
			return getSubPackages().get(0).getChildrenCompacted();
		}
		return getChildren();
	}

	public String getNameCompacted() {
		if (canBeCompacted()) {
			return getName() + "." + getSubPackages().get(0).getNameCompacted();
		}
		return getName();
	}

	@Override
	public @NotNull ImageIcon getIcon() {
		return Icons.PACKAGE;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return getChildrenCompacted().get(childIndex);
	}

	@Override
	public int getChildCount() {
		return getChildrenCompacted().size();
	}

	@Override
	public @Nullable NativeObject getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		return getChildrenCompacted().indexOf(node);
	}

	@Override
	public Enumeration<? extends TreeNode> children() {
		return Collections.enumeration(getChildrenCompacted());
	}
}
