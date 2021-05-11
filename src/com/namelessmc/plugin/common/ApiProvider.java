package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.Website;

public abstract class ApiProvider {

	private static final String USER_AGENT = "Nameless-Plugin";

	private NamelessAPI cachedApi;

	public ApiProvider() {

	}

	public NamelessAPI getNamelessApi() throws NamelessException {
		if (this.cachedApi != null) {
			return this.cachedApi;
		}

		this.cachedApi = NamelessAPI.builder()
				.apiUrl(this.getApiUrl())
				.userAgent(USER_AGENT)
				.debug(this.getDebug())
				.build();

		final Website info = this.cachedApi.getWebsite();
		if (!GlobalConstants.SUPPORTED_WEBSITE_VERSIONS.contains(info.getParsedVersion())) {
			// TODO Use slf4j
			System.err.println("Your website runs a version of Nameless (" + info.getVersion() + ") that is not supported by this version of the plugin.");
		}

		return this.cachedApi;
	}

	protected void clearCachedApi() {
		this.cachedApi = null;
	}

	protected abstract String getApiUrl();

	protected abstract boolean getDebug();

}
