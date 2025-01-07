package org.ajsmith.jadx.plugins.nativelibraries.components;

import jadx.api.ResourceFile;
import jadx.api.ResourceType;
import jadx.api.plugins.JadxPluginContext;
import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

public class NativeRoot extends NativeObject {
	private static final Logger LOG = LoggerFactory.getLogger(NativeRoot.class);
	private final List<NativeLibrary> libraries = new ArrayList<>();

	private final JadxPluginContext context;

	public NativeRoot(final JadxPluginContext context) {
		this.context = context;
	}

	public void loadFromResource(ResourceFile resource) {
		if (!resource.getType().equals(ResourceType.LIB)) return;

		LOG.debug("Library: {}", resource.getDeobfName());
		NativeLibrary library = new NativeLibrary(resource, this);
		addLibrary(library);

		byte[] bytes;
		try {
			bytes = library.getBytes();
		} catch (IOException e) {
			LOG.error("Error decoding resource", e);
			return;
		}

		// Parse symbols to find exported methods
		ElfFile elf = ElfFile.from(bytes);
		for (ElfSymbol symbol : elf.getDynamicSymbolTableSection().symbols) {
			String name = symbol.getName();
			if (!NativeMethod.isJavaMethod(name)) continue;

			LOG.debug("Symbol: {}", name);

			NativeMethod nativeMethod = library.addMethod(name);
			LOG.debug("Package: {}, Class: {}, Method: {}", nativeMethod.getCls().getPkg().getName(), nativeMethod.getCls().getName(), nativeMethod.getName());
		}
	}

	public void loadFromResources() {
		for (ResourceFile resource : context.getDecompiler().getResources()) {
			loadFromResource(resource);
		}
	}

	public void addLibrary(NativeLibrary lib) {
		libraries.add(lib);
		libraries.sort(Comparator.comparing(NativeLibrary::getName));
	}

	public NativeLibrary[] getLibraries() {
		return libraries.toArray(new NativeLibrary[0]);
	}

	@Override
	public JadxPluginContext getContext() {
		return context;
	}

	@Override
	@Nullable
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public @NotNull String getName() {
		return "<root>";
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return libraries.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return libraries.size();
	}

	@Override
	public NativeObject getParent() {
		return null;
	}

	@Override
	public int getIndex(TreeNode node) {
		if (!(node instanceof NativeLibrary)) return -1;
		return libraries.indexOf(node);
	}

	@Override
	public Enumeration<? extends TreeNode> children() {
		return Collections.enumeration(libraries);
	}
}
