package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.ApiProvider;

import net.md_5.bungee.config.Configuration;

public class ApiProviderImpl extends ApiProvider {

	private String apiUrl;
	private boolean debug;

	public void loadConfiguration(final Configuration config) {
		this.apiUrl = config.getString("api-url");
		this.debug = config.getBoolean("api-debug-mode", false);
	}

	@Override
	protected String getApiUrl() {
		return this.apiUrl;
	}

	@Override
	protected boolean getDebug() {
		return this.debug;
	}

}
