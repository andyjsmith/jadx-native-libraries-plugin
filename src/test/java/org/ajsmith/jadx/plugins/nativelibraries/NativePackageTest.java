package org.ajsmith.jadx.plugins.nativelibraries;

import org.ajsmith.jadx.plugins.nativelibraries.components.NativeClass;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeLibrary;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativePackage;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeRoot;
import org.junit.jupiter.api.Test;

import javax.swing.tree.TreeNode;
import java.net.URISyntaxException;
import java.util.List;

import static org.ajsmith.jadx.plugins.nativelibraries.TestUtils.loadSampleFile;
import static org.assertj.core.api.Assertions.assertThat;

public class NativePackageTest {
	@Test
	public void testNativePackage() throws URISyntaxException {
		NativeRoot root = loadSampleFile("libddgcrypto.so");
		NativeLibrary lib = root.getLibraries()[0];

		NativePackage pkg = lib.getPackageByName("com");

		assertThat(pkg).isNotNull();
		assertThat(pkg.getName()).isEqualTo("com");
		assertThat(pkg.getFullName()).isEqualTo("com");
		assertThat(pkg.getSubPackages().size()).isEqualTo(1);
		assertThat(pkg.getSubPackageByName("duckduckgo")).isInstanceOf(NativePackage.class);

		// Test package compaction
		assertThat(pkg.getNameCompacted()).isEqualTo("com.duckduckgo.sync.crypto");
		assertThat(pkg.toString()).isEqualTo(pkg.getNameCompacted());
		assertThat(pkg.getChildCount()).isEqualTo(1);

		List<TreeNode> children = pkg.getChildren();
		assertThat(children.size()).isEqualTo(pkg.getChildCount()).isGreaterThan(0);
		assertThat(children.get(0)).isInstanceOf(NativePackage.class);

		List<TreeNode> childrenCompacted = pkg.getChildrenCompacted();
		assertThat(childrenCompacted.size()).isEqualTo(pkg.getChildCount()).isGreaterThan(0);
		assertThat(childrenCompacted.get(0)).isInstanceOf(NativeClass.class);

		NativePackage classParent = (NativePackage) childrenCompacted.get(0).getParent();
		assertThat(classParent).isNotNull();
		assertThat(classParent.getFullName()).isEqualTo("com.duckduckgo.sync.crypto");
		assertThat(classParent.getSubPackages().size()).isEqualTo(0);
		assertThat(classParent.getClasses().size()).isEqualTo(1);
		assertThat(classParent.getClassByName("SyncNativeLib")).isInstanceOf(NativeClass.class);
		assertThat(classParent.getClassByName("SyncNativeLib")).isEqualTo(classParent.getClasses().get(0));
	}
}
