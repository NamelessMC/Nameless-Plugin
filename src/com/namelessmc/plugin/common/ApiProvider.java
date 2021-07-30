package com.namelessmc.plugin.common;

import java.util.Optional;

import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.Website;

public abstract class ApiProvider {

	private static final String USER_AGENT = "Nameless-Plugin";

	private Optional<NamelessAPI> cachedApi; // null if not cached

	public ApiProvider() {

	}

	public Optional<NamelessAPI> getNamelessApi() {
		if (this.cachedApi != null) {
			return this.cachedApi;
		}

		final NamelessAPI api = NamelessAPI.builder()
				.apiUrl(this.getApiUrl())
				.userAgent(USER_AGENT)
				.debug(this.getDebug())
				.build();

		try {
			final Website info = api.getWebsite();
			if (GlobalConstants.SUPPORTED_WEBSITE_VERSIONS.contains(info.getParsedVersion())) {
				this.cachedApi = Optional.of(api);
			} else {
				// TODO Use proper logger
				System.err.println("Your website runs a version of NamelessMC (" + info.getVersion() + ") that is not supported by this version of the plugin.");
				this.cachedApi = Optional.empty();
			}
		} catch (final ApiError e) {
			if (e.getError() == ApiError.INVALID_API_KEY) {
				// TODO Use proper logger
				System.err.println("You have entered an invalid API key. Please get an up-to-date API URL from StaffCP > Configuration > API and reload the plugin.");
			} else {
				System.err.println("Encountered an unexpected error code " + e.getError() + " while trying to connect to your website. Enable api debug mode in the config file for more details. When you think you've fixed the problem, reload the plugin to attempt connecting again.");
			}
			this.cachedApi = Optional.empty();
		} catch (final NamelessException e) {
			System.err.println("Encounted an error when connecting to the website. This message is expected if your site is down temporarily and can be ignored if the plugin works fine otherwise. If the plugin doesn't work as expected, please enable api-debug-mode in the config and run /nlpl reload to get more information.");
			// Do not cache so it immediately tries again the next time. These types of errors may fix on their
			// own, so we don't want to break the plugin until the administrator reloads.
			this.cachedApi = null;
		}

		return this.cachedApi;
	}

	protected void clearCachedApi() {
		this.cachedApi = null;
	}

	protected abstract String getApiUrl();

	protected abstract boolean getDebug();

}
