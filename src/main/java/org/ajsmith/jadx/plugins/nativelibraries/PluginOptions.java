package org.ajsmith.jadx.plugins.nativelibraries;

import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.options.impl.BasePluginOptionsBuilder;
import jadx.gui.settings.JadxSettings;
import jadx.gui.ui.MainWindow;

public class PluginOptions extends BasePluginOptionsBuilder {
	JadxPluginContext context;
	private String ghidraPath;

	public PluginOptions(JadxPluginContext context) {
		super();
		this.context = context;
	}

	@Override
	public void registerOptions() {
		strOption(NativeLibrariesPlugin.PLUGIN_ID + ".ghidraPath")
				.description("Ghidra path")
				.defaultValue("")
				.setter(v -> ghidraPath = v);
	}

	public String getGhidraPath() {
		return ghidraPath;
	}

	public void setGhidraPath(String ghidraPath) {
		this.ghidraPath = ghidraPath;
		save();
	}

	private void save() {
		if (context.getGuiContext() == null) return;
		JadxSettings settings = ((MainWindow) context.getGuiContext().getMainFrame()).getSettings();
		settings.getPluginOptions().put(NativeLibrariesPlugin.PLUGIN_ID + ".ghidraPath", ghidraPath);
		settings.sync();
	}
}
