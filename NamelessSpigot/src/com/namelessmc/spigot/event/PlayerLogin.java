package com.namelessmc.spigot.event;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.spigot.Config;
import com.namelessmc.spigot.Message;
import com.namelessmc.spigot.NamelessPlugin;

public class PlayerLogin implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		
		NamelessPlugin.LOGIN_TIME.put(player.getUniqueId(), System.currentTimeMillis());
		
		final FileConfiguration config = Config.MAIN.getConfig();
		
		if (config.getBoolean("not-registered-join-message")) {
			Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
				try {
					final Optional<NamelessUser> user = NamelessPlugin.getApi().getUser(player.getUniqueId());
					if (!user.isPresent()) {
						Message.JOIN_NOTREGISTERED.send(player);
					}
				} catch (final NamelessException e) {
					e.printStackTrace();
				}
			});
		}
		
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
	}

}