package com.namelessmc.plugin.common;

import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessVersion;
import com.namelessmc.java_api.Website;
import com.namelessmc.java_api.exception.UnknownNamelessVersionException;
import com.namelessmc.java_api.logger.ApiLogger;
import com.namelessmc.java_api.logger.JavaLoggerLogger;
import com.namelessmc.plugin.bungee.NamelessPlugin;

import java.net.MalformedURLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("OptionalAssignedToNull")
public abstract class ApiProvider {

	private static final String USER_AGENT = "Nameless-Plugin";

	private Optional<NamelessAPI> cachedApi; // null if not cached

	private final Logger logger;

	public ApiProvider(final Logger logger) {
		this.logger = logger;
	}

	public synchronized Optional<NamelessAPI> getNamelessApi() {
		if (this.cachedApi != null) {
			return this.cachedApi;
		}

		final ApiLogger debugLogger = this.getDebug()
				? new JavaLoggerLogger(this.logger, Level.INFO, "[Nameless-Java-API] ")
				: null;

		this.cachedApi = Optional.empty();

		try {
			if (this.getApiUrl().isEmpty()) {
				this.logger.severe("You have not entered an API URL in the config. Please get your site's API URL from StaffCP > Configuration > API and reload the plugin.");
			} else {
				final NamelessAPI api = NamelessAPI.builder()
						.apiUrl(this.getApiUrl())
						.userAgent(USER_AGENT)
						.withCustomDebugLogger(debugLogger)
						.withTimeoutMillis(this.getTimeout())
						.build();

				final Website info = api.getWebsite();
				try {
					NamelessVersion version = info.getParsedVersion();
					if (GlobalConstants.SUPPORTED_WEBSITE_VERSIONS.contains(version)) {
						this.cachedApi = Optional.of(api);
					} else if (GlobalConstants.DEPRECATED_WEBSITE_VERSIONS.contains(version)) {
						this.logger.warning("Support for your NamelessMC version (" + version + ") is deprecated, some functionality may be broken. Please upgrade to a newer version of NamelessMC as soon as possible.");
						this.cachedApi = Optional.of(api);
					} else {
						this.logger.severe("Your website runs a version of NamelessMC (" + version + ") that is not supported by this version of the plugin. Note that usually only the newest one or two NamelessMC versions are supported.");
					}
				} catch (final UnknownNamelessVersionException e) {
					this.logger.severe("The plugin doesn't recognize the NamelessMC version you are using. Ensure you are running a recent version of the plugin and NamelessMC v2.");
				}
			}
		} catch (final MalformedURLException e) {
			this.logger.severe("You have entered an invalid API URL or not entered one at all. Please get an up-to-date API URL from StaffCP > Configuration > API and reload the plugin.");
			this.logger.severe("Error message: '" + e.getMessage() + "'");
		} catch (final ApiError e) {
			if (e.getError() == ApiError.INVALID_API_KEY) {
				this.logger.severe("You have entered an invalid API key. Please get an up-to-date API URL from StaffCP > Configuration > API and reload the plugin.");
			} else {
				this.logger.severe("Encountered an unexpected error code " + e.getError() + " while trying to connect to your website. Enable api debug mode in the config file for more details. When you think you've fixed the problem, reload the plugin to attempt connecting again.");
			}
		} catch (final NamelessException e) {
			this.logger.warning("Encountered an error while connecting to the website. This message is expected if your site is down temporarily and can be ignored if the plugin works fine otherwise. If the plugin doesn't work as expected, please enable api-debug-mode in the config and run /nlpl reload to get more information.");
			// Do not cache so it immediately tries again the next time. These types of errors may fix on their
			// own, so we don't want to break the plugin until the administrator reloads.
			if (this.getDebug()) {
				this.logger.warning("Debug is enabled, printing full error message:");
				NamelessPlugin.getInstance().getExceptionLogger().logException(e);
			}

			this.cachedApi = null;
			return Optional.empty();
		}

		return this.cachedApi;
	}

	protected synchronized void clearCachedApi() {
		this.cachedApi = null;
	}

	protected abstract String getApiUrl();

	protected abstract boolean getDebug();

	protected abstract int getTimeout();

	public abstract boolean useUuids();

}
