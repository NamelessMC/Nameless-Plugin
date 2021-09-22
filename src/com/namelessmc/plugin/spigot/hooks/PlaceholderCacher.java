package com.namelessmc.plugin.spigot.hooks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.spigot.Config;
import com.namelessmc.plugin.spigot.NamelessPlugin;

public class PlaceholderCacher implements Runnable {

	static final Map<UUID, Integer> CACHED_NOTIFICATION_COUNT = new HashMap<>(); // TODO Remove player when they leave the server?

	@Override
	public void run() {
		try {
			final int delay = Config.MAIN.getConfig().getInt("placeholders-request-delay", 2000);
			while (true) {
				Thread.sleep(500); // In case no players are online, wait in between checking for online players
				final Optional<NamelessAPI> optApi = NamelessPlugin.getInstance().getNamelessApi();
				if (optApi.isPresent()) {
					final NamelessAPI api = optApi.get();
					for (final Player player : Bukkit.getOnlinePlayers()) {
						Thread.sleep(delay);
						try {
							final Optional<NamelessUser> user = NamelessPlugin.getInstance().getApiProvider().useUuids()
									? api.getUser(player.getUniqueId()) : api.getUser(player.getName());
							if (!user.isPresent()) {
								continue;
							}
							final int notificationCount = user.get().getNotificationCount();
							CACHED_NOTIFICATION_COUNT.put(player.getUniqueId(), notificationCount);
						} catch (final NamelessException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

}
