package com.namelessmc.plugin.NamelessSpigot;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.namelessmc.NamelessAPI.ApiError;
import com.namelessmc.NamelessAPI.NamelessException;

public class ServerDataSender extends BukkitRunnable {

	Gson gson = new Gson();
	
	@Override
	public void run() {
		int serverId = Config.MAIN.getConfig().getInt("server-id");
		if (serverId < 1) {
			return;
		}
		
		Map<String, Object> map = new HashMap<>();
		map.put("tps", 20); // TODO tps
		map.put("time", System.currentTimeMillis());
		map.put("free-memory", Runtime.getRuntime().freeMemory());
		map.put("max-memory", Runtime.getRuntime().maxMemory());
		map.put("allocated-memory", Runtime.getRuntime().totalMemory());
		map.put("server-id", serverId);
		
		Map<String, Map<String, Object>> players = new HashMap<>();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			Map<String, Object> playerInfo = new HashMap<>();
			
			playerInfo.put("name", player.getName());
			
			Map<String, Object> location = new HashMap<>();
			Location loc = player.getLocation();
			location.put("world", loc.getWorld().getName());
			location.put("x", loc.getBlockX());
			location.put("y", loc.getBlockY());
			location.put("z", loc.getBlockZ());
			
			playerInfo.put("location", location);
			playerInfo.put("ip", player.getAddress().getAddress().getHostAddress());
			
			try {
				if (NamelessPlugin.permissions != null) playerInfo.put("rank", NamelessPlugin.permissions.getPrimaryGroup(player));
			} catch (UnsupportedOperationException e) {}
			
			try {
				if (NamelessPlugin.economy != null) playerInfo.put("balance", NamelessPlugin.economy.getBalance(player));
			} catch (UnsupportedOperationException e) {}
			
			Map<String, String> placeholders = new HashMap<>();
			
			Config.MAIN.getConfig().getStringList("upload-placeholders")
				.forEach(placeholder -> 
				placeholders.put(placeholder, NamelessPlugin.getInstance().papiParser.parse(player, placeholder)));
			
			playerInfo.put("placeholders", placeholders);
			
			playerInfo.put("login-time", NamelessPlugin.LOGIN_TIME.get(player.getUniqueId()));
			
			players.put(player.getUniqueId().toString().replace("-", ""), playerInfo);
		}
		
		map.put("players", players);
		
		String data = gson.toJson(map);
		
		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				NamelessPlugin.getInstance().api.submitServerInfo(data);
			} catch (ApiError e) {
				if (e.getErrorCode() == 27) {
					NamelessPlugin.getInstance().getLogger().warning("Server ID is incorrect. Please enter a correct server ID or disable the server data uploader.");
				} else {
					e.printStackTrace();
				}
			} catch (NamelessException e) {
				e.printStackTrace();
			}
		});
	}

}
