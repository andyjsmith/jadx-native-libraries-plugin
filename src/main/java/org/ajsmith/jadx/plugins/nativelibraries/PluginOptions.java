package org.ajsmith.jadx.plugins.nativelibraries;

import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.options.impl.BasePluginOptionsBuilder;

public class PluginOptions extends BasePluginOptionsBuilder {
	private final JadxPluginContext context;

	public PluginOptions(JadxPluginContext context) {
		super();
		this.context = context;
	}

	@Override
	public void registerOptions() {
	}
}
