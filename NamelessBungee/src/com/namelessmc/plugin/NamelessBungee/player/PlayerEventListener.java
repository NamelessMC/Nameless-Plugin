package com.namelessmc.plugin.NamelessBungee.player;

import java.util.logging.Level;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessBungee.Chat;
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

public class PlayerEventListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ProxyServer.getInstance().getScheduler().runAsync(NamelessPlugin.getInstance(), () -> {
			NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			if (namelessPlayer.exists()) {
				if (namelessPlayer.isValidated()) {
					// shouldnt be outisde validated cause if your not validated you cant check anything.
					userGetNotifications(player);
				} else {
					player.sendMessage(Message.PLAYER_NOT_VALID.getComponents());
				}
				// theese should be outside, better!
				userNameCheck(player);
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

	public void userGroupSync(ProxiedPlayer player){
		Configuration config = Config.MAIN.getConfig();
		if (config.getBoolean("group-synchronization")) {
			Configuration permissionConfig = Config.MAIN.getConfig();
			for (String groupID : permissionConfig.getSection("permissions").getKeys()) {
				NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
				if (String.valueOf(namelessPlayer.getGroupID()).equals(groupID)) {
					return;
				} else if (player.hasPermission(permissionConfig.getString("permissions" + groupID))) {
					Integer previousgroup = namelessPlayer.getGroupID();
					BaseComponent[] successPlayerMessage = Message.GROUP_SYNC_PLAYER_ERROR.getComponents();
					try {
						namelessPlayer.setGroup(Integer.parseInt(groupID));
						Chat.log(Level.INFO, "Successfully changed " + player.getName() + "'s group from "
								+ previousgroup + " to " + groupID + "!");
						player.sendMessage(successPlayerMessage);
					} catch (NumberFormatException e) {
						Chat.log(Level.WARNING, "The Group ID is not a Integer/Number!");
					} catch (NamelessException e) {
						BaseComponent[] errorPlayerMessage = TextComponent.fromLegacyText(Message.GROUP_SYNC_PLAYER_ERROR.getMessage().replace("%error%", e.getMessage()));
						Chat.log(Level.WARNING, "Error changing &c"
								+ player.getName() + "'s group: " + e.getMessage());
						player.sendMessage(errorPlayerMessage);
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void userNameCheck(ProxiedPlayer player) {
		Configuration playerData = Config.PLAYER_INFO.getConfig();

		if (playerData.getBoolean("update-username")) {
			//Save data if not in file
			if (!playerData.contains(player.getUniqueId().toString())) {
				playerData.set(player.getUniqueId().toString() + ".username", player.getName());
				return;
			}
			
			//If the name in the file is different, the player has changed they name
			String previousName = playerData.getString(player.getUniqueId() + ".username");			
			if (!previousName.equals(player.getName())) {
				Chat.log(Level.INFO, "Detected that " + player.getName() + " has changed his/her username.");
	
				//Update name in file
				playerData.set(player.getUniqueId() + ".Username", player.getName());

				//Now change username on website
				NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
				try {
					namelessPlayer.updateUsername(player.getName());
					BaseComponent[] successMessage = Message.USERNAME_SYNC_SUCCESS.getComponents();
					Chat.log(Level.INFO, "Updated " + player.getName() + "'s username in the website");
					player.sendMessage(successMessage);
				} catch (NamelessException e) {
					BaseComponent[] errorMessage = TextComponent.fromLegacyText(Message.USERNAME_SYNC_ERROR.getMessage().replace("%error%", e.getMessage()));
					Chat.log(Level.WARNING,"Failed updating " + player.getName() + "'s username in the website, Error:" + e.getMessage());
					player.sendMessage(errorMessage);
					e.printStackTrace();
				}
			}
		}
	}

}