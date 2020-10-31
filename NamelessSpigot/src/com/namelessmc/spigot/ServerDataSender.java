package com.namelessmc.spigot;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessException;

public class ServerDataSender extends BukkitRunnable {
	
	@Override
	public void run() {
		final int serverId = Config.MAIN.getConfig().getInt("server-id");
		if (serverId < 1) {
			return;
		}
		
		final JsonObject data = new JsonObject();
		data.addProperty("time", System.currentTimeMillis());
		data.addProperty("free-memory", Runtime.getRuntime().freeMemory());
		data.addProperty("max-memory", Runtime.getRuntime().maxMemory());
		data.addProperty("allocated-memory", Runtime.getRuntime().totalMemory());
		data.addProperty("server-id", serverId);

		try {
			if (NamelessPlugin.permissions != null) {
				final String[] gArray = NamelessPlugin.permissions.getGroups();
				final JsonArray groups = new JsonArray(gArray.length);
				Arrays.stream(gArray).map(JsonPrimitive::new).forEach(groups::add);
				data.add("groups", groups);
			}
		} catch (final UnsupportedOperationException e) {}
		
		final JsonObject players = new JsonObject();
		
		for (final Player player : Bukkit.getOnlinePlayers()) {
			final JsonObject playerInfo = new JsonObject();
			
			playerInfo.addProperty("name", player.getName());
			
			final JsonObject location = new JsonObject();
			final Location loc = player.getLocation();
			location.addProperty("world", loc.getWorld().getName());
			location.addProperty("x", loc.getBlockX());
			location.addProperty("y", loc.getBlockY());
			location.addProperty("z", loc.getBlockZ());
			
			playerInfo.add("location", location);
			playerInfo.addProperty("ip", player.getAddress().getAddress().getHostAddress());
			playerInfo.addProperty("playtime", player.getStatistic(Statistic.PLAY_ONE_TICK) / 120);
			
			try {
				if (NamelessPlugin.permissions != null) {
					playerInfo.addProperty("rank", NamelessPlugin.permissions.getPrimaryGroup(player));
				}
			} catch (final UnsupportedOperationException e) {}
			
			try {
				if (NamelessPlugin.economy != null) {
					playerInfo.addProperty("balance", NamelessPlugin.economy.getBalance(player));
				}
			} catch (final UnsupportedOperationException e) {}
			
			final JsonObject placeholders = new JsonObject();
			
			Config.MAIN.getConfig().getStringList("upload-placeholders")
				.forEach(placeholder ->
				placeholders.addProperty(placeholder, NamelessPlugin.getInstance().papiParser.parse(player, placeholder)));
			
			playerInfo.add("placeholders", placeholders);
			
			playerInfo.addProperty("login-time", NamelessPlugin.LOGIN_TIME.get(player.getUniqueId()));
			
			players.add(player.getUniqueId().toString().replace("-", ""), playerInfo);
		}
		
		data.add("players", players);
		
		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				NamelessPlugin.getInstance().api.submitServerInfo(data);
			} catch (final ApiError e) {
				if (e.getError() == ApiError.INVALID_SERVER_ID) {
					NamelessPlugin.getInstance().getLogger().warning("Server ID is incorrect. Please enter a correct server ID or disable the server data uploader.");
				} else {
					e.printStackTrace();
				}
			} catch (final NamelessException e) {
				e.printStackTrace();
			}
		});
	}

}
