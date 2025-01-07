package org.ajsmith.jadx.plugins.nativelibraries.components;

import jadx.api.ResourceFile;
import jadx.api.ResourcesLoader;
import jadx.core.utils.exceptions.JadxException;
import jadx.gui.utils.UiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class NativeLibrary extends NativeObject {
	private static final ImageIcon SO_ICON = UiUtils.openSvgIcon("nodes/binaryFile");
	private final String libraryName;
	private final NativeRoot root;
	private final List<NativePackage> packages = new ArrayList<>();
	private final ResourceFile resourceFile;

	public NativeLibrary(ResourceFile resourceFile, NativeRoot root) {
		this(resourceFile, root, resourceFile.getOriginalName());
	}

	public NativeLibrary(ResourceFile resourceFile, NativeRoot root, String libraryName) {
		this.resourceFile = resourceFile;
		this.libraryName = libraryName;
		this.root = root;
	}

	public ResourceFile getResourceFile() {
		return resourceFile;
	}

	public byte[] getBytes() throws IOException {
		try {
			return ResourcesLoader.decodeStream(getResourceFile(), (size, is) -> is.readAllBytes());
		} catch (JadxException e) {
			throw new IOException("Failed to decode resource file", e);
		}
	}

	public void saveToFile(String path) throws IOException {
		try (FileOutputStream stream = new FileOutputStream(path)) {
			stream.write(getBytes());
		}
	}

	/// Add a method under this root package by full JNI name. Creates subpackages and classes as needed.
	public NativeMethod addMethod(@NotNull String jniName) throws IllegalArgumentException {
		if (!jniName.startsWith(JAVA_PREFIX)) {
			throw new IllegalArgumentException("JNI method name must start with " + JAVA_PREFIX);
		}

		String rawMethod = jniName.substring(JAVA_PREFIX.length());

		String[] overloadParts = NativeMethod.getOverloadParts(rawMethod);
		String rawMethodName;
		String rawSignature = null;

		// Get and remove overloaded function signature
		boolean isOverloaded = overloadParts.length > 1;
		if (isOverloaded) {
			rawMethodName = overloadParts[0];
			rawSignature = overloadParts[1];
		} else {
			rawMethodName = rawMethod;
		}

		// Unescape and get name parts
		List<String> nameParts = List.of(NativeMethod.unescapeName(rawMethodName).split("/"));
		String methodName = nameParts.get(nameParts.size() - 1);
		String className = nameParts.get(nameParts.size() - 2);
		String packageName = String.join(".", nameParts.subList(0, nameParts.size() - 2));

		NativeClass cls = getOrAddClass(packageName, className);

		// Create method
		NativeMethod method = new NativeMethod(methodName, cls, rawSignature);
		cls.addMethod(method);
		return method;
	}

	/// Get a package by name. Not recursive.
	public @Nullable NativePackage getPackageByName(String name) {
		for (NativePackage pkg : packages) {
			if (pkg.getName().equals(name)) {
				return pkg;
			}
		}
		return null;
	}

	/// Get an existing subpackage inside a package, or create the package if it doesn't exist
	protected @NotNull NativePackage getOrAddPackage(@NotNull String name) {
		String[] packageParts = NativePackage.splitPackageName(name);

		// Create package
		NativePackage pkg = getPackageByName(packageParts[0]);
		if (pkg == null) {
			pkg = new NativePackage(packageParts[0], this);
			packages.add(pkg);
		}

		// Create subpackages
		for (int i = 1; i < packageParts.length; i++) {
			NativePackage subPkg = pkg.getSubPackageByName(packageParts[i]);
			if (subPkg == null) {
				subPkg = new NativePackage(packageParts[i], pkg);
				pkg.addSubPackage(subPkg);
			}
			pkg = subPkg;
		}

		return pkg;
	}

	/// Get an existing class inside the specified package, or create the class if it doesn't exist
	protected @NotNull NativeClass getOrAddClass(@NotNull String packageName, @NotNull String className) {
		NativePackage pkg = getOrAddPackage(packageName);

		// Create class
		NativeClass cls = pkg.getClassByName(className);
		if (cls == null) {
			cls = new NativeClass(className, pkg);
			pkg.addClass(cls);
		}
		return cls;
	}

	@Override
	public @NotNull String getName() {
		return libraryName;
	}

	public String toString() {
		return getName();
	}

	@Override
	public @NotNull ImageIcon getIcon() {
		return SO_ICON;
	}

	@Override
	public NativePackage getChildAt(int childIndex) {
		return packages.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return packages.size();
	}

	@Override
	public @Nullable NativeObject getParent() {
		return root;
	}

	@Override
	public int getIndex(TreeNode node) {
		if (!(node instanceof NativePackage)) return -1;
		return packages.indexOf((NativePackage) node);
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public Enumeration<? extends TreeNode> children() {
		return Collections.enumeration(packages);
	}
}
