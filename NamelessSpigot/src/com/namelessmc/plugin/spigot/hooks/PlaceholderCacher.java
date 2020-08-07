package com.namelessmc.plugin.spigot.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.spigot.Config;
import com.namelessmc.plugin.spigot.NamelessPlugin;

import xyz.derkades.derkutils.caching.Cache;

public class PlaceholderCacher implements Runnable {
	
	@Override
	public void run() {
		try {
			final int delay = Config.MAIN.getConfig().getInt("placeholders-request-delay", 5000);
			while (true) {
				Thread.sleep(500); // In case no players are online, wait in between checking for online players
				for (final Player player : Bukkit.getOnlinePlayers()) {
					
					Thread.sleep(delay);
					try {
						final NamelessPlayer nameless = NamelessPlugin.getInstance().api.getPlayer(player.getUniqueId());
						
						if (!(nameless.exists() && nameless.isValidated())) {
							continue;
						}
						
						final int notificationCount = nameless.getNotifications().size();
						Cache.set("nlmc-not" + player.getName(), notificationCount, 60);
					} catch (final NamelessException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	

}
