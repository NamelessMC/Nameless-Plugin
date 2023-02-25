package com.namelessmc.plugin.common;

import com.github.mizosoft.methanol.Methanol;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.Website;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

public class Metrics implements Reloadable {

	private static final URI SUBMIT_URI = URI.create("https://metrics.rkslot.nl/submit");
	private static final String SOURCE = "nameless-plugin";
	private static final String USER_AGENT = "Nameless-Plugin/" + MavenConstants.PROJECT_VERSION;
	private static final Duration SEND_INTERVAL = Duration.ofMinutes(15);
	// Metrics id is only unique for a single session for better privacy
	private static final String METRICS_ID = UUID.randomUUID().toString();

	private final NamelessPlugin plugin;
	private final String platformInternalName;
	private final String platformVersion;
	private final Methanol methanol;

	private @Nullable AbstractScheduledTask task;
	private boolean firstStartup = true;

	public Metrics(NamelessPlugin plugin, String platformInternalName, String platformVersion) {
		this.plugin = plugin;
		this.platformInternalName = platformInternalName;
		this.platformVersion = platformVersion;
		this.methanol = Methanol.create();
	}

	private JsonObject metricsJson() {
		JsonObject json = new JsonObject();
		json.addProperty("source", SOURCE);
		json.addProperty("uuid", METRICS_ID);

		JsonObject fields = new JsonObject();

		// Version
		fields.addProperty("version", MavenConstants.PROJECT_VERSION);

		// Platform
		fields.addProperty("platform_internal_name", this.platformInternalName);
		fields.addProperty("platform_version", this.platformVersion);
		fields.addProperty("java_version", Runtime.version().feature());

		// Operating system
		fields.addProperty("os_name", System.getProperty("os.name"));

		// Stats
		final NamelessAPI api = this.plugin.apiProvider().apiIfCached();
		fields.addProperty("api_working", api != null);
		if (api != null) {
			Website website = api.websiteIfCached();
			if (website != null) {
				fields.addProperty("website_version", website.rawVersion());
			}
		}

		final AbstractPermissions permissionsAdapter = this.plugin.permissions();
		fields.addProperty("permissions_adapter", permissionsAdapter != null ? permissionsAdapter.getClass().getSimpleName() : "None");

		// Configuration
		ConfigurationNode config = this.plugin.config().main();
		ConfigurationNode modules = this.plugin.config().modules();
		fields.addProperty("language", this.plugin.language().getActiveLanguageCode());
		fields.addProperty("server_data_sender", config.node("server-data-sender", "enabled").getBoolean());
		fields.addProperty("server_data_sender_placeholders", config.node("server-data-sender", "placeholders", "enabled").getBoolean());
		fields.addProperty("register_custom_username", config.node("register-custom-username").getBoolean(true));
		fields.addProperty("sync_ban_to_website", config.node("sync-ban-to-website").getBoolean());
		fields.addProperty("not_registered_join_message", config.node("not-registered-join-message").getBoolean());
		fields.addProperty("user_sync_whitelist", config.node("user-sync", "whitelist", "enabled").getBoolean());
		fields.addProperty("user_sync_bans", config.node("user-sync", "bans", "enabled").getBoolean());
		fields.addProperty("announcements", config.node("announcements", "enabled").getBoolean());
		fields.addProperty("retrieve_placeholders", config.node("retrieve-placeholders", "enabled").getBoolean());
		fields.addProperty("websend_command_executor", modules.node("websend", "command-executor", "enabled").getBoolean());
		fields.addProperty("websend_send_logs", modules.node("websend", "send-logs", "enabled").getBoolean());
		fields.addProperty("store_command_executor", modules.node("store", "command-executor", "enabled").getBoolean());

		json.add("fields", fields);

		return json;
	}

	public void sendMetrics() {
		final String jsonString = this.metricsJson().toString();

		this.plugin.logger().fine(() -> "Sending metrics: " + jsonString);

		this.plugin.scheduler().runAsync(() -> {
			HttpRequest request = HttpRequest.newBuilder(SUBMIT_URI)
					.header("Content-Type", "application/json")
					.header("User-Agent", USER_AGENT)
					.timeout(Duration.ofSeconds(5))
					.POST(HttpRequest.BodyPublishers.ofString(jsonString, StandardCharsets.UTF_8))
					.build();

			try {
				if (this.plugin.logger().isVerbose()) {
					HttpResponse<String> response = this.methanol.send(request, HttpResponse.BodyHandlers.ofString());
					if (response.statusCode() != 200) {
						this.plugin.logger().fine(() -> "Received status code " + response.statusCode() + " with body:\n" + response.body());
					}
				} else {
					this.methanol.send(request, HttpResponse.BodyHandlers.discarding());
				}
			} catch (Exception e) {
				this.plugin.logger().fine(() -> "Exception while sending metrics: " + AbstractLogger.stackTraceAsString(e));
			}
		});
	}

	@Override
	public void unload() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
	}

	@Override
	public void load() {
		this.task = this.plugin.scheduler().runTimer(this::sendMetrics, SEND_INTERVAL);
		if (this.firstStartup) {
			// Also send soon after server startup
			this.plugin.scheduler().runDelayed(this::sendMetrics, Duration.ofSeconds(30));
			this.firstStartup = false;
		}
	}

}
