package com.namelessmc.plugin.spigot.event;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Optional;
import java.util.UUID;

public class PlayerBan implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBan(PlayerKickEvent event) {
		Player player = event.getPlayer();
		if (!player.isBanned()) {
			return;
		}

		if (!NamelessPlugin.getInstance().getConfig().getBoolean("auto-ban-on-website", false)) {
			return;
		}

		UUID uuid = player.getUniqueId();
		String name = player.getName();

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			Optional<NamelessAPI> optApi = NamelessPlugin.getInstance().getNamelessApi();
			if (optApi.isPresent()) {
				NamelessAPI api = optApi.get();
				try {
					Optional<NamelessUser> optUser = NamelessPlugin.getInstance().getApiProvider().useUuids()
							? api.getUser(uuid)
							: api.getUser(name);
					if (optUser.isPresent()) {
						NamelessUser user = optUser.get();
						throw new UnsupportedOperationException("Website does not have API to ban player yet");
					} else {
						NamelessPlugin.getInstance().getLogger().info(name + " does not have a website account.");
					}
				} catch (NamelessException e) {
					NamelessPlugin.getInstance().getLogger().warning("An error occured while trying to find " + name + "'s website account: " + e.getMessage());
				}
			} else {
				NamelessPlugin.getInstance().getLogger().warning("Skipped trying to ban " + name + " on website, API is not working properly.");
			};
		});
	}

}
