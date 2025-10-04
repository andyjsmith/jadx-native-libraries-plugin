package org.ajsmith.jadx.plugins.nativelibraries;

import jadx.api.plugins.JadxPlugin;
import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.JadxPluginInfo;
import jadx.api.plugins.JadxPluginInfoBuilder;
import jadx.api.plugins.gui.JadxGuiContext;

public class NativeLibrariesPlugin implements JadxPlugin {
	public static final String PLUGIN_ID = "native-libraries-plugin";
	private static final String DESCRIPTION = "Get information about the native JNI (.so) libraries in the APK.\n\n" +
			"View all of the methods defined in the native libraries included in the APK.\n" +
			"Jump to each method's Java definition and find usages.";

	private PluginOptions options;

	@Override
	public JadxPluginInfo getPluginInfo() {
		return JadxPluginInfoBuilder.pluginId(PLUGIN_ID)
				.name("Native Library Info")
				.description(DESCRIPTION)
				.homepage("https://github.com/andyjsmith/jadx-native-libraries-plugin")
				.requiredJadxVersion("1.5.2, r2472")
				.build();
	}

	@Override
	public void init(JadxPluginContext context) {
		options = new PluginOptions(context);
		context.registerOptions(options);
		JadxGuiContext guiContext = context.getGuiContext();
		if (guiContext == null) return;
		guiContext.addMenuAction("Native Libraries", () -> new PluginDialog(guiContext.getMainFrame(), context, options).showModal());
	}
}
