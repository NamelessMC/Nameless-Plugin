package com.namelessmc.plugin.common;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDataSender implements Runnable, Reloadable {

	private final @NotNull NamelessPlugin plugin;
	private @Nullable AbstractScheduledTask dataSenderTask;
	private List<InfoProvider> globalInfoProviders;
	private List<PlayerInfoProvider> playerInfoProviders;
	private int serverId;

	protected AbstractDataSender(final @NotNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	protected @NotNull NamelessPlugin getPlugin() {
		return this.plugin;
	}

	public boolean isEnabled() {
		return this.dataSenderTask != null; // for bStats
	}

	@Override
	public void reload() {
		if (this.dataSenderTask != null) {
			this.playerInfoProviders = null;
			this.globalInfoProviders = null;
			this.dataSenderTask.cancel();
			this.dataSenderTask = null;
		}

		final Configuration config = this.plugin.config().getMainConfig();

		this.serverId = config.getInt("server-data-sender.server-id");
		if (this.serverId <= 0) {
			return;
		}

		final String intervalStr = config.getString("server-data-sender.interval");
		Duration interval = Duration.parse(intervalStr);
		this.dataSenderTask = this.plugin.scheduler().runTimer(this, interval);

		this.globalInfoProviders = new ArrayList<>();
		this.playerInfoProviders = new ArrayList<>();
		this.registerBaseProviders();
		this.registerCustomProviders();
	}

	private @NotNull JsonObject buildJsonBody() {
		final JsonObject data = new JsonObject();
		data.addProperty("server-id", this.serverId);

		data.addProperty("time", System.currentTimeMillis());


		for (InfoProvider infoProvider : this.globalInfoProviders) {
			infoProvider.addInfoToJson(data);
		}

		final JsonObject players = new JsonObject();

		for (final NamelessPlayer player : this.plugin.audiences().onlinePlayers()) {
			JsonObject playerJson = new JsonObject();
			playerJson.addProperty("name", player.getUsername());

			for (PlayerInfoProvider infoProvider : this.playerInfoProviders) {
				infoProvider.addInfoToJson(playerJson, player);
			}

			players.add(player.getUniqueId().toString(), playerJson);
		}

		data.add("players", players);
		return data;
	}

	@Override
	public void run() {
		final JsonObject data = buildJsonBody();

		this.plugin.scheduler().runAsync(() -> {
			this.plugin.api().getNamelessApi().ifPresent((api) -> {
				final AbstractLogger logger = this.plugin.logger();
				try {
					api.submitServerInfo(data);
				} catch (final ApiError e) {
					if (e.getError() == ApiError.INVALID_SERVER_ID) {
						logger.warning("Server ID is incorrect. Please enter a correct server ID or disable the server data uploader.");
					} else {
						logger.logException(e);
					}
				} catch (final NamelessException e) {
					logger.logException(e);
				}
			});
		});
	}

	protected void registerGlobalInfoProvider(InfoProvider globalInfoProvider) {
		this.globalInfoProviders.add(globalInfoProvider);
	}

	protected void registerPlayerInfoProvider(PlayerInfoProvider playerInfoProvider) {
		this.playerInfoProviders.add(playerInfoProvider);
	}

	protected abstract void registerCustomProviders();

	private void registerBaseProviders() {
		this.registerGlobalInfoProvider(json -> {
			json.addProperty("free-memory", Runtime.getRuntime().freeMemory());
			json.addProperty("max-memory", Runtime.getRuntime().maxMemory());
			json.addProperty("allocated-memory", Runtime.getRuntime().totalMemory());
		});

		this.registerPlayerInfoProvider((json, player) ->
				json.addProperty("login-time", plugin.getLoginTime(player)));
	}

	@FunctionalInterface
	public interface InfoProvider {

		void addInfoToJson(final @NotNull JsonObject json);

	}

	@FunctionalInterface
	public interface PlayerInfoProvider {

		void addInfoToJson(final @NotNull JsonObject json, final @NotNull NamelessPlayer player);

	}

}
