package com.namelessmc.plugin.bungee;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessException;

import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class ServerDataSender implements Runnable {

	@Override
	public void run() {
		final Configuration config = NamelessPlugin.getInstance().getConfiguration().getMainConfig();
		final AbstractLogger logger = NamelessPlugin.getInstance().getCommonLogger();

		final int serverId = config.getInt("server-id");

		final JsonObject data = new JsonObject();
		data.addProperty("time", System.currentTimeMillis());
		data.addProperty("free-memory", Runtime.getRuntime().freeMemory());
		data.addProperty("max-memory", Runtime.getRuntime().maxMemory());
		data.addProperty("allocated-memory", Runtime.getRuntime().totalMemory());
		data.addProperty("server-id", serverId);

		final JsonObject players = new JsonObject();

		for (final ProxiedPlayer player : NamelessPlugin.getInstance().getProxy().getPlayers()) {
			final JsonObject playerInfo = new JsonObject();

			playerInfo.addProperty("name", player.getName());
			playerInfo.addProperty("address", player.getSocketAddress().toString());

			players.add(player.getUniqueId().toString().replace("-", ""), playerInfo);
		}

		data.add("players", players);

		NamelessPlugin.getInstance().getProxy().getScheduler().runAsync(NamelessPlugin.getInstance(), () ->
			NamelessPlugin.getInstance().getApiProvider().getNamelessApi().ifPresent((api) -> {
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
