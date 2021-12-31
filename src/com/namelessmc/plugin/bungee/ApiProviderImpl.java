package com.namelessmc.plugin.bungee;

import java.util.logging.Logger;

import com.namelessmc.plugin.common.ApiProvider;

import net.md_5.bungee.config.Configuration;

public class ApiProviderImpl extends ApiProvider {

	public ApiProviderImpl(final Logger logger) {
		super(logger);
	}

	private String apiUrl;
	private boolean debug;
	private int timeout;

	public void loadConfiguration(final Configuration config) {
		this.apiUrl = config.getString("api.url");
		this.debug = config.getBoolean("api.debug-mode", false);
		this.timeout = config.getInt("api.timeout", 5000);
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
		return false;
	}

	@Override
	protected int getTimeout() {
		return this.timeout;
	}

}
