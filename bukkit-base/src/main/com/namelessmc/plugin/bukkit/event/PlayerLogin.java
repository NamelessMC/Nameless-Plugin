package com.namelessmc.plugin.bukkit.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerLogin implements Listener {

	// TODO re-implement

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
//		final AbstractLogger logger = NamelessPluginSpigot.getInstance().getCommonLogger();
	}
//		final Configuration config = NamelessPluginSpigot.getInstance().getConfiguration().getMainConfig();
//
//		if (config.getBoolean("not-registered-join-message")) {
//			Bukkit.getScheduler().runTaskAsynchronously(NamelessPluginSpigot.getInstance(), () -> {
//				final Optional<NamelessAPI> optApi = NamelessPluginSpigot.getInstance().getNamelessApi();
//				if (optApi.isPresent()) {
//					try {
//						final Optional<NamelessUser> user = optApi.get().getUser(player.getUniqueId());
//						if (!user.isPresent()) {
//							Bukkit.getScheduler().runTask(NamelessPluginSpigot.getInstance(), () -> {
//								final Component message = NamelessPluginSpigot.getInstance().getLanguage().getComponent(Term.JOIN_NOTREGISTERED);
//								NamelessPluginSpigot.getInstance().adventure().player(event.getPlayer()).sendMessage(message);
//							});
//						}
//					} catch (final NamelessException e) {
//						logger.logException(e);
//					}
//				} else {
//					logger.warning("Not sending join message, API is not working properly.");
//				}
//			});
//		}

		/*if (!config.getBoolean("join-notifications")) {
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			if (!namelessPlayer.exists()) {
				return;
			}

			if (!namelessPlayer.isValidated()) {
				player.sendMessage(Message.ACCOUNT_NOT_VALIDATED.getMessage());
				return;
			}

			int messages;
			int alerts;*/

			//player.sendMessage("notifications are temporarely disabled");
			/*try {
				messages = namelessPlayer.getNotifications();
				alerts = namelessPlayer.getAlertCount();
			} catch (NamelessException e) {
				String errorMessage = Message.NOTIFICATIONS_ERROR.getMessage().replace("%error%", e.getMessage());
				player.sendMessage(errorMessage);
				e.printStackTrace();
				return;
			}


			String pmMessage = Message.NOTIFICATIONS_MESSAGES.getMessage().replace("%pms%", messages + "");
			String alertMessage = Message.NOTIFICATIONS_ALERTS.getMessage().replace("%alerts%", alerts + "");
			String noNotifications = Message.NO_NOTIFICATIONS.getMessage();

			if (alerts == 0 && messages == 0) {
				player.sendMessage(noNotifications);
			} else if (alerts == 0) {
				player.sendMessage(pmMessage);
			} else if (messages == 0) {
				player.sendMessage(alertMessage);
			} else {
				player.sendMessage(alertMessage);
				player.sendMessage(pmMessage);
			}

		});*/
//	}

}
