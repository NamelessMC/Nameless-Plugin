package com.namelessmc.plugin.spigot.event;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import net.md_5.bungee.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class PlayerBan implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBan(final PlayerKickEvent event) {
		final Player player = event.getPlayer();
		if (!player.isBanned()) {
			return;
		}

		final Configuration config = NamelessPlugin.getInstance().getConfiguration().getMainConfig();

		if (!config.getBoolean("auto-ban-on-website", false)) {
			return;
		}

		final UUID uuid = player.getUniqueId();
		final String name = player.getName();
		final Logger logger = NamelessPlugin.getInstance().getLogger();

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			Optional<NamelessAPI> optApi = NamelessPlugin.getInstance().getNamelessApi();
			if (optApi.isPresent()) {
				NamelessAPI api = optApi.get();
				try {
					Optional<NamelessUser> optUser = NamelessPlugin.getInstance().getApiProvider().userFromPlayer(api, uuid, name);
					if (optUser.isPresent()) {
						NamelessUser user = optUser.get();
						user.banUser();
						logger.info("Banned " + name + " on the website.");
					} else {
						logger.info(name + " does not have a website account.");
					}
				} catch (NamelessException e) {
					logger.warning("An error occured while trying to find " + name + "'s website account: " + e.getMessage());
				}
			} else {
				logger.warning("Skipped trying to ban " + name + " on website, API is not working properly.");
			};
		});
	}

}
