package com.namelessmc.plugin.common;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.time.Duration;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessApiBuilder;
import com.namelessmc.java_api.NamelessVersion;
import com.namelessmc.java_api.Website;
import com.namelessmc.java_api.exception.ApiException;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.exception.UnknownNamelessVersionException;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.common.logger.AbstractLogger;

public class ApiProvider implements Reloadable {

	private static final String USER_AGENT = "Nameless-Plugin/"	 + MavenConstants.PROJECT_VERSION;

	private final AbstractScheduler scheduler;
	private final AbstractLogger logger;
	private final ConfigurationHandler config;

	private	boolean apiIsCached;
	private @Nullable NamelessAPI api;

	private @Nullable URL apiUrl;
	private @Nullable String apiKey;
	private boolean debug;
	private @Nullable Duration timeout;
	private boolean bypassVersionCheck;
	private boolean forceHttp1;

	public ApiProvider(final @NonNull AbstractScheduler scheduler,
					   final @NonNull AbstractLogger logger,
					   final @NonNull ConfigurationHandler config) {
		this.scheduler = scheduler;
		this.logger = logger;
		this.config = config;
		this.api = null;
		this.apiIsCached = false;
	}

	@Override
	public void unload() {
		this.apiIsCached = false;
		this.api = null;
		this.apiUrl = null;
		this.apiKey = null;
		this.timeout = null;
	}

	@Override
	public void load() {
		final CommentedConfigurationNode config = this.config.main().node("api");
		try {
			final String rawUrl = config.node("url").getString();
			if (rawUrl != null ) {
				this.apiUrl = URI.create(rawUrl).toURL();
			}
		} catch (final MalformedURLException e) {
			this.logger.severe("You have entered an invalid API URL. Please get an up-to-date API URL from StaffCP > " +
					"Configuration > API and reload the plugin.");
			return;
		}
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
		this.forceHttp1 = config.node("force-http-1").getBoolean();

		this.scheduler.runAsync(this::api);
	}

	@Deprecated
	public @Nullable String isApiWorkingMetric() {
		if (!this.apiIsCached) {
			// In theory the API should always be cached, but in case it's not we
			// do not want to force load it because that would affect server performance.
			return "Unknown";
		}

		return this.api != null ? "Working" : "Not working";
	}

	public synchronized @Nullable NamelessAPI api() {
		if (this.apiIsCached) {
			return this.api;
		}

		if (this.apiUrl == null || this.apiKey == null || this.apiKey.isEmpty()) {
			this.logger.severe("You have not entered an API URL and API key in the config or the API URL or " +
					"API key is invalid. Please get your site's API URL and API key from " +
					"StaffCP > Configuration > API and reload the plugin.");
			// This won't be resolved without reloading, we don't have to retry.
			this.api = null;
			this.apiIsCached = true;
		} else {
			try {
				final NamelessApiBuilder builder = NamelessAPI.builder(this.apiUrl, this.apiKey)
						.userAgent(ApiProvider.USER_AGENT)
						.customDebugLogger(this.debug ? this.logger.getApiLogger() : null)
						.timeout(this.timeout);

				if (this.forceHttp1) {
					builder.httpVersion(HttpClient.Version.HTTP_1_1);
				}

				final NamelessAPI api = builder.build();

				final Website info = api.website();
				if (this.bypassVersionCheck) {
					this.logger.warning("Bypassing version checks, use at your own risk!");
					this.api = api; // Cache working API
					this.apiIsCached = true;
				} else {
					try {
						final NamelessVersion version = info.parsedVersion();
						if (NamelessVersion.isSupportedByJavaApi(version)) {
							this.logger.fine("Website connection appears to be working.");
							this.api = api; // Cache working API
							this.apiIsCached = true;
						} else {
							this.logger.severe("Your website runs a version of NamelessMC (" + version + ") that is not supported by this " +
									"version of the plugin. Please update your NamelessMC website and/or the plugin.");
							this.api = null; // No need to retry, cache that it's not working
							this.apiIsCached = true;
						}
					} catch (final UnknownNamelessVersionException ignored) {
						this.logger.severe("The plugin doesn't recognize the NamelessMC version you are using. Ensure you are running a " +
								"recent version of the plugin and NamelessMC v2. If no updated version is available, you can bypass " +
								"the version check at your own risk by adding `bypass-version-check: true` in the api section in main.yml.");
						this.api = null; // Probably won't resolve on its own, cache until reload
						this.apiIsCached = true;
					}
				}
			} catch (final NamelessException e) {
				this.logger.logException(e);

				if (e instanceof ApiException) {
					this.api = null; // This won't be resolved without reloading, we don't have to retry.
					this.apiIsCached = true;
				} else {
					// Do not cache, so it immediately tries again the next time. These types of errors may fix on their
					// own, so we don't want to break the plugin until the administrator reloads.
					this.api = null;
					this.apiIsCached = true;
				}
			}
		}

		return this.api;
	}

	public @Nullable NamelessAPI apiIfCached() {
		return this.apiIsCached ? this.api : null;
	}

}
