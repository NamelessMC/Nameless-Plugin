package com.namelessmc.plugin.bukkit.event;

import org.bukkit.event.Listener;

public class PlayerBan implements Listener {

	// TODO re-implement

//	@EventHandler(priority = EventPriority.MONITOR)
//	public void onBan(final PlayerKickEvent event) {
//		final Player player = event.getPlayer();
//		if (!player.isBanned()) {
//			return;
//		}
//
//		final Configuration config = NamelessPluginSpigot.getInstance().getConfiguration().getMainConfig();
//
//		if (!config.getBoolean("auto-ban-on-website", false)) {
//			return;
//		}
//
//		final UUID uuid = player.getUniqueId();
//		final String name = player.getName();
//		final Logger logger = NamelessPluginSpigot.getInstance().getLogger();
//
//		Bukkit.getScheduler().runTaskAsynchronously(NamelessPluginSpigot.getInstance(), () -> {
//			Optional<NamelessAPI> optApi = NamelessPluginSpigot.getInstance().getNamelessApi();
//			if (optApi.isPresent()) {
//				NamelessAPI api = optApi.get();
//				try {
//					Optional<NamelessUser> optUser = NamelessPluginSpigot.getInstance().getApiProvider().userFromPlayer(api, uuid, name);
//					if (optUser.isPresent()) {
//						NamelessUser user = optUser.get();
//						user.banUser();
//						logger.info("Banned " + name + " on the website.");
//					} else {
//						logger.info(name + " does not have a website account.");
//					}
//				} catch (NamelessException e) {
//					logger.warning("An error occurred while trying to find " + name + "'s website account: " + e.getMessage());
//				}
//			} else {
//				logger.warning("Skipped trying to ban " + name + " on website, API is not working properly.");
//			};
//		});
//	}

}
