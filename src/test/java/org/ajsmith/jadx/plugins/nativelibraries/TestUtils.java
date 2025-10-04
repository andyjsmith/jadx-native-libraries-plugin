package org.ajsmith.jadx.plugins.nativelibraries;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.ResourceFile;
import jadx.api.ResourceType;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeLibrary;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeRoot;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {
	protected static NativeLibrary createMockLibrary() {
		JadxArgs args = new JadxArgs();
		JadxDecompiler jadx = new JadxDecompiler(args);
		return new NativeLibrary(ResourceFile.createResourceFile(jadx, "lib.so", ResourceType.LIB), null);
	}

	protected static File getSampleFile(String fileName) throws URISyntaxException {
		URL file = TestUtils.class.getClassLoader().getResource("samples/" + fileName);
		assertThat(file).isNotNull();
		return new File(file.toURI());
	}

	protected static NativeRoot loadSampleFile(String fileName) throws URISyntaxException {
		File sampleFile = getSampleFile(fileName);
		NativeRoot root = new NativeRoot(null);
		ResourceFile resourceFile = ResourceFile.createResourceFile(null, sampleFile, ResourceType.LIB);
		root.loadFromResource(resourceFile);
		return root;
	}
}
