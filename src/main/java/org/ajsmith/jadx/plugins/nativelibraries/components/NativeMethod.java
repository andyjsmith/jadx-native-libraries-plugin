package org.ajsmith.jadx.plugins.nativelibraries.components;

import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import jadx.api.JavaPackage;
import jadx.core.utils.exceptions.DecodeException;
import jadx.gui.utils.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NativeMethod extends NativeObject {
	private static final Logger LOG = LoggerFactory.getLogger(NativeMethod.class);
	private final boolean isOverloaded;
	private final String name;
	private final NativeClass parent;
	private @Nullable String signature;
	final @Nullable JavaMethod javaMethod;
	private @Nullable List<String> parameters;

	public NativeMethod(@NotNull String name, NativeClass parent, @Nullable String rawSignature) throws IllegalArgumentException {
		this.parent = parent;
		this.name = name;

		if (rawSignature != null) {
			isOverloaded = true;
			signature = unescapeName(rawSignature);
		} else {
			isOverloaded = false;
		}

		try {
			parameters = parseParameters();
		} catch (DecodeException e) {
			LOG.warn(e.getMessage());
		}

		javaMethod = findJavaMethod();
	}

	public static @NotNull String[] getOverloadParts(@NotNull String jniName) {
		return jniName.split("__(?!\\d)", 2);
	}

	public static String @NotNull [] splitName(@NotNull String name) {
		return name.split(Pattern.compile("_(?!\\d)").pattern());
	}

	@NotNull
	public static String unescapeName(@NotNull String name) throws IndexOutOfBoundsException {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c != '_') {
				result.append(c);
				continue;
			}
			if (i == name.length() - 1) {
				result.append('/');
				break;
			}
			char d = name.charAt(i + 1);
			if (d == '1') {
				result.append('_');
				i++;
				continue;
			}
			if (d == '2') {
				result.append(';');
				i++;
				continue;
			}
			if (d == '3') {
				result.append('[');
				i++;
				continue;
			}
			if (d == '0') {
				// Unicode
				if (i + 5 >= name.length()) {
					throw new IndexOutOfBoundsException("Unicode hex string missing digits in name: " + name);
				}
				String hexCodeUnit = name.substring(i + 2, i + 6);
				byte[] codeUnit = hexStringToByteArray(hexCodeUnit);
				result.append(new String(codeUnit, StandardCharsets.UTF_16));
				i += 5;
				continue;
			}
			result.append('/');
		}
		return result.toString();
	}

	public static byte @NotNull [] hexStringToByteArray(@NotNull String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/// Given a native method's name, determine if it is a Java method (starts with 'Java_')
	public static boolean isJavaMethod(@Nullable String name) {
		if (name == null) return false;
		return name.startsWith(JAVA_PREFIX);
	}

	public boolean isOverloaded() {
		return isOverloaded;
	}

	@Override
	public @NotNull String getName() {
		return name;
	}

	public @NotNull String getFullName() {
		return parent.getFullName() + "." + name;
	}

	public @Nullable JavaMethod getJavaMethod() {
		return javaMethod;
	}

	/// Find the decompiled JavaMethod corresponding to this native method, or null if not found
	@Nullable
	private JavaMethod findJavaMethod() {
		if (getContext() == null) return null;
		List<JavaPackage> packages = getContext().getDecompiler().getPackages();

		JavaPackage pkg = packages.stream().filter(p -> p.getRawFullName().equals(getCls().getPkg().getFullName())).findFirst().orElse(null);
		if (pkg == null) {
			LOG.debug("Package {} not found", getCls().getPkg().getFullName());
			return null;
		}
		JavaClass cls = pkg.getClasses().stream().filter(c -> c.getRawName().equals(getCls().getFullName())).findFirst().orElse(null);
		if (cls == null) {
			LOG.debug("Class {} not found", getCls().getFullName());
			return null;
		}
		List<JavaMethod> methods = cls.getMethods().stream().filter(m -> m.getFullName().equals(getFullName())).collect(Collectors.toList());
		if (methods.isEmpty()) {
			LOG.debug("Method {} not found", getFullName());
			return null;
		}

		if (getParameters() == null) {
			return methods.get(0);
		}

		for (JavaMethod method : methods) {
			if (method.getArguments().size() != getParameters().size()) {
				continue;
			}

			boolean match = true;
			for (int i = 0; i < method.getArguments().size(); i++) {
				String javaParam = method.getArguments().get(i).toString();
				String nativeParam = getParameters().get(i);

				// TODO: Naive string comparison, but seems to work fine
				if (!javaParam.equals(nativeParam)) {
					match = false;
					break;
				}
			}
			if (match) {
				return method;
			}
		}
		return null;
	}

	@Nullable
	public List<String> getParameters() {
		return parameters;
	}

	/// Parse the parameters in the function signature if they're specified (happens when method is overloaded)
	@Nullable
	private List<String> parseParameters() throws DecodeException {
		if (signature == null) return null;

		List<String> parameters = new ArrayList<>();
		int arrayLevel = 0;
		StringBuilder parameter = new StringBuilder();
		for (int i = 0; i < signature.length(); i++) {
			char c = signature.charAt(i);
			switch (c) {
				case 'Z': // boolean
					parameter.append("boolean");
					break;
				case 'B': // byte
					parameter.append("byte");
					break;
				case 'C': // char
					parameter.append("char");
					break;
				case 'S': // short
					parameter.append("short");
					break;
				case 'I': // int
					parameter.append("int");
					break;
				case 'J': // long
					parameter.append("long");
					break;
				case 'F': // float
					parameter.append("float");
					break;
				case 'D': // double
					parameter.append("double");
					break;
				case 'V': // void
					parameter.append("void");
					break;
				case 'L': // class
					int classEnd = signature.indexOf(';', i + 1);
					if (classEnd == -1) {
						throw new DecodeException("Class in method signature not terminated");
					}

					parameter.append(signature.substring(i + 1, classEnd).replace('/', '.'));
					i = classEnd;
					break;
				case '[': // array
					arrayLevel++;
					continue;
				default:
					throw new DecodeException("Unrecognized character in method signature: " + c);
			}

			parameter.append("[]".repeat(Math.max(0, arrayLevel)));

			parameters.add(parameter.toString());
			parameter.setLength(0);
			arrayLevel = 0;
		}

		return parameters;
	}

	/// Join the parameters into a single string, or null if there are none
	@Nullable
	public String getParametersString() {
		List<String> parameters = getParameters();
		if (parameters == null || parameters.isEmpty()) return null;

		return String.join(", ", parameters);
	}

	public @NotNull NativeClass getCls() {
		return parent;
	}

	/// Get the name of the method and the function signature if present
	public String toString() {
		String name = getName();
		if (signature != null) {
			name += "(" + getParametersString() + ")";
		}
		return name;
	}

	@Override
	public @Nullable ImageIcon getIcon() {
		return Icons.METHOD;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return null;
	}

	@Override
	public int getChildCount() {
		return 0;
	}

	@Override
	public NativeObject getParent() {
		return getCls();
	}

	@Override
	public int getIndex(TreeNode node) {
		return -1;
	}

	@Override
	public boolean getAllowsChildren() {
		return false;
	}

	@Override
	public Enumeration<? extends TreeNode> children() {
		return null;
	}
}
