package org.ajsmith.jadx.plugins.nativelibraries;

import jadx.core.utils.exceptions.DecodeException;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeClass;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeLibrary;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeMethod;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativePackage;
import org.ajsmith.jadx.plugins.nativelibraries.components.NativeRoot;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.ajsmith.jadx.plugins.nativelibraries.TestUtils.createMockLibrary;
import static org.ajsmith.jadx.plugins.nativelibraries.TestUtils.loadSampleFile;
import static org.assertj.core.api.Assertions.assertThat;

public class NativeMethodTest {

	@Test
	public void testNativeMethod() throws URISyntaxException, DecodeException {
		NativeRoot root = loadSampleFile("libddgcrypto.so");
		NativeLibrary lib = root.getLibraries()[0];
		NativePackage pkg = lib.getPackageByName("com");
		assertThat(pkg).isNotNull();
		assertThat(pkg.getChildAt(0)).isInstanceOf(NativeClass.class);
		NativeClass cls = (NativeClass) pkg.getChildAt(0);
		NativeMethod method = cls.getMethodByName("decrypt");
		assertThat(method).isNotNull().isInstanceOf(NativeMethod.class);

		assertThat(method.getName()).isEqualTo("decrypt");
		assertThat(method.getFullName()).isEqualTo("com.duckduckgo.sync.crypto.SyncNativeLib.decrypt");
		assertThat(method.isOverloaded()).isFalse();
		assertThat(method.getJavaMethod()).isNull(); // we aren't loading Java classes
		assertThat(method.getChildCount()).isEqualTo(0);
		assertThat(method.getParametersString()).isNull();
		assertThat(method.getParameters()).isNull();
	}

	@Test
	public void testNativeMethodName() {
		NativeLibrary lib = createMockLibrary();
		assertThat(lib.getName()).isEqualTo("lib.so");
		NativeMethod method = lib.addMethod("Java_com_a_b_c_Class_method");
		assertThat(method.getFullName()).isEqualTo("com.a.b.c.Class.method");
		assertThat(method.getName()).isEqualTo("method");
		assertThat(method.isOverloaded()).isFalse();
	}

	@Test
	public void testNativeMethodUnderscore() {
		NativeLibrary lib = createMockLibrary();
		NativeMethod method = lib.addMethod("Java_com_foo_bar_Baz_A_1Grill");
		assertThat(method.getCls().getPkg().getFullName()).isEqualTo("com.foo.bar");
		assertThat(method.getCls().getPkg().getName()).isEqualTo("bar");
		assertThat(method.getCls().getName()).isEqualTo("Baz");
		assertThat(method.getName()).isEqualTo("A_Grill");
	}

	@Test
	public void testNativeMethodSplitName() {
		String[] splitName = NativeMethod.splitName("com_a1_1b_c_0d_E");
		assertThat(splitName).isEqualTo(new String[]{"com", "a1_1b", "c_0d", "E"});
	}

	@Test
	public void testNativeMethodUnescapeName() {
		assertThat(NativeMethod.unescapeName("a1_9a")).isEqualTo("a1/9a");
		assertThat(NativeMethod.unescapeName("a1_1b")).isEqualTo("a1_b");
		assertThat(NativeMethod.unescapeName("a1_2b")).isEqualTo("a1;b");
		assertThat(NativeMethod.unescapeName("a1_3b")).isEqualTo("a1[b");
		assertThat(NativeMethod.unescapeName("a1_00041")).isEqualTo("a1A");
		assertThat(NativeMethod.unescapeName("a1_01e0b")).isEqualTo("a1ḋ");
		assertThat(NativeMethod.unescapeName("a1_026d4")).isEqualTo("a1⛔");
		assertThat(NativeMethod.unescapeName("a1_026d4_026d4_1_2_3__")).isEqualTo("a1⛔⛔_;[//");
		assertThat(NativeMethod.unescapeName("com_a_b_c__1d")).isEqualTo("com/a/b/c/_d");
	}
}
