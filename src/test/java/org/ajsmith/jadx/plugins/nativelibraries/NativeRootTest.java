package org.ajsmith.jadx.plugins.nativelibraries;

import jadx.api.ResourceFile;
import jadx.api.ResourceType;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeLibrary;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeRoot;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.ajsmith.jadx.plugins.nativelibraries.TestUtils.getSampleFile;
import static org.assertj.core.api.Assertions.assertThat;

public class NativeRootTest {
	@Test
	public void testNativeRootImport() throws URISyntaxException, IOException {
		File sampleFile = getSampleFile("libddgcrypto.so");
		NativeRoot root = new NativeRoot(null);
		ResourceFile resourceFile = ResourceFile.createResourceFile(null, sampleFile, ResourceType.LIB);
		root.loadFromResource(resourceFile);

		assertThat(root.getName()).isEqualTo("<root>");
		assertThat(root.getRoot()).isEqualTo(root);
		assertThat(root.getChildAt(0)).isInstanceOf(NativeLibrary.class);
		assertThat(root.getChildCount()).isEqualTo(1);
		assertThat(root.getParent()).isNull();
		assertThat(root.getLibrary()).isNull();
		assertThat(root.getAllowsChildren()).isTrue();
		assertThat(root.isLeaf()).isFalse();
	}
}
