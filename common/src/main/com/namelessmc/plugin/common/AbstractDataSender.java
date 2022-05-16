package com.namelessmc.plugin.common;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.event.ServerJoinEvent;
import com.namelessmc.plugin.common.event.ServerQuitEvent;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.md_5.bungee.config.Configuration;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.*;

public abstract class AbstractDataSender implements Runnable, Reloadable {

	private final @NonNull NamelessPlugin plugin;
	private @Nullable AbstractScheduledTask dataSenderTask;
	private @Nullable List<@NonNull InfoProvider> globalInfoProviders;
	private @Nullable List<@NonNull PlayerInfoProvider> playerInfoProviders;
	private int serverId;

	private final @NonNull Map<UUID, Long> playerLoginTime = new HashMap<>();

	protected AbstractDataSender(final @NonNull NamelessPlugin plugin) {
		this.plugin = plugin;

		this.startLoginTimeTracking();
	}

	private void startLoginTimeTracking(@UnknownInitialization(AbstractDataSender.class) AbstractDataSender this) {
		this.plugin.registerReloadable(() -> {
			// If the plugin is loaded when the server is already started (e.g. using /reload on bukkit), add
			// players manually because the join event is never called for them.
			for (final NamelessPlayer player : this.plugin.audiences().onlinePlayers()) {
				playerLoginTime.put(player.uuid(), System.currentTimeMillis());
			}
		});

		this.plugin.events().subscribe(ServerJoinEvent.class, event ->
				playerLoginTime.put(event.player().uuid(), System.currentTimeMillis()));
		this.plugin.events().subscribe(ServerQuitEvent.class, event ->
				playerLoginTime.remove(event.uuid()));
	}

	protected @NonNull NamelessPlugin getPlugin() {
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

		final Configuration config = this.plugin.config().main();

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

	private @NonNull JsonObject buildJsonBody() {
		if (this.globalInfoProviders == null || this.playerInfoProviders == null) {
			throw new IllegalStateException("Providers are null, is the data sender disabled?");
		}

		final JsonObject data = new JsonObject();
		data.addProperty("server-id", this.serverId);

		data.addProperty("time", System.currentTimeMillis());

		for (InfoProvider infoProvider : this.globalInfoProviders) {
			try {
				infoProvider.addInfoToJson(data);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		final JsonObject players = new JsonObject();

		for (final NamelessPlayer player : this.plugin.audiences().onlinePlayers()) {
			JsonObject playerJson = new JsonObject();
			playerJson.addProperty("name", player.username());

			for (PlayerInfoProvider infoProvider : this.playerInfoProviders) {
				try {
					infoProvider.addInfoToJson(playerJson, player);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}

			players.add(player.websiteUuid(), playerJson);
		}

		data.add("players", players);
		return data;
	}

	@Override
	public void run() {
		final JsonObject data = buildJsonBody();

		this.plugin.scheduler().runAsync(() -> {
			this.plugin.apiProvider().api().ifPresent((api) -> {
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
		if (this.globalInfoProviders == null) {
			throw new IllegalStateException("Cannot register info provider when data sender is disabled");
		}
		this.globalInfoProviders.add(globalInfoProvider);
	}

	protected void registerPlayerInfoProvider(PlayerInfoProvider playerInfoProvider) {
		if (this.playerInfoProviders == null) {
			throw new IllegalStateException("Cannot register info provider when data sender is disabled");
		}
		this.playerInfoProviders.add(playerInfoProvider);
	}

	protected abstract void registerCustomProviders();

	private void registerBaseProviders() {
		this.registerGlobalInfoProvider(json -> {
			json.addProperty("free-memory", Runtime.getRuntime().freeMemory());
			json.addProperty("max-memory", Runtime.getRuntime().maxMemory());
			json.addProperty("allocated-memory", Runtime.getRuntime().totalMemory());
		});

		this.registerPlayerInfoProvider((json, player) -> {
			Long loginTime =  this.playerLoginTime.get(player.uuid());
			if (loginTime == null) {
				this.plugin.logger().warning("Player " + player.username() + " is missing from login time map");
				loginTime = System.currentTimeMillis();
			}
			json.addProperty("login-time", loginTime);
		});
	}

	@FunctionalInterface
	public interface InfoProvider {

		void addInfoToJson(final @NonNull JsonObject json);

	}

	@FunctionalInterface
	public interface PlayerInfoProvider {

		void addInfoToJson(final @NonNull JsonObject json, final @NonNull NamelessPlayer player);

	}

}
