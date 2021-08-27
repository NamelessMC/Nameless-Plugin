package com.namelessmc.plugin.common;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.Website;
import com.namelessmc.java_api.exception.UnknownNamelessVersionException;
import com.namelessmc.java_api.logger.ApiLogger;
import com.namelessmc.java_api.logger.JavaLoggerLogger;

public abstract class ApiProvider {

	private static final String USER_AGENT = "Nameless-Plugin";

	private Optional<NamelessAPI> cachedApi; // null if not cached

	private final Logger logger;

	public ApiProvider(final Logger logger) {
		this.logger = logger;
	}

	public Optional<NamelessAPI> getNamelessApi() {
		if (this.cachedApi != null) {
			return this.cachedApi;
		}

		final Optional<ApiLogger> debugLogger = this.getDebug()
				? Optional.of(new JavaLoggerLogger(this.logger, Level.INFO, "[Nameless-Java-API] "))
				: Optional.empty();

		final NamelessAPI api = NamelessAPI.builder()
				.apiUrl(this.getApiUrl())
				.userAgent(USER_AGENT)
				.withCustomDebugLogger(debugLogger)
				.build();

		try {
			final Website info = api.getWebsite();
			try {
				if (GlobalConstants.SUPPORTED_WEBSITE_VERSIONS.contains(info.getParsedVersion())) {
					this.cachedApi = Optional.of(api);
				} else {
					this.logger.severe("Your website runs a version of NamelessMC (" + info.getVersion() + ") that is not supported by this version of the plugin. Note that usually only the newest one or two NamelessMC versions are supported.");
					this.cachedApi = Optional.empty();
				}
			} catch (final UnknownNamelessVersionException e) {
				this.logger.severe("The plugin doesn't recognize the NamelessMC version you are using. Try updating the plugin to the latest version.");
				this.cachedApi = Optional.empty();
			}
		} catch (final ApiError e) {
			if (e.getError() == ApiError.INVALID_API_KEY) {
				this.logger.severe("You have entered an invalid API key. Please get an up-to-date API URL from StaffCP > Configuration > API and reload the plugin.");
			} else {
				this.logger.severe("Encountered an unexpected error code " + e.getError() + " while trying to connect to your website. Enable api debug mode in the config file for more details. When you think you've fixed the problem, reload the plugin to attempt connecting again.");
			}
			this.cachedApi = Optional.empty();
		} catch (final NamelessException e) {
			this.logger.warning("Encounted an error when connecting to the website. This message is expected if your site is down temporarily and can be ignored if the plugin works fine otherwise. If the plugin doesn't work as expected, please enable api-debug-mode in the config and run /nlpl reload to get more information.");
			// Do not cache so it immediately tries again the next time. These types of errors may fix on their
			// own, so we don't want to break the plugin until the administrator reloads.
			this.cachedApi = null;
			return Optional.empty();
		}

		return this.cachedApi;
	}

	protected void clearCachedApi() {
		this.cachedApi = null;
	}

	protected abstract String getApiUrl();

	protected abstract boolean getDebug();

}
