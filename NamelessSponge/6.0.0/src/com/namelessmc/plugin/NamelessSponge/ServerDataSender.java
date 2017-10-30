package com.namelessmc.plugin.NamelessSponge;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.gson.Gson;

public class ServerDataSender implements Runnable {

	Gson gson = new Gson();
	
	@Override
	public void run() {
		Map<String, Object> map = new HashMap<>();
		map.put("tps", NamelessPlugin.getGame().getServer().getTicksPerSecond());
		map.put("time", System.currentTimeMillis());
		map.put("free-memory", Runtime.getRuntime().freeMemory());
		map.put("max-memory", Runtime.getRuntime().maxMemory());
		map.put("allocated-memory", Runtime.getRuntime().totalMemory());
		
		Map<String, Map<String, Object>> players = new HashMap<>();
		
		for (Player player : Sponge.getGame().getServer().getOnlinePlayers()) {
			Map<String, Object> playerInfo = new HashMap<>();
			
			playerInfo.put("name", player.getName());
			
			Map<String, Object> location = new HashMap<>();
			Location<World> loc = player.getLocation();
			location.put("world", player.getWorld().getName());
			location.put("x", loc.getBlockX());
			location.put("y", loc.getBlockY());
			location.put("z", loc.getBlockZ());
			
			playerInfo.put("location", location);
			playerInfo.put("ip", player.getConnection().getAddress().getAddress().getHostAddress());
			
			try {
				if (NamelessPlugin.permissions != null) playerInfo.put("rank", NamelessPlugin.permissions.getGroup(player));
			} catch (UnsupportedOperationException e) {}
			
			try {
				if (NamelessPlugin.economy != null) playerInfo.put("balance", NamelessPlugin.economy.getOrCreateAccount(player.getName()).get().getDefaultBalance(NamelessPlugin.economy.getDefaultCurrency()));
			} catch (UnsupportedOperationException e) {}
			
			playerInfo.put("login-time", NamelessPlugin.LOGIN_TIME.get(player.getUniqueId()));
			
			players.put(player.getUniqueId().toString().replace("-", ""), playerInfo);
		}
		
		map.put("players", players);
		
		String data = gson.toJson(map);
		
		System.out.println(data);
	}

}
