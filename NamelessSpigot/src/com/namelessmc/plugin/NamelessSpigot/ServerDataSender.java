package com.namelessmc.plugin.NamelessSpigot;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;

public class ServerDataSender extends BukkitRunnable {

	Gson gson = new Gson();
	
	@Override
	public void run() {
		Map<String, Object> map = new HashMap<>();
		map.put("tps", 20); // TODO tps
		map.put("time", System.currentTimeMillis());
		map.put("free-memory", Runtime.getRuntime().freeMemory());
		map.put("max-memory", Runtime.getRuntime().maxMemory());
		map.put("allocated-memory", Runtime.getRuntime().totalMemory());
		
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
			playerInfo.put("rank", NamelessPlugin.permissions.getPrimaryGroup(player));
			playerInfo.put("money", NamelessPlugin.economy == null ? -1 : NamelessPlugin.economy.getBalance(player));
			playerInfo.put("login-time", NamelessPlugin.LOGIN_TIME.get(player.getUniqueId()));
			
			players.put(player.getUniqueId().toString().replace("-", ""), playerInfo);
		}
		
		map.put("players", players);
		
		String data = gson.toJson(map);
		
		System.out.println(data);
	}

}
