package org.ajsmith.jadx.plugins.nativelibraries;

import jadx.api.ResourceFile;
import jadx.api.ResourceType;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeLibrary;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeRoot;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import static org.ajsmith.jadx.plugins.nativelibraries.TestUtils.getSampleFile;
import static org.assertj.core.api.Assertions.assertThat;

public class NativeLibraryTest {
	@Test
	public void testImportNativeLibrary() throws URISyntaxException, IOException {
		File sampleFile = getSampleFile("libddgcrypto.so");
		ResourceFile resourceFile = ResourceFile.createResourceFile(null, sampleFile, ResourceType.LIB);
		NativeRoot root = new NativeRoot(null);
		root.loadFromResource(resourceFile);

		assertThat(root.getLibraries().length).isEqualTo(1);
		NativeLibrary lib = root.getLibraries()[0];

		assertThat(lib.getName()).isEqualTo(sampleFile.getAbsolutePath());
		assertThat(lib.toString()).isEqualTo(sampleFile.getAbsolutePath());
		assertThat(lib.getResourceFile()).isEqualTo(resourceFile);
		assertThat(lib.getBytes()).isEqualTo(Files.readAllBytes(sampleFile.toPath()));

		assertThat(lib.getPackageByName("com")).isNotNull();
	}
}
