package com.namelessmc.plugin.NamelessBungee.event;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessBungee.Config;
import com.namelessmc.plugin.NamelessBungee.Message;
import com.namelessmc.plugin.NamelessBungee.NamelessPlugin;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class PlayerLogin implements Listener {

	@EventHandler
	public void onPlayerJoin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ProxyServer.getInstance().getScheduler().runAsync(NamelessPlugin.getInstance(), () -> {
			NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			if (namelessPlayer.exists()) {
				if (namelessPlayer.isValidated()) {
					// Only show notifications if the player has validated their account
					userGetNotifications(player);
				} else {
					// If the player has not validated their account they get informed.
					player.sendMessage(Message.PLAYER_NOT_VALID.getComponents());
				}

				userGroupSync(player);
			}
		});
	}

	public void userGetNotifications(ProxiedPlayer player) {
		Configuration config = Config.MAIN.getConfig();
		if (config.getBoolean("join-notifications")) {
			try {
				NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
				int messages = namelessPlayer.getMessageCount();
				int alerts = namelessPlayer.getAlertCount();

				BaseComponent[] pmMessage = TextComponent.fromLegacyText(Message.NOTIFICATIONS_MESSAGES.getMessage().replace("%pms%", messages + ""));
				BaseComponent[] alertMessage = TextComponent.fromLegacyText(Message.NOTIFICATIONS_ALERTS.getMessage().replace("%alerts%", alerts + ""));
				BaseComponent[] noNotifications = Message.NO_NOTIFICATIONS.getComponents();
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
			} catch (NamelessException e) {
				BaseComponent[] errorMessage = TextComponent.fromLegacyText(Message.NOIFICATIONS_ERROR.getMessage().replace("%error%", e.getMessage()));
				player.sendMessage(errorMessage);
				e.printStackTrace();
			}
		}
	}
	
	public void userGroupSync(ProxiedPlayer player) {
		Configuration config = Config.MAIN.getConfig();
		if(config.getBoolean("group-synchronization.on-join")) {
			NamelessPlugin.syncGroup(player);
		}
	}

}