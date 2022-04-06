package com.namelessmc.plugin.bungee;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class ServerDataSender implements Runnable, Reloadable {

	private final @NotNull NamelessPlugin plugin;
	private @Nullable AbstractScheduledTask dataSenderTask;

	ServerDataSender(final @NotNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		if (this.dataSenderTask != null) {
			this.dataSenderTask.cancel();
			this.dataSenderTask = null;
		}

		final Configuration config = this.plugin.config().getMainConfig();

		final int serverId = config.getInt("server-data-sender.server-id");
		if (serverId > 0) {
			final String intervalStr = config.getString("server-data-sender.interval");
			Duration interval = Duration.parse(intervalStr);
			this.dataSenderTask = this.plugin.scheduler().runTimer(this, interval);
		}
	}

	@Override
	public void run() {
		final Configuration config = this.plugin.config().getMainConfig();
		final AbstractLogger logger = this.plugin.logger();

		final int serverId = config.getInt("server-id");

		final JsonObject data = new JsonObject();
		data.addProperty("time", System.currentTimeMillis());
		data.addProperty("free-memory", Runtime.getRuntime().freeMemory());
		data.addProperty("max-memory", Runtime.getRuntime().maxMemory());
		data.addProperty("allocated-memory", Runtime.getRuntime().totalMemory());
		data.addProperty("server-id", serverId);

		final JsonObject players = new JsonObject();

		for (final ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
			final JsonObject playerInfo = new JsonObject();

			playerInfo.addProperty("name", player.getName());
			playerInfo.addProperty("address", player.getSocketAddress().toString());

			players.add(player.getUniqueId().toString().replace("-", ""), playerInfo);
		}

		data.add("players", players);

		this.plugin.scheduler().runAsync(() ->
			this.plugin.api().getNamelessApi().ifPresent((api) -> {
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
			})
		);
	}

}
