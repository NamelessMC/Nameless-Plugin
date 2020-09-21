package com.namelessmc.plugin.spigot;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessException;

public class ServerDataSender extends BukkitRunnable {

	Gson gson = new Gson();
	
	@Override
	public void run() {
		final int serverId = Config.MAIN.getConfig().getInt("server-id");
		if (serverId < 1) {
			return;
		}
		
		final Map<String, Object> map = new HashMap<>();
		map.put("tps", 20); // TODO tps
		map.put("time", System.currentTimeMillis());
		map.put("free-memory", Runtime.getRuntime().freeMemory());
		map.put("max-memory", Runtime.getRuntime().maxMemory());
		map.put("allocated-memory", Runtime.getRuntime().totalMemory());
		map.put("server-id", serverId);
		
		final Map<String, Map<String, Object>> players = new HashMap<>();
		
		for (final Player player : Bukkit.getOnlinePlayers()) {
			final Map<String, Object> playerInfo = new HashMap<>();
			
			playerInfo.put("name", player.getName());
			
			final Map<String, Object> location = new HashMap<>();
			final Location loc = player.getLocation();
			location.put("world", loc.getWorld().getName());
			location.put("x", loc.getBlockX());
			location.put("y", loc.getBlockY());
			location.put("z", loc.getBlockZ());
			
			playerInfo.put("location", location);
			playerInfo.put("ip", player.getAddress().getAddress().getHostAddress());
			playerInfo.put("playtime", player.getStatistic(Statistic.PLAY_ONE_TICK) / 120);
			
			try {
				if (NamelessPlugin.permissions != null) {
					playerInfo.put("rank", NamelessPlugin.permissions.getPrimaryGroup(player));
				}
			} catch (final UnsupportedOperationException e) {}
			
			try {
				if (NamelessPlugin.economy != null) {
					playerInfo.put("balance", NamelessPlugin.economy.getBalance(player));
				}
			} catch (final UnsupportedOperationException e) {}
			
			final Map<String, String> placeholders = new HashMap<>();
			
			Config.MAIN.getConfig().getStringList("upload-placeholders")
				.forEach(placeholder ->
				placeholders.put(placeholder, NamelessPlugin.getInstance().papiParser.parse(player, placeholder)));
			
			playerInfo.put("placeholders", placeholders);
			
			playerInfo.put("login-time", NamelessPlugin.LOGIN_TIME.get(player.getUniqueId()));
			
			players.put(player.getUniqueId().toString().replace("-", ""), playerInfo);
		}
		
		map.put("players", players);
		
		final String data = this.gson.toJson(map);
		
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
