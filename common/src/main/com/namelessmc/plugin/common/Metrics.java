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
import java.util.zip.GZIPOutputStream;

public class Metrics implements Reloadable {

	// TODO actual uri
	private static final URI SUBMIT_URI = URI.create("https://metrics.namelessmc.com/plugin");
	private static final String USER_AGENT = "Nameless-Plugin/" + MavenConstants.PROJECT_VERSION;
	private static final Duration SEND_INTERVAL = Duration.ofMinutes(1);

	private final NamelessPlugin plugin;
	private final String platformInternalName;
	private final String platformVersion;
	private final Methanol methanol;

	private @Nullable AbstractScheduledTask task;

	public Metrics(NamelessPlugin plugin, String platformInternalName, String platformVersion) {
		this.plugin = plugin;
		this.platformInternalName = platformInternalName;
		this.platformVersion = platformVersion;
		this.methanol = Methanol.newBuilder().defaultHeader("User-Agent", USER_AGENT).build();

		this.plugin.properties().registerProperty("metrics-id", () -> UUID.randomUUID().toString());
		this.plugin.properties().registerProperty("metrics-debug", () -> "false");
	}

	private JsonObject metricsJson() {
		String metricsId = this.plugin.properties().get("metrics-id");

		// Format defined here: https://github.com/NamelessMC/Nameless-Plugin/wiki/Metrics
		JsonObject json = new JsonObject();
		json.addProperty("id", metricsId);
		json.addProperty("version", MavenConstants.PROJECT_VERSION);

		JsonObject platform = new JsonObject();
		platform.addProperty("internal-name", this.platformInternalName);
//		platform.addProperty("external-name", this.platformExternalName);
		platform.addProperty("version", this.platformVersion);
		platform.addProperty("java-version", Runtime.version().feature());
		json.add("platform", platform);

		JsonObject system = new JsonObject();
		system.addProperty("os", System.getProperty("os.name"));
		json.add("system", system);

		JsonObject stats = new JsonObject();
		stats.addProperty("api-working", this.plugin.apiProvider().isApiWorkingMetric());
		json.add("stats", stats);

		ConfigurationNode config = this.plugin.config().main();
		ConfigurationNode modules = this.plugin.config().modules();
		JsonObject settings = new JsonObject();
		settings.addProperty("language", this.plugin.language().getActiveLanguageCode());
		settings.addProperty("server-data-sender", config.node("server-data-sender", "enabled").getBoolean());
		settings.addProperty("server-data-sender-placeholders", config.node("server-data-sender", "placeholders", "enabled").getBoolean());
		settings.addProperty("auto-ban-on-website", config.node("auto-ban-on-website").getBoolean());
		settings.addProperty("not-registered-join-message", config.node("not-registered-join-message").getBoolean());
		settings.addProperty("user-sync-whitelist", config.node("user-sync", "whitelist", "enabled").getBoolean());
		settings.addProperty("user-sync-bans", config.node("user-sync", "bans", "enabled").getBoolean());
		settings.addProperty("announcements", config.node("announcements", "enabled").getBoolean());
		settings.addProperty("websend-command-executor", modules.node("websend", "command-executor", "enabled").getBoolean());
		settings.addProperty("websend-send-logs", modules.node("websend", "send-logs", "enabled").getBoolean());
		json.add("settings", settings);

		return json;
	}

	public void sendMetrics() {
		final String jsonString = this.metricsJson().toString();

		this.plugin.logger().fine(() -> "Sending metrics: " + jsonString);

		this.plugin.scheduler().runAsync(() -> {
			WritableBodyPublisher body = WritableBodyPublisher.create();
			HttpRequest request = HttpRequest.newBuilder(SUBMIT_URI)
					.POST(body)
					.build();

			this.plugin.scheduler().runAsync(() -> {
				try (Writer writer = new OutputStreamWriter(new GZIPOutputStream(body.outputStream()), StandardCharsets.UTF_8)) {
					writer.write(jsonString);
				} catch (IOException e) {
					body.closeExceptionally(e);
				}
			});

			try {
				this.methanol.send(request, HttpResponse.BodyHandlers.discarding());
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
