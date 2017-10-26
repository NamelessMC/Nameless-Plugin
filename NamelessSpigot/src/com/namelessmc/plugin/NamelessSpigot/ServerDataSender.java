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
		map.put("free-memory", Runtime.getRuntime().freeMemory());
		map.put("max-memory", Runtime.getRuntime().maxMemory());
		map.put("allocated-memory", Runtime.getRuntime().totalMemory());
		
		Map<String, Map<String, Object>> players = new HashMap<>();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			Map<String, Object> playerInfo = new HashMap<>();
			
			map.put("name", player.getName());
			
			Map<String, Object> location = new HashMap<>();
			Location loc = player.getLocation();
			location.put("world", loc.getWorld().getName());
			location.put("x", loc.getBlockX());
			location.put("y", loc.getBlockY());
			location.put("z", loc.getBlockZ());
			
			map.put("location", location);
			map.put("ip", player.getAddress().getAddress().getHostAddress());
			
			players.put(player.getUniqueId().toString().replace("-", ""), playerInfo);
		}
		
		String data = gson.toJson(map);
		
		System.out.println(data);
	}

}
