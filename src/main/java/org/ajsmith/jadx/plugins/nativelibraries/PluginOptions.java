package org.ajsmith.jadx.plugins.nativelibraries;

import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.options.impl.BasePluginOptionsBuilder;
import jadx.gui.settings.JadxSettings;
import jadx.gui.ui.MainWindow;

public class PluginOptions extends BasePluginOptionsBuilder {
	private final JadxPluginContext context;

	public PluginOptions(JadxPluginContext context) {
		super();
		this.context = context;
	}

	@Override
	public void registerOptions() {
	}

	private void save() {
		if (context.getGuiContext() == null) return;
		JadxSettings settings = ((MainWindow) context.getGuiContext().getMainFrame()).getSettings();
		// save settings here
		settings.sync();
	}
}
