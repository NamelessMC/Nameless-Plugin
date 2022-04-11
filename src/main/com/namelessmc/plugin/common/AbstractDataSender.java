package com.namelessmc.plugin.common;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import com.namelessmc.plugin.spigot.NamelessPluginSpigot;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDataSender implements Runnable, Reloadable {

	private final @NotNull NamelessPlugin plugin;
	private final @NotNull List<InfoProvider> globalInfoProviders = new ArrayList<>();
	private final @NotNull List<PlayerInfoProvider> playerInfoProviders = new ArrayList<>();

	private @Nullable AbstractScheduledTask dataSenderTask;
	private int serverId;

	protected AbstractDataSender(final @NotNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean isEnabled() {
		return this.dataSenderTask != null; // for bStats
	}

	@Override
	public void reload() {
		if (this.dataSenderTask != null) {
			this.dataSenderTask.cancel();
			this.dataSenderTask = null;
		}

		final Configuration config = this.plugin.config().getMainConfig();

		this.serverId = config.getInt("server-data-sender.server-id");
		if (this.serverId > 0) {
			final String intervalStr = config.getString("server-data-sender.interval");
			Duration interval = Duration.parse(intervalStr);
			this.dataSenderTask = this.plugin.scheduler().runTimer(this, interval);
		}
	}

	@Override
	public void run() {
		final JsonObject data = new JsonObject();
		data.addProperty("server-id", this.serverId);

		data.addProperty("time", System.currentTimeMillis());
		data.addProperty("free-memory", Runtime.getRuntime().freeMemory());
		data.addProperty("max-memory", Runtime.getRuntime().maxMemory());
		data.addProperty("allocated-memory", Runtime.getRuntime().totalMemory());

		for (InfoProvider infoProvider : this.globalInfoProviders) {
			infoProvider.addInfoToJson(data);
		}

		final JsonObject players = new JsonObject();

		for (final NamelessPlayer player : this.plugin.audiences().onlinePlayers()) {
			JsonObject playerJson = new JsonObject();
			playerJson.addProperty("name", player.getUsername());
			data.addProperty("login-time", plugin.getLoginTime(player));

			for (PlayerInfoProvider infoProvider : this.playerInfoProviders) {
				infoProvider.addInfoToJson(playerJson, player);
			}

			players.add(player.getUniqueId().toString(), playerJson);
		}

		data.add("players", players);

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

	@FunctionalInterface
	public interface InfoProvider {

		void addInfoToJson(final @NotNull JsonObject json);

	}

	@FunctionalInterface
	public interface PlayerInfoProvider {

		void addInfoToJson(final @NotNull JsonObject json, final @NotNull NamelessPlayer player);

	}

}
