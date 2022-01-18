package com.namelessmc.plugin.spigot;

import com.namelessmc.plugin.common.ApiProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class ApiProviderImpl extends ApiProvider {

	private @Nullable String apiUrl;
	private @Nullable String apiKey;
	private boolean debug;
	private boolean uuid;
	private int timeout;

	public ApiProviderImpl(final Logger logger) {
		super(logger);
	}

	void loadConfiguration(final FileConfiguration config) {
		this.apiUrl = config.getString("api.url");
		this.apiKey = config.getString("api.key");
		this.debug = config.getBoolean("api.debug", false);
		this.uuid = !config.getBoolean("api.usernames", false);
		this.timeout = config.getInt("api.timeout", 5000);
		this.clearCachedApi();
	}

	@Override
	protected @Nullable String getApiUrl() {
		return this.apiUrl;
	}

	@Override
	protected @Nullable String getApiKey() {
		return this.apiKey;
	}

	@Override
	protected boolean getDebug() {
		return this.debug;
	}

	@Override
	public boolean useUuids() {
		return this.uuid;
	}

	@Override
	protected int getTimeout() {
		return this.timeout;
	}

}
