package com.namelessmc.plugin.common;

import com.namelessmc.java_api.*;
import com.namelessmc.java_api.exception.UnknownNamelessVersionException;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import xyz.derkades.derkutils.OptionalOptional;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public class ApiProvider implements Reloadable {

	private static final String USER_AGENT = "Nameless-Plugin/"	 + MavenConstants.PROJECT_VERSION;

	private final @NonNull AbstractScheduler scheduler;
	private final @NonNull AbstractLogger logger;
	private final @NonNull ConfigurationHandler config;

	private @NonNull OptionalOptional<NamelessAPI> cachedApi;
	private @Nullable Throwable lastException = null;

	private @Nullable String apiUrl;
	private @Nullable String apiKey;
	private boolean debug;
	private @Nullable Duration timeout;
	private boolean bypassVersionCheck;

	public ApiProvider(final @NonNull AbstractScheduler scheduler,
					   final @NonNull AbstractLogger logger,
					   final @NonNull ConfigurationHandler config) {
		this.scheduler = scheduler;
		this.logger = logger;
		this.config = config;
		this.cachedApi = OptionalOptional.unknown();
	}

	@Override
	public void reload() {
		final CommentedConfigurationNode config = this.config.main().node("api");
		this.apiUrl = config.node("url").getString();
		this.apiKey = config.node("key").getString();
		this.debug = config.node("debug").getBoolean();

		final Duration timeout = ConfigurationHandler.getDuration(config.node("timeout"));
		if (timeout != null) {
			this.timeout = timeout;
		} else {
			this.logger.warning("Invalid API timeout, using 10 seconds.");
			this.timeout = Duration.ofSeconds(10);
		}
		this.bypassVersionCheck = config.node("bypass-version-check").getBoolean();

		this.cachedApi = OptionalOptional.unknown();
		this.lastException = null;

		scheduler.runAsync(this::api);
	}

	// For bStats
	public String isApiWorkingMetric() {
		if (!this.cachedApi.isKnown()) {
			// In theory the API should always be cached, but in case it's not we
			// do not want to force load it because that would affect server performance.
			return "Unknown";
		} else if (this.cachedApi.isPresent()) {
			return "Working";
		} else {
			return "Not working";
		}
	}

	public synchronized Optional<NamelessAPI> api() {
		Objects.requireNonNull(this.logger, "Exception logger not initialized before API was requested. This is a bug.");
		Objects.requireNonNull(this.timeout, "API requested before config settings are loaded");

		if (this.cachedApi.isKnown()) {
			return this.cachedApi.getValueAsOptional();
		}

		if (this.apiUrl == null || this.apiUrl.isEmpty() || this.apiKey == null || this.apiKey.isEmpty()) {
			this.logger.severe("You have not entered an API URL and API key in the config. Please get your site's API URL and " +
					"API key from StaffCP > Configuration > API and reload the plugin.");
			this.cachedApi = OptionalOptional.knownEmpty(); // This won't be resolved without reloading, we don't have to retry.
		} else {
			try {
				URL url = new URL(this.apiUrl);
				final NamelessAPI api = NamelessAPI.builder(url, this.apiKey)
						.userAgent(USER_AGENT)
						.customDebugLogger(this.debug ? this.logger.getApiLogger() : null)
						.timeout(this.timeout)
						.build();

				final Website info = api.getWebsite();
				NamelessVersion version = info.getParsedVersion();
				if (this.bypassVersionCheck) {
					this.logger.warning("Bypassing version checks, use at your own risk!");
					this.cachedApi = OptionalOptional.knownPresent(api); // Cache working API
				} else if (NamelessVersion.isSupportedByJavaApi(version)) {
					this.logger.info("Website connection appears to be working.");
					this.cachedApi = OptionalOptional.knownPresent(api); // Cache working API
				} else {
					this.logger.severe("Your website runs a version of NamelessMC (" + version + ") that is not supported by this " +
							"version of the plugin. Please update your NamelessMC website and/or the plugin.");
					this.cachedApi = OptionalOptional.knownEmpty(); // No need to retry, cache that it's not working
				}

				this.lastException = null;
			} catch (MalformedURLException e) {
				this.lastException = e;
				this.logger.severe("You have entered an invalid API URL. Please get an up-to-date API URL from StaffCP > " +
						"Configuration > API and reload the plugin.");
				this.logger.severe("Error message: '" + e.getMessage() + "'");
				this.cachedApi = OptionalOptional.knownEmpty(); // This won't be resolved without reloading, we don't have to retry.
			} catch (final UnknownNamelessVersionException e) {
				this.lastException = e;
				this.logger.severe("The plugin doesn't recognize the NamelessMC version you are using. Ensure you are running a " +
						"recent version of the plugin and NamelessMC v2.");
				this.cachedApi = OptionalOptional.knownEmpty(); // Probably won't resolve on its own, cache until reload
			} catch (final ApiError e) {
				this.lastException = e;
				if (e.getError() == ApiError.REQUEST_NOT_AUTHORIZED) {
					this.logger.severe("You have entered an invalid API key. Please get an up-to-date API URL from StaffCP > Configuration > API and reload the plugin.");
				} else {
					this.logger.severe("Encountered an unexpected error code " + e.getError() + " while trying to connect to your " +
							"website. Enable api debug mode in the config file for more details. When you think you've fixed the problem, " +
							"reload the plugin to attempt connecting again.");
				}
				this.cachedApi = OptionalOptional.knownEmpty(); // This won't be resolved without reloading, we don't have to retry.
			} catch (final NamelessException e) {
				this.lastException = e;
				final String pluginCommand = this.config.commands().node("plugin").getString();
				this.logger.warning("Encountered an error while connecting to the website. This message is expected if your " +
						"site is down temporarily and can be ignored if the plugin works fine otherwise. If the plugin doesn't work " +
						"as expected, run '/" + pluginCommand + " last_api_error' to print the full error message.");
				if (this.debug) {
					this.logger.warning("Debug is enabled, printing full error message:");
					this.logger.logException(e);
				}

				// Do not cache so it immediately tries again the next time. These types of errors may fix on their
				// own, so we don't want to break the plugin until the administrator reloads.
				this.cachedApi = OptionalOptional.unknown();
			}
		}

		return this.cachedApi.toOptional();
	}

	public @Nullable Throwable getLastException() {
		return this.lastException;
	}

}
