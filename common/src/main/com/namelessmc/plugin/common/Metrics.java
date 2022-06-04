package com.namelessmc.plugin.common;

import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.WritableBodyPublisher;
import com.google.gson.JsonObject;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

public class Metrics implements Reloadable {

	private static final URI SUBMIT_URI = URI.create("https://nameless-metrics.rkslot.nl/submit");
	private static final String USER_AGENT = "Nameless-Plugin/" + MavenConstants.PROJECT_VERSION;
	private static final Duration SEND_INTERVAL = Duration.ofMinutes(5);

	private final NamelessPlugin plugin;
	private final String platformInternalName;
	private final String platformVersion;
	private final Methanol methanol;

	private @Nullable AbstractScheduledTask task;

	public Metrics(NamelessPlugin plugin, String platformInternalName, String platformVersion) {
		this.plugin = plugin;
		this.platformInternalName = platformInternalName;
		this.platformVersion = platformVersion;
		this.methanol = Methanol.create();

		this.plugin.properties().registerProperty("metrics-id", () -> UUID.randomUUID().toString());
	}

	private JsonObject metricsJson() {
		String metricsId = this.plugin.properties().get("metrics-id");

		// Format defined here: https://github.com/NamelessMC/Nameless-Plugin/wiki/Metrics
		JsonObject json = new JsonObject();
		json.addProperty("source", "nameless-plugin");
		json.addProperty("uuid", metricsId);

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
		fields.addProperty("api_working", this.plugin.apiProvider().apiIfCached() != null);

		// Configuration
		ConfigurationNode config = this.plugin.config().main();
		ConfigurationNode modules = this.plugin.config().modules();
		fields.addProperty("language", this.plugin.language().getActiveLanguageCode());
		fields.addProperty("server_data_sender", config.node("server-data-sender", "enabled").getBoolean());
		fields.addProperty("server_data_sender_placeholders", config.node("server-data-sender", "placeholders", "enabled").getBoolean());
		fields.addProperty("auto_ban_on_website", config.node("auto-ban-on-website").getBoolean());
		fields.addProperty("not_registered_join_message", config.node("not-registered-join-message").getBoolean());
		fields.addProperty("user_sync_whitelist", config.node("user-sync", "whitelist", "enabled").getBoolean());
		fields.addProperty("user_sync_bans", config.node("user-sync", "bans", "enabled").getBoolean());
		fields.addProperty("announcements", config.node("announcements", "enabled").getBoolean());
		fields.addProperty("websend_command_executor", modules.node("websend", "command-executor", "enabled").getBoolean());
		fields.addProperty("websend_send_logs", modules.node("websend", "send-logs", "enabled").getBoolean());

		json.add("fields", fields);

		return json;
	}

	public void sendMetrics() {
		final String jsonString = this.metricsJson().toString();

		this.plugin.logger().fine(() -> "Sending metrics: " + jsonString);

		this.plugin.scheduler().runAsync(() -> {
			WritableBodyPublisher body = WritableBodyPublisher.create();
			HttpRequest request = HttpRequest.newBuilder(SUBMIT_URI)
					.header("Content-Type", "application/json")
//					.header("Content-Encoding", "gzip")
					.header("User-Agent", USER_AGENT)
					.POST(body)
					.build();

			this.plugin.scheduler().runAsync(() -> {
				try (Writer writer = new OutputStreamWriter(body.outputStream(), StandardCharsets.UTF_8)) {
					writer.write(jsonString);
				} catch (IOException e) {
					body.closeExceptionally(e);
				}
			});

			try {
				if (this.plugin.logger().isVerbose()) {
					HttpResponse<String> response = this.methanol.send(request, HttpResponse.BodyHandlers.ofString());
					this.plugin.logger().fine("Metrics submitted, received status code " + response.statusCode() + " with body:\n" + response.body());
				} else {
					this.methanol.send(request, HttpResponse.BodyHandlers.discarding());
				}
			} catch (Exception e) {
				this.plugin.logger().fine(() -> "Exception while sending metrics: " + AbstractLogger.stackTraceAsString(e));
			}
		});
	}

	@Override
	public void reload() {
		if (this.task == null) {
			this.task = this.plugin.scheduler().runTimer(this::sendMetrics, SEND_INTERVAL);
		}
	}

}
