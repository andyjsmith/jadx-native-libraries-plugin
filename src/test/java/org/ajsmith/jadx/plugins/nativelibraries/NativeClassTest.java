package org.ajsmith.jadx.plugins.nativelibraries;

import org.ajsmith.jadx.plugins.nativelibraries.components.NativeClass;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeLibrary;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeMethod;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativePackage;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeRoot;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.ajsmith.jadx.plugins.nativelibraries.TestUtils.loadSampleFile;
import static org.assertj.core.api.Assertions.assertThat;

public class NativeClassTest {
	@Test
	public void testNativeClass() throws URISyntaxException {
		NativeRoot root = loadSampleFile("libddgcrypto.so");
		NativeLibrary lib = root.getLibraries()[0];
		NativePackage pkg = lib.getPackageByName("com");
		assertThat(pkg).isNotNull();
		assertThat(pkg.getChildAt(0)).isInstanceOf(NativeClass.class);

		NativeClass cls = (NativeClass) pkg.getChildAt(0);
		assertThat(cls.getPkg()).isEqualTo(cls.getParent());
		assertThat(cls.getName()).isEqualTo("SyncNativeLib");
		assertThat(cls.getFullName()).isEqualTo("com.duckduckgo.sync.crypto.SyncNativeLib");
		assertThat(cls.getChildCount()).isEqualTo(cls.getMethods().size()).isEqualTo(16);
		assertThat(cls.getMethodByName("decrypt")).isInstanceOf(NativeMethod.class);
		assertThat(cls.getMethodByName("decrypt")).isInstanceOf(NativeMethod.class);

		assertThat(cls.getRoot()).isEqualTo(root);
		assertThat(cls.getLibrary()).isEqualTo(lib);
	}
}
