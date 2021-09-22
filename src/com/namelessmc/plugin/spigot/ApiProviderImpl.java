package com.namelessmc.plugin.spigot;

import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

import com.namelessmc.plugin.common.ApiProvider;

public class ApiProviderImpl extends ApiProvider {

	private String apiUrl;
	private boolean debug;
	private boolean uuid;

	public ApiProviderImpl(final Logger logger) {
		super(logger);
	}

	void loadConfiguration(final FileConfiguration config) {
		this.apiUrl = config.getString("api-url");
		this.debug = config.getBoolean("api-debug-mode", false);
		this.uuid = !config.getBoolean("api-usernames", false);
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

	@Override
	public boolean useUuids() {
		return this.uuid;
	}

}
