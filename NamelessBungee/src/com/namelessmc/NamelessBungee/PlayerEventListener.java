package com.namelessmc.NamelessBungee;

import java.util.logging.Level;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class PlayerEventListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ProxyServer.getInstance().getScheduler().runAsync(NamelessPlugin.getInstance(), () -> {
			NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			if (namelessPlayer.exists()) {
				if (namelessPlayer.isValidated()) {
					userGetNotification(player);
			
					try {
						userNameCheck(player);
						userGroupSync(player);
					} catch (NamelessException e) {
						e.printStackTrace();
					}
				} else {
					player.sendMessage(NamelessMessages.PLAYER_NOT_VALID.getComponents());
				}
			}
		});
	}
	
	public void userGetNotification(ProxiedPlayer player) {
		Configuration config = Config.MAIN.getConfig();
		if (config.getBoolean("join-notifications")) {
			try {
				NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
				int messages = namelessPlayer.getMessageCount();
				int alerts = namelessPlayer.getAlertCount();
	
				BaseComponent[] pmMessage = TextComponent.fromLegacyText(NamelessMessages.PM_NOTIFICATIONS_MESSAGE.getMessage().replace("%pms%", messages + ""));
				BaseComponent[] alertMessage = TextComponent.fromLegacyText(NamelessMessages.ALERTS_NOTIFICATIONS_MESSAGE.getMessage().replace("%alerts%", alerts + ""));
				BaseComponent[] noNotifications = NamelessMessages.NO_NOTIFICATIONS.getComponents();
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
				player.sendMessage(new ComponentBuilder("Error: " + e.getMessage()).color(ChatColor.RED).create());
				e.printStackTrace();
			}
		}
	}

	public void userGroupSync(ProxiedPlayer player) throws NamelessException {
		Configuration config = Config.MAIN.getConfig();
		if (config.getBoolean("group-synchronization")) {
			Configuration permissionConfig = Config.MAIN.getConfig();
			for (String groupID : permissionConfig.getSection("permissions").getKeys()) {
				NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
				if (String.valueOf(namelessPlayer.getGroupID()).equals(groupID)) {
					return;
				} else if (player.hasPermission(permissionConfig.getString("permissions" + groupID))) {
					namelessPlayer.setGroup(Integer.parseInt(groupID));
				}
			}
		}
	}
	
	public void userNameCheck(ProxiedPlayer player) throws NamelessException {
		Configuration playerData = Config.PLAYER_INFO.getConfig();

		if (playerData.getBoolean("update-username")) {
			//Save data if not in file
			if (!playerData.contains(player.getUniqueId().toString())) {
				playerData.set(player.getUniqueId().toString() + ".Username", player.getName());
				return;
			}
			
			//If the name in the file is different, the player has changed they name
			String previousName = playerData.getString(player.getUniqueId() + ".Username");
			if (!previousName.equals(player.getName())) {
				NamelessChat.log(Level.INFO, "&cDetected that &a" + player.getName() + " &chas changed his/her username.");
	
				//Update name in file
				playerData.set(player.getUniqueId() + ".Username", player.getName());

				//Now change username on website
				NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
				namelessPlayer.updateUsername(player.getName());
			}
		}
	}

}