package com.namelessmc.plugin.NamelessSpigot.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;

import xyz.derkades.derkutils.caching.Cache;

public class PlaceholderCacher implements Runnable {
	
	@Override
	public void run() {
		try {
			while (true) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					try {
						int notificationCount = NamelessPlugin.getInstance().api
								.getPlayer(player.getUniqueId()).getNotifications().size();
						Cache.addCachedObject("nlmc-not" + player.getName(), notificationCount, 60);
					} catch (NamelessException e) {
						e.printStackTrace();
					}
					Thread.sleep(150);
				}
				Thread.sleep(2000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	

}
