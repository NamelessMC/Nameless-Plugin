package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.ApiProvider;
import com.namelessmc.plugin.common.ExceptionLogger;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class ApiProviderImpl extends ApiProvider {

	public ApiProviderImpl(final Logger logger) {
		super(logger);
	}

	private @Nullable String apiUrl;
	private @Nullable String apiKey;
	private boolean debug;
	private int timeout;
	private boolean bypassVersionCheck;

	public void loadConfiguration(final @NotNull Configuration config, final @NotNull ExceptionLogger exceptionLogger) {
		super.exceptionLogger = exceptionLogger;

		this.apiUrl = config.getString("api.url");
		this.apiKey = config.getString("api.key");
		this.debug = config.getBoolean("api.debug-mode", false);
		this.timeout = config.getInt("api.timeout", 5000);
		this.bypassVersionCheck = config.getBoolean("api.bypass-version-check");
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
		return false;
	}

	@Override
	protected int getTimeout() {
		return this.timeout;
	}

	@Override
	protected boolean bypassVersionCheck() {
		return this.bypassVersionCheck;
	}

}
