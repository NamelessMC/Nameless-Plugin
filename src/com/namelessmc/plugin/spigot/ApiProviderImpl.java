package com.namelessmc.plugin.spigot;

import org.bukkit.configuration.file.FileConfiguration;

import com.namelessmc.plugin.common.ApiProvider;

public class ApiProviderImpl extends ApiProvider {

	private String apiUrl;
	private boolean debug;

	void loadConfiguration(final FileConfiguration config) {
		this.apiUrl = config.getString("api-url");
		this.debug = config.getBoolean("api-debug-mode", false);
		this.clearCachedApi();
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
