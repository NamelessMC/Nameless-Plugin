package com.namelessmc.plugin.spigot.hooks;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
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
						final Optional<NamelessUser> user = NamelessPlugin.getApi().getUser(player.getUniqueId());
						if (!user.isPresent()) {
							continue;
						}
						final int notificationCount = user.get().getNotificationCount();
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
